package com.oppo.internal.telephony.nwdiagnose;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.OppoCallManagerImpl;
import com.oppo.internal.telephony.OppoRIL;
import com.oppo.internal.telephony.utils.OppoManagerHelper;
import java.util.HashMap;

public class NetworkDiagnoseService {
    public static final int EVENT_ICC_CHANGED = 103;
    public static final int EVENT_SHUTDOWN_CHANGED = 102;
    public static final int EVT_SAVA_KEY_LOG = 101;
    public static final String ODI_EVENTID_SIGNALSTRENGTH = "050201";
    public static final String SERVICE_NAME = "nwdiagnose";
    private static final String TAG = "NetworkDs";
    private static Handler mEventHandler;
    private static NetworkDiagnoseService mInstance;
    private static final Object mLock = new Object();
    private static HandlerThread sHandlerThread = new HandlerThread(TAG);
    /* access modifiers changed from: private */
    public static OppoPhoneStateMonitor[] sPhoneStateMonitor = null;
    private IccCardStatus.CardState[] mCardState = null;
    private ConnectivityManager mConManager;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public int mPhoneNum = 0;
    private StateReceiver mReceiver;
    private boolean mRegistered = false;
    private int[] mSimState = null;
    private UiccController mUiccController;

    public String getApConfigInfoInner() {
        StringBuilder builder = new StringBuilder("nt:(");
        for (int i = 0; i < this.mPhoneNum; i++) {
            if (i != 0) {
                builder.append(",");
            }
            builder.append(sPhoneStateMonitor[i].getPreferredNetworkType());
        }
        builder.append(");");
        builder.append("sim:(");
        for (int i2 = 0; i2 < this.mPhoneNum; i2++) {
            if (i2 != 0) {
                builder.append(",");
            }
            builder.append(this.mSimState[i2]);
        }
        builder.append(");");
        builder.append("ds:");
        builder.append(getDefaultDataSubId());
        return builder.toString();
    }

    public String getServiceStateInfoInner() {
        StringBuilder builder = new StringBuilder("ss:(");
        for (int i = 0; i < this.mPhoneNum; i++) {
            if (i != 0) {
                builder.append(";");
            }
            builder.append(sPhoneStateMonitor[i].getServiceStateDesc());
        }
        builder.append(")");
        return builder.toString();
    }

    public String getSignalInfoInner() {
        StringBuilder builder = new StringBuilder("sig:(");
        for (int i = 0; i < this.mPhoneNum; i++) {
            if (i != 0) {
                builder.append(",");
            }
            builder.append(sPhoneStateMonitor[i].getSignalStrengthDesc());
        }
        builder.append(")");
        return builder.toString();
    }

    public String getCellInfoInner() {
        StringBuilder builder = new StringBuilder("cell:(");
        for (int i = 0; i < this.mPhoneNum; i++) {
            if (i != 0) {
                builder.append(",");
            }
            builder.append(sPhoneStateMonitor[i].getCellInfoDesc());
        }
        builder.append(")");
        return builder.toString();
    }

    public String getOtherInfoInner() {
        NetworkInfo networkInfo;
        StringBuilder builder = new StringBuilder("Apn:(");
        ConnectivityManager connectivityManager = this.mConManager;
        if (!(connectivityManager == null || (networkInfo = connectivityManager.getActiveNetworkInfo()) == null || networkInfo.getType() != 0)) {
            builder.append(this.mConManager.getActiveNetworkInfo().getExtraInfo());
        }
        builder.append(")");
        return builder.toString();
    }

    public String getCallCellInfoInner(int phoneId) {
        if (phoneId < 0 || phoneId >= this.mPhoneNum) {
            return "";
        }
        return "CallCell:[" + sPhoneStateMonitor[phoneId].getCallCellInfoDesc() + "]";
    }

    public int getDefaultDataSubId() {
        return SubscriptionManager.getSlotIndex(SubscriptionManager.getDefaultDataSubscriptionId());
    }

    public int getPreferredNetworkType(int slotId, int subId) {
        if (SubscriptionManager.from(this.mContext).isActiveSubId(subId)) {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            return Settings.Global.getInt(contentResolver, "preferred_network_mode" + subId, -1);
        }
        try {
            return TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", slotId);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "getPreferredNetworkType error:" + e);
            return -1;
        }
    }

    public static NetworkDiagnoseService getInstance() {
        return mInstance;
    }

    public static NetworkDiagnoseService make(Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new NetworkDiagnoseService(context);
            }
        }
        return mInstance;
    }

    private NetworkDiagnoseService(Context context) {
        this.mContext = context;
        this.mConManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        sHandlerThread.start();
        mEventHandler = new EventHandler(sHandlerThread.getLooper());
        this.mPhoneNum = TelephonyManager.getDefault().getPhoneCount();
        int i = this.mPhoneNum;
        sPhoneStateMonitor = new OppoPhoneStateMonitor[i];
        this.mSimState = new int[i];
        Rlog.d(TAG, "NetworkDiagnoseService.." + this.mPhoneNum);
        this.mReceiver = new StateReceiver();
        registerStateReceiver(context);
        this.mCardState = new IccCardStatus.CardState[this.mPhoneNum];
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(mEventHandler, 103, (Object) null);
        int airMode = Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0);
        for (int i2 = 0; i2 < this.mPhoneNum; i2++) {
            Phone phone = PhoneFactory.getPhone(i2);
            if (phone != null) {
                sPhoneStateMonitor[i2] = new OppoPhoneStateMonitor(phone, mEventHandler);
                sPhoneStateMonitor[i2].updateAirPlaneMode(airMode);
                if (i2 == 0) {
                    ((AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, phone)).registerForShutDownChanged(mEventHandler, 102, (Object) null);
                }
            }
        }
    }

    public void registerStateReceiver(Context context) {
        Rlog.d(TAG, "registerStateReceiver..");
        if (!this.mRegistered) {
            this.mRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.DATE_CHANGED");
            filter.addAction("android.intent.action.AIRPLANE_MODE");
            context.registerReceiver(this.mReceiver, filter);
        }
    }

    public void unRegisterStateReceiver(Context context) {
        Rlog.d(TAG, "unRegisterStateReceiver..");
        if (this.mRegistered) {
            this.mRegistered = false;
            context.unregisterReceiver(this.mReceiver);
        }
    }

    public String getApnInfo() {
        ConnectivityManager conManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (conManager == null || conManager.getActiveNetworkInfo() == null) {
            return "";
        }
        return conManager.getActiveNetworkInfo().getExtraInfo();
    }

    public OppoPhoneStateMonitor getPhoneStateMonitor(int phoneId) {
        OppoPhoneStateMonitor[] oppoPhoneStateMonitorArr = sPhoneStateMonitor;
        if (oppoPhoneStateMonitorArr != null && phoneId >= 0 && phoneId < oppoPhoneStateMonitorArr.length) {
            return oppoPhoneStateMonitorArr[phoneId];
        }
        Rlog.d(TAG, "getPhoneStateMonitor null");
        return null;
    }

    public void handleRecordStateInfo() {
        Log.e(TAG, "handleRecordStateInfo...");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sPhoneStateMonitor.length; i++) {
            if (i != 0) {
                sb.append("&");
            }
            sb.append("[");
            sb.append(sPhoneStateMonitor[i].fetchSignalRecord());
            sb.append("]");
        }
        Message msg = mEventHandler.obtainMessage();
        msg.what = 101;
        msg.arg1 = OppoRIL.SYS_OEM_NW_DIAG_CAUSE_SIGNAL_STATISTIC;
        msg.obj = sb.toString();
        mEventHandler.sendMessage(msg);
    }

    class StateReceiver extends BroadcastReceiver {
        StateReceiver() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:12:0x0042  */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x0064  */
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            Rlog.d(NetworkDiagnoseService.TAG, "StateReceiver action:" + action);
            int hashCode = action.hashCode();
            if (hashCode != -1076576821) {
                if (hashCode == 1041332296 && action.equals("android.intent.action.DATE_CHANGED")) {
                    c = 0;
                    if (c == 0) {
                        NetworkDiagnoseService.this.handleRecordStateInfo();
                        return;
                    } else if (c == 1) {
                        int airMode = Settings.System.getInt(context.getContentResolver(), "airplane_mode_on", 0);
                        for (int i = 0; i < NetworkDiagnoseService.this.mPhoneNum; i++) {
                            NetworkDiagnoseService.sPhoneStateMonitor[i].updateAirPlaneMode(airMode);
                        }
                        return;
                    } else {
                        return;
                    }
                }
            } else if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                c = 1;
                if (c == 0) {
                }
            }
            c = 65535;
            if (c == 0) {
            }
        }
    }

    /* access modifiers changed from: private */
    public String oppoGetStringFromType(int type) {
        if (type != 212) {
            return "";
        }
        return OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_RF_SIGNAL_STATIS;
    }

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper, null, false);
        }

        public void handleMessage(Message msg) {
            Rlog.d(NetworkDiagnoseService.TAG, "EventHandler:" + msg.what);
            switch (msg.what) {
                case 101:
                    if (msg.obj != null && (msg.obj instanceof String)) {
                        int issueType = msg.arg1;
                        String logInfo = (String) msg.obj;
                        String issue = NetworkDiagnoseService.this.oppoGetStringFromType(issueType);
                        try {
                            Context access$300 = NetworkDiagnoseService.this.mContext;
                            String log_string = OemTelephonyUtils.getOemRes(access$300, "zz_oppo_critical_log_" + issueType, "");
                            if (log_string.equals("")) {
                                Rlog.e(NetworkDiagnoseService.TAG, "Can not get resource of identifier zz_oppo_critical_log_" + issueType);
                                return;
                            }
                            String[] log_array = log_string.split(",");
                            int writeSize = OppoManagerHelper.writeLogToPartition(Integer.valueOf(log_array[0]).intValue(), logInfo, issue, log_array[1]);
                            if (issueType == 212) {
                                HashMap<String, String> signalRecord = new HashMap<>();
                                signalRecord.put(issue, logInfo);
                                OppoManagerHelper.onStamp(NetworkDiagnoseService.ODI_EVENTID_SIGNALSTRENGTH, signalRecord);
                                Rlog.d(NetworkDiagnoseService.TAG, "onStamp :" + issueType);
                            }
                            Rlog.d(NetworkDiagnoseService.TAG, "writeLogToPartition logTag:" + issueType + ",log:" + logInfo + ",size:" + writeSize);
                            return;
                        } catch (Exception e) {
                            Rlog.e(NetworkDiagnoseService.TAG, "Can not get resource of identifier zz_oppo_critical_log_" + issueType + ", Exception = " + e);
                            return;
                        }
                    } else {
                        return;
                    }
                case 102:
                    NetworkDiagnoseService.this.handleRecordStateInfo();
                    OppoCallManagerImpl.getInstance().updateCallRecord(NetworkDiagnoseService.this.mContext);
                    return;
                case 103:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    new Integer(0);
                    if (ar.result != null) {
                        NetworkDiagnoseService.this.updateIccAvailability(((Integer) ar.result).intValue());
                        return;
                    }
                    Rlog.e(NetworkDiagnoseService.TAG, "Error: Invalid card index EVENT_ICC_CHANGED ");
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateIccAvailability(int slotId) {
        if (this.mUiccController != null) {
            if (PhoneFactory.getPhone(slotId).isShuttingDown()) {
                Rlog.d(TAG, "Device is shutting down. No need update now.");
                return;
            }
            IccCardStatus.CardState newState = IccCardStatus.CardState.CARDSTATE_ABSENT;
            UiccCard newCard = this.mUiccController.getUiccCard(slotId);
            if (newCard != null) {
                newState = newCard.getCardState();
            }
            IccCardStatus.CardState[] cardStateArr = this.mCardState;
            IccCardStatus.CardState oldState = cardStateArr[slotId];
            cardStateArr[slotId] = newState;
            if (newState != oldState) {
                Rlog.d(TAG, "Slot[" + slotId + "]: New Card State = " + newState + " Old Card State = " + oldState);
                if (oldState == IccCardStatus.CardState.CARDSTATE_PRESENT && newState != IccCardStatus.CardState.CARDSTATE_PRESENT) {
                    Rlog.d(TAG, "SIM" + (slotId + 1) + " hot plug out");
                    this.mSimState[slotId] = 0;
                    sPhoneStateMonitor[slotId].updateUiccAvailable(false);
                } else if (oldState == IccCardStatus.CardState.CARDSTATE_ABSENT && newState == IccCardStatus.CardState.CARDSTATE_PRESENT) {
                    Rlog.d(TAG, "SIM" + (slotId + 1) + " hot plug in");
                    this.mSimState[slotId] = 1;
                    sPhoneStateMonitor[slotId].updateUiccAvailable(true);
                }
            }
        }
    }
}
