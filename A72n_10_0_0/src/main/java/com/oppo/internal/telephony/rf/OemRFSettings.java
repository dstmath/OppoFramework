package com.oppo.internal.telephony.rf;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.util.OemTelephonyUtils;
import java.lang.reflect.Method;
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

    public enum PhoneState {
        IDLE,
        OFFHOOK
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
        private SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangedListener;
        private PhoneStateListener[] mPhoneStateListeners;
        private NWReceiver mReceiver;

        protected class OemPhoneStateListener extends PhoneStateListener {
            OrientationSensorEventListener mOrienListener;

            public OemPhoneStateListener(OrientationSensorEventListener orienListener, int slotId) {
                this.mOrienListener = orienListener;
            }

            public void onCallStateChanged(int value, String incomingNumber) {
                this.mOrienListener.onCallStateChanged(value, incomingNumber);
            }
        }

        protected class OemSubscriptionsChangedListener extends SubscriptionManager.OnSubscriptionsChangedListener {
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

        /* access modifiers changed from: protected */
        public class NWReceiver extends BroadcastReceiver {
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
                if (activeInfo.getState() != NetworkInfo.State.CONNECTED && activeInfo.getState() != NetworkInfo.State.CONNECTING && activeInfo.getState() != NetworkInfo.State.SUSPENDED) {
                    return NetworkState.IDLE;
                }
                if (activeInfo.getType() == 0) {
                    return NetworkState.DATA;
                }
                return NetworkState.WIFI;
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
            String result;
            if (msg.what == EVENT_OEM_SET_RF_COMPLETE && OemRFSettings.DBG) {
                try {
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        int[] reval = (int[]) ar.result;
                        if (reval == null || reval.length <= 0 || reval[0] != 1) {
                            result = " fail";
                        } else {
                            result = " success";
                        }
                    } else {
                        result = " error";
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("switch ");
                    sb.append(this.mLastSwitchPortrait ? "Portrait" : "Lanscape");
                    sb.append(result);
                    String result2 = sb.toString();
                    if (OemRFSettings.DBG) {
                        Rlog.d(OemRFSettings.TAG, result2);
                    }
                    OemRFSettings.showToast(PhoneFactory.getDefaultPhone().getContext(), result2);
                } catch (Exception e) {
                }
            }
        }

        private Context getContext() {
            return PhoneFactory.getPhone(0).getContext();
        }

        private ComponentName getTopAppName() {
            try {
                Method getTopAppName = this.mAm.getClass().getMethod("getTopAppName", new Class[0]);
                getTopAppName.setAccessible(true);
                return (ComponentName) getTopAppName.invoke(this.mAm, new Object[0]);
            } catch (Exception e) {
                return null;
            }
        }

        public boolean isInGame() {
            ComponentName cn = getTopAppName();
            if (cn == null || this.mGameList == null) {
                return false;
            }
            if (this.mGameList.indexOf(cn.getPackageName()) >= 0) {
                return true;
            }
            return false;
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
                    this.mListener.onSwitchChange(this.mLastSwitchPortrait, Message.obtain(this, (int) EVENT_OEM_SET_RF_COMPLETE));
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
                if (state != this.mLastPortrait) {
                    this.mLastPortrait = state;
                    this.mListener.onConfigurationChange(this.mLastPortrait);
                    processSwitch();
                }
            }
        }

        public void onNetworkChanged(NetworkState state) {
            synchronized (this) {
                if (state != this.mLastNwState) {
                    this.mLastNwState = state;
                    this.mListener.onNetworkChange(state);
                    processSwitch();
                }
            }
        }

        public void registerPhone() {
            if (!this.mIsPhoneListen) {
                this.mIsPhoneListen = true;
                TelephonyManager tm = TelephonyManager.from(PhoneFactory.getPhone(0).getContext());
                int numPhones = tm.getPhoneCount();
                for (int i = 0; i < numPhones; i++) {
                    int subId = SubscriptionController.getInstance().getSubIdUsingPhoneId(i);
                    if (SubscriptionController.getInstance().isActiveSubId(subId)) {
                        tm.createForSubscriptionId(subId).listen(this.mPhoneStateListeners[i], 160);
                    }
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
                if (state != this.mLastCallState) {
                    this.mLastCallState = state;
                    this.mListener.onCallChange(state);
                    processSwitch();
                }
            }
        }

        public void onSubscriptionsChanged() {
            synchronized (this) {
                unregisterPhone();
                registerPhone();
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:56:0x00d5, code lost:
            if (com.oppo.internal.telephony.rf.OemRFSettings.DBG == false) goto L_0x00ef;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:57:0x00d7, code lost:
            android.telephony.Rlog.d(com.oppo.internal.telephony.rf.OemRFSettings.TAG, "onSwitchChange:" + r12.mLastSwitchPortrait);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:58:0x00ef, code lost:
            r12.mListener.onSwitchChange(r12.mLastSwitchPortrait, android.os.Message.obtain(r12, (int) com.oppo.internal.telephony.rf.OemRFSettings.OrientationSensorEventListener.EVENT_OEM_SET_RF_COMPLETE));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:59:0x00fc, code lost:
            return;
         */
        private void processSwitch() {
            synchronized (this) {
                boolean isSwitchLandscape = !this.mLastPortrait && isInGame() && this.mLastNwState == NetworkState.DATA;
                boolean isSwitchPortrait = !isSwitchLandscape && this.mLastCallState == PhoneState.IDLE;
                if (OemRFSettings.DBG) {
                    Rlog.d(OemRFSettings.TAG, "onSwitchChange double checke :" + isSwitchPortrait + "/" + isSwitchLandscape);
                }
                if (isSwitchPortrait != isSwitchLandscape) {
                    if (isSwitchPortrait != this.mLastSwitchPortrait) {
                        if (isSwitchLandscape && this.mMaxTimer > 0 && this.mMaxTimes > 0) {
                            long currentTime = System.currentTimeMillis();
                            if (OemRFSettings.DBG) {
                                Rlog.d(OemRFSettings.TAG, "onSwitchChange timer:" + (currentTime - this.mLastTimer));
                            }
                            if (this.mLastTimes != 0) {
                                if (this.mLastTimer != 0) {
                                    if (currentTime > this.mLastTimer + this.mMaxTimer) {
                                        this.mLastTimer = currentTime;
                                        this.mLastTimes = 0;
                                    } else if (this.mLastTimes >= this.mMaxTimes) {
                                        if (OemRFSettings.DBG) {
                                            Rlog.d(OemRFSettings.TAG, "onSwitchChange max bloack:" + this.mLastTimes + "/" + this.mMaxTimes);
                                        }
                                        return;
                                    }
                                    this.mLastTimes++;
                                }
                            }
                            this.mLastTimer = currentTime;
                            this.mLastTimes++;
                        }
                        this.mLastSwitchPortrait = isSwitchPortrait;
                    }
                }
            }
        }

        private PhoneState getCallStateFromValue(int value) {
            return value == 0 ? PhoneState.IDLE : PhoneState.OFFHOOK;
        }

        public void processSwitch(boolean isPortrait) {
            this.mLastSwitchPortrait = isPortrait;
            this.mListener.onSwitchChange(isPortrait, Message.obtain(this, (int) EVENT_OEM_SET_RF_COMPLETE));
        }

        public synchronized void setInitValue(int maxTimer, int maxTimes) {
            if (maxTimer >= 1440) {
                maxTimer = 0;
                maxTimes = 0;
            }
            int maxTimer2 = maxTimer < 0 ? 0 : maxTimer;
            int maxTimes2 = maxTimes < 0 ? 0 : maxTimes;
            this.mMaxTimer = (long) (maxTimer2 * 60 * ONE_SECOND);
            this.mMaxTimes = maxTimes2;
            this.mLastTimer = 0;
            this.mLastTimes = 0;
        }
    }

    public static void showToast(Context context, String msg) {
    }

    public OemRFSettings(Context context) {
        this(context, null);
    }

    public OemRFSettings(Context context, IOemRFListener listener) {
        this.mOrientationListener = null;
        this.mListener = new IOemRFListener() {
            /* class com.oppo.internal.telephony.rf.OemRFSettings.AnonymousClass1 */

            @Override // com.oppo.internal.telephony.rf.OemRFSettings.IOemRFListener
            public void onCallChange(PhoneState state) {
            }

            @Override // com.oppo.internal.telephony.rf.OemRFSettings.IOemRFListener
            public void onConfigurationChange(boolean isPortrait) {
            }

            @Override // com.oppo.internal.telephony.rf.OemRFSettings.IOemRFListener
            public void onNetworkChange(NetworkState nwState) {
            }

            @Override // com.oppo.internal.telephony.rf.OemRFSettings.IOemRFListener
            public void onSwitchChange(boolean isPortrait, Message msg) {
                Phone phone = PhoneFactory.getDefaultPhone();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(phone.getContext()).edit();
                editor.putBoolean(OemRFSettings.PREF, isPortrait);
                editor.apply();
                AbstractPhone tmpPhone = (AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, phone);
                if (isPortrait) {
                    tmpPhone.oppoSetTunerLogic(0, msg);
                } else {
                    tmpPhone.oppoSetTunerLogic(1, msg);
                }
                if (OemRFSettings.DBG) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("OemRFSettings: switch ");
                    sb.append(isPortrait ? "Portrait" : "Lanscape");
                    Rlog.d(OemRFSettings.TAG, sb.toString());
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
        OrientationSensorEventListener orientationSensorEventListener = this.mOrientationListener;
        if (orientationSensorEventListener != null) {
            orientationSensorEventListener.register();
        }
    }

    public void unregister() {
        OrientationSensorEventListener orientationSensorEventListener = this.mOrientationListener;
        if (orientationSensorEventListener != null) {
            orientationSensorEventListener.unregister();
        }
    }

    public void setConfigurationChanged(boolean isPortrait) {
        OrientationSensorEventListener orientationSensorEventListener = this.mOrientationListener;
        if (orientationSensorEventListener != null) {
            orientationSensorEventListener.onConfigurationChanged(isPortrait);
        }
    }

    public void setGameList(ArrayList<String> list) {
        OrientationSensorEventListener orientationSensorEventListener = this.mOrientationListener;
        if (orientationSensorEventListener != null) {
            orientationSensorEventListener.setGameList(list);
        }
    }

    public void restore(Context context) {
        OrientationSensorEventListener orientationSensorEventListener;
        boolean isPortrait = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF, true);
        if (DBG) {
            Rlog.d(TAG, "OemRFSettings: restore:" + isPortrait);
        }
        if (!isPortrait && (orientationSensorEventListener = this.mOrientationListener) != null) {
            orientationSensorEventListener.processSwitch(true);
        }
    }

    public void setInitValue(int maxTimer, int maxTimes) {
        OrientationSensorEventListener orientationSensorEventListener = this.mOrientationListener;
        if (orientationSensorEventListener != null) {
            orientationSensorEventListener.setInitValue(maxTimer, maxTimes);
        }
    }
}
