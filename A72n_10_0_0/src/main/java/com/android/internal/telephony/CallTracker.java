package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.PhoneConstants;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class CallTracker extends AbstractCallTracker {
    protected static final int AUTO_ANSWER_CDMA_TIMER = 2000;
    protected static final int AUTO_ANSWER_TIMER = 3;
    private static final boolean DBG_POLL = false;
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
    @UnsupportedAppUsage
    public CommandsInterface mCi;
    protected ArrayList<Connection> mHandoverConnections = new ArrayList<>();
    protected Message mLastRelevantPoll;
    @UnsupportedAppUsage
    protected boolean mNeedsPoll;
    @UnsupportedAppUsage
    protected boolean mNumberConverted = false;
    @UnsupportedAppUsage
    protected int mPendingOperations;

    /* access modifiers changed from: protected */
    public abstract Phone getPhone();

    @UnsupportedAppUsage
    public abstract PhoneConstants.State getState();

    public abstract void handleMessage(Message message);

    /* access modifiers changed from: protected */
    public abstract void handlePollCalls(AsyncResult asyncResult);

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public abstract void log(String str);

    @UnsupportedAppUsage
    public abstract void registerForVoiceCallEnded(Handler handler, int i, Object obj);

    public abstract void registerForVoiceCallStarted(Handler handler, int i, Object obj);

    public abstract void unregisterForVoiceCallEnded(Handler handler);

    public abstract void unregisterForVoiceCallStarted(Handler handler);

    /* access modifiers changed from: protected */
    public void pollCallsWhenSafe() {
        this.mNeedsPoll = true;
        if (checkNoOperationsPending()) {
            this.mLastRelevantPoll = obtainMessage(1);
            this.mCi.getCurrentCalls(this.mLastRelevantPoll);
        }
    }

    /* access modifiers changed from: protected */
    public void pollCallsAfterDelay() {
        Message msg = obtainMessage();
        msg.what = 3;
        sendMessageDelayed(msg, 250);
    }

    /* access modifiers changed from: protected */
    public boolean isCommandExceptionRadioNotAvailable(Throwable e) {
        return e != null && (e instanceof CommandException) && ((CommandException) e).getCommandError() == CommandException.Error.RADIO_NOT_AVAILABLE;
    }

    /* access modifiers changed from: protected */
    public Connection getHoConnection(DriverCall dc) {
        Iterator<Connection> it = this.mHandoverConnections.iterator();
        while (it.hasNext()) {
            Connection hoConn = it.next();
            log("getHoConnection - compare number: hoConn= " + hoConn.toString());
            if (hoConn.getAddress() != null && hoConn.getAddress().contains(dc.number)) {
                log("getHoConnection: Handover connection match found = " + hoConn.toString());
                return hoConn;
            }
        }
        Iterator<Connection> it2 = this.mHandoverConnections.iterator();
        while (it2.hasNext()) {
            Connection hoConn2 = it2.next();
            log("getHoConnection: compare state hoConn= " + hoConn2.toString());
            if (hoConn2.getStateBeforeHandover() == Call.stateFromDCState(dc.state)) {
                log("getHoConnection: Handover connection match found = " + hoConn2.toString());
                return hoConn2;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void notifySrvccState(Call.SrvccState state, ArrayList<Connection> c) {
        if (state == Call.SrvccState.STARTED && c != null) {
            this.mHandoverConnections.addAll(c);
        } else if (state != Call.SrvccState.COMPLETED) {
            this.mHandoverConnections.clear();
        }
        log("notifySrvccState: mHandoverConnections= " + this.mHandoverConnections.toString());
    }

    /* access modifiers changed from: protected */
    public void handleRadioAvailable() {
        pollCallsWhenSafe();
    }

    /* access modifiers changed from: protected */
    public Message obtainNoPollCompleteMessage(int what) {
        this.mPendingOperations++;
        this.mLastRelevantPoll = null;
        return obtainMessage(what);
    }

    private boolean checkNoOperationsPending() {
        return this.mPendingOperations == 0;
    }

    /* access modifiers changed from: protected */
    public String checkForTestEmergencyNumber(String dialString) {
        String testEn = SystemProperties.get("ril.test.emergencynumber");
        if (TextUtils.isEmpty(testEn)) {
            return dialString;
        }
        String[] values = testEn.split(":");
        log("checkForTestEmergencyNumber: values.length=" + values.length);
        if (values.length != 2 || !values[0].equals(PhoneNumberUtils.stripSeparators(dialString))) {
            return dialString;
        }
        CommandsInterface commandsInterface = this.mCi;
        if (commandsInterface != null) {
            commandsInterface.testingEmergencyCall();
        }
        log("checkForTestEmergencyNumber: remap " + dialString + " to " + values[1]);
        return values[1];
    }

    /* access modifiers changed from: protected */
    public String convertNumberIfNecessary(Phone phone, String dialNumber) {
        if (dialNumber == null) {
            return dialNumber;
        }
        String[] convertMaps = null;
        PersistableBundle bundle = ((CarrierConfigManager) phone.getContext().getSystemService("carrier_config")).getConfigForSubId(phone.getSubId());
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
        String outNumber = PhoneConfigurationManager.SSSS;
        int length = convertMaps.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            String convertMap = convertMaps[i];
            log("convertNumberIfNecessary: " + convertMap);
            String[] entry = convertMap.split(":");
            if (entry != null && entry.length > 1) {
                String dsToReplace = entry[0];
                String dsReplacement = entry[1];
                if (!TextUtils.isEmpty(dsToReplace) && dialNumber.equals(dsToReplace)) {
                    if (TextUtils.isEmpty(dsReplacement) || !dsReplacement.endsWith("MDN")) {
                        outNumber = dsReplacement;
                    } else {
                        String mdn = phone.getLine1Number();
                        if (!TextUtils.isEmpty(mdn)) {
                            if (mdn.startsWith("+")) {
                                outNumber = mdn;
                            } else {
                                outNumber = dsReplacement.substring(0, dsReplacement.length() - 3) + mdn;
                            }
                        }
                    }
                }
            }
            i++;
        }
        if (TextUtils.isEmpty(outNumber)) {
            return dialNumber;
        }
        log("convertNumberIfNecessary: convert service number");
        this.mNumberConverted = true;
        return outNumber;
    }

    private boolean compareGid1(Phone phone, String serviceGid1) {
        String gid1 = phone.getGroupIdLevel1();
        int gid_length = serviceGid1.length();
        boolean ret = true;
        if (serviceGid1.equals(PhoneConfigurationManager.SSSS)) {
            log("compareGid1 serviceGid is empty, return true");
            return true;
        }
        if (gid1 == null || gid1.length() < gid_length || !gid1.substring(0, gid_length).equalsIgnoreCase(serviceGid1)) {
            log(" gid1 " + gid1 + " serviceGid1 " + serviceGid1);
            ret = false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("compareGid1 is ");
        sb.append(ret ? "Same" : "Different");
        log(sb.toString());
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

    /* access modifiers changed from: protected */
    public boolean isOemBlackList(Phone phone, String number) {
        return false;
    }

    @Override // com.android.internal.telephony.AbstractCallTracker
    public boolean isOemAutoAnswer(Phone phone) {
        if (!phone.is_test_card() || !phone.getOemAutoAnswer()) {
            log("isOemAutoAnswer for test card...false");
            return false;
        }
        log("isOemAutoAnswer for test card...true");
        return true;
    }

    public void oemClearConn() {
        log("oemClearConn...");
    }
}
