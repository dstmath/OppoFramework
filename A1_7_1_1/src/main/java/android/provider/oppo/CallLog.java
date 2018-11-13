package android.provider.oppo;

import android.content.Context;
import android.net.Uri;
import android.telecom.PhoneAccountHandle;
import com.android.internal.telephony.CallerInfo;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class CallLog {
    public static final String AUTHORITY = "call_log";
    public static final Uri CONTENT_URI = null;
    public static final String PAYPHONE_NUMBER = "-3";
    public static final String PRIVATE_NUMBER = "-2";
    public static final String UNKNOWN_NUMBER = "-1";
    private static boolean isMtkGeminiSupport;
    private static boolean isMtkSupport;

    public static class Calls implements BaseColumns {
        public static final String ALLOW_VOICEMAILS_PARAM_KEY = "allow_voicemails";
        public static final int AUTOREJECTED_TYPE = 5;
        public static final String CACHED_FORMATTED_NUMBER = "formatted_number";
        public static final String CACHED_LOOKUP_URI = "lookup_uri";
        public static final String CACHED_MATCHED_NUMBER = "matched_number";
        public static final String CACHED_NAME = "name";
        public static final String CACHED_NORMALIZED_NUMBER = "normalized_number";
        public static final String CACHED_NUMBER_LABEL = "numberlabel";
        public static final String CACHED_NUMBER_TYPE = "numbertype";
        public static final String CACHED_PHOTO_ID = "photo_id";
        public static final Uri CONTENT_FILTER_URI = null;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/calls";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/calls";
        public static final Uri CONTENT_URI = null;
        public static final Uri CONTENT_URI_WITH_VOICEMAIL = null;
        public static final String COUNTRY_ISO = "countryiso";
        public static final String DATA_ID = "data_id";
        public static final String DATA_USAGE = "data_usage";
        public static final String DATE = "date";
        public static final String DEFAULT_SORT_ORDER = "date DESC";
        public static final String DURATION = "duration";
        public static final String DURATION_TYPE = "duration_type";
        public static final int DURATION_TYPE_ACTIVE = 0;
        public static final int DURATION_TYPE_CALLOUT = 1;
        public static final String EXTRA_CALL_TYPE_FILTER = "android.provider.extra.CALL_TYPE_FILTER";
        public static final String FEATURES = "features";
        public static final int FEATURES_VIDEO = 1;
        public static final String GEOCODED_LOCATION = "geocoded_location";
        public static final int INCOMING_TYPE = 1;
        public static final String IP_PREFIX = "ip_prefix";
        public static final String IS_READ = "is_read";
        public static final String LIMIT_PARAM_KEY = "limit";
        private static final int MIN_DURATION_FOR_NORMALIZED_NUMBER_UPDATE_MS = 10000;
        public static final int MISSED_TYPE = 3;
        public static final String NEW = "new";
        public static final String NUMBER = "number";
        public static final String NUMBER_PRESENTATION = "presentation";
        public static final String OFFSET_PARAM_KEY = "offset";
        public static final int OUTGOING_TYPE = 2;
        public static final String PHONE_ACCOUNT_COMPONENT_NAME = "subscription_component_name";
        public static final String PHONE_ACCOUNT_ID = "subscription_id";
        public static final int PRESENTATION_ALLOWED = 1;
        public static final int PRESENTATION_PAYPHONE = 4;
        public static final int PRESENTATION_RESTRICTED = 2;
        public static final int PRESENTATION_UNKNOWN = 3;
        public static final String PRIVATE_NAME = "private_name";
        public static final String PRIVATE_TYPE = "private_type";
        public static final String RAW_CONTACT_ID = "raw_contact_id";
        public static final String RING_TIME = "ring_time";
        public static final String SIM_ID = "simid";
        public static final String SUB_ID = "sub_id";
        public static final String TRANSCRIPTION = "transcription";
        public static final String TYPE = "type";
        public static final int VOICEMAIL_TYPE = 4;
        public static final String VOICEMAIL_URI = "voicemail_uri";
        public static final String VTCALL = "vtcall";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.oppo.CallLog.Calls.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.oppo.CallLog.Calls.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.oppo.CallLog.Calls.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.oppo.CallLog.Calls.<init>():void, dex: 
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
        public Calls() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.oppo.CallLog.Calls.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.oppo.CallLog.Calls.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: f in method: android.provider.oppo.CallLog.Calls.addCall(com.android.internal.telephony.CallerInfo, android.content.Context, java.lang.String, int, int, int, android.telecom.PhoneAccountHandle, long, int, java.lang.Long, boolean, int, int):android.net.Uri, dex:  in method: android.provider.oppo.CallLog.Calls.addCall(com.android.internal.telephony.CallerInfo, android.content.Context, java.lang.String, int, int, int, android.telecom.PhoneAccountHandle, long, int, java.lang.Long, boolean, int, int):android.net.Uri, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: f in method: android.provider.oppo.CallLog.Calls.addCall(com.android.internal.telephony.CallerInfo, android.content.Context, java.lang.String, int, int, int, android.telecom.PhoneAccountHandle, long, int, java.lang.Long, boolean, int, int):android.net.Uri, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: com.android.dex.DexException: bogus registerCount: f
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public static android.net.Uri addCall(com.android.internal.telephony.CallerInfo r1, android.content.Context r2, java.lang.String r3, int r4, int r5, int r6, android.telecom.PhoneAccountHandle r7, long r8, int r10, java.lang.Long r11, boolean r12, int r13, int r14) {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: f in method: android.provider.oppo.CallLog.Calls.addCall(com.android.internal.telephony.CallerInfo, android.content.Context, java.lang.String, int, int, int, android.telecom.PhoneAccountHandle, long, int, java.lang.Long, boolean, int, int):android.net.Uri, dex:  in method: android.provider.oppo.CallLog.Calls.addCall(com.android.internal.telephony.CallerInfo, android.content.Context, java.lang.String, int, int, int, android.telecom.PhoneAccountHandle, long, int, java.lang.Long, boolean, int, int):android.net.Uri, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.oppo.CallLog.Calls.addCall(com.android.internal.telephony.CallerInfo, android.content.Context, java.lang.String, int, int, int, android.telecom.PhoneAccountHandle, long, int, java.lang.Long, boolean, int, int):android.net.Uri");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.provider.oppo.CallLog.Calls.addCall(com.android.internal.telephony.CallerInfo, android.content.Context, java.lang.String, int, int, long, int, int, int):android.net.Uri, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public static android.net.Uri addCall(com.android.internal.telephony.CallerInfo r1, android.content.Context r2, java.lang.String r3, int r4, int r5, long r6, int r8, int r9, int r10) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.provider.oppo.CallLog.Calls.addCall(com.android.internal.telephony.CallerInfo, android.content.Context, java.lang.String, int, int, long, int, int, int):android.net.Uri, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.oppo.CallLog.Calls.addCall(com.android.internal.telephony.CallerInfo, android.content.Context, java.lang.String, int, int, long, int, int, int):android.net.Uri");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.provider.oppo.CallLog.Calls.addCall(com.android.internal.telephony.CallerInfo, android.content.Context, java.lang.String, int, int, long, int, int, int, int):android.net.Uri, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public static android.net.Uri addCall(com.android.internal.telephony.CallerInfo r1, android.content.Context r2, java.lang.String r3, int r4, int r5, long r6, int r8, int r9, int r10, int r11) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.provider.oppo.CallLog.Calls.addCall(com.android.internal.telephony.CallerInfo, android.content.Context, java.lang.String, int, int, long, int, int, int, int):android.net.Uri, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.oppo.CallLog.Calls.addCall(com.android.internal.telephony.CallerInfo, android.content.Context, java.lang.String, int, int, long, int, int, int, int):android.net.Uri");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.provider.oppo.CallLog.Calls.addCall(com.android.internal.telephony.CallerInfo, android.content.Context, java.lang.String, java.lang.String, int, int, long, int, int, int, int, int):android.net.Uri, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public static android.net.Uri addCall(com.android.internal.telephony.CallerInfo r1, android.content.Context r2, java.lang.String r3, java.lang.String r4, int r5, int r6, long r7, int r9, int r10, int r11, int r12, int r13) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.provider.oppo.CallLog.Calls.addCall(com.android.internal.telephony.CallerInfo, android.content.Context, java.lang.String, java.lang.String, int, int, long, int, int, int, int, int):android.net.Uri, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.oppo.CallLog.Calls.addCall(com.android.internal.telephony.CallerInfo, android.content.Context, java.lang.String, java.lang.String, int, int, long, int, int, int, int, int):android.net.Uri");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.oppo.CallLog.Calls.addEntryAndRemoveExpiredEntries(android.content.Context, android.net.Uri, android.content.ContentValues):android.net.Uri, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private static android.net.Uri addEntryAndRemoveExpiredEntries(android.content.Context r1, android.net.Uri r2, android.content.ContentValues r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.oppo.CallLog.Calls.addEntryAndRemoveExpiredEntries(android.content.Context, android.net.Uri, android.content.ContentValues):android.net.Uri, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.oppo.CallLog.Calls.addEntryAndRemoveExpiredEntries(android.content.Context, android.net.Uri, android.content.ContentValues):android.net.Uri");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.oppo.CallLog.Calls.getCurrentCountryIso(android.content.Context):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private static java.lang.String getCurrentCountryIso(android.content.Context r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.oppo.CallLog.Calls.getCurrentCountryIso(android.content.Context):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.oppo.CallLog.Calls.getCurrentCountryIso(android.content.Context):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.oppo.CallLog.Calls.getLastOutgoingCall(android.content.Context):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public static java.lang.String getLastOutgoingCall(android.content.Context r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.oppo.CallLog.Calls.getLastOutgoingCall(android.content.Context):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.oppo.CallLog.Calls.getLastOutgoingCall(android.content.Context):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.oppo.CallLog.Calls.removeExpiredEntries(android.content.Context):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private static void removeExpiredEntries(android.content.Context r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.oppo.CallLog.Calls.removeExpiredEntries(android.content.Context):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.oppo.CallLog.Calls.removeExpiredEntries(android.content.Context):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.oppo.CallLog.Calls.updateDataUsageStatForData(android.content.ContentResolver, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private static void updateDataUsageStatForData(android.content.ContentResolver r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.oppo.CallLog.Calls.updateDataUsageStatForData(android.content.ContentResolver, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.oppo.CallLog.Calls.updateDataUsageStatForData(android.content.ContentResolver, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.oppo.CallLog.Calls.updateNormalizedNumber(android.content.Context, android.content.ContentResolver, java.lang.String, java.lang.String):void, dex: 
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
        private static void updateNormalizedNumber(android.content.Context r1, android.content.ContentResolver r2, java.lang.String r3, java.lang.String r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.oppo.CallLog.Calls.updateNormalizedNumber(android.content.Context, android.content.ContentResolver, java.lang.String, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.oppo.CallLog.Calls.updateNormalizedNumber(android.content.Context, android.content.ContentResolver, java.lang.String, java.lang.String):void");
        }

        public static Uri addCall(CallerInfo ci, Context context, String number, int presentation, int callType, int features, PhoneAccountHandle accountHandle, long start, int duration, Long dataUsage) {
            return addCall(ci, context, number, presentation, callType, features, accountHandle, start, duration, dataUsage, false);
        }

        public static Uri addCall(CallerInfo ci, Context context, String number, int presentation, int callType, int features, PhoneAccountHandle accountHandle, long start, int duration, Long dataUsage, boolean addForAllUsers) {
            return addCall(ci, context, number, presentation, callType, features, accountHandle, start, duration, dataUsage, addForAllUsers, 1, 0);
        }

        public static Uri addCall(CallerInfo ci, Context context, String number, int presentation, int callType, long start, int duration) {
            return addCall(ci, context, number, presentation, callType, start, duration, -1, -1);
        }

        public static Uri addCall(CallerInfo ci, Context context, String number, int presentation, int callType, long start, int duration, int simId) {
            return addCall(ci, context, number, presentation, callType, start, duration, simId, -1);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.oppo.CallLog.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.oppo.CallLog.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.provider.oppo.CallLog.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.oppo.CallLog.<init>():void, dex: 
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
    public CallLog() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.oppo.CallLog.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.provider.oppo.CallLog.<init>():void");
    }
}
