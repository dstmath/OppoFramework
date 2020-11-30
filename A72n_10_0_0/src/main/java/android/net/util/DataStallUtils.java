package android.net.util;

public class DataStallUtils {
    public static final String CONFIG_DATA_STALL_CONSECUTIVE_DNS_TIMEOUT_THRESHOLD = "data_stall_consecutive_dns_timeout_threshold";
    public static final String CONFIG_DATA_STALL_EVALUATION_TYPE = "data_stall_evaluation_type";
    public static final String CONFIG_DATA_STALL_MIN_EVALUATE_INTERVAL = "data_stall_min_evaluate_interval";
    public static String CONFIG_DATA_STALL_TCP_POLLING_INTERVAL = "data_stall_tcp_polling_interval";
    public static final String CONFIG_DATA_STALL_VALID_DNS_TIME_THRESHOLD = "data_stall_valid_dns_time_threshold";
    public static final String CONFIG_MIN_PACKETS_THRESHOLD = "tcp_min_packets_threshold";
    public static final String CONFIG_TCP_PACKETS_FAIL_PERCENTAGE = "tcp_packets_fail_percentage";
    public static final int DATA_STALL_EVALUATION_TYPE_DNS = 1;
    public static final int DATA_STALL_EVALUATION_TYPE_TCP = 2;
    public static final int DEFAULT_CONSECUTIVE_DNS_TIMEOUT_THRESHOLD = 5;
    public static final int DEFAULT_DATA_STALL_EVALUATION_TYPES = 3;
    public static final int DEFAULT_DATA_STALL_MIN_EVALUATE_TIME_MS = 60000;
    public static final int DEFAULT_DATA_STALL_MIN_PACKETS_THRESHOLD = 10;
    public static final int DEFAULT_DATA_STALL_VALID_DNS_TIME_THRESHOLD_MS = 1800000;
    public static final int DEFAULT_DNS_LOG_SIZE = 20;
    public static final int DEFAULT_TCP_PACKETS_FAIL_PERCENTAGE = 80;
    public static int DEFAULT_TCP_POLLING_INTERVAL_MS = 10000;
    public static final int TCP_ESTABLISHED = 1;
    public static final int TCP_MONITOR_STATE_FILTER = 14;
    public static final int TCP_SYN_RECV = 3;
    public static final int TCP_SYN_SENT = 2;
}
