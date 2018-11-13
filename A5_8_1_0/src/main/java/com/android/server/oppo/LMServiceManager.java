package com.android.server.oppo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
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
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.app.ILMServiceManager.Stub;
import com.android.server.LocationManagerService;
import com.oppo.luckymoney.LuckyMoneyHelper;
import com.oppo.luckymoney.LuckyMoneyHelper.VersionItem;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class LMServiceManager {
    private static final int ACTION_BURST_ANR = 19;
    private static final String CLASS_HYPNUS_MANAGER_NAME = "com.oppo.hypnus.HypnusManager";
    private static boolean DEBUG = false;
    private static boolean GET_HASH = false;
    private static final String TAG = "LMServiceManager";
    private IBinder mBinder = new Stub() {
        public boolean enableBoost(int pid, int uid, int timeout, int code) {
            if (LMServiceManager.this.mEnable) {
                if (timeout == 0) {
                    timeout = LMServiceManager.this.mLMHelper.getBoostTimeout();
                    if (timeout == 0) {
                        return false;
                    }
                }
                Message msg = LMServiceManager.this.mHandler.obtainMessage();
                msg.what = 0;
                msg.arg1 = timeout;
                msg.arg2 = pid;
                msg.sendToTarget();
            } else if (LMServiceManager.DEBUG) {
                Slog.d(LMServiceManager.TAG, " disable.");
            }
            return LMServiceManager.this.mEnable;
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
            String cmd;
            if (args.length == 1) {
                cmd = args[0];
                if ("-d".equals(cmd)) {
                    LMServiceManager.this.mEnable = false;
                } else if ("-D".equals(cmd)) {
                    LMServiceManager.this.mEnable = true;
                } else {
                    writer.println("Invalid argument! Get detail help as bellow: -d disable -D enable");
                }
            } else if (args.length == 2) {
                cmd = args[0];
                if ("debug".equals(cmd)) {
                    LMServiceManager.DEBUG = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(args[1]);
                } else if ("hash".equals(cmd)) {
                    LMServiceManager.GET_HASH = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(args[1]);
                } else {
                    writer.println("Invalid argument! Get detail help as bellow:");
                }
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                LMServiceManager.this.dumpInternal(writer);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public Bundle getModeData(int type, int versionCode) {
            Bundle result = new Bundle();
            ArrayMap<Integer, ArrayMap<String, String>> modeMap = LMServiceManager.this.mLMHelper.getModeMap(type);
            int mode = 0;
            VersionItem myVi = null;
            for (VersionItem vi : LMServiceManager.this.mLMHelper.getVersionList(type)) {
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
                ArrayMap<String, String> modeValues = (ArrayMap) modeMap.get(Integer.valueOf(mode));
                if (modeValues != null) {
                    for (String key : modeValues.keySet()) {
                        result.putString(key, (String) modeValues.get(key));
                    }
                }
                if (mode == 2) {
                    String hashStr = (String) modeValues.get("hg_hash");
                    if (hashStr != null && hashStr.length() == 32) {
                        byte[] cc = new byte[(hashStr.length() / 2)];
                        int i = 0;
                        while (i < cc.length) {
                            try {
                                cc[i] = (byte) (Integer.parseInt(hashStr.substring(i * 2, (i * 2) + 2), 16) & 255);
                                i++;
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
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
            return result;
        }
    };
    private Context mContext;
    private boolean mEnable = true;
    private Object mHM = null;
    private LMServiceThreadHandler mHandler;
    private LuckyMoneyHelper mLMHelper = null;
    private final Object mLock = new Object();
    private long mMaintainTimeout = SystemClock.elapsedRealtime();
    private SparseArray<Object> mPidArray = new SparseArray(0);
    private PowerManager mPowerManager = null;
    private WifiManager mWifiManger = null;

    private class LMServiceThreadHandler extends Handler {
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
                                Object tmp;
                                Message timeoutMsg;
                                if (LMServiceManager.this.mPidArray.indexOfKey(pid) >= 0) {
                                    tmp = LMServiceManager.this.mPidArray.get(pid);
                                    LMServiceManager.this.mHandler.removeMessages(1, tmp);
                                    timeoutMsg = LMServiceManager.this.mHandler.obtainMessage();
                                    timeoutMsg.what = 1;
                                    timeoutMsg.arg1 = pid;
                                    timeoutMsg.obj = tmp;
                                    LMServiceManager.this.mHandler.sendMessageDelayed(timeoutMsg, timeout);
                                } else {
                                    tmp = new Object();
                                    LMServiceManager.this.mPidArray.put(pid, tmp);
                                    timeoutMsg = LMServiceManager.this.mHandler.obtainMessage();
                                    timeoutMsg.what = 1;
                                    timeoutMsg.arg1 = pid;
                                    timeoutMsg.obj = tmp;
                                    LMServiceManager.this.mHandler.sendMessageDelayed(timeoutMsg, timeout);
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
            Slog.d(LMServiceManager.TAG, "Exception in LMServiceThreadHandler.handleMessage: " + e);
        }
    }

    private static native int nativeRaisePriorityDisable(int i);

    private static native int nativeRaisePriorityEnable(int i);

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
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(new BroadcastReceiver() {
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
        filter = new IntentFilter();
        filter.addAction("android.net.wifi.STATE_CHANGE");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
                    DetailedState state = ((NetworkInfo) intent.getParcelableExtra("networkInfo")).getDetailedState();
                    if (state == DetailedState.CONNECTED) {
                        LMServiceManager.this.mHandler.sendEmptyMessage(5);
                    } else if (state == DetailedState.DISCONNECTED) {
                        LMServiceManager.this.mHandler.sendEmptyMessage(6);
                    }
                }
            }
        }, filter);
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
    }

    public String dumpToString() {
        return this.mLMHelper.dumpToString();
    }

    private void dumpInternal(PrintWriter pw) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Enable: ").append(this.mEnable).append("\n");
        strBuilder.append("Get Hash: ").append(GET_HASH).append("\n");
        strBuilder.append("DEBUG: ").append(DEBUG).append("\n");
        if (DEBUG) {
            strBuilder.append(dumpToString());
        }
        pw.println(strBuilder.toString());
    }

    private void enableBoostLocked(int pid, long timeout) {
        nativeRaisePriorityEnable(pid);
        if (this.mHM != null) {
            try {
                this.mHM.getClass().getMethod("hypnusSetAction", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(Integer.valueOf(19), new Object[]{Integer.valueOf((int) timeout)});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void disableBoostLocked(int pid) {
        nativeRaisePriorityDisable(pid);
    }

    private void enableMaintainLocked() {
        if (DEBUG) {
            Slog.d(TAG, "wifi power save: false");
        }
        if (this.mWifiManger != null) {
            try {
                this.mWifiManger.getClass().getMethod("setPowerSavingMode", new Class[]{Boolean.TYPE}).invoke(Boolean.valueOf(false), new Object[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void disableMaintain() {
        if (DEBUG) {
            Slog.d(TAG, "wifi power save: true");
        }
        if (this.mWifiManger != null) {
            try {
                this.mWifiManger.getClass().getMethod("setPowerSavingMode", new Class[]{Boolean.TYPE}).invoke(Boolean.valueOf(true), new Object[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
