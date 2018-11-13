package com.mediatek.datashaping;

import android.content.Context;
import android.content.Intent;
import android.util.Slog;

public class GateOpenState extends DataShapingState {
    private static final String TAG = "GateOpenState";

    public GateOpenState(DataShapingServiceImpl dataShapingServiceImpl, Context context) {
        super(dataShapingServiceImpl, context);
    }

    public void onLteAccessStratumStateChanged(Intent intent) {
        if (!this.mDataShapingUtils.isLteAccessStratumConnected(intent)) {
            turnStateFromOpenToClose();
        }
    }

    public void onNetworkTypeChanged(Intent intent) {
        if (!this.mDataShapingUtils.isNetworkTypeLte(intent)) {
            turnStateFromOpenToOpenLocked();
        }
    }

    public void onSharedDefaultApnStateChanged(Intent intent) {
        if (this.mDataShapingUtils.isSharedDefaultApnEstablished(intent)) {
            turnStateFromOpenToOpenLocked();
        }
    }

    public void onScreenStateChanged(boolean isOn) {
        if (isOn) {
            turnStateFromOpenToOpenLocked();
        }
    }

    public void onWifiTetherStateChanged(Intent intent) {
        if (this.mDataShapingUtils.isWifiTetheringEnabled(intent)) {
            turnStateFromOpenToOpenLocked();
        }
    }

    public void onUsbConnectionChanged(Intent intent) {
        if (this.mDataShapingUtils.isUsbConnected(intent)) {
            turnStateFromOpenToOpenLocked();
        }
    }

    public void onBTStateChanged(Intent intent) {
        if (this.mDataShapingUtils.isBTStateOn(intent)) {
            turnStateFromOpenToOpenLocked();
        }
    }

    public void onDeviceIdleStateChanged(boolean enabled) {
        Slog.d(TAG, "[onDeviceIdleStateChanged] DeviceIdle enable is =" + enabled);
        if (enabled) {
            turnStateFromOpenToOpenLocked();
        }
    }

    public void onAPPStandbyStateChanged(boolean isParoleOn) {
        Slog.d(TAG, "[onAPPStandbyStateChanged] APPStandby parole state is =" + isParoleOn);
        if (isParoleOn) {
            turnStateFromOpenToOpenLocked();
        }
    }

    public void onInputFilterStateChanged(boolean isInstall) {
        Slog.d(TAG, "[onInputFilterStateChanged] InputFilter install state is =" + isInstall);
    }

    private void turnStateFromOpenToOpenLocked() {
        this.mDataShapingManager.setCurrentState(1);
    }

    private void turnStateFromOpenToClose() {
        if (this.mDataShapingUtils.isMusicActive()) {
            this.mDataShapingUtils.setClosingDelayForMusic(true);
            Slog.d(TAG, "[turnStateFromOpenToClose] music active, so still in open state!");
        } else if (this.mDataShapingUtils.getClosingDelayForMusic()) {
            this.mDataShapingUtils.setClosingDelayStartTime(System.currentTimeMillis());
            this.mDataShapingUtils.setClosingDelayForMusic(false);
            Slog.d(TAG, "[turnStateFromOpenToClose] mIsClosingDelayForMusic is true, so still in open state!");
        } else if (System.currentTimeMillis() - this.mDataShapingUtils.getClosingDelayStartTime() < 5000) {
            Slog.d(TAG, "[turnStateFromOpenToClose] close delay < buffer time, so still in open state!");
        } else {
            this.mDataShapingUtils.setClosingDelayStartTime(0);
            if (this.mDataShapingManager.registerListener()) {
                if (this.mDataShapingUtils.setLteUplinkDataTransfer(false, 600000)) {
                    this.mDataShapingManager.setCurrentState(3);
                    this.mDataShapingManager.startCloseExpiredAlarm();
                } else {
                    Slog.d(TAG, "[turnStateFromOpenToClose] fail!");
                }
                return;
            }
            Slog.d(TAG, "[turnStateFromOpenToClose] registerListener Failed so still in open state!");
        }
    }
}
