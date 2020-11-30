package com.color.inner.mediatek.telephony;

import android.telephony.SmsMessage;
import android.util.Log;

public class MtkSmsMessageWrapper {
    private static final int INVALID_INT_RESULT = -1;
    private static final long INVALID_LONG_RESULT = -1;
    private static final String TAG = "MtkSmsMessageWrapper";
    private Object mMtkSmsMessage;

    private MtkSmsMessageWrapper() {
    }

    public MtkSmsMessageWrapper(Object obj) {
        this.mMtkSmsMessage = obj;
    }

    public static int[] calculateLength(CharSequence msgBody, boolean use7bitOnly) {
        try {
            Object result = Class.forName("mediatek.telephony.MtkSmsMessage").getMethod("calculateLength", CharSequence.class, Boolean.TYPE).invoke(null, msgBody, Boolean.valueOf(use7bitOnly));
            if (result == null || !(result instanceof int[])) {
                return null;
            }
            return (int[]) result;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public int getStatusOnIcc() {
        try {
            return ((Integer) Class.forName("mediatek.telephony.MtkSmsMessage").getMethod("getStatusOnIcc", new Class[0]).invoke(this.mMtkSmsMessage, new Object[0])).intValue();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public boolean isStatusReportMessage() {
        try {
            return ((Boolean) Class.forName("mediatek.telephony.MtkSmsMessage").getMethod("isStatusReportMessage", new Class[0]).invoke(this.mMtkSmsMessage, new Object[0])).booleanValue();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public int getIndexOnIcc() {
        try {
            return ((Integer) Class.forName("mediatek.telephony.MtkSmsMessage").getMethod("getIndexOnIcc", new Class[0]).invoke(this.mMtkSmsMessage, new Object[0])).intValue();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public String getServiceCenterAddress() {
        try {
            return (String) Class.forName("mediatek.telephony.MtkSmsMessage").getMethod("getServiceCenterAddress", new Class[0]).invoke(this.mMtkSmsMessage, new Object[0]);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public String getDisplayOriginatingAddress() {
        try {
            return (String) Class.forName("mediatek.telephony.MtkSmsMessage").getMethod("getDisplayOriginatingAddress", new Class[0]).invoke(this.mMtkSmsMessage, new Object[0]);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public String getDestinationAddress() {
        try {
            return (String) Class.forName("mediatek.telephony.MtkSmsMessage").getMethod("getDestinationAddress", new Class[0]).invoke(this.mMtkSmsMessage, new Object[0]);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public SmsMessage.MessageClass getMessageClass() {
        try {
            return (SmsMessage.MessageClass) Class.forName("mediatek.telephony.MtkSmsMessage").getMethod("getMessageClass", new Class[0]).invoke(this.mMtkSmsMessage, new Object[0]);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public String getDisplayMessageBody() {
        try {
            return (String) Class.forName("mediatek.telephony.MtkSmsMessage").getMethod("getDisplayMessageBody", new Class[0]).invoke(this.mMtkSmsMessage, new Object[0]);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public long getTimestampMillis() {
        try {
            return ((Long) Class.forName("mediatek.telephony.MtkSmsMessage").getMethod("getTimestampMillis", new Class[0]).invoke(this.mMtkSmsMessage, new Object[0])).longValue();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return INVALID_LONG_RESULT;
        }
    }
}
