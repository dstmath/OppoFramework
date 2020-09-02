package com.mediatek.internal.telephony.phb;

import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.util.SparseArray;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.uicc.MtkUiccCardApplication;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MtkAdnRecordCache extends AdnRecordCache {
    private static final int ADN_FILE_SIZE = 250;
    private static final boolean DBG;
    private static final String LOG_TAG = "MtkAdnRecordCache";
    public static final int MAX_PHB_NAME_LENGTH = 60;
    public static final int MAX_PHB_NUMBER_ANR_COUNT = 1;
    public static final int MAX_PHB_NUMBER_ANR_LENGTH = 20;
    public static final int MAX_PHB_NUMBER_LENGTH = 40;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private CommandsInterface mCi;
    private UiccCardApplication mCurrentApp;
    private final Object mLock = new Object();
    protected SparseArray<ArrayList<MtkAdnRecord>> mMtkAdnLikeFiles = new SparseArray<>();
    private boolean mNeedToWait = false;
    private int mSlotId = -1;
    private boolean mSuccess = false;
    private MtkUsimPhoneBookManager mUsimPhoneBookManager;

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1 && !SystemProperties.get("ro.build.type").equals(DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER)) {
            z = true;
        }
        DBG = z;
    }

    public MtkAdnRecordCache(IccFileHandler fh, CommandsInterface ci, UiccCardApplication app) {
        super(fh);
        this.mCi = ci;
        this.mCurrentApp = app;
        this.mUsimPhoneBookManager = new MtkUsimPhoneBookManager(this.mFh, this, ci, app);
        if (app != null) {
            this.mSlotId = ((MtkUiccCardApplication) app).getPhoneId();
        }
    }

    public void reset() {
        logi("reset");
        this.mMtkAdnLikeFiles.clear();
        this.mUsimPhoneBookManager.reset();
        synchronized (this.mAdnLikeWaiters) {
            clearWaiters();
        }
        clearUserWriters();
        if (!CsimPhbUtil.hasModemPhbEnhanceCapability(this.mFh)) {
            CsimPhbUtil.clearAdnRecordSize();
        }
    }

    public int getSlotId() {
        return this.mSlotId;
    }

    private void clearUserWriters() {
        logi("clearUserWriters");
        synchronized (this.mLock) {
            logi("mNeedToWait " + this.mNeedToWait);
            if (this.mNeedToWait) {
                this.mNeedToWait = false;
                this.mLock.notifyAll();
            }
        }
        int size = this.mUserWriteResponse.size();
        for (int i = 0; i < size; i++) {
            sendErrorResponse((Message) this.mUserWriteResponse.valueAt(i), "AdnCace reset " + this.mUserWriteResponse.valueAt(i));
        }
        this.mUserWriteResponse.clear();
    }

    private void sendErrorResponse(Message response, String errString) {
        sendErrorResponse(response, errString, 2);
    }

    private void sendErrorResponse(Message response, String errString, int ril_errno) {
        CommandException e = CommandException.fromRilErrno(ril_errno);
        if (response != null) {
            logw(errString);
            AsyncResult.forMessage(response).exception = e;
            response.sendToTarget();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:166:0x02db, code lost:
        return;
     */
    public synchronized void updateAdnByIndex(int efid, MtkAdnRecord adn, int recordIndex, String pin2, Message response) {
        int extensionEF;
        MtkAdnRecord foundAdn;
        int efid2;
        Object obj;
        Throwable th;
        logd("updateAdnByIndex efid:" + efid + ", recordIndex:" + recordIndex + ", adn [" + adn + "]");
        int extensionEF2 = extensionEfForEf(efid);
        String anr = null;
        if (extensionEF2 < 0) {
            sendErrorResponse(response, "EF is not known ADN-like EF:0x" + Integer.toHexString(efid).toUpperCase());
            return;
        }
        if (adn.mAlphaTag == null) {
            adn.mAlphaTag = "";
        }
        if (adn.mAlphaTag.length() > 60) {
            sendErrorResponse(response, "the input length of mAlphaTag is too long: " + adn.mAlphaTag, 502);
            return;
        }
        if (adn.mNumber == null) {
            adn.mNumber = "";
        }
        int numLength = adn.mNumber.length();
        if (adn.mNumber.indexOf(43) != -1) {
            numLength--;
        }
        if (numLength > 40) {
            sendErrorResponse(response, "the input length of phoneNumber is too long: " + adn.mNumber, 501);
            return;
        }
        for (int i = 0; i < 1; i++) {
            anr = adn.getAdditionalNumber(i);
            if (anr != null) {
                int numLength2 = anr.length();
                if (anr.indexOf(43) != -1) {
                    numLength2--;
                }
                if (numLength2 > 20) {
                    sendErrorResponse(response, "the input length of additional number is too long: " + anr, 505);
                    return;
                }
            }
        }
        if (!this.mUsimPhoneBookManager.checkEmailLength(adn.mEmails)) {
            sendErrorResponse(response, "the email string is too long", 509);
            return;
        }
        if (efid == 20272) {
            ArrayList<MtkAdnRecord> oldAdnList = this.mUsimPhoneBookManager.loadEfFilesFromUsim(null);
            if (oldAdnList == null) {
                sendErrorResponse(response, "Adn list not exist for EF:" + efid, 507);
                return;
            }
            MtkAdnRecord foundAdn2 = oldAdnList.get(recordIndex - 1);
            int efid3 = foundAdn2.mEfid;
            int extensionEF3 = foundAdn2.mExtRecord;
            adn.mEfid = efid3;
            efid2 = efid3;
            extensionEF = extensionEF3;
            foundAdn = foundAdn2;
        } else {
            efid2 = efid;
            extensionEF = extensionEF2;
            foundAdn = null;
        }
        if (!this.mUsimPhoneBookManager.checkEmailCapacityFree(recordIndex, adn.mEmails, foundAdn)) {
            sendErrorResponse(response, "drop the email for the limitation of the SIM card", 508);
            return;
        }
        for (int i2 = 0; i2 < 1; i2++) {
            String anr2 = adn.getAdditionalNumber(i2);
            if (!this.mUsimPhoneBookManager.isAnrCapacityFree(anr2, recordIndex, i2, foundAdn)) {
                sendErrorResponse(response, "drop the additional number for the update fail: " + anr2, 506);
                return;
            }
        }
        if (!this.mUsimPhoneBookManager.checkSneCapacityFree(recordIndex, adn.mSne, foundAdn)) {
            sendErrorResponse(response, "drop the sne for the limitation of the SIM card", 510);
        } else if (((Message) this.mUserWriteResponse.get(efid2)) != null) {
            sendErrorResponse(response, "Have pending update for EF:0x" + Integer.toHexString(efid2).toUpperCase());
        } else {
            this.mUserWriteResponse.put(efid2, response);
            if ((efid2 == 28474 || efid2 == 20272 || efid2 == 20282 || efid2 == 20283 || efid2 == 20284 || efid2 == 20285) && adn.mAlphaTag.length() == 0 && adn.mNumber.length() == 0) {
                this.mUsimPhoneBookManager.removeContactGroup(recordIndex);
            }
            if (this.mUserWriteResponse.size() != 0) {
                Object obj2 = this.mLock;
                synchronized (obj2) {
                    try {
                        this.mSuccess = false;
                        this.mNeedToWait = true;
                        obj = obj2;
                        try {
                            new MtkAdnRecordLoader(this.mFh).updateEF(adn, efid2, extensionEF, recordIndex, pin2, obtainMessage(2, efid2, recordIndex, adn));
                            while (this.mNeedToWait) {
                                try {
                                    this.mLock.wait();
                                } catch (InterruptedException e) {
                                } catch (Throwable th2) {
                                    e = th2;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th3) {
                                            e = th3;
                                        }
                                    }
                                    throw e;
                                }
                                try {
                                } catch (InterruptedException e2) {
                                    try {
                                        return;
                                    } catch (Throwable th4) {
                                        e = th4;
                                        while (true) {
                                            break;
                                        }
                                        throw e;
                                    }
                                }
                            }
                        } catch (Throwable th5) {
                            e = th5;
                            while (true) {
                                break;
                            }
                            throw e;
                        }
                        try {
                            if (this.mSuccess) {
                                if (efid2 == 28474 || efid2 == 20272 || efid2 == 20282 || efid2 == 20283 || efid2 == 20284) {
                                    th = null;
                                } else if (efid2 == 20285) {
                                    th = null;
                                } else if (efid2 == 28475) {
                                    AsyncResult.forMessage(response, (Object) null, (Throwable) null);
                                    response.sendToTarget();
                                }
                                try {
                                    try {
                                        int mResult = this.mUsimPhoneBookManager.updateSneByAdnIndex(adn.mSne, recordIndex, foundAdn);
                                        if (-30 == mResult) {
                                            sendErrorResponse(response, "drop the SNE for the limitation of the SIM card", 510);
                                        } else if (-40 == mResult) {
                                            sendErrorResponse(response, "the sne string is too long", 511);
                                        } else {
                                            int i3 = 0;
                                            while (i3 < 1) {
                                                try {
                                                    try {
                                                        this.mUsimPhoneBookManager.updateAnrByAdnIndex(adn.getAdditionalNumber(i3), recordIndex, i3, foundAdn);
                                                        i3++;
                                                    } catch (Exception e3) {
                                                        e = e3;
                                                        e.printStackTrace();
                                                    }
                                                } catch (Exception e4) {
                                                    e = e4;
                                                    e.printStackTrace();
                                                }
                                            }
                                            int success = this.mUsimPhoneBookManager.updateEmailsByAdnIndex(adn.mEmails, recordIndex, foundAdn);
                                            if (-30 == success) {
                                                sendErrorResponse(response, "drop the email for the limitation of the SIM card", 508);
                                            } else if (-40 == success) {
                                                sendErrorResponse(response, "the email string is too long", 509);
                                            } else if (-50 == success) {
                                                sendErrorResponse(response, "Unkown error occurs when update email", 2);
                                            } else {
                                                AsyncResult.forMessage(response, th, th);
                                                response.sendToTarget();
                                            }
                                        }
                                    } catch (Exception e5) {
                                        e = e5;
                                        e.printStackTrace();
                                    }
                                } catch (Exception e6) {
                                    e = e6;
                                    e.printStackTrace();
                                }
                            }
                        } catch (Throwable th6) {
                            e = th6;
                            while (true) {
                                break;
                            }
                            throw e;
                        }
                    } catch (Throwable th7) {
                        e = th7;
                        obj = obj2;
                        while (true) {
                            break;
                        }
                        throw e;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:159:0x0372, code lost:
        return r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x01a8, code lost:
        return r3;
     */
    public synchronized int updateAdnBySearch(int efid, MtkAdnRecord oldAdn, MtkAdnRecord newAdn, String pin2, Message response, Object object) {
        ArrayList<MtkAdnRecord> oldAdnList;
        int extensionEF;
        int index;
        int efid2;
        Object obj;
        logd("updateAdnBySearch efid:" + efid + ", oldAdn [" + oldAdn + "], new Adn[" + newAdn + "]");
        int index2 = -1;
        String anr = null;
        int extensionEF2 = extensionEfForEf(efid);
        if (extensionEF2 < 0) {
            sendErrorResponse(response, "EF is not known ADN-like EF:0x" + Integer.toHexString(efid).toUpperCase());
            return -1;
        }
        if (newAdn.mAlphaTag == null) {
            newAdn.mAlphaTag = "";
        }
        if (newAdn.mAlphaTag.length() > 60) {
            sendErrorResponse(response, "the input length of mAlphaTag is too long: " + newAdn.mAlphaTag, 502);
            return -1;
        }
        if (newAdn.mNumber == null) {
            newAdn.mNumber = "";
        }
        int numLength = newAdn.mNumber.length();
        if (newAdn.mNumber.indexOf(43) != -1) {
            numLength--;
        }
        if (numLength > 40) {
            sendErrorResponse(response, "the input length of phoneNumber is too long: " + newAdn.mNumber, 501);
            return -1;
        }
        for (int i = 0; i < 1; i++) {
            anr = newAdn.getAdditionalNumber(i);
            if (anr != null) {
                int numLength2 = anr.length();
                if (anr.indexOf(43) != -1) {
                    numLength2--;
                }
                if (numLength2 > 20) {
                    sendErrorResponse(response, "the input length of additional number is too long: " + anr, 505);
                    return -1;
                }
            }
        }
        if (!this.mUsimPhoneBookManager.checkEmailLength(newAdn.mEmails)) {
            sendErrorResponse(response, "the email string is too long", 509);
            return -1;
        }
        if (efid == 20272) {
            oldAdnList = this.mUsimPhoneBookManager.loadEfFilesFromUsim(null);
        } else {
            oldAdnList = getRecordsIfLoaded(efid, null);
        }
        if (oldAdnList == null) {
            sendErrorResponse(response, "Adn list not exist for EF:" + efid, 507);
            return -1;
        }
        int count = 1;
        Iterator<MtkAdnRecord> it = oldAdnList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            } else if (oldAdn.isEqual(it.next())) {
                index2 = count;
                break;
            } else {
                count++;
            }
        }
        logi("updateAdnBySearch index " + index2);
        if (index2 != -1) {
            MtkAdnRecord foundAdn = null;
            if (efid == 20272) {
                foundAdn = oldAdnList.get(index2 - 1);
                int efid3 = foundAdn.mEfid;
                int extensionEF3 = foundAdn.mExtRecord;
                int index3 = foundAdn.mRecordNumber;
                newAdn.mEfid = efid3;
                newAdn.mExtRecord = extensionEF3;
                newAdn.mRecordNumber = index3;
                efid2 = efid3;
                index = index3;
                extensionEF = extensionEF3;
            } else {
                efid2 = efid;
                index = index2;
                extensionEF = extensionEF2;
            }
            if (((Message) this.mUserWriteResponse.get(efid2)) != null) {
                sendErrorResponse(response, "Have pending update for EF:0x" + Integer.toHexString(efid2).toUpperCase());
                return index;
            } else if (efid2 == 0) {
                sendErrorResponse(response, "Abnormal efid: " + efid2);
                return index;
            } else if (!this.mUsimPhoneBookManager.checkEmailCapacityFree(index, newAdn.mEmails, foundAdn)) {
                sendErrorResponse(response, "drop the email for the limitation of the SIM card", 508);
                return index;
            } else {
                for (int i2 = 0; i2 < 1; i2++) {
                    String anr2 = newAdn.getAdditionalNumber(i2);
                    if (!this.mUsimPhoneBookManager.isAnrCapacityFree(anr2, index, i2, foundAdn)) {
                        sendErrorResponse(response, "drop the additional number for the write fail: " + anr2, 506);
                        return index;
                    }
                }
                if (!this.mUsimPhoneBookManager.checkSneCapacityFree(index, newAdn.mSne, foundAdn)) {
                    sendErrorResponse(response, "drop the sne for the limitation of the SIM card", 510);
                    return index;
                }
                this.mUserWriteResponse.put(efid2, response);
                Object obj2 = this.mLock;
                synchronized (obj2) {
                    try {
                        this.mSuccess = false;
                        this.mNeedToWait = true;
                        obj = obj2;
                        try {
                            new MtkAdnRecordLoader(this.mFh).updateEF(newAdn, efid2, extensionEF, index, pin2, obtainMessage(2, efid2, index, newAdn));
                            while (this.mNeedToWait) {
                                try {
                                    this.mLock.wait();
                                } catch (InterruptedException e) {
                                    return index;
                                }
                            }
                        } catch (Throwable th) {
                            e = th;
                            throw e;
                        }
                        try {
                            if (!this.mSuccess) {
                                loge("updateAdnBySearch mSuccess:" + this.mSuccess);
                                return index;
                            } else if (efid2 == 28474 || efid2 == 20272 || efid2 == 20282 || efid2 == 20283 || efid2 == 20284 || efid2 == 20285) {
                                int mResult = this.mUsimPhoneBookManager.updateSneByAdnIndex(newAdn.mSne, index, foundAdn);
                                if (-30 == mResult) {
                                    sendErrorResponse(response, "drop the SNE for the limitation of the SIM card", 510);
                                } else if (-40 == mResult) {
                                    sendErrorResponse(response, "the sne string is too long", 511);
                                } else {
                                    for (int i3 = 0; i3 < 1; i3++) {
                                        this.mUsimPhoneBookManager.updateAnrByAdnIndex(newAdn.getAdditionalNumber(i3), index, i3, foundAdn);
                                    }
                                    int success = this.mUsimPhoneBookManager.updateEmailsByAdnIndex(newAdn.mEmails, index, foundAdn);
                                    if (-30 == success) {
                                        sendErrorResponse(response, "drop the email for the limitation of the SIM card", 508);
                                    } else if (-40 == success) {
                                        sendErrorResponse(response, "the email string is too long", 509);
                                    } else if (-50 == success) {
                                        sendErrorResponse(response, "Unkown error occurs when update email", 2);
                                    } else {
                                        logd("updateAdnBySearch response:" + response);
                                        AsyncResult.forMessage(response, (Object) null, (Throwable) null);
                                        response.sendToTarget();
                                    }
                                }
                            } else if (efid2 == 28475) {
                                logd("updateAdnBySearch FDN response:" + response);
                                AsyncResult.forMessage(response, (Object) null, (Throwable) null);
                                response.sendToTarget();
                            }
                        } catch (Throwable th2) {
                            e = th2;
                            throw e;
                        }
                    } catch (Throwable th3) {
                        e = th3;
                        obj = obj2;
                        throw e;
                    }
                }
            }
        } else if (oldAdn.mAlphaTag.length() == 0 && oldAdn.mNumber.length() == 0) {
            sendErrorResponse(response, "Adn record don't exist for " + oldAdn, 503);
        } else {
            sendErrorResponse(response, "Adn record don't exist for " + oldAdn);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0090, code lost:
        if (r8 >= 0) goto L_0x00bc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0092, code lost:
        if (r9 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0094, code lost:
        r1 = android.os.AsyncResult.forMessage(r9);
        r1.exception = new java.lang.RuntimeException("EF is not known ADN-like EF:0x" + java.lang.Integer.toHexString(r7).toUpperCase());
        r9.sendToTarget();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00bc, code lost:
        new com.mediatek.internal.telephony.phb.MtkAdnRecordLoader(r6.mFh).loadAllFromEF(r7, r8, obtainMessage(1, r7, 0));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00ca, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        return;
     */
    public void requestLoadAllAdnLike(int efid, int extensionEf, Message response) {
        ArrayList<MtkAdnRecord> result;
        logd("requestLoadAllAdnLike efid = " + efid + ", extensionEf = " + extensionEf);
        if (efid == 20272) {
            result = this.mUsimPhoneBookManager.loadEfFilesFromUsim(null);
            if (result != null) {
                this.isUim = true;
            } else {
                efid = 28474;
            }
        } else {
            result = getRecordsIfLoaded(efid, null);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("requestLoadAllAdnLike efid = ");
        sb.append(efid);
        sb.append(", result = null ?");
        sb.append(result == null);
        logi(sb.toString());
        if (result != null) {
            if (response != null) {
                AsyncResult.forMessage(response).result = result;
                response.sendToTarget();
            }
        } else if (result == null && efid == 20272) {
            sendErrorResponse(response, "Error occurs when query PBR", 2);
        } else {
            synchronized (this.mAdnLikeWaiters) {
                ArrayList<Message> waiters = (ArrayList) this.mAdnLikeWaiters.get(efid);
                if (waiters != null) {
                    waiters.add(response);
                    return;
                }
                ArrayList<Message> waiters2 = new ArrayList<>();
                waiters2.add(response);
                this.mAdnLikeWaiters.put(efid, waiters2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyWaiters(ArrayList<Message> waiters, AsyncResult ar) {
        if (waiters != null) {
            int s = waiters.size();
            for (int i = 0; i < s; i++) {
                Message waiter = waiters.get(i);
                if (waiter != null) {
                    logi("NotifyWaiters: " + waiter);
                    AsyncResult.forMessage(waiter, ar.result, ar.exception);
                    waiter.sendToTarget();
                }
            }
        }
    }

    public ArrayList<MtkAdnRecord> getRecordsIfLoaded(int efid, Object object) {
        return this.mMtkAdnLikeFiles.get(efid);
    }

    public void handleMessage(Message msg) {
        ArrayList<Message> waiters;
        int i = msg.what;
        boolean z = true;
        if (i == 1) {
            AsyncResult ar = (AsyncResult) msg.obj;
            int efid = msg.arg1;
            synchronized (this.mAdnLikeWaiters) {
                waiters = (ArrayList) this.mAdnLikeWaiters.get(efid);
                this.mAdnLikeWaiters.delete(efid);
            }
            if (ar.exception == null) {
                this.mMtkAdnLikeFiles.put(efid, (ArrayList) ar.result);
            } else {
                Rlog.w(LOG_TAG, "EVENT_LOAD_ALL_ADN_LIKE_DONE exception(slot " + this.mSlotId + ")", ar.exception);
            }
            notifyWaiters(waiters, ar);
        } else if (i == 2) {
            logd("EVENT_UPDATE_ADN_DONE");
            synchronized (this.mLock) {
                if (this.mNeedToWait) {
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    int efid2 = msg.arg1;
                    int index = msg.arg2;
                    MtkAdnRecord adn = (MtkAdnRecord) ar2.userObj;
                    if (ar2.exception == null && adn != null) {
                        adn.setRecordIndex(index);
                        if (adn.mEfid <= 0) {
                            adn.mEfid = efid2;
                        }
                        logd("mMtkAdnLikeFiles changed index:" + index + ",adn:" + adn + "  efid:" + efid2);
                        if (!(this.mMtkAdnLikeFiles == null || this.mMtkAdnLikeFiles.get(efid2) == null)) {
                            if (efid2 == 20283 && !CsimPhbUtil.hasModemPhbEnhanceCapability(this.mFh)) {
                                index -= 250;
                            }
                            this.mMtkAdnLikeFiles.get(efid2).set(index - 1, adn);
                            logd(" index:" + index + "   efid:" + efid2);
                        }
                        if (!(this.mUsimPhoneBookManager == null || efid2 == 28475)) {
                            if (efid2 == 20283) {
                                index += ADN_FILE_SIZE;
                                logd(" index2:" + index);
                            }
                            this.mUsimPhoneBookManager.updateUsimPhonebookRecordsList(index - 1, adn);
                        }
                    }
                    Message response = (Message) this.mUserWriteResponse.get(efid2);
                    this.mUserWriteResponse.delete(efid2);
                    logi("MtkAdnRecordCache: " + ar2.exception);
                    if (!(ar2.exception == null || response == null)) {
                        AsyncResult.forMessage(response, (Object) null, ar2.exception);
                        response.sendToTarget();
                    }
                    if (ar2.exception != null) {
                        z = false;
                    }
                    this.mSuccess = z;
                    this.mNeedToWait = false;
                    this.mLock.notifyAll();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void logd(String msg) {
        if (DBG) {
            Rlog.d(LOG_TAG, msg + "(slot " + this.mSlotId + ")");
        }
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        Rlog.e(LOG_TAG, msg + "(slot " + this.mSlotId + ")");
    }

    /* access modifiers changed from: protected */
    public void logi(String msg) {
        Rlog.i(LOG_TAG, msg + "(slot " + this.mSlotId + ")");
    }

    /* access modifiers changed from: protected */
    public void logw(String msg) {
        Rlog.w(LOG_TAG, msg + "(slot " + this.mSlotId + ")");
    }

    public List<UsimGroup> getUsimGroups() {
        return this.mUsimPhoneBookManager.getUsimGroups();
    }

    public String getUsimGroupById(int nGasId) {
        return this.mUsimPhoneBookManager.getUsimGroupById(nGasId);
    }

    public boolean removeUsimGroupById(int nGasId) {
        return this.mUsimPhoneBookManager.removeUsimGroupById(nGasId);
    }

    public int insertUsimGroup(String grpName) {
        return this.mUsimPhoneBookManager.insertUsimGroup(grpName);
    }

    public int updateUsimGroup(int nGasId, String grpName) {
        return this.mUsimPhoneBookManager.updateUsimGroup(nGasId, grpName);
    }

    public boolean addContactToGroup(int adnIndex, int grpIndex) {
        return this.mUsimPhoneBookManager.addContactToGroup(adnIndex, grpIndex);
    }

    public boolean removeContactFromGroup(int adnIndex, int grpIndex) {
        return this.mUsimPhoneBookManager.removeContactFromGroup(adnIndex, grpIndex);
    }

    public boolean updateContactToGroups(int adnIndex, int[] grpIdList) {
        return this.mUsimPhoneBookManager.updateContactToGroups(adnIndex, grpIdList);
    }

    public boolean moveContactFromGroupsToGroups(int adnIndex, int[] fromGrpIdList, int[] toGrpIdList) {
        return this.mUsimPhoneBookManager.moveContactFromGroupsToGroups(adnIndex, fromGrpIdList, toGrpIdList);
    }

    public int hasExistGroup(String grpName) {
        return this.mUsimPhoneBookManager.hasExistGroup(grpName);
    }

    public int getUsimGrpMaxNameLen() {
        return this.mUsimPhoneBookManager.getUsimGrpMaxNameLen();
    }

    public int getUsimGrpMaxCount() {
        return this.mUsimPhoneBookManager.getUsimGrpMaxCount();
    }

    private void dumpAdnLikeFile() {
        int size = this.mMtkAdnLikeFiles.size();
        logd("dumpAdnLikeFile size " + size);
        for (int i = 0; i < size; i++) {
            int key = this.mMtkAdnLikeFiles.keyAt(i);
            ArrayList<MtkAdnRecord> records = this.mMtkAdnLikeFiles.get(key);
            logd("dumpAdnLikeFile index " + i + " key " + key + "records size " + records.size());
            for (int j = 0; j < records.size(); j++) {
                logd("mMtkAdnLikeFiles[" + j + "]=" + records.get(j));
            }
        }
    }

    public ArrayList<AlphaTag> getUsimAasList() {
        return this.mUsimPhoneBookManager.getUsimAasList();
    }

    public String getUsimAasById(int index) {
        return this.mUsimPhoneBookManager.getUsimAasById(index, 0);
    }

    public boolean removeUsimAasById(int index, int pbrIndex) {
        return this.mUsimPhoneBookManager.removeUsimAasById(index, pbrIndex);
    }

    public int insertUsimAas(String aasName) {
        return this.mUsimPhoneBookManager.insertUsimAas(aasName);
    }

    public boolean updateUsimAas(int index, int pbrIndex, String aasName) {
        return this.mUsimPhoneBookManager.updateUsimAas(index, pbrIndex, aasName);
    }

    public boolean updateAdnAas(int adnIndex, int aasIndex) {
        return this.mUsimPhoneBookManager.updateAdnAas(adnIndex, aasIndex);
    }

    public int getAnrCount() {
        return this.mUsimPhoneBookManager.getAnrCount();
    }

    public int getEmailCount() {
        return this.mUsimPhoneBookManager.getEmailCount();
    }

    public int getUsimAasMaxCount() {
        return this.mUsimPhoneBookManager.getUsimAasMaxCount();
    }

    public int getUsimAasMaxNameLen() {
        return this.mUsimPhoneBookManager.getUsimAasMaxNameLen();
    }

    public boolean hasSne() {
        return this.mUsimPhoneBookManager.hasSne();
    }

    public int getSneRecordLen() {
        return this.mUsimPhoneBookManager.getSneRecordLen();
    }

    public boolean isAdnAccessible() {
        return this.mUsimPhoneBookManager.isAdnAccessible();
    }

    public boolean isUsimPhbEfAndNeedReset(int fileId) {
        return this.mUsimPhoneBookManager.isUsimPhbEfAndNeedReset(fileId);
    }

    public UsimPBMemInfo[] getPhonebookMemStorageExt() {
        return this.mUsimPhoneBookManager.getPhonebookMemStorageExt();
    }

    public int getUpbDone() {
        return this.mUsimPhoneBookManager.getUpbDone();
    }

    public int[] getAdnRecordsCapacity() {
        return this.mUsimPhoneBookManager.getAdnRecordsCapacity();
    }

    public void getsendErrorResponse(Message response, String errString) {
        sendErrorResponse(response, errString);
    }

    public int getUpdateAdn(int efid, MtkAdnRecord oldAdn, MtkAdnRecord newAdn, int index, String pin2, Message response) {
        return this.mUsimPhoneBookManager.oppoUpdateAdn(efid, oldAdn, newAdn, index, pin2, response);
    }

    public boolean oppoCheckPbrIsExsit() {
        return this.mUsimPhoneBookManager.isPbrExsit();
    }
}
