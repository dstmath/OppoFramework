package android.content.pm;

import java.util.List;

public abstract class OppoCutomizeManagerInternal {
    public abstract List<String> getAccessibilityServiceWhiteList();

    public abstract List<String> getAllInstallSysAppList();

    public abstract List<String> getAppUninstallationPolicies(int i);

    public abstract List<String> getDetachableInstallSysAppList();

    public abstract List<String> getDisallowUninstallApps();

    public abstract List<String> getInstallSourceList();

    public abstract List<String> getInstalledAppBlackList();

    public abstract List<String> getInstalledAppWhiteList();

    public abstract List<String> getPrivInstallSysAppList();

    public abstract boolean isInstallSourceEnable();

    public abstract void sendBroadcastForArmy();
}
