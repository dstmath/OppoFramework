package android_maps_conflict_avoidance.com.google.image.compression.jpeg;

import com.google.android.maps.MapView.LayoutParams;

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
public class JpegUtil {
    private static final byte[][] JPEG_QUANT_TABLES = null;
    private static final int[] imageIoScaleFactor = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.image.compression.jpeg.JpegUtil.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.image.compression.jpeg.JpegUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.image.compression.jpeg.JpegUtil.<clinit>():void");
    }

    public static byte getScaledQuantizationFactor(int q, int quality, int qualityAlgorithm) {
        int val;
        switch (qualityAlgorithm) {
            case LayoutParams.MODE_MAP /*0*/:
                if (q != 99 || quality != 36) {
                    val = (int) ((((((long) q) * ((long) imageIoScaleFactor[quality])) / 16777216) + 1) / 2);
                    break;
                }
                val = 138;
                break;
            case 1:
                int iscale;
                if (quality < 50) {
                    iscale = Math.min(5000 / quality, 5000);
                } else {
                    iscale = Math.max(200 - (quality * 2), 0);
                }
                val = ((q * iscale) + 50) / 100;
                break;
            default:
                throw new IllegalArgumentException("qualityAlgorithm");
        }
        if (val < 1) {
            val = 1;
        } else if (val > 255) {
            val = 255;
        }
        return (byte) val;
    }

    public static synchronized byte[] getQuantTable(int quantType, int quality, int qualityAlgorithm) {
        byte[] qtable;
        synchronized (JpegUtil.class) {
            int index = ((quantType * 154) + (qualityAlgorithm * 77)) + (quality - 24);
            qtable = new byte[64];
            byte[] rawTable = JPEG_QUANT_TABLES[quantType];
            for (int j = 0; j < 64; j++) {
                qtable[j] = getScaledQuantizationFactor(rawTable[j] & 255, quality, qualityAlgorithm);
            }
        }
        return qtable;
    }

    static void prependStandardHeader(byte[] src, int soff, int len, byte[] dst, int doff, JpegHeaderParams params) {
        int variant = params.getVariant();
        int width = params.getWidth();
        int height = params.getHeight();
        int quality = params.getQuality();
        int qualityAlgorithm = params.getQualityAlgorithm();
        if (variant != 0) {
            throw new IllegalArgumentException("variant");
        }
        System.arraycopy(src, soff, dst, doff + GenerateJpegHeader.getHeaderLength(variant), len);
        GenerateJpegHeader.generate(dst, doff, variant, width, height, quality, qualityAlgorithm);
    }

    public static byte[] uncompactJpeg(byte[] compactJpegData, int off, int len) {
        if (compactJpegData[off] == (byte) -1 && compactJpegData[off + 1] == (byte) -40) {
            Object data = new byte[len];
            System.arraycopy(compactJpegData, off, data, 0, len);
            return data;
        } else if (compactJpegData[off] == (byte) 67 && compactJpegData[off + 1] == (byte) 74 && compactJpegData[off + 2] == (byte) 80 && compactJpegData[off + 3] == (byte) 71) {
            int variant = compactJpegData[off + 4] & 255;
            int width = ((compactJpegData[off + 5] & 255) << 8) | (compactJpegData[off + 6] & 255);
            int height = ((compactJpegData[off + 7] & 255) << 8) | (compactJpegData[off + 8] & 255);
            int quality = compactJpegData[off + 9] & 255;
            int qualityAlgorithm = compactJpegData[off + 10] & 255;
            try {
                int hlen = GenerateJpegHeader.getHeaderLength(variant);
                byte[] jpegData = new byte[((hlen + len) - 11)];
                prependStandardHeader(compactJpegData, off + 11, len - 11, jpegData, 0, new JpegHeaderParams(variant, width, height, quality, qualityAlgorithm, hlen));
                return jpegData;
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown variant " + variant);
            }
        } else {
            throw new IllegalArgumentException("Input is not in compact JPEG format");
        }
    }

    public static byte[] uncompactJpeg(byte[] compactJpegData) {
        return uncompactJpeg(compactJpegData, 0, compactJpegData.length);
    }
}
