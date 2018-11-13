package sun.util.locale;

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
public final class BaseLocale {
    private static final Cache CACHE = null;
    public static final String SEP = "_";
    private volatile int hash;
    private final String language;
    private final String region;
    private final String script;
    private final String variant;

    private static class Cache extends LocaleObjectCache<Key, BaseLocale> {
        protected Key normalizeKey(Key key) {
            return Key.normalize(key);
        }

        protected BaseLocale createObject(Key key) {
            return new BaseLocale(key.lang, key.scrt, key.regn, key.vart, null);
        }
    }

    private static final class Key implements Comparable<Key> {
        /* renamed from: -assertionsDisabled */
        static final /* synthetic */ boolean f69-assertionsDisabled = false;
        private final int hash;
        private final String lang;
        private final boolean normalized;
        private final String regn;
        private final String scrt;
        private final String vart;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.util.locale.BaseLocale.Key.<clinit>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.util.locale.BaseLocale.Key.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.locale.BaseLocale.Key.<clinit>():void");
        }

        /* synthetic */ Key(String language, String region, Key key) {
            this(language, region);
        }

        private Key(String language, String region) {
            boolean z = false;
            if (!f69-assertionsDisabled) {
                if (language.intern() == language && region.intern() == region) {
                    z = true;
                }
                if (!z) {
                    throw new AssertionError();
                }
            }
            this.lang = language;
            this.scrt = "";
            this.regn = region;
            this.vart = "";
            this.normalized = true;
            int h = language.hashCode();
            if (region != "") {
                for (int i = 0; i < region.length(); i++) {
                    h = (h * 31) + LocaleUtils.toLower(region.charAt(i));
                }
            }
            this.hash = h;
        }

        public Key(String language, String script, String region, String variant) {
            this(language, script, region, variant, false);
        }

        private Key(String language, String script, String region, String variant, boolean normalized) {
            int i;
            int h = 0;
            if (language != null) {
                this.lang = language;
                for (i = 0; i < language.length(); i++) {
                    h = (h * 31) + LocaleUtils.toLower(language.charAt(i));
                }
            } else {
                this.lang = "";
            }
            if (script != null) {
                this.scrt = script;
                for (i = 0; i < script.length(); i++) {
                    h = (h * 31) + LocaleUtils.toLower(script.charAt(i));
                }
            } else {
                this.scrt = "";
            }
            if (region != null) {
                this.regn = region;
                for (i = 0; i < region.length(); i++) {
                    h = (h * 31) + LocaleUtils.toLower(region.charAt(i));
                }
            } else {
                this.regn = "";
            }
            if (variant != null) {
                this.vart = variant;
                for (i = 0; i < variant.length(); i++) {
                    h = (h * 31) + variant.charAt(i);
                }
            } else {
                this.vart = "";
            }
            this.hash = h;
            this.normalized = normalized;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj instanceof Key) && this.hash == ((Key) obj).hash && LocaleUtils.caseIgnoreMatch(((Key) obj).lang, this.lang) && LocaleUtils.caseIgnoreMatch(((Key) obj).scrt, this.scrt) && LocaleUtils.caseIgnoreMatch(((Key) obj).regn, this.regn)) {
                return ((Key) obj).vart.equals(this.vart);
            }
            return false;
        }

        public int compareTo(Key other) {
            int res = LocaleUtils.caseIgnoreCompare(this.lang, other.lang);
            if (res != 0) {
                return res;
            }
            res = LocaleUtils.caseIgnoreCompare(this.scrt, other.scrt);
            if (res != 0) {
                return res;
            }
            res = LocaleUtils.caseIgnoreCompare(this.regn, other.regn);
            if (res == 0) {
                return this.vart.compareTo(other.vart);
            }
            return res;
        }

        public int hashCode() {
            return this.hash;
        }

        public static Key normalize(Key key) {
            if (key.normalized) {
                return key;
            }
            return new Key(LocaleUtils.toLowerString(key.lang).intern(), LocaleUtils.toTitleString(key.scrt).intern(), LocaleUtils.toUpperString(key.regn).intern(), key.vart.intern(), true);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.util.locale.BaseLocale.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.util.locale.BaseLocale.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.util.locale.BaseLocale.<clinit>():void");
    }

    /* synthetic */ BaseLocale(String language, String script, String region, String variant, BaseLocale baseLocale) {
        this(language, script, region, variant);
    }

    private BaseLocale(String language, String region) {
        this.hash = 0;
        this.language = language;
        this.script = "";
        this.region = region;
        this.variant = "";
    }

    private BaseLocale(String language, String script, String region, String variant) {
        this.hash = 0;
        this.language = language != null ? LocaleUtils.toLowerString(language).intern() : "";
        this.script = script != null ? LocaleUtils.toTitleString(script).intern() : "";
        this.region = region != null ? LocaleUtils.toUpperString(region).intern() : "";
        this.variant = variant != null ? variant.intern() : "";
    }

    public static BaseLocale createInstance(String language, String region) {
        BaseLocale base = new BaseLocale(language, region);
        CACHE.put(new Key(language, region, null), base);
        return base;
    }

    public static BaseLocale getInstance(String language, String script, String region, String variant) {
        if (language != null) {
            if (LocaleUtils.caseIgnoreMatch(language, "he")) {
                language = "iw";
            } else if (LocaleUtils.caseIgnoreMatch(language, "yi")) {
                language = "ji";
            } else if (LocaleUtils.caseIgnoreMatch(language, "id")) {
                language = "in";
            }
        }
        return (BaseLocale) CACHE.get(new Key(language, script, region, variant));
    }

    public String getLanguage() {
        return this.language;
    }

    public String getScript() {
        return this.script;
    }

    public String getRegion() {
        return this.region;
    }

    public String getVariant() {
        return this.variant;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BaseLocale)) {
            return false;
        }
        BaseLocale other = (BaseLocale) obj;
        if (this.language != other.language || this.script != other.script || this.region != other.region) {
            z = false;
        } else if (this.variant != other.variant) {
            z = false;
        }
        return z;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (this.language.length() > 0) {
            buf.append("language=");
            buf.append(this.language);
        }
        if (this.script.length() > 0) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append("script=");
            buf.append(this.script);
        }
        if (this.region.length() > 0) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append("region=");
            buf.append(this.region);
        }
        if (this.variant.length() > 0) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append("variant=");
            buf.append(this.variant);
        }
        return buf.toString();
    }

    public int hashCode() {
        int h = this.hash;
        if (h != 0) {
            return h;
        }
        h = (((((this.language.hashCode() * 31) + this.script.hashCode()) * 31) + this.region.hashCode()) * 31) + this.variant.hashCode();
        this.hash = h;
        return h;
    }
}
