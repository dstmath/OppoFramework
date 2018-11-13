package android.icu.impl;

import android.icu.impl.URLHandler.URLVisitor;
import android.icu.impl.UResource.ArraySink;
import android.icu.impl.UResource.Key;
import android.icu.impl.UResource.TableSink;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;
import android.icu.util.UResourceTypeMismatchException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

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
public class ICUResourceBundle extends UResourceBundle {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f5-assertionsDisabled = false;
    public static final int ALIAS = 3;
    public static final int ARRAY16 = 9;
    private static final boolean DEBUG = false;
    private static final String DEFAULT_TAG = "default";
    public static final int FROM_DEFAULT = 3;
    public static final int FROM_FALLBACK = 1;
    public static final int FROM_LOCALE = 4;
    public static final int FROM_ROOT = 2;
    private static final String FULL_LOCALE_NAMES_LIST = "fullLocaleNames.lst";
    private static CacheBase<String, AvailEntry, ClassLoader> GET_AVAILABLE_CACHE = null;
    private static final char HYPHEN = '-';
    private static final String ICUDATA = "ICUDATA";
    @Deprecated
    public static final String ICU_BASE_NAME = "android/icu/impl/data/icudt56b";
    @Deprecated
    public static final String ICU_BRKITR_BASE_NAME = "android/icu/impl/data/icudt56b/brkitr";
    @Deprecated
    public static final String ICU_BUNDLE = "data/icudt56b";
    @Deprecated
    public static final String ICU_COLLATION_BASE_NAME = "android/icu/impl/data/icudt56b/coll";
    @Deprecated
    public static final String ICU_CURR_BASE_NAME = "android/icu/impl/data/icudt56b/curr";
    public static final ClassLoader ICU_DATA_CLASS_LOADER = null;
    @Deprecated
    protected static final String ICU_DATA_PATH = "android/icu/impl/";
    @Deprecated
    public static final String ICU_LANG_BASE_NAME = "android/icu/impl/data/icudt56b/lang";
    @Deprecated
    public static final String ICU_RBNF_BASE_NAME = "android/icu/impl/data/icudt56b/rbnf";
    @Deprecated
    public static final String ICU_REGION_BASE_NAME = "android/icu/impl/data/icudt56b/region";
    private static final String ICU_RESOURCE_INDEX = "res_index";
    @Deprecated
    public static final String ICU_TRANSLIT_BASE_NAME = "android/icu/impl/data/icudt56b/translit";
    @Deprecated
    public static final String ICU_ZONE_BASE_NAME = "android/icu/impl/data/icudt56b/zone";
    protected static final String INSTALLED_LOCALES = "InstalledLocales";
    private static final String LOCALE = "LOCALE";
    private static final String NO_INHERITANCE_MARKER = "∅∅∅";
    public static final int RES_BOGUS = -1;
    private static final char RES_PATH_SEP_CHAR = '/';
    private static final String RES_PATH_SEP_STR = "/";
    public static final int STRING_V2 = 6;
    public static final int TABLE16 = 5;
    public static final int TABLE32 = 4;
    private ICUResourceBundle container;
    protected String key;
    private int loadingStatus;
    WholeBundle wholeBundle;

    private static final class AvailEntry {
        private volatile Set<String> fullNameSet;
        private ClassLoader loader;
        private volatile Locale[] locales;
        private volatile Set<String> nameSet;
        private String prefix;
        private volatile ULocale[] ulocales;

        AvailEntry(String prefix, ClassLoader loader) {
            this.prefix = prefix;
            this.loader = loader;
        }

        ULocale[] getULocaleList() {
            if (this.ulocales == null) {
                synchronized (this) {
                    if (this.ulocales == null) {
                        this.ulocales = ICUResourceBundle.createULocaleList(this.prefix, this.loader);
                    }
                }
            }
            return this.ulocales;
        }

        Locale[] getLocaleList() {
            if (this.locales == null) {
                getULocaleList();
                synchronized (this) {
                    if (this.locales == null) {
                        this.locales = ICUResourceBundle.getLocaleList(this.ulocales);
                    }
                }
            }
            return this.locales;
        }

        Set<String> getLocaleNameSet() {
            if (this.nameSet == null) {
                synchronized (this) {
                    if (this.nameSet == null) {
                        this.nameSet = ICUResourceBundle.createLocaleNameSet(this.prefix, this.loader);
                    }
                }
            }
            return this.nameSet;
        }

        Set<String> getFullLocaleNameSet() {
            if (this.fullNameSet == null) {
                synchronized (this) {
                    if (this.fullNameSet == null) {
                        this.fullNameSet = ICUResourceBundle.createFullLocaleNameSet(this.prefix, this.loader);
                    }
                }
            }
            return this.fullNameSet;
        }
    }

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum OpenType {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.ICUResourceBundle.OpenType.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.ICUResourceBundle.OpenType.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUResourceBundle.OpenType.<clinit>():void");
        }
    }

    protected static final class WholeBundle {
        String baseName;
        ClassLoader loader;
        String localeID;
        ICUResourceBundleReader reader;
        Set<String> topLevelKeys;
        ULocale ulocale;

        WholeBundle(String baseName, String localeID, ClassLoader loader, ICUResourceBundleReader reader) {
            this.baseName = baseName;
            this.localeID = localeID;
            this.ulocale = new ULocale(localeID);
            this.loader = loader;
            this.reader = reader;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUResourceBundle.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUResourceBundle.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUResourceBundle.<clinit>():void");
    }

    public void setLoadingStatus(int newStatus) {
        this.loadingStatus = newStatus;
    }

    public int getLoadingStatus() {
        return this.loadingStatus;
    }

    public void setLoadingStatus(String requestedLocale) {
        String locale = getLocaleID();
        if (locale.equals("root")) {
            setLoadingStatus(2);
        } else if (locale.equals(requestedLocale)) {
            setLoadingStatus(4);
        } else {
            setLoadingStatus(1);
        }
    }

    public static final ULocale getFunctionalEquivalent(String baseName, ClassLoader loader, String resName, String keyword, ULocale locID, boolean[] isAvailable, boolean omitDefault) {
        ICUResourceBundle irb;
        String kwVal = locID.getKeywordValue(keyword);
        String baseLoc = locID.getBaseName();
        String defStr = null;
        ULocale parent = new ULocale(baseLoc);
        ULocale defLoc = null;
        boolean lookForDefault = false;
        ULocale fullBase = null;
        int defDepth = 0;
        int resDepth = 0;
        if (kwVal == null || kwVal.length() == 0 || kwVal.equals(DEFAULT_TAG)) {
            kwVal = "";
            lookForDefault = true;
        }
        ICUResourceBundle r = (ICUResourceBundle) UResourceBundle.getBundleInstance(baseName, parent);
        if (isAvailable != null) {
            isAvailable[0] = false;
            ULocale[] availableULocales = getAvailEntry(baseName, loader).getULocaleList();
            for (ULocale equals : availableULocales) {
                if (parent.equals(equals)) {
                    isAvailable[0] = true;
                    break;
                }
            }
        }
        do {
            try {
                defStr = ((ICUResourceBundle) r.get(resName)).getString(DEFAULT_TAG);
                if (lookForDefault) {
                    kwVal = defStr;
                    lookForDefault = false;
                }
                defLoc = r.getULocale();
            } catch (MissingResourceException e) {
            }
            if (defLoc == null) {
                r = (ICUResourceBundle) r.getParent();
                defDepth++;
            }
            if (r == null) {
                break;
            }
        } while (defLoc == null);
        r = (ICUResourceBundle) UResourceBundle.getBundleInstance(baseName, new ULocale(baseLoc));
        do {
            try {
                irb = (ICUResourceBundle) r.get(resName);
                irb.get(kwVal);
                fullBase = irb.getULocale();
                if (fullBase != null && resDepth > defDepth) {
                    defStr = irb.getString(DEFAULT_TAG);
                    defLoc = r.getULocale();
                    defDepth = resDepth;
                }
            } catch (MissingResourceException e2) {
            }
            if (fullBase == null) {
                r = (ICUResourceBundle) r.getParent();
                resDepth++;
            }
            if (r == null) {
                break;
            }
        } while (fullBase == null);
        if (fullBase == null && defStr != null && !defStr.equals(kwVal)) {
            kwVal = defStr;
            r = (ICUResourceBundle) UResourceBundle.getBundleInstance(baseName, new ULocale(baseLoc));
            resDepth = 0;
            while (true) {
                try {
                    irb = (ICUResourceBundle) r.get(resName);
                    UResourceBundle urb = irb.get(kwVal);
                    fullBase = r.getULocale();
                    if (!fullBase.toString().equals(urb.getLocale().toString())) {
                        fullBase = null;
                    }
                    if (fullBase != null && resDepth > defDepth) {
                        defStr = irb.getString(DEFAULT_TAG);
                        defLoc = r.getULocale();
                        defDepth = resDepth;
                    }
                } catch (MissingResourceException e3) {
                }
                if (fullBase == null) {
                    r = (ICUResourceBundle) r.getParent();
                    resDepth++;
                }
                if (r == null || fullBase != null) {
                    break;
                }
            }
        }
        if (fullBase == null) {
            throw new MissingResourceException("Could not find locale containing requested or default keyword.", baseName, keyword + "=" + kwVal);
        } else if (omitDefault && defStr.equals(kwVal) && resDepth <= defDepth) {
            return fullBase;
        } else {
            return new ULocale(fullBase.toString() + "@" + keyword + "=" + kwVal);
        }
    }

    public static final String[] getKeywordValues(String baseName, String keyword) {
        Set<String> keywords = new HashSet();
        ULocale[] locales = getAvailEntry(baseName, ICU_DATA_CLASS_LOADER).getULocaleList();
        for (ULocale bundleInstance : locales) {
            try {
                Enumeration<String> e = ((ICUResourceBundle) UResourceBundle.getBundleInstance(baseName, bundleInstance).getObject(keyword)).getKeys();
                while (e.hasMoreElements()) {
                    String s = (String) e.nextElement();
                    if (!(DEFAULT_TAG.equals(s) || s.startsWith("private-"))) {
                        keywords.add(s);
                    }
                }
            } catch (Throwable th) {
            }
        }
        return (String[]) keywords.toArray(new String[0]);
    }

    public ICUResourceBundle getWithFallback(String path) throws MissingResourceException {
        ICUResourceBundle result = findResourceWithFallback(path, this, null);
        if (result == null) {
            throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + getType(), path, getKey());
        } else if (result.getType() != 0 || !result.getString().equals(NO_INHERITANCE_MARKER)) {
            return result;
        } else {
            throw new MissingResourceException("Encountered NO_INHERITANCE_MARKER", path, getKey());
        }
    }

    public ICUResourceBundle at(int index) {
        return (ICUResourceBundle) handleGet(index, null, (UResourceBundle) this);
    }

    public ICUResourceBundle at(String key) {
        if (this instanceof ResourceTable) {
            return (ICUResourceBundle) handleGet(key, null, (UResourceBundle) this);
        }
        return null;
    }

    public ICUResourceBundle findTopLevel(int index) {
        return (ICUResourceBundle) super.findTopLevel(index);
    }

    public ICUResourceBundle findTopLevel(String aKey) {
        return (ICUResourceBundle) super.findTopLevel(aKey);
    }

    public ICUResourceBundle findWithFallback(String path) {
        return findResourceWithFallback(path, this, null);
    }

    public String findStringWithFallback(String path) {
        return findStringWithFallback(path, this, null);
    }

    public String getStringWithFallback(String path) throws MissingResourceException {
        String result = findStringWithFallback(path, this, null);
        if (result == null) {
            throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + getType(), path, getKey());
        } else if (!result.equals(NO_INHERITANCE_MARKER)) {
            return result;
        } else {
            throw new MissingResourceException("Encountered NO_INHERITANCE_MARKER", path, getKey());
        }
    }

    public void getAllArrayItemsWithFallback(String path, ArraySink sink) throws MissingResourceException {
        getAllContainerItemsWithFallback(path, sink, null);
    }

    public void getAllTableItemsWithFallback(String path, TableSink sink) throws MissingResourceException {
        getAllContainerItemsWithFallback(path, null, sink);
    }

    private void getAllContainerItemsWithFallback(String path, ArraySink arraySink, TableSink tableSink) throws MissingResourceException {
        ICUResourceBundle rb;
        int numPathKeys = countPathKeys(path);
        if (numPathKeys == 0) {
            rb = this;
        } else {
            int depth = getResDepth();
            String[] pathKeys = new String[(depth + numPathKeys)];
            getResPathKeys(path, numPathKeys, pathKeys, depth);
            rb = findResourceWithFallback(pathKeys, depth, this, null);
            if (rb == null) {
                throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + getType(), path, getKey());
            }
        }
        if (rb.getType() != (arraySink != null ? 8 : 2)) {
            throw new UResourceTypeMismatchException("");
        }
        rb.getAllContainerItemsWithFallback(new Key(), new ReaderValue(), arraySink, tableSink);
    }

    private void getAllContainerItemsWithFallback(Key key, ReaderValue readerValue, ArraySink arraySink, TableSink tableSink) {
        int expectedType = arraySink != null ? 8 : 2;
        if (getType() == expectedType) {
            if (arraySink != null) {
                ((ResourceArray) this).getAllItems(key, readerValue, arraySink);
            } else {
                ((ResourceTable) this).getAllItems(key, readerValue, tableSink);
            }
        }
        if (this.parent != null) {
            ICUResourceBundle rb;
            ICUResourceBundle parentBundle = this.parent;
            int depth = getResDepth();
            if (depth == 0) {
                rb = parentBundle;
            } else {
                String[] pathKeys = new String[depth];
                getResPathKeys(pathKeys, depth);
                rb = findResourceWithFallback(pathKeys, 0, parentBundle, null);
            }
            if (rb != null && rb.getType() == expectedType) {
                rb.getAllContainerItemsWithFallback(key, readerValue, arraySink, tableSink);
            }
        }
    }

    public static Set<String> getAvailableLocaleNameSet(String bundlePrefix, ClassLoader loader) {
        return getAvailEntry(bundlePrefix, loader).getLocaleNameSet();
    }

    public static Set<String> getFullLocaleNameSet() {
        return getFullLocaleNameSet("android/icu/impl/data/icudt56b", ICU_DATA_CLASS_LOADER);
    }

    public static Set<String> getFullLocaleNameSet(String bundlePrefix, ClassLoader loader) {
        return getAvailEntry(bundlePrefix, loader).getFullLocaleNameSet();
    }

    public static Set<String> getAvailableLocaleNameSet() {
        return getAvailableLocaleNameSet("android/icu/impl/data/icudt56b", ICU_DATA_CLASS_LOADER);
    }

    public static final ULocale[] getAvailableULocales(String baseName, ClassLoader loader) {
        return getAvailEntry(baseName, loader).getULocaleList();
    }

    public static final ULocale[] getAvailableULocales() {
        return getAvailableULocales("android/icu/impl/data/icudt56b", ICU_DATA_CLASS_LOADER);
    }

    public static final Locale[] getAvailableLocales(String baseName, ClassLoader loader) {
        return getAvailEntry(baseName, loader).getLocaleList();
    }

    public static final Locale[] getAvailableLocales() {
        return getAvailEntry("android/icu/impl/data/icudt56b", ICU_DATA_CLASS_LOADER).getLocaleList();
    }

    public static final Locale[] getLocaleList(ULocale[] ulocales) {
        ArrayList<Locale> list = new ArrayList(ulocales.length);
        HashSet<Locale> uniqueSet = new HashSet();
        for (ULocale toLocale : ulocales) {
            Locale loc = toLocale.toLocale();
            if (!uniqueSet.contains(loc)) {
                list.add(loc);
                uniqueSet.add(loc);
            }
        }
        return (Locale[]) list.toArray(new Locale[list.size()]);
    }

    public Locale getLocale() {
        return getULocale().toLocale();
    }

    private static final ULocale[] createULocaleList(String baseName, ClassLoader root) {
        ICUResourceBundle bundle = (ICUResourceBundle) ((ICUResourceBundle) UResourceBundle.instantiateBundle(baseName, ICU_RESOURCE_INDEX, root, true)).get(INSTALLED_LOCALES);
        int i = 0;
        ULocale[] locales = new ULocale[bundle.getSize()];
        UResourceBundleIterator iter = bundle.getIterator();
        iter.reset();
        while (iter.hasNext()) {
            String locstr = iter.next().getKey();
            int i2;
            if (locstr.equals("root")) {
                i2 = i + 1;
                locales[i] = ULocale.ROOT;
                i = i2;
            } else {
                i2 = i + 1;
                locales[i] = new ULocale(locstr);
                i = i2;
            }
        }
        return locales;
    }

    private static final void addLocaleIDsFromIndexBundle(String baseName, ClassLoader root, Set<String> locales) {
        try {
            UResourceBundleIterator iter = ((ICUResourceBundle) ((ICUResourceBundle) UResourceBundle.instantiateBundle(baseName, ICU_RESOURCE_INDEX, root, true)).get(INSTALLED_LOCALES)).getIterator();
            iter.reset();
            while (iter.hasNext()) {
                locales.add(iter.next().getKey());
            }
        } catch (MissingResourceException e) {
            if (DEBUG) {
                System.out.println("couldn't find " + baseName + RES_PATH_SEP_CHAR + ICU_RESOURCE_INDEX + ".res");
                Thread.dumpStack();
            }
        }
    }

    private static final void addBundleBaseNamesFromClassLoader(final String bn, final ClassLoader root, final Set<String> names) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                try {
                    Enumeration<URL> urls = root.getResources(bn);
                    if (urls == null) {
                        return null;
                    }
                    final Set set = names;
                    URLVisitor v = new URLVisitor() {
                        public void visit(String s) {
                            if (s.endsWith(".res")) {
                                set.add(s.substring(0, s.length() - 4));
                            }
                        }
                    };
                    while (urls.hasMoreElements()) {
                        URL url = (URL) urls.nextElement();
                        URLHandler handler = URLHandler.get(url);
                        if (handler != null) {
                            handler.guide(v, false);
                        } else if (ICUResourceBundle.DEBUG) {
                            System.out.println("handler for " + url + " is null");
                        }
                    }
                    return null;
                } catch (IOException e) {
                    if (ICUResourceBundle.DEBUG) {
                        System.out.println("ouch: " + e.getMessage());
                    }
                }
            }
        });
    }

    private static void addLocaleIDsFromListFile(String bn, ClassLoader root, Set<String> locales) {
        try {
            InputStream s = root.getResourceAsStream(bn + FULL_LOCALE_NAMES_LIST);
            if (s != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(s, "ASCII"));
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        br.close();
                        return;
                    } else if (!(line.length() == 0 || line.startsWith("#"))) {
                        locales.add(line);
                    }
                }
            }
        } catch (IOException e) {
        }
    }

    private static Set<String> createFullLocaleNameSet(String baseName, ClassLoader loader) {
        String bn = baseName.endsWith(RES_PATH_SEP_STR) ? baseName : baseName + RES_PATH_SEP_STR;
        Set<String> set = new HashSet();
        if (!ICUConfig.get("android.icu.impl.ICUResourceBundle.skipRuntimeLocaleResourceScan", "false").equalsIgnoreCase("true")) {
            addBundleBaseNamesFromClassLoader(bn, loader, set);
            if (baseName.startsWith("android/icu/impl/data/icudt56b")) {
                String folder;
                if (baseName.length() == "android/icu/impl/data/icudt56b".length()) {
                    folder = "";
                } else if (baseName.charAt("android/icu/impl/data/icudt56b".length()) == RES_PATH_SEP_CHAR) {
                    folder = baseName.substring("android/icu/impl/data/icudt56b".length() + 1);
                } else {
                    folder = null;
                }
                if (folder != null) {
                    ICUBinary.addBaseNamesInFileFolder(folder, ".res", set);
                }
            }
            set.remove(ICU_RESOURCE_INDEX);
            Iterator<String> iter = set.iterator();
            while (iter.hasNext()) {
                String name = (String) iter.next();
                if ((name.length() == 1 || name.length() > 3) && name.indexOf(95) < 0) {
                    iter.remove();
                }
            }
        }
        if (set.isEmpty()) {
            if (DEBUG) {
                System.out.println("unable to enumerate data files in " + baseName);
            }
            addLocaleIDsFromListFile(bn, loader, set);
        }
        if (set.isEmpty()) {
            addLocaleIDsFromIndexBundle(baseName, loader, set);
        }
        set.remove("root");
        set.add(ULocale.ROOT.toString());
        return Collections.unmodifiableSet(set);
    }

    private static Set<String> createLocaleNameSet(String baseName, ClassLoader loader) {
        HashSet<String> set = new HashSet();
        addLocaleIDsFromIndexBundle(baseName, loader, set);
        return Collections.unmodifiableSet(set);
    }

    private static AvailEntry getAvailEntry(String key, ClassLoader loader) {
        return (AvailEntry) GET_AVAILABLE_CACHE.getInstance(key, loader);
    }

    private static final ICUResourceBundle findResourceWithFallback(String path, UResourceBundle actualBundle, UResourceBundle requested) {
        Object obj = null;
        if (path.length() == 0) {
            return null;
        }
        ICUResourceBundle base = (ICUResourceBundle) actualBundle;
        int depth = base.getResDepth();
        int numPathKeys = countPathKeys(path);
        if (!f5-assertionsDisabled) {
            if (numPathKeys > 0) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        String[] keys = new String[(depth + numPathKeys)];
        getResPathKeys(path, numPathKeys, keys, depth);
        return findResourceWithFallback(keys, depth, base, requested);
    }

    private static final ICUResourceBundle findResourceWithFallback(String[] keys, int depth, ICUResourceBundle base, UResourceBundle requested) {
        if (requested == null) {
            requested = base;
        }
        while (true) {
            int depth2 = depth + 1;
            ICUResourceBundle sub = (ICUResourceBundle) base.handleGet(keys[depth], null, requested);
            if (sub == null) {
                depth = depth2 - 1;
                ICUResourceBundle nextBase = (ICUResourceBundle) base.getParent();
                if (nextBase == null) {
                    return null;
                }
                int baseDepth = base.getResDepth();
                if (depth != baseDepth) {
                    String[] newKeys = new String[((keys.length - depth) + baseDepth)];
                    System.arraycopy(keys, depth, newKeys, baseDepth, keys.length - depth);
                    keys = newKeys;
                }
                base.getResPathKeys(keys, baseDepth);
                base = nextBase;
                depth = 0;
            } else if (depth2 == keys.length) {
                sub.setLoadingStatus(((ICUResourceBundle) requested).getLocaleID());
                return sub;
            } else {
                base = sub;
                depth = depth2;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:60:0x00dc  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x006d A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x006d A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00dc  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static final String findStringWithFallback(String path, UResourceBundle actualBundle, UResourceBundle requested) {
        if (path.length() == 0) {
            return null;
        }
        if (!(actualBundle instanceof ResourceContainer)) {
            return null;
        }
        if (requested == null) {
            requested = actualBundle;
        }
        ICUResourceBundle base = (ICUResourceBundle) actualBundle;
        ICUResourceBundleReader reader = base.wholeBundle.reader;
        int res = -1;
        int baseDepth = base.getResDepth();
        int depth = baseDepth;
        int numPathKeys = countPathKeys(path);
        if (!f5-assertionsDisabled) {
            if ((numPathKeys > 0 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        String[] keys = new String[(baseDepth + numPathKeys)];
        getResPathKeys(path, numPathKeys, keys, baseDepth);
        while (true) {
            Container readerContainer;
            ICUResourceBundle nextBase;
            int depth2 = depth;
            int type;
            if (res == -1) {
                type = base.getType();
                if (type == 2 || type == 8) {
                    readerContainer = ((ResourceContainer) base).value;
                } else {
                    nextBase = (ICUResourceBundle) base.getParent();
                    if (nextBase == null) {
                        return null;
                    }
                    base.getResPathKeys(keys, baseDepth);
                    base = nextBase;
                    reader = nextBase.wholeBundle.reader;
                    baseDepth = 0;
                    depth = 0;
                }
            } else {
                type = ICUResourceBundleReader.RES_GET_TYPE(res);
                if (ICUResourceBundleReader.URES_IS_TABLE(type)) {
                    readerContainer = reader.getTable(res);
                } else if (ICUResourceBundleReader.URES_IS_ARRAY(type)) {
                    readerContainer = reader.getArray(res);
                } else {
                    res = -1;
                    depth = depth2;
                    nextBase = (ICUResourceBundle) base.getParent();
                    if (nextBase == null) {
                    }
                }
            }
            depth = depth2 + 1;
            String subKey = keys[depth2];
            res = readerContainer.getResource(reader, subKey);
            if (res == -1) {
                depth--;
                nextBase = (ICUResourceBundle) base.getParent();
                if (nextBase == null) {
                }
            } else {
                ICUResourceBundle sub;
                if (ICUResourceBundleReader.RES_GET_TYPE(res) == 3) {
                    base.getResPathKeys(keys, baseDepth);
                    sub = getAliasedResource(base, keys, depth, subKey, res, null, requested);
                } else {
                    sub = null;
                }
                if (depth == keys.length) {
                    if (sub != null) {
                        return sub.getString();
                    }
                    String s = reader.getString(res);
                    if (s != null) {
                        return s;
                    }
                    throw new UResourceTypeMismatchException("");
                } else if (sub != null) {
                    base = sub;
                    reader = sub.wholeBundle.reader;
                    res = -1;
                    baseDepth = base.getResDepth();
                    if (depth != baseDepth) {
                        String[] newKeys = new String[((keys.length - depth) + baseDepth)];
                        System.arraycopy(keys, depth, newKeys, baseDepth, keys.length - depth);
                        keys = newKeys;
                        depth = baseDepth;
                    }
                }
            }
        }
    }

    private int getResDepth() {
        return this.container == null ? 0 : this.container.getResDepth() + 1;
    }

    private void getResPathKeys(String[] keys, int depth) {
        ICUResourceBundle b = this;
        while (depth > 0) {
            depth--;
            keys[depth] = b.key;
            b = b.container;
            if (!f5-assertionsDisabled) {
                if (((depth == 0 ? 1 : null) == (b.container == null ? 1 : null) ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
        }
    }

    private static int countPathKeys(String path) {
        if (path.length() == 0) {
            return 0;
        }
        int num = 1;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == RES_PATH_SEP_CHAR) {
                num++;
            }
        }
        return num;
    }

    private static void getResPathKeys(String path, int num, String[] keys, int start) {
        Object obj = 1;
        if (num != 0) {
            if (num == 1) {
                keys[start] = path;
                return;
            }
            int i = 0;
            while (true) {
                int j = path.indexOf(47, i);
                if (!f5-assertionsDisabled) {
                    if ((j >= i ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                int start2 = start + 1;
                keys[start] = path.substring(i, j);
                if (num == 2) {
                    if (!f5-assertionsDisabled) {
                        if (path.indexOf(47, j + 1) >= 0) {
                            obj = null;
                        }
                        if (obj == null) {
                            throw new AssertionError();
                        }
                    }
                    keys[start2] = path.substring(j + 1);
                    return;
                }
                i = j + 1;
                num--;
                start = start2;
            }
        }
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof ICUResourceBundle) {
            ICUResourceBundle o = (ICUResourceBundle) other;
            if (getBaseName().equals(o.getBaseName()) && getLocaleID().equals(o.getLocaleID())) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        if (f5-assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }

    public static UResourceBundle getBundleInstance(String baseName, String localeID, ClassLoader root, boolean disableFallback) {
        UResourceBundle b = instantiateBundle(baseName, localeID, root, disableFallback ? OpenType.DIRECT : OpenType.LOCALE_DEFAULT_ROOT);
        if (b != null) {
            return b;
        }
        throw new MissingResourceException("Could not find the bundle " + baseName + RES_PATH_SEP_STR + localeID + ".res", "", "");
    }

    protected static UResourceBundle instantiateBundle(String baseName, String localeID, ClassLoader root, boolean disableFallback) {
        return instantiateBundle(baseName, localeID, root, disableFallback ? OpenType.DIRECT : OpenType.LOCALE_DEFAULT_ROOT);
    }

    public static UResourceBundle getBundleInstance(String baseName, ULocale locale, OpenType openType) {
        if (locale == null) {
            locale = ULocale.getDefault();
        }
        return getBundleInstance(baseName, locale.toString(), ICU_DATA_CLASS_LOADER, openType);
    }

    public static UResourceBundle getBundleInstance(String baseName, String localeID, ClassLoader root, OpenType openType) {
        if (baseName == null) {
            baseName = "android/icu/impl/data/icudt56b";
        }
        UResourceBundle b = instantiateBundle(baseName, localeID, root, openType);
        if (b != null) {
            return b;
        }
        throw new MissingResourceException("Could not find the bundle " + baseName + RES_PATH_SEP_STR + localeID + ".res", "", "");
    }

    /* JADX WARNING: Missing block: B:42:0x0100, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static synchronized UResourceBundle instantiateBundle(String baseName, String localeID, ClassLoader root, OpenType openType) {
        synchronized (ICUResourceBundle.class) {
            ULocale defaultLocale = ULocale.getDefault();
            String localeName = localeID;
            if (localeID.indexOf(64) >= 0) {
                localeName = ULocale.getBaseName(localeID);
            }
            String fullName = ICUResourceBundleReader.getFullName(baseName, localeName);
            ICUResourceBundle b = (ICUResourceBundle) UResourceBundle.loadFromCache(fullName, defaultLocale);
            String rootLocale = baseName.indexOf(46) == -1 ? "root" : "";
            String defaultID = defaultLocale.getBaseName();
            if (localeName.equals("")) {
                localeName = rootLocale;
            }
            if (DEBUG) {
                System.out.println("Creating " + fullName + " currently b is " + b);
            }
            if (b == null) {
                b = createBundle(baseName, localeName, root);
                if (DEBUG) {
                    System.out.println("The bundle created is: " + b + " and openType=" + openType + " and bundle.getNoFallback=" + (b != null ? b.getNoFallback() : false));
                }
                int i;
                if (openType == OpenType.DIRECT || (b != null && b.getNoFallback())) {
                    UResourceBundle addToCache = UResourceBundle.addToCache(fullName, defaultLocale, b);
                    return addToCache;
                } else if (b == null) {
                    i = localeName.lastIndexOf(95);
                    if (i != -1) {
                        String temp = localeName.substring(0, i);
                        b = (ICUResourceBundle) instantiateBundle(baseName, temp, root, openType);
                        if (b != null && b.getULocale().getName().equals(temp)) {
                            b.setLoadingStatus(1);
                        }
                    } else if (openType == OpenType.LOCALE_DEFAULT_ROOT && !defaultLocale.getLanguage().equals(localeName)) {
                        b = (ICUResourceBundle) instantiateBundle(baseName, defaultID, root, openType);
                        if (b != null) {
                            b.setLoadingStatus(3);
                        }
                    } else if (rootLocale.length() != 0) {
                        b = createBundle(baseName, rootLocale, root);
                        if (b != null) {
                            b.setLoadingStatus(2);
                        }
                    }
                } else {
                    ResourceBundle parent = null;
                    localeName = b.getLocaleID();
                    i = localeName.lastIndexOf(95);
                    b = (ICUResourceBundle) UResourceBundle.addToCache(fullName, defaultLocale, b);
                    String parentLocaleName = ((ResourceTable) b).findString("%%Parent");
                    if (parentLocaleName != null) {
                        parent = instantiateBundle(baseName, parentLocaleName, root, openType);
                    } else if (i != -1) {
                        parent = instantiateBundle(baseName, localeName.substring(0, i), root, openType);
                    } else if (!localeName.equals(rootLocale)) {
                        parent = instantiateBundle(baseName, rootLocale, root, true);
                    }
                    if (!b.equals(parent)) {
                        b.setParent(parent);
                    }
                }
            }
        }
    }

    UResourceBundle get(String aKey, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
        ICUResourceBundle obj = (ICUResourceBundle) handleGet(aKey, (HashMap) aliasesVisited, requested);
        if (obj == null) {
            obj = (ICUResourceBundle) getParent();
            if (obj != null) {
                obj = (ICUResourceBundle) obj.get(aKey, aliasesVisited, requested);
            }
            if (obj == null) {
                throw new MissingResourceException("Can't find resource for bundle " + ICUResourceBundleReader.getFullName(getBaseName(), getLocaleID()) + ", key " + aKey, getClass().getName(), aKey);
            }
        }
        obj.setLoadingStatus(((ICUResourceBundle) requested).getLocaleID());
        return obj;
    }

    public static ICUResourceBundle createBundle(String baseName, String localeID, ClassLoader root) {
        ICUResourceBundleReader reader = ICUResourceBundleReader.getReader(baseName, localeID, root);
        if (reader == null) {
            return null;
        }
        return getBundle(reader, baseName, localeID, root);
    }

    protected String getLocaleID() {
        return this.wholeBundle.localeID;
    }

    protected String getBaseName() {
        return this.wholeBundle.baseName;
    }

    public ULocale getULocale() {
        return this.wholeBundle.ulocale;
    }

    public UResourceBundle getParent() {
        return (UResourceBundle) this.parent;
    }

    protected void setParent(ResourceBundle parent) {
        this.parent = parent;
    }

    public String getKey() {
        return this.key;
    }

    private boolean getNoFallback() {
        return this.wholeBundle.reader.getNoFallback();
    }

    private static ICUResourceBundle getBundle(ICUResourceBundleReader reader, String baseName, String localeID, ClassLoader loader) {
        int rootRes = reader.getRootResource();
        if (ICUResourceBundleReader.URES_IS_TABLE(ICUResourceBundleReader.RES_GET_TYPE(rootRes))) {
            ResourceTable rootTable = new ResourceTable(new WholeBundle(baseName, localeID, loader, reader), rootRes);
            String aliasString = rootTable.findString("%%ALIAS");
            if (aliasString != null) {
                return (ICUResourceBundle) UResourceBundle.getBundleInstance(baseName, aliasString);
            }
            return rootTable;
        }
        throw new IllegalStateException("Invalid format error");
    }

    protected ICUResourceBundle(WholeBundle wholeBundle) {
        this.loadingStatus = -1;
        this.wholeBundle = wholeBundle;
    }

    protected ICUResourceBundle(ICUResourceBundle container, String key) {
        this.loadingStatus = -1;
        this.key = key;
        this.wholeBundle = container.wholeBundle;
        this.container = (ResourceContainer) container;
        this.parent = container.parent;
    }

    protected static ICUResourceBundle getAliasedResource(ICUResourceBundle base, String[] keys, int depth, String key, int _resource, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
        WholeBundle wholeBundle = base.wholeBundle;
        ClassLoader loaderToUse = wholeBundle.loader;
        String keyPath = null;
        String rpath = wholeBundle.reader.getAlias(_resource);
        if (aliasesVisited == null) {
            aliasesVisited = new HashMap();
        }
        if (aliasesVisited.get(rpath) != null) {
            throw new IllegalArgumentException("Circular references in the resource bundles");
        }
        int i;
        String bundleName;
        String locale;
        aliasesVisited.put(rpath, "");
        if (rpath.indexOf(47) == 0) {
            i = rpath.indexOf(47, 1);
            int j = rpath.indexOf(47, i + 1);
            bundleName = rpath.substring(1, i);
            if (j < 0) {
                locale = rpath.substring(i + 1);
            } else {
                locale = rpath.substring(i + 1, j);
                keyPath = rpath.substring(j + 1, rpath.length());
            }
            if (bundleName.equals(ICUDATA)) {
                bundleName = "android/icu/impl/data/icudt56b";
                loaderToUse = ICU_DATA_CLASS_LOADER;
            } else if (bundleName.indexOf(ICUDATA) > -1) {
                int idx = bundleName.indexOf(45);
                if (idx > -1) {
                    bundleName = "android/icu/impl/data/icudt56b/" + bundleName.substring(idx + 1, bundleName.length());
                    loaderToUse = ICU_DATA_CLASS_LOADER;
                }
            }
        } else {
            i = rpath.indexOf(47);
            if (i != -1) {
                locale = rpath.substring(0, i);
                keyPath = rpath.substring(i + 1);
            } else {
                locale = rpath;
            }
            bundleName = wholeBundle.baseName;
        }
        ICUResourceBundle sub = null;
        ICUResourceBundle bundle;
        if (bundleName.equals(LOCALE)) {
            bundleName = wholeBundle.baseName;
            keyPath = rpath.substring(LOCALE.length() + 2, rpath.length());
            bundle = (ICUResourceBundle) requested;
            while (bundle.container != null) {
                bundle = bundle.container;
            }
            sub = findResourceWithFallback(keyPath, bundle, null);
        } else {
            int numKeys;
            if (locale == null) {
                bundle = (ICUResourceBundle) getBundleInstance(bundleName, "", loaderToUse, false);
            } else {
                bundle = (ICUResourceBundle) getBundleInstance(bundleName, locale, loaderToUse, false);
            }
            if (keyPath != null) {
                numKeys = countPathKeys(keyPath);
                if (numKeys > 0) {
                    keys = new String[numKeys];
                    getResPathKeys(keyPath, numKeys, keys, 0);
                }
            } else if (keys != null) {
                numKeys = depth;
            } else {
                depth = base.getResDepth();
                numKeys = depth + 1;
                keys = new String[numKeys];
                base.getResPathKeys(keys, depth);
                keys[depth] = key;
            }
            if (numKeys > 0) {
                sub = bundle;
                i = 0;
                while (sub != null && i < numKeys) {
                    sub = (ICUResourceBundle) sub.get(keys[i], aliasesVisited, requested);
                    i++;
                }
            }
        }
        if (sub != null) {
            return sub;
        }
        throw new MissingResourceException(wholeBundle.localeID, wholeBundle.baseName, key);
    }

    public final Set<String> getTopLevelKeySet() {
        return this.wholeBundle.topLevelKeys;
    }

    public final void setTopLevelKeySet(Set<String> keySet) {
        this.wholeBundle.topLevelKeys = keySet;
    }

    protected Enumeration<String> handleGetKeys() {
        return Collections.enumeration(handleKeySet());
    }

    protected boolean isTopLevelResource() {
        return this.container == null;
    }
}
