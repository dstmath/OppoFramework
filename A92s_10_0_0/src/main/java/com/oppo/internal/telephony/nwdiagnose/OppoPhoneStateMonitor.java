package com.oppo.internal.telephony.nwdiagnose;

import android.annotation.UnsupportedAppUsage;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityTdscdma;
import android.telephony.CellIdentityWcdma;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.OppoSignalStrength;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.OppoCallStateMonitor;
import com.oppo.internal.telephony.OppoServiceStateTracker;
import com.oppo.internal.telephony.OppoTelephonyController;
import com.oppo.internal.telephony.explock.RegionLockConstant;
import com.oppo.internal.telephony.utils.OppoServiceStateTrackerUtil;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class OppoPhoneStateMonitor {
    public static final String ACTION_INFORM_LTE_QUALITY = "android.intent.action.Smart5g_ltequality";
    public static final String ACTION_INFORM_RAT_CHANGED = "android.intent.action.RatChange";
    public static final String ACTION_SMART5G_LTE_POOR_THRES_CONFIG = "android.intent.action.Smart5g_ltePoorThres";
    private static final int EVENT_OEM_SCREEN_CHANGED = 2;
    private static final int EVENT_SERVICE_STATE_CHANGED = 1;
    public static final int MAX_LTE_RSRP = -44;
    private static final int MIN_BS_STAY_TIME = 10;
    public static final int MIN_LTE_RSRP = -140;
    private static final int POOR_BS_SIZE = 6;
    private static final int POOR_SIGNAL_LEVEL = 2;
    private static final String TAG = "phonestatemonitor";
    private int mAirplaneMode = 0;
    private CellInfo mCallCellInfo;
    private CellInfo mCellInfo;
    private Handler mEventHandler;
    private Handler mHandler = new Handler() {
        /* class com.oppo.internal.telephony.nwdiagnose.OppoPhoneStateMonitor.AnonymousClass1 */

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    OppoPhoneStateMonitor.this.onServiceStateChanged((ServiceState) ar.result);
                }
            } else if (i == 2) {
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.exception == null) {
                    OppoPhoneStateMonitor.this.onScreenStateChanged(((Boolean) ar2.result).booleanValue());
                }
            }
        }
    };
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.oppo.internal.telephony.nwdiagnose.OppoPhoneStateMonitor.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (((action.hashCode() == -1362716906 && action.equals(OppoPhoneStateMonitor.ACTION_SMART5G_LTE_POOR_THRES_CONFIG)) ? (char) 0 : 65535) != 0) {
                Rlog.d(OppoPhoneStateMonitor.TAG, "Unexpected broadcast intent: " + intent);
                return;
            }
            OppoPhoneStateMonitor.this.mSmart5gLtePoorRsrpThres = intent.getIntExtra("ltePoorRsrpThres", -115);
            OppoPhoneStateMonitor.this.mSmart5gLtePoorRsrqThres = intent.getIntExtra("ltePoorRsrqThres", -15);
            OppoPhoneStateMonitor.this.mSmart5gLtePoorBwThres = intent.getIntExtra("ltePoorBwThres", 1400);
            Rlog.d(OppoPhoneStateMonitor.TAG, "ltePoorRsrpThres is " + OppoPhoneStateMonitor.this.mSmart5gLtePoorRsrpThres + "ltePoorRsrqThres is " + OppoPhoneStateMonitor.this.mSmart5gLtePoorRsrqThres + "ltePoorBwThres is " + OppoPhoneStateMonitor.this.mSmart5gLtePoorBwThres);
        }
    };
    private boolean mIsLtePoor = false;
    protected boolean mIsScreenOn = true;
    private int mLastNetworkClass = 0;
    private int mLastSignalLevel = 0;
    private long mLastStatisTime = 0;
    private long mLastStatisTime1 = 0;
    private boolean mMonitoring = false;
    /* access modifiers changed from: private */
    public int mNetworkMode = Phone.PREFERRED_NT_MODE;
    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public RegistrantList mNetworkTypeChangedRegistrants = new RegistrantList();
    private SignalStrength mOrginalSignalStrength;
    /* access modifiers changed from: private */
    public Phone mPhone;
    private SortedSet<CellInfo> mPoorSignalBs = new TreeSet();
    private ContentObserver mPrefNetworkModeObserver = new ContentObserver(this.mHandler) {
        /* class com.oppo.internal.telephony.nwdiagnose.OppoPhoneStateMonitor.AnonymousClass3 */

        public void onChange(boolean selfChange) {
            OppoPhoneStateMonitor oppoPhoneStateMonitor = OppoPhoneStateMonitor.this;
            int unused = oppoPhoneStateMonitor.mNetworkMode = oppoPhoneStateMonitor.getNetworkModeFromDB();
            OppoPhoneStateMonitor.this.mNetworkTypeChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(OppoPhoneStateMonitor.this.mNetworkMode), (Throwable) null));
            Rlog.d(OppoPhoneStateMonitor.TAG, "PrefNetworkModeObserver mNetworkMode:" + OppoPhoneStateMonitor.this.mNetworkMode);
        }
    };
    /* access modifiers changed from: private */
    public int mPreviousSubId = -1;
    private ServiceState mSS;
    private long[] mServiceStateStatis = {0, 0, 0, 0, 0};
    private long[] mSignalLevelStatis = {0, 0, 0, 0, 0};
    private SignalStrength mSignalStrength;
    private boolean mSimInserted = false;
    protected int mSmart5gLtePoorBwThres = 1400;
    protected int mSmart5gLtePoorRsrpThres = -115;
    protected int mSmart5gLtePoorRsrqThres = -15;
    private long mStartStatisTime = 0;
    private long mStartStatisTime1 = 0;

    public OppoPhoneStateMonitor(Phone phone, Handler eventHandler) {
        this.mPhone = phone;
        this.mEventHandler = eventHandler;
        this.mSignalStrength = new SignalStrength();
        this.mOrginalSignalStrength = new SignalStrength();
        this.mSS = new ServiceState();
        this.mSS.setStateOutOfService();
        this.mCellInfo = new CellInfo();
        this.mCallCellInfo = new CellInfo();
        Rlog.d(TAG, "OppoPhoneStateMonitor:" + this.mPhone.getPhoneId());
        SubscriptionManager.from(this.mPhone.getContext()).addOnSubscriptionsChangedListener(new SubscriptionManager.OnSubscriptionsChangedListener(this.mHandler.getLooper()) {
            /* class com.oppo.internal.telephony.nwdiagnose.OppoPhoneStateMonitor.AnonymousClass2 */

            public void onSubscriptionsChanged() {
                int subId = OppoPhoneStateMonitor.this.mPhone.getSubId();
                if (OppoPhoneStateMonitor.this.mPreviousSubId != subId) {
                    Rlog.d(OppoPhoneStateMonitor.TAG, "subId:" + subId);
                    int unused = OppoPhoneStateMonitor.this.mPreviousSubId = subId;
                    OppoPhoneStateMonitor.this.registerPrefNetworkModeObserver();
                }
            }
        });
        registerPrefNetworkModeObserver();
        this.mPhone.registerForServiceStateChanged(this.mHandler, 1, (Object) null);
        OppoTelephonyController.getInstance(this.mPhone.getContext()).registerForOemScreenChanged(this.mHandler, 2, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SMART5G_LTE_POOR_THRES_CONFIG);
        this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter);
    }

    /* access modifiers changed from: private */
    public void registerPrefNetworkModeObserver() {
        int subId = this.mPhone.getSubId();
        unregisterPrefNetworkModeObserver();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
            contentResolver.registerContentObserver(Settings.Global.getUriFor("preferred_network_mode" + subId), true, this.mPrefNetworkModeObserver);
        }
    }

    private void unregisterPrefNetworkModeObserver() {
        this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mPrefNetworkModeObserver);
    }

    /* access modifiers changed from: private */
    public int getNetworkModeFromDB() {
        int i = Phone.PREFERRED_NT_MODE;
        int subId = this.mPhone.getSubId();
        if (SubscriptionController.getInstance().isActiveSubId(subId)) {
            ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
            return Settings.Global.getInt(contentResolver, "preferred_network_mode" + subId, Phone.PREFERRED_NT_MODE);
        }
        try {
            return TelephonyManager.getIntAtIndex(this.mPhone.getContext().getContentResolver(), "preferred_network_mode", this.mPhone.getPhoneId());
        } catch (Settings.SettingNotFoundException e) {
            return Phone.PREFERRED_NT_MODE;
        }
    }

    public boolean isDataEnabled() {
        return this.mPhone.isUserDataEnabled();
    }

    public int getSimType() {
        SubscriptionInfo sir;
        int subId = this.mPhone.getSubId();
        if (subId >= 0 && (sir = SubscriptionManager.from(this.mPhone.getContext()).getActiveSubscriptionInfo(subId)) != null) {
            return sir.getIconTint();
        }
        return -1;
    }

    public void updateAirPlaneMode(int mode) {
        if (this.mAirplaneMode != mode) {
            Rlog.d(TAG, "updateAirPlaneMode[" + this.mPhone.getPhoneId() + "] mAirplaneMode:" + this.mAirplaneMode);
            this.mAirplaneMode = mode;
            updateRecordState();
        }
    }

    public void updateUiccAvailable(boolean isInsert) {
        Rlog.d(TAG, "updateUiccAvailable[" + this.mPhone.getPhoneId() + "] isInsert:" + isInsert);
        if (isInsert != this.mSimInserted) {
            this.mSimInserted = isInsert;
            if (this.mSimInserted) {
                this.mNetworkMode = getNetworkModeFromDB();
            }
            updateRecordState();
        }
    }

    private void updateRecordState() {
        if (!this.mSimInserted || this.mAirplaneMode != 0 || !this.mIsScreenOn) {
            if (this.mMonitoring) {
                this.mMonitoring = false;
                updateRecordSignalStrength();
                this.mStartStatisTime = 0;
                this.mLastStatisTime = 0;
                this.mLastSignalLevel = 0;
                updateRecordServiceState();
                this.mStartStatisTime1 = 0;
                this.mLastStatisTime1 = 0;
                this.mLastNetworkClass = 0;
            }
        } else if (!this.mMonitoring) {
            this.mMonitoring = true;
        }
        Rlog.d(TAG, "mMonitoring11 :" + this.mMonitoring);
    }

    public int getOemRegState(ServiceState ss) {
        int regState = ss.getVoiceRegState();
        int dataRegState = ss.getDataRegState();
        return (regState == 1 && dataRegState == 0) ? dataRegState : regState;
    }

    public String fetchSignalRecord() {
        long current = SystemClock.elapsedRealtime();
        updateRecordSignalStrength();
        updateRecordServiceState();
        if (this.mCellInfo.mScored > 10) {
            updateCellInfo();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("slot:" + this.mPhone.getPhoneId() + ",mSimInserted :" + this.mSimInserted);
        if (this.mSimInserted) {
            sb.append(";sim:" + getSimType());
            StringBuilder sb2 = new StringBuilder();
            sb2.append(";ms:");
            sb2.append(isDataEnabled() ? "1" : "0");
            sb.append(sb2.toString());
            sb.append(";sig:[");
            for (int i = 0; i < this.mSignalLevelStatis.length; i++) {
                if (i != 0) {
                    sb.append(",");
                }
                sb.append("" + this.mSignalLevelStatis[i]);
            }
            sb.append("]");
            sb.append(";ss:[");
            for (int i2 = 0; i2 < this.mServiceStateStatis.length; i2++) {
                if (i2 != 0) {
                    sb.append(",");
                }
                sb.append("" + this.mServiceStateStatis[i2]);
            }
            sb.append("]");
            sb.append(";bs:[");
            Iterator<CellInfo> it = this.mPoorSignalBs.iterator();
            while (it.hasNext()) {
                sb.append("(" + it.next().toString() + ")");
            }
            sb.append("]");
        } else {
            sb.append(";sim:no");
        }
        this.mPoorSignalBs.clear();
        this.mCellInfo.mScored = 0;
        int i3 = 0;
        while (true) {
            long[] jArr = this.mSignalLevelStatis;
            if (i3 >= jArr.length) {
                break;
            }
            jArr[i3] = 0;
            i3++;
        }
        int i4 = 0;
        while (true) {
            long[] jArr2 = this.mServiceStateStatis;
            if (i4 < jArr2.length) {
                jArr2[i4] = 0;
                i4++;
            } else {
                this.mStartStatisTime = current;
                this.mLastStatisTime = current;
                this.mStartStatisTime1 = current;
                this.mLastStatisTime1 = current;
                Rlog.d(TAG, "fetchSignalRecord sb:" + sb.toString());
                return sb.toString();
            }
        }
    }

    private void updateRecordSignalStrength() {
        int OEMLevel_0 = ((OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, this.mSignalStrength)).getOEMLevel_0();
        Rlog.i(TAG, "updateRecordSignalStrength :" + getOemRegState(this.mSS) + ",oldlevel:" + this.mLastSignalLevel + ",newLevel:" + OEMLevel_0);
        if (getOemRegState(this.mSS) == 0) {
            long current = SystemClock.elapsedRealtime();
            if (this.mStartStatisTime > 0) {
                long j = this.mLastStatisTime;
                if (current > j) {
                    long duration = (current - j) / 1000;
                    int i = this.mLastSignalLevel;
                    if (i >= 0) {
                        long[] jArr = this.mSignalLevelStatis;
                        if (i < jArr.length) {
                            jArr[i] = jArr[i] + duration;
                        }
                    }
                    if (this.mLastSignalLevel < 2) {
                        this.mCellInfo.mScored += duration;
                    }
                    this.mLastStatisTime = current;
                    this.mLastSignalLevel = OEMLevel_0;
                }
            }
            if (this.mStartStatisTime == 0) {
                this.mStartStatisTime = current;
            }
            this.mLastStatisTime = current;
            this.mLastSignalLevel = OEMLevel_0;
        }
    }

    public void onScreenStateChanged(boolean screenOn) {
        this.mIsScreenOn = screenOn;
        updateRecordState();
    }

    private void updateRecordServiceState() {
        int nt = this.mSS.getRilDataRadioTechnology();
        if (nt == 0) {
            nt = this.mSS.getRilVoiceRadioTechnology();
        }
        int networkClass = TelephonyManager.getNetworkClass(ServiceState.rilRadioTechnologyToNetworkType(nt));
        Rlog.d(TAG, "updateRecordServiceState :" + nt + ",oldlevel:" + this.mLastNetworkClass + ",networkClass:" + networkClass);
        long current = SystemClock.elapsedRealtime();
        if (this.mStartStatisTime1 > 0) {
            long j = this.mLastStatisTime1;
            if (current > j) {
                long duration = (current - j) / 1000;
                int i = this.mLastNetworkClass;
                if (i >= 0) {
                    long[] jArr = this.mServiceStateStatis;
                    if (i < jArr.length) {
                        jArr[i] = jArr[i] + duration;
                    }
                }
                this.mLastStatisTime1 = current;
                this.mLastNetworkClass = networkClass;
            }
        }
        if (this.mStartStatisTime1 == 0) {
            this.mStartStatisTime1 = current;
        }
        this.mLastStatisTime1 = current;
        this.mLastNetworkClass = networkClass;
    }

    public void onSignalStrengthChanged(SignalStrength signal) {
        if (signal != null) {
            SignalStrength signalStrength = this.mSignalStrength;
            if (signalStrength == null) {
                this.mSignalStrength = new SignalStrength(signal);
            } else {
                OppoServiceStateTrackerUtil.copyFrom(signalStrength, signal);
            }
            if (this.mMonitoring && getOemRegState(this.mSS) == 0) {
                updateRecordSignalStrength();
            }
            smart5gUpdateLteQuality(signal);
        }
    }

    public void onOriginalSignalSignalStrengthChanged(SignalStrength signal) {
        Rlog.d(TAG, "origSignal:" + signal);
        if (signal != null) {
            if (this.mOrginalSignalStrength == null) {
                this.mOrginalSignalStrength = new SignalStrength(signal);
            } else {
                OppoServiceStateTrackerUtil.copyFrom(this.mSignalStrength, signal);
            }
        }
    }

    public void onServiceStateChanged(ServiceState ss) {
        if (ss != null) {
            checkRatTechChanged(this.mSS, ss);
            this.mSS = new ServiceState(ss);
            NetworkRegistrationInfo networkState = this.mSS.getNetworkRegistrationInfo(2, 1);
            if (networkState == null || networkState.getCellIdentity() == null) {
                networkState = this.mSS.getNetworkRegistrationInfo(1, 1);
            }
            if (networkState != null) {
                onCellLocationChanged(networkState.getCellIdentity());
            }
            if (this.mMonitoring) {
                updateRecordServiceState();
            }
        }
    }

    private void updateCellInfo() {
        boolean isExist = false;
        if (!this.mCellInfo.isValid()) {
            Rlog.d(TAG, "update return");
            return;
        }
        updateRecordSignalStrength();
        Iterator<CellInfo> it = this.mPoorSignalBs.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            CellInfo ci = it.next();
            if (ci.equals(this.mCellInfo)) {
                ci.mScored += this.mCellInfo.mScored;
                isExist = true;
                break;
            }
        }
        if (!isExist) {
            this.mPoorSignalBs.add(this.mCellInfo);
            if (this.mPoorSignalBs.size() >= 6) {
                SortedSet<CellInfo> sortedSet = this.mPoorSignalBs;
                sortedSet.remove(sortedSet.last());
            }
        }
    }

    public void onCellLocationChanged(CellIdentity cellIdentity) {
        if (cellIdentity == null) {
            this.mCellInfo = new CellInfo();
            return;
        }
        CellInfo cellInfo = new CellInfo();
        cellInfo.mType = cellIdentity.getType();
        cellInfo.mArfcnl = cellIdentity.getChannelNumber();
        int type = cellIdentity.getType();
        if (type == 1) {
            cellInfo.mMccMnc = ((CellIdentityGsm) cellIdentity).getMccString() + ((CellIdentityGsm) cellIdentity).getMncString();
            cellInfo.mCellId1 = ((CellIdentityGsm) cellIdentity).getCid();
            cellInfo.mCellId2 = ((CellIdentityGsm) cellIdentity).getLac();
        } else if (type == 2) {
            cellInfo.mMccMnc = this.mSS.getOperatorNumeric();
            cellInfo.mCellId1 = ((CellIdentityCdma) cellIdentity).getNetworkId();
            cellInfo.mCellId2 = ((CellIdentityCdma) cellIdentity).getSystemId();
            cellInfo.mCellId3 = ((CellIdentityCdma) cellIdentity).getBasestationId();
        } else if (type == 3) {
            cellInfo.mMccMnc = ((CellIdentityLte) cellIdentity).getMccString() + ((CellIdentityLte) cellIdentity).getMncString();
            cellInfo.mCellId1 = ((CellIdentityLte) cellIdentity).getCi();
            cellInfo.mCellId2 = ((CellIdentityLte) cellIdentity).getTac();
            cellInfo.mBw = ((CellIdentityLte) cellIdentity).getBandwidth();
        } else if (type == 4) {
            cellInfo.mMccMnc = ((CellIdentityWcdma) cellIdentity).getMccString() + ((CellIdentityWcdma) cellIdentity).getMncString();
            cellInfo.mCellId1 = ((CellIdentityWcdma) cellIdentity).getCid();
            cellInfo.mCellId2 = ((CellIdentityWcdma) cellIdentity).getLac();
            cellInfo.mCellId3 = ((CellIdentityWcdma) cellIdentity).getPsc();
        } else if (type == 5) {
            cellInfo.mMccMnc = ((CellIdentityTdscdma) cellIdentity).getMccString() + ((CellIdentityTdscdma) cellIdentity).getMncString();
            cellInfo.mCellId1 = ((CellIdentityTdscdma) cellIdentity).getCid();
            cellInfo.mCellId2 = ((CellIdentityTdscdma) cellIdentity).getLac();
        }
        Phone phone = this.mPhone;
        if (phone != null && OppoCallStateMonitor.getInstance(phone.getContext()).isCurrPhoneInCall(this.mPhone.getPhoneId())) {
            Rlog.d(TAG, "Incall ....");
            if (this.mCallCellInfo == null) {
                Rlog.d(TAG, "mCallCellInfo == null.");
                this.mCallCellInfo = new CellInfo();
            }
            this.mCallCellInfo.copyFrom(this.mCellInfo);
        }
        if (!cellInfo.equals(this.mCellInfo)) {
            if (this.mCellInfo.mScored > 10) {
                updateCellInfo();
            }
            this.mCellInfo = cellInfo;
        }
        Rlog.d(TAG, "mCellInfo:" + this.mCellInfo);
    }

    public void dispose() {
        unregisterPrefNetworkModeObserver();
    }

    private class CellInfo implements Comparable<CellInfo> {
        public int mArfcnl;
        public int mBw;
        public int mCellId1;
        public int mCellId2;
        public int mCellId3;
        public String mMccMnc;
        public long mScored;
        public int mType;

        private CellInfo() {
            this.mMccMnc = "";
        }

        public boolean isValid() {
            if (this.mArfcnl == 0 || this.mCellId1 == 0) {
                return false;
            }
            return true;
        }

        public void copyFrom(CellInfo info) {
            this.mMccMnc = new String(info.mMccMnc);
            this.mCellId1 = info.mCellId1;
            this.mCellId2 = info.mCellId2;
            this.mCellId3 = info.mCellId3;
            this.mType = info.mType;
            this.mArfcnl = info.mArfcnl;
            this.mBw = info.mBw;
            this.mScored = info.mScored;
        }

        public String toString() {
            return "" + this.mMccMnc + "," + this.mCellId1 + "," + this.mCellId2 + "," + this.mCellId3 + "," + this.mType + "," + this.mArfcnl + "," + this.mBw + "," + this.mScored;
        }

        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            try {
                CellInfo cellInfo = (CellInfo) o;
                if (this.mMccMnc.equals(cellInfo.mMccMnc) && this.mCellId1 == cellInfo.mCellId1 && this.mCellId2 == cellInfo.mCellId2 && this.mCellId3 == cellInfo.mCellId3 && this.mType == cellInfo.mType && this.mArfcnl == cellInfo.mArfcnl) {
                    return true;
                }
                return false;
            } catch (ClassCastException e) {
                return false;
            }
        }

        public int compareTo(CellInfo o) {
            if (this.mScored > o.mScored) {
                return -1;
            }
            return 1;
        }
    }

    private boolean inService() {
        ServiceState serviceState = this.mSS;
        if (serviceState == null) {
            return false;
        }
        if (serviceState.getDataRegState() == 0 || this.mSS.getVoiceRegState() == 0) {
            return true;
        }
        return false;
    }

    public String getServiceStateDesc() {
        if (!inService()) {
            return "<>";
        }
        return "<" + this.mSS.getOperatorNumeric() + "," + this.mSS.getDataNetworkType() + "," + this.mSS.getVoiceNetworkType() + "," + this.mSS.getDataRegState() + "," + this.mSS.getVoiceRegState() + ">";
    }

    public String getSignalStrengthDesc() {
        int rat;
        if (!inService() || this.mOrginalSignalStrength == null) {
            return "<>";
        }
        StringBuilder sb = new StringBuilder();
        if (this.mSS.getState() == 0) {
            if (this.mSS.getDataRegState() == 0) {
                rat = this.mSS.getDataNetworkType();
            } else {
                rat = this.mSS.getVoiceNetworkType();
            }
            if (rat == 19 || rat == 13) {
                sb.append("<" + this.mOrginalSignalStrength.getLteRsrp() + "," + (this.mOrginalSignalStrength.getLteRssnr() / 10) + ">");
            } else if (rat == 3 || rat == 8 || rat == 9 || rat == 10 || rat == 15 || rat == 17) {
                sb.append("<" + this.mOrginalSignalStrength.getDbm() + ",0>");
            } else if (rat == 1 || rat == 2 || rat == 16) {
                sb.append("<" + this.mOrginalSignalStrength.getDbm() + "," + this.mOrginalSignalStrength.getGsmBitErrorRate() + ">");
            } else if (rat == 5 || rat == 6 || rat == 12 || rat == 14) {
                sb.append("<" + this.mOrginalSignalStrength.getEvdoDbm() + "," + this.mOrginalSignalStrength.getEvdoSnr() + ">");
            } else if (rat == 4 || rat == 7) {
                sb.append("<" + this.mOrginalSignalStrength.getCdmaDbm() + "," + (this.mOrginalSignalStrength.getCdmaEcio() / 10) + ">");
            }
        }
        return sb.toString();
    }

    public String getCellInfoDesc() {
        if (this.mCellInfo == null || !inService()) {
            return "<>";
        }
        return "<" + this.mCellInfo.mType + "," + this.mCellInfo.mMccMnc + "," + this.mCellInfo.mCellId1 + "," + this.mCellInfo.mCellId2 + "," + this.mCellInfo.mCellId3 + "," + this.mCellInfo.mArfcnl + ">";
    }

    public String getCallCellInfoDesc() {
        if (this.mCallCellInfo == null) {
            return "()";
        }
        return "(" + this.mCallCellInfo.mType + "," + this.mCallCellInfo.mMccMnc + "," + this.mCallCellInfo.mCellId1 + "," + this.mCallCellInfo.mCellId2 + "," + this.mCallCellInfo.mCellId3 + "," + this.mCallCellInfo.mArfcnl + ")";
    }

    public int getPreferredNetworkType() {
        Context context = this.mPhone.getContext();
        if (SubscriptionManager.from(context).isActiveSubId(this.mPhone.getSubId())) {
            ContentResolver contentResolver = context.getContentResolver();
            return Settings.Global.getInt(contentResolver, "preferred_network_mode" + this.mPhone.getSubId(), -1);
        }
        try {
            return TelephonyManager.getIntAtIndex(context.getContentResolver(), "preferred_network_mode", this.mPhone.getPhoneId());
        } catch (Settings.SettingNotFoundException e) {
            Rlog.e(TAG, "getPreferredNetworkType error:" + e);
            return -1;
        }
    }

    private static boolean isValidLteBandwidthKhz(int bandwidth) {
        if (bandwidth == 1400 || bandwidth == 3000 || bandwidth == 5000 || bandwidth == 10000 || bandwidth == 15000 || bandwidth == 20000) {
            return true;
        }
        return false;
    }

    private static int ConvertBandwidthFromEnToKhz(byte bw) {
        if (bw == 0) {
            return 1400;
        }
        if (bw == 1) {
            return OppoServiceStateTracker.DELAYTIME_3S;
        }
        if (bw == 2) {
            return RegionLockConstant.EVENT_NETWORK_LOCK_STATUS;
        }
        if (bw == 3) {
            return OppoServiceStateTracker.DELAYTIME_10S;
        }
        if (bw == 4) {
            return 15000;
        }
        if (bw != 5) {
            return 0;
        }
        return 20000;
    }

    private void smart5gInfoLteQuality(int phoneId, boolean ltePoor) {
        Intent intent = new Intent(ACTION_INFORM_LTE_QUALITY);
        intent.putExtra("PhoneId", phoneId);
        intent.putExtra("ltePoor", ltePoor);
        this.mPhone.getContext().sendBroadcast(intent);
    }

    public void smart5gUpdateLteQuality(SignalStrength signal) {
        CellInfo cellInfo;
        boolean mIsLtePoorBackup = this.mIsLtePoor;
        this.mIsLtePoor = true;
        if (this.mSS.getDataRegState() == 0 && this.mSS.getRilDataRadioTechnology() == 14) {
            int curLteRsrp = signal.getLteRsrp();
            int curLteRsrq = signal.getLteRsrq();
            Rlog.d(TAG, "UpdateLteQuality rsrp= " + curLteRsrp + "rsrq= " + curLteRsrq);
            if (curLteRsrp > -44 || curLteRsrp < -140) {
                this.mIsLtePoor = true;
            } else if (curLteRsrp <= this.mSmart5gLtePoorRsrpThres || curLteRsrq <= this.mSmart5gLtePoorRsrqThres) {
                this.mIsLtePoor = true;
            } else {
                this.mIsLtePoor = false;
            }
            if (!this.mIsLtePoor && (cellInfo = this.mCellInfo) != null && isValidLteBandwidthKhz(cellInfo.mBw) && this.mCellInfo.mBw <= this.mSmart5gLtePoorBwThres) {
                this.mIsLtePoor = true;
            }
        } else {
            this.mIsLtePoor = false;
        }
        if (mIsLtePoorBackup != this.mIsLtePoor) {
            smart5gInfoLteQuality(this.mPhone.getPhoneId(), this.mIsLtePoor);
        }
    }

    private void infoRatChanged(int phoneId, int oldRat, int newRat) {
        Intent intent = new Intent(ACTION_INFORM_RAT_CHANGED);
        intent.putExtra("PhoneId", phoneId);
        intent.putExtra("OldRat", oldRat);
        intent.putExtra("NewRat", newRat);
        this.mPhone.getContext().sendBroadcast(intent);
    }

    public void checkRatTechChanged(ServiceState oldSS, ServiceState newSS) {
        int oldRAT;
        if (oldSS != null && newSS != null) {
            NetworkRegistrationInfo oldNrs = oldSS.getNetworkRegistrationInfo(2, 1);
            NetworkRegistrationInfo newNrs = newSS.getNetworkRegistrationInfo(2, 1);
            int newRAT = 0;
            if (oldNrs != null) {
                oldRAT = oldNrs.getAccessNetworkTechnology();
            } else {
                oldRAT = 0;
            }
            if (newNrs != null) {
                newRAT = newNrs.getAccessNetworkTechnology();
            }
            Rlog.d(TAG, "checkRatTechChanged oldRAT= " + oldRAT + "newRAT= " + newRAT);
            if (newRAT != oldRAT) {
                infoRatChanged(this.mPhone.getPhoneId(), oldRAT, newRAT);
            }
        }
    }

    public void registerForNetworkTypeChanged(Handler h, int what, Object obj) {
        this.mNetworkTypeChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForNetworkTypeChanged(Handler h) {
        this.mNetworkTypeChangedRegistrants.remove(h);
    }

    public SignalStrength getSignalStrength() {
        return this.mSignalStrength;
    }
}
