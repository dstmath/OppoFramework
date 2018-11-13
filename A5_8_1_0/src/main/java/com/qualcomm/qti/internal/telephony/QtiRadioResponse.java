package com.qualcomm.qti.internal.telephony;

import android.hardware.radio.V1_0.RadioResponseInfo;
import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.OppoRadioInfo;
import com.android.internal.telephony.OppoRxChainInfo;
import com.android.internal.telephony.OppoTxInfo;
import com.android.internal.telephony.OppoTxRxInfo;
import java.util.ArrayList;
import vendor.qti.hardware.radio.qtiradio.V1_0.IQtiRadioResponse.Stub;
import vendor.qti.hardware.radio.qtiradio.V1_0.OPPO_RIL_Radio_info;
import vendor.qti.hardware.radio.qtiradio.V1_0.OPPO_RIL_Tx_Rx_info;
import vendor.qti.hardware.radio.qtiradio.V1_0.QtiRadioResponseInfo;

public class QtiRadioResponse extends Stub {
    static final String QTI_RILJ_LOG_TAG = "QtiRadioResponse";
    QtiRIL mRil;

    public QtiRadioResponse(QtiRIL ril) {
        this.mRil = ril;
    }

    static void sendMessageResponse(Message msg, Object ret) {
        if (msg != null) {
            AsyncResult.forMessage(msg, ret, null);
            msg.sendToTarget();
        }
    }

    static RadioResponseInfo toRadioResponseInfo(QtiRadioResponseInfo qtiResponseInfo) {
        RadioResponseInfo responseInfo = new RadioResponseInfo();
        responseInfo.type = qtiResponseInfo.type;
        responseInfo.serial = qtiResponseInfo.serial;
        responseInfo.error = qtiResponseInfo.error;
        return responseInfo;
    }

    private void responseString(RadioResponseInfo responseInfo, String str) {
        Object request = this.mRil.qtiProcessResponse(responseInfo);
        Message result = this.mRil.qtiGetMessageFromRequest(request);
        if (result != null) {
            if (responseInfo.error == 0) {
                sendMessageResponse(result, str);
            }
            this.mRil.qtiProcessResponseDone(request, responseInfo, str);
        }
    }

    public void getAtrResponse(QtiRadioResponseInfo qtiResponseInfo, String atr) {
        Rlog.d(QTI_RILJ_LOG_TAG, "getAtrResponse");
        responseString(toRadioResponseInfo(qtiResponseInfo), atr);
    }

    private void responseTxRxInfo(RadioResponseInfo responseInfo, OPPO_RIL_Tx_Rx_info ret) {
        Object request = this.mRil.qtiProcessResponse(responseInfo);
        Message result = this.mRil.qtiGetMessageFromRequest(request);
        OppoRxChainInfo rxChain0 = new OppoRxChainInfo(ret.rx_chain_0.is_radio_turned, ret.rx_chain_0.rx_pwr, ret.rx_chain_0.ecio, ret.rx_chain_0.rscp, ret.rx_chain_0.rsrp, ret.rx_chain_0.phase);
        OppoRxChainInfo rxChain1 = new OppoRxChainInfo(ret.rx_chain_1.is_radio_turned, ret.rx_chain_1.rx_pwr, ret.rx_chain_1.ecio, ret.rx_chain_1.rscp, ret.rx_chain_1.rsrp, ret.rx_chain_1.phase);
        OppoRxChainInfo rxChain2 = new OppoRxChainInfo(ret.rx_chain_2.is_radio_turned, ret.rx_chain_2.rx_pwr, ret.rx_chain_2.ecio, ret.rx_chain_2.rscp, ret.rx_chain_2.rsrp, ret.rx_chain_2.phase);
        OppoRxChainInfo rxChain3 = new OppoRxChainInfo(ret.rx_chain_3.is_radio_turned, ret.rx_chain_3.rx_pwr, ret.rx_chain_3.ecio, ret.rx_chain_3.rscp, ret.rx_chain_3.rsrp, ret.rx_chain_3.phase);
        OppoRxChainInfo oppoRxChainInfo = rxChain0;
        OppoRxChainInfo oppoRxChainInfo2 = rxChain1;
        OppoRxChainInfo oppoRxChainInfo3 = rxChain2;
        OppoRxChainInfo oppoRxChainInfo4 = rxChain3;
        Object info = new OppoTxRxInfo(ret.rx_chain_0_valid, oppoRxChainInfo, ret.rx_chain_1_valid, oppoRxChainInfo2, ret.rx_chain_2_valid, oppoRxChainInfo3, ret.rx_chain_3_valid, oppoRxChainInfo4, ret.tx_valid, new OppoTxInfo(ret.tx.is_in_traffic, ret.tx.tx_pwr));
        if (result != null && responseInfo.error == 0) {
            sendMessageResponse(result, info);
        }
        if (request != null) {
            this.mRil.qtiProcessResponseDone(request, responseInfo, info);
        }
    }

    private void responseRadioInfo(RadioResponseInfo responseInfo, OPPO_RIL_Radio_info ret) {
        Object request = this.mRil.qtiProcessResponse(responseInfo);
        Message result = this.mRil.qtiGetMessageFromRequest(request);
        Object info = new OppoRadioInfo(ret.rat, ret.mcc, ret.mnc, ret.lac, ret.cellid, ret.arfcn, ret.band, ret.rssi, ret.sinr, ret.rrstatus, ret.tx_power, new ArrayList());
        if (result != null && responseInfo.error == 0) {
            sendMessageResponse(result, info);
        }
        if (request != null) {
            this.mRil.qtiProcessResponseDone(request, responseInfo, info);
        }
    }

    private void responseVoid(RadioResponseInfo responseInfo) {
        Object request = this.mRil.qtiProcessResponse(responseInfo);
        Message result = this.mRil.qtiGetMessageFromRequest(request);
        if (result != null && responseInfo.error == 0) {
            sendMessageResponse(result, null);
        }
        if (request != null) {
            this.mRil.qtiProcessResponseDone(request, responseInfo, null);
        }
    }

    private void responseInts(RadioResponseInfo responseInfo, int... var) {
        ArrayList<Integer> ints = new ArrayList();
        for (int valueOf : var) {
            ints.add(Integer.valueOf(valueOf));
        }
        responseIntArrayList(responseInfo, ints);
    }

    private void responseIntArrayList(RadioResponseInfo responseInfo, ArrayList<Integer> var) {
        Object request = this.mRil.qtiProcessResponse(responseInfo);
        Message result = this.mRil.qtiGetMessageFromRequest(request);
        Object ret = new int[var.size()];
        if (result != null) {
            for (int i = 0; i < var.size(); i++) {
                ret[i] = ((Integer) var.get(i)).intValue();
            }
            if (responseInfo.error == 0) {
                sendMessageResponse(result, ret);
            }
        }
        if (request != null) {
            this.mRil.qtiProcessResponseDone(request, responseInfo, ret);
        }
    }

    public void reserveNullResponse(QtiRadioResponseInfo info) {
        responseVoid(toRadioResponseInfo(info));
    }

    public void processFactoryModeNVResponse(QtiRadioResponseInfo info) {
        responseVoid(toRadioResponseInfo(info));
    }

    public void setFactoryModeGPIOResponse(QtiRadioResponseInfo info, int result) {
        responseInts(toRadioResponseInfo(info), result);
    }

    public void getBandModeResponse(QtiRadioResponseInfo info, int band) {
    }

    public void getRadioInfoResponse(QtiRadioResponseInfo info, OPPO_RIL_Radio_info respInfo) {
        responseRadioInfo(toRadioResponseInfo(info), respInfo);
    }

    public void reportNvRestoreResponse(QtiRadioResponseInfo info) {
    }

    public void getRffeDevInfoResponse(QtiRadioResponseInfo info, int result) {
        responseInts(toRadioResponseInfo(info), result);
    }

    public void setModemErrorFatalResponse(QtiRadioResponseInfo info) {
        responseVoid(toRadioResponseInfo(info));
    }

    public void getMdmBaseBandResponse(QtiRadioResponseInfo info, String baseband) {
        responseString(toRadioResponseInfo(info), baseband);
    }

    public void setTddLTEResponse(QtiRadioResponseInfo info) {
    }

    public void setFilterArfcnResponse(QtiRadioResponseInfo info, int result) {
        responseInts(toRadioResponseInfo(info), result);
    }

    public void setPplmnListResponse(QtiRadioResponseInfo info) {
        responseVoid(toRadioResponseInfo(info));
    }

    public void getTxRxInfoResponse(QtiRadioResponseInfo info, OPPO_RIL_Tx_Rx_info result) {
        responseTxRxInfo(toRadioResponseInfo(info), result);
    }

    public void getRegionChangedForEccListResponse(QtiRadioResponseInfo info) {
        responseVoid(toRadioResponseInfo(info));
    }

    public void setFakesBsWeightResponse(QtiRadioResponseInfo info, int result) {
        responseInts(toRadioResponseInfo(info), result);
    }

    public void setVolteFr2Response(QtiRadioResponseInfo info, int result) {
        responseInts(toRadioResponseInfo(info), result);
    }

    public void setVolteFr1Response(QtiRadioResponseInfo info, int result) {
        responseInts(toRadioResponseInfo(info), result);
    }

    public void lockGsmArfcnResponse(QtiRadioResponseInfo info, int result) {
        responseInts(toRadioResponseInfo(info), result);
    }

    public void getRffeCmdResponse(QtiRadioResponseInfo info, int result) {
        responseInts(toRadioResponseInfo(info), result);
    }

    public void lockLteCellResponse(QtiRadioResponseInfo info, int result) {
        responseInts(toRadioResponseInfo(info), result);
    }

    public void controlModemFeatureResponse(QtiRadioResponseInfo info, int result) {
        responseInts(toRadioResponseInfo(info), result);
    }

    public void getASDIVStateResponse(QtiRadioResponseInfo info, String result) {
        responseString(toRadioResponseInfo(info), result);
    }
}
