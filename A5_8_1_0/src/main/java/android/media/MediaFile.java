package android.media;

import android.content.ClipDescription;
import android.media.DecoderCapabilities.AudioDecoder;
import android.media.DecoderCapabilities.VideoDecoder;
import android.mtp.MtpConstants;
import com.android.internal.util.Preconditions;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MediaFile {
    public static final int FILE_TYPE_3GPA = 211;
    public static final int FILE_TYPE_3GPP = 23;
    public static final int FILE_TYPE_3GPP2 = 24;
    public static final int FILE_TYPE_AAC = 8;
    public static final int FILE_TYPE_AC3 = 212;
    public static final int FILE_TYPE_AIFF = 216;
    public static final int FILE_TYPE_AMR = 4;
    public static final int FILE_TYPE_APE = 217;
    public static final int FILE_TYPE_ARW = 304;
    public static final int FILE_TYPE_ASF = 26;
    public static final int FILE_TYPE_AVI = 29;
    public static final int FILE_TYPE_AWB = 5;
    public static final int FILE_TYPE_BMP = 34;
    public static final int FILE_TYPE_CR2 = 301;
    public static final int FILE_TYPE_DIVX = 202;
    public static final int FILE_TYPE_DNG = 300;
    public static final int FILE_TYPE_DSD = 218;
    public static final int FILE_TYPE_DTS = 210;
    public static final int FILE_TYPE_EC3 = 215;
    public static final int FILE_TYPE_FL = 51;
    public static final int FILE_TYPE_FLAC = 10;
    public static final int FILE_TYPE_FLV = 203;
    public static final int FILE_TYPE_GIF = 32;
    public static final int FILE_TYPE_HEIF = 37;
    public static final int FILE_TYPE_HTML = 101;
    public static final int FILE_TYPE_HTTPLIVE = 44;
    public static final int FILE_TYPE_IMY = 13;
    public static final int FILE_TYPE_JPEG = 31;
    public static final int FILE_TYPE_M3U = 41;
    public static final int FILE_TYPE_M4A = 2;
    public static final int FILE_TYPE_M4V = 22;
    public static final int FILE_TYPE_MID = 11;
    public static final int FILE_TYPE_MKA = 9;
    public static final int FILE_TYPE_MKV = 27;
    public static final int FILE_TYPE_MP2PS = 200;
    public static final int FILE_TYPE_MP2TS = 28;
    public static final int FILE_TYPE_MP3 = 1;
    public static final int FILE_TYPE_MP4 = 21;
    public static final int FILE_TYPE_MS_EXCEL = 105;
    public static final int FILE_TYPE_MS_POWERPOINT = 106;
    public static final int FILE_TYPE_MS_WORD = 104;
    public static final int FILE_TYPE_NEF = 302;
    public static final int FILE_TYPE_NRW = 303;
    public static final int FILE_TYPE_OGG = 7;
    public static final int FILE_TYPE_ORF = 306;
    public static final int FILE_TYPE_PCM = 214;
    public static final int FILE_TYPE_PDF = 102;
    public static final int FILE_TYPE_PEF = 308;
    public static final int FILE_TYPE_PLS = 42;
    public static final int FILE_TYPE_PNG = 33;
    public static final int FILE_TYPE_QCP = 213;
    public static final int FILE_TYPE_QT = 201;
    public static final int FILE_TYPE_RAF = 307;
    public static final int FILE_TYPE_RW2 = 305;
    public static final int FILE_TYPE_SMF = 12;
    public static final int FILE_TYPE_SRW = 309;
    public static final int FILE_TYPE_TEXT = 100;
    public static final int FILE_TYPE_WAV = 3;
    public static final int FILE_TYPE_WBMP = 35;
    public static final int FILE_TYPE_WEBM = 30;
    public static final int FILE_TYPE_WEBP = 36;
    public static final int FILE_TYPE_WMA = 6;
    public static final int FILE_TYPE_WMV = 25;
    public static final int FILE_TYPE_WPL = 43;
    public static final int FILE_TYPE_XML = 103;
    public static final int FILE_TYPE_ZIP = 107;
    private static final int FIRST_AUDIO_FILE_TYPE = 1;
    private static final int FIRST_AUDIO_FILE_TYPE_EXT = 210;
    private static final int FIRST_DRM_FILE_TYPE = 51;
    private static final int FIRST_IMAGE_FILE_TYPE = 31;
    private static final int FIRST_MIDI_FILE_TYPE = 11;
    private static final int FIRST_PLAYLIST_FILE_TYPE = 41;
    private static final int FIRST_RAW_IMAGE_FILE_TYPE = 300;
    private static final int FIRST_VIDEO_FILE_TYPE = 21;
    private static final int FIRST_VIDEO_FILE_TYPE2 = 200;
    private static final int LAST_AUDIO_FILE_TYPE = 10;
    private static final int LAST_AUDIO_FILE_TYPE_EXT = 218;
    private static final int LAST_DRM_FILE_TYPE = 51;
    private static final int LAST_IMAGE_FILE_TYPE = 37;
    private static final int LAST_MIDI_FILE_TYPE = 13;
    private static final int LAST_PLAYLIST_FILE_TYPE = 44;
    private static final int LAST_RAW_IMAGE_FILE_TYPE = 309;
    private static final int LAST_VIDEO_FILE_TYPE = 30;
    private static final int LAST_VIDEO_FILE_TYPE2 = 203;
    private static final HashMap<String, MediaFileType> sFileTypeMap = new HashMap();
    private static final HashMap<String, Integer> sFileTypeToFormatMap = new HashMap();
    private static final HashMap<Integer, String> sFormatToMimeTypeMap = new HashMap();
    private static final HashMap<String, Integer> sMimeTypeMap = new HashMap();
    private static final HashMap<String, Integer> sMimeTypeToFormatMap = new HashMap();

    public static class MediaFileType {
        public final int fileType;
        public final String mimeType;

        MediaFileType(int fileType, String mimeType) {
            this.fileType = fileType;
            this.mimeType = mimeType;
        }
    }

    static {
        addFileType("MP3", 1, MediaFormat.MIMETYPE_AUDIO_MPEG, 12297, true);
        addFileType("MPGA", 1, MediaFormat.MIMETYPE_AUDIO_MPEG, 12297, false);
        addFileType("M4A", 2, "audio/mp4", 12299, false);
        addFileType("WAV", 3, "audio/x-wav", 12296, true);
        addFileType("AMR", 4, "audio/amr");
        addFileType("AWB", 5, MediaFormat.MIMETYPE_AUDIO_AMR_WB);
        if (isWMAEnabled()) {
            addFileType("WMA", 6, "audio/x-ms-wma", MtpConstants.FORMAT_WMA, true);
        }
        addFileType("OGG", 7, "audio/ogg", MtpConstants.FORMAT_OGG, false);
        addFileType("OGG", 7, "application/ogg", MtpConstants.FORMAT_OGG, true);
        addFileType("OGA", 7, "application/ogg", MtpConstants.FORMAT_OGG, false);
        addFileType("AAC", 8, "audio/aac", MtpConstants.FORMAT_AAC, true);
        addFileType("AAC", 8, "audio/aac-adts", MtpConstants.FORMAT_AAC, false);
        addFileType("MKA", 9, "audio/x-matroska");
        addFileType("MID", 11, "audio/midi");
        addFileType("MIDI", 11, "audio/midi");
        addFileType("XMF", 11, "audio/midi");
        addFileType("RTTTL", 11, "audio/midi");
        addFileType("SMF", 12, "audio/sp-midi");
        addFileType("IMY", 13, "audio/imelody");
        addFileType("RTX", 11, "audio/midi");
        addFileType("OTA", 11, "audio/midi");
        addFileType("MXMF", 11, "audio/midi");
        addFileType("MPEG", 21, "video/mpeg", 12299, true);
        addFileType("MPG", 21, "video/mpeg", 12299, false);
        addFileType("MP4", 21, "video/mp4", 12299, false);
        addFileType("M4V", 22, "video/mp4", 12299, false);
        addFileType("MOV", 201, "video/quicktime", 12299, false);
        addFileType("3GP", 23, MediaFormat.MIMETYPE_VIDEO_H263, MtpConstants.FORMAT_3GP_CONTAINER, true);
        addFileType("3GPP", 23, MediaFormat.MIMETYPE_VIDEO_H263, MtpConstants.FORMAT_3GP_CONTAINER, false);
        addFileType("3G2", 24, "video/3gpp2", MtpConstants.FORMAT_3GP_CONTAINER, false);
        addFileType("3GPP2", 24, "video/3gpp2", MtpConstants.FORMAT_3GP_CONTAINER, false);
        addFileType("MKV", 27, "video/x-matroska");
        addFileType("WEBM", 30, "video/webm");
        addFileType("TS", 28, "video/mp2ts");
        addFileType("AVI", 29, "video/avi");
        if (isWMVEnabled()) {
            addFileType("WMV", 25, "video/x-ms-wmv", MtpConstants.FORMAT_WMV, true);
            addFileType("ASF", 26, "video/x-ms-asf");
        }
        addFileType("JPG", 31, "image/jpeg", MtpConstants.FORMAT_EXIF_JPEG, true);
        addFileType("JPEG", 31, "image/jpeg", MtpConstants.FORMAT_EXIF_JPEG, false);
        addFileType("GIF", 32, "image/gif", MtpConstants.FORMAT_GIF, true);
        addFileType("PNG", 33, "image/png", MtpConstants.FORMAT_PNG, true);
        addFileType("BMP", 34, "image/x-ms-bmp", MtpConstants.FORMAT_BMP, true);
        addFileType("WBMP", 35, "image/vnd.wap.wbmp", MtpConstants.FORMAT_DEFINED, false);
        addFileType("WEBP", 36, "image/webp", MtpConstants.FORMAT_DEFINED, false);
        addFileType("HEIC", 37, "image/heif", MtpConstants.FORMAT_HEIF, true);
        addFileType("HEIF", 37, "image/heif", MtpConstants.FORMAT_HEIF, false);
        addFileType("DNG", 300, "image/x-adobe-dng", MtpConstants.FORMAT_DNG, true);
        addFileType("CR2", FILE_TYPE_CR2, "image/x-canon-cr2", MtpConstants.FORMAT_TIFF, false);
        addFileType("NEF", FILE_TYPE_NEF, "image/x-nikon-nef", MtpConstants.FORMAT_TIFF_EP, false);
        addFileType("NRW", FILE_TYPE_NRW, "image/x-nikon-nrw", MtpConstants.FORMAT_TIFF, false);
        addFileType("ARW", FILE_TYPE_ARW, "image/x-sony-arw", MtpConstants.FORMAT_TIFF, false);
        addFileType("RW2", FILE_TYPE_RW2, "image/x-panasonic-rw2", MtpConstants.FORMAT_TIFF, false);
        addFileType("ORF", FILE_TYPE_ORF, "image/x-olympus-orf", MtpConstants.FORMAT_TIFF, false);
        addFileType("RAF", FILE_TYPE_RAF, "image/x-fuji-raf", MtpConstants.FORMAT_DEFINED, false);
        addFileType("PEF", FILE_TYPE_PEF, "image/x-pentax-pef", MtpConstants.FORMAT_TIFF, false);
        addFileType("SRW", 309, "image/x-samsung-srw", MtpConstants.FORMAT_TIFF, false);
        addFileType("M3U", 41, "audio/x-mpegurl", MtpConstants.FORMAT_M3U_PLAYLIST, true);
        addFileType("M3U", 41, "application/x-mpegurl", MtpConstants.FORMAT_M3U_PLAYLIST, false);
        addFileType("PLS", 42, "audio/x-scpls", MtpConstants.FORMAT_PLS_PLAYLIST, true);
        addFileType("WPL", 43, "application/vnd.ms-wpl", MtpConstants.FORMAT_WPL_PLAYLIST, true);
        addFileType("M3U8", 44, "application/vnd.apple.mpegurl");
        addFileType("M3U8", 44, "audio/mpegurl");
        addFileType("M3U8", 44, "audio/x-mpegurl");
        addFileType("FL", 51, "application/x-android-drm-fl");
        addFileType("TXT", 100, ClipDescription.MIMETYPE_TEXT_PLAIN, 12292, true);
        addFileType("HTM", 101, ClipDescription.MIMETYPE_TEXT_HTML, 12293, true);
        addFileType("HTML", 101, ClipDescription.MIMETYPE_TEXT_HTML, 12293, false);
        addFileType("PDF", 102, "application/pdf");
        addFileType("DOC", 104, "application/msword", MtpConstants.FORMAT_MS_WORD_DOCUMENT, true);
        addFileType("XLS", 105, "application/vnd.ms-excel", MtpConstants.FORMAT_MS_EXCEL_SPREADSHEET, true);
        addFileType("PPT", 106, "application/mspowerpoint", MtpConstants.FORMAT_MS_POWERPOINT_PRESENTATION, true);
        addFileType("FLAC", 10, MediaFormat.MIMETYPE_AUDIO_FLAC, MtpConstants.FORMAT_FLAC, true);
        addFileType("ZIP", 107, "application/zip");
        addFileType("MPG", 200, "video/mp2p");
        addFileType("MPEG", 200, "video/mp2p");
        addFileType("DIVX", 202, "video/divx");
        addFileType("FLV", 203, "video/flv");
        addFileType("QCP", 213, MediaFormat.MIMETYPE_AUDIO_QCELP);
        addFileType("AC3", 212, MediaFormat.MIMETYPE_AUDIO_AC3);
        addFileType("EC3", 215, MediaFormat.MIMETYPE_AUDIO_EAC3);
        addFileType("AIF", 216, "audio/x-aiff");
        addFileType("AIFF", 216, "audio/x-aiff");
        addFileType("APE", 217, "audio/x-ape");
        addFileType("DSF", 218, "audio/x-dsf");
        addFileType("DFF", 218, "audio/x-dff");
        addFileType("DSD", 218, "audio/dsd");
    }

    static void addFileType(String extension, int fileType, String mimeType) {
        sFileTypeMap.put(extension, new MediaFileType(fileType, mimeType));
        sMimeTypeMap.put(mimeType, Integer.valueOf(fileType));
    }

    private static void addFileType(String extension, int fileType, String mimeType, int mtpFormatCode, boolean primaryType) {
        addFileType(extension, fileType, mimeType);
        sFileTypeToFormatMap.put(extension, Integer.valueOf(mtpFormatCode));
        sMimeTypeToFormatMap.put(mimeType, Integer.valueOf(mtpFormatCode));
        if (primaryType) {
            Preconditions.checkArgument(sFormatToMimeTypeMap.containsKey(Integer.valueOf(mtpFormatCode)) ^ 1);
            sFormatToMimeTypeMap.put(Integer.valueOf(mtpFormatCode), mimeType);
        }
    }

    private static boolean isWMAEnabled() {
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
        if (fileType >= 1 && fileType <= 10) {
            return true;
        }
        if (fileType >= 11 && fileType <= 13) {
            return true;
        }
        if (fileType < 210) {
            return false;
        }
        if (fileType > 218) {
            return false;
        }
        return true;
    }

    public static boolean isVideoFileType(int fileType) {
        if (fileType >= 21 && fileType <= 30) {
            return true;
        }
        if (fileType < 200) {
            return false;
        }
        if (fileType > 203) {
            return false;
        }
        return true;
    }

    public static boolean isImageFileType(int fileType) {
        if (fileType >= 31 && fileType <= 37) {
            return true;
        }
        if (fileType < 300) {
            return false;
        }
        if (fileType > 309) {
            return false;
        }
        return true;
    }

    public static boolean isRawImageFileType(int fileType) {
        if (fileType < 300 || fileType > 309) {
            return false;
        }
        return true;
    }

    public static boolean isPlayListFileType(int fileType) {
        if (fileType < 41 || fileType > 44) {
            return false;
        }
        return true;
    }

    public static boolean isDrmFileType(int fileType) {
        if (fileType < 51 || fileType > 51) {
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
