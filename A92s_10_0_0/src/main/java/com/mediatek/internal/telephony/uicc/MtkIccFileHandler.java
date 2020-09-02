package com.mediatek.internal.telephony.uicc;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccFileTypeMismatch;
import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import java.util.ArrayList;

public final class MtkIccFileHandler extends IccFileHandler {
    protected static final int EVENT_GET_BINARY_SIZE_DONE_EX = 101;
    protected static final int EVENT_GET_RECORD_SIZE_DONE_EX = 102;
    protected static final int EVENT_READ_RECORD_DONE_EX = 103;
    protected static final int EVENT_SELECT_EF_FILE = 100;
    static final String LOG_TAG = "MtkIccFileHandler";

    public MtkIccFileHandler(UiccCardApplication app, String aid, CommandsInterface ci) {
        super(app, aid, ci);
        logd("SelectFileHandlerEx constructor");
    }

    static class MtkLoadLinearFixedContext {
        int mCountRecords;
        int mEfid;
        boolean mLoadAll = true;
        int mMode;
        Message mOnLoaded;
        String mPath;
        int mRecordNum = 1;
        int mRecordSize;
        ArrayList<byte[]> results;

        MtkLoadLinearFixedContext(int efid, Message onLoaded) {
            this.mEfid = efid;
            this.mOnLoaded = onLoaded;
            this.mPath = null;
            this.mMode = -1;
        }
    }

    static class MtkLoadTransparentContext {
        int mEfid;
        Message mOnLoaded;
        String mPath;

        MtkLoadTransparentContext(int efid, String path, Message onLoaded) {
            this.mEfid = efid;
            this.mPath = path;
            this.mOnLoaded = onLoaded;
        }
    }

    public void handleMessage(Message msg) {
        try {
            switch (msg.what) {
                case 100:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    Message response = (Message) ar.userObj;
                    IccIoResult result = (IccIoResult) ar.result;
                    if (processException(response, (AsyncResult) msg.obj)) {
                        loge("EVENT_SELECT_EF_FILE exception");
                        return;
                    }
                    byte[] data = result.payload;
                    if (4 == data[6]) {
                        sendResult(response, new EFResponseData(data), null);
                        return;
                    }
                    throw new IccFileTypeMismatch();
                case 101:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    MtkLoadTransparentContext tc = (MtkLoadTransparentContext) ar2.userObj;
                    IccIoResult result2 = (IccIoResult) ar2.result;
                    Message response2 = tc.mOnLoaded;
                    String path = tc.mPath;
                    if (!processException(response2, (AsyncResult) msg.obj)) {
                        byte[] data2 = result2.payload;
                        if (4 != data2[6]) {
                            throw new IccFileTypeMismatch();
                        } else if (data2[13] == 0) {
                            int size = (data2[3] & 255) + ((data2[2] & 255) << 8);
                            if (path == null) {
                                path = getEFPath(tc.mEfid);
                            }
                            this.mCi.iccIOForApp(176, tc.mEfid, path, 0, 0, size, (String) null, (String) null, this.mAid, obtainMessage(5, tc.mEfid, 0, response2));
                            return;
                        } else {
                            throw new IccFileTypeMismatch();
                        }
                    } else {
                        return;
                    }
                case 102:
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    MtkLoadLinearFixedContext lc = (MtkLoadLinearFixedContext) ar3.userObj;
                    IccIoResult result3 = (IccIoResult) ar3.result;
                    if (!processException(lc.mOnLoaded, (AsyncResult) msg.obj)) {
                        byte[] data3 = result3.payload;
                        String path2 = lc.mPath;
                        if (4 != data3[6]) {
                            throw new IccFileTypeMismatch();
                        } else if (1 == data3[13]) {
                            lc.mRecordSize = data3[14] & PplMessageManager.Type.INVALID;
                            lc.mCountRecords = (((data3[2] & 255) << 8) + (data3[3] & 255)) / lc.mRecordSize;
                            if (lc.mLoadAll) {
                                lc.results = new ArrayList<>(lc.mCountRecords);
                            }
                            if (lc.mMode != -1) {
                                this.mCi.iccIOForApp(178, lc.mEfid, getSmsEFPath(lc.mMode), lc.mRecordNum, 4, lc.mRecordSize, (String) null, (String) null, this.mAid, obtainMessage(EVENT_READ_RECORD_DONE_EX, lc));
                                return;
                            }
                            if (path2 == null) {
                                path2 = getEFPath(lc.mEfid);
                            }
                            this.mCi.iccIOForApp(178, lc.mEfid, path2, lc.mRecordNum, 4, lc.mRecordSize, (String) null, (String) null, this.mAid, obtainMessage(EVENT_READ_RECORD_DONE_EX, lc));
                            return;
                        } else {
                            throw new IccFileTypeMismatch();
                        }
                    } else {
                        return;
                    }
                case EVENT_READ_RECORD_DONE_EX /*{ENCODED_INT: 103}*/:
                    AsyncResult ar4 = (AsyncResult) msg.obj;
                    MtkLoadLinearFixedContext lc2 = (MtkLoadLinearFixedContext) ar4.userObj;
                    IccIoResult result4 = (IccIoResult) ar4.result;
                    Message response3 = lc2.mOnLoaded;
                    String path3 = lc2.mPath;
                    if (!processException(response3, (AsyncResult) msg.obj)) {
                        if (!lc2.mLoadAll) {
                            sendResult(response3, result4.payload, null);
                            return;
                        }
                        lc2.results.add(result4.payload);
                        lc2.mRecordNum++;
                        if (lc2.mRecordNum > lc2.mCountRecords) {
                            sendResult(response3, lc2.results, null);
                            return;
                        } else if (lc2.mMode != -1) {
                            this.mCi.iccIOForApp(178, lc2.mEfid, getSmsEFPath(lc2.mMode), lc2.mRecordNum, 4, lc2.mRecordSize, (String) null, (String) null, this.mAid, obtainMessage(EVENT_READ_RECORD_DONE_EX, lc2));
                            return;
                        } else {
                            if (path3 == null) {
                                path3 = getEFPath(lc2.mEfid);
                            }
                            this.mCi.iccIOForApp(178, lc2.mEfid, path3, lc2.mRecordNum, 4, lc2.mRecordSize, (String) null, (String) null, this.mAid, obtainMessage(EVENT_READ_RECORD_DONE_EX, lc2));
                            return;
                        }
                    } else {
                        return;
                    }
                default:
                    MtkIccFileHandler.super.handleMessage(msg);
                    return;
            }
        } catch (Exception exc) {
            if (0 != 0) {
                loge("caught exception:" + exc);
                sendResult(null, null, exc);
                return;
            }
            loge("uncaught exception" + exc);
        }
    }

    public void loadEFLinearFixedAllByPath(int fileid, Message onLoaded, boolean is7FFF) {
        this.mCi.iccIOForApp(192, fileid, getEFPath(fileid), 0, 0, 15, (String) null, (String) null, this.mAid, obtainMessage(102, new MtkLoadLinearFixedContext(fileid, onLoaded)));
    }

    public void loadEFLinearFixedAllByMode(int fileid, int mode, Message onLoaded) {
        MtkLoadLinearFixedContext lc = new MtkLoadLinearFixedContext(fileid, onLoaded);
        lc.mMode = mode;
        this.mCi.iccIOForApp(192, fileid, getSmsEFPath(mode), 0, 0, 15, (String) null, (String) null, this.mAid, obtainMessage(102, lc));
    }

    /* access modifiers changed from: protected */
    public String getSmsEFPath(int mode) {
        if (mode == 1) {
            return "3F007F10";
        }
        if (mode == 2) {
            return "3F007F25";
        }
        return "";
    }

    public void loadEFTransparent(int fileid, String path, Message onLoaded) {
        String efPath = path == null ? getEFPath(fileid) : path;
        this.mCi.iccIOForApp(192, fileid, efPath, 0, 0, 15, (String) null, (String) null, this.mAid, obtainMessage(101, new MtkLoadTransparentContext(fileid, efPath, onLoaded)));
    }

    public void updateEFTransparent(int fileid, String path, byte[] data, Message onComplete) {
        this.mCi.iccIOForApp(214, fileid, path == null ? getEFPath(fileid) : path, 0, 0, data.length, IccUtils.bytesToHexString(data), (String) null, this.mAid, onComplete);
    }

    public void readEFLinearFixed(int fileid, int recordNum, int recordSize, Message onLoaded) {
        this.mCi.iccIOForApp(178, fileid, getEFPath(fileid), recordNum, 4, recordSize, (String) null, (String) null, this.mAid, onLoaded);
    }

    public void selectEFFile(int fileid, Message onLoaded) {
        this.mCi.iccIOForApp(192, fileid, getEFPath(fileid), 0, 0, 15, (String) null, (String) null, this.mAid, obtainMessage(100, fileid, 0, onLoaded));
    }

    /* access modifiers changed from: protected */
    public String getEFPath(int efid) {
        String path = getCommonIccEFPath(efid);
        if (path == null) {
            Rlog.e(LOG_TAG, "Error: EF Path being returned in null");
        }
        return path;
    }

    /* access modifiers changed from: protected */
    public void logd(String msg) {
        Rlog.i(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
