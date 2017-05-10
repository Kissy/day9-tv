package tv.day9.apk.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import tv.day9.apk.R;
import tv.day9.apk.activity.phone.PreferencesActivity;
import tv.day9.apk.activity.phone.VideosActivity;
import tv.day9.apk.activity.tablet.PreferencesTabletActivity;
import tv.day9.apk.activity.tablet.VideosMultiPaneActivity;
import tv.day9.apk.widget.SimpleMenu;
import tv.day9.apk.worker.DownloadWorker;

/**
 * A class that handles some common activity-related functionality in the app, such as setting up
 * the action bar. This class provides functioanlity useful for both phones and tablets, and does
 * not require any Android 3.0-specific features.
 */
public class ActivityHelper {
    protected Activity activity;

    private SimpleMenu menu;
    private ImageView pauseSeparator;

    /**
     * Factory method for creating {@link ActivityHelper} objects for a given activity. Depending
     * on which device the app is running, either a basic helper or Honeycomb-specific helper will
     * be returned.
     *
     * @param activity The activity.
     * @return The activity helper.
     */
    public static ActivityHelper createInstance(Activity activity) {
        return UIUtils.isHoneycomb() ? new ActivityHelperHoneycomb(activity) : new ActivityHelper(activity);
    }

    protected ActivityHelper(Activity activity) {
        this.activity = activity;
    }

    public void onPostCreate(Bundle savedInstanceState) {
        // Create the action bar
        if (menu == null) {
            menu = new SimpleMenu(activity);
        }

        activity.onCreatePanelMenu(Window.FEATURE_OPTIONS_PANEL, menu);
        activity.onPreparePanel(Window.FEATURE_OPTIONS_PANEL, null, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            addActionButtonCompatFromMenuItem(item);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_search) {
            activity.onSearchRequested();
            return true;
        } else if (i == R.id.menu_settings) {
            goPreferences();
            return true;
        }
        return false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            goPreferences();
            return true;
        }
        return false;
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goHome();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            activity.onSearchRequested();
            return true;
        }
        return false;
    }

    /**
     * Method, to be called in <code>onDEstroy</code>.
     */
    public void onDestroy() {
    }

    /**
     * Method, to be called in <code>onPostCreate</code>, that sets up this activity as the
     * home activity for the app.
     */
    public void setupHomeActivity() {
    }

    /**
     * Method, to be called in <code>onPostCreate</code>, that sets up this activity as a
     * sub-activity in the app.
     */
    public void setupSubActivity() {
    }

    /**
     * Invoke "home" action, returning to {@link tv.day9.apk.activity.phone.VideosActivity}.
     */
    public void goHome() {
        if (activity instanceof VideosActivity || activity instanceof VideosMultiPaneActivity) {
            return;
        }

        final Intent intent = IntentUtils.getHomeActivityIntent(activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);

        if (!UIUtils.isHoneycomb()) {
            activity.overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
        }
    }

    /**
     * Invoke "preferences" action, returning to {@link tv.day9.apk.activity.phone.PreferencesActivity}.
     */
    public void goPreferences() {
        if (activity instanceof PreferencesActivity || activity instanceof PreferencesTabletActivity) {
            return;
        }

        activity.startActivity(IntentUtils.getPreferencesActivityIntent());
    }

    /**
     * Sets up the action bar with the given title. If title is null, then
     * the app logo will be shown instead of a title. Otherwise, a home button and title are
     * visible.
     *
     * @param title The title.
     */
    public void setupActionBar(CharSequence title) {
        final ViewGroup actionBarCompat = getActionBarCompat();
        if (actionBarCompat == null) {
            return;
        }

        LinearLayout.LayoutParams springLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.FILL_PARENT, 1);

        View.OnClickListener homeClickListener = new View.OnClickListener() {
            public void onClick(View view) {
                goHome();
            }
        };

        if (title != null) {
            // Add Home button
            addActionButtonCompat(R.drawable.ic_launcher, R.string.description_home, homeClickListener, true);

            // Add title text
            TextView titleText = new TextView(activity, null, R.attr.actionbarCompatTextStyle);
            titleText.setLayoutParams(springLayoutParams);
            titleText.setText(title);
            actionBarCompat.addView(titleText);
        } else {
            // Add logo
            ImageButton logo = new ImageButton(activity, null, R.attr.actionbarCompatLogoStyle);
            logo.setOnClickListener(homeClickListener);
            actionBarCompat.addView(logo);

            // Add spring (dummy view to align future children to the right)
            View spring = new View(activity);
            spring.setLayoutParams(springLayoutParams);
            actionBarCompat.addView(spring);
        }
    }

    /**
     * Sets the action bar title to the given string.
     *
     * @param title The title.
     */
    public void setActionBarTitle(CharSequence title) {
        ViewGroup actionBar = getActionBarCompat();
        if (actionBar == null) {
            return;
        }

        TextView titleText = (TextView) actionBar.findViewById(R.id.actionbar_compat_text);
        if (titleText != null) {
            titleText.setText(title);
        }
    }

    /**
     * Returns the {@link android.view.ViewGroup} for the action bar on phones (compatibility action bar).
     * Can return null, and will return null on Honeycomb.
     *
     * @return The view group.
     */
    public ViewGroup getActionBarCompat() {
        return (ViewGroup) activity.findViewById(R.id.actionbar_compat);
    }

    /**
     * Adds an action bar button to the compatibility action bar (on phones).
     *
     * @param iconResId      The icon res.
     * @param textResId      The text res.
     * @param clickListener  The click listener.
     * @param separatorAfter The after separator.
     * @return The view.
     */
    private View addActionButtonCompat(int iconResId, int textResId, View.OnClickListener clickListener, boolean separatorAfter) {
        final ViewGroup actionBar = getActionBarCompat();
        if (actionBar == null) {
            return null;
        }

        // Create the separator
        ImageView separator = new ImageView(activity, null, R.attr.actionbarCompatSeparatorStyle);
        separator.setLayoutParams(new ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.FILL_PARENT));

        // Create the button
        ImageButton actionButton = new ImageButton(activity, null, R.attr.actionbarCompatButtonStyle);
        actionButton.setLayoutParams(new ViewGroup.LayoutParams(
                (int) activity.getResources().getDimension(R.dimen.actionbar_compat_height),
                ViewGroup.LayoutParams.FILL_PARENT));
        actionButton.setImageResource(iconResId);
        actionButton.setScaleType(ImageView.ScaleType.CENTER);
        actionButton.setContentDescription(activity.getResources().getString(textResId));
        actionButton.setOnClickListener(clickListener);

        // Add separator and button to the action bar in the desired order
        if (!separatorAfter) {
            actionBar.addView(separator);
        }

        actionBar.addView(actionButton);

        if (separatorAfter) {
            actionBar.addView(separator);
        }

        return actionButton;
    }

    /**
     * Adds an action button to the compatibility action bar, using menu information from a
     * {@link android.view.MenuItem}.
     *
     * @param item The menu item.
     * @return The view.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private View addActionButtonCompatFromMenuItem(final MenuItem item) {
        final ViewGroup actionBar = getActionBarCompat();
        if (actionBar == null) {
            return null;
        }

        // Create the separator with hack for pause / resume buttons
        ImageView separator = null;
        if (item.getItemId() != R.id.menu_resume) {
            separator = new ImageView(activity, null, R.attr.actionbarCompatSeparatorStyle);
            separator.setLayoutParams(new ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.FILL_PARENT));
            // Save it to hide it later.
            if (item.getItemId() == R.id.menu_pause) {
                pauseSeparator = separator;
            }
        }

        // Create the button
        ImageButton actionButton = new ImageButton(activity, null, R.attr.actionbarCompatButtonStyle);
        actionButton.setId(item.getItemId());
        actionButton.setLayoutParams(new ViewGroup.LayoutParams(
                (int) activity.getResources().getDimension(R.dimen.actionbar_compat_height),
                ViewGroup.LayoutParams.FILL_PARENT));
        actionButton.setImageDrawable(item.getIcon());
        actionButton.setScaleType(ImageView.ScaleType.CENTER);
        actionButton.setContentDescription(item.getTitle());
        actionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                activity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, item);
            }
        });

        // Hack for pause / resume buttons
        if (separator != null) {
            actionBar.addView(separator);
        }
        actionBar.addView(actionButton);

        // Add the custom refresh view
        if (!UIUtils.isHoneycomb() && item.getItemId() == R.id.actionbar_compat_menu_refresh) {
            ProgressBar indicator = UIUtils.createProgressBar(activity, R.attr.actionbarCompatProgressIndicatorStyle);
            indicator.setId(R.id.actionbar_compat_menu_refreshing);
            indicator.setVisibility(View.GONE);
            actionBar.addView(indicator);
        }

        return actionButton;
    }

    /**
     * Set the refreshing state.
     *
     * @param refreshing The refreshing state.
     */
    public void setRefreshActionItemState(boolean refreshing) {
        View refreshButton = activity.findViewById(R.id.actionbar_compat_menu_refresh);
        View refreshIndicator = activity.findViewById(R.id.actionbar_compat_menu_refreshing);

        if (refreshButton != null) {
            refreshButton.setVisibility(refreshing ? View.GONE : View.VISIBLE);
        }
        if (refreshIndicator != null) {
            refreshIndicator.setVisibility(refreshing ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Set the refreshing state.
     *
     * @param display Do we need to display the button ?
     */
    public void setDownloadActionItemState(boolean display) {
        View resumeButton = activity.findViewById(R.id.menu_resume);
        View pauseButton = activity.findViewById(R.id.menu_pause);

        if (display) {
            boolean running = DownloadWorker.isRunning();
            if (resumeButton != null) {
                resumeButton.setVisibility(running ? View.GONE : View.VISIBLE);
            }
            if (pauseButton != null) {
                pauseButton.setVisibility(running ? View.VISIBLE : View.GONE);
            }
            if (pauseSeparator != null) {
                pauseSeparator.setVisibility(View.VISIBLE);
            }
        } else {
            if (resumeButton != null) {
                resumeButton.setVisibility(View.GONE);
            }
            if (pauseButton != null) {
                pauseButton.setVisibility(View.GONE);
            }
            if (pauseSeparator != null) {
                pauseSeparator.setVisibility(View.GONE);
            }
        }
    }

}
