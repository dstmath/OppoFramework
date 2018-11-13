package com.mediatek.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.LocationManagerService;
import com.android.server.display.OppoBrightUtils;
import com.android.server.location.LocationFudger;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

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
public class NlpUtils {
    private static final boolean DEBUG = false;
    private static final int LAST_LOCATION_EXPIRED_TIMEOUT = 600000;
    private static final boolean NIJ_ON_GPS_START_DEFAULT = false;
    private static final int NLPS_CMD_GPS_NIJ_CANCEL = 102;
    private static final int NLPS_CMD_GPS_NIJ_REQ = 101;
    private static final int NLPS_CMD_QUIT = 100;
    private static final int NLPS_MAX_CLIENTS = 2;
    private static final int NLPS_MSG_CLEAR_LAST_LOC = 5;
    private static final int NLPS_MSG_GPS_STARTED = 0;
    private static final int NLPS_MSG_GPS_STOPPED = 1;
    private static final int NLPS_MSG_NLP_NIJ_CANCEL = 3;
    private static final int NLPS_MSG_NLP_NIJ_REQ = 2;
    private static final int NLPS_MSG_NLP_UPDATED = 4;
    private static final int NLP_CMD_SRC_APM = 2;
    private static final int NLP_CMD_SRC_MNL = 1;
    private static final int NLP_CMD_SRC_UNKNOWN = 0;
    private static final String PROP_NLP_ENABLED = "persist.sys.nlp.enabled";
    protected static final String SOCKET_ADDRESS = "com.mediatek.nlpservice.NlpService";
    private static final int UPDATE_LOCATION = 7;
    private AtomicInteger mClientCount;
    private Context mContext;
    private Handler mGpsHandler;
    private NlpsMsgHandler mHandler;
    private volatile boolean mIsStopping;
    private Location mLastLocation;
    private LocationManager mLocationManager;
    private LocationListener mNetworkLocationListener;
    private int mNlpRequestSrc;
    private LocalServerSocket mNlpServerSocket;
    private LocationListener mPassiveLocationListener;
    private Thread mServerThread;

    private class NlpsMsgHandler extends Handler {
        public NlpsMsgHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    if (NlpUtils.DEBUG) {
                        NlpUtils.log("handle NLPS_MSG_NLP_NIJ_REQ arg1: " + msg.arg1);
                    }
                    int reqSrc = msg.arg1;
                    if (reqSrc == 0) {
                        reqSrc = 1;
                    }
                    NlpUtils.this.requestNlp(reqSrc);
                    return;
                case 3:
                    if (NlpUtils.DEBUG) {
                        NlpUtils.log("handle NLPS_MSG_NLP_NIJ_CANCEL arg1: " + msg.arg1);
                    }
                    NlpUtils.this.releaseNlp(msg.arg1);
                    return;
                case 4:
                    if (NlpUtils.DEBUG) {
                        NlpUtils.log("handle NLPS_MSG_NLP_UPDATED");
                    }
                    NlpUtils.this.releaseNlp(1);
                    return;
                case 5:
                    if (NlpUtils.DEBUG) {
                        NlpUtils.log("handle NLPS_MSG_CLEAR_LAST_LOC");
                    }
                    NlpUtils.this.clearLastLocation();
                    return;
                default:
                    NlpUtils.log("Undefined message: " + msg.what);
                    return;
            }
        }
    }

    private class ServerInstanceThread extends Thread {
        LocalSocket mSocket;

        public ServerInstanceThread(LocalSocket instanceSocket) {
            this.mSocket = instanceSocket;
            NlpUtils.this.mClientCount.getAndIncrement();
            if (NlpUtils.DEBUG) {
                NlpUtils.log("client count+: " + NlpUtils.this.mClientCount.get());
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:15:0x004d  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            try {
                if (NlpUtils.DEBUG) {
                    NlpUtils.log("NlpInstanceSocket+");
                }
                DataInputStream dins = new DataInputStream(this.mSocket.getInputStream());
                while (!NlpUtils.this.mIsStopping) {
                    int cmd = DataCoder.getInt(dins);
                    int data1 = DataCoder.getInt(dins);
                    int data2 = DataCoder.getInt(dins);
                    int data3 = DataCoder.getInt(dins);
                    if (cmd == 101) {
                        NlpUtils.log("ClientCmd: NLPS_CMD_GPS_INJECT_REQ");
                        NlpUtils.this.sendCommand(2, data1);
                    } else if (cmd == 102) {
                        NlpUtils.log("ClientCmd: NLPS_CMD_GPS_NIJ_CANCEL");
                        NlpUtils.this.sendCommand(3, data1);
                    } else {
                        if (cmd != 100) {
                            NlpUtils.log("ClientCmd, unknown: " + cmd);
                        } else if (NlpUtils.DEBUG) {
                            NlpUtils.log("ClientCmd: QUIT");
                        }
                        closeInstanceSocket();
                        if (NlpUtils.DEBUG) {
                            NlpUtils.log("NlpInstanceSocket-");
                        }
                        Thread.sleep(10);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            closeInstanceSocket();
            if (NlpUtils.DEBUG) {
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e2) {
            }
        }

        private void closeInstanceSocket() {
            NlpUtils.close(this.mSocket);
            this.mSocket = null;
            NlpUtils.this.mClientCount.getAndDecrement();
            if (NlpUtils.DEBUG) {
                NlpUtils.log("client count-: " + NlpUtils.this.mClientCount.get());
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.location.NlpUtils.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.location.NlpUtils.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.location.NlpUtils.<clinit>():void");
    }

    public NlpUtils(Context context, Handler gpsHandler) {
        this.mIsStopping = false;
        this.mClientCount = new AtomicInteger();
        this.mNlpServerSocket = null;
        this.mLastLocation = null;
        this.mNlpRequestSrc = 0;
        this.mPassiveLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (NlpUtils.this.mNlpRequestSrc != 0 && "network".equals(location.getProvider())) {
                    synchronized (this) {
                        if (NlpUtils.this.mLastLocation == null) {
                            NlpUtils.this.mLastLocation = new Location(location);
                        } else {
                            NlpUtils.this.mLastLocation.set(location);
                        }
                    }
                    NlpUtils.this.mHandler.removeMessages(5);
                    NlpUtils.this.sendCommandDelayed(5, LocationFudger.FASTEST_INTERVAL_MS);
                    NlpUtils.this.sendCommand(4, 0);
                }
            }

            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };
        this.mNetworkLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
            }

            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };
        if (DEBUG) {
            log("onCreate");
        }
        this.mLocationManager = (LocationManager) context.getSystemService("location");
        this.mContext = context;
        this.mGpsHandler = gpsHandler;
        HandlerThread handlerThread = new HandlerThread("[NlpUtils]");
        handlerThread.start();
        this.mHandler = new NlpsMsgHandler(handlerThread.getLooper());
        this.mServerThread = new Thread() {
            public void run() {
                if (NlpUtils.DEBUG) {
                    NlpUtils.log("mServerThread.run()");
                }
                NlpUtils.this.doServerTask();
            }
        };
        this.mServerThread.start();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.location.PROVIDERS_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    NlpUtils.this.connectivityAction(intent);
                } else if (!action.equals("android.location.PROVIDERS_CHANGED")) {
                } else {
                    if (NlpUtils.this.isNlpEnabled()) {
                        SystemProperties.set(NlpUtils.PROP_NLP_ENABLED, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                    } else {
                        SystemProperties.set(NlpUtils.PROP_NLP_ENABLED, "0");
                    }
                }
            }
        }, intentFilter);
        this.mLocationManager.requestLocationUpdates("passive", 0, OppoBrightUtils.MIN_LUX_LIMITI, this.mPassiveLocationListener);
    }

    private void connectivityAction(Intent intent) {
        NetworkInfo info = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(((NetworkInfo) intent.getParcelableExtra("networkInfo")).getType());
        if (intent.getBooleanExtra("noConnectivity", false) || !(info == null || info.isConnected())) {
            log("Connectivity set unConnected");
            clearLastLocation();
        }
    }

    private boolean isNlpEnabled() {
        return this.mLocationManager.isProviderEnabled("network");
    }

    private void startNlpQueryLocked(int src) {
        log("startNlpQueryLocked isNlpEnabled=" + isNlpEnabled() + " src:" + src + " mRequestSrc:" + this.mNlpRequestSrc);
        if ((this.mNlpRequestSrc & src) != 0) {
            stopNlpQueryLocked(src);
        }
        if (this.mNlpRequestSrc == 0) {
            this.mLocationManager.requestLocationUpdates("network", 1000, OppoBrightUtils.MIN_LUX_LIMITI, this.mNetworkLocationListener);
        }
        this.mNlpRequestSrc |= src;
    }

    private void stopNlpQueryLocked(int src) {
        if (DEBUG) {
            log("stopNlpQueryLocked src:" + src + " mRequestSrc:" + this.mNlpRequestSrc);
        }
        if ((this.mNlpRequestSrc & src) != 0) {
            this.mNlpRequestSrc &= ~src;
            if (this.mNlpRequestSrc == 0) {
                this.mLocationManager.removeUpdates(this.mNetworkLocationListener);
            }
        }
    }

    public static void log(String msg) {
        Log.d("NlpUtils", msg);
    }

    private static void close(LocalServerSocket lss) {
        try {
            lss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void close(LocalSocket ls) {
        try {
            ls.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void requestNlp(int src) {
        try {
            startNlpQueryLocked(src);
            if (src == 1 && this.mLastLocation != null) {
                if (DEBUG) {
                    log("inject NLP location");
                }
                this.mGpsHandler.obtainMessage(7, 0, 0, this.mLastLocation).sendToTarget();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    private synchronized void releaseNlp(int src) {
        try {
            stopNlpQueryLocked(src);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    private synchronized void closeServerSocket() {
        if (this.mNlpServerSocket != null) {
            close(this.mNlpServerSocket);
            this.mNlpServerSocket = null;
        }
    }

    public synchronized void clearLastLocation() {
        log("clearLastLocation");
        this.mLastLocation = null;
    }

    private void doServerTask() {
        try {
            if (DEBUG) {
                log("NlpUtilsSocket+");
            }
            synchronized (this) {
                this.mNlpServerSocket = new LocalServerSocket(SOCKET_ADDRESS);
                if (DEBUG) {
                    log("NlpServerSocket: " + this.mNlpServerSocket);
                }
            }
            while (!this.mIsStopping) {
                if (DEBUG) {
                    log("NlpUtilsSocket, wait client");
                }
                LocalSocket instanceSocket = this.mNlpServerSocket.accept();
                if (DEBUG) {
                    log("NlpUtilsSocket, instance: " + instanceSocket);
                }
                if (!this.mIsStopping) {
                    if (this.mClientCount.get() < 2) {
                        new ServerInstanceThread(instanceSocket).start();
                    } else {
                        log("no resource, client count: " + this.mClientCount.get());
                        close(instanceSocket);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        closeServerSocket();
        if (DEBUG) {
            log("NlpUtilsSocket-");
        }
    }

    private void sendCommand(int cmd, int arg1) {
        Message msg = Message.obtain();
        msg.what = cmd;
        msg.arg1 = arg1;
        this.mHandler.sendMessage(msg);
    }

    private void sendCommandDelayed(int cmd, long delayMs) {
        Message msg = Message.obtain();
        msg.what = cmd;
        this.mHandler.sendMessageDelayed(msg, delayMs);
    }
}
