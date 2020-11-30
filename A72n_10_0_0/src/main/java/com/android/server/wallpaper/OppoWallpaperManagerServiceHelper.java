package com.android.server.wallpaper;

import android.app.OppoWallpaperManagerHelper;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.server.wallpaper.WallpaperManagerService;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoWallpaperManagerServiceHelper {
    private static final String AUTO_PLAY_SWITCH = "oppo_pictorial_auto_play";
    private static final String BASE_OPPO_PRODUCT_DEFAULT_DIR = "/decouping_wallpaper/default/";
    private static final String BASE_OPPO_WALLPAPER_DEFAULT_DIR = "/decouping_wallpaper/";
    private static final int CHANGE_WALLPAPER_DISABLED = 1;
    private static final String CHANGE_WALLPAPER_DISABLED_STATE = "changeWallpaperDisabledState";
    private static final int CHANGE_WALLPAPER_ENABLE = 0;
    private static final int CUSTOMIZE_WALLPAPER_FINISH_INIT_STATE = 1;
    private static final String CUSTOMIZE_WALLPAPER_INIT_STATE = "customize_wallpaper_init_state";
    private static final String CUSTOM_LOCK_WALLPAPER_NAME = "default_wallpaper_lock";
    private static final String CUSTOM_WALLPAPER_DIR = "/custom/media/wallpaper/default";
    private static final String CUSTOM_WALLPAPER_NAME = "default_wallpaper";
    private static final int DEFAULT_CUSTOMIZE_WALLPAPER_INIT_STATE = 0;
    private static final String DEFAULT_LOCK_WALLPAPER_NAME = "oppo_default_wallpaper_lock";
    private static final String DEFAULT_WALLPAPER_NAME = "oppo_default_wallpaper";
    private static final String HEYTAP_UNLOCK_CHANGE_CLASS = "oppo_unlock_change_class";
    private static final String HEYTAP_UNLOCK_CHANGE_PKG = "oppo_unlock_change_pkg";
    private static final String HEYTAP_UNLOCK_CLASS_IN_KEYGUARD_PACKAGE = "com.android.keyguard.KeyguardService";
    private static final String HEYTAP_UNLOCK_CLASS_IN_SYSTEM_UI_PACKAGE = "com.android.systemui.keyguard.KeyguardService";
    private static final String KEYGUARD_PACKAGE_NAME = "com.android.keyguard";
    private static final String KEY_LAST_BUILD_CUSTOM_VERSON_OTA = "key_last_custom_version_ota";
    private static final String MAIN_SWITCH = "oppo_pictorial_apply";
    private static final String OPERATOR = "operator";
    private static final String OPPO_COTA_ROOT_PATH = "/my_cota";
    private static final String OPPO_COTA_WALLPAPER = getCotaDirectory().getAbsolutePath();
    private static final String OPPO_CUSTOM_ROOT_PATH = "/oppo_custom";
    private static final String OPPO_CUSTOM_WALLPAPER = getOppoCustomDirectory().getAbsolutePath();
    private static final String OPPO_CUSTOM_WALLPAPER_DIR = (OPPO_CUSTOM_WALLPAPER + WALLPAPER_CUSTOM_FILE_DIR);
    private static final String OPPO_PRODUCT_ROOT_PATH = "/oppo_product";
    private static final String OPPO_PRODUCT_WALLPAPER = getOppoProductDirectory().getAbsolutePath();
    private static final String OPPO_VERSION_ROOT_PATH = "/oppo_version";
    private static final String PHONE_COLOR_MAPS_DYNAMIC_FILE_NAME = "phone_color_dynamic_default_theme_maps";
    private static final String PHONE_COLOR_MAPS_FILE_SUFFIX = ".xml";
    private static final String PROP_HW_PHONE_COLOR = "ro.hw.phone.color";
    private static final String RO_BUILD_CUSTOM_VERSON_OTA = "ro.build.custom.version.ota";
    private static final String RO_BUILD_CUSTOM_VERSON_OTA_DEFAULT = "null";
    private static final String SYSTEM_UI_PACKAGE_NAME = "com.android.systemui";
    private static String TAG = "OppoWallpaperManagerServiceHelper";
    private static final String TAG_DYNAMIC_WALLPAPER_CLASS_NAME = "oppo_default_dynamic_wallpaper_class_name";
    private static final String TAG_DYNAMIC_WALLPAPER_PACKAGE_NAME = "oppo_default_dynamic_wallpaper_package_name";
    private static final String TAG_PHONE_COLOR = "PhoneColor";
    private static final String WALLPAPER_CUSTOM_FILE_DIR = "/media/wallpaper/default";
    private static final String WALLPAPER_LOCK_CROP = "wallpaper_lock";
    private static final String WALLPAPER_LOCK_ORIG = "wallpaper_lock_orig";
    private static final String WALLPAPER_ORIG = "wallpaper_orig";
    private static final String WALLPAPER_SUFFIX = ".png";
    private static final String XML_ENCODING = "UTF-8";
    private static Map<String, String> sDefaultFileNameCache = new ArrayMap();

    private static String getCotaFileName(Context context, int which) {
        File oppoCotaWallpaperDir = new File(OPPO_COTA_WALLPAPER + WALLPAPER_CUSTOM_FILE_DIR);
        if (which == 2) {
            File oppoCotaWallpaperLock = new File(oppoCotaWallpaperDir, "default_wallpaper_lock.png");
            if (oppoCotaWallpaperLock.exists()) {
                Log.d(TAG, "getCotaFileName oppoCotaWallpaperLock exist");
                return oppoCotaWallpaperLock.getAbsolutePath();
            }
            Log.d(TAG, "getCotaFileName oppoCotaWallpaperLock not exist");
            return null;
        }
        File oppoCotaWallpaperSystem = new File(oppoCotaWallpaperDir, "default_wallpaper.png");
        if (oppoCotaWallpaperSystem.exists()) {
            Log.d(TAG, "getCotaFileName oppoCotaWallpaperSystem exist");
            return oppoCotaWallpaperSystem.getAbsolutePath();
        }
        Log.d(TAG, "getCotaFileName oppoCotaWallpaperSystem not exist ");
        return null;
    }

    private static String getCustomFileName(Context context, int which) {
        File oppoCustomWallpaperDir = new File(OPPO_CUSTOM_WALLPAPER + WALLPAPER_CUSTOM_FILE_DIR);
        if (which == 2) {
            File oppoCustomWallpaperLock = new File(oppoCustomWallpaperDir, "default_wallpaper_lock.png");
            if (oppoCustomWallpaperLock.exists()) {
                Log.d(TAG, "getCustomFileName oppoCustomWallpaperLock exist");
                return oppoCustomWallpaperLock.getAbsolutePath();
            }
            Log.d(TAG, "getCustomFileName oppoCustomWallpaperLock not exist");
            return null;
        }
        File oppoCustomWallpaperSystem = new File(oppoCustomWallpaperDir, "default_wallpaper.png");
        if (oppoCustomWallpaperSystem.exists()) {
            Log.d(TAG, "getCustomFileName oppoCustomWallpaperSystem exist");
            return oppoCustomWallpaperSystem.getAbsolutePath();
        }
        Log.d(TAG, "getCustomFileName oppoCustomWallpaperSystem not exist ");
        return null;
    }

    private static String getOperatorFileName(Context context, int which) {
        String fileName;
        String sysOperatorName = SystemProperties.get("ro.oppo.operator");
        if (!TextUtils.isEmpty(sysOperatorName)) {
            String sysOperatorName2 = sysOperatorName.trim().toLowerCase();
            if (which == 2) {
                fileName = OPPO_PRODUCT_WALLPAPER + BASE_OPPO_PRODUCT_DEFAULT_DIR + DEFAULT_LOCK_WALLPAPER_NAME + "_" + OPERATOR + "_" + sysOperatorName2 + WALLPAPER_SUFFIX;
            } else {
                fileName = OPPO_PRODUCT_WALLPAPER + BASE_OPPO_PRODUCT_DEFAULT_DIR + DEFAULT_WALLPAPER_NAME + "_" + OPERATOR + "_" + sysOperatorName2 + WALLPAPER_SUFFIX;
            }
            File file = new File(fileName);
            Log.d(TAG, "getOperatorFileName operator fileName = " + fileName);
            if (file.exists()) {
                return fileName;
            }
            Log.d(TAG, "getOperatorFileName operator not exist ");
            return null;
        }
        Log.d(TAG, "getOperatorFileName valid operator  " + sysOperatorName);
        return null;
    }

    public static boolean isThirdPartLauncherCall(Context context, String callingPackage) {
        if (TextUtils.isEmpty(callingPackage) || !isThirdPartApp(context, callingPackage)) {
            return false;
        }
        List<ResolveInfo> apps = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            Intent mainIntent = new Intent("android.intent.action.MAIN");
            mainIntent.addCategory("android.intent.category.HOME");
            mainIntent.setPackage(callingPackage);
            apps = packageManager.queryIntentActivities(mainIntent, 0);
        } catch (Exception e) {
            String str = TAG;
            Slog.d(str, "isThirdPartLauncherCall e = " + e);
        }
        if (apps == null || apps.size() <= 0) {
            return false;
        }
        return true;
    }

    public static boolean isThirdPartApp(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return true;
        }
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, 8192);
            if (appInfo == null || (appInfo.flags & 1) != 1) {
                return true;
            }
            return false;
        } catch (Exception e) {
            String str = TAG;
            Slog.d(str, "isThirdPartApp e = " + e);
        }
    }

    public static int getDefaultWallpaperWidth(Context context) {
        int width = -1;
        try {
            Resources res = context.getResources();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, OppoWallpaperManagerHelper.getDefaultWallpaperResID(context), options);
            width = options.outWidth;
            String str = TAG;
            Slog.w(str, "getDefaultWallpaperWidth(): width = " + width);
            return width;
        } catch (OutOfMemoryError e) {
            Slog.w(TAG, "getDefaultWallpaperWidth(): Can't decode res:", e);
            return width;
        }
    }

    public static void initCustomizeWallpaper(Context context) {
        if (!isCustomOtaVersionChanged(context)) {
            Log.i(TAG, "initCustomizeWallpaper customVersion not changed,no need");
            return;
        }
        setMagazineWallPaperSwitch(context, !new File(OPPO_CUSTOM_WALLPAPER_DIR, "default_wallpaper_lock.png").exists());
        File wallPaperFileDir = getWallpaperDir(0);
        File wallPaperOriginFile = new File(wallPaperFileDir, WALLPAPER_ORIG);
        File wallPaperLockOriginFile = new File(wallPaperFileDir, WALLPAPER_LOCK_ORIG);
        if (wallPaperOriginFile.exists()) {
            Log.d(TAG, "initCustomizeWallpaper to clean systm wallpaper");
            try {
                WallpaperManager.getInstance(context).clearWallpaper(1, context.getUserId());
            } catch (Exception e) {
                Log.e(TAG, "failed to clean system wallpaper");
            }
        }
        if (wallPaperLockOriginFile.exists()) {
            Log.d(TAG, "initCustomizeWallpaper to clean lock wallpaper");
            try {
                WallpaperManager.getInstance(context).clearWallpaper(2, context.getUserId());
            } catch (Exception e2) {
                Log.e(TAG, "failed to clean lock wallpaper");
            }
        }
    }

    private static boolean isCustomOtaVersionChanged(Context context) {
        String curVersion = SystemProperties.get(RO_BUILD_CUSTOM_VERSON_OTA, RO_BUILD_CUSTOM_VERSON_OTA_DEFAULT);
        if (TextUtils.isEmpty(curVersion) || curVersion.equals(RO_BUILD_CUSTOM_VERSON_OTA_DEFAULT)) {
            return false;
        }
        String lastVersion = Settings.Secure.getString(context.getContentResolver(), KEY_LAST_BUILD_CUSTOM_VERSON_OTA);
        if (!TextUtils.isEmpty(lastVersion) && lastVersion.equals(curVersion)) {
            return false;
        }
        Settings.Secure.putString(context.getContentResolver(), KEY_LAST_BUILD_CUSTOM_VERSON_OTA, curVersion);
        Log.v(TAG, "isCustomOtaVersionChanged = true");
        return true;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    private static void setMagazineWallPaperSwitch(Context context, boolean switchState) {
        String serviceClass;
        String keyguardPkg;
        String str = TAG;
        Log.i(str, "setMagazineWallPaperSwitch =" + switchState);
        Settings.System.putInt(context.getContentResolver(), MAIN_SWITCH, switchState ? 1 : 0);
        Settings.System.putInt(context.getContentResolver(), AUTO_PLAY_SWITCH, switchState);
        if (switchState != 0) {
            if (Build.VERSION.SDK_INT < 27) {
                keyguardPkg = KEYGUARD_PACKAGE_NAME;
                serviceClass = HEYTAP_UNLOCK_CLASS_IN_KEYGUARD_PACKAGE;
            } else {
                keyguardPkg = SYSTEM_UI_PACKAGE_NAME;
                serviceClass = HEYTAP_UNLOCK_CLASS_IN_SYSTEM_UI_PACKAGE;
            }
            Settings.System.putString(context.getContentResolver(), HEYTAP_UNLOCK_CHANGE_PKG, keyguardPkg);
            Settings.System.putString(context.getContentResolver(), HEYTAP_UNLOCK_CHANGE_CLASS, serviceClass);
        }
    }

    private static boolean checkCustomizeWallpaperDir() {
        if (new File(CUSTOM_WALLPAPER_DIR).exists()) {
            return true;
        }
        return new File(OPPO_CUSTOM_WALLPAPER_DIR).exists();
    }

    private static File getWallpaperDir(int userId) {
        return Environment.getUserSystemDirectory(userId);
    }

    public static boolean isCustomDisabledChangeWallpaper(Context context) {
        if (Settings.Secure.getInt(context.getContentResolver(), CHANGE_WALLPAPER_DISABLED_STATE, 0) == 1) {
            Log.d(TAG, "isCustomDisabledChangeWallpaper: diabled change wallaper");
            return true;
        }
        Log.d(TAG, "isCustomDisabledChangeWallpaper: enable change wallaper");
        return false;
    }

    public static void showForbidChangeWallPaperToast(Context context) {
        Log.d(TAG, "showForbidChangeWallPaperToast!");
        context.getMainThreadHandler().post(new Runnable(context) {
            /* class com.android.server.wallpaper.$$Lambda$OppoWallpaperManagerServiceHelper$yC0QdSG6wqpFaKZaY1LohPAA0f0 */
            private final /* synthetic */ Context f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                OppoWallpaperManagerServiceHelper.lambda$showForbidChangeWallPaperToast$0(this.f$0);
            }
        });
    }

    public static ComponentName getDefaultDyWallpaperComponentName(Context context) {
        Log.d(TAG, "getDefaultDyWallpaperComponentName: start ");
        if (context == null) {
            Log.e(TAG, "getDefaultDyWallpaperComponentName: context is null ");
            return null;
        }
        String fileName = getCotaFileName(context, 1);
        if (fileName != null) {
            String str = TAG;
            Log.d(str, "getDefaultDyWallpaperComponentName: cota wallpaper " + fileName + " exists, return");
            return null;
        }
        String fileName2 = getCustomFileName(context, 1);
        if (fileName2 != null) {
            String str2 = TAG;
            Log.d(str2, "getDefaultDyWallpaperComponentName: custom wallpaper " + fileName2 + " exists, return");
            return null;
        }
        String fileName3 = getOperatorFileName(context, 1);
        if (fileName3 != null) {
            String str3 = TAG;
            Log.d(str3, "getDefaultDyWallpaperComponentName: operator wallpaper " + fileName3 + " exists, return");
            return null;
        }
        String[] componentName = {sDefaultFileNameCache.get(TAG_DYNAMIC_WALLPAPER_PACKAGE_NAME), sDefaultFileNameCache.get(TAG_DYNAMIC_WALLPAPER_CLASS_NAME)};
        if (TextUtils.isEmpty(componentName[0]) || TextUtils.isEmpty(componentName[1])) {
            componentName = getDynamicColorFileName(context);
        }
        if (componentName != null && !TextUtils.isEmpty(componentName[0]) && !TextUtils.isEmpty(componentName[1])) {
            try {
                Intent intent = new Intent("android.service.wallpaper.WallpaperService");
                intent.setClassName(componentName[0], componentName[1]);
                return intent.getComponent();
            } catch (Exception e) {
                String str4 = TAG;
                Log.e(str4, "getDefaultDyWallpaperComponentName: Exception e =  " + e);
            }
        }
        return null;
    }

    private static String[] getDynamicColorFileName(Context context) {
        String hwPhoneColor = SystemProperties.get(PROP_HW_PHONE_COLOR);
        if (!TextUtils.isEmpty(hwPhoneColor)) {
            String[] wallpaper = findPhoneColorDefaultDynamicWallpaper(hwPhoneColor);
            if (wallpaper == null || TextUtils.isEmpty(wallpaper[0]) || TextUtils.isEmpty(wallpaper[1])) {
                Log.d(TAG, "DynamicWallpaper not exist");
                return null;
            }
            String packageName = wallpaper[0];
            String className = wallpaper[1];
            if (isComponentExist(context, packageName, className)) {
                setDefaultFileNameCache(TAG_DYNAMIC_WALLPAPER_PACKAGE_NAME, packageName);
                setDefaultFileNameCache(TAG_DYNAMIC_WALLPAPER_CLASS_NAME, className);
                return wallpaper;
            }
            Log.d(TAG, "Component name not exist");
            return null;
        }
        Log.d(TAG, "getColorFileName hwPhoneColor is empty");
        return null;
    }

    private static boolean isComponentExist(Context context, String packageName, String className) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent("android.service.wallpaper.WallpaperService");
        intent.setPackage("com.android.wallpaper.livepicker");
        List<ResolveInfo> list = packageManager.queryIntentServices(intent, 128);
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            ResolveInfo resolveInfo = list.get(i);
            ComponentInfo ci = resolveInfo.serviceInfo;
            try {
                WallpaperInfo info = new WallpaperInfo(context, resolveInfo);
                String infoPackageName = info.getPackageName();
                String infoClassName = info.getServiceName();
                if (packageName.equals(infoPackageName) && className.equals(infoClassName)) {
                    return true;
                }
            } catch (XmlPullParserException e) {
                String str = TAG;
                Log.w(str, "findLiveWallpapers Skipping wallpaper " + ci, e);
            } catch (IOException e2) {
                String str2 = TAG;
                Log.w(str2, "findLiveWallpapers Skipping wallpaper " + ci, e2);
            }
        }
        return false;
    }

    private static String[] findPhoneColorDefaultDynamicWallpaper(String phoneColorName) {
        String[] defaultTheme;
        String str;
        StringBuilder sb;
        if (TextUtils.isEmpty(phoneColorName)) {
            Log.d(TAG, "findPhoneColorDefaultDynamicWallpaper: The phoneColorName is empty!");
            return null;
        }
        defaultTheme = new String[2];
        InputStream inputStream = null;
        try {
            File phoneColorMapFile = new File(getDefaultWallpaperPath().getAbsolutePath() + BASE_OPPO_PRODUCT_DEFAULT_DIR + PHONE_COLOR_MAPS_DYNAMIC_FILE_NAME + PHONE_COLOR_MAPS_FILE_SUFFIX);
            if (!phoneColorMapFile.exists()) {
                Log.e(TAG, "findPhoneColorDefaultDynamicWallpaper: The phone color map file is not exists!");
                if (0 != 0) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        String str2 = TAG;
                        Log.e(str2, "findPhoneColorDefaultDynamicWallpaper: Closing inputStream. e = " + e);
                    }
                }
                return null;
            }
            String str3 = TAG;
            Log.i(str3, "findPhoneColorDefaultDynamicWallpaper: phoneColorMapFile = " + phoneColorMapFile);
            InputStream inputStream2 = new FileInputStream(phoneColorMapFile);
            XmlPullParser pullParser = Xml.newPullParser();
            pullParser.setInput(inputStream2, XML_ENCODING);
            String foundColorName = null;
            for (int event = pullParser.getEventType(); event != 1; event = pullParser.next()) {
                if (event != 0 && event == 2) {
                    String pullParserName = pullParser.getName();
                    if (TAG_PHONE_COLOR.equals(pullParserName)) {
                        String colorName = new String(pullParser.getAttributeValue(0));
                        if (phoneColorName.equals(colorName)) {
                            foundColorName = colorName;
                        } else {
                            foundColorName = null;
                            defaultTheme[0] = null;
                            defaultTheme[1] = null;
                        }
                    }
                    if (foundColorName != null) {
                        if (TAG_DYNAMIC_WALLPAPER_PACKAGE_NAME.equals(pullParserName)) {
                            defaultTheme[0] = pullParser.nextText();
                        } else if (TAG_DYNAMIC_WALLPAPER_CLASS_NAME.equals(pullParserName)) {
                            defaultTheme[1] = pullParser.nextText();
                        }
                    }
                }
                if (TextUtils.isEmpty(defaultTheme[0]) || TextUtils.isEmpty(defaultTheme[1])) {
                }
            }
            try {
                inputStream2.close();
            } catch (Exception e2) {
                e = e2;
                str = TAG;
                sb = new StringBuilder();
            }
            String str4 = TAG;
            Log.i(str4, "findPhoneColorDefaultDynamicWallpaper: defaultTheme = " + defaultTheme[1]);
            return defaultTheme;
        } catch (Exception e3) {
            String str5 = TAG;
            Log.e(str5, "findPhoneColorDefaultDynamicWallpaper: e = " + e3);
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (Exception e4) {
                    e = e4;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (Exception e5) {
                    String str6 = TAG;
                    Log.e(str6, "findPhoneColorDefaultDynamicWallpaper: Closing inputStream. e = " + e5);
                }
            }
            throw th;
        }
        sb.append("findPhoneColorDefaultDynamicWallpaper: Closing inputStream. e = ");
        sb.append(e);
        Log.e(str, sb.toString());
        String str42 = TAG;
        Log.i(str42, "findPhoneColorDefaultDynamicWallpaper: defaultTheme = " + defaultTheme[1]);
        return defaultTheme;
    }

    public static File getDefaultWallpaperPath() {
        File path = getOppoVersionDirectory();
        if (!new File(path.getAbsolutePath() + BASE_OPPO_WALLPAPER_DEFAULT_DIR).exists()) {
            return getOppoProductDirectory();
        }
        return path;
    }

    public static File getOppoProductDirectory() {
        try {
            Method method = Environment.class.getMethod("getOppoProductDirectory", new Class[0]);
            method.setAccessible(true);
            Object product = method.invoke(null, new Object[0]);
            if (product != null) {
                return (File) product;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new File(OPPO_PRODUCT_ROOT_PATH);
    }

    public static File getOppoVersionDirectory() {
        try {
            Method method = Environment.class.getMethod("getOppoVersionDirectory", new Class[0]);
            method.setAccessible(true);
            Object version = method.invoke(null, new Object[0]);
            if (version != null) {
                return (File) version;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new File(OPPO_VERSION_ROOT_PATH);
    }

    private static synchronized void setDefaultFileNameCache(String which, String fileName) {
        synchronized (OppoWallpaperManagerServiceHelper.class) {
            sDefaultFileNameCache.put(which, fileName);
        }
    }

    public static File getOppoCustomDirectory() {
        try {
            Method method = Environment.class.getMethod("getOppoCustomDirectory", new Class[0]);
            method.setAccessible(true);
            Object custom = method.invoke(null, new Object[0]);
            if (custom != null) {
                return (File) custom;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new File(OPPO_CUSTOM_ROOT_PATH);
    }

    public static File getCotaDirectory() {
        try {
            Method method = Environment.class.getMethod("getOppoCotaDirectory", new Class[0]);
            method.setAccessible(true);
            Object custom = method.invoke(null, new Object[0]);
            if (custom != null) {
                return (File) custom;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new File(OPPO_COTA_ROOT_PATH);
    }

    public static void copySystemToLockWallpaperLocked(int userId, SparseArray<WallpaperManagerService.WallpaperData> wallpaperMap, SparseArray<WallpaperManagerService.WallpaperData> lockWallpaperMap) {
        Slog.i(TAG, "Copy system wallpaper to lock wallpaper");
        WallpaperManagerService.WallpaperData sysWP = wallpaperMap.get(userId);
        if (sysWP == null) {
            Slog.i(TAG, "copySystemToLockWallpaperLocked, no system wallpaper");
            return;
        }
        WallpaperManagerService.WallpaperData lockWP = new WallpaperManagerService.WallpaperData(userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
        lockWP.wallpaperId = sysWP.wallpaperId;
        lockWP.cropHint.set(sysWP.cropHint);
        lockWP.allowBackup = sysWP.allowBackup;
        lockWP.primaryColors = sysWP.primaryColors;
        try {
            FileUtils.copyFile(sysWP.wallpaperFile, lockWP.wallpaperFile);
            FileUtils.copyFile(sysWP.cropFile, lockWP.cropFile);
            lockWallpaperMap.put(userId, lockWP);
        } catch (Exception e) {
            String str = TAG;
            Slog.e(str, "Can't copy system wallpaper: " + e.getMessage());
            lockWP.wallpaperFile.delete();
            lockWP.cropFile.delete();
        }
    }
}
