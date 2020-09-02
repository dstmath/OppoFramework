package com.mediatek.internal.telephony;

import android.hardware.radio.V1_0.OperatorInfo;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.V1_4.CellInfo;
import android.os.SystemProperties;
import android.telephony.MtkRadioAccessFamily;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.RILRequest;
import com.android.internal.telephony.RadioResponse;
import com.android.internal.telephony.ServiceStateTracker;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import java.util.ArrayList;

public class MtkRadioResponse extends RadioResponse {
    private static final String TAG = "MtkRadioResp";
    private MtkRIL mMtkRil;

    public MtkRadioResponse(RIL ril) {
        super(ril);
        this.mMtkRil = (MtkRIL) ril;
    }

    public void switchWaitingOrHoldingAndActiveResponse(RadioResponseInfo responseInfo) {
        this.mMtkRil.riljLog("clear mIsSendChldRequest");
        this.mMtkRil.mDtmfReqQueue.resetSendChldRequest();
        MtkRadioResponse.super.switchWaitingOrHoldingAndActiveResponse(responseInfo);
    }

    public void conferenceResponse(RadioResponseInfo responseInfo) {
        this.mMtkRil.riljLog("clear mIsSendChldRequest");
        this.mMtkRil.mDtmfReqQueue.resetSendChldRequest();
        MtkRadioResponse.super.conferenceResponse(responseInfo);
    }

    public void startDtmfResponse(RadioResponseInfo responseInfo) {
        this.mMtkRil.handleDtmfQueueNext(responseInfo.serial);
        MtkRadioResponse.super.startDtmfResponse(responseInfo);
    }

    public void stopDtmfResponse(RadioResponseInfo responseInfo) {
        this.mMtkRil.handleDtmfQueueNext(responseInfo.serial);
        MtkRadioResponse.super.stopDtmfResponse(responseInfo);
    }

    public void separateConnectionResponse(RadioResponseInfo responseInfo) {
        this.mMtkRil.riljLog("clear mIsSendChldRequest");
        this.mMtkRil.mDtmfReqQueue.resetSendChldRequest();
        MtkRadioResponse.super.separateConnectionResponse(responseInfo);
    }

    public void explicitCallTransferResponse(RadioResponseInfo responseInfo) {
        this.mMtkRil.riljLog("clear mIsSendChldRequest");
        this.mMtkRil.mDtmfReqQueue.resetSendChldRequest();
        MtkRadioResponse.super.explicitCallTransferResponse(responseInfo);
    }

    private int getSubId(int phoneId) {
        int[] subIds = SubscriptionManager.getSubId(phoneId);
        if (subIds == null || subIds.length <= 0) {
            return -1;
        }
        return subIds[0];
    }

    public void getAvailableNetworksResponse(RadioResponseInfo responseInfo, ArrayList<OperatorInfo> networkInfos) {
        int mLac = -1;
        String mPlmn = null;
        Phone phone = PhoneFactory.getPhone(this.mMtkRil.mInstanceId.intValue());
        if (phone != null) {
            ServiceStateTracker sst = phone.getServiceStateTracker();
            mPlmn = sst.mSS.getOperatorNumeric();
            mLac = ((MtkServiceStateTracker) sst).getLac();
        }
        for (int i = 0; i < networkInfos.size(); i++) {
            String mccmnc = networkInfos.get(i).operatorNumeric;
            String optr = SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR);
            if (!TextUtils.isEmpty(optr) && optr.equals("OP07")) {
                MtkRIL mtkRIL = this.mMtkRil;
                String operatorLongName = mtkRIL.lookupOperatorNameEons(getSubId(mtkRIL.mInstanceId.intValue()), mccmnc, true, mLac);
                if (TextUtils.isEmpty(operatorLongName)) {
                    MtkRIL mtkRIL2 = this.mMtkRil;
                    operatorLongName = mtkRIL2.lookupOperatorNameMVNO(getSubId(mtkRIL2.mInstanceId.intValue()), mccmnc, true);
                }
                networkInfos.get(i).alphaLong = operatorLongName;
                MtkRIL mtkRIL3 = this.mMtkRil;
                String operatorShortName = mtkRIL3.lookupOperatorNameEons(getSubId(mtkRIL3.mInstanceId.intValue()), mccmnc, false, mLac);
                if (TextUtils.isEmpty(operatorShortName)) {
                    MtkRIL mtkRIL4 = this.mMtkRil;
                    operatorShortName = mtkRIL4.lookupOperatorNameMVNO(getSubId(mtkRIL4.mInstanceId.intValue()), mccmnc, false);
                }
                networkInfos.get(i).alphaShort = operatorShortName;
            } else if (TextUtils.isEmpty(mPlmn) || TextUtils.isEmpty(mccmnc) || !mPlmn.equals(mccmnc)) {
                this.mMtkRil.riljLog("consider ts.25 only for other results");
                MtkRIL mtkRIL5 = this.mMtkRil;
                networkInfos.get(i).alphaLong = mtkRIL5.lookupOperatorNameMVNO(getSubId(mtkRIL5.mInstanceId.intValue()), mccmnc, true);
                MtkRIL mtkRIL6 = this.mMtkRil;
                networkInfos.get(i).alphaShort = mtkRIL6.lookupOperatorNameMVNO(getSubId(mtkRIL6.mInstanceId.intValue()), mccmnc, false);
            } else {
                this.mMtkRil.riljLog("consider eons for camped plmn");
                MtkRIL mtkRIL7 = this.mMtkRil;
                networkInfos.get(i).alphaLong = mtkRIL7.lookupOperatorName(getSubId(mtkRIL7.mInstanceId.intValue()), mccmnc, true, mLac);
                MtkRIL mtkRIL8 = this.mMtkRil;
                networkInfos.get(i).alphaShort = mtkRIL8.lookupOperatorName(getSubId(mtkRIL8.mInstanceId.intValue()), mccmnc, false, mLac);
            }
        }
        MtkRadioResponse.super.getAvailableNetworksResponse(responseInfo, networkInfos);
    }

    public void getPreferredNetworkTypeBitmapResponse(RadioResponseInfo responseInfo, int halRadioAccessFamilyBitmap) {
        MtkRadioResponse.super.getPreferredNetworkTypeResponse(responseInfo, MtkRadioAccessFamily.getNetworkTypeFromRaf(RIL.convertToNetworkTypeBitMask(halRadioAccessFamilyBitmap)));
    }

    public void getCellInfoListResponse_1_4(RadioResponseInfo responseInfo, ArrayList<CellInfo> cellInfo) {
        responseMtkCellInfoList_1_4(responseInfo, cellInfo);
    }

    private void responseMtkCellInfoList_1_4(RadioResponseInfo responseInfo, ArrayList<CellInfo> cellInfo) {
        RILRequest rr = this.mRil.processResponse(responseInfo);
        if (rr != null) {
            ArrayList<android.telephony.CellInfo> ret = this.mMtkRil.mtkConvertHalCellInfoList_1_4(cellInfo);
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDone(rr, responseInfo, ret);
        }
    }
}
