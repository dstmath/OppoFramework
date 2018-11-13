package com.android.server.pm;

import android.app.AppGlobals;
import android.content.Intent;
import android.content.pm.PackageParser.Package;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.system.ErrnoException;
import android.util.ArraySet;
import android.util.Log;
import com.android.server.oppo.IElsaManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import libcore.io.Libcore;

public class PackageManagerServiceUtils {
    private static final long SEVEN_DAYS_IN_MILLISECONDS = 604800000;

    final /* synthetic */ class -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl0 implements Predicate {
        public boolean test(Object arg0) {
            return ((Package) arg0).coreApp;
        }
    }

    final /* synthetic */ class -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl1 implements Predicate {
        private /* synthetic */ ArraySet val$pkgNames;

        public /* synthetic */ -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl1(ArraySet arraySet) {
            this.val$pkgNames = arraySet;
        }

        public boolean test(Object arg0) {
            return this.val$pkgNames.contains(((Package) arg0).packageName);
        }
    }

    final /* synthetic */ class -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl2 implements Predicate {
        public boolean test(Object arg0) {
            return PackageDexOptimizer.isUsedByOtherApps((Package) arg0);
        }
    }

    final /* synthetic */ class -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl3 implements Comparator {
        public int compare(Object arg0, Object arg1) {
            return Long.compare(((Package) arg0).getLatestForegroundPackageUseTimeInMills(), ((Package) arg1).getLatestForegroundPackageUseTimeInMills());
        }
    }

    final /* synthetic */ class -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl4 implements Predicate {
        private /* synthetic */ long val$cutoffTime;

        public /* synthetic */ -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl4(long j) {
            this.val$cutoffTime = j;
        }

        public boolean test(Object arg0) {
            return PackageManagerServiceUtils.m31-com_android_server_pm_PackageManagerServiceUtils_lambda$6(this.val$cutoffTime, (Package) arg0);
        }
    }

    final /* synthetic */ class -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl5 implements Predicate {
        public boolean test(Object arg0) {
            return true;
        }
    }

    final /* synthetic */ class -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl6 implements Predicate {
        public boolean test(Object arg0) {
            return true;
        }
    }

    final /* synthetic */ class -java_util_List_getPersistPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl0 implements Predicate {
        private /* synthetic */ ArraySet val$persistPackages;

        public /* synthetic */ -java_util_List_getPersistPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl0(ArraySet arraySet) {
            this.val$persistPackages = arraySet;
        }

        public boolean test(Object arg0) {
            return this.val$persistPackages.contains(((Package) arg0).packageName);
        }
    }

    final /* synthetic */ class -void_sortPackagesByUsageDate_java_util_List_pkgs_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl0 implements Comparator {
        public int compare(Object arg0, Object arg1) {
            return Long.compare(((Package) arg1).getLatestForegroundPackageUseTimeInMills(), ((Package) arg0).getLatestForegroundPackageUseTimeInMills());
        }
    }

    private static ArraySet<String> getPackageNamesForIntent(Intent intent, int userId) {
        Iterable ris = null;
        try {
            ris = AppGlobals.getPackageManager().queryIntentReceivers(intent, null, 0, userId).getList();
        } catch (RemoteException e) {
        }
        ArraySet<String> pkgNames = new ArraySet();
        if (ris != null) {
            for (ResolveInfo ri : ris) {
                pkgNames.add(ri.activityInfo.packageName);
            }
        }
        return pkgNames;
    }

    public static void sortPackagesByUsageDate(List<Package> pkgs, PackageManagerService packageManagerService) {
        if (packageManagerService.isHistoricalPackageUsageAvailable()) {
            Collections.sort(pkgs, new -void_sortPackagesByUsageDate_java_util_List_pkgs_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl0());
        }
    }

    private static void applyPackageFilter(Predicate<Package> filter, Collection<Package> result, Collection<Package> packages, List<Package> sortTemp, PackageManagerService packageManagerService) {
        for (Package pkg : packages) {
            if (filter.test(pkg)) {
                sortTemp.add(pkg);
            }
        }
        sortPackagesByUsageDate(sortTemp, packageManagerService);
        packages.removeAll(sortTemp);
        for (Package pkg2 : sortTemp) {
            result.add(pkg2);
            Collection<Package> deps = packageManagerService.findSharedNonSystemLibraries(pkg2);
            if (!deps.isEmpty()) {
                deps.removeAll(result);
                result.addAll(deps);
                packages.removeAll(deps);
            }
        }
        sortTemp.clear();
    }

    public static List<Package> getPackagesForDexopt(Collection<Package> packages, PackageManagerService packageManagerService) {
        Predicate<Package> remainingPredicate;
        ArrayList<Package> remainingPkgs = new ArrayList(packages);
        LinkedList<Package> result = new LinkedList();
        ArrayList<Package> sortTemp = new ArrayList(remainingPkgs.size());
        applyPackageFilter(new -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl0(), result, remainingPkgs, sortTemp, packageManagerService);
        applyPackageFilter(new -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl1(getPackageNamesForIntent(new Intent("android.intent.action.PRE_BOOT_COMPLETED"), 0)), result, remainingPkgs, sortTemp, packageManagerService);
        applyPackageFilter(new -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl2(), result, remainingPkgs, sortTemp, packageManagerService);
        if (remainingPkgs.isEmpty() || !packageManagerService.isHistoricalPackageUsageAvailable()) {
            remainingPredicate = new -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl6();
        } else {
            if (PackageManagerService.DEBUG_DEXOPT) {
                Log.i("PackageManager", "Looking at historical package use");
            }
            Package lastUsed = (Package) Collections.max(remainingPkgs, new -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl3());
            if (PackageManagerService.DEBUG_DEXOPT) {
                Log.i("PackageManager", "Taking package " + lastUsed.packageName + " as reference in time use");
            }
            long estimatedPreviousSystemUseTime = lastUsed.getLatestForegroundPackageUseTimeInMills();
            if (estimatedPreviousSystemUseTime != 0) {
                remainingPredicate = new -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl4(estimatedPreviousSystemUseTime - 604800000);
            } else {
                remainingPredicate = new -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl5();
            }
            sortPackagesByUsageDate(remainingPkgs, packageManagerService);
        }
        applyPackageFilter(remainingPredicate, result, remainingPkgs, sortTemp, packageManagerService);
        if (PackageManagerService.DEBUG_DEXOPT) {
            Log.i("PackageManager", "Packages to be dexopted: " + packagesToString(result));
            Log.i("PackageManager", "Packages skipped from dexopt: " + packagesToString(remainingPkgs));
        }
        return result;
    }

    /* renamed from: -com_android_server_pm_PackageManagerServiceUtils_lambda$6 */
    static /* synthetic */ boolean m31-com_android_server_pm_PackageManagerServiceUtils_lambda$6(long cutoffTime, Package pkg) {
        return pkg.getLatestForegroundPackageUseTimeInMills() >= cutoffTime;
    }

    public static String realpath(File path) throws IOException {
        try {
            return Libcore.os.realpath(path.getAbsolutePath());
        } catch (ErrnoException ee) {
            throw ee.rethrowAsIOException();
        }
    }

    public static List<Package> getPersistPackagesForDexopt(Collection<Package> packages, PackageManagerService packageManagerService) {
        ArrayList<Package> remainingPkgs = new ArrayList(packages);
        LinkedList<Package> result = new LinkedList();
        applyPackageFilter(new -java_util_List_getPersistPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl0(getPackageNamesForPersist()), result, remainingPkgs, new ArrayList(remainingPkgs.size()), packageManagerService);
        if (PackageManagerService.DEBUG_DEXOPT) {
            Log.i("PackageManager", "Persist Packages to be dexopted: " + packagesToString(result));
            Log.i("PackageManager", "Persist Packages skipped from dexopt: " + packagesToString(remainingPkgs));
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x00af A:{SYNTHETIC, Splitter: B:26:0x00af} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0110 A:{SYNTHETIC, Splitter: B:40:0x0110} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static ArraySet<String> getPackageNamesForPersist() {
        IOException e;
        Throwable th;
        String DEFAULT_DATA_APP_NAME_LIST = "/data/engineermode/persistname";
        String COMMON_SOFT_DATA_APP_NAME_LIST = "/data/engineermode/persistname_%s.txt";
        String current_device_data_app_list_path = "/data/engineermode/persistname";
        String common_soft_device = SystemProperties.get("ro.commonsoft.product", IElsaManager.EMPTY_PACKAGE);
        if (!(common_soft_device == null || common_soft_device.isEmpty())) {
            current_device_data_app_list_path = String.format("/data/engineermode/persistname_%s.txt", new Object[]{common_soft_device.trim()});
        }
        if (!new File(current_device_data_app_list_path).exists()) {
            Log.i("PackageManager", "common soft but same built in apps");
            current_device_data_app_list_path = "/data/engineermode/persistname";
        }
        ArraySet<String> pkgNames = new ArraySet();
        String line = IElsaManager.EMPTY_PACKAGE;
        BufferedReader reader = null;
        File whiteListFile = new File(current_device_data_app_list_path);
        if (whiteListFile.exists()) {
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(whiteListFile));
                while (true) {
                    try {
                        line = reader2.readLine();
                        if (line == null) {
                            break;
                        } else if (!(line == null || line.isEmpty())) {
                            Log.i("PackageManager", "getPackageNamesForPersist read package name from " + current_device_data_app_list_path + " is " + line);
                            if (!pkgNames.contains(line)) {
                                pkgNames.add(line);
                            }
                        }
                    } catch (IOException e2) {
                        e = e2;
                        reader = reader2;
                        try {
                            Log.e("PackageManager", "getPackageNamesForPersist error :" + e.getMessage());
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e1) {
                                    Log.e("PackageManager", "getPackageNamesForPersist get io close exception :" + e1.getMessage());
                                }
                            }
                            return pkgNames;
                        } catch (Throwable th2) {
                            th = th2;
                            if (reader != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        reader = reader2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e12) {
                                Log.e("PackageManager", "getPackageNamesForPersist get io close exception :" + e12.getMessage());
                            }
                        }
                        throw th;
                    }
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e122) {
                        Log.e("PackageManager", "getPackageNamesForPersist get io close exception :" + e122.getMessage());
                    }
                }
            } catch (IOException e3) {
                e = e3;
                Log.e("PackageManager", "getPackageNamesForPersist error :" + e.getMessage());
                if (reader != null) {
                }
                return pkgNames;
            }
        }
        Log.e("PackageManager", "getPackageNamesForPersist file not exists : " + current_device_data_app_list_path);
        return pkgNames;
    }

    public static String packagesToString(Collection<Package> c) {
        StringBuilder sb = new StringBuilder();
        for (Package pkg : c) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(pkg.packageName);
        }
        return sb.toString();
    }
}
