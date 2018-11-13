package com.android.internal.telephony.uicc;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.CommandsInterface;
import com.mediatek.internal.telephony.uicc.EFResponseData;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.util.ArrayList;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public abstract class IccFileHandler extends Handler implements IccConstants {
    protected static final int COMMAND_GET_RESPONSE = 192;
    protected static final int COMMAND_READ_BINARY = 176;
    protected static final int COMMAND_READ_RECORD = 178;
    protected static final int COMMAND_SEEK = 162;
    protected static final int COMMAND_UPDATE_BINARY = 214;
    protected static final int COMMAND_UPDATE_RECORD = 220;
    protected static final int EF_TYPE_CYCLIC = 3;
    protected static final int EF_TYPE_LINEAR_FIXED = 1;
    protected static final int EF_TYPE_TRANSPARENT = 0;
    protected static final int EVENT_GET_BINARY_SIZE_DONE = 4;
    protected static final int EVENT_GET_BINARY_SIZE_DONE_EX = 101;
    protected static final int EVENT_GET_EF_LINEAR_RECORD_SIZE_DONE = 8;
    protected static final int EVENT_GET_RECORD_SIZE_DONE = 6;
    protected static final int EVENT_GET_RECORD_SIZE_IMG_DONE = 11;
    protected static final int EVENT_READ_BINARY_DONE = 5;
    protected static final int EVENT_READ_ICON_DONE = 10;
    protected static final int EVENT_READ_IMG_DONE = 9;
    protected static final int EVENT_READ_RECORD_DONE = 7;
    protected static final int EVENT_SELECT_EF_FILE = 100;
    protected static final int GET_RESPONSE_EF_IMG_SIZE_BYTES = 10;
    protected static final int GET_RESPONSE_EF_SIZE_BYTES = 15;
    protected static final int READ_RECORD_MODE_ABSOLUTE = 4;
    protected static final int RESPONSE_DATA_ACCESS_CONDITION_1 = 8;
    protected static final int RESPONSE_DATA_ACCESS_CONDITION_2 = 9;
    protected static final int RESPONSE_DATA_ACCESS_CONDITION_3 = 10;
    protected static final int RESPONSE_DATA_FILE_ID_1 = 4;
    protected static final int RESPONSE_DATA_FILE_ID_2 = 5;
    protected static final int RESPONSE_DATA_FILE_SIZE_1 = 2;
    protected static final int RESPONSE_DATA_FILE_SIZE_2 = 3;
    protected static final int RESPONSE_DATA_FILE_STATUS = 11;
    protected static final int RESPONSE_DATA_FILE_TYPE = 6;
    protected static final int RESPONSE_DATA_LENGTH = 12;
    protected static final int RESPONSE_DATA_RECORD_LENGTH = 14;
    protected static final int RESPONSE_DATA_RFU_1 = 0;
    protected static final int RESPONSE_DATA_RFU_2 = 1;
    protected static final int RESPONSE_DATA_RFU_3 = 7;
    protected static final int RESPONSE_DATA_STRUCTURE = 13;
    protected static final int TYPE_DF = 2;
    protected static final int TYPE_EF = 4;
    protected static final int TYPE_MF = 1;
    protected static final int TYPE_RFU = 0;
    protected final String mAid;
    protected final CommandsInterface mCi;
    protected final UiccCardApplication mParentApp;

    static class LoadLinearFixedContext {
        int mCountRecords;
        int mEfid;
        boolean mLoadAll;
        int mMode;
        Message mOnLoaded;
        String mPath;
        int mRecordNum;
        int mRecordSize;
        ArrayList<byte[]> results;

        LoadLinearFixedContext(int efid, int recordNum, Message onLoaded) {
            this.mEfid = efid;
            this.mRecordNum = recordNum;
            this.mOnLoaded = onLoaded;
            this.mLoadAll = false;
            this.mPath = null;
            this.mMode = -1;
        }

        LoadLinearFixedContext(int efid, int recordNum, String path, Message onLoaded) {
            this.mEfid = efid;
            this.mRecordNum = recordNum;
            this.mOnLoaded = onLoaded;
            this.mLoadAll = false;
            this.mPath = path;
            this.mMode = -1;
        }

        LoadLinearFixedContext(int efid, String path, Message onLoaded) {
            this.mEfid = efid;
            this.mRecordNum = 1;
            this.mLoadAll = true;
            this.mOnLoaded = onLoaded;
            this.mPath = path;
            this.mMode = -1;
        }

        LoadLinearFixedContext(int efid, Message onLoaded) {
            this.mEfid = efid;
            this.mRecordNum = 1;
            this.mLoadAll = true;
            this.mOnLoaded = onLoaded;
            this.mPath = null;
            this.mMode = -1;
        }
    }

    static class LoadTransparentContext {
        int mEfid;
        Message mOnLoaded;
        String mPath;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.android.internal.telephony.uicc.IccFileHandler.LoadTransparentContext.<init>(int, java.lang.String, android.os.Message):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        LoadTransparentContext(int r1, java.lang.String r2, android.os.Message r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.android.internal.telephony.uicc.IccFileHandler.LoadTransparentContext.<init>(int, java.lang.String, android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.IccFileHandler.LoadTransparentContext.<init>(int, java.lang.String, android.os.Message):void");
        }
    }

    protected abstract String getEFPath(int i);

    protected abstract void logd(String str);

    protected abstract void loge(String str);

    protected IccFileHandler(UiccCardApplication app, String aid, CommandsInterface ci) {
        this.mParentApp = app;
        this.mAid = aid;
        this.mCi = ci;
    }

    public void dispose() {
    }

    public void loadEFLinearFixed(int fileid, String path, int recordNum, Message onLoaded) {
        String efPath = path == null ? getEFPath(fileid) : path;
        this.mCi.iccIOForApp(192, fileid, efPath, 0, 0, 15, null, null, this.mAid, obtainMessage(6, new LoadLinearFixedContext(fileid, recordNum, efPath, onLoaded)));
    }

    public void loadEFLinearFixed(int fileid, int recordNum, Message onLoaded) {
        loadEFLinearFixed(fileid, getEFPath(fileid), recordNum, onLoaded);
    }

    public void loadEFImgLinearFixed(int recordNum, Message onLoaded) {
        int i = recordNum;
        String str = null;
        this.mCi.iccIOForApp(192, IccConstants.EF_IMG, getEFPath(IccConstants.EF_IMG), i, 4, 15, null, str, this.mAid, obtainMessage(11, new LoadLinearFixedContext((int) IccConstants.EF_IMG, recordNum, onLoaded)));
    }

    public void getEFLinearRecordSize(int fileid, String path, Message onLoaded) {
        String efPath = path == null ? getEFPath(fileid) : path;
        this.mCi.iccIOForApp(192, fileid, efPath, 0, 0, 15, null, null, this.mAid, obtainMessage(8, new LoadLinearFixedContext(fileid, efPath, onLoaded)));
    }

    public void getEFLinearRecordSize(int fileid, Message onLoaded) {
        getEFLinearRecordSize(fileid, getEFPath(fileid), onLoaded);
    }

    public void loadEFLinearFixedAll(int fileid, String path, Message onLoaded) {
        String efPath = path == null ? getEFPath(fileid) : path;
        this.mCi.iccIOForApp(192, fileid, efPath, 0, 0, 15, null, null, this.mAid, obtainMessage(6, new LoadLinearFixedContext(fileid, efPath, onLoaded)));
    }

    public void loadEFLinearFixedAll(int fileid, Message onLoaded) {
        loadEFLinearFixedAll(fileid, getEFPath(fileid), onLoaded);
    }

    public void loadEFTransparent(int fileid, Message onLoaded) {
        int i = fileid;
        int i2 = 0;
        String str = null;
        this.mCi.iccIOForApp(192, i, getEFPath(fileid), 0, i2, 15, null, str, this.mAid, obtainMessage(4, fileid, 0, onLoaded));
    }

    public void loadEFTransparent(int fileid, int size, Message onLoaded) {
        int i = fileid;
        int i2 = 0;
        int i3 = size;
        String str = null;
        this.mCi.iccIOForApp(176, i, getEFPath(fileid), 0, i2, i3, null, str, this.mAid, obtainMessage(5, fileid, 0, onLoaded));
    }

    public void loadEFImgTransparent(int fileid, int highOffset, int lowOffset, int length, Message onLoaded) {
        Message response = obtainMessage(10, fileid, 0, onLoaded);
        logd("IccFileHandler: loadEFImgTransparent fileid = " + fileid + " filePath = " + getEFPath(IccConstants.EF_IMG) + " highOffset = " + highOffset + " lowOffset = " + lowOffset + " length = " + length);
        this.mCi.iccIOForApp(176, fileid, getEFPath(IccConstants.EF_IMG), highOffset, lowOffset, length, null, null, this.mAid, response);
    }

    public void updateEFLinearFixed(int fileid, String path, int recordNum, byte[] data, String pin2, Message onComplete) {
        this.mCi.iccIOForApp(COMMAND_UPDATE_RECORD, fileid, path == null ? getEFPath(fileid) : path, recordNum, 4, data.length, IccUtils.bytesToHexString(data), pin2, this.mAid, onComplete);
    }

    public void updateEFLinearFixed(int fileid, int recordNum, byte[] data, String pin2, Message onComplete) {
        this.mCi.iccIOForApp(COMMAND_UPDATE_RECORD, fileid, getEFPath(fileid), recordNum, 4, data.length, IccUtils.bytesToHexString(data), pin2, this.mAid, onComplete);
    }

    public void updateEFTransparent(int fileid, byte[] data, Message onComplete) {
        this.mCi.iccIOForApp(214, fileid, getEFPath(fileid), 0, 0, data.length, IccUtils.bytesToHexString(data), null, this.mAid, onComplete);
    }

    public void getPhbRecordInfo(Message response) {
        this.mCi.queryPhbStorageInfo(0, response);
    }

    private void sendResult(Message response, Object result, Throwable ex) {
        if (response != null) {
            AsyncResult.forMessage(response, result, ex);
            response.sendToTarget();
        }
    }

    private boolean processException(Message response, AsyncResult ar) {
        IccIoResult result = ar.result;
        if (ar.exception != null) {
            sendResult(response, null, ar.exception);
            return true;
        }
        IccException iccException = result.getException();
        if (iccException == null) {
            return false;
        }
        sendResult(response, null, iccException);
        return true;
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(Message msg) {
        Throwable exc;
        Message response = null;
        try {
            AsyncResult ar;
            IccIoResult result;
            byte[] data;
            LoadLinearFixedContext lc;
            String str;
            switch (msg.what) {
                case 4:
                    ar = (AsyncResult) msg.obj;
                    response = (Message) ar.userObj;
                    result = (IccIoResult) ar.result;
                    if (!processException(response, (AsyncResult) msg.obj)) {
                        data = result.payload;
                        int fileid = msg.arg1;
                        if ((byte) 4 != data[6]) {
                            throw new IccFileTypeMismatch();
                        } else if (data[13] != (byte) 0) {
                            throw new IccFileTypeMismatch();
                        } else {
                            this.mCi.iccIOForApp(176, fileid, getEFPath(fileid), 0, 0, ((data[2] & 255) << 8) + (data[3] & 255), null, null, this.mAid, obtainMessage(5, fileid, 0, response));
                            return;
                        }
                    }
                    return;
                case 5:
                case 10:
                    ar = (AsyncResult) msg.obj;
                    response = (Message) ar.userObj;
                    result = (IccIoResult) ar.result;
                    if (!processException(response, (AsyncResult) msg.obj)) {
                        sendResult(response, result.payload, null);
                        return;
                    }
                    return;
                case 6:
                case 11:
                    ar = (AsyncResult) msg.obj;
                    lc = (LoadLinearFixedContext) ar.userObj;
                    result = (IccIoResult) ar.result;
                    response = lc.mOnLoaded;
                    if (!processException(response, (AsyncResult) msg.obj)) {
                        data = result.payload;
                        String path = lc.mPath;
                        try {
                            if ((byte) 4 != data[6]) {
                                throw new IccFileTypeMismatch();
                            } else if ((byte) 1 != data[13]) {
                                throw new IccFileTypeMismatch();
                            } else {
                                lc.mRecordSize = data[14] & 255;
                                lc.mCountRecords = (((data[2] & 255) << 8) + (data[3] & 255)) / lc.mRecordSize;
                                if (lc.mLoadAll) {
                                    lc.results = new ArrayList(lc.mCountRecords);
                                }
                                if (lc.mMode != -1) {
                                    this.mCi.iccIOForApp(178, lc.mEfid, getSmsEFPath(lc.mMode), lc.mRecordNum, 4, lc.mRecordSize, null, null, this.mAid, obtainMessage(7, lc));
                                    str = path;
                                    return;
                                }
                                if (path == null) {
                                    str = getEFPath(lc.mEfid);
                                } else {
                                    str = path;
                                }
                                this.mCi.iccIOForApp(178, lc.mEfid, str, lc.mRecordNum, 4, lc.mRecordSize, null, null, this.mAid, obtainMessage(7, lc));
                                return;
                            }
                        } catch (Exception e) {
                            exc = e;
                            break;
                        }
                    }
                    return;
                case 7:
                case 9:
                    ar = (AsyncResult) msg.obj;
                    lc = (LoadLinearFixedContext) ar.userObj;
                    result = (IccIoResult) ar.result;
                    response = lc.mOnLoaded;
                    str = lc.mPath;
                    if (!processException(response, (AsyncResult) msg.obj)) {
                        if (lc.mLoadAll) {
                            lc.results.add(result.payload);
                            lc.mRecordNum++;
                            if (lc.mRecordNum > lc.mCountRecords) {
                                sendResult(response, lc.results, null);
                                return;
                            } else if (lc.mMode != -1) {
                                this.mCi.iccIOForApp(178, lc.mEfid, getSmsEFPath(lc.mMode), lc.mRecordNum, 4, lc.mRecordSize, null, null, this.mAid, obtainMessage(7, lc));
                                return;
                            } else {
                                if (str == null) {
                                    str = getEFPath(lc.mEfid);
                                }
                                this.mCi.iccIOForApp(178, lc.mEfid, str, lc.mRecordNum, 4, lc.mRecordSize, null, null, this.mAid, obtainMessage(7, lc));
                                return;
                            }
                        }
                        sendResult(response, result.payload, null);
                        return;
                    }
                    return;
                case 8:
                    ar = msg.obj;
                    result = ar.result;
                    response = ar.userObj.mOnLoaded;
                    if (!processException(response, (AsyncResult) msg.obj)) {
                        data = result.payload;
                        if ((byte) 4 == data[6] && (byte) 1 == data[13]) {
                            sendResult(response, new int[]{data[14] & 255, ((data[2] & 255) << 8) + (data[3] & 255), recordSize[1] / recordSize[0]}, null);
                            return;
                        }
                        throw new IccFileTypeMismatch();
                    }
                    return;
                case 100:
                    ar = (AsyncResult) msg.obj;
                    response = (Message) ar.userObj;
                    result = (IccIoResult) ar.result;
                    if (processException(response, (AsyncResult) msg.obj)) {
                        loge("EVENT_SELECT_EF_FILE exception");
                        return;
                    }
                    data = result.payload;
                    if ((byte) 4 != data[6]) {
                        throw new IccFileTypeMismatch();
                    }
                    sendResult(response, new EFResponseData(data), null);
                    return;
                case 101:
                    ar = (AsyncResult) msg.obj;
                    LoadTransparentContext tc = ar.userObj;
                    result = (IccIoResult) ar.result;
                    response = tc.mOnLoaded;
                    str = tc.mPath;
                    if (!processException(response, (AsyncResult) msg.obj)) {
                        data = result.payload;
                        if ((byte) 4 != data[6]) {
                            throw new IccFileTypeMismatch();
                        } else if (data[13] != (byte) 0) {
                            throw new IccFileTypeMismatch();
                        } else {
                            int size = ((data[2] & 255) << 8) + (data[3] & 255);
                            if (str == null) {
                                str = getEFPath(tc.mEfid);
                            }
                            String str2 = str;
                            int i = size;
                            this.mCi.iccIOForApp(176, tc.mEfid, str2, 0, 0, i, null, null, this.mAid, obtainMessage(5, tc.mEfid, 0, response));
                            return;
                        }
                    }
                    return;
                default:
                    return;
            }
        } catch (Exception e2) {
            exc = e2;
        }
        if (response != null) {
            loge("caught exception:" + exc);
            sendResult(response, null, exc);
            return;
        }
        loge("uncaught exception" + exc);
    }

    protected String getCommonIccEFPath(int efid) {
        switch (efid) {
            case 12037:
            case IccConstants.EF_ICCID /*12258*/:
                return IccConstants.MF_SIM;
            case IccConstants.EF_IMG /*20256*/:
                return "3F007F105F50";
            case IccConstants.EF_PBR /*20272*/:
                return "3F007F105F3A";
            case 28474:
            case IccConstants.EF_FDN /*28475*/:
            case IccConstants.EF_MSISDN /*28480*/:
            case IccConstants.EF_SDN /*28489*/:
            case IccConstants.EF_EXT1 /*28490*/:
            case IccConstants.EF_EXT2 /*28491*/:
            case IccConstants.EF_EXT3 /*28492*/:
            case 28645:
                return "3F007F10";
            default:
                return null;
        }
    }

    public void loadEFLinearFixedAll(int fileid, Message onLoaded, boolean is7FFF) {
        int i = fileid;
        int i2 = 0;
        String str = null;
        this.mCi.iccIOForApp(192, i, getEFPath(fileid), 0, i2, 15, null, str, this.mAid, obtainMessage(6, new LoadLinearFixedContext(fileid, onLoaded)));
    }

    public void loadEFLinearFixedAll(int fileid, int mode, Message onLoaded) {
        LoadLinearFixedContext lc = new LoadLinearFixedContext(fileid, onLoaded);
        lc.mMode = mode;
        int i = fileid;
        int i2 = 0;
        String str = null;
        this.mCi.iccIOForApp(192, i, getSmsEFPath(mode), 0, i2, 15, null, str, this.mAid, obtainMessage(6, lc));
    }

    protected String getSmsEFPath(int mode) {
        String efpath = UsimPBMemInfo.STRING_NOT_SET;
        if (mode == 1) {
            return "3F007F10";
        }
        if (mode == 2) {
            return "3F007F25";
        }
        return efpath;
    }

    public void loadEFTransparent(int fileid, String path, Message onLoaded) {
        String efPath = path == null ? getEFPath(fileid) : path;
        this.mCi.iccIOForApp(192, fileid, efPath, 0, 0, 15, null, null, this.mAid, obtainMessage(101, new LoadTransparentContext(fileid, efPath, onLoaded)));
    }

    public void updateEFTransparent(int fileid, String path, byte[] data, Message onComplete) {
        this.mCi.iccIOForApp(214, fileid, path == null ? getEFPath(fileid) : path, 0, 0, data.length, IccUtils.bytesToHexString(data), null, this.mAid, onComplete);
    }

    public void readEFLinearFixed(int fileid, int recordNum, int recordSize, Message onLoaded) {
        this.mCi.iccIOForApp(178, fileid, getEFPath(fileid), recordNum, 4, recordSize, null, null, this.mAid, onLoaded);
    }

    public void selectEFFile(int fileid, Message onLoaded) {
        int i = fileid;
        int i2 = 0;
        String str = null;
        this.mCi.iccIOForApp(192, i, getEFPath(fileid), 0, i2, 15, null, str, this.mAid, obtainMessage(100, fileid, 0, onLoaded));
    }

    public void oppoReadEFLinearFixedRecord(int fileid, int recordNum, int recordSize, Message onLoaded) {
        int i = fileid;
        this.mCi.iccIO(178, i, getEFPath(fileid), recordNum, 4, recordSize, null, null, obtainMessage(7, new LoadLinearFixedContext(fileid, recordNum, onLoaded)));
    }
}
