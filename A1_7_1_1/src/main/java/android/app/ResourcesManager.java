package android.app;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.res.AssetManager;
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

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ResourcesManager {
    private static final boolean DEBUG = false;
    static final String TAG = "ResourcesManager";
    private static final Predicate<WeakReference<Resources>> sEmptyReferencePredicate = null;
    private static ResourcesManager sResourcesManager;
    private final WeakHashMap<IBinder, ActivityResources> mActivityResourceReferences;
    private final ArrayMap<Pair<Integer, DisplayAdjustments>, WeakReference<Display>> mDisplays;
    private CompatibilityInfo mResCompatibilityInfo;
    private final Configuration mResConfiguration;
    private final ArrayMap<ResourcesKey, WeakReference<ResourcesImpl>> mResourceImpls;
    private final ArrayList<WeakReference<Resources>> mResourceReferences;

    private static class ActivityResources {
        public final ArrayList<WeakReference<Resources>> activityResources;
        public final Configuration overrideConfig;

        /* synthetic */ ActivityResources(ActivityResources activityResources) {
            this();
        }

        private ActivityResources() {
            this.overrideConfig = new Configuration();
            this.activityResources = new ArrayList();
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ResourcesManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ResourcesManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.ResourcesManager.<clinit>():void");
    }

    public ResourcesManager() {
        this.mResConfiguration = new Configuration();
        this.mResourceImpls = new ArrayMap();
        this.mResourceReferences = new ArrayList();
        this.mActivityResourceReferences = new WeakHashMap();
        this.mDisplays = new ArrayMap();
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
                if (((ResourcesKey) this.mResourceImpls.keyAt(i)).isPathReferenced(path)) {
                    ResourcesImpl res = (ResourcesImpl) ((WeakReference) this.mResourceImpls.removeAt(i)).get();
                    if (res != null) {
                        res.flushLayoutCache();
                    }
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
        if (this.mResCompatibilityInfo == null || this.mResCompatibilityInfo.supportsScreen()) {
            return false;
        }
        this.mResCompatibilityInfo.applyToConfiguration(displayDensity, compatConfiguration);
        return true;
    }

    /* JADX WARNING: Missing block: B:23:0x0048, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Display getAdjustedDisplay(int displayId, DisplayAdjustments displayAdjustments) {
        DisplayAdjustments displayAdjustmentsCopy;
        if (displayAdjustments != null) {
            displayAdjustmentsCopy = new DisplayAdjustments(displayAdjustments);
        } else {
            displayAdjustmentsCopy = new DisplayAdjustments();
        }
        Pair<Integer, DisplayAdjustments> key = Pair.create(Integer.valueOf(displayId), displayAdjustmentsCopy);
        synchronized (this) {
            Display display;
            WeakReference<Display> wd = (WeakReference) this.mDisplays.get(key);
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
                this.mDisplays.put(key, new WeakReference(display));
            }
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
        ResourcesImpl impl;
        WeakReference<ResourcesImpl> weakImplRef = (WeakReference) this.mResourceImpls.get(key);
        if (weakImplRef != null) {
            impl = (ResourcesImpl) weakImplRef.get();
        } else {
            impl = null;
        }
        if (impl == null || !impl.getAssets().isUpToDate()) {
            return null;
        }
        return impl;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "wurun.zhou@Apps.Theme : Modify for rom theme.", property = OppoRomType.ROM)
    private ResourcesImpl findOrCreateResourcesImplForKeyLocked(ResourcesKey key) {
        return findOrCreateResourcesImplForKeyLocked(null, key);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "wurun.zhou@Apps.Theme : Add for rom theme", property = OppoRomType.ROM)
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
            ResourcesImpl impl;
            WeakReference<ResourcesImpl> weakImplRef = (WeakReference) this.mResourceImpls.valueAt(i);
            if (weakImplRef != null) {
                impl = (ResourcesImpl) weakImplRef.get();
            } else {
                impl = null;
            }
            if (impl != null && resourceImpl == impl) {
                return (ResourcesKey) this.mResourceImpls.keyAt(i);
            }
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:8:0x0011, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean isSameResourcesOverrideConfig(IBinder activityToken, Configuration overrideConfig) {
        synchronized (this) {
            ActivityResources activityResources = activityToken != null ? (ActivityResources) this.mActivityResourceReferences.get(activityToken) : null;
            boolean z;
            if (activityResources == null) {
                z = overrideConfig == null;
            } else {
                z = Objects.equals(activityResources.overrideConfig, overrideConfig);
                return z;
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

    private Resources getOrCreateResourcesForActivityLocked(IBinder activityToken, ClassLoader classLoader, ResourcesImpl impl) {
        Resources resources;
        ActivityResources activityResources = getOrCreateActivityResourcesStructLocked(activityToken);
        int refCount = activityResources.activityResources.size();
        for (int i = 0; i < refCount; i++) {
            resources = (Resources) ((WeakReference) activityResources.activityResources.get(i)).get();
            if (resources != null && Objects.equals(resources.getClassLoader(), classLoader) && resources.getImpl() == impl) {
                return resources;
            }
        }
        resources = new Resources(classLoader);
        resources.setImpl(impl);
        activityResources.activityResources.add(new WeakReference(resources));
        return resources;
    }

    private Resources getOrCreateResourcesLocked(ClassLoader classLoader, ResourcesImpl impl) {
        Resources resources;
        int refCount = this.mResourceReferences.size();
        for (int i = 0; i < refCount; i++) {
            resources = (Resources) ((WeakReference) this.mResourceReferences.get(i)).get();
            if (resources != null && Objects.equals(resources.getClassLoader(), classLoader) && resources.getImpl() == impl) {
                return resources;
            }
        }
        resources = new Resources(classLoader);
        resources.setImpl(impl);
        this.mResourceReferences.add(new WeakReference(resources));
        return resources;
    }

    public Resources createBaseActivityResources(IBinder activityToken, String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo, ClassLoader classLoader) {
        try {
            Configuration configuration;
            Trace.traceBegin(32768, "ResourcesManager#createBaseActivityResources");
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
            updateResourcesForActivity(activityToken, overrideConfig);
            Resources orCreateResources = getOrCreateResources(activityToken, key, classLoader);
            return orCreateResources;
        } finally {
            Trace.traceEnd(32768);
        }
    }

    /* JADX WARNING: Missing block: B:25:0x0054, code:
            r3 = createResourcesImpl(r10);
     */
    /* JADX WARNING: Missing block: B:26:0x0058, code:
            if (r3 != null) goto L_0x005b;
     */
    /* JADX WARNING: Missing block: B:27:0x005a, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:28:0x005b, code:
            monitor-enter(r8);
     */
    /* JADX WARNING: Missing block: B:30:?, code:
            r1 = findResourcesImplForKeyLocked(r10);
     */
    /* JADX WARNING: Missing block: B:31:0x0060, code:
            if (r1 == null) goto L_0x0072;
     */
    /* JADX WARNING: Missing block: B:32:0x0062, code:
            r3.getAssets().close();
            r3 = r1;
     */
    /* JADX WARNING: Missing block: B:33:0x006a, code:
            if (r9 == null) goto L_0x0080;
     */
    /* JADX WARNING: Missing block: B:34:0x006c, code:
            r2 = getOrCreateResourcesForActivityLocked(r9, r11, r3);
     */
    /* JADX WARNING: Missing block: B:35:0x0070, code:
            monitor-exit(r8);
     */
    /* JADX WARNING: Missing block: B:36:0x0071, code:
            return r2;
     */
    /* JADX WARNING: Missing block: B:38:?, code:
            r8.mResourceImpls.put(r10, new java.lang.ref.WeakReference(r3));
     */
    /* JADX WARNING: Missing block: B:43:?, code:
            r2 = getOrCreateResourcesLocked(r11, r3);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Resources getOrCreateResources(IBinder activityToken, ResourcesKey key, ClassLoader classLoader) {
        synchronized (this) {
            ResourcesImpl resourcesImpl;
            Resources orCreateResourcesForActivityLocked;
            if (activityToken != null) {
                ActivityResources activityResources = getOrCreateActivityResourcesStructLocked(activityToken);
                ArrayUtils.unstableRemoveIf(activityResources.activityResources, sEmptyReferencePredicate);
                if (key.hasOverrideConfiguration() && !activityResources.overrideConfig.equals(Configuration.EMPTY)) {
                    Configuration temp = new Configuration(activityResources.overrideConfig);
                    temp.updateFrom(key.mOverrideConfiguration);
                    key.mOverrideConfiguration.setTo(temp);
                }
                resourcesImpl = findResourcesImplForKeyLocked(key);
                if (resourcesImpl != null) {
                    orCreateResourcesForActivityLocked = getOrCreateResourcesForActivityLocked(activityToken, classLoader, resourcesImpl);
                    return orCreateResourcesForActivityLocked;
                }
            }
            ArrayUtils.unstableRemoveIf(this.mResourceReferences, sEmptyReferencePredicate);
            resourcesImpl = findResourcesImplForKeyLocked(key);
            if (resourcesImpl != null) {
                orCreateResourcesForActivityLocked = getOrCreateResourcesLocked(classLoader, resourcesImpl);
                return orCreateResourcesForActivityLocked;
            }
        }
    }

    public Resources getResources(IBinder activityToken, String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo, ClassLoader classLoader) {
        try {
            Configuration configuration;
            Trace.traceBegin(32768, "ResourcesManager#getResources");
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
            Trace.traceEnd(32768);
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "wurun.zhou@Apps.Theme : Modify for rom theme", property = OppoRomType.ROM)
    public void updateResourcesForActivity(IBinder activityToken, Configuration overrideConfig) {
        updateResourcesForActivity(null, activityToken, overrideConfig);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "wurun.zhou@Apps.Theme : Add for rom theme", property = OppoRomType.ROM)
    public void updateResourcesForActivity(String packageName, IBinder activityToken, Configuration overrideConfig) {
        try {
            Trace.traceBegin(32768, "ResourcesManager#updateResourcesForActivity");
            synchronized (this) {
                ActivityResources activityResources = getOrCreateActivityResourcesStructLocked(activityToken);
                if (!Objects.equals(activityResources.overrideConfig, overrideConfig)) {
                    Configuration oldConfig = new Configuration(activityResources.overrideConfig);
                    if (overrideConfig != null) {
                        activityResources.overrideConfig.setTo(overrideConfig);
                    } else {
                        activityResources.overrideConfig.setToDefaults();
                    }
                    boolean activityHasOverrideConfig = !activityResources.overrideConfig.equals(Configuration.EMPTY);
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
                                ResourcesKey newKey = new ResourcesKey(oldKey.mResDir, oldKey.mSplitResDirs, oldKey.mOverlayDirs, oldKey.mLibDirs, oldKey.mDisplayId, rebasedOverrideConfig, oldKey.mCompatInfo);
                                ResourcesImpl resourcesImpl = findResourcesImplForKeyLocked(newKey);
                                if (resourcesImpl == null) {
                                    resourcesImpl = createResourcesImpl(newKey);
                                    if (resourcesImpl != null) {
                                        if (!TextUtils.isEmpty(packageName)) {
                                            resourcesImpl.init(packageName);
                                        }
                                        this.mResourceImpls.put(newKey, new WeakReference(resourcesImpl));
                                    }
                                }
                                if (!(resourcesImpl == null || resourcesImpl == resources.getImpl())) {
                                    resources.setImpl(resourcesImpl);
                                }
                            }
                        }
                    }
                    Trace.traceEnd(32768);
                }
            }
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public final boolean applyConfigurationToResourcesLocked(Configuration config, CompatibilityInfo compat) {
        try {
            Trace.traceBegin(32768, "ResourcesManager#applyConfigurationToResourcesLocked");
            if (this.mResConfiguration.isOtherSeqNewer(config) || compat != null) {
                int changes = this.mResConfiguration.updateFrom(config);
                this.mDisplays.clear();
                DisplayMetrics defaultDisplayMetrics = getDisplayMetrics();
                if (compat != null && (this.mResCompatibilityInfo == null || !this.mResCompatibilityInfo.equals(compat))) {
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
                    ResourcesImpl r = (ResourcesImpl) ((WeakReference) this.mResourceImpls.valueAt(i)).get();
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
                Trace.traceEnd(32768);
                return z;
            }
            if (ActivityThread.DEBUG_CONFIGURATION) {
                Slog.v(TAG, "Skipping new config: curSeq=" + this.mResConfiguration.seq + ", newSeq=" + config.seq);
            }
            Trace.traceEnd(32768);
            return false;
        } catch (Throwable th) {
            Trace.traceEnd(32768);
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "wurun.zhou@Apps.Theme : Modify for rom theme", property = OppoRomType.ROM)
    public void appendLibAssetForMainAssetPath(String assetPath, String libAsset) {
        appendLibAssetForMainAssetPath(null, assetPath, libAsset);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "wurun.zhou@Apps.Theme : Add for rom theme", property = OppoRomType.ROM)
    public void appendLibAssetForMainAssetPath(String packageName, String assetPath, String libAsset) {
        synchronized (this) {
            int i;
            ResourcesImpl impl;
            ResourcesKey key;
            ArrayMap<ResourcesImpl, ResourcesKey> updatedResourceKeys = new ArrayMap();
            int implCount = this.mResourceImpls.size();
            for (i = 0; i < implCount; i++) {
                impl = (ResourcesImpl) ((WeakReference) this.mResourceImpls.valueAt(i)).get();
                key = (ResourcesKey) this.mResourceImpls.keyAt(i);
                if (!(impl == null || key.mResDir == null || !key.mResDir.equals(assetPath) || ArrayUtils.contains(key.mLibDirs, libAsset))) {
                    int newLibAssetCount = (key.mLibDirs != null ? key.mLibDirs.length : 0) + 1;
                    String[] newLibAssets = new String[newLibAssetCount];
                    if (key.mLibDirs != null) {
                        System.arraycopy(key.mLibDirs, 0, newLibAssets, 0, key.mLibDirs.length);
                    }
                    newLibAssets[newLibAssetCount - 1] = libAsset;
                    updatedResourceKeys.put(impl, new ResourcesKey(key.mResDir, key.mSplitResDirs, key.mOverlayDirs, newLibAssets, key.mDisplayId, key.mOverrideConfiguration, key.mCompatInfo));
                }
            }
            if (updatedResourceKeys.isEmpty()) {
                return;
            }
            Resources r;
            int resourcesCount = this.mResourceReferences.size();
            for (i = 0; i < resourcesCount; i++) {
                r = (Resources) ((WeakReference) this.mResourceReferences.get(i)).get();
                if (r != null) {
                    key = (ResourcesKey) updatedResourceKeys.get(r.getImpl());
                    if (key != null) {
                        impl = findOrCreateResourcesImplForKeyLocked(packageName, key);
                        if (impl == null) {
                            throw new NotFoundException("failed to load " + libAsset);
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
                    r = (Resources) ((WeakReference) activityResources.activityResources.get(i)).get();
                    if (r != null) {
                        key = (ResourcesKey) updatedResourceKeys.get(r.getImpl());
                        if (key != null) {
                            impl = findOrCreateResourcesImplForKeyLocked(packageName, key);
                            if (impl == null) {
                                throw new NotFoundException("failed to load " + libAsset);
                            }
                            r.setImpl(impl);
                        } else {
                            continue;
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
