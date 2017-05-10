package tv.day9.apk.activity.tablet;

import android.os.Bundle;
import android.view.ViewGroup;
import tv.day9.apk.R;
import tv.day9.apk.activity.BaseMultiPaneActivity;
import tv.day9.apk.activity.phone.VideoDetailActivity;
import tv.day9.apk.fragment.VideoDetailFragment;
import tv.day9.apk.fragment.VideosFragment;

public class VideosMultiPaneActivity extends BaseMultiPaneActivity {

    protected static final String VIDEO_DETAILS = "video_detail";

    /**
     * @inheritDoc
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_videos);
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();

        // Add the videos list fragment
        VideosFragment fragment = new VideosFragment();
        fragment.setArguments(intentToFragmentArguments(getIntent()));
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_videos, fragment).commit();

        // Get the video details container
        ViewGroup detailContainer = (ViewGroup) findViewById(R.id.fragment_container_video_detail);
        if (detailContainer != null && detailContainer.getChildCount() > 0) {
            detailContainer.setBackgroundColor(R.color.dark);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public FragmentReplaceInfo onSubstituteFragmentForActivityLaunch(String activityClassName) {
        if (VideoDetailActivity.class.getName().equals(activityClassName)) {
            findViewById(R.id.fragment_container_video_detail).setBackgroundColor(R.color.dark);
            return new FragmentReplaceInfo(VideoDetailFragment.class, VIDEO_DETAILS, R.id.fragment_container_video_detail);
        }
        return null;
    }
}