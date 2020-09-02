package com.android.server;

import com.android.server.am.IColorActivityManagerServiceEx;
import com.android.server.display.IColorDisplayManagerServiceEx;
import com.android.server.job.IColorJobSchedulerServiceEx;
import com.android.server.net.IColorNetworkPolicyManagerServiceEx;
import com.android.server.om.IColorOverlayManagerServiceEx;
import com.android.server.pm.IColorPackageManagerServiceEx;
import com.android.server.power.IColorPowerManagerServiceEx;
import com.android.server.wm.IColorActivityTaskManagerServiceEx;
import com.android.server.wm.IColorWindowManagerServiceEx;

public abstract class ColorBaseServiceRegistry extends OppoServiceBootPhase {
    public static int mCurrentPhase = 0;
    protected IColorAlarmManagerServiceEx mColorAlarmEx;
    protected IColorActivityManagerServiceEx mColorAmsEx;
    protected IColorActivityTaskManagerServiceEx mColorAtmsEx;
    protected IColorDeviceIdleControllerEx mColorDeviceIdleEx;
    protected IColorDisplayManagerServiceEx mColorDmsEx;
    protected IColorJobSchedulerServiceEx mColorJobEx;
    protected IColorNetworkPolicyManagerServiceEx mColorNetworkPolicyEx;
    protected IColorOverlayManagerServiceEx mColorOmsEx;
    protected IColorPackageManagerServiceEx mColorPmsEx;
    protected IColorPowerManagerServiceEx mColorPowerEx;
    protected IColorWindowManagerServiceEx mColorWmsEx;

    /* access modifiers changed from: protected */
    public final void handleAtmsInit(IOppoCommonManagerServiceEx ex) {
        if (ex != null && (ex instanceof IColorActivityTaskManagerServiceEx)) {
            this.mColorAtmsEx = (IColorActivityTaskManagerServiceEx) ex;
            onAtmsInit();
        }
    }

    /* access modifiers changed from: protected */
    public final void handleAmsInit(IOppoCommonManagerServiceEx ex) {
        if (ex != null && (ex instanceof IColorActivityManagerServiceEx)) {
            this.mColorAmsEx = (IColorActivityManagerServiceEx) ex;
            onAmsInit();
        }
    }

    /* access modifiers changed from: protected */
    public final void handlePowerInit(IOppoCommonManagerServiceEx ex) {
        if (ex != null && (ex instanceof IColorPowerManagerServiceEx)) {
            this.mColorPowerEx = (IColorPowerManagerServiceEx) ex;
            onPowerInit();
        }
    }

    /* access modifiers changed from: protected */
    public final void handlePmsInit(IOppoCommonManagerServiceEx ex) {
        if (ex != null && (ex instanceof IColorPackageManagerServiceEx)) {
            this.mColorPmsEx = (IColorPackageManagerServiceEx) ex;
            onPmsInit();
        }
    }

    /* access modifiers changed from: protected */
    public final void handleWmsInit(IOppoCommonManagerServiceEx ex) {
        if (ex != null && (ex instanceof IColorWindowManagerServiceEx)) {
            this.mColorWmsEx = (IColorWindowManagerServiceEx) ex;
            onWmsInit();
        }
    }

    /* access modifiers changed from: protected */
    public final void handleAlarmInit(IOppoCommonManagerServiceEx ex) {
        if (ex != null && (ex instanceof IColorAlarmManagerServiceEx)) {
            this.mColorAlarmEx = (IColorAlarmManagerServiceEx) ex;
            onAlarmInit();
        }
    }

    /* access modifiers changed from: protected */
    public final void handleDeviceIdleInit(IOppoCommonManagerServiceEx ex) {
        if (ex != null && (ex instanceof IColorDeviceIdleControllerEx)) {
            this.mColorDeviceIdleEx = (IColorDeviceIdleControllerEx) ex;
            onDeviceIdleInit();
        }
    }

    /* access modifiers changed from: protected */
    public final void handleJobInit(IOppoCommonManagerServiceEx ex) {
        if (ex != null && (ex instanceof IColorJobSchedulerServiceEx)) {
            this.mColorJobEx = (IColorJobSchedulerServiceEx) ex;
            onJobInit();
        }
    }

    /* access modifiers changed from: protected */
    public final void handleOmsInit(IOppoCommonManagerServiceEx ex) {
        if (ex != null && (ex instanceof IColorOverlayManagerServiceEx)) {
            this.mColorOmsEx = (IColorOverlayManagerServiceEx) ex;
            onOmsInit();
        }
    }

    /* access modifiers changed from: protected */
    public final void handleNetworkPolicyInit(IOppoCommonManagerServiceEx ex) {
        if (ex != null && (ex instanceof IColorNetworkPolicyManagerServiceEx)) {
            this.mColorNetworkPolicyEx = (IColorNetworkPolicyManagerServiceEx) ex;
            onNetworkPolicyInit();
        }
    }

    /* access modifiers changed from: protected */
    public final void handleDisplayManagerInit(IOppoCommonManagerServiceEx ex) {
        if (ex != null && (ex instanceof IColorDisplayManagerServiceEx)) {
            this.mColorDmsEx = (IColorDisplayManagerServiceEx) ex;
            onDmsInit();
        }
    }
}
