package tv.day9.apk.provider.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.provider.BaseColumns;

import com.foxykeep.datadroid.provider.util.DatabaseUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tv.day9.apk.config.Constants;
import tv.day9.apk.model.VideoParcel;
import tv.day9.apk.provider.AppContent;
import tv.day9.apk.provider.AppProvider;
import tv.day9.apk.provider.DatabaseHelper;
import tv.day9.apk.provider.columns.VideoColumns;

/**
* Created by Guillaume on 25/08/13.
*/
public final class VideoDAO extends AppContent implements VideoColumns, BaseColumns {
    public static final String TABLE_NAME = "video";
    public static final Uri CONTENT_URI = Uri.parse(AppContent.CONTENT_URI + Constants.SLASH + TABLE_NAME);

    public static final String TYPE_ELEM_TYPE = "vnd.android.cursor.item/" + AppProvider.AUTHORITY + ".Video";
    public static final String TYPE_DIR_TYPE = "vnd.android.cursor.dir/" + AppProvider.AUTHORITY + ".Video";

    public static final int CONTENT_ID_COLUMN = 0;
    public static final int CONTENT_TYPE_COLUMN = 1;
    public static final int CONTENT_SUBTYPE_COLUMN = 2;
    public static final int CONTENT_TITLE_COLUMN = 3;
    public static final int CONTENT_SUBTITLE_COLUMN = 4;
    public static final int CONTENT_DESCRIPTION_COLUMN = 5;
    public static final int CONTENT_VIDEO_PARTS_COLUMN = 6;
    public static final int CONTENT_FAVORITE_COLUMN = 7;
    public static final String[] CONTENT_PROJECTION = new String[] {
        _ID, TYPE, SUBTYPE, TITLE, SUBTITLE, DESCRIPTION, VIDEO_PARTS, FAVORITE
    };

    // Add the other projections (if any) you will use using the same
    // model as the content projection
    public static final String[] CONTENT_PROJECTION_LITE = new String[] {
        _ID, TITLE, SUBTITLE, DESCRIPTION, VIDEO_PARTS
    };

    /**
     * Create the table.
     *
     * @param db The database.
     */
    public static void createTable(final SQLiteDatabase db) {
        final String s = Constants.LEFT_PARENTHESIS + _ID + Constants.INTEGER_PRIMARY_KEY
                + TYPE + Constants.TEXT_COMMA + SUBTYPE + Constants.TEXT_COMMA + TITLE + Constants.TEXT_COMMA
                + SUBTITLE + Constants.INTEGER_COMMA + DESCRIPTION + Constants.BLOB_COMMA
                + VIDEO_PARTS + Constants.BLOB_COMMA + FAVORITE + Constants.INTEGER_SQLEND;

        db.execSQL(Constants.CREATE_TABLE + TABLE_NAME + s);

        // Indexes
        db.execSQL(DatabaseUtil.getCreateIndexString(TABLE_NAME, TYPE));
        db.execSQL(DatabaseUtil.getCreateIndexString(TABLE_NAME, SUBTITLE));
        db.execSQL(DatabaseUtil.getCreateIndexString(TABLE_NAME, FAVORITE));
    }

    /**
     * Upgrade the table.
     *
     * @param db The database.
     * @param oldVersion The old version.
     * @param newVersion The new version.
     */
    public static void upgradeTable(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        try {
            db.execSQL(Constants.DROP_TABLE + TABLE_NAME);
        } catch (final SQLException ignored) {}
        createTable(db);
    }

    /**
     * Get a bulk insert query string.
     *
     * @return The buld insert query string.
     */
    public static String getBulkInsertString() {
        final StringBuilder sqlRequest = new StringBuilder(Constants.INSERT_INTO);
        sqlRequest.append(TABLE_NAME);
        sqlRequest.append(Constants.LEFT_PARENTHESIS);
        sqlRequest.append(_ID);
        sqlRequest.append(Constants.COMMA);
        sqlRequest.append(TYPE);
        sqlRequest.append(Constants.COMMA);
        sqlRequest.append(SUBTYPE);
        sqlRequest.append(Constants.COMMA);
        sqlRequest.append(TITLE);
        sqlRequest.append(Constants.COMMA);
        sqlRequest.append(SUBTITLE);
        sqlRequest.append(Constants.COMMA);
        sqlRequest.append(DESCRIPTION);
        sqlRequest.append(Constants.COMMA);
        sqlRequest.append(VIDEO_PARTS);
        sqlRequest.append(Constants.COMMA);
        sqlRequest.append(FAVORITE);
        sqlRequest.append(Constants.RIGHT_PARENTHESIS);
        sqlRequest.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        return sqlRequest.toString();
    }

    /**
     * Bind values for bulk insert.
     *
     * @param stmt The statement.
     * @param values The values.
     */
    public static void bindValuesInBulkInsert(final SQLiteStatement stmt, final ContentValues values) {
        int i = 1;
        String value;

        stmt.bindLong(i++, values.getAsLong(_ID));

        value = values.getAsString(TYPE);
        stmt.bindString(i++, value != null ? value : Constants.STRING_EMPTY);

        value = values.getAsString(SUBTYPE);
        stmt.bindString(i++, value != null ? value : Constants.STRING_EMPTY);

        value = values.getAsString(TITLE);
        stmt.bindString(i++, value != null ? value : Constants.STRING_EMPTY);

        value = values.getAsString(SUBTITLE);
        stmt.bindString(i++, value != null ? value : Constants.STRING_EMPTY);

        value = values.getAsString(DESCRIPTION);
        stmt.bindString(i++, value != null ? value : Constants.STRING_EMPTY);

        stmt.bindString(i++, values.getAsString(VIDEO_PARTS));

        stmt.bindLong(i++, values.getAsInteger(FAVORITE));
    }

    /**
     * Get the value from a model.
     *
     * @param video The video model.
     * @return The content values.
     */
    public static ContentValues getContentValues(final JSONObject video) throws JSONException {
        ContentValues values = new ContentValues();
        values.put(_ID, video.getLong("timestamp"));
        values.put(TYPE, video.getString("type"));
        values.put(SUBTYPE, video.getString("subType"));
        values.put(TITLE, video.getString("title"));
        values.put(SUBTITLE, video.getString("subTitle"));
        values.put(DESCRIPTION, video.getString("description"));
        values.put(VIDEO_PARTS, video.getJSONArray("videoFiles").toString());
        values.put(FAVORITE, 0);
        return values;
    }

    /**
     * Build the URI with the video id.
     *
     * @param videoParcel The video parcel.
     * @return The URI.
     */
    public static Uri buildVideoURI(VideoParcel videoParcel) {
        return CONTENT_URI.buildUpon().appendPath(videoParcel.getTimestamp().toString()).build();
    }

    /**
     * Is the given URI a search URI ?
     *
     * @param videosUri The video uri.
     * @return True or false.
     */
    public static boolean isSearchUri(Uri videosUri) {
        List<String> pathSegments = videosUri.getPathSegments();
        return pathSegments.size() >= 2 && PATH_SEARCH.equals(pathSegments.get(1));
    }
}
