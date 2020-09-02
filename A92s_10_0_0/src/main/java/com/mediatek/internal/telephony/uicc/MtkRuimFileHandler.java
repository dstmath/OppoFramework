package com.mediatek.internal.telephony.uicc;

import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.RuimFileHandler;
import com.android.internal.telephony.uicc.UiccCardApplication;

public final class MtkRuimFileHandler extends RuimFileHandler implements MtkIccConstants {
    static final String LOG_TAG = "MtkRuimFH";
    MtkIccFileHandler mMtkIccFh = null;

    public MtkRuimFileHandler(UiccCardApplication app, String aid, CommandsInterface ci) {
        super(app, aid, ci);
        this.mMtkIccFh = new MtkIccFileHandler(app, aid, ci);
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
        Rlog.d(LOG_TAG, "[RuimFileHandler] " + msg);
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        Rlog.e(LOG_TAG, "[RuimFileHandler] " + msg);
    }
}
