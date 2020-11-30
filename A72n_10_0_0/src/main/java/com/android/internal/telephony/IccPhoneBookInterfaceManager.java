package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class IccPhoneBookInterfaceManager extends AbstractIccPhoneBookInterfaceManager {
    @UnsupportedAppUsage
    protected static final boolean DBG = true;
    protected static final int EVENT_GET_SIZE_DONE = 1;
    protected static final int EVENT_LOAD_DONE = 2;
    protected static final int EVENT_UPDATE_DONE = 3;
    static final String LOG_TAG = "IccPhoneBookIM";
    @UnsupportedAppUsage
    protected AdnRecordCache mAdnCache;
    @UnsupportedAppUsage
    protected Handler mBaseHandler = new Handler() {
        /* class com.android.internal.telephony.IccPhoneBookInterfaceManager.AnonymousClass1 */

        public void handleMessage(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            Request request = (Request) ar.userObj;
            int i = msg.what;
            boolean success = false;
            if (i == 1) {
                int[] recordSize = null;
                if (ar.exception == null) {
                    recordSize = (int[]) ar.result;
                    IccPhoneBookInterfaceManager.this.logd("GET_RECORD_SIZE Size " + recordSize[0] + " total " + recordSize[1] + " #record " + recordSize[2]);
                } else {
                    IccPhoneBookInterfaceManager.this.loge("EVENT_GET_SIZE_DONE: failed; ex=" + ar.exception);
                }
                notifyPending(request, recordSize);
            } else if (i == 2) {
                List<AdnRecord> records = null;
                if (ar.exception == null) {
                    records = (List) ar.result;
                } else {
                    IccPhoneBookInterfaceManager.this.loge("EVENT_LOAD_DONE: Cannot load ADN records; ex=" + ar.exception);
                }
                notifyPending(request, records);
            } else if (i == 3) {
                if (ar.exception == null) {
                    success = true;
                }
                if (!success) {
                    IccPhoneBookInterfaceManager.this.loge("EVENT_UPDATE_DONE - failed; ex=" + ar.exception);
                }
                notifyPending(request, Boolean.valueOf(success));
            }
        }

        private void notifyPending(Request request, Object result) {
            if (request != null) {
                synchronized (request) {
                    request.mResult = result;
                    request.mStatus.set(true);
                    request.notifyAll();
                }
            }
        }
    };
    @UnsupportedAppUsage
    protected Phone mPhone;

    public static final class Request {
        public Object mResult = null;
        public AtomicBoolean mStatus = new AtomicBoolean(false);
    }

    public IccPhoneBookInterfaceManager(Phone phone) {
        this.mPhone = phone;
        IccRecords r = phone.getIccRecords();
        if (r != null) {
            this.mAdnCache = r.getAdnCache();
        }
        this.mReference = (IOppoIccPhoneBookInterfaceManager) OppoTelephonyFactory.getInstance().getFeature(IOppoIccPhoneBookInterfaceManager.DEFAULT, this);
    }

    public void dispose() {
    }

    public void updateIccRecords(IccRecords iccRecords) {
        if (iccRecords != null) {
            this.mAdnCache = iccRecords.getAdnCache();
        } else {
            this.mAdnCache = null;
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void logd(String msg) {
        Rlog.d(LOG_TAG, "[IccPbInterfaceManager] " + msg);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void loge(String msg) {
        Rlog.e(LOG_TAG, "[IccPbInterfaceManager] " + msg);
    }

    public boolean updateAdnRecordsInEfBySearch(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") == 0) {
            logd("updateAdnRecordsInEfBySearch: efid=0x" + Integer.toHexString(efid).toUpperCase() + " (" + Rlog.pii(LOG_TAG, oldTag) + "," + Rlog.pii(LOG_TAG, oldPhoneNumber) + ")==> (" + Rlog.pii(LOG_TAG, newTag) + "," + Rlog.pii(LOG_TAG, newPhoneNumber) + ") pin2=" + Rlog.pii(LOG_TAG, pin2));
            int efid2 = updateEfForIccType(efid);
            checkThread();
            Request updateRequest = new Request();
            synchronized (updateRequest) {
                Message response = this.mBaseHandler.obtainMessage(3, updateRequest);
                AdnRecord oldAdn = new AdnRecord(oldTag, oldPhoneNumber);
                AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber);
                if (this.mAdnCache != null) {
                    this.mAdnCache.updateAdnBySearch(efid2, oldAdn, newAdn, pin2, response);
                    waitForResult(updateRequest);
                } else {
                    loge("Failure while trying to update by search due to uninitialised adncache");
                }
            }
            return ((Boolean) updateRequest.mResult).booleanValue();
        }
        throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
    }

    public boolean updateAdnRecordsInEfByIndex(int efid, String newTag, String newPhoneNumber, int index, String pin2) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") == 0) {
            logd("updateAdnRecordsInEfByIndex: efid=0x" + Integer.toHexString(efid).toUpperCase() + " Index=" + index + " ==> (" + Rlog.pii(LOG_TAG, newTag) + "," + Rlog.pii(LOG_TAG, newPhoneNumber) + ") pin2=" + Rlog.pii(LOG_TAG, pin2));
            checkThread();
            Request updateRequest = new Request();
            synchronized (updateRequest) {
                Message response = this.mBaseHandler.obtainMessage(3, updateRequest);
                AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber);
                if (this.mAdnCache != null) {
                    this.mAdnCache.updateAdnByIndex(efid, newAdn, index, pin2, response);
                    waitForResult(updateRequest);
                } else {
                    loge("Failure while trying to update by index due to uninitialised adncache");
                }
            }
            return ((Boolean) updateRequest.mResult).booleanValue();
        }
        throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
    }

    public int[] getAdnRecordsSize(int efid) {
        logd("getAdnRecordsSize: efid=" + efid);
        checkThread();
        Request getSizeRequest = new Request();
        synchronized (getSizeRequest) {
            Message response = this.mBaseHandler.obtainMessage(1, getSizeRequest);
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh != null) {
                fh.getEFLinearRecordSize(efid, response);
                waitForResult(getSizeRequest);
            }
        }
        return getSizeRequest.mResult == null ? new int[3] : (int[]) getSizeRequest.mResult;
    }

    public List<AdnRecord> getAdnRecordsInEf(int efid) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") == 0) {
            int efid2 = updateEfForIccType(efid);
            logd("getAdnRecordsInEF: efid=0x" + Integer.toHexString(efid2).toUpperCase());
            checkThread();
            Request loadRequest = new Request();
            synchronized (loadRequest) {
                Message response = this.mBaseHandler.obtainMessage(2, loadRequest);
                if (this.mAdnCache != null) {
                    this.mAdnCache.requestLoadAllAdnLike(efid2, this.mAdnCache.extensionEfForEf(efid2), response);
                    waitForResult(loadRequest);
                } else {
                    loge("Failure while trying to load from SIM due to uninitialised adncache");
                }
            }
            return (List) loadRequest.mResult;
        }
        throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
    }

    @UnsupportedAppUsage
    public void checkThread() {
        if (this.mBaseHandler.getLooper().equals(Looper.myLooper())) {
            loge("query() called on the main UI thread!");
            throw new IllegalStateException("You cannot call query on this provder from the main UI thread.");
        }
    }

    /* access modifiers changed from: protected */
    public void waitForResult(Request request) {
        synchronized (request) {
            while (!request.mStatus.get()) {
                try {
                    request.wait();
                } catch (InterruptedException e) {
                    logd("interrupted while trying to update by search");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public int updateEfForIccType(int efid) {
        if (efid == 28474 && this.mPhone.getCurrentUiccAppType() == IccCardApplicationStatus.AppType.APPTYPE_USIM) {
            return IccConstants.EF_PBR;
        }
        return efid;
    }

    @Override // com.android.internal.telephony.AbstractIccPhoneBookInterfaceManager
    public void oppoCheckThread() {
        checkThread();
    }

    @Override // com.android.internal.telephony.AbstractIccPhoneBookInterfaceManager
    public Phone getPhone() {
        return this.mPhone;
    }
}
