package java.util;

import dalvik.system.VMStack;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.JarEntry;
import sun.reflect.CallerSensitive;
import sun.util.locale.BaseLocale;
import sun.util.locale.LocaleObjectCache;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public abstract class ResourceBundle {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f75-assertionsDisabled = false;
    private static final int INITIAL_CACHE_SIZE = 32;
    private static final ResourceBundle NONEXISTENT_BUNDLE = null;
    private static final ConcurrentMap<CacheKey, BundleReference> cacheList = null;
    private static final ReferenceQueue referenceQueue = null;
    private volatile CacheKey cacheKey;
    private volatile boolean expired;
    private volatile Set<String> keySet;
    private Locale locale;
    private String name;
    protected ResourceBundle parent;

    private interface CacheKeyReference {
        CacheKey getCacheKey();
    }

    private static final class BundleReference extends SoftReference<ResourceBundle> implements CacheKeyReference {
        private CacheKey cacheKey;

        BundleReference(ResourceBundle referent, ReferenceQueue q, CacheKey key) {
            super(referent, q);
            this.cacheKey = key;
        }

        public CacheKey getCacheKey() {
            return this.cacheKey;
        }
    }

    private static final class CacheKey implements Cloneable {
        private Throwable cause;
        private volatile long expirationTime;
        private String format;
        private int hashCodeCache;
        private volatile long loadTime;
        private LoaderReference loaderRef;
        private Locale locale;
        private String name;

        CacheKey(String baseName, Locale locale, ClassLoader loader) {
            this.name = baseName;
            this.locale = locale;
            if (loader == null) {
                this.loaderRef = null;
            } else {
                this.loaderRef = new LoaderReference(loader, ResourceBundle.referenceQueue, this);
            }
            calculateHashCode();
        }

        String getName() {
            return this.name;
        }

        CacheKey setName(String baseName) {
            if (!this.name.equals(baseName)) {
                this.name = baseName;
                calculateHashCode();
            }
            return this;
        }

        Locale getLocale() {
            return this.locale;
        }

        CacheKey setLocale(Locale locale) {
            if (!this.locale.equals(locale)) {
                this.locale = locale;
                calculateHashCode();
            }
            return this;
        }

        ClassLoader getLoader() {
            return this.loaderRef != null ? (ClassLoader) this.loaderRef.get() : null;
        }

        public boolean equals(Object other) {
            boolean z = true;
            if (this == other) {
                return true;
            }
            try {
                CacheKey otherEntry = (CacheKey) other;
                if (this.hashCodeCache != otherEntry.hashCodeCache || !this.name.equals(otherEntry.name) || !this.locale.equals(otherEntry.locale)) {
                    return ResourceBundle.f75-assertionsDisabled;
                }
                if (this.loaderRef == null) {
                    if (otherEntry.loaderRef != null) {
                        z = ResourceBundle.f75-assertionsDisabled;
                    }
                    return z;
                }
                ClassLoader loader = (ClassLoader) this.loaderRef.get();
                if (otherEntry.loaderRef == null || loader == null) {
                    z = ResourceBundle.f75-assertionsDisabled;
                } else if (loader != otherEntry.loaderRef.get()) {
                    z = ResourceBundle.f75-assertionsDisabled;
                }
                return z;
            } catch (NullPointerException e) {
                return ResourceBundle.f75-assertionsDisabled;
            } catch (ClassCastException e2) {
                return ResourceBundle.f75-assertionsDisabled;
            }
        }

        public int hashCode() {
            return this.hashCodeCache;
        }

        private void calculateHashCode() {
            this.hashCodeCache = this.name.hashCode() << 3;
            this.hashCodeCache ^= this.locale.hashCode();
            ClassLoader loader = getLoader();
            if (loader != null) {
                this.hashCodeCache ^= loader.hashCode();
            }
        }

        public Object clone() {
            try {
                CacheKey clone = (CacheKey) super.clone();
                if (this.loaderRef != null) {
                    clone.loaderRef = new LoaderReference((ClassLoader) this.loaderRef.get(), ResourceBundle.referenceQueue, clone);
                }
                clone.cause = null;
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new InternalError();
            }
        }

        String getFormat() {
            return this.format;
        }

        void setFormat(String format) {
            this.format = format;
        }

        private void setCause(Throwable cause) {
            if (this.cause == null) {
                this.cause = cause;
            } else if (this.cause instanceof ClassNotFoundException) {
                this.cause = cause;
            }
        }

        private Throwable getCause() {
            return this.cause;
        }

        public String toString() {
            String l = this.locale.toString();
            if (l.length() == 0) {
                if (this.locale.getVariant().length() != 0) {
                    l = "__" + this.locale.getVariant();
                } else {
                    l = "\"\"";
                }
            }
            return "CacheKey[" + this.name + ", lc=" + l + ", ldr=" + getLoader() + "(format=" + this.format + ")]";
        }
    }

    public static class Control {
        private static final CandidateListCache CANDIDATES_CACHE = null;
        public static final List<String> FORMAT_CLASS = null;
        public static final List<String> FORMAT_DEFAULT = null;
        public static final List<String> FORMAT_PROPERTIES = null;
        private static final Control INSTANCE = null;
        public static final long TTL_DONT_CACHE = -1;
        public static final long TTL_NO_EXPIRATION_CONTROL = -2;

        private static class CandidateListCache extends LocaleObjectCache<BaseLocale, List<Locale>> {
            /* synthetic */ CandidateListCache(CandidateListCache candidateListCache) {
                this();
            }

            private CandidateListCache() {
            }

            protected List<Locale> createObject(BaseLocale base) {
                String language = base.getLanguage();
                String script = base.getScript();
                String region = base.getRegion();
                String variant = base.getVariant();
                boolean isNorwegianBokmal = ResourceBundle.f75-assertionsDisabled;
                boolean isNorwegianNynorsk = ResourceBundle.f75-assertionsDisabled;
                if (language.equals("no")) {
                    if (region.equals("NO") && variant.equals("NY")) {
                        variant = "";
                        isNorwegianNynorsk = true;
                    } else {
                        isNorwegianBokmal = true;
                    }
                }
                if (language.equals("nb") || isNorwegianBokmal) {
                    List<Locale> tmpList = getDefaultList("nb", script, region, variant);
                    List<Locale> bokmalList = new LinkedList();
                    for (Locale l : tmpList) {
                        bokmalList.add(l);
                        if (l.getLanguage().length() == 0) {
                            break;
                        }
                        bokmalList.add(Locale.getInstance("no", l.getScript(), l.getCountry(), l.getVariant(), null));
                    }
                    return bokmalList;
                } else if (language.equals("nn") || isNorwegianNynorsk) {
                    List<Locale> nynorskList = getDefaultList("nn", script, region, variant);
                    int size = nynorskList.size() - 1;
                    int i = size + 1;
                    nynorskList.add(size, Locale.getInstance("no", "NO", "NY"));
                    size = i + 1;
                    nynorskList.add(i, Locale.getInstance("no", "NO", ""));
                    i = size + 1;
                    nynorskList.add(size, Locale.getInstance("no", "", ""));
                    return nynorskList;
                } else {
                    if (language.equals("zh")) {
                        if (script.length() != 0 || region.length() <= 0) {
                            if (script.length() > 0 && region.length() == 0) {
                                if (script.equals("Hans")) {
                                    region = "CN";
                                } else if (script.equals("Hant")) {
                                    region = "TW";
                                }
                            }
                        } else if (region.equals("TW") || region.equals("HK") || region.equals("MO")) {
                            script = "Hant";
                        } else if (region.equals("CN") || region.equals("SG")) {
                            script = "Hans";
                        }
                    }
                    return getDefaultList(language, script, region, variant);
                }
            }

            private static List<Locale> getDefaultList(String language, String script, String region, String variant) {
                Iterable variants = null;
                if (variant.length() > 0) {
                    variants = new LinkedList();
                    int idx = variant.length();
                    while (idx != -1) {
                        variants.add(variant.substring(0, idx));
                        idx = variant.lastIndexOf(95, idx - 1);
                    }
                }
                List<Locale> list = new LinkedList();
                if (variants != null) {
                    for (String v : variants) {
                        list.add(Locale.getInstance(language, script, region, v, null));
                    }
                }
                if (region.length() > 0) {
                    list.add(Locale.getInstance(language, script, region, "", null));
                }
                if (script.length() > 0) {
                    list.add(Locale.getInstance(language, script, "", "", null));
                    if (variants != null) {
                        for (String v2 : variants) {
                            list.add(Locale.getInstance(language, "", region, v2, null));
                        }
                    }
                    if (region.length() > 0) {
                        list.add(Locale.getInstance(language, "", region, "", null));
                    }
                }
                if (language.length() > 0) {
                    list.add(Locale.getInstance(language, "", "", "", null));
                }
                list.add(Locale.ROOT);
                return list;
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.ResourceBundle.Control.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.ResourceBundle.Control.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.ResourceBundle.Control.<clinit>():void");
        }

        protected Control() {
        }

        public static final Control getControl(List<String> formats) {
            if (formats.equals(FORMAT_PROPERTIES)) {
                return SingleFormatControl.PROPERTIES_ONLY;
            }
            if (formats.equals(FORMAT_CLASS)) {
                return SingleFormatControl.CLASS_ONLY;
            }
            if (formats.equals(FORMAT_DEFAULT)) {
                return INSTANCE;
            }
            throw new IllegalArgumentException();
        }

        public static final Control getNoFallbackControl(List<String> formats) {
            if (formats.equals(FORMAT_DEFAULT)) {
                return NoFallbackControl.NO_FALLBACK;
            }
            if (formats.equals(FORMAT_PROPERTIES)) {
                return NoFallbackControl.PROPERTIES_ONLY_NO_FALLBACK;
            }
            if (formats.equals(FORMAT_CLASS)) {
                return NoFallbackControl.CLASS_ONLY_NO_FALLBACK;
            }
            throw new IllegalArgumentException();
        }

        public List<String> getFormats(String baseName) {
            if (baseName != null) {
                return FORMAT_DEFAULT;
            }
            throw new NullPointerException();
        }

        public List<Locale> getCandidateLocales(String baseName, Locale locale) {
            if (baseName != null) {
                return new ArrayList((Collection) CANDIDATES_CACHE.get(locale.getBaseLocale()));
            }
            throw new NullPointerException();
        }

        public Locale getFallbackLocale(String baseName, Locale locale) {
            if (baseName == null) {
                throw new NullPointerException();
            }
            Locale defaultLocale = Locale.getDefault();
            return locale.equals(defaultLocale) ? null : defaultLocale;
        }

        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            ResourceBundle bundle = null;
            if (format.equals("java.class")) {
                try {
                    Class<? extends ResourceBundle> bundleClass = loader.loadClass(bundleName);
                    if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
                        return (ResourceBundle) bundleClass.newInstance();
                    }
                    throw new ClassCastException(bundleClass.getName() + " cannot be cast to ResourceBundle");
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }
            if (format.equals("java.properties")) {
                final String resourceName = toResourceName(bundleName, "properties");
                ClassLoader classLoader = loader;
                boolean reloadFlag = reload;
                try {
                    final boolean z = reload;
                    final ClassLoader classLoader2 = loader;
                    InputStream stream = (InputStream) AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                        public InputStream run() throws IOException {
                            if (!z) {
                                return classLoader2.getResourceAsStream(resourceName);
                            }
                            URL url = classLoader2.getResource(resourceName);
                            if (url == null) {
                                return null;
                            }
                            URLConnection connection = url.openConnection();
                            if (connection == null) {
                                return null;
                            }
                            connection.setUseCaches(ResourceBundle.f75-assertionsDisabled);
                            return connection.getInputStream();
                        }
                    });
                    if (stream == null) {
                        return null;
                    }
                    try {
                        bundle = new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
                        return bundle;
                    } finally {
                        stream.close();
                    }
                } catch (PrivilegedActionException e2) {
                    throw ((IOException) e2.getException());
                }
            }
            throw new IllegalArgumentException("unknown format: " + format);
        }

        public long getTimeToLive(String baseName, Locale locale) {
            if (baseName != null && locale != null) {
                return -2;
            }
            throw new NullPointerException();
        }

        /* JADX WARNING: Missing block: B:6:0x001c, code:
            if (r17.equals("java.properties") != false) goto L_0x001e;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean needsReload(String baseName, Locale locale, String format, ClassLoader loader, ResourceBundle bundle, long loadTime) {
            if (bundle == null) {
                throw new NullPointerException();
            }
            if (!format.equals("java.class")) {
            }
            format = format.substring(5);
            try {
                URL url = loader.getResource(toResourceName(toBundleName(baseName, locale), format));
                if (url == null) {
                    return ResourceBundle.f75-assertionsDisabled;
                }
                long lastModified = 0;
                URLConnection connection = url.openConnection();
                if (connection != null) {
                    connection.setUseCaches(ResourceBundle.f75-assertionsDisabled);
                    if (connection instanceof JarURLConnection) {
                        JarEntry ent = ((JarURLConnection) connection).getJarEntry();
                        if (ent != null) {
                            lastModified = ent.getTime();
                            if (lastModified == -1) {
                                lastModified = 0;
                            }
                        }
                    } else {
                        lastModified = connection.getLastModified();
                    }
                }
                return lastModified >= loadTime ? true : ResourceBundle.f75-assertionsDisabled;
            } catch (NullPointerException npe) {
                throw npe;
            } catch (Exception e) {
                return ResourceBundle.f75-assertionsDisabled;
            }
        }

        public String toBundleName(String baseName, Locale locale) {
            if (locale == Locale.ROOT) {
                return baseName;
            }
            String language = locale.getLanguage();
            String script = locale.getScript();
            String country = locale.getCountry();
            String variant = locale.getVariant();
            if (language == "" && country == "" && variant == "") {
                return baseName;
            }
            StringBuilder sb = new StringBuilder(baseName);
            sb.append('_');
            if (script != "") {
                if (variant != "") {
                    sb.append(language).append('_').append(script).append('_').append(country).append('_').append(variant);
                } else if (country != "") {
                    sb.append(language).append('_').append(script).append('_').append(country);
                } else {
                    sb.append(language).append('_').append(script);
                }
            } else if (variant != "") {
                sb.append(language).append('_').append(country).append('_').append(variant);
            } else if (country != "") {
                sb.append(language).append('_').append(country);
            } else {
                sb.append(language);
            }
            return sb.toString();
        }

        public final String toResourceName(String bundleName, String suffix) {
            StringBuilder sb = new StringBuilder((bundleName.length() + 1) + suffix.length());
            sb.append(bundleName.replace('.', '/')).append('.').append(suffix);
            return sb.toString();
        }
    }

    private static final class LoaderReference extends WeakReference<ClassLoader> implements CacheKeyReference {
        private CacheKey cacheKey;

        LoaderReference(ClassLoader referent, ReferenceQueue q, CacheKey key) {
            super(referent, q);
            this.cacheKey = key;
        }

        public CacheKey getCacheKey() {
            return this.cacheKey;
        }
    }

    private static class SingleFormatControl extends Control {
        private static final Control CLASS_ONLY = null;
        private static final Control PROPERTIES_ONLY = null;
        private final List<String> formats;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.ResourceBundle.SingleFormatControl.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.ResourceBundle.SingleFormatControl.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.ResourceBundle.SingleFormatControl.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.ResourceBundle.SingleFormatControl.<init>(java.util.List):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected SingleFormatControl(java.util.List<java.lang.String> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.ResourceBundle.SingleFormatControl.<init>(java.util.List):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.ResourceBundle.SingleFormatControl.<init>(java.util.List):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.ResourceBundle.SingleFormatControl.getFormats(java.lang.String):java.util.List<java.lang.String>, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.util.List<java.lang.String> getFormats(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.ResourceBundle.SingleFormatControl.getFormats(java.lang.String):java.util.List<java.lang.String>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.ResourceBundle.SingleFormatControl.getFormats(java.lang.String):java.util.List<java.lang.String>");
        }
    }

    private static final class NoFallbackControl extends SingleFormatControl {
        private static final Control CLASS_ONLY_NO_FALLBACK = null;
        private static final Control NO_FALLBACK = null;
        private static final Control PROPERTIES_ONLY_NO_FALLBACK = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.ResourceBundle.NoFallbackControl.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.ResourceBundle.NoFallbackControl.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.ResourceBundle.NoFallbackControl.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.ResourceBundle.NoFallbackControl.<init>(java.util.List):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected NoFallbackControl(java.util.List<java.lang.String> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.ResourceBundle.NoFallbackControl.<init>(java.util.List):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.ResourceBundle.NoFallbackControl.<init>(java.util.List):void");
        }

        public Locale getFallbackLocale(String baseName, Locale locale) {
            if (baseName != null && locale != null) {
                return null;
            }
            throw new NullPointerException();
        }
    }

    private static class RBClassLoader extends ClassLoader {
        private static final RBClassLoader INSTANCE = null;
        private static final ClassLoader loader = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.ResourceBundle.RBClassLoader.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.ResourceBundle.RBClassLoader.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.ResourceBundle.RBClassLoader.<clinit>():void");
        }

        /* synthetic */ RBClassLoader(RBClassLoader rBClassLoader) {
            this();
        }

        private RBClassLoader() {
        }

        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (loader != null) {
                return loader.loadClass(name);
            }
            return Class.forName(name);
        }

        public URL getResource(String name) {
            if (loader != null) {
                return loader.getResource(name);
            }
            return ClassLoader.getSystemResource(name);
        }

        public InputStream getResourceAsStream(String name) {
            if (loader != null) {
                return loader.getResourceAsStream(name);
            }
            return ClassLoader.getSystemResourceAsStream(name);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.ResourceBundle.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.ResourceBundle.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.ResourceBundle.<clinit>():void");
    }

    public abstract Enumeration<String> getKeys();

    protected abstract Object handleGetObject(String str);

    public ResourceBundle() {
        this.parent = null;
        this.locale = null;
    }

    public final String getString(String key) {
        return (String) getObject(key);
    }

    public final String[] getStringArray(String key) {
        return (String[]) getObject(key);
    }

    public final Object getObject(String key) {
        Object obj = handleGetObject(key);
        if (obj == null) {
            if (this.parent != null) {
                obj = this.parent.getObject(key);
            }
            if (obj == null) {
                throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + key, getClass().getName(), key);
            }
        }
        return obj;
    }

    public Locale getLocale() {
        return this.locale;
    }

    private static ClassLoader getLoader(ClassLoader cl) {
        if (cl == null) {
            return RBClassLoader.INSTANCE;
        }
        return cl;
    }

    protected void setParent(ResourceBundle parent) {
        if (!f75-assertionsDisabled) {
            if ((parent != NONEXISTENT_BUNDLE ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        this.parent = parent;
    }

    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName) {
        return getBundleImpl(baseName, Locale.getDefault(), getLoader(VMStack.getCallingClassLoader()), Control.INSTANCE);
    }

    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName, Control control) {
        return getBundleImpl(baseName, Locale.getDefault(), getLoader(VMStack.getCallingClassLoader()), control);
    }

    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName, Locale locale) {
        return getBundleImpl(baseName, locale, getLoader(VMStack.getCallingClassLoader()), Control.INSTANCE);
    }

    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName, Locale targetLocale, Control control) {
        return getBundleImpl(baseName, targetLocale, getLoader(VMStack.getCallingClassLoader()), control);
    }

    public static ResourceBundle getBundle(String baseName, Locale locale, ClassLoader loader) {
        if (loader != null) {
            return getBundleImpl(baseName, locale, loader, Control.INSTANCE);
        }
        throw new NullPointerException();
    }

    public static ResourceBundle getBundle(String baseName, Locale targetLocale, ClassLoader loader, Control control) {
        if (loader != null && control != null) {
            return getBundleImpl(baseName, targetLocale, loader, control);
        }
        throw new NullPointerException();
    }

    private static ResourceBundle getBundleImpl(String baseName, Locale locale, ClassLoader loader, Control control) {
        if (locale == null || control == null) {
            throw new NullPointerException();
        }
        CacheKey cacheKey = new CacheKey(baseName, locale, loader);
        ResourceBundle bundle = null;
        BundleReference bundleRef = (BundleReference) cacheList.get(cacheKey);
        if (bundleRef != null) {
            bundle = (ResourceBundle) bundleRef.get();
        }
        if (isValidBundle(bundle) && hasValidParentChain(bundle)) {
            return bundle;
        }
        boolean isKnownControl;
        if (control != Control.INSTANCE) {
            isKnownControl = control instanceof SingleFormatControl;
        } else {
            isKnownControl = true;
        }
        List<String> formats = control.getFormats(baseName);
        if (isKnownControl || checkList(formats)) {
            ResourceBundle baseBundle = null;
            Locale targetLocale = locale;
            while (targetLocale != null) {
                List<Locale> candidateLocales = control.getCandidateLocales(baseName, targetLocale);
                if (isKnownControl || checkList(candidateLocales)) {
                    bundle = findBundle(cacheKey, candidateLocales, formats, 0, control, baseBundle);
                    if (isValidBundle(bundle)) {
                        boolean isBaseBundle = Locale.ROOT.equals(bundle.locale);
                        if (!isBaseBundle || bundle.locale.equals(locale) || (candidateLocales.size() == 1 && bundle.locale.equals(candidateLocales.get(0)))) {
                            break;
                        } else if (isBaseBundle && baseBundle == null) {
                            baseBundle = bundle;
                        }
                    }
                    targetLocale = control.getFallbackLocale(baseName, targetLocale);
                } else {
                    throw new IllegalArgumentException("Invalid Control: getCandidateLocales");
                }
            }
            if (bundle == null) {
                if (baseBundle == null) {
                    throwMissingResourceException(baseName, locale, cacheKey.getCause());
                }
                bundle = baseBundle;
            }
            return bundle;
        }
        throw new IllegalArgumentException("Invalid Control: getFormats");
    }

    private static final boolean checkList(List a) {
        boolean valid = f75-assertionsDisabled;
        if (!(a == null || a.size() == 0)) {
            valid = true;
        }
        if (valid) {
            int size = a.size();
            int i = 0;
            while (valid && i < size) {
                valid = a.get(i) != null ? true : f75-assertionsDisabled;
                i++;
            }
        }
        return valid;
    }

    private static final ResourceBundle findBundle(CacheKey cacheKey, List<Locale> candidateLocales, List<String> formats, int index, Control control, ResourceBundle baseBundle) {
        Locale targetLocale = (Locale) candidateLocales.get(index);
        ResourceBundle parent = null;
        if (index != candidateLocales.size() - 1) {
            parent = findBundle(cacheKey, candidateLocales, formats, index + 1, control, baseBundle);
        } else if (baseBundle != null && Locale.ROOT.equals(targetLocale)) {
            return baseBundle;
        }
        while (true) {
            Object ref = referenceQueue.poll();
            if (ref == null) {
                break;
            }
            cacheList.remove(((CacheKeyReference) ref).getCacheKey());
        }
        boolean expiredBundle = f75-assertionsDisabled;
        cacheKey.setLocale(targetLocale);
        ResourceBundle bundle = findBundleInCache(cacheKey, control);
        if (isValidBundle(bundle)) {
            expiredBundle = bundle.expired;
            if (!expiredBundle) {
                if (bundle.parent == parent) {
                    return bundle;
                }
                BundleReference bundleRef = (BundleReference) cacheList.get(cacheKey);
                if (bundleRef != null && bundleRef.get() == bundle) {
                    cacheList.remove(cacheKey, bundleRef);
                }
            }
        }
        if (bundle != NONEXISTENT_BUNDLE) {
            CacheKey constKey = (CacheKey) cacheKey.clone();
            try {
                bundle = loadBundle(cacheKey, formats, control, expiredBundle);
                if (bundle != null) {
                    if (bundle.parent == null) {
                        bundle.setParent(parent);
                    }
                    bundle.locale = targetLocale;
                    bundle = putBundleInCache(cacheKey, bundle, control);
                    return bundle;
                }
                putBundleInCache(cacheKey, NONEXISTENT_BUNDLE, control);
                if (constKey.getCause() instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            } finally {
                if (constKey.getCause() instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return parent;
    }

    private static final ResourceBundle loadBundle(CacheKey cacheKey, List<String> formats, Control control, boolean reload) {
        Locale targetLocale = cacheKey.getLocale();
        ResourceBundle bundle = null;
        int size = formats.size();
        for (int i = 0; i < size; i++) {
            String format = (String) formats.get(i);
            try {
                bundle = control.newBundle(cacheKey.getName(), targetLocale, format, cacheKey.getLoader(), reload);
            } catch (LinkageError error) {
                cacheKey.setCause(error);
            } catch (Exception cause) {
                cacheKey.setCause(cause);
            }
            if (bundle != null) {
                cacheKey.setFormat(format);
                bundle.name = cacheKey.getName();
                bundle.locale = targetLocale;
                bundle.expired = f75-assertionsDisabled;
                break;
            }
        }
        return bundle;
    }

    private static final boolean isValidBundle(ResourceBundle bundle) {
        return (bundle == null || bundle == NONEXISTENT_BUNDLE) ? f75-assertionsDisabled : true;
    }

    private static final boolean hasValidParentChain(ResourceBundle bundle) {
        long now = System.currentTimeMillis();
        while (bundle != null) {
            if (bundle.expired) {
                return f75-assertionsDisabled;
            }
            CacheKey key = bundle.cacheKey;
            if (key != null) {
                long expirationTime = key.expirationTime;
                if (expirationTime >= 0 && expirationTime <= now) {
                    return f75-assertionsDisabled;
                }
            }
            bundle = bundle.parent;
        }
        return true;
    }

    private static final void throwMissingResourceException(String baseName, Locale locale, Throwable cause) {
        if (cause instanceof MissingResourceException) {
            cause = null;
        }
        throw new MissingResourceException("Can't find bundle for base name " + baseName + ", locale " + locale, baseName + BaseLocale.SEP + locale, "", cause);
    }

    private static final ResourceBundle findBundleInCache(CacheKey cacheKey, Control control) {
        boolean z = f75-assertionsDisabled;
        BundleReference bundleRef = (BundleReference) cacheList.get(cacheKey);
        if (bundleRef == null) {
            return null;
        }
        ResourceBundle bundle = (ResourceBundle) bundleRef.get();
        if (bundle == null) {
            return null;
        }
        ResourceBundle p = bundle.parent;
        if (!f75-assertionsDisabled) {
            if (!(p != NONEXISTENT_BUNDLE)) {
                throw new AssertionError();
            }
        }
        if (p == null || !p.expired) {
            CacheKey key = bundleRef.getCacheKey();
            long expirationTime = key.expirationTime;
            if (!bundle.expired && expirationTime >= 0 && expirationTime <= System.currentTimeMillis()) {
                if (bundle != NONEXISTENT_BUNDLE) {
                    synchronized (bundle) {
                        expirationTime = key.expirationTime;
                        if (!bundle.expired && expirationTime >= 0 && expirationTime <= System.currentTimeMillis()) {
                            try {
                                bundle.expired = control.needsReload(key.getName(), key.getLocale(), key.getFormat(), key.getLoader(), bundle, key.loadTime);
                            } catch (Exception e) {
                                cacheKey.setCause(e);
                            }
                            if (bundle.expired) {
                                bundle.cacheKey = null;
                                cacheList.remove(cacheKey, bundleRef);
                            } else {
                                setExpirationTime(key, control);
                            }
                        }
                    }
                } else {
                    cacheList.remove(cacheKey, bundleRef);
                    bundle = null;
                }
            }
        } else {
            if (!f75-assertionsDisabled) {
                if (bundle != NONEXISTENT_BUNDLE) {
                    z = true;
                }
                if (!z) {
                    throw new AssertionError();
                }
            }
            bundle.expired = true;
            bundle.cacheKey = null;
            cacheList.remove(cacheKey, bundleRef);
            bundle = null;
        }
        return bundle;
    }

    private static final ResourceBundle putBundleInCache(CacheKey cacheKey, ResourceBundle bundle, Control control) {
        setExpirationTime(cacheKey, control);
        if (cacheKey.expirationTime == -1) {
            return bundle;
        }
        CacheKey key = (CacheKey) cacheKey.clone();
        BundleReference bundleRef = new BundleReference(bundle, referenceQueue, key);
        bundle.cacheKey = key;
        BundleReference result = (BundleReference) cacheList.putIfAbsent(key, bundleRef);
        if (result == null) {
            return bundle;
        }
        ResourceBundle rb = (ResourceBundle) result.get();
        if (rb == null || rb.expired) {
            cacheList.put(key, bundleRef);
            return bundle;
        }
        bundle.cacheKey = null;
        bundle = rb;
        bundleRef.clear();
        return bundle;
    }

    private static final void setExpirationTime(CacheKey cacheKey, Control control) {
        long ttl = control.getTimeToLive(cacheKey.getName(), cacheKey.getLocale());
        if (ttl >= 0) {
            long now = System.currentTimeMillis();
            cacheKey.loadTime = now;
            cacheKey.expirationTime = now + ttl;
        } else if (ttl >= -2) {
            cacheKey.expirationTime = ttl;
        } else {
            throw new IllegalArgumentException("Invalid Control: TTL=" + ttl);
        }
    }

    @CallerSensitive
    public static final void clearCache() {
        clearCache(getLoader(VMStack.getCallingClassLoader()));
    }

    public static final void clearCache(ClassLoader loader) {
        if (loader == null) {
            throw new NullPointerException();
        }
        Set<CacheKey> set = cacheList.keySet();
        for (CacheKey key : set) {
            if (key.getLoader() == loader) {
                set.remove(key);
            }
        }
    }

    public boolean containsKey(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        for (ResourceBundle rb = this; rb != null; rb = rb.parent) {
            if (rb.handleKeySet().contains(key)) {
                return true;
            }
        }
        return f75-assertionsDisabled;
    }

    public Set<String> keySet() {
        Set<String> keys = new HashSet();
        for (ResourceBundle rb = this; rb != null; rb = rb.parent) {
            keys.addAll(rb.handleKeySet());
        }
        return keys;
    }

    protected Set<String> handleKeySet() {
        if (this.keySet == null) {
            synchronized (this) {
                if (this.keySet == null) {
                    Set<String> keys = new HashSet();
                    Enumeration<String> enumKeys = getKeys();
                    while (enumKeys.hasMoreElements()) {
                        String key = (String) enumKeys.nextElement();
                        if (handleGetObject(key) != null) {
                            keys.add(key);
                        }
                    }
                    this.keySet = keys;
                }
            }
        }
        return this.keySet;
    }
}
