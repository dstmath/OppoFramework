package cm.android.mdm.interfaces;

import java.util.List;

public interface IRestrictionManager {
    void addAppRestriction(int i, List<String> list);

    void addNetworkRestriction(int i, List<String> list);

    List<String> getAppRestriction(int i);

    int getAppRestrictionPolicies();

    int getCameraPolicies();

    List<String> getSupportMethods();

    int getWlanPolicies();

    boolean isAdbDisabled();

    boolean isBackButtonDisabled();

    boolean isBluetoothDisabled();

    boolean isBluetoothEnabled();

    boolean isDeveloperOptionsDisabled();

    boolean isExternalStorageDisabled();

    boolean isGPSDisabled();

    boolean isHomeButtonDisabled();

    boolean isMobileDataDisabled();

    boolean isNFCDisabled();

    boolean isSMSDisabled();

    boolean isSafeModeDisabled();

    boolean isSendNotificationDisabled();

    boolean isStatusBarExpandPanelDisabled();

    boolean isTaskButtonDisabled();

    boolean isUSBDataDisabled();

    boolean isUSBOtgDisabled();

    boolean isVoiceDisabled();

    boolean isWifiApDisabled();

    boolean isWifiDisabled();

    void removeAllAppRestriction(int i);

    void removeAppRestriction(int i, List<String> list);

    void removeNetworkRestriction(int i, List<String> list);

    void removeNetworkRestrictionAll(int i);

    void setAdbDisabled(boolean z);

    void setAppRestrictionPolicies(int i);

    void setBackButtonDisabled(boolean z);

    void setBluetoothDisabled(boolean z);

    void setBluetoothEnabled(boolean z);

    boolean setCameraPolicies(int i);

    void setDeveloperOptionsDisabled(boolean z);

    void setExternalStorageDisabled(boolean z);

    void setGPSDisabled(boolean z);

    void setHomeButtonDisabled(boolean z);

    void setMobileDataDisabled(boolean z);

    void setNFCDisabled(boolean z);

    void setNetworkRestriction(int i);

    void setSMSDisabled(boolean z);

    void setSafeModeDisabled(boolean z);

    void setSendNotificationDisabled(boolean z);

    void setStatusBarExpandPanelDisabled(boolean z);

    void setTaskButtonDisabled(boolean z);

    void setUSBDataDisabled(boolean z);

    void setUSBOtgDisabled(boolean z);

    void setVoiceDisabled(boolean z);

    void setWifiApDisabled(boolean z);

    void setWifiDisabled(boolean z);

    boolean setWlanPolicies(int i);
}
