package tv.day9.apk.config;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Constants.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: Constants.java 216 2012-02-05 12:04:06Z Kissy $
 */
public class Constants {
    /**
     * The AppEngine app name, used to construct the production service URL
     * below.
     */
    private static final String APP_NAME = "kissy-day9-tv";

    /**
     * The URL of the production service.
     */
    public static final String PROD_URL = "https://" + APP_NAME + ".appspot.com";

    /**
     * The C2DM sender ID for the server. A C2DM registration with this name
     * must exist for the app to function correctly.
     */
    public static final String SENDER_ID = "tv.day9.c2dm@gmail.com";

    /**
     * Key for auth cookie name in shared preferences.
     */
    public static final String AUTH_COOKIE = "authCookie";

    /**
     * Key for account name in shared preferences.
     */
    public static final String ACCOUNT_NAME = "accountName";

    /**
     * The auth token type.
     */
    public static final String AUTH_TOKEN_TYPE = "ah";

    /**
     * Key for device registration id in shared preferences.
     */
    public static final String DEVICE_REGISTRATION_ID = "deviceRegistrationID";

    /**
     * Key for need videos update in shared preferences.
     */
    public static final String NEED_VIDEOS_UPDATE = "needVideosUpdate";

    /**
     * Key for the latest video id fetched.
     */
    public static final String LATEST_VIDEO_ID = "latestVideoId";

    /**
     * Time zone
     */
    public static final TimeZone GMT_TIMEZONE = TimeZone.getTimeZone("GMT");

    /**
     * Video check timeout
     */
    public static final int VIDEO_CHECK_TIMEOUT = 6;

    /**
     * C2DM
     */
    public static final String C2DM_MESSAGE_EXTRA = "message";
    public static final String C2DM_MESSAGE_SYNC = "sync";

    /**
     * Preferences
     */
    public static final String PREFERENCE_SCREEN = "preference_screen";
    public static final String DOWNLOAD_CATEGORY = "download_category";
    public static final String AUTOMATIC_DOWNLOAD_CATEGORY = "automatic_download_category";
    public static final String DATABASE_CATEGORY = "database_category";
    public static final String UPDATE_VIDEOS_IN_BACKGROUND = "update_videos_in_background";
    public static final String NOTIFY_NEW_VIDEOS_AVAILABLES = "notify_new_videos_availables";
    public static final String AUTOMATICALLY_DOWNLOAD_VIDEOS = "automatically_download_videos";
    public static final String AUTOMATIC_DOWNLOAD_QUALITY = "automatic_download_quality";
    public static final String AUTOMATIC_DOWNLOAD_TYPE = "automatic_download_type";
    public static final String MAX_NUMBER_OF_DOWNLOADS = "max_number_of_downloads";
    public static final String AUTOMATICALLY_DELETE_DOWNLOADS = "automatically_delete_downloads";
    public static final String LAST_VIDEO_CHECK = "lastVideoCheck";
    public static final String ACCOUNTS = "accounts";
    public static final String CLEAR_VIDEOS = "clear_videos";
    public static final String CLEAR_DOWNLOADS = "clear_downloads";
    public static final String DELETE_DOWNLOADS = "delete_downloads";
    public static final String NO_DOWNLOAD = "no_download";
    public static final String DOWNLOAD_WIFI_ONLY = "download_wifi_only";
    public static final String VIDEO_QUALITY = "video_quality";
    public static final String DEFAULT_VIDEO_QUALITY = "hd";
    public static final String SOURCE_QUALITY = "source_quality";
    public static final String LD_QUALITY = "ld_quality";
    public static final String SD_QUALITY = "sd_quality";
    public static final String HD_QUALITY = "hd_quality";

    /*
     * URL suffix for the remote videos servlet and register device
     */
    //public static final String REMOTE_VIDEOS_URL = PROD_URL + "/remote/videos?last=%s&new=%s";
    public static final String REMOTE_VIDEOS_URL = "http://day9-tv.herokuapp.com/api/videos/%s/%s/";
    public static final String REMOTE_REGISTER_URL = PROD_URL + "/remote/register?registration=%s&account=%s&device=%s&register=%s";
    public static final String REMOTE_AUTH_URL = PROD_URL + "/_ah/login?continue=%s&auth=%s";

    /**
     * Key for shared preferences.
     */
    public static final String SHARED_PREFS = "Day9TV_".toUpperCase(Locale.ENGLISH) + "_PREFS";

    /**
     * The base url for blip tv files.
     */
    public static final String BLIP_FILE_BASE_URL = "http://blip.tv/file/get/";

    /**
     * Download Day9TV directory
     */
    public static final String DAY9TV_DIRECTORY = "Day9TV";

    /**
     * Quality Display
     */
    public static final String SOURCE_DISPLAY = "Source";
    public static final String LD_DISPLAY = "Blip LD";
    public static final String SD_DISPLAY = "Blip SD";
    public static final String HD_DISPLAY = "Blip HD";

    /**
     * Common used strings
     */
    public static final String STRING_EMPTY = "";
    public static final String SPACE = " ";
    public static final String SEPARATOR = " - ";
    public static final String SLASH = "/";
    public static final String SLASH_SPACE = " / ";
    public static final String LEFT_PARENTHESIS = " (";
    public static final String RIGHT_PARENTHESIS = ")";
    public static final String X = "x";
    public static final String DOT = ".";
    public static final String EQUAL = " = ";
    public static final String SELECTION_EQUAL = " = ?";
    public static final String SELECTION_NOT_EQUAL = " != ?";
    public static final String SELECTION_GREATER_THAN = " > ?";
    public static final String _URI = "_uri";
    public static final String DATATYPE_VIDEO = "video/*";
    public static final String DATATYPE_TEXT = "text/plain";
    public static final String LEFT_LIKE = " LIKE '%";
    public static final String OR = " OR ";
    public static final String RIGHT_LIKE = "%'";
    public static final String NUMERIC_TRUE = "1";
    public static final String CONTENT = "content://";
    public static final String ASC = " ASC";
    public static final String DESC = " DESC";
    public static final String INTEGER_PRIMARY_KEY = " integer primary key, ";
    public static final String TEXT_COMMA = " text, ";
    public static final String INTEGER_COMMA = " integer, ";
    public static final String BLOB_COMMA = " blob, ";
    public static final String INTEGER_SQLEND = " integer);";
    public static final String CREATE_TABLE = "create table ";
    public static final String DROP_TABLE = "drop table ";
    public static final String INSERT_INTO = "insert into ";
    public static final String COMMA = ", ";
    public static final String INTEGER_PRIMARY_KEY_AUTOINCREMENT = " INTEGER PRIMARY KEY AUTOINCREMENT,";
    public static final String BIGINT_SQLEND = " BIGINT);";
    public static final String SLASH_POUND = "/#";
    public static final String RANGE = "Range";
    public static final String BYTES = "bytes=";
    public static final String DASH = "-";
    public static final String BYTE = " B";
    public static final String I = "i";
    public static final String UNITS_SI = "kMGTPE";
    public static final String UNITS = "KMGTPE";
    public static final String UNIT_FORMAT = "%.1f %sB";
    public static final String RESPONSE_OK = "OK";
    public static final String COM_GOOGLE = "com.google";
}
