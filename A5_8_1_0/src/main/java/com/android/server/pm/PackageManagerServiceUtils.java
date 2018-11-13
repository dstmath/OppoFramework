package com.android.server.pm;

import android.app.AppGlobals;
import android.content.Intent;
import android.content.pm.PackageParser.Package;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.system.ErrnoException;
import android.util.ArraySet;
import android.util.Log;
import com.android.server.pm.dex.PackageDexUsage.PackageUseInfo;
import dalvik.system.VMRuntime;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import libcore.io.Libcore;

public class PackageManagerServiceUtils {
    private static final long SEVEN_DAYS_IN_MILLISECONDS = 604800000;

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
            Collections.sort(pkgs, -$Lambda$tZuhGcRRWSq5m9LlSrypurdt-0w.$INST$1);
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
        applyPackageFilter(-$Lambda$s_oh3oeib-Exts1l3lS2Euiarsw.$INST$0, result, remainingPkgs, sortTemp, packageManagerService);
        applyPackageFilter(new -$Lambda$KFbchFEqJgs_hY1HweauKRNA_ds((byte) 2, getPackageNamesForIntent(new Intent("android.intent.action.PRE_BOOT_COMPLETED"), 0)), result, remainingPkgs, sortTemp, packageManagerService);
        applyPackageFilter(new -$Lambda$KFbchFEqJgs_hY1HweauKRNA_ds((byte) 3, packageManagerService.getDexManager()), result, remainingPkgs, sortTemp, packageManagerService);
        if (remainingPkgs.isEmpty() || !packageManagerService.isHistoricalPackageUsageAvailable()) {
            remainingPredicate = -$Lambda$s_oh3oeib-Exts1l3lS2Euiarsw.$INST$2;
        } else {
            if (PackageManagerService.DEBUG_DEXOPT) {
                Log.i("PackageManager", "Looking at historical package use");
            }
            Package lastUsed = (Package) Collections.max(remainingPkgs, -$Lambda$tZuhGcRRWSq5m9LlSrypurdt-0w.$INST$0);
            if (PackageManagerService.DEBUG_DEXOPT) {
                Log.i("PackageManager", "Taking package " + lastUsed.packageName + " as reference in time use");
            }
            long estimatedPreviousSystemUseTime = lastUsed.getLatestForegroundPackageUseTimeInMills();
            if (estimatedPreviousSystemUseTime != 0) {
                remainingPredicate = new -$Lambda$5qSWip3Q3NYNf0S8FNRU2st8ZfA((byte) 1, estimatedPreviousSystemUseTime - 604800000);
            } else {
                remainingPredicate = -$Lambda$s_oh3oeib-Exts1l3lS2Euiarsw.$INST$1;
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

    /* renamed from: lambda$-com_android_server_pm_PackageManagerServiceUtils_7346 */
    static /* synthetic */ boolean m87lambda$-com_android_server_pm_PackageManagerServiceUtils_7346(long cutoffTime, Package pkg) {
        return pkg.getLatestForegroundPackageUseTimeInMills() >= cutoffTime;
    }

    static boolean isUnusedSinceTimeInMillis(long firstInstallTime, long currentTimeInMillis, long thresholdTimeinMillis, PackageUseInfo packageUseInfo, long latestPackageUseTimeInMillis, long latestForegroundPackageUseTimeInMillis) {
        if (currentTimeInMillis - firstInstallTime < thresholdTimeinMillis) {
            return false;
        }
        if (currentTimeInMillis - latestForegroundPackageUseTimeInMillis < thresholdTimeinMillis) {
            return false;
        }
        int isActiveInBackgroundAndUsedByOtherPackages;
        if (currentTimeInMillis - latestPackageUseTimeInMillis < thresholdTimeinMillis) {
            isActiveInBackgroundAndUsedByOtherPackages = packageUseInfo.isAnyCodePathUsedByOtherApps();
        } else {
            isActiveInBackgroundAndUsedByOtherPackages = 0;
        }
        return isActiveInBackgroundAndUsedByOtherPackages ^ 1;
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
        applyPackageFilter(new -$Lambda$KFbchFEqJgs_hY1HweauKRNA_ds((byte) 4, getPackageNamesForPersist()), result, remainingPkgs, new ArrayList(remainingPkgs.size()), packageManagerService);
        if (PackageManagerService.DEBUG_DEXOPT) {
            Log.i("PackageManager", "Persist Packages to be dexopted: " + packagesToString(result));
            Log.i("PackageManager", "Persist Packages skipped from dexopt: " + packagesToString(remainingPkgs));
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x00c4 A:{SYNTHETIC, Splitter: B:27:0x00c4} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0112 A:{SYNTHETIC, Splitter: B:40:0x0112} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static ArraySet<String> getPackageNamesForPersist() {
        IOException e;
        Throwable th;
        String DEFAULT_DATA_APP_NAME_LIST = "/data/engineermode/persistname";
        String COMMON_SOFT_DATA_APP_NAME_LIST = "/data/engineermode/persistname_%s.txt";
        String current_device_data_app_list_path = "/data/engineermode/persistname";
        String common_soft_device = SystemProperties.get("ro.commonsoft.product", "");
        if (!(common_soft_device == null || (common_soft_device.isEmpty() ^ 1) == 0)) {
            current_device_data_app_list_path = String.format("/data/engineermode/persistname_%s.txt", new Object[]{common_soft_device.trim()});
        }
        if (!new File(current_device_data_app_list_path).exists()) {
            Log.i("PackageManager", "common soft but same built in apps");
            current_device_data_app_list_path = "/data/engineermode/persistname";
        }
        ArraySet<String> pkgNames = new ArraySet();
        String line = "";
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
                        } else if (!(line == null || (line.isEmpty() ^ 1) == 0)) {
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
                            } catch (IOException e1) {
                                Log.e("PackageManager", "getPackageNamesForPersist get io close exception :" + e1.getMessage());
                            }
                        }
                        throw th;
                    }
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e12) {
                        Log.e("PackageManager", "getPackageNamesForPersist get io close exception :" + e12.getMessage());
                    }
                }
            } catch (IOException e3) {
                e = e3;
                Log.e("PackageManager", "getPackageNamesForPersist error :" + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Log.e("PackageManager", "getPackageNamesForPersist get io close exception :" + e122.getMessage());
                    }
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

    public static boolean checkISA(String isa) {
        for (String abi : Build.SUPPORTED_ABIS) {
            if (VMRuntime.getInstructionSet(abi).equals(isa)) {
                return true;
            }
        }
        return false;
    }
}
