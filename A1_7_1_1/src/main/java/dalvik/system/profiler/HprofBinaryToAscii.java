package dalvik.system.profiler;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
public final class HprofBinaryToAscii {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: dalvik.system.profiler.HprofBinaryToAscii.<init>():void, dex: 
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
    public HprofBinaryToAscii() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: dalvik.system.profiler.HprofBinaryToAscii.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.HprofBinaryToAscii.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: dalvik.system.profiler.HprofBinaryToAscii.closeQuietly(java.io.Closeable):void, dex: 
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
    private static void closeQuietly(java.io.Closeable r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: dalvik.system.profiler.HprofBinaryToAscii.closeQuietly(java.io.Closeable):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.HprofBinaryToAscii.closeQuietly(java.io.Closeable):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.HprofBinaryToAscii.convert(java.lang.String[]):boolean, dex: 
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
    private static boolean convert(java.lang.String[] r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.HprofBinaryToAscii.convert(java.lang.String[]):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.HprofBinaryToAscii.convert(java.lang.String[]):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: dalvik.system.profiler.HprofBinaryToAscii.main(java.lang.String[]):void, dex: 
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
    public static void main(java.lang.String[] r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: dalvik.system.profiler.HprofBinaryToAscii.main(java.lang.String[]):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.HprofBinaryToAscii.main(java.lang.String[]):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.HprofBinaryToAscii.read(java.io.InputStream):dalvik.system.profiler.HprofData, dex: 
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
    private static dalvik.system.profiler.HprofData read(java.io.InputStream r1) throws java.io.IOException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.HprofBinaryToAscii.read(java.io.InputStream):dalvik.system.profiler.HprofData, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.HprofBinaryToAscii.read(java.io.InputStream):dalvik.system.profiler.HprofData");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.HprofBinaryToAscii.readSnapshot(java.io.File):dalvik.system.profiler.HprofData, dex: 
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
    private static dalvik.system.profiler.HprofData readSnapshot(java.io.File r1) throws java.io.IOException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.HprofBinaryToAscii.readSnapshot(java.io.File):dalvik.system.profiler.HprofData, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.HprofBinaryToAscii.readSnapshot(java.io.File):dalvik.system.profiler.HprofData");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.HprofBinaryToAscii.usage(java.lang.String):void, dex: 
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
    private static void usage(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.HprofBinaryToAscii.usage(java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.HprofBinaryToAscii.usage(java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.HprofBinaryToAscii.write(dalvik.system.profiler.HprofData):boolean, dex: 
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
    private static boolean write(dalvik.system.profiler.HprofData r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.HprofBinaryToAscii.write(dalvik.system.profiler.HprofData):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.HprofBinaryToAscii.write(dalvik.system.profiler.HprofData):boolean");
    }

    private static boolean startsWithMagic(File file) {
        Throwable th;
        boolean z = false;
        DataInputStream inputStream = null;
        try {
            DataInputStream inputStream2 = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            try {
                if (BinaryHprof.readMagic(inputStream2) != null) {
                    z = true;
                }
                closeQuietly(inputStream2);
                return z;
            } catch (IOException e) {
                inputStream = inputStream2;
            } catch (Throwable th2) {
                th = th2;
                inputStream = inputStream2;
                closeQuietly(inputStream);
                throw th;
            }
        } catch (IOException e2) {
            closeQuietly(inputStream);
            return false;
        } catch (Throwable th3) {
            th = th3;
            closeQuietly(inputStream);
            throw th;
        }
    }

    private static HprofData readHprof(File file) throws IOException {
        Throwable th;
        InputStream inputStream = null;
        try {
            InputStream inputStream2 = new BufferedInputStream(new FileInputStream(file));
            try {
                HprofData read = read(inputStream2);
                closeQuietly(inputStream2);
                return read;
            } catch (Throwable th2) {
                th = th2;
                inputStream = inputStream2;
                closeQuietly(inputStream);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            closeQuietly(inputStream);
            throw th;
        }
    }
}
