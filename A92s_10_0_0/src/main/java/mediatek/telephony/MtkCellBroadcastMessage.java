package mediatek.telephony;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.CellBroadcastMessage;
import android.telephony.SmsCbCmasInfo;
import android.telephony.SmsCbEtwsInfo;
import android.telephony.SmsCbLocation;
import android.telephony.SmsCbMessage;
import com.mediatek.internal.telephony.MtkSmsCbMessage;
import com.mediatek.internal.telephony.cat.BipUtils;
import com.mediatek.internal.telephony.gsm.MtkGsmSmsCbMessage;
import com.mediatek.internal.telephony.gsm.cbutil.Shape;
import com.mediatek.internal.telephony.gsm.cbutil.WhamTuple;
import com.mediatek.internal.telephony.ppl.IPplSmsFilter;
import java.util.ArrayList;

public class MtkCellBroadcastMessage extends CellBroadcastMessage {
    public static final Parcelable.Creator<MtkCellBroadcastMessage> CREATOR = new Parcelable.Creator<MtkCellBroadcastMessage>() {
        /* class mediatek.telephony.MtkCellBroadcastMessage.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MtkCellBroadcastMessage createFromParcel(Parcel in) {
            return new MtkCellBroadcastMessage(in);
        }

        @Override // android.os.Parcelable.Creator
        public MtkCellBroadcastMessage[] newArray(int size) {
            return new MtkCellBroadcastMessage[size];
        }
    };

    private MtkCellBroadcastMessage(int subId, SmsCbMessage message, long deliveryTime, boolean isRead) {
        this.mSubId = subId;
        this.mSmsCbMessage = message;
        this.mDeliveryTime = deliveryTime;
        this.mIsRead = isRead;
    }

    public MtkCellBroadcastMessage(SmsCbMessage message) {
        super(message);
    }

    private MtkCellBroadcastMessage(Parcel in) {
        this.mSmsCbMessage = new MtkSmsCbMessage(in);
        this.mDeliveryTime = in.readLong();
        this.mIsRead = in.readInt() != 0;
        this.mSubId = in.readInt();
    }

    public static MtkCellBroadcastMessage createFromCursor(Cursor cursor) {
        String plmn;
        int lac;
        int cid;
        SmsCbEtwsInfo etwsInfo;
        int cid2;
        int etwsWarningTypeColumn;
        int cmasMessageClassColumn;
        SmsCbCmasInfo cmasInfo;
        int cmasCategory;
        int responseType;
        int severity;
        int urgency;
        int certainty;
        int geoScope = cursor.getInt(cursor.getColumnIndexOrThrow("geo_scope"));
        int serialNum = cursor.getInt(cursor.getColumnIndexOrThrow("serial_number"));
        int category = cursor.getInt(cursor.getColumnIndexOrThrow("service_category"));
        String language = cursor.getString(cursor.getColumnIndexOrThrow("language"));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        int format = cursor.getInt(cursor.getColumnIndexOrThrow(IPplSmsFilter.KEY_FORMAT));
        int priority = cursor.getInt(cursor.getColumnIndexOrThrow("priority"));
        int plmnColumn = cursor.getColumnIndex("plmn");
        if (plmnColumn == -1 || cursor.isNull(plmnColumn)) {
            plmn = null;
        } else {
            plmn = cursor.getString(plmnColumn);
        }
        int lacColumn = cursor.getColumnIndex("lac");
        if (lacColumn == -1 || cursor.isNull(lacColumn)) {
            lac = -1;
        } else {
            lac = cursor.getInt(lacColumn);
        }
        int cidColumn = cursor.getColumnIndex(BipUtils.KEY_QOS_CID);
        if (cidColumn == -1 || cursor.isNull(cidColumn)) {
            cid = -1;
        } else {
            cid = cursor.getInt(cidColumn);
        }
        SmsCbLocation location = new SmsCbLocation(plmn, lac, cid);
        int etwsWarningTypeColumn2 = cursor.getColumnIndex("etws_warning_type");
        if (etwsWarningTypeColumn2 == -1 || cursor.isNull(etwsWarningTypeColumn2)) {
            etwsInfo = null;
        } else {
            etwsInfo = new SmsCbEtwsInfo(cursor.getInt(etwsWarningTypeColumn2), false, false, false, (byte[]) null);
        }
        int cmasMessageClassColumn2 = cursor.getColumnIndex("cmas_message_class");
        if (cmasMessageClassColumn2 == -1 || cursor.isNull(cmasMessageClassColumn2)) {
            cmasMessageClassColumn = cmasMessageClassColumn2;
            etwsWarningTypeColumn = etwsWarningTypeColumn2;
            cid2 = cid;
            cmasInfo = null;
        } else {
            int messageClass = cursor.getInt(cmasMessageClassColumn2);
            int cmasCategoryColumn = cursor.getColumnIndex("cmas_category");
            if (cmasCategoryColumn == -1 || cursor.isNull(cmasCategoryColumn)) {
                cmasCategory = -1;
            } else {
                cmasCategory = cursor.getInt(cmasCategoryColumn);
            }
            int cmasResponseTypeColumn = cursor.getColumnIndex("cmas_response_type");
            if (cmasResponseTypeColumn == -1 || cursor.isNull(cmasResponseTypeColumn)) {
                responseType = -1;
            } else {
                responseType = cursor.getInt(cmasResponseTypeColumn);
            }
            int cmasSeverityColumn = cursor.getColumnIndex("cmas_severity");
            cmasMessageClassColumn = cmasMessageClassColumn2;
            if (cmasSeverityColumn == -1 || cursor.isNull(cmasSeverityColumn)) {
                severity = -1;
            } else {
                severity = cursor.getInt(cmasSeverityColumn);
            }
            int cmasUrgencyColumn = cursor.getColumnIndex("cmas_urgency");
            etwsWarningTypeColumn = etwsWarningTypeColumn2;
            if (cmasUrgencyColumn == -1 || cursor.isNull(cmasUrgencyColumn)) {
                urgency = -1;
            } else {
                urgency = cursor.getInt(cmasUrgencyColumn);
            }
            int cmasCertaintyColumn = cursor.getColumnIndex("cmas_certainty");
            cid2 = cid;
            if (cmasCertaintyColumn == -1 || cursor.isNull(cmasCertaintyColumn)) {
                certainty = -1;
            } else {
                certainty = cursor.getInt(cmasCertaintyColumn);
            }
            cmasInfo = new MtkSmsCbCmasInfo(messageClass, cmasCategory, responseType, severity, urgency, certainty, 0);
        }
        return new MtkCellBroadcastMessage(cursor.getInt(cursor.getColumnIndexOrThrow("sub_id")), new MtkSmsCbMessage(format, geoScope, serialNum, location, category, language, body, priority, etwsInfo, cmasInfo, cursor.getBlob(cursor.getColumnIndexOrThrow("wac"))), cursor.getLong(cursor.getColumnIndexOrThrow("date")), cursor.getInt(cursor.getColumnIndexOrThrow("read")) != 0);
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues(17);
        MtkSmsCbMessage msg = this.mSmsCbMessage;
        cv.put("geo_scope", Integer.valueOf(msg.getGeographicalScope()));
        SmsCbLocation location = msg.getLocation();
        if (location.getPlmn() != null) {
            cv.put("plmn", location.getPlmn());
        }
        if (location.getLac() != -1) {
            cv.put("lac", Integer.valueOf(location.getLac()));
        }
        if (location.getCid() != -1) {
            cv.put(BipUtils.KEY_QOS_CID, Integer.valueOf(location.getCid()));
        }
        cv.put("serial_number", Integer.valueOf(msg.getSerialNumber()));
        cv.put("service_category", Integer.valueOf(msg.getServiceCategory()));
        cv.put("language", msg.getLanguageCode());
        cv.put("body", msg.getMessageBody());
        cv.put("date", Long.valueOf(this.mDeliveryTime));
        cv.put("read", Boolean.valueOf(this.mIsRead));
        cv.put(IPplSmsFilter.KEY_FORMAT, Integer.valueOf(msg.getMessageFormat()));
        cv.put("priority", Integer.valueOf(msg.getMessagePriority()));
        SmsCbEtwsInfo etwsInfo = this.mSmsCbMessage.getEtwsWarningInfo();
        if (etwsInfo != null) {
            cv.put("etws_warning_type", Integer.valueOf(etwsInfo.getWarningType()));
        }
        SmsCbCmasInfo cmasInfo = this.mSmsCbMessage.getCmasWarningInfo();
        if (cmasInfo != null) {
            cv.put("cmas_message_class", Integer.valueOf(cmasInfo.getMessageClass()));
            cv.put("cmas_category", Integer.valueOf(cmasInfo.getCategory()));
            cv.put("cmas_response_type", Integer.valueOf(cmasInfo.getResponseType()));
            cv.put("cmas_severity", Integer.valueOf(cmasInfo.getSeverity()));
            cv.put("cmas_urgency", Integer.valueOf(cmasInfo.getUrgency()));
            cv.put("cmas_certainty", Integer.valueOf(cmasInfo.getCertainty()));
        }
        cv.put("sub_id", Integer.valueOf(this.mSubId));
        cv.put("wac", msg.getWac());
        return cv;
    }

    public ArrayList<Shape> getWacResult() {
        return MtkGsmSmsCbMessage.parseWac(this.mSmsCbMessage);
    }

    public int getMaxWaitTime() {
        return this.mSmsCbMessage.getMaxWaitTime();
    }

    public ArrayList<ArrayList<WhamTuple>> getWHAMTupleList() {
        return MtkGsmSmsCbMessage.parseWHAMTupleList(this.mSmsCbMessage);
    }
}
