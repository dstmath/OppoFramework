package com.android.server.connectivity.networkrecovery.dnsresolve;

import android.hardware.contexthub.V1_0.HostEndPoint;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class Record implements MessageContent<Record> {
    private Domain domain;
    private RecordClass recordClass;
    private RecordData<?> recordData;
    private Type recordType;
    private long ttl;

    public Domain getDomain() {
        return this.domain;
    }

    public void setDomain(Domain domain2) {
        this.domain = domain2;
    }

    public Type getRecordType() {
        return this.recordType;
    }

    public void setRecordType(Type recordType2) {
        this.recordType = recordType2;
    }

    public RecordClass getRecordClass() {
        return this.recordClass;
    }

    public void setRecordClass(RecordClass recordClass2) {
        this.recordClass = recordClass2;
    }

    public long getTtl() {
        return this.ttl;
    }

    public void setTtl(long ttl2) {
        this.ttl = ttl2;
    }

    public RecordData<?> getRecordData() {
        return this.recordData;
    }

    public void setRecordData(RecordData<?> recordData2) {
        this.recordData = recordData2;
    }

    @Override // com.android.server.connectivity.networkrecovery.dnsresolve.MessageContent
    public Record toBytes(ByteBuffer buf) throws IOException {
        this.domain.toBytes(buf);
        buf.putShort((short) this.recordType.getCode());
        buf.putShort((short) this.recordClass.getCode());
        buf.putInt((int) this.ttl);
        ByteBuffer recordDataBuffer = ByteBuffer.allocate(65536);
        this.recordData.toBytes(recordDataBuffer);
        recordDataBuffer.flip();
        buf.putShort((short) recordDataBuffer.limit());
        buf.put(recordDataBuffer);
        return this;
    }

    @Override // com.android.server.connectivity.networkrecovery.dnsresolve.MessageContent
    public Record fromBytes(ByteBuffer buf) throws IOException, BufferUnderflowException {
        this.domain = new Domain().fromBytes(buf);
        try {
            this.recordType = Type.byCode(buf.getShort());
            this.recordClass = RecordClass.byCode(buf.getShort());
            this.ttl = (long) (buf.getInt() & -1);
            int recordDataLength = buf.getShort() & HostEndPoint.BROADCAST;
            int i = AnonymousClass1.$SwitchMap$com$android$server$connectivity$networkrecovery$dnsresolve$Type[this.recordType.ordinal()];
            if (i == 1) {
                this.recordData = new Cname();
            } else if (i == 2) {
                this.recordData = new A();
            }
            RecordData<?> recordData2 = this.recordData;
            if (recordData2 != null) {
                recordData2.setRecordLength(recordDataLength);
                this.recordData.fromBytes(buf);
            } else {
                PrintStream printStream = System.err;
                printStream.println("unknown type " + this.recordType);
                buf.get(new byte[recordDataLength]);
            }
            return this;
        } catch (BufferUnderflowException e) {
            throw new BufferUnderflowException();
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.connectivity.networkrecovery.dnsresolve.Record$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$connectivity$networkrecovery$dnsresolve$Type = new int[Type.values().length];

        static {
            try {
                $SwitchMap$com$android$server$connectivity$networkrecovery$dnsresolve$Type[Type.CNAME.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$connectivity$networkrecovery$dnsresolve$Type[Type.A.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public String toString() {
        return "Record [domain=" + this.domain + ", recordType=" + this.recordType + ", recordClass=" + this.recordClass + ", ttl=" + this.ttl + ", recordData=" + this.recordData + "]";
    }
}
