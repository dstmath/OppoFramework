package android.content.pm;

import android.content.pm.PackageParser;
import java.util.ArrayList;

public abstract class OppoPackageManagerInternal {
    public abstract boolean allowAddInstallPermForDataApp(String str);

    public abstract void autoUnfreezePackage(String str, int i, String str2);

    public abstract void clearIconCache();

    public abstract ArrayList<String> getIgnoreAppList();

    public abstract void grantOppoPermissionByGroup(PackageParser.Package packageR, String str, String str2, int i);

    public abstract void grantOppoPermissionByGroupAsUser(PackageParser.Package packageR, String str, String str2, int i, int i2);

    public abstract boolean grantPermissionOppoPolicy(PackageParser.Package packageR, String str, boolean z);

    public abstract void interceptClearUserDataIfNeeded(String str) throws SecurityException;

    public abstract boolean isRuntimePermissionFingerprintNew(int i);

    public abstract boolean onPermissionRevoked(ApplicationInfo applicationInfo, int i);

    public abstract void revokeOppoPermissionByGroup(PackageParser.Package packageR, String str, String str2, int i);

    public abstract void revokeOppoPermissionByGroupAsUser(PackageParser.Package packageR, String str, String str2, int i, int i2);
}
