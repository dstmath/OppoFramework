package com.qualcomm.qcnvitems;

import com.qualcomm.qcrilhook.BaseQmiTypes.BaseQmiItemType;
import com.qualcomm.qcrilhook.BaseQmiTypes.BaseQmiStructType;
import com.qualcomm.qcrilhook.BaseQmiTypes.QmiBase;
import com.qualcomm.qcrilhook.PrimitiveParser;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiByte;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiString;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

public class QmiNvItemTypes {
    public static final String LOG_TAG = "QmiItemTypes";

    public static class AmrStatus extends BaseQmiItemType {
        public static final int GSM_AMR_STATUS_SIZE = 1;
        public static final int WCDMA_AMR_STATUS_SIZE = 1;
        private short gsmAmrStatus;
        private short wcdmaAmrStatus;

        public AmrStatus() {
            this((short) 0, (short) 0);
        }

        public AmrStatus(short gsmAmrStatus, short wcdmaAmrStatus) throws InvalidParameterException {
            try {
                PrimitiveParser.checkByte(gsmAmrStatus);
                PrimitiveParser.checkByte(wcdmaAmrStatus);
                this.gsmAmrStatus = gsmAmrStatus;
                this.wcdmaAmrStatus = wcdmaAmrStatus;
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public AmrStatus(byte[] bArray) throws InvalidParameterException {
            if (bArray.length < getSize()) {
                throw new InvalidParameterException();
            }
            ByteBuffer buf = QmiBase.createByteBuffer(bArray);
            this.gsmAmrStatus = PrimitiveParser.toUnsigned(buf.get());
            this.wcdmaAmrStatus = PrimitiveParser.toUnsigned(buf.get());
        }

        public short getGsmAmrStatus() {
            return this.gsmAmrStatus;
        }

        public short getWcdmaAmrStatus() {
            return this.wcdmaAmrStatus;
        }

        public int getSize() {
            return 2;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = QmiBase.createByteBuffer(getSize());
            buf.put(PrimitiveParser.parseByte(this.gsmAmrStatus));
            buf.put(PrimitiveParser.parseByte(this.gsmAmrStatus));
            return buf.array();
        }

        public String toString() {
            return String.format("gsmAmrStatus=%d, wcdmaAmrStatus=%d", new Object[]{Short.valueOf(this.gsmAmrStatus), Short.valueOf(this.wcdmaAmrStatus)});
        }
    }

    public static class AutoAnswer extends BaseQmiItemType {
        public static final short DEFAULT_RINGS = (short) 5;
        public static final int ENABLE_SIZE = 1;
        public static final int RINGS_SIZE = 1;
        boolean enable;
        short rings;

        public AutoAnswer(boolean enable, short rings) throws InvalidParameterException {
            this.enable = enable;
            if (enable) {
                setRings(rings);
            } else {
                setRings((short) 0);
            }
        }

        public AutoAnswer(short rings) {
            this(true, rings);
        }

        public AutoAnswer(boolean enable) {
            this(enable, (short) 5);
        }

        public AutoAnswer(byte[] bArray) throws InvalidParameterException {
            byte enable = QmiBase.createByteBuffer(bArray).get();
            if (enable == (byte) 0) {
                this.enable = false;
            } else if (enable == (byte) 1) {
                this.enable = true;
            } else {
                throw new InvalidParameterException();
            }
        }

        public boolean isEnabled() {
            return this.enable;
        }

        public void setEnabled(boolean enable) {
            this.enable = enable;
        }

        public short getRings() {
            return this.rings;
        }

        public void setRings(short rings) throws InvalidParameterException {
            try {
                PrimitiveParser.checkByte(rings);
                this.rings = rings;
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public int getSize() {
            return 1;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = QmiBase.createByteBuffer(getSize());
            if (this.enable) {
                buf.put((byte) 1);
            } else {
                buf.put((byte) 0);
            }
            return buf.array();
        }

        public String toString() {
            return String.format("enable=" + this.enable + ", rings=%d", new Object[]{Short.valueOf(this.rings)});
        }
    }

    public static class CdmaChannels extends BaseQmiItemType {
        public static final int PRIMARY_A_SIZE = 2;
        public static final int PRIMARY_B_SIZE = 2;
        public static final int SECONDARY_A_SIZE = 2;
        public static final int SECONDARY_B_SIZE = 2;
        private int primaryA;
        private int primaryB;
        private int secondaryA;
        private int secondaryB;

        public CdmaChannels(int primaryA, int primaryB, int secondaryA, int secondaryB) throws InvalidParameterException {
            setPrimaryChannelA(primaryA);
            setPrimaryChannelB(primaryB);
            setSecondaryChannelA(secondaryA);
            setSecondaryChannelB(secondaryB);
        }

        public CdmaChannels(byte[] bArray) throws InvalidParameterException {
            if (bArray.length < getSize()) {
                throw new InvalidParameterException();
            }
            ByteBuffer buf = QmiBase.createByteBuffer(bArray);
            this.primaryA = PrimitiveParser.toUnsigned(buf.getShort());
            this.primaryB = PrimitiveParser.toUnsigned(buf.getShort());
            this.secondaryA = PrimitiveParser.toUnsigned(buf.getShort());
            this.secondaryB = PrimitiveParser.toUnsigned(buf.getShort());
        }

        public int getPrimaryChannelA() {
            return this.primaryA;
        }

        public void setPrimaryChannelA(int primaryA) throws InvalidParameterException {
            try {
                PrimitiveParser.checkShort(primaryA);
                this.primaryA = primaryA;
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public int getPrimaryChannelB() {
            return this.primaryB;
        }

        public void setPrimaryChannelB(int primaryB) throws InvalidParameterException {
            try {
                PrimitiveParser.checkShort(primaryB);
                this.primaryB = primaryB;
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public int getSecondaryChannelA() {
            return this.secondaryA;
        }

        public void setSecondaryChannelA(int secondaryA) throws InvalidParameterException {
            try {
                PrimitiveParser.checkShort(secondaryA);
                this.secondaryA = secondaryA;
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public int getSecondaryChannelB() {
            return this.secondaryB;
        }

        public void setSecondaryChannelB(int secondaryB) throws InvalidParameterException {
            try {
                PrimitiveParser.checkShort(secondaryB);
                this.secondaryB = secondaryB;
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public int getSize() {
            return 8;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = QmiBase.createByteBuffer(getSize());
            buf.putShort(PrimitiveParser.parseShort(this.primaryA));
            buf.putShort(PrimitiveParser.parseShort(this.primaryB));
            buf.putShort(PrimitiveParser.parseShort(this.secondaryA));
            buf.putShort(PrimitiveParser.parseShort(this.secondaryB));
            return buf.array();
        }

        public String toString() {
            return String.format("primaryA=%d, primaryB=%d, secondaryA=%d, secondaryB=%d", new Object[]{Integer.valueOf(this.primaryA), Integer.valueOf(this.primaryB), Integer.valueOf(this.secondaryA), Integer.valueOf(this.secondaryB)});
        }
    }

    public static class FieldTypeValues {
        public static final short AKEY_TYPE = (short) 1;
        public static final short MOB_CAI_REV_TYPE = (short) 1;
        public static final short RTRE_CONFIG_TYPE = (short) 16;
    }

    public static class MinImsi extends BaseQmiItemType {
        public static final int IMSI11_12_SIZE = 2;
        public static final int IMSI_S1_SIZE = 7;
        public static final int IMSI_S2_SIZE = 3;
        public static final int MCC_SIZE = 3;
        protected String imsi11_12;
        protected String imsiS1;
        protected String imsiS2;
        protected String mcc;

        public MinImsi() throws InvalidParameterException {
            setMcc("000");
            setImsi11_12("00");
            setImsiS1("0000000");
            setImsiS2("000");
        }

        public MinImsi(String mcc, String imsi11_12, String imsiS1, String imsiS2) throws InvalidParameterException {
            setMcc(mcc);
            setImsi11_12(imsi11_12);
            setImsiS1(imsiS1);
            setImsiS2(imsiS2);
        }

        public MinImsi(byte[] bArray) throws InvalidParameterException {
            if (bArray.length < getSize()) {
                throw new InvalidParameterException();
            }
            int i;
            ByteBuffer buf = QmiBase.createByteBuffer(bArray);
            this.mcc = "";
            for (i = 0; i < 3; i++) {
                this.mcc += Character.toString((char) buf.get());
            }
            this.imsi11_12 = "";
            for (i = 0; i < 2; i++) {
                this.imsi11_12 += Character.toString((char) buf.get());
            }
            this.imsiS1 = "";
            for (i = 0; i < 7; i++) {
                this.imsiS1 += Character.toString((char) buf.get());
            }
            this.imsiS2 = "";
            for (i = 0; i < 3; i++) {
                this.imsiS2 += Character.toString((char) buf.get());
            }
        }

        public String getMcc() {
            return this.mcc;
        }

        public void setMcc(String mcc) throws InvalidParameterException {
            if (mcc.length() != 3) {
                throw new InvalidParameterException();
            }
            this.mcc = mcc;
        }

        public String getImsi11_12() {
            return this.imsi11_12;
        }

        public void setImsi11_12(String imsi11_12) throws InvalidParameterException {
            if (imsi11_12.length() != 2) {
                throw new InvalidParameterException();
            }
            this.imsi11_12 = imsi11_12;
        }

        public String getImsiS1() {
            return this.imsiS1;
        }

        public void setImsiS1(String imsiS1) throws InvalidParameterException {
            if (imsiS1.length() != 7) {
                throw new InvalidParameterException();
            }
            this.imsiS1 = imsiS1;
        }

        public String getImsiS2() {
            return this.imsiS2;
        }

        public void setImsiS2(String imsiS2) throws InvalidParameterException {
            if (imsiS2.length() != 3) {
                throw new InvalidParameterException();
            }
            this.imsiS2 = imsiS2;
        }

        public String getImsiNumber() {
            return this.imsiS1 + this.imsiS2;
        }

        public void setImsiNumber(String phNumber) throws InvalidParameterException {
            if (phNumber.length() != 10) {
                throw new InvalidParameterException();
            }
            try {
                setImsiS1(phNumber.substring(0, 7));
                setImsiS2(phNumber.substring(phNumber.length() - 3, phNumber.length()));
            } catch (IndexOutOfBoundsException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public int getSize() {
            return 15;
        }

        public byte[] toByteArray() {
            int i;
            ByteBuffer buf = QmiBase.createByteBuffer(getSize());
            for (i = 0; i < 3; i++) {
                buf.put((byte) this.mcc.charAt(i));
            }
            for (i = 0; i < 2; i++) {
                buf.put((byte) this.imsi11_12.charAt(i));
            }
            for (i = 0; i < 7; i++) {
                buf.put((byte) this.imsiS1.charAt(i));
            }
            for (i = 0; i < 3; i++) {
                buf.put((byte) this.imsiS2.charAt(i));
            }
            return buf.array();
        }

        public String toString() {
            return "mcc=" + this.mcc + ", imsi11_12=" + this.imsi11_12 + ", imsiS1=" + this.imsiS1 + ", imsiS2=" + this.imsiS2;
        }

        public static String minToPhString(int min1Value, short min2Value) {
            try {
                String[] table = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
                int tempValue = min1Value >> 14;
                String phString = ((((("" + table[(min2Value / 100) % 10]) + table[(min2Value / 10) % 10]) + table[min2Value % 10]) + table[(tempValue / 100) % 10]) + table[(tempValue / 10) % 10]) + table[tempValue % 10];
                tempValue = ((min1Value & 16383) >> 10) & 15;
                StringBuilder append = new StringBuilder().append(phString);
                if (tempValue == 10) {
                    tempValue = 0;
                }
                phString = append.append(String.valueOf(tempValue)).toString();
                tempValue = min1Value & 1023;
                return ((phString + table[(tempValue / 100) % 10]) + table[(tempValue / 10) % 10]) + table[tempValue % 10];
            } catch (Exception e) {
                return "";
            }
        }

        public static int phStringToMin1(String phString) throws InvalidParameterException {
            int[] table = new int[]{9, 0, 1, 2, 3, 4, 5, 6, 7, 8};
            if (phString.length() != 10) {
                throw new InvalidParameterException("Invalid phone number");
            }
            char[] ph = phString.toCharArray();
            int min1 = ((short) ((((short) ((((short) (table[ph[3] - 48] + 0)) * 10) + table[ph[4] - 48])) * 10) + table[ph[5] - 48])) << 14;
            int tempValue = ph[6] - 48;
            if (tempValue == 0) {
                tempValue = 10;
            }
            return (min1 + (tempValue << 10)) + ((short) ((((short) ((((short) (table[ph[7] - 48] + 0)) * 10) + table[ph[8] - 48])) * 10) + table[ph[9] - 48]));
        }

        public static short phStringToMin2(String phString) throws InvalidParameterException {
            short[] table = new short[]{(short) 9, (short) 0, (short) 1, (short) 2, (short) 3, (short) 4, (short) 5, (short) 6, (short) 7, (short) 8};
            if (phString.length() != 10) {
                throw new InvalidParameterException("Invalid phone number");
            }
            char[] ph = phString.toCharArray();
            return (short) ((((short) ((((short) (table[ph[0] - 48] + 0)) * 10) + table[ph[1] - 48])) * 10) + table[ph[2] - 48]);
        }
    }

    public static class PreferredVoiceSo extends BaseQmiItemType {
        public static final int EVRC_CAPABILITY_SIZE = 1;
        public static final int HOME_ORIG_VOICE_SO_SIZE = 2;
        public static final int HOME_PAGE_VOICE_SO_SIZE = 2;
        public static final int NAM_ID_SIZE = 1;
        public static final int ROAM_ORIG_VOICE_SO_SIZE = 2;
        public static final int VOICE_13K = 32768;
        public static final int VOICE_13K_IS733 = 17;
        public static final int VOICE_4GV_NARROW_BAND = 68;
        public static final int VOICE_4GV_WIDE_BAND = 70;
        public static final int VOICE_EVRC = 3;
        public static final int VOICE_IS_96 = 32769;
        public static final int VOICE_IS_96_A = 1;
        public static final int VOICE_WVRC = 32803;
        private short evrcCapability;
        private int homeOrigVoiceSo;
        private int homePageVoiceSo;
        private short namId;
        private int roamOrigVoiceSo;

        public PreferredVoiceSo(short evrcCapability, int homePageVoiceSo, int homeOrigVoiceSo, int roamOrigVoiceSo, short namId) throws InvalidParameterException {
            setNamId(namId);
            setEvrcCapability(evrcCapability);
            setHomePageVoiceSo(homePageVoiceSo);
            setHomeOrigVoiceSo(homeOrigVoiceSo);
            setRoamOrigVoiceSo(roamOrigVoiceSo);
        }

        public PreferredVoiceSo(short evrcCapability, int homePageVoiceSo, int homeOrigVoiceSo, int roamOrigVoiceSo) throws InvalidParameterException {
            this(evrcCapability, homePageVoiceSo, homeOrigVoiceSo, roamOrigVoiceSo, (short) 0);
        }

        public PreferredVoiceSo(byte[] bArray) throws InvalidParameterException {
            if (bArray.length < getSize()) {
                throw new InvalidParameterException();
            }
            ByteBuffer buf = QmiBase.createByteBuffer(bArray);
            this.namId = PrimitiveParser.toUnsigned(buf.get());
            this.evrcCapability = PrimitiveParser.toUnsigned(buf.get());
            this.homePageVoiceSo = PrimitiveParser.toUnsigned(buf.getShort());
            this.homeOrigVoiceSo = PrimitiveParser.toUnsigned(buf.getShort());
            this.roamOrigVoiceSo = PrimitiveParser.toUnsigned(buf.getShort());
        }

        public short getNamId() {
            return this.namId;
        }

        public void setNamId(short namId) throws InvalidParameterException {
            try {
                PrimitiveParser.checkByte(namId);
                this.namId = namId;
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public short getEvrcCapability() {
            return this.evrcCapability;
        }

        public void setEvrcCapability(short evrcCapability) throws InvalidParameterException {
            try {
                PrimitiveParser.checkByte(evrcCapability);
                this.evrcCapability = evrcCapability;
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public int getHomePageVoiceSo() {
            return this.homePageVoiceSo;
        }

        public void setHomePageVoiceSo(int homePageVoiceSo) throws InvalidParameterException {
            try {
                PrimitiveParser.checkShort(homePageVoiceSo);
                this.homePageVoiceSo = homePageVoiceSo;
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public int getHomeOrigVoiceSo() {
            return this.homeOrigVoiceSo;
        }

        public void setHomeOrigVoiceSo(int homeOrigVoiceSo) throws InvalidParameterException {
            try {
                PrimitiveParser.checkShort(homeOrigVoiceSo);
                this.homeOrigVoiceSo = homeOrigVoiceSo;
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public int getRoamOrigVoiceSo() {
            return this.roamOrigVoiceSo;
        }

        public void setRoamOrigVoiceSo(int roamOrigVoiceSo) throws InvalidParameterException {
            try {
                PrimitiveParser.checkShort(roamOrigVoiceSo);
                this.roamOrigVoiceSo = roamOrigVoiceSo;
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public int getSize() {
            return 8;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = QmiBase.createByteBuffer(getSize());
            buf.put(PrimitiveParser.parseByte(this.namId));
            buf.put(PrimitiveParser.parseByte(this.evrcCapability));
            buf.putShort(PrimitiveParser.parseShort(this.homePageVoiceSo));
            buf.putShort(PrimitiveParser.parseShort(this.homeOrigVoiceSo));
            buf.putShort(PrimitiveParser.parseShort(this.roamOrigVoiceSo));
            return buf.array();
        }

        public String toString() {
            return String.format("namId=%d, evrcCapability=%d, homePageVoiceSo=%d, homeOrigVoiceSo=%d, roamOrigVoiceSo=%d", new Object[]{Short.valueOf(this.namId), Short.valueOf(this.evrcCapability), Integer.valueOf(this.homePageVoiceSo), Integer.valueOf(this.homeOrigVoiceSo), Integer.valueOf(this.roamOrigVoiceSo)});
        }
    }

    public static class SidNid extends BaseQmiItemType {
        public static final int NID_SIZE = 2;
        public static final int NUM_INSTANCES_SIZE = 1;
        public static final int SID_SIZE = 2;
        private int[] nid;
        private short numInstances;
        private int[] sid;

        public SidNid() {
            this.numInstances = (short) 0;
        }

        public SidNid(int sid, int nid) {
            this((short) 1, new int[]{sid}, new int[]{nid});
        }

        public SidNid(int[] sid, int[] nid) throws InvalidParameterException {
            this((short) sid.length, sid, nid);
        }

        public SidNid(short numInstances, int[] sid, int[] nid) throws InvalidParameterException {
            setSidNid(numInstances, sid, nid);
        }

        public SidNid(byte[] bArray) throws InvalidParameterException {
            if (bArray.length < 1) {
                throw new InvalidParameterException();
            }
            ByteBuffer buf = QmiBase.createByteBuffer(bArray);
            this.numInstances = PrimitiveParser.toUnsigned(buf.get());
            if (bArray.length < getSize()) {
                throw new InvalidParameterException();
            }
            for (short i = (short) 0; i < this.numInstances; i++) {
                this.sid[i] = PrimitiveParser.toUnsigned(buf.getShort());
                this.nid[i] = PrimitiveParser.toUnsigned(buf.getShort());
            }
        }

        public void setNumInstances(short numInstances) throws InvalidParameterException {
            try {
                PrimitiveParser.checkByte(numInstances);
                this.numInstances = numInstances;
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public int getNumInstances() {
            return this.numInstances;
        }

        public int getSid() {
            return getSid(0);
        }

        public int getSid(int index) {
            return this.sid[index];
        }

        public void setSid(int sid) throws InvalidParameterException {
            try {
                setSid(sid, 0);
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public void setSid(int sid, int index) throws InvalidParameterException {
            try {
                PrimitiveParser.checkShort(sid);
                this.sid[index] = sid;
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public int getNid() {
            return getNid(0);
        }

        public int getNid(int index) {
            return this.nid[index];
        }

        public void setNid(int nid) throws InvalidParameterException {
            try {
                PrimitiveParser.checkShort(nid);
                setNid(nid, 0);
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public void setNid(int nid, int index) throws InvalidParameterException {
            try {
                PrimitiveParser.checkShort(nid);
                this.nid[index] = nid;
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public void setSidNid(short numInstances, int[] sid, int[] nid) throws InvalidParameterException {
            if (sid.length == nid.length && sid.length == numInstances) {
                try {
                    PrimitiveParser.checkByte(numInstances);
                    for (short i = (short) 0; i < numInstances; i++) {
                        PrimitiveParser.checkShort(sid[i]);
                        PrimitiveParser.checkShort(nid[i]);
                    }
                    this.numInstances = numInstances;
                    this.sid = sid;
                    this.nid = nid;
                    return;
                } catch (NumberFormatException e) {
                    throw new InvalidParameterException(e.toString());
                }
            }
            throw new InvalidParameterException();
        }

        public void setSidNid(int[] sid, int[] nid) throws InvalidParameterException {
            setSidNid((short) sid.length, sid, nid);
        }

        public int getSize() {
            return (this.numInstances * 4) + 1;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = QmiBase.createByteBuffer(getSize());
            buf.put(PrimitiveParser.parseByte(this.numInstances));
            for (short i = (short) 0; i < this.numInstances; i++) {
                buf.putShort(PrimitiveParser.parseShort(this.sid[i]));
                buf.putShort(PrimitiveParser.parseShort(this.nid[i]));
            }
            return buf.array();
        }

        public String toString() {
            String temp = String.format("num_instances=%d", new Object[]{Short.valueOf(this.numInstances)});
            for (short i = (short) 0; i < this.numInstances; i++) {
                temp = temp + String.format(", sid[%d]=%d, nid[%d]=%d", new Object[]{Integer.valueOf(i), Integer.valueOf(this.sid[i]), Integer.valueOf(i), Integer.valueOf(this.nid[i])});
            }
            return temp;
        }
    }

    public static class Threegpp2Info extends BaseQmiStructType {
        public static final short GET_CDMA_CHANNELS_TYPE = (short) 21;
        public static final short GET_DIR_NUM_TYPE = (short) 17;
        public static final short GET_MIN_IMSI_TYPE = (short) 19;
        public static final short GET_NAM_NAME_TYPE = (short) 16;
        public static final short GET_SID_NID_TYPE = (short) 18;
        public static final short GET_TRUE_IMSI_TYPE = (short) 20;
        public static final short NAM_ID_TYPE = (short) 1;
        public static final short SET_CDMA_CHANNELS_TYPE = (short) 20;
        public static final short SET_DIR_NUM_TYPE = (short) 16;
        public static final short SET_MIN_IMSI_TYPE = (short) 18;
        public static final short SET_SID_NID_TYPE = (short) 17;
        public static final short SET_TRUE_IMSI_TYPE = (short) 19;
        private CdmaChannels cdmaChannels;
        private QmiString dirNum;
        private boolean inSetMode;
        private MinImsi minImsi;
        private QmiString namName;
        private SidNid sidNid;
        private TrueImsi trueImsi;

        public Threegpp2Info(String dirNum, SidNid sidNid, MinImsi minImsi, TrueImsi trueImsi, CdmaChannels cdmaChannels) {
            this.dirNum = new QmiString(dirNum);
            this.sidNid = sidNid;
            this.minImsi = minImsi;
            this.trueImsi = trueImsi;
            this.cdmaChannels = cdmaChannels;
            this.inSetMode = true;
        }

        public Threegpp2Info(String namName, String dirNum, SidNid sidNid, MinImsi minImsi, TrueImsi trueImsi, CdmaChannels cdmaChannels) {
            this(dirNum, sidNid, minImsi, trueImsi, cdmaChannels);
            this.namName = new QmiString(namName);
            this.inSetMode = false;
        }

        public Threegpp2Info(byte[] qmiMsg) {
            this(qmiMsg, false);
        }

        public Threegpp2Info(byte[] qmiMsg, boolean inSetMode) {
            this.inSetMode = inSetMode;
            ByteBuffer buf = ByteBuffer.wrap(qmiMsg);
            buf.order(BaseQmiItemType.QMI_BYTE_ORDER);
            int size = qmiMsg.length;
            while (size > 0) {
                short type = PrimitiveParser.toUnsigned(buf.get());
                int length = PrimitiveParser.toUnsigned(buf.getShort());
                byte[] data = BaseQmiStructType.parseData(buf, length);
                size -= length + 3;
                if (!inSetMode) {
                    switch (type) {
                        case (short) 16:
                            this.namName = new QmiString(data);
                            break;
                        case (short) 17:
                            this.dirNum = new QmiString(data);
                            break;
                        case QcNvItemIds.NV_ANALOG_HOME_SID_I /*18*/:
                            this.sidNid = new SidNid(data);
                            break;
                        case (short) 19:
                            this.minImsi = new MinImsi(data);
                            break;
                        case (short) 20:
                            this.trueImsi = new TrueImsi(data);
                            break;
                        case QcNvItemIds.NV_SCDMACH_I /*21*/:
                            this.cdmaChannels = new CdmaChannels(data);
                            break;
                        default:
                            break;
                    }
                }
                switch (type) {
                    case (short) 16:
                        this.dirNum = new QmiString(data);
                        break;
                    case (short) 17:
                        this.sidNid = new SidNid(data);
                        break;
                    case QcNvItemIds.NV_ANALOG_HOME_SID_I /*18*/:
                        this.minImsi = new MinImsi(data);
                        break;
                    case (short) 19:
                        this.trueImsi = new TrueImsi(data);
                        break;
                    case (short) 20:
                        this.cdmaChannels = new CdmaChannels(data);
                        break;
                    default:
                        break;
                }
            }
        }

        public String getNamName() {
            if (this.inSetMode) {
                return "";
            }
            return this.namName.toStringValue();
        }

        public String getDirNum() {
            return this.dirNum.toStringValue();
        }

        public void setDirNum(String dirNum) {
            this.inSetMode = true;
            this.dirNum = new QmiString(dirNum);
        }

        public SidNid getSidNid() {
            return this.sidNid;
        }

        public void setSidNid(SidNid sidNid) {
            this.inSetMode = true;
            this.sidNid = sidNid;
        }

        public MinImsi getMinImsi() {
            return this.minImsi;
        }

        public void setMinImsi(MinImsi minImsi) {
            this.inSetMode = true;
            this.minImsi = minImsi;
        }

        public TrueImsi getTrueImsi() {
            return this.trueImsi;
        }

        public void setTrueImsi(TrueImsi trueImsi) {
            this.inSetMode = true;
            this.trueImsi = trueImsi;
        }

        public CdmaChannels getCdmaChannels() {
            return this.cdmaChannels;
        }

        public void setCdmaChannels(CdmaChannels cdmaChannels) {
            this.inSetMode = true;
            this.cdmaChannels = cdmaChannels;
        }

        public BaseQmiItemType[] getItems() {
            return getItems(this.inSetMode);
        }

        public BaseQmiItemType[] getItems(boolean inSetMode) {
            if (inSetMode) {
                return new BaseQmiItemType[]{this.dirNum, this.sidNid, this.minImsi, this.trueImsi, this.cdmaChannels};
            }
            return new BaseQmiItemType[]{this.namName, this.dirNum, this.sidNid, this.minImsi, this.trueImsi, this.cdmaChannels};
        }

        public short[] getTypes() {
            return getTypes(this.inSetMode);
        }

        public static short[] getTypes(boolean inSetMode) {
            return inSetMode ? new short[]{(short) 16, (short) 17, (short) 18, (short) 19, (short) 20} : new short[]{(short) 16, (short) 17, (short) 18, (short) 19, (short) 20, (short) 21};
        }
    }

    public static class TimerCount extends BaseQmiItemType {
        public static final int NAM_ID_SIZE = 1;
        public static final int TIMER_SIZE = 4;
        private short namId;
        private long timerCount;

        public TimerCount(long timerCount, short namId) throws InvalidParameterException {
            setNamId(namId);
            setTimerCount(timerCount);
        }

        public TimerCount(long timerCount) throws InvalidParameterException {
            this(timerCount, (short) 0);
        }

        public TimerCount(byte[] bArray) throws InvalidParameterException {
            if (bArray.length < getSize()) {
                throw new InvalidParameterException();
            }
            ByteBuffer buf = QmiBase.createByteBuffer(bArray);
            this.namId = PrimitiveParser.toUnsigned(buf.get());
            this.timerCount = PrimitiveParser.toUnsigned(buf.getInt());
        }

        public short getNamId() {
            return this.namId;
        }

        public void setNamId(short namId) {
            PrimitiveParser.checkByte(namId);
            this.namId = namId;
        }

        public long getTimerCount() {
            return this.timerCount;
        }

        public void setTimerCount(long timerCount) {
            PrimitiveParser.checkInt(timerCount);
            this.timerCount = timerCount;
        }

        public int getSize() {
            return 5;
        }

        public byte[] toByteArray() {
            ByteBuffer buf = QmiBase.createByteBuffer(8);
            buf.put(PrimitiveParser.parseByte(this.namId));
            buf.putInt(PrimitiveParser.parseInt(this.timerCount));
            return buf.array();
        }

        public String toString() {
            return String.format("namId=%d", new Object[]{Short.valueOf(this.namId)}) + ", timerCount=" + this.timerCount;
        }
    }

    public static class TrueImsi extends MinImsi {
        public static final int IMSI_ADDR_NUM_SIZE = 1;
        private short imsiAddrNum;

        public TrueImsi() {
            this.imsiAddrNum = (short) 0;
        }

        public TrueImsi(String mcc, String imsi11_12, String imsiS1, String imsiS2, short imsiAddrNum) throws InvalidParameterException, InvalidParameterException {
            super(mcc, imsi11_12, imsiS1, imsiS2);
            setImsiAddrNum(imsiAddrNum);
        }

        public TrueImsi(byte[] bArray) throws InvalidParameterException {
            super(bArray);
            int sSize = super.getSize();
            if (bArray.length < sSize + 1) {
                throw new InvalidParameterException();
            }
            ByteBuffer buf = QmiBase.createByteBuffer(bArray);
            for (int i = 0; i < sSize; i++) {
                buf.get();
            }
            this.imsiAddrNum = PrimitiveParser.toUnsigned(buf.get());
        }

        public short getImsiAddrNum() {
            return this.imsiAddrNum;
        }

        public void setImsiAddrNum(short imsiAddrNum) throws InvalidParameterException {
            try {
                PrimitiveParser.checkByte(imsiAddrNum);
                this.imsiAddrNum = imsiAddrNum;
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }

        public int getSize() {
            return super.getSize() + 1;
        }

        public byte[] toByteArray() {
            int i;
            ByteBuffer buf = QmiBase.createByteBuffer(getSize());
            for (i = 0; i < 3; i++) {
                buf.put((byte) this.mcc.charAt(i));
            }
            for (i = 0; i < 2; i++) {
                buf.put((byte) this.imsi11_12.charAt(i));
            }
            for (i = 0; i < 7; i++) {
                buf.put((byte) this.imsiS1.charAt(i));
            }
            for (i = 0; i < 3; i++) {
                buf.put((byte) this.imsiS2.charAt(i));
            }
            buf.put(PrimitiveParser.parseByte(this.imsiAddrNum));
            return buf.array();
        }

        public String toString() {
            return super.toString() + String.format(", imsiAddrNum=%d", new Object[]{Short.valueOf(this.imsiAddrNum)});
        }
    }

    public static class VoiceConfig extends BaseQmiStructType {
        public static final short AIR_TIMER_TYPE = (short) 17;
        public static final short AMR_STATUS_TYPE = (short) 21;
        public static final short AUTO_ANSWER_STATUS_TYPE = (short) 16;
        public static final short CURRENT_TTY_MODE_TYPE = (short) 19;
        public static final short PREFERRED_VOICE_SO_TYPE = (short) 20;
        public static final short ROAM_TIMER_TYPE = (short) 18;
        public static final short VOICE_PRIVACY_TYPE = (short) 22;
        private TimerCount airTimerCount;
        private AmrStatus amrStatus;
        private AutoAnswer autoAnswerStatus;
        private QmiByte currentTtyMode;
        private boolean inSetMode = true;
        private PreferredVoiceSo preferredVoiceSo;
        private TimerCount roamTimerCount;
        private QmiByte voicePrivacy;

        public VoiceConfig(AutoAnswer autoAnswerStatus, TimerCount airTimerCount, TimerCount roamTimerCount, short currentTtyMode, PreferredVoiceSo preferredVoiceSo) {
            this.autoAnswerStatus = autoAnswerStatus;
            this.airTimerCount = airTimerCount;
            this.roamTimerCount = roamTimerCount;
            this.currentTtyMode = new QmiByte(currentTtyMode);
            this.preferredVoiceSo = preferredVoiceSo;
        }

        public VoiceConfig(AutoAnswer autoAnswerStatus, TimerCount airTimerCount, TimerCount roamTimerCount, short currentTtyMode, PreferredVoiceSo preferredVoiceSo, AmrStatus amrStatus, short voicePrivacy) {
            this(autoAnswerStatus, airTimerCount, roamTimerCount, currentTtyMode, preferredVoiceSo);
            this.amrStatus = amrStatus;
            this.voicePrivacy = new QmiByte(voicePrivacy);
        }

        public VoiceConfig(byte[] qmiMsg) {
            ByteBuffer buf = ByteBuffer.wrap(qmiMsg);
            buf.order(BaseQmiItemType.QMI_BYTE_ORDER);
            int size = qmiMsg.length;
            while (size > 0) {
                short type = PrimitiveParser.toUnsigned(buf.get());
                int length = PrimitiveParser.toUnsigned(buf.getShort());
                byte[] data = BaseQmiStructType.parseData(buf, length);
                size -= length + 3;
                if (this.inSetMode) {
                    switch (type) {
                        case (short) 16:
                            this.autoAnswerStatus = new AutoAnswer(data);
                            break;
                        case (short) 17:
                            this.airTimerCount = new TimerCount(data);
                            break;
                        case QcNvItemIds.NV_ANALOG_HOME_SID_I /*18*/:
                            this.roamTimerCount = new TimerCount(data);
                            break;
                        case (short) 19:
                            this.currentTtyMode = new QmiByte(data);
                            break;
                        case (short) 20:
                            this.preferredVoiceSo = new PreferredVoiceSo(data);
                            break;
                        case QcNvItemIds.NV_SCDMACH_I /*21*/:
                            this.amrStatus = new AmrStatus(data);
                            this.inSetMode = false;
                            break;
                        case (short) 22:
                            this.voicePrivacy = new QmiByte(data);
                            this.inSetMode = false;
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        public AutoAnswer getAutoAnswerStatus() {
            return this.autoAnswerStatus;
        }

        public void setAutoAnswerStatus(AutoAnswer autoAnswerStatus) {
            this.inSetMode = true;
            this.autoAnswerStatus = autoAnswerStatus;
        }

        public TimerCount getAirTimerCount() {
            return this.airTimerCount;
        }

        public void setAirTimerCount(TimerCount airTimerCount) {
            this.inSetMode = true;
            this.airTimerCount = airTimerCount;
        }

        public TimerCount getRoamTimerCount() {
            return this.roamTimerCount;
        }

        public void setRoamTimerCount(TimerCount roamTimerCount) {
            this.inSetMode = true;
            this.roamTimerCount = roamTimerCount;
        }

        public short getCurrentTtyMode() {
            return this.currentTtyMode.toShort();
        }

        public void setCurrentTtyMode(short currentTtyMode) {
            this.inSetMode = true;
            this.currentTtyMode = new QmiByte(currentTtyMode);
        }

        public PreferredVoiceSo getPreferredVoiceSo() {
            return this.preferredVoiceSo;
        }

        public void setPreferredVoiceSo(PreferredVoiceSo preferredVoiceSo) {
            this.inSetMode = true;
            this.preferredVoiceSo = preferredVoiceSo;
        }

        public AmrStatus getAmrStatus() {
            if (this.inSetMode) {
                return new AmrStatus();
            }
            return this.amrStatus;
        }

        public short getVoicePrivacy() {
            if (this.inSetMode) {
                return (short) 0;
            }
            return this.voicePrivacy.toShort();
        }

        public BaseQmiItemType[] getItems() {
            return getItems(this.inSetMode);
        }

        public BaseQmiItemType[] getItems(boolean inSetMode) {
            if (inSetMode) {
                return new BaseQmiItemType[]{this.autoAnswerStatus, this.airTimerCount, this.roamTimerCount, this.currentTtyMode, this.preferredVoiceSo};
            }
            return new BaseQmiItemType[]{this.autoAnswerStatus, this.airTimerCount, this.roamTimerCount, this.currentTtyMode, this.preferredVoiceSo, this.amrStatus, this.voicePrivacy};
        }

        public short[] getTypes() {
            return getTypes(this.inSetMode);
        }

        public static short[] getTypes(boolean inSetMode) {
            return inSetMode ? new short[]{(short) 16, (short) 17, (short) 18, (short) 19, (short) 20} : new short[]{(short) 16, (short) 17, (short) 18, (short) 19, (short) 20, (short) 21, (short) 22};
        }

        public static BaseQmiItemType[] generateRequest() {
            return new BaseQmiItemType[]{new QmiByte((short) 1), new QmiByte((short) 1), new QmiByte((short) 1), new QmiByte((short) 1), new QmiByte((short) 1), new QmiByte((short) 1), new QmiByte((short) 1)};
        }
    }
}
