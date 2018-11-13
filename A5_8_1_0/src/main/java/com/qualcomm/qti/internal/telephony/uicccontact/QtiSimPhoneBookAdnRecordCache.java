package com.qualcomm.qti.internal.telephony.uicccontact;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.util.SparseArray;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.qualcomm.qti.internal.telephony.QtiRilInterface;
import java.util.ArrayList;
import java.util.Iterator;

public final class QtiSimPhoneBookAdnRecordCache extends Handler {
    private static final boolean DBG = true;
    static final int EVENT_INIT_ADN_DONE = 1;
    static final int EVENT_LOAD_ADN_RECORD_DONE = 3;
    static final int EVENT_LOAD_ALL_ADN_LIKE_DONE = 4;
    static final int EVENT_QUERY_ADN_RECORD_DONE = 2;
    static final int EVENT_SIM_REFRESH = 6;
    static final int EVENT_UPDATE_ADN_RECORD_DONE = 5;
    private static final String LOG_TAG = "QtiSimPhoneBookAdnRecordCache";
    SparseArray<int[]> extRecList = new SparseArray();
    private int mAddNumCount = 0;
    private int mAdnCount = 0;
    ArrayList<Message> mAdnLoadingWaiters = new ArrayList();
    Message mAdnUpdatingWaiter = null;
    protected final CommandsInterface mCi;
    protected Context mContext;
    private int mEmailCount = 0;
    private Object mLock = new Object();
    private int mMaxAnrLen = 0;
    private int mMaxEmailLen = 0;
    private int mMaxNameLen = 0;
    private int mMaxNumberLen = 0;
    protected int mPhoneId;
    private QtiRilInterface mQtiRilInterface;
    private int mRecCount = 0;
    private boolean mRefreshAdnCache = false;
    private ArrayList<AdnRecord> mSimPbRecords;
    private int mValidAddNumCount = 0;
    private int mValidAdnCount = 0;
    private int mValidEmailCount = 0;
    private final BroadcastReceiver sReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SIM_STATE_CHANGED")) {
                int phoneId = intent.getIntExtra("phone", -1);
                String simStatus = intent.getStringExtra("ss");
                if ("ABSENT".equals(simStatus) && QtiSimPhoneBookAdnRecordCache.this.mPhoneId == phoneId) {
                    QtiSimPhoneBookAdnRecordCache.this.log("ACTION_SIM_STATE_CHANGED intent received simStatus: " + simStatus + "phoneId: " + phoneId);
                    QtiSimPhoneBookAdnRecordCache.this.invalidateAdnCache();
                }
            }
        }
    };

    public QtiSimPhoneBookAdnRecordCache(Context context, int phoneId, CommandsInterface ci) {
        this.mCi = ci;
        this.mSimPbRecords = new ArrayList();
        this.mPhoneId = phoneId;
        this.mContext = context;
        this.mQtiRilInterface = QtiRilInterface.getInstance(context);
        this.mQtiRilInterface.registerForAdnInitDone(this, 1, null);
        this.mCi.registerForIccRefresh(this, 6, null);
        context.registerReceiver(this.sReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
    }

    public void reset() {
        this.mAdnLoadingWaiters.clear();
        clearUpdatingWriter();
        this.mSimPbRecords.clear();
        this.mRecCount = 0;
        this.mRefreshAdnCache = false;
    }

    private void clearUpdatingWriter() {
        sendErrorResponse(this.mAdnUpdatingWaiter, "QtiSimPhoneBookAdnRecordCache reset");
        this.mAdnUpdatingWaiter = null;
    }

    private void sendErrorResponse(Message response, String errString) {
        if (response != null) {
            AsyncResult.forMessage(response).exception = new RuntimeException(errString);
            response.sendToTarget();
        }
    }

    private void notifyAndClearWaiters() {
        if (this.mAdnLoadingWaiters != null) {
            int s = this.mAdnLoadingWaiters.size();
            for (int i = 0; i < s; i++) {
                Message response = (Message) this.mAdnLoadingWaiters.get(i);
                if (response != null) {
                    AsyncResult.forMessage(response).result = this.mSimPbRecords;
                    response.sendToTarget();
                }
            }
            this.mAdnLoadingWaiters.clear();
        }
    }

    public void queryAdnRecord() {
        this.mRecCount = 0;
        this.mAdnCount = 0;
        this.mValidAdnCount = 0;
        this.mEmailCount = 0;
        this.mAddNumCount = 0;
        log("start to queryAdnRecord");
        this.mQtiRilInterface.registerForAdnRecordsInfo(this, 3, null);
        this.mQtiRilInterface.getAdnRecord(obtainMessage(2), this.mPhoneId);
        try {
            this.mLock.wait();
        } catch (InterruptedException e) {
            Rlog.e(LOG_TAG, "Interrupted Exception in queryAdnRecord");
        }
        this.mQtiRilInterface.unregisterForAdnRecordsInfo(this);
    }

    /* JADX WARNING: Missing block: B:12:0x0025, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void requestLoadAllAdnLike(Message response) {
        if (this.mAdnLoadingWaiters != null) {
            this.mAdnLoadingWaiters.add(response);
        }
        synchronized (this.mLock) {
            if (this.mSimPbRecords.isEmpty()) {
                queryAdnRecord();
                return;
            }
            log("ADN cache has already filled in");
            if (this.mRefreshAdnCache) {
                this.mRefreshAdnCache = false;
                refreshAdnCache();
            } else {
                notifyAndClearWaiters();
            }
        }
    }

    public void updateSimPbAdnBySearch(AdnRecord oldAdn, AdnRecord newAdn, Message response) {
        ArrayList<AdnRecord> oldAdnList = this.mSimPbRecords;
        synchronized (this.mLock) {
            if (this.mSimPbRecords.isEmpty()) {
                queryAdnRecord();
            } else {
                log("ADN cache has already filled in");
                if (this.mRefreshAdnCache) {
                    this.mRefreshAdnCache = false;
                    refreshAdnCache();
                }
            }
        }
        if (oldAdnList == null) {
            sendErrorResponse(response, "Sim PhoneBook Adn list not exist");
            return;
        }
        int index = -1;
        int count = 1;
        if (!oldAdn.isEmpty() || (newAdn.isEmpty() ^ 1) == 0) {
            Iterator<AdnRecord> it;
            if (oldAdn.getRecordNumber() > 0) {
                it = oldAdnList.iterator();
                while (it.hasNext()) {
                    AdnRecord itAdn = (AdnRecord) it.next();
                    if (itAdn != null && itAdn.getRecordNumber() == oldAdn.getRecordNumber()) {
                        index = count;
                        break;
                    }
                    count++;
                }
            }
            if (index == -1) {
                count = 1;
                it = oldAdnList.iterator();
                while (it.hasNext()) {
                    if (oldAdn.isEqualTagAndNumber((AdnRecord) it.next())) {
                        index = count;
                        break;
                    }
                    count++;
                }
            }
        } else {
            index = 0;
        }
        if (index == -1) {
            sendErrorResponse(response, "Sim PhoneBook Adn record don't exist for " + oldAdn);
        } else if (index == 0 && this.mValidAdnCount == this.mAdnCount) {
            sendErrorResponse(response, "Sim PhoneBook Adn record is full");
        } else {
            int recordIndex = index == 0 ? 0 : ((AdnRecord) oldAdnList.get(index - 1)).getRecordNumber();
            QtiSimPhoneBookAdnRecord updateAdn = new QtiSimPhoneBookAdnRecord();
            updateAdn.mRecordIndex = recordIndex;
            updateAdn.mAlphaTag = newAdn.getAlphaTag();
            updateAdn.mNumber = newAdn.getNumber();
            if (newAdn.getEmails() != null) {
                updateAdn.mEmails = newAdn.getEmails();
                updateAdn.mEmailCount = updateAdn.mEmails.length;
            }
            if (newAdn.getAdditionalNumbers() != null) {
                updateAdn.mAdNumbers = newAdn.getAdditionalNumbers();
                updateAdn.mAdNumCount = updateAdn.mAdNumbers.length;
            }
            if (this.mAdnUpdatingWaiter != null) {
                sendErrorResponse(response, "Have pending update for Sim PhoneBook Adn");
                return;
            }
            this.mAdnUpdatingWaiter = response;
            this.mQtiRilInterface.updateAdnRecord(updateAdn, obtainMessage(5, index, 0, newAdn), this.mPhoneId);
        }
    }

    public void handleMessage(Message msg) {
        AsyncResult ar = msg.obj;
        switch (msg.what) {
            case 1:
                ar = msg.obj;
                log("Initialized ADN done");
                if (ar.exception == null) {
                    invalidateAdnCache();
                    return;
                }
                log("Init ADN done Exception: " + ar.exception);
                return;
            case 2:
                log("Querying ADN record done");
                if (ar.exception != null) {
                    synchronized (this.mLock) {
                        this.mLock.notify();
                    }
                    for (Message response : this.mAdnLoadingWaiters) {
                        sendErrorResponse(response, "Query adn record failed" + ar.exception);
                    }
                    this.mAdnLoadingWaiters.clear();
                    return;
                }
                this.mAdnCount = ((int[]) ar.result)[0];
                this.mValidAdnCount = ((int[]) ar.result)[1];
                this.mEmailCount = ((int[]) ar.result)[2];
                this.mValidEmailCount = ((int[]) ar.result)[3];
                this.mAddNumCount = ((int[]) ar.result)[4];
                this.mValidAddNumCount = ((int[]) ar.result)[5];
                this.mMaxNameLen = ((int[]) ar.result)[6];
                this.mMaxNumberLen = ((int[]) ar.result)[7];
                this.mMaxEmailLen = ((int[]) ar.result)[8];
                this.mMaxAnrLen = ((int[]) ar.result)[9];
                log("Max ADN count is: " + this.mAdnCount + ", Valid ADN count is: " + this.mValidAdnCount + ", Email count is: " + this.mEmailCount + ", Valid email count is: " + this.mValidEmailCount + ", Add number count is: " + this.mAddNumCount + ", Valid add number count is: " + this.mValidAddNumCount + ", Max name length is: " + this.mMaxNameLen + ", Max number length is: " + this.mMaxNumberLen + ", Max email length is: " + this.mMaxEmailLen + ", Valid anr length is: " + this.mMaxAnrLen);
                if (this.mValidAdnCount == 0 || this.mRecCount == this.mValidAdnCount) {
                    sendMessage(obtainMessage(4));
                }
                if (OemConstant.EXP_VERSION) {
                    if (this.mAddNumCount > 0) {
                        TelephonyManager.getDefault();
                        TelephonyManager.setTelephonyProperty(this.mPhoneId, "gsm.sim.oppo.anr.support", "true");
                    } else {
                        TelephonyManager.getDefault();
                        TelephonyManager.setTelephonyProperty(this.mPhoneId, "gsm.sim.oppo.anr.support", "false");
                    }
                    if (this.mEmailCount > 0) {
                        TelephonyManager.getDefault();
                        TelephonyManager.setTelephonyProperty(this.mPhoneId, "gsm.sim.oppo.email.support", "true");
                        return;
                    }
                    TelephonyManager.getDefault();
                    TelephonyManager.setTelephonyProperty(this.mPhoneId, "gsm.sim.oppo.email.support", "false");
                    return;
                }
                return;
            case 3:
                log("Loading ADN record done");
                if (ar.exception == null) {
                    QtiSimPhoneBookAdnRecord[] AdnRecordsGroup = ar.result;
                    for (int i = 0; i < AdnRecordsGroup.length; i++) {
                        if (AdnRecordsGroup[i] != null) {
                            this.mSimPbRecords.add(new AdnRecord(0, AdnRecordsGroup[i].getRecordIndex(), AdnRecordsGroup[i].getAlphaTag(), AdnRecordsGroup[i].getNumber(), AdnRecordsGroup[i].getEmails(), AdnRecordsGroup[i].getAdNumbers()));
                            this.mRecCount++;
                        }
                    }
                    if (this.mRecCount == this.mValidAdnCount) {
                        sendMessage(obtainMessage(4));
                        return;
                    }
                    return;
                }
                return;
            case 4:
                log("Loading all ADN records done");
                synchronized (this.mLock) {
                    this.mLock.notify();
                }
                notifyAndClearWaiters();
                return;
            case 5:
                log("Update ADN record done");
                Throwable e = null;
                int cacheIndex = -1;
                if (ar.exception == null) {
                    int index = msg.arg1;
                    AdnRecord adn = ar.userObj;
                    int recordIndex = ((int[]) ar.result)[0];
                    cacheIndex = recordIndex;
                    AdnRecord adnRecord = null;
                    if (index > 0) {
                        adnRecord = (AdnRecord) this.mSimPbRecords.get(index - 1);
                    }
                    String[] newCount;
                    int adnRecordIndex;
                    String[] oldCount;
                    if (index == 0) {
                        log("Record number for added ADN is " + recordIndex);
                        adn.setRecordNumber(recordIndex);
                        this.mSimPbRecords.add(adn);
                        this.mValidAdnCount++;
                        newCount = adn.getEmails();
                        if (newCount != null && newCount.length > 0) {
                            this.mValidEmailCount++;
                        }
                        newCount = adn.getAdditionalNumbers();
                        if (newCount != null && newCount.length > 0) {
                            this.mValidAddNumCount++;
                        }
                    } else if (adn.isEmpty()) {
                        adnRecordIndex = ((AdnRecord) this.mSimPbRecords.get(index - 1)).getRecordNumber();
                        log("Record number for deleted ADN is " + adnRecordIndex);
                        if (recordIndex != adnRecordIndex || adnRecord == null) {
                            e = new RuntimeException("The index for deleted ADN record did not match");
                        } else {
                            oldCount = adnRecord.getEmails();
                            if (oldCount != null && oldCount.length > 0) {
                                this.mValidEmailCount--;
                            }
                            oldCount = adnRecord.getAdditionalNumbers();
                            if (oldCount != null && oldCount.length > 0) {
                                this.mValidAddNumCount--;
                            }
                            this.mSimPbRecords.remove(index - 1);
                            this.mValidAdnCount--;
                        }
                    } else {
                        adnRecordIndex = ((AdnRecord) this.mSimPbRecords.get(index - 1)).getRecordNumber();
                        log("Record number for changed ADN is " + adnRecordIndex);
                        if (recordIndex == adnRecordIndex) {
                            newCount = adn.getEmails();
                            oldCount = adnRecord.getEmails();
                            if ((newCount == null || newCount.length == 0) && oldCount != null && oldCount.length > 0) {
                                this.mValidEmailCount--;
                            } else if (newCount != null && newCount.length > 0 && (oldCount == null || oldCount.length == 0)) {
                                this.mValidEmailCount++;
                            }
                            newCount = adn.getAdditionalNumbers();
                            oldCount = adnRecord.getAdditionalNumbers();
                            if ((newCount == null || newCount.length == 0) && oldCount != null && oldCount.length > 0) {
                                this.mValidAddNumCount--;
                            } else if (newCount != null && newCount.length > 0 && (oldCount == null || oldCount.length == 0)) {
                                this.mValidAddNumCount++;
                            }
                            adn.setRecordNumber(recordIndex);
                            this.mSimPbRecords.set(index - 1, adn);
                        } else {
                            e = new RuntimeException("The index for changed ADN record did not match");
                        }
                    }
                } else {
                    e = new RuntimeException("Update adn record failed", ar.exception);
                }
                if (this.mAdnUpdatingWaiter != null) {
                    AsyncResult.forMessage(this.mAdnUpdatingWaiter, new Integer(cacheIndex), e);
                    Message tempAdnUpdatingWaiter = this.mAdnUpdatingWaiter;
                    this.mAdnUpdatingWaiter = null;
                    tempAdnUpdatingWaiter.sendToTarget();
                    return;
                }
                return;
            case 6:
                ar = msg.obj;
                log("SIM REFRESH occurred");
                if (ar.exception == null) {
                    IccRefreshResponse refreshRsp = ar.result;
                    if (refreshRsp == null) {
                        log("IccRefreshResponse received is null");
                        return;
                    } else if (refreshRsp.refreshResult == 0 || refreshRsp.refreshResult == 1) {
                        invalidateAdnCache();
                        return;
                    } else {
                        return;
                    }
                }
                log("SIM refresh Exception: " + ar.exception);
                return;
            default:
                return;
        }
    }

    public int getAdnCount() {
        return this.mAdnCount;
    }

    public int getUsedAdnCount() {
        return this.mValidAdnCount;
    }

    public int getEmailCount() {
        return this.mEmailCount;
    }

    public int getUsedEmailCount() {
        return this.mValidEmailCount;
    }

    public int getAnrCount() {
        return this.mAddNumCount;
    }

    public int getUsedAnrCount() {
        return this.mValidAddNumCount;
    }

    public int getMaxNameLen() {
        return this.mMaxNameLen;
    }

    public int getMaxNumberLen() {
        return this.mMaxNumberLen;
    }

    public int getMaxEmailLen() {
        return this.mMaxEmailLen;
    }

    public int getMaxAnrLen() {
        return this.mMaxAnrLen;
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    public void invalidateAdnCache() {
        log("invalidateAdnCache");
        this.mRefreshAdnCache = DBG;
    }

    private void refreshAdnCache() {
        log("refreshAdnCache");
        this.mSimPbRecords.clear();
        queryAdnRecord();
    }
}
