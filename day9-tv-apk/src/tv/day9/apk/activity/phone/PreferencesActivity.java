package tv.day9.apk.activity.phone;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import tv.day9.apk.R;
import tv.day9.apk.config.Constants;
import tv.day9.apk.handler.NotifyingAsyncQueryHandler;
import tv.day9.apk.util.ActivityHelper;
import tv.day9.apk.util.UIUtils;

/**
 * Preferences Activity.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: PreferencesActivity.java 207 2012-01-29 01:50:25Z Kissy $
 */
public class PreferencesActivity extends PreferenceActivity implements NotifyingAsyncQueryHandler.AsyncQueryListener, Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    private NotifyingAsyncQueryHandler queryHandler;

    /**
     * @inheritDoc
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        queryHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);

        Preference accountPreference = findPreference(Constants.ACCOUNTS);
        if (accountPreference != null) {
            accountPreference.setOnPreferenceClickListener(this);
        }

        findPreference(Constants.CLEAR_VIDEOS).setOnPreferenceClickListener(this);
        findPreference(Constants.CLEAR_DOWNLOADS).setOnPreferenceClickListener(this);
        findPreference(Constants.DELETE_DOWNLOADS).setOnPreferenceClickListener(this);
        findPreference(Constants.DOWNLOAD_WIFI_ONLY).setOnPreferenceChangeListener(this);
        findPreference(Constants.NO_DOWNLOAD).setOnPreferenceChangeListener(this);

        findPreference(Constants.SOURCE_QUALITY).setSummary(getString(R.string.summary_quality, Constants.SOURCE_DISPLAY));
        findPreference(Constants.LD_QUALITY).setSummary(getString(R.string.summary_quality, Constants.LD_DISPLAY));
        findPreference(Constants.SD_QUALITY).setSummary(getString(R.string.summary_quality, Constants.SD_DISPLAY));
        findPreference(Constants.HD_QUALITY).setSummary(getString(R.string.summary_quality, Constants.HD_DISPLAY));
        
        Preference automaticDownloadType = findPreference(Constants.AUTOMATIC_DOWNLOAD_TYPE);
        if (automaticDownloadType != null) {
            findPreference(Constants.AUTOMATIC_DOWNLOAD_TYPE).setDependency(Constants.AUTOMATICALLY_DOWNLOAD_VIDEOS);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ActivityHelper.createInstance(this).setupSubActivity();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {}

    /**
     * @inheritDoc
     */
    @Override
    public void onInsertComplete(int token, Object cookie, Uri uri) {}

    /**
     * @inheritDoc
     */
    @Override
    public void onUpdateComplete(int token, Object cookie, int result) {}

    /**
     * @inheritDoc
     */
    @Override
    public void onDeleteComplete(int token, Object cookie, int result) {
        UIUtils.onDeleteComplete(this);
        overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        return UIUtils.onPreferenceClick(this, preference, queryHandler);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        return UIUtils.onPreferenceChange(this, preference, o);
    }
}
