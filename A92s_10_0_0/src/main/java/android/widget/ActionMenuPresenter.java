package android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.ActionProvider;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ActionMenuView;
import com.android.internal.R;
import com.android.internal.view.ActionBarPolicy;
import com.android.internal.view.menu.ActionMenuItemView;
import com.android.internal.view.menu.BaseMenuPresenter;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuPopupHelper;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.view.menu.MenuView;
import com.android.internal.view.menu.ShowableListMenu;
import com.android.internal.view.menu.SubMenuBuilder;
import java.util.ArrayList;
import java.util.List;

public class ActionMenuPresenter extends BaseMenuPresenter implements ActionProvider.SubUiVisibilityListener {
    private static final boolean ACTIONBAR_ANIMATIONS_ENABLED = false;
    private static final int ITEM_ANIMATION_DURATION = 150;
    private final SparseBooleanArray mActionButtonGroups = new SparseBooleanArray();
    /* access modifiers changed from: private */
    public ActionButtonSubmenu mActionButtonPopup;
    private int mActionItemWidthLimit;
    private View.OnAttachStateChangeListener mAttachStateChangeListener = new View.OnAttachStateChangeListener() {
        /* class android.widget.ActionMenuPresenter.AnonymousClass2 */

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewAttachedToWindow(View v) {
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewDetachedFromWindow(View v) {
            ((View) ActionMenuPresenter.this.mMenuView).getViewTreeObserver().removeOnPreDrawListener(ActionMenuPresenter.this.mItemAnimationPreDrawListener);
            ActionMenuPresenter.this.mPreLayoutItems.clear();
            ActionMenuPresenter.this.mPostLayoutItems.clear();
        }
    };
    private boolean mExpandedActionViewsExclusive;
    /* access modifiers changed from: private */
    public ViewTreeObserver.OnPreDrawListener mItemAnimationPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        /* class android.widget.ActionMenuPresenter.AnonymousClass1 */

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            ActionMenuPresenter.this.computeMenuItemAnimationInfo(false);
            ((View) ActionMenuPresenter.this.mMenuView).getViewTreeObserver().removeOnPreDrawListener(this);
            ActionMenuPresenter.this.runItemAnimations();
            return true;
        }
    };
    private int mMaxItems;
    private boolean mMaxItemsSet;
    private int mMinCellSize;
    int mOpenSubMenuId;
    /* access modifiers changed from: private */
    public OverflowMenuButton mOverflowButton;
    /* access modifiers changed from: private */
    public OverflowPopup mOverflowPopup;
    private Drawable mPendingOverflowIcon;
    private boolean mPendingOverflowIconSet;
    private ActionMenuPopupCallback mPopupCallback;
    final PopupPresenterCallback mPopupPresenterCallback = new PopupPresenterCallback();
    /* access modifiers changed from: private */
    public SparseArray<MenuItemLayoutInfo> mPostLayoutItems = new SparseArray<>();
    /* access modifiers changed from: private */
    public OpenOverflowRunnable mPostedOpenRunnable;
    /* access modifiers changed from: private */
    public SparseArray<MenuItemLayoutInfo> mPreLayoutItems = new SparseArray<>();
    private boolean mReserveOverflow;
    private boolean mReserveOverflowSet;
    /* access modifiers changed from: private */
    public List<ItemAnimationInfo> mRunningItemAnimations = new ArrayList();
    private boolean mStrictWidthLimit;
    private int mWidthLimit;
    private boolean mWidthLimitSet;

    public ActionMenuPresenter(Context context) {
        super(context, R.layout.action_menu_layout, R.layout.action_menu_item_layout);
    }

    @Override // com.android.internal.view.menu.BaseMenuPresenter, com.android.internal.view.menu.MenuPresenter
    public void initForMenu(Context context, MenuBuilder menu) {
        super.initForMenu(context, menu);
        Resources res = context.getResources();
        ActionBarPolicy abp = ActionBarPolicy.get(context);
        if (!this.mReserveOverflowSet) {
            this.mReserveOverflow = abp.showsOverflowMenuButton();
        }
        if (!this.mWidthLimitSet) {
            this.mWidthLimit = abp.getEmbeddedMenuWidthLimit();
        }
        if (!this.mMaxItemsSet) {
            this.mMaxItems = abp.getMaxActionButtons();
        }
        int width = this.mWidthLimit;
        if (this.mReserveOverflow) {
            if (this.mOverflowButton == null) {
                this.mOverflowButton = new OverflowMenuButton(this.mSystemContext);
                if (this.mPendingOverflowIconSet) {
                    this.mOverflowButton.setImageDrawable(this.mPendingOverflowIcon);
                    this.mPendingOverflowIcon = null;
                    this.mPendingOverflowIconSet = false;
                }
                int spec = View.MeasureSpec.makeMeasureSpec(0, 0);
                this.mOverflowButton.measure(spec, spec);
            }
            width -= this.mOverflowButton.getMeasuredWidth();
        } else {
            this.mOverflowButton = null;
        }
        this.mActionItemWidthLimit = width;
        this.mMinCellSize = (int) (res.getDisplayMetrics().density * 56.0f);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (!this.mMaxItemsSet) {
            this.mMaxItems = ActionBarPolicy.get(this.mContext).getMaxActionButtons();
        }
        if (this.mMenu != null) {
            this.mMenu.onItemsChanged(true);
        }
    }

    public void setWidthLimit(int width, boolean strict) {
        this.mWidthLimit = width;
        this.mStrictWidthLimit = strict;
        this.mWidthLimitSet = true;
    }

    public void setReserveOverflow(boolean reserveOverflow) {
        this.mReserveOverflow = reserveOverflow;
        this.mReserveOverflowSet = true;
    }

    public void setItemLimit(int itemCount) {
        this.mMaxItems = itemCount;
        this.mMaxItemsSet = true;
    }

    public void setExpandedActionViewsExclusive(boolean isExclusive) {
        this.mExpandedActionViewsExclusive = isExclusive;
    }

    public void setOverflowIcon(Drawable icon) {
        OverflowMenuButton overflowMenuButton = this.mOverflowButton;
        if (overflowMenuButton != null) {
            overflowMenuButton.setImageDrawable(icon);
            return;
        }
        this.mPendingOverflowIconSet = true;
        this.mPendingOverflowIcon = icon;
    }

    public Drawable getOverflowIcon() {
        OverflowMenuButton overflowMenuButton = this.mOverflowButton;
        if (overflowMenuButton != null) {
            return overflowMenuButton.getDrawable();
        }
        if (this.mPendingOverflowIconSet) {
            return this.mPendingOverflowIcon;
        }
        return null;
    }

    @Override // com.android.internal.view.menu.BaseMenuPresenter, com.android.internal.view.menu.MenuPresenter
    public MenuView getMenuView(ViewGroup root) {
        MenuView oldMenuView = this.mMenuView;
        MenuView result = super.getMenuView(root);
        if (oldMenuView != result) {
            ((ActionMenuView) result).setPresenter(this);
            if (oldMenuView != null) {
                ((View) oldMenuView).removeOnAttachStateChangeListener(this.mAttachStateChangeListener);
            }
            ((View) result).addOnAttachStateChangeListener(this.mAttachStateChangeListener);
        }
        return result;
    }

    @Override // com.android.internal.view.menu.BaseMenuPresenter
    public View getItemView(MenuItemImpl item, View convertView, ViewGroup parent) {
        View actionView = item.getActionView();
        if (actionView == null || item.hasCollapsibleActionView()) {
            actionView = super.getItemView(item, convertView, parent);
        }
        actionView.setVisibility(item.isActionViewExpanded() ? 8 : 0);
        ActionMenuView menuParent = (ActionMenuView) parent;
        ViewGroup.LayoutParams lp = actionView.getLayoutParams();
        if (!menuParent.checkLayoutParams(lp)) {
            actionView.setLayoutParams(menuParent.generateLayoutParams(lp));
        }
        return actionView;
    }

    @Override // com.android.internal.view.menu.BaseMenuPresenter
    public void bindItemView(MenuItemImpl item, MenuView.ItemView itemView) {
        itemView.initialize(item, 0);
        ActionMenuItemView actionItemView = (ActionMenuItemView) itemView;
        actionItemView.setItemInvoker((ActionMenuView) this.mMenuView);
        if (this.mPopupCallback == null) {
            this.mPopupCallback = new ActionMenuPopupCallback();
        }
        actionItemView.setPopupCallback(this.mPopupCallback);
    }

    @Override // com.android.internal.view.menu.BaseMenuPresenter
    public boolean shouldIncludeItem(int childIndex, MenuItemImpl item) {
        return item.isActionButton();
    }

    /* access modifiers changed from: private */
    public void computeMenuItemAnimationInfo(boolean preLayout) {
        ViewGroup menuView = (ViewGroup) this.mMenuView;
        int count = menuView.getChildCount();
        SparseArray items = preLayout ? this.mPreLayoutItems : this.mPostLayoutItems;
        for (int i = 0; i < count; i++) {
            View child = menuView.getChildAt(i);
            int id = child.getId();
            if (!(id <= 0 || child.getWidth() == 0 || child.getHeight() == 0)) {
                items.put(id, new MenuItemLayoutInfo(child, preLayout));
            }
        }
    }

    /* access modifiers changed from: private */
    public void runItemAnimations() {
        ObjectAnimator anim;
        for (int i = 0; i < this.mPreLayoutItems.size(); i++) {
            int id = this.mPreLayoutItems.keyAt(i);
            final MenuItemLayoutInfo menuItemLayoutInfoPre = this.mPreLayoutItems.get(id);
            int postLayoutIndex = this.mPostLayoutItems.indexOfKey(id);
            if (postLayoutIndex >= 0) {
                MenuItemLayoutInfo menuItemLayoutInfoPost = this.mPostLayoutItems.valueAt(postLayoutIndex);
                PropertyValuesHolder pvhX = null;
                PropertyValuesHolder pvhY = null;
                if (menuItemLayoutInfoPre.left != menuItemLayoutInfoPost.left) {
                    pvhX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, (float) (menuItemLayoutInfoPre.left - menuItemLayoutInfoPost.left), 0.0f);
                }
                if (menuItemLayoutInfoPre.top != menuItemLayoutInfoPost.top) {
                    pvhY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, (float) (menuItemLayoutInfoPre.top - menuItemLayoutInfoPost.top), 0.0f);
                }
                if (!(pvhX == null && pvhY == null)) {
                    for (int j = 0; j < this.mRunningItemAnimations.size(); j++) {
                        ItemAnimationInfo oldInfo = this.mRunningItemAnimations.get(j);
                        if (oldInfo.id == id && oldInfo.animType == 0) {
                            oldInfo.animator.cancel();
                        }
                    }
                    if (pvhX == null) {
                        anim = ObjectAnimator.ofPropertyValuesHolder(menuItemLayoutInfoPost.view, pvhY);
                    } else if (pvhY != null) {
                        anim = ObjectAnimator.ofPropertyValuesHolder(menuItemLayoutInfoPost.view, pvhX, pvhY);
                    } else {
                        anim = ObjectAnimator.ofPropertyValuesHolder(menuItemLayoutInfoPost.view, pvhX);
                    }
                    anim.setDuration(150L);
                    anim.start();
                    this.mRunningItemAnimations.add(new ItemAnimationInfo(id, menuItemLayoutInfoPost, anim, 0));
                    anim.addListener(new AnimatorListenerAdapter() {
                        /* class android.widget.ActionMenuPresenter.AnonymousClass3 */

                        @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                        public void onAnimationEnd(Animator animation) {
                            for (int j = 0; j < ActionMenuPresenter.this.mRunningItemAnimations.size(); j++) {
                                if (((ItemAnimationInfo) ActionMenuPresenter.this.mRunningItemAnimations.get(j)).animator == animation) {
                                    ActionMenuPresenter.this.mRunningItemAnimations.remove(j);
                                    return;
                                }
                            }
                        }
                    });
                }
                this.mPostLayoutItems.remove(id);
            } else {
                float oldAlpha = 1.0f;
                for (int j2 = 0; j2 < this.mRunningItemAnimations.size(); j2++) {
                    ItemAnimationInfo oldInfo2 = this.mRunningItemAnimations.get(j2);
                    if (oldInfo2.id == id && oldInfo2.animType == 1) {
                        oldAlpha = oldInfo2.menuItemLayoutInfo.view.getAlpha();
                        oldInfo2.animator.cancel();
                    }
                }
                ObjectAnimator anim2 = ObjectAnimator.ofFloat(menuItemLayoutInfoPre.view, View.ALPHA, oldAlpha, 0.0f);
                ((ViewGroup) this.mMenuView).getOverlay().add(menuItemLayoutInfoPre.view);
                anim2.setDuration(150L);
                anim2.start();
                this.mRunningItemAnimations.add(new ItemAnimationInfo(id, menuItemLayoutInfoPre, anim2, 2));
                anim2.addListener(new AnimatorListenerAdapter() {
                    /* class android.widget.ActionMenuPresenter.AnonymousClass4 */

                    @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                    public void onAnimationEnd(Animator animation) {
                        int j = 0;
                        while (true) {
                            if (j >= ActionMenuPresenter.this.mRunningItemAnimations.size()) {
                                break;
                            } else if (((ItemAnimationInfo) ActionMenuPresenter.this.mRunningItemAnimations.get(j)).animator == animation) {
                                ActionMenuPresenter.this.mRunningItemAnimations.remove(j);
                                break;
                            } else {
                                j++;
                            }
                        }
                        ((ViewGroup) ActionMenuPresenter.this.mMenuView).getOverlay().remove(menuItemLayoutInfoPre.view);
                    }
                });
            }
        }
        for (int i2 = 0; i2 < this.mPostLayoutItems.size(); i2++) {
            int id2 = this.mPostLayoutItems.keyAt(i2);
            int postLayoutIndex2 = this.mPostLayoutItems.indexOfKey(id2);
            if (postLayoutIndex2 >= 0) {
                MenuItemLayoutInfo menuItemLayoutInfo = this.mPostLayoutItems.valueAt(postLayoutIndex2);
                float oldAlpha2 = 0.0f;
                for (int j3 = 0; j3 < this.mRunningItemAnimations.size(); j3++) {
                    ItemAnimationInfo oldInfo3 = this.mRunningItemAnimations.get(j3);
                    if (oldInfo3.id == id2 && oldInfo3.animType == 2) {
                        oldAlpha2 = oldInfo3.menuItemLayoutInfo.view.getAlpha();
                        oldInfo3.animator.cancel();
                    }
                }
                ObjectAnimator anim3 = ObjectAnimator.ofFloat(menuItemLayoutInfo.view, View.ALPHA, oldAlpha2, 1.0f);
                anim3.start();
                anim3.setDuration(150L);
                this.mRunningItemAnimations.add(new ItemAnimationInfo(id2, menuItemLayoutInfo, anim3, 1));
                anim3.addListener(new AnimatorListenerAdapter() {
                    /* class android.widget.ActionMenuPresenter.AnonymousClass5 */

                    @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                    public void onAnimationEnd(Animator animation) {
                        for (int j = 0; j < ActionMenuPresenter.this.mRunningItemAnimations.size(); j++) {
                            if (((ItemAnimationInfo) ActionMenuPresenter.this.mRunningItemAnimations.get(j)).animator == animation) {
                                ActionMenuPresenter.this.mRunningItemAnimations.remove(j);
                                return;
                            }
                        }
                    }
                });
            }
        }
        this.mPreLayoutItems.clear();
        this.mPostLayoutItems.clear();
    }

    private void setupItemAnimations() {
        computeMenuItemAnimationInfo(true);
        ((View) this.mMenuView).getViewTreeObserver().addOnPreDrawListener(this.mItemAnimationPreDrawListener);
    }

    @Override // com.android.internal.view.menu.BaseMenuPresenter, com.android.internal.view.menu.MenuPresenter
    public void updateMenuView(boolean cleared) {
        ViewGroup viewGroup = (ViewGroup) ((View) this.mMenuView).getParent();
        super.updateMenuView(cleared);
        ((View) this.mMenuView).requestLayout();
        if (this.mMenu != null) {
            ArrayList<MenuItemImpl> actionItems = this.mMenu.getActionItems();
            int count = actionItems.size();
            for (int i = 0; i < count; i++) {
                ActionProvider provider = actionItems.get(i).getActionProvider();
                if (provider != null) {
                    provider.setSubUiVisibilityListener(this);
                }
            }
        }
        ArrayList<MenuItemImpl> nonActionItems = this.mMenu != null ? this.mMenu.getNonActionItems() : null;
        boolean hasOverflow = false;
        if (this.mReserveOverflow && nonActionItems != null) {
            int count2 = nonActionItems.size();
            boolean z = false;
            if (count2 == 1) {
                hasOverflow = !nonActionItems.get(0).isActionViewExpanded();
            } else {
                if (count2 > 0) {
                    z = true;
                }
                hasOverflow = z;
            }
        }
        if (hasOverflow) {
            if (this.mOverflowButton == null) {
                this.mOverflowButton = new OverflowMenuButton(this.mSystemContext);
            }
            ViewGroup parent = (ViewGroup) this.mOverflowButton.getParent();
            if (parent != this.mMenuView) {
                if (parent != null) {
                    parent.removeView(this.mOverflowButton);
                }
                ActionMenuView menuView = (ActionMenuView) this.mMenuView;
                menuView.addView(this.mOverflowButton, menuView.generateOverflowButtonLayoutParams());
            }
        } else {
            OverflowMenuButton overflowMenuButton = this.mOverflowButton;
            if (overflowMenuButton != null && overflowMenuButton.getParent() == this.mMenuView) {
                ((ViewGroup) this.mMenuView).removeView(this.mOverflowButton);
            }
        }
        ((ActionMenuView) this.mMenuView).setOverflowReserved(this.mReserveOverflow);
    }

    @Override // com.android.internal.view.menu.BaseMenuPresenter
    public boolean filterLeftoverView(ViewGroup parent, int childIndex) {
        if (parent.getChildAt(childIndex) == this.mOverflowButton) {
            return false;
        }
        return super.filterLeftoverView(parent, childIndex);
    }

    @Override // com.android.internal.view.menu.BaseMenuPresenter, com.android.internal.view.menu.MenuPresenter
    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        if (!subMenu.hasVisibleItems()) {
            return false;
        }
        SubMenuBuilder topSubMenu = subMenu;
        while (topSubMenu.getParentMenu() != this.mMenu) {
            topSubMenu = (SubMenuBuilder) topSubMenu.getParentMenu();
        }
        View anchor = findViewForItem(topSubMenu.getItem());
        if (anchor == null) {
            return false;
        }
        this.mOpenSubMenuId = subMenu.getItem().getItemId();
        boolean preserveIconSpacing = false;
        int count = subMenu.size();
        int i = 0;
        while (true) {
            if (i >= count) {
                break;
            }
            MenuItem childItem = subMenu.getItem(i);
            if (childItem.isVisible() && childItem.getIcon() != null) {
                preserveIconSpacing = true;
                break;
            }
            i++;
        }
        this.mActionButtonPopup = new ActionButtonSubmenu(this.mContext, subMenu, anchor);
        this.mActionButtonPopup.setForceShowIcon(preserveIconSpacing);
        this.mActionButtonPopup.show();
        super.onSubMenuSelected(subMenu);
        return true;
    }

    private View findViewForItem(MenuItem item) {
        ViewGroup parent = (ViewGroup) this.mMenuView;
        if (parent == null) {
            return null;
        }
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = parent.getChildAt(i);
            if ((child instanceof MenuView.ItemView) && ((MenuView.ItemView) child).getItemData() == item) {
                return child;
            }
        }
        return null;
    }

    public boolean showOverflowMenu() {
        if (!this.mReserveOverflow || isOverflowMenuShowing() || this.mMenu == null || this.mMenuView == null || this.mPostedOpenRunnable != null || this.mMenu.getNonActionItems().isEmpty()) {
            return false;
        }
        this.mPostedOpenRunnable = new OpenOverflowRunnable(new OverflowPopup(this.mContext, this.mMenu, this.mOverflowButton, true));
        ((View) this.mMenuView).post(this.mPostedOpenRunnable);
        super.onSubMenuSelected(null);
        return true;
    }

    public boolean hideOverflowMenu() {
        if (this.mPostedOpenRunnable == null || this.mMenuView == null) {
            MenuPopupHelper popup = this.mOverflowPopup;
            if (popup == null) {
                return false;
            }
            popup.dismiss();
            return true;
        }
        ((View) this.mMenuView).removeCallbacks(this.mPostedOpenRunnable);
        this.mPostedOpenRunnable = null;
        return true;
    }

    @UnsupportedAppUsage
    public boolean dismissPopupMenus() {
        return hideOverflowMenu() | hideSubMenus();
    }

    public boolean hideSubMenus() {
        ActionButtonSubmenu actionButtonSubmenu = this.mActionButtonPopup;
        if (actionButtonSubmenu == null) {
            return false;
        }
        actionButtonSubmenu.dismiss();
        return true;
    }

    @UnsupportedAppUsage
    public boolean isOverflowMenuShowing() {
        OverflowPopup overflowPopup = this.mOverflowPopup;
        return overflowPopup != null && overflowPopup.isShowing();
    }

    public boolean isOverflowMenuShowPending() {
        return this.mPostedOpenRunnable != null || isOverflowMenuShowing();
    }

    public boolean isOverflowReserved() {
        return this.mReserveOverflow;
    }

    @Override // com.android.internal.view.menu.BaseMenuPresenter, com.android.internal.view.menu.MenuPresenter
    public boolean flagActionItems() {
        int itemsSize;
        ArrayList<MenuItemImpl> visibleItems;
        ArrayList<MenuItemImpl> visibleItems2;
        int requiredItems;
        boolean z;
        int maxActions;
        boolean isAction;
        int widthLimit;
        boolean z2;
        ActionMenuPresenter actionMenuPresenter = this;
        if (actionMenuPresenter.mMenu != null) {
            visibleItems = actionMenuPresenter.mMenu.getVisibleItems();
            itemsSize = visibleItems.size();
        } else {
            visibleItems = null;
            itemsSize = 0;
        }
        int maxActions2 = actionMenuPresenter.mMaxItems;
        int widthLimit2 = actionMenuPresenter.mActionItemWidthLimit;
        int querySpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        ViewGroup parent = (ViewGroup) actionMenuPresenter.mMenuView;
        int requiredItems2 = 0;
        int requestedItems = 0;
        int firstActionWidth = 0;
        boolean hasOverflow = false;
        for (int i = 0; i < itemsSize; i++) {
            MenuItemImpl item = visibleItems.get(i);
            if (item.requiresActionButton()) {
                requiredItems2++;
            } else if (item.requestsActionButton()) {
                requestedItems++;
            } else {
                hasOverflow = true;
            }
            if (actionMenuPresenter.mExpandedActionViewsExclusive && item.isActionViewExpanded()) {
                maxActions2 = 0;
            }
        }
        if (actionMenuPresenter.mReserveOverflow && (hasOverflow || requiredItems2 + requestedItems > maxActions2)) {
            maxActions2--;
        }
        int maxActions3 = maxActions2 - requiredItems2;
        SparseBooleanArray seenGroups = actionMenuPresenter.mActionButtonGroups;
        seenGroups.clear();
        int cellSize = 0;
        int cellsRemaining = 0;
        if (actionMenuPresenter.mStrictWidthLimit) {
            int i2 = actionMenuPresenter.mMinCellSize;
            cellsRemaining = widthLimit2 / i2;
            cellSize = i2 + ((widthLimit2 % i2) / cellsRemaining);
        }
        int i3 = 0;
        while (i3 < itemsSize) {
            MenuItemImpl item2 = visibleItems.get(i3);
            if (item2.requiresActionButton()) {
                View v = actionMenuPresenter.getItemView(item2, null, parent);
                requiredItems = requiredItems2;
                if (actionMenuPresenter.mStrictWidthLimit) {
                    cellsRemaining -= ActionMenuView.measureChildForCells(v, cellSize, cellsRemaining, querySpec, 0);
                } else {
                    v.measure(querySpec, querySpec);
                }
                int measuredWidth = v.getMeasuredWidth();
                int widthLimit3 = widthLimit2 - measuredWidth;
                if (firstActionWidth == 0) {
                    firstActionWidth = measuredWidth;
                }
                int groupId = item2.getGroupId();
                if (groupId != 0) {
                    widthLimit = widthLimit3;
                    z2 = true;
                    seenGroups.put(groupId, true);
                } else {
                    widthLimit = widthLimit3;
                    z2 = true;
                }
                item2.setIsActionButton(z2);
                visibleItems2 = visibleItems;
                widthLimit2 = widthLimit;
                z = false;
            } else {
                requiredItems = requiredItems2;
                if (item2.requestsActionButton()) {
                    int groupId2 = item2.getGroupId();
                    boolean inGroup = seenGroups.get(groupId2);
                    boolean isAction2 = (maxActions3 > 0 || inGroup) && widthLimit2 > 0 && (!actionMenuPresenter.mStrictWidthLimit || cellsRemaining > 0);
                    if (isAction2) {
                        View v2 = actionMenuPresenter.getItemView(item2, null, parent);
                        maxActions = maxActions3;
                        if (actionMenuPresenter.mStrictWidthLimit) {
                            int cells = ActionMenuView.measureChildForCells(v2, cellSize, cellsRemaining, querySpec, 0);
                            cellsRemaining -= cells;
                            if (cells == 0) {
                                isAction = false;
                            } else {
                                isAction = isAction2;
                            }
                        } else {
                            v2.measure(querySpec, querySpec);
                            isAction = isAction2;
                        }
                        int measuredWidth2 = v2.getMeasuredWidth();
                        widthLimit2 -= measuredWidth2;
                        if (firstActionWidth == 0) {
                            firstActionWidth = measuredWidth2;
                        }
                        if (actionMenuPresenter.mStrictWidthLimit) {
                            isAction2 = (widthLimit2 >= 0) & isAction;
                        } else {
                            isAction2 = (widthLimit2 + firstActionWidth > 0) & isAction;
                        }
                    } else {
                        maxActions = maxActions3;
                    }
                    if (isAction2 && groupId2 != 0) {
                        seenGroups.put(groupId2, true);
                        visibleItems2 = visibleItems;
                    } else if (inGroup) {
                        seenGroups.put(groupId2, false);
                        int j = 0;
                        while (j < i3) {
                            MenuItemImpl areYouMyGroupie = visibleItems.get(j);
                            if (areYouMyGroupie.getGroupId() == groupId2) {
                                if (areYouMyGroupie.isActionButton()) {
                                    maxActions++;
                                }
                                areYouMyGroupie.setIsActionButton(false);
                            }
                            j++;
                            visibleItems = visibleItems;
                        }
                        visibleItems2 = visibleItems;
                    } else {
                        visibleItems2 = visibleItems;
                    }
                    if (isAction2) {
                        maxActions--;
                    }
                    item2.setIsActionButton(isAction2);
                    maxActions3 = maxActions;
                    z = false;
                } else {
                    visibleItems2 = visibleItems;
                    z = false;
                    item2.setIsActionButton(false);
                }
            }
            i3++;
            requiredItems2 = requiredItems;
            itemsSize = itemsSize;
            visibleItems = visibleItems2;
            actionMenuPresenter = this;
        }
        return true;
    }

    @Override // com.android.internal.view.menu.BaseMenuPresenter, com.android.internal.view.menu.MenuPresenter
    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        dismissPopupMenus();
        super.onCloseMenu(menu, allMenusAreClosing);
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    @UnsupportedAppUsage
    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState();
        state.openSubMenuId = this.mOpenSubMenuId;
        return state;
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    @UnsupportedAppUsage
    public void onRestoreInstanceState(Parcelable state) {
        MenuItem item;
        SavedState saved = (SavedState) state;
        if (saved.openSubMenuId > 0 && (item = this.mMenu.findItem(saved.openSubMenuId)) != null) {
            onSubMenuSelected((SubMenuBuilder) item.getSubMenu());
        }
    }

    @Override // android.view.ActionProvider.SubUiVisibilityListener
    public void onSubUiVisibilityChanged(boolean isVisible) {
        if (isVisible) {
            super.onSubMenuSelected(null);
        } else if (this.mMenu != null) {
            this.mMenu.close(false);
        }
    }

    public void setMenuView(ActionMenuView menuView) {
        if (menuView != this.mMenuView) {
            if (this.mMenuView != null) {
                ((View) this.mMenuView).removeOnAttachStateChangeListener(this.mAttachStateChangeListener);
            }
            this.mMenuView = menuView;
            menuView.initialize(this.mMenu);
            menuView.addOnAttachStateChangeListener(this.mAttachStateChangeListener);
        }
    }

    private static class SavedState implements Parcelable {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class android.widget.ActionMenuPresenter.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public int openSubMenuId;

        SavedState() {
        }

        SavedState(Parcel in) {
            this.openSubMenuId = in.readInt();
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.openSubMenuId);
        }
    }

    /* access modifiers changed from: private */
    public class OverflowMenuButton extends ImageButton implements ActionMenuView.ActionMenuChildView {
        public OverflowMenuButton(Context context) {
            super(context, null, 16843510);
            setClickable(true);
            setFocusable(true);
            setVisibility(0);
            setEnabled(true);
            setOnTouchListener(new ForwardingListener(this, ActionMenuPresenter.this) {
                /* class android.widget.ActionMenuPresenter.OverflowMenuButton.AnonymousClass1 */

                @Override // android.widget.ForwardingListener
                public ShowableListMenu getPopup() {
                    if (ActionMenuPresenter.this.mOverflowPopup == null) {
                        return null;
                    }
                    return ActionMenuPresenter.this.mOverflowPopup.getPopup();
                }

                @Override // android.widget.ForwardingListener
                public boolean onForwardingStarted() {
                    ActionMenuPresenter.this.showOverflowMenu();
                    return true;
                }

                @Override // android.widget.ForwardingListener
                public boolean onForwardingStopped() {
                    if (ActionMenuPresenter.this.mPostedOpenRunnable != null) {
                        return false;
                    }
                    ActionMenuPresenter.this.hideOverflowMenu();
                    return true;
                }
            });
        }

        @Override // android.view.View
        public boolean performClick() {
            if (super.performClick()) {
                return true;
            }
            playSoundEffect(0);
            ActionMenuPresenter.this.showOverflowMenu();
            return true;
        }

        @Override // android.widget.ActionMenuView.ActionMenuChildView
        public boolean needsDividerBefore() {
            return false;
        }

        @Override // android.widget.ActionMenuView.ActionMenuChildView
        public boolean needsDividerAfter() {
            return false;
        }

        @Override // android.view.View
        public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfoInternal(info);
            info.setCanOpenPopup(true);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.ImageView, android.view.View
        public boolean setFrame(int l, int t, int r, int b) {
            boolean changed = super.setFrame(l, t, r, b);
            Drawable d = getDrawable();
            Drawable bg = getBackground();
            if (!(d == null || bg == null)) {
                int width = getWidth();
                int height = getHeight();
                int halfEdge = Math.max(width, height) / 2;
                int offsetX = getPaddingLeft() - getPaddingRight();
                int centerX = (width + offsetX) / 2;
                int centerY = (height + (getPaddingTop() - getPaddingBottom())) / 2;
                bg.setHotspotBounds(centerX - halfEdge, centerY - halfEdge, centerX + halfEdge, centerY + halfEdge);
            }
            return changed;
        }
    }

    /* access modifiers changed from: private */
    public class OverflowPopup extends MenuPopupHelper {
        public OverflowPopup(Context context, MenuBuilder menu, View anchorView, boolean overflowOnly) {
            super(context, menu, anchorView, overflowOnly, 16843844);
            setGravity(Gravity.END);
            setPresenterCallback(ActionMenuPresenter.this.mPopupPresenterCallback);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.view.menu.MenuPopupHelper
        public void onDismiss() {
            if (ActionMenuPresenter.this.mMenu != null) {
                ActionMenuPresenter.this.mMenu.close();
            }
            OverflowPopup unused = ActionMenuPresenter.this.mOverflowPopup = null;
            super.onDismiss();
        }
    }

    /* access modifiers changed from: private */
    public class ActionButtonSubmenu extends MenuPopupHelper {
        public ActionButtonSubmenu(Context context, SubMenuBuilder subMenu, View anchorView) {
            super(context, subMenu, anchorView, false, 16843844);
            if (!((MenuItemImpl) subMenu.getItem()).isActionButton()) {
                setAnchorView(ActionMenuPresenter.this.mOverflowButton == null ? (View) ActionMenuPresenter.this.mMenuView : ActionMenuPresenter.this.mOverflowButton);
            }
            setPresenterCallback(ActionMenuPresenter.this.mPopupPresenterCallback);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.view.menu.MenuPopupHelper
        public void onDismiss() {
            ActionButtonSubmenu unused = ActionMenuPresenter.this.mActionButtonPopup = null;
            ActionMenuPresenter.this.mOpenSubMenuId = 0;
            super.onDismiss();
        }
    }

    private class PopupPresenterCallback implements MenuPresenter.Callback {
        private PopupPresenterCallback() {
        }

        @Override // com.android.internal.view.menu.MenuPresenter.Callback
        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            if (subMenu == null) {
                return false;
            }
            ActionMenuPresenter.this.mOpenSubMenuId = ((SubMenuBuilder) subMenu).getItem().getItemId();
            MenuPresenter.Callback cb = ActionMenuPresenter.this.getCallback();
            if (cb != null) {
                return cb.onOpenSubMenu(subMenu);
            }
            return false;
        }

        @Override // com.android.internal.view.menu.MenuPresenter.Callback
        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
            if (menu instanceof SubMenuBuilder) {
                menu.getRootMenu().close(false);
            }
            MenuPresenter.Callback cb = ActionMenuPresenter.this.getCallback();
            if (cb != null) {
                cb.onCloseMenu(menu, allMenusAreClosing);
            }
        }
    }

    /* access modifiers changed from: private */
    public class OpenOverflowRunnable implements Runnable {
        private OverflowPopup mPopup;

        public OpenOverflowRunnable(OverflowPopup popup) {
            this.mPopup = popup;
        }

        public void run() {
            if (ActionMenuPresenter.this.mMenu != null) {
                ActionMenuPresenter.this.mMenu.changeMenuMode();
            }
            View menuView = (View) ActionMenuPresenter.this.mMenuView;
            if (!(menuView == null || menuView.getWindowToken() == null || !this.mPopup.tryShow())) {
                OverflowPopup unused = ActionMenuPresenter.this.mOverflowPopup = this.mPopup;
            }
            OpenOverflowRunnable unused2 = ActionMenuPresenter.this.mPostedOpenRunnable = null;
        }
    }

    private class ActionMenuPopupCallback extends ActionMenuItemView.PopupCallback {
        private ActionMenuPopupCallback() {
        }

        @Override // com.android.internal.view.menu.ActionMenuItemView.PopupCallback
        public ShowableListMenu getPopup() {
            if (ActionMenuPresenter.this.mActionButtonPopup != null) {
                return ActionMenuPresenter.this.mActionButtonPopup.getPopup();
            }
            return null;
        }
    }

    /* access modifiers changed from: private */
    public static class MenuItemLayoutInfo {
        int left;
        int top;
        View view;

        MenuItemLayoutInfo(View view2, boolean preLayout) {
            this.left = view2.getLeft();
            this.top = view2.getTop();
            if (preLayout) {
                this.left = (int) (((float) this.left) + view2.getTranslationX());
                this.top = (int) (((float) this.top) + view2.getTranslationY());
            }
            this.view = view2;
        }
    }

    /* access modifiers changed from: private */
    public static class ItemAnimationInfo {
        static final int FADE_IN = 1;
        static final int FADE_OUT = 2;
        static final int MOVE = 0;
        int animType;
        Animator animator;
        int id;
        MenuItemLayoutInfo menuItemLayoutInfo;

        ItemAnimationInfo(int id2, MenuItemLayoutInfo info, Animator anim, int animType2) {
            this.id = id2;
            this.menuItemLayoutInfo = info;
            this.animator = anim;
            this.animType = animType2;
        }
    }
}
