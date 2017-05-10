package tv.day9.apk.provider;

import android.net.Uri;

import tv.day9.apk.config.Constants;

/**
 * {@link AppContent} is the superclass of the various classes of content
 * stored by {@link AppProvider}.
 */
public abstract class AppContent {
    public static final Uri CONTENT_URI = Uri.parse(Constants.CONTENT + AppProvider.AUTHORITY);
    public static final String PATH_SEARCH = "search";
}
