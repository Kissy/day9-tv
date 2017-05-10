package tv.day9.apk.free.activity.phone;

import android.support.v4.app.Fragment;
import tv.day9.apk.free.fragment.FreeVideoDetailFragment;

/**
 * @inheritDoc
 */
public class VideoDetailActivity extends tv.day9.apk.activity.phone.VideoDetailActivity {

    /**
     * @inheritDoc
     */
    @Override
    protected Fragment onCreatePane() {
        return new FreeVideoDetailFragment();
    }

}
