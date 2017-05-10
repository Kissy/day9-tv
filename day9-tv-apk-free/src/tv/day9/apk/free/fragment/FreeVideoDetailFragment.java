package tv.day9.apk.free.fragment;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import tv.day9.apk.R;
import tv.day9.apk.fragment.VideoDetailFragment;
import tv.day9.apk.free.adapter.FreeVideoFileAdapter;
import tv.day9.apk.proto.VideoPartsProto;

/**
 * @inheritDoc
 */
public class FreeVideoDetailFragment extends VideoDetailFragment {

    /**
     * @inheritDoc
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Not available in free version
        Toast.makeText(getActivity().getApplicationContext(), R.string.not_available, Toast.LENGTH_SHORT).show();
    }

    /**
     * @inheritDoc
     */
    @Override
    protected View createVideoFileView(VideoPartsProto.VideoParts.VideoPart.VideoFile videoFile, VideoPartsProto.VideoParts.VideoPart videoPart, LinearLayout videoFileList) {
        FreeVideoFileAdapter adapter = new FreeVideoFileAdapter(getActivity(), videoParcel, videoFile, videoPart.getPart(), videoFileList);
        return adapter.getView();
    }
}
