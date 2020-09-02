package com.android.server;

import android.os.SystemProperties;
import android.util.Slog;

public abstract class OppoServiceBootPhase {
    public static final int PHASE_ALARM_INIT = 6;
    public static final int PHASE_ALARM_READY = 26;
    public static final int PHASE_AMS_INIT = 2;
    public static final int PHASE_AMS_READY = 22;
    public static final int PHASE_ATMS_INIT = 1;
    public static final int PHASE_ATMS_READY = 21;
    public static final int PHASE_DEVICEIDLE_INIT = 7;
    public static final int PHASE_DEVICEIDLE_READY = 27;
    public static final int PHASE_DISPLAY_MANAGER_INIT = 11;
    private static final int PHASE_INIT_INDEX = 0;
    public static final int PHASE_JOB_INIT = 8;
    public static final int PHASE_JOB_READY = 28;
    public static final int PHASE_NETWORK_POLICY_INIT = 10;
    public static final int PHASE_NETWORK_POLICY_READY = 30;
    public static final int PHASE_OMS_INIT = 9;
    public static final int PHASE_OMS_READY = 29;
    public static final int PHASE_PMS_INIT = 4;
    public static final int PHASE_PMS_READY = 24;
    public static final int PHASE_POWER_INIT = 3;
    public static final int PHASE_POWER_READY = 23;
    private static final int PHASE_READY_INDEX = 20;
    public static final int PHASE_WMS_INIT = 5;
    public static final int PHASE_WMS_READY = 25;
    public final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public final String TAG = getClass().getSimpleName();
    private Object mLock = new Object();

    /* access modifiers changed from: protected */
    public abstract void handleAlarmInit(IOppoCommonManagerServiceEx iOppoCommonManagerServiceEx);

    /* access modifiers changed from: protected */
    public abstract void handleAmsInit(IOppoCommonManagerServiceEx iOppoCommonManagerServiceEx);

    /* access modifiers changed from: protected */
    public abstract void handleAtmsInit(IOppoCommonManagerServiceEx iOppoCommonManagerServiceEx);

    /* access modifiers changed from: protected */
    public abstract void handleDeviceIdleInit(IOppoCommonManagerServiceEx iOppoCommonManagerServiceEx);

    /* access modifiers changed from: protected */
    public abstract void handleDisplayManagerInit(IOppoCommonManagerServiceEx iOppoCommonManagerServiceEx);

    /* access modifiers changed from: protected */
    public abstract void handleJobInit(IOppoCommonManagerServiceEx iOppoCommonManagerServiceEx);

    /* access modifiers changed from: protected */
    public abstract void handleNetworkPolicyInit(IOppoCommonManagerServiceEx iOppoCommonManagerServiceEx);

    /* access modifiers changed from: protected */
    public abstract void handleOmsInit(IOppoCommonManagerServiceEx iOppoCommonManagerServiceEx);

    /* access modifiers changed from: protected */
    public abstract void handlePmsInit(IOppoCommonManagerServiceEx iOppoCommonManagerServiceEx);

    /* access modifiers changed from: protected */
    public abstract void handlePowerInit(IOppoCommonManagerServiceEx iOppoCommonManagerServiceEx);

    /* access modifiers changed from: protected */
    public abstract void handleWmsInit(IOppoCommonManagerServiceEx iOppoCommonManagerServiceEx);

    /* access modifiers changed from: protected */
    public abstract void onAlarmInit();

    /* access modifiers changed from: protected */
    public abstract void onAlarmReady();

    /* access modifiers changed from: protected */
    public abstract void onAmsInit();

    /* access modifiers changed from: protected */
    public abstract void onAmsReady();

    /* access modifiers changed from: protected */
    public abstract void onAtmsInit();

    /* access modifiers changed from: protected */
    public abstract void onAtmsReady();

    /* access modifiers changed from: protected */
    public abstract void onDeviceIdleInit();

    /* access modifiers changed from: protected */
    public abstract void onDeviceIdleReady();

    /* access modifiers changed from: protected */
    public abstract void onDmsInit();

    /* access modifiers changed from: protected */
    public abstract void onJobInit();

    /* access modifiers changed from: protected */
    public abstract void onJobReady();

    /* access modifiers changed from: protected */
    public abstract void onNetworkPolicyInit();

    /* access modifiers changed from: protected */
    public abstract void onNetworkPolicyReady();

    /* access modifiers changed from: protected */
    public abstract void onOmsInit();

    /* access modifiers changed from: protected */
    public abstract void onOmsReady();

    /* access modifiers changed from: protected */
    public abstract void onPmsInit();

    /* access modifiers changed from: protected */
    public abstract void onPmsReady();

    /* access modifiers changed from: protected */
    public abstract void onPowerInit();

    /* access modifiers changed from: protected */
    public abstract void onPowerReady();

    /* access modifiers changed from: protected */
    public abstract void onWmsInit();

    /* access modifiers changed from: protected */
    public abstract void onWmsReady();

    public void serviceInit(int phase, IOppoCommonManagerServiceEx ex) {
        synchronized (this.mLock) {
            switch (phase) {
                case 1:
                    handleAtmsInit(ex);
                    break;
                case 2:
                    handleAmsInit(ex);
                    break;
                case 3:
                    handlePowerInit(ex);
                    break;
                case 4:
                    handlePmsInit(ex);
                    break;
                case 5:
                    handleWmsInit(ex);
                    break;
                case 6:
                    handleAlarmInit(ex);
                    break;
                case 7:
                    handleDeviceIdleInit(ex);
                    break;
                case 8:
                    handleJobInit(ex);
                    break;
                case 9:
                    handleOmsInit(ex);
                    break;
                case 10:
                    handleNetworkPolicyInit(ex);
                    break;
                case 11:
                    handleDisplayManagerInit(ex);
                    break;
                default:
                    String str = this.TAG;
                    Slog.w(str, "service init unknow phase = " + phase);
                    break;
            }
        }
    }

    public void serviceReady(int phase) {
        synchronized (this.mLock) {
            switch (phase) {
                case 21:
                    handleAtmsReady();
                    break;
                case 22:
                    handleAmsReady();
                    break;
                case 23:
                    handlePowerReady();
                    break;
                case 24:
                    handlePmsReady();
                    break;
                case 25:
                    handleWmsReady();
                    break;
                case PHASE_ALARM_READY /*{ENCODED_INT: 26}*/:
                    handleAlarmReady();
                    break;
                case 27:
                    handleDeviceIdleReady();
                    break;
                case 28:
                    handleJobReady();
                    break;
                case 29:
                    handleOmsReady();
                    break;
                case 30:
                    handleNetworkPolicyReady();
                    break;
                default:
                    String str = this.TAG;
                    Slog.w(str, "service ready unknow phase = " + phase);
                    break;
            }
        }
    }

    private void handleAtmsReady() {
        onAtmsReady();
    }

    private void handleAmsReady() {
        onAmsReady();
    }

    private void handlePowerReady() {
        onPowerReady();
    }

    private void handlePmsReady() {
        onPmsReady();
    }

    private void handleWmsReady() {
        onWmsReady();
    }

    private void handleAlarmReady() {
        onAlarmReady();
    }

    private void handleDeviceIdleReady() {
        onDeviceIdleReady();
    }

    private void handleJobReady() {
        onJobReady();
    }

    private void handleOmsReady() {
        onOmsReady();
    }

    private void handleNetworkPolicyReady() {
        onNetworkPolicyReady();
    }
}
