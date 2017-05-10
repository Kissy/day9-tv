package tv.day9.apk.free.fragment;

import android.widget.Toast;
import tv.day9.apk.R;
import tv.day9.apk.fragment.DashboardFragment;

/**
 * @inheritDoc
 */
public class FreeDashboardFragment extends DashboardFragment {

    /**
     * @inheritDoc
     */
    @Override
    protected void handleDownloadsButton() {
        // Not available in free version
        Toast.makeText(getActivity().getApplicationContext(), R.string.not_available, Toast.LENGTH_SHORT).show();
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void handleFavoritesButton() {
        // Not available in free version
        Toast.makeText(getActivity().getApplicationContext(), R.string.not_available, Toast.LENGTH_SHORT).show();
    }
}
