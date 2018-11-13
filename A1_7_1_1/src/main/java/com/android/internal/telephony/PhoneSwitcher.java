package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.LocalLog;
import com.android.internal.telephony.IOnSubscriptionsChangedListener.Stub;
import com.android.internal.telephony.dataconnection.DcRequest;
import com.android.internal.util.IndentingPrintWriter;
import com.google.android.mms.pdu.CharacterSets;
import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
import com.mediatek.internal.telephony.dataconnection.DataConnectionHelper;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class PhoneSwitcher extends Handler {
    private static final int EVENT_DEFAULT_SUBSCRIPTION_CHANGED = 101;
    private static final int EVENT_EMERGENCY_TOGGLE = 105;
    private static final int EVENT_RELEASE_NETWORK = 104;
    private static final int EVENT_REQUEST_NETWORK = 103;
    private static final int EVENT_RESEND_DATA_ALLOWED = 106;
    private static final int EVENT_SUBSCRIPTION_CHANGED = 102;
    private static final String LOG_TAG = "PhoneSwitcher";
    private static final int MAX_LOCAL_LOG_LINES = 30;
    private static final boolean REQUESTS_CHANGED = true;
    private static final boolean REQUESTS_UNCHANGED = false;
    private static final boolean VDBG = true;
    private final RegistrantList[] mActivePhoneRegistrants;
    private final CommandsInterface[] mCommandsInterfaces;
    private final Context mContext;
    private final BroadcastReceiver mDefaultDataChangedReceiver;
    private int mDefaultDataSlotId;
    private int mDefaultDataSubscription;
    private final LocalLog mLocalLog;
    private int mMaxActivePhones;
    private final int mNumPhones;
    private final PhoneState[] mPhoneStates;
    private final int[] mPhoneSubscriptions;
    private final Phone[] mPhones;
    private final List<DcRequest> mPrioritizedDcRequests;
    private final SubscriptionController mSubscriptionController;
    private final IOnSubscriptionsChangedListener mSubscriptionsChangedListener;
    private TelephonyDevController mTelDevController;

    private static class PhoneState {
        public volatile boolean active;
        public long lastRequested;

        /* synthetic */ PhoneState(PhoneState phoneState) {
            this();
        }

        private PhoneState() {
            this.active = false;
            this.lastRequested = 0;
        }
    }

    private static class PhoneSwitcherNetworkRequestListener extends NetworkFactory {
        private final PhoneSwitcher mPhoneSwitcher;

        public PhoneSwitcherNetworkRequestListener(Looper l, Context c, NetworkCapabilities nc, PhoneSwitcher ps) {
            super(l, c, "PhoneSwitcherNetworkRequstListener", nc);
            this.mPhoneSwitcher = ps;
        }

        protected void needNetworkFor(NetworkRequest networkRequest, int score) {
            log("needNetworkFor " + networkRequest + ", " + score);
            Message msg = this.mPhoneSwitcher.obtainMessage(103);
            msg.obj = networkRequest;
            msg.sendToTarget();
        }

        protected void releaseNetworkFor(NetworkRequest networkRequest) {
            log("releaseNetworkFor " + networkRequest);
            Message msg = this.mPhoneSwitcher.obtainMessage(104);
            msg.obj = networkRequest;
            msg.sendToTarget();
        }
    }

    public PhoneSwitcher(Looper looper) {
        super(looper);
        this.mPrioritizedDcRequests = new ArrayList();
        this.mDefaultDataSubscription = -2;
        this.mDefaultDataSlotId = -2;
        this.mTelDevController = TelephonyDevController.getInstance();
        this.mDefaultDataChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                PhoneSwitcher.this.obtainMessage(101).sendToTarget();
            }
        };
        this.mSubscriptionsChangedListener = new Stub() {
            public void onSubscriptionsChanged() {
                PhoneSwitcher.this.obtainMessage(102).sendToTarget();
            }
        };
        this.mMaxActivePhones = 0;
        this.mSubscriptionController = null;
        this.mPhoneSubscriptions = null;
        this.mCommandsInterfaces = null;
        this.mContext = null;
        this.mPhoneStates = null;
        this.mPhones = null;
        this.mLocalLog = null;
        this.mActivePhoneRegistrants = null;
        this.mNumPhones = 0;
    }

    public PhoneSwitcher(int maxActivePhones, int numPhones, Context context, SubscriptionController subscriptionController, Looper looper, ITelephonyRegistry tr, CommandsInterface[] cis, Phone[] phones) {
        super(looper);
        this.mPrioritizedDcRequests = new ArrayList();
        this.mDefaultDataSubscription = -2;
        this.mDefaultDataSlotId = -2;
        this.mTelDevController = TelephonyDevController.getInstance();
        this.mDefaultDataChangedReceiver = /* anonymous class already generated */;
        this.mSubscriptionsChangedListener = /* anonymous class already generated */;
        this.mContext = context;
        this.mNumPhones = numPhones;
        this.mPhones = phones;
        this.mPhoneSubscriptions = new int[numPhones];
        this.mMaxActivePhones = maxActivePhones;
        this.mLocalLog = new LocalLog(30);
        this.mSubscriptionController = subscriptionController;
        this.mActivePhoneRegistrants = new RegistrantList[numPhones];
        this.mPhoneStates = new PhoneState[numPhones];
        for (int i = 0; i < numPhones; i++) {
            this.mActivePhoneRegistrants[i] = new RegistrantList();
            this.mPhoneStates[i] = new PhoneState();
            if (this.mPhones[i] != null) {
                this.mPhones[i].registerForEmergencyCallToggle(this, 105, null);
            }
        }
        this.mCommandsInterfaces = cis;
        try {
            tr.addOnSubscriptionsChangedListener(LOG_TAG, this.mSubscriptionsChangedListener);
        } catch (RemoteException e) {
        }
        this.mContext.registerReceiver(this.mDefaultDataChangedReceiver, new IntentFilter("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED"));
        NetworkCapabilities netCap = new NetworkCapabilities();
        netCap.addTransportType(0);
        netCap.addCapability(0);
        netCap.addCapability(1);
        netCap.addCapability(2);
        netCap.addCapability(3);
        netCap.addCapability(4);
        netCap.addCapability(5);
        netCap.addCapability(7);
        netCap.addCapability(8);
        netCap.addCapability(9);
        netCap.addCapability(10);
        netCap.addCapability(13);
        netCap.addCapability(12);
        netCap.addCapability(27);
        netCap.setNetworkSpecifier(CharacterSets.MIMENAME_ANY_CHARSET);
        NetworkFactory networkFactory = new PhoneSwitcherNetworkRequestListener(looper, context, netCap, this);
        networkFactory.setScoreFilter(101);
        networkFactory.register();
        log("PhoneSwitcher started");
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 101:
                onEvaluate(false, "defaultChanged");
                return;
            case 102:
                onEvaluate(false, "subChanged");
                return;
            case 103:
                onRequestNetwork((NetworkRequest) msg.obj);
                return;
            case 104:
                onReleaseNetwork((NetworkRequest) msg.obj);
                return;
            case 105:
                onEvaluate(true, "emergencyToggle");
                return;
            case 106:
                onResendDataAllowed(msg);
                return;
            default:
                return;
        }
    }

    private boolean isEmergency() {
        for (Phone p : this.mPhones) {
            if (p != null && (p.isInEcm() || p.isInEmergencyCall())) {
                return true;
            }
        }
        return false;
    }

    private void onRequestNetwork(NetworkRequest networkRequest) {
        DcRequest dcRequest = new DcRequest(networkRequest, this.mContext);
        if (!this.mPrioritizedDcRequests.contains(dcRequest)) {
            this.mPrioritizedDcRequests.add(dcRequest);
            Collections.sort(this.mPrioritizedDcRequests);
            onEvaluate(true, "netRequest");
        }
    }

    private void onReleaseNetwork(NetworkRequest networkRequest) {
        if (this.mPrioritizedDcRequests.remove(new DcRequest(networkRequest, this.mContext))) {
            onEvaluate(true, "netReleased");
        }
    }

    private void onEvaluate(boolean requestsChanged, String reason) {
        StringBuilder sb = new StringBuilder(reason);
        if (isEmergency()) {
            log("onEvalute aborted due to Emergency");
            return;
        }
        int i;
        boolean diffDetected = requestsChanged;
        int dataSub = this.mSubscriptionController.getDefaultDataSubId();
        int slotId = SubscriptionManager.getSlotId(dataSub);
        log("onEvaluate -> mDefaultDataSubId: " + this.mDefaultDataSubscription + ", dataSub: " + dataSub + ", mDefaultDataSlotId: " + this.mDefaultDataSlotId + ", slotId: " + slotId);
        if (dataSub != this.mDefaultDataSubscription) {
            sb.append(" default ").append(this.mDefaultDataSubscription).append("->").append(dataSub);
            log("mDefaultDataSubscription: " + this.mDefaultDataSubscription + ", dataSub: " + dataSub);
            this.mDefaultDataSubscription = dataSub;
            DataConnectionHelper.updateDefaultDataIccid(dataSub);
            if (DataConnectionHelper.MTK_SVLTE_SUPPORT) {
                DataConnectionHelper.getInstance().updateMaxActivePhoneSvlte(SubscriptionManager.getPhoneId(this.mDefaultDataSubscription));
            }
            diffDetected = true;
        }
        if (slotId != this.mDefaultDataSlotId) {
            this.mDefaultDataSlotId = slotId;
            DataConnectionHelper.getInstance().syncDefaultDataSlotId(slotId);
        }
        for (i = 0; i < this.mNumPhones; i++) {
            int sub = this.mSubscriptionController.getSubIdUsingPhoneId(i);
            if (sub != this.mPhoneSubscriptions[i]) {
                sb.append(" phone[").append(i).append("] ").append(this.mPhoneSubscriptions[i]);
                sb.append("->").append(sub);
                this.mPhoneSubscriptions[i] = sub;
                diffDetected = true;
            }
        }
        if (diffDetected) {
            log("evaluating due to " + sb.toString());
            List<Integer> newActivePhones = new ArrayList();
            for (DcRequest dcRequest : this.mPrioritizedDcRequests) {
                int phoneIdForRequest = phoneIdForRequest(dcRequest.networkRequest);
                if (!(phoneIdForRequest == -1 || newActivePhones.contains(Integer.valueOf(phoneIdForRequest)))) {
                    newActivePhones.add(Integer.valueOf(phoneIdForRequest));
                    if (newActivePhones.size() >= this.mMaxActivePhones) {
                        break;
                    }
                }
            }
            DataConnectionHelper dcHelper = DataConnectionHelper.getInstance();
            int mainCapPhoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
            if (newActivePhones.isEmpty()) {
                log("newActivePhones is empty");
                if (dcHelper.isSimInserted(mainCapPhoneId)) {
                    log("newActivePhones mainCapPhoneId=" + mainCapPhoneId);
                    newActivePhones.add(Integer.valueOf(mainCapPhoneId));
                }
            }
            if (!(this.mTelDevController == null || this.mTelDevController.getModem(0) == null || this.mTelDevController.getModem(0).hasMdAutoSetupImsCapability() || !newActivePhones.isEmpty())) {
                log("check ECC w/o SIM");
                for (DcRequest dcRequest2 : this.mPrioritizedDcRequests) {
                    if (isSkipEimsCheck(dcRequest2.networkRequest)) {
                        log("newActivePhones mainCapPhoneId=" + mainCapPhoneId);
                        newActivePhones.add(Integer.valueOf(mainCapPhoneId));
                    }
                }
            }
            if (DataConnectionHelper.MTK_SVLTE_SUPPORT) {
                dcHelper.updateActivePhonesSvlte(newActivePhones);
            }
            log("default subId = " + this.mDefaultDataSubscription);
            for (i = 0; i < this.mNumPhones; i++) {
                log(" phone[" + i + "] using sub[" + this.mPhoneSubscriptions[i] + "]");
            }
            log(" newActivePhones:");
            for (Integer i2 : newActivePhones) {
                log("  " + i2);
            }
            for (int phoneId = 0; phoneId < this.mNumPhones; phoneId++) {
                if (!newActivePhones.contains(Integer.valueOf(phoneId))) {
                    deactivate(phoneId);
                }
            }
            for (Integer intValue : newActivePhones) {
                activate(intValue.intValue());
            }
        }
    }

    private void deactivate(int phoneId) {
        PhoneState state = this.mPhoneStates[phoneId];
        if (state.active) {
            state.active = false;
            log("deactivate " + phoneId);
            state.lastRequested = System.currentTimeMillis();
            this.mCommandsInterfaces[phoneId].setDataAllowed(false, null);
            this.mActivePhoneRegistrants[phoneId].notifyRegistrants();
        }
    }

    private void activate(int phoneId) {
        PhoneState state = this.mPhoneStates[phoneId];
        if (!state.active) {
            state.active = true;
            log("activate " + phoneId);
            state.lastRequested = System.currentTimeMillis();
            this.mCommandsInterfaces[phoneId].setDataAllowed(true, null);
            this.mActivePhoneRegistrants[phoneId].notifyRegistrants();
        }
    }

    public void resendDataAllowed(int phoneId) {
        log("resendDataAllowed " + phoneId);
        validatePhoneId(phoneId);
        Message msg = obtainMessage(106);
        msg.arg1 = phoneId;
        msg.sendToTarget();
    }

    private void onResendDataAllowed(Message msg) {
        int phoneId = msg.arg1;
        this.mCommandsInterfaces[phoneId].setDataAllowed(this.mPhoneStates[phoneId].active, null);
    }

    private int phoneIdForRequest(NetworkRequest netRequest) {
        int subId;
        String specifier = netRequest.networkCapabilities.getNetworkSpecifier();
        if (TextUtils.isEmpty(specifier)) {
            subId = this.mDefaultDataSubscription;
        } else {
            subId = Integer.parseInt(specifier);
        }
        int phoneId = -1;
        if (subId == -1) {
            return -1;
        }
        for (int i = 0; i < this.mNumPhones; i++) {
            if (this.mPhoneSubscriptions[i] == subId) {
                phoneId = i;
                break;
            }
        }
        return phoneId;
    }

    public boolean isPhoneActive(int phoneId) {
        validatePhoneId(phoneId);
        return this.mPhoneStates[phoneId].active;
    }

    public void registerForActivePhoneSwitch(int phoneId, Handler h, int what, Object o) {
        validatePhoneId(phoneId);
        Registrant r = new Registrant(h, what, o);
        this.mActivePhoneRegistrants[phoneId].add(r);
        r.notifyRegistrant();
    }

    public void unregisterForActivePhoneSwitch(int phoneId, Handler h) {
        validatePhoneId(phoneId);
        this.mActivePhoneRegistrants[phoneId].remove(h);
    }

    private void validatePhoneId(int phoneId) {
        if (phoneId < 0 || phoneId >= this.mNumPhones) {
            throw new IllegalArgumentException("Invalid PhoneId");
        }
    }

    private void log(String l) {
        Rlog.d(LOG_TAG, l);
        this.mLocalLog.log(l);
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        pw.println("PhoneSwitcher:");
        Calendar c = Calendar.getInstance();
        for (int i = 0; i < this.mNumPhones; i++) {
            String str;
            PhoneState ps = this.mPhoneStates[i];
            c.setTimeInMillis(ps.lastRequested);
            StringBuilder append = new StringBuilder().append("PhoneId(").append(i).append(") active=").append(ps.active).append(", lastRequest=");
            if (ps.lastRequested == 0) {
                str = "never";
            } else {
                str = String.format("%tm-%td %tH:%tM:%tS.%tL", new Object[]{c, c, c, c, c, c});
            }
            pw.println(append.append(str).toString());
        }
        pw.increaseIndent();
        this.mLocalLog.dump(fd, pw, args);
        pw.decreaseIndent();
    }

    private boolean isSkipEimsCheck(NetworkRequest networkRequest) {
        DataConnectionHelper dcHelper = DataConnectionHelper.getInstance();
        if (networkRequest.networkCapabilities.hasCapability(10)) {
            for (int i = 0; i < this.mNumPhones; i++) {
                if (dcHelper.isSimInserted(i)) {
                    Rlog.e(LOG_TAG, "isSkipEimsCheck, failCause: sim is not null");
                    return false;
                }
            }
            return true;
        }
        Rlog.e(LOG_TAG, "isSkipEimsCheck, failCause: not include EIMS capability");
        return false;
    }

    public void reRegisterPsNetwork() {
        for (int phoneId = 0; phoneId < this.mPhones.length; phoneId++) {
            deactivate(phoneId);
        }
        onEvaluate(false, "doRecovery");
    }

    public void setMaxActivePhones(int count) {
        this.mMaxActivePhones = count;
    }

    public int getMaxActivePhonesCount() {
        return this.mMaxActivePhones;
    }

    public void onModeChanged() {
        onEvaluate(true, "modeChanged");
    }
}
