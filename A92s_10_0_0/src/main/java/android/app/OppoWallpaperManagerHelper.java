package android.app;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Environment;
import android.os.SystemProperties;
import android.telecom.Logging.Session;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.WindowManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;

public class OppoWallpaperManagerHelper {
    private static String BASE_OPPO_PRODUCT_DEFAULT_DIR = (getDefaultWallpaperPath().getAbsolutePath() + "/decouping_wallpaper/default/");
    private static final String BASE_OPPO_WALLPAPER_DEFAULT_DIR = "/decouping_wallpaper/";
    private static final String CUSTOM_LOCK_WALLPAPER_NAME = "default_wallpaper_lock";
    private static final String CUSTOM_WALLPAPER = "/custom";
    private static final String CUSTOM_WALLPAPER_NAME = "default_wallpaper";
    private static final String DEFAULT_LOCK_WALLPAPER_NAME = "oppo_default_wallpaper_lock";
    private static final String DEFAULT_PATH_THEME = "default";
    private static final String DEFAULT_WALLPAPER_NAME = "oppo_default_wallpaper";
    private static final String OPERATOR = "operator";
    private static final String OPPO_COTA_ROOT_PATH = "/my_cota";
    private static final String OPPO_COTA_WALLPAPER = getCotaDirectory().getAbsolutePath();
    private static final String OPPO_CUSTOM_ROOT_PATH = "/oppo_custom";
    private static final String OPPO_CUSTOM_WALLPAPER = getOppoCustomDirectory().getAbsolutePath();
    private static final String OPPO_PRODUCT_ROOT_PATH = "/oppo_product";
    private static final String OPPO_VERSION_ROOT_PATH = "/oppo_version";
    private static final String PHONE_COLOR_MAPS_FILE_NAME = "phone_color_default_theme_maps";
    private static final String PHONE_COLOR_MAPS_FILE_SUFFIX = ".xml";
    private static final String PRJ_VERSION_FILE_NAME = "/proc/oppoVersion/prjVersion";
    private static final String PROP_HW_PHONE_COLOR = "ro.hw.phone.color";
    private static String TAG = "OppoWallpaperManagerHelper";
    private static final String TAG_DEFAULT_THEME_LOCK = "DefaultTheme_lock";
    private static final String TAG_DEFAULT_THEME_SYSTEM = "DefaultTheme_system";
    private static final String TAG_PHONE_COLOR = "PhoneColor";
    private static final String WALLPAPER_CUSTOM_FILE_DIR = "/media/wallpaper/default";
    private static final String WALLPAPER_SUFFIX = ".png";
    private static final String XML_ENCODING = "UTF-8";
    private static Map<Integer, String> sDefaultColorFileNameCache = new ArrayMap();

    public static int getDefaultWallpaperResID(Context context) {
        String wallpaperName;
        int wallpaperID = -1;
        String sysOperatorName = SystemProperties.get("ro.oppo.operator", "null");
        if (!"null".equals(sysOperatorName)) {
            Resources resources = context.getResources();
            wallpaperID = resources.getIdentifier("oppo:drawable/" + ("oppo_default_wallpaper_" + sysOperatorName.toLowerCase()), null, null);
        }
        if (wallpaperID <= 0) {
            if ("CN".equalsIgnoreCase(SystemProperties.get("persist.sys.oppo.region", "CN"))) {
                wallpaperName = DEFAULT_WALLPAPER_NAME;
            } else {
                wallpaperName = "oppo_default_wallpaper_exp";
            }
            wallpaperID = context.getResources().getIdentifier("oppo:drawable/" + wallpaperName, null, null);
        }
        if (wallpaperID <= 0) {
            return 201850942;
        }
        return wallpaperID;
    }

    public static Bitmap generateBitmap(Context context, Bitmap bm) {
        int desiredWidth;
        if (bm == null) {
            Log.d(TAG, "generateBitmap return bm = null");
            return null;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(displayMetrics);
        bm.setDensity(displayMetrics.noncompatDensityDpi);
        int maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
        int minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        int bmWidth = bm.getWidth();
        int bmHeight = bm.getHeight();
        float ratio = 1.0f;
        if (bmHeight < maxDim) {
            ratio = (float) (maxDim / bmHeight);
        }
        if (((float) bmWidth) * ratio <= ((float) minDim)) {
            desiredWidth = minDim;
        } else {
            desiredWidth = minDim * 2;
        }
        String str = TAG;
        Log.d(str, "generateBitmap desiredWidth = " + desiredWidth + " desiredHeight = " + maxDim);
        if (desiredWidth > 0 && maxDim > 0) {
            if (bm.getWidth() != desiredWidth || bm.getHeight() != maxDim) {
                try {
                    Bitmap newbm = Bitmap.createBitmap(desiredWidth, maxDim, Bitmap.Config.ARGB_8888);
                    newbm.setDensity(displayMetrics.noncompatDensityDpi);
                    Canvas c = new Canvas(newbm);
                    Rect targetRect = new Rect();
                    targetRect.right = bm.getWidth();
                    targetRect.bottom = bm.getHeight();
                    int deltaw = desiredWidth - targetRect.right;
                    int deltah = maxDim - targetRect.bottom;
                    if (deltaw > 0 || deltah > 0) {
                        try {
                            float scalew = ((float) desiredWidth) / ((float) targetRect.right);
                            float scaleh = ((float) maxDim) / ((float) targetRect.bottom);
                            float scale = scalew > scaleh ? scalew : scaleh;
                            targetRect.right = (int) (((float) targetRect.right) * scale);
                            targetRect.bottom = (int) (((float) targetRect.bottom) * scale);
                            deltaw = desiredWidth - targetRect.right;
                            deltah = maxDim - targetRect.bottom;
                        } catch (Exception e) {
                            e = e;
                            Log.w(TAG, "Can't generate default bitmap", e);
                            return bm;
                        }
                    }
                    targetRect.offset(deltaw / 2, deltah / 2);
                    Paint paint = new Paint();
                    paint.setFilterBitmap(true);
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                    c.drawBitmap(bm, (Rect) null, targetRect, paint);
                    bm.recycle();
                    c.setBitmap(null);
                    return newbm;
                } catch (Exception e2) {
                    e = e2;
                    Log.w(TAG, "Can't generate default bitmap", e);
                    return bm;
                }
            }
        }
        return bm;
    }

    public static InputStream openDefaultWallpaper(Context context, int which) {
        InputStream stream;
        long startTime = System.currentTimeMillis();
        String fileName = getDefaultWallpaperFileName(context, which);
        if (fileName != null) {
            try {
                stream = new FileInputStream(fileName);
            } catch (Exception e) {
                stream = openDefaultWallpaperFromApkRes(context, which);
                String str = TAG;
                Log.d(str, "openDefaultWallpaper failed to open Stream fileName = " + fileName);
            }
        } else {
            stream = openDefaultWallpaperFromApkRes(context, which);
            Log.d(TAG, "openDefaultWallpaper no file inner ");
        }
        String str2 = TAG;
        Log.d(str2, "openDefaultWallpaper costTime = " + (System.currentTimeMillis() - startTime) + " fileName = " + fileName + " which = " + which);
        return stream;
    }

    private static synchronized void setDefaultColorFileNameCache(int which, String fileName) {
        synchronized (OppoWallpaperManagerHelper.class) {
            sDefaultColorFileNameCache.put(Integer.valueOf(which), fileName);
        }
    }

    private static String getDefaultWallpaperFileName(Context context, int which) {
        String fileName = getCotaFileName(context, which);
        if (fileName == null) {
            fileName = getCustomFileName(context, which);
        }
        if (fileName == null) {
            fileName = getOperatorFileName(context, which);
        }
        String cacheFileName = sDefaultColorFileNameCache.get(Integer.valueOf(which));
        String str = TAG;
        Log.d(str, "getDefaultWallpaperFileName cacheFileName = " + cacheFileName + " which = " + which);
        if (!TextUtils.isEmpty(cacheFileName)) {
            return cacheFileName;
        }
        if (fileName == null) {
            fileName = getColorFileName(context, which);
        }
        if (fileName == null) {
            fileName = getNoColorFileName(context, which);
        }
        String str2 = TAG;
        Log.d(str2, "getDefaultWallpaperFileName final fileName = " + fileName);
        return fileName;
    }

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

    private static String getNoColorFileName(Context context, int which) {
        String fileName;
        if (which == 2) {
            fileName = BASE_OPPO_PRODUCT_DEFAULT_DIR + DEFAULT_LOCK_WALLPAPER_NAME + WALLPAPER_SUFFIX;
        } else {
            fileName = BASE_OPPO_PRODUCT_DEFAULT_DIR + DEFAULT_WALLPAPER_NAME + WALLPAPER_SUFFIX;
        }
        File file = new File(fileName);
        Log.d(TAG, "getNoColorFileName default fileName = " + fileName);
        if (file.exists()) {
            return fileName;
        }
        Log.d(TAG, "getNoColorFileName default fileName not exist");
        return null;
    }

    private static String getColorFileName(Context context, int which) {
        String fileName = null;
        String hwPhoneColor = SystemProperties.get(PROP_HW_PHONE_COLOR);
        if (!TextUtils.isEmpty(hwPhoneColor)) {
            String[] wallpaper = findPhoneColorDefaultWallpaper(hwPhoneColor);
            if (wallpaper == null || TextUtils.isEmpty(wallpaper[0]) || TextUtils.isEmpty(wallpaper[1])) {
                Log.d(TAG, "getColorFileName phoneColorDefaultTheme is empty");
                return null;
            }
            String lockFileName = BASE_OPPO_PRODUCT_DEFAULT_DIR + wallpaper[1] + WALLPAPER_SUFFIX;
            String systemFileName = BASE_OPPO_PRODUCT_DEFAULT_DIR + wallpaper[0] + WALLPAPER_SUFFIX;
            File lockFile = new File(lockFileName);
            File systemFile = new File(systemFileName);
            if (!lockFile.exists()) {
                Log.d(TAG, "getColorFileName lock not exist  lock  = " + lockFileName);
            } else if (which == 2) {
                setDefaultColorFileNameCache(2, lockFileName);
                fileName = lockFileName;
            }
            if (!systemFile.exists()) {
                Log.d(TAG, "getColorFileName system not exist system =  " + systemFileName);
                return fileName;
            } else if (which != 1) {
                return fileName;
            } else {
                setDefaultColorFileNameCache(1, systemFileName);
                return systemFileName;
            }
        } else {
            Log.d(TAG, "getColorFileName hwPhoneColor is empty");
            return null;
        }
    }

    private static String getOperatorFileName(Context context, int which) {
        String fileName;
        String sysOperatorName = SystemProperties.get("ro.oppo.operator");
        if (!TextUtils.isEmpty(sysOperatorName)) {
            String sysOperatorName2 = sysOperatorName.trim().toLowerCase();
            if (which == 2) {
                fileName = BASE_OPPO_PRODUCT_DEFAULT_DIR + DEFAULT_LOCK_WALLPAPER_NAME + Session.SESSION_SEPARATION_CHAR_CHILD + "operator" + Session.SESSION_SEPARATION_CHAR_CHILD + sysOperatorName2 + WALLPAPER_SUFFIX;
            } else {
                fileName = BASE_OPPO_PRODUCT_DEFAULT_DIR + DEFAULT_WALLPAPER_NAME + Session.SESSION_SEPARATION_CHAR_CHILD + "operator" + Session.SESSION_SEPARATION_CHAR_CHILD + sysOperatorName2 + WALLPAPER_SUFFIX;
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

    public static InputStream openDefaultWallpaperFromApkRes(Context context, int which) {
        if (which == 2) {
            return null;
        }
        return context.getResources().openRawResource(getDefaultWallpaperResID(context));
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x00c3  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00c8 A[RETURN] */
    private static String getPrjVersion() {
        StringBuilder sb;
        String str;
        String prjVersion = "";
        File prjVersionFile = new File(PRJ_VERSION_FILE_NAME);
        if (prjVersionFile.exists()) {
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(prjVersionFile));
                String prjVersion2 = bufferedReader.readLine();
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    String str2 = TAG;
                    Log.e(str2, "getPrjVersion: Close e = " + e);
                }
                prjVersion = prjVersion2;
            } catch (FileNotFoundException e2) {
                String str3 = TAG;
                Log.e(str3, "getPrjVersion: e = " + e2);
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e3) {
                        e = e3;
                        str = TAG;
                        sb = new StringBuilder();
                    }
                }
            } catch (IOException e4) {
                String str4 = TAG;
                Log.e(str4, "getPrjVersion: e = " + e4);
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e5) {
                        e = e5;
                        str = TAG;
                        sb = new StringBuilder();
                    }
                }
            } catch (Throwable th) {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e6) {
                        String str5 = TAG;
                        Log.e(str5, "getPrjVersion: Close e = " + e6);
                    }
                }
                throw th;
            }
        } else {
            Log.w(TAG, "getPrjVersion: The prjVersionFile is not exist!");
        }
        if (TextUtils.isEmpty(prjVersion)) {
            return prjVersion.trim();
        }
        return prjVersion;
        sb.append("getPrjVersion: Close e = ");
        sb.append(e);
        Log.e(str, sb.toString());
        if (TextUtils.isEmpty(prjVersion)) {
        }
    }

    private static String[] findPhoneColorDefaultWallpaper(String phoneColorName) {
        String str;
        StringBuilder sb;
        if (TextUtils.isEmpty(phoneColorName)) {
            Log.d(TAG, "findPhoneColorDefaultWallpaper: The phoneColorName is empty!");
            return null;
        }
        String[] defaultTheme = new String[2];
        InputStream inputStream = null;
        File phoneColorMapFile = null;
        try {
            String prjVersion = getPrjVersion();
            if (!TextUtils.isEmpty(prjVersion)) {
                phoneColorMapFile = new File(BASE_OPPO_PRODUCT_DEFAULT_DIR + PHONE_COLOR_MAPS_FILE_NAME + Session.SESSION_SEPARATION_CHAR_CHILD + prjVersion + PHONE_COLOR_MAPS_FILE_SUFFIX);
                if (!phoneColorMapFile.exists()) {
                    phoneColorMapFile = null;
                }
            }
            if (phoneColorMapFile == null) {
                phoneColorMapFile = new File(BASE_OPPO_PRODUCT_DEFAULT_DIR + PHONE_COLOR_MAPS_FILE_NAME + PHONE_COLOR_MAPS_FILE_SUFFIX);
                if (!phoneColorMapFile.exists()) {
                    Log.e(TAG, "findPhoneColorDefaultWallpaper: The phone color map file is not exists!");
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e) {
                            String str2 = TAG;
                            Log.e(str2, "findPhoneColorDefaultWallpaper: Closing inputStream. e = " + e);
                        }
                    }
                    return null;
                }
            }
            String str3 = TAG;
            Log.i(str3, "findPhoneColorDefaultWallpaper: prjVersion = " + prjVersion + " , phoneColorMapFile = " + phoneColorMapFile);
            InputStream inputStream2 = new FileInputStream(phoneColorMapFile);
            XmlPullParser pullParser = Xml.newPullParser();
            pullParser.setInput(inputStream2, XML_ENCODING);
            String foundColorName = null;
            int event = pullParser.getEventType();
            while (event != 1) {
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
                        if (TAG_DEFAULT_THEME_SYSTEM.equals(pullParserName)) {
                            defaultTheme[0] = pullParser.nextText();
                        } else if (TAG_DEFAULT_THEME_LOCK.equals(pullParserName)) {
                            defaultTheme[1] = pullParser.nextText();
                        }
                    }
                }
                if (TextUtils.isEmpty(defaultTheme[0]) || TextUtils.isEmpty(defaultTheme[1])) {
                    event = pullParser.next();
                }
            }
            try {
                inputStream2.close();
            } catch (Exception e2) {
                e = e2;
                str = TAG;
                sb = new StringBuilder();
            }
        } catch (Exception e3) {
            String str4 = TAG;
            Log.e(str4, "findPhoneColorDefaultWallpaper: e = " + e3);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e4) {
                    e = e4;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e5) {
                    String str5 = TAG;
                    Log.e(str5, "findPhoneColorDefaultWallpaper: Closing inputStream. e = " + e5);
                }
            }
            throw th;
        }
        String str6 = TAG;
        Log.i(str6, "findPhoneColorDefaultWallpaper: defaultTheme = " + defaultTheme[0] + " defaultTheme[]");
        return defaultTheme;
        sb.append("findPhoneColorDefaultWallpaper: Closing inputStream. e = ");
        sb.append(e);
        Log.e(str, sb.toString());
        String str62 = TAG;
        Log.i(str62, "findPhoneColorDefaultWallpaper: defaultTheme = " + defaultTheme[0] + " defaultTheme[]");
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
}
