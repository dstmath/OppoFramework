package com.android.internal.telephony.dataconnection;

import android.net.INetworkPolicyListener;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkPolicyManager;
import android.net.NetworkUtils;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.DataFailCause;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.telephony.data.DataCallResponse;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.InboundSmsTracker;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.dataconnection.DataConnection;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class DcController extends StateMachine {
    protected static final boolean DBG = true;
    protected static final boolean VDBG = Rlog.DATA_VDBG;
    protected final DataServiceManager mDataServiceManager;
    protected final HashMap<Integer, DataConnection> mDcListActiveByCid = new HashMap<>();
    protected final ArrayList<DataConnection> mDcListAll = new ArrayList<>();
    /* access modifiers changed from: private */
    public final DcTesterDeactivateAll mDcTesterDeactivateAll;
    protected DccDefaultState mDccDefaultState = new DccDefaultState();
    protected final DcTracker mDct;
    /* access modifiers changed from: private */
    public volatile boolean mExecutingCarrierChange;
    /* access modifiers changed from: private */
    public final INetworkPolicyListener mListener = new NetworkPolicyManager.Listener() {
        /* class com.android.internal.telephony.dataconnection.DcController.AnonymousClass2 */

        public void onSubscriptionOverride(int subId, int overrideMask, int overrideValue) {
            HashMap<Integer, DataConnection> dcListActiveByCid;
            if (DcController.this.mPhone != null && DcController.this.mPhone.getSubId() == subId) {
                synchronized (DcController.this.mDcListAll) {
                    dcListActiveByCid = new HashMap<>(DcController.this.mDcListActiveByCid);
                }
                for (DataConnection dc : dcListActiveByCid.values()) {
                    dc.onSubscriptionOverride(overrideMask, overrideValue);
                }
            }
        }
    };
    final NetworkPolicyManager mNetworkPolicyManager;
    protected final Phone mPhone;
    protected PhoneStateListener mPhoneStateListener;
    protected final TelephonyManager mTelephonyManager;

    public DcController(String name, Phone phone, DcTracker dct, DataServiceManager dataServiceManager, Handler handler) {
        super(name, handler);
        DcTesterDeactivateAll dcTesterDeactivateAll;
        setLogRecSize(300);
        log("E ctor");
        this.mPhone = phone;
        this.mDct = dct;
        this.mDataServiceManager = dataServiceManager;
        mtkReplaceStates();
        addState(this.mDccDefaultState);
        setInitialState(this.mDccDefaultState);
        log("X ctor");
        this.mPhoneStateListener = new PhoneStateListener(handler.getLooper()) {
            /* class com.android.internal.telephony.dataconnection.DcController.AnonymousClass1 */

            public void onCarrierNetworkChange(boolean active) {
                boolean unused = DcController.this.mExecutingCarrierChange = active;
            }
        };
        this.mTelephonyManager = (TelephonyManager) phone.getContext().getSystemService("phone");
        this.mNetworkPolicyManager = (NetworkPolicyManager) phone.getContext().getSystemService("netpolicy");
        if (Build.IS_DEBUGGABLE) {
            dcTesterDeactivateAll = new DcTesterDeactivateAll(this.mPhone, this, getHandler());
        } else {
            dcTesterDeactivateAll = null;
        }
        this.mDcTesterDeactivateAll = dcTesterDeactivateAll;
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager != null) {
            telephonyManager.listen(this.mPhoneStateListener, InboundSmsTracker.DEST_PORT_FLAG_NO_PORT);
        }
    }

    public static DcController makeDcc(Phone phone, DcTracker dct, DataServiceManager dataServiceManager, Handler handler, String tagSuffix) {
        TelephonyComponentFactory inject = TelephonyComponentFactory.getInstance().inject(TelephonyComponentFactory.class.getName());
        return inject.makeDcController("Dcc" + tagSuffix, phone, dct, dataServiceManager, handler);
    }

    /* access modifiers changed from: package-private */
    public void dispose() {
        log("dispose: call quiteNow()");
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager != null) {
            telephonyManager.listen(this.mPhoneStateListener, 0);
        }
        quitNow();
    }

    public void addDc(DataConnection dc) {
        synchronized (this.mDcListAll) {
            this.mDcListAll.add(dc);
        }
    }

    public void removeDc(DataConnection dc) {
        synchronized (this.mDcListAll) {
            this.mDcListActiveByCid.remove(Integer.valueOf(dc.mCid));
            this.mDcListAll.remove(dc);
        }
    }

    public void addActiveDcByCid(DataConnection dc) {
        if (dc.mCid < 0) {
            log("addActiveDcByCid dc.mCid < 0 dc=" + dc);
        }
        synchronized (this.mDcListAll) {
            this.mDcListActiveByCid.put(Integer.valueOf(dc.mCid), dc);
        }
    }

    public DataConnection getActiveDcByCid(int cid) {
        DataConnection dataConnection;
        synchronized (this.mDcListAll) {
            dataConnection = this.mDcListActiveByCid.get(Integer.valueOf(cid));
        }
        return dataConnection;
    }

    /* access modifiers changed from: protected */
    public void removeActiveDcByCid(DataConnection dc) {
        synchronized (this.mDcListAll) {
            if (this.mDcListActiveByCid.remove(Integer.valueOf(dc.mCid)) == null) {
                log("removeActiveDcByCid removedDc=null dc=" + dc);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isExecutingCarrierChange() {
        return this.mExecutingCarrierChange;
    }

    protected class DccDefaultState extends State {
        protected DccDefaultState() {
        }

        public void enter() {
            if (DcController.this.mPhone != null && DcController.this.mDataServiceManager.getTransportType() == 1) {
                DcController.this.mPhone.mCi.registerForRilConnected(DcController.this.getHandler(), DataConnection.EVENT_RIL_CONNECTED, null);
            }
            DcController.this.mDataServiceManager.registerForDataCallListChanged(DcController.this.getHandler(), DataConnection.EVENT_DATA_STATE_CHANGED);
            if (DcController.this.mNetworkPolicyManager != null) {
                DcController.this.mNetworkPolicyManager.registerListener(DcController.this.mListener);
            }
        }

        public void exit() {
            boolean z = false;
            boolean z2 = DcController.this.mPhone != null;
            if (DcController.this.mDataServiceManager.getTransportType() == 1) {
                z = true;
            }
            if (z2 && z) {
                DcController.this.mPhone.mCi.unregisterForRilConnected(DcController.this.getHandler());
            }
            DcController.this.mDataServiceManager.unregisterForDataCallListChanged(DcController.this.getHandler());
            if (DcController.this.mDcTesterDeactivateAll != null) {
                DcController.this.mDcTesterDeactivateAll.dispose();
            }
            if (DcController.this.mNetworkPolicyManager != null) {
                DcController.this.mNetworkPolicyManager.unregisterListener(DcController.this.mListener);
            }
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 262149) {
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    DcController dcController = DcController.this;
                    dcController.log("DccDefaultState: msg.what=EVENT_RIL_CONNECTED mRilVersion=" + ar.result);
                    return true;
                }
                DcController.this.log("DccDefaultState: Unexpected exception on EVENT_RIL_CONNECTED");
                return true;
            } else if (i != 262151) {
                return true;
            } else {
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.exception == null) {
                    onDataStateChanged((ArrayList) ar2.result);
                } else {
                    DcController.this.log("DccDefaultState: EVENT_DATA_STATE_CHANGED: exception; likely radio not available, ignore");
                }
                DcController.this.mDct.oppoWlanAssistantMeasureForDataStateChanged();
                return true;
            }
        }

        /* access modifiers changed from: protected */
        public void onDataStateChanged(ArrayList<DataCallResponse> dcsList) {
            ArrayList<DataConnection> dcListAll;
            HashMap<Integer, DataConnection> dcListActiveByCid;
            HashMap<Integer, DataConnection> dcListActiveByCid2;
            HashMap<Integer, DataCallResponse> dataCallResponseListByCid;
            ArrayList<DataConnection> dcListAll2;
            synchronized (DcController.this.mDcListAll) {
                dcListAll = new ArrayList<>(DcController.this.mDcListAll);
                dcListActiveByCid = new HashMap<>(DcController.this.mDcListActiveByCid);
            }
            DcController.this.lr("onDataStateChanged: dcsList=" + dcsList + " dcListActiveByCid=" + dcListActiveByCid);
            if (DcController.VDBG) {
                DcController.this.log("onDataStateChanged: mDcListAll=" + dcListAll);
            }
            HashMap<Integer, DataCallResponse> dataCallResponseListByCid2 = new HashMap<>();
            Iterator<DataCallResponse> it = dcsList.iterator();
            while (it.hasNext()) {
                DataCallResponse dcs = it.next();
                dataCallResponseListByCid2.put(Integer.valueOf(dcs.getId()), dcs);
            }
            ArrayList<DataConnection> dcsToRetry = new ArrayList<>();
            for (DataConnection dc : dcListActiveByCid.values()) {
                if (dataCallResponseListByCid2.get(Integer.valueOf(dc.mCid)) == null) {
                    DcController.this.log("onDataStateChanged: add to retry dc=" + dc);
                    dcsToRetry.add(dc);
                }
            }
            DcController.this.log("onDataStateChanged: dcsToRetry=" + dcsToRetry);
            ArrayList<ApnContext> apnsToCleanup = new ArrayList<>();
            boolean isAnyDataCallDormant = false;
            boolean isAnyDataCallActive = false;
            Iterator<DataCallResponse> it2 = dcsList.iterator();
            while (it2.hasNext()) {
                DataCallResponse newState = it2.next();
                DataConnection dc2 = dcListActiveByCid.get(Integer.valueOf(newState.getId()));
                if (dc2 == null) {
                    DcController.this.loge("onDataStateChanged: no associated DC yet, ignore");
                } else {
                    List<ApnContext> apnContexts = dc2.getApnContexts();
                    if (apnContexts.size() == 0) {
                        DcController.this.loge("onDataStateChanged: no connected apns, ignore");
                        dcListAll2 = dcListAll;
                        dataCallResponseListByCid = dataCallResponseListByCid2;
                        dcListActiveByCid2 = dcListActiveByCid;
                    } else {
                        DcController.this.log("onDataStateChanged: Found ConnId=" + newState.getId() + " newState=" + newState.toString());
                        if (newState.getLinkStatus() != 0) {
                            dcListAll2 = dcListAll;
                            DataConnection.UpdateLinkPropertyResult result = dc2.updateLinkProperty(newState);
                            if (result.oldLp.equals(result.newLp)) {
                                DcController.this.log("onDataStateChanged: no change");
                                dataCallResponseListByCid = dataCallResponseListByCid2;
                                dcListActiveByCid2 = dcListActiveByCid;
                            } else if (!result.oldLp.isIdenticalInterfaceName(result.newLp)) {
                                dataCallResponseListByCid = dataCallResponseListByCid2;
                                dcListActiveByCid2 = dcListActiveByCid;
                                apnsToCleanup.addAll(apnContexts);
                                DcController.this.log("onDataStateChanged: interface change, cleanup apns=" + apnContexts);
                            } else if (!result.oldLp.isIdenticalDnses(result.newLp) || !result.oldLp.isIdenticalRoutes(result.newLp) || !result.oldLp.isIdenticalHttpProxy(result.newLp) || !result.oldLp.isIdenticalAddresses(result.newLp)) {
                                LinkProperties.CompareResult<LinkAddress> car = result.oldLp.compareAddresses(result.newLp);
                                DcController dcController = DcController.this;
                                StringBuilder sb = new StringBuilder();
                                dataCallResponseListByCid = dataCallResponseListByCid2;
                                sb.append("onDataStateChanged: oldLp=");
                                sb.append(result.oldLp);
                                sb.append(" newLp=");
                                sb.append(result.newLp);
                                sb.append(" car=");
                                sb.append(car);
                                dcController.log(sb.toString());
                                boolean needToClean = false;
                                for (LinkAddress added : car.added) {
                                    Iterator it3 = car.removed.iterator();
                                    while (true) {
                                        if (!it3.hasNext()) {
                                            break;
                                        } else if (NetworkUtils.addressTypeMatches(((LinkAddress) it3.next()).getAddress(), added.getAddress())) {
                                            needToClean = true;
                                            break;
                                        } else {
                                            it3 = it3;
                                        }
                                    }
                                    dcListActiveByCid = dcListActiveByCid;
                                }
                                dcListActiveByCid2 = dcListActiveByCid;
                                if (needToClean) {
                                    DcController.this.log("onDataStateChanged: addr change, cleanup apns=" + apnContexts + " oldLp=" + result.oldLp + " newLp=" + result.newLp);
                                    apnsToCleanup.addAll(apnContexts);
                                } else {
                                    DcController.this.log("onDataStateChanged: simple change");
                                    for (ApnContext apnContext : apnContexts) {
                                        DcController.this.mPhone.notifyDataConnection(apnContext.getApnType());
                                    }
                                }
                            } else {
                                DcController.this.log("onDataStateChanged: no changes");
                                dataCallResponseListByCid = dataCallResponseListByCid2;
                                dcListActiveByCid2 = dcListActiveByCid;
                            }
                        } else if (DcController.this.mDct.isCleanupRequired.get()) {
                            apnsToCleanup.addAll(apnContexts);
                            DcController.this.mDct.isCleanupRequired.set(false);
                            dcListAll2 = dcListAll;
                            dataCallResponseListByCid = dataCallResponseListByCid2;
                            dcListActiveByCid2 = dcListActiveByCid;
                        } else {
                            int failCause = DataFailCause.getFailCause(newState.getCause());
                            if (DataFailCause.isRadioRestartFailure(DcController.this.mPhone.getContext(), failCause, DcController.this.mPhone.getSubId())) {
                                DcController dcController2 = DcController.this;
                                StringBuilder sb2 = new StringBuilder();
                                dcListAll2 = dcListAll;
                                sb2.append("onDataStateChanged: X restart radio, failCause=");
                                sb2.append(failCause);
                                dcController2.log(sb2.toString());
                                DcController.this.mDct.sendRestartRadio();
                            } else {
                                dcListAll2 = dcListAll;
                                if (DcController.this.mDct.isPermanentFailure(failCause)) {
                                    DcController.this.log("onDataStateChanged: inactive, add to cleanup list. failCause=" + failCause);
                                    apnsToCleanup.addAll(apnContexts);
                                } else {
                                    DcController.this.log("onDataStateChanged: inactive, add to retry list. failCause=" + failCause);
                                    dcsToRetry.add(dc2);
                                }
                            }
                            dataCallResponseListByCid = dataCallResponseListByCid2;
                            dcListActiveByCid2 = dcListActiveByCid;
                        }
                    }
                    if (newState.getLinkStatus() == 2) {
                        isAnyDataCallActive = true;
                    }
                    if (newState.getLinkStatus() == 1) {
                        isAnyDataCallDormant = true;
                    }
                    dcListAll = dcListAll2;
                    dataCallResponseListByCid2 = dataCallResponseListByCid;
                    dcListActiveByCid = dcListActiveByCid2;
                }
            }
            if (!isAnyDataCallDormant || isAnyDataCallActive) {
                DcController.this.log("onDataStateChanged: Data Activity updated to NONE. isAnyDataCallActive = " + isAnyDataCallActive + " isAnyDataCallDormant = " + isAnyDataCallDormant);
                if (isAnyDataCallActive) {
                    DcController.this.mDct.sendStartNetStatPoll(DctConstants.Activity.NONE);
                }
            } else {
                DcController.this.log("onDataStateChanged: Data Activity updated to DORMANT. stopNetStatePoll");
                DcController.this.mDct.sendStopNetStatPoll(DctConstants.Activity.DORMANT);
            }
            DcController.this.lr("onDataStateChanged: dcsToRetry=" + dcsToRetry + " apnsToCleanup=" + apnsToCleanup);
            Iterator<ApnContext> it4 = apnsToCleanup.iterator();
            while (it4.hasNext()) {
                DcController.this.mDct.cleanUpConnection(it4.next());
            }
            Iterator<DataConnection> it5 = dcsToRetry.iterator();
            while (it5.hasNext()) {
                DataConnection dc3 = it5.next();
                DcController.this.log("onDataStateChanged: send EVENT_LOST_CONNECTION dc.mTag=" + dc3.mTag);
                dc3.sendMessage(DataConnection.EVENT_LOST_CONNECTION, dc3.mTag);
            }
            if (DcController.VDBG) {
                DcController.this.log("onDataStateChanged: X");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void lr(String s) {
        logAndAddLogRec(s);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Rlog.e(getName(), s);
    }

    /* access modifiers changed from: protected */
    public String getWhatToString(int what) {
        return DataConnection.cmdToString(what);
    }

    public String toString() {
        String str;
        synchronized (this.mDcListAll) {
            str = "mDcListAll=" + this.mDcListAll + " mDcListActiveByCid=" + this.mDcListActiveByCid;
        }
        return str;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        DcController.super.dump(fd, pw, args);
        pw.println(" mPhone=" + this.mPhone);
        synchronized (this.mDcListAll) {
            pw.println(" mDcListAll=" + this.mDcListAll);
            pw.println(" mDcListActiveByCid=" + this.mDcListActiveByCid);
        }
    }

    /* access modifiers changed from: protected */
    public void mtkReplaceStates() {
    }
}
