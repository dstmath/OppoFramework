package javax.xml.datatype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;

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
public abstract class DatatypeFactory {
    public static final String DATATYPEFACTORY_IMPLEMENTATION_CLASS = null;
    public static final String DATATYPEFACTORY_PROPERTY = "javax.xml.datatype.DatatypeFactory";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: javax.xml.datatype.DatatypeFactory.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: javax.xml.datatype.DatatypeFactory.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: javax.xml.datatype.DatatypeFactory.<init>():void, dex: 
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
    protected DatatypeFactory() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: javax.xml.datatype.DatatypeFactory.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: javax.xml.datatype.DatatypeFactory.newInstance():javax.xml.datatype.DatatypeFactory, dex: 
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
    public static javax.xml.datatype.DatatypeFactory newInstance() throws javax.xml.datatype.DatatypeConfigurationException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: javax.xml.datatype.DatatypeFactory.newInstance():javax.xml.datatype.DatatypeFactory, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.newInstance():javax.xml.datatype.DatatypeFactory");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: javax.xml.datatype.DatatypeFactory.newInstance(java.lang.String, java.lang.ClassLoader):javax.xml.datatype.DatatypeFactory, dex: 
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
    public static javax.xml.datatype.DatatypeFactory newInstance(java.lang.String r1, java.lang.ClassLoader r2) throws javax.xml.datatype.DatatypeConfigurationException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: javax.xml.datatype.DatatypeFactory.newInstance(java.lang.String, java.lang.ClassLoader):javax.xml.datatype.DatatypeFactory, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.newInstance(java.lang.String, java.lang.ClassLoader):javax.xml.datatype.DatatypeFactory");
    }

    public abstract Duration newDuration(long j);

    public abstract Duration newDuration(String str);

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: javax.xml.datatype.DatatypeFactory.newDuration(boolean, int, int, int, int, int, int):javax.xml.datatype.Duration, dex: 
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
    public javax.xml.datatype.Duration newDuration(boolean r1, int r2, int r3, int r4, int r5, int r6, int r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: javax.xml.datatype.DatatypeFactory.newDuration(boolean, int, int, int, int, int, int):javax.xml.datatype.Duration, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.newDuration(boolean, int, int, int, int, int, int):javax.xml.datatype.Duration");
    }

    public abstract Duration newDuration(boolean z, BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4, BigInteger bigInteger5, BigDecimal bigDecimal);

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: javax.xml.datatype.DatatypeFactory.newDurationDayTime(long):javax.xml.datatype.Duration, dex: 
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
    public javax.xml.datatype.Duration newDurationDayTime(long r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: javax.xml.datatype.DatatypeFactory.newDurationDayTime(long):javax.xml.datatype.Duration, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.newDurationDayTime(long):javax.xml.datatype.Duration");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: javax.xml.datatype.DatatypeFactory.newDurationDayTime(java.lang.String):javax.xml.datatype.Duration, dex: 
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
    public javax.xml.datatype.Duration newDurationDayTime(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: javax.xml.datatype.DatatypeFactory.newDurationDayTime(java.lang.String):javax.xml.datatype.Duration, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.newDurationDayTime(java.lang.String):javax.xml.datatype.Duration");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: javax.xml.datatype.DatatypeFactory.newDurationDayTime(boolean, int, int, int, int):javax.xml.datatype.Duration, dex: 
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
    public javax.xml.datatype.Duration newDurationDayTime(boolean r1, int r2, int r3, int r4, int r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: javax.xml.datatype.DatatypeFactory.newDurationDayTime(boolean, int, int, int, int):javax.xml.datatype.Duration, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.newDurationDayTime(boolean, int, int, int, int):javax.xml.datatype.Duration");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: javax.xml.datatype.DatatypeFactory.newDurationDayTime(boolean, java.math.BigInteger, java.math.BigInteger, java.math.BigInteger, java.math.BigInteger):javax.xml.datatype.Duration, dex: 
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
    public javax.xml.datatype.Duration newDurationDayTime(boolean r1, java.math.BigInteger r2, java.math.BigInteger r3, java.math.BigInteger r4, java.math.BigInteger r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: javax.xml.datatype.DatatypeFactory.newDurationDayTime(boolean, java.math.BigInteger, java.math.BigInteger, java.math.BigInteger, java.math.BigInteger):javax.xml.datatype.Duration, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.newDurationDayTime(boolean, java.math.BigInteger, java.math.BigInteger, java.math.BigInteger, java.math.BigInteger):javax.xml.datatype.Duration");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: javax.xml.datatype.DatatypeFactory.newDurationYearMonth(long):javax.xml.datatype.Duration, dex: 
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
    public javax.xml.datatype.Duration newDurationYearMonth(long r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: javax.xml.datatype.DatatypeFactory.newDurationYearMonth(long):javax.xml.datatype.Duration, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.newDurationYearMonth(long):javax.xml.datatype.Duration");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: javax.xml.datatype.DatatypeFactory.newDurationYearMonth(java.lang.String):javax.xml.datatype.Duration, dex: 
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
    public javax.xml.datatype.Duration newDurationYearMonth(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: javax.xml.datatype.DatatypeFactory.newDurationYearMonth(java.lang.String):javax.xml.datatype.Duration, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.newDurationYearMonth(java.lang.String):javax.xml.datatype.Duration");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: javax.xml.datatype.DatatypeFactory.newDurationYearMonth(boolean, int, int):javax.xml.datatype.Duration, dex: 
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
    public javax.xml.datatype.Duration newDurationYearMonth(boolean r1, int r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: javax.xml.datatype.DatatypeFactory.newDurationYearMonth(boolean, int, int):javax.xml.datatype.Duration, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.newDurationYearMonth(boolean, int, int):javax.xml.datatype.Duration");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: javax.xml.datatype.DatatypeFactory.newDurationYearMonth(boolean, java.math.BigInteger, java.math.BigInteger):javax.xml.datatype.Duration, dex: 
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
    public javax.xml.datatype.Duration newDurationYearMonth(boolean r1, java.math.BigInteger r2, java.math.BigInteger r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: javax.xml.datatype.DatatypeFactory.newDurationYearMonth(boolean, java.math.BigInteger, java.math.BigInteger):javax.xml.datatype.Duration, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.newDurationYearMonth(boolean, java.math.BigInteger, java.math.BigInteger):javax.xml.datatype.Duration");
    }

    public abstract XMLGregorianCalendar newXMLGregorianCalendar();

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendar(int, int, int, int, int, int, int, int):javax.xml.datatype.XMLGregorianCalendar, dex: 
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
    public javax.xml.datatype.XMLGregorianCalendar newXMLGregorianCalendar(int r1, int r2, int r3, int r4, int r5, int r6, int r7, int r8) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendar(int, int, int, int, int, int, int, int):javax.xml.datatype.XMLGregorianCalendar, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendar(int, int, int, int, int, int, int, int):javax.xml.datatype.XMLGregorianCalendar");
    }

    public abstract XMLGregorianCalendar newXMLGregorianCalendar(String str);

    public abstract XMLGregorianCalendar newXMLGregorianCalendar(BigInteger bigInteger, int i, int i2, int i3, int i4, int i5, BigDecimal bigDecimal, int i6);

    public abstract XMLGregorianCalendar newXMLGregorianCalendar(GregorianCalendar gregorianCalendar);

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarDate(int, int, int, int):javax.xml.datatype.XMLGregorianCalendar, dex:  in method: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarDate(int, int, int, int):javax.xml.datatype.XMLGregorianCalendar, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarDate(int, int, int, int):javax.xml.datatype.XMLGregorianCalendar, dex: 
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
    public javax.xml.datatype.XMLGregorianCalendar newXMLGregorianCalendarDate(int r1, int r2, int r3, int r4) {
        /*
        // Can't load method instructions: Load method exception: null in method: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarDate(int, int, int, int):javax.xml.datatype.XMLGregorianCalendar, dex:  in method: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarDate(int, int, int, int):javax.xml.datatype.XMLGregorianCalendar, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarDate(int, int, int, int):javax.xml.datatype.XMLGregorianCalendar");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarTime(int, int, int, int):javax.xml.datatype.XMLGregorianCalendar, dex:  in method: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarTime(int, int, int, int):javax.xml.datatype.XMLGregorianCalendar, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarTime(int, int, int, int):javax.xml.datatype.XMLGregorianCalendar, dex: 
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
    public javax.xml.datatype.XMLGregorianCalendar newXMLGregorianCalendarTime(int r1, int r2, int r3, int r4) {
        /*
        // Can't load method instructions: Load method exception: null in method: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarTime(int, int, int, int):javax.xml.datatype.XMLGregorianCalendar, dex:  in method: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarTime(int, int, int, int):javax.xml.datatype.XMLGregorianCalendar, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarTime(int, int, int, int):javax.xml.datatype.XMLGregorianCalendar");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarTime(int, int, int, int, int):javax.xml.datatype.XMLGregorianCalendar, dex: 
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
    public javax.xml.datatype.XMLGregorianCalendar newXMLGregorianCalendarTime(int r1, int r2, int r3, int r4, int r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarTime(int, int, int, int, int):javax.xml.datatype.XMLGregorianCalendar, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarTime(int, int, int, int, int):javax.xml.datatype.XMLGregorianCalendar");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarTime(int, int, int, java.math.BigDecimal, int):javax.xml.datatype.XMLGregorianCalendar, dex: 
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
    public javax.xml.datatype.XMLGregorianCalendar newXMLGregorianCalendarTime(int r1, int r2, int r3, java.math.BigDecimal r4, int r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarTime(int, int, int, java.math.BigDecimal, int):javax.xml.datatype.XMLGregorianCalendar, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendarTime(int, int, int, java.math.BigDecimal, int):javax.xml.datatype.XMLGregorianCalendar");
    }
}
