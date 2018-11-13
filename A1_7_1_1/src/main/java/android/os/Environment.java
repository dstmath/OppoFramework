package android.os;

import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;

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
public class Environment {
    public static String DIRECTORY_ALARMS = null;
    @Deprecated
    public static final String DIRECTORY_ANDROID = "Android";
    public static String DIRECTORY_DCIM = null;
    public static String DIRECTORY_DOCUMENTS = null;
    public static String DIRECTORY_DOWNLOADS = null;
    public static String DIRECTORY_MOVIES = null;
    public static String DIRECTORY_MUSIC = null;
    public static String DIRECTORY_NOTIFICATIONS = null;
    public static String DIRECTORY_PICTURES = null;
    public static String DIRECTORY_PODCASTS = null;
    public static String DIRECTORY_RINGTONES = null;
    public static final String DIRECTORY_USBOTG = null;
    public static final String DIR_ANDROID = "Android";
    private static final File DIR_ANDROID_DATA = null;
    private static final File DIR_ANDROID_EXPAND = null;
    private static final File DIR_ANDROID_ROOT = null;
    private static final File DIR_ANDROID_STORAGE = null;
    private static final String DIR_CACHE = "cache";
    private static final String DIR_DATA = "data";
    private static final File DIR_DOWNLOAD_CACHE = null;
    private static final String DIR_FILES = "files";
    private static final String DIR_MEDIA = "media";
    private static final String DIR_OBB = "obb";
    private static final File DIR_ODM_ROOT = null;
    private static final File DIR_OEM_ROOT = null;
    private static final File DIR_VENDOR_ROOT = null;
    private static final String ENV_ANDROID_DATA = "ANDROID_DATA";
    private static final String ENV_ANDROID_EXPAND = "ANDROID_EXPAND";
    private static final String ENV_ANDROID_ROOT = "ANDROID_ROOT";
    private static final String ENV_ANDROID_STORAGE = "ANDROID_STORAGE";
    private static final String ENV_DOWNLOAD_CACHE = "DOWNLOAD_CACHE";
    private static final String ENV_EXTERNAL_STORAGE = "EXTERNAL_STORAGE";
    private static final String ENV_ODM_ROOT = "ODM_ROOT";
    private static final String ENV_OEM_ROOT = "OEM_ROOT";
    private static final String ENV_USBOTG_STORAGE = "USBOTG_STORAGE";
    private static final String ENV_VENDOR_ROOT = "VENDOR_ROOT";
    public static final String MEDIA_BAD_REMOVAL = "bad_removal";
    public static final String MEDIA_CHECKING = "checking";
    public static final String MEDIA_EJECTING = "ejecting";
    public static final String MEDIA_MOUNTED = "mounted";
    public static final String MEDIA_MOUNTED_READ_ONLY = "mounted_ro";
    public static final String MEDIA_NOFS = "nofs";
    public static final String MEDIA_REMOVED = "removed";
    public static final String MEDIA_SHARED = "shared";
    public static final String MEDIA_UNKNOWN = "unknown";
    public static final String MEDIA_UNMOUNTABLE = "unmountable";
    public static final String MEDIA_UNMOUNTED = "unmounted";
    private static final boolean NOT_ALLOW_EXT4_ACCESS = true;
    public static final String[] STANDARD_DIRECTORIES = null;
    private static final String TAG = "Environment";
    private static final String USBOTG_PATH_ZONE = "usbotg-sd";
    private static UserEnvironment sCurrentUser;
    private static boolean sUserRequired;

    public static class UserEnvironment {
        private final int mUserId;

        public UserEnvironment(int userId) {
            this.mUserId = userId;
        }

        public File[] getExternalDirs() {
            StorageVolume[] volumes = StorageManager.getVolumeList(this.mUserId, 256);
            File[] files = new File[volumes.length];
            for (int i = 0; i < volumes.length; i++) {
                files[i] = volumes[i].getPathFile();
            }
            return files;
        }

        @Deprecated
        public File getExternalStorageDirectory() {
            return getExternalDirs()[0];
        }

        @Deprecated
        public File getExternalStoragePublicDirectory(String type) {
            return buildExternalStoragePublicDirs(type)[0];
        }

        public File[] buildExternalStoragePublicDirs(String type) {
            File[] externalDirs = getExternalDirs();
            String[] strArr = new String[1];
            strArr[0] = type;
            return Environment.buildPaths(externalDirs, strArr);
        }

        public File[] buildExternalStorageAndroidDataDirs() {
            File[] externalDirs = getExternalDirs();
            String[] strArr = new String[2];
            strArr[0] = "Android";
            strArr[1] = "data";
            return Environment.buildPaths(externalDirs, strArr);
        }

        public File[] buildExternalStorageAndroidObbDirs() {
            File[] externalDirs = getExternalDirs();
            String[] strArr = new String[2];
            strArr[0] = "Android";
            strArr[1] = "obb";
            return Environment.buildPaths(externalDirs, strArr);
        }

        public File[] buildExternalStorageAppDataDirs(String packageName) {
            File[] externalDirs = getExternalDirs();
            String[] strArr = new String[3];
            strArr[0] = "Android";
            strArr[1] = "data";
            strArr[2] = packageName;
            return Environment.buildPaths(externalDirs, strArr);
        }

        public File[] buildExternalStorageAppMediaDirs(String packageName) {
            File[] externalDirs = getExternalDirs();
            String[] strArr = new String[3];
            strArr[0] = "Android";
            strArr[1] = "media";
            strArr[2] = packageName;
            return Environment.buildPaths(externalDirs, strArr);
        }

        public File[] buildExternalStorageAppObbDirs(String packageName) {
            File[] externalDirs = getExternalDirs();
            String[] strArr = new String[3];
            strArr[0] = "Android";
            strArr[1] = "obb";
            strArr[2] = packageName;
            return Environment.buildPaths(externalDirs, strArr);
        }

        public File[] buildExternalStorageAppFilesDirs(String packageName) {
            File[] externalDirs = getExternalDirs();
            String[] strArr = new String[4];
            strArr[0] = "Android";
            strArr[1] = "data";
            strArr[2] = packageName;
            strArr[3] = Environment.DIR_FILES;
            return Environment.buildPaths(externalDirs, strArr);
        }

        public File[] buildExternalStorageAppCacheDirs(String packageName) {
            File[] externalDirs = getExternalDirs();
            String[] strArr = new String[4];
            strArr[0] = "Android";
            strArr[1] = "data";
            strArr[2] = packageName;
            strArr[3] = Environment.DIR_CACHE;
            return Environment.buildPaths(externalDirs, strArr);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.os.Environment.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.os.Environment.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.Environment.<clinit>():void");
    }

    public static void initForCurrentUser() {
        sCurrentUser = new UserEnvironment(UserHandle.myUserId());
    }

    public static File getRootDirectory() {
        return DIR_ANDROID_ROOT;
    }

    public static File getStorageDirectory() {
        return DIR_ANDROID_STORAGE;
    }

    public static File getOemDirectory() {
        return DIR_OEM_ROOT;
    }

    public static File getOdmDirectory() {
        return DIR_ODM_ROOT;
    }

    public static File getVendorDirectory() {
        return DIR_VENDOR_ROOT;
    }

    @Deprecated
    public static File getUserSystemDirectory(int userId) {
        return new File(new File(getDataSystemDirectory(), "users"), Integer.toString(userId));
    }

    @Deprecated
    public static File getUserConfigDirectory(int userId) {
        return new File(new File(new File(getDataDirectory(), "misc"), Context.USER_SERVICE), Integer.toString(userId));
    }

    public static File getDataDirectory() {
        return DIR_ANDROID_DATA;
    }

    public static File getDataDirectory(String volumeUuid) {
        if (TextUtils.isEmpty(volumeUuid)) {
            return DIR_ANDROID_DATA;
        }
        return new File("/mnt/expand/" + volumeUuid);
    }

    public static File getExpandDirectory() {
        return DIR_ANDROID_EXPAND;
    }

    public static File getDataSystemDirectory() {
        return new File(getDataDirectory(), "system");
    }

    public static File getDataSystemDeDirectory() {
        File dataDirectory = getDataDirectory();
        String[] strArr = new String[1];
        strArr[0] = "system_de";
        return buildPath(dataDirectory, strArr);
    }

    public static File getDataSystemCeDirectory() {
        File dataDirectory = getDataDirectory();
        String[] strArr = new String[1];
        strArr[0] = "system_ce";
        return buildPath(dataDirectory, strArr);
    }

    public static File getDataSystemCeDirectory(int userId) {
        File dataDirectory = getDataDirectory();
        String[] strArr = new String[2];
        strArr[0] = "system_ce";
        strArr[1] = String.valueOf(userId);
        return buildPath(dataDirectory, strArr);
    }

    public static File getDataSystemDeDirectory(int userId) {
        File dataDirectory = getDataDirectory();
        String[] strArr = new String[2];
        strArr[0] = "system_de";
        strArr[1] = String.valueOf(userId);
        return buildPath(dataDirectory, strArr);
    }

    public static File getDataMiscDirectory() {
        return new File(getDataDirectory(), "misc");
    }

    public static File getDataMiscCeDirectory(int userId) {
        File dataDirectory = getDataDirectory();
        String[] strArr = new String[2];
        strArr[0] = "misc_ce";
        strArr[1] = String.valueOf(userId);
        return buildPath(dataDirectory, strArr);
    }

    public static File getDataMiscDeDirectory(int userId) {
        File dataDirectory = getDataDirectory();
        String[] strArr = new String[2];
        strArr[0] = "misc_de";
        strArr[1] = String.valueOf(userId);
        return buildPath(dataDirectory, strArr);
    }

    private static File getDataProfilesDeDirectory(int userId) {
        File dataDirectory = getDataDirectory();
        String[] strArr = new String[4];
        strArr[0] = "misc";
        strArr[1] = "profiles";
        strArr[2] = "cur";
        strArr[3] = String.valueOf(userId);
        return buildPath(dataDirectory, strArr);
    }

    public static File getReferenceProfile(String packageName) {
        File dataDirectory = getDataDirectory();
        String[] strArr = new String[4];
        strArr[0] = "misc";
        strArr[1] = "profiles";
        strArr[2] = "ref";
        strArr[3] = packageName;
        return buildPath(dataDirectory, strArr);
    }

    public static File getDataProfilesDePackageDirectory(int userId, String packageName) {
        File dataProfilesDeDirectory = getDataProfilesDeDirectory(userId);
        String[] strArr = new String[1];
        strArr[0] = packageName;
        return buildPath(dataProfilesDeDirectory, strArr);
    }

    public static File getDataProfilesDeForeignDexDirectory(int userId) {
        File dataProfilesDeDirectory = getDataProfilesDeDirectory(userId);
        String[] strArr = new String[1];
        strArr[0] = "foreign-dex";
        return buildPath(dataProfilesDeDirectory, strArr);
    }

    public static File getDataAppDirectory(String volumeUuid) {
        return new File(getDataDirectory(volumeUuid), "app");
    }

    public static File getDataAppEphemeralDirectory(String volumeUuid) {
        return new File(getDataDirectory(volumeUuid), "app-ephemeral");
    }

    public static File getDataUserCeDirectory(String volumeUuid) {
        return new File(getDataDirectory(volumeUuid), Context.USER_SERVICE);
    }

    public static File getDataUserCeDirectory(String volumeUuid, int userId) {
        return new File(getDataUserCeDirectory(volumeUuid), String.valueOf(userId));
    }

    public static File getDataUserCePackageDirectory(String volumeUuid, int userId, String packageName) {
        return new File(getDataUserCeDirectory(volumeUuid, userId), packageName);
    }

    public static File getDataUserDeDirectory(String volumeUuid) {
        return new File(getDataDirectory(volumeUuid), "user_de");
    }

    public static File getDataUserDeDirectory(String volumeUuid, int userId) {
        return new File(getDataUserDeDirectory(volumeUuid), String.valueOf(userId));
    }

    public static File getDataUserDePackageDirectory(String volumeUuid, int userId, String packageName) {
        return new File(getDataUserDeDirectory(volumeUuid, userId), packageName);
    }

    public static File getDataPreloadsDirectory() {
        return new File(getDataDirectory(), "preloads");
    }

    public static File getDataPreloadsDemoDirectory() {
        return new File(getDataPreloadsDirectory(), "demo");
    }

    public static File getDataPreloadsAppsDirectory() {
        return new File(getDataPreloadsDirectory(), "apps");
    }

    public static File getDataPreloadsMediaDirectory() {
        return new File(getDataPreloadsDirectory(), "media");
    }

    public static File getExternalStorageDirectory() {
        throwIfUserRequired();
        File[] files = sCurrentUser.getExternalDirs();
        if (files != null && files.length != 0) {
            return files[0];
        }
        Log.e("environment", "getExternalStorageState files.length == 0 ");
        return null;
    }

    public static File getLegacyExternalStorageDirectory() {
        return new File(System.getenv(ENV_EXTERNAL_STORAGE));
    }

    public static File getLegacyExternalStorageObbDirectory() {
        File legacyExternalStorageDirectory = getLegacyExternalStorageDirectory();
        String[] strArr = new String[2];
        strArr[0] = "Android";
        strArr[1] = "obb";
        return buildPath(legacyExternalStorageDirectory, strArr);
    }

    public static boolean isStandardDirectory(String dir) {
        for (String valid : STANDARD_DIRECTORIES) {
            if (valid.equals(dir)) {
                return true;
            }
        }
        return false;
    }

    public static File getExternalStoragePublicDirectory(String type) {
        throwIfUserRequired();
        return sCurrentUser.buildExternalStoragePublicDirs(type)[0];
    }

    public static File[] buildExternalStorageAndroidDataDirs() {
        throwIfUserRequired();
        return sCurrentUser.buildExternalStorageAndroidDataDirs();
    }

    public static File[] buildExternalStorageAppDataDirs(String packageName) {
        throwIfUserRequired();
        return sCurrentUser.buildExternalStorageAppDataDirs(packageName);
    }

    public static File[] buildExternalStorageAppMediaDirs(String packageName) {
        throwIfUserRequired();
        return sCurrentUser.buildExternalStorageAppMediaDirs(packageName);
    }

    public static File[] buildExternalStorageAppObbDirs(String packageName) {
        throwIfUserRequired();
        return sCurrentUser.buildExternalStorageAppObbDirs(packageName);
    }

    public static File[] buildExternalStorageAppFilesDirs(String packageName) {
        throwIfUserRequired();
        return sCurrentUser.buildExternalStorageAppFilesDirs(packageName);
    }

    public static File[] buildExternalStorageAppCacheDirs(String packageName) {
        throwIfUserRequired();
        return sCurrentUser.buildExternalStorageAppCacheDirs(packageName);
    }

    public static File getDownloadCacheDirectory() {
        return DIR_DOWNLOAD_CACHE;
    }

    public static String getExternalStorageState() {
        File[] files = sCurrentUser.getExternalDirs();
        if (files != null && files.length != 0) {
            return getExternalStorageState(files[0]);
        }
        Log.e("environment", "getExternalStorageState files.length == 0 ");
        return "unknown";
    }

    @Deprecated
    public static String getStorageState(File path) {
        return getExternalStorageState(path);
    }

    public static String getExternalStorageState(File path) {
        StorageVolume volume = StorageManager.getStorageVolume(path, UserHandle.myUserId());
        if (volume != null) {
            return volume.getState();
        }
        return "unknown";
    }

    public static boolean isExternalStorageRemovable() {
        if (isStorageDisabled()) {
            return false;
        }
        return isExternalStorageRemovable(sCurrentUser.getExternalDirs()[0]);
    }

    public static boolean isExternalStorageRemovable(File path) {
        StorageVolume volume = StorageManager.getStorageVolume(path, UserHandle.myUserId());
        if (volume != null) {
            return volume.isRemovable();
        }
        Log.d(TAG, "isExternalStorageRemovable, Failed to find storage device at " + path);
        return false;
    }

    public static boolean isExternalStorageEmulated() {
        if (isStorageDisabled()) {
            return false;
        }
        return isExternalStorageEmulated(sCurrentUser.getExternalDirs()[0]);
    }

    public static boolean isExternalStorageEmulated(File path) {
        StorageVolume volume = StorageManager.getStorageVolume(path, UserHandle.myUserId());
        if (volume != null) {
            return volume.isEmulated();
        }
        Log.d(TAG, "isExternalStorageEmulated, Failed to find storage device at " + path);
        return false;
    }

    static File getDirectory(String variableName, String defaultPath) {
        String path = System.getenv(variableName);
        return path == null ? new File(defaultPath) : new File(path);
    }

    public static void setUserRequired(boolean userRequired) {
        sUserRequired = userRequired;
    }

    private static void throwIfUserRequired() {
        if (sUserRequired) {
            Log.wtf(TAG, "Path requests must specify a user by using UserEnvironment", new Throwable());
        }
    }

    public static File[] buildPaths(File[] base, String... segments) {
        File[] result = new File[base.length];
        for (int i = 0; i < base.length; i++) {
            result[i] = buildPath(base[i], segments);
        }
        return result;
    }

    public static File buildPath(File base, String... segments) {
        File cur = base;
        int i = 0;
        int length = segments.length;
        File cur2 = cur;
        while (i < length) {
            String segment = segments[i];
            if (cur2 == null) {
                cur = new File(segment);
            } else {
                cur = new File(cur2, segment);
            }
            i++;
            cur2 = cur;
        }
        return cur2;
    }

    public static boolean isUsbotg(String path) {
        if (path.length() <= USBOTG_PATH_ZONE.length()) {
            return false;
        }
        return path.contains(USBOTG_PATH_ZONE);
    }

    public static String getOtgDescription(String path) {
        if (path.length() <= USBOTG_PATH_ZONE.length()) {
            return null;
        }
        String[] splited = path.split("/");
        return splited[splited.length - 1];
    }

    private static boolean isStorageDisabled() {
        return SystemProperties.getBoolean("config.disable_storage", false);
    }

    public static File maybeTranslateEmulatedPathToInternal(File path) {
        return path;
    }
}
