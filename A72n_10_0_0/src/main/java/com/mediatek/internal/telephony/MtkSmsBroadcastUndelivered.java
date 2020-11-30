package com.mediatek.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.UserManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.InboundSmsHandler;
import com.android.internal.telephony.InboundSmsTracker;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SmsBroadcastUndelivered;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.cdma.CdmaInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.util.MtkSmsCommonUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class MtkSmsBroadcastUndelivered extends SmsBroadcastUndelivered {
    private static final String[] PDU_PENDING_MESSAGE_PROJECTION = {"pdu", "sequence", "destination_port", "date", "reference_number", "count", "address", "_id", "message_body", "display_originating_addr", "sub_id"};
    private static final String TAG = "MtkSmsBroadcastUndelivered";
    private static MtkSmsBroadcastUndelivered[] instance;
    private final Phone mPhone;

    public static void initialize(Context context, GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler) {
        MtkSmsBroadcastUndelivered[] mtkSmsBroadcastUndeliveredArr;
        int phoneId = gsmInboundSmsHandler.getPhone().getPhoneId();
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        Rlog.i(TAG, " call initialize() phoneCount = " + phoneCount);
        if (instance == null && phoneCount > 0) {
            instance = new MtkSmsBroadcastUndelivered[phoneCount];
        }
        if (SubscriptionManager.isValidPhoneId(phoneId) && (mtkSmsBroadcastUndeliveredArr = instance) != null && mtkSmsBroadcastUndeliveredArr[phoneId] == null) {
            Rlog.d(TAG, "Phone " + phoneId + " call initialize");
            instance[phoneId] = new MtkSmsBroadcastUndelivered(context, gsmInboundSmsHandler, cdmaInboundSmsHandler);
        }
        gsmInboundSmsHandler.sendMessage(6);
        if (cdmaInboundSmsHandler != null) {
            cdmaInboundSmsHandler.sendMessage(6);
        }
    }

    private MtkSmsBroadcastUndelivered(Context context, GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler) {
        super(context, gsmInboundSmsHandler, cdmaInboundSmsHandler);
        this.mPhone = gsmInboundSmsHandler.getPhone();
        if (!((UserManager) context.getSystemService(DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER)).isUserUnlocked()) {
            Rlog.d(TAG, "Phone " + this.mPhone.getPhoneId() + " register user unlock event");
        }
    }

    public static void scanRawTable(Context context, CdmaInboundSmsHandler cdmaInboundSmsHandler, GsmInboundSmsHandler gsmInboundSmsHandler, long oldMessageTimestamp) {
        scanRawTable(context, false, cdmaInboundSmsHandler, gsmInboundSmsHandler, oldMessageTimestamp);
        scanRawTable(context, true, cdmaInboundSmsHandler, gsmInboundSmsHandler, oldMessageTimestamp);
    }

    /* JADX WARNING: Removed duplicated region for block: B:122:0x02b0  */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x02d6  */
    private static void scanRawTable(Context context, boolean isCurrentFormat3gpp2, CdmaInboundSmsHandler cdmaInboundSmsHandler, GsmInboundSmsHandler gsmInboundSmsHandler, long oldMessageTimestamp) {
        Cursor cursor;
        IllegalArgumentException e;
        SQLException e2;
        Iterator<SmsReferenceKey> it;
        MtkInboundSmsTracker tracker;
        String nonDeleteWhere;
        HashSet<SmsReferenceKey> oldMultiPartMessages;
        boolean z = isCurrentFormat3gpp2;
        Rlog.d(TAG, "scanning raw table for undelivered messages");
        long startTime = System.nanoTime();
        ContentResolver contentResolver = context.getContentResolver();
        HashMap<SmsReferenceKey, Integer> multiPartReceivedCount = new HashMap<>(4);
        HashSet<SmsReferenceKey> oldMultiPartMessages2 = new HashSet<>(4);
        Cursor cursor2 = null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("deleted = 0");
            sb.append(z ? MtkSmsCommonUtil.SQL_3GPP2_SMS : MtkSmsCommonUtil.SQL_3GPP_SMS);
            String nonDeleteWhere2 = sb.toString();
            ContentResolver contentResolver2 = contentResolver;
            HashSet<SmsReferenceKey> oldMultiPartMessages3 = oldMultiPartMessages2;
            try {
                cursor = contentResolver.query(InboundSmsHandler.sRawUri, PDU_PENDING_MESSAGE_PROJECTION, nonDeleteWhere2, null, null);
                if (cursor == null) {
                    try {
                        Rlog.e(TAG, "error getting pending message cursor");
                        if (cursor != null) {
                            cursor.close();
                        }
                        Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                    } catch (SQLException e3) {
                        e2 = e3;
                        cursor2 = cursor;
                        try {
                            Rlog.e(TAG, "error reading pending SMS messages", e2);
                            if (cursor2 != null) {
                            }
                            Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                        } catch (Throwable th) {
                            e = th;
                            cursor = cursor2;
                        }
                    } catch (Throwable th2) {
                        e = th2;
                        if (cursor != null) {
                        }
                        Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                        throw e;
                    }
                } else {
                    while (cursor.moveToNext()) {
                        try {
                            try {
                                tracker = TelephonyComponentFactory.getInstance().inject(TelephonyComponentFactory.class.getName()).makeInboundSmsTracker(cursor, z);
                            } catch (IllegalArgumentException e4) {
                                Rlog.e(TAG, "error loading SmsTracker: " + e4);
                                oldMultiPartMessages3 = oldMultiPartMessages3;
                                nonDeleteWhere2 = nonDeleteWhere2;
                            }
                        } catch (SQLException e5) {
                            e2 = e5;
                            cursor2 = cursor;
                            Rlog.e(TAG, "error reading pending SMS messages", e2);
                            if (cursor2 != null) {
                            }
                            Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                        } catch (Throwable th3) {
                            e = th3;
                            if (cursor != null) {
                            }
                            Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                            throw e;
                        }
                        try {
                            if (tracker.getMessageCount() != 1) {
                                SmsReferenceKey reference = new SmsReferenceKey(tracker);
                                Integer receivedCount = multiPartReceivedCount.get(reference);
                                if (receivedCount == null) {
                                    try {
                                        multiPartReceivedCount.put(reference, 1);
                                        if (tracker.getTimestamp() < oldMessageTimestamp) {
                                            oldMultiPartMessages = oldMultiPartMessages3;
                                            try {
                                                oldMultiPartMessages.add(reference);
                                                nonDeleteWhere = nonDeleteWhere2;
                                            } catch (SQLException e6) {
                                                e2 = e6;
                                                cursor2 = cursor;
                                                Rlog.e(TAG, "error reading pending SMS messages", e2);
                                                if (cursor2 != null) {
                                                }
                                                Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                                            } catch (Throwable th4) {
                                                e = th4;
                                                if (cursor != null) {
                                                }
                                                Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                                                throw e;
                                            }
                                        } else {
                                            oldMultiPartMessages = oldMultiPartMessages3;
                                            nonDeleteWhere = nonDeleteWhere2;
                                        }
                                    } catch (SQLException e7) {
                                        e2 = e7;
                                        cursor2 = cursor;
                                        Rlog.e(TAG, "error reading pending SMS messages", e2);
                                        if (cursor2 != null) {
                                        }
                                        Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                                    } catch (Throwable th5) {
                                        e = th5;
                                        if (cursor != null) {
                                        }
                                        Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                                        throw e;
                                    }
                                } else {
                                    try {
                                        int newCount = receivedCount.intValue() + 1;
                                        nonDeleteWhere = nonDeleteWhere2;
                                        if (newCount == tracker.getMessageCount()) {
                                            Rlog.d(TAG, "found complete multi-part message");
                                            if (tracker.getSubId() == gsmInboundSmsHandler.getPhone().getSubId()) {
                                                try {
                                                    Rlog.d(TAG, "New sms on raw table, subId: " + tracker.getSubId());
                                                    broadcastSms(tracker, cdmaInboundSmsHandler, gsmInboundSmsHandler);
                                                } catch (SQLException e8) {
                                                    e2 = e8;
                                                    cursor2 = cursor;
                                                } catch (Throwable th6) {
                                                    e = th6;
                                                    if (cursor != null) {
                                                    }
                                                    Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                                                    throw e;
                                                }
                                            }
                                            oldMultiPartMessages = oldMultiPartMessages3;
                                            oldMultiPartMessages.remove(reference);
                                        } else {
                                            oldMultiPartMessages = oldMultiPartMessages3;
                                            multiPartReceivedCount.put(reference, Integer.valueOf(newCount));
                                        }
                                    } catch (SQLException e9) {
                                        e2 = e9;
                                        cursor2 = cursor;
                                        Rlog.e(TAG, "error reading pending SMS messages", e2);
                                        if (cursor2 != null) {
                                        }
                                        Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                                    } catch (Throwable th7) {
                                        e = th7;
                                        if (cursor != null) {
                                        }
                                        Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                                        throw e;
                                    }
                                }
                            } else if (tracker.getSubId() == gsmInboundSmsHandler.getPhone().getSubId()) {
                                Rlog.d(TAG, "New sms on raw table, subId: " + tracker.getSubId());
                                broadcastSms(tracker, cdmaInboundSmsHandler, gsmInboundSmsHandler);
                                nonDeleteWhere = nonDeleteWhere2;
                                oldMultiPartMessages = oldMultiPartMessages3;
                            } else {
                                nonDeleteWhere = nonDeleteWhere2;
                                oldMultiPartMessages = oldMultiPartMessages3;
                            }
                            oldMultiPartMessages3 = oldMultiPartMessages;
                            nonDeleteWhere2 = nonDeleteWhere;
                        } catch (SQLException e10) {
                            e2 = e10;
                            cursor2 = cursor;
                            Rlog.e(TAG, "error reading pending SMS messages", e2);
                            if (cursor2 != null) {
                            }
                            Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                        } catch (Throwable th8) {
                            e = th8;
                            if (cursor != null) {
                            }
                            Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                            throw e;
                        }
                    }
                    try {
                        Iterator<SmsReferenceKey> it2 = oldMultiPartMessages3.iterator();
                        while (it2.hasNext()) {
                            SmsReferenceKey message = it2.next();
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append("address=? AND reference_number=? AND count=? AND deleted=0 AND sub_id=?");
                            sb2.append(z ? MtkSmsCommonUtil.SQL_3GPP2_SMS : MtkSmsCommonUtil.SQL_3GPP_SMS);
                            try {
                                int rows = contentResolver2.delete(InboundSmsHandler.sRawUriPermanentDelete, sb2.toString(), message.getDeleteWhereArgs());
                                if (rows == 0) {
                                    Rlog.e(TAG, "No rows were deleted from raw table!");
                                    it = it2;
                                } else {
                                    StringBuilder sb3 = new StringBuilder();
                                    it = it2;
                                    sb3.append("Deleted ");
                                    sb3.append(rows);
                                    sb3.append(" rows from raw table for incomplete ");
                                    sb3.append(message.mMessageCount);
                                    sb3.append(" part message");
                                    Rlog.d(TAG, sb3.toString());
                                }
                                contentResolver2 = contentResolver2;
                                it2 = it;
                                z = isCurrentFormat3gpp2;
                            } catch (SQLException e11) {
                                e2 = e11;
                                cursor2 = cursor;
                                Rlog.e(TAG, "error reading pending SMS messages", e2);
                                if (cursor2 != null) {
                                    cursor2.close();
                                }
                                Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                            } catch (Throwable th9) {
                                e = th9;
                                if (cursor != null) {
                                    cursor.close();
                                }
                                Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                                throw e;
                            }
                        }
                        cursor.close();
                        Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                    } catch (SQLException e12) {
                        e2 = e12;
                        cursor2 = cursor;
                        Rlog.e(TAG, "error reading pending SMS messages", e2);
                        if (cursor2 != null) {
                        }
                        Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                    } catch (Throwable th10) {
                        e = th10;
                        if (cursor != null) {
                        }
                        Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                        throw e;
                    }
                }
            } catch (SQLException e13) {
                e2 = e13;
                Rlog.e(TAG, "error reading pending SMS messages", e2);
                if (cursor2 != null) {
                }
                Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
            } catch (Throwable th11) {
                e = th11;
                cursor = null;
                if (cursor != null) {
                }
                Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                throw e;
            }
        } catch (SQLException e14) {
            e2 = e14;
            Rlog.e(TAG, "error reading pending SMS messages", e2);
            if (cursor2 != null) {
            }
            Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
        } catch (Throwable th12) {
            e = th12;
            cursor = null;
            if (cursor != null) {
            }
            Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
            throw e;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: com.android.internal.telephony.gsm.GsmInboundSmsHandler */
    /* JADX WARN: Multi-variable type inference failed */
    private static void broadcastSms(InboundSmsTracker tracker, CdmaInboundSmsHandler cdmaInboundSmsHandler, GsmInboundSmsHandler gsmInboundSmsHandler) {
        CdmaInboundSmsHandler cdmaInboundSmsHandler2;
        if (tracker.is3gpp2()) {
            cdmaInboundSmsHandler2 = cdmaInboundSmsHandler;
        } else {
            cdmaInboundSmsHandler2 = gsmInboundSmsHandler;
        }
        if (cdmaInboundSmsHandler2 != null) {
            cdmaInboundSmsHandler2.sendMessage(2, tracker);
            return;
        }
        Rlog.e(TAG, "null handler for " + tracker.getFormat() + " format, can't deliver.");
    }

    /* access modifiers changed from: private */
    public static class SmsReferenceKey {
        final String mAddress;
        final int mMessageCount;
        final int mReferenceNumber;
        final long mSubId;

        SmsReferenceKey(MtkInboundSmsTracker tracker) {
            this.mAddress = tracker.getAddress();
            this.mReferenceNumber = tracker.getReferenceNumber();
            this.mMessageCount = tracker.getMessageCount();
            this.mSubId = (long) tracker.getSubId();
        }

        /* access modifiers changed from: package-private */
        public String[] getDeleteWhereArgs() {
            return new String[]{this.mAddress, Integer.toString(this.mReferenceNumber), Integer.toString(this.mMessageCount), Long.toString(this.mSubId)};
        }

        public int hashCode() {
            return (((((int) this.mSubId) * 63) + (this.mReferenceNumber * 31) + this.mMessageCount) * 31) + this.mAddress.hashCode();
        }

        public boolean equals(Object o) {
            if (!(o instanceof SmsReferenceKey)) {
                return false;
            }
            SmsReferenceKey other = (SmsReferenceKey) o;
            if (other.mAddress.equals(this.mAddress) && other.mReferenceNumber == this.mReferenceNumber && other.mMessageCount == this.mMessageCount && other.mSubId == this.mSubId) {
                return true;
            }
            return false;
        }
    }
}
