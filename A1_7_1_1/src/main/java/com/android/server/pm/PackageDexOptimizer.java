package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageParser.Package;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.InstallerConnection.InstallerException;
import com.android.internal.util.IndentingPrintWriter;
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;
import java.util.List;

class PackageDexOptimizer {
    static final int DEX_OPT_FAILED = -1;
    static final int DEX_OPT_PERFORMED = 1;
    static final int DEX_OPT_SKIPPED = 0;
    static final String OAT_DIR_NAME = "oat";
    private static final String TAG = "PackageManager.DexOptimizer";
    private final WakeLock mDexoptWakeLock;
    private final Object mInstallLock;
    private final Installer mInstaller;
    private volatile boolean mSystemReady;

    public static class ForcedUpdatePackageDexOptimizer extends PackageDexOptimizer {
        public ForcedUpdatePackageDexOptimizer(Installer installer, Object installLock, Context context, String wakeLockTag) {
            super(installer, installLock, context, wakeLockTag);
        }

        public ForcedUpdatePackageDexOptimizer(PackageDexOptimizer from) {
            super(from);
        }

        protected int adjustDexoptNeeded(int dexoptNeeded) {
            return 1;
        }
    }

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

    static boolean canOptimizePackage(Package pkg) {
        return (pkg.applicationInfo.flags & 4) != 0;
    }

    int performDexOpt(Package pkg, String[] sharedLibraries, String[] instructionSets, boolean checkProfiles, String targetCompilationFilter, PackageStats packageStats) {
        int performDexOptLI;
        synchronized (this.mInstallLock) {
            boolean useLock = this.mSystemReady;
            if (useLock) {
                this.mDexoptWakeLock.setWorkSource(new WorkSource(pkg.applicationInfo.uid));
                this.mDexoptWakeLock.acquire();
            }
            try {
                performDexOptLI = performDexOptLI(pkg, sharedLibraries, instructionSets, checkProfiles, targetCompilationFilter, packageStats);
                if (useLock) {
                    this.mDexoptWakeLock.release();
                }
            } catch (Throwable th) {
                if (useLock) {
                    this.mDexoptWakeLock.release();
                }
            }
        }
        return performDexOptLI;
    }

    protected int adjustDexoptNeeded(int dexoptNeeded) {
        return dexoptNeeded;
    }

    protected int adjustDexoptFlags(int dexoptFlags) {
        return dexoptFlags;
    }

    void dumpDexoptState(IndentingPrintWriter pw, Package pkg) {
        String[] dexCodeInstructionSets = InstructionSets.getDexCodeInstructionSets(InstructionSets.getAppDexInstructionSets(pkg.applicationInfo));
        List<String> paths = pkg.getAllCodePathsExcludingResourceOnly();
        for (String instructionSet : dexCodeInstructionSets) {
            pw.println("Instruction Set: " + instructionSet);
            pw.increaseIndent();
            for (String path : paths) {
                String status;
                try {
                    status = DexFile.getDexFileStatus(path, instructionSet);
                } catch (IOException ioe) {
                    status = "[Exception]: " + ioe.getMessage();
                }
                pw.println("path: " + path);
                pw.println("status: " + status);
            }
            pw.decreaseIndent();
        }
    }

    private int performDexOptLI(Package pkg, String[] sharedLibraries, String[] targetInstructionSets, boolean checkProfiles, String targetCompilerFilter, PackageStats packageStats) {
        String[] instructionSets = targetInstructionSets != null ? targetInstructionSets : InstructionSets.getAppDexInstructionSets(pkg.applicationInfo);
        if (!canOptimizePackage(pkg)) {
            return 0;
        }
        List<String> paths = pkg.getAllCodePathsExcludingResourceOnly();
        int sharedGid = UserHandle.getSharedAppGid(pkg.applicationInfo.uid);
        boolean isProfileGuidedFilter = DexFile.isProfileGuidedCompilerFilter(targetCompilerFilter);
        if (isProfileGuidedFilter && isUsedByOtherApps(pkg)) {
            checkProfiles = false;
            targetCompilerFilter = PackageManagerServiceCompilerMapping.getNonProfileGuidedCompilerFilter(targetCompilerFilter);
            if (DexFile.isProfileGuidedCompilerFilter(targetCompilerFilter)) {
                throw new IllegalStateException(targetCompilerFilter);
            }
            isProfileGuidedFilter = false;
        }
        boolean newProfile = false;
        if (checkProfiles && isProfileGuidedFilter) {
            try {
                newProfile = this.mInstaller.mergeProfiles(sharedGid, pkg.packageName);
            } catch (InstallerException e) {
                Slog.w(TAG, "Failed to merge profiles", e);
            }
        }
        boolean vmSafeMode = (pkg.applicationInfo.flags & 16384) != 0;
        boolean debuggable = (pkg.applicationInfo.flags & 2) != 0;
        boolean performedDexOpt = false;
        boolean successfulDexOpt = true;
        String[] dexCodeInstructionSets = InstructionSets.getDexCodeInstructionSets(instructionSets);
        int i = 0;
        int length = dexCodeInstructionSets.length;
        while (true) {
            int i2 = i;
            if (i2 < length) {
                String dexCodeInstructionSet = dexCodeInstructionSets[i2];
                for (String path : paths) {
                    try {
                        String dexoptType;
                        int dexoptNeeded = adjustDexoptNeeded(DexFile.getDexOptNeeded(path, dexCodeInstructionSet, targetCompilerFilter, newProfile));
                        if (PackageManagerService.DEBUG_DEXOPT) {
                            Log.i(TAG, "DexoptNeeded for " + path + "@" + targetCompilerFilter + " is " + dexoptNeeded);
                        }
                        String oatDir = null;
                        switch (dexoptNeeded) {
                            case 0:
                                break;
                            case 1:
                                dexoptType = "dex2oat";
                                oatDir = createOatDirIfSupported(pkg, dexCodeInstructionSet);
                                break;
                            case 2:
                                dexoptType = "patchoat";
                                break;
                            case 3:
                                dexoptType = "self patchoat";
                                break;
                            default:
                                throw new IllegalStateException("Invalid dexopt:" + dexoptNeeded);
                        }
                        String sharedLibrariesPath = null;
                        if (!(sharedLibraries == null || sharedLibraries.length == 0)) {
                            StringBuilder sb = new StringBuilder();
                            for (String lib : sharedLibraries) {
                                if (sb.length() != 0) {
                                    sb.append(":");
                                }
                                sb.append(lib);
                            }
                            sharedLibrariesPath = sb.toString();
                        }
                        Log.i(TAG, "Running dexopt (" + dexoptType + ") on: " + path + " pkg=" + pkg.applicationInfo.packageName + " isa=" + dexCodeInstructionSet + " vmSafeMode=" + vmSafeMode + " debuggable=" + debuggable + " target-filter=" + targetCompilerFilter + " oatDir = " + oatDir + " sharedLibraries=" + sharedLibrariesPath);
                        boolean isPublic = (pkg.isForwardLocked() || isProfileGuidedFilter) ? false : true;
                        int dexFlags = adjustDexoptFlags((((debuggable ? 8 : 0) | ((isPublic ? 2 : 0) | (vmSafeMode ? 4 : 0))) | (isProfileGuidedFilter ? 32 : 0)) | 16);
                        try {
                            long startTime = System.currentTimeMillis();
                            this.mInstaller.dexopt(path, sharedGid, pkg.packageName, dexCodeInstructionSet, dexoptNeeded, oatDir, dexFlags, targetCompilerFilter, pkg.volumeUuid, sharedLibrariesPath);
                            performedDexOpt = true;
                            if (packageStats != null) {
                                packageStats.setCompileTime(path, (long) ((int) (System.currentTimeMillis() - startTime)));
                            }
                        } catch (InstallerException e2) {
                            Slog.w(TAG, "Failed to dexopt", e2);
                            successfulDexOpt = false;
                        }
                    } catch (Throwable ioe) {
                        Slog.w(TAG, "IOException reading apk: " + path, ioe);
                        return -1;
                    }
                }
                i = i2 + 1;
            } else if (!successfulDexOpt) {
                return -1;
            } else {
                return performedDexOpt ? 1 : 0;
            }
        }
    }

    private String createOatDirIfSupported(Package pkg, String dexInstructionSet) {
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
        } catch (InstallerException e) {
            Slog.w(TAG, "Failed to create oat dir", e);
            return null;
        }
    }

    static File getOatDir(File codePath) {
        return new File(codePath, OAT_DIR_NAME);
    }

    void systemReady() {
        this.mSystemReady = true;
    }

    public static boolean isUsedByOtherApps(Package pkg) {
        if (pkg.isForwardLocked()) {
            return false;
        }
        for (String apkPath : pkg.getAllCodePathsExcludingResourceOnly()) {
            try {
                String useMarker = PackageManagerServiceUtils.realpath(new File(apkPath)).replace('/', '@');
                int[] currentUserIds = UserManagerService.getInstance().getUserIds();
                for (int dataProfilesDeForeignDexDirectory : currentUserIds) {
                    if (new File(Environment.getDataProfilesDeForeignDexDirectory(dataProfilesDeForeignDexDirectory), useMarker).exists()) {
                        return true;
                    }
                }
                continue;
            } catch (IOException e) {
                Slog.w(TAG, "Failed to get canonical path", e);
            }
        }
        return false;
    }
}
