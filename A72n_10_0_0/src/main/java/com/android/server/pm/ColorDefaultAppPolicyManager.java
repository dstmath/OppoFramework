package com.android.server.pm;

import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OppoBaseIntent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.util.ColorTypeCastingHelper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ColorDefaultAppPolicyManager implements IColorDefaultAppPolicyManager {
    private static final String CHROME_BROWSER_PKG_NAME = "com.android.chrome";
    static final boolean EXP_VERSION = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    private static final String FEATURE_BUSINESS_CUSTOM = "oppo.business.custom";
    private static final String FEATURE_CHILDREN_SPACE = "oppo.childspace.support";
    private static final String OPPO_BROWSER_PKG_NAME = "com.android.browser";
    private static final String OPPO_DEFAULT_BROWSER_FEATURE = "oppo.exp.default.browser";
    private static final String PKG_ANDROID_PACKAGEINSTALLER = "com.android.packageinstaller";
    private static final String PKG_BOOTREG = "com.coloros.bootreg";
    private static final String PKG_CHILDREN = "com.coloros.childrenspace";
    private static final String PKG_OPPO_LAUNCHER = "com.oppo.launcher";
    private static final String PKG_SETTINGS = "com.android.settings";
    private static final String REGION_INDIA = "IN";
    private static final String REGION_INDONESIA = "ID";
    private static final String SETTINGS_KEY_CHILDREN_MODE_ON = "children_mode_on";
    public static final String TAG = "ColorDefaultAppPolicyManager";
    private static ColorDefaultAppPolicyManager sColorDefaultAppPolicyManager = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) EXP_VERSION);
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private Context mContext = null;
    boolean mDynamicDebug = EXP_VERSION;
    private PackageManagerService mPms = null;

    public static ColorDefaultAppPolicyManager getInstance() {
        if (sColorDefaultAppPolicyManager == null) {
            sColorDefaultAppPolicyManager = new ColorDefaultAppPolicyManager();
        }
        return sColorDefaultAppPolicyManager;
    }

    private ColorDefaultAppPolicyManager() {
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        if (pmsEx != null) {
            this.mPms = pmsEx.getPackageManagerService();
            this.mContext = pmsEx.getContext();
        }
        registerLogModule();
    }

    public void addBrowserToDefaultPackageList() {
        boolean hasDefaultBrowser = this.mPms.hasSystemFeature(OPPO_DEFAULT_BROWSER_FEATURE, 0);
        boolean defaultChromeBrowser = this.mPms.hasSystemFeature("oppo.default.browser.chrome", 0);
        if (EXP_VERSION && defaultChromeBrowser) {
            ColorPackageManagerHelper.initDefaultPackageList(CHROME_BROWSER_PKG_NAME);
        } else if (EXP_VERSION && hasDefaultBrowser) {
            ColorPackageManagerHelper.initDefaultPackageList(OPPO_BROWSER_PKG_NAME);
            ColorPackageManagerHelper.initDefaultPackageList("com.coloros.browser");
        }
    }

    public void addExpBrowserToDefaultPackageList(boolean firsBoot) {
        if (firsBoot) {
            SystemProperties.set("persist.sys.q.default.exp.browser", "1");
        }
    }

    public boolean isGotDefaultAppBeforeAddPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        String callingPkg = OppoPackageManagerHelper.getProcessNameByPid(Binder.getCallingPid());
        if (!(callingPkg == null || !callingPkg.equals("system:ui") || activity == null)) {
            boolean isSystemApp = EXP_VERSION;
            String pkgName = activity.getPackageName();
            if (pkgName != null) {
                synchronized (this.mPms.mPackages) {
                    PackageParser.Package pkg = (PackageParser.Package) this.mPms.mPackages.get(pkgName);
                    if (pkg != null && pkg.applicationInfo.isSystemApp()) {
                        isSystemApp = true;
                    }
                }
            }
            if (ColorPackageManagerHelper.isSetContainsOppoDefaultPkg(set, activity) && !isSystemApp) {
                Slog.d(TAG, "addPreferredActivity called from ResolverActivity and contains oppo default package, skip!");
                return true;
            }
        }
        if (ColorPackageManagerHelper.forbiddenSetPreferredActivity(this.mPms, filter)) {
            return true;
        }
        return EXP_VERSION;
    }

    public boolean forbiddenSetPreferredActivity(IntentFilter filter) {
        return ColorPackageManagerHelper.forbiddenSetPreferredActivity(this.mPms, filter);
    }

    public boolean isDefaultAppEnabled(Intent intent) {
        int oppoRealCallingUid = -1;
        OppoBaseIntent baseIntent = typeCasting(intent);
        if (baseIntent != null) {
            try {
                oppoRealCallingUid = baseIntent.getCallingUid();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (oppoRealCallingUid < 0) {
            oppoRealCallingUid = Binder.getCallingUid();
        }
        String callerName = this.mPms.getNameForUid(oppoRealCallingUid);
        if (TextUtils.isEmpty(callerName)) {
            callerName = this.mPms.getNameForUid(Binder.getCallingUid());
        }
        return ColorPackageManagerHelper.isDefaultAppEnabled(callerName, OppoFeatureCache.get(IColorFullmodeManager.DEFAULT).isClosedSuperFirewall(), intent);
    }

    public boolean isQueryListContainsDefaultPkg(List<ResolveInfo> query) {
        return ColorPackageManagerHelper.isQueryListContainsOppoDefaultPkg(query);
    }

    public ResolveInfo getForceAppBeforeChooseBestActivity(Intent intent, List<ResolveInfo> query) {
        boolean hasArmyFeatury = this.mPms.hasSystemFeature(FEATURE_BUSINESS_CUSTOM, 0);
        if (!isDefaultAppEnabled(intent)) {
            return null;
        }
        List<String> mFoundPackageList = new ArrayList<>();
        ResolveInfo matchResolve = null;
        for (int p = 0; p < query.size(); p++) {
            ResolveInfo r = query.get(p);
            ActivityInfo ait = r.activityInfo;
            if (hasArmyFeatury && PKG_ANDROID_PACKAGEINSTALLER.equals(ait.packageName)) {
                Slog.d(TAG, "Army feature filter app ai.packageName = " + ait.packageName);
            } else if (ColorPackageManagerHelper.isOppoForceApp(ait.packageName)) {
                matchResolve = r;
                if (!mFoundPackageList.contains(ait.packageName)) {
                    mFoundPackageList.add(ait.packageName);
                }
            } else if (ColorPackageManagerHelper.isOppoDefaultApp(ait.packageName) && !mFoundPackageList.contains(ait.packageName)) {
                mFoundPackageList.add(ait.packageName);
            }
        }
        if (matchResolve == null || mFoundPackageList.size() != 1) {
            return null;
        }
        Slog.d(TAG, "Force app ai.packageName = " + matchResolve.activityInfo.packageName + " ai.name = " + matchResolve.activityInfo.name);
        return matchResolve;
    }

    /* JADX INFO: Multiple debug info for r7v13 android.content.pm.ActivityInfo: [D('ait' android.content.pm.ActivityInfo), D('inChildMode' boolean)] */
    public ResolveInfo getDefaultAppAfterChooseBestActivity(Intent intent, List<ResolveInfo> query, ResolveInfo ri) {
        ResolveInfo oppoLauncher;
        boolean hasArmyFeatury;
        boolean bChildrenMode;
        boolean isDefaultApp = isDefaultAppEnabled(intent);
        boolean inChildMode = this.mPms.hasSystemFeature(FEATURE_CHILDREN_SPACE, 0);
        boolean bChildrenMode2 = inChildMode && Settings.Global.getInt(this.mContext.getContentResolver(), SETTINGS_KEY_CHILDREN_MODE_ON, 0) == 1;
        boolean hasArmyFeatury2 = this.mPms.hasSystemFeature(FEATURE_BUSINESS_CUSTOM, 0);
        if (isDefaultApp) {
            int p = 0;
            while (true) {
                if (p >= query.size()) {
                    break;
                }
                ResolveInfo r = query.get(p);
                if ("com.oppo.launcher".equals(r.activityInfo.packageName)) {
                    oppoLauncher = r;
                    break;
                }
                p++;
            }
        }
        oppoLauncher = null;
        int oppoRealCallingUid = -1;
        OppoBaseIntent baseIntent = typeCasting(intent);
        if (baseIntent != null) {
            try {
                oppoRealCallingUid = baseIntent.getCallingUid();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (oppoRealCallingUid < 0) {
            oppoRealCallingUid = Binder.getCallingUid();
        }
        String callerName = this.mPms.getNameForUid(oppoRealCallingUid);
        if (TextUtils.isEmpty(callerName)) {
            callerName = this.mPms.getNameForUid(Binder.getCallingUid());
        }
        boolean openFlag = ColorPackageManagerHelper.hasFileManagerOpenFlag(intent, callerName);
        if (ri != null) {
            ActivityInfo ait = ri.activityInfo;
            if (bChildrenMode2 && oppoLauncher != null && ait != null && !"com.oppo.launcher".equals(ait.packageName) && !PKG_BOOTREG.equals(ait.packageName) && !PKG_SETTINGS.equals(ait.packageName)) {
                return oppoLauncher;
            }
            if (!openFlag) {
                return ri;
            }
        }
        if (isDefaultApp) {
            HashMap<String, Integer> mFoundPackageMap = new HashMap<>();
            int pos = 0;
            int match = 0;
            int p2 = 0;
            while (p2 < query.size()) {
                ResolveInfo r2 = query.get(p2);
                ActivityInfo ait2 = r2.activityInfo;
                if (hasArmyFeatury2) {
                    bChildrenMode = bChildrenMode2;
                    hasArmyFeatury = hasArmyFeatury2;
                    if (PKG_ANDROID_PACKAGEINSTALLER.equals(ait2.packageName)) {
                        Slog.d(TAG, "Army feature filter app ai.packageName = " + ait2.packageName);
                        p2++;
                        isDefaultApp = isDefaultApp;
                        inChildMode = inChildMode;
                        bChildrenMode2 = bChildrenMode;
                        hasArmyFeatury2 = hasArmyFeatury;
                    }
                } else {
                    bChildrenMode = bChildrenMode2;
                    hasArmyFeatury = hasArmyFeatury2;
                }
                if (ColorPackageManagerHelper.isOppoDefaultApp(ait2.packageName)) {
                    if (!mFoundPackageMap.containsKey(ait2.packageName)) {
                        mFoundPackageMap.put(ait2.packageName, Integer.valueOf(r2.priority));
                        match++;
                        pos = p2;
                    } else if (mFoundPackageMap.get(ait2.packageName).intValue() < r2.priority) {
                        pos = p2;
                    }
                }
                p2++;
                isDefaultApp = isDefaultApp;
                inChildMode = inChildMode;
                bChildrenMode2 = bChildrenMode;
                hasArmyFeatury2 = hasArmyFeatury;
            }
            if (1 == match) {
                Slog.d(TAG, "oppo select Default app ai.packageName = " + query.get(pos).activityInfo.packageName + " ai.name = " + query.get(pos).activityInfo.name);
                if (!openFlag) {
                    return query.get(pos);
                }
            }
        }
        ColorPackageManagerHelper.filterBlackList(this.mContext, intent, query);
        if (query.size() == 1) {
            return query.get(0);
        }
        return null;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public List<ResolveInfo> queryColorFilteredIntentActivities(Intent intent, String resolvedType, int flags, int userId) {
        String callerName = this.mPms.getNameForUid(Binder.getCallingUid());
        List<ResolveInfo> query = getInner().queryIntentActivitiesInternal(intent, resolvedType, flags, userId);
        boolean openFlag = ColorPackageManagerHelper.hasFileManagerOpenFlag(intent, callerName);
        PackageManagerService packageManagerService = this.mPms;
        if (PackageManagerService.DEBUG_PREFERRED) {
            Slog.d(TAG, "queryIntentActivities, openFlag=" + openFlag);
        }
        if (openFlag) {
            ColorPackageManagerHelper.filterBlackList(this.mContext, intent, query);
        }
        if (callerName == null || !callerName.startsWith("oppo.uid.launcher:")) {
            Intent intentHome = new Intent("android.intent.action.MAIN");
            intentHome.addCategory("android.intent.category.HOME");
            List<ResolveInfo> homeActivitys = new ArrayList<>();
            try {
                homeActivitys = getInner().queryIntentActivitiesInternal(intentHome, (String) null, 65536, userId);
            } catch (Exception e) {
                Slog.d(TAG, "queryIntentActivities homeActivitys:" + e.getMessage());
            }
            new ArrayList();
            Iterator<ResolveInfo> it = homeActivitys.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ResolveInfo ri = it.next();
                if (callerName != null && ri.activityInfo != null && callerName.equals(ri.activityInfo.packageName) && !PKG_SETTINGS.equals(ri.activityInfo.packageName)) {
                    ColorPackageManagerHelper.filterHideLauncherIconList(this.mContext, query);
                    break;
                }
            }
        } else {
            ColorPackageManagerHelper.filterHideLauncherIconList(this.mContext, query);
        }
        return query;
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog#### mDynamicDebug = " + this.mDynamicDebug);
        setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug = " + this.mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorDefaultAppPolicyManager.class.getName());
            Slog.i(TAG, "invoke end!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }

    private static OppoBaseIntent typeCasting(Intent intent) {
        if (intent != null) {
            return (OppoBaseIntent) ColorTypeCastingHelper.typeCasting(OppoBaseIntent.class, intent);
        }
        return null;
    }

    private static OppoBasePackageManagerService typeCasting(PackageManagerService pms) {
        if (pms != null) {
            return (OppoBasePackageManagerService) ColorTypeCastingHelper.typeCasting(OppoBasePackageManagerService.class, pms);
        }
        return null;
    }

    private IColorPackageManagerServiceInner getInner() {
        OppoBasePackageManagerService base = typeCasting(this.mPms);
        if (base == null || base.mColorPmsInner == null) {
            return IColorPackageManagerServiceInner.DEFAULT;
        }
        return base.mColorPmsInner;
    }
}
