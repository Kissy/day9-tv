package tv.day9.apk.listener;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;
import tv.day9.apk.R;
import tv.day9.apk.manager.AppRequestManager;
import tv.day9.apk.service.AppService;

/**
 * @author Guillaume Le Biller <lebiller@ekino.com>
 * @version $Id: VideoListListener.java 195 2012-01-24 16:23:48Z kissy $
 */
public class VideoListListener implements AppRequestManager.OnRequestFinishedListener {
    
    private static final int NOTIFICATION_ID = 2;

    private Context context;
    private int requestId;

    /**
     * Default constrcutor.
     *
     * @param context The context.
     */
    public VideoListListener(Context context) {
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

            if (resultCode == AppService.ERROR_CODE) {
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.connection_error), Toast.LENGTH_LONG).show();
            }

            // TODO NotifyDatasetChanged if we display video list
        }
    }

    /**
     * Set the request id.
     *
     * @param requestId The request id to set.
     */
    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }
}
