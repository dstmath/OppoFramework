package android.icu.util;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.ICUResourceTableAccess;
import android.icu.impl.LocaleIDParser;
import android.icu.impl.LocaleIDs;
import android.icu.impl.LocaleUtility;
import android.icu.impl.SimpleCache;
import android.icu.impl.locale.AsciiUtil;
import android.icu.impl.locale.BaseLocale;
import android.icu.impl.locale.Extension;
import android.icu.impl.locale.InternalLocaleBuilder;
import android.icu.impl.locale.KeyTypeData;
import android.icu.impl.locale.LanguageTag;
import android.icu.impl.locale.LocaleExtensions;
import android.icu.impl.locale.LocaleSyntaxException;
import android.icu.impl.locale.UnicodeLocaleExtension;
import android.icu.lang.UScript;
import android.icu.text.LocaleDisplayNames;
import android.icu.text.LocaleDisplayNames.DialectHandling;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;
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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.collectAllInsns(BlockUtils.java:556)
    	at jadx.core.dex.visitors.ClassModifier.removeBridgeMethod(ClassModifier.java:197)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticMethods(ClassModifier.java:135)
    	at jadx.core.dex.visitors.ClassModifier.lambda$visit$0(ClassModifier.java:49)
    	at java.util.ArrayList.forEach(ArrayList.java:1251)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:49)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
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
public final class ULocale implements Serializable, Comparable<ULocale> {
    public static Type ACTUAL_LOCALE = null;
    private static final SimpleCache<Locale, ULocale> CACHE = null;
    public static final ULocale CANADA = null;
    public static final ULocale CANADA_FRENCH = null;
    private static String[][] CANONICALIZE_MAP = null;
    public static final ULocale CHINA = null;
    public static final ULocale CHINESE = null;
    private static final Locale EMPTY_LOCALE = null;
    private static final String EMPTY_STRING = "";
    public static final ULocale ENGLISH = null;
    public static final ULocale FRANCE = null;
    public static final ULocale FRENCH = null;
    public static final ULocale GERMAN = null;
    public static final ULocale GERMANY = null;
    public static final ULocale ITALIAN = null;
    public static final ULocale ITALY = null;
    public static final ULocale JAPAN = null;
    public static final ULocale JAPANESE = null;
    public static final ULocale KOREA = null;
    public static final ULocale KOREAN = null;
    private static final String LANG_DIR_STRING = "root-en-es-pt-zh-ja-ko-de-fr-it-ar+he+fa+ru-nl-pl-th-tr-";
    private static final String LOCALE_ATTRIBUTE_KEY = "attribute";
    public static final ULocale PRC = null;
    public static final char PRIVATE_USE_EXTENSION = 'x';
    public static final ULocale ROOT = null;
    public static final ULocale SIMPLIFIED_CHINESE = null;
    public static final ULocale TAIWAN = null;
    public static final ULocale TRADITIONAL_CHINESE = null;
    public static final ULocale UK = null;
    private static final String UNDEFINED_LANGUAGE = "und";
    private static final String UNDEFINED_REGION = "ZZ";
    private static final String UNDEFINED_SCRIPT = "Zzzz";
    private static final char UNDERSCORE = '_';
    public static final char UNICODE_LOCALE_EXTENSION = 'u';
    public static final ULocale US = null;
    public static Type VALID_LOCALE = null;
    private static Locale[] defaultCategoryLocales = null;
    private static ULocale[] defaultCategoryULocales = null;
    private static Locale defaultLocale = null;
    private static ULocale defaultULocale = null;
    private static ICUCache<String, String> nameCache = null;
    private static final long serialVersionUID = 3715177670352309217L;
    private static String[][] variantsToKeywords;
    private volatile transient BaseLocale baseLocale;
    private volatile transient LocaleExtensions extensions;
    private volatile transient Locale locale;
    private String localeID;

    /* renamed from: android.icu.util.ULocale$1ULocaleAcceptLanguageQ */
    class AnonymousClass1ULocaleAcceptLanguageQ implements Comparable<AnonymousClass1ULocaleAcceptLanguageQ> {
        private double q;
        private double serial;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e7 in method: android.icu.util.ULocale.1ULocaleAcceptLanguageQ.<init>(double, int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e7
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public AnonymousClass1ULocaleAcceptLanguageQ(double r1, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e7 in method: android.icu.util.ULocale.1ULocaleAcceptLanguageQ.<init>(double, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.1ULocaleAcceptLanguageQ.<init>(double, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.icu.util.ULocale.1ULocaleAcceptLanguageQ.compareTo(android.icu.util.ULocale$1ULocaleAcceptLanguageQ):int, dex:  in method: android.icu.util.ULocale.1ULocaleAcceptLanguageQ.compareTo(android.icu.util.ULocale$1ULocaleAcceptLanguageQ):int, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.icu.util.ULocale.1ULocaleAcceptLanguageQ.compareTo(android.icu.util.ULocale$1ULocaleAcceptLanguageQ):int, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$35.decode(InstructionCodec.java:790)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public int compareTo(android.icu.util.ULocale.AnonymousClass1ULocaleAcceptLanguageQ r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.icu.util.ULocale.1ULocaleAcceptLanguageQ.compareTo(android.icu.util.ULocale$1ULocaleAcceptLanguageQ):int, dex:  in method: android.icu.util.ULocale.1ULocaleAcceptLanguageQ.compareTo(android.icu.util.ULocale$1ULocaleAcceptLanguageQ):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.1ULocaleAcceptLanguageQ.compareTo(android.icu.util.ULocale$1ULocaleAcceptLanguageQ):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.util.ULocale.1ULocaleAcceptLanguageQ.compareTo(java.lang.Object):int, dex: 
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
        public /* bridge */ /* synthetic */ int compareTo(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.util.ULocale.1ULocaleAcceptLanguageQ.compareTo(java.lang.Object):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.1ULocaleAcceptLanguageQ.compareTo(java.lang.Object):int");
        }
    }

    public static final class Builder {
        private final InternalLocaleBuilder _locbld;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.util.ULocale.Builder.<init>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public Builder() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.util.ULocale.Builder.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.Builder.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.addUnicodeLocaleAttribute(java.lang.String):android.icu.util.ULocale$Builder, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.icu.util.ULocale.Builder addUnicodeLocaleAttribute(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.addUnicodeLocaleAttribute(java.lang.String):android.icu.util.ULocale$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.Builder.addUnicodeLocaleAttribute(java.lang.String):android.icu.util.ULocale$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.build():android.icu.util.ULocale, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.icu.util.ULocale build() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.build():android.icu.util.ULocale, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.Builder.build():android.icu.util.ULocale");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.clear():android.icu.util.ULocale$Builder, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.icu.util.ULocale.Builder clear() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.clear():android.icu.util.ULocale$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.Builder.clear():android.icu.util.ULocale$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.clearExtensions():android.icu.util.ULocale$Builder, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.icu.util.ULocale.Builder clearExtensions() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.clearExtensions():android.icu.util.ULocale$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.Builder.clearExtensions():android.icu.util.ULocale$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.removeUnicodeLocaleAttribute(java.lang.String):android.icu.util.ULocale$Builder, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.icu.util.ULocale.Builder removeUnicodeLocaleAttribute(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.removeUnicodeLocaleAttribute(java.lang.String):android.icu.util.ULocale$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.Builder.removeUnicodeLocaleAttribute(java.lang.String):android.icu.util.ULocale$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.setExtension(char, java.lang.String):android.icu.util.ULocale$Builder, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.icu.util.ULocale.Builder setExtension(char r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.setExtension(char, java.lang.String):android.icu.util.ULocale$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.Builder.setExtension(char, java.lang.String):android.icu.util.ULocale$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.setLanguage(java.lang.String):android.icu.util.ULocale$Builder, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.icu.util.ULocale.Builder setLanguage(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.setLanguage(java.lang.String):android.icu.util.ULocale$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.Builder.setLanguage(java.lang.String):android.icu.util.ULocale$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.util.ULocale.Builder.setLanguageTag(java.lang.String):android.icu.util.ULocale$Builder, dex: 
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
        public android.icu.util.ULocale.Builder setLanguageTag(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.util.ULocale.Builder.setLanguageTag(java.lang.String):android.icu.util.ULocale$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.Builder.setLanguageTag(java.lang.String):android.icu.util.ULocale$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.setLocale(android.icu.util.ULocale):android.icu.util.ULocale$Builder, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.icu.util.ULocale.Builder setLocale(android.icu.util.ULocale r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.setLocale(android.icu.util.ULocale):android.icu.util.ULocale$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.Builder.setLocale(android.icu.util.ULocale):android.icu.util.ULocale$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.setRegion(java.lang.String):android.icu.util.ULocale$Builder, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.icu.util.ULocale.Builder setRegion(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.setRegion(java.lang.String):android.icu.util.ULocale$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.Builder.setRegion(java.lang.String):android.icu.util.ULocale$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.setScript(java.lang.String):android.icu.util.ULocale$Builder, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.icu.util.ULocale.Builder setScript(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.setScript(java.lang.String):android.icu.util.ULocale$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.Builder.setScript(java.lang.String):android.icu.util.ULocale$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.setUnicodeLocaleKeyword(java.lang.String, java.lang.String):android.icu.util.ULocale$Builder, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.icu.util.ULocale.Builder setUnicodeLocaleKeyword(java.lang.String r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.setUnicodeLocaleKeyword(java.lang.String, java.lang.String):android.icu.util.ULocale$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.Builder.setUnicodeLocaleKeyword(java.lang.String, java.lang.String):android.icu.util.ULocale$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.setVariant(java.lang.String):android.icu.util.ULocale$Builder, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.icu.util.ULocale.Builder setVariant(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.Builder.setVariant(java.lang.String):android.icu.util.ULocale$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.Builder.setVariant(java.lang.String):android.icu.util.ULocale$Builder");
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
    public enum Category {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.util.ULocale.Category.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.util.ULocale.Category.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.Category.<clinit>():void");
        }
    }

    private static final class JDKLocaleHelper {
        /* renamed from: -android-icu-util-ULocale$CategorySwitchesValues */
        private static final /* synthetic */ int[] f7-android-icu-util-ULocale$CategorySwitchesValues = null;
        private static final String[][] JAVA6_MAPDATA = null;
        private static Object eDISPLAY;
        private static Object eFORMAT;
        private static boolean hasLocaleCategories;
        private static boolean hasScriptsAndUnicodeExtensions;
        private static Method mForLanguageTag;
        private static Method mGetDefault;
        private static Method mGetExtension;
        private static Method mGetExtensionKeys;
        private static Method mGetScript;
        private static Method mGetUnicodeLocaleAttributes;
        private static Method mGetUnicodeLocaleKeys;
        private static Method mGetUnicodeLocaleType;
        private static Method mSetDefault;

        /* renamed from: android.icu.util.ULocale$JDKLocaleHelper$1 */
        static class AnonymousClass1 implements PrivilegedAction<String> {
            final /* synthetic */ String val$fkey;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.util.ULocale.JDKLocaleHelper.1.<init>(java.lang.String):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            AnonymousClass1(java.lang.String r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.util.ULocale.JDKLocaleHelper.1.<init>(java.lang.String):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.JDKLocaleHelper.1.<init>(java.lang.String):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.util.ULocale.JDKLocaleHelper.1.run():java.lang.Object, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public /* bridge */ /* synthetic */ java.lang.Object run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.util.ULocale.JDKLocaleHelper.1.run():java.lang.Object, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.JDKLocaleHelper.1.run():java.lang.Object");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.JDKLocaleHelper.1.run():java.lang.String, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public java.lang.String run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.util.ULocale.JDKLocaleHelper.1.run():java.lang.String, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.JDKLocaleHelper.1.run():java.lang.String");
            }
        }

        /* renamed from: -getandroid-icu-util-ULocale$CategorySwitchesValues */
        private static /* synthetic */ int[] m0-getandroid-icu-util-ULocale$CategorySwitchesValues() {
            if (f7-android-icu-util-ULocale$CategorySwitchesValues != null) {
                return f7-android-icu-util-ULocale$CategorySwitchesValues;
            }
            int[] iArr = new int[Category.values().length];
            try {
                iArr[Category.DISPLAY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Category.FORMAT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            f7-android-icu-util-ULocale$CategorySwitchesValues = iArr;
            return iArr;
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.util.ULocale.JDKLocaleHelper.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.util.ULocale.JDKLocaleHelper.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.JDKLocaleHelper.<clinit>():void");
        }

        private JDKLocaleHelper() {
        }

        public static boolean hasLocaleCategories() {
            return hasLocaleCategories;
        }

        public static ULocale toULocale(Locale loc) {
            return hasScriptsAndUnicodeExtensions ? toULocale7(loc) : toULocale6(loc);
        }

        public static Locale toLocale(ULocale uloc) {
            return hasScriptsAndUnicodeExtensions ? toLocale7(uloc) : toLocale6(uloc);
        }

        private static ULocale toULocale7(Locale loc) {
            IllegalAccessException e;
            InvocationTargetException e2;
            String language = loc.getLanguage();
            String script = "";
            String country = loc.getCountry();
            String variant = loc.getVariant();
            Iterable attributes = null;
            Map keywords = null;
            try {
                String kwKey;
                String kwVal;
                script = (String) mGetScript.invoke(loc, (Object[]) null);
                Set<Character> extKeys = (Set) mGetExtensionKeys.invoke(loc, (Object[]) null);
                if (!extKeys.isEmpty()) {
                    Iterator extKey$iterator = extKeys.iterator();
                    while (true) {
                        Map<String, String> keywords2;
                        Map<String, String> keywords3;
                        Set<String> attributes2;
                        Set<String> attributes3;
                        try {
                            keywords2 = keywords3;
                            attributes2 = attributes3;
                            if (!extKey$iterator.hasNext()) {
                                keywords = keywords2;
                                attributes = attributes2;
                                break;
                            }
                            Character extKey = (Character) extKey$iterator.next();
                            Method method;
                            if (extKey.charValue() == 'u') {
                                Set<String> uAttributes = (Set) mGetUnicodeLocaleAttributes.invoke(loc, (Object[]) null);
                                if (uAttributes.isEmpty()) {
                                    attributes3 = attributes2;
                                } else {
                                    attributes3 = new TreeSet();
                                    try {
                                        for (String attr : uAttributes) {
                                            attributes3.add(attr);
                                        }
                                    } catch (IllegalAccessException e3) {
                                        e = e3;
                                        keywords3 = keywords2;
                                        throw new RuntimeException(e);
                                    } catch (InvocationTargetException e4) {
                                        e2 = e4;
                                        keywords3 = keywords2;
                                        throw new RuntimeException(e2);
                                    }
                                }
                                for (String kwKey2 : (Set) mGetUnicodeLocaleKeys.invoke(loc, (Object[]) null)) {
                                    method = mGetUnicodeLocaleType;
                                    String[] strArr = new Object[1];
                                    strArr[0] = kwKey2;
                                    kwVal = (String) method.invoke(loc, strArr);
                                    if (kwVal == null) {
                                        keywords3 = keywords2;
                                    } else if (!kwKey2.equals("va")) {
                                        if (keywords2 == null) {
                                            keywords3 = new TreeMap();
                                        } else {
                                            keywords3 = keywords2;
                                        }
                                        keywords3.put(kwKey2, kwVal);
                                    } else if (variant.length() == 0) {
                                        variant = kwVal;
                                        keywords3 = keywords2;
                                    } else {
                                        variant = kwVal + BaseLocale.SEP + variant;
                                        keywords3 = keywords2;
                                    }
                                    keywords2 = keywords3;
                                }
                                keywords3 = keywords2;
                            } else {
                                method = mGetExtension;
                                Character[] chArr = new Object[1];
                                chArr[0] = extKey;
                                String extVal = (String) method.invoke(loc, chArr);
                                if (extVal != null) {
                                    if (keywords2 == null) {
                                        keywords3 = new TreeMap();
                                    } else {
                                        keywords3 = keywords2;
                                    }
                                    try {
                                        keywords3.put(String.valueOf(extKey), extVal);
                                        attributes3 = attributes2;
                                    } catch (IllegalAccessException e5) {
                                        e = e5;
                                        attributes3 = attributes2;
                                    } catch (InvocationTargetException e6) {
                                        e2 = e6;
                                        attributes3 = attributes2;
                                    }
                                } else {
                                    keywords3 = keywords2;
                                    attributes3 = attributes2;
                                }
                            }
                        } catch (IllegalAccessException e7) {
                            e = e7;
                            keywords3 = keywords2;
                            attributes3 = attributes2;
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e8) {
                            e2 = e8;
                            keywords3 = keywords2;
                            attributes3 = attributes2;
                            throw new RuntimeException(e2);
                        }
                    }
                }
                if (language.equals("no") && country.equals("NO") && variant.equals("NY")) {
                    language = "nn";
                    variant = "";
                }
                StringBuilder buf = new StringBuilder(language);
                if (script.length() > 0) {
                    buf.append(ULocale.UNDERSCORE);
                    buf.append(script);
                }
                if (country.length() > 0) {
                    buf.append(ULocale.UNDERSCORE);
                    buf.append(country);
                }
                if (variant.length() > 0) {
                    if (country.length() == 0) {
                        buf.append(ULocale.UNDERSCORE);
                    }
                    buf.append(ULocale.UNDERSCORE);
                    buf.append(variant);
                }
                if (attributes != null) {
                    StringBuilder attrBuf = new StringBuilder();
                    for (String attr2 : attributes) {
                        if (attrBuf.length() != 0) {
                            attrBuf.append('-');
                        }
                        attrBuf.append(attr2);
                    }
                    if (keywords == null) {
                        keywords = new TreeMap();
                    }
                    keywords.put(ULocale.LOCALE_ATTRIBUTE_KEY, attrBuf.toString());
                }
                if (keywords != null) {
                    buf.append('@');
                    boolean addSep = false;
                    for (Entry<String, String> kwEntry : keywords.entrySet()) {
                        kwKey2 = (String) kwEntry.getKey();
                        kwVal = (String) kwEntry.getValue();
                        if (kwKey2.length() != 1) {
                            kwKey2 = ULocale.toLegacyKey(kwKey2);
                            if (kwVal.length() == 0) {
                                kwVal = "yes";
                            }
                            kwVal = ULocale.toLegacyType(kwKey2, kwVal);
                        }
                        if (addSep) {
                            buf.append(';');
                        } else {
                            addSep = true;
                        }
                        buf.append(kwKey2);
                        buf.append('=');
                        buf.append(kwVal);
                    }
                }
                return new ULocale(ULocale.getName(buf.toString()), loc, null);
            } catch (IllegalAccessException e9) {
                e = e9;
            } catch (InvocationTargetException e10) {
                e2 = e10;
            }
        }

        private static ULocale toULocale6(Locale loc) {
            String locStr = loc.toString();
            if (locStr.length() == 0) {
                return ULocale.ROOT;
            }
            for (int i = 0; i < JAVA6_MAPDATA.length; i++) {
                if (JAVA6_MAPDATA[i][0].equals(locStr)) {
                    LocaleIDParser p = new LocaleIDParser(JAVA6_MAPDATA[i][1]);
                    p.setKeywordValue(JAVA6_MAPDATA[i][2], JAVA6_MAPDATA[i][3]);
                    locStr = p.getName();
                    break;
                }
            }
            return new ULocale(ULocale.getName(locStr), loc, null);
        }

        private static Locale toLocale7(ULocale uloc) {
            Locale locale = null;
            String ulocStr = uloc.getName();
            if (uloc.getScript().length() > 0 || ulocStr.contains("@")) {
                String tag = AsciiUtil.toUpperString(uloc.toLanguageTag());
                try {
                    Method method = mForLanguageTag;
                    Object[] objArr = new Object[1];
                    objArr[0] = tag;
                    locale = (Locale) method.invoke(null, objArr);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e2) {
                    throw new RuntimeException(e2);
                }
            }
            if (locale == null) {
                return new Locale(uloc.getLanguage(), uloc.getCountry(), uloc.getVariant());
            }
            return locale;
        }

        private static Locale toLocale6(ULocale uloc) {
            String locstr = uloc.getBaseName();
            int i = 0;
            while (i < JAVA6_MAPDATA.length) {
                if (locstr.equals(JAVA6_MAPDATA[i][1]) || locstr.equals(JAVA6_MAPDATA[i][4])) {
                    if (JAVA6_MAPDATA[i][2] == null) {
                        locstr = JAVA6_MAPDATA[i][0];
                        break;
                    }
                    String val = uloc.getKeywordValue(JAVA6_MAPDATA[i][2]);
                    if (val != null && val.equals(JAVA6_MAPDATA[i][3])) {
                        locstr = JAVA6_MAPDATA[i][0];
                        break;
                    }
                }
                i++;
            }
            String[] names = new LocaleIDParser(locstr).getLanguageScriptCountryVariant();
            return new Locale(names[0], names[2], names[3]);
        }

        public static Locale getDefault(Category category) {
            Locale loc = Locale.getDefault();
            if (!hasLocaleCategories) {
                return loc;
            }
            Object cat = null;
            switch (m0-getandroid-icu-util-ULocale$CategorySwitchesValues()[category.ordinal()]) {
                case 1:
                    cat = eDISPLAY;
                    break;
                case 2:
                    cat = eFORMAT;
                    break;
            }
            if (cat == null) {
                return loc;
            }
            try {
                Method method = mGetDefault;
                Object[] objArr = new Object[1];
                objArr[0] = cat;
                return (Locale) method.invoke(null, objArr);
            } catch (InvocationTargetException e) {
                return loc;
            } catch (IllegalArgumentException e2) {
                return loc;
            } catch (IllegalAccessException e3) {
                return loc;
            }
        }

        public static void setDefault(Category category, Locale newLocale) {
            if (hasLocaleCategories) {
                Object cat = null;
                switch (m0-getandroid-icu-util-ULocale$CategorySwitchesValues()[category.ordinal()]) {
                    case 1:
                        cat = eDISPLAY;
                        break;
                    case 2:
                        cat = eFORMAT;
                        break;
                }
                if (cat != null) {
                    try {
                        Method method = mSetDefault;
                        Object[] objArr = new Object[2];
                        objArr[0] = cat;
                        objArr[1] = newLocale;
                        method.invoke(null, objArr);
                    } catch (InvocationTargetException e) {
                    } catch (IllegalArgumentException e2) {
                    } catch (IllegalAccessException e3) {
                    }
                }
            }
        }

        public static boolean isOriginalDefaultLocale(Locale loc) {
            boolean z = false;
            if (hasScriptsAndUnicodeExtensions) {
                String script = "";
                try {
                    boolean equals;
                    script = (String) mGetScript.invoke(loc, (Object[]) null);
                    if (loc.getLanguage().equals(getSystemProperty("user.language")) && loc.getCountry().equals(getSystemProperty("user.country")) && loc.getVariant().equals(getSystemProperty("user.variant"))) {
                        equals = script.equals(getSystemProperty("user.script"));
                    } else {
                        equals = false;
                    }
                    return equals;
                } catch (Exception e) {
                    return false;
                }
            }
            if (loc.getLanguage().equals(getSystemProperty("user.language")) && loc.getCountry().equals(getSystemProperty("user.country"))) {
                z = loc.getVariant().equals(getSystemProperty("user.variant"));
            }
            return z;
        }

        /*  JADX ERROR: NullPointerException in pass: ModVisitor
            java.lang.NullPointerException
            	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
            	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
            	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
            	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
            	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
            	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
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
        public static java.lang.String getSystemProperty(java.lang.String r5) {
            /*
            r3 = 0;
            r2 = r5;
            r4 = java.lang.System.getSecurityManager();
            if (r4 == 0) goto L_0x0016;
        L_0x0008:
            r4 = new android.icu.util.ULocale$JDKLocaleHelper$1;	 Catch:{ AccessControlException -> 0x001b }
            r4.<init>(r5);	 Catch:{ AccessControlException -> 0x001b }
            r4 = java.security.AccessController.doPrivileged(r4);	 Catch:{ AccessControlException -> 0x001b }
            r0 = r4;	 Catch:{ AccessControlException -> 0x001b }
            r0 = (java.lang.String) r0;	 Catch:{ AccessControlException -> 0x001b }
            r3 = r0;	 Catch:{ AccessControlException -> 0x001b }
        L_0x0015:
            return r3;
        L_0x0016:
            r3 = java.lang.System.getProperty(r5);
            goto L_0x0015;
        L_0x001b:
            r1 = move-exception;
            goto L_0x0015;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.JDKLocaleHelper.getSystemProperty(java.lang.String):java.lang.String");
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
    @Deprecated
    public enum Minimize {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.util.ULocale.Minimize.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.util.ULocale.Minimize.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.Minimize.<clinit>():void");
        }
    }

    public static final class Type {
        /* synthetic */ Type(Type type) {
            this();
        }

        private Type() {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.util.ULocale.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.util.ULocale.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.<clinit>():void");
    }

    /* synthetic */ ULocale(String localeID, Locale locale, ULocale uLocale) {
        this(localeID, locale);
    }

    private static void initCANONICALIZE_MAP() {
        String[] strArr;
        if (CANONICALIZE_MAP == null) {
            String[][] tempCANONICALIZE_MAP = new String[50][];
            strArr = new String[4];
            strArr[0] = "C";
            strArr[1] = "en_US_POSIX";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[0] = strArr;
            strArr = new String[4];
            strArr[0] = "art_LOJBAN";
            strArr[1] = "jbo";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[1] = strArr;
            strArr = new String[4];
            strArr[0] = "az_AZ_CYRL";
            strArr[1] = "az_Cyrl_AZ";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[2] = strArr;
            strArr = new String[4];
            strArr[0] = "az_AZ_LATN";
            strArr[1] = "az_Latn_AZ";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[3] = strArr;
            strArr = new String[4];
            strArr[0] = "ca_ES_PREEURO";
            strArr[1] = "ca_ES";
            strArr[2] = "currency";
            strArr[3] = "ESP";
            tempCANONICALIZE_MAP[4] = strArr;
            strArr = new String[4];
            strArr[0] = "cel_GAULISH";
            strArr[1] = "cel__GAULISH";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[5] = strArr;
            strArr = new String[4];
            strArr[0] = "de_1901";
            strArr[1] = "de__1901";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[6] = strArr;
            strArr = new String[4];
            strArr[0] = "de_1906";
            strArr[1] = "de__1906";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[7] = strArr;
            strArr = new String[4];
            strArr[0] = "de__PHONEBOOK";
            strArr[1] = "de";
            strArr[2] = "collation";
            strArr[3] = "phonebook";
            tempCANONICALIZE_MAP[8] = strArr;
            strArr = new String[4];
            strArr[0] = "de_AT_PREEURO";
            strArr[1] = "de_AT";
            strArr[2] = "currency";
            strArr[3] = "ATS";
            tempCANONICALIZE_MAP[9] = strArr;
            strArr = new String[4];
            strArr[0] = "de_DE_PREEURO";
            strArr[1] = "de_DE";
            strArr[2] = "currency";
            strArr[3] = "DEM";
            tempCANONICALIZE_MAP[10] = strArr;
            strArr = new String[4];
            strArr[0] = "de_LU_PREEURO";
            strArr[1] = "de_LU";
            strArr[2] = "currency";
            strArr[3] = "EUR";
            tempCANONICALIZE_MAP[11] = strArr;
            strArr = new String[4];
            strArr[0] = "el_GR_PREEURO";
            strArr[1] = "el_GR";
            strArr[2] = "currency";
            strArr[3] = "GRD";
            tempCANONICALIZE_MAP[12] = strArr;
            strArr = new String[4];
            strArr[0] = "en_BOONT";
            strArr[1] = "en__BOONT";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[13] = strArr;
            strArr = new String[4];
            strArr[0] = "en_SCOUSE";
            strArr[1] = "en__SCOUSE";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[14] = strArr;
            strArr = new String[4];
            strArr[0] = "en_BE_PREEURO";
            strArr[1] = "en_BE";
            strArr[2] = "currency";
            strArr[3] = "BEF";
            tempCANONICALIZE_MAP[15] = strArr;
            strArr = new String[4];
            strArr[0] = "en_IE_PREEURO";
            strArr[1] = "en_IE";
            strArr[2] = "currency";
            strArr[3] = "IEP";
            tempCANONICALIZE_MAP[16] = strArr;
            strArr = new String[4];
            strArr[0] = "es__TRADITIONAL";
            strArr[1] = "es";
            strArr[2] = "collation";
            strArr[3] = "traditional";
            tempCANONICALIZE_MAP[17] = strArr;
            strArr = new String[4];
            strArr[0] = "es_ES_PREEURO";
            strArr[1] = "es_ES";
            strArr[2] = "currency";
            strArr[3] = "ESP";
            tempCANONICALIZE_MAP[18] = strArr;
            strArr = new String[4];
            strArr[0] = "eu_ES_PREEURO";
            strArr[1] = "eu_ES";
            strArr[2] = "currency";
            strArr[3] = "ESP";
            tempCANONICALIZE_MAP[19] = strArr;
            strArr = new String[4];
            strArr[0] = "fi_FI_PREEURO";
            strArr[1] = "fi_FI";
            strArr[2] = "currency";
            strArr[3] = "FIM";
            tempCANONICALIZE_MAP[20] = strArr;
            strArr = new String[4];
            strArr[0] = "fr_BE_PREEURO";
            strArr[1] = "fr_BE";
            strArr[2] = "currency";
            strArr[3] = "BEF";
            tempCANONICALIZE_MAP[21] = strArr;
            strArr = new String[4];
            strArr[0] = "fr_FR_PREEURO";
            strArr[1] = "fr_FR";
            strArr[2] = "currency";
            strArr[3] = "FRF";
            tempCANONICALIZE_MAP[22] = strArr;
            strArr = new String[4];
            strArr[0] = "fr_LU_PREEURO";
            strArr[1] = "fr_LU";
            strArr[2] = "currency";
            strArr[3] = "LUF";
            tempCANONICALIZE_MAP[23] = strArr;
            strArr = new String[4];
            strArr[0] = "ga_IE_PREEURO";
            strArr[1] = "ga_IE";
            strArr[2] = "currency";
            strArr[3] = "IEP";
            tempCANONICALIZE_MAP[24] = strArr;
            strArr = new String[4];
            strArr[0] = "gl_ES_PREEURO";
            strArr[1] = "gl_ES";
            strArr[2] = "currency";
            strArr[3] = "ESP";
            tempCANONICALIZE_MAP[25] = strArr;
            strArr = new String[4];
            strArr[0] = "hi__DIRECT";
            strArr[1] = "hi";
            strArr[2] = "collation";
            strArr[3] = "direct";
            tempCANONICALIZE_MAP[26] = strArr;
            strArr = new String[4];
            strArr[0] = "it_IT_PREEURO";
            strArr[1] = "it_IT";
            strArr[2] = "currency";
            strArr[3] = "ITL";
            tempCANONICALIZE_MAP[27] = strArr;
            strArr = new String[4];
            strArr[0] = "ja_JP_TRADITIONAL";
            strArr[1] = "ja_JP";
            strArr[2] = "calendar";
            strArr[3] = "japanese";
            tempCANONICALIZE_MAP[28] = strArr;
            strArr = new String[4];
            strArr[0] = "nl_BE_PREEURO";
            strArr[1] = "nl_BE";
            strArr[2] = "currency";
            strArr[3] = "BEF";
            tempCANONICALIZE_MAP[29] = strArr;
            strArr = new String[4];
            strArr[0] = "nl_NL_PREEURO";
            strArr[1] = "nl_NL";
            strArr[2] = "currency";
            strArr[3] = "NLG";
            tempCANONICALIZE_MAP[30] = strArr;
            strArr = new String[4];
            strArr[0] = "pt_PT_PREEURO";
            strArr[1] = "pt_PT";
            strArr[2] = "currency";
            strArr[3] = "PTE";
            tempCANONICALIZE_MAP[31] = strArr;
            strArr = new String[4];
            strArr[0] = "sl_ROZAJ";
            strArr[1] = "sl__ROZAJ";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[32] = strArr;
            strArr = new String[4];
            strArr[0] = "sr_SP_CYRL";
            strArr[1] = "sr_Cyrl_RS";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[33] = strArr;
            strArr = new String[4];
            strArr[0] = "sr_SP_LATN";
            strArr[1] = "sr_Latn_RS";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[34] = strArr;
            strArr = new String[4];
            strArr[0] = "sr_YU_CYRILLIC";
            strArr[1] = "sr_Cyrl_RS";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[35] = strArr;
            strArr = new String[4];
            strArr[0] = "th_TH_TRADITIONAL";
            strArr[1] = "th_TH";
            strArr[2] = "calendar";
            strArr[3] = "buddhist";
            tempCANONICALIZE_MAP[36] = strArr;
            strArr = new String[4];
            strArr[0] = "uz_UZ_CYRILLIC";
            strArr[1] = "uz_Cyrl_UZ";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[37] = strArr;
            strArr = new String[4];
            strArr[0] = "uz_UZ_CYRL";
            strArr[1] = "uz_Cyrl_UZ";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[38] = strArr;
            strArr = new String[4];
            strArr[0] = "uz_UZ_LATN";
            strArr[1] = "uz_Latn_UZ";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[39] = strArr;
            strArr = new String[4];
            strArr[0] = "zh_CHS";
            strArr[1] = "zh_Hans";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[40] = strArr;
            strArr = new String[4];
            strArr[0] = "zh_CHT";
            strArr[1] = "zh_Hant";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[41] = strArr;
            strArr = new String[4];
            strArr[0] = "zh_GAN";
            strArr[1] = "zh__GAN";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[42] = strArr;
            strArr = new String[4];
            strArr[0] = "zh_GUOYU";
            strArr[1] = "zh";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[43] = strArr;
            strArr = new String[4];
            strArr[0] = "zh_HAKKA";
            strArr[1] = "zh__HAKKA";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[44] = strArr;
            strArr = new String[4];
            strArr[0] = "zh_MIN";
            strArr[1] = "zh__MIN";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[45] = strArr;
            strArr = new String[4];
            strArr[0] = "zh_MIN_NAN";
            strArr[1] = "zh__MINNAN";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[46] = strArr;
            strArr = new String[4];
            strArr[0] = "zh_WUU";
            strArr[1] = "zh__WUU";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[47] = strArr;
            strArr = new String[4];
            strArr[0] = "zh_XIANG";
            strArr[1] = "zh__XIANG";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[48] = strArr;
            strArr = new String[4];
            strArr[0] = "zh_YUE";
            strArr[1] = "zh__YUE";
            strArr[2] = null;
            strArr[3] = null;
            tempCANONICALIZE_MAP[49] = strArr;
            synchronized (ULocale.class) {
                if (CANONICALIZE_MAP == null) {
                    CANONICALIZE_MAP = tempCANONICALIZE_MAP;
                }
            }
        }
        if (variantsToKeywords == null) {
            String[][] tempVariantsToKeywords = new String[3][];
            strArr = new String[3];
            strArr[0] = "EURO";
            strArr[1] = "currency";
            strArr[2] = "EUR";
            tempVariantsToKeywords[0] = strArr;
            strArr = new String[3];
            strArr[0] = "PINYIN";
            strArr[1] = "collation";
            strArr[2] = "pinyin";
            tempVariantsToKeywords[1] = strArr;
            strArr = new String[3];
            strArr[0] = "STROKE";
            strArr[1] = "collation";
            strArr[2] = "stroke";
            tempVariantsToKeywords[2] = strArr;
            synchronized (ULocale.class) {
                if (variantsToKeywords == null) {
                    variantsToKeywords = tempVariantsToKeywords;
                }
            }
        }
    }

    private ULocale(String localeID, Locale locale) {
        this.localeID = localeID;
        this.locale = locale;
    }

    private ULocale(Locale loc) {
        this.localeID = getName(forLocale(loc).toString());
        this.locale = loc;
    }

    public static ULocale forLocale(Locale loc) {
        if (loc == null) {
            return null;
        }
        ULocale result = (ULocale) CACHE.get(loc);
        if (result == null) {
            result = JDKLocaleHelper.toULocale(loc);
            CACHE.put(loc, result);
        }
        return result;
    }

    public ULocale(String localeID) {
        this.localeID = getName(localeID);
    }

    public ULocale(String a, String b) {
        this(a, b, null);
    }

    public ULocale(String a, String b, String c) {
        this.localeID = getName(lscvToID(a, b, c, ""));
    }

    public static ULocale createCanonical(String nonCanonicalID) {
        return new ULocale(canonicalize(nonCanonicalID), (Locale) null);
    }

    private static String lscvToID(String lang, String script, String country, String variant) {
        StringBuilder buf = new StringBuilder();
        if (lang != null && lang.length() > 0) {
            buf.append(lang);
        }
        if (script != null && script.length() > 0) {
            buf.append(UNDERSCORE);
            buf.append(script);
        }
        if (country != null && country.length() > 0) {
            buf.append(UNDERSCORE);
            buf.append(country);
        }
        if (variant != null && variant.length() > 0) {
            if (country == null || country.length() == 0) {
                buf.append(UNDERSCORE);
            }
            buf.append(UNDERSCORE);
            buf.append(variant);
        }
        return buf.toString();
    }

    public Locale toLocale() {
        if (this.locale == null) {
            this.locale = JDKLocaleHelper.toLocale(this);
        }
        return this.locale;
    }

    public static ULocale getDefault() {
        synchronized (ULocale.class) {
            if (defaultULocale == null) {
                return ROOT;
            }
            Locale currentDefault = Locale.getDefault();
            if (!defaultLocale.equals(currentDefault)) {
                defaultLocale = currentDefault;
                defaultULocale = forLocale(currentDefault);
                if (!JDKLocaleHelper.hasLocaleCategories()) {
                    for (Category cat : Category.values()) {
                        int idx = cat.ordinal();
                        defaultCategoryLocales[idx] = currentDefault;
                        defaultCategoryULocales[idx] = forLocale(currentDefault);
                    }
                }
            }
            return defaultULocale;
        }
    }

    public static synchronized void setDefault(ULocale newLocale) {
        synchronized (ULocale.class) {
            defaultLocale = newLocale.toLocale();
            Locale.setDefault(defaultLocale);
            defaultULocale = newLocale;
            for (Category cat : Category.values()) {
                setDefault(cat, newLocale);
            }
        }
    }

    public static ULocale getDefault(Category category) {
        synchronized (ULocale.class) {
            int idx = category.ordinal();
            if (defaultCategoryULocales[idx] == null) {
                return ROOT;
            }
            if (JDKLocaleHelper.hasLocaleCategories()) {
                Locale currentCategoryDefault = JDKLocaleHelper.getDefault(category);
                if (!defaultCategoryLocales[idx].equals(currentCategoryDefault)) {
                    defaultCategoryLocales[idx] = currentCategoryDefault;
                    defaultCategoryULocales[idx] = forLocale(currentCategoryDefault);
                }
            } else {
                Locale currentDefault = Locale.getDefault();
                if (!defaultLocale.equals(currentDefault)) {
                    defaultLocale = currentDefault;
                    defaultULocale = forLocale(currentDefault);
                    for (Category cat : Category.values()) {
                        int tmpIdx = cat.ordinal();
                        defaultCategoryLocales[tmpIdx] = currentDefault;
                        defaultCategoryULocales[tmpIdx] = forLocale(currentDefault);
                    }
                }
            }
            return defaultCategoryULocales[idx];
        }
    }

    public static synchronized void setDefault(Category category, ULocale newLocale) {
        synchronized (ULocale.class) {
            Locale newJavaDefault = newLocale.toLocale();
            int idx = category.ordinal();
            defaultCategoryULocales[idx] = newLocale;
            defaultCategoryLocales[idx] = newJavaDefault;
            JDKLocaleHelper.setDefault(category, newJavaDefault);
        }
    }

    public Object clone() {
        return this;
    }

    public int hashCode() {
        return this.localeID.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ULocale) {
            return this.localeID.equals(((ULocale) obj).localeID);
        }
        return false;
    }

    public /* bridge */ /* synthetic */ int compareTo(Object other) {
        return compareTo((ULocale) other);
    }

    public int compareTo(ULocale other) {
        int i = 0;
        if (this == other) {
            return 0;
        }
        int cmp = getLanguage().compareTo(other.getLanguage());
        if (cmp == 0) {
            cmp = getScript().compareTo(other.getScript());
            if (cmp == 0) {
                cmp = getCountry().compareTo(other.getCountry());
                if (cmp == 0) {
                    cmp = getVariant().compareTo(other.getVariant());
                    if (cmp == 0) {
                        Iterator<String> thisKwdItr = getKeywords();
                        Iterator<String> otherKwdItr = other.getKeywords();
                        if (thisKwdItr == null) {
                            if (otherKwdItr == null) {
                                cmp = 0;
                            } else {
                                cmp = -1;
                            }
                        } else if (otherKwdItr == null) {
                            cmp = 1;
                        } else {
                            while (cmp == 0 && thisKwdItr.hasNext()) {
                                if (!otherKwdItr.hasNext()) {
                                    cmp = 1;
                                    break;
                                }
                                String thisKey = (String) thisKwdItr.next();
                                String otherKey = (String) otherKwdItr.next();
                                cmp = thisKey.compareTo(otherKey);
                                if (cmp == 0) {
                                    String thisVal = getKeywordValue(thisKey);
                                    String otherVal = other.getKeywordValue(otherKey);
                                    if (thisVal == null) {
                                        if (otherVal == null) {
                                            cmp = 0;
                                        } else {
                                            cmp = -1;
                                        }
                                    } else if (otherVal == null) {
                                        cmp = 1;
                                    } else {
                                        cmp = thisVal.compareTo(otherVal);
                                    }
                                }
                            }
                            if (cmp == 0 && otherKwdItr.hasNext()) {
                                cmp = -1;
                            }
                        }
                    }
                }
            }
        }
        if (cmp < 0) {
            i = -1;
        } else if (cmp > 0) {
            i = 1;
        }
        return i;
    }

    public static ULocale[] getAvailableLocales() {
        return ICUResourceBundle.getAvailableULocales();
    }

    public static String[] getISOCountries() {
        return LocaleIDs.getISOCountries();
    }

    public static String[] getISOLanguages() {
        return LocaleIDs.getISOLanguages();
    }

    public String getLanguage() {
        return base().getLanguage();
    }

    public static String getLanguage(String localeID) {
        return new LocaleIDParser(localeID).getLanguage();
    }

    public String getScript() {
        return base().getScript();
    }

    public static String getScript(String localeID) {
        return new LocaleIDParser(localeID).getScript();
    }

    public String getCountry() {
        return base().getRegion();
    }

    public static String getCountry(String localeID) {
        return new LocaleIDParser(localeID).getCountry();
    }

    public String getVariant() {
        return base().getVariant();
    }

    public static String getVariant(String localeID) {
        return new LocaleIDParser(localeID).getVariant();
    }

    public static String getFallback(String localeID) {
        return getFallbackString(getName(localeID));
    }

    public ULocale getFallback() {
        if (this.localeID.length() == 0 || this.localeID.charAt(0) == '@') {
            return null;
        }
        return new ULocale(getFallbackString(this.localeID), (Locale) null);
    }

    private static String getFallbackString(String fallback) {
        int extStart = fallback.indexOf(64);
        if (extStart == -1) {
            extStart = fallback.length();
        }
        int last = fallback.lastIndexOf(95, extStart);
        if (last == -1) {
            last = 0;
        } else {
            while (last > 0 && fallback.charAt(last - 1) == UNDERSCORE) {
                last--;
            }
        }
        return fallback.substring(0, last) + fallback.substring(extStart);
    }

    public String getBaseName() {
        return getBaseName(this.localeID);
    }

    public static String getBaseName(String localeID) {
        if (localeID.indexOf(64) == -1) {
            return localeID;
        }
        return new LocaleIDParser(localeID).getBaseName();
    }

    public String getName() {
        return this.localeID;
    }

    private static int getShortestSubtagLength(String localeID) {
        int localeIDLength = localeID.length();
        int length = localeIDLength;
        boolean reset = true;
        int tmpLength = 0;
        int i = 0;
        while (i < localeIDLength) {
            if (localeID.charAt(i) == UNDERSCORE || localeID.charAt(i) == '-') {
                if (tmpLength != 0 && tmpLength < length) {
                    length = tmpLength;
                }
                reset = true;
            } else {
                if (reset) {
                    reset = false;
                    tmpLength = 0;
                }
                tmpLength++;
            }
            i++;
        }
        return length;
    }

    public static String getName(String localeID) {
        String tmpLocaleID;
        if (localeID == null || localeID.contains("@") || getShortestSubtagLength(localeID) != 1) {
            tmpLocaleID = localeID;
        } else {
            tmpLocaleID = forLanguageTag(localeID).getName();
            if (tmpLocaleID.length() == 0) {
                tmpLocaleID = localeID;
            }
        }
        String name = (String) nameCache.get(tmpLocaleID);
        if (name != null) {
            return name;
        }
        name = new LocaleIDParser(tmpLocaleID).getName();
        nameCache.put(tmpLocaleID, name);
        return name;
    }

    public String toString() {
        return this.localeID;
    }

    public Iterator<String> getKeywords() {
        return getKeywords(this.localeID);
    }

    public static Iterator<String> getKeywords(String localeID) {
        return new LocaleIDParser(localeID).getKeywords();
    }

    public String getKeywordValue(String keywordName) {
        return getKeywordValue(this.localeID, keywordName);
    }

    public static String getKeywordValue(String localeID, String keywordName) {
        return new LocaleIDParser(localeID).getKeywordValue(keywordName);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0069  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String canonicalize(String localeID) {
        LocaleIDParser parser = new LocaleIDParser(localeID, true);
        String baseName = parser.getBaseName();
        boolean foundVariant = false;
        if (localeID.equals("")) {
            return "";
        }
        int i;
        initCANONICALIZE_MAP();
        for (String[] vals : variantsToKeywords) {
            String[] vals2;
            int idx = baseName.lastIndexOf(BaseLocale.SEP + vals2[0]);
            if (idx > -1) {
                foundVariant = true;
                baseName = baseName.substring(0, idx);
                if (baseName.endsWith(BaseLocale.SEP)) {
                    baseName = baseName.substring(0, idx - 1);
                }
                parser.setBaseName(baseName);
                parser.defaultKeywordValue(vals2[1], vals2[2]);
                for (i = 0; i < CANONICALIZE_MAP.length; i++) {
                    if (CANONICALIZE_MAP[i][0].equals(baseName)) {
                        foundVariant = true;
                        vals2 = CANONICALIZE_MAP[i];
                        parser.setBaseName(vals2[1]);
                        if (vals2[2] != null) {
                            parser.defaultKeywordValue(vals2[2], vals2[3]);
                        }
                        if (!foundVariant && parser.getLanguage().equals("nb") && parser.getVariant().equals("NY")) {
                            parser.setBaseName(lscvToID("nn", parser.getScript(), parser.getCountry(), null));
                        }
                        return parser.getName();
                    }
                }
                parser.setBaseName(lscvToID("nn", parser.getScript(), parser.getCountry(), null));
                return parser.getName();
            }
        }
        while (i < CANONICALIZE_MAP.length) {
        }
        parser.setBaseName(lscvToID("nn", parser.getScript(), parser.getCountry(), null));
        return parser.getName();
    }

    public ULocale setKeywordValue(String keyword, String value) {
        return new ULocale(setKeywordValue(this.localeID, keyword, value), (Locale) null);
    }

    public static String setKeywordValue(String localeID, String keyword, String value) {
        LocaleIDParser parser = new LocaleIDParser(localeID);
        parser.setKeywordValue(keyword, value);
        return parser.getName();
    }

    public String getISO3Language() {
        return getISO3Language(this.localeID);
    }

    public static String getISO3Language(String localeID) {
        return LocaleIDs.getISO3Language(getLanguage(localeID));
    }

    public String getISO3Country() {
        return getISO3Country(this.localeID);
    }

    public static String getISO3Country(String localeID) {
        return LocaleIDs.getISO3Country(getCountry(localeID));
    }

    public boolean isRightToLeft() {
        String script = getScript();
        if (script.length() == 0) {
            String lang = getLanguage();
            if (lang.length() == 0) {
                return false;
            }
            int langIndex = LANG_DIR_STRING.indexOf(lang);
            if (langIndex >= 0) {
                switch (LANG_DIR_STRING.charAt(lang.length() + langIndex)) {
                    case '+':
                        return true;
                    case '-':
                        return false;
                }
            }
            script = addLikelySubtags(this).getScript();
            if (script.length() == 0) {
                return false;
            }
        }
        return UScript.isRightToLeft(UScript.getCodeFromName(script));
    }

    public String getDisplayLanguage() {
        return getDisplayLanguageInternal(this, getDefault(Category.DISPLAY), false);
    }

    public String getDisplayLanguage(ULocale displayLocale) {
        return getDisplayLanguageInternal(this, displayLocale, false);
    }

    public static String getDisplayLanguage(String localeID, String displayLocaleID) {
        return getDisplayLanguageInternal(new ULocale(localeID), new ULocale(displayLocaleID), false);
    }

    public static String getDisplayLanguage(String localeID, ULocale displayLocale) {
        return getDisplayLanguageInternal(new ULocale(localeID), displayLocale, false);
    }

    public String getDisplayLanguageWithDialect() {
        return getDisplayLanguageInternal(this, getDefault(Category.DISPLAY), true);
    }

    public String getDisplayLanguageWithDialect(ULocale displayLocale) {
        return getDisplayLanguageInternal(this, displayLocale, true);
    }

    public static String getDisplayLanguageWithDialect(String localeID, String displayLocaleID) {
        return getDisplayLanguageInternal(new ULocale(localeID), new ULocale(displayLocaleID), true);
    }

    public static String getDisplayLanguageWithDialect(String localeID, ULocale displayLocale) {
        return getDisplayLanguageInternal(new ULocale(localeID), displayLocale, true);
    }

    private static String getDisplayLanguageInternal(ULocale locale, ULocale displayLocale, boolean useDialect) {
        return LocaleDisplayNames.getInstance(displayLocale).languageDisplayName(useDialect ? locale.getBaseName() : locale.getLanguage());
    }

    public String getDisplayScript() {
        return getDisplayScriptInternal(this, getDefault(Category.DISPLAY));
    }

    @Deprecated
    public String getDisplayScriptInContext() {
        return getDisplayScriptInContextInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayScript(ULocale displayLocale) {
        return getDisplayScriptInternal(this, displayLocale);
    }

    @Deprecated
    public String getDisplayScriptInContext(ULocale displayLocale) {
        return getDisplayScriptInContextInternal(this, displayLocale);
    }

    public static String getDisplayScript(String localeID, String displayLocaleID) {
        return getDisplayScriptInternal(new ULocale(localeID), new ULocale(displayLocaleID));
    }

    @Deprecated
    public static String getDisplayScriptInContext(String localeID, String displayLocaleID) {
        return getDisplayScriptInContextInternal(new ULocale(localeID), new ULocale(displayLocaleID));
    }

    public static String getDisplayScript(String localeID, ULocale displayLocale) {
        return getDisplayScriptInternal(new ULocale(localeID), displayLocale);
    }

    @Deprecated
    public static String getDisplayScriptInContext(String localeID, ULocale displayLocale) {
        return getDisplayScriptInContextInternal(new ULocale(localeID), displayLocale);
    }

    private static String getDisplayScriptInternal(ULocale locale, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale).scriptDisplayName(locale.getScript());
    }

    private static String getDisplayScriptInContextInternal(ULocale locale, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale).scriptDisplayNameInContext(locale.getScript());
    }

    public String getDisplayCountry() {
        return getDisplayCountryInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayCountry(ULocale displayLocale) {
        return getDisplayCountryInternal(this, displayLocale);
    }

    public static String getDisplayCountry(String localeID, String displayLocaleID) {
        return getDisplayCountryInternal(new ULocale(localeID), new ULocale(displayLocaleID));
    }

    public static String getDisplayCountry(String localeID, ULocale displayLocale) {
        return getDisplayCountryInternal(new ULocale(localeID), displayLocale);
    }

    private static String getDisplayCountryInternal(ULocale locale, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale).regionDisplayName(locale.getCountry());
    }

    public String getDisplayVariant() {
        return getDisplayVariantInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayVariant(ULocale displayLocale) {
        return getDisplayVariantInternal(this, displayLocale);
    }

    public static String getDisplayVariant(String localeID, String displayLocaleID) {
        return getDisplayVariantInternal(new ULocale(localeID), new ULocale(displayLocaleID));
    }

    public static String getDisplayVariant(String localeID, ULocale displayLocale) {
        return getDisplayVariantInternal(new ULocale(localeID), displayLocale);
    }

    private static String getDisplayVariantInternal(ULocale locale, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale).variantDisplayName(locale.getVariant());
    }

    public static String getDisplayKeyword(String keyword) {
        return getDisplayKeywordInternal(keyword, getDefault(Category.DISPLAY));
    }

    public static String getDisplayKeyword(String keyword, String displayLocaleID) {
        return getDisplayKeywordInternal(keyword, new ULocale(displayLocaleID));
    }

    public static String getDisplayKeyword(String keyword, ULocale displayLocale) {
        return getDisplayKeywordInternal(keyword, displayLocale);
    }

    private static String getDisplayKeywordInternal(String keyword, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale).keyDisplayName(keyword);
    }

    public String getDisplayKeywordValue(String keyword) {
        return getDisplayKeywordValueInternal(this, keyword, getDefault(Category.DISPLAY));
    }

    public String getDisplayKeywordValue(String keyword, ULocale displayLocale) {
        return getDisplayKeywordValueInternal(this, keyword, displayLocale);
    }

    public static String getDisplayKeywordValue(String localeID, String keyword, String displayLocaleID) {
        return getDisplayKeywordValueInternal(new ULocale(localeID), keyword, new ULocale(displayLocaleID));
    }

    public static String getDisplayKeywordValue(String localeID, String keyword, ULocale displayLocale) {
        return getDisplayKeywordValueInternal(new ULocale(localeID), keyword, displayLocale);
    }

    private static String getDisplayKeywordValueInternal(ULocale locale, String keyword, ULocale displayLocale) {
        keyword = AsciiUtil.toLowerString(keyword.trim());
        return LocaleDisplayNames.getInstance(displayLocale).keyValueDisplayName(keyword, locale.getKeywordValue(keyword));
    }

    public String getDisplayName() {
        return getDisplayNameInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayName(ULocale displayLocale) {
        return getDisplayNameInternal(this, displayLocale);
    }

    public static String getDisplayName(String localeID, String displayLocaleID) {
        return getDisplayNameInternal(new ULocale(localeID), new ULocale(displayLocaleID));
    }

    public static String getDisplayName(String localeID, ULocale displayLocale) {
        return getDisplayNameInternal(new ULocale(localeID), displayLocale);
    }

    private static String getDisplayNameInternal(ULocale locale, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale).localeDisplayName(locale);
    }

    public String getDisplayNameWithDialect() {
        return getDisplayNameWithDialectInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayNameWithDialect(ULocale displayLocale) {
        return getDisplayNameWithDialectInternal(this, displayLocale);
    }

    public static String getDisplayNameWithDialect(String localeID, String displayLocaleID) {
        return getDisplayNameWithDialectInternal(new ULocale(localeID), new ULocale(displayLocaleID));
    }

    public static String getDisplayNameWithDialect(String localeID, ULocale displayLocale) {
        return getDisplayNameWithDialectInternal(new ULocale(localeID), displayLocale);
    }

    private static String getDisplayNameWithDialectInternal(ULocale locale, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale, DialectHandling.DIALECT_NAMES).localeDisplayName(locale);
    }

    public String getCharacterOrientation() {
        return ICUResourceTableAccess.getTableString("android/icu/impl/data/icudt56b", this, "layout", "characters");
    }

    public String getLineOrientation() {
        return ICUResourceTableAccess.getTableString("android/icu/impl/data/icudt56b", this, "layout", "lines");
    }

    public static ULocale acceptLanguage(String acceptLanguageList, ULocale[] availableLocales, boolean[] fallback) {
        if (acceptLanguageList == null) {
            throw new NullPointerException();
        }
        ULocale[] acceptList;
        try {
            acceptList = parseAcceptLanguage(acceptLanguageList, true);
        } catch (ParseException e) {
            acceptList = null;
        }
        if (acceptList == null) {
            return null;
        }
        return acceptLanguage(acceptList, availableLocales, fallback);
    }

    public static ULocale acceptLanguage(ULocale[] acceptLanguageList, ULocale[] availableLocales, boolean[] fallback) {
        if (fallback != null) {
            fallback[0] = true;
        }
        for (ULocale aLocale : acceptLanguageList) {
            boolean[] setFallback = fallback;
            ULocale aLocale2;
            do {
                int j = 0;
                while (j < availableLocales.length) {
                    if (availableLocales[j].equals(aLocale2)) {
                        if (setFallback != null) {
                            setFallback[0] = false;
                        }
                        return availableLocales[j];
                    } else if (aLocale2.getScript().length() == 0 && availableLocales[j].getScript().length() > 0 && availableLocales[j].getLanguage().equals(aLocale2.getLanguage()) && availableLocales[j].getCountry().equals(aLocale2.getCountry()) && availableLocales[j].getVariant().equals(aLocale2.getVariant()) && minimizeSubtags(availableLocales[j]).getScript().length() == 0) {
                        if (setFallback != null) {
                            setFallback[0] = false;
                        }
                        return aLocale2;
                    } else {
                        j++;
                    }
                }
                Locale parent = LocaleUtility.fallback(aLocale2.toLocale());
                if (parent != null) {
                    aLocale2 = new ULocale(parent);
                } else {
                    aLocale2 = null;
                }
                setFallback = null;
            } while (aLocale2 != null);
        }
        return null;
    }

    public static ULocale acceptLanguage(String acceptLanguageList, boolean[] fallback) {
        return acceptLanguage(acceptLanguageList, getAvailableLocales(), fallback);
    }

    public static ULocale acceptLanguage(ULocale[] acceptLanguageList, boolean[] fallback) {
        return acceptLanguage(acceptLanguageList, getAvailableLocales(), fallback);
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    static android.icu.util.ULocale[] parseAcceptLanguage(java.lang.String r20, boolean r21) throws java.text.ParseException {
        /*
        r7 = new java.util.TreeMap;
        r7.<init>();
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r13 = new java.lang.StringBuilder;
        r13.<init>();
        r15 = 0;
        r17 = new java.lang.StringBuilder;
        r17.<init>();
        r0 = r17;
        r1 = r20;
        r17 = r0.append(r1);
        r18 = ",";
        r17 = r17.append(r18);
        r20 = r17.toString();
        r16 = 0;
        r12 = 0;
        r8 = 0;
    L_0x002c:
        r17 = r20.length();
        r0 = r17;
        if (r8 >= r0) goto L_0x02a3;
    L_0x0034:
        r5 = 0;
        r0 = r20;
        r3 = r0.charAt(r8);
        switch(r15) {
            case 0: goto L_0x0051;
            case 1: goto L_0x0089;
            case 2: goto L_0x00fc;
            case 3: goto L_0x0120;
            case 4: goto L_0x0141;
            case 5: goto L_0x0159;
            case 6: goto L_0x0171;
            case 7: goto L_0x01ab;
            case 8: goto L_0x01d4;
            case 9: goto L_0x01f7;
            case 10: goto L_0x022f;
            default: goto L_0x003e;
        };
    L_0x003e:
        r17 = -1;
        r0 = r17;
        if (r15 != r0) goto L_0x0247;
    L_0x0044:
        r17 = new java.text.ParseException;
        r18 = "Invalid Accept-Language";
        r0 = r17;
        r1 = r18;
        r0.<init>(r1, r8);
        throw r17;
    L_0x0051:
        r17 = 65;
        r0 = r17;
        if (r0 > r3) goto L_0x0064;
    L_0x0057:
        r17 = 90;
        r0 = r17;
        if (r3 > r0) goto L_0x0064;
    L_0x005d:
        r6.append(r3);
        r15 = 1;
        r16 = 0;
        goto L_0x003e;
    L_0x0064:
        r17 = 97;
        r0 = r17;
        if (r0 > r3) goto L_0x0070;
    L_0x006a:
        r17 = 122; // 0x7a float:1.71E-43 double:6.03E-322;
        r0 = r17;
        if (r3 <= r0) goto L_0x005d;
    L_0x0070:
        r17 = 42;
        r0 = r17;
        if (r3 != r0) goto L_0x007b;
    L_0x0076:
        r6.append(r3);
        r15 = 2;
        goto L_0x003e;
    L_0x007b:
        r17 = 32;
        r0 = r17;
        if (r3 == r0) goto L_0x003e;
    L_0x0081:
        r17 = 9;
        r0 = r17;
        if (r3 == r0) goto L_0x003e;
    L_0x0087:
        r15 = -1;
        goto L_0x003e;
    L_0x0089:
        r17 = 65;
        r0 = r17;
        if (r0 > r3) goto L_0x0099;
    L_0x008f:
        r17 = 90;
        r0 = r17;
        if (r3 > r0) goto L_0x0099;
    L_0x0095:
        r6.append(r3);
        goto L_0x003e;
    L_0x0099:
        r17 = 97;
        r0 = r17;
        if (r0 > r3) goto L_0x00a5;
    L_0x009f:
        r17 = 122; // 0x7a float:1.71E-43 double:6.03E-322;
        r0 = r17;
        if (r3 <= r0) goto L_0x0095;
    L_0x00a5:
        r17 = 45;
        r0 = r17;
        if (r3 != r0) goto L_0x00b1;
    L_0x00ab:
        r16 = 1;
        r6.append(r3);
        goto L_0x003e;
    L_0x00b1:
        r17 = 95;
        r0 = r17;
        if (r3 != r0) goto L_0x00c2;
    L_0x00b7:
        if (r21 == 0) goto L_0x00bf;
    L_0x00b9:
        r16 = 1;
        r6.append(r3);
        goto L_0x003e;
    L_0x00bf:
        r15 = -1;
        goto L_0x003e;
    L_0x00c2:
        r17 = 48;
        r0 = r17;
        if (r0 > r3) goto L_0x00d8;
    L_0x00c8:
        r17 = 57;
        r0 = r17;
        if (r3 > r0) goto L_0x00d8;
    L_0x00ce:
        if (r16 == 0) goto L_0x00d5;
    L_0x00d0:
        r6.append(r3);
        goto L_0x003e;
    L_0x00d5:
        r15 = -1;
        goto L_0x003e;
    L_0x00d8:
        r17 = 44;
        r0 = r17;
        if (r3 != r0) goto L_0x00e1;
    L_0x00de:
        r5 = 1;
        goto L_0x003e;
    L_0x00e1:
        r17 = 32;
        r0 = r17;
        if (r3 == r0) goto L_0x00ed;
    L_0x00e7:
        r17 = 9;
        r0 = r17;
        if (r3 != r0) goto L_0x00f0;
    L_0x00ed:
        r15 = 3;
        goto L_0x003e;
    L_0x00f0:
        r17 = 59;
        r0 = r17;
        if (r3 != r0) goto L_0x00f9;
    L_0x00f6:
        r15 = 4;
        goto L_0x003e;
    L_0x00f9:
        r15 = -1;
        goto L_0x003e;
    L_0x00fc:
        r17 = 44;
        r0 = r17;
        if (r3 != r0) goto L_0x0105;
    L_0x0102:
        r5 = 1;
        goto L_0x003e;
    L_0x0105:
        r17 = 32;
        r0 = r17;
        if (r3 == r0) goto L_0x0111;
    L_0x010b:
        r17 = 9;
        r0 = r17;
        if (r3 != r0) goto L_0x0114;
    L_0x0111:
        r15 = 3;
        goto L_0x003e;
    L_0x0114:
        r17 = 59;
        r0 = r17;
        if (r3 != r0) goto L_0x011d;
    L_0x011a:
        r15 = 4;
        goto L_0x003e;
    L_0x011d:
        r15 = -1;
        goto L_0x003e;
    L_0x0120:
        r17 = 44;
        r0 = r17;
        if (r3 != r0) goto L_0x0129;
    L_0x0126:
        r5 = 1;
        goto L_0x003e;
    L_0x0129:
        r17 = 59;
        r0 = r17;
        if (r3 != r0) goto L_0x0132;
    L_0x012f:
        r15 = 4;
        goto L_0x003e;
    L_0x0132:
        r17 = 32;
        r0 = r17;
        if (r3 == r0) goto L_0x003e;
    L_0x0138:
        r17 = 9;
        r0 = r17;
        if (r3 == r0) goto L_0x003e;
    L_0x013e:
        r15 = -1;
        goto L_0x003e;
    L_0x0141:
        r17 = 113; // 0x71 float:1.58E-43 double:5.6E-322;
        r0 = r17;
        if (r3 != r0) goto L_0x014a;
    L_0x0147:
        r15 = 5;
        goto L_0x003e;
    L_0x014a:
        r17 = 32;
        r0 = r17;
        if (r3 == r0) goto L_0x003e;
    L_0x0150:
        r17 = 9;
        r0 = r17;
        if (r3 == r0) goto L_0x003e;
    L_0x0156:
        r15 = -1;
        goto L_0x003e;
    L_0x0159:
        r17 = 61;
        r0 = r17;
        if (r3 != r0) goto L_0x0162;
    L_0x015f:
        r15 = 6;
        goto L_0x003e;
    L_0x0162:
        r17 = 32;
        r0 = r17;
        if (r3 == r0) goto L_0x003e;
    L_0x0168:
        r17 = 9;
        r0 = r17;
        if (r3 == r0) goto L_0x003e;
    L_0x016e:
        r15 = -1;
        goto L_0x003e;
    L_0x0171:
        r17 = 48;
        r0 = r17;
        if (r3 != r0) goto L_0x017e;
    L_0x0177:
        r12 = 0;
        r13.append(r3);
        r15 = 7;
        goto L_0x003e;
    L_0x017e:
        r17 = 49;
        r0 = r17;
        if (r3 != r0) goto L_0x018a;
    L_0x0184:
        r13.append(r3);
        r15 = 7;
        goto L_0x003e;
    L_0x018a:
        r17 = 46;
        r0 = r17;
        if (r3 != r0) goto L_0x019c;
    L_0x0190:
        if (r21 == 0) goto L_0x0199;
    L_0x0192:
        r13.append(r3);
        r15 = 8;
        goto L_0x003e;
    L_0x0199:
        r15 = -1;
        goto L_0x003e;
    L_0x019c:
        r17 = 32;
        r0 = r17;
        if (r3 == r0) goto L_0x003e;
    L_0x01a2:
        r17 = 9;
        r0 = r17;
        if (r3 == r0) goto L_0x003e;
    L_0x01a8:
        r15 = -1;
        goto L_0x003e;
    L_0x01ab:
        r17 = 46;
        r0 = r17;
        if (r3 != r0) goto L_0x01b8;
    L_0x01b1:
        r13.append(r3);
        r15 = 8;
        goto L_0x003e;
    L_0x01b8:
        r17 = 44;
        r0 = r17;
        if (r3 != r0) goto L_0x01c1;
    L_0x01be:
        r5 = 1;
        goto L_0x003e;
    L_0x01c1:
        r17 = 32;
        r0 = r17;
        if (r3 == r0) goto L_0x01cd;
    L_0x01c7:
        r17 = 9;
        r0 = r17;
        if (r3 != r0) goto L_0x01d1;
    L_0x01cd:
        r15 = 10;
        goto L_0x003e;
    L_0x01d1:
        r15 = -1;
        goto L_0x003e;
    L_0x01d4:
        r17 = 48;
        r0 = r17;
        if (r0 <= r3) goto L_0x01e0;
    L_0x01da:
        r17 = 57;
        r0 = r17;
        if (r3 > r0) goto L_0x01f4;
    L_0x01e0:
        if (r12 == 0) goto L_0x01ea;
    L_0x01e2:
        r17 = 48;
        r0 = r17;
        if (r3 == r0) goto L_0x01ea;
    L_0x01e8:
        if (r21 == 0) goto L_0x01f1;
    L_0x01ea:
        r13.append(r3);
        r15 = 9;
        goto L_0x003e;
    L_0x01f1:
        r15 = -1;
        goto L_0x003e;
    L_0x01f4:
        r15 = -1;
        goto L_0x003e;
    L_0x01f7:
        r17 = 48;
        r0 = r17;
        if (r0 > r3) goto L_0x0213;
    L_0x01fd:
        r17 = 57;
        r0 = r17;
        if (r3 > r0) goto L_0x0213;
    L_0x0203:
        if (r12 == 0) goto L_0x020e;
    L_0x0205:
        r17 = 48;
        r0 = r17;
        if (r3 == r0) goto L_0x020e;
    L_0x020b:
        r15 = -1;
        goto L_0x003e;
    L_0x020e:
        r13.append(r3);
        goto L_0x003e;
    L_0x0213:
        r17 = 44;
        r0 = r17;
        if (r3 != r0) goto L_0x021c;
    L_0x0219:
        r5 = 1;
        goto L_0x003e;
    L_0x021c:
        r17 = 32;
        r0 = r17;
        if (r3 == r0) goto L_0x0228;
    L_0x0222:
        r17 = 9;
        r0 = r17;
        if (r3 != r0) goto L_0x022c;
    L_0x0228:
        r15 = 10;
        goto L_0x003e;
    L_0x022c:
        r15 = -1;
        goto L_0x003e;
    L_0x022f:
        r17 = 44;
        r0 = r17;
        if (r3 != r0) goto L_0x0238;
    L_0x0235:
        r5 = 1;
        goto L_0x003e;
    L_0x0238:
        r17 = 32;
        r0 = r17;
        if (r3 == r0) goto L_0x003e;
    L_0x023e:
        r17 = 9;
        r0 = r17;
        if (r3 == r0) goto L_0x003e;
    L_0x0244:
        r15 = -1;
        goto L_0x003e;
    L_0x0247:
        if (r5 == 0) goto L_0x029b;
    L_0x0249:
        r10 = 4607182418800017408; // 0x3ff0000000000000 float:0.0 double:1.0;
        r17 = r13.length();
        if (r17 == 0) goto L_0x0261;
    L_0x0251:
        r17 = r13.toString();	 Catch:{ NumberFormatException -> 0x029f }
        r10 = java.lang.Double.parseDouble(r17);	 Catch:{ NumberFormatException -> 0x029f }
    L_0x0259:
        r18 = 4607182418800017408; // 0x3ff0000000000000 float:0.0 double:1.0;
        r17 = (r10 > r18 ? 1 : (r10 == r18 ? 0 : -1));
        if (r17 <= 0) goto L_0x0261;
    L_0x025f:
        r10 = 4607182418800017408; // 0x3ff0000000000000 float:0.0 double:1.0;
    L_0x0261:
        r17 = 0;
        r0 = r17;
        r17 = r6.charAt(r0);
        r18 = 42;
        r0 = r17;
        r1 = r18;
        if (r0 == r1) goto L_0x028c;
    L_0x0271:
        r14 = r7.size();
        r4 = new android.icu.util.ULocale$1ULocaleAcceptLanguageQ;
        r4.<init>(r10, r14);
        r17 = new android.icu.util.ULocale;
        r18 = r6.toString();
        r18 = canonicalize(r18);
        r17.<init>(r18);
        r0 = r17;
        r7.put(r4, r0);
    L_0x028c:
        r17 = 0;
        r0 = r17;
        r6.setLength(r0);
        r17 = 0;
        r0 = r17;
        r13.setLength(r0);
        r15 = 0;
    L_0x029b:
        r8 = r8 + 1;
        goto L_0x002c;
    L_0x029f:
        r9 = move-exception;
        r10 = 4607182418800017408; // 0x3ff0000000000000 float:0.0 double:1.0;
        goto L_0x0259;
    L_0x02a3:
        if (r15 == 0) goto L_0x02b2;
    L_0x02a5:
        r17 = new java.text.ParseException;
        r18 = "Invalid AcceptlLanguage";
        r0 = r17;
        r1 = r18;
        r0.<init>(r1, r8);
        throw r17;
    L_0x02b2:
        r17 = r7.values();
        r18 = r7.size();
        r0 = r18;
        r0 = new android.icu.util.ULocale[r0];
        r18 = r0;
        r2 = r17.toArray(r18);
        r2 = (android.icu.util.ULocale[]) r2;
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.ULocale.parseAcceptLanguage(java.lang.String, boolean):android.icu.util.ULocale[]");
    }

    public static ULocale addLikelySubtags(ULocale loc) {
        String[] tags = new String[3];
        String trailing = null;
        int trailingIndex = parseTagString(loc.localeID, tags);
        if (trailingIndex < loc.localeID.length()) {
            trailing = loc.localeID.substring(trailingIndex);
        }
        String newLocaleID = createLikelySubtagsString(tags[0], tags[1], tags[2], trailing);
        return newLocaleID == null ? loc : new ULocale(newLocaleID);
    }

    public static ULocale minimizeSubtags(ULocale loc) {
        return minimizeSubtags(loc, Minimize.FAVOR_REGION);
    }

    @Deprecated
    public static ULocale minimizeSubtags(ULocale loc, Minimize fieldToFavor) {
        String[] tags = new String[3];
        int trailingIndex = parseTagString(loc.localeID, tags);
        String originalLang = tags[0];
        String originalScript = tags[1];
        String originalRegion = tags[2];
        String originalTrailing = null;
        if (trailingIndex < loc.localeID.length()) {
            originalTrailing = loc.localeID.substring(trailingIndex);
        }
        String maximizedLocaleID = createLikelySubtagsString(originalLang, originalScript, originalRegion, null);
        if (isEmptyString(maximizedLocaleID)) {
            return loc;
        }
        if (createLikelySubtagsString(originalLang, null, null, null).equals(maximizedLocaleID)) {
            return new ULocale(createTagString(originalLang, null, null, originalTrailing));
        }
        if (fieldToFavor == Minimize.FAVOR_REGION) {
            if (originalRegion.length() != 0 && createLikelySubtagsString(originalLang, null, originalRegion, null).equals(maximizedLocaleID)) {
                return new ULocale(createTagString(originalLang, null, originalRegion, originalTrailing));
            }
            if (originalScript.length() != 0 && createLikelySubtagsString(originalLang, originalScript, null, null).equals(maximizedLocaleID)) {
                return new ULocale(createTagString(originalLang, originalScript, null, originalTrailing));
            }
        } else if (originalScript.length() != 0 && createLikelySubtagsString(originalLang, originalScript, null, null).equals(maximizedLocaleID)) {
            return new ULocale(createTagString(originalLang, originalScript, null, originalTrailing));
        } else {
            if (originalRegion.length() != 0 && createLikelySubtagsString(originalLang, null, originalRegion, null).equals(maximizedLocaleID)) {
                return new ULocale(createTagString(originalLang, null, originalRegion, originalTrailing));
            }
        }
        return loc;
    }

    private static boolean isEmptyString(String string) {
        return string == null || string.length() == 0;
    }

    private static void appendTag(String tag, StringBuilder buffer) {
        if (buffer.length() != 0) {
            buffer.append(UNDERSCORE);
        }
        buffer.append(tag);
    }

    private static String createTagString(String lang, String script, String region, String trailing, String alternateTags) {
        LocaleIDParser parser = null;
        boolean regionAppended = false;
        StringBuilder tag = new StringBuilder();
        if (!isEmptyString(lang)) {
            appendTag(lang, tag);
        } else if (isEmptyString(alternateTags)) {
            appendTag(UNDEFINED_LANGUAGE, tag);
        } else {
            parser = new LocaleIDParser(alternateTags);
            String alternateLang = parser.getLanguage();
            if (isEmptyString(alternateLang)) {
                alternateLang = UNDEFINED_LANGUAGE;
            }
            appendTag(alternateLang, tag);
        }
        if (!isEmptyString(script)) {
            appendTag(script, tag);
        } else if (!isEmptyString(alternateTags)) {
            if (parser == null) {
                parser = new LocaleIDParser(alternateTags);
            }
            String alternateScript = parser.getScript();
            if (!isEmptyString(alternateScript)) {
                appendTag(alternateScript, tag);
            }
        }
        if (!isEmptyString(region)) {
            appendTag(region, tag);
            regionAppended = true;
        } else if (!isEmptyString(alternateTags)) {
            if (parser == null) {
                parser = new LocaleIDParser(alternateTags);
            }
            String alternateRegion = parser.getCountry();
            if (!isEmptyString(alternateRegion)) {
                appendTag(alternateRegion, tag);
                regionAppended = true;
            }
        }
        if (trailing != null && trailing.length() > 1) {
            int separators = 0;
            if (trailing.charAt(0) != UNDERSCORE) {
                separators = 1;
            } else if (trailing.charAt(1) == UNDERSCORE) {
                separators = 2;
            }
            if (!regionAppended) {
                if (separators == 1) {
                    tag.append(UNDERSCORE);
                }
                tag.append(trailing);
            } else if (separators == 2) {
                tag.append(trailing.substring(1));
            } else {
                tag.append(trailing);
            }
        }
        return tag.toString();
    }

    static String createTagString(String lang, String script, String region, String trailing) {
        return createTagString(lang, script, region, trailing, null);
    }

    private static int parseTagString(String localeID, String[] tags) {
        LocaleIDParser parser = new LocaleIDParser(localeID);
        String lang = parser.getLanguage();
        String script = parser.getScript();
        String region = parser.getCountry();
        if (isEmptyString(lang)) {
            tags[0] = UNDEFINED_LANGUAGE;
        } else {
            tags[0] = lang;
        }
        if (script.equals(UNDEFINED_SCRIPT)) {
            tags[1] = "";
        } else {
            tags[1] = script;
        }
        if (region.equals(UNDEFINED_REGION)) {
            tags[2] = "";
        } else {
            tags[2] = region;
        }
        String variant = parser.getVariant();
        int index;
        if (isEmptyString(variant)) {
            index = localeID.indexOf(64);
            if (index == -1) {
                index = localeID.length();
            }
            return index;
        }
        index = localeID.indexOf(variant);
        if (index > 0) {
            index--;
        }
        return index;
    }

    private static String lookupLikelySubtags(String localeId) {
        try {
            return UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", "likelySubtags").getString(localeId);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    private static String createLikelySubtagsString(String lang, String script, String region, String variants) {
        String likelySubtags;
        if (!(isEmptyString(script) || isEmptyString(region))) {
            likelySubtags = lookupLikelySubtags(createTagString(lang, script, region, null));
            if (likelySubtags != null) {
                return createTagString(null, null, null, variants, likelySubtags);
            }
        }
        if (!isEmptyString(script)) {
            likelySubtags = lookupLikelySubtags(createTagString(lang, script, null, null));
            if (likelySubtags != null) {
                return createTagString(null, null, region, variants, likelySubtags);
            }
        }
        if (!isEmptyString(region)) {
            likelySubtags = lookupLikelySubtags(createTagString(lang, null, region, null));
            if (likelySubtags != null) {
                return createTagString(null, script, null, variants, likelySubtags);
            }
        }
        likelySubtags = lookupLikelySubtags(createTagString(lang, null, null, null));
        if (likelySubtags != null) {
            return createTagString(null, script, region, variants, likelySubtags);
        }
        return null;
    }

    public String getExtension(char key) {
        if (LocaleExtensions.isValidKey(key)) {
            return extensions().getExtensionValue(Character.valueOf(key));
        }
        throw new IllegalArgumentException("Invalid extension key: " + key);
    }

    public Set<Character> getExtensionKeys() {
        return extensions().getKeys();
    }

    public Set<String> getUnicodeLocaleAttributes() {
        return extensions().getUnicodeLocaleAttributes();
    }

    public String getUnicodeLocaleType(String key) {
        if (LocaleExtensions.isValidUnicodeLocaleKey(key)) {
            return extensions().getUnicodeLocaleType(key);
        }
        throw new IllegalArgumentException("Invalid Unicode locale key: " + key);
    }

    public Set<String> getUnicodeLocaleKeys() {
        return extensions().getUnicodeLocaleKeys();
    }

    public String toLanguageTag() {
        BaseLocale base = base();
        LocaleExtensions exts = extensions();
        if (base.getVariant().equalsIgnoreCase("POSIX")) {
            base = BaseLocale.getInstance(base.getLanguage(), base.getScript(), base.getRegion(), "");
            if (exts.getUnicodeLocaleType("va") == null) {
                InternalLocaleBuilder ilocbld = new InternalLocaleBuilder();
                try {
                    ilocbld.setLocale(BaseLocale.ROOT, exts);
                    ilocbld.setUnicodeLocaleKeyword("va", "posix");
                    exts = ilocbld.getLocaleExtensions();
                } catch (LocaleSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        LanguageTag tag = LanguageTag.parseLocale(base, exts);
        StringBuilder buf = new StringBuilder();
        String subtag = tag.getLanguage();
        if (subtag.length() > 0) {
            buf.append(LanguageTag.canonicalizeLanguage(subtag));
        }
        subtag = tag.getScript();
        if (subtag.length() > 0) {
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeScript(subtag));
        }
        subtag = tag.getRegion();
        if (subtag.length() > 0) {
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeRegion(subtag));
        }
        for (String s : tag.getVariants()) {
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeVariant(s));
        }
        for (String s2 : tag.getExtensions()) {
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeExtension(s2));
        }
        subtag = tag.getPrivateuse();
        if (subtag.length() > 0) {
            if (buf.length() > 0) {
                buf.append(LanguageTag.SEP);
            }
            buf.append(LanguageTag.PRIVATEUSE).append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizePrivateuse(subtag));
        }
        return buf.toString();
    }

    public static ULocale forLanguageTag(String languageTag) {
        LanguageTag tag = LanguageTag.parse(languageTag, null);
        InternalLocaleBuilder bldr = new InternalLocaleBuilder();
        bldr.setLanguageTag(tag);
        return getInstance(bldr.getBaseLocale(), bldr.getLocaleExtensions());
    }

    public static String toUnicodeLocaleKey(String keyword) {
        String bcpKey = KeyTypeData.toBcpKey(keyword);
        if (bcpKey == null && UnicodeLocaleExtension.isKey(keyword)) {
            return AsciiUtil.toLowerString(keyword);
        }
        return bcpKey;
    }

    public static String toUnicodeLocaleType(String keyword, String value) {
        String bcpType = KeyTypeData.toBcpType(keyword, value, null, null);
        if (bcpType == null && UnicodeLocaleExtension.isType(value)) {
            return AsciiUtil.toLowerString(value);
        }
        return bcpType;
    }

    public static String toLegacyKey(String keyword) {
        String legacyKey = KeyTypeData.toLegacyKey(keyword);
        if (legacyKey == null && keyword.matches("[0-9a-zA-Z]+")) {
            return AsciiUtil.toLowerString(keyword);
        }
        return legacyKey;
    }

    public static String toLegacyType(String keyword, String value) {
        String legacyType = KeyTypeData.toLegacyType(keyword, value, null, null);
        if (legacyType == null && value.matches("[0-9a-zA-Z]+([_/\\-][0-9a-zA-Z]+)*")) {
            return AsciiUtil.toLowerString(value);
        }
        return legacyType;
    }

    private static ULocale getInstance(BaseLocale base, LocaleExtensions exts) {
        String id = lscvToID(base.getLanguage(), base.getScript(), base.getRegion(), base.getVariant());
        Set<Character> extKeys = exts.getKeys();
        if (!extKeys.isEmpty()) {
            TreeMap<String, String> kwds = new TreeMap();
            for (Character key : extKeys) {
                Extension ext = exts.getExtension(key);
                if (ext instanceof UnicodeLocaleExtension) {
                    UnicodeLocaleExtension uext = (UnicodeLocaleExtension) ext;
                    for (String bcpKey : uext.getUnicodeLocaleKeys()) {
                        String bcpType = uext.getUnicodeLocaleType(bcpKey);
                        String lkey = toLegacyKey(bcpKey);
                        if (bcpType.length() == 0) {
                            bcpType = "yes";
                        }
                        String ltype = toLegacyType(bcpKey, bcpType);
                        if (lkey.equals("va") && ltype.equals("posix") && base.getVariant().length() == 0) {
                            id = id + "_POSIX";
                        } else {
                            kwds.put(lkey, ltype);
                        }
                    }
                    Set<String> uattributes = uext.getUnicodeLocaleAttributes();
                    if (uattributes.size() > 0) {
                        StringBuilder attrbuf = new StringBuilder();
                        for (String attr : uattributes) {
                            if (attrbuf.length() > 0) {
                                attrbuf.append('-');
                            }
                            attrbuf.append(attr);
                        }
                        kwds.put(LOCALE_ATTRIBUTE_KEY, attrbuf.toString());
                    }
                } else {
                    kwds.put(String.valueOf(key), ext.getValue());
                }
            }
            if (!kwds.isEmpty()) {
                StringBuilder buf = new StringBuilder(id);
                buf.append("@");
                boolean insertSep = false;
                for (Entry<String, String> kwd : kwds.entrySet()) {
                    if (insertSep) {
                        buf.append(";");
                    } else {
                        insertSep = true;
                    }
                    buf.append((String) kwd.getKey());
                    buf.append("=");
                    buf.append((String) kwd.getValue());
                }
                id = buf.toString();
            }
        }
        return new ULocale(id);
    }

    private BaseLocale base() {
        if (this.baseLocale == null) {
            String variant = "";
            String region = variant;
            String script = variant;
            String language = variant;
            if (!equals(ROOT)) {
                LocaleIDParser lp = new LocaleIDParser(this.localeID);
                language = lp.getLanguage();
                script = lp.getScript();
                region = lp.getCountry();
                variant = lp.getVariant();
            }
            this.baseLocale = BaseLocale.getInstance(language, script, region, variant);
        }
        return this.baseLocale;
    }

    private LocaleExtensions extensions() {
        if (this.extensions == null) {
            Iterator<String> kwitr = getKeywords();
            if (kwitr == null) {
                this.extensions = LocaleExtensions.EMPTY_EXTENSIONS;
            } else {
                InternalLocaleBuilder intbld = new InternalLocaleBuilder();
                while (kwitr.hasNext()) {
                    String key = (String) kwitr.next();
                    if (key.equals(LOCALE_ATTRIBUTE_KEY)) {
                        for (String uattr : getKeywordValue(key).split("[-_]")) {
                            try {
                                intbld.addUnicodeLocaleAttribute(uattr);
                            } catch (LocaleSyntaxException e) {
                            }
                        }
                    } else if (key.length() >= 2) {
                        String bcpKey = toUnicodeLocaleKey(key);
                        String bcpType = toUnicodeLocaleType(key, getKeywordValue(key));
                        if (!(bcpKey == null || bcpType == null)) {
                            try {
                                intbld.setUnicodeLocaleKeyword(bcpKey, bcpType);
                            } catch (LocaleSyntaxException e2) {
                            }
                        }
                    } else if (key.length() == 1 && key.charAt(0) != 'u') {
                        try {
                            intbld.setExtension(key.charAt(0), getKeywordValue(key).replace(BaseLocale.SEP, LanguageTag.SEP));
                        } catch (LocaleSyntaxException e3) {
                        }
                    }
                }
                this.extensions = intbld.getLocaleExtensions();
            }
        }
        return this.extensions;
    }
}
