package com.mediatek.internal.telephony.dataconnection;

import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.DataFailCause;
import android.telephony.data.DataCallResponse;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.CellularDataService;
import com.android.internal.telephony.dataconnection.DataConnection;
import com.android.internal.telephony.dataconnection.DataServiceManager;
import com.android.internal.telephony.dataconnection.DcController;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.mediatek.internal.telephony.EndcBearController;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MtkDcController extends DcController {
    private static final boolean DBG = Build.IS_DEBUGGABLE;
    private static final boolean MTK_SRLTE_SUPPORT;
    private static final boolean MTK_SVLTE_SUPPORT = (SystemProperties.getInt(PROP_MTK_CDMA_LTE_MODE, 0) == 1);
    private static final String PROP_MTK_CDMA_LTE_MODE = "ro.vendor.mtk_c2k_support";
    private CellularDataService mCellularDataService = new CellularDataService();

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_MTK_CDMA_LTE_MODE, 0) == 2) {
            z = true;
        }
        MTK_SRLTE_SUPPORT = z;
    }

    public MtkDcController(String name, Phone phone, DcTracker dct, DataServiceManager dataServiceManager, Handler handler) {
        super(name, phone, dct, dataServiceManager, handler);
    }

    public void removeDc(DataConnection dc) {
        MtkDcController.super.removeDc(dc);
        if (DBG) {
            log("removeDc: " + dc);
        }
    }

    public void addActiveDcByCid(DataConnection dc) {
        MtkDcController.super.addActiveDcByCid(dc);
        if (DBG) {
            log("addActiveDcByCid: " + dc);
        }
    }

    /* access modifiers changed from: protected */
    public void removeActiveDcByCid(DataConnection dc) {
        MtkDcController.super.removeActiveDcByCid(dc);
        if (DBG) {
            log("removeActiveDcByCid: " + dc);
        }
    }

    protected class MtkDccDefaultState extends DcController.DccDefaultState {
        protected MtkDccDefaultState() {
            super(MtkDcController.this);
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 262149) {
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    if (MtkDcController.DBG) {
                        MtkDcController mtkDcController = MtkDcController.this;
                        mtkDcController.log("DccDefaultState: msg.what=EVENT_RIL_CONNECTED mRilVersion=" + ar.result);
                    }
                    MtkDcController.this.mPhone.mCi.getDataCallList(MtkDcController.this.obtainMessage(262177));
                    return true;
                }
                MtkDcController.this.log("DccDefaultState: Unexpected exception on EVENT_RIL_CONNECTED");
                return true;
            } else if (i != 262177) {
                return MtkDcController.super.processMessage(msg);
            } else {
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.exception == null) {
                    onDataStateChanged((ArrayList) ar2.result);
                    return true;
                }
                MtkDcController.this.log("DccDefaultState: EVENT_DATA_STATE_CHANGED: exception; likely radio not available, ignore");
                return true;
            }
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:189:0x05b3, code lost:
            r0 = th;
         */
        public void onDataStateChanged(ArrayList<DataCallResponse> dcsList) {
            ArrayList<DataConnection> dcListAll;
            HashMap<Integer, DataConnection> dcListActiveByCid;
            HashMap<Integer, DataCallResponse> dataCallResponseListByCid;
            ArrayList<DataConnection> dcListAll2;
            HashMap<Integer, DataConnection> dcListActiveByCid2;
            HashMap<Integer, DataCallResponse> dataCallResponseListByCid2;
            boolean needToClean;
            LinkProperties.CompareResult<LinkAddress> car;
            synchronized (MtkDcController.this.mDcListAll) {
                dcListAll = new ArrayList<>(MtkDcController.this.mDcListAll);
                dcListActiveByCid = new HashMap<>(MtkDcController.this.mDcListActiveByCid);
            }
            if (MtkDcController.DBG) {
                MtkDcController.this.lr("onDataStateChanged: dcsList=" + dcsList + " dcListActiveByCid=" + dcListActiveByCid);
            }
            if (MtkDcController.VDBG) {
                MtkDcController.this.log("onDataStateChanged: mDcListAll=" + dcListAll);
            }
            HashMap<Integer, DataCallResponse> dataCallResponseListByCid3 = new HashMap<>();
            Iterator<DataCallResponse> it = dcsList.iterator();
            while (it.hasNext()) {
                DataCallResponse dcs = it.next();
                dataCallResponseListByCid3.put(Integer.valueOf(dcs.getId()), dcs);
            }
            ArrayList<DataConnection> dcsToRetry = new ArrayList<>();
            for (DataConnection dc : dcListActiveByCid.values()) {
                if (dataCallResponseListByCid3.get(Integer.valueOf(dc.mCid)) == null) {
                    if (MtkDcController.DBG) {
                        MtkDcController.this.log("onDataStateChanged: add to retry dc=" + dc);
                    }
                    dcsToRetry.add(dc);
                }
            }
            if (MtkDcController.DBG) {
                MtkDcController.this.log("onDataStateChanged: dcsToRetry=" + dcsToRetry);
            }
            ArrayList<ApnContext> apnsToCleanup = new ArrayList<>();
            boolean isAnyDataCallDormant = false;
            boolean isAnyDataCallActive = false;
            Iterator<DataCallResponse> it2 = dcsList.iterator();
            while (it2.hasNext()) {
                DataCallResponse newState = it2.next();
                int dcActive = EndcBearController.INVALID_INT;
                if (MtkDcController.this.ignoreTransportModeNotMatch(newState)) {
                    MtkDcController.this.log("TransportMode not match, ignore");
                    dcListAll2 = dcListAll;
                    dataCallResponseListByCid = dataCallResponseListByCid3;
                } else {
                    DataConnection dc2 = dcListActiveByCid.get(Integer.valueOf(newState.getId()));
                    if (dc2 == null) {
                        MtkDcController.this.loge("onDataStateChanged: no associated DC yet, ignore");
                        if (MtkDcController.this.mDct.getState("default") == DctConstants.State.IDLE) {
                            MtkDcController.this.loge("Deactivate unlinked PDP context.");
                            MtkDcController.this.mDct.deactivatePdpByCid(newState.getId());
                            dcListAll2 = dcListAll;
                            dataCallResponseListByCid = dataCallResponseListByCid3;
                        } else {
                            MtkDcController.this.loge("Default pdn is not in IDLE state, get data call list again");
                            MtkDcController.this.mPhone.mCi.getDataCallList(MtkDcController.this.obtainMessage(262177));
                            dcListAll2 = dcListAll;
                            dataCallResponseListByCid = dataCallResponseListByCid3;
                        }
                    } else {
                        List<ApnContext> apnContexts = dc2.getApnContexts();
                        if (apnContexts.size() == 0) {
                            if (MtkDcController.DBG) {
                                MtkDcController.this.loge("onDataStateChanged: no connected apns, ignore");
                            }
                            dcListAll2 = dcListAll;
                            dataCallResponseListByCid2 = dataCallResponseListByCid3;
                            dcListActiveByCid2 = dcListActiveByCid;
                        } else {
                            if (MtkDcController.DBG) {
                                MtkDcController mtkDcController = MtkDcController.this;
                                StringBuilder sb = new StringBuilder();
                                dcListAll2 = dcListAll;
                                sb.append("onDataStateChanged: Found ConnId=");
                                sb.append(newState.getId());
                                sb.append(" newState=");
                                sb.append(newState.toString());
                                mtkDcController.log(sb.toString());
                            } else {
                                dcListAll2 = dcListAll;
                            }
                            if (DataFailCause.getFailCause(newState.getCause()) == -1100) {
                                if (MtkDcController.DBG) {
                                    MtkDcController.this.log("onDataStateChanged: trySetupDataOnEvent. EVENT_DATA_SETUP_SSC_MODE3, cid=" + newState.getId() + ", lifetime=" + newState.getSuggestedRetryTime());
                                }
                                dataCallResponseListByCid = dataCallResponseListByCid3;
                                MtkDcController.this.mDct.trySetupDataOnEvent(270864, newState.getId(), newState.getSuggestedRetryTime());
                            } else {
                                dataCallResponseListByCid2 = dataCallResponseListByCid3;
                                ((MtkDataConnection) dc2).setConnectionRat(MtkDcHelper.decodeRat(newState.getLinkStatus()), "data call list");
                                dcActive = newState.getLinkStatus() % 1000;
                                if (dcActive != 0) {
                                    DataConnection.UpdateLinkPropertyResult result = dc2.updateLinkProperty(newState);
                                    if (result.oldLp.equals(result.newLp)) {
                                        if (MtkDcController.DBG) {
                                            MtkDcController.this.log("onDataStateChanged: no change");
                                        }
                                        dcListActiveByCid2 = dcListActiveByCid;
                                    } else if (!result.oldLp.isIdenticalInterfaceName(result.newLp)) {
                                        dcListActiveByCid2 = dcListActiveByCid;
                                        apnsToCleanup.addAll(apnContexts);
                                        if (MtkDcController.DBG) {
                                            MtkDcController.this.log("onDataStateChanged: interface change, cleanup apns=" + apnContexts);
                                        }
                                    } else if (!MtkDcController.this.isDNSMatched(result.oldLp, result.newLp) || !MtkDcController.this.isRouteMatched(result.oldLp, result.newLp) || !result.oldLp.isIdenticalHttpProxy(result.newLp) || !MtkDcController.this.isIpMatched(result.oldLp, result.newLp)) {
                                        LinkProperties.CompareResult<LinkAddress> car2 = result.oldLp.compareAddresses(result.newLp);
                                        if (MtkDcController.DBG) {
                                            MtkDcController mtkDcController2 = MtkDcController.this;
                                            StringBuilder sb2 = new StringBuilder();
                                            dcListActiveByCid2 = dcListActiveByCid;
                                            sb2.append("onDataStateChanged: oldLp=");
                                            sb2.append(result.oldLp);
                                            sb2.append(" newLp=");
                                            sb2.append(result.newLp);
                                            sb2.append(" car=");
                                            sb2.append(car2);
                                            mtkDcController2.log(sb2.toString());
                                        } else {
                                            dcListActiveByCid2 = dcListActiveByCid;
                                        }
                                        boolean needToClean2 = false;
                                        for (LinkAddress added : car2.added) {
                                            Iterator it3 = car2.removed.iterator();
                                            while (true) {
                                                if (!it3.hasNext()) {
                                                    car = car2;
                                                    needToClean2 = needToClean2;
                                                    break;
                                                }
                                                car = car2;
                                                if (NetworkUtils.addressTypeMatches(((LinkAddress) it3.next()).getAddress(), added.getAddress())) {
                                                    needToClean2 = true;
                                                    break;
                                                } else {
                                                    car2 = car;
                                                    it3 = it3;
                                                }
                                            }
                                            car2 = car;
                                        }
                                        if ((MtkDcController.MTK_SVLTE_SUPPORT || MtkDcController.MTK_SRLTE_SUPPORT) && MtkDcController.this.mPhone.getPhoneType() == 2) {
                                            if (MtkDcController.DBG) {
                                                MtkDcController.this.log("onDataStateChanged: IRAT set needToClean false");
                                            }
                                            needToClean = false;
                                        } else if ("OP07".equals(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR))) {
                                            if (MtkDcController.DBG) {
                                                MtkDcController.this.log("onDataStateChanged: OP07 set needToClean false");
                                            }
                                            needToClean = false;
                                        } else {
                                            needToClean = needToClean2;
                                        }
                                        if (needToClean) {
                                            if (MtkDcController.DBG) {
                                                MtkDcController.this.log("onDataStateChanged: addr change, cleanup apns=" + apnContexts + " oldLp=" + result.oldLp + " newLp=" + result.newLp);
                                            }
                                            apnsToCleanup.addAll(apnContexts);
                                        } else {
                                            if (MtkDcController.DBG) {
                                                MtkDcController.this.log("onDataStateChanged: simple change");
                                            }
                                            for (ApnContext apnContext : apnContexts) {
                                                MtkDcController.this.mPhone.notifyDataConnection(apnContext.getApnType());
                                                result = result;
                                            }
                                        }
                                    } else if (MtkDcController.DBG) {
                                        MtkDcController.this.log("onDataStateChanged: no changes");
                                        dcListActiveByCid2 = dcListActiveByCid;
                                    } else {
                                        dcListActiveByCid2 = dcListActiveByCid;
                                    }
                                } else if (MtkDcController.this.mDct.isCleanupRequired.get()) {
                                    apnsToCleanup.addAll(apnContexts);
                                    MtkDcController.this.mDct.isCleanupRequired.set(false);
                                    dcListActiveByCid2 = dcListActiveByCid;
                                } else {
                                    int failCause = DataFailCause.getFailCause(newState.getCause());
                                    if (DataFailCause.isRadioRestartFailure(MtkDcController.this.mPhone.getContext(), failCause, MtkDcController.this.mPhone.getSubId())) {
                                        if (MtkDcController.DBG) {
                                            MtkDcController.this.log("onDataStateChanged: X restart radio, failCause=" + failCause);
                                        }
                                        MtkDcController.this.mDct.sendRestartRadio();
                                    } else if (MtkDcController.this.mDct.isPermanentFailure(failCause)) {
                                        if (MtkDcController.DBG) {
                                            MtkDcController.this.log("onDataStateChanged: inactive, add to cleanup list. failCause=" + failCause);
                                        }
                                        apnsToCleanup.addAll(apnContexts);
                                    } else {
                                        if (MtkDcController.DBG) {
                                            MtkDcController.this.log("onDataStateChanged: inactive, add to retry list. failCause=" + failCause);
                                        }
                                        dcsToRetry.add(dc2);
                                        ((MtkDataConnection) dc2).setLostConnectionCause(failCause);
                                    }
                                    dcListActiveByCid2 = dcListActiveByCid;
                                }
                            }
                        }
                        if (dcActive == 2) {
                            isAnyDataCallActive = true;
                        }
                        if (dcActive == 1) {
                            isAnyDataCallDormant = true;
                        }
                        dcListAll = dcListAll2;
                        dataCallResponseListByCid3 = dataCallResponseListByCid2;
                        dcListActiveByCid = dcListActiveByCid2;
                    }
                }
                dcListAll = dcListAll2;
                dataCallResponseListByCid3 = dataCallResponseListByCid;
            }
            if (!isAnyDataCallDormant || isAnyDataCallActive) {
                if (MtkDcController.DBG) {
                    MtkDcController.this.log("onDataStateChanged: Data Activity updated to NONE. isAnyDataCallActive = " + isAnyDataCallActive + " isAnyDataCallDormant = " + isAnyDataCallDormant);
                }
                if (isAnyDataCallActive) {
                    MtkDcController.this.mDct.sendStartNetStatPoll(DctConstants.Activity.NONE);
                }
            } else {
                if (MtkDcController.DBG) {
                    MtkDcController.this.log("onDataStateChanged: Data Activity updated to DORMANT. stopNetStatePoll");
                }
                MtkDcController.this.mDct.sendStopNetStatPoll(DctConstants.Activity.DORMANT);
            }
            if (MtkDcController.DBG) {
                MtkDcController.this.lr("onDataStateChanged: dcsToRetry=" + dcsToRetry + " apnsToCleanup=" + apnsToCleanup);
            }
            Iterator<ApnContext> it4 = apnsToCleanup.iterator();
            while (it4.hasNext()) {
                MtkDcController.this.mDct.cleanUpConnection(it4.next());
            }
            Iterator<DataConnection> it5 = dcsToRetry.iterator();
            while (it5.hasNext()) {
                DataConnection dc3 = it5.next();
                if (MtkDcController.DBG) {
                    MtkDcController.this.log("onDataStateChanged: send EVENT_LOST_CONNECTION dc.mTag=" + dc3.mTag);
                }
                dc3.sendMessage(262153, dc3.mTag);
            }
            if (MtkDcController.VDBG) {
                MtkDcController.this.log("onDataStateChanged: X");
                return;
            }
            return;
            while (true) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isIpMatched(LinkProperties oldLp, LinkProperties newLp) {
        if (oldLp.isIdenticalAddresses(newLp)) {
            return true;
        }
        if (DBG) {
            log("isIpMatched: address count is different but matched");
        }
        return newLp.getAddresses().containsAll(oldLp.getAddresses());
    }

    /* access modifiers changed from: protected */
    public void mtkReplaceStates() {
        this.mDccDefaultState = new MtkDccDefaultState();
    }

    /* access modifiers changed from: package-private */
    public boolean ignoreTransportModeNotMatch(DataCallResponse newDcr) {
        boolean ignore = false;
        if (!this.mPhone.getTransportManager().isInLegacyMode()) {
            int mRat = MtkDcHelper.decodeRat(newDcr.getLinkStatus());
            if (mRat > 0) {
                if (this.mDataServiceManager.getTransportType() == 2 && mRat == 1) {
                    ignore = true;
                }
                if (this.mDataServiceManager.getTransportType() == 1 && mRat == 2) {
                    ignore = true;
                }
            } else if (DBG) {
                log("ignoreTransportModeNotMatch: can't handle mRat=" + mRat);
            }
        }
        if (DBG) {
            log("ignoreTransportModeNotMatch: ignore=" + ignore);
        }
        return ignore;
    }

    /* access modifiers changed from: package-private */
    public int getActiveDcCount() {
        return this.mDcListActiveByCid.size();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDNSMatched(LinkProperties oldLp, LinkProperties newLp) {
        if (oldLp.isIdenticalDnses(newLp)) {
            return true;
        }
        if (DBG) {
            log("isIpMatched: address count is different but matched");
        }
        return newLp.getDnsServers().containsAll(oldLp.getDnsServers());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isRouteMatched(LinkProperties oldLp, LinkProperties newLp) {
        if (oldLp.isIdenticalRoutes(newLp)) {
            return true;
        }
        if (DBG) {
            log("isIpMatched: address count is different but matched");
        }
        return newLp.getRoutes().containsAll(oldLp.getRoutes());
    }
}
