package com.mediatek.common.regionalphone;

import android.net.Uri;
import android.provider.BaseColumns;

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
public class RegionalPhone {
    public static final Uri APN_URI = null;
    public static final String AUTHORITY = "com.mediatek.provider.regionalphone";
    public static final Uri BROWSER_URI = null;
    public static final String DATABASE_NAME = "regionalphone.db";
    public static final Uri MMS_SMS_URI = null;
    public static final Uri SEARCHENGINE_URI = null;
    public static final Uri SETTINGS_URI = null;
    public static final String TABLE_APN = "apn";
    public static final String TABLE_BROWSER = "browser";
    public static final String TABLE_MMS_SMS = "mms_sms";
    public static final String TABLE_SEARCHENGINE = "searchengine";
    public static final String TABLE_SETTINGS = "settings";
    public static final String TABLE_WALLPAPER = "wallpaper";
    public static final Uri WALLPAPER_URI = null;

    public static final class APN implements BaseColumns {
        public static final String MCC_MNC_TIMESTAMP = "mcc_mnc_timestamp";
        public static final String MMS_GPRS_APN = "mms_GPRS_APN";
        public static final String MMS_NAME = "mms_name";
        public static final String MMS_PORT = "mms_port";
        public static final String MMS_PROXY = "mms_proxy";
        public static final String MMS_SERVER = "mms_server";
        public static final String SMS_PREFERRED_BEARER = "sms_preferredBearer";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.regionalphone.RegionalPhone.APN.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public APN() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.regionalphone.RegionalPhone.APN.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.common.regionalphone.RegionalPhone.APN.<init>():void");
        }
    }

    public static final class BROWSER implements BaseColumns {
        public static final String BOOKMARK_TITLE = "bookmarkTitle";
        public static final String BOOKMARK_URL = "bookmarkURL";
        public static final String IS_FOLDER = "folder";
        public static final String MCC_MNC_TIMESTAMP = "mcc_mnc_timestamp";
        public static final String PARENT = "parent";
        public static final String THUMBNAIL = "thumbnail";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.regionalphone.RegionalPhone.BROWSER.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public BROWSER() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.regionalphone.RegionalPhone.BROWSER.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.common.regionalphone.RegionalPhone.BROWSER.<init>():void");
        }
    }

    public static final class MMS_SMS implements BaseColumns {
        public static final String MCC_MNC_TIMESTAMP = "mcc_mnc_timestamp";
        public static final String MMS_CREATION_MODE = "creationMode";
        public static final String SMS_C_NUMBER = "CNumber";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.regionalphone.RegionalPhone.MMS_SMS.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public MMS_SMS() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.regionalphone.RegionalPhone.MMS_SMS.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.common.regionalphone.RegionalPhone.MMS_SMS.<init>():void");
        }
    }

    public static final class SEARCHENGINE implements BaseColumns {
        public static final String ENCODING = "encoding";
        public static final String FAVICON = "favicon";
        public static final String KEYWORD = "keyword";
        public static final String MCC_MNC_TIMESTAMP = "mcc_mnc_timestamp";
        public static final String SEARCH_ENGINE_LABEL = "searchEngineLabel";
        public static final String SEARCH_ENGINE_NAME = "searchEngineName";
        public static final String SEARCH_URL = "searchURL";
        public static final String SUGGESTION_URL = "suggestionURL";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.regionalphone.RegionalPhone.SEARCHENGINE.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public SEARCHENGINE() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.regionalphone.RegionalPhone.SEARCHENGINE.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.common.regionalphone.RegionalPhone.SEARCHENGINE.<init>():void");
        }
    }

    public static final class SETTINGS implements BaseColumns {
        public static final String MCC_MNC_TIMESTAMP = "mcc_mnc_timestamp";
        public static final String NITZ_AUTOUPDATE = "NITZAutoUpdate";
        public static final String WIFI_DEFAULT = "wifi";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.regionalphone.RegionalPhone.SETTINGS.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public SETTINGS() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.regionalphone.RegionalPhone.SETTINGS.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.common.regionalphone.RegionalPhone.SETTINGS.<init>():void");
        }
    }

    public static final class WALLPAPER implements BaseColumns {
        public static final String IMAGE_FILE_NAME = "fileName";
        public static final String MCC_MNC_TIMESTAMP = "mcc_mnc_timestamp";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.regionalphone.RegionalPhone.WALLPAPER.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public WALLPAPER() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.regionalphone.RegionalPhone.WALLPAPER.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.common.regionalphone.RegionalPhone.WALLPAPER.<init>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.regionalphone.RegionalPhone.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.regionalphone.RegionalPhone.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.common.regionalphone.RegionalPhone.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.regionalphone.RegionalPhone.<init>():void, dex: 
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
    public RegionalPhone() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.common.regionalphone.RegionalPhone.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.common.regionalphone.RegionalPhone.<init>():void");
    }
}
