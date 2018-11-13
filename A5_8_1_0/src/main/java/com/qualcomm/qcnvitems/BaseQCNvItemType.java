package com.qualcomm.qcnvitems;

import com.qualcomm.qcrilhook.QcRilHook;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class BaseQCNvItemType {
    public abstract int getSize();

    public abstract byte[] toByteArray();

    public abstract String toDebugString();

    protected ByteBuffer createByteBuffer(int size) {
        ByteBuffer buf = ByteBuffer.allocate(size);
        buf.order(ByteOrder.nativeOrder());
        return buf;
    }

    public ByteBuffer createByteBuffer(byte[] bytes) {
        return QcRilHook.createBufferWithNativeByteOrder(bytes);
    }

    protected void skipPaddingBytes(ByteBuffer buf, int num) {
        buf.get(new byte[4], 0, num);
    }

    protected void addPaddingBytes(ByteBuffer buf, int num) {
        buf.put(new byte[4], 0, num);
    }

    protected byte[] pack(byte a, byte b) {
        ByteBuffer buf = ByteBuffer.allocate(getSize());
        buf.order(ByteOrder.nativeOrder());
        buf.put(a);
        buf.put(b);
        return buf.array();
    }

    protected byte[] pack(byte a, short b) {
        ByteBuffer buf = ByteBuffer.allocate(getSize());
        buf.order(ByteOrder.nativeOrder());
        buf.put(a);
        buf.putShort(b);
        return buf.array();
    }

    protected byte[] pack(byte a, int b) {
        ByteBuffer buf = ByteBuffer.allocate(getSize());
        buf.order(ByteOrder.nativeOrder());
        buf.put(a);
        buf.putInt(b);
        return buf.array();
    }

    protected byte[] pack(short a, short b) {
        ByteBuffer buf = ByteBuffer.allocate(getSize());
        buf.order(ByteOrder.nativeOrder());
        buf.putShort(a);
        buf.putShort(b);
        return buf.array();
    }

    protected byte[] pack(byte a, short b, short c) {
        ByteBuffer buf = ByteBuffer.allocate(getSize());
        buf.order(ByteOrder.nativeOrder());
        buf.put(a);
        buf.putShort(b);
        buf.putShort(c);
        return buf.array();
    }
}
