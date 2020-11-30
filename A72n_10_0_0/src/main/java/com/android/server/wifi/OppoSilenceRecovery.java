package com.android.server.wifi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.wifi.V1_3.IWifiChip;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.Handler;
import android.os.Message;
import android.os.OppoAssertTip;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.wifi.OppoWifiOCloudImpl;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;
import oppo.util.OppoStatistics;

public class OppoSilenceRecovery {
    private static final String ACTION_DETECT_NETWORK_ACCESS = "oppo.intent.action.DETECT_NET_ACCESS";
    private static final String ACTION_RECOVERY_WIFI = "oppo.intent.action.RECOVERY_WIFI";
    private static final String ACTION_RECOVERY_WIFI_OTHER = "oppo.intent.action.Other.RECOVERY_WIFI";
    private static boolean DEBUG = true;
    private static final int LATER_MINS_DETECT_AGAIN = 15;
    private static final int MAX_DERECT_TIMERS = 60;
    private static final int MAX_RETRY_DETECT_COUNT = 3;
    private static final int OPPO_SLIENCI_RECOVERY = 8;
    private static final String TAG = "OppoSilenceRecovery";
    private static final int TRAFFIC_RX_LOWEST = 80;
    private static final int TRAFFIC_TX_LOWEST = 50;
    private static final int TRIGER_RESTART_THIS_MORNING = 1;
    private static final int TRIGER_RESTART_TIMER_END = 6;
    private static final int TRIGER_RESTART_TIME_START = 0;
    private static final String TRIGGER_RECOVERY_MODE_MTK = "mtk";
    private static final String TRIGGER_RECOVERY_MODE_NONE = "none";
    private static final String TRIGGER_RECOVERY_MODE_QCOM = "qcom";
    private static Boolean mNeedFwRecovery = false;
    private static String mRecoveryMode = "none";
    private static String mRecoveryType = "general";
    private AlarmManager mAlarmManager;
    private OppoAssertTip mAssertProxy;
    private Context mContext;
    private PendingIntent mDetectIntent;
    private Excecuter mExcecuter;
    private Handler mHandler;
    private long mLastScreenOff = 0;
    private Boolean mNetWorkConnected = false;
    private Boolean mOtherRequest = false;
    private PacketInfo mPktInfo;
    private PendingIntent mRecoveryIntent;
    private PendingIntent mRecoveryIntentOther;
    private int mResetAlarmCount = 0;
    private boolean mScreenOn = true;
    private PendingIntent mStartIntent;
    private PendingIntent mStopIntent;
    private WifiInjector mWifiInjector;
    private WifiNative mWifiNative;
    private WifiRomUpdateHelper mWifiRomUpdateHelper = null;
    private int netWorkCount = 3;

    static /* synthetic */ int access$1608(OppoSilenceRecovery x0) {
        int i = x0.mResetAlarmCount;
        x0.mResetAlarmCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$610(OppoSilenceRecovery x0) {
        int i = x0.netWorkCount;
        x0.netWorkCount = i - 1;
        return i;
    }

    OppoSilenceRecovery(Context c, WifiInjector injector, WifiNative wn) {
        this.mContext = c;
        this.mWifiNative = wn;
        this.mWifiInjector = injector;
        this.mPktInfo = new PacketInfo();
        this.mExcecuter = new Excecuter();
        this.mAssertProxy = OppoAssertTip.getInstance();
        this.mAlarmManager = (AlarmManager) c.getSystemService("alarm");
        this.mRecoveryIntent = PendingIntent.getBroadcast(c, 0, new Intent(ACTION_RECOVERY_WIFI, (Uri) null), IWifiChip.ChipCapabilityMask.DUAL_BAND_DUAL_CHANNEL);
        this.mDetectIntent = PendingIntent.getBroadcast(c, 0, new Intent(ACTION_DETECT_NETWORK_ACCESS, (Uri) null), IWifiChip.ChipCapabilityMask.DUAL_BAND_DUAL_CHANNEL);
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(ACTION_RECOVERY_WIFI);
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction(ACTION_RECOVERY_WIFI_OTHER);
        filter.addAction(ACTION_DETECT_NETWORK_ACCESS);
        c.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.OppoSilenceRecovery.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (OppoSilenceRecovery.DEBUG) {
                    Log.d(OppoSilenceRecovery.TAG, OppoWifiOCloudImpl.SimpleWifiConfig.ACTION + action);
                }
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    OppoSilenceRecovery.this.mScreenOn = true;
                    OppoSilenceRecovery.this.mLastScreenOff = 0;
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    OppoSilenceRecovery.this.mScreenOn = false;
                    OppoSilenceRecovery.this.mLastScreenOff = System.currentTimeMillis();
                } else if (action.equals(OppoSilenceRecovery.ACTION_RECOVERY_WIFI)) {
                    OppoSilenceRecovery.this.recoveryWifi();
                } else if (action.equals(OppoSilenceRecovery.ACTION_RECOVERY_WIFI_OTHER)) {
                    OppoSilenceRecovery.this.mOtherRequest = true;
                    OppoSilenceRecovery.this.recoveryWifi();
                } else if (action.equals(OppoSilenceRecovery.ACTION_DETECT_NETWORK_ACCESS)) {
                    OppoSilenceRecovery.this.hasNetworkAccessing();
                } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    NetworkInfo.DetailedState state = ((NetworkInfo) intent.getParcelableExtra("networkInfo")).getDetailedState();
                    if (OppoSilenceRecovery.DEBUG) {
                        Log.d(OppoSilenceRecovery.TAG, "wifi connect state =" + state);
                    }
                    if (state == NetworkInfo.DetailedState.CONNECTED) {
                        OppoSilenceRecovery.this.mNetWorkConnected = true;
                    } else if (state == NetworkInfo.DetailedState.DISCONNECTED) {
                        OppoSilenceRecovery.this.mNetWorkConnected = false;
                    } else if (OppoSilenceRecovery.DEBUG) {
                        Log.d(OppoSilenceRecovery.TAG, "wifi connect other state");
                    }
                } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                    int wifiState = intent.getIntExtra("wifi_state", 4);
                    if (OppoSilenceRecovery.DEBUG) {
                        Log.d(OppoSilenceRecovery.TAG, "wifi wifiState = " + wifiState);
                    }
                    if (wifiState == 3) {
                        OppoSilenceRecovery.this.enableFoolProof(true);
                    } else if (wifiState == 1) {
                        OppoSilenceRecovery.this.enableFoolProof(false);
                    } else if (OppoSilenceRecovery.DEBUG) {
                        Log.d(OppoSilenceRecovery.TAG, "wifi other state");
                    }
                }
            }
        }, filter);
        this.mHandler = new Handler() {
            /* class com.android.server.wifi.OppoSilenceRecovery.AnonymousClass2 */

            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 8) {
                    boolean result = ((Boolean) msg.obj).booleanValue();
                    if (OppoSilenceRecovery.DEBUG) {
                        Log.d(OppoSilenceRecovery.TAG, "handleMessage msg.what = " + msg.what + " result=" + result + " mOther=" + OppoSilenceRecovery.this.mOtherRequest + " Count=" + OppoSilenceRecovery.this.netWorkCount);
                    }
                    if (result || OppoSilenceRecovery.this.netWorkCount == 0 || !OppoSilenceRecovery.this.mOtherRequest.booleanValue()) {
                        if (OppoSilenceRecovery.this.mOtherRequest.booleanValue() && !result) {
                            result = OppoSilenceRecovery.this.hasEnoughSleep();
                        }
                        OppoSilenceRecovery.this.netWorkCount = 3;
                        OppoSilenceRecovery.this.netWorkRestart(result);
                        return;
                    }
                    OppoSilenceRecovery.access$610(OppoSilenceRecovery.this);
                    OppoSilenceRecovery.this.mExcecuter.detectWifi();
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean hasEnoughSleep() {
        boolean enoughSleep = false;
        if (this.mLastScreenOff > 0 && System.currentTimeMillis() - this.mLastScreenOff >= 2700000) {
            enoughSleep = true;
        }
        Log.d(TAG, "hasEnoughSleep " + enoughSleep);
        return enoughSleep;
    }

    public Integer getRomUpdateIntegerValue(String key, Integer defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getIntegerValue(key, defaultVal);
        }
        return defaultVal;
    }

    /* access modifiers changed from: package-private */
    public void enableFoolProof(boolean enable) {
        this.mResetAlarmCount = 0;
        if (getRomUpdateIntegerValue("BASIC_FOOL_PROOF_ON", 1).intValue() != 1) {
            Log.d(TAG, "foolProofOn != 1, don't restart!");
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "enableFoolProof enable = " + enable);
        }
        if (enable) {
            this.mExcecuter.startRecoveryTrigger(3);
        } else {
            this.mExcecuter.cancelTrigger();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean ifPermitRecovery() {
        if (!checkTimeInMorning() || this.mScreenOn) {
            return false;
        }
        return true;
    }

    public void trigger_fw_recovery(String recoveryMode, String recoveryType) {
        if (recoveryMode.equals("none")) {
            Log.e(TAG, "trigger_fw_recovery none, assert......");
            return;
        }
        this.mOtherRequest = true;
        mNeedFwRecovery = true;
        mRecoveryMode = recoveryMode;
        mRecoveryType = recoveryType;
        recoveryWifi();
    }

    /* access modifiers changed from: package-private */
    public void recoveryWifi() {
        if (!ifPermitRecovery()) {
            this.mExcecuter.startRecoveryTrigger(1);
        } else if (this.mNetWorkConnected.booleanValue()) {
            hasNetworkAccessing();
        } else {
            Log.e(TAG, "silence recovery ,wifi will recoery later... ... ");
            setStatistics("silence", "wifi_restart_in_silence");
            executeRecovery();
            this.mOtherRequest = false;
        }
    }

    private boolean checkTimeInMorning() {
        int hour = Calendar.getInstance().get(11);
        if (DEBUG) {
            Log.d(TAG, "checkTimeInMorning hour = " + hour);
        }
        if (hour >= 6 || hour <= 0) {
            return false;
        }
        return true;
    }

    public void setStatistics(String mapValue, String eventId) {
        HashMap<String, String> map = new HashMap<>();
        map.put("mapKey-", mapValue);
        if (DEBUG) {
            Log.d(TAG, "fool-proof, onCommon eventId = " + eventId);
        }
        OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", eventId, map, false);
    }

    public void reportFoolProofException() {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.cta.support")) {
            Log.d(TAG, "fool-proof, CTA version don't reportFoolProofException");
            return;
        }
        if (getLoggingLevel() == 0) {
            ((WifiManager) this.mContext.getSystemService("wifi")).enableVerboseLogging(1);
        }
        RuntimeException excp = new RuntimeException("Please send this log to Yuanliu.Tang of wifi team,thank you!");
        excp.fillInStackTrace();
        this.mAssertProxy.requestShowAssertMessage(Log.getStackTraceString(excp));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void netWorkRestart(boolean enable) {
        if (!enable || !ifPermitRecovery()) {
            if (DEBUG) {
                Log.d(TAG, "netWorkRestart later do---tomorrow");
            }
            this.mExcecuter.resetRestartAlarm();
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "silence recovery network connect wifi wifi will recovery ");
        }
        setStatistics("silence_connected", "wifi_restart_in_silence_connected");
        executeRecovery();
        this.mOtherRequest = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hasNetworkAccessing() {
        new Thread() {
            /* class com.android.server.wifi.OppoSilenceRecovery.AnonymousClass3 */

            public void run() {
                AnonymousClass3 r1 = this;
                PacketInfo pktInfo = new PacketInfo();
                String interFace = OppoSilenceRecovery.this.mWifiNative.getClientInterfaceName();
                super.run();
                long txDiffMin = 50;
                long rxDiffMin = 80;
                int i = 0;
                while (i < 60) {
                    if (!OppoSilenceRecovery.this.mNetWorkConnected.booleanValue()) {
                        Log.e(OppoSilenceRecovery.TAG, "networkdisconnected return ");
                        return;
                    }
                    if (OppoSilenceRecovery.this.mOtherRequest.booleanValue()) {
                        rxDiffMin = 160;
                        txDiffMin = 100;
                    }
                    long txPkts = TrafficStats.getTxPackets(interFace);
                    long rxPkts = TrafficStats.getRxPackets(interFace);
                    if (OppoSilenceRecovery.DEBUG) {
                        Log.d(OppoSilenceRecovery.TAG, "hasNetworkAccessing count = " + i + " txPkts = " + txPkts + " rxPkts = " + rxPkts);
                    }
                    if (i != 0) {
                        if (rxPkts - pktInfo.rxPkts > rxDiffMin && txPkts - pktInfo.txPkts > txDiffMin) {
                            break;
                        }
                    } else {
                        pktInfo.txPkts = txPkts;
                        pktInfo.rxPkts = rxPkts;
                    }
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i++;
                    r1 = this;
                }
                Message msg = Message.obtain();
                msg.what = 8;
                msg.obj = Boolean.valueOf(i == 60);
                OppoSilenceRecovery.this.mHandler.sendMessage(msg);
            }
        }.start();
    }

    /* access modifiers changed from: package-private */
    public int getLoggingLevel() {
        return SystemProperties.getBoolean(WifiServiceImpl.DEBUG_PROPERTY, false) ? 1 : 0;
    }

    private void executeRecovery() {
        if (mRecoveryMode.equals("mtk")) {
            if (mNeedFwRecovery.booleanValue()) {
                executeMtkFwRecovey();
            } else {
                this.mWifiInjector.getSelfRecovery().trigger(0);
            }
        } else if (!mRecoveryMode.equals("qcom")) {
            this.mWifiInjector.getSelfRecovery().trigger(0);
        } else if (!mNeedFwRecovery.booleanValue()) {
            this.mWifiInjector.getSelfRecovery().trigger(0);
        }
        mRecoveryMode = "none";
    }

    private void executeMtkFwRecovey() {
        setStatistics("mtk_sau", "wifi_restart_in_silence");
        Log.d(TAG, "execute mtk wifi sau and trigger coredump");
        SystemProperties.set("oppo.wifi.sau.fw.assert.type", mRecoveryType);
        SystemProperties.set(SupplicantStaIfaceHal.INIT_START_PROPERTY, "sauMtkWifiObjsTriggerFwAssert");
        mNeedFwRecovery = false;
    }

    /* access modifiers changed from: package-private */
    public class Excecuter {
        private static final int DAYS_THIRD = 3;
        private static final int DAYS_TODAY = 0;
        private static final int DAYS_TOMORROW = 1;

        Excecuter() {
        }

        /* access modifiers changed from: package-private */
        public void startRecoveryTrigger(int ways) {
            if (OppoSilenceRecovery.DEBUG) {
                Log.d(OppoSilenceRecovery.TAG, "startRecoveryTrigger ways = " + ways);
            }
            if (ways == 3) {
                OppoSilenceRecovery.this.mAlarmManager.set(0, caculateTimeIntoMillis(3, getRandomHour(), getRandomMin()), OppoSilenceRecovery.this.mRecoveryIntent);
            } else {
                OppoSilenceRecovery.this.mAlarmManager.set(0, caculateTimeIntoMillis(1, getRandomHour(), getRandomMin()), OppoSilenceRecovery.this.mRecoveryIntent);
            }
        }

        /* access modifiers changed from: package-private */
        public void detectWifi() {
            Calendar ca = Calendar.getInstance();
            int hour = ca.get(11);
            int min = ca.get(12) + 15;
            if (min > 60) {
                hour++;
                min -= 60;
            }
            OppoSilenceRecovery.this.mAlarmManager.set(0, caculateTimeIntoMillis(0, hour, min), OppoSilenceRecovery.this.mDetectIntent);
        }

        /* access modifiers changed from: package-private */
        public void cancelTrigger() {
            if (OppoSilenceRecovery.DEBUG) {
                Log.d(OppoSilenceRecovery.TAG, "cancelTrigger ");
            }
            OppoSilenceRecovery.this.mAlarmManager.cancel(OppoSilenceRecovery.this.mRecoveryIntent);
        }

        private long caculateTimeIntoMillis(int days, int hours, int mins) {
            Calendar cal = Calendar.getInstance();
            if (days < 0) {
                cal.add(5, 1);
            } else {
                cal.add(5, days);
            }
            if (hours < 0 || hours > 6) {
                cal.set(11, 1);
            } else {
                cal.set(11, hours);
            }
            cal.set(12, mins);
            cal.set(13, 0);
            if (OppoSilenceRecovery.DEBUG) {
                Log.d(OppoSilenceRecovery.TAG, "caculateTimeIntoMillis: " + cal.getTime());
            }
            return cal.getTimeInMillis();
        }

        private int getRandomHour() {
            int ret = new Random().nextInt(5 - 1) + 1;
            if (OppoSilenceRecovery.DEBUG) {
                Log.d(OppoSilenceRecovery.TAG, "fool-proof, start=1 end=5 random=" + ret);
            }
            return ret;
        }

        private int getRandomMin() {
            int ret = new Random().nextInt(60 - 0) + 0;
            if (OppoSilenceRecovery.DEBUG) {
                Log.d(OppoSilenceRecovery.TAG, "fool-proof, start=0 end=60 random=" + ret);
            }
            return ret;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void resetRestartAlarm() {
            long detectInterval = System.currentTimeMillis() + 3600000;
            if (OppoSilenceRecovery.DEBUG) {
                Log.d(OppoSilenceRecovery.TAG, "fool-proof,reset alarm count = " + OppoSilenceRecovery.this.mResetAlarmCount);
            }
            if (OppoSilenceRecovery.access$1608(OppoSilenceRecovery.this) >= 3) {
                if (OppoSilenceRecovery.DEBUG) {
                    Log.d(OppoSilenceRecovery.TAG, "fool-proof,reset alarm next night!");
                }
                OppoSilenceRecovery.this.mResetAlarmCount = 0;
                OppoSilenceRecovery.this.mAlarmManager.set(0, caculateTimeIntoMillis(1, getRandomHour(), getRandomMin()), OppoSilenceRecovery.this.mRecoveryIntent);
                return;
            }
            OppoSilenceRecovery.this.mAlarmManager.set(0, detectInterval, OppoSilenceRecovery.this.mRecoveryIntent);
        }
    }

    public class PacketInfo {
        public long rxPkts = 0;
        public long txPkts = 0;

        public PacketInfo() {
        }
    }
}
