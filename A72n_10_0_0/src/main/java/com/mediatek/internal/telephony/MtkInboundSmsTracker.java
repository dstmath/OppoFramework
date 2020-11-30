package com.mediatek.internal.telephony;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Pair;
import com.android.internal.telephony.InboundSmsTracker;
import com.mediatek.internal.telephony.util.MtkSmsCommonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MtkInboundSmsTracker extends InboundSmsTracker {
    public static final int SUB_ID_COLUMN = 10;
    private long mRecvTime;
    private int mSubId;
    private int mUploadFlag;

    public MtkInboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, boolean is3gpp2WapPdu, String address, String displayAddress, String messageBody, boolean isClass0) {
        super(pdu, timestamp, destPort, is3gpp2, is3gpp2WapPdu, address, displayAddress, messageBody, isClass0);
    }

    public MtkInboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, String address, String displayAddress, int referenceNumber, int sequenceNumber, int messageCount, boolean is3gpp2WapPdu, String messageBody, boolean isClass0) {
        super(pdu, timestamp, destPort, is3gpp2, address, displayAddress, referenceNumber, sequenceNumber, messageCount, is3gpp2WapPdu, messageBody, isClass0);
    }

    public MtkInboundSmsTracker(Cursor cursor, boolean isCurrentFormat3gpp2) {
        super(cursor, isCurrentFormat3gpp2);
        this.mSubId = cursor.getInt(10);
        if (cursor.getInt(5) != 1) {
            setDeleteWhere(this.mDeleteWhere, this.mDeleteWhereArgs);
        }
    }

    public ContentValues getContentValues() {
        ContentValues values = MtkInboundSmsTracker.super.getContentValues();
        values.put("sub_id", Integer.valueOf(this.mSubId));
        return values;
    }

    public boolean is3gpp2WapPdu() {
        return this.mIs3gpp2WapPdu;
    }

    public int getSubId() {
        return this.mSubId;
    }

    public void setSubId(int subId) {
        this.mSubId = subId;
    }

    public int getDestPort() {
        return this.mDestPort;
    }

    public String getQueryForSegments() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(MtkInboundSmsTracker.super.getQueryForSegments());
        sb.append(" AND sub_id=?");
        if (is3gpp2()) {
            str = MtkSmsCommonUtil.SQL_3GPP2_SMS;
        } else {
            str = MtkSmsCommonUtil.SQL_3GPP_SMS;
        }
        sb.append(str);
        return sb.toString();
    }

    public void setDeleteWhere(String deleteWhere, String[] deleteWhereArgs) {
        if (getMessageCount() == 1) {
            MtkInboundSmsTracker.super.setDeleteWhere(deleteWhere, deleteWhereArgs);
        } else {
            MtkInboundSmsTracker.super.setDeleteWhere(deleteWhere, (String[]) appendSubIdInQuery(null, deleteWhereArgs).second);
        }
    }

    public Pair<String, String[]> getExactMatchDupDetectQuery() {
        return appendSubIdInQuery(MtkInboundSmsTracker.super.getExactMatchDupDetectQuery());
    }

    public Pair<String, String[]> getInexactMatchDupDetectQuery() {
        return appendSubIdInQuery(MtkInboundSmsTracker.super.getInexactMatchDupDetectQuery());
    }

    private Pair<String, String[]> appendSubIdInQuery(Pair<String, String[]> base) {
        if (base == null) {
            return null;
        }
        return appendSubIdInQuery((String) base.first, (String[]) base.second);
    }

    private Pair<String, String[]> appendSubIdInQuery(String where, String[] whereArgs) {
        String newWhere;
        String str;
        if (where == null) {
            newWhere = null;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(where);
            sb.append(" AND sub_id=?");
            if (is3gpp2()) {
                str = MtkSmsCommonUtil.SQL_3GPP2_SMS;
            } else {
                str = MtkSmsCommonUtil.SQL_3GPP_SMS;
            }
            sb.append(str);
            newWhere = sb.toString();
        }
        List<String> baseWhereArgs = new ArrayList<>(Arrays.asList(whereArgs));
        baseWhereArgs.add(Integer.toString(this.mSubId));
        String[] newWhereArgs = new String[baseWhereArgs.size()];
        baseWhereArgs.toArray(newWhereArgs);
        return new Pair<>(newWhere, newWhereArgs);
    }
}
