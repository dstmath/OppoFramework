package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.hardware.radio.V1_0.ApnTypes;
import android.hardware.radio.V1_0.CallForwardInfo;
import android.hardware.radio.V1_0.Carrier;
import android.hardware.radio.V1_0.CarrierRestrictions;
import android.hardware.radio.V1_0.CdmaBroadcastSmsConfigInfo;
import android.hardware.radio.V1_0.CdmaSmsAck;
import android.hardware.radio.V1_0.CdmaSmsMessage;
import android.hardware.radio.V1_0.CdmaSmsSubaddress;
import android.hardware.radio.V1_0.CdmaSmsWriteArgs;
import android.hardware.radio.V1_0.CellInfoCdma;
import android.hardware.radio.V1_0.CellInfoGsm;
import android.hardware.radio.V1_0.CellInfoLte;
import android.hardware.radio.V1_0.CellInfoWcdma;
import android.hardware.radio.V1_0.DataProfileInfo;
import android.hardware.radio.V1_0.Dial;
import android.hardware.radio.V1_0.GsmBroadcastSmsConfigInfo;
import android.hardware.radio.V1_0.GsmSmsMessage;
import android.hardware.radio.V1_0.HardwareConfig;
import android.hardware.radio.V1_0.HardwareConfigModem;
import android.hardware.radio.V1_0.HardwareConfigSim;
import android.hardware.radio.V1_0.IRadio;
import android.hardware.radio.V1_0.IccIo;
import android.hardware.radio.V1_0.ImsSmsMessage;
import android.hardware.radio.V1_0.LceDataInfo;
import android.hardware.radio.V1_0.NvWriteItem;
import android.hardware.radio.V1_0.RadioCapability;
import android.hardware.radio.V1_0.RadioError;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.V1_0.SelectUiccSub;
import android.hardware.radio.V1_0.SetupDataCallResult;
import android.hardware.radio.V1_0.SimApdu;
import android.hardware.radio.V1_0.SmsWriteArgs;
import android.hardware.radio.V1_0.UusInfo;
import android.hardware.radio.deprecated.V1_0.IOemHook;
import android.net.ConnectivityManager;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IHwBinder.DeathRecipient;
import android.os.Message;
import android.os.OppoManager;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.provider.oppo.CallLog.Calls;
import android.service.carrier.CarrierIdentifier;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.ClientRequestStats;
import android.telephony.ImsiEncryptionInfo;
import android.telephony.ModemActivityInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.NetworkScanRequest;
import android.telephony.PhoneNumberUtils;
import android.telephony.RadioAccessFamily;
import android.telephony.RadioAccessSpecifier;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import android.telephony.TelephonyHistogram;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.cdma.CdmaInformationRecords;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaDisplayInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaLineControlInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaNumberInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaRedirectingNumberInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaSignalInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaT53AudioControlInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaT53ClirInfoRec;
import com.android.internal.telephony.cdma.CdmaSmsBroadcastConfigInfo;
import com.android.internal.telephony.dataconnection.DataCallResponse;
import com.android.internal.telephony.dataconnection.DataProfile;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.SpnOverride;
import com.android.internal.telephony.util.NotificationChannelController;
import com.android.internal.util.Preconditions;
import com.google.android.mms.pdu.CharacterSets;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class RIL extends BaseCommands implements CommandsInterface {
    private static final int DEFAULT_ACK_WAKE_LOCK_TIMEOUT_MS = 200;
    private static final int DEFAULT_BLOCKING_MESSAGE_RESPONSE_TIMEOUT_MS = 2000;
    public static final long DEFAULT_DUP_SMS_KEPP_PERIOD = 300000;
    private static final int DEFAULT_WAKE_LOCK_TIMEOUT_MS = 60000;
    static final int EVENT_ACK_WAKE_LOCK_TIMEOUT = 4;
    static final int EVENT_BLOCKING_RESPONSE_TIMEOUT = 5;
    public static final int EVENT_PROCESS_SOLICITED = 2;
    public static final int EVENT_PROCESS_UNSOLICITED = 1;
    static final int EVENT_RADIO_PROXY_DEAD = 6;
    static final int EVENT_WAKE_LOCK_TIMEOUT = 2;
    public static final int FOR_ACK_WAKELOCK = 1;
    public static final int FOR_WAKELOCK = 0;
    static final String[] HIDL_SERVICE_NAME = new String[]{"slot1", "slot2", "slot3"};
    public static final int INVALID_WAKELOCK = -1;
    static final int IRADIO_GET_SERVICE_DELAY_MILLIS = 4000;
    public static final String ISSUE_SYS_OEM_NW_ANSWER_HANGUP_FAIL = "answer_hangup_fail";
    public static final String ISSUE_SYS_OEM_NW_CALL_NUMBER_UNKNOWN = "call_number_unknown";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_AS_FAILED = "as_failed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_AUTHENTICATION_REJECT = "authentication_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_CALL_DROP = "call_drop";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_RX_BREAK = "card_drop_rx_break";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_TIME_OUT = "card_drop_time_out";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_NOT_ALLOWED = "data_not_allowed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_NO_AVAILABLE_APN = "data_no_available_apn";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_SET_UP_DATA_ERROR = "data_setup_data_error";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_STALL_ERROR = "data_stall_error";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_FAKE_BS = "fake_bs";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_FAKE_BS_ONLY = "fake_bs_only";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_GSM_T3126_EXPIRED = "gsm_t3126_expired";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_AS_FAILED = "lte_as_failed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_REG_REJECT = "lte_reg_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_REG_WITHOUT_LTE = "lte_reg_without_lte";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MCFG_ICCID_FAILED = "mcfg_iccid_failed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MO_DROP = "mo_drop";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_CSFB = "mt_csfb";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_DISC = "mt_disconnect";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_PAGE_FAIL = "mt_fail_after_page";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_PCH = "mt_pch";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RACH = "mt_rach";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_REJECT = "mt_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RLF = "mt_rlf";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RRC = "mt_rrc";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_REJECT = "reg_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_ON = "srv_on";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_MCC = "srv_req_mcc";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_MNC = "srv_req_mnc";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_RAT = "srv_req_rat";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_RFFE_MISSING_NONFATAL = "rffe_missing_nonfatal";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_RF_MIPI_HW_FAILED = "rf_mipi_hw_failed";
    static final String RILJ_ACK_WAKELOCK_NAME = "RILJ_ACK_WL";
    static final boolean RILJ_LOGD = true;
    static final boolean RILJ_LOGV = false;
    static final String RILJ_LOG_TAG = "RILJ";
    static final int RIL_HISTOGRAM_BUCKET_COUNT = 5;
    public static final long SMS_ACK_FAILED_DEFAULT_TIME = -1;
    private static final int SYS_OEM_NW_DIAG_CAUSE_AS_FAILED = 65;
    private static final int SYS_OEM_NW_DIAG_CAUSE_AUTHENTICATION_REJECT = 64;
    private static final int SYS_OEM_NW_DIAG_CAUSE_CALL_BASE = 10;
    private static final int SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_RX_BREAK = 160;
    private static final int SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_TIME_OUT = 161;
    private static final int SYS_OEM_NW_DIAG_CAUSE_DATA_BASE = 110;
    private static final int SYS_OEM_NW_DIAG_CAUSE_DATA_NOT_ALLOWED = 110;
    private static final int SYS_OEM_NW_DIAG_CAUSE_DATA_NO_AVAILABLE_APN = 111;
    private static final int SYS_OEM_NW_DIAG_CAUSE_DATA_SET_UP_DATA_ERROR = 112;
    private static final int SYS_OEM_NW_DIAG_CAUSE_FAKE_BS = 70;
    private static final int SYS_OEM_NW_DIAG_CAUSE_FAKE_BS_ONLY = 71;
    private static final int SYS_OEM_NW_DIAG_CAUSE_GSM_T3126_EXPIRED = 66;
    private static final int SYS_OEM_NW_DIAG_CAUSE_LTE_AS_FAILED = 60;
    private static final int SYS_OEM_NW_DIAG_CAUSE_LTE_AUTHENTICATION_REJECT = 68;
    private static final int SYS_OEM_NW_DIAG_CAUSE_LTE_REG_REJECT = 61;
    private static final int SYS_OEM_NW_DIAG_CAUSE_LTE_REG_WITHOUT_LTE = 62;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MCFG_ICCID_FAILED = 67;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MO_DROP = 10;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MT_CSFB = 14;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MT_DISC = 20;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MT_PAGE_FAIL = 19;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MT_PCH = 13;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MT_RACH = 11;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MT_REJECT = 15;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MT_RLF = 12;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MT_RRC = 16;
    private static final int SYS_OEM_NW_DIAG_CAUSE_REG_BASE = 60;
    private static final int SYS_OEM_NW_DIAG_CAUSE_REG_MCC_CHANGE = 69;
    private static final int SYS_OEM_NW_DIAG_CAUSE_REG_REJECT = 63;
    private static final int SYS_OEM_NW_DIAG_CAUSE_REG_SRV_ON = 75;
    private static final int SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_MCC = 73;
    private static final int SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_MNC = 74;
    private static final int SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_RAT = 72;
    private static final int SYS_OEM_NW_DIAG_CAUSE_RFFE_MISSING_NONFATAL = 211;
    private static final int SYS_OEM_NW_DIAG_CAUSE_RF_BASE = 210;
    private static final int SYS_OEM_NW_DIAG_CAUSE_RF_MIPI_HW_FAILED = 210;
    private static final int SYS_OEM_NW_DIAG_CAUSE_UIM_BASE = 160;
    private static final int USER_APP_MAX = 1000;
    private static int[] mLocationUid = new int[3000];
    private static HashMap<Integer, Integer> mRilCount = new HashMap();
    static SparseArray<TelephonyHistogram> mRilTimeHistograms = new SparseArray();
    final WakeLock mAckWakeLock;
    final int mAckWakeLockTimeout;
    volatile int mAckWlSequenceNum;
    private WorkSource mActiveWakelockWorkSource;
    private final ClientWakelockTracker mClientWakelockTracker;
    private int mFakeBSArfcn;
    public long mHangupTime;
    private final int[] mIgnoreKey;
    protected boolean mIsMobileNetworkSupported;
    public Handler mKeylogHandler;
    Object[] mLastNITZTimeInfo;
    private TelephonyMetrics mMetrics;
    OemHookIndication mOemHookIndication;
    volatile IOemHook mOemHookProxy;
    OemHookResponse mOemHookResponse;
    final Integer mPhoneId;
    protected WorkSource mRILDefaultWorkSource;
    RadioIndication mRadioIndication;
    volatile IRadio mRadioProxy;
    final AtomicLong mRadioProxyCookie;
    final RadioProxyDeathRecipient mRadioProxyDeathRecipient;
    RadioResponse mRadioResponse;
    SparseArray<RILRequest> mRequestList;
    final RilHandler mRilHandler;
    AtomicBoolean mTestingEmergencyCall;
    final WakeLock mWakeLock;
    int mWakeLockCount;
    final int mWakeLockTimeout;
    volatile int mWlSequenceNum;
    public long smsAckFailedTime;

    final class RadioProxyDeathRecipient implements DeathRecipient {
        RadioProxyDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            RIL.this.riljLog("serviceDied");
            RIL.this.mRilHandler.sendMessageDelayed(RIL.this.mRilHandler.obtainMessage(6, Long.valueOf(cookie)), 4000);
        }
    }

    class RilHandler extends Handler {
        RilHandler() {
        }

        public void handleMessage(Message msg) {
            RILRequest rr;
            switch (msg.what) {
                case 2:
                    synchronized (RIL.this.mRequestList) {
                        if (msg.arg1 == RIL.this.mWlSequenceNum && RIL.this.clearWakeLock(0)) {
                            int count = RIL.this.mRequestList.size();
                            Rlog.d(RIL.RILJ_LOG_TAG, "WAKE_LOCK_TIMEOUT  mRequestList=" + count);
                            for (int i = 0; i < count; i++) {
                                rr = (RILRequest) RIL.this.mRequestList.valueAt(i);
                                Rlog.d(RIL.RILJ_LOG_TAG, i + ": [" + rr.mSerial + "] " + RIL.requestToString(rr.mRequest));
                            }
                        }
                    }
                    return;
                case 4:
                    if (msg.arg1 == RIL.this.mAckWlSequenceNum) {
                        boolean -wrap2 = RIL.this.clearWakeLock(1);
                        return;
                    }
                    return;
                case 5:
                    rr = RIL.this.findAndRemoveRequestFromList(msg.arg1);
                    if (rr != null) {
                        if (rr.mResult != null) {
                            AsyncResult.forMessage(rr.mResult, RIL.getResponseForTimedOutRILRequest(rr), null);
                            rr.mResult.sendToTarget();
                            RIL.this.mMetrics.writeOnRilTimeoutResponse(RIL.this.mPhoneId.intValue(), rr.mSerial, rr.mRequest);
                        }
                        RIL.this.decrementWakeLock(rr);
                        rr.release();
                        return;
                    }
                    return;
                case 6:
                    RIL.this.riljLog("handleMessage: EVENT_RADIO_PROXY_DEAD cookie = " + msg.obj + " mRadioProxyCookie = " + RIL.this.mRadioProxyCookie.get());
                    if (((Long) msg.obj).longValue() == RIL.this.mRadioProxyCookie.get()) {
                        RIL.this.resetProxyAndRequestList();
                        RIL.this.getRadioProxy(null);
                        RIL.this.getOemHookProxy(null);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private static class USER {
        public static int SYSTEM = 0;
        public static int USER1 = 1;
        public static int USER2 = 2;

        private USER() {
        }
    }

    public static List<TelephonyHistogram> getTelephonyRILTimingHistograms() {
        List<TelephonyHistogram> list;
        synchronized (mRilTimeHistograms) {
            list = new ArrayList(mRilTimeHistograms.size());
            for (int i = 0; i < mRilTimeHistograms.size(); i++) {
                list.add(new TelephonyHistogram((TelephonyHistogram) mRilTimeHistograms.valueAt(i)));
            }
        }
        return list;
    }

    private static Object getResponseForTimedOutRILRequest(RILRequest rr) {
        if (rr == null) {
            return null;
        }
        Object timeoutResponse = null;
        switch (rr.mRequest) {
            case 135:
                timeoutResponse = new ModemActivityInfo(0, 0, 0, new int[5], 0, 0);
                break;
        }
        return timeoutResponse;
    }

    protected void resetProxyAndRequestList() {
        this.mRadioProxy = null;
        this.mOemHookProxy = null;
        this.mRadioProxyCookie.incrementAndGet();
        setRadioState(RadioState.RADIO_UNAVAILABLE);
        RILRequest.resetSerial();
        clearRequestList(1, false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x007c A:{Splitter: B:9:0x001a, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:26:0x007c, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:27:0x007d, code:
            r8.mRadioProxy = null;
            riljLoge("RadioProxy getService/setResponseFunctions: " + r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private IRadio getRadioProxy(Message result) {
        if (!this.mIsMobileNetworkSupported) {
            if (result != null) {
                AsyncResult.forMessage(result, null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return null;
        } else if (this.mRadioProxy != null) {
            return this.mRadioProxy;
        } else {
            try {
                this.mRadioProxy = IRadio.getService(HIDL_SERVICE_NAME[this.mPhoneId == null ? 0 : this.mPhoneId.intValue()]);
                if (this.mRadioProxy != null) {
                    this.mRadioProxy.linkToDeath(this.mRadioProxyDeathRecipient, this.mRadioProxyCookie.incrementAndGet());
                    this.mRadioProxy.setResponseFunctions(this.mRadioResponse, this.mRadioIndication);
                } else {
                    riljLoge("getRadioProxy: mRadioProxy == null");
                }
            } catch (Exception e) {
            }
            if (this.mRadioProxy == null) {
                if (result != null) {
                    AsyncResult.forMessage(result, null, CommandException.fromRilErrno(1));
                    result.sendToTarget();
                }
                this.mRilHandler.sendMessageDelayed(this.mRilHandler.obtainMessage(6, Long.valueOf(this.mRadioProxyCookie.incrementAndGet())), 4000);
            }
            return this.mRadioProxy;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x006f A:{Splitter: B:9:0x001a, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:26:0x006f, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:27:0x0070, code:
            r6.mOemHookProxy = null;
            riljLoge("OemHookProxy getService/setResponseFunctions: " + r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private IOemHook getOemHookProxy(Message result) {
        if (!this.mIsMobileNetworkSupported) {
            if (result != null) {
                AsyncResult.forMessage(result, null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return null;
        } else if (this.mOemHookProxy != null) {
            return this.mOemHookProxy;
        } else {
            try {
                this.mOemHookProxy = IOemHook.getService(HIDL_SERVICE_NAME[this.mPhoneId == null ? 0 : this.mPhoneId.intValue()]);
                if (this.mOemHookProxy != null) {
                    this.mOemHookProxy.setResponseFunctions(this.mOemHookResponse, this.mOemHookIndication);
                } else {
                    riljLoge("getOemHookProxy: mOemHookProxy == null");
                }
            } catch (Exception e) {
            }
            if (this.mOemHookProxy == null) {
                if (result != null) {
                    AsyncResult.forMessage(result, null, CommandException.fromRilErrno(1));
                    result.sendToTarget();
                }
                this.mRilHandler.sendMessageDelayed(this.mRilHandler.obtainMessage(6, Long.valueOf(this.mRadioProxyCookie.incrementAndGet())), 4000);
            }
            return this.mOemHookProxy;
        }
    }

    public RIL(Context context, int preferredNetworkType, int cdmaSubscription) {
        this(context, preferredNetworkType, cdmaSubscription, null);
    }

    public RIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context);
        this.mClientWakelockTracker = new ClientWakelockTracker();
        this.mWlSequenceNum = 0;
        this.mAckWlSequenceNum = 0;
        this.mRequestList = new SparseArray();
        this.mTestingEmergencyCall = new AtomicBoolean(false);
        this.mMetrics = TelephonyMetrics.getInstance();
        this.mRadioProxy = null;
        this.mOemHookProxy = null;
        this.mRadioProxyCookie = new AtomicLong(0);
        this.smsAckFailedTime = -1;
        this.mIgnoreKey = new int[]{1009, 19, 135, 1028};
        this.mHangupTime = -1;
        this.mFakeBSArfcn = -1;
        this.mKeylogHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        RIL.this.riljLog("EVENT_PROCESS_UNSOLICITED");
                        RIL.this.oppoProcessUnsolOemKeyLogErrMsg((Bundle) msg.obj);
                        return;
                    case 2:
                        RIL.this.riljLog("EVENT_PROCESS_SOLICITED");
                        RIL.this.oppoProcessSolOemKeyLogErrMsg(msg.arg1, msg.arg2);
                        return;
                    default:
                        return;
                }
            }
        };
        riljLog("RIL: init preferredNetworkType=" + preferredNetworkType + " cdmaSubscription=" + cdmaSubscription + ")");
        this.mContext = context;
        this.mCdmaSubscription = cdmaSubscription;
        this.mPreferredNetworkType = preferredNetworkType;
        this.mPhoneType = 0;
        this.mPhoneId = instanceId;
        this.mIsMobileNetworkSupported = ((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(0);
        this.mRadioResponse = new RadioResponse(this);
        this.mRadioIndication = new RadioIndication(this);
        this.mOemHookResponse = new OemHookResponse(this);
        this.mOemHookIndication = new OemHookIndication(this);
        this.mRilHandler = new RilHandler();
        this.mRadioProxyDeathRecipient = new RadioProxyDeathRecipient();
        PowerManager pm = (PowerManager) context.getSystemService("power");
        this.mWakeLock = pm.newWakeLock(1, RILJ_LOG_TAG);
        this.mWakeLock.setReferenceCounted(false);
        this.mAckWakeLock = pm.newWakeLock(1, RILJ_ACK_WAKELOCK_NAME);
        this.mAckWakeLock.setReferenceCounted(false);
        this.mWakeLockTimeout = SystemProperties.getInt("ro.ril.wake_lock_timeout", 60000);
        this.mAckWakeLockTimeout = SystemProperties.getInt("ro.ril.wake_lock_timeout", 200);
        this.mWakeLockCount = 0;
        this.mRILDefaultWorkSource = new WorkSource(context.getApplicationInfo().uid, context.getPackageName());
        TelephonyDevController tdc = TelephonyDevController.getInstance();
        TelephonyDevController.registerRIL(this);
        getRadioProxy(null);
        getOemHookProxy(null);
    }

    public void setOnNITZTime(Handler h, int what, Object obj) {
        super.setOnNITZTime(h, what, obj);
        if (this.mLastNITZTimeInfo != null) {
            this.mNITZTimeRegistrant.notifyRegistrant(new AsyncResult(null, this.mLastNITZTimeInfo, null));
        }
    }

    private void addRequest(RILRequest rr) {
        acquireWakeLock(rr, 0);
        synchronized (this.mRequestList) {
            rr.mStartTimeMs = SystemClock.elapsedRealtime();
            this.mRequestList.append(rr.mSerial, rr);
        }
        if (rr != null) {
            countRilRequestMsg(rr.mRequest);
        }
    }

    private RILRequest obtainRequest(int request, Message result, WorkSource workSource) {
        RILRequest rr = RILRequest.obtain(request, result, workSource);
        addRequest(rr);
        return rr;
    }

    protected int obtainRequestSerial(int request, Message result, WorkSource workSource) {
        RILRequest rr = RILRequest.obtain(request, result, workSource);
        addRequest(rr);
        return rr.mSerial;
    }

    private void handleRadioProxyExceptionForRR(RILRequest rr, String caller, Exception e) {
        riljLoge(caller + ": " + e);
        resetProxyAndRequestList();
        this.mRilHandler.sendMessageDelayed(this.mRilHandler.obtainMessage(6, Long.valueOf(this.mRadioProxyCookie.incrementAndGet())), 4000);
    }

    private String convertNullToEmptyString(String string) {
        return string != null ? string : SpnOverride.MVNO_TYPE_NONE;
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0038 A:{Splitter: B:3:0x0032, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0038, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0039, code:
            handleRadioProxyExceptionForRR(r2, "getIccCardStatus", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getIccCardStatus(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(1, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getIccCardStatus(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    public void supplyIccPin(String pin, Message result) {
        supplyIccPinForApp(pin, null, result);
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x004b A:{Splitter: B:3:0x003d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x004b, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x004c, code:
            handleRadioProxyExceptionForRR(r2, "supplyIccPinForApp", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void supplyIccPinForApp(String pin, String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " aid = " + aid);
            try {
                radioProxy.supplyIccPinForApp(rr.mSerial, convertNullToEmptyString(pin), convertNullToEmptyString(aid));
            } catch (Exception e) {
            }
        }
    }

    public void supplyIccPuk(String puk, String newPin, Message result) {
        supplyIccPukForApp(puk, newPin, null, result);
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x004f A:{Splitter: B:3:0x003d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x004f, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0050, code:
            handleRadioProxyExceptionForRR(r2, "supplyIccPukForApp", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void supplyIccPukForApp(String puk, String newPin, String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(3, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " aid = " + aid);
            try {
                radioProxy.supplyIccPukForApp(rr.mSerial, convertNullToEmptyString(puk), convertNullToEmptyString(newPin), convertNullToEmptyString(aid));
            } catch (Exception e) {
            }
        }
    }

    public void supplyIccPin2(String pin, Message result) {
        supplyIccPin2ForApp(pin, null, result);
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x004b A:{Splitter: B:3:0x003d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x004b, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x004c, code:
            handleRadioProxyExceptionForRR(r2, "supplyIccPin2ForApp", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void supplyIccPin2ForApp(String pin, String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(4, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " aid = " + aid);
            try {
                radioProxy.supplyIccPin2ForApp(rr.mSerial, convertNullToEmptyString(pin), convertNullToEmptyString(aid));
            } catch (Exception e) {
            }
        }
    }

    public void supplyIccPuk2(String puk2, String newPin2, Message result) {
        supplyIccPuk2ForApp(puk2, newPin2, null, result);
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x004f A:{Splitter: B:3:0x003d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x004f, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0050, code:
            handleRadioProxyExceptionForRR(r2, "supplyIccPuk2ForApp", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void supplyIccPuk2ForApp(String puk, String newPin2, String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(5, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " aid = " + aid);
            try {
                radioProxy.supplyIccPuk2ForApp(rr.mSerial, convertNullToEmptyString(puk), convertNullToEmptyString(newPin2), convertNullToEmptyString(aid));
            } catch (Exception e) {
            }
        }
    }

    public void changeIccPin(String oldPin, String newPin, Message result) {
        changeIccPinForApp(oldPin, newPin, null, result);
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0065 A:{Splitter: B:3:0x0053, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0065, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0066, code:
            handleRadioProxyExceptionForRR(r2, "changeIccPinForApp", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void changeIccPinForApp(String oldPin, String newPin, String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(6, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " oldPin = " + oldPin + " newPin = " + newPin + " aid = " + aid);
            try {
                radioProxy.changeIccPinForApp(rr.mSerial, convertNullToEmptyString(oldPin), convertNullToEmptyString(newPin), convertNullToEmptyString(aid));
            } catch (Exception e) {
            }
        }
    }

    public void changeIccPin2(String oldPin2, String newPin2, Message result) {
        changeIccPin2ForApp(oldPin2, newPin2, null, result);
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0065 A:{Splitter: B:3:0x0053, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0065, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0066, code:
            handleRadioProxyExceptionForRR(r2, "changeIccPin2ForApp", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void changeIccPin2ForApp(String oldPin2, String newPin2, String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(7, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " oldPin = " + oldPin2 + " newPin = " + newPin2 + " aid = " + aid);
            try {
                radioProxy.changeIccPin2ForApp(rr.mSerial, convertNullToEmptyString(oldPin2), convertNullToEmptyString(newPin2), convertNullToEmptyString(aid));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0048 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0048, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0049, code:
            handleRadioProxyExceptionForRR(r2, "supplyNetworkDepersonalization", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void supplyNetworkDepersonalization(String netpin, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(8, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " netpin = " + netpin);
            try {
                radioProxy.supplyNetworkDepersonalization(rr.mSerial, convertNullToEmptyString(netpin));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getCurrentCalls", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getCurrentCalls(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(9, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getCurrentCalls(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    public void dial(String address, int clirMode, Message result) {
        dial(address, clirMode, null, result);
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x007d A:{Splitter: B:6:0x0064, ExcHandler: android.os.RemoteException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:9:0x007d, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:0x007e, code:
            handleRadioProxyExceptionForRR(r4, "dial", r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dial(String address, int clirMode, UUSInfo uusInfo, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(10, result, this.mRILDefaultWorkSource);
            Dial dialInfo = new Dial();
            dialInfo.address = convertNullToEmptyString(address);
            dialInfo.clir = clirMode;
            if (uusInfo != null) {
                UusInfo info = new UusInfo();
                info.uusType = uusInfo.getType();
                info.uusDcs = uusInfo.getDcs();
                info.uusData = new String(uusInfo.getUserData());
                dialInfo.uusInfo.add(info);
            }
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.dial(rr.mSerial, dialInfo);
            } catch (Exception e) {
            }
            this.mKeylogHandler.removeMessages(2);
            this.mKeylogHandler.sendMessageDelayed(this.mKeylogHandler.obtainMessage(2, 10, -1), 20000);
        }
    }

    public void getIMSI(Message result) {
        getIMSIForApp(null, result);
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0048 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0048, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0049, code:
            handleRadioProxyExceptionForRR(r2, "getIMSIForApp", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getIMSIForApp(String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(11, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " aid = " + aid);
            try {
                radioProxy.getImsiForApp(rr.mSerial, convertNullToEmptyString(aid));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x004a A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:6:0x004a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:7:0x004b, code:
            handleRadioProxyExceptionForRR(r2, "hangupConnection", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void hangupConnection(int gsmIndex, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(12, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " gsmIndex = " + gsmIndex);
            try {
                radioProxy.hangup(rr.mSerial, gsmIndex);
            } catch (Exception e) {
            }
            this.mHangupTime = System.currentTimeMillis();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "hangupWaitingOrBackground", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void hangupWaitingOrBackground(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(13, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.hangupWaitingOrBackground(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "hangupForegroundResumeBackground", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void hangupForegroundResumeBackground(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(14, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.hangupForegroundResumeBackground(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "switchWaitingOrHoldingAndActive", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void switchWaitingOrHoldingAndActive(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(15, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.switchWaitingOrHoldingAndActive(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "conference", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void conference(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(16, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.conference(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "rejectCall", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void rejectCall(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(17, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.rejectCall(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getLastCallFailCause", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getLastCallFailCause(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(18, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getLastCallFailCause(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getSignalStrength", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getSignalStrength(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(19, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getSignalStrength(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getVoiceRegistrationState", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getVoiceRegistrationState(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(20, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getVoiceRegistrationState(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getDataRegistrationState", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getDataRegistrationState(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(21, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getDataRegistrationState(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getOperator", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getOperator(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(22, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getOperator(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "setRadioPower", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setRadioPower(boolean on, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(23, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " on = " + on);
            try {
                radioProxy.setRadioPower(rr.mSerial, on);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x004d A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x004d, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x004e, code:
            handleRadioProxyExceptionForRR(r2, "sendDtmf", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendDtmf(char c, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(24, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.sendDtmf(rr.mSerial, c + SpnOverride.MVNO_TYPE_NONE);
            } catch (Exception e) {
            }
        }
    }

    private GsmSmsMessage constructGsmSendSmsRilRequest(String smscPdu, String pdu) {
        GsmSmsMessage msg = new GsmSmsMessage();
        if (smscPdu == null) {
            smscPdu = SpnOverride.MVNO_TYPE_NONE;
        }
        msg.smscPdu = smscPdu;
        if (pdu == null) {
            pdu = SpnOverride.MVNO_TYPE_NONE;
        }
        msg.pdu = pdu;
        return msg;
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x004c A:{Splitter: B:3:0x0037, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x004c, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x004d, code:
            handleRadioProxyExceptionForRR(r3, "sendSMS", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendSMS(String smscPdu, String pdu, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(25, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.sendSms(rr.mSerial, constructGsmSendSmsRilRequest(smscPdu, pdu));
                this.mMetrics.writeRilSendSms(this.mPhoneId.intValue(), rr.mSerial, 1, 1);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x004c A:{Splitter: B:3:0x0037, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x004c, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x004d, code:
            handleRadioProxyExceptionForRR(r3, "sendSMSExpectMore", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendSMSExpectMore(String smscPdu, String pdu, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(26, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.sendSMSExpectMore(rr.mSerial, constructGsmSendSmsRilRequest(smscPdu, pdu));
                this.mMetrics.writeRilSendSms(this.mPhoneId.intValue(), rr.mSerial, 1, 1);
            } catch (Exception e) {
            }
        }
    }

    private static int convertToHalMvnoType(String mvnoType) {
        if (mvnoType.equals(SpnOverride.MVNO_TYPE_IMSI)) {
            return 1;
        }
        if (mvnoType.equals(SpnOverride.MVNO_TYPE_GID)) {
            return 2;
        }
        if (mvnoType.equals(SpnOverride.MVNO_TYPE_SPN)) {
            return 3;
        }
        return 0;
    }

    private static DataProfileInfo convertToHalDataProfile(DataProfile dp) {
        DataProfileInfo dpi = new DataProfileInfo();
        dpi.profileId = dp.profileId;
        dpi.apn = dp.apn;
        dpi.protocol = dp.protocol;
        dpi.roamingProtocol = dp.roamingProtocol;
        dpi.authType = dp.authType;
        dpi.user = dp.user;
        dpi.password = dp.password;
        dpi.type = dp.type;
        dpi.maxConnsTime = dp.maxConnsTime;
        dpi.maxConns = dp.maxConns;
        dpi.waitTime = dp.waitTime;
        dpi.enabled = dp.enabled;
        dpi.supportedApnTypesBitmap = dp.supportedApnTypesBitmap;
        dpi.bearerBitmap = dp.bearerBitmap;
        dpi.mtu = dp.mtu;
        dpi.mvnoType = convertToHalMvnoType(dp.mvnoType);
        dpi.mvnoMatchData = dp.mvnoMatchData;
        return dpi;
    }

    private static int convertToHalResetNvType(int resetType) {
        switch (resetType) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
                return 2;
            default:
                return -1;
        }
    }

    static DataCallResponse convertDataCallResult(SetupDataCallResult dcResult) {
        return new DataCallResponse(dcResult.status, dcResult.suggestedRetryTime, dcResult.cid, dcResult.active, dcResult.type, dcResult.ifname, dcResult.addresses, dcResult.dnses, dcResult.gateways, dcResult.pcscf, dcResult.mtu);
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0096 A:{Splitter: B:3:0x006f, ExcHandler: android.os.RemoteException (r13_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0096, code:
            r13 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0097, code:
            handleRadioProxyExceptionForRR(r14, "setupDataCall", r13);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setupDataCall(int radioTechnology, DataProfile dataProfile, boolean isRoaming, boolean allowRoaming, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(27, result, this.mRILDefaultWorkSource);
            DataProfileInfo dpi = convertToHalDataProfile(dataProfile);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ",radioTechnology=" + radioTechnology + ",isRoaming=" + isRoaming + ",allowRoaming=" + allowRoaming + "," + dataProfile);
            try {
                radioProxy.setupDataCall(rr.mSerial, radioTechnology, dpi, dataProfile.modemCognitive, allowRoaming, isRoaming);
                this.mMetrics.writeRilSetupDataCall(this.mPhoneId.intValue(), rr.mSerial, radioTechnology, dpi.profileId, dpi.apn, dpi.authType, dpi.protocol);
            } catch (Exception e) {
            }
        }
    }

    public void iccIO(int command, int fileId, String path, int p1, int p2, int p3, String data, String pin2, Message result) {
        iccIOForApp(command, fileId, path, p1, p2, p3, data, pin2, null, result);
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x00c4 A:{Splitter: B:3:0x00be, ExcHandler: android.os.RemoteException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x00c4, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x00c5, code:
            handleRadioProxyExceptionForRR(r4, "iccIOForApp", r1);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void iccIOForApp(int command, int fileId, String path, int p1, int p2, int p3, String data, String pin2, String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(28, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> iccIO: " + requestToString(rr.mRequest) + " command = 0x" + Integer.toHexString(command) + " fileId = 0x" + Integer.toHexString(fileId) + " path = " + path + " p1 = " + p1 + " p2 = " + p2 + " p3 = " + " data = " + data + " aid = " + aid);
            IccIo iccIo = new IccIo();
            iccIo.command = command;
            iccIo.fileId = fileId;
            iccIo.path = convertNullToEmptyString(path);
            iccIo.p1 = p1;
            iccIo.p2 = p2;
            iccIo.p3 = p3;
            iccIo.data = convertNullToEmptyString(data);
            iccIo.pin2 = convertNullToEmptyString(pin2);
            iccIo.aid = convertNullToEmptyString(aid);
            try {
                radioProxy.iccIOForApp(rr.mSerial, iccIo);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x004b A:{Splitter: B:3:0x0041, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x004b, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x004c, code:
            handleRadioProxyExceptionForRR(r3, "sendUSSD", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendUSSD(String ussd, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(29, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " ussd = " + "*******");
            try {
                radioProxy.sendUssd(rr.mSerial, convertNullToEmptyString(ussd));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "cancelPendingUssd", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void cancelPendingUssd(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(30, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.cancelPendingUssd(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getCLIR", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getCLIR(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(31, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getClir(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "setCLIR", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setCLIR(int clirMode, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(32, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " clirMode = " + clirMode);
            try {
                radioProxy.setClir(rr.mSerial, clirMode);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0067 A:{Splitter: B:3:0x0061, ExcHandler: android.os.RemoteException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0067, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0068, code:
            handleRadioProxyExceptionForRR(r3, "queryCallForwardStatus", r1);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void queryCallForwardStatus(int cfReason, int serviceClass, String number, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(33, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " cfreason = " + cfReason + " serviceClass = " + serviceClass);
            CallForwardInfo cfInfo = new CallForwardInfo();
            cfInfo.reason = cfReason;
            cfInfo.serviceClass = serviceClass;
            cfInfo.toa = PhoneNumberUtils.toaFromString(number);
            cfInfo.number = convertNullToEmptyString(number);
            cfInfo.timeSeconds = 0;
            try {
                radioProxy.getCallForwardStatus(rr.mSerial, cfInfo);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x007e A:{Splitter: B:3:0x0078, ExcHandler: android.os.RemoteException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x007e, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x007f, code:
            handleRadioProxyExceptionForRR(r3, "setCallForward", r1);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setCallForward(int action, int cfReason, int serviceClass, String number, int timeSeconds, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(34, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " action = " + action + " cfReason = " + cfReason + " serviceClass = " + serviceClass + " timeSeconds = " + timeSeconds);
            CallForwardInfo cfInfo = new CallForwardInfo();
            cfInfo.status = action;
            cfInfo.reason = cfReason;
            cfInfo.serviceClass = serviceClass;
            cfInfo.toa = PhoneNumberUtils.toaFromString(number);
            cfInfo.number = convertNullToEmptyString(number);
            cfInfo.timeSeconds = timeSeconds;
            try {
                radioProxy.setCallForward(rr.mSerial, cfInfo);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "queryCallWaiting", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void queryCallWaiting(int serviceClass, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(35, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " serviceClass = " + serviceClass);
            try {
                radioProxy.getCallWaiting(rr.mSerial, serviceClass);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x004f A:{Splitter: B:3:0x0049, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x004f, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0050, code:
            handleRadioProxyExceptionForRR(r2, "setCallWaiting", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setCallWaiting(boolean enable, int serviceClass, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(36, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " enable = " + enable + " serviceClass = " + serviceClass);
            try {
                radioProxy.setCallWaiting(rr.mSerial, enable, serviceClass);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0052 A:{Splitter: B:3:0x004c, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0052, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0053, code:
            handleRadioProxyExceptionForRR(r2, "acknowledgeLastIncomingGsmSms", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void acknowledgeLastIncomingGsmSms(boolean success, int cause, Message result) {
        setSmsAckFailedTime(success);
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(37, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " success = " + success + " cause = " + cause);
            try {
                radioProxy.acknowledgeLastIncomingGsmSms(rr.mSerial, success, cause);
            } catch (Exception e) {
            }
        }
    }

    private void setSmsAckFailedTime(boolean smsAckSuccess) {
        if (!smsAckSuccess) {
            Rlog.e(NotificationChannelController.CHANNEL_ID_SMS, "---ack---error----!!");
            this.smsAckFailedTime = System.currentTimeMillis();
        } else if (this.smsAckFailedTime != -1 && System.currentTimeMillis() < this.smsAckFailedTime + DEFAULT_DUP_SMS_KEPP_PERIOD) {
            this.smsAckFailedTime = -1;
        }
    }

    public boolean needCheckSmsWhenLastAckFailed(long checkpointTime) {
        if (this.smsAckFailedTime == -1) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0046 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0046, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0047, code:
            handleRadioProxyExceptionForRR(r2, "acceptCall", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void acceptCall(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(40, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.acceptCall(rr.mSerial);
                this.mMetrics.writeRilAnswer(this.mPhoneId.intValue(), rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0061 A:{Splitter: B:3:0x004a, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:8:0x0061, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:9:0x0062, code:
            handleRadioProxyExceptionForRR(r2, "deactivateDataCall", r0);
     */
    /* JADX WARNING: Missing block: B:12:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void deactivateDataCall(int cid, int reason, Message result) {
        boolean z = false;
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(41, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " cid = " + cid + " reason = " + reason);
            try {
                int i = rr.mSerial;
                if (reason != 0) {
                    z = true;
                }
                radioProxy.deactivateDataCall(i, cid, z);
                this.mMetrics.writeRilDeactivateDataCall(this.mPhoneId.intValue(), rr.mSerial, cid, reason);
            } catch (Exception e) {
            }
        }
    }

    public void queryFacilityLock(String facility, String password, int serviceClass, Message result) {
        queryFacilityLockForApp(facility, password, serviceClass, null, result);
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0067 A:{Splitter: B:3:0x0054, ExcHandler: android.os.RemoteException (r6_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0067, code:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0068, code:
            handleRadioProxyExceptionForRR(r7, "getFacilityLockForApp", r6);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void queryFacilityLockForApp(String facility, String password, int serviceClass, String appId, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(42, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " facility = " + facility + " serviceClass = " + serviceClass + " appId = " + appId);
            try {
                radioProxy.getFacilityLockForApp(rr.mSerial, convertNullToEmptyString(facility), convertNullToEmptyString(password), serviceClass, convertNullToEmptyString(appId));
            } catch (Exception e) {
            }
        }
    }

    public void setFacilityLock(String facility, boolean lockState, String password, int serviceClass, Message result) {
        setFacilityLockForApp(facility, lockState, password, serviceClass, null, result);
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0073 A:{Splitter: B:3:0x005f, ExcHandler: android.os.RemoteException (r7_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0073, code:
            r7 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0074, code:
            handleRadioProxyExceptionForRR(r8, "setFacilityLockForApp", r7);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setFacilityLockForApp(String facility, boolean lockState, String password, int serviceClass, String appId, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(43, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " facility = " + facility + " lockstate = " + lockState + " serviceClass = " + serviceClass + " appId = " + appId);
            try {
                radioProxy.setFacilityLockForApp(rr.mSerial, convertNullToEmptyString(facility), lockState, convertNullToEmptyString(password), serviceClass, convertNullToEmptyString(appId));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0050 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0050, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0051, code:
            handleRadioProxyExceptionForRR(r2, "changeBarringPassword", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void changeBarringPassword(String facility, String oldPwd, String newPwd, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(44, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + "facility = " + facility);
            try {
                radioProxy.setBarringPassword(rr.mSerial, convertNullToEmptyString(facility), convertNullToEmptyString(oldPwd), convertNullToEmptyString(newPwd));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getNetworkSelectionMode", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getNetworkSelectionMode(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(45, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getNetworkSelectionMode(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "setNetworkSelectionModeAutomatic", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setNetworkSelectionModeAutomatic(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(46, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.setNetworkSelectionModeAutomatic(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0048 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0048, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0049, code:
            handleRadioProxyExceptionForRR(r2, "setNetworkSelectionModeManual", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setNetworkSelectionModeManual(String operatorNumeric, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(47, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " operatorNumeric = " + operatorNumeric);
            try {
                radioProxy.setNetworkSelectionModeManual(rr.mSerial, convertNullToEmptyString(operatorNumeric));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getAvailableNetworks", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getAvailableNetworks(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(48, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getAvailableNetworks(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x00fe A:{Splitter: B:30:0x00f7, ExcHandler: android.os.RemoteException (r5_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:32:0x00fe, code:
            r5 = move-exception;
     */
    /* JADX WARNING: Missing block: B:33:0x00ff, code:
            handleRadioProxyExceptionForRR(r10, "startNetworkScan", r5);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startNetworkScan(NetworkScanRequest nsr, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            android.hardware.radio.V1_1.IRadio radioProxy11 = android.hardware.radio.V1_1.IRadio.castFrom(radioProxy);
            if (radioProxy11 != null) {
                android.hardware.radio.V1_1.NetworkScanRequest request = new android.hardware.radio.V1_1.NetworkScanRequest();
                request.type = nsr.scanType;
                request.interval = 60;
                for (RadioAccessSpecifier ras : nsr.specifiers) {
                    List<Integer> bands;
                    android.hardware.radio.V1_1.RadioAccessSpecifier s = new android.hardware.radio.V1_1.RadioAccessSpecifier();
                    s.radioAccessNetwork = ras.radioAccessNetwork;
                    switch (ras.radioAccessNetwork) {
                        case 1:
                            bands = s.geranBands;
                            break;
                        case 2:
                            bands = s.utranBands;
                            break;
                        case 3:
                            bands = s.eutranBands;
                            break;
                        default:
                            Log.wtf(RILJ_LOG_TAG, "radioAccessNetwork " + ras.radioAccessNetwork + " not supported!");
                            return;
                    }
                    if (ras.bands != null) {
                        for (int band : ras.bands) {
                            bands.add(Integer.valueOf(band));
                        }
                    }
                    if (ras.channels != null) {
                        for (int channel : ras.channels) {
                            s.channels.add(Integer.valueOf(channel));
                        }
                    }
                    request.specifiers.add(s);
                }
                RILRequest rr = obtainRequest(142, result, this.mRILDefaultWorkSource);
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
                try {
                    radioProxy11.startNetworkScan(rr.mSerial, request);
                } catch (Exception e) {
                }
            } else if (result != null) {
                AsyncResult.forMessage(result, null, CommandException.fromRilErrno(6));
                result.sendToTarget();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x004e A:{Splitter: B:7:0x0048, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:9:0x004e, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:0x004f, code:
            handleRadioProxyExceptionForRR(r3, "stopNetworkScan", r0);
     */
    /* JADX WARNING: Missing block: B:14:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stopNetworkScan(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            android.hardware.radio.V1_1.IRadio radioProxy11 = android.hardware.radio.V1_1.IRadio.castFrom(radioProxy);
            if (radioProxy11 != null) {
                RILRequest rr = obtainRequest(143, result, this.mRILDefaultWorkSource);
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
                try {
                    radioProxy11.stopNetworkScan(rr.mSerial);
                } catch (Exception e) {
                }
            } else if (result != null) {
                AsyncResult.forMessage(result, null, CommandException.fromRilErrno(6));
                result.sendToTarget();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x004d A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x004d, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x004e, code:
            handleRadioProxyExceptionForRR(r2, "startDtmf", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startDtmf(char c, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(49, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.startDtmf(rr.mSerial, c + SpnOverride.MVNO_TYPE_NONE);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "stopDtmf", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stopDtmf(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(50, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.stopDtmf(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "separateConnection", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void separateConnection(int gsmIndex, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(52, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " gsmIndex = " + gsmIndex);
            try {
                radioProxy.separateConnection(rr.mSerial, gsmIndex);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getBasebandVersion", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getBasebandVersion(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(51, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getBasebandVersion(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "setMute", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setMute(boolean enableMute, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(53, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " enableMute = " + enableMute);
            try {
                radioProxy.setMute(rr.mSerial, enableMute);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getMute", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getMute(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(54, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getMute(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "queryCLIP", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void queryCLIP(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(55, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getClip(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    @Deprecated
    public void getPDPContextList(Message result) {
        getDataCallList(result);
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getDataCallList", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getDataCallList(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(57, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getDataCallList(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0061 A:{Splitter: B:3:0x0057, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0061, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0062, code:
            handleRadioProxyExceptionForRR(r2, "invokeOemRilRequestStrings", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void invokeOemRilRequestRaw(byte[] data, Message response) {
        IOemHook oemHookProxy = getOemHookProxy(response);
        if (oemHookProxy != null) {
            RILRequest rr = obtainRequest(59, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + "[" + IccUtils.bytesToHexString(data) + "]");
            Rlog.e(RILJ_LOG_TAG, "invokeOemRilRequestRaw", new Throwable());
            try {
                oemHookProxy.sendRequestRaw(rr.mSerial, primitiveArrayToArrayList(data));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x007f A:{Splitter: B:7:0x0070, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:9:0x007f, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:0x0080, code:
            handleRadioProxyExceptionForRR(r4, "invokeOemRilRequestStrings", r0);
     */
    /* JADX WARNING: Missing block: B:14:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void invokeOemRilRequestStrings(String[] strings, Message result) {
        IOemHook oemHookProxy = getOemHookProxy(result);
        if (oemHookProxy != null) {
            RILRequest rr = obtainRequest(60, result, this.mRILDefaultWorkSource);
            String logStr = SpnOverride.MVNO_TYPE_NONE;
            for (String str : strings) {
                logStr = logStr + str + " ";
            }
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " strings = " + logStr);
            Rlog.e(RILJ_LOG_TAG, "invokeOemRilRequestStrings", new Throwable());
            try {
                oemHookProxy.sendRequestStrings(rr.mSerial, new ArrayList(Arrays.asList(strings)));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "setSuppServiceNotifications", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setSuppServiceNotifications(boolean enable, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(62, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " enable = " + enable);
            try {
                radioProxy.setSuppServiceNotifications(rr.mSerial, enable);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x002b A:{Splitter: B:3:0x0025, ExcHandler: android.os.RemoteException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x002b, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x002c, code:
            handleRadioProxyExceptionForRR(r3, "writeSmsToSim", r1);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void writeSmsToSim(int status, String smsc, String pdu, Message result) {
        status = translateStatus(status);
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(63, result, this.mRILDefaultWorkSource);
            SmsWriteArgs args = new SmsWriteArgs();
            args.status = status;
            args.smsc = convertNullToEmptyString(smsc);
            args.pdu = convertNullToEmptyString(pdu);
            try {
                radioProxy.writeSmsToSim(rr.mSerial, args);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0014 A:{Splitter: B:3:0x000e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0014, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0015, code:
            handleRadioProxyExceptionForRR(r2, "deleteSmsOnSim", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void deleteSmsOnSim(int index, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(64, result, this.mRILDefaultWorkSource);
            try {
                radioProxy.deleteSmsOnSim(rr.mSerial, index);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "setBandMode", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setBandMode(int bandMode, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(65, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " bandMode = " + bandMode);
            try {
                radioProxy.setBandMode(rr.mSerial, bandMode);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "queryAvailableBandMode", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void queryAvailableBandMode(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(66, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getAvailableBandModes(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0048 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0048, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0049, code:
            handleRadioProxyExceptionForRR(r2, "sendEnvelope", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendEnvelope(String contents, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(69, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " contents = " + contents);
            try {
                radioProxy.sendEnvelope(rr.mSerial, convertNullToEmptyString(contents));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0048 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0048, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0049, code:
            handleRadioProxyExceptionForRR(r2, "sendTerminalResponse", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendTerminalResponse(String contents, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(70, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " contents = " + contents);
            try {
                radioProxy.sendTerminalResponseToSim(rr.mSerial, convertNullToEmptyString(contents));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0048 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0048, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0049, code:
            handleRadioProxyExceptionForRR(r2, "sendEnvelopeWithStatus", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendEnvelopeWithStatus(String contents, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(107, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " contents = " + contents);
            try {
                radioProxy.sendEnvelopeWithStatus(rr.mSerial, convertNullToEmptyString(contents));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "explicitCallTransfer", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void explicitCallTransfer(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(72, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.explicitCallTransfer(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0051 A:{Splitter: B:3:0x004b, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0051, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0052, code:
            handleRadioProxyExceptionForRR(r2, "setPreferredNetworkType", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPreferredNetworkType(int networkType, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(73, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " networkType = " + networkType);
            this.mPreferredNetworkType = networkType;
            this.mMetrics.writeSetPreferredNetworkType(this.mPhoneId.intValue(), networkType);
            try {
                radioProxy.setPreferredNetworkType(rr.mSerial, networkType);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getPreferredNetworkType", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getPreferredNetworkType(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(74, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getPreferredNetworkType(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x003b A:{Splitter: B:3:0x0035, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x003b, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003c, code:
            handleRadioProxyExceptionForRR(r2, "getNeighboringCids", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getNeighboringCids(Message result, WorkSource workSource) {
        workSource = getDeafultWorkSourceIfInvalid(workSource);
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(75, result, workSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getNeighboringCids(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "setLocationUpdates", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setLocationUpdates(boolean enable, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(76, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " enable = " + enable);
            try {
                radioProxy.setLocationUpdates(rr.mSerial, enable);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "setCdmaSubscriptionSource", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setCdmaSubscriptionSource(int cdmaSubscription, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(77, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " cdmaSubscription = " + cdmaSubscription);
            try {
                radioProxy.setCdmaSubscriptionSource(rr.mSerial, cdmaSubscription);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "queryCdmaRoamingPreference", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void queryCdmaRoamingPreference(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(79, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getCdmaRoamingPreference(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "setCdmaRoamingPreference", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setCdmaRoamingPreference(int cdmaRoamingType, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(78, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " cdmaRoamingType = " + cdmaRoamingType);
            try {
                radioProxy.setCdmaRoamingPreference(rr.mSerial, cdmaRoamingType);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "queryTTYMode", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void queryTTYMode(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(81, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getTTYMode(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "setTTYMode", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setTTYMode(int ttyMode, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(80, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " ttyMode = " + ttyMode);
            try {
                radioProxy.setTTYMode(rr.mSerial, ttyMode);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "setPreferredVoicePrivacy", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPreferredVoicePrivacy(boolean enable, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(82, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " enable = " + enable);
            try {
                radioProxy.setPreferredVoicePrivacy(rr.mSerial, enable);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getPreferredVoicePrivacy", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getPreferredVoicePrivacy(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(83, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getPreferredVoicePrivacy(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0048 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0048, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0049, code:
            handleRadioProxyExceptionForRR(r2, "sendCDMAFeatureCode", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendCDMAFeatureCode(String featureCode, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(84, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " featureCode = " + featureCode);
            try {
                radioProxy.sendCDMAFeatureCode(rr.mSerial, convertNullToEmptyString(featureCode));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x005e A:{Splitter: B:3:0x0054, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x005e, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x005f, code:
            handleRadioProxyExceptionForRR(r2, "sendBurstDtmf", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendBurstDtmf(String dtmfString, int on, int off, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(85, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " dtmfString = " + dtmfString + " on = " + on + " off = " + off);
            try {
                radioProxy.sendBurstDtmf(rr.mSerial, convertNullToEmptyString(dtmfString), on, off);
            } catch (Exception e) {
            }
        }
    }

    private void constructCdmaSendSmsRilRequest(CdmaSmsMessage msg, byte[] pdu) {
        boolean z = true;
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(pdu));
        try {
            boolean z2;
            int i;
            msg.teleserviceId = dis.readInt();
            if (((byte) dis.readInt()) == (byte) 1) {
                z2 = true;
            } else {
                z2 = false;
            }
            msg.isServicePresent = z2;
            msg.serviceCategory = dis.readInt();
            msg.address.digitMode = dis.read();
            msg.address.numberMode = dis.read();
            msg.address.numberType = dis.read();
            msg.address.numberPlan = dis.read();
            int addrNbrOfDigits = (byte) dis.read();
            for (i = 0; i < addrNbrOfDigits; i++) {
                msg.address.digits.add(Byte.valueOf(dis.readByte()));
            }
            msg.subAddress.subaddressType = dis.read();
            CdmaSmsSubaddress cdmaSmsSubaddress = msg.subAddress;
            if (((byte) dis.read()) != (byte) 1) {
                z = false;
            }
            cdmaSmsSubaddress.odd = z;
            int subaddrNbrOfDigits = (byte) dis.read();
            for (i = 0; i < subaddrNbrOfDigits; i++) {
                msg.subAddress.digits.add(Byte.valueOf(dis.readByte()));
            }
            int bearerDataLength = dis.read();
            for (i = 0; i < bearerDataLength; i++) {
                msg.bearerData.add(Byte.valueOf(dis.readByte()));
            }
        } catch (IOException ex) {
            riljLog("sendSmsCdma: conversion from input stream to object failed: " + ex);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0050 A:{Splitter: B:3:0x003b, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0050, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0051, code:
            handleRadioProxyExceptionForRR(r3, "sendCdmaSms", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendCdmaSms(byte[] pdu, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(87, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            CdmaSmsMessage msg = new CdmaSmsMessage();
            constructCdmaSendSmsRilRequest(msg, pdu);
            try {
                radioProxy.sendCdmaSms(rr.mSerial, msg);
                this.mMetrics.writeRilSendSms(this.mPhoneId.intValue(), rr.mSerial, 2, 2);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0060 A:{Splitter: B:6:0x0058, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:9:0x0060, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:0x0061, code:
            handleRadioProxyExceptionForRR(r3, "acknowledgeLastIncomingCdmaSms", r0);
     */
    /* JADX WARNING: Missing block: B:13:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void acknowledgeLastIncomingCdmaSms(boolean success, int cause, Message result) {
        setSmsAckFailedTime(success);
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(88, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " success = " + success + " cause = " + cause);
            CdmaSmsAck msg = new CdmaSmsAck();
            msg.errorClass = success ? 0 : 1;
            msg.smsCauseCode = cause;
            try {
                radioProxy.acknowledgeLastIncomingCdmaSms(rr.mSerial, msg);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getGsmBroadcastConfig", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getGsmBroadcastConfig(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(89, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getGsmBroadcastConfig(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0098 A:{Splitter: B:9:0x0092, ExcHandler: android.os.RemoteException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:11:0x0098, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:12:0x0099, code:
            handleRadioProxyExceptionForRR(r6, "setGsmBroadcastConfig", r1);
     */
    /* JADX WARNING: Missing block: B:16:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setGsmBroadcastConfig(SmsBroadcastConfigInfo[] config, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            int i;
            RILRequest rr = obtainRequest(90, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " with " + config.length + " configs : ");
            for (SmsBroadcastConfigInfo smsBroadcastConfigInfo : config) {
                riljLog(smsBroadcastConfigInfo.toString());
            }
            ArrayList<GsmBroadcastSmsConfigInfo> configs = new ArrayList();
            int numOfConfig = config.length;
            for (i = 0; i < numOfConfig; i++) {
                GsmBroadcastSmsConfigInfo info = new GsmBroadcastSmsConfigInfo();
                info.fromServiceId = config[i].getFromServiceId();
                info.toServiceId = config[i].getToServiceId();
                info.fromCodeScheme = config[i].getFromCodeScheme();
                info.toCodeScheme = config[i].getToCodeScheme();
                info.selected = config[i].isSelected();
                configs.add(info);
            }
            try {
                radioProxy.setGsmBroadcastConfig(rr.mSerial, configs);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "setGsmBroadcastActivation", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setGsmBroadcastActivation(boolean activate, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(91, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " activate = " + activate);
            try {
                radioProxy.setGsmBroadcastActivation(rr.mSerial, activate);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getCdmaBroadcastConfig", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getCdmaBroadcastConfig(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(92, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getCdmaBroadcastConfig(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0098 A:{Splitter: B:13:0x0092, ExcHandler: android.os.RemoteException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:15:0x0098, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:16:0x0099, code:
            handleRadioProxyExceptionForRR(r8, "setCdmaBroadcastConfig", r3);
     */
    /* JADX WARNING: Missing block: B:21:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setCdmaBroadcastConfig(CdmaSmsBroadcastConfigInfo[] configs, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(93, result, this.mRILDefaultWorkSource);
            ArrayList<CdmaBroadcastSmsConfigInfo> halConfigs = new ArrayList();
            for (CdmaSmsBroadcastConfigInfo config : configs) {
                for (int i = config.getFromServiceCategory(); i <= config.getToServiceCategory(); i++) {
                    CdmaBroadcastSmsConfigInfo info = new CdmaBroadcastSmsConfigInfo();
                    info.serviceCategory = i;
                    info.language = config.getLanguage();
                    info.selected = config.isSelected();
                    halConfigs.add(info);
                }
            }
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " with " + halConfigs.size() + " configs : ");
            for (CdmaBroadcastSmsConfigInfo config2 : halConfigs) {
                riljLog(config2.toString());
            }
            try {
                radioProxy.setCdmaBroadcastConfig(rr.mSerial, halConfigs);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "setCdmaBroadcastActivation", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setCdmaBroadcastActivation(boolean activate, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(94, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " activate = " + activate);
            try {
                radioProxy.setCdmaBroadcastActivation(rr.mSerial, activate);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getCDMASubscription", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getCDMASubscription(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(95, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getCDMASubscription(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x0028 A:{Splitter: B:4:0x0022, ExcHandler: android.os.RemoteException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:6:0x0028, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:8:?, code:
            handleRadioProxyExceptionForRR(r4, "writeSmsToRuim", r1);
     */
    /* JADX WARNING: Missing block: B:14:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void writeSmsToRuim(int status, String pdu, Message result) {
        try {
            status = translateStatus(status);
            IRadio radioProxy = getRadioProxy(result);
            if (radioProxy != null) {
                RILRequest rr = obtainRequest(96, result, this.mRILDefaultWorkSource);
                CdmaSmsWriteArgs args = new CdmaSmsWriteArgs();
                args.status = status;
                constructCdmaSendSmsRilRequest(args.message, IccUtils.hexStringToBytes(pdu));
                try {
                    radioProxy.writeSmsToRuim(rr.mSerial, args);
                } catch (Exception e) {
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0014 A:{Splitter: B:3:0x000e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0014, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0015, code:
            handleRadioProxyExceptionForRR(r2, "deleteSmsOnRuim", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void deleteSmsOnRuim(int index, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(97, result, this.mRILDefaultWorkSource);
            try {
                radioProxy.deleteSmsOnRuim(rr.mSerial, index);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getDeviceIdentity", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getDeviceIdentity(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(98, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getDeviceIdentity(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "exitEmergencyCallbackMode", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void exitEmergencyCallbackMode(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(99, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.exitEmergencyCallbackMode(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getSmscAddress", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getSmscAddress(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(100, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getSmscAddress(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0048 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0048, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0049, code:
            handleRadioProxyExceptionForRR(r2, "setSmscAddress", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setSmscAddress(String address, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(101, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " address = " + address);
            try {
                radioProxy.setSmscAddress(rr.mSerial, convertNullToEmptyString(address));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "reportSmsMemoryStatus", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void reportSmsMemoryStatus(boolean available, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(102, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " available = " + available);
            try {
                radioProxy.reportSmsMemoryStatus(rr.mSerial, available);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "reportStkServiceIsRunning", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void reportStkServiceIsRunning(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(103, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.reportStkServiceIsRunning(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getCdmaSubscriptionSource", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getCdmaSubscriptionSource(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(104, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getCdmaSubscriptionSource(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0048 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0048, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0049, code:
            handleRadioProxyExceptionForRR(r2, "requestIsimAuthentication", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void requestIsimAuthentication(String nonce, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(105, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " nonce = " + nonce);
            try {
                radioProxy.requestIsimAuthentication(rr.mSerial, convertNullToEmptyString(nonce));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x004b A:{Splitter: B:3:0x0041, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x004b, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x004c, code:
            handleRadioProxyExceptionForRR(r2, "acknowledgeIncomingGsmSmsWithPdu", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void acknowledgeIncomingGsmSmsWithPdu(boolean success, String ackPdu, Message result) {
        setSmsAckFailedTime(success);
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(106, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " success = " + success);
            try {
                radioProxy.acknowledgeIncomingGsmSmsWithPdu(rr.mSerial, success, convertNullToEmptyString(ackPdu));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getVoiceRadioTechnology", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getVoiceRadioTechnology(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(108, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getVoiceRadioTechnology(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0090 A:{Splitter: B:3:0x0037, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:12:0x0090, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:13:0x0091, code:
            handleRadioProxyExceptionForRR(r2, "getCellInfoList", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getCellInfoList(Message result, WorkSource workSource) {
        workSource = getDeafultWorkSourceIfInvalid(workSource);
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(109, result, workSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getCellInfoList(rr.mSerial);
            } catch (Exception e) {
            }
            if (OemConstant.getPowerCenterEnable(this.mContext)) {
                int uid = Binder.getCallingUid();
                int user = USER.SYSTEM;
                if (uid >= 10000 && uid <= 19999) {
                    user = USER.USER1;
                } else if (uid > 19999) {
                    user = USER.USER2;
                }
                int[] iArr = mLocationUid;
                int i = (user * 1000) + (uid % 1000);
                iArr[i] = iArr[i] + 1;
                riljLog("[POWERSTATE] call Uid=" + uid + " package:" + this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid()));
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0046 A:{Splitter: B:3:0x0040, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0046, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0047, code:
            handleRadioProxyExceptionForRR(r2, "setCellInfoListRate", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setCellInfoListRate(int rateInMillis, Message result, WorkSource workSource) {
        workSource = getDeafultWorkSourceIfInvalid(workSource);
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(110, result, workSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " rateInMillis = " + rateInMillis);
            try {
                radioProxy.setCellInfoListRate(rr.mSerial, rateInMillis);
            } catch (Exception e) {
            }
        }
    }

    void setCellInfoListRate() {
        setCellInfoListRate(Integer.MAX_VALUE, null, this.mRILDefaultWorkSource);
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0043 A:{Splitter: B:3:0x0037, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0043, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0044, code:
            handleRadioProxyExceptionForRR(r2, "setInitialAttachApn", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setInitialAttachApn(DataProfile dataProfile, boolean isRoaming, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(111, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + dataProfile);
            try {
                radioProxy.setInitialAttachApn(rr.mSerial, convertToHalDataProfile(dataProfile), dataProfile.modemCognitive, isRoaming);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getImsRegistrationState", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getImsRegistrationState(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(112, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getImsRegistrationState(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0063 A:{Splitter: B:5:0x004c, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:8:0x0063, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:9:0x0064, code:
            handleRadioProxyExceptionForRR(r4, "sendImsGsmSms", r0);
     */
    /* JADX WARNING: Missing block: B:12:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendImsGsmSms(String smscPdu, String pdu, int retry, int messageRef, Message result) {
        boolean z = true;
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(113, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            ImsSmsMessage msg = new ImsSmsMessage();
            msg.tech = 1;
            if (((byte) retry) <= (byte) 0) {
                z = false;
            }
            msg.retry = z;
            msg.messageRef = messageRef;
            msg.gsmMessage.add(constructGsmSendSmsRilRequest(smscPdu, pdu));
            try {
                radioProxy.sendImsSms(rr.mSerial, msg);
                this.mMetrics.writeRilSendSms(this.mPhoneId.intValue(), rr.mSerial, 3, 1);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0068 A:{Splitter: B:5:0x0051, ExcHandler: android.os.RemoteException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:8:0x0068, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:9:0x0069, code:
            handleRadioProxyExceptionForRR(r4, "sendImsCdmaSms", r1);
     */
    /* JADX WARNING: Missing block: B:12:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendImsCdmaSms(byte[] pdu, int retry, int messageRef, Message result) {
        boolean z = true;
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(113, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            ImsSmsMessage msg = new ImsSmsMessage();
            msg.tech = 2;
            if (((byte) retry) <= (byte) 0) {
                z = false;
            }
            msg.retry = z;
            msg.messageRef = messageRef;
            CdmaSmsMessage cdmaMsg = new CdmaSmsMessage();
            constructCdmaSendSmsRilRequest(cdmaMsg, pdu);
            msg.cdmaMessage.add(cdmaMsg);
            try {
                radioProxy.sendImsSms(rr.mSerial, msg);
                this.mMetrics.writeRilSendSms(this.mPhoneId.intValue(), rr.mSerial, 3, 1);
            } catch (Exception e) {
            }
        }
    }

    private SimApdu createSimApdu(int channel, int cla, int instruction, int p1, int p2, int p3, String data) {
        SimApdu msg = new SimApdu();
        msg.sessionId = channel;
        msg.cla = cla;
        msg.instruction = instruction;
        msg.p1 = p1;
        msg.p2 = p2;
        msg.p3 = p3;
        msg.data = convertNullToEmptyString(data);
        return msg;
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0091 A:{Splitter: B:3:0x008b, ExcHandler: android.os.RemoteException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0091, code:
            r9 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0092, code:
            handleRadioProxyExceptionForRR(r12, "iccTransmitApduBasicChannel", r9);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void iccTransmitApduBasicChannel(int cla, int instruction, int p1, int p2, int p3, String data, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(114, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " cla = " + cla + " instruction = " + instruction + " p1 = " + p1 + " p2 = " + " p3 = " + p3 + " data = " + data);
            try {
                radioProxy.iccTransmitApduBasicChannel(rr.mSerial, createSimApdu(0, cla, instruction, p1, p2, p3, data));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0053 A:{Splitter: B:3:0x0049, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0053, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0054, code:
            handleRadioProxyExceptionForRR(r2, "iccOpenLogicalChannel", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void iccOpenLogicalChannel(String aid, int p2, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(115, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " aid = " + aid + " p2 = " + p2);
            try {
                radioProxy.iccOpenLogicalChannel(rr.mSerial, convertNullToEmptyString(aid), p2);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "iccCloseLogicalChannel", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void iccCloseLogicalChannel(int channel, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(116, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " channel = " + channel);
            try {
                radioProxy.iccCloseLogicalChannel(rr.mSerial, channel);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x00a2 A:{Splitter: B:6:0x009c, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:8:0x00a2, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:9:0x00a3, code:
            handleRadioProxyExceptionForRR(r3, "iccTransmitApduLogicalChannel", r0);
     */
    /* JADX WARNING: Missing block: B:12:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void iccTransmitApduLogicalChannel(int channel, int cla, int instruction, int p1, int p2, int p3, String data, Message result) {
        if (channel <= 0) {
            throw new RuntimeException("Invalid channel in iccTransmitApduLogicalChannel: " + channel);
        }
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(117, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " channel = " + channel + " cla = " + cla + " instruction = " + instruction + " p1 = " + p1 + " p2 = " + " p3 = " + p3 + " data = " + data);
            try {
                radioProxy.iccTransmitApduLogicalChannel(rr.mSerial, createSimApdu(channel, cla, instruction, p1, p2, p3, data));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "nvReadItem", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void nvReadItem(int itemID, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(118, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " itemId = " + itemID);
            try {
                radioProxy.nvReadItem(rr.mSerial, itemID);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x005c A:{Splitter: B:3:0x0056, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x005c, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x005d, code:
            handleRadioProxyExceptionForRR(r3, "nvWriteItem", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void nvWriteItem(int itemId, String itemValue, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(119, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " itemId = " + itemId + " itemValue = " + itemValue);
            NvWriteItem item = new NvWriteItem();
            item.itemId = itemId;
            item.value = convertNullToEmptyString(itemValue);
            try {
                radioProxy.nvWriteItem(rr.mSerial, item);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x005d A:{Splitter: B:6:0x0057, ExcHandler: android.os.RemoteException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:8:0x005d, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:9:0x005e, code:
            handleRadioProxyExceptionForRR(r4, "nvWriteCdmaPrl", r1);
     */
    /* JADX WARNING: Missing block: B:12:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void nvWriteCdmaPrl(byte[] preferredRoamingList, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(120, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " PreferredRoamingList = 0x" + IccUtils.bytesToHexString(preferredRoamingList));
            ArrayList<Byte> arrList = new ArrayList();
            for (byte valueOf : preferredRoamingList) {
                arrList.add(Byte.valueOf(valueOf));
            }
            try {
                radioProxy.nvWriteCdmaPrl(rr.mSerial, arrList);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0048 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0048, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0049, code:
            handleRadioProxyExceptionForRR(r2, "nvResetConfig", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void nvResetConfig(int resetType, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(121, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " resetType = " + resetType);
            try {
                radioProxy.nvResetConfig(rr.mSerial, convertToHalResetNvType(resetType));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0072 A:{Splitter: B:3:0x006c, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0072, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0073, code:
            handleRadioProxyExceptionForRR(r3, "setUiccSubscription", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setUiccSubscription(int slotId, int appIndex, int subId, int subStatus, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(122, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " slot = " + slotId + " appIndex = " + appIndex + " subId = " + subId + " subStatus = " + subStatus);
            SelectUiccSub info = new SelectUiccSub();
            info.slot = slotId;
            info.appIndex = appIndex;
            info.subType = subId;
            info.actStatus = subStatus;
            try {
                radioProxy.setUiccSubscription(rr.mSerial, info);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "setDataAllowed", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setDataAllowed(boolean allowed, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(123, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " allowed = " + allowed);
            try {
                radioProxy.setDataAllowed(rr.mSerial, allowed);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getHardwareConfig", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getHardwareConfig(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(124, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getHardwareConfig(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0041 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0041, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0042, code:
            handleRadioProxyExceptionForRR(r2, "requestIccSimAuthentication", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void requestIccSimAuthentication(int authContext, String data, String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(125, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.requestIccSimAuthentication(rr.mSerial, authContext, convertNullToEmptyString(data), convertNullToEmptyString(aid));
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0065 A:{Splitter: B:8:0x005f, ExcHandler: android.os.RemoteException (r2_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:10:0x0065, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x0066, code:
            handleRadioProxyExceptionForRR(r5, "setDataProfile", r2);
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setDataProfile(DataProfile[] dps, boolean isRoaming, Message result) {
        int i = 0;
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(128, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " with data profiles : ");
            for (DataProfile profile : dps) {
                riljLog(profile.toString());
            }
            ArrayList<DataProfileInfo> dpis = new ArrayList();
            int length = dps.length;
            while (i < length) {
                dpis.add(convertToHalDataProfile(dps[i]));
                i++;
            }
            try {
                radioProxy.setDataProfile(rr.mSerial, dpis, isRoaming);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "requestShutdown", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void requestShutdown(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(129, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.requestShutdown(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getRadioCapability", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getRadioCapability(Message response) {
        IRadio radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(130, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getRadioCapability(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    public void setRadioCapability(RadioCapability rc, Message response) {
        IRadio radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(131, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " RadioCapability = " + rc.toString());
            RadioCapability halRc = new RadioCapability();
            halRc.session = rc.getSession();
            halRc.phase = rc.getPhase();
            halRc.raf = rc.getRadioAccessFamily();
            halRc.logicalModemUuid = convertNullToEmptyString(rc.getLogicalModemUuid());
            halRc.status = rc.getStatus();
            try {
                radioProxy.setRadioCapability(rr.mSerial, halRc);
            } catch (Exception e) {
                handleRadioProxyExceptionForRR(rr, "setRadioCapability", e);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x004f A:{Splitter: B:3:0x0049, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x004f, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0050, code:
            handleRadioProxyExceptionForRR(r2, "startLceService", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startLceService(int reportIntervalMs, boolean pullMode, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(132, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " reportIntervalMs = " + reportIntervalMs + " pullMode = " + pullMode);
            try {
                radioProxy.startLceService(rr.mSerial, reportIntervalMs, pullMode);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "stopLceService", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stopLceService(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(133, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.stopLceService(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "pullLceData", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void pullLceData(Message response) {
        IRadio radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(134, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.pullLceData(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x004e A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x004e, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x004f, code:
            handleRadioProxyExceptionForRR(r3, "getModemActivityInfo", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getModemActivityInfo(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(135, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getModemActivityInfo(rr.mSerial);
                Message msg = this.mRilHandler.obtainMessage(5);
                msg.obj = null;
                msg.arg1 = rr.mSerial;
                this.mRilHandler.sendMessageDelayed(msg, 2000);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0101 A:{Splitter: B:26:0x00fb, ExcHandler: android.os.RemoteException (r6_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:28:0x0101, code:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:29:0x0102, code:
            handleRadioProxyExceptionForRR(r12, "setAllowedCarriers", r6);
     */
    /* JADX WARNING: Missing block: B:38:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setAllowedCarriers(List<CarrierIdentifier> carriers, Message result) {
        Preconditions.checkNotNull(carriers, "Allowed carriers list cannot be null.");
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            boolean allAllowed;
            RILRequest rr = obtainRequest(136, result, this.mRILDefaultWorkSource);
            String logStr = SpnOverride.MVNO_TYPE_NONE;
            for (int i = 0; i < carriers.size(); i++) {
                logStr = logStr + carriers.get(i) + " ";
            }
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " carriers = " + logStr);
            if (carriers.size() == 0) {
                allAllowed = true;
            } else {
                allAllowed = false;
            }
            CarrierRestrictions carrierList = new CarrierRestrictions();
            for (CarrierIdentifier ci : carriers) {
                Carrier c = new Carrier();
                c.mcc = convertNullToEmptyString(ci.getMcc());
                c.mnc = convertNullToEmptyString(ci.getMnc());
                int matchType = 0;
                String matchData = null;
                if (!TextUtils.isEmpty(ci.getSpn())) {
                    matchType = 1;
                    matchData = ci.getSpn();
                } else if (!TextUtils.isEmpty(ci.getImsi())) {
                    matchType = 2;
                    matchData = ci.getImsi();
                } else if (!TextUtils.isEmpty(ci.getGid1())) {
                    matchType = 3;
                    matchData = ci.getGid1();
                } else if (!TextUtils.isEmpty(ci.getGid2())) {
                    matchType = 4;
                    matchData = ci.getGid2();
                }
                c.matchType = matchType;
                c.matchData = convertNullToEmptyString(matchData);
                carrierList.allowedCarriers.add(c);
            }
            try {
                radioProxy.setAllowedCarriers(rr.mSerial, allAllowed, carrierList);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getAllowedCarriers", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getAllowedCarriers(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(137, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getAllowedCarriers(rr.mSerial);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x004f A:{Splitter: B:3:0x0049, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x004f, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0050, code:
            handleRadioProxyExceptionForRR(r2, "sendDeviceState", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendDeviceState(int stateType, boolean state, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(138, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + stateType + ":" + state);
            try {
                radioProxy.sendDeviceState(rr.mSerial, stateType, state);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0044 A:{Splitter: B:3:0x003e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0044, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0045, code:
            handleRadioProxyExceptionForRR(r2, "setIndicationFilter", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setUnsolResponseFilter(int filter, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(139, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + filter);
            try {
                radioProxy.setIndicationFilter(rr.mSerial, filter);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x005d A:{Splitter: B:7:0x004a, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0072 A:{Splitter: B:14:0x006c, ExcHandler: android.os.RemoteException (r0_1 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:16:0x0072, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:17:0x0073, code:
            handleRadioProxyExceptionForRR(r3, "setSimCardPower", r0);
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setSimCardPower(int state, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(140, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + state);
            android.hardware.radio.V1_1.IRadio radioProxy11 = android.hardware.radio.V1_1.IRadio.castFrom(radioProxy);
            if (radioProxy11 == null) {
                switch (state) {
                    case 0:
                        radioProxy.setSimCardPower(rr.mSerial, false);
                        return;
                    case 1:
                        radioProxy.setSimCardPower(rr.mSerial, true);
                        return;
                    default:
                        if (result != null) {
                            try {
                                AsyncResult.forMessage(result, null, CommandException.fromRilErrno(6));
                                result.sendToTarget();
                                return;
                            } catch (Exception e) {
                            }
                        } else {
                            return;
                        }
                }
                handleRadioProxyExceptionForRR(rr, "setSimCardPower", e);
                return;
            }
            try {
                radioProxy11.setSimCardPower_1_1(rr.mSerial, state);
            } catch (Exception e2) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0096 A:{Splitter: B:7:0x004e, ExcHandler: android.os.RemoteException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:15:0x0096, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:16:0x0097, code:
            handleRadioProxyExceptionForRR(r5, "setCarrierInfoForImsiEncryption", r1);
     */
    /* JADX WARNING: Missing block: B:21:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setCarrierInfoForImsiEncryption(ImsiEncryptionInfo imsiEncryptionInfo, Message result) {
        Preconditions.checkNotNull(imsiEncryptionInfo, "ImsiEncryptionInfo cannot be null.");
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            android.hardware.radio.V1_1.IRadio radioProxy11 = android.hardware.radio.V1_1.IRadio.castFrom(radioProxy);
            if (radioProxy11 != null) {
                RILRequest rr = obtainRequest(141, result, this.mRILDefaultWorkSource);
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
                try {
                    android.hardware.radio.V1_1.ImsiEncryptionInfo halImsiInfo = new android.hardware.radio.V1_1.ImsiEncryptionInfo();
                    halImsiInfo.mnc = imsiEncryptionInfo.getMnc();
                    halImsiInfo.mcc = imsiEncryptionInfo.getMcc();
                    halImsiInfo.keyIdentifier = imsiEncryptionInfo.getKeyIdentifier();
                    if (imsiEncryptionInfo.getExpirationTime() != null) {
                        halImsiInfo.expirationTime = imsiEncryptionInfo.getExpirationTime().getTime();
                    }
                    for (byte b : imsiEncryptionInfo.getPublicKey().getEncoded()) {
                        halImsiInfo.carrierKey.add(new Byte(b));
                    }
                    radioProxy11.setCarrierInfoForImsiEncryption(rr.mSerial, halImsiInfo);
                } catch (Exception e) {
                }
            } else if (result != null) {
                AsyncResult.forMessage(result, null, CommandException.fromRilErrno(6));
                result.sendToTarget();
            }
        }
    }

    public void getAtr(Message response) {
    }

    public void getIMEI(Message result) {
        throw new RuntimeException("getIMEI not expected to be called");
    }

    public void getIMEISV(Message result) {
        throw new RuntimeException("getIMEISV not expected to be called");
    }

    @Deprecated
    public void getLastPdpFailCause(Message result) {
        throw new RuntimeException("getLastPdpFailCause not expected to be called");
    }

    public void getLastDataCallFailCause(Message result) {
        throw new RuntimeException("getLastDataCallFailCause not expected to be called");
    }

    private int translateStatus(int status) {
        switch (status & 7) {
            case 1:
                return 1;
            case 3:
                return 0;
            case 5:
                return 3;
            case 7:
                return 2;
            default:
                return 1;
        }
    }

    public void resetRadio(Message result) {
        throw new RuntimeException("resetRadio not expected to be called");
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0039 A:{Splitter: B:3:0x0033, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003a, code:
            handleRadioProxyExceptionForRR(r2, "getAllowedCarriers", r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleCallSetupRequestFromSim(boolean accept, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(71, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.handleStkCallSetupRequestFromSim(rr.mSerial, accept);
            } catch (Exception e) {
            }
        }
    }

    void processIndication(int indicationType) {
        if (indicationType == 1) {
            sendAck();
            riljLog("Unsol response received; Sending ack to ril.cpp");
        }
    }

    void processRequestAck(int serial) {
        RILRequest rr;
        synchronized (this.mRequestList) {
            rr = (RILRequest) this.mRequestList.get(serial);
        }
        if (rr == null) {
            Rlog.w(RILJ_LOG_TAG, "processRequestAck: Unexpected solicited ack response! serial: " + serial);
            return;
        }
        decrementWakeLock(rr);
        riljLog(rr.serialString() + " Ack < " + requestToString(rr.mRequest));
    }

    protected RILRequest processResponse(RadioResponseInfo responseInfo) {
        int serial = responseInfo.serial;
        int error = responseInfo.error;
        int type = responseInfo.type;
        RILRequest rr;
        if (type == 1) {
            synchronized (this.mRequestList) {
                rr = (RILRequest) this.mRequestList.get(serial);
            }
            if (rr == null) {
                Rlog.w(RILJ_LOG_TAG, "Unexpected solicited ack response! sn: " + serial);
            } else {
                decrementWakeLock(rr);
                riljLog(rr.serialString() + " Ack < " + requestToString(rr.mRequest));
            }
            return rr;
        }
        rr = findAndRemoveRequestFromList(serial);
        if (rr == null) {
            Rlog.e(RILJ_LOG_TAG, "processResponse: Unexpected response! serial: " + serial + " error: " + error);
            return null;
        }
        addToRilHistogram(rr);
        if (type == 2) {
            sendAck();
            riljLog("Response received for " + rr.serialString() + " " + requestToString(rr.mRequest) + " Sending ack to ril.cpp");
        }
        switch (rr.mRequest) {
            case 3:
            case 5:
                if (this.mIccStatusChangedRegistrants != null) {
                    riljLog("ON enter sim puk fakeSimStatusChanged: reg count=" + this.mIccStatusChangedRegistrants.size());
                    this.mIccStatusChangedRegistrants.notifyRegistrants();
                    break;
                }
                break;
            case 129:
                setRadioState(RadioState.RADIO_UNAVAILABLE);
                break;
        }
        if (error == 0) {
            switch (rr.mRequest) {
                case 14:
                    if (this.mTestingEmergencyCall.getAndSet(false) && this.mEmergencyCallbackModeRegistrant != null) {
                        riljLog("testing emergency call, notify ECM Registrants");
                        this.mEmergencyCallbackModeRegistrant.notifyRegistrant();
                        break;
                    }
            }
        }
        switch (rr.mRequest) {
            case 2:
            case 4:
            case 6:
            case 7:
            case 43:
                if (this.mIccStatusChangedRegistrants != null) {
                    riljLog("ON some errors fakeSimStatusChanged: reg count=" + this.mIccStatusChangedRegistrants.size());
                    this.mIccStatusChangedRegistrants.notifyRegistrants();
                    break;
                }
                break;
        }
        return rr;
    }

    protected Message getMessageFromRequest(Object request) {
        RILRequest rr = (RILRequest) request;
        if (rr != null) {
            return rr.mResult;
        }
        return null;
    }

    void processResponseDone(RILRequest rr, RadioResponseInfo responseInfo, Object ret) {
        if (responseInfo.error == 0) {
            riljLog(rr.serialString() + "< " + requestToString(rr.mRequest) + " " + retToString(rr.mRequest, ret));
        } else {
            riljLog(rr.serialString() + "< " + requestToString(rr.mRequest) + " error " + responseInfo.error);
            rr.onError(responseInfo.error, ret);
        }
        this.mMetrics.writeOnRilSolicitedResponse(this.mPhoneId.intValue(), rr.mSerial, responseInfo.error, rr.mRequest, ret);
        if (rr != null) {
            if (responseInfo.type == 0) {
                decrementWakeLock(rr);
            }
            rr.release();
        }
    }

    protected void processResponseDone(Object request, RadioResponseInfo responseInfo, Object ret) {
        processResponseDone((RILRequest) request, responseInfo, ret);
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x001a A:{Splitter: B:2:0x0013, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:6:0x001a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:7:0x001b, code:
            handleRadioProxyExceptionForRR(r2, "sendAck", r0);
            riljLoge("sendAck: " + r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void sendAck() {
        RILRequest rr = RILRequest.obtain(800, null, this.mRILDefaultWorkSource);
        acquireWakeLock(rr, 1);
        IRadio radioProxy = getRadioProxy(null);
        if (radioProxy != null) {
            try {
                radioProxy.responseAcknowledgement();
            } catch (Exception e) {
            }
        } else {
            Rlog.e(RILJ_LOG_TAG, "Error trying to send ack, radioProxy = null");
        }
        rr.release();
    }

    private WorkSource getDeafultWorkSourceIfInvalid(WorkSource workSource) {
        if (workSource == null) {
            return this.mRILDefaultWorkSource;
        }
        return workSource;
    }

    private String getWorkSourceClientId(WorkSource workSource) {
        if (workSource != null) {
            return String.valueOf(workSource.get(0)) + ":" + workSource.getName(0);
        }
        return null;
    }

    private void acquireWakeLock(RILRequest rr, int wakeLockType) {
        synchronized (rr) {
            if (rr.mWakeLockType != -1) {
                Rlog.d(RILJ_LOG_TAG, "Failed to aquire wakelock for " + rr.serialString());
                return;
            }
            Message msg;
            switch (wakeLockType) {
                case 0:
                    synchronized (this.mWakeLock) {
                        this.mWakeLock.acquire();
                        this.mWakeLockCount++;
                        this.mWlSequenceNum++;
                        if (!this.mClientWakelockTracker.isClientActive(getWorkSourceClientId(rr.mWorkSource))) {
                            if (this.mActiveWakelockWorkSource != null) {
                                this.mActiveWakelockWorkSource.add(rr.mWorkSource);
                            } else {
                                this.mActiveWakelockWorkSource = rr.mWorkSource;
                            }
                            this.mWakeLock.setWorkSource(this.mActiveWakelockWorkSource);
                        }
                        this.mClientWakelockTracker.startTracking(rr.mClientId, rr.mRequest, rr.mSerial, this.mWakeLockCount);
                        msg = this.mRilHandler.obtainMessage(2);
                        msg.arg1 = this.mWlSequenceNum;
                        this.mRilHandler.sendMessageDelayed(msg, (long) this.mWakeLockTimeout);
                    }
                case 1:
                    synchronized (this.mAckWakeLock) {
                        this.mAckWakeLock.acquire();
                        this.mAckWlSequenceNum++;
                        msg = this.mRilHandler.obtainMessage(4);
                        msg.arg1 = this.mAckWlSequenceNum;
                        this.mRilHandler.sendMessageDelayed(msg, (long) this.mAckWakeLockTimeout);
                    }
                default:
                    Rlog.w(RILJ_LOG_TAG, "Acquiring Invalid Wakelock type " + wakeLockType);
                    return;
            }
            rr.mWakeLockType = wakeLockType;
        }
    }

    private void decrementWakeLock(RILRequest rr) {
        int i = 0;
        synchronized (rr) {
            switch (rr.mWakeLockType) {
                case -1:
                case 1:
                    break;
                case 0:
                    synchronized (this.mWakeLock) {
                        ClientWakelockTracker clientWakelockTracker = this.mClientWakelockTracker;
                        String str = rr.mClientId;
                        int i2 = rr.mRequest;
                        int i3 = rr.mSerial;
                        if (this.mWakeLockCount > 1) {
                            i = this.mWakeLockCount - 1;
                        }
                        clientWakelockTracker.stopTracking(str, i2, i3, i);
                        if (!(this.mClientWakelockTracker.isClientActive(getWorkSourceClientId(rr.mWorkSource)) || this.mActiveWakelockWorkSource == null)) {
                            this.mActiveWakelockWorkSource.remove(rr.mWorkSource);
                            if (this.mActiveWakelockWorkSource.size() == 0) {
                                this.mActiveWakelockWorkSource = null;
                            }
                            this.mWakeLock.setWorkSource(this.mActiveWakelockWorkSource);
                        }
                        if (this.mWakeLockCount > 1) {
                            this.mWakeLockCount--;
                        } else {
                            this.mWakeLockCount = 0;
                            this.mWakeLock.release();
                        }
                    }
                default:
                    Rlog.w(RILJ_LOG_TAG, "Decrementing Invalid Wakelock type " + rr.mWakeLockType);
                    break;
            }
            rr.mWakeLockType = -1;
        }
    }

    private boolean clearWakeLock(int wakeLockType) {
        if (wakeLockType == 0) {
            synchronized (this.mWakeLock) {
                if (this.mWakeLockCount != 0 || (this.mWakeLock.isHeld() ^ 1) == 0) {
                    Rlog.d(RILJ_LOG_TAG, "NOTE: mWakeLockCount is " + this.mWakeLockCount + "at time of clearing");
                    this.mWakeLockCount = 0;
                    this.mWakeLock.release();
                    this.mClientWakelockTracker.stopTrackingAll();
                    this.mActiveWakelockWorkSource = null;
                    return true;
                }
                return false;
            }
        }
        synchronized (this.mAckWakeLock) {
            if (this.mAckWakeLock.isHeld()) {
                this.mAckWakeLock.release();
                return true;
            }
            return false;
        }
    }

    private void clearRequestList(int error, boolean loggable) {
        synchronized (this.mRequestList) {
            int count = this.mRequestList.size();
            if (loggable) {
                Rlog.d(RILJ_LOG_TAG, "clearRequestList  mWakeLockCount=" + this.mWakeLockCount + " mRequestList=" + count);
            }
            for (int i = 0; i < count; i++) {
                RILRequest rr = (RILRequest) this.mRequestList.valueAt(i);
                if (loggable) {
                    Rlog.d(RILJ_LOG_TAG, i + ": [" + rr.mSerial + "] " + requestToString(rr.mRequest));
                }
                rr.onError(error, null);
                decrementWakeLock(rr);
                rr.release();
            }
            this.mRequestList.clear();
        }
    }

    private RILRequest findAndRemoveRequestFromList(int serial) {
        RILRequest rr;
        synchronized (this.mRequestList) {
            rr = (RILRequest) this.mRequestList.get(serial);
            if (rr != null) {
                this.mRequestList.remove(serial);
            }
        }
        return rr;
    }

    private void addToRilHistogram(RILRequest rr) {
        int totalTime = (int) (SystemClock.elapsedRealtime() - rr.mStartTimeMs);
        synchronized (mRilTimeHistograms) {
            TelephonyHistogram entry = (TelephonyHistogram) mRilTimeHistograms.get(rr.mRequest);
            if (entry == null) {
                entry = new TelephonyHistogram(1, rr.mRequest, 5);
                mRilTimeHistograms.put(rr.mRequest, entry);
            }
            entry.addTimeTaken(totalTime);
        }
    }

    RadioCapability makeStaticRadioCapability() {
        int raf = 1;
        String rafString = this.mContext.getResources().getString(17039709);
        if (!TextUtils.isEmpty(rafString)) {
            raf = RadioAccessFamily.rafTypeFromString(rafString);
        }
        RadioCapability rc = new RadioCapability(this.mPhoneId.intValue(), 0, 0, raf, SpnOverride.MVNO_TYPE_NONE, 1);
        riljLog("Faking RIL_REQUEST_GET_RADIO_CAPABILITY response using " + raf);
        return rc;
    }

    static String retToString(int req, Object ret) {
        if (ret == null) {
            return SpnOverride.MVNO_TYPE_NONE;
        }
        switch (req) {
            case 11:
            case 38:
            case 39:
            case 115:
            case 117:
                return SpnOverride.MVNO_TYPE_NONE;
            default:
                String s;
                int length;
                StringBuilder stringBuilder;
                int i;
                int i2;
                if (ret instanceof int[]) {
                    int[] intArray = (int[]) ret;
                    length = intArray.length;
                    stringBuilder = new StringBuilder("{");
                    if (length > 0) {
                        stringBuilder.append(intArray[0]);
                        i = 1;
                        while (i < length) {
                            i2 = i + 1;
                            stringBuilder.append(", ").append(intArray[i]);
                            i = i2;
                        }
                    }
                    stringBuilder.append("}");
                    s = stringBuilder.toString();
                } else if (ret instanceof String[]) {
                    String[] strings = (String[]) ret;
                    length = strings.length;
                    stringBuilder = new StringBuilder("{");
                    if (length > 0) {
                        stringBuilder.append(strings[0]);
                        i = 1;
                        while (i < length) {
                            i2 = i + 1;
                            stringBuilder.append(", ").append(strings[i]);
                            i = i2;
                        }
                    }
                    stringBuilder.append("}");
                    s = stringBuilder.toString();
                } else if (req == 9) {
                    ArrayList<DriverCall> calls = (ArrayList) ret;
                    stringBuilder = new StringBuilder("{");
                    for (DriverCall dc : calls) {
                        stringBuilder.append("[").append(dc).append("] ");
                    }
                    stringBuilder.append("}");
                    s = stringBuilder.toString();
                } else if (req == 75) {
                    ArrayList<NeighboringCellInfo> cells = (ArrayList) ret;
                    stringBuilder = new StringBuilder("{");
                    for (NeighboringCellInfo cell : cells) {
                        stringBuilder.append("[").append(cell).append("] ");
                    }
                    stringBuilder.append("}");
                    s = stringBuilder.toString();
                } else if (req == 33) {
                    stringBuilder = new StringBuilder("{");
                    for (Object append : (CallForwardInfo[]) ret) {
                        stringBuilder.append("[").append(append).append("] ");
                    }
                    stringBuilder.append("}");
                    s = stringBuilder.toString();
                } else if (req == 124) {
                    ArrayList<HardwareConfig> hwcfgs = (ArrayList) ret;
                    stringBuilder = new StringBuilder(" ");
                    for (HardwareConfig hwcfg : hwcfgs) {
                        stringBuilder.append("[").append(hwcfg).append("] ");
                    }
                    s = stringBuilder.toString();
                } else {
                    s = ret.toString();
                }
                return s;
        }
    }

    void writeMetricsNewSms(int tech, int format) {
        this.mMetrics.writeRilNewSms(this.mPhoneId.intValue(), tech, format);
    }

    void writeMetricsCallRing(char[] response) {
        this.mMetrics.writeRilCallRing(this.mPhoneId.intValue(), response);
    }

    void writeMetricsSrvcc(int state) {
        this.mMetrics.writeRilSrvcc(this.mPhoneId.intValue(), state);
    }

    void writeMetricsModemRestartEvent(String reason) {
        this.mMetrics.writeModemRestartEvent(this.mPhoneId.intValue(), reason);
    }

    void notifyRegistrantsRilConnectionChanged(int rilVer) {
        this.mRilVersion = rilVer;
        if (this.mRilConnectedRegistrants != null) {
            this.mRilConnectedRegistrants.notifyRegistrants(new AsyncResult(null, new Integer(rilVer), null));
        }
    }

    void notifyRegistrantsCdmaInfoRec(CdmaInformationRecords infoRec) {
        if (infoRec.record instanceof CdmaDisplayInfoRec) {
            if (this.mDisplayInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mDisplayInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if (infoRec.record instanceof CdmaSignalInfoRec) {
            if (this.mSignalInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mSignalInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if (infoRec.record instanceof CdmaNumberInfoRec) {
            if (this.mNumberInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mNumberInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if (infoRec.record instanceof CdmaRedirectingNumberInfoRec) {
            if (this.mRedirNumInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mRedirNumInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if (infoRec.record instanceof CdmaLineControlInfoRec) {
            if (this.mLineControlInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mLineControlInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if (infoRec.record instanceof CdmaT53ClirInfoRec) {
            if (this.mT53ClirInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mT53ClirInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if ((infoRec.record instanceof CdmaT53AudioControlInfoRec) && this.mT53AudCntrlInfoRegistrants != null) {
            unsljLogRet(1027, infoRec.record);
            this.mT53AudCntrlInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
        }
    }

    static String requestToString(int request) {
        OemConstant.printStack("requestToString");
        switch (request) {
            case 1:
                return "GET_SIM_STATUS";
            case 2:
                return "ENTER_SIM_PIN";
            case 3:
                return "ENTER_SIM_PUK";
            case 4:
                return "ENTER_SIM_PIN2";
            case 5:
                return "ENTER_SIM_PUK2";
            case 6:
                return "CHANGE_SIM_PIN";
            case 7:
                return "CHANGE_SIM_PIN2";
            case 8:
                return "ENTER_NETWORK_DEPERSONALIZATION";
            case 9:
                return "GET_CURRENT_CALLS";
            case 10:
                return "DIAL";
            case 11:
                return "GET_IMSI";
            case 12:
                return "HANGUP";
            case 13:
                return "HANGUP_WAITING_OR_BACKGROUND";
            case 14:
                return "HANGUP_FOREGROUND_RESUME_BACKGROUND";
            case 15:
                return "REQUEST_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE";
            case 16:
                return "CONFERENCE";
            case 17:
                return "UDUB";
            case 18:
                return "LAST_CALL_FAIL_CAUSE";
            case 19:
                return "SIGNAL_STRENGTH";
            case 20:
                return "VOICE_REGISTRATION_STATE";
            case 21:
                return "DATA_REGISTRATION_STATE";
            case 22:
                return "OPERATOR";
            case 23:
                return "RADIO_POWER";
            case 24:
                return "DTMF";
            case 25:
                return "SEND_SMS";
            case 26:
                return "SEND_SMS_EXPECT_MORE";
            case 27:
                return "SETUP_DATA_CALL";
            case 28:
                return "SIM_IO";
            case 29:
                return "SEND_USSD";
            case 30:
                return "CANCEL_USSD";
            case 31:
                return "GET_CLIR";
            case 32:
                return "SET_CLIR";
            case 33:
                return "QUERY_CALL_FORWARD_STATUS";
            case 34:
                return "SET_CALL_FORWARD";
            case 35:
                return "QUERY_CALL_WAITING";
            case 36:
                return "SET_CALL_WAITING";
            case 37:
                return "SMS_ACKNOWLEDGE";
            case 38:
                return "GET_IMEI";
            case 39:
                return "GET_IMEISV";
            case 40:
                return "ANSWER";
            case 41:
                return "DEACTIVATE_DATA_CALL";
            case 42:
                return "QUERY_FACILITY_LOCK";
            case 43:
                return "SET_FACILITY_LOCK";
            case 44:
                return "CHANGE_BARRING_PASSWORD";
            case 45:
                return "QUERY_NETWORK_SELECTION_MODE";
            case 46:
                return "SET_NETWORK_SELECTION_AUTOMATIC";
            case 47:
                return "SET_NETWORK_SELECTION_MANUAL";
            case 48:
                return "QUERY_AVAILABLE_NETWORKS ";
            case 49:
                return "DTMF_START";
            case 50:
                return "DTMF_STOP";
            case 51:
                return "BASEBAND_VERSION";
            case 52:
                return "SEPARATE_CONNECTION";
            case 53:
                return "SET_MUTE";
            case 54:
                return "GET_MUTE";
            case 55:
                return "QUERY_CLIP";
            case 56:
                return "LAST_DATA_CALL_FAIL_CAUSE";
            case 57:
                return "DATA_CALL_LIST";
            case 58:
                return "RESET_RADIO";
            case 59:
                return "OEM_HOOK_RAW";
            case RadioError.NETWORK_NOT_READY /*60*/:
                return "OEM_HOOK_STRINGS";
            case 61:
                return "SCREEN_STATE";
            case 62:
                return "SET_SUPP_SVC_NOTIFICATION";
            case 63:
                return "WRITE_SMS_TO_SIM";
            case 64:
                return "DELETE_SMS_ON_SIM";
            case 65:
                return "SET_BAND_MODE";
            case 66:
                return "QUERY_AVAILABLE_BAND_MODE";
            case SYS_OEM_NW_DIAG_CAUSE_MCFG_ICCID_FAILED /*67*/:
                return "REQUEST_STK_GET_PROFILE";
            case 68:
                return "REQUEST_STK_SET_PROFILE";
            case 69:
                return "REQUEST_STK_SEND_ENVELOPE_COMMAND";
            case 70:
                return "REQUEST_STK_SEND_TERMINAL_RESPONSE";
            case 71:
                return "REQUEST_STK_HANDLE_CALL_SETUP_REQUESTED_FROM_SIM";
            case 72:
                return "REQUEST_EXPLICIT_CALL_TRANSFER";
            case 73:
                return "REQUEST_SET_PREFERRED_NETWORK_TYPE";
            case 74:
                return "REQUEST_GET_PREFERRED_NETWORK_TYPE";
            case 75:
                return "REQUEST_GET_NEIGHBORING_CELL_IDS";
            case 76:
                return "REQUEST_SET_LOCATION_UPDATES";
            case 77:
                return "RIL_REQUEST_CDMA_SET_SUBSCRIPTION_SOURCE";
            case 78:
                return "RIL_REQUEST_CDMA_SET_ROAMING_PREFERENCE";
            case 79:
                return "RIL_REQUEST_CDMA_QUERY_ROAMING_PREFERENCE";
            case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /*80*/:
                return "RIL_REQUEST_SET_TTY_MODE";
            case 81:
                return "RIL_REQUEST_QUERY_TTY_MODE";
            case RadioNVItems.RIL_NV_LTE_BSR_MAX_TIME /*82*/:
                return "RIL_REQUEST_CDMA_SET_PREFERRED_VOICE_PRIVACY_MODE";
            case 83:
                return "RIL_REQUEST_CDMA_QUERY_PREFERRED_VOICE_PRIVACY_MODE";
            case 84:
                return "RIL_REQUEST_CDMA_FLASH";
            case 85:
                return "RIL_REQUEST_CDMA_BURST_DTMF";
            case 86:
                return "RIL_REQUEST_CDMA_VALIDATE_AND_WRITE_AKEY";
            case 87:
                return "RIL_REQUEST_CDMA_SEND_SMS";
            case 88:
                return "RIL_REQUEST_CDMA_SMS_ACKNOWLEDGE";
            case 89:
                return "RIL_REQUEST_GSM_GET_BROADCAST_CONFIG";
            case 90:
                return "RIL_REQUEST_GSM_SET_BROADCAST_CONFIG";
            case 91:
                return "RIL_REQUEST_GSM_BROADCAST_ACTIVATION";
            case 92:
                return "RIL_REQUEST_CDMA_GET_BROADCAST_CONFIG";
            case 93:
                return "RIL_REQUEST_CDMA_SET_BROADCAST_CONFIG";
            case 94:
                return "RIL_REQUEST_CDMA_BROADCAST_ACTIVATION";
            case 95:
                return "RIL_REQUEST_CDMA_SUBSCRIPTION";
            case 96:
                return "RIL_REQUEST_CDMA_WRITE_SMS_TO_RUIM";
            case 97:
                return "RIL_REQUEST_CDMA_DELETE_SMS_ON_RUIM";
            case 98:
                return "RIL_REQUEST_DEVICE_IDENTITY";
            case 99:
                return "REQUEST_EXIT_EMERGENCY_CALLBACK_MODE";
            case 100:
                return "RIL_REQUEST_GET_SMSC_ADDRESS";
            case 101:
                return "RIL_REQUEST_SET_SMSC_ADDRESS";
            case 102:
                return "RIL_REQUEST_REPORT_SMS_MEMORY_STATUS";
            case 103:
                return "RIL_REQUEST_REPORT_STK_SERVICE_IS_RUNNING";
            case 104:
                return "RIL_REQUEST_CDMA_GET_SUBSCRIPTION_SOURCE";
            case 105:
                return "RIL_REQUEST_ISIM_AUTHENTICATION";
            case 106:
                return "RIL_REQUEST_ACKNOWLEDGE_INCOMING_GSM_SMS_WITH_PDU";
            case 107:
                return "RIL_REQUEST_STK_SEND_ENVELOPE_WITH_STATUS";
            case 108:
                return "RIL_REQUEST_VOICE_RADIO_TECH";
            case 109:
                return "RIL_REQUEST_GET_CELL_INFO_LIST";
            case 110:
                return "RIL_REQUEST_SET_CELL_INFO_LIST_RATE";
            case 111:
                return "RIL_REQUEST_SET_INITIAL_ATTACH_APN";
            case 112:
                return "RIL_REQUEST_IMS_REGISTRATION_STATE";
            case 113:
                return "RIL_REQUEST_IMS_SEND_SMS";
            case 114:
                return "RIL_REQUEST_SIM_TRANSMIT_APDU_BASIC";
            case 115:
                return "RIL_REQUEST_SIM_OPEN_CHANNEL";
            case 116:
                return "RIL_REQUEST_SIM_CLOSE_CHANNEL";
            case 117:
                return "RIL_REQUEST_SIM_TRANSMIT_APDU_CHANNEL";
            case 118:
                return "RIL_REQUEST_NV_READ_ITEM";
            case 119:
                return "RIL_REQUEST_NV_WRITE_ITEM";
            case 120:
                return "RIL_REQUEST_NV_WRITE_CDMA_PRL";
            case 121:
                return "RIL_REQUEST_NV_RESET_CONFIG";
            case 122:
                return "RIL_REQUEST_SET_UICC_SUBSCRIPTION";
            case 123:
                return "RIL_REQUEST_ALLOW_DATA";
            case 124:
                return "GET_HARDWARE_CONFIG";
            case 125:
                return "RIL_REQUEST_SIM_AUTHENTICATION";
            case 128:
                return "RIL_REQUEST_SET_DATA_PROFILE";
            case 129:
                return "RIL_REQUEST_SHUTDOWN";
            case 130:
                return "RIL_REQUEST_GET_RADIO_CAPABILITY";
            case 131:
                return "RIL_REQUEST_SET_RADIO_CAPABILITY";
            case 132:
                return "RIL_REQUEST_START_LCE";
            case 133:
                return "RIL_REQUEST_STOP_LCE";
            case 134:
                return "RIL_REQUEST_PULL_LCEDATA";
            case 135:
                return "RIL_REQUEST_GET_ACTIVITY_INFO";
            case 136:
                return "RIL_REQUEST_SET_ALLOWED_CARRIERS";
            case 137:
                return "RIL_REQUEST_GET_ALLOWED_CARRIERS";
            case 138:
                return "RIL_REQUEST_SEND_DEVICE_STATE";
            case 139:
                return "RIL_REQUEST_SET_UNSOLICITED_RESPONSE_FILTER";
            case 140:
                return "RIL_REQUEST_SET_SIM_CARD_POWER";
            case 141:
                return "RIL_REQUEST_SET_CARRIER_INFO_IMSI_ENCRYPTION";
            case 142:
                return "RIL_REQUEST_START_NETWORK_SCAN";
            case 143:
                return "RIL_REQUEST_STOP_NETWORK_SCAN";
            case 200:
                return "RIL_REQUEST_SIM_QUERY_ATR";
            case 800:
                return "RIL_RESPONSE_ACKNOWLEDGEMENT";
            default:
                return "<unknown request>";
        }
    }

    static String responseToString(int request) {
        switch (request) {
            case 1000:
                return "UNSOL_RESPONSE_RADIO_STATE_CHANGED";
            case 1001:
                return "UNSOL_RESPONSE_CALL_STATE_CHANGED";
            case 1002:
                return "UNSOL_RESPONSE_NETWORK_STATE_CHANGED";
            case 1003:
                return "UNSOL_RESPONSE_NEW_SMS";
            case 1004:
                return "UNSOL_RESPONSE_NEW_SMS_STATUS_REPORT";
            case 1005:
                return "UNSOL_RESPONSE_NEW_SMS_ON_SIM";
            case 1006:
                return "UNSOL_ON_USSD";
            case 1007:
                return "UNSOL_ON_USSD_REQUEST";
            case 1008:
                return "UNSOL_NITZ_TIME_RECEIVED";
            case 1009:
                return "UNSOL_SIGNAL_STRENGTH";
            case 1010:
                return "UNSOL_DATA_CALL_LIST_CHANGED";
            case 1011:
                return "UNSOL_SUPP_SVC_NOTIFICATION";
            case 1012:
                return "UNSOL_STK_SESSION_END";
            case 1013:
                return "UNSOL_STK_PROACTIVE_COMMAND";
            case 1014:
                return "UNSOL_STK_EVENT_NOTIFY";
            case CharacterSets.UTF_16 /*1015*/:
                return "UNSOL_STK_CALL_SETUP";
            case 1016:
                return "UNSOL_SIM_SMS_STORAGE_FULL";
            case 1017:
                return "UNSOL_SIM_REFRESH";
            case 1018:
                return "UNSOL_CALL_RING";
            case 1019:
                return "UNSOL_RESPONSE_SIM_STATUS_CHANGED";
            case 1020:
                return "UNSOL_RESPONSE_CDMA_NEW_SMS";
            case 1021:
                return "UNSOL_RESPONSE_NEW_BROADCAST_SMS";
            case 1022:
                return "UNSOL_CDMA_RUIM_SMS_STORAGE_FULL";
            case ApnTypes.ALL /*1023*/:
                return "UNSOL_RESTRICTED_STATE_CHANGED";
            case android.hardware.radio.V1_0.RadioAccessFamily.HSUPA /*1024*/:
                return "UNSOL_ENTER_EMERGENCY_CALLBACK_MODE";
            case 1025:
                return "UNSOL_CDMA_CALL_WAITING";
            case 1026:
                return "UNSOL_CDMA_OTA_PROVISION_STATUS";
            case 1027:
                return "UNSOL_CDMA_INFO_REC";
            case 1028:
                return "UNSOL_OEM_HOOK_RAW";
            case 1029:
                return "UNSOL_RINGBACK_TONE";
            case 1030:
                return "UNSOL_RESEND_INCALL_MUTE";
            case 1031:
                return "CDMA_SUBSCRIPTION_SOURCE_CHANGED";
            case 1032:
                return "UNSOL_CDMA_PRL_CHANGED";
            case 1033:
                return "UNSOL_EXIT_EMERGENCY_CALLBACK_MODE";
            case 1034:
                return "UNSOL_RIL_CONNECTED";
            case 1035:
                return "UNSOL_VOICE_RADIO_TECH_CHANGED";
            case 1036:
                return "UNSOL_CELL_INFO_LIST";
            case 1037:
                return "UNSOL_RESPONSE_IMS_NETWORK_STATE_CHANGED";
            case 1038:
                return "RIL_UNSOL_UICC_SUBSCRIPTION_STATUS_CHANGED";
            case 1039:
                return "UNSOL_SRVCC_STATE_NOTIFY";
            case 1040:
                return "RIL_UNSOL_HARDWARE_CONFIG_CHANGED";
            case 1042:
                return "RIL_UNSOL_RADIO_CAPABILITY";
            case 1043:
                return "UNSOL_ON_SS";
            case 1044:
                return "UNSOL_STK_CC_ALPHA_NOTIFY";
            case 1045:
                return "UNSOL_LCE_INFO_RECV";
            case 1046:
                return "UNSOL_PCO_DATA";
            case 1047:
                return "UNSOL_MODEM_RESTART";
            case 1048:
                return "RIL_UNSOL_CARRIER_INFO_IMSI_ENCRYPTION";
            case 1049:
                return "RIL_UNSOL_NETWORK_SCAN_RESULT";
            default:
                return "<unknown response>";
        }
    }

    void riljLog(String msg) {
        Rlog.d(RILJ_LOG_TAG, msg + (this.mPhoneId != null ? " [SUB" + this.mPhoneId + "]" : SpnOverride.MVNO_TYPE_NONE));
    }

    void riljLoge(String msg) {
        Rlog.e(RILJ_LOG_TAG, msg + (this.mPhoneId != null ? " [SUB" + this.mPhoneId + "]" : SpnOverride.MVNO_TYPE_NONE));
    }

    void riljLoge(String msg, Exception e) {
        Rlog.e(RILJ_LOG_TAG, msg + (this.mPhoneId != null ? " [SUB" + this.mPhoneId + "]" : SpnOverride.MVNO_TYPE_NONE), e);
    }

    void riljLogv(String msg) {
        Rlog.v(RILJ_LOG_TAG, msg + (this.mPhoneId != null ? " [SUB" + this.mPhoneId + "]" : SpnOverride.MVNO_TYPE_NONE));
    }

    void unsljLog(int response) {
        riljLog("[UNSL]< " + responseToString(response));
        countUnsolMsg(response);
    }

    void unsljLogMore(int response, String more) {
        riljLog("[UNSL]< " + responseToString(response) + " " + more);
        countUnsolMsg(response);
    }

    void unsljLogRet(int response, Object ret) {
        riljLog("[UNSL]< " + responseToString(response) + " " + retToString(response, ret));
        countUnsolMsg(response);
    }

    void unsljLogvRet(int response, Object ret) {
        riljLogv("[UNSL]< " + responseToString(response) + " " + retToString(response, ret));
        countUnsolMsg(response);
    }

    public void setPhoneType(int phoneType) {
        riljLog("setPhoneType=" + phoneType + " old value=" + this.mPhoneType);
        this.mPhoneType = phoneType;
    }

    public void testingEmergencyCall() {
        riljLog("testingEmergencyCall");
        this.mTestingEmergencyCall.set(true);
    }

    /* JADX WARNING: Unexpected end of synchronized block */
    /* JADX WARNING: Missing block: B:6:?, code:
            r9.println(" mWakeLockCount=" + r7.mWakeLockCount);
     */
    /* JADX WARNING: Missing block: B:9:0x0069, code:
            r0 = r7.mRequestList.size();
            r9.println(" mRequestList count=" + r0);
            r1 = 0;
     */
    /* JADX WARNING: Missing block: B:10:0x0087, code:
            if (r1 >= r0) goto L_0x00c4;
     */
    /* JADX WARNING: Missing block: B:11:0x0089, code:
            r2 = (com.android.internal.telephony.RILRequest) r7.mRequestList.valueAt(r1);
            r9.println("  [" + r2.mSerial + "] " + requestToString(r2.mRequest));
            r1 = r1 + 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("RIL: " + this);
        pw.println(" mWakeLock=" + this.mWakeLock);
        pw.println(" mWakeLockTimeout=" + this.mWakeLockTimeout);
        synchronized (this.mRequestList) {
            synchronized (this.mWakeLock) {
            }
        }
        pw.println(" mLastNITZTimeInfo=" + Arrays.toString(this.mLastNITZTimeInfo));
        pw.println(" mTestingEmergencyCall=" + this.mTestingEmergencyCall.get());
        this.mClientWakelockTracker.dumpClientRequestTracker(pw);
    }

    public List<ClientRequestStats> getClientRequestStats() {
        return this.mClientWakelockTracker.getClientRequestStats();
    }

    public static ArrayList<Byte> primitiveArrayToArrayList(byte[] arr) {
        ArrayList<Byte> arrayList = new ArrayList(arr.length);
        for (byte b : arr) {
            arrayList.add(Byte.valueOf(b));
        }
        return arrayList;
    }

    public static byte[] arrayListToPrimitiveArray(ArrayList<Byte> bytes) {
        byte[] ret = new byte[bytes.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = ((Byte) bytes.get(i)).byteValue();
        }
        return ret;
    }

    static ArrayList<HardwareConfig> convertHalHwConfigList(ArrayList<HardwareConfig> hwListRil, RIL ril) {
        ArrayList<HardwareConfig> response = new ArrayList(hwListRil.size());
        for (HardwareConfig hwRil : hwListRil) {
            HardwareConfig hw;
            int type = hwRil.type;
            switch (type) {
                case 0:
                    hw = new HardwareConfig(type);
                    HardwareConfigModem hwModem = (HardwareConfigModem) hwRil.modem.get(0);
                    hw.assignModem(hwRil.uuid, hwRil.state, hwModem.rilModel, hwModem.rat, hwModem.maxVoice, hwModem.maxData, hwModem.maxStandby);
                    break;
                case 1:
                    hw = new HardwareConfig(type);
                    hw.assignSim(hwRil.uuid, hwRil.state, ((HardwareConfigSim) hwRil.sim.get(0)).modemUuid);
                    break;
                default:
                    throw new RuntimeException("RIL_REQUEST_GET_HARDWARE_CONFIG invalid hardward type:" + type);
            }
            response.add(hw);
        }
        return response;
    }

    static RadioCapability convertHalRadioCapability(RadioCapability rcRil, RIL ril) {
        int session = rcRil.session;
        int phase = rcRil.phase;
        int rat = rcRil.raf;
        String logicModemUuid = rcRil.logicalModemUuid;
        int status = rcRil.status;
        ril.riljLog("convertHalRadioCapability: session=" + session + ", phase=" + phase + ", rat=" + rat + ", logicModemUuid=" + logicModemUuid + ", status=" + status);
        return new RadioCapability(ril.mPhoneId.intValue(), session, phase, rat, logicModemUuid, status);
    }

    static ArrayList<Integer> convertHalLceData(LceDataInfo lce, RIL ril) {
        ArrayList<Integer> capacityResponse = new ArrayList();
        int capacityDownKbps = lce.lastHopCapacityKbps;
        int confidenceLevel = Byte.toUnsignedInt(lce.confidenceLevel);
        int lceSuspended = lce.lceSuspended ? 1 : 0;
        ril.riljLog("LCE capacity information received: capacity=" + capacityDownKbps + " confidence=" + confidenceLevel + " lceSuspended=" + lceSuspended);
        capacityResponse.add(Integer.valueOf(capacityDownKbps));
        capacityResponse.add(Integer.valueOf(confidenceLevel));
        capacityResponse.add(Integer.valueOf(lceSuspended));
        return capacityResponse;
    }

    static ArrayList<CellInfo> convertHalCellInfoList(ArrayList<android.hardware.radio.V1_0.CellInfo> records) {
        ArrayList<CellInfo> response = new ArrayList(records.size());
        for (android.hardware.radio.V1_0.CellInfo record : records) {
            Parcel p = Parcel.obtain();
            p.writeInt(record.cellInfoType);
            p.writeInt(record.registered ? 1 : 0);
            p.writeInt(record.timeStampType);
            p.writeLong(record.timeStamp);
            switch (record.cellInfoType) {
                case 1:
                    CellInfoGsm cellInfoGsm = (CellInfoGsm) record.gsm.get(0);
                    p.writeInt(Integer.parseInt(cellInfoGsm.cellIdentityGsm.mcc));
                    p.writeInt(Integer.parseInt(cellInfoGsm.cellIdentityGsm.mnc));
                    p.writeInt(cellInfoGsm.cellIdentityGsm.lac);
                    p.writeInt(cellInfoGsm.cellIdentityGsm.cid);
                    p.writeInt(cellInfoGsm.cellIdentityGsm.arfcn);
                    p.writeInt(Byte.toUnsignedInt(cellInfoGsm.cellIdentityGsm.bsic));
                    p.writeInt(cellInfoGsm.signalStrengthGsm.signalStrength);
                    p.writeInt(cellInfoGsm.signalStrengthGsm.bitErrorRate);
                    p.writeInt(cellInfoGsm.signalStrengthGsm.timingAdvance);
                    break;
                case 2:
                    CellInfoCdma cellInfoCdma = (CellInfoCdma) record.cdma.get(0);
                    p.writeInt(cellInfoCdma.cellIdentityCdma.networkId);
                    p.writeInt(cellInfoCdma.cellIdentityCdma.systemId);
                    p.writeInt(cellInfoCdma.cellIdentityCdma.baseStationId);
                    p.writeInt(cellInfoCdma.cellIdentityCdma.longitude);
                    p.writeInt(cellInfoCdma.cellIdentityCdma.latitude);
                    p.writeInt(cellInfoCdma.signalStrengthCdma.dbm);
                    p.writeInt(cellInfoCdma.signalStrengthCdma.ecio);
                    p.writeInt(cellInfoCdma.signalStrengthEvdo.dbm);
                    p.writeInt(cellInfoCdma.signalStrengthEvdo.ecio);
                    p.writeInt(cellInfoCdma.signalStrengthEvdo.signalNoiseRatio);
                    break;
                case 3:
                    CellInfoLte cellInfoLte = (CellInfoLte) record.lte.get(0);
                    p.writeInt(Integer.parseInt(cellInfoLte.cellIdentityLte.mcc));
                    p.writeInt(Integer.parseInt(cellInfoLte.cellIdentityLte.mnc));
                    p.writeInt(cellInfoLte.cellIdentityLte.ci);
                    p.writeInt(cellInfoLte.cellIdentityLte.pci);
                    p.writeInt(cellInfoLte.cellIdentityLte.tac);
                    p.writeInt(cellInfoLte.cellIdentityLte.earfcn);
                    p.writeInt(cellInfoLte.signalStrengthLte.signalStrength);
                    p.writeInt(cellInfoLte.signalStrengthLte.rsrp);
                    p.writeInt(cellInfoLte.signalStrengthLte.rsrq);
                    p.writeInt(cellInfoLte.signalStrengthLte.rssnr);
                    p.writeInt(cellInfoLte.signalStrengthLte.cqi);
                    p.writeInt(cellInfoLte.signalStrengthLte.timingAdvance);
                    break;
                case 4:
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) record.wcdma.get(0);
                    p.writeInt(Integer.parseInt(cellInfoWcdma.cellIdentityWcdma.mcc));
                    p.writeInt(Integer.parseInt(cellInfoWcdma.cellIdentityWcdma.mnc));
                    p.writeInt(cellInfoWcdma.cellIdentityWcdma.lac);
                    p.writeInt(cellInfoWcdma.cellIdentityWcdma.cid);
                    p.writeInt(cellInfoWcdma.cellIdentityWcdma.psc);
                    p.writeInt(cellInfoWcdma.cellIdentityWcdma.uarfcn);
                    p.writeInt(cellInfoWcdma.signalStrengthWcdma.signalStrength);
                    p.writeInt(cellInfoWcdma.signalStrengthWcdma.bitErrorRate);
                    break;
                default:
                    throw new RuntimeException("unexpected cellinfotype: " + record.cellInfoType);
            }
            p.setDataPosition(0);
            CellInfo InfoRec = (CellInfo) CellInfo.CREATOR.createFromParcel(p);
            p.recycle();
            response.add(InfoRec);
        }
        return response;
    }

    static SignalStrength convertHalSignalStrength(android.hardware.radio.V1_0.SignalStrength signalStrength) {
        return new SignalStrength(signalStrength.gw.signalStrength, signalStrength.gw.bitErrorRate, signalStrength.cdma.dbm, signalStrength.cdma.ecio, signalStrength.evdo.dbm, signalStrength.evdo.ecio, signalStrength.evdo.signalNoiseRatio, signalStrength.lte.signalStrength, signalStrength.lte.rsrp, signalStrength.lte.rsrq, signalStrength.lte.rssnr, signalStrength.lte.cqi, signalStrength.tdScdma.rscp, false);
    }

    private String oppoGetStringFromType(int type) {
        switch (type) {
            case 10:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MO_DROP;
            case 11:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RACH;
            case 12:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RLF;
            case 13:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_PCH;
            case 14:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_CSFB;
            case 15:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_REJECT;
            case 16:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RRC;
            case 19:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_PAGE_FAIL;
            case 20:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_DISC;
            case RadioError.NETWORK_NOT_READY /*60*/:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_AS_FAILED;
            case 61:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_REG_REJECT;
            case 62:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_REG_WITHOUT_LTE;
            case 63:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_REJECT;
            case 64:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_AUTHENTICATION_REJECT;
            case 65:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_AS_FAILED;
            case 66:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_GSM_T3126_EXPIRED;
            case SYS_OEM_NW_DIAG_CAUSE_MCFG_ICCID_FAILED /*67*/:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MCFG_ICCID_FAILED;
            case 70:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_FAKE_BS;
            case 71:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_FAKE_BS_ONLY;
            case 72:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_RAT;
            case 73:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_MCC;
            case 74:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_REQ_MNC;
            case 75:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_ON;
            case 110:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_NOT_ALLOWED;
            case 111:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_NO_AVAILABLE_APN;
            case 112:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_SET_UP_DATA_ERROR;
            case 160:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_RX_BREAK;
            case 161:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_TIME_OUT;
            case 210:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_RF_MIPI_HW_FAILED;
            case 211:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_RFFE_MISSING_NONFATAL;
            default:
                return SpnOverride.MVNO_TYPE_NONE;
        }
    }

    public Object oppoProcessUnsolOemKeyLogErrMsg(Bundle b) {
        String msg = null;
        int type = b.getInt(Calls.TYPE);
        int rat = b.getInt("rat");
        int errcode = b.getInt("errcode");
        int is_message = b.getInt("is_message");
        if (1 == is_message) {
            msg = b.getString("message");
        }
        riljLog("Get message, type:" + type + ", rat:" + rat + ", errcode:" + errcode);
        if (type >= 72 && type <= 75 && OppoRegSrvInfo.collectRegSrvInfo(type, rat, errcode)) {
            if (OppoRegSrvInfo.isCollectFinished(rat)) {
                is_message = 1;
                msg = OppoRegSrvInfo.getCollectInfo(rat);
                OppoRegSrvInfo.setCollectReported(rat);
            } else {
                riljLog("OppoRegSrvInfo Collect has not finish ");
                return null;
            }
        }
        String issue = oppoGetStringFromType(type);
        if (!(issue == null || (issue.equals(SpnOverride.MVNO_TYPE_NONE) ^ 1) == 0)) {
            int log_type = -1;
            String log_desc = SpnOverride.MVNO_TYPE_NONE;
            try {
                String[] log_array = this.mContext.getString(this.mContext.getResources().getIdentifier("zz_oppo_critical_log_" + type, "string", "android")).split(",");
                log_type = Integer.valueOf(log_array[0]).intValue();
                log_desc = log_array[1];
            } catch (Exception e) {
                riljLog("Can not get resource of identifier zz_oppo_critical_log_" + type);
            }
            String log = getCellLocation() + ", type:" + type + ", rat:" + rat + ", errcode:" + errcode;
            if (1 == is_message) {
                log = log + ", err_msg:" + msg;
            }
            riljLog("Write log, return:" + OppoManager.writeLogToPartition(log_type, log, "NETWORK", issue, log_desc));
            if (type == 19 || type == 20) {
                OppoModemLogManager.saveModemLogPostBack(this.mContext, SpnOverride.MVNO_TYPE_NONE + log_type, log);
            }
            if (type == 70 && this.mFakeBSArfcn != errcode) {
                Intent intent = new Intent("oppo.intent.action.FAKE_BS_BLOCKED");
                this.mFakeBSArfcn = errcode;
                intent.putExtra("arfcn", this.mFakeBSArfcn);
                this.mContext.sendBroadcast(intent, "oppo.permission.OPPO_COMPONENT_SAFE");
                riljLog("fake BS blocked, arfcn:" + this.mFakeBSArfcn);
            }
        }
        return null;
    }

    public void oppoProcessSolOemKeyLogErrMsg(int request, int error) {
        String log_string;
        int size;
        Rlog.d(RILJ_LOG_TAG, "dial or answer or hangup call failure");
        int log_type = -1;
        String log_desc = SpnOverride.MVNO_TYPE_NONE;
        if (request == 10) {
            try {
                log_string = this.mContext.getString(this.mContext.getResources().getIdentifier("zz_oppo_critical_log_10", "string", "android"));
            } catch (Exception e) {
                riljLog("Can not get resource of identifier zz_oppo_critical_log_17");
            }
        } else {
            log_string = this.mContext.getString(this.mContext.getResources().getIdentifier("zz_oppo_critical_log_17", "string", "android"));
        }
        String[] log_array = log_string.split(",");
        log_type = Integer.valueOf(log_array[0]).intValue();
        log_desc = log_array[1];
        if (request == 10) {
            Rlog.d(RILJ_LOG_TAG, "dial call failure");
            size = OppoManager.writeLogToPartition(log_type, getCellLocation() + ", mo drop cause: dial failure, imscall:false", "NETWORK", ISSUE_SYS_OEM_NW_DIAG_CAUSE_MO_DROP, log_desc);
            OppoModemLogManager.saveModemLogPostBack(this.mContext, SpnOverride.MVNO_TYPE_NONE + log_type, getCellLocation() + ", mo drop cause: dial failure, imscall:false");
        } else {
            size = OppoManager.writeLogToPartition(log_type, getCellLocation() + ", request:" + request + ", error:" + error, "NETWORK", ISSUE_SYS_OEM_NW_ANSWER_HANGUP_FAIL, log_desc);
        }
        riljLog("Write log, return:" + size);
    }

    public String getCellLocation() {
        Phone phone = PhoneFactory.getPhone(this.mPhoneId.intValue());
        String mccMnc = SpnOverride.MVNO_TYPE_NONE;
        String prop = SystemProperties.get("gsm.operator.numeric", SpnOverride.MVNO_TYPE_NONE);
        if (prop != null && prop.length() > 0) {
            String[] values = prop.split(",");
            if (this.mPhoneId.intValue() >= 0 && this.mPhoneId.intValue() < values.length && values[this.mPhoneId.intValue()] != null) {
                mccMnc = values[this.mPhoneId.intValue()];
            }
        }
        int mcc = 0;
        int mnc = 0;
        if (mccMnc != null) {
            try {
                if (mccMnc.length() >= 3) {
                    mcc = Integer.parseInt(mccMnc.substring(0, 3));
                    mnc = Integer.parseInt(mccMnc.substring(3));
                }
            } catch (Exception e) {
                riljLog("couldn't parse mcc/mnc: " + mccMnc);
            }
        }
        if (mcc == 460) {
            if (mnc == 2 || mnc == 7 || mnc == 8) {
                mnc = 0;
            }
            if (mnc == 6 || mnc == 9) {
                mnc = 1;
            }
            if (mnc == 3) {
                mnc = 11;
            }
        }
        String loc = "MCC:" + mcc + ", MNC:" + mnc;
        if (phone != null) {
            CellLocation cell = phone.getCellLocation();
            if (cell instanceof GsmCellLocation) {
                loc = loc + ", LAC:" + ((GsmCellLocation) cell).getLac() + ", CID:" + ((GsmCellLocation) cell).getCid();
            } else if (cell instanceof CdmaCellLocation) {
                loc = loc + ", SID:" + ((CdmaCellLocation) cell).getSystemId() + ", NID:" + ((CdmaCellLocation) cell).getNetworkId() + ", BID:" + ((CdmaCellLocation) cell).getBaseStationId();
            }
            SignalStrength signal = phone.getSignalStrength();
            if (signal != null) {
                loc = loc + ", signalstrength:" + signal.getDbm() + ", signallevel:" + signal.getLevel();
            }
        }
        riljLog("getCellLocation:" + loc);
        return loc;
    }

    public void setFactoryModeNvProcess(int cmd, Message result) {
    }

    public void getBandMode(Message response) {
    }

    public void setFactoryModeModemGPIO(int status, int num, Message result) {
    }

    public void OppoGetRffeDevInfo(int rf_tech, Message response) {
    }

    public void setModemCrash(Message response) {
    }

    public void OppoGetMdmBaseBand(Message response) {
    }

    public void oppoGetRadioInfo(Message response) {
    }

    public void oppoSetFilterArfcn(int arfcn1, int arfcn2, Message result) {
    }

    public void oppoLockGSMArfcn(int arfcn1, Message result) {
    }

    public void oppoLockLteCell(int arfcn, int pci, Message result) {
    }

    public void oppoUpdatePplmnList(byte[] value, Message response) {
    }

    public void oppoUpdateFakeBsWeight(int[] values, Message response) {
    }

    public void oppoUpdateVolteFr2(int flags, int rsrp_thresh, int fr2_rsrp, int rsrp_adj, Message response) {
    }

    public void oppoCtlModemFeature(int[] values, Message respnse) {
    }

    public void oppoNoticeUpdateVolteFr(int flags, Message response) {
    }

    public void OppoSetTddLte(int status, Message response) {
    }

    public void OppoExpSetRegionForRilEcclist(Message response) {
    }

    public void oppoGetTxRxInfo(int sys_mode, Message response) {
    }

    public void oppoRffeCmd(int[] rffe_params, Message response) {
    }

    public void oppoGetEsnChangeFlag(Message result) {
    }

    public void oppoResetSwitchDssState(DataCallResponse ret) {
        if (ret == null || ret.status != 0) {
            riljLog("oppoResetSwitchDssState SETUP_DATA_CALL return error");
            return;
        }
        riljLog("oppoResetSwitchDssState SETUP_DATA_CALL Completed,SwitchingDssState set to false");
        SubscriptionController.getInstance().setSwitchingDssState(0, false);
        SubscriptionController.getInstance().setSwitchingDssState(1, false);
    }

    private void countRilRequestMsg(int request) {
        if (OemConstant.getPowerCenterEnable(this.mContext) && mRilCount != null && request != 0) {
            int count = 0;
            try {
                synchronized (mRilCount) {
                    if (mRilCount.get(Integer.valueOf(request)) != null) {
                        count = ((Integer) mRilCount.get(Integer.valueOf(request))).intValue();
                    }
                    mRilCount.put(Integer.valueOf(request), new Integer(count + 1));
                }
            } catch (Exception e) {
                riljLog("processSolicited rilCount.put(" + request + ") error:" + e.toString());
            }
        }
    }

    private void countUnsolMsg(int response) {
        if (OemConstant.getPowerCenterEnable(this.mContext) && mRilCount != null && response != 0) {
            int count = 0;
            try {
                synchronized (mRilCount) {
                    if (mRilCount.get(Integer.valueOf(response)) != null) {
                        count = ((Integer) mRilCount.get(Integer.valueOf(response))).intValue();
                    }
                    mRilCount.put(Integer.valueOf(response), new Integer(count + 1));
                }
            } catch (Exception e) {
                riljLog("processUnsolicited rilCount.put(" + response + ") error:" + e.toString());
            }
        }
    }

    public int oppoResetRilCount() {
        synchronized (mRilCount) {
            mRilCount.clear();
        }
        for (int i = 0; i < mLocationUid.length; i++) {
            mLocationUid[i] = 0;
        }
        return 0;
    }

    public String oppoGetRilTopMsg() {
        int i;
        int[] top3Value = new int[]{0, 0, 0};
        int[] top3Key = new int[]{0, 0, 0};
        int max_location = 0;
        int max_uid = 0;
        int real_uid = 0;
        synchronized (mRilCount) {
        }
        for (Entry<Integer, Integer> entry : ((HashMap) mRilCount.clone()).entrySet()) {
            int key = ((Integer) entry.getKey()).intValue();
            int value = ((Integer) entry.getValue()).intValue();
            int tempKey = 0;
            if (value > 0) {
                for (int i2 : this.mIgnoreKey) {
                    if (key == i2) {
                        tempKey = -1;
                        break;
                    }
                }
                if (tempKey >= 0) {
                    for (i = 0; i < top3Value.length; i++) {
                        if (value > top3Value[i]) {
                            int tempValue = top3Value[i];
                            tempKey = top3Key[i];
                            top3Value[i] = value;
                            top3Key[i] = key;
                            value = tempValue;
                            key = tempKey;
                        }
                    }
                }
            }
        }
        StringBuffer buffer = new StringBuffer("[POWERSTATE]RILJ_TOP:");
        for (int k = 0; k < top3Value.length; k++) {
            if (top3Key[k] >= 1000) {
                buffer.append(responseToString(top3Key[k]) + ":" + top3Value[k] + " ");
            } else if (top3Key[k] == 109) {
                for (i = 0; i < mLocationUid.length; i++) {
                    if (max_location < mLocationUid[i]) {
                        max_location = mLocationUid[i];
                        max_uid = i;
                    }
                }
                if (max_uid < USER.USER1 * 1000) {
                    real_uid = max_uid + 1000;
                } else if (max_uid >= USER.USER1 * 1000 && max_uid < USER.USER2 * 1000) {
                    real_uid = (max_uid % 1000) + 10000;
                } else if (max_uid >= USER.USER2 * 1000) {
                    real_uid = 99910000 + (max_uid % 1000);
                }
                buffer.append(requestToString(top3Key[k]) + "_" + this.mContext.getPackageManager().getNameForUid(real_uid) + ":" + top3Value[k] + " ");
            } else {
                buffer.append(requestToString(top3Key[k]) + ":" + top3Value[k] + " ");
            }
        }
        riljLog(buffer.toString());
        if (top3Key[0] == 109) {
            return Integer.toString(top3Key[0]) + "_" + this.mContext.getPackageManager().getNameForUid(real_uid);
        }
        return Integer.toString(top3Key[0]);
    }

    public void oppoGetASDIVState(int rat, Message response) {
    }
}
