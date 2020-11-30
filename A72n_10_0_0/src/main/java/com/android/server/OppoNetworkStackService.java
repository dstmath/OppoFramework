package com.android.server;

import android.app.ActivityManager;
import android.common.PswFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.IDnsResolver;
import android.net.INetd;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.metrics.INetdEventListener;
import android.net.netlink.NetlinkSocket;
import android.net.netlink.StructNlMsgHdr;
import android.net.util.NetdService;
import android.net.util.SocketUtils;
import android.net.wifi.IWifiRomUpdateHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.system.Os;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoTdscdma;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.util.RingBuffer;
import com.android.server.TcpSocketTracker;
import com.android.server.UiModeManagerService;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.IColorEyeProtectManager;
import com.oppo.network.IOppoNetScoreChange;
import com.oppo.network.IOppoNetworkStack;
import java.io.FileDescriptor;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import libcore.io.IoUtils;
import oppo.util.OppoStatistics;

public class OppoNetworkStackService extends IOppoNetworkStack.Stub {
    private static final int ABNORMAL_CELLULAR_SCORE = 99999;
    private static final int BAD_DNS_LANTENCY = 1500;
    private static final int BAD_SCORE = 60;
    private static final int BAD_TCP_LANTENCY = 1000;
    private static final int BIND_PORT = 100;
    private static final int CHECK_NETWORK_TIMEOUT = 3000;
    private static final int CODE_DNS_TIMEOUT = 255;
    private static final int CONFIG_BAD_DNS_LANTENCY = 7;
    private static final int CONFIG_BAD_SCORE_INTERVAL = 5;
    private static final int CONFIG_BAD_TCP_LANTENCY = 8;
    private static final int CONFIG_DISABLE_FEATURE = 0;
    private static final int CONFIG_ENABLE_DEBUG = 1;
    private static final int CONFIG_FORE_DNS_FACTOR = 3;
    private static final int CONFIG_GOOD_SCORE_INTERVAL = 6;
    private static final int CONFIG_MAX_DNS_DELAY = 2;
    private static final int CONFIG_NOTIFY_DELAY = 4;
    private static final int DEFAULT_MAX_DNS_TIMEOUT = 10000;
    private static final int DNS_EVENT_GETADDRINFO = 1;
    private static final int ERROR_DNS_FAIL = 4;
    private static final int ERROR_DNS_MAX = 8;
    private static final int ERROR_DNS_NODATA = 7;
    private static final int FOREGROUND_IMPORTANCE_CUTOFF = 100;
    private static final int GOOD_DNS_LANTENCY = 200;
    private static final int GOOD_SCORE = 80;
    private static final int GOOD_TCP_LANTENCY = 500;
    private static final int INVALID_DNS_LANTENCY = 100000;
    private static final int INVALID_SCORE = 88888;
    private static final long IO_TIMEOUT = 300;
    private static final String KEEP_CELL_NETWORK_FOR_MPTCP = "keep_celluar_network_for_mptcp";
    private static final int MAX_DNS_CALLBACK = 60;
    private static final int MAX_EVENTS = 8;
    private static final int MAX_REQ_NETWORK_TIMEOUT = 15000;
    private static final int MSG_NOTIFY_SCORE = 103;
    private static final int MSG_RECHECK_SCORE = 104;
    private static final int MSG_SHOW_TOAST = 100;
    private static final int MSG_TCP_SCORE = 101;
    private static final int MSG_TOP_UID_CHANGED = 102;
    private static final int NETLINK_OPPO_KERNEL2USER = 37;
    private static final short OPPO_FOREGROUND_ANDROID_UID = 32;
    private static final String OPPO_MPTCP_APPS = "OPPO_MPTCP_APPS";
    private static final short OPPO_MPTCP_UID = 33;
    private static final short OPPO_NETWORK_UID = 34;
    private static final short OPPO_SEND_NETWORK_SCORE = 49;
    private static final short OPPO_SEND_TCP_RETRANSMIT = 48;
    private static final int RECHECK_NETWORK_DELAY = 5000;
    private static final int SIZE_OF_INT = 4;
    public static final String TAG = "OppoNetworkStackService";
    private static final int VALID_DNS_LANTENCY = 10;
    private static OppoNetworkStackService mInstance = null;
    private static FileDescriptor mNlFd;
    private final String CHINA_MCC = "460";
    private final String DEFAULT_CHECK_SERVER = "http://conn1.coloros.com/generate_204";
    private final String DEFAULT_CHECK_SERVER_EXP = "http://connectivitycheck.gstatic.com/generate_204";
    private final int MAX_HTTP_REDIRECT = IColorEyeProtectManager.LEVEL_COLOR_MATRIX_COLOR;
    private final int MIN_HTTP_REDIRECT = 300;
    private final int SUCCESS_HTTP_CODE = 204;
    private String allCheckResult = "";
    private boolean bNetworkStackScoreFeature = true;
    private int badDnsLantency = 1500;
    private int badTcpLantency = 1000;
    private int[] configParams = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private int curDefaultNetid = 0;
    private String curDefaultNetworkType = "";
    private int curForegroundUid = 0;
    private String currentMcc = null;
    private HashSet<Integer> foregroundMptcpSet = new HashSet<>();
    private int foreground_dns_factor = 30;
    private boolean hasRedirectInCellular = false;
    private boolean hasRegisterFromCS = false;
    private boolean hasRequested = false;
    private boolean isCheckingNetwork = false;
    private boolean isScreenOn = false;
    private int lastDnsScore = 0;
    private int lastTcpScore = 0;
    private int lastTotalScore = 0;
    private ActivityManager mActivityManager = null;
    private Handler mAsyncHandler;
    private BroadcastReceiver mBroadcastReceiver = null;
    private ConnectivityManager mCm = null;
    private Context mContext = null;
    private final Object mDnsLock = new Object();
    private IDnsResolver mDnsResolver;
    private Handler mHandler;
    private INetd mNetdService;
    private String mNotifyScoreApp = UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
    private final Collection<IOppoNetScoreChange> mScoreChangeLists = new ArrayList();
    private TelephonyManager mTelephonyManager = null;
    private ActivityManager.OnUidImportanceListener mUidImportanceListener = null;
    private IWifiRomUpdateHelper mWifiRomUpdateHelper = null;
    private ConnectivityManager.NetworkCallback mptcpNetworkCallback = null;
    private String myCheckUrl = "http://conn1.coloros.com/generate_204";
    private String myCheckUrlExp = "http://connectivitycheck.gstatic.com/generate_204";
    private MyNetdEventListenerService myDnsListener = null;
    private HashMap<String, RingBuffer<DnsEvent>> myDnsTracker = new HashMap<>();
    private HashMap<String, Boolean> myHttpServerValid = new HashMap<>();
    private ConnectivityManager.NetworkCallback myNetworkCallback = null;
    private HashMap<String, TcpSocketTracker> mySocketTracker = new HashMap<>();
    Toast myToast = null;
    private boolean needCheckHttpHeader = false;
    private int notifyBadScore = -1;
    private int notifyGoodScore = -1;
    private int notify_delay = 1000;
    private boolean openConnSuccess = false;
    private int processDnsEventRef = 0;
    private int statisticBadScoreInterval = 10;
    private int statisticGoodScoreInterval = 20;
    private HashMap<String, String> statisticsData = new HashMap<>();
    private int toomany47error = 0;

    static /* synthetic */ int access$1908(OppoNetworkStackService x0) {
        int i = x0.processDnsEventRef;
        x0.processDnsEventRef = i + 1;
        return i;
    }

    static /* synthetic */ int access$1910(OppoNetworkStackService x0) {
        int i = x0.processDnsEventRef;
        x0.processDnsEventRef = i - 1;
        return i;
    }

    static /* synthetic */ int access$2008(OppoNetworkStackService x0) {
        int i = x0.toomany47error;
        x0.toomany47error = i + 1;
        return i;
    }

    public OppoNetworkStackService(Context context) {
        this.mContext = context;
        try {
            this.mWifiRomUpdateHelper = PswFrameworkFactory.getInstance().getFeature(IWifiRomUpdateHelper.DEFAULT, new Object[]{this.mContext});
            this.mActivityManager = (ActivityManager) this.mContext.getSystemService(IColorAppStartupManager.TYPE_ACTIVITY);
            this.mCm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            this.mUidImportanceListener = new ActivityManager.OnUidImportanceListener() {
                /* class com.android.server.OppoNetworkStackService.AnonymousClass1 */

                public void onUidImportance(int uid, int importance) {
                    OppoNetworkStackService.this.mAsyncHandler.sendMessage(OppoNetworkStackService.this.mAsyncHandler.obtainMessage(102, uid, importance));
                    boolean isMPTCP = OppoNetworkStackService.this.isMptcpUid(uid);
                    if (isMPTCP && importance <= 300) {
                        OppoNetworkStackService.this.foregroundMptcpSet.add(Integer.valueOf(uid));
                        if (!OppoNetworkStackService.this.hasRequested) {
                            OppoNetworkStackService.this.requestCellularNetwork();
                            OppoNetworkStackService.this.hasRequested = true;
                            Settings.System.putInt(OppoNetworkStackService.this.mContext.getContentResolver(), OppoNetworkStackService.KEEP_CELL_NETWORK_FOR_MPTCP, 1);
                        }
                    } else if (isMPTCP && importance > 300) {
                        OppoNetworkStackService.this.foregroundMptcpSet.remove(Integer.valueOf(uid));
                        if (OppoNetworkStackService.this.hasRequested && OppoNetworkStackService.this.foregroundMptcpSet.size() == 0) {
                            OppoNetworkStackService.this.releaseCellularNetwork();
                            OppoNetworkStackService.this.hasRequested = false;
                            Settings.System.putInt(OppoNetworkStackService.this.mContext.getContentResolver(), OppoNetworkStackService.KEEP_CELL_NETWORK_FOR_MPTCP, 0);
                        }
                    }
                }
            };
            this.mHandler = new Handler() {
                /* class com.android.server.OppoNetworkStackService.AnonymousClass2 */

                public void handleMessage(Message msg) {
                    if (msg.what != 100) {
                        OppoNetworkStackService oppoNetworkStackService = OppoNetworkStackService.this;
                        oppoNetworkStackService.logFunc(OppoNetworkStackService.TAG, "Unknow message:" + msg.what);
                        return;
                    }
                    Log.w(OppoNetworkStackService.TAG, "MSG_SHOW_TOAST:" + msg.obj);
                    OppoNetworkStackService.this.showMyToast((String) msg.obj, 0);
                }
            };
            HandlerThread handlerThread = new HandlerThread("OppoNetworkStackService_Handler");
            handlerThread.start();
            this.mAsyncHandler = new Handler(handlerThread.getLooper()) {
                /* class com.android.server.OppoNetworkStackService.AnonymousClass3 */

                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 101:
                            Handler handler = OppoNetworkStackService.this.mHandler;
                            Handler handler2 = OppoNetworkStackService.this.mHandler;
                            handler.sendMessage(handler2.obtainMessage(100, "" + msg.obj));
                            return;
                        case 102:
                            OppoNetworkStackService oppoNetworkStackService = OppoNetworkStackService.this;
                            oppoNetworkStackService.logFunc(OppoNetworkStackService.TAG, "MSG_TOP_UID_CHANGED:uid=" + msg.arg1 + " importance=" + msg.arg2);
                            if (msg.arg2 <= 100) {
                                synchronized (OppoNetworkStackService.this.myDnsListener) {
                                    OppoNetworkStackService.this.curForegroundUid = msg.arg1;
                                    OppoNetworkStackService.this.mNotifyScoreApp = OppoNetworkStackService.this.mContext.getPackageManager().getNameForUid(OppoNetworkStackService.this.curForegroundUid);
                                }
                                OppoNetworkStackService.this.sendToKernel((OppoNetworkStackService) 32, (short) msg.arg1);
                                return;
                            }
                            return;
                        case 103:
                            OppoNetworkStackService.this.notifyScoreChange(msg.arg1, msg.arg2);
                            return;
                        case 104:
                            OppoNetworkStackService.this.recheckNetworkForLowScore();
                            return;
                        default:
                            OppoNetworkStackService oppoNetworkStackService2 = OppoNetworkStackService.this;
                            oppoNetworkStackService2.logFunc(OppoNetworkStackService.TAG, "Unknow message:" + msg.what);
                            return;
                    }
                }
            };
            startListenKernel();
            if (this.mActivityManager != null) {
                this.mActivityManager.addOnUidImportanceListener(this.mUidImportanceListener, 100);
            }
            this.mDnsResolver = IDnsResolver.Stub.asInterface(ServiceManager.getService("dnsresolver"));
            this.myDnsListener = new MyNetdEventListenerService();
            this.mDnsResolver.registerEventListener(this.myDnsListener);
            this.mNetdService = NetdService.getInstance();
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
            registerNetidChange();
            initBroadcastReceiver();
        } catch (Exception e) {
            Log.e(TAG, "Exception when init:" + e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logFunc(String tag, String content) {
        if (SystemProperties.getBoolean("persist.oppo.networkstack.show", false) || this.configParams[1] > 0) {
            Log.w(tag, content);
        }
    }

    /* access modifiers changed from: private */
    public class DnsEvent {
        String hostname;
        int latencyMs;
        int netId;
        int returnCode;
        int uid;

        private DnsEvent(int netId2, int returnCode2, int latencyMs2, String hostname2, int uid2) {
            this.hostname = hostname2;
            this.returnCode = returnCode2;
            this.latencyMs = latencyMs2;
            this.netId = netId2;
            this.uid = uid2;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized int calculateAvgDnsLatency(int uid, int networkid) {
        RingBuffer<DnsEvent> tempDnsEvents;
        synchronized (this.mDnsLock) {
            try {
                RingBuffer<DnsEvent> tempDnsEvents2 = this.myDnsTracker.get(String.valueOf(networkid));
                if (tempDnsEvents2 == null) {
                    try {
                        Log.w(TAG, "calculateAvgLatency tempEvents = null, netId=" + networkid);
                        return 0;
                    } catch (Throwable th) {
                        tempDnsEvents = th;
                        throw tempDnsEvents;
                    }
                } else {
                    DnsEvent[] allEvents = (DnsEvent[]) tempDnsEvents2.toArray();
                    int count = 0;
                    int sum = 0;
                    for (DnsEvent event : allEvents) {
                        try {
                            if ((uid == event.uid || uid == -1) && networkid == event.netId) {
                                sum += event.latencyMs;
                                count++;
                            }
                        } catch (Throwable th2) {
                            tempDnsEvents = th2;
                            throw tempDnsEvents;
                        }
                    }
                    if (count > 0) {
                        logFunc(TAG, "calculateAvgLatency UID=" + uid + ", count=" + count + ", avg=" + (sum / count) + ", netId=" + networkid);
                        return sum / count;
                    }
                    try {
                        Log.w(TAG, "calculateAvgLatency count = 0, UID=" + uid + ", netId=" + networkid);
                        return 0;
                    } catch (Throwable th3) {
                        tempDnsEvents = th3;
                        throw tempDnsEvents;
                    }
                }
            } catch (Throwable th4) {
                tempDnsEvents = th4;
                throw tempDnsEvents;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean needQuickCheck(int uid) {
        return true;
    }

    public class MyNetdEventListenerService extends INetdEventListener.Stub {
        private MyNetdEventListenerService() {
            OppoNetworkStackService.this.logFunc(OppoNetworkStackService.TAG, "MyNetdEventListenerService init!");
        }

        /* JADX WARNING: Code restructure failed: missing block: B:75:0x022f, code lost:
            r0 = r18.this$0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:76:0x023a, code lost:
            if (r18.this$0.configParams[3] != 0) goto L_0x023f;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:77:0x023c, code lost:
            r4 = 30;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:78:0x023f, code lost:
            r4 = r18.this$0.configParams[3];
         */
        /* JADX WARNING: Code restructure failed: missing block: B:79:0x0247, code lost:
            r0.foreground_dns_factor = r4;
            r0 = r18.this$0.calculateAvgDnsLatency(-1, r19);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:80:0x0257, code lost:
            if (r26 != r18.this$0.curForegroundUid) goto L_0x0272;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:81:0x0259, code lost:
            r18.this$0.lastDnsScore = (((100 - r18.this$0.foreground_dns_factor) * r0) + (r18.this$0.foreground_dns_factor * r4)) / 100;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:82:0x0272, code lost:
            r18.this$0.lastDnsScore = r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:83:0x0277, code lost:
            r4 = r18.this$0.lastTcpScore - (r18.this$0.lastDnsScore / 100);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:84:0x0286, code lost:
            if (r4 >= 0) goto L_0x0289;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:85:0x0288, code lost:
            r4 = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:87:0x028f, code lost:
            if (r4 <= r18.this$0.badDnsLantency) goto L_0x02b8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:88:0x0291, code lost:
            r18.this$0.mAsyncHandler.removeMessages(103);
            r18.this$0.mAsyncHandler.sendMessageDelayed(r18.this$0.mAsyncHandler.obtainMessage(103, 1, r4), (long) r18.this$0.notify_delay);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:90:0x02bb, code lost:
            if (r4 >= 200) goto L_0x02f0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:92:0x02c3, code lost:
            if (r18.this$0.lastTotalScore >= 80) goto L_0x02f0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:94:0x02cb, code lost:
            if (r18.this$0.lastTcpScore <= 0) goto L_0x02f0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:95:0x02cd, code lost:
            r18.this$0.mAsyncHandler.removeMessages(103);
            r18.this$0.mAsyncHandler.sendMessageDelayed(r18.this$0.mAsyncHandler.obtainMessage(103, 1, r4), (long) r18.this$0.notify_delay);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:96:0x02f0, code lost:
            com.android.server.OppoNetworkStackService.access$1910(r18.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:98:0x02f6, code lost:
            return;
         */
        @Override // android.net.metrics.INetdEventListener
        public synchronized void onDnsEvent(int netId, int eventType, int returnCode, int latencyMs, String hostname, String[] ipAddresses, int ipAddressesCount, int uid) throws RemoteException {
            int latencyMs2;
            RingBuffer<DnsEvent> tempDnsEvents;
            if (OppoNetworkStackService.this.bNetworkStackScoreFeature) {
                OppoNetworkStackService oppoNetworkStackService = OppoNetworkStackService.this;
                oppoNetworkStackService.logFunc(OppoNetworkStackService.TAG, "onDnsEvent netId:" + netId + ",eventType=" + eventType + ",returnCode=" + returnCode + ",latencyMs=" + latencyMs + ",hostname=" + hostname + ",uid=" + uid + ",curDefaultNetid=" + OppoNetworkStackService.this.curDefaultNetid);
                OppoNetworkStackService.access$1908(OppoNetworkStackService.this);
                if (OppoNetworkStackService.this.processDnsEventRef > 60) {
                    Log.e(OppoNetworkStackService.TAG, "too many DNS Event in processing.");
                    return;
                }
                if ((returnCode == 4 || returnCode == 7) && uid == OppoNetworkStackService.this.curForegroundUid && latencyMs > 10) {
                    OppoNetworkStackService.access$2008(OppoNetworkStackService.this);
                }
                if ((returnCode != 0 || latencyMs <= OppoNetworkStackService.INVALID_DNS_LANTENCY) && eventType == 1) {
                    if (OppoNetworkStackService.this.configParams[4] != 0) {
                        OppoNetworkStackService.this.notify_delay = OppoNetworkStackService.this.configParams[4];
                    }
                    if (OppoNetworkStackService.this.toomany47error > 8) {
                        Log.w(OppoNetworkStackService.TAG, "toomany47error!");
                        OppoNetworkStackService.this.mAsyncHandler.removeMessages(103);
                        OppoNetworkStackService.this.mAsyncHandler.sendMessageDelayed(OppoNetworkStackService.this.mAsyncHandler.obtainMessage(103, 1, 0), 0);
                    }
                    int maxTimeout = OppoNetworkStackService.this.configParams[2] == 0 ? 10000 : OppoNetworkStackService.this.configParams[2];
                    if (latencyMs > maxTimeout) {
                        latencyMs2 = maxTimeout;
                    } else {
                        latencyMs2 = latencyMs;
                    }
                    final DnsEvent dns = new DnsEvent(netId, returnCode, latencyMs2, hostname, uid);
                    if (latencyMs2 > 10) {
                        OppoNetworkStackService.this.toomany47error = 0;
                        synchronized (OppoNetworkStackService.this.mDnsLock) {
                            try {
                                RingBuffer<DnsEvent> tempDnsEvents2 = (RingBuffer) OppoNetworkStackService.this.myDnsTracker.get(String.valueOf(netId));
                                if (tempDnsEvents2 != null) {
                                    try {
                                        tempDnsEvents2.append(dns);
                                        if (OppoNetworkStackService.this.curDefaultNetid != netId) {
                                            Log.e(OppoNetworkStackService.TAG, "Non Default DNS query maybe is not right!");
                                            OppoNetworkStackService.access$1910(OppoNetworkStackService.this);
                                            return;
                                        }
                                        OppoNetworkStackService oppoNetworkStackService2 = OppoNetworkStackService.this;
                                        oppoNetworkStackService2.logFunc(OppoNetworkStackService.TAG, "checkNetworkWork ready? lastDnsScore=" + OppoNetworkStackService.this.lastDnsScore + ",latencyMs=" + latencyMs2 + ",lastTotalScore=" + OppoNetworkStackService.this.lastTotalScore + ",isCheckingNetwork=" + OppoNetworkStackService.this.isCheckingNetwork);
                                        if ((OppoNetworkStackService.this.needQuickCheck(OppoNetworkStackService.this.curForegroundUid) && latencyMs2 > OppoNetworkStackService.this.badDnsLantency && OppoNetworkStackService.this.lastTotalScore >= 80 && !OppoNetworkStackService.this.isCheckingNetwork) || (OppoNetworkStackService.this.needQuickCheck(OppoNetworkStackService.this.curForegroundUid) && latencyMs2 < 200 && OppoNetworkStackService.this.lastTotalScore <= 60 && !OppoNetworkStackService.this.isCheckingNetwork)) {
                                            new Thread(new Runnable() {
                                                /* class com.android.server.OppoNetworkStackService.MyNetdEventListenerService.AnonymousClass1 */

                                                public void run() {
                                                    OppoNetworkStackService.this.checkNetworkWork(dns, null);
                                                }
                                            }).start();
                                            Log.w(OppoNetworkStackService.TAG, "needQuickCheck curForegroundUid=" + OppoNetworkStackService.this.curForegroundUid + ", latencyMs=" + latencyMs2);
                                        }
                                    } catch (Throwable th) {
                                        tempDnsEvents = th;
                                        throw tempDnsEvents;
                                    }
                                }
                            } catch (Throwable th2) {
                                tempDnsEvents = th2;
                                throw tempDnsEvents;
                            }
                        }
                    } else {
                        OppoNetworkStackService.access$1910(OppoNetworkStackService.this);
                    }
                } else {
                    OppoNetworkStackService.access$1910(OppoNetworkStackService.this);
                }
            }
        }

        @Override // android.net.metrics.INetdEventListener
        public int getInterfaceVersion() {
            return 10000;
        }

        @Override // android.net.metrics.INetdEventListener
        public synchronized void onNat64PrefixEvent(int netId, boolean added, String prefixString, int prefixLength) throws RemoteException {
        }

        @Override // android.net.metrics.INetdEventListener
        public synchronized void onPrivateDnsValidationEvent(int netId, String ipAddress, String hostname, boolean validated) throws RemoteException {
        }

        @Override // android.net.metrics.INetdEventListener
        public synchronized void onConnectEvent(int netId, int error, int latencyMs, String ipAddr, int port, int uid) throws RemoteException {
        }

        @Override // android.net.metrics.INetdEventListener
        public synchronized void onWakeupEvent(String prefix, int uid, int ethertype, int ipNextHeader, byte[] dstHw, String srcIp, String dstIp, int srcPort, int dstPort, long timestampNs) {
        }

        @Override // android.net.metrics.INetdEventListener
        public synchronized void onTcpSocketStatsEvent(int[] networkIds, int[] sentPackets, int[] lostPackets, int[] rttsUs, int[] sentAckDiffsMs) {
        }
    }

    public static OppoNetworkStackService getInstance(Context context) {
        OppoNetworkStackService oppoNetworkStackService;
        synchronized (OppoNetworkStackService.class) {
            if (mInstance == null) {
                mInstance = new OppoNetworkStackService(context);
            }
            oppoNetworkStackService = mInstance;
        }
        return oppoNetworkStackService;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestCellularNetwork() {
        NetworkRequest request = new NetworkRequest.Builder().addTransportType(0).addCapability(12).build();
        this.mptcpNetworkCallback = new ConnectivityManager.NetworkCallback() {
            /* class com.android.server.OppoNetworkStackService.AnonymousClass4 */

            public void onAvailable(Network network) {
                OppoNetworkStackService.this.logFunc(OppoNetworkStackService.TAG, "requestCellularNetwork onAvailable");
            }

            public void onUnavailable() {
                OppoNetworkStackService.this.logFunc(OppoNetworkStackService.TAG, "requestCellularNetwork onUnavailable");
            }

            public void onLost(Network lostNetwork) {
                OppoNetworkStackService.this.logFunc(OppoNetworkStackService.TAG, "requestCellularNetwork onLost");
            }
        };
        if (this.mCm != null) {
            logFunc(TAG, "mCm is not null at first time");
            this.mCm.requestNetwork(request, this.mptcpNetworkCallback, 15000);
        } else {
            logFunc(TAG, "mCm now is null");
            this.mCm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            ConnectivityManager connectivityManager = this.mCm;
            if (connectivityManager == null) {
                Log.w(TAG, "mCm always null");
                return;
            }
            connectivityManager.requestNetwork(request, this.mptcpNetworkCallback, 15000);
        }
        Log.w(TAG, "requestCellularNetwork by MPTCP!");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseCellularNetwork() {
        if (this.mptcpNetworkCallback != null && this.mCm != null) {
            Log.w(TAG, "releaseCellularNetwork by MPTCP!");
            this.mCm.unregisterNetworkCallback(this.mptcpNetworkCallback);
        }
    }

    private boolean needNotify(int score) {
        int diff = this.lastTotalScore - score;
        if ((diff < 0 ? -diff : diff) < 5) {
            return false;
        }
        if (score == 0) {
            return true;
        }
        if (this.lastTotalScore < 60 && score < 60) {
            return false;
        }
        if (score >= 60 && score < 80) {
            return false;
        }
        int i = this.lastTotalScore;
        if (i > 90 && i != ABNORMAL_CELLULAR_SCORE && score >= 70) {
            return false;
        }
        int i2 = this.lastTotalScore;
        if (i2 < 60 || i2 >= 90 || score < 80 || score >= 90) {
            return true;
        }
        return false;
    }

    private String getMccFromCellInfo(CellInfo cell) {
        if (cell instanceof CellInfoGsm) {
            return ((CellInfoGsm) cell).getCellIdentity().getMccString();
        }
        if (cell instanceof CellInfoLte) {
            return ((CellInfoLte) cell).getCellIdentity().getMccString();
        }
        if (cell instanceof CellInfoWcdma) {
            return ((CellInfoWcdma) cell).getCellIdentity().getMccString();
        }
        if (cell instanceof CellInfoTdscdma) {
            return ((CellInfoTdscdma) cell).getCellIdentity().getMccString();
        }
        if (cell instanceof CellInfoNr) {
            return ((CellIdentityNr) ((CellInfoNr) cell).getCellIdentity()).getMccString();
        }
        return null;
    }

    public String getLocationMcc() {
        try {
            List<CellInfo> cells = this.mTelephonyManager.getAllCellInfo();
            Map<String, Integer> countryCodeMap = new HashMap<>();
            if (cells == null) {
                return null;
            }
            for (CellInfo cell : cells) {
                String mcc = getMccFromCellInfo(cell);
                if (mcc != null) {
                    countryCodeMap.put(mcc, Integer.valueOf(countryCodeMap.getOrDefault(mcc, 0).intValue() + 1));
                }
            }
            if (countryCodeMap.size() <= 0) {
                return null;
            }
            this.currentMcc = (String) ((Map.Entry) Collections.max(countryCodeMap.entrySet(), $$Lambda$OppoNetworkStackService$kZRyor1pAhzCQcPgME4Ivn8H4.INSTANCE)).getKey();
            Log.w(TAG, "getLocationMcc=" + this.currentMcc);
            return this.currentMcc;
        } catch (Exception e) {
            Log.w(TAG, "getLocationMcc Exception = " + e);
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void recheckNetworkForLowScore() {
        if (this.isScreenOn) {
            long result = checkNetworkWork(null, null);
            if (result < 500 && this.lastTotalScore < 60) {
                logFunc(TAG, "notifyScoreChange!Real Network is becoming better,ignore!recheckNetworkWork result=" + result);
                notifyScoreChange(4, INVALID_SCORE);
            }
        }
    }

    private void loadStatisticsData(int type, int score) {
        HashMap<String, String> hashMap = this.statisticsData;
        if (hashMap != null) {
            hashMap.put("key_notify_app", "" + this.mNotifyScoreApp);
            HashMap<String, String> hashMap2 = this.statisticsData;
            hashMap2.put("key_notify_type", "" + type);
            HashMap<String, String> hashMap3 = this.statisticsData;
            hashMap3.put("key_total_score", "" + score);
            HashMap<String, String> hashMap4 = this.statisticsData;
            hashMap4.put("key_dns_score", "" + this.lastDnsScore);
            HashMap<String, String> hashMap5 = this.statisticsData;
            hashMap5.put("key_tcp_score", "" + this.lastTcpScore);
            this.statisticsData.put("key_network_type", this.curDefaultNetworkType);
        }
    }

    private void maybeTriggerRestoreStatsData() {
        HashMap<String, String> hashMap;
        if (((this.notifyGoodScore % this.statisticGoodScoreInterval == 0 && this.lastTotalScore > 80) || (this.notifyBadScore % this.statisticBadScoreInterval == 0 && this.lastTotalScore < 60)) && (hashMap = this.statisticsData) != null && hashMap.size() > 1) {
            OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", "network_score", this.statisticsData, false);
            logFunc(TAG, "maybeTriggerRestoreStatsData!statisticsData = " + this.statisticsData);
        }
        HashMap<String, String> hashMap2 = this.statisticsData;
        if (hashMap2 != null) {
            hashMap2.clear();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyScoreChange(int type, int score) {
        Log.w(TAG, "notifyScoreChange! type = " + type + ",score=" + score);
        if (this.myDnsTracker.size() == 0) {
            Log.w(TAG, "notifyScoreChange!myDnsTracker.size() == 0, so score =0!");
            score = 0;
        }
        if (needNotify(score)) {
            boolean better = score > this.lastTotalScore;
            if (this.hasRedirectInCellular) {
                score = ABNORMAL_CELLULAR_SCORE;
                Log.w(TAG, "notifyScoreChange! abnormal SIM Card!");
            }
            if (INVALID_SCORE != score) {
                this.lastTotalScore = score;
            }
            if (score < 60) {
                this.notifyBadScore++;
                this.mAsyncHandler.removeMessages(104);
                Handler handler = this.mAsyncHandler;
                handler.sendMessageDelayed(handler.obtainMessage(104), 5000);
            }
            if (score > 80) {
                this.notifyGoodScore++;
                this.mAsyncHandler.removeMessages(104);
            }
            synchronized (this.mScoreChangeLists) {
                for (IOppoNetScoreChange scorechange : this.mScoreChangeLists) {
                    try {
                        scorechange.networkScoreChange(better, score);
                    } catch (RemoteException e) {
                        Log.w(TAG, "networkScoreChange! RemoteException ");
                    }
                }
            }
            loadStatisticsData(type, score);
            new Thread(new Runnable() {
                /* class com.android.server.OppoNetworkStackService.AnonymousClass5 */

                public void run() {
                    OppoNetworkStackService.this.checkNetworkWork(null, null);
                }
            }).start();
            Handler handler2 = this.mAsyncHandler;
            handler2.sendMessage(handler2.obtainMessage(101, "TotalScore=" + score + ",-DnsScore=" + this.lastDnsScore + ",TcpScore=" + this.lastTcpScore));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long checkNetworkWork(DnsEvent dns, final Network network) {
        long result;
        int i;
        String checkresult = "Score " + this.lastTotalScore + ",tcp=" + this.lastTcpScore + ",udp=" + this.lastDnsScore + "!";
        this.isCheckingNetwork = true;
        logFunc(TAG, "checkNetworkWork start !");
        long start = System.currentTimeMillis();
        final CountDownLatch checkLatch = new CountDownLatch(1);
        new Thread(new Runnable() {
            /* class com.android.server.OppoNetworkStackService.AnonymousClass6 */

            public void run() {
                URL url;
                HttpURLConnection ipconnection;
                try {
                    if (OppoNetworkStackService.this.currentMcc == null || !OppoNetworkStackService.this.currentMcc.equals("460")) {
                        url = new URL(OppoNetworkStackService.this.myCheckUrlExp);
                    } else {
                        url = new URL(OppoNetworkStackService.this.myCheckUrl);
                    }
                    if (network != null) {
                        ipconnection = (HttpURLConnection) network.openConnection(url);
                    } else {
                        ipconnection = (HttpURLConnection) url.openConnection();
                    }
                    ipconnection.setConnectTimeout(OppoNetworkStackService.CHECK_NETWORK_TIMEOUT);
                    ipconnection.setReadTimeout(OppoNetworkStackService.CHECK_NETWORK_TIMEOUT);
                    ipconnection.setInstanceFollowRedirects(false);
                    ipconnection.setRequestProperty("Connection", "close");
                    ipconnection.connect();
                    OppoNetworkStackService oppoNetworkStackService = OppoNetworkStackService.this;
                    oppoNetworkStackService.logFunc(OppoNetworkStackService.TAG, "phowedcard checkNetworkWork start! needCheckHttpHeader = " + OppoNetworkStackService.this.needCheckHttpHeader);
                    if (OppoNetworkStackService.this.needCheckHttpHeader) {
                        int httpResponseCode = ipconnection.getResponseCode();
                        OppoNetworkStackService oppoNetworkStackService2 = OppoNetworkStackService.this;
                        oppoNetworkStackService2.logFunc(OppoNetworkStackService.TAG, "owedcard checkNetworkWork start! httpResponseCode = " + httpResponseCode);
                        if (httpResponseCode >= 300 && httpResponseCode <= 400) {
                            OppoNetworkStackService.this.hasRedirectInCellular = true;
                            Log.w(OppoNetworkStackService.TAG, "owedcard hasSpecialBehaviorInCellular = true ");
                        } else if (httpResponseCode == 204) {
                            OppoNetworkStackService.this.hasRedirectInCellular = false;
                            OppoNetworkStackService.this.needCheckHttpHeader = false;
                            OppoNetworkStackService.this.logFunc(OppoNetworkStackService.TAG, "owedcard hasSpecialBehaviorInCellular = false ");
                        }
                    }
                    OppoNetworkStackService.this.openConnSuccess = true;
                } catch (Exception e) {
                    OppoNetworkStackService.this.openConnSuccess = false;
                    Log.w(OppoNetworkStackService.TAG, "owedcard checkNetworkWork end! Exception = " + e);
                    e.printStackTrace();
                }
                checkLatch.countDown();
            }
        }).start();
        try {
            checkLatch.await(BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
        }
        logFunc(TAG, "checkNetworkWork end !");
        this.isCheckingNetwork = false;
        long result2 = System.currentTimeMillis() - start;
        if (!this.openConnSuccess) {
            result = result2 + BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS;
        } else {
            result = result2;
        }
        if (result < 500) {
            if (network != null) {
                this.myHttpServerValid.put(String.valueOf(network.netId), true);
            } else if (dns != null) {
                this.myHttpServerValid.put(String.valueOf(dns.netId), true);
            }
        }
        if (dns == null) {
            this.allCheckResult += (checkresult + "time=" + result + StringUtils.LF);
            HashMap<String, String> hashMap = this.statisticsData;
            if (hashMap != null && hashMap.size() > 1) {
                this.statisticsData.put("key_tcp_handshake_time", "" + result);
                maybeTriggerRestoreStatsData();
            }
            return result;
        }
        boolean isServerNormal = false;
        Boolean IsServerNormal = this.myHttpServerValid.get(String.valueOf(dns.netId));
        if (IsServerNormal != null) {
            isServerNormal = IsServerNormal.booleanValue();
        }
        logFunc(TAG, "checkNetworkWork quick result=" + result + ",lastTotalScore=" + this.lastTotalScore + ",latencyMs=" + dns.latencyMs + ",isServerNormal=" + isServerNormal);
        RingBuffer<DnsEvent> tempDnsEvents = this.myDnsTracker.get(String.valueOf(dns.netId));
        boolean updateNow = false;
        if (result > ((long) this.badTcpLantency) && this.lastTotalScore > 80 && dns.latencyMs > this.badDnsLantency && isServerNormal) {
            this.lastTcpScore = 70;
            this.lastDnsScore = dns.latencyMs;
            for (int i2 = 1; i2 < 8; i2++) {
                if (tempDnsEvents != null) {
                    tempDnsEvents.append(dns);
                } else {
                    logFunc(TAG, "tempDnsEvents == null!");
                }
            }
            updateNow = true;
            logFunc(TAG, "checkNetworkWork quick --score !");
        }
        if (result < 500 && (((i = this.lastTotalScore) < 80 || i == ABNORMAL_CELLULAR_SCORE) && dns.latencyMs < 200 && !this.hasRedirectInCellular)) {
            this.lastTcpScore = 90;
            this.lastDnsScore = dns.latencyMs;
            for (int i3 = 1; i3 < 8; i3++) {
                if (tempDnsEvents != null) {
                    tempDnsEvents.append(dns);
                } else {
                    logFunc(TAG, "tempDnsEvents == null!");
                }
            }
            updateNow = true;
            logFunc(TAG, "checkNetworkWork quick ++score !");
        }
        int notifyScore = this.lastTcpScore - (this.lastDnsScore / 100);
        if (notifyScore < 0) {
            notifyScore = 0;
        }
        this.mAsyncHandler.removeMessages(103);
        Handler handler = this.mAsyncHandler;
        handler.sendMessageDelayed(handler.obtainMessage(103, 3, notifyScore), updateNow ? 0 : (long) this.notify_delay);
        return result;
    }

    public int[] getCurrentMptcpApps() {
        String mptcpApps = Settings.System.getString(this.mContext.getContentResolver(), OPPO_MPTCP_APPS);
        if (mptcpApps == null) {
            return new int[]{0};
        }
        String[] appuids = mptcpApps.split("#");
        int len = appuids.length;
        if (len == 0) {
            return new int[]{0};
        }
        int[] result = new int[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.parseInt(appuids[i]);
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isMptcpUid(int uid) {
        int[] mptcpUids = getCurrentMptcpApps();
        if (mptcpUids[0] == 0) {
            logFunc(TAG, "found MPTCP UID fail, uidlen=0 !");
            return false;
        }
        for (int i = 1; i < mptcpUids.length; i++) {
            if (mptcpUids[i] == uid) {
                Log.w(TAG, "found MPTCP UID" + uid);
                return true;
            }
        }
        logFunc(TAG, "found MPTCP UID fail2!");
        return false;
    }

    public void notifyMPTCPStatusChange() {
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            Log.w(TAG, "sleep Exception!");
        }
        int[] data = getCurrentMptcpApps();
        logFunc(TAG, "notifyMPTCPStatusChange!");
        sendToKernel((short) 33, data);
    }

    private void initBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.server.OppoNetworkStackService.AnonymousClass7 */

            public void onReceive(Context context, Intent intent) {
                Network network;
                String action = intent.getAction();
                if (action.equals("android.intent.action.SCREEN_OFF")) {
                    Log.w(OppoNetworkStackService.TAG, "onReceive ACTION_SCREEN_OFF!");
                    OppoNetworkStackService.this.isScreenOn = false;
                } else if (action.equals("android.intent.action.SCREEN_ON")) {
                    OppoNetworkStackService.this.logFunc(OppoNetworkStackService.TAG, "onReceive ACTION_SCREEN_ON!");
                    OppoNetworkStackService.this.isScreenOn = true;
                } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    if (!(OppoNetworkStackService.this.mCm == null || (network = OppoNetworkStackService.this.mCm.getActiveNetwork()) == null)) {
                        OppoNetworkStackService.this.curDefaultNetid = network.netId;
                        if (OppoNetworkStackService.this.mCm.getNetworkCapabilities(network).hasTransport(1)) {
                            OppoNetworkStackService.this.curDefaultNetworkType = "WLAN";
                        } else if (OppoNetworkStackService.this.mCm.getNetworkCapabilities(network).hasTransport(0)) {
                            OppoNetworkStackService.this.curDefaultNetworkType = "CELLULAR";
                        } else {
                            OppoNetworkStackService.this.curDefaultNetworkType = "Unknown";
                        }
                        OppoNetworkStackService oppoNetworkStackService = OppoNetworkStackService.this;
                        oppoNetworkStackService.logFunc(OppoNetworkStackService.TAG, "onReceive CONNECTIVITY_ACTION!,curDefaultNetid = " + OppoNetworkStackService.this.curDefaultNetid);
                    }
                    OppoNetworkStackService.this.isScreenOn = true;
                }
            }
        };
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
    }

    private void registerNetidChange() {
        if (!this.hasRegisterFromCS) {
            this.myNetworkCallback = new ConnectivityManager.NetworkCallback() {
                /* class com.android.server.OppoNetworkStackService.AnonymousClass8 */

                public void onAvailable(Network network) {
                    try {
                        if (((RingBuffer) OppoNetworkStackService.this.myDnsTracker.get(String.valueOf(network.netId))) == null) {
                            OppoNetworkStackService.this.myDnsTracker.put(String.valueOf(network.netId), new RingBuffer<>(DnsEvent.class, 8));
                        }
                        OppoNetworkStackService.this.getLocationMcc();
                        synchronized (OppoNetworkStackService.this.myDnsListener) {
                            OppoNetworkStackService.this.toomany47error = 0;
                            OppoNetworkStackService.this.processDnsEventRef = 0;
                        }
                        try {
                            if (OppoNetworkStackService.this.mWifiRomUpdateHelper != null) {
                                OppoNetworkStackService.this.setOppoNetworkStackConfig(OppoNetworkStackService.this.mWifiRomUpdateHelper.getValue("OPPO_NETWORK_STACK_PARAMS", "0,0,0,0,0,0,0,0,0,0"));
                                if (OppoNetworkStackService.this.configParams[5] > 0) {
                                    OppoNetworkStackService.this.statisticBadScoreInterval = OppoNetworkStackService.this.configParams[5];
                                }
                                if (OppoNetworkStackService.this.configParams[6] > 0) {
                                    OppoNetworkStackService.this.statisticGoodScoreInterval = OppoNetworkStackService.this.configParams[6];
                                }
                                if (OppoNetworkStackService.this.configParams[7] > 0) {
                                    OppoNetworkStackService.this.badDnsLantency = OppoNetworkStackService.this.configParams[7];
                                }
                                if (OppoNetworkStackService.this.configParams[8] > 0) {
                                    OppoNetworkStackService.this.badTcpLantency = OppoNetworkStackService.this.configParams[8];
                                }
                                OppoNetworkStackService.this.myCheckUrl = OppoNetworkStackService.this.mWifiRomUpdateHelper.getValue("OPPO_NETWORK_STACK_CHECK_SERVER", "http://conn1.coloros.com/generate_204");
                                OppoNetworkStackService.this.myCheckUrlExp = OppoNetworkStackService.this.mWifiRomUpdateHelper.getValue("OPPO_NETWORK_STACK_CHECK_SERVER_EXP", "http://connectivitycheck.gstatic.com/generate_204");
                                if (OppoNetworkStackService.this.configParams[0] != 0) {
                                    OppoNetworkStackService.this.bNetworkStackScoreFeature = false;
                                    OppoNetworkStackService.this.stopListening();
                                    Log.w(OppoNetworkStackService.TAG, "disable network stack score feature.");
                                }
                            }
                        } catch (Exception e) {
                            Log.w(OppoNetworkStackService.TAG, "setOppoNetworkStackConfig exception.");
                            e.printStackTrace();
                        }
                        OppoNetworkStackService.this.hasRedirectInCellular = false;
                        OppoNetworkStackService.this.needCheckHttpHeader = false;
                        OppoNetworkStackService.this.myHttpServerValid.put(String.valueOf(network.netId), false);
                        if (OppoNetworkStackService.this.mCm.getNetworkCapabilities(network).hasTransport(0) && !OppoNetworkStackService.this.isCheckingNetwork) {
                            Log.w(OppoNetworkStackService.TAG, "owedcard start check!");
                            OppoNetworkStackService.this.needCheckHttpHeader = true;
                            long result = OppoNetworkStackService.this.checkNetworkWork(null, network);
                            Log.w(OppoNetworkStackService.TAG, "owedcard after checkNetworkWork hasSpecialBehaviorInCellular=" + OppoNetworkStackService.this.hasRedirectInCellular + ",result=" + result);
                            if (!OppoNetworkStackService.this.hasRedirectInCellular && result < BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS) {
                                OppoNetworkStackService.this.needCheckHttpHeader = false;
                            }
                        } else if (OppoNetworkStackService.this.mCm.getNetworkCapabilities(network).hasTransport(1)) {
                            OppoNetworkStackService.this.hasRedirectInCellular = false;
                            OppoNetworkStackService.this.needCheckHttpHeader = false;
                            OppoNetworkStackService.this.checkNetworkWork(null, network);
                            Log.w(OppoNetworkStackService.TAG, "owedcard hasSpecialBehaviorInCellular = false ,on Available WIFI.");
                        }
                        Log.w(OppoNetworkStackService.TAG, "Network onAvailable" + network);
                    } catch (Exception e2) {
                        Log.e(OppoNetworkStackService.TAG, "Exception when onAvailable:" + e2);
                    }
                }

                public void onUnavailable() {
                }

                public void onLost(Network lostNetwork) {
                    try {
                        if (OppoNetworkStackService.this.mySocketTracker.containsKey(String.valueOf(lostNetwork.netId))) {
                            OppoNetworkStackService.this.mySocketTracker.remove(String.valueOf(lostNetwork.netId));
                        }
                        if (OppoNetworkStackService.this.myDnsTracker.containsKey(String.valueOf(lostNetwork.netId))) {
                            OppoNetworkStackService.this.myDnsTracker.remove(String.valueOf(lostNetwork.netId));
                        }
                        OppoNetworkStackService.this.myHttpServerValid.remove(String.valueOf(lostNetwork.netId));
                        Log.w(OppoNetworkStackService.TAG, "Network lost lostNetwork.netId = " + lostNetwork.netId);
                    } catch (Exception e) {
                        Log.e(OppoNetworkStackService.TAG, "Exception when onLost:" + e);
                    }
                }
            };
            ConnectivityManager connectivityManager = this.mCm;
            if (connectivityManager != null) {
                connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().addCapability(12).build(), this.myNetworkCallback);
                this.hasRegisterFromCS = true;
                return;
            }
            Log.w(TAG, "mCm == null!");
        }
    }

    private void startListenKernel() {
        if (mNlFd != null) {
            Log.w(TAG, "Already listening!!");
        } else {
            new Thread() {
                /* class com.android.server.OppoNetworkStackService.AnonymousClass9 */

                public void run() {
                    OppoNetworkStackService oppoNetworkStackService = OppoNetworkStackService.this;
                    oppoNetworkStackService.logFunc(OppoNetworkStackService.TAG, "startListenKernel tid=" + Thread.currentThread().getId());
                    try {
                        FileDescriptor unused = OppoNetworkStackService.mNlFd = NetlinkSocket.forProto(37);
                        Os.bind(OppoNetworkStackService.mNlFd, SocketUtils.makeNetlinkSocketAddress(100, 0));
                        NetlinkSocket.connectToKernel(OppoNetworkStackService.mNlFd);
                        boolean sendToKernel = OppoNetworkStackService.this.sendToKernel((OppoNetworkStackService) 32, (short) 12345);
                        OppoNetworkStackService oppoNetworkStackService2 = OppoNetworkStackService.this;
                        oppoNetworkStackService2.logFunc(OppoNetworkStackService.TAG, "After sending pid:result=" + sendToKernel);
                        while (true) {
                            ByteBuffer bytes = NetlinkSocket.recvMessage(OppoNetworkStackService.mNlFd, 8192, 0);
                            StructNlMsgHdr nlmsghdr = StructNlMsgHdr.parse(bytes);
                            if (nlmsghdr != null) {
                                if (nlmsghdr.nlmsg_type != 48) {
                                    Log.w(OppoNetworkStackService.TAG, "Received unknow message:type=" + ((int) nlmsghdr.nlmsg_type));
                                } else {
                                    OppoNetworkStackService.this.logFunc(OppoNetworkStackService.TAG, "Received message:OPPO_SEND_TCP_RETRANSMIT");
                                    if (nlmsghdr.nlmsg_len < 28) {
                                        Log.e(OppoNetworkStackService.TAG, "Received message:OPPO_SEND_TCP_RETRANSMIT invalid length");
                                    } else {
                                        int score = bytes.getInt(24);
                                        if (score != OppoNetworkStackService.this.lastTcpScore) {
                                            Log.w(OppoNetworkStackService.TAG, "Score fromkernel lastTcpScore = " + score);
                                            OppoNetworkStackService.this.lastTcpScore = score;
                                            OppoNetworkStackService.this.mAsyncHandler.removeMessages(103);
                                            int notifyScore = score - (OppoNetworkStackService.this.lastDnsScore / 100);
                                            if (notifyScore < 0) {
                                                notifyScore = 0;
                                            }
                                            OppoNetworkStackService.this.mAsyncHandler.removeMessages(103);
                                            OppoNetworkStackService.this.mAsyncHandler.sendMessageDelayed(OppoNetworkStackService.this.mAsyncHandler.obtainMessage(103, 0, notifyScore), (long) OppoNetworkStackService.this.notify_delay);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(OppoNetworkStackService.TAG, "Exception when startListenKernel:tid=" + Thread.currentThread().getId(), new Throwable());
                    }
                }
            }.start();
        }
    }

    public void stopListening() {
        if (mNlFd != null) {
            logFunc(TAG, "stopListening");
            IoUtils.closeQuietly(mNlFd);
            mNlFd = null;
        }
        try {
            this.mActivityManager.removeOnUidImportanceListener(this.mUidImportanceListener);
            releaseCellularNetwork();
            this.mCm.unregisterNetworkCallback(this.myNetworkCallback);
        } catch (Exception e) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean sendToKernel(short type, int data) {
        if (mNlFd != null) {
            try {
                StructNlMsgHdr nlMsgHdr = new StructNlMsgHdr();
                nlMsgHdr.nlmsg_len = 20;
                nlMsgHdr.nlmsg_type = type;
                nlMsgHdr.nlmsg_flags = 1;
                nlMsgHdr.nlmsg_pid = 100;
                byte[] msg = new byte[nlMsgHdr.nlmsg_len];
                ByteBuffer byteBuffer = ByteBuffer.wrap(msg);
                byteBuffer.order(ByteOrder.nativeOrder());
                nlMsgHdr.pack(byteBuffer);
                byteBuffer.putInt(data);
                if (msg.length == NetlinkSocket.sendMessage(mNlFd, msg, 0, msg.length, 300)) {
                    return true;
                }
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Exception when sendToKernel:" + e);
            }
        }
        return false;
    }

    private boolean sendToKernel(short type, int[] data) {
        if (mNlFd != null) {
            try {
                StructNlMsgHdr nlMsgHdr = new StructNlMsgHdr();
                nlMsgHdr.nlmsg_len = 16 + ((data == null ? 0 : data.length) * 4);
                nlMsgHdr.nlmsg_type = type;
                nlMsgHdr.nlmsg_flags = 1;
                nlMsgHdr.nlmsg_pid = 100;
                byte[] msg = new byte[nlMsgHdr.nlmsg_len];
                ByteBuffer byteBuffer = ByteBuffer.wrap(msg);
                byteBuffer.order(ByteOrder.nativeOrder());
                nlMsgHdr.pack(byteBuffer);
                if (data != null) {
                    for (int i : data) {
                        byteBuffer.putInt(i);
                    }
                }
                if (msg.length == NetlinkSocket.sendMessage(mNlFd, msg, 0, msg.length, 300)) {
                    return true;
                }
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Exception when sendToKernel:" + e);
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showMyToast(String text, int cnt) {
        if (SystemProperties.getBoolean("persist.oppo.networkstack.show", false) || this.configParams[1] > 0) {
            Toast toast = this.myToast;
            if (toast != null) {
                toast.cancel();
            }
            if (this.hasRedirectInCellular) {
                text = text + ",maybe has abnormal SIM card.";
            }
            this.myToast = Toast.makeText(this.mContext, text, 1);
            this.myToast.show();
        }
    }

    public void enableScreenshotDetect() throws RemoteException {
    }

    public void registerTcpScoreChange(IOppoNetScoreChange scorechange) throws RemoteException {
        Log.w(TAG, "registerTcpScoreChange in the service.");
        synchronized (this.mScoreChangeLists) {
            if (!this.mScoreChangeLists.contains(scorechange)) {
                this.mScoreChangeLists.add(scorechange);
            }
        }
    }

    public void unregisterTcpScoreChange(IOppoNetScoreChange scorechange) throws RemoteException {
        synchronized (this.mScoreChangeLists) {
            if (this.mScoreChangeLists.contains(scorechange)) {
                this.mScoreChangeLists.remove(scorechange);
            }
        }
    }

    public int getPortalResult(Network network, int timeout) throws RemoteException {
        return 1;
    }

    public boolean isGatewayConflict(Network network) throws RemoteException {
        return false;
    }

    public boolean enableDUALWIFIUid(int uid) throws RemoteException {
        return false;
    }

    public boolean enableMPTCPUid(int uid) throws RemoteException {
        return false;
    }

    public boolean enableSLAUid(int uid) throws RemoteException {
        return false;
    }

    public boolean disableDUALWIFIUid(int uid) throws RemoteException {
        return false;
    }

    public boolean disableMPTCPUid(int uid) throws RemoteException {
        return false;
    }

    public boolean disableSLAPUid(int uid) throws RemoteException {
        return false;
    }

    public int getNetworkScore(Network network) throws RemoteException {
        TcpSocketTracker tst = this.mySocketTracker.get(String.valueOf(network.netId));
        if (tst == null) {
            tst = new TcpSocketTracker(new TcpSocketTracker.Dependencies(this.mContext, true), network);
            this.mySocketTracker.put(String.valueOf(network.netId), tst);
        }
        tst.pollSocketsInfo();
        int result = tst.getLatestPacketFailPercentage();
        Log.w(TAG, "getNetworkScore getLatestPacketFailPercentage in the service result = " + result + "network=" + network);
        return this.lastTotalScore;
    }

    public String getOppoNetworkStackInfo() throws RemoteException {
        return "current: lastDnsScore=" + this.lastDnsScore + ",lastTcpScore=" + this.lastTcpScore + ",lastTotalScore=" + this.lastTotalScore + ",mNotifyScoreApp=" + this.mNotifyScoreApp + StringUtils.LF + this.allCheckResult;
    }

    public void setOppoNetworkStackConfig(String config) throws RemoteException {
        Log.w(TAG, "setOppoNetworkStackConfig:" + config);
        String[] params = config.split(",");
        int[] iArr = this.configParams;
        int len = iArr.length < params.length ? iArr.length : params.length;
        for (int i = 0; i < len; i++) {
            this.configParams[i] = Integer.parseInt(params[i]);
        }
    }
}
