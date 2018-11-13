package com.qualcomm.qcrilhook;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidParameterException;

public class BaseQmiTypes {
    public static final short DEFAULT_NAM = (short) 0;

    public static abstract class QmiBase {
        public static final ByteOrder QMI_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
        public static final String QMI_CHARSET = "US-ASCII";
        public static final int TLV_LENGTH_SIZE = 2;
        public static final int TLV_TYPE_SIZE = 1;

        public abstract String toString();

        public static ByteBuffer createByteBuffer(int size) {
            ByteBuffer buf = ByteBuffer.allocate(size);
            buf.order(QMI_BYTE_ORDER);
            return buf;
        }

        public static ByteBuffer createByteBuffer(byte[] bytes) {
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            buf.order(QMI_BYTE_ORDER);
            return buf;
        }
    }

    public static abstract class BaseQmiItemType extends QmiBase {
        public abstract int getSize();

        public abstract byte[] toByteArray();

        public byte[] toTlv(short type) throws InvalidParameterException {
            ByteBuffer buf = QmiBase.createByteBuffer(getSize() + 3);
            try {
                buf.put(PrimitiveParser.parseByte(type));
                buf.putShort(PrimitiveParser.parseShort(getSize()));
                buf.put(toByteArray());
                return buf.array();
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public static byte[] parseTlv(byte[] tlv) {
            ByteBuffer buf = QmiBase.createByteBuffer(tlv);
            buf.get();
            short size = buf.getShort();
            ByteBuffer bArray = ByteBuffer.allocate(size);
            for (short i = (short) 0; i < size; i++) {
                bArray.put(buf.get());
            }
            return bArray.array();
        }
    }

    public static abstract class BaseQmiStructType extends QmiBase {
        public abstract BaseQmiItemType[] getItems();

        public abstract short[] getTypes();

        public static byte[] parseData(ByteBuffer buf, int length) {
            byte[] data = new byte[length];
            for (int i = 0; i < length; i++) {
                data[i] = buf.get();
            }
            return data;
        }

        public String toString() {
            String temp = "";
            boolean isFirstItem = true;
            for (BaseQmiItemType i : getItems()) {
                if (isFirstItem) {
                    isFirstItem = false;
                } else {
                    temp = temp + ", ";
                }
                temp = temp + i.toString();
            }
            return temp;
        }
    }
}
