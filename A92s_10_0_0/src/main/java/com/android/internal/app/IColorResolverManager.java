package com.android.internal.app;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.widget.AbsListView;
import android.widget.TextView;
import com.android.internal.app.ChooserActivity;
import com.android.internal.app.ResolverActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface IColorResolverManager extends IOppoCommonFeature {
    public static final IColorResolverManager DEFAULT = new IColorResolverManager() {
        /* class com.android.internal.app.IColorResolverManager.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default IColorResolverManager getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorResolverManager;
    }

    default boolean isLoadTheme() {
        return true;
    }

    default boolean isMultiApp(ResolverActivity.DisplayResolveInfo dri, ResolverActivity.DisplayResolveInfo existingInfo) {
        return true;
    }

    default boolean isOriginUi() {
        return true;
    }

    default void onCreate(ColorBaseResolverActivity activity) {
    }

    default void onResume() {
    }

    default void onPause() {
    }

    default void onDestroy() {
    }

    default void initActionSend() {
    }

    default boolean isOneAppFinish(int count, ResolverActivity.DisplayResolveInfo resolveInfo) {
        return count == 1 && resolveInfo == null;
    }

    default void fixIntents(int launchedFromUid, Intent intent, ArrayList<Intent> arrayList, Intent[] initialIntents) {
    }

    default void fixInfo(Intent intent, ResolverActivity.DisplayResolveInfo displayResolveInfo) {
    }

    default int fixUserId(int userId, Intent resolvedIntent, int launchedFromUid) {
        return userId;
    }

    default void setLastChosen(ResolverListController controller, Intent intent, IntentFilter filter, int bestMatch) throws RemoteException {
        controller.setLastChosen(intent, filter, bestMatch);
    }

    default void initView(AbsListView adapterView, int count, List<ResolverActivity.ResolvedComponentInfo> list, boolean safeForwardingMode, boolean supportsAlwaysUseOption, ColorBaseResolverActivity mBaseResolverActivity) {
        if (count == 0 && mBaseResolverActivity.getResolverAdapter().getPlaceholderCount() == 0) {
            ((TextView) mBaseResolverActivity.findViewById(16908292)).setVisibility(0);
            adapterView.setVisibility(8);
            return;
        }
        adapterView.setVisibility(0);
        mBaseResolverActivity.onPrepareAdapterView(adapterView, mBaseResolverActivity.getResolverAdapter());
    }

    default void updateView(List<ResolverActivity.ResolvedComponentInfo> list, boolean safeForwardingMode, boolean supportsAlwaysUseOption) {
    }

    default ResolverActivity.ResolveListAdapter createAdapter(ChooserActivity chooserActivity, List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid, boolean filterLastUsed, ResolverListController controller) {
        Objects.requireNonNull(chooserActivity);
        return new ChooserActivity.ChooserListAdapter(chooserActivity, payloadIntents, initialIntents, rList, launchedFromUid, filterLastUsed, controller);
    }

    default void onMultiWindowModeChanged() {
    }

    default void setResolverContent() {
    }

    default void initPreferenceAndPinList() {
    }

    default void statisticsData(ResolveInfo ri, int which) {
    }

    default ResolveInfo getResolveInfo(Intent ii, ColorBaseResolverActivity mBaseResolverActivity) {
        ActivityInfo ai = ii.resolveActivityInfo(mBaseResolverActivity.getPackageManager(), 0);
        if (ai == null) {
            return null;
        }
        ResolveInfo ri = new ResolveInfo();
        ri.activityInfo = ai;
        return ri;
    }
}
