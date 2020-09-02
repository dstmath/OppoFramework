package com.mediatek.internal.telephony;

import android.hardware.radio.V1_0.RadioResponseInfo;
import android.os.RemoteException;
import com.android.internal.telephony.RIL;
import java.util.ArrayList;
import vendor.mediatek.hardware.mtkradioex.V1_0.CallForwardInfoEx;
import vendor.mediatek.hardware.mtkradioex.V1_0.OperatorInfoWithAct;
import vendor.mediatek.hardware.mtkradioex.V1_0.PhbEntryExt;
import vendor.mediatek.hardware.mtkradioex.V1_0.PhbEntryStructure;
import vendor.mediatek.hardware.mtkradioex.V1_0.PhbMemStorageResponse;
import vendor.mediatek.hardware.mtkradioex.V1_0.SignalStrengthWithWcdmaEcio;
import vendor.mediatek.hardware.mtkradioex.V1_0.SmsMemStatus;
import vendor.mediatek.hardware.mtkradioex.V1_0.SmsParams;
import vendor.mediatek.hardware.mtkradioex.V1_0.VsimEvent;
import vendor.mediatek.hardware.mtkradioex.V1_5.IMtkRadioExResponse;

public class MtkRadioExResponseBase extends IMtkRadioExResponse.Stub {
    public MtkRadioExResponseBase(RIL ril) {
    }

    public void acknowledgeRequest(int serial) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setClipResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getColpResponse(RadioResponseInfo responseInfo, int n, int m) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getColrResponse(RadioResponseInfo responseInfo, int status) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void sendCnapResponse(RadioResponseInfo responseInfo, int n, int m) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setColpResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setColrResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void queryCallForwardInTimeSlotStatusResponse(RadioResponseInfo responseInfo, ArrayList<CallForwardInfoEx> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setCallForwardInTimeSlotResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void runGbaAuthenticationResponse(RadioResponseInfo responseInfo, ArrayList<String> arrayList) {
    }

    public void sendOemRilRequestRawResponse(RadioResponseInfo responseInfo, ArrayList<Byte> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setTrmResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getATRResponse(RadioResponseInfo info, String response) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getIccidResponse(RadioResponseInfo info, String response) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setSimPowerResponse(RadioResponseInfo info) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void activateUiccCardRsp(RadioResponseInfo info, int simPowerOnOffResponse) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void deactivateUiccCardRsp(RadioResponseInfo info, int simPowerOnOffResponse) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getCurrentUiccCardProvisioningStatusRsp(RadioResponseInfo info, int simPowerOnOffStatus) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setNetworkSelectionModeManualWithActResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getAvailableNetworksWithActResponse(RadioResponseInfo responseInfo, ArrayList<OperatorInfoWithAct> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getSignalStrengthWithWcdmaEcioResponse(RadioResponseInfo responseInfo, SignalStrengthWithWcdmaEcio signalStrength) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void cancelAvailableNetworksResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void cfgA2offsetResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void cfgB1offsetResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void enableSCGfailureResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioExResponse
    public void deactivateNrScgCommunicationResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioExResponse
    public void getDeactivateNrScgCommunicationResponse(RadioResponseInfo responseInfo, int deactivate, int allowSCGAdd) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void disableNRResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setTxPowerResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setSearchStoredFreqInfoResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setSearchRatResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setBgsrchDeltaSleepTimerResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setModemPowerResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getSmsParametersResponse(RadioResponseInfo responseInfo, SmsParams params) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setSmsParametersResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setEtwsResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void removeCbMsgResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getSmsMemStatusResponse(RadioResponseInfo responseInfo, SmsMemStatus params) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setGsmBroadcastLangsResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getGsmBroadcastLangsResponse(RadioResponseInfo responseInfo, String langs) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getGsmBroadcastActivationRsp(RadioResponseInfo responseInfo, int activation) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void sendEmbmsAtCommandResponse(RadioResponseInfo responseInfo, String result) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void hangupAllResponse(RadioResponseInfo responseInfo) throws RemoteException {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setCallIndicationResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setVoicePreferStatusResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setEccNumResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getEccNumResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setEccModeResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void eccPreferredRatResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setApcModeResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getApcInfoResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void triggerModeSwitchByEccResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getSmsRuimMemoryStatusResponse(RadioResponseInfo responseInfo, SmsMemStatus memStatus) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setFdModeResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setResumeRegistrationResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void modifyModemTypeResponse(RadioResponseInfo responseInfo, int applyType) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void handleStkCallSetupRequestFromSimWithResCodeResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void queryPhbStorageInfoResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void writePhbEntryResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void readPhbEntryResponse(RadioResponseInfo responseInfo, ArrayList<PhbEntryStructure> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void queryUPBCapabilityResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void editUPBEntryResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void deleteUPBEntryResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void readUPBGasListResponse(RadioResponseInfo responseInfo, ArrayList<String> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void readUPBGrpEntryResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void writeUPBGrpEntryResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getPhoneBookStringsLengthResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getPhoneBookMemStorageResponse(RadioResponseInfo responseInfo, PhbMemStorageResponse phbMemStorage) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setPhoneBookMemStorageResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void readPhoneBookEntryExtResponse(RadioResponseInfo responseInfo, ArrayList<PhbEntryExt> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void writePhoneBookEntryExtResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void queryUPBAvailableResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void readUPBEmailEntryResponse(RadioResponseInfo responseInfo, String email) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void readUPBSneEntryResponse(RadioResponseInfo responseInfo, String sne) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void readUPBAnrEntryResponse(RadioResponseInfo responseInfo, ArrayList<PhbEntryStructure> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void readUPBAasListResponse(RadioResponseInfo responseInfo, ArrayList<String> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setPhonebookReadyResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void restartRILDResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getFemtocellListResponse(RadioResponseInfo responseInfo, ArrayList<String> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void abortFemtocellListResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void selectFemtocellResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void queryFemtoCellSystemSelectionModeResponse(RadioResponseInfo responseInfo, int mode) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setFemtoCellSystemSelectionModeResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void syncDataSettingsToMdResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioExResponse
    public void setMaxUlSpeedResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void resetMdDataRetryCountResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setRemoveRestrictEutranModeResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setLteAccessStratumReportResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setLteUplinkDataTransferResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void queryNetworkLockResponse(RadioResponseInfo info, int catagory, int state, int retry_cnt, int autolock_cnt, int num_set, int total_set, int key_state) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setNetworkLockResponse(RadioResponseInfo info) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void supplyDepersonalizationResponse(RadioResponseInfo responseInfo, int retriesRemaining) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void supplyDeviceNetworkDepersonalizationResponse(RadioResponseInfo responseInfo, int remainingAttempts) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setRxTestConfigResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getRxTestResultResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getPOLCapabilityResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getCurrentPOLListResponse(RadioResponseInfo responseInfo, ArrayList<String> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setPOLEntryResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setRoamingEnableResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getRoamingEnableResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setLteReleaseVersionResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getLteReleaseVersionResponse(RadioResponseInfo responseInfo, int mode) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void vsimNotificationResponse(RadioResponseInfo info, VsimEvent event) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void vsimOperationResponse(RadioResponseInfo info) {
    }

    public void setWifiEnabledResponse(RadioResponseInfo responseInfo) {
    }

    public void setWifiAssociatedResponse(RadioResponseInfo responseInfo) {
    }

    public void setWifiSignalLevelResponse(RadioResponseInfo responseInfo) {
    }

    public void setWifiIpAddressResponse(RadioResponseInfo responseInfo) {
    }

    public void setLocationInfoResponse(RadioResponseInfo responseInfo) {
    }

    public void setEmergencyAddressIdResponse(RadioResponseInfo responseInfo) {
    }

    public void setNattKeepAliveStatusResponse(RadioResponseInfo responseInfo) {
    }

    public void setWifiPingResultResponse(RadioResponseInfo responseInfo) {
    }

    public void notifyEPDGScreenStateResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setServiceStateToModemResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void sendRequestRawResponse(RadioResponseInfo responseInfo, ArrayList<Byte> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void sendRequestStringsResponse(RadioResponseInfo responseInfo, ArrayList<String> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void dataConnectionAttachResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void dataConnectionDetachResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void resetAllConnectionsResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setTxPowerStatusResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setSuppServPropertyResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void hangupWithReasonResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setVendorSettingResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getPlmnNameFromSE13TableResponse(RadioResponseInfo info, String name) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void enableCAPlusBandWidthFilterResponse(RadioResponseInfo info) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setGwsdModeResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setCallValidTimerResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setIgnoreSameNumberIntervalResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setKeepAliveByPDCPCtrlPDUResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void setKeepAliveByIpDataResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void enableDsdaIndicationResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getDsdaStatusResponse(RadioResponseInfo responseInfo, int mode) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void registerCellQltyReportResponse(RadioResponseInfo responseInfo) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse
    public void getSuggestedPlmnListResponse(RadioResponseInfo responseInfo, ArrayList<String> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_5.IMtkRadioExResponse
    public void sendSarIndicatorResponse(RadioResponseInfo info) {
    }
}
