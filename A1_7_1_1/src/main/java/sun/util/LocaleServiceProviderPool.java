package sun.util;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IllformedLocaleException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.spi.LocaleServiceProvider;
import libcore.icu.ICU;
import sun.util.logging.PlatformLogger;
import sun.util.resources.OpenListResourceBundle;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
public final class LocaleServiceProviderPool {
    private static volatile List<Locale> availableJRELocales;
    private static Locale locale_ja_JP_JP;
    private static Locale locale_th_TH_TH;
    private static ConcurrentMap<Class<? extends LocaleServiceProvider>, LocaleServiceProviderPool> poolOfPools;
    private Set<Locale> availableLocales;
    private Set<Locale> providerLocales;
    private Set<LocaleServiceProvider> providers;
    private Map<Locale, LocaleServiceProvider> providersCache;

    public interface LocalizedObjectGetter<P, S> {
        S getObject(P p, Locale locale, String str, Object... objArr);
    }

    private static class AllAvailableLocales {
        static final Locale[] allAvailableLocales = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.util.LocaleServiceProviderPool.AllAvailableLocales.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.util.LocaleServiceProviderPool.AllAvailableLocales.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.LocaleServiceProviderPool.AllAvailableLocales.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.util.LocaleServiceProviderPool.AllAvailableLocales.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        private AllAvailableLocales() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.util.LocaleServiceProviderPool.AllAvailableLocales.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.LocaleServiceProviderPool.AllAvailableLocales.<init>():void");
        }
    }

    private static class NullProvider extends LocaleServiceProvider {
        private static final NullProvider INSTANCE = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.util.LocaleServiceProviderPool.NullProvider.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.util.LocaleServiceProviderPool.NullProvider.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.LocaleServiceProviderPool.NullProvider.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.util.LocaleServiceProviderPool.NullProvider.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        private NullProvider() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.util.LocaleServiceProviderPool.NullProvider.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.LocaleServiceProviderPool.NullProvider.<init>():void");
        }

        public Locale[] getAvailableLocales() {
            throw new RuntimeException("Should not get called.");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.util.LocaleServiceProviderPool.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.util.LocaleServiceProviderPool.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.util.LocaleServiceProviderPool.<clinit>():void");
    }

    public static LocaleServiceProviderPool getPool(Class<? extends LocaleServiceProvider> providerClass) {
        LocaleServiceProviderPool pool = (LocaleServiceProviderPool) poolOfPools.get(providerClass);
        if (pool != null) {
            return pool;
        }
        LocaleServiceProviderPool newPool = new LocaleServiceProviderPool(providerClass);
        pool = (LocaleServiceProviderPool) poolOfPools.putIfAbsent(providerClass, newPool);
        if (pool == null) {
            return newPool;
        }
        return pool;
    }

    private LocaleServiceProviderPool(final Class<? extends LocaleServiceProvider> c) {
        this.providers = new LinkedHashSet();
        this.providersCache = new ConcurrentHashMap();
        this.availableLocales = null;
        this.providerLocales = null;
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() {
                    for (LocaleServiceProvider provider : ServiceLoader.loadInstalled(c)) {
                        LocaleServiceProviderPool.this.providers.add(provider);
                    }
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            config(e.toString());
        }
    }

    private static void config(String message) {
        PlatformLogger.getLogger("sun.util.LocaleServiceProviderPool").config(message);
    }

    public static Locale[] getAllAvailableLocales() {
        return (Locale[]) AllAvailableLocales.allAvailableLocales.clone();
    }

    public synchronized Locale[] getAvailableLocales() {
        Locale[] tmp;
        if (this.availableLocales == null) {
            this.availableLocales = new HashSet(getJRELocales());
            if (hasProviders()) {
                this.availableLocales.addAll(getProviderLocales());
            }
        }
        tmp = new Locale[this.availableLocales.size()];
        this.availableLocales.toArray(tmp);
        return tmp;
    }

    private synchronized Set<Locale> getProviderLocales() {
        if (this.providerLocales == null) {
            this.providerLocales = new HashSet();
            if (hasProviders()) {
                for (LocaleServiceProvider lsp : this.providers) {
                    for (Locale locale : lsp.getAvailableLocales()) {
                        this.providerLocales.add(getLookupLocale(locale));
                    }
                }
            }
        }
        return this.providerLocales;
    }

    public boolean hasProviders() {
        return !this.providers.isEmpty();
    }

    private List<Locale> getJRELocales() {
        if (availableJRELocales == null) {
            synchronized (LocaleServiceProviderPool.class) {
                if (availableJRELocales == null) {
                    Locale[] allLocales = ICU.getAvailableLocales();
                    List<Locale> tmpList = new ArrayList(allLocales.length);
                    for (Locale locale : allLocales) {
                        tmpList.add(getLookupLocale(locale));
                    }
                    availableJRELocales = tmpList;
                }
            }
        }
        return availableJRELocales;
    }

    private boolean isJRESupported(Locale locale) {
        return getJRELocales().contains(getLookupLocale(locale));
    }

    public <P, S> S getLocalizedObject(LocalizedObjectGetter<P, S> getter, Locale locale, Object... params) {
        return getLocalizedObjectImpl(getter, locale, true, null, null, null, params);
    }

    public <P, S> S getLocalizedObject(LocalizedObjectGetter<P, S> getter, Locale locale, OpenListResourceBundle bundle, String key, Object... params) {
        return getLocalizedObjectImpl(getter, locale, false, null, bundle, key, params);
    }

    public <P, S> S getLocalizedObject(LocalizedObjectGetter<P, S> getter, Locale locale, String bundleKey, OpenListResourceBundle bundle, String key, Object... params) {
        return getLocalizedObjectImpl(getter, locale, false, bundleKey, bundle, key, params);
    }

    private <P, S> S getLocalizedObjectImpl(LocalizedObjectGetter<P, S> getter, Locale locale, boolean isObjectProvider, String bundleKey, OpenListResourceBundle bundle, String key, Object... params) {
        if (hasProviders()) {
            S providersObj;
            if (bundleKey == null) {
                bundleKey = key;
            }
            Object bundleLocale = bundle != null ? bundle.getLocale() : null;
            List<Locale> lookupLocales = getLookupLocales(locale);
            Set<Locale> provLoc = getProviderLocales();
            for (int i = 0; i < lookupLocales.size(); i++) {
                Locale current = (Locale) lookupLocales.get(i);
                if (bundleLocale == null) {
                    if (isJRESupported(current)) {
                        break;
                    }
                } else if (current.equals(bundleLocale)) {
                    break;
                }
                if (provLoc.contains(current)) {
                    Object lsp = findProvider(current);
                    if (lsp != null) {
                        providersObj = getter.getObject(lsp, locale, key, params);
                        if (providersObj != null) {
                            return providersObj;
                        }
                        if (isObjectProvider) {
                            config("A locale sensitive service provider returned null for a localized objects,  which should not happen.  provider: " + lsp + " locale: " + locale);
                        }
                    } else {
                        continue;
                    }
                }
            }
            while (bundle != null) {
                Locale bundleLocale2 = bundle.getLocale();
                if (bundle.handleGetKeys().contains(bundleKey)) {
                    return null;
                }
                P lsp2 = findProvider(bundleLocale2);
                if (lsp2 != null) {
                    providersObj = getter.getObject(lsp2, locale, key, params);
                    if (providersObj != null) {
                        return providersObj;
                    }
                }
                bundle = bundle.getParent();
            }
        }
        return null;
    }

    private LocaleServiceProvider findProvider(Locale locale) {
        if (!hasProviders()) {
            return null;
        }
        if (this.providersCache.containsKey(locale)) {
            LocaleServiceProvider provider = (LocaleServiceProvider) this.providersCache.get(locale);
            if (provider != NullProvider.INSTANCE) {
                return provider;
            }
        }
        for (LocaleServiceProvider lsp : this.providers) {
            for (Locale available : lsp.getAvailableLocales()) {
                if (locale.equals(getLookupLocale(available))) {
                    LocaleServiceProvider providerInCache = (LocaleServiceProvider) this.providersCache.put(locale, lsp);
                    if (providerInCache == null) {
                        providerInCache = lsp;
                    }
                    return providerInCache;
                }
            }
        }
        this.providersCache.put(locale, NullProvider.INSTANCE);
        return null;
    }

    private static List<Locale> getLookupLocales(Locale locale) {
        return 
/*
Method generation error in method: sun.util.LocaleServiceProviderPool.getLookupLocales(java.util.Locale):java.util.List<java.util.Locale>, dex: 
jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000c: RETURN  (wrap: java.util.List<java.util.Locale>
  0x0008: INVOKE  (r0_0 'lookupLocales' java.util.List<java.util.Locale>) = (wrap: sun.util.LocaleServiceProviderPool$2
  0x0002: CONSTRUCTOR  (r1_0 sun.util.LocaleServiceProviderPool$2) =  sun.util.LocaleServiceProviderPool.2.<init>():void CONSTRUCTOR), (wrap: java.lang.String
  0x0005: CONST_STR  (r2_0 java.lang.String) =  ""), (r3_0 'locale' java.util.Locale) sun.util.LocaleServiceProviderPool.2.getCandidateLocales(java.lang.String, java.util.Locale):java.util.List type: VIRTUAL) in method: sun.util.LocaleServiceProviderPool.getLookupLocales(java.util.Locale):java.util.List<java.util.Locale>, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:205)
	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:100)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:50)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:173)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:321)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:259)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:221)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:10)
	at jadx.core.ProcessClass.process(ProcessClass.java:38)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: INVOKE  (r0_0 'lookupLocales' java.util.List<java.util.Locale>) = (wrap: sun.util.LocaleServiceProviderPool$2
  0x0002: CONSTRUCTOR  (r1_0 sun.util.LocaleServiceProviderPool$2) =  sun.util.LocaleServiceProviderPool.2.<init>():void CONSTRUCTOR), (wrap: java.lang.String
  0x0005: CONST_STR  (r2_0 java.lang.String) =  ""), (r3_0 'locale' java.util.Locale) sun.util.LocaleServiceProviderPool.2.getCandidateLocales(java.lang.String, java.util.Locale):java.util.List type: VIRTUAL in method: sun.util.LocaleServiceProviderPool.getLookupLocales(java.util.Locale):java.util.List<java.util.Locale>, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:101)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:286)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:222)
	... 16 more
Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0002: CONSTRUCTOR  (r1_0 sun.util.LocaleServiceProviderPool$2) =  sun.util.LocaleServiceProviderPool.2.<init>():void CONSTRUCTOR in method: sun.util.LocaleServiceProviderPool.getLookupLocales(java.util.Locale):java.util.List<java.util.Locale>, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:101)
	at jadx.core.codegen.InsnGen.addArgDot(InsnGen.java:84)
	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:634)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:340)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:213)
	... 19 more
Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Null container variable
	at jadx.core.utils.RegionUtils.notEmpty(RegionUtils.java:151)
	at jadx.core.codegen.InsnGen.inlineAnonymousConstr(InsnGen.java:595)
	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:561)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:336)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:213)
	... 24 more

*/

    private static Locale getLookupLocale(Locale locale) {
        Locale lookupLocale = locale;
        if (locale.getExtensionKeys().isEmpty() || locale.equals(locale_ja_JP_JP) || locale.equals(locale_th_TH_TH)) {
            return lookupLocale;
        }
        Builder locbld = new Builder();
        try {
            locbld.setLocale(locale);
            locbld.clearExtensions();
            return locbld.build();
        } catch (IllformedLocaleException e) {
            config("A locale(" + locale + ") has non-empty extensions, but has illformed fields.");
            return new Locale(locale.getLanguage(), locale.getCountry(), locale.getVariant());
        }
    }
}
