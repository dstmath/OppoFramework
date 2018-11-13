package com.color.widget;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;

public interface ColorActionModeCallback extends MultiChoiceModeListener {
    boolean onCreateSplitMenu(ActionMode actionMode, Menu menu);

    boolean onPrepareSplitMenu(ActionMode actionMode, Menu menu);

    boolean onSplitItemClicked(ActionMode actionMode, MenuItem menuItem);

    void onStartActionMode(ActionMode actionMode);
}
