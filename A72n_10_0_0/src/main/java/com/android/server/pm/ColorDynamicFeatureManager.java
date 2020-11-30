package com.android.server.pm;

import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageParser;
import android.os.Binder;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.server.OppoBaseSystemConfig;
import com.android.server.SystemConfig;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.util.ColorTypeCastingHelper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public class ColorDynamicFeatureManager implements IColorDynamicFeatureManager {
    private static final boolean DEBUG = true;
    private static final String PROPERTY_REGION = "persist.sys.oppo.region";
    private static final String TAG = "ColorDynamicFeatureManager";
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static ColorDynamicFeatureManager sInstance = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private final Runnable mClearSystemApp = new Runnable() {
        /* class com.android.server.pm.ColorDynamicFeatureManager.AnonymousClass1 */

        public void run() {
            Slog.d(ColorDynamicFeatureManager.TAG, "Run clearSystemApp");
            Intent intent = new Intent("oppo.intent.action.SWITCH_REGION_CLEAR");
            intent.setPackage("com.coloros.athena");
            ColorDynamicFeatureManager.this.mPms.mContext.startService(intent);
        }
    };
    boolean mDynamicDebug = false;
    ArrayMap<String, FeatureInfo> mOppoAvailableFeatures = null;
    private PackageManagerService mPms = null;
    private IColorPackageManagerServiceEx mPmsEx = null;

    public static ColorDynamicFeatureManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorDynamicFeatureManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorDynamicFeatureManager();
                }
            }
        }
        return sInstance;
    }

    private ColorDynamicFeatureManager() {
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

    public boolean loadRegionFeature(String name) {
        Slog.d(TAG, "loadRegionFeature::name = " + name);
        String callerName = this.mPms.getNameForUid(Binder.getCallingUid());
        Slog.d(TAG, "loadRegionFeature callerName = " + callerName);
        if (callerName != null && !callerName.contains("android.uid.system")) {
            synchronized (this.mPms.mPackages) {
                PackageParser.Package pkg = (PackageParser.Package) this.mPms.mPackages.get(callerName);
                if (pkg != null && !pkg.applicationInfo.isSystemApp()) {
                    Slog.d(TAG, "this is not system app : " + callerName);
                    return false;
                }
            }
        }
        SystemConfig systemConfig = SystemConfig.getInstance();
        synchronized (this.mPms.mPackages) {
            OppoBaseSystemConfig baseSystemConfig = typeCasting(systemConfig);
            ArrayMap<String, FeatureInfo> tmp = baseSystemConfig != null ? baseSystemConfig.loadOppoAvailableFeatures(name) : null;
            if (tmp == null || tmp.size() <= 0) {
                return false;
            }
            this.mOppoAvailableFeatures = tmp;
            SystemProperties.set(PROPERTY_REGION, name);
            if (this.mPms.mSystemReady) {
                this.mPms.mHandler.postDelayed(this.mClearSystemApp, 1000);
            }
            return true;
        }
    }

    public FeatureInfo[] getOppoSystemAvailableFeatures() {
        Slog.d(TAG, "getOppoSystemAvailableFeatures");
        synchronized (this.mPms.mPackages) {
            if (this.mOppoAvailableFeatures == null) {
                Slog.d(TAG, "mOppoAvailableFeatures is null");
                return null;
            }
            Slog.d(TAG, "mOppoAvailableFeatures.size() = " + this.mOppoAvailableFeatures.size());
            Collection<FeatureInfo> featSet = this.mOppoAvailableFeatures.values();
            int size = featSet.size();
            if (size <= 0) {
                return null;
            }
            FeatureInfo[] features = new FeatureInfo[(size + 1)];
            featSet.toArray(features);
            FeatureInfo fi = new FeatureInfo();
            fi.reqGlEsVersion = SystemProperties.getInt("ro.opengles.version", 0);
            features[size] = fi;
            return features;
        }
    }

    public boolean hasOppoSystemFeature(String name) {
        ArrayMap<String, FeatureInfo> arrayMap;
        if (TextUtils.isEmpty(name) || (arrayMap = this.mOppoAvailableFeatures) == null || !arrayMap.containsKey(name)) {
            return false;
        }
        return true;
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog####");
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorDynamicFeatureManager.class.getName());
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

    private static OppoBaseSystemConfig typeCasting(SystemConfig systemConfig) {
        if (systemConfig != null) {
            return (OppoBaseSystemConfig) ColorTypeCastingHelper.typeCasting(OppoBaseSystemConfig.class, systemConfig);
        }
        return null;
    }
}
