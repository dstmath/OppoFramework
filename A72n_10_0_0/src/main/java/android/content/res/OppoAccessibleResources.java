package android.content.res;

import android.content.res.OppoThemeZipFile;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import oppo.content.res.OppoExtraConfiguration;

public class OppoAccessibleResources {
    private static Map<String, WeakReference<OppoAccessibleResources>> mPackageCaches = new ConcurrentHashMap();
    private OppoAccessibleFile mAccessible = null;
    private boolean mHasDrawables = false;
    private boolean mHasValues = false;
    private String mPackageName = null;
    private ColorBaseResourcesImpl mResources = null;

    public OppoAccessibleResources(ColorBaseResourcesImpl resources, String packageName) {
        this.mPackageName = packageName;
        this.mResources = resources;
        checkUpdate();
    }

    public static OppoAccessibleResources getAccessResources(ColorBaseResourcesImpl resources, String packageName) {
        WeakReference<OppoAccessibleResources> weakReference;
        OppoAccessibleResources mAcessResources = null;
        synchronized (mPackageCaches) {
            if (mPackageCaches.containsKey(packageName) && (weakReference = mPackageCaches.get(packageName)) != null) {
                mAcessResources = weakReference.get();
            }
            if (mAcessResources != null) {
                mAcessResources.setResources(resources);
                mAcessResources.checkUpdate();
            } else {
                mAcessResources = new OppoAccessibleResources(resources, packageName);
                mPackageCaches.put(packageName, new WeakReference<>(mAcessResources));
            }
        }
        return mAcessResources;
    }

    public boolean checkUpdate() {
        if (isAssetEnable()) {
            ColorBaseResourcesImpl colorBaseResourcesImpl = this.mResources;
            if (!OppoBaseFile.rejectNightMode(colorBaseResourcesImpl.typeCasting(colorBaseResourcesImpl))) {
                this.mAccessible = OppoAccessibleFile.getAssetFile(this.mPackageName, this.mResources);
                OppoAccessibleFile oppoAccessibleFile = this.mAccessible;
                if (oppoAccessibleFile != null) {
                    boolean update = oppoAccessibleFile.initValue();
                    this.mHasValues = this.mAccessible.hasValues();
                    this.mHasDrawables = this.mAccessible.hasDrawables();
                    return update;
                }
                this.mHasValues = false;
                this.mHasDrawables = false;
                return false;
            }
        }
        OppoAccessibleFile oppoAccessibleFile2 = this.mAccessible;
        if (oppoAccessibleFile2 != null) {
            oppoAccessibleFile2.clearCache(null);
        }
        this.mHasDrawables = false;
        this.mHasValues = false;
        return false;
    }

    public void setResources(ColorBaseResourcesImpl mResources2) {
        this.mResources = mResources2;
    }

    public boolean hasDrawables() {
        return this.mHasDrawables;
    }

    public boolean hasValues() {
        return this.mHasValues;
    }

    public Integer getAccessibleInt(int id) {
        OppoAccessibleFile oppoAccessibleFile = this.mAccessible;
        if (oppoAccessibleFile == null || !this.mHasValues) {
            return null;
        }
        return oppoAccessibleFile.getInt(id);
    }

    public CharSequence getAccessibleChars(int id) {
        OppoAccessibleFile oppoAccessibleFile = this.mAccessible;
        if (oppoAccessibleFile == null || !this.mHasValues) {
            return null;
        }
        return oppoAccessibleFile.getCharSequence(id);
    }

    public OppoThemeZipFile.ThemeFileInfo getAccessibleStream(int index, String path) {
        OppoAccessibleFile oppoAccessibleFile = this.mAccessible;
        if (oppoAccessibleFile == null || !this.mHasDrawables) {
            return null;
        }
        return oppoAccessibleFile.getAssetInputStream(index, path);
    }

    private boolean isAssetEnable() {
        OppoExtraConfiguration extrConfig = this.mResources.getSystemConfiguration().getOppoExtraConfiguration();
        if (extrConfig != null && extrConfig.mAccessibleChanged > 0) {
            return true;
        }
        return false;
    }
}
