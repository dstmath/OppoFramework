package android.icu.text;

import android.icu.text.PluralRules.FixedDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
@Deprecated
public class PluralSamples {
    private static final int LIMIT_FRACTION_SAMPLES = 3;
    private static final int[] TENS = null;
    private final Set<FixedDecimal> _fractionSamples;
    private final Map<String, Set<FixedDecimal>> _keyFractionSamplesMap;
    @Deprecated
    public final Map<String, Boolean> _keyLimitedMap;
    private final Map<String, List<Double>> _keySamplesMap;
    private PluralRules pluralRules;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.PluralSamples.<clinit>():void, dex: 
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
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.PluralSamples.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralSamples.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.text.PluralSamples.<init>(android.icu.text.PluralRules):void, dex: 
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
    @java.lang.Deprecated
    public PluralSamples(android.icu.text.PluralRules r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.text.PluralSamples.<init>(android.icu.text.PluralRules):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralSamples.<init>(android.icu.text.PluralRules):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.text.PluralSamples.addIfNotPresent(double, java.util.Set, java.util.Map):boolean, dex: 
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
    private boolean addIfNotPresent(double r1, java.util.Set<android.icu.text.PluralRules.FixedDecimal> r3, java.util.Map<java.lang.String, java.util.Set<android.icu.text.PluralRules.FixedDecimal>> r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.text.PluralSamples.addIfNotPresent(double, java.util.Set, java.util.Map):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralSamples.addIfNotPresent(double, java.util.Set, java.util.Map):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.PluralSamples.addRelation(java.util.Map, java.lang.String, android.icu.text.PluralRules$FixedDecimal):void, dex: 
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
    private void addRelation(java.util.Map<java.lang.String, java.util.Set<android.icu.text.PluralRules.FixedDecimal>> r1, java.lang.String r2, android.icu.text.PluralRules.FixedDecimal r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.PluralSamples.addRelation(java.util.Map, java.lang.String, android.icu.text.PluralRules$FixedDecimal):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralSamples.addRelation(java.util.Map, java.lang.String, android.icu.text.PluralRules$FixedDecimal):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.text.PluralSamples.addSimpleSamples(android.icu.text.PluralRules, int, java.util.Map, int, double):int, dex: 
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
    private int addSimpleSamples(android.icu.text.PluralRules r1, int r2, java.util.Map<java.lang.String, java.util.List<java.lang.Double>> r3, int r4, double r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.text.PluralSamples.addSimpleSamples(android.icu.text.PluralRules, int, java.util.Map, int, double):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralSamples.addSimpleSamples(android.icu.text.PluralRules, int, java.util.Map, int, double):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.icu.text.PluralSamples.fractions(java.util.Set):java.util.Set<android.icu.text.PluralRules$FixedDecimal>, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private java.util.Set<android.icu.text.PluralRules.FixedDecimal> fractions(java.util.Set<android.icu.text.PluralRules.FixedDecimal> r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.icu.text.PluralSamples.fractions(java.util.Set):java.util.Set<android.icu.text.PluralRules$FixedDecimal>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralSamples.fractions(java.util.Set):java.util.Set<android.icu.text.PluralRules$FixedDecimal>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.text.PluralSamples.getDifferentCategory(java.util.List, java.lang.String):java.lang.Integer, dex: 
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
    private java.lang.Integer getDifferentCategory(java.util.List<java.lang.Integer> r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.text.PluralSamples.getDifferentCategory(java.util.List, java.lang.String):java.lang.Integer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralSamples.getDifferentCategory(java.util.List, java.lang.String):java.lang.Integer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.text.PluralSamples.getAllKeywordValues(java.lang.String):java.util.Collection<java.lang.Double>, dex: 
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
    java.util.Collection<java.lang.Double> getAllKeywordValues(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.text.PluralSamples.getAllKeywordValues(java.lang.String):java.util.Collection<java.lang.Double>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralSamples.getAllKeywordValues(java.lang.String):java.util.Collection<java.lang.Double>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.text.PluralSamples.getFractionSamples():java.util.Set<android.icu.text.PluralRules$FixedDecimal>, dex: 
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
    java.util.Set<android.icu.text.PluralRules.FixedDecimal> getFractionSamples() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.text.PluralSamples.getFractionSamples():java.util.Set<android.icu.text.PluralRules$FixedDecimal>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralSamples.getFractionSamples():java.util.Set<android.icu.text.PluralRules$FixedDecimal>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.text.PluralSamples.getKeyFractionSamplesMap():java.util.Map<java.lang.String, java.util.Set<android.icu.text.PluralRules$FixedDecimal>>, dex: 
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
    java.util.Map<java.lang.String, java.util.Set<android.icu.text.PluralRules.FixedDecimal>> getKeyFractionSamplesMap() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.text.PluralSamples.getKeyFractionSamplesMap():java.util.Map<java.lang.String, java.util.Set<android.icu.text.PluralRules$FixedDecimal>>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralSamples.getKeyFractionSamplesMap():java.util.Map<java.lang.String, java.util.Set<android.icu.text.PluralRules$FixedDecimal>>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.icu.text.PluralSamples.getKeySamplesMap():java.util.Map<java.lang.String, java.util.List<java.lang.Double>>, dex:  in method: android.icu.text.PluralSamples.getKeySamplesMap():java.util.Map<java.lang.String, java.util.List<java.lang.Double>>, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.icu.text.PluralSamples.getKeySamplesMap():java.util.Map<java.lang.String, java.util.List<java.lang.Double>>, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    java.util.Map<java.lang.String, java.util.List<java.lang.Double>> getKeySamplesMap() {
        /*
        // Can't load method instructions: Load method exception: null in method: android.icu.text.PluralSamples.getKeySamplesMap():java.util.Map<java.lang.String, java.util.List<java.lang.Double>>, dex:  in method: android.icu.text.PluralSamples.getKeySamplesMap():java.util.Map<java.lang.String, java.util.List<java.lang.Double>>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralSamples.getKeySamplesMap():java.util.Map<java.lang.String, java.util.List<java.lang.Double>>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.text.PluralSamples.getStatus(java.lang.String, int, java.util.Set, android.icu.util.Output):android.icu.text.PluralRules$KeywordStatus, dex: 
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
    @java.lang.Deprecated
    public android.icu.text.PluralRules.KeywordStatus getStatus(java.lang.String r1, int r2, java.util.Set<java.lang.Double> r3, android.icu.util.Output<java.lang.Double> r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.text.PluralSamples.getStatus(java.lang.String, int, java.util.Set, android.icu.util.Output):android.icu.text.PluralRules$KeywordStatus, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralSamples.getStatus(java.lang.String, int, java.util.Set, android.icu.util.Output):android.icu.text.PluralRules$KeywordStatus");
    }
}
