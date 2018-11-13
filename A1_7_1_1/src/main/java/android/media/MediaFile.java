package android.media;

import android.media.DecoderCapabilities.AudioDecoder;
import android.media.DecoderCapabilities.VideoDecoder;
import android.os.SystemProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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
public class MediaFile {
    public static final int FILE_TYPE_3GA = 193;
    public static final int FILE_TYPE_3GPP = 303;
    public static final int FILE_TYPE_3GPP2 = 304;
    public static final int FILE_TYPE_3GPP3 = 199;
    public static final int FILE_TYPE_AAC = 108;
    public static final int FILE_TYPE_AMR = 104;
    public static final int FILE_TYPE_APE = 111;
    public static final int FILE_TYPE_APK = 799;
    public static final int FILE_TYPE_ARW = 804;
    public static final int FILE_TYPE_ASF = 306;
    public static final int FILE_TYPE_AVI = 309;
    public static final int FILE_TYPE_AWB = 105;
    public static final int FILE_TYPE_BMP = 404;
    public static final int FILE_TYPE_CAF = 112;
    public static final int FILE_TYPE_CR2 = 801;
    public static final int FILE_TYPE_DNG = 800;
    public static final int FILE_TYPE_FL = 601;
    public static final int FILE_TYPE_FLA = 196;
    public static final int FILE_TYPE_FLAC = 110;
    public static final int FILE_TYPE_FLV = 398;
    public static final int FILE_TYPE_GIF = 402;
    public static final int FILE_TYPE_HTML = 701;
    public static final int FILE_TYPE_HTTPLIVE = 504;
    public static final int FILE_TYPE_ICS = 795;
    public static final int FILE_TYPE_ICZ = 796;
    public static final int FILE_TYPE_IMY = 203;
    public static final int FILE_TYPE_JPEG = 401;
    public static final int FILE_TYPE_M3U = 501;
    public static final int FILE_TYPE_M4A = 102;
    public static final int FILE_TYPE_M4V = 302;
    public static final int FILE_TYPE_MID = 201;
    public static final int FILE_TYPE_MKA = 109;
    public static final int FILE_TYPE_MKV = 307;
    public static final int FILE_TYPE_MP2 = 197;
    public static final int FILE_TYPE_MP2PS = 393;
    public static final int FILE_TYPE_MP2TS = 308;
    public static final int FILE_TYPE_MP3 = 101;
    public static final int FILE_TYPE_MP4 = 301;
    public static final int FILE_TYPE_MPO = 499;
    public static final int FILE_TYPE_MS_EXCEL = 705;
    public static final int FILE_TYPE_MS_POWERPOINT = 706;
    public static final int FILE_TYPE_MS_WORD = 704;
    public static final int FILE_TYPE_NEF = 802;
    public static final int FILE_TYPE_NRW = 803;
    public static final int FILE_TYPE_OGG = 107;
    public static final int FILE_TYPE_OGM = 394;
    public static final int FILE_TYPE_ORF = 806;
    public static final int FILE_TYPE_PDF = 702;
    public static final int FILE_TYPE_PEF = 808;
    public static final int FILE_TYPE_PLS = 502;
    public static final int FILE_TYPE_PNG = 403;
    public static final int FILE_TYPE_QT = 200;
    public static final int FILE_TYPE_QUICKTIME_AUDIO = 194;
    public static final int FILE_TYPE_QUICKTIME_VIDEO = 397;
    public static final int FILE_TYPE_RA = 198;
    public static final int FILE_TYPE_RAF = 807;
    public static final int FILE_TYPE_RM = 399;
    public static final int FILE_TYPE_RMVB = 396;
    public static final int FILE_TYPE_RV = 395;
    public static final int FILE_TYPE_RW2 = 805;
    public static final int FILE_TYPE_SMF = 202;
    public static final int FILE_TYPE_SRW = 809;
    public static final int FILE_TYPE_TEXT = 700;
    public static final int FILE_TYPE_VCF = 797;
    public static final int FILE_TYPE_VCS = 798;
    public static final int FILE_TYPE_WAV = 103;
    public static final int FILE_TYPE_WBMP = 405;
    public static final int FILE_TYPE_WEBM = 310;
    public static final int FILE_TYPE_WEBP = 406;
    public static final int FILE_TYPE_WMA = 106;
    public static final int FILE_TYPE_WMV = 305;
    public static final int FILE_TYPE_WPL = 503;
    public static final int FILE_TYPE_XML = 703;
    public static final int FILE_TYPE_ZIP = 707;
    private static final int FIRST_AUDIO_FILE_TYPE = 101;
    private static final int FIRST_DRM_FILE_TYPE = 601;
    private static final int FIRST_IMAGE_FILE_TYPE = 401;
    private static final int FIRST_MIDI_FILE_TYPE = 201;
    private static final int FIRST_PLAYLIST_FILE_TYPE = 501;
    private static final int FIRST_RAW_IMAGE_FILE_TYPE = 800;
    private static final int FIRST_VIDEO_FILE_TYPE = 301;
    private static final int FIRST_VIDEO_FILE_TYPE2 = 200;
    private static final int LAST_AUDIO_FILE_TYPE = 199;
    private static final int LAST_DRM_FILE_TYPE = 601;
    private static final int LAST_IMAGE_FILE_TYPE = 499;
    private static final int LAST_MIDI_FILE_TYPE = 203;
    private static final int LAST_PLAYLIST_FILE_TYPE = 504;
    private static final int LAST_RAW_IMAGE_FILE_TYPE = 809;
    private static final int LAST_VIDEO_FILE_TYPE = 399;
    private static final int LAST_VIDEO_FILE_TYPE2 = 200;
    private static final HashMap<String, MediaFileType> sFileTypeMap = null;
    private static final HashMap<String, Integer> sFileTypeToFormatMap = null;
    private static final HashMap<Integer, String> sFormatToMimeTypeMap = null;
    private static final HashMap<String, Integer> sMimeTypeMap = null;
    private static final HashMap<String, Integer> sMimeTypeToFormatMap = null;

    public static class MediaFileType {
        public final int fileType;
        public final String mimeType;

        MediaFileType(int fileType, String mimeType) {
            this.fileType = fileType;
            this.mimeType = mimeType;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.MediaFile.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.MediaFile.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaFile.<clinit>():void");
    }

    static void addFileType(String extension, int fileType, String mimeType) {
        sFileTypeMap.put(extension, new MediaFileType(fileType, mimeType));
        sMimeTypeMap.put(mimeType, Integer.valueOf(fileType));
    }

    static void addFileType(String extension, int fileType, String mimeType, int mtpFormatCode) {
        addFileType(extension, fileType, mimeType);
        sFileTypeToFormatMap.put(extension, Integer.valueOf(mtpFormatCode));
        sMimeTypeToFormatMap.put(mimeType, Integer.valueOf(mtpFormatCode));
        sFormatToMimeTypeMap.put(Integer.valueOf(mtpFormatCode), mimeType);
    }

    private static boolean isWMAEnabled() {
        if (!SystemProperties.getBoolean("ro.mtk_wmv_playback_support", false)) {
            return false;
        }
        List<AudioDecoder> decoders = DecoderCapabilities.getAudioDecoders();
        int count = decoders.size();
        for (int i = 0; i < count; i++) {
            if (((AudioDecoder) decoders.get(i)) == AudioDecoder.AUDIO_DECODER_WMA) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWMVEnabled() {
        if (!SystemProperties.getBoolean("ro.mtk_wmv_playback_support", false)) {
            return false;
        }
        List<VideoDecoder> decoders = DecoderCapabilities.getVideoDecoders();
        int count = decoders.size();
        for (int i = 0; i < count; i++) {
            if (((VideoDecoder) decoders.get(i)) == VideoDecoder.VIDEO_DECODER_WMV) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAudioFileType(int fileType) {
        if (fileType >= 101 && fileType <= 199) {
            return true;
        }
        if (fileType < 201) {
            return false;
        }
        if (fileType > 203) {
            return false;
        }
        return true;
    }

    public static boolean isVideoFileType(int fileType) {
        if (fileType >= 301 && fileType <= 399) {
            return true;
        }
        if (fileType < 200) {
            return false;
        }
        if (fileType > 200) {
            return false;
        }
        return true;
    }

    public static boolean isImageFileType(int fileType) {
        if (fileType >= 401 && fileType <= 499) {
            return true;
        }
        if (fileType < 800) {
            return false;
        }
        if (fileType > 809) {
            return false;
        }
        return true;
    }

    public static boolean isRawImageFileType(int fileType) {
        if (fileType < 800 || fileType > 809) {
            return false;
        }
        return true;
    }

    public static boolean isPlayListFileType(int fileType) {
        if (fileType < 501 || fileType > 504) {
            return false;
        }
        return true;
    }

    public static boolean isDrmFileType(int fileType) {
        if (fileType < FILE_TYPE_FL || fileType > FILE_TYPE_FL) {
            return false;
        }
        return true;
    }

    public static MediaFileType getFileType(String path) {
        int lastDot = path.lastIndexOf(46);
        if (lastDot < 0) {
            return null;
        }
        return (MediaFileType) sFileTypeMap.get(path.substring(lastDot + 1).toUpperCase(Locale.ROOT));
    }

    public static boolean isMimeTypeMedia(String mimeType) {
        int fileType = getFileTypeForMimeType(mimeType);
        if (isAudioFileType(fileType) || isVideoFileType(fileType) || isImageFileType(fileType)) {
            return true;
        }
        return isPlayListFileType(fileType);
    }

    public static String getFileTitle(String path) {
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash >= 0) {
            lastSlash++;
            if (lastSlash < path.length()) {
                path = path.substring(lastSlash);
            }
        }
        int lastDot = path.lastIndexOf(46);
        if (lastDot > 0) {
            return path.substring(0, lastDot);
        }
        return path;
    }

    public static int getFileTypeForMimeType(String mimeType) {
        Integer value = (Integer) sMimeTypeMap.get(mimeType);
        return value == null ? 0 : value.intValue();
    }

    public static String getMimeTypeForFile(String path) {
        MediaFileType mediaFileType = getFileType(path);
        if (mediaFileType == null) {
            return null;
        }
        return mediaFileType.mimeType;
    }

    public static int getFormatCode(String fileName, String mimeType) {
        Integer value;
        if (mimeType != null) {
            value = (Integer) sMimeTypeToFormatMap.get(mimeType);
            if (value != null) {
                return value.intValue();
            }
        }
        int lastDot = fileName.lastIndexOf(46);
        if (lastDot > 0) {
            value = (Integer) sFileTypeToFormatMap.get(fileName.substring(lastDot + 1).toUpperCase(Locale.ROOT));
            if (value != null) {
                return value.intValue();
            }
        }
        return 12288;
    }

    public static String getMimeTypeForFormatCode(int formatCode) {
        return (String) sFormatToMimeTypeMap.get(Integer.valueOf(formatCode));
    }
}
