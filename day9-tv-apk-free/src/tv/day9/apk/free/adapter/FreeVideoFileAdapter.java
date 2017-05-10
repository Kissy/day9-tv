package tv.day9.apk.free.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Toast;
import tv.day9.apk.R;
import tv.day9.apk.adapter.VideoFileAdapter;
import tv.day9.apk.model.VideoParcel;
import tv.day9.apk.proto.VideoPartsProto;

/**
 * @inheritDoc
 */
public class FreeVideoFileAdapter extends VideoFileAdapter {

    /**
     * Default constructor.
     *
     * @param context     The context.
     * @param videoParcel The video parcel.
     * @param videoFile   The video file data for the view.
     * @param videoPart   The video part.
     * @param parent      The container.
     */
    public FreeVideoFileAdapter(Context context, VideoParcel videoParcel, VideoPartsProto.VideoParts.VideoPart.VideoFile videoFile, int videoPart, ViewGroup parent) {
        super(context, videoParcel, videoFile, videoPart, parent);
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void handleDownloadButton(VideoPartsProto.VideoParts.VideoPart.VideoFile videoFile) {
        // Not available in free version
        Toast.makeText(context.getApplicationContext(), R.string.not_available, Toast.LENGTH_SHORT).show();
    }
}
