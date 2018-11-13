package android.media;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import java.util.Arrays;
import java.util.HashMap;

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
public class CameraProfile {
    public static final int QUALITY_HIGH = 2;
    public static final int QUALITY_LOW = 0;
    public static final int QUALITY_MEDIUM = 1;
    private static final HashMap<Integer, int[]> sCache = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.CameraProfile.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.CameraProfile.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.CameraProfile.<clinit>():void");
    }

    private static final native int native_get_image_encoding_quality_level(int i, int i2);

    private static final native int native_get_num_image_encoding_quality_levels(int i);

    private static final native void native_init();

    public static int getJpegEncodingQualityParameter(int quality) {
        int numberOfCameras = Camera.getNumberOfCameras();
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == 0) {
                return getJpegEncodingQualityParameter(i, quality);
            }
        }
        return 0;
    }

    public static int getJpegEncodingQualityParameter(int cameraId, int quality) {
        if (quality < 0 || quality > 2) {
            throw new IllegalArgumentException("Unsupported quality level: " + quality);
        }
        int i;
        synchronized (sCache) {
            int[] levels = (int[]) sCache.get(Integer.valueOf(cameraId));
            if (levels == null) {
                levels = getImageEncodingQualityLevels(cameraId);
                sCache.put(Integer.valueOf(cameraId), levels);
            }
            i = levels[quality];
        }
        return i;
    }

    private static int[] getImageEncodingQualityLevels(int cameraId) {
        int nLevels = native_get_num_image_encoding_quality_levels(cameraId);
        if (nLevels != 3) {
            throw new RuntimeException("Unexpected Jpeg encoding quality levels " + nLevels);
        }
        int[] levels = new int[nLevels];
        for (int i = 0; i < nLevels; i++) {
            levels[i] = native_get_image_encoding_quality_level(cameraId, i);
        }
        Arrays.sort(levels);
        return levels;
    }
}
