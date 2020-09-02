package com.oppo.media;

import android.annotation.OppoHook;
import android.content.ClipDescription;
import android.content.ContentResolver;
import android.content.Context;
import android.media.DecoderCapabilities;
import android.media.MediaFormat;
import android.mtp.MtpConstants;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.util.Preconditions;
import com.android.internal.widget.MessagingMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import libcore.net.MimeUtils;

public class MediaFile {
    public static final int FILE_TYPE_3GPP = 23;
    public static final int FILE_TYPE_3GPP2 = 24;
    public static final int FILE_TYPE_AAC = 8;
    public static final int FILE_TYPE_AMR = 4;
    public static final int FILE_TYPE_APE = 1001;
    public static final int FILE_TYPE_APK = 10011;
    public static final int FILE_TYPE_ARW = 304;
    public static final int FILE_TYPE_ASF = 26;
    public static final int FILE_TYPE_AUDIO_3GPP = 11;
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
    private static final int LAST_AUDIO_FILE_TYPE = 11;
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
    @OppoHook(level = OppoHook.OppoHookType.NEW_FIELD, note = "oppo default uri name", property = OppoHook.OppoRomType.OPPO)
    public static final String OPPO_DEFAULT_ALARM = "oppo_default_alarm";
    @OppoHook(level = OppoHook.OppoHookType.NEW_FIELD, note = "oppo default uri name", property = OppoHook.OppoRomType.OPPO)
    public static final String OPPO_DEFAULT_NOTIFICATION = "oppo_default_notification";
    @OppoHook(level = OppoHook.OppoHookType.NEW_FIELD, note = "oppo default uri name", property = OppoHook.OppoRomType.OPPO)
    public static final String OPPO_DEFAULT_NOTIFICATION_SIM2 = "oppo_default_notification_sim2";
    @OppoHook(level = OppoHook.OppoHookType.NEW_FIELD, note = "oppo default uri name", property = OppoHook.OppoRomType.OPPO)
    public static final String OPPO_DEFAULT_RINGTONE = "oppo_default_ringtone";
    @OppoHook(level = OppoHook.OppoHookType.NEW_FIELD, note = "oppo default uri name", property = OppoHook.OppoRomType.OPPO)
    public static final String OPPO_DEFAULT_RINGTONE_SIM2 = "oppo_default_ringtone_sim2";
    @OppoHook(level = OppoHook.OppoHookType.NEW_FIELD, note = "oppo default sms notification name", property = OppoHook.OppoRomType.OPPO)
    public static final String OPPO_DEFAULT_SMS_NOTIFICATION = "oppo_default_sms_notification_sound";
    @OppoHook(level = OppoHook.OppoHookType.NEW_FIELD, note = "flag for scanning all files on storage", property = OppoHook.OppoRomType.OPPO)
    public static final int SCAN_ALL_FILE = 0;
    @OppoHook(level = OppoHook.OppoHookType.NEW_FIELD, note = "flag for scanning audio files on storage", property = OppoHook.OppoRomType.OPPO)
    public static final int SCAN_AUDIO_FILE = 1;
    @OppoHook(level = OppoHook.OppoHookType.NEW_FIELD, note = "flag for scanning image files on storage", property = OppoHook.OppoRomType.OPPO)
    public static final int SCAN_IMAGE_FILE = 2;
    @OppoHook(level = OppoHook.OppoHookType.NEW_FIELD, note = "flag for scanning other files on storage like *.apk, *.csv, *.vcf, *.ics, *.vcs", property = OppoHook.OppoRomType.OPPO)
    public static final int SCAN_OTHER_FILE = 8;
    @OppoHook(level = OppoHook.OppoHookType.NEW_FIELD, note = "flag for scanning video files on storage", property = OppoHook.OppoRomType.OPPO)
    public static final int SCAN_VIDEO_FILE = 4;
    private static final HashMap<Integer, String> sDeprecatedFormatToMimeTypeMap = new HashMap<>();
    private static final HashMap<String, Integer> sDeprecatedMimeTypeToFormatMap = new HashMap<>();
    private static final HashMap<String, MediaFileType> sFileTypeMap = new HashMap<>();
    private static final HashMap<String, Integer> sFileTypeToFormatMap = new HashMap<>();
    private static final HashMap<Integer, String> sFormatToMimeTypeMap = new HashMap<>();
    private static final HashMap<String, Integer> sMimeTypeMap = new HashMap<>();
    private static final HashMap<String, Integer> sMimeTypeToFormatMap = new HashMap<>();

    public static class MediaFileType {
        public final int fileType;
        public final String mimeType;

        MediaFileType(int fileType2, String mimeType2) {
            this.fileType = fileType2;
            this.mimeType = mimeType2;
        }
    }

    static {
        addFileAndMineType("MP3", 1, MediaFormat.MIMETYPE_AUDIO_MPEG, 12297, true);
        addFileAndMineType("MPGA", 1, MediaFormat.MIMETYPE_AUDIO_MPEG, 12297, false);
        addFileAndMineType("M4A", 2, "audio/mp4", 12299, false);
        addFileAndMineType("WAV", 3, "audio/x-wav", 12296, true);
        addFileAndMineType("AMR", 4, "audio/amr");
        addFileAndMineType("3GPP", 11, MediaFormat.MIMETYPE_AUDIO_AMR_NB);
        addFileAndMineType("AWB", 5, MediaFormat.MIMETYPE_AUDIO_AMR_WB);
        if (isWMAEnabled()) {
            addFileAndMineType("WMA", 6, "audio/x-ms-wma", MtpConstants.FORMAT_WMA, true);
        }
        addFileAndMineType("OGG", 7, "audio/ogg", MtpConstants.FORMAT_OGG, false);
        addFileAndMineType("OGG", 7, "application/ogg", MtpConstants.FORMAT_OGG, true);
        addFileAndMineType("OGA", 7, "application/ogg", MtpConstants.FORMAT_OGG, false);
        addFileAndMineType("AAC", 8, "audio/aac", MtpConstants.FORMAT_AAC, true);
        addFileAndMineType("AAC", 8, "audio/aac-adts", MtpConstants.FORMAT_AAC, false);
        addFileAndMineType("MKA", 9, "audio/x-matroska");
        addFileAndMineType("MID", 11, "audio/mid");
        addFileAndMineType("MID", 11, "audio/midi");
        addFileAndMineType("MIDI", 11, "audio/midi");
        addFileAndMineType("XMF", 11, "audio/midi");
        addFileAndMineType("RTTTL", 11, "audio/midi");
        addFileAndMineType("SMF", 12, "audio/sp-midi");
        addFileAndMineType("IMY", 13, "audio/imelody");
        addFileAndMineType("RTX", 11, "audio/midi");
        addFileAndMineType("OTA", 11, "audio/midi");
        addFileAndMineType("MXMF", 11, "audio/midi");
        addFileAndMineType("MPEG", 21, "video/mpeg", 12299, true);
        addFileAndMineType("MPG", 21, "video/mpeg", 12299, false);
        addFileAndMineType("MP4", 21, "video/mp4", 12299, false);
        addFileAndMineType("M4V", 22, "video/mp4", 12299, false);
        addFileAndMineType("MOV", 201, "video/quicktime", 12299, false);
        addFileAndMineType("3GP", 23, MediaFormat.MIMETYPE_VIDEO_H263, MtpConstants.FORMAT_3GP_CONTAINER, true);
        addFileAndMineType("3GPP", 23, MediaFormat.MIMETYPE_VIDEO_H263, MtpConstants.FORMAT_3GP_CONTAINER, false);
        addFileAndMineType("3G2", 24, "video/3gpp2", MtpConstants.FORMAT_3GP_CONTAINER, false);
        addFileAndMineType("3GPP2", 24, "video/3gpp2", MtpConstants.FORMAT_3GP_CONTAINER, false);
        addFileAndMineType("MKV", 27, "video/x-matroska");
        addFileAndMineType("WEBM", 30, "video/webm");
        addFileAndMineType("TS", 28, "video/mp2ts");
        addFileAndMineType("AVI", 29, "video/avi");
        if (isWMVEnabled()) {
            addFileAndMineType("WMV", 25, "video/x-ms-wmv", MtpConstants.FORMAT_WMV, true);
            addFileAndMineType("ASF", 26, "video/x-ms-asf");
        }
        addFileAndMineType("JPG", 31, "image/jpeg", MtpConstants.FORMAT_EXIF_JPEG, true);
        addFileAndMineType("JPEG", 31, "image/jpeg", MtpConstants.FORMAT_EXIF_JPEG, false);
        addFileAndMineType("GIF", 32, "image/gif", MtpConstants.FORMAT_GIF, true);
        addFileAndMineType("PNG", 33, "image/png", MtpConstants.FORMAT_PNG, true);
        addFileAndMineType("BMP", 34, "image/x-ms-bmp", MtpConstants.FORMAT_BMP, true);
        addFileAndMineType("BMP", 34, "image/bmp", MtpConstants.FORMAT_BMP, false);
        addFileAndMineType("WBMP", 35, "image/vnd.wap.wbmp", 14336, false);
        addFileAndMineType("WEBP", 36, "image/webp", 14336, false);
        addFileAndMineType("HEIC", 37, "image/heif", MtpConstants.FORMAT_HEIF, true);
        addFileAndMineType("HEIF", 37, "image/heif", MtpConstants.FORMAT_HEIF, false);
        addFileAndMineType("DNG", 300, "image/x-adobe-dng", MtpConstants.FORMAT_DNG, true);
        addFileAndMineType("CR2", 301, "image/x-canon-cr2", MtpConstants.FORMAT_TIFF, false);
        addFileAndMineType("NEF", 302, "image/x-nikon-nef", MtpConstants.FORMAT_TIFF_EP, false);
        addFileAndMineType("NRW", 303, "image/x-nikon-nrw", MtpConstants.FORMAT_TIFF, false);
        addFileAndMineType("ARW", 304, "image/x-sony-arw", MtpConstants.FORMAT_TIFF, false);
        addFileAndMineType("RW2", 305, "image/x-panasonic-rw2", MtpConstants.FORMAT_TIFF, false);
        addFileAndMineType("ORF", 306, "image/x-olympus-orf", MtpConstants.FORMAT_TIFF, false);
        addFileAndMineType("RAF", 307, "image/x-fuji-raf", 14336, false);
        addFileAndMineType("PEF", 308, "image/x-pentax-pef", MtpConstants.FORMAT_TIFF, false);
        addFileAndMineType("SRW", 309, "image/x-samsung-srw", MtpConstants.FORMAT_TIFF, false);
        addFileAndMineType("M3U", 41, "audio/x-mpegurl", MtpConstants.FORMAT_M3U_PLAYLIST, true);
        addFileAndMineType("M3U", 41, "application/x-mpegurl", MtpConstants.FORMAT_M3U_PLAYLIST, false);
        addFileAndMineType("PLS", 42, "audio/x-scpls", MtpConstants.FORMAT_PLS_PLAYLIST, true);
        addFileAndMineType("WPL", 43, "application/vnd.ms-wpl", MtpConstants.FORMAT_WPL_PLAYLIST, true);
        addFileAndMineType("M3U8", 44, "application/vnd.apple.mpegurl");
        addFileAndMineType("M3U8", 44, "audio/mpegurl");
        addFileAndMineType("M3U8", 44, "audio/x-mpegurl");
        addFileAndMineType("FL", 51, "application/x-android-drm-fl");
        addFileAndMineType("TXT", 100, ClipDescription.MIMETYPE_TEXT_PLAIN, 12292, true);
        addFileAndMineType("HTM", 101, ClipDescription.MIMETYPE_TEXT_HTML, 12293, true);
        addFileAndMineType("HTML", 101, ClipDescription.MIMETYPE_TEXT_HTML, 12293, false);
        addFileAndMineType("PDF", 102, "application/pdf");
        addFileAndMineType("DOC", 104, "application/msword", MtpConstants.FORMAT_MS_WORD_DOCUMENT, true);
        addFileAndMineType("XLS", 105, "application/vnd.ms-excel", MtpConstants.FORMAT_MS_EXCEL_SPREADSHEET, true);
        addFileAndMineType("PPT", 106, "application/mspowerpoint", MtpConstants.FORMAT_MS_POWERPOINT_PRESENTATION, true);
        addFileAndMineType("FLAC", 10, MediaFormat.MIMETYPE_AUDIO_FLAC, MtpConstants.FORMAT_FLAC, true);
        addFileAndMineType("ZIP", 107, "application/zip");
        addFileAndMineType("MPG", 200, "video/mp2p");
        addFileAndMineType("MPEG", 200, "video/mp2p");
        addFileAndMineType("APE", 1001, "audio/ape");
        addFileAndMineType("MP2", 1002, MediaFormat.MIMETYPE_AUDIO_MPEG);
        addFileAndMineType("CUE", 1003, "audio/cue");
        addFileAndMineType("FLV", 1101, "video/x-flv");
        addFileAndMineType("F4V", 1101, "video/x-flv");
        addFileAndMineType("MOV", 1103, "video/x-quicktime");
        addFileAndMineType("M2TS", 1104, "video/m2ts");
        addFileAndMineType("DOCX", 104, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        addFileAndMineType("XLSX", 105, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        addFileAndMineType("PPTX", 106, "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        addFileAndMineType("RAR", 10001, "application/rar");
        addFileAndMineType("JAR", 10002, "application/java-archive");
        addFileAndMineType("APK", 10011, "application/vnd.android.package-archive");
        addFileAndMineType("CHM", 10021, "application/x-expandedbook");
        addFileAndMineType("CSV", 10022, "text/comma-separated-values");
        addFileAndMineType("ICS", 10023, "text/calendar");
        addFileAndMineType("VCF", 10024, ContactsContract.Contacts.CONTENT_VCARD_TYPE);
        addFileAndMineType("VCS", 10025, "text/x-vcalendar");
        addFileAndMineType("EBK2", 10026, "text/x-expandedbook");
        addFileAndMineType("EBK3", 10026, "text/x-expandedbook");
        addFileAndMineType("EPUB", 10027, ClipDescription.MIMETYPE_TEXT_PLAIN);
        addFileType(12297, MediaFormat.MIMETYPE_AUDIO_MPEG);
        addFileType(12296, "audio/x-wav");
        addFileType(MtpConstants.FORMAT_WMA, "audio/x-ms-wma");
        addFileType(MtpConstants.FORMAT_OGG, "audio/ogg");
        addFileType(MtpConstants.FORMAT_AAC, "audio/aac");
        addFileType(MtpConstants.FORMAT_FLAC, MediaFormat.MIMETYPE_AUDIO_FLAC);
        addFileType(12295, "audio/x-aiff");
        addFileType(MtpConstants.FORMAT_MP2, MediaFormat.MIMETYPE_AUDIO_MPEG);
        addFileType(12299, "video/mpeg");
        addFileType(MtpConstants.FORMAT_MP4_CONTAINER, "video/mp4");
        addFileType(MtpConstants.FORMAT_3GP_CONTAINER, MediaFormat.MIMETYPE_VIDEO_H263);
        addFileType(MtpConstants.FORMAT_3GP_CONTAINER, "video/3gpp2");
        addFileType(12298, "video/avi");
        addFileType(MtpConstants.FORMAT_WMV, "video/x-ms-wmv");
        addFileType(12300, "video/x-ms-asf");
        addFileType(MtpConstants.FORMAT_EXIF_JPEG, "image/jpeg");
        addFileType(MtpConstants.FORMAT_GIF, "image/gif");
        addFileType(MtpConstants.FORMAT_PNG, "image/png");
        addFileType(MtpConstants.FORMAT_BMP, "image/x-ms-bmp");
        addFileType(MtpConstants.FORMAT_HEIF, "image/heif");
        addFileType(MtpConstants.FORMAT_DNG, "image/x-adobe-dng");
        addFileType(MtpConstants.FORMAT_TIFF, "image/tiff");
        addFileType(MtpConstants.FORMAT_TIFF, "image/x-canon-cr2");
        addFileType(MtpConstants.FORMAT_TIFF, "image/x-nikon-nrw");
        addFileType(MtpConstants.FORMAT_TIFF, "image/x-sony-arw");
        addFileType(MtpConstants.FORMAT_TIFF, "image/x-panasonic-rw2");
        addFileType(MtpConstants.FORMAT_TIFF, "image/x-olympus-orf");
        addFileType(MtpConstants.FORMAT_TIFF, "image/x-pentax-pef");
        addFileType(MtpConstants.FORMAT_TIFF, "image/x-samsung-srw");
        addFileType(MtpConstants.FORMAT_TIFF_EP, "image/tiff");
        addFileType(MtpConstants.FORMAT_TIFF_EP, "image/x-nikon-nef");
        addFileType(MtpConstants.FORMAT_JP2, "image/jp2");
        addFileType(MtpConstants.FORMAT_JPX, "image/jpx");
        addFileType(MtpConstants.FORMAT_M3U_PLAYLIST, "audio/x-mpegurl");
        addFileType(MtpConstants.FORMAT_PLS_PLAYLIST, "audio/x-scpls");
        addFileType(MtpConstants.FORMAT_WPL_PLAYLIST, "application/vnd.ms-wpl");
        addFileType(MtpConstants.FORMAT_ASX_PLAYLIST, "video/x-ms-asf");
        addFileType(12292, ClipDescription.MIMETYPE_TEXT_PLAIN);
        addFileType(12293, ClipDescription.MIMETYPE_TEXT_HTML);
        addFileType(MtpConstants.FORMAT_XML_DOCUMENT, "text/xml");
        addFileType(MtpConstants.FORMAT_MS_WORD_DOCUMENT, "application/msword");
        addFileType(MtpConstants.FORMAT_MS_WORD_DOCUMENT, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        addFileType(MtpConstants.FORMAT_MS_EXCEL_SPREADSHEET, "application/vnd.ms-excel");
        addFileType(MtpConstants.FORMAT_MS_EXCEL_SPREADSHEET, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        addFileType(MtpConstants.FORMAT_MS_POWERPOINT_PRESENTATION, "application/vnd.ms-powerpoint");
        addFileType(MtpConstants.FORMAT_MS_POWERPOINT_PRESENTATION, "application/vnd.openxmlformats-officedocument.presentationml.presentation");
    }

    static void addFileAndMineType(String extension, int fileType, String mimeType) {
        sFileTypeMap.put(extension, new MediaFileType(fileType, mimeType));
        sMimeTypeMap.put(mimeType, Integer.valueOf(fileType));
    }

    private static void addFileAndMineType(String extension, int fileType, String mimeType, int mtpFormatCode, boolean primaryType) {
        addFileAndMineType(extension, fileType, mimeType);
        sFileTypeToFormatMap.put(extension, Integer.valueOf(mtpFormatCode));
        sDeprecatedMimeTypeToFormatMap.put(mimeType, Integer.valueOf(mtpFormatCode));
        if (primaryType) {
            Preconditions.checkArgument(!sDeprecatedFormatToMimeTypeMap.containsKey(Integer.valueOf(mtpFormatCode)));
            sDeprecatedFormatToMimeTypeMap.put(Integer.valueOf(mtpFormatCode), mimeType);
        }
    }

    private static boolean isWMAEnabled() {
        List<DecoderCapabilities.AudioDecoder> decoders = DecoderCapabilities.getAudioDecoders();
        int count = decoders.size();
        for (int i = 0; i < count; i++) {
            if (decoders.get(i) == DecoderCapabilities.AudioDecoder.AUDIO_DECODER_WMA) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWMVEnabled() {
        List<DecoderCapabilities.VideoDecoder> decoders = DecoderCapabilities.getVideoDecoders();
        int count = decoders.size();
        for (int i = 0; i < count; i++) {
            if (decoders.get(i) == DecoderCapabilities.VideoDecoder.VIDEO_DECODER_WMV) {
                return true;
            }
        }
        return false;
    }

    @OppoHook(level = OppoHook.OppoHookType.CHANGE_CODE, note = "additional audio type", property = OppoHook.OppoRomType.OPPO)
    public static boolean isAudioFileType(int fileType) {
        if (fileType >= 1 && fileType <= 11) {
            return true;
        }
        if (fileType < 11 || fileType > 13) {
            return fileType >= 1001 && fileType <= 1004;
        }
        return true;
    }

    @OppoHook(level = OppoHook.OppoHookType.CHANGE_CODE, note = "additional video type", property = OppoHook.OppoRomType.OPPO)
    public static boolean isVideoFileType(int fileType) {
        return (fileType >= 21 && fileType <= 30) || (fileType >= 200 && fileType <= 201) || (fileType >= 1101 && fileType <= 1104);
    }

    public static boolean isImageFileType(int fileType) {
        return (fileType >= 31 && fileType <= 37) || (fileType >= 300 && fileType <= 309);
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
        return (fileType >= 10001 && fileType <= 10002) || fileType == 107;
    }

    public static boolean isApkFileType(int fileType) {
        return fileType >= 10011 && fileType <= 10011;
    }

    public static boolean isDocFileType(int fileType) {
        return (fileType >= 10021 && fileType <= 10027) || (fileType >= 100 && fileType <= 106 && fileType != 103);
    }

    public static MediaFileType getFileType(String path) {
        int lastDot = path.lastIndexOf(46);
        if (lastDot < 0) {
            return null;
        }
        return sFileTypeMap.get(path.substring(lastDot + 1).toUpperCase(Locale.ROOT));
    }

    public static boolean isMimeTypeMedia(String mimeType) {
        int fileType = getFileTypeForMimeType(mimeType);
        return isAudioFileType(fileType) || isVideoFileType(fileType) || isImageFileType(fileType) || isPlayListFileType(fileType);
    }

    public static int getFileTypeForMimeType(String mimeType) {
        Integer value = sMimeTypeMap.get(mimeType);
        if (value == null) {
            return 0;
        }
        return value.intValue();
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "get oppo default ringtone uri", property = OppoHook.OppoRomType.OPPO)
    public static Uri getDefaultRingtoneUri(Context context) {
        return getUriFor(context, "oppo_default_ringtone");
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "get oppo default alarm uri", property = OppoHook.OppoRomType.OPPO)
    public static Uri getDefaultAlarmUri(Context context) {
        return getUriFor(context, "oppo_default_alarm");
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "get oppo default notification uri", property = OppoHook.OppoRomType.OPPO)
    public static Uri getDefaultNotificationUri(Context context) {
        return getUriFor(context, "oppo_default_notification");
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "get oppo default ringtone uri for sim2", property = OppoHook.OppoRomType.OPPO)
    public static Uri getDefaultRingtoneUriSIM2(Context context) {
        return getUriFor(context, "oppo_default_ringtone_sim2");
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "get oppo default notificaiton uri for sim2", property = OppoHook.OppoRomType.OPPO)
    public static Uri getDefaultNotificationUriSIM2(Context context) {
        return getUriFor(context, "oppo_default_notification_sim2");
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "get oppo default sms notificaiton uri", property = OppoHook.OppoRomType.OPPO)
    public static Uri getDefaultSmsNotificationUri(Context context) {
        return getUriFor(context, OPPO_DEFAULT_SMS_NOTIFICATION);
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "get oppo default  uris ", property = OppoHook.OppoRomType.OPPO)
    private static Uri getUriFor(Context context, String name) {
        String value = Settings.System.getString(context.getContentResolver(), name);
        if (value != null) {
            return Uri.parse(value);
        }
        Log.e("MediaFile", name + " not set?!!!");
        return null;
    }

    static void addFileType(String extension, int fileType, String mimeType) {
    }

    private static void addFileType(int mtpFormatCode, String mimeType) {
        if (!sMimeTypeToFormatMap.containsKey(mimeType)) {
            sMimeTypeToFormatMap.put(mimeType, Integer.valueOf(mtpFormatCode));
        }
        if (!sFormatToMimeTypeMap.containsKey(Integer.valueOf(mtpFormatCode))) {
            sFormatToMimeTypeMap.put(Integer.valueOf(mtpFormatCode), mimeType);
        }
    }

    public static boolean isExifMimeType(String mimeType) {
        return isImageMimeType(mimeType);
    }

    public static boolean isAudioMimeType(String mimeType) {
        return normalizeMimeType(mimeType).startsWith("audio/");
    }

    public static boolean isVideoMimeType(String mimeType) {
        return normalizeMimeType(mimeType).startsWith("video/");
    }

    public static boolean isImageMimeType(String mimeType) {
        return normalizeMimeType(mimeType).startsWith(MessagingMessage.IMAGE_MIME_TYPE_PREFIX);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static boolean isPlayListMimeType(String mimeType) {
        char c;
        String normalizeMimeType = normalizeMimeType(mimeType);
        switch (normalizeMimeType.hashCode()) {
            case -1165508903:
                if (normalizeMimeType.equals("audio/x-scpls")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -979095690:
                if (normalizeMimeType.equals("application/x-mpegurl")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -622808459:
                if (normalizeMimeType.equals("application/vnd.apple.mpegurl")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -432766831:
                if (normalizeMimeType.equals("audio/mpegurl")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 264230524:
                if (normalizeMimeType.equals("audio/x-mpegurl")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1872259501:
                if (normalizeMimeType.equals("application/vnd.ms-wpl")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        return c == 0 || c == 1 || c == 2 || c == 3 || c == 4 || c == 5;
    }

    public static boolean isDrmMimeType(String mimeType) {
        return normalizeMimeType(mimeType).equals("application/x-android-drm-fl");
    }

    public static boolean isApkMimeType(String mimeType) {
        return normalizeMimeType(mimeType).equals("application/vnd.android.package-archive");
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x003c A[ADDED_TO_REGION] */
    public static boolean isCompressMimeType(String mimeType) {
        char c;
        String normalizeMimeType = normalizeMimeType(mimeType);
        int hashCode = normalizeMimeType.hashCode();
        if (hashCode != -1248333084) {
            if (hashCode != -1248325150) {
                if (hashCode == 2049276534 && normalizeMimeType.equals("application/java-archive")) {
                    c = 1;
                    return c != 0 || c == 1 || c == 2;
                }
            } else if (normalizeMimeType.equals("application/zip")) {
                c = 2;
                if (c != 0) {
                }
            }
        } else if (normalizeMimeType.equals("application/rar")) {
            c = 0;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static boolean isDocMimeType(String mimeType) {
        char c;
        String normalizeMimeType = normalizeMimeType(mimeType);
        switch (normalizeMimeType.hashCode()) {
            case -2135895576:
                if (normalizeMimeType.equals("text/comma-separated-values")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -2008589971:
                if (normalizeMimeType.equals("application/epub+zip")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -1248334925:
                if (normalizeMimeType.equals("application/pdf")) {
                    c = 9;
                    break;
                }
                c = 65535;
                break;
            case -1082243251:
                if (normalizeMimeType.equals(ClipDescription.MIMETYPE_TEXT_HTML)) {
                    c = 8;
                    break;
                }
                c = 65535;
                break;
            case -1073633483:
                if (normalizeMimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case -1071817359:
                if (normalizeMimeType.equals("application/vnd.ms-powerpoint")) {
                    c = 12;
                    break;
                }
                c = 65535;
                break;
            case -1050893613:
                if (normalizeMimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                    c = 13;
                    break;
                }
                c = 65535;
                break;
            case -1004747228:
                if (normalizeMimeType.equals("text/csv")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -958424608:
                if (normalizeMimeType.equals("text/calendar")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -366307023:
                if (normalizeMimeType.equals("application/vnd.ms-excel")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 262346941:
                if (normalizeMimeType.equals("text/x-vcalendar")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 501428239:
                if (normalizeMimeType.equals(ContactsContract.Contacts.CONTENT_VCARD_TYPE)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 817335912:
                if (normalizeMimeType.equals(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 904647503:
                if (normalizeMimeType.equals("application/msword")) {
                    c = 10;
                    break;
                }
                c = 65535;
                break;
            case 1496903267:
                if (normalizeMimeType.equals("chemical/x-chemdraw")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1993842850:
                if (normalizeMimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                return true;
            default:
                return false;
        }
    }

    public static boolean isMediaMimeType(String mimeType) {
        return isAudioMimeType(mimeType) || isVideoMimeType(mimeType) || isImageMimeType(mimeType) || isPlayListMimeType(mimeType);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static boolean isRawImageMimeType(String mimeType) {
        char c;
        String normalizeMimeType = normalizeMimeType(mimeType);
        switch (normalizeMimeType.hashCode()) {
            case -1635437028:
                if (normalizeMimeType.equals("image/x-samsung-srw")) {
                    c = 8;
                    break;
                }
                c = 65535;
                break;
            case -1594371159:
                if (normalizeMimeType.equals("image/x-sony-arw")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1487103447:
                if (normalizeMimeType.equals("image/tiff")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1423313290:
                if (normalizeMimeType.equals("image/x-adobe-dng")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -985160897:
                if (normalizeMimeType.equals("image/x-panasonic-rw2")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -332763809:
                if (normalizeMimeType.equals("image/x-pentax-pef")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1378106698:
                if (normalizeMimeType.equals("image/x-olympus-orf")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 2099152104:
                if (normalizeMimeType.equals("image/x-nikon-nef")) {
                    c = 9;
                    break;
                }
                c = 65535;
                break;
            case 2099152524:
                if (normalizeMimeType.equals("image/x-nikon-nrw")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 2111234748:
                if (normalizeMimeType.equals("image/x-canon-cr2")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                return true;
            default:
                return false;
        }
    }

    public static String getFileTitle(String path) {
        int lastSlash;
        int lastSlash2 = path.lastIndexOf(47);
        if (lastSlash2 >= 0 && (lastSlash = lastSlash2 + 1) < path.length()) {
            path = path.substring(lastSlash);
        }
        int lastDot = path.lastIndexOf(46);
        if (lastDot > 0) {
            return path.substring(0, lastDot);
        }
        return path;
    }

    public static String getFileExtension(String path) {
        int lastDot;
        if (path != null && (lastDot = path.lastIndexOf(46)) >= 0) {
            return path.substring(lastDot + 1);
        }
        return null;
    }

    public static String getMimeType(String path, int formatCode) {
        String mimeType = getMimeTypeForFile(path);
        if (!ContentResolver.MIME_TYPE_DEFAULT.equals(mimeType)) {
            return mimeType;
        }
        return getMimeTypeForFormatCode(formatCode);
    }

    public static String getMimeTypeForFile(String path) {
        String mimeType = guessMimeTypeFromExtension(getFileExtension(path));
        return mimeType != null ? mimeType : ContentResolver.MIME_TYPE_DEFAULT;
    }

    public static String getMimeTypeForFormatCode(int formatCode) {
        String mimeType = sFormatToMimeTypeMap.get(Integer.valueOf(formatCode));
        return mimeType != null ? mimeType : ContentResolver.MIME_TYPE_DEFAULT;
    }

    public static int getFormatCode(String path, String mimeType) {
        int formatCode = getFormatCodeForMimeType(mimeType);
        if (formatCode != 12288) {
            return formatCode;
        }
        return getFormatCodeForFile(path);
    }

    public static int getFormatCodeForFile(String path) {
        return getFormatCodeForMimeType(getMimeTypeForFile(path));
    }

    public static int getFormatCodeForMimeType(String mimeType) {
        if (mimeType == null) {
            return 12288;
        }
        Integer value = sMimeTypeToFormatMap.get(mimeType);
        if (value != null) {
            return value.intValue();
        }
        String mimeType2 = normalizeMimeType(mimeType);
        Integer value2 = sMimeTypeToFormatMap.get(mimeType2);
        if (value2 != null) {
            return value2.intValue();
        }
        if (mimeType2.startsWith("audio/")) {
            return MtpConstants.FORMAT_UNDEFINED_AUDIO;
        }
        if (mimeType2.startsWith("video/")) {
            return MtpConstants.FORMAT_UNDEFINED_VIDEO;
        }
        if (mimeType2.startsWith(MessagingMessage.IMAGE_MIME_TYPE_PREFIX)) {
            return 14336;
        }
        return 12288;
    }

    private static String normalizeMimeType(String mimeType) {
        String extensionMimeType;
        String extension = MimeUtils.guessExtensionFromMimeType(mimeType);
        if (extension == null || (extensionMimeType = MimeUtils.guessMimeTypeFromExtension(extension)) == null) {
            return mimeType != null ? mimeType : ContentResolver.MIME_TYPE_DEFAULT;
        }
        return extensionMimeType;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002b, code lost:
        if (r0.equals("audio/x-pn-realaudio") != false) goto L_0x002f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0076, code lost:
        if (r8.equals("cue") != false) goto L_0x007a;
     */
    private static String guessMimeTypeFromExtension(String extension) {
        String mimeType = null;
        if (extension != null) {
            mimeType = MimeUtils.guessMimeTypeFromExtension(extension);
            boolean z = false;
            if (mimeType != null) {
                int hashCode = mimeType.hashCode();
                if (hashCode != -794081673) {
                    if (hashCode == 379957065 && mimeType.equals("audio/x-pn-realaudio-plugin")) {
                        z = true;
                        if (z || z) {
                            return null;
                        }
                        return mimeType;
                    }
                }
                z = true;
                if (z) {
                }
                return null;
            }
            String extension2 = extension.toLowerCase(Locale.ROOT);
            switch (extension2.hashCode()) {
                case 98867:
                    break;
                case 99752:
                    if (extension2.equals("f4v")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 3106436:
                    if (extension2.equals("ebk2")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 3106437:
                    if (extension2.equals("ebk3")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 3298980:
                    if (extension2.equals("m2ts")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                default:
                    z = true;
                    break;
            }
            if (!z) {
                mimeType = "audio/cue";
            } else if (z) {
                mimeType = "video/x-flv";
            } else if (z) {
                mimeType = "video/m2ts";
            } else if (z || z) {
                mimeType = "text/x-expandedbook";
            }
        }
        return mimeType != null ? mimeType : ContentResolver.MIME_TYPE_DEFAULT;
    }
}
