package com.color.widget;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import com.color.actionbar.app.ColorActionBarUtil;
import com.color.util.ColorLog;
import com.color.widget.ColorViewPager.OnPageChangeListener;
import com.color.widget.ColorViewPager.OnPageMenuChangeListener;

public class ColorPagerController implements TabListener, OnPageChangeListener, OnPageMenuChangeListener {
    private Activity mActivity = null;
    private int mScrollState = 0;
    private int mSelectedPage = -1;
    protected final Class<?> mTagClass = getClass();
    private ColorViewPager mViewPager = null;

    public ColorPagerController(Activity activity, ColorViewPager viewPager) {
        setActivity(activity);
        setViewPager(viewPager);
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        ColorLog.d("log.key.view_pager.select", this.mTagClass, new Object[]{"app onTabReselected"});
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        this.mSelectedPage = tab.getPosition();
        this.mViewPager.setCurrentItem(this.mSelectedPage, false);
        if (this.mActivity != null && this.mScrollState == 0) {
            ColorActionBarUtil.setMenuUpdateMode(this.mActivity.getActionBar(), 0);
        }
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        ColorLog.d("log.key.view_pager.select", this.mTagClass, new Object[]{"app onTabUnselected"});
    }

    public void onPageScrollStateChanged(int state) {
        this.mScrollState = state;
        ActionBar actionBar = this.mActivity.getActionBar();
        if (actionBar != null) {
            ColorActionBarUtil.updateTabScrollState(actionBar, state);
        }
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        ActionBar actionBar = this.mActivity.getActionBar();
        if (actionBar != null) {
            ColorActionBarUtil.updateTabScrollPosition(actionBar, position, positionOffset, positionOffsetPixels);
        }
    }

    public void onPageSelected(int position) {
        ActionBar actionBar = this.mActivity.getActionBar();
        if (actionBar != null) {
            actionBar.selectTab(actionBar.getTabAt(position));
        }
    }

    public void onPageMenuScrolled(int index, float offset) {
        ActionBar actionBar = this.mActivity.getActionBar();
        if (actionBar != null) {
            ColorActionBarUtil.updateMenuScrollPosition(actionBar, index, offset);
        }
    }

    public void onPageMenuSelected(int position) {
        this.mSelectedPage = position;
        this.mActivity.invalidateOptionsMenu();
    }

    public void onPageMenuScrollStateChanged(int state) {
        ActionBar actionBar = this.mActivity.getActionBar();
        if (actionBar != null) {
            ColorActionBarUtil.updateMenuScrollState(actionBar, state);
        }
    }

    public void onPageMenuScrollDataChanged() {
        ActionBar actionBar = this.mActivity.getActionBar();
        if (actionBar != null) {
            ColorLog.d("log.key.view_pager.select", this.mTagClass, new Object[]{"app onPageMenuScrollDataChanged"});
            ColorActionBarUtil.updateMenuScrollData(actionBar);
        }
    }

    public int getSelectedPage() {
        return this.mSelectedPage;
    }

    private void setActivity(Activity activity) {
        if (activity == null) {
            throw new RuntimeException("Your activity is null!");
        }
        this.mActivity = activity;
    }

    private void setViewPager(ColorViewPager viewPager) {
        if (viewPager == null) {
            throw new RuntimeException("Your viewPager is null!");
        }
        this.mViewPager = viewPager;
        ActionBar actionBar = this.mActivity.getActionBar();
        if (actionBar instanceof ColorBottomMenuCallback) {
            this.mViewPager.bindSplitMenuCallback((ColorBottomMenuCallback) actionBar);
        }
    }
}
