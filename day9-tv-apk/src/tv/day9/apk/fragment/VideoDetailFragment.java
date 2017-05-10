package tv.day9.apk.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;

import tv.day9.apk.R;
import tv.day9.apk.activity.BaseActivity;
import tv.day9.apk.adapter.VideoPartAdapter;
import tv.day9.apk.config.Constants;
import tv.day9.apk.model.VideoParcel;
import tv.day9.apk.task.UpdateFavoriteTask;

/**
 * A fragment that shows detail information for a session, including session title, abstract,
 * time information, speaker photos and bios, etc.
 */
public class VideoDetailFragment extends Fragment {
    private static final String TAG = VideoDetailFragment.class.getSimpleName();
    public static final String INTENT_EXTRA_VIDEO = "tv.day9.model.extras.video";

    private WeakReference<UpdateFavoriteTask> updateFavoriteTask;
    private MenuItem starredMenuItem;
    private VideoParcel videoParcel;
    private JSONArray videoParts;
    private String videoQuality;

    /**
     * @inheritDoc
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
        videoParcel = intent.getParcelableExtra(INTENT_EXTRA_VIDEO);
        if (videoParcel == null) {
            return;
        }

        videoParts = getVideoPartsAsList(videoParcel.getVideoParts());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        videoQuality = sharedPreferences.getString(Constants.VIDEO_QUALITY, Constants.DEFAULT_VIDEO_QUALITY);

        setHasOptionsMenu(true);
    }

    /**
     * @inheritDoc
     */
    @Override
    @SuppressWarnings("ConstantConditions")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_video_detail, container, false);
        ((TextView) rootView.findViewById(R.id.v_subtitle)).setText(videoParcel.getSubtitle());
        ((TextView) rootView.findViewById(R.id.v_description)).setText(videoParcel.getDescription());

        // Video parts list
        LinearLayout videoFilesRoot = (LinearLayout) rootView.findViewById(R.id.videoFiles);
        for (int i = 0; i < videoParts.length(); ++i) {
            try {
                new VideoPartAdapter(getActivity(), inflater, videoFilesRoot, videoParts, i, videoQuality);
            } catch (JSONException ignored) {}
        }
        return rootView;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.video_detail, menu);
        starredMenuItem = menu.findItem(R.id.menu_starred);
        if (videoParcel.getFavorite() == 1) {
            starredMenuItem.setIcon(R.drawable.icon_starred);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_share:
                final String shareString = getString(R.string.share_template, videoParcel.getTitle(), videoParcel.getSubtitle(), videoParcel.getDescription());
                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType(Constants.DATATYPE_TEXT);
                intent.putExtra(Intent.EXTRA_TEXT, shareString);
                startActivity(Intent.createChooser(intent, getText(R.string.title_share)));
                return true;
            case R.id.menu_starred:
                if (!isUpdateFavoriteTaskRunning()) {
                    startUpdateFavoriteTask();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Get the video parts as list;
     *
     * @param videoParts The video parts as byte array.
     * @return The list of video parts.
     */
    private JSONArray getVideoPartsAsList(String videoParts) {
        try {
            return new JSONArray(videoParts);
        } catch (JSONException ignored) {}
        return new JSONArray();
    }

    /**
     * Check if the update favorite task is running.
     * @return
     */
    private boolean isUpdateFavoriteTaskRunning() {
        return this.updateFavoriteTask != null && this.updateFavoriteTask.get() != null &&
                !AsyncTask.Status.FINISHED.equals(this.updateFavoriteTask.get().getStatus());
    }

    /**
     * Start a new update favorite task.
     */
    private void startUpdateFavoriteTask() {
        Boolean newStatus = this.videoParcel.getFavorite() != 1;
        UpdateFavoriteTask updateFavoriteTask = new UpdateFavoriteTask(this, this.videoParcel.getTimestamp());
        this.updateFavoriteTask = new WeakReference<UpdateFavoriteTask>(updateFavoriteTask);
        updateFavoriteTask.execute(newStatus);
    }

    public void updateFavoriteTaskFinished(Boolean response) {
        this.videoParcel.setFavorite(response ? 1 : 0);
        this.starredMenuItem.setIcon(response ? R.drawable.icon_starred : R.drawable.icon_not_starred);
    }

    public VideoParcel getVideoParcel() {
        return videoParcel;
    }

    public JSONArray getVideoParts() {
        return videoParts;
    }

    public String getVideoQuality() {
        return videoQuality;
    }
}
