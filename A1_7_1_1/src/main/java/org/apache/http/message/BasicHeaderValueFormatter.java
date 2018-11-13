package org.apache.http.message;

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
public class BasicHeaderValueFormatter implements HeaderValueFormatter {
    public static final BasicHeaderValueFormatter DEFAULT = null;
    public static final String SEPARATORS = " ;,:@()<>\\\"/[]?={}\t";
    public static final String UNSAFE_CHARS = "\"\\";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.http.message.BasicHeaderValueFormatter.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.http.message.BasicHeaderValueFormatter.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicHeaderValueFormatter.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.http.message.BasicHeaderValueFormatter.<init>():void, dex: 
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
    public BasicHeaderValueFormatter() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.http.message.BasicHeaderValueFormatter.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicHeaderValueFormatter.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.formatElements(org.apache.http.HeaderElement[], boolean, org.apache.http.message.HeaderValueFormatter):java.lang.String, dex: 
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
    public static final java.lang.String formatElements(org.apache.http.HeaderElement[] r1, boolean r2, org.apache.http.message.HeaderValueFormatter r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.formatElements(org.apache.http.HeaderElement[], boolean, org.apache.http.message.HeaderValueFormatter):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicHeaderValueFormatter.formatElements(org.apache.http.HeaderElement[], boolean, org.apache.http.message.HeaderValueFormatter):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.formatHeaderElement(org.apache.http.HeaderElement, boolean, org.apache.http.message.HeaderValueFormatter):java.lang.String, dex: 
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
    public static final java.lang.String formatHeaderElement(org.apache.http.HeaderElement r1, boolean r2, org.apache.http.message.HeaderValueFormatter r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.formatHeaderElement(org.apache.http.HeaderElement, boolean, org.apache.http.message.HeaderValueFormatter):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicHeaderValueFormatter.formatHeaderElement(org.apache.http.HeaderElement, boolean, org.apache.http.message.HeaderValueFormatter):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.formatNameValuePair(org.apache.http.NameValuePair, boolean, org.apache.http.message.HeaderValueFormatter):java.lang.String, dex: 
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
    public static final java.lang.String formatNameValuePair(org.apache.http.NameValuePair r1, boolean r2, org.apache.http.message.HeaderValueFormatter r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.formatNameValuePair(org.apache.http.NameValuePair, boolean, org.apache.http.message.HeaderValueFormatter):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicHeaderValueFormatter.formatNameValuePair(org.apache.http.NameValuePair, boolean, org.apache.http.message.HeaderValueFormatter):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.formatParameters(org.apache.http.NameValuePair[], boolean, org.apache.http.message.HeaderValueFormatter):java.lang.String, dex: 
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
    public static final java.lang.String formatParameters(org.apache.http.NameValuePair[] r1, boolean r2, org.apache.http.message.HeaderValueFormatter r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.formatParameters(org.apache.http.NameValuePair[], boolean, org.apache.http.message.HeaderValueFormatter):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicHeaderValueFormatter.formatParameters(org.apache.http.NameValuePair[], boolean, org.apache.http.message.HeaderValueFormatter):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.doFormatValue(org.apache.http.util.CharArrayBuffer, java.lang.String, boolean):void, dex: 
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
    protected void doFormatValue(org.apache.http.util.CharArrayBuffer r1, java.lang.String r2, boolean r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.doFormatValue(org.apache.http.util.CharArrayBuffer, java.lang.String, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicHeaderValueFormatter.doFormatValue(org.apache.http.util.CharArrayBuffer, java.lang.String, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.estimateElementsLen(org.apache.http.HeaderElement[]):int, dex: 
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
    protected int estimateElementsLen(org.apache.http.HeaderElement[] r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.estimateElementsLen(org.apache.http.HeaderElement[]):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicHeaderValueFormatter.estimateElementsLen(org.apache.http.HeaderElement[]):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.estimateHeaderElementLen(org.apache.http.HeaderElement):int, dex: 
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
    protected int estimateHeaderElementLen(org.apache.http.HeaderElement r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.estimateHeaderElementLen(org.apache.http.HeaderElement):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicHeaderValueFormatter.estimateHeaderElementLen(org.apache.http.HeaderElement):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.estimateNameValuePairLen(org.apache.http.NameValuePair):int, dex: 
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
    protected int estimateNameValuePairLen(org.apache.http.NameValuePair r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.estimateNameValuePairLen(org.apache.http.NameValuePair):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicHeaderValueFormatter.estimateNameValuePairLen(org.apache.http.NameValuePair):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.estimateParametersLen(org.apache.http.NameValuePair[]):int, dex: 
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
    protected int estimateParametersLen(org.apache.http.NameValuePair[] r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.estimateParametersLen(org.apache.http.NameValuePair[]):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicHeaderValueFormatter.estimateParametersLen(org.apache.http.NameValuePair[]):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.apache.http.message.BasicHeaderValueFormatter.formatElements(org.apache.http.util.CharArrayBuffer, org.apache.http.HeaderElement[], boolean):org.apache.http.util.CharArrayBuffer, dex:  in method: org.apache.http.message.BasicHeaderValueFormatter.formatElements(org.apache.http.util.CharArrayBuffer, org.apache.http.HeaderElement[], boolean):org.apache.http.util.CharArrayBuffer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.apache.http.message.BasicHeaderValueFormatter.formatElements(org.apache.http.util.CharArrayBuffer, org.apache.http.HeaderElement[], boolean):org.apache.http.util.CharArrayBuffer, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:756)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public org.apache.http.util.CharArrayBuffer formatElements(org.apache.http.util.CharArrayBuffer r1, org.apache.http.HeaderElement[] r2, boolean r3) {
        /*
        // Can't load method instructions: Load method exception: null in method: org.apache.http.message.BasicHeaderValueFormatter.formatElements(org.apache.http.util.CharArrayBuffer, org.apache.http.HeaderElement[], boolean):org.apache.http.util.CharArrayBuffer, dex:  in method: org.apache.http.message.BasicHeaderValueFormatter.formatElements(org.apache.http.util.CharArrayBuffer, org.apache.http.HeaderElement[], boolean):org.apache.http.util.CharArrayBuffer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicHeaderValueFormatter.formatElements(org.apache.http.util.CharArrayBuffer, org.apache.http.HeaderElement[], boolean):org.apache.http.util.CharArrayBuffer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.formatHeaderElement(org.apache.http.util.CharArrayBuffer, org.apache.http.HeaderElement, boolean):org.apache.http.util.CharArrayBuffer, dex: 
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
    public org.apache.http.util.CharArrayBuffer formatHeaderElement(org.apache.http.util.CharArrayBuffer r1, org.apache.http.HeaderElement r2, boolean r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.formatHeaderElement(org.apache.http.util.CharArrayBuffer, org.apache.http.HeaderElement, boolean):org.apache.http.util.CharArrayBuffer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicHeaderValueFormatter.formatHeaderElement(org.apache.http.util.CharArrayBuffer, org.apache.http.HeaderElement, boolean):org.apache.http.util.CharArrayBuffer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.formatNameValuePair(org.apache.http.util.CharArrayBuffer, org.apache.http.NameValuePair, boolean):org.apache.http.util.CharArrayBuffer, dex: 
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
    public org.apache.http.util.CharArrayBuffer formatNameValuePair(org.apache.http.util.CharArrayBuffer r1, org.apache.http.NameValuePair r2, boolean r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.formatNameValuePair(org.apache.http.util.CharArrayBuffer, org.apache.http.NameValuePair, boolean):org.apache.http.util.CharArrayBuffer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicHeaderValueFormatter.formatNameValuePair(org.apache.http.util.CharArrayBuffer, org.apache.http.NameValuePair, boolean):org.apache.http.util.CharArrayBuffer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.apache.http.message.BasicHeaderValueFormatter.formatParameters(org.apache.http.util.CharArrayBuffer, org.apache.http.NameValuePair[], boolean):org.apache.http.util.CharArrayBuffer, dex:  in method: org.apache.http.message.BasicHeaderValueFormatter.formatParameters(org.apache.http.util.CharArrayBuffer, org.apache.http.NameValuePair[], boolean):org.apache.http.util.CharArrayBuffer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.apache.http.message.BasicHeaderValueFormatter.formatParameters(org.apache.http.util.CharArrayBuffer, org.apache.http.NameValuePair[], boolean):org.apache.http.util.CharArrayBuffer, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:756)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public org.apache.http.util.CharArrayBuffer formatParameters(org.apache.http.util.CharArrayBuffer r1, org.apache.http.NameValuePair[] r2, boolean r3) {
        /*
        // Can't load method instructions: Load method exception: null in method: org.apache.http.message.BasicHeaderValueFormatter.formatParameters(org.apache.http.util.CharArrayBuffer, org.apache.http.NameValuePair[], boolean):org.apache.http.util.CharArrayBuffer, dex:  in method: org.apache.http.message.BasicHeaderValueFormatter.formatParameters(org.apache.http.util.CharArrayBuffer, org.apache.http.NameValuePair[], boolean):org.apache.http.util.CharArrayBuffer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicHeaderValueFormatter.formatParameters(org.apache.http.util.CharArrayBuffer, org.apache.http.NameValuePair[], boolean):org.apache.http.util.CharArrayBuffer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.isSeparator(char):boolean, dex: 
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
    protected boolean isSeparator(char r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.isSeparator(char):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicHeaderValueFormatter.isSeparator(char):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.isUnsafe(char):boolean, dex: 
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
    protected boolean isUnsafe(char r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.apache.http.message.BasicHeaderValueFormatter.isUnsafe(char):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.message.BasicHeaderValueFormatter.isUnsafe(char):boolean");
    }
}
