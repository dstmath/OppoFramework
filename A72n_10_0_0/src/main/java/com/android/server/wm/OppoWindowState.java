package com.android.server.wm;

import android.common.OppoFeatureCache;
import android.graphics.Rect;
import android.view.IWindow;
import android.view.WindowManager;
import com.android.server.LocalServices;
import com.android.server.wm.WindowState;
import com.color.util.ColorTypeCastingHelper;
import com.color.zoomwindow.ColorZoomWindowManager;

public class OppoWindowState extends WindowState {
    private static final String DOCK_DIVIDER = "DockedStackDivider";
    private static final String OPPO_LAUNCHER = "com.oppo.launcher.Launcher";
    private static final String OPPO_PRIVACY = "com.coloros.safecenter.privacy.view.password";
    private static final int WIDTH_RANGE = 200;
    private OppoBaseWindowState mOppoBaseWindowState;

    OppoWindowState(WindowManagerService service, Session s, IWindow c, WindowToken token, WindowState parentWindow, int appOp, int seq, WindowManager.LayoutParams a, int viewVisibility, int ownerId, boolean ownerCanAddInternalSystemWindow) {
        super(service, s, c, token, parentWindow, appOp, seq, a, viewVisibility, ownerId, ownerCanAddInternalSystemWindow);
        if (this.mOppoBaseWindowState == null) {
            this.mOppoBaseWindowState = (OppoBaseWindowState) ColorTypeCastingHelper.typeCasting(OppoBaseWindowState.class, this);
        }
    }

    OppoWindowState(WindowManagerService service, Session s, IWindow c, WindowToken token, WindowState parentWindow, int appOp, int seq, WindowManager.LayoutParams a, int viewVisibility, int ownerId, boolean ownerCanAddInternalSystemWindow, WindowState.PowerManagerWrapper powerManagerWrapper) {
        super(service, s, c, token, parentWindow, appOp, seq, a, viewVisibility, ownerId, ownerCanAddInternalSystemWindow, powerManagerWrapper);
        if (this.mOppoBaseWindowState == null) {
            this.mOppoBaseWindowState = (OppoBaseWindowState) ColorTypeCastingHelper.typeCasting(OppoBaseWindowState.class, this);
        }
        if (this.mOppoBaseWindowState != null) {
            if (this.mAttrs.getTitle() != null && this.mAttrs.getTitle().toString().contains(OPPO_LAUNCHER)) {
                this.mOppoBaseWindowState.mIsLauncherWin = true;
            } else if (this.mAttrs.getTitle() != null && this.mAttrs.getTitle().toString().contains(DOCK_DIVIDER)) {
                this.mOppoBaseWindowState.mIsDockDividerWin = true;
            } else if (this.mAttrs.getTitle() != null && this.mAttrs.getTitle().toString().contains(OPPO_PRIVACY)) {
                this.mOppoBaseWindowState.mIsOppoPrivacyWin = true;
            }
            if (this.mOppoBaseWindowState.mWindowManagerInternal == null) {
                this.mOppoBaseWindowState.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.OppoBaseWindowState
    public void adjustFrameForSpecialType(Rect decorFrame, Rect overscanFrame, Rect currentFrame) {
        OppoBaseWindowState oppoBaseWindowState = this.mOppoBaseWindowState;
        if (oppoBaseWindowState == null) {
            return;
        }
        if (oppoBaseWindowState.mIsLauncherWin && this.mOppoBaseWindowState.mWindowManagerInternal != null && this.mOppoBaseWindowState.mWindowManagerInternal.isStackVisibleLw(3)) {
            decorFrame.set(overscanFrame);
        } else if (!this.mOppoBaseWindowState.mIsDockDividerWin) {
        } else {
            if (currentFrame.left < 0) {
                currentFrame.offset(overscanFrame.right - currentFrame.left, 0);
            } else if (currentFrame.top < 0) {
                currentFrame.offset(0, overscanFrame.bottom - currentFrame.top);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.WindowState
    public void removeImmediately() {
        super.removeImmediately();
        OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).removeTapExcluedWindow(this);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowState
    public void attach() {
        super.attach();
        OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).addTapExcluedWindow(this);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowState
    public void prelayout() {
        if (getWindowingMode() != ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
            super.prelayout();
        }
    }
}
