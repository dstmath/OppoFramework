package com.android.internal.app;

import android.app.Activity;
import android.common.ColorFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import com.android.internal.app.ResolverActivity;
import com.android.internal.policy.DecorView;
import java.util.ArrayList;
import java.util.List;

public abstract class ColorBaseResolverActivity extends Activity {
    protected IColorResolverManager iColorResolverManager = ((IColorResolverManager) ColorFrameworkFactory.getInstance().getFeature(IColorResolverManager.DEFAULT, new Object[0]));

    /* access modifiers changed from: protected */
    public abstract String getReferrerPackageName();

    /* access modifiers changed from: protected */
    public abstract ResolverActivity.ResolveListAdapter getResolverAdapter();

    /* access modifiers changed from: protected */
    public abstract Intent getTargetIntent();

    /* access modifiers changed from: protected */
    public abstract void onPrepareAdapterView(AbsListView absListView, ResolverActivity.ResolveListAdapter resolveListAdapter);

    /* access modifiers changed from: protected */
    public abstract void startSelected(int i, boolean z, boolean z2);

    @Override // android.app.Activity
    public void finish() {
        super.finish();
        if (!isOriginUi()) {
            overridePendingTransition(201982984, 201982985);
        }
    }

    @Override // android.app.Activity
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig);
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            iColorResolverManager2.onMultiWindowModeChanged();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            iColorResolverManager2.onCreate(this);
        }
        super.onCreate(savedInstanceState);
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            iColorResolverManager2.onResume();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onPause() {
        super.onPause();
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            iColorResolverManager2.onPause();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            iColorResolverManager2.onDestroy();
        }
    }

    @Override // android.app.Activity
    public boolean onTouchEvent(MotionEvent event) {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 == null || iColorResolverManager2.isOriginUi() || event.getAction() != 1 || !isOutOfBounds(this, event)) {
            return super.onTouchEvent(event);
        }
        finish();
        return true;
    }

    private boolean isOutOfBounds(Context context, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int slop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
        View parentPanel = findViewById(201458852);
        if (parentPanel != null) {
            int left = getLeft(parentPanel, 0);
            if (x >= (left + parentPanel.getPaddingLeft()) - slop && x <= ((parentPanel.getWidth() + left) - parentPanel.getPaddingRight()) + slop) {
                int top = getTop(parentPanel, 0);
                if (y < (top + parentPanel.getPaddingTop()) - slop || y > ((parentPanel.getHeight() + top) - parentPanel.getPaddingBottom()) + slop) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    private int getLeft(View parent, int result) {
        if (parent == null || (parent instanceof DecorView)) {
            return 0;
        }
        if (parent.getParent() instanceof View) {
            return result + getLeft((View) parent.getParent(), parent.getLeft());
        }
        return result;
    }

    private int getTop(View parent, int result) {
        if (parent == null || (parent instanceof DecorView)) {
            return 0;
        }
        if (parent.getParent() instanceof View) {
            return result + getTop((View) parent.getParent(), parent.getTop());
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public void setOriginTheme(int resId) {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 == null || iColorResolverManager2.isLoadTheme()) {
            super.setTheme(resId);
        }
    }

    /* access modifiers changed from: protected */
    public void setOriginContentView(int layoutId) {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 == null || iColorResolverManager2.isOriginUi()) {
            super.setContentView(layoutId);
        }
    }

    /* access modifiers changed from: protected */
    public void fixIntents(int launchedFromUid, Intent intent, ArrayList<Intent> intents, Intent[] initialIntents) {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            iColorResolverManager2.fixIntents(launchedFromUid, intent, intents, initialIntents);
        }
    }

    /* access modifiers changed from: protected */
    public void fixInfo(Intent intent, ResolverActivity.DisplayResolveInfo displayResolveInfo) {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            iColorResolverManager2.fixInfo(intent, displayResolveInfo);
        }
    }

    /* access modifiers changed from: protected */
    public int fixUserId(int userId, Intent resolvedIntent, int launchedFromUid) {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            return iColorResolverManager2.fixUserId(userId, resolvedIntent, launchedFromUid);
        }
        return userId;
    }

    /* access modifiers changed from: protected */
    public boolean isMultiApp(ResolverActivity.DisplayResolveInfo dri, ResolverActivity.DisplayResolveInfo existingInfo) {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            return iColorResolverManager2.isMultiApp(dri, existingInfo);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isOriginUi() {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            return iColorResolverManager2.isOriginUi();
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isOneAppFinish(int count, ResolverActivity.DisplayResolveInfo resolveInfo) {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            return iColorResolverManager2.isOneAppFinish(count, resolveInfo);
        }
        return count == 1 && resolveInfo == null;
    }

    /* access modifiers changed from: protected */
    public void setLastChosen(ResolverListController controller, Intent intent, IntentFilter filter, int bestMatch) throws RemoteException {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            iColorResolverManager2.setLastChosen(controller, intent, filter, bestMatch);
        }
    }

    /* access modifiers changed from: protected */
    public void initView(AbsListView adapterView, int count, List<ResolverActivity.ResolvedComponentInfo> placeholderResolveList, boolean safeForwardingMode, boolean supportsAlwaysUseOption) {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            iColorResolverManager2.initView(adapterView, count, placeholderResolveList, safeForwardingMode, supportsAlwaysUseOption, this);
        }
    }

    /* access modifiers changed from: protected */
    public void updateView(List<ResolverActivity.ResolvedComponentInfo> placeholderResolveList, boolean safeForwardingMode, boolean supportsAlwaysUseOption) {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            iColorResolverManager2.updateView(placeholderResolveList, safeForwardingMode, supportsAlwaysUseOption);
        }
    }

    /* access modifiers changed from: protected */
    public void initActionSend() {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            iColorResolverManager2.initActionSend();
        }
    }

    /* access modifiers changed from: protected */
    public void statisticsData(ResolveInfo ri, int which) {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            iColorResolverManager2.statisticsData(ri, which);
        }
    }

    public void setResolverContent() {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            iColorResolverManager2.setResolverContent();
        }
    }

    /* access modifiers changed from: protected */
    public void initPreferenceAndPinList() {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            iColorResolverManager2.initPreferenceAndPinList();
        }
    }

    /* access modifiers changed from: protected */
    public ResolveInfo getResolveInfo(Intent ii) {
        IColorResolverManager iColorResolverManager2 = this.iColorResolverManager;
        if (iColorResolverManager2 != null) {
            return iColorResolverManager2.getResolveInfo(ii, this);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void performAnimation() {
        if (!isOriginUi()) {
            overridePendingTransition(201981964, 201981968);
        }
    }

    /* access modifiers changed from: protected */
    public boolean sortComponentsNull(List sortedComponents, boolean originShow) {
        if (originShow && isOriginUi()) {
            return sortedComponents != null && !sortedComponents.isEmpty();
        }
        if (originShow && !isOriginUi()) {
            return true;
        }
        if ((originShow || !isOriginUi()) && !originShow && !isOriginUi()) {
            return sortedComponents != null && !sortedComponents.isEmpty();
        }
        return true;
    }

    public abstract class ColorBaseResolveListAdapter extends BaseAdapter {
        public ColorBaseResolveListAdapter() {
        }

        public void processSortedListWrapper(List<ResolverActivity.ResolvedComponentInfo> list) {
        }
    }
}
