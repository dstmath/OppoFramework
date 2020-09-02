package com.mediatek.internal.telephony.uicc;

import com.mediatek.internal.telephony.ppl.PplMessageManager;

public class EFResponseData {
    private static final int RESPONSE_DATA_FILE_STATUS = 11;
    private int mFileStatus;

    public EFResponseData(byte[] data) {
        this.mFileStatus = data[11] & PplMessageManager.Type.INVALID;
    }

    public int getFileStatus() {
        return this.mFileStatus;
    }
}
