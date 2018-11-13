package com.qualcomm.qcnvitems;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.util.Log;
import com.qualcomm.qcnvitems.QcNvItemTypes.EccListType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvAutoAnswerType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvByte;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvByteArray;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvCallCntType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvCarrierVersion;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvCdmaChType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvDirNumberType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvEncryptImeiType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvGpsSnrType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvHomeSidNidType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvImsi1112Type;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvImsiAddrNumType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvImsiMccType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvInteger;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvLightSensorType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvLockCodeType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvMin1Type;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvMin2Type;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvPcbNumberType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvPrefVoiceSoType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvSecCodeType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvShort;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvSidNidPairType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvSidNidType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvSidType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvSrvDomainPrefType;
import com.qualcomm.qcnvitems.QcNvItemTypes.NvWifiPerfType;
import com.qualcomm.qcnvitems.QmiNvItemTypes.AmrStatus;
import com.qualcomm.qcnvitems.QmiNvItemTypes.AutoAnswer;
import com.qualcomm.qcnvitems.QmiNvItemTypes.CdmaChannels;
import com.qualcomm.qcnvitems.QmiNvItemTypes.MinImsi;
import com.qualcomm.qcnvitems.QmiNvItemTypes.PreferredVoiceSo;
import com.qualcomm.qcnvitems.QmiNvItemTypes.SidNid;
import com.qualcomm.qcnvitems.QmiNvItemTypes.Threegpp2Info;
import com.qualcomm.qcnvitems.QmiNvItemTypes.TimerCount;
import com.qualcomm.qcnvitems.QmiNvItemTypes.TrueImsi;
import com.qualcomm.qcnvitems.QmiNvItemTypes.VoiceConfig;
import com.qualcomm.qcrilhook.IQcRilHook;
import com.qualcomm.qcrilhook.PrimitiveParser;
import com.qualcomm.qcrilhook.QcRilHook;
import com.qualcomm.qcrilhook.QcRilHookCallback;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidParameterException;

public class QcNvItems implements IQcNvItems {
    private static final String DEFAULT_SPC = "000000";
    private static final int HEADER_SIZE = 8;
    private static String LOG_TAG = "QC_NV_ITEMS";
    private static final int MAX_SPC_LEN = 6;
    private static final boolean enableVLog = true;
    private boolean mIsQcRilHookReady = false;
    private QcRilHook mQcRilOemHook;
    private QcRilHookCallback mQcrilHookCb = new QcRilHookCallback() {
        public void onQcRilHookReady() {
            QcNvItems.this.mIsQcRilHookReady = QcNvItems.enableVLog;
        }

        public void onQcRilHookDisconnected() {
            QcNvItems.this.mIsQcRilHookReady = false;
        }
    };

    public class SidNidPair {
        String nid;
        String sid;

        public SidNidPair(String sid, String nid) {
            this.sid = sid;
            this.nid = nid;
        }

        public String getSid() {
            return this.sid;
        }

        public void setSid(String sid) {
            this.sid = sid;
        }

        public String getNid() {
            return this.nid;
        }

        public void setNid(String nid) {
            this.nid = nid;
        }
    }

    public QcNvItems() {
        vLog("QcNvItems instance created.");
        this.mQcRilOemHook = new QcRilHook();
    }

    public QcNvItems(Context context) {
        vLog("QcNvItems instance created.");
        this.mQcRilOemHook = new QcRilHook(context, this.mQcrilHookCb);
    }

    public void dispose() {
        this.mQcRilOemHook.dispose();
        this.mQcRilOemHook = null;
        this.mIsQcRilHookReady = false;
    }

    private static void vLog(String logString) {
        Log.v(LOG_TAG, logString);
    }

    private void checkSpc(String spc) throws InvalidParameterException {
        if (spc == null || spc.length() > 6) {
            throw new InvalidParameterException("SPC is null or longer than 6 bytes.");
        }
    }

    private void doNvWrite(BaseQCNvItemType nvItem, int itemId, String spc) throws IOException {
        vLog(nvItem.toDebugString());
        if (this.mIsQcRilHookReady) {
            checkSpc(spc);
            ByteBuffer buf = ByteBuffer.allocate((nvItem.getSize() + 8) + spc.length());
            buf.order(ByteOrder.nativeOrder());
            buf.putInt(itemId);
            buf.putInt(nvItem.getSize());
            buf.put(nvItem.toByteArray());
            buf.put(spc.getBytes());
            AsyncResult result = this.mQcRilOemHook.sendQcRilHookMsg((int) IQcRilHook.QCRILHOOK_NV_WRITE, buf.array());
            if (result.exception != null) {
                Log.w(LOG_TAG, String.format("doNvWrite() Failed : %s", new Object[]{result.exception.toString()}));
                result.exception.printStackTrace();
                throw new IOException();
            }
            return;
        }
        Log.e(LOG_TAG, "QcRilHook isn't ready yet.");
        throw new IOException();
    }

    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            ret.append("0123456789abcdef".charAt((bytes[i] >> 4) & 15));
            ret.append("0123456789abcdef".charAt(bytes[i] & 15));
        }
        return ret.toString();
    }

    private byte[] doNvRead(int itemId) throws IOException {
        if (this.mIsQcRilHookReady) {
            AsyncResult result = this.mQcRilOemHook.sendQcRilHookMsg(524289, itemId);
            if (result == null) {
                throw new IOException();
            } else if (result.exception != null) {
                Log.w(LOG_TAG, String.format("doNvRead() Failed : %s", new Object[]{result.exception.toString()}));
                result.exception.printStackTrace();
                throw new IOException();
            } else {
                vLog("Received: " + bytesToHexString((byte[]) result.result));
                return (byte[]) result.result;
            }
        }
        Log.e(LOG_TAG, "QcRilHook isn't ready yet.");
        throw new IOException();
    }

    public void updateAkey(String akey) throws IOException, InvalidParameterException {
    }

    public Threegpp2Info get3gpp2SubscriptionInfo() throws IOException {
        return new Threegpp2Info("", getDirectoryNumber(), getHomeSidNid(), getMinImsi(), getTrueImsi(), getCdmaChannels());
    }

    public void set3gpp2SubscriptionInfo(Threegpp2Info threegpp2Info, String spc) throws IOException, InvalidParameterException {
        if (threegpp2Info == null) {
            throw new InvalidParameterException();
        }
        setDirectoryNumber(threegpp2Info.getDirNum(), spc);
        setHomeSidNid(threegpp2Info.getSidNid(), spc);
        setMinImsi(threegpp2Info.getMinImsi(), spc);
        setTrueImsi(threegpp2Info.getTrueImsi(), spc);
        setCdmaChannels(threegpp2Info.getCdmaChannels(), spc);
    }

    public void set3gpp2SubscriptionInfo(Threegpp2Info threegpp2Info) throws IOException, InvalidParameterException {
        set3gpp2SubscriptionInfo(threegpp2Info, DEFAULT_SPC);
    }

    public String getNamName() throws IOException {
        return "";
    }

    public int getAnalogHomeSid() throws IOException {
        vLog("getAnalogHomeSid()");
        NvSidType o = new NvSidType(doNvRead(18));
        vLog(o.toDebugString());
        return o.mSid;
    }

    public void setAnalogHomeSid(int analogHomeSid, String spc) throws IOException, InvalidParameterException {
        vLog("setAnalogHomeSid()");
        NvSidType o = new NvSidType();
        try {
            o.mSid = PrimitiveParser.parseShort(analogHomeSid);
            doNvWrite(o, 18, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setAnalogHomeSid(int analogHomeSid) throws IOException, InvalidParameterException {
        setAnalogHomeSid(analogHomeSid, DEFAULT_SPC);
    }

    public String getDirectoryNumber() throws IOException {
        vLog("getDirectoryNumber()");
        return getNvDirNumber();
    }

    public void setDirectoryNumber(String phNumber, String spc) throws IOException, InvalidParameterException {
        vLog("setDirectoryNumber()");
        if (phNumber == null) {
            throw new InvalidParameterException();
        }
        NvMin1Type min1 = new NvMin1Type(doNvRead(32));
        NvMin2Type min2 = new NvMin2Type(doNvRead(33));
        min1.mMin1[0] = MinImsi.phStringToMin1(phNumber);
        min2.mMin2[0] = MinImsi.phStringToMin2(phNumber);
        doNvWrite(min1, 32, spc);
        doNvWrite(min2, 33, spc);
        setNvDirNumber(phNumber, spc);
    }

    public void setDirectoryNumber(String phNumber) throws IOException, InvalidParameterException {
        setDirectoryNumber(phNumber, DEFAULT_SPC);
    }

    private String getNvDirNumber() throws IOException {
        vLog("getNvDirNumber()");
        NvDirNumberType retVal = new NvDirNumberType(doNvRead(QcNvItemIds.NV_DIR_NUMBER_I));
        vLog(retVal.toDebugString());
        return retVal.getDirNumber();
    }

    private void setNvDirNumber(String dirNumber, String spc) throws IOException, InvalidParameterException {
        vLog("setNvDirNumber()");
        if (dirNumber == null) {
            throw new InvalidParameterException();
        }
        NvDirNumberType o = new NvDirNumberType();
        o.mNam = (byte) 0;
        o.setDirNumber(dirNumber);
        doNvWrite(o, QcNvItemIds.NV_DIR_NUMBER_I, spc);
    }

    public SidNid getHomeSidNid() throws IOException {
        vLog("getHomeSidNid()");
        NvHomeSidNidType o = new NvHomeSidNidType(doNvRead(QcNvItemIds.NV_HOME_SID_NID_I));
        vLog(o.toDebugString());
        int[] sid = new int[20];
        int[] nid = new int[20];
        for (int i = 0; i < 20; i++) {
            sid[i] = PrimitiveParser.toUnsigned(o.mPair[i].mSid);
            nid[i] = PrimitiveParser.toUnsigned(o.mPair[i].mNid);
        }
        return new SidNid(sid, nid);
    }

    public void setHomeSidNid(SidNid homeSidNid, String spc) throws IOException, InvalidParameterException {
        vLog("setHomeSidNid()");
        if (homeSidNid == null) {
            throw new InvalidParameterException();
        }
        NvHomeSidNidType o = new NvHomeSidNidType();
        int i = 0;
        while (i < 20) {
            try {
                short sid = PrimitiveParser.parseShort(homeSidNid.getSid(i));
                short nid = PrimitiveParser.parseShort(homeSidNid.getNid(i));
                o.mPair[i] = new NvSidNidPairType();
                o.mPair[i].mSid = sid;
                o.mPair[i].mNid = nid;
                i++;
            } catch (NumberFormatException e) {
                throw new InvalidParameterException(e.toString());
            }
        }
        doNvWrite(o, QcNvItemIds.NV_HOME_SID_NID_I, spc);
    }

    public void setHomeSidNid(SidNid homeSidNid) throws IOException, InvalidParameterException {
        setHomeSidNid(homeSidNid, DEFAULT_SPC);
    }

    public MinImsi getMinImsi() throws IOException {
        vLog("getMinImsi()");
        String mcc = getImsiMcc();
        String imsi11_12 = getImsi11_12();
        String num = getMinImsiNumber();
        return new MinImsi(mcc, imsi11_12, num.substring(0, 7), num.substring(num.length() - 3, num.length()));
    }

    public void setMinImsi(MinImsi minImsi, String spc) throws IOException, InvalidParameterException {
        vLog("setMinImsi()");
        if (minImsi == null) {
            throw new InvalidParameterException();
        }
        setImsiMcc(minImsi.getMcc(), spc);
        setImsi11_12(minImsi.getImsi11_12(), spc);
        setMinImsiNumber(minImsi.getImsiNumber(), spc);
    }

    public void setMinImsi(MinImsi minImsi) throws IOException, InvalidParameterException {
        setMinImsi(minImsi, DEFAULT_SPC);
    }

    public String getMinImsiNumber() throws IOException {
        vLog("getMinImsiNumber()");
        return MinImsi.minToPhString(new NvMin1Type(doNvRead(32)).mMin1[1], new NvMin2Type(doNvRead(33)).mMin2[1]);
    }

    public void setMinImsiNumber(String phNumber, String spc) throws IOException, InvalidParameterException {
        vLog("setMinImsiNumber()");
        if (phNumber == null) {
            throw new InvalidParameterException();
        }
        NvMin1Type min1 = new NvMin1Type(doNvRead(32));
        NvMin2Type min2 = new NvMin2Type(doNvRead(33));
        min1.mMin1[1] = MinImsi.phStringToMin1(phNumber);
        min2.mMin2[1] = MinImsi.phStringToMin2(phNumber);
        doNvWrite(min1, 32, spc);
        doNvWrite(min2, 33, spc);
    }

    public void setMinImsiNumber(String phNumber) throws IOException, InvalidParameterException {
        setMinImsiNumber(phNumber, DEFAULT_SPC);
    }

    public String getImsiMcc() throws IOException {
        vLog("getImsiMcc()");
        NvImsiMccType o = new NvImsiMccType(doNvRead(QcNvItemIds.NV_IMSI_MCC_I));
        vLog(o.toDebugString());
        return PrimitiveParser.toUnsignedString(o.mImsiMcc);
    }

    public void setImsiMcc(String imsiMcc, String spc) throws IOException, InvalidParameterException {
        vLog("setImsiMcc()");
        if (imsiMcc == null || imsiMcc.length() != 3) {
            throw new InvalidParameterException();
        }
        NvImsiMccType o = new NvImsiMccType();
        o.mNam = (byte) 0;
        try {
            o.mImsiMcc = PrimitiveParser.parseUnsignedShort(imsiMcc);
            doNvWrite(o, QcNvItemIds.NV_IMSI_MCC_I, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setImsiMcc(String imsiMcc) throws IOException, InvalidParameterException {
        setImsiMcc(imsiMcc, DEFAULT_SPC);
    }

    public String getImsi11_12() throws IOException {
        vLog("getImsi11_12()");
        NvImsi1112Type o = new NvImsi1112Type(doNvRead(QcNvItemIds.NV_IMSI_11_12_I));
        vLog(o.toDebugString());
        return PrimitiveParser.toUnsignedString(o.mImsi1112);
    }

    public void setImsi11_12(String imsi11_12, String spc) throws IOException, InvalidParameterException {
        vLog("setImsi11_12()");
        if (imsi11_12 == null || imsi11_12.length() != 2) {
            throw new InvalidParameterException();
        }
        NvImsi1112Type o = new NvImsi1112Type();
        o.mNam = (byte) 0;
        try {
            o.mImsi1112 = PrimitiveParser.parseUnsignedByte(imsi11_12);
            doNvWrite(o, QcNvItemIds.NV_IMSI_11_12_I, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setImsi11_12(String imsi11_12) throws IOException, InvalidParameterException {
        setImsi11_12(imsi11_12, DEFAULT_SPC);
    }

    public TrueImsi getTrueImsi() throws IOException {
        vLog("getTrueImsi()");
        String mcc = getTrueImsiMcc();
        String imsi11_12 = getTrueImsi11_12();
        String num = getTrueImsiNumber();
        return new TrueImsi(mcc, imsi11_12, num.substring(0, 7), num.substring(num.length() - 3, num.length()), getTrueImsiAddrNum());
    }

    public void setTrueImsi(TrueImsi trueImsi, String spc) throws IOException, InvalidParameterException {
        vLog("setTrueImsi()");
        if (trueImsi == null) {
            throw new InvalidParameterException();
        }
        setImsiMcc(trueImsi.getMcc(), spc);
        setImsi11_12(trueImsi.getImsi11_12(), spc);
        setMinImsiNumber(trueImsi.getImsiNumber(), spc);
        setTrueImsiAddrNum(trueImsi.getImsiAddrNum(), spc);
    }

    public void setTrueImsi(TrueImsi trueImsi) throws IOException, InvalidParameterException {
        setTrueImsi(trueImsi, DEFAULT_SPC);
    }

    public String getTrueImsiNumber() throws IOException {
        vLog("getTrueImsiNumber()");
        return MinImsi.minToPhString(new NvMin1Type(doNvRead(QcNvItemIds.NV_IMSI_T_S1_I)).mMin1[1], new NvMin2Type(doNvRead(QcNvItemIds.NV_IMSI_T_S2_I)).mMin2[1]);
    }

    public void setTrueImsiNumber(String phNumber, String spc) throws IOException, InvalidParameterException {
        vLog("setTrueImsiNumber()");
        if (phNumber == null) {
            throw new InvalidParameterException();
        }
        NvMin1Type min1 = new NvMin1Type(doNvRead(QcNvItemIds.NV_IMSI_T_S1_I));
        NvMin2Type min2 = new NvMin2Type(doNvRead(QcNvItemIds.NV_IMSI_T_S2_I));
        min1.mMin1[1] = MinImsi.phStringToMin1(phNumber);
        min2.mMin2[1] = MinImsi.phStringToMin2(phNumber);
        doNvWrite(min1, QcNvItemIds.NV_IMSI_T_S1_I, spc);
        doNvWrite(min2, QcNvItemIds.NV_IMSI_T_S2_I, spc);
    }

    public void setTrueImsiNumber(String phNumber) throws IOException, InvalidParameterException {
        setTrueImsiNumber(phNumber, DEFAULT_SPC);
    }

    public String getTrueImsiMcc() throws IOException {
        vLog("getTrueImsiMcc()");
        NvImsiMccType o = new NvImsiMccType(doNvRead(QcNvItemIds.NV_IMSI_T_MCC_I));
        vLog(o.toDebugString());
        return PrimitiveParser.toUnsignedString(o.mImsiMcc);
    }

    public void setTrueImsiMcc(String imsiTMcc, String spc) throws IOException, InvalidParameterException {
        vLog("setTrueImsiMcc()");
        if (imsiTMcc == null || imsiTMcc.length() != 3) {
            throw new InvalidParameterException();
        }
        NvImsiMccType o = new NvImsiMccType();
        o.mNam = (byte) 0;
        try {
            o.mImsiMcc = PrimitiveParser.parseUnsignedShort(imsiTMcc);
            doNvWrite(o, QcNvItemIds.NV_IMSI_T_MCC_I, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setTrueImsiMcc(String imsiTMcc) throws IOException, InvalidParameterException {
        setTrueImsiMcc(imsiTMcc, DEFAULT_SPC);
    }

    public String getTrueImsi11_12() throws IOException {
        vLog("getTrueImsi11_12()");
        NvImsi1112Type o = new NvImsi1112Type(doNvRead(QcNvItemIds.NV_IMSI_T_11_12_I));
        vLog(o.toDebugString());
        return PrimitiveParser.toUnsignedString(o.mImsi1112);
    }

    public void setTrueImsi11_12(String imsiT11_12, String spc) throws IOException, InvalidParameterException {
        vLog("setTrueImsi11_12()");
        if (imsiT11_12 == null || imsiT11_12.length() != 2) {
            throw new InvalidParameterException();
        }
        NvImsi1112Type o = new NvImsi1112Type();
        o.mNam = (byte) 0;
        try {
            o.mImsi1112 = PrimitiveParser.parseUnsignedByte(imsiT11_12);
            doNvWrite(o, QcNvItemIds.NV_IMSI_T_11_12_I, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setTrueImsi11_12(String imsiT11_12) throws IOException, InvalidParameterException {
        setTrueImsi11_12(imsiT11_12, DEFAULT_SPC);
    }

    public short getTrueImsiAddrNum() throws IOException {
        vLog("getTrueImsiAddrNum()");
        NvImsiAddrNumType o = new NvImsiAddrNumType(doNvRead(QcNvItemIds.NV_IMSI_T_ADDR_NUM_I));
        vLog(o.toDebugString());
        return PrimitiveParser.toUnsigned(o.mNum);
    }

    public void setTrueImsiAddrNum(short imsiTAddrNum, String spc) throws IOException, InvalidParameterException {
        vLog("setTrueImsiAddrNum()");
        NvImsiAddrNumType o = new NvImsiAddrNumType();
        o.mNam = (byte) 0;
        try {
            o.mNum = PrimitiveParser.parseByte(imsiTAddrNum);
            doNvWrite(o, QcNvItemIds.NV_IMSI_T_ADDR_NUM_I, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setTrueImsiAddrNum(short imsiTAddrNum) throws IOException, InvalidParameterException {
        setTrueImsiAddrNum(imsiTAddrNum, DEFAULT_SPC);
    }

    public CdmaChannels getCdmaChannels() throws IOException {
        int[] primaryChannels = getPrimaryCdmaChannels();
        int[] secondaryChannels = getSecondaryCdmaChannels();
        return new CdmaChannels(primaryChannels[0], primaryChannels[1], secondaryChannels[0], secondaryChannels[1]);
    }

    public void setCdmaChannels(CdmaChannels cdmaChannels, String spc) throws IOException, InvalidParameterException {
        if (cdmaChannels == null) {
            throw new InvalidParameterException();
        }
        setPrimaryCdmaChannels(new int[]{cdmaChannels.getPrimaryChannelA(), cdmaChannels.getPrimaryChannelB()}, spc);
        setSecondaryCdmaChannels(new int[]{cdmaChannels.getSecondaryChannelA(), cdmaChannels.getSecondaryChannelB()}, spc);
    }

    public void setCdmaChannels(CdmaChannels cdmaChannels) throws IOException, InvalidParameterException {
        setCdmaChannels(cdmaChannels, DEFAULT_SPC);
    }

    public int[] getPrimaryCdmaChannels() throws IOException {
        vLog("getPrimaryCdmaChannels()");
        vLog(new NvCdmaChType(doNvRead(20)).toDebugString());
        return new int[]{PrimitiveParser.toUnsigned(r.mChannelA), PrimitiveParser.toUnsigned(r.mChannelB)};
    }

    public void setPrimaryCdmaChannels(int[] primaryChannels, String spc) throws IOException, InvalidParameterException {
        vLog("setPrimaryCdmaChannels()");
        if (primaryChannels == null || primaryChannels.length != 2) {
            throw new InvalidParameterException();
        }
        NvCdmaChType o = new NvCdmaChType();
        o.mNam = (byte) 0;
        try {
            o.mChannelA = PrimitiveParser.parseShort(primaryChannels[0]);
            o.mChannelB = PrimitiveParser.parseShort(primaryChannels[1]);
            doNvWrite(o, 20, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setPrimaryCdmaChannels(int[] primaryChannels) throws IOException, InvalidParameterException {
        setPrimaryCdmaChannels(primaryChannels, DEFAULT_SPC);
    }

    public int[] getSecondaryCdmaChannels() throws IOException {
        vLog("getSecondaryCdmaChannels()");
        vLog(new NvCdmaChType(doNvRead(21)).toDebugString());
        return new int[]{PrimitiveParser.toUnsigned(r.mChannelA), PrimitiveParser.toUnsigned(r.mChannelB)};
    }

    public void setSecondaryCdmaChannels(int[] secondaryChannels, String spc) throws IOException, InvalidParameterException {
        vLog("setSecondaryCdmaChannels()");
        if (secondaryChannels == null || secondaryChannels.length != 2) {
            throw new InvalidParameterException();
        }
        NvCdmaChType o = new NvCdmaChType();
        o.mNam = (byte) 0;
        try {
            o.mChannelA = PrimitiveParser.parseShort(secondaryChannels[0]);
            o.mChannelB = PrimitiveParser.parseShort(secondaryChannels[1]);
            doNvWrite(o, 21, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setSecondaryCdmaChannels(int[] secondaryChannels) throws IOException, InvalidParameterException {
        setSecondaryCdmaChannels(secondaryChannels, DEFAULT_SPC);
    }

    public short getMobCaiRev() throws IOException {
        vLog("getMobCaiRev()");
        NvByte o = new NvByte(doNvRead(6));
        vLog(o.toDebugString());
        return PrimitiveParser.toUnsigned(o.mVal);
    }

    public void setMobCaiRev(short mobCaiRev, String spc) throws IOException, InvalidParameterException {
        vLog("setMobCaiRev()");
        NvByte o = new NvByte();
        try {
            o.mVal = PrimitiveParser.parseByte(mobCaiRev);
            doNvWrite(o, 6, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setMobCaiRev(short mobCaiRev) throws IOException, InvalidParameterException {
        setMobCaiRev(mobCaiRev, DEFAULT_SPC);
    }

    public short getRtreConfig() throws IOException {
        return (short) 0;
    }

    public void setRtreConfig(short rtreCfg) throws IOException, InvalidParameterException {
    }

    public VoiceConfig getVoiceConfig() throws IOException {
        vLog("getVoiceConfig()");
        return new VoiceConfig(getAutoAnswerStatus(), getAirTimerCount(), getRoamTimerCount(), getCurrentTtyMode(), getPreferredVoiceSo(), getAmrStatus(), getVoicePrivacyPref());
    }

    public void setVoiceConfig(VoiceConfig voiceConfig, String spc) throws IOException, InvalidParameterException {
        vLog("setVoiceConfig()");
        if (voiceConfig == null) {
            throw new InvalidParameterException();
        }
        setAutoAnswerStatus(voiceConfig.getAutoAnswerStatus(), spc);
        setAirTimerCount(voiceConfig.getAirTimerCount(), spc);
        setRoamTimerCount(voiceConfig.getRoamTimerCount(), spc);
        setCurrentTtyMode(voiceConfig.getCurrentTtyMode(), spc);
        setPreferredVoiceSo(voiceConfig.getPreferredVoiceSo(), spc);
    }

    public void setVoiceConfig(VoiceConfig voiceConfig) throws IOException, InvalidParameterException {
        setVoiceConfig(voiceConfig, DEFAULT_SPC);
    }

    public AutoAnswer getAutoAnswerStatus() throws IOException {
        vLog("getAutoAnswerStatus()");
        NvAutoAnswerType mAutoAnswer = getAutoAnswer();
        return new AutoAnswer(mAutoAnswer.enable, (short) mAutoAnswer.rings);
    }

    public void setAutoAnswerStatus(AutoAnswer autoAnswer, String spc) throws IOException, InvalidParameterException {
        vLog("setAutoAnswerStatus()");
        if (autoAnswer == null) {
            throw new InvalidParameterException();
        }
        NvAutoAnswerType mAutoAnswer = new NvAutoAnswerType();
        mAutoAnswer.enable = autoAnswer.isEnabled();
        mAutoAnswer.rings = PrimitiveParser.parseByte(autoAnswer.getRings());
        setAutoAnswer(mAutoAnswer, spc);
    }

    public void setAutoAnswerStatus(AutoAnswer autoAnswer) throws IOException, InvalidParameterException {
        setAutoAnswerStatus(autoAnswer, DEFAULT_SPC);
    }

    public void disableAutoAnswer(String spc) throws IOException {
        vLog("disableAutoAnswer()");
        NvAutoAnswerType mAutoAnswer = new NvAutoAnswerType();
        mAutoAnswer.enable = false;
        mAutoAnswer.rings = (byte) 0;
        setAutoAnswer(mAutoAnswer, spc);
    }

    public void disableAutoAnswer() throws IOException {
        disableAutoAnswer(DEFAULT_SPC);
    }

    public void enableAutoAnswer(short rings, String spc) throws IOException, InvalidParameterException {
        vLog("enableAutoAnswer()");
        NvAutoAnswerType mAutoAnswer = new NvAutoAnswerType();
        mAutoAnswer.enable = enableVLog;
        mAutoAnswer.rings = PrimitiveParser.parseByte(rings);
        setAutoAnswer(mAutoAnswer, spc);
    }

    public void enableAutoAnswer(short rings) throws IOException, InvalidParameterException {
        enableAutoAnswer(rings, DEFAULT_SPC);
    }

    public void enableAutoAnswer(String spc) throws IOException {
        enableAutoAnswer((short) 5);
    }

    public void enableAutoAnswer() throws IOException {
        enableAutoAnswer(DEFAULT_SPC);
    }

    public TimerCount getAirTimerCount() throws IOException {
        vLog("getAirTimerCount()");
        NvCallCntType o = new NvCallCntType(doNvRead(QcNvItemIds.NV_AIR_CNT_I));
        vLog(o.toDebugString());
        return new TimerCount(PrimitiveParser.toUnsigned(o.mCount));
    }

    public void setAirTimerCount(TimerCount timerCnt, String spc) throws IOException, InvalidParameterException {
        vLog("setAirTimerCount()");
        if (timerCnt == null) {
            throw new InvalidParameterException();
        }
        NvCallCntType o = new NvCallCntType();
        try {
            o.mCount = PrimitiveParser.parseInt(timerCnt.getTimerCount());
            doNvWrite(o, QcNvItemIds.NV_AIR_CNT_I, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setAirTimerCount(TimerCount timerCnt) throws IOException, InvalidParameterException {
        setAirTimerCount(timerCnt, DEFAULT_SPC);
    }

    public TimerCount getRoamTimerCount() throws IOException {
        vLog("getRoamTimerCount()");
        NvCallCntType o = new NvCallCntType(doNvRead(QcNvItemIds.NV_ROAM_CNT_I));
        vLog(o.toDebugString());
        return new TimerCount(PrimitiveParser.toUnsigned(o.mCount));
    }

    public void setRoamTimerCount(TimerCount timerCnt, String spc) throws IOException, InvalidParameterException {
        vLog("setRoamCount()");
        if (timerCnt == null) {
            throw new InvalidParameterException();
        }
        NvCallCntType o = new NvCallCntType();
        try {
            o.mCount = PrimitiveParser.parseInt(timerCnt.getTimerCount());
            doNvWrite(o, QcNvItemIds.NV_ROAM_CNT_I, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setRoamTimerCount(TimerCount timerCnt) throws IOException, InvalidParameterException {
        setRoamTimerCount(timerCnt, DEFAULT_SPC);
    }

    public short getCurrentTtyMode() throws IOException {
        return (short) 0;
    }

    public void setCurrentTtyMode(short ttyMode, String spc) throws IOException, InvalidParameterException {
    }

    public void setCurrentTtyMode(short ttyMode) throws IOException, InvalidParameterException {
        setCurrentTtyMode(ttyMode, DEFAULT_SPC);
    }

    public PreferredVoiceSo getPreferredVoiceSo() throws IOException {
        vLog("getPreferredVoiceSo()");
        NvPrefVoiceSoType r = new NvPrefVoiceSoType(doNvRead(QcNvItemIds.NV_PREF_VOICE_SO_I));
        vLog(r.toDebugString());
        return new PreferredVoiceSo(r.mEvrcCapabilityEnabled ? (short) 1 : (short) 0, PrimitiveParser.toUnsigned(r.mHomePageVoiceSo), PrimitiveParser.toUnsigned(r.mHomeOrigVoiceSo), PrimitiveParser.toUnsigned(r.mRoamOrigVoiceSo));
    }

    public void setPreferredVoiceSo(PreferredVoiceSo prefVoiceSo, String spc) throws IOException, InvalidParameterException {
        boolean z = false;
        vLog("setPreferredVoiceSo()");
        if (prefVoiceSo == null) {
            throw new InvalidParameterException();
        }
        NvPrefVoiceSoType o = new NvPrefVoiceSoType();
        if (prefVoiceSo.getEvrcCapability() != (short) 0) {
            z = enableVLog;
        }
        o.mEvrcCapabilityEnabled = z;
        try {
            o.mHomePageVoiceSo = PrimitiveParser.parseShort(prefVoiceSo.getHomePageVoiceSo());
            o.mHomeOrigVoiceSo = PrimitiveParser.parseShort(prefVoiceSo.getHomeOrigVoiceSo());
            o.mRoamOrigVoiceSo = PrimitiveParser.parseShort(prefVoiceSo.getRoamOrigVoiceSo());
            doNvWrite(o, QcNvItemIds.NV_PREF_VOICE_SO_I, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setPreferredVoiceSo(PreferredVoiceSo prefVoiceSo) throws IOException, InvalidParameterException {
        setPreferredVoiceSo(prefVoiceSo, DEFAULT_SPC);
    }

    public AmrStatus getAmrStatus() throws IOException {
        return new AmrStatus();
    }

    public short getVoicePrivacyPref() throws IOException {
        return (short) 0;
    }

    public String getFtmMode() throws IOException {
        NvByte o = new NvByte(doNvRead(QcNvItemIds.NV_FTM_MODE_I));
        vLog(o.toDebugString());
        return String.valueOf(o.mVal);
    }

    public String getSwVersion() throws IOException {
        return null;
    }

    public void updateSpCode() throws IOException {
    }

    public String[] getDeviceSerials() throws IOException {
        return null;
    }

    public Boolean getSpcChangeEnabled() throws IOException {
        boolean z = enableVLog;
        vLog("getSpcChangeEnabled()");
        NvByte o = new NvByte(doNvRead(QcNvItemIds.NV_SPC_CHANGE_ENABLED_I));
        vLog(o.toDebugString());
        if (o.mVal != (byte) 1) {
            z = false;
        }
        return Boolean.valueOf(z);
    }

    public void setSpcChangeEnabled(Boolean spcChangeEnabled, String spc) throws IOException, InvalidParameterException {
        vLog("setSpcChangeEnabled()");
        NvByte o = new NvByte();
        o.mVal = (byte) (spcChangeEnabled.booleanValue() ? 1 : 0);
        doNvWrite(o, QcNvItemIds.NV_SPC_CHANGE_ENABLED_I, spc);
    }

    public void setSpcChangeEnabled(Boolean spcChangeEnabled) throws IOException, InvalidParameterException {
        setSpcChangeEnabled(spcChangeEnabled, DEFAULT_SPC);
    }

    public String getDefaultBaudRate() throws IOException {
        vLog("getDefaultBaudRate()");
        NvShort o = new NvShort(doNvRead(QcNvItemIds.NV_DS_DEFAULT_BAUDRATE_I));
        vLog(o.toDebugString());
        return PrimitiveParser.toUnsignedString(o.mVal);
    }

    public void setDefaultBaudRate(String defaultBaudrate, String spc) throws IOException, InvalidParameterException {
        vLog("setDefaultBaudRate()");
        if (defaultBaudrate == null) {
            throw new InvalidParameterException();
        }
        NvShort o = new NvShort();
        try {
            o.mVal = PrimitiveParser.parseUnsignedShort(defaultBaudrate);
            doNvWrite(o, QcNvItemIds.NV_DS_DEFAULT_BAUDRATE_I, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setDefaultBaudRate(String defaultBaudrate) throws IOException, InvalidParameterException {
        setDefaultBaudRate(defaultBaudrate, DEFAULT_SPC);
    }

    public String getEmailGateway() throws IOException {
        return null;
    }

    public void setEmailGateway(String gateway) throws IOException, InvalidParameterException {
    }

    public String[] getEccList() throws IOException {
        vLog("getEccList()");
        EccListType r = new EccListType(doNvRead(QcNvItemIds.NV_ECC_LIST_I));
        vLog(r.toDebugString());
        return r.getEcclist();
    }

    public void setEccList(String[] eccList, String spc) throws IOException, InvalidParameterException {
        vLog("setEccList()");
        if (eccList == null) {
            throw new InvalidParameterException();
        }
        EccListType o = new EccListType();
        o.setEccList(eccList);
        doNvWrite(o, QcNvItemIds.NV_ECC_LIST_I, spc);
    }

    public void setEccList(String[] eccList) throws IOException, InvalidParameterException {
        setEccList(eccList, DEFAULT_SPC);
    }

    public String getSecCode() throws IOException {
        vLog("getSecCode()");
        NvSecCodeType o = new NvSecCodeType(doNvRead(85));
        vLog(o.toDebugString());
        return o.getSecCode();
    }

    public void setSecCode(String securityCode, String spc) throws IOException, InvalidParameterException {
        vLog("setSecCode()");
        if (securityCode == null) {
            throw new InvalidParameterException();
        }
        NvSecCodeType o = new NvSecCodeType();
        o.setSecCode(securityCode);
        doNvWrite(o, 85, spc);
    }

    public void setSecCode(String securityCode) throws IOException, InvalidParameterException {
        setSecCode(securityCode, DEFAULT_SPC);
    }

    public String getLockCode() throws IOException {
        vLog("getLockCode()");
        NvLockCodeType o = new NvLockCodeType(doNvRead(82));
        vLog(o.toDebugString());
        return o.getLockCode();
    }

    public void setLockCode(String lockCode, String spc) throws IOException, InvalidParameterException {
        vLog("setLockCode()");
        if (lockCode == null) {
            throw new InvalidParameterException();
        }
        NvLockCodeType o = new NvLockCodeType();
        o.setLockCode(lockCode);
        doNvWrite(o, 82, spc);
    }

    public void setLockCode(String lockCode) throws IOException, InvalidParameterException {
        setLockCode(lockCode, DEFAULT_SPC);
    }

    public String getGpsOnePdeAddress() throws IOException {
        vLog("getGpsOnePdeAddress()");
        NvInteger o = new NvInteger(doNvRead(QcNvItemIds.NV_GPS1_PDE_ADDRESS_I));
        vLog(o.toDebugString());
        return intToIpAddress(o.mVal);
    }

    public void setGpsOnePdeAddress(String gpsOnePdeAddress, String spc) throws IOException, InvalidParameterException {
        vLog("setGpsOnePdeAddress()");
        if (gpsOnePdeAddress == null) {
            throw new InvalidParameterException();
        }
        NvInteger o = new NvInteger();
        try {
            o.mVal = ipAddressStringToInt(gpsOnePdeAddress);
            doNvWrite(o, QcNvItemIds.NV_GPS1_PDE_ADDRESS_I, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setGpsOnePdeAddress(String gpsOnePdeAddress) throws IOException, InvalidParameterException {
        setGpsOnePdeAddress(gpsOnePdeAddress, DEFAULT_SPC);
    }

    public String getGpsOnePdePort() throws IOException {
        vLog("getGpsOnePdePort()");
        NvInteger o = new NvInteger(doNvRead(QcNvItemIds.NV_GPS1_PDE_PORT_I));
        vLog(o.toDebugString());
        return PrimitiveParser.toUnsignedString(o.mVal);
    }

    public void setGpsOnePdePort(String gpsOnePdePort, String spc) throws IOException, InvalidParameterException {
        vLog("setGpsOnePdePort()");
        if (gpsOnePdePort == null) {
            throw new InvalidParameterException();
        }
        NvInteger o = new NvInteger();
        try {
            o.mVal = PrimitiveParser.parseUnsignedInt(gpsOnePdePort);
            doNvWrite(o, QcNvItemIds.NV_GPS1_PDE_PORT_I, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setGpsOnePdePort(String gpsOnePdePort) throws IOException, InvalidParameterException {
        setGpsOnePdePort(gpsOnePdePort, DEFAULT_SPC);
    }

    private NvAutoAnswerType getAutoAnswer() throws IOException {
        if (!this.mIsQcRilHookReady) {
            return null;
        }
        NvAutoAnswerType mAutoAnswer = new NvAutoAnswerType();
        AsyncResult result = this.mQcRilOemHook.sendQcRilHookMsg(524289, 74);
        if (result.exception != null || result.result == null || ((byte[]) result.result).length < NvAutoAnswerType.getSize()) {
            Log.w(LOG_TAG, "Unable to read Auto Answer Value from NV Memory");
            throw new IOException();
        }
        byte[] responseData = result.result;
        mAutoAnswer.enable = responseData[0] == (byte) 0 ? false : enableVLog;
        mAutoAnswer.rings = responseData[1];
        return mAutoAnswer;
    }

    private void setAutoAnswer(NvAutoAnswerType mAutoAnswer, String spc) throws IOException {
        if (this.mIsQcRilHookReady) {
            try {
                checkSpc(spc);
                byte[] requestData = new byte[(NvAutoAnswerType.getSize() + 8)];
                ByteBuffer buf = QcRilHook.createBufferWithNativeByteOrder(requestData);
                buf.putInt(74);
                buf.putInt(NvAutoAnswerType.getSize());
                buf.put((byte) (mAutoAnswer.enable ? 1 : 0));
                buf.put(mAutoAnswer.rings);
                buf.put(spc.getBytes());
                if (this.mQcRilOemHook.sendQcRilHookMsg((int) IQcRilHook.QCRILHOOK_NV_WRITE, requestData).exception != null) {
                    Log.w(LOG_TAG, "Unable to Set Auto Answer");
                    throw new IOException();
                }
            } catch (InvalidParameterException e) {
                Log.w(LOG_TAG, e.toString());
            }
        }
    }

    public static int ipAddressStringToInt(String ipAddress) throws InvalidParameterException {
        String[] add = ipAddress.split("\\.");
        if (add.length != 4) {
            throw new InvalidParameterException("Incorrectly formatted IP Address.");
        }
        int ip = 0;
        for (int i = 0; i < 4; i++) {
            int t = Integer.parseInt(add[i]);
            if ((t & -256) != 0) {
                throw new InvalidParameterException("Incorrectly formatted IP Address.");
            }
            ip = (ip << 8) + (t & 255);
            vLog("t=" + (t & 255));
            vLog("ip=" + ip);
        }
        return ip;
    }

    public static String intToIpAddress(int ip) {
        return ((("" + ((ip >> 24) & 255) + ".") + ((ip >> 16) & 255) + ".") + ((ip >> 8) & 255) + ".") + (ip & 255);
    }

    public SidNidPair[][] getSidNid() throws IOException {
        vLog("getSidNid()");
        NvSidNidType o = new NvSidNidType(doNvRead(38));
        vLog(o.toDebugString());
        SidNidPair[][] retVal = (SidNidPair[][]) Array.newInstance(SidNidPair.class, new int[]{2, 1});
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 1; j++) {
                retVal[i][j] = new SidNidPair();
                retVal[i][j].setSid(PrimitiveParser.toUnsignedString(o.mPair[i][j].mSid));
                retVal[i][j].setNid(PrimitiveParser.toUnsignedString(o.mPair[i][j].mNid));
            }
        }
        return retVal;
    }

    public void setSidNid(SidNidPair[][] sn, String spc) throws IOException, InvalidParameterException {
        vLog("setSidNid()");
        if (sn == null) {
            throw new InvalidParameterException();
        }
        NvSidNidType o = new NvSidNidType();
        for (int i = 0; i < 2; i++) {
            int j = 0;
            while (j < 1) {
                try {
                    short sid = PrimitiveParser.parseUnsignedShort(sn[i][j].getSid());
                    short nid = PrimitiveParser.parseUnsignedShort(sn[i][j].getNid());
                    if ((32768 & sid) != 0) {
                        throw new InvalidParameterException("Parameter out of range : 0<=sid<=32767");
                    }
                    o.mPair[i][j] = new NvSidNidPairType();
                    o.mPair[i][j].mSid = sid;
                    o.mPair[i][j].mNid = nid;
                    j++;
                } catch (NumberFormatException e) {
                    throw new InvalidParameterException(e.toString());
                }
            }
        }
        doNvWrite(o, 38, spc);
    }

    public void setSidNid(SidNidPair[][] sn) throws IOException, InvalidParameterException {
        setSidNid(sn, DEFAULT_SPC);
    }

    public String[] getImsiMin1() throws IOException {
        vLog("getImsiMin1()");
        vLog(new NvMin1Type(doNvRead(32)).toDebugString());
        return new String[]{PrimitiveParser.toUnsignedString(o.mMin1[0]), PrimitiveParser.toUnsignedString(o.mMin1[1])};
    }

    public void setImsiMin1(String[] minString, String spc) throws IOException, InvalidParameterException {
        vLog("setImsiMin1()");
        if (minString == null || minString.length != 2) {
            throw new InvalidParameterException();
        }
        NvMin1Type min = new NvMin1Type();
        try {
            min.mNam = (byte) 0;
            min.mMin1[0] = PrimitiveParser.parseUnsignedInt(minString[0]);
            min.mMin1[1] = PrimitiveParser.parseUnsignedInt(minString[1]);
            doNvWrite(min, 32, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setImsiMin1(String[] minString) throws IOException, InvalidParameterException {
        setImsiMin1(minString, DEFAULT_SPC);
    }

    public String[] getImsiMin2() throws IOException {
        vLog("getImsiMin2()");
        vLog(new NvMin2Type(doNvRead(33)).toDebugString());
        return new String[]{PrimitiveParser.toUnsignedString(o.mMin2[0]), PrimitiveParser.toUnsignedString(o.mMin2[1])};
    }

    public void setImsiMin2(String[] minString, String spc) throws IOException, InvalidParameterException {
        vLog("setImsiMin2()");
        if (minString == null || minString.length != 2) {
            throw new InvalidParameterException();
        }
        NvMin2Type min = new NvMin2Type();
        try {
            min.mNam = (byte) 0;
            min.mMin2[0] = PrimitiveParser.parseUnsignedShort(minString[0]);
            min.mMin2[1] = PrimitiveParser.parseUnsignedShort(minString[1]);
            doNvWrite(min, 33, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setImsiMin2(String[] minString) throws IOException, InvalidParameterException {
        setImsiMin2(minString, DEFAULT_SPC);
    }

    public String[] getTrueImsiS1() throws IOException {
        vLog("getTrueImsiS1()");
        vLog(new NvMin1Type(doNvRead(QcNvItemIds.NV_IMSI_T_S1_I)).toDebugString());
        return new String[]{PrimitiveParser.toUnsignedString(o.mMin1[0]), PrimitiveParser.toUnsignedString(o.mMin1[1])};
    }

    public void setTrueImsiS1(String[] imsiTS1, String spc) throws IOException, InvalidParameterException {
        vLog("setTrueImsiS1()");
        if (imsiTS1 == null || imsiTS1.length != 2) {
            throw new InvalidParameterException();
        }
        NvMin1Type min = new NvMin1Type();
        try {
            min.mNam = (byte) 0;
            min.mMin1[0] = PrimitiveParser.parseUnsignedInt(imsiTS1[0]);
            min.mMin1[1] = PrimitiveParser.parseUnsignedInt(imsiTS1[1]);
            doNvWrite(min, QcNvItemIds.NV_IMSI_T_S1_I, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setTrueImsiS1(String[] imsiTS1) throws IOException, InvalidParameterException {
        setTrueImsiS1(imsiTS1, DEFAULT_SPC);
    }

    public String[] getTrueImsiS2() throws IOException {
        vLog("getTrueImsiS2()");
        vLog(new NvMin2Type(doNvRead(QcNvItemIds.NV_IMSI_T_S2_I)).toDebugString());
        return new String[]{PrimitiveParser.toUnsignedString(o.mMin2[0]), PrimitiveParser.toUnsignedString(o.mMin2[1])};
    }

    public void setTrueImsiS2(String[] imsiTS2, String spc) throws IOException, InvalidParameterException {
        vLog("setTrueImsiS2()");
        if (imsiTS2 == null || imsiTS2.length != 2) {
            throw new InvalidParameterException();
        }
        NvMin2Type min = new NvMin2Type();
        try {
            min.mNam = (byte) 0;
            min.mMin2[0] = PrimitiveParser.parseUnsignedShort(imsiTS2[0]);
            min.mMin2[1] = PrimitiveParser.parseUnsignedShort(imsiTS2[1]);
            doNvWrite(min, QcNvItemIds.NV_IMSI_T_S2_I, spc);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setTrueImsiS2(String[] imsiTS2) throws IOException, InvalidParameterException {
        setTrueImsiS2(imsiTS2, DEFAULT_SPC);
    }

    public String getEncryptImei() throws IOException {
        vLog(String.format("getEncryptImei()", new Object[0]));
        NvEncryptImeiType o = new NvEncryptImeiType(doNvRead(QcNvItemIds.NV_ENCRYPT_IMEI_I));
        vLog(o.toDebugString());
        return o.getEncryptImei();
    }

    public String getPcbNumber() throws IOException {
        vLog(String.format("getPcbNumber()", new Object[0]));
        NvPcbNumberType o = new NvPcbNumberType(doNvRead(QcNvItemIds.NV_PCB_NUMBER_I));
        vLog(o.toDebugString());
        return o.getPcbNumber();
    }

    public void setPcbNumber(String pcbNumber) throws IOException, InvalidParameterException {
        vLog(String.format("setPcbNumber()", new Object[0]));
        if (pcbNumber == null || pcbNumber.length() > 32) {
            Log.w(LOG_TAG, "setPcbNumber() Failed : Invalid Parameter");
            throw new InvalidParameterException();
        } else {
            doNvWrite(new NvPcbNumberType(pcbNumber), QcNvItemIds.NV_PCB_NUMBER_I, DEFAULT_SPC);
        }
    }

    public byte[] getGpsSnr() throws IOException {
        vLog(String.format("getGpsSnr()", new Object[0]));
        NvGpsSnrType o = new NvGpsSnrType(doNvRead(QcNvItemIds.NV_GPS_SNR_I));
        vLog(o.toDebugString());
        return o.getGpsSnrByteArray();
    }

    public void setGpsSnr(byte[] gpsSnr) throws IOException, InvalidParameterException {
        vLog(String.format("setGpsSnr()", new Object[0]));
        if (gpsSnr == null || gpsSnr.length > 124) {
            Log.w(LOG_TAG, "setGpsSnr() Failed : Invalid Parameter");
            throw new InvalidParameterException();
        } else {
            doNvWrite(new NvGpsSnrType(gpsSnr), QcNvItemIds.NV_GPS_SNR_I, DEFAULT_SPC);
        }
    }

    public int getWifiPerf() throws IOException {
        vLog(String.format("getWifiPerf()", new Object[0]));
        NvWifiPerfType o = new NvWifiPerfType(doNvRead(QcNvItemIds.NV_WIFI_PERF_I));
        vLog(o.toDebugString());
        return o.getWifiPerfInteger();
    }

    public void setWifiPerf(int wifiPerf) throws IOException, InvalidParameterException {
        vLog(String.format("setWifiPerf()", new Object[0]));
        if (wifiPerf < 0) {
            Log.w(LOG_TAG, "setWifiPerf() Failed : Invalid Parameter");
            throw new InvalidParameterException();
        } else {
            doNvWrite(new NvWifiPerfType(wifiPerf), QcNvItemIds.NV_WIFI_PERF_I, DEFAULT_SPC);
        }
    }

    public int getSrvDomainPref() throws IOException {
        vLog("getSrvDomainPref()");
        NvSrvDomainPrefType o = new NvSrvDomainPrefType(doNvRead(QcNvItemIds.NV_SERVICE_DOMAIN_PREF_I));
        vLog(o.toDebugString());
        return o.getSrvDomainPref();
    }

    public void setSrvDomainPref(int srvDomainPref) throws IOException, InvalidParameterException {
        vLog("setSrvDomainPref()");
        if (srvDomainPref < 0 || srvDomainPref > 2) {
            throw new InvalidParameterException();
        }
        doNvWrite(new NvSrvDomainPrefType(srvDomainPref), QcNvItemIds.NV_SERVICE_DOMAIN_PREF_I, DEFAULT_SPC);
    }

    public int[] getLightSensor() throws IOException {
        vLog("getLightSensor()");
        NvLightSensorType o = new NvLightSensorType(doNvRead(QcNvItemIds.NV_LIGHT_SENSOR_I));
        vLog(o.toDebugString());
        return o.getLightSensor();
    }

    public void setLightSensor(int[] lightSensor) throws IOException, InvalidParameterException {
        vLog("setLightSensor()");
        if (lightSensor == null || lightSensor.length > 3) {
            throw new InvalidParameterException();
        }
        doNvWrite(new NvLightSensorType(lightSensor), QcNvItemIds.NV_LIGHT_SENSOR_I, DEFAULT_SPC);
    }

    public byte getNVBackupFlag() throws IOException {
        vLog("getNVBackupFlag()");
        NvByteArray o = new NvByteArray(doNvRead(QcNvItemIds.NV_BACKUP_I));
        vLog(o.toDebugString());
        return o.getByte(0);
    }

    public void setNVBackupFlag(byte flag) throws IOException, InvalidParameterException {
        vLog("setNVBackupFlag()");
        if (flag == (byte) 0 || flag == (byte) 1) {
            NvByteArray o = new NvByteArray(doNvRead(QcNvItemIds.NV_BACKUP_I));
            o.setByte(0, flag);
            doNvWrite(o, QcNvItemIds.NV_BACKUP_I, DEFAULT_SPC);
            return;
        }
        throw new InvalidParameterException();
    }

    public byte[] getCalibrateInformation() throws IOException {
        vLog("getCalibrateInformation()");
        NvByteArray o = new NvByteArray(doNvRead(QcNvItemIds.NV_CALIBRATATE_INFO_I));
        vLog(o.toDebugString());
        return o.getByteArray();
    }

    public boolean isEnableDiag() throws IOException {
        NvByte o = new NvByte(doNvRead(QcNvItemIds.NV_FACTORY_DATA_2_I));
        vLog(o.toDebugString());
        Log.d("wjp", "isEnableDiag mval:" + o.mVal);
        if (o.mVal == (byte) 1) {
            return enableVLog;
        }
        return false;
    }

    public byte[] getProductLineTestFlagEx() throws IOException {
        vLog("==getProductLineTestFlag()");
        NvByteArray o = new NvByteArray(doNvRead(QcNvItemIds.NV_OPPO_CUSTOM_DATA_I));
        vLog(o.toDebugString());
        return o.getByteArray();
    }

    public void setProductLineTestFlagEx(byte[] value) throws IOException, InvalidParameterException {
        if (value != null && value.length <= 31) {
            vLog("==setProductLineTestFlag()" + value.length);
            doNvWrite(new NvByteArray(value), QcNvItemIds.NV_OPPO_CUSTOM_DATA_I, DEFAULT_SPC);
        }
    }

    public byte[] getProductLineTestFlag() throws IOException {
        vLog("==getProductLineTestFlag()");
        NvByteArray o = new NvByteArray(doNvRead(QcNvItemIds.NV_OPPO_PRODUCT_LINE_TEST_I));
        vLog(o.toDebugString());
        return o.getByteArray();
    }

    public void setProductLineTestFlag(byte[] value) throws IOException, InvalidParameterException {
        if (value != null && value.length <= 124) {
            vLog("==setProductLineTestFlag()" + value.length);
            doNvWrite(new NvByteArray(value), QcNvItemIds.NV_OPPO_PRODUCT_LINE_TEST_I, DEFAULT_SPC);
        }
    }

    public void registerOnServiceConnected(Handler h, int what, Object obj) {
        if (this.mQcRilOemHook != null) {
            vLog(" mQcRilOemHook.registerOnServiceConnected(h, what, obj)");
            QcRilHook qcRilHook = this.mQcRilOemHook;
            QcRilHook.registerOnServiceConnected(h, what, obj);
        }
    }

    public void unregisterOnServiceConnected(Handler h) {
        if (this.mQcRilOemHook != null) {
            QcRilHook qcRilHook = this.mQcRilOemHook;
            QcRilHook.unregisterOnServiceConnected(h);
        }
    }

    public String getCarrierVersion() throws IOException {
        vLog(String.format("getCarrierVersion()", new Object[0]));
        NvCarrierVersion o = new NvCarrierVersion(doNvRead(QcNvItemIds.NV_OPPO_CARRIER_VERSION_I));
        vLog(o.toDebugString());
        return o.getCarrierVersion();
    }

    public void setCarrierVersion(String carreirVersion) throws IOException, InvalidParameterException {
        vLog(String.format("setCarrierVersion()", new Object[0]));
        if (carreirVersion == null || carreirVersion.length() > 32) {
            Log.w(LOG_TAG, "setCarrierVersion() Failed : Invalid Parameter");
            throw new InvalidParameterException();
        } else {
            doNvWrite(new NvCarrierVersion(carreirVersion), QcNvItemIds.NV_OPPO_CARRIER_VERSION_I, DEFAULT_SPC);
        }
    }

    public void setPlmnList(byte[] value) throws IOException {
        vLog(String.format("setPlmnList()", new Object[0]));
        doNvWrite(new NvByteArray(value), QcNvItemIds.NV_OPPO_PLMN_LIST_I, DEFAULT_SPC);
    }
}
