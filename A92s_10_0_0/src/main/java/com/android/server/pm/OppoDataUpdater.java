package com.android.server.pm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageParser;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.os.BackgroundThread;
import com.android.server.am.ColorAppStartupManager;
import com.android.server.am.OppoPermissionInterceptPolicy;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import libcore.io.IoUtils;
import libcore.io.Streams;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class OppoDataUpdater {
    private static final String ACTION_PACKAGE_MANUAL_OPERATION = "debug.package.manual.operation";
    private static final String BACKUP_TEMP_FILES_PATH = "/data/oppo/common/ota";
    private static final String DATA_ENGINEERMODE_DIR = (DATA_ROOT + File.separator + "engineermode");
    private static final String DATA_ETC_DIR = (DATA_ROOT + File.separator + "etc");
    private static final String DATA_ROOT = "/data";
    private static final String DATA_UPDATE_DUMP_FILE = "dump_data_update.txt";
    private static final String DATA_UPDATE_FLAG_FILE = "flag";
    private static final String DATA_UPDATE_PATCH_APP = (DATA_UPDATE_PATCH_ROOT + File.separator + BrightnessConstants.AppSplineXml.TAG_APP);
    private static final String DATA_UPDATE_PATCH_CONF = (DATA_UPDATE_PATCH_ROOT + File.separator + "conf");
    private static final String DATA_UPDATE_PATCH_CONF_ENGINEERMODE = (DATA_UPDATE_PATCH_CONF + File.separator + "engineermode");
    private static final String DATA_UPDATE_PATCH_CONF_ETC = (DATA_UPDATE_PATCH_CONF + File.separator + "etc");
    private static final String DATA_UPDATE_PATCH_DELETE_LIST = (DATA_UPDATE_PATCH_ROOT + File.separator + "delete.list");
    private static final String DATA_UPDATE_PATCH_ROOT = "/data/oppo/coloros/OTA/data_update_patch";
    private static final String DATA_UPDATE_REPORT_FILE = (DATA_UPDATE_ROOT + File.separator + "data_update_report.txt");
    private static final String DATA_UPDATE_ROOT = "/data/oppo/coloros/dataupdate";
    private static final String DATA_UPDATE_STATE_FILE = "data_update_state.xml";
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_DATA_UPDATE_STATE = true;
    private static final boolean DEBUG_MANUAL_PACKAGE_OPERATION = true;
    private static final boolean DEBUG_SCAN = true;
    private static final boolean DEBUG_SYSTEM_MIGRATE_TO_DATA = true;
    private static final int REASON_COPY_APK_FAIL = 4;
    private static final int REASON_DATA_LATEST = 2;
    private static final int REASON_DEFAULT = 0;
    private static final int REASON_DELETE_SYSTEM_REJECTED = 6;
    private static final int REASON_NOT_EXISTS_IN_PRESET_LIST = 8;
    private static final int REASON_NO_SPACE = 7;
    private static final int REASON_REMOVE_APK_FAIL = 3;
    private static final int REASON_RESERVED = 9;
    private static final String[] REASON_STRING_VALUES = {BrightnessConstants.DEFAULT_SPLINE, "app exists in system partition", "app is latest", "delete app failed", "copy apk failed", "app uninstalled by user", "delete system app rejected", "no space", "not exists in preset list", "reserved"};
    private static final int REASON_SYSTEM_EXISTS = 1;
    private static final int REASON_UNINSTALLED_BY_USER = 5;
    private static final String[] RESULT_STRING_VALUES = {BrightnessConstants.DEFAULT_SPLINE, "success", "fail"};
    private static final int RETRY_COUNT = 3;
    private static final int STATE_DEFAULT = 0;
    private static final String[] STATE_STRING_VALUES = {BrightnessConstants.DEFAULT_SPLINE, "updating", "finish"};
    private static final int STATE_UPDATE_FINISH = 2;
    private static final int STATE_UPDATING = 1;
    private static final String TAG = "OppoDataUpdater";
    private static final int TYPE_ADD_MODE = 0;
    private static final int TYPE_DELETE_MODE = 2;
    private static final int TYPE_REPLACE_MODE = 1;
    private static final boolean UPDATED_FAILURE = false;
    private static final boolean UPDATED_SUCCESS = true;
    private static final String VERSION = "2.0.1";
    private static OppoDataUpdater mInstance = null;
    private boolean hasConfigFiles = UPDATED_FAILURE;
    private boolean isBootFromOTAMode = UPDATED_FAILURE;
    private boolean isLastFailed;
    private List<DataUpdatePackage> mAddPackages = new ArrayList();
    private ArraySet<String> mDataPackageNameList = null;
    private DataUpdateStatePersistence mDataUpdateStatePersistence;
    private DataUpdaterCallback mDataUpdaterCallback = null;
    private List<DataUpdatePackage> mDeletePackages = new ArrayList();
    private List<DataUpdatePackage> mFailedPackages = new ArrayList();
    private Handler mH = new Handler(BackgroundThread.getHandler().getLooper());
    /* access modifiers changed from: private */
    public List<String> mInvalidAddPackages = new ArrayList();
    /* access modifiers changed from: private */
    public List<String> mInvalidDeletePackages = new ArrayList();
    private List<DataUpdatePackage> mNeedAddPackages = new ArrayList();
    private List<String> mNeedDeleteCmds = new ArrayList();
    private List<DataUpdatePackage> mNeedDeletePackages = new ArrayList();
    private List<DataUpdatePackage> mNeedUpdatePackages = new ArrayList();
    /* access modifiers changed from: private */
    public Map<String, PackageDataUpdateState> mPackageStateMap = new HashMap();
    private List<DataUpdatePackage> mPackages = new ArrayList();
    private ArraySet<String> mUpdateFailedPackages = new ArraySet<>();

    public enum DataUpdateType {
        ADD,
        DELETE,
        INVALID
    }

    public interface DataUpdaterCallback {
        PackageParser.Package getPackage(String str);

        PackageSetting getPackageSetting(String str);

        void removeCodePath(File file);

        void removePackageSetting(String str);
    }

    public static OppoDataUpdater getInstance() {
        if (mInstance == null) {
            mInstance = new OppoDataUpdater();
        }
        return mInstance;
    }

    private OppoDataUpdater() {
        Slog.i(TAG, "OppoDataUpdaterVersion[2.0.1]");
    }

    public void init(boolean bootFromOTAMode) {
        Slog.d(TAG, "init OppoDataUpdater, bootFromOTAMode[" + bootFromOTAMode + "]");
        ensureDataUpdateDir();
        this.mDataUpdateStatePersistence = new DataUpdateStatePersistence();
        this.isBootFromOTAMode = bootFromOTAMode;
        if (this.isBootFromOTAMode) {
            this.isLastFailed = readFlag();
            scanDataUpdatePatch();
            if (!this.isLastFailed) {
                generateFlag();
            }
            this.mDataUpdateStatePersistence.start();
            updateConfigFiles();
        }
    }

    public void systemReady(Context context) {
        if (context != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_PACKAGE_MANUAL_OPERATION);
            context.registerReceiver(new DataUpdaterReceiver(), filter);
        }
        if (this.isBootFromOTAMode) {
            this.mH.post(new Runnable() {
                /* class com.android.server.pm.OppoDataUpdater.AnonymousClass1 */

                public void run() {
                    OppoDataUpdater.this.resetFlag();
                    OppoDataUpdater.this.collectUpdateResult();
                    if (OppoDataUpdater.this.needUpdate()) {
                        OppoDataUpdater.this.generateReport();
                    }
                    OppoDataUpdater.this.cleanup();
                }
            });
        }
    }

    public void setDataPackageNameList(ArraySet<String> list) {
        if (list != null) {
            this.mDataPackageNameList = list;
        } else {
            Slog.e(TAG, "list is null.");
        }
    }

    private void ensureDataUpdateDir() {
        File dataUpdateDir = new File(DATA_UPDATE_ROOT);
        if (!dataUpdateDir.exists()) {
            Slog.d(TAG, "create /data/oppo/coloros/dataupdate");
            dataUpdateDir.mkdirs();
            if (!dataUpdateDir.exists()) {
                Slog.e(TAG, "create /data/oppo/coloros/dataupdate failed.");
                return;
            }
            return;
        }
        Slog.i(TAG, "/data/oppo/coloros/dataupdate exists.");
    }

    /* access modifiers changed from: private */
    public boolean needUpdate() {
        if (this.mPackages.size() > 0 || this.hasConfigFiles) {
            return true;
        }
        return UPDATED_FAILURE;
    }

    private void scanDataUpdatePatch() {
        File dir = new File(DATA_UPDATE_PATCH_ROOT);
        if (!dir.exists() || !dir.isDirectory()) {
            StringBuilder sb = new StringBuilder();
            sb.append(DATA_UPDATE_PATCH_ROOT);
            sb.append(!dir.exists() ? " not exists" : "not directory");
            sb.append(", stop scan.");
            Slog.w(TAG, sb.toString());
            return;
        }
        scanAddPackages();
        parseDeletePackages();
        scanConfigurations();
    }

    private void scanAddPackages() {
        File appDir = new File(DATA_UPDATE_PATCH_APP);
        if (!appDir.exists()) {
            Slog.w(TAG, appDir.getPath() + " not exist, scan finish.");
            return;
        }
        String[] apkFiles = appDir.list(new FilenameFilter() {
            /* class com.android.server.pm.OppoDataUpdater.AnonymousClass2 */

            public boolean accept(File dir, String name) {
                if (name == null || !name.endsWith(".apk")) {
                    return OppoDataUpdater.UPDATED_FAILURE;
                }
                return true;
            }
        });
        if (apkFiles == null || apkFiles.length <= 0) {
            Slog.w(TAG, "scan nothing in " + appDir.getPath());
            return;
        }
        for (String path : apkFiles) {
            Slog.i(TAG, "scan apk[" + path + "]");
            DataUpdatePackage pkg = new DataUpdatePackage(DataUpdateType.ADD, appDir.getPath() + File.separatorChar + path, null);
            this.mPackages.add(pkg);
            this.mAddPackages.add(pkg);
        }
    }

    private void parseDeletePackages() {
        File deleteList = new File(DATA_UPDATE_PATCH_DELETE_LIST);
        if (!deleteList.exists() || !deleteList.canRead()) {
            StringBuilder sb = new StringBuilder();
            sb.append(deleteList.getPath());
            sb.append(deleteList.exists() ? " can't readable." : " not exists.");
            Slog.w(TAG, sb.toString());
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(deleteList));
            List<String> list = new ArrayList<>();
            while (true) {
                String line = reader.readLine();
                if (line != null) {
                    Slog.d(TAG, "read line[" + line + "]");
                    String line2 = line.trim();
                    if (line2.length() != 0) {
                        if (!list.contains(line2)) {
                            DataUpdatePackage pkg = new DataUpdatePackage(DataUpdateType.DELETE, null, line2);
                            this.mPackages.add(pkg);
                            this.mDeletePackages.add(pkg);
                            list.add(line2);
                        }
                    }
                } else {
                    reader.close();
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void scanConfigurations() {
        File confDir = new File(DATA_UPDATE_PATCH_CONF);
        if (confDir.exists()) {
            this.hasConfigFiles = hasValidFile(confDir);
        }
        Slog.d(TAG, "scan config[" + this.hasConfigFiles + "]");
    }

    private boolean removeCodePath(String path) {
        if (TextUtils.isEmpty(path)) {
            Slog.e(TAG, "invalid path[" + path + "]");
            return UPDATED_FAILURE;
        }
        File codePath = new File(path);
        if (!codePath.exists()) {
            return UPDATED_FAILURE;
        }
        this.mDataUpdaterCallback.removeCodePath(codePath);
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00bf, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00c0, code lost:
        $closeResource(r8, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00c3, code lost:
        throw r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00c6, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00c7, code lost:
        $closeResource(r1, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00ca, code lost:
        throw r8;
     */
    public boolean copyPackageCodeOnly(String src, String dest) {
        String str;
        if (TextUtils.isEmpty(src) || TextUtils.isEmpty(dest)) {
            Slog.e(TAG, src == null ? "src is empty." : "dest is empty.");
            return UPDATED_FAILURE;
        }
        File srcFile = new File(src);
        if (!srcFile.exists()) {
            Slog.e(TAG, "copyPackageCodeOnly::" + src + " not exists.");
            return UPDATED_FAILURE;
        }
        File destDir = new File(dest);
        if (!destDir.exists()) {
            try {
                Os.mkdir(destDir.getAbsolutePath(), 505);
                Os.chmod(destDir.getPath(), 505);
            } catch (ErrnoException e) {
                e.printStackTrace();
            }
            if (!destDir.exists()) {
                Slog.e(TAG, "create " + destDir.getPath() + " failed.");
                return UPDATED_FAILURE;
            }
        }
        File destFile = new File(destDir.getPath() + File.separatorChar + srcFile.getName());
        try {
            InputStream fileIn = new FileInputStream(srcFile);
            OutputStream fileOut = new FileOutputStream(destFile, (boolean) UPDATED_FAILURE);
            Streams.copy(fileIn, fileOut);
            Os.chmod(destFile.getAbsolutePath(), 420);
            $closeResource(null, fileOut);
            $closeResource(null, fileIn);
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (ErrnoException e3) {
            e3.printStackTrace();
        }
        boolean success = destFile.exists();
        StringBuilder sb = new StringBuilder();
        sb.append("copyPackageCodeOnly from ");
        sb.append(srcFile);
        sb.append(" to ");
        sb.append(destFile.getPath());
        if (success) {
            str = " successful.";
        } else {
            str = " failed.";
        }
        sb.append(str);
        Slog.w(TAG, sb.toString());
        if (!success) {
            Slog.e(TAG, "copyPackageCodeOnly from " + srcFile + " to " + destFile.getPath() + " failed.");
        }
        return success;
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public void addManualPackageOperationState(String pkg, boolean installed) {
        if (UserUninstallRecorder.getInstance() != null) {
            UserUninstallRecorder.getInstance().addManualOperatedPackage(pkg, installed);
        }
    }

    public boolean isPkgUninstalledByUser(String pkg) {
        if (UserUninstallRecorder.getInstance() != null) {
            return UserUninstallRecorder.getInstance().isPkgUninstalledByUser(pkg);
        }
        return UPDATED_FAILURE;
    }

    public void setCallback(DataUpdaterCallback callback) {
        if (callback == null) {
            Slog.e(TAG, "callback is null, check it please.");
        } else {
            this.mDataUpdaterCallback = callback;
        }
    }

    /* JADX WARN: Type inference failed for: r3v0 */
    /* JADX WARN: Type inference failed for: r3v1, types: [int, boolean] */
    /* JADX WARN: Type inference failed for: r3v11 */
    public void update() {
        if (!this.isBootFromOTAMode) {
            Slog.w(TAG, "not boot from ota mode");
            return;
        }
        ? r3 = 1;
        if (this.mDataUpdaterCallback == null) {
            Slog.e(TAG, "mDataUpdaterCallback is null, can't update.");
            this.mDataUpdateStatePersistence.end(true);
        } else if (!needUpdate()) {
            Slog.e(TAG, "not need update data.");
            this.mDataUpdateStatePersistence.end(true);
        } else {
            long start = System.currentTimeMillis();
            reparseAllPackages();
            for (DataUpdatePackage pkg : this.mNeedDeletePackages) {
                pkg.state.pkgState = 1;
                if (removePackage(pkg)) {
                    this.mDataUpdaterCallback.removePackageSetting(pkg.packageName);
                    pkg.state.pkgState = 2;
                    pkg.state.pkgUpdated = true;
                } else {
                    pkg.state.pkgState = 2;
                    pkg.state.pkgUpdated = UPDATED_FAILURE;
                    pkg.state.pkgReason = 3;
                    this.mFailedPackages.add(pkg);
                    Slog.e(TAG, "remove " + pkg.apkPath + " failed.");
                }
                this.mDataUpdateStatePersistence.persistence();
            }
            for (DataUpdatePackage pkg2 : this.mNeedUpdatePackages) {
                pkg2.state.pkgState = r3;
                if (!removePackage(pkg2)) {
                    pkg2.state.pkgState = 2;
                    pkg2.state.pkgUpdated = UPDATED_FAILURE;
                    pkg2.state.pkgReason = 3;
                    this.mFailedPackages.add(pkg2);
                    Slog.e(TAG, "remove " + pkg2.apkPath + " failed.");
                } else if (copyPackage(pkg2)) {
                    pkg2.state.pkgState = 2;
                    pkg2.state.pkgUpdated = r3;
                    deleteDataUpdatePatchApk(pkg2.filePath);
                } else {
                    pkg2.state.pkgState = 2;
                    pkg2.state.pkgUpdated = UPDATED_FAILURE;
                    pkg2.state.pkgReason = 4;
                    this.mFailedPackages.add(pkg2);
                    Slog.e(TAG, "copy " + pkg2.filePath + " failed.");
                }
                this.mDataUpdateStatePersistence.persistence();
                r3 = 1;
            }
            for (DataUpdatePackage pkg3 : this.mNeedAddPackages) {
                pkg3.state.pkgState = 1;
                if (copyPackage(pkg3)) {
                    pkg3.state.pkgState = 2;
                    pkg3.state.pkgUpdated = true;
                    deleteDataUpdatePatchApk(pkg3.filePath);
                } else {
                    pkg3.state.pkgState = 2;
                    pkg3.state.pkgUpdated = UPDATED_FAILURE;
                    pkg3.state.pkgReason = 4;
                    this.mFailedPackages.add(pkg3);
                    Slog.e(TAG, "copy " + pkg3.filePath + " failed.");
                }
                this.mDataUpdateStatePersistence.persistence();
            }
            if (this.mFailedPackages.isEmpty()) {
                this.mDataUpdateStatePersistence.end(true);
            } else {
                this.mDataUpdateStatePersistence.end(UPDATED_FAILURE);
            }
            Slog.d(TAG, "PMS data update take " + (System.currentTimeMillis() - start) + "ms.");
        }
    }

    private void updateConfigFiles() {
        if (this.hasConfigFiles) {
            copyConfigFiles();
        }
    }

    /* access modifiers changed from: private */
    public void cleanup() {
        if (SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) UPDATED_FAILURE)) {
            ensureTempFilesDir();
            dumpToFile(DATA_UPDATE_ROOT + File.separatorChar + DATA_UPDATE_DUMP_FILE);
            backupTempFiles();
            cleanDataUpdatePatch(UPDATED_FAILURE);
            return;
        }
        deleteTempFiles();
        cleanDataUpdatePatch(true);
    }

    private void backupTempFiles() {
        File dstDir = new File(BACKUP_TEMP_FILES_PATH + File.separator + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime()));
        if (!dstDir.exists()) {
            dstDir.mkdirs();
        }
        if (dstDir.exists()) {
            try {
                Os.chmod(dstDir.getPath(), 511);
            } catch (ErrnoException e) {
                e.printStackTrace();
            }
            backupTempFile(DATA_UPDATE_ROOT + File.separator + DATA_UPDATE_DUMP_FILE, dstDir.getPath() + File.separator + DATA_UPDATE_DUMP_FILE);
            backupTempFile(DATA_UPDATE_ROOT + File.separator + DATA_UPDATE_STATE_FILE, dstDir.getPath() + File.separator + DATA_UPDATE_STATE_FILE);
            return;
        }
        Slog.e(TAG, "create " + dstDir.getPath() + " failed.");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004b, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004c, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004f, code lost:
        throw r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0052, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0053, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0056, code lost:
        throw r4;
     */
    private void backupTempFile(String src, String dst) {
        if (src == null || dst == null) {
            Slog.d(TAG, "invalid path, src[" + src + "], dst[" + dst + "]");
            return;
        }
        File srcFile = new File(src);
        if (!srcFile.exists()) {
            Slog.e(TAG, src + " not exists.");
            return;
        }
        File dstFile = new File(dst);
        try {
            InputStream fileIn = new FileInputStream(srcFile);
            OutputStream fileOut = new FileOutputStream(dstFile, (boolean) UPDATED_FAILURE);
            Streams.copy(fileIn, fileOut);
            Os.chmod(dst, 511);
            $closeResource(null, fileOut);
            $closeResource(null, fileIn);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ErrnoException e2) {
            e2.printStackTrace();
        }
        srcFile.delete();
    }

    private void deleteTempFiles() {
        File dumpFile = new File(DATA_UPDATE_ROOT + File.separator + DATA_UPDATE_DUMP_FILE);
        if (dumpFile.exists()) {
            dumpFile.delete();
            if (dumpFile.exists()) {
                Slog.e(TAG, "delete " + dumpFile.getPath() + " failed.");
            }
        }
        File stateFile = new File(DATA_UPDATE_ROOT + File.separator + DATA_UPDATE_STATE_FILE);
        if (stateFile.exists()) {
            stateFile.delete();
            if (stateFile.exists()) {
                Slog.e(TAG, "delete " + stateFile.getPath() + " failed.");
            }
        }
    }

    private void ensureTempFilesDir() {
        File file = new File(BACKUP_TEMP_FILES_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (file.exists()) {
            try {
                Os.chmod(BACKUP_TEMP_FILES_PATH, 511);
            } catch (ErrnoException e) {
                e.printStackTrace();
            }
        } else {
            Slog.e(TAG, "create /data/oppo/common/ota failed.");
        }
    }

    private boolean removePackage(DataUpdatePackage pkg) {
        boolean removeSuccess;
        int retry = 3;
        do {
            removeCodePath(pkg.apkPath);
            removeSuccess = !hasValidFile(new File(pkg.apkPath));
            retry--;
            if (retry <= 0) {
                break;
            }
        } while (!removeSuccess);
        Slog.d(TAG, "retry[" + retry + "], removeSuccess[" + removeSuccess + "]");
        return removeSuccess;
    }

    private boolean copyPackage(DataUpdatePackage pkg) {
        boolean copySuccess;
        int retry = 3;
        do {
            copySuccess = copyPackageCodeOnly(pkg.filePath, pkg.apkPath);
            retry--;
            if (copySuccess) {
                break;
            }
        } while (retry >= 0);
        Slog.d(TAG, "retry[" + retry + "], copySuccess[" + copySuccess + "]");
        return copySuccess;
    }

    private void cleanDataUpdatePatch(boolean delete) {
        File file = new File(DATA_UPDATE_PATCH_ROOT);
        if (delete) {
            deleteFileOrDir(file);
        } else if (file.exists()) {
            long suffix = System.currentTimeMillis();
            file.renameTo(new File("/data/oppo/coloros/OTA/data_update_patch." + suffix));
        }
        if (file.exists()) {
            Slog.e(TAG, "cleanup failed.");
        }
    }

    private boolean reparseAllPackages() {
        checkDeletePackages();
        checkAddPackages();
        if (this.mNeedDeletePackages.size() > 0 || this.mNeedUpdatePackages.size() > 0 || this.mNeedAddPackages.size() > 0) {
            return true;
        }
        return UPDATED_FAILURE;
    }

    private void checkDeletePackages() {
        for (DataUpdatePackage pkg : this.mDeletePackages) {
            checkDeletePackage(pkg);
        }
    }

    private void checkAddPackages() {
        for (DataUpdatePackage pkg : this.mAddPackages) {
            checkAddPackage(pkg);
        }
    }

    private void checkDeletePackage(DataUpdatePackage pkg) {
        if (pkg == null || TextUtils.isEmpty(pkg.packageName)) {
            Slog.e(TAG, "checkDeletePackages::pkg is invalid, ignore it");
            return;
        }
        PackageSetting packageSetting = this.mDataUpdaterCallback.getPackageSetting(pkg.packageName);
        if (packageSetting != null) {
            Slog.i(TAG, "checkDeletePackage()::remove package [" + pkg.packageName + ", " + packageSetting.codePathString + "].");
            if (packageSetting.isSystem()) {
                pkg.state = updatePackageDataUpdateState(pkg.packageName, packageSetting.codePathString, 2, 2, UPDATED_FAILURE, 6);
            } else {
                pkg.needUpdate = true;
                pkg.apkPath = packageSetting.codePathString;
                pkg.state = updatePackageDataUpdateState(pkg.packageName, pkg.apkPath, 2, 0, UPDATED_FAILURE, 0);
                this.mNeedDeleteCmds.add(pkg.apkPath);
                this.mNeedDeletePackages.add(pkg);
            }
            this.mDataUpdateStatePersistence.persistence();
            return;
        }
        this.mInvalidDeletePackages.add(pkg.packageName);
    }

    private void checkAddPackage(DataUpdatePackage pkg) {
        String destPath;
        boolean success = UPDATED_FAILURE;
        File file = new File(pkg.filePath);
        if (!file.exists() || !file.canRead()) {
            StringBuilder sb = new StringBuilder();
            sb.append("checkAddPackages::");
            sb.append(pkg.filePath);
            sb.append(!file.exists() ? " not exists." : " can't readable.");
            Slog.e(TAG, sb.toString());
            return;
        }
        PackageParser.PackageLite parsedPackage = parsePackageLite(file);
        if (parsedPackage == null || TextUtils.isEmpty(parsedPackage.packageName)) {
            this.mInvalidAddPackages.add(pkg.filePath);
            Slog.e(TAG, "parse " + pkg.filePath + " failed");
            return;
        }
        String systemPackage = this.mDataUpdaterCallback.getPackage(parsedPackage.packageName);
        String packageSetting = this.mDataUpdaterCallback.getPackageSetting(parsedPackage.packageName);
        Slog.w(TAG, "parsedPackage[" + parsedPackage + "], packageSetting[" + ((Object) packageSetting) + "].");
        if (packageSetting != null) {
            Slog.i(TAG, "packageSetting.versionCode[" + ((PackageSetting) packageSetting).versionCode + "], parsedPackage.versionCode[" + parsedPackage.versionCode + "].");
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("systemPackage[");
        String str = "null";
        sb2.append((Object) (systemPackage != null ? systemPackage : str));
        sb2.append("]");
        Slog.d(TAG, sb2.toString());
        StringBuilder sb3 = new StringBuilder();
        sb3.append("packageSetting[");
        sb3.append((Object) (packageSetting != null ? packageSetting : str));
        sb3.append("]");
        Slog.d(TAG, sb3.toString());
        pkg.packageName = parsedPackage.packageName;
        if (systemPackage != null) {
            pkg.state = updatePackageDataUpdateState(pkg.packageName, pkg.filePath, 0, 2, UPDATED_FAILURE, 1);
        } else {
            ArraySet<String> arraySet = this.mDataPackageNameList;
            if (arraySet != null && arraySet.size() > 0 && !this.mDataPackageNameList.contains(pkg.packageName)) {
                Slog.e(TAG, "package " + pkg.packageName + " not exists in preset data app list.");
                pkg.state = updatePackageDataUpdateState(pkg.packageName, pkg.filePath, 0, 2, UPDATED_FAILURE, 8);
                return;
            } else if (packageSetting != null) {
                if (((long) parsedPackage.versionCode) >= ((PackageSetting) packageSetting).versionCode) {
                    Slog.i(TAG, "checkAddPackages()::update package [" + pkg.packageName + ", " + pkg.filePath + "].");
                    pkg.needUpdate = true;
                    pkg.apkPath = ((PackageSetting) packageSetting).codePathString;
                    pkg.state = updatePackageDataUpdateState(pkg.packageName, pkg.filePath, 1, 0, UPDATED_FAILURE, 0);
                    success = true;
                    this.mNeedDeleteCmds.add(pkg.apkPath);
                    this.mNeedUpdatePackages.add(pkg);
                } else {
                    pkg.state = updatePackageDataUpdateState(pkg.packageName, pkg.filePath, 0, 2, UPDATED_FAILURE, 2);
                }
            } else if (isPkgUninstalledByUser(pkg.packageName)) {
                Slog.e(TAG, "package " + pkg.packageName + " has uninstalled by user, we can't add it.");
                pkg.state = updatePackageDataUpdateState(pkg.packageName, pkg.filePath, 0, 2, UPDATED_FAILURE, 5);
                return;
            } else {
                String apkName = file.getName();
                StringBuilder sb4 = new StringBuilder();
                sb4.append("apkName[");
                sb4.append(apkName == null ? str : apkName);
                sb4.append("].");
                Slog.i(TAG, sb4.toString());
                if (apkName == null || !apkName.contains(".apk")) {
                    destPath = null;
                } else {
                    String apkNameWithoutSuffix = apkName.substring(0, apkName.indexOf(".apk"));
                    Slog.i(TAG, "apkNameWithoutSuffix[" + apkNameWithoutSuffix + "].");
                    destPath = Environment.getDataAppDirectory(null) + File.separator + apkNameWithoutSuffix;
                }
                StringBuilder sb5 = new StringBuilder();
                sb5.append("destPath[");
                if (destPath != null) {
                    str = destPath;
                }
                sb5.append(str);
                sb5.append("].");
                Slog.i(TAG, sb5.toString());
                success = true;
                pkg.needUpdate = true;
                pkg.apkPath = destPath;
                pkg.state = updatePackageDataUpdateState(pkg.packageName, pkg.filePath, 0, 0, UPDATED_FAILURE, 0);
                this.mNeedAddPackages.add(pkg);
            }
        }
        this.mDataUpdateStatePersistence.persistence();
        Slog.d(TAG, pkg.filePath + "[" + success + "]");
    }

    private void copyConfigFiles() {
        copyFile(DATA_UPDATE_PATCH_CONF_ENGINEERMODE, DATA_ENGINEERMODE_DIR);
        copyFile(DATA_UPDATE_PATCH_CONF_ETC, DATA_ETC_DIR);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0130, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0131, code lost:
        $closeResource(r0, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0134, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0137, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0138, code lost:
        $closeResource(r0, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x013b, code lost:
        throw r2;
     */
    private void copyFile(String src, String dst) {
        if (src == null || src.isEmpty() || dst == null || dst.isEmpty()) {
            Slog.e(TAG, "invalid path:src[" + src + "], dst[" + dst + "]");
            return;
        }
        Slog.d(TAG, "copy " + src + " to " + dst);
        File srcFile = new File(src);
        if (srcFile.exists()) {
            File dstFile = new File(dst);
            if (srcFile.isDirectory()) {
                if (!dstFile.exists()) {
                    dstFile.mkdirs();
                }
                if (dstFile.exists()) {
                    String[] subFileList = srcFile.list();
                    for (String sub : subFileList) {
                        copyFile(srcFile.getPath() + File.separator + sub, dstFile.getPath() + File.separator + sub);
                    }
                    return;
                }
                Slog.e(TAG, "create " + dstFile.getPath() + " failed.");
            } else if (srcFile.isFile()) {
                if (!srcFile.canRead()) {
                    Slog.e(TAG, src + " can't read.");
                }
                if (!dstFile.canWrite()) {
                    Slog.e(TAG, dst + " can't write.");
                }
                try {
                    InputStream fileIn = new FileInputStream(srcFile);
                    OutputStream fileOut = new FileOutputStream(dstFile, (boolean) UPDATED_FAILURE);
                    Streams.copy(fileIn, fileOut);
                    Os.chmod(dstFile.getAbsolutePath(), 420);
                    Slog.d(TAG, "copy " + src + " to " + dst + "success.");
                    $closeResource(null, fileOut);
                    $closeResource(null, fileIn);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ErrnoException e2) {
                    e2.printStackTrace();
                }
            }
        } else {
            Slog.e(TAG, srcFile.getPath() + " not exists.");
        }
    }

    private PackageDataUpdateState updatePackageDataUpdateState(String name, String path, int type, int state, boolean updated, int reason) {
        PackageDataUpdateState pkgState = this.mPackageStateMap.get(name);
        if (pkgState == null) {
            PackageDataUpdateState pkgState2 = new PackageDataUpdateState(name, path, type, state, updated, reason);
            this.mPackageStateMap.put(name, pkgState2);
            return pkgState2;
        }
        pkgState.pkgType = type;
        pkgState.pkgState = state;
        pkgState.pkgUpdated = updated;
        pkgState.pkgReason = reason;
        return pkgState;
    }

    private boolean hasValidFile(File file) {
        if (!file.exists()) {
            Slog.e(TAG, file.getPath() + " not exists");
            return UPDATED_FAILURE;
        } else if (!file.canRead()) {
            Slog.e(TAG, file.getPath() + " not readable");
            return UPDATED_FAILURE;
        } else {
            Slog.d(TAG, file.getPath() + " exist.");
            if (file.isDirectory()) {
                File[] subFiles = file.listFiles();
                if (subFiles != null && subFiles.length > 0) {
                    for (File sub : subFiles) {
                        if (hasValidFile(sub)) {
                            return true;
                        }
                    }
                }
            } else if (file.isFile()) {
                return true;
            }
            return UPDATED_FAILURE;
        }
    }

    private boolean readFlag() {
        File flag = new File(DATA_UPDATE_ROOT + File.separatorChar + DATA_UPDATE_FLAG_FILE);
        if (!flag.exists()) {
            return UPDATED_FAILURE;
        }
        String value = null;
        try {
            value = new BufferedReader(new FileReader(flag)).readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        if (value == null || this.mDataUpdateStatePersistence.mFlag == null || value.length() <= 0) {
            Slog.d(TAG, "flag[" + value + "], state[" + this.mDataUpdateStatePersistence.mFlag + "]");
            return UPDATED_FAILURE;
        } else if (!value.equals(this.mDataUpdateStatePersistence.mFlag)) {
            return UPDATED_FAILURE;
        } else {
            Slog.w(TAG, "update failed last one.");
            return true;
        }
    }

    private void generateFlag() {
        long magic = System.currentTimeMillis();
        File flag = new File(DATA_UPDATE_ROOT + File.separatorChar + DATA_UPDATE_FLAG_FILE);
        if (!flag.exists()) {
            try {
                flag.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!flag.exists()) {
            Slog.e(TAG, "create " + flag.getPath() + " failed.");
            return;
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(flag));
            writer.write(magic + "");
            writer.flush();
            writer.close();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        DataUpdateStatePersistence dataUpdateStatePersistence = this.mDataUpdateStatePersistence;
        String unused = dataUpdateStatePersistence.mFlag = magic + "";
        this.mDataUpdateStatePersistence.persistence();
    }

    /* access modifiers changed from: private */
    public void resetFlag() {
        File flag = new File(DATA_UPDATE_ROOT + File.separatorChar + DATA_UPDATE_FLAG_FILE);
        if (flag.exists()) {
            flag.delete();
            if (flag.exists()) {
                Slog.e(TAG, "delete " + flag.getPath() + " failed");
            }
        }
    }

    private boolean deleteDataUpdatePatchApk(String path) {
        Slog.d(TAG, "deleteDataUpdatePatchApk::path[" + path + "]");
        if (path == null || !path.startsWith(DATA_UPDATE_PATCH_APP)) {
            Slog.e(TAG, "invalid path[" + path + "]");
            return UPDATED_FAILURE;
        }
        File file = new File(path);
        file.delete();
        if (file.exists()) {
            Slog.e(TAG, "delete " + file.getPath() + " failed.");
            return UPDATED_FAILURE;
        }
        Slog.d(TAG, "delete " + file.getPath() + " successful.");
        return true;
    }

    private void deleteFileOrDir(File file) {
        if (file == null) {
            Slog.e(TAG, "file is null, ignored");
        } else if (!file.exists()) {
            Slog.e(TAG, file.getPath() + " not exists, ignored.");
        } else if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File subFile : subFiles) {
                    deleteFileOrDir(subFile);
                }
            }
            file.delete();
            if (file.exists()) {
                Slog.e(TAG, "delete directory " + file.getPath() + " failed.");
            }
        } else if (file.isFile()) {
            file.delete();
            if (file.exists()) {
                Slog.e(TAG, "delete file " + file.getPath() + " failed.");
            }
        }
    }

    public PackageParser.PackageLite parsePackageLite(File codePath) {
        Slog.w(TAG, "parsePackageLite::file[" + codePath.getPath() + "].");
        try {
            return PackageParser.parsePackageLite(codePath, 0);
        } catch (PackageParser.PackageParserException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void dumpToFile(String path) {
        if (path == null || path.isEmpty()) {
            Slog.e(TAG, "path error[" + path + "]");
            return;
        }
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(new File(path)));
            BufferedWriter writer = new BufferedWriter(pw);
            dump(pw, null);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dump(PrintWriter pw, String[] args) {
        pw.println("OppoDataUpdater :");
        pw.println("  Version[2.0.1]");
        pw.println("");
        if (UserUninstallRecorder.getInstance() != null) {
            List<String> list = UserUninstallRecorder.getInstance().getUninstalledList();
            if (list != null && list.size() > 0) {
                pw.println("  Uninstalled By User:");
                Iterator<String> it = list.iterator();
                while (it.hasNext()) {
                    pw.println("    " + it.next());
                }
            }
            pw.println("");
        }
        ArraySet<String> arraySet = this.mDataPackageNameList;
        if (arraySet != null && !arraySet.isEmpty()) {
            pw.println("  Data Package Name List:");
            Iterator<String> it2 = this.mDataPackageNameList.iterator();
            while (it2.hasNext()) {
                pw.println("    " + it2.next());
            }
            pw.println("");
        }
        pw.println("  isBootFromOTAMode[" + this.isBootFromOTAMode + "]");
        if (this.isBootFromOTAMode) {
            pw.println("  isLastFailed[" + this.isLastFailed + "]");
            pw.println("  mState[" + this.mDataUpdateStatePersistence.mState + "]");
            pw.println("  mResult[" + this.mDataUpdateStatePersistence.mResult + "]");
            pw.println("  mFlag[" + this.mDataUpdateStatePersistence.mFlag + "]");
            if (this.hasConfigFiles) {
                pw.println("  mConfigState[" + this.mDataUpdateStatePersistence.mConfigState + "]");
            }
            pw.println("");
            if (this.mPackages.size() > 0) {
                pw.println("  Packages :");
                pw.println("    total count[" + this.mPackages.size() + "]");
                if (this.mAddPackages.size() > 0) {
                    pw.println("    apk count[" + this.mAddPackages.size() + "]");
                    Iterator<DataUpdatePackage> it3 = this.mAddPackages.iterator();
                    while (it3.hasNext()) {
                        pw.println("      ota apk path = " + it3.next().filePath);
                    }
                    pw.println("");
                }
                if (this.mDeletePackages.size() > 0) {
                    pw.println("    delete count[" + this.mDeletePackages.size() + "]");
                    Iterator<DataUpdatePackage> it4 = this.mDeletePackages.iterator();
                    while (it4.hasNext()) {
                        pw.println("      packageName = " + it4.next().packageName);
                    }
                    pw.println("");
                }
                for (PackageDataUpdateState state : this.mPackageStateMap.values()) {
                    pw.println("    name[" + state.pkgName + "]");
                    pw.println("    path[" + state.pkgPath + "]");
                    pw.println("    type[" + state.pkgType + "]");
                    pw.println("    state[" + state.pkgState + "]");
                    if (state.pkgState == 2) {
                        pw.println("    updated[" + Boolean.toString(state.pkgUpdated) + "]");
                        if (!state.pkgUpdated) {
                            pw.println("    reason[" + Integer.toString(state.pkgReason) + "]");
                        }
                    }
                    pw.println("");
                }
            }
            if (this.mNeedDeletePackages.size() > 0) {
                pw.println("  Delete Packages:");
                pw.println("    size[" + this.mNeedDeletePackages.size() + "]");
                for (DataUpdatePackage dup : this.mNeedDeletePackages) {
                    pw.println("      packageName = " + dup.packageName + ", apk path = " + dup.apkPath);
                }
                pw.println("");
            }
            if (this.mNeedUpdatePackages.size() > 0) {
                pw.println("  Update Packages:");
                pw.println("    size[" + this.mNeedUpdatePackages.size() + "]");
                for (DataUpdatePackage dup2 : this.mNeedUpdatePackages) {
                    pw.println("      packageName = " + dup2.packageName + ", ota apk path = " + dup2.filePath);
                }
                pw.println("");
            }
            if (this.mNeedAddPackages.size() > 0) {
                pw.println("  Add Packages:");
                pw.println("    size[" + this.mNeedAddPackages.size() + "]");
                for (DataUpdatePackage dup3 : this.mNeedAddPackages) {
                    pw.println("      packageName = " + dup3.packageName + ", ota apk path = " + dup3.filePath);
                }
                pw.println("");
            }
            if (this.mFailedPackages.size() > 0) {
                pw.println("  Failed Packages:");
                pw.println("    size[" + this.mFailedPackages.size() + "]");
                for (DataUpdatePackage dup4 : this.mFailedPackages) {
                    pw.println("      packageName = " + dup4.packageName + ", reason = " + getReasonString(dup4.state.pkgReason));
                }
                pw.println("");
            }
            if (this.mInvalidAddPackages.size() > 0) {
                pw.println("  Invalid Add Packages:");
                pw.println("    size[" + this.mInvalidAddPackages.size() + "]");
                Iterator<String> it5 = this.mInvalidAddPackages.iterator();
                while (it5.hasNext()) {
                    pw.println("      ota package path = " + it5.next());
                }
                pw.println("");
            }
            if (this.mInvalidDeletePackages.size() > 0) {
                pw.println("  Invalid Delete Packages:");
                pw.println("    size[" + this.mInvalidDeletePackages.size() + "]");
                Iterator<String> it6 = this.mInvalidDeletePackages.iterator();
                while (it6.hasNext()) {
                    pw.println("      packageName = " + it6.next());
                }
                pw.println("");
            }
            if (this.mNeedDeleteCmds.size() > 0) {
                pw.println("  Need Remove Path:");
                pw.println("    size[" + this.mNeedDeleteCmds.size() + "]");
                Iterator<String> it7 = this.mNeedDeleteCmds.iterator();
                while (it7.hasNext()) {
                    pw.println("    apk path = " + it7.next());
                }
                pw.println("");
            }
            if (this.mUpdateFailedPackages.size() > 0) {
                pw.println("  Update failed package:");
                pw.println("    size[" + this.mUpdateFailedPackages.size() + "]");
                Iterator<String> it8 = this.mUpdateFailedPackages.iterator();
                while (it8.hasNext()) {
                    pw.println("    package : " + it8.next());
                }
                pw.println("");
            }
        }
    }

    /* access modifiers changed from: private */
    public void generateReport() {
        StringBuilder builder = new StringBuilder();
        builder.append("success:[" + this.mDataUpdateStatePersistence.isSuccess() + "]");
        builder.append("\n");
        builder.append("is_last_failed:[" + this.isLastFailed + "]");
        builder.append("\n");
        builder.append("total_count:[" + this.mPackages.size() + "]");
        builder.append("\n");
        if (this.mAddPackages.size() > 0) {
            builder.append("add_pkg_count:[" + this.mAddPackages.size() + "]");
            builder.append("\n");
        }
        if (this.mDeletePackages.size() > 0) {
            builder.append("delete_pkg_count:[" + this.mDeletePackages.size() + "]");
            builder.append("\n");
        }
        if (this.mNeedAddPackages.size() > 0) {
            builder.append("need_add_pkg_count:[" + this.mNeedAddPackages.size() + "]");
            builder.append("\n");
        }
        if (this.mNeedUpdatePackages.size() > 0) {
            builder.append("need_update_pkg_count:[" + this.mNeedUpdatePackages.size() + "]");
            builder.append("\n");
        }
        if (this.mNeedDeletePackages.size() > 0) {
            builder.append("need_delete_pkg_count:[" + this.mNeedDeletePackages.size() + "]");
            builder.append("\n");
        }
        if (this.mInvalidAddPackages.size() > 0) {
            builder.append("invalid_add_pkg_count:[" + this.mInvalidAddPackages.size() + "]");
            builder.append("\n");
            builder.append("invalid_add_pkgs:");
            Iterator<String> it = this.mInvalidAddPackages.iterator();
            while (it.hasNext()) {
                builder.append("[" + it.next() + "]");
            }
            builder.append("\n");
        }
        if (this.mInvalidDeletePackages.size() > 0) {
            builder.append("invalid_delete_pkg_count:[" + this.mInvalidDeletePackages.size() + "]");
            builder.append("\n");
            builder.append("invalid_delete_pkgs:");
            Iterator<String> it2 = this.mInvalidDeletePackages.iterator();
            while (it2.hasNext()) {
                builder.append("[" + it2.next() + "]");
            }
            builder.append("\n");
        }
        if (this.mFailedPackages.size() > 0) {
            builder.append("failed_pkg_count:[" + this.mFailedPackages.size() + "]");
            builder.append("\n");
            builder.append("failed_pkgs:");
            for (DataUpdatePackage pkg : this.mFailedPackages) {
                if (!(pkg == null || pkg.state == null)) {
                    builder.append("[" + pkg.state.pkgName + ", " + getStateString(pkg.state.pkgState) + ", " + pkg.state.pkgUpdated + ", " + getReasonString(pkg.state.pkgReason) + "]");
                }
            }
            builder.append("\n");
        }
        if (this.mPackageStateMap.size() > 0) {
            builder.append("not_update:");
            boolean hasNotUpdate = UPDATED_FAILURE;
            for (String name : this.mPackageStateMap.keySet()) {
                PackageDataUpdateState state = this.mPackageStateMap.get(name);
                if (state != null && 2 == state.pkgState && !state.pkgUpdated) {
                    builder.append("[" + state.pkgName + ", " + getReasonString(state.pkgReason) + "]");
                    hasNotUpdate = true;
                }
            }
            if (!hasNotUpdate) {
                builder.append("[none]");
            }
            builder.append("\n");
        }
        if (this.mUpdateFailedPackages.size() > 0) {
            builder.append("update_failed:");
            Iterator<String> it3 = this.mUpdateFailedPackages.iterator();
            while (it3.hasNext()) {
                builder.append("[" + it3.next() + "]");
            }
            builder.append("\n");
        }
        builder.append("has_config_file:[" + this.hasConfigFiles + "]");
        builder.append("\n");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(DATA_UPDATE_REPORT_FILE)));
            writer.write(builder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Os.chmod(DATA_UPDATE_REPORT_FILE, 511);
        } catch (ErrnoException e2) {
            e2.printStackTrace();
        }
        builder.setLength(0);
    }

    private String getStateString(int value) {
        if (value >= 0) {
            String[] strArr = STATE_STRING_VALUES;
            if (value < strArr.length) {
                return strArr[value];
            }
        }
        return STATE_STRING_VALUES[0];
    }

    private String getReasonString(int value) {
        if (value >= 0) {
            String[] strArr = REASON_STRING_VALUES;
            if (value < strArr.length) {
                return strArr[value];
            }
        }
        return REASON_STRING_VALUES[0];
    }

    /* access modifiers changed from: private */
    public void collectUpdateResult() {
        PackageDataUpdateState state;
        Map<String, PackageDataUpdateState> map = this.mPackageStateMap;
        if (map != null && map.size() > 0) {
            for (Map.Entry<String, PackageDataUpdateState> item : this.mPackageStateMap.entrySet()) {
                if (!(item == null || (state = item.getValue()) == null)) {
                    if ((state.pkgType == 0 || state.pkgType == 1) && 2 == state.pkgState && state.pkgUpdated && !TextUtils.isEmpty(state.pkgName)) {
                        PackageParser.Package pkg = null;
                        DataUpdaterCallback dataUpdaterCallback = this.mDataUpdaterCallback;
                        if (dataUpdaterCallback != null) {
                            pkg = dataUpdaterCallback.getPackage(state.pkgName);
                        }
                        if (pkg == null) {
                            Slog.e(TAG, "package " + state.pkgName + " upgrade failed.");
                            this.mUpdateFailedPackages.add(state.pkgName);
                        }
                    }
                }
            }
        }
        if (this.mUpdateFailedPackages.size() > 0) {
            Slog.e(TAG, this.mUpdateFailedPackages.size() + " packages update failed.");
        }
    }

    private final class ManualPkgOpPersistence {
        private static final String ATTR_NAME = "name";
        private static final String MANUAL_PACKAGE_OPERATION_FILE = "/data/oppo/coloros/dataupdate/manual_package_operation.xml";
        private static final int MSG_WHAT = 20180925;
        private static final String TAG_MANUAL_STATE = "manual-state";
        private static final String TAG_PACKAGE = "package";
        private static final String TAG_PACKAGE_UNINSTALLED = "package-uninstalled";
        private Handler mHandler = new MyHandler();
        private List<String> mUninstalledPackagesByUser = new ArrayList();

        public ManualPkgOpPersistence() {
            loadManualPackageOperation();
            Iterator<String> it = this.mUninstalledPackagesByUser.iterator();
            while (it.hasNext()) {
                Slog.i(OppoDataUpdater.TAG, "ManualPkgOpPersistence::uninstalled[" + it.next() + "].");
            }
        }

        public boolean isPkgUninstalledByUser(String pkg) {
            return this.mUninstalledPackagesByUser.contains(pkg);
        }

        private void loadManualPackageOperation() {
            StringBuilder sb;
            File operationFile = new File(MANUAL_PACKAGE_OPERATION_FILE);
            if (!operationFile.exists()) {
                Slog.i(OppoDataUpdater.TAG, "manual_package_operation.xml not exist.");
                return;
            }
            try {
                FileInputStream in = new AtomicFile(operationFile).openRead();
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(in, null);
                    parseManualPackageOperation(parser);
                    IoUtils.closeQuietly(in);
                    if (1 == 0) {
                        sb = new StringBuilder();
                        sb.append("parse ");
                        sb.append(operationFile.getPath());
                        sb.append(" failed, delete it.");
                        Slog.e(OppoDataUpdater.TAG, sb.toString());
                        operationFile.delete();
                    }
                } catch (IOException | XmlPullParserException e) {
                    Slog.e(OppoDataUpdater.TAG, "Failed parsing file: " + operationFile, e);
                    IoUtils.closeQuietly(in);
                    if (0 == 0) {
                        sb = new StringBuilder();
                    }
                } catch (Throwable th) {
                    IoUtils.closeQuietly(in);
                    if (1 == 0) {
                        Slog.e(OppoDataUpdater.TAG, "parse " + operationFile.getPath() + " failed, delete it.");
                        operationFile.delete();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e2) {
                Slog.i(OppoDataUpdater.TAG, "No package operation state.");
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:22:0x0046  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x004d  */
        private void parseManualPackageOperation(XmlPullParser parser) throws IOException, XmlPullParserException {
            boolean z;
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4)) {
                    String name = parser.getName();
                    int hashCode = name.hashCode();
                    if (hashCode != 1435800058) {
                        if (hashCode == 1827165130 && name.equals(TAG_MANUAL_STATE)) {
                            z = OppoDataUpdater.UPDATED_FAILURE;
                            if (!z) {
                                Slog.d(OppoDataUpdater.TAG, "parse manual-state tag.");
                            } else if (z) {
                                parsePackage(parser);
                            }
                        }
                    } else if (name.equals(TAG_PACKAGE_UNINSTALLED)) {
                        z = true;
                        if (!z) {
                        }
                    }
                    z = true;
                    if (!z) {
                    }
                }
            }
        }

        private void parsePackage(XmlPullParser parser) throws IOException, XmlPullParserException {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4)) {
                    String name = parser.getName();
                    char c = 65535;
                    if (name.hashCode() == -807062458 && name.equals("package")) {
                        c = 0;
                    }
                    if (c == 0) {
                        String name2 = parser.getAttributeValue(null, "name");
                        Slog.d(OppoDataUpdater.TAG, "parsePackage::name[" + name2 + "].");
                        this.mUninstalledPackagesByUser.add(name2);
                    }
                }
            }
        }

        public void addManualOperatedPackage(String pkg, boolean installed) {
            if (TextUtils.isEmpty(pkg)) {
                Slog.e(OppoDataUpdater.TAG, "pkg is null!");
                return;
            }
            Slog.i(OppoDataUpdater.TAG, "addManualOperatedPackage::pkg[" + pkg + "], installed[" + installed + "].");
            if (installed) {
                if (this.mUninstalledPackagesByUser.contains(pkg)) {
                    this.mUninstalledPackagesByUser.remove(pkg);
                } else {
                    Slog.d(OppoDataUpdater.TAG, "not care " + pkg + " installed.");
                }
            } else if (!this.mUninstalledPackagesByUser.contains(pkg)) {
                this.mUninstalledPackagesByUser.add(pkg);
            } else {
                Slog.e(OppoDataUpdater.TAG, "duplicate uninstall " + pkg + ", please check it.");
            }
            if (this.mHandler.hasMessages(MSG_WHAT)) {
                this.mHandler.removeMessages(MSG_WHAT);
            }
            this.mHandler.sendEmptyMessage(MSG_WHAT);
        }

        /* access modifiers changed from: private */
        public void persistence() {
            AtomicFile destination = new AtomicFile(new File(MANUAL_PACKAGE_OPERATION_FILE));
            FileOutputStream out = null;
            try {
                out = destination.startWrite();
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(out, StandardCharsets.UTF_8.name());
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startDocument(null, true);
                serializer.startTag(null, TAG_MANUAL_STATE);
                serializer.startTag(null, TAG_PACKAGE_UNINSTALLED);
                for (String pkg : this.mUninstalledPackagesByUser) {
                    serializer.startTag(null, "package");
                    serializer.attribute(null, "name", pkg);
                    serializer.endTag(null, "package");
                }
                serializer.endTag(null, TAG_PACKAGE_UNINSTALLED);
                serializer.endTag(null, TAG_MANUAL_STATE);
                serializer.endDocument();
                destination.finishWrite(out);
            } catch (Throwable th) {
                IoUtils.closeQuietly((AutoCloseable) null);
                throw th;
            }
            IoUtils.closeQuietly(out);
        }

        private final class MyHandler extends Handler {
            public MyHandler() {
                super(BackgroundThread.getHandler().getLooper());
            }

            public void handleMessage(Message message) {
                ManualPkgOpPersistence.this.persistence();
            }
        }
    }

    private final class DataUpdateStatePersistence {
        private static final String ATTR_CONFIG_STATE = "state";
        private static final String ATTR_FLAG = "flag";
        private static final String ATTR_INVALID_NAME = "name";
        private static final String ATTR_PACKAGE_NAME = "name";
        private static final String ATTR_PACKAGE_PATH = "path";
        private static final String ATTR_PACKAGE_REASON = "reason";
        private static final String ATTR_PACKAGE_STATE = "state";
        private static final String ATTR_PACKAGE_TYPE = "type";
        private static final String ATTR_PACKAGE_UPDATED = "updated";
        private static final String ATTR_RESULT = "result";
        private static final String ATTR_STATE = "state";
        private static final String DATA_UPDATE_STATE_NAME = "data_update_state";
        private static final int MSG_WHAT = 20180926;
        private static final String TAG_CONFIG = "config";
        private static final String TAG_INVALID_ADD = "invalid-add";
        private static final String TAG_INVALID_DELETE = "invalid-delete";
        private static final String TAG_PACKAGE = "package";
        private static final String TAG_UPDATE_STATE = "update-state";
        private final String DATA_UPDATE_STATE_FILE = (OppoDataUpdater.DATA_UPDATE_ROOT + File.separator + DATA_UPDATE_STATE_NAME + ".xml");
        /* access modifiers changed from: private */
        public int mConfigState = 0;
        /* access modifiers changed from: private */
        public String mFlag = "";
        private Handler mHandler = new MyHandler();
        /* access modifiers changed from: private */
        public int mResult = 0;
        /* access modifiers changed from: private */
        public int mState = 0;
        private boolean needReset = OppoDataUpdater.UPDATED_FAILURE;

        public DataUpdateStatePersistence() {
            loadState();
        }

        public void persistence() {
            if (this.mHandler.hasMessages(MSG_WHAT)) {
                this.mHandler.removeMessages(MSG_WHAT);
            }
            this.mHandler.sendEmptyMessage(MSG_WHAT);
        }

        public void start() {
            this.mState = 1;
            persistence();
        }

        public void end(boolean success) {
            this.mState = 2;
            if (success) {
                this.mResult = 1;
            } else {
                this.mResult = 2;
            }
            persistence();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x003f, code lost:
            if (1 == 0) goto L_0x0041;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0041, code lost:
            r0.delete();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0060, code lost:
            if (0 != 0) goto L_0x0063;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0065, code lost:
            if (r7.needReset == false) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0067, code lost:
            reset();
            backupStateFile();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
            return;
         */
        private void loadState() {
            File stateFile = new File(this.DATA_UPDATE_STATE_FILE);
            if (!stateFile.exists()) {
                Slog.i(OppoDataUpdater.TAG, this.DATA_UPDATE_STATE_FILE + " not exist, first update after ota.");
                return;
            }
            try {
                FileInputStream in = new AtomicFile(stateFile).openRead();
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(in, null);
                    parseDataUpdateState(parser);
                    IoUtils.closeQuietly(in);
                } catch (IOException | XmlPullParserException e) {
                    Slog.e(OppoDataUpdater.TAG, "Failed parsing update state file: " + stateFile, e);
                    IoUtils.closeQuietly(in);
                } catch (Throwable th) {
                    IoUtils.closeQuietly(in);
                    if (1 == 0) {
                        stateFile.delete();
                    }
                    throw th;
                }
            } catch (FileNotFoundException fnfe) {
                Slog.i(OppoDataUpdater.TAG, "no data update state.");
                fnfe.printStackTrace();
            }
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        private void parseDataUpdateState(XmlPullParser parser) throws IOException, XmlPullParserException {
            boolean z;
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4)) {
                    if (!this.needReset) {
                        String name = parser.getName();
                        switch (name.hashCode()) {
                            case -2035275199:
                                if (name.equals(TAG_INVALID_DELETE)) {
                                    z = true;
                                    break;
                                }
                                z = true;
                                break;
                            case -2029021555:
                                if (name.equals(TAG_UPDATE_STATE)) {
                                    z = OppoDataUpdater.UPDATED_FAILURE;
                                    break;
                                }
                                z = true;
                                break;
                            case -1354792126:
                                if (name.equals(TAG_CONFIG)) {
                                    z = true;
                                    break;
                                }
                                z = true;
                                break;
                            case -954332213:
                                if (name.equals(TAG_INVALID_ADD)) {
                                    z = true;
                                    break;
                                }
                                z = true;
                                break;
                            case -807062458:
                                if (name.equals("package")) {
                                    z = true;
                                    break;
                                }
                                z = true;
                                break;
                            default:
                                z = true;
                                break;
                        }
                        if (!z) {
                            parseUpdateState(parser);
                        } else if (z) {
                            parsePackage(parser);
                        } else if (z) {
                            String name2 = parser.getAttributeValue(null, BrightnessConstants.AppSplineXml.TAG_NAME);
                            if (name2 != null && name2.length() > 0) {
                                OppoDataUpdater.this.mInvalidAddPackages.add(name2);
                            }
                        } else if (z) {
                            String name3 = parser.getAttributeValue(null, BrightnessConstants.AppSplineXml.TAG_NAME);
                            if (name3 != null && name3.length() > 0) {
                                OppoDataUpdater.this.mInvalidDeletePackages.add(name3);
                            }
                        } else if (z) {
                            String state = parser.getAttributeValue(null, OppoPermissionInterceptPolicy.COLUMN_STATE_STR);
                            Slog.d(OppoDataUpdater.TAG, "tag[config], state[" + state + ']');
                            if (state == null || !isStateStringValid(state)) {
                                Slog.w(OppoDataUpdater.TAG, "invalid attribute[state, " + state + "] in " + TAG_CONFIG + " tag");
                            } else {
                                this.mConfigState = Integer.parseInt(state);
                            }
                        }
                    } else {
                        return;
                    }
                }
            }
        }

        private void parseUpdateState(XmlPullParser parser) {
            String state = parser.getAttributeValue(null, OppoPermissionInterceptPolicy.COLUMN_STATE_STR);
            Slog.d(OppoDataUpdater.TAG, "tag[update-state], state[" + state + "]");
            boolean z = true;
            if ("0".equals(state)) {
                this.mState = 0;
                this.mResult = 0;
            } else if ("1".equals(state)) {
                this.mState = 1;
                this.mResult = 0;
            } else if ("2".equals(state)) {
                this.mState = 2;
                String result = parser.getAttributeValue(null, ATTR_RESULT);
                Slog.d(OppoDataUpdater.TAG, "tag[update-state], result[" + result + "]");
                if ("1".equals(result)) {
                    this.mResult = 1;
                } else if ("2".equals(result)) {
                    this.mResult = 2;
                }
            } else {
                Slog.e(OppoDataUpdater.TAG, "invalid attribute [state, " + state + "] in tag " + TAG_UPDATE_STATE);
                this.mState = 0;
                this.mResult = 0;
            }
            String flag = parser.getAttributeValue(null, ATTR_FLAG);
            Slog.d(OppoDataUpdater.TAG, "tag[update-state], flag[" + flag + "]");
            if (flag != null) {
                this.mFlag = flag;
            } else {
                this.mFlag = "";
            }
            if (!(this.mState == 2 && this.mResult == 1)) {
                z = false;
            }
            this.needReset = z;
            Slog.i(OppoDataUpdater.TAG, "parseUpdateState::mState[" + this.mState + "], mResult[" + this.mResult + "], mFlag[" + this.mFlag + "]");
        }

        private void parsePackage(XmlPullParser parser) {
            int state;
            String name = parser.getAttributeValue(null, BrightnessConstants.AppSplineXml.TAG_NAME);
            String path = parser.getAttributeValue(null, ATTR_PACKAGE_PATH);
            String typeString = parser.getAttributeValue(null, ATTR_PACKAGE_TYPE);
            String updatedString = parser.getAttributeValue(null, ATTR_PACKAGE_UPDATED);
            String reasonString = parser.getAttributeValue(null, ATTR_PACKAGE_REASON);
            String stateString = parser.getAttributeValue(null, OppoPermissionInterceptPolicy.COLUMN_STATE_STR);
            Slog.d(OppoDataUpdater.TAG, "tag[package], name[" + name + "], " + ATTR_PACKAGE_PATH + "[" + path + "], " + ATTR_PACKAGE_TYPE + "[" + typeString + "], " + ATTR_PACKAGE_UPDATED + "[" + updatedString + "], " + ATTR_PACKAGE_REASON + "[" + reasonString + "], " + OppoPermissionInterceptPolicy.COLUMN_STATE_STR + "[" + stateString + "]");
            if (isTypeStringValid(typeString)) {
                int type = Integer.parseInt(typeString);
                if (isStateStringValid(stateString)) {
                    int state2 = Integer.parseInt(stateString);
                    if (state2 == 0) {
                        state = state2;
                    } else if (state2 == 1) {
                        state = state2;
                    } else if (isUpdatedStringValid(updatedString)) {
                        boolean updated = getUpdatedValue(updatedString);
                        if (updated) {
                            OppoDataUpdater.this.mPackageStateMap.put(name, new PackageDataUpdateState(name, path, type, state2, true, 0));
                            return;
                        } else if (!isReasonStringValid(reasonString)) {
                            loge(ATTR_PACKAGE_UPDATED, updatedString, "package");
                            return;
                        } else if (Integer.parseInt(reasonString) == 0) {
                            loge(ATTR_PACKAGE_UPDATED, updatedString, "package");
                            Slog.e(OppoDataUpdater.TAG, "because attribute updated is " + updated + " now.");
                            return;
                        } else {
                            OppoDataUpdater.this.mPackageStateMap.put(name, new PackageDataUpdateState(name, path, type, state2, OppoDataUpdater.UPDATED_FAILURE, 0));
                            return;
                        }
                    } else {
                        loge(ATTR_PACKAGE_UPDATED, updatedString, "package");
                        return;
                    }
                    if (state == 1) {
                        Slog.e(OppoDataUpdater.TAG, "state is processing, the package is updating when shutdown.");
                    }
                    OppoDataUpdater.this.mPackageStateMap.put(name, new PackageDataUpdateState(name, path, type, state, OppoDataUpdater.UPDATED_FAILURE, 0));
                    return;
                }
                loge(OppoPermissionInterceptPolicy.COLUMN_STATE_STR, stateString, "package");
                return;
            }
            loge(ATTR_PACKAGE_TYPE, typeString, "package");
        }

        /* access modifiers changed from: private */
        public void persistenceDataUpdateState() {
            File file = new File(OppoDataUpdater.DATA_UPDATE_ROOT);
            if (!file.exists()) {
                Slog.e(OppoDataUpdater.TAG, "/data/oppo/coloros/dataupdate not exists");
                return;
            }
            AtomicFile destination = new AtomicFile(new File(this.DATA_UPDATE_STATE_FILE));
            FileOutputStream out = null;
            try {
                out = destination.startWrite();
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(out, StandardCharsets.UTF_8.name());
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startDocument(null, true);
                serializer.startTag(null, TAG_UPDATE_STATE);
                serializer.attribute(null, OppoPermissionInterceptPolicy.COLUMN_STATE_STR, this.mState + "");
                serializer.attribute(null, ATTR_RESULT, this.mResult + "");
                serializer.attribute(null, ATTR_FLAG, this.mFlag + "");
                Iterator it = OppoDataUpdater.this.mPackageStateMap.values().iterator();
                while (it.hasNext()) {
                    PackageDataUpdateState pkg = (PackageDataUpdateState) it.next();
                    try {
                        serializer.startTag(null, "package");
                        serializer.attribute(null, BrightnessConstants.AppSplineXml.TAG_NAME, pkg.pkgName);
                        serializer.attribute(null, ATTR_PACKAGE_PATH, pkg.pkgPath);
                        serializer.attribute(null, ATTR_PACKAGE_TYPE, Integer.toString(pkg.pkgType));
                        serializer.attribute(null, OppoPermissionInterceptPolicy.COLUMN_STATE_STR, Integer.toString(pkg.pkgState));
                        if (pkg.pkgState == 2) {
                            serializer.attribute(null, ATTR_PACKAGE_UPDATED, Boolean.toString(pkg.pkgUpdated));
                            if (!pkg.pkgUpdated) {
                                serializer.attribute(null, ATTR_PACKAGE_REASON, Integer.toString(pkg.pkgReason));
                            }
                        }
                        serializer.endTag(null, "package");
                        file = file;
                        it = it;
                    } catch (Throwable th) {
                        t = th;
                        try {
                            Slog.wtf(OppoDataUpdater.TAG, "Failed to write data_update_state.xml, restoring backup", t);
                            destination.failWrite(out);
                            IoUtils.closeQuietly(out);
                        } catch (Throwable th2) {
                            IoUtils.closeQuietly(out);
                            throw th2;
                        }
                    }
                }
                for (String name : OppoDataUpdater.this.mInvalidAddPackages) {
                    serializer.startTag(null, TAG_INVALID_ADD);
                    serializer.attribute(null, BrightnessConstants.AppSplineXml.TAG_NAME, name);
                    serializer.endTag(null, TAG_INVALID_ADD);
                }
                for (String name2 : OppoDataUpdater.this.mInvalidDeletePackages) {
                    serializer.startTag(null, TAG_INVALID_DELETE);
                    serializer.attribute(null, BrightnessConstants.AppSplineXml.TAG_NAME, name2);
                    serializer.endTag(null, TAG_INVALID_DELETE);
                }
                serializer.startTag(null, TAG_CONFIG);
                serializer.attribute(null, OppoPermissionInterceptPolicy.COLUMN_STATE_STR, this.mConfigState + "");
                serializer.endTag(null, TAG_CONFIG);
                serializer.endTag(null, TAG_UPDATE_STATE);
                serializer.endDocument();
                destination.finishWrite(out);
            } catch (Throwable th3) {
                t = th3;
                Slog.wtf(OppoDataUpdater.TAG, "Failed to write data_update_state.xml, restoring backup", t);
                destination.failWrite(out);
                IoUtils.closeQuietly(out);
            }
            IoUtils.closeQuietly(out);
        }

        private boolean isTypeStringValid(String type) {
            if ("0".equals(type) || "1".equals(type) || "2".equals(type)) {
                return true;
            }
            return OppoDataUpdater.UPDATED_FAILURE;
        }

        private boolean isUpdatedStringValid(String updated) {
            if ("true".equals(updated) || "false".equals(updated)) {
                return true;
            }
            return OppoDataUpdater.UPDATED_FAILURE;
        }

        private boolean getUpdatedValue(String updated) {
            return "true".equals(updated);
        }

        private boolean isStateStringValid(String state) {
            if ("0".equals(state) || "1".equals(state) || "2".equals(state)) {
                return true;
            }
            return OppoDataUpdater.UPDATED_FAILURE;
        }

        private boolean isReasonStringValid(String reason) {
            if ("0".equals(reason) || "1".equals(reason) || "2".equals(reason) || ColorAppStartupManager.RECORD_AUTO_LAUNCH_ALLOW_MODE.equals(reason) || ColorAppStartupManager.RECORD_ASSOCIATE_LAUNCH_ALLOW_MODE.equals(reason) || "5".equals(reason) || "6".equals(reason) || "7".equals(reason)) {
                return true;
            }
            return OppoDataUpdater.UPDATED_FAILURE;
        }

        /* access modifiers changed from: private */
        public boolean isSuccess() {
            if (this.mState == 2 && this.mResult == 1) {
                return true;
            }
            return OppoDataUpdater.UPDATED_FAILURE;
        }

        private void loge(String name, String value, String tag) {
            Slog.e(OppoDataUpdater.TAG, "invalid attribute [" + name + ", " + value + "] in tag " + tag);
        }

        private void reset() {
            this.mState = 0;
            this.mResult = 0;
            this.mFlag = "";
            OppoDataUpdater.this.mPackageStateMap.clear();
            this.mConfigState = 0;
            this.needReset = OppoDataUpdater.UPDATED_FAILURE;
            OppoDataUpdater.this.mInvalidAddPackages.clear();
            OppoDataUpdater.this.mInvalidDeletePackages.clear();
        }

        private void backupStateFile() {
            File file = new File(this.DATA_UPDATE_STATE_FILE);
            if (file.exists()) {
                String backup = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime());
                File destFile = new File(OppoDataUpdater.DATA_UPDATE_ROOT + File.separator + DATA_UPDATE_STATE_NAME + "." + backup + ".xml");
                if (!file.renameTo(destFile)) {
                    Slog.e(OppoDataUpdater.TAG, file.getPath() + " rename to " + destFile.getPath() + " failed.");
                    return;
                }
                return;
            }
            Slog.e(OppoDataUpdater.TAG, file.getPath() + " not exists, can't backup it.");
        }

        private final class MyHandler extends Handler {
            public MyHandler() {
                super(BackgroundThread.getHandler().getLooper());
            }

            public void handleMessage(Message message) {
                DataUpdateStatePersistence.this.persistenceDataUpdateState();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class PackageDataUpdateState {
        String pkgName = null;
        String pkgPath = null;
        int pkgReason = -1;
        int pkgState = 0;
        int pkgType = -1;
        boolean pkgUpdated = OppoDataUpdater.UPDATED_FAILURE;

        public PackageDataUpdateState(String name, String path, int type, int state, boolean updated, int reason) {
            this.pkgName = name;
            this.pkgPath = path;
            this.pkgType = type;
            this.pkgState = state;
            this.pkgUpdated = updated;
            this.pkgReason = reason;
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append(super.toString() + "\n");
            buffer.append("pkgName[" + this.pkgName + "], pkgPath[" + this.pkgPath + "], pkgType[" + this.pkgType + "], pkgState[" + this.pkgState + "], pkgResult[" + this.pkgUpdated + "], pkgReason[" + this.pkgReason + "]");
            return buffer.toString();
        }
    }

    class DataUpdatePackage {
        public String apkPath = null;
        public String filePath = null;
        public boolean needUpdate = OppoDataUpdater.UPDATED_FAILURE;
        public String packageName = null;
        PackageDataUpdateState state = null;
        public DataUpdateType type = DataUpdateType.INVALID;

        DataUpdatePackage(DataUpdateType type2, String filePath2, String packageName2) {
            this.type = type2;
            this.filePath = filePath2;
            this.packageName = packageName2;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(super.toString());
            builder.append("(type[" + this.type + "],filePath[" + this.filePath + "], packageName[" + this.packageName + "], apkPath[" + this.apkPath + "], needUpdate[" + this.needUpdate + "]");
            return builder.toString();
        }
    }

    private class DataUpdaterReceiver extends BroadcastReceiver {
        private DataUpdaterReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.w(OppoDataUpdater.TAG, "action[" + action + "].");
            if (OppoDataUpdater.ACTION_PACKAGE_MANUAL_OPERATION.equals(action)) {
                boolean install = intent.getBooleanExtra("install", OppoDataUpdater.UPDATED_FAILURE);
                String pkg = intent.getStringExtra("pkg");
                if (pkg == null || pkg.length() == 0) {
                    Slog.e(OppoDataUpdater.TAG, "package name is empty.");
                    return;
                }
                Slog.d(OppoDataUpdater.TAG, "install[" + install + "], pkg[" + pkg + "].");
                OppoDataUpdater.this.addManualPackageOperationState(pkg, install);
            }
        }
    }
}
