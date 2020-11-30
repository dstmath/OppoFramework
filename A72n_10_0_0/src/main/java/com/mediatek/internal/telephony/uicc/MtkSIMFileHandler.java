package com.mediatek.internal.telephony.uicc;

import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.SIMFileHandler;
import com.android.internal.telephony.uicc.UiccCardApplication;

public final class MtkSIMFileHandler extends SIMFileHandler {
    static final String LOG_TAG_EX = "MtkSIMFH";
    MtkIccFileHandler mMtkIccFh = null;

    public MtkSIMFileHandler(UiccCardApplication app, String aid, CommandsInterface ci) {
        super(app, aid, ci);
        this.mMtkIccFh = new MtkIccFileHandler(app, aid, ci);
    }

    /* access modifiers changed from: protected */
    public String getEFPath(int efid) {
        if (efid == 20278) {
            return "7FFF7F665F30";
        }
        if (efid == 28450) {
            return "3F007F25";
        }
        if (efid == 28482) {
            return "3F007F10";
        }
        if (efid == 28539 || efid == 28599 || efid == 28614) {
            return "3F007F20";
        }
        Rlog.d(LOG_TAG_EX, "SIM aosp default getEFPath.");
        return MtkSIMFileHandler.super.getEFPath(efid);
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
}
