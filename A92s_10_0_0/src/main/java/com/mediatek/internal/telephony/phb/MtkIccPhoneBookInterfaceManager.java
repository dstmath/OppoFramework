package com.mediatek.internal.telephony.phb;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.IccPhoneBookInterfaceManager;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.uicc.MtkRuimRecords;
import com.mediatek.internal.telephony.uicc.MtkSIMRecords;
import java.util.List;

public class MtkIccPhoneBookInterfaceManager extends IccPhoneBookInterfaceManager {
    protected static final boolean DBG = (!SystemProperties.get("ro.build.type").equals(DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER));
    static final String LOG_TAG = "MtkIccPhoneBookIM";
    public static final HandlerThread mHandlerThread = new HandlerThread("OemSimStability", -2);
    /* access modifiers changed from: private */
    public int mErrorCause;
    private IccRecords mIccRecords;
    protected final IccPbHandler mMtkBaseHandler;
    /* access modifiers changed from: private */
    public List<MtkAdnRecord> mRecords;
    private int mSlotId = -1;

    static {
        mHandlerThread.start();
    }

    public MtkIccPhoneBookInterfaceManager(Phone phone) {
        super(phone);
        this.phonebookReady = false;
        this.mMtkBaseHandler = new IccPbHandler(mHandlerThread.getLooper());
    }

    protected class IccPbHandler extends Handler {
        public IccPbHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            IccPhoneBookInterfaceManager.Request request = (IccPhoneBookInterfaceManager.Request) ar.userObj;
            int i = msg.what;
            if (i == 1) {
                MtkIccPhoneBookInterfaceManager.this.mBaseHandler.handleMessage(msg);
            } else if (i == 2) {
                List<MtkAdnRecord> records = null;
                if (ar.exception == null) {
                    records = (List) ar.result;
                    List unused = MtkIccPhoneBookInterfaceManager.this.mRecords = (List) ar.result;
                    MtkIccPhoneBookInterfaceManager.this.phonebookReady = true;
                    if (OemConstant.EXP_VERSION) {
                        int phoneId = MtkIccPhoneBookInterfaceManager.this.mPhone.getPhoneId();
                        if (MtkIccPhoneBookInterfaceManager.this.getAnrCount() > 0) {
                            TelephonyManager.setTelephonyProperty(phoneId, "gsm.sim.oppo.anr.support", "true");
                        } else {
                            TelephonyManager.setTelephonyProperty(phoneId, "gsm.sim.oppo.anr.support", "false");
                        }
                        if (MtkIccPhoneBookInterfaceManager.this.getEmailCount() > 0) {
                            TelephonyManager.setTelephonyProperty(phoneId, "gsm.sim.oppo.email.support", "true");
                        } else {
                            TelephonyManager.setTelephonyProperty(phoneId, "gsm.sim.oppo.email.support", "false");
                        }
                    }
                } else {
                    List unused2 = MtkIccPhoneBookInterfaceManager.this.mRecords = null;
                    MtkIccPhoneBookInterfaceManager mtkIccPhoneBookInterfaceManager = MtkIccPhoneBookInterfaceManager.this;
                    mtkIccPhoneBookInterfaceManager.loge("EVENT_LOAD_DONE: Cannot load ADN records; ex=" + ar.exception);
                }
                notifyPending(request, records);
            } else if (i == 3) {
                MtkIccPhoneBookInterfaceManager.this.logd("EVENT_UPDATE_DONE");
                AsyncResult ar2 = (AsyncResult) msg.obj;
                boolean success = ar2.exception == null;
                MtkIccPhoneBookInterfaceManager mtkIccPhoneBookInterfaceManager2 = MtkIccPhoneBookInterfaceManager.this;
                mtkIccPhoneBookInterfaceManager2.logd("EVENT_UPDATE_DONE success:" + success);
                if (success) {
                    int unused3 = MtkIccPhoneBookInterfaceManager.this.mErrorCause = 1;
                } else if (ar2.exception instanceof CommandException) {
                    MtkIccPhoneBookInterfaceManager mtkIccPhoneBookInterfaceManager3 = MtkIccPhoneBookInterfaceManager.this;
                    int unused4 = mtkIccPhoneBookInterfaceManager3.mErrorCause = mtkIccPhoneBookInterfaceManager3.getErrorCauseFromException(ar2.exception);
                } else {
                    MtkIccPhoneBookInterfaceManager.this.loge("Error : Unknow exception instance");
                    int unused5 = MtkIccPhoneBookInterfaceManager.this.mErrorCause = -10;
                }
                MtkIccPhoneBookInterfaceManager mtkIccPhoneBookInterfaceManager4 = MtkIccPhoneBookInterfaceManager.this;
                mtkIccPhoneBookInterfaceManager4.logi("update done result: " + MtkIccPhoneBookInterfaceManager.this.mErrorCause);
                notifyPending(request, Boolean.valueOf(success));
            }
        }

        private void notifyPending(IccPhoneBookInterfaceManager.Request request, Object result) {
            if (request != null) {
                synchronized (request) {
                    request.mResult = result;
                    request.mStatus.set(true);
                    request.notifyAll();
                }
            }
        }
    }

    public void updateIccRecords(IccRecords iccRecords) {
        this.mIccRecords = iccRecords;
        if (iccRecords != null) {
            this.mAdnCache = iccRecords.getAdnCache();
            if (this.mAdnCache == null || !(this.mAdnCache instanceof MtkAdnRecordCache)) {
                this.mSlotId = -1;
            } else {
                this.mSlotId = this.mAdnCache.getSlotId();
            }
            if (!this.phonebookReady) {
                onPhbReady();
            }
            logi("[updateIccRecords] Set mAdnCache value");
            return;
        }
        this.mAdnCache = null;
        this.phonebookReady = false;
        logi("[updateIccRecords] Set mAdnCache value to null");
        this.mSlotId = -1;
    }

    /* access modifiers changed from: protected */
    public void logd(String msg) {
        Rlog.d(LOG_TAG, "[IccPbInterfaceManager] " + msg + "(slot " + this.mSlotId + ")");
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        Rlog.e(LOG_TAG, "[IccPbInterfaceManager] " + msg + "(slot " + this.mSlotId + ")");
    }

    /* access modifiers changed from: protected */
    public void logi(String msg) {
        Rlog.i(LOG_TAG, "[IccPbInterfaceManager] " + msg + "(slot " + this.mSlotId + ")");
    }

    public boolean updateAdnRecordsInEfBySearch(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) {
        return updateAdnRecordsInEfBySearchWithError(efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber, pin2) == 1;
    }

    public synchronized int updateAdnRecordsInEfBySearchWithError(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) {
        int index = -1;
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            loge("updateAdnRecordsInEfBySearchWithError mAdnCache is null");
            return 0;
        } else if (!this.phonebookReady) {
            logd("IccPhoneBookInterfaceManager: updateAdnRecordsInEfBySearchWithError: phonebook not ready.");
            return 0;
        } else {
            if (DBG) {
                logd("updateAdnRecordsInEfBySearch: efid=0x" + Integer.toHexString(efid).toUpperCase());
            }
            int efid2 = updateEfForIccType(efid);
            if (this.mAdnCache == null || !this.mAdnCache.hasCmdInProgress(efid2)) {
                checkThread();
                IccPhoneBookInterfaceManager.Request updateRequest = new IccPhoneBookInterfaceManager.Request();
                synchronized (updateRequest) {
                    Message response = this.mMtkBaseHandler.obtainMessage(3, updateRequest);
                    MtkAdnRecord oldAdn = new MtkAdnRecord(oldTag, oldPhoneNumber);
                    if (newPhoneNumber == null) {
                        newPhoneNumber = "";
                    }
                    if (newTag == null) {
                        newTag = "";
                    }
                    MtkAdnRecord newAdn = new MtkAdnRecord(newTag, newPhoneNumber);
                    if (this.mAdnCache != null) {
                        index = this.mAdnCache.updateAdnBySearch(efid2, oldAdn, newAdn, pin2, response, null);
                        waitForResult(updateRequest);
                    } else {
                        loge("Failure while trying to update by search due to uninitialised adncache");
                    }
                }
                if (this.mErrorCause == 1) {
                    logi("updateAdnRecordsInEfBySearchWithError success index is " + index);
                    return index;
                }
                return this.mErrorCause;
            }
            logd("IccPhoneBookInterfaceManager: updateAdnRecordsInEfBySearchWithError: hasCmdInProgress");
            return -10;
        }
    }

    public synchronized int updateUsimPBRecordsInEfBySearchWithError(int efid, String oldTag, String oldPhoneNumber, String oldAnr, String oldGrpIds, String[] oldEmails, String newTag, String newPhoneNumber, String newAnr, String newGrpIds, String[] newEmails) {
        IccPhoneBookInterfaceManager.Request updateRequest;
        String newPhoneNumber2;
        String newTag2;
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            loge("updateUsimPBRecordsInEfBySearchWithError mAdnCache is null");
            return 0;
        } else {
            if (DBG) {
                logd("updateUsimPBRecordsInEfBySearchWithError: efid=" + efid + "oldAnr" + oldAnr + " oldGrpIds " + oldGrpIds + "==> newAnr= " + newAnr + " newGrpIds = " + newGrpIds);
            }
            if (this.mAdnCache != null && this.mAdnCache.hasCmdInProgress(efid)) {
                return -10;
            }
            checkThread();
            IccPhoneBookInterfaceManager.Request updateRequest2 = new IccPhoneBookInterfaceManager.Request();
            synchronized (updateRequest2) {
                try {
                    Message response = this.mMtkBaseHandler.obtainMessage(3, updateRequest2);
                    MtkAdnRecord oldAdn = new MtkAdnRecord(oldTag, oldPhoneNumber);
                    if (newPhoneNumber == null) {
                        newPhoneNumber2 = "";
                    } else {
                        newPhoneNumber2 = newPhoneNumber;
                    }
                    if (newTag == null) {
                        newTag2 = "";
                    } else {
                        newTag2 = newTag;
                    }
                    try {
                        updateRequest = updateRequest2;
                        try {
                            int index = this.mAdnCache.updateAdnBySearch(efid, oldAdn, new MtkAdnRecord(0, 0, newTag2, newPhoneNumber2, newAnr, newEmails, newGrpIds), null, response, null);
                            waitForResult(updateRequest);
                            if (this.mErrorCause == 1) {
                                logi("updateUsimPBRecordsInEfBySearchWithError success index is " + index);
                                return index;
                            }
                            return this.mErrorCause;
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        updateRequest = updateRequest2;
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    updateRequest = updateRequest2;
                    throw th;
                }
            }
        }
    }

    public synchronized int updateUsimPBRecordsBySearchWithError(int efid, MtkAdnRecord oldAdn, MtkAdnRecord newAdn) {
        int index;
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            loge("updateUsimPBRecordsBySearchWithError mAdnCache is null");
            return 0;
        } else {
            if (DBG) {
                logd("updateUsimPBRecordsBySearchWithError: efid=" + efid + " (" + oldAdn + ")==>(" + newAdn + ")");
            }
            checkThread();
            IccPhoneBookInterfaceManager.Request updateRequest = new IccPhoneBookInterfaceManager.Request();
            synchronized (updateRequest) {
                Message response = this.mMtkBaseHandler.obtainMessage(3, updateRequest);
                if (newAdn.getNumber() == null) {
                    newAdn.setNumber("");
                }
                index = this.mAdnCache.updateAdnBySearch(efid, oldAdn, newAdn, null, response, null);
                waitForResult(updateRequest);
            }
            if (this.mErrorCause == 1) {
                logi("updateUsimPBRecordsBySearchWithError success index is " + index);
                return index;
            }
            return this.mErrorCause;
        }
    }

    public boolean updateAdnRecordsInEfByIndex(int efid, String newTag, String newPhoneNumber, int index, String pin2) {
        return updateAdnRecordsInEfByIndexWithError(efid, newTag, newPhoneNumber, index, pin2) == 1;
    }

    public synchronized int updateAdnRecordsInEfByIndexWithError(int efid, String newTag, String newPhoneNumber, int index, String pin2) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            loge("updateAdnRecordsInEfByIndex mAdnCache is null");
            return 0;
        } else if (!this.phonebookReady) {
            logd("MtkIccPhoneBookInterfaceManager: updateAdnRecordsInEfByIndex: phonebook not ready.");
            return 0;
        } else {
            if (DBG) {
                logd("updateAdnRecordsInEfByIndex: efid=0x" + Integer.toHexString(efid).toUpperCase() + " Index=" + index + " ==> (" + newTag + "," + newPhoneNumber + ") pin2=" + pin2);
            }
            if (this.mAdnCache != null && this.mAdnCache.hasCmdInProgress(efid)) {
                return -10;
            }
            checkThread();
            IccPhoneBookInterfaceManager.Request updateRequest = new IccPhoneBookInterfaceManager.Request();
            synchronized (updateRequest) {
                Message response = this.mMtkBaseHandler.obtainMessage(3, updateRequest);
                if (newPhoneNumber == null) {
                    newPhoneNumber = "";
                }
                if (newTag == null) {
                    newTag = "";
                }
                MtkAdnRecord newAdn = new MtkAdnRecord(newTag, newPhoneNumber);
                if (this.mAdnCache != null) {
                    this.mAdnCache.updateAdnByIndex(efid, newAdn, index, pin2, response);
                    waitForResult(updateRequest);
                } else {
                    loge("Failure while trying to update by index due to uninitialised adncache");
                }
            }
            return this.mErrorCause;
        }
    }

    public synchronized int updateUsimPBRecordsInEfByIndexWithError(int efid, String newTag, String newPhoneNumber, String newAnr, String newGrpIds, String[] newEmails, int index) {
        IccPhoneBookInterfaceManager.Request updateRequest;
        String newPhoneNumber2;
        String newTag2;
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            loge("updateUsimPBRecordsInEfByIndexWithError mAdnCache is null");
            return 0;
        } else {
            if (DBG) {
                logd("updateUsimPBRecordsInEfByIndexWithError: efid=" + efid + " Index=" + index + " ==>  newAnr= " + newAnr + " newGrpIds = " + newGrpIds);
            }
            if (this.mAdnCache != null && this.mAdnCache.hasCmdInProgress(efid)) {
                return -10;
            }
            checkThread();
            IccPhoneBookInterfaceManager.Request updateRequest2 = new IccPhoneBookInterfaceManager.Request();
            synchronized (updateRequest2) {
                try {
                    Message response = this.mMtkBaseHandler.obtainMessage(3, updateRequest2);
                    if (newPhoneNumber == null) {
                        newPhoneNumber2 = "";
                    } else {
                        newPhoneNumber2 = newPhoneNumber;
                    }
                    if (newTag == null) {
                        newTag2 = "";
                    } else {
                        newTag2 = newTag;
                    }
                    try {
                        updateRequest = updateRequest2;
                        try {
                            this.mAdnCache.updateAdnByIndex(efid, new MtkAdnRecord(efid, index, newTag2, newPhoneNumber2, newAnr, newEmails, newGrpIds), index, null, response);
                            waitForResult(updateRequest);
                            return this.mErrorCause;
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        updateRequest = updateRequest2;
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    updateRequest = updateRequest2;
                    throw th;
                }
            }
        }
    }

    public synchronized int updateUsimPBRecordsByIndexWithError(int efid, MtkAdnRecord record, int index) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            loge("updateUsimPBRecordsByIndexWithError mAdnCache is null");
            return 0;
        } else {
            if (DBG) {
                logd("updateUsimPBRecordsByIndexWithError: efid=" + efid + " Index=" + index + " ==> " + record);
            }
            checkThread();
            IccPhoneBookInterfaceManager.Request updateRequest = new IccPhoneBookInterfaceManager.Request();
            synchronized (updateRequest) {
                this.mAdnCache.updateAdnByIndex(efid, record, index, null, this.mMtkBaseHandler.obtainMessage(3, updateRequest));
                waitForResult(updateRequest);
            }
            return this.mErrorCause;
        }
    }

    private String getAdnEFPath(int efid) {
        if (efid == 28474) {
            return "3F007F10";
        }
        return null;
    }

    public int[] getAdnRecordsSize(int efid) {
        if (DBG) {
            logd("getAdnRecordsSize: efid=" + efid);
        }
        checkThread();
        IccPhoneBookInterfaceManager.Request getSizeRequest = new IccPhoneBookInterfaceManager.Request();
        synchronized (getSizeRequest) {
            Message response = this.mMtkBaseHandler.obtainMessage(1, getSizeRequest);
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh != null) {
                if (getAdnEFPath(efid) != null) {
                    fh.getEFLinearRecordSize(efid, getAdnEFPath(efid), response);
                } else {
                    fh.getEFLinearRecordSize(efid, response);
                }
                waitForResult(getSizeRequest);
            }
        }
        return getSizeRequest.mResult == null ? new int[3] : (int[]) getSizeRequest.mResult;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x007c, code lost:
        return (java.util.List) r0.mResult;
     */
    public synchronized List<MtkAdnRecord> getAdnRecordsInEf(int efid, Object object) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") == 0) {
            int efid2 = updateEfForIccType(efid);
            if (DBG) {
                logd("getAdnRecordsInEF: efid=0x" + Integer.toHexString(efid2).toUpperCase());
            }
            if (this.mAdnCache == null) {
                loge("getAdnRecordsInEF mAdnCache is null");
                return null;
            } else if (this.mAdnCache.hasCmdInProgress(efid2)) {
                return null;
            } else {
                checkThread();
                IccPhoneBookInterfaceManager.Request loadRequest = new IccPhoneBookInterfaceManager.Request();
                synchronized (loadRequest) {
                    try {
                        Message response = this.mMtkBaseHandler.obtainMessage(2, loadRequest);
                        if (this.mAdnCache != null) {
                            try {
                                this.mAdnCache.requestLoadAllAdnLike(efid2, this.mAdnCache.extensionEfForEf(efid2), response);
                                waitForResult(loadRequest);
                            } catch (Throwable th) {
                                th = th;
                            }
                        } else {
                            loge("Failure while trying to load from SIM due to uninitialised adncache");
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
            }
        } else {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        }
    }

    public void checkThread() {
        if (this.mMtkBaseHandler.getLooper().equals(Looper.myLooper())) {
            loge("query() called on the main UI thread!");
            throw new IllegalStateException("You cannot call query on this provder from the main UI thread.");
        }
    }

    /* access modifiers changed from: private */
    public int getErrorCauseFromException(CommandException e) {
        if (e == null) {
            return 1;
        }
        switch (AnonymousClass1.$SwitchMap$com$android$internal$telephony$CommandException$Error[e.getCommandError().ordinal()]) {
            case 1:
                return -10;
            case 2:
                return -1;
            case 3:
            case 4:
                return -5;
            case 5:
                return -2;
            case 6:
                return -3;
            case 7:
                return -4;
            case 8:
                return -6;
            case 9:
                return -14;
            case 10:
            case 11:
                return -11;
            case 12:
                return -12;
            case 13:
                return -13;
            case 14:
                return -16;
            case 15:
                return -17;
            default:
                return 0;
        }
    }

    /* renamed from: com.mediatek.internal.telephony.phb.MtkIccPhoneBookInterfaceManager$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$CommandException$Error = new int[CommandException.Error.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.GENERIC_FAILURE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.OEM_ERROR_1.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.SIM_PUK2.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.PASSWORD_INCORRECT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.OEM_ERROR_2.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.OEM_ERROR_3.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.OEM_ERROR_4.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.OEM_ERROR_5.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.OEM_ERROR_6.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.RADIO_NOT_AVAILABLE.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.OEM_ERROR_7.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.OEM_ERROR_8.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.OEM_ERROR_9.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.OEM_ERROR_10.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.OEM_ERROR_11.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
        }
    }

    public void onPhbReady() {
        if (this.mAdnCache != null) {
            this.mAdnCache.requestLoadAllAdnLike(28474, this.mAdnCache.extensionEfForEf(28474), (Message) null);
            this.phonebookReady = isPhbReady();
        }
    }

    public boolean isPhbReady() {
        MtkSIMRecords mtkSIMRecords;
        if (this.mAdnCache == null || !SubscriptionManager.isValidSlotIndex(this.mSlotId) || (mtkSIMRecords = this.mIccRecords) == null) {
            return false;
        }
        if (mtkSIMRecords instanceof MtkSIMRecords) {
            return mtkSIMRecords.isPhbReady();
        }
        if (mtkSIMRecords instanceof MtkRuimRecords) {
            return ((MtkRuimRecords) mtkSIMRecords).isPhbReady();
        }
        return false;
    }

    public List<UsimGroup> getUsimGroups() {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return null;
        } else {
            return this.mAdnCache.getUsimGroups();
        }
    }

    public String getUsimGroupById(int nGasId) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return null;
        } else {
            return this.mAdnCache.getUsimGroupById(nGasId);
        }
    }

    public boolean removeUsimGroupById(int nGasId) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return false;
        } else {
            return this.mAdnCache.removeUsimGroupById(nGasId);
        }
    }

    public int insertUsimGroup(String grpName) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return -1;
        } else {
            return this.mAdnCache.insertUsimGroup(grpName);
        }
    }

    public int updateUsimGroup(int nGasId, String grpName) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return -1;
        } else {
            return this.mAdnCache.updateUsimGroup(nGasId, grpName);
        }
    }

    public boolean addContactToGroup(int adnIndex, int grpIndex) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return false;
        } else {
            return this.mAdnCache.addContactToGroup(adnIndex, grpIndex);
        }
    }

    public boolean removeContactFromGroup(int adnIndex, int grpIndex) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return false;
        } else {
            return this.mAdnCache.removeContactFromGroup(adnIndex, grpIndex);
        }
    }

    public boolean updateContactToGroups(int adnIndex, int[] grpIdList) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return false;
        } else {
            return this.mAdnCache.updateContactToGroups(adnIndex, grpIdList);
        }
    }

    public boolean moveContactFromGroupsToGroups(int adnIndex, int[] fromGrpIdList, int[] toGrpIdList) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return false;
        } else {
            return this.mAdnCache.moveContactFromGroupsToGroups(adnIndex, fromGrpIdList, toGrpIdList);
        }
    }

    public int hasExistGroup(String grpName) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return -1;
        } else {
            return this.mAdnCache.hasExistGroup(grpName);
        }
    }

    public int getUsimGrpMaxNameLen() {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return -1;
        } else {
            return this.mAdnCache.getUsimGrpMaxNameLen();
        }
    }

    public int getUsimGrpMaxCount() {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return -1;
        } else {
            return this.mAdnCache.getUsimGrpMaxCount();
        }
    }

    public List<AlphaTag> getUsimAasList() {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return null;
        } else {
            return this.mAdnCache.getUsimAasList();
        }
    }

    public String getUsimAasById(int index) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return null;
        } else {
            return this.mAdnCache.getUsimAasById(index);
        }
    }

    public boolean removeUsimAasById(int index, int pbrIndex) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return false;
        } else {
            return this.mAdnCache.removeUsimAasById(index, pbrIndex);
        }
    }

    public int insertUsimAas(String aasName) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return -1;
        } else {
            return this.mAdnCache.insertUsimAas(aasName);
        }
    }

    public boolean updateUsimAas(int index, int pbrIndex, String aasName) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return false;
        } else {
            return this.mAdnCache.updateUsimAas(index, pbrIndex, aasName);
        }
    }

    public boolean updateAdnAas(int adnIndex, int aasIndex) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return false;
        } else {
            return this.mAdnCache.updateAdnAas(adnIndex, aasIndex);
        }
    }

    public int getAnrCount() {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return 0;
        } else {
            return this.mAdnCache.getAnrCount();
        }
    }

    public int getEmailCount() {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return 0;
        } else {
            return this.mAdnCache.getEmailCount();
        }
    }

    public int getUsimAasMaxCount() {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return -1;
        } else {
            return this.mAdnCache.getUsimAasMaxCount();
        }
    }

    public int getUsimAasMaxNameLen() {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return -1;
        } else {
            return this.mAdnCache.getUsimAasMaxNameLen();
        }
    }

    public boolean hasSne() {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return false;
        } else {
            return this.mAdnCache.hasSne();
        }
    }

    public int getSneRecordLen() {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return -1;
        } else {
            return this.mAdnCache.getSneRecordLen();
        }
    }

    public boolean isAdnAccessible() {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return false;
        } else {
            return this.mAdnCache.isAdnAccessible();
        }
    }

    public synchronized UsimPBMemInfo[] getPhonebookMemStorageExt() {
        UsimPBMemInfo[] usimPBMemInfoArr;
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            usimPBMemInfoArr = null;
        } else {
            usimPBMemInfoArr = this.mAdnCache.getPhonebookMemStorageExt();
        }
        return usimPBMemInfoArr;
    }

    public int getUpbDone() {
        if (this.mAdnCache == null) {
            return -1;
        }
        return this.mAdnCache.getUpbDone();
    }

    public int[] getAdnRecordsCapacity() {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            return null;
        } else {
            return this.mAdnCache.getAdnRecordsCapacity();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0079, code lost:
        if (((java.lang.Boolean) r0.mResult).booleanValue() != false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007b, code lost:
        return -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        return r3;
     */
    public int oppoAddAdnRecordsInEfBySearchEx(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber1, String newPhoneNumber2, String pin2, String email) {
        MtkAdnRecord oldAdn;
        int index = -1;
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (!this.phonebookReady) {
            return -1;
        } else {
            if (this.mAdnCache == null) {
                return -1;
            }
            checkThread();
            IccPhoneBookInterfaceManager.Request updateRequest = new IccPhoneBookInterfaceManager.Request();
            synchronized (updateRequest) {
                try {
                    Message response = this.mMtkBaseHandler.obtainMessage(3, updateRequest);
                    try {
                        oldAdn = new MtkAdnRecord(oldTag, oldPhoneNumber);
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                    try {
                        MtkAdnRecord newAdn = new MtkAdnRecord(newTag, newPhoneNumber1);
                        if (this.mAdnCache != null) {
                            if (newPhoneNumber2 != null) {
                                newAdn.setNumber2(newPhoneNumber2);
                            }
                            if (email != null) {
                                newAdn.setEmails(new String[]{email.toString()});
                            }
                            index = this.mAdnCache.oppoUpdateAdnBySearch(efid, oldAdn, newAdn, pin2, response);
                            waitForResult(updateRequest);
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
    }

    public boolean oppoUpdateAdnRecordsInEfByIndexEx(int efid, String newTag, String newPhoneNumber1, String newPhoneNumber2, int index, String pin2, String email) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") == 0) {
            if (this.phonebookReady) {
                if (this.mAdnCache != null) {
                    checkThread();
                    IccPhoneBookInterfaceManager.Request updateRequest = new IccPhoneBookInterfaceManager.Request();
                    synchronized (updateRequest) {
                        try {
                            Message response = this.mMtkBaseHandler.obtainMessage(3, updateRequest);
                            try {
                                MtkAdnRecord newAdn = new MtkAdnRecord(efid, index, newTag, newPhoneNumber1);
                                if (newPhoneNumber2 != null) {
                                    newAdn.setNumber2(newPhoneNumber2);
                                }
                                if (email != null) {
                                    newAdn.setEmails(new String[]{email.toString()});
                                }
                                this.mAdnCache.oppoUpdateAdnByIndex(efid, this.mAdnCache.extensionEfForEf(efid), newAdn, index, pin2, response);
                                waitForResult(updateRequest);
                                return ((Boolean) updateRequest.mResult).booleanValue();
                            } catch (Throwable th) {
                                th = th;
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                }
            }
            return false;
        }
        throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
    }

    public boolean isPhoneBookPbrExist() {
        return this.mAdnCache.oppoCheckPbrIsExsit();
    }

    public int getRecordsSize() {
        List<MtkAdnRecord> list = this.mRecords;
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public boolean isEmptyRecords(int i) {
        List<MtkAdnRecord> list = this.mRecords;
        if (list != null) {
            return list.get(i).isEmpty();
        }
        return true;
    }

    public int getSlotId() {
        return this.mSlotId;
    }
}
