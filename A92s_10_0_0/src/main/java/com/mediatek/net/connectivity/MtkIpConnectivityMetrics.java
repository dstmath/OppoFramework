package com.mediatek.net.connectivity;

import android.content.Context;
import android.net.INetdEventCallback;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseBooleanArray;
import com.android.server.connectivity.NetdEventListenerService;
import com.android.server.net.BaseNetdEventCallback;
import com.mediatek.net.connectivity.IMtkIpConnectivityMetrics;
import com.mediatek.server.MtkSystemServiceFactory;
import com.mediatek.server.powerhal.PowerHalManager;

public final class MtkIpConnectivityMetrics {
    private static final boolean DBG = true;
    private static final boolean FEATURE_SUPPORTED = true;
    /* access modifiers changed from: private */
    public static final String TAG = MtkIpConnectivityMetrics.class.getSimpleName();
    private Context mContext;
    public Impl mImpl;
    private final INetdEventCallback mNetdEventListener = new BaseNetdEventCallback() {
        /* class com.mediatek.net.connectivity.MtkIpConnectivityMetrics.AnonymousClass1 */

        public void onDnsEvent(int netId, int eventType, int returnCode, String hostname, String[] ipAddresses, int ipAddressesCount, long timestamp, int uid) {
            MtkIpConnectivityMetrics.this.mImpl.onCtaDnsEvent(netId, uid);
            MtkIpConnectivityMetrics.this.mImpl.onMonitorDnsEvent(netId, eventType, returnCode, hostname, ipAddressesCount, uid);
        }

        public synchronized void onConnectEvent(String ipAddr, int port, long timestamp, int uid) {
            MtkIpConnectivityMetrics.this.mImpl.onCtaConnectEvent(uid);
            MtkIpConnectivityMetrics.this.mImpl.onMonitorConnectEvent(uid);
        }
    };
    private NetdEventListenerService mNetdEventListenerService;
    /* access modifiers changed from: private */
    public PowerHalManager mPowerHalManager = MtkSystemServiceFactory.getInstance().makePowerHalManager();

    public MtkIpConnectivityMetrics(Context ctx, NetdEventListenerService service) {
        Log.d(TAG, "MtkIpConnectivityMetrics is created:true");
        this.mContext = ctx;
        this.mNetdEventListenerService = service;
        this.mImpl = new Impl(this.mContext);
        try {
            this.mNetdEventListenerService.addNetdEventCallback(3, this.mNetdEventListener);
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "MtkIpConnectivityMetrics addNetdEventCallback:" + e);
        }
    }

    public IBinder getMtkIpConnSrv() {
        return this.mImpl;
    }

    public final class Impl extends IMtkIpConnectivityMetrics.Stub {
        private Context mContext;
        private INetdEventCallback mNetdEventCallback;
        private INetdEventCallback mSocketEventCallback;
        final Object mUidSockeRulestLock = new Object();
        private SparseBooleanArray mUidSocketRules = new SparseBooleanArray();

        public Impl(Context ctx) {
            this.mContext = ctx;
        }

        @Override // com.mediatek.net.connectivity.IMtkIpConnectivityMetrics
        public boolean registerMtkNetdEventCallback(INetdEventCallback callback) {
            if (!isPermissionAllowed()) {
                return false;
            }
            Log.d(MtkIpConnectivityMetrics.TAG, "registerMtkNetdEventCallback");
            this.mNetdEventCallback = callback;
            return true;
        }

        @Override // com.mediatek.net.connectivity.IMtkIpConnectivityMetrics
        public boolean unregisterMtkNetdEventCallback() {
            if (!isPermissionAllowed()) {
                return false;
            }
            Log.d(MtkIpConnectivityMetrics.TAG, "unregisterMtkNetdEventCallback");
            this.mNetdEventCallback = null;
            return true;
        }

        @Override // com.mediatek.net.connectivity.IMtkIpConnectivityMetrics
        public boolean registerMtkSocketEventCallback(INetdEventCallback callback) {
            if (!isPermissionAllowed()) {
                return false;
            }
            Log.d(MtkIpConnectivityMetrics.TAG, "registerMtkSocketEventCallback");
            this.mSocketEventCallback = callback;
            return true;
        }

        @Override // com.mediatek.net.connectivity.IMtkIpConnectivityMetrics
        public boolean unregisterMtkSocketEventCallback() {
            if (!isPermissionAllowed()) {
                return false;
            }
            Log.d(MtkIpConnectivityMetrics.TAG, "unregisterMtkSocketEventCallback");
            this.mSocketEventCallback = null;
            return true;
        }

        @Override // com.mediatek.net.connectivity.IMtkIpConnectivityMetrics
        public void updateCtaAppStatus(int uid, boolean isNotified) {
            if (isPermissionAllowed() && uid >= 10000) {
                synchronized (this.mUidSockeRulestLock) {
                    String access$400 = MtkIpConnectivityMetrics.TAG;
                    Log.d(access$400, "updateCtaAppStatus:" + uid + ":" + isNotified);
                    this.mUidSocketRules.put(uid, isNotified);
                }
            }
        }

        @Override // com.mediatek.net.connectivity.IMtkIpConnectivityMetrics
        public void setSpeedDownload(int timeoutMs) {
            if (MtkIpConnectivityMetrics.this.mPowerHalManager != null) {
                String access$400 = MtkIpConnectivityMetrics.TAG;
                Log.d(access$400, "setSpeedDownload:" + timeoutMs);
                MtkIpConnectivityMetrics.this.mPowerHalManager.setSpeedDownload(timeoutMs);
            }
        }

        /* access modifiers changed from: private */
        public void onCtaDnsEvent(int netId, int uid) {
            String access$400;
            StringBuilder sb;
            if (this.mNetdEventCallback != null && uid >= 10000) {
                synchronized (this.mUidSockeRulestLock) {
                    try {
                        boolean isNotified = this.mUidSocketRules.get(uid, true);
                        try {
                            access$400 = MtkIpConnectivityMetrics.TAG;
                            sb = new StringBuilder();
                            sb.append("onDnsEvent:uid=");
                            sb.append(uid);
                            sb.append(", netId=");
                        } catch (Throwable th) {
                            th = th;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            throw th;
                        }
                        try {
                            sb.append(netId);
                            sb.append(", isNotified=");
                            sb.append(isNotified);
                            Log.d(access$400, sb.toString());
                            if (isNotified) {
                                try {
                                    this.mNetdEventCallback.onDnsEvent(netId, 0, 0, "", (String[]) null, 0, 0, uid);
                                } catch (Exception e) {
                                    String access$4002 = MtkIpConnectivityMetrics.TAG;
                                    Log.d(access$4002, "onCtaDnsEvent:" + e);
                                }
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        public void onCtaConnectEvent(int uid) {
            if (this.mNetdEventCallback != null && uid >= 10000) {
                synchronized (this.mUidSockeRulestLock) {
                    boolean isNotified = this.mUidSocketRules.get(uid, true);
                    String access$400 = MtkIpConnectivityMetrics.TAG;
                    Log.d(access$400, "onDnsEvent:" + uid + ":" + isNotified);
                    if (isNotified) {
                        try {
                            this.mNetdEventCallback.onConnectEvent("", 0, 0, uid);
                        } catch (Exception e) {
                            String access$4002 = MtkIpConnectivityMetrics.TAG;
                            Log.d(access$4002, "onCtaConnectEvent:" + e);
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        public void onMonitorDnsEvent(int netId, int eventType, int returnCode, String hostname, int ipAddressesCount, int uid) {
            INetdEventCallback iNetdEventCallback = this.mSocketEventCallback;
            if (iNetdEventCallback != null) {
                try {
                    iNetdEventCallback.onDnsEvent(netId, eventType, returnCode, hostname, (String[]) null, ipAddressesCount, 0, uid);
                } catch (Exception e) {
                    String access$400 = MtkIpConnectivityMetrics.TAG;
                    Log.d(access$400, "onMonitorDnsEvent:" + e);
                }
            }
        }

        /* access modifiers changed from: private */
        public void onMonitorConnectEvent(int uid) {
            INetdEventCallback iNetdEventCallback = this.mSocketEventCallback;
            if (iNetdEventCallback != null) {
                try {
                    iNetdEventCallback.onConnectEvent("", 0, 0, uid);
                } catch (Exception e) {
                    String access$400 = MtkIpConnectivityMetrics.TAG;
                    Log.d(access$400, "onMonitorConnectEvent:" + e);
                }
            }
        }

        private boolean isPermissionAllowed() {
            enforceNetworkMonitorPermission();
            if (Binder.getCallingUid() == 1000) {
                return true;
            }
            String access$400 = MtkIpConnectivityMetrics.TAG;
            Log.d(access$400, "No permission:" + Binder.getCallingUid());
            return false;
        }

        private void enforceNetworkMonitorPermission() {
            int uid = Binder.getCallingUid();
            if (uid != 1000) {
                throw new SecurityException(String.format("Uid %d has no permission to change watchlist setting.", Integer.valueOf(uid)));
            }
        }
    }
}
