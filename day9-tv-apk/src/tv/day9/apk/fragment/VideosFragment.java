package tv.day9.apk.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.Toast;

import java.util.Calendar;

import tv.day9.apk.R;
import tv.day9.apk.activity.BaseActivity;
import tv.day9.apk.adapter.VideoEndlessAdapter;
import tv.day9.apk.config.Constants;
import tv.day9.apk.model.VideoParcel;
import tv.day9.apk.provider.impl.VideoDAO;
import tv.day9.apk.util.IntentUtils;
import tv.day9.apk.util.UIUtils;

/**
 * A {@link ListFragment} showing a list of sessions.
 */
public class VideosFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String STATE_SAVED_POSITION = "savedPosition";

    public static final String SEARCH_QUERY = "searchQuery";
    public static final String DISPLAY_STATE = "displayState";
    public static final int SEARCH = 2;

    private SharedPreferences sharedPreferences;
    private CursorAdapter cursorAdapter;

    private String searchQuery = null;
    private MenuItem refreshMenuItem;
    private ProgressBar refreshMenuItemProgressBar;
    private int oldPosition = -1;


    /**
     * @inheritDoc
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFS, 0);
        cursorAdapter = new VideoEndlessAdapter(getActivity());
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onResume() {
        super.onResume();
        ((VideoEndlessAdapter) cursorAdapter).getKeepOnAppending().getAndSet(true);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onPause() {
        super.onPause();
        ((VideoEndlessAdapter) cursorAdapter).getKeepOnAppending().getAndSet(false);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getString(R.string.empty_videos));
        setHasOptionsMenu(true);

        if (UIUtils.isHoneycomb()) {
            refreshMenuItemProgressBar = UIUtils.createProgressBar(getActivity(), android.R.attr.progressBarStyle);
        }

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.videos, menu);

        super.onCreateOptionsMenu(menu, inflater);
        refreshMenuItem = menu.findItem(R.id.actionbar_compat_menu_refresh);
    }

    /**
     * Update the refresh button.
     *
     * @param refresh The refresh status.
     */
    @SuppressWarnings("RedundantCast")
    private void updateRefreshButton(boolean refresh) {
        if (UIUtils.isHoneycomb()) {
            if (refreshMenuItem != null) {
                refreshMenuItem.setActionView(refresh ? refreshMenuItemProgressBar : null);
            }
        } else {
            ((BaseActivity) getActivity()).setRefreshActionItemState(refresh);
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.actionbar_compat_menu_refresh) {
            fetchMoreData(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri baseUri;
        if (searchQuery != null) {
            baseUri = Uri.withAppendedPath(VideoDAO.CONTENT_URI, Uri.encode(searchQuery));
        } else {
            baseUri = VideoDAO.CONTENT_URI;
        }

        return new CursorLoader(getActivity(), baseUri, VideoDAO.CONTENT_PROJECTION, null, null,
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
     * Fetch more data from the server.
     *
     * @param notify Notify the user ?
     */
    private void fetchMoreData(boolean notify) {
        // Periodic check is only for non Froyo or for people that skipped automatic update.
        // non-Froyo users always have DEVICE_REGISTRATION_ID to null.
        String deviceRegistrationId = sharedPreferences.getString(Constants.DEVICE_REGISTRATION_ID, null);
        if (deviceRegistrationId == null || deviceRegistrationId.length() == 0) {
            // Only check every 6 hours;
            Calendar currentTime = Calendar.getInstance(Constants.GMT_TIMEZONE);
            Calendar nextCheckTime = Calendar.getInstance(Constants.GMT_TIMEZONE);
            nextCheckTime.setTimeInMillis(sharedPreferences.getLong(Constants.LAST_VIDEO_CHECK, 0L));
            nextCheckTime.add(Calendar.HOUR, Constants.VIDEO_CHECK_TIMEOUT);
            if (nextCheckTime.after(currentTime)) {
                if (notify) {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.no_new_videos,  Toast.LENGTH_SHORT).show();
                }
                //return;
            }
        } else {
            // If the C2DM is enabled, but background update is disable then we
            // need to check that NEED_VIDEO_UPDATE is true.
            if (!sharedPreferences.getBoolean(Constants.NEED_VIDEOS_UPDATE, false)) {
                if (notify) {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.no_new_videos,  Toast.LENGTH_SHORT).show();
                }
                return;
            } else {
                sharedPreferences.edit().remove(Constants.NEED_VIDEOS_UPDATE).commit();
            }
        }

        // If where are here that mean we are starting an update
        updateRefreshButton(true);

        long id = sharedPreferences.getLong(Constants.LATEST_VIDEO_ID, 0L);
        // Fall back if issue fetch it with cursor.
        /*if (id == 0) {
            cursor.moveToFirst();
            id = cursor.getInt(VideoDAO.CONTENT_ID_COLUMN);
        }*/

        //AppRequestManager.from(getActivity()).addOnRequestFinishedListener(this);
        //requestId = AppRequestManager.from(getActivity()).getNewVideoList(id, false);
    }
}
