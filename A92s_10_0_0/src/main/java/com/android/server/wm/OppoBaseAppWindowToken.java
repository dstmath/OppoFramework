package com.android.server.wm;

import android.content.res.Configuration;
import android.os.IBinder;
import android.util.proto.ProtoOutputStream;
import android.view.SurfaceControl;

public class OppoBaseAppWindowToken extends WindowToken {
    String mActivityName = "";
    int mFixedRate = 0;
    String mPackageName = "";
    int mRefreshRateSpec = 0;
    int mTmpRefreshRateSpec = 0;

    @Override // com.android.server.wm.SurfaceAnimator.Animatable, com.android.server.wm.WindowContainer
    public /* bridge */ /* synthetic */ void commitPendingTransaction() {
        super.commitPendingTransaction();
    }

    @Override // com.android.server.wm.WindowContainer
    public /* bridge */ /* synthetic */ int compareTo(WindowContainer windowContainer) {
        return super.compareTo(windowContainer);
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable, com.android.server.wm.WindowContainer
    public /* bridge */ /* synthetic */ SurfaceControl getAnimationLeashParent() {
        return super.getAnimationLeashParent();
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable, com.android.server.wm.WindowContainer
    public /* bridge */ /* synthetic */ SurfaceControl getParentSurfaceControl() {
        return super.getParentSurfaceControl();
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable, com.android.server.wm.WindowContainer
    public /* bridge */ /* synthetic */ SurfaceControl.Transaction getPendingTransaction() {
        return super.getPendingTransaction();
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable, com.android.server.wm.WindowContainer
    public /* bridge */ /* synthetic */ SurfaceControl getSurfaceControl() {
        return super.getSurfaceControl();
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable, com.android.server.wm.WindowContainer
    public /* bridge */ /* synthetic */ int getSurfaceHeight() {
        return super.getSurfaceHeight();
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable, com.android.server.wm.WindowContainer
    public /* bridge */ /* synthetic */ int getSurfaceWidth() {
        return super.getSurfaceWidth();
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable, com.android.server.wm.WindowContainer
    public /* bridge */ /* synthetic */ SurfaceControl.Builder makeAnimationLeash() {
        return super.makeAnimationLeash();
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable, com.android.server.wm.WindowContainer
    public /* bridge */ /* synthetic */ void onAnimationLeashCreated(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl) {
        super.onAnimationLeashCreated(transaction, surfaceControl);
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable, com.android.server.wm.WindowContainer
    public /* bridge */ /* synthetic */ void onAnimationLeashLost(SurfaceControl.Transaction transaction) {
        super.onAnimationLeashLost(transaction);
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public /* bridge */ /* synthetic */ void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public /* bridge */ /* synthetic */ void onRequestedOverrideConfigurationChanged(Configuration configuration) {
        super.onRequestedOverrideConfigurationChanged(configuration);
    }

    @Override // com.android.server.wm.WindowToken
    public /* bridge */ /* synthetic */ String toString() {
        return super.toString();
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.WindowToken, com.android.server.wm.ConfigurationContainer
    public /* bridge */ /* synthetic */ void writeToProto(ProtoOutputStream protoOutputStream, long j, int i) {
        super.writeToProto(protoOutputStream, j, i);
    }

    OppoBaseAppWindowToken(WindowManagerService service, IBinder _token, int type, boolean persistOnEmpty, DisplayContent dc, boolean ownerCanManageAppTokens) {
        super(service, _token, type, persistOnEmpty, dc, ownerCanManageAppTokens);
    }

    /* access modifiers changed from: package-private */
    public void initScreenMode(String pkg, String activity) {
        if (pkg != null) {
            this.mPackageName = pkg;
        }
        if (activity != null) {
            this.mActivityName = activity;
        }
    }
}
