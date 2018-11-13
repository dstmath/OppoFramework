package android.icu.impl.coll;

import android.icu.impl.ICUResourceBundle;
import android.icu.impl.ICUResourceBundle.OpenType;
import android.icu.util.ICUUncheckedIOException;
import android.icu.util.Output;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.io.IOException;
import java.util.MissingResourceException;

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
public final class CollationLoader {
    private static volatile String rootRules;

    private static final class ASCII {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.coll.CollationLoader.ASCII.<init>():void, dex: 
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
        private ASCII() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.coll.CollationLoader.ASCII.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationLoader.ASCII.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.coll.CollationLoader.ASCII.toLowerCase(java.lang.String):java.lang.String, dex: 
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
        static java.lang.String toLowerCase(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.coll.CollationLoader.ASCII.toLowerCase(java.lang.String):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationLoader.ASCII.toLowerCase(java.lang.String):java.lang.String");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.coll.CollationLoader.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.coll.CollationLoader.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationLoader.<clinit>():void");
    }

    private CollationLoader() {
    }

    private static void loadRootRules() {
        if (rootRules == null) {
            synchronized (CollationLoader.class) {
                if (rootRules == null) {
                    rootRules = UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b/coll", ULocale.ROOT).getString("UCARules");
                }
            }
        }
    }

    public static String getRootRules() {
        loadRootRules();
        return rootRules;
    }

    static String loadRules(ULocale locale, String collationType) {
        return ((ICUResourceBundle) UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b/coll", locale)).getWithFallback("collations/" + ASCII.toLowerCase(collationType)).getString("Sequence");
    }

    private static final UResourceBundle findWithFallback(UResourceBundle table, String entryName) {
        return ((ICUResourceBundle) table).findWithFallback(entryName);
    }

    public static CollationTailoring loadTailoring(ULocale locale, Output<ULocale> outValidLocale) {
        CollationTailoring root = CollationRoot.getRoot();
        String localeName = locale.getName();
        if (localeName.length() == 0 || localeName.equals("root")) {
            outValidLocale.value = ULocale.ROOT;
            return root;
        }
        try {
            UResourceBundle bundle = ICUResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b/coll", locale, OpenType.LOCALE_ROOT);
            ULocale validLocale = bundle.getULocale();
            String validLocaleName = validLocale.getName();
            if (validLocaleName.length() == 0 || validLocaleName.equals("root")) {
                validLocale = ULocale.ROOT;
            }
            outValidLocale.value = validLocale;
            try {
                UResourceBundle collations = bundle.get("collations");
                if (collations == null) {
                    return root;
                }
                String type = locale.getKeywordValue("collation");
                String defaultType = "standard";
                String defT = ((ICUResourceBundle) collations).findStringWithFallback("default");
                if (defT != null) {
                    defaultType = defT;
                }
                if (type == null || type.equals("default")) {
                    type = defaultType;
                } else {
                    type = ASCII.toLowerCase(type);
                }
                UResourceBundle data = findWithFallback(collations, type);
                if (data == null && type.length() > 6 && type.startsWith("search")) {
                    type = "search";
                    data = findWithFallback(collations, type);
                }
                if (data == null && !type.equals(defaultType)) {
                    type = defaultType;
                    data = findWithFallback(collations, type);
                }
                if (data == null && !type.equals("standard")) {
                    type = "standard";
                    data = findWithFallback(collations, type);
                }
                if (data == null) {
                    return root;
                }
                ULocale actualLocale = data.getULocale();
                String actualLocaleName = actualLocale.getName();
                if (actualLocaleName.length() == 0 || actualLocaleName.equals("root")) {
                    actualLocale = ULocale.ROOT;
                    if (type.equals("standard")) {
                        return root;
                    }
                }
                CollationTailoring collationTailoring = new CollationTailoring(root.settings);
                collationTailoring.actualLocale = actualLocale;
                try {
                    CollationDataReader.read(root, data.get("%%CollationBin").getBinary(), collationTailoring);
                    try {
                        collationTailoring.setRulesResource(data.get("Sequence"));
                    } catch (MissingResourceException e) {
                    }
                    if (!type.equals(defaultType)) {
                        outValidLocale.value = validLocale.setKeywordValue("collation", type);
                    }
                    if (!actualLocale.equals(validLocale)) {
                        defT = ((ICUResourceBundle) UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b/coll", actualLocale)).findStringWithFallback("collations/default");
                        if (defT != null) {
                            defaultType = defT;
                        }
                    }
                    if (!type.equals(defaultType)) {
                        collationTailoring.actualLocale = collationTailoring.actualLocale.setKeywordValue("collation", type);
                    }
                    return collationTailoring;
                } catch (IOException e2) {
                    throw new ICUUncheckedIOException("Failed to load collation tailoring data for locale:" + actualLocale + " type:" + type, e2);
                }
            } catch (MissingResourceException e3) {
                return root;
            }
        } catch (MissingResourceException e4) {
            outValidLocale.value = ULocale.ROOT;
            return root;
        }
    }
}
