package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import com.android.server.WifiRomUpdateHelper;
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
    private static Context mContext = null;
    private static OppoManuConnectManagerHandler mHandler;
    private static OppoManuConnectManager mInstance = null;
    private static WifiConfigManager mWifiConfigManager;
    private static WifiRomUpdateHelper mWifiRomUpdateHelpler;
    private static WifiStateMachine mWifiStateMachine;
    private String mBssid;
    private int mConnectTimeoutToken = 0;
    private HashMap<Integer, String> mEventStrHashMap = new HashMap();
    private int mNetId;
    private int mState;
    private HashMap<Integer, String> mStateStrHashMap = new HashMap();
    private int mType;
    private HashMap<Integer, String> mTypeStrHashMap = new HashMap();
    private int mUid;

    private final class OppoManuConnectManagerHandler extends Handler {
        public OppoManuConnectManagerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                OppoManuConnectManager.this.loge("msg is null!!");
                return;
            }
            int what = msg.what;
            OppoManuConnectManager.this.logd("message = " + OppoManuConnectManager.this.getEventString(what));
            switch (what) {
                case 1:
                    int token = msg.arg1;
                    OppoManuConnectManager.this.logd("token = " + token + ", mConnectTimeoutToken = " + OppoManuConnectManager.this.mConnectTimeoutToken);
                    if (token == OppoManuConnectManager.this.mConnectTimeoutToken) {
                        OppoManuConnectManager.this.handleManuConnectTimeout();
                        break;
                    } else {
                        OppoManuConnectManager.this.logd("token missmatch!!");
                        break;
                    }
            }
        }
    }

    public int getState() {
        return this.mState;
    }

    private void setState(int newState) {
        logd("set manu connect State = " + newState + " [" + getStateString(newState) + "]");
        if (newState >= 4) {
            logd("invalid state:" + newState);
        } else {
            this.mState = newState;
        }
    }

    public int getType() {
        return this.mType;
    }

    private void setType(int newType) {
        logd("set manu connect Type = " + newType + " [" + getTypeString(newType) + "]");
        if (newType >= 3) {
            logd("invalid type = " + newType);
        } else {
            this.mType = newType;
        }
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
        if (mContext != null) {
            mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
        logd("sendConnectModeChangeBroadcast " + manualConnect);
    }

    private void update(int state, int type, int netId, int uid) {
        int oldState = getState();
        setState(state);
        setType(type);
        setNetId(netId);
        setUid(uid);
        if (oldState == 0 && oldState != state) {
            cancelManuConnectTimeoutEvt();
        }
    }

    public void reset() {
        logd("manu connect manager reset!!");
        update(3, -1, -1, UID_DEFAULT);
    }

    public String toString() {
        return "manu connect: netId = " + this.mNetId + ",type = [" + this.mType + "]" + getTypeString(this.mType) + ",state = [" + this.mState + "]" + getStateString(this.mState);
    }

    private void debugStringsInit() {
        if (this.mStateStrHashMap != null) {
            this.mStateStrHashMap.put(Integer.valueOf(-1), "invalid");
            this.mStateStrHashMap.put(Integer.valueOf(0), STATE_STARTED_STR);
            this.mStateStrHashMap.put(Integer.valueOf(1), STATE_AUTH_COMPLETED_STR);
            this.mStateStrHashMap.put(Integer.valueOf(2), STATE_CONNECTED_STR);
            this.mStateStrHashMap.put(Integer.valueOf(3), STATE_DISCONNECTED_STR);
        }
        if (this.mTypeStrHashMap != null) {
            this.mTypeStrHashMap.put(Integer.valueOf(-1), "invalid");
            this.mTypeStrHashMap.put(Integer.valueOf(0), TYPE_SETTING_STR);
            this.mTypeStrHashMap.put(Integer.valueOf(1), TYPE_THIRDAPK_STR);
            this.mTypeStrHashMap.put(Integer.valueOf(2), TYPE_WPS_STR);
        }
        if (this.mEventStrHashMap != null) {
            this.mEventStrHashMap.put(Integer.valueOf(0), "");
            this.mEventStrHashMap.put(Integer.valueOf(1), "");
        }
    }

    private String getStateString(int state) {
        if (this.mStateStrHashMap == null || this.mStateStrHashMap.size() <= 0) {
            logd("mStateStrHashMap is null or empty!!");
            return "invalid";
        }
        String str = (String) this.mStateStrHashMap.get(Integer.valueOf(state));
        if (str == null) {
            return "invalid";
        }
        return str;
    }

    private String getTypeString(int type) {
        if (this.mTypeStrHashMap == null || this.mTypeStrHashMap.size() <= 0) {
            logd("mTypeStrHashMap is null or empty!!");
            return "invalid";
        }
        String str = (String) this.mTypeStrHashMap.get(Integer.valueOf(type));
        if (str == null) {
            return "invalid";
        }
        return str;
    }

    private String getEventString(int evt) {
        if (this.mEventStrHashMap == null || this.mEventStrHashMap.size() <= 0) {
            logd("mEventStrHashMap is null or empty!!");
            return EVT_UNKNOWN_STR;
        }
        String str = (String) this.mEventStrHashMap.get(Integer.valueOf(evt));
        if (str == null) {
            return EVT_UNKNOWN_STR;
        }
        return str;
    }

    public boolean isManuConnecting() {
        int state = getState();
        return state == 0 || 1 == state;
    }

    public boolean isManuConnected() {
        if (2 == getState()) {
            return true;
        }
        return false;
    }

    public boolean isManuConnect() {
        return !isManuConnected() ? isManuConnecting() : true;
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
            if (mWifiConfigManager != null) {
                tmpConf = mWifiConfigManager.getConfiguredNetwork(netId);
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
        return UID_DEFAULT;
    }

    public void handleNetworkDisabled(int netId) {
        logd("handleNetworkDisabled,netId = " + netId);
        if (-1 != netId) {
            if (netId != getNetId()) {
                logd("disabled netId is not the current manu connect netId!!");
                return;
            }
            if (isManuConnect()) {
                logi("manu connect netId disabled,clear manu connect state!!");
                update(3, -1, -1, UID_DEFAULT);
            }
        }
    }

    public void handleNetworkDeleted(int netId) {
        logd("handleNetworkDeleted,netId = " + netId);
        if (-1 != netId) {
            if (netId != getNetId()) {
                logd("deleted netId is not the current manu connect netId!!");
                return;
            }
            if (isManuConnect()) {
                logi("manu connect netId deleted,clear manu connect state!!");
                update(3, -1, -1, UID_DEFAULT);
            }
        }
    }

    public void handleWifiStateChanged(int wifiState) {
        logd("handleWifiStateChanged,wifiState = " + wifiState);
        if (1 == wifiState) {
            update(-1, -1, -1, UID_DEFAULT);
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
        StateChangeResult stateChangeResult = message.obj;
        if (stateChangeResult == null) {
            logd("stateChangeResult is null!");
            return;
        }
        SupplicantState state = stateChangeResult.state;
        int netId = stateChangeResult.networkId;
        logd("netId = " + netId + " state = " + state);
        if (-1 != netId && SupplicantState.COMPLETED == state) {
            if (netId == getNetId() && isManuConnect()) {
                setState(1);
                sendConnectModeChangeBroadcast(true);
            } else {
                sendConnectModeChangeBroadcast(false);
            }
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
                    update(3, -1, -1, UID_DEFAULT);
                }
            } else if (manuConnectId == netId && 2 == state) {
                update(3, -1, -1, UID_DEFAULT);
            }
        }
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

    private void logd(String str) {
        if (DEBUG) {
            Log.d(TAG, "debug:" + str);
        }
    }

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
        if (mWifiRomUpdateHelpler == null) {
            return CONNECT_TIMEOUT;
        }
        return mWifiRomUpdateHelpler.getLongValue("WIFI_MANU_CONNECT_CONNECT_TIMEOUT", Long.valueOf(CONNECT_TIMEOUT)).longValue();
    }

    private void sendMessage(int what) {
        if (mHandler != null) {
            mHandler.obtainMessage(what).sendToTarget();
        }
    }

    private void removeMessage(int what) {
        if (mHandler != null) {
            mHandler.removeMessages(what);
        }
    }

    private void sendMessageDelayed(int what, long delay) {
        if (mHandler != null) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(what), delay);
        }
    }

    private void sendMessageDelayed(int what, int arg1, int arg2, Object obj, long delay) {
        if (mHandler != null) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(what, arg1, arg2, obj), delay);
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

    private void handleManuConnectTimeout() {
        int state = getState();
        logd("handleManuConnectTimeout,currntState = " + state + "[" + getStateString(state) + "]");
        if (!isManuConnected()) {
            loge("manu connect timeout for " + getManuConnectTimeout() + "ms, reset manu connect!!");
            update(3, -1, -1, UID_DEFAULT);
        }
    }

    public OppoManuConnectManager() {
        update(-1, -1, -1, UID_DEFAULT);
        debugStringsInit();
        Handler handler = null;
        if (mWifiStateMachine != null) {
            handler = mWifiStateMachine.getHandler();
        }
        if (handler != null) {
            mHandler = new OppoManuConnectManagerHandler(handler.getLooper());
        }
    }

    public static void init(Context contxt, WifiStateMachine wsm, WifiConfigManager wcm, WifiRomUpdateHelper wrh) {
        mContext = contxt;
        mWifiStateMachine = wsm;
        mWifiConfigManager = wcm;
        mWifiRomUpdateHelpler = wrh;
    }

    public static OppoManuConnectManager getInstance() {
        if (mContext == null || mWifiStateMachine == null) {
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
