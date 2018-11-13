package android_maps_conflict_avoidance.com.google.map;

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
public final class Zoom {
    private static int minZoomLevel;
    private static final Zoom[] zoomArray = null;
    private final int equatorPixels;
    private final int zoomLevel;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.map.Zoom.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.map.Zoom.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.map.Zoom.<clinit>():void");
    }

    private Zoom(int zoomLevel, int equatorPixels) {
        this.zoomLevel = zoomLevel;
        this.equatorPixels = equatorPixels;
    }

    public int getPixelsForDistance(int meters) {
        return Math.max((int) ((((long) meters) * ((long) this.equatorPixels)) / 40076000), 1);
    }

    public static Zoom getZoom(int zoomLevel) {
        if (zoomLevel < minZoomLevel || zoomLevel > 22) {
            return null;
        }
        return zoomArray[zoomLevel - 1];
    }

    public int getZoomRatio(Zoom zoomIn) {
        return zoomIn.equatorPixels / this.equatorPixels;
    }

    public int getZoomLevel() {
        return this.zoomLevel;
    }

    public int getEquatorPixels() {
        return this.equatorPixels;
    }

    public boolean isMoreZoomedIn(Zoom zoom) {
        return this.zoomLevel > zoom.zoomLevel;
    }

    public Zoom getNextHigherZoom() {
        return getZoom(this.zoomLevel + 1);
    }

    public int changePixelsToTargetZoomlevel(int pixels, int zoomTargetLevel) {
        if (this.zoomLevel < zoomTargetLevel) {
            return pixels << (zoomTargetLevel - this.zoomLevel);
        }
        return pixels >> (this.zoomLevel - zoomTargetLevel);
    }

    public Zoom getNextLowerZoom() {
        return getZoom(this.zoomLevel - 1);
    }

    public String toString() {
        return super.toString();
    }
}
