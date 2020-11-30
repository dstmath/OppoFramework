package com.mediatek.internal.telephony;

import android.hardware.radio.V1_0.RadioResponseInfo;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemClock;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.RILRequest;
import java.util.ArrayList;

public class MtkMessageBoost {
    protected static final int MESSAGE_BOOT_TIME_MSEC = 10000;
    private static MtkMessageBoost sMtkMessageBoost;
    MtkRIL mMtkRil;
    protected int mPriorityFlag = 0;

    public static MtkMessageBoost init(MtkRIL mtkRIL) {
        MtkMessageBoost mtkMessageBoost;
        synchronized (MtkMessageBoost.class) {
            if (sMtkMessageBoost == null) {
                sMtkMessageBoost = new MtkMessageBoost(mtkRIL);
            }
            mtkMessageBoost = sMtkMessageBoost;
        }
        return mtkMessageBoost;
    }

    public static MtkMessageBoost getInstance() {
        MtkMessageBoost mtkMessageBoost;
        synchronized (MtkMessageBoost.class) {
            mtkMessageBoost = sMtkMessageBoost;
        }
        return mtkMessageBoost;
    }

    public MtkMessageBoost(MtkRIL mtkRIL) {
        this.mMtkRil = mtkRIL;
    }

    public void setPriorityFlag(int flag, Phone phone) {
        this.mPriorityFlag |= flag;
    }

    public void clearPriorityFlag(int flag) {
        this.mPriorityFlag &= ~flag;
    }

    public int getPriorityFlag(int flag) {
        return this.mPriorityFlag & flag;
    }

    public static void sendMessageResponseWithPriority(Message msg, Object ret) {
        if (msg != null) {
            AsyncResult.forMessage(msg, ret, (Throwable) null);
            msg.getTarget().sendMessageAtTime(msg, SystemClock.uptimeMillis() - 10000);
        }
    }

    public void responseStringsWithPriority(RadioResponseInfo responseInfo, String... str) {
        ArrayList<String> strings = new ArrayList<>();
        for (String str2 : str) {
            strings.add(str2);
        }
        responseStringArrayListWithPriority(this.mMtkRil, responseInfo, strings);
    }

    public void responseStringArrayListWithPriority(RIL ril, RadioResponseInfo responseInfo, ArrayList<String> strings) {
        RILRequest rr = ril.processResponse(responseInfo);
        if (rr != null) {
            String[] ret = new String[strings.size()];
            for (int i = 0; i < strings.size(); i++) {
                ret[i] = strings.get(i);
            }
            if (responseInfo.error == 0) {
                sendMessageResponseWithPriority(rr.mResult, ret);
            }
            ril.processResponseDone(rr, responseInfo, ret);
        }
    }

    public void responseIntsWithPriority(RadioResponseInfo responseInfo, int... var) {
        ArrayList<Integer> ints = new ArrayList<>();
        for (int i : var) {
            ints.add(Integer.valueOf(i));
        }
        responseIntArrayListWithPriority(responseInfo, ints);
    }

    public void responseIntArrayListWithPriority(RadioResponseInfo responseInfo, ArrayList<Integer> var) {
        RILRequest rr = this.mMtkRil.processResponse(responseInfo);
        if (rr != null) {
            int[] ret = new int[var.size()];
            for (int i = 0; i < var.size(); i++) {
                ret[i] = var.get(i).intValue();
            }
            if (responseInfo.error == 0) {
                sendMessageResponseWithPriority(rr.mResult, ret);
            }
            this.mMtkRil.processResponseDone(rr, responseInfo, ret);
        }
    }
}
