package com.oppo.media;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.Context;
import android.media.DecoderCapabilities;
import android.media.DecoderCapabilities.AudioDecoder;
import android.media.DecoderCapabilities.VideoDecoder;
import android.net.Uri;
import android.provider.Settings.System;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.microedition.khronos.egl.EGL10;

public class MediaFile {
    public static final int FILE_TYPE_3GPP = 23;
    public static final int FILE_TYPE_3GPP2 = 24;
    public static final int FILE_TYPE_AAC = 8;
    public static final int FILE_TYPE_AMR = 4;
    public static final int FILE_TYPE_APE = 1001;
    public static final int FILE_TYPE_APK = 10011;
    public static final int FILE_TYPE_ARW = 304;
    public static final int FILE_TYPE_ASF = 26;
    public static final int FILE_TYPE_AVI = 29;
    public static final int FILE_TYPE_AWB = 5;
    public static final int FILE_TYPE_BMP = 34;
    public static final int FILE_TYPE_CHM = 10021;
    public static final int FILE_TYPE_CR2 = 301;
    public static final int FILE_TYPE_CSV = 10022;
    public static final int FILE_TYPE_CUE = 1003;
    public static final int FILE_TYPE_DNG = 300;
    public static final int FILE_TYPE_EBK = 10026;
    public static final int FILE_TYPE_EPUB = 10027;
    public static final int FILE_TYPE_FL = 51;
    public static final int FILE_TYPE_FLAC = 10;
    public static final int FILE_TYPE_FLV = 1101;
    public static final int FILE_TYPE_GIF = 32;
    public static final int FILE_TYPE_HEIF = 37;
    public static final int FILE_TYPE_HTML = 101;
    public static final int FILE_TYPE_HTTPLIVE = 44;
    public static final int FILE_TYPE_ICS = 10023;
    public static final int FILE_TYPE_IMY = 13;
    public static final int FILE_TYPE_JAR = 10002;
    public static final int FILE_TYPE_JPEG = 31;
    public static final int FILE_TYPE_M2TS = 1104;
    public static final int FILE_TYPE_M3U = 41;
    public static final int FILE_TYPE_M4A = 2;
    public static final int FILE_TYPE_M4V = 22;
    public static final int FILE_TYPE_MID = 11;
    public static final int FILE_TYPE_MKA = 9;
    public static final int FILE_TYPE_MKV = 27;
    public static final int FILE_TYPE_MOV = 1103;
    public static final int FILE_TYPE_MP2 = 1002;
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
    public static final int FILE_TYPE_PDF = 102;
    public static final int FILE_TYPE_PEF = 308;
    public static final int FILE_TYPE_PLS = 42;
    public static final int FILE_TYPE_PNG = 33;
    public static final int FILE_TYPE_QT = 201;
    public static final int FILE_TYPE_RA = 1004;
    public static final int FILE_TYPE_RAF = 307;
    public static final int FILE_TYPE_RAR = 10001;
    public static final int FILE_TYPE_RV = 1102;
    public static final int FILE_TYPE_RW2 = 305;
    public static final int FILE_TYPE_SMF = 12;
    public static final int FILE_TYPE_SRW = 309;
    public static final int FILE_TYPE_TEXT = 100;
    public static final int FILE_TYPE_VCF = 10024;
    public static final int FILE_TYPE_VCS = 10025;
    public static final int FILE_TYPE_WAV = 3;
    public static final int FILE_TYPE_WBMP = 35;
    public static final int FILE_TYPE_WEBM = 30;
    public static final int FILE_TYPE_WEBP = 36;
    public static final int FILE_TYPE_WMA = 6;
    public static final int FILE_TYPE_WMV = 25;
    public static final int FILE_TYPE_WPL = 43;
    public static final int FILE_TYPE_XML = 103;
    public static final int FILE_TYPE_ZIP = 107;
    private static final int FIRST_APK_FILE_TYPE = 10011;
    private static final int FIRST_AUDIO_FILE_TYPE = 1;
    private static final int FIRST_COMPRESS_FILE_TYPE = 10001;
    private static final int FIRST_DOC_FILE_TYPE = 10021;
    private static final int FIRST_DRM_FILE_TYPE = 51;
    private static final int FIRST_FFMPEG_AUDIO_FILE_TYPE = 1001;
    private static final int FIRST_FFMPEG_VIDEO_FILE_TYPE = 1101;
    private static final int FIRST_IMAGE_FILE_TYPE = 31;
    private static final int FIRST_MIDI_FILE_TYPE = 11;
    private static final int FIRST_PLAYLIST_FILE_TYPE = 41;
    private static final int FIRST_POPULAR_DOC_FILE_TYPE = 100;
    private static final int FIRST_RAW_IMAGE_FILE_TYPE = 300;
    private static final int FIRST_VIDEO_FILE_TYPE = 21;
    private static final int FIRST_VIDEO_FILE_TYPE2 = 200;
    private static final int LAST_APK_FILE_TYPE = 10011;
    private static final int LAST_AUDIO_FILE_TYPE = 10;
    private static final int LAST_COMPRESS_FILE_TYPE = 10002;
    private static final int LAST_DOC_FILE_TYPE = 10027;
    private static final int LAST_DRM_FILE_TYPE = 51;
    private static final int LAST_FFMPEG_AUDIO_FILE_TYPE = 1004;
    private static final int LAST_FFMPEG_VIDEO_FILE_TYPE = 1104;
    private static final int LAST_IMAGE_FILE_TYPE = 37;
    private static final int LAST_MIDI_FILE_TYPE = 13;
    private static final int LAST_PLAYLIST_FILE_TYPE = 44;
    private static final int LAST_POPULAR_DOC_FILE_TYPE = 106;
    private static final int LAST_RAW_IMAGE_FILE_TYPE = 309;
    private static final int LAST_VIDEO_FILE_TYPE = 30;
    private static final int LAST_VIDEO_FILE_TYPE2 = 201;
    public static final int MEDIA_TYPE_APK = 10002;
    public static final int MEDIA_TYPE_COMPRESS = 10001;
    public static final int MEDIA_TYPE_DOC = 10003;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "oppo default uri name", property = OppoRomType.OPPO)
    public static final String OPPO_DEFAULT_ALARM = "oppo_default_alarm";
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "oppo default uri name", property = OppoRomType.OPPO)
    public static final String OPPO_DEFAULT_NOTIFICATION = "oppo_default_notification";
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "oppo default uri name", property = OppoRomType.OPPO)
    public static final String OPPO_DEFAULT_NOTIFICATION_SIM2 = "oppo_default_notification_sim2";
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "oppo default uri name", property = OppoRomType.OPPO)
    public static final String OPPO_DEFAULT_RINGTONE = "oppo_default_ringtone";
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "oppo default uri name", property = OppoRomType.OPPO)
    public static final String OPPO_DEFAULT_RINGTONE_SIM2 = "oppo_default_ringtone_sim2";
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "oppo default sms notification name", property = OppoRomType.OPPO)
    public static final String OPPO_DEFAULT_SMS_NOTIFICATION = "oppo_default_sms_notification_sound";
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "flag for scanning all files on storage", property = OppoRomType.OPPO)
    public static final int SCAN_ALL_FILE = 0;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "flag for scanning audio files on storage", property = OppoRomType.OPPO)
    public static final int SCAN_AUDIO_FILE = 1;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "flag for scanning image files on storage", property = OppoRomType.OPPO)
    public static final int SCAN_IMAGE_FILE = 2;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "flag for scanning other files on storage like *.apk, *.csv, *.vcf, *.ics, *.vcs", property = OppoRomType.OPPO)
    public static final int SCAN_OTHER_FILE = 8;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "flag for scanning video files on storage", property = OppoRomType.OPPO)
    public static final int SCAN_VIDEO_FILE = 4;
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
        addFileType("MP3", 1, "audio/mpeg", EGL10.EGL_BAD_MATCH, true);
        addFileType("MPGA", 1, "audio/mpeg", EGL10.EGL_BAD_MATCH, false);
        addFileType("M4A", 2, "audio/mp4", EGL10.EGL_BAD_NATIVE_WINDOW, false);
        addFileType("WAV", 3, "audio/x-wav", EGL10.EGL_BAD_DISPLAY, true);
        addFileType("AMR", 4, "audio/amr");
        addFileType("AWB", 5, "audio/amr-wb");
        if (isWMAEnabled()) {
            addFileType("WMA", 6, "audio/x-ms-wma", 47361, true);
        }
        addFileType("OGG", 7, "audio/ogg", 47362, false);
        addFileType("OGG", 7, "application/ogg", 47362, true);
        addFileType("OGA", 7, "application/ogg", 47362, false);
        addFileType("AAC", 8, "audio/aac", 47363, true);
        addFileType("AAC", 8, "audio/aac-adts", 47363, false);
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
        addFileType("MPEG", 21, "video/mpeg", EGL10.EGL_BAD_NATIVE_WINDOW, true);
        addFileType("MPG", 21, "video/mpeg", EGL10.EGL_BAD_NATIVE_WINDOW, false);
        addFileType("MP4", 21, "video/mp4", EGL10.EGL_BAD_NATIVE_WINDOW, false);
        addFileType("M4V", 22, "video/mp4", EGL10.EGL_BAD_NATIVE_WINDOW, false);
        addFileType("MOV", 201, "video/quicktime", EGL10.EGL_BAD_NATIVE_WINDOW, false);
        addFileType("3GP", 23, "video/3gpp", 47492, true);
        addFileType("3GPP", 23, "video/3gpp", 47492, false);
        addFileType("3G2", 24, "video/3gpp2", 47492, false);
        addFileType("3GPP2", 24, "video/3gpp2", 47492, false);
        addFileType("MKV", 27, "video/x-matroska");
        addFileType("WEBM", 30, "video/webm");
        addFileType("TS", 28, "video/mp2ts");
        addFileType("AVI", 29, "video/avi");
        if (isWMVEnabled()) {
            addFileType("WMV", 25, "video/x-ms-wmv", 47489, true);
            addFileType("ASF", 26, "video/x-ms-asf");
        }
        addFileType("JPG", 31, "image/jpeg", 14337, true);
        addFileType("JPEG", 31, "image/jpeg", 14337, false);
        addFileType("GIF", 32, "image/gif", 14343, true);
        addFileType("PNG", 33, "image/png", 14347, true);
        addFileType("BMP", 34, "image/x-ms-bmp", 14340, true);
        addFileType("WBMP", 35, "image/vnd.wap.wbmp", 14336, false);
        addFileType("WEBP", 36, "image/webp", 14336, false);
        addFileType("HEIC", 37, "image/heif", 14354, true);
        addFileType("HEIF", 37, "image/heif", 14354, false);
        addFileType("DNG", 300, "image/x-adobe-dng", 14353, true);
        addFileType("CR2", FILE_TYPE_CR2, "image/x-canon-cr2", 14349, false);
        addFileType("NEF", FILE_TYPE_NEF, "image/x-nikon-nef", 14338, false);
        addFileType("NRW", FILE_TYPE_NRW, "image/x-nikon-nrw", 14349, false);
        addFileType("ARW", FILE_TYPE_ARW, "image/x-sony-arw", 14349, false);
        addFileType("RW2", FILE_TYPE_RW2, "image/x-panasonic-rw2", 14349, false);
        addFileType("ORF", FILE_TYPE_ORF, "image/x-olympus-orf", 14349, false);
        addFileType("RAF", FILE_TYPE_RAF, "image/x-fuji-raf", 14336, false);
        addFileType("PEF", FILE_TYPE_PEF, "image/x-pentax-pef", 14349, false);
        addFileType("SRW", 309, "image/x-samsung-srw", 14349, false);
        addFileType("M3U", 41, "audio/x-mpegurl", 47633, true);
        addFileType("M3U", 41, "application/x-mpegurl", 47633, false);
        addFileType("PLS", 42, "audio/x-scpls", 47636, true);
        addFileType("WPL", 43, "application/vnd.ms-wpl", 47632, true);
        addFileType("M3U8", 44, "application/vnd.apple.mpegurl");
        addFileType("M3U8", 44, "audio/mpegurl");
        addFileType("M3U8", 44, "audio/x-mpegurl");
        addFileType("FL", 51, "application/x-android-drm-fl");
        addFileType("TXT", 100, "text/plain", 12292, true);
        addFileType("HTM", 101, "text/html", 12293, true);
        addFileType("HTML", 101, "text/html", 12293, false);
        addFileType("PDF", 102, "application/pdf");
        addFileType("DOC", 104, "application/msword", 47747, true);
        addFileType("XLS", 105, "application/vnd.ms-excel", 47749, true);
        addFileType("PPT", 106, "application/mspowerpoint", 47750, true);
        addFileType("FLAC", 10, "audio/flac", 47366, true);
        addFileType("ZIP", 107, "application/zip");
        addFileType("MPG", 200, "video/mp2p");
        addFileType("MPEG", 200, "video/mp2p");
        addFileType("APE", 1001, "audio/ape");
        addFileType("MP2", 1002, "audio/mpeg");
        addFileType("CUE", 1003, "audio/cue");
        addFileType("FLV", 1101, "video/x-flv");
        addFileType("F4V", 1101, "video/x-flv");
        addFileType("MOV", FILE_TYPE_MOV, "video/x-quicktime");
        addFileType("M2TS", 1104, "video/m2ts");
        addFileType("DOCX", 104, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        addFileType("XLSX", 105, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        addFileType("PPTX", 106, "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        addFileType("RAR", 10001, "application/rar");
        addFileType("JAR", 10002, "application/java-archive");
        addFileType("APK", FILE_TYPE_APK, "application/vnd.android.package-archive");
        addFileType("CHM", 10021, "application/x-expandedbook");
        addFileType("CSV", FILE_TYPE_CSV, "text/comma-separated-values");
        addFileType("ICS", FILE_TYPE_ICS, "text/calendar");
        addFileType("VCF", FILE_TYPE_VCF, "text/x-vcard");
        addFileType("VCS", FILE_TYPE_VCS, "text/x-vcalendar");
        addFileType("EBK2", FILE_TYPE_EBK, "text/x-expandedbook");
        addFileType("EBK3", FILE_TYPE_EBK, "text/x-expandedbook");
        addFileType("EPUB", 10027, "text/plain");
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

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "additional audio type", property = OppoRomType.OPPO)
    public static boolean isAudioFileType(int fileType) {
        if (fileType >= 1 && fileType <= 10) {
            return true;
        }
        if (fileType >= 11 && fileType <= 13) {
            return true;
        }
        if (fileType < 1001 || fileType > 1004) {
            return false;
        }
        return true;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "additional video type", property = OppoRomType.OPPO)
    public static boolean isVideoFileType(int fileType) {
        if (fileType >= 21 && fileType <= 30) {
            return true;
        }
        if (fileType >= 200 && fileType <= 201) {
            return true;
        }
        if (fileType < 1101 || fileType > 1104) {
            return false;
        }
        return true;
    }

    public static boolean isImageFileType(int fileType) {
        if (fileType >= 31 && fileType <= 37) {
            return true;
        }
        if (fileType < 300 || fileType > 309) {
            return false;
        }
        return true;
    }

    public static boolean isRawImageFileType(int fileType) {
        return fileType >= 300 && fileType <= 309;
    }

    public static boolean isPlayListFileType(int fileType) {
        return fileType >= 41 && fileType <= 44;
    }

    public static boolean isDrmFileType(int fileType) {
        return fileType >= 51 && fileType <= 51;
    }

    public static boolean isCompressFileType(int fileType) {
        if ((fileType < 10001 || fileType > 10002) && fileType != 107) {
            return false;
        }
        return true;
    }

    public static boolean isApkFileType(int fileType) {
        return fileType >= FILE_TYPE_APK && fileType <= FILE_TYPE_APK;
    }

    public static boolean isDocFileType(int fileType) {
        if (fileType >= 10021 && fileType <= 10027) {
            return true;
        }
        if (fileType < 100 || fileType > 106 || fileType == 103) {
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

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "get oppo default ringtone uri", property = OppoRomType.OPPO)
    public static Uri getDefaultRingtoneUri(Context context) {
        return getUriFor(context, OPPO_DEFAULT_RINGTONE);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "get oppo default alarm uri", property = OppoRomType.OPPO)
    public static Uri getDefaultAlarmUri(Context context) {
        return getUriFor(context, OPPO_DEFAULT_ALARM);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "get oppo default notification uri", property = OppoRomType.OPPO)
    public static Uri getDefaultNotificationUri(Context context) {
        return getUriFor(context, OPPO_DEFAULT_NOTIFICATION);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "get oppo default ringtone uri for sim2", property = OppoRomType.OPPO)
    public static Uri getDefaultRingtoneUriSIM2(Context context) {
        return getUriFor(context, OPPO_DEFAULT_RINGTONE_SIM2);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "get oppo default notificaiton uri for sim2", property = OppoRomType.OPPO)
    public static Uri getDefaultNotificationUriSIM2(Context context) {
        return getUriFor(context, OPPO_DEFAULT_NOTIFICATION_SIM2);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "get oppo default sms notificaiton uri", property = OppoRomType.OPPO)
    public static Uri getDefaultSmsNotificationUri(Context context) {
        return getUriFor(context, OPPO_DEFAULT_SMS_NOTIFICATION);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "get oppo default  uris ", property = OppoRomType.OPPO)
    private static Uri getUriFor(Context context, String name) {
        String value = System.getString(context.getContentResolver(), name);
        if (value != null) {
            return Uri.parse(value);
        }
        Log.e("MediaFile", name + " not set?!!!");
        return null;
    }
}
