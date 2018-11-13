package cm.android.mdm.manager;

import android.content.Context;
import android.content.pm.IPackageDeleteObserver.Stub;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import cm.android.mdm.interfaces.IPackageManager;
import cm.android.mdm.interfaces.IPackageManager.PackageDeleteObserver;
import cm.android.mdm.interfaces.IPackageManager.PackageInstallObserver;
import cm.android.mdm.util.CustomizeServiceManager;
import cm.android.mdm.util.MethodSignature;
import java.util.ArrayList;
import java.util.List;

public class PackageManager2 implements IPackageManager {
    private static final int ADD_APP_LIST = 1;
    private static final int BLACK_LIST = 1;
    private static final int DELETE_APP_LIST = 2;
    private static final int NORMAL = 0;
    private static final String TAG = "PackageManager2";
    private static final int WHITE_LIST = 2;
    private Context mContext;
    private PackageManager mPackageManager = null;

    private class PackageDeleteObserver2 extends Stub {
        private boolean mFinished;
        private String mPackageName;
        private int mResult;

        /* synthetic */ PackageDeleteObserver2(PackageManager2 this$0, PackageDeleteObserver2 -this1) {
            this();
        }

        private PackageDeleteObserver2() {
            this.mFinished = false;
            this.mPackageName = "";
        }

        public void packageDeleted(String name, int status) {
            this.mFinished = true;
            this.mResult = status;
            this.mPackageName = name;
        }
    }

    private class PackageInstallObserver2 extends IPackageInstallObserver.Stub {
        private boolean mFinished;
        private String mPackageName;
        private int mResult;

        /* synthetic */ PackageInstallObserver2(PackageManager2 this$0, PackageInstallObserver2 -this1) {
            this();
        }

        private PackageInstallObserver2() {
            this.mFinished = false;
            this.mPackageName = "";
        }

        public void packageInstalled(String name, int status) {
            this.mFinished = true;
            this.mResult = status;
            this.mPackageName = name;
        }
    }

    public PackageManager2(Context context) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
    }

    public void installPackage(Uri packageURI, PackageInstallObserver observer, int flags, String installerPackageName) {
        if (this.mPackageManager != null) {
            PackageInstallObserver2 installObserver = new PackageInstallObserver2(this, null);
            this.mPackageManager.installPackage(packageURI, installObserver, flags, installerPackageName);
            if (observer != null) {
                observer.packageInstalled(installerPackageName, installObserver.mResult);
            }
        }
    }

    public void deletePackage(String packageName, PackageDeleteObserver observer, int flags) {
        if (this.mPackageManager != null) {
            PackageDeleteObserver2 deleteObserver = new PackageDeleteObserver2(this, null);
            this.mPackageManager.deletePackage(packageName, deleteObserver, flags);
            if (observer != null) {
                observer.packageDeleted(packageName, deleteObserver.mResult);
            }
        }
    }

    public void clearApplicationUserData(String packageName) {
        CustomizeServiceManager.clearAppData(packageName);
    }

    public void addDisallowUninstallApps(List<String> packageNames) {
        this.mPackageManager.addDisallowUninstallApps(packageNames);
    }

    public void removeDisallowUninstallApps(List<String> packageNames) {
        if (packageNames != null && packageNames.size() > 0) {
            this.mPackageManager.removeDisallowUninstallApps(packageNames);
        }
    }

    public void removeDisallowUninstallApps() {
        this.mPackageManager.removeDisallowUninstallApps(new ArrayList());
    }

    public List<String> getDisallowUninstallApps() {
        return this.mPackageManager.getDisallowUninstallApps();
    }

    public void setAppRestriction(int pattern) {
        if (pattern == 1) {
            CustomizeServiceManager.setProp("persist.sys.enable_black_list", "true");
            CustomizeServiceManager.setProp("persist.sys.enable_white_list", "false");
        } else if (pattern == 2) {
            CustomizeServiceManager.setProp("persist.sys.enable_black_list", "false");
            CustomizeServiceManager.setProp("persist.sys.enable_white_list", "true");
        } else if (pattern == 0) {
            CustomizeServiceManager.setProp("persist.sys.enable_black_list", "false");
            CustomizeServiceManager.setProp("persist.sys.enable_white_list", "false");
        }
    }

    public void addAppRestriction(int pattern, List<String> pkgs) {
        if (this.mPackageManager == null) {
            return;
        }
        if (pattern == 1) {
            this.mPackageManager.addInstallPackageBlacklist(1, pkgs);
        } else if (pattern == 2) {
            this.mPackageManager.addInstallPackageWhitelist(1, pkgs);
        }
    }

    public void removeAppRestriction(int pattern, List<String> pkgs) {
        if (this.mPackageManager == null) {
            return;
        }
        if (pattern == 1) {
            this.mPackageManager.addInstallPackageBlacklist(2, pkgs);
        } else if (pattern == 2) {
            this.mPackageManager.addInstallPackageWhitelist(2, pkgs);
        }
    }

    public void removeAppRestriction(int pattern) {
        if (this.mPackageManager == null) {
            return;
        }
        if (pattern == 1) {
            this.mPackageManager.addInstallPackageBlacklist(2, null);
        } else if (pattern == 2) {
            this.mPackageManager.addInstallPackageWhitelist(2, null);
        }
    }

    public List<String> getSupportMethods() {
        return MethodSignature.getMethodSignatures(PackageManager2.class);
    }
}
