package java.util.concurrent;

import libcore.icu.RelativeDateTimeFormatter;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: EnumVisitor
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public enum TimeUnit {
    ;
    
    static final long C0 = 1;
    static final long C1 = 1000;
    static final long C2 = 1000000;
    static final long C3 = 1000000000;
    static final long C4 = 60000000000L;
    static final long C5 = 3600000000000L;
    static final long C6 = 86400000000000L;
    static final long MAX = Long.MAX_VALUE;

    /* renamed from: java.util.concurrent.TimeUnit$1 */
    enum AnonymousClass1 extends TimeUnit {
        AnonymousClass1(String str, int i) {
            super(str, i, null);
        }

        public long toNanos(long d) {
            return d;
        }

        public long toMicros(long d) {
            return d / 1000;
        }

        public long toMillis(long d) {
            return d / TimeUnit.C2;
        }

        public long toSeconds(long d) {
            return d / TimeUnit.C3;
        }

        public long toMinutes(long d) {
            return d / TimeUnit.C4;
        }

        public long toHours(long d) {
            return d / TimeUnit.C5;
        }

        public long toDays(long d) {
            return d / TimeUnit.C6;
        }

        public long convert(long d, TimeUnit u) {
            return u.toNanos(d);
        }

        int excessNanos(long d, long m) {
            return (int) (d - (TimeUnit.C2 * m));
        }
    }

    /* renamed from: java.util.concurrent.TimeUnit$2 */
    enum AnonymousClass2 extends TimeUnit {
        AnonymousClass2(String str, int i) {
            super(str, i, null);
        }

        public long toNanos(long d) {
            return TimeUnit.x(d, 1000, 9223372036854775L);
        }

        public long toMicros(long d) {
            return d;
        }

        public long toMillis(long d) {
            return d / 1000;
        }

        public long toSeconds(long d) {
            return d / TimeUnit.C2;
        }

        public long toMinutes(long d) {
            return d / 60000000;
        }

        public long toHours(long d) {
            return d / 3600000000L;
        }

        public long toDays(long d) {
            return d / 86400000000L;
        }

        public long convert(long d, TimeUnit u) {
            return u.toMicros(d);
        }

        int excessNanos(long d, long m) {
            return (int) ((1000 * d) - (TimeUnit.C2 * m));
        }
    }

    /* renamed from: java.util.concurrent.TimeUnit$3 */
    enum AnonymousClass3 extends TimeUnit {
        AnonymousClass3(String str, int i) {
            super(str, i, null);
        }

        public long toNanos(long d) {
            return TimeUnit.x(d, TimeUnit.C2, 9223372036854L);
        }

        public long toMicros(long d) {
            return TimeUnit.x(d, 1000, 9223372036854775L);
        }

        public long toMillis(long d) {
            return d;
        }

        public long toSeconds(long d) {
            return d / 1000;
        }

        public long toMinutes(long d) {
            return d / RelativeDateTimeFormatter.MINUTE_IN_MILLIS;
        }

        public long toHours(long d) {
            return d / RelativeDateTimeFormatter.HOUR_IN_MILLIS;
        }

        public long toDays(long d) {
            return d / 86400000;
        }

        public long convert(long d, TimeUnit u) {
            return u.toMillis(d);
        }

        int excessNanos(long d, long m) {
            return 0;
        }
    }

    /* renamed from: java.util.concurrent.TimeUnit$4 */
    enum AnonymousClass4 extends TimeUnit {
        AnonymousClass4(String str, int i) {
            super(str, i, null);
        }

        public long toNanos(long d) {
            return TimeUnit.x(d, TimeUnit.C3, 9223372036L);
        }

        public long toMicros(long d) {
            return TimeUnit.x(d, TimeUnit.C2, 9223372036854L);
        }

        public long toMillis(long d) {
            return TimeUnit.x(d, 1000, 9223372036854775L);
        }

        public long toSeconds(long d) {
            return d;
        }

        public long toMinutes(long d) {
            return d / 60;
        }

        public long toHours(long d) {
            return d / 3600;
        }

        public long toDays(long d) {
            return d / 86400;
        }

        public long convert(long d, TimeUnit u) {
            return u.toSeconds(d);
        }

        int excessNanos(long d, long m) {
            return 0;
        }
    }

    /* renamed from: java.util.concurrent.TimeUnit$5 */
    enum AnonymousClass5 extends TimeUnit {
        AnonymousClass5(String str, int i) {
            super(str, i, null);
        }

        public long toNanos(long d) {
            return TimeUnit.x(d, TimeUnit.C4, 153722867);
        }

        public long toMicros(long d) {
            return TimeUnit.x(d, 60000000, 153722867280L);
        }

        public long toMillis(long d) {
            return TimeUnit.x(d, RelativeDateTimeFormatter.MINUTE_IN_MILLIS, 153722867280912L);
        }

        public long toSeconds(long d) {
            return TimeUnit.x(d, 60, 153722867280912930L);
        }

        public long toMinutes(long d) {
            return d;
        }

        public long toHours(long d) {
            return d / 60;
        }

        public long toDays(long d) {
            return d / 1440;
        }

        public long convert(long d, TimeUnit u) {
            return u.toMinutes(d);
        }

        int excessNanos(long d, long m) {
            return 0;
        }
    }

    /* renamed from: java.util.concurrent.TimeUnit$6 */
    enum AnonymousClass6 extends TimeUnit {
        AnonymousClass6(String str, int i) {
            super(str, i, null);
        }

        public long toNanos(long d) {
            return TimeUnit.x(d, TimeUnit.C5, 2562047);
        }

        public long toMicros(long d) {
            return TimeUnit.x(d, 3600000000L, 2562047788L);
        }

        public long toMillis(long d) {
            return TimeUnit.x(d, RelativeDateTimeFormatter.HOUR_IN_MILLIS, 2562047788015L);
        }

        public long toSeconds(long d) {
            return TimeUnit.x(d, 3600, 2562047788015215L);
        }

        public long toMinutes(long d) {
            return TimeUnit.x(d, 60, 153722867280912930L);
        }

        public long toHours(long d) {
            return d;
        }

        public long toDays(long d) {
            return d / 24;
        }

        public long convert(long d, TimeUnit u) {
            return u.toHours(d);
        }

        int excessNanos(long d, long m) {
            return 0;
        }
    }

    /* renamed from: java.util.concurrent.TimeUnit$7 */
    enum AnonymousClass7 extends TimeUnit {
        AnonymousClass7(String str, int i) {
            super(str, i, null);
        }

        public long toNanos(long d) {
            return TimeUnit.x(d, TimeUnit.C6, 106751);
        }

        public long toMicros(long d) {
            return TimeUnit.x(d, 86400000000L, 106751991);
        }

        public long toMillis(long d) {
            return TimeUnit.x(d, 86400000, 106751991167L);
        }

        public long toSeconds(long d) {
            return TimeUnit.x(d, 86400, 106751991167300L);
        }

        public long toMinutes(long d) {
            return TimeUnit.x(d, 1440, 6405119470038038L);
        }

        public long toHours(long d) {
            return TimeUnit.x(d, 24, 384307168202282325L);
        }

        public long toDays(long d) {
            return d;
        }

        public long convert(long d, TimeUnit u) {
            return u.toDays(d);
        }

        int excessNanos(long d, long m) {
            return 0;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.TimeUnit.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.concurrent.TimeUnit.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.TimeUnit.<clinit>():void");
    }

    abstract int excessNanos(long j, long j2);

    static long x(long d, long m, long over) {
        if (d > over) {
            return MAX;
        }
        if (d < (-over)) {
            return Long.MIN_VALUE;
        }
        return d * m;
    }

    public long convert(long sourceDuration, TimeUnit sourceUnit) {
        throw new AbstractMethodError();
    }

    public long toNanos(long duration) {
        throw new AbstractMethodError();
    }

    public long toMicros(long duration) {
        throw new AbstractMethodError();
    }

    public long toMillis(long duration) {
        throw new AbstractMethodError();
    }

    public long toSeconds(long duration) {
        throw new AbstractMethodError();
    }

    public long toMinutes(long duration) {
        throw new AbstractMethodError();
    }

    public long toHours(long duration) {
        throw new AbstractMethodError();
    }

    public long toDays(long duration) {
        throw new AbstractMethodError();
    }

    public void timedWait(Object obj, long timeout) throws InterruptedException {
        if (timeout > 0) {
            long ms = toMillis(timeout);
            obj.wait(ms, excessNanos(timeout, ms));
        }
    }

    public void timedJoin(Thread thread, long timeout) throws InterruptedException {
        if (timeout > 0) {
            long ms = toMillis(timeout);
            thread.join(ms, excessNanos(timeout, ms));
        }
    }

    public void sleep(long timeout) throws InterruptedException {
        if (timeout > 0) {
            long ms = toMillis(timeout);
            Thread.sleep(ms, excessNanos(timeout, ms));
        }
    }
}
