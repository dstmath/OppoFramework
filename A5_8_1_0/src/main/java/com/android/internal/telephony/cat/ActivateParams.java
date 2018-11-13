package com.android.internal.telephony.cat;

/* compiled from: CommandParams */
class ActivateParams extends CommandParams {
    int mActivateTarget;

    ActivateParams(CommandDetails cmdDet, int target) {
        super(cmdDet);
        this.mActivateTarget = target;
    }
}
