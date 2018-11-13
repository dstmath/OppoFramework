package android.telephony;

public class DisconnectCause {
    public static final int ACCESS_INFORMATION_DISCARDED = 80;
    public static final int ANSWERED_ELSEWHERE = 52;
    public static final int BEARER_NOT_AUTHORIZED = 67;
    public static final int BEARER_NOT_AVAIL = 68;
    public static final int BEARER_NOT_IMPLEMENT = 70;
    public static final int BUSY = 4;
    public static final int CALL_BARRED = 20;
    public static final int CALL_PULLED = 51;
    public static final int CALL_REJECTED = 60;
    public static final int CDMA_ACCESS_BLOCKED = 35;
    public static final int CDMA_ACCESS_FAILURE = 32;
    public static final int CDMA_ALREADY_ACTIVATED = 49;
    public static final int CDMA_CALL_LOST = 41;
    public static final int CDMA_DROP = 27;
    public static final int CDMA_INTERCEPT = 28;
    public static final int CDMA_LOCKED_UNTIL_POWER_CYCLE = 26;
    public static final int CDMA_NOT_EMERGENCY = 34;
    public static final int CDMA_PREEMPTED = 33;
    public static final int CDMA_REORDER = 29;
    public static final int CDMA_RETRY_ORDER = 31;
    public static final int CDMA_SO_REJECT = 30;
    public static final int CHANNEL_UNACCEPTABLE = 75;
    public static final int CM_MM_RR_CONNECTION_RELEASE = 96;
    public static final int CONDITIONAL_IE_ERROR = 91;
    public static final int CONGESTION = 5;
    public static final int CS_RESTRICTED = 22;
    public static final int CS_RESTRICTED_EMERGENCY = 24;
    public static final int CS_RESTRICTED_NORMAL = 23;
    public static final int DATA_DISABLED = 54;
    public static final int DATA_LIMIT_REACHED = 55;
    public static final int DESTINATION_OUT_OF_ORDER = 79;
    public static final int DIALED_MMI = 39;
    public static final int DIALED_ON_WRONG_SLOT = 56;
    public static final int DIAL_MODIFIED_TO_DIAL = 48;
    public static final int DIAL_MODIFIED_TO_SS = 47;
    public static final int DIAL_MODIFIED_TO_USSD = 46;
    public static final int ECC_OVER_WIFI_UNSUPPORTED = 404;
    public static final int EMERGENCY_ONLY = 37;
    public static final int ERROR_UNSPECIFIED = 36;
    public static final int EXITED_ECM = 42;
    public static final int FACILITY_NOT_IMPLEMENT = 71;
    public static final int FACILITY_REJECTED = 62;
    public static final int FDN_BLOCKED = 21;
    public static final int ICC_ERROR = 19;
    public static final int IE_NON_EXISTENT_OR_NOT_IMPLEMENTED = 90;
    public static final int IMS_EMERGENCY_REREG = 380;
    public static final int IMS_MERGED_SUCCESSFULLY = 45;
    public static final int INCOMING_CALL_BARRED_WITHIN_CUG = 82;
    public static final int INCOMING_MISSED = 1;
    public static final int INCOMING_REJECTED = 16;
    public static final int INCOMPATIBLE_DESTINATION = 74;
    public static final int INTERWORKING_UNSPECIFIED = 95;
    public static final int INVALID_CREDENTIALS = 10;
    public static final int INVALID_MANDATORY_INFORMATION = 87;
    public static final int INVALID_NUMBER = 7;
    public static final int INVALID_NUMBER_FORMAT = 61;
    public static final int INVALID_TRANSACTION_ID_VALUE = 83;
    public static final int INVALID_TRANSIT_NETWORK_SELECTION = 85;
    public static final int LIMIT_EXCEEDED = 15;
    public static final int LOCAL = 3;
    public static final int LOST_SIGNAL = 14;
    public static final int MAXIMUM_NUMBER_OF_CALLS_REACHED = 53;
    public static final int MAXIMUM_VALID_VALUE = 56;
    public static final int MESSAGE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE = 92;
    public static final int MESSAGE_TYPE_NON_EXISTENT = 88;
    public static final int MESSAGE_TYPE_NOT_COMPATIBLE_WITH_PROT_STATE = 89;
    public static final int MINIMUM_VALID_VALUE = 0;
    public static final int MMI = 6;
    public static final int MTK_DISCONNECTED_CAUSE_BASE = 56;
    public static final int NON_SELECTED_USER_CLEARING = 78;
    public static final int NORMAL = 2;
    public static final int NORMAL_UNSPECIFIED = 63;
    public static final int NOT_DISCONNECTED = 0;
    public static final int NOT_VALID = -1;
    public static final int NO_CIRCUIT_AVAIL = 64;
    public static final int NO_PHONE_NUMBER_SUPPLIED = 38;
    public static final int NO_ROUTE_TO_DESTINATION = 57;
    public static final int NO_USER_RESPONDING = 58;
    public static final int NUMBER_UNREACHABLE = 8;
    public static final int OEM_SWITCH_PHONE = 299;
    public static final int OPERATOR_DETERMINED_BARRING = 76;
    public static final int OPTION_NOT_AVAILABLE = 73;
    public static final int OUTGOING_CANCELED = 44;
    public static final int OUTGOING_CANCELED_BY_SERVICE = 97;
    public static final int OUTGOING_FAILURE = 43;
    public static final int OUT_OF_NETWORK = 11;
    public static final int OUT_OF_SERVICE = 18;
    public static final int POWER_OFF = 17;
    public static final int PRE_EMPTION = 77;
    public static final int PROTOCOL_ERROR_UNSPECIFIED = 94;
    public static final int RECOVERY_ON_TIMER_EXPIRY = 93;
    public static final int REQUESTED_FACILITY_NOT_SUBSCRIBED = 81;
    public static final int RESOURCE_UNAVAILABLE = 66;
    public static final int RESTRICTED_BEARER_AVAILABLE = 72;
    public static final int SEMANTICALLY_INCORRECT_MESSAGE = 86;
    public static final int SERVER_ERROR = 12;
    public static final int SERVER_UNREACHABLE = 9;
    public static final int SERVICE_NOT_AVAILABLE = 69;
    public static final int SWITCHING_CONGESTION = 65;
    public static final int TIMED_OUT = 13;
    public static final int UNOBTAINABLE_NUMBER = 25;
    public static final int USER_ALERTING_NO_ANSWER = 59;
    public static final int USER_NOT_MEMBER_OF_CUG = 84;
    public static final int VIDEO_CALL_NOT_ALLOWED_WHILE_TTY_ENABLED = 50;
    public static final int VOICEMAIL_NUMBER_MISSING = 40;
    public static final int VOLTE_SS_DATA_OFF = 98;
    public static final int WFC_HANDOVER_LTE_FAIL = 403;
    public static final int WFC_HANDOVER_WIFI_FAIL = 402;
    public static final int WFC_ISP_PROBLEM = 401;
    public static final int WFC_UNAVAILABLE_IN_CURRENT_LOCATION = 405;
    public static final int WFC_WIFI_SIGNAL_LOST = 400;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.DisconnectCause.toString(int):java.lang.String, dex: 
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
    public static java.lang.String toString(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.DisconnectCause.toString(int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.DisconnectCause.toString(int):java.lang.String");
    }

    private DisconnectCause() {
    }
}
