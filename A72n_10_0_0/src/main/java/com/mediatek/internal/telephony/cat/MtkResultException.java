package com.mediatek.internal.telephony.cat;

import com.android.internal.telephony.cat.ResultCode;
import com.android.internal.telephony.cat.ResultException;

public class MtkResultException extends ResultException {
    MtkResultException(ResultCode result, int additionalInfo) {
        super(result);
        this.mResult = result;
        this.mExplanation = "";
        if (additionalInfo >= 0) {
            this.mAdditionalInfo = additionalInfo;
            return;
        }
        throw new AssertionError("Additional info must be greater than zero!");
    }

    MtkResultException(ResultCode result, int additionalInfo, String explanation) {
        this(result, additionalInfo);
        this.mExplanation = explanation;
    }
}
