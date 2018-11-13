package android.app;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.res.AssetManager;
import android.content.res.CompatResources;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.ResourcesImpl;
import android.content.res.ResourcesKey;
import android.hardware.display.DisplayManagerGlobal;
import android.os.IBinder;
import android.os.Trace;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayAdjustments;
import com.android.internal.util.ArrayUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import oppo.content.res.OppoFontUtils;

public class ResourcesManager {
    private static final boolean DEBUG = false;
    static final String TAG = "ResourcesManager";
    private static final Predicate<WeakReference<Resources>> sEmptyReferencePredicate = new Predicate<WeakReference<Resources>>() {
        public boolean test(WeakReference<Resources> weakRef) {
            return weakRef == null || weakRef.get() == null;
        }
    };
    private static ResourcesManager sResourcesManager;
    private final WeakHashMap<IBinder, ActivityResources> mActivityResourceReferences = new WeakHashMap();
    private final ArrayMap<Pair<Integer, DisplayAdjustments>, WeakReference<Display>> mAdjustedDisplays = new ArrayMap();
    private CompatibilityInfo mResCompatibilityInfo;
    private final Configuration mResConfiguration = new Configuration();
    private final ArrayMap<ResourcesKey, WeakReference<ResourcesImpl>> mResourceImpls = new ArrayMap();
    private final ArrayList<WeakReference<Resources>> mResourceReferences = new ArrayList();

    private static class ActivityResources {
        public final ArrayList<WeakReference<Resources>> activityResources;
        public final Configuration overrideConfig;

        /* synthetic */ ActivityResources(ActivityResources -this0) {
            this();
        }

        private ActivityResources() {
            this.overrideConfig = new Configuration();
            this.activityResources = new ArrayList();
        }
    }

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
                ResourcesKey key = (ResourcesKey) this.mResourceImpls.keyAt(i);
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

    DisplayMetrics getDisplayMetrics() {
        return getDisplayMetrics(0, DisplayAdjustments.DEFAULT_DISPLAY_ADJUSTMENTS);
    }

    protected DisplayMetrics getDisplayMetrics(int displayId, DisplayAdjustments da) {
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
        config.smallestScreenWidthDp = config.screenWidthDp;
        config.compatScreenWidthDp = config.screenWidthDp;
        config.compatScreenHeightDp = config.screenHeightDp;
        config.compatSmallestScreenWidthDp = config.smallestScreenWidthDp;
    }

    public boolean applyCompatConfigurationLocked(int displayDensity, Configuration compatConfiguration) {
        if (this.mResCompatibilityInfo == null || (this.mResCompatibilityInfo.supportsScreen() ^ 1) == 0) {
            return false;
        }
        this.mResCompatibilityInfo.applyToConfiguration(displayDensity, compatConfiguration);
        return true;
    }

    /* JADX WARNING: Missing block: B:23:0x0048, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Display getAdjustedDisplay(int displayId, DisplayAdjustments displayAdjustments) {
        DisplayAdjustments displayAdjustmentsCopy;
        if (displayAdjustments != null) {
            displayAdjustmentsCopy = new DisplayAdjustments(displayAdjustments);
        } else {
            displayAdjustmentsCopy = new DisplayAdjustments();
        }
        Pair<Integer, DisplayAdjustments> key = Pair.create(Integer.valueOf(displayId), displayAdjustmentsCopy);
        synchronized (this) {
            Display display;
            WeakReference<Display> wd = (WeakReference) this.mAdjustedDisplays.get(key);
            if (wd != null) {
                display = (Display) wd.get();
                if (display != null) {
                    return display;
                }
            }
            DisplayManagerGlobal dm = DisplayManagerGlobal.getInstance();
            if (dm == null) {
                return null;
            }
            display = dm.getCompatibleDisplay(displayId, (DisplayAdjustments) key.second);
            if (display != null) {
                this.mAdjustedDisplays.put(key, new WeakReference(display));
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
        ResourcesImpl res = (ResourcesImpl) ((WeakReference) this.mResourceImpls.remove(removedKey)).get();
        if (res != null) {
            res.flushLayoutCache();
        }
    }

    protected AssetManager createAssetManager(ResourcesKey key) {
        int i = 0;
        AssetManager assets = new AssetManager();
        if (key.mResDir == null || assets.addAssetPath(key.mResDir) != 0) {
            if (key.mSplitResDirs != null) {
                for (String splitResDir : key.mSplitResDirs) {
                    if (assets.addAssetPath(splitResDir) == 0) {
                        Log.e(TAG, "failed to add split asset path " + splitResDir);
                        return null;
                    }
                }
            }
            if (key.mOverlayDirs != null) {
                for (String idmapPath : key.mOverlayDirs) {
                    assets.addOverlayPath(idmapPath);
                }
            }
            if (key.mLibDirs != null) {
                String[] strArr = key.mLibDirs;
                int length = strArr.length;
                while (i < length) {
                    String libDir = strArr[i];
                    if (libDir.endsWith(".apk") && assets.addAssetPathAsSharedLibrary(libDir) == 0) {
                        Log.w(TAG, "Asset path '" + libDir + "' does not exist or contains no resources.");
                    }
                    i++;
                }
            }
            return assets;
        }
        Log.e(TAG, "failed to add asset path " + key.mResDir);
        return null;
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
        WeakReference<ResourcesImpl> weakImplRef = (WeakReference) this.mResourceImpls.get(key);
        ResourcesImpl impl = weakImplRef != null ? (ResourcesImpl) weakImplRef.get() : null;
        if (impl == null || !impl.getAssets().isUpToDate()) {
            return null;
        }
        return impl;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "wurun.zhou@Plf.Launcher : Modify for rom theme.", property = OppoRomType.ROM)
    private ResourcesImpl findOrCreateResourcesImplForKeyLocked(ResourcesKey key) {
        return findOrCreateResourcesImplForKeyLocked(null, key);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "wurun.zhou@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    private ResourcesImpl findOrCreateResourcesImplForKeyLocked(String packageName, ResourcesKey key) {
        ResourcesImpl impl = findResourcesImplForKeyLocked(key);
        if (impl == null) {
            impl = createResourcesImpl(key);
            if (impl != null) {
                if (!TextUtils.isEmpty(packageName)) {
                    impl.init(packageName);
                }
                this.mResourceImpls.put(key, new WeakReference(impl));
            }
        }
        return impl;
    }

    private ResourcesKey findKeyForResourceImplLocked(ResourcesImpl resourceImpl) {
        int refCount = this.mResourceImpls.size();
        for (int i = 0; i < refCount; i++) {
            WeakReference<ResourcesImpl> weakImplRef = (WeakReference) this.mResourceImpls.valueAt(i);
            ResourcesImpl impl = weakImplRef != null ? (ResourcesImpl) weakImplRef.get() : null;
            if (impl != null && resourceImpl == impl) {
                return (ResourcesKey) this.mResourceImpls.keyAt(i);
            }
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:8:0x0012, code:
            return r1;
     */
    /* JADX WARNING: Missing block: B:20:0x002e, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean isSameResourcesOverrideConfig(IBinder activityToken, Configuration overrideConfig) {
        boolean z = true;
        synchronized (this) {
            ActivityResources activityResources = activityToken != null ? (ActivityResources) this.mActivityResourceReferences.get(activityToken) : null;
            if (activityResources == null) {
                if (overrideConfig != null) {
                    z = false;
                }
            } else if (!Objects.equals(activityResources.overrideConfig, overrideConfig)) {
                if (overrideConfig == null || activityResources.overrideConfig == null) {
                    z = false;
                } else if (overrideConfig.diffPublicOnly(activityResources.overrideConfig) != 0) {
                    z = false;
                }
            }
        }
    }

    private ActivityResources getOrCreateActivityResourcesStructLocked(IBinder activityToken) {
        ActivityResources activityResources = (ActivityResources) this.mActivityResourceReferences.get(activityToken);
        if (activityResources != null) {
            return activityResources;
        }
        activityResources = new ActivityResources();
        this.mActivityResourceReferences.put(activityToken, activityResources);
        return activityResources;
    }

    private Resources getOrCreateResourcesForActivityLocked(IBinder activityToken, ClassLoader classLoader, ResourcesImpl impl, CompatibilityInfo compatInfo) {
        Resources resources;
        ActivityResources activityResources = getOrCreateActivityResourcesStructLocked(activityToken);
        int refCount = activityResources.activityResources.size();
        for (int i = 0; i < refCount; i++) {
            resources = (Resources) ((WeakReference) activityResources.activityResources.get(i)).get();
            if (resources != null && Objects.equals(resources.getClassLoader(), classLoader) && resources.getImpl() == impl) {
                return resources;
            }
        }
        if (compatInfo.needsCompatResources()) {
            resources = new CompatResources(classLoader);
        } else {
            resources = new Resources(classLoader);
        }
        resources.setImpl(impl);
        activityResources.activityResources.add(new WeakReference(resources));
        return resources;
    }

    private Resources getOrCreateResourcesLocked(ClassLoader classLoader, ResourcesImpl impl, CompatibilityInfo compatInfo) {
        Resources resources;
        int refCount = this.mResourceReferences.size();
        for (int i = 0; i < refCount; i++) {
            resources = (Resources) ((WeakReference) this.mResourceReferences.get(i)).get();
            if (resources != null && Objects.equals(resources.getClassLoader(), classLoader) && resources.getImpl() == impl) {
                return resources;
            }
        }
        if (compatInfo.needsCompatResources()) {
            resources = new CompatResources(classLoader);
        } else {
            resources = new Resources(classLoader);
        }
        resources.setImpl(impl);
        this.mResourceReferences.add(new WeakReference(resources));
        return resources;
    }

    public Resources createBaseActivityResources(IBinder activityToken, String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo, ClassLoader classLoader) {
        try {
            Configuration configuration;
            Trace.traceBegin(8192, "ResourcesManager#createBaseActivityResources");
            if (overrideConfig != null) {
                configuration = new Configuration(overrideConfig);
            } else {
                configuration = null;
            }
            ResourcesKey key = new ResourcesKey(resDir, splitResDirs, overlayDirs, libDirs, displayId, configuration, compatInfo);
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            synchronized (this) {
                getOrCreateActivityResourcesStructLocked(activityToken);
            }
            updateResourcesForActivity(activityToken, overrideConfig, displayId, false);
            Resources orCreateResources = getOrCreateResources(activityToken, key, classLoader);
            return orCreateResources;
        } finally {
            Trace.traceEnd(8192);
        }
    }

    /* JADX WARNING: Missing block: B:21:0x0056, code:
            r3 = createResourcesImpl(r10);
     */
    /* JADX WARNING: Missing block: B:22:0x005a, code:
            if (r3 != null) goto L_0x0060;
     */
    /* JADX WARNING: Missing block: B:23:0x005c, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:27:0x0060, code:
            monitor-enter(r8);
     */
    /* JADX WARNING: Missing block: B:29:?, code:
            r1 = findResourcesImplForKeyLocked(r10);
     */
    /* JADX WARNING: Missing block: B:30:0x0065, code:
            if (r1 == null) goto L_0x0079;
     */
    /* JADX WARNING: Missing block: B:31:0x0067, code:
            r3.getAssets().close();
            r3 = r1;
     */
    /* JADX WARNING: Missing block: B:32:0x006f, code:
            if (r9 == null) goto L_0x0087;
     */
    /* JADX WARNING: Missing block: B:33:0x0071, code:
            r2 = getOrCreateResourcesForActivityLocked(r9, r11, r3, r10.mCompatInfo);
     */
    /* JADX WARNING: Missing block: B:34:0x0077, code:
            monitor-exit(r8);
     */
    /* JADX WARNING: Missing block: B:35:0x0078, code:
            return r2;
     */
    /* JADX WARNING: Missing block: B:37:?, code:
            r8.mResourceImpls.put(r10, new java.lang.ref.WeakReference(r3));
     */
    /* JADX WARNING: Missing block: B:42:?, code:
            r2 = getOrCreateResourcesLocked(r11, r3, r10.mCompatInfo);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Resources getOrCreateResources(IBinder activityToken, ResourcesKey key, ClassLoader classLoader) {
        synchronized (this) {
            ResourcesImpl resourcesImpl;
            Resources orCreateResourcesForActivityLocked;
            if (activityToken != null) {
                ActivityResources activityResources = getOrCreateActivityResourcesStructLocked(activityToken);
                ArrayUtils.unstableRemoveIf(activityResources.activityResources, sEmptyReferencePredicate);
                if (key.hasOverrideConfiguration() && (activityResources.overrideConfig.equals(Configuration.EMPTY) ^ 1) != 0) {
                    Configuration temp = new Configuration(activityResources.overrideConfig);
                    temp.updateFrom(key.mOverrideConfiguration);
                    key.mOverrideConfiguration.setTo(temp);
                }
                resourcesImpl = findResourcesImplForKeyLocked(key);
                if (resourcesImpl != null) {
                    orCreateResourcesForActivityLocked = getOrCreateResourcesForActivityLocked(activityToken, classLoader, resourcesImpl, key.mCompatInfo);
                    return orCreateResourcesForActivityLocked;
                }
            }
            ArrayUtils.unstableRemoveIf(this.mResourceReferences, sEmptyReferencePredicate);
            resourcesImpl = findResourcesImplForKeyLocked(key);
            if (resourcesImpl != null) {
                orCreateResourcesForActivityLocked = getOrCreateResourcesLocked(classLoader, resourcesImpl, key.mCompatInfo);
                return orCreateResourcesForActivityLocked;
            }
        }
    }

    public Resources getResources(IBinder activityToken, String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo, ClassLoader classLoader) {
        try {
            Configuration configuration;
            Trace.traceBegin(8192, "ResourcesManager#getResources");
            if (overrideConfig != null) {
                configuration = new Configuration(overrideConfig);
            } else {
                configuration = null;
            }
            ResourcesKey key = new ResourcesKey(resDir, splitResDirs, overlayDirs, libDirs, displayId, configuration, compatInfo);
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            Resources orCreateResources = getOrCreateResources(activityToken, key, classLoader);
            return orCreateResources;
        } finally {
            Trace.traceEnd(8192);
        }
    }

    public void updateResourcesForActivity(IBinder activityToken, Configuration overrideConfig, int displayId, boolean movedToDifferentDisplay) {
        try {
            Trace.traceBegin(8192, "ResourcesManager#updateResourcesForActivity");
            synchronized (this) {
                ActivityResources activityResources = getOrCreateActivityResourcesStructLocked(activityToken);
                if (!Objects.equals(activityResources.overrideConfig, overrideConfig) || (movedToDifferentDisplay ^ 1) == 0) {
                    Configuration oldConfig = new Configuration(activityResources.overrideConfig);
                    if (overrideConfig != null) {
                        activityResources.overrideConfig.setTo(overrideConfig);
                    } else {
                        activityResources.overrideConfig.unset();
                    }
                    boolean activityHasOverrideConfig = activityResources.overrideConfig.equals(Configuration.EMPTY) ^ 1;
                    int refCount = activityResources.activityResources.size();
                    for (int i = 0; i < refCount; i++) {
                        Resources resources = (Resources) ((WeakReference) activityResources.activityResources.get(i)).get();
                        if (resources != null) {
                            ResourcesKey oldKey = findKeyForResourceImplLocked(resources.getImpl());
                            if (oldKey == null) {
                                Slog.e(TAG, "can't find ResourcesKey for resources impl=" + resources.getImpl());
                            } else {
                                Configuration rebasedOverrideConfig = new Configuration();
                                if (overrideConfig != null) {
                                    rebasedOverrideConfig.setTo(overrideConfig);
                                }
                                if (activityHasOverrideConfig && oldKey.hasOverrideConfiguration()) {
                                    rebasedOverrideConfig.updateFrom(Configuration.generateDelta(oldConfig, oldKey.mOverrideConfiguration));
                                }
                                ResourcesKey newKey = new ResourcesKey(oldKey.mResDir, oldKey.mSplitResDirs, oldKey.mOverlayDirs, oldKey.mLibDirs, displayId, rebasedOverrideConfig, oldKey.mCompatInfo);
                                ResourcesImpl resourcesImpl = findResourcesImplForKeyLocked(newKey);
                                if (resourcesImpl == null) {
                                    resourcesImpl = createResourcesImpl(newKey);
                                    if (resourcesImpl != null) {
                                        this.mResourceImpls.put(newKey, new WeakReference(resourcesImpl));
                                    }
                                }
                                if (!(resourcesImpl == null || resourcesImpl == resources.getImpl())) {
                                    resources.setImpl(resourcesImpl);
                                }
                            }
                        }
                    }
                    Trace.traceEnd(8192);
                }
            }
        } finally {
            Trace.traceEnd(8192);
        }
    }

    public final boolean applyConfigurationToResourcesLocked(Configuration config, CompatibilityInfo compat) {
        try {
            Trace.traceBegin(8192, "ResourcesManager#applyConfigurationToResourcesLocked");
            if (this.mResConfiguration.isOtherSeqNewer(config) || compat != null) {
                int changes = this.mResConfiguration.updateFrom(config);
                this.mAdjustedDisplays.clear();
                DisplayMetrics defaultDisplayMetrics = getDisplayMetrics();
                if (compat != null && (this.mResCompatibilityInfo == null || (this.mResCompatibilityInfo.equals(compat) ^ 1) != 0)) {
                    this.mResCompatibilityInfo = compat;
                    changes |= 3328;
                }
                OppoThemeHelper.handleExtraConfigurationChanges(changes);
                if ((536870912 & changes) != 0) {
                    OppoFontUtils.SetFlipFont(config);
                }
                Resources.updateSystemConfiguration(config, defaultDisplayMetrics, compat);
                ApplicationPackageManager.configurationChanged();
                Configuration tmpConfig = null;
                for (int i = this.mResourceImpls.size() - 1; i >= 0; i--) {
                    ResourcesKey key = (ResourcesKey) this.mResourceImpls.keyAt(i);
                    WeakReference<ResourcesImpl> weakImplRef = (WeakReference) this.mResourceImpls.valueAt(i);
                    ResourcesImpl r = weakImplRef != null ? (ResourcesImpl) weakImplRef.get() : null;
                    if (r != null) {
                        if (ActivityThread.DEBUG_CONFIGURATION) {
                            Slog.v(TAG, "Changing resources " + r + " config to: " + config);
                        }
                        int displayId = key.mDisplayId;
                        boolean isDefaultDisplay = displayId == 0;
                        DisplayMetrics dm = defaultDisplayMetrics;
                        boolean hasOverrideConfiguration = key.hasOverrideConfiguration();
                        if (!isDefaultDisplay || hasOverrideConfiguration) {
                            if (tmpConfig == null) {
                                tmpConfig = new Configuration();
                            }
                            tmpConfig.setTo(config);
                            DisplayAdjustments daj = r.getDisplayAdjustments();
                            if (compat != null) {
                                DisplayAdjustments daj2 = new DisplayAdjustments(daj);
                                daj2.setCompatibilityInfo(compat);
                                daj = daj2;
                            }
                            dm = getDisplayMetrics(displayId, daj);
                            if (!isDefaultDisplay) {
                                applyNonDefaultDisplayMetricsToConfiguration(dm, tmpConfig);
                            }
                            if (hasOverrideConfiguration) {
                                tmpConfig.updateFrom(key.mOverrideConfiguration);
                            }
                            r.updateConfiguration(tmpConfig, dm, compat);
                        } else {
                            r.updateConfiguration(config, defaultDisplayMetrics, compat);
                        }
                    } else {
                        this.mResourceImpls.removeAt(i);
                    }
                }
                boolean z = changes != 0;
                Trace.traceEnd(8192);
                return z;
            }
            if (ActivityThread.DEBUG_CONFIGURATION) {
                Slog.v(TAG, "Skipping new config: curSeq=" + this.mResConfiguration.seq + ", newSeq=" + config.seq);
            }
            Trace.traceEnd(8192);
            return false;
        } catch (Throwable th) {
            Trace.traceEnd(8192);
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "wurun.zhou@Plf.Launcher : Modify for OPPO theme", property = OppoRomType.ROM)
    public void appendLibAssetForMainAssetPath(String assetPath, String libAsset) {
        appendLibAssetForMainAssetPath(null, assetPath, libAsset);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "wurun.zhou@Plf.Launcher : Add for OPPO theme", property = OppoRomType.ROM)
    public void appendLibAssetForMainAssetPath(String packageName, String assetPath, String libAsset) {
        synchronized (this) {
            ArrayMap<ResourcesImpl, ResourcesKey> updatedResourceKeys = new ArrayMap();
            int implCount = this.mResourceImpls.size();
            for (int i = 0; i < implCount; i++) {
                ResourcesKey key = (ResourcesKey) this.mResourceImpls.keyAt(i);
                WeakReference<ResourcesImpl> weakImplRef = (WeakReference) this.mResourceImpls.valueAt(i);
                Object impl = weakImplRef != null ? (ResourcesImpl) weakImplRef.get() : null;
                if (!(impl == null || !Objects.equals(key.mResDir, assetPath) || ArrayUtils.contains(key.mLibDirs, libAsset))) {
                    int newLibAssetCount = (key.mLibDirs != null ? key.mLibDirs.length : 0) + 1;
                    String[] newLibAssets = new String[newLibAssetCount];
                    if (key.mLibDirs != null) {
                        System.arraycopy(key.mLibDirs, 0, newLibAssets, 0, key.mLibDirs.length);
                    }
                    newLibAssets[newLibAssetCount - 1] = libAsset;
                    updatedResourceKeys.put(impl, new ResourcesKey(key.mResDir, key.mSplitResDirs, key.mOverlayDirs, newLibAssets, key.mDisplayId, key.mOverrideConfiguration, key.mCompatInfo));
                }
            }
            redirectResourcesToNewImplLocked(updatedResourceKeys);
        }
    }

    final void applyNewResourceDirsLocked(String baseCodePath, String[] newResourceDirs) {
        try {
            Trace.traceBegin(8192, "ResourcesManager#applyNewResourceDirsLocked");
            ArrayMap<ResourcesImpl, ResourcesKey> updatedResourceKeys = new ArrayMap();
            int implCount = this.mResourceImpls.size();
            for (int i = 0; i < implCount; i++) {
                ResourcesKey key = (ResourcesKey) this.mResourceImpls.keyAt(i);
                WeakReference<ResourcesImpl> weakImplRef = (WeakReference) this.mResourceImpls.valueAt(i);
                Object impl = weakImplRef != null ? (ResourcesImpl) weakImplRef.get() : null;
                if (impl != null && (key.mResDir == null || key.mResDir.equals(baseCodePath))) {
                    updatedResourceKeys.put(impl, new ResourcesKey(key.mResDir, key.mSplitResDirs, newResourceDirs, key.mLibDirs, key.mDisplayId, key.mOverrideConfiguration, key.mCompatInfo));
                }
            }
            redirectResourcesToNewImplLocked(updatedResourceKeys);
        } finally {
            Trace.traceEnd(8192);
        }
    }

    private void redirectResourcesToNewImplLocked(ArrayMap<ResourcesImpl, ResourcesKey> updatedResourceKeys) {
        if (!updatedResourceKeys.isEmpty()) {
            int i;
            WeakReference<Resources> ref;
            Resources r;
            ResourcesKey key;
            ResourcesImpl impl;
            int resourcesCount = this.mResourceReferences.size();
            for (i = 0; i < resourcesCount; i++) {
                ref = (WeakReference) this.mResourceReferences.get(i);
                r = ref != null ? (Resources) ref.get() : null;
                if (r != null) {
                    key = (ResourcesKey) updatedResourceKeys.get(r.getImpl());
                    if (key != null) {
                        impl = findOrCreateResourcesImplForKeyLocked(key);
                        if (impl == null) {
                            throw new NotFoundException("failed to redirect ResourcesImpl");
                        }
                        r.setImpl(impl);
                    } else {
                        continue;
                    }
                }
            }
            for (ActivityResources activityResources : this.mActivityResourceReferences.values()) {
                int resCount = activityResources.activityResources.size();
                for (i = 0; i < resCount; i++) {
                    ref = (WeakReference) activityResources.activityResources.get(i);
                    r = ref != null ? (Resources) ref.get() : null;
                    if (r != null) {
                        key = (ResourcesKey) updatedResourceKeys.get(r.getImpl());
                        if (key != null) {
                            impl = findOrCreateResourcesImplForKeyLocked(key);
                            if (impl == null) {
                                throw new NotFoundException("failed to redirect ResourcesImpl");
                            }
                            r.setImpl(impl);
                        }
                    }
                }
            }
        }
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    public Resources createBaseActivityResources(String packageName, IBinder activityToken, String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo, ClassLoader classLoader) {
        Resources r = createBaseActivityResources(activityToken, resDir, splitResDirs, overlayDirs, libDirs, displayId, overrideConfig, compatInfo, classLoader);
        if (r != null) {
            r.init(packageName);
        }
        return r;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    public Resources getResources(String packageName, IBinder activityToken, String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo, ClassLoader classLoader) {
        Resources r = getResources(activityToken, resDir, splitResDirs, overlayDirs, libDirs, displayId, overrideConfig, compatInfo, classLoader);
        if (r != null) {
            r.init(packageName);
        }
        return r;
    }
}
