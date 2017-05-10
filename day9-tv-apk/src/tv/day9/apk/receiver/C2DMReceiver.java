package tv.day9.apk.receiver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;

import tv.day9.apk.config.Constants;
import tv.day9.apk.listener.VideoListListener;
import tv.day9.apk.manager.AppRequestManager;

/**
 * Receive a push message from the Cloud to Device Messaging (C2DM) service.
 * This class should be modified to include functionality specific to your
 * application. This class must have a no-arg constructor and pass the sender id
 * to the superclass constructor.
 */
public class C2DMReceiver extends C2DMBaseReceiver {

    private static final String TAG = C2DMReceiver.class.getSimpleName();

    private static int requestId = -1;

    public C2DMReceiver() {
        super(Constants.SENDER_ID);
    }

    /**
     * Called when a registration token has been received.
     *
     * @param context the Context
     * @param deviceRegistrationId the device registration id as a String
     */
    @Override
    public void onRegistered(Context context, String deviceRegistrationId) {
        requestId = AppRequestManager.from(context).registerDevice(deviceRegistrationId, true);
    }

    /**
     * Called when the device has been unregistered.
     *
     * @param context the Context
     */
    @Override
    public void onUnregistered(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        String deviceRegistrationID = sharedPreferences.getString(Constants.DEVICE_REGISTRATION_ID, null);
        requestId = AppRequestManager.from(context).registerDevice(deviceRegistrationID, false);
    }

    /**
     * Called on registration error. This is called in the context of a Service
     * - no dialog or UI.
     *
     * @param context the Context
     * @param errorId an error message, defined in {@link com.google.android.c2dm.C2DMBaseReceiver}
     */
    @Override
    public void onError(Context context, String errorId) {
        Log.e(TAG, "Error while trying to register or unregister : " + errorId);
    }

    /**
     * Called when a cloud message has been received.
     */
    @Override
    public void onMessage(Context context, Intent intent) {
        String message = intent.getExtras().getString(Constants.C2DM_MESSAGE_EXTRA);
        if (Constants.C2DM_MESSAGE_SYNC.equals(message)) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
            SharedPreferences commonSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            if (commonSharedPreferences.getBoolean(Constants.UPDATE_VIDEOS_IN_BACKGROUND, true)) {
                fetchMoreData(context, sharedPreferences);
            } else {
                sharedPreferences.edit().putBoolean(Constants.NEED_VIDEOS_UPDATE, true).commit();
            }
        }
    }

    /**
     * Fetch more data from server.
     *
     * @param context The context.
     * @param sharedPreferences The shared preferences.
     */
    private void fetchMoreData(Context context, SharedPreferences sharedPreferences) {
        VideoListListener videoListListener = new VideoListListener(this);
        AppRequestManager.from(context).addOnRequestFinishedListener(videoListListener);

        long id = sharedPreferences.getLong(Constants.LATEST_VIDEO_ID, 0L);
        if (id == 0L) {
            videoListListener.setRequestId(AppRequestManager.from(context).getVideoList(0L));
        } else {
            videoListListener.setRequestId(AppRequestManager.from(context).getNewVideoList(id, true));
        }
    }

    /**
     * Get the request id.
     *
     * @return The request id.
     */
    public static int getRequestId() {
        return requestId;
    }

    /**
     * Set the request id.
     *
     * @param requestId The request id.
     */
    public static void setRequestId(int requestId) {
        C2DMReceiver.requestId = requestId;
    }
}