package tv.day9.apk.worker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import com.foxykeep.datadroid.exception.RestClientException;
import org.json.JSONException;
import org.xml.sax.SAXException;
import tv.day9.apk.config.Constants;
import tv.day9.apk.config.DownloadConstants;
import tv.day9.apk.model.VideoFileParcel;
import tv.day9.apk.provider.impl.DownloadDAO;
import tv.day9.apk.util.UIUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class DownloadInitWorker {
    private static final String TAG = DownloadInitWorker.class.getSimpleName();

    private static File UPLOAD_DIRECTORY = null;

    /**
     * Get the upload directory.
     *
     * @return The upload directory.
     */
    public static File getUploadDirectory() {
        if (UPLOAD_DIRECTORY == null) {
            if (UIUtils.isFroyo()) {
                UPLOAD_DIRECTORY = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), Constants.DAY9TV_DIRECTORY);
            } else {
                UPLOAD_DIRECTORY = new File(Environment.getExternalStorageDirectory(), Constants.DAY9TV_DIRECTORY);
            }
        }

        return UPLOAD_DIRECTORY;
    }

    /**
     * Start the worker.
     *
     *
     * @param context         The context.
     * @param videoFileParcel The video file to download.
     * @return Is there any more data to fetch ?
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
    public static boolean start(final Context context, final VideoFileParcel videoFileParcel) throws IllegalStateException, IOException,
            URISyntaxException, RestClientException, ParserConfigurationException, SAXException, JSONException {
        return start(context, videoFileParcel, true);
    }

    /**
     * Start the worker.
     *
     *
     * @param context         The context.
     * @param videoFileParcel The video file to download.
     * @param startDownload   Start the download after.
     * @return Is there any more data to fetch ?
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
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean start(final Context context, final VideoFileParcel videoFileParcel, boolean startDownload) throws IllegalStateException, IOException,
            URISyntaxException, RestClientException, ParserConfigurationException, SAXException, JSONException {

        // Create directory
        if (!getUploadDirectory().exists()) {
            getUploadDirectory().mkdirs();
        }

        // Check for file
        int downloadStatus = DownloadConstants.STATUS_PENDING;
        File file = new File(getUploadDirectory(), videoFileParcel.getFile());
        if (file.exists() && file.length() == videoFileParcel.getSize()) {
            downloadStatus = DownloadConstants.STATUS_SUCCESS;
        }

        // Check for database
        Cursor cursor = findDownloadFromFile(context, file.getAbsolutePath());
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            long id = cursor.getLong(DownloadDAO.CONTENT_ID_COLUMN);
            Uri uri = DownloadDAO.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            DownloadWorker.updateDownloadStatus(context, uri, downloadStatus);
        } else {
            // Add to database
            ContentValues values = DownloadDAO.getContentValuesForNewDownload(context, videoFileParcel, file, downloadStatus);
            context.getContentResolver().insert(DownloadDAO.CONTENT_URI, values);
        }
        cursor.close();

        if (startDownload && downloadStatus == DownloadConstants.STATUS_PENDING) {
            // Start the download worker if needed.
            DownloadWorker.checkForRunningStatus(context);
        }

        return true;
    }

    /**
     * Find a download entry matching a file.
     *
     * @param context The context.
     * @param destination The destination file.
     * @return The cursor pointing to it.
     */
    private static Cursor findDownloadFromFile(Context context, String destination) {
        String selection = DownloadDAO.DESTINATION + Constants.SELECTION_EQUAL;
        String[] selectionArgs = new String[] {destination};
        return context.getContentResolver().query(DownloadDAO.CONTENT_URI, DownloadDAO.CONTENT_PROJECTION,
                selection, selectionArgs, DownloadDAO._ID + Constants.DESC);
    }
}
