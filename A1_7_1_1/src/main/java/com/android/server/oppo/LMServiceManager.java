package com.android.server.oppo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.oppo.hypnus.HypnusManager;
import com.oppo.luckymoney.LuckyMoneyHelper;
import com.oppo.luckymoney.LuckyMoneyHelper.VersionItem;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class LMServiceManager {
    private static boolean DEBUG = false;
    private static boolean GET_HASH = false;
    private static final String TAG = "LMServiceManager";
    private IBinder mBinder;
    private Context mContext;
    private boolean mEnable;
    private HypnusManager mHM;
    private LMServiceThreadHandler mHandler;
    private LuckyMoneyHelper mLMHelper;
    private final Object mLock;
    private long mMaintainTimeout;
    private SparseArray<Object> mPidArray;
    private PowerManager mPowerManager;
    private WifiManager mWifiManger;

    private class LMServiceThreadHandler extends Handler {
        private static final int MESSAGE_BOOST_DISABLE = 1;
        private static final int MESSAGE_BOOST_ENABLE_TIMEOUT = 0;
        private static final int MESSAGE_BOOST_MAINTAIN = 2;
        private static final int MESSAGE_SCREEN_OFF = 4;
        private static final int MESSAGE_SCREEN_ON = 3;

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
                                Message timeout_msg;
                                if (LMServiceManager.this.mPidArray.indexOfKey(pid) >= 0) {
                                    tmp = LMServiceManager.this.mPidArray.get(pid);
                                    LMServiceManager.this.mHandler.removeMessages(1, tmp);
                                    timeout_msg = LMServiceManager.this.mHandler.obtainMessage();
                                    timeout_msg.what = 1;
                                    timeout_msg.arg1 = pid;
                                    timeout_msg.obj = tmp;
                                    LMServiceManager.this.mHandler.sendMessageDelayed(timeout_msg, timeout);
                                } else {
                                    tmp = new Object();
                                    LMServiceManager.this.mPidArray.put(pid, tmp);
                                    timeout_msg = LMServiceManager.this.mHandler.obtainMessage();
                                    timeout_msg.what = 1;
                                    timeout_msg.arg1 = pid;
                                    timeout_msg.obj = tmp;
                                    LMServiceManager.this.mHandler.sendMessageDelayed(timeout_msg, timeout);
                                }
                                LMServiceManager.this.enableBoostLocked(pid, timeout);
                            }
                        }
                        LMServiceManager.this.mHandler.removeMessages(2);
                        int delay_timeout = LMServiceManager.this.mLMHelper.getDelayTimeout();
                        LMServiceManager.this.mMaintainTimeout = SystemClock.elapsedRealtime() + ((long) delay_timeout);
                        if (LMServiceManager.DEBUG) {
                            Slog.d(LMServiceManager.TAG, "wifi power save delay_timeout: " + delay_timeout);
                        }
                        if (delay_timeout > 0 && isInteractive) {
                            LMServiceManager.this.enableMaintainLocked();
                            LMServiceManager.this.mHandler.sendEmptyMessageDelayed(2, (long) delay_timeout);
                            return;
                        }
                        return;
                    case 1:
                        synchronized (LMServiceManager.this.mLock) {
                            int _pid = msg.arg1;
                            LMServiceManager.this.mPidArray.delete(_pid);
                            LMServiceManager.this.disableBoostLocked(_pid);
                        }
                        return;
                    case 2:
                        LMServiceManager.this.disableMaintain();
                        return;
                    case 3:
                        LMServiceManager.this.mHandler.removeMessages(2);
                        int remain_timeout = (int) (LMServiceManager.this.mMaintainTimeout - SystemClock.elapsedRealtime());
                        if (LMServiceManager.DEBUG) {
                            Slog.d(LMServiceManager.TAG, "SCREEN_ON wifi power save remain_timeout: " + remain_timeout);
                        }
                        if (remain_timeout > 0) {
                            LMServiceManager.this.enableMaintainLocked();
                            LMServiceManager.this.mHandler.sendEmptyMessageDelayed(2, (long) remain_timeout);
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
                    default:
                        return;
                }
            } catch (NullPointerException e) {
                Slog.d(LMServiceManager.TAG, "Exception in LMServiceThreadHandler.handleMessage: " + e);
            }
            Slog.d(LMServiceManager.TAG, "Exception in LMServiceThreadHandler.handleMessage: " + e);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.oppo.LMServiceManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.oppo.LMServiceManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.oppo.LMServiceManager.<clinit>():void");
    }

    private static native int nativeRaisePriorityDisable(int i);

    private static native int nativeRaisePriorityEnable(int i);

    public LMServiceManager(Context context, Handler mainHandler) {
        this.mEnable = true;
        this.mLMHelper = null;
        this.mHM = null;
        this.mWifiManger = null;
        this.mPowerManager = null;
        this.mMaintainTimeout = 0;
        this.mPidArray = new SparseArray(0);
        this.mLock = new Object();
        this.mBinder = new Stub() {
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
        this.mContext = context;
        this.mHandler = new LMServiceThreadHandler(mainHandler.getLooper());
        this.mLMHelper = new LuckyMoneyHelper(this.mContext);
        this.mHM = new HypnusManager();
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
        strBuilder.append(dumpToString());
        pw.println(strBuilder.toString());
    }

    private void enableBoostLocked(int pid, long timeout) {
        nativeRaisePriorityEnable(pid);
        if (this.mHM != null) {
            this.mHM.hypnusSetAction(19, (int) timeout);
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
            this.mWifiManger.setPowerSavingMode(false);
        }
    }

    private void disableMaintain() {
        if (DEBUG) {
            Slog.d(TAG, "wifi power save: true");
        }
        if (this.mWifiManger != null) {
            this.mWifiManger.setPowerSavingMode(true);
        }
    }
}
