package java.lang;

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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.collectAllInsns(BlockUtils.java:556)
    	at jadx.core.dex.visitors.ClassModifier.removeBridgeMethod(ClassModifier.java:197)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticMethods(ClassModifier.java:135)
    	at jadx.core.dex.visitors.ClassModifier.lambda$visit$0(ClassModifier.java:49)
    	at java.util.ArrayList.forEach(ArrayList.java:1251)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:49)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class Shutdown {
    private static final int FINALIZERS = 2;
    private static final int HOOKS = 1;
    private static final int MAX_SYSTEM_HOOKS = 10;
    private static final int RUNNING = 0;
    private static int currentRunningHook;
    private static Object haltLock;
    private static final Runnable[] hooks = null;
    private static Object lock;
    private static boolean runFinalizersOnExit;
    private static int state;

    private static class Lock {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.lang.Shutdown.Lock.<init>():void, dex: 
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
        private Lock() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.lang.Shutdown.Lock.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.Shutdown.Lock.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.lang.Shutdown.Lock.<init>(java.lang.Shutdown$Lock):void, dex: 
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
        /* synthetic */ Lock(java.lang.Shutdown.Lock r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.lang.Shutdown.Lock.<init>(java.lang.Shutdown$Lock):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.Shutdown.Lock.<init>(java.lang.Shutdown$Lock):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.lang.Shutdown.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.lang.Shutdown.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.Shutdown.<clinit>():void");
    }

    static native void halt0(int i);

    private static native void runAllFinalizers();

    public Shutdown() {
    }

    static void setRunFinalizersOnExit(boolean run) {
        synchronized (lock) {
            runFinalizersOnExit = run;
        }
    }

    public static void add(int slot, boolean registerShutdownInProgress, Runnable hook) {
        synchronized (lock) {
            if (hooks[slot] != null) {
                throw new InternalError("Shutdown hook at slot " + slot + " already registered");
            }
            if (registerShutdownInProgress) {
                if (state > 1 || (state == 1 && slot <= currentRunningHook)) {
                    throw new IllegalStateException("Shutdown in progress");
                }
            } else if (state > 0) {
                throw new IllegalStateException("Shutdown in progress");
            }
            hooks[slot] = hook;
        }
    }

    private static void runHooks() {
        for (int i = 0; i < 10; i++) {
            try {
                Runnable hook;
                synchronized (lock) {
                    currentRunningHook = i;
                    hook = hooks[i];
                }
                if (hook != null) {
                    hook.run();
                }
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath threadDeath = (ThreadDeath) t;
                }
            }
        }
    }

    static void halt(int status) {
        synchronized (haltLock) {
            halt0(status);
        }
    }

    /* JADX WARNING: Missing block: B:9:0x000b, code:
            runHooks();
            r2 = lock;
     */
    /* JADX WARNING: Missing block: B:10:0x0010, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:13:?, code:
            state = 2;
            r0 = runFinalizersOnExit;
     */
    /* JADX WARNING: Missing block: B:14:0x0016, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:15:0x0017, code:
            if (r0 == false) goto L_0x001c;
     */
    /* JADX WARNING: Missing block: B:16:0x0019, code:
            runAllFinalizers();
     */
    /* JADX WARNING: Missing block: B:17:0x001c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void sequence() {
        synchronized (lock) {
            if (state != 1) {
            }
        }
    }

    static void exit(int status) {
        boolean runMoreFinalizers = false;
        synchronized (lock) {
            if (status != 0) {
                runFinalizersOnExit = false;
            }
            switch (state) {
                case 0:
                    state = 1;
                    break;
                case 2:
                    if (status == 0) {
                        runMoreFinalizers = runFinalizersOnExit;
                        break;
                    } else {
                        halt(status);
                        break;
                    }
            }
        }
        if (runMoreFinalizers) {
            runAllFinalizers();
            halt(status);
        }
        synchronized (Shutdown.class) {
            sequence();
            halt(status);
        }
    }

    static void shutdown() {
        synchronized (lock) {
            switch (state) {
                case 0:
                    state = 1;
                    break;
            }
        }
        synchronized (Shutdown.class) {
            sequence();
        }
    }
}
