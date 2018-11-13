package com.android.server.pm;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.IPackageInstallerSession.Stub;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller.SessionInfo;
import android.content.pm.PackageInstaller.SessionParams;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.ApkLite;
import android.content.pm.PackageParser.PackageLite;
import android.content.pm.PackageParser.PackageParserException;
import android.content.pm.Signature;
import android.os.Binder;
import android.os.Bundle;
import android.os.FileBridge;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.UserHandle;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStat;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.ExceptionUtils;
import android.util.MathUtils;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.content.NativeLibraryHelper.Handle;
import com.android.internal.content.PackageHelper;
import com.android.internal.os.InstallerConnection.InstallerException;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.server.display.OppoBrightUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileFilter;
import java.io.IOException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import libcore.io.IoUtils;
import libcore.io.Libcore;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class PackageInstallerSession extends Stub {
    private static final boolean LOGD = true;
    private static final int MSG_COMMIT = 0;
    private static final String REMOVE_SPLIT_MARKER_EXTENSION = ".removed";
    private static final String TAG = "PackageInstaller";
    private static final FileFilter sAddedFilter = null;
    private static final FileFilter sRemovedFilter = null;
    final long createdMillis;
    final int defaultContainerGid;
    final String installerPackageName;
    final int installerUid;
    private final AtomicInteger mActiveCount;
    @GuardedBy("mLock")
    private ArrayList<FileBridge> mBridges;
    private final InternalCallback mCallback;
    private Certificate[][] mCertificates;
    @GuardedBy("mLock")
    private float mClientProgress;
    private final Context mContext;
    @GuardedBy("mLock")
    private boolean mDestroyed;
    private String mFinalMessage;
    private int mFinalStatus;
    private final Handler mHandler;
    private final Callback mHandlerCallback;
    @GuardedBy("mLock")
    private File mInheritedFilesBase;
    @GuardedBy("mLock")
    private float mInternalProgress;
    private final boolean mIsInstallerDeviceOwner;
    private final Object mLock;
    private String mPackageName;
    @GuardedBy("mLock")
    private boolean mPermissionsAccepted;
    private final PackageManagerService mPm;
    @GuardedBy("mLock")
    private boolean mPrepared;
    @GuardedBy("mLock")
    private float mProgress;
    @GuardedBy("mLock")
    private boolean mRelinquished;
    @GuardedBy("mLock")
    private IPackageInstallObserver2 mRemoteObserver;
    @GuardedBy("mLock")
    private float mReportedProgress;
    @GuardedBy("mLock")
    private File mResolvedBaseFile;
    @GuardedBy("mLock")
    private final List<File> mResolvedInheritedFiles;
    @GuardedBy("mLock")
    private final List<String> mResolvedInstructionSets;
    @GuardedBy("mLock")
    private File mResolvedStageDir;
    @GuardedBy("mLock")
    private final List<File> mResolvedStagedFiles;
    @GuardedBy("mLock")
    private boolean mSealed;
    private Signature[] mSignatures;
    private int mVersionCode;
    final SessionParams params;
    final int sessionId;
    final String stageCid;
    final File stageDir;
    final int userId;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.PackageInstallerSession.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.PackageInstallerSession.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.PackageInstallerSession.<clinit>():void");
    }

    public PackageInstallerSession(InternalCallback callback, Context context, PackageManagerService pm, Looper looper, int sessionId, int userId, String installerPackageName, int installerUid, SessionParams params, long createdMillis, File stageDir, String stageCid, boolean prepared, boolean sealed) {
        this.mActiveCount = new AtomicInteger();
        this.mLock = new Object();
        this.mClientProgress = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mInternalProgress = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mProgress = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mReportedProgress = -1.0f;
        this.mPrepared = false;
        this.mSealed = false;
        this.mPermissionsAccepted = false;
        this.mRelinquished = false;
        this.mDestroyed = false;
        this.mBridges = new ArrayList();
        this.mResolvedStagedFiles = new ArrayList();
        this.mResolvedInheritedFiles = new ArrayList();
        this.mResolvedInstructionSets = new ArrayList();
        this.mHandlerCallback = new Callback() {
            public boolean handleMessage(Message msg) {
                PackageInfo pkgInfo = PackageInstallerSession.this.mPm.getPackageInfo(PackageInstallerSession.this.params.appPackageName, 64, PackageInstallerSession.this.userId);
                ApplicationInfo appInfo = PackageInstallerSession.this.mPm.getApplicationInfo(PackageInstallerSession.this.params.appPackageName, 0, PackageInstallerSession.this.userId);
                synchronized (PackageInstallerSession.this.mLock) {
                    if (msg.obj != null) {
                        PackageInstallerSession.this.mRemoteObserver = (IPackageInstallObserver2) msg.obj;
                    }
                    try {
                        PackageInstallerSession.this.commitLocked(pkgInfo, appInfo);
                    } catch (PackageManagerException e) {
                        String completeMsg = ExceptionUtils.getCompleteMessage(e);
                        Slog.e(PackageInstallerSession.TAG, "Commit of session " + PackageInstallerSession.this.sessionId + " failed: " + completeMsg);
                        PackageInstallerSession.this.destroyInternal();
                        PackageInstallerSession.this.dispatchSessionFinished(e.error, completeMsg, null);
                    }
                }
                return true;
            }
        };
        this.mCallback = callback;
        this.mContext = context;
        this.mPm = pm;
        this.mHandler = new Handler(looper, this.mHandlerCallback);
        this.sessionId = sessionId;
        this.userId = userId;
        this.installerPackageName = installerPackageName;
        this.installerUid = installerUid;
        this.params = params;
        this.createdMillis = createdMillis;
        this.stageDir = stageDir;
        this.stageCid = stageCid;
        if ((stageDir == null ? 1 : null) == (stageCid == null ? 1 : null)) {
            throw new IllegalArgumentException("Exactly one of stageDir or stageCid stage must be set");
        }
        this.mPrepared = prepared;
        this.mSealed = sealed;
        DevicePolicyManager dpm = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        boolean isPermissionGranted = this.mPm.checkUidPermission("android.permission.INSTALL_PACKAGES", installerUid) == 0;
        boolean isInstallerRoot = installerUid == 0;
        boolean forcePermissionPrompt = (params.installFlags & 1024) != 0;
        this.mIsInstallerDeviceOwner = dpm != null ? dpm.isDeviceOwnerAppOnCallingUser(installerPackageName) : false;
        if ((isPermissionGranted || isInstallerRoot || this.mIsInstallerDeviceOwner) && !forcePermissionPrompt) {
            this.mPermissionsAccepted = true;
        } else {
            this.mPermissionsAccepted = false;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            this.defaultContainerGid = UserHandle.getSharedAppGid(this.mPm.getPackageUid("com.android.defcontainer", DumpState.DUMP_DEXOPT, 0));
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public SessionInfo generateInfo() {
        String str = null;
        boolean z = false;
        SessionInfo info = new SessionInfo();
        synchronized (this.mLock) {
            info.sessionId = this.sessionId;
            info.installerPackageName = this.installerPackageName;
            if (this.mResolvedBaseFile != null) {
                str = this.mResolvedBaseFile.getAbsolutePath();
            }
            info.resolvedBaseCodePath = str;
            info.progress = this.mProgress;
            info.sealed = this.mSealed;
            if (this.mActiveCount.get() > 0) {
                z = true;
            }
            info.active = z;
            info.mode = this.params.mode;
            info.sizeBytes = this.params.sizeBytes;
            info.appPackageName = this.params.appPackageName;
            info.appIcon = this.params.appIcon;
            info.appLabel = this.params.appLabel;
        }
        return info;
    }

    public boolean isPrepared() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mPrepared;
        }
        return z;
    }

    public boolean isSealed() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mSealed;
        }
        return z;
    }

    private void assertPreparedAndNotSealed(String cookie) {
        synchronized (this.mLock) {
            if (!this.mPrepared) {
                throw new IllegalStateException(cookie + " before prepared");
            } else if (this.mSealed) {
                throw new SecurityException(cookie + " not allowed after commit");
            }
        }
    }

    private File resolveStageDir() throws IOException {
        File file;
        synchronized (this.mLock) {
            if (this.mResolvedStageDir == null) {
                if (this.stageDir != null) {
                    this.mResolvedStageDir = this.stageDir;
                } else {
                    String path = PackageHelper.getSdDir(this.stageCid);
                    if (path != null) {
                        this.mResolvedStageDir = new File(path);
                    } else {
                        throw new IOException("Failed to resolve path to container " + this.stageCid);
                    }
                }
            }
            file = this.mResolvedStageDir;
        }
        return file;
    }

    public void setClientProgress(float progress) {
        synchronized (this.mLock) {
            boolean forcePublish = this.mClientProgress == OppoBrightUtils.MIN_LUX_LIMITI;
            this.mClientProgress = progress;
            computeProgressLocked(forcePublish);
        }
    }

    public void addClientProgress(float progress) {
        synchronized (this.mLock) {
            setClientProgress(this.mClientProgress + progress);
        }
    }

    private void computeProgressLocked(boolean forcePublish) {
        this.mProgress = MathUtils.constrain(this.mClientProgress * 0.8f, OppoBrightUtils.MIN_LUX_LIMITI, 0.8f) + MathUtils.constrain(this.mInternalProgress * 0.2f, OppoBrightUtils.MIN_LUX_LIMITI, 0.2f);
        if (forcePublish || ((double) Math.abs(this.mProgress - this.mReportedProgress)) >= 0.01d) {
            this.mReportedProgress = this.mProgress;
            this.mCallback.onSessionProgressChanged(this, this.mProgress);
        }
    }

    public String[] getNames() {
        assertPreparedAndNotSealed("getNames");
        try {
            return resolveStageDir().list();
        } catch (IOException e) {
            throw ExceptionUtils.wrap(e);
        }
    }

    public void removeSplit(String splitName) {
        if (TextUtils.isEmpty(this.params.appPackageName)) {
            throw new IllegalStateException("Must specify package name to remove a split");
        }
        try {
            createRemoveSplitMarker(splitName);
        } catch (IOException e) {
            throw ExceptionUtils.wrap(e);
        }
    }

    private void createRemoveSplitMarker(String splitName) throws IOException {
        try {
            String markerName = splitName + REMOVE_SPLIT_MARKER_EXTENSION;
            if (FileUtils.isValidExtFilename(markerName)) {
                File target = new File(resolveStageDir(), markerName);
                target.createNewFile();
                Os.chmod(target.getAbsolutePath(), 0);
                return;
            }
            throw new IllegalArgumentException("Invalid marker: " + markerName);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public ParcelFileDescriptor openWrite(String name, long offsetBytes, long lengthBytes) {
        try {
            return openWriteInternal(name, offsetBytes, lengthBytes);
        } catch (IOException e) {
            throw ExceptionUtils.wrap(e);
        }
    }

    private ParcelFileDescriptor openWriteInternal(String name, long offsetBytes, long lengthBytes) throws IOException {
        FileBridge bridge;
        synchronized (this.mLock) {
            assertPreparedAndNotSealed("openWrite");
            bridge = new FileBridge();
            this.mBridges.add(bridge);
        }
        long identity;
        try {
            if (FileUtils.isValidExtFilename(name)) {
                identity = Binder.clearCallingIdentity();
                File target = new File(resolveStageDir(), name);
                Binder.restoreCallingIdentity(identity);
                FileDescriptor targetFd = Libcore.os.open(target.getAbsolutePath(), OsConstants.O_CREAT | OsConstants.O_WRONLY, 420);
                Os.chmod(target.getAbsolutePath(), 420);
                if (lengthBytes > 0) {
                    long deltaBytes = lengthBytes - Libcore.os.fstat(targetFd).st_size;
                    if (this.stageDir != null && deltaBytes > 0) {
                        this.mPm.freeStorage(this.params.volumeUuid, deltaBytes);
                    }
                    Libcore.os.posix_fallocate(targetFd, 0, lengthBytes);
                }
                if (offsetBytes > 0) {
                    Libcore.os.lseek(targetFd, offsetBytes, OsConstants.SEEK_SET);
                }
                bridge.setTargetFile(targetFd);
                bridge.start();
                return new ParcelFileDescriptor(bridge.getClientSocket());
            }
            throw new IllegalArgumentException("Invalid name: " + name);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public ParcelFileDescriptor openRead(String name) {
        try {
            return openReadInternal(name);
        } catch (IOException e) {
            throw ExceptionUtils.wrap(e);
        }
    }

    private ParcelFileDescriptor openReadInternal(String name) throws IOException {
        assertPreparedAndNotSealed("openRead");
        try {
            if (FileUtils.isValidExtFilename(name)) {
                return new ParcelFileDescriptor(Libcore.os.open(new File(resolveStageDir(), name).getAbsolutePath(), OsConstants.O_RDONLY, 0));
            }
            throw new IllegalArgumentException("Invalid name: " + name);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public void commit(IntentSender statusReceiver) {
        boolean wasSealed;
        Preconditions.checkNotNull(statusReceiver);
        synchronized (this.mLock) {
            wasSealed = this.mSealed;
            if (!this.mSealed) {
                for (FileBridge bridge : this.mBridges) {
                    if (!bridge.isClosed()) {
                        throw new SecurityException("Files still open");
                    }
                }
                this.mSealed = true;
            }
            this.mClientProgress = 1.0f;
            computeProgressLocked(true);
        }
        if (!wasSealed) {
            this.mCallback.onSessionSealedBlocking(this);
        }
        this.mActiveCount.incrementAndGet();
        this.mHandler.obtainMessage(0, new PackageInstallObserverAdapter(this.mContext, statusReceiver, this.sessionId, this.mIsInstallerDeviceOwner, this.userId).getBinder()).sendToTarget();
    }

    private void commitLocked(PackageInfo pkgInfo, ApplicationInfo appInfo) throws PackageManagerException {
        if (this.mDestroyed) {
            throw new PackageManagerException(-110, "Session destroyed");
        } else if (this.mSealed) {
            try {
                resolveStageDir();
                validateInstallLocked(pkgInfo, appInfo);
                Preconditions.checkNotNull(this.mPackageName);
                Preconditions.checkNotNull(this.mSignatures);
                Preconditions.checkNotNull(this.mResolvedBaseFile);
                if (this.mPermissionsAccepted) {
                    UserHandle user;
                    if (this.stageCid != null) {
                        resizeContainer(this.stageCid, calculateInstalledSize());
                    }
                    if (this.params.mode == 2) {
                        try {
                            List<File> fromFiles = this.mResolvedInheritedFiles;
                            File toDir = resolveStageDir();
                            Slog.d(TAG, "Inherited files: " + this.mResolvedInheritedFiles);
                            if (!this.mResolvedInheritedFiles.isEmpty() && this.mInheritedFilesBase == null) {
                                throw new IllegalStateException("mInheritedFilesBase == null");
                            } else if (isLinkPossible(fromFiles, toDir)) {
                                if (!this.mResolvedInstructionSets.isEmpty()) {
                                    File file = new File(toDir, "oat");
                                    createOatDirs(this.mResolvedInstructionSets, file);
                                }
                                linkFiles(fromFiles, toDir, this.mInheritedFilesBase);
                            } else {
                                copyFiles(fromFiles, toDir);
                            }
                        } catch (IOException e) {
                            throw new PackageManagerException(-4, "Failed to inherit existing install", e);
                        }
                    }
                    this.mInternalProgress = 0.5f;
                    computeProgressLocked(true);
                    extractNativeLibraries(this.mResolvedStageDir, this.params.abiOverride);
                    if (this.stageCid != null) {
                        finalizeAndFixContainer(this.stageCid);
                    }
                    IPackageInstallObserver2 localObserver = new IPackageInstallObserver2.Stub() {
                        public void onUserActionRequired(Intent intent) {
                            throw new IllegalStateException();
                        }

                        public void onPackageInstalled(String basePackageName, int returnCode, String msg, Bundle extras) {
                            PackageInstallerSession.this.destroyInternal();
                            PackageInstallerSession.this.dispatchSessionFinished(returnCode, msg, extras);
                        }
                    };
                    if ((this.params.installFlags & 64) != 0) {
                        user = UserHandle.ALL;
                    } else {
                        user = new UserHandle(this.userId);
                    }
                    this.mRelinquished = true;
                    this.mPm.installStage(this.mPackageName, this.stageDir, this.stageCid, localObserver, this.params, this.installerPackageName, this.installerUid, user, this.mCertificates);
                    return;
                }
                Intent intent = new Intent("android.content.pm.action.CONFIRM_PERMISSIONS");
                intent.setPackage(this.mContext.getPackageManager().getPermissionControllerPackageName());
                intent = intent;
                intent.putExtra("android.content.pm.extra.SESSION_ID", this.sessionId);
                try {
                    this.mRemoteObserver.onUserActionRequired(intent);
                } catch (RemoteException e2) {
                }
                close();
            } catch (IOException e3) {
                throw new PackageManagerException(-18, "Failed to resolve stage location", e3);
            }
        } else {
            throw new PackageManagerException(-110, "Session not sealed");
        }
    }

    private void validateInstallLocked(PackageInfo pkgInfo, ApplicationInfo appInfo) throws PackageManagerException {
        int i;
        int length;
        this.mPackageName = null;
        this.mVersionCode = -1;
        this.mSignatures = null;
        this.mResolvedBaseFile = null;
        this.mResolvedStagedFiles.clear();
        this.mResolvedInheritedFiles.clear();
        File[] removedFiles = this.mResolvedStageDir.listFiles(sRemovedFilter);
        List<String> removeSplitList = new ArrayList();
        if (!ArrayUtils.isEmpty(removedFiles)) {
            for (File removedFile : removedFiles) {
                String fileName = removedFile.getName();
                removeSplitList.add(fileName.substring(0, fileName.length() - REMOVE_SPLIT_MARKER_EXTENSION.length()));
            }
        }
        File[] addedFiles = this.mResolvedStageDir.listFiles(sAddedFilter);
        if (ArrayUtils.isEmpty(addedFiles) && removeSplitList.size() == 0) {
            throw new PackageManagerException(-2, "No packages staged");
        }
        File file;
        String splitName;
        ArraySet<String> stagedSplits = new ArraySet();
        i = 0;
        length = addedFiles.length;
        while (i < length) {
            File addedFile = addedFiles[i];
            try {
                ApkLite apk = PackageParser.parseApkLite(addedFile, 256);
                if (stagedSplits.add(apk.splitName)) {
                    String targetName;
                    if (this.mPackageName == null) {
                        this.mPackageName = apk.packageName;
                        this.mVersionCode = apk.versionCode;
                    }
                    if (this.mSignatures == null) {
                        this.mSignatures = apk.signatures;
                        this.mCertificates = apk.certificates;
                    }
                    assertApkConsistent(String.valueOf(addedFile), apk);
                    if (apk.splitName == null) {
                        targetName = "base.apk";
                    } else {
                        targetName = "split_" + apk.splitName + ".apk";
                    }
                    if (FileUtils.isValidExtFilename(targetName)) {
                        file = new File(this.mResolvedStageDir, targetName);
                        if (!addedFile.equals(file)) {
                            addedFile.renameTo(file);
                        }
                        if (apk.splitName == null) {
                            this.mResolvedBaseFile = file;
                        }
                        this.mResolvedStagedFiles.add(file);
                        i++;
                    } else {
                        throw new PackageManagerException(-2, "Invalid filename: " + targetName);
                    }
                }
                throw new PackageManagerException(-2, "Split " + apk.splitName + " was defined multiple times");
            } catch (PackageParserException e) {
                throw PackageManagerException.from(e);
            }
        }
        if (removeSplitList.size() > 0) {
            for (String splitName2 : removeSplitList) {
                if (!ArrayUtils.contains(pkgInfo.splitNames, splitName2)) {
                    throw new PackageManagerException(-2, "Split not found: " + splitName2);
                }
            }
            if (this.mPackageName == null) {
                this.mPackageName = pkgInfo.packageName;
                this.mVersionCode = pkgInfo.versionCode;
            }
            if (this.mSignatures == null) {
                this.mSignatures = pkgInfo.signatures;
            }
        }
        if (this.params.mode == 1) {
            if (!stagedSplits.contains(null)) {
                throw new PackageManagerException(-2, "Full install must include a base package");
            }
        } else if (appInfo == null) {
            throw new PackageManagerException(-2, "Missing existing base package for " + this.mPackageName);
        } else {
            try {
                PackageLite existing = PackageParser.parsePackageLite(new File(appInfo.getCodePath()), 0);
                assertApkConsistent("Existing base", PackageParser.parseApkLite(new File(appInfo.getBaseCodePath()), 256));
                if (this.mResolvedBaseFile == null) {
                    this.mResolvedBaseFile = new File(appInfo.getBaseCodePath());
                    this.mResolvedInheritedFiles.add(this.mResolvedBaseFile);
                }
                if (!ArrayUtils.isEmpty(existing.splitNames)) {
                    for (int i2 = 0; i2 < existing.splitNames.length; i2++) {
                        splitName2 = existing.splitNames[i2];
                        file = new File(existing.splitCodePaths[i2]);
                        boolean splitRemoved = removeSplitList.contains(splitName2);
                        if (!(stagedSplits.contains(splitName2) || splitRemoved)) {
                            this.mResolvedInheritedFiles.add(file);
                        }
                    }
                }
                File packageInstallDir = new File(appInfo.getBaseCodePath()).getParentFile();
                this.mInheritedFilesBase = packageInstallDir;
                File oatDir = new File(packageInstallDir, "oat");
                if (oatDir.exists()) {
                    File[] archSubdirs = oatDir.listFiles();
                    if (archSubdirs != null && archSubdirs.length > 0) {
                        String[] instructionSets = InstructionSets.getAllDexCodeInstructionSets();
                        for (File archSubDir : archSubdirs) {
                            if (ArrayUtils.contains(instructionSets, archSubDir.getName())) {
                                this.mResolvedInstructionSets.add(archSubDir.getName());
                                List<File> oatFiles = Arrays.asList(archSubDir.listFiles());
                                if (!oatFiles.isEmpty()) {
                                    this.mResolvedInheritedFiles.addAll(oatFiles);
                                }
                            }
                        }
                    }
                }
            } catch (PackageParserException e2) {
                throw PackageManagerException.from(e2);
            }
        }
    }

    private void assertApkConsistent(String tag, ApkLite apk) throws PackageManagerException {
        if (!this.mPackageName.equals(apk.packageName)) {
            throw new PackageManagerException(-2, tag + " package " + apk.packageName + " inconsistent with " + this.mPackageName);
        } else if (this.params.appPackageName != null && !this.params.appPackageName.equals(apk.packageName)) {
            throw new PackageManagerException(-2, tag + " specified package " + this.params.appPackageName + " inconsistent with " + apk.packageName);
        } else if (this.mVersionCode != apk.versionCode) {
            throw new PackageManagerException(-2, tag + " version code " + apk.versionCode + " inconsistent with " + this.mVersionCode);
        } else if (!Signature.areExactMatch(this.mSignatures, apk.signatures)) {
            throw new PackageManagerException(-2, tag + " signatures are inconsistent");
        }
    }

    private long calculateInstalledSize() throws PackageManagerException {
        Preconditions.checkNotNull(this.mResolvedBaseFile);
        try {
            ApkLite baseApk = PackageParser.parseApkLite(this.mResolvedBaseFile, 0);
            List<String> splitPaths = new ArrayList();
            for (File file : this.mResolvedStagedFiles) {
                if (!this.mResolvedBaseFile.equals(file)) {
                    splitPaths.add(file.getAbsolutePath());
                }
            }
            for (File file2 : this.mResolvedInheritedFiles) {
                if (!this.mResolvedBaseFile.equals(file2)) {
                    splitPaths.add(file2.getAbsolutePath());
                }
            }
            try {
                return PackageHelper.calculateInstalledSize(new PackageLite(null, baseApk, null, (String[]) splitPaths.toArray(new String[splitPaths.size()]), null), (this.params.installFlags & 1) != 0, this.params.abiOverride);
            } catch (IOException e) {
                throw new PackageManagerException(-2, "Failed to calculate install size", e);
            }
        } catch (PackageParserException e2) {
            throw PackageManagerException.from(e2);
        }
    }

    private boolean isLinkPossible(List<File> fromFiles, File toDir) {
        try {
            StructStat toStat = Os.stat(toDir.getAbsolutePath());
            for (File fromFile : fromFiles) {
                if (Os.stat(fromFile.getAbsolutePath()).st_dev != toStat.st_dev) {
                    return false;
                }
            }
            return true;
        } catch (ErrnoException e) {
            Slog.w(TAG, "Failed to detect if linking possible: " + e);
            return false;
        }
    }

    private static String getRelativePath(File file, File base) throws IOException {
        String pathStr = file.getAbsolutePath();
        String baseStr = base.getAbsolutePath();
        if (pathStr.contains("/.")) {
            throw new IOException("Invalid path (was relative) : " + pathStr);
        } else if (pathStr.startsWith(baseStr)) {
            return pathStr.substring(baseStr.length());
        } else {
            throw new IOException("File: " + pathStr + " outside base: " + baseStr);
        }
    }

    private void createOatDirs(List<String> instructionSets, File fromDir) throws PackageManagerException {
        for (String instructionSet : instructionSets) {
            try {
                this.mPm.mInstaller.createOatDir(fromDir.getAbsolutePath(), instructionSet);
            } catch (InstallerException e) {
                throw PackageManagerException.from(e);
            }
        }
    }

    private void linkFiles(List<File> fromFiles, File toDir, File fromDir) throws IOException {
        for (File fromFile : fromFiles) {
            String relativePath = getRelativePath(fromFile, fromDir);
            try {
                this.mPm.mInstaller.linkFile(relativePath, fromDir.getAbsolutePath(), toDir.getAbsolutePath());
            } catch (InstallerException e) {
                throw new IOException("failed linkOrCreateDir(" + relativePath + ", " + fromDir + ", " + toDir + ")", e);
            }
        }
        Slog.d(TAG, "Linked " + fromFiles.size() + " files into " + toDir);
    }

    private static void copyFiles(List<File> fromFiles, File toDir) throws IOException {
        for (File file : toDir.listFiles()) {
            if (file.getName().endsWith(".tmp")) {
                file.delete();
            }
        }
        for (File fromFile : fromFiles) {
            File tmpFile = File.createTempFile("inherit", ".tmp", toDir);
            Slog.d(TAG, "Copying " + fromFile + " to " + tmpFile);
            if (FileUtils.copyFile(fromFile, tmpFile)) {
                try {
                    Os.chmod(tmpFile.getAbsolutePath(), 420);
                    File toFile = new File(toDir, fromFile.getName());
                    Slog.d(TAG, "Renaming " + tmpFile + " to " + toFile);
                    if (!tmpFile.renameTo(toFile)) {
                        throw new IOException("Failed to rename " + tmpFile + " to " + toFile);
                    }
                } catch (ErrnoException e) {
                    throw new IOException("Failed to chmod " + tmpFile);
                }
            }
            throw new IOException("Failed to copy " + fromFile + " to " + tmpFile);
        }
        Slog.d(TAG, "Copied " + fromFiles.size() + " files into " + toDir);
    }

    private static void extractNativeLibraries(File packageDir, String abiOverride) throws PackageManagerException {
        File libDir = new File(packageDir, "lib");
        NativeLibraryHelper.removeNativeBinariesFromDirLI(libDir, true);
        try {
            Handle handle = Handle.create(packageDir);
            int res = NativeLibraryHelper.copyNativeBinariesWithOverride(handle, libDir, abiOverride);
            if (res != 1) {
                throw new PackageManagerException(res, "Failed to extract native libraries, res=" + res);
            }
            IoUtils.closeQuietly(handle);
        } catch (IOException e) {
            throw new PackageManagerException(-110, "Failed to extract native libraries", e);
        } catch (Throwable th) {
            IoUtils.closeQuietly(null);
        }
    }

    private static void resizeContainer(String cid, long targetSize) throws PackageManagerException {
        String path = PackageHelper.getSdDir(cid);
        if (path == null) {
            throw new PackageManagerException(-18, "Failed to find mounted " + cid);
        }
        long currentSize = new File(path).getTotalSpace();
        if (currentSize > targetSize) {
            Slog.w(TAG, "Current size " + currentSize + " is larger than target size " + targetSize + "; skipping resize");
        } else if (!PackageHelper.unMountSdDir(cid)) {
            throw new PackageManagerException(-18, "Failed to unmount " + cid + " before resize");
        } else if (!PackageHelper.resizeSdDir(targetSize, cid, PackageManagerService.getEncryptKey())) {
            throw new PackageManagerException(-18, "Failed to resize " + cid + " to " + targetSize + " bytes");
        } else if (PackageHelper.mountSdDir(cid, PackageManagerService.getEncryptKey(), 1000, false) == null) {
            throw new PackageManagerException(-18, "Failed to mount " + cid + " after resize");
        }
    }

    private void finalizeAndFixContainer(String cid) throws PackageManagerException {
        if (!PackageHelper.finalizeSdDir(cid)) {
            throw new PackageManagerException(-18, "Failed to finalize container " + cid);
        } else if (!PackageHelper.fixSdPermissions(cid, this.defaultContainerGid, null)) {
            throw new PackageManagerException(-18, "Failed to fix permissions on container " + cid);
        }
    }

    void setPermissionsResult(boolean accepted) {
        if (!this.mSealed) {
            throw new SecurityException("Must be sealed to accept permissions");
        } else if (accepted) {
            synchronized (this.mLock) {
                this.mPermissionsAccepted = true;
            }
            this.mHandler.obtainMessage(0).sendToTarget();
        } else {
            destroyInternal();
            dispatchSessionFinished(-115, "User rejected permissions", null);
        }
    }

    public void open() throws IOException {
        if (this.mActiveCount.getAndIncrement() == 0) {
            this.mCallback.onSessionActiveChanged(this, true);
        }
        synchronized (this.mLock) {
            if (!this.mPrepared) {
                if (this.stageDir != null) {
                    PackageInstallerService.prepareStageDir(this.stageDir);
                } else if (this.stageCid != null) {
                    long identity = Binder.clearCallingIdentity();
                    try {
                        PackageInstallerService.prepareExternalStageCid(this.stageCid, this.params.sizeBytes);
                        this.mInternalProgress = 0.25f;
                        computeProgressLocked(true);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                } else {
                    throw new IllegalArgumentException("Exactly one of stageDir or stageCid stage must be set");
                }
                this.mPrepared = true;
                this.mCallback.onSessionPrepared(this);
            }
        }
    }

    public void close() {
        if (this.mActiveCount.decrementAndGet() == 0) {
            this.mCallback.onSessionActiveChanged(this, false);
        }
    }

    public void abandon() {
        if (this.mRelinquished) {
            Slog.d(TAG, "Ignoring abandon after commit relinquished control");
            return;
        }
        destroyInternal();
        dispatchSessionFinished(-115, "Session was abandoned", null);
    }

    private void dispatchSessionFinished(int returnCode, String msg, Bundle extras) {
        this.mFinalStatus = returnCode;
        this.mFinalMessage = msg;
        if (this.mRemoteObserver != null) {
            try {
                this.mRemoteObserver.onPackageInstalled(this.mPackageName, returnCode, msg, extras);
            } catch (RemoteException e) {
            }
        }
        this.mCallback.onSessionFinished(this, returnCode == 1);
    }

    private void destroyInternal() {
        synchronized (this.mLock) {
            this.mSealed = true;
            this.mDestroyed = true;
            for (FileBridge bridge : this.mBridges) {
                bridge.forceClose();
            }
        }
        if (this.stageDir != null) {
            try {
                this.mPm.mInstaller.rmPackageDir(this.stageDir.getAbsolutePath());
            } catch (InstallerException e) {
            }
        }
        if (this.stageCid != null) {
            PackageHelper.destroySdDir(this.stageCid);
        }
    }

    void dump(IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            dumpLocked(pw);
        }
    }

    private void dumpLocked(IndentingPrintWriter pw) {
        pw.println("Session " + this.sessionId + ":");
        pw.increaseIndent();
        pw.printPair("userId", Integer.valueOf(this.userId));
        pw.printPair("installerPackageName", this.installerPackageName);
        pw.printPair("installerUid", Integer.valueOf(this.installerUid));
        pw.printPair("createdMillis", Long.valueOf(this.createdMillis));
        pw.printPair("stageDir", this.stageDir);
        pw.printPair("stageCid", this.stageCid);
        pw.println();
        this.params.dump(pw);
        pw.printPair("mClientProgress", Float.valueOf(this.mClientProgress));
        pw.printPair("mProgress", Float.valueOf(this.mProgress));
        pw.printPair("mSealed", Boolean.valueOf(this.mSealed));
        pw.printPair("mPermissionsAccepted", Boolean.valueOf(this.mPermissionsAccepted));
        pw.printPair("mRelinquished", Boolean.valueOf(this.mRelinquished));
        pw.printPair("mDestroyed", Boolean.valueOf(this.mDestroyed));
        pw.printPair("mBridges", Integer.valueOf(this.mBridges.size()));
        pw.printPair("mFinalStatus", Integer.valueOf(this.mFinalStatus));
        pw.printPair("mFinalMessage", this.mFinalMessage);
        pw.println();
        pw.decreaseIndent();
    }
}
