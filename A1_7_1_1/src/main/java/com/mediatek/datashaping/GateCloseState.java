package com.mediatek.datashaping;

import android.content.Context;
import android.content.Intent;
import android.util.Slog;

public class GateCloseState extends DataShapingState {
    private static final String TAG = "GateCloseState";

    public GateCloseState(DataShapingServiceImpl dataShapingServiceImpl, Context context) {
        super(dataShapingServiceImpl, context);
    }

    public void onLteAccessStratumStateChanged(Intent intent) {
        if (this.mDataShapingUtils.isLteAccessStratumConnected(intent)) {
            turnStateFromCloseToOpen();
        }
    }

    public void onMediaButtonTrigger() {
        Slog.d(TAG, "[onMediaButtonTrigger]");
        turnStateFromCloseToOpen();
    }

    public void onAlarmManagerTrigger() {
        turnStateFromCloseToOpen();
    }

    public void onCloseTimeExpired() {
        turnStateFromCloseToOpen();
    }

    public void onNetworkTypeChanged(Intent intent) {
        if (!this.mDataShapingUtils.isNetworkTypeLte(intent)) {
            turnStateFromCloseToOpenLocked();
        }
    }

    public void onSharedDefaultApnStateChanged(Intent intent) {
        if (this.mDataShapingUtils.isSharedDefaultApnEstablished(intent)) {
            turnStateFromCloseToOpenLocked();
        }
    }

    public void onScreenStateChanged(boolean isOn) {
        if (isOn) {
            turnStateFromCloseToOpenLocked();
        }
    }

    public void onWifiTetherStateChanged(Intent intent) {
        if (this.mDataShapingUtils.isWifiTetheringEnabled(intent)) {
            turnStateFromCloseToOpenLocked();
        }
    }

    public void onUsbConnectionChanged(Intent intent) {
        if (this.mDataShapingUtils.isUsbConnected(intent)) {
            turnStateFromCloseToOpenLocked();
        }
    }

    public void onBTStateChanged(Intent intent) {
        if (this.mDataShapingUtils.isBTStateOn(intent)) {
            turnStateFromCloseToOpenLocked();
        }
    }

    public void onDeviceIdleStateChanged(boolean enabled) {
        Slog.d(TAG, "[onDeviceIdleStateChanged] DeviceIdle enable is =" + enabled);
        if (enabled) {
            turnStateFromCloseToOpenLocked();
        }
    }

    public void onAPPStandbyStateChanged(boolean isParoleOn) {
        Slog.d(TAG, "[onAPPStandbyStateChanged] APPStandby parole state is =" + isParoleOn);
        if (isParoleOn) {
            turnStateFromCloseToOpenLocked();
        }
    }

    public void onInputFilterStateChanged(boolean isInstall) {
        Slog.d(TAG, "[onInputFilterStateChanged] InputFilter install state is =" + isInstall);
        if (!isInstall) {
            turnStateFromCloseToOpen();
        }
    }

    private void turnStateFromCloseToOpenLocked() {
        Slog.d(TAG, "[turnStateFromCloseToOpenLocked]");
        if (this.mDataShapingUtils.setLteUplinkDataTransfer(true, 600000)) {
            this.mDataShapingManager.setCurrentState(1);
        } else {
            Slog.d(TAG, "[turnStateFromCloseToOpenLocked] fail!");
        }
        cancelCloseTimer();
    }

    private void turnStateFromCloseToOpen() {
        Slog.d(TAG, "[turnStateFromCloseToOpen]");
        if (this.mDataShapingUtils.setLteUplinkDataTransfer(true, 600000)) {
            this.mDataShapingManager.setCurrentState(2);
        } else {
            Slog.d(TAG, "[turnStateFromCloseToOpen] fail!");
        }
        cancelCloseTimer();
    }

    private void cancelCloseTimer() {
        this.mDataShapingManager.cancelCloseExpiredAlarm();
    }
}
