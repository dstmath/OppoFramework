package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.android.internal.telephony.Call.SrvccState;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.imsphone.ImsPhoneCall;
import com.android.internal.telephony.uicc.SpnOverride;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public abstract class CallTracker extends Handler {
    protected static final int AUTO_ANSWER_CDMA_TIMER = 2000;
    protected static final int AUTO_ANSWER_TIMER = 3;
    protected static final boolean DBG_POLL = OemConstant.SWITCH_LOG;
    protected static final int EVENT_AUTO_ANSWER = 900;
    protected static final int EVENT_CALL_STATE_CHANGE = 2;
    protected static final int EVENT_CALL_WAITING_INFO_CDMA = 15;
    protected static final int EVENT_CONFERENCE_RESULT = 11;
    protected static final int EVENT_ECT_RESULT = 13;
    protected static final int EVENT_EXIT_ECM_RESPONSE_CDMA = 14;
    protected static final int EVENT_GET_LAST_CALL_FAIL_CAUSE = 5;
    protected static final int EVENT_OPERATION_COMPLETE = 4;
    protected static final int EVENT_POLL_CALLS_RESULT = 1;
    protected static final int EVENT_RADIO_AVAILABLE = 9;
    protected static final int EVENT_RADIO_NOT_AVAILABLE = 10;
    protected static final int EVENT_REPOLL_AFTER_DELAY = 3;
    protected static final int EVENT_SEPARATE_RESULT = 12;
    protected static final int EVENT_SWITCH_RESULT = 8;
    protected static final int EVENT_THREE_WAY_DIAL_BLANK_FLASH = 20;
    protected static final int EVENT_THREE_WAY_DIAL_L2_RESULT_CDMA = 16;
    static final int POLL_DELAY_MSEC = 250;
    private final int VALID_COMPARE_LENGTH = 3;
    public CommandsInterface mCi;
    protected ArrayList<Connection> mHandoverConnections = new ArrayList();
    protected Message mLastRelevantPoll;
    protected boolean mNeedsPoll;
    protected boolean mNumberConverted = false;
    public String mPendingHangupAddr = null;
    public ImsPhoneCall mPendingHangupCall = null;
    protected int mPendingOperations;

    public abstract State getState();

    public abstract void handleMessage(Message message);

    protected abstract void handlePollCalls(AsyncResult asyncResult);

    protected abstract void log(String str);

    public abstract void registerForVoiceCallEnded(Handler handler, int i, Object obj);

    public abstract void registerForVoiceCallStarted(Handler handler, int i, Object obj);

    public abstract void unregisterForVoiceCallEnded(Handler handler);

    public abstract void unregisterForVoiceCallStarted(Handler handler);

    protected void pollCallsWhenSafe() {
        this.mNeedsPoll = true;
        if (checkNoOperationsPending()) {
            this.mLastRelevantPoll = obtainMessage(1);
            this.mCi.getCurrentCalls(this.mLastRelevantPoll);
        }
    }

    protected void pollCallsAfterDelay() {
        Message msg = obtainMessage();
        msg.what = 3;
        sendMessageDelayed(msg, 250);
    }

    protected boolean isCommandExceptionRadioNotAvailable(Throwable e) {
        if (e != null && (e instanceof CommandException) && ((CommandException) e).getCommandError() == Error.RADIO_NOT_AVAILABLE) {
            return true;
        }
        return false;
    }

    protected Connection getHoConnection(DriverCall dc) {
        for (Connection hoConn : this.mHandoverConnections) {
            log("getHoConnection - compare number: hoConn= " + hoConn.toString());
            if (hoConn.getAddress() != null && hoConn.getAddress().contains(dc.number)) {
                log("getHoConnection: Handover connection match found = " + hoConn.toString());
                return hoConn;
            }
        }
        for (Connection hoConn2 : this.mHandoverConnections) {
            log("getHoConnection: compare state hoConn= " + hoConn2.toString());
            if (hoConn2.getStateBeforeHandover() == Call.stateFromDCState(dc.state)) {
                log("getHoConnection: Handover connection match found = " + hoConn2.toString());
                return hoConn2;
            }
        }
        return null;
    }

    protected void notifySrvccState(SrvccState state, ArrayList<Connection> c) {
        if (state == SrvccState.STARTED && c != null) {
            this.mHandoverConnections.addAll(c);
        } else if (state != SrvccState.COMPLETED) {
            this.mHandoverConnections.clear();
        }
        log("notifySrvccState: mHandoverConnections= " + this.mHandoverConnections.toString());
    }

    protected void handleRadioAvailable() {
        pollCallsWhenSafe();
    }

    protected Message obtainNoPollCompleteMessage(int what) {
        this.mPendingOperations++;
        this.mLastRelevantPoll = null;
        return obtainMessage(what);
    }

    private boolean checkNoOperationsPending() {
        if (DBG_POLL) {
            log("checkNoOperationsPending: pendingOperations=" + this.mPendingOperations);
        }
        if (this.mPendingOperations == 0) {
            return true;
        }
        return false;
    }

    protected String checkForTestEmergencyNumber(String dialString) {
        String testEn = SystemProperties.get("ril.test.emergencynumber");
        if (DBG_POLL) {
            log("checkForTestEmergencyNumber: dialString=" + dialString + " testEn=" + testEn);
        }
        if (TextUtils.isEmpty(testEn)) {
            return dialString;
        }
        String[] values = testEn.split(":");
        log("checkForTestEmergencyNumber: values.length=" + values.length);
        if (values.length != 2 || !values[0].equals(PhoneNumberUtils.stripSeparators(dialString))) {
            return dialString;
        }
        if (this.mCi != null) {
            this.mCi.testingEmergencyCall();
        }
        log("checkForTestEmergencyNumber: remap " + dialString + " to " + values[1]);
        return values[1];
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x00ef A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00c2  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00c2  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00ef A:{RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected String convertNumberIfNecessary(Phone phone, String dialNumber) {
        if (dialNumber == null) {
            return dialNumber;
        }
        String[] convertMaps = null;
        PersistableBundle bundle = ((CarrierConfigManager) phone.getContext().getSystemService("carrier_config")).getConfig();
        if (bundle != null) {
            convertMaps = bundle.getStringArray("dial_string_replace_string_array");
        }
        if (convertMaps == null) {
            log("convertNumberIfNecessary convertMaps is null");
            return dialNumber;
        }
        log("convertNumberIfNecessary Roaming convertMaps.length " + convertMaps.length + " dialNumber.length() " + dialNumber.length());
        if (convertMaps.length < 1 || dialNumber.length() < 3) {
            return dialNumber;
        }
        String outNumber = SpnOverride.MVNO_TYPE_NONE;
        for (String convertMap : convertMaps) {
            log("convertNumberIfNecessary: " + convertMap);
            String[] entry = convertMap.split(":");
            if (entry != null && entry.length > 1) {
                String dsToReplace = entry[0];
                String dsReplacement = entry[1];
                if (!TextUtils.isEmpty(dsToReplace) && dialNumber.equals(dsToReplace)) {
                    if (TextUtils.isEmpty(dsReplacement) || !dsReplacement.endsWith("MDN")) {
                        outNumber = dsReplacement;
                        if (TextUtils.isEmpty(outNumber)) {
                            return dialNumber;
                        }
                        log("convertNumberIfNecessary: convert service number");
                        this.mNumberConverted = true;
                        return outNumber;
                    }
                    String mdn = phone.getLine1Number();
                    if (!TextUtils.isEmpty(mdn)) {
                        outNumber = mdn.startsWith("+") ? mdn : dsReplacement.substring(0, dsReplacement.length() - 3) + mdn;
                    }
                    if (TextUtils.isEmpty(outNumber)) {
                    }
                }
            }
        }
        if (TextUtils.isEmpty(outNumber)) {
        }
    }

    private boolean compareGid1(Phone phone, String serviceGid1) {
        int i = 0;
        String gid1 = phone.getGroupIdLevel1();
        int gid_length = serviceGid1.length();
        boolean ret = true;
        if (serviceGid1 == null || serviceGid1.equals(SpnOverride.MVNO_TYPE_NONE)) {
            log("compareGid1 serviceGid is empty, return " + true);
            return true;
        }
        if (gid1 != null && gid1.length() >= gid_length) {
            i = gid1.substring(0, gid_length).equalsIgnoreCase(serviceGid1);
        }
        if (i == 0) {
            log(" gid1 " + gid1 + " serviceGid1 " + serviceGid1);
            ret = false;
        }
        log("compareGid1 is " + (ret ? "Same" : "Different"));
        return ret;
    }

    public void cleanupCalls() {
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("CallTracker:");
        pw.println(" mPendingOperations=" + this.mPendingOperations);
        pw.println(" mNeedsPoll=" + this.mNeedsPoll);
        pw.println(" mLastRelevantPoll=" + this.mLastRelevantPoll);
    }

    protected void oemNofiyNoService(Phone phone, boolean isIdle) {
        boolean z = false;
        try {
            if (SystemProperties.get("persist.radio.multisim.config", SpnOverride.MVNO_TYPE_NONE).equals("dsds")) {
                z = phone.getContext().getPackageManager().hasSystemFeature("oppo.ct.optr");
            }
            if (z) {
                int pid = 0;
                if (phone.getPhoneId() == 0) {
                    if (phone.getServiceStateTracker().mSS.getRoaming()) {
                        pid = 1;
                    } else {
                        return;
                    }
                }
                Phone oPhone = PhoneFactory.getPhone(pid);
                if (oPhone != null && (pid != 0 || (oPhone.getServiceStateTracker().mSS.getRoaming() ^ 1) == 0)) {
                    oPhone.getServiceStateTracker().oemNofiyNoService(isIdle);
                }
            }
        } catch (Exception e) {
        }
    }

    protected boolean isOemBlackList(Phone phonebase, String number) {
        if (OemConstant.isCallInEnable(phonebase)) {
            return false;
        }
        return true;
    }

    protected boolean isOemAutoAnswer(Phone phone) {
        if (!phone.is_test_card() || !phone.getOemAutoAnswer()) {
            return false;
        }
        log("isOemAutoAnswer for test card...");
        return true;
    }

    protected void setOemStates(State state) {
    }

    public boolean isImsCallHangupPending() {
        return this.mPendingHangupCall != null;
    }

    public String getPendingHangupAddr() {
        return this.mPendingHangupAddr;
    }
}
