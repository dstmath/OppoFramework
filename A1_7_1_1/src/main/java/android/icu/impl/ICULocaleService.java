package android.icu.impl;

import android.icu.impl.ICUService.Factory;
import android.icu.impl.ICUService.Key;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ICULocaleService extends ICUService {
    private ULocale fallbackLocale;
    private String fallbackLocaleName;

    public static abstract class LocaleKeyFactory implements Factory {
        public static final boolean INVISIBLE = false;
        public static final boolean VISIBLE = true;
        protected final String name;
        protected final boolean visible;

        protected LocaleKeyFactory(boolean visible) {
            this.visible = visible;
            this.name = null;
        }

        protected LocaleKeyFactory(boolean visible, String name) {
            this.visible = visible;
            this.name = name;
        }

        public Object create(Key key, ICUService service) {
            if (!handlesKey(key)) {
                return null;
            }
            LocaleKey lkey = (LocaleKey) key;
            return handleCreate(lkey.currentLocale(), lkey.kind(), service);
        }

        protected boolean handlesKey(Key key) {
            if (key == null) {
                return false;
            }
            return getSupportedIDs().contains(key.currentID());
        }

        public void updateVisibleIDs(Map<String, Factory> result) {
            for (String id : getSupportedIDs()) {
                if (this.visible) {
                    result.put(id, this);
                } else {
                    result.remove(id);
                }
            }
        }

        public String getDisplayName(String id, ULocale locale) {
            if (locale == null) {
                return id;
            }
            return new ULocale(id).getDisplayName(locale);
        }

        protected Object handleCreate(ULocale loc, int kind, ICUService service) {
            return null;
        }

        protected boolean isSupportedID(String id) {
            return getSupportedIDs().contains(id);
        }

        protected Set<String> getSupportedIDs() {
            return Collections.emptySet();
        }

        public String toString() {
            StringBuilder buf = new StringBuilder(super.toString());
            if (this.name != null) {
                buf.append(", name: ");
                buf.append(this.name);
            }
            buf.append(", visible: ");
            buf.append(this.visible);
            return buf.toString();
        }
    }

    public static class ICUResourceBundleFactory extends LocaleKeyFactory {
        protected final String bundleName;

        public ICUResourceBundleFactory() {
            this("android/icu/impl/data/icudt56b");
        }

        public ICUResourceBundleFactory(String bundleName) {
            super(true);
            this.bundleName = bundleName;
        }

        protected Set<String> getSupportedIDs() {
            return ICUResourceBundle.getFullLocaleNameSet(this.bundleName, loader());
        }

        public void updateVisibleIDs(Map<String, Factory> result) {
            for (String id : ICUResourceBundle.getAvailableLocaleNameSet(this.bundleName, loader())) {
                result.put(id, this);
            }
        }

        protected Object handleCreate(ULocale loc, int kind, ICUService service) {
            return UResourceBundle.getBundleInstance(this.bundleName, loc, loader());
        }

        protected ClassLoader loader() {
            return ClassLoaderUtil.getClassLoader(getClass());
        }

        public String toString() {
            return super.toString() + ", bundle: " + this.bundleName;
        }
    }

    public static class LocaleKey extends Key {
        public static final int KIND_ANY = -1;
        private String currentID;
        private String fallbackID;
        private int kind;
        private String primaryID;
        private int varstart;

        public static LocaleKey createWithCanonicalFallback(String primaryID, String canonicalFallbackID) {
            return createWithCanonicalFallback(primaryID, canonicalFallbackID, -1);
        }

        public static LocaleKey createWithCanonicalFallback(String primaryID, String canonicalFallbackID, int kind) {
            if (primaryID == null) {
                return null;
            }
            return new LocaleKey(primaryID, ULocale.getName(primaryID), canonicalFallbackID, kind);
        }

        public static LocaleKey createWithCanonical(ULocale locale, String canonicalFallbackID, int kind) {
            if (locale == null) {
                return null;
            }
            String canonicalPrimaryID = locale.getName();
            return new LocaleKey(canonicalPrimaryID, canonicalPrimaryID, canonicalFallbackID, kind);
        }

        protected LocaleKey(String primaryID, String canonicalPrimaryID, String canonicalFallbackID, int kind) {
            String str;
            super(primaryID);
            this.kind = kind;
            if (canonicalPrimaryID == null || canonicalPrimaryID.equalsIgnoreCase("root")) {
                this.primaryID = "";
                this.fallbackID = null;
            } else {
                int idx = canonicalPrimaryID.indexOf(64);
                if (idx == 4) {
                    if (canonicalPrimaryID.regionMatches(true, 0, "root", 0, 4)) {
                        this.primaryID = canonicalPrimaryID.substring(4);
                        this.varstart = 0;
                        this.fallbackID = null;
                    }
                }
                this.primaryID = canonicalPrimaryID;
                this.varstart = idx;
                if (canonicalFallbackID == null || this.primaryID.equals(canonicalFallbackID)) {
                    this.fallbackID = "";
                } else {
                    this.fallbackID = canonicalFallbackID;
                }
            }
            if (this.varstart == -1) {
                str = this.primaryID;
            } else {
                str = this.primaryID.substring(0, this.varstart);
            }
            this.currentID = str;
        }

        public String prefix() {
            return this.kind == -1 ? null : Integer.toString(kind());
        }

        public int kind() {
            return this.kind;
        }

        public String canonicalID() {
            return this.primaryID;
        }

        public String currentID() {
            return this.currentID;
        }

        public String currentDescriptor() {
            String result = currentID();
            if (result == null) {
                return result;
            }
            StringBuilder buf = new StringBuilder();
            if (this.kind != -1) {
                buf.append(prefix());
            }
            buf.append('/');
            buf.append(result);
            if (this.varstart != -1) {
                buf.append(this.primaryID.substring(this.varstart, this.primaryID.length()));
            }
            return buf.toString();
        }

        public ULocale canonicalLocale() {
            return new ULocale(this.primaryID);
        }

        public ULocale currentLocale() {
            if (this.varstart == -1) {
                return new ULocale(this.currentID);
            }
            return new ULocale(this.currentID + this.primaryID.substring(this.varstart));
        }

        public boolean fallback() {
            int x = this.currentID.lastIndexOf(95);
            if (x != -1) {
                do {
                    x--;
                    if (x < 0) {
                        break;
                    }
                } while (this.currentID.charAt(x) == '_');
                this.currentID = this.currentID.substring(0, x + 1);
                return true;
            } else if (this.fallbackID != null) {
                this.currentID = this.fallbackID;
                if (this.fallbackID.length() == 0) {
                    this.fallbackID = null;
                } else {
                    this.fallbackID = "";
                }
                return true;
            } else {
                this.currentID = null;
                return false;
            }
        }

        public boolean isFallbackOf(String id) {
            return LocaleUtility.isFallbackOf(canonicalID(), id);
        }
    }

    public static class SimpleLocaleKeyFactory extends LocaleKeyFactory {
        private final String id;
        private final int kind;
        private final Object obj;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.<init>(java.lang.Object, android.icu.util.ULocale, int, boolean, java.lang.String):void, dex:  in method: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.<init>(java.lang.Object, android.icu.util.ULocale, int, boolean, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.<init>(java.lang.Object, android.icu.util.ULocale, int, boolean, java.lang.String):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public SimpleLocaleKeyFactory(java.lang.Object r1, android.icu.util.ULocale r2, int r3, boolean r4, java.lang.String r5) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.<init>(java.lang.Object, android.icu.util.ULocale, int, boolean, java.lang.String):void, dex:  in method: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.<init>(java.lang.Object, android.icu.util.ULocale, int, boolean, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.<init>(java.lang.Object, android.icu.util.ULocale, int, boolean, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.create(android.icu.impl.ICUService$Key, android.icu.impl.ICUService):java.lang.Object, dex:  in method: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.create(android.icu.impl.ICUService$Key, android.icu.impl.ICUService):java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.create(android.icu.impl.ICUService$Key, android.icu.impl.ICUService):java.lang.Object, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public java.lang.Object create(android.icu.impl.ICUService.Key r1, android.icu.impl.ICUService r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.create(android.icu.impl.ICUService$Key, android.icu.impl.ICUService):java.lang.Object, dex:  in method: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.create(android.icu.impl.ICUService$Key, android.icu.impl.ICUService):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.create(android.icu.impl.ICUService$Key, android.icu.impl.ICUService):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.isSupportedID(java.lang.String):boolean, dex: 
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
        protected boolean isSupportedID(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.isSupportedID(java.lang.String):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.isSupportedID(java.lang.String):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.toString():java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.toString():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.updateVisibleIDs(java.util.Map):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void updateVisibleIDs(java.util.Map<java.lang.String, android.icu.impl.ICUService.Factory> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.updateVisibleIDs(java.util.Map):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICULocaleService.SimpleLocaleKeyFactory.updateVisibleIDs(java.util.Map):void");
        }

        public SimpleLocaleKeyFactory(Object obj, ULocale locale, int kind, boolean visible) {
            this(obj, locale, kind, visible, null);
        }
    }

    public ICULocaleService(String name) {
        super(name);
    }

    public Object get(ULocale locale) {
        return get(locale, -1, null);
    }

    public Object get(ULocale locale, int kind) {
        return get(locale, kind, null);
    }

    public Object get(ULocale locale, ULocale[] actualReturn) {
        return get(locale, -1, actualReturn);
    }

    public Object get(ULocale locale, int kind, ULocale[] actualReturn) {
        Key key = createKey(locale, kind);
        if (actualReturn == null) {
            return getKey(key);
        }
        String[] temp = new String[1];
        Object result = getKey(key, temp);
        if (result != null) {
            int n = temp[0].indexOf("/");
            if (n >= 0) {
                temp[0] = temp[0].substring(n + 1);
            }
            actualReturn[0] = new ULocale(temp[0]);
        }
        return result;
    }

    public Factory registerObject(Object obj, ULocale locale) {
        return registerObject(obj, locale, -1, true);
    }

    public Factory registerObject(Object obj, ULocale locale, boolean visible) {
        return registerObject(obj, locale, -1, visible);
    }

    public Factory registerObject(Object obj, ULocale locale, int kind) {
        return registerObject(obj, locale, kind, true);
    }

    public Factory registerObject(Object obj, ULocale locale, int kind, boolean visible) {
        return registerFactory(new SimpleLocaleKeyFactory(obj, locale, kind, visible));
    }

    public Locale[] getAvailableLocales() {
        Set<String> visIDs = getVisibleIDs();
        Locale[] locales = new Locale[visIDs.size()];
        int n = 0;
        for (String id : visIDs) {
            int n2 = n + 1;
            locales[n] = LocaleUtility.getLocaleFromName(id);
            n = n2;
        }
        return locales;
    }

    public ULocale[] getAvailableULocales() {
        Set<String> visIDs = getVisibleIDs();
        ULocale[] locales = new ULocale[visIDs.size()];
        int n = 0;
        for (String id : visIDs) {
            int n2 = n + 1;
            locales[n] = new ULocale(id);
            n = n2;
        }
        return locales;
    }

    public String validateFallbackLocale() {
        ULocale loc = ULocale.getDefault();
        if (loc != this.fallbackLocale) {
            synchronized (this) {
                if (loc != this.fallbackLocale) {
                    this.fallbackLocale = loc;
                    this.fallbackLocaleName = loc.getBaseName();
                    clearServiceCache();
                }
            }
        }
        return this.fallbackLocaleName;
    }

    public Key createKey(String id) {
        return LocaleKey.createWithCanonicalFallback(id, validateFallbackLocale());
    }

    public Key createKey(String id, int kind) {
        return LocaleKey.createWithCanonicalFallback(id, validateFallbackLocale(), kind);
    }

    public Key createKey(ULocale l, int kind) {
        return LocaleKey.createWithCanonical(l, validateFallbackLocale(), kind);
    }
}
