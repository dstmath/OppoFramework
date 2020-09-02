package com.st.android.nfc_extensions;

import android.os.RemoteException;
import android.util.Log;

public final class NfcWalletAdapter {
    private static final String TAG = "NfcWalletAdapter";
    private static INfcWalletAdapter sInterface = null;

    public NfcWalletAdapter(INfcWalletAdapter intf) {
        sInterface = intf;
    }

    /* access modifiers changed from: package-private */
    public void attemptDeadServiceRecovery(Exception e) {
        Log.e(TAG, "NFC Wallet Adapter ST Extensions dead - recover by close / open, TODO");
    }

    public boolean keepEseSwpActive(boolean enable) {
        try {
            return sInterface.keepEseSwpActive(enable);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public boolean setMuteTech(boolean muteA, boolean muteB, boolean muteF) {
        try {
            return sInterface.setMuteTech(muteA, muteB, muteF);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public boolean setObserverMode(boolean enable) {
        try {
            return sInterface.setObserverMode(enable);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public boolean registerStLogCallback(INfcWalletLogCallback cb) {
        try {
            return sInterface.registerStLogCallback(cb);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public boolean unregisterStLogCallback() {
        try {
            return sInterface.unregisterStLogCallback();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public boolean rotateRfParameters(boolean reset) {
        try {
            return sInterface.rotateRfParameters(reset);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }
}
