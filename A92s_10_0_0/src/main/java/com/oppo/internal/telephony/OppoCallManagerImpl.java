package com.oppo.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.format.Time;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.IOppoCallManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.imsphone.ImsPhoneConnection;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.android.internal.telephony.util.ReflectionHelper;
import com.oppo.internal.telephony.utils.OppoManagerHelper;
import com.oppo.internal.telephony.utils.OppoPhoneUtil;
import com.oppo.internal.telephony.utils.OppoPolicyController;
import java.util.HashMap;

public class OppoCallManagerImpl implements IOppoCallManager {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "OppoCallManagerImpl";
    public static final int TYPE_CALLIN = 0;
    public static final int TYPE_CALLOUT = 1;
    public static final int TYPE_PS = 4;
    public static final int TYPE_SMSIN = 2;
    public static final int TYPE_SMSOUT = 3;
    private static OppoCallManagerImpl sInstance = null;
    String calleventid = "050101";
    private int mCallFailNumberMO = 0;
    private int mCallFailNumberMT = 0;
    private boolean mCallRegisterEveryday = false;
    private int mCallSuccessNumberBeforeFail = 0;
    private int mCallSuccessNumberMO = 0;
    private int mCallSuccessNumberMT = 0;
    private int mCmccCsCallFailNumber = 0;
    private int mCmccCsCallSuccessNumber = 0;
    private int mCmccImsCallFailNumber = 0;
    private int mCmccImsCallSuccessNumber = 0;
    private long mCsCallDuration = 0;
    private int mCtCsCallFailNumber = 0;
    private int mCtCsCallSuccessNumber = 0;
    private int mCtImsCallFailNumber = 0;
    private int mCtImsCallSuccessNumber = 0;
    private int mCuGsmCsCallFailNumber = 0;
    private int mCuGsmCsCallSuccessNumber = 0;
    private int mCuImsCallFailNumber = 0;
    private int mCuImsCallSuccessNumber = 0;
    private int mCuWcdmaCsCallFailNumber = 0;
    private int mCuWcdmaCsCallSuccessNumber = 0;
    private long mImsCallDuration = 0;
    private int mImsCallFailNumberMO = 0;
    private int mImsCallFailNumberMT = 0;
    private int mImsCallSuccessNumberMO = 0;
    private int mImsCallSuccessNumberMT = 0;
    private int mMnc = OppoRIL.MAX_MODEM_CRASH_CAUSE_LEN;
    private int mNetworkType = OppoRIL.MAX_MODEM_CRASH_CAUSE_LEN;
    private Object mOppoUsageManager = null;

    private void initValueByReflection() {
        this.mOppoUsageManager = ReflectionHelper.callMethod((Object) null, "android.os.OppoUsageManager", "getOppoUsageManager", new Class[0], new Object[0]);
    }

    public static OppoCallManagerImpl getInstance() {
        OppoCallManagerImpl oppoCallManagerImpl;
        OppoCallManagerImpl oppoCallManagerImpl2 = sInstance;
        if (oppoCallManagerImpl2 != null) {
            return oppoCallManagerImpl2;
        }
        synchronized (OppoCallManagerImpl.class) {
            if (sInstance == null) {
                sInstance = new OppoCallManagerImpl();
            }
            oppoCallManagerImpl = sInstance;
        }
        return oppoCallManagerImpl;
    }

    public boolean isConferenceHostConnection(boolean isConf, String connAddr, Phone phone) {
        return OppoPhoneUtil.isConferenceHostConnection(isConf, connAddr, phone);
    }

    public boolean isCurrPhoneInCall(Phone phone) {
        return OppoCallStateMonitor.getInstance(phone.getContext()).isCurrPhoneInCall(phone.getPhoneId());
    }

    public boolean isOtherPhoneInCall(Phone phone) {
        return OppoCallStateMonitor.getInstance(phone.getContext()).isOtherPhoneInCall(phone.getPhoneId());
    }

    public void handleSetCFFDone(Phone phone, int serviceClass, boolean cffEnable, String dialingNum) {
    }

    public void checkVoocState(String value) {
        OppoPhoneUtil.checkVoocState(value);
    }

    public boolean isCtcCardCtaTest(Context mContext, Phone phone) {
        Rlog.d(LOG_TAG, "isCtcCardCtaTest");
        if (!mContext.getPackageManager().hasSystemFeature("oppo.cta.support") || !OppoPhoneUtil.isCtCard(phone)) {
            return false;
        }
        return DBG;
    }

    public String getNetworkOperator(Phone phone) {
        String operator = TelephonyManager.from(phone.getContext()).getNetworkOperatorForPhone(phone.getPhoneId());
        if (operator.equals("46003") || operator.equals("46011")) {
            return "46003";
        }
        if (operator.equals("46001") || operator.equals("46009")) {
            return "46001";
        }
        if (operator.equals("46000") || operator.equals("46002") || operator.equals("46004") || operator.equals("46007") || operator.equals("46008")) {
            return "46000";
        }
        return operator;
    }

    public String getSignalQuality(Connection c) {
        Phone phone = null;
        if (!(c.getCall() == null || c.getCall().getPhone() == null)) {
            phone = c.getCall().getPhone().getDefaultPhone();
        }
        if (phone == null) {
            return "";
        }
        SignalStrength signal = phone.getSignalStrength();
        int networkType = phone.getServiceStateTracker().mSS.getVoiceNetworkType();
        this.mNetworkType = networkType;
        StringBuilder log = new StringBuilder();
        if (networkType == 13 || networkType == 19 || networkType == 20) {
            int lteRsrq = signal.getLteRsrq();
            int lteRssnr = signal.getLteRssnr();
            int lteDbm = signal.getLteDbm();
            log.append("LTE,");
            log.append(" rsrq:");
            log.append(lteRsrq);
            log.append(" rssnr:");
            log.append(lteRssnr);
            log.append(" dbm:");
            log.append(lteDbm);
        } else if (networkType == 17) {
            int tdScdmaDbm = signal.getTdScdmaDbm();
            log.append("TDSCDMA,");
            log.append(" rscp:");
            log.append(tdScdmaDbm);
        } else if (networkType == 3 || networkType == 8 || networkType == 9 || networkType == 10 || networkType == 15) {
            int wcdmaRscp = signal.getWcdmaRscp();
            log.append("WCDMA,");
            log.append(" rscp:");
            log.append(wcdmaRscp);
        } else if (networkType == 4 || networkType == 7) {
            int cdmaEcio = signal.getCdmaEcio();
            int cdmaDbm = signal.getCdmaDbm();
            log.append("CDMA,");
            log.append(" ecio:");
            log.append(cdmaEcio);
            log.append(" dbm:");
            log.append(cdmaDbm);
        } else if (networkType == 5 || networkType == 6 || networkType == 12) {
            int evdoEcio = signal.getEvdoEcio();
            int EvdoDbm = signal.getEvdoDbm();
            log.append("EVDO,");
            log.append(" ecio:");
            log.append(evdoEcio);
            log.append(" dbm:");
            log.append(EvdoDbm);
        } else if (networkType == 1 || networkType == 2 || networkType == 16) {
            int gsmBitErrRat = signal.getGsmBitErrorRate();
            int gsmDbm = signal.getGsmDbm();
            log.append("GSM,");
            log.append(" bitErrorRate:");
            log.append(gsmBitErrRat);
            log.append(" dbm:");
            log.append(gsmDbm);
        } else {
            Rlog.d(LOG_TAG, "RAT may be Wrong:" + networkType);
            return "";
        }
        return log.toString();
    }

    public String getLoc(Connection c) {
        Phone phone = null;
        if (!(c.getCall() == null || c.getCall().getPhone() == null)) {
            phone = c.getCall().getPhone().getDefaultPhone();
        }
        if (phone == null) {
            return "";
        }
        String mccMnc = getNetworkOperator(phone);
        int mcc = 0;
        int mnc = 0;
        int slotId = 0;
        if (mccMnc != null) {
            try {
                if (mccMnc.length() >= 3) {
                    mcc = Integer.parseInt(mccMnc.substring(0, 3));
                    mnc = Integer.parseInt(mccMnc.substring(3));
                    slotId = phone.getPhoneId();
                    this.mMnc = mnc;
                }
            } catch (Exception e) {
                Rlog.d(LOG_TAG, "couldn't parse mcc/mnc: " + mccMnc);
            }
        }
        String loc = "MCC:" + mcc + ", MNC:" + mnc + ", slotId:" + slotId;
        CellLocation cell = phone.getCellLocation();
        if (cell instanceof GsmCellLocation) {
            loc = loc + ", LAC:" + ((GsmCellLocation) cell).getLac() + ", CID:" + ((GsmCellLocation) cell).getCid();
        } else if (cell instanceof CdmaCellLocation) {
            loc = loc + ", SID:" + ((CdmaCellLocation) cell).getSystemId() + ", NID:" + ((CdmaCellLocation) cell).getNetworkId() + ", BID:" + ((CdmaCellLocation) cell).getBaseStationId();
        }
        SignalStrength signal = phone.getSignalStrength();
        String ssq = getSignalQuality(c);
        if (signal == null) {
            return loc;
        }
        return loc + ", signalstrength:" + signal.getDbm() + ", signallevel:" + signal.getLevel() + ", signalquality:\"" + ssq + "\"";
    }

    /* JADX INFO: Multiple debug info for r12v3 boolean: [D('imscall' boolean), D('log_array' java.lang.String[])] */
    public void writeCallRecord(Connection c, Context context) {
        long callDuration;
        boolean isIncoming;
        String address;
        String createDate;
        long callDuration2;
        boolean called_status;
        String address2 = c.getAddress();
        c.getCreateTime();
        long callDuration3 = c.getDurationMillis() / 1000;
        boolean isIncoming2 = c.isIncoming();
        String createDate2 = getCurrentDateStr();
        HashMap<String, String> calldrop = new HashMap<>();
        if (!this.mCallRegisterEveryday) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.DATE_CHANGED");
            context.registerReceiver(new DateChangeReceiverForCall(), filter);
            this.mCallRegisterEveryday = DBG;
        }
        if (callDuration3 <= 0 || callDuration3 >= 60) {
            callDuration = callDuration3 / 60;
        } else {
            callDuration = 1;
        }
        Rlog.d(LOG_TAG, "writeCallRecord address = " + address2 + ", createDate = " + createDate2 + ", callDuration = " + callDuration + ", isIncoming:" + isIncoming2);
        try {
            if (this.mOppoUsageManager == null) {
                address = address2;
                isIncoming = isIncoming2;
                createDate = createDate2;
            } else if (isIncoming2) {
                try {
                    address = address2;
                    createDate = createDate2;
                    try {
                        isIncoming = isIncoming2;
                        try {
                            ReflectionHelper.callMethod(this.mOppoUsageManager, "android.os.OppoUsageManager", "accumulateInComingCallDuration", new Class[]{Integer.TYPE}, new Object[]{Long.valueOf(callDuration)});
                        } catch (Exception e) {
                            return;
                        }
                    } catch (Exception e2) {
                        return;
                    }
                } catch (Exception e3) {
                    return;
                }
            } else {
                address = address2;
                isIncoming = isIncoming2;
                createDate = createDate2;
                ReflectionHelper.callMethod(this.mOppoUsageManager, "android.os.OppoUsageManager", "accumulateDialOutDuration", new Class[]{Integer.TYPE}, new Object[]{Long.valueOf(callDuration)});
            }
            try {
                String log_string = OemTelephonyUtils.getOemRes(context, "zz_oppo_critical_log_10", "");
                if (log_string.equals("")) {
                    Rlog.e(LOG_TAG, "Can not get resource of identifier zz_oppo_critical_log");
                    return;
                }
                String[] log_array = log_string.split(",");
                int log_type = Integer.valueOf(log_array[0]).intValue();
                String log_desc = log_array[1];
                String loc = getLoc(c);
                int cause = c.getDisconnectCause();
                boolean imscall = c instanceof ImsPhoneConnection;
                if ((cause == 36 && !imscall) || cause == 5 || cause == 7 || cause == 18 || cause == 27 || cause == 28 || ((cause == 12 && imscall) || (cause == 9 && imscall))) {
                    callDuration2 = callDuration;
                    try {
                        if (c.getCall().mState == Call.State.DIALING) {
                            try {
                                OppoManagerHelper.writeLogToPartition(log_type, loc + ", mo drop cause:" + cause + ", imscall:" + imscall + ", mCallSuccessNumberBeforeFail:" + this.mCallSuccessNumberBeforeFail, OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_MO_DROP, log_desc);
                                calldrop.put(OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_MO_DROP, loc + ", mo drop cause:" + cause + ", imscall:" + imscall + ", mCallSuccessNumberBeforeFail:" + this.mCallSuccessNumberBeforeFail);
                                OppoManagerHelper.onStamp(this.calleventid, calldrop);
                                this.mCallFailNumberMO = this.mCallFailNumberMO + 1;
                                if (imscall) {
                                    this.mImsCallFailNumberMO++;
                                }
                            } catch (Exception e4) {
                                return;
                            }
                        } else if (isIncoming) {
                            String log_string1 = OemTelephonyUtils.getOemRes(context, "zz_oppo_critical_log_24", "");
                            if (log_string1.equals("")) {
                                Rlog.e(LOG_TAG, "Can not get resource of identifier zz_oppo_critical_log");
                                return;
                            }
                            String[] log_array1 = log_string1.split(",");
                            OppoManagerHelper.writeLogToPartition(Integer.valueOf(log_array1[0]).intValue(), loc + ",MT call drop cause:" + cause + ", imscall:" + imscall + ", mCallSuccessNumberBeforeFail:" + this.mCallSuccessNumberBeforeFail, OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_CALL_DROP, log_array1[1]);
                            calldrop.put(OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_CALL_DROP, loc + ",MT call drop cause:" + cause + ", imscall:" + imscall + ", mCallSuccessNumberBeforeFail:" + this.mCallSuccessNumberBeforeFail);
                            OppoManagerHelper.onStamp(this.calleventid, calldrop);
                            this.mCallFailNumberMT = this.mCallFailNumberMT + 1;
                            if (imscall) {
                                this.mImsCallFailNumberMT++;
                            }
                        } else {
                            String log_string2 = OemTelephonyUtils.getOemRes(context, "zz_oppo_critical_log_23", "");
                            if (log_string2.equals("")) {
                                Rlog.e(LOG_TAG, "Can not get resource of identifier zz_oppo_critical_log");
                                return;
                            }
                            String[] log_array2 = log_string2.split(",");
                            OppoManagerHelper.writeLogToPartition(Integer.valueOf(log_array2[0]).intValue(), loc + ",MO call drop cause:" + cause + ", imscall:" + imscall + ", mCallSuccessNumberBeforeFail:" + this.mCallSuccessNumberBeforeFail, OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_MO_CALL_DROP, log_array2[1]);
                            calldrop.put(OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_MO_CALL_DROP, loc + ",MO call drop cause:" + cause + ", imscall:" + imscall + ", mCallSuccessNumberBeforeFail:" + this.mCallSuccessNumberBeforeFail);
                            OppoManagerHelper.onStamp(this.calleventid, calldrop);
                            this.mCallFailNumberMO = this.mCallFailNumberMO + 1;
                            if (imscall) {
                                this.mImsCallFailNumberMO++;
                            }
                        }
                        this.mCallSuccessNumberBeforeFail = 0;
                        int i = this.mMnc;
                        if (i != 0) {
                            if (i != 1) {
                                if (i == 3) {
                                    if (imscall) {
                                        this.mCtImsCallFailNumber++;
                                    } else {
                                        this.mCtCsCallFailNumber++;
                                    }
                                }
                            } else if (imscall) {
                                this.mCuImsCallFailNumber++;
                            } else if (this.mNetworkType == 1 || this.mNetworkType == 2 || this.mNetworkType == 16) {
                                this.mCuGsmCsCallFailNumber++;
                            } else {
                                this.mCuWcdmaCsCallFailNumber++;
                            }
                        } else if (imscall) {
                            this.mCmccImsCallFailNumber++;
                        } else {
                            this.mCmccCsCallFailNumber++;
                        }
                        called_status = false;
                    } catch (Exception e5) {
                        return;
                    }
                } else {
                    callDuration2 = callDuration;
                    called_status = true;
                }
                if (called_status && (cause == 2 || cause == 4 || cause == 3 || cause == 16 || cause == 65)) {
                    this.mCallSuccessNumberBeforeFail++;
                    if (isIncoming) {
                        this.mCallSuccessNumberMT++;
                        if (imscall) {
                            this.mImsCallSuccessNumberMT++;
                        }
                    } else {
                        this.mCallSuccessNumberMO++;
                        if (imscall) {
                            this.mImsCallSuccessNumberMO++;
                        }
                    }
                    int i2 = this.mMnc;
                    if (i2 != 0) {
                        if (i2 != 1) {
                            if (i2 == 3) {
                                if (imscall) {
                                    this.mCtImsCallSuccessNumber++;
                                } else {
                                    this.mCtCsCallSuccessNumber++;
                                }
                            }
                        } else if (imscall) {
                            this.mCuImsCallSuccessNumber++;
                        } else if (this.mNetworkType == 1 || this.mNetworkType == 2 || this.mNetworkType == 16) {
                            this.mCuGsmCsCallSuccessNumber++;
                        } else {
                            this.mCuWcdmaCsCallSuccessNumber++;
                        }
                    } else if (imscall) {
                        this.mCmccImsCallSuccessNumber++;
                    } else {
                        this.mCmccCsCallSuccessNumber++;
                    }
                }
                if (imscall) {
                    this.mImsCallDuration += callDuration2;
                } else {
                    this.mCsCallDuration += callDuration2;
                }
                Rlog.d(LOG_TAG, "called_status = " + called_status + ", mCallSuccessNumberBeforeFail = " + this.mCallSuccessNumberBeforeFail + ", mCallSuccessNumberMT = " + this.mCallSuccessNumberMT + ", mCallSuccessNumberMO =" + this.mCallSuccessNumberMO + ",mImsCallSuccessNumberMO = " + this.mImsCallSuccessNumberMO + ",mImsCallFailNumberMO = " + this.mImsCallFailNumberMO + ",mImsCallFailNumberMT = " + this.mImsCallFailNumberMT + ", mCallFailNumberMO = " + this.mCallFailNumberMO + ", mCallFailNumberMT =" + this.mCallFailNumberMT + ", mImsCallDuration:" + this.mImsCallDuration + ", mCsCallDuration:" + this.mCsCallDuration + ",DisconnectCause = " + cause);
                Rlog.d(LOG_TAG, "mCmccCsCallSuccessNumber = " + this.mCmccCsCallSuccessNumber + ", mCtCsCallSuccessNumber = " + this.mCtCsCallSuccessNumber + ", mCuGsmCsCallSuccessNumber = " + this.mCuGsmCsCallSuccessNumber + ", mCuWcdmaCsCallSuccessNumber = " + this.mCuWcdmaCsCallSuccessNumber + ", mCmccImsCallSuccessNumber =" + this.mCmccImsCallSuccessNumber + ",mCtImsCallSuccessNumber = " + this.mCtImsCallSuccessNumber + ",mCuImsCallSuccessNumber = " + this.mCuImsCallSuccessNumber + ",mCmccCsCallFailNumber = " + this.mCmccCsCallFailNumber + ", mCtCsCallFailNumber = " + this.mCtCsCallFailNumber + ", mCuGsmCsCallFailNumber =" + this.mCuGsmCsCallFailNumber + ", mCuWcdmaCsCallFailNumber =" + this.mCuWcdmaCsCallFailNumber + ", mCmccImsCallFailNumber:" + this.mCmccImsCallFailNumber + ", mCtImsCallFailNumber:" + this.mCtImsCallFailNumber + ",mCuImsCallFailNumber = " + this.mCuImsCallFailNumber);
                try {
                    String addressInfo = dealWithAddress(address, isIncoming);
                    if (addressInfo == null) {
                        return;
                    }
                    if (addressInfo.length() > 0) {
                        if (this.mOppoUsageManager != null) {
                            ((Boolean) ReflectionHelper.callMethod(this.mOppoUsageManager, "android.os.OppoUsageManager", "writePhoneCallHistoryRecord", new Class[]{String.class, String.class}, new Object[]{addressInfo, createDate})).booleanValue();
                        }
                    }
                } catch (Exception e6) {
                }
            } catch (Exception e7) {
            }
        } catch (Exception e8) {
        }
    }

    public String dealWithAddress(String addr, boolean isIncoming) {
        if (addr == null || addr.length() <= 0) {
            return null;
        }
        StringBuilder strBuilder = new StringBuilder();
        String prefix = isIncoming ? "in :" : "out:";
        int length = addr.length();
        if (length <= 6) {
            strBuilder.append(prefix);
            strBuilder.append(addr);
        } else {
            int remainCharNum = length - 4;
            int halfOfRemain = remainCharNum / 2;
            int lastPartNum = remainCharNum - halfOfRemain;
            strBuilder.append(prefix);
            if (halfOfRemain > 0) {
                strBuilder.append(addr.substring(0, halfOfRemain));
            }
            for (int i = 0; i < 4; i++) {
                strBuilder.append('*');
            }
            if (lastPartNum > 0 && lastPartNum < length) {
                strBuilder.append(addr.substring(halfOfRemain + 4, length));
            }
        }
        return strBuilder.toString();
    }

    public String getCurrentDateStr() {
        Time timeObj = new Time();
        timeObj.set(System.currentTimeMillis());
        return timeObj.format("%Y-%m-%d %H:%M:%S");
    }

    public void updateCallRecord(Context context) {
        String log_string = OemTelephonyUtils.getOemRes(context, "zz_oppo_critical_log_25", "");
        if (log_string.equals("")) {
            Rlog.e(LOG_TAG, "return for get log_string fail.");
            return;
        }
        String[] log_array = log_string.split(",");
        int log_type = Integer.valueOf(log_array[0]).intValue();
        String log_desc = log_array[1];
        HashMap<String, String> callstate = new HashMap<>();
        OppoManagerHelper.writeLogToPartition(log_type, "mCallSuccessNumberMO:" + this.mCallSuccessNumberMO + ", mCallSuccessNumberMT:" + this.mCallSuccessNumberMT + ", mCallFailNumberMO:" + this.mCallFailNumberMO + ", mCallFailNumberMT:" + this.mCallFailNumberMT + ", mImsCallSuccessNumberMO:" + this.mImsCallSuccessNumberMO + ", mImsCallSuccessNumberMT:" + this.mImsCallSuccessNumberMT + ", mImsCallFailNumberMO:" + this.mImsCallFailNumberMO + ", mImsCallFailNumberMT:" + this.mImsCallFailNumberMT + ", mImsCallDuration:" + this.mImsCallDuration + ", mCsCallDuration:" + this.mCsCallDuration + ", mCmccCsCallSuccessNumber:" + this.mCmccCsCallSuccessNumber + ", mCtCsCallSuccessNumber:" + this.mCtCsCallSuccessNumber + ", mCuGsmCsCallSuccessNumber:" + this.mCuGsmCsCallSuccessNumber + ", mCuWcdmaCsCallSuccessNumber:" + this.mCuWcdmaCsCallSuccessNumber + ", mCmccImsCallSuccessNumber:" + this.mCmccImsCallSuccessNumber + ", mCtImsCallSuccessNumber:" + this.mCtImsCallSuccessNumber + ", mCuImsCallSuccessNumber:" + this.mCuImsCallSuccessNumber + ", mCmccCsCallFailNumber:" + this.mCmccCsCallFailNumber + ", mCtCsCallFailNumber:" + this.mCtCsCallFailNumber + ", mCuGsmCsCallFailNumber:" + this.mCuGsmCsCallFailNumber + ", mCuWcdmaCsCallFailNumber:" + this.mCuWcdmaCsCallFailNumber + ", mCmccImsCallFailNumber:" + this.mCmccImsCallFailNumber + ", mCtImsCallFailNumber:" + this.mCtImsCallFailNumber + ", mCuImsCallFailNumber:" + this.mCuImsCallFailNumber, OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_MO_MT_DROP_RATE, log_desc);
        callstate.put(OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_MO_MT_DROP_RATE, "mCallSuccessNumberMO:" + this.mCallSuccessNumberMO + ", mCallSuccessNumberMT:" + this.mCallSuccessNumberMT + ", mCallFailNumberMO:" + this.mCallFailNumberMO + ", mCallFailNumberMT:" + this.mCallFailNumberMT + ", mImsCallSuccessNumberMO:" + this.mImsCallSuccessNumberMO + ", mImsCallSuccessNumberMT:" + this.mImsCallSuccessNumberMT + ", mImsCallFailNumberMO:" + this.mImsCallFailNumberMO + ", mImsCallFailNumberMT:" + this.mImsCallFailNumberMT + ", mImsCallDuration:" + this.mImsCallDuration + ", mCsCallDuration:" + this.mCsCallDuration + ", mCmccCsCallSuccessNumber:" + this.mCmccCsCallSuccessNumber + ", mCtCsCallSuccessNumber:" + this.mCtCsCallSuccessNumber + ", mCuGsmCsCallSuccessNumber:" + this.mCuGsmCsCallSuccessNumber + ", mCuWcdmaCsCallSuccessNumber:" + this.mCuWcdmaCsCallSuccessNumber + ", mCmccImsCallSuccessNumber:" + this.mCmccImsCallSuccessNumber + ", mCtImsCallSuccessNumber:" + this.mCtImsCallSuccessNumber + ", mCuImsCallSuccessNumber:" + this.mCuImsCallSuccessNumber + ", mCmccCsCallFailNumber:" + this.mCmccCsCallFailNumber + ", mCtCsCallFailNumber:" + this.mCtCsCallFailNumber + ", mCuGsmCsCallFailNumber:" + this.mCuGsmCsCallFailNumber + ", mCuWcdmaCsCallFailNumber:" + this.mCuWcdmaCsCallFailNumber + ", mCmccImsCallFailNumber:" + this.mCmccImsCallFailNumber + ", mCtImsCallFailNumber:" + this.mCtImsCallFailNumber + ", mCuImsCallFailNumber:" + this.mCuImsCallFailNumber);
        StringBuilder sb = new StringBuilder();
        sb.append("updateCallRecord: ");
        sb.append(log_type);
        Rlog.d(LOG_TAG, sb.toString());
        OppoManagerHelper.onStamp(this.calleventid, callstate);
        this.mCallSuccessNumberMO = 0;
        this.mCallSuccessNumberMT = 0;
        this.mCallFailNumberMO = 0;
        this.mCallFailNumberMT = 0;
        this.mImsCallSuccessNumberMO = 0;
        this.mImsCallSuccessNumberMT = 0;
        this.mImsCallFailNumberMO = 0;
        this.mImsCallFailNumberMT = 0;
        this.mImsCallDuration = 0;
        this.mCsCallDuration = 0;
        this.mCmccCsCallSuccessNumber = 0;
        this.mCtCsCallSuccessNumber = 0;
        this.mCuGsmCsCallSuccessNumber = 0;
        this.mCuWcdmaCsCallSuccessNumber = 0;
        this.mCmccImsCallSuccessNumber = 0;
        this.mCtImsCallSuccessNumber = 0;
        this.mCuImsCallSuccessNumber = 0;
        this.mCmccCsCallFailNumber = 0;
        this.mCtCsCallFailNumber = 0;
        this.mCuGsmCsCallFailNumber = 0;
        this.mCuWcdmaCsCallFailNumber = 0;
        this.mCmccImsCallFailNumber = 0;
        this.mCtImsCallFailNumber = 0;
        this.mCuImsCallFailNumber = 0;
    }

    class DateChangeReceiverForCall extends BroadcastReceiver {
        public DateChangeReceiverForCall() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.DATE_CHANGED".equals(intent.getAction())) {
                OppoCallManagerImpl.this.updateCallRecord(context);
            }
        }
    }

    public boolean isUssiEnabled(Phone mPhone) {
        CarrierConfigManager configLoader = (CarrierConfigManager) mPhone.getContext().getSystemService("carrier_config");
        if (configLoader == null) {
            return false;
        }
        PersistableBundle b = configLoader.getConfigForSubId(mPhone.getSubId());
        boolean isUssiEnabled = false;
        if (b != null) {
            isUssiEnabled = b.getBoolean("oppo_ussd_over_ims", false);
        }
        Rlog.d(LOG_TAG, "isUssiEnabled " + isUssiEnabled);
        return isUssiEnabled;
    }

    public boolean isRestricted(int type, int slot) {
        if (type == 0) {
            return OppoPolicyController.getCallInRestricted(slot);
        }
        if (type == 1) {
            return OppoPolicyController.getCallOutRestricted(slot);
        }
        if (type == 2) {
            return OppoPolicyController.getSmsReceiveRestricted(slot);
        }
        if (type == 3) {
            return OppoPolicyController.getSmsSendRestricted(slot);
        }
        if (type != 4) {
            return false;
        }
        return OppoPolicyController.isPsRestrictedEnable();
    }
}
