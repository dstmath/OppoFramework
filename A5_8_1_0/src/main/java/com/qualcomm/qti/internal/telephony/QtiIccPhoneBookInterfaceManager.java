package com.qualcomm.qti.internal.telephony;

import android.content.ContentValues;
import android.os.Message;
import android.text.TextUtils;
import com.android.internal.telephony.IccPhoneBookInterfaceManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.uicc.AdnRecord;
import com.qualcomm.qti.internal.telephony.uicccontact.QtiSimPhoneBookAdnRecordCache;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class QtiIccPhoneBookInterfaceManager extends IccPhoneBookInterfaceManager {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "QtiIccPhoneBookInterfaceManager";
    private QtiSimPhoneBookAdnRecordCache mSimPbAdnCache;

    public QtiIccPhoneBookInterfaceManager(Phone phone) {
        super(phone);
        if (isSimPhoneBookEnabled() && this.mSimPbAdnCache == null) {
            this.mSimPbAdnCache = new QtiSimPhoneBookAdnRecordCache(phone.getContext(), phone.getPhoneId(), phone.mCi);
        }
    }

    private boolean isSimPhoneBookEnabled() {
        if (this.mPhone.getContext().getResources().getBoolean(17957013)) {
            return DBG;
        }
        return false;
    }

    public void dispose() {
        super.dispose();
        if (this.mRecords != null) {
            this.mRecords.clear();
        }
    }

    public List<AdnRecord> getAdnRecordsInEf(int efid) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        }
        efid = updateEfForIccType(efid);
        logd("getAdnRecordsInEF: efid=0x" + Integer.toHexString(efid).toUpperCase() + " for slot:" + this.mPhone.getPhoneId());
        synchronized (this.mLock) {
            checkThread();
            AtomicBoolean status = new AtomicBoolean(false);
            Message response = this.mBaseHandler.obtainMessage(2, efid, 0, status);
            if (!isSimPhoneBookEnabled() || (efid != 20272 && efid != 28474)) {
                logd("getAdnRecordsInEF: use AdnCache for slot:" + this.mPhone.getPhoneId());
                if (this.mAdnCache != null) {
                    this.mAdnCache.requestLoadAllAdnLike(efid, this.mAdnCache.extensionEfForEf(efid), response);
                    waitForResult(status);
                } else {
                    loge("Failure while trying to load from SIM due to uninitialised adncache");
                }
            } else if (this.mSimPbAdnCache != null) {
                this.mSimPbAdnCache.requestLoadAllAdnLike(response);
                waitForResult(status);
            } else {
                loge("Failure while trying to load from SIM due to uninit  sim pb adncache");
            }
        }
        return this.mRecords;
    }

    public boolean updateAdnRecordsWithContentValuesInEfBySearch(int efid, ContentValues values, String pin2) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        }
        String oldTag = values.getAsString("tag");
        String newTag = values.getAsString("newTag");
        String oldPhoneNumber = values.getAsString("number");
        String newPhoneNumber = values.getAsString("newNumber");
        String oldEmail = values.getAsString("emails");
        String newEmail = values.getAsString("newEmails");
        String oldAnr = values.getAsString("anrs");
        String newAnr = values.getAsString("newAnrs");
        String[] oldEmailArray = TextUtils.isEmpty(oldEmail) ? null : getStringArray(oldEmail);
        String[] newEmailArray = TextUtils.isEmpty(newEmail) ? null : getStringArray(newEmail);
        String[] oldAnrArray = TextUtils.isEmpty(oldAnr) ? null : getAnrStringArray(oldAnr);
        String[] newAnrArray = TextUtils.isEmpty(newAnr) ? null : getAnrStringArray(newAnr);
        efid = updateEfForIccType(efid);
        int index = -1;
        String strIndex = values.getAsString("index");
        if (!TextUtils.isEmpty(strIndex)) {
            index = Integer.valueOf(strIndex).intValue();
        }
        logd("updateAdnRecordsWithContentValuesInEfBySearch: efid=" + efid + ", values = " + values + ", pin2=" + pin2);
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
            if (isSimPhoneBookEnabled() && (efid == 20272 || efid == 28474)) {
                if (this.mSimPbAdnCache != null) {
                    this.mSimPbAdnCache.updateSimPbAdnBySearch(oldAdn, newAdn, response);
                    waitForResult(atomicBoolean);
                } else {
                    loge("Failure while trying to update by search due to uninit sim pb adncache");
                }
            } else if (this.mAdnCache != null) {
                this.mAdnCache.updateAdnBySearch(efid, oldAdn, newAdn, pin2, response);
                waitForResult(atomicBoolean);
            } else {
                loge("Failure while trying to update by search due to uninitialised adncache");
            }
        }
        return this.mSuccess;
    }

    public int[] getAdnRecordsCapacity() {
        int[] capacity = new int[10];
        if (isSimPhoneBookEnabled()) {
            if (this.mSimPbAdnCache != null) {
                capacity[0] = this.mSimPbAdnCache.getAdnCount();
                capacity[1] = this.mSimPbAdnCache.getUsedAdnCount();
                capacity[2] = this.mSimPbAdnCache.getEmailCount();
                capacity[3] = this.mSimPbAdnCache.getUsedEmailCount();
                capacity[4] = this.mSimPbAdnCache.getAnrCount();
                capacity[5] = this.mSimPbAdnCache.getUsedAnrCount();
                capacity[6] = this.mSimPbAdnCache.getMaxNameLen();
                capacity[7] = this.mSimPbAdnCache.getMaxNumberLen();
                capacity[8] = this.mSimPbAdnCache.getMaxEmailLen();
                capacity[9] = this.mSimPbAdnCache.getMaxAnrLen();
            } else {
                loge("mAdnCache is NULL when getAdnRecordsCapacity.");
            }
        }
        logd("getAdnRecordsCapacity: max adn=" + capacity[0] + ", used adn=" + capacity[1] + ", max email=" + capacity[2] + ", used email=" + capacity[3] + ", max anr=" + capacity[4] + ", used anr=" + capacity[5] + ", max name length =" + capacity[6] + ", max number length =" + capacity[7] + ", max email length =" + capacity[8] + ", max anr length =" + capacity[9]);
        return capacity;
    }

    public int oppoGetSimPhonebookAllSpace() {
        if (this.phonebookReady) {
            int simTotal = this.mSimPbAdnCache == null ? 0 : this.mSimPbAdnCache.getAdnCount();
            logd("oppoGetSimPhonebookAllSpace:" + simTotal + " for slot:" + this.mPhone.getPhoneId());
            return simTotal;
        }
        logd("oppoGetSimPhonebookAllSpace: phonebook not ready for slot:" + this.mPhone.getPhoneId());
        return -1;
    }

    public int oppoGetSimPhonebookUsedSpace() {
        logd("oppoGetSimPhonebookUsedSpace");
        if (this.phonebookReady) {
            int simUsed = this.mSimPbAdnCache == null ? 0 : this.mSimPbAdnCache.getUsedAdnCount();
            logd("oppoGetSimPhonebookUsedSpace:" + simUsed + " for slot:" + this.mPhone.getPhoneId());
            return simUsed;
        }
        logd("oppoGetSimPhonebookUsedSpace: phonebook not ready for slot:" + this.mPhone.getPhoneId());
        return -1;
    }
}
