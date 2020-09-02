package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.os.UserManager;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.cdma.CdmaInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class SmsBroadcastUndelivered {
    protected static final boolean DBG = true;
    protected static final long DEFAULT_PARTIAL_SEGMENT_EXPIRE_AGE = 604800000;
    private static final String[] PDU_PENDING_MESSAGE_PROJECTION = {"pdu", "sequence", "destination_port", "date", "reference_number", "count", "address", HbpcdLookup.ID, "message_body", "display_originating_addr"};
    private static final String TAG = "SmsBroadcastUndelivered";
    private static SmsBroadcastUndelivered instance;
    private static Class<?> sMtkSmsBroadcastUndelivered = getMtkSmsBroadcastUndelivered();
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.SmsBroadcastUndelivered.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            OppoRlog.Rlog.d(SmsBroadcastUndelivered.TAG, "Received broadcast " + intent.getAction());
            if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                new ScanRawTableThread(context).start();
            }
        }
    };
    protected final CdmaInboundSmsHandler mCdmaInboundSmsHandler;
    protected final GsmInboundSmsHandler mGsmInboundSmsHandler;
    protected final ContentResolver mResolver;

    private class ScanRawTableThread extends Thread {
        private final Context context;

        private ScanRawTableThread(Context context2) {
            this.context = context2;
        }

        public void run() {
            SmsBroadcastUndelivered.scanRawTable(this.context, SmsBroadcastUndelivered.this.mCdmaInboundSmsHandler, SmsBroadcastUndelivered.this.mGsmInboundSmsHandler, System.currentTimeMillis() - SmsBroadcastUndelivered.this.getUndeliveredSmsExpirationTime(this.context));
            InboundSmsHandler.cancelNewMessageNotification(this.context);
        }
    }

    public static void initialize(Context context, GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler) {
        if (instance == null) {
            instance = new SmsBroadcastUndelivered(context, gsmInboundSmsHandler, cdmaInboundSmsHandler);
        }
        if (gsmInboundSmsHandler != null) {
            gsmInboundSmsHandler.sendMessage(6);
        }
        if (cdmaInboundSmsHandler != null) {
            cdmaInboundSmsHandler.sendMessage(6);
        }
    }

    @UnsupportedAppUsage
    protected SmsBroadcastUndelivered(Context context, GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler) {
        this.mResolver = context.getContentResolver();
        this.mGsmInboundSmsHandler = gsmInboundSmsHandler;
        this.mCdmaInboundSmsHandler = cdmaInboundSmsHandler;
        if (((UserManager) context.getSystemService("user")).isUserUnlocked()) {
            new ScanRawTableThread(context).start();
            return;
        }
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_UNLOCKED");
        context.registerReceiver(this.mBroadcastReceiver, userFilter);
    }

    /* JADX WARNING: Removed duplicated region for block: B:105:0x0233  */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x0248  */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x026e  */
    static void scanRawTable(Context context, CdmaInboundSmsHandler cdmaInboundSmsHandler, GsmInboundSmsHandler gsmInboundSmsHandler, long oldMessageTimestamp) {
        Cursor cursor;
        StringBuilder sb;
        HashMap<SmsReferenceKey, Integer> multiPartReceivedCount;
        int i = 1;
        if (sMtkSmsBroadcastUndelivered != null) {
            try {
                Method extScanRawTableMethod = sMtkSmsBroadcastUndelivered.getDeclaredMethod("scanRawTable", Context.class, CdmaInboundSmsHandler.class, GsmInboundSmsHandler.class, Long.TYPE);
                Object[] params = {context, cdmaInboundSmsHandler, gsmInboundSmsHandler, Long.valueOf(oldMessageTimestamp)};
                extScanRawTableMethod.setAccessible(true);
                extScanRawTableMethod.invoke(null, params);
                return;
            } catch (Exception e) {
                OppoRlog.Rlog.e(TAG, "No MtkSmsBroadcastUndelivered! Used AOSP for instead!");
            }
        }
        OppoRlog.Rlog.d(TAG, "scanning raw table for undelivered messages");
        long startTime = System.nanoTime();
        ContentResolver contentResolver = context.getContentResolver();
        HashMap<SmsReferenceKey, Integer> multiPartReceivedCount2 = new HashMap<>(4);
        HashSet<SmsReferenceKey> oldMultiPartMessages = new HashSet<>(4);
        Cursor cursor2 = null;
        try {
            HashMap<SmsReferenceKey, Integer> multiPartReceivedCount3 = multiPartReceivedCount2;
            ContentResolver contentResolver2 = contentResolver;
            try {
                cursor = contentResolver.query(InboundSmsHandler.sRawUri, PDU_PENDING_MESSAGE_PROJECTION, "deleted = 0", null, null);
                if (cursor == null) {
                    try {
                        OppoRlog.Rlog.e(TAG, "error getting pending message cursor");
                        if (cursor != null) {
                            cursor.close();
                        }
                        OppoRlog.Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                    } catch (SQLException e2) {
                        e = e2;
                        cursor2 = cursor;
                        OppoRlog.Rlog.e(TAG, "error reading pending SMS messages", e);
                        if (cursor2 != null) {
                        }
                        sb = new StringBuilder();
                        sb.append("finished scanning raw table in ");
                        sb.append((System.nanoTime() - startTime) / 1000000);
                        sb.append(" ms");
                        OppoRlog.Rlog.d(TAG, sb.toString());
                    } catch (Exception e3) {
                        ex = e3;
                        cursor2 = cursor;
                        try {
                            OppoRlog.Rlog.e(TAG, "error reading pending SMS messages2", ex);
                            if (cursor2 != null) {
                            }
                            sb = new StringBuilder();
                            sb.append("finished scanning raw table in ");
                            sb.append((System.nanoTime() - startTime) / 1000000);
                            sb.append(" ms");
                            OppoRlog.Rlog.d(TAG, sb.toString());
                        } catch (Throwable th) {
                            th = th;
                            cursor = cursor2;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (cursor != null) {
                        }
                        OppoRlog.Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                        throw th;
                    }
                } else {
                    try {
                        boolean isCurrentFormat3gpp2 = InboundSmsHandler.isCurrentFormat3gpp2();
                        while (cursor.moveToNext()) {
                            try {
                                InboundSmsTracker tracker = TelephonyComponentFactory.getInstance().inject(InboundSmsTracker.class.getName()).makeInboundSmsTracker(cursor, isCurrentFormat3gpp2);
                                if (tracker.getMessageCount() == i) {
                                    broadcastSms(tracker, cdmaInboundSmsHandler, gsmInboundSmsHandler);
                                    multiPartReceivedCount = multiPartReceivedCount3;
                                } else {
                                    SmsReferenceKey reference = new SmsReferenceKey(tracker);
                                    multiPartReceivedCount = multiPartReceivedCount3;
                                    try {
                                        Integer receivedCount = multiPartReceivedCount.get(reference);
                                        if (receivedCount == null) {
                                            multiPartReceivedCount.put(reference, Integer.valueOf(i));
                                            if (tracker.getTimestamp() < oldMessageTimestamp) {
                                                oldMultiPartMessages.add(reference);
                                            }
                                        } else {
                                            int newCount = receivedCount.intValue() + i;
                                            if (newCount == tracker.getMessageCount()) {
                                                OppoRlog.Rlog.d(TAG, "found complete multi-part message");
                                                broadcastSms(tracker, cdmaInboundSmsHandler, gsmInboundSmsHandler);
                                                oldMultiPartMessages.remove(reference);
                                            } else {
                                                multiPartReceivedCount.put(reference, Integer.valueOf(newCount));
                                            }
                                        }
                                    } catch (SQLException e4) {
                                        e = e4;
                                        cursor2 = cursor;
                                        OppoRlog.Rlog.e(TAG, "error reading pending SMS messages", e);
                                        if (cursor2 != null) {
                                        }
                                        sb = new StringBuilder();
                                        sb.append("finished scanning raw table in ");
                                        sb.append((System.nanoTime() - startTime) / 1000000);
                                        sb.append(" ms");
                                        OppoRlog.Rlog.d(TAG, sb.toString());
                                    } catch (Exception e5) {
                                        ex = e5;
                                        cursor2 = cursor;
                                        OppoRlog.Rlog.e(TAG, "error reading pending SMS messages2", ex);
                                        if (cursor2 != null) {
                                        }
                                        sb = new StringBuilder();
                                        sb.append("finished scanning raw table in ");
                                        sb.append((System.nanoTime() - startTime) / 1000000);
                                        sb.append(" ms");
                                        OppoRlog.Rlog.d(TAG, sb.toString());
                                    } catch (Throwable th3) {
                                        th = th3;
                                        if (cursor != null) {
                                        }
                                        OppoRlog.Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                                        throw th;
                                    }
                                }
                                multiPartReceivedCount3 = multiPartReceivedCount;
                                i = 1;
                            } catch (IllegalArgumentException e6) {
                                OppoRlog.Rlog.e(TAG, "error loading SmsTracker: " + e6);
                                multiPartReceivedCount3 = multiPartReceivedCount3;
                                i = 1;
                            }
                        }
                        int phoneId = getPhoneId(gsmInboundSmsHandler, cdmaInboundSmsHandler);
                        Iterator<SmsReferenceKey> it = oldMultiPartMessages.iterator();
                        while (it.hasNext()) {
                            SmsReferenceKey message = it.next();
                            try {
                                int rows = contentResolver2.delete(InboundSmsHandler.sRawUriPermanentDelete, message.getDeleteWhere(), message.getDeleteWhereArgs());
                                if (rows == 0) {
                                    try {
                                        OppoRlog.Rlog.e(TAG, "No rows were deleted from raw table!");
                                    } catch (SQLException e7) {
                                        e = e7;
                                        cursor2 = cursor;
                                    } catch (Exception e8) {
                                        ex = e8;
                                        cursor2 = cursor;
                                        OppoRlog.Rlog.e(TAG, "error reading pending SMS messages2", ex);
                                        if (cursor2 != null) {
                                        }
                                        sb = new StringBuilder();
                                        sb.append("finished scanning raw table in ");
                                        sb.append((System.nanoTime() - startTime) / 1000000);
                                        sb.append(" ms");
                                        OppoRlog.Rlog.d(TAG, sb.toString());
                                    } catch (Throwable th4) {
                                        th = th4;
                                        if (cursor != null) {
                                        }
                                        OppoRlog.Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                                        throw th;
                                    }
                                } else {
                                    OppoRlog.Rlog.d(TAG, "Deleted " + rows + " rows from raw table for incomplete " + message.mMessageCount + " part message");
                                }
                                if (rows > 0) {
                                    contentResolver2 = contentResolver2;
                                    TelephonyMetrics.getInstance().writeDroppedIncomingMultipartSms(phoneId, message.mFormat, rows, message.mMessageCount);
                                } else {
                                    contentResolver2 = contentResolver2;
                                }
                            } catch (SQLException e9) {
                                e = e9;
                                cursor2 = cursor;
                                OppoRlog.Rlog.e(TAG, "error reading pending SMS messages", e);
                                if (cursor2 != null) {
                                }
                                sb = new StringBuilder();
                                sb.append("finished scanning raw table in ");
                                sb.append((System.nanoTime() - startTime) / 1000000);
                                sb.append(" ms");
                                OppoRlog.Rlog.d(TAG, sb.toString());
                            } catch (Exception e10) {
                                ex = e10;
                                cursor2 = cursor;
                                OppoRlog.Rlog.e(TAG, "error reading pending SMS messages2", ex);
                                if (cursor2 != null) {
                                }
                                sb = new StringBuilder();
                                sb.append("finished scanning raw table in ");
                                sb.append((System.nanoTime() - startTime) / 1000000);
                                sb.append(" ms");
                                OppoRlog.Rlog.d(TAG, sb.toString());
                            } catch (Throwable th5) {
                                th = th5;
                                if (cursor != null) {
                                }
                                OppoRlog.Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                                throw th;
                            }
                        }
                        cursor.close();
                        OppoRlog.Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                    } catch (SQLException e11) {
                        e = e11;
                        cursor2 = cursor;
                        OppoRlog.Rlog.e(TAG, "error reading pending SMS messages", e);
                        if (cursor2 != null) {
                            cursor2.close();
                        }
                        sb = new StringBuilder();
                        sb.append("finished scanning raw table in ");
                        sb.append((System.nanoTime() - startTime) / 1000000);
                        sb.append(" ms");
                        OppoRlog.Rlog.d(TAG, sb.toString());
                    } catch (Exception e12) {
                        ex = e12;
                        cursor2 = cursor;
                        OppoRlog.Rlog.e(TAG, "error reading pending SMS messages2", ex);
                        if (cursor2 != null) {
                            cursor2.close();
                        }
                        sb = new StringBuilder();
                        sb.append("finished scanning raw table in ");
                        sb.append((System.nanoTime() - startTime) / 1000000);
                        sb.append(" ms");
                        OppoRlog.Rlog.d(TAG, sb.toString());
                    } catch (Throwable th6) {
                        th = th6;
                        if (cursor != null) {
                            cursor.close();
                        }
                        OppoRlog.Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                        throw th;
                    }
                }
            } catch (SQLException e13) {
                e = e13;
                OppoRlog.Rlog.e(TAG, "error reading pending SMS messages", e);
                if (cursor2 != null) {
                }
                sb = new StringBuilder();
                sb.append("finished scanning raw table in ");
                sb.append((System.nanoTime() - startTime) / 1000000);
                sb.append(" ms");
                OppoRlog.Rlog.d(TAG, sb.toString());
            } catch (Exception e14) {
                ex = e14;
                OppoRlog.Rlog.e(TAG, "error reading pending SMS messages2", ex);
                if (cursor2 != null) {
                }
                sb = new StringBuilder();
                sb.append("finished scanning raw table in ");
                sb.append((System.nanoTime() - startTime) / 1000000);
                sb.append(" ms");
                OppoRlog.Rlog.d(TAG, sb.toString());
            } catch (Throwable th7) {
                th = th7;
                cursor = null;
                if (cursor != null) {
                }
                OppoRlog.Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                throw th;
            }
        } catch (SQLException e15) {
            e = e15;
            OppoRlog.Rlog.e(TAG, "error reading pending SMS messages", e);
            if (cursor2 != null) {
            }
            sb = new StringBuilder();
            sb.append("finished scanning raw table in ");
            sb.append((System.nanoTime() - startTime) / 1000000);
            sb.append(" ms");
            OppoRlog.Rlog.d(TAG, sb.toString());
        } catch (Exception e16) {
            ex = e16;
            OppoRlog.Rlog.e(TAG, "error reading pending SMS messages2", ex);
            if (cursor2 != null) {
            }
            sb = new StringBuilder();
            sb.append("finished scanning raw table in ");
            sb.append((System.nanoTime() - startTime) / 1000000);
            sb.append(" ms");
            OppoRlog.Rlog.d(TAG, sb.toString());
        } catch (Throwable th8) {
            th = th8;
            cursor = null;
            if (cursor != null) {
            }
            OppoRlog.Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
            throw th;
        }
    }

    private static int getPhoneId(GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler) {
        if (gsmInboundSmsHandler != null) {
            return gsmInboundSmsHandler.getPhone().getPhoneId();
        }
        if (cdmaInboundSmsHandler != null) {
            return cdmaInboundSmsHandler.getPhone().getPhoneId();
        }
        return -1;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: com.android.internal.telephony.cdma.CdmaInboundSmsHandler} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: com.android.internal.telephony.cdma.CdmaInboundSmsHandler} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: com.android.internal.telephony.cdma.CdmaInboundSmsHandler} */
    /* JADX WARN: Multi-variable type inference failed */
    private static void broadcastSms(InboundSmsTracker tracker, CdmaInboundSmsHandler cdmaInboundSmsHandler, GsmInboundSmsHandler gsmInboundSmsHandler) {
        InboundSmsHandler handler;
        if (tracker.is3gpp2()) {
            handler = cdmaInboundSmsHandler;
        } else {
            handler = gsmInboundSmsHandler;
        }
        if (handler != null) {
            handler.sendMessage(2, tracker);
            return;
        }
        OppoRlog.Rlog.e(TAG, "null handler for " + tracker.getFormat() + " format, can't deliver.");
    }

    /* access modifiers changed from: private */
    public long getUndeliveredSmsExpirationTime(Context context) {
        PersistableBundle bundle = ((CarrierConfigManager) context.getSystemService("carrier_config")).getConfigForSubId(SubscriptionManager.getDefaultSmsSubscriptionId());
        if (bundle != null) {
            return bundle.getLong("undelivered_sms_message_expiration_time", DEFAULT_PARTIAL_SEGMENT_EXPIRE_AGE);
        }
        return DEFAULT_PARTIAL_SEGMENT_EXPIRE_AGE;
    }

    private static Class<?> getMtkSmsBroadcastUndelivered() {
        if (!SystemProperties.get("ro.vendor.mtk_telephony_add_on_policy", OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK).equals(OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK)) {
            return null;
        }
        try {
            return Class.forName("com.mediatek.internal.telephony.MtkSmsBroadcastUndelivered");
        } catch (Exception e) {
            OppoRlog.Rlog.e(TAG, "No MtkSmsBroadcastUndeliveredClass! Used AOSP for instead!");
            return null;
        }
    }

    private static class SmsReferenceKey {
        final String mAddress;
        final String mFormat;
        final int mMessageCount;
        final String mQuery;
        final int mReferenceNumber;

        SmsReferenceKey(InboundSmsTracker tracker) {
            this.mAddress = tracker.getAddress();
            this.mReferenceNumber = tracker.getReferenceNumber();
            this.mMessageCount = tracker.getMessageCount();
            this.mQuery = tracker.getQueryForSegments();
            this.mFormat = tracker.getFormat();
        }

        /* access modifiers changed from: package-private */
        public String[] getDeleteWhereArgs() {
            return new String[]{this.mAddress, Integer.toString(this.mReferenceNumber), Integer.toString(this.mMessageCount)};
        }

        /* access modifiers changed from: package-private */
        public String getDeleteWhere() {
            return this.mQuery;
        }

        public int hashCode() {
            return (((this.mReferenceNumber * 31) + this.mMessageCount) * 31) + this.mAddress.hashCode();
        }

        public boolean equals(Object o) {
            if (!(o instanceof SmsReferenceKey)) {
                return false;
            }
            SmsReferenceKey other = (SmsReferenceKey) o;
            if (other.mAddress.equals(this.mAddress) && other.mReferenceNumber == this.mReferenceNumber && other.mMessageCount == this.mMessageCount) {
                return true;
            }
            return false;
        }
    }
}
