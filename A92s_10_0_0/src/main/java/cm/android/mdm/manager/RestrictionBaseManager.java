package cm.android.mdm.manager;

import cm.android.mdm.exception.MdmException;
import cm.android.mdm.interfaces.IRestrictionManager;
import cm.android.mdm.util.MethodSignature;
import java.util.List;

public class RestrictionBaseManager implements IRestrictionManager {
    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setWifiDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isWifiDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setBluetoothEnabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isBluetoothEnabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setBluetoothDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isBluetoothDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setWifiApDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isWifiApDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public int getWlanPolicies() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean setWlanPolicies(int mode) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setUSBDataDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isUSBDataDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setExternalStorageDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isExternalStorageDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setNFCDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isNFCDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setMobileDataDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isMobileDataDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setVoiceDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isVoiceDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setSMSDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isSMSDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setSafeModeDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isSafeModeDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setAdbDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isAdbDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setUSBOtgDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isUSBOtgDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setGPSDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isGPSDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setDeveloperOptionsDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isDeveloperOptionsDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public List<String> getSupportMethods() {
        return MethodSignature.getMethodSignatures(getClass());
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setTaskButtonDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isTaskButtonDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setHomeButtonDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isHomeButtonDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setBackButtonDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isBackButtonDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setSendNotificationDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isSendNotificationDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setStatusBarExpandPanelDisabled(boolean disable) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean isStatusBarExpandPanelDisabled() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setNetworkRestriction(int pattern) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void addNetworkRestriction(int pattern, List<String> list) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void removeNetworkRestriction(int pattern, List<String> list) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void removeNetworkRestrictionAll(int pattern) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public boolean setCameraPolicies(int mode) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public int getCameraPolicies() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void setAppRestrictionPolicies(int pattern) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public int getAppRestrictionPolicies() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public List<String> getAppRestriction(int pattern) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void addAppRestriction(int pattern, List<String> list) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void removeAppRestriction(int pattern, List<String> list) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager
    public void removeAllAppRestriction(int pattern) {
        throw new MdmException("Not implement yet");
    }
}
