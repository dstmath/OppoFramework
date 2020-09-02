package com.android.server.pm;

import android.os.SystemProperties;
import android.util.ArraySet;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import java.util.Iterator;
import java.util.Set;

public class ColorExpDefaultBrowserManager implements IColorExpDefaultBrowserManager {
    private static final String CHROME_BROWSER_FEATURE = "oppo.default.browser.chrome";
    private static final String CHROME_BROWSER_PKG_NAME = "com.android.chrome";
    private static final boolean DEBUG = true;
    public static boolean EXP_VERSION = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    private static final Set<String> OPPO_BROWSER_PACKAGES = new ArraySet();
    private static final String OPPO_BROWSER_PKG_NAME = "com.android.browser";
    private static final String OPPO_BROWSER_PKG_NAME_NEW = "com.coloros.browser";
    private static final String OPPO_BROWSER_PKG_NAME_NEW_UNIQUE = "com.heytap.browser";
    private static final String OPPO_DEFAULT_BROWSER_FEATURE = "oppo.exp.default.browser";
    private static final String REALME_BROWSER_PKG_NAME = "com.nearme.browser";
    private static final String TAG = "ColorExpDefaultBrowser";
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static ColorExpDefaultBrowserManager sInstance = null;
    private PackageManagerService mPms = null;
    private IColorPackageManagerServiceEx mPmsEx = null;

    static {
        OPPO_BROWSER_PACKAGES.add(OPPO_BROWSER_PKG_NAME);
        OPPO_BROWSER_PACKAGES.add(OPPO_BROWSER_PKG_NAME_NEW);
        OPPO_BROWSER_PACKAGES.add(REALME_BROWSER_PKG_NAME);
        OPPO_BROWSER_PACKAGES.add(REALME_BROWSER_PKG_NAME);
        OPPO_BROWSER_PACKAGES.add(OPPO_BROWSER_PKG_NAME_NEW_UNIQUE);
    }

    public static ColorExpDefaultBrowserManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorExpDefaultBrowserManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorExpDefaultBrowserManager();
                }
            }
        }
        return sInstance;
    }

    private ColorExpDefaultBrowserManager() {
        Slog.d(TAG, "Constructor");
    }

    public void init(IColorPackageManagerServiceEx ex) {
        Slog.d(TAG, "init");
        this.mPmsEx = ex;
        IColorPackageManagerServiceEx iColorPackageManagerServiceEx = this.mPmsEx;
        if (iColorPackageManagerServiceEx != null) {
            this.mPms = iColorPackageManagerServiceEx.getPackageManagerService();
        }
    }

    public void setExpDefaultBrowser() {
        PackageManagerService packageManagerService = this.mPms;
        if (packageManagerService != null) {
            if (packageManagerService.isFirstBoot()) {
                SystemProperties.set("persist.sys.default.exp.browser", "1");
            }
            boolean hasDefaultBrowser = this.mPms.hasSystemFeature(OPPO_DEFAULT_BROWSER_FEATURE, 0);
            boolean defaultChromeBrowser = this.mPms.hasSystemFeature(CHROME_BROWSER_FEATURE, 0);
            if (EXP_VERSION) {
                String browserFlag = SystemProperties.get("persist.sys.default.exp.browser", "");
                String rusValue = SystemProperties.get("ro.com.google.clientidbase.ms", "");
                if ((this.mPms.isFirstBoot() || "1".equals(browserFlag)) && "android-oppo-rev1".equals(rusValue)) {
                    PackageSetting ps = null;
                    Iterator<String> it = OPPO_BROWSER_PACKAGES.iterator();
                    while (true) {
                        if (it.hasNext()) {
                            String browserName = it.next();
                            if (browserName != null && (ps = this.mPms.mSettings.getPackageLPr(browserName)) != null) {
                                ColorPackageManagerHelper.initDefaultPackageList(browserName);
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    if (ps == null) {
                        ColorPackageManagerHelper.initDefaultPackageList(CHROME_BROWSER_PKG_NAME);
                    }
                } else if (defaultChromeBrowser) {
                    ColorPackageManagerHelper.initDefaultPackageList(CHROME_BROWSER_PKG_NAME);
                } else if (hasDefaultBrowser) {
                    for (String browserName2 : OPPO_BROWSER_PACKAGES) {
                        if (browserName2 != null && this.mPms.mSettings.getPackageLPr(browserName2) != null) {
                            ColorPackageManagerHelper.initDefaultPackageList(browserName2);
                            return;
                        }
                    }
                }
            }
        }
    }
}
