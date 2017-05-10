package tv.day9.apk.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import tv.day9.apk.R;
import tv.day9.apk.binder.VideoViewBinder;
import tv.day9.apk.manager.AppRequestManager;
import tv.day9.apk.provider.impl.VideoDAO;
import tv.day9.apk.service.AppService;
import tv.day9.apk.worker.VideoListWorker;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Video list adapter.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: VideoEndlessAdapter.java 194 2012-01-24 14:53:01Z kissy $
 */
public class VideoEndlessAdapter extends SimpleCursorAdapter implements AppRequestManager.OnRequestFinishedListener  {
    private Context context;
	private View pendingView = null;
    private AtomicBoolean keepOnAppending = new AtomicBoolean(true);
    private int requestId = -1;

    /**
     * @inheritDoc
     */
    public VideoEndlessAdapter(Context context) {
        super(context, R.layout.list_item_video, null,
                new String[] {VideoDAO.TYPE, VideoDAO.TITLE, VideoDAO.SUBTITLE, VideoDAO._ID},
                new int[] {R.id.vi_image, R.id.vi_title, R.id.vi_subtitle, R.id.vi_date},
                0);
        setViewBinder(new VideoViewBinder());
        this.context = context;
    }

	/**
     * How many items are in the data set represented by this
     * Adapter.
     */
	@Override
	public int getCount() {
		if (keepOnAppending.get()) {
			// one more for "pending"
			return super.getCount() + 1;
		}

		return super.getCount();
	}

	/**
	 * Masks ViewType so the AdapterView replaces the "Pending" row when new
	 * data is loaded.
	 */
    @Override
	public int getItemViewType(int position) {
		if (position == super.getCount()) {
			return IGNORE_ITEM_VIEW_TYPE;
		}

		return super.getItemViewType(position);
	}

	/**
	 * Masks ViewType so the AdapterView replaces the "Pending" row when new
	 * data is loaded.
	 *
	 * @see #getItemViewType(int)
	 */
    @Override
	public int getViewTypeCount() {
		return super.getViewTypeCount() + 1;
	}

	/**
     * Get a View that displays the data at the specified
     * position in the data set. In this case, if we are at
     * the end of the list and we are still in append mode,
     * we ask for a pending view and return it, plus kick
     * off the background task to append more data to the
     * wrapped adapter.
     *
     * @param position Position of the item whose data we want
     * @param convertView View to recycle, if not null
     * @param parent ViewGroup containing the returned View
     */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        if (position == super.getCount() && keepOnAppending.get()) {
            if (pendingView == null) {
                pendingView = LayoutInflater.from(context).inflate(R.layout.list_item_loader, parent, false);
                fetchMoreData(position);
            }

            return pendingView;
        }

		return super.getView(position, convertView, parent);
	}

    /**
     * @inheritDoc
     */
    @Override
    @SuppressWarnings("unchecked")
    public void onRequestFinished(int requestId, int resultCode, Bundle payload) {
        if (this.requestId == requestId) {
            this.requestId = -1;
            AppRequestManager.from(context).removeOnRequestFinishedListener(this);

            if (resultCode == AppService.ERROR_CODE) {
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                keepOnAppending.getAndSet(false);
                return;
            }

            // No more data, stop appending
            if (!payload.getBoolean(VideoListWorker.HAS_MORE_DATA_KEY, false)) {
                keepOnAppending.getAndSet(false);
            }

            // Reset the pending view for next request.
            pendingView = null;
            notifyDataSetChanged();
        }
    }

    /**
     * Fetch mode data from server.
     *
     * @param position The position of the last item.
     */
    public void fetchMoreData(int position) {
        long lastVideoId = 0;
        if (position > 0) {
            lastVideoId = ((Cursor) getItem(position - 1)).getLong(VideoDAO.CONTENT_ID_COLUMN);
        }


        AppRequestManager.from(context).addOnRequestFinishedListener(this);
        requestId = AppRequestManager.from(context).getVideoList(lastVideoId);
    }

    /**
     * Get the keep on appending status.
     *
     * @return The keep on appending.
     */
    public AtomicBoolean getKeepOnAppending() {
        return keepOnAppending;
    }
}
