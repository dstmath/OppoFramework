package com.android.server.pm;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.IIntentSender.Stub;
import android.content.Intent;
import android.content.Intent.CommandOptionHandler;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageManager;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller.Session;
import android.content.pm.PackageInstaller.SessionInfo;
import android.content.pm.PackageInstaller.SessionParams;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.PackageLite;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ShellCommand;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.PrintWriterPrinter;
import com.android.internal.content.PackageHelper;
import com.android.internal.util.SizedInputStream;
import com.android.server.oppo.IElsaManager;
import com.android.server.secrecy.policy.DecryptTool;
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import libcore.io.IoUtils;
import oppo.content.res.OppoFontUtils;

class PackageManagerShellCommand extends ShellCommand {
    boolean mBrief;
    boolean mComponents;
    final IPackageManager mInterface;
    private final WeakHashMap<String, Resources> mResourceCache = new WeakHashMap();
    int mTargetUser;

    private static class InstallParams {
        String installerPackageName;
        SessionParams sessionParams;
        int userId;

        /* synthetic */ InstallParams(InstallParams installParams) {
            this();
        }

        private InstallParams() {
            this.userId = -1;
        }
    }

    private static class LocalIntentReceiver {
        private Stub mLocalSender;
        private final SynchronousQueue<Intent> mResult;

        /* synthetic */ LocalIntentReceiver(LocalIntentReceiver localIntentReceiver) {
            this();
        }

        private LocalIntentReceiver() {
            this.mResult = new SynchronousQueue();
            this.mLocalSender = new Stub() {
                public void send(int code, Intent intent, String resolvedType, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
                    try {
                        LocalIntentReceiver.this.mResult.offer(intent, 5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }

        public IntentSender getIntentSender() {
            return new IntentSender(this.mLocalSender);
        }

        public Intent getResult() {
            try {
                return (Intent) this.mResult.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    PackageManagerShellCommand(PackageManagerService service) {
        this.mInterface = service;
    }

    public int onCommand(String cmd) {
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            if (cmd.equals("install")) {
                return runInstall();
            }
            if (cmd.equals("install-abandon") || cmd.equals("install-destroy")) {
                return runInstallAbandon();
            }
            if (cmd.equals("install-commit")) {
                return runInstallCommit();
            }
            if (cmd.equals("install-create")) {
                return runInstallCreate();
            }
            if (cmd.equals("install-remove")) {
                return runInstallRemove();
            }
            if (cmd.equals("install-write")) {
                return runInstallWrite();
            }
            if (cmd.equals("compile")) {
                return runCompile();
            }
            if (cmd.equals("dump-profiles")) {
                return runDumpProfiles();
            }
            if (cmd.equals("list")) {
                return runList();
            }
            if (cmd.equals("uninstall")) {
                return runUninstall();
            }
            if (cmd.equals("resolve-activity")) {
                return runResolveActivity();
            }
            if (cmd.equals("query-activities")) {
                return runQueryIntentActivities();
            }
            if (cmd.equals("query-services")) {
                return runQueryIntentServices();
            }
            if (cmd.equals("query-receivers")) {
                return runQueryIntentReceivers();
            }
            if (cmd.equals("suspend")) {
                return runSuspend(true);
            }
            if (cmd.equals("unsuspend")) {
                return runSuspend(false);
            }
            if (cmd.equals("set-home-activity")) {
                return runSetHomeActivity();
            }
            return handleDefaultCommands(cmd);
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
            return -1;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x008b A:{Splitter: B:10:0x0036, ExcHandler: android.content.pm.PackageParser.PackageParserException (r13_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:25:0x008b, code:
            r13 = move-exception;
     */
    /* JADX WARNING: Missing block: B:26:0x008c, code:
            r19.println("Error: Failed to parse APK file : " + r13);
     */
    /* JADX WARNING: Missing block: B:27:0x00a6, code:
            return 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int runInstall() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        InstallParams params = makeInstallParams();
        String inPath = getNextArg();
        boolean installExternal = (params.sessionParams.installFlags & 8) != 0;
        if (params.sessionParams.sizeBytes < 0 && inPath != null) {
            File file = new File(inPath);
            if (file.isFile()) {
                if (installExternal) {
                    try {
                        params.sessionParams.setSize(PackageHelper.calculateInstalledSize(new PackageLite(null, PackageParser.parseApkLite(file, 0), null, null, null), false, params.sessionParams.abiOverride));
                    } catch (Exception e) {
                    }
                } else {
                    params.sessionParams.setSize(file.length());
                }
            }
        }
        int sessionId = doCreateSession(params.sessionParams, params.installerPackageName, params.userId);
        boolean abandonSession = true;
        if (inPath == null) {
            try {
                if (params.sessionParams.sizeBytes == 0) {
                    pw.println("Error: must either specify a package size or an APK file");
                    if (1 != null) {
                        try {
                            doAbandonSession(sessionId, false);
                        } catch (Exception e2) {
                        }
                    }
                    return 1;
                }
            } catch (Throwable th) {
                if (abandonSession) {
                    try {
                        doAbandonSession(sessionId, false);
                    } catch (Exception e3) {
                    }
                }
            }
        }
        if (doWriteSplit(sessionId, inPath, params.sessionParams.sizeBytes, "base.apk", false) != 0) {
            if (1 != null) {
                try {
                    doAbandonSession(sessionId, false);
                } catch (Exception e4) {
                }
            }
            return 1;
        } else if (doCommitSession(sessionId, false) != 0) {
            if (1 != null) {
                try {
                    doAbandonSession(sessionId, false);
                } catch (Exception e5) {
                }
            }
            return 1;
        } else {
            abandonSession = false;
            pw.println("Success");
            if (null != null) {
                try {
                    doAbandonSession(sessionId, false);
                } catch (Exception e6) {
                }
            }
            return 0;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0078 A:{Splitter: B:12:0x0044, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:15:0x0078, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:16:0x0079, code:
            r3.println(r0.toString());
     */
    /* JADX WARNING: Missing block: B:17:0x0080, code:
            return 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int runSuspend(boolean suspendedState) {
        PrintWriter pw = getOutPrintWriter();
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                String packageName = getNextArg();
                if (packageName == null) {
                    pw.println("Error: package name not specified");
                    return 1;
                }
                try {
                    this.mInterface.setPackagesSuspendedAsUser(new String[]{packageName}, suspendedState, userId);
                    pw.println("Package " + packageName + " new suspended state: " + this.mInterface.isPackageSuspendedForUser(packageName, userId));
                    return 0;
                } catch (Exception e) {
                }
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                pw.println("Error: Unknown option: " + opt);
                return 1;
            }
        }
    }

    private int runInstallAbandon() throws RemoteException {
        return doAbandonSession(Integer.parseInt(getNextArg()), true);
    }

    private int runInstallCommit() throws RemoteException {
        return doCommitSession(Integer.parseInt(getNextArg()), true);
    }

    private int runInstallCreate() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        InstallParams installParams = makeInstallParams();
        pw.println("Success: created install session [" + doCreateSession(installParams.sessionParams, installParams.installerPackageName, installParams.userId) + "]");
        return 0;
    }

    private int runInstallWrite() throws RemoteException {
        long sizeBytes = -1;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                return doWriteSplit(Integer.parseInt(getNextArg()), getNextArg(), sizeBytes, getNextArg(), true);
            } else if (opt.equals("-S")) {
                sizeBytes = Long.parseLong(getNextArg());
            } else {
                throw new IllegalArgumentException("Unknown option: " + opt);
            }
        }
    }

    private int runInstallRemove() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        int sessionId = Integer.parseInt(getNextArg());
        String splitName = getNextArg();
        if (splitName != null) {
            return doRemoveSplit(sessionId, splitName, true);
        }
        pw.println("Error: split name not specified");
        return 1;
    }

    private int runCompile() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        boolean checkProfiles = SystemProperties.getBoolean("dalvik.vm.usejitprofiles", false);
        boolean forceCompilation = false;
        boolean allPackages = false;
        boolean clearProfileData = false;
        String compilerFilter = null;
        String compilationReason = null;
        Object checkProfilesRaw = null;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                if (checkProfilesRaw != null) {
                    if ("true".equals(checkProfilesRaw)) {
                        checkProfiles = true;
                    } else if ("false".equals(checkProfilesRaw)) {
                        checkProfiles = false;
                    } else {
                        pw.println("Invalid value for \"--check-prof\". Expected \"true\" or \"false\".");
                        return 1;
                    }
                }
                if (compilerFilter != null && compilationReason != null) {
                    pw.println("Cannot use compilation filter (\"-m\") and compilation reason (\"-r\") at the same time");
                    return 1;
                } else if (compilerFilter == null && compilationReason == null) {
                    pw.println("Cannot run without any of compilation filter (\"-m\") and compilation reason (\"-r\") at the same time");
                    return 1;
                } else {
                    String targetCompilerFilter;
                    String packageName;
                    if (compilerFilter == null) {
                        int reason = -1;
                        for (int i = 0; i < PackageManagerServiceCompilerMapping.REASON_STRINGS.length; i++) {
                            if (PackageManagerServiceCompilerMapping.REASON_STRINGS[i].equals(compilationReason)) {
                                reason = i;
                                break;
                            }
                        }
                        if (reason == -1) {
                            pw.println("Error: Unknown compilation reason: " + compilationReason);
                            return 1;
                        }
                        targetCompilerFilter = PackageManagerServiceCompilerMapping.getCompilerFilterForReason(reason);
                    } else if (DexFile.isValidCompilerFilter(compilerFilter)) {
                        targetCompilerFilter = compilerFilter;
                    } else {
                        pw.println("Error: \"" + compilerFilter + "\" is not a valid compilation filter.");
                        return 1;
                    }
                    List<String> packageNames;
                    if (allPackages) {
                        packageNames = this.mInterface.getAllPackages();
                    } else {
                        packageName = getNextArg();
                        if (packageName == null) {
                            pw.println("Error: package name not specified");
                            return 1;
                        }
                        packageNames = Collections.singletonList(packageName);
                    }
                    List<String> failedPackages = new ArrayList();
                    for (String packageName2 : packageNames) {
                        if (clearProfileData) {
                            this.mInterface.clearApplicationProfileData(packageName2);
                        }
                        if (!this.mInterface.performDexOptMode(packageName2, checkProfiles, targetCompilerFilter, forceCompilation)) {
                            failedPackages.add(packageName2);
                        }
                    }
                    if (failedPackages.isEmpty()) {
                        pw.println("Success");
                        return 0;
                    } else if (failedPackages.size() == 1) {
                        pw.println("Failure: package " + ((String) failedPackages.get(0)) + " could not be compiled");
                        return 1;
                    } else {
                        pw.print("Failure: the following packages could not be compiled: ");
                        boolean is_first = true;
                        for (String packageName22 : failedPackages) {
                            if (is_first) {
                                is_first = false;
                            } else {
                                pw.print(", ");
                            }
                            pw.print(packageName22);
                        }
                        pw.println();
                        return 1;
                    }
                }
            } else if (opt.equals("-a")) {
                allPackages = true;
            } else if (opt.equals("-c")) {
                clearProfileData = true;
            } else if (opt.equals("-f")) {
                forceCompilation = true;
            } else if (opt.equals("-m")) {
                compilerFilter = getNextArgRequired();
            } else if (opt.equals("-r")) {
                compilationReason = getNextArgRequired();
            } else if (opt.equals("--check-prof")) {
                checkProfilesRaw = getNextArgRequired();
            } else if (opt.equals("--reset")) {
                forceCompilation = true;
                clearProfileData = true;
                compilationReason = "install";
            } else {
                pw.println("Error: Unknown option: " + opt);
                return 1;
            }
        }
    }

    private int runDumpProfiles() throws RemoteException {
        this.mInterface.dumpProfiles(getNextArg());
        return 0;
    }

    private int runList() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        String type = getNextArg();
        if (type == null) {
            pw.println("Error: didn't specify type of data to list");
            return -1;
        } else if (type.equals("features")) {
            return runListFeatures();
        } else {
            if (type.equals("instrumentation")) {
                return runListInstrumentation();
            }
            if (type.equals("libraries")) {
                return runListLibraries();
            }
            if (type.equals("package") || type.equals("packages")) {
                return runListPackages(false);
            }
            if (type.equals("permission-groups")) {
                return runListPermissionGroups();
            }
            if (type.equals("permissions")) {
                return runListPermissions();
            }
            pw.println("Error: unknown list type '" + type + "'");
            return -1;
        }
    }

    private int runListFeatures() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        List<FeatureInfo> list = this.mInterface.getSystemAvailableFeatures().getList();
        Collections.sort(list, new Comparator<FeatureInfo>() {
            public int compare(FeatureInfo o1, FeatureInfo o2) {
                if (o1.name == o2.name) {
                    return 0;
                }
                if (o1.name == null) {
                    return -1;
                }
                if (o2.name == null) {
                    return 1;
                }
                return o1.name.compareTo(o2.name);
            }
        });
        int count = list != null ? list.size() : 0;
        for (int p = 0; p < count; p++) {
            FeatureInfo fi = (FeatureInfo) list.get(p);
            pw.print("feature:");
            if (fi.name != null) {
                pw.print(fi.name);
                if (fi.version > 0) {
                    pw.print("=");
                    pw.print(fi.version);
                }
                pw.println();
            } else {
                pw.println("reqGlEsVersion=0x" + Integer.toHexString(fi.reqGlEsVersion));
            }
        }
        return 0;
    }

    private int runListInstrumentation() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        boolean showSourceDir = false;
        String targetPackage = null;
        while (true) {
            try {
                String opt = getNextArg();
                if (opt == null) {
                    List<InstrumentationInfo> list = this.mInterface.queryInstrumentation(targetPackage, 0).getList();
                    Collections.sort(list, new Comparator<InstrumentationInfo>() {
                        public int compare(InstrumentationInfo o1, InstrumentationInfo o2) {
                            return o1.targetPackage.compareTo(o2.targetPackage);
                        }
                    });
                    int count = list != null ? list.size() : 0;
                    for (int p = 0; p < count; p++) {
                        InstrumentationInfo ii = (InstrumentationInfo) list.get(p);
                        pw.print("instrumentation:");
                        if (showSourceDir) {
                            pw.print(ii.sourceDir);
                            pw.print("=");
                        }
                        pw.print(new ComponentName(ii.packageName, ii.name).flattenToShortString());
                        pw.print(" (target=");
                        pw.print(ii.targetPackage);
                        pw.println(")");
                    }
                    return 0;
                } else if (opt.equals("-f")) {
                    showSourceDir = true;
                } else if (opt.charAt(0) != '-') {
                    targetPackage = opt;
                } else {
                    pw.println("Error: Unknown option: " + opt);
                    return -1;
                }
            } catch (RuntimeException ex) {
                pw.println("Error: " + ex.toString());
                return -1;
            }
        }
    }

    private int runListLibraries() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        List<String> list = new ArrayList();
        String[] rawList = this.mInterface.getSystemSharedLibraryNames();
        for (Object add : rawList) {
            list.add(add);
        }
        Collections.sort(list, new Comparator<String>() {
            public int compare(String o1, String o2) {
                if (o1 == o2) {
                    return 0;
                }
                if (o1 == null) {
                    return -1;
                }
                if (o2 == null) {
                    return 1;
                }
                return o1.compareTo(o2);
            }
        });
        int count = list != null ? list.size() : 0;
        for (int p = 0; p < count; p++) {
            String lib = (String) list.get(p);
            pw.print("library:");
            pw.println(lib);
        }
        return 0;
    }

    private int runListPackages(boolean showSourceDir) throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        int getFlags = 0;
        boolean listDisabled = false;
        boolean listEnabled = false;
        boolean listSystem = false;
        boolean listThirdParty = false;
        boolean listInstaller = false;
        int userId = 0;
        while (true) {
            try {
                String opt = getNextOption();
                if (opt == null) {
                    String filter = getNextArg();
                    List<PackageInfo> packages = this.mInterface.getInstalledPackages(getFlags, userId).getList();
                    int count = packages.size();
                    for (int p = 0; p < count; p++) {
                        PackageInfo info = (PackageInfo) packages.get(p);
                        if (filter == null || info.packageName.contains(filter)) {
                            boolean isSystem = (info.applicationInfo.flags & 1) != 0;
                            if (!(listDisabled && info.applicationInfo.enabled) && ((!listEnabled || info.applicationInfo.enabled) && ((!listSystem || isSystem) && !(listThirdParty && isSystem)))) {
                                pw.print("package:");
                                if (showSourceDir) {
                                    pw.print(info.applicationInfo.sourceDir);
                                    pw.print("=");
                                }
                                pw.print(info.packageName);
                                if (listInstaller) {
                                    pw.print("  installer=");
                                    pw.print(this.mInterface.getInstallerPackageName(info.packageName));
                                }
                                pw.println();
                            }
                        }
                    }
                    return 0;
                } else if (opt.equals("-d")) {
                    listDisabled = true;
                } else if (opt.equals("-e")) {
                    listEnabled = true;
                } else if (opt.equals("-f")) {
                    showSourceDir = true;
                } else if (opt.equals("-i")) {
                    listInstaller = true;
                } else if (opt.equals("-l")) {
                    continue;
                } else if (opt.equals("-lf")) {
                    showSourceDir = true;
                } else if (opt.equals("-s")) {
                    listSystem = true;
                } else if (opt.equals("-u")) {
                    getFlags |= DumpState.DUMP_PREFERRED_XML;
                } else if (opt.equals("-3")) {
                    listThirdParty = true;
                } else if (opt.equals("--user")) {
                    userId = UserHandle.parseUserArg(getNextArgRequired());
                } else {
                    pw.println("Error: Unknown option: " + opt);
                    return -1;
                }
            } catch (RuntimeException ex) {
                pw.println("Error: " + ex.toString());
                return -1;
            }
        }
    }

    private int runListPermissionGroups() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        List<PermissionGroupInfo> pgs = this.mInterface.getAllPermissionGroups(0).getList();
        int count = pgs.size();
        for (int p = 0; p < count; p++) {
            PermissionGroupInfo pgi = (PermissionGroupInfo) pgs.get(p);
            pw.print("permission group:");
            pw.println(pgi.name);
        }
        return 0;
    }

    private int runListPermissions() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        boolean labels = false;
        boolean groups = false;
        boolean userOnly = false;
        boolean summary = false;
        boolean dangerousOnly = false;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                ArrayList<String> groupList = new ArrayList();
                if (groups) {
                    List<PermissionGroupInfo> infos = this.mInterface.getAllPermissionGroups(0).getList();
                    int count = infos.size();
                    for (int i = 0; i < count; i++) {
                        groupList.add(((PermissionGroupInfo) infos.get(i)).name);
                    }
                    groupList.add(null);
                } else {
                    groupList.add(getNextArg());
                }
                if (dangerousOnly) {
                    pw.println("Dangerous Permissions:");
                    pw.println(IElsaManager.EMPTY_PACKAGE);
                    doListPermissions(groupList, groups, labels, summary, 1, 1);
                    if (userOnly) {
                        pw.println("Normal Permissions:");
                        pw.println(IElsaManager.EMPTY_PACKAGE);
                        doListPermissions(groupList, groups, labels, summary, 0, 0);
                    }
                } else if (userOnly) {
                    pw.println("Dangerous and Normal Permissions:");
                    pw.println(IElsaManager.EMPTY_PACKAGE);
                    doListPermissions(groupList, groups, labels, summary, 0, 1);
                } else {
                    pw.println("All Permissions:");
                    pw.println(IElsaManager.EMPTY_PACKAGE);
                    doListPermissions(groupList, groups, labels, summary, -10000, 10000);
                }
                return 0;
            } else if (opt.equals("-d")) {
                dangerousOnly = true;
            } else if (opt.equals("-f")) {
                labels = true;
            } else if (opt.equals("-g")) {
                groups = true;
            } else if (opt.equals("-s")) {
                groups = true;
                labels = true;
                summary = true;
            } else if (opt.equals("-u")) {
                userOnly = true;
            } else {
                pw.println("Error: Unknown option: " + opt);
                return 1;
            }
        }
    }

    private int runUninstall() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        int flags = 0;
        int userId = -1;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                String packageName = getNextArg();
                if (packageName == null) {
                    pw.println("Error: package name not specified");
                    return 1;
                }
                String splitName = getNextArg();
                if (splitName != null) {
                    return runRemoveSplit(packageName, splitName);
                }
                boolean deleteAll = false;
                userId = translateUserId(userId, "runUninstall");
                if (userId == -1) {
                    userId = 0;
                    flags |= 2;
                    deleteAll = true;
                } else {
                    PackageInfo info = this.mInterface.getPackageInfo(packageName, 0, userId);
                    if (info == null) {
                        pw.println("Failure [not installed for " + userId + "]");
                        return 1;
                    }
                    if ((info.applicationInfo.flags & 1) != 0) {
                        flags |= 4;
                    }
                }
                PackageInfo info2 = this.mInterface.getPackageInfo(packageName, 0, userId);
                if (info2 != null || deleteAll) {
                    if (!(info2 == null || (info2.applicationInfo.flagsEx & 1) == 0)) {
                        flags &= -3;
                    }
                    LocalIntentReceiver receiver = new LocalIntentReceiver();
                    this.mInterface.getPackageInstaller().uninstall(packageName, null, flags, receiver.getIntentSender(), userId);
                    Intent result = receiver.getResult();
                    if (result.getIntExtra("android.content.pm.extra.STATUS", 1) == 0) {
                        pw.println("Success");
                        OppoFontUtils.createFontLink(packageName);
                        return 0;
                    }
                    pw.println("Failure [" + result.getStringExtra("android.content.pm.extra.STATUS_MESSAGE") + "]");
                    return 1;
                }
                pw.println("Failure - not installed for " + userId);
                return 1;
            } else if (opt.equals("-k")) {
                flags |= 1;
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                pw.println("Error: Unknown option: " + opt);
                return 1;
            }
        }
    }

    private int runRemoveSplit(String packageName, String splitName) throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        SessionParams sessionParams = new SessionParams(2);
        sessionParams.installFlags |= 2;
        sessionParams.appPackageName = packageName;
        int sessionId = doCreateSession(sessionParams, null, -1);
        boolean abandonSession = true;
        try {
            if (doRemoveSplit(sessionId, splitName, false) != 0) {
                if (1 != null) {
                    try {
                        doAbandonSession(sessionId, false);
                    } catch (Exception e) {
                    }
                }
                return 1;
            } else if (doCommitSession(sessionId, false) != 0) {
                if (1 != null) {
                    try {
                        doAbandonSession(sessionId, false);
                    } catch (Exception e2) {
                    }
                }
                return 1;
            } else {
                abandonSession = false;
                pw.println("Success");
                if (null != null) {
                    try {
                        doAbandonSession(sessionId, false);
                    } catch (Exception e3) {
                    }
                }
                return 0;
            }
        } catch (Throwable th) {
            if (abandonSession) {
                try {
                    doAbandonSession(sessionId, false);
                } catch (Exception e4) {
                }
            }
        }
    }

    private Intent parseIntentAndUser() throws URISyntaxException {
        this.mTargetUser = -2;
        this.mBrief = false;
        this.mComponents = false;
        Intent intent = Intent.parseCommandArgs(this, new CommandOptionHandler() {
            public boolean handleOption(String opt, ShellCommand cmd) {
                if ("--user".equals(opt)) {
                    PackageManagerShellCommand.this.mTargetUser = UserHandle.parseUserArg(cmd.getNextArgRequired());
                    return true;
                } else if ("--brief".equals(opt)) {
                    PackageManagerShellCommand.this.mBrief = true;
                    return true;
                } else if (!"--components".equals(opt)) {
                    return false;
                } else {
                    PackageManagerShellCommand.this.mComponents = true;
                    return true;
                }
            }
        });
        this.mTargetUser = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), this.mTargetUser, false, false, null, null);
        return intent;
    }

    private void printResolveInfo(PrintWriterPrinter pr, String prefix, ResolveInfo ri, boolean brief, boolean components) {
        if (brief || components) {
            ComponentName comp;
            if (ri.activityInfo != null) {
                comp = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
            } else if (ri.serviceInfo != null) {
                comp = new ComponentName(ri.serviceInfo.packageName, ri.serviceInfo.name);
            } else if (ri.providerInfo != null) {
                comp = new ComponentName(ri.providerInfo.packageName, ri.providerInfo.name);
            } else {
                comp = null;
            }
            if (comp != null) {
                if (!components) {
                    pr.println(prefix + "priority=" + ri.priority + " preferredOrder=" + ri.preferredOrder + " match=0x" + Integer.toHexString(ri.match) + " specificIndex=" + ri.specificIndex + " isDefault=" + ri.isDefault);
                }
                pr.println(prefix + comp.flattenToShortString());
                return;
            }
        }
        ri.dump(pr, prefix);
    }

    private int runResolveActivity() {
        try {
            try {
                ResolveInfo ri = this.mInterface.resolveIntent(parseIntentAndUser(), null, 0, this.mTargetUser);
                PrintWriter pw = getOutPrintWriter();
                if (ri == null) {
                    pw.println("No activity found");
                } else {
                    printResolveInfo(new PrintWriterPrinter(pw), IElsaManager.EMPTY_PACKAGE, ri, this.mBrief, this.mComponents);
                }
                return 0;
            } catch (RemoteException e) {
                throw new RuntimeException("Failed calling service", e);
            }
        } catch (URISyntaxException e2) {
            throw new RuntimeException(e2.getMessage(), e2);
        }
    }

    private int runQueryIntentActivities() {
        try {
            try {
                List<ResolveInfo> result = this.mInterface.queryIntentActivities(parseIntentAndUser(), null, 0, this.mTargetUser).getList();
                PrintWriter pw = getOutPrintWriter();
                PrintWriterPrinter pr;
                int i;
                if (result == null || result.size() <= 0) {
                    pw.println("No activities found");
                } else if (this.mComponents) {
                    pr = new PrintWriterPrinter(pw);
                    for (i = 0; i < result.size(); i++) {
                        printResolveInfo(pr, IElsaManager.EMPTY_PACKAGE, (ResolveInfo) result.get(i), this.mBrief, this.mComponents);
                    }
                } else {
                    pw.print(result.size());
                    pw.println(" activities found:");
                    pr = new PrintWriterPrinter(pw);
                    for (i = 0; i < result.size(); i++) {
                        pw.print("  Activity #");
                        pw.print(i);
                        pw.println(":");
                        printResolveInfo(pr, "    ", (ResolveInfo) result.get(i), this.mBrief, this.mComponents);
                    }
                }
                return 0;
            } catch (RemoteException e) {
                throw new RuntimeException("Failed calling service", e);
            }
        } catch (URISyntaxException e2) {
            throw new RuntimeException(e2.getMessage(), e2);
        }
    }

    private int runQueryIntentServices() {
        try {
            try {
                List<ResolveInfo> result = this.mInterface.queryIntentServices(parseIntentAndUser(), null, 0, this.mTargetUser).getList();
                PrintWriter pw = getOutPrintWriter();
                PrintWriterPrinter pr;
                int i;
                if (result == null || result.size() <= 0) {
                    pw.println("No services found");
                } else if (this.mComponents) {
                    pr = new PrintWriterPrinter(pw);
                    for (i = 0; i < result.size(); i++) {
                        printResolveInfo(pr, IElsaManager.EMPTY_PACKAGE, (ResolveInfo) result.get(i), this.mBrief, this.mComponents);
                    }
                } else {
                    pw.print(result.size());
                    pw.println(" services found:");
                    pr = new PrintWriterPrinter(pw);
                    for (i = 0; i < result.size(); i++) {
                        pw.print("  Service #");
                        pw.print(i);
                        pw.println(":");
                        printResolveInfo(pr, "    ", (ResolveInfo) result.get(i), this.mBrief, this.mComponents);
                    }
                }
                return 0;
            } catch (RemoteException e) {
                throw new RuntimeException("Failed calling service", e);
            }
        } catch (URISyntaxException e2) {
            throw new RuntimeException(e2.getMessage(), e2);
        }
    }

    private int runQueryIntentReceivers() {
        try {
            try {
                List<ResolveInfo> result = this.mInterface.queryIntentReceivers(parseIntentAndUser(), null, 0, this.mTargetUser).getList();
                PrintWriter pw = getOutPrintWriter();
                PrintWriterPrinter pr;
                int i;
                if (result == null || result.size() <= 0) {
                    pw.println("No receivers found");
                } else if (this.mComponents) {
                    pr = new PrintWriterPrinter(pw);
                    for (i = 0; i < result.size(); i++) {
                        printResolveInfo(pr, IElsaManager.EMPTY_PACKAGE, (ResolveInfo) result.get(i), this.mBrief, this.mComponents);
                    }
                } else {
                    pw.print(result.size());
                    pw.println(" receivers found:");
                    pr = new PrintWriterPrinter(pw);
                    for (i = 0; i < result.size(); i++) {
                        pw.print("  Receiver #");
                        pw.print(i);
                        pw.println(":");
                        printResolveInfo(pr, "    ", (ResolveInfo) result.get(i), this.mBrief, this.mComponents);
                    }
                }
                return 0;
            } catch (RemoteException e) {
                throw new RuntimeException("Failed calling service", e);
            }
        } catch (URISyntaxException e2) {
            throw new RuntimeException(e2.getMessage(), e2);
        }
    }

    private InstallParams makeInstallParams() {
        SessionParams sessionParams = new SessionParams(1);
        InstallParams params = new InstallParams();
        params.sessionParams = sessionParams;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                return params;
            }
            if (opt.equals("-l")) {
                sessionParams.installFlags |= 1;
            } else if (opt.equals("-r")) {
                sessionParams.installFlags |= 2;
            } else if (opt.equals("-i")) {
                params.installerPackageName = getNextArg();
                if (params.installerPackageName == null) {
                    throw new IllegalArgumentException("Missing installer package");
                }
            } else if (opt.equals("-t")) {
                sessionParams.installFlags |= 4;
            } else if (opt.equals("-s")) {
                sessionParams.installFlags |= 8;
            } else if (opt.equals("-f")) {
                sessionParams.installFlags |= 16;
            } else if (opt.equals("-d")) {
                sessionParams.installFlags |= 128;
            } else if (opt.equals("-g")) {
                sessionParams.installFlags |= 256;
            } else if (opt.equals("--dont-kill")) {
                sessionParams.installFlags |= 4096;
            } else if (opt.equals("--originating-uri")) {
                sessionParams.originatingUri = Uri.parse(getNextArg());
            } else if (opt.equals("--referrer")) {
                sessionParams.referrerUri = Uri.parse(getNextArg());
            } else if (opt.equals("-p")) {
                sessionParams.mode = 2;
                sessionParams.appPackageName = getNextArg();
                if (sessionParams.appPackageName == null) {
                    throw new IllegalArgumentException("Missing inherit package name");
                }
            } else if (opt.equals("-S")) {
                sessionParams.setSize(Long.parseLong(getNextArg()));
            } else if (opt.equals("--abi")) {
                sessionParams.abiOverride = checkAbiArgument(getNextArg());
            } else if (opt.equals("--ephemeral")) {
                sessionParams.installFlags |= 2048;
            } else if (opt.equals("--user")) {
                params.userId = UserHandle.parseUserArg(getNextArgRequired());
            } else if (opt.equals("--install-location")) {
                sessionParams.installLocation = Integer.parseInt(getNextArg());
            } else if (opt.equals("--force-uuid")) {
                sessionParams.installFlags |= 512;
                sessionParams.volumeUuid = getNextArg();
                if (DecryptTool.UNLOCK_TYPE_INTERNAL.equals(sessionParams.volumeUuid)) {
                    sessionParams.volumeUuid = null;
                }
            } else if (opt.equals("--force-sdk")) {
                sessionParams.installFlags |= DumpState.DUMP_PREFERRED_XML;
            } else {
                throw new IllegalArgumentException("Unknown option " + opt);
            }
        }
    }

    private int runSetHomeActivity() {
        ComponentName componentName = null;
        PrintWriter pw = getOutPrintWriter();
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                String component = getNextArg();
                if (component != null) {
                    componentName = ComponentName.unflattenFromString(component);
                }
                if (componentName == null) {
                    pw.println("Error: component name not specified or invalid");
                    return 1;
                }
                try {
                    this.mInterface.setHomeActivity(componentName, userId);
                    pw.println("Success");
                    return 0;
                } catch (Exception e) {
                    pw.println(e.toString());
                    return 1;
                }
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                pw.println("Error: Unknown option: " + opt);
                return 1;
            }
        }
    }

    private static String checkAbiArgument(String abi) {
        if (TextUtils.isEmpty(abi)) {
            throw new IllegalArgumentException("Missing ABI argument");
        } else if ("-".equals(abi)) {
            return abi;
        } else {
            for (String supportedAbi : Build.SUPPORTED_ABIS) {
                if (supportedAbi.equals(abi)) {
                    return abi;
                }
            }
            throw new IllegalArgumentException("ABI " + abi + " not supported on this device");
        }
    }

    private int translateUserId(int userId, String logContext) {
        return ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, true, logContext, "pm command");
    }

    private int doCreateSession(SessionParams params, String installerPackageName, int userId) throws RemoteException {
        userId = translateUserId(userId, "runInstallCreate");
        if (userId == -1) {
            userId = 0;
            params.installFlags |= 64;
        }
        return this.mInterface.getPackageInstaller().createSession(params, installerPackageName, userId);
    }

    private int doWriteSplit(int sessionId, String inPath, long sizeBytes, String splitName, boolean logSuccess) throws RemoteException {
        IOException e;
        Throwable th;
        PrintWriter pw = getOutPrintWriter();
        if (sizeBytes <= 0) {
            pw.println("Error: must specify a APK size");
            return 1;
        } else if (inPath == null || "-".equals(inPath)) {
            SessionInfo info = this.mInterface.getPackageInstaller().getSessionInfo(sessionId);
            AutoCloseable in = null;
            OutputStream out = null;
            Session session;
            try {
                session = new Session(this.mInterface.getPackageInstaller().openSession(sessionId));
                try {
                    InputStream in2 = new SizedInputStream(getRawInputStream(), sizeBytes);
                    try {
                        out = session.openWrite(splitName, 0, sizeBytes);
                        int total = 0;
                        byte[] buffer = new byte[DumpState.DUMP_INSTALLS];
                        while (true) {
                            int c = in2.read(buffer);
                            if (c == -1) {
                                break;
                            }
                            total += c;
                            out.write(buffer, 0, c);
                            if (info.sizeBytes > 0) {
                                session.addProgress(((float) c) / ((float) info.sizeBytes));
                            }
                        }
                        session.fsync(out);
                        if (logSuccess) {
                            pw.println("Success: streamed " + total + " bytes");
                        }
                        IoUtils.closeQuietly(out);
                        IoUtils.closeQuietly(in2);
                        IoUtils.closeQuietly(session);
                        return 0;
                    } catch (IOException e2) {
                        e = e2;
                        in = in2;
                        try {
                            pw.println("Error: failed to write; " + e.getMessage());
                            IoUtils.closeQuietly(out);
                            IoUtils.closeQuietly(in);
                            IoUtils.closeQuietly(session);
                            return 1;
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(out);
                            IoUtils.closeQuietly(in);
                            IoUtils.closeQuietly(session);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        Object in3 = in2;
                        IoUtils.closeQuietly(out);
                        IoUtils.closeQuietly(in3);
                        IoUtils.closeQuietly(session);
                        throw th;
                    }
                } catch (IOException e3) {
                    e = e3;
                    pw.println("Error: failed to write; " + e.getMessage());
                    IoUtils.closeQuietly(out);
                    IoUtils.closeQuietly(in3);
                    IoUtils.closeQuietly(session);
                    return 1;
                }
            } catch (IOException e4) {
                e = e4;
                session = null;
                pw.println("Error: failed to write; " + e.getMessage());
                IoUtils.closeQuietly(out);
                IoUtils.closeQuietly(in3);
                IoUtils.closeQuietly(session);
                return 1;
            } catch (Throwable th4) {
                th = th4;
                session = null;
                IoUtils.closeQuietly(out);
                IoUtils.closeQuietly(in3);
                IoUtils.closeQuietly(session);
                throw th;
            }
        } else {
            pw.println("Error: APK content must be streamed");
            return 1;
        }
    }

    private int doRemoveSplit(int sessionId, String splitName, boolean logSuccess) throws RemoteException {
        IOException e;
        Object session;
        Throwable th;
        PrintWriter pw = getOutPrintWriter();
        AutoCloseable session2 = null;
        try {
            Session session3 = new Session(this.mInterface.getPackageInstaller().openSession(sessionId));
            try {
                session3.removeSplit(splitName);
                if (logSuccess) {
                    pw.println("Success");
                }
                IoUtils.closeQuietly(session3);
                return 0;
            } catch (IOException e2) {
                e = e2;
                session2 = session3;
                try {
                    pw.println("Error: failed to remove split; " + e.getMessage());
                    IoUtils.closeQuietly(session2);
                    return 1;
                } catch (Throwable th2) {
                    th = th2;
                    IoUtils.closeQuietly(session2);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                session2 = session3;
                IoUtils.closeQuietly(session2);
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            pw.println("Error: failed to remove split; " + e.getMessage());
            IoUtils.closeQuietly(session2);
            return 1;
        }
    }

    private int doCommitSession(int sessionId, boolean logSuccess) throws RemoteException {
        Throwable th;
        PrintWriter pw = getOutPrintWriter();
        AutoCloseable autoCloseable = null;
        try {
            Session session = new Session(this.mInterface.getPackageInstaller().openSession(sessionId));
            try {
                LocalIntentReceiver receiver = new LocalIntentReceiver();
                session.commit(receiver.getIntentSender());
                Intent result = receiver.getResult();
                int status = result.getIntExtra("android.content.pm.extra.STATUS", 1);
                if (status != 0) {
                    pw.println("Failure [" + result.getStringExtra("android.content.pm.extra.STATUS_MESSAGE") + "]");
                } else if (logSuccess) {
                    pw.println("Success");
                }
                IoUtils.closeQuietly(session);
                return status;
            } catch (Throwable th2) {
                th = th2;
                autoCloseable = session;
            }
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(autoCloseable);
            throw th;
        }
    }

    private int doAbandonSession(int sessionId, boolean logSuccess) throws RemoteException {
        Throwable th;
        PrintWriter pw = getOutPrintWriter();
        Session session = null;
        try {
            Session session2 = new Session(this.mInterface.getPackageInstaller().openSession(sessionId));
            try {
                session2.abandon();
                if (logSuccess) {
                    pw.println("Success");
                }
                IoUtils.closeQuietly(session2);
                return 0;
            } catch (Throwable th2) {
                th = th2;
                session = session2;
                IoUtils.closeQuietly(session);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(session);
            throw th;
        }
    }

    private void doListPermissions(ArrayList<String> groupList, boolean groups, boolean labels, boolean summary, int startProtectionLevel, int endProtectionLevel) throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        int groupCount = groupList.size();
        for (int i = 0; i < groupCount; i++) {
            String groupName = (String) groupList.get(i);
            String prefix = IElsaManager.EMPTY_PACKAGE;
            if (groups) {
                if (i > 0) {
                    pw.println(IElsaManager.EMPTY_PACKAGE);
                }
                if (groupName != null) {
                    PermissionGroupInfo pgi = this.mInterface.getPermissionGroupInfo(groupName, 0);
                    if (!summary) {
                        pw.println((labels ? "+ " : IElsaManager.EMPTY_PACKAGE) + "group:" + pgi.name);
                        if (labels) {
                            pw.println("  package:" + pgi.packageName);
                            if (getResources(pgi) != null) {
                                pw.println("  label:" + loadText(pgi, pgi.labelRes, pgi.nonLocalizedLabel));
                                pw.println("  description:" + loadText(pgi, pgi.descriptionRes, pgi.nonLocalizedDescription));
                            }
                        }
                    } else if (getResources(pgi) != null) {
                        pw.print(loadText(pgi, pgi.labelRes, pgi.nonLocalizedLabel) + ": ");
                    } else {
                        pw.print(pgi.name + ": ");
                    }
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    String str = (!labels || summary) ? IElsaManager.EMPTY_PACKAGE : "+ ";
                    pw.println(stringBuilder.append(str).append("ungrouped:").toString());
                }
                prefix = "  ";
            }
            List<PermissionInfo> ps = this.mInterface.queryPermissionsByGroup((String) groupList.get(i), 0).getList();
            int count = ps.size();
            boolean first = true;
            for (int p = 0; p < count; p++) {
                PermissionInfo pi = (PermissionInfo) ps.get(p);
                if (!groups || groupName != null || pi.group == null) {
                    int base = pi.protectionLevel & 15;
                    if (base >= startProtectionLevel && base <= endProtectionLevel) {
                        if (summary) {
                            if (first) {
                                first = false;
                            } else {
                                pw.print(", ");
                            }
                            if (getResources(pi) != null) {
                                pw.print(loadText(pi, pi.labelRes, pi.nonLocalizedLabel));
                            } else {
                                pw.print(pi.name);
                            }
                        } else {
                            pw.println(prefix + (labels ? "+ " : IElsaManager.EMPTY_PACKAGE) + "permission:" + pi.name);
                            if (labels) {
                                pw.println(prefix + "  package:" + pi.packageName);
                                if (getResources(pi) != null) {
                                    pw.println(prefix + "  label:" + loadText(pi, pi.labelRes, pi.nonLocalizedLabel));
                                    pw.println(prefix + "  description:" + loadText(pi, pi.descriptionRes, pi.nonLocalizedDescription));
                                }
                                pw.println(prefix + "  protectionLevel:" + PermissionInfo.protectionToString(pi.protectionLevel));
                            }
                        }
                    }
                }
            }
            if (summary) {
                pw.println(IElsaManager.EMPTY_PACKAGE);
            }
        }
    }

    private String loadText(PackageItemInfo pii, int res, CharSequence nonLocalized) throws RemoteException {
        String str = null;
        if (nonLocalized != null) {
            return nonLocalized.toString();
        }
        if (res != 0) {
            Resources r = getResources(pii);
            if (r != null) {
                try {
                    return r.getString(res);
                } catch (NotFoundException e) {
                }
            }
        }
        return str;
    }

    private Resources getResources(PackageItemInfo pii) throws RemoteException {
        Resources res = (Resources) this.mResourceCache.get(pii.packageName);
        if (res != null) {
            return res;
        }
        ApplicationInfo ai = this.mInterface.getApplicationInfo(pii.packageName, 0, 0);
        AssetManager am = new AssetManager();
        am.addAssetPath(ai.publicSourceDir);
        res = new Resources(am, null, null);
        this.mResourceCache.put(pii.packageName, res);
        return res;
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Package manager (package) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println(IElsaManager.EMPTY_PACKAGE);
        pw.println("  compile [-m MODE | -r REASON] [-f] [-c]");
        pw.println("          [--reset] [--check-prof (true | false)] (-a | TARGET-PACKAGE)");
        pw.println("    Trigger compilation of TARGET-PACKAGE or all packages if \"-a\".");
        pw.println("    Options:");
        pw.println("      -a: compile all packages");
        pw.println("      -c: clear profile data before compiling");
        pw.println("      -f: force compilation even if not needed");
        pw.println("      -m: select compilation mode");
        pw.println("          MODE is one of the dex2oat compiler filters:");
        pw.println("            verify-none");
        pw.println("            verify-at-runtime");
        pw.println("            verify-profile");
        pw.println("            interpret-only");
        pw.println("            space-profile");
        pw.println("            space");
        pw.println("            speed-profile");
        pw.println("            speed");
        pw.println("            everything");
        pw.println("      -r: select compilation reason");
        pw.println("          REASON is one of:");
        for (String str : PackageManagerServiceCompilerMapping.REASON_STRINGS) {
            pw.println("            " + str);
        }
        pw.println("      --reset: restore package to its post-install state");
        pw.println("      --check-prof (true | false): look at profiles when doing dexopt?");
        pw.println("  list features");
        pw.println("    Prints all features of the system.");
        pw.println("  list instrumentation [-f] [TARGET-PACKAGE]");
        pw.println("    Prints all test packages; optionally only those targeting TARGET-PACKAGE");
        pw.println("    Options:");
        pw.println("      -f: dump the name of the .apk file containing the test package");
        pw.println("  list libraries");
        pw.println("    Prints all system libraries.");
        pw.println("  list packages [-f] [-d] [-e] [-s] [-3] [-i] [-u] [--user USER_ID] [FILTER]");
        pw.println("    Prints all packages; optionally only those whose name contains");
        pw.println("    the text in FILTER.");
        pw.println("    Options:");
        pw.println("      -f: see their associated file");
        pw.println("      -d: filter to only show disabled packages");
        pw.println("      -e: filter to only show enabled packages");
        pw.println("      -s: filter to only show system packages");
        pw.println("      -3: filter to only show third party packages");
        pw.println("      -i: see the installer for the packages");
        pw.println("      -u: also include uninstalled packages");
        pw.println("  list permission-groups");
        pw.println("    Prints all known permission groups.");
        pw.println("  list permissions [-g] [-f] [-d] [-u] [GROUP]");
        pw.println("    Prints all known permissions; optionally only those in GROUP.");
        pw.println("    Options:");
        pw.println("      -g: organize by group");
        pw.println("      -f: print all information");
        pw.println("      -s: short summary");
        pw.println("      -d: only list dangerous permissions");
        pw.println("      -u: list only the permissions users will see");
        pw.println("  dump-profiles TARGET-PACKAGE");
        pw.println("    Dumps method/class profile files to");
        pw.println("    /data/misc/profman/TARGET-PACKAGE.txt");
        pw.println("  resolve-activity [--brief] [--components] [--user USER_ID] INTENT");
        pw.println("    Prints the activity that resolves to the given Intent.");
        pw.println("  query-activities [--brief] [--components] [--user USER_ID] INTENT");
        pw.println("    Prints all activities that can handle the given Intent.");
        pw.println("  query-services [--brief] [--components] [--user USER_ID] INTENT");
        pw.println("    Prints all services that can handle the given Intent.");
        pw.println("  query-receivers [--brief] [--components] [--user USER_ID] INTENT");
        pw.println("    Prints all broadcast receivers that can handle the given Intent.");
        pw.println("  suspend [--user USER_ID] TARGET-PACKAGE");
        pw.println("    Suspends the specified package (as user).");
        pw.println("  unsuspend [--user USER_ID] TARGET-PACKAGE");
        pw.println("    Unsuspends the specified package (as user).");
        pw.println("  set-home-activity [--user USER_ID] TARGET-COMPONENT");
        pw.println("    set the default home activity (aka launcher).");
        pw.println();
        Intent.printIntentArgsHelp(pw, IElsaManager.EMPTY_PACKAGE);
    }
}
