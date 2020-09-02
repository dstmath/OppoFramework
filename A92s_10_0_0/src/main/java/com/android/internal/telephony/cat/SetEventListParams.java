package com.android.internal.telephony.cat;

public class SetEventListParams extends CommandParams {
    public int[] mEventInfo;

    public SetEventListParams(CommandDetails cmdDet, int[] eventInfo) {
        super(cmdDet);
        this.mEventInfo = eventInfo;
    }
}
