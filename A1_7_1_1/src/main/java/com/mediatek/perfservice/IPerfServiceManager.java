package com.mediatek.perfservice;

public interface IPerfServiceManager {
    void appBoostEnable(String str);

    void boostDisable(int i);

    void boostEnable(int i);

    void boostEnableTimeout(int i, int i2);

    void boostEnableTimeoutMs(int i, int i2);

    void dumpAll();

    int getClusterInfo(int i, int i2);

    String getGiftAttr(String str, String str2);

    int getLastBoostPid();

    int getPackAttr(String str, int i);

    void levelBoost(int i);

    void notifyAppState(String str, String str2, int i, int i2);

    void notifyDisplayType(int i);

    void notifyFrameUpdate(int i);

    void notifyUserStatus(int i, int i2);

    int reloadWhiteList();

    void restorePolicy(int i);

    void setExclusiveCore(int i, int i2);

    void setFavorPid(int i);

    void setUidInfo(int i, int i2);

    void systemReady();

    void userDisable(int i);

    void userDisableAll();

    void userEnable(int i);

    void userEnableAsync(int i);

    void userEnableTimeout(int i, int i2);

    void userEnableTimeoutAsync(int i, int i2);

    void userEnableTimeoutMs(int i, int i2);

    void userEnableTimeoutMsAsync(int i, int i2);

    int userGetCapability(int i);

    int userReg(int i, int i2, int i3, int i4);

    int userRegBigLittle(int i, int i2, int i3, int i4, int i5, int i6);

    int userRegScn(int i, int i2);

    void userRegScnConfig(int i, int i2, int i3, int i4, int i5, int i6);

    void userResetAll();

    void userRestoreAll();

    void userUnreg(int i);

    void userUnregScn(int i);
}
