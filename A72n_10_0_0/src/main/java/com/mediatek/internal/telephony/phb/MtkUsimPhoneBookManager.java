package com.mediatek.internal.telephony.phb;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.gsm.SimTlv;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.mediatek.internal.telephony.MtkPhoneNumberUtils;
import com.mediatek.internal.telephony.MtkRIL;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import com.mediatek.internal.telephony.uicc.EFResponseData;
import com.mediatek.telephony.internal.telephony.vsim.ExternalSimConstants;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MtkUsimPhoneBookManager extends UsimPhoneBookManager {
    private static final boolean DBG;
    private static final int EVENT_AAS_LOAD_DONE = 5;
    private static final int EVENT_AAS_LOAD_DONE_OPTMZ = 28;
    private static final int EVENT_AAS_UPDATE_DONE = 10;
    private static final int EVENT_ANR_RECORD_LOAD_DONE = 16;
    private static final int EVENT_ANR_RECORD_LOAD_OPTMZ_DONE = 23;
    private static final int EVENT_ANR_UPDATE_DONE = 9;
    private static final int EVENT_EMAIL_RECORD_LOAD_DONE = 15;
    private static final int EVENT_EMAIL_RECORD_LOAD_OPTMZ_DONE = 22;
    private static final int EVENT_EMAIL_UPDATE_DONE = 8;
    private static final int EVENT_EXT1_LOAD_DONE = 1001;
    private static final int EVENT_GAS_LOAD_DONE = 6;
    private static final int EVENT_GAS_UPDATE_DONE = 13;
    private static final int EVENT_GET_RECORDS_SIZE_DONE = 1000;
    private static final int EVENT_GRP_RECORD_LOAD_DONE = 17;
    private static final int EVENT_GRP_UPDATE_DONE = 12;
    private static final int EVENT_IAP_RECORD_LOAD_DONE = 14;
    private static final int EVENT_IAP_UPDATE_DONE = 7;
    private static final int EVENT_QUERY_ANR_AVAILABLE_OPTMZ_DONE = 26;
    private static final int EVENT_QUERY_EMAIL_AVAILABLE_OPTMZ_DONE = 25;
    private static final int EVENT_QUERY_PHB_ADN_INFO = 21;
    private static final int EVENT_QUERY_SNE_AVAILABLE_OPTMZ_DONE = 27;
    private static final int EVENT_SELECT_EF_FILE_DONE = 20;
    private static final int EVENT_SNE_RECORD_LOAD_DONE = 18;
    private static final int EVENT_SNE_RECORD_LOAD_OPTMZ_DONE = 24;
    private static final int EVENT_SNE_UPDATE_DONE = 11;
    private static final int EVENT_UPB_CAPABILITY_QUERY_DONE = 19;
    private static final String LOG_TAG = "MtkUsimPhoneBookManager";
    private static final int PBR_NOT_NEED_NOTIFY = -1;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final int UPB_EF_AAS = 3;
    private static final int UPB_EF_ANR = 0;
    private static final int UPB_EF_EMAIL = 1;
    private static final int UPB_EF_GAS = 4;
    private static final int UPB_EF_GRP = 5;
    private static final int UPB_EF_SNE = 2;
    private static final int USIM_DEFAULT_MAX_ADN_FILE_SIZE = 250;
    private static final int USIM_DEFAULT_MAX_EMAIL_FILE_SIZE = 100;
    public static final int USIM_ERROR_CAPACITY_FULL = -30;
    public static final int USIM_ERROR_GROUP_COUNT = -20;
    public static final int USIM_ERROR_NAME_LEN = -10;
    public static final int USIM_ERROR_OTHERS = -50;
    public static final int USIM_ERROR_STRING_TOOLONG = -40;
    private static final int USIM_MAX_AAS_ENTRIES_COUNT = 5;
    public static final int USIM_MAX_ANR_COUNT = 3;
    private static final int USIM_TYPE2_CONDITIONAL_LENGTH = 2;
    private ArrayList<String> mAasForAnr;
    private final Object mAasLock;
    private MtkAdnRecordCache mAdnCache;
    private int mAdnFileSize;
    private int[] mAdnRecordSize;
    private ArrayList<int[]> mAnrInfo;
    private int mAnrRecordSize;
    private MtkRIL mCi;
    private UiccCardApplication mCurrentApp;
    protected EFResponseData mEfData;
    private int mEmailFileSize;
    private int[] mEmailInfo;
    private int[] mEmailRecTable;
    private int mEmailRecordSize;
    private ArrayList<ArrayList<byte[]>> mExt1FileList;
    private ArrayList<UsimGroup> mGasForGrp;
    private final Object mGasLock;
    private ArrayList<ArrayList<byte[]>> mIapFileList;
    private boolean mIsReset;
    private AtomicBoolean mNeedNotify;
    private ArrayList<Integer> mOPPOEFRecNum;
    Handler mOppoHandler;
    private int mPbrNeedNotify;
    private ArrayList<PbrRecord> mPbrRecords;
    private ArrayList<MtkAdnRecord> mPhoneBookRecords;
    private int mReadEFLinerRecordSizeNum;
    private AtomicInteger mReadingAnrNum;
    private AtomicInteger mReadingEmailNum;
    private AtomicInteger mReadingGrpNum;
    private AtomicInteger mReadingIapNum;
    private AtomicInteger mReadingSneNum;
    private SparseArray<int[]> mRecordSize;
    private boolean mRefreshAdnInfo;
    private boolean mRefreshAnrInfo;
    private boolean mRefreshEmailInfo;
    private int mResult;
    private int mSliceCount;
    private int mSlotId;
    private int[] mSneInfo;
    private final Object mUPBCapabilityLock;
    private int[] mUpbCap;
    private int mUpbDone;
    private Message pendingResponse;

    static /* synthetic */ int access$408(MtkUsimPhoneBookManager x0) {
        int i = x0.mSliceCount;
        x0.mSliceCount = i + 1;
        return i;
    }

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1 && !SystemProperties.get("ro.build.type").equals(DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER)) {
            z = true;
        }
        DBG = z;
    }

    /* access modifiers changed from: private */
    public class File {
        public int mAnrIndex;
        private final int mEfid;
        private final int mIndex;
        private final int mParentTag;
        public int mPbrRecord;
        private final int mSfi;
        public int mTag;

        File(int parentTag, int efid, int sfi, int index) {
            this.mParentTag = parentTag;
            this.mEfid = efid;
            this.mSfi = sfi;
            this.mIndex = index;
        }

        public int getParentTag() {
            return this.mParentTag;
        }

        public int getEfid() {
            return this.mEfid;
        }

        public int getSfi() {
            return this.mSfi;
        }

        public int getIndex() {
            return this.mIndex;
        }

        public String toString() {
            return "mParentTag:" + Integer.toHexString(this.mParentTag).toUpperCase() + ",mEfid:" + Integer.toHexString(this.mEfid).toUpperCase() + ",mSfi:" + Integer.toHexString(this.mSfi).toUpperCase() + ",mIndex:" + this.mIndex + ",mPbrRecord:" + this.mPbrRecord + ",mAnrIndex" + this.mAnrIndex + ",mTag:" + Integer.toHexString(this.mTag).toUpperCase();
        }
    }

    public MtkUsimPhoneBookManager(IccFileHandler fh, AdnRecordCache cache) {
        super(fh, cache);
        this.mSlotId = -1;
        this.mGasLock = new Object();
        this.mUPBCapabilityLock = new Object();
        this.mAasLock = new Object();
        this.mEmailRecordSize = -1;
        this.mEmailFileSize = 100;
        this.mAdnFileSize = USIM_DEFAULT_MAX_ADN_FILE_SIZE;
        this.mAnrRecordSize = -1;
        this.mSliceCount = 0;
        this.mUpbDone = -1;
        this.mIsReset = false;
        this.mPbrNeedNotify = -1;
        this.mReadEFLinerRecordSizeNum = 0;
        this.mIapFileList = null;
        this.mRefreshEmailInfo = false;
        this.mRefreshAnrInfo = false;
        this.mRefreshAdnInfo = false;
        this.mEmailRecTable = new int[400];
        this.mUpbCap = new int[8];
        this.mResult = -1;
        this.mReadingAnrNum = new AtomicInteger(0);
        this.mReadingEmailNum = new AtomicInteger(0);
        this.mReadingGrpNum = new AtomicInteger(0);
        this.mReadingSneNum = new AtomicInteger(0);
        this.mReadingIapNum = new AtomicInteger(0);
        this.mNeedNotify = new AtomicBoolean(false);
        this.mEfData = null;
        this.mOppoHandler = new Handler() {
            /* class com.mediatek.internal.telephony.phb.MtkUsimPhoneBookManager.AnonymousClass1 */

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 90:
                        AsyncResult ar = (AsyncResult) msg.obj;
                        int index = msg.arg1;
                        MtkAdnRecord adn = (MtkAdnRecord) ar.userObj;
                        if (adn.getRecId() <= 0) {
                            adn.setRecordIndex(index);
                        }
                        if (ar.exception == null) {
                            MtkUsimPhoneBookManager.this.mPhoneBookRecords.set(index - 1, adn);
                        }
                        if (MtkUsimPhoneBookManager.this.pendingResponse != null) {
                            AsyncResult.forMessage(MtkUsimPhoneBookManager.this.pendingResponse, (Object) null, ar.exception);
                            MtkUsimPhoneBookManager.this.pendingResponse.sendToTarget();
                            MtkUsimPhoneBookManager.this.pendingResponse = null;
                            return;
                        }
                        return;
                    case 91:
                        if (((AsyncResult) msg.obj).exception == null) {
                            MtkUsimPhoneBookManager.this.mFh.loadEFTransparent(20259, (Message) null);
                            return;
                        }
                        return;
                    case 92:
                        AsyncResult ar2 = (AsyncResult) msg.obj;
                        if (ar2.exception == null && ((byte[]) ar2.result)[0] == 1) {
                            MtkUsimPhoneBookManager.this.resetEFpbcControlInfor(msg.arg1, msg.arg2);
                            return;
                        }
                        return;
                    case 93:
                        AsyncResult ar3 = (AsyncResult) msg.obj;
                        if (ar3.exception == null) {
                            byte[] bArr = (byte[]) ar3.result;
                            return;
                        }
                        return;
                    case 94:
                        AsyncResult ar4 = (AsyncResult) msg.obj;
                        if (ar4.exception == null) {
                            int[] record = (int[]) ar4.result;
                            if (record.length == 3) {
                                MtkUsimPhoneBookManager.this.clearAllEFPbcControlInformation(msg.arg2, record[0], record[2]);
                                return;
                            }
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.mOPPOEFRecNum = new ArrayList<>();
    }

    public MtkUsimPhoneBookManager(IccFileHandler fh, AdnRecordCache cache, CommandsInterface ci, UiccCardApplication app) {
        super(fh, cache);
        this.mSlotId = -1;
        this.mGasLock = new Object();
        this.mUPBCapabilityLock = new Object();
        this.mAasLock = new Object();
        this.mEmailRecordSize = -1;
        this.mEmailFileSize = 100;
        this.mAdnFileSize = USIM_DEFAULT_MAX_ADN_FILE_SIZE;
        this.mAnrRecordSize = -1;
        this.mSliceCount = 0;
        this.mUpbDone = -1;
        this.mIsReset = false;
        this.mPbrNeedNotify = -1;
        this.mReadEFLinerRecordSizeNum = 0;
        this.mIapFileList = null;
        this.mRefreshEmailInfo = false;
        this.mRefreshAnrInfo = false;
        this.mRefreshAdnInfo = false;
        this.mEmailRecTable = new int[400];
        this.mUpbCap = new int[8];
        this.mResult = -1;
        this.mReadingAnrNum = new AtomicInteger(0);
        this.mReadingEmailNum = new AtomicInteger(0);
        this.mReadingGrpNum = new AtomicInteger(0);
        this.mReadingSneNum = new AtomicInteger(0);
        this.mReadingIapNum = new AtomicInteger(0);
        this.mNeedNotify = new AtomicBoolean(false);
        this.mEfData = null;
        this.mOppoHandler = new Handler() {
            /* class com.mediatek.internal.telephony.phb.MtkUsimPhoneBookManager.AnonymousClass1 */

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 90:
                        AsyncResult ar = (AsyncResult) msg.obj;
                        int index = msg.arg1;
                        MtkAdnRecord adn = (MtkAdnRecord) ar.userObj;
                        if (adn.getRecId() <= 0) {
                            adn.setRecordIndex(index);
                        }
                        if (ar.exception == null) {
                            MtkUsimPhoneBookManager.this.mPhoneBookRecords.set(index - 1, adn);
                        }
                        if (MtkUsimPhoneBookManager.this.pendingResponse != null) {
                            AsyncResult.forMessage(MtkUsimPhoneBookManager.this.pendingResponse, (Object) null, ar.exception);
                            MtkUsimPhoneBookManager.this.pendingResponse.sendToTarget();
                            MtkUsimPhoneBookManager.this.pendingResponse = null;
                            return;
                        }
                        return;
                    case 91:
                        if (((AsyncResult) msg.obj).exception == null) {
                            MtkUsimPhoneBookManager.this.mFh.loadEFTransparent(20259, (Message) null);
                            return;
                        }
                        return;
                    case 92:
                        AsyncResult ar2 = (AsyncResult) msg.obj;
                        if (ar2.exception == null && ((byte[]) ar2.result)[0] == 1) {
                            MtkUsimPhoneBookManager.this.resetEFpbcControlInfor(msg.arg1, msg.arg2);
                            return;
                        }
                        return;
                    case 93:
                        AsyncResult ar3 = (AsyncResult) msg.obj;
                        if (ar3.exception == null) {
                            byte[] bArr = (byte[]) ar3.result;
                            return;
                        }
                        return;
                    case 94:
                        AsyncResult ar4 = (AsyncResult) msg.obj;
                        if (ar4.exception == null) {
                            int[] record = (int[]) ar4.result;
                            if (record.length == 3) {
                                MtkUsimPhoneBookManager.this.clearAllEFPbcControlInformation(msg.arg2, record[0], record[2]);
                                return;
                            }
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.mOPPOEFRecNum = new ArrayList<>();
        this.mFh = fh;
        this.mPhoneBookRecords = new ArrayList<>();
        this.mGasForGrp = new ArrayList<>();
        this.mIapFileList = new ArrayList<>();
        this.mPbrRecords = null;
        this.mIsPbrPresent = true;
        this.mAdnCache = (MtkAdnRecordCache) cache;
        this.mCi = (MtkRIL) ci;
        this.mCurrentApp = app;
        this.mSlotId = app == null ? -1 : app.getPhoneId();
        this.mEmailsForAdnRec = new SparseArray();
        this.mSfiEfidTable = new SparseIntArray();
        for (int i = 0; i < 8; i++) {
            this.mUpbCap[i] = -1;
        }
        logi("constructor finished. ");
    }

    public void reset() {
        this.mIsReset = true;
        this.mPhoneBookRecords.clear();
        this.mIapFileRecord = null;
        this.mEmailFileRecord = null;
        this.mPbrRecords = null;
        this.mIsPbrPresent = true;
        this.mRefreshCache = false;
        this.mEmailsForAdnRec.clear();
        this.mSfiEfidTable.clear();
        this.mGasForGrp.clear();
        this.mIapFileList = null;
        this.mAasForAnr = null;
        this.mExt1FileList = null;
        this.mSliceCount = 0;
        this.mEmailRecTable = new int[400];
        this.mEmailInfo = null;
        this.mSneInfo = null;
        this.mAnrInfo = null;
        for (int i = 0; i < 8; i++) {
            this.mUpbCap[i] = -1;
        }
        this.mEmailRecordSize = -1;
        this.mAnrRecordSize = -1;
        this.mUpbDone = -1;
        this.mAdnRecordSize = null;
        this.mRefreshEmailInfo = false;
        this.mRefreshAnrInfo = false;
        this.mRefreshAdnInfo = false;
        this.mOPPOEFRecNum.clear();
        synchronized (this.mLock) {
            this.mLock.notifyAll();
        }
        this.mPbrNeedNotify = -1;
        logi("reset finished. ");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:69:0x014e, code lost:
        if (r9.mPbrRecords == null) goto L_0x0179;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0150, code lost:
        r2 = java.lang.System.currentTimeMillis();
        logi("loadEfFilesFromUsim Time: " + (r2 - r0) + " AppType: " + r9.mCurrentApp.getType());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0179, code lost:
        logi("loadEfFilesFromUsim end");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0180, code lost:
        return r9.mPhoneBookRecords;
     */
    public ArrayList<MtkAdnRecord> loadEfFilesFromUsim(Object object) {
        int[] size;
        long prevTime = System.currentTimeMillis();
        synchronized (this.mLock) {
            this.mIsReset = false;
            if (!this.mPhoneBookRecords.isEmpty()) {
                if (this.mRefreshCache) {
                    this.mRefreshCache = false;
                    refreshCache();
                }
                return this.mPhoneBookRecords;
            } else if (!this.mIsPbrPresent.booleanValue()) {
                return null;
            } else {
                if (this.mPbrRecords == null || this.mPbrRecords.size() == 0) {
                    this.mPbrNeedNotify++;
                    readPbrFileAndWait();
                }
                Rlog.d(LOG_TAG, "loadEfFilesFromUsim, mIsPbrPresent: " + this.mIsPbrPresent);
                if (this.mPbrRecords != null) {
                    if (this.mPbrRecords.size() != 0) {
                        logi("loadEfFilesFromUsim mPbrNeedNotify:" + this.mPbrNeedNotify);
                        if (this.mEmailRecordSize < 0) {
                            readEmailRecordSize();
                        }
                        if (this.mAnrRecordSize < 0) {
                            readAnrRecordSize();
                        }
                        int adnEf = ((File) this.mPbrRecords.get(0).mFileIds.get(192)).getEfid();
                        if (adnEf > 0 && (size = readEFLinearRecordSize(adnEf)) != null && size.length == 3) {
                            this.mAdnFileSize = size[2];
                        }
                        if (this.mPbrRecords.get(0).mFileIds.get(195) != null) {
                            readEFLinearRecordSize(((File) this.mPbrRecords.get(0).mFileIds.get(195)).getEfid());
                        }
                        int numRecs = this.mPbrRecords.size();
                        if (!CsimPhbUtil.hasModemPhbEnhanceCapability(this.mFh)) {
                            for (int i = 0; i < numRecs; i++) {
                                readAASFileAndWait(i);
                                readAdnFileAndWaitForUICC(i);
                            }
                        } else {
                            readAasFileAndWaitOptmz();
                            readAdnFileAndWait(0);
                        }
                        if (this.mPhoneBookRecords.isEmpty()) {
                            logi("loadEfFilesFromUsim mPhoneBookRecords Empty");
                            return this.mPhoneBookRecords;
                        }
                        if (!CsimPhbUtil.hasModemPhbEnhanceCapability(this.mFh)) {
                            for (int i2 = 0; i2 < numRecs; i2++) {
                                if (isSupportSne()) {
                                    readSneFileAndWait(i2);
                                }
                                readAnrFileAndWait(i2);
                                readEmailFileAndWait(i2);
                            }
                        } else {
                            logi("loadEfFilesFromUsim Speed up read begin");
                            readSneFileAndWaitOptmz();
                            readAnrFileAndWaitOptmz();
                            readEmailFileAndWaitOptmz();
                            logi("loadEfFilesFromUsim Speed up read end");
                        }
                        readGrpIdsAndWait();
                        if (this.mPbrRecords != null) {
                            this.mUpbDone = 1;
                        }
                    }
                }
                if (!checkIsPhbReady() || this.mIsReset) {
                    logi("loadEfFilesFromUsim phb not ready and Reset");
                    return null;
                }
                if (true == readAdnFileAndWait(0)) {
                    this.mIsPbrPresent = false;
                    this.mEmailRecordSize = 0;
                    this.mAnrRecordSize = 0;
                    this.mUpbDone = 1;
                }
                logi("loadEfFilesFromUsim getRecordIfLoaded EF_ADN pbrP:" + this.mIsPbrPresent);
                return this.mAdnCache.getRecordsIfLoaded(28474, null);
            }
        }
    }

    private void readEmailFileAndWait(int recId) {
        SparseArray<File> files;
        File emailFile;
        logi("readEmailFileAndWait " + recId);
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (arrayList != null && arrayList.size() != 0 && (files = this.mPbrRecords.get(recId).mFileIds) != null && (emailFile = files.get(ExternalSimConstants.EVENT_TYPE_RECEIVE_RSIM_AUTH_RSP)) != null) {
            emailFile.getEfid();
            if (emailFile.getParentTag() == 168) {
                readType1Ef(emailFile, 0);
            } else if (emailFile.getParentTag() == 169) {
                readType2Ef(emailFile);
            }
        }
    }

    private void readIapFileAndWait(int pbrIndex, int efid, boolean forceRefresh) {
        int[] size;
        logi("readIapFileAndWait pbrIndex :" + pbrIndex + ",efid:" + efid + ",forceRefresh:" + forceRefresh);
        if (efid > 0) {
            if (this.mIapFileList == null) {
                logi("readIapFileAndWait IapFileList is null !!!! recreate it !");
                this.mIapFileList = new ArrayList<>();
            }
            SparseArray<int[]> sparseArray = this.mRecordSize;
            if (sparseArray == null || sparseArray.get(efid) == null) {
                size = readEFLinearRecordSize(efid);
            } else {
                size = this.mRecordSize.get(efid);
            }
            if (size == null || size.length != 3) {
                Rlog.e(LOG_TAG, "readIapFileAndWait: read record size error.");
                this.mIapFileList.add(pbrIndex, new ArrayList<>());
                return;
            }
            char c = 0;
            if (this.mIapFileList.size() <= pbrIndex) {
                log("Create IAP first!");
                ArrayList<byte[]> iapList = new ArrayList<>();
                for (int i = 0; i < this.mAdnFileSize; i++) {
                    byte[] value = new byte[size[0]];
                    int lens = value.length;
                    for (int le = 0; le < lens; le++) {
                        value[le] = -1;
                    }
                    iapList.add(value);
                }
                this.mIapFileList.add(pbrIndex, iapList);
            } else {
                log("This IAP has been loaded!");
                if (!forceRefresh) {
                    return;
                }
            }
            int numAdnRecs = this.mPhoneBookRecords.size();
            int i2 = this.mAdnFileSize;
            int nOffset = pbrIndex * i2;
            int nMax = i2 + nOffset;
            int nMax2 = numAdnRecs < nMax ? numAdnRecs : nMax;
            log("readIapFileAndWait nOffset " + nOffset + ", nMax " + nMax2);
            int i3 = nOffset;
            int totalReadingNum = 0;
            while (i3 < nMax2) {
                try {
                    MtkAdnRecord rec = this.mPhoneBookRecords.get(i3);
                    if (rec.getAlphaTag().length() > 0 || rec.getNumber().length() > 0) {
                        this.mReadingIapNum.addAndGet(1);
                        int[] data = new int[2];
                        data[c] = pbrIndex;
                        data[1] = i3 - nOffset;
                        this.mFh.readEFLinearFixed(efid, (i3 + 1) - nOffset, size[c], obtainMessage(14, data));
                        totalReadingNum++;
                    }
                    i3++;
                    c = 0;
                } catch (IndexOutOfBoundsException e) {
                    Rlog.e(LOG_TAG, "readIapFileAndWait: mPhoneBookRecords IndexOutOfBoundsException numAdnRecs is " + numAdnRecs + "index is " + i3);
                }
            }
            if (this.mReadingIapNum.get() == 0) {
                this.mNeedNotify.set(false);
                return;
            }
            this.mNeedNotify.set(true);
            logi("readIapFileAndWait before mLock.wait " + this.mNeedNotify.get() + " total:" + totalReadingNum);
            synchronized (this.mLock) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e2) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readIapFileAndWait");
                }
            }
            logi("readIapFileAndWait after mLock.wait after mLock.wait:" + this.mNeedNotify.get());
            if (true == this.mNeedNotify.get()) {
                this.mNeedNotify.set(false);
            }
        }
    }

    private void readAASFileAndWait(int recId) {
        SparseArray<File> files;
        File aasFile;
        logi("readAASFileAndWait " + recId);
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (arrayList != null && arrayList.size() != 0 && (files = this.mPbrRecords.get(recId).mFileIds) != null && (aasFile = files.get(199)) != null) {
            int aasEfid = aasFile.getEfid();
            log("readAASFileAndWait-get AAS EFID " + aasEfid);
            if (this.mAasForAnr != null) {
                logi("AAS has been loaded for Pbr number " + recId);
            }
            if (this.mFh != null) {
                Message msg = obtainMessage(5);
                msg.arg1 = recId;
                this.mFh.loadEFLinearFixedAll(aasEfid, msg);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readAASFileAndWait");
                }
            } else {
                Rlog.e(LOG_TAG, "readAASFileAndWait-IccFileHandler is null");
            }
        }
    }

    private void readSneFileAndWait(int recId) {
        SparseArray<File> files;
        File sneFile;
        logi("readSneFileAndWait " + recId);
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (arrayList != null && arrayList.size() != 0 && (files = this.mPbrRecords.get(recId).mFileIds) != null && (sneFile = files.get(195)) != null) {
            int sneEfid = sneFile.getEfid();
            log("readSneFileAndWait: EFSNE id is " + sneEfid);
            if (sneFile.getParentTag() == 169) {
                readType2Ef(sneFile);
            } else if (sneFile.getParentTag() == 168) {
                readType1Ef(sneFile, 0);
            }
        }
    }

    private void readAnrFileAndWait(int recId) {
        logi("readAnrFileAndWait: recId is " + recId);
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (!(arrayList == null || arrayList.size() == 0)) {
            SparseArray<File> files = this.mPbrRecords.get(recId).mFileIds;
            if (files == null) {
                log("readAnrFileAndWait: No anr tag in pbr record " + recId);
                return;
            }
            for (int index = 0; index < this.mPbrRecords.get(recId).mAnrIndex; index++) {
                File anrFile = files.get((index * 256) + 196);
                if (anrFile != null) {
                    if (anrFile.getParentTag() == 169) {
                        anrFile.mAnrIndex = index;
                        readType2Ef(anrFile);
                        return;
                    } else if (anrFile.getParentTag() == 168) {
                        anrFile.mAnrIndex = index;
                        readType1Ef(anrFile, index);
                        return;
                    } else {
                        return;
                    }
                }
            }
        }
    }

    private void readGrpIdsAndWait() {
        SparseArray<File> files;
        logi("readGrpIdsAndWait begin");
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (!(arrayList == null || arrayList.size() == 0 || (files = this.mPbrRecords.get(0).mFileIds) == null || files.get(198) == null)) {
            int totalReadingNum = 0;
            int numAdnRecs = this.mPhoneBookRecords.size();
            for (int i = 0; i < numAdnRecs; i++) {
                try {
                    MtkAdnRecord rec = this.mPhoneBookRecords.get(i);
                    if (rec.getAlphaTag().length() > 0 || rec.getNumber().length() > 0) {
                        this.mReadingGrpNum.incrementAndGet();
                        int adnIndex = rec.getRecId();
                        this.mCi.readUPBGrpEntry(adnIndex, obtainMessage(17, new int[]{i, adnIndex}));
                        totalReadingNum++;
                    }
                } catch (IndexOutOfBoundsException e) {
                    Rlog.e(LOG_TAG, "readGrpIdsAndWait: mPhoneBookRecords IndexOutOfBoundsException numAdnRecs is " + numAdnRecs + "index is " + i);
                }
            }
            if (this.mReadingGrpNum.get() == 0) {
                this.mNeedNotify.set(false);
                return;
            }
            this.mNeedNotify.set(true);
            logi("readGrpIdsAndWait before mLock.wait " + this.mNeedNotify.get() + " total:" + totalReadingNum);
            try {
                this.mLock.wait();
            } catch (InterruptedException e2) {
                Rlog.e(LOG_TAG, "Interrupted Exception in readGrpIdsAndWait");
            }
            logi("readGrpIdsAndWait after mLock.wait after mLock.wait " + this.mNeedNotify.get());
            if (true == this.mNeedNotify.get()) {
                this.mNeedNotify.set(false);
            }
        }
    }

    private boolean readAdnFileAndWait(int recId) {
        logi("readAdnFileAndWait begin: recId is " + recId + ",mIsReset:" + this.mIsReset);
        int previousSize = this.mPhoneBookRecords.size();
        MtkAdnRecordCache mtkAdnRecordCache = this.mAdnCache;
        mtkAdnRecordCache.requestLoadAllAdnLike(28474, mtkAdnRecordCache.extensionEfForEf(28474), obtainMessage(2));
        try {
            this.mLock.wait();
        } catch (InterruptedException e) {
            Rlog.e(LOG_TAG, "Interrupted Exception in readAdnFileAndWait");
        }
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (arrayList != null && arrayList.size() > recId) {
            this.mPbrRecords.get(recId).mMasterFileRecordNum = this.mPhoneBookRecords.size() - previousSize;
        }
        logi("readAdnFileAndWait end: recId is " + recId + ",mIsReset:" + this.mIsReset);
        if (!this.mIsReset) {
            return true;
        }
        return false;
    }

    private void createPbrFile(ArrayList<byte[]> records) {
        int sfi;
        if (records == null) {
            this.mPbrRecords = null;
            this.mIsPbrPresent = false;
            return;
        }
        this.mPbrRecords = new ArrayList<>();
        this.mSliceCount = 0;
        for (int i = 0; i < records.size(); i++) {
            if (!(records.get(i) == null || records.get(i).length <= 0 || records.get(i)[0] == -1)) {
                this.mPbrRecords.add(new PbrRecord(records.get(i)));
            }
        }
        Iterator<PbrRecord> it = this.mPbrRecords.iterator();
        while (it.hasNext()) {
            PbrRecord record = it.next();
            File file = (File) record.mFileIds.get(192);
            if (!(file == null || (sfi = file.getSfi()) == -1)) {
                this.mSfiEfidTable.put(sfi, ((File) record.mFileIds.get(192)).getEfid());
            }
        }
    }

    private void readAasFileAndWaitOptmz() {
        SparseArray<File> files;
        File aasFile;
        logi("readAasFileAndWaitOptmz begin");
        ArrayList<String> arrayList = this.mAasForAnr;
        if (arrayList == null || arrayList.size() == 0) {
            int aasRecNum = 0;
            int[] iArr = this.mUpbCap;
            if (iArr[3] < 0) {
                ArrayList<PbrRecord> arrayList2 = this.mPbrRecords;
                if (arrayList2 != null && arrayList2.size() != 0 && (files = this.mPbrRecords.get(0).mFileIds) != null && (aasFile = files.get(199)) != null) {
                    int[] size = readEFLinearRecordSize(aasFile.getEfid());
                    if (size != null && size.length == 3) {
                        aasRecNum = size[2];
                    }
                } else {
                    return;
                }
            } else {
                aasRecNum = iArr[3];
            }
            if (aasRecNum > 5) {
                aasRecNum = 5;
            }
            this.mCi.readUPBAasList(1, aasRecNum, obtainMessage(EVENT_AAS_LOAD_DONE_OPTMZ));
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                Rlog.e(LOG_TAG, "Interrupted Exception in readAasFileAndWaitOptmz");
            }
        }
        logi("readAasFileAndWaitOptmz end");
    }

    private void readEmailFileAndWaitOptmz() {
        SparseArray<File> files;
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (!(arrayList == null || arrayList.size() == 0 || (files = this.mPbrRecords.get(0).mFileIds) == null || files.get(ExternalSimConstants.EVENT_TYPE_RECEIVE_RSIM_AUTH_RSP) == null)) {
            int totalReadingNum = 0;
            int numAdnRecs = this.mPhoneBookRecords.size();
            for (int i = 0; i < numAdnRecs; i++) {
                try {
                    MtkAdnRecord rec = this.mPhoneBookRecords.get(i);
                    if (rec.getAlphaTag().length() > 0 || rec.getNumber().length() > 0) {
                        this.mReadingEmailNum.incrementAndGet();
                        this.mCi.readUPBEmailEntry(i + 1, 1, obtainMessage(EVENT_EMAIL_RECORD_LOAD_OPTMZ_DONE, new int[]{0, i}));
                        totalReadingNum++;
                    }
                } catch (IndexOutOfBoundsException e) {
                    Rlog.e(LOG_TAG, "readEmailFileAndWaitOptmz: mPhoneBookRecords IndexOutOfBoundsnumAdnRecs is " + numAdnRecs + "index is " + i);
                }
            }
            if (this.mReadingEmailNum.get() == 0) {
                this.mNeedNotify.set(false);
                return;
            }
            this.mNeedNotify.set(true);
            logi("readEmailFileAndWaitOptmz before mLock.wait " + this.mNeedNotify.get() + " total:" + totalReadingNum);
            synchronized (this.mLock) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e2) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readEmailFileAndWaitOptmz");
                }
            }
            logi("readEmailFileAndWaitOptmz after mLock.wait: " + this.mNeedNotify.get());
            if (true == this.mNeedNotify.get()) {
                this.mNeedNotify.set(false);
            }
        }
    }

    private void readAnrFileAndWaitOptmz() {
        SparseArray<File> files;
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (!(arrayList == null || arrayList.size() == 0 || (files = this.mPbrRecords.get(0).mFileIds) == null || files.get((0 * 256) + 196) == null)) {
            int totalReadingNum = 0;
            int numAdnRecs = this.mPhoneBookRecords.size();
            for (int i = 0; i < numAdnRecs; i++) {
                try {
                    MtkAdnRecord rec = this.mPhoneBookRecords.get(i);
                    if (rec.getAlphaTag().length() > 0 || rec.getNumber().length() > 0) {
                        this.mReadingAnrNum.addAndGet(1);
                        this.mCi.readUPBAnrEntry(i + 1, 0 + 1, obtainMessage(EVENT_ANR_RECORD_LOAD_OPTMZ_DONE, new int[]{0, i, 0}));
                        totalReadingNum++;
                    }
                } catch (IndexOutOfBoundsException e) {
                    Rlog.e(LOG_TAG, "readAnrFileAndWaitOptmz: mPhoneBookRecords IndexOutOfBoundsnumAdnRecs is " + numAdnRecs + "index is " + i);
                }
            }
            if (this.mReadingAnrNum.get() == 0) {
                this.mNeedNotify.set(false);
                return;
            }
            this.mNeedNotify.set(true);
            logi("readAnrFileAndWaitOptmz before mLock.wait " + this.mNeedNotify.get() + " total:" + totalReadingNum);
            synchronized (this.mLock) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e2) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readAnrFileAndWaitOptmz");
                }
            }
            logi("readAnrFileAndWaitOptmz after mLock.wait:" + this.mNeedNotify.get());
            if (true == this.mNeedNotify.get()) {
                this.mNeedNotify.set(false);
            }
        }
    }

    private void readSneFileAndWaitOptmz() {
        SparseArray<File> files;
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (!(arrayList == null || arrayList.size() == 0 || (files = this.mPbrRecords.get(0).mFileIds) == null || files.get(195) == null)) {
            int totalReadingNum = 0;
            int numAdnRecs = this.mPhoneBookRecords.size();
            for (int i = 0; i < numAdnRecs; i++) {
                try {
                    MtkAdnRecord rec = this.mPhoneBookRecords.get(i);
                    if (rec.getAlphaTag().length() > 0 || rec.getNumber().length() > 0) {
                        this.mReadingSneNum.incrementAndGet();
                        this.mCi.readUPBSneEntry(i + 1, 1, obtainMessage(EVENT_SNE_RECORD_LOAD_OPTMZ_DONE, new int[]{0, i}));
                        totalReadingNum++;
                    }
                } catch (IndexOutOfBoundsException e) {
                    Rlog.e(LOG_TAG, "readSneFileAndWaitOptmz: mPhoneBookRecords IndexOutOfBoundsnumAdnRecs is " + numAdnRecs + "index is " + i);
                }
            }
            if (this.mReadingSneNum.get() == 0) {
                this.mNeedNotify.set(false);
                return;
            }
            this.mNeedNotify.set(true);
            logi("readSneFileAndWaitOptmz before mLock.wait " + this.mNeedNotify.get() + " total:" + totalReadingNum);
            synchronized (this.mLock) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e2) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readSneFileAndWaitOptmz");
                }
            }
            logi("readSneFileAndWaitOptmz after mLock.wait: " + this.mNeedNotify.get());
            if (true == this.mNeedNotify.get()) {
                this.mNeedNotify.set(false);
            }
        }
    }

    private void updatePhoneAdnRecordWithEmailByIndexOptmz(int emailIndex, int adnIndex, String email) {
        log("updatePhoneAdnRecordWithEmailByIndex emailIndex = " + emailIndex + ",adnIndex = " + adnIndex);
        if (email != null) {
            try {
                if (!email.equals("")) {
                    this.mPhoneBookRecords.get(adnIndex).setEmails(new String[]{email});
                }
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "[JE]updatePhoneAdnRecordWithEmailByIndex " + e.getMessage());
            }
        }
    }

    private void updatePhoneAdnRecordWithAnrByIndexOptmz(int recId, int adnIndex, int anrIndex, PhbEntry anrData) {
        String anr;
        ArrayList<String> arrayList;
        log("updatePhoneAdnRecordWithAnrByIndexOptmz the " + adnIndex + " anr record:" + anrData);
        if (anrData != null && anrData.number != null && !anrData.number.equals("")) {
            if (anrData.ton == 145) {
                anr = MtkPhoneNumberUtils.prependPlusToNumber(anrData.number);
            } else {
                anr = anrData.number;
            }
            String anr2 = anr.replace('?', 'N').replace('p', ',').replace('w', ';');
            int anrAas = anrData.index;
            if (anr2 != null && !anr2.equals("")) {
                String aas = null;
                if (anrAas > 0 && anrAas != 255 && (arrayList = this.mAasForAnr) != null && anrAas <= arrayList.size()) {
                    aas = this.mAasForAnr.get(anrAas - 1);
                }
                log(" updatePhoneAdnRecordWithAnrByIndex " + adnIndex + " th anr is " + anr2 + " the anrIndex is " + anrIndex);
                try {
                    MtkAdnRecord rec = this.mPhoneBookRecords.get(adnIndex);
                    rec.setAnr(anr2, anrIndex);
                    if (aas != null && aas.length() > 0) {
                        rec.setAasIndex(anrAas);
                    }
                    this.mPhoneBookRecords.set(adnIndex, rec);
                } catch (IndexOutOfBoundsException e) {
                    Rlog.e(LOG_TAG, "updatePhoneAdnRecordWithAnrByIndex: mPhoneBookRecords IndexOutOfBoundsException size: " + this.mPhoneBookRecords.size() + "index: " + adnIndex);
                }
            }
        }
    }

    private String[] buildAnrRecordOptmz(String number, int aas) {
        int ton = 129;
        if (number.indexOf(43) != -1) {
            if (number.indexOf(43) != number.lastIndexOf(43)) {
                Rlog.w(LOG_TAG, "There are multiple '+' in the number: " + number);
            }
            ton = 145;
            number = number.replace("+", "");
        }
        return new String[]{number.replace('N', '?').replace(',', 'p').replace(';', 'w'), Integer.toString(ton), Integer.toString(aas)};
    }

    private void updatePhoneAdnRecordWithSneByIndexOptmz(int adnIndex, String sne) {
        if (sne != null) {
            log("updatePhoneAdnRecordWithSneByIndex index " + adnIndex + " recData file is " + sne);
            if (!sne.equals("")) {
                try {
                    this.mPhoneBookRecords.get(adnIndex).setSne(sne);
                } catch (IndexOutOfBoundsException e) {
                    Rlog.e(LOG_TAG, "updatePhoneAdnRecordWithSneByIndex: mPhoneBookRecords IndexOutOfBoundsException size() is " + this.mPhoneBookRecords.size() + "index is " + adnIndex);
                }
            }
        }
    }

    public void handleMessage(Message msg) {
        ArrayList<byte[]> record;
        ArrayList<byte[]> aasFileRecords;
        String[] gasList;
        String[] aasList;
        int i = msg.what;
        if (i == EVENT_GET_RECORDS_SIZE_DONE) {
            AsyncResult ar = (AsyncResult) msg.obj;
            int efid = msg.arg1;
            logi("EVENT_GET_RECORDS_SIZE_DONE done, recNum:" + this.mReadEFLinerRecordSizeNum + ", ef_id:" + efid);
            if (ar.exception == null) {
                int[] recordSize = (int[]) ar.result;
                if (recordSize.length == 3) {
                    if (this.mRecordSize == null) {
                        this.mRecordSize = new SparseArray<>();
                    }
                    this.mRecordSize.put(efid, recordSize);
                } else {
                    Rlog.e(LOG_TAG, "get wrong record size format" + ar.exception);
                }
            } else {
                Rlog.e(LOG_TAG, "get EF record size failed" + ar.exception);
            }
            if (this.mReadEFLinerRecordSizeNum > 0) {
                synchronized (this.mLock) {
                    this.mLock.notify();
                }
            }
        } else if (i != 1001) {
            switch (i) {
                case 1:
                    logi("handleMessage: EVENT_PBR_LOAD_DONE:" + this.mPbrNeedNotify);
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2.exception == null) {
                        createPbrFile((ArrayList) ar2.result);
                    } else {
                        this.mIsPbrPresent = false;
                        Rlog.d(LOG_TAG, "UsimPhoneBookManager, get PBR with exception:" + ar2.exception);
                    }
                    if (this.mPbrNeedNotify != -1) {
                        synchronized (this.mLock) {
                            this.mLock.notify();
                        }
                        this.mPbrNeedNotify--;
                        return;
                    }
                    return;
                case 2:
                    logi("Loading USIM ADN records done");
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    if (ar3.exception != null || this.mPhoneBookRecords == null) {
                        Rlog.w(LOG_TAG, "Loading USIM ADN records fail.");
                    } else if (!CsimPhbUtil.hasModemPhbEnhanceCapability(this.mFh) && this.mPhoneBookRecords.size() > 0 && ar3.result != null) {
                        ArrayList<MtkAdnRecord> adnList = changeAdnRecordNumber(this.mPhoneBookRecords.size(), (ArrayList) ar3.result);
                        this.mOPPOEFRecNum.add(Integer.valueOf(adnList.size()));
                        this.mPhoneBookRecords.addAll(adnList);
                        CsimPhbUtil.initPhbStorage(adnList);
                    } else if (ar3.result != null) {
                        this.mOPPOEFRecNum.add(Integer.valueOf(((ArrayList) ar3.result).size()));
                        this.mPhoneBookRecords.addAll((ArrayList) ar3.result);
                        if (!CsimPhbUtil.hasModemPhbEnhanceCapability(this.mFh)) {
                            CsimPhbUtil.initPhbStorage((ArrayList) ar3.result);
                        }
                        log("Loading USIM ADN records " + this.mPhoneBookRecords.size());
                    } else {
                        log("Loading USIM ADN records ar.result:" + ar3.result);
                    }
                    synchronized (this.mLock) {
                        this.mLock.notify();
                    }
                    return;
                case 3:
                    logi("Loading USIM IAP records done");
                    AsyncResult ar4 = (AsyncResult) msg.obj;
                    if (ar4.exception == null) {
                        this.mIapFileRecord = (ArrayList) ar4.result;
                    }
                    synchronized (this.mLock) {
                        this.mLock.notify();
                    }
                    return;
                case 4:
                    logi("Loading USIM Email records done");
                    AsyncResult ar5 = (AsyncResult) msg.obj;
                    if (ar5.exception == null) {
                        this.mEmailFileRecord = (ArrayList) ar5.result;
                    }
                    synchronized (this.mLock) {
                        this.mLock.notify();
                    }
                    return;
                case 5:
                    AsyncResult ar6 = (AsyncResult) msg.obj;
                    logi("EVENT_AAS_LOAD_DONE done pbr " + msg.arg1);
                    if (ar6.exception == null && (aasFileRecords = (ArrayList) ar6.result) != null) {
                        int size = aasFileRecords.size();
                        ArrayList<String> list = new ArrayList<>();
                        for (int i2 = 0; i2 < size; i2++) {
                            byte[] aas = aasFileRecords.get(i2);
                            if (aas == null) {
                                list.add(null);
                            } else {
                                list.add(IccUtils.adnStringFieldToString(aas, 0, aas.length));
                            }
                        }
                        this.mAasForAnr = list;
                    }
                    synchronized (this.mLock) {
                        this.mLock.notify();
                    }
                    return;
                case 6:
                    logi("Load UPB GAS done");
                    AsyncResult ar7 = (AsyncResult) msg.obj;
                    if (ar7.exception == null && (gasList = (String[]) ar7.result) != null && gasList.length > 0) {
                        this.mGasForGrp = new ArrayList<>();
                        for (int i3 = 0; i3 < gasList.length; i3++) {
                            String gas = decodeGas(gasList[i3]);
                            this.mGasForGrp.add(new UsimGroup(i3 + 1, gas));
                            log("Load UPB GAS done i is " + i3 + ", gas is " + gas);
                        }
                    }
                    synchronized (this.mGasLock) {
                        this.mGasLock.notify();
                    }
                    return;
                case 7:
                    logi("Updating USIM IAP records done");
                    if (((AsyncResult) msg.obj).exception == null) {
                        log("Updating USIM IAP records successfully!");
                        return;
                    }
                    return;
                case 8:
                    logi("Updating USIM Email records done");
                    AsyncResult ar8 = (AsyncResult) msg.obj;
                    if (ar8.exception == null) {
                        log("Updating USIM Email records successfully!");
                        this.mRefreshEmailInfo = true;
                    } else {
                        Rlog.e(LOG_TAG, "EVENT_EMAIL_UPDATE_DONE exception", ar8.exception);
                    }
                    synchronized (this.mLock) {
                        this.mLock.notify();
                    }
                    return;
                case 9:
                    logi("Updating USIM ANR records done");
                    AsyncResult ar9 = (AsyncResult) msg.obj;
                    IccIoResult res = (IccIoResult) ar9.result;
                    if (ar9.exception != null) {
                        Rlog.e(LOG_TAG, "EVENT_ANR_UPDATE_DONE exception", ar9.exception);
                    } else if (res == null) {
                        this.mRefreshAnrInfo = true;
                    } else if (res.getException() == null) {
                        log("Updating USIM ANR records successfully!");
                        this.mRefreshAnrInfo = true;
                    }
                    synchronized (this.mLock) {
                        this.mLock.notify();
                    }
                    return;
                case 10:
                    logi("EVENT_AAS_UPDATE_DONE done.");
                    synchronized (this.mAasLock) {
                        this.mAasLock.notify();
                    }
                    return;
                case 11:
                    logi("update UPB SNE done");
                    AsyncResult ar10 = (AsyncResult) msg.obj;
                    if (ar10.exception != null) {
                        Rlog.e(LOG_TAG, "EVENT_SNE_UPDATE_DONE exception", ar10.exception);
                        CommandException e = ar10.exception;
                        if (e.getCommandError() == CommandException.Error.OEM_ERROR_2) {
                            this.mResult = -40;
                        } else if (e.getCommandError() == CommandException.Error.OEM_ERROR_3) {
                            this.mResult = -30;
                        } else {
                            this.mResult = -50;
                        }
                    } else {
                        this.mResult = 0;
                    }
                    synchronized (this.mLock) {
                        this.mLock.notify();
                    }
                    return;
                case 12:
                    logi("update UPB GRP done");
                    if (((AsyncResult) msg.obj).exception == null) {
                        this.mResult = 0;
                    } else {
                        this.mResult = -1;
                    }
                    synchronized (this.mLock) {
                        this.mLock.notify();
                    }
                    return;
                case 13:
                    logi("update UPB GAS done");
                    AsyncResult ar11 = (AsyncResult) msg.obj;
                    if (ar11.exception == null) {
                        this.mResult = 0;
                    } else {
                        CommandException e2 = ar11.exception;
                        if (e2.getCommandError() == CommandException.Error.OEM_ERROR_2) {
                            this.mResult = -10;
                        } else if (e2.getCommandError() == CommandException.Error.OEM_ERROR_3) {
                            this.mResult = -20;
                        } else {
                            this.mResult = -1;
                        }
                    }
                    logi("update UPB GAS done mResult is " + this.mResult);
                    synchronized (this.mGasLock) {
                        this.mGasLock.notify();
                    }
                    return;
                case 14:
                    AsyncResult ar12 = (AsyncResult) msg.obj;
                    int[] userData = (int[]) ar12.userObj;
                    IccIoResult re = (IccIoResult) ar12.result;
                    boolean isNotify = this.mNeedNotify.get();
                    if (!(re == null || this.mIapFileList == null || re.getException() != null)) {
                        log("Loading USIM Iap record done result is " + IccUtils.bytesToHexString(re.payload));
                        try {
                            ArrayList<byte[]> iapList = this.mIapFileList.get(userData[0]);
                            if (iapList.size() > 0) {
                                iapList.set(userData[1], re.payload);
                            } else {
                                Rlog.w(LOG_TAG, "Warning: IAP size is 0");
                            }
                        } catch (IndexOutOfBoundsException e3) {
                            Rlog.e(LOG_TAG, "Index out of bounds.");
                        }
                    }
                    this.mReadingIapNum.decrementAndGet();
                    log("haman, mReadingIapNum when load done after minus: " + this.mReadingIapNum.get() + ",mNeedNotify " + this.mNeedNotify.get() + ", Iap pbr:" + userData[0] + ", adn i:" + userData[1]);
                    if (this.mReadingIapNum.get() == 0) {
                        if (this.mNeedNotify.get()) {
                            this.mNeedNotify.set(false);
                            synchronized (this.mLock) {
                                this.mLock.notify();
                            }
                        }
                        logi("EVENT_IAP_RECORD_LOAD_DONE end mLock.notify:" + isNotify);
                        return;
                    }
                    return;
                case 15:
                    AsyncResult ar13 = (AsyncResult) msg.obj;
                    int[] userData2 = (int[]) ar13.userObj;
                    IccIoResult em = (IccIoResult) ar13.result;
                    log("Loading USIM email record done email index:" + userData2[0] + ", adn i:" + userData2[1]);
                    if (em != null && em.getException() == null) {
                        updatePhoneAdnRecordWithEmailByIndex(userData2[0], userData2[1], em.payload);
                    }
                    this.mReadingEmailNum.decrementAndGet();
                    log("haman, mReadingEmailNum when load done after minus: " + this.mReadingEmailNum.get() + ", mNeedNotify:" + this.mNeedNotify.get());
                    if (this.mReadingEmailNum.get() == 0) {
                        if (this.mNeedNotify.get()) {
                            this.mNeedNotify.set(false);
                            synchronized (this.mLock) {
                                this.mLock.notify();
                            }
                        }
                        logi("EVENT_EMAIL_RECORD_LOAD_DONE end mLock.notify");
                        return;
                    }
                    return;
                case 16:
                    AsyncResult ar14 = (AsyncResult) msg.obj;
                    int[] userData3 = (int[]) ar14.userObj;
                    IccIoResult result = (IccIoResult) ar14.result;
                    if (result != null && result.getException() == null) {
                        updatePhoneAdnRecordWithAnrByIndex(userData3[0], userData3[1], userData3[2], result.payload);
                    }
                    this.mReadingAnrNum.decrementAndGet();
                    log("haman, mReadingAnrNum when load done after minus: " + this.mReadingAnrNum.get() + ", mNeedNotify:" + this.mNeedNotify.get());
                    if (this.mReadingAnrNum.get() == 0) {
                        if (this.mNeedNotify.get()) {
                            this.mNeedNotify.set(false);
                            synchronized (this.mLock) {
                                this.mLock.notify();
                            }
                        }
                        logi("EVENT_ANR_RECORD_LOAD_DONE end mLock.notify");
                        return;
                    }
                    return;
                case 17:
                    AsyncResult ar15 = (AsyncResult) msg.obj;
                    int[] userData4 = (int[]) ar15.userObj;
                    boolean isNotify2 = this.mNeedNotify.get();
                    if (ar15.result != null) {
                        int[] grpIds = (int[]) ar15.result;
                        if (grpIds.length > 0) {
                            updatePhoneAdnRecordWithGrpByIndex(userData4[0], userData4[1], grpIds);
                        }
                    }
                    this.mReadingGrpNum.decrementAndGet();
                    log("haman, mReadingGrpNum when load done after minus: " + this.mReadingGrpNum.get() + ",mNeedNotify:" + isNotify2);
                    if (this.mReadingGrpNum.get() == 0) {
                        if (this.mNeedNotify.get()) {
                            this.mNeedNotify.set(false);
                            synchronized (this.mLock) {
                                this.mLock.notify();
                            }
                        }
                        logi("EVENT_GRP_RECORD_LOAD_DONE end mLock.notify:" + isNotify2);
                        return;
                    }
                    return;
                case 18:
                    logi("Loading USIM SNE record done");
                    AsyncResult ar16 = (AsyncResult) msg.obj;
                    int[] userData5 = (int[]) ar16.userObj;
                    IccIoResult r = (IccIoResult) ar16.result;
                    if (r != null && r.getException() == null) {
                        log("Loading USIM SNE record done result is " + IccUtils.bytesToHexString(r.payload));
                        updatePhoneAdnRecordWithSneByIndex(userData5[0], userData5[1], r.payload);
                    }
                    this.mReadingSneNum.decrementAndGet();
                    log("haman, mReadingSneNum when load done after minus: " + this.mReadingSneNum.get() + ",mNeedNotify:" + this.mNeedNotify.get());
                    if (this.mReadingSneNum.get() == 0) {
                        if (this.mNeedNotify.get()) {
                            this.mNeedNotify.set(false);
                            synchronized (this.mLock) {
                                this.mLock.notify();
                            }
                        }
                        logi("EVENT_SNE_RECORD_LOAD_DONE end mLock.notify");
                        return;
                    }
                    return;
                case 19:
                    logi("Query UPB capability done");
                    AsyncResult ar17 = (AsyncResult) msg.obj;
                    if (ar17.exception == null) {
                        this.mUpbCap = (int[]) ar17.result;
                    }
                    synchronized (this.mUPBCapabilityLock) {
                        this.mUPBCapabilityLock.notify();
                    }
                    return;
                case 20:
                    AsyncResult ar18 = (AsyncResult) msg.obj;
                    if (ar18.exception == null) {
                        this.mEfData = (EFResponseData) ar18.result;
                    } else {
                        Rlog.w(LOG_TAG, "Select EF file fail" + ar18.exception);
                    }
                    synchronized (this.mLock) {
                        this.mLock.notify();
                    }
                    return;
                case 21:
                    logi("EVENT_QUERY_PHB_ADN_INFO");
                    AsyncResult ar19 = (AsyncResult) msg.obj;
                    if (ar19.exception == null) {
                        int[] info = (int[]) ar19.result;
                        if (info == null || info.length != 4) {
                            this.mAdnRecordSize = new int[4];
                            int[] iArr = this.mAdnRecordSize;
                            iArr[0] = 0;
                            iArr[1] = 0;
                            iArr[2] = 0;
                            iArr[3] = 0;
                        } else {
                            this.mAdnRecordSize = new int[4];
                            int[] iArr2 = this.mAdnRecordSize;
                            iArr2[0] = info[0];
                            iArr2[1] = info[1];
                            iArr2[2] = info[2];
                            iArr2[3] = info[3];
                            log("recordSize[0]=" + this.mAdnRecordSize[0] + ",recordSize[1]=" + this.mAdnRecordSize[1] + ",recordSize[2]=" + this.mAdnRecordSize[2] + ",recordSize[3]=" + this.mAdnRecordSize[3]);
                        }
                    }
                    synchronized (this.mLock) {
                        this.mLock.notify();
                    }
                    return;
                case EVENT_EMAIL_RECORD_LOAD_OPTMZ_DONE /* 22 */:
                    AsyncResult ar20 = (AsyncResult) msg.obj;
                    int[] userData6 = (int[]) ar20.userObj;
                    String emailResult = (String) ar20.result;
                    boolean isNotify3 = this.mNeedNotify.get();
                    if (emailResult != null && ar20.exception == null) {
                        log("Loading USIM Email record done result is " + emailResult);
                        updatePhoneAdnRecordWithEmailByIndexOptmz(userData6[0], userData6[1], emailResult);
                    }
                    this.mReadingEmailNum.decrementAndGet();
                    log("haman, mReadingEmailNum when load done after minus: " + this.mReadingEmailNum.get() + ", mNeedNotify:" + this.mNeedNotify.get() + ", email index:" + userData6[0] + ", adn i:" + userData6[1]);
                    if (this.mReadingEmailNum.get() == 0) {
                        if (this.mNeedNotify.get()) {
                            this.mNeedNotify.set(false);
                            synchronized (this.mLock) {
                                this.mLock.notify();
                            }
                        }
                        logi("EVENT_EMAIL_RECORD_LOAD_OPTMZ_DONE end mLock.notify:" + isNotify3);
                        return;
                    }
                    return;
                case EVENT_ANR_RECORD_LOAD_OPTMZ_DONE /* 23 */:
                    AsyncResult ar21 = (AsyncResult) msg.obj;
                    int[] userData7 = (int[]) ar21.userObj;
                    PhbEntry[] anrResult = (PhbEntry[]) ar21.result;
                    boolean isNotify4 = this.mNeedNotify.get();
                    if (anrResult != null && ar21.exception == null) {
                        log("Loading USIM Anr record done result is " + anrResult[0]);
                        updatePhoneAdnRecordWithAnrByIndexOptmz(userData7[0], userData7[1], userData7[2], anrResult[0]);
                    }
                    this.mReadingAnrNum.decrementAndGet();
                    log("haman, mReadingAnrNum when load done after minus: " + this.mReadingAnrNum.get() + ", mNeedNotify:" + this.mNeedNotify.get() + ", anr index:" + userData7[2] + ", adn i:" + userData7[1]);
                    if (this.mReadingAnrNum.get() == 0) {
                        if (this.mNeedNotify.get()) {
                            this.mNeedNotify.set(false);
                            synchronized (this.mLock) {
                                this.mLock.notify();
                            }
                        }
                        logi("EVENT_ANR_RECORD_LOAD_OPTMZ_DONE end mLock.notify:" + isNotify4);
                        return;
                    }
                    return;
                case EVENT_SNE_RECORD_LOAD_OPTMZ_DONE /* 24 */:
                    AsyncResult ar22 = (AsyncResult) msg.obj;
                    int[] userData8 = (int[]) ar22.userObj;
                    String sneResult = (String) ar22.result;
                    boolean isNotify5 = this.mNeedNotify.get();
                    if (sneResult != null && ar22.exception == null) {
                        String sneResult2 = decodeGas(sneResult);
                        log("Loading USIM Sne record done result is " + sneResult2);
                        updatePhoneAdnRecordWithSneByIndexOptmz(userData8[1], sneResult2);
                    }
                    this.mReadingSneNum.decrementAndGet();
                    log("haman, mReadingSneNum when load done after minus: " + this.mReadingSneNum.get() + ", mNeedNotify:" + this.mNeedNotify.get() + ", sne index:" + userData8[0] + ", adn i:" + userData8[1]);
                    if (this.mReadingSneNum.get() == 0) {
                        if (this.mNeedNotify.get()) {
                            this.mNeedNotify.set(false);
                            synchronized (this.mLock) {
                                this.mLock.notify();
                            }
                        }
                        logi("EVENT_SNE_RECORD_LOAD_OPTMZ_DONE end mLock.notify:" + isNotify5);
                        return;
                    }
                    return;
                case EVENT_QUERY_EMAIL_AVAILABLE_OPTMZ_DONE /* 25 */:
                    AsyncResult ar23 = (AsyncResult) msg.obj;
                    if (ar23.exception == null) {
                        this.mEmailInfo = (int[]) ar23.result;
                        if (this.mEmailInfo == null) {
                            log("mEmailInfo Null!");
                        } else {
                            logi("mEmailInfo = " + this.mEmailInfo[0] + " " + this.mEmailInfo[1] + " " + this.mEmailInfo[2]);
                        }
                    }
                    synchronized (this.mLock) {
                        this.mLock.notify();
                    }
                    return;
                case EVENT_QUERY_ANR_AVAILABLE_OPTMZ_DONE /* 26 */:
                    AsyncResult ar24 = (AsyncResult) msg.obj;
                    int[] tmpAnrInfo = (int[]) ar24.result;
                    if (ar24.exception == null) {
                        if (tmpAnrInfo == null) {
                            log("tmpAnrInfo Null!");
                        } else {
                            logi("tmpAnrInfo = " + tmpAnrInfo[0] + " " + tmpAnrInfo[1] + " " + tmpAnrInfo[2]);
                            ArrayList<int[]> arrayList = this.mAnrInfo;
                            if (arrayList == null) {
                                this.mAnrInfo = new ArrayList<>();
                            } else if (arrayList.size() > 0) {
                                this.mAnrInfo.clear();
                            }
                            this.mAnrInfo.add(tmpAnrInfo);
                        }
                    }
                    synchronized (this.mLock) {
                        this.mLock.notify();
                    }
                    return;
                case EVENT_QUERY_SNE_AVAILABLE_OPTMZ_DONE /* 27 */:
                    AsyncResult ar25 = (AsyncResult) msg.obj;
                    if (ar25.exception == null) {
                        this.mSneInfo = (int[]) ar25.result;
                        if (this.mSneInfo == null) {
                            log("mSneInfo Null!");
                        } else {
                            logi("mSneInfo = " + this.mSneInfo[0] + " " + this.mSneInfo[1] + " " + this.mSneInfo[2]);
                        }
                    }
                    synchronized (this.mLock) {
                        this.mLock.notify();
                    }
                    return;
                case EVENT_AAS_LOAD_DONE_OPTMZ /* 28 */:
                    logi("Load UPB AAS done");
                    AsyncResult ar26 = (AsyncResult) msg.obj;
                    if (ar26.exception == null && (aasList = (String[]) ar26.result) != null && aasList.length > 0) {
                        this.mAasForAnr = new ArrayList<>();
                        for (int i4 = 0; i4 < aasList.length; i4++) {
                            String aas2 = decodeGas(aasList[i4]);
                            this.mAasForAnr.add(aas2);
                            log("Load UPB AAS done i is " + i4 + ", aas is " + aas2);
                        }
                    }
                    synchronized (this.mLock) {
                        this.mLock.notify();
                    }
                    return;
                default:
                    Rlog.e(LOG_TAG, "UnRecognized Message : " + msg.what);
                    return;
            }
        } else {
            AsyncResult ar27 = (AsyncResult) msg.obj;
            logi("EVENT_EXT1_LOAD_DONE done pbr " + msg.arg1);
            if (ar27.exception == null && (record = (ArrayList) ar27.result) != null) {
                log("EVENT_EXT1_LOAD_DONE done size " + record.size());
                if (this.mExt1FileList == null) {
                    this.mExt1FileList = new ArrayList<>();
                }
                this.mExt1FileList.add(record);
            }
            synchronized (this.mLock) {
                this.mLock.notify();
            }
        }
    }

    /* access modifiers changed from: private */
    public class PbrRecord {
        private int mAnrIndex = 0;
        private SparseArray<File> mFileIds = new SparseArray<>();
        private int mMasterFileRecordNum;

        PbrRecord(byte[] record) {
            MtkUsimPhoneBookManager.this.logi("PBR rec: " + IccUtils.bytesToHexString(record));
            parseTag(new SimTlv(record, 0, record.length));
        }

        /* access modifiers changed from: package-private */
        public void parseTag(SimTlv tlv) {
            do {
                int tag = tlv.getTag();
                switch (tag) {
                    case 168:
                    case 169:
                    case 170:
                        byte[] data = tlv.getData();
                        parseEfAndSFI(new SimTlv(data, 0, data.length), tag);
                        break;
                }
            } while (tlv.nextObject());
            MtkUsimPhoneBookManager.access$408(MtkUsimPhoneBookManager.this);
        }

        /* access modifiers changed from: package-private */
        public void parseEfAndSFI(SimTlv tlv, int parentTag) {
            int sfi;
            int tag;
            int tagNumberWithinParentTag = 0;
            do {
                int tag2 = tlv.getTag();
                switch (tag2) {
                    case 192:
                    case 193:
                    case 194:
                    case 195:
                    case 196:
                    case 197:
                    case 198:
                    case 199:
                    case 200:
                    case 201:
                    case ExternalSimConstants.EVENT_TYPE_RECEIVE_RSIM_AUTH_RSP /* 202 */:
                    case ExternalSimConstants.EVENT_TYPE_RSIM_AUTH_DONE /* 203 */:
                        byte[] data = tlv.getData();
                        if (data.length >= 2 && data.length <= 3) {
                            if (data.length == 3) {
                                sfi = data[2] & PplMessageManager.Type.INVALID;
                            } else {
                                sfi = -1;
                            }
                            int efid = ((data[0] & PplMessageManager.Type.INVALID) << 8) | (data[1] & PplMessageManager.Type.INVALID);
                            if (tag2 == 196) {
                                int i = this.mAnrIndex;
                                this.mAnrIndex = i + 1;
                                tag = tag2 + (i * 256);
                            } else {
                                tag = tag2;
                            }
                            File object = new File(parentTag, efid, sfi, tagNumberWithinParentTag);
                            object.mTag = tag;
                            object.mPbrRecord = MtkUsimPhoneBookManager.this.mSliceCount;
                            MtkUsimPhoneBookManager.this.logi("pbr " + object);
                            this.mFileIds.put(tag, object);
                            break;
                        } else {
                            Rlog.w(MtkUsimPhoneBookManager.LOG_TAG, "Invalid TLV length: " + data.length);
                            break;
                        }
                        break;
                }
                tagNumberWithinParentTag++;
            } while (tlv.nextObject());
        }
    }

    private void queryUpbCapablityAndWait() {
        logi("queryUpbCapablityAndWait begin");
        synchronized (this.mUPBCapabilityLock) {
            for (int i = 0; i < 8; i++) {
                this.mUpbCap[i] = -1;
            }
            if (checkIsPhbReady()) {
                this.mCi.queryUPBCapability(obtainMessage(19));
                try {
                    this.mUPBCapabilityLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in queryUpbCapablityAndWait");
                }
            }
        }
        logi("queryUpbCapablityAndWait done:N_Anr :" + this.mUpbCap[0] + ",N_Email:" + this.mUpbCap[1] + ",N_Sne:" + this.mUpbCap[2] + ",N_Aas:" + this.mUpbCap[3] + ",L_Aas:" + this.mUpbCap[4] + ",N_Gas:" + this.mUpbCap[5] + ",L_Gas:" + this.mUpbCap[6] + ",N_Grp:" + this.mUpbCap[7]);
    }

    private void readGasListAndWait() {
        logi("readGasListAndWait begin");
        synchronized (this.mGasLock) {
            if (this.mUpbCap[5] <= 0) {
                log("readGasListAndWait no need to read. return");
                return;
            }
            this.mCi.readUPBGasList(1, this.mUpbCap[5], obtainMessage(6));
            try {
                this.mGasLock.wait();
            } catch (InterruptedException e) {
                Rlog.e(LOG_TAG, "Interrupted Exception in readGasListAndWait");
            }
            logi("readGasListAndWait end");
        }
    }

    private void updatePhoneAdnRecordWithAnrByIndex(int recId, int adnIndex, int anrIndex, byte[] anrRecData) {
        String anr;
        ArrayList<String> aasList;
        log("updatePhoneAdnRecordWithAnrByIndex the " + adnIndex + "th anr record is " + IccUtils.bytesToHexString(anrRecData));
        byte b = anrRecData[1];
        byte b2 = anrRecData[0];
        if (b > 0 && b <= 11 && (anr = MtkPhoneNumberUtils.calledPartyBCDToString(anrRecData, 2, anrRecData[1])) != null && !anr.equals("")) {
            String aas = null;
            if (!(b2 <= 0 || b2 == 255 || this.mAasForAnr == null || (aasList = this.mAasForAnr) == null || b2 > aasList.size())) {
                aas = aasList.get(b2 - 1);
            }
            logi(" updatePhoneAdnRecordWithAnrByIndex " + adnIndex + " th anr is " + anr + " the anrIndex is " + anrIndex);
            try {
                MtkAdnRecord rec = this.mPhoneBookRecords.get(adnIndex);
                rec.setAnr(anr, anrIndex);
                if (aas != null && aas.length() > 0) {
                    rec.setAasIndex(b2);
                }
                this.mPhoneBookRecords.set(adnIndex, rec);
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "updatePhoneAdnRecordWithAnrByIndex: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is " + this.mPhoneBookRecords.size() + "index is " + adnIndex);
            }
        }
    }

    public ArrayList<UsimGroup> getUsimGroups() {
        logi("getUsimGroups begin");
        synchronized (this.mGasLock) {
            if (!this.mGasForGrp.isEmpty()) {
                return this.mGasForGrp;
            }
            queryUpbCapablityAndWait();
            readGasListAndWait();
            logi("getUsimGroups end");
            return this.mGasForGrp;
        }
    }

    public String getUsimGroupById(int nGasId) {
        UsimGroup uGas;
        String grpName = null;
        logi("getUsimGroupById nGasId is " + nGasId);
        ArrayList<UsimGroup> arrayList = this.mGasForGrp;
        if (!(arrayList == null || nGasId > arrayList.size() || (uGas = this.mGasForGrp.get(nGasId - 1)) == null)) {
            grpName = uGas.getAlphaTag();
            log("getUsimGroupById index is " + uGas.getRecordIndex() + ", name is " + grpName);
        }
        logi("getUsimGroupById grpName is " + grpName);
        return grpName;
    }

    public synchronized boolean removeUsimGroupById(int nGasId) {
        boolean ret;
        Throwable th;
        ret = false;
        logi("removeUsimGroupById nGasId is " + nGasId);
        synchronized (this.mGasLock) {
            try {
                if (this.mGasForGrp != null) {
                    try {
                        if (nGasId <= this.mGasForGrp.size()) {
                            UsimGroup uGas = this.mGasForGrp.get(nGasId - 1);
                            if (uGas != null) {
                                log(" removeUsimGroupById index is " + uGas.getRecordIndex());
                            }
                            if (uGas == null || uGas.getAlphaTag() == null) {
                                Rlog.w(LOG_TAG, "removeUsimGroupById fail: this gas doesn't exist ");
                                logi("removeUsimGroupById result is " + ret);
                            } else {
                                this.mCi.deleteUPBEntry(4, 0, nGasId, obtainMessage(13));
                                try {
                                    this.mGasLock.wait();
                                } catch (InterruptedException e) {
                                    Rlog.e(LOG_TAG, "Interrupted Exception in removeUsimGroupById");
                                }
                                if (this.mResult == 0) {
                                    ret = true;
                                    uGas.setAlphaTag(null);
                                    this.mGasForGrp.set(nGasId - 1, uGas);
                                }
                                logi("removeUsimGroupById result is " + ret);
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
                Rlog.e(LOG_TAG, "removeUsimGroupById fail ");
                logi("removeUsimGroupById result is " + ret);
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        return ret;
    }

    private String decodeGas(String srcGas) {
        StringBuilder sb = new StringBuilder();
        sb.append("[decodeGas] gas string is ");
        sb.append(srcGas == null ? "null" : srcGas);
        log(sb.toString());
        if (srcGas == null || TextUtils.isEmpty(srcGas) || srcGas.length() % 2 != 0) {
            return null;
        }
        try {
            byte[] ba = IccUtils.hexStringToBytes(srcGas);
            if (ba != null) {
                return new String(ba, 0, srcGas.length() / 2, "utf-16be");
            }
            Rlog.w(LOG_TAG, "gas string is null");
            return null;
        } catch (UnsupportedEncodingException ex) {
            Rlog.e(LOG_TAG, "[decodeGas] implausible UnsupportedEncodingException", ex);
            return null;
        } catch (RuntimeException ex2) {
            Rlog.e(LOG_TAG, "[decodeGas] RuntimeException", ex2);
            return null;
        }
    }

    private String encodeToUcs2(String input) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            String hexInt = Integer.toHexString(input.charAt(i));
            for (int j = 0; j < 4 - hexInt.length(); j++) {
                output.append("0");
            }
            output.append(hexInt);
        }
        return output.toString();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00a6, code lost:
        r2 = th;
     */
    public synchronized int insertUsimGroup(String grpName) {
        int index = -1;
        logi("insertUsimGroup grpName");
        synchronized (this.mGasLock) {
            if (this.mGasForGrp != null) {
                if (this.mGasForGrp.size() != 0) {
                    UsimGroup gasEntry = null;
                    int i = 0;
                    while (true) {
                        if (i < this.mGasForGrp.size()) {
                            gasEntry = this.mGasForGrp.get(i);
                            if (gasEntry != null && gasEntry.getAlphaTag() == null) {
                                index = gasEntry.getRecordIndex();
                                log("insertUsimGroup index is " + index);
                                break;
                            }
                            i++;
                        } else {
                            break;
                        }
                    }
                    if (index < 0) {
                        Rlog.w(LOG_TAG, "insertUsimGroup fail: gas file is full.");
                        return -20;
                    }
                    this.mCi.editUPBEntry(4, 0, index, encodeToUcs2(grpName), null, obtainMessage(13));
                    try {
                        this.mGasLock.wait();
                    } catch (InterruptedException e) {
                        Rlog.e(LOG_TAG, "Interrupted Exception in insertUsimGroup");
                    }
                    if (this.mResult < 0) {
                        Rlog.e(LOG_TAG, "result is negative. insertUsimGroup");
                        return this.mResult;
                    }
                    gasEntry.setAlphaTag(grpName);
                    this.mGasForGrp.set(i, gasEntry);
                    return index;
                }
            }
            Rlog.w(LOG_TAG, "insertUsimGroup fail ");
            return index;
        }
        while (true) {
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0092, code lost:
        r2 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0058  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0089  */
    public synchronized int updateUsimGroup(int nGasId, String grpName) {
        int ret;
        logi("updateUsimGroup nGasId is " + nGasId);
        synchronized (this.mGasLock) {
            this.mResult = -1;
            if (this.mGasForGrp != null) {
                if (nGasId <= this.mGasForGrp.size()) {
                    if (grpName != null) {
                        this.mCi.editUPBEntry(4, 0, nGasId, encodeToUcs2(grpName), null, obtainMessage(13));
                        try {
                            this.mGasLock.wait();
                        } catch (InterruptedException e) {
                            Rlog.e(LOG_TAG, "Interrupted Exception in updateUsimGroup");
                        }
                    }
                    if (this.mResult != 0) {
                        ret = nGasId;
                        UsimGroup uGasEntry = this.mGasForGrp.get(nGasId - 1);
                        if (uGasEntry != null) {
                            log("updateUsimGroup index is " + uGasEntry.getRecordIndex());
                            uGasEntry.setAlphaTag(grpName);
                        } else {
                            Rlog.w(LOG_TAG, "updateUsimGroup the entry doesn't exist ");
                        }
                    } else {
                        ret = this.mResult;
                    }
                }
            }
            Rlog.w(LOG_TAG, "updateUsimGroup fail ");
            if (this.mResult != 0) {
            }
        }
        return ret;
        while (true) {
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00b2, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00b3, code lost:
        r16 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        android.telephony.Rlog.d(com.mediatek.internal.telephony.phb.MtkUsimPhoneBookManager.LOG_TAG, r0.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00bf, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00c0, code lost:
        r16 = r4;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00b2 A[ExcHandler: Exception (r0v33 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:24:0x00a3] */
    public boolean addContactToGroup(int adnIndex, int grpIndex) {
        boolean ret;
        boolean ret2;
        IndexOutOfBoundsException e;
        int grpMaxCount;
        int nOrder;
        boolean ret3 = false;
        logi("addContactToGroup begin adnIndex is " + adnIndex + " to grp " + grpIndex);
        ArrayList<MtkAdnRecord> arrayList = this.mPhoneBookRecords;
        if (arrayList != null && adnIndex > 0) {
            if (adnIndex <= arrayList.size()) {
                synchronized (this.mLock) {
                    try {
                        MtkAdnRecord rec = this.mPhoneBookRecords.get(adnIndex - 1);
                        if (rec != null) {
                            try {
                                log(" addContactToGroup the adn index is " + rec.getRecId() + " old grpList is " + rec.getGrpIds());
                                String grpList = rec.getGrpIds();
                                boolean bExist = false;
                                int grpCount = this.mUpbCap[7];
                                if (this.mUpbCap[7] > this.mUpbCap[5]) {
                                    try {
                                        grpMaxCount = this.mUpbCap[5];
                                    } catch (Throwable th) {
                                        e = th;
                                    }
                                } else {
                                    grpMaxCount = this.mUpbCap[7];
                                }
                                int[] grpIdArray = new int[grpCount];
                                for (int i = 0; i < grpCount; i++) {
                                    grpIdArray[i] = 0;
                                }
                                if (grpList != null) {
                                    String[] grpIds = rec.getGrpIds().split(",");
                                    int i2 = 0;
                                    nOrder = -1;
                                    while (true) {
                                        if (i2 >= grpMaxCount) {
                                            ret2 = ret3;
                                            break;
                                        }
                                        try {
                                            grpIdArray[i2] = Integer.parseInt(grpIds[i2]);
                                            ret2 = ret3;
                                        } catch (NumberFormatException e2) {
                                            NumberFormatException e3 = e2;
                                            ret2 = ret3;
                                            Rlog.d(LOG_TAG, e3.toString());
                                        } catch (Exception e4) {
                                        } catch (Throwable th2) {
                                            e = th2;
                                            throw e;
                                        }
                                        if (grpIndex == grpIdArray[i2]) {
                                            log(" addContactToGroup the adn is already in the group. i is " + i2);
                                            bExist = true;
                                            break;
                                        }
                                        if (nOrder < 0 && (grpIdArray[i2] == 0 || grpIdArray[i2] == 255)) {
                                            log(" addContactToGroup found an unsed position in the group list. i is " + i2);
                                            nOrder = i2;
                                        }
                                        i2++;
                                        ret3 = ret2;
                                    }
                                } else {
                                    ret2 = false;
                                    nOrder = 0;
                                }
                                if (!bExist && nOrder >= 0) {
                                    grpIdArray[nOrder] = grpIndex;
                                    this.mCi.writeUPBGrpEntry(adnIndex, grpIdArray, obtainMessage(12));
                                    try {
                                        this.mLock.wait();
                                    } catch (InterruptedException e5) {
                                        Rlog.e(LOG_TAG, "Interrupted Exception in addContactToGroup");
                                    }
                                    if (this.mResult == 0) {
                                        ret = true;
                                        updatePhoneAdnRecordWithGrpByIndex(adnIndex - 1, adnIndex, grpIdArray);
                                        logi(" addContactToGroup the adn index is " + rec.getRecId());
                                        this.mResult = -1;
                                        logi("addContactToGroup end adnIndex is " + adnIndex + " to grp " + grpIndex);
                                        return ret;
                                    }
                                }
                            } catch (Throwable th3) {
                                e = th3;
                                throw e;
                            }
                        } else {
                            ret2 = false;
                        }
                        ret = ret2;
                        logi("addContactToGroup end adnIndex is " + adnIndex + " to grp " + grpIndex);
                        return ret;
                    } catch (IndexOutOfBoundsException e6) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("addContactToGroup: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is ");
                        sb.append(this.mPhoneBookRecords.size());
                        sb.append("index is ");
                        sb.append(adnIndex - 1);
                        Rlog.e(LOG_TAG, sb.toString());
                        return false;
                    }
                }
            }
        }
        Rlog.e(LOG_TAG, "addContactToGroup no records or invalid index.");
        return false;
    }

    public synchronized boolean removeContactFromGroup(int adnIndex, int grpIndex) {
        Throwable th;
        boolean ret = false;
        logi("removeContactFromGroup begin adnIndex is " + adnIndex + " to grp " + grpIndex);
        if (this.mPhoneBookRecords != null && adnIndex > 0) {
            if (adnIndex <= this.mPhoneBookRecords.size()) {
                synchronized (this.mLock) {
                    try {
                        MtkAdnRecord rec = this.mPhoneBookRecords.get(adnIndex - 1);
                        if (rec != null) {
                            try {
                                String grpList = rec.getGrpIds();
                                if (grpList == null) {
                                    Rlog.e(LOG_TAG, " the adn is not in any group. ");
                                    return false;
                                }
                                String[] grpIds = grpList.split(",");
                                boolean bExist = false;
                                int nOrder = -1;
                                int[] grpIdArray = new int[grpIds.length];
                                for (int i = 0; i < grpIds.length; i++) {
                                    try {
                                        grpIdArray[i] = Integer.parseInt(grpIds[i]);
                                        if (grpIndex == grpIdArray[i]) {
                                            bExist = true;
                                            nOrder = i;
                                            log(" removeContactFromGroup the adn is in the group. i is " + i);
                                        }
                                    } catch (NumberFormatException e) {
                                        Rlog.d(LOG_TAG, e.toString());
                                    } catch (Exception e2) {
                                        Rlog.d(LOG_TAG, e2.toString());
                                    }
                                }
                                if (!bExist || nOrder < 0) {
                                    Rlog.e(LOG_TAG, " removeContactFromGroup the adn is not in the group. ");
                                } else {
                                    grpIdArray[nOrder] = 0;
                                    this.mCi.writeUPBGrpEntry(adnIndex, grpIdArray, obtainMessage(12));
                                    try {
                                        this.mLock.wait();
                                    } catch (InterruptedException e3) {
                                        Rlog.e(LOG_TAG, "Interrupted Exception in removeContactFromGroup");
                                    }
                                    if (this.mResult == 0) {
                                        ret = true;
                                        updatePhoneAdnRecordWithGrpByIndex(adnIndex - 1, adnIndex, grpIdArray);
                                        this.mResult = -1;
                                    }
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                        try {
                            logi("removeContactFromGroup end adnIndex is " + adnIndex + " to grp " + grpIndex);
                            return ret;
                        } catch (Throwable th3) {
                            th = th3;
                            throw th;
                        }
                    } catch (IndexOutOfBoundsException e4) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("removeContactFromGroup: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is ");
                        sb.append(this.mPhoneBookRecords.size());
                        sb.append("index is ");
                        sb.append(adnIndex - 1);
                        Rlog.e(LOG_TAG, sb.toString());
                        return false;
                    }
                }
            }
        }
        Rlog.e(LOG_TAG, "removeContactFromGroup no records or invalid index.");
        return false;
    }

    public boolean updateContactToGroups(int adnIndex, int[] grpIdList) {
        boolean ret = false;
        ArrayList<MtkAdnRecord> arrayList = this.mPhoneBookRecords;
        if (arrayList == null || adnIndex <= 0 || adnIndex > arrayList.size() || grpIdList == null) {
            Rlog.e(LOG_TAG, "updateContactToGroups no records or invalid index.");
            return false;
        }
        logi("updateContactToGroups begin grpIdList is " + adnIndex + " to grp list count " + grpIdList.length);
        synchronized (this.mLock) {
            MtkAdnRecord rec = this.mPhoneBookRecords.get(adnIndex - 1);
            if (rec != null) {
                log(" updateContactToGroups the adn index is " + rec.getRecId() + " old grpList is " + rec.getGrpIds());
                int grpCount = this.mUpbCap[7];
                if (grpIdList.length > grpCount) {
                    Rlog.e(LOG_TAG, "updateContactToGroups length of grpIdList > grpCount.");
                    return false;
                }
                int[] grpIdArray = new int[grpCount];
                int i = 0;
                while (i < grpCount) {
                    grpIdArray[i] = i < grpIdList.length ? grpIdList[i] : 0;
                    log("updateContactToGroups i:" + i + ",grpIdArray[" + i + "]:" + grpIdArray[i]);
                    i++;
                }
                this.mCi.writeUPBGrpEntry(adnIndex, grpIdArray, obtainMessage(12));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in updateContactToGroups");
                }
                if (this.mResult == 0) {
                    ret = true;
                    updatePhoneAdnRecordWithGrpByIndex(adnIndex - 1, adnIndex, grpIdArray);
                    logi(" updateContactToGroups the adn index is " + rec.getRecId());
                    this.mResult = -1;
                }
            }
            logi("updateContactToGroups end grpIdList is " + adnIndex + " to grp list count " + grpIdList.length);
            return ret;
        }
    }

    public boolean moveContactFromGroupsToGroups(int adnIndex, int[] fromGrpIdList, int[] toGrpIdList) {
        boolean ret = false;
        ArrayList<MtkAdnRecord> arrayList = this.mPhoneBookRecords;
        if (arrayList == null || adnIndex <= 0 || adnIndex > arrayList.size()) {
            Rlog.e(LOG_TAG, "moveContactFromGroupsToGroups no records or invalid index.");
            return false;
        }
        synchronized (this.mLock) {
            MtkAdnRecord rec = this.mPhoneBookRecords.get(adnIndex - 1);
            if (rec != null) {
                int grpCount = this.mUpbCap[7];
                int grpMaxCount = this.mUpbCap[7] > this.mUpbCap[5] ? this.mUpbCap[5] : this.mUpbCap[7];
                String grpIds = rec.getGrpIds();
                StringBuilder sb = new StringBuilder();
                sb.append(" moveContactFromGroupsToGroups the adn index is ");
                sb.append(rec.getRecId());
                sb.append(" original grpIds is ");
                sb.append(grpIds);
                sb.append(", fromGrpIdList: ");
                sb.append(fromGrpIdList == null ? "null" : fromGrpIdList);
                sb.append(", toGrpIdList: ");
                sb.append(toGrpIdList == null ? "null" : toGrpIdList);
                logi(sb.toString());
                int[] grpIdIntArray = new int[grpCount];
                for (int i = 0; i < grpCount; i++) {
                    grpIdIntArray[i] = 0;
                }
                if (grpIds != null) {
                    String[] grpIdStrArray = grpIds.split(",");
                    for (int i2 = 0; i2 < grpMaxCount; i2++) {
                        try {
                            grpIdIntArray[i2] = Integer.parseInt(grpIdStrArray[i2]);
                        } catch (NumberFormatException e) {
                            Rlog.d(LOG_TAG, e.toString());
                        } catch (Exception e2) {
                            Rlog.d(LOG_TAG, e2.toString());
                        }
                    }
                }
                if (fromGrpIdList != null) {
                    for (int i3 = 0; i3 < fromGrpIdList.length; i3++) {
                        for (int j = 0; j < grpMaxCount; j++) {
                            if (grpIdIntArray[j] == fromGrpIdList[i3]) {
                                grpIdIntArray[j] = 0;
                            }
                        }
                    }
                }
                if (toGrpIdList != null) {
                    for (int i4 = 0; i4 < toGrpIdList.length; i4++) {
                        boolean bEmpty = false;
                        boolean bExist = false;
                        int k = 0;
                        while (true) {
                            if (k >= grpMaxCount) {
                                break;
                            } else if (grpIdIntArray[k] == toGrpIdList[i4]) {
                                bExist = true;
                                break;
                            } else {
                                k++;
                            }
                        }
                        if (bExist) {
                            Rlog.w(LOG_TAG, "moveContactFromGroupsToGroups the adn isalready in the group.");
                        } else {
                            int j2 = 0;
                            while (true) {
                                if (j2 >= grpMaxCount) {
                                    break;
                                } else if (grpIdIntArray[j2] == 0 || grpIdIntArray[j2] == 255) {
                                    bEmpty = true;
                                    grpIdIntArray[j2] = toGrpIdList[i4];
                                } else {
                                    j2++;
                                }
                            }
                            if (!bEmpty) {
                                Rlog.e(LOG_TAG, "moveContactFromGroupsToGroups no empty to add.");
                                return false;
                            }
                        }
                    }
                }
                this.mCi.writeUPBGrpEntry(adnIndex, grpIdIntArray, obtainMessage(12));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e3) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in moveContactFromGroupsToGroups");
                }
                if (this.mResult == 0) {
                    ret = true;
                    updatePhoneAdnRecordWithGrpByIndex(adnIndex - 1, adnIndex, grpIdIntArray);
                    logi("moveContactFromGroupsToGroups the adn index is " + rec.getRecId());
                    this.mResult = -1;
                }
            }
            return ret;
        }
    }

    public boolean removeContactGroup(int adnIndex) {
        boolean ret = false;
        logi("removeContactsGroup adnIndex is " + adnIndex);
        ArrayList<MtkAdnRecord> arrayList = this.mPhoneBookRecords;
        if (arrayList == null || arrayList.isEmpty()) {
            return false;
        }
        synchronized (this.mLock) {
            try {
                MtkAdnRecord rec = this.mPhoneBookRecords.get(adnIndex - 1);
                if (rec == null) {
                    return false;
                }
                log("removeContactsGroup rec is " + rec);
                String grpList = rec.getGrpIds();
                if (grpList == null) {
                    return false;
                }
                String[] grpIds = grpList.split(",");
                boolean hasGroup = false;
                int i = 0;
                while (true) {
                    if (i >= grpIds.length) {
                        break;
                    }
                    try {
                        int value = Integer.parseInt(grpIds[i]);
                        if (value > 0 && value < 255) {
                            hasGroup = true;
                            break;
                        }
                    } catch (NumberFormatException e) {
                        Rlog.d(LOG_TAG, e.toString());
                    } catch (Exception e2) {
                        Rlog.d(LOG_TAG, e2.toString());
                    }
                    i++;
                }
                if (hasGroup) {
                    this.mCi.writeUPBGrpEntry(adnIndex, new int[0], obtainMessage(12));
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e3) {
                        Rlog.e(LOG_TAG, "Interrupted Exception in removeContactGroup");
                    }
                    if (this.mResult == 0) {
                        ret = true;
                        int[] grpIdArray = new int[grpIds.length];
                        for (int i2 = 0; i2 < grpIds.length; i2++) {
                            grpIdArray[i2] = 0;
                        }
                        updatePhoneAdnRecordWithGrpByIndex(adnIndex - 1, adnIndex, grpIdArray);
                        logi(" removeContactGroup the adn index is " + rec.getRecId());
                        this.mResult = -1;
                    }
                }
                return ret;
            } catch (IndexOutOfBoundsException e4) {
                StringBuilder sb = new StringBuilder();
                sb.append("removeContactGroup: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is ");
                sb.append(this.mPhoneBookRecords.size());
                sb.append("index is ");
                sb.append(adnIndex - 1);
                Rlog.e(LOG_TAG, sb.toString());
                return false;
            }
        }
    }

    public int hasExistGroup(String grpName) {
        int grpId = -1;
        logi("hasExistGroup grpName is " + grpName);
        if (grpName == null) {
            return -1;
        }
        ArrayList<UsimGroup> arrayList = this.mGasForGrp;
        if (arrayList != null && arrayList.size() > 0) {
            int i = 0;
            while (true) {
                if (i < this.mGasForGrp.size()) {
                    UsimGroup uGas = this.mGasForGrp.get(i);
                    if (uGas != null && grpName.equals(uGas.getAlphaTag())) {
                        log("getUsimGroupById index is " + uGas.getRecordIndex() + ", name is " + grpName);
                        grpId = uGas.getRecordIndex();
                        break;
                    }
                    i++;
                } else {
                    break;
                }
            }
        }
        logi("hasExistGroup grpId is " + grpId);
        return grpId;
    }

    public int getUsimGrpMaxNameLen() {
        int ret;
        logi("getUsimGrpMaxNameLen begin");
        synchronized (this.mUPBCapabilityLock) {
            if (checkIsPhbReady()) {
                if (this.mUpbCap[6] < 0) {
                    queryUpbCapablityAndWait();
                }
                ret = this.mUpbCap[6];
            } else {
                ret = -1;
            }
            logi("getUsimGrpMaxNameLen done: L_Gas is " + ret);
        }
        return ret;
    }

    public int getUsimGrpMaxCount() {
        int ret;
        logi("getUsimGrpMaxCount begin");
        synchronized (this.mUPBCapabilityLock) {
            if (checkIsPhbReady()) {
                if (this.mUpbCap[5] < 0) {
                    queryUpbCapablityAndWait();
                }
                ret = this.mUpbCap[5];
            } else {
                ret = -1;
            }
            logi("getUsimGrpMaxCount done: N_Gas is " + ret);
        }
        return ret;
    }

    private void log(String msg) {
        if (DBG) {
            Rlog.d(LOG_TAG, msg + "(slot " + this.mSlotId + ")");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logi(String msg) {
        Rlog.i(LOG_TAG, msg + "(slot " + this.mSlotId + ")");
    }

    public boolean isAnrCapacityFree(String anr, int adnIndex, int anrIndex, MtkAdnRecord oldAdn) {
        String oldAnr = null;
        if (oldAdn != null) {
            oldAnr = oldAdn.getAdditionalNumber(anrIndex);
        }
        if (anr == null || anr.equals("") || anrIndex < 0 || getUsimEfType(196) == 168 || (oldAnr != null && !oldAnr.equals(""))) {
            return true;
        }
        if (!CsimPhbUtil.hasModemPhbEnhanceCapability(this.mFh)) {
            int i = this.mAdnFileSize;
            int pbrRecNum = (adnIndex - 1) / i;
            int anrRecNum = (adnIndex - 1) % i;
            try {
                log("isAnrCapacityFree anr: " + anr);
                if (this.mRecordSize != null) {
                    if (this.mRecordSize.size() != 0) {
                        File anrFile = (File) this.mPbrRecords.get(pbrRecNum).mFileIds.get((anrIndex * 256) + 196);
                        if (anrFile == null) {
                            return false;
                        }
                        int size = this.mRecordSize.get(anrFile.getEfid())[2];
                        log("isAnrCapacityFree size: " + size);
                        if (size >= anrRecNum + 1) {
                            return true;
                        }
                        log("isAnrCapacityFree: anrRecNum out of size: " + anrRecNum);
                        return false;
                    }
                }
                log("isAnrCapacityFree: mAnrFileSize is empty");
                return false;
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "isAnrCapacityFree Index out of bounds.");
                return false;
            } catch (NullPointerException e2) {
                Rlog.e(LOG_TAG, "isAnrCapacityFree exception:" + e2.toString());
                return false;
            }
        } else {
            synchronized (this.mLock) {
                if (this.mAnrInfo == null || anrIndex >= this.mAnrInfo.size()) {
                    this.mCi.queryUPBAvailable(0, anrIndex + 1, obtainMessage(EVENT_QUERY_ANR_AVAILABLE_OPTMZ_DONE));
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e3) {
                        Rlog.e(LOG_TAG, "Interrupted Exception in isAnrCapacityFree");
                    }
                }
            }
            ArrayList<int[]> arrayList = this.mAnrInfo;
            if (arrayList == null || arrayList.get(anrIndex) == null || this.mAnrInfo.get(anrIndex)[1] <= 0) {
                return false;
            }
            return true;
        }
    }

    public void updateAnrByAdnIndex(String anr, int adnIndex, int anrIndex, MtkAdnRecord oldAdn) {
        String oldAnr;
        File anrFile;
        Object obj;
        Throwable th;
        Message msg;
        int i = this.mAdnFileSize;
        int pbrRecNum = (adnIndex - 1) / i;
        int anrRecNum = (adnIndex - 1) % i;
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (arrayList != null && arrayList.size() != 0) {
            SparseArray<File> fileIds = this.mPbrRecords.get(pbrRecNum).mFileIds;
            if (fileIds == null) {
                log("updateAnrByAdnIndex: No anr tag in pbr record 0");
                return;
            }
            ArrayList<MtkAdnRecord> arrayList2 = this.mPhoneBookRecords;
            if (arrayList2 != null) {
                if (!arrayList2.isEmpty()) {
                    File anrFile2 = fileIds.get((anrIndex * 256) + 196);
                    if (anrFile2 == null) {
                        log("updateAnrByAdnIndex no efFile anrIndex: " + anrIndex);
                        return;
                    }
                    logi("updateAnrByAdnIndex begin effile " + anrFile2);
                    if (oldAdn != null) {
                        String oldAnr2 = oldAdn.getAdditionalNumber(anrIndex);
                        oldAdn.getAasIndex();
                        oldAnr = oldAnr2;
                    } else {
                        oldAnr = null;
                    }
                    if (!CsimPhbUtil.hasModemPhbEnhanceCapability(this.mFh)) {
                        int efid = anrFile2.getEfid();
                        log("updateAnrByAdnIndex recId: " + pbrRecNum + " EF_ANR id is " + Integer.toHexString(efid).toUpperCase());
                        if (anrFile2.getParentTag() == 169) {
                            updateType2Anr(anr, adnIndex, anrFile2);
                            return;
                        }
                        try {
                            byte[] data = buildAnrRecord(anr, this.mAnrRecordSize, this.mPhoneBookRecords.get(adnIndex - 1).getAasIndex());
                            if (data != null) {
                                this.mFh.updateEFLinearFixed(efid, anrRecNum + 1, data, (String) null, obtainMessage(9));
                            }
                            anrFile = anrFile2;
                        } catch (IndexOutOfBoundsException e) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("updateAnrByAdnIndex: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is ");
                            sb.append(this.mPhoneBookRecords.size());
                            sb.append("index is ");
                            sb.append(adnIndex - 1);
                            Rlog.e(LOG_TAG, sb.toString());
                            return;
                        }
                    } else {
                        try {
                            int aas = this.mPhoneBookRecords.get(adnIndex - 1).getAasIndex();
                            Message msg2 = obtainMessage(9);
                            Object obj2 = this.mLock;
                            synchronized (obj2) {
                                if (anr != null) {
                                    try {
                                        if (anr.length() == 0) {
                                            obj = obj2;
                                            msg = msg2;
                                            anrFile = anrFile2;
                                        } else {
                                            String[] param = buildAnrRecordOptmz(anr, aas);
                                            obj = obj2;
                                            anrFile = anrFile2;
                                            try {
                                                this.mCi.editUPBEntry(0, anrIndex + 1, adnIndex, param[0], param[1], param[2], msg2);
                                                try {
                                                    this.mLock.wait();
                                                } catch (InterruptedException e2) {
                                                    Rlog.e(LOG_TAG, "Interrupted Exception in updateAnrByAdnIndexOptmz");
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                    throw th;
                                                }
                                            } catch (Throwable th3) {
                                                th = th3;
                                                throw th;
                                            }
                                        }
                                    } catch (Throwable th4) {
                                        th = th4;
                                        obj = obj2;
                                        throw th;
                                    }
                                } else {
                                    obj = obj2;
                                    msg = msg2;
                                    anrFile = anrFile2;
                                }
                                if (oldAnr != null) {
                                    try {
                                        if (oldAnr.length() != 0) {
                                            try {
                                                this.mCi.deleteUPBEntry(0, anrIndex + 1, adnIndex, msg);
                                                this.mLock.wait();
                                            } catch (Throwable th5) {
                                                th = th5;
                                                throw th;
                                            }
                                        }
                                    } catch (Throwable th6) {
                                        th = th6;
                                        throw th;
                                    }
                                }
                                try {
                                    return;
                                } catch (Throwable th7) {
                                    th = th7;
                                    throw th;
                                }
                            }
                        } catch (IndexOutOfBoundsException e3) {
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append("updateAnrByAdnIndexOptmz: mPhoneBookRecords IndexOutOfBoundsException size() is ");
                            sb2.append(this.mPhoneBookRecords.size());
                            sb2.append("index is ");
                            sb2.append(adnIndex - 1);
                            Rlog.e(LOG_TAG, sb2.toString());
                            return;
                        }
                    }
                    logi("updateAnrByAdnIndex end effile " + anrFile);
                    return;
                }
            }
            Rlog.w(LOG_TAG, "updateAnrByAdnIndex: mPhoneBookRecords is empty");
        }
    }

    private int getEmailRecNum(String[] emails, int pbrRecNum, int nIapRecNum, byte[] iapRec, int tagNum) {
        boolean hasEmail = false;
        int recNum = iapRec[tagNum] & PplMessageManager.Type.INVALID;
        log("getEmailRecNum recNum:" + recNum);
        if (emails == null) {
            if (recNum < 255 && recNum > 0) {
                this.mEmailRecTable[recNum - 1] = 0;
            }
            return -1;
        }
        int i = 0;
        while (true) {
            if (i < emails.length) {
                if (emails[i] != null && !emails[i].equals("")) {
                    hasEmail = true;
                    break;
                }
                i++;
            } else {
                break;
            }
        }
        if (!hasEmail) {
            if (recNum < 255 && recNum > 0) {
                this.mEmailRecTable[recNum - 1] = 0;
            }
            return -1;
        }
        if (recNum > this.mEmailFileSize || recNum >= 255 || recNum <= 0) {
            int nOffset = this.mEmailFileSize * pbrRecNum;
            int i2 = nOffset;
            while (true) {
                if (i2 >= this.mEmailFileSize + nOffset) {
                    break;
                }
                log("updateEmailsByAdnIndex: mEmailRecTable[" + i2 + "] is " + this.mEmailRecTable[i2]);
                int[] iArr = this.mEmailRecTable;
                if (iArr[i2] == 0) {
                    recNum = (i2 + 1) - nOffset;
                    iArr[i2] = nIapRecNum;
                    break;
                }
                i2++;
            }
        }
        if (recNum > this.mEmailFileSize) {
            recNum = 255;
        }
        if (recNum == -1) {
            return -2;
        }
        return recNum;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:47:0x008a, code lost:
        r0 = r7.mEmailInfo;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x008c, code lost:
        if (r0 == null) goto L_0x0093;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0090, code lost:
        if (r0[1] <= 0) goto L_0x0093;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0092, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0093, code lost:
        return false;
     */
    public boolean checkEmailCapacityFree(int adnIndex, String[] emails, MtkAdnRecord oldAdn) {
        int i;
        if (emails == null || getUsimEfType(ExternalSimConstants.EVENT_TYPE_RECEIVE_RSIM_AUTH_RSP) == 168 || !(oldAdn == null || oldAdn.getEmails() == null)) {
            return true;
        }
        int i2 = 0;
        while (true) {
            if (i2 >= emails.length) {
                i = 0;
                break;
            }
            if (!(emails[i2] == null || emails[i2].equals(""))) {
                i = 1;
                break;
            }
            i2++;
        }
        if (i == 0) {
            return true;
        }
        if (!CsimPhbUtil.hasModemPhbEnhanceCapability(this.mFh)) {
            int nOffset = this.mEmailFileSize * ((adnIndex - 1) / this.mAdnFileSize);
            for (int i3 = nOffset; i3 < this.mEmailFileSize + nOffset; i3++) {
                if (this.mEmailRecTable[i3] == 0) {
                    return true;
                }
            }
            return false;
        }
        synchronized (this.mLock) {
            if (this.mEmailInfo == null || this.mEmailInfo.length != 3) {
                this.mCi.queryUPBAvailable(1, 1, obtainMessage(EVENT_QUERY_EMAIL_AVAILABLE_OPTMZ_DONE));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in CheckEmailCapacityFree");
                }
                if (this.mUpbDone == -1) {
                    return true;
                }
                this.mEmailFileSize = countEmailFileSize();
            }
        }
    }

    private int countEmailFileSize() {
        int numAdnRecs = this.mPhoneBookRecords.size();
        int i = this.mAdnFileSize;
        int totalPbrRecNum = numAdnRecs / i;
        if (numAdnRecs % i > 0) {
            totalPbrRecNum++;
        }
        int[] iArr = this.mEmailInfo;
        if (iArr == null || iArr.length != 3 || totalPbrRecNum <= 0) {
            return 100;
        }
        return iArr[0] / totalPbrRecNum;
    }

    private int countEmailCapacity(int adnIndex) {
        String[] emails;
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (arrayList == null || arrayList.size() == 0) {
            return -1;
        }
        int i = this.mAdnFileSize;
        int pbrRecNum = (adnIndex - 1) / i;
        int nOffset = i * pbrRecNum;
        int numAdnRecs = this.mPhoneBookRecords.size();
        int nMax = this.mAdnFileSize + nOffset;
        int nMax2 = numAdnRecs < nMax ? numAdnRecs : nMax;
        int used = 0;
        if (this.mPbrRecords.get(pbrRecNum).mFileIds.get(ExternalSimConstants.EVENT_TYPE_RECEIVE_RSIM_AUTH_RSP) == null) {
            return -1;
        }
        MtkAdnRecord rec = null;
        for (int i2 = nOffset; i2 < nMax2; i2++) {
            try {
                rec = this.mPhoneBookRecords.get(i2);
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "countEmailCapacity: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is " + this.mPhoneBookRecords.size() + "index is " + i2);
            }
            if (rec != null && (emails = rec.getEmails()) != null && emails.length > 0 && emails[0].length() > 0) {
                used++;
            }
        }
        log("countEmailCapacity: email used: " + used);
        return used;
    }

    public boolean checkSneCapacityFree(int adnIndex, String sne, MtkAdnRecord oldAdn) {
        String oldSne = null;
        if (oldAdn != null) {
            oldSne = oldAdn.getSne();
        }
        if (sne == null || sne.equals("") || getUsimEfType(195) == 168 || ((oldSne != null && !oldSne.equals("")) || !CsimPhbUtil.hasModemPhbEnhanceCapability(this.mFh))) {
            return true;
        }
        synchronized (this.mLock) {
            if (this.mSneInfo == null) {
                this.mCi.queryUPBAvailable(2, 1, obtainMessage(EVENT_QUERY_SNE_AVAILABLE_OPTMZ_DONE));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in checkSneCapacityFree");
                }
            }
        }
        int[] iArr = this.mSneInfo;
        if (iArr == null || iArr[1] <= 0) {
            return false;
        }
        return true;
    }

    private int getUsimEfType(int efTag) {
        SparseArray<File> files;
        File efFile;
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (arrayList == null || arrayList.size() == 0 || (files = this.mPbrRecords.get(0).mFileIds) == null || (efFile = files.get(efTag)) == null) {
            return 0;
        }
        Rlog.d(LOG_TAG, "[getUsimEfType] efTag: " + efTag + ", type: " + efFile.getParentTag());
        return efFile.getParentTag();
    }

    public boolean checkEmailLength(String[] emails) {
        ArrayList<PbrRecord> arrayList;
        SparseArray<File> files;
        File emailFile;
        if (emails == null || emails[0] == null || (arrayList = this.mPbrRecords) == null || arrayList.size() == 0 || (files = this.mPbrRecords.get(0).mFileIds) == null || (emailFile = files.get(ExternalSimConstants.EVENT_TYPE_RECEIVE_RSIM_AUTH_RSP)) == null) {
            return true;
        }
        boolean emailType2 = emailFile.getParentTag() == 169;
        int i = this.mEmailRecordSize;
        int maxDataLength = (i == -1 || !emailType2) ? this.mEmailRecordSize : i - 2;
        byte[] eMailData = GsmAlphabet.stringToGsm8BitPacked(emails[0]);
        logi("checkEmailLength eMailData.length=" + eMailData.length + ", maxDataLength=" + maxDataLength);
        return maxDataLength == -1 || eMailData.length <= maxDataLength;
    }

    /* JADX WARNING: Removed duplicated region for block: B:81:0x0182 A[Catch:{ all -> 0x0161, all -> 0x0184 }] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x018a A[SYNTHETIC, Splitter:B:85:0x018a] */
    public int updateEmailsByAdnIndex(String[] emails, int adnIndex, MtkAdnRecord oldAdn) {
        SparseArray<File> files;
        ArrayList<MtkAdnRecord> arrayList;
        String[] oldEmails;
        String oldEmail;
        Object obj;
        Throwable th;
        String oldEmail2;
        int emailIndex;
        Message msg;
        int i = this.mAdnFileSize;
        int pbrRecNum = (adnIndex - 1) / i;
        int adnRecNum = (adnIndex - 1) % i;
        ArrayList<PbrRecord> arrayList2 = this.mPbrRecords;
        if (arrayList2 == null || arrayList2.size() == 0 || (files = this.mPbrRecords.get(pbrRecNum).mFileIds) == null || files.size() == 0 || (arrayList = this.mPhoneBookRecords) == null || arrayList.isEmpty()) {
            return 0;
        }
        File efFile = files.get(ExternalSimConstants.EVENT_TYPE_RECEIVE_RSIM_AUTH_RSP);
        if (efFile == null) {
            log("updateEmailsByAdnIndex: No email tag in pbr record 0");
            return 0;
        }
        if (oldAdn != null) {
            oldEmails = oldAdn.getEmails();
        } else {
            oldEmails = null;
        }
        if (oldEmails == null || oldEmails.length == 0 || TextUtils.isEmpty(oldEmails[0])) {
            oldEmail = null;
        } else {
            oldEmail = oldEmails[0];
        }
        int efid = efFile.getEfid();
        boolean emailType2 = efFile.getParentTag() == 169;
        efFile.getIndex();
        logi("updateEmailsByAdnIndex: pbrrecNum is " + pbrRecNum + " EF_EMAIL id is " + Integer.toHexString(efid).toUpperCase());
        if (CsimPhbUtil.hasModemPhbEnhanceCapability(this.mFh)) {
            Message msg2 = obtainMessage(8);
            Object obj2 = this.mLock;
            synchronized (obj2) {
                if (emails != null) {
                    try {
                        if (emails.length != 0) {
                            if (TextUtils.isEmpty(emails[0])) {
                                msg = msg2;
                                emailIndex = 1;
                                obj = obj2;
                                oldEmail2 = oldEmail;
                                if (oldEmail2 != null) {
                                    return 0;
                                }
                                try {
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                                try {
                                    this.mCi.deleteUPBEntry(1, emailIndex, adnIndex, msg);
                                    try {
                                        this.mLock.wait();
                                    } catch (InterruptedException e) {
                                        Rlog.e(LOG_TAG, "Interrupted Exception in updateEmailsByAdnIndex");
                                    }
                                    return 0;
                                } catch (Throwable th3) {
                                    th = th3;
                                    throw th;
                                }
                            } else if (emails[0].equals(oldEmail)) {
                                try {
                                } catch (Throwable th4) {
                                    th = th4;
                                    obj = obj2;
                                    throw th;
                                }
                            } else {
                                obj = obj2;
                                try {
                                    this.mCi.editUPBEntry(1, 1, adnIndex, encodeToUcs2(emails[0]), null, msg2);
                                    this.mLock.wait();
                                    return 0;
                                } catch (Throwable th5) {
                                    th = th5;
                                    throw th;
                                }
                            }
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        obj = obj2;
                        throw th;
                    }
                }
                msg = msg2;
                emailIndex = 1;
                obj = obj2;
                oldEmail2 = oldEmail;
                if (oldEmail2 != null) {
                }
            }
            return 0;
        } else if (emailType2 && this.mIapFileList != null) {
            return updateType2Email(emails, adnIndex, efFile);
        } else {
            log("updateEmailsByAdnIndex file: " + efFile);
            String email = (emails == null || emails.length <= 0) ? null : emails[0];
            int i2 = this.mEmailRecordSize;
            if (i2 <= 0) {
                return -50;
            }
            byte[] data = buildEmailRecord(email, adnIndex, i2, emailType2);
            if (data == null) {
                return -40;
            }
            this.mFh.updateEFLinearFixed(efid, adnRecNum + 1, data, (String) null, obtainMessage(8));
            return 0;
        }
    }

    private int updateType2Email(String[] emails, int adnIndex, File emailFile) {
        int i = this.mAdnFileSize;
        int pbrRecNum = (adnIndex - 1) / i;
        int adnRecNum = (adnIndex - 1) % i;
        int emailType2Index = emailFile.getIndex();
        emailFile.getEfid();
        try {
            ArrayList<byte[]> iapFile = this.mIapFileList.get(pbrRecNum);
            if (iapFile.size() > 0) {
                byte[] iapRec = iapFile.get(adnRecNum);
                int recNum = getEmailRecNum(emails, pbrRecNum, adnRecNum + 1, iapRec, emailType2Index);
                log("updateEmailsByAdnIndex: Email recNum is " + recNum);
                if (-2 == recNum) {
                    return -30;
                }
                log("updateEmailsByAdnIndex: found Email recNum is " + recNum);
                iapRec[emailType2Index] = (byte) recNum;
                SparseArray<File> files = this.mPbrRecords.get(pbrRecNum).mFileIds;
                if (files.get(193) != null) {
                    this.mFh.updateEFLinearFixed(files.get(193).getEfid(), adnRecNum + 1, iapRec, (String) null, obtainMessage(7));
                    if (!(recNum == 255 || recNum == -1)) {
                        String eMailAd = null;
                        if (emails != null) {
                            try {
                                eMailAd = emails[0];
                            } catch (IndexOutOfBoundsException e) {
                                Rlog.e(LOG_TAG, "Error: updateEmailsByAdnIndex no email address, continuing");
                            }
                            int i2 = this.mEmailRecordSize;
                            if (i2 <= 0) {
                                return -50;
                            }
                            byte[] eMailRecData = buildEmailRecord(eMailAd, adnIndex, i2, true);
                            if (eMailRecData == null) {
                                return -40;
                            }
                            this.mFh.updateEFLinearFixed(files.get(ExternalSimConstants.EVENT_TYPE_RECEIVE_RSIM_AUTH_RSP).getEfid(), recNum, eMailRecData, (String) null, obtainMessage(8));
                        }
                    }
                    return 0;
                }
                Rlog.e(LOG_TAG, "updateEmailsByAdnIndex Error: No IAP file!");
                return -50;
            }
            Rlog.w(LOG_TAG, "Warning: IAP size is 0");
            return -50;
        } catch (IndexOutOfBoundsException e2) {
            Rlog.e(LOG_TAG, "Index out of bounds.");
            return -50;
        }
    }

    private byte[] buildAnrRecord(String anr, int recordSize, int aas) {
        log("buildAnrRecord anr:" + anr + ",recordSize:" + recordSize + ",aas:" + aas);
        if (recordSize <= 0) {
            readAnrRecordSize();
        }
        byte[] anrString = new byte[recordSize];
        for (int i = 0; i < recordSize; i++) {
            anrString[i] = -1;
        }
        String updatedAnr = MtkPhoneNumberUtils.convertPreDial(anr);
        if (TextUtils.isEmpty(updatedAnr)) {
            Rlog.w(LOG_TAG, "[buildAnrRecord] Empty dialing number");
            return anrString;
        } else if (updatedAnr.length() > 20) {
            Rlog.w(LOG_TAG, "[buildAnrRecord] Max length of dialing number is 20");
            return null;
        } else {
            byte[] bcdNumber = MtkPhoneNumberUtils.numberToCalledPartyBCD(updatedAnr);
            if (bcdNumber != null) {
                anrString[0] = (byte) aas;
                System.arraycopy(bcdNumber, 0, anrString, 2, bcdNumber.length);
                anrString[1] = (byte) bcdNumber.length;
            }
            return anrString;
        }
    }

    private byte[] buildEmailRecord(String strEmail, int adnIndex, int recordSize, boolean emailType2) {
        ArrayList<PbrRecord> arrayList;
        byte[] eMailRecData = new byte[recordSize];
        for (int i = 0; i < recordSize; i++) {
            eMailRecData[i] = -1;
        }
        if (strEmail != null && !strEmail.equals("")) {
            byte[] eMailData = GsmAlphabet.stringToGsm8BitPacked(strEmail);
            int maxDataLength = (this.mEmailRecordSize == -1 || !emailType2) ? eMailRecData.length : eMailRecData.length - 2;
            log("buildEmailRecord eMailData.length=" + eMailData.length + ", maxDataLength=" + maxDataLength);
            if (eMailData.length > maxDataLength) {
                return null;
            }
            System.arraycopy(eMailData, 0, eMailRecData, 0, eMailData.length);
            if (emailType2 && (arrayList = this.mPbrRecords) != null) {
                int i2 = this.mAdnFileSize;
                int pbrIndex = (adnIndex - 1) / i2;
                int adnRecId = (adnIndex % i2) & 255;
                File adnFile = arrayList.get(pbrIndex).mFileIds.get(192);
                eMailRecData[recordSize - 2] = (byte) adnFile.getSfi();
                eMailRecData[recordSize - 1] = (byte) adnRecId;
                log("buildEmailRecord x+1=" + adnFile.getSfi() + ", x+2=" + adnRecId);
            }
        }
        return eMailRecData;
    }

    public void updateUsimPhonebookRecordsList(int index, MtkAdnRecord newAdn) {
        logi("updateUsimPhonebookRecordsList update the " + index + "th record.");
        if (index < this.mPhoneBookRecords.size()) {
            MtkAdnRecord oldAdn = this.mPhoneBookRecords.get(index);
            if (!(oldAdn == null || oldAdn.getGrpIds() == null)) {
                newAdn.setGrpIds(oldAdn.getGrpIds());
            }
            this.mPhoneBookRecords.set(index, newAdn);
            this.mRefreshAdnInfo = true;
        }
    }

    private void updatePhoneAdnRecordWithGrpByIndex(int recIndex, int adnIndex, int[] grpIds) {
        int grpSize;
        log("updatePhoneAdnRecordWithGrpByIndex the " + recIndex + "th grp ");
        if (recIndex <= this.mPhoneBookRecords.size() && (grpSize = grpIds.length) > 0) {
            try {
                MtkAdnRecord rec = this.mPhoneBookRecords.get(recIndex);
                log("updatePhoneAdnRecordWithGrpByIndex the adnIndex is " + adnIndex + "; the original index is " + rec.getRecId());
                StringBuilder grpIdsSb = new StringBuilder();
                for (int i = 0; i < grpSize - 1; i++) {
                    grpIdsSb.append(grpIds[i]);
                    grpIdsSb.append(",");
                }
                grpIdsSb.append(grpIds[grpSize - 1]);
                rec.setGrpIds(grpIdsSb.toString());
                log("updatePhoneAdnRecordWithGrpByIndex grpIds is " + grpIdsSb.toString());
                this.mPhoneBookRecords.set(recIndex, rec);
                log("updatePhoneAdnRecordWithGrpByIndex the rec:" + rec);
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "updatePhoneAdnRecordWithGrpByIndex: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is " + this.mPhoneBookRecords.size() + "index is " + recIndex);
            }
        }
    }

    private void readType1Ef(File file, int anrIndex) {
        int[] size;
        int nOffset;
        int nMax;
        int pbrIndex;
        log("readType1Ef:" + file);
        if (file.getParentTag() == 168) {
            int pbrIndex2 = file.mPbrRecord;
            int numAdnRecs = this.mPhoneBookRecords.size();
            int i = this.mAdnFileSize;
            int nOffset2 = pbrIndex2 * i;
            int nMax2 = i + nOffset2;
            int nMax3 = numAdnRecs < nMax2 ? numAdnRecs : nMax2;
            SparseArray<int[]> sparseArray = this.mRecordSize;
            if (sparseArray == null || sparseArray.get(file.getEfid()) == null) {
                size = readEFLinearRecordSize(file.getEfid());
            } else {
                size = this.mRecordSize.get(file.getEfid());
            }
            if (size != null) {
                int i2 = 3;
                if (size.length == 3) {
                    int recordSize = size[0];
                    int tag = file.mTag % 256;
                    int i3 = file.mTag / 256;
                    log("readType1Ef: RecordSize = " + recordSize);
                    if (tag == 202) {
                        for (int i4 = nOffset2; i4 < this.mEmailFileSize + nOffset2; i4++) {
                            try {
                                this.mEmailRecTable[i4] = 0;
                            } catch (ArrayIndexOutOfBoundsException e) {
                                Rlog.e(LOG_TAG, "init RecTable error " + e.getMessage());
                            }
                        }
                    }
                    if (recordSize == 0) {
                        Rlog.w(LOG_TAG, "readType1Ef: recordSize is 0. ");
                        return;
                    }
                    int i5 = nOffset2;
                    int totalReadingNum = 0;
                    while (i5 < nMax3) {
                        try {
                            MtkAdnRecord rec = this.mPhoneBookRecords.get(i5);
                            if (rec.getAlphaTag().length() > 0 || rec.getNumber().length() > 0) {
                                int[] data = new int[i2];
                                data[0] = file.mPbrRecord;
                                data[1] = i5;
                                data[2] = anrIndex;
                                int loadWhat = 0;
                                pbrIndex = pbrIndex2;
                                if (tag == 195) {
                                    loadWhat = 18;
                                    this.mReadingSneNum.incrementAndGet();
                                } else if (tag == 196) {
                                    loadWhat = 16;
                                    this.mReadingAnrNum.addAndGet(1);
                                } else if (tag != 202) {
                                    Rlog.e(LOG_TAG, "not support tag " + file.mTag);
                                } else {
                                    data[0] = ((i5 + 1) - nOffset2) + (this.mEmailFileSize * nOffset2);
                                    loadWhat = 15;
                                    this.mReadingEmailNum.incrementAndGet();
                                }
                                nMax = nMax3;
                                nOffset = nOffset2;
                                this.mFh.readEFLinearFixed(file.getEfid(), (i5 + 1) - nOffset2, recordSize, obtainMessage(loadWhat, data));
                                totalReadingNum++;
                            } else {
                                pbrIndex = pbrIndex2;
                                nOffset = nOffset2;
                                nMax = nMax3;
                            }
                            i5++;
                            pbrIndex2 = pbrIndex;
                            nMax3 = nMax;
                            nOffset2 = nOffset;
                            i2 = 3;
                        } catch (IndexOutOfBoundsException e2) {
                            Rlog.e(LOG_TAG, "readType1Ef: mPhoneBookRecords IndexOutOfBoundsException numAdnRecs is " + numAdnRecs + "index is " + i5);
                        }
                    }
                    if (tag != 195) {
                        if (tag != 196) {
                            if (tag != 202) {
                                Rlog.e(LOG_TAG, "not support tag " + Integer.toHexString(file.mTag).toUpperCase());
                            } else if (this.mReadingEmailNum.get() == 0) {
                                this.mNeedNotify.set(false);
                                return;
                            } else {
                                this.mNeedNotify.set(true);
                            }
                        } else if (this.mReadingAnrNum.get() == 0) {
                            this.mNeedNotify.set(false);
                            return;
                        } else {
                            this.mNeedNotify.set(true);
                        }
                    } else if (this.mReadingSneNum.get() == 0) {
                        this.mNeedNotify.set(false);
                        return;
                    } else {
                        this.mNeedNotify.set(true);
                    }
                    logi("readType1Ef before mLock.wait " + this.mNeedNotify.get() + " total:" + totalReadingNum);
                    synchronized (this.mLock) {
                        try {
                            this.mLock.wait();
                        } catch (InterruptedException e3) {
                            Rlog.e(LOG_TAG, "Interrupted Exception in readType1Ef");
                        }
                    }
                    logi("readType1Ef after mLock.wait " + this.mNeedNotify.get());
                    return;
                }
            }
            Rlog.e(LOG_TAG, "readType1Ef: read record size error.");
        }
    }

    private void readType2Ef(File file) {
        int[] size;
        int recId;
        File iapFile;
        int recId2;
        SparseArray<File> files;
        int recId3;
        int loadWhat;
        log("readType2Ef:" + file);
        if (file.getParentTag() == 169) {
            int recId4 = file.mPbrRecord;
            SparseArray<File> files2 = this.mPbrRecords.get(file.mPbrRecord).mFileIds;
            if (files2 == null) {
                Rlog.e(LOG_TAG, "Error: no fileIds");
                return;
            }
            File iapFile2 = files2.get(193);
            if (iapFile2 == null) {
                Rlog.e(LOG_TAG, "Can't locate EF_IAP in EF_PBR.");
                return;
            }
            readIapFileAndWait(recId4, iapFile2.getEfid(), false);
            ArrayList<ArrayList<byte[]>> arrayList = this.mIapFileList;
            if (arrayList != null && arrayList.size() > recId4) {
                if (this.mIapFileList.get(recId4).size() != 0) {
                    int numAdnRecs = this.mPhoneBookRecords.size();
                    int i = this.mAdnFileSize;
                    int nOffset = recId4 * i;
                    int nMax = i + nOffset;
                    int nMax2 = numAdnRecs < nMax ? numAdnRecs : nMax;
                    int nMax3 = file.mTag;
                    if (!(nMax3 == 195 || nMax3 == 196)) {
                        if (nMax3 != 202) {
                            Rlog.e(LOG_TAG, "no implement type2 EF " + file.mTag);
                            return;
                        }
                        for (int i2 = nOffset; i2 < this.mEmailFileSize + nOffset; i2++) {
                            try {
                                this.mEmailRecTable[i2] = 0;
                            } catch (ArrayIndexOutOfBoundsException e) {
                                Rlog.e(LOG_TAG, "init RecTable error " + e.getMessage());
                            }
                        }
                    }
                    int efid = file.getEfid();
                    SparseArray<int[]> sparseArray = this.mRecordSize;
                    if (sparseArray == null || sparseArray.get(efid) == null) {
                        size = readEFLinearRecordSize(efid);
                    } else {
                        size = this.mRecordSize.get(efid);
                    }
                    if (size != null) {
                        if (size.length == 3) {
                            log("readType2: RecordSize = " + size[0]);
                            ArrayList<byte[]> iapList = this.mIapFileList.get(recId4);
                            if (iapList.size() == 0) {
                                Rlog.e(LOG_TAG, "Warning: IAP size is 0");
                                return;
                            }
                            int type2Index = file.getIndex();
                            int totalReadingNum = 0;
                            int i3 = nOffset;
                            while (i3 < nMax2) {
                                try {
                                    MtkAdnRecord arec = this.mPhoneBookRecords.get(i3);
                                    if (arec.getAlphaTag().length() > 0 || arec.getNumber().length() > 0) {
                                        int index = iapList.get(i3 - nOffset)[type2Index] & PplMessageManager.Type.INVALID;
                                        if (index <= 0) {
                                            recId2 = recId4;
                                            files = files2;
                                            iapFile = iapFile2;
                                            recId3 = totalReadingNum;
                                        } else if (index >= 255) {
                                            recId2 = recId4;
                                            files = files2;
                                            iapFile = iapFile2;
                                            recId3 = totalReadingNum;
                                        } else {
                                            log("Type2 iap[" + (i3 - nOffset) + "]=" + index);
                                            int[] data = new int[3];
                                            data[0] = recId4;
                                            data[1] = i3;
                                            int i4 = file.mTag;
                                            recId2 = recId4;
                                            if (i4 == 195) {
                                                this.mReadingSneNum.incrementAndGet();
                                                loadWhat = 18;
                                            } else if (i4 == 196) {
                                                data[2] = file.mAnrIndex;
                                                this.mReadingAnrNum.addAndGet(1);
                                                loadWhat = 16;
                                            } else if (i4 != 202) {
                                                Rlog.e(LOG_TAG, "not support tag " + file.mTag);
                                                loadWhat = 0;
                                            } else {
                                                data[0] = ((i3 + 1) - nOffset) + (this.mEmailFileSize * nOffset);
                                                this.mReadingEmailNum.incrementAndGet();
                                                loadWhat = 15;
                                            }
                                            files = files2;
                                            iapFile = iapFile2;
                                            this.mFh.readEFLinearFixed(efid, index, size[0], obtainMessage(loadWhat, data));
                                            totalReadingNum++;
                                            i3++;
                                            files2 = files;
                                            recId4 = recId2;
                                            iapFile2 = iapFile;
                                        }
                                    } else {
                                        recId2 = recId4;
                                        files = files2;
                                        iapFile = iapFile2;
                                        recId3 = totalReadingNum;
                                    }
                                    totalReadingNum = recId3;
                                    i3++;
                                    files2 = files;
                                    recId4 = recId2;
                                    iapFile2 = iapFile;
                                } catch (IndexOutOfBoundsException e2) {
                                    recId = totalReadingNum;
                                    Rlog.e(LOG_TAG, "readType2Ef: mPhoneBookRecords IndexOutOfBoundsException numAdnRecs is " + numAdnRecs + "index is " + i3);
                                }
                            }
                            recId = totalReadingNum;
                            int i5 = file.mTag;
                            if (i5 != 195) {
                                if (i5 != 196) {
                                    if (i5 != 202) {
                                        Rlog.e(LOG_TAG, "not support tag " + file.mTag);
                                    } else if (this.mReadingEmailNum.get() == 0) {
                                        this.mNeedNotify.set(false);
                                        return;
                                    } else {
                                        this.mNeedNotify.set(true);
                                    }
                                } else if (this.mReadingAnrNum.get() == 0) {
                                    this.mNeedNotify.set(false);
                                    return;
                                } else {
                                    this.mNeedNotify.set(true);
                                }
                            } else if (this.mReadingSneNum.get() == 0) {
                                this.mNeedNotify.set(false);
                                return;
                            } else {
                                this.mNeedNotify.set(true);
                            }
                            logi("readType2Ef before mLock.wait " + this.mNeedNotify.get() + " total:" + recId);
                            synchronized (this.mLock) {
                                try {
                                    this.mLock.wait();
                                } catch (InterruptedException e3) {
                                    Rlog.e(LOG_TAG, "Interrupted Exception in readType2Ef");
                                }
                            }
                            logi("readType2Ef after mLock.wait " + this.mNeedNotify.get());
                            return;
                        }
                    }
                    Rlog.e(LOG_TAG, "readType2: read record size error.");
                    return;
                }
            }
            Rlog.e(LOG_TAG, "Error: IAP file is empty");
        }
    }

    private void updatePhoneAdnRecordWithEmailByIndex(int emailIndex, int adnIndex, byte[] emailRecData) {
        ArrayList<PbrRecord> arrayList;
        log("updatePhoneAdnRecordWithEmailByIndex emailIndex = " + emailIndex + ",adnIndex = " + adnIndex);
        if (emailRecData != null && (arrayList = this.mPbrRecords) != null && arrayList.size() != 0) {
            boolean emailType2 = ((File) this.mPbrRecords.get(0).mFileIds.get(ExternalSimConstants.EVENT_TYPE_RECEIVE_RSIM_AUTH_RSP)).getParentTag() == 169;
            log("updatePhoneAdnRecordWithEmailByIndex: Type2: " + emailType2 + " emailData: " + IccUtils.bytesToHexString(emailRecData));
            int length = emailRecData.length;
            if (emailType2 && emailRecData.length >= 2) {
                length = emailRecData.length - 2;
            }
            log("updatePhoneAdnRecordWithEmailByIndex length = " + length);
            byte[] validEMailData = new byte[length];
            for (int i = 0; i < length; i++) {
                validEMailData[i] = -1;
            }
            System.arraycopy(emailRecData, 0, validEMailData, 0, length);
            try {
                String email = IccUtils.adnStringFieldToString(validEMailData, 0, length);
                if (email != null && !email.equals("")) {
                    this.mPhoneBookRecords.get(adnIndex).setEmails(new String[]{email});
                }
                this.mEmailRecTable[emailIndex - 1] = adnIndex + 1;
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "[JE]updatePhoneAdnRecordWithEmailByIndex " + e.getMessage());
            }
        }
    }

    private void updateType2Anr(String anr, int adnIndex, File file) {
        int index;
        int recNum;
        MtkAdnRecord rec;
        boolean sharedAnr;
        ArrayList<byte[]> relatedList;
        int pbrRecNum;
        ArrayList<byte[]> list;
        logi("updateType2Ef anr:" + anr + ",adnIndex:" + adnIndex + ",file:" + file);
        int i = this.mAdnFileSize;
        int pbrRecNum2 = (adnIndex + -1) / i;
        int iapRecNum = (adnIndex + -1) % i;
        log("updateType2Ef pbrRecNum:" + pbrRecNum2 + ",iapRecNum:" + iapRecNum);
        if (this.mIapFileList != null && file != null) {
            ArrayList<PbrRecord> arrayList = this.mPbrRecords;
            if (arrayList == null) {
                return;
            }
            if (arrayList.size() != 0) {
                SparseArray<File> files = this.mPbrRecords.get(file.mPbrRecord).mFileIds;
                if (files != null) {
                    try {
                        ArrayList<byte[]> list2 = this.mIapFileList.get(file.mPbrRecord);
                        if (list2 != null) {
                            if (list2.size() == 0) {
                                Rlog.e(LOG_TAG, "Warning: IAP size is 0");
                                return;
                            }
                            byte[] iap = list2.get(iapRecNum);
                            if (iap != null) {
                                int index2 = iap[file.getIndex()] & 255;
                                log("updateType2Ef orignal index :" + index2);
                                if (anr == null) {
                                    index = index2;
                                } else if (anr.length() == 0) {
                                    index = index2;
                                } else {
                                    int size = this.mRecordSize.get(file.getEfid())[2];
                                    log("updateType2Anr size :" + size);
                                    if (index2 <= 0 || index2 > size) {
                                        int[] indexArray = new int[(size + 1)];
                                        for (int i2 = 1; i2 <= size; i2++) {
                                            indexArray[i2] = 0;
                                        }
                                        int i3 = 0;
                                        while (i3 < list2.size()) {
                                            byte[] value = list2.get(i3);
                                            if (value != null) {
                                                pbrRecNum = pbrRecNum2;
                                                list = list2;
                                                int tem = value[file.getIndex()] & PplMessageManager.Type.INVALID;
                                                if (tem > 0 && tem < 255 && tem <= size) {
                                                    indexArray[tem] = 1;
                                                }
                                            } else {
                                                pbrRecNum = pbrRecNum2;
                                                list = list2;
                                            }
                                            i3++;
                                            list2 = list;
                                            pbrRecNum2 = pbrRecNum;
                                        }
                                        boolean sharedAnr2 = false;
                                        File file2 = null;
                                        int i4 = 0;
                                        while (true) {
                                            if (i4 >= this.mPbrRecords.size()) {
                                                sharedAnr = sharedAnr2;
                                                break;
                                            }
                                            if (i4 != file.mPbrRecord) {
                                                sharedAnr = sharedAnr2;
                                                file2 = (File) this.mPbrRecords.get(i4).mFileIds.get((adnIndex * 256) + 196);
                                                if (file2 != null) {
                                                    if (file2.getEfid() == file.getEfid()) {
                                                        sharedAnr = true;
                                                    }
                                                }
                                            } else {
                                                sharedAnr = sharedAnr2;
                                            }
                                            i4++;
                                            sharedAnr2 = sharedAnr;
                                        }
                                        if (sharedAnr) {
                                            try {
                                                ArrayList<byte[]> relatedList2 = this.mIapFileList.get(file2.mPbrRecord);
                                                if (relatedList2 != null && relatedList2.size() > 0) {
                                                    int i5 = 0;
                                                    while (i5 < relatedList2.size()) {
                                                        byte[] value2 = relatedList2.get(i5);
                                                        if (value2 != null) {
                                                            relatedList = relatedList2;
                                                            int tem2 = value2[file2.getIndex()] & PplMessageManager.Type.INVALID;
                                                            if (tem2 > 0 && tem2 < 255 && tem2 <= size) {
                                                                indexArray[tem2] = 1;
                                                            }
                                                        } else {
                                                            relatedList = relatedList2;
                                                        }
                                                        i5++;
                                                        relatedList2 = relatedList;
                                                    }
                                                }
                                            } catch (IndexOutOfBoundsException e) {
                                                Rlog.e(LOG_TAG, "Index out of bounds.");
                                                return;
                                            }
                                        }
                                        int i6 = 1;
                                        while (true) {
                                            if (i6 > size) {
                                                recNum = 0;
                                                break;
                                            } else if (indexArray[i6] == 0) {
                                                recNum = i6;
                                                break;
                                            } else {
                                                i6++;
                                            }
                                        }
                                    } else {
                                        recNum = index2;
                                    }
                                    log("updateType2Anr final index :" + recNum);
                                    if (recNum != 0) {
                                        try {
                                            rec = this.mPhoneBookRecords.get(adnIndex - 1);
                                        } catch (IndexOutOfBoundsException e2) {
                                            StringBuilder sb = new StringBuilder();
                                            sb.append("updateType2Anr: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is ");
                                            sb.append(this.mPhoneBookRecords.size());
                                            sb.append("index is ");
                                            sb.append(adnIndex - 1);
                                            Rlog.e(LOG_TAG, sb.toString());
                                            rec = null;
                                        }
                                        if (rec != null) {
                                            byte[] data = buildAnrRecord(anr, this.mAnrRecordSize, rec.getAasIndex());
                                            int fileId = file.getEfid();
                                            if (data != null) {
                                                this.mFh.updateEFLinearFixed(fileId, recNum, data, (String) null, obtainMessage(9));
                                                if (recNum != index2) {
                                                    iap[file.getIndex()] = (byte) recNum;
                                                    if (files.get(193) != null) {
                                                        this.mFh.updateEFLinearFixed(files.get(193).getEfid(), iapRecNum + 1, iap, (String) null, obtainMessage(7));
                                                        return;
                                                    } else {
                                                        Rlog.e(LOG_TAG, "updateType2Anr Error: No IAP file!");
                                                        return;
                                                    }
                                                } else {
                                                    return;
                                                }
                                            } else {
                                                return;
                                            }
                                        } else {
                                            return;
                                        }
                                    } else {
                                        return;
                                    }
                                }
                                if (index > 0) {
                                    iap[file.getIndex()] = -1;
                                    if (files.get(193) != null) {
                                        this.mFh.updateEFLinearFixed(files.get(193).getEfid(), iapRecNum + 1, iap, (String) null, obtainMessage(7));
                                    } else {
                                        Rlog.e(LOG_TAG, "updateType2Anr Error: No IAP file!");
                                    }
                                }
                            }
                        }
                    } catch (IndexOutOfBoundsException e3) {
                        Rlog.e(LOG_TAG, "Index out of bounds.");
                    }
                }
            }
        }
    }

    private void readAnrRecordSize() {
        logi("readAnrRecordSize");
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (arrayList == null || arrayList.size() == 0) {
            Rlog.w(LOG_TAG, "readAnrRecordSize: PBR null ");
            return;
        }
        SparseArray<File> fileIds = this.mPbrRecords.get(0).mFileIds;
        if (fileIds == null) {
            Rlog.w(LOG_TAG, "readAnrRecordSize: fileIds null ");
            return;
        }
        File anrFile = fileIds.get(196);
        if (fileIds.size() == 0 || anrFile == null) {
            this.mAnrRecordSize = 0;
            Rlog.w(LOG_TAG, "readAnrRecordSize: No anr tag in pbr file ");
            return;
        }
        int[] size = readEFLinearRecordSize(anrFile.getEfid());
        if (size == null || size.length != 3) {
            Rlog.e(LOG_TAG, "readAnrRecordSize: read record size error.");
            return;
        }
        this.mAnrRecordSize = size[0];
        logi("readAnrRecordSize end size = " + this.mAnrRecordSize);
    }

    private void readEmailRecordSize() {
        logi("readEmailRecordSize");
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (arrayList == null || arrayList.size() == 0) {
            Rlog.w(LOG_TAG, "readEmailRecordSize: PBR null");
            return;
        }
        SparseArray<File> fileIds = this.mPbrRecords.get(0).mFileIds;
        if (fileIds == null) {
            Rlog.w(LOG_TAG, "readEmailRecordSize: fileId null");
            return;
        }
        File emailFile = fileIds.get(ExternalSimConstants.EVENT_TYPE_RECEIVE_RSIM_AUTH_RSP);
        if (fileIds.size() == 0 || emailFile == null) {
            this.mEmailRecordSize = 0;
            Rlog.w(LOG_TAG, "readEmailRecordSize: No email tag in pbr file ");
            return;
        }
        int[] size = readEFLinearRecordSize(emailFile.getEfid());
        if (size == null || size.length != 3) {
            Rlog.e(LOG_TAG, "readEmailRecordSize: read record size error.");
            return;
        }
        this.mEmailFileSize = size[2];
        this.mEmailRecordSize = size[0];
        logi("readEmailRecordSize Size:" + this.mEmailFileSize + "," + this.mEmailRecordSize);
    }

    private boolean loadAasFiles() {
        synchronized (this.mLock) {
            if (this.mAasForAnr == null || this.mAasForAnr.size() == 0) {
                if (!this.mIsPbrPresent.booleanValue()) {
                    Rlog.e(LOG_TAG, "No PBR files");
                    return false;
                }
                loadPBRFiles();
                if (this.mPbrRecords == null) {
                    return false;
                }
                int numRecs = this.mPbrRecords.size();
                if (this.mAasForAnr == null) {
                    this.mAasForAnr = new ArrayList<>();
                }
                this.mAasForAnr.clear();
                logi("loadAasFiles read num:" + numRecs + ", " + this.mPbrNeedNotify);
                if (!CsimPhbUtil.hasModemPhbEnhanceCapability(this.mFh)) {
                    for (int i = 0; i < numRecs; i++) {
                        readAASFileAndWait(i);
                    }
                } else {
                    readAasFileAndWaitOptmz();
                }
            }
            return true;
        }
    }

    public ArrayList<AlphaTag> getUsimAasList() {
        ArrayList<String> allAas;
        logi("getUsimAasList start");
        ArrayList<AlphaTag> results = new ArrayList<>();
        if (!loadAasFiles() || (allAas = this.mAasForAnr) == null) {
            return results;
        }
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < allAas.size(); j++) {
                String value = allAas.get(j);
                logi("aasIndex:" + (j + 1) + ",pbrIndex:" + i + ",value:" + value);
                results.add(new AlphaTag(j + 1, value, i));
            }
        }
        return results;
    }

    public String getUsimAasById(int index, int pbrIndex) {
        logi("getUsimAasById by id " + index + ",pbrIndex " + pbrIndex);
        if (!loadAasFiles()) {
            return null;
        }
        ArrayList<String> map = this.mAasForAnr;
        if (map != null) {
            logi("getUsimAasById NonNULL by id " + index + ",pbrIndex " + pbrIndex);
            return map.get(index - 1);
        }
        logi("getUsimAasById NULL by id " + index + ",pbrIndex " + pbrIndex);
        return null;
    }

    public boolean removeUsimAasById(int index, int pbrIndex) {
        logi("removeUsimAasById by id " + index + ",pbrIndex " + pbrIndex);
        if (!loadAasFiles()) {
            return false;
        }
        SparseArray<File> files = this.mPbrRecords.get(pbrIndex).mFileIds;
        if (files == null || files.get(199) == null) {
            Rlog.e(LOG_TAG, "removeUsimAasById-PBR have no AAS EF file");
            return false;
        }
        int efid = files.get(199).getEfid();
        log("removeUsimAasById result,efid:" + efid);
        if (this.mFh != null) {
            Message msg = obtainMessage(10);
            int len = getUsimAasMaxNameLen();
            byte[] aasString = new byte[len];
            for (int i = 0; i < len; i++) {
                aasString[i] = -1;
            }
            synchronized (this.mAasLock) {
                this.mCi.deleteUPBEntry(3, 1, index, msg);
                try {
                    this.mAasLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in removesimAasById");
                }
            }
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar == null || ar.exception == null) {
                ArrayList<String> list = this.mAasForAnr;
                if (list != null) {
                    log("remove aas done " + list.get(index - 1));
                    list.set(index + -1, null);
                } else {
                    log("remove aas mAasForAnr is null ");
                }
                return true;
            }
            Rlog.e(LOG_TAG, "removeUsimAasById exception " + ar.exception);
            return false;
        }
        Rlog.e(LOG_TAG, "removeUsimAasById-IccFileHandler is null");
        return false;
    }

    public int insertUsimAas(String aasName) {
        logi("insertUsimAas begin" + aasName);
        int j = 0;
        if (aasName == null || aasName.length() == 0) {
            return 0;
        }
        if (!loadAasFiles()) {
            return -1;
        }
        if (aasName.length() > getUsimAasMaxNameLen()) {
            return 0;
        }
        synchronized (this.mAasLock) {
            int aasIndex = 0;
            boolean found = false;
            ArrayList<String> allAas = this.mAasForAnr;
            while (true) {
                if (j >= allAas.size()) {
                    break;
                }
                String value = allAas.get(j);
                if (value == null) {
                    break;
                } else if (value.length() == 0) {
                    break;
                } else {
                    j++;
                }
            }
            found = true;
            aasIndex = j + 1;
            log("insertUsimAas aasIndex:" + aasIndex + ",found:" + found);
            if (!found) {
                return -2;
            }
            String temp = encodeToUcs2(aasName);
            Message msg = obtainMessage(10);
            this.mCi.editUPBEntry(3, 0, aasIndex, temp, null, msg);
            try {
                this.mAasLock.wait();
            } catch (InterruptedException e) {
                Rlog.e(LOG_TAG, "Interrupted Exception in insertUsimAas");
            }
            AsyncResult ar = (AsyncResult) msg.obj;
            logi("insertUsimAas UPB_EF_AAS: ar " + ar);
            if (ar == null || ar.exception == null) {
                ArrayList<String> list = this.mAasForAnr;
                if (list != null) {
                    list.set(aasIndex - 1, aasName);
                    logi("insertUsimAas update mAasForAnr done");
                } else {
                    logi("insertUsimAas mAasForAnr is null");
                }
                return aasIndex;
            }
            Rlog.e(LOG_TAG, "insertUsimAas exception " + ar.exception);
            return -1;
        }
    }

    public boolean updateUsimAas(int index, int pbrIndex, String aasName) {
        Object obj;
        AsyncResult ar;
        logi("updateUsimAas index " + index + ",pbrIndex " + pbrIndex + ",aasName " + aasName);
        if (!loadAasFiles()) {
            return false;
        }
        ArrayList<String> map = this.mAasForAnr;
        if (index <= 0 || index > map.size()) {
            Rlog.e(LOG_TAG, "updateUsimAas not found aas index " + index);
            return false;
        }
        String aas = map.get(index - 1);
        log("updateUsimAas old aas " + aas);
        if (aasName == null || aasName.length() == 0) {
            return removeUsimAasById(index, pbrIndex);
        }
        int limit = getUsimAasMaxNameLen();
        int len = aasName.length();
        log("updateUsimAas aas limit " + limit);
        if (len > limit) {
            return false;
        }
        log("updateUsimAas offset 0");
        int aasIndex = index + 0;
        String temp = encodeToUcs2(aasName);
        Message msg = obtainMessage(10);
        Object obj2 = this.mAasLock;
        synchronized (obj2) {
            try {
                obj = obj2;
                try {
                    this.mCi.editUPBEntry(3, 0, aasIndex, temp, null, msg);
                    try {
                        this.mAasLock.wait();
                    } catch (InterruptedException e) {
                        Rlog.e(LOG_TAG, "Interrupted Exception in updateUsimAas");
                    } catch (Throwable th) {
                        ar = th;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th2) {
                                ar = th2;
                            }
                        }
                        throw ar;
                    }
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2 == null || ar2.exception == null) {
                        ArrayList<String> list = this.mAasForAnr;
                        if (list != null) {
                            list.set(index - 1, aasName);
                            logi("updateUsimAas update mAasForAnr done");
                            return true;
                        }
                        logi("updateUsimAas mAasForAnr is null");
                        return true;
                    }
                    Rlog.e(LOG_TAG, "updateUsimAas exception " + ar2.exception);
                    return false;
                } catch (Throwable th3) {
                    ar = th3;
                    while (true) {
                        break;
                    }
                    throw ar;
                }
            } catch (Throwable th4) {
                ar = th4;
                obj = obj2;
                while (true) {
                    break;
                }
                throw ar;
            }
        }
    }

    public boolean updateAdnAas(int adnIndex, int aasIndex) {
        int i = this.mAdnFileSize;
        int i2 = (adnIndex - 1) / i;
        int i3 = (adnIndex - 1) % i;
        try {
            MtkAdnRecord rec = this.mPhoneBookRecords.get(adnIndex - 1);
            rec.setAasIndex(aasIndex);
            for (int i4 = 0; i4 < 3; i4++) {
                updateAnrByAdnIndex(rec.getAdditionalNumber(i4), adnIndex, i4, rec);
            }
            return true;
        } catch (IndexOutOfBoundsException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("updateADNAAS: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is ");
            sb.append(this.mPhoneBookRecords.size());
            sb.append("index is ");
            sb.append(adnIndex - 1);
            Rlog.e(LOG_TAG, sb.toString());
            return false;
        }
    }

    public int getUsimAasMaxNameLen() {
        logi("getUsimAasMaxNameLen begin");
        synchronized (this.mUPBCapabilityLock) {
            if (this.mUpbCap[4] < 0 && checkIsPhbReady()) {
                this.mCi.queryUPBCapability(obtainMessage(19));
                try {
                    this.mUPBCapabilityLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in getUsimAasMaxNameLen");
                }
            }
        }
        logi("getUsimAasMaxNameLen done: L_AAS is " + this.mUpbCap[4]);
        return this.mUpbCap[4];
    }

    public int getUsimAasMaxCount() {
        logi("getUsimAasMaxCount begin");
        synchronized (this.mUPBCapabilityLock) {
            if (this.mUpbCap[3] < 0 && checkIsPhbReady()) {
                this.mCi.queryUPBCapability(obtainMessage(19));
                try {
                    this.mUPBCapabilityLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in getUsimAasMaxCount");
                }
            }
        }
        logi("getUsimAasMaxCount done: N_AAS is " + this.mUpbCap[3]);
        return this.mUpbCap[3];
    }

    public void loadPBRFiles() {
        if (this.mIsPbrPresent.booleanValue()) {
            synchronized (this.mLock) {
                if (this.mPbrRecords == null) {
                    this.mPbrNeedNotify++;
                    readPbrFileAndWait();
                }
            }
        }
    }

    public int getAnrCount() {
        logi("getAnrCount begin");
        synchronized (this.mUPBCapabilityLock) {
            if (this.mUpbCap[0] < 0 && checkIsPhbReady()) {
                this.mCi.queryUPBCapability(obtainMessage(19));
                try {
                    this.mUPBCapabilityLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in getAnrCount");
                }
            }
        }
        if (this.mAnrRecordSize <= 0) {
            logi("getAnrCount end mAnrRecordSize:" + this.mAnrRecordSize);
            return this.mAnrRecordSize;
        }
        logi("getAnrCount done: N_ANR is " + this.mUpbCap[0]);
        if (this.mUpbCap[0] > 0) {
            return 1;
        }
        return 0;
    }

    public int getEmailCount() {
        logi("getEmailCount begin");
        synchronized (this.mUPBCapabilityLock) {
            if (this.mUpbCap[1] < 0 && checkIsPhbReady()) {
                this.mCi.queryUPBCapability(obtainMessage(19));
                try {
                    this.mUPBCapabilityLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in getEmailCount");
                }
            }
        }
        if (this.mEmailRecordSize <= 0) {
            logi("getEmailCount end mEmailRecordSize:" + this.mEmailRecordSize);
            return this.mEmailRecordSize;
        }
        logi("getEmailCount done: N_EMAIL is " + this.mUpbCap[1]);
        if (this.mUpbCap[1] > 0) {
            return 1;
        }
        return 0;
    }

    public boolean hasSne() {
        log("hasSne begin");
        synchronized (this.mUPBCapabilityLock) {
            if (this.mUpbCap[2] < 0 && checkIsPhbReady()) {
                this.mCi.queryUPBCapability(obtainMessage(19));
                try {
                    this.mUPBCapabilityLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in hasSne");
                }
            }
        }
        log("hasSne done: N_Sne is " + this.mUpbCap[2]);
        return this.mUpbCap[2] > 0;
    }

    public int getSneRecordLen() {
        SparseArray<File> files;
        int[] size;
        if (!hasSne()) {
            return 0;
        }
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (arrayList == null || arrayList.size() == 0 || this.mPbrRecords.get(0) == null || (files = this.mPbrRecords.get(0).mFileIds) == null) {
            return -1;
        }
        File sneFile = files.get(195);
        if (sneFile == null) {
            return 0;
        }
        int efid = sneFile.getEfid();
        boolean sneType2 = sneFile.getParentTag() == 169;
        logi("getSneRecordLen: EFSNE id is " + efid);
        SparseArray<int[]> sparseArray = this.mRecordSize;
        if (sparseArray == null || sparseArray.get(efid) == null) {
            size = readEFLinearRecordSize(efid);
        } else {
            size = this.mRecordSize.get(efid);
        }
        if (size == null) {
            return 0;
        }
        if (sneType2) {
            return size[0] - 2;
        }
        return size[0];
    }

    public int getUpbDone() {
        return this.mUpbDone;
    }

    private void updatePhoneAdnRecordWithSneByIndex(int recNum, int adnIndex, byte[] recData) {
        if (recData != null) {
            String sne = IccUtils.adnStringFieldToString(recData, 0, recData.length);
            log("updatePhoneAdnRecordWithSneByIndex index " + adnIndex + " recData file is " + sne);
            if (sne != null && !sne.equals("")) {
                try {
                    this.mPhoneBookRecords.get(adnIndex).setSne(sne);
                } catch (IndexOutOfBoundsException e) {
                    Rlog.e(LOG_TAG, "updatePhoneAdnRecordWithSneByIndex: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is " + this.mPhoneBookRecords.size() + "index is " + adnIndex);
                }
            }
        }
    }

    public int updateSneByAdnIndex(String sne, int adnIndex, MtkAdnRecord oldAdn) {
        String oldSne;
        Object obj;
        int efIndex;
        Throwable th;
        logi("updateSneByAdnIndex begin, adnIndex " + adnIndex);
        int i = this.mAdnFileSize;
        int pbrRecNum = (adnIndex + -1) / i;
        int i2 = (adnIndex + -1) % i;
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (arrayList == null || arrayList.size() == 0) {
            return -1;
        }
        Message msg = obtainMessage(11);
        SparseArray<File> files = this.mPbrRecords.get(pbrRecNum).mFileIds;
        if (files == null || files.get(195) == null) {
            log("updateSneByAdnIndex: No SNE tag in pbr file 0");
            return -1;
        }
        ArrayList<MtkAdnRecord> arrayList2 = this.mPhoneBookRecords;
        if (arrayList2 == null || arrayList2.isEmpty()) {
            return -1;
        }
        if (oldAdn != null) {
            oldSne = oldAdn.getSne();
        } else {
            oldSne = null;
        }
        int efid = files.get(195).getEfid();
        log("updateSneByAdnIndex: EF_SNE id is " + Integer.toHexString(efid).toUpperCase());
        log("updateSneByAdnIndex: efIndex is 1");
        Object obj2 = this.mLock;
        synchronized (obj2) {
            if (sne != null) {
                try {
                    if (sne.length() == 0) {
                        obj = obj2;
                        efIndex = 1;
                    } else if (sne.equals(oldSne)) {
                        try {
                        } catch (Throwable th2) {
                            th = th2;
                            obj = obj2;
                            throw th;
                        }
                    } else {
                        obj = obj2;
                        this.mCi.editUPBEntry(2, 1, adnIndex, encodeToUcs2(sne), null, msg);
                        try {
                            this.mLock.wait();
                        } catch (InterruptedException e) {
                            Rlog.e(LOG_TAG, "Interrupted Exception in updateSneByAdnIndex");
                        }
                        logi("updateSneByAdnIndex end, adnIndex " + adnIndex);
                        return this.mResult;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            } else {
                obj = obj2;
                efIndex = 1;
            }
            if (oldSne != null) {
                if (oldSne.length() != 0) {
                    this.mCi.deleteUPBEntry(2, efIndex, adnIndex, msg);
                    this.mLock.wait();
                    logi("updateSneByAdnIndex end, adnIndex " + adnIndex);
                    return this.mResult;
                }
            }
            return 0;
        }
        return 0;
    }

    public int[] getAdnRecordsCapacity() {
        ArrayList<int[]> arrayList;
        int[] iArr;
        int[] iArr2;
        int[] capacity = new int[6];
        if (this.mRefreshAdnInfo || this.mRefreshEmailInfo || this.mRefreshAnrInfo || (iArr2 = this.mAdnRecordSize) == null || iArr2.length != 4) {
            getAdnStorageInfo();
            this.mRefreshAdnInfo = false;
        }
        int[] iArr3 = this.mAdnRecordSize;
        if (iArr3 == null || iArr3.length != 4) {
            return null;
        }
        capacity[0] = iArr3[1];
        capacity[1] = iArr3[0];
        if (this.mRefreshEmailInfo || (iArr = this.mEmailInfo) == null || iArr.length != 3) {
            this.mCi.queryUPBAvailable(1, 1, obtainMessage(EVENT_QUERY_EMAIL_AVAILABLE_OPTMZ_DONE));
            synchronized (this.mLock) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in getAdnRecordsCapacity");
                }
            }
            this.mRefreshEmailInfo = false;
        }
        int[] iArr4 = this.mEmailInfo;
        if (iArr4 == null || iArr4.length != 3) {
            return null;
        }
        capacity[2] = iArr4[0];
        capacity[3] = iArr4[0] - iArr4[1];
        if (this.mRefreshAnrInfo || (arrayList = this.mAnrInfo) == null || arrayList.get(0) == null || this.mAnrInfo.get(0).length != 3) {
            this.mCi.queryUPBAvailable(0, 1, obtainMessage(EVENT_QUERY_ANR_AVAILABLE_OPTMZ_DONE));
            synchronized (this.mLock) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e2) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in getAdnRecordsCapacity");
                }
            }
            this.mRefreshAnrInfo = false;
        }
        ArrayList<int[]> arrayList2 = this.mAnrInfo;
        if (arrayList2 == null || arrayList2.get(0) == null || this.mAnrInfo.get(0).length != 3) {
            return null;
        }
        capacity[4] = this.mAnrInfo.get(0)[0];
        capacity[5] = this.mAnrInfo.get(0)[0] - this.mAnrInfo.get(0)[1];
        logi("getAdnRecordsCapacity: max adn=" + capacity[0] + ", used adn=" + capacity[1] + ", max email=" + capacity[2] + ", used email=" + capacity[3] + ", max anr=" + capacity[4] + ", used anr=" + capacity[5]);
        return capacity;
    }

    private int[] getAdnStorageInfo() {
        logi("getAdnStorageInfo");
        MtkRIL mtkRIL = this.mCi;
        if (mtkRIL != null) {
            mtkRIL.queryPhbStorageInfo(0, obtainMessage(21));
            synchronized (this.mLock) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in getAdnStorageInfo");
                }
            }
            return this.mAdnRecordSize;
        }
        Rlog.w(LOG_TAG, "GetAdnStorageInfo: filehandle is null.");
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:142:0x03c2, code lost:
        r0 = th;
     */
    public UsimPBMemInfo[] getPhonebookMemStorageExt() {
        int[] size;
        int used;
        boolean is3G;
        char c;
        File emailFile;
        MtkAdnRecord rec;
        boolean is3G2;
        File anrFile;
        char c2 = 0;
        boolean is3G3 = this.mCurrentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_USIM;
        logi("getPhonebookMemStorageExt isUsim " + is3G3);
        if (!is3G3) {
            return getPhonebookMemStorageExt2G();
        }
        if (this.mPbrRecords == null) {
            loadPBRFiles();
        }
        if (this.mPbrRecords == null) {
            return null;
        }
        log("getPhonebookMemStorageExt slice " + this.mPbrRecords.size());
        UsimPBMemInfo[] response = new UsimPBMemInfo[this.mPbrRecords.size()];
        for (int i = 0; i < this.mPbrRecords.size(); i++) {
            response[i] = new UsimPBMemInfo();
        }
        if (this.mPhoneBookRecords.isEmpty()) {
            Rlog.w(LOG_TAG, "mPhoneBookRecords has not been loaded.");
            return response;
        }
        int[] size2 = null;
        int used2 = 0;
        int pbrIndex = 0;
        while (pbrIndex < this.mPbrRecords.size()) {
            SparseArray<File> files = this.mPbrRecords.get(pbrIndex).mFileIds;
            int numAdnRecs = this.mPhoneBookRecords.size();
            int i2 = this.mAdnFileSize;
            int nOffset = pbrIndex * i2;
            int nMax = i2 + nOffset;
            int nMax2 = numAdnRecs < nMax ? numAdnRecs : nMax;
            File adnFile = files.get(192);
            if (adnFile != null) {
                size = readEFLinearRecordSize(adnFile.getEfid());
                if (size != null) {
                    response[pbrIndex].setAdnLength(size[c2]);
                    if (0 > 0) {
                        response[pbrIndex].setAdnTotal(size[2] + 0);
                    } else {
                        response[pbrIndex].setAdnTotal(size[2]);
                    }
                }
                response[pbrIndex].setAdnType(adnFile.getParentTag());
                response[pbrIndex].setSliceIndex(pbrIndex + 1);
                MtkAdnRecord rec2 = null;
                used = 0;
                for (int j = nOffset; j < nMax2; j++) {
                    try {
                        rec2 = this.mPhoneBookRecords.get(j);
                    } catch (IndexOutOfBoundsException e) {
                        Rlog.e(LOG_TAG, "getPhonebookMemStorageExt: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is " + this.mPhoneBookRecords.size() + "index is " + j);
                    }
                    if (rec2 != null && ((rec2.getAlphaTag() != null && rec2.getAlphaTag().length() > 0) || (rec2.getNumber() != null && rec2.getNumber().length() > 0))) {
                        log("Adn: " + rec2.toString());
                        used++;
                        rec2 = null;
                    }
                }
                log("adn used " + used);
                response[pbrIndex].setAdnUsed(used);
            } else {
                size = size2;
                used = used2;
            }
            File anrFile2 = files.get(196);
            if (anrFile2 != null) {
                size = readEFLinearRecordSize(anrFile2.getEfid());
                if (size != null) {
                    response[pbrIndex].setAnrLength(size[0]);
                    response[pbrIndex].setAnrTotal(size[2]);
                }
                response[pbrIndex].setAnrType(anrFile2.getParentTag());
                int i3 = nOffset;
                MtkAdnRecord rec3 = null;
                used = 0;
                while (i3 < nMax2) {
                    try {
                        rec3 = this.mPhoneBookRecords.get(i3);
                        is3G2 = is3G3;
                    } catch (IndexOutOfBoundsException e2) {
                        StringBuilder sb = new StringBuilder();
                        is3G2 = is3G3;
                        sb.append("getPhonebookMemStorageExt: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is ");
                        sb.append(this.mPhoneBookRecords.size());
                        sb.append("index is ");
                        sb.append(i3);
                        Rlog.e(LOG_TAG, sb.toString());
                    }
                    if (rec3 == null) {
                        log("null anr rec ");
                        anrFile = anrFile2;
                    } else {
                        String anrStr = rec3.getAdditionalNumber();
                        if (anrStr == null || anrStr.length() <= 0) {
                            anrFile = anrFile2;
                        } else {
                            StringBuilder sb2 = new StringBuilder();
                            anrFile = anrFile2;
                            sb2.append("anrStr: ");
                            sb2.append(anrStr);
                            log(sb2.toString());
                            used++;
                        }
                    }
                    i3++;
                    anrFile2 = anrFile;
                    is3G3 = is3G2;
                }
                is3G = is3G3;
                log("anr used: " + used);
                response[pbrIndex].setAnrUsed(used);
            } else {
                is3G = is3G3;
            }
            File emailFile2 = files.get(ExternalSimConstants.EVENT_TYPE_RECEIVE_RSIM_AUTH_RSP);
            if (emailFile2 != null) {
                size = readEFLinearRecordSize(emailFile2.getEfid());
                if (size != null) {
                    response[pbrIndex].setEmailLength(size[0]);
                    response[pbrIndex].setEmailTotal(size[2]);
                }
                response[pbrIndex].setEmailType(emailFile2.getParentTag());
                int i4 = nOffset;
                MtkAdnRecord rec4 = null;
                used = 0;
                while (i4 < nMax2) {
                    try {
                        rec4 = this.mPhoneBookRecords.get(i4);
                        emailFile = emailFile2;
                    } catch (IndexOutOfBoundsException e3) {
                        StringBuilder sb3 = new StringBuilder();
                        emailFile = emailFile2;
                        sb3.append("getPhonebookMemStorageExt: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is ");
                        sb3.append(this.mPhoneBookRecords.size());
                        sb3.append("index is ");
                        sb3.append(i4);
                        Rlog.e(LOG_TAG, sb3.toString());
                    }
                    if (rec4 == null) {
                        log("null email rec ");
                        rec = rec4;
                    } else {
                        String[] emails = rec4.getEmails();
                        if (emails == null || emails.length <= 0 || emails[0].length() <= 0) {
                            rec = rec4;
                        } else {
                            StringBuilder sb4 = new StringBuilder();
                            rec = rec4;
                            sb4.append("email: ");
                            sb4.append(emails[0]);
                            log(sb4.toString());
                            used++;
                        }
                    }
                    i4++;
                    rec4 = rec;
                    emailFile2 = emailFile;
                }
                log("email used: " + used);
                response[pbrIndex].setEmailUsed(used);
            }
            File ext1File = files.get(194);
            if (ext1File != null) {
                int[] size3 = readEFLinearRecordSize(ext1File.getEfid());
                if (size3 != null) {
                    response[pbrIndex].setExt1Length(size3[0]);
                    response[pbrIndex].setExt1Total(size3[2]);
                }
                response[pbrIndex].setExt1Type(ext1File.getParentTag());
                synchronized (this.mLock) {
                    readExt1FileAndWait(pbrIndex);
                }
                int used3 = 0;
                ArrayList<ArrayList<byte[]>> arrayList = this.mExt1FileList;
                if (arrayList != null && pbrIndex < arrayList.size()) {
                    ArrayList<byte[]> ext1 = this.mExt1FileList.get(pbrIndex);
                    if (ext1 != null) {
                        int len = ext1.size();
                        int i5 = 0;
                        while (i5 < len) {
                            byte[] arr = ext1.get(i5);
                            log("ext1[" + i5 + "]=" + IccUtils.bytesToHexString(arr));
                            if (arr != null && arr.length > 0 && (arr[0] == 1 || arr[0] == 2)) {
                                used3++;
                            }
                            i5++;
                            ext1File = ext1File;
                            ext1 = ext1;
                        }
                    }
                }
                response[pbrIndex].setExt1Used(used3);
                used2 = used3;
                size = size3;
            } else {
                used2 = used;
            }
            File gasFile = files.get(200);
            if (gasFile != null) {
                size = readEFLinearRecordSize(gasFile.getEfid());
                if (size != null) {
                    response[pbrIndex].setGasLength(size[0]);
                    response[pbrIndex].setGasTotal(size[2]);
                }
                response[pbrIndex].setGasType(gasFile.getParentTag());
            }
            File aasFile = files.get(199);
            if (aasFile != null) {
                size = readEFLinearRecordSize(aasFile.getEfid());
                if (size != null) {
                    response[pbrIndex].setAasLength(size[0]);
                    response[pbrIndex].setAasTotal(size[2]);
                }
                response[pbrIndex].setAasType(aasFile.getParentTag());
            }
            File sneFile = files.get(195);
            if (sneFile != null) {
                size = readEFLinearRecordSize(sneFile.getEfid());
                if (size != null) {
                    response[pbrIndex].setSneLength(size[0]);
                    response[pbrIndex].setSneTotal(size[0]);
                }
                response[pbrIndex].setSneType(sneFile.getParentTag());
            }
            File ccpFile = files.get(ExternalSimConstants.EVENT_TYPE_RSIM_AUTH_DONE);
            if (ccpFile != null) {
                int[] size4 = readEFLinearRecordSize(ccpFile.getEfid());
                if (size4 != null) {
                    c = 0;
                    response[pbrIndex].setCcpLength(size4[0]);
                    response[pbrIndex].setCcpTotal(size4[0]);
                } else {
                    c = 0;
                }
                response[pbrIndex].setCcpType(ccpFile.getParentTag());
                size2 = size4;
            } else {
                c = 0;
                size2 = size;
            }
            pbrIndex++;
            c2 = c;
            is3G3 = is3G;
        }
        for (int i6 = 0; i6 < this.mPbrRecords.size(); i6++) {
            log("getPhonebookMemStorageExt[" + i6 + "]:" + response[i6]);
        }
        return response;
        while (true) {
        }
    }

    public UsimPBMemInfo[] getPhonebookMemStorageExt2G() {
        ArrayList<byte[]> ext1;
        UsimPBMemInfo[] response = {new UsimPBMemInfo()};
        int[] size = readEFLinearRecordSize(28474);
        if (size != null) {
            response[0].setAdnLength(size[0]);
            if (isAdnAccessible()) {
                response[0].setAdnTotal(size[2]);
            } else {
                response[0].setAdnTotal(0);
            }
        }
        response[0].setAdnType(168);
        response[0].setSliceIndex(1);
        int[] size2 = readEFLinearRecordSize(28490);
        if (size2 != null) {
            response[0].setExt1Length(size2[0]);
            response[0].setExt1Total(size2[2]);
        }
        response[0].setExt1Type(170);
        synchronized (this.mLock) {
            if (this.mFh != null) {
                Message msg = obtainMessage(1001);
                msg.arg1 = 0;
                this.mFh.loadEFLinearFixedAll(28490, msg);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readExt1FileAndWait");
                }
            } else {
                Rlog.e(LOG_TAG, "readExt1FileAndWait-IccFileHandler is null");
                return response;
            }
        }
        int used = 0;
        ArrayList<ArrayList<byte[]>> arrayList = this.mExt1FileList;
        if (!(arrayList == null || arrayList.size() <= 0 || (ext1 = this.mExt1FileList.get(0)) == null)) {
            int len = ext1.size();
            for (int i = 0; i < len; i++) {
                byte[] arr = ext1.get(i);
                log("ext1[" + i + "]=" + IccUtils.bytesToHexString(arr));
                if (arr != null && arr.length > 0 && (arr[0] == 1 || arr[0] == 2)) {
                    used++;
                }
            }
        }
        response[0].setExt1Used(used);
        logi("getPhonebookMemStorageExt2G:" + response[0]);
        return response;
    }

    public int[] readEFLinearRecordSize(int fileId) {
        int[] recordSize;
        logi("readEFLinearRecordSize fileid:" + Integer.toHexString(fileId).toUpperCase() + ",recordNum:" + this.mReadEFLinerRecordSizeNum);
        Message msg = obtainMessage(EVENT_GET_RECORDS_SIZE_DONE);
        msg.arg1 = fileId;
        synchronized (this.mLock) {
            if (this.mFh != null) {
                this.mReadEFLinerRecordSizeNum++;
                this.mFh.getEFLinearRecordSize(fileId, msg);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readEFLinearRecordSize");
                }
            } else {
                Rlog.e(LOG_TAG, "readEFLinearRecordSize-IccFileHandler is null");
            }
            if (this.mFh != null) {
                this.mReadEFLinerRecordSizeNum--;
            }
            recordSize = this.mRecordSize != null ? this.mRecordSize.get(fileId) : null;
            if (recordSize != null) {
                logi("readEFLinearRecordSize fileid:" + Integer.toHexString(fileId).toUpperCase() + ",len:" + recordSize[0] + ",total:" + recordSize[1] + ",count:" + recordSize[2] + ",recordNum:" + this.mReadEFLinerRecordSizeNum);
            } else {
                logi("readEFLinearRecordSize fileid:" + Integer.toHexString(fileId).toUpperCase() + ",recordSize: null");
            }
        }
        return recordSize;
    }

    private void readExt1FileAndWait(int recId) {
        logi("readExt1FileAndWait " + recId);
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (arrayList != null && arrayList.size() != 0 && this.mPbrRecords.get(recId) != null) {
            SparseArray<File> files = this.mPbrRecords.get(recId).mFileIds;
            if (files == null || files.get(194) == null) {
                Rlog.e(LOG_TAG, "readExt1FileAndWait-PBR have no Ext1 record");
                return;
            }
            int efid = files.get(194).getEfid();
            log("readExt1FileAndWait-get EXT1 EFID " + efid);
            ArrayList<ArrayList<byte[]>> arrayList2 = this.mExt1FileList;
            if (arrayList2 != null && recId < arrayList2.size()) {
                log("EXT1 has been loaded for Pbr number " + recId);
            } else if (this.mFh != null) {
                Message msg = obtainMessage(1001);
                msg.arg1 = recId;
                this.mFh.loadEFLinearFixedAll(efid, msg);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readExt1FileAndWait");
                }
            } else {
                Rlog.e(LOG_TAG, "readExt1FileAndWait-IccFileHandler is null");
            }
        }
    }

    private boolean checkIsPhbReady() {
        String strCurSimState = "";
        if (!SubscriptionManager.isValidSlotIndex(this.mSlotId)) {
            log("[isPhbReady] InvalidSlotId slotId: " + this.mSlotId);
            return false;
        }
        String strPhbReady = TelephonyManager.getTelephonyProperty(this.mSlotId, "vendor.gsm.sim.ril.phbready", "false");
        if (SystemProperties.get("ro.vendor.mtk_ril_mode").equals("c6m_1rild")) {
            logi("[isPhbReady] isPhbReady: " + strPhbReady);
            return strPhbReady.equals("true");
        }
        String strAllSimState = SystemProperties.get("gsm.sim.state");
        if (strAllSimState != null && strAllSimState.length() > 0) {
            String[] values = strAllSimState.split(",");
            int i = this.mSlotId;
            if (i >= 0 && i < values.length && values[i] != null) {
                strCurSimState = values[i];
            }
        }
        boolean isSimLocked = strCurSimState.equals("NETWORK_LOCKED") || strCurSimState.equals("PIN_REQUIRED");
        logi("[isPhbReady] isPhbReady: " + strPhbReady + ",strSimState: " + strAllSimState);
        if (!strPhbReady.equals("true") || isSimLocked) {
            return false;
        }
        return true;
    }

    public boolean isAdnAccessible() {
        if (this.mFh != null && this.mCurrentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_SIM) {
            synchronized (this.mLock) {
                this.mFh.selectEFFile(28474, obtainMessage(20));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in isAdnAccessible");
                }
            }
            EFResponseData eFResponseData = this.mEfData;
            if (eFResponseData == null || (eFResponseData.getFileStatus() & 5) > 0) {
                return true;
            }
            return false;
        }
        return true;
    }

    public boolean isUsimPhbEfAndNeedReset(int fileId) {
        logi("isUsimPhbEfAndNeedReset, fileId: " + Integer.toHexString(fileId).toUpperCase());
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (arrayList == null) {
            Rlog.e(LOG_TAG, "isUsimPhbEfAndNeedReset, No PBR files");
            return false;
        }
        int numRecs = arrayList.size();
        for (int i = 0; i < numRecs; i++) {
            SparseArray<File> files = this.mPbrRecords.get(i).mFileIds;
            for (int j = 192; j <= 203; j++) {
                if (j == 197 || j == 201 || j == 203) {
                    logi("isUsimPhbEfAndNeedReset, not reset EF: " + j);
                } else if (files.get(j) != null && fileId == files.get(j).getEfid()) {
                    logi("isUsimPhbEfAndNeedReset, return true with EF: " + j);
                    return true;
                }
            }
        }
        log("isUsimPhbEfAndNeedReset, return false.");
        return false;
    }

    private void readAdnFileAndWaitForUICC(int recId) {
        SparseArray<File> files;
        logi("readAdnFileAndWaitForUICC begin" + recId);
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (arrayList != null && arrayList.size() != 0 && (files = this.mPbrRecords.get(recId).mFileIds) != null && files.size() != 0) {
            if (files.get(192) == null) {
                Rlog.e(LOG_TAG, "readAdnFileAndWaitForUICC: No ADN tag in pbr record " + recId);
                return;
            }
            int efid = files.get(192).getEfid();
            log("readAdnFileAndWaitForUICC: EFADN id is " + efid);
            log("UiccPhoneBookManager readAdnFileAndWaitForUICC: recId is " + recId + "");
            MtkAdnRecordCache mtkAdnRecordCache = this.mAdnCache;
            mtkAdnRecordCache.requestLoadAllAdnLike(efid, mtkAdnRecordCache.extensionEfForEf(28474), obtainMessage(2));
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                Rlog.e(LOG_TAG, "Interrupted Exception in readAdnFileAndWait");
            }
            int previousSize = this.mPhoneBookRecords.size();
            ArrayList<PbrRecord> arrayList2 = this.mPbrRecords;
            if (arrayList2 != null && arrayList2.size() > recId) {
                this.mPbrRecords.get(recId).mMasterFileRecordNum = this.mPhoneBookRecords.size() - previousSize;
            }
            logi("readAdnFileAndWaitForUICC end" + recId);
        }
    }

    private ArrayList<MtkAdnRecord> changeAdnRecordNumber(int baseNumber, ArrayList<MtkAdnRecord> adnList) {
        int size = adnList.size();
        for (int i = 0; i < size; i++) {
            MtkAdnRecord adnRecord = adnList.get(i);
            if (adnRecord != null) {
                adnRecord.setRecordIndex(adnRecord.getRecId() + baseNumber);
            }
        }
        return adnList;
    }

    public boolean isPbrExsit() {
        log("isPbrExsit: mIsPbrPresent = " + this.mIsPbrPresent);
        return this.mIsPbrPresent.booleanValue();
    }

    private boolean isSupportSne() {
        log("isSupportSne: false");
        return false;
    }

    public int oppoUpdateAdn(int efid, MtkAdnRecord oldAdn, MtkAdnRecord newAdn, int index, String pin2, Message response) {
        int index2;
        int recordIndex;
        int index3 = index;
        log("oppoUpdateAdn: efid = 0x" + Integer.toHexString(efid) + " index = " + index3);
        if (efid == 28474) {
            if (newAdn != null) {
                int count = 1;
                if (-1 == index3) {
                    Iterator<MtkAdnRecord> it = this.mPhoneBookRecords.iterator();
                    while (true) {
                        if (it.hasNext()) {
                            if (oldAdn != null && oldAdn.isEqual(it.next())) {
                                index3 = count;
                                break;
                            }
                            count++;
                        } else {
                            break;
                        }
                    }
                    if (-1 == index3) {
                        return -1;
                    }
                    index2 = index3;
                } else {
                    index2 = index3;
                }
                Iterator<Integer> it2 = this.mOPPOEFRecNum.iterator();
                int recNum = 0;
                int count2 = 0;
                while (true) {
                    if (!it2.hasNext()) {
                        recordIndex = -1;
                        break;
                    }
                    int k = it2.next().intValue();
                    if (index2 <= count2 + k) {
                        recordIndex = index2 - count2;
                        break;
                    }
                    recNum++;
                    count2 += k;
                }
                if (-1 != recordIndex) {
                    ArrayList<PbrRecord> arrayList = this.mPbrRecords;
                    if (arrayList != null) {
                        SparseArray<File> FileIds = arrayList.get(recNum).mFileIds;
                        if (FileIds != null) {
                            if (FileIds.size() >= 1) {
                                if (this.pendingResponse != null) {
                                    log("oppoUpdateAdn: pendingResponse not null");
                                    return -1;
                                }
                                this.pendingResponse = response;
                                this.mAdnCache.updateAdnByIndex(FileIds.get(192).getEfid(), newAdn, recordIndex, pin2, this.mOppoHandler.obtainMessage(90, index2, recordIndex, newAdn));
                                return index2;
                            }
                        }
                        log("oppoUpdateAdn: FileIds error");
                        return -1;
                    }
                }
                log("oppoUpdateAdn: recordIndex or mPbrRecords error");
                return -1;
            }
        }
        log("oppoUpdateAdn: efid must is EF_ADN and newAdn can't  null");
        return -1;
    }

    public int oppoGetAdnEfIdForUsim() {
        SparseArray<File> FileIds;
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (arrayList == null || (FileIds = arrayList.get(0).mFileIds) == null) {
            return 28474;
        }
        return FileIds.get(192).getEfid();
    }

    public int oppoGetExt1EfIdForUsim() {
        SparseArray<File> FileIds;
        ArrayList<PbrRecord> arrayList = this.mPbrRecords;
        if (arrayList == null || (FileIds = arrayList.get(0).mFileIds) == null) {
            return 0;
        }
        return FileIds.get(194).getEfid();
    }

    public int getUsimEmailLength() {
        return this.mEmailRecordSize;
    }

    private int oppoGePbcEFidForUsim(int index) {
        int recNum = 0;
        int recordIndex = -1;
        int count = 0;
        Iterator<Integer> it = this.mOPPOEFRecNum.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            int k = it.next().intValue();
            if (index <= count + k) {
                recordIndex = index - count;
                break;
            }
            recNum++;
            count += k;
        }
        if (-1 == recordIndex) {
            log("oppoGePbcEFidForUsim: not found recordIndex!!!");
            return -1;
        }
        SparseArray<File> FileIds = this.mPbrRecords.get(recNum).mFileIds;
        if (FileIds == null) {
            log("ClearEFPbc:mFileIds is null for record:" + recNum);
            return -1;
        }
        File file = FileIds.get(197);
        if (file != null) {
            return file.getEfid();
        }
        log("ClearEFPbc:File is null for record:" + recNum);
        return -1;
    }

    private void ClearEFPbc() {
        int numRecs = this.mPbrRecords.size();
        for (int i = 0; i < numRecs; i++) {
            SparseArray<File> FileIds = this.mPbrRecords.get(i).mFileIds;
            if (FileIds == null) {
                log("ClearEFPbc:mFileIds is null for number:" + i);
                return;
            }
            File file = FileIds.get(197);
            if (file == null) {
                log("ClearEFPbc:File is null for number:" + i);
                return;
            }
            int efid = file.getEfid();
            this.mFh.getEFLinearRecordSize(efid, this.mOppoHandler.obtainMessage(94, i, efid));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearAllEFPbcControlInformation(int efid, int recordSize, int recordCount) {
        for (int numRecords = 1; numRecords <= recordCount; numRecords++) {
            this.mFh.oppoReadEFLinearFixedRecord(efid, numRecords, recordSize, this.mOppoHandler.obtainMessage(92, efid, numRecords));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetEFpbcControlInfor(int efid, int numRecords) {
        this.mFh.updateEFLinearFixed(efid, numRecords, new byte[]{0, 0}, (String) null, (Message) null);
        this.mFh.loadEFTransparent(20259, this.mOppoHandler.obtainMessage(93));
    }
}
