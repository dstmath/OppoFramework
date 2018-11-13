package com.mediatek.location;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.LocalSocketAddress.Namespace;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.UserHandle;
import android.util.Log;
import com.mediatek.location.Agps2FrameworkInterface.Agps2FrameworkInterfaceReceiver;
import com.mediatek.location.Framework2AgpsInterface.Framework2AgpsInterfaceSender;
import com.mediatek.socket.base.UdpClient;
import com.mediatek.socket.base.UdpServer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

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
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class AgpsHelper extends Thread {
    private static final String CHANNEL_IN = "mtk_agps2framework";
    private static final String CHANNEL_OUT = "mtk_framework2agps";
    private static final int CMD_NET_TIMEOUT = 102;
    private static final int CMD_QUERY_DNS = 101;
    private static final int CMD_RELEASE_NET = 103;
    private static final int CMD_REMOVE_GPS_ICON = 105;
    private static final int CMD_REQUEST_GPS_ICON = 104;
    private static final int CMD_REQUEST_NET = 100;
    private static final boolean DEBUG = false;
    private static final long NET_REQ_TIMEOUT = 10000;
    private static final String TAG = "MtkAgpsHelper";
    private static final String WAKELOCK_KEY = "MtkAgps";
    private final ArrayList<AgpsNetReq> mAgpsNetReqs;
    private final ConnectivityManager mConnManager;
    private final Context mContext;
    private final byte[] mEmptyIpv6;
    private Framework2AgpsInterfaceSender mFwkToAgps;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private final LocationExt mLocExt;
    private NetworkRequest mNetReqEmergency;
    private NetworkRequest mNetReqIms;
    private NetworkRequest mNetReqSupl;
    private UdpServer mNodeIn;
    private UdpClient mNodeOut;
    private PowerManager mPowerManager;
    private Agps2FrameworkInterfaceReceiver mReceiver;
    private WakeLock mWakeLock;

    class AgpsNetReq {
        String mFqdn;
        boolean mIsEsupl;
        boolean mIsQueried = false;
        boolean mIsSuplApn;
        Network mNet = null;
        NetworkRequest mNetReq = null;
        NetworkCallback mNetworkCallback = null;

        AgpsNetReq(String fqdn, boolean isEsupl, boolean isSuplApn) {
            this.mFqdn = fqdn;
            this.mIsEsupl = isEsupl;
            this.mIsSuplApn = isSuplApn;
        }

        void decideRoute() {
            Network netEmergemcy = null;
            Network netIms = null;
            Network netSupl = null;
            Network[] nets = AgpsHelper.this.mConnManager.getAllNetworks();
            if (nets != null) {
                for (Network net : nets) {
                    NetworkCapabilities netCap = AgpsHelper.this.mConnManager.getNetworkCapabilities(net);
                    if (AgpsHelper.DEBUG) {
                        AgpsHelper.log("checking net=" + net + " cap=" + netCap);
                    }
                    if (netEmergemcy == null && netCap != null && netCap.hasCapability(10)) {
                        netEmergemcy = net;
                        if (AgpsHelper.DEBUG) {
                            AgpsHelper.log("NetEmergemcy");
                        }
                    }
                    if (netIms == null && netCap != null && netCap.hasCapability(4)) {
                        netIms = net;
                        if (AgpsHelper.DEBUG) {
                            AgpsHelper.log("NetIms");
                        }
                    }
                    if (netSupl == null && netCap != null && netCap.hasCapability(1)) {
                        netSupl = net;
                        if (AgpsHelper.DEBUG) {
                            AgpsHelper.log("NetSupl");
                        }
                    }
                }
            }
            if (this.mIsEsupl) {
                if (netEmergemcy != null) {
                    if (AgpsHelper.DEBUG) {
                        AgpsHelper.log("to use NetEmergemcy");
                    }
                    this.mNet = netEmergemcy;
                    this.mNetReq = AgpsHelper.this.mNetReqEmergency;
                    return;
                } else if (netIms != null) {
                    if (AgpsHelper.DEBUG) {
                        AgpsHelper.log("to use NetIms");
                    }
                    this.mNet = netIms;
                    this.mNetReq = AgpsHelper.this.mNetReqIms;
                    return;
                }
            }
            if (this.mIsSuplApn && AgpsHelper.this.mLocExt.hasIccCard() && !AgpsHelper.this.mLocExt.isAirplaneModeOn()) {
                if (AgpsHelper.DEBUG) {
                    AgpsHelper.log("try to use NetSupl");
                }
                this.mNet = netSupl;
                this.mNetReq = AgpsHelper.this.mNetReqSupl;
            }
        }

        void requestNet() {
            boolean isDirectDns = false;
            decideRoute();
            if (this.mNetReq != null) {
                this.mNetworkCallback = new NetworkCallback() {
                    public void onAvailable(Network net) {
                        if (AgpsHelper.DEBUG) {
                            AgpsHelper.log("onAvailable: network=" + net);
                        }
                        synchronized (AgpsNetReq.this) {
                            if (AgpsNetReq.this.mNet == null) {
                                AgpsNetReq.this.mNet = net;
                                AgpsHelper.this.removeMessages(102, AgpsNetReq.this);
                                AgpsHelper.this.sendMessage(101, AgpsNetReq.this);
                            }
                        }
                    }

                    public void onLost(Network net) {
                        if (AgpsHelper.DEBUG) {
                            AgpsHelper.log("onLost: network=" + net);
                        }
                    }
                };
                synchronized (this) {
                    if (AgpsHelper.DEBUG) {
                        AgpsHelper.log("request net:" + this.mNetReq);
                    }
                    AgpsHelper.this.mConnManager.requestNetwork(this.mNetReq, this.mNetworkCallback);
                    if (this.mNet == null) {
                        if (AgpsHelper.DEBUG) {
                            AgpsHelper.log("wait for net callback");
                        }
                        AgpsHelper.this.sendMessageDelayed(102, this, 10000);
                    } else {
                        isDirectDns = true;
                    }
                }
            } else {
                isDirectDns = true;
            }
            if (isDirectDns) {
                queryDns();
            }
        }

        void queryDns() {
            if (!this.mIsQueried) {
                this.mIsQueried = true;
                boolean hasIpv4 = false;
                boolean hasIpv6 = false;
                int ipv4 = 0;
                byte[] ipv6 = AgpsHelper.this.mEmptyIpv6;
                try {
                    InetAddress[] ias;
                    if (this.mNet != null) {
                        ias = this.mNet.getAllByName(this.mFqdn);
                    } else {
                        ias = InetAddress.getAllByName(this.mFqdn);
                    }
                    for (InetAddress ia : ias) {
                        byte[] addr = ia.getAddress();
                        AgpsHelper.log("ia=" + ia.toString() + " bytes=" + Arrays.toString(addr) + " network=" + this.mNet);
                        if (addr.length == 4 && !hasIpv4) {
                            hasIpv4 = true;
                            ipv4 = ((((((addr[3] & 255) << 8) | (addr[2] & 255)) << 8) | (addr[1] & 255)) << 8) | (addr[0] & 255);
                        } else if (addr.length == 16 && !hasIpv6) {
                            hasIpv6 = true;
                            ipv6 = addr;
                        }
                    }
                } catch (UnknownHostException e) {
                    AgpsHelper.log("UnknownHostException for fqdn=" + this.mFqdn);
                }
                boolean isSuccess = !hasIpv4 ? hasIpv6 : true;
                boolean hasNetId = this.mNet != null;
                int netId = hasNetId ? this.mNet.netId : -1;
                boolean ret = AgpsHelper.this.mFwkToAgps.DnsQueryResult2(AgpsHelper.this.mNodeOut, isSuccess, hasIpv4, ipv4, hasIpv6, ipv6, hasNetId, netId);
                if (AgpsHelper.DEBUG) {
                    AgpsHelper.log("DnsQueryResult() fqdn=" + this.mFqdn + " isSuccess=" + isSuccess + " hasIpv4=" + hasIpv4 + " ipv4=" + Integer.toHexString(ipv4) + " hasIpv6=" + hasIpv6 + " ipv6=" + Arrays.toString(ipv6) + " hasNetId=" + hasNetId + " netId=" + netId + " ret=" + ret);
                }
                if (!isSuccess) {
                    AgpsHelper.this.doReleaseNet(this);
                }
            }
        }

        synchronized void releaseNet() {
            if (AgpsHelper.DEBUG) {
                AgpsHelper.log("releaseNet() fqdn=" + this.mFqdn + " eSupl=" + this.mIsEsupl + " suplApn=" + this.mIsSuplApn);
            }
            if (this.mNetworkCallback != null) {
                if (AgpsHelper.DEBUG) {
                    AgpsHelper.log("remove net callback");
                }
                AgpsHelper.this.mConnManager.unregisterNetworkCallback(this.mNetworkCallback);
                this.mNetworkCallback = null;
                AgpsHelper.this.removeMessages(102, this);
            }
            this.mIsQueried = true;
            this.mNetReq = null;
            this.mNet = null;
            this.mFqdn = null;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.location.AgpsHelper.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.location.AgpsHelper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.location.AgpsHelper.<clinit>():void");
    }

    public static void log(String msg) {
        Log.d(TAG, msg);
    }

    public AgpsHelper(LocationExt locExt, Context context, ConnectivityManager connMgr) {
        this.mAgpsNetReqs = new ArrayList(2);
        this.mEmptyIpv6 = new byte[16];
        this.mReceiver = new Agps2FrameworkInterfaceReceiver() {
            public void isExist() {
                if (AgpsHelper.DEBUG) {
                    AgpsHelper.log("isExist()");
                }
            }

            public void acquireWakeLock() {
                if (AgpsHelper.DEBUG) {
                    AgpsHelper.log("acquireWakeLock()");
                }
                AgpsHelper.this.mWakeLock.acquire();
            }

            public void releaseWakeLock() {
                if (AgpsHelper.DEBUG) {
                    AgpsHelper.log("releaseWakeLock()");
                }
                AgpsHelper.this.mWakeLock.release();
            }

            public void requestDedicatedApnAndDnsQuery(String fqdn, boolean isEsupl, boolean isSuplApn) {
                if (AgpsHelper.DEBUG) {
                    AgpsHelper.log("requestDedicatedApnAndDnsQuery() fqdn=" + fqdn + " isEsupl=" + isEsupl + " isSuplApn=" + isSuplApn);
                }
                AgpsHelper.this.sendMessage(100, new AgpsNetReq(fqdn, isEsupl, isSuplApn));
            }

            public void releaseDedicatedApn() {
                if (AgpsHelper.DEBUG) {
                    AgpsHelper.log("releaseDedicatedApn()");
                }
                AgpsHelper.this.sendMessage(103, null);
            }

            public void requestGpsIcon() {
                if (AgpsHelper.DEBUG) {
                    AgpsHelper.log("requestGpsIcon");
                }
                AgpsHelper.this.sendMessage(104, null);
            }

            public void removeGpsIcon() {
                if (AgpsHelper.DEBUG) {
                    AgpsHelper.log("removeGpsIcon()");
                }
                AgpsHelper.this.sendMessage(105, null);
            }
        };
        if (DEBUG) {
            log("AgpsHelper constructor");
        }
        this.mLocExt = locExt;
        this.mContext = context;
        this.mConnManager = connMgr;
        new Thread("MtkAgpsSocket") {
            public void run() {
                if (AgpsHelper.DEBUG) {
                    AgpsHelper.log("SocketThread.run()");
                }
                AgpsHelper.this.waitForAgpsCommands();
            }
        }.start();
    }

    protected void setup() {
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mWakeLock = this.mPowerManager.newWakeLock(1, WAKELOCK_KEY);
        this.mWakeLock.setReferenceCounted(true);
        Builder nrBuilder = new Builder();
        this.mNetReqEmergency = nrBuilder.addTransportType(0).addCapability(10).build();
        this.mNetReqIms = nrBuilder.removeCapability(10).addCapability(4).build();
        this.mNetReqSupl = nrBuilder.removeCapability(4).addCapability(1).build();
        this.mHandlerThread = new HandlerThread("MtkAgpsHandler");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 100:
                        AgpsHelper.this.handleRequestNet((AgpsNetReq) msg.obj);
                        return;
                    case 101:
                        AgpsHelper.this.handleDnsQuery((AgpsNetReq) msg.obj);
                        return;
                    case 102:
                        AgpsHelper.this.handleNetTimeout((AgpsNetReq) msg.obj);
                        return;
                    case 103:
                        AgpsHelper.this.handleReleaseNet((AgpsNetReq) msg.obj);
                        return;
                    case 104:
                        AgpsHelper.this.handleRequestGpsIcon();
                        return;
                    case 105:
                        AgpsHelper.this.handleRemoveGpsIcon();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    protected void waitForAgpsCommands() {
        setup();
        try {
            this.mNodeOut = new UdpClient(CHANNEL_OUT, Namespace.ABSTRACT, 40);
            this.mNodeIn = new UdpServer(CHANNEL_IN, Namespace.ABSTRACT, Agps2FrameworkInterface.MAX_BUFF_SIZE);
            this.mFwkToAgps = new Framework2AgpsInterfaceSender();
            while (true) {
                this.mReceiver.readAndDecode(this.mNodeIn);
            }
        } catch (Exception e) {
            log(e.toString());
        } finally {
            if (this.mNodeIn != null) {
                this.mNodeIn.close();
                this.mNodeIn = null;
            }
            this.mReceiver = null;
        }
    }

    void sendMessage(int what, Object obj) {
        this.mHandler.obtainMessage(what, 0, 0, obj).sendToTarget();
    }

    void sendMessageDelayed(int what, Object obj, long delayMillis) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(what, 0, 0, obj), delayMillis);
    }

    void removeMessages(int what, Object obj) {
        this.mHandler.removeMessages(what, obj);
    }

    void doReleaseNet(AgpsNetReq req) {
        if (DEBUG) {
            log("doReleaseNet");
        }
        this.mAgpsNetReqs.remove(req);
        req.releaseNet();
    }

    void handleRequestNet(AgpsNetReq req) {
        if (DEBUG) {
            log("handleRequestNet");
        }
        while (this.mAgpsNetReqs.size() >= 2) {
            if (DEBUG) {
                log("remove potential leak of AgpsNetReq");
            }
            doReleaseNet((AgpsNetReq) this.mAgpsNetReqs.get(0));
        }
        this.mAgpsNetReqs.add(req);
        req.requestNet();
    }

    void handleDnsQuery(AgpsNetReq req) {
        if (DEBUG) {
            log("handleDnsQuery");
        }
        req.queryDns();
    }

    void handleNetTimeout(AgpsNetReq req) {
        if (DEBUG) {
            log("handleNetTimeout");
        }
        req.queryDns();
    }

    void handleReleaseNet(AgpsNetReq req) {
        if (DEBUG) {
            log("handleReleaseNet");
        }
        if (req != null) {
            doReleaseNet(req);
        } else if (!this.mAgpsNetReqs.isEmpty()) {
            doReleaseNet((AgpsNetReq) this.mAgpsNetReqs.get(0));
        }
    }

    void handleRequestGpsIcon() {
        Intent intent = new Intent("android.location.HIGH_POWER_REQUEST_CHANGE");
        intent.putExtra("requestGpsByNi", true);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    void handleRemoveGpsIcon() {
        Intent intent = new Intent("android.location.HIGH_POWER_REQUEST_CHANGE");
        intent.putExtra("requestGpsByNi", false);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }
}
