package android.icu.util;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.ICUResourceBundleReader;
import android.icu.impl.ResourceBundleWrapper;
import android.icu.impl.SimpleCache;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

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
public abstract class UResourceBundle extends ResourceBundle {
    public static final int ARRAY = 8;
    public static final int BINARY = 1;
    private static ICUCache<ResourceCacheKey, UResourceBundle> BUNDLE_CACHE = null;
    public static final int INT = 7;
    public static final int INT_VECTOR = 14;
    public static final int NONE = -1;
    private static SoftReference<ConcurrentHashMap<String, Integer>> ROOT_CACHE = null;
    private static final int ROOT_ICU = 1;
    private static final int ROOT_JAVA = 2;
    private static final int ROOT_MISSING = 0;
    public static final int STRING = 0;
    public static final int TABLE = 2;
    private static final ResourceCacheKey cacheKey = null;

    private static final class ResourceCacheKey implements Cloneable {
        private ULocale defaultLocale;
        private int hashCodeCache;
        private String searchName;

        /* synthetic */ ResourceCacheKey(ResourceCacheKey resourceCacheKey) {
            this();
        }

        private ResourceCacheKey() {
        }

        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (this == other) {
                return true;
            }
            try {
                ResourceCacheKey otherEntry = (ResourceCacheKey) other;
                if (this.hashCodeCache != otherEntry.hashCodeCache || !this.searchName.equals(otherEntry.searchName)) {
                    return false;
                }
                if (this.defaultLocale == null) {
                    if (otherEntry.defaultLocale != null) {
                        return false;
                    }
                } else if (!this.defaultLocale.equals(otherEntry.defaultLocale)) {
                    return false;
                }
                return true;
            } catch (NullPointerException e) {
                return false;
            } catch (ClassCastException e2) {
                return false;
            }
        }

        public int hashCode() {
            return this.hashCodeCache;
        }

        public Object clone() {
            try {
                return super.clone();
            } catch (Throwable e) {
                throw new ICUCloneNotSupportedException(e);
            }
        }

        private synchronized void setKeyValues(String searchName, ULocale defaultLocale) {
            this.searchName = searchName;
            this.hashCodeCache = searchName.hashCode();
            this.defaultLocale = defaultLocale;
            if (defaultLocale != null) {
                this.hashCodeCache ^= defaultLocale.hashCode();
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.util.UResourceBundle.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.util.UResourceBundle.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.UResourceBundle.<clinit>():void");
    }

    protected abstract String getBaseName();

    protected abstract String getLocaleID();

    protected abstract UResourceBundle getParent();

    public abstract ULocale getULocale();

    @Deprecated
    protected abstract void setLoadingStatus(int i);

    public static UResourceBundle getBundleInstance(String baseName, String localeName) {
        return getBundleInstance(baseName, localeName, ICUResourceBundle.ICU_DATA_CLASS_LOADER, false);
    }

    public static UResourceBundle getBundleInstance(String baseName, String localeName, ClassLoader root) {
        return getBundleInstance(baseName, localeName, root, false);
    }

    protected static UResourceBundle getBundleInstance(String baseName, String localeName, ClassLoader root, boolean disableFallback) {
        return instantiateBundle(baseName, localeName, root, disableFallback);
    }

    public static UResourceBundle getBundleInstance(ULocale locale) {
        if (locale == null) {
            locale = ULocale.getDefault();
        }
        return getBundleInstance("android/icu/impl/data/icudt56b", locale.toString(), ICUResourceBundle.ICU_DATA_CLASS_LOADER, false);
    }

    public static UResourceBundle getBundleInstance(String baseName) {
        if (baseName == null) {
            baseName = "android/icu/impl/data/icudt56b";
        }
        return getBundleInstance(baseName, ULocale.getDefault().toString(), ICUResourceBundle.ICU_DATA_CLASS_LOADER, false);
    }

    public static UResourceBundle getBundleInstance(String baseName, Locale locale) {
        if (baseName == null) {
            baseName = "android/icu/impl/data/icudt56b";
        }
        return getBundleInstance(baseName, (locale == null ? ULocale.getDefault() : ULocale.forLocale(locale)).toString(), ICUResourceBundle.ICU_DATA_CLASS_LOADER, false);
    }

    public static UResourceBundle getBundleInstance(String baseName, ULocale locale) {
        if (baseName == null) {
            baseName = "android/icu/impl/data/icudt56b";
        }
        if (locale == null) {
            locale = ULocale.getDefault();
        }
        return getBundleInstance(baseName, locale.toString(), ICUResourceBundle.ICU_DATA_CLASS_LOADER, false);
    }

    public static UResourceBundle getBundleInstance(String baseName, Locale locale, ClassLoader loader) {
        if (baseName == null) {
            baseName = "android/icu/impl/data/icudt56b";
        }
        return getBundleInstance(baseName, (locale == null ? ULocale.getDefault() : ULocale.forLocale(locale)).toString(), loader, false);
    }

    public static UResourceBundle getBundleInstance(String baseName, ULocale locale, ClassLoader loader) {
        if (baseName == null) {
            baseName = "android/icu/impl/data/icudt56b";
        }
        if (locale == null) {
            locale = ULocale.getDefault();
        }
        return getBundleInstance(baseName, locale.toString(), loader, false);
    }

    public Locale getLocale() {
        return getULocale().toLocale();
    }

    @Deprecated
    public static void resetBundleCache() {
        BUNDLE_CACHE = new SimpleCache();
    }

    @Deprecated
    protected static UResourceBundle addToCache(String fullName, ULocale defaultLocale, UResourceBundle b) {
        synchronized (cacheKey) {
            cacheKey.setKeyValues(fullName, defaultLocale);
            UResourceBundle cachedBundle = (UResourceBundle) BUNDLE_CACHE.get(cacheKey);
            if (cachedBundle != null) {
                return cachedBundle;
            }
            BUNDLE_CACHE.put((ResourceCacheKey) cacheKey.clone(), b);
            return b;
        }
    }

    @Deprecated
    protected static UResourceBundle loadFromCache(String fullName, ULocale defaultLocale) {
        UResourceBundle uResourceBundle;
        synchronized (cacheKey) {
            cacheKey.setKeyValues(fullName, defaultLocale);
            uResourceBundle = (UResourceBundle) BUNDLE_CACHE.get(cacheKey);
        }
        return uResourceBundle;
    }

    private static int getRootType(String baseName, ClassLoader root) {
        Throwable th;
        ConcurrentHashMap<String, Integer> m = (ConcurrentHashMap) ROOT_CACHE.get();
        if (m == null) {
            synchronized (UResourceBundle.class) {
                try {
                    m = (ConcurrentHashMap) ROOT_CACHE.get();
                    if (m == null) {
                        ConcurrentHashMap<String, Integer> m2 = new ConcurrentHashMap();
                        try {
                            ROOT_CACHE = new SoftReference(m2);
                            m = m2;
                        } catch (Throwable th2) {
                            th = th2;
                            m = m2;
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
        Integer rootType = (Integer) m.get(baseName);
        if (rootType == null) {
            String rootLocale = baseName.indexOf(46) == -1 ? "root" : "";
            int rt = 0;
            try {
                ICUResourceBundle.getBundleInstance(baseName, rootLocale, root, true);
                rt = 1;
            } catch (MissingResourceException e) {
                try {
                    ResourceBundleWrapper.getBundleInstance(baseName, rootLocale, root, true);
                    rt = 2;
                } catch (MissingResourceException e2) {
                }
            }
            rootType = Integer.valueOf(rt);
            m.putIfAbsent(baseName, rootType);
        }
        return rootType.intValue();
    }

    private static void setRootType(String baseName, int rootType) {
        Throwable th;
        Integer rt = Integer.valueOf(rootType);
        ConcurrentHashMap<String, Integer> m = (ConcurrentHashMap) ROOT_CACHE.get();
        if (m == null) {
            synchronized (UResourceBundle.class) {
                try {
                    m = (ConcurrentHashMap) ROOT_CACHE.get();
                    if (m == null) {
                        ConcurrentHashMap<String, Integer> m2 = new ConcurrentHashMap();
                        try {
                            ROOT_CACHE = new SoftReference(m2);
                            m = m2;
                        } catch (Throwable th2) {
                            th = th2;
                            m = m2;
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
        m.put(baseName, rt);
    }

    protected static UResourceBundle instantiateBundle(String baseName, String localeName, ClassLoader root, boolean disableFallback) {
        int rootType = getRootType(baseName, root);
        ULocale defaultLocale = ULocale.getDefault();
        UResourceBundle b;
        switch (rootType) {
            case 1:
                if (disableFallback) {
                    b = loadFromCache(ICUResourceBundleReader.getFullName(baseName, localeName), defaultLocale);
                    if (b == null) {
                        b = ICUResourceBundle.getBundleInstance(baseName, localeName, root, disableFallback);
                    }
                } else {
                    b = ICUResourceBundle.getBundleInstance(baseName, localeName, root, disableFallback);
                }
                return b;
            case 2:
                return ResourceBundleWrapper.getBundleInstance(baseName, localeName, root, disableFallback);
            default:
                try {
                    b = ICUResourceBundle.getBundleInstance(baseName, localeName, root, disableFallback);
                    setRootType(baseName, 1);
                } catch (MissingResourceException e) {
                    b = ResourceBundleWrapper.getBundleInstance(baseName, localeName, root, disableFallback);
                    setRootType(baseName, 2);
                }
                return b;
        }
    }

    public ByteBuffer getBinary() {
        throw new UResourceTypeMismatchException("");
    }

    public String getString() {
        throw new UResourceTypeMismatchException("");
    }

    public String[] getStringArray() {
        throw new UResourceTypeMismatchException("");
    }

    public byte[] getBinary(byte[] ba) {
        throw new UResourceTypeMismatchException("");
    }

    public int[] getIntVector() {
        throw new UResourceTypeMismatchException("");
    }

    public int getInt() {
        throw new UResourceTypeMismatchException("");
    }

    public int getUInt() {
        throw new UResourceTypeMismatchException("");
    }

    public UResourceBundle get(String aKey) {
        UResourceBundle obj = findTopLevel(aKey);
        if (obj != null) {
            return obj;
        }
        throw new MissingResourceException("Can't find resource for bundle " + ICUResourceBundleReader.getFullName(getBaseName(), getLocaleID()) + ", key " + aKey, getClass().getName(), aKey);
    }

    @Deprecated
    protected UResourceBundle findTopLevel(String aKey) {
        for (UResourceBundle res = this; res != null; res = res.getParent()) {
            UResourceBundle obj = res.handleGet(aKey, null, this);
            if (obj != null) {
                ((ICUResourceBundle) obj).setLoadingStatus(getLocaleID());
                return obj;
            }
        }
        return null;
    }

    public String getString(int index) {
        ICUResourceBundle temp = (ICUResourceBundle) get(index);
        if (temp.getType() == 0) {
            return temp.getString();
        }
        throw new UResourceTypeMismatchException("");
    }

    public UResourceBundle get(int index) {
        UResourceBundle obj = handleGet(index, null, this);
        if (obj == null) {
            obj = (ICUResourceBundle) getParent();
            if (obj != null) {
                obj = obj.get(index);
            }
            if (obj == null) {
                throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + getKey(), getClass().getName(), getKey());
            }
        }
        ((ICUResourceBundle) obj).setLoadingStatus(getLocaleID());
        return obj;
    }

    @Deprecated
    protected UResourceBundle findTopLevel(int index) {
        for (UResourceBundle res = this; res != null; res = res.getParent()) {
            UResourceBundle obj = res.handleGet(index, null, this);
            if (obj != null) {
                ((ICUResourceBundle) obj).setLoadingStatus(getLocaleID());
                return obj;
            }
        }
        return null;
    }

    public Enumeration<String> getKeys() {
        return Collections.enumeration(keySet());
    }

    @Deprecated
    public Set<String> keySet() {
        Set<String> keys = null;
        ICUResourceBundle icurb = null;
        if (isTopLevelResource() && (this instanceof ICUResourceBundle)) {
            icurb = (ICUResourceBundle) this;
            keys = icurb.getTopLevelKeySet();
        }
        if (keys == null) {
            if (!isTopLevelResource()) {
                return handleKeySet();
            }
            TreeSet<String> newKeySet;
            if (this.parent == null) {
                newKeySet = new TreeSet();
            } else if (this.parent instanceof UResourceBundle) {
                newKeySet = new TreeSet(((UResourceBundle) this.parent).keySet());
            } else {
                newKeySet = new TreeSet();
                Enumeration<String> parentKeys = this.parent.getKeys();
                while (parentKeys.hasMoreElements()) {
                    newKeySet.add((String) parentKeys.nextElement());
                }
            }
            newKeySet.addAll(handleKeySet());
            keys = Collections.unmodifiableSet(newKeySet);
            if (icurb != null) {
                icurb.setTopLevelKeySet(keys);
            }
        }
        return keys;
    }

    @Deprecated
    protected Set<String> handleKeySet() {
        return Collections.emptySet();
    }

    public int getSize() {
        return 1;
    }

    public int getType() {
        return -1;
    }

    public VersionInfo getVersion() {
        return null;
    }

    public UResourceBundleIterator getIterator() {
        return new UResourceBundleIterator(this);
    }

    public String getKey() {
        return null;
    }

    protected UResourceBundle handleGet(String aKey, HashMap<String, String> hashMap, UResourceBundle requested) {
        return null;
    }

    protected UResourceBundle handleGet(int index, HashMap<String, String> hashMap, UResourceBundle requested) {
        return null;
    }

    protected String[] handleGetStringArray() {
        return null;
    }

    protected Enumeration<String> handleGetKeys() {
        return null;
    }

    protected Object handleGetObject(String aKey) {
        return handleGetObjectImpl(aKey, this);
    }

    private Object handleGetObjectImpl(String aKey, UResourceBundle requested) {
        Object obj = resolveObject(aKey, requested);
        if (obj == null) {
            UResourceBundle parentBundle = getParent();
            if (parentBundle != null) {
                obj = parentBundle.handleGetObjectImpl(aKey, requested);
            }
            if (obj == null) {
                throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + aKey, getClass().getName(), aKey);
            }
        }
        return obj;
    }

    private Object resolveObject(String aKey, UResourceBundle requested) {
        if (getType() == 0) {
            return getString();
        }
        UResourceBundle obj = handleGet(aKey, null, requested);
        if (obj != null) {
            if (obj.getType() == 0) {
                return obj.getString();
            }
            try {
                if (obj.getType() == 8) {
                    return obj.handleGetStringArray();
                }
            } catch (UResourceTypeMismatchException e) {
                return obj;
            }
        }
        return obj;
    }

    @Deprecated
    protected boolean isTopLevelResource() {
        return true;
    }
}
