package com.mediatek.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Process;
import android.provider.CallLog;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.PhoneConstants;
import com.mediatek.internal.telephony.MtkIncomingCallChecker;
import java.util.Date;
import java.util.Iterator;

public final class MtkGsmCdmaCallTrackerHelper implements MtkIncomingCallChecker.OnCheckCompleteListener {
    protected static final int EVENT_CALL_STATE_CHANGE = 2;
    protected static final int EVENT_CALL_WAITING_INFO_CDMA = 15;
    protected static final int EVENT_CONFERENCE_RESULT = 11;
    protected static final int EVENT_DIAL_CALL_RESULT = 1002;
    protected static final int EVENT_ECT_RESULT = 13;
    protected static final int EVENT_EXIT_ECM_RESPONSE_CDMA = 14;
    protected static final int EVENT_GET_LAST_CALL_FAIL_CAUSE = 5;
    protected static final int EVENT_HANG_UP_RESULT = 1003;
    protected static final int EVENT_INCOMING_CALL_INDICATION = 1000;
    protected static final int EVENT_MTK_BASE = 1000;
    protected static final int EVENT_OPERATION_COMPLETE = 4;
    protected static final int EVENT_POLL_CALLS_RESULT = 1;
    protected static final int EVENT_RADIO_AVAILABLE = 9;
    protected static final int EVENT_RADIO_NOT_AVAILABLE = 10;
    protected static final int EVENT_RADIO_OFF_OR_NOT_AVAILABLE = 1001;
    protected static final int EVENT_REPOLL_AFTER_DELAY = 3;
    protected static final int EVENT_SEPARATE_RESULT = 12;
    protected static final int EVENT_SWITCH_RESULT = 8;
    protected static final int EVENT_THREE_WAY_DIAL_BLANK_FLASH = 20;
    protected static final int EVENT_THREE_WAY_DIAL_L2_RESULT_CDMA = 16;
    static final String LOG_TAG = "GsmCallTkrHlpr";
    private static final int MT_CALL_GWSD = 10;
    private static final int MT_CALL_MISSED = 2;
    private static final int MT_CALL_NUMREDIRECT = 3;
    private static final int MT_CALL_REJECTED = 1;
    private boolean mContainForwardingAddress = false;
    private Context mContext;
    private String mForwardingAddress = null;
    private int mForwardingAddressCallId = 0;
    private MtkIncomingCallChecker mIncomingCallChecker = null;
    private boolean mIsGwsdCall = false;
    private MtkGsmCdmaCallTracker mMtkTracker;

    public MtkGsmCdmaCallTrackerHelper(Context context, MtkGsmCdmaCallTracker tracker) {
        this.mContext = context;
        this.mMtkTracker = tracker;
    }

    /* access modifiers changed from: package-private */
    public void logD(String msg) {
        Rlog.d(LOG_TAG, msg + " (slot " + this.mMtkTracker.mPhone.getPhoneId() + ")");
    }

    /* access modifiers changed from: package-private */
    public void logI(String msg) {
        Rlog.i(LOG_TAG, msg + " (slot " + this.mMtkTracker.mPhone.getPhoneId() + ")");
    }

    /* access modifiers changed from: package-private */
    public void logW(String msg) {
        Rlog.w(LOG_TAG, msg + " (slot " + this.mMtkTracker.mPhone.getPhoneId() + ")");
    }

    /* access modifiers changed from: package-private */
    public void logE(String msg) {
        Rlog.e(LOG_TAG, msg + " (slot " + this.mMtkTracker.mPhone.getPhoneId() + ")");
    }

    public void LogerMessage(int msgType) {
        if (msgType == 1) {
            logD("handle EVENT_POLL_CALLS_RESULT");
        } else if (msgType == 2) {
            logD("handle EVENT_CALL_STATE_CHANGE");
        } else if (msgType == 3) {
            logD("handle EVENT_REPOLL_AFTER_DELAY");
        } else if (msgType == 4) {
            logD("handle EVENT_OPERATION_COMPLETE");
        } else if (msgType != 5) {
            switch (msgType) {
                case 8:
                    logD("handle EVENT_SWITCH_RESULT");
                    return;
                case 9:
                    logD("handle EVENT_RADIO_AVAILABLE");
                    return;
                case 10:
                    logD("handle EVENT_RADIO_NOT_AVAILABLE");
                    return;
                case 11:
                    logD("handle EVENT_CONFERENCE_RESULT");
                    return;
                case 12:
                    logD("handle EVENT_SEPARATE_RESULT");
                    return;
                case 13:
                    logD("handle EVENT_ECT_RESULT");
                    return;
                default:
                    switch (msgType) {
                        case 1000:
                            logD("handle EVENT_INCOMING_CALL_INDICATION");
                            return;
                        case 1001:
                            logD("handle EVENT_RADIO_OFF_OR_NOT_AVAILABLE");
                            return;
                        case 1002:
                            logD("handle EVENT_DIAL_CALL_RESULT");
                            return;
                        case 1003:
                            logD("handle EVENT_HANG_UP_RESULT");
                            return;
                        default:
                            logD("handle XXXXX");
                            return;
                    }
            }
        } else {
            logD("handle EVENT_GET_LAST_CALL_FAIL_CAUSE");
        }
    }

    public void LogState() {
        int count = 0;
        int s = this.mMtkTracker.getMaxConnections();
        for (int i = 0; i < s; i++) {
            if (this.mMtkTracker.mConnections[i] != null) {
                count++;
                logI("* conn id " + (this.mMtkTracker.mConnections[i].mIndex + 1) + " existed");
            }
        }
        logI("* GsmCT has " + count + " connection");
    }

    public int getCurrentTotalConnections() {
        int count = 0;
        for (int i = 0; i < this.mMtkTracker.getMaxConnections(); i++) {
            if (this.mMtkTracker.mConnections[i] != null) {
                count++;
            }
        }
        return count;
    }

    public void CallIndicationProcess(AsyncResult ar) {
        CallIndicationProcess(ar, false, false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0158  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x01a0  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x01a6  */
    /* JADX WARNING: Removed duplicated region for block: B:45:? A[RETURN, SYNTHETIC] */
    public void CallIndicationProcess(AsyncResult ar, boolean isIncomingNumberChecked, boolean isBlocked) {
        int rejectCause;
        int mode;
        int seqNumber;
        String number;
        int pid;
        int mode2 = 0;
        try {
            String[] incomingCallInfo = (String[]) ar.result;
            int callId = Integer.parseInt(incomingCallInfo[0]);
            String number2 = incomingCallInfo[1];
            Integer.parseInt(incomingCallInfo[3]);
            int seqNumber2 = Integer.parseInt(incomingCallInfo[4]);
            int subId = this.mMtkTracker.getPhone().getSubId();
            int rejectCause2 = 1;
            logD("CallIndicationProcess " + 0 + " callId " + callId + " seqNumber " + seqNumber2 + "(subId=" + subId + ", isIncomingNumberChecked=" + isIncomingNumberChecked + ", isBlocked=" + isBlocked + ")");
            this.mForwardingAddress = null;
            if (incomingCallInfo[5] != null && incomingCallInfo[5].length() > 0) {
                this.mContainForwardingAddress = false;
                this.mForwardingAddress = incomingCallInfo[5];
                this.mForwardingAddressCallId = callId;
                logD("EAIC message contains forwarding address - " + this.mForwardingAddress + "," + callId);
            }
            if (this.mMtkTracker.mState == PhoneConstants.State.RINGING) {
                mode2 = 1;
                rejectCause2 = 1;
            }
            if (mode2 == 0) {
                if (isIncomingNumberChecked) {
                    if (isBlocked) {
                        mode = 1;
                        rejectCause = 16;
                        if (mode != 0) {
                            int pid2 = Process.myPid();
                            Process.setThreadPriority(pid2, -10);
                            logD("Adjust the priority of process - " + pid2 + " to " + Process.getThreadPriority(pid2));
                            if (this.mForwardingAddress != null) {
                                this.mContainForwardingAddress = true;
                            }
                            seqNumber = seqNumber2;
                            number = number2;
                            pid = 1;
                            this.mMtkTracker.mMtkCi.setCallIndication(mode, callId, seqNumber, -1, null);
                        } else {
                            seqNumber = seqNumber2;
                            number = number2;
                            pid = 1;
                        }
                        if (mode == pid) {
                            return;
                        }
                        if (rejectCause != pid) {
                            this.mMtkTracker.mMtkCi.setCallIndication(mode, callId, seqNumber, rejectCause, null);
                            if (rejectCause == 16) {
                                addCallLog(this.mContext, this.mMtkTracker.mPhone.getIccSerialNumber(), number, 5);
                                return;
                            } else {
                                addCallLog(this.mContext, this.mMtkTracker.mPhone.getIccSerialNumber(), number, 3);
                                return;
                            }
                        } else {
                            this.mMtkTracker.mMtkCi.setCallIndication(mode, callId, seqNumber, -1, null);
                            return;
                        }
                    }
                } else if (SubscriptionManager.isValidSubscriptionId(subId) && MtkIncomingCallChecker.isMtkEnhancedCallBlockingEnabled(this.mContext, subId)) {
                    this.mIncomingCallChecker = new MtkIncomingCallChecker("" + callId + "_" + seqNumber2, ar);
                    if (this.mIncomingCallChecker.startIncomingCallNumberCheck(this.mContext, subId, number2, this)) {
                        logD("startIncomingCallNumberCheck true. start check (callId_seqNo=" + callId + "_" + seqNumber2 + ", subId=" + subId + ", number=" + number2 + ")");
                        return;
                    }
                    logE("startIncomingCallNumberCheck false, and flow continues. (callId_seqNo=" + callId + "_" + seqNumber2 + ", subId=" + subId + ", number=" + number2 + ")");
                }
            }
            mode = mode2;
            rejectCause = rejectCause2;
            if (mode != 0) {
            }
            if (mode == pid) {
            }
        } catch (NumberFormatException e) {
            Rlog.d(LOG_TAG, e.toString());
        } catch (Exception e2) {
            Rlog.d(LOG_TAG, e2.toString());
        }
    }

    public void CallIndicationEnd() {
        int pid = Process.myPid();
        if (Process.getThreadPriority(pid) != 0) {
            Process.setThreadPriority(pid, 0);
            logD("Current priority = " + Process.getThreadPriority(pid));
        }
    }

    @Override // com.mediatek.internal.telephony.MtkIncomingCallChecker.OnCheckCompleteListener
    public void onCheckComplete(boolean result, Object obj) {
        CallIndicationProcess(obj != null ? (AsyncResult) obj : null, true, result);
    }

    private void addCallLog(Context context, String iccId, String number, int type) {
        String number2;
        int presentationMode;
        PhoneAccountHandle phoneAccountHandle = null;
        Iterator<PhoneAccountHandle> phoneAccounts = TelecomManager.from(context).getCallCapablePhoneAccounts().listIterator();
        while (true) {
            if (!phoneAccounts.hasNext()) {
                break;
            }
            PhoneAccountHandle handle = phoneAccounts.next();
            String id = handle.getId();
            if (id != null) {
                if (id.equals(iccId)) {
                    phoneAccountHandle = handle;
                    break;
                }
            }
        }
        if (number == null) {
            number2 = "";
        } else {
            number2 = number;
        }
        if (number2.equals("")) {
            presentationMode = 2;
        } else {
            presentationMode = 1;
        }
        CallLog.Calls.addCall(null, context, number2, presentationMode, type, 0, phoneAccountHandle, new Date().getTime(), 0, new Long(0));
    }

    public void handleCallAdditionalInfo(AsyncResult ar) {
        String[] callAdditionalInfo = (String[]) ar.result;
        int type = Integer.parseInt(callAdditionalInfo[0]);
        if (type == 2 || type == 1 || type == 3 || type == 10) {
            String number = callAdditionalInfo[1];
            int callMode = Integer.parseInt(callAdditionalInfo[2]);
            if (callMode != 0) {
                logD("handleCallAdditionalInfo unexpected callMode");
                return;
            }
            logD("handleCallAdditionalInfo type:" + type + " mode:" + callMode);
            if (type == 1) {
                addCallLog(this.mContext, this.mMtkTracker.mPhone.getIccSerialNumber(), number, 5);
            } else if (type == 2) {
                addCallLog(this.mContext, this.mMtkTracker.mPhone.getIccSerialNumber(), number, 3);
            } else if (type == 3) {
                String redirectNumber = callAdditionalInfo[3];
                int callId = Integer.parseInt(callAdditionalInfo[4]);
                this.mForwardingAddress = null;
                if (redirectNumber != null && redirectNumber.length() > 0) {
                    this.mContainForwardingAddress = true;
                    this.mForwardingAddress = redirectNumber;
                    this.mForwardingAddressCallId = callId;
                    logD("Forwarding address: " + this.mForwardingAddress + "," + callId);
                }
            } else if (type == 10) {
                this.mIsGwsdCall = true;
            }
        } else {
            logD("handleCallAdditionalInfo not handle event");
        }
    }

    public boolean isGwsdCall() {
        return this.mIsGwsdCall;
    }

    public void setGwsdCall(boolean isGwsd) {
        this.mIsGwsdCall = isGwsd;
    }

    public void clearForwardingAddressVariables(int index) {
        if (this.mContainForwardingAddress && this.mForwardingAddressCallId == index + 1) {
            this.mContainForwardingAddress = false;
            this.mForwardingAddress = null;
            this.mForwardingAddressCallId = 0;
        }
    }

    public void setForwardingAddressToConnection(int index, Connection conn) {
        String str;
        if (this.mContainForwardingAddress && (str = this.mForwardingAddress) != null && this.mForwardingAddressCallId == index + 1) {
            ((MtkGsmCdmaConnection) conn).setForwardingAddress(str);
            logD("Store forwarding address - " + this.mForwardingAddress);
            logD("Get forwarding address - " + ((MtkGsmCdmaConnection) conn).getForwardingAddress());
            clearForwardingAddressVariables(index);
        }
    }
}
