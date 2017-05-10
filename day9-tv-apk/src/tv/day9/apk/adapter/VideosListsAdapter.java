package tv.day9.apk.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import tv.day9.apk.fragment.DownloadsFragment;
import tv.day9.apk.fragment.FavoritesFragment;
import tv.day9.apk.fragment.VideosFragment;

public class VideosListsAdapter extends FragmentPagerAdapter {
    private static final int NUM_ITEMS = 3;

    public VideosListsAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * @inheritDoc
     */
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    /**
     * @inheritDoc
     */
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            default:
                return "Videos";
            case 1:
                return "Favorites";
            case 2:
                return "Downloads";
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            default:
                return new VideosFragment();
            case 1:
                return new FavoritesFragment();
            case 2:
                return new DownloadsFragment();
        }
    }
}