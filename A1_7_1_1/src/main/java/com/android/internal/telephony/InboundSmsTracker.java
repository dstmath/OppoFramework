package com.android.internal.telephony;

import android.content.ContentValues;
import android.database.Cursor;
import android.telephony.Rlog;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import com.android.internal.util.HexDump;
import java.util.Arrays;
import java.util.Date;

public class InboundSmsTracker {
    private static final int DEST_PORT_FLAG_3GPP = 131072;
    private static final int DEST_PORT_FLAG_3GPP2 = 262144;
    private static final int DEST_PORT_FLAG_3GPP2_WAP_PDU = 524288;
    private static final int DEST_PORT_FLAG_NO_PORT = 65536;
    private static final int DEST_PORT_MASK = 65535;
    private final String mAddress;
    private String mDeleteWhere;
    private String[] mDeleteWhereArgs;
    private final int mDestPort;
    private final boolean mIs3gpp2;
    private final boolean mIs3gpp2WapPdu;
    private boolean mIsUndeliverMsg;
    private final String mMessageBody;
    private final int mMessageCount;
    private final byte[] mPdu;
    private long mRecvTime;
    private final int mReferenceNumber;
    private final int mSequenceNumber;
    private int mSubId;
    private final long mTimestamp;
    private int mUploadFlag;

    public InboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, boolean is3gpp2WapPdu, String address, String messageBody) {
        this.mIsUndeliverMsg = false;
        this.mSubId = SubscriptionManager.getDefaultSmsSubscriptionId();
        this.mPdu = pdu;
        this.mTimestamp = timestamp;
        this.mDestPort = destPort;
        this.mIs3gpp2 = is3gpp2;
        this.mIs3gpp2WapPdu = is3gpp2WapPdu;
        this.mMessageBody = messageBody;
        this.mAddress = address;
        this.mReferenceNumber = -1;
        this.mSequenceNumber = getIndexOffset();
        this.mMessageCount = 1;
    }

    InboundSmsTracker(int subId, byte[] pdu, long timestamp, int destPort, boolean is3gpp2, boolean is3gpp2WapPdu, String address, String messageBody) {
        this.mIsUndeliverMsg = false;
        this.mSubId = subId;
        this.mPdu = pdu;
        this.mTimestamp = timestamp;
        this.mDestPort = destPort;
        this.mIs3gpp2 = is3gpp2;
        this.mIs3gpp2WapPdu = is3gpp2WapPdu;
        this.mMessageBody = messageBody;
        this.mAddress = address;
        this.mReferenceNumber = -1;
        this.mSequenceNumber = getIndexOffset();
        this.mMessageCount = 1;
    }

    public InboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, String address, int referenceNumber, int sequenceNumber, int messageCount, boolean is3gpp2WapPdu, String messageBody) {
        this.mIsUndeliverMsg = false;
        this.mSubId = SubscriptionManager.getDefaultSmsSubscriptionId();
        this.mPdu = pdu;
        this.mTimestamp = timestamp;
        this.mDestPort = destPort;
        this.mIs3gpp2 = is3gpp2;
        this.mIs3gpp2WapPdu = is3gpp2WapPdu;
        this.mMessageBody = messageBody;
        this.mAddress = address;
        this.mReferenceNumber = referenceNumber;
        this.mSequenceNumber = sequenceNumber;
        this.mMessageCount = messageCount;
    }

    public InboundSmsTracker(int subId, byte[] pdu, long timestamp, int destPort, boolean is3gpp2, String address, int referenceNumber, int sequenceNumber, int messageCount, boolean is3gpp2WapPdu, String messageBody) {
        this.mIsUndeliverMsg = false;
        this.mSubId = subId;
        this.mPdu = pdu;
        this.mTimestamp = timestamp;
        this.mDestPort = destPort;
        this.mIs3gpp2 = is3gpp2;
        this.mIs3gpp2WapPdu = is3gpp2WapPdu;
        this.mMessageBody = messageBody;
        this.mAddress = address;
        this.mReferenceNumber = referenceNumber;
        this.mSequenceNumber = sequenceNumber;
        this.mMessageCount = messageCount;
    }

    public InboundSmsTracker(Cursor cursor, boolean isCurrentFormat3gpp2) {
        this.mIsUndeliverMsg = false;
        this.mPdu = HexDump.hexStringToByteArray(cursor.getString(0));
        if (cursor.isNull(2)) {
            this.mDestPort = -1;
            this.mIs3gpp2 = isCurrentFormat3gpp2;
            this.mIs3gpp2WapPdu = false;
        } else {
            boolean z;
            int destPort = cursor.getInt(2);
            if ((DEST_PORT_FLAG_3GPP & destPort) != 0) {
                this.mIs3gpp2 = false;
            } else if ((262144 & destPort) != 0) {
                this.mIs3gpp2 = true;
            } else {
                this.mIs3gpp2 = isCurrentFormat3gpp2;
            }
            if ((DEST_PORT_FLAG_3GPP2_WAP_PDU & destPort) != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mIs3gpp2WapPdu = z;
            this.mDestPort = getRealDestPort(destPort);
        }
        this.mTimestamp = cursor.getLong(3);
        this.mAddress = cursor.getString(6);
        this.mSubId = cursor.getInt(9);
        if (cursor.isNull(5)) {
            long rowId = cursor.getLong(7);
            this.mReferenceNumber = -1;
            this.mSequenceNumber = getIndexOffset();
            this.mMessageCount = 1;
            this.mDeleteWhere = InboundSmsHandler.SELECT_BY_ID;
            this.mDeleteWhereArgs = new String[]{Long.toString(rowId)};
        } else {
            this.mReferenceNumber = cursor.getInt(4);
            this.mMessageCount = cursor.getInt(5);
            this.mSequenceNumber = cursor.getInt(1);
            int index = this.mSequenceNumber - getIndexOffset();
            if (index < 0 || index >= this.mMessageCount) {
                throw new IllegalArgumentException("invalid PDU sequence " + this.mSequenceNumber + " of " + this.mMessageCount);
            }
            this.mDeleteWhere = InboundSmsHandler.SELECT_BY_REFERENCE;
            this.mDeleteWhereArgs = new String[]{this.mAddress, Integer.toString(this.mReferenceNumber), Integer.toString(this.mMessageCount), Integer.toString(this.mSubId)};
        }
        this.mMessageBody = cursor.getString(8);
    }

    public InboundSmsTracker(Cursor cursor, boolean isCurrentFormat3gpp2, boolean isUndeliverMsg) {
        this.mIsUndeliverMsg = false;
        Rlog.d("InboundSmsTracker", " Cusrsor constructor");
        this.mIsUndeliverMsg = isUndeliverMsg;
        this.mPdu = HexDump.hexStringToByteArray(cursor.getString(0));
        if (cursor.isNull(2)) {
            this.mDestPort = -1;
            this.mIs3gpp2 = isCurrentFormat3gpp2;
            this.mIs3gpp2WapPdu = false;
        } else {
            int destPort = cursor.getInt(2);
            if ((DEST_PORT_FLAG_3GPP & destPort) != 0) {
                this.mIs3gpp2 = false;
            } else if ((262144 & destPort) != 0) {
                this.mIs3gpp2 = true;
            } else {
                this.mIs3gpp2 = isCurrentFormat3gpp2;
            }
            this.mIs3gpp2WapPdu = (DEST_PORT_FLAG_3GPP2_WAP_PDU & destPort) != 0;
            this.mDestPort = getRealDestPort(destPort);
        }
        this.mTimestamp = cursor.getLong(3);
        this.mAddress = cursor.getString(6);
        this.mSubId = cursor.getInt(9);
        long rowId;
        if (cursor.isNull(5)) {
            rowId = cursor.getLong(7);
            this.mReferenceNumber = -1;
            this.mSequenceNumber = getIndexOffset();
            this.mMessageCount = 1;
            this.mDeleteWhere = InboundSmsHandler.SELECT_BY_ID;
            this.mDeleteWhereArgs = new String[]{Long.toString(rowId)};
        } else if (this.mIsUndeliverMsg && cursor.getInt(5) == 1) {
            rowId = cursor.getLong(7);
            this.mReferenceNumber = -1;
            this.mSequenceNumber = getIndexOffset();
            this.mMessageCount = 1;
            this.mDeleteWhere = "_id=? AND deleted=0";
            this.mDeleteWhereArgs = new String[]{Long.toString(rowId)};
        } else {
            this.mReferenceNumber = cursor.getInt(4);
            this.mMessageCount = cursor.getInt(5);
            this.mSequenceNumber = cursor.getInt(1);
            int index = this.mSequenceNumber - getIndexOffset();
            if (index < 0 || index >= this.mMessageCount) {
                throw new IllegalArgumentException("invalid PDU sequence " + this.mSequenceNumber + " of " + this.mMessageCount);
            }
            this.mDeleteWhere = InboundSmsHandler.SELECT_BY_REFERENCE;
            this.mDeleteWhereArgs = new String[]{this.mAddress, Integer.toString(this.mReferenceNumber), Integer.toString(this.mMessageCount), Integer.toString(this.mSubId)};
        }
        this.mMessageBody = cursor.getString(8);
    }

    public ContentValues getContentValues() {
        int destPort;
        ContentValues values = new ContentValues();
        values.put("pdu", HexDump.toHexString(this.mPdu));
        values.put("date", Long.valueOf(this.mTimestamp));
        if (this.mDestPort == -1) {
            destPort = 65536;
        } else {
            destPort = this.mDestPort & 65535;
        }
        if (this.mIs3gpp2) {
            destPort |= 262144;
        } else {
            destPort |= DEST_PORT_FLAG_3GPP;
        }
        if (this.mIs3gpp2WapPdu) {
            destPort |= DEST_PORT_FLAG_3GPP2_WAP_PDU;
        }
        values.put("destination_port", Integer.valueOf(destPort));
        if (this.mAddress != null) {
            values.put("address", this.mAddress);
            values.put("reference_number", Integer.valueOf(this.mReferenceNumber));
            values.put("sequence", Integer.valueOf(this.mSequenceNumber));
            values.put("count", Integer.valueOf(this.mMessageCount));
        }
        values.put("message_body", this.mMessageBody);
        values.put("sub_id", Integer.valueOf(this.mSubId));
        values.put("recv_time", Long.valueOf(this.mRecvTime));
        values.put("upload_flag", Integer.valueOf(this.mUploadFlag));
        return values;
    }

    public static int getRealDestPort(int destPort) {
        if ((65536 & destPort) != 0) {
            return -1;
        }
        return 65535 & destPort;
    }

    public void setDeleteWhere(String deleteWhere, String[] deleteWhereArgs) {
        this.mDeleteWhere = deleteWhere;
        this.mDeleteWhereArgs = deleteWhereArgs;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("SmsTracker{timestamp=");
        builder.append(new Date(this.mTimestamp));
        builder.append(" destPort=").append(this.mDestPort);
        builder.append(" is3gpp2=").append(this.mIs3gpp2);
        if (this.mAddress != null) {
            builder.append(" address=").append(this.mAddress);
            builder.append(" refNumber=").append(this.mReferenceNumber);
            builder.append(" seqNumber=").append(this.mSequenceNumber);
            builder.append(" msgCount=").append(this.mMessageCount);
        }
        if (this.mDeleteWhere != null) {
            builder.append(" deleteWhere(").append(this.mDeleteWhere);
            builder.append(") deleteArgs=(").append(Arrays.toString(this.mDeleteWhereArgs));
            builder.append(')');
        }
        builder.append('}');
        return builder.toString();
    }

    public byte[] getPdu() {
        return this.mPdu;
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }

    public int getDestPort() {
        return this.mDestPort;
    }

    public boolean is3gpp2() {
        return this.mIs3gpp2;
    }

    public boolean is3gpp2WapPdu() {
        return this.mIs3gpp2WapPdu;
    }

    public String getFormat() {
        return this.mIs3gpp2 ? SmsMessage.FORMAT_3GPP2 : SmsMessage.FORMAT_3GPP;
    }

    public int getIndexOffset() {
        return (this.mIs3gpp2 && this.mIs3gpp2WapPdu) ? 0 : 1;
    }

    public String getAddress() {
        return this.mAddress;
    }

    public String getMessageBody() {
        return this.mMessageBody;
    }

    public int getReferenceNumber() {
        return this.mReferenceNumber;
    }

    public int getSequenceNumber() {
        return this.mSequenceNumber;
    }

    public int getMessageCount() {
        return this.mMessageCount;
    }

    public String getDeleteWhere() {
        return this.mDeleteWhere;
    }

    public String[] getDeleteWhereArgs() {
        return this.mDeleteWhereArgs;
    }

    public int getSubId() {
        return this.mSubId;
    }

    public void setSubId(int subid) {
        this.mSubId = subid;
    }

    public boolean isDupCheckRequired() {
        return this.mIsUndeliverMsg;
    }
}
