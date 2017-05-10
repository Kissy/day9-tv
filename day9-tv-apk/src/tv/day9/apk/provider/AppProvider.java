/*
 * 2011 Foxykeep (http://datadroid.foxykeep.com)
 *
 * Licensed under the Beerware License :
 * 
 *   As long as you retain this notice you can do whatever you want with this stuff. If we meet some day, and you think
 *   this stuff is worth it, you can buy me a beer in return
 */
package tv.day9.apk.provider;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.provider.BaseColumns;
import tv.day9.apk.config.Constants;
import tv.day9.apk.provider.impl.DownloadDAO;
import tv.day9.apk.provider.impl.VideoDAO;

@SuppressWarnings({"PointlessArithmeticExpression"})
public class AppProvider extends ContentProvider {

    private static final String LOG_TAG = AppProvider.class.getSimpleName();

    protected static final String DATABASE_NAME = "day9tvprovider.db";

    // Any changes to the database format *must* include update-in-place code.
    // Original version: 1
    public static final int DATABASE_VERSION = 3;

    public static final String AUTHORITY = "tv.day9.apk.provider.appprovider";

    public static final Uri INTEGRITY_CHECK_URI = Uri.parse(Constants.CONTENT + AUTHORITY + "/integrityCheck");

    public static final int VIDEO_BASE = 0;
    private static final int VIDEO = VIDEO_BASE;
    private static final int VIDEO_ID = VIDEO_BASE + 1;

    public static final int DOWNLOAD_BASE = 0x1000;
    private static final int DOWNLOAD = DOWNLOAD_BASE;
    private static final int DOWNLOAD_ID = DOWNLOAD_BASE + 1;

    // Exemple for second table
    // private static final int PERIOD_BASE = 0x2;
    // private static final int PERIOD = PERIOD_BASE;
    // private static final int PERIOD_ID = PERIOD_BASE + 1;

    private static final int BASE_SHIFT = 12; // DO NOT TOUCH ! 12 bits to the
                                              // base type: 0,
                                              // 0x1000, 0x2000, etc.

    private static final String[] TABLE_NAMES = {
        VideoDAO.TABLE_NAME,
        DownloadDAO.TABLE_NAME
    };

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        final UriMatcher matcher = sURIMatcher;

        // All videos
        matcher.addURI(AUTHORITY, VideoDAO.TABLE_NAME, VIDEO);
        // A specific video
        matcher.addURI(AUTHORITY, VideoDAO.TABLE_NAME + Constants.SLASH_POUND, VIDEO_ID);

        // All downloads
        matcher.addURI(AUTHORITY, DownloadDAO.TABLE_NAME, DOWNLOAD);
        // A specific video
        matcher.addURI(AUTHORITY, DownloadDAO.TABLE_NAME + Constants.SLASH_POUND, DOWNLOAD_ID);
    }

    private SQLiteDatabase mDatabase;

    public synchronized SQLiteDatabase getDatabase(final Context context) {
        // Always return the cached database, if we've got one
        if (mDatabase != null) {
            return mDatabase;
        }

        final DatabaseHelper helper = new DatabaseHelper(context, DATABASE_NAME);
        mDatabase = helper.getWritableDatabase();
        return mDatabase;
    }

    @Override
    public int delete(final Uri uri, final String selection, final String[] selectionArgs) {

        final int match = sURIMatcher.match(uri);
        final Context context = getContext();

        // Pick the correct database for this operation
        final SQLiteDatabase db = getDatabase(context);
        final int table = match >> BASE_SHIFT;
        String id;

        int result;

        switch (match) {
            case VIDEO_ID:
            case DOWNLOAD_ID:
                id = uri.getPathSegments().get(1);
                result = db.delete(TABLE_NAMES[table], whereWithId(id, selection), selectionArgs);
                break;
            case VIDEO:
            case DOWNLOAD:
                result = db.delete(TABLE_NAMES[table], selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public String getType(final Uri uri) {
        final int match = sURIMatcher.match(uri);
        switch (match) {
            case VIDEO_ID:
                return VideoDAO.TYPE_ELEM_TYPE;
            case VIDEO:
                return VideoDAO.TYPE_DIR_TYPE;
            case DOWNLOAD_ID:
                return DownloadDAO.TYPE_ELEM_TYPE;
            case DOWNLOAD:
                return DownloadDAO.TYPE_DIR_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(final Uri uri, final ContentValues values) {

        final int match = sURIMatcher.match(uri);
        final Context context = getContext();

        // Pick the correct database for this operation
        final SQLiteDatabase db = getDatabase(context);
        final int table = match >> BASE_SHIFT;
        long id;

        Uri resultUri;

        switch (match) {
            case VIDEO:
            case DOWNLOAD:
                id = db.insert(TABLE_NAMES[table], "foo", values);
                resultUri = ContentUris.withAppendedId(uri, id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Notify with the base uri, not the new uri (nobody is watching a new
        // record)
        getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }

    @Override
    public int bulkInsert(final Uri uri, final ContentValues[] values) {

        final int match = sURIMatcher.match(uri);
        final Context context = getContext();

        // Pick the correct database for this operation
        final SQLiteDatabase db = getDatabase(context);

        int numberInserted = 0;
        SQLiteStatement insertStmt;

        db.beginTransaction();
        try {
            switch (match) {
                case VIDEO:
                case DOWNLOAD:
                    insertStmt = db.compileStatement(VideoDAO.getBulkInsertString());
                    for (final ContentValues value : values) {
                        VideoDAO.bindValuesInBulkInsert(insertStmt, value);
                        insertStmt.execute();
                        insertStmt.clearBindings();
                    }
                    insertStmt.close();
                    db.setTransactionSuccessful();
                    numberInserted = values.length;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } finally {
            db.endTransaction();
        }

        // Notify with the base uri, not the new uri (nobody is watching a new
        // record)
        context.getContentResolver().notifyChange(uri, null);
        return numberInserted;
    }

    @Override
    public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs,
            final String sortOrder) {

        Cursor c;
        final Uri notificationUri = AppContent.CONTENT_URI;
        final int match = sURIMatcher.match(uri);
        final Context context = getContext();
        // Pick the correct database for this operation
        final SQLiteDatabase db = getDatabase(context);
        final int table = match >> BASE_SHIFT;
        String id;

        switch (match) {
            case VIDEO_ID:
            case DOWNLOAD_ID:
                id = uri.getPathSegments().get(1);
                c = db.query(TABLE_NAMES[table], projection, whereWithId(id, selection), selectionArgs, null, null,
                        sortOrder);
                break;
            case VIDEO:
            case DOWNLOAD:
                c = db.query(TABLE_NAMES[table], projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if ((c != null) && !isTemporary()) {
            c.setNotificationUri(getContext().getContentResolver(), notificationUri);
        }
        return c;
    }

    private String whereWithId(final String id, final String selection) {
        final StringBuilder sb = new StringBuilder(256);
        sb.append(BaseColumns._ID);
        sb.append(" = ");
        sb.append(id);
        if (selection != null) {
            sb.append(" AND (");
            sb.append(selection);
            sb.append(')');
        }
        return sb.toString();
    }

    @Override
    public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {

        final int match = sURIMatcher.match(uri);
        final Context context = getContext();
        // Pick the correct database for this operation
        final SQLiteDatabase db = getDatabase(context);
        final int table = match >> BASE_SHIFT;
        int result;

        switch (match) {
            case VIDEO_ID:
            case DOWNLOAD_ID:
                final String id = uri.getPathSegments().get(1);
                result = db.update(TABLE_NAMES[table], values, whereWithId(id, selection), selectionArgs);
                break;
            case VIDEO:
            case DOWNLOAD:
                result = db.update(TABLE_NAMES[table], values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public boolean onCreate() {
        return true;
    }
}
