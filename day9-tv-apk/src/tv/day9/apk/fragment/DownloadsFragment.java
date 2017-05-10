package tv.day9.apk.fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import tv.day9.apk.R;
import tv.day9.apk.activity.BaseActivity;
import tv.day9.apk.adapter.DownloadCursorAdapter;
import tv.day9.apk.config.Constants;
import tv.day9.apk.config.DownloadConstants;
import tv.day9.apk.handler.NotifyingAsyncQueryHandler;
import tv.day9.apk.provider.AppProvider;
import tv.day9.apk.provider.impl.DownloadDAO;
import tv.day9.apk.util.UIUtils;
import tv.day9.apk.worker.DownloadWorker;

import java.io.File;

/**
 * A {@link android.support.v4.app.ListFragment} showing a list of sessions.
 */
public class DownloadsFragment extends ListFragment implements NotifyingAsyncQueryHandler.AsyncQueryListener {

    private static final String TAG = DownloadsFragment.class.getSimpleName();
    private static final String STATE_SAVED_POSITION = "savedPosition";
    private static final String TRACK_ACTION_WATCH = "Watch";
    private static final String TRACK_ACTION_DELETE = "Delete";

    private Cursor cursor;
    private DownloadCursorAdapter listAdapter;
    private int oldPosition = -1;
    private boolean hasSetEmptyText = false;

    private NotifyingAsyncQueryHandler queryHandler;
    private SharedPreferences sharedPreferences;

    /**
     * @inheritDoc
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DownloadWorker.checkForRunningStatus(getActivity());
        setHasOptionsMenu(true);
    }

    /**
     * Reload the cursor and adapter.
     */
    public void reloadData() {
        // Teardown from previous arguments
        if (cursor != null) {
            getActivity().stopManagingCursor(cursor);
            cursor = null;
        }

        oldPosition = -1;
        setListAdapter(null);

        queryHandler.cancelOperation(AppProvider.DOWNLOAD_BASE);
        listAdapter = new DownloadCursorAdapter(getActivity(), null);
        setListAdapter(listAdapter);

        queryHandler.startQuery(AppProvider.DOWNLOAD_BASE, null, DownloadDAO.CONTENT_URI, DownloadDAO.CONTENT_PROJECTION,
                null, null, DownloadDAO.LAST_MODIFICATION + Constants.DESC);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        queryHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        reloadData();

        registerForContextMenu(getListView());
        getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
        getListView().setBackgroundColor(getResources().getColor(R.color.black));
        getListView().setCacheColorHint(getResources().getColor(R.color.black));

        if (savedInstanceState != null) {
            oldPosition = savedInstanceState.getInt(STATE_SAVED_POSITION, -1);
        }

        if (!hasSetEmptyText) {
            // Could be a bug, but calling this twice makes it become visible when it shouldn't
            // be visible.
            setEmptyText(getString(R.string.empty_downloads));
            hasSetEmptyText = true;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onResume() {
        super.onResume();

        if (sharedPreferences.getBoolean(Constants.NO_DOWNLOAD, false)) {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.no_downloads), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.downloads, menu);
        refreshActionBar();
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_pause) {
            return pauseDownload();
        } else if (i == R.id.menu_resume) {
            return resumeDownload();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (getActivity() == null) {
            return;
        }

        if (token == AppProvider.DOWNLOAD_BASE) {
            if (this.cursor != null) {
                // In case cancelOperation() doesn't work and we end up with consecutive calls to this
                // callback.
                getActivity().stopManagingCursor(this.cursor);
                this.cursor = null;
            }

            this.cursor = cursor;
            getActivity().startManagingCursor(this.cursor);
            listAdapter.changeCursor(this.cursor);
            if (oldPosition >= 0 && getView() != null) {
                this.cursor.moveToPosition(oldPosition);
            }
            refreshActionBar();
        } else {
            if (this.cursor != null) {
                this.cursor.close();
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onInsertComplete(int token, Object cookie, Uri uri) {
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onUpdateComplete(int token, Object cookie, int result) {
        refreshActionBar();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onDeleteComplete(int token, Object cookie, int result) {
        refreshActionBar();
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        menu.setHeaderTitle(cursor.getString(DownloadDAO.CONTENT_TITLE_COLUMN));
        
        int status = cursor.getInt(DownloadDAO.CONTENT_STATUS_COLUMN);
        switch (status) {
            case DownloadConstants.STATUS_PENDING:
                menu.add(Menu.NONE, R.id.dcm_resume, Menu.NONE, R.string.download_context_resume);
                menu.add(Menu.NONE, R.id.dcm_cancel, Menu.NONE, R.string.download_context_cancel);
                break;
            case DownloadConstants.STATUS_CANCELED:
                menu.add(Menu.NONE, R.id.dcm_resume, Menu.NONE, R.string.download_context_resume);
                break;
            case DownloadConstants.STATUS_RUNNING:
                menu.add(Menu.NONE, R.id.dcm_pause, Menu.NONE, R.string.download_context_pause);
                menu.add(Menu.NONE, R.id.dcm_cancel, Menu.NONE, R.string.download_context_cancel);
                break;
            case DownloadConstants.STATUS_SUCCESS:
                menu.add(Menu.NONE, R.id.dcm_view, Menu.NONE, R.string.download_context_view);
                break;
        }
        menu.add(Menu.NONE, R.id.dcm_delete, Menu.NONE, R.string.download_context_delete);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);

        int status = cursor.getInt(DownloadDAO.CONTENT_STATUS_COLUMN);
        int i = item.getItemId();
        if (i == R.id.dcm_view) {
            Intent watchVideoItent = new Intent(Intent.ACTION_VIEW);
            File videoFile = new File(cursor.getString(DownloadDAO.CONTENT_DESTINATION_COLUMN));
            if (!videoFile.exists() || !videoFile.canRead()) {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.file_not_found), Toast.LENGTH_LONG).show();
                return false;
            }

            watchVideoItent.setDataAndType(Uri.fromFile(videoFile), Constants.DATATYPE_VIDEO);
            getActivity().startActivity(watchVideoItent);
            return true;
        } else if (i == R.id.dcm_delete) {
            if (status == DownloadConstants.STATUS_RUNNING) {
                DownloadWorker.cancelCurrentDownload();
            }
            File videoFile = new File(cursor.getString(DownloadDAO.CONTENT_DESTINATION_COLUMN));
            return deleteDownload(cursor.getString(DownloadDAO.CONTENT_ID_COLUMN), cursor.getString(DownloadDAO.CONTENT_DESTINATION_COLUMN))
                    || (videoFile.exists() && videoFile.delete());
        } else if (i == R.id.dcm_pause) {
            return pauseDownload();
        } else if (i == R.id.dcm_resume) {
            return updateDownloadStatus(cursor.getString(DownloadDAO.CONTENT_ID_COLUMN), DownloadConstants.STATUS_PENDING)
                    && resumeDownload();
        } else if (i == R.id.dcm_cancel) {
            if (status == DownloadConstants.STATUS_RUNNING) {
                DownloadWorker.cancelCurrentDownload();
            }
            return updateDownloadStatus(cursor.getString(DownloadDAO.CONTENT_ID_COLUMN), DownloadConstants.STATUS_CANCELED);
        } else {
            return super.onContextItemSelected(item);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * Update the status of the download.
     *
     * @param downloadId The download id.
     * @param status The new status.
     * @return The status of the update.
     */
    private boolean updateDownloadStatus(String downloadId, int status) {
        final ContentValues updateContentValues = new ContentValues();
        updateContentValues.put(DownloadDAO.STATUS, status);
        queryHandler.startUpdate(DownloadDAO.CONTENT_URI.buildUpon().appendPath(downloadId).build(), updateContentValues);
        return true;
    }

    /**
     * Pause a download.
     *
     * @return The status.
     */
    private boolean pauseDownload() {
        DownloadWorker.updatePauseStatus(getActivity(), true);
        return refreshActionBar();
    }

    /**
     * Resume download.
     *
     * @return the status.
     */
    private boolean resumeDownload() {
        DownloadWorker.updatePauseStatus(getActivity(), false);
        return refreshActionBar();
    }

    /**
     * Refresh the action bar or action bar compat.
     *
     * @return True.
     */
    private boolean refreshActionBar() {
        ((BaseActivity) getActivity()).setDownloadActionItemState(cursor != null && cursor.getCount() > 0);
        return true;
    }

    /**
     * Delete a download and the associated file.
     *
     * @param downloadId The download id.
     * @param downloadDestination The download file.
     * @return The status.
     */
    private boolean deleteDownload(String downloadId, String downloadDestination) {
        DownloadWorker.updatePauseStatus(getActivity(), true);
        queryHandler.startDelete(DownloadDAO.CONTENT_URI.buildUpon().appendPath(downloadId).build());
        File file = new File(downloadDestination);
        return !file.exists() || file.delete();
    }
}
