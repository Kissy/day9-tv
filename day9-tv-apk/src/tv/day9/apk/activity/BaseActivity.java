package tv.day9.apk.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import tv.day9.apk.config.Constants;
import tv.day9.apk.util.ActivityHelper;

/**
 * A base activity that defers common functionality across app activities to an
 * {@link ActivityHelper}. This class shouldn't be used directly; instead, activities should
 * inherit from {@link BaseSinglePaneActivity} or {@link BaseMultiPaneActivity}.
 */
public abstract class BaseActivity extends FragmentActivity {
    final ActivityHelper activityHelper = ActivityHelper.createInstance(this);

    /**
     * @inheritDoc
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        activityHelper.onPostCreate(savedInstanceState);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return activityHelper.onKeyLongPress(keyCode, event) || super.onKeyLongPress(keyCode, event);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return activityHelper.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return activityHelper.onCreateOptionsMenu(menu) || super.onCreateOptionsMenu(menu);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return activityHelper.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
    
    /**
     * @inheritDoc
     */
    @Override
    protected void onDestroy() {
        activityHelper.onDestroy();
        super.onDestroy();
    }

    /**
     * Returns the {@link ActivityHelper} object associated with this activity.
     *
     * @return The activity helper.
     */
    protected ActivityHelper getActivityHelper() {
        return activityHelper;
    }

    /**
     * Set the refresh action item state.
     *
     * @param state The refresh action item state.
     */
    public void setRefreshActionItemState(boolean state) {
        activityHelper.setRefreshActionItemState(state);
    }

    /**
     * Set the display action item state.
     *
     * @param display The display action item state.
     */
    public void setDownloadActionItemState(boolean display) {
        activityHelper.setDownloadActionItemState(display);
    }

    /**
     * Takes a given intent and either starts a new activity to handle it (the default behavior),
     * or creates/updates a fragment (in the case of a multi-pane activity) that can handle the
     * intent.
     * <p/>
     * Must be called from the main (UI) thread.
     *
     * @param intent The intent to start the activity.
     */
    public void openActivityOrFragment(Intent intent) {
        // Default implementation simply calls startActivity
        startActivity(intent);
    }

    /**
     * Converts an intent into a {@link android.os.Bundle} suitable for use as fragment arguments.
     *
     * @param intent The intent to convert.
     * @return The created bundle.
     */
    public static Bundle intentToFragmentArguments(Intent intent) {
        Bundle arguments = new Bundle();
        if (intent == null) {
            return arguments;
        }

        final Uri data = intent.getData();
        if (data != null) {
            arguments.putParcelable(Constants._URI, data);
        }

        final Bundle extras = intent.getExtras();
        if (extras != null) {
            arguments.putAll(intent.getExtras());
        }

        return arguments;
    }

    /**
     * Converts a fragment arguments bundle into an intent.
     *
     * @param arguments The bundle to convert.
     * @return The created intent.
     */
    public static Intent fragmentArgumentsToIntent(Bundle arguments) {
        Intent intent = new Intent();
        if (arguments == null) {
            return intent;
        }

        final Uri data = arguments.getParcelable(Constants._URI);
        if (data != null) {
            intent.setData(data);
        }

        intent.putExtras(arguments);
        intent.removeExtra(Constants._URI);
        return intent;
    }
}
