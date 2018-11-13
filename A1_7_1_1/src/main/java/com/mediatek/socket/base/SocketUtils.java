package com.mediatek.socket.base;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class SocketUtils {

    public interface ProtocolHandler {
        boolean decode(UdpServerInterface udpServerInterface);

        int getProtocolType();
    }

    public static class BaseBuffer {
        private byte[] mBuff;
        private int mOffset = 0;

        public BaseBuffer(int buffSize) {
            this.mBuff = new byte[buffSize];
        }

        public String toString() {
            StringBuilder o = new StringBuilder();
            o.append("BaseBuffer ");
            o.append("offset=[").append(this.mOffset).append("] ");
            o.append("maxSize=[").append(this.mBuff.length).append("] ");
            return o.toString();
        }

        public void setOffset(int position) {
            this.mOffset = position;
        }

        public int getOffset() {
            return this.mOffset;
        }

        public byte[] getBuff() {
            return this.mBuff;
        }

        public int size() {
            return this.mBuff.length;
        }

        public void clear() {
            this.mOffset = 0;
            Arrays.fill(this.mBuff, (byte) 0);
        }

        public void putBool(boolean data) {
            putByte(data ? (byte) 1 : (byte) 0);
        }

        public void putByte(byte data) {
            this.mBuff[this.mOffset] = data;
            this.mOffset++;
        }

        public void putShort(short data) {
            putByte((byte) (data & 255));
            putByte((byte) ((data >> 8) & 255));
        }

        public void putInt(int data) {
            putShort((short) (data & 65535));
            putShort((short) ((data >> 16) & 65535));
        }

        public void putLong(long data) {
            putInt((int) (data & -1));
            putInt((int) ((data >> 32) & -1));
        }

        public void putFloat(float data) {
            putInt(Float.floatToIntBits(data));
        }

        public void putDouble(double data) {
            putLong(Double.doubleToLongBits(data));
        }

        public void putString(String data) {
            assertNotNull(data);
            try {
                byte[] output = data.getBytes("utf8");
                putInt(output.length);
                System.arraycopy(output, 0, this.mBuff, this.mOffset, output.length);
                this.mOffset += output.length;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        public void putCodable(Codable data) {
            assertNotNull(data);
            data.encode(this);
        }

        public void putArrayBool(boolean[] data) {
            assertNotNull(data);
            putInt(data.length);
            for (boolean putBool : data) {
                putBool(putBool);
            }
        }

        public void putArrayByte(byte[] data) {
            assertNotNull(data);
            putInt(data.length);
            System.arraycopy(data, 0, this.mBuff, this.mOffset, data.length);
            this.mOffset += data.length;
        }

        public void putArrayShort(short[] data) {
            assertNotNull(data);
            putInt(data.length);
            for (short putShort : data) {
                putShort(putShort);
            }
        }

        public void putArrayInt(int[] data) {
            assertNotNull(data);
            putInt(data.length);
            for (int putInt : data) {
                putInt(putInt);
            }
        }

        public void putArrayLong(long[] data) {
            assertNotNull(data);
            putInt(data.length);
            for (long putLong : data) {
                putLong(putLong);
            }
        }

        public void putArrayFloat(float[] data) {
            assertNotNull(data);
            putInt(data.length);
            for (float putFloat : data) {
                putFloat(putFloat);
            }
        }

        public void putArrayDouble(double[] data) {
            assertNotNull(data);
            putInt(data.length);
            for (double putDouble : data) {
                putDouble(putDouble);
            }
        }

        public void putArrayString(String[] data) {
            assertNotNull(data);
            putInt(data.length);
            for (String putString : data) {
                putString(putString);
            }
        }

        public void putArrayCodable(Codable[] data) {
            assertNotNull(data);
            putInt(data.length);
            for (Codable putCodable : data) {
                putCodable(putCodable);
            }
        }

        public boolean getBool() {
            return getByte() != (byte) 0;
        }

        public byte getByte() {
            byte ret = this.mBuff[this.mOffset];
            this.mOffset++;
            return ret;
        }

        public short getShort() {
            return (short) ((getByte() << 8) | ((short) ((getByte() & 255) | 0)));
        }

        public int getInt() {
            return ((getShort() & 65535) | 0) | (getShort() << 16);
        }

        public long getLong() {
            return (0 | (((long) getInt()) & 4294967295L)) | (((long) getInt()) << 32);
        }

        public float getFloat() {
            return Float.intBitsToFloat(getInt());
        }

        public double getDouble() {
            return Double.longBitsToDouble(getLong());
        }

        public String getString() {
            int len = getInt();
            byte[] buff = new byte[len];
            System.arraycopy(this.mBuff, this.mOffset, buff, 0, len);
            this.mOffset += len;
            try {
                return new String(buff, "utf8").trim();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        }

        public Codable getCodable(Codable instance) {
            return instance.decode(this);
        }

        public boolean[] getArrayBool() {
            int len = getInt();
            boolean[] out = new boolean[len];
            for (int i = 0; i < len; i++) {
                out[i] = getBool();
            }
            return out;
        }

        public byte[] getArrayByte() {
            int len = getInt();
            byte[] buff = new byte[len];
            System.arraycopy(this.mBuff, this.mOffset, buff, 0, len);
            this.mOffset += len;
            return buff;
        }

        public short[] getArrayShort() {
            int len = getInt();
            short[] out = new short[len];
            for (int i = 0; i < len; i++) {
                out[i] = getShort();
            }
            return out;
        }

        public int[] getArrayInt() {
            int len = getInt();
            int[] out = new int[len];
            for (int i = 0; i < len; i++) {
                out[i] = getInt();
            }
            return out;
        }

        public long[] getArrayLong() {
            int len = getInt();
            long[] out = new long[len];
            for (int i = 0; i < len; i++) {
                out[i] = getLong();
            }
            return out;
        }

        public float[] getArrayFloat() {
            int len = getInt();
            float[] out = new float[len];
            for (int i = 0; i < len; i++) {
                out[i] = getFloat();
            }
            return out;
        }

        public double[] getArrayDouble() {
            int len = getInt();
            double[] out = new double[len];
            for (int i = 0; i < len; i++) {
                out[i] = getDouble();
            }
            return out;
        }

        public String[] getArrayString() {
            int len = getInt();
            String[] out = new String[len];
            for (int i = 0; i < len; i++) {
                out[i] = getString();
            }
            return out;
        }

        public Codable[] getArrayCodable(Codable instance) {
            int len = getInt();
            Codable[] out = new Codable[len];
            for (int i = 0; i < len; i++) {
                out[i] = getCodable(instance);
            }
            return out;
        }

        private void assertNotNull(Object object) {
            if (object == null) {
                throw new RuntimeException("assertNotNull() failed");
            }
        }
    }

    public interface Codable {
        Codable decode(BaseBuffer baseBuffer);

        void encode(BaseBuffer baseBuffer);

        Codable[] getArray(Codable[] codableArr);
    }

    public interface UdpServerInterface {
        BaseBuffer getBuff();

        boolean read();

        boolean setSoTimeout(int i);
    }

    public static void assertSize(Object data, int maxSize, int maxSize2) {
        int size = 0;
        if (data instanceof boolean[]) {
            size = ((boolean[]) data).length;
        } else if (data instanceof byte[]) {
            size = ((byte[]) data).length;
        } else if (data instanceof short[]) {
            size = ((short[]) data).length;
        } else if (data instanceof int[]) {
            size = ((int[]) data).length;
        } else if (data instanceof long[]) {
            size = ((long[]) data).length;
        } else if (data instanceof float[]) {
            size = ((float[]) data).length;
        } else if (data instanceof double[]) {
            size = ((double[]) data).length;
        } else if (data instanceof String[]) {
            size = ((String[]) data).length;
            for (String s : (String[]) data) {
                if (s.length() > maxSize2) {
                    throw new RuntimeException("your string.length=" + s.length() + " is more than maxSize2=" + maxSize2);
                }
            }
        } else if (data instanceof Object[]) {
            size = ((Object[]) data).length;
        } else if (data instanceof String) {
            size = ((String) data).length();
        }
        if (size > maxSize) {
            throw new RuntimeException("your size=" + size + " is more than maxSize=" + maxSize);
        }
    }

    public static void assertEqual(Object o1, Object o2) {
        if (o1 instanceof boolean[]) {
            boolean[] t1 = (boolean[]) o1;
            boolean[] t2 = (boolean[]) o2;
            if (!Arrays.equals(t1, t2)) {
                throw new RuntimeException("t1=[" + Arrays.toString(t1) + "] is not equal to t2=[" + Arrays.toString(t2) + "]");
            }
        } else if (o1 instanceof byte[]) {
            byte[] t12 = (byte[]) o1;
            byte[] t22 = (byte[]) o2;
            if (!Arrays.equals(t12, t22)) {
                throw new RuntimeException("t1=[" + Arrays.toString(t12) + "] is not equal to t2=[" + Arrays.toString(t22) + "]");
            }
        } else if (o1 instanceof short[]) {
            short[] t13 = (short[]) o1;
            short[] t23 = (short[]) o2;
            if (!Arrays.equals(t13, t23)) {
                throw new RuntimeException("t1=[" + Arrays.toString(t13) + "] is not equal to t2=[" + Arrays.toString(t23) + "]");
            }
        } else if (o1 instanceof int[]) {
            int[] t14 = (int[]) o1;
            int[] t24 = (int[]) o2;
            if (!Arrays.equals(t14, t24)) {
                throw new RuntimeException("t1=[" + Arrays.toString(t14) + "] is not equal to t2=[" + Arrays.toString(t24) + "]");
            }
        } else if (o1 instanceof long[]) {
            long[] t15 = (long[]) o1;
            long[] t25 = (long[]) o2;
            if (!Arrays.equals(t15, t25)) {
                throw new RuntimeException("t1=[" + Arrays.toString(t15) + "] is not equal to t2=[" + Arrays.toString(t25) + "]");
            }
        } else if (o1 instanceof float[]) {
            float[] t16 = (float[]) o1;
            float[] t26 = (float[]) o2;
            if (!Arrays.equals(t16, t26)) {
                throw new RuntimeException("t1=[" + Arrays.toString(t16) + "] is not equal to t2=[" + Arrays.toString(t26) + "]");
            }
        } else if (o1 instanceof double[]) {
            double[] t17 = (double[]) o1;
            double[] t27 = (double[]) o2;
            if (!Arrays.equals(t17, t27)) {
                throw new RuntimeException("t1=[" + Arrays.toString(t17) + "] is not equal to t2=[" + Arrays.toString(t27) + "]");
            }
        } else if (o1 instanceof String[]) {
            String[] t18 = (String[]) o1;
            String[] t28 = (String[]) o2;
            if (!Arrays.equals(t18, t28)) {
                throw new RuntimeException("t1=[" + Arrays.toString(t18) + "] is not equal to t2=[" + Arrays.toString(t28) + "]");
            }
        } else if (o1 instanceof Object[]) {
            Object[] t19 = (Object[]) o1;
            Object[] t29 = (Object[]) o2;
            if (!Arrays.equals(t19, t29)) {
                throw new RuntimeException("t1=[" + Arrays.toString(t19) + "] is not equal to t2=[" + Arrays.toString(t29) + "]");
            }
        } else if (!o1.equals(o2)) {
            throw new RuntimeException("o1=[" + o1 + "] is not equal to o2=[" + o2 + "]");
        }
    }
}
