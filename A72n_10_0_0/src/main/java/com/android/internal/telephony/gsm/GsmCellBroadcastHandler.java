package com.android.internal.telephony.gsm;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import android.telephony.CellLocation;
import android.telephony.SmsCbLocation;
import android.telephony.SmsCbMessage;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import com.android.internal.telephony.CellBroadcastHandler;
import com.android.internal.telephony.Phone;
import java.util.HashMap;
import java.util.Iterator;

public class GsmCellBroadcastHandler extends CellBroadcastHandler {
    private static final boolean VDBG = false;
    protected final HashMap<SmsCbConcatInfo, byte[][]> mSmsCbPageMap = new HashMap<>(4);

    protected GsmCellBroadcastHandler(Context context, Phone phone) {
        super("GsmCellBroadcastHandler", context, phone);
        phone.mCi.setOnNewGsmBroadcastSms(getHandler(), 1, null);
    }

    protected GsmCellBroadcastHandler(String debugTag, Context context, Phone phone, Object dummy) {
        super(debugTag, context, phone, dummy);
        phone.mCi.setOnNewGsmBroadcastSms(getHandler(), 1, null);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.WakeLockStateMachine
    public void onQuitting() {
        this.mPhone.mCi.unSetOnNewGsmBroadcastSms(getHandler());
        super.onQuitting();
    }

    public static GsmCellBroadcastHandler makeGsmCellBroadcastHandler(Context context, Phone phone) {
        GsmCellBroadcastHandler handler = new GsmCellBroadcastHandler(context, phone);
        handler.start();
        return handler;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.CellBroadcastHandler, com.android.internal.telephony.WakeLockStateMachine
    public boolean handleSmsMessage(Message message) {
        SmsCbMessage cbMessage;
        try {
            if (!(message.obj instanceof AsyncResult) || (cbMessage = handleGsmBroadcastSms((AsyncResult) message.obj)) == null) {
                return super.handleSmsMessage(message);
            }
            handleBroadcastSms(cbMessage);
            return true;
        } catch (Exception e) {
            log("GsmCellBroadcastHandler--error");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x005a A[Catch:{ RuntimeException -> 0x00bb }] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x008f A[Catch:{ RuntimeException -> 0x00bb }] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00a4 A[Catch:{ RuntimeException -> 0x00bb }] */
    public SmsCbMessage handleGsmBroadcastSms(AsyncResult ar) {
        SmsCbLocation location;
        int pageCount;
        byte[][] pdus;
        Iterator<SmsCbConcatInfo> iter;
        try {
            byte[] receivedPdu = (byte[]) ar.result;
            SmsCbHeader header = new SmsCbHeader(receivedPdu);
            String plmn = TelephonyManager.from(this.mContext).getNetworkOperatorForPhone(this.mPhone.getPhoneId());
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
                    SmsCbConcatInfo concatInfo = new SmsCbConcatInfo(header, location);
                    pdus = this.mSmsCbPageMap.get(concatInfo);
                    if (pdus == null) {
                        pdus = new byte[pageCount][];
                        this.mSmsCbPageMap.put(concatInfo, pdus);
                    }
                    pdus[header.getPageIndex() - 1] = receivedPdu;
                    for (byte[] pdu : pdus) {
                        if (pdu == null) {
                            log("still missing pdu");
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
                return GsmSmsCbMessage.createSmsCbMessage(this.mContext, header, location, pdus);
            }
            location = new SmsCbLocation(plmn, lac, cid);
            pageCount = header.getNumberOfPages();
            if (pageCount <= 1) {
            }
            iter = this.mSmsCbPageMap.keySet().iterator();
            while (iter.hasNext()) {
            }
            return GsmSmsCbMessage.createSmsCbMessage(this.mContext, header, location, pdus);
        } catch (RuntimeException e) {
            loge("Error in decoding SMS CB pdu", e);
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public static final class SmsCbConcatInfo {
        private final SmsCbHeader mHeader;
        private final SmsCbLocation mLocation;

        public SmsCbConcatInfo(SmsCbHeader header, SmsCbLocation location) {
            this.mHeader = header;
            this.mLocation = location;
        }

        public int hashCode() {
            return (this.mHeader.getSerialNumber() * 31) + this.mLocation.hashCode();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof SmsCbConcatInfo)) {
                return false;
            }
            SmsCbConcatInfo other = (SmsCbConcatInfo) obj;
            if (this.mHeader.getSerialNumber() == other.mHeader.getSerialNumber() && this.mLocation.equals(other.mLocation) && this.mHeader.getServiceCategory() == other.mHeader.getServiceCategory()) {
                return true;
            }
            return false;
        }

        public boolean matchesLocation(String plmn, int lac, int cid) {
            return this.mLocation.isInLocationArea(plmn, lac, cid);
        }
    }
}
