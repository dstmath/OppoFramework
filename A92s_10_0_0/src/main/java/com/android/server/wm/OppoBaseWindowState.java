package com.android.server.wm;

import android.common.OppoFeatureCache;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.proto.ProtoOutputStream;
import android.view.SurfaceControl;

public abstract class OppoBaseWindowState extends WindowContainer<WindowState> {
    static final String INPUT_WINDOW = "input";
    IColorWindowStateInner mColorWindowStateInner = null;
    private boolean mDisplayCompat = false;
    private boolean mDisplayHideFullscreenButton = false;
    boolean mIsDockDividerWin;
    boolean mIsLauncherWin;
    boolean mIsOppoPrivacyWin;
    boolean mLastInputmethodShow;
    OppoWindowManagerInternal mOppoWindowManagerInternal;
    private boolean mSystemWindowShow = false;
    WindowManagerInternal mWindowManagerInternal;

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

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public /* bridge */ /* synthetic */ void writeToProto(ProtoOutputStream protoOutputStream, long j, int i) {
        super.writeToProto(protoOutputStream, j, i);
    }

    OppoBaseWindowState(WindowManagerService service) {
        super(service);
    }

    /* access modifiers changed from: protected */
    public void adjustFrameForSpecialType(Rect decorFrame, Rect overscanFrame, Rect currentFrame) {
    }

    public boolean isDisplayCompat() {
        return this.mDisplayCompat;
    }

    public boolean isDisplayCompat(String packageName, int uid) {
        return OppoFeatureCache.get(IOppoScreenModeManagerFeature.DEFAULT).isDisplayCompat(packageName, uid);
    }

    public void setDisplayCompat(boolean displayCompat) {
        this.mDisplayCompat = displayCompat;
    }

    public boolean isDisplayHideFullscreenButtonNeeded() {
        return this.mDisplayHideFullscreenButton;
    }

    public void setmDisplayHideFullscreenButton(boolean displayHideFullscreenButton) {
        this.mDisplayHideFullscreenButton = displayHideFullscreenButton;
    }

    public void setSystemWindowStatus(boolean systemWindowShow) {
        this.mSystemWindowShow = systemWindowShow;
    }

    public boolean isSystemWindowStatus() {
        return this.mSystemWindowShow;
    }
}
