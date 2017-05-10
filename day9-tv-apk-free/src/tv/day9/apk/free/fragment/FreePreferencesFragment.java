package tv.day9.apk.free.fragment;

import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import tv.day9.apk.config.Constants;
import tv.day9.apk.fragment.PreferencesFragment;

/**
 * @inheritDoc
 */
public class FreePreferencesFragment extends PreferencesFragment {
    /**
     * @inheritDoc
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference(Constants.PREFERENCE_SCREEN);
        if (preferenceScreen != null) {
            preferenceScreen.removePreference(findPreference(Constants.DOWNLOAD_CATEGORY));
            preferenceScreen.removePreference(findPreference(Constants.AUTOMATIC_DOWNLOAD_CATEGORY));
        }
        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference(Constants.DATABASE_CATEGORY);
        if (preferenceCategory != null) {
            preferenceCategory.removePreference(findPreference(Constants.CLEAR_DOWNLOADS));
            preferenceCategory.removePreference(findPreference(Constants.DELETE_DOWNLOADS));
        }
    }
}
