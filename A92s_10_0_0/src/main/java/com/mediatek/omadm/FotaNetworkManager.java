package com.mediatek.omadm;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.SystemClock;
import android.util.Log;

public class FotaNetworkManager {
    private static final int NETWORK_ACQUIRE_TIMEOUT_MILLIS = 20000;
    private static final int NETWORK_REQUEST_TIMEOUT_MILLIS = 15000;
    private static final String TAG = "FotaNetworkManager";
    private volatile ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private int mFotaReqCnt;
    /* access modifiers changed from: private */
    public Network mNwk;
    private ConnectivityManager.NetworkCallback mNwkClbk;
    private final NetworkRequest mNwkReq;
    OmadmServiceImpl mOmadmSrv = null;
    private final int mSubId;

    private class NetworkReqClbk extends ConnectivityManager.NetworkCallback {
        private NetworkReqClbk() {
        }

        public void onAvailable(Network network) {
            super.onAvailable(network);
            Log.i(FotaNetworkManager.TAG, "NetworkCallbackListener.onAvailable: network=" + network);
            synchronized (FotaNetworkManager.this) {
                Network unused = FotaNetworkManager.this.mNwk = network;
                FotaNetworkManager.this.notifyAll();
            }
        }

        public void onLost(Network network) {
            super.onLost(network);
            Log.w(FotaNetworkManager.TAG, "NetworkCallbackListener.onLost: network=" + network);
            synchronized (FotaNetworkManager.this) {
                FotaNetworkManager.this.releaseReqLock(this);
                FotaNetworkManager.this.notifyAll();
                if (FotaNetworkManager.this.mOmadmSrv != null) {
                    FotaNetworkManager.this.mOmadmSrv.omadmControllerDispachAdminNetStatus(3, 0);
                }
                Network unused = FotaNetworkManager.this.mNwk = null;
            }
        }

        public void onUnavailable() {
            super.onUnavailable();
            Log.w(FotaNetworkManager.TAG, "NetworkCallbackListener.onUnavailable");
            synchronized (FotaNetworkManager.this) {
                FotaNetworkManager.this.releaseReqLock(this);
                FotaNetworkManager.this.notifyAll();
            }
        }
    }

    private void startNwkReqLock() {
        ConnectivityManager connectivityManager = getConnectivityManager();
        this.mNwkClbk = new NetworkReqClbk();
        connectivityManager.requestNetwork(this.mNwkReq, this.mNwkClbk, NETWORK_REQUEST_TIMEOUT_MILLIS);
    }

    /* access modifiers changed from: private */
    public void releaseReqLock(ConnectivityManager.NetworkCallback callback) {
        if (callback != null) {
            try {
                getConnectivityManager().unregisterNetworkCallback(callback);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Unregister network callback exception", e);
            }
        }
        resetLocked();
    }

    private void resetLocked() {
        this.mNwkClbk = null;
        this.mNwk = null;
        this.mFotaReqCnt = 0;
    }

    private ConnectivityManager getConnectivityManager() {
        if (this.mConnectivityManager == null) {
            this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        return this.mConnectivityManager;
    }

    public FotaNetworkManager(OmadmServiceImpl service, Context context, int subId) {
        this.mContext = context;
        this.mNwkClbk = null;
        this.mNwk = null;
        this.mFotaReqCnt = 0;
        this.mConnectivityManager = null;
        this.mSubId = subId;
        this.mOmadmSrv = service;
        this.mNwkReq = new NetworkRequest.Builder().addTransportType(0).addCapability(3).setNetworkSpecifier(Integer.toString(this.mSubId)).build();
    }

    public int acquireNetwork(String callerID) throws FotaException {
        synchronized (this) {
            this.mFotaReqCnt++;
            if (this.mNwk != null) {
                Log.d(callerID, "FotaNetworkManager: already available");
                int i = this.mNwk.netId;
                return i;
            }
            if (this.mNwkClbk == null) {
                Log.d(callerID, "FotaNetworkManager: start new network request");
                startNwkReqLock();
            }
            long shouldEnd = SystemClock.elapsedRealtime() + 20000;
            for (long waitTime = 20000; waitTime > 0; waitTime = shouldEnd - SystemClock.elapsedRealtime()) {
                try {
                    wait(waitTime);
                } catch (InterruptedException e) {
                    Log.w(callerID, "FotaNetworkManager: acquire network wait interrupted");
                }
                if (this.mNwk != null) {
                    int i2 = this.mNwk.netId;
                    return i2;
                }
            }
            Log.e(callerID, "FotaNetworkManager: timed out");
            releaseReqLock(this.mNwkClbk);
            throw new FotaException("Acquiring network timed out");
        }
    }

    public boolean releaseNetwork(String callerID) {
        synchronized (this) {
            if (this.mFotaReqCnt > 0) {
                this.mFotaReqCnt--;
                Log.d(callerID, "FotaNetworkManager: release, count=" + this.mFotaReqCnt);
                if (this.mFotaReqCnt < 1) {
                    Log.d(callerID, "FotaNetworkManager: Release FOTA Network");
                    releaseReqLock(this.mNwkClbk);
                    return true;
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000d, code lost:
        r3 = getConnectivityManager().getNetworkInfo(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0016, code lost:
        if (r3 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        return r3.getExtraInfo();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        return null;
     */
    public String getApnName() {
        synchronized (this) {
            if (this.mNwk == null) {
                return null;
            }
            Network network = this.mNwk;
        }
    }
}
