/*
 * 2011 Foxykeep (http://datadroid.foxykeep.com)
 *
 * Licensed under the Beerware License :
 * 
 *   As long as you retain this notice you can do whatever you want with this stuff. If we meet some day, and you think
 *   this stuff is worth it, you can buy me a beer in return
 */
package tv.day9.apk.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.SparseArray;

import com.foxykeep.datadroid.requestmanager.RequestManager;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.Random;
import java.util.WeakHashMap;

import tv.day9.apk.service.AppService;
import tv.day9.apk.util.IntentUtils;

/**
 * This class is used as a proxy to call the Service. It provides easy-to-use
 * methods to call the service and manages the Intent creation. It also assures
 * that a request will not be sent again if an exactly identical one is already
 * in progress
 * 
 * @author Foxykeep
 */
public class AppRequestManager extends RequestManager {
    private static final int MAX_RANDOM_REQUEST_ID = 1000000;

    // Singleton management
    private static AppRequestManager sInstance;
    private static Random sRandom = new Random();

    public static AppRequestManager from(final Context context) {
        if (sInstance == null) {
            sInstance = new AppRequestManager(context);
        }

        return sInstance;
    }

    private SparseArray<Intent> mRequestSparseArray;
    private Context mContext;
    private final WeakHashMap<OnRequestFinishedListener, Object> mListenerList;
    private Handler mHandler = new Handler();
    private EvalReceiver mEvalReceiver = new EvalReceiver(mHandler);

    private AppRequestManager(final Context context) {
        mContext = context.getApplicationContext();
        mRequestSparseArray = new SparseArray<Intent>();
        mListenerList = new WeakHashMap<OnRequestFinishedListener, Object>();
    }

    /**
     * The ResultReceiver that will receive the result from the Service
     */
    private class EvalReceiver extends ResultReceiver {
        EvalReceiver(final Handler h) {
            super(h);
        }

        @Override
        public void onReceiveResult(final int resultCode, final Bundle resultData) {
            handleResult(resultCode, resultData);
        }
    }

    /**
     * Clients may implements this interface to be notified when a request is
     * finished
     * 
     * @author Foxykeep
     */
    public static interface OnRequestFinishedListener extends EventListener {

        /**
         * Event fired when a request is finished.
         * 
         * @param requestId The request Id (to see if this is the right request)
         * @param resultCode The result code (0 if there was no error)
         * @param payload The result of the service execution.
         */
        public void onRequestFinished(int requestId, int resultCode, Bundle payload);
    }

    /**
     * Add a {@link OnRequestFinishedListener} to this
     * {@link AppRequestManager}. Clients may use it in order to listen to
     * events fired when a request is finished.
     * <p>
     * <b>Warning !! </b> If it's an {@link Activity} that is used as a
     * Listener, it must be detached when {@link Activity#onPause} is called in
     * an {@link Activity}.
     * </p>
     * 
     * @param listener The listener to add to this
     *            {@link AppRequestManager} .
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void addOnRequestFinishedListener(final OnRequestFinishedListener listener) {
        synchronized(mListenerList) {
            if (!mListenerList.containsKey(listener)) {
                mListenerList.put(listener, null);
            }
        }
    }

    /**
     * Remove a {@link OnRequestFinishedListener} to this
     * {@link AppRequestManager}.
     * 
     * @param listener The listener to remove to this
     *            {@link AppRequestManager}.
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void removeOnRequestFinishedListener(final OnRequestFinishedListener listener) {
        if (listener == null) {
            return;
        }
        
        synchronized(mListenerList) {
            mListenerList.remove(listener);
        }
    }

    /**
     * Return whether a request (specified by its id) is still in progress or
     * not
     * 
     * @param requestId The request id
     * @return whether the request is still in progress or not.
     */
    public boolean isRequestInProgress(final int requestId) {
        return (mRequestSparseArray.indexOfKey(requestId) >= 0);
    }

    /**
     * This method is call whenever a request is finished. Call all the
     * available listeners to let them know about the finished request
     * 
     * @param resultCode The result code of the request
     * @param resultData The bundle sent back by the service
     */
    @SuppressWarnings({"WhileLoopReplaceableByForEach", "SynchronizationOnLocalVariableOrMethodParameter"})
    protected void handleResult(final int resultCode, final Bundle resultData) {
        // Get the request Id
        final int requestId = resultData.getInt(RECEIVER_EXTRA_REQUEST_ID);

        // Remove the request Id from the "in progress" request list
        mRequestSparseArray.remove(requestId);

        ArrayList<OnRequestFinishedListener> listeners = new ArrayList<OnRequestFinishedListener>();
        synchronized(mListenerList) {
            listeners.addAll(mListenerList.keySet());
        }

        // Make the callbacks
        for (OnRequestFinishedListener listener : listeners) {
            listener.onRequestFinished(requestId, resultCode, resultData);
        }
    }

    // This is where you will add your methods which will call the
    // service

    /**
     * Get the video list.
     *
     * @param lastVideoId The lastVideoId.
     * @return The request id.
     */
    public int getVideoList(final long lastVideoId) {
        // Check if a match to this request is already launched
        final int requestSparseArrayLength = mRequestSparseArray.size();
        for (int i = 0; i < requestSparseArrayLength; i++) {
            final Intent savedIntent = mRequestSparseArray.valueAt(i);

            if (savedIntent.getIntExtra(AppService.INTENT_EXTRA_WORKER_TYPE, -1) != AppService.WORKER_TYPE_VIDEO_LIST) {
                continue;
            }
            if (savedIntent.getIntExtra(AppService.INTENT_EXTRA_VIDEO_LIST_PAGE, -1) != lastVideoId) {
                continue;
            }
            if (!savedIntent.getBooleanExtra(AppService.INTENT_EXTRA_VIDEO_LIST_NEW_VIDEOS, false)) {
                continue;
            }

            return mRequestSparseArray.keyAt(i);
        }

        final int requestId = sRandom.nextInt(MAX_RANDOM_REQUEST_ID);

        final Intent intent = IntentUtils.getAppServiceIntent();
        intent.putExtra(AppService.INTENT_EXTRA_WORKER_TYPE, AppService.WORKER_TYPE_VIDEO_LIST);
        intent.putExtra(AppService.INTENT_EXTRA_RECEIVER, mEvalReceiver);
        intent.putExtra(AppService.INTENT_EXTRA_REQUEST_ID, requestId);
        intent.putExtra(AppService.INTENT_EXTRA_VIDEO_LIST_PAGE, lastVideoId);
        intent.putExtra(AppService.INTENT_EXTRA_VIDEO_LIST_NEW_VIDEOS, false);
        intent.putExtra(AppService.INTENT_EXTRA_VIDEO_LIST_NOTIFY, false);
        mContext.startService(intent);

        mRequestSparseArray.append(requestId, intent);

        return requestId;
    }

    /**
     * Get the video list.
     *
     * @param lastVideoId The lastVideoId.
     * @param notify Do we need to notify ?
     * @return The request id.
     */
    public int getNewVideoList(final long lastVideoId, boolean notify) {
        // Check if a match to this request is already launched
        final int requestSparseArrayLength = mRequestSparseArray.size();
        for (int i = 0; i < requestSparseArrayLength; i++) {
            final Intent savedIntent = mRequestSparseArray.valueAt(i);

            if (savedIntent.getIntExtra(AppService.INTENT_EXTRA_WORKER_TYPE, -1) != AppService.WORKER_TYPE_VIDEO_LIST) {
                continue;
            }
            if (savedIntent.getLongExtra(AppService.INTENT_EXTRA_VIDEO_LIST_PAGE, -1) != lastVideoId) {
                continue;
            }
            if (savedIntent.getBooleanExtra(AppService.INTENT_EXTRA_VIDEO_LIST_NEW_VIDEOS, false)) {
                continue;
            }

            return mRequestSparseArray.keyAt(i);
        }

        final int requestId = sRandom.nextInt(MAX_RANDOM_REQUEST_ID);

        final Intent intent = IntentUtils.getAppServiceIntent();
        intent.putExtra(AppService.INTENT_EXTRA_WORKER_TYPE, AppService.WORKER_TYPE_VIDEO_LIST);
        intent.putExtra(AppService.INTENT_EXTRA_RECEIVER, mEvalReceiver);
        intent.putExtra(AppService.INTENT_EXTRA_REQUEST_ID, requestId);
        intent.putExtra(AppService.INTENT_EXTRA_VIDEO_LIST_PAGE, lastVideoId);
        intent.putExtra(AppService.INTENT_EXTRA_VIDEO_LIST_NEW_VIDEOS, true);
        intent.putExtra(AppService.INTENT_EXTRA_VIDEO_LIST_NOTIFY, notify);
        mContext.startService(intent);

        mRequestSparseArray.append(requestId, intent);

        return requestId;
    }

    /**
     * Check if a request of type update video list
     * is currently running.
     *
     * @return True if the request is running.
     */
    public boolean isUpdatingNewVideoList() {
        // Check if a match to this request is already launched
        final int requestSparseArrayLength = mRequestSparseArray.size();
        for (int i = 0; i < requestSparseArrayLength; i++) {
            final Intent savedIntent = mRequestSparseArray.valueAt(i);

            if (savedIntent.getIntExtra(AppService.INTENT_EXTRA_WORKER_TYPE, -1) != AppService.WORKER_TYPE_VIDEO_LIST) {
                continue;
            }
            if (savedIntent.getBooleanExtra(AppService.INTENT_EXTRA_VIDEO_LIST_NEW_VIDEOS, false)) {
                continue;
            }

            return true;
        }

        return false;
    }

    /**
     * Download the file.
     *
     *
     *
     * @param videoParcel The video parcel.
     * @param file The file file to download.
     * @param videoPart The video part.
     * @return The request id.
     */
    /*public int initDownloadFile(VideoParcel videoParcel, final VideoPartsProto.VideoParts.VideoPart.VideoFile file, int videoPart) {
        if (videoParcel == null || file == null) {
            return -1;
        }

        // Check if a match to this request is already launched
        final int requestSparseArrayLength = mRequestSparseArray.size();
        for (int i = 0; i < requestSparseArrayLength; i++) {
            final Intent savedIntent = mRequestSparseArray.valueAt(i);

            if (savedIntent.getIntExtra(AppService.INTENT_EXTRA_WORKER_TYPE, -1) != AppService.WORKER_TYPE_DOWNLOAD_INIT) {
                continue;
            }
            VideoFileParcel videoFileParcel = savedIntent.getParcelableExtra(AppService.INTENT_EXTRA_DOWNLOAD_FILE);
            if (videoFileParcel != null && !file.getFile().equals(videoFileParcel.getFile())) {
                continue;
            }

            return mRequestSparseArray.keyAt(i);
        }

        final int requestId = sRandom.nextInt(MAX_RANDOM_REQUEST_ID);

        final Intent intent = IntentUtils.getAppServiceIntent();
        intent.putExtra(AppService.INTENT_EXTRA_WORKER_TYPE, AppService.WORKER_TYPE_DOWNLOAD_INIT);
        intent.putExtra(AppService.INTENT_EXTRA_RECEIVER, mEvalReceiver);
        intent.putExtra(AppService.INTENT_EXTRA_REQUEST_ID, requestId);
        intent.putExtra(AppService.INTENT_EXTRA_DOWNLOAD_FILE, new VideoFileParcel(videoParcel, file, videoPart));
        mContext.startService(intent);

        mRequestSparseArray.append(requestId, intent);

        return requestId;
    }*/

    /**
     * Process download pages.
     *
     * @return The request id.
     */
    public int processDownloads() {
        // Check if a match to this request is already launched
        final int requestSparseArrayLength = mRequestSparseArray.size();
        for (int i = 0; i < requestSparseArrayLength; i++) {
            final Intent savedIntent = mRequestSparseArray.valueAt(i);

            if (savedIntent.getIntExtra(AppService.INTENT_EXTRA_WORKER_TYPE, -1) != AppService.WORKER_TYPE_DOWNLOAD) {
                continue;
            }

            return mRequestSparseArray.keyAt(i);
        }

        final int requestId = sRandom.nextInt(MAX_RANDOM_REQUEST_ID);

        final Intent intent = IntentUtils.getAppServiceIntent();
        intent.putExtra(AppService.INTENT_EXTRA_WORKER_TYPE, AppService.WORKER_TYPE_DOWNLOAD);
        intent.putExtra(AppService.INTENT_EXTRA_RECEIVER, mEvalReceiver);
        intent.putExtra(AppService.INTENT_EXTRA_REQUEST_ID, requestId);
        mContext.startService(intent);

        mRequestSparseArray.append(requestId, intent);

        return requestId;
    }

    /**
     * Register or unregister the device to c2dm.
     *
     * @param accountName The account name.
     * @param register Register or unregister ?
     * @return The request id.
     */
    public int c2dmRegister(final String accountName, final boolean register) {
        // Check if a match to this request is already launched
        final int requestSparseArrayLength = mRequestSparseArray.size();
        for (int i = 0; i < requestSparseArrayLength; i++) {
            final Intent savedIntent = mRequestSparseArray.valueAt(i);

            if (savedIntent.getIntExtra(AppService.INTENT_EXTRA_WORKER_TYPE, -1) != AppService.WORKER_TYPE_C2DM) {
                continue;
            }

            return mRequestSparseArray.keyAt(i);
        }

        final int requestId = sRandom.nextInt(MAX_RANDOM_REQUEST_ID);

        final Intent intent = IntentUtils.getAppServiceIntent();
        intent.putExtra(AppService.INTENT_EXTRA_WORKER_TYPE, AppService.WORKER_TYPE_C2DM);
        intent.putExtra(AppService.INTENT_EXTRA_RECEIVER, mEvalReceiver);
        intent.putExtra(AppService.INTENT_EXTRA_REQUEST_ID, requestId);
        intent.putExtra(AppService.INTENT_EXTRA_C2DM_ACCOUNT, accountName);
        intent.putExtra(AppService.INTENT_EXTRA_C2DM_STATUS, register);
        mContext.startService(intent);

        mRequestSparseArray.append(requestId, intent);

        return requestId;
    }

    /**
     * Register or unregister the device for c2dm.
     *
     * @param deviceRegistrationId The device id.
     * @param register Register or unregister ?
     * @return The request id.
     */
    public int registerDevice(final String deviceRegistrationId, final boolean register) {
        // Check if a match to this request is already launched
        final int requestSparseArrayLength = mRequestSparseArray.size();
        for (int i = 0; i < requestSparseArrayLength; i++) {
            final Intent savedIntent = mRequestSparseArray.valueAt(i);

            if (savedIntent.getIntExtra(AppService.INTENT_EXTRA_WORKER_TYPE, -1) != AppService.WORKER_TYPE_REGISTER) {
                continue;
            }

            return mRequestSparseArray.keyAt(i);
        }

        final int requestId = sRandom.nextInt(MAX_RANDOM_REQUEST_ID);

        final Intent intent = IntentUtils.getAppServiceIntent();
        intent.putExtra(AppService.INTENT_EXTRA_WORKER_TYPE, AppService.WORKER_TYPE_REGISTER);
        intent.putExtra(AppService.INTENT_EXTRA_RECEIVER, mEvalReceiver);
        intent.putExtra(AppService.INTENT_EXTRA_REQUEST_ID, requestId);
        intent.putExtra(AppService.INTENT_EXTRA_REGISTER_DEVICE_ID, deviceRegistrationId);
        intent.putExtra(AppService.INTENT_EXTRA_REGISTER_STATUS, register);
        mContext.startService(intent);

        mRequestSparseArray.append(requestId, intent);

        return requestId;
    }
}
