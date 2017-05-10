package tv.day9.apk.binder;

import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import tv.day9.apk.R;
import tv.day9.apk.util.UIUtils;

/**
 * @author Guillaume Le Biller
 */
public class VideoViewBinder implements SimpleCursorAdapter.ViewBinder {
    /**
     * @inheritDoc
     */
    @Override
    public boolean setViewValue(View view, Cursor cursor, int i) {
        switch (view.getId()) {
            case R.id.vi_image:
                UIUtils.setVideoTypeIcon((ImageView) view, cursor.getString(i));
                return true;
            case R.id.vi_date:
                ((TextView) view).setText(DateUtils.getRelativeTimeSpanString(cursor.getLong(i)));
                return true;
            default:
                return false;
        }
    }
}
