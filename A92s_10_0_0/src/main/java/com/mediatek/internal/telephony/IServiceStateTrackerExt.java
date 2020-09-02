package com.mediatek.internal.telephony;

import mediatek.telephony.MtkServiceState;

public interface IServiceStateTrackerExt {
    boolean allowSpnDisplayed();

    int[] getMtkLteRsrpThreshold();

    int[] getMtkLteRssnrThreshold();

    boolean getMtkRsrpOnly();

    boolean isBroadcastEmmrrsPsResume(int i);

    boolean isImeiLocked();

    boolean isNeedDisableIVSR();

    boolean isRoamingForSpecialSIM(String str, String str2);

    boolean isSupportRatBalancing();

    int needAutoSwitchRatMode(int i, String str);

    boolean needBlankDisplay(int i);

    boolean needBrodcastAcmt(int i, int i2);

    boolean needEMMRRS();

    boolean needIgnoreFemtocellUpdate(int i, int i2);

    boolean needIgnoredState(int i, int i2, int i3);

    boolean needRejectCauseNotification(int i);

    boolean needSpnRuleShowPlmnOnly();

    boolean needToShowCsgId();

    String onUpdateSpnDisplay(String str, MtkServiceState mtkServiceState, int i);

    String onUpdateSpnDisplayForIms(String str, MtkServiceState mtkServiceState, int i, int i2, Object obj);

    boolean operatorDefinedInternationalRoaming(String str);

    boolean showEccForIms();
}
