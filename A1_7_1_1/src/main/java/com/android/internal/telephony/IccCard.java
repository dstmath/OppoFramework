package com.android.internal.telephony;

import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.PersoSubState;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;

public interface IccCard {
    void changeIccFdnPassword(String str, String str2, Message message);

    void changeIccLockPassword(String str, String str2, Message message);

    void getCarrierRestrictionState(Message message);

    String getIccCardType();

    boolean getIccFdnAvailable();

    boolean getIccFdnEnabled();

    IccFileHandler getIccFileHandler();

    boolean getIccLockEnabled();

    boolean getIccPin2Blocked();

    boolean getIccPuk2Blocked();

    IccRecords getIccRecords();

    PersoSubState getNetworkPersoType();

    String getServiceProviderName();

    State getState();

    boolean hasIccCard();

    boolean isApplicationOnIcc(AppType appType);

    void queryIccNetworkLock(int i, Message message);

    void registerForAbsent(Handler handler, int i, Object obj);

    void registerForFdnChanged(Handler handler, int i, Object obj);

    void registerForLocked(Handler handler, int i, Object obj);

    void registerForNetworkLocked(Handler handler, int i, Object obj);

    void repollIccStateForModemSmlChangeFeatrue(boolean z);

    void setCarrierRestrictionState(int i, String str, Message message);

    void setIccFdnEnabled(boolean z, String str, Message message);

    void setIccLockEnabled(boolean z, String str, Message message);

    void setIccNetworkLockEnabled(int i, int i2, String str, String str2, String str3, String str4, Message message);

    void supplyNetworkDepersonalization(String str, Message message);

    void supplyPin(String str, Message message);

    void supplyPin2(String str, Message message);

    void supplyPuk(String str, String str2, Message message);

    void supplyPuk2(String str, String str2, Message message);

    void unregisterForAbsent(Handler handler);

    void unregisterForFdnChanged(Handler handler);

    void unregisterForLocked(Handler handler);

    void unregisterForNetworkLocked(Handler handler);
}
