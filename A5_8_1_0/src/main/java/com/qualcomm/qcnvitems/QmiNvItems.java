package com.qualcomm.qcnvitems;

import android.util.Log;
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
import com.qualcomm.qcrilhook.BaseQmiTypes.BaseQmiItemType;
import com.qualcomm.qcrilhook.IQcRilHook;
import com.qualcomm.qcrilhook.QmiOemHook;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiByte;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiLong;
import java.io.IOException;
import java.security.InvalidParameterException;

public class QmiNvItems implements IQcNvItems {
    private static String LOG_TAG = "SERVICE_PROG";
    private static final boolean enableVLog = true;
    private static int modemId = 0;
    private QmiOemHook mQmiOemHook = QmiOemHook.getInstance(null);

    public QmiNvItems() {
        vLog("Service Programming instance created.");
    }

    public void dispose() {
        this.mQmiOemHook.dispose();
        this.mQmiOemHook = null;
    }

    public static int getModemId() {
        return modemId;
    }

    public static void setModemId(int id) {
        modemId = id;
    }

    private static void vLog(String logString) {
        Log.v(LOG_TAG, logString);
    }

    public void updateAkey(String akey) throws IOException, InvalidParameterException {
        vLog("updateAkey()");
        this.mQmiOemHook.sendQmiMessage((int) IQcRilHook.QCRILHOOK_NAS_UPDATE_AKEY, (short) 1, new QmiLong(akey));
    }

    public Threegpp2Info get3gpp2SubscriptionInfo(short namId) throws IOException, InvalidParameterException {
        vLog("get3gpp2SubscriptionInfo()");
        Threegpp2Info result = new Threegpp2Info(this.mQmiOemHook.sendQmiMessage((int) IQcRilHook.QCRILHOOK_NAS_GET_3GPP2_SUBSCRIPTION_INFO, (short) 1, new QmiByte(namId)));
        vLog(result.toString());
        return result;
    }

    public Threegpp2Info get3gpp2SubscriptionInfo() throws IOException {
        return get3gpp2SubscriptionInfo((short) 0);
    }

    public void set3gpp2SubscriptionInfo(Threegpp2Info threegpp2Info, short namId) throws IOException, InvalidParameterException {
        vLog("set3gpp2SubscriptionInfo()");
        if (threegpp2Info == null) {
            throw new InvalidParameterException();
        }
        short[] s = threegpp2Info.getTypes();
        BaseQmiItemType[] t = threegpp2Info.getItems();
        short[] types = new short[(s.length + 1)];
        types[0] = (short) 1;
        System.arraycopy(s, 0, types, 1, s.length);
        BaseQmiItemType[] tlvs = new BaseQmiItemType[(t.length + 1)];
        tlvs[0] = new QmiByte(namId);
        System.arraycopy(t, 0, tlvs, 1, t.length);
        this.mQmiOemHook.sendQmiMessage((int) IQcRilHook.QCRILHOOK_NAS_SET_3GPP2_SUBSCRIPTION_INFO, types, tlvs);
    }

    public void set3gpp2SubscriptionInfo(Threegpp2Info threegpp2Info) throws IOException, InvalidParameterException {
        set3gpp2SubscriptionInfo(threegpp2Info, (short) 0);
    }

    public String getNamName(short namId) throws InvalidParameterException, IOException {
        vLog("getNamName()");
        return get3gpp2SubscriptionInfo(namId).getNamName();
    }

    public String getNamName() throws IOException {
        return getNamName((short) 0);
    }

    public int getAnalogHomeSid() throws IOException {
        return 0;
    }

    public void setAnalogHomeSid(int sid) throws IOException, InvalidParameterException {
    }

    public String getDirectoryNumber(short namId) throws IOException {
        vLog("getDirectoryNumber()");
        return get3gpp2SubscriptionInfo(namId).getDirNum();
    }

    public String getDirectoryNumber() throws IOException {
        return getDirectoryNumber((short) 0);
    }

    public void setDirectoryNumber(String directoryNumber, short namId) throws IOException, InvalidParameterException {
        vLog("setDirectoryNumber()");
        if (directoryNumber == null) {
            throw new InvalidParameterException();
        }
        Threegpp2Info info = get3gpp2SubscriptionInfo(namId);
        info.setDirNum(directoryNumber);
        set3gpp2SubscriptionInfo(info, namId);
    }

    public void setDirectoryNumber(String directoryNumber) throws IOException, InvalidParameterException {
        setDirectoryNumber(directoryNumber, (short) 0);
    }

    public SidNid getHomeSidNid(short namId) throws IOException {
        vLog("getSidNid()");
        return get3gpp2SubscriptionInfo(namId).getSidNid();
    }

    public SidNid getHomeSidNid() throws IOException {
        return getHomeSidNid((short) 0);
    }

    public void setHomeSidNid(SidNid sidNid, short namId) throws IOException, InvalidParameterException {
        vLog("setSidNid()");
        if (sidNid == null) {
            throw new InvalidParameterException();
        }
        Threegpp2Info info = get3gpp2SubscriptionInfo(namId);
        info.setSidNid(sidNid);
        set3gpp2SubscriptionInfo(info, namId);
    }

    public void setHomeSidNid(SidNid sidNid) throws IOException, InvalidParameterException {
        setHomeSidNid(sidNid, (short) 0);
    }

    public MinImsi getMinImsi(short namId) throws IOException {
        vLog("getMinImsi()");
        return get3gpp2SubscriptionInfo(namId).getMinImsi();
    }

    public MinImsi getMinImsi() throws IOException {
        return getMinImsi((short) 0);
    }

    public void setMinImsi(MinImsi minImsi, short namId) throws IOException, InvalidParameterException {
        vLog("setMinImsi()");
        if (minImsi == null) {
            throw new InvalidParameterException();
        }
        Threegpp2Info info = get3gpp2SubscriptionInfo(namId);
        info.setMinImsi(minImsi);
        set3gpp2SubscriptionInfo(info, namId);
    }

    public void setMinImsi(MinImsi minImsi) throws IOException, InvalidParameterException {
        setMinImsi(minImsi, (short) 0);
    }

    public String getMinImsiNumber(short namId) throws IOException {
        vLog("getMinImsiNumber()");
        return get3gpp2SubscriptionInfo(namId).getMinImsi().getImsiNumber();
    }

    public String getMinImsiNumber() throws IOException {
        return getMinImsiNumber((short) 0);
    }

    public void setMinImsiNumber(String phNumber, short namId) throws IOException, InvalidParameterException {
        vLog("setMinImsiNumber()");
        if (phNumber == null) {
            throw new InvalidParameterException();
        }
        Threegpp2Info info = get3gpp2SubscriptionInfo(namId);
        MinImsi minImsi = info.getMinImsi();
        minImsi.setImsiNumber(phNumber);
        info.setMinImsi(minImsi);
        set3gpp2SubscriptionInfo(info);
    }

    public void setMinImsiNumber(String phNumber) throws IOException, InvalidParameterException {
        setMinImsiNumber(phNumber, (short) 0);
    }

    public String getImsiMcc(short namId) throws IOException {
        vLog("getImsiMcc()");
        return get3gpp2SubscriptionInfo(namId).getMinImsi().getMcc();
    }

    public String getImsiMcc() throws IOException {
        return getImsiMcc((short) 0);
    }

    public void setImsiMcc(String imsiMcc, short namId) throws IOException, InvalidParameterException {
        vLog("setImsiMcc()");
        if (imsiMcc == null) {
            throw new InvalidParameterException();
        }
        Threegpp2Info info = get3gpp2SubscriptionInfo(namId);
        MinImsi minImsi = info.getMinImsi();
        minImsi.setMcc(imsiMcc);
        info.setMinImsi(minImsi);
        set3gpp2SubscriptionInfo(info, namId);
    }

    public void setImsiMcc(String imsiMcc) throws IOException, InvalidParameterException {
        setImsiMcc(imsiMcc, (short) 0);
    }

    public String getImsi11_12(short namId) throws IOException {
        vLog("getImsi11_12()");
        return get3gpp2SubscriptionInfo(namId).getMinImsi().getImsi11_12();
    }

    public String getImsi11_12() throws IOException {
        return getImsi11_12((short) 0);
    }

    public void setImsi11_12(String imsi11_12, short namId) throws IOException, InvalidParameterException {
        vLog("setImsi11_12()");
        if (imsi11_12 == null) {
            throw new InvalidParameterException();
        }
        Threegpp2Info info = get3gpp2SubscriptionInfo(namId);
        MinImsi minImsi = info.getMinImsi();
        minImsi.setImsi11_12(imsi11_12);
        info.setMinImsi(minImsi);
        set3gpp2SubscriptionInfo(info, namId);
    }

    public void setImsi11_12(String imsi11_12) throws IOException, InvalidParameterException {
        setImsi11_12(imsi11_12, (short) 0);
    }

    public String[] getImsiMin1(short namId) throws IOException {
        vLog("getImsiMin1()");
        int min1_0 = MinImsi.phStringToMin1(getDirectoryNumber(namId));
        int min1_1 = MinImsi.phStringToMin1(get3gpp2SubscriptionInfo(namId).getMinImsi().getImsiNumber());
        return new String[]{PrimitiveParser.toUnsignedString(min1_0), PrimitiveParser.toUnsignedString(min1_1)};
    }

    public String[] getImsiMin1() throws IOException {
        return getImsiMin1((short) 0);
    }

    public void setImsiMin1(String[] min1, short namId) throws IOException, InvalidParameterException {
        vLog("setImsiMin1()");
        if (min1 == null) {
            throw new InvalidParameterException();
        }
        try {
            int min1_0 = PrimitiveParser.parseUnsignedInt(min1[0]);
            int min1_1 = PrimitiveParser.parseUnsignedInt(min1[1]);
            String[] min2 = getImsiMin2(namId);
            short min2_0 = PrimitiveParser.parseUnsignedShort(min2[0]);
            short min2_1 = PrimitiveParser.parseUnsignedShort(min2[1]);
            setDirectoryNumber(MinImsi.minToPhString(min1_0, min2_0), namId);
            setMinImsiNumber(MinImsi.minToPhString(min1_1, min2_1), namId);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setImsiMin1(String[] min1) throws IOException, InvalidParameterException {
        setImsiMin1(min1, (short) 0);
    }

    public String[] getImsiMin2(short namId) throws IOException {
        vLog("getImsiMin2()");
        int min2_0 = MinImsi.phStringToMin2(getDirectoryNumber(namId));
        int min2_1 = MinImsi.phStringToMin2(get3gpp2SubscriptionInfo(namId).getMinImsi().getImsiNumber());
        return new String[]{PrimitiveParser.toUnsignedString(min2_0), PrimitiveParser.toUnsignedString(min2_1)};
    }

    public String[] getImsiMin2() throws IOException {
        return getImsiMin2((short) 0);
    }

    public void setImsiMin2(String[] min2, short namId) throws IOException, InvalidParameterException {
        vLog("setImsiMin2()");
        if (min2 == null) {
            throw new InvalidParameterException();
        }
        try {
            short min2_0 = PrimitiveParser.parseUnsignedShort(min2[0]);
            short min2_1 = PrimitiveParser.parseUnsignedShort(min2[1]);
            String[] min1 = getImsiMin1(namId);
            int min1_0 = PrimitiveParser.parseUnsignedInt(min1[0]);
            int min1_1 = PrimitiveParser.parseUnsignedInt(min1[1]);
            setDirectoryNumber(MinImsi.minToPhString(min1_0, min2_0), namId);
            setMinImsiNumber(MinImsi.minToPhString(min1_1, min2_1), namId);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e.toString());
        }
    }

    public void setImsiMin2(String[] min2) throws IOException, InvalidParameterException {
        setImsiMin2(min2, (short) 0);
    }

    public TrueImsi getTrueImsi(short namId) throws IOException {
        vLog("getTrueImsi()");
        return get3gpp2SubscriptionInfo(namId).getTrueImsi();
    }

    public TrueImsi getTrueImsi() throws IOException {
        return getTrueImsi((short) 0);
    }

    public void setTrueImsi(TrueImsi trueImsi, short namId) throws IOException, InvalidParameterException {
        vLog("setTrueImsi()");
        if (trueImsi == null) {
            throw new InvalidParameterException();
        }
        Threegpp2Info info = get3gpp2SubscriptionInfo(namId);
        info.setTrueImsi(trueImsi);
        set3gpp2SubscriptionInfo(info, namId);
    }

    public void setTrueImsi(TrueImsi trueImsi) throws IOException, InvalidParameterException {
        setTrueImsi(trueImsi, (short) 0);
    }

    public String getTrueImsiNumber(short namId) throws IOException {
        vLog("getTrueImsiNumber()");
        return get3gpp2SubscriptionInfo(namId).getTrueImsi().getImsiNumber();
    }

    public String getTrueImsiNumber() throws IOException {
        return getTrueImsiNumber((short) 0);
    }

    public void setTrueImsiNumber(String phNumber, short namId) throws IOException, InvalidParameterException {
        vLog("setTrueImsiNumber()");
        if (phNumber == null) {
            throw new InvalidParameterException();
        }
        Threegpp2Info info = get3gpp2SubscriptionInfo(namId);
        MinImsi trueImsi = info.getTrueImsi();
        trueImsi.setImsiNumber(phNumber);
        info.setMinImsi(trueImsi);
        set3gpp2SubscriptionInfo(info);
    }

    public void setTrueImsiNumber(String phNumber) throws IOException, InvalidParameterException {
        setTrueImsiNumber(phNumber, (short) 0);
    }

    public String getTrueImsiMcc(short namId) throws IOException {
        vLog("getTrueImsiMcc()");
        return get3gpp2SubscriptionInfo(namId).getTrueImsi().getMcc();
    }

    public String getTrueImsiMcc() throws IOException {
        return getTrueImsiMcc((short) 0);
    }

    public void setTrueImsiMcc(String imsiTMcc, short namId) throws IOException, InvalidParameterException {
        vLog("setTrueImsiMcc()");
        if (imsiTMcc == null) {
            throw new InvalidParameterException();
        }
        Threegpp2Info info = get3gpp2SubscriptionInfo(namId);
        TrueImsi trueImsi = info.getTrueImsi();
        trueImsi.setMcc(imsiTMcc);
        info.setTrueImsi(trueImsi);
        set3gpp2SubscriptionInfo(info, namId);
    }

    public void setTrueImsiMcc(String imsiTMcc) throws IOException, InvalidParameterException {
        setTrueImsiMcc(imsiTMcc, (short) 0);
    }

    public String getTrueImsi11_12(short namId) throws IOException {
        vLog("getTrueImsi11_12()");
        return get3gpp2SubscriptionInfo(namId).getTrueImsi().getImsi11_12();
    }

    public String getTrueImsi11_12() throws IOException {
        return getTrueImsi11_12((short) 0);
    }

    public void setTrueImsi11_12(String imsiT11_12, short namId) throws IOException, InvalidParameterException {
        vLog("setTrueImsi11_12()");
        if (imsiT11_12 == null) {
            throw new InvalidParameterException();
        }
        Threegpp2Info info = get3gpp2SubscriptionInfo(namId);
        TrueImsi trueImsi = info.getTrueImsi();
        trueImsi.setImsi11_12(imsiT11_12);
        info.setTrueImsi(trueImsi);
        set3gpp2SubscriptionInfo(info, namId);
    }

    public void setTrueImsi11_12(String imsiT11_12) throws IOException, InvalidParameterException {
        setTrueImsi11_12(imsiT11_12, (short) 0);
    }

    public short getTrueImsiAddrNum(short namId) throws IOException {
        vLog("getTrueImsiAddrNum()");
        return get3gpp2SubscriptionInfo(namId).getTrueImsi().getImsiAddrNum();
    }

    public short getTrueImsiAddrNum() throws IOException {
        return getTrueImsiAddrNum((short) 0);
    }

    public void setTrueImsiAddrNum(short imsiTAddrNum, short namId) throws IOException, InvalidParameterException {
        vLog("setTrueImsiAddrNum()");
        Threegpp2Info info = get3gpp2SubscriptionInfo(namId);
        TrueImsi trueImsi = info.getTrueImsi();
        trueImsi.setImsiAddrNum(imsiTAddrNum);
        info.setTrueImsi(trueImsi);
        set3gpp2SubscriptionInfo(info, namId);
    }

    public void setTrueImsiAddrNum(short imsiTAddrNum) throws IOException, InvalidParameterException {
        setTrueImsiAddrNum(imsiTAddrNum, (short) 0);
    }

    public CdmaChannels getCdmaChannels(short namId) throws IOException {
        vLog("getCdmaChannels()");
        return get3gpp2SubscriptionInfo(namId).getCdmaChannels();
    }

    public CdmaChannels getCdmaChannels() throws IOException {
        return getCdmaChannels((short) 0);
    }

    public void setCdmaChannels(CdmaChannels cdmaChannels, short namId) throws IOException, InvalidParameterException {
        vLog("setCdmaChannels()");
        if (cdmaChannels == null) {
            throw new InvalidParameterException();
        }
        Threegpp2Info info = get3gpp2SubscriptionInfo(namId);
        info.setCdmaChannels(cdmaChannels);
        set3gpp2SubscriptionInfo(info, namId);
    }

    public void setCdmaChannels(CdmaChannels cdmaChannels) throws IOException, InvalidParameterException {
        setCdmaChannels(cdmaChannels, (short) 0);
    }

    public int[] getPrimaryCdmaChannels(short namId) throws IOException {
        vLog("getPrimaryCdmaChannels()");
        CdmaChannels cdmaChannels = get3gpp2SubscriptionInfo(namId).getCdmaChannels();
        return new int[]{cdmaChannels.getPrimaryChannelA(), cdmaChannels.getPrimaryChannelB()};
    }

    public int[] getPrimaryCdmaChannels() throws IOException {
        return getPrimaryCdmaChannels((short) 0);
    }

    public void setPrimaryCdmaChannels(int[] primaryChannels, short namId) throws IOException, InvalidParameterException {
        vLog("setPrimaryCdmaChannels()");
        if (primaryChannels == null || primaryChannels.length != 2) {
            throw new InvalidParameterException();
        }
        Threegpp2Info info = get3gpp2SubscriptionInfo(namId);
        CdmaChannels cdmaChannels = info.getCdmaChannels();
        cdmaChannels.setPrimaryChannelA(primaryChannels[0]);
        cdmaChannels.setPrimaryChannelB(primaryChannels[1]);
        info.setCdmaChannels(cdmaChannels);
        set3gpp2SubscriptionInfo(info, namId);
    }

    public void setPrimaryCdmaChannels(int[] primaryChannels) throws IOException, InvalidParameterException {
        setPrimaryCdmaChannels(primaryChannels, (short) 0);
    }

    public int[] getSecondaryCdmaChannels(short namId) throws IOException {
        vLog("getSecondaryCdmaChannels()");
        CdmaChannels cdmaChannels = get3gpp2SubscriptionInfo(namId).getCdmaChannels();
        return new int[]{cdmaChannels.getSecondaryChannelA(), cdmaChannels.getSecondaryChannelB()};
    }

    public int[] getSecondaryCdmaChannels() throws IOException {
        return getSecondaryCdmaChannels((short) 0);
    }

    public void setSecondaryCdmaChannels(int[] secondaryChannels, short namId) throws IOException, InvalidParameterException {
        vLog("setSecondaryCdmaChannels()");
        if (secondaryChannels == null || secondaryChannels.length != 2) {
            throw new InvalidParameterException();
        }
        Threegpp2Info info = get3gpp2SubscriptionInfo(namId);
        CdmaChannels cdmaChannels = info.getCdmaChannels();
        cdmaChannels.setPrimaryChannelA(secondaryChannels[0]);
        cdmaChannels.setPrimaryChannelB(secondaryChannels[1]);
        info.setCdmaChannels(cdmaChannels);
        set3gpp2SubscriptionInfo(info, namId);
    }

    public void setSecondaryCdmaChannels(int[] secondaryChannels) throws IOException, InvalidParameterException {
        setSecondaryCdmaChannels(secondaryChannels, (short) 0);
    }

    public short getMobCaiRev() throws IOException {
        vLog("getMobCaiRev()");
        QmiByte result = new QmiByte(this.mQmiOemHook.sendQmiMessage(IQcRilHook.QCRILHOOK_NAS_GET_MOB_CAI_REV));
        vLog(result.toString());
        return result.toShort();
    }

    public void setMobCaiRev(short mobCaiRev) throws IOException, InvalidParameterException {
        vLog("setMobCaiRev()");
        this.mQmiOemHook.sendQmiMessage((int) IQcRilHook.QCRILHOOK_NAS_SET_MOB_CAI_REV, (short) 1, new QmiByte(mobCaiRev));
    }

    public short getRtreConfig() throws IOException {
        vLog("getRtreConfig()");
        QmiByte result = new QmiByte(this.mQmiOemHook.sendQmiMessage(IQcRilHook.QCRILHOOK_NAS_GET_RTRE_CONFIG));
        vLog(result.toString());
        return result.toShort();
    }

    public void setRtreConfig(short rtreCfg) throws IOException, InvalidParameterException {
        vLog("setRtreConfig()");
        if (rtreCfg < (short) 0) {
            throw new InvalidParameterException();
        }
        this.mQmiOemHook.sendQmiMessage((int) IQcRilHook.QCRILHOOK_NAS_SET_RTRE_CONFIG, (short) 16, new QmiByte(rtreCfg));
    }

    public VoiceConfig getVoiceConfig() throws IOException {
        vLog("getVoiceConfig()");
        VoiceConfig result = new VoiceConfig(this.mQmiOemHook.sendQmiMessage((int) IQcRilHook.QCRILHOOK_VOICE_GET_CONFIG, VoiceConfig.getTypes(false), VoiceConfig.generateRequest()));
        vLog(result.toString());
        return result;
    }

    public void setVoiceConfig(VoiceConfig voiceConfig) throws IOException, InvalidParameterException {
        vLog("setVoiceConfig()");
        if (voiceConfig == null) {
            throw new InvalidParameterException();
        }
        this.mQmiOemHook.sendQmiMessage((int) IQcRilHook.QCRILHOOK_VOICE_SET_CONFIG, voiceConfig.getTypes(), voiceConfig.getItems());
    }

    public AutoAnswer getAutoAnswerStatus() throws IOException {
        vLog("getAutoAnswerStatus()");
        return getVoiceConfig().getAutoAnswerStatus();
    }

    public void setAutoAnswerStatus(AutoAnswer autoAnswer) throws IOException, InvalidParameterException {
        vLog("setAutoAnswerStatus()");
        if (autoAnswer == null) {
            throw new InvalidParameterException();
        }
        VoiceConfig vc = getVoiceConfig();
        vc.setAutoAnswerStatus(autoAnswer);
        setVoiceConfig(vc);
    }

    public void disableAutoAnswer() throws IOException {
        vLog("disableAutoAnswer");
        VoiceConfig vc = getVoiceConfig();
        vc.setAutoAnswerStatus(new AutoAnswer(false));
        setVoiceConfig(vc);
    }

    public void enableAutoAnswer() throws IOException {
        vLog("enableAutoAnswer");
        VoiceConfig vc = getVoiceConfig();
        vc.setAutoAnswerStatus(new AutoAnswer((boolean) enableVLog));
        setVoiceConfig(vc);
    }

    public TimerCount getAirTimerCount() throws IOException {
        vLog("getAirTimerCount()");
        return getVoiceConfig().getAirTimerCount();
    }

    public void setAirTimerCount(TimerCount airTimerCount) throws IOException, InvalidParameterException {
        vLog("setAirTimerCount()");
        if (airTimerCount == null) {
            throw new InvalidParameterException();
        }
        VoiceConfig vc = getVoiceConfig();
        vc.setAirTimerCount(airTimerCount);
        setVoiceConfig(vc);
    }

    public TimerCount getRoamTimerCount() throws IOException {
        vLog("getRoamTimerCount()");
        return getVoiceConfig().getRoamTimerCount();
    }

    public void setRoamTimerCount(TimerCount roamTimerCount) throws IOException, InvalidParameterException {
        vLog("setRoamTimerCount()");
        if (roamTimerCount == null) {
            throw new InvalidParameterException();
        }
        VoiceConfig vc = getVoiceConfig();
        vc.setRoamTimerCount(roamTimerCount);
        setVoiceConfig(vc);
    }

    public short getCurrentTtyMode() throws IOException {
        vLog("getCurrentTtyMode()");
        return getVoiceConfig().getCurrentTtyMode();
    }

    public void setCurrentTtyMode(short ttyMode) throws IOException, InvalidParameterException {
        vLog("setCurrentTtyMode()");
        VoiceConfig vc = getVoiceConfig();
        vc.setCurrentTtyMode(ttyMode);
        setVoiceConfig(vc);
    }

    public PreferredVoiceSo getPreferredVoiceSo() throws IOException {
        vLog("getPreferredVoiceSo()");
        return getVoiceConfig().getPreferredVoiceSo();
    }

    public void setPreferredVoiceSo(PreferredVoiceSo preferredVoiceSo) throws IOException, InvalidParameterException {
        vLog("setPreferredVoiceSo()");
        if (preferredVoiceSo == null) {
            throw new InvalidParameterException();
        }
        VoiceConfig vc = getVoiceConfig();
        vc.setPreferredVoiceSo(preferredVoiceSo);
        setVoiceConfig(vc);
    }

    public AmrStatus getAmrStatus() throws IOException {
        vLog("getAmrStatus()");
        return getVoiceConfig().getAmrStatus();
    }

    public short getVoicePrivacyPref() throws IOException {
        vLog("getVoicePrivacyPref()");
        return getVoiceConfig().getVoicePrivacy();
    }

    public String getFtmMode() throws IOException {
        return null;
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
        return null;
    }

    public void setSpcChangeEnabled(Boolean spcChangeEnabled) throws IOException, InvalidParameterException {
    }

    public String getDefaultBaudRate() throws IOException {
        return null;
    }

    public void setDefaultBaudRate(String baudRate) throws IOException, InvalidParameterException {
    }

    public String getEmailGateway() throws IOException {
        return null;
    }

    public void setEmailGateway(String gateway) throws IOException, InvalidParameterException {
    }

    public String[] getEccList() throws IOException {
        return null;
    }

    public void setEccList(String[] eccList) throws IOException, InvalidParameterException {
    }

    public String getSecCode() throws IOException {
        return null;
    }

    public void setSecCode(String securityCode) throws IOException, InvalidParameterException {
    }

    public String getLockCode() throws IOException {
        return null;
    }

    public void setLockCode(String lockCode) throws IOException, InvalidParameterException {
    }

    public String getGpsOnePdeAddress() throws IOException {
        return null;
    }

    public void setGpsOnePdeAddress(String gps1PdeAddress) throws IOException, InvalidParameterException {
    }

    public String getGpsOnePdePort() throws IOException {
        return null;
    }

    public void setGpsOnePdePort(String gps1PdePort) throws IOException, InvalidParameterException {
    }

    public boolean isEnableDiag() throws IOException {
        return false;
    }
}
