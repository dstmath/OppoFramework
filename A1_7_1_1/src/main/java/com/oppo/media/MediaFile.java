package com.oppo.media;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.Context;
import android.media.DecoderCapabilities;
import android.media.DecoderCapabilities.AudioDecoder;
import android.media.DecoderCapabilities.VideoDecoder;
import android.net.Uri;
import android.os.Build.VERSION;
import java.util.HashMap;
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
    private static final int FORMAT_DEFINED = 14336;
    private static final int FORMAT_DNG = 14353;
    private static final int LAST_APK_FILE_TYPE = 10011;
    private static final int LAST_AUDIO_FILE_TYPE = 10;
    private static final int LAST_COMPRESS_FILE_TYPE = 10002;
    private static final int LAST_DOC_FILE_TYPE = 10027;
    private static final int LAST_DRM_FILE_TYPE = 51;
    private static final int LAST_FFMPEG_AUDIO_FILE_TYPE = 1004;
    private static final int LAST_FFMPEG_VIDEO_FILE_TYPE = 1104;
    private static final int LAST_IMAGE_FILE_TYPE = 36;
    private static final int LAST_MIDI_FILE_TYPE = 13;
    private static final int LAST_PLAYLIST_FILE_TYPE = 44;
    private static final int LAST_POPULAR_DOC_FILE_TYPE = 106;
    private static final int LAST_RAW_IMAGE_FILE_TYPE = 309;
    private static final int LAST_VIDEO_FILE_TYPE = 30;
    private static final int LAST_VIDEO_FILE_TYPE2 = 200;
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
    private static final int SDK_ANDROID_N = 24;
    private static final HashMap<String, MediaFileType> sFileTypeMap = null;
    private static final HashMap<String, Integer> sFileTypeToFormatMap = null;
    private static final HashMap<Integer, String> sFormatToMimeTypeMap = null;
    private static final HashMap<String, Integer> sMimeTypeMap = null;
    private static final HashMap<String, Integer> sMimeTypeToFormatMap = null;

    public static class MediaFileType {
        public final int fileType;
        public final String mimeType;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.oppo.media.MediaFile.MediaFileType.<init>(int, java.lang.String):void, dex: 
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
        MediaFileType(int r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.oppo.media.MediaFile.MediaFileType.<init>(int, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.MediaFile.MediaFileType.<init>(int, java.lang.String):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.media.MediaFile.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.media.MediaFile.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.MediaFile.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.media.MediaFile.<init>():void, dex: 
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
    public MediaFile() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.media.MediaFile.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.MediaFile.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.MediaFile.addFileType(java.lang.String, int, java.lang.String):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static void addFileType(java.lang.String r1, int r2, java.lang.String r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.MediaFile.addFileType(java.lang.String, int, java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.MediaFile.addFileType(java.lang.String, int, java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.MediaFile.addFileType(java.lang.String, int, java.lang.String, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static void addFileType(java.lang.String r1, int r2, java.lang.String r3, int r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.MediaFile.addFileType(java.lang.String, int, java.lang.String, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.MediaFile.addFileType(java.lang.String, int, java.lang.String, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.MediaFile.getFileTitle(java.lang.String):java.lang.String, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static java.lang.String getFileTitle(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.MediaFile.getFileTitle(java.lang.String):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.MediaFile.getFileTitle(java.lang.String):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.MediaFile.getFileType(java.lang.String):com.oppo.media.MediaFile$MediaFileType, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static com.oppo.media.MediaFile.MediaFileType getFileType(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.MediaFile.getFileType(java.lang.String):com.oppo.media.MediaFile$MediaFileType, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.MediaFile.getFileType(java.lang.String):com.oppo.media.MediaFile$MediaFileType");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.MediaFile.getFileTypeForMimeType(java.lang.String):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static int getFileTypeForMimeType(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.MediaFile.getFileTypeForMimeType(java.lang.String):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.MediaFile.getFileTypeForMimeType(java.lang.String):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.MediaFile.getFormatCode(java.lang.String, java.lang.String):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static int getFormatCode(java.lang.String r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.MediaFile.getFormatCode(java.lang.String, java.lang.String):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.MediaFile.getFormatCode(java.lang.String, java.lang.String):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.media.MediaFile.getMimeTypeForFile(java.lang.String):java.lang.String, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static java.lang.String getMimeTypeForFile(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.media.MediaFile.getMimeTypeForFile(java.lang.String):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.MediaFile.getMimeTypeForFile(java.lang.String):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.MediaFile.getMimeTypeForFormatCode(int):java.lang.String, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static java.lang.String getMimeTypeForFormatCode(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.MediaFile.getMimeTypeForFormatCode(int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.MediaFile.getMimeTypeForFormatCode(int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.MediaFile.getUriFor(android.content.Context, java.lang.String):android.net.Uri, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    @android.annotation.OppoHook(level = android.annotation.OppoHook.OppoHookType.NEW_METHOD, note = "get oppo default  uris ", property = android.annotation.OppoHook.OppoRomType.OPPO)
    private static android.net.Uri getUriFor(android.content.Context r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.media.MediaFile.getUriFor(android.content.Context, java.lang.String):android.net.Uri, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.MediaFile.getUriFor(android.content.Context, java.lang.String):android.net.Uri");
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
        if (fileType >= 200 && fileType <= 200) {
            return true;
        }
        if (fileType < 1101 || fileType > 1104) {
            return false;
        }
        return true;
    }

    public static boolean isImageFileType(int fileType) {
        boolean z = true;
        boolean z2 = false;
        if (VERSION.SDK_INT >= 24) {
            if (fileType < 31 || fileType > 36) {
                if (fileType < 300) {
                    z = false;
                } else if (fileType > 309) {
                    z = false;
                }
            }
            return z;
        }
        if (fileType >= 31 && fileType <= 36) {
            z2 = true;
        }
        return z2;
    }

    public static boolean isRawImageFileType(int fileType) {
        if (fileType < 300 || fileType > 309) {
            return false;
        }
        return true;
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
        return fileType >= 10011 && fileType <= 10011;
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

    public static boolean isMimeTypeMedia(String mimeType) {
        int fileType = getFileTypeForMimeType(mimeType);
        if (isAudioFileType(fileType) || isVideoFileType(fileType) || isImageFileType(fileType)) {
            return true;
        }
        return isPlayListFileType(fileType);
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
}
