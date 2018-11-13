package com.qualcomm.qti.internal.telephony;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.SubscriptionController;
import java.util.ArrayList;
import vendor.qti.hardware.radio.qtiradio.V1_0.IQtiRadioIndication.Stub;
import vendor.qti.hardware.radio.qtiradio.V1_0.Oem_key_log_err_msg_type;

public class QtiRadioIndication extends Stub {
    static final String QTI_RILJ_LOG_TAG = "QtiRadioIndication";
    private static final int SYS_OEM_NW_DIAG_CAUSE_CALL_BASE = 10;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MCFG_CONFIG_CHANGE = 76;
    private static final int SYS_OEM_NW_DIAG_CAUSE_REG_BASE = 60;
    private static final int SYS_OEM_NW_DIAG_CAUSE_REG_MCC_CHANGE = 69;
    QtiRIL mRil;

    public QtiRadioIndication(QtiRIL ril) {
        this.mRil = ril;
    }

    public void qtiRadioIndication(int type) {
    }

    public void setMccProperties(int stackId, String mcc) {
        Rlog.d(QTI_RILJ_LOG_TAG, "setMccProperties: stackId = " + stackId + "  mcc:" + mcc);
        if (SubscriptionController.getInstance().getActiveSubInfoCount(getClass().getPackage().getName()) <= 1) {
            SystemProperties.set("android.telephony.mcc_change", mcc);
            SystemProperties.set("android.telephony.mcc_change2", mcc);
        } else if (stackId == 0) {
            SystemProperties.set("android.telephony.mcc_change", mcc);
        } else {
            SystemProperties.set("android.telephony.mcc_change2", mcc);
        }
    }

    public void oemKeyLogErrInd(int indType, Oem_key_log_err_msg_type errMsg) {
        String msg = null;
        int type = errMsg.type;
        int rat = errMsg.rat;
        int errcode = errMsg.errcode;
        int is_message = errMsg.is_message;
        if (1 == is_message) {
            msg = errMsg.msg;
        }
        Rlog.d(QTI_RILJ_LOG_TAG, "Get message, type:" + type + ", rat:" + rat + ", errcode:" + errcode);
        String ACTION_MCC_CHANGE = "android.telephony.action.mcc_change";
        String ACTION_MCFG_CONFIG_CHANGE = "android.telephony.action.mcfg_change";
        Intent intent;
        if (type == SYS_OEM_NW_DIAG_CAUSE_REG_MCC_CHANGE) {
            intent = new Intent(ACTION_MCC_CHANGE);
            intent.putExtra("slotid", String.valueOf(rat));
            intent.putExtra("mcc", String.valueOf(errcode));
            setMccProperties(rat, String.valueOf(errcode));
            Rlog.d(QTI_RILJ_LOG_TAG, "ACTION_MCC_CHANGE, slotid:" + rat + ", mcc:" + errcode);
            this.mRil.mQtiContext.sendBroadcast(intent);
        } else if (type == SYS_OEM_NW_DIAG_CAUSE_MCFG_CONFIG_CHANGE) {
            intent = new Intent(ACTION_MCFG_CONFIG_CHANGE);
            Rlog.d(QTI_RILJ_LOG_TAG, "yipeng.sun send ACTION_MCFG_CONFIG_CHANGE");
            this.mRil.mQtiContext.sendBroadcast(intent);
        } else {
            Bundle b = new Bundle();
            b.putInt("type", type);
            b.putInt("rat", rat);
            b.putInt("errcode", errcode);
            b.putInt("is_message", is_message);
            if (1 == is_message) {
                b.putString("message", msg);
            }
            this.mRil.mKeylogHandler.obtainMessage(1, b).sendToTarget();
        }
    }

    public void nvBackupStatusInd(int type, String result) {
        if (result != null) {
            String[] split = result.split(";");
            if (split.length > 0) {
                Intent intent = new Intent(split[0]);
                if (split.length > 1) {
                    Rlog.d(QTI_RILJ_LOG_TAG, "oppoProcessNvBackupResponse the result:" + Integer.parseInt(split[1]));
                    intent.putExtra("result", Integer.parseInt(split[1]));
                }
                if (split.length > 2) {
                    Rlog.d(QTI_RILJ_LOG_TAG, "oppoProcessNvBackupResponse the reason:" + Integer.parseInt(split[2]));
                    intent.putExtra("reason", Integer.parseInt(split[2]));
                }
                this.mRil.mQtiContext.sendBroadcast(intent);
            }
        }
    }

    public void oemLteCAInfoInd(int indicationType, ArrayList<Integer> ca_info) {
        Rlog.d(QTI_RILJ_LOG_TAG, "oemLteCAInfoInd");
        int[] result = new int[ca_info.size()];
        for (int i = 0; i < ca_info.size(); i++) {
            result[i] = ((Integer) ca_info.get(i)).intValue();
        }
        this.mRil.notifyLteCARegistrants(result);
    }
}
