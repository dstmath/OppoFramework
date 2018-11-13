package cm.android.mdm.interfaces;

import java.util.List;

public interface IRestrictionManager {
    List<String> getSupportMethods();

    boolean isAdbDisabled();

    boolean isBluetoothDisabled();

    boolean isDeveloperOptionsDisabled();

    boolean isExternalStorageDisabled();

    boolean isGPSDisabled();

    boolean isMobileDataDisabled();

    boolean isNFCDisabled();

    boolean isSMSDisabled();

    boolean isSafeModeDisabled();

    boolean isUSBDataDisabled();

    boolean isUSBOtgDisabled();

    boolean isVoiceDisabled();

    boolean isWifiApDisabled();

    boolean isWifiDisabled();

    void setAdbDisabled(boolean z);

    void setBluetoothDisabled(boolean z);

    void setDeveloperOptionsDisabled(boolean z);

    void setExternalStorageDisabled(boolean z);

    void setGPSDisabled(boolean z);

    void setMobileDataDisabled(boolean z);

    void setNFCDisabled(boolean z);

    void setSMSDisabled(boolean z);

    void setSafeModeDisabled(boolean z);

    void setUSBDataDisabled(boolean z);

    void setUSBOtgDisabled(boolean z);

    void setVoiceDisabled(boolean z);

    void setWifiApDisabled(boolean z);

    void setWifiDisabled(boolean z);
}
