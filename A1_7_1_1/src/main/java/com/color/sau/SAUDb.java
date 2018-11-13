package com.color.sau;

import android.net.Uri;
import android.provider.BaseColumns;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SAUDb {
    public static final String AUTHORITY = "com.coloros.sau.db";

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static final class UpdateInfoColumns implements BaseColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.oppo.update_info";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.oppo.update_info";
        public static final Uri CONTENT_URI = null;
        public static final String _ALL_SIZE = "all_size";
        public static final String _DESCRIPTION = "description";
        public static final String _DOWNLOADED_SIZE = "downloaded_size";
        public static final String _DOWNLOAD_FINISHED = "download_finished";
        public static final String _ERROR_TYPE = "error_type";
        public static final String _FILE_NAME = "file_name";
        public static final String _FORCE_DOWNLOAD = "force_download";
        public static final String _FORCE_INSTALL = "force_install";
        public static final String _ICON_EXISTS = "icon_exists";
        public static final String _INSTALL_FINISHED = "install_finished";
        public static final String _MD5_ALL = "md5_all";
        public static final String _MD5_PATCH = "md5_patch";
        public static final String _NEW_VERSION_CODE = "new_version_code";
        public static final String _NEW_VERSION_NAME = "new_version_name";
        public static final String _OLD_FILE_DIR = "old_file_dir";
        public static final String _PATCH_FILE_NAME = "patch_file_name";
        public static final String _PATCH_FINISHED = "patch_finished";
        public static final String _PKG_NAME = "pkg_name";
        public static final String _SAU_TYPE = "sau_type";
        public static final String _SILENT_UPDATING_STATUS = "status_updating";
        public static final String _SIZE = "size";
        public static final String _TYPE = "type";
        public static final String _UPGRADE_STATUS = "upgrade_status";
        public static final String _URL = "url";
        public static final String _USE_OLD = "can_use_old";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.sau.SAUDb.UpdateInfoColumns.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.sau.SAUDb.UpdateInfoColumns.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.sau.SAUDb.UpdateInfoColumns.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.sau.SAUDb.UpdateInfoColumns.<init>():void, dex: 
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
        public UpdateInfoColumns() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.sau.SAUDb.UpdateInfoColumns.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.sau.SAUDb.UpdateInfoColumns.<init>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.sau.SAUDb.<init>():void, dex: 
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
    public SAUDb() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.sau.SAUDb.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.sau.SAUDb.<init>():void");
    }
}
