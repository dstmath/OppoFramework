package com.android.internal.telephony.uicc;

import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.IOppoAdnRecordCache;

public class AbstractAdnRecordCache extends Handler {
    public boolean isUim;
    public AdnRecordLoader mRecordLoader;
    protected IOppoAdnRecordCache mReference;

    public boolean hasCmdInProgress(int efid) {
        return this.mReference.hasCmdInProgress(efid);
    }

    public int oppoUpdateAdnBySearch(int efid, AdnRecord oldAdn, AdnRecord newAdn, String pin2, Message response) {
        return this.mReference.oppoUpdateAdnBySearch(efid, oldAdn, newAdn, pin2, response);
    }

    public void oppoUpdateAdnByIndex(int efid, int extensionEF, AdnRecord adn, int recordIndex, String pin2, Message response) {
        this.mReference.oppoUpdateAdnByIndex(efid, extensionEF, adn, recordIndex, pin2, response);
    }

    public int getUpdateAdn(int efid, AdnRecord oldAdn, AdnRecord newAdn, int index, String pin2, Message response) {
        return -1;
    }

    public void getsendErrorResponse(Message response, String errString) {
    }

    public Message getUserWriteResponse(int efid) {
        return null;
    }

    public void putUserWriteResponse(int efid, Message response) {
    }
}
