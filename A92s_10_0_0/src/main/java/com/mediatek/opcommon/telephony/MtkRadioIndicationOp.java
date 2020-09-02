package com.mediatek.opcommon.telephony;

import android.os.AsyncResult;
import android.os.Build;
import android.telephony.Rlog;
import java.util.ArrayList;
import vendor.mediatek.hardware.radio_op.V2_0.IRadioIndicationOp;

public class MtkRadioIndicationOp extends IRadioIndicationOp.Stub {
    private static final boolean ENG = "eng".equals(Build.TYPE);
    static final String TAG = "MtkRadioIndicationOp";
    private MtkRilOp mMtkRilOp;

    MtkRadioIndicationOp(MtkRilOp ril) {
        this.mMtkRilOp = ril;
        this.mMtkRilOp.log("MtkRadioIndicationOp constructor");
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IRadioIndicationOp
    public void onSimMeLockEvent(int indicationType, int eventId) {
        this.mMtkRilOp.processIndication(indicationType);
        MtkRilOp mtkRilOp = this.mMtkRilOp;
        mtkRilOp.log("onSimMeLockEvent eventId " + eventId);
        if (ENG) {
            this.mMtkRilOp.unsljLog(MtkRILConstantsOp.RIL_UNSOL_MELOCK_NOTIFICATION);
        }
        int[] response = {eventId};
        if (this.mMtkRilOp.mMelockRegistrants != null) {
            this.mMtkRilOp.mMelockRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    public void log(String text) {
        Rlog.d(TAG, text);
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IRadioIndicationOp
    public void responseModulationInfoInd(int indicationType, ArrayList<Integer> data) {
        this.mMtkRilOp.processIndication(indicationType);
        int[] response = new int[data.size()];
        for (int i = 0; i < data.size(); i++) {
            response[i] = data.get(i).intValue();
        }
        this.mMtkRilOp.unsljLogRet(3019, response);
        if (this.mMtkRilOp.mModulationRegistrants.size() != 0) {
            this.mMtkRilOp.mModulationRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IRadioIndicationOp
    public void enterSCBMInd(int indicationType) {
        this.mMtkRilOp.processIndication(indicationType);
        if (ENG) {
            this.mMtkRilOp.unsljLog(MtkRILConstantsOp.RIL_UNSOL_ENTER_SCBM);
        }
        if (this.mMtkRilOp.mEnterSCBMRegistrant != null) {
            this.mMtkRilOp.mEnterSCBMRegistrant.notifyRegistrant();
        }
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IRadioIndicationOp
    public void exitSCBMInd(int indicationType) {
        this.mMtkRilOp.processIndication(indicationType);
        if (ENG) {
            this.mMtkRilOp.unsljLog(MtkRILConstantsOp.RIL_UNSOL_EXIT_SCBM);
        }
        this.mMtkRilOp.mExitSCBMRegistrants.notifyRegistrants();
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IRadioIndicationOp
    public void onRsuEvent(int indicationType, int eventId, String eventString) {
        this.mMtkRilOp.processIndication(indicationType);
        if (ENG) {
            this.mMtkRilOp.unsljLog(MtkRILConstantsOp.RIL_UNSOL_RSU_EVENT);
        }
        this.mMtkRilOp.mRsuEventRegistrants.notifyRegistrants(new AsyncResult((Object) null, new String[]{Integer.toString(eventId), eventString}, (Throwable) null));
    }
}
