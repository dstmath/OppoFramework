package com.qualcomm.qcnvitems;

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
import java.io.IOException;
import java.security.InvalidParameterException;

public interface IQcNvItems {
    public static final int NV_BACKUP_INDEX = 0;
    public static final int NV_CRRIER_VERSION_SIZE = 124;
    public static final int NV_DIR_NUMB_SIZ = 10;
    public static final int NV_ECC_NUMBER_SIZE = 3;
    public static final int NV_ENCRYPT_IMEI_NUMBER_SIZE = 15;
    public static final int NV_FACTORY_DATA_SIZE = 128;
    public static final int NV_GPS_SNR_SIZE = 124;
    public static final int NV_LIGHT_SENSOR_SIZE = 3;
    public static final int NV_LOCK_CODE_SIZE = 4;
    public static final int NV_MAX_HOME_SID_NID = 20;
    public static final int NV_MAX_MINS = 2;
    public static final int NV_MAX_NUM_OF_ECC_NUMBER = 10;
    public static final int NV_MAX_SID_NID = 1;
    public static final int NV_OEM_BYTE_SIZE = 124;
    public static final int NV_OEM_ITEM_SIZE = 31;
    public static final int NV_PCB_NUMBER_SIZE = 32;
    public static final int NV_SEC_CODE_SIZE = 6;
    public static final int NV_SRV_DOMAIN_PREF_CS_ONLY = 0;
    public static final int NV_SRV_DOMAIN_PREF_CS_PS = 2;
    public static final int NV_SRV_DOMAIN_PREF_PS_ONLY = 1;
    public static final int P_REV_IS2000 = 6;
    public static final int P_REV_IS2000_RELA = 7;
    public static final int P_REV_IS2000_RELB = 8;
    public static final int P_REV_IS2000_RELC = 9;
    public static final int P_REV_IS2000_RELC_MI = 10;
    public static final int P_REV_IS2000_RELD = 11;
    public static final int P_REV_IS95A = 3;
    public static final int P_REV_IS95B = 4;
    public static final int P_REV_JSTD008 = 1;

    void disableAutoAnswer() throws IOException;

    void dispose();

    void enableAutoAnswer() throws IOException;

    Threegpp2Info get3gpp2SubscriptionInfo() throws IOException;

    TimerCount getAirTimerCount() throws IOException;

    AmrStatus getAmrStatus() throws IOException;

    @Deprecated
    int getAnalogHomeSid() throws IOException;

    AutoAnswer getAutoAnswerStatus() throws IOException;

    CdmaChannels getCdmaChannels() throws IOException;

    short getCurrentTtyMode() throws IOException;

    String getDefaultBaudRate() throws IOException;

    String[] getDeviceSerials() throws IOException;

    String getDirectoryNumber() throws IOException;

    String[] getEccList() throws IOException;

    String getEmailGateway() throws IOException;

    String getFtmMode() throws IOException;

    String getGpsOnePdeAddress() throws IOException;

    String getGpsOnePdePort() throws IOException;

    SidNid getHomeSidNid() throws IOException;

    String getImsi11_12() throws IOException;

    String getImsiMcc() throws IOException;

    @Deprecated
    String[] getImsiMin1() throws IOException;

    @Deprecated
    String[] getImsiMin2() throws IOException;

    String getLockCode() throws IOException;

    MinImsi getMinImsi() throws IOException;

    String getMinImsiNumber() throws IOException;

    short getMobCaiRev() throws IOException;

    String getNamName() throws IOException;

    PreferredVoiceSo getPreferredVoiceSo() throws IOException;

    int[] getPrimaryCdmaChannels() throws IOException;

    TimerCount getRoamTimerCount() throws IOException;

    short getRtreConfig() throws IOException;

    String getSecCode() throws IOException;

    int[] getSecondaryCdmaChannels() throws IOException;

    Boolean getSpcChangeEnabled() throws IOException;

    String getSwVersion() throws IOException;

    TrueImsi getTrueImsi() throws IOException;

    String getTrueImsi11_12() throws IOException;

    short getTrueImsiAddrNum() throws IOException;

    String getTrueImsiMcc() throws IOException;

    String getTrueImsiNumber() throws IOException;

    VoiceConfig getVoiceConfig() throws IOException;

    short getVoicePrivacyPref() throws IOException;

    void set3gpp2SubscriptionInfo(Threegpp2Info threegpp2Info) throws IOException, InvalidParameterException;

    void setAirTimerCount(TimerCount timerCount) throws IOException, InvalidParameterException;

    @Deprecated
    void setAnalogHomeSid(int i) throws IOException, InvalidParameterException;

    void setAutoAnswerStatus(AutoAnswer autoAnswer) throws IOException, InvalidParameterException;

    void setCdmaChannels(CdmaChannels cdmaChannels) throws IOException, InvalidParameterException;

    void setCurrentTtyMode(short s) throws IOException, InvalidParameterException;

    void setDefaultBaudRate(String str) throws IOException, InvalidParameterException;

    void setDirectoryNumber(String str) throws IOException, InvalidParameterException;

    void setEccList(String[] strArr) throws IOException, InvalidParameterException;

    void setEmailGateway(String str) throws IOException, InvalidParameterException;

    void setGpsOnePdeAddress(String str) throws IOException, InvalidParameterException;

    void setGpsOnePdePort(String str) throws IOException, InvalidParameterException;

    void setHomeSidNid(SidNid sidNid) throws IOException, InvalidParameterException;

    void setImsi11_12(String str) throws IOException, InvalidParameterException;

    void setImsiMcc(String str) throws IOException, InvalidParameterException;

    @Deprecated
    void setImsiMin1(String[] strArr) throws IOException, InvalidParameterException;

    @Deprecated
    void setImsiMin2(String[] strArr) throws IOException, InvalidParameterException;

    void setLockCode(String str) throws IOException, InvalidParameterException;

    void setMinImsi(MinImsi minImsi) throws IOException, InvalidParameterException;

    void setMinImsiNumber(String str) throws IOException, InvalidParameterException;

    @Deprecated
    void setMobCaiRev(short s) throws IOException, InvalidParameterException;

    void setPreferredVoiceSo(PreferredVoiceSo preferredVoiceSo) throws IOException, InvalidParameterException;

    void setPrimaryCdmaChannels(int[] iArr) throws IOException, InvalidParameterException;

    void setRoamTimerCount(TimerCount timerCount) throws IOException, InvalidParameterException;

    void setRtreConfig(short s) throws IOException, InvalidParameterException;

    void setSecCode(String str) throws IOException, InvalidParameterException;

    void setSecondaryCdmaChannels(int[] iArr) throws IOException, InvalidParameterException;

    void setSpcChangeEnabled(Boolean bool) throws IOException, InvalidParameterException;

    void setTrueImsi(TrueImsi trueImsi) throws IOException, InvalidParameterException;

    void setTrueImsi11_12(String str) throws IOException, InvalidParameterException;

    void setTrueImsiAddrNum(short s) throws IOException, InvalidParameterException;

    void setTrueImsiMcc(String str) throws IOException, InvalidParameterException;

    void setTrueImsiNumber(String str) throws IOException, InvalidParameterException;

    void setVoiceConfig(VoiceConfig voiceConfig) throws IOException, InvalidParameterException;

    void updateAkey(String str) throws IOException, InvalidParameterException;

    void updateSpCode() throws IOException;
}
