package tv.day9.apk.activity.phone;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import tv.day9.apk.activity.BaseSinglePaneActivity;
import tv.day9.apk.fragment.DownloadsFragment;
import tv.day9.apk.worker.DownloadWorker;

public class DownloadsActivity extends BaseSinglePaneActivity {

    /**
     * @inheritDoc
     */
    @Override
    protected Fragment onCreatePane() {
        return new DownloadsFragment();
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void onPause() {
        super.onPause();
        DownloadWorker.setUpdateStatus(false);
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void onResume() {
        super.onPause();
        DownloadWorker.setUpdateStatus(true);
    }
}
