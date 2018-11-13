package dalvik.system.profiler;

import java.io.DataOutputStream;
import java.util.Map;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    */
public final class BinaryHprofWriter {
    /* renamed from: -dalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues */
    private static final /* synthetic */ int[] f124-dalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues = null;
    private final Map<String, Integer> classNameToId;
    private final HprofData data;
    private int nextClassId;
    private int nextStackFrameId;
    private int nextStringId;
    private final DataOutputStream out;
    private final Map<StackTraceElement, Integer> stackFrameToId;
    private final Map<String, Integer> stringToId;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.BinaryHprofWriter.-getdalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues():int[], dex: 
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
    /* renamed from: -getdalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues */
    private static /* synthetic */ int[] m110xb57bcfd4() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.BinaryHprofWriter.-getdalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues():int[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprofWriter.-getdalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues():int[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: dalvik.system.profiler.BinaryHprofWriter.<init>(dalvik.system.profiler.HprofData, java.io.OutputStream):void, dex: 
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
    private BinaryHprofWriter(dalvik.system.profiler.HprofData r1, java.io.OutputStream r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: dalvik.system.profiler.BinaryHprofWriter.<init>(dalvik.system.profiler.HprofData, java.io.OutputStream):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprofWriter.<init>(dalvik.system.profiler.HprofData, java.io.OutputStream):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: dalvik.system.profiler.BinaryHprofWriter.write():void, dex:  in method: dalvik.system.profiler.BinaryHprofWriter.write():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: dalvik.system.profiler.BinaryHprofWriter.write():void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void write() throws java.io.IOException {
        /*
        // Can't load method instructions: Load method exception: null in method: dalvik.system.profiler.BinaryHprofWriter.write():void, dex:  in method: dalvik.system.profiler.BinaryHprofWriter.write():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprofWriter.write():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: dalvik.system.profiler.BinaryHprofWriter.write(dalvik.system.profiler.HprofData, java.io.OutputStream):void, dex: 
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
    public static void write(dalvik.system.profiler.HprofData r1, java.io.OutputStream r2) throws java.io.IOException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: dalvik.system.profiler.BinaryHprofWriter.write(dalvik.system.profiler.HprofData, java.io.OutputStream):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprofWriter.write(dalvik.system.profiler.HprofData, java.io.OutputStream):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.BinaryHprofWriter.writeControlSettings(int, int):void, dex: 
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
    private void writeControlSettings(int r1, int r2) throws java.io.IOException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.BinaryHprofWriter.writeControlSettings(int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprofWriter.writeControlSettings(int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: dalvik.system.profiler.BinaryHprofWriter.writeCpuSamples(int, java.util.Set):void, dex:  in method: dalvik.system.profiler.BinaryHprofWriter.writeCpuSamples(int, java.util.Set):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: dalvik.system.profiler.BinaryHprofWriter.writeCpuSamples(int, java.util.Set):void, dex: 
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
    private void writeCpuSamples(int r1, java.util.Set<dalvik.system.profiler.HprofData.Sample> r2) throws java.io.IOException {
        /*
        // Can't load method instructions: Load method exception: null in method: dalvik.system.profiler.BinaryHprofWriter.writeCpuSamples(int, java.util.Set):void, dex:  in method: dalvik.system.profiler.BinaryHprofWriter.writeCpuSamples(int, java.util.Set):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprofWriter.writeCpuSamples(int, java.util.Set):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.BinaryHprofWriter.writeHeader(long):void, dex: 
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
    private void writeHeader(long r1) throws java.io.IOException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.BinaryHprofWriter.writeHeader(long):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprofWriter.writeHeader(long):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: dalvik.system.profiler.BinaryHprofWriter.writeId(int):void, dex:  in method: dalvik.system.profiler.BinaryHprofWriter.writeId(int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: dalvik.system.profiler.BinaryHprofWriter.writeId(int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:72)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void writeId(int r1) throws java.io.IOException {
        /*
        // Can't load method instructions: Load method exception: null in method: dalvik.system.profiler.BinaryHprofWriter.writeId(int):void, dex:  in method: dalvik.system.profiler.BinaryHprofWriter.writeId(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprofWriter.writeId(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.BinaryHprofWriter.writeLoadClass(java.lang.String):int, dex: 
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
    private int writeLoadClass(java.lang.String r1) throws java.io.IOException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.BinaryHprofWriter.writeLoadClass(java.lang.String):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprofWriter.writeLoadClass(java.lang.String):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: dalvik.system.profiler.BinaryHprofWriter.writeRecordHeader(dalvik.system.profiler.BinaryHprof$Tag, int, int):void, dex:  in method: dalvik.system.profiler.BinaryHprofWriter.writeRecordHeader(dalvik.system.profiler.BinaryHprof$Tag, int, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: dalvik.system.profiler.BinaryHprofWriter.writeRecordHeader(dalvik.system.profiler.BinaryHprof$Tag, int, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:72)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void writeRecordHeader(dalvik.system.profiler.BinaryHprof.Tag r1, int r2, int r3) throws java.io.IOException {
        /*
        // Can't load method instructions: Load method exception: null in method: dalvik.system.profiler.BinaryHprofWriter.writeRecordHeader(dalvik.system.profiler.BinaryHprof$Tag, int, int):void, dex:  in method: dalvik.system.profiler.BinaryHprofWriter.writeRecordHeader(dalvik.system.profiler.BinaryHprof$Tag, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprofWriter.writeRecordHeader(dalvik.system.profiler.BinaryHprof$Tag, int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: dalvik.system.profiler.BinaryHprofWriter.writeStackFrame(java.lang.StackTraceElement):int, dex:  in method: dalvik.system.profiler.BinaryHprofWriter.writeStackFrame(java.lang.StackTraceElement):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: dalvik.system.profiler.BinaryHprofWriter.writeStackFrame(java.lang.StackTraceElement):int, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:72)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private int writeStackFrame(java.lang.StackTraceElement r1) throws java.io.IOException {
        /*
        // Can't load method instructions: Load method exception: null in method: dalvik.system.profiler.BinaryHprofWriter.writeStackFrame(java.lang.StackTraceElement):int, dex:  in method: dalvik.system.profiler.BinaryHprofWriter.writeStackFrame(java.lang.StackTraceElement):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprofWriter.writeStackFrame(java.lang.StackTraceElement):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.BinaryHprofWriter.writeStackTrace(dalvik.system.profiler.HprofData$StackTrace):void, dex: 
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
    private void writeStackTrace(dalvik.system.profiler.HprofData.StackTrace r1) throws java.io.IOException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.BinaryHprofWriter.writeStackTrace(dalvik.system.profiler.HprofData$StackTrace):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprofWriter.writeStackTrace(dalvik.system.profiler.HprofData$StackTrace):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.BinaryHprofWriter.writeStartThread(dalvik.system.profiler.HprofData$ThreadEvent):void, dex: 
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
    private void writeStartThread(dalvik.system.profiler.HprofData.ThreadEvent r1) throws java.io.IOException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.BinaryHprofWriter.writeStartThread(dalvik.system.profiler.HprofData$ThreadEvent):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprofWriter.writeStartThread(dalvik.system.profiler.HprofData$ThreadEvent):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: dalvik.system.profiler.BinaryHprofWriter.writeStopThread(dalvik.system.profiler.HprofData$ThreadEvent):void, dex:  in method: dalvik.system.profiler.BinaryHprofWriter.writeStopThread(dalvik.system.profiler.HprofData$ThreadEvent):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: dalvik.system.profiler.BinaryHprofWriter.writeStopThread(dalvik.system.profiler.HprofData$ThreadEvent):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:72)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void writeStopThread(dalvik.system.profiler.HprofData.ThreadEvent r1) throws java.io.IOException {
        /*
        // Can't load method instructions: Load method exception: null in method: dalvik.system.profiler.BinaryHprofWriter.writeStopThread(dalvik.system.profiler.HprofData$ThreadEvent):void, dex:  in method: dalvik.system.profiler.BinaryHprofWriter.writeStopThread(dalvik.system.profiler.HprofData$ThreadEvent):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprofWriter.writeStopThread(dalvik.system.profiler.HprofData$ThreadEvent):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: dalvik.system.profiler.BinaryHprofWriter.writeString(java.lang.String):int, dex:  in method: dalvik.system.profiler.BinaryHprofWriter.writeString(java.lang.String):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: dalvik.system.profiler.BinaryHprofWriter.writeString(java.lang.String):int, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private int writeString(java.lang.String r1) throws java.io.IOException {
        /*
        // Can't load method instructions: Load method exception: null in method: dalvik.system.profiler.BinaryHprofWriter.writeString(java.lang.String):int, dex:  in method: dalvik.system.profiler.BinaryHprofWriter.writeString(java.lang.String):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprofWriter.writeString(java.lang.String):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.BinaryHprofWriter.writeThreadEvent(dalvik.system.profiler.HprofData$ThreadEvent):void, dex: 
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
    private void writeThreadEvent(dalvik.system.profiler.HprofData.ThreadEvent r1) throws java.io.IOException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.BinaryHprofWriter.writeThreadEvent(dalvik.system.profiler.HprofData$ThreadEvent):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.BinaryHprofWriter.writeThreadEvent(dalvik.system.profiler.HprofData$ThreadEvent):void");
    }
}
