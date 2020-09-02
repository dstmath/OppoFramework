package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.gsm.NetworkInfoWithAcT;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import java.util.ArrayList;

public abstract class AbstractSIMRecords extends AbstractBaseRecords {
    public static final int EVENT_GET_ALL_OPL_DONE = 501;
    public static final int EVENT_GET_ALL_PNN_DONE = 500;
    public static final int EVENT_GET_CPHSONS_DONE = 502;
    public static final int EVENT_GET_POL_DONE = 99;
    public static final int EVENT_GET_SHORT_CPHSONS_DONE = 503;
    public static final int EVENT_SET_POL_DONE = 88;
    public static final int SIM_STATUE_LOCK = 1;
    public static final int SIM_STATUE_LOCK_PROCESSED = 2;
    public static final int SIM_STATUE_READY = 3;
    public static final int SIM_STATUE_UNKNOW = 0;
    public String cphsOnsl;
    public String cphsOnss;
    public boolean isNeedSetSpnLater = false;
    public boolean isRecordLoadResponse = false;
    public IccCardApplicationStatus.AppType mApptype;
    public byte[] mEfSST = null;
    public boolean mIsSimLoaded = false;
    public Phone mPhone;
    protected IOppoSIMRecords mReference;
    private volatile int mSimState;
    public String mSpNameInEfSpn = null;
    public Message onCompleteMsg;

    public static class OperatorName {
        public String sFullName;
        public String sShortName;
    }

    public AbstractSIMRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mPhone = PhoneFactory.getPhone(app.getPhoneId());
        this.mApptype = app.getType();
        this.mReference = (IOppoSIMRecords) OppoTelephonyFactory.getInstance().getFeature(IOppoSIMRecords.DEFAULT, this);
    }

    public Context getContext() {
        return this.mContext;
    }

    public IccFileHandler getFh() {
        return this.mFh;
    }

    public UiccCardApplication getParentApp() {
        return this.mParentApp;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public int getMncLength() {
        return this.mMncLength;
    }

    public int getRecordsToLoad() {
        return this.mRecordsToLoad;
    }

    public void setRecordsToLoad(int recordsToLoad) {
        this.mRecordsToLoad = recordsToLoad;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void setServiceProviderName(String spn) {
        super.setServiceProviderName(spn);
    }

    public void oppoProcessChangeRegion(Context context, int slotId) {
        this.mReference.oppoProcessChangeRegion(context, slotId);
    }

    public String getEonsIfExist(String plmn, int nLac, boolean bLongNameRequired) {
        return this.mReference.getEonsIfExist(plmn, nLac, bLongNameRequired);
    }

    public void fetchCdmaPrl() {
        this.mReference.fetchCdmaPrl();
    }

    @Override // com.android.internal.telephony.uicc.AbstractBaseRecords
    public void getPreferedOperatorList(Message onComplete) {
        this.mReference.getPreferedOperatorList(onComplete);
    }

    @Override // com.android.internal.telephony.uicc.AbstractBaseRecords
    public void setPOLEntry(NetworkInfoWithAcT networkWithAct, Message onComplete) {
        this.mReference.setPOLEntry(networkWithAct, onComplete);
    }

    public String getPrlVersion() {
        return this.mReference.getPrlVersion();
    }

    public void oppoSetSimSpn(String spn) {
        this.mReference.oppoSetSimSpn(spn);
    }

    @Override // com.android.internal.telephony.uicc.AbstractBaseRecords
    public String getSIMCPHSOns() {
        return this.mReference.getSIMCPHSOns();
    }

    @Override // com.android.internal.telephony.uicc.IccRecords, com.android.internal.telephony.uicc.AbstractBaseRecords
    public String getSpNameInEfSpn() {
        return this.mReference.getSpNameInEfSpn();
    }

    public String getFirstFullNameInEfPnn() {
        return this.mReference.getFirstFullNameInEfPnn();
    }

    public boolean isSimLoadedCompleted() {
        return this.mIsSimLoaded;
    }

    public OperatorName getEFpnnNetworkNames(int index) {
        return this.mReference.getEFpnnNetworkNames(index);
    }

    public void setSimState(int state) {
        this.mSimState = state;
    }

    public int getSimState() {
        log("getSimState:" + this.mSimState);
        return this.mSimState;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void handleMessage(Message msg) {
        IOppoSIMRecords iOppoSIMRecords;
        int i = msg.what;
        if (i == 88) {
            log("wjp_pol EVENT_SET_POL_DONE");
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception != null) {
                loge("Exception in EVENT_SET_POL_DONE EF POL data " + ar.exception);
            }
            if (ar.userObj != null) {
                AsyncResult.forMessage((Message) ar.userObj).exception = ar.exception;
                ((Message) ar.userObj).sendToTarget();
            }
        } else if (i != 99) {
            switch (i) {
                case EVENT_GET_ALL_PNN_DONE /*{ENCODED_INT: 500}*/:
                    this.isRecordLoadResponse = true;
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2.exception != null) {
                        log("EVENT_GET_ALL_PNN_DONE exception = " + ar2.exception);
                        return;
                    }
                    IOppoSIMRecords iOppoSIMRecords2 = this.mReference;
                    if (iOppoSIMRecords2 != null) {
                        iOppoSIMRecords2.parseEFpnn((ArrayList) ar2.result);
                        return;
                    }
                    return;
                case EVENT_GET_ALL_OPL_DONE /*{ENCODED_INT: 501}*/:
                    this.isRecordLoadResponse = true;
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    if (ar3 != null && ar3.exception == null && (iOppoSIMRecords = this.mReference) != null) {
                        iOppoSIMRecords.parseEFopl((ArrayList) ar3.result);
                        return;
                    }
                    return;
                case EVENT_GET_CPHSONS_DONE /*{ENCODED_INT: 502}*/:
                    log("handleMessage (EVENT_GET_CPHSONS_DONE)");
                    this.isRecordLoadResponse = true;
                    AsyncResult ar4 = (AsyncResult) msg.obj;
                    if (ar4 != null && ar4.exception == null) {
                        byte[] data = (byte[]) ar4.result;
                        this.cphsOnsl = IccUtils.adnStringFieldToString(data, 0, data.length);
                        log("Load EF_SPN_CPHS: " + this.cphsOnsl);
                        return;
                    }
                    return;
                case EVENT_GET_SHORT_CPHSONS_DONE /*{ENCODED_INT: 503}*/:
                    log("handleMessage (EVENT_GET_SHORT_CPHSONS_DONE)");
                    this.isRecordLoadResponse = true;
                    AsyncResult ar5 = (AsyncResult) msg.obj;
                    if (ar5 != null && ar5.exception == null) {
                        byte[] data2 = (byte[]) ar5.result;
                        this.cphsOnss = IccUtils.adnStringFieldToString(data2, 0, data2.length);
                        log("Load EF_SPN_SHORT_CPHS: " + this.cphsOnss);
                        return;
                    }
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        } else {
            log("EVENT_GET_POL_DONE fileid:" + msg.arg1);
            AsyncResult ar6 = (AsyncResult) msg.obj;
            if (ar6.exception != null) {
                loge("Exception in fetching EF POL data " + ar6.exception);
                if (msg.arg1 != 28464) {
                    this.mFh.loadEFTransparent(28464, obtainMessage(99, 28464, 0, this.onCompleteMsg));
                    return;
                }
                Message response = (Message) ar6.userObj;
                byte[] data3 = (byte[]) ar6.result;
                IOppoSIMRecords iOppoSIMRecords3 = this.mReference;
                if (iOppoSIMRecords3 != null) {
                    iOppoSIMRecords3.handlePlmnListData(response, data3, ar6.exception);
                    return;
                }
                return;
            }
            byte[] data4 = (byte[]) ar6.result;
            log("EVENT_GET_POL_DONE data " + IccUtils.bytesToHexString(data4));
            if (ar6.userObj != null) {
                AsyncResult.forMessage((Message) ar6.userObj).exception = ar6.exception;
                Message msgtarget = (Message) ar6.userObj;
                IOppoSIMRecords iOppoSIMRecords4 = this.mReference;
                if (iOppoSIMRecords4 != null) {
                    iOppoSIMRecords4.handleEfPOLResponse(msg.arg1, data4, msgtarget);
                }
            }
        }
    }

    public void processImsiReadComplete(String imsi) {
        this.mReference.processImsiReadComplete(imsi);
    }

    @Override // com.android.internal.telephony.uicc.IccRecords, com.android.internal.telephony.uicc.AbstractBaseRecords
    public void dispose() {
        super.dispose();
        this.mReference.dispose();
    }

    public void setSpnFromConfig(String carrier) {
        this.mReference.setSpnFromConfig(carrier);
    }
}
