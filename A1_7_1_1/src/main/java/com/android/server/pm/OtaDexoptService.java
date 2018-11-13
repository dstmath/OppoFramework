package com.android.server.pm;

import android.content.Context;
import android.content.pm.IOtaDexopt.Stub;
import android.content.pm.PackageParser.Package;
import android.os.Environment;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.storage.StorageManager;
import android.util.Log;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.InstallerConnection;
import com.android.internal.os.InstallerConnection.InstallerException;
import com.android.server.pm.PackageDexOptimizer.ForcedUpdatePackageDexOptimizer;
import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OtaDexoptService extends Stub {
    private static final long BULK_DELETE_THRESHOLD = 1073741824;
    private static final boolean DEBUG_DEXOPT = true;
    private static final String[] NO_LIBRARIES = null;
    private static final String TAG = "OTADexopt";
    private long availableSpaceAfterBulkDelete;
    private long availableSpaceAfterDexopt;
    private long availableSpaceBefore;
    private int completeSize;
    private int dexoptCommandCountExecuted;
    private int dexoptCommandCountTotal;
    private int importantPackageCount;
    private final Context mContext;
    private List<String> mDexoptCommands;
    private final PackageManagerService mPackageManagerService;
    private long otaDexoptTimeStart;
    private int otherPackageCount;

    private static class OTADexoptPackageDexOptimizer extends ForcedUpdatePackageDexOptimizer {
        public OTADexoptPackageDexOptimizer(Installer installer, Object installLock, Context context) {
            super(installer, installLock, context, "*otadexopt*");
        }

        protected int adjustDexoptFlags(int dexoptFlags) {
            return dexoptFlags | 64;
        }
    }

    private static class RecordingInstallerConnection extends InstallerConnection {
        public List<String> commands;

        /* synthetic */ RecordingInstallerConnection(RecordingInstallerConnection recordingInstallerConnection) {
            this();
        }

        private RecordingInstallerConnection() {
            this.commands = new ArrayList(1);
        }

        public void setWarnIfHeld(Object warnIfHeld) {
            throw new IllegalStateException("Should not reach here");
        }

        public synchronized String transact(String cmd) {
            this.commands.add(cmd);
            return "0";
        }

        public boolean mergeProfiles(int uid, String pkgName) throws InstallerException {
            throw new IllegalStateException("Should not reach here");
        }

        public boolean dumpProfiles(String gid, String packageName, String codePaths) throws InstallerException {
            throw new IllegalStateException("Should not reach here");
        }

        public void disconnect() {
            throw new IllegalStateException("Should not reach here");
        }

        public void waitForConnection() {
            throw new IllegalStateException("Should not reach here");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.OtaDexoptService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.OtaDexoptService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.OtaDexoptService.<clinit>():void");
    }

    public OtaDexoptService(Context context, PackageManagerService packageManagerService) {
        this.mContext = context;
        this.mPackageManagerService = packageManagerService;
        moveAbArtifacts(packageManagerService.mInstaller);
    }

    public static OtaDexoptService main(Context context, PackageManagerService packageManagerService) {
        OtaDexoptService ota = new OtaDexoptService(context, packageManagerService);
        ServiceManager.addService("otadexopt", ota);
        return ota;
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) throws RemoteException {
        new OtaDexoptShellCommand(this).exec(this, in, out, err, args, resultReceiver);
    }

    public synchronized void prepare() throws RemoteException {
        if (this.mDexoptCommands != null) {
            throw new IllegalStateException("already called prepare()");
        }
        List<Package> important;
        List<Package> others;
        synchronized (this.mPackageManagerService.mPackages) {
            important = PackageManagerServiceUtils.getPackagesForDexopt(this.mPackageManagerService.mPackages.values(), this.mPackageManagerService);
            others = new ArrayList(this.mPackageManagerService.mPackages.values());
            others.removeAll(important);
            this.mDexoptCommands = new ArrayList((this.mPackageManagerService.mPackages.size() * 3) / 2);
        }
        for (Package p : important) {
            int compilationReason;
            if (p.coreApp) {
                compilationReason = 8;
            } else {
                compilationReason = 4;
            }
            this.mDexoptCommands.addAll(generatePackageDexopts(p, compilationReason));
        }
        for (Package p2 : others) {
            if (p2.coreApp) {
                throw new IllegalStateException("Found a core app that's not important");
            }
            this.mDexoptCommands.addAll(generatePackageDexopts(p2, 0));
        }
        this.completeSize = this.mDexoptCommands.size();
        long spaceAvailable = getAvailableSpace();
        if (spaceAvailable < BULK_DELETE_THRESHOLD) {
            Log.i(TAG, "Low on space, deleting oat files in an attempt to free up space: " + PackageManagerServiceUtils.packagesToString(others));
            for (Package pkg : others) {
                deleteOatArtifactsOfPackage(pkg);
            }
        }
        prepareMetricsLogging(important.size(), others.size(), spaceAvailable, getAvailableSpace());
    }

    public synchronized void cleanup() throws RemoteException {
        Log.i(TAG, "Cleaning up OTA Dexopt state.");
        this.mDexoptCommands = null;
        this.availableSpaceAfterDexopt = getAvailableSpace();
        performMetricsLogging();
    }

    public synchronized boolean isDone() throws RemoteException {
        if (this.mDexoptCommands == null) {
            throw new IllegalStateException("done() called before prepare()");
        }
        return this.mDexoptCommands.isEmpty();
    }

    public synchronized float getProgress() throws RemoteException {
        if (this.completeSize == 0) {
            return 1.0f;
        }
        return ((float) (this.completeSize - this.mDexoptCommands.size())) / ((float) this.completeSize);
    }

    public synchronized String nextDexoptCommand() throws RemoteException {
        if (this.mDexoptCommands == null) {
            throw new IllegalStateException("dexoptNextPackage() called before prepare()");
        } else if (this.mDexoptCommands.isEmpty()) {
            return "(all done)";
        } else {
            String next = (String) this.mDexoptCommands.remove(0);
            if (getAvailableSpace() > 0) {
                this.dexoptCommandCountExecuted++;
                return next;
            }
            Log.w(TAG, "Not enough space for OTA dexopt, stopping with " + (this.mDexoptCommands.size() + 1) + " commands left.");
            this.mDexoptCommands.clear();
            return "(no free space)";
        }
    }

    private long getMainLowSpaceThreshold() {
        long lowThreshold = StorageManager.from(this.mContext).getStorageLowBytes(Environment.getDataDirectory());
        if (lowThreshold != 0) {
            return lowThreshold;
        }
        throw new IllegalStateException("Invalid low memory threshold");
    }

    private long getAvailableSpace() {
        return Environment.getDataDirectory().getUsableSpace() - getMainLowSpaceThreshold();
    }

    private static String getOatDir(Package pkg) {
        if (!pkg.canHaveOatDir()) {
            return null;
        }
        File codePath = new File(pkg.codePath);
        if (codePath.isDirectory()) {
            return PackageDexOptimizer.getOatDir(codePath).getAbsolutePath();
        }
        return null;
    }

    private void deleteOatArtifactsOfPackage(Package pkg) {
        String[] instructionSets = InstructionSets.getAppDexInstructionSets(pkg.applicationInfo);
        for (String codePath : pkg.getAllCodePaths()) {
            for (String isa : instructionSets) {
                try {
                    this.mPackageManagerService.mInstaller.deleteOdex(codePath, isa, getOatDir(pkg));
                } catch (InstallerException e) {
                    Log.e(TAG, "Failed deleting oat files for " + codePath, e);
                }
            }
        }
    }

    private synchronized List<String> generatePackageDexopts(Package pkg, int compilationReason) {
        RecordingInstallerConnection collectingConnection;
        collectingConnection = new RecordingInstallerConnection();
        PackageDexOptimizer optimizer = new OTADexoptPackageDexOptimizer(new Installer(this.mContext, collectingConnection), this.mPackageManagerService.mInstallLock, this.mContext);
        String[] libraryDependencies = pkg.usesLibraryFiles;
        if (pkg.isSystemApp()) {
            libraryDependencies = NO_LIBRARIES;
        }
        optimizer.performDexOpt(pkg, libraryDependencies, null, false, PackageManagerServiceCompilerMapping.getCompilerFilterForReason(compilationReason), null);
        return collectingConnection.commands;
    }

    public synchronized void dexoptNextPackage() throws RemoteException {
        throw new UnsupportedOperationException();
    }

    private void moveAbArtifacts(Installer installer) {
        if (this.mDexoptCommands != null) {
            throw new IllegalStateException("Should not be ota-dexopting when trying to move.");
        }
        for (Package pkg : this.mPackageManagerService.getPackages()) {
            if (pkg != null && PackageDexOptimizer.canOptimizePackage(pkg)) {
                if (pkg.codePath == null) {
                    Slog.w(TAG, "Package " + pkg + " can be optimized but has null codePath");
                } else if (!(pkg.codePath.startsWith("/system") || pkg.codePath.startsWith("/vendor"))) {
                    String[] instructionSets = InstructionSets.getAppDexInstructionSets(pkg.applicationInfo);
                    List<String> paths = pkg.getAllCodePathsExcludingResourceOnly();
                    for (String dexCodeInstructionSet : InstructionSets.getDexCodeInstructionSets(instructionSets)) {
                        for (String path : paths) {
                            try {
                                installer.moveAb(path, dexCodeInstructionSet, PackageDexOptimizer.getOatDir(new File(pkg.codePath)).getAbsolutePath());
                            } catch (InstallerException e) {
                            }
                        }
                    }
                }
            }
        }
    }

    private void prepareMetricsLogging(int important, int others, long spaceBegin, long spaceBulk) {
        this.availableSpaceBefore = spaceBegin;
        this.availableSpaceAfterBulkDelete = spaceBulk;
        this.availableSpaceAfterDexopt = 0;
        this.importantPackageCount = important;
        this.otherPackageCount = others;
        this.dexoptCommandCountTotal = this.mDexoptCommands.size();
        this.dexoptCommandCountExecuted = 0;
        this.otaDexoptTimeStart = System.nanoTime();
    }

    private static int inMegabytes(long value) {
        long in_mega_bytes = value / 1048576;
        if (in_mega_bytes <= 2147483647L) {
            return (int) in_mega_bytes;
        }
        Log.w(TAG, "Recording " + in_mega_bytes + "MB of free space, overflowing range");
        return Integer.MAX_VALUE;
    }

    private void performMetricsLogging() {
        long finalTime = System.nanoTime();
        MetricsLogger.histogram(this.mContext, "ota_dexopt_available_space_before_mb", inMegabytes(this.availableSpaceBefore));
        MetricsLogger.histogram(this.mContext, "ota_dexopt_available_space_after_bulk_delete_mb", inMegabytes(this.availableSpaceAfterBulkDelete));
        MetricsLogger.histogram(this.mContext, "ota_dexopt_available_space_after_dexopt_mb", inMegabytes(this.availableSpaceAfterDexopt));
        MetricsLogger.histogram(this.mContext, "ota_dexopt_num_important_packages", this.importantPackageCount);
        MetricsLogger.histogram(this.mContext, "ota_dexopt_num_other_packages", this.otherPackageCount);
        MetricsLogger.histogram(this.mContext, "ota_dexopt_num_commands", this.dexoptCommandCountTotal);
        MetricsLogger.histogram(this.mContext, "ota_dexopt_num_commands_executed", this.dexoptCommandCountExecuted);
        MetricsLogger.histogram(this.mContext, "ota_dexopt_time_s", (int) TimeUnit.NANOSECONDS.toSeconds(finalTime - this.otaDexoptTimeStart));
    }
}
