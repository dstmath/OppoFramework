package com.android.server.wm;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.util.Slog;
import android.util.SparseIntArray;
import com.android.server.oppo.IElsaManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
public class ColorNavigationBarUtil {
    private static final String ACTIVITY_COLOR = "activityColor";
    private static final String ACTIVITY_NAME = "activityName";
    private static final int ALPHA_BIT_NUM = 4;
    private static final int COLOR_BIT_NUM = 6;
    private static final int COLOR_OPAQUE = -16777216;
    private static final boolean DEBUG = false;
    private static final String DEFAULT_COLOR = "default_color";
    private static final boolean FORCE_STATUS_BAR_COLOR = false;
    private static final int HEX_NUM = 16;
    private static final String IS_NEED_PALETTE = "is_need_palette";
    private static final int MAX_COUNT = 20;
    private static final String NAVBAR_BACKGROUND = "nav_bg";
    private static final String NAV_BG_COLOR = "bg_color";
    private static final String PKG = "pkg";
    private static final String PKG_VERSION = "pkg_version";
    private static final String TAG = null;
    private static final List<AdaptationAppInfo> mDefaultAdapationApps = null;
    private static final String[] mDefaultAdaptationAppNames = null;
    private static final int[] mDefaultAppColors = null;
    private static final List<AdaptationActivityInfo> mDefaultNotAdapationActivities = null;
    private static final int[] mDefaultNotAdaptationActivityColors = null;
    private static final String[] mDefaultNotAdaptationActivityNames = null;
    private static final List<AdaptationAppInfo> mStatusDefaultAdapationApps = null;
    private Context mContext;
    private final Object mObject;
    private boolean mReadNavData;
    private boolean mReadStatusData;
    private int mUpdateNavCount;
    private int mUpdateStaCount;
    private boolean mUseDefualtData;

    class AdaptationActivityInfo {
        String mActivityName;
        int mDefaultColor;

        AdaptationActivityInfo() {
        }
    }

    class AdaptationAppInfo {
        Map<String, String> mActivityColorList;
        SparseIntArray mColorArray;
        int mDefaultColor;
        boolean mIsNeedPalette;
        int[] mKeys;
        String mPkg;

        AdaptationAppInfo() {
        }
    }

    public class NavBarContentObserver extends ContentObserver {
        public NavBarContentObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(ColorNavigationBarUtil.TAG, "the navigationbar table has changed");
            }
            ColorNavigationBarUtil.this.updateNavBgColorListFromDB();
        }
    }

    public class StatusBarContentObserver extends ContentObserver {
        public StatusBarContentObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(ColorNavigationBarUtil.TAG, "the StatusBar table has changed");
            }
            ColorNavigationBarUtil.this.updateStatusBgColorListFromDB();
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.wm.ColorNavigationBarUtil.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.wm.ColorNavigationBarUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.ColorNavigationBarUtil.<clinit>():void");
    }

    public ColorNavigationBarUtil(Context context) {
        this.mObject = new Object();
        this.mReadNavData = false;
        this.mReadStatusData = false;
        this.mUseDefualtData = true;
        this.mUpdateNavCount = 0;
        this.mUpdateStaCount = 0;
        this.mContext = context;
        init();
    }

    private void init() {
        updateAppNavBarDefaultList();
        registerContentObserver();
    }

    private void updateAppNavBarDefaultList() {
        synchronized (this.mObject) {
            int i;
            mDefaultAdapationApps.clear();
            mDefaultNotAdapationActivities.clear();
            int size = mDefaultAdaptationAppNames.length;
            for (i = 0; i < size; i++) {
                addAdaptationApp(mDefaultAdaptationAppNames[i], mDefaultAppColors[i]);
            }
            size = mDefaultNotAdaptationActivityNames.length;
            for (i = 0; i < size; i++) {
                addNotAdaptationActivity(mDefaultNotAdaptationActivityNames[i], mDefaultNotAdaptationActivityColors[i]);
            }
            this.mUseDefualtData = true;
        }
    }

    private void addAdaptationApp(String pkg, int color) {
        addAdaptationApp(pkg, color, false);
    }

    private void addAdaptationApp(String pkg, int color, boolean palette) {
        addAdaptationApp(pkg, color, palette, null);
    }

    private void addAdaptationApp(String pkg, int color, boolean palette, Map activityColorList) {
        AdaptationAppInfo appInfo = new AdaptationAppInfo();
        appInfo.mPkg = pkg;
        appInfo.mDefaultColor = color;
        appInfo.mIsNeedPalette = palette;
        appInfo.mActivityColorList = activityColorList;
        mDefaultAdapationApps.add(appInfo);
    }

    private void addStatusAdaptationApp(String pkg, int color, Map activityColorList) {
        AdaptationAppInfo appInfo = new AdaptationAppInfo();
        appInfo.mPkg = pkg;
        appInfo.mDefaultColor = color;
        appInfo.mActivityColorList = activityColorList;
        mStatusDefaultAdapationApps.add(appInfo);
    }

    private void addNotAdaptationActivity(String activityName, int color) {
        AdaptationActivityInfo activityInfo = new AdaptationActivityInfo();
        activityInfo.mActivityName = activityName;
        activityInfo.mDefaultColor = color;
        mDefaultNotAdapationActivities.add(activityInfo);
    }

    private void updateNavBgColorListFromDB() {
        new Thread() {
            /* JADX WARNING: Removed duplicated region for block: B:86:? A:{SYNTHETIC, RETURN} */
            /* JADX WARNING: Removed duplicated region for block: B:70:0x0229  */
            /* JADX WARNING: Removed duplicated region for block: B:73:0x0231  */
            /* JADX WARNING: Missing block: B:56:?, code:
            com.android.server.wm.ColorNavigationBarUtil.-set2(r22.this$0, false);
            com.android.server.wm.ColorNavigationBarUtil.-set0(r22.this$0, true);
     */
            /* JADX WARNING: Missing block: B:57:0x01c2, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR == false) goto L_0x01e7;
     */
            /* JADX WARNING: Missing block: B:58:0x01c4, code:
            android.util.Slog.d(com.android.server.wm.ColorNavigationBarUtil.-get0(), "query navigation bar data success! size:" + com.android.server.wm.ColorNavigationBarUtil.-get2().size());
     */
            /* JADX WARNING: Missing block: B:61:0x01e8, code:
            r18 = r19;
     */
            /* JADX WARNING: Missing block: B:73:0x0231, code:
            r13.close();
     */
            /* JADX WARNING: Missing block: B:75:0x0235, code:
            r3 = th;
     */
            /* JADX WARNING: Missing block: B:76:0x0236, code:
            r18 = r19;
     */
            /* JADX WARNING: Missing block: B:77:0x0239, code:
            r16 = e;
     */
            /* JADX WARNING: Missing block: B:78:0x023a, code:
            r18 = r19;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                Cursor cursor = null;
                Map<String, String> map = null;
                Uri uri = Uri.parse("content://com.oppo.systemui/navigationbar");
                Throwable th;
                try {
                    if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Log.d(ColorNavigationBarUtil.TAG, "updateNavBgColorListFromDB");
                    }
                    cursor = ColorNavigationBarUtil.this.mContext.getContentResolver().query(uri, null, null, null, null);
                    if (cursor == null || cursor.getCount() == 0) {
                        Log.w(ColorNavigationBarUtil.TAG, "cursor is null or count is 0.");
                    } else {
                        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                            Slog.d(ColorNavigationBarUtil.TAG, "query cursor:" + cursor.getCount());
                        }
                        synchronized (ColorNavigationBarUtil.this.mObject) {
                            try {
                                ColorNavigationBarUtil.mDefaultAdapationApps.clear();
                                while (true) {
                                    Map<String, String> map2;
                                    try {
                                        map2 = map;
                                        if (!cursor.moveToNext()) {
                                            break;
                                        }
                                        String pkg = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.PKG));
                                        String defColor = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.DEFAULT_COLOR));
                                        if (defColor == null || defColor.equals(IElsaManager.EMPTY_PACKAGE)) {
                                            defColor = "0";
                                        }
                                        int defaultColor = ColorNavigationBarUtil.this.stringColorToIntColor(defColor);
                                        boolean palette = 1 == cursor.getInt(cursor.getColumnIndex(ColorNavigationBarUtil.IS_NEED_PALETTE));
                                        String activity = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ACTIVITY_NAME));
                                        String activityColor = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ACTIVITY_COLOR));
                                        if (activity.equals(IElsaManager.EMPTY_PACKAGE) || activityColor.equals(IElsaManager.EMPTY_PACKAGE)) {
                                            map = map2;
                                        } else {
                                            map = new HashMap();
                                            String[] actList = activity.split(",");
                                            String[] actcolorList = activityColor.split(",");
                                            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                                                Slog.d(ColorNavigationBarUtil.TAG, "actList:" + actList + " " + actList.length + " actcolorList:" + actcolorList);
                                            }
                                            int i = 0;
                                            while (actList.length > i && actcolorList.length > i) {
                                                map.put(actList[i], actcolorList[i]);
                                                i++;
                                            }
                                        }
                                        ColorNavigationBarUtil.this.addAdaptationApp(pkg, defaultColor, palette, map);
                                        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                                            Slog.d(ColorNavigationBarUtil.TAG, "nav pkg:" + pkg + " defaultColor:" + defaultColor + " Hex:" + Integer.toHexString(defaultColor) + " palette:" + palette + " map:" + map);
                                        }
                                    } catch (Throwable th2) {
                                        th = th2;
                                        map = map2;
                                    }
                                }
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                        return;
                    }
                    return;
                    throw th;
                } catch (Exception e) {
                    Exception e2 = e;
                    try {
                        ColorNavigationBarUtil.this.updateAppNavBarDefaultList();
                        Log.w(ColorNavigationBarUtil.TAG, "query error! list size " + ColorNavigationBarUtil.mDefaultAdapationApps.size() + " e:" + e2);
                        if (cursor == null) {
                            cursor.close();
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                }
            }
        }.start();
    }

    private void updateStatusBgColorListFromDB() {
        new Thread() {
            /* JADX WARNING: Removed duplicated region for block: B:67:0x01d6  */
            /* JADX WARNING: Removed duplicated region for block: B:81:? A:{SYNTHETIC, RETURN} */
            /* JADX WARNING: Removed duplicated region for block: B:64:0x01ce  */
            /* JADX WARNING: Missing block: B:49:?, code:
            com.android.server.wm.ColorNavigationBarUtil.-set1(r20.this$0, true);
     */
            /* JADX WARNING: Missing block: B:53:0x017d, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR == false) goto L_0x01e6;
     */
            /* JADX WARNING: Missing block: B:54:0x017f, code:
            android.util.Slog.d(com.android.server.wm.ColorNavigationBarUtil.-get0(), "query status bar data success! size:" + com.android.server.wm.ColorNavigationBarUtil.-get4().size());
     */
            /* JADX WARNING: Missing block: B:55:0x01a2, code:
            r17 = r18;
     */
            /* JADX WARNING: Missing block: B:64:0x01ce, code:
            r12.close();
     */
            /* JADX WARNING: Missing block: B:69:0x01da, code:
            r2 = th;
     */
            /* JADX WARNING: Missing block: B:70:0x01db, code:
            r17 = r18;
     */
            /* JADX WARNING: Missing block: B:71:0x01de, code:
            r15 = e;
     */
            /* JADX WARNING: Missing block: B:72:0x01df, code:
            r17 = r18;
     */
            /* JADX WARNING: Missing block: B:75:0x01e6, code:
            r17 = r18;
     */
            /* JADX WARNING: Missing block: B:81:?, code:
            return;
     */
            /* JADX WARNING: Missing block: B:82:?, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                Cursor cursor = null;
                Map<String, String> map = null;
                Throwable th;
                try {
                    cursor = ColorNavigationBarUtil.this.mContext.getContentResolver().query(Uri.parse("content://com.oppo.systemui/statusbar"), null, null, null, null);
                    if (cursor == null || cursor.getCount() == 0) {
                        Log.w(ColorNavigationBarUtil.TAG, "updateStatusBgColorListFromDB cursor is null or count is 0.");
                    } else {
                        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                            Slog.d(ColorNavigationBarUtil.TAG, "query cursor:" + cursor.getCount());
                        }
                        synchronized (ColorNavigationBarUtil.this.mObject) {
                            try {
                                ColorNavigationBarUtil.mStatusDefaultAdapationApps.clear();
                                while (true) {
                                    Map<String, String> map2;
                                    try {
                                        map2 = map;
                                        if (!cursor.moveToNext()) {
                                            break;
                                        }
                                        String pkg = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.PKG));
                                        String defColor = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.DEFAULT_COLOR));
                                        if (defColor == null || defColor.equals(IElsaManager.EMPTY_PACKAGE)) {
                                            defColor = "0";
                                        }
                                        int defaultColor = ColorNavigationBarUtil.this.stringColorToIntColor(defColor);
                                        String activity = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ACTIVITY_NAME));
                                        String activityColor = cursor.getString(cursor.getColumnIndex(ColorNavigationBarUtil.ACTIVITY_COLOR));
                                        if (activity.equals(IElsaManager.EMPTY_PACKAGE) || activityColor.equals(IElsaManager.EMPTY_PACKAGE)) {
                                            map = map2;
                                        } else {
                                            map = new HashMap();
                                            String[] actList = activity.split(",");
                                            String[] actcolorList = activityColor.split(",");
                                            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                                                Slog.d(ColorNavigationBarUtil.TAG, "actList:" + actList + " " + actList.length + " actcolorList:" + actcolorList);
                                            }
                                            int i = 0;
                                            while (actList.length > i && actcolorList.length > i) {
                                                map.put(actList[i], actcolorList[i]);
                                                i++;
                                            }
                                        }
                                        ColorNavigationBarUtil.this.addStatusAdaptationApp(pkg, defaultColor, map);
                                        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                                            Slog.d(ColorNavigationBarUtil.TAG, "status pkg:" + pkg + " defaultColor:" + defaultColor + " Hex:" + Integer.toHexString(defaultColor));
                                        }
                                    } catch (Throwable th2) {
                                        th = th2;
                                        map = map2;
                                    }
                                }
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                        return;
                    }
                    return;
                    throw th;
                } catch (Exception e) {
                    Exception e2 = e;
                    try {
                        ColorNavigationBarUtil.mStatusDefaultAdapationApps.clear();
                        Log.w(ColorNavigationBarUtil.TAG, "updateStatusBgColorListFromDB query error:" + e2);
                        if (cursor == null) {
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            }
        }.start();
    }

    private int stringColorToIntColor(String color) {
        int length = color.length();
        if (length < 6) {
            Slog.e(TAG, "Color String Error! colorString:" + color);
            return 0;
        }
        String alpha = color.substring(0, length - 6);
        String colorString = color.substring(length - 6, length);
        if (alpha.equals(IElsaManager.EMPTY_PACKAGE)) {
            alpha = "ff";
        }
        if (colorString.equals(IElsaManager.EMPTY_PACKAGE)) {
            return 0;
        }
        return (Integer.valueOf(alpha, 16).intValue() << 24) | Integer.valueOf(colorString, 16).intValue();
    }

    private void registerContentObserver() {
        this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.oppo.systemui/navigationbar"), true, new NavBarContentObserver());
        this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.oppo.systemui/statusbar"), true, new StatusBarContentObserver());
    }

    /* JADX WARNING: Missing block: B:24:0x00c3, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isActivityNeedPalette(String pkg, String activityName) {
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Log.d(TAG, "isActivityNeedPalette mReadNavData:" + this.mReadNavData + " activity:" + pkg + "/" + activityName);
        }
        if (!this.mReadNavData && this.mUpdateNavCount < 20) {
            updateNavBgColorListFromDB();
            this.mUpdateNavCount++;
            Slog.d(TAG, "isActivityNeedPalette mUpdateNavCount:" + this.mUpdateNavCount);
        }
        synchronized (this.mObject) {
            int size = mDefaultAdapationApps.size();
            for (int i = 0; i < size; i++) {
                AdaptationAppInfo appInfo = (AdaptationAppInfo) mDefaultAdapationApps.get(i);
                if (appInfo.mActivityColorList != null) {
                    for (Entry<String, String> entry : appInfo.mActivityColorList.entrySet()) {
                        if (((String) entry.getKey()).equals(activityName)) {
                            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                                Slog.d(TAG, "The activity:" + activityName + " isn't need palette!");
                            }
                        }
                    }
                }
                if (appInfo.mPkg.equals(pkg)) {
                    if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Slog.d(TAG, "The app " + pkg + " isNeedPalette:" + appInfo.mIsNeedPalette);
                    }
                    boolean z = appInfo.mIsNeedPalette;
                    return z;
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:38:0x012b, code:
            return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getNavBarColorFromAdaptation(String pkg, String activityName) {
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Log.d(TAG, "getNavBarColorFromAdaptation mReadNavData:" + this.mReadNavData + " activity:" + pkg + "/" + activityName);
        }
        if (!this.mReadNavData && this.mUpdateNavCount < 20) {
            updateNavBgColorListFromDB();
            this.mUpdateNavCount++;
            Slog.d(TAG, "getNavBarColorFromAdaptation mUpdateNavCount:" + this.mUpdateNavCount);
        }
        synchronized (this.mObject) {
            int size;
            int i;
            int i2;
            if (this.mUseDefualtData) {
                size = mDefaultNotAdapationActivities.size();
                for (i = 0; i < size; i++) {
                    AdaptationActivityInfo activityInfo = (AdaptationActivityInfo) mDefaultNotAdapationActivities.get(i);
                    if (activityInfo.mActivityName.equals(activityName)) {
                        Slog.d(TAG, "the defualt activity:" + activityName + " color: " + Integer.toHexString(activityInfo.mDefaultColor));
                        i2 = activityInfo.mDefaultColor;
                        return i2;
                    }
                }
            }
            size = mDefaultAdapationApps.size();
            for (i = 0; i < size; i++) {
                AdaptationAppInfo appInfo = (AdaptationAppInfo) mDefaultAdapationApps.get(i);
                if (appInfo.mActivityColorList != null) {
                    for (Entry<String, String> entry : appInfo.mActivityColorList.entrySet()) {
                        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                            Slog.d(TAG, "activity:" + ((String) entry.getKey()) + " color: " + ((String) entry.getValue()));
                        }
                        if (entry.getValue() == null || ((String) entry.getValue()).equals(IElsaManager.EMPTY_PACKAGE)) {
                        } else if (((String) entry.getKey()).equals(activityName)) {
                            i2 = Integer.valueOf((String) entry.getValue(), 16).intValue() | -16777216;
                            return i2;
                        }
                    }
                }
                if (appInfo.mPkg.equals(pkg)) {
                    if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Slog.d(TAG, "Nav pkg: " + pkg + " activity:" + activityName + " Hex:" + Integer.toHexString(appInfo.mDefaultColor));
                    }
                    i2 = appInfo.mDefaultColor;
                    return i2;
                }
            }
            return 0;
        }
    }

    public int getStatusBarColorFromAdaptation(String pkg, String activityName) {
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Log.d(TAG, "getStatusBarColorFromAdaptation mReadStatusData:" + this.mReadStatusData + " activity:" + pkg + "/" + activityName);
        }
        return 0;
    }
}
