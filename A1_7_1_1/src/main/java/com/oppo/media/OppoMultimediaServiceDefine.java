package com.oppo.media;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OppoMultimediaServiceDefine {
    public static final String ACTION_AUDIO_DEVICE_ROUTE_CHANGED = "android.media.ACTION_AUDIO_DEVICE_ROUTE_CHANGED";
    public static final String ACTION_AUDIO_RECORD_INVALID = "android.media.ACTION_AUDIO_RECORD_INVALID";
    public static final String ACTION_AUDIO_RECORD_START = "android.media.ACTION_AUDIO_RECORD_START";
    public static final String ACTION_AUDIO_RECORD_STOP = "android.media.ACTION_AUDIO_RECORD_STOP";
    public static final String APP_LIST_ATTRIBUTE_FORBID = "forbid";
    public static final String APP_LIST_ATTRIBUTE_KILL = "kill";
    public static final String APP_LIST_ATTRIBUTE_NO_NEED_MUTE_IN_CALL = "no_need_mute_in_call";
    public static final String APP_LIST_ATTRIBUTE_NO_NEED_MUTE_IN_RINGING = "no_need_mute_in_ringing";
    public static final String APP_LIST_ATTRIBUTE_RECORD_NO_HINT = "record-no-hint";
    public static final String APP_LIST_ATTRIBUTE_SCO_ON_DELAY = "sco-on-delay";
    public static final String APP_LIST_ATTRIBUTE_STREAMMUTE = "streammute";
    public static final String BLUETOOTH_PACKAGE_NAME = "com.android.bluetooth";
    public static final String CAMERA_PACKAGE_NAME = "com.oppo.camera";
    public static final String DAEMON_LIST_MODULE_INDEX_CONTROL = "2";
    public static final String DAEMON_LIST_MODULE_INDEX_DIRAC = "0";
    public static final String DAEMON_LIST_MODULE_INDEX_MEDIA_CODEC = "6";
    public static final String DAEMON_LIST_MODULE_INDEX_VOLUME = "1";
    public static final String DAEMON_LIST_MODULE_INDEX_ZENMODE = "3";
    public static final int EVENT_LOCALSERVICE_DEFAULT = 100;
    public static final int EVENT_LOCALSERVICE_KILL_APP = 107;
    public static final int EVENT_LOCALSERVICE_KILL_AUDIOSYSTEM = 105;
    public static final int EVENT_LOCALSERVICE_MUTE_STREAM = 106;
    public static final int EVENT_LOCALSERVICE_RECORD_INVALID = 101;
    public static final int EVENT_LOCALSERVICE_RELEASE_AUDIOTRACK = 108;
    public static final int EVENT_LOCALSERVICE_SETMODE = 102;
    public static final int EVENT_LOCALSERVICE_SETSPEAKERPHONEON = 104;
    public static final int EVENT_LOCALSERVICE_STREAMTYPE_ADJUST = 103;
    public static final int EVENT_LOCALSERVICE_VOICE_SMALL = 109;
    public static final String EXTRA_DEVICE_STATE = "android.media.EXTRA_DEVICE_STATE";
    public static final String EXTRA_DEVICE_TYPE = "android.media.EXTRA_DEVICE_TYPE";
    public static final String EXTRA_RECORD_ACTION_PID = "android.media.EXTRA_RECORD_ACTION_PID";
    public static final String EXTRA_RECORD_START_PACKAGE_TYPE = "android.media.EXTRA_RECORD_START_PACKAGE_TYPE";
    public static final String FEATURE_RECORD_CONFLICT_NAME = "oppo.multimedia.record.conflict";
    public static final String KEY_AUDIO_CHECK_DAEMON_LISTINFO_BYNAME = "check_daemon_listinfo_byname";
    public static final String KEY_AUDIO_CHECK_DAEMON_LISTINFO_BYPID = "check_daemon_listinfo_bypid";
    public static final String KEY_AUDIO_GET_ADJUSTSTREAMVOLUME_CONTROL_STATE = "get_adjustStreamVolume_control_status";
    public static final String KEY_AUDIO_GET_ALLMODES = "allmodes";
    public static final String KEY_AUDIO_GET_ALLSESSION = "allsessions";
    public static final String KEY_AUDIO_GET_APPLICATION_TOPACTIVITY = "get_app_topactivity";
    public static final String KEY_AUDIO_GET_AUDIO_INFO = "get_audioinfos";
    public static final String KEY_AUDIO_GET_DAEMON_LISTINFO_BYNAME = "get_daemon_listinfo_byname";
    public static final String KEY_AUDIO_GET_DAEMON_LISTINFO_BYPID = "get_daemon_listinfo_bypid";
    public static final String KEY_AUDIO_GET_DEVICE_CHANGE_AUTHORITY = "get_device_change_authority";
    public static final String KEY_AUDIO_GET_EXEC_COMMAND_INFO = "get_exec_command_info";
    public static final String KEY_AUDIO_GET_PLAYBACK_INFOS = "get_playback_infos";
    public static final String KEY_AUDIO_GET_RECORD_FAILED_INFO = "get_record_failed_info";
    public static final String KEY_AUDIO_GET_RECORD_INFOS = "get_record_infos";
    public static final String KEY_AUDIO_GET_RECORD_STATE = "get_record_status";
    public static final String KEY_AUDIO_GET_SPEAKER_AUTHORITY = "get_speaker_authority";
    public static final String KEY_AUDIO_GET_STREAMTYPE_ADJUST_REVISE = "streamtype_adjust_revise";
    public static final String KEY_AUDIO_GET_VOICE_SMALL_INFO = "get_voice_small_info";
    public static final String KEY_AUDIO_GET_VOLUME_CONTROL_STATE = "get_volume_control_status";
    public static final int MSG_AUDIO_CHECK_AUDIOSYSTEM = 13;
    public static final int MSG_AUDIO_DEFAULT = 0;
    public static final int MSG_AUDIO_DEVICE_ROUTE_CHANGED = 12;
    public static final int MSG_AUDIO_MMLIST_UPDATE = 15;
    public static final int MSG_AUDIO_MUSIC_MUTE = 16;
    public static final int MSG_AUDIO_MUSIC_UNMUTE = 17;
    public static final int MSG_AUDIO_RECORD_INVALID = 10;
    public static final int MSG_AUDIO_RECORD_START = 8;
    public static final int MSG_AUDIO_RECORD_STOP = 9;
    public static final int MSG_AUDIO_START_LOCALSERVICE = 300;
    public static final int MSG_AUDIO_SYNC_AUDIO_MODE = 14;
    public static final int MSG_AUDIO_TRACK_ALARM = 11;
    public static final int MSG_AUDIO_TRACK_CREATE = 1;
    public static final int MSG_AUDIO_TRACK_DESTROY = 2;
    public static final int MSG_AUDIO_VOICE_SMALL = 18;
    public static final int MSG_DISPLAY_DEFAULT = 20;
    public static final int MSG_DISPLAY_JUNK_REPORT = 21;
    public static final int MSG_GET_APPS_MODE = 4;
    public static final int MSG_RECORD_INVALID = 200;
    public static final int MSG_SET_APPS_MODE = 5;
    public static final int MSG_SET_APPS_SESSION = 6;
    public static final int MSG_SET_INFO = 3;
    public static final int MSG_SET_MODE_ONLY_READ = 7;
    public static final String TEL_PACKAGE_NAME = "com.android.server.telecom";

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
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum DaemonFun {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.media.OppoMultimediaServiceDefine.DaemonFun.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.media.OppoMultimediaServiceDefine.DaemonFun.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.OppoMultimediaServiceDefine.DaemonFun.<clinit>():void");
        }
    }

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
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum ModuleTag {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.media.OppoMultimediaServiceDefine.ModuleTag.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.media.OppoMultimediaServiceDefine.ModuleTag.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.OppoMultimediaServiceDefine.ModuleTag.<clinit>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.media.OppoMultimediaServiceDefine.<init>():void, dex: 
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
    public OppoMultimediaServiceDefine() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.media.OppoMultimediaServiceDefine.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.media.OppoMultimediaServiceDefine.<init>():void");
    }
}
