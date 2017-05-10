package tv.day9.apk.activity.tablet;

import android.app.Fragment;
import android.os.Bundle;
import tv.day9.apk.activity.BaseActivity;
import tv.day9.apk.fragment.PreferencesFragment;

/**
 * Preferences Activity.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: PreferencesTabletActivity.java 207 2012-01-29 01:50:25Z Kissy $
 */
public class PreferencesTabletActivity extends BaseActivity {

    /**
     * @inheritDoc
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content, getPreferenceFragment()).commit();
        getActivityHelper().setupActionBar(getTitle());
    }

    /**
     * Get the preference fragment.
     *
     * @return The preference fragment.
     */
    protected Fragment getPreferenceFragment() {
        return new PreferencesFragment();
    }

}
