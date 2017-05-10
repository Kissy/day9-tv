package tv.day9.apk.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import tv.day9.apk.R;
import tv.day9.apk.provider.impl.VideoDAO;
import tv.day9.apk.util.UIUtils;

/**
 * VideoCursorAdapter.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: VideoCursorAdapter.java 175 2012-01-15 22:31:46Z Kissy $
 */
public class VideoCursorAdapter extends CursorAdapter {
    protected Context context;

    /**
     * Default constructor.
     *
     * @param context The context.
     * @param c The cursor.
     */
    public VideoCursorAdapter(Context context, Cursor c) {
        super(context, c, false);

        // Set context.
        this.context = context;
    }

    /**
     * @inheritDoc
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.list_item_video, viewGroup, false);
        view.setTag(new VideoViewHolder(view));
        return view;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((VideoViewHolder) view.getTag()).populateView(cursor);
    }

    /**
     * The view holder.
     */
    class VideoViewHolder {
        private ImageView imageViewIcon;
        private TextView textViewTitle;
        private TextView textViewSubTitle;
        private TextView textViewDate;

        /**
         * Default constructor.
         *
         * @param view The view.
         */
        public VideoViewHolder(final View view) {
            imageViewIcon = (ImageView) view.findViewById(R.id.vi_image);
            textViewTitle = (TextView) view.findViewById(R.id.vi_title);
            textViewSubTitle = (TextView) view.findViewById(R.id.vi_subtitle);
            textViewDate = (TextView) view.findViewById(R.id.vi_date);

        }

        /**
         * Bind data from cursor to view.
         *
         * @param cursor The cursor.
         */
        public void populateView(final Cursor cursor) {
            UIUtils.setVideoTypeIcon(imageViewIcon, cursor.getString(VideoDAO.CONTENT_TYPE_COLUMN));

            textViewTitle.setText(cursor.getString(VideoDAO.CONTENT_TITLE_COLUMN));
            textViewSubTitle.setText(cursor.getString(VideoDAO.CONTENT_SUBTITLE_COLUMN));
            textViewDate.setText(DateUtils.getRelativeTimeSpanString(0));
        }
    }
}
