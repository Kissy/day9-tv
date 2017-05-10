package tv.day9.apk.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import tv.day9.apk.R;
import tv.day9.apk.activity.BaseActivity;
import tv.day9.apk.adapter.VideoCursorAdapter;
import tv.day9.apk.adapter.VideoEndlessAdapter;
import tv.day9.apk.config.Constants;
import tv.day9.apk.handler.NotifyingAsyncQueryHandler;
import tv.day9.apk.model.VideoParcel;
import tv.day9.apk.provider.impl.VideoDAO;
import tv.day9.apk.util.IntentUtils;
import tv.day9.apk.util.UIUtils;

/**
 * A {@link android.support.v4.app.ListFragment} showing a list of sessions.
 */
public class FavoritesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String STATE_SAVED_POSITION = "savedPosition";

    private CursorAdapter cursorAdapter;
    private int oldPosition = -1;

    /**
     * @inheritDoc
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cursorAdapter = new VideoCursorAdapter(getActivity(), null);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getString(R.string.empty_videos));

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setBackgroundColor(getResources().getColor(R.color.black));
        getListView().setCacheColorHint(getResources().getColor(R.color.black));

        /*if (savedInstanceState != null) {
            oldPosition = savedInstanceState.getInt(STATE_SAVED_POSITION, -1);
        }*/

        setListAdapter(cursorAdapter);

        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SAVED_POSITION, oldPosition);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        oldPosition = position;

        // Launch viewer for specific video, passing along any track knowledge
        // that should influence the title-bar.
        final Cursor cursor = (Cursor) cursorAdapter.getItem(position);
        final Intent intent = IntentUtils.getVideoDetailActivityIntent();
        VideoParcel videoParcel = new VideoParcel(cursor);
        intent.putExtra(VideoDetailFragment.INTENT_EXTRA_VIDEO, videoParcel);
        intent.putExtra(Intent.EXTRA_TITLE, videoParcel.getTitle());
        ((BaseActivity) getActivity()).openActivityOrFragment(intent);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), VideoDAO.CONTENT_URI, VideoDAO.CONTENT_PROJECTION,
                VideoDAO.FAVORITE + Constants.SELECTION_EQUAL, new String[] {Constants.NUMERIC_TRUE},
                VideoDAO._ID + Constants.DESC);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        cursorAdapter.swapCursor(cursor);

        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        cursorAdapter.swapCursor(null);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_starred) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
