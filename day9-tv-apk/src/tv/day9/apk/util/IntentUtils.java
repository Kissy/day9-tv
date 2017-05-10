package tv.day9.apk.util;

import android.content.Context;
import android.content.Intent;

import tv.day9.apk.activity.AccountsActivity;
import tv.day9.apk.activity.phone.DownloadsActivity;
import tv.day9.apk.activity.phone.PreferencesActivity;
import tv.day9.apk.activity.phone.VideoDetailActivity;
import tv.day9.apk.activity.phone.VideosActivity;
import tv.day9.apk.activity.tablet.PreferencesTabletActivity;
import tv.day9.apk.activity.tablet.VideosMultiPaneActivity;
import tv.day9.apk.config.Constants;
import tv.day9.apk.service.AppService;

/**
 * Intent utils
 */
public class IntentUtils {

    private static final String ACTIVITY_PACKAGE = "tv.day9.apk.activity";
    private static final String PHONE_ACTIVITY_PACKAGE = ACTIVITY_PACKAGE + ".phone";
    private static final String TABLET_ACTIVITY_PACKAGE = ACTIVITY_PACKAGE + ".tablet";
    private static final String SERVICE_PACKAGE = "tv.day9.apk.service";

    /**
     * Get the home activity intent.
     *
     * @return The home activity intent.
     */
    public static Intent getHomeActivityIntent(Context context) {
        if (UIUtils.isTablet(context)) {
            return new Intent(context, VideosMultiPaneActivity.class);
        } else {
            return new Intent(context, VideosActivity.class);
        }
    }

    /**
     * Get the accounts activity intent.
     *
     * @return The accounts activity intent.
     */
    public static Intent getAccountActivityIntent() {
        return createIntent(ACTIVITY_PACKAGE, AccountsActivity.class.getSimpleName());
    }

    /**
     * Get the videos activity intent.
     *
     * @param context The context.
     * @return The videos activity intent.
     */
    public static Intent getVideosActivityIntent(Context context) {
        if (UIUtils.isHoneycombTablet(context)) {
            return createIntent(TABLET_ACTIVITY_PACKAGE, VideosMultiPaneActivity.class.getSimpleName());
        } else {
            return createIntent(PHONE_ACTIVITY_PACKAGE, VideosActivity.class.getSimpleName());
        }
    }

    /**
     * Get the video detail activity intent.
     *
     * @return The video detail activity intent.
     */
    public static Intent getVideoDetailActivityIntent() {
        return createIntent(PHONE_ACTIVITY_PACKAGE, VideoDetailActivity.class.getSimpleName());
    }

    /**
     * Get the downloads activity intent.
     *
     * @return The downloads activity intent.
     */
    public static Intent getDownloadsActivityIntent() {
        return createIntent(PHONE_ACTIVITY_PACKAGE, DownloadsActivity.class.getSimpleName());
    }

    /**
     * Get the preferences activity intent.
     *
     * @return The preferences activity intent.
     */
    public static Intent getPreferencesActivityIntent() {
        if (UIUtils.isHoneycomb()) {
            return createIntent(TABLET_ACTIVITY_PACKAGE, PreferencesTabletActivity.class.getSimpleName());
        } else {
            return createIntent(PHONE_ACTIVITY_PACKAGE, PreferencesActivity.class.getSimpleName());
        }
    }

    /**
     * Get the downloads activity intent.
     *
     * @return The downloads activity intent.
     */
    public static Intent getAppServiceIntent() {
        return createIntent(SERVICE_PACKAGE, AppService.class.getSimpleName());
    }

    /**
     * Generic create intent.
     *
     * @param activityPackage The activity package.
     * @param simpleName      The simple name.
     * @return The created intent.
     */
    private static Intent createIntent(String activityPackage, String simpleName) {
        Intent intent = new Intent();
        intent.setClassName("tv.day9.apk", activityPackage + Constants.DOT + simpleName);
        return intent;
    }
}
