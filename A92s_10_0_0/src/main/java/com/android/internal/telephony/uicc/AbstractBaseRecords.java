package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IOppoUiccManager;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.gsm.NetworkInfoWithAcT;
import com.android.internal.telephony.util.OemTelephonyUtils;

public abstract class AbstractBaseRecords extends IccRecords {
    public static final String WHITE_SIM_CARD_INSERT_PROC = "persist.radio.oppo.white_sim_card_insert";
    public boolean mIsTestCard = false;

    public AbstractBaseRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void dispose() {
        super.dispose();
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void seticcId(String striccId) {
        this.mIccId = striccId;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void setSimOperatorNumericForPhone() {
        String operatorFromIMSI = getOperatorNumeric();
        if (!TextUtils.isEmpty(operatorFromIMSI)) {
            log("IMSI: set 'gsm.sim.operator.numeric' to operator='" + operatorFromIMSI + "'");
            this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), operatorFromIMSI);
        }
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public boolean is_test_card() {
        return this.mIsTestCard || OemTelephonyUtils.isTestCard(this.mImsi);
    }

    /* access modifiers changed from: protected */
    public void setOemSpnFromConfig(String carrier) {
        String spn = getServiceProviderName();
        if (OemTelephonyUtils.isInCnList(this.mContext, spn) || TextUtils.isEmpty(spn) || (spn != null && spn.startsWith("460"))) {
            String operator = ((IOppoUiccManager) OppoTelephonyFactory.getInstance().getFeature(IOppoUiccManager.DEFAULT, new Object[0])).getOemOperator(this.mContext, carrier);
            if (!TextUtils.isEmpty(operator)) {
                setServiceProviderName(operator);
            }
        }
        setSystemProperty("gsm.sim.operator.alpha", getServiceProviderName());
    }

    public String getSIMCPHSOns() {
        return null;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public String getSpNameInEfSpn() {
        return null;
    }

    public String isOperatorMvnoForImsi() {
        return null;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public String isOperatorMvnoForEfPnn() {
        return null;
    }

    public void getPreferedOperatorList(Message onComplete) {
    }

    public void setPOLEntry(NetworkInfoWithAcT networkWithAct, Message onComplete) {
    }

    public void updateNrModeTestCard() {
        Phone mPhone = PhoneFactory.getPhone(this.mParentApp.getPhoneId());
        if (mPhone != null && is_test_card()) {
            SystemProperties.set(WHITE_SIM_CARD_INSERT_PROC, String.valueOf(1));
            mPhone.invokeOemRilRequestStrings(new String[]{"AT+E5GOPT=" + "7", PhoneConfigurationManager.SSSS}, null);
            log("oppo set NSA&SA mode for test card");
        }
    }
}
