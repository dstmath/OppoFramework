package android_maps_conflict_avoidance.com.google.common.util;

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
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
public class Primitives {
    private static PrimitiveConverter converter;

    public static abstract class PrimitiveConverter {
        public abstract Integer toInteger(int i);

        public abstract Long toLong(long j);
    }

    private static class J2meConverter extends PrimitiveConverter {
        private Integer[] SMALL_INTS;
        private Long[] SMALL_LONGS;

        private J2meConverter() {
            Long[] lArr = new Long[16];
            lArr[0] = new Long(0);
            lArr[1] = new Long(1);
            lArr[2] = new Long(2);
            lArr[3] = new Long(3);
            lArr[4] = new Long(4);
            lArr[5] = new Long(5);
            lArr[6] = new Long(6);
            lArr[7] = new Long(7);
            lArr[8] = new Long(8);
            lArr[9] = new Long(9);
            lArr[10] = new Long(10);
            lArr[11] = new Long(11);
            lArr[12] = new Long(12);
            lArr[13] = new Long(13);
            lArr[14] = new Long(14);
            lArr[15] = new Long(15);
            this.SMALL_LONGS = lArr;
            Integer[] numArr = new Integer[16];
            numArr[0] = new Integer(0);
            numArr[1] = new Integer(1);
            numArr[2] = new Integer(2);
            numArr[3] = new Integer(3);
            numArr[4] = new Integer(4);
            numArr[5] = new Integer(5);
            numArr[6] = new Integer(6);
            numArr[7] = new Integer(7);
            numArr[8] = new Integer(8);
            numArr[9] = new Integer(9);
            numArr[10] = new Integer(10);
            numArr[11] = new Integer(11);
            numArr[12] = new Integer(12);
            numArr[13] = new Integer(13);
            numArr[14] = new Integer(14);
            numArr[15] = new Integer(15);
            this.SMALL_INTS = numArr;
        }

        public Long toLong(long l) {
            return (l < 0 || l >= ((long) this.SMALL_LONGS.length)) ? new Long(l) : this.SMALL_LONGS[(int) l];
        }

        public Integer toInteger(int i) {
            return (i < 0 || i >= this.SMALL_INTS.length) ? new Integer(i) : this.SMALL_INTS[i];
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.common.util.Primitives.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.common.util.Primitives.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.util.Primitives.<clinit>():void");
    }

    static void resetConverter() {
        converter = new J2meConverter();
    }

    public static Long toLong(long l) {
        return converter.toLong(l);
    }

    public static Integer toInteger(int i) {
        return converter.toInteger(i);
    }
}
