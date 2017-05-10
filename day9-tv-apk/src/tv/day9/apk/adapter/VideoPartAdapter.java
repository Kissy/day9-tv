package tv.day9.apk.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tv.day9.apk.R;
import tv.day9.apk.config.Constants;
import tv.day9.apk.model.VideoParcel;
import tv.day9.apk.util.IntentUtils;
import tv.day9.apk.util.UIUtils;

/**
 * The video file adapter.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: VideoPartAdapter.java 207 2012-01-29 01:50:25Z Kissy $
 */
public class VideoPartAdapter implements View.OnClickListener {
    private static final String TAG = VideoPartAdapter.class.getSimpleName();
    private static final String[] VIDEO_QUALITIES = new String[] {"hd", "sd", "ld", "source"};

    private Context context;
    private String videoFile;

    /**
     * Default constructor.
     *
     * @param context The context.
     * @param rootView The container.
     * @param videoFiles The video files data for the view.
     * @param videoPart The video part.
     * @param videoQuality The video quality.
     */
    @SuppressWarnings("ConstantConditions")
    public VideoPartAdapter(final Context context, LayoutInflater inflater, ViewGroup rootView, JSONArray videoFiles, final int videoPart, String videoQuality) throws JSONException {
        JSONObject videoFilesPart = (JSONObject) videoFiles.get(videoPart);
        String availableVideoQuality = getAvailableVideoQuality(videoFilesPart, videoQuality);
        if (availableVideoQuality == null) {
            return;
        }

        this.context = context;
        this.videoFile = videoFilesPart.getString(availableVideoQuality + "File");

        View view = inflater.inflate(R.layout.include_video_detail_part, rootView, false);
        ((TextView) view.findViewById(R.id.vp_title)).setText("Part " + (videoPart + 1));
        ((TextView) view.findViewById(R.id.vp_duration)).setText(DateUtils.formatElapsedTime(videoFilesPart.getLong("duration")));
        ((TextView) view.findViewById(R.id.vp_quality)).setText(videoFilesPart.getString(availableVideoQuality + "Width") + Constants.X + videoFilesPart.getString(availableVideoQuality + "Height"));
        ((TextView) view.findViewById(R.id.vp_size)).setText(UIUtils.formatSize(videoFilesPart.getLong(availableVideoQuality + "Size")));
        view.findViewById(R.id.vp_play).setOnClickListener(this);

        rootView.addView(view);
    }

    /**
     * Get the available video quality.
     *
     * @param videoFilesPart The list of files part.
     * @param videoQuality The desired video quality.
     * @return The available video quality.
     */
    private String getAvailableVideoQuality(JSONObject videoFilesPart, String videoQuality) throws JSONException {
        if (videoFilesPart.get(videoQuality + "File") != JSONObject.NULL) {
            return videoQuality;
        }
        for (String quality : VIDEO_QUALITIES) {
            if (videoFilesPart.get(quality + "File") != JSONObject.NULL) {
                return quality;
            }
        }
        return null;
    }

    /**
     * Check if the network state is available.
     *
     * @return False is not availble.
     */
    private boolean checkNetworkState() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        if (info == null || !info.isConnected()) {
            Toast.makeText(context.getApplicationContext(), R.string.connection_notavailable, Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }

    protected void handleDownloadButton(JSONObject videoFile) {
        // Start download worker.
        //AppRequestManager.from(context).initDownloadFile(videoParcel, videoFile, videoPart);
        context.startActivity(IntentUtils.getDownloadsActivityIntent());
    }

    /**
     * Get the extensions from file path.
     *
     * @param file The file path.
     * @return The extension.
     */
    public static String getExt(String file) {
        int k = file.lastIndexOf(Constants.DOT);
        String ext = Constants.STRING_EMPTY;
        if (k != -1) {
            ext = file.substring(k + 1);
        }
        return ext;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vp_play:
                Intent watchVideoItent = new Intent(Intent.ACTION_VIEW);
                watchVideoItent.setDataAndType(Uri.parse(videoFile), Constants.DATATYPE_VIDEO);
                context.startActivity(watchVideoItent);
                break;
        }
    }
}
