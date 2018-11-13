package com.android.internal.os;

import android.content.pm.PackageInfo;
import android.os.Build;
import android.util.Log;
import com.android.internal.content.NativeLibraryHelper;
import dalvik.system.profiler.BinaryHprofWriter;
import dalvik.system.profiler.SamplingProfiler;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import libcore.io.IoUtils;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SamplingProfilerIntegration {
    public static final String SNAPSHOT_DIR = "/data/snapshots";
    private static final String TAG = "SamplingProfilerIntegration";
    private static final boolean enabled = false;
    private static final AtomicBoolean pending = null;
    private static SamplingProfiler samplingProfiler;
    private static final int samplingProfilerDepth = 0;
    private static final int samplingProfilerMilliseconds = 0;
    private static final Executor snapshotWriter = null;
    private static long startMillis;

    /* renamed from: com.android.internal.os.SamplingProfilerIntegration$2 */
    static class AnonymousClass2 implements Runnable {
        final /* synthetic */ PackageInfo val$packageInfo;
        final /* synthetic */ String val$processName;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.os.SamplingProfilerIntegration.2.<init>(java.lang.String, android.content.pm.PackageInfo):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        AnonymousClass2(java.lang.String r1, android.content.pm.PackageInfo r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.os.SamplingProfilerIntegration.2.<init>(java.lang.String, android.content.pm.PackageInfo):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.SamplingProfilerIntegration.2.<init>(java.lang.String, android.content.pm.PackageInfo):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: 9 in method: com.android.internal.os.SamplingProfilerIntegration.2.run():void, dex:  in method: com.android.internal.os.SamplingProfilerIntegration.2.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: 9 in method: com.android.internal.os.SamplingProfilerIntegration.2.run():void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: com.android.dex.DexException: bogus registerCount: 9
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: 9 in method: com.android.internal.os.SamplingProfilerIntegration.2.run():void, dex:  in method: com.android.internal.os.SamplingProfilerIntegration.2.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.SamplingProfilerIntegration.2.run():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.os.SamplingProfilerIntegration.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.os.SamplingProfilerIntegration.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.SamplingProfilerIntegration.<clinit>():void");
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void start() {
        if (!enabled) {
            return;
        }
        if (samplingProfiler != null) {
            Log.e(TAG, "SamplingProfilerIntegration already started at " + new Date(startMillis));
            return;
        }
        samplingProfiler = new SamplingProfiler(samplingProfilerDepth, SamplingProfiler.newThreadGroupThreadSet(Thread.currentThread().getThreadGroup()));
        samplingProfiler.start(samplingProfilerMilliseconds);
        startMillis = System.currentTimeMillis();
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
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static void writeSnapshot(java.lang.String r3, android.content.pm.PackageInfo r4) {
        /*
        r0 = enabled;
        if (r0 != 0) goto L_0x0005;
    L_0x0004:
        return;
    L_0x0005:
        r0 = samplingProfiler;
        if (r0 != 0) goto L_0x0013;
    L_0x0009:
        r0 = "SamplingProfilerIntegration";
        r1 = "SamplingProfilerIntegration is not started";
        android.util.Log.e(r0, r1);
        return;
    L_0x0013:
        r0 = pending;
        r1 = 0;
        r2 = 1;
        r0 = r0.compareAndSet(r1, r2);
        if (r0 == 0) goto L_0x0027;
    L_0x001d:
        r0 = snapshotWriter;
        r1 = new com.android.internal.os.SamplingProfilerIntegration$2;
        r1.<init>(r3, r4);
        r0.execute(r1);
    L_0x0027:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.SamplingProfilerIntegration.writeSnapshot(java.lang.String, android.content.pm.PackageInfo):void");
    }

    public static void writeZygoteSnapshot() {
        if (enabled) {
            writeSnapshotFile("zygote", null);
            samplingProfiler.shutdown();
            samplingProfiler = null;
            startMillis = 0;
        }
    }

    private static void writeSnapshotFile(String processName, PackageInfo packageInfo) {
        IOException e;
        Throwable th;
        if (enabled) {
            samplingProfiler.stop();
            String name = processName.replaceAll(":", ".");
            String path = "/data/snapshots/" + name + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + startMillis + ".snapshot";
            long start = System.currentTimeMillis();
            AutoCloseable outputStream = null;
            try {
                OutputStream outputStream2 = new BufferedOutputStream(new FileOutputStream(path));
                try {
                    PrintStream out = new PrintStream(outputStream2);
                    generateSnapshotHeader(name, packageInfo, out);
                    if (out.checkError()) {
                        throw new IOException();
                    }
                    BinaryHprofWriter.write(samplingProfiler.getHprofData(), outputStream2);
                    IoUtils.closeQuietly(outputStream2);
                    new File(path).setReadable(true, false);
                    Log.i(TAG, "Wrote snapshot " + path + " in " + (System.currentTimeMillis() - start) + "ms.");
                    samplingProfiler.start(samplingProfilerMilliseconds);
                } catch (IOException e2) {
                    e = e2;
                    outputStream = outputStream2;
                    try {
                        Log.e(TAG, "Error writing snapshot to " + path, e);
                        IoUtils.closeQuietly(outputStream);
                    } catch (Throwable th2) {
                        th = th2;
                        IoUtils.closeQuietly(outputStream);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    Object outputStream3 = outputStream2;
                    IoUtils.closeQuietly(outputStream);
                    throw th;
                }
            } catch (IOException e3) {
                e = e3;
                Log.e(TAG, "Error writing snapshot to " + path, e);
                IoUtils.closeQuietly(outputStream);
            }
        }
    }

    private static void generateSnapshotHeader(String processName, PackageInfo packageInfo, PrintStream out) {
        out.println("Version: 3");
        out.println("Process: " + processName);
        if (packageInfo != null) {
            out.println("Package: " + packageInfo.packageName);
            out.println("Package-Version: " + packageInfo.versionCode);
        }
        out.println("Build: " + Build.FINGERPRINT);
        out.println();
    }
}
