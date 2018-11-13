package com.android.server.pm;

import android.annotation.IntDef;
import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.IUidObserver;
import android.app.usage.UsageStatsManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.IShortcutService.Stub;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutServiceInternal;
import android.content.pm.ShortcutServiceInternal.ShortcutChangeListener;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.KeyValueListParser;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.util.TypedValue;
import android.util.Xml;
import android.view.IWindowManager;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.SystemService;
import com.android.server.display.OppoBrightUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import libcore.io.IoUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

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
public class ShortcutService extends Stub {
    private static Predicate<ResolveInfo> ACTIVITY_NOT_EXPORTED = null;
    private static final String ATTR_VALUE = "value";
    static final boolean DEBUG = false;
    static final boolean DEBUG_LOAD = false;
    static final boolean DEBUG_PROCSTATE = false;
    static final String DEFAULT_ICON_PERSIST_FORMAT = null;
    static final int DEFAULT_ICON_PERSIST_QUALITY = 100;
    static final int DEFAULT_MAX_ICON_DIMENSION_DP = 96;
    static final int DEFAULT_MAX_ICON_DIMENSION_LOWRAM_DP = 48;
    static final int DEFAULT_MAX_SHORTCUTS_PER_APP = 5;
    static final int DEFAULT_MAX_UPDATES_PER_INTERVAL = 10;
    static final long DEFAULT_RESET_INTERVAL_SEC = 86400;
    static final int DEFAULT_SAVE_DELAY_MS = 3000;
    static final String DIRECTORY_BITMAPS = "bitmaps";
    static final String DIRECTORY_PER_USER = "shortcut_service";
    private static List<ResolveInfo> EMPTY_RESOLVE_INFO = null;
    static final String FILENAME_BASE_STATE = "shortcut_service.xml";
    static final String FILENAME_USER_PACKAGES = "shortcuts.xml";
    private static final String KEY_ICON_SIZE = "iconSize";
    private static final String KEY_LOW_RAM = "lowRam";
    private static final String KEY_SHORTCUT = "shortcut";
    private static final String LAUNCHER_INTENT_CATEGORY = "android.intent.category.LAUNCHER";
    static final int OPERATION_ADD = 1;
    static final int OPERATION_SET = 0;
    static final int OPERATION_UPDATE = 2;
    private static final int PACKAGE_MATCH_FLAGS = 794624;
    private static Predicate<PackageInfo> PACKAGE_NOT_INSTALLED = null;
    private static final int PROCESS_STATE_FOREGROUND_THRESHOLD = 4;
    private static final String[] STAT_LABELS = null;
    static final String TAG = "ShortcutService";
    private static final String TAG_LAST_RESET_TIME = "last_reset_time";
    private static final String TAG_ROOT = "root";
    private final ActivityManagerInternal mActivityManagerInternal;
    private final AtomicBoolean mBootCompleted;
    final Context mContext;
    @GuardedBy("mStatLock")
    private final int[] mCountStats;
    @GuardedBy("mLock")
    private List<Integer> mDirtyUserIds;
    @GuardedBy("mStatLock")
    private final long[] mDurationStats;
    private final Handler mHandler;
    private final IPackageManager mIPackageManager;
    private CompressFormat mIconPersistFormat;
    private int mIconPersistQuality;
    @GuardedBy("mLock")
    private Exception mLastWtfStacktrace;
    @GuardedBy("mLock")
    private final ArrayList<ShortcutChangeListener> mListeners;
    private final Object mLock;
    private int mMaxIconDimension;
    private int mMaxShortcuts;
    int mMaxUpdatesPerInterval;
    private final PackageManagerInternal mPackageManagerInternal;
    final BroadcastReceiver mPackageMonitor;
    @GuardedBy("mLock")
    private long mRawLastResetTime;
    final BroadcastReceiver mReceiver;
    private long mResetInterval;
    private int mSaveDelayMillis;
    private final Runnable mSaveDirtyInfoRunner;
    final Object mStatLock;
    @GuardedBy("mLock")
    final SparseLongArray mUidLastForegroundElapsedTime;
    private final IUidObserver mUidObserver;
    @GuardedBy("mLock")
    final SparseIntArray mUidState;
    @GuardedBy("mLock")
    final SparseBooleanArray mUnlockedUsers;
    private final UsageStatsManagerInternal mUsageStatsManagerInternal;
    private final UserManager mUserManager;
    @GuardedBy("mLock")
    private final SparseArray<ShortcutUser> mUsers;
    @GuardedBy("mLock")
    private int mWtfCount;

    final /* synthetic */ class -android_content_pm_ParceledListSlice_getDynamicShortcuts_java_lang_String_packageName_int_userId_LambdaImpl0 implements Predicate {
        public boolean test(Object arg0) {
            return ((ShortcutInfo) arg0).isDynamic();
        }
    }

    final /* synthetic */ class -android_content_pm_ParceledListSlice_getManifestShortcuts_java_lang_String_packageName_int_userId_LambdaImpl0 implements Predicate {
        public boolean test(Object arg0) {
            return ((ShortcutInfo) arg0).isManifestShortcut();
        }
    }

    final /* synthetic */ class -android_content_pm_ParceledListSlice_getPinnedShortcuts_java_lang_String_packageName_int_userId_LambdaImpl0 implements Predicate {
        public boolean test(Object arg0) {
            return ((ShortcutInfo) arg0).isPinned();
        }
    }

    final /* synthetic */ class -byte__getBackupPayload_int_userId_LambdaImpl0 implements Consumer {
        public void accept(Object arg0) {
            ((ShortcutPackageItem) arg0).-com_android_server_pm_ShortcutService_lambda$16();
        }
    }

    final /* synthetic */ class -byte__getBackupPayload_int_userId_LambdaImpl1 implements Consumer {
        public void accept(Object arg0) {
            ((ShortcutLauncher) arg0).-com_android_server_pm_ShortcutService_lambda$17();
        }
    }

    final /* synthetic */ class -void__init__android_content_Context_context_android_os_Looper_looper_boolean_onlyForPackageManagerApis_LambdaImpl0 implements Runnable {
        public void run() {
            ShortcutService.this.-com_android_server_pm_ShortcutService-mthref-0();
        }
    }

    final /* synthetic */ class -void_checkPackageChanges_int_ownerUserId_LambdaImpl0 implements Consumer {
        private /* synthetic */ ArrayList val$gonePackages;

        public /* synthetic */ -void_checkPackageChanges_int_ownerUserId_LambdaImpl0(ArrayList arrayList) {
            this.val$gonePackages = arrayList;
        }

        public void accept(Object arg0) {
            ShortcutService.this.m38-com_android_server_pm_ShortcutService_lambda$14(this.val$gonePackages, (ShortcutPackageItem) arg0);
        }
    }

    final /* synthetic */ class -void_cleanUpPackageForAllLoadedUsers_java_lang_String_packageName_int_packageUserId_boolean_appStillExists_LambdaImpl0 implements Consumer {
        private /* synthetic */ boolean val$appStillExists;
        private /* synthetic */ String val$packageName;
        private /* synthetic */ int val$packageUserId;

        public /* synthetic */ -void_cleanUpPackageForAllLoadedUsers_java_lang_String_packageName_int_packageUserId_boolean_appStillExists_LambdaImpl0(String str, int i, boolean z) {
            this.val$packageName = str;
            this.val$packageUserId = i;
            this.val$appStillExists = z;
        }

        public void accept(Object arg0) {
            ShortcutService.this.m37-com_android_server_pm_ShortcutService_lambda$10(this.val$packageName, this.val$packageUserId, this.val$appStillExists, (ShortcutUser) arg0);
        }
    }

    final /* synthetic */ class -void_cleanUpPackageLocked_java_lang_String_packageName_int_owningUserId_int_packageUserId_boolean_appStillExists_LambdaImpl0 implements Consumer {
        private /* synthetic */ String val$packageName;
        private /* synthetic */ int val$packageUserId;

        public /* synthetic */ -void_cleanUpPackageLocked_java_lang_String_packageName_int_owningUserId_int_packageUserId_boolean_appStillExists_LambdaImpl0(String str, int i) {
            this.val$packageName = str;
            this.val$packageUserId = i;
        }

        public void accept(Object arg0) {
            ((ShortcutLauncher) arg0).-com_android_server_pm_ShortcutService_lambda$11(this.val$packageName, this.val$packageUserId);
        }
    }

    final /* synthetic */ class -void_cleanUpPackageLocked_java_lang_String_packageName_int_owningUserId_int_packageUserId_boolean_appStillExists_LambdaImpl1 implements Consumer {
        public void accept(Object arg0) {
            ((ShortcutPackage) arg0).-com_android_server_pm_ShortcutService_lambda$12();
        }
    }

    final /* synthetic */ class -void_handleLocaleChanged__LambdaImpl0 implements Consumer {
        public void accept(Object arg0) {
            ((ShortcutUser) arg0).-com_android_server_pm_ShortcutService_lambda$13();
        }
    }

    final /* synthetic */ class -void_handleUnlockUser_int_userId_LambdaImpl0 implements Runnable {
        private /* synthetic */ long val$start;
        private /* synthetic */ int val$userId;

        public /* synthetic */ -void_handleUnlockUser_int_userId_LambdaImpl0(long j, int i) {
            this.val$start = j;
            this.val$userId = i;
        }

        public void run() {
            ShortcutService.this.m40-com_android_server_pm_ShortcutService_lambda$5(this.val$start, this.val$userId);
        }
    }

    final /* synthetic */ class -void_notifyListeners_java_lang_String_packageName_int_userId_LambdaImpl0 implements Runnable {
        private /* synthetic */ String val$packageName;
        private /* synthetic */ int val$userId;

        public /* synthetic */ -void_notifyListeners_java_lang_String_packageName_int_userId_LambdaImpl0(int i, String str) {
            this.val$userId = i;
            this.val$packageName = str;
        }

        public void run() {
            ShortcutService.this.m41-com_android_server_pm_ShortcutService_lambda$6(this.val$userId, this.val$packageName);
        }
    }

    final /* synthetic */ class -void_rescanUpdatedPackagesLocked_int_userId_long_lastScanTime_boolean_forceRescan_LambdaImpl0 implements Consumer {
        private /* synthetic */ boolean val$forceRescan;
        private /* synthetic */ ShortcutUser val$user;
        private /* synthetic */ int val$userId;

        public /* synthetic */ -void_rescanUpdatedPackagesLocked_int_userId_long_lastScanTime_boolean_forceRescan_LambdaImpl0(ShortcutUser shortcutUser, int i, boolean z) {
            this.val$user = shortcutUser;
            this.val$userId = i;
            this.val$forceRescan = z;
        }

        public void accept(Object arg0) {
            ShortcutService.this.m39-com_android_server_pm_ShortcutService_lambda$15(this.val$user, this.val$userId, this.val$forceRescan, (ApplicationInfo) arg0);
        }
    }

    final /* synthetic */ class -void_verifyStatesInner__LambdaImpl0 implements Consumer {
        public void accept(Object arg0) {
            ((ShortcutUser) arg0).forAllPackageItems(new ShortcutService$-void_-com_android_server_pm_ShortcutService_lambda$18_com_android_server_pm_ShortcutUser_u_LambdaImpl0());
        }
    }

    static class CommandException extends Exception {
        public CommandException(String message) {
            super(message);
        }
    }

    interface ConfigConstants {
        public static final String KEY_ICON_FORMAT = "icon_format";
        public static final String KEY_ICON_QUALITY = "icon_quality";
        public static final String KEY_MAX_ICON_DIMENSION_DP = "max_icon_dimension_dp";
        public static final String KEY_MAX_ICON_DIMENSION_DP_LOWRAM = "max_icon_dimension_dp_lowram";
        public static final String KEY_MAX_SHORTCUTS = "max_shortcuts";
        public static final String KEY_MAX_UPDATES_PER_INTERVAL = "max_updates_per_interval";
        public static final String KEY_RESET_INTERVAL_SEC = "reset_interval_sec";
        public static final String KEY_SAVE_DELAY_MILLIS = "save_delay_ms";
    }

    static class FileOutputStreamWithPath extends FileOutputStream {
        private final File mFile;

        public FileOutputStreamWithPath(File file) throws FileNotFoundException {
            super(file);
            this.mFile = file;
        }

        public File getFile() {
            return this.mFile;
        }
    }

    static class InvalidFileFormatException extends Exception {
        public InvalidFileFormatException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static final class Lifecycle extends SystemService {
        final ShortcutService mService;

        public Lifecycle(Context context) {
            super(context);
            this.mService = new ShortcutService(context);
        }

        public void onStart() {
            publishBinderService(ShortcutService.KEY_SHORTCUT, this.mService);
        }

        public void onBootPhase(int phase) {
            this.mService.onBootPhase(phase);
        }

        public void onCleanupUser(int userHandle) {
            this.mService.handleCleanupUser(userHandle);
        }

        public void onUnlockUser(int userId) {
            this.mService.handleUnlockUser(userId);
        }
    }

    private class LocalService extends ShortcutServiceInternal {

        final /* synthetic */ class -android_content_pm_ShortcutInfo_getShortcutInfoLocked_int_launcherUserId_java_lang_String_callingPackage_java_lang_String_packageName_java_lang_String_shortcutId_int_userId_LambdaImpl0 implements Predicate {
            private /* synthetic */ String val$shortcutId;

            public /* synthetic */ -android_content_pm_ShortcutInfo_getShortcutInfoLocked_int_launcherUserId_java_lang_String_callingPackage_java_lang_String_packageName_java_lang_String_shortcutId_int_userId_LambdaImpl0(String str) {
                this.val$shortcutId = str;
            }

            public boolean test(Object arg0) {
                return this.val$shortcutId.equals(((ShortcutInfo) arg0).getId());
            }
        }

        final /* synthetic */ class -java_util_List_getShortcuts_int_launcherUserId_java_lang_String_callingPackage_long_changedSince_java_lang_String_packageName_java_util_List_shortcutIds_android_content_ComponentName_componentName_int_queryFlags_int_userId_LambdaImpl0 implements Consumer {
            private /* synthetic */ String val$callingPackage;
            private /* synthetic */ long val$changedSince;
            private /* synthetic */ int val$cloneFlag;
            private /* synthetic */ ComponentName val$componentName;
            private /* synthetic */ int val$launcherUserId;
            private /* synthetic */ int val$queryFlags;
            private /* synthetic */ ArrayList val$ret;
            private /* synthetic */ List val$shortcutIdsF;
            private /* synthetic */ int val$userId;

            public /* synthetic */ -java_util_List_getShortcuts_int_launcherUserId_java_lang_String_callingPackage_long_changedSince_java_lang_String_packageName_java_util_List_shortcutIds_android_content_ComponentName_componentName_int_queryFlags_int_userId_LambdaImpl0(int i, String str, List list, long j, ComponentName componentName, int i2, int i3, ArrayList arrayList, int i4) {
                this.val$launcherUserId = i;
                this.val$callingPackage = str;
                this.val$shortcutIdsF = list;
                this.val$changedSince = j;
                this.val$componentName = componentName;
                this.val$queryFlags = i2;
                this.val$userId = i3;
                this.val$ret = arrayList;
                this.val$cloneFlag = i4;
            }

            public void accept(Object arg0) {
                LocalService.this.m43-com_android_server_pm_ShortcutService$LocalService_lambda$1(this.val$launcherUserId, this.val$callingPackage, this.val$shortcutIdsF, this.val$changedSince, this.val$componentName, this.val$queryFlags, this.val$userId, this.val$ret, this.val$cloneFlag, (ShortcutPackage) arg0);
            }
        }

        final /* synthetic */ class -void_getShortcutsInnerLocked_int_launcherUserId_java_lang_String_callingPackage_java_lang_String_packageName_java_util_List_shortcutIds_long_changedSince_android_content_ComponentName_componentName_int_queryFlags_int_userId_java_util_ArrayList_ret_int_cloneFlag_LambdaImpl0 implements Predicate {
            private /* synthetic */ long val$changedSince;
            private /* synthetic */ ComponentName val$componentName;
            private /* synthetic */ ArraySet val$ids;
            private /* synthetic */ int val$queryFlags;

            public /* synthetic */ -void_getShortcutsInnerLocked_int_launcherUserId_java_lang_String_callingPackage_java_lang_String_packageName_java_util_List_shortcutIds_long_changedSince_android_content_ComponentName_componentName_int_queryFlags_int_userId_java_util_ArrayList_ret_int_cloneFlag_LambdaImpl0(long j, ArraySet arraySet, ComponentName componentName, int i) {
                this.val$changedSince = j;
                this.val$ids = arraySet;
                this.val$componentName = componentName;
                this.val$queryFlags = i;
            }

            public boolean test(Object arg0) {
                return LocalService.m42-com_android_server_pm_ShortcutService$LocalService_lambda$2(this.val$changedSince, this.val$ids, this.val$componentName, this.val$queryFlags, (ShortcutInfo) arg0);
            }
        }

        /* synthetic */ LocalService(ShortcutService this$0, LocalService localService) {
            this();
        }

        private LocalService() {
        }

        public List<ShortcutInfo> getShortcuts(int launcherUserId, String callingPackage, long changedSince, String packageName, List<String> shortcutIds, ComponentName componentName, int queryFlags, int userId) {
            int cloneFlag;
            ArrayList<ShortcutInfo> ret = new ArrayList();
            if ((queryFlags & 4) != 0) {
                cloneFlag = 4;
            } else {
                cloneFlag = 11;
            }
            if (packageName == null) {
                shortcutIds = null;
            }
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.throwIfUserLockedL(userId);
                ShortcutService.this.throwIfUserLockedL(launcherUserId);
                ShortcutService.this.getLauncherShortcutsLocked(callingPackage, userId, launcherUserId).-com_android_server_pm_ShortcutUser_lambda$3();
                if (packageName != null) {
                    getShortcutsInnerLocked(launcherUserId, callingPackage, packageName, shortcutIds, changedSince, componentName, queryFlags, userId, ret, cloneFlag);
                } else {
                    ShortcutService.this.getUserShortcutsLocked(userId).forAllPackages(new -java_util_List_getShortcuts_int_launcherUserId_java_lang_String_callingPackage_long_changedSince_java_lang_String_packageName_java_util_List_shortcutIds_android_content_ComponentName_componentName_int_queryFlags_int_userId_LambdaImpl0(launcherUserId, callingPackage, shortcutIds, changedSince, componentName, queryFlags, userId, ret, cloneFlag));
                }
            }
            return ret;
        }

        /* renamed from: -com_android_server_pm_ShortcutService$LocalService_lambda$1 */
        /* synthetic */ void m43-com_android_server_pm_ShortcutService$LocalService_lambda$1(int launcherUserId, String callingPackage, List shortcutIdsF, long changedSince, ComponentName componentName, int queryFlags, int userId, ArrayList ret, int cloneFlag, ShortcutPackage p) {
            getShortcutsInnerLocked(launcherUserId, callingPackage, p.getPackageName(), shortcutIdsF, changedSince, componentName, queryFlags, userId, ret, cloneFlag);
        }

        private void getShortcutsInnerLocked(int launcherUserId, String callingPackage, String packageName, List<String> shortcutIds, long changedSince, ComponentName componentName, int queryFlags, int userId, ArrayList<ShortcutInfo> ret, int cloneFlag) {
            ArraySet ids;
            if (shortcutIds == null) {
                ids = null;
            } else {
                ids = new ArraySet(shortcutIds);
            }
            ShortcutPackage p = ShortcutService.this.getUserShortcutsLocked(userId).getPackageShortcutsIfExists(packageName);
            if (p != null) {
                p.findAll(ret, new -void_getShortcutsInnerLocked_int_launcherUserId_java_lang_String_callingPackage_java_lang_String_packageName_java_util_List_shortcutIds_long_changedSince_android_content_ComponentName_componentName_int_queryFlags_int_userId_java_util_ArrayList_ret_int_cloneFlag_LambdaImpl0(changedSince, ids, componentName, queryFlags), cloneFlag, callingPackage, launcherUserId);
            }
        }

        /* renamed from: -com_android_server_pm_ShortcutService$LocalService_lambda$2 */
        static /* synthetic */ boolean m42-com_android_server_pm_ShortcutService$LocalService_lambda$2(long changedSince, ArraySet ids, ComponentName componentName, int queryFlags, ShortcutInfo si) {
            if (si.getLastChangedTimestamp() < changedSince) {
                return false;
            }
            if (ids != null && !ids.contains(si.getId())) {
                return false;
            }
            if (componentName != null && si.getActivity() != null && !si.getActivity().equals(componentName)) {
                return false;
            }
            if ((queryFlags & 1) != 0 && si.isDynamic()) {
                return true;
            }
            if ((queryFlags & 2) == 0 || !si.isPinned()) {
                return (queryFlags & 8) != 0 && si.isManifestShortcut();
            } else {
                return true;
            }
        }

        public boolean isPinnedByCaller(int launcherUserId, String callingPackage, String packageName, String shortcutId, int userId) {
            boolean isPinned;
            Preconditions.checkStringNotEmpty(packageName, "packageName");
            Preconditions.checkStringNotEmpty(shortcutId, "shortcutId");
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.throwIfUserLockedL(userId);
                ShortcutService.this.throwIfUserLockedL(launcherUserId);
                ShortcutService.this.getLauncherShortcutsLocked(callingPackage, userId, launcherUserId).-com_android_server_pm_ShortcutUser_lambda$3();
                ShortcutInfo si = getShortcutInfoLocked(launcherUserId, callingPackage, packageName, shortcutId, userId);
                isPinned = si != null ? si.isPinned() : false;
            }
            return isPinned;
        }

        private ShortcutInfo getShortcutInfoLocked(int launcherUserId, String callingPackage, String packageName, String shortcutId, int userId) {
            Preconditions.checkStringNotEmpty(packageName, "packageName");
            Preconditions.checkStringNotEmpty(shortcutId, "shortcutId");
            ShortcutService.this.throwIfUserLockedL(userId);
            ShortcutService.this.throwIfUserLockedL(launcherUserId);
            ShortcutPackage p = ShortcutService.this.getUserShortcutsLocked(userId).getPackageShortcutsIfExists(packageName);
            if (p == null) {
                return null;
            }
            ArrayList<ShortcutInfo> list = new ArrayList(1);
            p.findAll(list, new -android_content_pm_ShortcutInfo_getShortcutInfoLocked_int_launcherUserId_java_lang_String_callingPackage_java_lang_String_packageName_java_lang_String_shortcutId_int_userId_LambdaImpl0(shortcutId), 0, callingPackage, launcherUserId);
            return list.size() == 0 ? null : (ShortcutInfo) list.get(0);
        }

        public void pinShortcuts(int launcherUserId, String callingPackage, String packageName, List<String> shortcutIds, int userId) {
            Preconditions.checkStringNotEmpty(packageName, "packageName");
            Preconditions.checkNotNull(shortcutIds, "shortcutIds");
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.throwIfUserLockedL(userId);
                ShortcutService.this.throwIfUserLockedL(launcherUserId);
                ShortcutLauncher launcher = ShortcutService.this.getLauncherShortcutsLocked(callingPackage, userId, launcherUserId);
                launcher.-com_android_server_pm_ShortcutUser_lambda$3();
                launcher.pinShortcuts(userId, packageName, shortcutIds);
            }
            ShortcutService.this.packageShortcutsChanged(packageName, userId);
            ShortcutService.this.verifyStates();
        }

        public Intent[] createShortcutIntents(int launcherUserId, String callingPackage, String packageName, String shortcutId, int userId) {
            Preconditions.checkStringNotEmpty(packageName, "packageName can't be empty");
            Preconditions.checkStringNotEmpty(shortcutId, "shortcutId can't be empty");
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.throwIfUserLockedL(userId);
                ShortcutService.this.throwIfUserLockedL(launcherUserId);
                ShortcutService.this.getLauncherShortcutsLocked(callingPackage, userId, launcherUserId).-com_android_server_pm_ShortcutUser_lambda$3();
                ShortcutInfo si = getShortcutInfoLocked(launcherUserId, callingPackage, packageName, shortcutId, userId);
                if (si != null && si.isEnabled() && si.isAlive()) {
                    Intent[] intents = si.getIntents();
                    return intents;
                }
                Log.e(ShortcutService.TAG, "Shortcut " + shortcutId + " does not exist or disabled");
                return null;
            }
        }

        public void addListener(ShortcutChangeListener listener) {
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.mListeners.add((ShortcutChangeListener) Preconditions.checkNotNull(listener));
            }
        }

        /* JADX WARNING: Missing block: B:14:0x004c, code:
            return r2;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int getShortcutIconResId(int launcherUserId, String callingPackage, String packageName, String shortcutId, int userId) {
            int i = 0;
            Preconditions.checkNotNull(callingPackage, "callingPackage");
            Preconditions.checkNotNull(packageName, "packageName");
            Preconditions.checkNotNull(shortcutId, "shortcutId");
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.throwIfUserLockedL(userId);
                ShortcutService.this.throwIfUserLockedL(launcherUserId);
                ShortcutService.this.getLauncherShortcutsLocked(callingPackage, userId, launcherUserId).-com_android_server_pm_ShortcutUser_lambda$3();
                ShortcutPackage p = ShortcutService.this.getUserShortcutsLocked(userId).getPackageShortcutsIfExists(packageName);
                if (p == null) {
                    return 0;
                }
                ShortcutInfo shortcutInfo = p.findShortcutById(shortcutId);
                if (shortcutInfo != null && shortcutInfo.hasIconResource()) {
                    i = shortcutInfo.getIconResourceId();
                }
            }
        }

        /* JADX WARNING: Missing block: B:19:0x0059, code:
            return null;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public ParcelFileDescriptor getShortcutIconFd(int launcherUserId, String callingPackage, String packageName, String shortcutId, int userId) {
            Preconditions.checkNotNull(callingPackage, "callingPackage");
            Preconditions.checkNotNull(packageName, "packageName");
            Preconditions.checkNotNull(shortcutId, "shortcutId");
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.throwIfUserLockedL(userId);
                ShortcutService.this.throwIfUserLockedL(launcherUserId);
                ShortcutService.this.getLauncherShortcutsLocked(callingPackage, userId, launcherUserId).-com_android_server_pm_ShortcutUser_lambda$3();
                ShortcutPackage p = ShortcutService.this.getUserShortcutsLocked(userId).getPackageShortcutsIfExists(packageName);
                if (p == null) {
                    return null;
                }
                ShortcutInfo shortcutInfo = p.findShortcutById(shortcutId);
                if (shortcutInfo == null || !shortcutInfo.hasIconFile()) {
                } else {
                    try {
                        if (shortcutInfo.getBitmapPath() == null) {
                            Slog.w(ShortcutService.TAG, "null bitmap detected in getShortcutIconFd()");
                            return null;
                        }
                        ParcelFileDescriptor open = ParcelFileDescriptor.open(new File(shortcutInfo.getBitmapPath()), 268435456);
                        return open;
                    } catch (FileNotFoundException e) {
                        Slog.e(ShortcutService.TAG, "Icon file not found: " + shortcutInfo.getBitmapPath());
                        return null;
                    }
                }
            }
        }

        public boolean hasShortcutHostPermission(int launcherUserId, String callingPackage) {
            return ShortcutService.this.hasShortcutHostPermission(callingPackage, launcherUserId);
        }
    }

    private class MyShellCommand extends ShellCommand {
        private int mUserId;

        /* synthetic */ MyShellCommand(ShortcutService this$0, MyShellCommand myShellCommand) {
            this();
        }

        private MyShellCommand() {
            this.mUserId = 0;
        }

        private void parseOptionsLocked(boolean takeUser) throws CommandException {
            do {
                String opt = getNextOption();
                if (opt == null) {
                    return;
                }
                if (opt.equals("--user") && takeUser) {
                    this.mUserId = UserHandle.parseUserArg(getNextArgRequired());
                } else {
                    throw new CommandException("Unknown option: " + opt);
                }
            } while (ShortcutService.this.isUserUnlockedL(this.mUserId));
            throw new CommandException("User " + this.mUserId + " is not running or locked");
        }

        public int onCommand(String cmd) {
            if (cmd == null) {
                return handleDefaultCommands(cmd);
            }
            PrintWriter pw = getOutPrintWriter();
            try {
                if (cmd.equals("reset-throttling")) {
                    handleResetThrottling();
                } else if (cmd.equals("reset-all-throttling")) {
                    handleResetAllThrottling();
                } else if (cmd.equals("override-config")) {
                    handleOverrideConfig();
                } else if (cmd.equals("reset-config")) {
                    handleResetConfig();
                } else if (cmd.equals("clear-default-launcher")) {
                    handleClearDefaultLauncher();
                } else if (cmd.equals("get-default-launcher")) {
                    handleGetDefaultLauncher();
                } else if (cmd.equals("unload-user")) {
                    handleUnloadUser();
                } else if (cmd.equals("clear-shortcuts")) {
                    handleClearShortcuts();
                } else if (!cmd.equals("verify-states")) {
                    return handleDefaultCommands(cmd);
                } else {
                    handleVerifyStates();
                }
                pw.println("Success");
                return 0;
            } catch (CommandException e) {
                pw.println("Error: " + e.getMessage());
                return 1;
            }
        }

        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("Usage: cmd shortcut COMMAND [options ...]");
            pw.println();
            pw.println("cmd shortcut reset-throttling [--user USER_ID]");
            pw.println("    Reset throttling for all packages and users");
            pw.println();
            pw.println("cmd shortcut reset-all-throttling");
            pw.println("    Reset the throttling state for all users");
            pw.println();
            pw.println("cmd shortcut override-config CONFIG");
            pw.println("    Override the configuration for testing (will last until reboot)");
            pw.println();
            pw.println("cmd shortcut reset-config");
            pw.println("    Reset the configuration set with \"update-config\"");
            pw.println();
            pw.println("cmd shortcut clear-default-launcher [--user USER_ID]");
            pw.println("    Clear the cached default launcher");
            pw.println();
            pw.println("cmd shortcut get-default-launcher [--user USER_ID]");
            pw.println("    Show the default launcher");
            pw.println();
            pw.println("cmd shortcut unload-user [--user USER_ID]");
            pw.println("    Unload a user from the memory");
            pw.println("    (This should not affect any observable behavior)");
            pw.println();
            pw.println("cmd shortcut clear-shortcuts [--user USER_ID] PACKAGE");
            pw.println("    Remove all shortcuts from a package, including pinned shortcuts");
            pw.println();
        }

        private void handleResetThrottling() throws CommandException {
            synchronized (ShortcutService.this.mLock) {
                parseOptionsLocked(true);
                Slog.i(ShortcutService.TAG, "cmd: handleResetThrottling: user=" + this.mUserId);
                ShortcutService.this.resetThrottlingInner(this.mUserId);
            }
        }

        private void handleResetAllThrottling() {
            Slog.i(ShortcutService.TAG, "cmd: handleResetAllThrottling");
            ShortcutService.this.resetAllThrottlingInner();
        }

        private void handleOverrideConfig() throws CommandException {
            String config = getNextArgRequired();
            Slog.i(ShortcutService.TAG, "cmd: handleOverrideConfig: " + config);
            synchronized (ShortcutService.this.mLock) {
                if (ShortcutService.this.updateConfigurationLocked(config)) {
                } else {
                    throw new CommandException("override-config failed.  See logcat for details.");
                }
            }
        }

        private void handleResetConfig() {
            Slog.i(ShortcutService.TAG, "cmd: handleResetConfig");
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.loadConfigurationLocked();
            }
        }

        private void clearLauncher() {
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.getUserShortcutsLocked(this.mUserId).forceClearLauncher();
            }
        }

        private void showLauncher() {
            synchronized (ShortcutService.this.mLock) {
                ShortcutService.this.hasShortcutHostPermissionInner("-", this.mUserId);
                getOutPrintWriter().println("Launcher: " + ShortcutService.this.getUserShortcutsLocked(this.mUserId).getLastKnownLauncher());
            }
        }

        private void handleClearDefaultLauncher() throws CommandException {
            synchronized (ShortcutService.this.mLock) {
                parseOptionsLocked(true);
                clearLauncher();
            }
        }

        private void handleGetDefaultLauncher() throws CommandException {
            synchronized (ShortcutService.this.mLock) {
                parseOptionsLocked(true);
                clearLauncher();
                showLauncher();
            }
        }

        private void handleUnloadUser() throws CommandException {
            synchronized (ShortcutService.this.mLock) {
                parseOptionsLocked(true);
                Slog.i(ShortcutService.TAG, "cmd: handleUnloadUser: user=" + this.mUserId);
                ShortcutService.this.handleCleanupUser(this.mUserId);
            }
        }

        private void handleClearShortcuts() throws CommandException {
            synchronized (ShortcutService.this.mLock) {
                parseOptionsLocked(true);
                String packageName = getNextArgRequired();
                Slog.i(ShortcutService.TAG, "cmd: handleClearShortcuts: user" + this.mUserId + ", " + packageName);
                ShortcutService.this.cleanUpPackageForAllLoadedUsers(packageName, this.mUserId, true);
            }
        }

        private void handleVerifyStates() throws CommandException {
            try {
                ShortcutService.this.verifyStatesForce();
            } catch (Throwable th) {
                CommandException commandException = new CommandException(th.getMessage() + "\n" + Log.getStackTraceString(th));
            }
        }
    }

    @IntDef({0, 1, 2})
    @Retention(RetentionPolicy.SOURCE)
    @interface ShortcutOperation {
    }

    interface Stats {
        public static final int ASYNC_PRELOAD_USER_DELAY = 15;
        public static final int CHECK_LAUNCHER_ACTIVITY = 12;
        public static final int CHECK_PACKAGE_CHANGES = 8;
        public static final int CLEANUP_DANGLING_BITMAPS = 5;
        public static final int COUNT = 16;
        public static final int GET_ACTIVITY_WITH_METADATA = 6;
        public static final int GET_APPLICATION_INFO = 3;
        public static final int GET_APPLICATION_RESOURCES = 9;
        public static final int GET_DEFAULT_HOME = 0;
        public static final int GET_INSTALLED_PACKAGES = 7;
        public static final int GET_LAUNCHER_ACTIVITY = 11;
        public static final int GET_PACKAGE_INFO = 1;
        public static final int GET_PACKAGE_INFO_WITH_SIG = 2;
        public static final int IS_ACTIVITY_ENABLED = 13;
        public static final int LAUNCHER_PERMISSION_CHECK = 4;
        public static final int PACKAGE_UPDATE_CHECK = 14;
        public static final int RESOURCE_NAME_LOOKUP = 10;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.pm.ShortcutService.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.pm.ShortcutService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.ShortcutService.<clinit>():void");
    }

    public ShortcutService(Context context) {
        this(context, BackgroundThread.get().getLooper(), false);
    }

    ShortcutService(Context context, Looper looper, boolean onlyForPackageManagerApis) {
        this.mLock = new Object();
        this.mListeners = new ArrayList(1);
        this.mUsers = new SparseArray();
        this.mUidState = new SparseIntArray();
        this.mUidLastForegroundElapsedTime = new SparseLongArray();
        this.mDirtyUserIds = new ArrayList();
        this.mBootCompleted = new AtomicBoolean();
        this.mUnlockedUsers = new SparseBooleanArray();
        this.mStatLock = new Object();
        this.mCountStats = new int[16];
        this.mDurationStats = new long[16];
        this.mWtfCount = 0;
        this.mUidObserver = new IUidObserver.Stub() {
            public void onUidStateChanged(int uid, int procState) throws RemoteException {
                ShortcutService.this.handleOnUidStateChanged(uid, procState);
            }

            public void onUidGone(int uid) throws RemoteException {
                ShortcutService.this.handleOnUidStateChanged(uid, 16);
            }

            public void onUidActive(int uid) throws RemoteException {
            }

            public void onUidIdle(int uid) throws RemoteException {
            }
        };
        this.mSaveDirtyInfoRunner = new -void__init__android_content_Context_context_android_os_Looper_looper_boolean_onlyForPackageManagerApis_LambdaImpl0();
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (ShortcutService.this.mBootCompleted.get()) {
                    try {
                        if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
                            ShortcutService.this.handleLocaleChanged();
                        }
                    } catch (Exception e) {
                        ShortcutService.this.wtf("Exception in mReceiver.onReceive", e);
                    }
                }
            }
        };
        this.mPackageMonitor = new BroadcastReceiver() {
            /* JADX WARNING: Missing block: B:20:0x0057, code:
            if ("android.intent.action.ACTION_PREFERRED_ACTIVITY_CHANGED".equals(r0) == false) goto L_0x0071;
     */
            /* JADX WARNING: Missing block: B:21:0x0059, code:
            r12.this$0.injectRestoreCallingIdentity(r6);
     */
            /* JADX WARNING: Missing block: B:22:0x005e, code:
            return;
     */
            /* JADX WARNING: Missing block: B:32:?, code:
            r2 = r14.getData();
     */
            /* JADX WARNING: Missing block: B:33:0x0075, code:
            if (r2 == null) goto L_0x009d;
     */
            /* JADX WARNING: Missing block: B:34:0x0077, code:
            r3 = r2.getSchemeSpecificPart();
     */
            /* JADX WARNING: Missing block: B:35:0x007b, code:
            if (r3 != null) goto L_0x009f;
     */
            /* JADX WARNING: Missing block: B:36:0x007d, code:
            android.util.Slog.w(com.android.server.pm.ShortcutService.TAG, "Intent broadcast does not contain package name: " + r14);
     */
            /* JADX WARNING: Missing block: B:37:0x0097, code:
            r12.this$0.injectRestoreCallingIdentity(r6);
     */
            /* JADX WARNING: Missing block: B:38:0x009c, code:
            return;
     */
            /* JADX WARNING: Missing block: B:39:0x009d, code:
            r3 = null;
     */
            /* JADX WARNING: Missing block: B:41:?, code:
            r4 = r14.getBooleanExtra("android.intent.extra.REPLACING", false);
     */
            /* JADX WARNING: Missing block: B:42:0x00ae, code:
            if (r0.equals("android.intent.action.PACKAGE_ADDED") == false) goto L_0x00bd;
     */
            /* JADX WARNING: Missing block: B:43:0x00b0, code:
            if (r4 == false) goto L_0x00f3;
     */
            /* JADX WARNING: Missing block: B:44:0x00b2, code:
            com.android.server.pm.ShortcutService.-wrap6(r12.this$0, r3, r8);
     */
            /* JADX WARNING: Missing block: B:45:0x00b7, code:
            r12.this$0.injectRestoreCallingIdentity(r6);
     */
            /* JADX WARNING: Missing block: B:48:0x00c4, code:
            if (r0.equals("android.intent.action.PACKAGE_REMOVED") == false) goto L_0x00d5;
     */
            /* JADX WARNING: Missing block: B:49:0x00c6, code:
            if (r4 != false) goto L_0x00b7;
     */
            /* JADX WARNING: Missing block: B:50:0x00c8, code:
            com.android.server.pm.ShortcutService.-wrap5(r12.this$0, r3, r8);
     */
            /* JADX WARNING: Missing block: B:55:0x00dc, code:
            if (r0.equals("android.intent.action.PACKAGE_CHANGED") == false) goto L_0x00e4;
     */
            /* JADX WARNING: Missing block: B:56:0x00de, code:
            com.android.server.pm.ShortcutService.-wrap3(r12.this$0, r3, r8);
     */
            /* JADX WARNING: Missing block: B:58:0x00eb, code:
            if (r0.equals("android.intent.action.PACKAGE_DATA_CLEARED") == false) goto L_0x00b7;
     */
            /* JADX WARNING: Missing block: B:59:0x00ed, code:
            com.android.server.pm.ShortcutService.-wrap4(r12.this$0, r3, r8);
     */
            /* JADX WARNING: Missing block: B:60:0x00f3, code:
            com.android.server.pm.ShortcutService.-wrap2(r12.this$0, r3, r8);
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onReceive(Context context, Intent intent) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                if (userId == -10000) {
                    Slog.w(ShortcutService.TAG, "Intent broadcast does not contain user handle: " + intent);
                    return;
                }
                String action = intent.getAction();
                long token = ShortcutService.this.injectClearCallingIdentity();
                try {
                    synchronized (ShortcutService.this.mLock) {
                        if (ShortcutService.this.isUserUnlockedL(userId)) {
                            ShortcutService.this.getUserShortcutsLocked(userId).clearLauncher();
                        } else {
                            ShortcutService.this.injectRestoreCallingIdentity(token);
                        }
                    }
                } catch (Exception e) {
                    try {
                        ShortcutService.this.wtf("Exception in mPackageMonitor.onReceive", e);
                    } finally {
                        ShortcutService.this.injectRestoreCallingIdentity(token);
                    }
                }
            }
        };
        this.mContext = (Context) Preconditions.checkNotNull(context);
        LocalServices.addService(ShortcutServiceInternal.class, new LocalService(this, null));
        this.mHandler = new Handler(looper);
        this.mIPackageManager = AppGlobals.getPackageManager();
        this.mPackageManagerInternal = (PackageManagerInternal) Preconditions.checkNotNull((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class));
        this.mUserManager = (UserManager) Preconditions.checkNotNull((UserManager) context.getSystemService(UserManager.class));
        this.mUsageStatsManagerInternal = (UsageStatsManagerInternal) Preconditions.checkNotNull((UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class));
        this.mActivityManagerInternal = (ActivityManagerInternal) Preconditions.checkNotNull((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class));
        if (!onlyForPackageManagerApis) {
            IntentFilter packageFilter = new IntentFilter();
            packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
            packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            packageFilter.addAction("android.intent.action.PACKAGE_CHANGED");
            packageFilter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
            packageFilter.addDataScheme("package");
            packageFilter.setPriority(1000);
            this.mContext.registerReceiverAsUser(this.mPackageMonitor, UserHandle.ALL, packageFilter, null, this.mHandler);
            IntentFilter preferedActivityFilter = new IntentFilter();
            preferedActivityFilter.addAction("android.intent.action.ACTION_PREFERRED_ACTIVITY_CHANGED");
            preferedActivityFilter.setPriority(1000);
            this.mContext.registerReceiverAsUser(this.mPackageMonitor, UserHandle.ALL, preferedActivityFilter, null, this.mHandler);
            IntentFilter localeFilter = new IntentFilter();
            localeFilter.addAction("android.intent.action.LOCALE_CHANGED");
            localeFilter.setPriority(1000);
            this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, localeFilter, null, this.mHandler);
            injectRegisterUidObserver(this.mUidObserver, 3);
        }
    }

    void logDurationStat(int statId, long start) {
        synchronized (this.mStatLock) {
            int[] iArr = this.mCountStats;
            iArr[statId] = iArr[statId] + 1;
            long[] jArr = this.mDurationStats;
            jArr[statId] = jArr[statId] + (injectElapsedRealtime() - start);
        }
    }

    public String injectGetLocaleTagsForUser(int userId) {
        return LocaleList.getDefault().toLanguageTags();
    }

    void handleOnUidStateChanged(int uid, int procState) {
        synchronized (this.mLock) {
            this.mUidState.put(uid, procState);
            if (isProcessStateForeground(procState)) {
                this.mUidLastForegroundElapsedTime.put(uid, injectElapsedRealtime());
            }
        }
    }

    private boolean isProcessStateForeground(int processState) {
        if (processState == -1 || processState > 4) {
            return false;
        }
        return true;
    }

    boolean isUidForegroundLocked(int uid) {
        if (uid == 1000 || isProcessStateForeground(this.mUidState.get(uid, 16))) {
            return true;
        }
        return isProcessStateForeground(this.mActivityManagerInternal.getUidProcessState(uid));
    }

    long getUidLastForegroundElapsedTimeLocked(int uid) {
        return this.mUidLastForegroundElapsedTime.get(uid);
    }

    void onBootPhase(int phase) {
        switch (phase) {
            case SystemService.PHASE_LOCK_SETTINGS_READY /*480*/:
                initialize();
                return;
            case 1000:
                this.mBootCompleted.set(true);
                return;
            default:
                return;
        }
    }

    void handleUnlockUser(int userId) {
        synchronized (this.mLock) {
            this.mUnlockedUsers.put(userId, true);
        }
        injectRunOnNewThread(new -void_handleUnlockUser_int_userId_LambdaImpl0(injectElapsedRealtime(), userId));
    }

    /* renamed from: -com_android_server_pm_ShortcutService_lambda$5 */
    /* synthetic */ void m40-com_android_server_pm_ShortcutService_lambda$5(long start, int userId) {
        synchronized (this.mLock) {
            logDurationStat(15, start);
            getUserShortcutsLocked(userId);
        }
    }

    void handleCleanupUser(int userId) {
        synchronized (this.mLock) {
            unloadUserLocked(userId);
            this.mUnlockedUsers.put(userId, false);
        }
    }

    private void unloadUserLocked(int userId) {
        -com_android_server_pm_ShortcutService-mthref-0();
        this.mUsers.delete(userId);
    }

    private AtomicFile getBaseStateFile() {
        File path = new File(injectSystemDataPath(), FILENAME_BASE_STATE);
        path.mkdirs();
        return new AtomicFile(path);
    }

    private void initialize() {
        synchronized (this.mLock) {
            loadConfigurationLocked();
            loadBaseStateLocked();
        }
    }

    private void loadConfigurationLocked() {
        updateConfigurationLocked(injectShortcutManagerConstants());
    }

    boolean updateConfigurationLocked(String config) {
        int i;
        boolean result = true;
        KeyValueListParser parser = new KeyValueListParser(',');
        try {
            parser.setString(config);
        } catch (IllegalArgumentException e) {
            Slog.e(TAG, "Bad shortcut manager settings", e);
            result = false;
        }
        this.mSaveDelayMillis = Math.max(0, (int) parser.getLong(ConfigConstants.KEY_SAVE_DELAY_MILLIS, 3000));
        this.mResetInterval = Math.max(1, parser.getLong(ConfigConstants.KEY_RESET_INTERVAL_SEC, DEFAULT_RESET_INTERVAL_SEC) * 1000);
        this.mMaxUpdatesPerInterval = Math.max(0, (int) parser.getLong(ConfigConstants.KEY_MAX_UPDATES_PER_INTERVAL, 10));
        this.mMaxShortcuts = Math.max(0, (int) parser.getLong(ConfigConstants.KEY_MAX_SHORTCUTS, 5));
        if (injectIsLowRamDevice()) {
            i = (int) parser.getLong(ConfigConstants.KEY_MAX_ICON_DIMENSION_DP_LOWRAM, 48);
        } else {
            i = (int) parser.getLong(ConfigConstants.KEY_MAX_ICON_DIMENSION_DP, 96);
        }
        this.mMaxIconDimension = injectDipToPixel(Math.max(1, i));
        this.mIconPersistFormat = CompressFormat.valueOf(parser.getString(ConfigConstants.KEY_ICON_FORMAT, DEFAULT_ICON_PERSIST_FORMAT));
        this.mIconPersistQuality = (int) parser.getLong(ConfigConstants.KEY_ICON_QUALITY, 100);
        return result;
    }

    String injectShortcutManagerConstants() {
        return Global.getString(this.mContext.getContentResolver(), "shortcut_manager_constants");
    }

    int injectDipToPixel(int dip) {
        return (int) TypedValue.applyDimension(1, (float) dip, this.mContext.getResources().getDisplayMetrics());
    }

    static String parseStringAttribute(XmlPullParser parser, String attribute) {
        return parser.getAttributeValue(null, attribute);
    }

    static boolean parseBooleanAttribute(XmlPullParser parser, String attribute) {
        return parseLongAttribute(parser, attribute) == 1;
    }

    static int parseIntAttribute(XmlPullParser parser, String attribute) {
        return (int) parseLongAttribute(parser, attribute);
    }

    static int parseIntAttribute(XmlPullParser parser, String attribute, int def) {
        return (int) parseLongAttribute(parser, attribute, (long) def);
    }

    static long parseLongAttribute(XmlPullParser parser, String attribute) {
        return parseLongAttribute(parser, attribute, 0);
    }

    static long parseLongAttribute(XmlPullParser parser, String attribute, long def) {
        String value = parseStringAttribute(parser, attribute);
        if (TextUtils.isEmpty(value)) {
            return def;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            Slog.e(TAG, "Error parsing long " + value);
            return def;
        }
    }

    static ComponentName parseComponentNameAttribute(XmlPullParser parser, String attribute) {
        String value = parseStringAttribute(parser, attribute);
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        return ComponentName.unflattenFromString(value);
    }

    static Intent parseIntentAttributeNoDefault(XmlPullParser parser, String attribute) {
        String value = parseStringAttribute(parser, attribute);
        Intent parsed = null;
        if (TextUtils.isEmpty(value)) {
            return parsed;
        }
        try {
            return Intent.parseUri(value, 0);
        } catch (URISyntaxException e) {
            Slog.e(TAG, "Error parsing intent", e);
            return parsed;
        }
    }

    static Intent parseIntentAttribute(XmlPullParser parser, String attribute) {
        Intent parsed = parseIntentAttributeNoDefault(parser, attribute);
        if (parsed == null) {
            return new Intent("android.intent.action.VIEW");
        }
        return parsed;
    }

    static void writeTagValue(XmlSerializer out, String tag, String value) throws IOException {
        if (!TextUtils.isEmpty(value)) {
            out.startTag(null, tag);
            out.attribute(null, ATTR_VALUE, value);
            out.endTag(null, tag);
        }
    }

    static void writeTagValue(XmlSerializer out, String tag, long value) throws IOException {
        writeTagValue(out, tag, Long.toString(value));
    }

    static void writeTagValue(XmlSerializer out, String tag, ComponentName name) throws IOException {
        if (name != null) {
            writeTagValue(out, tag, name.flattenToString());
        }
    }

    static void writeTagExtra(XmlSerializer out, String tag, PersistableBundle bundle) throws IOException, XmlPullParserException {
        if (bundle != null) {
            out.startTag(null, tag);
            bundle.saveToXml(out);
            out.endTag(null, tag);
        }
    }

    static void writeAttr(XmlSerializer out, String name, CharSequence value) throws IOException {
        if (!TextUtils.isEmpty(value)) {
            out.attribute(null, name, value.toString());
        }
    }

    static void writeAttr(XmlSerializer out, String name, long value) throws IOException {
        writeAttr(out, name, String.valueOf(value));
    }

    static void writeAttr(XmlSerializer out, String name, boolean value) throws IOException {
        if (value) {
            writeAttr(out, name, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        }
    }

    static void writeAttr(XmlSerializer out, String name, ComponentName comp) throws IOException {
        if (comp != null) {
            writeAttr(out, name, comp.flattenToString());
        }
    }

    static void writeAttr(XmlSerializer out, String name, Intent intent) throws IOException {
        if (intent != null) {
            writeAttr(out, name, intent.toUri(0));
        }
    }

    void saveBaseStateLocked() {
        AtomicFile file = getBaseStateFile();
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = file.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fileOutputStream, StandardCharsets.UTF_8.name());
            out.startDocument(null, Boolean.valueOf(true));
            out.startTag(null, TAG_ROOT);
            writeTagValue(out, TAG_LAST_RESET_TIME, this.mRawLastResetTime);
            out.endTag(null, TAG_ROOT);
            out.endDocument();
            file.finishWrite(fileOutputStream);
        } catch (IOException e) {
            Slog.e(TAG, "Failed to write to file " + file.getBaseFile(), e);
            file.failWrite(fileOutputStream);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x0084 A:{Splitter: B:16:0x005a, ExcHandler: java.io.IOException (r2_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:11:0x0039, code:
            android.util.Slog.e(TAG, "Invalid root tag: " + r6);
     */
    /* JADX WARNING: Missing block: B:12:0x0053, code:
            if (r4 == null) goto L_0x0058;
     */
    /* JADX WARNING: Missing block: B:14:?, code:
            r4.close();
     */
    /* JADX WARNING: Missing block: B:21:0x0060, code:
            r9 = th;
     */
    /* JADX WARNING: Missing block: B:39:0x0084, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:40:0x0085, code:
            android.util.Slog.e(TAG, "Failed to read file " + r3.getBaseFile(), r2);
            r13.mRawLastResetTime = 0;
     */
    /* JADX WARNING: Missing block: B:45:0x00c6, code:
            if (r4 == null) goto L_0x00cb;
     */
    /* JADX WARNING: Missing block: B:47:?, code:
            r4.close();
     */
    /* JADX WARNING: Missing block: B:51:0x00ce, code:
            r9 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadBaseStateLocked() {
        this.mRawLastResetTime = 0;
        AtomicFile file = getBaseStateFile();
        Throwable th = null;
        FileInputStream in = null;
        Throwable th2;
        try {
            in = file.openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(in, StandardCharsets.UTF_8.name());
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    break;
                } else if (type == 2) {
                    int depth = parser.getDepth();
                    String tag = parser.getName();
                    if (depth == 1) {
                        if (!TAG_ROOT.equals(tag)) {
                            break;
                        }
                    } else if (tag.equals(TAG_LAST_RESET_TIME)) {
                        this.mRawLastResetTime = parseLongAttribute(parser, ATTR_VALUE);
                    } else {
                        Slog.e(TAG, "Invalid tag: " + tag);
                    }
                }
            }
            getLastResetTimeLocked();
            return;
            if (th != null) {
                try {
                    throw th;
                } catch (FileNotFoundException e) {
                } catch (Exception e2) {
                }
            } else {
                return;
            }
            if (th != null) {
                throw th;
            }
            getLastResetTimeLocked();
            return;
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable th3) {
                    if (th == null) {
                        th = th3;
                    } else if (th != th3) {
                        th.addSuppressed(th3);
                    }
                }
            }
            if (th != null) {
                throw th;
            } else {
                throw th2;
            }
        } catch (Throwable th4) {
            Throwable th5 = th4;
            th4 = th2;
            th2 = th5;
        }
    }

    final File getUserFile(int userId) {
        return new File(injectUserDataPath(userId), FILENAME_USER_PACKAGES);
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x0020 A:{Splitter: B:1:0x0011, ExcHandler: org.xmlpull.v1.XmlPullParserException (r0_0 'e' java.lang.Exception), PHI: r2 } */
    /* JADX WARNING: Missing block: B:3:0x0020, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:4:0x0021, code:
            android.util.Slog.e(TAG, "Failed to write to file " + r1.getBaseFile(), r0);
            r1.failWrite(r2);
     */
    /* JADX WARNING: Missing block: B:6:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveUserLocked(int userId) {
        File path = getUserFile(userId);
        path.getParentFile().mkdirs();
        AtomicFile file = new AtomicFile(path);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = file.startWrite();
            saveUserInternalLocked(userId, fileOutputStream, false);
            file.finishWrite(fileOutputStream);
            cleanupDanglingBitmapDirectoriesLocked(userId);
        } catch (Exception e) {
        }
    }

    private void saveUserInternalLocked(int userId, OutputStream os, boolean forBackup) throws IOException, XmlPullParserException {
        BufferedOutputStream bos = new BufferedOutputStream(os);
        XmlSerializer out = new FastXmlSerializer();
        out.setOutput(bos, StandardCharsets.UTF_8.name());
        out.startDocument(null, Boolean.valueOf(true));
        getUserShortcutsLocked(userId).saveToXml(out, forBackup);
        out.endDocument();
        bos.flush();
        os.flush();
    }

    static IOException throwForInvalidTag(int depth, String tag) throws IOException {
        Object[] objArr = new Object[2];
        objArr[0] = tag;
        objArr[1] = Integer.valueOf(depth);
        throw new IOException(String.format("Invalid tag '%s' found at depth %d", objArr));
    }

    static void warnForInvalidTag(int depth, String tag) throws IOException {
        String str = TAG;
        Object[] objArr = new Object[2];
        objArr[0] = tag;
        objArr[1] = Integer.valueOf(depth);
        Slog.w(str, String.format("Invalid tag '%s' found at depth %d", objArr));
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0019 A:{Splitter: B:4:0x000f, ExcHandler: java.io.IOException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0019 A:{Splitter: B:4:0x000f, ExcHandler: java.io.IOException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:9:0x0019, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:?, code:
            android.util.Slog.e(TAG, "Failed to read file " + r2.getBaseFile(), r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ShortcutUser loadUserLocked(int userId) {
        AtomicFile file = new AtomicFile(getUserFile(userId));
        try {
            FileInputStream in = file.openRead();
            try {
                ShortcutUser ret = loadUserInternal(userId, in, false);
                return ret;
            } catch (Exception e) {
            } finally {
                IoUtils.closeQuietly(in);
            }
            return null;
        } catch (FileNotFoundException e2) {
            return null;
        }
    }

    private ShortcutUser loadUserInternal(int userId, InputStream is, boolean fromBackup) throws XmlPullParserException, IOException, InvalidFileFormatException {
        BufferedInputStream bis = new BufferedInputStream(is);
        ShortcutUser ret = null;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(bis, StandardCharsets.UTF_8.name());
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return ret;
            }
            if (type == 2) {
                int depth = parser.getDepth();
                String tag = parser.getName();
                if (depth == 1 && "user".equals(tag)) {
                    ret = ShortcutUser.loadFromXml(this, parser, userId, fromBackup);
                } else {
                    throwForInvalidTag(depth, tag);
                }
            }
        }
    }

    private void scheduleSaveBaseState() {
        scheduleSaveInner(-10000);
    }

    void scheduleSaveUser(int userId) {
        scheduleSaveInner(userId);
    }

    private void scheduleSaveInner(int userId) {
        synchronized (this.mLock) {
            if (!this.mDirtyUserIds.contains(Integer.valueOf(userId))) {
                this.mDirtyUserIds.add(Integer.valueOf(userId));
            }
        }
        this.mHandler.removeCallbacks(this.mSaveDirtyInfoRunner);
        this.mHandler.postDelayed(this.mSaveDirtyInfoRunner, (long) this.mSaveDelayMillis);
    }

    /* renamed from: saveDirtyInfo */
    void -com_android_server_pm_ShortcutService-mthref-0() {
        try {
            synchronized (this.mLock) {
                for (int i = this.mDirtyUserIds.size() - 1; i >= 0; i--) {
                    int userId = ((Integer) this.mDirtyUserIds.get(i)).intValue();
                    if (userId == -10000) {
                        saveBaseStateLocked();
                    } else {
                        saveUserLocked(userId);
                    }
                }
                this.mDirtyUserIds.clear();
            }
        } catch (Exception e) {
            wtf("Exception in saveDirtyInfo", e);
        }
    }

    long getLastResetTimeLocked() {
        updateTimesLocked();
        return this.mRawLastResetTime;
    }

    long getNextResetTimeLocked() {
        updateTimesLocked();
        return this.mRawLastResetTime + this.mResetInterval;
    }

    static boolean isClockValid(long time) {
        return time >= 1420070400;
    }

    private void updateTimesLocked() {
        long now = injectCurrentTimeMillis();
        long prevLastResetTime = this.mRawLastResetTime;
        if (this.mRawLastResetTime == 0) {
            this.mRawLastResetTime = now;
        } else if (now < this.mRawLastResetTime) {
            if (isClockValid(now)) {
                Slog.w(TAG, "Clock rewound");
                this.mRawLastResetTime = now;
            }
        } else if (this.mRawLastResetTime + this.mResetInterval <= now) {
            this.mRawLastResetTime = ((now / this.mResetInterval) * this.mResetInterval) + (this.mRawLastResetTime % this.mResetInterval);
        }
        if (prevLastResetTime != this.mRawLastResetTime) {
            scheduleSaveBaseState();
        }
    }

    protected boolean isUserUnlockedL(int userId) {
        if (this.mUnlockedUsers.get(userId)) {
            return true;
        }
        long token = injectClearCallingIdentity();
        try {
            boolean isUserUnlockingOrUnlocked = this.mUserManager.isUserUnlockingOrUnlocked(userId);
            return isUserUnlockingOrUnlocked;
        } finally {
            injectRestoreCallingIdentity(token);
        }
    }

    void throwIfUserLockedL(int userId) {
        if (!isUserUnlockedL(userId)) {
            throw new IllegalStateException("User " + userId + " is locked or not running");
        }
    }

    @GuardedBy("mLock")
    private boolean isUserLoadedLocked(int userId) {
        return this.mUsers.get(userId) != null;
    }

    @GuardedBy("mLock")
    ShortcutUser getUserShortcutsLocked(int userId) {
        if (!isUserUnlockedL(userId)) {
            wtf("User still locked");
        }
        ShortcutUser userPackages = (ShortcutUser) this.mUsers.get(userId);
        if (userPackages == null) {
            userPackages = loadUserLocked(userId);
            if (userPackages == null) {
                userPackages = new ShortcutUser(this, userId);
            }
            this.mUsers.put(userId, userPackages);
            checkPackageChanges(userId);
        }
        return userPackages;
    }

    void forEachLoadedUserLocked(Consumer<ShortcutUser> c) {
        for (int i = this.mUsers.size() - 1; i >= 0; i--) {
            c.accept((ShortcutUser) this.mUsers.valueAt(i));
        }
    }

    @GuardedBy("mLock")
    ShortcutPackage getPackageShortcutsLocked(String packageName, int userId) {
        return getUserShortcutsLocked(userId).getPackageShortcuts(packageName);
    }

    @GuardedBy("mLock")
    ShortcutPackage getPackageShortcutsForPublisherLocked(String packageName, int userId) {
        ShortcutPackage ret = getUserShortcutsLocked(userId).getPackageShortcuts(packageName);
        ret.getUser().onCalledByPublisher(packageName);
        return ret;
    }

    @GuardedBy("mLock")
    ShortcutLauncher getLauncherShortcutsLocked(String packageName, int ownerUserId, int launcherUserId) {
        return getUserShortcutsLocked(ownerUserId).getLauncherShortcuts(packageName, launcherUserId);
    }

    void removeIcon(int userId, ShortcutInfo shortcut) {
        shortcut.setIconResourceId(0);
        shortcut.setIconResName(null);
        shortcut.clearFlags(12);
    }

    public void cleanupBitmapsForPackage(int userId, String packageName) {
        File packagePath = new File(getUserBitmapFilePath(userId), packageName);
        if (packagePath.isDirectory()) {
            if (!(FileUtils.deleteContents(packagePath) ? packagePath.delete() : false)) {
                Slog.w(TAG, "Unable to remove directory " + packagePath);
            }
        }
    }

    private void cleanupDanglingBitmapDirectoriesLocked(int userId) {
        long start = injectElapsedRealtime();
        ShortcutUser user = getUserShortcutsLocked(userId);
        File[] children = getUserBitmapFilePath(userId).listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    String packageName = child.getName();
                    if (user.hasPackage(packageName)) {
                        cleanupDanglingBitmapFilesLocked(userId, user, packageName, child);
                    } else {
                        cleanupBitmapsForPackage(userId, packageName);
                    }
                }
            }
            logDurationStat(5, start);
        }
    }

    private void cleanupDanglingBitmapFilesLocked(int userId, ShortcutUser user, String packageName, File path) {
        ArraySet<String> usedFiles = user.getPackageShortcuts(packageName).getUsedBitmapFiles();
        for (File child : path.listFiles()) {
            if (child.isFile() && !usedFiles.contains(child.getName())) {
                child.delete();
            }
        }
    }

    FileOutputStreamWithPath openIconFileForWrite(int userId, ShortcutInfo shortcut) throws IOException {
        File packagePath = new File(getUserBitmapFilePath(userId), shortcut.getPackage());
        if (!packagePath.isDirectory()) {
            packagePath.mkdirs();
            if (packagePath.isDirectory()) {
                SELinux.restorecon(packagePath);
            } else {
                throw new IOException("Unable to create directory " + packagePath);
            }
        }
        String baseName = String.valueOf(injectCurrentTimeMillis());
        int suffix = 0;
        while (true) {
            File file = new File(packagePath, (suffix == 0 ? baseName : baseName + LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + suffix) + ".png");
            if (!file.exists()) {
                return new FileOutputStreamWithPath(file);
            }
            suffix++;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:63:0x00a0 A:{Splitter: B:38:0x0060, ExcHandler: java.io.IOException (r1_0 'e' java.lang.Exception), PHI: r4 } */
    /* JADX WARNING: Missing block: B:63:0x00a0, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:65:?, code:
            android.util.Slog.wtf(TAG, "Unable to write bitmap to file", r1);
     */
    /* JADX WARNING: Missing block: B:69:0x00b2, code:
            r4.delete();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void saveIconAndFixUpShortcut(int userId, ShortcutInfo shortcut) {
        if (!shortcut.hasIconFile() && !shortcut.hasIconResource()) {
            long token = injectClearCallingIdentity();
            try {
                removeIcon(userId, shortcut);
                Icon icon = shortcut.getIcon();
                if (icon == null) {
                    injectRestoreCallingIdentity(token);
                    return;
                }
                switch (icon.getType()) {
                    case 1:
                        Bitmap bitmap = icon.getBitmap();
                        if (bitmap == null) {
                            Slog.e(TAG, "Null bitmap detected");
                            shortcut.clearIcon();
                            injectRestoreCallingIdentity(token);
                            return;
                        }
                        File file = null;
                        try {
                            FileOutputStreamWithPath out = openIconFileForWrite(userId, shortcut);
                            Bitmap shrunk;
                            try {
                                file = out.getFile();
                                shrunk = shrinkBitmap(bitmap, this.mMaxIconDimension);
                                shrunk.compress(this.mIconPersistFormat, this.mIconPersistQuality, out);
                                if (bitmap != shrunk) {
                                    shrunk.recycle();
                                }
                                shortcut.setBitmapPath(out.getFile().getAbsolutePath());
                                shortcut.addFlags(8);
                                IoUtils.closeQuietly(out);
                            } catch (Throwable th) {
                                IoUtils.closeQuietly(out);
                            }
                        } catch (Exception e) {
                        }
                        shortcut.clearIcon();
                        injectRestoreCallingIdentity(token);
                        return;
                    case 2:
                        injectValidateIconResPackage(shortcut, icon);
                        shortcut.setIconResourceId(icon.getResId());
                        shortcut.addFlags(4);
                        shortcut.clearIcon();
                        injectRestoreCallingIdentity(token);
                        return;
                    default:
                        throw ShortcutInfo.getInvalidIconException();
                }
            } catch (Throwable th2) {
                injectRestoreCallingIdentity(token);
            }
        }
    }

    void injectValidateIconResPackage(ShortcutInfo shortcut, Icon icon) {
        if (!shortcut.getPackage().equals(icon.getResPackage())) {
            throw new IllegalArgumentException("Icon resource must reside in shortcut owner package");
        }
    }

    static Bitmap shrinkBitmap(Bitmap in, int maxSize) {
        int ow = in.getWidth();
        int oh = in.getHeight();
        if (ow <= maxSize && oh <= maxSize) {
            return in;
        }
        int longerDimension = Math.max(ow, oh);
        int nw = (ow * maxSize) / longerDimension;
        int nh = (oh * maxSize) / longerDimension;
        Bitmap scaledBitmap = Bitmap.createBitmap(nw, nh, Config.ARGB_8888);
        new Canvas(scaledBitmap).drawBitmap(in, null, new RectF(OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, (float) nw, (float) nh), null);
        return scaledBitmap;
    }

    void fixUpShortcutResourceNamesAndValues(ShortcutInfo si) {
        Resources publisherRes = injectGetResourcesForApplicationAsUser(si.getPackage(), si.getUserId());
        if (publisherRes != null) {
            long start = injectElapsedRealtime();
            try {
                si.lookupAndFillInResourceNames(publisherRes);
                si.resolveResourceStrings(publisherRes);
            } finally {
                logDurationStat(10, start);
            }
        }
    }

    private boolean isCallerSystem() {
        return UserHandle.isSameApp(injectBinderCallingUid(), 1000);
    }

    private boolean isCallerShell() {
        int callingUid = injectBinderCallingUid();
        if (callingUid == 2000 || callingUid == 0) {
            return true;
        }
        return false;
    }

    private void enforceSystemOrShell() {
        if (!(!isCallerSystem() ? isCallerShell() : true)) {
            throw new SecurityException("Caller must be system or shell");
        }
    }

    private void enforceShell() {
        if (!isCallerShell()) {
            throw new SecurityException("Caller must be shell");
        }
    }

    private void enforceSystem() {
        if (!isCallerSystem()) {
            throw new SecurityException("Caller must be system");
        }
    }

    private void enforceResetThrottlingPermission() {
        if (!isCallerSystem()) {
            enforceCallingOrSelfPermission("android.permission.RESET_SHORTCUT_MANAGER_THROTTLING", null);
        }
    }

    private void enforceCallingOrSelfPermission(String permission, String message) {
        if (!isCallerSystem()) {
            injectEnforceCallingPermission(permission, message);
        }
    }

    void injectEnforceCallingPermission(String permission, String message) {
        this.mContext.enforceCallingPermission(permission, message);
    }

    private void verifyCaller(String packageName, int userId) {
        Preconditions.checkStringNotEmpty(packageName, "packageName");
        if (!isCallerSystem()) {
            if (UserHandle.getUserId(injectBinderCallingUid()) != userId) {
                throw new SecurityException("Invalid user-ID");
            } else if (injectGetPackageUid(packageName, userId) != injectBinderCallingUid()) {
                throw new SecurityException("Calling package name mismatch");
            }
        }
    }

    void injectPostToHandler(Runnable r) {
        this.mHandler.post(r);
    }

    void injectRunOnNewThread(Runnable r) {
        new Thread(r).start();
    }

    void enforceMaxActivityShortcuts(int numShortcuts) {
        if (numShortcuts > this.mMaxShortcuts) {
            throw new IllegalArgumentException("Max number of dynamic shortcuts exceeded");
        }
    }

    int getMaxActivityShortcuts() {
        return this.mMaxShortcuts;
    }

    void packageShortcutsChanged(String packageName, int userId) {
        notifyListeners(packageName, userId);
        scheduleSaveUser(userId);
    }

    private void notifyListeners(String packageName, int userId) {
        injectPostToHandler(new -void_notifyListeners_java_lang_String_packageName_int_userId_LambdaImpl0(userId, packageName));
    }

    /* JADX WARNING: Missing block: B:13:0x0013, code:
            r1 = r0.size() - 1;
     */
    /* JADX WARNING: Missing block: B:14:0x0019, code:
            if (r1 < 0) goto L_0x002b;
     */
    /* JADX WARNING: Missing block: B:15:0x001b, code:
            ((android.content.pm.ShortcutServiceInternal.ShortcutChangeListener) r0.get(r1)).onShortcutChanged(r7, r6);
            r1 = r1 - 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    /* renamed from: -com_android_server_pm_ShortcutService_lambda$6 */
    /* synthetic */ void m41-com_android_server_pm_ShortcutService_lambda$6(int userId, String packageName) {
        try {
            synchronized (this.mLock) {
                if (isUserUnlockedL(userId)) {
                    ArrayList<ShortcutChangeListener> copy = new ArrayList(this.mListeners);
                }
            }
        } catch (Exception e) {
        }
    }

    private void fixUpIncomingShortcutInfo(ShortcutInfo shortcut, boolean forUpdate) {
        Preconditions.checkNotNull(shortcut, "Null shortcut detected");
        if (shortcut.getActivity() != null) {
            Preconditions.checkState(shortcut.getPackage().equals(shortcut.getActivity().getPackageName()), "Cannot publish shortcut: activity " + shortcut.getActivity() + " does not" + " belong to package " + shortcut.getPackage());
            Preconditions.checkState(injectIsMainActivity(shortcut.getActivity(), shortcut.getUserId()), "Cannot publish shortcut: activity " + shortcut.getActivity() + " is not" + " main activity");
        }
        if (!forUpdate) {
            shortcut.enforceMandatoryFields();
            Preconditions.checkArgument(injectIsMainActivity(shortcut.getActivity(), shortcut.getUserId()), "Cannot publish shortcut: " + shortcut.getActivity() + " is not main activity");
        }
        if (shortcut.getIcon() != null) {
            ShortcutInfo.validateIcon(shortcut.getIcon());
        }
        shortcut.replaceFlags(0);
    }

    private void fillInDefaultActivity(List<ShortcutInfo> shortcuts) {
        ComponentName defaultActivity = null;
        for (int i = shortcuts.size() - 1; i >= 0; i--) {
            ShortcutInfo si = (ShortcutInfo) shortcuts.get(i);
            if (si.getActivity() == null) {
                if (defaultActivity == null) {
                    boolean z;
                    defaultActivity = injectGetDefaultMainActivity(si.getPackage(), si.getUserId());
                    if (defaultActivity != null) {
                        z = true;
                    } else {
                        z = false;
                    }
                    Preconditions.checkState(z, "Launcher activity not found for package " + si.getPackage());
                }
                si.setActivity(defaultActivity);
            }
        }
    }

    private void assignImplicitRanks(List<ShortcutInfo> shortcuts) {
        for (int i = shortcuts.size() - 1; i >= 0; i--) {
            ((ShortcutInfo) shortcuts.get(i)).setImplicitRank(i);
        }
    }

    public boolean setDynamicShortcuts(String packageName, ParceledListSlice shortcutInfoList, int userId) {
        verifyCaller(packageName, userId);
        List<ShortcutInfo> newShortcuts = shortcutInfoList.getList();
        int size = newShortcuts.size();
        synchronized (this.mLock) {
            throwIfUserLockedL(userId);
            ShortcutPackage ps = getPackageShortcutsForPublisherLocked(packageName, userId);
            ps.ensureImmutableShortcutsNotIncluded(newShortcuts);
            fillInDefaultActivity(newShortcuts);
            ps.enforceShortcutCountsBeforeOperation(newShortcuts, 0);
            if (ps.tryApiCall()) {
                int i;
                ps.clearAllImplicitRanks();
                assignImplicitRanks(newShortcuts);
                for (i = 0; i < size; i++) {
                    fixUpIncomingShortcutInfo((ShortcutInfo) newShortcuts.get(i), false);
                }
                ps.deleteAllDynamicShortcuts();
                for (i = 0; i < size; i++) {
                    ps.addOrUpdateDynamicShortcut((ShortcutInfo) newShortcuts.get(i));
                }
                ps.adjustRanks();
                packageShortcutsChanged(packageName, userId);
                verifyStates();
                return true;
            }
            return false;
        }
    }

    public boolean updateShortcuts(String packageName, ParceledListSlice shortcutInfoList, int userId) {
        verifyCaller(packageName, userId);
        List<ShortcutInfo> newShortcuts = shortcutInfoList.getList();
        int size = newShortcuts.size();
        synchronized (this.mLock) {
            throwIfUserLockedL(userId);
            ShortcutPackage ps = getPackageShortcutsForPublisherLocked(packageName, userId);
            ps.ensureImmutableShortcutsNotIncluded(newShortcuts);
            ps.enforceShortcutCountsBeforeOperation(newShortcuts, 2);
            if (ps.tryApiCall()) {
                ps.clearAllImplicitRanks();
                assignImplicitRanks(newShortcuts);
                for (int i = 0; i < size; i++) {
                    ShortcutInfo source = (ShortcutInfo) newShortcuts.get(i);
                    fixUpIncomingShortcutInfo(source, true);
                    ShortcutInfo target = ps.findShortcutById(source.getId());
                    if (target != null) {
                        if (target.isEnabled() != source.isEnabled()) {
                            Slog.w(TAG, "ShortcutInfo.enabled cannot be changed with updateShortcuts()");
                        }
                        if (source.hasRank()) {
                            target.setRankChanged();
                            target.setImplicitRank(source.getImplicitRank());
                        }
                        boolean replacingIcon = source.getIcon() != null;
                        if (replacingIcon) {
                            removeIcon(userId, target);
                        }
                        target.copyNonNullFieldsFrom(source);
                        target.setTimestamp(injectCurrentTimeMillis());
                        if (replacingIcon) {
                            saveIconAndFixUpShortcut(userId, target);
                        }
                        if (replacingIcon || source.hasStringResources()) {
                            fixUpShortcutResourceNamesAndValues(target);
                        }
                    }
                }
                ps.adjustRanks();
                packageShortcutsChanged(packageName, userId);
                verifyStates();
                return true;
            }
            return false;
        }
    }

    public boolean addDynamicShortcuts(String packageName, ParceledListSlice shortcutInfoList, int userId) {
        verifyCaller(packageName, userId);
        List<ShortcutInfo> newShortcuts = shortcutInfoList.getList();
        int size = newShortcuts.size();
        synchronized (this.mLock) {
            throwIfUserLockedL(userId);
            ShortcutPackage ps = getPackageShortcutsForPublisherLocked(packageName, userId);
            ps.ensureImmutableShortcutsNotIncluded(newShortcuts);
            fillInDefaultActivity(newShortcuts);
            ps.enforceShortcutCountsBeforeOperation(newShortcuts, 1);
            ps.clearAllImplicitRanks();
            assignImplicitRanks(newShortcuts);
            if (ps.tryApiCall()) {
                for (int i = 0; i < size; i++) {
                    ShortcutInfo newShortcut = (ShortcutInfo) newShortcuts.get(i);
                    fixUpIncomingShortcutInfo(newShortcut, false);
                    newShortcut.setRankChanged();
                    ps.addOrUpdateDynamicShortcut(newShortcut);
                }
                ps.adjustRanks();
                packageShortcutsChanged(packageName, userId);
                verifyStates();
                return true;
            }
            return false;
        }
    }

    public void disableShortcuts(String packageName, List shortcutIds, CharSequence disabledMessage, int disabledMessageResId, int userId) {
        verifyCaller(packageName, userId);
        Preconditions.checkNotNull(shortcutIds, "shortcutIds must be provided");
        synchronized (this.mLock) {
            throwIfUserLockedL(userId);
            ShortcutPackage ps = getPackageShortcutsForPublisherLocked(packageName, userId);
            ps.ensureImmutableShortcutsNotIncludedWithIds(shortcutIds);
            String disabledMessageString = disabledMessage == null ? null : disabledMessage.toString();
            for (int i = shortcutIds.size() - 1; i >= 0; i--) {
                ps.disableWithId((String) Preconditions.checkStringNotEmpty((String) shortcutIds.get(i)), disabledMessageString, disabledMessageResId, false);
            }
            ps.adjustRanks();
        }
        packageShortcutsChanged(packageName, userId);
        verifyStates();
    }

    public void enableShortcuts(String packageName, List shortcutIds, int userId) {
        verifyCaller(packageName, userId);
        Preconditions.checkNotNull(shortcutIds, "shortcutIds must be provided");
        synchronized (this.mLock) {
            throwIfUserLockedL(userId);
            ShortcutPackage ps = getPackageShortcutsForPublisherLocked(packageName, userId);
            ps.ensureImmutableShortcutsNotIncludedWithIds(shortcutIds);
            for (int i = shortcutIds.size() - 1; i >= 0; i--) {
                ps.enableWithId((String) shortcutIds.get(i));
            }
        }
        packageShortcutsChanged(packageName, userId);
        verifyStates();
    }

    public void removeDynamicShortcuts(String packageName, List shortcutIds, int userId) {
        verifyCaller(packageName, userId);
        Preconditions.checkNotNull(shortcutIds, "shortcutIds must be provided");
        synchronized (this.mLock) {
            throwIfUserLockedL(userId);
            ShortcutPackage ps = getPackageShortcutsForPublisherLocked(packageName, userId);
            ps.ensureImmutableShortcutsNotIncludedWithIds(shortcutIds);
            for (int i = shortcutIds.size() - 1; i >= 0; i--) {
                ps.deleteDynamicWithId((String) Preconditions.checkStringNotEmpty((String) shortcutIds.get(i)));
            }
            ps.adjustRanks();
        }
        packageShortcutsChanged(packageName, userId);
        verifyStates();
    }

    public void removeAllDynamicShortcuts(String packageName, int userId) {
        verifyCaller(packageName, userId);
        synchronized (this.mLock) {
            throwIfUserLockedL(userId);
            getPackageShortcutsForPublisherLocked(packageName, userId).deleteAllDynamicShortcuts();
        }
        packageShortcutsChanged(packageName, userId);
        verifyStates();
    }

    public ParceledListSlice<ShortcutInfo> getDynamicShortcuts(String packageName, int userId) {
        ParceledListSlice<ShortcutInfo> shortcutsWithQueryLocked;
        verifyCaller(packageName, userId);
        synchronized (this.mLock) {
            throwIfUserLockedL(userId);
            shortcutsWithQueryLocked = getShortcutsWithQueryLocked(packageName, userId, 9, new -android_content_pm_ParceledListSlice_getDynamicShortcuts_java_lang_String_packageName_int_userId_LambdaImpl0());
        }
        return shortcutsWithQueryLocked;
    }

    public ParceledListSlice<ShortcutInfo> getManifestShortcuts(String packageName, int userId) {
        ParceledListSlice<ShortcutInfo> shortcutsWithQueryLocked;
        verifyCaller(packageName, userId);
        synchronized (this.mLock) {
            throwIfUserLockedL(userId);
            shortcutsWithQueryLocked = getShortcutsWithQueryLocked(packageName, userId, 9, new -android_content_pm_ParceledListSlice_getManifestShortcuts_java_lang_String_packageName_int_userId_LambdaImpl0());
        }
        return shortcutsWithQueryLocked;
    }

    public ParceledListSlice<ShortcutInfo> getPinnedShortcuts(String packageName, int userId) {
        ParceledListSlice<ShortcutInfo> shortcutsWithQueryLocked;
        verifyCaller(packageName, userId);
        synchronized (this.mLock) {
            throwIfUserLockedL(userId);
            shortcutsWithQueryLocked = getShortcutsWithQueryLocked(packageName, userId, 9, new -android_content_pm_ParceledListSlice_getPinnedShortcuts_java_lang_String_packageName_int_userId_LambdaImpl0());
        }
        return shortcutsWithQueryLocked;
    }

    private ParceledListSlice<ShortcutInfo> getShortcutsWithQueryLocked(String packageName, int userId, int cloneFlags, Predicate<ShortcutInfo> query) {
        ArrayList<ShortcutInfo> ret = new ArrayList();
        getPackageShortcutsForPublisherLocked(packageName, userId).findAll(ret, query, cloneFlags);
        return new ParceledListSlice(ret);
    }

    public int getMaxShortcutCountPerActivity(String packageName, int userId) throws RemoteException {
        verifyCaller(packageName, userId);
        return this.mMaxShortcuts;
    }

    public int getRemainingCallCount(String packageName, int userId) {
        int apiCallCount;
        verifyCaller(packageName, userId);
        synchronized (this.mLock) {
            throwIfUserLockedL(userId);
            apiCallCount = this.mMaxUpdatesPerInterval - getPackageShortcutsForPublisherLocked(packageName, userId).getApiCallCount();
        }
        return apiCallCount;
    }

    public long getRateLimitResetTime(String packageName, int userId) {
        long nextResetTimeLocked;
        verifyCaller(packageName, userId);
        synchronized (this.mLock) {
            throwIfUserLockedL(userId);
            nextResetTimeLocked = getNextResetTimeLocked();
        }
        return nextResetTimeLocked;
    }

    public int getIconMaxDimensions(String packageName, int userId) {
        int i;
        verifyCaller(packageName, userId);
        synchronized (this.mLock) {
            i = this.mMaxIconDimension;
        }
        return i;
    }

    /* JADX WARNING: Missing block: B:9:0x002f, code:
            r2 = injectClearCallingIdentity();
     */
    /* JADX WARNING: Missing block: B:11:?, code:
            r8.mUsageStatsManagerInternal.reportShortcutUsage(r9, r10, r11);
     */
    /* JADX WARNING: Missing block: B:17:0x0040, code:
            injectRestoreCallingIdentity(r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void reportShortcutUsed(String packageName, String shortcutId, int userId) {
        verifyCaller(packageName, userId);
        Preconditions.checkNotNull(shortcutId);
        synchronized (this.mLock) {
            throwIfUserLockedL(userId);
            if (getPackageShortcutsForPublisherLocked(packageName, userId).findShortcutById(shortcutId) == null) {
                String str = TAG;
                Object[] objArr = new Object[2];
                objArr[0] = packageName;
                objArr[1] = shortcutId;
                Log.w(str, String.format("reportShortcutUsed: package %s doesn't have shortcut %s", objArr));
            }
        }
    }

    public void resetThrottling() {
        enforceSystemOrShell();
        resetThrottlingInner(getCallingUserId());
    }

    void resetThrottlingInner(int userId) {
        synchronized (this.mLock) {
            if (isUserUnlockedL(userId)) {
                getUserShortcutsLocked(userId).resetThrottling();
                scheduleSaveUser(userId);
                Slog.i(TAG, "ShortcutManager: throttling counter reset for user " + userId);
                return;
            }
            Log.w(TAG, "User " + userId + " is locked or not running");
        }
    }

    void resetAllThrottlingInner() {
        synchronized (this.mLock) {
            this.mRawLastResetTime = injectCurrentTimeMillis();
        }
        scheduleSaveBaseState();
        Slog.i(TAG, "ShortcutManager: throttling counter reset for all users");
    }

    public void onApplicationActive(String packageName, int userId) {
        enforceResetThrottlingPermission();
        synchronized (this.mLock) {
            if (isUserUnlockedL(userId)) {
                getPackageShortcutsLocked(packageName, userId).resetRateLimitingForCommandLineNoSaving();
                saveUserLocked(userId);
                return;
            }
        }
    }

    boolean hasShortcutHostPermission(String callingPackage, int userId) {
        long start = injectElapsedRealtime();
        try {
            boolean hasShortcutHostPermissionInner = hasShortcutHostPermissionInner(callingPackage, userId);
            return hasShortcutHostPermissionInner;
        } finally {
            logDurationStat(4, start);
        }
    }

    boolean hasShortcutHostPermissionInner(String callingPackage, int userId) {
        synchronized (this.mLock) {
            throwIfUserLockedL(userId);
            ShortcutUser user = getUserShortcutsLocked(userId);
            ComponentName cached = user.getCachedLauncher();
            if (cached == null || !cached.getPackageName().equals(callingPackage)) {
                ComponentName detected;
                List<ResolveInfo> allHomeCandidates = new ArrayList();
                long startGetHomeActivitiesAsUser = injectElapsedRealtime();
                ComponentName defaultLauncher = this.mPackageManagerInternal.getHomeActivitiesAsUser(allHomeCandidates, userId);
                logDurationStat(0, startGetHomeActivitiesAsUser);
                if (defaultLauncher != null) {
                    detected = defaultLauncher;
                } else {
                    detected = user.getLastKnownLauncher();
                    if (!(detected == null || injectIsActivityEnabledAndExported(detected, userId))) {
                        Slog.w(TAG, "Cached launcher " + detected + " no longer exists");
                        detected = null;
                        user.clearLauncher();
                    }
                }
                if (detected == null) {
                    int size = allHomeCandidates.size();
                    int lastPriority = Integer.MIN_VALUE;
                    for (int i = 0; i < size; i++) {
                        ResolveInfo ri = (ResolveInfo) allHomeCandidates.get(i);
                        if (ri.activityInfo.applicationInfo.isSystemApp()) {
                            if (ri.priority >= lastPriority) {
                                detected = ri.activityInfo.getComponentName();
                                lastPriority = ri.priority;
                            }
                        }
                    }
                }
                user.setLauncher(detected);
                if (detected != null) {
                    boolean equals = detected.getPackageName().equals(callingPackage);
                    return equals;
                }
                return false;
            }
            return true;
        }
    }

    private void cleanUpPackageForAllLoadedUsers(String packageName, int packageUserId, boolean appStillExists) {
        synchronized (this.mLock) {
            forEachLoadedUserLocked(new -void_cleanUpPackageForAllLoadedUsers_java_lang_String_packageName_int_packageUserId_boolean_appStillExists_LambdaImpl0(packageName, packageUserId, appStillExists));
        }
    }

    /* renamed from: -com_android_server_pm_ShortcutService_lambda$10 */
    /* synthetic */ void m37-com_android_server_pm_ShortcutService_lambda$10(String packageName, int packageUserId, boolean appStillExists, ShortcutUser user) {
        cleanUpPackageLocked(packageName, user.getUserId(), packageUserId, appStillExists);
    }

    void cleanUpPackageLocked(String packageName, int owningUserId, int packageUserId, boolean appStillExists) {
        boolean wasUserLoaded = isUserLoadedLocked(owningUserId);
        ShortcutUser user = getUserShortcutsLocked(owningUserId);
        boolean doNotify = false;
        if (packageUserId == owningUserId && user.removePackage(packageName) != null) {
            doNotify = true;
        }
        user.removeLauncher(packageUserId, packageName);
        user.forAllLaunchers(new -void_cleanUpPackageLocked_java_lang_String_packageName_int_owningUserId_int_packageUserId_boolean_appStillExists_LambdaImpl0(packageName, packageUserId));
        user.forAllPackages(new -void_cleanUpPackageLocked_java_lang_String_packageName_int_owningUserId_int_packageUserId_boolean_appStillExists_LambdaImpl1());
        scheduleSaveUser(owningUserId);
        if (doNotify) {
            notifyListeners(packageName, owningUserId);
        }
        if (appStillExists && packageUserId == owningUserId) {
            user.rescanPackageIfNeeded(packageName, true);
        }
        if (!wasUserLoaded) {
            unloadUserLocked(owningUserId);
        }
    }

    void handleLocaleChanged() {
        scheduleSaveBaseState();
        synchronized (this.mLock) {
            long token = injectClearCallingIdentity();
            try {
                forEachLoadedUserLocked(new -void_handleLocaleChanged__LambdaImpl0());
                injectRestoreCallingIdentity(token);
            } catch (Throwable th) {
                injectRestoreCallingIdentity(token);
            }
        }
    }

    void checkPackageChanges(int ownerUserId) {
        if (injectIsSafeModeEnabled()) {
            Slog.i(TAG, "Safe mode, skipping checkPackageChanges()");
            return;
        }
        long start = injectElapsedRealtime();
        try {
            ArrayList<PackageWithUser> gonePackages = new ArrayList();
            synchronized (this.mLock) {
                ShortcutUser user = getUserShortcutsLocked(ownerUserId);
                user.forAllPackageItems(new -void_checkPackageChanges_int_ownerUserId_LambdaImpl0(gonePackages));
                if (gonePackages.size() > 0) {
                    for (int i = gonePackages.size() - 1; i >= 0; i--) {
                        PackageWithUser pu = (PackageWithUser) gonePackages.get(i);
                        cleanUpPackageLocked(pu.packageName, ownerUserId, pu.userId, false);
                    }
                }
                rescanUpdatedPackagesLocked(ownerUserId, user.getLastAppScanTime(), false);
            }
            verifyStates();
        } finally {
            logDurationStat(8, start);
        }
    }

    /* renamed from: -com_android_server_pm_ShortcutService_lambda$14 */
    /* synthetic */ void m38-com_android_server_pm_ShortcutService_lambda$14(ArrayList gonePackages, ShortcutPackageItem spi) {
        if (!(spi.getPackageInfo().isShadow() || isPackageInstalled(spi.getPackageName(), spi.getPackageUserId()))) {
            gonePackages.add(PackageWithUser.of(spi));
        }
    }

    private void rescanUpdatedPackagesLocked(int userId, long lastScanTime, boolean forceRescan) {
        ShortcutUser user = getUserShortcutsLocked(userId);
        long now = injectCurrentTimeMillis();
        forUpdatedPackages(userId, lastScanTime, !injectBuildFingerprint().equals(user.getLastAppScanOsFingerprint()), new -void_rescanUpdatedPackagesLocked_int_userId_long_lastScanTime_boolean_forceRescan_LambdaImpl0(user, userId, forceRescan));
        user.setLastAppScanTime(now);
        user.setLastAppScanOsFingerprint(injectBuildFingerprint());
        scheduleSaveUser(userId);
    }

    /* renamed from: -com_android_server_pm_ShortcutService_lambda$15 */
    /* synthetic */ void m39-com_android_server_pm_ShortcutService_lambda$15(ShortcutUser user, int userId, boolean forceRescan, ApplicationInfo ai) {
        user.attemptToRestoreIfNeededAndSave(this, ai.packageName, userId);
        user.rescanPackageIfNeeded(ai.packageName, forceRescan);
    }

    private void handlePackageAdded(String packageName, int userId) {
        synchronized (this.mLock) {
            ShortcutUser user = getUserShortcutsLocked(userId);
            user.attemptToRestoreIfNeededAndSave(this, packageName, userId);
            user.rescanPackageIfNeeded(packageName, true);
        }
        verifyStates();
    }

    private void handlePackageUpdateFinished(String packageName, int userId) {
        synchronized (this.mLock) {
            ShortcutUser user = getUserShortcutsLocked(userId);
            user.attemptToRestoreIfNeededAndSave(this, packageName, userId);
            if (isPackageInstalled(packageName, userId)) {
                user.rescanPackageIfNeeded(packageName, true);
            }
        }
        verifyStates();
    }

    private void handlePackageRemoved(String packageName, int packageUserId) {
        cleanUpPackageForAllLoadedUsers(packageName, packageUserId, false);
        verifyStates();
    }

    private void handlePackageDataCleared(String packageName, int packageUserId) {
        cleanUpPackageForAllLoadedUsers(packageName, packageUserId, true);
        verifyStates();
    }

    private void handlePackageChanged(String packageName, int packageUserId) {
        synchronized (this.mLock) {
            getUserShortcutsLocked(packageUserId).rescanPackageIfNeeded(packageName, true);
        }
        verifyStates();
    }

    final PackageInfo getPackageInfoWithSignatures(String packageName, int userId) {
        return getPackageInfo(packageName, userId, true);
    }

    final PackageInfo getPackageInfo(String packageName, int userId) {
        return getPackageInfo(packageName, userId, false);
    }

    int injectGetPackageUid(String packageName, int userId) {
        long token = injectClearCallingIdentity();
        try {
            int packageUid = this.mIPackageManager.getPackageUid(packageName, PACKAGE_MATCH_FLAGS, userId);
            injectRestoreCallingIdentity(token);
            return packageUid;
        } catch (RemoteException e) {
            Slog.wtf(TAG, "RemoteException", e);
            injectRestoreCallingIdentity(token);
            return -1;
        } catch (Throwable th) {
            injectRestoreCallingIdentity(token);
            throw th;
        }
    }

    final PackageInfo getPackageInfo(String packageName, int userId, boolean getSignatures) {
        return isInstalledOrNull(injectPackageInfoWithUninstalled(packageName, userId, getSignatures));
    }

    PackageInfo injectPackageInfoWithUninstalled(String packageName, int userId, boolean getSignatures) {
        int i = 2;
        long start = injectElapsedRealtime();
        long token = injectClearCallingIdentity();
        try {
            int i2;
            PackageInfo packageInfo = this.mIPackageManager.getPackageInfo(packageName, (getSignatures ? 64 : 0) | PACKAGE_MATCH_FLAGS, userId);
            injectRestoreCallingIdentity(token);
            if (getSignatures) {
                i2 = 2;
            } else {
                i2 = 1;
            }
            logDurationStat(i2, start);
            return packageInfo;
        } catch (RemoteException e) {
            Slog.wtf(TAG, "RemoteException", e);
            injectRestoreCallingIdentity(token);
            if (!getSignatures) {
                i = 1;
            }
            logDurationStat(i, start);
            return null;
        } catch (Throwable th) {
            injectRestoreCallingIdentity(token);
            if (!getSignatures) {
                i = 1;
            }
            logDurationStat(i, start);
            throw th;
        }
    }

    final ApplicationInfo getApplicationInfo(String packageName, int userId) {
        return isInstalledOrNull(injectApplicationInfoWithUninstalled(packageName, userId));
    }

    ApplicationInfo injectApplicationInfoWithUninstalled(String packageName, int userId) {
        long start = injectElapsedRealtime();
        long token = injectClearCallingIdentity();
        try {
            ApplicationInfo applicationInfo = this.mIPackageManager.getApplicationInfo(packageName, PACKAGE_MATCH_FLAGS, userId);
            injectRestoreCallingIdentity(token);
            logDurationStat(3, start);
            return applicationInfo;
        } catch (RemoteException e) {
            Slog.wtf(TAG, "RemoteException", e);
            injectRestoreCallingIdentity(token);
            logDurationStat(3, start);
            return null;
        } catch (Throwable th) {
            injectRestoreCallingIdentity(token);
            logDurationStat(3, start);
            throw th;
        }
    }

    final ActivityInfo getActivityInfoWithMetadata(ComponentName activity, int userId) {
        return isInstalledOrNull(injectGetActivityInfoWithMetadataWithUninstalled(activity, userId));
    }

    ActivityInfo injectGetActivityInfoWithMetadataWithUninstalled(ComponentName activity, int userId) {
        long start = injectElapsedRealtime();
        long token = injectClearCallingIdentity();
        try {
            ActivityInfo activityInfo = this.mIPackageManager.getActivityInfo(activity, 794752, userId);
            injectRestoreCallingIdentity(token);
            logDurationStat(6, start);
            return activityInfo;
        } catch (RemoteException e) {
            Slog.wtf(TAG, "RemoteException", e);
            injectRestoreCallingIdentity(token);
            logDurationStat(6, start);
            return null;
        } catch (Throwable th) {
            injectRestoreCallingIdentity(token);
            logDurationStat(6, start);
            throw th;
        }
    }

    final List<PackageInfo> getInstalledPackages(int userId) {
        long start = injectElapsedRealtime();
        long token = injectClearCallingIdentity();
        try {
            List<PackageInfo> all = injectGetPackagesWithUninstalled(userId);
            all.removeIf(PACKAGE_NOT_INSTALLED);
            injectRestoreCallingIdentity(token);
            logDurationStat(7, start);
            return all;
        } catch (RemoteException e) {
            Slog.wtf(TAG, "RemoteException", e);
            injectRestoreCallingIdentity(token);
            logDurationStat(7, start);
            return null;
        } catch (Throwable th) {
            injectRestoreCallingIdentity(token);
            logDurationStat(7, start);
            throw th;
        }
    }

    List<PackageInfo> injectGetPackagesWithUninstalled(int userId) throws RemoteException {
        ParceledListSlice<PackageInfo> parceledList = this.mIPackageManager.getInstalledPackages(PACKAGE_MATCH_FLAGS, userId);
        if (parceledList == null) {
            return Collections.emptyList();
        }
        return parceledList.getList();
    }

    private void forUpdatedPackages(int userId, long lastScanTime, boolean afterOta, Consumer<ApplicationInfo> callback) {
        List<PackageInfo> list = getInstalledPackages(userId);
        for (int i = list.size() - 1; i >= 0; i--) {
            PackageInfo pi = (PackageInfo) list.get(i);
            if (pi.lastUpdateTime >= lastScanTime || (afterOta && isPureSystemApp(pi.applicationInfo))) {
                callback.accept(pi.applicationInfo);
            }
        }
    }

    private boolean isPureSystemApp(ApplicationInfo ai) {
        return ai.isSystemApp() && !ai.isUpdatedSystemApp();
    }

    private boolean isApplicationFlagSet(String packageName, int userId, int flags) {
        ApplicationInfo ai = injectApplicationInfoWithUninstalled(packageName, userId);
        if (ai == null || (ai.flags & flags) != flags) {
            return false;
        }
        return true;
    }

    private static boolean isInstalled(ApplicationInfo ai) {
        return (ai == null || (ai.flags & 8388608) == 0) ? false : true;
    }

    private static boolean isInstalled(PackageInfo pi) {
        return pi != null ? isInstalled(pi.applicationInfo) : false;
    }

    private static boolean isInstalled(ActivityInfo ai) {
        return ai != null ? isInstalled(ai.applicationInfo) : false;
    }

    private static ApplicationInfo isInstalledOrNull(ApplicationInfo ai) {
        return isInstalled(ai) ? ai : null;
    }

    private static PackageInfo isInstalledOrNull(PackageInfo pi) {
        return isInstalled(pi) ? pi : null;
    }

    private static ActivityInfo isInstalledOrNull(ActivityInfo ai) {
        return isInstalled(ai) ? ai : null;
    }

    boolean isPackageInstalled(String packageName, int userId) {
        return getApplicationInfo(packageName, userId) != null;
    }

    XmlResourceParser injectXmlMetaData(ActivityInfo activityInfo, String key) {
        return activityInfo.loadXmlMetaData(this.mContext.getPackageManager(), key);
    }

    Resources injectGetResourcesForApplicationAsUser(String packageName, int userId) {
        long start = injectElapsedRealtime();
        long token = injectClearCallingIdentity();
        try {
            Resources resourcesForApplicationAsUser = this.mContext.getPackageManager().getResourcesForApplicationAsUser(packageName, userId);
            injectRestoreCallingIdentity(token);
            logDurationStat(9, start);
            return resourcesForApplicationAsUser;
        } catch (NameNotFoundException e) {
            Slog.e(TAG, "Resources for package " + packageName + " not found");
            injectRestoreCallingIdentity(token);
            logDurationStat(9, start);
            return null;
        } catch (Throwable th) {
            injectRestoreCallingIdentity(token);
            logDurationStat(9, start);
            throw th;
        }
    }

    private Intent getMainActivityIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory(LAUNCHER_INTENT_CATEGORY);
        return intent;
    }

    List<ResolveInfo> queryActivities(Intent baseIntent, String packageName, ComponentName activity, int userId) {
        baseIntent.setPackage((String) Preconditions.checkNotNull(packageName));
        if (activity != null) {
            baseIntent.setComponent(activity);
        }
        List<ResolveInfo> resolved = this.mContext.getPackageManager().queryIntentActivitiesAsUser(baseIntent, PACKAGE_MATCH_FLAGS, userId);
        if (resolved == null || resolved.size() == 0) {
            return EMPTY_RESOLVE_INFO;
        }
        if (!isInstalled(((ResolveInfo) resolved.get(0)).activityInfo)) {
            return EMPTY_RESOLVE_INFO;
        }
        resolved.removeIf(ACTIVITY_NOT_EXPORTED);
        return resolved;
    }

    ComponentName injectGetDefaultMainActivity(String packageName, int userId) {
        ComponentName componentName = null;
        long start = injectElapsedRealtime();
        long token = injectClearCallingIdentity();
        try {
            List<ResolveInfo> resolved = queryActivities(getMainActivityIntent(), packageName, null, userId);
            if (resolved.size() != 0) {
                componentName = ((ResolveInfo) resolved.get(0)).activityInfo.getComponentName();
            }
            injectRestoreCallingIdentity(token);
            logDurationStat(11, start);
            return componentName;
        } catch (Throwable th) {
            injectRestoreCallingIdentity(token);
            logDurationStat(11, start);
        }
    }

    boolean injectIsMainActivity(ComponentName activity, int userId) {
        boolean z = false;
        long start = injectElapsedRealtime();
        long token = injectClearCallingIdentity();
        try {
            if (queryActivities(getMainActivityIntent(), activity.getPackageName(), activity, userId).size() > 0) {
                z = true;
            }
            injectRestoreCallingIdentity(token);
            logDurationStat(12, start);
            return z;
        } catch (Throwable th) {
            injectRestoreCallingIdentity(token);
            logDurationStat(12, start);
        }
    }

    List<ResolveInfo> injectGetMainActivities(String packageName, int userId) {
        long start = injectElapsedRealtime();
        long token = injectClearCallingIdentity();
        try {
            List<ResolveInfo> queryActivities = queryActivities(getMainActivityIntent(), packageName, null, userId);
            return queryActivities;
        } finally {
            injectRestoreCallingIdentity(token);
            logDurationStat(12, start);
        }
    }

    boolean injectIsActivityEnabledAndExported(ComponentName activity, int userId) {
        boolean z = false;
        long start = injectElapsedRealtime();
        long token = injectClearCallingIdentity();
        try {
            if (queryActivities(new Intent(), activity.getPackageName(), activity, userId).size() > 0) {
                z = true;
            }
            injectRestoreCallingIdentity(token);
            logDurationStat(13, start);
            return z;
        } catch (Throwable th) {
            injectRestoreCallingIdentity(token);
            logDurationStat(13, start);
        }
    }

    boolean injectIsSafeModeEnabled() {
        long token = injectClearCallingIdentity();
        boolean isSafeModeEnabled;
        try {
            isSafeModeEnabled = IWindowManager.Stub.asInterface(ServiceManager.getService("window")).isSafeModeEnabled();
            return isSafeModeEnabled;
        } catch (RemoteException e) {
            isSafeModeEnabled = false;
            return isSafeModeEnabled;
        } finally {
            injectRestoreCallingIdentity(token);
        }
    }

    boolean shouldBackupApp(String packageName, int userId) {
        return isApplicationFlagSet(packageName, userId, 32768);
    }

    boolean shouldBackupApp(PackageInfo pi) {
        return (pi.applicationInfo.flags & 32768) != 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0074 A:{Splitter: B:17:0x006b, ExcHandler: org.xmlpull.v1.XmlPullParserException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:23:0x0074, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:25:?, code:
            android.util.Slog.w(TAG, "Backup failed.", r0);
     */
    /* JADX WARNING: Missing block: B:27:0x007f, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public byte[] getBackupPayload(int userId) {
        enforceSystem();
        synchronized (this.mLock) {
            if (isUserUnlockedL(userId)) {
                ShortcutUser user = getUserShortcutsLocked(userId);
                if (user == null) {
                    wtf("Can't backup: user not found: id=" + userId);
                    return null;
                }
                user.forAllPackageItems(new -byte__getBackupPayload_int_userId_LambdaImpl0());
                user.forAllLaunchers(new -byte__getBackupPayload_int_userId_LambdaImpl1());
                scheduleSaveUser(userId);
                -com_android_server_pm_ShortcutService-mthref-0();
                ByteArrayOutputStream os = new ByteArrayOutputStream(32768);
                try {
                    saveUserInternalLocked(userId, os, true);
                    byte[] toByteArray = os.toByteArray();
                    return toByteArray;
                } catch (Exception e) {
                }
            } else {
                wtf("Can't backup: user " + userId + " is locked or not running");
                return null;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0048 A:{Splitter: B:11:0x0032, ExcHandler: org.xmlpull.v1.XmlPullParserException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0048 A:{Splitter: B:11:0x0032, ExcHandler: org.xmlpull.v1.XmlPullParserException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:17:0x0048, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:19:?, code:
            android.util.Slog.w(TAG, "Restoration failed.", r0);
     */
    /* JADX WARNING: Missing block: B:21:0x0053, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void applyRestore(byte[] payload, int userId) {
        enforceSystem();
        synchronized (this.mLock) {
            if (isUserUnlockedL(userId)) {
                try {
                    getUserShortcutsLocked(userId).mergeRestoredFile(loadUserInternal(userId, new ByteArrayInputStream(payload), true));
                    rescanUpdatedPackagesLocked(userId, 0, true);
                    saveUserLocked(userId);
                } catch (Exception e) {
                }
            } else {
                wtf("Can't restore: user " + userId + " is locked or not running");
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        enforceCallingOrSelfPermission("android.permission.DUMP", "can't dump by this caller");
        boolean checkin = false;
        boolean clear = false;
        if (args != null) {
            for (String arg : args) {
                if ("-c".equals(arg)) {
                    checkin = true;
                } else if ("--checkin".equals(arg)) {
                    checkin = true;
                    clear = true;
                }
            }
        }
        if (checkin) {
            dumpCheckin(pw, clear);
        } else {
            dumpInner(pw);
        }
    }

    private void dumpInner(PrintWriter pw) {
        synchronized (this.mLock) {
            int i;
            long now = injectCurrentTimeMillis();
            pw.print("Now: [");
            pw.print(now);
            pw.print("] ");
            pw.print(formatTime(now));
            pw.print("  Raw last reset: [");
            pw.print(this.mRawLastResetTime);
            pw.print("] ");
            pw.print(formatTime(this.mRawLastResetTime));
            long last = getLastResetTimeLocked();
            pw.print("  Last reset: [");
            pw.print(last);
            pw.print("] ");
            pw.print(formatTime(last));
            long next = getNextResetTimeLocked();
            pw.print("  Next reset: [");
            pw.print(next);
            pw.print("] ");
            pw.print(formatTime(next));
            pw.print("  Config:");
            pw.print("    Max icon dim: ");
            pw.println(this.mMaxIconDimension);
            pw.print("    Icon format: ");
            pw.println(this.mIconPersistFormat);
            pw.print("    Icon quality: ");
            pw.println(this.mIconPersistQuality);
            pw.print("    saveDelayMillis: ");
            pw.println(this.mSaveDelayMillis);
            pw.print("    resetInterval: ");
            pw.println(this.mResetInterval);
            pw.print("    maxUpdatesPerInterval: ");
            pw.println(this.mMaxUpdatesPerInterval);
            pw.print("    maxShortcutsPerActivity: ");
            pw.println(this.mMaxShortcuts);
            pw.println();
            pw.println("  Stats:");
            synchronized (this.mStatLock) {
                for (i = 0; i < 16; i++) {
                    dumpStatLS(pw, "    ", i);
                }
            }
            pw.println();
            pw.print("  #Failures: ");
            pw.println(this.mWtfCount);
            if (this.mLastWtfStacktrace != null) {
                pw.print("  Last failure stack trace: ");
                pw.println(Log.getStackTraceString(this.mLastWtfStacktrace));
            }
            for (i = 0; i < this.mUsers.size(); i++) {
                pw.println();
                ((ShortcutUser) this.mUsers.valueAt(i)).dump(pw, "  ");
            }
            pw.println();
            pw.println("  UID state:");
            for (i = 0; i < this.mUidState.size(); i++) {
                int uid = this.mUidState.keyAt(i);
                int state = this.mUidState.valueAt(i);
                pw.print("    UID=");
                pw.print(uid);
                pw.print(" state=");
                pw.print(state);
                if (isProcessStateForeground(state)) {
                    pw.print("  [FG]");
                }
                pw.print("  last FG=");
                pw.print(this.mUidLastForegroundElapsedTime.get(uid));
                pw.println();
            }
        }
    }

    static String formatTime(long time) {
        Time tobj = new Time();
        tobj.set(time);
        return tobj.format("%Y-%m-%d %H:%M:%S");
    }

    private void dumpStatLS(PrintWriter pw, String prefix, int statId) {
        pw.print(prefix);
        int count = this.mCountStats[statId];
        long dur = this.mDurationStats[statId];
        String str = "%s: count=%d, total=%dms, avg=%.1fms";
        Object[] objArr = new Object[4];
        objArr[0] = STAT_LABELS[statId];
        objArr[1] = Integer.valueOf(count);
        objArr[2] = Long.valueOf(dur);
        objArr[3] = Double.valueOf(count == 0 ? 0.0d : ((double) dur) / ((double) count));
        pw.println(String.format(str, objArr));
    }

    private void dumpCheckin(PrintWriter pw, boolean clear) {
        synchronized (this.mLock) {
            try {
                JSONArray users = new JSONArray();
                for (int i = 0; i < this.mUsers.size(); i++) {
                    users.put(((ShortcutUser) this.mUsers.valueAt(i)).dumpCheckin(clear));
                }
                JSONObject result = new JSONObject();
                result.put(KEY_SHORTCUT, users);
                result.put(KEY_LOW_RAM, injectIsLowRamDevice());
                result.put(KEY_ICON_SIZE, this.mMaxIconDimension);
                pw.println(result.toString(1));
            } catch (JSONException e) {
                Slog.e(TAG, "Unable to write in json", e);
            }
        }
        return;
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) throws RemoteException {
        enforceShell();
        long token = injectClearCallingIdentity();
        try {
            resultReceiver.send(new MyShellCommand(this, null).exec(this, in, out, err, args, resultReceiver), null);
        } finally {
            injectRestoreCallingIdentity(token);
        }
    }

    long injectCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    long injectElapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }

    int injectBinderCallingUid() {
        return getCallingUid();
    }

    private int getCallingUserId() {
        return UserHandle.getUserId(injectBinderCallingUid());
    }

    long injectClearCallingIdentity() {
        return Binder.clearCallingIdentity();
    }

    void injectRestoreCallingIdentity(long token) {
        Binder.restoreCallingIdentity(token);
    }

    String injectBuildFingerprint() {
        return Build.FINGERPRINT;
    }

    final void wtf(String message) {
        wtf(message, null);
    }

    void wtf(String message, Throwable e) {
        if (e == null) {
            e = new RuntimeException("Stacktrace");
        }
        synchronized (this.mLock) {
            this.mWtfCount++;
            this.mLastWtfStacktrace = new Exception("Last failure was logged here:");
        }
        Slog.wtf(TAG, message, e);
    }

    File injectSystemDataPath() {
        return Environment.getDataSystemDirectory();
    }

    File injectUserDataPath(int userId) {
        return new File(Environment.getDataSystemCeDirectory(userId), DIRECTORY_PER_USER);
    }

    boolean injectIsLowRamDevice() {
        return ActivityManager.isLowRamDeviceStatic();
    }

    void injectRegisterUidObserver(IUidObserver observer, int which) {
        try {
            ActivityManagerNative.getDefault().registerUidObserver(observer, which);
        } catch (RemoteException e) {
        }
    }

    File getUserBitmapFilePath(int userId) {
        return new File(injectUserDataPath(userId), DIRECTORY_BITMAPS);
    }

    SparseArray<ShortcutUser> getShortcutsForTest() {
        return this.mUsers;
    }

    int getMaxShortcutsForTest() {
        return this.mMaxShortcuts;
    }

    int getMaxUpdatesPerIntervalForTest() {
        return this.mMaxUpdatesPerInterval;
    }

    long getResetIntervalForTest() {
        return this.mResetInterval;
    }

    int getMaxIconDimensionForTest() {
        return this.mMaxIconDimension;
    }

    CompressFormat getIconPersistFormatForTest() {
        return this.mIconPersistFormat;
    }

    int getIconPersistQualityForTest() {
        return this.mIconPersistQuality;
    }

    ShortcutPackage getPackageShortcutForTest(String packageName, int userId) {
        synchronized (this.mLock) {
            ShortcutUser user = (ShortcutUser) this.mUsers.get(userId);
            if (user == null) {
                return null;
            }
            ShortcutPackage shortcutPackage = (ShortcutPackage) user.getAllPackagesForTest().get(packageName);
            return shortcutPackage;
        }
    }

    ShortcutInfo getPackageShortcutForTest(String packageName, String shortcutId, int userId) {
        synchronized (this.mLock) {
            ShortcutPackage pkg = getPackageShortcutForTest(packageName, userId);
            if (pkg == null) {
                return null;
            }
            ShortcutInfo findShortcutById = pkg.findShortcutById(shortcutId);
            return findShortcutById;
        }
    }

    boolean injectShouldPerformVerification() {
        return false;
    }

    final void verifyStates() {
        if (injectShouldPerformVerification()) {
            verifyStatesInner();
        }
    }

    private final void verifyStatesForce() {
        verifyStatesInner();
    }

    private void verifyStatesInner() {
        synchronized (this.mLock) {
            forEachLoadedUserLocked(new -void_verifyStatesInner__LambdaImpl0());
        }
    }
}
