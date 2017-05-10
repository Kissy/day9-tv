package tv.day9.apk.worker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.foxykeep.datadroid.exception.RestClientException;
import org.json.JSONException;
import org.xml.sax.SAXException;
import tv.day9.apk.R;
import tv.day9.apk.config.Constants;
import tv.day9.apk.config.DownloadConstants;
import tv.day9.apk.listener.DownloadWorkerListener;
import tv.day9.apk.manager.AppRequestManager;
import tv.day9.apk.provider.impl.DownloadDAO;
import tv.day9.apk.util.IntentUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadWorker {
    private static final String TAG = DownloadWorker.class.getSimpleName();

    public static final String HAS_MORE_DOWNLOADS_KEY = "hasMoreDownloads";
    public static final String HAS_ERROR_MESSAGE = "hasErrorMessage";

    private static AtomicBoolean PAUSED = new AtomicBoolean(false);
    private static AtomicBoolean CANCELED = new AtomicBoolean(false);
    private static AtomicBoolean UPDATE_STATUS = new AtomicBoolean(false);
    private static String ERROR_MESSAGE = null;

    private static final int NOTIFICATION_ID = 1;
    private static final int BUFFER = 1024;
    private static final int UPDATE_INTERVAL = 2000;

    /**
     * Start the worker.
     *
     * @param context         The context.
     * @return Do we need to process more downloads ?
     * @throws IllegalStateException       Exception.
     * @throws java.io.IOException         Exception.
     * @throws java.net.URISyntaxException Exception.
     * @throws com.foxykeep.datadroid.exception.RestClientException
     *                                     Exception.
     * @throws javax.xml.parsers.ParserConfigurationException
     *                                     Exception.
     * @throws org.xml.sax.SAXException    Exception.
     * @throws org.json.JSONException      Exception.
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored", "UnusedAssignment"})
    public static boolean start(final Context context) throws IllegalStateException, IOException,
            URISyntaxException, RestClientException, ParserConfigurationException, SAXException, JSONException {
        
        if (PAUSED.get()) {
            return false;
        }

        Long id;
        String fullPath;
        String downloadUri;
        String videoTitle;
        int downloadTotalByte;

        Cursor cursor = null;
        try {
            cursor = findResumeDownload(context);
            // If no download to resume, try to find the next download.
            if (cursor.getCount() <= 0) {
                cursor.close();
                cursor = findNextDownload(context);
                // If still no download, then break
                if (cursor.getCount() <= 0) {
                    cursor.close();
                    return false;
                }
            }

            cursor.moveToFirst();
            fullPath = cursor.getString(DownloadDAO.CONTENT_DESTINATION_COLUMN);
            downloadUri = cursor.getString(DownloadDAO.CONTENT_URI_COLUMN);
            downloadTotalByte = cursor.getInt(DownloadDAO.CONTENT_TOTAL_BYTES_COLUMN);
            videoTitle = cursor.getString(DownloadDAO.CONTENT_TITLE_COLUMN);
            id = cursor.getLong(DownloadDAO.CONTENT_ID_COLUMN);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Uri updateUri = DownloadDAO.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();

        // Test if the external storage is availables
        if (externalStorageNotAvailable()) {
            ERROR_MESSAGE = context.getString(R.string.external_storage_not_ready);
            Log.e(TAG, "Impossible to download the file : " + ERROR_MESSAGE);
            return false;
        }

        // Find the directory path
        int lastSlashIndex = fullPath.lastIndexOf(Constants.SLASH);
        File path = new File(fullPath.substring(0, lastSlashIndex));
        if (!path.exists()) {
            path.mkdirs();
        }

        // Create file if needed
        File file = new File(path, fullPath.substring(lastSlashIndex + 1));
        if (!file.exists()) {
            file.createNewFile();
        }

        boolean success = true;
        if (file.length() != downloadTotalByte) {
            // Go for it
            updateDownloadStatus(context, updateUri, DownloadConstants.STATUS_RUNNING);

            // Create the notification
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            createNotification(context, notificationManager, videoTitle);

            // Download the file
            try {
                downloadFile(context, file, downloadUri, updateUri);
            } catch (Exception e) {
                success = false;
                ERROR_MESSAGE = e.getLocalizedMessage();
                Log.e(TAG, "Impossible to download the file : " + e.getMessage());
                updateDownloadStatus(context, updateUri, DownloadConstants.STATUS_CANCELED);
            } finally {
                notificationManager.cancel(NOTIFICATION_ID);
            }
        } else {
            updateDownloadStatus(context, updateUri, DownloadConstants.STATUS_SUCCESS);
            updateDownloadProgress(context, updateUri, downloadTotalByte);
        }

        return success;
    }

    /**
     * Test if the external storage is ready.
     *
     * @return The status of the external storage.
     */
    private static boolean externalStorageNotAvailable() {
        return !Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Update the pause status.
     *
     * @param context The context.
     * @param newStatus The new status.
     */
    public static void updatePauseStatus(Context context, boolean newStatus) {
        PAUSED.set(newStatus);
        checkForRunningStatus(context);
    }

    /**
     * Check for the running status.
     * Update the RUNNING boolean.
     *
     * @param context The context.
     */
    public static void checkForRunningStatus(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean overWifiOnly = sharedPreferences.getBoolean(Constants.DOWNLOAD_WIFI_ONLY, true);
        boolean noDownload = sharedPreferences.getBoolean(Constants.NO_DOWNLOAD, false);
        checkForRunningStatus(context, overWifiOnly, noDownload);
    }

    /**
     * Check for the running status.
     * Update the RUNNING boolean.
     *
     * @param context The context.
     * @param noDownload The pause status preference.
     */
    public static void updateNoDownload(Context context, boolean noDownload) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean overWifiOnly = sharedPreferences.getBoolean(Constants.DOWNLOAD_WIFI_ONLY, true);
        checkForRunningStatus(context, overWifiOnly, noDownload);
    }

    /**
     * Check for the running status.
     * Update the RUNNING boolean.
     *
     * @param context The context.
     * @param overWifiOnly The over wifi only preference.
     */
    public static void updateOverWifiOnly(Context context, boolean overWifiOnly) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean noDownload = sharedPreferences.getBoolean(Constants.NO_DOWNLOAD, false);
        checkForRunningStatus(context, overWifiOnly, noDownload);
    }
    
    /**
     * Check for the running status.
     * Update the RUNNING boolean.
     *
     * @param context The context.
     * @param overWifiOnly The over wifi only preference.
     * @param noDownload The download status preference.
     */
    public static void checkForRunningStatus(Context context, boolean overWifiOnly, boolean noDownload) {
        // Download allowed ?
        if (PAUSED.get()) {
            return;
        }

        if (noDownload) {
            PAUSED.set(true);
            return;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Connection active ?
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnected();
        if (!isConnected) {
            PAUSED.set(true);
            Toast.makeText(context.getApplicationContext(), R.string.connection_notavailable, Toast.LENGTH_LONG).show();
            return;
        }

        // Download over wifi only ?
        int wifiType = connectivityManager.getActiveNetworkInfo().getType();
        boolean isWifi = wifiType == ConnectivityManager.TYPE_WIFI || wifiType == ConnectivityManager.TYPE_WIMAX || wifiType == ConnectivityManager.TYPE_ETHERNET;
        if (overWifiOnly && !isWifi) {
            PAUSED.set(true);
            Toast.makeText(context.getApplicationContext(), R.string.connection_wifi_notavailable, Toast.LENGTH_LONG).show();
            return;
        }

        // If we are still good, try to launch a new processDownloads worker.
        DownloadWorkerListener downloadWorkerListener = new DownloadWorkerListener(context);
        AppRequestManager.from(context).addOnRequestFinishedListener(downloadWorkerListener);
        downloadWorkerListener.setRequestId(AppRequestManager.from(context).processDownloads());
    }

    /**
     * Download the file. Loop.
     *
     *
     *
     * @param context The context.
     * @param file The file to save.
     * @param downloadUri The uri to download.
     * @param updateUri The database uri.
     * @throws IOException Read / write exception.
     */
    private static void downloadFile(Context context, File file, String downloadUri, Uri updateUri) throws IOException {
        int downloaded = (int) file.length();
        URL url = new URL(downloadUri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty(Constants.RANGE, Constants.BYTES + downloaded + Constants.DASH);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
        FileOutputStream fileOutputStream = new FileOutputStream(file, downloaded != 0);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER);
        byte[] data = new byte[BUFFER];

        try {
            int x;
            long lastTimeUpdate = System.currentTimeMillis();
            while (true) {
                if (CANCELED.get()) {
                    CANCELED.set(false);
                    break;
                }
                if (PAUSED.get()) {
                    updateDownloadStatus(context, updateUri, DownloadConstants.STATUS_PENDING);
                    break;
                }

                x = inputStream.read(data, 0, BUFFER);
                if (x <= 0) {
                    updateDownloadStatus(context, updateUri, DownloadConstants.STATUS_SUCCESS);
                    break;
                }

                bufferedOutputStream.write(data, 0, x);
                downloaded += x;

                // DO we need update ?
                if (UPDATE_STATUS.get()) {
                    long timePassed = System.currentTimeMillis() - lastTimeUpdate;
                    if (timePassed > UPDATE_INTERVAL) {
                        lastTimeUpdate = System.currentTimeMillis();
                        updateDownloadProgress(context, updateUri, downloaded);
                    }
                }
            }
        } finally {
            updateDownloadProgress(context, updateUri, downloaded);
            bufferedOutputStream.close();
            fileOutputStream.close();
            inputStream.close();
            connection.disconnect();
        }
    }

    /**
     * Create the notification.
     *
     * @param context The context.
     * @param notificationManager The notification manager.
     * @param videoTitle The video title.
     */
    private static void createNotification(Context context, NotificationManager notificationManager, String videoTitle) {
        Notification notification = new Notification(R.drawable.ic_launcher, context.getText(R.string.notification_download_title), 0);
        Intent notificationIntent = IntentUtils.getDownloadsActivityIntent();
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, context.getText(R.string.notification_download_title), videoTitle, pendingIntent);
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Find the resume download.
     *
     * @param context The context.
     * @return The cursor pointing to it.
     */
    public static Cursor findResumeDownload(Context context) {
        return findDownloadWithStatus(context, DownloadConstants.STATUS_RUNNING);
    }

    /**
     * Find the next download.
     *
     * @param context The context.
     * @return The cursor pointing to it.
     */
    public static Cursor findNextDownload(Context context) {
        return findDownloadWithStatus(context, DownloadConstants.STATUS_PENDING);
    }

    /**
     * Find a download with given status.
     *
     * @param context The context.
     * @param status The given status.
     * @return The cursor pointing to it.
     */
    private static Cursor findDownloadWithStatus(Context context, int status) {
        String[] selectionArgs = new String[] {String.valueOf(status)};
        return context.getContentResolver().query(DownloadDAO.CONTENT_URI, DownloadDAO.CONTENT_PROJECTION,
                DownloadDAO.STATUS + Constants.SELECTION_EQUAL, selectionArgs, DownloadDAO.LAST_MODIFICATION + Constants.ASC);
    }

    /**
     * Update the download status.
     *
     * @param context The context.
     * @param uri The uri to update.
     * @param status The new status.
     */
    public static void updateDownloadStatus(Context context, Uri uri, int status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadDAO.STATUS, status);
        context.getContentResolver().update(uri, contentValues, null, null);
    }

    /**
     * Update the download status.
     *
     * @param context The context.
     * @param uri The uri to update.
     * @param progress The new progress.
     */
    public static void updateDownloadProgress(Context context, Uri uri, int progress) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadDAO.CURRENT_BYTES, progress);
        context.getContentResolver().update(uri, contentValues, null, null);
    }

    /**
     * Get the running status.
     *
     * @return The running status.
     */
    public static boolean isRunning() {
        return !PAUSED.get();
    }

    /**
     * Cancel the current download.
     */
    public static void cancelCurrentDownload() {
        CANCELED.set(true);
    }

    /**
     * Cancel the notification.
     *
     * @param context The context.
     */
    public static void cancelNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        cancelCurrentDownload();
    }

    /**
     * Set the update status.
     *
     * @param status The new status.
     */
    public static void setUpdateStatus(boolean status) {
        UPDATE_STATUS.set(status);
    }

    /**
     * Get the error message.
     *
     * @return The error message.
     */
    public static String getErrorMessage() {
        return ERROR_MESSAGE;
    }

    /**
     * Reset the error message.
     */
    public static void resetErrorMessage() {
        DownloadWorker.ERROR_MESSAGE = null;
    }
}
