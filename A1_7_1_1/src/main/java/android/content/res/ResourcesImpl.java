package android.content.res;

import android.animation.Animator;
import android.animation.StateListAnimator;
import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.pm.ActivityInfo;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.icu.text.PluralRules;
import android.os.Build.VERSION;
import android.os.LocaleList;
import android.os.Trace;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.util.Xml;
import android.view.DisplayAdjustments;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import oppo.content.res.OppoExtraConfiguration;
import oppo.content.res.OppoThemeResources;
import oppo.content.res.OppoThemeResourcesPackage;
import oppo.content.res.OppoThemeZipFile.ThemeFileInfo;
import org.xmlpull.v1.XmlPullParserException;

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
public class ResourcesImpl {
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    public static final int COOKIE_TYPE_FRAMEWORK = 1;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    public static final int COOKIE_TYPE_OPPO = 2;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    public static final int COOKIE_TYPE_OTHERPACKAGE = 3;
    private static final boolean DEBUG_CONFIG = false;
    private static final boolean DEBUG_LOAD = false;
    private static final int ID_OTHER = 16777220;
    private static final int LAYOUT_DIR_CONFIG = 0;
    static final String TAG = "Resources";
    private static final boolean TRACE_FOR_MISS_PRELOAD = false;
    private static final boolean TRACE_FOR_PRELOAD = false;
    private static final int XML_BLOCK_CACHE_SIZE = 4;
    private static final ArrayMap<String, LongSparseArray<ConstantState>> sBoostDrawableCache = null;
    private static boolean sPreloaded;
    private static final LongSparseArray<ConstantState> sPreloadedColorDrawables = null;
    private static final LongSparseArray<ConstantState<ComplexColor>> sPreloadedComplexColors = null;
    private static final LongSparseArray<ConstantState>[] sPreloadedDrawables = null;
    private static final Object sSync = null;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    private static boolean themeChanged;
    private final Object mAccessLock;
    private final ConfigurationBoundResourceCache<Animator> mAnimatorCache;
    final AssetManager mAssets;
    private String mBoostKey;
    private final int[] mCachedXmlBlockCookies;
    private final String[] mCachedXmlBlockFiles;
    private final XmlBlock[] mCachedXmlBlocks;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    private SparseArray<CharSequence> mCharSequences;
    private final DrawableCache mColorDrawableCache;
    private final ConfigurationBoundResourceCache<ComplexColor> mComplexColorCache;
    private final Configuration mConfiguration;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    private SparseIntArray mCookies;
    private final DisplayAdjustments mDisplayAdjustments;
    private final DrawableCache mDrawableCache;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    private SparseArray<Integer> mIntegers;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Xiaokang.Feng@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    private boolean mIsHasDrawables;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    private boolean mIsHasValues;
    private int mLastCachedXmlBlockIndex;
    private final DisplayMetrics mMetrics;
    private PluralRules mPluralRule;
    private boolean mPreloading;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    private SparseArray<Boolean> mSkipFiles;
    private final ConfigurationBoundResourceCache<StateListAnimator> mStateListAnimatorCache;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "XiaoKang.Feng@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    private boolean mThemeChangeEnable;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    private OppoThemeResources mThemeResources;
    private final Configuration mTmpConfig;

    public class ThemeImpl {
        private final AssetManager mAssets;
        private final ThemeKey mKey = new ThemeKey();
        private final long mTheme;
        private int mThemeResId = 0;

        ThemeImpl() {
            this.mAssets = ResourcesImpl.this.mAssets;
            this.mTheme = this.mAssets.createTheme();
        }

        protected void finalize() throws Throwable {
            super.finalize();
            this.mAssets.releaseTheme(this.mTheme);
        }

        ThemeKey getKey() {
            return this.mKey;
        }

        long getNativeTheme() {
            return this.mTheme;
        }

        int getAppliedStyleResId() {
            return this.mThemeResId;
        }

        void applyStyle(int resId, boolean force) {
            synchronized (this.mKey) {
                AssetManager.applyThemeStyle(this.mTheme, resId, force);
                this.mThemeResId = resId;
                this.mKey.append(resId, force);
            }
        }

        void setTo(ThemeImpl other) {
            synchronized (this.mKey) {
                synchronized (other.mKey) {
                    AssetManager.copyTheme(this.mTheme, other.mTheme);
                    this.mThemeResId = other.mThemeResId;
                    this.mKey.setTo(other.getKey());
                }
            }
        }

        TypedArray obtainStyledAttributes(Theme wrapper, AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
            TypedArray replaceTypedArray;
            synchronized (this.mKey) {
                TypedArray array = TypedArray.obtain(wrapper.getResources(), attrs.length);
                Parser parser = (Parser) set;
                AssetManager.applyStyle(this.mTheme, defStyleAttr, defStyleRes, parser != null ? parser.mParseState : 0, attrs, array.mData, array.mIndices);
                array.mTheme = wrapper;
                array.mXml = parser;
                replaceTypedArray = ResourcesImpl.this.replaceTypedArray(array);
            }
            return replaceTypedArray;
        }

        TypedArray resolveAttributes(Theme wrapper, int[] values, int[] attrs) {
            TypedArray replaceTypedArray;
            synchronized (this.mKey) {
                int len = attrs.length;
                if (values == null || len != values.length) {
                    throw new IllegalArgumentException("Base attribute values must the same length as attrs");
                }
                TypedArray array = TypedArray.obtain(wrapper.getResources(), len);
                AssetManager.resolveAttrs(this.mTheme, 0, 0, values, attrs, array.mData, array.mIndices);
                array.mTheme = wrapper;
                array.mXml = null;
                replaceTypedArray = ResourcesImpl.this.replaceTypedArray(array);
            }
            return replaceTypedArray;
        }

        boolean resolveAttribute(int resid, TypedValue outValue, boolean resolveRefs) {
            boolean themeValue;
            synchronized (this.mKey) {
                themeValue = this.mAssets.getThemeValue(this.mTheme, resid, outValue, resolveRefs);
            }
            return themeValue;
        }

        int[] getAllAttributes() {
            return this.mAssets.getStyleAttributes(getAppliedStyleResId());
        }

        int getChangingConfigurations() {
            int activityInfoConfigNativeToJava;
            synchronized (this.mKey) {
                activityInfoConfigNativeToJava = ActivityInfo.activityInfoConfigNativeToJava(AssetManager.getThemeChangingConfigurations(this.mTheme));
            }
            return activityInfoConfigNativeToJava;
        }

        public void dump(int priority, String tag, String prefix) {
            synchronized (this.mKey) {
                AssetManager.dumpTheme(this.mTheme, priority, tag, prefix);
            }
        }

        String[] getTheme() {
            String[] themes;
            synchronized (this.mKey) {
                int N = this.mKey.mCount;
                themes = new String[(N * 2)];
                int i = 0;
                int j = N - 1;
                while (i < themes.length) {
                    String str;
                    int resId = this.mKey.mResId[j];
                    boolean forced = this.mKey.mForce[j];
                    try {
                        themes[i] = ResourcesImpl.this.getResourceName(resId);
                    } catch (NotFoundException e) {
                        themes[i] = Integer.toHexString(i);
                    }
                    int i2 = i + 1;
                    if (forced) {
                        str = "forced";
                    } else {
                        str = "not forced";
                    }
                    themes[i2] = str;
                    i += 2;
                    j--;
                }
            }
            return themes;
        }

        void rebase() {
            synchronized (this.mKey) {
                AssetManager.clearTheme(this.mTheme);
                for (int i = 0; i < this.mKey.mCount; i++) {
                    AssetManager.applyThemeStyle(this.mTheme, this.mKey.mResId[i], this.mKey.mForce[i]);
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.content.res.ResourcesImpl.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.content.res.ResourcesImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.res.ResourcesImpl.<clinit>():void");
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "YaoJun.Luo@Plf.SDK : Modify for rom theme", property = OppoRomType.ROM)
    public ResourcesImpl(AssetManager assets, DisplayMetrics metrics, Configuration config, DisplayAdjustments displayAdjustments) {
        this.mBoostKey = "";
        this.mAccessLock = new Object();
        this.mTmpConfig = new Configuration();
        this.mDrawableCache = new DrawableCache();
        this.mColorDrawableCache = new DrawableCache();
        this.mComplexColorCache = new ConfigurationBoundResourceCache();
        this.mAnimatorCache = new ConfigurationBoundResourceCache();
        this.mStateListAnimatorCache = new ConfigurationBoundResourceCache();
        this.mLastCachedXmlBlockIndex = -1;
        this.mCachedXmlBlockCookies = new int[4];
        this.mCachedXmlBlockFiles = new String[4];
        this.mCachedXmlBlocks = new XmlBlock[4];
        this.mMetrics = new DisplayMetrics();
        this.mConfiguration = new Configuration();
        this.mIsHasValues = false;
        this.mIsHasDrawables = false;
        this.mCharSequences = new SparseArray();
        this.mCookies = new SparseIntArray();
        this.mIntegers = new SparseArray();
        this.mSkipFiles = new SparseArray();
        this.mThemeChangeEnable = true;
        this.mAssets = assets;
        this.mMetrics.setToDefaults();
        this.mDisplayAdjustments = displayAdjustments;
        updateConfiguration(config, metrics, displayAdjustments.getCompatibilityInfo());
        this.mAssets.ensureStringBlocks();
        init(null);
    }

    public DisplayAdjustments getDisplayAdjustments() {
        return this.mDisplayAdjustments;
    }

    public AssetManager getAssets() {
        return this.mAssets;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE_AND_ACCESS, note = "Xiaokang.Feng@Plf.SDK : [+public] Modify for rom theme", property = OppoRomType.ROM)
    public DisplayMetrics getDisplayMetrics() {
        return this.mMetrics;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE_AND_ACCESS, note = "Xiaokang.Feng@Plf.SDK : [+public] Modify for rom theme", property = OppoRomType.ROM)
    public Configuration getConfiguration() {
        return this.mConfiguration;
    }

    Configuration[] getSizeConfigurations() {
        return this.mAssets.getSizeConfigurations();
    }

    CompatibilityInfo getCompatibilityInfo() {
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

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "YaoJun.Luo@Plf.SDK : Modify for rom theme", property = OppoRomType.ROM)
    void getValue(int id, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        if (!this.mAssets.getResourceValue(id, 0, outValue, resolveRefs)) {
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id));
        } else if ((outValue.type >= 16 && outValue.type <= 31) || outValue.type == 5) {
            Integer res = getThemeInt(id, outValue.resourceId);
            if (res != null) {
                outValue.data = res.intValue();
            }
        }
    }

    void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        if (!this.mAssets.getResourceValue(id, density, outValue, resolveRefs)) {
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id));
        }
    }

    void getValue(String name, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        int id = getIdentifier(name, "string", null);
        if (id != 0) {
            getValue(id, outValue, resolveRefs);
            return;
        }
        throw new NotFoundException("String resource name " + name);
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE_AND_ACCESS, note = "Xiaokang.Feng@Plf.SDK : [+public] Modify for rom theme", property = OppoRomType.ROM)
    public int getIdentifier(String name, String defType, String defPackage) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        try {
            return Integer.parseInt(name);
        } catch (Exception e) {
            return this.mAssets.getResourceIdentifier(name, defType, defPackage);
        }
    }

    String getResourceName(int resid) throws NotFoundException {
        String str = this.mAssets.getResourceName(resid);
        if (str != null) {
            return str;
        }
        throw new NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    String getResourcePackageName(int resid) throws NotFoundException {
        String str = this.mAssets.getResourcePackageName(resid);
        if (str != null) {
            return str;
        }
        throw new NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    String getResourceTypeName(int resid) throws NotFoundException {
        String str = this.mAssets.getResourceTypeName(resid);
        if (str != null) {
            return str;
        }
        throw new NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    String getResourceEntryName(int resid) throws NotFoundException {
        String str = this.mAssets.getResourceEntryName(resid);
        if (str != null) {
            return str;
        }
        throw new NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
        PluralRules rule = getPluralRule();
        CharSequence res = this.mAssets.getResourceBagText(id, attrForQuantityCode(rule.select((double) quantity)));
        if (res != null) {
            return res;
        }
        res = this.mAssets.getResourceBagText(id, ID_OTHER);
        if (res != null) {
            return res;
        }
        throw new NotFoundException("Plural resource ID #0x" + Integer.toHexString(id) + " quantity=" + quantity + " item=" + rule.select((double) quantity));
    }

    private static int attrForQuantityCode(String quantityCode) {
        if (quantityCode.equals("zero")) {
            return 16777221;
        }
        if (quantityCode.equals("one")) {
            return 16777222;
        }
        if (quantityCode.equals("two")) {
            return 16777223;
        }
        if (quantityCode.equals("few")) {
            return 16777224;
        }
        if (quantityCode.equals("many")) {
            return 16777225;
        }
        return ID_OTHER;
    }

    AssetFileDescriptor openRawResourceFd(int id, TypedValue tempValue) throws NotFoundException {
        getValue(id, tempValue, true);
        try {
            return this.mAssets.openNonAssetFd(tempValue.assetCookie, tempValue.string.toString());
        } catch (Exception e) {
            throw new NotFoundException("File " + tempValue.string.toString() + " from drawable " + "resource ID #0x" + Integer.toHexString(id), e);
        }
    }

    InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
        getValue(id, value, true);
        InputStream input = openOppoThemeRawResource(id, value);
        if (input != null) {
            return input;
        }
        try {
            return this.mAssets.openNonAsset(value.assetCookie, value.string.toString(), 2);
        } catch (Exception e) {
            NotFoundException rnf = new NotFoundException("File " + (value.string == null ? "(null)" : value.string.toString()) + " from drawable resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(e);
            throw rnf;
        }
    }

    ConfigurationBoundResourceCache<Animator> getAnimatorCache() {
        return this.mAnimatorCache;
    }

    ConfigurationBoundResourceCache<StateListAnimator> getStateListAnimatorCache() {
        return this.mStateListAnimatorCache;
    }

    public void updateConfiguration(Configuration config, DisplayMetrics metrics, CompatibilityInfo compat) {
        String appName = this.mBoostKey;
        if (appName != null) {
            if (appName.contains("com.jb.gosms") && this.mMetrics != null && this.mMetrics.equals(metrics) && this.mConfiguration != null && this.mConfiguration.equals(config) && (compat == null || (compat != null && this.mDisplayAdjustments.getCompatibilityInfo().equals(compat)))) {
                return;
            }
        }
        Trace.traceBegin(32768, "ResourcesImpl#updateConfiguration");
        int i = 0;
        boolean languageChaged = false;
        try {
            if (!(this.mConfiguration == null || config == null)) {
                i = this.mConfiguration.diff(config);
                if (!(this.mConfiguration.locale == null || config.locale == null)) {
                    languageChaged = !this.mConfiguration.locale.equals(config.locale);
                }
            }
            synchronized (this.mAccessLock) {
                int width;
                int height;
                int keyboardHidden;
                if (compat != null) {
                    this.mDisplayAdjustments.setCompatibilityInfo(compat);
                }
                if (metrics != null) {
                    this.mMetrics.setTo(metrics);
                }
                this.mDisplayAdjustments.getCompatibilityInfo().applyToDisplayMetrics(this.mMetrics);
                int configChanges = calcConfigChanges(config);
                LocaleList locales = this.mConfiguration.getLocales();
                if (locales.isEmpty()) {
                    locales = LocaleList.getDefault();
                    this.mConfiguration.setLocales(locales);
                }
                if ((configChanges & 4) != 0) {
                    String[] availableLocales;
                    if (locales.size() > 1) {
                        availableLocales = this.mAssets.getNonSystemLocales();
                        if (LocaleList.isPseudoLocalesOnly(availableLocales)) {
                            availableLocales = this.mAssets.getLocales();
                            if (LocaleList.isPseudoLocalesOnly(availableLocales)) {
                                availableLocales = null;
                            }
                        }
                        if (availableLocales != null) {
                            Locale bestLocale = locales.getFirstMatchWithEnglishSupported(availableLocales);
                            if (!(bestLocale == null || bestLocale == locales.get(0))) {
                                this.mConfiguration.setLocales(new LocaleList(bestLocale, locales));
                            }
                        }
                    }
                    availableLocales = this.mAssets.getNonSystemLocales();
                    if (this.mConfiguration.getLocales().get(0).toString().equals("ug_CN") && availableLocales != null && availableLocales.length > 0 && !Arrays.asList(availableLocales).contains("ug-CN")) {
                        Log.i(TAG, "Current system Language is ug_CN but this application is not support ug_CN, So we nee to change the language to zh_CN");
                        this.mConfiguration.setLocales(new LocaleList(Locale.SIMPLIFIED_CHINESE, locales));
                    }
                }
                if (this.mConfiguration.densityDpi != 0) {
                    this.mMetrics.densityDpi = this.mConfiguration.densityDpi;
                    this.mMetrics.density = ((float) this.mConfiguration.densityDpi) * 0.00625f;
                }
                this.mMetrics.scaledDensity = this.mMetrics.density * this.mConfiguration.fontScale;
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
                this.mAssets.setConfiguration(this.mConfiguration.mcc, this.mConfiguration.mnc, adjustLanguageTag(this.mConfiguration.getLocales().get(0).toLanguageTag()), this.mConfiguration.orientation, this.mConfiguration.touchscreen, this.mConfiguration.densityDpi, this.mConfiguration.keyboard, keyboardHidden, this.mConfiguration.navigation, width, height, this.mConfiguration.smallestScreenWidthDp, this.mConfiguration.screenWidthDp, this.mConfiguration.screenHeightDp, this.mConfiguration.screenLayout, this.mConfiguration.uiMode, VERSION.RESOURCES_SDK_INT);
                String boostKey = this.mBoostKey;
                if (boostKey != null && isBoostApp(boostKey)) {
                    LongSparseArray<ConstantState> boostCache = (LongSparseArray) sBoostDrawableCache.get(boostKey);
                    if (boostCache != null) {
                        clearBoostDrawableCacheLocked(boostCache, configChanges);
                        Slog.w(TAG, "Clear boost cache");
                    }
                }
                this.mDrawableCache.onConfigurationChange(configChanges);
                this.mColorDrawableCache.onConfigurationChange(configChanges);
                this.mComplexColorCache.onConfigurationChange(configChanges);
                this.mAnimatorCache.onConfigurationChange(configChanges);
                this.mStateListAnimatorCache.onConfigurationChange(configChanges);
                flushLayoutCache();
            }
            synchronized (sSync) {
                if (this.mPluralRule != null) {
                    this.mPluralRule = PluralRules.forLocale(this.mConfiguration.getLocales().get(0));
                }
            }
            if (this.mThemeResources != null && OppoExtraConfiguration.needNewResources(i)) {
                clearCache();
                this.mIntegers.clear();
                this.mCharSequences.clear();
                this.mSkipFiles.clear();
                this.mThemeResources.checkUpdate();
                this.mIsHasValues = this.mThemeResources.hasValues();
                if (this.mIsHasValues) {
                    this.mIsHasDrawables = true;
                } else {
                    this.mIsHasDrawables = this.mThemeResources.isValid();
                }
            } else if (this.mThemeResources != null && languageChaged) {
                this.mIntegers.clear();
                this.mCharSequences.clear();
                this.mSkipFiles.clear();
                this.mThemeResources.checkUpdate();
                this.mIsHasValues = this.mThemeResources.hasValues();
                if (this.mIsHasValues) {
                    this.mIsHasDrawables = true;
                } else {
                    this.mIsHasDrawables = this.mThemeResources.isValid();
                }
            }
            Trace.traceEnd(32768);
        } catch (Throwable th) {
            Trace.traceEnd(32768);
        }
    }

    private boolean isBoostApp(String appname) {
        if (appname.equals("android.content.res.Resources")) {
            return false;
        }
        String[] applist = new String[1];
        applist[0] = "com.tencent.mm";
        for (String name : applist) {
            if (appname.contains(name)) {
                return true;
            }
        }
        return false;
    }

    private void clearBoostDrawableCacheLocked(LongSparseArray<ConstantState> cache, int configChanges) {
        int N = cache.size();
        for (int i = 0; i < N; i++) {
            ConstantState cs = (ConstantState) cache.valueAt(i);
            if (cs != null && Configuration.needNewResources(configChanges, cs.getChangingConfigurations())) {
                cache.setValueAt(i, null);
            }
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
        String language;
        String remainder;
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
            Arrays.fill(this.mCachedXmlBlockFiles, null);
            XmlBlock[] cachedXmlBlocks = this.mCachedXmlBlocks;
            for (int i = 0; i < 4; i++) {
                XmlBlock oldBlock = cachedXmlBlocks[i];
                if (oldBlock != null) {
                    oldBlock.close();
                }
            }
            Arrays.fill(cachedXmlBlocks, null);
        }
    }

    Drawable loadDrawable(Resources wrapper, TypedValue value, int id, Theme theme, boolean useCache) throws NotFoundException {
        try {
            boolean isColorDrawable;
            DrawableCache caches;
            long key;
            ConstantState cs;
            Drawable dr;
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
                Drawable cachedDrawable = caches.getInstance(key, wrapper, theme);
                if (cachedDrawable != null) {
                    return cachedDrawable;
                }
                Drawable boostDrawable = getBoostCachedDrawable(wrapper, key);
                if (boostDrawable != null) {
                    Slog.w(TAG, "Using Boost");
                    return boostDrawable;
                }
            }
            if (this.mIsHasValues) {
                cs = null;
            } else if (isColorDrawable) {
                cs = (ConstantState) sPreloadedColorDrawables.get(key);
            } else {
                cs = (ConstantState) sPreloadedDrawables[this.mConfiguration.getLayoutDirection()].get(key);
            }
            if (cs != null) {
                dr = cs.newDrawable(wrapper);
            } else if (isColorDrawable) {
                dr = new ColorDrawable(value.data);
            } else {
                dr = loadDrawableForCookie(wrapper, value, id, null);
            }
            boolean canApplyTheme = dr != null ? dr.canApplyTheme() : false;
            if (canApplyTheme && theme != null) {
                dr = dr.mutate();
                dr.applyTheme(theme);
                dr.clearMutated();
            }
            if (dr != null && useCache) {
                dr.setChangingConfigurations(value.changingConfigurations);
                cacheDrawable(value, isColorDrawable, caches, theme, canApplyTheme, key, dr);
            }
            return dr;
        } catch (Exception e) {
            String name;
            try {
                name = getResourceName(id);
            } catch (NotFoundException e2) {
                name = "(missing name)";
            }
            NotFoundException notFoundException = new NotFoundException("Drawable " + name + " with resource ID #0x" + Integer.toHexString(id), e);
            notFoundException.setStackTrace(new StackTraceElement[0]);
            throw notFoundException;
        }
    }

    private void cacheDrawable(TypedValue value, boolean isColorDrawable, DrawableCache caches, Theme theme, boolean usesTheme, long key, Drawable dr) {
        ConstantState cs = dr.getConstantState();
        if (cs != null) {
            if (this.mPreloading) {
                int changingConfigs = cs.getChangingConfigurations();
                if (isColorDrawable) {
                    if (verifyPreloadConfig(changingConfigs, 0, value.resourceId, "drawable")) {
                        sPreloadedColorDrawables.put(key, cs);
                    }
                } else if (verifyPreloadConfig(changingConfigs, LAYOUT_DIR_CONFIG, value.resourceId, "drawable")) {
                    if ((LAYOUT_DIR_CONFIG & changingConfigs) == 0) {
                        sPreloadedDrawables[0].put(key, cs);
                        sPreloadedDrawables[1].put(key, cs);
                    } else {
                        sPreloadedDrawables[this.mConfiguration.getLayoutDirection()].put(key, cs);
                    }
                }
            } else {
                synchronized (this.mAccessLock) {
                    caches.put(key, theme, cs, usesTheme);
                    if (!isColorDrawable) {
                        String boostKey = this.mBoostKey;
                        if (isBoostApp(boostKey)) {
                            LongSparseArray<ConstantState> boostCache = (LongSparseArray) sBoostDrawableCache.get(boostKey);
                            if (boostCache == null) {
                                boostCache = new LongSparseArray(1);
                                sBoostDrawableCache.put(boostKey, boostCache);
                                for (String resKey : sBoostDrawableCache.keySet()) {
                                    Slog.w(TAG, "ResourceKey:" + resKey);
                                }
                            }
                            boostCache.put(key, cs);
                            Slog.w(TAG, "CacheKey:" + key + " Resource:" + this.mBoostKey);
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x002f, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Drawable getBoostCachedDrawable(Resources wrapper, long key) {
        synchronized (this.mAccessLock) {
            this.mBoostKey = wrapper.toString().split("@")[0];
            String boostKey = this.mBoostKey;
            if (isBoostApp(boostKey)) {
                LongSparseArray<ConstantState> boostCache = (LongSparseArray) sBoostDrawableCache.get(boostKey);
                if (boostCache != null) {
                    Drawable boostDrawable = getBoostCachedDrawableLocked(wrapper, boostCache, key);
                    if (boostDrawable != null) {
                        return boostDrawable;
                    }
                }
            }
        }
    }

    private Drawable getBoostCachedDrawableLocked(Resources wrapper, LongSparseArray<ConstantState> drawableCache, long key) {
        ConstantState entry = (ConstantState) drawableCache.get(key);
        if (entry != null) {
            return entry.newDrawable(wrapper);
        }
        drawableCache.delete(key);
        return null;
    }

    private boolean verifyPreloadConfig(int changingConfigurations, int allowVarying, int resourceId, String name) {
        if (((-1073745921 & changingConfigurations) & (~allowVarying)) == 0) {
            return true;
        }
        String resName;
        try {
            resName = getResourceName(resourceId);
        } catch (NotFoundException e) {
            resName = "?";
        }
        Log.w(TAG, "Preloaded " + name + " resource #0x" + Integer.toHexString(resourceId) + " (" + resName + ") that varies with configuration!!");
        return false;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "YaoJun.Luo@Plf.SDK : Modify for rom theme", property = OppoRomType.ROM)
    private Drawable loadDrawableForCookie(Resources wrapper, TypedValue value, int id, Theme theme) {
        if (value.string == null) {
            throw new NotFoundException("Resource \"" + getResourceName(id) + "\" (" + Integer.toHexString(id) + ") is not a Drawable (color or path): " + value);
        }
        String file = value.string.toString();
        Trace.traceBegin(32768, file);
        try {
            Drawable dr;
            if (file.endsWith(".xml")) {
                XmlResourceParser rp = loadXmlResourceParser(file, id, value.assetCookie, "drawable");
                dr = Drawable.createFromXml(wrapper, rp, theme);
                rp.close();
            } else {
                dr = loadOverlayDrawable(wrapper, value, id);
                if (dr == null) {
                    InputStream is = this.mAssets.openNonAsset(value.assetCookie, file, 2);
                    dr = Drawable.createFromResourceStream(wrapper, value, is, file, null);
                    is.close();
                }
            }
            Trace.traceEnd(32768);
            return dr;
        } catch (Exception e) {
            Trace.traceEnd(32768);
            NotFoundException rnf = new NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(e);
            throw rnf;
        }
    }

    private ComplexColor loadComplexColorFromName(Resources wrapper, Theme theme, TypedValue value, int id) {
        long key = (((long) value.assetCookie) << 32) | ((long) value.data);
        ConfigurationBoundResourceCache<ComplexColor> cache = this.mComplexColorCache;
        ComplexColor complexColor = (ComplexColor) cache.getInstance(key, wrapper, theme);
        if (complexColor != null) {
            return complexColor;
        }
        ConstantState<ComplexColor> factory = (ConstantState) sPreloadedComplexColors.get(key);
        if (factory != null) {
            complexColor = (ComplexColor) factory.newInstance(wrapper, theme);
        }
        if (complexColor == null) {
            complexColor = loadComplexColorForCookie(wrapper, value, id, theme);
        }
        if (complexColor != null) {
            complexColor.setBaseChangingConfigurations(value.changingConfigurations);
            if (!this.mPreloading) {
                cache.put(key, theme, complexColor.getConstantState());
            } else if (verifyPreloadConfig(complexColor.getChangingConfigurations(), 0, value.resourceId, ColorsColumns.COLOR)) {
                sPreloadedComplexColors.put(key, complexColor.getConstantState());
            }
        }
        return complexColor;
    }

    ComplexColor loadComplexColor(Resources wrapper, TypedValue value, int id, Theme theme) {
        long key = (((long) value.assetCookie) << 32) | ((long) value.data);
        if (value.type >= 28 && value.type <= 31) {
            return getColorStateListFromInt(value, key);
        }
        String file = value.string.toString();
        if (file.endsWith(".xml")) {
            try {
                return loadComplexColorFromName(wrapper, theme, value, id);
            } catch (Exception e) {
                NotFoundException rnf = new NotFoundException("File " + file + " from complex color resource ID #0x" + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        }
        throw new NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id) + ": .xml extension required");
    }

    ColorStateList loadColorStateList(Resources wrapper, TypedValue value, int id, Theme theme) throws NotFoundException {
        long key = (((long) value.assetCookie) << 32) | ((long) value.data);
        if (value.type >= 28 && value.type <= 31) {
            return getColorStateListFromInt(value, key);
        }
        ComplexColor complexColor = loadComplexColorFromName(wrapper, theme, value, id);
        if (complexColor != null && (complexColor instanceof ColorStateList)) {
            return (ColorStateList) complexColor;
        }
        throw new NotFoundException("Can't find ColorStateList from drawable resource ID #0x" + Integer.toHexString(id));
    }

    private ColorStateList getColorStateListFromInt(TypedValue value, long key) {
        ConstantState<ComplexColor> factory = (ConstantState) sPreloadedComplexColors.get(key);
        if (factory != null) {
            return (ColorStateList) factory.newInstance();
        }
        ColorStateList csl = ColorStateList.valueOf(value.data);
        if (this.mPreloading && verifyPreloadConfig(value.changingConfigurations, 0, value.resourceId, ColorsColumns.COLOR)) {
            sPreloadedComplexColors.put(key, csl.getConstantState());
        }
        return csl;
    }

    private ComplexColor loadComplexColorForCookie(Resources wrapper, TypedValue value, int id, Theme theme) {
        if (value.string == null) {
            throw new UnsupportedOperationException("Can't convert to ComplexColor: type=0x" + value.type);
        }
        String file = value.string.toString();
        ComplexColor complexColor = null;
        Trace.traceBegin(32768, file);
        if (file.endsWith(".xml")) {
            try {
                int type;
                XmlResourceParser parser = loadXmlResourceParser(file, id, value.assetCookie, "ComplexColor");
                AttributeSet attrs = Xml.asAttributeSet(parser);
                do {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } while (type != 1);
                if (type != 2) {
                    throw new XmlPullParserException("No start tag found");
                }
                String name = parser.getName();
                if (name.equals("gradient")) {
                    complexColor = GradientColor.createFromXmlInner(wrapper, parser, attrs, theme);
                } else if (name.equals("selector")) {
                    complexColor = ColorStateList.createFromXmlInner(wrapper, parser, attrs, theme);
                }
                parser.close();
                Trace.traceEnd(32768);
                return complexColor;
            } catch (Exception e) {
                Trace.traceEnd(32768);
                NotFoundException rnf = new NotFoundException("File " + file + " from ComplexColor resource ID #0x" + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        }
        Trace.traceEnd(32768);
        throw new NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id) + ": .xml extension required");
    }

    XmlResourceParser loadXmlResourceParser(String file, int id, int assetCookie, String type) throws NotFoundException {
        if (id != 0) {
            try {
                synchronized (this.mCachedXmlBlocks) {
                    XmlResourceParser newParser;
                    int[] cachedXmlBlockCookies = this.mCachedXmlBlockCookies;
                    String[] cachedXmlBlockFiles = this.mCachedXmlBlockFiles;
                    XmlBlock[] cachedXmlBlocks = this.mCachedXmlBlocks;
                    int num = cachedXmlBlockFiles.length;
                    int i = 0;
                    while (i < num) {
                        if (cachedXmlBlockCookies[i] == assetCookie && cachedXmlBlockFiles[i] != null && cachedXmlBlockFiles[i].equals(file)) {
                            newParser = cachedXmlBlocks[i].newParser();
                            return newParser;
                        }
                        i++;
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
                        newParser = block.newParser();
                        return newParser;
                    }
                }
            } catch (Exception e) {
                NotFoundException rnf = new NotFoundException("File " + file + " from xml type " + type + " resource ID #0x" + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        }
        throw new NotFoundException("File " + file + " from xml type " + type + " resource ID #0x" + Integer.toHexString(id));
    }

    public final void startPreloading() {
        synchronized (sSync) {
            if (sPreloaded) {
                throw new IllegalStateException("Resources already preloaded");
            }
            sPreloaded = true;
            this.mPreloading = true;
            this.mConfiguration.densityDpi = DisplayMetrics.DENSITY_DEVICE;
            updateConfiguration(null, null, null);
        }
    }

    void finishPreloading() {
        if (this.mPreloading) {
            this.mPreloading = false;
            flushLayoutCache();
        }
    }

    LongSparseArray<ConstantState> getPreloadedDrawables() {
        return sPreloadedDrawables[0];
    }

    ThemeImpl newThemeImpl() {
        return new ThemeImpl();
    }

    ThemeImpl newThemeImpl(ThemeKey key) {
        ThemeImpl impl = new ThemeImpl();
        impl.mKey.setTo(key);
        impl.rebase();
        return impl;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    static void clearPreloadedCache() {
        sPreloadedDrawables[0].clear();
        sPreloadedComplexColors.clear();
        sPreloadedColorDrawables.clear();
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    public void clearCache() {
        themeChanged = true;
        this.mDrawableCache.clear();
        this.mComplexColorCache.clear();
        this.mColorDrawableCache.clear();
        clearPreloadedCache();
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme; Suying.You@Plf.SDK : Modify for Modify the packagename of the third party, and decompress the zipFile of data/string", property = OppoRomType.ROM)
    public void init(String name) {
        if (TextUtils.isEmpty(name) || ZenModeConfig.SYSTEM_AUTHORITY.equals(name) || "oppo".equals(name)) {
            this.mThemeResources = OppoThemeResources.getSystem(this);
        } else {
            this.mThemeResources = OppoThemeResourcesPackage.getThemeResources(this, name);
        }
        if (this.mThemeResources != null) {
            this.mIsHasValues = this.mThemeResources.hasValues();
            if (this.mIsHasValues) {
                this.mIsHasDrawables = true;
            } else {
                this.mIsHasDrawables = this.mThemeResources.isValid();
            }
        }
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    public Drawable loadOverlayDrawable(Resources wrapper, TypedValue value, int id) {
        if (!this.mIsHasDrawables) {
            return null;
        }
        Drawable drawable = null;
        if (this.mSkipFiles.get(id) == null && this.mThemeResources != null) {
            String path = value.string.toString();
            ThemeFileInfo themeFileInfo = this.mThemeResources.getThemeFileStream(getCookieType(value.assetCookie), path);
            if (themeFileInfo == null) {
                this.mSkipFiles.put(id, Boolean.valueOf(true));
                return drawable;
            }
            try {
                Options options = new Options();
                options.inDensity = themeFileInfo.mDensity;
                drawable = Drawable.createFromResourceStream(wrapper, value, themeFileInfo.mInput, path, options);
                if (themeFileInfo != null) {
                    try {
                        themeFileInfo.mInput.close();
                    } catch (Exception e) {
                    }
                }
            } catch (OutOfMemoryError e2) {
                Log.e(TAG, "out of memory !!");
                if (themeFileInfo != null) {
                    try {
                        themeFileInfo.mInput.close();
                    } catch (Exception e3) {
                    }
                }
            } catch (Throwable th) {
                if (themeFileInfo != null) {
                    try {
                        themeFileInfo.mInput.close();
                    } catch (Exception e4) {
                    }
                }
            }
        }
        return drawable;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    private int getCookieType(int id) {
        int i = this.mCookies.get(id);
        if (i == 0) {
            String packageName = this.mAssets.getCookieName(id);
            if ("/system/framework/framework-res.apk".equals(packageName)) {
                i = 1;
            } else if (OppoResourceHelper.OPPO_RES_PATH.equals(packageName)) {
                i = 2;
            } else {
                i = 3;
            }
            this.mCookies.put(id, i);
        }
        return i;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    public Integer getThemeInt(int id) {
        return getThemeInt(id, 0);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme; Xiaokang.Feng@Plf.SDK : Modify for rom theme", property = OppoRomType.ROM)
    public Integer getThemeInt(int id, int resourceId) {
        if (!this.mIsHasValues) {
            return null;
        }
        int index = this.mIntegers.indexOfKey(id);
        if (index >= 0) {
            return (Integer) this.mIntegers.valueAt(index);
        }
        Integer res = this.mThemeResources.getThemeInt(id);
        if (!(res != null || resourceId == 0 || resourceId == id)) {
            res = this.mThemeResources.getThemeInt(resourceId);
        }
        this.mIntegers.put(id, res);
        return res;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme; Xiaokang.Feng@Plf.SDK : Modify for rom theme", property = OppoRomType.ROM)
    public TypedArray replaceTypedArray(TypedArray typedarray) {
        if (this.mIsHasValues) {
            int[] ai = typedarray.mData;
            for (int i = 0; i < ai.length; i += 6) {
                int type = ai[i + 0];
                int id = ai[i + 3];
                if ((type >= 16 && type <= 31) || type == 5) {
                    Integer res = getThemeInt(id);
                    if (res != null) {
                        ai[i + 1] = res.intValue();
                    }
                }
            }
        }
        return typedarray;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Xiaokang.Feng@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    public void setIsThemeChanged(boolean changed) {
        themeChanged = changed;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Xiaokang.Feng@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    public boolean getThemeChanged() {
        return themeChanged;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "YaoJun.Luo@Plf.SDK : Add for rom theme. ConvertIcon; XiaoKang.Feng@Plf.SDK : Modify for ConvertIcon", property = OppoRomType.ROM)
    public Drawable loadIcon(Resources wrapper, int id, String str, boolean useWrap) {
        Drawable drawable = null;
        TypedValue value = new TypedValue();
        getValue(id, value, true);
        String path = value.string.toString();
        if (str != null) {
            path = path.replace(path.substring(path.lastIndexOf("/") + 1), str);
        }
        if (this.mSkipFiles.get(id) == null && this.mThemeResources != null) {
            OppoThemeResources oppoThemeResources = this.mThemeResources;
            ThemeFileInfo themeFileInfo = OppoThemeResources.getSystem().getIconStream(path, useWrap);
            if (themeFileInfo == null) {
                this.mSkipFiles.put(id, Boolean.valueOf(true));
                return null;
            }
            if (themeFileInfo != null) {
                try {
                    Options options = new Options();
                    options.inDensity = themeFileInfo.mDensity;
                    drawable = Drawable.createFromResourceStream(wrapper, value, themeFileInfo.mInput, path, options);
                } catch (OutOfMemoryError e) {
                    if (themeFileInfo != null) {
                        try {
                            themeFileInfo.mInput.close();
                        } catch (IOException e2) {
                        }
                    }
                } catch (Throwable th) {
                    if (themeFileInfo != null) {
                        try {
                            themeFileInfo.mInput.close();
                        } catch (IOException e3) {
                        }
                    }
                }
            }
            if (themeFileInfo != null) {
                try {
                    themeFileInfo.mInput.close();
                } catch (IOException e4) {
                }
            }
        }
        return drawable;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK : Add for rom theme", property = OppoRomType.ROM)
    public InputStream openOppoThemeRawResource(int id, TypedValue outValue) throws NotFoundException {
        if (this.mIsHasDrawables && this.mSkipFiles.get(id) == null && this.mThemeResources != null) {
            ThemeFileInfo themeFileInfo = this.mThemeResources.getThemeFileStream(getCookieType(outValue.assetCookie), outValue.string.toString());
            if (themeFileInfo != null) {
                return themeFileInfo.mInput;
            }
            this.mSkipFiles.put(id, Boolean.valueOf(true));
        }
        return null;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Suying.You@Plf.SDK, 2016-03-25 : Add for rom theme. Modify the packagename of the third party, and decompress the zipFile of data/string", property = OppoRomType.ROM)
    public CharSequence getThemeCharSequence(int id) {
        int index = this.mCharSequences.indexOfKey(id);
        CharSequence resou = this.mThemeResources.getThemeCharSequence(id);
        if (!this.mIsHasValues) {
            return null;
        }
        if (index >= 0) {
            return (CharSequence) this.mCharSequences.valueAt(index);
        }
        return this.mThemeResources.getThemeCharSequence(id);
    }
}
