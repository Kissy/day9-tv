package tv.day9.apk.free.activity.tablet;

import tv.day9.apk.R;
import tv.day9.apk.free.activity.phone.VideoDetailActivity;
import tv.day9.apk.free.fragment.FreeVideoDetailFragment;

public class VideosMultiPaneActivity extends tv.day9.apk.activity.tablet.VideosMultiPaneActivity {

    /**
     * @inheritDoc
     */
    @Override
    public FragmentReplaceInfo onSubstituteFragmentForActivityLaunch(String activityClassName) {
        if (VideoDetailActivity.class.getName().equals(activityClassName)) {
            findViewById(R.id.fragment_container_video_detail).setBackgroundColor(0);
            return new FragmentReplaceInfo(FreeVideoDetailFragment.class, VIDEO_DETAILS, R.id.fragment_container_video_detail);
        }
        return null;
    }
}
