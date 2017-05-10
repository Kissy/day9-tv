package tv.day9.apk.task;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.provider.BaseColumns;

import java.lang.ref.WeakReference;

import tv.day9.apk.fragment.VideoDetailFragment;
import tv.day9.apk.provider.DatabaseHelper;
import tv.day9.apk.provider.impl.VideoDAO;

/**
 * @author Guillaume Le Biller
 */
public class UpdateFavoriteTask extends AsyncTask<Boolean, Void, Boolean> {
    private WeakReference<VideoDetailFragment> videoDetailFragment;
    private WeakReference<DatabaseHelper> databaseHelper;
    private Long videoId;

    /**
     * Default constructor.
     *
     * @param videoDetailFragment The VideoDetailFragment that started the task.
     */
    public UpdateFavoriteTask(VideoDetailFragment videoDetailFragment, Long videoId) {
        this.videoDetailFragment = new WeakReference<VideoDetailFragment>(videoDetailFragment);
        this.databaseHelper = new WeakReference<DatabaseHelper>(DatabaseHelper.getInstance(videoDetailFragment.getActivity()));
        this.videoId = videoId;
    }

    /**
     * @inheritDoc
     */
    @Override
    protected Boolean doInBackground(Boolean... params) {
        Boolean newStatus = params[0];
        if (databaseHelper.get() != null) {
            ContentValues updateFavorite = new ContentValues();
            updateFavorite.put(VideoDAO.FAVORITE, newStatus);
            databaseHelper.get().getWritableDatabase().update(VideoDAO.TABLE_NAME, updateFavorite,
                    BaseColumns._ID + " = ?" , new String[] {this.videoId.toString()});
        }
        return newStatus;
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void onPostExecute(Boolean response) {
        super.onPostExecute(response);
        if (this.videoDetailFragment.get() != null) {
            this.videoDetailFragment.get().updateFavoriteTaskFinished(response);
        }
    }
}
