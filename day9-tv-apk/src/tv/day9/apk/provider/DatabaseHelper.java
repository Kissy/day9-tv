package tv.day9.apk.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;

import tv.day9.apk.provider.impl.DownloadDAO;
import tv.day9.apk.provider.impl.VideoDAO;

/**
* Created by Guillaume on 16/09/13.
*/
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "day9tvprovider.db";

    private static DatabaseHelper instance;

    public DatabaseHelper(final Context context, final String name) {
        super(context, name, null, AppProvider.DATABASE_VERSION);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onCreate(final SQLiteDatabase db) {
        VideoDAO.createTable(db);
        DownloadDAO.createTable(db);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        VideoDAO.upgradeTable(db, oldVersion, newVersion);
        DownloadDAO.upgradeTable(db, oldVersion, newVersion);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onOpen(final SQLiteDatabase db) {
    }

    /**
     * Get the singleton.
     *
     * @return The singleton.
     */
    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context, DATABASE_NAME);
        }
        return instance;
    }
}
