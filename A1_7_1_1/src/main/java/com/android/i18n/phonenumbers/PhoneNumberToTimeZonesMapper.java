package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.prefixmapper.PrefixTimeZonesMap;
import java.util.List;
import java.util.logging.Logger;

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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.collectAllInsns(BlockUtils.java:556)
    	at jadx.core.dex.visitors.ClassModifier.removeBridgeMethod(ClassModifier.java:197)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticMethods(ClassModifier.java:135)
    	at jadx.core.dex.visitors.ClassModifier.lambda$visit$0(ClassModifier.java:49)
    	at java.util.ArrayList.forEach(ArrayList.java:1251)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:49)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class PhoneNumberToTimeZonesMapper {
    private static final Logger LOGGER = null;
    private static final String MAPPING_DATA_DIRECTORY = "/com/android/i18n/phonenumbers/timezones/data/";
    private static final String MAPPING_DATA_FILE_NAME = "map_data";
    private static final String UNKNOWN_TIMEZONE = "Etc/Unknown";
    static final List<String> UNKNOWN_TIME_ZONE_LIST = null;
    private PrefixTimeZonesMap prefixTimeZonesMap;

    private static class LazyHolder {
        private static final PhoneNumberToTimeZonesMapper INSTANCE = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.LazyHolder.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.LazyHolder.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.LazyHolder.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.LazyHolder.<init>():void, dex: 
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
        private LazyHolder() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.LazyHolder.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.LazyHolder.<init>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.<init>(com.android.i18n.phonenumbers.prefixmapper.PrefixTimeZonesMap):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private PhoneNumberToTimeZonesMapper(com.android.i18n.phonenumbers.prefixmapper.PrefixTimeZonesMap r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.<init>(com.android.i18n.phonenumbers.prefixmapper.PrefixTimeZonesMap):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.<init>(com.android.i18n.phonenumbers.prefixmapper.PrefixTimeZonesMap):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.<init>(com.android.i18n.phonenumbers.prefixmapper.PrefixTimeZonesMap, com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    /* synthetic */ PhoneNumberToTimeZonesMapper(com.android.i18n.phonenumbers.prefixmapper.PrefixTimeZonesMap r1, com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.<init>(com.android.i18n.phonenumbers.prefixmapper.PrefixTimeZonesMap, com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.<init>(com.android.i18n.phonenumbers.prefixmapper.PrefixTimeZonesMap, com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.<init>(java.lang.String):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    PhoneNumberToTimeZonesMapper(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.<init>(java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.<init>(java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.close(java.io.InputStream):void, dex: 
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
    private static void close(java.io.InputStream r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.close(java.io.InputStream):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.close(java.io.InputStream):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.getCountryLevelTimeZonesforNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):java.util.List<java.lang.String>, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private java.util.List<java.lang.String> getCountryLevelTimeZonesforNumber(com.android.i18n.phonenumbers.Phonenumber.PhoneNumber r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.getCountryLevelTimeZonesforNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):java.util.List<java.lang.String>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.getCountryLevelTimeZonesforNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):java.util.List<java.lang.String>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.getTimeZonesForGeocodableNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):java.util.List<java.lang.String>, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private java.util.List<java.lang.String> getTimeZonesForGeocodableNumber(com.android.i18n.phonenumbers.Phonenumber.PhoneNumber r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.getTimeZonesForGeocodableNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):java.util.List<java.lang.String>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.getTimeZonesForGeocodableNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):java.util.List<java.lang.String>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.loadPrefixTimeZonesMapFromFile(java.lang.String):com.android.i18n.phonenumbers.prefixmapper.PrefixTimeZonesMap, dex: 
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
    private static com.android.i18n.phonenumbers.prefixmapper.PrefixTimeZonesMap loadPrefixTimeZonesMapFromFile(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.loadPrefixTimeZonesMapFromFile(java.lang.String):com.android.i18n.phonenumbers.prefixmapper.PrefixTimeZonesMap, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.loadPrefixTimeZonesMapFromFile(java.lang.String):com.android.i18n.phonenumbers.prefixmapper.PrefixTimeZonesMap");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.getTimeZonesForNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):java.util.List<java.lang.String>, dex: 
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
    public java.util.List<java.lang.String> getTimeZonesForNumber(com.android.i18n.phonenumbers.Phonenumber.PhoneNumber r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.getTimeZonesForNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):java.util.List<java.lang.String>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.getTimeZonesForNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):java.util.List<java.lang.String>");
    }

    public static synchronized PhoneNumberToTimeZonesMapper getInstance() {
        PhoneNumberToTimeZonesMapper -get0;
        synchronized (PhoneNumberToTimeZonesMapper.class) {
            -get0 = LazyHolder.INSTANCE;
        }
        return -get0;
    }

    public List<String> getTimeZonesForGeographicalNumber(PhoneNumber number) {
        return getTimeZonesForGeocodableNumber(number);
    }

    private boolean canBeGeocoded(PhoneNumberType numberType) {
        if (numberType == PhoneNumberType.FIXED_LINE || numberType == PhoneNumberType.MOBILE || numberType == PhoneNumberType.FIXED_LINE_OR_MOBILE) {
            return true;
        }
        return false;
    }

    public static String getUnknownTimeZone() {
        return UNKNOWN_TIMEZONE;
    }
}
