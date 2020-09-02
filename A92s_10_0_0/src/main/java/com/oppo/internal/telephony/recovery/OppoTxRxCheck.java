package com.oppo.internal.telephony.recovery;

import android.telephony.Rlog;
import java.util.ArrayList;
import java.util.Iterator;

public class OppoTxRxCheck {
    private static final String TAG = "OppoTxRxCheck";
    private OppoFastRecovery mOppoFastRecovery;
    private ArrayList<Integer> mRx0CountList = new ArrayList<>();

    public OppoTxRxCheck(OppoFastRecovery recovery) {
        this.mOppoFastRecovery = recovery;
    }

    private void checkRx0ListCount() {
        int maxCount = this.mOppoFastRecovery.mRecoveryConfig.mTxRxCheckCount;
        if (this.mRx0CountList.size() > maxCount) {
            int removeCount = this.mRx0CountList.size() - maxCount;
            Iterator<Integer> iter = this.mRx0CountList.iterator();
            while (iter.hasNext() && removeCount > 0) {
                iter.next();
                iter.remove();
                removeCount--;
            }
        }
    }

    private void addRx0countInner(int rx0count) {
        this.mRx0CountList.add(Integer.valueOf(rx0count));
    }

    public void addNewRx0count(int rx0cout) {
        addRx0countInner(rx0cout);
        checkRx0ListCount();
    }

    public int getRx0CountTotal() {
        int total = 0;
        Iterator<Integer> it = this.mRx0CountList.iterator();
        while (it.hasNext()) {
            total += it.next().intValue();
        }
        return total;
    }

    public boolean checkRx0Invalid() {
        int total = getRx0CountTotal();
        if (total < this.mOppoFastRecovery.mRecoveryConfig.mRx0CountInvalidTh) {
            return false;
        }
        Rlog.d(TAG, "checkRx0Invalid totalcout:" + total + ", th:" + this.mOppoFastRecovery.mRecoveryConfig.mRx0CountInvalidTh);
        return true;
    }

    public void clearRx0countList() {
        this.mRx0CountList.clear();
    }
}
