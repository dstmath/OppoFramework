package com.oppo.internal.telephony.phb;

import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.IOppoAdnRecordCache;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.uicc.AbstractAdnRecordCache;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.util.OemTelephonyUtils;
import java.util.ArrayList;
import java.util.Iterator;

public class OppoAdnRecordCache extends Handler implements IOppoAdnRecordCache {
    protected static final int EVENT_UPDATE_ADN_DONE = 2;
    private String LOG_TAG = "OppoAdnRecordCache";
    private AdnRecordCache mAdnCache;

    public OppoAdnRecordCache(AdnRecordCache AdnCache) {
        this.mAdnCache = AdnCache;
    }

    public boolean hasCmdInProgress(int efid) {
        if (((AbstractAdnRecordCache) OemTelephonyUtils.typeCasting(AbstractAdnRecordCache.class, this.mAdnCache)).getUserWriteResponse(efid) != null) {
            logd("hasCmdInProgress is True: efid:" + efid);
            return true;
        }
        logd("hasCmdInProgress is False efid:" + efid);
        return false;
    }

    public int oppoUpdateAdnBySearch(int efid, AdnRecord oldAdn, AdnRecord newAdn, String pin2, Message response) {
        int index = -1;
        AbstractAdnRecordCache tmpAbstractAdnRecordCache = (AbstractAdnRecordCache) OemTelephonyUtils.typeCasting(AbstractAdnRecordCache.class, this.mAdnCache);
        if (tmpAbstractAdnRecordCache.isUim && efid == 28474 && (index = tmpAbstractAdnRecordCache.getUpdateAdn(efid, oldAdn, newAdn, -1, pin2, response)) != -1) {
            return index;
        }
        int extensionEF = this.mAdnCache.extensionEfForEf(efid);
        if (extensionEF < 0) {
            getsendErrorResponse(response, "EF is not known ADN-like EF:" + efid);
            return -1;
        }
        ArrayList<AdnRecord> oldAdnList = this.mAdnCache.getRecordsIfLoaded(efid);
        if (oldAdnList == null) {
            getsendErrorResponse(response, "Adn list not exist for EF:" + efid);
            return -1;
        }
        Iterator<AdnRecord> it = oldAdnList.iterator();
        int count = 1;
        while (true) {
            if (!it.hasNext()) {
                break;
            } else if (oldAdn.isEqual(it.next())) {
                index = count;
                break;
            } else {
                count++;
            }
        }
        if (index == -1) {
            getsendErrorResponse(response, "Adn record don't exist for " + oldAdn);
            return -1;
        } else if (tmpAbstractAdnRecordCache.getUserWriteResponse(efid) != null) {
            getsendErrorResponse(response, "Have pending update for EF:" + efid);
            return -1;
        } else {
            tmpAbstractAdnRecordCache.putUserWriteResponse(efid, response);
            tmpAbstractAdnRecordCache.mRecordLoader.updateEF(newAdn, efid, extensionEF, index, pin2, obtainMessage(2, efid, index, newAdn));
            return index;
        }
    }

    public void oppoUpdateAdnByIndex(int efid, int extensionEF, AdnRecord adn, int recordIndex, String pin2, Message response) {
        AbstractAdnRecordCache tmpAbstractAdnRecordCache = (AbstractAdnRecordCache) OemTelephonyUtils.typeCasting(AbstractAdnRecordCache.class, this.mAdnCache);
        if (!tmpAbstractAdnRecordCache.isUim || efid != 28474) {
            if (extensionEF < 0) {
                getsendErrorResponse(response, "EF is not known ADN-like EF:" + efid);
            } else if (recordIndex == -1) {
                getsendErrorResponse(response, "Adn record don't exist for " + adn);
            } else if (tmpAbstractAdnRecordCache.getUserWriteResponse(efid) != null) {
                getsendErrorResponse(response, "Have pending update for EF:" + efid);
            } else {
                tmpAbstractAdnRecordCache.putUserWriteResponse(efid, response);
                tmpAbstractAdnRecordCache.mRecordLoader.updateEF(adn, efid, extensionEF, recordIndex, pin2, obtainMessage(2, efid, recordIndex, adn));
            }
        } else if (-1 == tmpAbstractAdnRecordCache.getUpdateAdn(efid, (AdnRecord) null, adn, recordIndex, pin2, response)) {
            getsendErrorResponse(response, "oppoUpdateAdnByIndex : update adn failed " + efid);
        }
    }

    public void getsendErrorResponse(Message response, String errString) {
        AbstractAdnRecordCache tmpAbstractAdnRecordCache = (AbstractAdnRecordCache) OemTelephonyUtils.typeCasting(AbstractAdnRecordCache.class, this.mAdnCache);
        if (tmpAbstractAdnRecordCache != null) {
            tmpAbstractAdnRecordCache.getsendErrorResponse(response, errString);
        }
    }

    /* access modifiers changed from: package-private */
    public void logd(String s) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(this.LOG_TAG, s);
        }
    }
}
