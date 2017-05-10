package tv.day9.apk.provider.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.provider.BaseColumns;

import com.foxykeep.datadroid.provider.util.DatabaseUtil;

import java.io.File;
import java.util.Date;

import tv.day9.apk.R;
import tv.day9.apk.config.Constants;
import tv.day9.apk.model.VideoFileParcel;
import tv.day9.apk.provider.AppContent;
import tv.day9.apk.provider.AppProvider;
import tv.day9.apk.provider.columns.DownloadColumns;

/**
* Created by Guillaume on 25/08/13.
*/
public final class DownloadDAO extends AppContent implements DownloadColumns, BaseColumns {
    public static final String TABLE_NAME = "download";
    public static final Uri CONTENT_URI = Uri.parse(AppContent.CONTENT_URI + "/" + TABLE_NAME);

    public static final String TYPE_ELEM_TYPE = "vnd.android.cursor.item/" + AppProvider.AUTHORITY + ".Download";
    public static final String TYPE_DIR_TYPE = "vnd.android.cursor.dir/" + AppProvider.AUTHORITY + ".Download";

    public static final int CONTENT_ID_COLUMN = 0;
    public static final int CONTENT_URI_COLUMN = 1;
    public static final int CONTENT_TITLE_COLUMN = 2;
    public static final int CONTENT_DESCRIPTION_COLUMN = 3;
    public static final int CONTENT_TYPE_COLUMN = 4;
    public static final int CONTENT_DIMENSION_COLUMN = 5;
    public static final int CONTENT_DURATION_COLUMN = 6;
    public static final int CONTENT_TOTAL_BYTES_COLUMN = 7;
    public static final int CONTENT_CURRENT_BYTES_COLUMN = 8;
    public static final int CONTENT_DESTINATION_COLUMN = 9;
    public static final int CONTENT_STATUS_COLUMN = 10;
    public static final int CONTENT_LAST_MODIFICATION_COLUMN = 11;

    public static final String[] CONTENT_PROJECTION = new String[] {
        _ID, URI, TITLE, DESCRIPTION, TYPE, DIMENSION, DURATION, TOTAL_BYTES, CURRENT_BYTES, DESTINATION, STATUS, LAST_MODIFICATION
    };
    public static final String[] CONTENT_LITE_PROJECTION = new String[] {
            _ID, DESTINATION
    };

    /**
     * Create the table.
     *
     * @param db The database.
     */
    public static void createTable(final SQLiteDatabase db) {
        final String s = Constants.LEFT_PARENTHESIS + _ID + Constants.INTEGER_PRIMARY_KEY_AUTOINCREMENT + URI + Constants.TEXT_COMMA
            + TITLE + Constants.TEXT_COMMA + DESCRIPTION + Constants.TEXT_COMMA + TOTAL_BYTES + Constants.INTEGER_COMMA
            + TYPE + Constants.TEXT_COMMA + DIMENSION + Constants.TEXT_COMMA + DURATION + Constants.INTEGER_COMMA
            + CURRENT_BYTES + Constants.INTEGER_COMMA + DESTINATION + Constants.TEXT_COMMA + STATUS + Constants.INTEGER_COMMA
            + LAST_MODIFICATION + Constants.BIGINT_SQLEND;

        db.execSQL(Constants.CREATE_TABLE + TABLE_NAME + s);

        // Indexes
        db.execSQL(DatabaseUtil.getCreateIndexString(TABLE_NAME, LAST_MODIFICATION));
        db.execSQL(DatabaseUtil.getCreateIndexString(TABLE_NAME, STATUS));
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
        sqlRequest.append(URI);
        sqlRequest.append(Constants.COMMA);
        sqlRequest.append(TITLE);
        sqlRequest.append(Constants.COMMA);
        sqlRequest.append(DESCRIPTION);
        sqlRequest.append(Constants.COMMA);
        sqlRequest.append(TYPE);
        sqlRequest.append(Constants.COMMA);
        sqlRequest.append(DIMENSION);
        sqlRequest.append(Constants.COMMA);
        sqlRequest.append(DURATION);
        sqlRequest.append(Constants.COMMA);
        sqlRequest.append(TOTAL_BYTES);
        sqlRequest.append(Constants.COMMA);
        sqlRequest.append(CURRENT_BYTES);
        sqlRequest.append(Constants.COMMA);
        sqlRequest.append(DESTINATION);
        sqlRequest.append(Constants.COMMA);
        sqlRequest.append(STATUS);
        sqlRequest.append(Constants.COMMA);
        sqlRequest.append(LAST_MODIFICATION);
        sqlRequest.append(Constants.RIGHT_PARENTHESIS);
        sqlRequest.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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

        value = values.getAsString(URI);
        stmt.bindString(i++, value != null ? value : Constants.STRING_EMPTY);

        value = values.getAsString(TITLE);
        stmt.bindString(i++, value != null ? value : Constants.STRING_EMPTY);

        value = values.getAsString(DESCRIPTION);
        stmt.bindString(i++, value != null ? value : Constants.STRING_EMPTY);

        value = values.getAsString(TYPE);
        stmt.bindString(i++, value != null ? value : Constants.STRING_EMPTY);

        value = values.getAsString(DIMENSION);
        stmt.bindString(i++, value != null ? value : Constants.STRING_EMPTY);

        stmt.bindLong(i++, values.getAsLong(DURATION));

        stmt.bindLong(i++, values.getAsLong(TOTAL_BYTES));

        stmt.bindLong(i++, values.getAsLong(CURRENT_BYTES));

        value = values.getAsString(DESTINATION);
        stmt.bindString(i++, value != null ? value : Constants.STRING_EMPTY);

        stmt.bindLong(i++, values.getAsInteger(STATUS));

        stmt.bindLong(i++, values.getAsLong(LAST_MODIFICATION));
    }

    /**
     * Get the value from a model without id.
     *
     *
     * @param context The context.
     * @param videoFileParcel The video file parcel.
     * @param destinationFile The base destination file.
     * @param downloadStatus The download status.
     * @return The content values.
     */
    public static ContentValues getContentValuesForNewDownload(Context context, final VideoFileParcel videoFileParcel, File destinationFile, int downloadStatus) {
        ContentValues values = new ContentValues();
        values.put(URI, Constants.BLIP_FILE_BASE_URL + videoFileParcel.getFile());
        values.put(TITLE, videoFileParcel.getTitle() + Constants.SPACE + context.getString(R.string.videopart, videoFileParcel.getVideoPart()));
        values.put(DESCRIPTION, videoFileParcel.getDescription());
        values.put(TYPE, videoFileParcel.getType());
        values.put(DIMENSION, videoFileParcel.getWidth() + Constants.X + videoFileParcel.getHeight());
        values.put(DURATION, videoFileParcel.getDuration());
        values.put(TOTAL_BYTES, videoFileParcel.getSize());
        values.put(CURRENT_BYTES, destinationFile.length());
        values.put(DESTINATION, destinationFile.getAbsolutePath());
        values.put(STATUS, downloadStatus);
        values.put(LAST_MODIFICATION, new Date().getTime());
        return values;
    }
}
