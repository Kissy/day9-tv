package tv.day9.apk.activity.phone;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import tv.day9.apk.activity.BaseSinglePaneActivity;
import tv.day9.apk.fragment.VideoDetailFragment;

public class VideoDetailActivity extends BaseSinglePaneActivity {

    /**
     * @inheritDoc
     */
    @Override
    protected Fragment onCreatePane() {
        return new VideoDetailFragment();
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();
    }

}
