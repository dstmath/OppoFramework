package android_maps_conflict_avoidance.com.google.common.task;

import java.util.Vector;

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
public abstract class AbstractTask {
    private static final AbstractTask[] EMPTY_TASK_ARRAY = null;
    private final String name;
    private int runCounter;
    private Object runCounterLock;
    protected Runnable runnable;
    protected TaskRunner runner;
    private int state;
    protected Vector tasks;
    private final String varzInsideQueue;
    private final String varzOutsideQueue;
    private final String varzTime;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.common.task.AbstractTask.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.common.task.AbstractTask.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.task.AbstractTask.<clinit>():void");
    }

    abstract int cancelInternal();

    abstract void scheduleInternal();

    public AbstractTask(TaskRunner runner, Runnable runnable, String name) {
        this.runCounterLock = new Object();
        this.runner = runner;
        this.runnable = runnable;
        this.name = name;
        this.varzOutsideQueue = null;
        this.varzInsideQueue = null;
        this.varzTime = null;
    }

    protected AbstractTask[] getTasks() {
        AbstractTask[] taskArray;
        synchronized (this) {
            if (this.tasks != null) {
                taskArray = new AbstractTask[this.tasks.size()];
                this.tasks.copyInto(taskArray);
            } else {
                taskArray = EMPTY_TASK_ARRAY;
            }
        }
        return taskArray;
    }

    protected int getState() {
        return this.state;
    }

    protected void setState(int state) {
        this.state = state;
    }

    public void schedule() {
        synchronized (this.runCounterLock) {
            this.runCounter = 0;
        }
        this.runner.scheduleTask(this);
    }

    protected void run() {
        if (this.runnable != null) {
            this.runnable.run();
        }
    }

    void runInternal() {
        try {
            run();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        synchronized (this.runCounterLock) {
            this.runCounter++;
            this.runCounterLock.notifyAll();
        }
        AbstractTask[] taskArray = getTasks();
        for (AbstractTask schedule : taskArray) {
            schedule.schedule();
        }
    }

    void updateScheduleTimestamp() {
        if (this.name != null) {
        }
    }

    void updateRunnableTimestamp() {
        if (this.name != null) {
        }
    }

    void updateStartTimestamp() {
        if (this.name != null) {
        }
    }

    void updateFinishTimestamp() {
        if (this.name != null) {
        }
    }
}
