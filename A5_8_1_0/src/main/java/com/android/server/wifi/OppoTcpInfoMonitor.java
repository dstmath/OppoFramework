package com.android.server.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class OppoTcpInfoMonitor {
    private static final String ATO_STR = "ato=";
    private static final String CA_STATE_STR = "ca_state=";
    private static boolean DBG = false;
    private static final String LAST_A_R_STR = "last_a_r=";
    private static final String LAST_D_R_STR = "last_d_r=";
    private static final String LAST_D_S_STR = "last_d_s=";
    private static final String OPTION_STR = "options=";
    private static final String PRINT_CMD = "cat proc/sys/net/ipv4/tcp_info_print";
    private static final String RCV_SPACE_STR = "rcv_space=";
    private static final String RTO_STR = "rto=";
    private static final String RTT_STR = "rtt=";
    private static final String STATE_STR = "state=";
    private static final String TAG = "OppoTcpInfoMonitor";
    private static final long TCP_AGE_THRESHOLD = 30000;
    public static final int TCP_LINK_FAIR = 17;
    public static final int TCP_LINK_GOOD = 16;
    public static final int TCP_LINK_LOST = 2;
    public static final int TCP_LINK_OFF = 1;
    public static final int TCP_LINK_POOR = 18;
    public static final int TCP_LINK_RESTRICTED = 3;
    public static final int TCP_LINK_UNSPECIFIED = 0;
    private static final long TCP_RTO_THRESHOLD = 1000000;
    private static final long TCP_RTT_THRESHOLD = 1000000;
    private static final String T_RETRANS_STR = "t_retrans=";
    private static final String UNACKED_STR = "unacked=";
    public static final int VAILD_LINK_COUNT = 3;
    private Context mContext;
    private int mStatus = 0;
    private Handler mTcpHandler;
    private List<TcpSocketInfo> mTcpSocketInfo = new ArrayList();

    private class TcpSocketInfo {
        private static final int TCP_CA_CWR = 2;
        private static final int TCP_CA_Disorder = 1;
        private static final int TCP_CA_Loss = 4;
        private static final int TCP_CA_Open = 0;
        private static final int TCP_CA_Recovery = 3;
        private static final int TCP_CLOSE = 7;
        private static final int TCP_CLOSE_WAIT = 8;
        private static final int TCP_CLOSING = 11;
        private static final int TCP_ESTABLISHED = 1;
        private static final int TCP_FIN_WAIT1 = 4;
        private static final int TCP_FIN_WAIT2 = 5;
        private static final int TCP_LAST_ACK = 9;
        private static final int TCP_LISTEN = 10;
        private static final int TCP_SYN_RECV = 3;
        private static final int TCP_SYN_SENT = 2;
        private static final int TCP_TIME_WAIT = 6;
        private long ato;
        private int ca_state;
        private long last_ack_recv;
        private long last_data_recv;
        private long last_data_sent;
        private int option;
        private long rcv_spac;
        private long rto;
        private long rtt;
        private int state;
        private long total_retrans;
        private long unacked;

        /* synthetic */ TcpSocketInfo(OppoTcpInfoMonitor this$0, int state, int ca_state, int option, long rto, long ato, long unacked, long last_data_sent, long last_data_recv, long last_ack_recv, long rtt, long rcv_spac, long total_retrans, TcpSocketInfo -this13) {
            this(state, ca_state, option, rto, ato, unacked, last_data_sent, last_data_recv, last_ack_recv, rtt, rcv_spac, total_retrans);
        }

        private TcpSocketInfo(int state, int ca_state, int option, long rto, long ato, long unacked, long last_data_sent, long last_data_recv, long last_ack_recv, long rtt, long rcv_spac, long total_retrans) {
            this.state = state;
            this.ca_state = ca_state;
            this.option = option;
            this.rto = rto;
            this.ato = ato;
            this.unacked = unacked;
            this.last_data_sent = last_data_sent;
            this.last_data_recv = last_data_recv;
            this.last_ack_recv = last_ack_recv;
            this.rtt = rtt;
            this.rcv_spac = rcv_spac;
            this.total_retrans = total_retrans;
        }
    }

    public OppoTcpInfoMonitor(Context context) {
        this.mContext = context;
        HandlerThread handlerThread = new HandlerThread("DumpTcpInfo");
        handlerThread.start();
        this.mTcpHandler = new Handler(handlerThread.getLooper());
    }

    private boolean needToMonitorTcpInfo() {
        NetworkInfo networkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isAvailable();
        }
        return false;
    }

    private int parseIntValueByString(String str, String prefix) {
        if (str == null || prefix == null) {
            Log.e(TAG, "parseValueByString, str=" + str + ", prefix=" + prefix);
            return 0;
        }
        int value = 0;
        int index = str.indexOf(prefix);
        if (index != -1) {
            try {
                value = Integer.parseInt(str.substring(prefix.length() + index));
            } catch (NumberFormatException e) {
                Log.e(TAG, "parseIntValueByString, NumberFormatException!");
                value = 0;
            }
        }
        return value;
    }

    private long parseLongValueByString(String str, String prefix) {
        if (str == null || prefix == null) {
            Log.e(TAG, "parseLongValueByString, str=" + str + ", prefix=" + prefix);
            return 0;
        }
        long value = 0;
        int index = str.indexOf(prefix);
        if (index != -1) {
            try {
                value = Long.parseLong(str.substring(prefix.length() + index));
            } catch (NumberFormatException e) {
                Log.e(TAG, "parseLongValueByString, NumberFormatException!");
                value = 0;
            }
        }
        return value;
    }

    /* JADX WARNING: Removed duplicated region for block: B:66:0x01ab A:{SYNTHETIC, Splitter: B:66:0x01ab} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0196 A:{SYNTHETIC, Splitter: B:54:0x0196} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean catchTcpSocketInfo() {
        Exception e;
        Throwable th;
        BufferedReader reader = null;
        boolean result = false;
        int state = 0;
        int ca_state = 0;
        int option = 0;
        long rto = 0;
        long ato = 0;
        long unacked = 0;
        long last_data_sent = 0;
        long last_data_recv = 0;
        long last_ack_recv = 0;
        long rtt = 0;
        long rcv_space = 0;
        long total_retrans = 0;
        this.mTcpSocketInfo.clear();
        try {
            Process process = Runtime.getRuntime().exec(PRINT_CMD);
            process.waitFor();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            try {
                String str = "";
                while (true) {
                    str = bufferedReader.readLine();
                    if (str == null) {
                        break;
                    }
                    for (String string : str.split("\\s+")) {
                        if (string.contains(CA_STATE_STR)) {
                            ca_state = parseIntValueByString(string, CA_STATE_STR);
                        } else if (string.contains(STATE_STR)) {
                            state = parseIntValueByString(string, STATE_STR);
                        } else if (string.contains(OPTION_STR)) {
                            option = parseIntValueByString(string, OPTION_STR);
                        } else if (string.contains(RTO_STR)) {
                            rto = parseLongValueByString(string, RTO_STR);
                        } else if (string.contains(ATO_STR)) {
                            ato = parseLongValueByString(string, ATO_STR);
                        } else if (string.contains(UNACKED_STR)) {
                            unacked = parseLongValueByString(string, UNACKED_STR);
                        } else if (string.contains(LAST_D_S_STR)) {
                            last_data_sent = parseLongValueByString(string, LAST_D_S_STR);
                        } else if (string.contains(LAST_D_R_STR)) {
                            last_data_recv = parseLongValueByString(string, LAST_D_R_STR);
                        } else if (string.contains(LAST_A_R_STR)) {
                            last_ack_recv = parseLongValueByString(string, LAST_A_R_STR);
                        } else if (string.contains(RTT_STR)) {
                            rtt = parseLongValueByString(string, RTT_STR);
                        } else if (string.contains(RCV_SPACE_STR)) {
                            rcv_space = parseLongValueByString(string, RCV_SPACE_STR);
                        } else if (string.contains(T_RETRANS_STR)) {
                            total_retrans = parseLongValueByString(string, T_RETRANS_STR);
                        }
                    }
                    this.mTcpSocketInfo.add(new TcpSocketInfo(this, state, ca_state, option, rto, ato, unacked, last_data_sent, last_data_recv, last_ack_recv, rtt, rcv_space, total_retrans, null));
                }
                result = true;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e2) {
                    }
                }
                reader = bufferedReader;
            } catch (Exception e3) {
                e = e3;
                reader = bufferedReader;
                try {
                    e.printStackTrace();
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception e4) {
                        }
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception e5) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                reader = bufferedReader;
                if (reader != null) {
                }
                throw th;
            }
        } catch (Exception e6) {
            e = e6;
            e.printStackTrace();
            if (reader != null) {
            }
            return result;
        }
        return result;
    }

    public int getCurrentTcpLinkStatus() {
        if (!needToMonitorTcpInfo()) {
            return -1;
        }
        if (this.mTcpHandler != null) {
            this.mTcpHandler.post(new Runnable() {
                /* JADX WARNING: Missing block: B:53:0x0145, code:
            return;
     */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void run() {
                    int poorLinkCount = 0;
                    int unreachableLinkCount = 0;
                    int closeWaitLinkCount = 0;
                    int deadLinkCount = 0;
                    synchronized (OppoTcpInfoMonitor.this.mTcpSocketInfo) {
                        if (OppoTcpInfoMonitor.this.catchTcpSocketInfo()) {
                            for (TcpSocketInfo info : OppoTcpInfoMonitor.this.mTcpSocketInfo) {
                                if (info.state == 2 && info.option == 0 && info.rto >= 1000000 && info.ato == 0 && info.last_data_sent == info.last_data_recv && info.rtt == 0 && info.rcv_spac == 0) {
                                    unreachableLinkCount++;
                                }
                                if (info.state != 2 && info.rto > 1000000 && ((info.rtt == 0 || info.rtt > 1000000) && (info.total_retrans > 0 || info.unacked > 0))) {
                                    poorLinkCount++;
                                }
                                if (info.state == 8) {
                                    closeWaitLinkCount++;
                                } else if (info.last_data_recv > OppoTcpInfoMonitor.TCP_AGE_THRESHOLD && info.last_data_sent > OppoTcpInfoMonitor.TCP_AGE_THRESHOLD && info.last_ack_recv > OppoTcpInfoMonitor.TCP_AGE_THRESHOLD) {
                                    deadLinkCount++;
                                }
                            }
                            int totalLinkCount = OppoTcpInfoMonitor.this.mTcpSocketInfo.size();
                            int notGoodLinkCount = unreachableLinkCount + poorLinkCount;
                            int notCloseLinkCount = totalLinkCount - closeWaitLinkCount;
                            OppoTcpInfoMonitor.this.Logd("getCurrentTcpLinkStatus, totalLinkCount=" + totalLinkCount + ", unreachableLinkCount=" + unreachableLinkCount + ", poorLinkCount=" + poorLinkCount + ", closeWaitLinkCount=" + closeWaitLinkCount + ", deadLinkCount=" + deadLinkCount);
                            if (totalLinkCount == 0) {
                                OppoTcpInfoMonitor.this.mStatus = 1;
                            } else if (totalLinkCount < 3 && notGoodLinkCount == 0) {
                                OppoTcpInfoMonitor.this.mStatus = 0;
                            } else if (unreachableLinkCount >= (notCloseLinkCount + 1) / 2) {
                                if (closeWaitLinkCount != 0 || unreachableLinkCount == notCloseLinkCount) {
                                    OppoTcpInfoMonitor.this.mStatus = 2;
                                } else {
                                    OppoTcpInfoMonitor.this.mStatus = 3;
                                }
                            } else if (notGoodLinkCount >= (notCloseLinkCount + 1) / 2 || deadLinkCount >= ((notCloseLinkCount * 2) + 2) / 3) {
                                OppoTcpInfoMonitor.this.mStatus = 18;
                            } else if (notGoodLinkCount >= (notCloseLinkCount + 2) / 3 || deadLinkCount >= (notCloseLinkCount + 1) / 2) {
                                OppoTcpInfoMonitor.this.mStatus = 17;
                            } else if (notGoodLinkCount < (notCloseLinkCount + 2) / 3 && deadLinkCount < (notCloseLinkCount + 1) / 2) {
                                OppoTcpInfoMonitor.this.mStatus = 16;
                            }
                        }
                    }
                }
            });
        }
        Logd("getCurrentTcpLinkStatus, status=" + this.mStatus);
        return this.mStatus;
    }

    public void resetTcpLinkStatus() {
        this.mStatus = 0;
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            DBG = true;
        } else {
            DBG = false;
        }
    }

    private void Logd(String log) {
        if (DBG) {
            Log.d(TAG, "" + log);
        }
    }
}
