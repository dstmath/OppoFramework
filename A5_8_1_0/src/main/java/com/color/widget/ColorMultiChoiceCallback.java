package com.color.widget;

import android.database.DataSetObserver;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import com.android.internal.view.menu.MenuBuilder;

public abstract class ColorMultiChoiceCallback extends DataSetObserver implements ColorActionModeCallback {
    protected ColorMultiChoiceAdapter mAdapter;
    private Menu mSplitMenu;
    private ColorViewPager mViewPager;

    public ColorMultiChoiceCallback(ColorMultiChoiceAdapter adapter) {
        this(adapter, null);
    }

    public ColorMultiChoiceCallback(ColorMultiChoiceAdapter adapter, ColorViewPager viewPager) {
        this.mAdapter = null;
        this.mViewPager = null;
        this.mSplitMenu = null;
        this.mAdapter = adapter;
        this.mViewPager = viewPager;
        this.mAdapter.setActionModeCallaback(this);
    }

    public void onChanged() {
        int checkedCount = this.mAdapter.getListView().getCheckedItemCount();
        int totalCount = getCheckableItemCount();
        updateTitle(this.mAdapter.getActionMode(), checkedCount, totalCount);
        updateMenu(checkedCount, totalCount);
    }

    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (this.mViewPager != null) {
            this.mViewPager.setDisableTouchEvent(true);
        }
        return this.mAdapter.onCreateActionMode(mode, menu);
    }

    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return this.mAdapter.onPrepareActionMode(mode, menu);
    }

    public void onDestroyActionMode(ActionMode mode) {
        if (this.mViewPager != null) {
            this.mViewPager.setDisableTouchEvent(false);
        }
        this.mAdapter.onDestroyActionMode(mode);
    }

    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return this.mAdapter.onActionItemClicked(mode, item);
    }

    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        this.mAdapter.onItemCheckedStateChanged(mode, position, id, checked);
    }

    public boolean onCreateSplitMenu(ActionMode mode, Menu menu) {
        return this.mAdapter.onCreateSplitMenu(mode, menu);
    }

    public boolean onPrepareSplitMenu(ActionMode mode, Menu menu) {
        return this.mAdapter.onPrepareSplitMenu(mode, menu);
    }

    public boolean onSplitItemClicked(ActionMode mode, MenuItem item) {
        return this.mAdapter.onSplitItemClicked(mode, item);
    }

    public void onStartActionMode(ActionMode mode) {
        this.mAdapter.onStartActionMode(mode);
    }

    protected void onUpdateTitle(ActionMode mode, int checkedCount, int totalCount) {
    }

    protected boolean onUpdateActionMenu(Menu menu, int checkedCount, int totalCount) {
        return false;
    }

    protected boolean onUpdateSplitMenu(Menu menu, int checkedCount, int totalCount) {
        return false;
    }

    protected int getCheckableItemCount() {
        return this.mAdapter.getCount();
    }

    private void updateTitle(ActionMode mode, int checkedCount, int totalCount) {
        if (mode != null) {
            onUpdateTitle(mode, checkedCount, totalCount);
        }
    }

    private void updateMenu(int checkedCount, int totalCount) {
        MenuBuilder actionMenu = (MenuBuilder) this.mAdapter.getActionMenu();
        MenuBuilder splitMenu = (MenuBuilder) this.mAdapter.getSplitMenu();
        if (actionMenu != null || splitMenu != null) {
            if (this.mAdapter.isAnimationsRunning()) {
                this.mAdapter.lockMenuUpdate();
            }
            if (actionMenu != null) {
                actionMenu.stopDispatchingItemsChanged();
                actionMenu.startDispatchingItemsChanged(onUpdateActionMenu(actionMenu, checkedCount, totalCount));
            }
            if (splitMenu != null) {
                splitMenu.stopDispatchingItemsChanged();
                splitMenu.startDispatchingItemsChanged(onUpdateSplitMenu(splitMenu, checkedCount, totalCount));
            }
            if (this.mAdapter.isAnimationsRunning()) {
                this.mAdapter.unlockMenuUpdate();
            }
        }
    }
}
