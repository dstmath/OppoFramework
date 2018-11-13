package com.android.internal.telephony;

import android.os.Handler;
import android.os.Message;
import android.service.carrier.CarrierIdentifier;
import android.telephony.SmsParameters;
import com.android.internal.telephony.cdma.CdmaSmsBroadcastConfigInfo;
import com.android.internal.telephony.dataconnection.DataProfile;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.mediatek.common.telephony.gsm.PBEntry;
import com.mediatek.internal.telephony.FemtoCellInfo;
import com.mediatek.internal.telephony.uicc.PhbEntry;
import java.util.List;

public interface CommandsInterface {
    public static final int CAT_CORPORATE = 3;
    public static final int CAT_NETOWRK_SUBSET = 1;
    public static final int CAT_NETWOEK = 0;
    public static final int CAT_SERVICE_PROVIDER = 2;
    public static final int CAT_SIM = 4;
    public static final String CB_FACILITY_BAIC = "AI";
    public static final String CB_FACILITY_BAICr = "IR";
    public static final String CB_FACILITY_BAOC = "AO";
    public static final String CB_FACILITY_BAOIC = "OI";
    public static final String CB_FACILITY_BAOICxH = "OX";
    public static final String CB_FACILITY_BA_ALL = "AB";
    public static final String CB_FACILITY_BA_FD = "FD";
    public static final String CB_FACILITY_BA_MO = "AG";
    public static final String CB_FACILITY_BA_MT = "AC";
    public static final String CB_FACILITY_BA_SIM = "SC";
    public static final int CDMA_SMS_FAIL_CAUSE_ENCODING_PROBLEM = 96;
    public static final int CDMA_SMS_FAIL_CAUSE_INVALID_TELESERVICE_ID = 4;
    public static final int CDMA_SMS_FAIL_CAUSE_OTHER_TERMINAL_PROBLEM = 39;
    public static final int CDMA_SMS_FAIL_CAUSE_RESOURCE_SHORTAGE = 35;
    public static final int CF_ACTION_DISABLE = 0;
    public static final int CF_ACTION_ENABLE = 1;
    public static final int CF_ACTION_ERASURE = 4;
    public static final int CF_ACTION_REGISTRATION = 3;
    public static final int CF_ACTION_UNUSED = 2;
    public static final int CF_REASON_ALL = 4;
    public static final int CF_REASON_ALL_CONDITIONAL = 5;
    public static final int CF_REASON_BUSY = 1;
    public static final int CF_REASON_NOT_REACHABLE = 3;
    public static final int CF_REASON_NOT_REGISTERED = 6;
    public static final int CF_REASON_NO_REPLY = 2;
    public static final int CF_REASON_UNCONDITIONAL = 0;
    public static final int CLIR_DEFAULT = 0;
    public static final int CLIR_INVOCATION = 1;
    public static final int CLIR_SUPPRESSION = 2;
    public static final int GSM_SMS_FAIL_CAUSE_MEMORY_CAPACITY_EXCEEDED = 211;
    public static final int GSM_SMS_FAIL_CAUSE_UNSPECIFIED_ERROR = 255;
    public static final int GSM_SMS_FAIL_CAUSE_USIM_APP_TOOLKIT_BUSY = 212;
    public static final int GSM_SMS_FAIL_CAUSE_USIM_DATA_DOWNLOAD_ERROR = 213;
    public static final int OP_ADD = 2;
    public static final int OP_LOCK = 1;
    public static final int OP_PERMANENT_UNLOCK = 4;
    public static final int OP_REMOVE = 3;
    public static final int OP_UNLOCK = 0;
    public static final int SERVICE_CLASS_DATA = 2;
    public static final int SERVICE_CLASS_DATA_ASYNC = 32;
    public static final int SERVICE_CLASS_DATA_SYNC = 16;
    public static final int SERVICE_CLASS_FAX = 4;
    public static final int SERVICE_CLASS_LINE2 = 256;
    public static final int SERVICE_CLASS_MAX = 512;
    public static final int SERVICE_CLASS_NONE = 0;
    public static final int SERVICE_CLASS_PACKET = 64;
    public static final int SERVICE_CLASS_PAD = 128;
    public static final int SERVICE_CLASS_SMS = 8;
    public static final int SERVICE_CLASS_VIDEO = 512;
    public static final int SERVICE_CLASS_VOICE = 1;
    public static final int USSD_HANDLED_BY_STK = 3;
    public static final int USSD_MODE_LOCAL_CLIENT = 3;
    public static final int USSD_MODE_NOTIFY = 0;
    public static final int USSD_MODE_NOT_SUPPORTED = 4;
    public static final int USSD_MODE_NW_RELEASE = 2;
    public static final int USSD_MODE_NW_TIMEOUT = 5;
    public static final int USSD_MODE_REQUEST = 1;
    public static final int USSD_NETWORK_TIMEOUT = 5;
    public static final int USSD_OPERATION_NOT_SUPPORTED = 4;
    public static final int USSD_SESSION_END = 2;

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum RadioState {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.CommandsInterface.RadioState.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.CommandsInterface.RadioState.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.CommandsInterface.RadioState.<clinit>():void");
        }

        public boolean isOn() {
            return this == RADIO_ON;
        }

        public boolean isAvailable() {
            return this != RADIO_UNAVAILABLE;
        }
    }

    void ReadPhbEntry(int i, int i2, int i3, Message message);

    void abortFemtoCellList(Message message);

    void acceptCall(Message message);

    void acceptVtCallWithVoiceOnly(int i, Message message);

    void acknowledgeIncomingGsmSmsWithPdu(boolean z, String str, Message message);

    void acknowledgeLastIncomingCdmaSms(boolean z, int i, Message message);

    void acknowledgeLastIncomingGsmSms(boolean z, int i, Message message);

    void addConferenceMember(int i, String str, int i2, Message message);

    void cancelAvailableNetworks(Message message);

    void cancelPendingUssd(Message message);

    void changeBarringPassword(String str, String str2, String str3, Message message);

    void changeBarringPassword(String str, String str2, String str3, String str4, Message message);

    void changeIccPin(String str, String str2, Message message);

    void changeIccPin2(String str, String str2, Message message);

    void changeIccPin2ForApp(String str, String str2, String str3, Message message);

    void changeIccPinForApp(String str, String str2, String str3, Message message);

    void conference(Message message);

    void conferenceDial(String[] strArr, int i, boolean z, Message message);

    void deactivateDataCall(int i, int i2, Message message);

    void deleteSmsOnRuim(int i, Message message);

    void deleteSmsOnSim(int i, Message message);

    void deleteUPBEntry(int i, int i2, int i3, Message message);

    void dial(String str, int i, Message message);

    void dial(String str, int i, UUSInfo uUSInfo, Message message);

    void disablePseudoBSMonitor(Message message);

    void doGeneralSimAuthentication(int i, int i2, int i3, String str, String str2, Message message);

    void editUPBEntry(int i, int i2, int i3, String str, String str2, Message message);

    void editUPBEntry(int i, int i2, int i3, String str, String str2, String str3, Message message);

    void emergencyDial(String str, int i, UUSInfo uUSInfo, Message message);

    void enableMd3Sleep(int i);

    void enablePseudoBSMonitor(int i, boolean z, int i2, Message message);

    void enablePseudoBSMonitor(boolean z, int i, Message message);

    void exitEmergencyCallbackMode(Message message);

    void explicitCallTransfer(Message message);

    void getAllowedCarriers(Message message);

    void getAvailableNetworks(Message message);

    void getBasebandVersion(Message message);

    RadioCapability getBootupRadioCapability();

    void getCDMASubscription(Message message);

    void getCLIR(Message message);

    void getCOLP(Message message);

    void getCOLR(Message message);

    void getCarrierRestrictionState(Message message);

    void getCdmaBroadcastConfig(Message message);

    void getCdmaSubscriptionSource(Message message);

    void getCellInfoList(Message message);

    void getCurrentCalls(Message message);

    void getCurrentPOLList(Message message);

    void getDataCallList(Message message);

    void getDataRegistrationState(Message message);

    void getDeviceIdentity(Message message);

    int getDisplayState();

    void getFemtoCellList(String str, int i, Message message);

    void getGsmBroadcastActivation(Message message);

    void getGsmBroadcastConfig(Message message);

    void getGsmBroadcastConfigEx(Message message);

    void getGsmBroadcastLangs(Message message);

    void getHardwareConfig(Message message);

    void getIMEI(Message message);

    void getIMEISV(Message message);

    void getIMSI(Message message);

    void getIMSIForApp(String str, Message message);

    void getIccApplicationStatus(int i, Message message);

    void getIccCardStatus(Message message);

    void getImsRegistrationState(Message message);

    void getLastCallFailCause(Message message);

    void getLastDataCallFailCause(Message message);

    @Deprecated
    void getLastPdpFailCause(Message message);

    int getLteOnCdmaMode();

    void getModemActivityInfo(Message message);

    void getMute(Message message);

    void getNeighboringCids(Message message);

    void getNetworkSelectionMode(Message message);

    void getOperator(Message message);

    @Deprecated
    void getPDPContextList(Message message);

    void getPOLCapabilty(Message message);

    void getPhoneBookMemStorage(Message message);

    void getPhoneBookStringsLength(Message message);

    void getPreferredNetworkType(Message message);

    void getPreferredVoicePrivacy(Message message);

    void getRadioCapability(Message message);

    RadioState getRadioState();

    int getRilVersion();

    void getRxTestResult(Message message);

    void getSignalStrength(Message message);

    void getSmsParameters(Message message);

    void getSmsRuimMemoryStatus(Message message);

    void getSmsSimMemoryStatus(Message message);

    void getSmscAddress(Message message);

    void getVoiceRadioTechnology(Message message);

    void getVoiceRegistrationState(Message message);

    void handleCallSetupRequestFromSim(boolean z, int i, Message message);

    void hangupAll(Message message);

    void hangupConnection(int i, Message message);

    void hangupForegroundResumeBackground(Message message);

    void hangupWaitingOrBackground(Message message);

    void holdCall(int i, Message message);

    void iccCloseLogicalChannel(int i, Message message);

    void iccGetATR(Message message);

    void iccIO(int i, int i2, String str, int i3, int i4, int i5, String str2, String str3, Message message);

    void iccIOForApp(int i, int i2, String str, int i3, int i4, int i5, String str2, String str3, String str4, Message message);

    void iccOpenChannelWithSw(String str, Message message);

    void iccOpenLogicalChannel(String str, Message message);

    void iccTransmitApduBasicChannel(int i, int i2, int i3, int i4, int i5, String str, Message message);

    void iccTransmitApduLogicalChannel(int i, int i2, int i3, int i4, int i5, int i6, String str, Message message);

    void invokeOemRilRequestRaw(byte[] bArr, Message message);

    void invokeOemRilRequestStrings(String[] strArr, Message message);

    boolean isGettingAvailableNetworks();

    String lookupOperatorNameFromNetwork(long j, String str, boolean z);

    void notifyCachedCdmaSms();

    void notifyCachedSimSms();

    void notifyCachedSms();

    void notifyCachedStatusSms();

    void nvReadItem(int i, Message message);

    void nvResetConfig(int i, Message message);

    void nvWriteCdmaPrl(byte[] bArr, Message message);

    void nvWriteItem(int i, String str, Message message);

    void openIccApplication(int i, Message message);

    int oppoGetPreferredNetworkType();

    void pullLceData(Message message);

    void queryAvailableBandMode(Message message);

    void queryCLIP(Message message);

    void queryCallForwardInTimeSlotStatus(int i, int i2, Message message);

    void queryCallForwardStatus(int i, int i2, String str, Message message);

    void queryCallWaiting(int i, Message message);

    void queryCdmaRoamingPreference(Message message);

    void queryCellBroadcastConfigInfo(Message message);

    void queryFacilityLock(String str, String str2, int i, Message message);

    void queryFacilityLockForApp(String str, String str2, int i, String str3, Message message);

    void queryFemtoCellSystemSelectionMode(Message message);

    void queryModemType(Message message);

    void queryNetworkLock(int i, Message message);

    void queryPhbStorageInfo(int i, Message message);

    void queryPseudoBSRecords(Message message);

    void queryTTYMode(Message message);

    void queryUPBAvailable(int i, int i2, Message message);

    void queryUPBCapability(Message message);

    void readPhoneBookEntryExt(int i, int i2, Message message);

    void readUPBAasList(int i, int i2, Message message);

    void readUPBAnrEntry(int i, int i2, Message message);

    void readUPBEmailEntry(int i, int i2, Message message);

    void readUPBGasList(int i, int i2, Message message);

    void readUPBGrpEntry(int i, Message message);

    void readUPBSneEntry(int i, int i2, Message message);

    void registerFoT53ClirlInfo(Handler handler, int i, Object obj);

    void registerForAbnormalEvent(Handler handler, int i, Object obj);

    void registerForAttachApnChanged(Handler handler, int i, Object obj);

    void registerForAvailable(Handler handler, int i, Object obj);

    void registerForCallAccepted(Handler handler, int i, Object obj);

    void registerForCallForwardingInfo(Handler handler, int i, Object obj);

    void registerForCallInfo(Handler handler, int i, Object obj);

    void registerForCallRedialState(Handler handler, int i, Object obj);

    void registerForCallStateChanged(Handler handler, int i, Object obj);

    void registerForCallWaitingInfo(Handler handler, int i, Object obj);

    void registerForCdmaOtaProvision(Handler handler, int i, Object obj);

    void registerForCdmaPrlChanged(Handler handler, int i, Object obj);

    void registerForCdmaSubscriptionChanged(Handler handler, int i, Object obj);

    void registerForCellInfoList(Handler handler, int i, Object obj);

    void registerForCipherIndication(Handler handler, int i, Object obj);

    void registerForCommonSlotNoChanged(Handler handler, int i, Object obj);

    void registerForCsNetworkStateChanged(Handler handler, int i, Object obj);

    void registerForDataNetworkStateChanged(Handler handler, int i, Object obj);

    void registerForDisplayInfo(Handler handler, int i, Object obj);

    void registerForEconfResult(Handler handler, int i, Object obj);

    void registerForEconfSrvcc(Handler handler, int i, Object obj);

    void registerForEfCspPlmnModeBitChanged(Handler handler, int i, Object obj);

    void registerForExitEmergencyCallbackMode(Handler handler, int i, Object obj);

    void registerForFemtoCellInfo(Handler handler, int i, Object obj);

    void registerForGetAvailableNetworksDone(Handler handler, int i, Object obj);

    void registerForGmssRatChanged(Handler handler, int i, Object obj);

    void registerForHardwareConfigChanged(Handler handler, int i, Object obj);

    void registerForIMEILock(Handler handler, int i, Object obj);

    void registerForIccRefresh(Handler handler, int i, Object obj);

    void registerForIccStatusChanged(Handler handler, int i, Object obj);

    void registerForImsDisable(Handler handler, int i, Object obj);

    void registerForImsDisableDone(Handler handler, int i, Object obj);

    void registerForImsEnable(Handler handler, int i, Object obj);

    void registerForImsNetworkStateChanged(Handler handler, int i, Object obj);

    void registerForImsRegistrationInfo(Handler handler, int i, Object obj);

    void registerForImsiRefreshDone(Handler handler, int i, Object obj);

    void registerForInCallVoicePrivacyOff(Handler handler, int i, Object obj);

    void registerForInCallVoicePrivacyOn(Handler handler, int i, Object obj);

    void registerForLceInfo(Handler handler, int i, Object obj);

    void registerForLineControlInfo(Handler handler, int i, Object obj);

    void registerForLteAccessStratumState(Handler handler, int i, Object obj);

    void registerForMdDataRetryCountReset(Handler handler, int i, Object obj);

    void registerForMelockChanged(Handler handler, int i, Object obj);

    void registerForModulation(Handler handler, int i, Object obj);

    void registerForNeighboringInfo(Handler handler, int i, Object obj);

    void registerForNetworkEvent(Handler handler, int i, Object obj);

    void registerForNetworkExsit(Handler handler, int i, Object obj);

    void registerForNetworkInfo(Handler handler, int i, Object obj);

    void registerForNotAvailable(Handler handler, int i, Object obj);

    void registerForNumberInfo(Handler handler, int i, Object obj);

    void registerForOemScreenChanged(Handler handler, int i, Object obj);

    void registerForOffOrNotAvailable(Handler handler, int i, Object obj);

    void registerForOn(Handler handler, int i, Object obj);

    void registerForPcoData(Handler handler, int i, Object obj);

    void registerForPcoStatus(Handler handler, int i, Object obj);

    void registerForPhbReady(Handler handler, int i, Object obj);

    void registerForPsNetworkStateChanged(Handler handler, int i, Object obj);

    void registerForRadioCapabilityChanged(Handler handler, int i, Object obj);

    void registerForRadioStateChanged(Handler handler, int i, Object obj);

    void registerForRedirectedNumberInfo(Handler handler, int i, Object obj);

    void registerForRemoveRestrictEutran(Handler handler, int i, Object obj);

    void registerForResendIncallMute(Handler handler, int i, Object obj);

    void registerForResetAttachApn(Handler handler, int i, Object obj);

    void registerForRilConnected(Handler handler, int i, Object obj);

    void registerForRingbackTone(Handler handler, int i, Object obj);

    void registerForSessionChanged(Handler handler, int i, Object obj);

    void registerForSignalInfo(Handler handler, int i, Object obj);

    void registerForSimMissing(Handler handler, int i, Object obj);

    void registerForSimPlugIn(Handler handler, int i, Object obj);

    void registerForSimPlugOut(Handler handler, int i, Object obj);

    void registerForSimRecovery(Handler handler, int i, Object obj);

    void registerForSmsReady(Handler handler, int i, Object obj);

    void registerForSrvccStateChanged(Handler handler, int i, Object obj);

    void registerForSubscriptionStatusChanged(Handler handler, int i, Object obj);

    void registerForT53AudioControlInfo(Handler handler, int i, Object obj);

    void registerForTrayPlugIn(Handler handler, int i, Object obj);

    void registerForTxPower(Handler handler, int i, Object obj);

    void registerForVirtualSimOff(Handler handler, int i, Object obj);

    void registerForVirtualSimOn(Handler handler, int i, Object obj);

    void registerForVoiceNetworkStateChanged(Handler handler, int i, Object obj);

    void registerForVoiceRadioTechChanged(Handler handler, int i, Object obj);

    void registerForVtRingInfo(Handler handler, int i, Object obj);

    void registerForVtStatusInfo(Handler handler, int i, Object obj);

    void registerSetDataAllowed(Handler handler, int i, Object obj);

    void rejectCall(Message message);

    void reloadModemType(int i, Message message);

    void removeCellBroadcastMsg(int i, int i2, Message message);

    void removeConferenceMember(int i, String str, int i2, Message message);

    void replaceVtCall(int i, Message message);

    void reportSmsMemoryStatus(boolean z, Message message);

    void reportStkServiceIsRunning(Message message);

    void requestIccSimAuthentication(int i, String str, String str2, Message message);

    void requestIsimAuthentication(String str, Message message);

    void requestShutdown(Message message);

    void resetMdDataRetryCount(String str, Message message);

    void resetRadio(Message message);

    void resumeCall(int i, Message message);

    void selectFemtoCell(FemtoCellInfo femtoCellInfo, Message message);

    void sendBTSIMProfile(int i, int i2, String str, Message message);

    void sendBurstDtmf(String str, int i, int i2, Message message);

    void sendCDMAFeatureCode(String str, Message message);

    void sendCNAPSS(String str, Message message);

    void sendCdmaSms(byte[] bArr, Message message);

    void sendDtmf(char c, Message message);

    void sendEnvelope(String str, Message message);

    void sendEnvelopeWithStatus(String str, Message message);

    void sendImsCdmaSms(byte[] bArr, int i, int i2, Message message);

    void sendImsGsmSms(String str, String str2, int i, int i2, Message message);

    void sendSMS(String str, String str2, Message message);

    void sendSMSExpectMore(String str, String str2, Message message);

    void sendScreenState(boolean z);

    void sendTerminalResponse(String str, Message message);

    void sendUSSD(String str, Message message);

    void separateConnection(int i, Message message);

    void setAllowedCarriers(List<CarrierIdentifier> list, Message message);

    void setBandMode(int i, Message message);

    void setBandMode(int[] iArr, Message message);

    void setCDMACardInitalEsnMeid(Handler handler, int i, Object obj);

    void setCLIP(boolean z, Message message);

    void setCLIR(int i, Message message);

    void setCOLP(boolean z, Message message);

    void setCallForward(int i, int i2, int i3, String str, int i4, Message message);

    void setCallForwardInTimeSlot(int i, int i2, int i3, String str, int i4, long[] jArr, Message message);

    void setCallIndication(int i, int i2, int i3, Message message);

    void setCallWaiting(boolean z, int i, Message message);

    void setCarrierRestrictionState(int i, String str, Message message);

    void setCdmaBroadcastActivation(boolean z, Message message);

    void setCdmaBroadcastConfig(CdmaSmsBroadcastConfigInfo[] cdmaSmsBroadcastConfigInfoArr, Message message);

    void setCdmaRoamingPreference(int i, Message message);

    void setCdmaSubscriptionSource(int i, Message message);

    void setCellBroadcastChannelConfigInfo(String str, int i, Message message);

    void setCellBroadcastLanguageConfigInfo(String str, Message message);

    void setCellInfoListRate(int i, Message message);

    void setCurrentStatus(int i, int i2, Message message);

    void setDataAllowed(boolean z, Message message);

    void setDataCentric(boolean z, Message message);

    void setDataProfile(DataProfile[] dataProfileArr, Message message);

    void setEccPreferredRat(int i, Message message);

    void setEccServiceCategory(int i);

    void setEmergencyCallbackMode(Handler handler, int i, Object obj);

    void setEtws(int i, Message message);

    void setFDMode(int i, int i2, int i3, Message message);

    void setFacilityLock(String str, boolean z, String str2, int i, Message message);

    void setFacilityLockForApp(String str, boolean z, String str2, int i, String str3, Message message);

    void setFemtoCellSystemSelectionMode(int i, Message message);

    void setGsmBroadcastActivation(boolean z, Message message);

    void setGsmBroadcastConfig(SmsBroadcastConfigInfo[] smsBroadcastConfigInfoArr, Message message);

    void setGsmBroadcastConfigEx(SmsBroadcastConfigInfo[] smsBroadcastConfigInfoArr, Message message);

    void setGsmBroadcastLangs(String str, Message message);

    void setIMSEnabled(boolean z, Message message);

    void setImsCallStatus(boolean z, Message message);

    void setInitialAttachApn(String str, String str2, int i, String str3, String str4, Message message);

    void setInitialAttachApn(String str, String str2, int i, String str3, String str4, Object obj, Message message);

    void setInvalidSimInfo(Handler handler, int i, Object obj);

    void setLocationUpdates(boolean z, Message message);

    void setLteAccessStratumReport(boolean z, Message message);

    void setLteUplinkDataTransfer(int i, int i2, Message message);

    void setModemPower(boolean z, Message message);

    void setMute(boolean z, Message message);

    void setNetworkLock(int i, int i2, String str, String str2, String str3, String str4, Message message);

    void setNetworkSelectionModeAutomatic(Message message);

    void setNetworkSelectionModeManual(String str, Message message);

    void setNetworkSelectionModeManualWithAct(String str, String str2, Message message);

    void setNetworkSelectionModeSemiAutomatic(String str, String str2, Message message);

    void setOnBipProactiveCmd(Handler handler, int i, Object obj);

    void setOnCallRelatedSuppSvc(Handler handler, int i, Object obj);

    void setOnCallRing(Handler handler, int i, Object obj);

    void setOnCatCallSetUp(Handler handler, int i, Object obj);

    void setOnCatCcAlphaNotify(Handler handler, int i, Object obj);

    void setOnCatEvent(Handler handler, int i, Object obj);

    void setOnCatProactiveCmd(Handler handler, int i, Object obj);

    void setOnCatSessionEnd(Handler handler, int i, Object obj);

    void setOnEtwsNotification(Handler handler, int i, Object obj);

    void setOnIccRefresh(Handler handler, int i, Object obj);

    void setOnIccSmsFull(Handler handler, int i, Object obj);

    void setOnIncomingCallIndication(Handler handler, int i, Object obj);

    void setOnMeSmsFull(Handler handler, int i, Object obj);

    void setOnNITZTime(Handler handler, int i, Object obj);

    void setOnNewCdmaSms(Handler handler, int i, Object obj);

    void setOnNewGsmBroadcastSms(Handler handler, int i, Object obj);

    void setOnNewGsmSms(Handler handler, int i, Object obj);

    void setOnPlmnChangeNotification(Handler handler, int i, Object obj);

    void setOnRegistrationSuspended(Handler handler, int i, Object obj);

    void setOnRestrictedStateChanged(Handler handler, int i, Object obj);

    void setOnSignalStrengthUpdate(Handler handler, int i, Object obj);

    void setOnSmsOnSim(Handler handler, int i, Object obj);

    void setOnSmsStatus(Handler handler, int i, Object obj);

    void setOnSpeechCodecInfo(Handler handler, int i, Object obj);

    void setOnSs(Handler handler, int i, Object obj);

    void setOnStkCallCtrl(Handler handler, int i, Object obj);

    void setOnStkEvdlCall(Handler handler, int i, Object obj);

    void setOnStkSetupMenuReset(Handler handler, int i, Object obj);

    void setOnSuppServiceNotification(Handler handler, int i, Object obj);

    void setOnUSSD(Handler handler, int i, Object obj);

    void setOnUnsolOemHookRaw(Handler handler, int i, Object obj);

    void setPOLEntry(int i, String str, int i2, Message message);

    void setPhoneBookMemStorage(String str, String str2, Message message);

    void setPhoneType(int i);

    void setPreferredNetworkType(int i, Message message);

    void setPreferredVoicePrivacy(boolean z, Message message);

    void setRadioCapability(RadioCapability radioCapability, Message message);

    void setRadioPower(boolean z, Message message);

    void setRegistrationSuspendEnabled(int i, Message message);

    void setRemoveRestrictEutranMode(boolean z, Message message);

    void setResumeRegistration(int i, Message message);

    void setRxTestConfig(int i, Message message);

    void setSimPower(int i, Message message);

    void setSmsParameters(SmsParameters smsParameters, Message message);

    void setSmscAddress(String str, Message message);

    void setSpeechCodecInfo(boolean z, Message message);

    void setStkEvdlCallByAP(int i, Message message);

    void setSuppServiceNotifications(boolean z, Message message);

    void setTTYMode(int i, Message message);

    void setTrm(int i, Message message);

    void setUiccSubscription(int i, int i2, int i3, int i4, Message message);

    void setupDataCall(int i, int i2, String str, String str2, String str3, int i3, String str4, int i4, Message message);

    void setupDataCall(int i, int i2, String str, String str2, String str3, int i3, String str4, Message message);

    void startDtmf(char c, Message message);

    void startLceService(int i, boolean z, Message message);

    void stopDtmf(Message message);

    void stopLceService(Message message);

    void storeModemType(int i, Message message);

    void supplyIccPin(String str, Message message);

    void supplyIccPin2(String str, Message message);

    void supplyIccPin2ForApp(String str, String str2, Message message);

    void supplyIccPinForApp(String str, String str2, Message message);

    void supplyIccPuk(String str, String str2, Message message);

    void supplyIccPuk2(String str, String str2, Message message);

    void supplyIccPuk2ForApp(String str, String str2, String str3, Message message);

    void supplyIccPukForApp(String str, String str2, String str3, Message message);

    void supplyNetworkDepersonalization(String str, Message message);

    void switchWaitingOrHoldingAndActive(Message message);

    void syncApnTable(String[] strArr, Message message);

    void syncApnTableToRds(String[] strArr, Message message);

    void syncDataSettingsToMd(int[] iArr, Message message);

    void testingEmergencyCall();

    void triggerModeSwitchByEcc(int i, Message message);

    void unSetCDMACardInitalEsnMeid(Handler handler);

    void unSetInvalidSimInfo(Handler handler);

    void unSetOnBipProactiveCmd(Handler handler);

    void unSetOnCallRelatedSuppSvc(Handler handler);

    void unSetOnCallRing(Handler handler);

    void unSetOnCatCallSetUp(Handler handler);

    void unSetOnCatCcAlphaNotify(Handler handler);

    void unSetOnCatEvent(Handler handler);

    void unSetOnCatProactiveCmd(Handler handler);

    void unSetOnCatSessionEnd(Handler handler);

    void unSetOnEtwsNotification(Handler handler);

    void unSetOnIccSmsFull(Handler handler);

    void unSetOnMeSmsFull(Handler handler);

    void unSetOnNITZTime(Handler handler);

    void unSetOnNewCdmaSms(Handler handler);

    void unSetOnNewGsmBroadcastSms(Handler handler);

    void unSetOnNewGsmSms(Handler handler);

    void unSetOnPlmnChangeNotification(Handler handler);

    void unSetOnRegistrationSuspended(Handler handler);

    void unSetOnRestrictedStateChanged(Handler handler);

    void unSetOnSignalStrengthUpdate(Handler handler);

    void unSetOnSmsOnSim(Handler handler);

    void unSetOnSmsStatus(Handler handler);

    void unSetOnSpeechCodecInfo(Handler handler);

    void unSetOnSs(Handler handler);

    void unSetOnStkCallCtrl(Handler handler);

    void unSetOnStkEvdlCall(Handler handler);

    void unSetOnStkSetupMenuReset(Handler handler);

    void unSetOnSuppServiceNotification(Handler handler);

    void unSetOnUSSD(Handler handler);

    void unSetOnUnsolOemHookRaw(Handler handler);

    void unregisterForAbnormalEvent(Handler handler);

    void unregisterForAttachApnChanged(Handler handler);

    void unregisterForAvailable(Handler handler);

    void unregisterForCallAccepted(Handler handler);

    void unregisterForCallForwardingInfo(Handler handler);

    void unregisterForCallInfo(Handler handler);

    void unregisterForCallRedialState(Handler handler);

    void unregisterForCallStateChanged(Handler handler);

    void unregisterForCallWaitingInfo(Handler handler);

    void unregisterForCdmaOtaProvision(Handler handler);

    void unregisterForCdmaPrlChanged(Handler handler);

    void unregisterForCdmaSubscriptionChanged(Handler handler);

    void unregisterForCellInfoList(Handler handler);

    void unregisterForCipherIndication(Handler handler);

    void unregisterForCommonSlotNoChanged(Handler handler);

    void unregisterForCsNetworkStateChanged(Handler handler);

    void unregisterForDataNetworkStateChanged(Handler handler);

    void unregisterForDisplayInfo(Handler handler);

    void unregisterForEconfResult(Handler handler);

    void unregisterForEconfSrvcc(Handler handler);

    void unregisterForEfCspPlmnModeBitChanged(Handler handler);

    void unregisterForExitEmergencyCallbackMode(Handler handler);

    void unregisterForFemtoCellInfo(Handler handler);

    void unregisterForGetAvailableNetworksDone(Handler handler);

    void unregisterForHardwareConfigChanged(Handler handler);

    void unregisterForIMEILock(Handler handler);

    void unregisterForIccRefresh(Handler handler);

    void unregisterForIccStatusChanged(Handler handler);

    void unregisterForImsDisable(Handler handler);

    void unregisterForImsDisableDone(Handler handler);

    void unregisterForImsEnable(Handler handler);

    void unregisterForImsNetworkStateChanged(Handler handler);

    void unregisterForImsRegistrationInfo(Handler handler);

    void unregisterForImsiRefreshDone(Handler handler);

    void unregisterForInCallVoicePrivacyOff(Handler handler);

    void unregisterForInCallVoicePrivacyOn(Handler handler);

    void unregisterForLceInfo(Handler handler);

    void unregisterForLineControlInfo(Handler handler);

    void unregisterForLteAccessStratumState(Handler handler);

    void unregisterForMdDataRetryCountReset(Handler handler);

    void unregisterForMelockChanged(Handler handler);

    void unregisterForModulation(Handler handler);

    void unregisterForNeighboringInfo(Handler handler);

    void unregisterForNetworkEvent(Handler handler);

    void unregisterForNetworkExsit(Handler handler);

    void unregisterForNetworkInfo(Handler handler);

    void unregisterForNotAvailable(Handler handler);

    void unregisterForNumberInfo(Handler handler);

    void unregisterForOffOrNotAvailable(Handler handler);

    void unregisterForOn(Handler handler);

    void unregisterForPcoData(Handler handler);

    void unregisterForPcoStatus(Handler handler);

    void unregisterForPhbReady(Handler handler);

    void unregisterForPsNetworkStateChanged(Handler handler);

    void unregisterForRadioCapabilityChanged(Handler handler);

    void unregisterForRadioStateChanged(Handler handler);

    void unregisterForRedirectedNumberInfo(Handler handler);

    void unregisterForRemoveRestrictEutran(Handler handler);

    void unregisterForResendIncallMute(Handler handler);

    void unregisterForResetAttachApn(Handler handler);

    void unregisterForRilConnected(Handler handler);

    void unregisterForRingbackTone(Handler handler);

    void unregisterForSessionChanged(Handler handler);

    void unregisterForSignalInfo(Handler handler);

    void unregisterForSimMissing(Handler handler);

    void unregisterForSimPlugIn(Handler handler);

    void unregisterForSimPlugOut(Handler handler);

    void unregisterForSimRecovery(Handler handler);

    void unregisterForSmsReady(Handler handler);

    void unregisterForSrvccStateChanged(Handler handler);

    void unregisterForSubscriptionStatusChanged(Handler handler);

    void unregisterForT53AudioControlInfo(Handler handler);

    void unregisterForT53ClirInfo(Handler handler);

    void unregisterForTrayPlugIn(Handler handler);

    void unregisterForTxPower(Handler handler);

    void unregisterForVirtualSimOff(Handler handler);

    void unregisterForVirtualSimOn(Handler handler);

    void unregisterForVoiceNetworkStateChanged(Handler handler);

    void unregisterForVoiceRadioTechChanged(Handler handler);

    void unregisterForVtRingInfo(Handler handler);

    void unregisterForVtStatusInfo(Handler handler);

    void unregisterOemScreenChanged(Handler handler);

    void unregisterSetDataAllowed(Handler handler);

    void unsetOnIccRefresh(Handler handler);

    void unsetOnIncomingCallIndication(Handler handler);

    void updateImsRegistrationStatus(int i, int i2, int i3);

    void vtDial(String str, int i, UUSInfo uUSInfo, Message message);

    void writePhbEntry(PhbEntry phbEntry, Message message);

    void writePhoneBookEntryExt(PBEntry pBEntry, Message message);

    void writeSmsToRuim(int i, String str, Message message);

    void writeSmsToSim(int i, String str, String str2, Message message);

    void writeUPBGrpEntry(int i, int[] iArr, Message message);
}
