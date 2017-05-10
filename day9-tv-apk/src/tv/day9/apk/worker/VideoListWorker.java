package tv.day9.apk.worker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.foxykeep.datadroid.exception.RestClientException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import tv.day9.apk.R;
import tv.day9.apk.config.Constants;
import tv.day9.apk.config.DownloadConstants;
import tv.day9.apk.enums.EnumVideoType;
import tv.day9.apk.provider.impl.DownloadDAO;
import tv.day9.apk.provider.impl.VideoDAO;
import tv.day9.apk.util.IntentUtils;
import tv.day9.apk.util.UIUtils;

public class VideoListWorker {
    private static final String TAG = VideoListWorker.class.getSimpleName();
    private static final int NOTIFICATION_ID = 2;
    private static final String[] CANCELED_STATUS = new String[]{String.valueOf(DownloadConstants.STATUS_CANCELED)};
    private static final EnumVideoType[] videoTypes = new EnumVideoType[] {
        EnumVideoType.DAY9_DAILY, EnumVideoType.AHGL, EnumVideoType.GSPA, EnumVideoType.CSL, EnumVideoType.DREAMHACK
    };

    public static final String HAS_MORE_DATA_KEY = "hasMoreData";

    /**
     * Start the worker.
     *
     * @param context The context.
     * @param lastVideoId    The page to load.
     * @param newVideos     Do we retrieve new videos ?
     * @param notify Do we need to notify new videos ?
     * @throws IllegalStateException        Exception.
     * @throws IOException                  Exception.
     * @throws URISyntaxException           Exception.
     * @throws RestClientException          Exception.
     * @throws ParserConfigurationException Exception.
     * @throws SAXException                 Exception.
     * @throws JSONException                Exception.
     * @return Is there any more data to fetch ?
     */
    public static boolean start(final Context context, final long lastVideoId, boolean newVideos, boolean notify) throws IllegalStateException, IOException,
            URISyntaxException, RestClientException, ParserConfigurationException, SAXException, JSONException {
        JSONArray videos = new JSONArray();

        URL url = new URL(String.format(Constants.REMOTE_VIDEOS_URL, newVideos ? "new" : "old", lastVideoId));
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;
            StringBuilder total = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                total.append(line);
            }

            videos = new JSONArray(total.toString());
        } finally {
            urlConnection.disconnect();
        }

        // Adds the videos to the database
        final int videosLength = videos.length();
        if (videosLength > 0) {
            long newLastVideoId = lastVideoId;
            ContentValues[] valuesArray = new ContentValues[videosLength];
            for (int i = 0; i < videosLength; i++) {
                JSONObject video = (JSONObject) videos.get(i);
                valuesArray[i] = VideoDAO.getContentValues(video);
                
                long currentLastVideoId = video.getLong("timestamp");
                if (currentLastVideoId > newLastVideoId) {
                    newLastVideoId = currentLastVideoId;
                }
            }

            try {
                context.getContentResolver().bulkInsert(VideoDAO.CONTENT_URI, valuesArray);
            } catch (Exception e) {
                Log.e(TAG, "Exception while trying to save video list " + e);
            }

            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (newVideos && notify) {
                // Make a notification if needed
                if (defaultSharedPreferences.getBoolean(Constants.NOTIFY_NEW_VIDEOS_AVAILABLES, true)) {
                    createNewVideosAvailableNotification(context);
                }

                // Put new videos to download
                if (defaultSharedPreferences.getBoolean(Constants.AUTOMATICALLY_DOWNLOAD_VIDEOS, false)) {
                    addVideosToDownload(context, defaultSharedPreferences, videos);
                }
            }

            // Save last video id.
            if (newLastVideoId > lastVideoId) {
                SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
                sharedPreferences.edit().putLong(Constants.LATEST_VIDEO_ID, newLastVideoId).commit();
            }

            return newVideos || newLastVideoId > 1;
        }

        // in both case no videos returned mean
        // there is nothing more to fetch
        return false;
    }

    /**
     * Add videos to download.
     *
     *
     * @param context The context.
     * @param defaultSharedPreferences The shared preferences.
     * @param videosList The video list.
     * @throws IllegalStateException        Exception.
     * @throws IOException                  Exception.
     * @throws URISyntaxException           Exception.
     * @throws RestClientException          Exception.
     * @throws ParserConfigurationException Exception.
     * @throws SAXException                 Exception.
     * @throws JSONException                Exception.
     */
    private static void addVideosToDownload(Context context, SharedPreferences defaultSharedPreferences, JSONArray videosList) throws IOException,
            SAXException, URISyntaxException, JSONException, ParserConfigurationException, RestClientException {
        // If the video quality is not set.
        String videoQuality = defaultSharedPreferences.getString(Constants.AUTOMATIC_DOWNLOAD_QUALITY, Constants.STRING_EMPTY);
        if (videoQuality.length() == 0) {
            return;
        }

        // If the video types are not set.
        Set<String> videoTypes = getVideoTypesToDownload(defaultSharedPreferences);
        if (videoTypes == null || videoTypes.size() == 0) {
            return;
        }

        // Video media type
        //VideoPartsProto.VideoParts.VideoPart.VideoFile.VideoMediaType videoMediaType = VideoPartsProto.VideoParts.VideoPart.VideoFile.VideoMediaType.valueOf(videoQuality);

        // Count the current number of download
        Cursor downloadCursor = getDownloadsCursor(context);
        int downloadCount = downloadCursor.getCount();
        downloadCursor.close();

        int maxDownloadCount = Integer.valueOf(defaultSharedPreferences.getString(Constants.MAX_NUMBER_OF_DOWNLOADS, "10"));
        boolean automaticallyDeleteDownloads = defaultSharedPreferences.getBoolean(Constants.AUTOMATICALLY_DELETE_DOWNLOADS, true);

        /*for (VideosProto.Videos.Video video : videosList) {
            // If we do not delete downloads, and we have too many downlods, break.
            if (!automaticallyDeleteDownloads && downloadCount > maxDownloadCount) {
                break;
            }
            if (!videoTypes.contains(video.getType())) {
                continue;
            }
            
            VideoFileParcel videoFileParcel = new VideoFileParcel(video.getTitle(),  video.getDescription(), Constants.STRING_EMPTY, video.getType(), 0, 0, 0, 0, 0);
            List<VideoPartsProto.VideoParts.VideoPart> videoPartsList = VideoDetailFragment.getVideoPartsAsList(video.getVideoParts().toByteArray());
            downloadCount += addVideoPartsToDownload(context, videoFileParcel, videoPartsList, videoMediaType);
        }*/

        removeOldDownloads(context, automaticallyDeleteDownloads, maxDownloadCount);

        // Start downloads
        DownloadWorker.checkForRunningStatus(context);
    }

    /**
     * Remove old downloads if needed.
     * Also close the download cursor.
     *
     * @param context The context.
     * @param automaticallyDeleteDownloads If we need to delete it
     * @param maxDownloadCount The max download count.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void removeOldDownloads(Context context, boolean automaticallyDeleteDownloads, int maxDownloadCount) {
        // If we do delete old downloads & we have too many downloads, remove them.
        Map<String, String> idsFilesToRemove = new HashMap<String, String>();
        if (automaticallyDeleteDownloads) {
            // Refresh the cursor to include the new ones
            Cursor downloadCursor = getDownloadsCursor(context);
            int downloadCount = downloadCursor.getCount();

            while (downloadCount > maxDownloadCount) {
                if (!downloadCursor.moveToNext()) {
                    break;
                }

                // WARNING this is not really the URI column that we read here,
                // But with CONTENT_LITE projection, the 2 (index 1) column is the Destination column.
                idsFilesToRemove.put(downloadCursor.getString(DownloadDAO.CONTENT_ID_COLUMN),
                        downloadCursor.getString(DownloadDAO.CONTENT_URI_COLUMN));
                downloadCount --;
            }
            downloadCursor.close();
        }

        // If there is any downloads to remove, then delete it.
        if (idsFilesToRemove.size() > 0) {
            StringBuilder whereString = new StringBuilder();
            for (Map.Entry<String, String> entry : idsFilesToRemove.entrySet()) {
                if (whereString.length() > 0) {
                    whereString.append(Constants.OR);
                }
                whereString.append(DownloadDAO._ID).append(Constants.EQUAL).append(entry.getKey());
                
                // Remove file
                File fileToDelete = new File(entry.getValue());
                if (fileToDelete.exists()) {
                    fileToDelete.delete();
                }
            }

            // Bulk delete is more effective
            context.getContentResolver().delete(DownloadDAO.CONTENT_URI, whereString.toString(), null);
        }
    }

    /**
     * Get the download count.
     * 
     * @param context The context.
     * @return the downloads count.
     */
    private static Cursor getDownloadsCursor(Context context) {
        // Do select downloads with oldest first.
        return context.getContentResolver().query(DownloadDAO.CONTENT_URI, DownloadDAO.CONTENT_LITE_PROJECTION,
                DownloadDAO.STATUS + Constants.SELECTION_NOT_EQUAL, CANCELED_STATUS, DownloadDAO.LAST_MODIFICATION + Constants.ASC);
    }

    /**
     * Add a video parts to download.
     *
     * @param context The context.
     * @param videoFileParcel The video file parcel.
     * @param videoPartsList Tje video parts list.
     * @param videoMediaType The video media type.
     * @throws IllegalStateException        Exception.
     * @throws IOException                  Exception.
     * @throws URISyntaxException           Exception.
     * @throws RestClientException          Exception.
     * @throws ParserConfigurationException Exception.
     * @throws SAXException                 Exception.
     * @throws JSONException                Exception.
     * @return The number of download added.
     */
    /*private static int addVideoPartsToDownload(Context context, VideoFileParcel videoFileParcel,
            List<VideoPartsProto.VideoParts.VideoPart> videoPartsList,
            VideoPartsProto.VideoParts.VideoPart.VideoFile.VideoMediaType videoMediaType) throws IOException,
            SAXException, URISyntaxException, JSONException, ParserConfigurationException, RestClientException {
        int downloadAdded = 0;
        // For each parts
        for (VideoPartsProto.VideoParts.VideoPart videoPart : videoPartsList) {
            videoFileParcel.setVideoPart(videoPart.getPart());

            // For each files
            for (VideoPartsProto.VideoParts.VideoPart.VideoFile videoFile : videoPart.getFilesList()) {
                // Only for the good one
                if (videoMediaType.equals(videoFile.getType())) {
                    videoFileParcel.setFile(videoFile.getFile());
                    videoFileParcel.setSize(videoFile.getSize());
                    videoFileParcel.setHeight(videoFile.getHeight());
                    videoFileParcel.setWidth(videoFile.getWidth());
                    videoFileParcel.setDuration(videoFile.getDuration());
                    break;
                }
            }

            // Add it to download queue
            downloadAdded ++;
            DownloadInitWorker.start(context, videoFileParcel, false);
        }
        return downloadAdded;
    }*/

    /**
     * Get the preferences as Set<String>
     *     
     * @param defaultSharedPreferences The shared preferences.
     * @return The set of string preferences.
     */
    private static Set<String> getVideoTypesToDownload(SharedPreferences defaultSharedPreferences) {
        if (UIUtils.isHoneycomb()) {
            return defaultSharedPreferences.getStringSet(Constants.AUTOMATIC_DOWNLOAD_TYPE, null);
        } else {
            Set<String> enabledVideoTypes = new HashSet<String>();
            for (EnumVideoType type : videoTypes) {
                if (defaultSharedPreferences.getBoolean(Constants.AUTOMATIC_DOWNLOAD_TYPE + Constants.DOT + type, false)) {
                    enabledVideoTypes.add(type.name());
                }
            }
            return enabledVideoTypes;
        }
    }

    /**
     * Create a notification for new videos avilables.
     *
     * @param context The context.
     */
    private static void createNewVideosAvailableNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new Notification(R.drawable.ic_launcher, context.getText(R.string.notification_new_videos_available_title), System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, IntentUtils.getVideosActivityIntent(context), 0);

        notification.setLatestEventInfo(context, context.getText(R.string.notification_new_videos_available_title),
                context.getText(R.string.notification_new_videos_available), pendingIntent);

        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
