package com.color.darkmode;

import android.app.Application;
import android.app.ColorUxIconConstants;
import android.app.OppoActivityManager;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.NinePatch;
import android.graphics.OppoBaseBaseCanvas;
import android.graphics.OppoBaseBitmap;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.OppoBaseView;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.android.internal.R;
import com.color.settings.ColorSettings;
import com.color.settings.ColorSettingsChangeListener;
import com.color.util.ColorTypeCastingHelper;
import com.google.android.collect.Sets;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class ColorDarkModeManager extends ColorDummyDarkModeManager {
    public static final String COLOROS_DISABLE_SYSTEM_DARKMODE_META_DATA = "com.coloros.DisableSystemDarkMode";
    private static final String DARKMODE_DIR = "darkmode";
    private static final String DARKMODE_DIR_PATH = "/data/oppo/coloros/darkmode";
    private static final boolean DBG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final boolean DEBUG = (false | DBG);
    private static final String DISPLAY_MCA_FILE = "/sys/kernel/oppo_display/mca_state";
    private static final String FILENAME_BLACK_LIST = "sys_darkmode_managed_list.xml";
    private static final String FILENAME_CLICK_APP = "click_app";
    private static final String FILENAME_OPEN_APP = "open_app";
    private static final int HIDDEN_TYPE_ADAPTED_SYSTEM_DARK_MODE = 1;
    private static final int HIDDEN_TYPE_CONFIGURABLE_ADAPT_SYSTEM_DARK_MODE = 2;
    private static final Set<String> LOCAL_SOFT_PACKAGE = Sets.newHashSet(new String[]{"com.facebook.katana", "com.facebook.orca", "com.whatsapp", "com.tencent.mm", "com.tencent.mobileqq"});
    private static final Object LOCK = new Object();
    private static final int MAX_TIME = 5;
    public static final int MIX_MASK = 4;
    public static final int OPEN_MASK = 1;
    private static final String SETTING_BLACK_LIST = (DARKMODE_DIR + File.separator + FILENAME_BLACK_LIST);
    private static final String SETTING_CLICK_PATH = (DARKMODE_DIR + File.separator + FILENAME_CLICK_APP);
    private static final String SETTING_OPEN_PATH = (DARKMODE_DIR + File.separator + FILENAME_OPEN_APP);
    public static final int SOFT_MASK = 2;
    private static final String TAG = "ColorDarkModeManager";
    private static final Object TYPE_LOCK = new Object();
    public static final int TYPE_MIX = 2;
    public static final int TYPE_ORIGIN = 0;
    public static final int TYPE_SOFT = 1;
    private static final Set<String> WHITE_LIST_PACKAGE = Sets.newHashSet(new String[]{"com.android.systemui", "com.android.settings", "com.android.browser", "com.android.phone", "com.android.wallpaper.livepicker", "com.android.calculator2", "com.android.calendar", ColorUxIconConstants.IconLoader.COM_ANDROID_CONTACTS, "com.android.mms", "com.android.stk", "com.android.packageinstaller", "com.android.permissioncontroller", "com.color.eyeprotect", "system_process", "com.finshell.wallet", "android", "com.redteamobile.roaming"});
    private static int sDisableSystemDarkModeByApp = -1;
    private Set<String> mAdaptedHiddenAppLists = new HashSet();
    private Map<String, Integer> mAppTypeList = new ArrayMap();
    private ColorSettingsChangeListener mConfigChangeListener = new ColorSettingsChangeListener(null) {
        /* class com.color.darkmode.ColorDarkModeManager.AnonymousClass2 */

        public void onSettingsChange(boolean selfChange, String customPath, int userId) {
            ColorDarkModeManager.log("onSettingsChange path=" + customPath + " selfChange=" + selfChange + " userId:" + userId);
            ColorDarkModeManager.this.updateListInThreadPool(userId);
        }
    };
    private ColorDarkModeWebManager mDarkModeWebManager = new ColorDarkModeWebManager(this);
    private View mDecor;
    private boolean mIsChangeSystemUiVisibility = false;
    private boolean mIsConfigurationChanged = false;
    private boolean mIsUseColorForceDark;
    private boolean mIsUseColorSoftDraw;
    private Set<String> mOpenAppList = new HashSet();
    private OppoActivityManager mOppoActivityManager;
    private ColorSettingsChangeListener mRUSChangeListener = new ColorSettingsChangeListener(null) {
        /* class com.color.darkmode.ColorDarkModeManager.AnonymousClass3 */

        public void onSettingsChange(boolean selfChange, String customPath, int userId) {
            ColorDarkModeManager.log("onSettingsChange by RUS path=" + customPath + " selfChange=" + selfChange + " userId:" + userId);
            ColorDarkModeManager.this.updateListInThreadPool(userId);
        }
    };
    private Context mServerContext;
    private ExecutorService mThreadPool;
    private boolean mUseOwnerForceDark = false;
    private int mUserId = 0;

    public boolean isConfigurationChanged() {
        return this.mIsConfigurationChanged;
    }

    public boolean isUseColorForceDark() {
        return this.mIsUseColorForceDark && !this.mUseOwnerForceDark;
    }

    public boolean isUseColorSoftDraw() {
        return this.mIsUseColorSoftDraw;
    }

    private static class Holder {
        private static final ColorDarkModeManager INSTANCE = new ColorDarkModeManager();

        private Holder() {
        }
    }

    public static ColorDarkModeManager getInstance() {
        return Holder.INSTANCE;
    }

    public void startDelayInjectJS(WebView webView) {
        this.mDarkModeWebManager.startDelayInjectJS(webView);
    }

    public WebViewClient createWebViewClientWrapper(WebView webView, WebViewClient client) {
        return this.mDarkModeWebManager.createWebViewClientWrapper(webView, client);
    }

    public static void log(String content) {
        if (DEBUG) {
            Log.d(TAG, content);
        }
    }

    public static void log(String tag, String content) {
        if (DEBUG) {
            Log.d(tag, content);
        }
    }

    public static boolean isNightMode(Context context) {
        UiModeManager uiModeManager;
        if (context == null || (uiModeManager = (UiModeManager) context.getSystemService("uimode")) == null || uiModeManager.getNightMode() != 2) {
            return false;
        }
        return true;
    }

    public int hideAutoChangeUiMode(int curMode) {
        if (curMode != 0) {
            return curMode;
        }
        log("hideAutoChangeUiMode-->hide uiMode to 1");
        return 1;
    }

    public boolean useForcePowerSave() {
        return false;
    }

    public int changeSystemUiVisibility(int oldSystemUiVisibility) {
        if (isUseColorForceDark() || isUseColorSoftDraw()) {
            if ((oldSystemUiVisibility & 8192) != 0) {
                this.mIsChangeSystemUiVisibility = true;
            }
            return oldSystemUiVisibility & -8193 & -17;
        } else if (!this.mIsChangeSystemUiVisibility) {
            return oldSystemUiVisibility;
        } else {
            this.mIsChangeSystemUiVisibility = false;
            return oldSystemUiVisibility | 8192 | 16;
        }
    }

    private boolean isUseColorForceDark(int value) {
        return (value & 1) != 0;
    }

    private boolean useMixDraw(int value) {
        return (value & 4) != 0;
    }

    private boolean useSoftDraw(int value) {
        return (value & 2) != 0;
    }

    public void refreshForceDark(View decor, int colorForceDarkValue) {
        boolean useColorSoftHardwareDraw = true;
        if (this.mUseOwnerForceDark || decor == null || decor.getContext() == null || isSystemApp(decor.getContext().getPackageName())) {
            ColorSoftDarkModeManager.getInstance().setIsSupportDarkModeStatus(0);
            ColorSoftDarkModeManager.getInstance().setUseHardwareDraw(true);
            return;
        }
        if (!(!"com.tencent.mm".equals(decor.getContext().getPackageName()) || decor.getViewRootImpl() == null || decor.getViewRootImpl().mWindowAttributes == null)) {
            if ((((Object) decor.getViewRootImpl().mWindowAttributes.getTitle()) + "").startsWith("com.tencent.mm/com.tencent.mm.plugin.appbrand.ui.AppBrandUI")) {
                ColorSoftDarkModeManager.getInstance().setIsSupportDarkModeStatus(0);
                ColorSoftDarkModeManager.getInstance().setUseHardwareDraw(true);
                return;
            }
        }
        this.mDecor = decor;
        this.mIsUseColorForceDark = isUseColorForceDark(colorForceDarkValue);
        boolean useSoftDraw = useSoftDraw(colorForceDarkValue);
        boolean useMixDraw = useMixDraw(colorForceDarkValue);
        boolean isUseForceDark = this.mIsUseColorForceDark && !useSoftDraw;
        this.mIsUseColorSoftDraw = this.mIsUseColorForceDark && (useSoftDraw || useMixDraw);
        if (!this.mIsUseColorForceDark || !useSoftDraw) {
            useColorSoftHardwareDraw = false;
        }
        ColorSoftDarkModeManager.getInstance().setIsSupportDarkModeStatus(this.mIsUseColorSoftDraw ? 1 : 0);
        ColorSoftDarkModeManager.getInstance().setUseHardwareDraw(useColorSoftHardwareDraw);
        log("refreshForceDark:" + colorForceDarkValue + ",useSoftDraw:" + useSoftDraw + ",useMixDraw:" + useMixDraw + ",isUseForceDark:" + isUseForceDark + ",useColorSoftDraw:" + this.mIsUseColorSoftDraw + ",useColorSoftHardwareDraw:" + useColorSoftHardwareDraw);
        ThreadedRenderer threadedRenderer = decor.getThreadedRenderer();
        if (threadedRenderer != null && threadedRenderer.setForceDark(isUseForceDark)) {
            invalidateWorld(decor);
        }
        ColorDarkModeWebManager colorDarkModeWebManager = this.mDarkModeWebManager;
        if (colorDarkModeWebManager != null) {
            colorDarkModeWebManager.tryRefreshWebView(decor);
        }
    }

    public void refreshForceDark(View decor) {
        if (!this.mUseOwnerForceDark && decor != null && decor.getContext() != null && !isSystemApp(decor.getContext().getPackageName())) {
            this.mDarkModeWebManager.refreshView(decor);
        }
    }

    /* access modifiers changed from: package-private */
    public void invalidateWorld(View view) {
        checkViewOnDraw(view);
        view.invalidate();
        if (view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view;
            for (int i = 0; i < parent.getChildCount(); i++) {
                invalidateWorld(parent.getChildAt(i));
            }
        }
    }

    public void logForceDarkAllowedStatus(Context context, boolean forceDarkAllowedDefault) {
        if (DEBUG) {
            TypedArray a = context.obtainStyledAttributes(R.styleable.Theme);
            boolean useAutoDark = true;
            boolean isLightTheme = a.getBoolean(279, true);
            boolean forceDarkAllowed = a.getBoolean(278, forceDarkAllowedDefault);
            if (!isLightTheme || !forceDarkAllowed) {
                useAutoDark = false;
            }
            log("logForceDarkAllowedStatus-->packageName:" + context.getPackageName() + ",isLightTheme:" + isLightTheme + ",forceDarkAllowed:" + forceDarkAllowed + ",useAutoDark:" + useAutoDark);
            a.recycle();
        }
    }

    public void logConfigurationNightError(Context context, boolean isNightConfiguration) {
        if (DEBUG && isNightConfiguration != isNightMode(context)) {
            log("logForceDarkAllowedStatus-->packageName:" + context.getPackageName() + ",use App Custom Night Configuration");
        }
    }

    private static boolean isSystemApp(String packageName) {
        if (WHITE_LIST_PACKAGE.contains(packageName) || packageName.startsWith("com.coloros.") || packageName.startsWith("com.heytap.") || packageName.startsWith("com.oppo.") || packageName.startsWith("com.nearme.") || packageName.startsWith("com.google.android.")) {
            return true;
        }
        return false;
    }

    public boolean forceDarkWithoutTheme(Context context, boolean useAutoDark) {
        View view;
        if (useAutoDark) {
            log("forceDarkWithoutTheme-->false, app use owner force dark!");
            this.mIsUseColorForceDark = false;
            this.mUseOwnerForceDark = true;
            this.mIsUseColorSoftDraw = false;
            this.mIsConfigurationChanged = true;
            ColorSoftDarkModeManager.getInstance().setIsSupportDarkModeStatus(0);
            ColorSoftDarkModeManager.getInstance().setUseHardwareDraw(true);
            return true;
        }
        this.mUseOwnerForceDark = false;
        if (context == null) {
            log("forceDarkWithoutTheme-->false, context is null!");
            this.mIsUseColorForceDark = false;
            this.mIsUseColorSoftDraw = false;
            this.mIsConfigurationChanged = true;
            ColorSoftDarkModeManager.getInstance().setIsSupportDarkModeStatus(0);
            ColorSoftDarkModeManager.getInstance().setUseHardwareDraw(true);
            return false;
        }
        boolean oldIsUseColorForceDark = this.mIsUseColorForceDark;
        int result = shouldUseColorForceDark(context, true);
        this.mIsUseColorForceDark = isUseColorForceDark(result);
        boolean useSoftDraw = useSoftDraw(result);
        boolean useMixDraw = useMixDraw(result);
        this.mIsUseColorSoftDraw = this.mIsUseColorForceDark && (useMixDraw || useSoftDraw);
        boolean useColorSoftHardwareDraw = this.mIsUseColorForceDark && useSoftDraw;
        ColorSoftDarkModeManager.getInstance().setIsSupportDarkModeStatus(this.mIsUseColorSoftDraw ? 1 : 0);
        ColorSoftDarkModeManager.getInstance().setUseHardwareDraw(useColorSoftHardwareDraw);
        if (!(!this.mIsConfigurationChanged || (view = this.mDecor) == null || oldIsUseColorForceDark == this.mIsUseColorForceDark)) {
            this.mDarkModeWebManager.tryRefreshWebView(view);
        }
        this.mIsConfigurationChanged = true;
        log("forceDarkWithoutTheme-->packageName:" + context.getPackageName() + "-->use color Force Dark:" + this.mIsUseColorForceDark + "-->useSoftDraw:" + useSoftDraw + ",useMixDraw:" + useMixDraw + "useColorSoftDraw:" + this.mIsUseColorSoftDraw + ",useColorSoftHardwareDraw:" + useColorSoftHardwareDraw);
        return this.mIsUseColorForceDark && !useSoftDraw;
    }

    private boolean appDisableSystemDarkMode(Context context) {
        int i = sDisableSystemDarkModeByApp;
        if (i != -1) {
            return i == 1;
        }
        sDisableSystemDarkModeByApp = 0;
        ApplicationInfo appInfo = null;
        if (context != null) {
            try {
                appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
            } catch (Exception ex) {
                log("appDisableSystemDarkMode ex :" + ex.getMessage());
            }
        }
        if (appInfo == null || appInfo.metaData == null) {
            return false;
        }
        try {
            Boolean disableSystemDarkMode = (Boolean) appInfo.metaData.get(COLOROS_DISABLE_SYSTEM_DARKMODE_META_DATA);
            sDisableSystemDarkModeByApp = (disableSystemDarkMode == null || !disableSystemDarkMode.booleanValue()) ? 0 : 1;
            log("appDisableSystemDarkMode :" + disableSystemDarkMode + "; sDisable = " + sDisableSystemDarkModeByApp);
            return sDisableSystemDarkModeByApp == 1;
        } catch (Exception ex2) {
            log("appDisableSystemDarkMode error:" + ex2.getMessage());
            return false;
        }
    }

    public void handleStartingWindow(Context context, String appWindowToken, Window window, View decorView) {
        int color;
        if (context != null && isNightMode(context)) {
            boolean shouldMakeDark = false;
            int darkColor = -1;
            if (decorView != null) {
                Drawable drawable = decorView.getBackground();
                if ((drawable instanceof ColorDrawable) && (darkColor = ColorDarkModeUtils.makeDark((color = ((ColorDrawable) drawable).getColor()))) != color) {
                    shouldMakeDark = true;
                }
            }
            if (shouldMakeDark && (decorView instanceof ViewGroup)) {
                ViewGroup viewGroup = (ViewGroup) decorView;
                View view = new View(viewGroup.getContext());
                view.setBackgroundColor(darkColor);
                window.setStatusBarColor(darkColor);
                window.setNavigationBarColor(darkColor);
                viewGroup.addView(view, 0);
                log("handleStartingWindow-->appWindowToken:" + appWindowToken);
            }
        }
    }

    public int shouldUseColorForceDark(Context context, boolean useCache) {
        if (context == null || isSystemApp(context.getPackageName())) {
            return 0;
        }
        return getDarkModeData(context, context.getPackageName(), useCache);
    }

    public int getDarkModeData(Context context, String packageName, boolean useCache) {
        if (packageName == null) {
            return 0;
        }
        long startTime = SystemClock.uptimeMillis();
        if (this.mOppoActivityManager == null) {
            this.mOppoActivityManager = new OppoActivityManager();
        }
        int result = 0;
        try {
            result = this.mOppoActivityManager.getDarkModeData(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        long costTime = SystemClock.uptimeMillis() - startTime;
        if (costTime >= 5) {
            log("getDarkModeData-->use " + costTime + "ms");
        }
        return result;
    }

    public void changeUsageForceDarkAlgorithmType(View view, int type) {
        OppoBaseView baseView;
        if (view != null && (baseView = (OppoBaseView) ColorTypeCastingHelper.typeCasting(OppoBaseView.class, view)) != null) {
            baseView.setUsageForceDarkAlgorithmType(type);
        }
    }

    private void initThreadPool() {
        if (this.mThreadPool == null) {
            this.mThreadPool = Executors.newSingleThreadExecutor();
        }
    }

    private boolean initFile(String dir, String path) {
        File filePath = new File(dir);
        if (!filePath.exists()) {
            try {
                if (!filePath.mkdirs()) {
                    log("initFile: failed create dir = " + dir);
                } else {
                    log("initFile: create dir = " + dir);
                }
            } catch (Exception e) {
                log("failed create dir " + e);
            }
        }
        File file = new File(dir, path);
        if (file.exists()) {
            return false;
        }
        try {
            if (!file.createNewFile()) {
                log("initFile: file.createNewFile() failed");
                return false;
            }
            log("initFile: create file = " + path);
            return true;
        } catch (Exception e2) {
            log("failed create file " + e2);
            return false;
        }
    }

    public void init(Context context) {
        try {
            long startTime = SystemClock.uptimeMillis();
            this.mServerContext = context;
            initFile(DARKMODE_DIR_PATH, FILENAME_OPEN_APP);
            initFile(DARKMODE_DIR_PATH, FILENAME_CLICK_APP);
            initFile(DARKMODE_DIR_PATH, FILENAME_BLACK_LIST);
            lambda$updateListInThreadPool$0$ColorDarkModeManager(context.getUserId());
            initObserver();
            log("init cost " + (SystemClock.uptimeMillis() - startTime) + " ms");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initObserver() {
        ColorSettings.registerChangeListener(this.mServerContext, SETTING_OPEN_PATH, 0, this.mConfigChangeListener);
        ColorSettings.registerChangeListener(this.mServerContext, SETTING_BLACK_LIST, 0, this.mRUSChangeListener);
        this.mServerContext.registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.color.darkmode.ColorDarkModeManager.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", 0);
                ColorSettings.unRegisterChangeListener(ColorDarkModeManager.this.mServerContext, ColorDarkModeManager.this.mConfigChangeListener);
                ColorSettings.unRegisterChangeListener(ColorDarkModeManager.this.mServerContext, ColorDarkModeManager.this.mRUSChangeListener);
                if (userId == 0) {
                    ColorSettings.registerChangeListener(ColorDarkModeManager.this.mServerContext, ColorDarkModeManager.SETTING_OPEN_PATH, 0, ColorDarkModeManager.this.mConfigChangeListener);
                    ColorSettings.registerChangeListener(ColorDarkModeManager.this.mServerContext, ColorDarkModeManager.SETTING_BLACK_LIST, 0, ColorDarkModeManager.this.mRUSChangeListener);
                } else {
                    ColorSettings.registerChangeListenerAsUser(ColorDarkModeManager.this.mServerContext, ColorDarkModeManager.SETTING_OPEN_PATH, userId, 0, ColorDarkModeManager.this.mConfigChangeListener);
                    ColorSettings.registerChangeListenerAsUser(ColorDarkModeManager.this.mServerContext, ColorDarkModeManager.SETTING_BLACK_LIST, userId, 0, ColorDarkModeManager.this.mRUSChangeListener);
                }
                ColorDarkModeManager.log("action user switched:" + userId);
                ColorDarkModeManager.this.mUserId = userId;
                ColorDarkModeManager colorDarkModeManager = ColorDarkModeManager.this;
                colorDarkModeManager.updateListInThreadPool(colorDarkModeManager.mUserId);
            }
        }, UserHandle.ALL, new IntentFilter("android.intent.action.USER_SWITCHED"), null, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateListInThreadPool(int userId) {
        initThreadPool();
        this.mThreadPool.execute(new Runnable(userId) {
            /* class com.color.darkmode.$$Lambda$ColorDarkModeManager$KeF7sI_jGzjMHo7HNpPcRggsH78 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ColorDarkModeManager.this.lambda$updateListInThreadPool$0$ColorDarkModeManager(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: updateList */
    public void lambda$updateListInThreadPool$0$ColorDarkModeManager(int userId) {
        try {
            long startTime = SystemClock.uptimeMillis();
            Set<String> romUpdateOpenApp = new HashSet<>();
            Set<String> romUpdateHiddenApp = new HashSet<>();
            Set<String> romAdaptedHiddenApp = new HashSet<>();
            parserHiddenXmlValueToSet(this.mServerContext, romUpdateHiddenApp, romUpdateOpenApp, romAdaptedHiddenApp);
            Collection<? extends String> openApp = getLineSet(this.mServerContext, SETTING_OPEN_PATH);
            Collection<?> clickApp = getLineSet(this.mServerContext, SETTING_CLICK_PATH);
            Set<String> list = new HashSet<>(romUpdateOpenApp);
            list.removeAll(clickApp);
            list.addAll(openApp);
            list.removeAll(romUpdateHiddenApp);
            synchronized (LOCK) {
                this.mOpenAppList = list;
                this.mAdaptedHiddenAppLists = romAdaptedHiddenApp;
            }
            log("updateList userId = " + userId + ", cost " + (SystemClock.uptimeMillis() - startTime) + " ms");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private InputStream getFileStreamFromProvider(Context context, String path) {
        try {
            return ColorSettings.readConfigAsUser(context, path, this.mUserId, 0);
        } catch (Throwable th) {
            return null;
        }
    }

    private Set<String> getLineSet(Context context, String path) {
        Set<String> result = new HashSet<>();
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = getFileStreamFromProvider(context, path);
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String str = bufferedReader.readLine();
                while (str != null && !str.isEmpty()) {
                    result.add(str);
                    str = bufferedReader.readLine();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            close(null);
            throw th;
        }
        close(bufferedReader);
        return result;
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parserHiddenXmlValueToSet(Context context, Set<String> hiddenSet, Set<String> openSet, Set<String> adaptedHiddenSet) {
        int type;
        int hideType;
        if (hiddenSet != null && openSet != null && context != null && adaptedHiddenSet != null) {
            Map<String, Integer> entitySet = new ArrayMap<>();
            InputStreamReader reader = null;
            InputStream inputStream = null;
            try {
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                inputStream = getFileStreamFromProvider(context, SETTING_BLACK_LIST);
                if (inputStream != null) {
                    reader = new InputStreamReader(inputStream);
                    parser.setInput(reader);
                    parser.nextTag();
                    do {
                        type = parser.next();
                        if (type == 2) {
                            String tag = parser.getName();
                            if ("h".equals(tag)) {
                                String value = parser.getAttributeValue(null, "attr");
                                if (value != null) {
                                    hiddenSet.add(value);
                                    try {
                                        hideType = Integer.parseInt(parser.getAttributeValue(null, "type"));
                                    } catch (Exception e) {
                                        hideType = 0;
                                    }
                                    if (hideType == 1 || hideType == 2) {
                                        adaptedHiddenSet.add(value);
                                        continue;
                                    }
                                } else {
                                    continue;
                                }
                            } else if ("w".equals(tag)) {
                                int value2 = 0;
                                try {
                                    value2 = Integer.parseInt(parser.getAttributeValue(null, "type"));
                                } catch (Exception e2) {
                                }
                                String attr = parser.getAttributeValue(null, "attr");
                                if (!TextUtils.isEmpty(attr)) {
                                    entitySet.put(attr, Integer.valueOf(value2));
                                    openSet.add(attr);
                                    continue;
                                } else {
                                    continue;
                                }
                            } else if ("o".equals(tag)) {
                                int value3 = 0;
                                try {
                                    value3 = Integer.parseInt(parser.getAttributeValue(null, "type"));
                                } catch (Exception e3) {
                                }
                                String attr2 = parser.getAttributeValue(null, "attr");
                                if (!TextUtils.isEmpty(attr2)) {
                                    entitySet.put(attr2, Integer.valueOf(value3));
                                    continue;
                                } else {
                                    continue;
                                }
                            } else {
                                continue;
                            }
                        }
                    } while (type != 1);
                }
            } catch (Exception e4) {
                e4.printStackTrace();
            } catch (Throwable th) {
                close(null);
                throw th;
            }
            close(reader);
            synchronized (TYPE_LOCK) {
                this.mAppTypeList = entitySet;
            }
        }
    }

    public int getDarkModeData(String packageName) {
        try {
            if (!isNightMode(this.mServerContext)) {
                return 0;
            }
            return getDarkModeDateFromAppList(packageName);
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public int getDarkModeDateFromAppList(String packageName) {
        int result = 0;
        try {
            synchronized (LOCK) {
                if (this.mOpenAppList != null && packageName != null && !packageName.isEmpty() && this.mOpenAppList.contains(packageName)) {
                    result = 0 | 1;
                    synchronized (TYPE_LOCK) {
                        if (this.mAppTypeList.isEmpty()) {
                            return result | 2;
                        }
                        int type = this.mAppTypeList.getOrDefault(packageName, 1).intValue();
                        if (type == 1) {
                            result |= 2;
                        } else if (type == 2) {
                            result |= 4;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public boolean isDarkModePage(String packageName, boolean systemDarkMode) {
        if (!systemDarkMode) {
            return false;
        }
        if (!isSystemApp(packageName)) {
            return isDarkModeApp(packageName);
        }
        return systemDarkMode;
    }

    public void handleDisplayMCAFeature(int mode) {
        initThreadPool();
        this.mThreadPool.execute(new Runnable(mode) {
            /* class com.color.darkmode.$$Lambda$ColorDarkModeManager$O0n7GQ8wlR37hdSfEmxYQQPKw5s */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                ColorDarkModeManager.lambda$handleDisplayMCAFeature$1(this.f$0);
            }
        });
    }

    static /* synthetic */ void lambda$handleDisplayMCAFeature$1(int mode) {
        if (mode == 2) {
            writeFileNodeValue(DISPLAY_MCA_FILE, 0);
            log("close-->node:MCA");
            return;
        }
        writeFileNodeValue(DISPLAY_MCA_FILE, 1);
        log("open-->node:MCA");
    }

    public static void writeFileNodeValue(String str, int value) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(new File(str));
            writer.write(String.valueOf(value));
            writer.flush();
            try {
                writer.close();
            } catch (Exception ioe) {
                ioe.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (writer != null) {
                writer.close();
            }
        } catch (Throwable th) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ioe2) {
                    ioe2.printStackTrace();
                }
            }
            throw th;
        }
    }

    private boolean isDarkModeApp(String packageName) {
        boolean z;
        synchronized (LOCK) {
            boolean isOpenApp = false;
            boolean isAdaptedApp = false;
            if (!(this.mOpenAppList == null || packageName == null || packageName.isEmpty())) {
                isOpenApp = this.mOpenAppList.contains(packageName);
            }
            if (!(this.mAdaptedHiddenAppLists == null || packageName == null || packageName.isEmpty())) {
                isAdaptedApp = this.mAdaptedHiddenAppLists.contains(packageName);
            }
            if (!isAdaptedApp) {
                if (!isOpenApp) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    public void initDarkModeStatus(Application application) {
        ColorSoftDarkModeManager.initDarkModeStatus(application);
    }

    public int handleEraseColor(int color) {
        if (ColorSoftDarkModeManager.getInstance().isInDarkMode()) {
            return ColorSoftDarkModeManager.getDarkModeColor(color);
        }
        return color;
    }

    public boolean shouldIntercept() {
        return true;
    }

    public Bitmap handleDecodeStream(InputStream is, Rect pad, BitmapFactory.Options opts) {
        OppoBaseBitmap baseBitmap;
        Bitmap bm = BitmapFactory.decodeStream(is, pad, opts);
        if (!(bm == null || (baseBitmap = (OppoBaseBitmap) ColorTypeCastingHelper.typeCasting(OppoBaseBitmap.class, bm)) == null)) {
            baseBitmap.setIsAssetSource(true);
        }
        return bm;
    }

    public Shader getDarkModeLinearGradient(float mX0, float mY0, float mX1, float mY1, int[] mColors, float[] mPositions, int mColor0, int mColor1, Shader.TileMode mTileMode) {
        if (mColors == null) {
            return new LinearGradient(mX0, mY0, mX1, mY1, ColorSoftDarkModeManager.getDarkModeColor(mColor0), ColorSoftDarkModeManager.getDarkModeColor(mColor1), mTileMode);
        }
        return new LinearGradient(mX0, mY0, mX1, mY1, ColorSoftDarkModeManager.getDarkModeColors(mColors), mPositions != null ? (float[]) mPositions.clone() : null, mTileMode);
    }

    public Shader getDarkModeRadialGradient(float mX, float mY, float mRadius, int[] mColors, int mCenterColor, float[] mPositions, int mEdgeColor, Shader.TileMode mTileMode) {
        if (mColors == null) {
            return new RadialGradient(mX, mY, mRadius, ColorSoftDarkModeManager.getDarkModeColor(mCenterColor), ColorSoftDarkModeManager.getDarkModeColor(mEdgeColor), mTileMode);
        }
        return new RadialGradient(mX, mY, mRadius, ColorSoftDarkModeManager.getDarkModeColors(mColors), mPositions != null ? (float[]) mPositions.clone() : null, mTileMode);
    }

    public Shader getDarkModeSweepGradient(float mCx, float mCy, int[] mColors, float[] mPositions, int mColor0, int mColor1) {
        if (mColors == null) {
            return new SweepGradient(mCx, mCy, ColorSoftDarkModeManager.getDarkModeColor(mColor0), ColorSoftDarkModeManager.getDarkModeColor(mColor1));
        }
        return new SweepGradient(mCx, mCy, ColorSoftDarkModeManager.getDarkModeColors(mColors), mPositions != null ? (float[]) mPositions.clone() : null);
    }

    public int getVectorColor(int color) {
        return ColorSoftDarkModeManager.getVectorColor(color);
    }

    public void changeColorFilterInDarkMode(ColorFilter colorFilter) {
        ColorSoftDarkModeManager.getInstance().changeColorFilterInDarkMode(colorFilter);
    }

    public boolean isInDarkMode(boolean isHardware) {
        return ColorSoftDarkModeManager.getInstance().isInDarkMode(isHardware);
    }

    public OppoBaseBaseCanvas.RealPaintState getRealPaintState(Paint paint) {
        return ColorSoftDarkModeManager.getInstance().getRealPaintState(paint);
    }

    public void changePaintWhenDrawText(Paint paint) {
        ColorSoftDarkModeManager.getInstance().changePaintWhenDrawText(paint);
    }

    public void resetRealPaintIfNeed(Paint paint, OppoBaseBaseCanvas.RealPaintState realPaintState) {
        ColorSoftDarkModeManager.getInstance().resetRealPaintIfNeed(paint, realPaintState);
    }

    public void changePaintWhenDrawArea(Paint paint, RectF rectF) {
        ColorSoftDarkModeManager.getInstance().changePaintWhenDrawArea(paint, rectF);
    }

    public void changePaintWhenDrawArea(Paint paint, RectF rectF, Path path) {
        ColorSoftDarkModeManager.getInstance().changePaintWhenDrawArea(paint, rectF, path);
    }

    public void changePaintWhenDrawPatch(NinePatch patch, Paint paint, RectF rectF) {
        ColorSoftDarkModeManager.getInstance().changePaintWhenDrawPatch(patch, paint, rectF);
    }

    public int changeWhenDrawColor(int color, boolean isDarkMode) {
        return ColorSoftDarkModeManager.getInstance().changeWhenDrawColor(color, isDarkMode);
    }

    public void changePaintWhenDrawBitmap(Paint paint, Bitmap bitmap, RectF rectF) {
        ColorSoftDarkModeManager.getInstance().changePaintWhenDrawBitmap(paint, bitmap, rectF);
    }

    public int[] getDarkModeColors(int[] colors) {
        return ColorSoftDarkModeManager.getDarkModeColors(colors);
    }

    public Paint getPaintWhenDrawPatch(NinePatch patch, Paint paint, RectF rectF) {
        return ColorSoftDarkModeManager.getInstance().changePaintWhenDrawPatch(patch, paint, rectF);
    }

    public Paint getPaintWhenDrawBitmap(Paint paint, Bitmap bitmap, RectF rectF) {
        return ColorSoftDarkModeManager.getInstance().changePaintWhenDrawBitmap(paint, bitmap, rectF);
    }
}
