package tv.day9.apk.free.activity.phone;

import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import tv.day9.apk.config.Constants;

public class PreferencesActivity extends tv.day9.apk.activity.phone.PreferencesActivity {
    /**
     * @inheritDoc
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
