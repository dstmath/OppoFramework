package cm.android.mdm.manager;

import android.content.ComponentName;
import android.graphics.Bitmap;
import cm.android.mdm.exception.MdmException;
import cm.android.mdm.interfaces.IDeviceManager;
import cm.android.mdm.util.MethodSignature;
import java.util.List;

public class DeviceBaseManager implements IDeviceManager {
    public boolean setDeviceOwner(String packageName) {
        throw new MdmException("Not implement yet");
    }

    public boolean setDeviceOwner(ComponentName componentName) {
        throw new MdmException("Not implement yet");
    }

    public boolean setActiveAdmin(ComponentName adminReceiver) {
        throw new MdmException("Not implement yet");
    }

    public void enableAccessibilityService(ComponentName componentName) {
        throw new MdmException("Not implement yet");
    }

    public void allowGetUsageStats(String packageName) {
        throw new MdmException("Not implement yet");
    }

    public void ignoringBatteryOptimizations(String packageName) {
        throw new MdmException("Not implement yet");
    }

    public void allowDrawOverlays(String packageName) {
        throw new MdmException("Not implement yet");
    }

    public void allowReadLogs(String packageName) {
        throw new MdmException("Not implement yet");
    }

    public void shutdownDevice() {
        throw new MdmException("Not implement yet");
    }

    public void rebootDevice() {
        throw new MdmException("Not implement yet");
    }

    public boolean isRooted() {
        throw new MdmException("Not implement yet");
    }

    public void turnOnGPS(boolean on) {
        throw new MdmException("Not implement yet");
    }

    public boolean isGPSTurnOn() {
        throw new MdmException("Not implement yet");
    }

    public void setTimeChangeDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public void setLanguageChangeDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public void setFaceLockDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public Bitmap captureScreen() {
        throw new MdmException("Not implement yet");
    }

    public void setWallPaper(Bitmap bitmap) {
        throw new MdmException("Not implement yet");
    }

    public void setLockWallPaper(Bitmap bitmap) {
        throw new MdmException("Not implement yet");
    }

    public List<String> getSupportMethods() {
        return MethodSignature.getMethodSignatures(getClass());
    }
}
