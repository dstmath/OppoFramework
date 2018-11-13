package com.android.server.net;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.Slog;
import com.android.server.LocationManagerService;
import com.android.server.am.EventLogTags;
import com.android.server.job.controllers.JobStatus;
import com.android.server.location.FlpHardwareProvider;
import com.android.server.oppo.IElsaManager;
import com.mediatek.location.Agps2FrameworkInterface;
import com.mediatek.location.Framework2AgpsInterface;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class NetworkHttpMonitor {
    private static final String ACTION_POLL = "com.android.server.net.NetworkHttpMonitor.action.POLL";
    private static final String ACTION_ROUTING_UPDATE = "com.android.server.net.NetworkHttpMonitor.action.routing";
    private static final boolean DBG = true;
    private static final String DEFAULT_SERVER = "connectivitycheck.android.com";
    private static final int EVENT_DISABLE_FIREWALL = 2;
    private static final int EVENT_ENABLE_FIREWALL = 1;
    private static final int EVENT_KEEP_ALIVE = 3;
    private static final int EXPIRE_TIME = 1200000;
    private static final String HTTP_FIREWALL_UID = "net.http.browser.uid";
    private static final int KEEP_ALIVE_INTERVAL = 120000;
    private static final int MAX_REDIRECT_CONNECTION = 3;
    private static final int MOBILE = 0;
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final String TAG = "NetworkHttpMonitor";
    private static String WEB_LOCATION;
    private static ArrayList<String> mBrowserAppList;
    private static ArrayList<Integer> mBrowserAppUids;
    private static Context mContext;
    private static int mHttpRedirectCount;
    private static boolean mIsFirewallEnabled;
    private static INetworkManagementService mNetd;
    private static PackageManager mPackageManager;
    private ConnectivityManager cm;
    private AlarmManager mAlarmManager;
    private Handler mHandler;
    private PendingIntent mPendingPollIntent;
    final Object mRulesLock;
    private String mServer;

    private class MyHandler extends Handler {
        public MyHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            Slog.w(NetworkHttpMonitor.TAG, "msg:" + msg.what);
            switch (msg.what) {
                case 1:
                    NetworkHttpMonitor.this.enableFirewallPolicy();
                    return;
                case 2:
                    NetworkHttpMonitor.this.disableFirewall();
                    return;
                case 3:
                    NetworkHttpMonitor.this.sendKeepAlive();
                    return;
                default:
                    return;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.net.NetworkHttpMonitor.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.net.NetworkHttpMonitor.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.net.NetworkHttpMonitor.<clinit>():void");
    }

    public NetworkHttpMonitor(Context context, INetworkManagementService netd) {
        this.mRulesLock = new Object();
        mContext = context;
        mNetd = netd;
        mPackageManager = mContext.getPackageManager();
        this.mAlarmManager = (AlarmManager) mContext.getSystemService("alarm");
        this.mPendingPollIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_POLL), 0);
        registerForAlarms();
        registerForRougingUpdate();
        registerForRoutingUpdate();
        registerWifiEvent();
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new MyHandler(thread.getLooper());
        this.mServer = Global.getString(mContext.getContentResolver(), "captive_portal_server");
        if (this.mServer == null) {
            this.mServer = DEFAULT_SERVER;
        }
    }

    private void sendKeepAlive() {
        new Thread(new Runnable() {
            public void run() {
                synchronized (this) {
                    HttpURLConnection urlConnection = null;
                    String checkUrl = "http://" + NetworkHttpMonitor.this.mServer + "/generate_204";
                    Slog.w(NetworkHttpMonitor.TAG, "Checking:" + checkUrl);
                    try {
                        urlConnection = (HttpURLConnection) new URL(checkUrl).openConnection();
                        urlConnection.setInstanceFollowRedirects(false);
                        urlConnection.setConnectTimeout(10000);
                        urlConnection.setReadTimeout(10000);
                        urlConnection.setUseCaches(false);
                        urlConnection.getInputStream();
                        int status = urlConnection.getResponseCode();
                        boolean isConnected = status == 204;
                        Slog.w(NetworkHttpMonitor.TAG, "Checking status:" + status);
                        if (isConnected) {
                            if (NetworkHttpMonitor.this.isFirewallEnabled()) {
                                NetworkHttpMonitor.this.resetFirewallStatus();
                            }
                        } else if (status == Framework2AgpsInterface.PROTOCOL_TYPE || status == Agps2FrameworkInterface.PROTOCOL_TYPE || status == 303) {
                            String loc = urlConnection.getHeaderField(FlpHardwareProvider.LOCATION);
                            Slog.w(NetworkHttpMonitor.TAG, "new loc:" + loc);
                            if (loc.contains(NetworkHttpMonitor.this.getWebLocation())) {
                                if (NetworkHttpMonitor.this.isFirewallEnabled()) {
                                    NetworkHttpMonitor.this.mHandler.sendMessageDelayed(NetworkHttpMonitor.this.mHandler.obtainMessage(3), JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                                } else {
                                    NetworkHttpMonitor.this.mHandler.obtainMessage(1).sendToTarget();
                                    NetworkHttpMonitor.mIsFirewallEnabled = true;
                                }
                            }
                        }
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                    } catch (IOException e) {
                        Slog.w(NetworkHttpMonitor.TAG, "ioe:" + e);
                        NetworkHttpMonitor.this.mHandler.sendMessageDelayed(NetworkHttpMonitor.this.mHandler.obtainMessage(3), JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                    } catch (Throwable th) {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                    }
                }
            }
        }).start();
    }

    public void clearFirewallRule() {
        resetFirewallStatus();
    }

    private void resetFirewallStatus() {
        synchronized (this.mRulesLock) {
            if (mIsFirewallEnabled) {
                Slog.w(TAG, "resetFirewallStatus");
                mIsFirewallEnabled = false;
                mHttpRedirectCount = 0;
                SystemProperties.set(HTTP_FIREWALL_UID, IElsaManager.EMPTY_PACKAGE);
                this.mAlarmManager.cancel(this.mPendingPollIntent);
                this.mHandler.obtainMessage(2).sendToTarget();
            }
        }
    }

    private void registerForAlarms() {
        mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Slog.w(NetworkHttpMonitor.TAG, "onReceive: registerForAlarms");
                NetworkHttpMonitor.this.resetFirewallStatus();
            }
        }, new IntentFilter(ACTION_POLL));
    }

    private void registerForRoutingUpdate() {
        mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Slog.d(NetworkHttpMonitor.TAG, "onReceive: registerForRoutingUpdate");
                NetworkHttpMonitor.this.mAlarmManager.cancel(NetworkHttpMonitor.this.mPendingPollIntent);
                NetworkHttpMonitor.this.resetFirewallStatus();
            }
        }, new IntentFilter(ACTION_ROUTING_UPDATE));
    }

    private void registerForRougingUpdate() {
        mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Slog.w(NetworkHttpMonitor.TAG, "onReceive: registerForRougingUpdate");
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    int event_type = bundle.getInt("eventType");
                    if (event_type == 1 || event_type == 2) {
                        NetworkHttpMonitor.this.mAlarmManager.cancel(NetworkHttpMonitor.this.mPendingPollIntent);
                        NetworkHttpMonitor.this.resetFirewallStatus();
                    }
                }
            }
        }, new IntentFilter("android.intent.action.ACTION_NETWORK_EVENT"));
    }

    private void registerWifiEvent() {
        mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                Slog.w(NetworkHttpMonitor.TAG, "onReceive: CONNECTIVITY_ACTION");
                if (bundle != null) {
                    NetworkInfo info = (NetworkInfo) bundle.get("networkInfo");
                    if (info != null && info.getType() == 1 && info.isConnected()) {
                        NetworkHttpMonitor.this.mAlarmManager.cancel(NetworkHttpMonitor.this.mPendingPollIntent);
                        Slog.w(NetworkHttpMonitor.TAG, "onReceive: resetFirewallStatus");
                        NetworkHttpMonitor.this.resetFirewallStatus();
                    }
                }
            }
        }, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    public boolean isFirewallEnabled() {
        Slog.w(TAG, "isFirewallEnabled:" + mIsFirewallEnabled);
        return mIsFirewallEnabled;
    }

    public String getWebLocation() {
        String web = WEB_LOCATION;
        String testWeb = SystemProperties.get("net.http.web.location", IElsaManager.EMPTY_PACKAGE);
        if (testWeb.length() != 0) {
            web = testWeb;
        }
        Slog.w(TAG, "getWebLocation:" + web);
        return web;
    }

    public void monitorHttpRedirect(String location, int appUid) {
        Slog.w(TAG, "HttpRedirect:" + mHttpRedirectCount + ":" + appUid + "\r\nloc:" + location);
        if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_pre_sim_wo_bal_support", "0"))) {
            Slog.w(TAG, "test 1");
            if (!location.contains(getWebLocation())) {
                return;
            }
            if (mIsFirewallEnabled) {
                Slog.w(TAG, "Http Firewall is enabled");
                return;
            }
            Slog.w(TAG, "Non-app id:" + appUid);
            Slog.w(TAG, "Non-app id:" + appUid);
            if (appUid < 10000) {
                Slog.w(TAG, "Non-app id:" + appUid);
                return;
            }
            ArrayList appList = getBrowserAppList();
            if (!isBrowsrAppByUid(appUid)) {
                mHttpRedirectCount++;
                Slog.w(TAG, "mHttpRedirectCount add");
                Slog.w(TAG, "mHttpRedirectCount add");
                if (mHttpRedirectCount >= 3) {
                    Slog.w(TAG, "Enable firewall");
                    synchronized (this.mRulesLock) {
                        this.mHandler.obtainMessage(1).sendToTarget();
                    }
                }
            } else {
                return;
            }
        }
        Slog.w(TAG, "test 2");
    }

    private void enableFirewallWithUid(int appUid, boolean isEnabled) {
        if (isEnabled) {
            try {
                mNetd.setFirewallUidRule(0, appUid, 1);
            } catch (Exception e) {
                Slog.w(TAG, "e:" + e + "\r\n" + appUid + ":" + isEnabled);
                return;
            }
        }
        mNetd.setFirewallUidRule(0, appUid, 2);
        Slog.w(TAG, "Test:" + appUid + ":" + isEnabled);
    }

    private void enableFirewall() {
        try {
            mNetd.setFirewallEnabled(true);
            mNetd.setFirewallUidRule(0, 0, 1);
            mNetd.setFirewallUidRule(0, 1000, 1);
            mNetd.setFirewallEgressDestRule("0.0.0.0/0", 53, true);
            mNetd.setFirewallEgressProtoRule("icmp", true);
            mNetd.setFirewallInterfaceRule("lo", true);
            mNetd.setFirewallEgressDestRule("0.0.0.0/0", EventLogTags.AM_LOW_MEMORY, true);
            mNetd.setFirewallEgressDestRule("0.0.0.0/0", 5037, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disableFirewall() {
        try {
            mNetd.setFirewallEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Slog.w(TAG, "disableFirewall");
        sendFirewallIntent(false);
        if (this.mHandler.hasMessages(3)) {
            this.mHandler.removeMessages(3);
        }
        Slog.w(TAG, "Keep alive after the disableFirewall");
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
    }

    private void enableFirewallPolicy() {
        StringBuffer sb = new StringBuffer();
        Slog.w(TAG, "enableFirewallPolicy");
        enableFirewall();
        for (int i = 0; i < mBrowserAppUids.size(); i++) {
            if (i != 0) {
                sb.append("," + mBrowserAppUids.get(i));
                Slog.w(TAG, "mBrowserAppUids.get 1");
            } else {
                sb.append(mBrowserAppUids.get(i));
                Slog.w(TAG, "mBrowserAppUids.get 2");
            }
            enableFirewallWithUid(((Integer) mBrowserAppUids.get(i)).intValue(), true);
        }
        sendFirewallIntent(true);
        Slog.w(TAG, "new property:" + sb.toString());
        SystemProperties.set(HTTP_FIREWALL_UID, sb.toString());
        Slog.w(TAG, "start 20 minutes timer");
        mIsFirewallEnabled = true;
        this.mAlarmManager.set(3, SystemClock.elapsedRealtime() + 1200000, this.mPendingPollIntent);
        if (!this.mHandler.hasMessages(3)) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
        }
    }

    private boolean isBrowsrAppByUid(int appUid) {
        for (int i = 0; i < mBrowserAppUids.size(); i++) {
            Slog.w(TAG, "isBrowsrAppByUid");
            if (appUid == ((Integer) mBrowserAppUids.get(i)).intValue()) {
                return true;
            }
        }
        return false;
    }

    private void sendFirewallIntent(boolean isEnabled) {
        Slog.w(TAG, "sendFirewallIntent");
        long ident = Binder.clearCallingIdentity();
        Intent intent = new Intent("com.android.server.net.NetworkHttpMonitor.action.firewall");
        intent.addFlags(536870912);
        intent.putExtra("isEnabled", isEnabled);
        mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private ArrayList getBrowserAppList() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.BROWSABLE");
        intent.setData(Uri.parse("http://www.google.com"));
        Slog.w(TAG, "getBrowserAppList");
        List<ResolveInfo> infos = mPackageManager.queryIntentActivities(intent, 64);
        Slog.w(TAG, "getBrowserAppList:" + infos.size());
        mBrowserAppList.clear();
        mBrowserAppUids.clear();
        for (ResolveInfo info : infos) {
            mBrowserAppList.add(info.activityInfo.packageName);
            mBrowserAppUids.add(new Integer(info.activityInfo.applicationInfo.uid));
        }
        return mBrowserAppList;
    }
}
