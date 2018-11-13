package com.android.server;

import android.content.Context;

class BluetoothService extends SystemService {
    private BluetoothManagerService mBluetoothManagerService;

    public BluetoothService(Context context) {
        super(context);
        this.mBluetoothManagerService = new BluetoothManagerService(context);
    }

    public void onStart() {
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            publishBinderService("bluetooth_manager", this.mBluetoothManagerService);
        } else if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
            this.mBluetoothManagerService.handleOnBootPhase();
        }
    }

    public void onSwitchUser(int userHandle) {
        this.mBluetoothManagerService.handleOnSwitchUser(userHandle);
    }

    public void onUnlockUser(int userHandle) {
        this.mBluetoothManagerService.handleOnUnlockUser(userHandle);
    }
}
