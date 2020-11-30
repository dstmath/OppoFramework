package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import java.util.HashMap;

public class OppoManuConnectManager {
    private static final String CONNECT_MODE_CHANGE_ACTION = "android.net.wifi.CONNECT_MODE_CHANGE";
    private static final long CONNECT_TIMEOUT = 120000;
    private static boolean DEBUG = true;
    private static final int EVT_CONNECT_TIMEOUT = 1;
    private static final String EVT_CONNECT_TIMEOUT_STR = "connect_timeout";
    private static final int EVT_UNKNOWN = 0;
    private static final String EVT_UNKNOWN_STR = "unknown";
    private static final String EXTRA_CONNECT_MODE = "connectMode";
    private static final String EXTRA_CONNECT_UID = "connectUid";
    public static final int NETID_INVALID = -1;
    public static final int NTEID_NETWORK_SUGGESTION_INAVLID = -3;
    public static final int NTEID_WPS_INAVLID = -2;
    public static final int STATE_AUTH_COMPLETED = 1;
    private static final String STATE_AUTH_COMPLETED_STR = "completed";
    public static final int STATE_CONNECTED = 2;
    private static final String STATE_CONNECTED_STR = "connected";
    public static final int STATE_DISCONNECTED = 3;
    private static final String STATE_DISCONNECTED_STR = "disconnected";
    public static final int STATE_INVALID = -1;
    private static final String STATE_INVALID_STR = "invalid";
    public static final int STATE_MAX = 4;
    public static final int STATE_STARTED = 0;
    private static final String STATE_STARTED_STR = "started";
    private static final String TAG = "OppoManuConnectManager";
    private static final int TOKEN_MAX = 268435455;
    public static final int TYPE_INVALID = -1;
    private static final String TYPE_INVALID_STR = "invalid";
    public static final int TYPE_MAX = 3;
    public static final int TYPE_SETTING = 0;
    private static final String TYPE_SETTING_STR = "setting";
    public static final int TYPE_THIRDAPK = 1;
    private static final String TYPE_THIRDAPK_STR = "thirdapk";
    public static final int TYPE_WPS = 2;
    private static final String TYPE_WPS_STR = "wps";
    public static final int UID_DEFAULT = 1000;
    private static final String WPS_TAG = "WPS";
    private static final String WPS_TYPE_AUTH = "WPS-AUTH";
    private static final String WPS_TYPE_PBC = "WPS-PBC";
    private static final String WPS_TYPE_PIN = "WPS-PIN";
    private static ClientModeImpl mClientModeImpl;
    private static Context mContext = null;
    private static OppoManuConnectManagerHandler mHandler;
    private static OppoManuConnectManager mInstance = null;
    private static WifiConfigManager mWifiConfigManager;
    private static WifiRomUpdateHelper mWifiRomUpdateHelpler;
    private String mBssid;
    private int mConnectTimeoutToken = 0;
    private HashMap<Integer, String> mEventStrHashMap = new HashMap<>();
    private boolean mIsRoaming = false;
    private int mNetId;
    private int mState;
    private HashMap<Integer, String> mStateStrHashMap = new HashMap<>();
    private int mType;
    private HashMap<Integer, String> mTypeStrHashMap = new HashMap<>();
    private int mUid;

    public int getState() {
        return this.mState;
    }

    private void setState(int newState) {
        logd("set manu connect State = " + newState + " [" + getStateString(newState) + "]");
        if (newState >= 4) {
            logd("invalid state:" + newState);
            return;
        }
        if (newState == 0) {
            sendManuConnectTimeoutEvt();
        } else if (2 == newState || 3 == newState) {
            cancelManuConnectTimeoutEvt();
        }
        if (2 == this.mState && 1 == newState) {
            this.mIsRoaming = true;
        } else {
            this.mIsRoaming = false;
        }
        this.mState = newState;
    }

    public int getType() {
        return this.mType;
    }

    private void setType(int newType) {
        logd("set manu connect Type = " + newType + " [" + getTypeString(newType) + "]");
        if (newType >= 3) {
            logd("invalid type = " + newType);
            return;
        }
        this.mType = newType;
    }

    public int getNetId() {
        return this.mNetId;
    }

    private void setNetId(int newNetId) {
        logd("set manu connect NetId = " + newNetId);
        this.mNetId = newNetId;
    }

    public String getBssid() {
        return this.mBssid;
    }

    private void setBssid(String bssid) {
        logd("set manu connect bssid = " + bssid);
        this.mBssid = bssid;
    }

    private int getUid() {
        return this.mUid;
    }

    private void setUid(int uid) {
        logd("set manu connect uid = " + uid);
        this.mUid = uid;
    }

    public void sendConnectModeChangeBroadcast(boolean manualConnect) {
        Intent intent = new Intent(CONNECT_MODE_CHANGE_ACTION);
        intent.addFlags(67108864);
        intent.putExtra(EXTRA_CONNECT_MODE, manualConnect);
        intent.putExtra(EXTRA_CONNECT_UID, getManuConnectUid());
        Context context = mContext;
        if (context != null) {
            context.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
        logd("sendConnectModeChangeBroadcast " + manualConnect);
    }

    private void update(int state, int type, int netId, int uid) {
        getState();
        setState(state);
        setType(type);
        setNetId(netId);
        setUid(uid);
    }

    public void reset() {
        logd("manu connect manager reset!!");
        update(3, -1, -1, 1000);
    }

    public String toString() {
        return "manu connect: netId = " + this.mNetId + ",type = [" + this.mType + "]" + getTypeString(this.mType) + ",state = [" + this.mState + "]" + getStateString(this.mState);
    }

    private void debugStringsInit() {
        HashMap<Integer, String> hashMap = this.mStateStrHashMap;
        if (hashMap != null) {
            hashMap.put(-1, "invalid");
            this.mStateStrHashMap.put(0, STATE_STARTED_STR);
            this.mStateStrHashMap.put(1, STATE_AUTH_COMPLETED_STR);
            this.mStateStrHashMap.put(2, STATE_CONNECTED_STR);
            this.mStateStrHashMap.put(3, STATE_DISCONNECTED_STR);
        }
        HashMap<Integer, String> hashMap2 = this.mTypeStrHashMap;
        if (hashMap2 != null) {
            hashMap2.put(-1, "invalid");
            this.mTypeStrHashMap.put(0, TYPE_SETTING_STR);
            this.mTypeStrHashMap.put(1, TYPE_THIRDAPK_STR);
            this.mTypeStrHashMap.put(2, TYPE_WPS_STR);
        }
        HashMap<Integer, String> hashMap3 = this.mEventStrHashMap;
        if (hashMap3 != null) {
            hashMap3.put(0, "");
            this.mEventStrHashMap.put(1, "");
        }
    }

    private String getStateString(int state) {
        HashMap<Integer, String> hashMap = this.mStateStrHashMap;
        if (hashMap == null || hashMap.size() <= 0) {
            logd("mStateStrHashMap is null or empty!!");
            return "invalid";
        }
        String str = this.mStateStrHashMap.get(Integer.valueOf(state));
        if (str == null) {
            return "invalid";
        }
        return str;
    }

    private String getTypeString(int type) {
        HashMap<Integer, String> hashMap = this.mTypeStrHashMap;
        if (hashMap == null || hashMap.size() <= 0) {
            logd("mTypeStrHashMap is null or empty!!");
            return "invalid";
        }
        String str = this.mTypeStrHashMap.get(Integer.valueOf(type));
        if (str == null) {
            return "invalid";
        }
        return str;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getEventString(int evt) {
        HashMap<Integer, String> hashMap = this.mEventStrHashMap;
        if (hashMap == null || hashMap.size() <= 0) {
            logd("mEventStrHashMap is null or empty!!");
            return EVT_UNKNOWN_STR;
        }
        String str = this.mEventStrHashMap.get(Integer.valueOf(evt));
        if (str == null) {
            return EVT_UNKNOWN_STR;
        }
        return str;
    }

    public boolean isManuConnecting() {
        int state = getState();
        if (state == 0 || 1 == state) {
            return true;
        }
        return false;
    }

    public boolean isManuConnected() {
        if (2 == getState()) {
            return true;
        }
        return false;
    }

    public boolean isManuConnect() {
        return isManuConnected() || isManuConnecting();
    }

    public int getManuConnectNetId() {
        if (isManuConnect()) {
            return getNetId();
        }
        return -1;
    }

    public String getManuConnectBssid() {
        return getBssid();
    }

    public void setManuConnectBssid(int netId, WifiConfiguration wconf) {
        if (netId != -1) {
            WifiConfiguration tmpConf = null;
            WifiConfigManager wifiConfigManager = mWifiConfigManager;
            if (wifiConfigManager != null) {
                tmpConf = wifiConfigManager.getConfiguredNetwork(netId);
            }
            if (tmpConf != null) {
                setBssid(tmpConf.BSSID);
            }
        } else if (wconf != null) {
            setBssid(wconf.BSSID);
        }
    }

    public int getManuConnectUid() {
        if (isManuConnect()) {
            return getUid();
        }
        return 1000;
    }

    public void handleNetworkDisabled(int netId) {
        logd("handleNetworkDisabled,netId = " + netId);
        if (-1 != netId) {
            if (netId != getNetId()) {
                logd("disabled netId is not the current manu connect netId!!");
            } else if (isManuConnect()) {
                logi("manu connect netId disabled,clear manu connect state!!");
                update(3, -1, -1, 1000);
            }
        }
    }

    public void handleNetworkDeleted(int netId) {
        logd("handleNetworkDeleted,netId = " + netId);
        if (-1 != netId) {
            if (netId != getNetId()) {
                logd("deleted netId is not the current manu connect netId!!");
            } else if (isManuConnect()) {
                logi("manu connect netId deleted,clear manu connect state!!");
                update(3, -1, -1, 1000);
            }
        }
    }

    public void handleWifiStateChanged(int wifiState) {
        logd("handleWifiStateChanged,wifiState = " + wifiState);
        if (1 == wifiState) {
            update(-1, -1, -1, 1000);
        }
    }

    public void handleWpsCompleted(int netId) {
        logd("handleWpsCompleted netId = " + netId);
        setNetId(netId);
    }

    public void handleSupplicantStateChanged(Message message) {
        logd("handleSupplicantStateChanged");
        if (message == null) {
            logd("message is null!!");
            return;
        }
        StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
        if (stateChangeResult == null) {
            logd("stateChangeResult is null!");
            return;
        }
        SupplicantState state = stateChangeResult.state;
        int netId = stateChangeResult.networkId;
        logd("netId = " + netId + " state = " + state);
        if (-1 != netId && SupplicantState.COMPLETED == state) {
            int manuState = getState();
            if (netId != getNetId() || !isManuConnect()) {
                sendConnectModeChangeBroadcast(false);
                return;
            }
            setState(1);
            if (1 == manuState) {
                logd("roam happened!");
                sendManuConnectTimeoutEvt();
                return;
            }
            sendConnectModeChangeBroadcast(true);
        }
    }

    public void handleConnectStateChanged(boolean connected, int netId) {
        logd("handleConnectStateChanged, connected = " + connected + " netId = " + netId);
        if (-1 != netId && isManuConnect()) {
            int state = getState();
            int manuConnectId = getNetId();
            if (connected) {
                if (manuConnectId == netId) {
                    setState(2);
                } else if (2 == state) {
                    logd("manu connect disconnect may missed,reset manu connect!!");
                    update(3, -1, -1, 1000);
                }
            } else if (manuConnectId != netId) {
            } else {
                if (2 == state || (1 == state && this.mIsRoaming)) {
                    update(3, -1, -1, 1000);
                }
            }
        }
    }

    public boolean shouldClearBssid() {
        boolean isThirdApkConnect = false;
        int manuConnectUid = getManuConnectUid();
        boolean isManuConnect = isManuConnect();
        String manuConnectBssid = getManuConnectBssid();
        int manuConnectType = getType();
        if (!isManuConnect) {
            return true;
        }
        if ((manuConnectUid > 10000 && manuConnectUid < 19999) || manuConnectType == 1) {
            isThirdApkConnect = true;
        }
        logd("isManuConnect = " + isManuConnect + " , manuConnectUid = " + manuConnectUid + " , manuConnectType = " + manuConnectType + " ,isThirdApkConnect = " + isThirdApkConnect + ", manuConnectBssid = " + manuConnectBssid);
        if (!isThirdApkConnect || manuConnectBssid == null || "any".equals(manuConnectBssid)) {
            return true;
        }
        return false;
    }

    public void handleManuConnect(int netId, int uid) {
        logd("handleManuConnect,netId = " + netId);
        if (-1 != netId) {
            update(0, 0, netId, uid);
        }
    }

    public void handleThirdAPKConnect(int netId, int uid) {
        logd("handleThirdAPKConnect,netId = " + netId);
        if (-1 != netId) {
            update(0, 1, netId, uid);
        }
    }

    public void handleWpsConnect(int uid) {
        logd("handleWpsConnect");
        update(0, 2, -2, uid);
    }

    public void setNetworkSuggestionConnect(String bssid, int uid) {
        logd("handleNetworkSuggestion");
        setBssid(bssid);
        update(0, 1, -3, uid);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String str) {
        if (DEBUG) {
            Log.d(TAG, "debug:" + str);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String str) {
        Log.d(TAG, "error:" + str);
    }

    private void logi(String str) {
        if (DEBUG) {
            Log.d(TAG, "Info:" + str);
        }
    }

    private void logw(String str) {
        if (DEBUG) {
            Log.d(TAG, "Warning:" + str);
        }
    }

    public void enableVerboseLogging(int verbose) {
        Log.d(TAG, "enableVerboseLogging verbose = " + verbose);
        if (verbose > 0) {
            DEBUG = true;
        } else {
            DEBUG = false;
        }
    }

    private long getManuConnectTimeout() {
        WifiRomUpdateHelper wifiRomUpdateHelper = mWifiRomUpdateHelpler;
        if (wifiRomUpdateHelper == null) {
            return CONNECT_TIMEOUT;
        }
        return wifiRomUpdateHelper.getLongValue("WIFI_MANU_CONNECT_CONNECT_TIMEOUT", Long.valueOf((long) CONNECT_TIMEOUT)).longValue();
    }

    private void sendMessage(int what) {
        OppoManuConnectManagerHandler oppoManuConnectManagerHandler = mHandler;
        if (oppoManuConnectManagerHandler != null) {
            oppoManuConnectManagerHandler.obtainMessage(what).sendToTarget();
        }
    }

    private void removeMessage(int what) {
        OppoManuConnectManagerHandler oppoManuConnectManagerHandler = mHandler;
        if (oppoManuConnectManagerHandler != null) {
            oppoManuConnectManagerHandler.removeMessages(what);
        }
    }

    private void sendMessageDelayed(int what, long delay) {
        OppoManuConnectManagerHandler oppoManuConnectManagerHandler = mHandler;
        if (oppoManuConnectManagerHandler != null) {
            oppoManuConnectManagerHandler.sendMessageDelayed(oppoManuConnectManagerHandler.obtainMessage(what), delay);
        }
    }

    private void sendMessageDelayed(int what, int arg1, int arg2, Object obj, long delay) {
        OppoManuConnectManagerHandler oppoManuConnectManagerHandler = mHandler;
        if (oppoManuConnectManagerHandler != null) {
            oppoManuConnectManagerHandler.sendMessageDelayed(oppoManuConnectManagerHandler.obtainMessage(what, arg1, arg2, obj), delay);
        }
    }

    private void sendManuConnectTimeoutEvt() {
        long timeout = getManuConnectTimeout();
        this.mConnectTimeoutToken++;
        if (this.mConnectTimeoutToken >= TOKEN_MAX) {
            this.mConnectTimeoutToken = 0;
        }
        logd("sendManuConnectTimeoutEvt, mConnectTimeoutToken = " + this.mConnectTimeoutToken + ", timeout = " + timeout);
        sendMessageDelayed(1, this.mConnectTimeoutToken, -1, null, timeout);
    }

    private void cancelManuConnectTimeoutEvt() {
        logd("cancelManuConnectTimeoutEvt");
        this.mConnectTimeoutToken = 0;
        removeMessage(1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleManuConnectTimeout() {
        int state = getState();
        logd("handleManuConnectTimeout,currntState = " + state + "[" + getStateString(state) + "]");
        if (!isManuConnected()) {
            loge("manu connect timeout for " + getManuConnectTimeout() + "ms, reset manu connect!!");
            update(3, -1, -1, 1000);
        }
    }

    /* access modifiers changed from: private */
    public final class OppoManuConnectManagerHandler extends Handler {
        public OppoManuConnectManagerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                OppoManuConnectManager.this.loge("msg is null!!");
                return;
            }
            int what = msg.what;
            OppoManuConnectManager oppoManuConnectManager = OppoManuConnectManager.this;
            oppoManuConnectManager.logd("message = " + OppoManuConnectManager.this.getEventString(what));
            if (what == 1) {
                int token = msg.arg1;
                OppoManuConnectManager oppoManuConnectManager2 = OppoManuConnectManager.this;
                oppoManuConnectManager2.logd("token = " + token + ", mConnectTimeoutToken = " + OppoManuConnectManager.this.mConnectTimeoutToken);
                if (token != OppoManuConnectManager.this.mConnectTimeoutToken) {
                    OppoManuConnectManager.this.logd("token missmatch!!");
                } else {
                    OppoManuConnectManager.this.handleManuConnectTimeout();
                }
            }
        }
    }

    public OppoManuConnectManager() {
        update(-1, -1, -1, 1000);
        debugStringsInit();
        Handler handler = null;
        ClientModeImpl clientModeImpl = mClientModeImpl;
        handler = clientModeImpl != null ? clientModeImpl.getHandler() : handler;
        if (handler != null) {
            mHandler = new OppoManuConnectManagerHandler(handler.getLooper());
        }
    }

    public static void init(Context contxt, ClientModeImpl wsm, WifiConfigManager wcm, WifiRomUpdateHelper wrh) {
        mContext = contxt;
        mClientModeImpl = wsm;
        mWifiConfigManager = wcm;
        mWifiRomUpdateHelpler = wrh;
    }

    public static OppoManuConnectManager getInstance() {
        if (mContext == null || mClientModeImpl == null) {
            return null;
        }
        synchronized (OppoManuConnectManager.class) {
            if (mInstance == null) {
                mInstance = new OppoManuConnectManager();
            }
        }
        return mInstance;
    }
}
