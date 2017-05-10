package tv.day9.apk.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import tv.day9.apk.R;
import tv.day9.apk.config.Constants;
import tv.day9.apk.handler.NotifyingAsyncQueryHandler;
import tv.day9.apk.util.ActivityHelper;
import tv.day9.apk.util.UIUtils;

public class PreferencesFragment extends android.preference.PreferenceFragment implements NotifyingAsyncQueryHandler.AsyncQueryListener, Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    protected static final String TAG = PreferencesFragment.class.getSimpleName();

    protected NotifyingAsyncQueryHandler queryHandler;

    /**
     * @inheritDoc
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        queryHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);

        findPreference(Constants.ACCOUNTS).setOnPreferenceClickListener(this);
        findPreference(Constants.NO_DOWNLOAD).setOnPreferenceChangeListener(this);
        findPreference(Constants.DOWNLOAD_WIFI_ONLY).setOnPreferenceChangeListener(this);
        findPreference(Constants.CLEAR_VIDEOS).setOnPreferenceClickListener(this);
        findPreference(Constants.CLEAR_DOWNLOADS).setOnPreferenceClickListener(this);
        findPreference(Constants.DELETE_DOWNLOADS).setOnPreferenceClickListener(this);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActivityHelper.createInstance(getActivity()).setupSubActivity();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onInsertComplete(int token, Object cookie, Uri uri) {
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onUpdateComplete(int token, Object cookie, int result) {
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onDeleteComplete(int token, Object cookie, int result) {
        UIUtils.onDeleteComplete(getActivity());
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        return UIUtils.onPreferenceClick(getActivity(), preference, queryHandler);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        return UIUtils.onPreferenceChange(getActivity(), preference, o);
    }
}
