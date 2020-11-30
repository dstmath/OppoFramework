package com.android.internal.app;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.TextView;
import com.android.internal.app.ChooserActivity;
import com.android.internal.app.ResolverActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ColorDummyResolverManager implements IColorResolverManager {
    private static final String TAG = "ColorDummyResolverManager";

    @Override // com.android.internal.app.IColorResolverManager
    public void onCreate(ColorBaseResolverActivity activity) {
    }

    @Override // com.android.internal.app.IColorResolverManager
    public void onResume() {
    }

    @Override // com.android.internal.app.IColorResolverManager
    public void onPause() {
    }

    @Override // com.android.internal.app.IColorResolverManager
    public void onDestroy() {
    }

    @Override // com.android.internal.app.IColorResolverManager
    public void initActionSend() {
    }

    @Override // com.android.internal.app.IColorResolverManager
    public boolean isOneAppFinish(int count, ResolverActivity.DisplayResolveInfo resolveInfo) {
        return count == 1 && resolveInfo == null;
    }

    @Override // com.android.internal.app.IColorResolverManager
    public void fixIntents(int launchedFromUid, Intent intent, ArrayList<Intent> arrayList, Intent[] initialIntents) {
    }

    @Override // com.android.internal.app.IColorResolverManager
    public void fixInfo(Intent intent, ResolverActivity.DisplayResolveInfo displayResolveInfo) {
    }

    @Override // com.android.internal.app.IColorResolverManager
    public int fixUserId(int userId, Intent resolvedIntent, int launchedFromUid) {
        return userId;
    }

    @Override // com.android.internal.app.IColorResolverManager
    public void setLastChosen(ResolverListController controller, Intent intent, IntentFilter filter, int bestMatch) throws RemoteException {
        controller.setLastChosen(intent, filter, bestMatch);
    }

    @Override // com.android.internal.app.IColorResolverManager
    public void initView(AbsListView adapterView, int count, List<ResolverActivity.ResolvedComponentInfo> list, boolean safeForwardingMode, boolean supportsAlwaysUseOption, ColorBaseResolverActivity mBaseResolverActivity) {
        if (count == 0 && mBaseResolverActivity.getResolverAdapter().getPlaceholderCount() == 0) {
            ((TextView) mBaseResolverActivity.findViewById(16908292)).setVisibility(0);
            adapterView.setVisibility(8);
            return;
        }
        adapterView.setVisibility(0);
        mBaseResolverActivity.onPrepareAdapterView(adapterView, mBaseResolverActivity.getResolverAdapter());
    }

    @Override // com.android.internal.app.IColorResolverManager
    public void updateView(List<ResolverActivity.ResolvedComponentInfo> list, boolean safeForwardingMode, boolean supportsAlwaysUseOption) {
    }

    @Override // com.android.internal.app.IColorResolverManager
    public ResolverActivity.ResolveListAdapter createAdapter(ChooserActivity chooserActivity, List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid, boolean filterLastUsed, ResolverListController controller) {
        Objects.requireNonNull(chooserActivity);
        return new ChooserActivity.ChooserListAdapter(chooserActivity, payloadIntents, initialIntents, rList, launchedFromUid, filterLastUsed, controller);
    }

    @Override // com.android.internal.app.IColorResolverManager
    public void onMultiWindowModeChanged() {
    }

    @Override // com.android.internal.app.IColorResolverManager
    public void setResolverContent() {
    }

    @Override // com.android.internal.app.IColorResolverManager
    public void initPreferenceAndPinList() {
    }

    @Override // com.android.internal.app.IColorResolverManager
    public void statisticsData(ResolveInfo ri, int which) {
    }

    @Override // com.android.internal.app.IColorResolverManager
    public ResolveInfo getResolveInfo(Intent ii, ColorBaseResolverActivity mBaseResolverActivity) {
        ActivityInfo ai = ii.resolveActivityInfo(mBaseResolverActivity.getPackageManager(), 0);
        if (ai == null) {
            Log.w(TAG, "No activity found for " + ii);
            return null;
        }
        ResolveInfo ri = new ResolveInfo();
        ri.activityInfo = ai;
        return ri;
    }
}
