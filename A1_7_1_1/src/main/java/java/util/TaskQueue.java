package java.util;

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
/* compiled from: Timer */
class TaskQueue {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f77-assertionsDisabled = false;
    private TimerTask[] queue;
    private int size;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.TaskQueue.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.TaskQueue.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.TaskQueue.<clinit>():void");
    }

    TaskQueue() {
        this.queue = new TimerTask[128];
        this.size = 0;
    }

    int size() {
        return this.size;
    }

    void add(TimerTask task) {
        if (this.size + 1 == this.queue.length) {
            this.queue = (TimerTask[]) Arrays.copyOf(this.queue, this.queue.length * 2);
        }
        TimerTask[] timerTaskArr = this.queue;
        int i = this.size + 1;
        this.size = i;
        timerTaskArr[i] = task;
        fixUp(this.size);
    }

    TimerTask getMin() {
        return this.queue[1];
    }

    TimerTask get(int i) {
        return this.queue[i];
    }

    void removeMin() {
        this.queue[1] = this.queue[this.size];
        TimerTask[] timerTaskArr = this.queue;
        int i = this.size;
        this.size = i - 1;
        timerTaskArr[i] = null;
        fixDown(1);
    }

    void quickRemove(int i) {
        if (!f77-assertionsDisabled) {
            if ((i <= this.size ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        this.queue[i] = this.queue[this.size];
        TimerTask[] timerTaskArr = this.queue;
        int i2 = this.size;
        this.size = i2 - 1;
        timerTaskArr[i2] = null;
    }

    void rescheduleMin(long newTime) {
        this.queue[1].nextExecutionTime = newTime;
        fixDown(1);
    }

    boolean isEmpty() {
        return this.size == 0;
    }

    void clear() {
        for (int i = 1; i <= this.size; i++) {
            this.queue[i] = null;
        }
        this.size = 0;
    }

    private void fixUp(int k) {
        while (k > 1) {
            int j = k >> 1;
            if (this.queue[j].nextExecutionTime > this.queue[k].nextExecutionTime) {
                TimerTask tmp = this.queue[j];
                this.queue[j] = this.queue[k];
                this.queue[k] = tmp;
                k = j;
            } else {
                return;
            }
        }
    }

    private void fixDown(int k) {
        while (true) {
            int j = k << 1;
            if (j <= this.size && j > 0) {
                if (j < this.size && this.queue[j].nextExecutionTime > this.queue[j + 1].nextExecutionTime) {
                    j++;
                }
                if (this.queue[k].nextExecutionTime > this.queue[j].nextExecutionTime) {
                    TimerTask tmp = this.queue[j];
                    this.queue[j] = this.queue[k];
                    this.queue[k] = tmp;
                    k = j;
                } else {
                    return;
                }
            }
            return;
        }
    }

    void heapify() {
        for (int i = this.size / 2; i >= 1; i--) {
            fixDown(i);
        }
    }
}
