package com.mediatek.internal.telephony.cat;

import android.os.Handler;
import com.android.internal.telephony.cat.BerTlv;
import com.android.internal.telephony.cat.ResultCode;
import com.android.internal.telephony.cat.ResultException;
import com.android.internal.telephony.cat.RilMessage;
import com.android.internal.telephony.cat.RilMessageDecoder;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccUtils;

public class MtkRilMessageDecoder extends RilMessageDecoder {
    private int mSlotId;

    public int getSlotId() {
        return this.mSlotId;
    }

    public MtkRilMessageDecoder(Handler caller, IccFileHandler fh, int slotId) {
        super(caller, fh);
        this.mSlotId = slotId;
        MtkCatLog.d(this, "mCaller is " + this.mCaller.getClass().getName());
    }

    public MtkRilMessageDecoder() {
    }

    /* access modifiers changed from: protected */
    public void sendCmdForExecution(RilMessage rilMsg) {
        MtkCatLog.d(this, "sendCmdForExecution");
        if (rilMsg instanceof MtkRilMessage) {
            this.mCaller.obtainMessage(10, new MtkRilMessage((MtkRilMessage) rilMsg)).sendToTarget();
        } else {
            MtkRilMessageDecoder.super.sendCmdForExecution(rilMsg);
        }
    }

    public boolean decodeMessageParams(RilMessage rilMsg) {
        boolean decodingStarted;
        MtkCatLog.d(this, "decodeMessageParams");
        this.mCurrentRilMessage = rilMsg;
        int i = rilMsg.mId;
        if (i != 1) {
            if (!(i == 2 || i == 3)) {
                if (i != 4) {
                    if (i != 5) {
                        return false;
                    }
                }
            }
            try {
                byte[] rawData = IccUtils.hexStringToBytes((String) rilMsg.mData);
                try {
                    if (this.mCmdParamsFactory != null) {
                        this.mCmdParamsFactory.make(BerTlv.decode(rawData));
                        decodingStarted = true;
                    } else {
                        decodingStarted = false;
                    }
                    return decodingStarted;
                } catch (ResultException e) {
                    MtkCatLog.d(this, "decodeMessageParams: caught ResultException e=" + e);
                    this.mCurrentRilMessage.mId = 1;
                    this.mCurrentRilMessage.mResCode = e.result();
                    sendCmdForExecution(this.mCurrentRilMessage);
                    return false;
                }
            } catch (Exception e2) {
                MtkCatLog.d(this, "decodeMessageParams dropping zombie messages");
                return false;
            }
        }
        this.mCurrentRilMessage.mResCode = ResultCode.OK;
        sendCmdForExecution(this.mCurrentRilMessage);
        return false;
    }

    public void dispose() {
        quitNow();
        this.mStateStart = null;
        this.mStateCmdParamsReady = null;
        this.mCmdParamsFactory.dispose();
        this.mCmdParamsFactory = null;
        this.mCurrentRilMessage = null;
        this.mCaller = null;
        if (mInstance != null) {
            if (mInstance[this.mSlotId] != null) {
                mInstance[this.mSlotId] = null;
            }
            int i = 0;
            while (i < mSimCount && mInstance[i] == null) {
                i++;
            }
            if (i == mSimCount) {
                mInstance = null;
            }
        }
    }
}
