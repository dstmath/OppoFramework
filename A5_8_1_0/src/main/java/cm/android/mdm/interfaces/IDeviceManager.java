package cm.android.mdm.interfaces;

import android.content.ComponentName;
import android.graphics.Bitmap;
import java.util.List;

public interface IDeviceManager {
    void allowDrawOverlays(String str);

    void allowGetUsageStats(String str);

    void allowReadLogs(String str);

    Bitmap captureScreen();

    void enableAccessibilityService(ComponentName componentName);

    List<String> getSupportMethods();

    void ignoringBatteryOptimizations(String str);

    boolean isGPSTurnOn();

    boolean isRooted();

    void rebootDevice();

    boolean setActiveAdmin(ComponentName componentName);

    boolean setDeviceOwner(ComponentName componentName);

    boolean setDeviceOwner(String str);

    void setFaceLockDisabled(boolean z);

    void setLanguageChangeDisabled(boolean z);

    void setLockWallPaper(Bitmap bitmap);

    void setTimeChangeDisabled(boolean z);

    void setWallPaper(Bitmap bitmap);

    void shutdownDevice();

    void turnOnGPS(boolean z);
}
