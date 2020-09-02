package com.mediatek.internal.telephony.uicc;

import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.CsimFileHandler;
import com.android.internal.telephony.uicc.UiccCardApplication;

public final class MtkCsimFileHandler extends CsimFileHandler implements MtkIccConstants {
    static final String LOG_TAG = "MtkCsimFH";
    MtkIccFileHandler mMtkIccFh = null;

    public MtkCsimFileHandler(UiccCardApplication app, String aid, CommandsInterface ci) {
        super(app, aid, ci);
        this.mMtkIccFh = new MtkIccFileHandler(app, aid, ci);
    }

    /* access modifiers changed from: protected */
    public String getEFPath(int efid) {
        logd("GetEFPath : " + efid);
        if (efid != 28533) {
            return MtkCsimFileHandler.super.getEFPath(efid);
        }
        return "3F007FFF";
    }

    /* access modifiers changed from: protected */
    public String getCommonIccEFPath(int efid) {
        logd("getCommonIccEFPath : " + efid);
        if (efid != 28645) {
            return MtkCsimFileHandler.super.getCommonIccEFPath(efid);
        }
        return null;
    }

    public void loadEFLinearFixedAll(int fileid, Message onLoaded, boolean is7FFF) {
        this.mMtkIccFh.loadEFLinearFixedAllByPath(fileid, onLoaded, is7FFF);
    }

    public void loadEFLinearFixedAll(int fileid, int mode, Message onLoaded) {
        this.mMtkIccFh.loadEFLinearFixedAllByMode(fileid, mode, onLoaded);
    }

    public void loadEFTransparent(int fileid, String path, Message onLoaded) {
        this.mMtkIccFh.loadEFTransparent(fileid, path, onLoaded);
    }

    public void updateEFTransparent(int fileid, String path, byte[] data, Message onComplete) {
        this.mMtkIccFh.updateEFTransparent(fileid, path, data, onComplete);
    }

    public void readEFLinearFixed(int fileid, int recordNum, int recordSize, Message onLoaded) {
        this.mMtkIccFh.readEFLinearFixed(fileid, recordNum, recordSize, onLoaded);
    }

    public void selectEFFile(int fileid, Message onLoaded) {
        this.mMtkIccFh.selectEFFile(fileid, onLoaded);
    }

    /* access modifiers changed from: protected */
    public void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
