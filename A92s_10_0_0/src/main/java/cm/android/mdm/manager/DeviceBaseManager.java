package cm.android.mdm.manager;

import android.content.ComponentName;
import android.graphics.Bitmap;
import cm.android.mdm.exception.MdmException;
import cm.android.mdm.interfaces.IDeviceManager;
import cm.android.mdm.util.MethodSignature;
import java.util.List;

public class DeviceBaseManager implements IDeviceManager {
    @Override // cm.android.mdm.interfaces.IDeviceManager
    public boolean setDeviceOwner(String packageName) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public boolean setDeviceOwner(ComponentName componentName) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public boolean setActiveAdmin(ComponentName adminReceiver) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public void enableAccessibilityService(ComponentName componentName) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public void allowGetUsageStats(String packageName) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public void ignoringBatteryOptimizations(String packageName) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public void allowDrawOverlays(String packageName) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public void allowReadLogs(String packageName) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public void shutdownDevice() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public void rebootDevice() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public boolean isRooted() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public void turnOnGPS(boolean on) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public boolean isGPSTurnOn() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public void setTimeChangeDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public void setLanguageChangeDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public void setFaceLockDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public Bitmap captureScreen() {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public void setWallPaper(Bitmap bitmap) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public void setLockWallPaper(Bitmap bitmap) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public List<String> getSupportMethods() {
        return MethodSignature.getMethodSignatures(getClass());
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public boolean setDefaultLauncher(ComponentName home) {
        throw new MdmException("Not implement yet");
    }

    @Override // cm.android.mdm.interfaces.IDeviceManager
    public void clearDefaultLauncher() {
        throw new MdmException("Not implement yet");
    }
}
