package com.android.server.connectivity.networkrecovery.dnsresolve;

public abstract class RecordData<T> implements MessageContent<RecordData<T>> {
    private int recordLength;

    /* access modifiers changed from: package-private */
    public void setRecordLength(int recordLength2) {
        this.recordLength = recordLength2;
    }

    /* access modifiers changed from: protected */
    public int getRecordLength() {
        return this.recordLength;
    }
}
