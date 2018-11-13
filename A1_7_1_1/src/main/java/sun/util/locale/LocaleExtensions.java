package sun.util.locale;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

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
public class LocaleExtensions {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f74-assertionsDisabled = false;
    public static final LocaleExtensions CALENDAR_JAPANESE = null;
    public static final LocaleExtensions NUMBER_THAI = null;
    private final Map<Character, Extension> extensionMap;
    private final String id;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.util.locale.LocaleExtensions.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.util.locale.LocaleExtensions.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.util.locale.LocaleExtensions.<clinit>():void");
    }

    private LocaleExtensions(String id, Character key, Extension value) {
        this.id = id;
        this.extensionMap = Collections.singletonMap(key, value);
    }

    LocaleExtensions(Map<CaseInsensitiveChar, String> extensions, Set<CaseInsensitiveString> uattributes, Map<CaseInsensitiveString, String> ukeywords) {
        boolean hasExtension = !LocaleUtils.isEmpty((Map) extensions);
        boolean hasUAttributes = !LocaleUtils.isEmpty((Set) uattributes);
        boolean hasUKeywords = !LocaleUtils.isEmpty((Map) ukeywords);
        if (hasExtension || hasUAttributes || hasUKeywords) {
            SortedMap<Character, Extension> map = new TreeMap();
            if (hasExtension) {
                for (Entry<CaseInsensitiveChar, String> ext : extensions.entrySet()) {
                    char key = LocaleUtils.toLower(((CaseInsensitiveChar) ext.getKey()).value());
                    String value = (String) ext.getValue();
                    if (LanguageTag.isPrivateusePrefixChar(key)) {
                        value = InternalLocaleBuilder.removePrivateuseVariant(value);
                        if (value == null) {
                        }
                    }
                    map.put(Character.valueOf(key), new Extension(key, LocaleUtils.toLowerString(value)));
                }
            }
            if (hasUAttributes || hasUKeywords) {
                SortedSet uaset = null;
                SortedMap<String, String> ukmap = null;
                if (hasUAttributes) {
                    uaset = new TreeSet();
                    for (CaseInsensitiveString cis : uattributes) {
                        uaset.add(LocaleUtils.toLowerString(cis.value()));
                    }
                }
                if (hasUKeywords) {
                    ukmap = new TreeMap();
                    for (Entry<CaseInsensitiveString, String> kwd : ukeywords.entrySet()) {
                        ukmap.put(LocaleUtils.toLowerString(((CaseInsensitiveString) kwd.getKey()).value()), LocaleUtils.toLowerString((String) kwd.getValue()));
                    }
                }
                map.put(Character.valueOf('u'), new UnicodeLocaleExtension(uaset, (SortedMap) ukmap));
            }
            if (map.isEmpty()) {
                this.id = "";
                this.extensionMap = Collections.emptyMap();
            } else {
                this.id = toID(map);
                this.extensionMap = map;
            }
            return;
        }
        this.id = "";
        this.extensionMap = Collections.emptyMap();
    }

    public Set<Character> getKeys() {
        if (this.extensionMap.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(this.extensionMap.keySet());
    }

    public Extension getExtension(Character key) {
        return (Extension) this.extensionMap.get(Character.valueOf(LocaleUtils.toLower(key.charValue())));
    }

    public String getExtensionValue(Character key) {
        Extension ext = (Extension) this.extensionMap.get(Character.valueOf(LocaleUtils.toLower(key.charValue())));
        if (ext == null) {
            return null;
        }
        return ext.getValue();
    }

    public Set<String> getUnicodeLocaleAttributes() {
        Extension ext = (Extension) this.extensionMap.get(Character.valueOf('u'));
        if (ext == null) {
            return Collections.emptySet();
        }
        if (f74-assertionsDisabled || (ext instanceof UnicodeLocaleExtension)) {
            return ((UnicodeLocaleExtension) ext).getUnicodeLocaleAttributes();
        }
        throw new AssertionError();
    }

    public Set<String> getUnicodeLocaleKeys() {
        Extension ext = (Extension) this.extensionMap.get(Character.valueOf('u'));
        if (ext == null) {
            return Collections.emptySet();
        }
        if (f74-assertionsDisabled || (ext instanceof UnicodeLocaleExtension)) {
            return ((UnicodeLocaleExtension) ext).getUnicodeLocaleKeys();
        }
        throw new AssertionError();
    }

    public String getUnicodeLocaleType(String unicodeLocaleKey) {
        Extension ext = (Extension) this.extensionMap.get(Character.valueOf('u'));
        if (ext == null) {
            return null;
        }
        if (f74-assertionsDisabled || (ext instanceof UnicodeLocaleExtension)) {
            return ((UnicodeLocaleExtension) ext).getUnicodeLocaleType(LocaleUtils.toLowerString(unicodeLocaleKey));
        }
        throw new AssertionError();
    }

    public boolean isEmpty() {
        return this.extensionMap.isEmpty();
    }

    public static boolean isValidKey(char c) {
        return !LanguageTag.isExtensionSingletonChar(c) ? LanguageTag.isPrivateusePrefixChar(c) : true;
    }

    public static boolean isValidUnicodeLocaleKey(String ukey) {
        return UnicodeLocaleExtension.isKey(ukey);
    }

    private static String toID(SortedMap<Character, Extension> map) {
        StringBuilder buf = new StringBuilder();
        Object privuse = null;
        for (Entry<Character, Extension> entry : map.entrySet()) {
            Object extension = (Extension) entry.getValue();
            if (LanguageTag.isPrivateusePrefixChar(((Character) entry.getKey()).charValue())) {
                privuse = extension;
            } else {
                if (buf.length() > 0) {
                    buf.append(LanguageTag.SEP);
                }
                buf.append(extension);
            }
        }
        if (privuse != null) {
            if (buf.length() > 0) {
                buf.append(LanguageTag.SEP);
            }
            buf.append(privuse);
        }
        return buf.toString();
    }

    public String toString() {
        return this.id;
    }

    public String getID() {
        return this.id;
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof LocaleExtensions) {
            return this.id.equals(((LocaleExtensions) other).id);
        }
        return false;
    }
}
