package android.media;

import java.util.ArrayList;
import java.util.List;

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
public class EncoderCapabilities {
    private static final String TAG = "EncoderCapabilities";

    public static class AudioEncoderCap {
        public final int mCodec;
        public final int mMaxBitRate;
        public final int mMaxChannels;
        public final int mMaxSampleRate;
        public final int mMinBitRate;
        public final int mMinChannels;
        public final int mMinSampleRate;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.media.EncoderCapabilities.AudioEncoderCap.<init>(int, int, int, int, int, int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private AudioEncoderCap(int r1, int r2, int r3, int r4, int r5, int r6, int r7) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.media.EncoderCapabilities.AudioEncoderCap.<init>(int, int, int, int, int, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.EncoderCapabilities.AudioEncoderCap.<init>(int, int, int, int, int, int, int):void");
        }
    }

    public static class VideoEncoderCap {
        public final int mCodec;
        public final int mMaxBitRate;
        public final int mMaxFrameHeight;
        public final int mMaxFrameRate;
        public final int mMaxFrameWidth;
        public final int mMinBitRate;
        public final int mMinFrameHeight;
        public final int mMinFrameRate;
        public final int mMinFrameWidth;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.media.EncoderCapabilities.VideoEncoderCap.<init>(int, int, int, int, int, int, int, int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private VideoEncoderCap(int r1, int r2, int r3, int r4, int r5, int r6, int r7, int r8, int r9) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.media.EncoderCapabilities.VideoEncoderCap.<init>(int, int, int, int, int, int, int, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.EncoderCapabilities.VideoEncoderCap.<init>(int, int, int, int, int, int, int, int, int):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.EncoderCapabilities.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.EncoderCapabilities.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.EncoderCapabilities.<clinit>():void");
    }

    private static final native AudioEncoderCap native_get_audio_encoder_cap(int i);

    private static final native int native_get_file_format(int i);

    private static final native int native_get_num_audio_encoders();

    private static final native int native_get_num_file_formats();

    private static final native int native_get_num_video_encoders();

    private static final native VideoEncoderCap native_get_video_encoder_cap(int i);

    private static final native void native_init();

    public static int[] getOutputFileFormats() {
        int nFormats = native_get_num_file_formats();
        if (nFormats == 0) {
            return null;
        }
        int[] formats = new int[nFormats];
        for (int i = 0; i < nFormats; i++) {
            formats[i] = native_get_file_format(i);
        }
        return formats;
    }

    public static List<VideoEncoderCap> getVideoEncoders() {
        int nEncoders = native_get_num_video_encoders();
        if (nEncoders == 0) {
            return null;
        }
        List<VideoEncoderCap> encoderList = new ArrayList();
        for (int i = 0; i < nEncoders; i++) {
            encoderList.add(native_get_video_encoder_cap(i));
        }
        return encoderList;
    }

    public static List<AudioEncoderCap> getAudioEncoders() {
        int nEncoders = native_get_num_audio_encoders();
        if (nEncoders == 0) {
            return null;
        }
        List<AudioEncoderCap> encoderList = new ArrayList();
        for (int i = 0; i < nEncoders; i++) {
            encoderList.add(native_get_audio_encoder_cap(i));
        }
        return encoderList;
    }

    private EncoderCapabilities() {
    }
}
