package mediatek.telephony;

public class MtkDisconnectCause {
    public static final int CAUSE_ADDRESS_INCOMPLETE = 1524;
    public static final int CAUSE_AMBIGUOUS = 1525;
    public static final int CAUSE_BAD_EXTENSION = 1517;
    public static final int CAUSE_BAD_GATEWAY = 1531;
    public static final int CAUSE_BAD_REQUEST = 1501;
    public static final int CAUSE_BUSY_EVERYWHERE = 1536;
    public static final int CAUSE_BUSY_HERE = 1526;
    public static final int CAUSE_CALL_TRANSACTION_NOT_EXIST = 1521;
    public static final int CAUSE_CONFLICT = 1510;
    public static final int CAUSE_DECLINE = 1537;
    public static final int CAUSE_DOES_NOT_EXIST_ANYWHERE = 1538;
    public static final int CAUSE_EXTENSION_REQUIRED = 1518;
    public static final int CAUSE_FORBIDDEN = 1504;
    public static final int CAUSE_GATEWAY_TIMEOUT = 1533;
    public static final int CAUSE_GONE = 1511;
    public static final int CAUSE_INTERVAL_TOO_BRIEF = 1519;
    public static final int CAUSE_LENGTH_REQUIRED = 1512;
    public static final int CAUSE_LOOP_DETECTED = 1522;
    public static final int CAUSE_MESSAGE_TOO_LONG = 1535;
    public static final int CAUSE_METHOD_NOT_ALLOWED = 1506;
    public static final int CAUSE_MOVED_PERMANENTLY = 1500;
    public static final int CAUSE_NOT_ACCEPTABLE = 1507;
    public static final int CAUSE_NOT_ACCEPTABLE_HERE = 1528;
    public static final int CAUSE_NOT_FOUND = 1505;
    public static final int CAUSE_NOT_IMPLEMENTED = 1530;
    public static final int CAUSE_PAYMENT_REQUIRED = 1503;
    public static final int CAUSE_PROXY_AUTHENTICATION_REQUIRED = 1508;
    public static final int CAUSE_REQUEST_ENTRY_TOO_LONG = 1513;
    public static final int CAUSE_REQUEST_TERMINATED = 1527;
    public static final int CAUSE_REQUEST_TIMEOUT = 1509;
    public static final int CAUSE_REQUEST_URI_TOO_LONG = 1514;
    public static final int CAUSE_SERVER_INTERNAL_ERROR = 1529;
    public static final int CAUSE_SERVICE_UNAVAILABLE = 1532;
    public static final int CAUSE_SESSION_NOT_ACCEPTABLE = 1539;
    public static final int CAUSE_TEMPORARILY_UNAVAILABLE = 1520;
    public static final int CAUSE_TOO_MANY_HOPS = 1523;
    public static final int CAUSE_UNAUTHORIZED = 1502;
    public static final int CAUSE_UNSUPPORTED_MEDIA_TYPE = 1515;
    public static final int CAUSE_UNSUPPORTED_URI_SCHEME = 1516;
    public static final int CAUSE_VERSION_NOT_SUPPORTED = 1534;
    public static final int ECC_OVER_WIFI_UNSUPPORTED = 1002;
    public static final int INCOMING_REJECTED_FORWARD = 1008;
    public static final int INCOMING_REJECTED_LOW_BATTERY = 1010;
    public static final int INCOMING_REJECTED_NO_COVERAGE = 1009;
    public static final int INCOMING_REJECTED_NO_FORWARD = 1007;
    public static final int INCOMING_REJECTED_SPECIAL_HANGUP = 1011;
    public static final int MTK_DISCONNECTED_CAUSE_BASE = 1000;
    public static final int OUTGOING_CANCELED_BY_SERVICE = 1001;
    public static final int VOLTE_SS_DATA_OFF = 1004;
    public static final int WFC_CALL_DROP_BACKHAUL_CONGESTION = 1006;
    public static final int WFC_CALL_DROP_BAD_RSSI = 1005;
    public static final int WFC_HANDOVER_LTE_FAIL = 403;
    public static final int WFC_HANDOVER_WIFI_FAIL = 402;
    public static final int WFC_ISP_PROBLEM = 401;
    public static final int WFC_UNAVAILABLE_IN_CURRENT_LOCATION = 1003;
    public static final int WFC_WIFI_SIGNAL_LOST = 400;

    public static String toString(int cause) {
        switch (cause) {
            case 1001:
                return "OUTGOING_CANCELED_BY_SERVICE";
            case 1002:
                return "ECC_OVER_WIFI_UNSUPPORTED";
            case 1003:
                return "WFC_UNAVAILABLE_IN_CURRENT_LOCATION";
            case 1004:
                return "VOLTE_SS_DATA_OFF";
            case 1005:
                return "WFC_CALL_DROP_BAD_RSSI";
            case 1006:
                return "WFC_CALL_DROP_BACKHAUL_CONGESTION";
            case 1007:
                return "INCOMING_REJECTED_NO_FORWARD";
            case 1008:
                return "INCOMING_REJECTED_FORWARD";
            case 1009:
                return "INCOMING_REJECTED_NO_COVERAGE";
            case 1010:
                return "INCOMING_REJECTED_LOW_BATTERY";
            case 1011:
                return "INCOMING_REJECTED_SPECIAL_HANGUP";
            default:
                switch (cause) {
                    case CAUSE_MOVED_PERMANENTLY /*{ENCODED_INT: 1500}*/:
                        return "CAUSE_MOVED_PERMANENTLY";
                    case CAUSE_BAD_REQUEST /*{ENCODED_INT: 1501}*/:
                        return "CAUSE_BAD_REQUEST";
                    case CAUSE_UNAUTHORIZED /*{ENCODED_INT: 1502}*/:
                        return "CAUSE_UNAUTHORIZED";
                    case CAUSE_PAYMENT_REQUIRED /*{ENCODED_INT: 1503}*/:
                        return "CAUSE_PAYMENT_REQUIRED";
                    case CAUSE_FORBIDDEN /*{ENCODED_INT: 1504}*/:
                        return "CAUSE_FORBIDDEN";
                    case CAUSE_NOT_FOUND /*{ENCODED_INT: 1505}*/:
                        return "CAUSE_NOT_FOUND";
                    case CAUSE_METHOD_NOT_ALLOWED /*{ENCODED_INT: 1506}*/:
                        return "CAUSE_METHOD_NOT_ALLOWED";
                    case CAUSE_NOT_ACCEPTABLE /*{ENCODED_INT: 1507}*/:
                        return "CAUSE_NOT_ACCEPTABLE";
                    case CAUSE_PROXY_AUTHENTICATION_REQUIRED /*{ENCODED_INT: 1508}*/:
                        return "CAUSE_PROXY_AUTHENTICATION_REQUIRED";
                    case CAUSE_REQUEST_TIMEOUT /*{ENCODED_INT: 1509}*/:
                        return "CAUSE_REQUEST_TIMEOUT";
                    case CAUSE_CONFLICT /*{ENCODED_INT: 1510}*/:
                        return "CAUSE_CONFLICT";
                    case CAUSE_GONE /*{ENCODED_INT: 1511}*/:
                        return "CAUSE_GONE";
                    case CAUSE_LENGTH_REQUIRED /*{ENCODED_INT: 1512}*/:
                        return "CAUSE_LENGTH_REQUIRED";
                    case CAUSE_REQUEST_ENTRY_TOO_LONG /*{ENCODED_INT: 1513}*/:
                        return "CAUSE_REQUEST_ENTRY_TOO_LONG";
                    case CAUSE_REQUEST_URI_TOO_LONG /*{ENCODED_INT: 1514}*/:
                        return "CAUSE_REQUEST_URI_TOO_LONG";
                    case CAUSE_UNSUPPORTED_MEDIA_TYPE /*{ENCODED_INT: 1515}*/:
                        return "CAUSE_UNSUPPORTED_MEDIA_TYPE";
                    case CAUSE_UNSUPPORTED_URI_SCHEME /*{ENCODED_INT: 1516}*/:
                        return "CAUSE_UNSUPPORTED_URI_SCHEME";
                    case CAUSE_BAD_EXTENSION /*{ENCODED_INT: 1517}*/:
                        return "CAUSE_BAD_EXTENSION";
                    case CAUSE_EXTENSION_REQUIRED /*{ENCODED_INT: 1518}*/:
                        return "CAUSE_EXTENSION_REQUIRED";
                    case CAUSE_INTERVAL_TOO_BRIEF /*{ENCODED_INT: 1519}*/:
                        return "CAUSE_INTERVAL_TOO_BRIEF";
                    case CAUSE_TEMPORARILY_UNAVAILABLE /*{ENCODED_INT: 1520}*/:
                        return "CAUSE_TEMPORARILY_UNAVAILABLE";
                    case CAUSE_CALL_TRANSACTION_NOT_EXIST /*{ENCODED_INT: 1521}*/:
                        return "CAUSE_CALL_TRANSACTION_NOT_EXIST";
                    case CAUSE_LOOP_DETECTED /*{ENCODED_INT: 1522}*/:
                        return "CAUSE_LOOP_DETECTED";
                    case CAUSE_TOO_MANY_HOPS /*{ENCODED_INT: 1523}*/:
                        return "CAUSE_TOO_MANY_HOPS";
                    case CAUSE_ADDRESS_INCOMPLETE /*{ENCODED_INT: 1524}*/:
                        return "CAUSE_ADDRESS_INCOMPLETE";
                    case CAUSE_AMBIGUOUS /*{ENCODED_INT: 1525}*/:
                        return "CAUSE_AMBIGUOUS";
                    case CAUSE_BUSY_HERE /*{ENCODED_INT: 1526}*/:
                        return "CAUSE_BUSY_HERE";
                    case CAUSE_REQUEST_TERMINATED /*{ENCODED_INT: 1527}*/:
                        return "CAUSE_REQUEST_TERMINATED";
                    case CAUSE_NOT_ACCEPTABLE_HERE /*{ENCODED_INT: 1528}*/:
                        return "CAUSE_NOT_ACCEPTABLE_HERE";
                    case CAUSE_SERVER_INTERNAL_ERROR /*{ENCODED_INT: 1529}*/:
                        return "CAUSE_SERVER_INTERNAL_ERROR";
                    case CAUSE_NOT_IMPLEMENTED /*{ENCODED_INT: 1530}*/:
                        return "CAUSE_NOT_IMPLEMENTED";
                    case CAUSE_BAD_GATEWAY /*{ENCODED_INT: 1531}*/:
                        return "CAUSE_BAD_GATEWAY";
                    case CAUSE_SERVICE_UNAVAILABLE /*{ENCODED_INT: 1532}*/:
                        return "CAUSE_SERVICE_UNAVAILABLE";
                    case CAUSE_GATEWAY_TIMEOUT /*{ENCODED_INT: 1533}*/:
                        return "CAUSE_GATEWAY_TIMEOUT";
                    case CAUSE_VERSION_NOT_SUPPORTED /*{ENCODED_INT: 1534}*/:
                        return "CAUSE_VERSION_NOT_SUPPORTED";
                    case CAUSE_MESSAGE_TOO_LONG /*{ENCODED_INT: 1535}*/:
                        return "CAUSE_MESSAGE_TOO_LONG";
                    case CAUSE_BUSY_EVERYWHERE /*{ENCODED_INT: 1536}*/:
                        return "CAUSE_BUSY_EVERYWHERE";
                    case CAUSE_DECLINE /*{ENCODED_INT: 1537}*/:
                        return "CAUSE_DECLINE";
                    case CAUSE_DOES_NOT_EXIST_ANYWHERE /*{ENCODED_INT: 1538}*/:
                        return "CAUSE_DOES_NOT_EXIST_ANYWHERE";
                    case CAUSE_SESSION_NOT_ACCEPTABLE /*{ENCODED_INT: 1539}*/:
                        return "CAUSE_SESSION_NOT_ACCEPTABLE";
                    default:
                        return "UNKNOWN";
                }
        }
    }
}
