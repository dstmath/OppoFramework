package com.android.server.pm;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.content.pm.SharedLibraryInfo;
import android.content.pm.dex.ArtManager;
import android.content.pm.dex.DexMetadataHelper;
import android.os.FileUtils;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.pm.CompilerStats;
import com.android.server.pm.Installer;
import com.android.server.pm.dex.ArtManagerService;
import com.android.server.pm.dex.DexManager;
import com.android.server.pm.dex.DexoptOptions;
import com.android.server.pm.dex.DexoptUtils;
import com.android.server.pm.dex.PackageDexUsage;
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PackageDexOptimizer {
    public static final int DEX_OPT_FAILED = -1;
    public static final int DEX_OPT_PERFORMED = 1;
    public static final int DEX_OPT_SKIPPED = 0;
    static final String OAT_DIR_NAME = "oat";
    private static final String TAG = "PackageManager.DexOptimizer";
    private static final long WAKELOCK_TIMEOUT_MS = 660000;
    @GuardedBy({"mInstallLock"})
    private final PowerManager.WakeLock mDexoptWakeLock;
    private final Object mInstallLock;
    @GuardedBy({"mInstallLock"})
    private final Installer mInstaller;
    private volatile boolean mSystemReady;

    PackageDexOptimizer(Installer installer, Object installLock, Context context, String wakeLockTag) {
        this.mInstaller = installer;
        this.mInstallLock = installLock;
        this.mDexoptWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, wakeLockTag);
    }

    protected PackageDexOptimizer(PackageDexOptimizer from) {
        this.mInstaller = from.mInstaller;
        this.mInstallLock = from.mInstallLock;
        this.mDexoptWakeLock = from.mDexoptWakeLock;
        this.mSystemReady = from.mSystemReady;
    }

    static boolean canOptimizePackage(PackageParser.Package pkg) {
        if ((pkg.applicationInfo.flags & 4) == 0) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public int performDexOpt(PackageParser.Package pkg, String[] instructionSets, CompilerStats.PackageStats packageStats, PackageDexUsage.PackageUseInfo packageUseInfo, DexoptOptions options) {
        int performDexOptLI;
        if (pkg.applicationInfo.uid == -1) {
            throw new IllegalArgumentException("Dexopt for " + pkg.packageName + " has invalid uid.");
        } else if (!canOptimizePackage(pkg)) {
            return 0;
        } else {
            synchronized (this.mInstallLock) {
                long acquireTime = acquireWakeLockLI(pkg.applicationInfo.uid);
                try {
                    performDexOptLI = performDexOptLI(pkg, instructionSets, packageStats, packageUseInfo, options);
                } finally {
                    releaseWakeLockLI(acquireTime);
                }
            }
            return performDexOptLI;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:66:0x0182  */
    @GuardedBy({"mInstallLock"})
    private int performDexOptLI(PackageParser.Package pkg, String[] targetInstructionSets, CompilerStats.PackageStats packageStats, PackageDexUsage.PackageUseInfo packageUseInfo, DexoptOptions options) {
        int sharedGid;
        List<SharedLibraryInfo> sharedLibraries;
        List<String> paths;
        int i;
        int sharedGid2;
        boolean[] pathsWithCode;
        String[] dexCodeInstructionSets;
        String[] classLoaderContexts;
        char c;
        String dexMetadataPath;
        boolean isUsedByOtherApps;
        int length;
        int newResult;
        PackageDexOptimizer packageDexOptimizer = this;
        PackageParser.Package packageR = pkg;
        List<SharedLibraryInfo> sharedLibraries2 = packageR.usesLibraryInfos;
        String[] dexCodeInstructionSets2 = InstructionSets.getDexCodeInstructionSets(targetInstructionSets != null ? targetInstructionSets : InstructionSets.getAppDexInstructionSets(packageR.applicationInfo));
        List<String> paths2 = pkg.getAllCodePaths();
        int sharedGid3 = UserHandle.getSharedAppGid(packageR.applicationInfo.uid);
        char c2 = 65535;
        if (sharedGid3 == -1) {
            Slog.wtf(TAG, "Well this is awkward; package " + packageR.applicationInfo.name + " had UID " + packageR.applicationInfo.uid, new Throwable());
            sharedGid = 9999;
        } else {
            sharedGid = sharedGid3;
        }
        boolean[] pathsWithCode2 = new boolean[paths2.size()];
        pathsWithCode2[0] = (packageR.applicationInfo.flags & 4) != 0;
        for (int i2 = 1; i2 < paths2.size(); i2++) {
            pathsWithCode2[i2] = (packageR.splitFlags[i2 + -1] & 4) != 0;
        }
        String[] classLoaderContexts2 = DexoptUtils.getClassLoaderContexts(packageR.applicationInfo, sharedLibraries2, pathsWithCode2);
        if (paths2.size() != classLoaderContexts2.length) {
            String[] splitCodePaths = packageR.applicationInfo.getSplitCodePaths();
            StringBuilder sb = new StringBuilder();
            sb.append("Inconsistent information between PackageParser.Package and its ApplicationInfo. pkg.getAllCodePaths=");
            sb.append(paths2);
            sb.append(" pkg.applicationInfo.getBaseCodePath=");
            sb.append(packageR.applicationInfo.getBaseCodePath());
            sb.append(" pkg.applicationInfo.getSplitCodePaths=");
            sb.append(splitCodePaths == null ? "null" : Arrays.toString(splitCodePaths));
            throw new IllegalStateException(sb.toString());
        }
        int result = 0;
        int i3 = 0;
        while (i3 < paths2.size()) {
            if (pathsWithCode2[i3]) {
                if (classLoaderContexts2[i3] != null) {
                    String path = paths2.get(i3);
                    if (options.getSplitName() == null || options.getSplitName().equals(new File(path).getName())) {
                        String str = null;
                        String profileName = ArtManager.getProfileName(i3 == 0 ? null : packageR.splitNames[i3 - 1]);
                        if (options.isDexoptInstallWithDexMetadata()) {
                            File dexMetadataFile = DexMetadataHelper.findDexMetadataForFile(new File(path));
                            if (dexMetadataFile != null) {
                                str = dexMetadataFile.getAbsolutePath();
                            }
                            dexMetadataPath = str;
                        } else {
                            dexMetadataPath = null;
                        }
                        if (!options.isDexoptAsSharedLibrary()) {
                            if (!packageUseInfo.isUsedByOtherApps(path)) {
                                isUsedByOtherApps = false;
                                String compilerFilter = packageDexOptimizer.getRealCompilerFilter(packageR.applicationInfo, options.getCompilerFilter(), isUsedByOtherApps);
                                boolean profileUpdated = !options.isCheckForProfileUpdates() && packageDexOptimizer.isProfileUpdated(packageR, sharedGid, profileName, compilerFilter);
                                int dexoptFlags = packageDexOptimizer.getDexFlags(packageR, compilerFilter, options);
                                length = dexCodeInstructionSets2.length;
                                int result2 = result;
                                newResult = 0;
                                while (newResult < length) {
                                    int newResult2 = dexOptPath(pkg, path, dexCodeInstructionSets2[newResult], compilerFilter, profileUpdated, classLoaderContexts2[i3], dexoptFlags, sharedGid, packageStats, options.isDowngrade(), profileName, dexMetadataPath, options.getCompilationReason());
                                    if (!(result2 == -1 || newResult2 == 0)) {
                                        result2 = newResult2;
                                    }
                                    newResult++;
                                    profileUpdated = profileUpdated;
                                    compilerFilter = compilerFilter;
                                    classLoaderContexts2 = classLoaderContexts2;
                                    dexCodeInstructionSets2 = dexCodeInstructionSets2;
                                    isUsedByOtherApps = isUsedByOtherApps;
                                    profileName = profileName;
                                    path = path;
                                    length = length;
                                    pathsWithCode2 = pathsWithCode2;
                                    sharedGid = sharedGid;
                                    i3 = i3;
                                    paths2 = paths2;
                                    sharedLibraries2 = sharedLibraries2;
                                }
                                i = i3;
                                classLoaderContexts = classLoaderContexts2;
                                pathsWithCode = pathsWithCode2;
                                sharedGid2 = sharedGid;
                                paths = paths2;
                                dexCodeInstructionSets = dexCodeInstructionSets2;
                                sharedLibraries = sharedLibraries2;
                                c = 65535;
                                result = result2;
                                i3 = i + 1;
                                packageR = pkg;
                                c2 = c;
                                classLoaderContexts2 = classLoaderContexts;
                                dexCodeInstructionSets2 = dexCodeInstructionSets;
                                pathsWithCode2 = pathsWithCode;
                                sharedGid = sharedGid2;
                                paths2 = paths;
                                sharedLibraries2 = sharedLibraries;
                                packageDexOptimizer = this;
                            }
                        }
                        isUsedByOtherApps = true;
                        String compilerFilter2 = packageDexOptimizer.getRealCompilerFilter(packageR.applicationInfo, options.getCompilerFilter(), isUsedByOtherApps);
                        boolean profileUpdated2 = !options.isCheckForProfileUpdates() && packageDexOptimizer.isProfileUpdated(packageR, sharedGid, profileName, compilerFilter2);
                        int dexoptFlags2 = packageDexOptimizer.getDexFlags(packageR, compilerFilter2, options);
                        length = dexCodeInstructionSets2.length;
                        int result22 = result;
                        newResult = 0;
                        while (newResult < length) {
                        }
                        i = i3;
                        classLoaderContexts = classLoaderContexts2;
                        pathsWithCode = pathsWithCode2;
                        sharedGid2 = sharedGid;
                        paths = paths2;
                        dexCodeInstructionSets = dexCodeInstructionSets2;
                        sharedLibraries = sharedLibraries2;
                        c = 65535;
                        result = result22;
                        i3 = i + 1;
                        packageR = pkg;
                        c2 = c;
                        classLoaderContexts2 = classLoaderContexts;
                        dexCodeInstructionSets2 = dexCodeInstructionSets;
                        pathsWithCode2 = pathsWithCode;
                        sharedGid = sharedGid2;
                        paths2 = paths;
                        sharedLibraries2 = sharedLibraries;
                        packageDexOptimizer = this;
                    }
                } else {
                    throw new IllegalStateException("Inconsistent information in the package structure. A split is marked to contain code but has no dependency listed. Index=" + i3 + " path=" + paths2.get(i3));
                }
            }
            i = i3;
            classLoaderContexts = classLoaderContexts2;
            pathsWithCode = pathsWithCode2;
            sharedGid2 = sharedGid;
            c = c2;
            paths = paths2;
            dexCodeInstructionSets = dexCodeInstructionSets2;
            sharedLibraries = sharedLibraries2;
            i3 = i + 1;
            packageR = pkg;
            c2 = c;
            classLoaderContexts2 = classLoaderContexts;
            dexCodeInstructionSets2 = dexCodeInstructionSets;
            pathsWithCode2 = pathsWithCode;
            sharedGid = sharedGid2;
            paths2 = paths;
            sharedLibraries2 = sharedLibraries;
            packageDexOptimizer = this;
        }
        return result;
    }

    @GuardedBy({"mInstallLock"})
    private int dexOptPath(PackageParser.Package pkg, String path, String isa, String compilerFilter, boolean profileUpdated, String classLoaderContext, int dexoptFlags, int uid, CompilerStats.PackageStats packageStats, boolean downgrade, String profileName, String dexMetadataPath, int compilationReason) {
        int dexoptNeeded = getDexoptNeeded(path, isa, compilerFilter, classLoaderContext, profileUpdated, downgrade);
        if (Math.abs(dexoptNeeded) == 0) {
            return 0;
        }
        if (ActivityThread.inCptWhiteList((int) CompatibilityHelper.DEX_OPT_SKIP_POLICY, pkg.packageName + "," + compilationReason)) {
            return 0;
        }
        String oatDir = createOatDirIfSupported(pkg, isa);
        Log.i(TAG, "Running dexopt (dexoptNeeded=" + dexoptNeeded + ") on: " + path + " pkg=" + pkg.applicationInfo.packageName + " isa=" + isa + " dexoptFlags=" + printDexoptFlags(dexoptFlags) + " targetFilter=" + compilerFilter + " oatDir=" + oatDir + " classLoaderContext=" + classLoaderContext);
        try {
            long startTime = System.currentTimeMillis();
            try {
                this.mInstaller.dexopt(path, uid, pkg.packageName, isa, dexoptNeeded, oatDir, dexoptFlags, compilerFilter, pkg.volumeUuid, classLoaderContext, pkg.applicationInfo.seInfo, false, pkg.applicationInfo.targetSdkVersion, profileName, dexMetadataPath, getAugmentedReasonName(compilationReason, dexMetadataPath != null));
                if (packageStats != null) {
                    packageStats.setCompileTime(path, (long) ((int) (System.currentTimeMillis() - startTime)));
                }
                return 1;
            } catch (Installer.InstallerException e) {
                e = e;
                Slog.w(TAG, "Failed to dexopt", e);
                return -1;
            }
        } catch (Installer.InstallerException e2) {
            e = e2;
            Slog.w(TAG, "Failed to dexopt", e);
            return -1;
        }
    }

    private String getAugmentedReasonName(int compilationReason, boolean useDexMetadata) {
        String annotation = useDexMetadata ? ArtManagerService.DEXOPT_REASON_WITH_DEX_METADATA_ANNOTATION : "";
        return PackageManagerServiceCompilerMapping.getReasonName(compilationReason) + annotation;
    }

    public int dexOptSecondaryDexPath(ApplicationInfo info, String path, PackageDexUsage.DexUseInfo dexUseInfo, DexoptOptions options) {
        int dexOptSecondaryDexPathLI;
        if (info.uid != -1) {
            synchronized (this.mInstallLock) {
                long acquireTime = acquireWakeLockLI(info.uid);
                try {
                    dexOptSecondaryDexPathLI = dexOptSecondaryDexPathLI(info, path, dexUseInfo, options);
                } finally {
                    releaseWakeLockLI(acquireTime);
                }
            }
            return dexOptSecondaryDexPathLI;
        }
        throw new IllegalArgumentException("Dexopt for path " + path + " has invalid uid.");
    }

    @GuardedBy({"mInstallLock"})
    private long acquireWakeLockLI(int uid) {
        if (!this.mSystemReady) {
            return -1;
        }
        this.mDexoptWakeLock.setWorkSource(new WorkSource(uid));
        this.mDexoptWakeLock.acquire(WAKELOCK_TIMEOUT_MS);
        return SystemClock.elapsedRealtime();
    }

    @GuardedBy({"mInstallLock"})
    private void releaseWakeLockLI(long acquireTime) {
        if (acquireTime >= 0) {
            try {
                if (this.mDexoptWakeLock.isHeld()) {
                    this.mDexoptWakeLock.release();
                }
                long duration = SystemClock.elapsedRealtime() - acquireTime;
                if (duration >= WAKELOCK_TIMEOUT_MS) {
                    Slog.wtf(TAG, "WakeLock " + this.mDexoptWakeLock.getTag() + " time out. Operation took " + duration + " ms. Thread: " + Thread.currentThread().getName());
                }
            } catch (Exception e) {
                Slog.wtf(TAG, "Error while releasing " + this.mDexoptWakeLock.getTag() + " lock", e);
            }
        }
    }

    @GuardedBy({"mInstallLock"})
    private int dexOptSecondaryDexPathLI(ApplicationInfo info, String path, PackageDexUsage.DexUseInfo dexUseInfo, DexoptOptions options) {
        int dexoptFlags;
        String compilerFilter;
        String classLoaderContext;
        String str;
        String str2;
        if (options.isDexoptOnlySharedDex() && !dexUseInfo.isUsedByOtherApps()) {
            return 0;
        }
        String compilerFilter2 = getRealCompilerFilter(info, options.getCompilerFilter(), dexUseInfo.isUsedByOtherApps());
        int dexoptFlags2 = getDexFlags(info, compilerFilter2, options) | 32;
        String str3 = info.deviceProtectedDataDir;
        String str4 = TAG;
        if (str3 == null || !FileUtils.contains(info.deviceProtectedDataDir, path)) {
            if (info.credentialProtectedDataDir == null) {
                str2 = str4;
            } else if (FileUtils.contains(info.credentialProtectedDataDir, path)) {
                dexoptFlags = dexoptFlags2 | 128;
            } else {
                str2 = str4;
            }
            Slog.e(str2, "Could not infer CE/DE storage for package " + info.packageName);
            return -1;
        }
        dexoptFlags = dexoptFlags2 | 256;
        if (dexUseInfo.isUnknownClassLoaderContext() || dexUseInfo.isVariableClassLoaderContext()) {
            compilerFilter = "extract";
            classLoaderContext = null;
        } else {
            compilerFilter = compilerFilter2;
            classLoaderContext = dexUseInfo.getClassLoaderContext();
        }
        int reason = options.getCompilationReason();
        Log.d(str4, "Running dexopt on: " + path + " pkg=" + info.packageName + " isa=" + dexUseInfo.getLoaderIsas() + " reason=" + PackageManagerServiceCompilerMapping.getReasonName(reason) + " dexoptFlags=" + printDexoptFlags(dexoptFlags) + " target-filter=" + compilerFilter + " class-loader-context=" + classLoaderContext);
        try {
            for (String isa : dexUseInfo.getLoaderIsas()) {
                str = str4;
                try {
                    this.mInstaller.dexopt(path, info.uid, info.packageName, isa, 0, null, dexoptFlags, compilerFilter, info.volumeUuid, classLoaderContext, info.seInfo, options.isDowngrade(), info.targetSdkVersion, null, null, PackageManagerServiceCompilerMapping.getReasonName(reason));
                    classLoaderContext = classLoaderContext;
                    compilerFilter = compilerFilter;
                    dexoptFlags = dexoptFlags;
                    str4 = str;
                } catch (Installer.InstallerException e) {
                    e = e;
                    Slog.w(str, "Failed to dexopt", e);
                    return -1;
                }
            }
            return 1;
        } catch (Installer.InstallerException e2) {
            e = e2;
            str = str4;
            Slog.w(str, "Failed to dexopt", e);
            return -1;
        }
    }

    /* access modifiers changed from: protected */
    public int adjustDexoptNeeded(int dexoptNeeded) {
        return dexoptNeeded;
    }

    /* access modifiers changed from: protected */
    public int adjustDexoptFlags(int dexoptFlags) {
        return dexoptFlags;
    }

    /* access modifiers changed from: package-private */
    public void dumpDexoptState(IndentingPrintWriter pw, PackageParser.Package pkg, PackageDexUsage.PackageUseInfo useInfo) {
        String[] dexCodeInstructionSets = InstructionSets.getDexCodeInstructionSets(InstructionSets.getAppDexInstructionSets(pkg.applicationInfo));
        for (String path : pkg.getAllCodePathsExcludingResourceOnly()) {
            pw.println("path: " + path);
            pw.increaseIndent();
            int length = dexCodeInstructionSets.length;
            for (int i = 0; i < length; i++) {
                String isa = dexCodeInstructionSets[i];
                try {
                    DexFile.OptimizationInfo info = DexFile.getDexFileOptimizationInfo(path, isa);
                    pw.println(isa + ": [status=" + info.getStatus() + "] [reason=" + info.getReason() + "]");
                } catch (IOException ioe) {
                    pw.println(isa + ": [Exception]: " + ioe.getMessage());
                }
            }
            if (useInfo.isUsedByOtherApps(path)) {
                pw.println("used by other apps: " + useInfo.getLoadingPackages(path));
            }
            Map<String, PackageDexUsage.DexUseInfo> dexUseInfoMap = useInfo.getDexUseInfoMap();
            if (!dexUseInfoMap.isEmpty()) {
                pw.println("known secondary dex files:");
                pw.increaseIndent();
                for (Map.Entry<String, PackageDexUsage.DexUseInfo> e : dexUseInfoMap.entrySet()) {
                    PackageDexUsage.DexUseInfo dexUseInfo = e.getValue();
                    pw.println(e.getKey());
                    pw.increaseIndent();
                    pw.println("class loader context: " + dexUseInfo.getClassLoaderContext());
                    if (dexUseInfo.isUsedByOtherApps()) {
                        pw.println("used by other apps: " + dexUseInfo.getLoadingPackages());
                    }
                    pw.decreaseIndent();
                }
                pw.decreaseIndent();
            }
            pw.decreaseIndent();
        }
    }

    private String getRealCompilerFilter(ApplicationInfo info, String targetCompilerFilter, boolean isUsedByOtherApps) {
        if (info.isEmbeddedDexUsed()) {
            return "verify";
        }
        if (info.isPrivilegedApp() && DexManager.isPackageSelectedToRunOob(info.packageName)) {
            return "verify";
        }
        if (((info.flags & 16384) == 0 && (info.flags & 2) == 0) ? false : true) {
            return DexFile.getSafeModeCompilerFilter(targetCompilerFilter);
        }
        if (!DexFile.isProfileGuidedCompilerFilter(targetCompilerFilter) || !isUsedByOtherApps) {
            return targetCompilerFilter;
        }
        return PackageManagerServiceCompilerMapping.getCompilerFilterForReason(6);
    }

    private int getDexFlags(PackageParser.Package pkg, String compilerFilter, DexoptOptions options) {
        return getDexFlags(pkg.applicationInfo, compilerFilter, options);
    }

    private boolean isAppImageEnabled() {
        return SystemProperties.get("dalvik.vm.appimageformat", "").length() > 0;
    }

    private int getDexFlags(ApplicationInfo info, String compilerFilter, DexoptOptions options) {
        int hiddenApiFlag;
        boolean generateAppImage = true;
        int i = 0;
        boolean debuggable = (info.flags & 2) != 0;
        boolean isProfileGuidedFilter = DexFile.isProfileGuidedCompilerFilter(compilerFilter);
        boolean isPublic = !isProfileGuidedFilter || options.isDexoptInstallWithDexMetadata();
        int profileFlag = isProfileGuidedFilter ? 16 : 0;
        if (info.getHiddenApiEnforcementPolicy() == 0) {
            hiddenApiFlag = 0;
        } else {
            hiddenApiFlag = 1024;
        }
        int compilationReason = options.getCompilationReason();
        boolean generateCompactDex = true;
        int i2 = 2;
        if (compilationReason == 0 || compilationReason == 1 || compilationReason == 2) {
            generateCompactDex = false;
        }
        if (!isProfileGuidedFilter || ((info.splitDependencies != null && info.requestsIsolatedSplitLoading()) || !isAppImageEnabled())) {
            generateAppImage = false;
        }
        if (!isPublic) {
            i2 = 0;
        }
        int i3 = i2 | (debuggable ? 4 : 0) | profileFlag | (options.isBootComplete() ? 8 : 0) | (options.isDexoptIdleBackgroundJob() ? 512 : 0) | (generateCompactDex ? 2048 : 0);
        if (generateAppImage) {
            i = 4096;
        }
        return adjustDexoptFlags(i | i3 | hiddenApiFlag);
    }

    private int getDexoptNeeded(String path, String isa, String compilerFilter, String classLoaderContext, boolean newProfile, boolean downgrade) {
        try {
            return adjustDexoptNeeded(DexFile.getDexOptNeeded(path, isa, compilerFilter, classLoaderContext, newProfile, downgrade));
        } catch (IOException ioe) {
            Slog.w(TAG, "IOException reading apk: " + path, ioe);
            return -1;
        }
    }

    private boolean isProfileUpdated(PackageParser.Package pkg, int uid, String profileName, String compilerFilter) {
        if (!DexFile.isProfileGuidedCompilerFilter(compilerFilter)) {
            return false;
        }
        try {
            return this.mInstaller.mergeProfiles(uid, pkg.packageName, profileName);
        } catch (Installer.InstallerException e) {
            Slog.w(TAG, "Failed to merge profiles", e);
            return false;
        }
    }

    private String createOatDirIfSupported(PackageParser.Package pkg, String dexInstructionSet) {
        if (!pkg.canHaveOatDir()) {
            return null;
        }
        File codePath = new File(pkg.codePath);
        if (!codePath.isDirectory()) {
            return null;
        }
        File oatDir = getOatDir(codePath);
        try {
            this.mInstaller.createOatDir(oatDir.getAbsolutePath(), dexInstructionSet);
            return oatDir.getAbsolutePath();
        } catch (Installer.InstallerException e) {
            Slog.w(TAG, "Failed to create oat dir", e);
            return null;
        }
    }

    static File getOatDir(File codePath) {
        return new File(codePath, OAT_DIR_NAME);
    }

    /* access modifiers changed from: package-private */
    public void systemReady() {
        this.mSystemReady = true;
    }

    private String printDexoptFlags(int flags) {
        ArrayList<String> flagsList = new ArrayList<>();
        if ((flags & 8) == 8) {
            flagsList.add("boot_complete");
        }
        if ((flags & 4) == 4) {
            flagsList.add("debuggable");
        }
        if ((flags & 16) == 16) {
            flagsList.add("profile_guided");
        }
        if ((flags & 2) == 2) {
            flagsList.add("public");
        }
        if ((flags & 32) == 32) {
            flagsList.add("secondary");
        }
        if ((flags & 64) == 64) {
            flagsList.add("force");
        }
        if ((flags & 128) == 128) {
            flagsList.add("storage_ce");
        }
        if ((flags & 256) == 256) {
            flagsList.add("storage_de");
        }
        if ((flags & 512) == 512) {
            flagsList.add("idle_background_job");
        }
        if ((flags & 1024) == 1024) {
            flagsList.add("enable_hidden_api_checks");
        }
        return String.join(",", flagsList);
    }

    public static class ForcedUpdatePackageDexOptimizer extends PackageDexOptimizer {
        public ForcedUpdatePackageDexOptimizer(Installer installer, Object installLock, Context context, String wakeLockTag) {
            super(installer, installLock, context, wakeLockTag);
        }

        public ForcedUpdatePackageDexOptimizer(PackageDexOptimizer from) {
            super(from);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.pm.PackageDexOptimizer
        public int adjustDexoptNeeded(int dexoptNeeded) {
            if (dexoptNeeded == 0) {
                return -3;
            }
            return dexoptNeeded;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.pm.PackageDexOptimizer
        public int adjustDexoptFlags(int flags) {
            return flags | 64;
        }
    }
}
