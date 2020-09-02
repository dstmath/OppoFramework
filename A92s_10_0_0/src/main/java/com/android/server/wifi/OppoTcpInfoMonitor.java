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
    private Runnable mGetTcpInfoTask = new Runnable() {
        /* class com.android.server.wifi.OppoTcpInfoMonitor.AnonymousClass1 */

        /* JADX WARNING: Code restructure failed: missing block: B:80:0x0171, code lost:
            return;
         */
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
                    OppoTcpInfoMonitor oppoTcpInfoMonitor = OppoTcpInfoMonitor.this;
                    oppoTcpInfoMonitor.Logd("getCurrentTcpLinkStatus, totalLinkCount=" + totalLinkCount + ", unreachableLinkCount=" + unreachableLinkCount + ", poorLinkCount=" + poorLinkCount + ", closeWaitLinkCount=" + closeWaitLinkCount + ", deadLinkCount=" + deadLinkCount);
                    if (totalLinkCount == 0) {
                        int unused = OppoTcpInfoMonitor.this.mStatus = 1;
                    } else if (totalLinkCount < 3 && notGoodLinkCount == 0) {
                        int unused2 = OppoTcpInfoMonitor.this.mStatus = 0;
                    } else if (unreachableLinkCount < (notCloseLinkCount + 1) / 2) {
                        if (notGoodLinkCount < (notCloseLinkCount + 1) / 2) {
                            if (deadLinkCount < ((notCloseLinkCount * 2) + 2) / 3) {
                                if (notGoodLinkCount < (notCloseLinkCount + 2) / 3) {
                                    if (deadLinkCount < (notCloseLinkCount + 1) / 2) {
                                        if (notGoodLinkCount < (notCloseLinkCount + 2) / 3 && deadLinkCount < (notCloseLinkCount + 1) / 2) {
                                            int unused3 = OppoTcpInfoMonitor.this.mStatus = 16;
                                        }
                                    }
                                }
                                int unused4 = OppoTcpInfoMonitor.this.mStatus = 17;
                            }
                        }
                        int unused5 = OppoTcpInfoMonitor.this.mStatus = 18;
                    } else if (closeWaitLinkCount != 0 || unreachableLinkCount == notCloseLinkCount) {
                        int unused6 = OppoTcpInfoMonitor.this.mStatus = 2;
                    } else {
                        int unused7 = OppoTcpInfoMonitor.this.mStatus = 3;
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int mStatus = 0;
    private Handler mTcpHandler;
    /* access modifiers changed from: private */
    public List<TcpSocketInfo> mTcpSocketInfo = new ArrayList();

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
        int index = str.indexOf(prefix);
        if (index == -1) {
            return 0;
        }
        try {
            return Integer.parseInt(str.substring(prefix.length() + index));
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseIntValueByString, NumberFormatException!");
            return 0;
        }
    }

    private long parseLongValueByString(String str, String prefix) {
        if (str == null || prefix == null) {
            Log.e(TAG, "parseLongValueByString, str=" + str + ", prefix=" + prefix);
            return 0;
        }
        int index = str.indexOf(prefix);
        if (index == -1) {
            return 0;
        }
        try {
            return Long.parseLong(str.substring(prefix.length() + index));
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseLongValueByString, NumberFormatException!");
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0141, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0142, code lost:
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x01ce, code lost:
        r0 = e;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x02b3 A[SYNTHETIC, Splitter:B:109:0x02b3] */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x02d6 A[SYNTHETIC, Splitter:B:116:0x02d6] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x01ce A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:19:0x0094] */
    public boolean catchTcpSocketInfo() {
        Throwable th;
        BufferedReader reader;
        Process process;
        long last_ack_recv;
        long rtt;
        long rcv_space;
        long total_retrans;
        long rto;
        long ato;
        long unacked;
        long last_data_sent;
        int ca_state;
        int option;
        long last_data_recv;
        OppoTcpInfoMonitor oppoTcpInfoMonitor = this;
        String str = RCV_SPACE_STR;
        String str2 = RTT_STR;
        String str3 = LAST_A_R_STR;
        String str4 = LAST_D_R_STR;
        String str5 = LAST_D_S_STR;
        String str6 = UNACKED_STR;
        String str7 = ATO_STR;
        String str8 = RTO_STR;
        String str9 = OPTION_STR;
        String str10 = STATE_STR;
        String str11 = CA_STATE_STR;
        boolean result = false;
        int ca_state2 = 0;
        int option2 = 0;
        long rto2 = 0;
        long ato2 = 0;
        long unacked2 = 0;
        long last_data_sent2 = 0;
        long last_data_recv2 = 0;
        long rto3 = 0;
        long ato3 = 0;
        long unacked3 = 0;
        long last_data_sent3 = 0;
        BufferedReader reader2 = null;
        oppoTcpInfoMonitor.mTcpSocketInfo.clear();
        try {
            try {
                process = Runtime.getRuntime().exec(PRINT_CMD);
                process.waitFor();
            } catch (Exception e) {
                e = e;
                reader = null;
                try {
                    e.printStackTrace();
                    if (reader != null) {
                    }
                    return result;
                } catch (Throwable th2) {
                    reader2 = reader;
                    th = th2;
                    if (reader2 != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (reader2 != null) {
                }
                throw th;
            }
            try {
                reader2 = new BufferedReader(new InputStreamReader(process.getInputStream()));
                int state = 0;
                ca_state2 = 0;
                while (true) {
                    try {
                        String line = reader2.readLine();
                        if (line == null) {
                            break;
                        }
                        int state2 = state;
                        try {
                            String[] str12 = line.split("\\s+");
                            int length = str12.length;
                            last_ack_recv = rto3;
                            rtt = ato3;
                            rcv_space = unacked3;
                            total_retrans = last_data_sent3;
                            rto = rto2;
                            ato = ato2;
                            unacked = unacked2;
                            last_data_sent = last_data_sent2;
                            ca_state = ca_state2;
                            option = option2;
                            int ca_state3 = 0;
                            last_data_recv = last_data_recv2;
                            while (ca_state3 < length) {
                                try {
                                    String string = str12[ca_state3];
                                    if (string.contains(str11)) {
                                        ca_state = oppoTcpInfoMonitor.parseIntValueByString(string, str11);
                                    } else if (string.contains(str10)) {
                                        state2 = oppoTcpInfoMonitor.parseIntValueByString(string, str10);
                                    } else if (string.contains(str9)) {
                                        option = oppoTcpInfoMonitor.parseIntValueByString(string, str9);
                                    } else if (string.contains(str8)) {
                                        rto = oppoTcpInfoMonitor.parseLongValueByString(string, str8);
                                    } else if (string.contains(str7)) {
                                        ato = oppoTcpInfoMonitor.parseLongValueByString(string, str7);
                                    } else if (string.contains(str6)) {
                                        unacked = oppoTcpInfoMonitor.parseLongValueByString(string, str6);
                                    } else if (string.contains(str5)) {
                                        last_data_sent = oppoTcpInfoMonitor.parseLongValueByString(string, str5);
                                    } else if (string.contains(str4)) {
                                        last_data_recv = oppoTcpInfoMonitor.parseLongValueByString(string, str4);
                                    } else if (string.contains(str3)) {
                                        last_ack_recv = oppoTcpInfoMonitor.parseLongValueByString(string, str3);
                                    } else if (string.contains(str2)) {
                                        rtt = oppoTcpInfoMonitor.parseLongValueByString(string, str2);
                                    } else if (string.contains(str)) {
                                        rcv_space = oppoTcpInfoMonitor.parseLongValueByString(string, str);
                                    } else if (string.contains(T_RETRANS_STR)) {
                                        total_retrans = oppoTcpInfoMonitor.parseLongValueByString(string, T_RETRANS_STR);
                                    }
                                    ca_state3++;
                                    length = length;
                                } catch (Exception e2) {
                                } catch (Throwable th4) {
                                    th = th4;
                                    th = th;
                                    if (reader2 != null) {
                                    }
                                    throw th;
                                }
                            }
                        } catch (Exception e3) {
                            e = e3;
                            reader = reader2;
                            e.printStackTrace();
                            if (reader != null) {
                            }
                            return result;
                        } catch (Throwable th5) {
                            th = th5;
                            if (reader2 != null) {
                            }
                            throw th;
                        }
                        try {
                        } catch (Exception e4) {
                            e = e4;
                            ca_state2 = ca_state;
                            option2 = option;
                            rto2 = rto;
                            ato2 = ato;
                            unacked2 = unacked;
                            last_data_sent2 = last_data_sent;
                            reader = reader2;
                            last_data_recv2 = last_data_recv;
                            rto3 = last_ack_recv;
                            ato3 = rtt;
                            unacked3 = rcv_space;
                            last_data_sent3 = total_retrans;
                            e.printStackTrace();
                            if (reader != null) {
                            }
                            return result;
                        } catch (Throwable th6) {
                            th = th6;
                            th = th;
                            if (reader2 != null) {
                            }
                            throw th;
                        }
                        try {
                            this.mTcpSocketInfo.add(new TcpSocketInfo(state2, ca_state, option, rto, ato, unacked, last_data_sent, last_data_recv, last_ack_recv, rtt, rcv_space, total_retrans));
                            oppoTcpInfoMonitor = this;
                            ca_state2 = ca_state;
                            option2 = option;
                            rto2 = rto;
                            ato2 = ato;
                            unacked2 = unacked;
                            last_data_sent2 = last_data_sent;
                            state = state2;
                            last_data_recv2 = last_data_recv;
                            rto3 = last_ack_recv;
                            ato3 = rtt;
                            unacked3 = rcv_space;
                            last_data_sent3 = total_retrans;
                            str11 = str11;
                            str10 = str10;
                            str9 = str9;
                            str8 = str8;
                            str7 = str7;
                            str6 = str6;
                            str5 = str5;
                            str4 = str4;
                            str3 = str3;
                            str2 = str2;
                            str = str;
                        } catch (Exception e5) {
                            e = e5;
                            ca_state2 = ca_state;
                            option2 = option;
                            rto2 = rto;
                            ato2 = ato;
                            unacked2 = unacked;
                            last_data_sent2 = last_data_sent;
                            reader = reader2;
                            last_data_recv2 = last_data_recv;
                            rto3 = last_ack_recv;
                            ato3 = rtt;
                            unacked3 = rcv_space;
                            last_data_sent3 = total_retrans;
                            e.printStackTrace();
                            if (reader != null) {
                            }
                            return result;
                        } catch (Throwable th7) {
                            th = th7;
                            th = th;
                            if (reader2 != null) {
                            }
                            throw th;
                        }
                    } catch (Exception e6) {
                        e = e6;
                        reader = reader2;
                        e.printStackTrace();
                        if (reader != null) {
                        }
                        return result;
                    } catch (Throwable th8) {
                        th = th8;
                        if (reader2 != null) {
                        }
                        throw th;
                    }
                }
                result = true;
                try {
                    reader2.close();
                } catch (Exception e7) {
                }
            } catch (Exception e8) {
                e = e8;
                reader = reader2;
                ca_state2 = 0;
                e.printStackTrace();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e9) {
                    }
                }
                return result;
            } catch (Throwable th9) {
                th = th9;
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (Exception e10) {
                    }
                }
                throw th;
            }
        } catch (Exception e11) {
            e = e11;
            reader = null;
            e.printStackTrace();
            if (reader != null) {
            }
            return result;
        } catch (Throwable th10) {
            th = th10;
            if (reader2 != null) {
            }
            throw th;
        }
        return result;
    }

    public int getCurrentTcpLinkStatus() {
        if (!needToMonitorTcpInfo()) {
            return -1;
        }
        Handler handler = this.mTcpHandler;
        if (handler != null && !handler.hasCallbacks(this.mGetTcpInfoTask)) {
            this.mTcpHandler.post(this.mGetTcpInfoTask);
        }
        Logd("getCurrentTcpLinkStatus, status=" + this.mStatus);
        return this.mStatus;
    }

    public void resetTcpLinkStatus() {
        this.mStatus = 0;
    }

    /* access modifiers changed from: private */
    public class TcpSocketInfo {
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
        /* access modifiers changed from: private */
        public long ato;
        private int ca_state;
        /* access modifiers changed from: private */
        public long last_ack_recv;
        /* access modifiers changed from: private */
        public long last_data_recv;
        /* access modifiers changed from: private */
        public long last_data_sent;
        /* access modifiers changed from: private */
        public int option;
        /* access modifiers changed from: private */
        public long rcv_spac;
        /* access modifiers changed from: private */
        public long rto;
        /* access modifiers changed from: private */
        public long rtt;
        /* access modifiers changed from: private */
        public int state;
        /* access modifiers changed from: private */
        public long total_retrans;
        /* access modifiers changed from: private */
        public long unacked;

        private TcpSocketInfo(int state2, int ca_state2, int option2, long rto2, long ato2, long unacked2, long last_data_sent2, long last_data_recv2, long last_ack_recv2, long rtt2, long rcv_spac2, long total_retrans2) {
            this.state = state2;
            this.ca_state = ca_state2;
            this.option = option2;
            this.rto = rto2;
            this.ato = ato2;
            this.unacked = unacked2;
            this.last_data_sent = last_data_sent2;
            this.last_data_recv = last_data_recv2;
            this.last_ack_recv = last_ack_recv2;
            this.rtt = rtt2;
            this.rcv_spac = rcv_spac2;
            this.total_retrans = total_retrans2;
        }
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            DBG = true;
        } else {
            DBG = false;
        }
    }

    /* access modifiers changed from: private */
    public void Logd(String log) {
        if (DBG) {
            Log.d(TAG, "" + log);
        }
    }
}
