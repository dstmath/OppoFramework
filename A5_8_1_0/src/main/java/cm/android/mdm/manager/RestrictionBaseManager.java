package cm.android.mdm.manager;

import cm.android.mdm.exception.MdmException;
import cm.android.mdm.interfaces.IRestrictionManager;
import cm.android.mdm.util.MethodSignature;
import java.util.List;

public class RestrictionBaseManager implements IRestrictionManager {
    public void setWifiDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isWifiDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setBluetoothDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isBluetoothDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setWifiApDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isWifiApDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setUSBDataDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isUSBDataDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setExternalStorageDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isExternalStorageDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setNFCDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isNFCDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setMobileDataDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isMobileDataDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setVoiceDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isVoiceDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setSMSDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isSMSDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setSafeModeDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isSafeModeDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setAdbDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isAdbDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setUSBOtgDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isUSBOtgDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setGPSDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isGPSDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setDeveloperOptionsDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isDeveloperOptionsDisabled() {
        throw new MdmException("Not implement yet");
    }

    public List<String> getSupportMethods() {
        return MethodSignature.getMethodSignatures(getClass());
    }
}
