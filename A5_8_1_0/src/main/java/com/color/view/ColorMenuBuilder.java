package com.color.view;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyCharacterMap.KeyData;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import com.android.internal.R;
import com.android.internal.view.menu.SubMenuBuilder;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ColorMenuBuilder implements Menu {
    private static final String ACTION_VIEW_STATES_KEY = "android:menu:actionviewstates";
    private static final String EXPANDED_ACTION_VIEW_ID = "android:menu:expandedactionview";
    private static final String PRESENTER_KEY = "android:menu:presenters";
    private static final String TAG = "ColorMenuBuilder";
    private static final int[] sCategoryToOrder = new int[]{1, 4, 5, 3, 2, 0};
    private ArrayList<ColorMenuItemImpl> mActionItems;
    private Callback mCallback;
    private final Context mContext;
    private ContextMenuInfo mCurrentMenuInfo;
    private int mDefaultShowAsAction = 0;
    private ColorMenuItemImpl mExpandedItem;
    private SparseArray<Parcelable> mFrozenViewStates;
    Drawable mHeaderIcon;
    CharSequence mHeaderTitle;
    View mHeaderView;
    private boolean mIsActionItemsStale;
    private boolean mIsClosing = false;
    private boolean mIsVisibleItemsStale;
    private ArrayList<ColorMenuItemImpl> mItems;
    private boolean mItemsChangedWhileDispatchPrevented = false;
    private ArrayList<ColorMenuItemImpl> mNonActionItems;
    private boolean mOptionalIconsVisible = false;
    private CopyOnWriteArrayList<WeakReference<ColorMenuPresenter>> mPresenters = new CopyOnWriteArrayList();
    private boolean mPreventDispatchingItemsChanged = false;
    private boolean mQwertyMode;
    private final Resources mResources;
    private boolean mShortcutsVisible;
    private ArrayList<ColorMenuItemImpl> mTempShortcutItemList = new ArrayList();
    private ArrayList<ColorMenuItemImpl> mVisibleItems;

    public interface Callback {
        boolean onMenuItemSelected(ColorMenuBuilder colorMenuBuilder, MenuItem menuItem);

        void onMenuModeChange(ColorMenuBuilder colorMenuBuilder);
    }

    public interface ItemInvoker {
        boolean invokeItem(ColorMenuItemImpl colorMenuItemImpl);
    }

    public ColorMenuBuilder(Context context) {
        this.mContext = context;
        this.mResources = context.getResources();
        this.mItems = new ArrayList();
        this.mVisibleItems = new ArrayList();
        this.mIsVisibleItemsStale = true;
        this.mActionItems = new ArrayList();
        this.mNonActionItems = new ArrayList();
        this.mIsActionItemsStale = true;
        setShortcutsVisibleInner(true);
    }

    public ColorMenuBuilder setDefaultShowAsAction(int defaultShowAsAction) {
        this.mDefaultShowAsAction = defaultShowAsAction;
        return this;
    }

    public void addMenuPresenter(ColorMenuPresenter presenter) {
        addMenuPresenter(presenter, this.mContext);
    }

    public void addMenuPresenter(ColorMenuPresenter presenter, Context menuContext) {
        this.mPresenters.add(new WeakReference(presenter));
        presenter.initForMenu(menuContext, this);
        this.mIsActionItemsStale = true;
    }

    public void removeMenuPresenter(ColorMenuPresenter presenter) {
        for (WeakReference<ColorMenuPresenter> ref : this.mPresenters) {
            ColorMenuPresenter item = (ColorMenuPresenter) ref.get();
            if (item == null || item == presenter) {
                this.mPresenters.remove(ref);
            }
        }
    }

    private void dispatchPresenterUpdate(boolean cleared) {
        if (!this.mPresenters.isEmpty()) {
            stopDispatchingItemsChanged();
            for (WeakReference<ColorMenuPresenter> ref : this.mPresenters) {
                ColorMenuPresenter presenter = (ColorMenuPresenter) ref.get();
                if (presenter == null) {
                    this.mPresenters.remove(ref);
                } else {
                    presenter.updateMenuView(cleared);
                }
            }
            startDispatchingItemsChanged();
        }
    }

    private boolean dispatchSubMenuSelected(SubMenuBuilder subMenu, ColorMenuPresenter preferredPresenter) {
        if (this.mPresenters.isEmpty()) {
            return false;
        }
        boolean result = false;
        if (preferredPresenter != null) {
            result = preferredPresenter.onSubMenuSelected(subMenu);
        }
        for (WeakReference<ColorMenuPresenter> ref : this.mPresenters) {
            ColorMenuPresenter presenter = (ColorMenuPresenter) ref.get();
            if (presenter == null) {
                this.mPresenters.remove(ref);
            } else if (!result) {
                result = presenter.onSubMenuSelected(subMenu);
            }
        }
        return result;
    }

    private void dispatchSaveInstanceState(Bundle outState) {
        if (!this.mPresenters.isEmpty()) {
            SparseArray<Parcelable> presenterStates = new SparseArray();
            for (WeakReference<ColorMenuPresenter> ref : this.mPresenters) {
                ColorMenuPresenter presenter = (ColorMenuPresenter) ref.get();
                if (presenter == null) {
                    this.mPresenters.remove(ref);
                } else {
                    int id = presenter.getId();
                    if (id > 0) {
                        Parcelable state = presenter.onSaveInstanceState();
                        if (state != null) {
                            presenterStates.put(id, state);
                        }
                    }
                }
            }
            outState.putSparseParcelableArray(PRESENTER_KEY, presenterStates);
        }
    }

    private void dispatchRestoreInstanceState(Bundle state) {
        SparseArray<Parcelable> presenterStates = state.getSparseParcelableArray(PRESENTER_KEY);
        if (presenterStates != null && !this.mPresenters.isEmpty()) {
            for (WeakReference<ColorMenuPresenter> ref : this.mPresenters) {
                ColorMenuPresenter presenter = (ColorMenuPresenter) ref.get();
                if (presenter == null) {
                    this.mPresenters.remove(ref);
                } else {
                    int id = presenter.getId();
                    if (id > 0) {
                        Parcelable parcel = (Parcelable) presenterStates.get(id);
                        if (parcel != null) {
                            presenter.onRestoreInstanceState(parcel);
                        }
                    }
                }
            }
        }
    }

    public void savePresenterStates(Bundle outState) {
        dispatchSaveInstanceState(outState);
    }

    public void restorePresenterStates(Bundle state) {
        dispatchRestoreInstanceState(state);
    }

    public void saveActionViewStates(Bundle outStates) {
        SparseArray viewStates = null;
        int itemCount = size();
        for (int i = 0; i < itemCount; i++) {
            MenuItem item = getItem(i);
            View v = item.getActionView();
            if (!(v == null || v.getId() == -1)) {
                if (viewStates == null) {
                    viewStates = new SparseArray();
                }
                v.saveHierarchyState(viewStates);
                if (item.isActionViewExpanded()) {
                    outStates.putInt(EXPANDED_ACTION_VIEW_ID, item.getItemId());
                }
            }
            if (item.hasSubMenu()) {
                ((SubMenuBuilder) item.getSubMenu()).saveActionViewStates(outStates);
            }
        }
        if (viewStates != null) {
            outStates.putSparseParcelableArray(getActionViewStatesKey(), viewStates);
        }
    }

    public void restoreActionViewStates(Bundle states) {
        if (states != null) {
            SparseArray<Parcelable> viewStates = states.getSparseParcelableArray(getActionViewStatesKey());
            int itemCount = size();
            for (int i = 0; i < itemCount; i++) {
                MenuItem item = getItem(i);
                View v = item.getActionView();
                if (!(v == null || v.getId() == -1)) {
                    v.restoreHierarchyState(viewStates);
                }
                if (item.hasSubMenu()) {
                    ((SubMenuBuilder) item.getSubMenu()).restoreActionViewStates(states);
                }
            }
            int expandedId = states.getInt(EXPANDED_ACTION_VIEW_ID);
            if (expandedId > 0) {
                MenuItem itemToExpand = findItem(expandedId);
                if (itemToExpand != null) {
                    itemToExpand.expandActionView();
                }
            }
        }
    }

    protected String getActionViewStatesKey() {
        return ACTION_VIEW_STATES_KEY;
    }

    public void setCallback(Callback cb) {
        this.mCallback = cb;
    }

    private MenuItem addInternal(int group, int id, int categoryOrder, CharSequence title) {
        int ordering = getOrdering(categoryOrder);
        ColorMenuItemImpl item = createNewMenuItem(group, id, categoryOrder, ordering, title, this.mDefaultShowAsAction);
        if (this.mCurrentMenuInfo != null) {
            item.setMenuInfo(this.mCurrentMenuInfo);
        }
        this.mItems.add(findInsertIndex(this.mItems, ordering), item);
        onItemsChanged(true);
        return item;
    }

    private ColorMenuItemImpl createNewMenuItem(int group, int id, int categoryOrder, int ordering, CharSequence title, int defaultShowAsAction) {
        return new ColorMenuItemImpl(this, group, id, categoryOrder, ordering, title, defaultShowAsAction);
    }

    public MenuItem add(CharSequence title) {
        return addInternal(0, 0, 0, title);
    }

    public MenuItem add(int titleRes) {
        return addInternal(0, 0, 0, this.mResources.getString(titleRes));
    }

    public MenuItem add(int group, int id, int categoryOrder, CharSequence title) {
        return addInternal(group, id, categoryOrder, title);
    }

    public MenuItem add(int group, int id, int categoryOrder, int title) {
        return addInternal(group, id, categoryOrder, this.mResources.getString(title));
    }

    public SubMenu addSubMenu(CharSequence title) {
        return null;
    }

    public SubMenu addSubMenu(int titleRes) {
        return null;
    }

    public SubMenu addSubMenu(int group, int id, int categoryOrder, CharSequence title) {
        return null;
    }

    public SubMenu addSubMenu(int group, int id, int categoryOrder, int title) {
        return null;
    }

    public int addIntentOptions(int group, int id, int categoryOrder, ComponentName caller, Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems) {
        PackageManager pm = this.mContext.getPackageManager();
        List<ResolveInfo> lri = pm.queryIntentActivityOptions(caller, specifics, intent, 0);
        int N = lri != null ? lri.size() : 0;
        if ((flags & 1) == 0) {
            removeGroup(group);
        }
        for (int i = 0; i < N; i++) {
            Intent intent2;
            ResolveInfo ri = (ResolveInfo) lri.get(i);
            if (ri.specificIndex < 0) {
                intent2 = intent;
            } else {
                intent2 = specifics[ri.specificIndex];
            }
            Intent rintent = new Intent(intent2);
            rintent.setComponent(new ComponentName(ri.activityInfo.applicationInfo.packageName, ri.activityInfo.name));
            MenuItem item = add(group, id, categoryOrder, ri.loadLabel(pm)).setIcon(ri.loadIcon(pm)).setIntent(rintent);
            if (outSpecificItems != null && ri.specificIndex >= 0) {
                outSpecificItems[ri.specificIndex] = item;
            }
        }
        return N;
    }

    public void removeItem(int id) {
        removeItemAtInt(findItemIndex(id), true);
    }

    public void removeGroup(int group) {
        int i = findGroupIndex(group);
        if (i >= 0) {
            int maxRemovable = this.mItems.size() - i;
            int numRemoved = 0;
            while (true) {
                int numRemoved2 = numRemoved;
                numRemoved = numRemoved2 + 1;
                if (numRemoved2 >= maxRemovable || ((ColorMenuItemImpl) this.mItems.get(i)).getGroupId() != group) {
                    onItemsChanged(true);
                } else {
                    removeItemAtInt(i, false);
                }
            }
            onItemsChanged(true);
        }
    }

    private void removeItemAtInt(int index, boolean updateChildrenOnMenuViews) {
        if (index >= 0 && index < this.mItems.size()) {
            this.mItems.remove(index);
            if (updateChildrenOnMenuViews) {
                onItemsChanged(true);
            }
        }
    }

    public void removeItemAt(int index) {
        removeItemAtInt(index, true);
    }

    public void clearAll() {
        this.mPreventDispatchingItemsChanged = true;
        clear();
        clearHeader();
        this.mPreventDispatchingItemsChanged = false;
        this.mItemsChangedWhileDispatchPrevented = false;
        onItemsChanged(true);
    }

    public void clear() {
        if (this.mExpandedItem != null) {
            collapseItemActionView(this.mExpandedItem);
        }
        this.mItems.clear();
        onItemsChanged(true);
    }

    void setExclusiveItemChecked(MenuItem item) {
        int group = item.getGroupId();
        int N = this.mItems.size();
        for (int i = 0; i < N; i++) {
            MenuItem curItem = (ColorMenuItemImpl) this.mItems.get(i);
            if (curItem.getGroupId() == group && curItem.isExclusiveCheckable() && curItem.isCheckable()) {
                curItem.setCheckedInt(curItem == item);
            }
        }
    }

    public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {
        int N = this.mItems.size();
        for (int i = 0; i < N; i++) {
            ColorMenuItemImpl item = (ColorMenuItemImpl) this.mItems.get(i);
            if (item.getGroupId() == group) {
                item.setExclusiveCheckable(exclusive);
                item.setCheckable(checkable);
            }
        }
    }

    public void setGroupVisible(int group, boolean visible) {
        int N = this.mItems.size();
        boolean changedAtLeastOneItem = false;
        for (int i = 0; i < N; i++) {
            ColorMenuItemImpl item = (ColorMenuItemImpl) this.mItems.get(i);
            if (item.getGroupId() == group && item.setVisibleInt(visible)) {
                changedAtLeastOneItem = true;
            }
        }
        if (changedAtLeastOneItem) {
            onItemsChanged(true);
        }
    }

    public void setGroupEnabled(int group, boolean enabled) {
        int N = this.mItems.size();
        for (int i = 0; i < N; i++) {
            ColorMenuItemImpl item = (ColorMenuItemImpl) this.mItems.get(i);
            if (item.getGroupId() == group) {
                item.setEnabled(enabled);
            }
        }
    }

    public boolean hasVisibleItems() {
        int size = size();
        for (int i = 0; i < size; i++) {
            if (((ColorMenuItemImpl) this.mItems.get(i)).isVisible()) {
                return true;
            }
        }
        return false;
    }

    public MenuItem findItem(int id) {
        int size = size();
        for (int i = 0; i < size; i++) {
            ColorMenuItemImpl item = (ColorMenuItemImpl) this.mItems.get(i);
            if (item.getItemId() == id) {
                return item;
            }
            if (item.hasSubMenu()) {
                MenuItem possibleItem = item.getSubMenu().findItem(id);
                if (possibleItem != null) {
                    return possibleItem;
                }
            }
        }
        return null;
    }

    public int findItemIndex(int id) {
        int size = size();
        for (int i = 0; i < size; i++) {
            if (((ColorMenuItemImpl) this.mItems.get(i)).getItemId() == id) {
                return i;
            }
        }
        return -1;
    }

    public int findGroupIndex(int group) {
        return findGroupIndex(group, 0);
    }

    public int findGroupIndex(int group, int start) {
        int size = size();
        if (start < 0) {
            start = 0;
        }
        for (int i = start; i < size; i++) {
            if (((ColorMenuItemImpl) this.mItems.get(i)).getGroupId() == group) {
                return i;
            }
        }
        return -1;
    }

    public int size() {
        return this.mItems.size();
    }

    public MenuItem getItem(int index) {
        return (MenuItem) this.mItems.get(index);
    }

    public boolean isShortcutKey(int keyCode, KeyEvent event) {
        return findItemWithShortcutForKey(keyCode, event) != null;
    }

    public void setQwertyMode(boolean isQwerty) {
        this.mQwertyMode = isQwerty;
        onItemsChanged(false);
    }

    private static int getOrdering(int categoryOrder) {
        int index = (Menu.CATEGORY_MASK & categoryOrder) >> 16;
        if (index >= 0 && index < sCategoryToOrder.length) {
            return (sCategoryToOrder[index] << 16) | (65535 & categoryOrder);
        }
        throw new IllegalArgumentException("order does not contain a valid category.");
    }

    boolean isQwertyMode() {
        return this.mQwertyMode;
    }

    public void setShortcutsVisible(boolean shortcutsVisible) {
        if (this.mShortcutsVisible != shortcutsVisible) {
            setShortcutsVisibleInner(shortcutsVisible);
            onItemsChanged(false);
        }
    }

    private void setShortcutsVisibleInner(boolean shortcutsVisible) {
        boolean z;
        if (!shortcutsVisible || this.mResources.getConfiguration().keyboard == 1) {
            z = false;
        } else {
            z = this.mResources.getBoolean(R.bool.config_showMenuShortcutsWhenKeyboardPresent);
        }
        this.mShortcutsVisible = z;
    }

    public boolean isShortcutsVisible() {
        return this.mShortcutsVisible;
    }

    Resources getResources() {
        return this.mResources;
    }

    public Context getContext() {
        return this.mContext;
    }

    boolean dispatchMenuItemSelected(ColorMenuBuilder menu, MenuItem item) {
        return this.mCallback != null ? this.mCallback.onMenuItemSelected(menu, item) : false;
    }

    public void changeMenuMode() {
        if (this.mCallback != null) {
            this.mCallback.onMenuModeChange(this);
        }
    }

    private static int findInsertIndex(ArrayList<ColorMenuItemImpl> items, int ordering) {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (((ColorMenuItemImpl) items.get(i)).getOrdering() <= ordering) {
                return i + 1;
            }
        }
        return 0;
    }

    public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
        ColorMenuItemImpl item = findItemWithShortcutForKey(keyCode, event);
        boolean handled = false;
        if (item != null) {
            handled = performItemAction(item, flags);
        }
        if ((flags & 2) != 0) {
            close(true);
        }
        return handled;
    }

    void findItemsWithShortcutForKey(List<ColorMenuItemImpl> items, int keyCode, KeyEvent event) {
        boolean qwerty = isQwertyMode();
        int metaState = event.getMetaState();
        KeyData possibleChars = new KeyData();
        if (event.getKeyData(possibleChars) || keyCode == 67) {
            int N = this.mItems.size();
            for (int i = 0; i < N; i++) {
                ColorMenuItemImpl item = (ColorMenuItemImpl) this.mItems.get(i);
                if (item.hasSubMenu()) {
                    ((ColorMenuBuilder) item.getSubMenu()).findItemsWithShortcutForKey(items, keyCode, event);
                }
                char shortcutChar = qwerty ? item.getAlphabeticShortcut() : item.getNumericShortcut();
                if ((metaState & 5) == 0 && shortcutChar != 0 && ((shortcutChar == possibleChars.meta[0] || shortcutChar == possibleChars.meta[2] || (qwerty && shortcutChar == 8 && keyCode == 67)) && item.isEnabled())) {
                    items.add(item);
                }
            }
        }
    }

    ColorMenuItemImpl findItemWithShortcutForKey(int keyCode, KeyEvent event) {
        ArrayList<ColorMenuItemImpl> items = this.mTempShortcutItemList;
        items.clear();
        findItemsWithShortcutForKey(items, keyCode, event);
        if (items.isEmpty()) {
            return null;
        }
        int metaState = event.getMetaState();
        KeyData possibleChars = new KeyData();
        event.getKeyData(possibleChars);
        int size = items.size();
        if (size == 1) {
            return (ColorMenuItemImpl) items.get(0);
        }
        boolean qwerty = isQwertyMode();
        for (int i = 0; i < size; i++) {
            char shortcutChar;
            ColorMenuItemImpl item = (ColorMenuItemImpl) items.get(i);
            if (qwerty) {
                shortcutChar = item.getAlphabeticShortcut();
            } else {
                shortcutChar = item.getNumericShortcut();
            }
            if ((shortcutChar == possibleChars.meta[0] && (metaState & 2) == 0) || ((shortcutChar == possibleChars.meta[2] && (metaState & 2) != 0) || (qwerty && shortcutChar == 8 && keyCode == 67))) {
                return item;
            }
        }
        return null;
    }

    public boolean performIdentifierAction(int id, int flags) {
        return performItemAction(findItem(id), flags);
    }

    public boolean performItemAction(MenuItem item, int flags) {
        return performItemAction(item, null, flags);
    }

    public boolean performItemAction(MenuItem item, ColorMenuPresenter preferredPresenter, int flags) {
        ColorMenuItemImpl itemImpl = (ColorMenuItemImpl) item;
        if (itemImpl == null || (itemImpl.isEnabled() ^ 1) != 0) {
            return false;
        }
        boolean invoked = itemImpl.invoke();
        ActionProvider provider = item.getActionProvider();
        boolean providerHasSubMenu = provider != null ? provider.hasSubMenu() : false;
        if (itemImpl.hasCollapsibleActionView()) {
            invoked |= itemImpl.expandActionView();
            if (invoked) {
                close(true);
            }
        } else if (itemImpl.hasSubMenu() || providerHasSubMenu) {
            close(false);
            boolean hasSubMenu = itemImpl.hasSubMenu();
            SubMenuBuilder subMenu = (SubMenuBuilder) itemImpl.getSubMenu();
            if (providerHasSubMenu) {
                provider.onPrepareSubMenu(subMenu);
            }
            invoked |= dispatchSubMenuSelected(subMenu, preferredPresenter);
            if (!invoked) {
                close(true);
            }
        } else if ((flags & 1) == 0) {
            close(true);
        }
        return invoked;
    }

    public final void close(boolean allMenusAreClosing) {
        if (!this.mIsClosing) {
            this.mIsClosing = true;
            for (WeakReference<ColorMenuPresenter> ref : this.mPresenters) {
                ColorMenuPresenter presenter = (ColorMenuPresenter) ref.get();
                if (presenter == null) {
                    this.mPresenters.remove(ref);
                } else {
                    presenter.onCloseMenu(this, allMenusAreClosing);
                }
            }
            this.mIsClosing = false;
        }
    }

    public void close() {
        close(true);
    }

    public void onItemsChanged(boolean structureChanged) {
        if (this.mPreventDispatchingItemsChanged) {
            this.mItemsChangedWhileDispatchPrevented = true;
            return;
        }
        if (structureChanged) {
            this.mIsVisibleItemsStale = true;
            this.mIsActionItemsStale = true;
        }
        dispatchPresenterUpdate(structureChanged);
    }

    public void stopDispatchingItemsChanged() {
        if (!this.mPreventDispatchingItemsChanged) {
            this.mPreventDispatchingItemsChanged = true;
            this.mItemsChangedWhileDispatchPrevented = false;
        }
    }

    public void startDispatchingItemsChanged() {
        this.mPreventDispatchingItemsChanged = false;
        if (this.mItemsChangedWhileDispatchPrevented) {
            this.mItemsChangedWhileDispatchPrevented = false;
            onItemsChanged(true);
        }
    }

    void onItemVisibleChanged(ColorMenuItemImpl item) {
        this.mIsVisibleItemsStale = true;
        onItemsChanged(true);
    }

    void onItemActionRequestChanged(ColorMenuItemImpl item) {
        this.mIsActionItemsStale = true;
        onItemsChanged(true);
    }

    public ArrayList<ColorMenuItemImpl> getVisibleItems() {
        if (!this.mIsVisibleItemsStale) {
            return this.mVisibleItems;
        }
        this.mVisibleItems.clear();
        int itemsSize = this.mItems.size();
        for (int i = 0; i < itemsSize; i++) {
            ColorMenuItemImpl item = (ColorMenuItemImpl) this.mItems.get(i);
            if (item.isVisible()) {
                this.mVisibleItems.add(item);
            }
        }
        this.mIsVisibleItemsStale = false;
        this.mIsActionItemsStale = true;
        return this.mVisibleItems;
    }

    public void flagActionItems() {
        ArrayList<ColorMenuItemImpl> visibleItems = getVisibleItems();
        if (this.mIsActionItemsStale) {
            boolean flagged = false;
            for (WeakReference<ColorMenuPresenter> ref : this.mPresenters) {
                ColorMenuPresenter presenter = (ColorMenuPresenter) ref.get();
                if (presenter == null) {
                    this.mPresenters.remove(ref);
                } else {
                    flagged |= presenter.flagActionItems();
                }
            }
            if (flagged) {
                this.mActionItems.clear();
                this.mNonActionItems.clear();
                int itemsSize = visibleItems.size();
                for (int i = 0; i < itemsSize; i++) {
                    ColorMenuItemImpl item = (ColorMenuItemImpl) visibleItems.get(i);
                    if (item.isActionButton()) {
                        this.mActionItems.add(item);
                    } else {
                        this.mNonActionItems.add(item);
                    }
                }
            } else {
                this.mActionItems.clear();
                this.mNonActionItems.clear();
                this.mNonActionItems.addAll(getVisibleItems());
            }
            this.mIsActionItemsStale = false;
        }
    }

    public ArrayList<ColorMenuItemImpl> getActionItems() {
        flagActionItems();
        return this.mActionItems;
    }

    public ArrayList<ColorMenuItemImpl> getNonActionItems() {
        flagActionItems();
        return this.mNonActionItems;
    }

    public void clearHeader() {
        this.mHeaderIcon = null;
        this.mHeaderTitle = null;
        this.mHeaderView = null;
        onItemsChanged(false);
    }

    private void setHeaderInternal(int titleRes, CharSequence title, int iconRes, Drawable icon, View view) {
        Resources r = getResources();
        if (view != null) {
            this.mHeaderView = view;
            this.mHeaderTitle = null;
            this.mHeaderIcon = null;
        } else {
            if (titleRes > 0) {
                this.mHeaderTitle = r.getText(titleRes);
            } else if (title != null) {
                this.mHeaderTitle = title;
            }
            if (iconRes > 0) {
                this.mHeaderIcon = getContext().getDrawable(iconRes);
            } else if (icon != null) {
                this.mHeaderIcon = icon;
            }
            this.mHeaderView = null;
        }
        onItemsChanged(false);
    }

    protected ColorMenuBuilder setHeaderTitleInt(CharSequence title) {
        setHeaderInternal(0, title, 0, null, null);
        return this;
    }

    protected ColorMenuBuilder setHeaderTitleInt(int titleRes) {
        setHeaderInternal(titleRes, null, 0, null, null);
        return this;
    }

    protected ColorMenuBuilder setHeaderIconInt(Drawable icon) {
        setHeaderInternal(0, null, 0, icon, null);
        return this;
    }

    protected ColorMenuBuilder setHeaderIconInt(int iconRes) {
        setHeaderInternal(0, null, iconRes, null, null);
        return this;
    }

    protected ColorMenuBuilder setHeaderViewInt(View view) {
        setHeaderInternal(0, null, 0, null, view);
        return this;
    }

    public CharSequence getHeaderTitle() {
        return this.mHeaderTitle;
    }

    public Drawable getHeaderIcon() {
        return this.mHeaderIcon;
    }

    public View getHeaderView() {
        return this.mHeaderView;
    }

    public ColorMenuBuilder getRootMenu() {
        return this;
    }

    public void setCurrentMenuInfo(ContextMenuInfo menuInfo) {
        this.mCurrentMenuInfo = menuInfo;
    }

    void setOptionalIconsVisible(boolean visible) {
        this.mOptionalIconsVisible = visible;
    }

    boolean getOptionalIconsVisible() {
        return this.mOptionalIconsVisible;
    }

    public boolean expandItemActionView(ColorMenuItemImpl item) {
        if (this.mPresenters.isEmpty()) {
            return false;
        }
        boolean expanded = false;
        stopDispatchingItemsChanged();
        for (WeakReference<ColorMenuPresenter> ref : this.mPresenters) {
            ColorMenuPresenter presenter = (ColorMenuPresenter) ref.get();
            if (presenter == null) {
                this.mPresenters.remove(ref);
            } else {
                expanded = presenter.expandItemActionView(this, item);
                if (expanded) {
                    break;
                }
            }
        }
        startDispatchingItemsChanged();
        if (expanded) {
            this.mExpandedItem = item;
        }
        return expanded;
    }

    public boolean collapseItemActionView(ColorMenuItemImpl item) {
        if (this.mPresenters.isEmpty() || this.mExpandedItem != item) {
            return false;
        }
        boolean collapsed = false;
        stopDispatchingItemsChanged();
        for (WeakReference<ColorMenuPresenter> ref : this.mPresenters) {
            ColorMenuPresenter presenter = (ColorMenuPresenter) ref.get();
            if (presenter == null) {
                this.mPresenters.remove(ref);
            } else {
                collapsed = presenter.collapseItemActionView(this, item);
                if (collapsed) {
                    break;
                }
            }
        }
        startDispatchingItemsChanged();
        if (collapsed) {
            this.mExpandedItem = null;
        }
        return collapsed;
    }

    public ColorMenuItemImpl getExpandedItem() {
        return this.mExpandedItem;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "JianHui.Yu@Plf.SDK, 2015-06-16 : Add for SplitMenu", property = OppoRomType.ROM)
    public List<ColorMenuPresenter> removeMenuPresenters(List<Class<?>> classList) {
        List<ColorMenuPresenter> presenters = new ArrayList();
        for (WeakReference<ColorMenuPresenter> ref : this.mPresenters) {
            ColorMenuPresenter presenter = (ColorMenuPresenter) ref.get();
            for (Class<?> clazz : classList) {
                if (clazz.isInstance(presenter)) {
                    presenters.add(presenter);
                    this.mPresenters.remove(ref);
                }
            }
        }
        return presenters;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "JianHui.Yu@Plf.SDK, 2015-07-10 : Add for SplitMenu", property = OppoRomType.ROM)
    public void restoreMenuPresenters(List<ColorMenuPresenter> presenters) {
        for (ColorMenuPresenter presenter : presenters) {
            this.mPresenters.add(new WeakReference(presenter));
        }
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "JianHui.Yu@Plf.SDK, 2015-07-08 : Add for SplitMenu", property = OppoRomType.ROM)
    public void startDispatchingItemsChanged(boolean structureChanged) {
        this.mPreventDispatchingItemsChanged = false;
        if (this.mItemsChangedWhileDispatchPrevented) {
            this.mItemsChangedWhileDispatchPrevented = false;
            onItemsChanged(structureChanged);
        }
    }
}
