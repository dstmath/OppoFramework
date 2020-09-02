package com.android.server.neuron.publish;

import android.os.Parcel;
import java.util.concurrent.atomic.AtomicInteger;

public final class Request {
    private static final int MAX_POOL_SIZE = 4;
    static final String TAG = "NeuronSystem";
    private static AtomicInteger sNextSerial = new AtomicInteger(0);
    private static Request sPool = null;
    private static int sPoolSize = 0;
    private static Object sPoolSync = new Object();
    private byte[] mData;
    private Request mNext;
    private Parcel mParcel;
    private int mSeq;
    private long mTimeStamp;

    static {
        sNextSerial.set(10000);
    }

    private Request() {
    }

    public static Request obtain() {
        Request req = null;
        synchronized (sPoolSync) {
            if (sPool != null) {
                req = sPool;
                sPool = sPool.mNext;
                req.mNext = null;
                sPoolSize--;
            }
        }
        if (req == null) {
            return new Request();
        }
        return req;
    }

    public Parcel prepare() {
        this.mTimeStamp = System.nanoTime() / 1000;
        this.mSeq = sNextSerial.getAndIncrement();
        this.mParcel = Parcel.obtain();
        this.mParcel.setDataPosition(0);
        this.mParcel.writeInt(0);
        this.mParcel.writeInt(61695);
        this.mParcel.writeInt(this.mSeq);
        return this.mParcel;
    }

    public void commit() {
        this.mParcel.setDataPosition(0);
        Parcel parcel = this.mParcel;
        parcel.writeInt(parcel.dataSize() - 4);
        this.mData = this.mParcel.marshall();
        this.mParcel.recycle();
        this.mParcel = null;
    }

    public void release() {
        synchronized (sPoolSync) {
            if (sPoolSize < 4) {
                this.mNext = sPool;
                sPool = this;
                sPoolSize++;
                this.mData = null;
            }
        }
    }

    public byte[] getBytes() {
        return this.mData;
    }

    public int getSequenceNumber() {
        return this.mSeq;
    }

    public long getTimeStamp() {
        return this.mTimeStamp;
    }
}
