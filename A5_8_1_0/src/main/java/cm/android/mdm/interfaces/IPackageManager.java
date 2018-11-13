package cm.android.mdm.interfaces;

import android.net.Uri;
import java.util.List;

public interface IPackageManager {

    public static abstract class PackageDeleteObserver {
        public abstract void packageDeleted(String str, int i);
    }

    public static abstract class PackageInstallObserver {
        public abstract void packageInstalled(String str, int i);
    }

    void addAppRestriction(int i, List<String> list);

    void addDisallowUninstallApps(List<String> list);

    void clearApplicationUserData(String str);

    void deletePackage(String str, PackageDeleteObserver packageDeleteObserver, int i);

    List<String> getDisallowUninstallApps();

    List<String> getSupportMethods();

    void installPackage(Uri uri, PackageInstallObserver packageInstallObserver, int i, String str);

    void removeAppRestriction(int i);

    void removeAppRestriction(int i, List<String> list);

    void removeDisallowUninstallApps();

    void removeDisallowUninstallApps(List<String> list);

    void setAppRestriction(int i);
}
