package android_maps_conflict_avoidance.com.google.image.compression.jpeg;

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
public class GenerateJpegHeader {
    private static final byte[] JPEG_STANDARD_HEADER = null;
    private static int JPEG_STANDARD_HEADER_CHROMINANCE_QUANT_OFFSET;
    private static int JPEG_STANDARD_HEADER_LUMINANCE_QUANT_OFFSET;
    private static int JPEG_STANDARD_HEADER_Y_X_OFFSET;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.image.compression.jpeg.GenerateJpegHeader.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.image.compression.jpeg.GenerateJpegHeader.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.image.compression.jpeg.GenerateJpegHeader.<clinit>():void");
    }

    public static int getHeaderLength(int variant) {
        if (variant == 0) {
            return JPEG_STANDARD_HEADER.length;
        }
        throw new IllegalArgumentException("Unknown variant " + variant);
    }

    private static void copyQuantTable(byte[] dest, int off, int quantType, int quality, int qualityAlgorithm) {
        byte[] qtable = JpegUtil.getQuantTable(quantType, quality, qualityAlgorithm);
        System.arraycopy(qtable, 0, dest, off, qtable.length);
    }

    public static int generate(byte[] dest, int off, int variant, int width, int height, int quality, int qualityAlgorithm) {
        if (variant != 0) {
            throw new IllegalArgumentException("variant");
        } else if (quality < 24 || quality > 100) {
            throw new IllegalArgumentException("quality");
        } else if (qualityAlgorithm == 0 || qualityAlgorithm == 1) {
            int len = JPEG_STANDARD_HEADER.length;
            if (off + len > dest.length) {
                throw new ArrayIndexOutOfBoundsException("dest");
            }
            System.arraycopy(JPEG_STANDARD_HEADER, 0, dest, off, len);
            int yxOffset = off + JPEG_STANDARD_HEADER_Y_X_OFFSET;
            dest[yxOffset] = (byte) (width >> 8);
            dest[yxOffset + 1] = (byte) (width & 255);
            dest[yxOffset + 2] = (byte) (height >> 8);
            dest[yxOffset + 3] = (byte) (height & 255);
            if (quality != 75) {
                int cOff = off + JPEG_STANDARD_HEADER_CHROMINANCE_QUANT_OFFSET;
                copyQuantTable(dest, off + JPEG_STANDARD_HEADER_LUMINANCE_QUANT_OFFSET, 0, quality, qualityAlgorithm);
                copyQuantTable(dest, cOff, 1, quality, qualityAlgorithm);
            }
            return len;
        } else {
            throw new IllegalArgumentException("qualityAlgorithm: " + qualityAlgorithm);
        }
    }
}
