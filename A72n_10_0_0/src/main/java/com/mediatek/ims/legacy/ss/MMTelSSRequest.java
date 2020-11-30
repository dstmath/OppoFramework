package com.mediatek.ims.legacy.ss;

import android.os.AsyncResult;
import android.os.Message;
import android.os.Parcel;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandException;

/* access modifiers changed from: package-private */
/* compiled from: MMTelSSTransport */
public class MMTelSSRequest {
    static final String LOG_TAG = "MMTelSSReq";
    private static final int MAX_POOL_SIZE = 4;
    static int sNextSerial = 0;
    private static MMTelSSRequest sPool = null;
    private static int sPoolSize = 0;
    private static Object sPoolSync = new Object();
    static Object sSerialMonitor = new Object();
    MMTelSSRequest mNext;
    int mRequest;
    Message mResult;
    int mSerial;
    Parcel mp;
    Object requestParm;

    static MMTelSSRequest obtain(int request, Message result) {
        MMTelSSRequest rr;
        MMTelSSRequest rr2 = null;
        synchronized (sPoolSync) {
            if (sPool != null) {
                rr2 = sPool;
                sPool = rr2.mNext;
                rr2.mNext = null;
                sPoolSize--;
            }
        }
        if (rr2 == null) {
            rr = new MMTelSSRequest();
        } else {
            rr = rr2;
        }
        synchronized (sSerialMonitor) {
            int i = sNextSerial;
            sNextSerial = i + 1;
            rr.mSerial = i;
        }
        rr.mRequest = request;
        rr.mResult = result;
        rr.mp = Parcel.obtain();
        if (result == null || result.getTarget() != null) {
            rr.mp.writeInt(request);
            rr.mp.writeInt(rr.mSerial);
            return rr;
        }
        throw new NullPointerException("Message target must not be null");
    }

    /* access modifiers changed from: package-private */
    public void release() {
        synchronized (sPoolSync) {
            if (sPoolSize < 4) {
                this.mNext = sPool;
                sPool = this;
                sPoolSize++;
                this.mResult = null;
            }
        }
    }

    private MMTelSSRequest() {
    }

    static void resetSerial() {
        synchronized (sSerialMonitor) {
            sNextSerial = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public String serialString() {
        StringBuilder sb = new StringBuilder(8);
        String sn = Integer.toString(this.mSerial);
        sb.append('[');
        int s = sn.length();
        for (int i = 0; i < 4 - s; i++) {
            sb.append('0');
        }
        sb.append(sn);
        sb.append(']');
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public void onError(int error, Object ret) {
        CommandException ex = CommandException.fromRilErrno(error);
        Rlog.d(LOG_TAG, serialString() + "< " + MMTelSSTransport.requestToString(this.mRequest) + " error: " + ex);
        Message message = this.mResult;
        if (message != null) {
            AsyncResult.forMessage(message, ret, ex);
            this.mResult.sendToTarget();
        }
        Parcel parcel = this.mp;
        if (parcel != null) {
            parcel.recycle();
            this.mp = null;
        }
    }
}
