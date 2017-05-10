package tv.day9.apk.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import tv.day9.apk.R;
import tv.day9.apk.config.Constants;
import tv.day9.apk.config.DownloadConstants;
import tv.day9.apk.provider.impl.DownloadDAO;
import tv.day9.apk.util.UIUtils;

/**
 * DownloadCursorAdapter.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: DownloadCursorAdapter.java 183 2012-01-19 00:39:45Z Kissy $
 */
public class DownloadCursorAdapter extends CursorAdapter {

    protected Context context;

    /**
     * Default constructor.
     *
     * @param context The context.
     * @param c The cursor.
     */
    public DownloadCursorAdapter(Context context, Cursor c) {
        super(context, c);

        // Set context.
        this.context = context;
    }

    /**
     * @inheritDoc
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.list_item_download, viewGroup, false);
        view.setTag(new DownloadViewHolder(view));
        return view;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((DownloadViewHolder) view.getTag()).populateView(cursor);
    }

    /**
     * The view holder.
     */
    class DownloadViewHolder {

        private TextView textViewTitle;
        private TextView textViewStatus;
        private TextView textViewSubType;
        private ProgressBar progressBar;
        private TextView progressText;

        /**
         * Default constructor.
         *
         * @param view The view.
         */
        public DownloadViewHolder(final View view) {
            textViewTitle = (TextView) view.findViewById(R.id.vd_title);
            textViewStatus = (TextView) view.findViewById(R.id.vd_status);
            textViewSubType = (TextView) view.findViewById(R.id.vd_subtype);
            progressBar = (ProgressBar) view.findViewById(R.id.vd_progress);
            progressText = (TextView) view.findViewById(R.id.vd_progressText);
        }

        /**
         * Bind data from cursor to view.
         *
         * @param cursor The cursor.
         */
        public void populateView(final Cursor cursor) {
            textViewTitle.setText(cursor.getString(DownloadDAO.CONTENT_TITLE_COLUMN));
            int status = cursor.getInt(DownloadDAO.CONTENT_STATUS_COLUMN);
            textViewStatus.setText(UIUtils.getStringFromDownloadStatus(context, status));
            textViewSubType.setText(DateUtils.formatElapsedTime(cursor.getLong(DownloadDAO.CONTENT_DURATION_COLUMN))
                    + Constants.SEPARATOR + cursor.getString(DownloadDAO.CONTENT_TYPE_COLUMN)
                    + Constants.LEFT_PARENTHESIS + cursor.getString(DownloadDAO.CONTENT_DIMENSION_COLUMN) + Constants.RIGHT_PARENTHESIS);

            int totalByte = cursor.getInt(DownloadDAO.CONTENT_TOTAL_BYTES_COLUMN);
            int currentByte = cursor.getInt(DownloadDAO.CONTENT_CURRENT_BYTES_COLUMN);

            progressBar.setMax(totalByte);
            progressBar.setProgress(currentByte);
            progressBar.setIndeterminate(status == DownloadConstants.STATUS_PENDING);

            if (status == DownloadConstants.STATUS_SUCCESS) {
                progressText.setText(UIUtils.formatSize(totalByte));
            } else {
                progressText.setText(UIUtils.formatSize(currentByte) + Constants.SLASH_SPACE + UIUtils.formatSize(totalByte));
            }
        }
    }
}
