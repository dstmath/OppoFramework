package com.mediatek.internal.telephony.dataconnection;

import android.os.AsyncResult;
import android.telephony.PcoData;
import android.telephony.data.ApnSetting;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.dataconnection.ApnContext;
import java.util.ArrayList;

public interface IDataConnectionExt {
    long getDisconnectDoneRetryTimer(String str, long j);

    boolean getIsPcoAllowedDefault();

    String getOperatorNumericFromImpi(String str, int i);

    int getPcoActionByApnType(ApnContext apnContext, PcoData pcoData);

    void handlePcoDataAfterAttached(AsyncResult asyncResult, Phone phone, ArrayList<ApnSetting> arrayList);

    boolean ignoreDataRoaming(String str);

    boolean ignoreDefaultDataUnselected(String str);

    boolean isDataAllowedAsOff(String str);

    boolean isDomesticRoamingEnabled();

    boolean isFdnEnableSupport();

    boolean isMeteredApnType(String str, boolean z);

    boolean isMeteredApnTypeByLoad();

    boolean isOnlySingleDcAllowed();

    boolean isPermanentCause(int i);

    void onDcActivated(String[] strArr, String str);

    void onDcDeactivated(String[] strArr, String str);

    void setIsPcoAllowedDefault(boolean z);

    void startDataRoamingStrategy(Phone phone);

    void stopDataRoamingStrategy();
}
