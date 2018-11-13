package com.android.server.wifi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.OppoAssertTip;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.WifiRomUpdateHelper;
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
    private AlarmManager mAlarmManager;
    private OppoAssertTip mAssertProxy;
    private Context mContext;
    private PendingIntent mDetectIntent;
    private Excecuter mExcecuter;
    private Handler mHandler;
    private long mLastScreenOff = 0;
    private Boolean mNetWorkConnected = Boolean.valueOf(false);
    private Boolean mOtherRequest = Boolean.valueOf(false);
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

    class Excecuter {
        private static final int DAYS_THIRD = 3;
        private static final int DAYS_TODAY = 0;
        private static final int DAYS_TOMORROW = 1;

        Excecuter() {
        }

        void startRecoveryTrigger(int ways) {
            if (OppoSilenceRecovery.DEBUG) {
                Log.d(OppoSilenceRecovery.TAG, "startRecoveryTrigger ways = " + ways);
            }
            if (ways == 3) {
                OppoSilenceRecovery.this.mAlarmManager.set(0, caculateTimeIntoMillis(3, getRandomHour(), getRandomMin()), OppoSilenceRecovery.this.mRecoveryIntent);
            } else {
                OppoSilenceRecovery.this.mAlarmManager.set(0, caculateTimeIntoMillis(1, getRandomHour(), getRandomMin()), OppoSilenceRecovery.this.mRecoveryIntent);
            }
        }

        void detectWifi() {
            Calendar ca = Calendar.getInstance();
            int hour = ca.get(11);
            int min = ca.get(12) + 15;
            if (min > 60) {
                hour++;
                min -= 60;
            }
            OppoSilenceRecovery.this.mAlarmManager.set(0, caculateTimeIntoMillis(0, hour, min), OppoSilenceRecovery.this.mDetectIntent);
        }

        void cancelTrigger() {
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
            int ret = new Random().nextInt(4) + 1;
            if (OppoSilenceRecovery.DEBUG) {
                Log.d(OppoSilenceRecovery.TAG, "fool-proof, start=" + 1 + " end=" + 5 + " random=" + ret);
            }
            return ret;
        }

        private int getRandomMin() {
            int ret = new Random().nextInt(60) + 0;
            if (OppoSilenceRecovery.DEBUG) {
                Log.d(OppoSilenceRecovery.TAG, "fool-proof, start=" + 0 + " end=" + 60 + " random=" + ret);
            }
            return ret;
        }

        private void resetRestartAlarm() {
            long detectInterval = System.currentTimeMillis() + 3600000;
            if (OppoSilenceRecovery.DEBUG) {
                Log.d(OppoSilenceRecovery.TAG, "fool-proof,reset alarm count = " + OppoSilenceRecovery.this.mResetAlarmCount);
            }
            OppoSilenceRecovery oppoSilenceRecovery = OppoSilenceRecovery.this;
            int -get8 = oppoSilenceRecovery.mResetAlarmCount;
            oppoSilenceRecovery.mResetAlarmCount = -get8 + 1;
            if (-get8 >= 3) {
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
    }

    OppoSilenceRecovery(Context c, WifiInjector injector, WifiNative wn) {
        this.mContext = c;
        this.mWifiNative = wn;
        this.mWifiInjector = injector;
        this.mPktInfo = new PacketInfo();
        this.mExcecuter = new Excecuter();
        this.mAssertProxy = OppoAssertTip.getInstance();
        this.mAlarmManager = (AlarmManager) c.getSystemService("alarm");
        this.mRecoveryIntent = PendingIntent.getBroadcast(c, 0, new Intent(ACTION_RECOVERY_WIFI, null), 268435456);
        this.mDetectIntent = PendingIntent.getBroadcast(c, 0, new Intent(ACTION_DETECT_NETWORK_ACCESS, null), 268435456);
        this.mWifiRomUpdateHelper = new WifiRomUpdateHelper(this.mContext);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(ACTION_RECOVERY_WIFI);
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction(ACTION_RECOVERY_WIFI_OTHER);
        filter.addAction(ACTION_DETECT_NETWORK_ACCESS);
        c.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (OppoSilenceRecovery.DEBUG) {
                    Log.d(OppoSilenceRecovery.TAG, "action" + action);
                }
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    OppoSilenceRecovery.this.mScreenOn = true;
                    OppoSilenceRecovery.this.mLastScreenOff = 0;
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    OppoSilenceRecovery.this.mScreenOn = false;
                    OppoSilenceRecovery.this.mLastScreenOff = System.currentTimeMillis();
                } else if (action.equals(OppoSilenceRecovery.ACTION_RECOVERY_WIFI)) {
                    OppoSilenceRecovery.this.recoveyWifi();
                } else if (action.equals(OppoSilenceRecovery.ACTION_RECOVERY_WIFI_OTHER)) {
                    OppoSilenceRecovery.this.mOtherRequest = Boolean.valueOf(true);
                    OppoSilenceRecovery.this.recoveyWifi();
                } else if (action.equals(OppoSilenceRecovery.ACTION_DETECT_NETWORK_ACCESS)) {
                    OppoSilenceRecovery.this.hasNetworkAccessing();
                } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    DetailedState state = ((NetworkInfo) intent.getParcelableExtra("networkInfo")).getDetailedState();
                    if (OppoSilenceRecovery.DEBUG) {
                        Log.d(OppoSilenceRecovery.TAG, "wifi connect state =" + state);
                    }
                    if (state == DetailedState.CONNECTED) {
                        OppoSilenceRecovery.this.mNetWorkConnected = Boolean.valueOf(true);
                    } else if (state == DetailedState.DISCONNECTED) {
                        OppoSilenceRecovery.this.mNetWorkConnected = Boolean.valueOf(false);
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
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 8) {
                    boolean result = ((Boolean) msg.obj).booleanValue();
                    if (OppoSilenceRecovery.DEBUG) {
                        Log.d(OppoSilenceRecovery.TAG, "handleMessage msg.what = " + msg.what + " result=" + result + " mOther=" + OppoSilenceRecovery.this.mOtherRequest + " Count=" + OppoSilenceRecovery.this.netWorkCount);
                    }
                    if (result || OppoSilenceRecovery.this.netWorkCount == 0 || !OppoSilenceRecovery.this.mOtherRequest.booleanValue()) {
                        if (OppoSilenceRecovery.this.mOtherRequest.booleanValue() && (result ^ 1) != 0) {
                            result = OppoSilenceRecovery.this.hasEnoughSleep();
                        }
                        OppoSilenceRecovery.this.netWorkCount = 3;
                        OppoSilenceRecovery.this.netWorkRestart(result);
                        return;
                    }
                    OppoSilenceRecovery oppoSilenceRecovery = OppoSilenceRecovery.this;
                    oppoSilenceRecovery.netWorkCount = oppoSilenceRecovery.netWorkCount - 1;
                    OppoSilenceRecovery.this.mExcecuter.detectWifi();
                }
            }
        };
    }

    private boolean hasEnoughSleep() {
        boolean enoughSleep = false;
        if (this.mLastScreenOff > 0 && System.currentTimeMillis() - this.mLastScreenOff >= 2700000) {
            enoughSleep = true;
        }
        Log.d(TAG, "hasEnoughSleep " + enoughSleep);
        return enoughSleep;
    }

    public Integer getRomUpdateIntegerValue(String key, Integer defaultVal) {
        if (this.mWifiRomUpdateHelper != null) {
            return this.mWifiRomUpdateHelper.getIntegerValue(key, defaultVal);
        }
        return defaultVal;
    }

    void enableFoolProof(boolean enable) {
        this.mResetAlarmCount = 0;
        if (getRomUpdateIntegerValue("BASIC_FOOL_PROOF_ON", Integer.valueOf(1)).intValue() != 1) {
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

    boolean ifPermitRecovery() {
        if (!checkTimeInMorning() || this.mScreenOn) {
            return false;
        }
        return true;
    }

    void recoveyWifi() {
        if (!ifPermitRecovery()) {
            this.mExcecuter.startRecoveryTrigger(1);
        } else if (this.mNetWorkConnected.booleanValue()) {
            hasNetworkAccessing();
        } else {
            Log.e(TAG, "silence recovery ,wifi will recoery later... ... ");
            setStatistics("silence", "wifi_restart_in_silence");
            this.mWifiInjector.getSelfRecovery().trigger(0);
            this.mOtherRequest = Boolean.valueOf(false);
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
        HashMap<String, String> map = new HashMap();
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

    private void netWorkRestart(boolean enable) {
        if (enable && ifPermitRecovery()) {
            if (DEBUG) {
                Log.d(TAG, "silence recovery network connect wifi wifi will recovery ");
            }
            setStatistics("silence_connected", "wifi_restart_in_silence_connected");
            this.mWifiInjector.getSelfRecovery().trigger(0);
            this.mOtherRequest = Boolean.valueOf(false);
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "netWorkRestart later do---tomorrow");
        }
        this.mExcecuter.resetRestartAlarm();
    }

    private void hasNetworkAccessing() {
        new Thread() {
            public void run() {
                long rxDiffMin = 80;
                long txDiffMin = 50;
                PacketInfo pktInfo = new PacketInfo();
                String interFace = OppoSilenceRecovery.this.mWifiNative.getInterfaceName();
                super.run();
                int i = 0;
                while (i < 60) {
                    if (OppoSilenceRecovery.this.mNetWorkConnected.booleanValue()) {
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
                            AnonymousClass3.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        i++;
                    } else {
                        Log.e(OppoSilenceRecovery.TAG, "networkdisconnected return ");
                        return;
                    }
                }
                Message msg = Message.obtain();
                msg.what = 8;
                msg.obj = Boolean.valueOf(i == 60);
                OppoSilenceRecovery.this.mHandler.sendMessage(msg);
            }
        }.start();
    }

    int getLoggingLevel() {
        return SystemProperties.getBoolean(WifiStateMachine.DEBUG_PROPERTY, false) ? 1 : 0;
    }
}
