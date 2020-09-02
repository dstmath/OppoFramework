package android.content.res;

import android.animation.Animator;
import android.animation.StateListAnimator;
import android.annotation.UnsupportedAppUsage;
import android.common.OppoFeatureCache;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.FontResourcesParser;
import android.content.res.Resources;
import android.content.res.XmlBlock;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ColorStateListDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.icu.text.PluralRules;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Build;
import android.os.LocaleList;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Slog;
import android.util.TypedValue;
import android.util.Xml;
import android.view.DisplayAdjustments;
import com.android.internal.util.GrowingArrayUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import mediatek.content.res.MtkBoostDrawableCache;
import org.xmlpull.v1.XmlPullParserException;

public class ResourcesImpl extends ColorBaseResourcesImpl {
    private static final boolean DEBUG_CONFIG = false;
    private static final boolean DEBUG_LOAD = false;
    private static final int ID_OTHER = 16777220;
    static final String TAG = "Resources";
    static final String TAG_PRELOAD = "Resources.preload";
    public static final boolean TRACE_FOR_DETAILED_PRELOAD = SystemProperties.getBoolean("debug.trace_resource_preload", false);
    @UnsupportedAppUsage
    private static final boolean TRACE_FOR_MISS_PRELOAD = false;
    @UnsupportedAppUsage
    private static final boolean TRACE_FOR_PRELOAD = false;
    private static final int XML_BLOCK_CACHE_SIZE = 4;
    private static int sPreloadTracingNumLoadedDrawables;
    private static boolean sPreloaded;
    @UnsupportedAppUsage
    private static final LongSparseArray<Drawable.ConstantState> sPreloadedColorDrawables = new LongSparseArray<>();
    @UnsupportedAppUsage
    private static final LongSparseArray<ConstantState<ComplexColor>> sPreloadedComplexColors = new LongSparseArray<>();
    @UnsupportedAppUsage
    private static final LongSparseArray<Drawable.ConstantState>[] sPreloadedDrawables = new LongSparseArray[2];
    private static final Object sSync = new Object();
    @UnsupportedAppUsage
    private final Object mAccessLock = new Object();
    @UnsupportedAppUsage
    private final ConfigurationBoundResourceCache<Animator> mAnimatorCache = new ConfigurationBoundResourceCache<>();
    @UnsupportedAppUsage
    final AssetManager mAssets;
    private final int[] mCachedXmlBlockCookies = new int[4];
    private final String[] mCachedXmlBlockFiles = new String[4];
    private final XmlBlock[] mCachedXmlBlocks = new XmlBlock[4];
    @UnsupportedAppUsage
    private final DrawableCache mColorDrawableCache = new DrawableCache();
    private final ConfigurationBoundResourceCache<ComplexColor> mComplexColorCache = new ConfigurationBoundResourceCache<>();
    @UnsupportedAppUsage
    private final Configuration mConfiguration = new Configuration();
    private final DisplayAdjustments mDisplayAdjustments;
    @UnsupportedAppUsage
    private final DrawableCache mDrawableCache = new DrawableCache();
    private int mLastCachedXmlBlockIndex = -1;
    private final ThreadLocal<LookupStack> mLookupStack = ThreadLocal.withInitial($$Lambda$ResourcesImpl$h3PTRX185BeQl8SVC2_w9arp5Og.INSTANCE);
    private final DisplayMetrics mMetrics = new DisplayMetrics();
    private MtkBoostDrawableCache mMtkBoostDrawableCache = new MtkBoostDrawableCache();
    private PluralRules mPluralRule;
    private long mPreloadTracingPreloadStartTime;
    private long mPreloadTracingStartBitmapCount;
    private long mPreloadTracingStartBitmapSize;
    @UnsupportedAppUsage
    private boolean mPreloading;
    @UnsupportedAppUsage
    private final ConfigurationBoundResourceCache<StateListAnimator> mStateListAnimatorCache = new ConfigurationBoundResourceCache<>();
    private final Configuration mTmpConfig = new Configuration();

    static {
        sPreloadedDrawables[0] = new LongSparseArray<>();
        sPreloadedDrawables[1] = new LongSparseArray<>();
    }

    static /* synthetic */ LookupStack lambda$new$0() {
        return new LookupStack();
    }

    @UnsupportedAppUsage
    public ResourcesImpl(AssetManager assets, DisplayMetrics metrics, Configuration config, DisplayAdjustments displayAdjustments) {
        this.mAssets = assets;
        this.mMetrics.setToDefaults();
        this.mDisplayAdjustments = displayAdjustments;
        this.mConfiguration.setToDefaults();
        updateConfiguration(config, metrics, displayAdjustments.getCompatibilityInfo());
        ((IColorThemeManager) OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0])).init(this, null);
    }

    public DisplayAdjustments getDisplayAdjustments() {
        return this.mDisplayAdjustments;
    }

    @UnsupportedAppUsage
    public AssetManager getAssets() {
        return this.mAssets;
    }

    public DisplayMetrics getDisplayMetrics() {
        return this.mMetrics;
    }

    @Override // android.content.res.ColorBaseResourcesImpl
    public Configuration getConfiguration() {
        return this.mConfiguration;
    }

    /* access modifiers changed from: package-private */
    public Configuration[] getSizeConfigurations() {
        return this.mAssets.getSizeConfigurations();
    }

    /* access modifiers changed from: package-private */
    public CompatibilityInfo getCompatibilityInfo() {
        return this.mDisplayAdjustments.getCompatibilityInfo();
    }

    private PluralRules getPluralRule() {
        PluralRules pluralRules;
        synchronized (sSync) {
            if (this.mPluralRule == null) {
                this.mPluralRule = PluralRules.forLocale(this.mConfiguration.getLocales().get(0));
            }
            pluralRules = this.mPluralRule;
        }
        return pluralRules;
    }

    /* access modifiers changed from: package-private */
    @Override // android.content.res.ColorBaseResourcesImpl
    @UnsupportedAppUsage
    public void getValue(int id, TypedValue outValue, boolean resolveRefs) throws Resources.NotFoundException {
        if (this.mAssets.getResourceValue(id, 0, outValue, resolveRefs)) {
            ((IColorThemeManager) OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0])).getValue(this, id, outValue, resolveRefs);
            return;
        }
        throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id));
    }

    /* access modifiers changed from: package-private */
    public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs) throws Resources.NotFoundException {
        if (this.mAssets.getResourceValue(id, density, outValue, resolveRefs)) {
            ((IColorThemeManager) OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0])).getValue(this, id, outValue, resolveRefs);
            return;
        }
        throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id));
    }

    /* access modifiers changed from: package-private */
    public void getValue(String name, TypedValue outValue, boolean resolveRefs) throws Resources.NotFoundException {
        int id = getIdentifier(name, "string", null);
        if (id != 0) {
            getValue(id, outValue, resolveRefs);
            return;
        }
        throw new Resources.NotFoundException("String resource name " + name);
    }

    public int getIdentifier(String name, String defType, String defPackage) {
        if (name != null) {
            try {
                return Integer.parseInt(name);
            } catch (Exception e) {
                return this.mAssets.getResourceIdentifier(name, defType, defPackage);
            }
        } else {
            throw new NullPointerException("name is null");
        }
    }

    /* access modifiers changed from: package-private */
    public String getResourceName(int resid) throws Resources.NotFoundException {
        String str = this.mAssets.getResourceName(resid);
        if (str != null) {
            return str;
        }
        throw new Resources.NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    /* access modifiers changed from: package-private */
    @Override // android.content.res.ColorBaseResourcesImpl
    public String getResourcePackageName(int resid) throws Resources.NotFoundException {
        String str = this.mAssets.getResourcePackageName(resid);
        if (str != null) {
            return str;
        }
        throw new Resources.NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    /* access modifiers changed from: package-private */
    public String getResourceTypeName(int resid) throws Resources.NotFoundException {
        String str = this.mAssets.getResourceTypeName(resid);
        if (str != null) {
            return str;
        }
        throw new Resources.NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    /* access modifiers changed from: package-private */
    public String getResourceEntryName(int resid) throws Resources.NotFoundException {
        String str = this.mAssets.getResourceEntryName(resid);
        if (str != null) {
            return str;
        }
        throw new Resources.NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    /* access modifiers changed from: package-private */
    public String getLastResourceResolution() throws Resources.NotFoundException {
        String str = this.mAssets.getLastResourceResolution();
        if (str != null) {
            return str;
        }
        throw new Resources.NotFoundException("Associated AssetManager hasn't resolved a resource");
    }

    /* access modifiers changed from: package-private */
    public CharSequence getQuantityText(int id, int quantity) throws Resources.NotFoundException {
        PluralRules rule = getPluralRule();
        CharSequence res = this.mAssets.getResourceBagText(id, attrForQuantityCode(rule.select((double) quantity)));
        if (res != null) {
            return res;
        }
        CharSequence res2 = this.mAssets.getResourceBagText(id, ID_OTHER);
        if (res2 != null) {
            return res2;
        }
        throw new Resources.NotFoundException("Plural resource ID #0x" + Integer.toHexString(id) + " quantity=" + quantity + " item=" + rule.select((double) quantity));
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int attrForQuantityCode(String quantityCode) {
        char c;
        switch (quantityCode.hashCode()) {
            case 101272:
                if (quantityCode.equals("few")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 110182:
                if (quantityCode.equals("one")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 115276:
                if (quantityCode.equals("two")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 3343967:
                if (quantityCode.equals("many")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 3735208:
                if (quantityCode.equals("zero")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            return 16777221;
        }
        if (c == 1) {
            return 16777222;
        }
        if (c == 2) {
            return 16777223;
        }
        if (c == 3) {
            return 16777224;
        }
        if (c != 4) {
            return ID_OTHER;
        }
        return 16777225;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.content.res.ResourcesImpl.getValue(int, android.util.TypedValue, boolean):void
     arg types: [int, android.util.TypedValue, int]
     candidates:
      android.content.res.ResourcesImpl.getValue(java.lang.String, android.util.TypedValue, boolean):void
      android.content.res.ResourcesImpl.getValue(int, android.util.TypedValue, boolean):void */
    /* access modifiers changed from: package-private */
    public AssetFileDescriptor openRawResourceFd(int id, TypedValue tempValue) throws Resources.NotFoundException {
        getValue(id, tempValue, true);
        try {
            return this.mAssets.openNonAssetFd(tempValue.assetCookie, tempValue.string.toString());
        } catch (Exception e) {
            throw new Resources.NotFoundException("File " + tempValue.string.toString() + " from drawable resource ID #0x" + Integer.toHexString(id), e);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.content.res.ResourcesImpl.getValue(int, android.util.TypedValue, boolean):void
     arg types: [int, android.util.TypedValue, int]
     candidates:
      android.content.res.ResourcesImpl.getValue(java.lang.String, android.util.TypedValue, boolean):void
      android.content.res.ResourcesImpl.getValue(int, android.util.TypedValue, boolean):void */
    /* access modifiers changed from: package-private */
    public InputStream openRawResource(int id, TypedValue value) throws Resources.NotFoundException {
        getValue(id, value, true);
        InputStream input = ((IColorThemeManager) OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0])).openRawResource(this, id, value);
        if (input != null) {
            return input;
        }
        try {
            return this.mAssets.openNonAsset(value.assetCookie, value.string.toString(), 2);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("File ");
            sb.append(value.string == null ? "(null)" : value.string.toString());
            sb.append(" from drawable resource ID #0x");
            sb.append(Integer.toHexString(id));
            Resources.NotFoundException rnf = new Resources.NotFoundException(sb.toString());
            rnf.initCause(e);
            throw rnf;
        }
    }

    /* access modifiers changed from: package-private */
    public ConfigurationBoundResourceCache<Animator> getAnimatorCache() {
        return this.mAnimatorCache;
    }

    /* access modifiers changed from: package-private */
    public ConfigurationBoundResourceCache<StateListAnimator> getStateListAnimatorCache() {
        return this.mStateListAnimatorCache;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0190, code lost:
        r2 = android.content.res.ResourcesImpl.sSync;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0192, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0195, code lost:
        if (r36.mPluralRule == null) goto L_0x01ad;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:?, code lost:
        r36.mPluralRule = android.icu.text.PluralRules.forLocale(r36.mConfiguration.getLocales().get(0));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x01a9, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x01ad, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x01ae, code lost:
        ((android.content.res.IColorThemeManager) android.common.OppoFeatureCache.getOrCreate(android.content.res.IColorThemeManager.DEFAULT, new java.lang.Object[0])).checkUpdate(r36, r0, false);
        r2 = 8192;
        android.os.Trace.traceEnd(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x01c4, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x01c5, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:?, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x01c9, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x01ca, code lost:
        r0 = th;
     */
    public void updateConfiguration(Configuration config, DisplayMetrics metrics, CompatibilityInfo compat) {
        int configChanges;
        int height;
        int width;
        int keyboardHidden;
        Locale bestLocale;
        Trace.traceBegin(8192, "ResourcesImpl#updateConfiguration");
        try {
            int i = ((IColorThemeManager) OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0])).updateExConfiguration(this, config);
            synchronized (this.mAccessLock) {
                if (compat != null) {
                    try {
                        this.mDisplayAdjustments.setCompatibilityInfo(compat);
                    } catch (Throwable th) {
                        th = th;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        }
                        throw th;
                    }
                }
                if (metrics != null) {
                    this.mMetrics.setTo(metrics);
                }
                try {
                    this.mDisplayAdjustments.getCompatibilityInfo().applyToDisplayMetrics(this.mMetrics);
                    configChanges = calcConfigChanges(config);
                    LocaleList locales = this.mConfiguration.getLocales();
                    if (locales.isEmpty()) {
                        locales = LocaleList.getDefault();
                        this.mConfiguration.setLocales(locales);
                    }
                    if ((configChanges & 4) != 0 && locales.size() > 1) {
                        String[] availableLocales = this.mAssets.getNonSystemLocales();
                        if (LocaleList.isPseudoLocalesOnly(availableLocales)) {
                            availableLocales = this.mAssets.getLocales();
                            if (LocaleList.isPseudoLocalesOnly(availableLocales)) {
                                availableLocales = null;
                            }
                        }
                        if (!(availableLocales == null || (bestLocale = locales.getFirstMatchWithEnglishSupported(availableLocales)) == null || bestLocale == locales.get(0))) {
                            this.mConfiguration.setLocales(new LocaleList(bestLocale, locales));
                        }
                    }
                    if (this.mConfiguration.densityDpi != 0) {
                        this.mMetrics.densityDpi = this.mConfiguration.densityDpi;
                        this.mMetrics.density = ((float) this.mConfiguration.densityDpi) * 0.00625f;
                    }
                    this.mMetrics.scaledDensity = this.mMetrics.density * (this.mConfiguration.fontScale != 0.0f ? this.mConfiguration.fontScale : 1.0f);
                    if (this.mMetrics.widthPixels >= this.mMetrics.heightPixels) {
                        width = this.mMetrics.widthPixels;
                        height = this.mMetrics.heightPixels;
                    } else {
                        width = this.mMetrics.heightPixels;
                        height = this.mMetrics.widthPixels;
                    }
                    if (this.mConfiguration.keyboardHidden == 1 && this.mConfiguration.hardKeyboardHidden == 2) {
                        keyboardHidden = 3;
                    } else {
                        keyboardHidden = this.mConfiguration.keyboardHidden;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    while (true) {
                        break;
                    }
                    throw th;
                }
                try {
                    this.mAssets.setConfiguration(this.mConfiguration.mcc, this.mConfiguration.mnc, adjustLanguageTag(this.mConfiguration.getLocales().get(0).toLanguageTag()), this.mConfiguration.orientation, this.mConfiguration.touchscreen, this.mConfiguration.densityDpi, this.mConfiguration.keyboard, keyboardHidden, this.mConfiguration.navigation, width, height, this.mConfiguration.smallestScreenWidthDp, this.mConfiguration.screenWidthDp, this.mConfiguration.screenHeightDp, this.mConfiguration.screenLayout, this.mConfiguration.uiMode, this.mConfiguration.colorMode, Build.VERSION.RESOURCES_SDK_INT);
                    this.mMtkBoostDrawableCache.onConfigurationChange(configChanges);
                    int newConfigChanges = configChanges | i;
                    this.mDrawableCache.onConfigurationChange(newConfigChanges);
                    this.mColorDrawableCache.onConfigurationChange(newConfigChanges);
                    this.mComplexColorCache.onConfigurationChange(newConfigChanges);
                    this.mAnimatorCache.onConfigurationChange(newConfigChanges);
                    this.mStateListAnimatorCache.onConfigurationChange(newConfigChanges);
                    flushLayoutCache();
                } catch (Throwable th4) {
                    th = th4;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
        } finally {
            Trace.traceEnd(8192);
        }
    }

    public int calcConfigChanges(Configuration config) {
        if (config == null) {
            return -1;
        }
        this.mTmpConfig.setTo(config);
        int density = config.densityDpi;
        if (density == 0) {
            density = this.mMetrics.noncompatDensityDpi;
        }
        this.mDisplayAdjustments.getCompatibilityInfo().applyToConfiguration(density, this.mTmpConfig);
        if (this.mTmpConfig.getLocales().isEmpty()) {
            this.mTmpConfig.setLocales(LocaleList.getDefault());
        }
        return this.mConfiguration.updateFrom(this.mTmpConfig);
    }

    private static String adjustLanguageTag(String languageTag) {
        String remainder;
        String language;
        int separator = languageTag.indexOf(45);
        if (separator == -1) {
            language = languageTag;
            remainder = "";
        } else {
            language = languageTag.substring(0, separator);
            remainder = languageTag.substring(separator);
        }
        return Locale.adjustLanguageCode(language) + remainder;
    }

    public void flushLayoutCache() {
        synchronized (this.mCachedXmlBlocks) {
            Arrays.fill(this.mCachedXmlBlockCookies, 0);
            Arrays.fill(this.mCachedXmlBlockFiles, (Object) null);
            XmlBlock[] cachedXmlBlocks = this.mCachedXmlBlocks;
            for (int i = 0; i < 4; i++) {
                XmlBlock oldBlock = cachedXmlBlocks[i];
                if (oldBlock != null) {
                    oldBlock.close();
                }
            }
            Arrays.fill(cachedXmlBlocks, (Object) null);
        }
    }

    /* access modifiers changed from: package-private */
    public Drawable loadDrawable(Resources wrapper, TypedValue value, int id, int density, Resources.Theme theme) throws Resources.NotFoundException {
        int i;
        String name;
        boolean isColorDrawable;
        DrawableCache caches;
        long key;
        Drawable.ConstantState cs;
        Drawable dr;
        boolean needsNewDrawableAfterCache;
        Drawable.ConstantState state;
        String name2;
        boolean useCache = density == 0 || value.density == this.mMetrics.densityDpi;
        if (density > 0 && value.density > 0 && value.density != 65535) {
            if (value.density == density) {
                value.density = this.mMetrics.densityDpi;
            } else {
                value.density = (value.density * this.mMetrics.densityDpi) / density;
            }
        }
        try {
            if (value.type < 28 || value.type > 31) {
                isColorDrawable = false;
                caches = this.mDrawableCache;
                key = (((long) value.assetCookie) << 32) | ((long) value.data);
            } else {
                isColorDrawable = true;
                caches = this.mColorDrawableCache;
                key = (long) value.data;
            }
            if (!this.mPreloading && useCache) {
                this.mMtkBoostDrawableCache.hbBoost(wrapper, value);
                Drawable cachedDrawable = caches.getInstance(key, wrapper, theme);
                if (cachedDrawable != null) {
                    cachedDrawable.setChangingConfigurations(value.changingConfigurations);
                    return cachedDrawable;
                }
                synchronized (this.mAccessLock) {
                    Drawable boostDrawable = this.mMtkBoostDrawableCache.getBoostCachedDrawable(wrapper, key);
                    if (boostDrawable != null) {
                        Slog.w(TAG, "Using Boost");
                        return boostDrawable;
                    }
                }
            }
            if (isColorDrawable) {
                cs = sPreloadedColorDrawables.get(key);
            } else {
                cs = sPreloadedDrawables[this.mConfiguration.getLayoutDirection()].get(key);
            }
            if (cs != null) {
                if (TRACE_FOR_DETAILED_PRELOAD && (id >>> 24) == 1 && Process.myUid() != 0 && (name2 = getResourceName(id)) != null) {
                    Log.d(TAG_PRELOAD, "Hit preloaded FW drawable #" + Integer.toHexString(id) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + name2);
                }
                dr = cs.newDrawable(wrapper);
            } else if (isColorDrawable) {
                dr = new ColorDrawable(value.data);
            } else {
                dr = loadDrawableForCookie(wrapper, value, id, density);
            }
            if (dr instanceof DrawableContainer) {
                needsNewDrawableAfterCache = true;
            } else {
                needsNewDrawableAfterCache = false;
            }
            boolean canApplyTheme = dr != null && dr.canApplyTheme();
            if (canApplyTheme && theme != null) {
                dr = dr.mutate();
                dr.applyTheme(theme);
                dr.clearMutated();
            }
            if (dr == null) {
                return dr;
            }
            dr.setChangingConfigurations(value.changingConfigurations);
            if (!useCache) {
                return dr;
            }
            i = 0;
            try {
                cacheDrawable(value, isColorDrawable, caches, theme, canApplyTheme, key, dr);
                if (!needsNewDrawableAfterCache || (state = dr.getConstantState()) == null) {
                    return dr;
                }
                return state.newDrawable(wrapper);
            } catch (Exception e) {
                e = e;
                try {
                    name = getResourceName(id);
                } catch (Resources.NotFoundException e2) {
                    name = "(missing name)";
                }
                Resources.NotFoundException nfe = new Resources.NotFoundException("Drawable " + name + " with resource ID #0x" + Integer.toHexString(id), e);
                nfe.setStackTrace(new StackTraceElement[i]);
                throw nfe;
            }
        } catch (Exception e3) {
            e = e3;
            i = 0;
            name = getResourceName(id);
            Resources.NotFoundException nfe2 = new Resources.NotFoundException("Drawable " + name + " with resource ID #0x" + Integer.toHexString(id), e);
            nfe2.setStackTrace(new StackTraceElement[i]);
            throw nfe2;
        }
    }

    private void cacheDrawable(TypedValue value, boolean isColorDrawable, DrawableCache caches, Resources.Theme theme, boolean usesTheme, long key, Drawable dr) {
        Drawable.ConstantState cs = dr.getConstantState();
        if (cs != null) {
            if (this.mPreloading) {
                int changingConfigs = cs.getChangingConfigurations();
                if (isColorDrawable) {
                    if (verifyPreloadConfig(changingConfigs, 0, value.resourceId, "drawable")) {
                        sPreloadedColorDrawables.put(key, cs);
                    }
                } else if (!verifyPreloadConfig(changingConfigs, 8192, value.resourceId, "drawable")) {
                } else {
                    if ((changingConfigs & 8192) == 0) {
                        sPreloadedDrawables[0].put(key, cs);
                        sPreloadedDrawables[1].put(key, cs);
                        return;
                    }
                    sPreloadedDrawables[this.mConfiguration.getLayoutDirection()].put(key, cs);
                }
            } else {
                synchronized (this.mAccessLock) {
                    caches.put(key, theme, cs, usesTheme);
                    if (!isColorDrawable) {
                        this.mMtkBoostDrawableCache.putBoostCache(key, cs);
                    }
                }
            }
        }
    }

    private boolean verifyPreloadConfig(int changingConfigurations, int allowVarying, int resourceId, String name) {
        String resName;
        if ((-1073745921 & changingConfigurations & (~allowVarying)) == 0) {
            return true;
        }
        try {
            resName = getResourceName(resourceId);
        } catch (Resources.NotFoundException e) {
            resName = "?";
        }
        Log.w(TAG, "Preloaded " + name + " resource #0x" + Integer.toHexString(resourceId) + " (" + resName + ") that varies with configuration!!");
        return false;
    }

    private Drawable decodeImageDrawable(AssetManager.AssetInputStream ais, Resources wrapper, TypedValue value) {
        try {
            return ImageDecoder.decodeDrawable(new ImageDecoder.AssetInputStreamSource(ais, wrapper, value), $$Lambda$ResourcesImpl$99dm2ENnzo9b0SIUjUj2Kl3pi90.INSTANCE);
        } catch (IOException e) {
            return null;
        }
    }

    private Drawable loadDrawableForCookie(Resources wrapper, TypedValue value, int id, int density) {
        long startBitmapSize;
        long startTime;
        int startDrawableCount;
        int startBitmapCount;
        long j;
        LookupStack stack;
        boolean isRoot;
        String str;
        if (value.string != null) {
            String file = value.string.toString();
            if (TRACE_FOR_DETAILED_PRELOAD) {
                long startTime2 = System.nanoTime();
                startTime = startTime2;
                startBitmapCount = Bitmap.sPreloadTracingNumInstantiatedBitmaps;
                startBitmapSize = Bitmap.sPreloadTracingTotalBitmapsSize;
                startDrawableCount = sPreloadTracingNumLoadedDrawables;
            } else {
                startTime = 0;
                startBitmapCount = 0;
                startBitmapSize = 0;
                startDrawableCount = 0;
            }
            Trace.traceBegin(8192, file);
            LookupStack stack2 = this.mLookupStack.get();
            try {
                if (!stack2.contains(id)) {
                    stack2.push(id);
                    try {
                        Drawable dr = ((IColorThemeManager) OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0])).loadOverlayDrawable(this, wrapper, value, id);
                        if (dr != null) {
                            isRoot = false;
                            stack = stack2;
                            j = 8192;
                        } else if (!file.endsWith(".xml")) {
                            isRoot = false;
                            stack = stack2;
                            j = 8192;
                            dr = decodeImageDrawable((AssetManager.AssetInputStream) this.mAssets.openNonAsset(value.assetCookie, file, 2), wrapper, value);
                        } else if (file.startsWith("res/color/")) {
                            isRoot = false;
                            stack = stack2;
                            j = 8192;
                            try {
                                dr = loadColorOrXmlDrawable(wrapper, value, id, density, file);
                            } catch (Throwable th) {
                                th = th;
                                try {
                                    stack.pop();
                                    throw th;
                                } catch (Exception | StackOverflowError e) {
                                    e = e;
                                    Trace.traceEnd(j);
                                    Resources.NotFoundException rnf = new Resources.NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id));
                                    rnf.initCause(e);
                                    throw rnf;
                                }
                            }
                        } else {
                            isRoot = false;
                            stack = stack2;
                            j = 8192;
                            dr = loadXmlDrawable(wrapper, value, id, density, file);
                        }
                        stack.pop();
                        Trace.traceEnd(j);
                        if (TRACE_FOR_DETAILED_PRELOAD && (id >>> 24) == 1) {
                            String name = getResourceName(id);
                            if (name != null) {
                                long time = System.nanoTime() - startTime;
                                int loadedBitmapCount = Bitmap.sPreloadTracingNumInstantiatedBitmaps - startBitmapCount;
                                long loadedBitmapSize = Bitmap.sPreloadTracingTotalBitmapsSize - startBitmapSize;
                                int i = sPreloadTracingNumLoadedDrawables;
                                int loadedDrawables = i - startDrawableCount;
                                sPreloadTracingNumLoadedDrawables = i + 1;
                                if (Process.myUid() == 0) {
                                    isRoot = true;
                                }
                                StringBuilder sb = new StringBuilder();
                                if (isRoot) {
                                    str = "Preloaded FW drawable #";
                                } else {
                                    str = "Loaded non-preloaded FW drawable #";
                                }
                                sb.append(str);
                                sb.append(Integer.toHexString(id));
                                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                                sb.append(name);
                                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                                sb.append(file);
                                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                                sb.append(dr.getClass().getCanonicalName());
                                sb.append(" #nested_drawables= ");
                                sb.append(loadedDrawables);
                                sb.append(" #bitmaps= ");
                                sb.append(loadedBitmapCount);
                                sb.append(" total_bitmap_size= ");
                                sb.append(loadedBitmapSize);
                                sb.append(" in[us] ");
                                sb.append(time / 1000);
                                Log.d(TAG_PRELOAD, sb.toString());
                            }
                        }
                        return dr;
                    } catch (Throwable th2) {
                        th = th2;
                        stack = stack2;
                        j = 8192;
                        stack.pop();
                        throw th;
                    }
                } else {
                    throw new Exception("Recursive reference in drawable");
                }
            } catch (Exception | StackOverflowError e2) {
                e = e2;
                j = 8192;
                Trace.traceEnd(j);
                Resources.NotFoundException rnf2 = new Resources.NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id));
                rnf2.initCause(e);
                throw rnf2;
            }
        } else {
            throw new Resources.NotFoundException("Resource \"" + getResourceName(id) + "\" (" + Integer.toHexString(id) + ") is not a Drawable (color or path): " + value);
        }
    }

    private Drawable loadColorOrXmlDrawable(Resources wrapper, TypedValue value, int id, int density, String file) {
        try {
            return new ColorStateListDrawable(loadColorStateList(wrapper, value, id, null));
        } catch (Resources.NotFoundException originalException) {
            try {
                return loadXmlDrawable(wrapper, value, id, density, file);
            } catch (Exception e) {
                throw originalException;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0017, code lost:
        if (r0 != null) goto L_0x0019;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001e, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0021, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0016, code lost:
        r2 = move-exception;
     */
    private Drawable loadXmlDrawable(Resources wrapper, TypedValue value, int id, int density, String file) throws IOException, XmlPullParserException {
        XmlResourceParser rp = loadXmlResourceParser(file, id, value.assetCookie, "drawable");
        Drawable createFromXmlForDensity = Drawable.createFromXmlForDensity(wrapper, rp, density, null);
        if (rp != null) {
            rp.close();
        }
        return createFromXmlForDensity;
    }

    public Typeface loadFont(Resources wrapper, TypedValue value, int id) {
        if (value.string != null) {
            String file = value.string.toString();
            if (!file.startsWith("res/")) {
                return null;
            }
            Typeface cached = Typeface.findFromCache(this.mAssets, file);
            if (cached != null) {
                return cached;
            }
            Trace.traceBegin(8192, file);
            try {
                if (file.endsWith("xml")) {
                    FontResourcesParser.FamilyResourceEntry familyEntry = FontResourcesParser.parse(loadXmlResourceParser(file, id, value.assetCookie, "font"), wrapper);
                    if (familyEntry == null) {
                        Trace.traceEnd(8192);
                        return null;
                    }
                    Typeface createFromResources = Typeface.createFromResources(familyEntry, this.mAssets, file);
                    Trace.traceEnd(8192);
                    return createFromResources;
                }
                Typeface build = new Typeface.Builder(this.mAssets, file, false, value.assetCookie).build();
                Trace.traceEnd(8192);
                return build;
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Failed to parse xml resource " + file, e);
            } catch (IOException e2) {
                Log.e(TAG, "Failed to read xml resource " + file, e2);
            } catch (Throwable th) {
                Trace.traceEnd(8192);
                throw th;
            }
        } else {
            throw new Resources.NotFoundException("Resource \"" + getResourceName(id) + "\" (" + Integer.toHexString(id) + ") is not a Font: " + value);
        }
        Trace.traceEnd(8192);
        return null;
    }

    private ComplexColor loadComplexColorFromName(Resources wrapper, Resources.Theme theme, TypedValue value, int id) {
        long key = (((long) value.assetCookie) << 32) | ((long) value.data);
        ConfigurationBoundResourceCache<ComplexColor> cache = this.mComplexColorCache;
        ComplexColor complexColor = cache.getInstance(key, wrapper, theme);
        if (complexColor != null) {
            return complexColor;
        }
        ConstantState<ComplexColor> factory = sPreloadedComplexColors.get(key);
        if (factory != null) {
            complexColor = factory.newInstance(wrapper, theme);
        }
        if (complexColor == null) {
            complexColor = loadComplexColorForCookie(wrapper, value, id, theme);
        }
        if (complexColor != null) {
            complexColor.setBaseChangingConfigurations(value.changingConfigurations);
            if (!this.mPreloading) {
                cache.put(key, theme, complexColor.getConstantState());
            } else if (verifyPreloadConfig(complexColor.getChangingConfigurations(), 0, value.resourceId, "color")) {
                sPreloadedComplexColors.put(key, complexColor.getConstantState());
            }
        }
        return complexColor;
    }

    /* access modifiers changed from: package-private */
    public ComplexColor loadComplexColor(Resources wrapper, TypedValue value, int id, Resources.Theme theme) {
        long key = (((long) value.assetCookie) << 32) | ((long) value.data);
        if (value.type >= 28 && value.type <= 31) {
            return getColorStateListFromInt(value, key);
        }
        String file = value.string.toString();
        if (file.endsWith(".xml")) {
            try {
                return loadComplexColorFromName(wrapper, theme, value, id);
            } catch (Exception e) {
                Resources.NotFoundException rnf = new Resources.NotFoundException("File " + file + " from complex color resource ID #0x" + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        } else {
            throw new Resources.NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id) + ": .xml extension required");
        }
    }

    /* access modifiers changed from: package-private */
    public ColorStateList loadColorStateList(Resources wrapper, TypedValue value, int id, Resources.Theme theme) throws Resources.NotFoundException {
        long key = (((long) value.assetCookie) << 32) | ((long) value.data);
        if (value.type >= 28 && value.type <= 31) {
            return getColorStateListFromInt(value, key);
        }
        ComplexColor complexColor = loadComplexColorFromName(wrapper, theme, value, id);
        if (complexColor != null && (complexColor instanceof ColorStateList)) {
            return (ColorStateList) complexColor;
        }
        throw new Resources.NotFoundException("Can't find ColorStateList from drawable resource ID #0x" + Integer.toHexString(id));
    }

    private ColorStateList getColorStateListFromInt(TypedValue value, long key) {
        ConstantState<ComplexColor> factory = sPreloadedComplexColors.get(key);
        if (factory != null) {
            return (ColorStateList) factory.newInstance();
        }
        ColorStateList csl = ColorStateList.valueOf(value.data);
        if (this.mPreloading && verifyPreloadConfig(value.changingConfigurations, 0, value.resourceId, "color")) {
            sPreloadedComplexColors.put(key, csl.getConstantState());
        }
        return csl;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0034 A[Catch:{ Exception -> 0x0064 }] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x005c  */
    private ComplexColor loadComplexColorForCookie(Resources wrapper, TypedValue value, int id, Resources.Theme theme) {
        int type;
        if (value.string != null) {
            String file = value.string.toString();
            ComplexColor complexColor = null;
            Trace.traceBegin(8192, file);
            if (file.endsWith(".xml")) {
                try {
                    XmlResourceParser parser = loadXmlResourceParser(file, id, value.assetCookie, "ComplexColor");
                    AttributeSet attrs = Xml.asAttributeSet(parser);
                    while (true) {
                        type = parser.next();
                        if (type == 2 || type == 1) {
                            if (type != 2) {
                                String name = parser.getName();
                                if (name.equals("gradient")) {
                                    complexColor = GradientColor.createFromXmlInner(wrapper, parser, attrs, theme);
                                } else if (name.equals("selector")) {
                                    complexColor = ColorStateList.createFromXmlInner(wrapper, parser, attrs, theme);
                                }
                                parser.close();
                                Trace.traceEnd(8192);
                                return complexColor;
                            }
                            throw new XmlPullParserException("No start tag found");
                        }
                    }
                    if (type != 2) {
                    }
                } catch (Exception e) {
                    Trace.traceEnd(8192);
                    Resources.NotFoundException rnf = new Resources.NotFoundException("File " + file + " from ComplexColor resource ID #0x" + Integer.toHexString(id));
                    rnf.initCause(e);
                    throw rnf;
                }
            } else {
                Trace.traceEnd(8192);
                throw new Resources.NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id) + ": .xml extension required");
            }
        } else {
            throw new UnsupportedOperationException("Can't convert to ComplexColor: type=0x" + value.type);
        }
    }

    /* access modifiers changed from: package-private */
    public XmlResourceParser loadXmlResourceParser(String file, int id, int assetCookie, String type) throws Resources.NotFoundException {
        if (id != 0) {
            try {
                synchronized (this.mCachedXmlBlocks) {
                    int[] cachedXmlBlockCookies = this.mCachedXmlBlockCookies;
                    String[] cachedXmlBlockFiles = this.mCachedXmlBlockFiles;
                    XmlBlock[] cachedXmlBlocks = this.mCachedXmlBlocks;
                    int num = cachedXmlBlockFiles.length;
                    int i = 0;
                    while (i < num) {
                        if (cachedXmlBlockCookies[i] != assetCookie || cachedXmlBlockFiles[i] == null || !cachedXmlBlockFiles[i].equals(file)) {
                            i++;
                        } else {
                            XmlResourceParser newParser = cachedXmlBlocks[i].newParser(id);
                            return newParser;
                        }
                    }
                    XmlBlock block = this.mAssets.openXmlBlockAsset(assetCookie, file);
                    if (block != null) {
                        int pos = (this.mLastCachedXmlBlockIndex + 1) % num;
                        this.mLastCachedXmlBlockIndex = pos;
                        XmlBlock oldBlock = cachedXmlBlocks[pos];
                        if (oldBlock != null) {
                            oldBlock.close();
                        }
                        cachedXmlBlockCookies[pos] = assetCookie;
                        cachedXmlBlockFiles[pos] = file;
                        cachedXmlBlocks[pos] = block;
                        XmlResourceParser newParser2 = block.newParser(id);
                        return newParser2;
                    }
                }
            } catch (Exception e) {
                Resources.NotFoundException rnf = new Resources.NotFoundException("File " + file + " from xml type " + type + " resource ID #0x" + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        }
        throw new Resources.NotFoundException("File " + file + " from xml type " + type + " resource ID #0x" + Integer.toHexString(id));
    }

    public final void startPreloading() {
        synchronized (sSync) {
            if (!sPreloaded) {
                sPreloaded = true;
                this.mPreloading = true;
                this.mConfiguration.densityDpi = DisplayMetrics.DENSITY_DEVICE;
                updateConfiguration(null, null, null);
                if (TRACE_FOR_DETAILED_PRELOAD) {
                    this.mPreloadTracingPreloadStartTime = SystemClock.uptimeMillis();
                    this.mPreloadTracingStartBitmapSize = Bitmap.sPreloadTracingTotalBitmapsSize;
                    this.mPreloadTracingStartBitmapCount = (long) Bitmap.sPreloadTracingNumInstantiatedBitmaps;
                    Log.d(TAG_PRELOAD, "Preload starting");
                }
            } else {
                throw new IllegalStateException("Resources already preloaded");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void finishPreloading() {
        if (this.mPreloading) {
            if (TRACE_FOR_DETAILED_PRELOAD) {
                long time = SystemClock.uptimeMillis() - this.mPreloadTracingPreloadStartTime;
                long size = Bitmap.sPreloadTracingTotalBitmapsSize - this.mPreloadTracingStartBitmapSize;
                long count = ((long) Bitmap.sPreloadTracingNumInstantiatedBitmaps) - this.mPreloadTracingStartBitmapCount;
                Log.d(TAG_PRELOAD, "Preload finished, " + count + " bitmaps of " + size + " bytes in " + time + " ms");
            }
            this.mPreloading = false;
            flushLayoutCache();
        }
    }

    static int getAttributeSetSourceResId(AttributeSet set) {
        if (set == null || !(set instanceof XmlBlock.Parser)) {
            return 0;
        }
        return ((XmlBlock.Parser) set).getSourceResId();
    }

    /* access modifiers changed from: package-private */
    public LongSparseArray<Drawable.ConstantState> getPreloadedDrawables() {
        return sPreloadedDrawables[0];
    }

    /* access modifiers changed from: package-private */
    public ThemeImpl newThemeImpl() {
        return new ThemeImpl();
    }

    /* access modifiers changed from: package-private */
    public ThemeImpl newThemeImpl(Resources.ThemeKey key) {
        ThemeImpl impl = new ThemeImpl();
        impl.mKey.setTo(key);
        impl.rebase();
        return impl;
    }

    public class ThemeImpl {
        private final AssetManager mAssets;
        /* access modifiers changed from: private */
        public final Resources.ThemeKey mKey = new Resources.ThemeKey();
        private final long mTheme;
        private int mThemeResId = 0;

        ThemeImpl() {
            this.mAssets = ResourcesImpl.this.mAssets;
            this.mTheme = this.mAssets.createTheme();
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            super.finalize();
            this.mAssets.releaseTheme(this.mTheme);
        }

        /* access modifiers changed from: package-private */
        public Resources.ThemeKey getKey() {
            return this.mKey;
        }

        /* access modifiers changed from: package-private */
        public long getNativeTheme() {
            return this.mTheme;
        }

        /* access modifiers changed from: package-private */
        public int getAppliedStyleResId() {
            return this.mThemeResId;
        }

        /* access modifiers changed from: package-private */
        public void applyStyle(int resId, boolean force) {
            synchronized (this.mKey) {
                this.mAssets.applyStyleToTheme(this.mTheme, resId, force);
                this.mThemeResId = resId;
                this.mKey.append(resId, force);
            }
        }

        /* access modifiers changed from: package-private */
        public void setTo(ThemeImpl other) {
            synchronized (this.mKey) {
                synchronized (other.mKey) {
                    this.mAssets.setThemeTo(this.mTheme, other.mAssets, other.mTheme);
                    this.mThemeResId = other.mThemeResId;
                    this.mKey.setTo(other.getKey());
                }
            }
        }

        /* access modifiers changed from: package-private */
        public TypedArray obtainStyledAttributes(Resources.Theme wrapper, AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
            synchronized (this.mKey) {
                try {
                    TypedArray array = TypedArray.obtain(wrapper.getResources(), attrs.length);
                    XmlBlock.Parser parser = (XmlBlock.Parser) set;
                    this.mAssets.applyStyle(this.mTheme, defStyleAttr, defStyleRes, parser, attrs, array.mDataAddress, array.mIndicesAddress);
                    array.mTheme = wrapper;
                    array.mXml = parser;
                    TypedArray replaceTypedArray = ((IColorThemeManager) OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0])).replaceTypedArray(ResourcesImpl.this, array);
                    return replaceTypedArray;
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public TypedArray resolveAttributes(Resources.Theme wrapper, int[] values, int[] attrs) {
            TypedArray replaceTypedArray;
            synchronized (this.mKey) {
                int len = attrs.length;
                if (values == null || len != values.length) {
                    throw new IllegalArgumentException("Base attribute values must the same length as attrs");
                }
                TypedArray array = TypedArray.obtain(wrapper.getResources(), len);
                this.mAssets.resolveAttrs(this.mTheme, 0, 0, values, attrs, array.mData, array.mIndices);
                array.mTheme = wrapper;
                array.mXml = null;
                replaceTypedArray = ((IColorThemeManager) OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0])).replaceTypedArray(ResourcesImpl.this, array);
            }
            return replaceTypedArray;
        }

        /* access modifiers changed from: package-private */
        public boolean resolveAttribute(int resid, TypedValue outValue, boolean resolveRefs) {
            boolean themeValue;
            synchronized (this.mKey) {
                themeValue = this.mAssets.getThemeValue(this.mTheme, resid, outValue, resolveRefs);
            }
            return themeValue;
        }

        /* access modifiers changed from: package-private */
        public int[] getAllAttributes() {
            return this.mAssets.getStyleAttributes(getAppliedStyleResId());
        }

        /* access modifiers changed from: package-private */
        public int getChangingConfigurations() {
            int activityInfoConfigNativeToJava;
            synchronized (this.mKey) {
                activityInfoConfigNativeToJava = ActivityInfo.activityInfoConfigNativeToJava(AssetManager.nativeThemeGetChangingConfigurations(this.mTheme));
            }
            return activityInfoConfigNativeToJava;
        }

        public void dump(int priority, String tag, String prefix) {
            synchronized (this.mKey) {
                this.mAssets.dumpTheme(this.mTheme, priority, tag, prefix);
            }
        }

        /* access modifiers changed from: package-private */
        public String[] getTheme() {
            String[] themes;
            synchronized (this.mKey) {
                int N = this.mKey.mCount;
                themes = new String[(N * 2)];
                int i = 0;
                int j = N - 1;
                while (i < themes.length) {
                    int resId = this.mKey.mResId[j];
                    boolean forced = this.mKey.mForce[j];
                    try {
                        themes[i] = ResourcesImpl.this.getResourceName(resId);
                    } catch (Resources.NotFoundException e) {
                        themes[i] = Integer.toHexString(i);
                    }
                    themes[i + 1] = forced ? "forced" : "not forced";
                    i += 2;
                    j--;
                }
            }
            return themes;
        }

        /* access modifiers changed from: package-private */
        public void rebase() {
            synchronized (this.mKey) {
                AssetManager.nativeThemeClear(this.mTheme);
                for (int i = 0; i < this.mKey.mCount; i++) {
                    this.mAssets.applyStyleToTheme(this.mTheme, this.mKey.mResId[i], this.mKey.mForce[i]);
                }
            }
        }

        public int[] getAttributeResolutionStack(int defStyleAttr, int defStyleRes, int explicitStyleRes) {
            int[] attributeResolutionStack;
            synchronized (this.mKey) {
                attributeResolutionStack = this.mAssets.getAttributeResolutionStack(this.mTheme, defStyleAttr, defStyleRes, explicitStyleRes);
            }
            return attributeResolutionStack;
        }
    }

    /* access modifiers changed from: private */
    public static class LookupStack {
        private int[] mIds;
        private int mSize;

        private LookupStack() {
            this.mIds = new int[4];
            this.mSize = 0;
        }

        public void push(int id) {
            this.mIds = GrowingArrayUtils.append(this.mIds, this.mSize, id);
            this.mSize++;
        }

        public boolean contains(int id) {
            for (int i = 0; i < this.mSize; i++) {
                if (this.mIds[i] == id) {
                    return true;
                }
            }
            return false;
        }

        public void pop() {
            this.mSize--;
        }
    }

    @Override // android.content.res.ColorBaseResourcesImpl
    public void clearCache() {
        super.clearCache();
        sPreloadedDrawables[0].clear();
        sPreloadedComplexColors.clear();
        sPreloadedColorDrawables.clear();
    }

    @Override // android.content.res.ColorBaseResourcesImpl
    public Configuration getSystemConfiguration() {
        return Resources.getSystem().getConfiguration();
    }
}
