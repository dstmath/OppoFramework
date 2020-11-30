package com.mediatek.ims.legacy.ss;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.PreciseDataConnectionState;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.mediatek.ims.SuppSrvConfig;

public class XcapMobileDataNetworkManager {
    private static final String LOG_TAG = "XcapMobileDataNetworkManager";
    private static final int MSG_RELEASE_NETWORK = 0;
    public static final int TYPE_XCAP = 2048;
    private static int mDataCoolDownTimer = 0;
    private static int mKeepAliveTimer = 0;
    private static int mRequestTimer = 0;
    private boolean isNotifyByDataDisconnected = false;
    private ConnectivityManager mConnectivityManager;
    private Context mContext;
    private int mDataUsedPhoneId = -1;
    private Handler mHandlerReleaseNW = null;
    private HandlerThread mHandlerThread;
    private Network mNetwork;
    private ConnectivityManager.NetworkCallback mNetworkCallback;
    private PhoneStateListener mPhoneStateListener;
    private long mPreviousReleaseTime = -1;
    private TelephonyManager mTelephonyManager;
    private int mXcapDataConnectionState = -1;
    private int mXcapMobileDataNetworkRequestCount;

    public XcapMobileDataNetworkManager(Context context, Looper looper) {
        this.mContext = context;
        this.mNetworkCallback = null;
        this.mNetwork = null;
        this.mXcapMobileDataNetworkRequestCount = 0;
        this.mConnectivityManager = null;
        this.mHandlerReleaseNW = new NetworkHandler(looper);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mHandlerThread = new HandlerThread("PhoneStateListenerNotify");
        this.mHandlerThread.start();
    }

    public static void setKeepAliveTimer(int timer) {
        Rlog.d(LOG_TAG, "setKeepAliveTimer: " + timer);
        mKeepAliveTimer = timer;
    }

    public static void setRequestDataTimer(int timer) {
        Rlog.d(LOG_TAG, "setRequestDataTimer: " + timer);
        mRequestTimer = timer;
    }

    public static void setDataCoolDownTimer(int timer) {
        Rlog.d(LOG_TAG, "setDataCoolDownTimer: " + timer);
        mDataCoolDownTimer = timer;
    }

    public Network acquireNetwork(int phoneId) {
        Rlog.d(LOG_TAG, "acquireNetwork: phoneId = " + phoneId);
        SuppSrvConfig ssConfig = SuppSrvConfig.getInstance(this.mContext);
        if (ssConfig.isUseXCAPTypeApn() || ssConfig.isUseInternetTypeApn()) {
            synchronized (this) {
                Rlog.d(LOG_TAG, "acquireNetwork start");
                this.mXcapMobileDataNetworkRequestCount++;
                this.mHandlerReleaseNW.removeMessages(0);
                if (this.mNetwork != null) {
                    if (this.mDataUsedPhoneId != phoneId) {
                        releaseRequest();
                        this.mXcapMobileDataNetworkRequestCount++;
                    } else {
                        Rlog.d(LOG_TAG, "already available: mNetwork=" + this.mNetwork);
                        return this.mNetwork;
                    }
                }
                Rlog.d(LOG_TAG, "start new network request");
                this.mDataUsedPhoneId = phoneId;
                long coolDownDuration = SystemClock.elapsedRealtime() - this.mPreviousReleaseTime;
                long waitCoolDown = coolDownDuration > ((long) mDataCoolDownTimer) ? 0 : ((long) mDataCoolDownTimer) - coolDownDuration;
                Rlog.d(LOG_TAG, "waitCoolDown=" + waitCoolDown + ", coolDownDuration=" + coolDownDuration + ", mDataCoolDownTimer=" + mDataCoolDownTimer);
                try {
                    Thread.sleep(waitCoolDown);
                } catch (InterruptedException e) {
                    Rlog.d(LOG_TAG, "wait cool down interrupted");
                }
                newRequest(phoneId);
                Rlog.d(LOG_TAG, "wait request result ... mRequestTimer=" + mRequestTimer);
                try {
                    wait((long) mRequestTimer);
                } catch (InterruptedException e2) {
                    Rlog.d(LOG_TAG, "wait request interrupted");
                }
                Rlog.d(LOG_TAG, "continue ...");
                if (this.mNetwork != null) {
                    Rlog.d(LOG_TAG, "acquireNetwork success: mNetwork=" + this.mNetwork);
                    return this.mNetwork;
                }
                if (this.isNotifyByDataDisconnected) {
                    Rlog.d(LOG_TAG, "create xcap data connection failed");
                    this.isNotifyByDataDisconnected = false;
                } else {
                    Rlog.d(LOG_TAG, "timed out");
                }
                stopListenXcapDataConnectionState();
                releaseRequest();
                return null;
            }
        }
        Rlog.d(LOG_TAG, "not use any APN. No need to acquireNetwork");
        return null;
    }

    class NetworkHandler extends Handler {
        public NetworkHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                Rlog.d(XcapMobileDataNetworkManager.LOG_TAG, "Ready to release network: " + XcapMobileDataNetworkManager.this.mNetwork);
                synchronized (XcapMobileDataNetworkManager.this) {
                    XcapMobileDataNetworkManager.this.releaseRequest();
                }
            }
        }
    }

    public void releaseNetwork() {
        synchronized (this) {
            if (this.mXcapMobileDataNetworkRequestCount > 0) {
                this.mXcapMobileDataNetworkRequestCount--;
                Rlog.d(LOG_TAG, "releaseNetwork count=" + this.mXcapMobileDataNetworkRequestCount);
                if (this.mXcapMobileDataNetworkRequestCount < 1) {
                    if (this.mNetwork == null) {
                        Rlog.d(LOG_TAG, "No dedicate network here, release directly.");
                        releaseRequest();
                    } else {
                        Rlog.d(LOG_TAG, "Delay release network.");
                        this.mHandlerReleaseNW.sendMessageDelayed(this.mHandlerReleaseNW.obtainMessage(0), (long) mKeepAliveTimer);
                    }
                }
            }
        }
    }

    private void newRequest(int phoneId) {
        ConnectivityManager connectivityManager = getConnectivityManager();
        int subId = getSubIdUsingPhoneId(phoneId);
        this.mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            /* class com.mediatek.ims.legacy.ss.XcapMobileDataNetworkManager.AnonymousClass1 */

            public void onAvailable(Network network) {
                super.onAvailable(network);
                Rlog.d(XcapMobileDataNetworkManager.LOG_TAG, "NetworkCallbackListener.onAvailable: network=" + network);
                XcapMobileDataNetworkManager.this.stopListenXcapDataConnectionState();
                synchronized (XcapMobileDataNetworkManager.this) {
                    XcapMobileDataNetworkManager.this.mNetwork = network;
                    XcapMobileDataNetworkManager.this.notifyAll();
                }
            }

            public void onLost(Network network) {
                super.onLost(network);
                Rlog.d(XcapMobileDataNetworkManager.LOG_TAG, "NetworkCallbackListener.onLost: network=" + network);
                XcapMobileDataNetworkManager.this.stopListenXcapDataConnectionState();
                synchronized (XcapMobileDataNetworkManager.this) {
                    XcapMobileDataNetworkManager.this.releaseRequest();
                    XcapMobileDataNetworkManager.this.notifyAll();
                }
            }

            public void onUnavailable() {
                super.onUnavailable();
                Rlog.d(XcapMobileDataNetworkManager.LOG_TAG, "NetworkCallbackListener.onUnavailable");
                XcapMobileDataNetworkManager.this.stopListenXcapDataConnectionState();
                synchronized (XcapMobileDataNetworkManager.this) {
                    XcapMobileDataNetworkManager.this.releaseRequest();
                    XcapMobileDataNetworkManager.this.notifyAll();
                }
            }
        };
        Rlog.d(LOG_TAG, "newRequest, subId=" + subId);
        NetworkRequest.Builder networkBuilder = new NetworkRequest.Builder().addTransportType(0).setNetworkSpecifier(Integer.toString(subId));
        if (SuppSrvConfig.getInstance(this.mContext).isUseInternetTypeApn()) {
            networkBuilder.addCapability(12);
            networkBuilder.addCapability(13);
        } else {
            networkBuilder.addCapability(9);
        }
        startListenXcapDataConnectionState(phoneId);
        connectivityManager.requestNetwork(networkBuilder.build(), this.mNetworkCallback, mRequestTimer);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseRequest() {
        Rlog.d(LOG_TAG, "releaseRequest: mNetwork=" + this.mNetwork + ", mNetworkCallback=" + this.mNetworkCallback);
        ConnectivityManager connectivityManager = getConnectivityManager();
        ConnectivityManager.NetworkCallback networkCallback = this.mNetworkCallback;
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        if (this.mNetwork != null) {
            this.mPreviousReleaseTime = SystemClock.elapsedRealtime();
            Rlog.d(LOG_TAG, "Release time: " + this.mPreviousReleaseTime);
        }
        if (connectivityManager != null) {
            Rlog.d(LOG_TAG, "UnBind process network");
            connectivityManager.bindProcessToNetwork(null);
        }
        this.mNetworkCallback = null;
        this.mNetwork = null;
        this.mXcapMobileDataNetworkRequestCount = 0;
        this.mDataUsedPhoneId = -1;
    }

    private ConnectivityManager getConnectivityManager() {
        if (this.mConnectivityManager == null) {
            this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        return this.mConnectivityManager;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String dataStateToString(int state) {
        if (state == -1) {
            return "DATA_UNKNOWN";
        }
        if (state == 0) {
            return "DATA_DISCONNECTED";
        }
        if (state == 1) {
            return "DATA_CONNECTING";
        }
        if (state == 2) {
            return "DATA_CONNECTED";
        }
        if (state != 3) {
            return "Error";
        }
        return "DATA_SUSPENDED";
    }

    private void startListenXcapDataConnectionState(int phoneId) {
        int subId = getSubIdUsingPhoneId(phoneId);
        Rlog.d(LOG_TAG, "startListenXcapDataConnectionState: subid=" + subId);
        this.mPhoneStateListener = new PhoneStateListener(this.mHandlerThread.getLooper()) {
            /* class com.mediatek.ims.legacy.ss.XcapMobileDataNetworkManager.AnonymousClass2 */

            public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState state) {
                int newState = state.getDataConnectionState();
                int apnType = state.getDataConnectionApnTypeBitMask();
                StringBuilder sb = new StringBuilder();
                sb.append("onPreciseDataConnectionStateChanged: apnType=");
                sb.append(apnType);
                sb.append(", newState=");
                sb.append(XcapMobileDataNetworkManager.this.dataStateToString(newState));
                sb.append(", currentXcapState=");
                XcapMobileDataNetworkManager xcapMobileDataNetworkManager = XcapMobileDataNetworkManager.this;
                sb.append(xcapMobileDataNetworkManager.dataStateToString(xcapMobileDataNetworkManager.mXcapDataConnectionState));
                Rlog.d(XcapMobileDataNetworkManager.LOG_TAG, sb.toString());
                if (apnType == 2048) {
                    if (XcapMobileDataNetworkManager.this.mXcapDataConnectionState == 1 && XcapMobileDataNetworkManager.this.mXcapDataConnectionState != newState && newState == 0) {
                        XcapMobileDataNetworkManager.this.isNotifyByDataDisconnected = true;
                        XcapMobileDataNetworkManager.this.stopListenXcapDataConnectionState();
                        synchronized (XcapMobileDataNetworkManager.this) {
                            XcapMobileDataNetworkManager.this.notifyAll();
                        }
                    } else if (newState != -1) {
                        XcapMobileDataNetworkManager.this.mXcapDataConnectionState = newState;
                    }
                }
            }
        };
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager != null) {
            telephonyManager.listen(this.mPhoneStateListener, 4096);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopListenXcapDataConnectionState() {
        PhoneStateListener phoneStateListener;
        Rlog.d(LOG_TAG, "stopListenXcapDataConnectionState: listener=" + this.mPhoneStateListener);
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (!(telephonyManager == null || (phoneStateListener = this.mPhoneStateListener) == null)) {
            telephonyManager.listen(phoneStateListener, 0);
        }
        this.mPhoneStateListener = null;
        this.mXcapDataConnectionState = -1;
    }

    private int getSubIdUsingPhoneId(int phoneId) {
        int[] subIds = SubscriptionManager.getSubId(phoneId);
        if (subIds.length != 0) {
            return subIds[0];
        }
        return -1;
    }
}
