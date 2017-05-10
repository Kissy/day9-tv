package tv.day9.apk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import tv.day9.apk.R;

/**
 * A {@link BaseActivity} that simply contains a single fragment. The intent used to invoke this
 * activity is forwarded to the fragment as arguments during fragment instantiation. Derived
 * activities should only need to implement
 * {@link tv.day9.apk.activity.BaseSinglePaneActivity#onCreatePane()}.
 */
public abstract class BaseSinglePaneActivity extends BaseActivity {
    /**
     * @inheritDoc
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlepane_empty);
        getActivityHelper().setupActionBar(getCustomTitle());

        final String customTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        getActivityHelper().setActionBarTitle(customTitle != null ? customTitle : getTitle());

        if (savedInstanceState == null) {
            Fragment fragment = onCreatePane();
            fragment.setArguments(intentToFragmentArguments(getIntent()));

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.root_container, fragment)
                    .commit();
        }
    }

    /**
     * Workaround for home page.
     *
     * @return The custom title.
     */
    protected CharSequence getCustomTitle() {
        return getTitle();
    }

    /**
     * Called in <code>onCreate</code> when the fragment constituting this activity is needed.
     * The returned fragment's arguments will be set to the intent used to invoke this activity.
     *
     * @return The fragment.
     */
    protected abstract Fragment onCreatePane();
}
