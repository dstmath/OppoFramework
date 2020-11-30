package com.mediatek.internal.telephony.gsm;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.CellLocation;
import android.telephony.SmsCbLocation;
import android.telephony.SmsCbMessage;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.WakeLockStateMachine;
import com.android.internal.telephony.gsm.GsmCellBroadcastHandler;
import com.android.internal.telephony.gsm.GsmSmsCbMessage;
import com.mediatek.internal.telephony.MtkEtwsNotification;
import java.util.Iterator;

public class MtkGsmCellBroadcastHandler extends GsmCellBroadcastHandler {
    protected static final int EVENT_NEW_ETWS_NOTIFICATION = 2000;
    private static final boolean VDBG = false;

    public MtkGsmCellBroadcastHandler(Context context, Phone phone) {
        super("MtkGsmCellBroadcastHandler", context, phone, (Object) null);
        this.mDefaultState = new DefaultStateEx();
        this.mIdleState = new IdleStateEx();
        this.mWaitingState = new WaitingStateEx();
        addState(this.mDefaultState);
        addState(this.mIdleState, this.mDefaultState);
        addState(this.mWaitingState, this.mDefaultState);
        setInitialState(this.mIdleState);
        phone.mCi.setOnEtwsNotification(getHandler(), 2000, null);
    }

    /* access modifiers changed from: protected */
    public void onQuitting() {
        this.mPhone.mCi.unSetOnEtwsNotification(getHandler());
        MtkGsmCellBroadcastHandler.super.onQuitting();
    }

    class DefaultStateEx extends WakeLockStateMachine.DefaultState {
        DefaultStateEx() {
            super(MtkGsmCellBroadcastHandler.this);
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            return MtkGsmCellBroadcastHandler.super.processMessage(msg);
        }
    }

    class IdleStateEx extends WakeLockStateMachine.IdleState {
        IdleStateEx() {
            super(MtkGsmCellBroadcastHandler.this);
        }

        public boolean processMessage(Message msg) {
            if (msg.what != 2000) {
                return MtkGsmCellBroadcastHandler.super.processMessage(msg);
            }
            MtkGsmCellBroadcastHandler.this.log("receive ETWS notification");
            if (!MtkGsmCellBroadcastHandler.this.handleEtwsPrimaryNotification(msg)) {
                return true;
            }
            MtkGsmCellBroadcastHandler mtkGsmCellBroadcastHandler = MtkGsmCellBroadcastHandler.this;
            mtkGsmCellBroadcastHandler.transitionTo(mtkGsmCellBroadcastHandler.mWaitingState);
            return true;
        }
    }

    class WaitingStateEx extends WakeLockStateMachine.WaitingState {
        WaitingStateEx() {
            super(MtkGsmCellBroadcastHandler.this);
        }

        public boolean processMessage(Message msg) {
            if (msg.what != 2000) {
                return MtkGsmCellBroadcastHandler.super.processMessage(msg);
            }
            MtkGsmCellBroadcastHandler.this.log("deferring message until return to idle");
            MtkGsmCellBroadcastHandler.this.deferMessage(msg);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x005a A[Catch:{ RuntimeException -> 0x00cb }] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x008a A[Catch:{ RuntimeException -> 0x00cb }] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x009f A[Catch:{ RuntimeException -> 0x00cb }] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00bd A[Catch:{ RuntimeException -> 0x00cb }] */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00c4 A[Catch:{ RuntimeException -> 0x00cb }] */
    public SmsCbMessage handleGsmBroadcastSms(AsyncResult ar) {
        SmsCbLocation location;
        int pageCount;
        byte[][] pdus;
        Iterator<GsmCellBroadcastHandler.SmsCbConcatInfo> iter;
        try {
            byte[] receivedPdu = (byte[]) ar.result;
            String plmn = TelephonyManager.from(this.mContext).getNetworkOperatorForPhone(this.mPhone.getPhoneId());
            MtkSmsCbHeader header = new MtkSmsCbHeader(receivedPdu, plmn, false);
            int lac = -1;
            int cid = -1;
            CellLocation cl = this.mPhone.getCellLocation();
            if (cl instanceof GsmCellLocation) {
                GsmCellLocation cellLocation = (GsmCellLocation) cl;
                lac = cellLocation.getLac();
                cid = cellLocation.getCid();
            }
            int geographicalScope = header.getGeographicalScope();
            if (geographicalScope != 0) {
                if (geographicalScope == 2) {
                    location = new SmsCbLocation(plmn, lac, -1);
                } else if (geographicalScope != 3) {
                    location = new SmsCbLocation(plmn);
                }
                pageCount = header.getNumberOfPages();
                if (pageCount <= 1) {
                    GsmCellBroadcastHandler.SmsCbConcatInfo concatInfo = new GsmCellBroadcastHandler.SmsCbConcatInfo(header, location);
                    pdus = (byte[][]) this.mSmsCbPageMap.get(concatInfo);
                    if (pdus == null) {
                        pdus = new byte[pageCount][];
                        this.mSmsCbPageMap.put(concatInfo, pdus);
                    }
                    pdus[header.getPageIndex() - 1] = receivedPdu;
                    for (byte[] pdu : pdus) {
                        if (pdu == null) {
                            return null;
                        }
                    }
                    this.mSmsCbPageMap.remove(concatInfo);
                } else {
                    pdus = new byte[][]{receivedPdu};
                }
                iter = this.mSmsCbPageMap.keySet().iterator();
                while (iter.hasNext()) {
                    if (!iter.next().matchesLocation(plmn, lac, cid)) {
                        iter.remove();
                    }
                }
                if (!SystemProperties.get("ro.vendor.enable.geo.fencing").equals("1")) {
                    return MtkGsmSmsCbMessage.createSmsCbMessage(this.mContext, header, location, pdus);
                }
                return GsmSmsCbMessage.createSmsCbMessage(this.mContext, header, location, pdus);
            }
            location = new SmsCbLocation(plmn, lac, cid);
            pageCount = header.getNumberOfPages();
            if (pageCount <= 1) {
            }
            iter = this.mSmsCbPageMap.keySet().iterator();
            while (iter.hasNext()) {
            }
            if (!SystemProperties.get("ro.vendor.enable.geo.fencing").equals("1")) {
            }
        } catch (RuntimeException e) {
            loge("Error in decoding SMS CB pdu", e);
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean handleEtwsPrimaryNotification(Message message) {
        if (!(message.obj instanceof AsyncResult)) {
            return false;
        }
        MtkEtwsNotification noti = (MtkEtwsNotification) ((AsyncResult) message.obj).result;
        log(noti.toString());
        SmsCbMessage etwsPrimary = handleEtwsPdu(noti.getEtwsPdu(), noti.plmnId);
        if (etwsPrimary == null) {
            return false;
        }
        log("ETWS Primary dispatch to App");
        handleBroadcastSms(etwsPrimary);
        return true;
    }

    private SmsCbMessage handleEtwsPdu(byte[] pdu, String plmn) {
        SmsCbLocation location;
        if (pdu == null || pdu.length != 56) {
            log("invalid ETWS PDU");
            return null;
        }
        MtkSmsCbHeader header = new MtkSmsCbHeader(pdu, plmn, true);
        int lac = -1;
        int cid = -1;
        CellLocation cl = this.mPhone.getCellLocation();
        if (cl instanceof GsmCellLocation) {
            GsmCellLocation cellLocation = (GsmCellLocation) cl;
            lac = cellLocation.getLac();
            cid = cellLocation.getCid();
        }
        int geographicalScope = header.getGeographicalScope();
        if (geographicalScope != 0) {
            if (geographicalScope == 2) {
                location = new SmsCbLocation(plmn, lac, -1);
            } else if (geographicalScope != 3) {
                location = new SmsCbLocation(plmn);
            }
            return GsmSmsCbMessage.createSmsCbMessage(this.mContext, header, location, new byte[][]{pdu});
        }
        location = new SmsCbLocation(plmn, lac, cid);
        return GsmSmsCbMessage.createSmsCbMessage(this.mContext, header, location, new byte[][]{pdu});
    }

    public static MtkGsmCellBroadcastHandler makeGsmCellBroadcastHandler(Context context, Phone phone) {
        MtkGsmCellBroadcastHandler handler = new MtkGsmCellBroadcastHandler(context, phone);
        handler.start();
        return handler;
    }
}
