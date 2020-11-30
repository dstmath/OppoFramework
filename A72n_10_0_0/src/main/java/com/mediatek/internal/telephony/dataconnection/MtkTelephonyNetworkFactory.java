package com.mediatek.internal.telephony.dataconnection;

import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Looper;
import android.os.Message;
import android.telephony.AccessNetworkConstants;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SubscriptionMonitor;
import com.android.internal.telephony.dataconnection.TelephonyNetworkFactory;
import com.android.internal.telephony.dataconnection.TransportManager;
import java.util.Iterator;
import java.util.Map;

public class MtkTelephonyNetworkFactory extends TelephonyNetworkFactory {
    private boolean mHasRedirectReleaseRequestDuringHandover = false;

    public MtkTelephonyNetworkFactory(SubscriptionMonitor subscriptionMonitor, Looper looper, Phone phone) {
        super(subscriptionMonitor, looper, phone);
    }

    /* access modifiers changed from: protected */
    public NetworkCapabilities makeNetworkFilter(int subscriptionId) {
        NetworkCapabilities nc = MtkTelephonyNetworkFactory.super.makeNetworkFilter(subscriptionId);
        nc.addCapability(27);
        nc.addCapability(26);
        return nc;
    }

    /* access modifiers changed from: protected */
    public boolean mtkIgnoreCapabilityCheck(NetworkCapabilities capabilities, int action) {
        if (capabilities.hasCapability(4) || capabilities.hasCapability(10)) {
            if (action != 1) {
                log("ignoreCapabilityCheck() ignore IMS/EIMS PDN");
                return true;
            } else if (MtkDcHelper.getInstance().isMultiPsAttachSupport()) {
                log("ignoreCapabilityCheck() allow IMS/EIMS pdn activation");
                return true;
            } else if (!capabilities.hasCapability(10) || TelephonyManager.getDefault().getPhoneCount() != 1) {
                log("ignoreCapabilityCheck() reject IMS/EIMS pdn activation");
                return false;
            } else {
                log("ignoreCapabilityCheck() allow IMS/EIMS pdn activation in ss load");
                return true;
            }
        } else if (!capabilities.hasCapability(26)) {
            return false;
        } else {
            log("ignoreCapabilityCheck() ignore VSIM PDN");
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void onReleaseNetworkFor(Message msg) {
        TransportManager.HandoverParams hoParams;
        NetworkRequest nr;
        NetworkRequest networkRequest = (NetworkRequest) msg.obj;
        boolean applied = ((Integer) this.mNetworkRequests.get(networkRequest)).intValue() != -1;
        this.mNetworkRequests.remove(networkRequest);
        logl("onReleaseNetworkFor " + networkRequest + " applied " + applied);
        if (applied || mtkIgnoreCapabilityCheck(networkRequest.networkCapabilities, 2)) {
            int transport = getTransportTypeFromNetworkRequest(networkRequest);
            int releaseType = 1;
            Iterator it = this.mPendingHandovers.entrySet().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Map.Entry<Message, TransportManager.HandoverParams> entry = (Map.Entry) it.next();
                Message onCompleteMsg = entry.getKey();
                if (onCompleteMsg != null && (hoParams = entry.getValue()) != null && (nr = (NetworkRequest) onCompleteMsg.getData().getParcelable("extra_network_request")) != null && networkRequest.requestId == nr.requestId && transport != hoParams.targetTransport) {
                    log("onReleaseNetworkFor handover ongoing, release " + AccessNetworkConstants.transportTypeToString(hoParams.targetTransport) + " instead of original " + AccessNetworkConstants.transportTypeToString(transport));
                    transport = hoParams.targetTransport;
                    releaseType = 3;
                    this.mHasRedirectReleaseRequestDuringHandover = true;
                    break;
                }
            }
            releaseNetworkInternal(networkRequest, releaseType, transport);
        }
    }

    /* access modifiers changed from: protected */
    public void onDataHandoverSetupCompleted(NetworkRequest networkRequest, boolean success, int targetTransport, boolean fallback, TransportManager.HandoverParams handoverParams) {
        MtkTelephonyNetworkFactory.super.onDataHandoverSetupCompleted(networkRequest, success, targetTransport, fallback, handoverParams);
        if (this.mHasRedirectReleaseRequestDuringHandover) {
            this.mHasRedirectReleaseRequestDuringHandover = false;
            if (fallback) {
                log("onDataHandoverSetupCompleted release origin network request");
                int originTransport = 1;
                if (targetTransport == 1) {
                    originTransport = 2;
                }
                releaseNetworkInternal(networkRequest, 3, originTransport);
                return;
            }
            log("onDataHandoverSetupCompleted remove network request");
            this.mNetworkRequests.remove(networkRequest);
        } else if (fallback) {
            log("onDataHandoverSetupCompleted release target network request");
            releaseNetworkInternal(networkRequest, 3, targetTransport);
        }
    }
}
