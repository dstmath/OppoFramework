package com.android.internal.telephony;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import java.util.ArrayList;

public class OemRFSettings {
    public static final boolean DBG = OemConstant.SWITCH_LOG;
    public static final String PREF = "rfsettings";
    public static final String TAG = "OemRFSettings";
    static OemRFSettings sInstance = null;
    public static boolean sIsSwitch = false;
    IOemRFListener mListener;
    OrientationSensorEventListener mOrientationListener;

    public interface IOemRFListener {
        void onCallChange(PhoneState phoneState);

        void onConfigurationChange(boolean z);

        void onNetworkChange(NetworkState networkState);

        void onSwitchChange(boolean z, Message message);
    }

    public enum NetworkState {
        IDLE,
        DATA,
        WIFI
    }

    private static class OrientationSensorEventListener extends Handler {
        protected static final int EVENT_OEM_SET_RF_COMPLETE = 288;
        protected static final boolean IS_CALL_ENABLE = false;
        private static final int ONE_DAY = 24;
        private static final int ONE_HOUR = 60;
        private static final int ONE_MINUTE = 60;
        private static final int ONE_SECOND = 1000;
        private ActivityManager mAm;
        private ArrayList<String> mGameList;
        private volatile boolean mIsPhoneListen;
        private volatile boolean mIsRegist;
        private PhoneState mLastCallState;
        private NetworkState mLastNwState;
        private volatile boolean mLastPortrait;
        private volatile boolean mLastSwitchPortrait;
        private long mLastTimer;
        private int mLastTimes;
        private final IOemRFListener mListener;
        private long mMaxTimer;
        private int mMaxTimes;
        private OnSubscriptionsChangedListener mOnSubscriptionsChangedListener;
        private PhoneStateListener[] mPhoneStateListeners;
        private NWReceiver mReceiver;

        protected class NWReceiver extends BroadcastReceiver {
            OrientationSensorEventListener mOrienListener;

            public NWReceiver(OrientationSensorEventListener orienListener) {
                this.mOrienListener = orienListener;
            }

            public void onReceive(Context context, Intent intent) {
                NetworkInfo activeInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
                if (OemRFSettings.DBG) {
                    Rlog.d(OemRFSettings.TAG, "NetworkInfo:" + activeInfo);
                }
                this.mOrienListener.onNetworkChanged(getNwState(activeInfo));
            }

            private NetworkState getNwState(NetworkInfo activeInfo) {
                if (activeInfo == null) {
                    return NetworkState.IDLE;
                }
                if (activeInfo.getState() != State.CONNECTED && activeInfo.getState() != State.CONNECTING && activeInfo.getState() != State.SUSPENDED) {
                    return NetworkState.IDLE;
                }
                if (activeInfo.getType() == 0) {
                    return NetworkState.DATA;
                }
                return NetworkState.WIFI;
            }
        }

        protected class OemPhoneStateListener extends PhoneStateListener {
            OrientationSensorEventListener mOrienListener;

            public OemPhoneStateListener(OrientationSensorEventListener orienListener, int slotId) {
                super(PhoneFactory.getPhone(slotId).getSubId(), slotId);
                this.mOrienListener = orienListener;
            }

            public void onCallStateChanged(int value, String incomingNumber) {
                this.mOrienListener.onCallStateChanged(value, incomingNumber);
            }
        }

        protected class OemSubscriptionsChangedListener extends OnSubscriptionsChangedListener {
            OrientationSensorEventListener mOrienListener;

            public OemSubscriptionsChangedListener(OrientationSensorEventListener orienListener) {
                this.mOrienListener = orienListener;
            }

            public void onSubscriptionsChanged() {
                if (OemRFSettings.DBG) {
                    Rlog.d(OemRFSettings.TAG, "onSubscriptionsChanged");
                }
                this.mOrienListener.onSubscriptionsChanged();
            }
        }

        public OrientationSensorEventListener(IOemRFListener listener) {
            this.mPhoneStateListeners = null;
            this.mOnSubscriptionsChangedListener = null;
            this.mReceiver = null;
            this.mAm = null;
            this.mIsPhoneListen = false;
            this.mGameList = null;
            this.mIsRegist = false;
            this.mMaxTimer = 0;
            this.mMaxTimes = 0;
            this.mLastTimer = 0;
            this.mLastTimes = 0;
            this.mAm = (ActivityManager) getContext().getSystemService("activity");
            this.mListener = listener;
            this.mLastPortrait = true;
            this.mLastCallState = PhoneState.IDLE;
            this.mLastSwitchPortrait = true;
            this.mReceiver = new NWReceiver(this);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_OEM_SET_RF_COMPLETE /*288*/:
                    if (OemRFSettings.DBG) {
                        try {
                            String result;
                            AsyncResult ar = (AsyncResult) msg.obj;
                            if (ar.exception == null) {
                                int[] reval = ar.result;
                                if (reval == null || reval.length <= 0 || reval[0] != 1) {
                                    result = " fail";
                                } else {
                                    result = " success";
                                }
                            } else {
                                result = " error";
                            }
                            result = "switch " + (this.mLastSwitchPortrait ? "Portrait" : "Lanscape") + result;
                            if (OemRFSettings.DBG) {
                                Rlog.d(OemRFSettings.TAG, result);
                            }
                            OemRFSettings.showToast(PhoneFactory.getDefaultPhone().getContext(), result);
                            break;
                        } catch (Exception e) {
                            break;
                        }
                    }
                    return;
                    break;
            }
        }

        public static Context getContext() {
            return PhoneFactory.getPhone(0).getContext();
        }

        public boolean isInGame() {
            boolean z = false;
            ComponentName cn = this.mAm.getTopAppName();
            if (cn == null || this.mGameList == null) {
                return false;
            }
            if (this.mGameList.indexOf(cn.getPackageName()) >= 0) {
                z = true;
            }
            return z;
        }

        public void setGameList(ArrayList<String> list) {
            this.mGameList = list;
        }

        public synchronized void register() {
            if (!this.mIsRegist) {
                this.mIsRegist = true;
                getContext().registerReceiver(this.mReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            }
        }

        public synchronized void unregister() {
            if (this.mIsRegist) {
                this.mIsRegist = false;
                if (!this.mLastSwitchPortrait) {
                    this.mLastSwitchPortrait = true;
                    this.mListener.onSwitchChange(this.mLastSwitchPortrait, Message.obtain(this, EVENT_OEM_SET_RF_COMPLETE));
                }
                getContext().unregisterReceiver(this.mReceiver);
            }
        }

        public void registerSubInfo() {
            SubscriptionManager.from(getContext()).addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        }

        public void unregisterSubInfo() {
            SubscriptionManager.from(getContext()).removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        }

        public void onConfigurationChanged(boolean state) {
            synchronized (this) {
                if (state == this.mLastPortrait) {
                    return;
                }
                this.mLastPortrait = state;
                this.mListener.onConfigurationChange(this.mLastPortrait);
                processSwitch();
            }
        }

        public void onNetworkChanged(NetworkState state) {
            synchronized (this) {
                if (state == this.mLastNwState) {
                    return;
                }
                this.mLastNwState = state;
                this.mListener.onNetworkChange(this.mLastNwState);
                processSwitch();
            }
        }

        public void registerPhone() {
            if (!this.mIsPhoneListen) {
                this.mIsPhoneListen = true;
                TelephonyManager tm = TelephonyManager.from(PhoneFactory.getPhone(0).getContext());
                int numPhones = tm.getPhoneCount();
                for (int i = 0; i < numPhones; i++) {
                    this.mPhoneStateListeners[i].setSubscripitonId(PhoneFactory.getPhone(i).getSubId());
                    tm.listen(this.mPhoneStateListeners[i], 160);
                }
            }
        }

        public void unregisterPhone() {
            if (this.mIsPhoneListen) {
                this.mIsPhoneListen = false;
                TelephonyManager tm = TelephonyManager.from(PhoneFactory.getPhone(0).getContext());
                int numPhones = tm.getPhoneCount();
                for (int i = 0; i < numPhones; i++) {
                    tm.listen(this.mPhoneStateListeners[i], 0);
                }
            }
        }

        public void onCallStateChanged(int value, String incomingNumber) {
            PhoneState state = getCallStateFromValue(value);
            if (OemRFSettings.DBG) {
                Rlog.d(OemRFSettings.TAG, "onCallStateChanged:" + state);
            }
            synchronized (this) {
                if (state == this.mLastCallState) {
                    return;
                }
                this.mLastCallState = state;
                this.mListener.onCallChange(state);
                processSwitch();
            }
        }

        public void onSubscriptionsChanged() {
            synchronized (this) {
                unregisterPhone();
                registerPhone();
            }
        }

        /* JADX WARNING: Missing block: B:46:0x009f, code:
            if (com.android.internal.telephony.OemRFSettings.DBG == false) goto L_0x00bd;
     */
        /* JADX WARNING: Missing block: B:47:0x00a1, code:
            android.telephony.Rlog.d(com.android.internal.telephony.OemRFSettings.TAG, "onSwitchChange:" + r12.mLastSwitchPortrait);
     */
        /* JADX WARNING: Missing block: B:48:0x00bd, code:
            r12.mListener.onSwitchChange(r12.mLastSwitchPortrait, android.os.Message.obtain(r12, EVENT_OEM_SET_RF_COMPLETE));
     */
        /* JADX WARNING: Missing block: B:49:0x00ca, code:
            return;
     */
        /* JADX WARNING: Missing block: B:67:0x0116, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void processSwitch() {
            synchronized (this) {
                boolean isSwitchLandscape = (this.mLastPortrait || !isInGame()) ? false : this.mLastNwState == NetworkState.DATA;
                boolean isSwitchPortrait = !isSwitchLandscape && this.mLastCallState == PhoneState.IDLE;
                if (OemRFSettings.DBG) {
                    Rlog.d(OemRFSettings.TAG, "onSwitchChange double checke :" + isSwitchPortrait + "/" + isSwitchLandscape);
                }
                if (isSwitchPortrait == isSwitchLandscape) {
                } else if (isSwitchPortrait == this.mLastSwitchPortrait) {
                } else {
                    if (isSwitchLandscape) {
                        if (this.mMaxTimer > 0 && this.mMaxTimes > 0) {
                            long currentTime = System.currentTimeMillis();
                            if (OemRFSettings.DBG) {
                                Rlog.d(OemRFSettings.TAG, "onSwitchChange timer:" + (currentTime - this.mLastTimer));
                            }
                            if (this.mLastTimes == 0 || this.mLastTimer == 0) {
                                this.mLastTimer = currentTime;
                            } else {
                                if (currentTime > this.mLastTimer + this.mMaxTimer) {
                                    this.mLastTimer = currentTime;
                                    this.mLastTimes = 0;
                                } else if (this.mLastTimes >= this.mMaxTimes) {
                                    if (OemRFSettings.DBG) {
                                        Rlog.d(OemRFSettings.TAG, "onSwitchChange max bloack:" + this.mLastTimes + "/" + this.mMaxTimes);
                                    }
                                }
                            }
                            this.mLastTimes++;
                        }
                    }
                    this.mLastSwitchPortrait = isSwitchPortrait;
                }
            }
        }

        private PhoneState getCallStateFromValue(int value) {
            return value == 0 ? PhoneState.IDLE : PhoneState.OFFHOOK;
        }

        public void processSwitch(boolean isPortrait) {
            this.mLastSwitchPortrait = isPortrait;
            this.mListener.onSwitchChange(isPortrait, Message.obtain(this, EVENT_OEM_SET_RF_COMPLETE));
        }

        public synchronized void setInitValue(int maxTimer, int maxTimes) {
            if (maxTimer >= 1440) {
                maxTimer = 0;
                maxTimes = 0;
            }
            if (maxTimer < 0) {
                maxTimer = 0;
            }
            if (maxTimes < 0) {
                maxTimes = 0;
            }
            this.mMaxTimer = (long) ((maxTimer * 60) * 1000);
            this.mMaxTimes = maxTimes;
            this.mLastTimer = 0;
            this.mLastTimes = 0;
        }
    }

    public enum PhoneState {
        IDLE,
        OFFHOOK
    }

    public static void showToast(Context context, String msg) {
    }

    public OemRFSettings(Context context) {
        this(context, null);
    }

    public OemRFSettings(Context context, IOemRFListener listener) {
        this.mOrientationListener = null;
        this.mListener = new IOemRFListener() {
            public void onCallChange(PhoneState state) {
            }

            public void onConfigurationChange(boolean isPortrait) {
            }

            public void onNetworkChange(NetworkState nwState) {
            }

            public void onSwitchChange(boolean isPortrait, Message msg) {
                Phone phone = PhoneFactory.getDefaultPhone();
                Editor editor = PreferenceManager.getDefaultSharedPreferences(phone.getContext()).edit();
                editor.putBoolean(OemRFSettings.PREF, isPortrait);
                editor.apply();
                if (isPortrait) {
                    phone.oppoSetTunerLogic(0, msg);
                } else {
                    phone.oppoSetTunerLogic(1, msg);
                }
                if (OemRFSettings.DBG) {
                    Rlog.d(OemRFSettings.TAG, "OemRFSettings: switch " + (isPortrait ? "Portrait" : "Lanscape"));
                }
            }
        };
        if (listener != null) {
            this.mListener = listener;
        }
        this.mOrientationListener = new OrientationSensorEventListener(this.mListener);
        if (DBG) {
            Rlog.d(TAG, "OemRFSettings: init and release first");
        }
    }

    public static synchronized OemRFSettings getDefault(Context context) {
        OemRFSettings oemRFSettings;
        synchronized (OemRFSettings.class) {
            if (sInstance == null) {
                sInstance = new OemRFSettings(context);
            }
            oemRFSettings = sInstance;
        }
        return oemRFSettings;
    }

    public void register() {
        if (this.mOrientationListener != null) {
            this.mOrientationListener.register();
        }
    }

    public void unregister() {
        if (this.mOrientationListener != null) {
            this.mOrientationListener.unregister();
        }
    }

    public void setConfigurationChanged(boolean isPortrait) {
        if (this.mOrientationListener != null) {
            this.mOrientationListener.onConfigurationChanged(isPortrait);
        }
    }

    public void setGameList(ArrayList<String> list) {
        if (this.mOrientationListener != null) {
            this.mOrientationListener.setGameList(list);
        }
    }

    public void restore(Context context) {
        boolean isPortrait = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF, true);
        if (DBG) {
            Rlog.d(TAG, "OemRFSettings: restore:" + isPortrait);
        }
        if (!isPortrait && this.mOrientationListener != null) {
            this.mOrientationListener.processSwitch(true);
        }
    }

    public void setInitValue(int maxTimer, int maxTimes) {
        if (this.mOrientationListener != null) {
            this.mOrientationListener.setInitValue(maxTimer, maxTimes);
        }
    }
}
