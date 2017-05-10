package tv.day9.apk.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import tv.day9.apk.R;
import tv.day9.apk.fragment.VideosFragment;
import tv.day9.apk.util.IntentUtils;

/**
 * An activity that shows session and sandbox search results. This activity can be either single
 * or multi-pane, depending on the device configuration. We want the multi-pane support that
 * {@link BaseMultiPaneActivity} offers, so we inherit from it instead of
 * {@link BaseSinglePaneActivity}.
 */
public class SearchActivity extends BaseActivity {
    private static final String TAG = SearchActivity.class.getSimpleName();
    private static final String VIDEOS_FRAGMENT = "fragment_videos";
    private static final String SEARCH = "Search";

    private String query;
    private VideosFragment videosFragment;

    /**
     * @inheritDoc
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlepane_empty);

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
        }

        final CharSequence title = getString(R.string.title_search_query, query);
        if (title == null || title.length() == 0) {
            getActivityHelper().setActionBarTitle(getTitle());
        } else {
            getActivityHelper().setActionBarTitle(title);
        }

        setupVideoFragment();
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
        }

        final CharSequence title = getString(R.string.title_search_query, query);
        getActivityHelper().setActionBarTitle(title);

        //videosFragment.reloadFromArguments(getVideosFragmentArguments());
    }

    /**
     * Setup the videos fragment.
     */
    private void setupVideoFragment() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        videosFragment = (VideosFragment) fragmentManager.findFragmentByTag(VIDEOS_FRAGMENT);
        if (videosFragment == null) {
            videosFragment = new VideosFragment();
            videosFragment.setArguments(getVideosFragmentArguments());
            fragmentManager.beginTransaction()
                    .add(R.id.root_container, videosFragment, VIDEOS_FRAGMENT)
                    .commit();
        } else {
            //videosFragment.reloadFromArguments(getVideosFragmentArguments());
        }
    }

    /**
     * Get video fragment arguments.
     *
     * @return The video fragment arguments.
     */
    private Bundle getVideosFragmentArguments() {
        final Intent intent = IntentUtils.getVideosActivityIntent(this);
        intent.putExtra(VideosFragment.DISPLAY_STATE, VideosFragment.SEARCH);
        intent.putExtra(VideosFragment.SEARCH_QUERY, query);
        return intentToFragmentArguments(intent);
    }
}

