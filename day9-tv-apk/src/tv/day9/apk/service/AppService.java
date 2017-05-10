package tv.day9.apk.service;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.foxykeep.datadroid.service.WorkerService;
import tv.day9.apk.model.VideoFileParcel;
import tv.day9.apk.worker.*;

/**
 * This class is called by the {@link tv.day9.apk.manager.AppRequestManager} through the
 * {@link Intent} system. Get the parameters stored in the {@link Intent} and
 * call the right Worker.
 *
 * @author Foxykeep
 */
public class AppService extends WorkerService {

    private static final String LOG_TAG = AppService.class.getSimpleName();

    // Max number of parallel threads used
    private static final int MAX_THREADS = 3;

    // Set a numeric constant for each worker (to distinguish them).
    // These constants will be sent in the Intent in order to see which worker
    // to call
    public static final int WORKER_TYPE_VIDEO_LIST = 0;
    public static final int WORKER_TYPE_DOWNLOAD_INIT = 1;
    public static final int WORKER_TYPE_DOWNLOAD = 2;
    public static final int WORKER_TYPE_REGISTER = 3;
    public static final int WORKER_TYPE_C2DM = 4;

    // Set a string constants for each param to send to the worker. You
    // should use these constants in the Intent as extra name
    public static final String INTENT_EXTRA_VIDEO_LIST_PAGE = "tv.day9.extras.videosPage";
    public static final String INTENT_EXTRA_VIDEO_LIST_NEW_VIDEOS = "tv.day9.extras.newVideos";
    public static final String INTENT_EXTRA_VIDEO_LIST_NOTIFY = "tv.day9.extras.notify";
    public static final String INTENT_EXTRA_DOWNLOAD_FILE = "tv.day9.extras.downloadFile";
    public static final String INTENT_EXTRA_REGISTER_DEVICE_ID = "tv.day9.extras.registerDeviceId";
    public static final String INTENT_EXTRA_REGISTER_STATUS = "tv.day9.extras.registerStatus";
    public static final String INTENT_EXTRA_C2DM_ACCOUNT = "tv.day9.extras.c2dmAccount";
    public static final String INTENT_EXTRA_C2DM_STATUS = "tv.day9.extras.c2dmStatus";

    /**
     * Default constructor.
     */
    public AppService() {
        super(MAX_THREADS);
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void onHandleIntent(final Intent intent) {
        final int workerType = intent.getIntExtra(INTENT_EXTRA_WORKER_TYPE, -1);

        try {
            final Bundle bundle = new Bundle();
            switch (workerType) {
                // Add a case per worker where you do the following
                // things :
                // - get the parameters for this worker (if any)
                // - either call a private method if it is a short work and
                // create
                // the Bundle to return (if any)
                // - or create the worker and start the worker and get the
                // returned
                // Bundle (if any)
                // - call sendSuccess() with the received Intent and the Bundle
                // (if
                // any)
                // See the PoC if you need more information.
                case WORKER_TYPE_VIDEO_LIST:
                    long videoId = intent.getLongExtra(INTENT_EXTRA_VIDEO_LIST_PAGE, Long.MAX_VALUE);
                    boolean newVideos = intent.getBooleanExtra(INTENT_EXTRA_VIDEO_LIST_NEW_VIDEOS, false);
                    boolean notify = intent.getBooleanExtra(INTENT_EXTRA_VIDEO_LIST_NOTIFY, true);
                    boolean hasMoreData = VideoListWorker.start(this, videoId, newVideos, notify);
                    bundle.putBoolean(VideoListWorker.HAS_MORE_DATA_KEY, hasMoreData);
                    sendSuccess(intent, bundle);
                    break;
                case WORKER_TYPE_DOWNLOAD_INIT:
                    DownloadInitWorker.start(this, intent.<VideoFileParcel>getParcelableExtra(INTENT_EXTRA_DOWNLOAD_FILE));
                    sendSuccess(intent, bundle);
                    break;
                case WORKER_TYPE_DOWNLOAD:
                    boolean moreDownloads = DownloadWorker.start(this);
                    bundle.putBoolean(DownloadWorker.HAS_MORE_DOWNLOADS_KEY, moreDownloads);
                    bundle.putString(DownloadWorker.HAS_ERROR_MESSAGE, DownloadWorker.getErrorMessage());
                    sendSuccess(intent, bundle);
                    break;
                case WORKER_TYPE_REGISTER:
                    String deviceRegistrationId = intent.getStringExtra(INTENT_EXTRA_REGISTER_DEVICE_ID);
                    boolean register = intent.getBooleanExtra(INTENT_EXTRA_REGISTER_STATUS, true);
                    DeviceRegisterWorker.start(this, deviceRegistrationId, register);
                    sendSuccess(intent, bundle);
                    break;
                case WORKER_TYPE_C2DM:
                    String accountName = intent.getStringExtra(INTENT_EXTRA_C2DM_ACCOUNT);
                    boolean c2dmRegister = intent.getBooleanExtra(INTENT_EXTRA_C2DM_STATUS, true);
                    C2dmRegisterWorker.start(this, accountName, c2dmRegister);
                    sendSuccess(intent, bundle);
                    break;

                // Not implemented
                default:
                    Log.e(LOG_TAG, "This worker type is not implemented");
                    sendFailure(intent, Bundle.EMPTY);
                    break;
            }
            // This block (which should be the last one in your implementation)
            // will catch all the RuntimeException and send you back an error
            // that you can manage. If you remove this catch, the
            // RuntimeException will still crash the Service but you will not be
            // informed (as it is in 'background') so you should never remove
            // this catch
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Global Error", e);
            sendFailure(intent, null);
        }
    }
}
