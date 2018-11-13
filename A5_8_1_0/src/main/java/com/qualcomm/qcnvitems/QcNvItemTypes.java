package com.qualcomm.qcnvitems;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

public class QcNvItemTypes {
    public static final String LOG_TAG = "QcNvItemTypes";
    public static final int SIZE_OF_BYTE = 1;
    public static final int SIZE_OF_INT = 4;
    public static final int SIZE_OF_SHORT = 2;

    public static class EccListType extends BaseQCNvItemType {
        byte[][] mEccList = ((byte[][]) Array.newInstance(Byte.TYPE, new int[]{10, 3}));

        EccListType() {
        }

        EccListType(byte[] bArray) throws IOException {
            ByteBuffer buf = createByteBuffer(bArray);
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 3; j++) {
                    this.mEccList[i][j] = buf.get();
                }
            }
        }

        public void setEccList(String[] in) throws InvalidParameterException {
            this.mEccList = (byte[][]) Array.newInstance(Byte.TYPE, new int[]{10, 3});
            if (in == null || in.length != 10) {
                throw new InvalidParameterException();
            }
            int i = 0;
            while (i < 10) {
                int j;
                if (in[i] == null || in[i].length() == 0) {
                    for (j = 0; j < 3; j++) {
                        this.mEccList[i][j] = (byte) 0;
                    }
                } else if (in[i].length() != 3) {
                    throw new InvalidParameterException();
                } else {
                    for (j = 0; j < 3; j++) {
                        this.mEccList[i][j] = (byte) in[i].charAt(j);
                    }
                }
                i++;
            }
        }

        public String[] getEcclist() {
            String[] ret = new String[10];
            for (int i = 0; i < 10; i++) {
                ret[i] = "";
                boolean isAllZero = true;
                for (int j = 0; j < 3; j++) {
                    if (this.mEccList[i][j] != (byte) 0) {
                        isAllZero = false;
                    }
                    ret[i] = ret[i] + ((char) this.mEccList[i][j]);
                }
                if (isAllZero) {
                    ret[i] = "";
                }
            }
            return ret;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 3; j++) {
                    buf.put(this.mEccList[i][j]);
                }
            }
            return buf.array();
        }

        public int getSize() {
            return 30;
        }

        public String toDebugString() {
            String ret = "";
            for (int i = 0; i < 10; i++) {
                ret = ret + String.format("%d : ", new Object[]{Integer.valueOf(i)});
                for (int j = 0; j < 3; j++) {
                    ret = ret + ((char) this.mEccList[i][j]);
                }
                ret = ret + ", ";
            }
            return String.format("ecc_list : %s", new Object[]{ret});
        }
    }

    public static class NvAutoAnswerType {
        boolean enable;
        byte rings;

        public static int getSize() {
            return 2;
        }
    }

    public static class NvByte extends BaseQCNvItemType {
        byte mVal;

        NvByte() {
            this.mVal = (byte) 0;
        }

        NvByte(byte[] bArray) {
            this.mVal = bArray[0];
        }

        public int getSize() {
            return 1;
        }

        public String toDebugString() {
            return String.format("val=%d", new Object[]{Byte.valueOf(this.mVal)});
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            buf.put(this.mVal);
            return buf.array();
        }
    }

    public static class NvByteArray extends BaseQCNvItemType {
        private byte[] data;

        NvByteArray(byte[] bArray) throws IOException {
            this.data = new byte[bArray.length];
            for (int i = 0; i < bArray.length; i++) {
                this.data[i] = bArray[i];
            }
        }

        public byte[] getByteArray() {
            return this.data;
        }

        public byte getByte(int idx) throws IOException {
            return this.data[idx];
        }

        public byte setByte(int idx, byte value) throws IOException {
            this.data[idx] = value;
            return value;
        }

        public boolean getSpecifyByteArray(int startIdx, int length, byte[] outBuf) {
            if (startIdx < 0 || length <= 0 || startIdx + length > this.data.length) {
                return false;
            }
            int i = 0;
            int startIdx2 = startIdx;
            while (i < length) {
                startIdx = startIdx2 + 1;
                outBuf[i] = this.data[startIdx2];
                i++;
                startIdx2 = startIdx;
            }
            return true;
        }

        public boolean setSpecifyByteArray(int startIdx, int length, byte[] srcBuf) {
            if (startIdx < 0 || length <= 0 || startIdx + length > this.data.length) {
                return false;
            }
            int i = 0;
            int startIdx2 = startIdx;
            while (i < length) {
                startIdx = startIdx2 + 1;
                this.data[startIdx2] = srcBuf[i];
                i++;
                startIdx2 = startIdx;
            }
            return true;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(size);
            for (byte put : this.data) {
                buf.put(put);
            }
            return buf.array();
        }

        public int getSize() {
            return this.data.length;
        }

        public String getString() {
            String ret = "";
            for (byte b : this.data) {
                ret = ret + " " + b;
            }
            return ret;
        }

        public String toDebugString() {
            return String.format("NvByteArrary:%s", new Object[]{getString()});
        }
    }

    public static class NvCallCntType extends BaseQCNvItemType {
        int mCount;
        byte nam;

        NvCallCntType() {
            this.nam = (byte) 0;
            this.mCount = 0;
        }

        NvCallCntType(byte[] bArray) throws IOException {
            ByteBuffer buf = createByteBuffer(bArray);
            this.nam = buf.get();
            skipPaddingBytes(buf, 3);
            this.mCount = buf.getInt();
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            buf.put(this.nam);
            addPaddingBytes(buf, 3);
            buf.putInt(this.mCount);
            return buf.array();
        }

        public int getSize() {
            return 8;
        }

        public String toDebugString() {
            return String.format("nam:%d, Count:%d", new Object[]{Byte.valueOf(this.nam), Integer.valueOf(this.mCount)});
        }
    }

    public static class NvCarrierVersion extends BaseQCNvItemType {
        private byte[] mCarrierVersion;

        public NvCarrierVersion() {
            this.mCarrierVersion = new byte[124];
        }

        NvCarrierVersion(byte[] bArray) throws IOException {
            this.mCarrierVersion = new byte[124];
            ByteBuffer buf = createByteBuffer(bArray);
            int i = 0;
            while (i < 124 && i < bArray.length) {
                this.mCarrierVersion[i] = buf.get();
                i++;
            }
        }

        NvCarrierVersion(String carrierVersion) {
            this.mCarrierVersion = new byte[124];
            this.mCarrierVersion = carrierVersion.getBytes();
        }

        public String getCarrierVersion() {
            String ret = "";
            int i = 0;
            while (i < 124 && i < this.mCarrierVersion.length) {
                ret = ret + ((char) this.mCarrierVersion[i]);
                i++;
            }
            return ret;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            int i = 0;
            while (i < 124 && i < this.mCarrierVersion.length) {
                buf.put(this.mCarrierVersion[i]);
                i++;
            }
            return buf.array();
        }

        public int getSize() {
            return 124;
        }

        public String toDebugString() {
            return String.format("NvCarrierVersion:%s", new Object[]{getCarrierVersion()});
        }
    }

    public static class NvCdmaChType extends BaseQCNvItemType {
        short mChannelA;
        short mChannelB;
        byte mNam;

        NvCdmaChType() throws InvalidParameterException {
            this.mNam = (byte) 0;
            this.mChannelA = (short) 0;
            this.mChannelB = (short) 0;
        }

        NvCdmaChType(byte[] bArray) throws IOException {
            ByteBuffer buf = createByteBuffer(bArray);
            this.mNam = buf.get();
            skipPaddingBytes(buf, 1);
            this.mChannelA = buf.getShort();
            this.mChannelB = buf.getShort();
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            buf.put(this.mNam);
            addPaddingBytes(buf, 1);
            buf.putShort(this.mChannelA);
            buf.putShort(this.mChannelB);
            return buf.array();
        }

        public int getSize() {
            return 6;
        }

        public String toDebugString() {
            return String.format("nam:%x, channel_a:%d, channel_b:%d", new Object[]{Byte.valueOf(this.mNam), Short.valueOf(this.mChannelA), Short.valueOf(this.mChannelB)});
        }
    }

    public static class NvDirNumberType extends BaseQCNvItemType {
        byte[] mDirNumber;
        byte mNam;

        public NvDirNumberType() {
            this.mDirNumber = new byte[10];
            this.mNam = (byte) 0;
            this.mDirNumber = new byte[10];
        }

        NvDirNumberType(byte[] bArray) throws IOException {
            this.mDirNumber = new byte[10];
            ByteBuffer buf = createByteBuffer(bArray);
            this.mNam = buf.get();
            for (int i = 0; i < 10; i++) {
                this.mDirNumber[i] = buf.get();
            }
        }

        public void setDirNumber(String in) throws InvalidParameterException {
            if (in.length() != 10) {
                throw new InvalidParameterException();
            }
            int i = 0;
            while (i < 10) {
                if (Character.isDigit(Character.valueOf(in.charAt(i)).charValue())) {
                    this.mDirNumber[i] = (byte) in.charAt(i);
                    i++;
                } else {
                    throw new InvalidParameterException();
                }
            }
        }

        public String getDirNumber() {
            String ret = "";
            for (int i = 0; i < 10; i++) {
                ret = ret + ((char) this.mDirNumber[i]);
            }
            return ret;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            buf.put(this.mNam);
            for (int i = 0; i < 10; i++) {
                buf.put(this.mDirNumber[i]);
            }
            return buf.array();
        }

        public int getSize() {
            return 11;
        }

        public String toDebugString() {
            return String.format("nam:%x, dir_number:%s", new Object[]{Byte.valueOf(this.mNam), getDirNumber()});
        }
    }

    public static class NvEncryptImeiType extends BaseQCNvItemType {
        private byte[] mEncryptImei;

        NvEncryptImeiType() {
            this.mEncryptImei = new byte[15];
        }

        NvEncryptImeiType(byte[] bArray) throws IOException {
            this.mEncryptImei = new byte[15];
            ByteBuffer buf = createByteBuffer(bArray);
            for (int i = 0; i < 15; i++) {
                this.mEncryptImei[i] = buf.get(i);
            }
        }

        public String getEncryptImei() {
            String ret = "";
            for (int i = 0; i < 15; i++) {
                ret = ret + ((char) this.mEncryptImei[i]);
            }
            return ret;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            buf.put(this.mEncryptImei, 0, 15);
            return buf.array();
        }

        public int getSize() {
            return IQcNvItems.NV_FACTORY_DATA_SIZE;
        }

        public String toDebugString() {
            return String.format("encrypt_imei:%s", new Object[]{getEncryptImei()});
        }
    }

    public static class NvGpsSnrType extends BaseQCNvItemType {
        private byte[] mGpsSnr = new byte[124];

        NvGpsSnrType(byte[] bArray) throws IOException {
            for (int i = 0; i < bArray.length; i++) {
                this.mGpsSnr[i] = bArray[i];
            }
        }

        public String getGpsSnr() {
            String ret = "";
            for (int i = 0; i < 124; i++) {
                ret = ret + " " + this.mGpsSnr[i];
            }
            return ret;
        }

        public byte[] getGpsSnrByteArray() {
            return this.mGpsSnr;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            for (int i = 0; i < 124; i++) {
                buf.put(this.mGpsSnr[i]);
            }
            return buf.array();
        }

        public int getSize() {
            return 124;
        }

        public String toDebugString() {
            return String.format("gpssnr:%s", new Object[]{getGpsSnr()});
        }
    }

    public static class NvHomeSidNidType extends BaseQCNvItemType {
        byte mNam;
        NvSidNidPairType[] mPair;

        public NvHomeSidNidType() {
            this.mNam = (byte) 0;
            this.mPair = new NvSidNidPairType[20];
        }

        public NvHomeSidNidType(byte[] bArray) {
            ByteBuffer buf = createByteBuffer(bArray);
            this.mNam = buf.get();
            skipPaddingBytes(buf, 1);
            this.mPair = new NvSidNidPairType[20];
            for (int j = 0; j < 20; j++) {
                this.mPair[j] = new NvSidNidPairType();
                this.mPair[j].mSid = buf.getShort();
                this.mPair[j].mNid = buf.getShort();
            }
        }

        public int getSize() {
            return 82;
        }

        public String toDebugString() {
            String ppString = "" + "[";
            for (int j = 0; j < 20; j++) {
                ppString = ppString + this.mPair[j].toDebugString() + ", ";
            }
            return ppString + "]";
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            buf.put(this.mNam);
            addPaddingBytes(buf, 1);
            for (int j = 0; j < 20; j++) {
                buf.put(this.mPair[j].toByteArray());
            }
            return buf.array();
        }
    }

    public static class NvImsi1112Type extends BaseQCNvItemType {
        byte mImsi1112;
        byte mNam;

        public NvImsi1112Type() {
            this.mNam = (byte) 0;
            this.mImsi1112 = (byte) 0;
        }

        NvImsi1112Type(byte[] bArray) throws IOException {
            ByteBuffer buf = createByteBuffer(bArray);
            this.mNam = buf.get();
            this.mImsi1112 = buf.get();
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            buf.put(this.mNam);
            buf.put(this.mImsi1112);
            return buf.array();
        }

        public int getSize() {
            return 2;
        }

        public String toDebugString() {
            return String.format("nam:%x, imsi_11_12:%d", new Object[]{Byte.valueOf(this.mNam), Byte.valueOf(this.mImsi1112)});
        }
    }

    public static class NvImsiAddrNumType extends BaseQCNvItemType {
        byte mNam;
        byte mNum;

        NvImsiAddrNumType() {
            this.mNam = (byte) 0;
            this.mNum = (byte) 0;
        }

        NvImsiAddrNumType(byte[] bArray) throws IOException {
            ByteBuffer buf = createByteBuffer(bArray);
            this.mNam = buf.get();
            this.mNum = buf.get();
        }

        public byte[] toByteArray() {
            return pack(this.mNam, this.mNum);
        }

        public int getSize() {
            return 2;
        }

        public String toDebugString() {
            return String.format("nam:%x, num:%d", new Object[]{Byte.valueOf(this.mNam), Byte.valueOf(this.mNum)});
        }
    }

    public static class NvImsiMccType extends BaseQCNvItemType {
        short mImsiMcc;
        byte mNam;

        NvImsiMccType() throws InvalidParameterException {
            this.mNam = (byte) 0;
            this.mImsiMcc = (short) 0;
        }

        NvImsiMccType(byte[] bArray) throws IOException {
            ByteBuffer buf = createByteBuffer(bArray);
            this.mNam = buf.get();
            skipPaddingBytes(buf, 1);
            this.mImsiMcc = buf.getShort();
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            buf.put(this.mNam);
            addPaddingBytes(buf, 1);
            buf.putShort(this.mImsiMcc);
            return buf.array();
        }

        public int getSize() {
            return 4;
        }

        public String toDebugString() {
            return String.format("nam:%x, imsi_mcc:%d", new Object[]{Byte.valueOf(this.mNam), Short.valueOf(this.mImsiMcc)});
        }
    }

    public static class NvImsiType extends BaseQCNvItemType {
        int mImsi;
        byte mNam;

        NvImsiType() {
            this.mNam = (byte) 0;
            this.mImsi = 0;
        }

        NvImsiType(byte[] bArray) throws IOException {
            ByteBuffer buf = createByteBuffer(bArray);
            this.mNam = buf.get();
            this.mImsi = buf.getInt();
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            buf.put(this.mNam);
            addPaddingBytes(buf, 3);
            buf.putInt(this.mImsi);
            return buf.array();
        }

        public int getSize() {
            return 8;
        }

        public String toDebugString() {
            return String.format("nam:%x, imsi:%d", new Object[]{Byte.valueOf(this.mNam), Integer.valueOf(this.mImsi)});
        }
    }

    public static class NvInteger extends BaseQCNvItemType {
        int mVal;

        NvInteger() {
            this.mVal = 0;
        }

        NvInteger(byte[] bArray) {
            this.mVal = createByteBuffer(bArray).getInt();
        }

        public int getSize() {
            return 4;
        }

        public String toDebugString() {
            return String.format("val=%d", new Object[]{Integer.valueOf(this.mVal)});
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            buf.putInt(this.mVal);
            return buf.array();
        }
    }

    public static class NvIntegerArray extends BaseQCNvItemType {
        private int[] mVal;

        NvIntegerArray(int idx, int value) throws InvalidParameterException {
            if (idx < 0 || idx >= 31) {
                throw new InvalidParameterException();
            }
            this.mVal = new int[31];
            this.mVal[idx] = value;
        }

        NvIntegerArray(byte[] bArray) {
            this.mVal = new int[31];
            ByteBuffer buf = createByteBuffer(bArray);
            for (int i = 0; i < 31; i++) {
                this.mVal[i] = buf.getInt();
            }
        }

        public String toDebugString() {
            return String.format("val1=%d", new Object[]{Integer.valueOf(this.mVal[0])});
        }

        public int getSize() {
            return 124;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            for (int i = 0; i < 31; i++) {
                buf.putInt(this.mVal[i]);
            }
            return buf.array();
        }

        public int getInteger(int idx) throws InvalidParameterException {
            if (idx >= 0 && idx < 31) {
                return this.mVal[idx];
            }
            throw new InvalidParameterException();
        }

        public void setInteger(int idx, int value) throws InvalidParameterException {
            if (idx < 0 || idx >= 31) {
                throw new InvalidParameterException();
            }
            this.mVal[idx] = value;
        }
    }

    public static class NvLightSensorType extends BaseQCNvItemType {
        private int[] mLightSensor;

        NvLightSensorType(int[] lightSensor) {
            this.mLightSensor = new int[31];
            for (int i = 0; i < 3; i++) {
                this.mLightSensor[i] = lightSensor[i];
            }
        }

        NvLightSensorType(byte[] bArray) throws IOException {
            ByteBuffer buf = createByteBuffer(bArray);
            this.mLightSensor = new int[31];
            for (int i = 0; i < 3; i++) {
                this.mLightSensor[i] = buf.getInt();
            }
        }

        public int getSize() {
            return 124;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            for (int i = 0; i < 3; i++) {
                buf.putInt(this.mLightSensor[i]);
            }
            return buf.array();
        }

        public int[] getLightSensor() {
            return this.mLightSensor;
        }

        public String toDebugString() {
            return String.format("light_sensor: 0x%x 0x%x 0x%x", new Object[]{Integer.valueOf(this.mLightSensor[0]), Integer.valueOf(this.mLightSensor[1]), Integer.valueOf(this.mLightSensor[2])});
        }
    }

    public static class NvLockCodeType extends BaseQCNvItemType {
        byte[] mDigits;

        NvLockCodeType() {
            this.mDigits = new byte[4];
        }

        NvLockCodeType(byte[] bArray) throws IOException {
            this.mDigits = new byte[4];
            ByteBuffer buf = createByteBuffer(bArray);
            for (int i = 0; i < 4; i++) {
                this.mDigits[i] = buf.get();
            }
        }

        public void setLockCode(String in) throws InvalidParameterException {
            if (in == null || in.length() != 4) {
                throw new InvalidParameterException();
            }
            for (int i = 0; i < 4; i++) {
                this.mDigits[i] = (byte) in.charAt(i);
            }
        }

        public String getLockCode() {
            String ret = "";
            for (int i = 0; i < 4; i++) {
                ret = ret + ((char) this.mDigits[i]);
            }
            return ret;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            for (int i = 0; i < 4; i++) {
                buf.put(this.mDigits[i]);
            }
            return buf.array();
        }

        public int getSize() {
            return 4;
        }

        public String toDebugString() {
            return String.format("lock_code:%s", new Object[]{getLockCode()});
        }
    }

    public static class NvMin1Type extends BaseQCNvItemType {
        int[] mMin1;
        byte mNam;

        NvMin1Type() {
            this.mNam = (byte) 0;
            this.mMin1 = new int[2];
        }

        NvMin1Type(byte[] bArray) throws IOException {
            this.mMin1 = new int[2];
            ByteBuffer buf = createByteBuffer(bArray);
            this.mNam = buf.get();
            skipPaddingBytes(buf, 3);
            this.mMin1[0] = buf.getInt();
            this.mMin1[1] = buf.getInt();
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            buf.put(this.mNam);
            addPaddingBytes(buf, 3);
            buf.putInt(this.mMin1[0]);
            buf.putInt(this.mMin1[1]);
            return buf.array();
        }

        public int getSize() {
            return 12;
        }

        public String toDebugString() {
            return String.format("nam:%x, min1[0]:%x, min1[1]:%x", new Object[]{Byte.valueOf(this.mNam), Integer.valueOf(this.mMin1[0]), Integer.valueOf(this.mMin1[1])});
        }
    }

    public static class NvMin2Type extends BaseQCNvItemType {
        short[] mMin2;
        byte mNam;

        NvMin2Type() {
            this.mNam = (byte) 0;
            this.mMin2 = new short[2];
        }

        NvMin2Type(byte[] bArray) throws IOException {
            this.mMin2 = new short[2];
            ByteBuffer buf = createByteBuffer(bArray);
            this.mNam = buf.get();
            skipPaddingBytes(buf, 1);
            this.mMin2[0] = buf.getShort();
            this.mMin2[1] = buf.getShort();
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            buf.put(this.mNam);
            addPaddingBytes(buf, 1);
            buf.putShort(this.mMin2[0]);
            buf.putShort(this.mMin2[1]);
            return buf.array();
        }

        public int getSize() {
            return 6;
        }

        public String toDebugString() {
            return String.format("nam:%x, min1[0]:%x, min1[1]:%x", new Object[]{Byte.valueOf(this.mNam), Short.valueOf(this.mMin2[0]), Short.valueOf(this.mMin2[1])});
        }
    }

    public static class NvPcbNumberType extends BaseQCNvItemType {
        private byte[] mPcbNumber;

        public NvPcbNumberType() {
            this.mPcbNumber = new byte[33];
        }

        NvPcbNumberType(byte[] bArray) throws IOException {
            this.mPcbNumber = new byte[33];
            ByteBuffer buf = createByteBuffer(bArray);
            int i = 0;
            while (i < 32 && i < bArray.length) {
                this.mPcbNumber[i] = buf.get();
                i++;
            }
            this.mPcbNumber[i] = (byte) 0;
        }

        NvPcbNumberType(String pcbNumber) {
            this.mPcbNumber = new byte[33];
            byte[] szTemp = pcbNumber.getBytes();
            int length = szTemp.length;
            if (length >= 32) {
                length = 31;
            }
            this.mPcbNumber[0] = (byte) (length + 1);
            int i = 1;
            while (i <= length) {
                this.mPcbNumber[i] = szTemp[i - 1];
                i++;
            }
            this.mPcbNumber[i] = (byte) 0;
        }

        public String getPcbNumber() {
            String ret = "";
            for (int i = 1; i < 32; i++) {
                ret = ret + ((char) this.mPcbNumber[i]);
            }
            return ret;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            for (int i = 0; i < 32; i++) {
                buf.put(this.mPcbNumber[i]);
            }
            return buf.array();
        }

        public int getSize() {
            return 32;
        }

        public String toDebugString() {
            return String.format("pcb_number:%s", new Object[]{getPcbNumber()});
        }
    }

    public static class NvPrefVoiceSoType extends BaseQCNvItemType {
        boolean mEvrcCapabilityEnabled;
        short mHomeOrigVoiceSo;
        short mHomePageVoiceSo;
        byte mNam;
        short mRoamOrigVoiceSo;

        NvPrefVoiceSoType() throws InvalidParameterException {
            this.mNam = (byte) 0;
        }

        NvPrefVoiceSoType(byte[] bArray) throws IOException {
            ByteBuffer buf = createByteBuffer(bArray);
            this.mNam = buf.get();
            this.mEvrcCapabilityEnabled = (buf.get() == (byte) 1 ? Boolean.TRUE : Boolean.FALSE).booleanValue();
            this.mHomePageVoiceSo = buf.getShort();
            this.mHomeOrigVoiceSo = buf.getShort();
            this.mRoamOrigVoiceSo = buf.getShort();
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            buf.put(this.mNam);
            buf.put((byte) (this.mEvrcCapabilityEnabled ? 1 : 0));
            buf.putShort(this.mHomePageVoiceSo);
            buf.putShort(this.mHomeOrigVoiceSo);
            buf.putShort(this.mRoamOrigVoiceSo);
            return buf.array();
        }

        public int getSize() {
            return 8;
        }

        public String toDebugString() {
            return String.format("nam:%x, evrc_capability_enabled:%s, home_page_voice_so=%d, home_orig_voice_so=%d, roam_orig_voice_so=%d", new Object[]{Byte.valueOf(this.mNam), String.valueOf(this.mEvrcCapabilityEnabled), Short.valueOf(this.mHomePageVoiceSo), Short.valueOf(this.mHomeOrigVoiceSo), Short.valueOf(this.mRoamOrigVoiceSo)});
        }
    }

    public static class NvSecCodeType extends BaseQCNvItemType {
        byte[] mDigits;

        NvSecCodeType() {
            this.mDigits = new byte[6];
        }

        NvSecCodeType(byte[] bArray) throws IOException {
            this.mDigits = new byte[6];
            ByteBuffer buf = createByteBuffer(bArray);
            for (int i = 0; i < 6; i++) {
                this.mDigits[i] = buf.get();
            }
        }

        public void setSecCode(String in) throws InvalidParameterException {
            if (in == null || in.length() != 6) {
                throw new InvalidParameterException();
            }
            for (int i = 0; i < 6; i++) {
                this.mDigits[i] = (byte) in.charAt(i);
            }
        }

        public String getSecCode() {
            String ret = "";
            for (int i = 0; i < 6; i++) {
                ret = ret + ((char) this.mDigits[i]);
            }
            return ret;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            for (int i = 0; i < 6; i++) {
                buf.put(this.mDigits[i]);
            }
            return buf.array();
        }

        public int getSize() {
            return 6;
        }

        public String toDebugString() {
            return String.format("sec_code:%s", new Object[]{getSecCode()});
        }
    }

    public static class NvShort extends BaseQCNvItemType {
        short mVal;

        NvShort() {
            this.mVal = (short) 0;
        }

        NvShort(byte[] bArray) {
            this.mVal = createByteBuffer(bArray).getShort();
        }

        public int getSize() {
            return 2;
        }

        public String toDebugString() {
            return String.format("val=%d", new Object[]{Short.valueOf(this.mVal)});
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            buf.putShort(this.mVal);
            return buf.array();
        }
    }

    public static class NvSidNidPairType extends BaseQCNvItemType {
        short mNid;
        short mSid;

        NvSidNidPairType() {
            this.mSid = (short) 0;
            this.mNid = (short) 0;
        }

        public NvSidNidPairType(byte[] bArray) throws IOException {
            ByteBuffer buf = createByteBuffer(bArray);
            this.mSid = buf.getShort();
            this.mNid = buf.getShort();
        }

        public int getSize() {
            return 4;
        }

        public String toDebugString() {
            return String.format("SID:%d, NID:%d", new Object[]{Short.valueOf(this.mSid), Short.valueOf(this.mNid)});
        }

        public byte[] toByteArray() {
            return pack(this.mSid, this.mNid);
        }
    }

    public static class NvSidNidType extends BaseQCNvItemType {
        byte mNam;
        NvSidNidPairType[][] mPair;

        public NvSidNidType() {
            this.mNam = (byte) 0;
            this.mPair = (NvSidNidPairType[][]) Array.newInstance(NvSidNidPairType.class, new int[]{2, 1});
        }

        public NvSidNidType(byte[] bArray) {
            ByteBuffer buf = createByteBuffer(bArray);
            this.mNam = buf.get();
            skipPaddingBytes(buf, 1);
            this.mPair = (NvSidNidPairType[][]) Array.newInstance(NvSidNidPairType.class, new int[]{2, 1});
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 1; j++) {
                    this.mPair[i][j] = new NvSidNidPairType();
                    this.mPair[i][j].mSid = buf.getShort();
                    this.mPair[i][j].mNid = buf.getShort();
                }
            }
        }

        public int getSize() {
            return 10;
        }

        public String toDebugString() {
            String ppString = "";
            for (int i = 0; i < 2; i++) {
                ppString = ppString + "[";
                for (int j = 0; j < 1; j++) {
                    ppString = ppString + this.mPair[i][j].toDebugString() + ", ";
                }
                ppString = ppString + "]";
            }
            return ppString;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            buf.put(this.mNam);
            addPaddingBytes(buf, 1);
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 1; j++) {
                    buf.put(this.mPair[i][j].toByteArray());
                }
            }
            return buf.array();
        }
    }

    public static class NvSidType extends BaseQCNvItemType {
        byte mNam;
        short mSid;

        public NvSidType() {
            this.mNam = (byte) 0;
            this.mSid = (short) 0;
        }

        public NvSidType(byte[] bArray) {
            ByteBuffer buf = createByteBuffer(bArray);
            this.mNam = buf.get();
            skipPaddingBytes(buf, 1);
            this.mSid = buf.getShort();
        }

        public int getSize() {
            return 4;
        }

        public String toDebugString() {
            return String.format("NAM: %d, SID: %d", new Object[]{Byte.valueOf(this.mNam), Short.valueOf(this.mSid)});
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            buf.put(this.mNam);
            addPaddingBytes(buf, 1);
            buf.putShort(this.mSid);
            return buf.array();
        }
    }

    public static class NvSrvDomainPrefType extends BaseQCNvItemType {
        private short mSrvDomainPref;

        NvSrvDomainPrefType(int srvDomainPref) {
            this.mSrvDomainPref = PrimitiveParser.parseShort(srvDomainPref);
        }

        NvSrvDomainPrefType(byte[] bArray) throws IOException {
            this.mSrvDomainPref = createByteBuffer(bArray).getShort();
        }

        public int getSize() {
            return 4;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            buf.putShort(this.mSrvDomainPref);
            return buf.array();
        }

        public int getSrvDomainPref() {
            return this.mSrvDomainPref;
        }

        public String toDebugString() {
            return String.format("srv_domain_pref: %d", new Object[]{Short.valueOf(this.mSrvDomainPref)});
        }
    }

    public static class NvWifiPerfType extends BaseQCNvItemType {
        private int[] mWifiPerf;

        NvWifiPerfType(int wifiPerf) {
            this.mWifiPerf = new int[31];
            this.mWifiPerf[0] = wifiPerf;
        }

        NvWifiPerfType(byte[] bArray) throws IOException {
            this.mWifiPerf = new int[31];
            ByteBuffer buf = createByteBuffer(bArray);
            for (int i = 0; i < 31; i++) {
                this.mWifiPerf[i] = buf.getInt();
            }
        }

        public String getWifiPerf() {
            return String.valueOf(this.mWifiPerf[0]);
        }

        public int getWifiPerfInteger() {
            return this.mWifiPerf[0];
        }

        public byte[] toByteArray() {
            ByteBuffer buf = createByteBuffer(getSize());
            for (int i = 0; i < 31; i++) {
                buf.putInt(this.mWifiPerf[i]);
            }
            return buf.array();
        }

        public int getSize() {
            return 124;
        }

        public String toDebugString() {
            return String.format("wifiperf:%s", new Object[]{getWifiPerf()});
        }
    }
}
