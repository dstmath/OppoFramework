package com.android.server.oppo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.app.ILMServiceManager;
import com.android.internal.telephony.ITelephony;
import com.android.server.biometrics.face.health.HealthState;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.theia.NoFocusWindow;
import com.oppo.luckymoney.LuckyMoneyHelper;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LMServiceManager {
    private static final int ACTION_BURST_ANR = 19;
    private static final String CLASS_HYPNUS_MANAGER_NAME = "com.oppo.hypnus.HypnusManager";
    private static boolean DEBUG = false;
    private static boolean GET_HASH = false;
    private static final String METHOD_START_MOBILE_DATA_POLICY = "startMobileDataHongbaoPolicy";
    private static final int MOBILE_POLICY_LAST_TIME = 300;
    private static final int PER_PING_TIME = 8;
    private static final String TAG = "LMServiceManager";
    private static ITelephony sTelManager = null;
    private IBinder mBinder = new ILMServiceManager.Stub() {
        /* class com.android.server.oppo.LMServiceManager.AnonymousClass3 */

        public void writeDCS(Bundle data) {
            Slog.d(LMServiceManager.TAG, "writeDCS");
            if (data != null) {
                HashMap<String, String> map = new HashMap<>();
                map.put("TYPE", data.getString("TYPE", ""));
                map.put("DETECT_RETURN", data.getString("DETECT_RETURN", ""));
                map.put("SPEND_TIME", data.getString("SPEND_TIME", "0"));
                map.put("CURRENT_TIME", data.getString("CURRENT_TIME", "00"));
                Slog.d(LMServiceManager.TAG, "writeDCS onCommon");
                try {
                    Method onCommonMtd = Class.forName("oppo.util.OppoStatistics").getMethod("onCommon", Context.class, String.class, String.class, Map.class, Boolean.TYPE);
                    onCommonMtd.setAccessible(true);
                    onCommonMtd.invoke(null, LMServiceManager.this.mContext, "2016101", "LuckyMoney", map, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public boolean enableBoost(int pid, int uid, int timeout, int code) {
            if (!LMServiceManager.this.mEnable) {
                if (LMServiceManager.DEBUG) {
                    Slog.d(LMServiceManager.TAG, " disable.");
                }
            } else if (timeout == 0 && (timeout = LMServiceManager.this.mLMHelper.getBoostTimeout()) == 0) {
                return false;
            } else {
                Message msg = LMServiceManager.this.mHandler.obtainMessage();
                msg.what = 0;
                msg.arg1 = timeout;
                msg.arg2 = pid;
                msg.sendToTarget();
            }
            return LMServiceManager.this.mEnable;
        }

        public boolean enableMobileBoost() {
            long ident = Binder.clearCallingIdentity();
            boolean mobileBoostEnabled = false;
            if (LMServiceManager.DEBUG) {
                Slog.d(LMServiceManager.TAG, "enableMobileBoost");
            }
            try {
                if (SystemProperties.get("sys.oppo.nw.hongbao", "0").equals(NoFocusWindow.HUNG_CONFIG_ENABLE)) {
                    Slog.d(LMServiceManager.TAG, "Mobile Boost enabled");
                    if (LMServiceManager.sTelManager == null) {
                        ITelephony unused = LMServiceManager.sTelManager = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
                    }
                    if (LMServiceManager.sTelManager != null) {
                        try {
                            Method func = LMServiceManager.sTelManager.getClass().getMethod(LMServiceManager.METHOD_START_MOBILE_DATA_POLICY, Integer.TYPE, Integer.TYPE, String.class, String.class);
                            if (func == null) {
                                Slog.d(LMServiceManager.TAG, "Method startMobileDataHongbaoPolicy not found");
                            } else {
                                func.invoke(LMServiceManager.sTelManager, 300, 8, null, null);
                                mobileBoostEnabled = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Slog.e(LMServiceManager.TAG, "cannot get sTelManager");
                    }
                }
                return mobileBoostEnabled;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public String getLuckyMoneyInfo(int type) {
            return LMServiceManager.this.mLMHelper.getLuckyMoneyInfo(type);
        }

        public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
            if (LMServiceManager.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                writer.println("Permission Denial: can't dump PowerManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            if (LMServiceManager.DEBUG) {
                Slog.d(LMServiceManager.TAG, "dump, args=" + args);
            }
            if (args.length == 1) {
                String cmd = args[0];
                if ("-d".equals(cmd)) {
                    LMServiceManager.this.mEnable = false;
                } else if ("-D".equals(cmd)) {
                    LMServiceManager.this.mEnable = true;
                } else {
                    writer.println("Invalid argument! Get detail help as bellow: -d disable -D enable");
                }
            } else if (args.length == 2) {
                String cmd2 = args[0];
                if (HealthState.DEBUG.equals(cmd2)) {
                    boolean unused = LMServiceManager.DEBUG = NoFocusWindow.HUNG_CONFIG_ENABLE.equals(args[1]);
                    return;
                } else if ("hash".equals(cmd2)) {
                    boolean unused2 = LMServiceManager.GET_HASH = NoFocusWindow.HUNG_CONFIG_ENABLE.equals(args[1]);
                    return;
                } else {
                    writer.println("Invalid argument! Get detail help as bellow:");
                    return;
                }
            }
            long ident = Binder.clearCallingIdentity();
            try {
                LMServiceManager.this.dumpInternal(writer);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public Bundle getSwitchInfo() {
            Bundle result = new Bundle();
            boolean lmEnable = LMServiceManager.this.mLMHelper.getLuckyMoneyIsEnable();
            boolean smEnable = LMServiceManager.this.mLMHelper.getSwitchModeEnable();
            result.putBoolean("isEnable", lmEnable);
            result.putBoolean("smEnable", smEnable);
            return result;
        }

        public Bundle getModeEnableInfo(int type, int versionCode) {
            LuckyMoneyHelper.AppVersionItem appVersionItem;
            Bundle result = new Bundle();
            HashMap<Integer, Boolean> modeEnableInfo = new HashMap<>();
            LuckyMoneyHelper.AppItem appItem = LMServiceManager.this.mLMHelper.getAppItem(type);
            if (appItem == null || (appVersionItem = appItem.getAppVersionItem(versionCode)) == null) {
                return result;
            }
            Iterator<LuckyMoneyHelper.LuckMoneyMode> it = appVersionItem.getLuckMoneyModes().iterator();
            while (it.hasNext()) {
                LuckyMoneyHelper.LuckMoneyMode luckMoneyMode = it.next();
                modeEnableInfo.put(Integer.valueOf(luckMoneyMode.getId()), Boolean.valueOf(luckMoneyMode.getModeEnable()));
            }
            result.putSerializable("modeEnableInfo", modeEnableInfo);
            return result;
        }

        public boolean isInitialized() {
            return LMServiceManager.this.mInitialized;
        }

        /* JADX WARNING: Removed duplicated region for block: B:52:0x01a3  */
        public Bundle getModeData(int type, int versionCode, int defaultMode) {
            NumberFormatException e;
            LuckyMoneyHelper.AppVersionItem appVersionItem;
            LuckyMoneyHelper.LuckMoneyMode modeItem;
            Bundle result = new Bundle();
            int luckyMoneyXmlVersion = LMServiceManager.this.mLMHelper.getLuckyMoneyXMLVerion();
            boolean lmEnable = LMServiceManager.this.mLMHelper.getLuckyMoneyIsEnable();
            boolean smEnable = LMServiceManager.this.mLMHelper.getSwitchModeEnable();
            result.putInt("xmlVersion", luckyMoneyXmlVersion);
            result.putBoolean("isEnable", lmEnable);
            result.putBoolean("smEnable", smEnable);
            if (luckyMoneyXmlVersion == 1) {
                LuckyMoneyHelper.AppItem appItem = LMServiceManager.this.mLMHelper.getAppItem(type);
                if (appItem == null || (appVersionItem = appItem.getAppVersionItem(versionCode)) == null || (modeItem = appVersionItem.getLuckMoneyMode(defaultMode)) == null) {
                    return result;
                }
                result.putInt("mode", modeItem.getId());
                result.putBoolean("isModeEnable", modeItem.getModeEnable());
                result.putString("chatView", appItem.getChatView());
                result.putString("receiver_class", appItem.getReceiverClass());
                result.putStringArrayList("openHbActivity", appItem.getOpenHbActivity());
                result.putIntegerArrayList("hbHeights", modeItem.getHbHeight());
                result.putIntegerArrayList("hbWidths", modeItem.getHbWidth());
                result.putStringArrayList("hbHashs", modeItem.getHbHash());
                result.putString("hbText", modeItem.getHbText());
                result.putStringArrayList("hbLayout", modeItem.getHbLayout());
                result.putIntegerArrayList("hbLayoutNodes", modeItem.getHbLayoutNodes());
                result.putBoolean("get_hash", LMServiceManager.GET_HASH);
            } else {
                ArrayMap<Integer, ArrayMap<String, String>> modeMap = LMServiceManager.this.mLMHelper.getModeMap(type);
                int mode = 0;
                LuckyMoneyHelper.VersionItem myVi = null;
                Iterator<LuckyMoneyHelper.VersionItem> it = LMServiceManager.this.mLMHelper.getVersionList(type).iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    LuckyMoneyHelper.VersionItem vi = it.next();
                    int start = vi.getStart();
                    int end = vi.getEnd();
                    if (versionCode >= start && versionCode < end) {
                        mode = vi.getMode();
                        myVi = vi;
                        break;
                    }
                }
                result.putInt("mode", mode);
                result.putBoolean("get_hash", LMServiceManager.GET_HASH);
                if (mode > 0) {
                    ArrayMap<String, String> modeValues = modeMap.get(Integer.valueOf(mode));
                    if (modeValues != null) {
                        for (String key : modeValues.keySet()) {
                            result.putString(key, modeValues.get(key));
                        }
                    }
                    if (mode == 2 && modeValues != null) {
                        String hashStr = modeValues.get("hg_hash");
                        if (hashStr != null && hashStr.length() == 32) {
                            byte[] cc = new byte[(hashStr.length() / 2)];
                            int i = 0;
                            while (i < cc.length) {
                                try {
                                    try {
                                        cc[i] = (byte) (Integer.parseInt(hashStr.substring(i * 2, (i * 2) + 2), 16) & 255);
                                        i++;
                                        modeValues = modeValues;
                                    } catch (NumberFormatException e2) {
                                        e = e2;
                                        e.printStackTrace();
                                        result.putByteArray("hg_hash", cc);
                                        if (myVi != null) {
                                        }
                                        return result;
                                    }
                                } catch (NumberFormatException e3) {
                                    e = e3;
                                    e.printStackTrace();
                                    result.putByteArray("hg_hash", cc);
                                    if (myVi != null) {
                                    }
                                    return result;
                                }
                            }
                            result.putByteArray("hg_hash", cc);
                        }
                    }
                    if (myVi != null) {
                        for (String key2 : myVi.getSpecValues().keySet()) {
                            result.putString(key2, (String) myVi.getSpecValues().get(key2));
                        }
                    }
                }
            }
            return result;
        }
    };
    private Context mContext;
    private boolean mEnable = true;
    private Object mHM = null;
    private LMServiceThreadHandler mHandler;
    private boolean mInitialized = false;
    private LuckyMoneyHelper mLMHelper = null;
    private final Object mLock = new Object();
    private long mMaintainTimeout = SystemClock.elapsedRealtime();
    private SparseArray<Object> mPidArray = new SparseArray<>(0);
    private PowerManager mPowerManager = null;
    private WifiManager mWifiManger = null;

    private static native int nativeRaisePriorityDisable(int i);

    private static native int nativeRaisePriorityEnable(int i);

    /* JADX WARN: Type inference failed for: r0v4, types: [com.android.server.oppo.LMServiceManager$3, android.os.IBinder] */
    public LMServiceManager(Context context, Handler mainHandler) {
        this.mContext = context;
        this.mHandler = new LMServiceThreadHandler(mainHandler.getLooper());
        this.mLMHelper = new LuckyMoneyHelper(this.mContext);
        try {
            this.mHM = Class.forName(CLASS_HYPNUS_MANAGER_NAME).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mWifiManger = (WifiManager) this.mContext.getSystemService("wifi");
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        sTelManager = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.oppo.LMServiceManager.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (((int) (LMServiceManager.this.mMaintainTimeout - SystemClock.elapsedRealtime())) <= 0) {
                    return;
                }
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    LMServiceManager.this.mHandler.sendEmptyMessage(3);
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    LMServiceManager.this.mHandler.sendEmptyMessage(4);
                }
            }
        }, filter);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.net.wifi.STATE_CHANGE");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.oppo.LMServiceManager.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
                    NetworkInfo.DetailedState state = ((NetworkInfo) intent.getParcelableExtra("networkInfo")).getDetailedState();
                    if (state == NetworkInfo.DetailedState.CONNECTED) {
                        LMServiceManager.this.mHandler.sendEmptyMessage(5);
                    } else if (state == NetworkInfo.DetailedState.DISCONNECTED) {
                        LMServiceManager.this.mHandler.sendEmptyMessage(6);
                    }
                }
            }
        }, filter2);
        initService();
    }

    private void initService() {
        try {
            Slog.i(TAG, "Start Service");
            ServiceManager.addService("luckymoney", this.mBinder);
        } catch (Throwable e) {
            Slog.i(TAG, "Start Service failed", e);
        }
    }

    public void systemReady() {
        this.mLMHelper.initUpdateBroadcastReceiver();
        this.mInitialized = true;
    }

    public String dumpToString() {
        return this.mLMHelper.dumpToString();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpInternal(PrintWriter pw) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Enable: " + this.mEnable + StringUtils.LF);
        strBuilder.append("Get Hash: " + GET_HASH + StringUtils.LF);
        strBuilder.append("DEBUG: " + DEBUG + StringUtils.LF);
        if (DEBUG) {
            strBuilder.append(dumpToString());
        }
        pw.println(strBuilder.toString());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableBoostLocked(int pid, long timeout) {
        try {
            nativeRaisePriorityEnable(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Object obj = this.mHM;
        if (obj != null) {
            try {
                obj.getClass().getMethod("hypnusSetAction", Integer.TYPE, Integer.TYPE).invoke(this.mHM, 19, Integer.valueOf((int) timeout));
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disableBoostLocked(int pid) {
        try {
            nativeRaisePriorityDisable(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableMaintainLocked() {
        if (DEBUG) {
            Slog.d(TAG, "wifi power save: false");
        }
        WifiManager wifiManager = this.mWifiManger;
        if (wifiManager != null) {
            try {
                wifiManager.getClass().getMethod("setPowerSavingMode", Boolean.TYPE).invoke(this.mWifiManger, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disableMaintain() {
        if (DEBUG) {
            Slog.d(TAG, "wifi power save: true");
        }
        WifiManager wifiManager = this.mWifiManger;
        if (wifiManager != null) {
            try {
                wifiManager.getClass().getMethod("setPowerSavingMode", Boolean.TYPE).invoke(this.mWifiManger, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    public class LMServiceThreadHandler extends Handler {
        private static final int MESSAGE_BOOST_DISABLE = 1;
        private static final int MESSAGE_BOOST_ENABLE_TIMEOUT = 0;
        private static final int MESSAGE_BOOST_MAINTAIN = 2;
        private static final int MESSAGE_SCREEN_OFF = 4;
        private static final int MESSAGE_SCREEN_ON = 3;
        private static final int MESSAGE_WIFI_CONECTED = 5;
        private static final int MESSAGE_WIFI_DISCONECTED = 6;
        private boolean mIsWifiConected = false;

        public LMServiceThreadHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 0:
                        long timeout = (long) msg.arg1;
                        int pid = msg.arg2;
                        boolean isInteractive = LMServiceManager.this.mPowerManager.isInteractive();
                        if (LMServiceManager.DEBUG) {
                            Slog.d(LMServiceManager.TAG, "isInteractive: " + isInteractive);
                        }
                        synchronized (LMServiceManager.this.mLock) {
                            if (isInteractive) {
                                if (LMServiceManager.this.mPidArray.indexOfKey(pid) >= 0) {
                                    Object tmp = LMServiceManager.this.mPidArray.get(pid);
                                    LMServiceManager.this.mHandler.removeMessages(1, tmp);
                                    Message timeoutMsg = LMServiceManager.this.mHandler.obtainMessage();
                                    timeoutMsg.what = 1;
                                    timeoutMsg.arg1 = pid;
                                    timeoutMsg.obj = tmp;
                                    LMServiceManager.this.mHandler.sendMessageDelayed(timeoutMsg, timeout);
                                } else {
                                    Object tmp2 = new Object();
                                    LMServiceManager.this.mPidArray.put(pid, tmp2);
                                    Message timeoutMsg2 = LMServiceManager.this.mHandler.obtainMessage();
                                    timeoutMsg2.what = 1;
                                    timeoutMsg2.arg1 = pid;
                                    timeoutMsg2.obj = tmp2;
                                    LMServiceManager.this.mHandler.sendMessageDelayed(timeoutMsg2, timeout);
                                }
                                LMServiceManager.this.enableBoostLocked(pid, timeout);
                            }
                        }
                        LMServiceManager.this.mHandler.removeMessages(2);
                        int delayTimeout = LMServiceManager.this.mLMHelper.getDelayTimeout();
                        LMServiceManager.this.mMaintainTimeout = SystemClock.elapsedRealtime() + ((long) delayTimeout);
                        if (LMServiceManager.DEBUG) {
                            Slog.d(LMServiceManager.TAG, "wifi power save delayTimeout: " + delayTimeout);
                        }
                        if (delayTimeout > 0 && isInteractive && this.mIsWifiConected) {
                            LMServiceManager.this.enableMaintainLocked();
                            LMServiceManager.this.mHandler.sendEmptyMessageDelayed(2, (long) delayTimeout);
                            return;
                        }
                        return;
                    case 1:
                        synchronized (LMServiceManager.this.mLock) {
                            int iPid = msg.arg1;
                            LMServiceManager.this.mPidArray.delete(iPid);
                            LMServiceManager.this.disableBoostLocked(iPid);
                        }
                        return;
                    case 2:
                        LMServiceManager.this.disableMaintain();
                        return;
                    case 3:
                        LMServiceManager.this.mHandler.removeMessages(2);
                        int remainTimeout = (int) (LMServiceManager.this.mMaintainTimeout - SystemClock.elapsedRealtime());
                        if (LMServiceManager.DEBUG) {
                            Slog.d(LMServiceManager.TAG, "SCREEN_ON wifi power save remainTimeout: " + remainTimeout);
                        }
                        if (remainTimeout > 0 && this.mIsWifiConected) {
                            LMServiceManager.this.enableMaintainLocked();
                            LMServiceManager.this.mHandler.sendEmptyMessageDelayed(2, (long) remainTimeout);
                            return;
                        }
                        return;
                    case 4:
                        LMServiceManager.this.mHandler.removeMessages(2);
                        if (LMServiceManager.DEBUG) {
                            Slog.d(LMServiceManager.TAG, "SCREEN_OFF wifi power save");
                        }
                        LMServiceManager.this.disableMaintain();
                        return;
                    case 5:
                        if (LMServiceManager.DEBUG) {
                            Slog.d(LMServiceManager.TAG, "wifi CONNECTED.");
                        }
                        this.mIsWifiConected = true;
                        if (LMServiceManager.this.mPowerManager.isInteractive()) {
                            LMServiceManager.this.mHandler.sendEmptyMessage(3);
                            return;
                        } else {
                            LMServiceManager.this.mHandler.sendEmptyMessage(4);
                            return;
                        }
                    case 6:
                        if (LMServiceManager.DEBUG) {
                            Slog.d(LMServiceManager.TAG, "wifi DISCONNECTED.");
                        }
                        if (((int) (LMServiceManager.this.mMaintainTimeout - SystemClock.elapsedRealtime())) > 0 && this.mIsWifiConected) {
                            if (LMServiceManager.DEBUG) {
                                Slog.d(LMServiceManager.TAG, "During LM window, enable wifi power save.");
                            }
                            LMServiceManager.this.mHandler.removeMessages(2);
                            LMServiceManager.this.disableMaintain();
                        }
                        this.mIsWifiConected = false;
                        return;
                    default:
                        return;
                }
            } catch (NullPointerException e) {
                Slog.d(LMServiceManager.TAG, "Exception in LMServiceThreadHandler.handleMessage: " + e);
            }
        }
    }
}
