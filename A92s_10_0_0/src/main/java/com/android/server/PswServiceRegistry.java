package com.android.server;

import android.util.Slog;

public class PswServiceRegistry extends PswBaseServiceRegistry {
    private static PswServiceRegistry sInstance;

    public static PswServiceRegistry getInstance() {
        if (sInstance == null) {
            synchronized (PswServiceRegistry.class) {
                if (sInstance == null) {
                    sInstance = new PswServiceRegistry();
                }
            }
        }
        return sInstance;
    }

    /* access modifiers changed from: protected */
    public void onAtmsInit() {
        Slog.i(this.TAG, "onAtmsInit");
    }

    /* access modifiers changed from: protected */
    public void onAmsInit() {
        Slog.i(this.TAG, "onAmsInit");
    }

    /* access modifiers changed from: protected */
    public void onPowerInit() {
        Slog.i(this.TAG, "onPowerInit");
    }

    /* access modifiers changed from: protected */
    public void onPmsInit() {
        Slog.i(this.TAG, "onPmsInit");
    }

    /* access modifiers changed from: protected */
    public void onWmsInit() {
        Slog.i(this.TAG, "onWmsInit");
    }

    /* access modifiers changed from: protected */
    public void onJobInit() {
        Slog.i(this.TAG, "onJobInit");
    }

    /* access modifiers changed from: protected */
    public void onAlarmInit() {
        Slog.i(this.TAG, "onAlarmInit");
    }

    /* access modifiers changed from: protected */
    public void onDeviceIdleInit() {
        Slog.i(this.TAG, "onDeviceIdleInit");
    }

    /* access modifiers changed from: protected */
    public void onNetworkPolicyInit() {
        Slog.i(this.TAG, "onNetworkPolicyInit");
    }

    /* access modifiers changed from: protected */
    public void onDmsInit() {
        Slog.i(this.TAG, "onDmsInit");
    }

    /* access modifiers changed from: protected */
    public void onOmsInit() {
        Slog.i(this.TAG, "onOmsInit");
    }

    /* access modifiers changed from: protected */
    public void onAtmsReady() {
        Slog.i(this.TAG, "onAtmsReady");
    }

    /* access modifiers changed from: protected */
    public void onAmsReady() {
        Slog.i(this.TAG, "onAmsReady");
    }

    /* access modifiers changed from: protected */
    public void onPowerReady() {
        Slog.i(this.TAG, "onPowerReady");
    }

    /* access modifiers changed from: protected */
    public void onPmsReady() {
        Slog.i(this.TAG, "onPmsReady");
    }

    /* access modifiers changed from: protected */
    public void onWmsReady() {
        Slog.i(this.TAG, "onWmsReady");
    }

    /* access modifiers changed from: protected */
    public void onAlarmReady() {
        Slog.i(this.TAG, "onAlarmReady");
    }

    /* access modifiers changed from: protected */
    public void onDeviceIdleReady() {
        Slog.i(this.TAG, "onDeviceIdleReady");
    }

    /* access modifiers changed from: protected */
    public void onJobReady() {
        Slog.i(this.TAG, "onJobReady");
    }

    /* access modifiers changed from: protected */
    public void onOmsReady() {
        Slog.i(this.TAG, "onOmsReady");
    }

    /* access modifiers changed from: protected */
    public void onNetworkPolicyReady() {
        Slog.i(this.TAG, "onNetworkPolicyReady");
    }
}
