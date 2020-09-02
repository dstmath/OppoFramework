package com.mediatek.internal.telephony.uicc;

/* compiled from: IccFileFetcherBase */
class IccFileRequest {
    public int mAppType;
    public byte[] mData;
    public String mEfPath;
    public int mEfType;
    public int mEfid;
    public String mKey = null;
    public String mPin2;
    public int mRecordNum;

    public IccFileRequest(int efid, int eftype, int apptype, String path, byte[] data, int recordnum, String pin2) {
        this.mEfid = efid;
        this.mEfType = eftype;
        this.mAppType = apptype;
        this.mEfPath = path;
        this.mData = data;
        this.mRecordNum = recordnum;
        this.mPin2 = pin2;
    }
}
