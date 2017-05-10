package tv.day9.apk.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.Preference;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import tv.day9.apk.R;
import tv.day9.apk.config.Constants;
import tv.day9.apk.config.DownloadConstants;
import tv.day9.apk.enums.EnumVideoType;
import tv.day9.apk.handler.NotifyingAsyncQueryHandler;
import tv.day9.apk.provider.impl.DownloadDAO;
import tv.day9.apk.provider.impl.VideoDAO;
import tv.day9.apk.worker.DownloadInitWorker;
import tv.day9.apk.worker.DownloadWorker;

/**
 * An assortment of UI helpers.
 */
public class UIUtils {

    private static final String PREFERENCE = "Preferences";
    private static final String CLICK = "Click";

    /**
     * Is the devide a honeycomb device ?
     *
     * @return True or false.
     */
    public static boolean isHoneycomb() {
        // Can use static final constants like HONEYCOMB, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * Is the devide a froyo device ?
     *
     * @return True or false.
     */
    public static boolean isFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    /**
     * Is the devide a eclair device ?
     *
     * @return True or false.
     */
    public static boolean isEclair() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1;
    }

    /**
     * Is the device a tablet ?
     *
     * @param context The context.
     * @return True or false.
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * Is honeycomb tablet ?
     *
     * @param context The context.
     * @return True or false.
     */
    public static boolean isHoneycombTablet(Context context) {
        return isHoneycomb() && isTablet(context);
    }


    /**
     * Set the video type icon from the cursor.
     *
     * @param imageViewIcon The image view icon.
     * @param type The video type.
     */
    public static void setVideoTypeIcon(final ImageView imageViewIcon, final String type) {
        EnumVideoType videoType = EnumVideoType.fromString(type);
        switch (videoType) {
            case DAY9_DAILY:
                imageViewIcon.setImageResource(R.drawable.ic_list_day9);
                return;
            case GSPA:
                imageViewIcon.setImageResource(R.drawable.ic_list_gspa);
                return;
            case AHGL:
                imageViewIcon.setImageResource(R.drawable.ic_list_ahgl);
                return;
            case DREAMHACK:
                imageViewIcon.setImageResource(R.drawable.ic_list_dreamhack);
                return;
            case CSL:
                imageViewIcon.setImageResource(R.drawable.ic_list_csl);
                return;
            case AMAZON:
                imageViewIcon.setImageResource(R.drawable.ic_list_amazon);
                break;
            case ROOT_GAMINGS_WARZONE:
                imageViewIcon.setImageResource(R.drawable.ic_list_root_gaming);
                return;
            case BLIZZARD_IN_HOUSE_TOURNAMENT:
                imageViewIcon.setImageResource(R.drawable.ic_list_blizzard);
                break;
            case KING_OF_THE_BETA:
                imageViewIcon.setImageResource(R.drawable.ic_list_sc2_beta);
                return;
            case ZOTAC13:
                imageViewIcon.setImageResource(R.drawable.ic_list_zotac);
                return;
            case MONOBATTLE:
                imageViewIcon.setImageResource(R.drawable.ic_list_monobattle);
                return;

            case DIABLO_III_BETA:
                imageViewIcon.setImageResource(R.drawable.ic_list_diablo3);
                return;
            case ELDER_SCROLLS_V:
                imageViewIcon.setImageResource(R.drawable.ic_list_elder_scroll_v);
                break;
            case AMNESIA:
                imageViewIcon.setImageResource(R.drawable.ic_list_amnesia);
                break;
            default:
                imageViewIcon.setImageResource(R.drawable.ic_list_default);
                break;
        }
    }

    /**
     * Format the byte to be readable.
     * Default the SI to true.
     *
     * @param bytes The byte number.
     * @return The formatted value.
     */
    public static String formatSize(long bytes) {
        return formatSize(bytes, true);
    }

    /**
     * Format the byte to be readable.
     *
     * @param bytes The byte number.
     * @param si    Si units ?
     * @return The formatted value.
     */
    public static String formatSize(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + Constants.BYTE;
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? Constants.UNITS_SI : Constants.UNITS).charAt(exp - 1) + (si ? Constants.STRING_EMPTY : Constants.I);
        return String.format(Constants.UNIT_FORMAT, bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Get the string from download status.
     *
     * @param context The context.
     * @param downloadStatus The download status.
     * @return The string.
     */
    public static String getStringFromDownloadStatus(Context context, int downloadStatus) {
        switch (downloadStatus) {
            case DownloadConstants.STATUS_SUCCESS:
                return context.getString(R.string.download_success_status);
            case DownloadConstants.STATUS_RUNNING:
                return context.getString(R.string.download_running_status);
            case DownloadConstants.STATUS_PENDING:
                return context.getString(R.string.download_pending_status);
            case DownloadConstants.STATUS_CANCELED:
                return context.getString(R.string.download_canceled_status);
            default:
                return context.getString(R.string.download_default_status);
        }
    }

    /**
     * Remove the C2DM pereferences.
     *
     * @param sharedPreferences The shared preference.
     */
    public static void removeC2dmPreferences(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Constants.AUTH_COOKIE);
        editor.remove(Constants.NEED_VIDEOS_UPDATE); // Is only needed for C2DM
        editor.putString(Constants.DEVICE_REGISTRATION_ID, Constants.STRING_EMPTY);
        editor.commit();
    }

    /**
     * Handle on preference click.
     * 
     * @param context The context.
     * @param preference The preference.
     * @param queryHandler The query handler.
     * @return The status.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean onPreferenceClick(Context context, Preference preference, NotifyingAsyncQueryHandler queryHandler) {
        if (Constants.CLEAR_VIDEOS.equals(preference.getKey())) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
            sharedPreferences.edit().remove(Constants.LATEST_VIDEO_ID).remove(Constants.LAST_VIDEO_CHECK).commit();
            queryHandler.startDelete(VideoDAO.CONTENT_URI);
            return true;
        } else if (Constants.ACCOUNTS.equals(preference.getKey())) {
            context.startActivity(IntentUtils.getAccountActivityIntent());
            return true;
        } else if (Constants.CLEAR_DOWNLOADS.equals(preference.getKey())) {
            DownloadWorker.cancelNotification(context);
            queryHandler.startDelete(DownloadDAO.CONTENT_URI);
            return true;
        } else if (Constants.DELETE_DOWNLOADS.equals(preference.getKey())) {
            DownloadInitWorker.getUploadDirectory().delete();
            alertAndGoHome(context, R.string.files_deleted);
            return true;
        }

        return false;
    }

    /**
     * Handle on preference change.
     * 
     * @param context The context.
     * @param preference The preference.
     * @param o The object.
     * @return The status.
     */
    public static boolean onPreferenceChange(Context context, Preference preference, Object o) {
        if (Constants.DOWNLOAD_WIFI_ONLY.equals(preference.getKey())) {
            DownloadWorker.updateOverWifiOnly(context, Boolean.parseBoolean(o.toString()));
            return true;
        } else if (Constants.NO_DOWNLOAD.equals(preference.getKey())) {
            DownloadWorker.updateNoDownload(context, Boolean.parseBoolean(o.toString()));
            return true;
        }

        return false;
    }

    /**
     * Handle on delete complete.
     *
     * @param context The context.
     */
    public static void onDeleteComplete(Context context) {
        alertAndGoHome(context, R.string.data_removed);
    }

    /**
     * Create a toast message and redirect to home.
     *
     * @param context The context.
     * @param messageResId The message res id to display.
     */
    private static void alertAndGoHome(Context context, int messageResId) {
        Toast.makeText(context.getApplicationContext(), context.getString(messageResId), Toast.LENGTH_LONG).show();

        final Intent intent = IntentUtils.getHomeActivityIntent(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * Create the progress bar.
     *
     * @param context The context.
     * @param baseStyle The base style.
     * @return The Progress bar created.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public static ProgressBar createProgressBar(Context context, int baseStyle) {
        ProgressBar progressBar = new ProgressBar(context, null, baseStyle);

        final int buttonWidth = context.getResources().getDimensionPixelSize(R.dimen.actionbar_compat_height);
        final int buttonHeight = context.getResources().getDimensionPixelSize(R.dimen.actionbar_compat_height);
        final int progressIndicatorWidth = buttonWidth / 2;

        LinearLayout.LayoutParams indicatorLayoutParams = new LinearLayout.LayoutParams(progressIndicatorWidth, progressIndicatorWidth);
        indicatorLayoutParams.setMargins((buttonWidth - progressIndicatorWidth) / 2,
                (buttonHeight - progressIndicatorWidth) / 2,
                (buttonWidth - progressIndicatorWidth) / 2, 0);

        progressBar.setLayoutParams(indicatorLayoutParams);
        return progressBar;
    }
}
