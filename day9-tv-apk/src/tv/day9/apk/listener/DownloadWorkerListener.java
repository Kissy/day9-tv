package tv.day9.apk.listener;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;
import tv.day9.apk.R;
import tv.day9.apk.manager.AppRequestManager;
import tv.day9.apk.service.AppService;
import tv.day9.apk.worker.DownloadWorker;

/**
 * @author Guillaume Le Biller <lebiller@ekino.com>
 * @version $Id: DownloadWorkerListener.java 194 2012-01-24 14:53:01Z kissy $
 */
public class DownloadWorkerListener implements AppRequestManager.OnRequestFinishedListener {

    private Context context;
    private int requestId = -1;

    /**
     * Default constructor.
     *
     * @param context The context.
     */
    public DownloadWorkerListener(Context context) {
        this.context = context;
    }

    /**
     * @inheritDoc
     */
    @Override
    @SuppressWarnings("unchecked")
    public void onRequestFinished(int requestId, int resultCode, Bundle payload) {
        if (this.requestId == requestId) {
            this.requestId = -1;

            AppRequestManager.from(context).removeOnRequestFinishedListener(this);

            String errorMessage = payload.getString(DownloadWorker.HAS_ERROR_MESSAGE);
            if (errorMessage != null) {
                Toast.makeText(context.getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
            } else if (resultCode == AppService.ERROR_CODE) {
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                return;
            }

            // If we are still running,
            // That mean the download is finished.
            // Launch again to download the next one.
            if (payload.getBoolean(DownloadWorker.HAS_MORE_DOWNLOADS_KEY, false) && DownloadWorker.isRunning()) {
                DownloadWorker.checkForRunningStatus(context);
            }
        }
    }

    /**
     * Set the request id.
     *
     * @param requestId The request id.
     */
    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }
}
