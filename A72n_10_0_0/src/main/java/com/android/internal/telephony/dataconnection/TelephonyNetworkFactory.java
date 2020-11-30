package com.android.internal.telephony.dataconnection;

import android.content.Context;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkRequest;
import android.net.StringNetworkSpecifier;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.AccessNetworkConstants;
import android.telephony.Rlog;
import android.telephony.data.ApnSetting;
import android.util.LocalLog;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneSwitcher;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.SubscriptionMonitor;
import com.android.internal.telephony.dataconnection.TransportManager;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TelephonyNetworkFactory extends NetworkFactory {
    protected static final int ACTION_NO_OP = 0;
    protected static final int ACTION_RELEASE = 2;
    protected static final int ACTION_REQUEST = 1;
    protected static final boolean DBG = true;
    private static final int EVENT_ACTIVE_PHONE_SWITCH = 1;
    private static final int EVENT_DATA_HANDOVER_COMPLETED = 6;
    private static final int EVENT_DATA_HANDOVER_NEEDED = 5;
    private static final int EVENT_NETWORK_RELEASE = 4;
    private static final int EVENT_NETWORK_REQUEST = 3;
    private static final int EVENT_SUBSCRIPTION_CHANGED = 2;
    private static final int REQUEST_LOG_SIZE = 40;
    private static final int TELEPHONY_NETWORK_SCORE = 50;
    public final String LOG_TAG;
    private final Handler mInternalHandler;
    private final LocalLog mLocalLog = new LocalLog(40);
    protected final Map<NetworkRequest, Integer> mNetworkRequests = new HashMap();
    protected final Map<Message, TransportManager.HandoverParams> mPendingHandovers = new HashMap();
    private final Phone mPhone;
    private final PhoneSwitcher mPhoneSwitcher;
    private final SubscriptionController mSubscriptionController;
    private int mSubscriptionId;
    private final SubscriptionMonitor mSubscriptionMonitor;
    private final TransportManager mTransportManager;

    /* JADX WARNING: Illegal instructions before constructor call */
    public TelephonyNetworkFactory(SubscriptionMonitor subscriptionMonitor, Looper looper, Phone phone) {
        super(looper, r0, "TelephonyNetworkFactory[" + phone.getPhoneId() + "]", (NetworkCapabilities) null);
        Context context = phone.getContext();
        this.mPhone = phone;
        this.mTransportManager = this.mPhone.getTransportManager();
        this.mInternalHandler = new InternalHandler(looper);
        this.mSubscriptionController = SubscriptionController.getInstance();
        setCapabilityFilter(makeNetworkFilter(this.mSubscriptionController, this.mPhone.getPhoneId()));
        setScoreFilter(50);
        this.mPhoneSwitcher = PhoneSwitcher.getInstance();
        this.mSubscriptionMonitor = subscriptionMonitor;
        this.LOG_TAG = "TelephonyNetworkFactory[" + this.mPhone.getPhoneId() + "]";
        this.mPhoneSwitcher.registerForActivePhoneSwitch(this.mInternalHandler, 1, null);
        this.mTransportManager.registerForHandoverNeededEvent(this.mInternalHandler, 5);
        this.mSubscriptionId = -1;
        this.mSubscriptionMonitor.registerForSubscriptionChanged(this.mPhone.getPhoneId(), this.mInternalHandler, 2, null);
        register();
    }

    private NetworkCapabilities makeNetworkFilter(SubscriptionController subscriptionController, int phoneId) {
        return makeNetworkFilter(subscriptionController.getSubIdUsingPhoneId(phoneId));
    }

    /* access modifiers changed from: protected */
    public NetworkCapabilities makeNetworkFilter(int subscriptionId) {
        NetworkCapabilities nc = new NetworkCapabilities();
        nc.addTransportType(0);
        nc.addCapability(0);
        nc.addCapability(1);
        nc.addCapability(2);
        nc.addCapability(3);
        nc.addCapability(4);
        nc.addCapability(5);
        nc.addCapability(7);
        nc.addCapability(8);
        nc.addCapability(9);
        nc.addCapability(10);
        nc.addCapability(13);
        nc.addCapability(12);
        nc.setNetworkSpecifier(new StringNetworkSpecifier(String.valueOf(subscriptionId)));
        return nc;
    }

    private class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    TelephonyNetworkFactory.this.onActivePhoneSwitch();
                    return;
                case 2:
                    TelephonyNetworkFactory.this.onSubIdChange();
                    return;
                case 3:
                    TelephonyNetworkFactory.this.onNeedNetworkFor(msg);
                    return;
                case 4:
                    TelephonyNetworkFactory.this.onReleaseNetworkFor(msg);
                    return;
                case 5:
                    TransportManager.HandoverParams handoverParams = (TransportManager.HandoverParams) ((AsyncResult) msg.obj).result;
                    TelephonyNetworkFactory.this.onDataHandoverNeeded(handoverParams.apnType, handoverParams.targetTransport, handoverParams);
                    return;
                case 6:
                    Bundle bundle = msg.getData();
                    if (bundle.getInt("extra_request_type") == 2) {
                        NetworkRequest nr = (NetworkRequest) bundle.getParcelable(DcTracker.DATA_COMPLETE_MSG_EXTRA_NETWORK_REQUEST);
                        boolean success = bundle.getBoolean("extra_success");
                        int transport = bundle.getInt("extra_transport_type");
                        boolean fallback = bundle.getBoolean("extra_handover_failure_fallback");
                        TransportManager.HandoverParams handoverParams2 = TelephonyNetworkFactory.this.mPendingHandovers.remove(msg);
                        if (handoverParams2 != null) {
                            TelephonyNetworkFactory.this.onDataHandoverSetupCompleted(nr, success, transport, fallback, handoverParams2);
                            return;
                        } else {
                            TelephonyNetworkFactory.this.logl("Handover completed but cannot find handover entry!");
                            return;
                        }
                    } else {
                        return;
                    }
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getTransportTypeFromNetworkRequest(NetworkRequest networkRequest) {
        return this.mTransportManager.getCurrentTransport(ApnContext.getApnTypeFromNetworkRequest(networkRequest));
    }

    private void requestNetworkInternal(NetworkRequest networkRequest, int requestType, int transport, Message onCompleteMsg) {
        if (this.mPhone.getDcTracker(transport) != null) {
            this.mPhone.getDcTracker(transport).requestNetwork(networkRequest, requestType, onCompleteMsg);
        }
    }

    /* access modifiers changed from: protected */
    public void releaseNetworkInternal(NetworkRequest networkRequest, int releaseType, int transport) {
        if (this.mPhone.getDcTracker(transport) != null) {
            this.mPhone.getDcTracker(transport).releaseNetwork(networkRequest, releaseType);
        }
    }

    private static int getAction(boolean wasActive, boolean isActive) {
        if (!wasActive && isActive) {
            return 1;
        }
        if (!wasActive || isActive) {
            return 0;
        }
        return 2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActivePhoneSwitch() {
        for (Map.Entry<NetworkRequest, Integer> entry : this.mNetworkRequests.entrySet()) {
            NetworkRequest networkRequest = entry.getKey();
            int i = -1;
            boolean applied = entry.getValue().intValue() != -1;
            boolean shouldApply = this.mPhoneSwitcher.shouldApplyNetworkRequest(networkRequest, this.mPhone.getPhoneId());
            int action = getAction(applied, shouldApply);
            if (action != 0 && !mtkIgnoreCapabilityCheck(networkRequest.networkCapabilities, getAction(applied, shouldApply))) {
                StringBuilder sb = new StringBuilder();
                sb.append("onActivePhoneSwitch: ");
                sb.append(action == 1 ? "Requesting" : "Releasing");
                sb.append(" network request ");
                sb.append(networkRequest);
                logl(sb.toString());
                int transportType = getTransportTypeFromNetworkRequest(networkRequest);
                if (action == 1) {
                    requestNetworkInternal(networkRequest, 1, getTransportTypeFromNetworkRequest(networkRequest), null);
                } else if (action == 2) {
                    releaseNetworkInternal(networkRequest, 2, getTransportTypeFromNetworkRequest(networkRequest));
                }
                Map<NetworkRequest, Integer> map = this.mNetworkRequests;
                if (shouldApply) {
                    i = transportType;
                }
                map.put(networkRequest, Integer.valueOf(i));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSubIdChange() {
        int newSubscriptionId = this.mSubscriptionController.getSubIdUsingPhoneId(this.mPhone.getPhoneId());
        if (this.mSubscriptionId != newSubscriptionId) {
            log("onSubIdChange " + this.mSubscriptionId + "->" + newSubscriptionId);
            this.mSubscriptionId = newSubscriptionId;
            setCapabilityFilter(makeNetworkFilter(this.mSubscriptionId));
        }
    }

    public void needNetworkFor(NetworkRequest networkRequest, int score) {
        Message msg = this.mInternalHandler.obtainMessage(3);
        msg.obj = networkRequest;
        msg.sendToTarget();
    }

    /* access modifiers changed from: protected */
    public void onNeedNetworkFor(Message msg) {
        int i;
        NetworkRequest networkRequest = (NetworkRequest) msg.obj;
        boolean shouldApply = this.mPhoneSwitcher.shouldApplyNetworkRequest(networkRequest, this.mPhone.getPhoneId());
        Map<NetworkRequest, Integer> map = this.mNetworkRequests;
        if (shouldApply) {
            i = getTransportTypeFromNetworkRequest(networkRequest);
        } else {
            i = -1;
        }
        map.put(networkRequest, Integer.valueOf(i));
        logl("onNeedNetworkFor " + networkRequest + " shouldApply " + shouldApply);
        if (shouldApply || mtkIgnoreCapabilityCheck(networkRequest.networkCapabilities, 1)) {
            requestNetworkInternal(networkRequest, 1, getTransportTypeFromNetworkRequest(networkRequest), null);
        }
    }

    public void releaseNetworkFor(NetworkRequest networkRequest) {
        Message msg = this.mInternalHandler.obtainMessage(4);
        msg.obj = networkRequest;
        msg.sendToTarget();
    }

    /* access modifiers changed from: protected */
    public void onReleaseNetworkFor(Message msg) {
        NetworkRequest networkRequest = (NetworkRequest) msg.obj;
        boolean applied = this.mNetworkRequests.get(networkRequest).intValue() != -1;
        this.mNetworkRequests.remove(networkRequest);
        logl("onReleaseNetworkFor " + networkRequest + " applied " + applied);
        if (applied) {
            releaseNetworkInternal(networkRequest, 1, getTransportTypeFromNetworkRequest(networkRequest));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDataHandoverNeeded(int apnType, int targetTransport, TransportManager.HandoverParams handoverParams) {
        log("onDataHandoverNeeded: apnType=" + ApnSetting.getApnTypeString(apnType) + ", target transport=" + AccessNetworkConstants.transportTypeToString(targetTransport));
        if (this.mTransportManager.getCurrentTransport(apnType) == targetTransport) {
            log("APN type " + ApnSetting.getApnTypeString(apnType) + " is already on " + AccessNetworkConstants.transportTypeToString(targetTransport));
            return;
        }
        boolean handoverPending = false;
        Iterator<Map.Entry<NetworkRequest, Integer>> it = this.mNetworkRequests.entrySet().iterator();
        while (true) {
            boolean applied = false;
            if (!it.hasNext()) {
                break;
            }
            Map.Entry<NetworkRequest, Integer> entry = it.next();
            NetworkRequest networkRequest = entry.getKey();
            int currentTransport = entry.getValue().intValue();
            if (currentTransport != -1) {
                applied = true;
            }
            if (ApnContext.getApnTypeFromNetworkRequest(networkRequest) == apnType && applied && currentTransport != targetTransport) {
                DcTracker dcTracker = this.mPhone.getDcTracker(currentTransport);
                if (dcTracker != null) {
                    DataConnection dc = dcTracker.getDataConnectionByApnType(ApnSetting.getApnTypeString(apnType));
                    if (dc == null || (!dc.isActive() && !dc.isActivating())) {
                        log("The network request is on transport " + AccessNetworkConstants.transportTypeToString(currentTransport) + ", but no live data connection. Just move the request to transport " + AccessNetworkConstants.transportTypeToString(targetTransport) + ", dc=" + dc);
                        releaseNetworkInternal(networkRequest, 1, currentTransport);
                        requestNetworkInternal(networkRequest, 1, targetTransport, null);
                    } else {
                        Message onCompleteMsg = this.mInternalHandler.obtainMessage(6);
                        onCompleteMsg.getData().putParcelable(DcTracker.DATA_COMPLETE_MSG_EXTRA_NETWORK_REQUEST, networkRequest);
                        this.mPendingHandovers.put(onCompleteMsg, handoverParams);
                        requestNetworkInternal(networkRequest, 2, targetTransport, onCompleteMsg);
                        handoverPending = true;
                    }
                } else {
                    log("DcTracker on " + AccessNetworkConstants.transportTypeToString(currentTransport) + " is not available.");
                }
            }
        }
        if (!handoverPending) {
            log("No handover request pending. Handover process is now completed");
            handoverParams.callback.onCompleted(true, false);
        }
    }

    /* access modifiers changed from: protected */
    public void onDataHandoverSetupCompleted(NetworkRequest networkRequest, boolean success, int targetTransport, boolean fallback, TransportManager.HandoverParams handoverParams) {
        int originTransport;
        log("onDataHandoverSetupCompleted: " + networkRequest + ", success=" + success + ", targetTransport=" + AccessNetworkConstants.transportTypeToString(targetTransport) + ", fallback=" + fallback);
        if (!fallback) {
            int releaseType = 1;
            if (targetTransport == 1) {
                originTransport = 2;
            } else {
                originTransport = 1;
            }
            if (success) {
                releaseType = 3;
            }
            releaseNetworkInternal(networkRequest, releaseType, originTransport);
            this.mNetworkRequests.put(networkRequest, Integer.valueOf(targetTransport));
        }
        handoverParams.callback.onCompleted(success, fallback);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(this.LOG_TAG, s);
    }

    /* access modifiers changed from: protected */
    public void logl(String s) {
        log(s);
        this.mLocalLog.log(s);
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        pw.println("Network Requests:");
        pw.increaseIndent();
        for (Map.Entry<NetworkRequest, Integer> entry : this.mNetworkRequests.entrySet()) {
            int transport = entry.getValue().intValue();
            StringBuilder sb = new StringBuilder();
            sb.append(entry.getKey());
            sb.append(transport != -1 ? " applied on " + transport : " not applied");
            pw.println(sb.toString());
        }
        this.mLocalLog.dump(fd, pw, args);
        pw.decreaseIndent();
    }

    /* access modifiers changed from: protected */
    public boolean mtkIgnoreCapabilityCheck(NetworkCapabilities capabilities, int action) {
        return false;
    }
}
