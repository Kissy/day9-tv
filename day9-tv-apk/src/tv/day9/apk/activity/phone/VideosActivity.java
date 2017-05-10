package tv.day9.apk.activity.phone;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import tv.day9.apk.R;
import tv.day9.apk.activity.BaseActivity;
import tv.day9.apk.activity.BaseSinglePaneActivity;
import tv.day9.apk.adapter.VideosListsAdapter;
import tv.day9.apk.fragment.VideosFragment;

/**
 * Videos activity
 */
public class VideosActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    private ViewPager pager;

    /**
     * @inheritDoc
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        PagerAdapter adapter = new VideosListsAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.pager);

        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(this);
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
    public void onPageScrolled(int i, float v, int i2) {

    }

    /**
     * @inheritDoc
     */
    @Override
    public void onPageSelected(int i) {

    }

    /**
     * @inheritDoc
     */
    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
