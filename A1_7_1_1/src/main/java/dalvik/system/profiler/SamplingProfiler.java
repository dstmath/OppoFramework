package dalvik.system.profiler;

import dalvik.system.profiler.HprofData.StackTrace;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class SamplingProfiler {
    private Thread[] currentThreads;
    private final int depth;
    private final HprofData hprofData;
    private final StackTrace mutableStackTrace;
    private int nextObjectId;
    private int nextStackTraceId;
    private int nextThreadId;
    private Sampler sampler;
    private final Map<StackTrace, int[]> stackTraces;
    private final Map<Thread, Integer> threadIds;
    private final ThreadSampler threadSampler;
    private final ThreadSet threadSet;
    private final Timer timer;

    public interface ThreadSet {
        Thread[] threads();
    }

    private static class ArrayThreadSet implements ThreadSet {
        private final Thread[] threads;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: dalvik.system.profiler.SamplingProfiler.ArrayThreadSet.<init>(java.lang.Thread[]):void, dex: 
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
        public ArrayThreadSet(java.lang.Thread... r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: dalvik.system.profiler.SamplingProfiler.ArrayThreadSet.<init>(java.lang.Thread[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.ArrayThreadSet.<init>(java.lang.Thread[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.ArrayThreadSet.threads():java.lang.Thread[], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public java.lang.Thread[] threads() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.ArrayThreadSet.threads():java.lang.Thread[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.ArrayThreadSet.threads():java.lang.Thread[]");
        }
    }

    private class Sampler extends TimerTask {
        private boolean stop;
        private boolean stopped;
        final /* synthetic */ SamplingProfiler this$0;
        private Thread timerThread;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: dalvik.system.profiler.SamplingProfiler.Sampler.-get0(dalvik.system.profiler.SamplingProfiler$Sampler):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -get0 */
        static /* synthetic */ boolean m126-get0(dalvik.system.profiler.SamplingProfiler.Sampler r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: dalvik.system.profiler.SamplingProfiler.Sampler.-get0(dalvik.system.profiler.SamplingProfiler$Sampler):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.Sampler.-get0(dalvik.system.profiler.SamplingProfiler$Sampler):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: dalvik.system.profiler.SamplingProfiler.Sampler.-set0(dalvik.system.profiler.SamplingProfiler$Sampler, boolean):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -set0 */
        static /* synthetic */ boolean m127-set0(dalvik.system.profiler.SamplingProfiler.Sampler r1, boolean r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: dalvik.system.profiler.SamplingProfiler.Sampler.-set0(dalvik.system.profiler.SamplingProfiler$Sampler, boolean):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.Sampler.-set0(dalvik.system.profiler.SamplingProfiler$Sampler, boolean):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: dalvik.system.profiler.SamplingProfiler.Sampler.<init>(dalvik.system.profiler.SamplingProfiler):void, dex: 
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
        private Sampler(dalvik.system.profiler.SamplingProfiler r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: dalvik.system.profiler.SamplingProfiler.Sampler.<init>(dalvik.system.profiler.SamplingProfiler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.Sampler.<init>(dalvik.system.profiler.SamplingProfiler):void");
        }

        /* synthetic */ Sampler(SamplingProfiler this$0, Sampler sampler) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.Sampler.addEndThread(java.lang.Thread):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void addEndThread(java.lang.Thread r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.Sampler.addEndThread(java.lang.Thread):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.Sampler.addEndThread(java.lang.Thread):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.Sampler.addStartThread(java.lang.Thread):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void addStartThread(java.lang.Thread r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.Sampler.addStartThread(java.lang.Thread):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.Sampler.addStartThread(java.lang.Thread):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.Sampler.recordStackTrace(java.lang.Thread, java.lang.StackTraceElement[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void recordStackTrace(java.lang.Thread r1, java.lang.StackTraceElement[] r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.Sampler.recordStackTrace(java.lang.Thread, java.lang.StackTraceElement[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.Sampler.recordStackTrace(java.lang.Thread, java.lang.StackTraceElement[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.Sampler.updateThreadHistory(java.lang.Thread[], java.lang.Thread[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void updateThreadHistory(java.lang.Thread[] r1, java.lang.Thread[] r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.Sampler.updateThreadHistory(java.lang.Thread[], java.lang.Thread[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.Sampler.updateThreadHistory(java.lang.Thread[], java.lang.Thread[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: dalvik.system.profiler.SamplingProfiler.Sampler.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: dalvik.system.profiler.SamplingProfiler.Sampler.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.Sampler.run():void");
        }
    }

    private static class ThreadGroupThreadSet implements ThreadSet {
        private int lastThread;
        private final ThreadGroup threadGroup;
        private Thread[] threads;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: dalvik.system.profiler.SamplingProfiler.ThreadGroupThreadSet.<init>(java.lang.ThreadGroup):void, dex: 
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
        public ThreadGroupThreadSet(java.lang.ThreadGroup r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: dalvik.system.profiler.SamplingProfiler.ThreadGroupThreadSet.<init>(java.lang.ThreadGroup):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.ThreadGroupThreadSet.<init>(java.lang.ThreadGroup):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.ThreadGroupThreadSet.resize():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void resize() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.ThreadGroupThreadSet.resize():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.ThreadGroupThreadSet.resize():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.ThreadGroupThreadSet.threads():java.lang.Thread[], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public java.lang.Thread[] threads() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.ThreadGroupThreadSet.threads():java.lang.Thread[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.ThreadGroupThreadSet.threads():java.lang.Thread[]");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.-get0(dalvik.system.profiler.SamplingProfiler):java.lang.Thread[], dex: 
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
    /* renamed from: -get0 */
    static /* synthetic */ java.lang.Thread[] m112-get0(dalvik.system.profiler.SamplingProfiler r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.-get0(dalvik.system.profiler.SamplingProfiler):java.lang.Thread[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.-get0(dalvik.system.profiler.SamplingProfiler):java.lang.Thread[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.-get1(dalvik.system.profiler.SamplingProfiler):dalvik.system.profiler.HprofData, dex: 
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
    /* renamed from: -get1 */
    static /* synthetic */ dalvik.system.profiler.HprofData m113-get1(dalvik.system.profiler.SamplingProfiler r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.-get1(dalvik.system.profiler.SamplingProfiler):dalvik.system.profiler.HprofData, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.-get1(dalvik.system.profiler.SamplingProfiler):dalvik.system.profiler.HprofData");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.-get2(dalvik.system.profiler.SamplingProfiler):dalvik.system.profiler.HprofData$StackTrace, dex: 
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
    /* renamed from: -get2 */
    static /* synthetic */ dalvik.system.profiler.HprofData.StackTrace m114-get2(dalvik.system.profiler.SamplingProfiler r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.-get2(dalvik.system.profiler.SamplingProfiler):dalvik.system.profiler.HprofData$StackTrace, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.-get2(dalvik.system.profiler.SamplingProfiler):dalvik.system.profiler.HprofData$StackTrace");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: dalvik.system.profiler.SamplingProfiler.-get3(dalvik.system.profiler.SamplingProfiler):int, dex: 
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
    /* renamed from: -get3 */
    static /* synthetic */ int m115-get3(dalvik.system.profiler.SamplingProfiler r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: dalvik.system.profiler.SamplingProfiler.-get3(dalvik.system.profiler.SamplingProfiler):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.-get3(dalvik.system.profiler.SamplingProfiler):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: dalvik.system.profiler.SamplingProfiler.-get4(dalvik.system.profiler.SamplingProfiler):int, dex: 
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
    /* renamed from: -get4 */
    static /* synthetic */ int m116-get4(dalvik.system.profiler.SamplingProfiler r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: dalvik.system.profiler.SamplingProfiler.-get4(dalvik.system.profiler.SamplingProfiler):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.-get4(dalvik.system.profiler.SamplingProfiler):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: dalvik.system.profiler.SamplingProfiler.-get5(dalvik.system.profiler.SamplingProfiler):int, dex: 
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
    /* renamed from: -get5 */
    static /* synthetic */ int m117-get5(dalvik.system.profiler.SamplingProfiler r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: dalvik.system.profiler.SamplingProfiler.-get5(dalvik.system.profiler.SamplingProfiler):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.-get5(dalvik.system.profiler.SamplingProfiler):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: dalvik.system.profiler.SamplingProfiler.-get6(dalvik.system.profiler.SamplingProfiler):java.util.Map, dex:  in method: dalvik.system.profiler.SamplingProfiler.-get6(dalvik.system.profiler.SamplingProfiler):java.util.Map, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: dalvik.system.profiler.SamplingProfiler.-get6(dalvik.system.profiler.SamplingProfiler):java.util.Map, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get6 */
    static /* synthetic */ java.util.Map m118-get6(dalvik.system.profiler.SamplingProfiler r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: dalvik.system.profiler.SamplingProfiler.-get6(dalvik.system.profiler.SamplingProfiler):java.util.Map, dex:  in method: dalvik.system.profiler.SamplingProfiler.-get6(dalvik.system.profiler.SamplingProfiler):java.util.Map, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.-get6(dalvik.system.profiler.SamplingProfiler):java.util.Map");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.-get7(dalvik.system.profiler.SamplingProfiler):java.util.Map, dex: 
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
    /* renamed from: -get7 */
    static /* synthetic */ java.util.Map m119-get7(dalvik.system.profiler.SamplingProfiler r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.-get7(dalvik.system.profiler.SamplingProfiler):java.util.Map, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.-get7(dalvik.system.profiler.SamplingProfiler):java.util.Map");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.-get8(dalvik.system.profiler.SamplingProfiler):dalvik.system.profiler.ThreadSampler, dex: 
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
    /* renamed from: -get8 */
    static /* synthetic */ dalvik.system.profiler.ThreadSampler m120-get8(dalvik.system.profiler.SamplingProfiler r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.-get8(dalvik.system.profiler.SamplingProfiler):dalvik.system.profiler.ThreadSampler, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.-get8(dalvik.system.profiler.SamplingProfiler):dalvik.system.profiler.ThreadSampler");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: dalvik.system.profiler.SamplingProfiler.-get9(dalvik.system.profiler.SamplingProfiler):dalvik.system.profiler.SamplingProfiler$ThreadSet, dex:  in method: dalvik.system.profiler.SamplingProfiler.-get9(dalvik.system.profiler.SamplingProfiler):dalvik.system.profiler.SamplingProfiler$ThreadSet, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: dalvik.system.profiler.SamplingProfiler.-get9(dalvik.system.profiler.SamplingProfiler):dalvik.system.profiler.SamplingProfiler$ThreadSet, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get9 */
    static /* synthetic */ dalvik.system.profiler.SamplingProfiler.ThreadSet m121-get9(dalvik.system.profiler.SamplingProfiler r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: dalvik.system.profiler.SamplingProfiler.-get9(dalvik.system.profiler.SamplingProfiler):dalvik.system.profiler.SamplingProfiler$ThreadSet, dex:  in method: dalvik.system.profiler.SamplingProfiler.-get9(dalvik.system.profiler.SamplingProfiler):dalvik.system.profiler.SamplingProfiler$ThreadSet, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.-get9(dalvik.system.profiler.SamplingProfiler):dalvik.system.profiler.SamplingProfiler$ThreadSet");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: dalvik.system.profiler.SamplingProfiler.-set0(dalvik.system.profiler.SamplingProfiler, java.lang.Thread[]):java.lang.Thread[], dex: 
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
    /* renamed from: -set0 */
    static /* synthetic */ java.lang.Thread[] m122-set0(dalvik.system.profiler.SamplingProfiler r1, java.lang.Thread[] r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: dalvik.system.profiler.SamplingProfiler.-set0(dalvik.system.profiler.SamplingProfiler, java.lang.Thread[]):java.lang.Thread[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.-set0(dalvik.system.profiler.SamplingProfiler, java.lang.Thread[]):java.lang.Thread[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: dalvik.system.profiler.SamplingProfiler.-set1(dalvik.system.profiler.SamplingProfiler, int):int, dex: 
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
    /* renamed from: -set1 */
    static /* synthetic */ int m123-set1(dalvik.system.profiler.SamplingProfiler r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: dalvik.system.profiler.SamplingProfiler.-set1(dalvik.system.profiler.SamplingProfiler, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.-set1(dalvik.system.profiler.SamplingProfiler, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: dalvik.system.profiler.SamplingProfiler.-set2(dalvik.system.profiler.SamplingProfiler, int):int, dex: 
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
    /* renamed from: -set2 */
    static /* synthetic */ int m124-set2(dalvik.system.profiler.SamplingProfiler r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: dalvik.system.profiler.SamplingProfiler.-set2(dalvik.system.profiler.SamplingProfiler, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.-set2(dalvik.system.profiler.SamplingProfiler, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: dalvik.system.profiler.SamplingProfiler.-set3(dalvik.system.profiler.SamplingProfiler, int):int, dex: 
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
    /* renamed from: -set3 */
    static /* synthetic */ int m125-set3(dalvik.system.profiler.SamplingProfiler r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: dalvik.system.profiler.SamplingProfiler.-set3(dalvik.system.profiler.SamplingProfiler, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.-set3(dalvik.system.profiler.SamplingProfiler, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: dalvik.system.profiler.SamplingProfiler.<init>(int, dalvik.system.profiler.SamplingProfiler$ThreadSet):void, dex: 
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
    public SamplingProfiler(int r1, dalvik.system.profiler.SamplingProfiler.ThreadSet r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: dalvik.system.profiler.SamplingProfiler.<init>(int, dalvik.system.profiler.SamplingProfiler$ThreadSet):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.<init>(int, dalvik.system.profiler.SamplingProfiler$ThreadSet):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.SamplingProfiler.findDefaultThreadSampler():dalvik.system.profiler.ThreadSampler, dex: 
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
    private static dalvik.system.profiler.ThreadSampler findDefaultThreadSampler() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.SamplingProfiler.findDefaultThreadSampler():dalvik.system.profiler.ThreadSampler, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.findDefaultThreadSampler():dalvik.system.profiler.ThreadSampler");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.getHprofData():dalvik.system.profiler.HprofData, dex: 
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
    public dalvik.system.profiler.HprofData getHprofData() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.getHprofData():dalvik.system.profiler.HprofData, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.getHprofData():dalvik.system.profiler.HprofData");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.SamplingProfiler.shutdown():void, dex: 
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
    public void shutdown() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: dalvik.system.profiler.SamplingProfiler.shutdown():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.shutdown():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.start(int):void, dex: 
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
    public void start(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.start(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.start(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.stop():void, dex: 
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
    public void stop() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: dalvik.system.profiler.SamplingProfiler.stop():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.SamplingProfiler.stop():void");
    }

    public static ThreadSet newArrayThreadSet(Thread... threads) {
        return new ArrayThreadSet(threads);
    }

    public static ThreadSet newThreadGroupThreadSet(ThreadGroup threadGroup) {
        return new ThreadGroupThreadSet(threadGroup);
    }
}
