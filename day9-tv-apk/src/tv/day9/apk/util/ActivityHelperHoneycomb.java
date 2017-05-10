package tv.day9.apk.util;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * An extension of {@link ActivityHelper} that provides Android 3.0-specific functionality for
 * Honeycomb tablets. It thus requires API level 11.
 */
public class ActivityHelperHoneycomb extends ActivityHelper {
    private Menu mOptionsMenu;

    protected ActivityHelperHoneycomb(Activity activity) {
        super(activity);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        // Do nothing in onPostCreate. ActivityHelper creates the old action bar, we don't
        // need to for Honeycomb.
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionsMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Handle the HOME / UP affordance. Since the app is only two levels deep
                // hierarchically, UP always just goes home.
                goHome();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setupHomeActivity() {
        super.setupHomeActivity();
        // NOTE: there needs to be a content view set before this is called, so this method
        // should be called in onPostCreate.
        activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_USE_LOGO, ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setupSubActivity() {
        super.setupSubActivity();
        // NOTE: there needs to be a content view set before this is called, so this method
        // should be called in onPostCreate.
        activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setActionBarTitle(CharSequence title) {
        activity.getActionBar().setTitle(title);
    }


}
