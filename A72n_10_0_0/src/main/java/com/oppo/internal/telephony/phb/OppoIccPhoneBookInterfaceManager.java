package com.oppo.internal.telephony.phb;

import android.app.ActivityManagerNative;
import android.content.Intent;
import android.telephony.Rlog;
import com.android.internal.telephony.AbstractIccPhoneBookInterfaceManager;
import com.android.internal.telephony.IOppoIccPhoneBookInterfaceManager;
import com.android.internal.telephony.IccPhoneBookInterfaceManager;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.util.OemTelephonyUtils;

public class OppoIccPhoneBookInterfaceManager implements IOppoIccPhoneBookInterfaceManager {
    private String LOG_TAG = "OppoIccPBIM";
    private IccPhoneBookInterfaceManager mIccPhoneBookInterfaceManager;
    protected int simNameLeng;
    private int simTotal;
    private int simUsed;
    protected int simrecord_efid;

    public OppoIccPhoneBookInterfaceManager(IccPhoneBookInterfaceManager IccPhoneBookInterfaceManager) {
        this.mIccPhoneBookInterfaceManager = IccPhoneBookInterfaceManager;
        this.LOG_TAG += "/" + ((AbstractIccPhoneBookInterfaceManager) OemTelephonyUtils.typeCasting(AbstractIccPhoneBookInterfaceManager.class, this.mIccPhoneBookInterfaceManager)).getSlotId();
    }

    public int oppoGetAdnEmailLen() {
        return 30;
    }

    public int oppoGetSimPhonebookAllSpace() {
        AbstractIccPhoneBookInterfaceManager tmpPbManager = (AbstractIccPhoneBookInterfaceManager) OemTelephonyUtils.typeCasting(AbstractIccPhoneBookInterfaceManager.class, this.mIccPhoneBookInterfaceManager);
        if (!tmpPbManager.phonebookReady) {
            logd("oppoGetSimPhonebookAllSpace: phonebook not ready");
            return -1;
        }
        logd("IccPhoneBookInterfaceManager: oppoGetSimPhonebookAllSpace: simrecord_efid:" + this.simrecord_efid);
        if (tmpPbManager.getPhone() == null) {
            return -1;
        }
        int i = this.simrecord_efid;
        if (!(i == 28474 || i == 20272)) {
            if (tmpPbManager.getPhone().getCurrentUiccAppType() == IccCardApplicationStatus.AppType.APPTYPE_USIM) {
                this.simrecord_efid = 20272;
            } else {
                this.simrecord_efid = 28474;
            }
            this.mIccPhoneBookInterfaceManager.getAdnRecordsInEf(this.simrecord_efid);
        }
        this.simTotal = tmpPbManager.getRecordsSize();
        logd("oppoGetSimPhonebookAllSpace:" + this.simTotal);
        return this.simTotal;
    }

    public int oppoGetSimPhonebookUsedSpace() {
        logd("oppoGetSimPhonebookUsedSpace");
        AbstractIccPhoneBookInterfaceManager tmpPbManager = (AbstractIccPhoneBookInterfaceManager) OemTelephonyUtils.typeCasting(AbstractIccPhoneBookInterfaceManager.class, this.mIccPhoneBookInterfaceManager);
        if (!tmpPbManager.phonebookReady) {
            logd("oppoGetSimPhonebookUsedSpace: phonebook not ready");
            return -1;
        }
        logd("IccPhoneBookInterfaceManager: oppoGetSimPhonebookUsedSpace: simrecord_efid:" + this.simrecord_efid);
        if (tmpPbManager.getPhone() == null) {
            return -1;
        }
        int i = this.simrecord_efid;
        if (!(i == 28474 || i == 20272)) {
            if (tmpPbManager.getPhone().getCurrentUiccAppType() == IccCardApplicationStatus.AppType.APPTYPE_USIM) {
                this.simrecord_efid = 20272;
            } else {
                this.simrecord_efid = 28474;
            }
            this.mIccPhoneBookInterfaceManager.getAdnRecordsInEf(this.simrecord_efid);
        }
        this.simUsed = 0;
        int N = tmpPbManager.getRecordsSize();
        for (int i2 = 0; i2 < N; i2++) {
            if (!tmpPbManager.isEmptyRecords(i2)) {
                this.simUsed++;
            }
        }
        logd("oppoGetSimPhonebookUsedSpace:" + this.simUsed);
        return this.simUsed;
    }

    public int oppoGetSimPhonebookNameLength() {
        AbstractIccPhoneBookInterfaceManager tmpPbManager = (AbstractIccPhoneBookInterfaceManager) OemTelephonyUtils.typeCasting(AbstractIccPhoneBookInterfaceManager.class, this.mIccPhoneBookInterfaceManager);
        if (!tmpPbManager.phonebookReady || tmpPbManager.getPhone() == null) {
            return -1;
        }
        if (tmpPbManager.getPhone().getContext().getPackageManager().hasSystemFeature("oppo.ct.optr")) {
            this.simNameLeng = 14;
            return this.simNameLeng;
        }
        if (this.simNameLeng <= 0) {
            tmpPbManager.oppoCheckThread();
            int[] recordSize = this.mIccPhoneBookInterfaceManager.getAdnRecordsSize(28474);
            if (recordSize != null && recordSize.length == 3) {
                this.simNameLeng = recordSize[0] - 14;
            }
        }
        if (OemConstant.EXP_VERSION) {
            int i = this.simNameLeng;
            if (i <= 0) {
                i = 10;
            }
            this.simNameLeng = i;
        } else {
            int i2 = this.simNameLeng;
            if (i2 <= 0) {
                i2 = 14;
            }
            this.simNameLeng = i2;
        }
        logd("oppoGetSimPhonebookNameLength  v1 simNameLeng:" + this.simNameLeng);
        return this.simNameLeng;
    }

    public boolean isPhoneBookReady() {
        return ((AbstractIccPhoneBookInterfaceManager) OemTelephonyUtils.typeCasting(AbstractIccPhoneBookInterfaceManager.class, this.mIccPhoneBookInterfaceManager)).phonebookReady;
    }

    public void broadcastIccPhoneBookReadyIntent(String value, String reason) {
        Intent intent = new Intent("android.intent.action.PBM_STATE_READY");
        intent.putExtra("pbstate", value);
        logd("Broadcasting intent ACTION_PBM_STATE_READY" + value + " reason " + reason);
        ActivityManagerNative.broadcastStickyIntent(intent, (String) null, -1);
    }

    public void resetSimNameLength() {
        this.simNameLeng = -1;
    }

    /* access modifiers changed from: protected */
    public void logd(String msg) {
        String str = this.LOG_TAG;
        Rlog.d(str, "[OppoIccPbInterfaceManager] " + msg);
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        String str = this.LOG_TAG;
        Rlog.e(str, "[OppoIccPbInterfaceManager] " + msg);
    }

    /* access modifiers changed from: protected */
    public void logi(String msg) {
        String str = this.LOG_TAG;
        Rlog.i(str, "[OppoIccPbInterfaceManager] " + msg);
    }
}
