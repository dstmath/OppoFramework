package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.internal.MatcherApi;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class ShortNumberInfo {
    /* renamed from: -com-android-i18n-phonenumbers-ShortNumberInfo$ShortNumberCostSwitchesValues */
    private static final /* synthetic */ int[] f3x1c15fcc = null;
    private static final ShortNumberInfo INSTANCE = null;
    private static final Set<String> REGIONS_WHERE_EMERGENCY_NUMBERS_MUST_BE_EXACT = null;
    private static final Logger logger = null;
    private final Map<Integer, List<String>> countryCallingCodeToRegionCodeMap;
    private final MatcherApi matcherApi;

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum ShortNumberCost {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.i18n.phonenumbers.ShortNumberInfo.ShortNumberCost.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.i18n.phonenumbers.ShortNumberInfo.ShortNumberCost.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.ShortNumberCost.<clinit>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.-getcom-android-i18n-phonenumbers-ShortNumberInfo$ShortNumberCostSwitchesValues():int[], dex: 
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
    /* renamed from: -getcom-android-i18n-phonenumbers-ShortNumberInfo$ShortNumberCostSwitchesValues */
    private static /* synthetic */ int[] m3xf95e7a70() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.-getcom-android-i18n-phonenumbers-ShortNumberInfo$ShortNumberCostSwitchesValues():int[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.-getcom-android-i18n-phonenumbers-ShortNumberInfo$ShortNumberCostSwitchesValues():int[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.i18n.phonenumbers.ShortNumberInfo.<init>(com.android.i18n.phonenumbers.internal.MatcherApi):void, dex: 
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
    ShortNumberInfo(com.android.i18n.phonenumbers.internal.MatcherApi r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.i18n.phonenumbers.ShortNumberInfo.<init>(com.android.i18n.phonenumbers.internal.MatcherApi):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.<init>(com.android.i18n.phonenumbers.internal.MatcherApi):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.getNationalSignificantNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):java.lang.String, dex: 
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
    private static java.lang.String getNationalSignificantNumber(com.android.i18n.phonenumbers.Phonenumber.PhoneNumber r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.getNationalSignificantNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.getNationalSignificantNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.getRegionCodeForShortNumberFromRegionList(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber, java.util.List):java.lang.String, dex: 
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
    private java.lang.String getRegionCodeForShortNumberFromRegionList(com.android.i18n.phonenumbers.Phonenumber.PhoneNumber r1, java.util.List<java.lang.String> r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.getRegionCodeForShortNumberFromRegionList(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber, java.util.List):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.getRegionCodeForShortNumberFromRegionList(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber, java.util.List):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.i18n.phonenumbers.ShortNumberInfo.getRegionCodesForCountryCode(int):java.util.List<java.lang.String>, dex: 
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
    private java.util.List<java.lang.String> getRegionCodesForCountryCode(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.i18n.phonenumbers.ShortNumberInfo.getRegionCodesForCountryCode(int):java.util.List<java.lang.String>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.getRegionCodesForCountryCode(int):java.util.List<java.lang.String>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.matchesEmergencyNumberHelper(java.lang.String, java.lang.String, boolean):boolean, dex: 
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
    private boolean matchesEmergencyNumberHelper(java.lang.String r1, java.lang.String r2, boolean r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.matchesEmergencyNumberHelper(java.lang.String, java.lang.String, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.matchesEmergencyNumberHelper(java.lang.String, java.lang.String, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.i18n.phonenumbers.ShortNumberInfo.matchesPossibleNumberAndNationalNumber(java.lang.String, com.android.i18n.phonenumbers.Phonemetadata$PhoneNumberDesc):boolean, dex: 
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
    private boolean matchesPossibleNumberAndNationalNumber(java.lang.String r1, com.android.i18n.phonenumbers.Phonemetadata.PhoneNumberDesc r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.i18n.phonenumbers.ShortNumberInfo.matchesPossibleNumberAndNationalNumber(java.lang.String, com.android.i18n.phonenumbers.Phonemetadata$PhoneNumberDesc):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.matchesPossibleNumberAndNationalNumber(java.lang.String, com.android.i18n.phonenumbers.Phonemetadata$PhoneNumberDesc):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.regionDialingFromMatchesNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber, java.lang.String):boolean, dex: 
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
    private boolean regionDialingFromMatchesNumber(com.android.i18n.phonenumbers.Phonenumber.PhoneNumber r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.regionDialingFromMatchesNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber, java.lang.String):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.regionDialingFromMatchesNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber, java.lang.String):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.getExampleShortNumber(java.lang.String):java.lang.String, dex: 
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
    java.lang.String getExampleShortNumber(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.getExampleShortNumber(java.lang.String):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.getExampleShortNumber(java.lang.String):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.getExampleShortNumberForCost(java.lang.String, com.android.i18n.phonenumbers.ShortNumberInfo$ShortNumberCost):java.lang.String, dex: 
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
    java.lang.String getExampleShortNumberForCost(java.lang.String r1, com.android.i18n.phonenumbers.ShortNumberInfo.ShortNumberCost r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.getExampleShortNumberForCost(java.lang.String, com.android.i18n.phonenumbers.ShortNumberInfo$ShortNumberCost):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.getExampleShortNumberForCost(java.lang.String, com.android.i18n.phonenumbers.ShortNumberInfo$ShortNumberCost):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.getExpectedCost(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):com.android.i18n.phonenumbers.ShortNumberInfo$ShortNumberCost, dex: 
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
    public com.android.i18n.phonenumbers.ShortNumberInfo.ShortNumberCost getExpectedCost(com.android.i18n.phonenumbers.Phonenumber.PhoneNumber r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.getExpectedCost(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):com.android.i18n.phonenumbers.ShortNumberInfo$ShortNumberCost, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.getExpectedCost(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):com.android.i18n.phonenumbers.ShortNumberInfo$ShortNumberCost");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.getExpectedCostForRegion(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber, java.lang.String):com.android.i18n.phonenumbers.ShortNumberInfo$ShortNumberCost, dex: 
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
    public com.android.i18n.phonenumbers.ShortNumberInfo.ShortNumberCost getExpectedCostForRegion(com.android.i18n.phonenumbers.Phonenumber.PhoneNumber r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.getExpectedCostForRegion(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber, java.lang.String):com.android.i18n.phonenumbers.ShortNumberInfo$ShortNumberCost, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.getExpectedCostForRegion(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber, java.lang.String):com.android.i18n.phonenumbers.ShortNumberInfo$ShortNumberCost");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.getExpectedCostForRegion(java.lang.String, java.lang.String):com.android.i18n.phonenumbers.ShortNumberInfo$ShortNumberCost, dex: 
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
    @java.lang.Deprecated
    public com.android.i18n.phonenumbers.ShortNumberInfo.ShortNumberCost getExpectedCostForRegion(java.lang.String r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.getExpectedCostForRegion(java.lang.String, java.lang.String):com.android.i18n.phonenumbers.ShortNumberInfo$ShortNumberCost, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.getExpectedCostForRegion(java.lang.String, java.lang.String):com.android.i18n.phonenumbers.ShortNumberInfo$ShortNumberCost");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.isCarrierSpecific(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):boolean, dex: 
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
    public boolean isCarrierSpecific(com.android.i18n.phonenumbers.Phonenumber.PhoneNumber r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.isCarrierSpecific(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.isCarrierSpecific(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.isPossibleShortNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):boolean, dex: 
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
    public boolean isPossibleShortNumber(com.android.i18n.phonenumbers.Phonenumber.PhoneNumber r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.isPossibleShortNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.isPossibleShortNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.i18n.phonenumbers.ShortNumberInfo.isPossibleShortNumberForRegion(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber, java.lang.String):boolean, dex: 
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
    public boolean isPossibleShortNumberForRegion(com.android.i18n.phonenumbers.Phonenumber.PhoneNumber r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.i18n.phonenumbers.ShortNumberInfo.isPossibleShortNumberForRegion(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber, java.lang.String):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.isPossibleShortNumberForRegion(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber, java.lang.String):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.i18n.phonenumbers.ShortNumberInfo.isPossibleShortNumberForRegion(java.lang.String, java.lang.String):boolean, dex: 
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
    @java.lang.Deprecated
    public boolean isPossibleShortNumberForRegion(java.lang.String r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.i18n.phonenumbers.ShortNumberInfo.isPossibleShortNumberForRegion(java.lang.String, java.lang.String):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.isPossibleShortNumberForRegion(java.lang.String, java.lang.String):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.i18n.phonenumbers.ShortNumberInfo.isValidShortNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):boolean, dex:  in method: com.android.i18n.phonenumbers.ShortNumberInfo.isValidShortNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.i18n.phonenumbers.ShortNumberInfo.isValidShortNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):boolean, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public boolean isValidShortNumber(com.android.i18n.phonenumbers.Phonenumber.PhoneNumber r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.i18n.phonenumbers.ShortNumberInfo.isValidShortNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):boolean, dex:  in method: com.android.i18n.phonenumbers.ShortNumberInfo.isValidShortNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.isValidShortNumber(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.isValidShortNumberForRegion(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber, java.lang.String):boolean, dex: 
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
    public boolean isValidShortNumberForRegion(com.android.i18n.phonenumbers.Phonenumber.PhoneNumber r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.isValidShortNumberForRegion(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber, java.lang.String):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.isValidShortNumberForRegion(com.android.i18n.phonenumbers.Phonenumber$PhoneNumber, java.lang.String):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.isValidShortNumberForRegion(java.lang.String, java.lang.String):boolean, dex: 
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
    @java.lang.Deprecated
    public boolean isValidShortNumberForRegion(java.lang.String r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.ShortNumberInfo.isValidShortNumberForRegion(java.lang.String, java.lang.String):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.ShortNumberInfo.isValidShortNumberForRegion(java.lang.String, java.lang.String):boolean");
    }

    public static ShortNumberInfo getInstance() {
        return INSTANCE;
    }

    Set<String> getSupportedRegions() {
        return Collections.unmodifiableSet(MetadataManager.getShortNumberMetadataSupportedRegions());
    }

    public boolean connectsToEmergencyNumber(String number, String regionCode) {
        return matchesEmergencyNumberHelper(number, regionCode, true);
    }

    public boolean isEmergencyNumber(String number, String regionCode) {
        return matchesEmergencyNumberHelper(number, regionCode, false);
    }
}
