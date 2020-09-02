package android.app;

import android.annotation.OppoHook;
import android.annotation.UnsupportedAppUsage;
import android.common.OppoFeatureCache;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.content.res.ApkAssets;
import android.content.res.AssetManager;
import android.content.res.CompatResources;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.ResourcesImpl;
import android.content.res.ResourcesKey;
import android.hardware.display.DisplayManagerGlobal;
import android.os.IBinder;
import android.os.Process;
import android.os.Trace;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayAdjustments;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public class ResourcesManager {
    private static final boolean DEBUG = false;
    private static final boolean ENABLE_APK_ASSETS_CACHE = false;
    static final String TAG = "ResourcesManager";
    private static final Predicate<WeakReference<Resources>> sEmptyReferencePredicate = $$Lambda$ResourcesManager$QJ7UiVk_XS90KuXAsIjIEym1DnM.INSTANCE;
    private static ResourcesManager sResourcesManager;
    @UnsupportedAppUsage
    private final WeakHashMap<IBinder, ActivityResources> mActivityResourceReferences = new WeakHashMap<>();
    private final ArrayMap<Pair<Integer, DisplayAdjustments>, WeakReference<Display>> mAdjustedDisplays = new ArrayMap<>();
    private final ArrayMap<ApkKey, WeakReference<ApkAssets>> mCachedApkAssets = new ArrayMap<>();
    private final LruCache<ApkKey, ApkAssets> mLoadedApkAssets = null;
    private CompatibilityInfo mResCompatibilityInfo;
    @UnsupportedAppUsage
    private final Configuration mResConfiguration = new Configuration();
    @UnsupportedAppUsage
    private final ArrayMap<ResourcesKey, WeakReference<ResourcesImpl>> mResourceImpls = new ArrayMap<>();
    @UnsupportedAppUsage
    private final ArrayList<WeakReference<Resources>> mResourceReferences = new ArrayList<>();

    static /* synthetic */ boolean lambda$static$0(WeakReference weakRef) {
        return weakRef == null || weakRef.get() == null;
    }

    private static class ApkKey {
        public final boolean overlay;
        public final String path;
        public final boolean sharedLib;

        ApkKey(String path2, boolean sharedLib2, boolean overlay2) {
            this.path = path2;
            this.sharedLib = sharedLib2;
            this.overlay = overlay2;
        }

        public int hashCode() {
            return (((((1 * 31) + this.path.hashCode()) * 31) + Boolean.hashCode(this.sharedLib)) * 31) + Boolean.hashCode(this.overlay);
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof ApkKey)) {
                return false;
            }
            ApkKey other = (ApkKey) obj;
            if (this.path.equals(other.path) && this.sharedLib == other.sharedLib && this.overlay == other.overlay) {
                return true;
            }
            return false;
        }
    }

    private static class ActivityResources {
        public final ArrayList<WeakReference<Resources>> activityResources;
        public final Configuration overrideConfig;

        private ActivityResources() {
            this.overrideConfig = new Configuration();
            this.activityResources = new ArrayList<>();
        }
    }

    @UnsupportedAppUsage
    public static ResourcesManager getInstance() {
        ResourcesManager resourcesManager;
        synchronized (ResourcesManager.class) {
            if (sResourcesManager == null) {
                sResourcesManager = new ResourcesManager();
            }
            resourcesManager = sResourcesManager;
        }
        return resourcesManager;
    }

    public void invalidatePath(String path) {
        synchronized (this) {
            int count = 0;
            int i = 0;
            while (i < this.mResourceImpls.size()) {
                ResourcesKey key = this.mResourceImpls.keyAt(i);
                if (key.isPathReferenced(path)) {
                    cleanupResourceImpl(key);
                    count++;
                } else {
                    i++;
                }
            }
            Log.i(TAG, "Invalidated " + count + " asset managers that referenced " + path);
        }
    }

    public Configuration getConfiguration() {
        Configuration configuration;
        synchronized (this) {
            configuration = this.mResConfiguration;
        }
        return configuration;
    }

    /* access modifiers changed from: package-private */
    public DisplayMetrics getDisplayMetrics() {
        return getDisplayMetrics(0, DisplayAdjustments.DEFAULT_DISPLAY_ADJUSTMENTS);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public DisplayMetrics getDisplayMetrics(int displayId, DisplayAdjustments da) {
        DisplayMetrics dm = new DisplayMetrics();
        Display display = getAdjustedDisplay(displayId, da);
        if (display != null) {
            display.getMetrics(dm);
        } else {
            dm.setToDefaults();
        }
        return dm;
    }

    private static void applyNonDefaultDisplayMetricsToConfiguration(DisplayMetrics dm, Configuration config) {
        config.touchscreen = 1;
        config.densityDpi = dm.densityDpi;
        config.screenWidthDp = (int) (((float) dm.widthPixels) / dm.density);
        config.screenHeightDp = (int) (((float) dm.heightPixels) / dm.density);
        int sl = Configuration.resetScreenLayout(config.screenLayout);
        if (dm.widthPixels > dm.heightPixels) {
            config.orientation = 2;
            config.screenLayout = Configuration.reduceScreenLayout(sl, config.screenWidthDp, config.screenHeightDp);
        } else {
            config.orientation = 1;
            config.screenLayout = Configuration.reduceScreenLayout(sl, config.screenHeightDp, config.screenWidthDp);
        }
        config.smallestScreenWidthDp = Math.min(config.screenWidthDp, config.screenHeightDp);
        config.compatScreenWidthDp = config.screenWidthDp;
        config.compatScreenHeightDp = config.screenHeightDp;
        config.compatSmallestScreenWidthDp = config.smallestScreenWidthDp;
    }

    public boolean applyCompatConfigurationLocked(int displayDensity, Configuration compatConfiguration) {
        CompatibilityInfo compatibilityInfo = this.mResCompatibilityInfo;
        if (compatibilityInfo == null || compatibilityInfo.supportsScreen()) {
            return false;
        }
        this.mResCompatibilityInfo.applyToConfiguration(displayDensity, compatConfiguration);
        return true;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.hardware.display.DisplayManagerGlobal.getCompatibleDisplay(int, android.view.DisplayAdjustments):android.view.Display
     arg types: [int, S]
     candidates:
      android.hardware.display.DisplayManagerGlobal.getCompatibleDisplay(int, android.content.res.Resources):android.view.Display
      android.hardware.display.DisplayManagerGlobal.getCompatibleDisplay(int, android.view.DisplayAdjustments):android.view.Display */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0049, code lost:
        return r4;
     */
    private Display getAdjustedDisplay(int displayId, DisplayAdjustments displayAdjustments) {
        Display display;
        Pair<Integer, DisplayAdjustments> key = Pair.create(Integer.valueOf(displayId), displayAdjustments != null ? new DisplayAdjustments(displayAdjustments) : new DisplayAdjustments());
        synchronized (this) {
            WeakReference<Display> wd = this.mAdjustedDisplays.get(key);
            if (wd != null && (display = wd.get()) != null) {
                return display;
            }
            DisplayManagerGlobal dm = DisplayManagerGlobal.getInstance();
            if (dm == null) {
                return null;
            }
            Display display2 = dm.getCompatibleDisplay(displayId, (DisplayAdjustments) key.second);
            if (display2 != null) {
                this.mAdjustedDisplays.put(key, new WeakReference<>(display2));
            }
        }
    }

    public Display getAdjustedDisplay(int displayId, Resources resources) {
        synchronized (this) {
            DisplayManagerGlobal dm = DisplayManagerGlobal.getInstance();
            if (dm == null) {
                return null;
            }
            Display compatibleDisplay = dm.getCompatibleDisplay(displayId, resources);
            return compatibleDisplay;
        }
    }

    private void cleanupResourceImpl(ResourcesKey removedKey) {
        ResourcesImpl res = this.mResourceImpls.remove(removedKey).get();
        if (res != null) {
            res.flushLayoutCache();
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.lang.String.replace(char, char):java.lang.String}
     arg types: [int, int]
     candidates:
      ClspMth{java.lang.String.replace(java.lang.CharSequence, java.lang.CharSequence):java.lang.String}
      ClspMth{java.lang.String.replace(char, char):java.lang.String} */
    private static String overlayPathToIdmapPath(String path) {
        return "/data/resource-cache/" + path.substring(1).replace('/', '@') + "@idmap";
    }

    private ApkAssets loadApkAssets(String path, boolean sharedLib, boolean overlay) throws IOException {
        ApkAssets apkAssets;
        ApkAssets apkAssets2;
        ApkKey newKey = new ApkKey(path, sharedLib, overlay);
        LruCache<ApkKey, ApkAssets> lruCache = this.mLoadedApkAssets;
        if (lruCache != null && (apkAssets2 = lruCache.get(newKey)) != null) {
            return apkAssets2;
        }
        WeakReference<ApkAssets> apkAssetsRef = this.mCachedApkAssets.get(newKey);
        if (apkAssetsRef != null) {
            ApkAssets apkAssets3 = apkAssetsRef.get();
            if (apkAssets3 != null) {
                LruCache<ApkKey, ApkAssets> lruCache2 = this.mLoadedApkAssets;
                if (lruCache2 != null) {
                    lruCache2.put(newKey, apkAssets3);
                }
                return apkAssets3;
            }
            this.mCachedApkAssets.remove(newKey);
        }
        if (overlay) {
            apkAssets = ApkAssets.loadOverlayFromPath(overlayPathToIdmapPath(path), false);
        } else {
            apkAssets = ApkAssets.loadFromPath(path, false, sharedLib);
        }
        LruCache<ApkKey, ApkAssets> lruCache3 = this.mLoadedApkAssets;
        if (lruCache3 != null) {
            lruCache3.put(newKey, apkAssets);
        }
        this.mCachedApkAssets.put(newKey, new WeakReference<>(apkAssets));
        return apkAssets;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    @VisibleForTesting
    public AssetManager createAssetManager(ResourcesKey key) {
        AssetManager.Builder builder = new AssetManager.Builder();
        if (key.mResDir != null) {
            try {
                builder.addApkAssets(loadApkAssets(key.mResDir, false, false));
            } catch (IOException e) {
                Log.e(TAG, "failed to add asset path " + key.mResDir);
                return null;
            }
        }
        if (key.mSplitResDirs != null) {
            String[] strArr = key.mSplitResDirs;
            int length = strArr.length;
            int i = 0;
            while (i < length) {
                String splitResDir = strArr[i];
                try {
                    builder.addApkAssets(loadApkAssets(splitResDir, false, false));
                    i++;
                } catch (IOException e2) {
                    Log.e(TAG, "failed to add split asset path " + splitResDir);
                    return null;
                }
            }
        }
        if (key.mOverlayDirs != null) {
            String[] strArr2 = key.mOverlayDirs;
            for (String idmapPath : strArr2) {
                try {
                    builder.addApkAssets(loadApkAssets(idmapPath, false, true));
                } catch (IOException e3) {
                    Log.w(TAG, "failed to add overlay path " + idmapPath);
                }
            }
        }
        if (key.mLibDirs != null) {
            String[] strArr3 = key.mLibDirs;
            for (String libDir : strArr3) {
                if (libDir.endsWith(PackageParser.APK_FILE_EXTENSION)) {
                    try {
                        builder.addApkAssets(loadApkAssets(libDir, true, false));
                    } catch (IOException e4) {
                        Log.w(TAG, "Asset path '" + libDir + "' does not exist or contains no resources.");
                    }
                }
            }
        }
        return builder.build();
    }

    private static <T> int countLiveReferences(Collection<WeakReference<T>> collection) {
        int count = 0;
        Iterator<WeakReference<T>> it = collection.iterator();
        while (it.hasNext()) {
            WeakReference<T> ref = it.next();
            if ((ref != null ? ref.get() : null) != null) {
                count++;
            }
        }
        return count;
    }

    public void dump(String prefix, PrintWriter printWriter) {
        synchronized (this) {
            IndentingPrintWriter pw = new IndentingPrintWriter(printWriter, "  ");
            for (int i = 0; i < prefix.length() / 2; i++) {
                pw.increaseIndent();
            }
            pw.println("ResourcesManager:");
            pw.increaseIndent();
            if (this.mLoadedApkAssets != null) {
                pw.print("cached apks: total=");
                pw.print(this.mLoadedApkAssets.size());
                pw.print(" created=");
                pw.print(this.mLoadedApkAssets.createCount());
                pw.print(" evicted=");
                pw.print(this.mLoadedApkAssets.evictionCount());
                pw.print(" hit=");
                pw.print(this.mLoadedApkAssets.hitCount());
                pw.print(" miss=");
                pw.print(this.mLoadedApkAssets.missCount());
                pw.print(" max=");
                pw.print(this.mLoadedApkAssets.maxSize());
            } else {
                pw.print("cached apks: 0 [cache disabled]");
            }
            pw.println();
            pw.print("total apks: ");
            pw.println(countLiveReferences(this.mCachedApkAssets.values()));
            pw.print("resources: ");
            int references = countLiveReferences(this.mResourceReferences);
            for (ActivityResources activityResources : this.mActivityResourceReferences.values()) {
                references += countLiveReferences(activityResources.activityResources);
            }
            pw.println(references);
            pw.print("resource impls: ");
            pw.println(countLiveReferences(this.mResourceImpls.values()));
        }
    }

    private Configuration generateConfig(ResourcesKey key, DisplayMetrics dm) {
        boolean isDefaultDisplay = key.mDisplayId == 0;
        boolean hasOverrideConfig = key.hasOverrideConfiguration();
        if (isDefaultDisplay && !hasOverrideConfig) {
            return getConfiguration();
        }
        Configuration config = new Configuration(getConfiguration());
        if (!isDefaultDisplay) {
            applyNonDefaultDisplayMetricsToConfiguration(dm, config);
        }
        if (!hasOverrideConfig) {
            return config;
        }
        config.updateFrom(key.mOverrideConfiguration);
        return config;
    }

    private ResourcesImpl createResourcesImpl(ResourcesKey key) {
        DisplayAdjustments daj = new DisplayAdjustments(key.mOverrideConfiguration);
        daj.setCompatibilityInfo(key.mCompatInfo);
        AssetManager assets = createAssetManager(key);
        if (assets == null) {
            return null;
        }
        DisplayMetrics dm = getDisplayMetrics(key.mDisplayId, daj);
        return new ResourcesImpl(assets, dm, generateConfig(key, dm), daj);
    }

    private ResourcesImpl findResourcesImplForKeyLocked(ResourcesKey key) {
        WeakReference<ResourcesImpl> weakImplRef = this.mResourceImpls.get(key);
        ResourcesImpl impl = weakImplRef != null ? weakImplRef.get() : null;
        if (impl == null || !impl.getAssets().isUpToDate()) {
            return null;
        }
        return impl;
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "HongWu.Yang@ROM.SDK : Add for OPPO theme", property = OppoHook.OppoRomType.ROM)
    private ResourcesImpl findOrCreateResourcesImplForKeyLocked(String packageName, ResourcesKey key) {
        ResourcesImpl impl = findResourcesImplForKeyLocked(key);
        if (impl == null && (impl = createResourcesImpl(packageName, key)) != null) {
            this.mResourceImpls.put(key, new WeakReference<>(impl));
        }
        return impl;
    }

    private ResourcesKey findKeyForResourceImplLocked(ResourcesImpl resourceImpl) {
        int refCount = this.mResourceImpls.size();
        int i = 0;
        while (true) {
            ResourcesImpl impl = null;
            if (i >= refCount) {
                return null;
            }
            WeakReference<ResourcesImpl> weakImplRef = this.mResourceImpls.valueAt(i);
            if (weakImplRef != null) {
                impl = weakImplRef.get();
            }
            if (impl != null && resourceImpl == impl) {
                return this.mResourceImpls.keyAt(i);
            }
            i++;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0018, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0034, code lost:
        return r1;
     */
    public boolean isSameResourcesOverrideConfig(IBinder activityToken, Configuration overrideConfig) {
        ActivityResources activityResources;
        synchronized (this) {
            if (activityToken != null) {
                try {
                    activityResources = this.mActivityResourceReferences.get(activityToken);
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                activityResources = null;
            }
            boolean z = true;
            if (activityResources == null) {
                if (overrideConfig != null) {
                    z = false;
                }
            } else if (!Objects.equals(activityResources.overrideConfig, overrideConfig)) {
                if (overrideConfig == null || activityResources.overrideConfig == null || overrideConfig.diffPublicOnly(activityResources.overrideConfig) != 0) {
                    z = false;
                }
            }
        }
    }

    private ActivityResources getOrCreateActivityResourcesStructLocked(IBinder activityToken) {
        ActivityResources activityResources = this.mActivityResourceReferences.get(activityToken);
        if (activityResources != null) {
            return activityResources;
        }
        ActivityResources activityResources2 = new ActivityResources();
        this.mActivityResourceReferences.put(activityToken, activityResources2);
        return activityResources2;
    }

    private Resources getOrCreateResourcesForActivityLocked(IBinder activityToken, ClassLoader classLoader, ResourcesImpl impl, CompatibilityInfo compatInfo) {
        Resources resources;
        ActivityResources activityResources = getOrCreateActivityResourcesStructLocked(activityToken);
        int refCount = activityResources.activityResources.size();
        for (int i = 0; i < refCount; i++) {
            Resources resources2 = activityResources.activityResources.get(i).get();
            if (resources2 != null && Objects.equals(resources2.getClassLoader(), classLoader) && resources2.getImpl() == impl) {
                return resources2;
            }
        }
        if (compatInfo.needsCompatResources()) {
            resources = new CompatResources(classLoader);
        } else {
            resources = new Resources(classLoader);
        }
        resources.setImpl(impl);
        activityResources.activityResources.add(new WeakReference<>(resources));
        return resources;
    }

    private Resources getOrCreateResourcesLocked(ClassLoader classLoader, ResourcesImpl impl, CompatibilityInfo compatInfo) {
        Resources resources;
        int refCount = this.mResourceReferences.size();
        for (int i = 0; i < refCount; i++) {
            Resources resources2 = this.mResourceReferences.get(i).get();
            if (resources2 != null && Objects.equals(resources2.getClassLoader(), classLoader) && resources2.getImpl() == impl) {
                return resources2;
            }
        }
        if (compatInfo.needsCompatResources()) {
            resources = new CompatResources(classLoader);
        } else {
            resources = new Resources(classLoader);
        }
        resources.setImpl(impl);
        this.mResourceReferences.add(new WeakReference<>(resources));
        return resources;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x004c, code lost:
        r0 = th;
     */
    public Resources createBaseActivityResources(IBinder activityToken, String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo, ClassLoader classLoader) {
        try {
            Trace.traceBegin(8192, "ResourcesManager#createBaseActivityResources");
            ResourcesKey key = new ResourcesKey(resDir, splitResDirs, overlayDirs, libDirs, displayId, overrideConfig != null ? new Configuration(overrideConfig) : null, compatInfo);
            ClassLoader classLoader2 = classLoader != null ? classLoader : ClassLoader.getSystemClassLoader();
            try {
                synchronized (this) {
                    getOrCreateActivityResourcesStructLocked(activityToken);
                }
                while (true) {
                }
            } catch (Throwable th) {
                th = th;
                Trace.traceEnd(8192);
                throw th;
            }
            try {
                updateResourcesForActivity(activityToken, overrideConfig, displayId, false);
                Resources orCreateResources = getOrCreateResources(activityToken, key, classLoader2);
                Trace.traceEnd(8192);
                return orCreateResources;
            } catch (Throwable th2) {
                th = th2;
                Trace.traceEnd(8192);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            Trace.traceEnd(8192);
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0077, code lost:
        return r1;
     */
    private Resources getOrCreateResources(IBinder activityToken, ResourcesKey key, ClassLoader classLoader) {
        Resources resources;
        synchronized (this) {
            if (activityToken != null) {
                ActivityResources activityResources = getOrCreateActivityResourcesStructLocked(activityToken);
                ArrayUtils.unstableRemoveIf(activityResources.activityResources, sEmptyReferencePredicate);
                if (key.hasOverrideConfiguration() && !activityResources.overrideConfig.equals(Configuration.EMPTY)) {
                    Configuration temp = new Configuration(activityResources.overrideConfig);
                    temp.updateFrom(key.mOverrideConfiguration);
                    key.mOverrideConfiguration.setTo(temp);
                }
                ResourcesImpl resourcesImpl = findResourcesImplForKeyLocked(key);
                if (resourcesImpl != null) {
                    Resources orCreateResourcesForActivityLocked = getOrCreateResourcesForActivityLocked(activityToken, classLoader, resourcesImpl, key.mCompatInfo);
                    return orCreateResourcesForActivityLocked;
                }
            } else {
                ArrayUtils.unstableRemoveIf(this.mResourceReferences, sEmptyReferencePredicate);
                ResourcesImpl resourcesImpl2 = findResourcesImplForKeyLocked(key);
                if (resourcesImpl2 != null) {
                    Resources orCreateResourcesLocked = getOrCreateResourcesLocked(classLoader, resourcesImpl2, key.mCompatInfo);
                    return orCreateResourcesLocked;
                }
            }
            ResourcesImpl resourcesImpl3 = createResourcesImpl(key);
            if (resourcesImpl3 == null) {
                return null;
            }
            this.mResourceImpls.put(key, new WeakReference<>(resourcesImpl3));
            if (activityToken != null) {
                resources = getOrCreateResourcesForActivityLocked(activityToken, classLoader, resourcesImpl3, key.mCompatInfo);
            } else {
                resources = getOrCreateResourcesLocked(classLoader, resourcesImpl3, key.mCompatInfo);
            }
        }
    }

    public Resources getResources(IBinder activityToken, String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo, ClassLoader classLoader) {
        try {
            Trace.traceBegin(8192, "ResourcesManager#getResources");
            try {
                Resources orCreateResources = getOrCreateResources(activityToken, new ResourcesKey(resDir, splitResDirs, overlayDirs, libDirs, displayId, overrideConfig != null ? new Configuration(overrideConfig) : null, compatInfo), classLoader != null ? classLoader : ClassLoader.getSystemClassLoader());
                Trace.traceEnd(8192);
                return orCreateResources;
            } catch (Throwable th) {
                th = th;
                Trace.traceEnd(8192);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            Trace.traceEnd(8192);
            throw th;
        }
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "HongWu.Yang@ROM.SDK : Add for OPPO theme", property = OppoHook.OppoRomType.ROM)
    public void updateResourcesForActivity(IBinder activityToken, Configuration overrideConfig, int displayId, boolean movedToDifferentDisplay) {
        updateResourcesForActivity(null, activityToken, overrideConfig, displayId, movedToDifferentDisplay);
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "HongWu.Yang@ROM.SDK : Add for OPPO theme", property = OppoHook.OppoRomType.ROM)
    public void updateResourcesForActivity(String packageName, IBinder activityToken, Configuration overrideConfig, int displayId, boolean movedToDifferentDisplay) {
        ActivityResources activityResources;
        try {
            Trace.traceBegin(8192, "ResourcesManager#updateResourcesForActivity");
            synchronized (this) {
                try {
                    ActivityResources activityResources2 = getOrCreateActivityResourcesStructLocked(activityToken);
                    if (!Objects.equals(activityResources2.overrideConfig, overrideConfig) || movedToDifferentDisplay) {
                        Configuration oldConfig = new Configuration(activityResources2.overrideConfig);
                        if (overrideConfig != null) {
                            activityResources2.overrideConfig.setTo(overrideConfig);
                        } else {
                            activityResources2.overrideConfig.unset();
                        }
                        boolean activityHasOverrideConfig = !activityResources2.overrideConfig.equals(Configuration.EMPTY);
                        int refCount = activityResources2.activityResources.size();
                        int i = 0;
                        while (i < refCount) {
                            Resources resources = activityResources2.activityResources.get(i).get();
                            if (resources == null) {
                                activityResources = activityResources2;
                            } else {
                                ResourcesKey oldKey = findKeyForResourceImplLocked(resources.getImpl());
                                if (oldKey == null) {
                                    Slog.e(TAG, "can't find ResourcesKey for resources impl=" + resources.getImpl());
                                    activityResources = activityResources2;
                                } else {
                                    Configuration rebasedOverrideConfig = new Configuration();
                                    if (overrideConfig != null) {
                                        rebasedOverrideConfig.setTo(overrideConfig);
                                    }
                                    if (activityHasOverrideConfig && oldKey.hasOverrideConfiguration()) {
                                        rebasedOverrideConfig.updateFrom(Configuration.generateDelta(oldConfig, oldKey.mOverrideConfiguration));
                                    }
                                    activityResources = activityResources2;
                                    ResourcesKey newKey = new ResourcesKey(oldKey.mResDir, oldKey.mSplitResDirs, oldKey.mOverlayDirs, oldKey.mLibDirs, displayId, rebasedOverrideConfig, oldKey.mCompatInfo);
                                    ResourcesImpl resourcesImpl = findResourcesImplForKeyLocked(newKey);
                                    if (resourcesImpl == null) {
                                        try {
                                            resourcesImpl = createResourcesImpl(packageName, newKey);
                                            if (resourcesImpl != null) {
                                                this.mResourceImpls.put(newKey, new WeakReference<>(resourcesImpl));
                                            }
                                        } catch (Throwable th) {
                                            th = th;
                                            try {
                                                throw th;
                                            } catch (Throwable th2) {
                                                th = th2;
                                            }
                                        }
                                    }
                                    if (!(resourcesImpl == null || resourcesImpl == resources.getImpl())) {
                                        resources.setImpl(resourcesImpl);
                                    }
                                }
                            }
                            i++;
                            activityResources2 = activityResources;
                        }
                        Trace.traceEnd(8192);
                        return;
                    }
                    Trace.traceEnd(8192);
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        } catch (Throwable th4) {
            th = th4;
            Trace.traceEnd(8192);
            throw th;
        }
    }

    public final boolean applyConfigurationToResourcesLocked(Configuration config, CompatibilityInfo compat) {
        DisplayAdjustments daj;
        try {
            Trace.traceBegin(8192, "ResourcesManager#applyConfigurationToResourcesLocked");
            if (this.mResConfiguration.isOtherSeqNewer(config) || compat != null) {
                int changes = this.mResConfiguration.updateFrom(config);
                this.mAdjustedDisplays.clear();
                DisplayMetrics defaultDisplayMetrics = getDisplayMetrics();
                if (compat != null && (this.mResCompatibilityInfo == null || !this.mResCompatibilityInfo.equals(compat))) {
                    this.mResCompatibilityInfo = compat;
                    changes |= 3328;
                }
                ((IColorCommonInjector) OppoFeatureCache.getOrCreate(IColorCommonInjector.DEFAULT, new Object[0])).applyConfigurationToResourcesForResourcesManager(config, changes);
                Resources.updateSystemConfiguration(config, defaultDisplayMetrics, compat);
                ApplicationPackageManager.configurationChanged();
                Configuration tmpConfig = null;
                boolean z = true;
                int i = this.mResourceImpls.size() - 1;
                while (i >= 0) {
                    ResourcesKey key = this.mResourceImpls.keyAt(i);
                    WeakReference<ResourcesImpl> weakImplRef = this.mResourceImpls.valueAt(i);
                    ResourcesImpl r = weakImplRef != null ? weakImplRef.get() : null;
                    if (r != null) {
                        if (ActivityThread.DEBUG_CONFIGURATION) {
                            Slog.v(TAG, "Changing resources " + r + " config to: " + config);
                        }
                        int displayId = key.mDisplayId;
                        boolean isDefaultDisplay = displayId == 0 ? z : false;
                        boolean hasOverrideConfiguration = key.hasOverrideConfiguration();
                        if (isDefaultDisplay) {
                            if (!hasOverrideConfiguration) {
                                r.updateConfiguration(config, defaultDisplayMetrics, compat);
                            }
                        }
                        if (tmpConfig == null) {
                            tmpConfig = new Configuration();
                        }
                        tmpConfig.setTo(config);
                        DisplayAdjustments daj2 = r.getDisplayAdjustments();
                        if (compat != null) {
                            daj = new DisplayAdjustments(daj2);
                            daj.setCompatibilityInfo(compat);
                        } else {
                            daj = daj2;
                        }
                        DisplayMetrics dm = getDisplayMetrics(displayId, daj);
                        if (!isDefaultDisplay) {
                            applyNonDefaultDisplayMetricsToConfiguration(dm, tmpConfig);
                        }
                        if (hasOverrideConfiguration) {
                            tmpConfig.updateFrom(key.mOverrideConfiguration);
                        }
                        r.updateConfiguration(tmpConfig, dm, compat);
                    } else {
                        this.mResourceImpls.removeAt(i);
                    }
                    i--;
                    z = true;
                }
                return changes != 0;
            }
            if (ActivityThread.DEBUG_CONFIGURATION) {
                Slog.v(TAG, "Skipping new config: curSeq=" + this.mResConfiguration.seq + ", newSeq=" + config.seq);
            }
            Trace.traceEnd(8192);
            return false;
        } finally {
            Trace.traceEnd(8192);
        }
    }

    @UnsupportedAppUsage
    public void appendLibAssetForMainAssetPath(String assetPath, String libAsset) {
        appendLibAssetsForMainAssetPath(null, assetPath, new String[]{libAsset});
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "HongWu.Yang@ROM.SDK : Add for OPPO theme", property = OppoHook.OppoRomType.ROM)
    public void appendLibAssetsForMainAssetPath(String packageName, String assetPath, String[] libAssets) {
        String[] strArr = libAssets;
        synchronized (this) {
            try {
                ArrayMap<ResourcesImpl, ResourcesKey> updatedResourceKeys = new ArrayMap<>();
                int implCount = this.mResourceImpls.size();
                int i = 0;
                while (i < implCount) {
                    try {
                        ResourcesKey key = this.mResourceImpls.keyAt(i);
                        WeakReference<ResourcesImpl> weakImplRef = this.mResourceImpls.valueAt(i);
                        ResourcesImpl impl = weakImplRef != null ? weakImplRef.get() : null;
                        if (impl != null) {
                            try {
                                if (Objects.equals(key.mResDir, assetPath)) {
                                    String[] newLibAssets = key.mLibDirs;
                                    for (String libAsset : strArr) {
                                        newLibAssets = (String[]) ArrayUtils.appendElement(String.class, newLibAssets, libAsset);
                                    }
                                    if (newLibAssets != key.mLibDirs) {
                                        updatedResourceKeys.put(impl, new ResourcesKey(key.mResDir, key.mSplitResDirs, key.mOverlayDirs, newLibAssets, key.mDisplayId, key.mOverrideConfiguration, key.mCompatInfo));
                                    }
                                }
                            } catch (Throwable th) {
                                th = th;
                                throw th;
                            }
                        }
                        i++;
                        strArr = libAssets;
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
                redirectResourcesToNewImplLocked(packageName, updatedResourceKeys);
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void applyNewResourceDirsLocked(ApplicationInfo appInfo, String[] oldPaths) {
        String[] newSplitDirs;
        String baseCodePath;
        int i;
        int implCount;
        try {
            Trace.traceBegin(8192, "ResourcesManager#applyNewResourceDirsLocked");
            String baseCodePath2 = appInfo.getBaseCodePath();
            if (appInfo.uid == Process.myUid()) {
                newSplitDirs = appInfo.splitSourceDirs;
            } else {
                newSplitDirs = appInfo.splitPublicSourceDirs;
            }
            String[] copiedSplitDirs = (String[]) ArrayUtils.cloneOrNull(newSplitDirs);
            String[] copiedResourceDirs = (String[]) ArrayUtils.cloneOrNull(appInfo.resourceDirs);
            ArrayMap<ResourcesImpl, ResourcesKey> updatedResourceKeys = new ArrayMap<>();
            int implCount2 = this.mResourceImpls.size();
            int i2 = 0;
            while (i2 < implCount2) {
                ResourcesKey key = this.mResourceImpls.keyAt(i2);
                WeakReference<ResourcesImpl> weakImplRef = this.mResourceImpls.valueAt(i2);
                ResourcesImpl impl = weakImplRef != null ? weakImplRef.get() : null;
                if (impl == null) {
                    baseCodePath = baseCodePath2;
                    i = i2;
                    implCount = implCount2;
                } else {
                    if (key.mResDir != null) {
                        try {
                            if (!key.mResDir.equals(baseCodePath2)) {
                                if (!ArrayUtils.contains(oldPaths, key.mResDir)) {
                                    baseCodePath = baseCodePath2;
                                    i = i2;
                                    implCount = implCount2;
                                }
                            }
                        } catch (Throwable th) {
                            th = th;
                            Trace.traceEnd(8192);
                            throw th;
                        }
                    }
                    baseCodePath = baseCodePath2;
                    i = i2;
                    implCount = implCount2;
                    updatedResourceKeys.put(impl, new ResourcesKey(baseCodePath2, copiedSplitDirs, copiedResourceDirs, key.mLibDirs, key.mDisplayId, key.mOverrideConfiguration, key.mCompatInfo));
                }
                i2 = i + 1;
                implCount2 = implCount;
                baseCodePath2 = baseCodePath;
            }
            redirectResourcesToNewImplLocked(updatedResourceKeys);
            Trace.traceEnd(8192);
        } catch (Throwable th2) {
            th = th2;
            Trace.traceEnd(8192);
            throw th;
        }
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "HongWu.Yang@ROM.SDK : Add for OPPO theme", property = OppoHook.OppoRomType.ROM)
    private void redirectResourcesToNewImplLocked(ArrayMap<ResourcesImpl, ResourcesKey> updatedResourceKeys) {
        redirectResourcesToNewImplLocked(null, updatedResourceKeys);
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "HongWu.Yang@ROM.SDK : Add for OPPO theme", property = OppoHook.OppoRomType.ROM)
    private void redirectResourcesToNewImplLocked(String packageName, ArrayMap<ResourcesImpl, ResourcesKey> updatedResourceKeys) {
        ResourcesKey key;
        ResourcesKey key2;
        if (!updatedResourceKeys.isEmpty()) {
            int resourcesCount = this.mResourceReferences.size();
            int i = 0;
            while (true) {
                Resources r = null;
                if (i < resourcesCount) {
                    WeakReference<Resources> ref = this.mResourceReferences.get(i);
                    if (ref != null) {
                        r = ref.get();
                    }
                    if (!(r == null || (key2 = updatedResourceKeys.get(r.getImpl())) == null)) {
                        ResourcesImpl impl = findOrCreateResourcesImplForKeyLocked(packageName, key2);
                        if (impl != null) {
                            r.setImpl(impl);
                        } else {
                            throw new Resources.NotFoundException("failed to redirect ResourcesImpl");
                        }
                    }
                    i++;
                } else {
                    for (ActivityResources activityResources : this.mActivityResourceReferences.values()) {
                        int resCount = activityResources.activityResources.size();
                        int i2 = 0;
                        while (true) {
                            if (i2 < resCount) {
                                WeakReference<Resources> ref2 = activityResources.activityResources.get(i2);
                                Resources r2 = ref2 != null ? ref2.get() : null;
                                if (!(r2 == null || (key = updatedResourceKeys.get(r2.getImpl())) == null)) {
                                    ResourcesImpl impl2 = findOrCreateResourcesImplForKeyLocked(packageName, key);
                                    if (impl2 != null) {
                                        r2.setImpl(impl2);
                                    } else {
                                        throw new Resources.NotFoundException("failed to redirect ResourcesImpl");
                                    }
                                }
                                i2++;
                            }
                        }
                    }
                    return;
                }
            }
        }
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK : Add for rom theme", property = OppoHook.OppoRomType.ROM)
    public Resources createBaseActivityResources(String packageName, IBinder activityToken, String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo, ClassLoader classLoader) {
        Resources r = createBaseActivityResources(activityToken, resDir, splitResDirs, overlayDirs, libDirs, displayId, overrideConfig, compatInfo, classLoader);
        if (r != null) {
            r.init(packageName);
        }
        return r;
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK : Add for rom theme", property = OppoHook.OppoRomType.ROM)
    public Resources getResources(String packageName, IBinder activityToken, String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo, ClassLoader classLoader) {
        Resources r = getResources(activityToken, resDir, splitResDirs, overlayDirs, libDirs, displayId, overrideConfig, compatInfo, classLoader);
        if (r != null) {
            r.init(packageName);
        }
        return r;
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "HongWu.Yang@ROM.SDK : Add for OPPO theme", property = OppoHook.OppoRomType.ROM)
    private ResourcesImpl createResourcesImpl(String packageName, ResourcesKey key) {
        ResourcesImpl impl = createResourcesImpl(key);
        if (impl != null && !TextUtils.isEmpty(packageName)) {
            impl.init(packageName);
        }
        return impl;
    }
}
