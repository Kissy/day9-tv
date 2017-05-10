package tv.day9.apk.free.activity.tablet;

import android.app.Fragment;
import tv.day9.apk.free.fragment.FreePreferencesFragment;

public class PreferencesTabletActivity extends tv.day9.apk.activity.tablet.PreferencesTabletActivity {

    /**
     * @inheritDoc
     */
    @Override
    protected Fragment getPreferenceFragment() {
        return new FreePreferencesFragment();
    }
}
