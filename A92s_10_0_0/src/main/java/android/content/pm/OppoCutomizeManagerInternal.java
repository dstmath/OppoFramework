package android.content.pm;

import java.util.List;

public abstract class OppoCutomizeManagerInternal {
    public abstract List<String> getAppUninstallationPolicies(int i);

    public abstract List<String> getDisallowUninstallApps();

    public abstract List<String> getInstallSourceList();

    public abstract boolean isInstallSourceEnable();

    public abstract void sendBroadcastForArmy();
}
