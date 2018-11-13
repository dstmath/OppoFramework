package com.mediatek.common.telephony;

import android.content.Context;
import android.telephony.ServiceState;
import java.util.Map;

public interface IServiceStateExt {
    boolean allowSpnDisplayed();

    int getNetworkTypeForMota(int i);

    boolean isBroadcastEmmrrsPsResume(int i);

    boolean isImeiLocked();

    boolean isNeedDisableIVSR();

    boolean isRoamingForSpecialSIM(String str, String str2);

    boolean isSupportRatBalancing();

    Map<String, String> loadSpnOverrides();

    int mapGsmSignalDbm(int i, int i2);

    int mapGsmSignalLevel(int i, int i2);

    int mapLteSignalLevel(int i, int i2, int i3);

    int mapUmtsSignalLevel(int i);

    int needAutoSwitchRatMode(int i, String str);

    boolean needBlankDisplay(int i);

    boolean needBrodcastAcmt(int i, int i2);

    boolean needEMMRRS();

    boolean needIccCardTypeNotification(String str);

    boolean needIgnoreFemtocellUpdate(int i, int i2);

    boolean needIgnoredState(int i, int i2, int i3);

    boolean needRejectCauseNotification(int i);

    boolean needSpnRuleShowPlmnOnly();

    boolean needToShowCsgId();

    String onUpdateSpnDisplay(String str, ServiceState serviceState, int i);

    String onUpdateSpnDisplayForIms(String str, ServiceState serviceState, int i, int i2, Object obj);

    boolean operatorDefinedInternationalRoaming(String str);

    boolean supportEccForEachSIM();

    String updateOpAlphaLongForHK(String str, String str2, int i);

    void updateOplmn(Context context, Object obj);
}
