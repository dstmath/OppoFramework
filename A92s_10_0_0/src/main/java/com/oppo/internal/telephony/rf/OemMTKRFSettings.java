package com.oppo.internal.telephony.rf;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.AbstractServiceStateTracker;
import com.android.internal.telephony.DataEntity;
import com.android.internal.telephony.DeviceStateMonitor;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.android.internal.telephony.util.ReflectionHelper;
import com.oppo.internal.telephony.OppoRIL;
import com.oppo.internal.telephony.OppoTelephonyController;

public class OemMTKRFSettings {
    public static final boolean DBG = OemConstant.SWITCH_LOG;
    public static final String PREF = "mtk_rfsettings";
    public static final String TAG = "OemMTKRFSettings";
    public static boolean isSwitch = true;
    static OemMTKRFSettings sInstance = null;
    IOemRFListener mListener;
    OrientationSensorEventListener mOrientationListener;

    public interface IOemRFListener {
        boolean isTestCard();

        void onCallChange(PhoneState phoneState);

        void onNetworkChange(NetworkState networkState);

        void onSuscriptionChange();

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

    public enum SensorState {
        FAR,
        NEAR
    }

    private static class OrientationSensorEventListener extends Handler {
        protected static final int EVENT_OEM_SCREEN_CHANGED = 298;
        protected static final int EVENT_OEM_SET_RF_COMPLETE = 288;
        protected static final boolean IS_CALL_ENABLE = true;
        protected static final boolean IS_DATA_ENABLE = false;
        protected static final boolean IS_HEADSET_ENABLE = true;
        protected static final boolean IS_SIGNAL_ENABLE = true;
        private volatile boolean isProximListen = false;
        private volatile boolean isTestCard = false;
        private ActivityManager mAm = null;
        private DataEntity mData = new DataEntity();
        private volatile boolean mIsPhoneListen = false;
        private volatile boolean mIsRegist = false;
        private int mLastCallSlotId = -1;
        private PhoneState mLastCallState;
        private volatile boolean mLastHeadsetPresent;
        private NetworkState mLastNwState;
        private volatile boolean mLastRFState;
        private volatile boolean mLastScreenOn;
        private SensorState mLastSensorState;
        private ServiceState[] mLastServiceState = null;
        private SignalStrength[] mLastSignalStrength = null;
        private volatile boolean mLastSwitchPortrait;
        private final IOemRFListener mListener;
        private OemSensorEventListener mOemSensorEventListener = null;
        private SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangedListener = null;
        private PhoneStateListener[] mPhoneStateListeners = null;
        Sensor mProximitySensor = null;
        private NWReceiver mReceiver = null;
        SensorManager mSensorManager = null;

        protected class OemSensorEventListener implements SensorEventListener {
            private static final float FAR_THRESHOLD = 5.0f;
            float mMaxValue;
            OrientationSensorEventListener mOrienListener;

            public OemSensorEventListener(OrientationSensorEventListener orienListener, float maxValue) {
                this.mOrienListener = orienListener;
                this.mMaxValue = maxValue;
            }

            public void onSensorChanged(SensorEvent event) {
                if (event.values != null && event.values.length != 0) {
                    this.mOrienListener.onSensorChange(getStateFromValue(event.values[0]));
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            private SensorState getStateFromValue(float value) {
                return (value > FAR_THRESHOLD || value == this.mMaxValue) ? SensorState.FAR : SensorState.NEAR;
            }
        }

        protected class OemPhoneStateListener extends PhoneStateListener {
            OrientationSensorEventListener mOrienListener;
            int mSlotId = -1;

            public OemPhoneStateListener(OrientationSensorEventListener orienListener, int slotId) {
                this.mOrienListener = orienListener;
                this.mSlotId = slotId;
            }

            public void onCallStateChanged(int value, String incomingNumber) {
                int mValue = PhoneFactory.getPhone(this.mSlotId).getState().ordinal();
                if (OemMTKRFSettings.DBG) {
                    Rlog.d(OemMTKRFSettings.TAG, "onCallStateChanged" + this.mSlotId + ":mValue:" + mValue + "/value:" + value);
                }
                if (mValue == value) {
                    this.mOrienListener.onCallStateChanged(this.mSlotId, value, incomingNumber);
                }
            }

            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                Phone phone = PhoneFactory.getPhone(this.mSlotId);
                if (phone != null && phone.getServiceStateTracker() != null) {
                    this.mOrienListener.onSignalStrengthsChanged(this.mSlotId, ((AbstractServiceStateTracker) OemTelephonyUtils.typeCasting(AbstractServiceStateTracker.class, phone.getServiceStateTracker())).getOrigSignalStrength());
                }
            }

            public void onServiceStateChanged(ServiceState serviceState) {
                this.mOrienListener.onServiceStateChanged(this.mSlotId, serviceState);
            }

            public int getSlotId() {
                return this.mSlotId;
            }
        }

        protected class OemSubscriptionsChangedListener extends SubscriptionManager.OnSubscriptionsChangedListener {
            OrientationSensorEventListener mOrienListener;

            public OemSubscriptionsChangedListener(OrientationSensorEventListener orienListener) {
                this.mOrienListener = orienListener;
            }

            public void onSubscriptionsChanged() {
                if (OemMTKRFSettings.DBG) {
                    Rlog.d(OemMTKRFSettings.TAG, "onSubscriptionsChanged");
                }
                this.mOrienListener.onSubscriptionsChanged();
            }
        }

        protected class NWReceiver extends BroadcastReceiver {
            OrientationSensorEventListener mOrienListener;

            public NWReceiver(OrientationSensorEventListener orienListener) {
                this.mOrienListener = orienListener;
            }

            public void onReceive(Context context, Intent intent) {
                String action;
                if (intent != null && (action = intent.getAction()) != null) {
                    if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                        NetworkInfo activeInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
                        if (OemMTKRFSettings.DBG) {
                            Rlog.d(OemMTKRFSettings.TAG, "NetworkInfo:" + activeInfo);
                        }
                        this.mOrienListener.onNetworkChanged(getNwState(activeInfo));
                    } else if (action.equals("android.intent.action.HEADSET_PLUG")) {
                        boolean headsetPresent = false;
                        if (intent.getIntExtra("state", 0) == 1) {
                            headsetPresent = true;
                        }
                        this.mOrienListener.onHeadsetStateChanged(headsetPresent);
                    }
                }
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
            this.mListener = listener;
            this.mLastCallState = PhoneState.IDLE;
            this.mLastNwState = NetworkState.IDLE;
            this.mLastSensorState = SensorState.FAR;
            this.mLastScreenOn = true;
            this.mLastHeadsetPresent = false;
            this.mLastRFState = false;
            this.mOnSubscriptionsChangedListener = new OemSubscriptionsChangedListener(this);
            Context context = PhoneFactory.getPhone(0).getContext();
            int numPhones = TelephonyManager.from(PhoneFactory.getPhone(0).getContext()).getPhoneCount();
            this.mLastServiceState = new ServiceState[numPhones];
            this.mLastSignalStrength = new SignalStrength[numPhones];
            this.mPhoneStateListeners = new OemPhoneStateListener[numPhones];
            for (int i = 0; i < numPhones; i++) {
                this.mLastServiceState[i] = new ServiceState();
                this.mLastSignalStrength[i] = new SignalStrength();
                this.mPhoneStateListeners[i] = new OemPhoneStateListener(this, i);
            }
            this.mSensorManager = (SensorManager) context.getSystemService("sensor");
            SensorManager sensorManager = this.mSensorManager;
            if (sensorManager != null) {
                this.mProximitySensor = sensorManager.getDefaultSensor(8);
                this.mOemSensorEventListener = new OemSensorEventListener(this, this.mProximitySensor.getMaximumRange());
            }
            this.mReceiver = new NWReceiver(this);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != EVENT_OEM_SET_RF_COMPLETE && i == EVENT_OEM_SCREEN_CHANGED) {
                AsyncResult arscreen = (AsyncResult) msg.obj;
                if (arscreen != null) {
                    boolean isOn = ((Boolean) arscreen.result).booleanValue();
                    if (OemConstant.SWITCH_LOG) {
                        Rlog.w(OemMTKRFSettings.TAG, "EVENT_OEM_SCREEN_CHANGED " + isOn);
                    }
                    onSreenChanged(isOn);
                    return;
                }
                Rlog.w(OemMTKRFSettings.TAG, "EVENT_OEM_SCREEN_CHANGED error");
            }
        }

        public static Context getContext() {
            return PhoneFactory.getPhone(0).getContext();
        }

        public synchronized void register() {
            if (!this.mIsRegist) {
                this.mIsRegist = true;
                registerSubInfo();
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.HEADSET_PLUG");
                getContext().registerReceiver(this.mReceiver, filter);
                registerSceeen();
            }
        }

        public synchronized void unregister() {
            if (this.mIsRegist) {
                this.mIsRegist = false;
                getContext().unregisterReceiver(this.mReceiver);
                unregisterSubInfo();
                unregisterPhone();
                unregisterScreen();
            }
        }

        public void registerSceeen() {
            OppoTelephonyController.getInstance(getContext()).registerForOemScreenChanged(this, EVENT_OEM_SCREEN_CHANGED, null);
        }

        public void unregisterScreen() {
            OppoTelephonyController.getInstance(getContext()).unregisterOemScreenChanged(this);
        }

        public void onSreenChanged(boolean isOn) {
            if (OemConstant.SWITCH_LOG) {
                Rlog.d(OemMTKRFSettings.TAG, "onSreenChange:" + isOn);
            }
            synchronized (this) {
                if (this.mLastScreenOn != isOn) {
                    this.mLastScreenOn = isOn;
                    processSwitch();
                }
            }
        }

        public void registerProximi() {
            if (!this.isProximListen && this.mSensorManager != null && this.mOemSensorEventListener != null) {
                if (OemConstant.SWITCH_LOG) {
                    Rlog.d(OemMTKRFSettings.TAG, "registerProximi");
                }
                this.isProximListen = true;
                this.mSensorManager.registerListener(this.mOemSensorEventListener, this.mProximitySensor, 2);
            }
        }

        public void unregisterProximi() {
            if (this.isProximListen && this.mSensorManager != null && this.mOemSensorEventListener != null) {
                if (OemConstant.SWITCH_LOG) {
                    Rlog.d(OemMTKRFSettings.TAG, "unregisterProximi");
                }
                this.isProximListen = false;
                this.mSensorManager.unregisterListener(this.mOemSensorEventListener);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0026, code lost:
            if (r2.mLastScreenOn != false) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x002c, code lost:
            if (r2.mLastSensorState != com.oppo.internal.telephony.rf.OemMTKRFSettings.SensorState.NEAR) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x002e, code lost:
            processSwitch();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
            return;
         */
        public void onSensorChange(SensorState state) {
            if (OemConstant.SWITCH_LOG) {
                Rlog.d(OemMTKRFSettings.TAG, "onSensorChange:" + state);
            }
            synchronized (this) {
                if (this.mLastSensorState != state) {
                    this.mLastSensorState = state;
                }
            }
        }

        public void registerSubInfo() {
            SubscriptionManager.from(getContext()).addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        }

        public void unregisterSubInfo() {
            SubscriptionManager.from(getContext()).removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        }

        public void onNetworkChanged(NetworkState state) {
            synchronized (this) {
                if (state != this.mLastNwState) {
                    this.mLastNwState = state;
                    this.mListener.onNetworkChange(this.mLastNwState);
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
                        tm.createForSubscriptionId(subId).listen(this.mPhoneStateListeners[i], OppoRIL.SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_TIME_OUT);
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

        /* JADX WARNING: Code restructure failed: missing block: B:33:0x006a, code lost:
            r1 = r3.mLastCallSlotId;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x006c, code lost:
            if (r1 < 0) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x006e, code lost:
            if (r1 <= 1) goto L_0x0071;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x0071, code lost:
            r3.mListener.onCallChange(r0);
            processSwitch();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x0079, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
            return;
         */
        public void onCallStateChanged(int slotId, int value, String incomingNumber) {
            PhoneState state = getCallStateFromValue(value);
            if (OemMTKRFSettings.DBG) {
                Rlog.d(OemMTKRFSettings.TAG, "onCallStateChanged[" + slotId + "]:" + state);
            }
            synchronized (this) {
                if (state == PhoneState.OFFHOOK || this.mLastCallSlotId == slotId) {
                    if (this.mLastCallState != PhoneState.OFFHOOK || state != PhoneState.OFFHOOK || this.mLastCallSlotId == slotId) {
                        if (state == this.mLastCallState) {
                            return;
                        }
                    }
                    this.mLastCallState = state;
                    if (state == PhoneState.OFFHOOK) {
                        this.mLastCallSlotId = slotId;
                        notifyCallStateChanged(true);
                        if (!this.isProximListen) {
                            registerProximi();
                        }
                    } else if (this.mLastCallSlotId == slotId) {
                        notifyCallStateChanged(false);
                        if (this.isProximListen) {
                            unregisterProximi();
                        }
                    }
                }
            }
        }

        public void onSubscriptionsChanged() {
            this.mListener.onSuscriptionChange();
            this.isTestCard = this.mListener.isTestCard();
            synchronized (this) {
                unregisterPhone();
                registerPhone();
            }
        }

        public void onSignalStrengthsChanged(int slotId, SignalStrength signalStrength) {
            if (slotId >= 0 && slotId <= 1) {
                this.mLastSignalStrength[slotId] = signalStrength;
                if (OemConstant.SWITCH_LOG) {
                    Rlog.d(OemMTKRFSettings.TAG, "onSignalStrengthsChanged[" + slotId + "]:" + this.mLastSignalStrength[slotId]);
                }
                if (!this.mLastRFState) {
                    processSwitch();
                }
            }
        }

        public void onServiceStateChanged(int slotId, ServiceState serviceState) {
            if (slotId >= 0 && slotId <= 1) {
                this.mLastServiceState[slotId] = serviceState;
                if (OemConstant.SWITCH_LOG) {
                    Rlog.d(OemMTKRFSettings.TAG, "onServiceStateChanged[" + slotId + "]:" + this.mLastServiceState[slotId]);
                }
                if (!this.mLastRFState) {
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x000e, code lost:
            android.telephony.Rlog.d(com.oppo.internal.telephony.rf.OemMTKRFSettings.TAG, "onHeadsetStateChanged:" + r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0024, code lost:
            processSwitch();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0027, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x000c, code lost:
            if (com.android.internal.telephony.OemConstant.SWITCH_LOG == false) goto L_0x0024;
         */
        public void onHeadsetStateChanged(boolean headsetPresent) {
            synchronized (this) {
                if (this.mLastHeadsetPresent != headsetPresent) {
                    this.mLastHeadsetPresent = headsetPresent;
                }
            }
        }

        private void notifyCallStateChanged(boolean isInCall) {
            try {
                for (Phone phone : PhoneFactory.getPhones()) {
                    DeviceStateMonitor deviceStateMonitor = ((AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, phone)).getDeviceStateMonitor();
                    if (deviceStateMonitor != null) {
                        ReflectionHelper.callMethod(deviceStateMonitor, "com.android.internal.telephony.DeviceStateMonitor", "notifyCallStateChange", new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(isInCall)});
                    }
                }
            } catch (Exception ex) {
                Rlog.w(OemMTKRFSettings.TAG, "notifyCallStateChanged:" + ex.getMessage());
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:14:0x004a, code lost:
            return;
         */
        private void processSwitch() {
            this.isTestCard = this.mListener.isTestCard();
            if (!this.isTestCard) {
                synchronized (this) {
                    if (OemMTKRFSettings.DBG) {
                        Rlog.d(OemMTKRFSettings.TAG, "onSwitchChange:" + this.mLastRFState);
                    }
                    boolean switch_rfstate = false;
                    if (!this.mLastRFState) {
                        if (!this.mLastRFState && !this.mLastScreenOn && this.mLastSensorState == SensorState.NEAR && this.mLastCallState == PhoneState.OFFHOOK && !this.mLastHeadsetPresent && isSignalThreshold()) {
                            switch_rfstate = true;
                        }
                        if (OemMTKRFSettings.DBG) {
                            Rlog.d(OemMTKRFSettings.TAG, "onSwitchChange double checke :" + switch_rfstate + "/" + this.mLastRFState);
                        }
                        if (switch_rfstate != this.mLastRFState) {
                            this.mLastRFState = switch_rfstate;
                            this.mListener.onSwitchChange(this.mLastRFState, null);
                            sendEmptyMessage(EVENT_OEM_SET_RF_COMPLETE);
                        }
                    } else if (this.mLastCallState == PhoneState.IDLE) {
                        this.mLastCallSlotId = -1;
                        this.mLastRFState = false;
                        this.mListener.onSwitchChange(this.mLastRFState, null);
                        sendEmptyMessage(EVENT_OEM_SET_RF_COMPLETE);
                    }
                }
            }
        }

        private PhoneState getCallStateFromValue(int value) {
            if (value == 2 || (value == 1 && this.mLastCallState == PhoneState.OFFHOOK)) {
                return PhoneState.OFFHOOK;
            }
            PhoneState phoneState = PhoneState.OFFHOOK;
            return PhoneState.IDLE;
        }

        private String getSignalThreshold() {
            int i = this.mLastCallSlotId;
            if (i < 0 || i > 1) {
                return "error slotid";
            }
            ServiceState[] serviceStateArr = this.mLastServiceState;
            if (serviceStateArr == null || this.mLastSignalStrength == null) {
                return "NULL";
            }
            int cs = getNetworkModeBySS(serviceStateArr[i].getRilVoiceRadioTechnology());
            int ps = getNetworkModeBySS(this.mLastServiceState[this.mLastCallSlotId].getRilDataRadioTechnology());
            if (cs == 1) {
                return " GSM:" + this.mLastSignalStrength[this.mLastCallSlotId].getGsmDbm();
            } else if (cs == 4 && !OemConstant.isCMCC(this.mLastServiceState[this.mLastCallSlotId].getOperatorNumeric())) {
                return " Wcdma:" + this.mLastSignalStrength[this.mLastCallSlotId].getGsmDbm();
            } else if (cs == 5 || cs == 6 || ps != 3) {
                return " UNKOWN";
            } else {
                return " LTE:" + this.mLastSignalStrength[this.mLastCallSlotId].getLteDbm();
            }
        }

        private boolean isSignalThreshold() {
            ServiceState[] serviceStateArr;
            int i = this.mLastCallSlotId;
            if (i < 0 || i > 1 || (serviceStateArr = this.mLastServiceState) == null || this.mLastSignalStrength == null) {
                return false;
            }
            int cs = getNetworkModeBySS(serviceStateArr[i].getRilVoiceRadioTechnology());
            int ps = getNetworkModeBySS(this.mLastServiceState[this.mLastCallSlotId].getRilDataRadioTechnology());
            if (OemMTKRFSettings.DBG) {
                Rlog.d(OemMTKRFSettings.TAG, "isSignalThreshold: cs:" + cs + ",ps:" + ps + ",gsm:" + this.mLastSignalStrength[this.mLastCallSlotId].getGsmDbm() + ",wcdma:" + this.mLastSignalStrength[this.mLastCallSlotId].getTdScdmaDbm() + ",lte:" + this.mLastSignalStrength[this.mLastCallSlotId].getLteDbm() + ",threshold:" + this.mData.toString());
            }
            return cs == 1 ? this.mLastSignalStrength[this.mLastCallSlotId].getGsmDbm() < this.mData.gsm : (cs != 4 || OemConstant.isCMCC(this.mLastServiceState[this.mLastCallSlotId].getOperatorNumeric())) ? cs != 5 && cs != 6 && ps == 3 && this.mLastSignalStrength[this.mLastCallSlotId].getLteDbm() < this.mData.lte : this.mLastSignalStrength[this.mLastCallSlotId].getTdScdmaDbm() < this.mData.wcdma;
        }

        private int getNetworkModeBySS(int nt) {
            if (nt == 1 || nt == 2 || nt == 16) {
                return 1;
            }
            if (nt == 3 || nt == 9 || nt == 10 || nt == 11 || nt == 15) {
                return 4;
            }
            if (nt == 4 || nt == 5 || nt == 6) {
                return 5;
            }
            if (nt == 7 || nt == 8 || nt == 12 || nt == 13) {
                return 6;
            }
            if (nt == 17) {
                return 2;
            }
            if (nt == 14 || nt == 19) {
                return 3;
            }
            return 0;
        }

        public void processSwitch(boolean isRfSwitch) {
            this.mLastSwitchPortrait = isRfSwitch;
            this.mListener.onSwitchChange(isRfSwitch, null);
            sendEmptyMessage(EVENT_OEM_SET_RF_COMPLETE);
        }

        public synchronized void setInitValue(DataEntity data) {
            this.mData.isSwitch = data.isSwitch;
            this.mData.gsm = data.gsm;
            this.mData.wcdma = data.wcdma;
            this.mData.lte = data.lte;
        }
    }

    public static void showToast(Context context, String msg) {
        try {
            ((NotificationManager) context.getSystemService("notification")).notify(8888, new Notification.Builder(context).setTicker("RF Switching").setSmallIcon(17301624).setContentTitle("RF Switching").setContentText(msg).setWhen(System.currentTimeMillis()).build());
        } catch (Exception ex) {
            Rlog.d(TAG, "OemMTKRFSettings: showToast " + ex.getMessage());
        }
    }

    public static void vibrateForAccepted(Context context, int timer) {
        ((Vibrator) context.getSystemService("vibrator")).vibrate((long) timer);
    }

    public OemMTKRFSettings(Context context) {
        this(context, null);
    }

    public OemMTKRFSettings(Context context, IOemRFListener listener) {
        this.mOrientationListener = null;
        this.mListener = new IOemRFListener() {
            /* class com.oppo.internal.telephony.rf.OemMTKRFSettings.AnonymousClass1 */
            boolean isTestCard = false;

            @Override // com.oppo.internal.telephony.rf.OemMTKRFSettings.IOemRFListener
            public void onSuscriptionChange() {
                this.isTestCard = initTestCard();
            }

            @Override // com.oppo.internal.telephony.rf.OemMTKRFSettings.IOemRFListener
            public void onCallChange(PhoneState state) {
            }

            @Override // com.oppo.internal.telephony.rf.OemMTKRFSettings.IOemRFListener
            public void onNetworkChange(NetworkState nwState) {
            }

            @Override // com.oppo.internal.telephony.rf.OemMTKRFSettings.IOemRFListener
            public boolean isTestCard() {
                return this.isTestCard;
            }

            @Override // com.oppo.internal.telephony.rf.OemMTKRFSettings.IOemRFListener
            public void onSwitchChange(boolean isRfSwitch, Message msg) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(PhoneFactory.getPhone(0).getContext()).edit();
                editor.putBoolean(OemMTKRFSettings.PREF, isRfSwitch);
                editor.apply();
                Phone[] phones = PhoneFactory.getPhones();
                for (Phone phone : phones) {
                    if (phone != null) {
                        ((AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, phone)).oppoSetTunerLogic(!isRfSwitch, msg);
                    }
                }
                if (OemMTKRFSettings.DBG) {
                    Rlog.d(OemMTKRFSettings.TAG, "OemMTKRFSettings: switch " + isRfSwitch);
                }
            }

            private synchronized boolean initTestCard() {
                IccCard iccCard;
                Phone[] phones = PhoneFactory.getPhones();
                for (Phone phone : phones) {
                    if (phone != null && (iccCard = phone.getIccCard()) != null && iccCard.hasIccCard()) {
                        return ((AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, phone)).is_test_card();
                    }
                }
                return true;
            }
        };
        if (listener != null) {
            this.mListener = listener;
        }
        this.mOrientationListener = new OrientationSensorEventListener(this.mListener);
        if (DBG) {
            Rlog.d(TAG, "OemMTKRFSettings: init and release first");
        }
    }

    public static synchronized OemMTKRFSettings getDefault(Context context) {
        OemMTKRFSettings oemMTKRFSettings;
        synchronized (OemMTKRFSettings.class) {
            if (sInstance == null) {
                sInstance = new OemMTKRFSettings(context);
            }
            oemMTKRFSettings = sInstance;
        }
        return oemMTKRFSettings;
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

    public void restore(Context context) {
        if (getLastRFState(context) && this.mOrientationListener != null) {
            if (DBG) {
                Rlog.d(TAG, "OemMTKRFSettings: restore to default");
            }
            this.mOrientationListener.processSwitch(false);
        }
    }

    public void setInitValue(DataEntity data) {
        OrientationSensorEventListener orientationSensorEventListener = this.mOrientationListener;
        if (orientationSensorEventListener != null) {
            orientationSensorEventListener.setInitValue(data);
        }
    }

    public static boolean getLastRFState(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF, false);
    }
}
