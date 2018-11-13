package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.SpnOverride;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class IccPhoneBookInterfaceManager {
    protected static final boolean ALLOW_SIM_OP_IN_UI_THREAD = false;
    protected static final boolean DBG = OemConstant.SWITCH_LOG;
    protected static final int EVENT_GET_SIZE_DONE = 1;
    protected static final int EVENT_LOAD_DONE = 2;
    protected static final int EVENT_UPDATE_DONE = 3;
    static final String LOG_TAG = "IccPhoneBookIM";
    protected static final int OPPO_EVENT_ADN_FIELD_DONE = 102;
    public static final HandlerThread mHandlerThread = new HandlerThread("OemSimStability", -2);
    protected AdnRecordCache mAdnCache;
    private int mAdnRecordIndex;
    protected final IccPbHandler mBaseHandler;
    private UiccCardApplication mCurrentApp = null;
    private boolean mIs3gCard = false;
    protected boolean mIsCsim = false;
    protected boolean mIsRuim = false;
    protected final Object mLock = new Object();
    protected Phone mPhone;
    protected int[] mRecordSize;
    protected List<AdnRecord> mRecords;
    protected boolean mSuccess;
    protected boolean phonebookReady = false;
    protected int simNameLeng;
    protected int simrecord_efid;

    protected class IccPbHandler extends Handler {
        public IccPbHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            AsyncResult ar;
            Object obj;
            switch (msg.what) {
                case 1:
                    ar = msg.obj;
                    obj = IccPhoneBookInterfaceManager.this.mLock;
                    synchronized (obj) {
                        if (ar.exception == null) {
                            IccPhoneBookInterfaceManager.this.mRecordSize = (int[]) ar.result;
                            IccPhoneBookInterfaceManager.this.logd("GET_RECORD_SIZE Size " + IccPhoneBookInterfaceManager.this.mRecordSize[0] + " total " + IccPhoneBookInterfaceManager.this.mRecordSize[1] + " #record " + IccPhoneBookInterfaceManager.this.mRecordSize[2]);
                        }
                        notifyPending(ar);
                        break;
                    }
                case 2:
                    ar = (AsyncResult) msg.obj;
                    obj = IccPhoneBookInterfaceManager.this.mLock;
                    synchronized (obj) {
                        if (ar.exception == null) {
                            if (IccPhoneBookInterfaceManager.DBG) {
                                IccPhoneBookInterfaceManager.this.logd("Load ADN records done");
                            }
                            IccPhoneBookInterfaceManager.this.mRecords = (List) ar.result;
                            IccPhoneBookInterfaceManager.this.phonebookReady = true;
                            IccPhoneBookInterfaceManager.this.simrecord_efid = msg.arg1;
                            IccPhoneBookInterfaceManager.this.broadcastIccPhoneBookReadyIntent("PBREADY", null);
                        } else {
                            if (IccPhoneBookInterfaceManager.DBG) {
                                IccPhoneBookInterfaceManager.this.logd("Cannot load ADN records");
                            }
                            IccPhoneBookInterfaceManager.this.mRecords = null;
                            IccPhoneBookInterfaceManager.this.simrecord_efid = -1;
                        }
                        notifyPending(ar);
                        break;
                    }
                case 3:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null && IccPhoneBookInterfaceManager.DBG) {
                        IccPhoneBookInterfaceManager.this.logd("exception of EVENT_UPDATE_DONE is" + ar.exception);
                    }
                    synchronized (IccPhoneBookInterfaceManager.this.mLock) {
                        IccPhoneBookInterfaceManager iccPhoneBookInterfaceManager = IccPhoneBookInterfaceManager.this;
                        if (ar.exception != null) {
                            z = false;
                        }
                        iccPhoneBookInterfaceManager.mSuccess = z;
                        if (ar.exception != null || ar.result == null) {
                            IccPhoneBookInterfaceManager.this.mAdnRecordIndex = -1;
                        } else {
                            IccPhoneBookInterfaceManager.this.mAdnRecordIndex = ((Integer) ar.result).intValue();
                        }
                        IccPhoneBookInterfaceManager.this.logd("mAdnRecordIndex: " + IccPhoneBookInterfaceManager.this.mAdnRecordIndex);
                        notifyPending(ar);
                    }
                    return;
                case 102:
                    ar = (AsyncResult) msg.obj;
                    obj = IccPhoneBookInterfaceManager.this.mLock;
                    synchronized (obj) {
                        if (ar.exception == null) {
                            int[] fieldInfo = ar.result;
                            if (fieldInfo.length == 2) {
                                IccPhoneBookInterfaceManager.this.simNameLeng = fieldInfo[1];
                            }
                        }
                        notifyPending(ar);
                        break;
                    }
                default:
                    return;
            }
        }

        private void notifyPending(AsyncResult ar) {
            if (ar != null && ar.userObj != null) {
                try {
                    ar.userObj.set(true);
                    IccPhoneBookInterfaceManager.this.mLock.notifyAll();
                } catch (ClassCastException e) {
                    IccPhoneBookInterfaceManager.this.loge("notifyPending " + e.getMessage());
                }
            }
        }
    }

    static {
        mHandlerThread.start();
    }

    public IccPhoneBookInterfaceManager(Phone phone) {
        this.mPhone = phone;
        IccRecords r = phone.getIccRecords();
        if (r != null) {
            this.mAdnCache = r.getAdnCache();
        }
        this.mBaseHandler = new IccPbHandler(mHandlerThread.getLooper());
        this.simNameLeng = -1;
    }

    public void dispose() {
        this.mIs3gCard = false;
        this.mIsCsim = false;
        this.mIsRuim = false;
        this.simNameLeng = -1;
    }

    public void updateIccRecords(IccRecords iccRecords) {
        if (iccRecords != null) {
            this.mAdnCache = iccRecords.getAdnCache();
        } else {
            this.mAdnCache = null;
        }
    }

    protected void logd(String msg) {
        Rlog.d(LOG_TAG, "[IccPbInterfaceManager] " + msg);
    }

    protected void loge(String msg) {
        Rlog.e(LOG_TAG, "[IccPbInterfaceManager] " + msg);
    }

    public boolean updateAdnRecordsInEfBySearch(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        }
        if (DBG) {
            logd("updateAdnRecordsInEfBySearch: efid=0x" + Integer.toHexString(efid).toUpperCase() + " (" + Rlog.pii(LOG_TAG, oldTag) + "," + Rlog.pii(LOG_TAG, oldPhoneNumber) + ")" + "==>" + " (" + Rlog.pii(LOG_TAG, newTag) + "," + Rlog.pii(LOG_TAG, newPhoneNumber) + ")" + " pin2=" + Rlog.pii(LOG_TAG, pin2));
        }
        efid = updateEfForIccType(efid);
        synchronized (this.mLock) {
            checkThread();
            this.mSuccess = false;
            AtomicBoolean status = new AtomicBoolean(false);
            Message response = this.mBaseHandler.obtainMessage(3, status);
            AdnRecord oldAdn = new AdnRecord(oldTag, oldPhoneNumber);
            AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber);
            if (this.mAdnCache != null) {
                this.mAdnCache.updateAdnBySearch(efid, oldAdn, newAdn, pin2, response);
                waitForResult(status);
            } else {
                loge("Failure while trying to update by search due to uninitialised adncache");
            }
        }
        return this.mSuccess;
    }

    public boolean updateAdnRecordsWithContentValuesInEfBySearch(int efid, ContentValues values, String pin2) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        }
        String oldTag = values.getAsString(IccProvider.STR_TAG);
        String newTag = values.getAsString(IccProvider.STR_NEW_TAG);
        String oldPhoneNumber = values.getAsString("number");
        String newPhoneNumber = values.getAsString(IccProvider.STR_NEW_NUMBER);
        String oldEmail = values.getAsString(IccProvider.STR_EMAILS);
        String newEmail = values.getAsString(IccProvider.STR_NEW_EMAILS);
        String oldAnr = values.getAsString(IccProvider.STR_ANRS);
        String newAnr = values.getAsString(IccProvider.STR_NEW_ANRS);
        String[] oldEmailArray = TextUtils.isEmpty(oldEmail) ? null : getStringArray(oldEmail);
        String[] newEmailArray = TextUtils.isEmpty(newEmail) ? null : getStringArray(newEmail);
        String[] oldAnrArray = TextUtils.isEmpty(oldAnr) ? null : getAnrStringArray(oldAnr);
        String[] newAnrArray = TextUtils.isEmpty(newAnr) ? null : getAnrStringArray(newAnr);
        efid = updateEfForIccType(efid);
        int index = -1;
        String strIndex = values.getAsString(IccProvider.STR_INDEX);
        if (!TextUtils.isEmpty(strIndex)) {
            index = Integer.valueOf(strIndex).intValue();
        }
        if (DBG) {
            logd("updateAdnRecordsWithContentValuesInEfBySearch: efid=" + efid + ", values = " + values + ", pin2=" + pin2);
        }
        synchronized (this.mLock) {
            AdnRecord oldAdn;
            checkThread();
            this.mSuccess = false;
            AtomicBoolean atomicBoolean = new AtomicBoolean(false);
            Message response = this.mBaseHandler.obtainMessage(3, atomicBoolean);
            if (index > 0) {
                oldAdn = new AdnRecord(0, index, oldTag, oldPhoneNumber, oldEmailArray, oldAnrArray);
            } else {
                oldAdn = new AdnRecord(oldTag, oldPhoneNumber, oldEmailArray, oldAnrArray);
            }
            AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber, newEmailArray, newAnrArray);
            if (this.mAdnCache != null) {
                this.mAdnCache.updateAdnBySearch(efid, oldAdn, newAdn, pin2, response);
                waitForResult(atomicBoolean);
            } else {
                loge("Failure while trying to update by search due to uninitialised adncache");
            }
        }
        return this.mSuccess;
    }

    public boolean updateAdnRecordsInEfByIndex(int efid, String newTag, String newPhoneNumber, int index, String pin2) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        }
        if (DBG) {
            logd("updateAdnRecordsInEfByIndex: efid=0x" + Integer.toHexString(efid).toUpperCase() + " Index=" + index + " ==> " + "(" + Rlog.pii(LOG_TAG, newTag) + "," + Rlog.pii(LOG_TAG, newPhoneNumber) + ")" + " pin2=" + Rlog.pii(LOG_TAG, pin2));
        }
        synchronized (this.mLock) {
            checkThread();
            this.mSuccess = false;
            AtomicBoolean status = new AtomicBoolean(false);
            Message response = this.mBaseHandler.obtainMessage(3, status);
            AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber);
            if (this.mAdnCache != null) {
                this.mAdnCache.updateAdnByIndex(efid, newAdn, index, pin2, response);
                waitForResult(status);
            } else {
                loge("Failure while trying to update by index due to uninitialised adncache");
            }
        }
        return this.mSuccess;
    }

    public int[] getAdnRecordsSize(int efid) {
        if (DBG) {
            logd("getAdnRecordsSize: efid=" + efid);
        }
        synchronized (this.mLock) {
            checkThread();
            this.mRecordSize = new int[3];
            AtomicBoolean status = new AtomicBoolean(false);
            Message response = this.mBaseHandler.obtainMessage(1, status);
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh != null) {
                fh.getEFLinearRecordSize(efid, response);
                waitForResult(status);
            }
        }
        return this.mRecordSize;
    }

    public List<AdnRecord> getAdnRecordsInEf(int efid) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        }
        efid = updateEfForIccType(efid);
        if (DBG) {
            logd("getAdnRecordsInEF: efid=0x" + Integer.toHexString(efid).toUpperCase() + " for slot:" + this.mPhone.getPhoneId());
        }
        synchronized (this.mLock) {
            checkThread();
            AtomicBoolean status = new AtomicBoolean(false);
            Message response = this.mBaseHandler.obtainMessage(2, efid, 0, status);
            if (this.mAdnCache != null) {
                this.mAdnCache.requestLoadAllAdnLike(efid, this.mAdnCache.extensionEfForEf(efid), response);
                waitForResult(status);
            } else {
                loge("Failure while trying to load from SIM due to uninitialised adncache");
            }
        }
        return this.mRecords;
    }

    protected void checkThread() {
        if (Looper.getMainLooper().equals(Looper.myLooper())) {
            loge("query() called on the main UI thread!");
            throw new IllegalStateException("You cannot call query on this provder from the main UI thread.");
        }
    }

    protected void waitForResult(AtomicBoolean status) {
        while (!status.get()) {
            try {
                this.mLock.wait(1800000);
            } catch (InterruptedException e) {
                logd("interrupted while trying to update by search");
            }
        }
    }

    protected int updateEfForIccType(int efid) {
        if (this.mIsCsim && efid == 28474 && this.mAdnCache != null && !this.mAdnCache.oppoCheckPbrIsExsit()) {
            logd("yangli CSIM read 0x6F3A");
            return 28474;
        } else if (efid == 28474 && this.mPhone.getCurrentUiccAppType() == AppType.APPTYPE_USIM) {
            return IccConstants.EF_PBR;
        } else {
            return efid;
        }
    }

    protected String[] getStringArray(String str) {
        if (str != null) {
            return str.split(",");
        }
        return null;
    }

    protected String[] getAnrStringArray(String str) {
        if (str != null) {
            return str.split(":");
        }
        return null;
    }

    public int[] getAdnRecordsCapacity() {
        if (DBG) {
            logd("getAdnRecordsCapacity");
        }
        return new int[10];
    }

    public int oppoGetAdnEmailLen() {
        return 30;
    }

    public int oppoGetSimPhonebookAllSpace() {
        if (this.phonebookReady) {
            logd("oppoGetSimPhonebookAllSpace:" + 0 + " for slot:" + this.mPhone.getPhoneId());
            return 0;
        }
        logd("oppoGetSimPhonebookAllSpace: phonebook not ready for slot:" + this.mPhone.getPhoneId());
        return -1;
    }

    public int oppoGetSimPhonebookUsedSpace() {
        logd("oppoGetSimPhonebookUsedSpace");
        if (this.phonebookReady) {
            logd("oppoGetSimPhonebookUsedSpace:" + 0 + " for slot:" + this.mPhone.getPhoneId());
            return 0;
        }
        logd("oppoGetSimPhonebookUsedSpace: phonebook not ready for slot:" + this.mPhone.getPhoneId());
        return -1;
    }

    public AdnRecord oppoGetAndRecordByIndex(int efid, int index) {
        logd("oppoGetAndRecordByIndex index = " + index);
        if (efid == 28474) {
            if (!(this.simrecord_efid == 28474 || this.simrecord_efid == IccConstants.EF_PBR)) {
                getAdnRecordsInEf(28474);
            }
        } else if (efid == IccConstants.EF_FDN && this.simrecord_efid != efid) {
            getAdnRecordsInEf(IccConstants.EF_FDN);
        }
        for (AdnRecord adn : this.mRecords) {
            if (adn.getRecordNumber() == index) {
                return adn;
            }
        }
        logd("no record find for index = " + index);
        return null;
    }

    public int oppoGetSimPhonebookNameLength() {
        int i = 14;
        logd("into oppoGetSimPhonebookNameLength");
        if (!this.phonebookReady) {
            logd("oppoGetSimPhonebookNameLength phone book is not ready return default len 14");
            return 14;
        } else if (this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.ct.optr")) {
            this.simNameLeng = 14;
            return this.simNameLeng;
        } else {
            synchronized (this.mLock) {
                if (this.simNameLeng <= 0) {
                    checkThread();
                    getAdnRecordsSize(28474);
                    if (this.mRecordSize != null && this.mRecordSize.length == 3) {
                        this.simNameLeng = this.mRecordSize[0] - 14;
                    }
                }
            }
            if (OemConstant.EXP_VERSION) {
                this.simNameLeng = this.simNameLeng <= 0 ? 10 : this.simNameLeng;
            } else {
                if (this.simNameLeng > 0) {
                    i = this.simNameLeng;
                }
                this.simNameLeng = i;
            }
            logd("out v1 oppoGetSimPhonebookNameLength = " + this.simNameLeng);
            return this.simNameLeng;
        }
    }

    public int oppoAddAdnRecordsInEfBySearchEx(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber1, String newPhoneNumber2, String pin2, String email) {
        int index = -1;
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (!this.phonebookReady || this.mAdnCache == null) {
            return -1;
        } else {
            synchronized (this.mLock) {
                checkThread();
                this.mSuccess = false;
                AtomicBoolean status = new AtomicBoolean(false);
                Message response = this.mBaseHandler.obtainMessage(3, status);
                AdnRecord oldAdn = new AdnRecord(oldTag, oldPhoneNumber);
                AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber1);
                if (this.mAdnCache != null) {
                    if (newPhoneNumber2 != null) {
                        newAdn.setNumber2(newPhoneNumber2);
                    }
                    if (email != null) {
                        newAdn.setEmails(new String[]{email.toString()});
                    }
                    index = this.mAdnCache.oppoUpdateAdnBySearch(efid, oldAdn, newAdn, pin2, response);
                    waitForResult(status);
                }
            }
            if (!this.mSuccess) {
                index = -1;
            }
            return index;
        }
    }

    public boolean oppoUpdateAdnRecordsInEfByIndexEx(int efid, String newTag, String newPhoneNumber1, String newPhoneNumber2, int index, String pin2, String email) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (!this.phonebookReady || this.mAdnCache == null) {
            return false;
        } else {
            synchronized (this.mLock) {
                checkThread();
                this.mSuccess = false;
                AtomicBoolean status = new AtomicBoolean(false);
                Message response = this.mBaseHandler.obtainMessage(3, status);
                AdnRecord newAdn = new AdnRecord(efid, index, newTag, newPhoneNumber1);
                if (newPhoneNumber2 != null) {
                    newAdn.setNumber2(newPhoneNumber2);
                }
                if (email != null) {
                    newAdn.setEmails(new String[]{email.toString()});
                }
                this.mAdnCache.oppoUpdateAdnByIndex(efid, this.mAdnCache.extensionEfForEf(efid), newAdn, index, pin2, response);
                waitForResult(status);
            }
            return this.mSuccess;
        }
    }

    public boolean isPhoneBookReady() {
        return this.phonebookReady;
    }

    public void broadcastIccPhoneBookReadyIntent(String value, String reason) {
        Intent intent = new Intent("android.intent.action.PBM_STATE_READY");
        intent.putExtra("pbstate", value);
        logd("Broadcasting intent ACTION_PBM_STATE_READY,value:" + value + ", reason " + reason + " ,for slot:" + this.mPhone.getPhoneId());
        ActivityManagerNative.broadcastStickyIntent(intent, null, -1);
    }

    public void resetSimNameLength() {
        this.simNameLeng = -1;
    }

    public boolean isPhoneBookPbrExist() {
        if (this.mAdnCache == null) {
            return false;
        }
        return this.mAdnCache.oppoCheckPbrIsExsit();
    }

    public String colorGetIccCardType() {
        String vRet = SpnOverride.MVNO_TYPE_NONE;
        if (this.mIsCsim) {
            vRet = "CSIM";
        } else if (this.mIsRuim) {
            vRet = "RUIM";
        } else if (this.mIs3gCard) {
            vRet = "USIM";
        } else {
            vRet = "SIM";
        }
        logd("colorGetIccCardType---->" + vRet);
        return vRet;
    }

    public int oppoUpdateAdnRecordsWithContentValuesInEfBySearch(int efid, ContentValues values, String pin2) {
        boolean result = updateAdnRecordsWithContentValuesInEfBySearch(efid, values, pin2);
        if (this.mSuccess) {
            return this.mAdnRecordIndex;
        }
        loge("update adn not success!");
        return -1;
    }

    public void SetIccCardTypeFromApp(UiccCard card) {
        logd("SetIccCardTypeFromApp: " + card);
        if (card != null) {
            if (DBG) {
                logd("SetIccCardTypeFromApp getCardState" + card.getCardState());
            }
            if (card.getCardState() == CardState.CARDSTATE_ABSENT) {
                this.mIs3gCard = false;
                this.mIsCsim = false;
                this.mIsRuim = false;
                return;
            }
        }
        if (card == null) {
            logd("Card is null");
            this.mIs3gCard = false;
            this.mIsCsim = false;
            this.mIsRuim = false;
            return;
        }
        int numApps = card.getNumApplications();
        this.mIs3gCard = false;
        for (int i = 0; i < numApps; i++) {
            UiccCardApplication app = card.getApplicationIndex(i);
            if (app != null) {
                AppType type = app.getType();
                if (type == AppType.APPTYPE_CSIM || type == AppType.APPTYPE_USIM || type == AppType.APPTYPE_ISIM) {
                    logd("Card is 3G");
                    this.mIs3gCard = true;
                }
                if (type == AppType.APPTYPE_CSIM) {
                    this.mIsCsim = true;
                }
                if (type == AppType.APPTYPE_RUIM) {
                    this.mIsRuim = true;
                }
            }
        }
    }
}
