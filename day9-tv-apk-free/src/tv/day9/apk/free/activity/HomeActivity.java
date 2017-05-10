package tv.day9.apk.free.activity;

import android.support.v4.app.Fragment;
import tv.day9.apk.free.fragment.FreeDashboardFragment;

/**
 * The HomeActivity
 */
public class HomeActivity extends tv.day9.apk.activity.HomeActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();

    /**
     * @inheritDoc
     */
    @Override
    protected Fragment onCreatePane() {
        return new FreeDashboardFragment();
    }
}
