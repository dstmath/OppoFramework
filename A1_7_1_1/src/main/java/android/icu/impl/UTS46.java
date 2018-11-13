package android.icu.impl;

import android.icu.lang.UCharacter;
import android.icu.text.IDNA;
import android.icu.text.IDNA.Error;
import android.icu.text.IDNA.Info;
import android.icu.text.Normalizer2;
import java.util.EnumSet;

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
public final class UTS46 extends IDNA {
    private static final int EN_AN_MASK = 0;
    private static final int ES_CS_ET_ON_BN_NSM_MASK = 0;
    private static final int L_EN_ES_CS_ET_ON_BN_NSM_MASK = 0;
    private static final int L_EN_MASK = 0;
    private static final int L_MASK = 0;
    private static final int L_R_AL_MASK = 0;
    private static final int R_AL_AN_EN_ES_CS_ET_ON_BN_NSM_MASK = 0;
    private static final int R_AL_AN_MASK = 0;
    private static final int R_AL_EN_AN_MASK = 0;
    private static final int R_AL_MASK = 0;
    private static int U_GC_M_MASK;
    private static final byte[] asciiData = null;
    private static final EnumSet<Error> severeErrors = null;
    private static final Normalizer2 uts46Norm2 = null;
    final int options;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.UTS46.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.UTS46.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UTS46.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.icu.impl.UTS46.<init>(int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public UTS46(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.icu.impl.UTS46.<init>(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UTS46.<init>(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UTS46.checkLabelBiDi(java.lang.CharSequence, int, int, android.icu.text.IDNA$Info):void, dex: 
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
    private void checkLabelBiDi(java.lang.CharSequence r1, int r2, int r3, android.icu.text.IDNA.Info r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UTS46.checkLabelBiDi(java.lang.CharSequence, int, int, android.icu.text.IDNA$Info):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UTS46.checkLabelBiDi(java.lang.CharSequence, int, int, android.icu.text.IDNA$Info):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.UTS46.checkLabelContextO(java.lang.CharSequence, int, int, android.icu.text.IDNA$Info):void, dex: 
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
    private void checkLabelContextO(java.lang.CharSequence r1, int r2, int r3, android.icu.text.IDNA.Info r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.UTS46.checkLabelContextO(java.lang.CharSequence, int, int, android.icu.text.IDNA$Info):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UTS46.checkLabelContextO(java.lang.CharSequence, int, int, android.icu.text.IDNA$Info):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UTS46.isLabelOkContextJ(java.lang.CharSequence, int, int):boolean, dex: 
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
    private boolean isLabelOkContextJ(java.lang.CharSequence r1, int r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UTS46.isLabelOkContextJ(java.lang.CharSequence, int, int):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UTS46.isLabelOkContextJ(java.lang.CharSequence, int, int):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UTS46.mapDevChars(java.lang.StringBuilder, int, int):int, dex: 
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
    private int mapDevChars(java.lang.StringBuilder r1, int r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UTS46.mapDevChars(java.lang.StringBuilder, int, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UTS46.mapDevChars(java.lang.StringBuilder, int, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.UTS46.markBadACELabel(java.lang.StringBuilder, int, int, boolean, android.icu.text.IDNA$Info):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private int markBadACELabel(java.lang.StringBuilder r1, int r2, int r3, boolean r4, android.icu.text.IDNA.Info r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.UTS46.markBadACELabel(java.lang.StringBuilder, int, int, boolean, android.icu.text.IDNA$Info):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UTS46.markBadACELabel(java.lang.StringBuilder, int, int, boolean, android.icu.text.IDNA$Info):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UTS46.process(java.lang.CharSequence, boolean, boolean, java.lang.StringBuilder, android.icu.text.IDNA$Info):java.lang.StringBuilder, dex: 
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
    private java.lang.StringBuilder process(java.lang.CharSequence r1, boolean r2, boolean r3, java.lang.StringBuilder r4, android.icu.text.IDNA.Info r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UTS46.process(java.lang.CharSequence, boolean, boolean, java.lang.StringBuilder, android.icu.text.IDNA$Info):java.lang.StringBuilder, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UTS46.process(java.lang.CharSequence, boolean, boolean, java.lang.StringBuilder, android.icu.text.IDNA$Info):java.lang.StringBuilder");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.icu.impl.UTS46.processLabel(java.lang.StringBuilder, int, int, boolean, android.icu.text.IDNA$Info):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private int processLabel(java.lang.StringBuilder r1, int r2, int r3, boolean r4, android.icu.text.IDNA.Info r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.icu.impl.UTS46.processLabel(java.lang.StringBuilder, int, int, boolean, android.icu.text.IDNA$Info):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UTS46.processLabel(java.lang.StringBuilder, int, int, boolean, android.icu.text.IDNA$Info):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UTS46.processUnicode(java.lang.CharSequence, int, int, boolean, boolean, java.lang.StringBuilder, android.icu.text.IDNA$Info):java.lang.StringBuilder, dex: 
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
    private java.lang.StringBuilder processUnicode(java.lang.CharSequence r1, int r2, int r3, boolean r4, boolean r5, java.lang.StringBuilder r6, android.icu.text.IDNA.Info r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UTS46.processUnicode(java.lang.CharSequence, int, int, boolean, boolean, java.lang.StringBuilder, android.icu.text.IDNA$Info):java.lang.StringBuilder, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UTS46.processUnicode(java.lang.CharSequence, int, int, boolean, boolean, java.lang.StringBuilder, android.icu.text.IDNA$Info):java.lang.StringBuilder");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UTS46.replaceLabel(java.lang.StringBuilder, int, int, java.lang.CharSequence, int):int, dex: 
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
    private static int replaceLabel(java.lang.StringBuilder r1, int r2, int r3, java.lang.CharSequence r4, int r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UTS46.replaceLabel(java.lang.StringBuilder, int, int, java.lang.CharSequence, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UTS46.replaceLabel(java.lang.StringBuilder, int, int, java.lang.CharSequence, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UTS46.nameToASCII(java.lang.CharSequence, java.lang.StringBuilder, android.icu.text.IDNA$Info):java.lang.StringBuilder, dex: 
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
    public java.lang.StringBuilder nameToASCII(java.lang.CharSequence r1, java.lang.StringBuilder r2, android.icu.text.IDNA.Info r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UTS46.nameToASCII(java.lang.CharSequence, java.lang.StringBuilder, android.icu.text.IDNA$Info):java.lang.StringBuilder, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UTS46.nameToASCII(java.lang.CharSequence, java.lang.StringBuilder, android.icu.text.IDNA$Info):java.lang.StringBuilder");
    }

    public StringBuilder labelToASCII(CharSequence label, StringBuilder dest, Info info) {
        return process(label, true, true, dest, info);
    }

    public StringBuilder labelToUnicode(CharSequence label, StringBuilder dest, Info info) {
        return process(label, true, false, dest, info);
    }

    public StringBuilder nameToUnicode(CharSequence name, StringBuilder dest, Info info) {
        return process(name, false, false, dest, info);
    }

    private static boolean isASCIIString(CharSequence dest) {
        int length = dest.length();
        for (int i = 0; i < length; i++) {
            if (dest.charAt(i) > 127) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNonASCIIDisallowedSTD3Valid(int c) {
        return c == 8800 || c == 8814 || c == 8815;
    }

    private static boolean isASCIIOkBiDi(CharSequence s, int length) {
        int labelStart = 0;
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c == '.') {
                if (i > labelStart) {
                    c = s.charAt(i - 1);
                    if (('a' > c || c > 'z') && ('0' > c || c > '9')) {
                        return false;
                    }
                }
                labelStart = i + 1;
            } else if (i == labelStart) {
                if ('a' > c || c > 'z') {
                    return false;
                }
            } else if (c <= ' ' && (c >= 28 || (9 <= c && c <= 13))) {
                return false;
            }
        }
        return true;
    }

    private static int U_MASK(int x) {
        return 1 << x;
    }

    private static int U_GET_GC_MASK(int c) {
        return 1 << UCharacter.getType(c);
    }
}
