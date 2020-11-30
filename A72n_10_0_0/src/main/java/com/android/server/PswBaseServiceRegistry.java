package com.android.server;

import com.android.server.am.IPswActivityManagerServiceEx;
import com.android.server.pm.IPswPackageManagerServiceEx;
import com.android.server.power.IPswPowerManagerServiceEx;
import com.android.server.wm.IPswActivityTaskManagerServiceEx;
import com.android.server.wm.IPswWindowManagerServiceEx;

public abstract class PswBaseServiceRegistry extends OppoServiceBootPhase {
    protected IPswActivityManagerServiceEx mPswAmsEx;
    protected IPswActivityTaskManagerServiceEx mPswAtmsEx;
    protected IPswPackageManagerServiceEx mPswPmsEx;
    protected IPswPowerManagerServiceEx mPswPowerEx;
    protected IPswWindowManagerServiceEx mPswWmsEx;

    /* access modifiers changed from: protected */
    public final void handleAtmsInit(IOppoCommonManagerServiceEx ex) {
        if (ex != null && (ex instanceof IPswActivityTaskManagerServiceEx)) {
            this.mPswAtmsEx = (IPswActivityTaskManagerServiceEx) ex;
            onAtmsInit();
        }
    }

    /* access modifiers changed from: protected */
    public final void handleAmsInit(IOppoCommonManagerServiceEx ex) {
        if (ex != null && (ex instanceof IPswActivityManagerServiceEx)) {
            this.mPswAmsEx = (IPswActivityManagerServiceEx) ex;
            onAmsInit();
        }
    }

    /* access modifiers changed from: protected */
    public final void handlePowerInit(IOppoCommonManagerServiceEx ex) {
        if (ex != null && (ex instanceof IPswPowerManagerServiceEx)) {
            this.mPswPowerEx = (IPswPowerManagerServiceEx) ex;
            onPowerInit();
        }
    }

    /* access modifiers changed from: protected */
    public final void handlePmsInit(IOppoCommonManagerServiceEx ex) {
        if (ex != null && (ex instanceof IPswPackageManagerServiceEx)) {
            this.mPswPmsEx = (IPswPackageManagerServiceEx) ex;
            onPmsInit();
        }
    }

    /* access modifiers changed from: protected */
    public final void handleWmsInit(IOppoCommonManagerServiceEx ex) {
        if (ex != null && (ex instanceof IPswWindowManagerServiceEx)) {
            this.mPswWmsEx = (IPswWindowManagerServiceEx) ex;
            onWmsInit();
        }
    }

    /* access modifiers changed from: protected */
    public final void handleAlarmInit(IOppoCommonManagerServiceEx ex) {
    }

    /* access modifiers changed from: protected */
    public final void handleDeviceIdleInit(IOppoCommonManagerServiceEx ex) {
    }

    /* access modifiers changed from: protected */
    public final void handleJobInit(IOppoCommonManagerServiceEx ex) {
    }

    /* access modifiers changed from: protected */
    public final void handleOmsInit(IOppoCommonManagerServiceEx ex) {
    }

    /* access modifiers changed from: protected */
    public final void handleNetworkPolicyInit(IOppoCommonManagerServiceEx ex) {
    }

    /* access modifiers changed from: protected */
    public final void handleDisplayManagerInit(IOppoCommonManagerServiceEx ex) {
    }
}
