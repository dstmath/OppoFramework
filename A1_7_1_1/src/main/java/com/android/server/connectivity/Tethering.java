package com.android.server.connectivity;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkRequest;
import android.net.NetworkState;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.wifi.WifiDevice;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.IoThread;
import com.android.server.connectivity.tethering.IControlsTethering;
import com.android.server.connectivity.tethering.IPv6TetheringCoordinator;
import com.android.server.connectivity.tethering.MdDirectTethering;
import com.android.server.connectivity.tethering.TetherInterfaceStateMachine;
import com.android.server.net.BaseNetworkObserver;
import com.android.server.notification.NotificationManagerService;
import com.android.server.oppo.IElsaManager;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
public class Tethering extends BaseNetworkObserver implements IControlsTethering {
    public static final String ACTION_ENABLE_NSIOT_TESTING = "android.intent.action.ACTION_ENABLE_NSIOT_TESTING";
    private static final String ACTION_STOP_HOTSPOT = "com.android.server.WifiManager.action.STOP_HOTSPOT";
    private static final boolean DBG = false;
    private static final String[] DHCP_DEFAULT_RANGE = null;
    private static final int DNSMASQ_POLLING_INTERVAL = 1000;
    private static final int DNSMASQ_POLLING_MAX_TIMES = 10;
    private static final String DNS_DEFAULT_SERVER1 = "8.8.8.8";
    private static final String DNS_DEFAULT_SERVER2 = "8.8.4.4";
    private static final Integer DUN_TYPE = null;
    public static final String EXTRA_NSIOT_ENABLED = "nsiot_enabled";
    public static final String EXTRA_NSIOT_IP_ADDR = "nsiot_ip_addr";
    private static final Integer HIPRI_TYPE = null;
    private static final long HOTSPOT_DISABLE_MS = 300000;
    private static final boolean IS_USER_BUILD = false;
    private static final Integer MOBILE_TYPE = null;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.log.tag.tel_dbg";
    private static final int STOP_HOTSPOT_REQUEST = 2;
    public static final String SYSTEM_PROPERTY_NSIOT_PENDING = "net.nsiot_pending";
    private static final String TAG = "Tethering";
    private static final ComponentName TETHER_SERVICE = null;
    private static final boolean VDBG = false;
    private static final int WIFI_SAVED_STATE_OPEN = 1;
    private static final String dhcpLocation = "/data/misc/dhcp/dnsmasq.leases";
    private static final Class[] messageClasses = null;
    private static final SparseArray<String> sMagicDecoderRing = null;
    private AlarmManager mAlarmManager;
    private HashMap<String, WifiDevice> mConnectedDeviceMap;
    private final Context mContext;
    private String mCurrentUpstreamIface;
    private String[] mDefaultDnsServers;
    private String[] mDhcpRange;
    private int mDuration;
    private int mHotspotClientNum;
    private PendingIntent mIntentStopHotspot;
    private boolean mIpv6FeatureEnable;
    private boolean mIsTetheringChangeDone;
    private HashMap<String, WifiDevice> mL2ConnectedDeviceMap;
    private int mLastClientNum;
    private int mLastNotificationId;
    private final Looper mLooper;
    MdDirectTethering mMdDirectTethering;
    private final INetworkManagementService mNMService;
    private Object mNotificationSync;
    private final INetworkPolicyManager mPolicyManager;
    private int mPreferredUpstreamMobileApn;
    private final Object mPublicSync;
    private boolean mRndisEnabled;
    private final BroadcastReceiver mStateReceiver;
    private final INetworkStatsService mStatsService;
    private final StateMachine mTetherMasterSM;
    private final ArrayMap<String, TetherState> mTetherStates;
    private String[] mTetherableBluetoothRegexs;
    private String[] mTetherableUsbRegexs;
    private String[] mTetherableWifiRegexs;
    private Builder mTetheredNotificationBuilder;
    private Collection<Integer> mUpstreamIfaceTypes;
    private final UpstreamNetworkMonitor mUpstreamNetworkMonitor;
    private boolean mUsbTetherRequested;
    private WifiManager mWifiManager;
    private boolean mWifiTetherRequested;

    private static class DnsmasqThread extends Thread {
        private WifiDevice mDevice;
        private int mInterval;
        private int mMaxTimes;
        private final Tethering mTethering;

        public DnsmasqThread(Tethering tethering, WifiDevice device, int interval, int maxTimes) {
            super(Tethering.TAG);
            this.mTethering = tethering;
            this.mInterval = interval;
            this.mMaxTimes = maxTimes;
            this.mDevice = device;
        }

        /* JADX WARNING: Removed duplicated region for block: B:35:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x010d  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            WifiDevice other;
            boolean result = false;
            while (this.mMaxTimes > 0) {
                try {
                    result = this.mTethering.readDeviceInfoFromDnsmasq(this.mDevice);
                    if (result) {
                        if (Tethering.DBG) {
                            Log.d(Tethering.TAG, "Successfully poll device info for " + this.mDevice.deviceAddress);
                        }
                        if (!result && Tethering.DBG) {
                            Log.d(Tethering.TAG, "Pulling timeout, suppose STA uses static ip " + this.mDevice.deviceAddress);
                        }
                        other = (WifiDevice) this.mTethering.mL2ConnectedDeviceMap.get(this.mDevice.deviceAddress);
                        if (other == null && other.deviceState == 1) {
                            this.mTethering.mConnectedDeviceMap.put(this.mDevice.deviceAddress, this.mDevice);
                            Log.d(Tethering.TAG, "DnsmasqThread: Some client connect, clientnum = " + this.mTethering.mConnectedDeviceMap.size());
                            if (this.mTethering.mDuration != 0 && this.mTethering.mConnectedDeviceMap.size() >= 1) {
                                this.mTethering.mAlarmManager.cancel(this.mTethering.mIntentStopHotspot);
                            }
                            this.mTethering.sendTetherConnectStateChangedBroadcast();
                            return;
                        } else if (!Tethering.DBG) {
                            Log.d(Tethering.TAG, "Device " + this.mDevice.deviceAddress + "already disconnected, ignoring");
                            return;
                        } else {
                            return;
                        }
                    }
                    this.mMaxTimes--;
                    Thread.sleep((long) this.mInterval);
                } catch (Exception ex) {
                    result = false;
                    Log.e(Tethering.TAG, "Pulling " + this.mDevice.deviceAddress + "error" + ex);
                }
            }
            Log.d(Tethering.TAG, "Pulling timeout, suppose STA uses static ip " + this.mDevice.deviceAddress);
            other = (WifiDevice) this.mTethering.mL2ConnectedDeviceMap.get(this.mDevice.deviceAddress);
            if (other == null) {
            }
            if (!Tethering.DBG) {
            }
        }
    }

    private class StateReceiver extends BroadcastReceiver {
        /* synthetic */ StateReceiver(Tethering this$0, StateReceiver stateReceiver) {
            this();
        }

        private StateReceiver() {
        }

        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            Log.i(Tethering.TAG, "StateReceiver onReceive action:" + action);
            if (action != null) {
                Object -get14;
                if (action.equals("android.hardware.usb.action.USB_STATE")) {
                    -get14 = Tethering.this.mPublicSync;
                    synchronized (-get14) {
                        boolean usbConnected = intent.getBooleanExtra("connected", false);
                        Tethering.this.mRndisEnabled = intent.getBooleanExtra("rndis", false);
                        if (usbConnected && Tethering.this.mRndisEnabled && Tethering.this.mUsbTetherRequested) {
                            Log.i(Tethering.TAG, "StateReceiver onReceive action synchronized: usbConnected && mRndisEnabled && mUsbTetherRequested, tetherUsb!! ");
                            Tethering.this.tetherMatchingInterfaces(true, 1);
                        }
                        Tethering.this.mUsbTetherRequested = false;
                    }
                } else {
                    if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                        NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                        if (Tethering.VDBG) {
                            Log.i(Tethering.TAG, "Tethering got CONNECTIVITY_ACTION, networkInfo:" + networkInfo);
                        }
                        if (!(networkInfo == null || networkInfo.getDetailedState() == DetailedState.FAILED || Tethering.this.mTetherMasterSM.getHandler().hasMessages(327683))) {
                            Tethering.this.mTetherMasterSM.sendMessage(327683);
                        }
                    } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                        -get14 = Tethering.this.mPublicSync;
                        synchronized (-get14) {
                            int curState = intent.getIntExtra("wifi_state", 11);
                            if (Tethering.VDBG) {
                                Log.d(Tethering.TAG, "curState:" + curState);
                            }
                            if (curState == 13 || curState == 11) {
                                Tethering.this.mConnectedDeviceMap.clear();
                                Tethering.this.mL2ConnectedDeviceMap.clear();
                            }
                            switch (curState) {
                                case 11:
                                    if (Tethering.this.mDuration != 0) {
                                        Log.d(Tethering.TAG, "Cancel alarm for exit TetheredState, mDuration:" + Tethering.this.mDuration);
                                        Tethering.this.mAlarmManager.cancel(Tethering.this.mIntentStopHotspot);
                                        break;
                                    }
                                    break;
                                case 12:
                                    break;
                                case 13:
                                    Tethering.this.tetherMatchingInterfaces(true, 0);
                                    if (Tethering.this.mDuration != 0) {
                                        Tethering.this.mAlarmManager.cancel(Tethering.this.mIntentStopHotspot);
                                        Log.d(Tethering.TAG, "Set alarm for enter TetheredState, mDuration:" + Tethering.this.mDuration);
                                        Tethering.this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + (((long) Tethering.this.mDuration) * 300000), Tethering.this.mIntentStopHotspot);
                                        break;
                                    }
                                    break;
                            }
                            if (Tethering.DBG) {
                                Log.d(Tethering.TAG, "Canceling WiFi tethering request - AP_STATE=" + curState);
                            }
                            int i = 0;
                            while (i < Tethering.this.mTetherStates.size()) {
                                TetherInterfaceStateMachine tism = ((TetherState) Tethering.this.mTetherStates.valueAt(i)).mStateMachine;
                                if (tism.interfaceType() == 0) {
                                    tism.sendMessage(TetherInterfaceStateMachine.CMD_TETHER_UNREQUESTED);
                                    Tethering.this.mWifiTetherRequested = false;
                                } else {
                                    i++;
                                }
                            }
                            Tethering.this.mWifiTetherRequested = false;
                        }
                    } else if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                        Tethering.this.updateConfiguration();
                    } else if (action.equals(Tethering.ACTION_ENABLE_NSIOT_TESTING)) {
                        boolean enabled = intent.getBooleanExtra(Tethering.EXTRA_NSIOT_ENABLED, false);
                        String ipAddr = intent.getStringExtra(Tethering.EXTRA_NSIOT_IP_ADDR);
                        Log.e(Tethering.TAG, "[NS-IOT]Receieve ACTION_ENABLE_NSIOT_TESTING:nsiot_enabled = " + enabled + "," + Tethering.EXTRA_NSIOT_IP_ADDR + " = " + ipAddr);
                        SystemProperties.set(Tethering.SYSTEM_PROPERTY_NSIOT_PENDING, "true");
                        Tethering.this.enableUdpForwardingForUsb(enabled, ipAddr);
                    }
                }
            }
        }
    }

    class TetherMasterSM extends StateMachine {
        private static final int BASE_MASTER = 327680;
        static final int CMD_CLEAR_ERROR = 327686;
        static final int CMD_RETRY_UPSTREAM = 327684;
        static final int CMD_TETHER_MODE_REQUESTED = 327681;
        static final int CMD_TETHER_MODE_UNREQUESTED = 327682;
        static final int CMD_UPSTREAM_CHANGED = 327683;
        static final int EVENT_UPSTREAM_CALLBACK = 327685;
        private static final int UPSTREAM_SETTLE_TIME_MS = 10000;
        private SimChangeBroadcastReceiver mBroadcastReceiver = null;
        private final IPv6TetheringCoordinator mIPv6TetheringCoordinator;
        private State mInitialState;
        private int mMobileApnReserved = -1;
        private NetworkCallback mMobileUpstreamCallback;
        private String mName;
        private final ArrayList<TetherInterfaceStateMachine> mNotifyList;
        private State mSetDnsForwardersErrorState;
        private State mSetIpForwardingDisabledErrorState;
        private State mSetIpForwardingEnabledErrorState;
        private final AtomicInteger mSimBcastGenerationNumber = new AtomicInteger(0);
        private State mStartTetheringErrorState;
        private State mStopTetheringErrorState;
        private State mTetherModeAliveState;

        class ErrorState extends State {
            int mErrorNotification;

            ErrorState() {
            }

            public boolean processMessage(Message message) {
                Log.i(Tethering.TAG, "[MSM_Error][" + TetherMasterSM.this.mName + "] processMessage what=" + message.what);
                switch (message.what) {
                    case TetherMasterSM.CMD_TETHER_MODE_REQUESTED /*327681*/:
                        message.obj.sendMessage(this.mErrorNotification);
                        return true;
                    case TetherMasterSM.CMD_CLEAR_ERROR /*327686*/:
                        this.mErrorNotification = 0;
                        TetherMasterSM.this.transitionTo(TetherMasterSM.this.mInitialState);
                        return true;
                    default:
                        return false;
                }
            }

            void notify(int msgType) {
                this.mErrorNotification = msgType;
                for (TetherInterfaceStateMachine sm : TetherMasterSM.this.mNotifyList) {
                    sm.sendMessage(msgType);
                }
            }
        }

        class TetherMasterUtilState extends State {
            TetherMasterUtilState() {
            }

            public boolean processMessage(Message m) {
                return false;
            }

            protected boolean turnOnUpstreamMobileConnection(int apnType) {
                if (apnType == -1) {
                    return false;
                }
                if (apnType != TetherMasterSM.this.mMobileApnReserved) {
                    turnOffUpstreamMobileConnection();
                }
                if (TetherMasterSM.this.mMobileUpstreamCallback != null) {
                    return true;
                }
                switch (apnType) {
                    case 0:
                    case 4:
                    case 5:
                        if (SystemProperties.getBoolean("persist.op12.ccp.mode", false)) {
                            Log.i(Tethering.TAG, "isCcpMode enabled, don't enable mobile");
                            return false;
                        }
                        TetherMasterSM.this.mMobileApnReserved = apnType;
                        Log.i(Tethering.TAG, "[MSM_TetherModeAlive][" + TetherMasterSM.this.mName + "] mMobileApnReserved:" + TetherMasterSM.this.mMobileApnReserved);
                        NetworkRequest.Builder builder = new NetworkRequest.Builder().addTransportType(0);
                        if (apnType == 4) {
                            builder.removeCapability(13).addCapability(2);
                        } else {
                            builder.addCapability(12);
                        }
                        NetworkRequest mobileUpstreamRequest = builder.build();
                        TetherMasterSM.this.mMobileUpstreamCallback = new NetworkCallback();
                        if (Tethering.DBG) {
                            Log.d(Tethering.TAG, "requesting mobile upstream network: " + mobileUpstreamRequest);
                        }
                        Tethering.this.getConnectivityManager().requestNetwork(mobileUpstreamRequest, TetherMasterSM.this.mMobileUpstreamCallback, 0, apnType);
                        return true;
                    default:
                        return false;
                }
            }

            protected void turnOffUpstreamMobileConnection() {
                if (TetherMasterSM.this.mMobileUpstreamCallback != null) {
                    Tethering.this.getConnectivityManager().unregisterNetworkCallback(TetherMasterSM.this.mMobileUpstreamCallback);
                    TetherMasterSM.this.mMobileUpstreamCallback = null;
                }
                TetherMasterSM.this.mMobileApnReserved = -1;
            }

            protected boolean turnOnMasterTetherSettings() {
                try {
                    Tethering.this.mNMService.setIpForwardingEnabled(true);
                    try {
                        Tethering.this.mNMService.startTethering(Tethering.this.mDhcpRange);
                    } catch (Exception e) {
                        try {
                            Tethering.this.mNMService.stopTethering();
                            Tethering.this.mNMService.startTethering(Tethering.this.mDhcpRange);
                        } catch (Exception e2) {
                            TetherMasterSM.this.transitionTo(TetherMasterSM.this.mStartTetheringErrorState);
                            return false;
                        }
                    }
                    return true;
                } catch (Exception e3) {
                    TetherMasterSM.this.transitionTo(TetherMasterSM.this.mSetIpForwardingEnabledErrorState);
                    return false;
                }
            }

            protected boolean turnOffMasterTetherSettings() {
                try {
                    Tethering.this.mNMService.stopTethering();
                    try {
                        Tethering.this.mNMService.setIpForwardingEnabled(false);
                        TetherMasterSM.this.transitionTo(TetherMasterSM.this.mInitialState);
                        return true;
                    } catch (Exception e) {
                        TetherMasterSM.this.transitionTo(TetherMasterSM.this.mSetIpForwardingDisabledErrorState);
                        return false;
                    }
                } catch (Exception e2) {
                    TetherMasterSM.this.transitionTo(TetherMasterSM.this.mStopTetheringErrorState);
                    return false;
                }
            }

            private boolean checkDataEnabled(int networkType) {
                boolean dataEnabled = TelephonyManager.getDefault().getDataEnabled();
                Log.i(Tethering.TAG, "checkDataEnabled:" + dataEnabled);
                return dataEnabled;
            }

            protected void chooseUpstreamType(boolean tryCell) {
                ConnectivityManager cm = Tethering.this.getConnectivityManager();
                int upType = -1;
                String iface = null;
                int radioNetworkType = 0;
                Tethering.this.updateConfiguration();
                synchronized (Tethering.this.mPublicSync) {
                    if (Tethering.VDBG) {
                        String result = IElsaManager.EMPTY_PACKAGE;
                        Iterator<Integer> iterator = Tethering.this.mUpstreamIfaceTypes.iterator();
                        for (int i = 0; i < Tethering.this.mUpstreamIfaceTypes.size(); i++) {
                            result = result + " " + iterator.next();
                        }
                        Log.d(Tethering.TAG, "[" + TetherMasterSM.this.mName + "]chooseUpstreamType has upstream iface types: " + result);
                    }
                    for (Integer netType : Tethering.this.mUpstreamIfaceTypes) {
                        NetworkInfo info = cm.getNetworkInfo(netType.intValue());
                        if (info != null && (info.isConnected() || info.getState() == NetworkInfo.State.SUSPENDED)) {
                            upType = netType.intValue();
                            if (Tethering.this.mMdDirectTethering != null && Tethering.this.mMdDirectTethering.isMobileUpstream(upType)) {
                                TelephonyManager tm = TelephonyManager.getDefault();
                                int subId = SubscriptionManager.getDefaultDataSubscriptionId();
                                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                                    radioNetworkType = tm.getDataNetworkType(subId);
                                }
                                if (Tethering.DBG) {
                                    Log.d(Tethering.TAG, "radioNetworkType:" + radioNetworkType);
                                }
                            }
                        }
                    }
                }
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "[" + TetherMasterSM.this.mName + "]chooseUpstreamType(" + tryCell + ")," + " preferredApn=" + ConnectivityManager.getNetworkTypeName(Tethering.this.mPreferredUpstreamMobileApn) + ", got type=" + ConnectivityManager.getNetworkTypeName(upType));
                }
                Log.d(Tethering.TAG, "pre-checkDataEnabled + " + checkDataEnabled(upType));
                switch (upType) {
                    case -1:
                        if (!(tryCell && turnOnUpstreamMobileConnection(Tethering.this.mPreferredUpstreamMobileApn))) {
                            TetherMasterSM.this.sendMessageDelayed(TetherMasterSM.CMD_RETRY_UPSTREAM, 10000);
                            break;
                        }
                    case 4:
                    case 5:
                        turnOnUpstreamMobileConnection(upType);
                        break;
                    default:
                        turnOffUpstreamMobileConnection();
                        break;
                }
                Network network = null;
                if (upType != -1) {
                    LinkProperties linkProperties = cm.getLinkProperties(upType);
                    if (linkProperties != null) {
                        Log.i(Tethering.TAG, "Finding IPv4 upstream interface on: " + linkProperties);
                        RouteInfo ipv4Default = RouteInfo.selectBestRoute(linkProperties.getAllRoutes(), Inet4Address.ANY);
                        if (ipv4Default != null) {
                            iface = ipv4Default.getInterface();
                            Log.i(Tethering.TAG, "Found interface " + ipv4Default.getInterface());
                        } else if (ConnectivityManager.isNetworkTypeMobile(upType) && linkProperties.hasIPv6DefaultRoute()) {
                            iface = linkProperties.getInterfaceName();
                            Log.i(Tethering.TAG, "Found v6 upstream: " + iface);
                        } else {
                            Log.i(Tethering.TAG, "No IPv4 upstream interface, giving up.");
                        }
                    }
                    if (iface != null) {
                        network = cm.getNetworkForType(upType);
                        if (network == null) {
                            Log.e(Tethering.TAG, "No Network for upstream type " + upType + "!");
                        }
                        setDnsForwarders(network, linkProperties);
                    }
                }
                String dIface = Tethering.this.getDownstreamIface();
                if (dIface != null && Tethering.this.mMdDirectTethering != null && Tethering.this.mMdDirectTethering.isMdtEnable() && Tethering.this.isUsb(dIface)) {
                    Tethering.this.mMdDirectTethering.addBridgeInterface(dIface, Tethering.this.mMdDirectTethering.shouldUseMdt(radioNetworkType));
                }
                notifyTetheredOfNewUpstreamIface(iface);
                NetworkState ns = Tethering.this.mUpstreamNetworkMonitor.lookup(network);
                if (ns != null && Tethering.this.pertainsToCurrentUpstream(ns)) {
                    handleNewUpstreamNetworkState(ns);
                } else if (Tethering.this.mCurrentUpstreamIface == null) {
                    handleNewUpstreamNetworkState(null);
                }
            }

            protected void setDnsForwarders(Network network, LinkProperties lp) {
                String[] dnsServers = Tethering.this.mDefaultDnsServers;
                Collection<InetAddress> dnses = lp.getDnsServers();
                if (!(dnses == null || dnses.isEmpty())) {
                    Collection<InetAddress> sortedDnses = new ArrayList();
                    for (InetAddress ia : dnses) {
                        if (ia instanceof Inet6Address) {
                            sortedDnses.add(ia);
                        }
                    }
                    for (InetAddress ia2 : dnses) {
                        if (ia2 instanceof Inet4Address) {
                            sortedDnses.add(ia2);
                        }
                    }
                    dnses = sortedDnses;
                    dnsServers = NetworkUtils.makeStrings(sortedDnses);
                }
                if (Tethering.VDBG) {
                    Log.d(Tethering.TAG, "Setting DNS forwarders: Network=" + network + ", dnsServers=" + Arrays.toString(dnsServers));
                }
                try {
                    Tethering.this.mNMService.setDnsForwarders(network, dnsServers);
                } catch (Exception e) {
                    Log.e(Tethering.TAG, "Setting DNS forwarders failed!");
                    TetherMasterSM.this.transitionTo(TetherMasterSM.this.mSetDnsForwardersErrorState);
                }
            }

            protected void notifyTetheredOfNewUpstreamIface(String ifaceName) {
                if (Tethering.DBG) {
                    Log.i(Tethering.TAG, "[MSM_TetherModeAlive][" + TetherMasterSM.this.mName + "] Notifying tethered with upstream =" + ifaceName);
                }
                Tethering.this.mCurrentUpstreamIface = ifaceName;
                for (TetherInterfaceStateMachine sm : TetherMasterSM.this.mNotifyList) {
                    sm.sendMessage(TetherInterfaceStateMachine.CMD_TETHER_CONNECTION_CHANGED, ifaceName);
                }
                if (ifaceName != null && SystemProperties.getBoolean(Tethering.SYSTEM_PROPERTY_NSIOT_PENDING, false)) {
                    Tethering.this.enableUdpForwardingForUsb(true, null);
                }
            }

            protected void handleNewUpstreamNetworkState(NetworkState ns) {
                TetherMasterSM.this.mIPv6TetheringCoordinator.updateUpstreamNetworkState(ns);
            }
        }

        class InitialState extends TetherMasterUtilState {
            InitialState() {
                super();
            }

            public void enter() {
                Log.i(Tethering.TAG, "[MSM_Initial][" + TetherMasterSM.this.mName + "] enter");
            }

            public boolean processMessage(Message message) {
                Tethering.this.maybeLogMessage(this, message.what);
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "[MSM_Initial][" + TetherMasterSM.this.mName + "] processMessage what=" + message.what);
                }
                TetherInterfaceStateMachine who;
                switch (message.what) {
                    case TetherMasterSM.CMD_TETHER_MODE_REQUESTED /*327681*/:
                        who = message.obj;
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "[MSM_Initial][" + TetherMasterSM.this.mName + "] Tether Mode requested by " + who);
                        }
                        if (TetherMasterSM.this.mNotifyList.indexOf(who) < 0) {
                            TetherMasterSM.this.mNotifyList.add(who);
                            TetherMasterSM.this.mIPv6TetheringCoordinator.addActiveDownstream(who);
                        }
                        TetherMasterSM.this.transitionTo(TetherMasterSM.this.mTetherModeAliveState);
                        return true;
                    case TetherMasterSM.CMD_TETHER_MODE_UNREQUESTED /*327682*/:
                        who = (TetherInterfaceStateMachine) message.obj;
                        Log.d(Tethering.TAG, "[MSM_Initial] CMD_TETHER_MODE_UNREQUESTED ===========>");
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "[MSM_Initial][" + TetherMasterSM.this.mName + "]Tether Mode unrequested by " + who);
                        }
                        TetherMasterSM.this.mNotifyList.remove(who);
                        TetherMasterSM.this.mIPv6TetheringCoordinator.removeActiveDownstream(who);
                        Log.i(Tethering.TAG, "[MSM_Initial] CMD_TETHER_MODE_UNREQUESTED <===========");
                        return true;
                    default:
                        return false;
                }
            }
        }

        class SetDnsForwardersErrorState extends ErrorState {
            SetDnsForwardersErrorState() {
                super();
            }

            public void enter() {
                Log.e(Tethering.TAG, "[MSM_Error][" + TetherMasterSM.this.mName + "] setDnsForwarders");
                notify(TetherInterfaceStateMachine.CMD_SET_DNS_FORWARDERS_ERROR);
                try {
                    Tethering.this.mNMService.stopTethering();
                } catch (Exception e) {
                }
                try {
                    Tethering.this.mNMService.setIpForwardingEnabled(false);
                } catch (Exception e2) {
                }
            }
        }

        class SetIpForwardingDisabledErrorState extends ErrorState {
            SetIpForwardingDisabledErrorState() {
                super();
            }

            public void enter() {
                Log.e(Tethering.TAG, "[MSM_Error][" + TetherMasterSM.this.mName + "] setIpForwardingDisabled");
                notify(TetherInterfaceStateMachine.CMD_IP_FORWARDING_DISABLE_ERROR);
            }
        }

        class SetIpForwardingEnabledErrorState extends ErrorState {
            SetIpForwardingEnabledErrorState() {
                super();
            }

            public void enter() {
                Log.e(Tethering.TAG, "[MSM_Error][" + TetherMasterSM.this.mName + "] setIpForwardingEnabled");
                notify(TetherInterfaceStateMachine.CMD_IP_FORWARDING_ENABLE_ERROR);
            }
        }

        class SimChangeBroadcastReceiver extends BroadcastReceiver {
            private final int mGenerationNumber;
            private boolean mSimNotLoadedSeen = false;

            public SimChangeBroadcastReceiver(int generationNumber) {
                this.mGenerationNumber = generationNumber;
            }

            public void onReceive(Context context, Intent intent) {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "simchange mGenerationNumber=" + this.mGenerationNumber + ", current generationNumber=" + TetherMasterSM.this.mSimBcastGenerationNumber.get());
                }
                if (this.mGenerationNumber == TetherMasterSM.this.mSimBcastGenerationNumber.get()) {
                    String state = intent.getStringExtra("ss");
                    Log.d(Tethering.TAG, "got Sim changed to state " + state + ", mSimNotLoadedSeen=" + this.mSimNotLoadedSeen);
                    if (!(this.mSimNotLoadedSeen || "LOADED".equals(state))) {
                        this.mSimNotLoadedSeen = true;
                    }
                    if (this.mSimNotLoadedSeen && "LOADED".equals(state)) {
                        this.mSimNotLoadedSeen = false;
                        try {
                            if (Tethering.this.mContext.getResources().getString(17039413).isEmpty()) {
                                Log.d(Tethering.TAG, "no prov-check needed for new SIM");
                            } else {
                                ArrayList<Integer> tethered = new ArrayList();
                                synchronized (Tethering.this.mPublicSync) {
                                    for (int i = 0; i < Tethering.this.mTetherStates.size(); i++) {
                                        if (((TetherState) Tethering.this.mTetherStates.valueAt(i)).mLastState == 2) {
                                            int interfaceType = Tethering.this.ifaceNameToType((String) Tethering.this.mTetherStates.keyAt(i));
                                            if (interfaceType != -1) {
                                                tethered.add(new Integer(interfaceType));
                                            }
                                        }
                                    }
                                }
                                for (Integer intValue : tethered) {
                                    int tetherType = intValue.intValue();
                                    Intent startProvIntent = new Intent();
                                    startProvIntent.putExtra("extraAddTetherType", tetherType);
                                    startProvIntent.putExtra("extraRunProvision", true);
                                    startProvIntent.setComponent(Tethering.TETHER_SERVICE);
                                    Tethering.this.mContext.startServiceAsUser(startProvIntent, UserHandle.CURRENT);
                                }
                                Log.d(Tethering.TAG, "re-evaluate provisioning");
                            }
                        } catch (NotFoundException e) {
                            Log.d(Tethering.TAG, "no prov-check needed for new SIM");
                        }
                    }
                }
            }
        }

        class StartTetheringErrorState extends ErrorState {
            StartTetheringErrorState() {
                super();
            }

            public void enter() {
                Log.e(Tethering.TAG, "[MSM_Error][" + TetherMasterSM.this.mName + "] startTethering");
                notify(TetherInterfaceStateMachine.CMD_START_TETHERING_ERROR);
                try {
                    Tethering.this.mNMService.setIpForwardingEnabled(false);
                } catch (Exception e) {
                }
            }
        }

        class StopTetheringErrorState extends ErrorState {
            StopTetheringErrorState() {
                super();
            }

            public void enter() {
                Log.e(Tethering.TAG, "[MSM_Error][" + TetherMasterSM.this.mName + "] stopTethering");
                notify(TetherInterfaceStateMachine.CMD_STOP_TETHERING_ERROR);
                try {
                    Tethering.this.mNMService.setIpForwardingEnabled(false);
                } catch (Exception e) {
                }
            }
        }

        class TetherModeAliveState extends TetherMasterUtilState {
            boolean mTryCell = true;

            TetherModeAliveState() {
                super();
            }

            public void enter() {
                boolean z = true;
                Log.i(Tethering.TAG, "[MSM_TetherModeAlive][" + TetherMasterSM.this.mName + "] enter");
                turnOnMasterTetherSettings();
                TetherMasterSM.this.startListeningForSimChanges();
                Tethering.this.mUpstreamNetworkMonitor.start();
                this.mTryCell = true;
                chooseUpstreamType(this.mTryCell);
                if (this.mTryCell) {
                    z = false;
                }
                this.mTryCell = z;
            }

            public void exit() {
                Log.i(Tethering.TAG, "[MSM_TetherModeAlive][" + TetherMasterSM.this.mName + "] exit");
                turnOffUpstreamMobileConnection();
                Tethering.this.mUpstreamNetworkMonitor.stop();
                TetherMasterSM.this.stopListeningForSimChanges();
                notifyTetheredOfNewUpstreamIface(null);
                handleNewUpstreamNetworkState(null);
            }

            public boolean processMessage(Message message) {
                boolean z = false;
                Tethering.this.maybeLogMessage(this, message.what);
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "[MSM_TetherModeAlive][" + TetherMasterSM.this.mName + "] processMessage what=" + message.what);
                }
                TetherInterfaceStateMachine who;
                switch (message.what) {
                    case TetherMasterSM.CMD_TETHER_MODE_REQUESTED /*327681*/:
                        who = message.obj;
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "Tether Mode requested by " + who);
                        }
                        if (TetherMasterSM.this.mNotifyList.indexOf(who) < 0) {
                            TetherMasterSM.this.mNotifyList.add(who);
                            TetherMasterSM.this.mIPv6TetheringCoordinator.addActiveDownstream(who);
                        }
                        who.sendMessage(TetherInterfaceStateMachine.CMD_TETHER_CONNECTION_CHANGED, Tethering.this.mCurrentUpstreamIface);
                        return true;
                    case TetherMasterSM.CMD_TETHER_MODE_UNREQUESTED /*327682*/:
                        who = (TetherInterfaceStateMachine) message.obj;
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "Tether Mode unrequested by " + who);
                        }
                        if (TetherMasterSM.this.mNotifyList.remove(who)) {
                            if (Tethering.DBG) {
                                Log.d(Tethering.TAG, "TetherModeAlive removing notifyee " + who);
                            }
                            if (TetherMasterSM.this.mNotifyList.isEmpty()) {
                                Log.i(Tethering.TAG, "[MSM_TetherModeAlive] CMD_TETHER_MODE_UNREQUESTED is empty");
                                turnOffMasterTetherSettings();
                            } else if (Tethering.DBG) {
                                Log.d(Tethering.TAG, "TetherModeAlive still has " + TetherMasterSM.this.mNotifyList.size() + " live requests:");
                                for (TetherInterfaceStateMachine o : TetherMasterSM.this.mNotifyList) {
                                    Log.d(Tethering.TAG, "  " + o);
                                }
                            }
                        } else {
                            Log.e(Tethering.TAG, "TetherModeAliveState UNREQUESTED has unknown who: " + who);
                        }
                        TetherMasterSM.this.mIPv6TetheringCoordinator.removeActiveDownstream(who);
                        Log.i(Tethering.TAG, "[MSM_TetherModeAlive] CMD_TETHER_MODE_UNREQUESTED <==========");
                        return true;
                    case TetherMasterSM.CMD_UPSTREAM_CHANGED /*327683*/:
                        this.mTryCell = true;
                        chooseUpstreamType(this.mTryCell);
                        if (!this.mTryCell) {
                            z = true;
                        }
                        this.mTryCell = z;
                        return true;
                    case TetherMasterSM.CMD_RETRY_UPSTREAM /*327684*/:
                        chooseUpstreamType(this.mTryCell);
                        if (!this.mTryCell) {
                            z = true;
                        }
                        this.mTryCell = z;
                        return true;
                    case TetherMasterSM.EVENT_UPSTREAM_CALLBACK /*327685*/:
                        NetworkState ns = Tethering.this.mUpstreamNetworkMonitor.processCallback(message.arg1, message.obj);
                        if (ns != null && Tethering.this.pertainsToCurrentUpstream(ns)) {
                            switch (message.arg1) {
                                case 2:
                                    handleNewUpstreamNetworkState(ns);
                                    return true;
                                case 3:
                                    setDnsForwarders(ns.network, ns.linkProperties);
                                    handleNewUpstreamNetworkState(ns);
                                    return true;
                                case 4:
                                    handleNewUpstreamNetworkState(null);
                                    return true;
                                default:
                                    return true;
                            }
                        } else if (Tethering.this.mCurrentUpstreamIface != null) {
                            return true;
                        } else {
                            chooseUpstreamType(false);
                            return true;
                        }
                    default:
                        return false;
                }
            }
        }

        TetherMasterSM(String name, Looper looper) {
            super(name, looper);
            this.mName = name;
            this.mInitialState = new InitialState();
            addState(this.mInitialState);
            this.mTetherModeAliveState = new TetherModeAliveState();
            addState(this.mTetherModeAliveState);
            this.mSetIpForwardingEnabledErrorState = new SetIpForwardingEnabledErrorState();
            addState(this.mSetIpForwardingEnabledErrorState);
            this.mSetIpForwardingDisabledErrorState = new SetIpForwardingDisabledErrorState();
            addState(this.mSetIpForwardingDisabledErrorState);
            this.mStartTetheringErrorState = new StartTetheringErrorState();
            addState(this.mStartTetheringErrorState);
            this.mStopTetheringErrorState = new StopTetheringErrorState();
            addState(this.mStopTetheringErrorState);
            this.mSetDnsForwardersErrorState = new SetDnsForwardersErrorState();
            addState(this.mSetDnsForwardersErrorState);
            this.mNotifyList = new ArrayList();
            this.mIPv6TetheringCoordinator = new IPv6TetheringCoordinator(this.mNotifyList);
            setInitialState(this.mInitialState);
        }

        private void startListeningForSimChanges() {
            if (Tethering.DBG) {
                Log.d(Tethering.TAG, "startListeningForSimChanges");
            }
            if (this.mBroadcastReceiver == null) {
                this.mBroadcastReceiver = new SimChangeBroadcastReceiver(this.mSimBcastGenerationNumber.incrementAndGet());
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.SIM_STATE_CHANGED");
                Tethering.this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
            }
        }

        private void stopListeningForSimChanges() {
            if (Tethering.DBG) {
                Log.d(Tethering.TAG, "stopListeningForSimChanges");
            }
            if (this.mBroadcastReceiver != null) {
                this.mSimBcastGenerationNumber.incrementAndGet();
                Tethering.this.mContext.unregisterReceiver(this.mBroadcastReceiver);
                this.mBroadcastReceiver = null;
            }
        }
    }

    private static class TetherState {
        public int mLastError = 0;
        public int mLastState = 1;
        public final TetherInterfaceStateMachine mStateMachine;

        public TetherState(TetherInterfaceStateMachine sm) {
            this.mStateMachine = sm;
        }
    }

    class UpstreamNetworkCallback extends NetworkCallback {
        UpstreamNetworkCallback() {
        }

        public void onAvailable(Network network) {
            Tethering.this.mTetherMasterSM.sendMessage(327685, 1, 0, network);
        }

        public void onCapabilitiesChanged(Network network, NetworkCapabilities newNc) {
            Tethering.this.mTetherMasterSM.sendMessage(327685, 2, 0, new NetworkState(null, null, newNc, network, null, null));
        }

        public void onLinkPropertiesChanged(Network network, LinkProperties newLp) {
            if (Tethering.VDBG) {
                Log.i(Tethering.TAG, "onLinkPropertiesChanged LinkProperties:" + newLp);
            }
            Tethering.this.mTetherMasterSM.sendMessage(327685, 3, 0, new NetworkState(null, newLp, null, network, null, null));
        }

        public void onLost(Network network) {
            Tethering.this.mTetherMasterSM.sendMessage(327685, 4, 0, network);
        }
    }

    class UpstreamNetworkMonitor {
        static final int EVENT_ON_AVAILABLE = 1;
        static final int EVENT_ON_CAPABILITIES = 2;
        static final int EVENT_ON_LINKPROPERTIES = 3;
        static final int EVENT_ON_LOST = 4;
        NetworkCallback mDefaultNetworkCallback;
        NetworkCallback mDunTetheringCallback;
        final HashMap<Network, NetworkState> mNetworkMap = new HashMap();

        UpstreamNetworkMonitor() {
        }

        void start() {
            stop();
            this.mDefaultNetworkCallback = new UpstreamNetworkCallback();
            Tethering.this.getConnectivityManager().registerDefaultNetworkCallback(this.mDefaultNetworkCallback);
            NetworkRequest dunTetheringRequest = new NetworkRequest.Builder().addTransportType(0).removeCapability(13).addCapability(2).build();
            this.mDunTetheringCallback = new UpstreamNetworkCallback();
            Tethering.this.getConnectivityManager().registerNetworkCallback(dunTetheringRequest, this.mDunTetheringCallback);
        }

        void stop() {
            if (this.mDefaultNetworkCallback != null) {
                Tethering.this.getConnectivityManager().unregisterNetworkCallback(this.mDefaultNetworkCallback);
                this.mDefaultNetworkCallback = null;
            }
            if (this.mDunTetheringCallback != null) {
                Tethering.this.getConnectivityManager().unregisterNetworkCallback(this.mDunTetheringCallback);
                this.mDunTetheringCallback = null;
            }
            this.mNetworkMap.clear();
        }

        NetworkState lookup(Network network) {
            return network != null ? (NetworkState) this.mNetworkMap.get(network) : null;
        }

        NetworkState processCallback(int arg1, Object obj) {
            Network network;
            NetworkState ns;
            String str;
            Object[] objArr;
            switch (arg1) {
                case 1:
                    network = (Network) obj;
                    if (Tethering.VDBG) {
                        Log.d(Tethering.TAG, "EVENT_ON_AVAILABLE for " + network);
                    }
                    if (!this.mNetworkMap.containsKey(network)) {
                        this.mNetworkMap.put(network, new NetworkState(null, null, null, network, null, null));
                    }
                    ConnectivityManager cm = Tethering.this.getConnectivityManager();
                    if (this.mDefaultNetworkCallback != null) {
                        cm.requestNetworkCapabilities(this.mDefaultNetworkCallback);
                        cm.requestLinkProperties(this.mDefaultNetworkCallback);
                    }
                    return (NetworkState) this.mNetworkMap.get(network);
                case 2:
                    ns = (NetworkState) obj;
                    if (!this.mNetworkMap.containsKey(ns.network)) {
                        return null;
                    }
                    if (Tethering.VDBG) {
                        str = Tethering.TAG;
                        objArr = new Object[2];
                        objArr[0] = ns.network;
                        objArr[1] = ns.networkCapabilities;
                        Log.d(str, String.format("EVENT_ON_CAPABILITIES for %s: %s", objArr));
                    }
                    this.mNetworkMap.put(ns.network, new NetworkState(null, ((NetworkState) this.mNetworkMap.get(ns.network)).linkProperties, ns.networkCapabilities, ns.network, null, null));
                    return (NetworkState) this.mNetworkMap.get(ns.network);
                case 3:
                    ns = (NetworkState) obj;
                    if (!this.mNetworkMap.containsKey(ns.network)) {
                        return null;
                    }
                    if (Tethering.VDBG) {
                        str = Tethering.TAG;
                        objArr = new Object[2];
                        objArr[0] = ns.network;
                        objArr[1] = ns.linkProperties;
                        Log.d(str, String.format("EVENT_ON_LINKPROPERTIES for %s: %s", objArr));
                    }
                    this.mNetworkMap.put(ns.network, new NetworkState(null, ns.linkProperties, ((NetworkState) this.mNetworkMap.get(ns.network)).networkCapabilities, ns.network, null, null));
                    return (NetworkState) this.mNetworkMap.get(ns.network);
                case 4:
                    network = (Network) obj;
                    if (Tethering.VDBG) {
                        Log.d(Tethering.TAG, "EVENT_ON_LOST for " + network);
                    }
                    return (NetworkState) this.mNetworkMap.remove(network);
                default:
                    return null;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.connectivity.Tethering.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.connectivity.Tethering.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.Tethering.<clinit>():void");
    }

    public Tethering(Context context, INetworkManagementService nmService, INetworkStatsService statsService, INetworkPolicyManager policyManager) {
        this.mPreferredUpstreamMobileApn = -1;
        this.mIsTetheringChangeDone = false;
        this.mL2ConnectedDeviceMap = new HashMap();
        this.mConnectedDeviceMap = new HashMap();
        this.mIpv6FeatureEnable = false;
        this.mHotspotClientNum = 0;
        this.mLastClientNum = -1;
        this.mDuration = 2;
        this.mContext = context;
        this.mNMService = nmService;
        this.mStatsService = statsService;
        this.mPolicyManager = policyManager;
        this.mPublicSync = new Object();
        this.mTetherStates = new ArrayMap();
        this.mLooper = IoThread.get().getLooper();
        this.mTetherMasterSM = new TetherMasterSM("TetherMaster", this.mLooper);
        this.mTetherMasterSM.start();
        this.mUpstreamNetworkMonitor = new UpstreamNetworkMonitor();
        this.mStateReceiver = new StateReceiver(this, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_STATE");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        filter.addAction(ACTION_ENABLE_NSIOT_TESTING);
        this.mContext.registerReceiver(this.mStateReceiver, filter);
        this.mNotificationSync = new Object();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        initializeHotspotExtra();
        this.mDhcpRange = context.getResources().getStringArray(17235994);
        if (this.mDhcpRange.length == 0 || this.mDhcpRange.length % 2 == 1) {
            this.mDhcpRange = DHCP_DEFAULT_RANGE;
        }
        this.mMdDirectTethering = new MdDirectTethering(this.mNMService);
        updateConfiguration();
        this.mDefaultDnsServers = new String[2];
        this.mDefaultDnsServers[0] = DNS_DEFAULT_SERVER1;
        this.mDefaultDnsServers[1] = DNS_DEFAULT_SERVER2;
        SystemProperties.set(SYSTEM_PROPERTY_NSIOT_PENDING, "false");
    }

    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) this.mContext.getSystemService("connectivity");
    }

    void updateConfiguration() {
        String[] tetherableUsbRegexs = this.mContext.getResources().getStringArray(17235990);
        String[] tetherableWifiRegexs = this.mContext.getResources().getStringArray(17235991);
        String[] tetherableBluetoothRegexs = this.mContext.getResources().getStringArray(17235993);
        int[] ifaceTypes = null;
        try {
            TelephonyManager mTelephonyManager = TelephonyManager.getDefault();
            int subId = SubscriptionManager.getDefaultDataSubscriptionId();
            String sMccMnc = null;
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                sMccMnc = mTelephonyManager.getSimOperator(subId);
            }
            if (sMccMnc != null && sMccMnc.length() >= 5) {
                String sMcc = sMccMnc.substring(0, 3);
                String sMnc = sMccMnc.substring(3, sMccMnc.length());
                Resources res = getResourcesUsingMccMnc(this.mContext, Integer.parseInt(sMcc), Integer.parseInt(sMnc));
                if (res != null) {
                    ifaceTypes = res.getIntArray(17235996);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ifaceTypes == null) {
            ifaceTypes = this.mContext.getResources().getIntArray(17235996);
        }
        Collection<Integer> upstreamIfaceTypes = new ArrayList();
        for (int i : ifaceTypes) {
            upstreamIfaceTypes.add(new Integer(i));
        }
        synchronized (this.mPublicSync) {
            if (this.mMdDirectTethering == null || !this.mMdDirectTethering.isMdtEnable()) {
                this.mTetherableUsbRegexs = tetherableUsbRegexs;
            } else {
                this.mTetherableUsbRegexs = this.mMdDirectTethering.getMdtTetherableUsbRegexs();
            }
            this.mTetherableWifiRegexs = tetherableWifiRegexs;
            this.mTetherableBluetoothRegexs = tetherableBluetoothRegexs;
            this.mUpstreamIfaceTypes = upstreamIfaceTypes;
        }
        checkDunRequired();
    }

    /* JADX WARNING: Missing block: B:17:0x0045, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void interfaceStatusChanged(String iface, boolean up) {
        if (VDBG) {
            Log.d(TAG, "interfaceStatusChanged " + iface + ", " + up);
        }
        synchronized (this.mPublicSync) {
            int interfaceType = ifaceNameToType(iface);
            if (interfaceType == -1) {
                return;
            }
            TetherState tetherState = (TetherState) this.mTetherStates.get(iface);
            if (up) {
                if (tetherState == null) {
                    trackNewTetherableInterface(iface, interfaceType);
                }
            } else if (interfaceType == 2) {
                Log.d(TAG, "interfaceLinkStatusChanged, only interfaceType == TETHERING_BLUETOOTH sendMessage:CMD_INTERFACE_DOWN");
                tetherState.mStateMachine.sendMessage(TetherInterfaceStateMachine.CMD_INTERFACE_DOWN);
                this.mTetherStates.remove(iface);
            } else if (VDBG) {
                Log.d(TAG, "ignore interface down for " + iface);
            }
        }
    }

    public void interfaceLinkStateChanged(String iface, boolean up) {
        if (VDBG) {
            Log.d(TAG, "interfaceLinkStateChanged " + iface + ", " + up);
        }
        interfaceStatusChanged(iface, up);
    }

    private boolean isUsb(String iface) {
        synchronized (this.mPublicSync) {
            for (String regex : this.mTetherableUsbRegexs) {
                if (iface.matches(regex)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean isWifi(String iface) {
        synchronized (this.mPublicSync) {
            for (String regex : this.mTetherableWifiRegexs) {
                if (iface.matches(regex)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean isBluetooth(String iface) {
        synchronized (this.mPublicSync) {
            for (String regex : this.mTetherableBluetoothRegexs) {
                if (iface.matches(regex)) {
                    return true;
                }
            }
            return false;
        }
    }

    private int ifaceNameToType(String iface) {
        if (isWifi(iface)) {
            return 0;
        }
        if (isUsb(iface)) {
            return 1;
        }
        if (isBluetooth(iface)) {
            return 2;
        }
        return -1;
    }

    /* JADX WARNING: Missing block: B:12:0x0047, code:
            return;
     */
    /* JADX WARNING: Missing block: B:18:0x0056, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void interfaceAdded(String iface) {
        if (VDBG) {
            Log.d(TAG, "interfaceAdded " + iface);
        }
        synchronized (this.mPublicSync) {
            int interfaceType = ifaceNameToType(iface);
            if (interfaceType == -1) {
                if (VDBG) {
                    Log.d(TAG, iface + " is not a tetherable iface, ignoring");
                }
            } else if (((TetherState) this.mTetherStates.get(iface)) == null) {
                trackNewTetherableInterface(iface, interfaceType);
            } else if (VDBG) {
                Log.d(TAG, "active iface (" + iface + ") reported as added, ignoring");
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0051, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void interfaceRemoved(String iface) {
        if (VDBG) {
            Log.d(TAG, "interfaceRemoved " + iface);
        }
        synchronized (this.mPublicSync) {
            TetherState tetherState = (TetherState) this.mTetherStates.get(iface);
            if (tetherState != null) {
                Log.d(TAG, "interfaceRemoved, iface=" + iface + ", sendMessage:CMD_INTERFACE_DOWN");
                tetherState.mStateMachine.sendMessage(TetherInterfaceStateMachine.CMD_INTERFACE_DOWN);
                this.mTetherStates.remove(iface);
            } else if (VDBG) {
                Log.e(TAG, "attempting to remove unknown iface (" + iface + "), ignoring");
            }
        }
    }

    public void startTethering(int type, ResultReceiver receiver, boolean showProvisioningUi) {
        Log.d(TAG, "startTethering:" + type);
        if (isTetherProvisioningRequired()) {
            Log.d(TAG, "TetherProvisioningRequired");
            if (showProvisioningUi) {
                runUiTetherProvisioningAndEnable(type, receiver);
            } else {
                runSilentTetherProvisioningAndEnable(type, receiver);
            }
            return;
        }
        Log.d(TAG, "Not TetherProvisioningRequired");
        enableTetheringInternal(type, true, receiver);
    }

    public void stopTethering(int type) {
        Log.d(TAG, "stopTethering:" + type);
        enableTetheringInternal(type, false, null);
        if (isTetherProvisioningRequired()) {
            cancelTetherProvisioningRechecks(type);
        }
    }

    private boolean isTetherProvisioningRequired() {
        boolean z = false;
        String[] provisionApp = this.mContext.getResources().getStringArray(17235995);
        if (SystemProperties.getBoolean("net.tethering.noprovisioning", false) || provisionApp == null) {
            return false;
        }
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if ((configManager == null || configManager.getConfig() == null) && VDBG) {
            Log.i(TAG, "configManager is null ,provisionApp.length " + provisionApp.length);
        }
        if (configManager == null || configManager.getConfig() == null || configManager.getConfig().getBoolean("require_entitlement_checks_bool")) {
            if (provisionApp.length == 2) {
                z = true;
            }
            return z;
        }
        if (VDBG) {
            Log.i(TAG, "isEntitlementCheckRequired = false ,return false");
        }
        return false;
    }

    private void enableTetheringInternal(int type, boolean enable, ResultReceiver receiver) {
        Log.d(TAG, "enableTetheringInternal type:" + type + ", enable:" + enable);
        boolean isProvisioningRequired = enable ? isTetherProvisioningRequired() : false;
        int result;
        switch (type) {
            case 0:
                result = setWifiTethering(enable);
                if (isProvisioningRequired && result == 0) {
                    scheduleProvisioningRechecks(type);
                }
                sendTetherResult(receiver, result);
                return;
            case 1:
                result = setUsbTethering(enable);
                if (isProvisioningRequired && result == 0) {
                    scheduleProvisioningRechecks(type);
                }
                sendTetherResult(receiver, result);
                return;
            case 2:
                setBluetoothTethering(enable, receiver);
                return;
            default:
                Log.w(TAG, "Invalid tether type.");
                sendTetherResult(receiver, 1);
                return;
        }
    }

    private void sendTetherResult(ResultReceiver receiver, int result) {
        if (receiver != null) {
            receiver.send(result, null);
        }
    }

    private int setWifiTethering(boolean enable) {
        synchronized (this.mPublicSync) {
            if (VDBG) {
                Log.d(TAG, "setWifiTethering:" + enable);
            }
            this.mWifiTetherRequested = enable;
            if (((WifiManager) this.mContext.getSystemService("wifi")).setWifiApEnabled(null, enable)) {
                return 0;
            }
            return 5;
        }
    }

    private void setBluetoothTethering(final boolean enable, final ResultReceiver receiver) {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            Log.w(TAG, "Tried to enable bluetooth tethering with null or disabled adapter. null: " + (adapter == null));
            sendTetherResult(receiver, 2);
            return;
        }
        adapter.getProfileProxy(this.mContext, new ServiceListener() {
            public void onServiceDisconnected(int profile) {
            }

            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                int result;
                ((BluetoothPan) proxy).setBluetoothTethering(enable);
                if (((BluetoothPan) proxy).isTetheringOn() == enable) {
                    result = 0;
                } else {
                    result = 5;
                }
                Tethering.this.sendTetherResult(receiver, result);
                if (enable && Tethering.this.isTetherProvisioningRequired()) {
                    Tethering.this.scheduleProvisioningRechecks(2);
                }
                adapter.closeProfileProxy(5, proxy);
            }
        }, 5);
    }

    private void runUiTetherProvisioningAndEnable(int type, ResultReceiver receiver) {
        sendUiTetherProvisionIntent(type, getProxyReceiver(type, receiver));
    }

    private void sendUiTetherProvisionIntent(int type, ResultReceiver receiver) {
        Intent intent = new Intent("android.settings.TETHER_PROVISIONING_UI");
        intent.putExtra("extraAddTetherType", type);
        intent.putExtra("extraProvisionCallback", receiver);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private ResultReceiver getProxyReceiver(final int type, final ResultReceiver receiver) {
        ResultReceiver rr = new ResultReceiver(null) {
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == 0) {
                    Tethering.this.enableTetheringInternal(type, true, receiver);
                } else {
                    Tethering.this.sendTetherResult(receiver, resultCode);
                }
            }
        };
        Parcel parcel = Parcel.obtain();
        rr.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ResultReceiver receiverForSending = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return receiverForSending;
    }

    private void scheduleProvisioningRechecks(int type) {
        Log.d(TAG, "scheduleProvisioningRechecks type:" + type);
        Intent intent = new Intent();
        intent.putExtra("extraAddTetherType", type);
        intent.putExtra("extraSetAlarm", true);
        intent.setComponent(TETHER_SERVICE);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void runSilentTetherProvisioningAndEnable(int type, ResultReceiver receiver) {
        sendSilentTetherProvisionIntent(type, getProxyReceiver(type, receiver));
    }

    private void sendSilentTetherProvisionIntent(int type, ResultReceiver receiver) {
        Intent intent = new Intent();
        intent.putExtra("extraAddTetherType", type);
        intent.putExtra("extraRunProvision", true);
        intent.putExtra("extraProvisionCallback", receiver);
        intent.setComponent(TETHER_SERVICE);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void cancelTetherProvisioningRechecks(int type) {
        if (getConnectivityManager().isTetheringSupported()) {
            Intent intent = new Intent();
            intent.putExtra("extraRemTetherType", type);
            intent.setComponent(TETHER_SERVICE);
            long ident = Binder.clearCallingIdentity();
            try {
                this.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public List<WifiDevice> getTetherConnectedSta() {
        List<WifiDevice> TetherConnectedStaList = new ArrayList();
        for (String key : this.mConnectedDeviceMap.keySet()) {
            WifiDevice device = (WifiDevice) this.mConnectedDeviceMap.get(key);
            if (device != null) {
                if (VDBG) {
                    Log.d(TAG, "getTetherConnectedSta: addr=" + key + " name=" + device.deviceName);
                }
                TetherConnectedStaList.add(device);
            }
        }
        return TetherConnectedStaList;
    }

    private void sendTetherConnectStateChangedBroadcast() {
        if (getConnectivityManager().isTetheringSupported()) {
            Intent broadcast = new Intent("codeaurora.net.conn.TETHER_CONNECT_STATE_CHANGED");
            broadcast.addFlags(603979776);
            this.mContext.sendStickyBroadcastAsUser(broadcast, UserHandle.ALL);
            int clientNum = this.mConnectedDeviceMap.size();
            if (this.mHotspotClientNum != clientNum) {
                if (DBG) {
                    Log.d(TAG, "WIFI_HOTSPOT_CLIENTS_CHANGED_ACTION: mHotspotClientNum=" + this.mHotspotClientNum + " DeviceMap size=" + clientNum);
                }
                this.mHotspotClientNum = clientNum;
                sendTetherStateChangedBroadcast();
            }
            Log.d(TAG, "clientNum of L2ConnectedDevice is : " + clientNum);
            Intent numIntent = new Intent("android.net.wifi.WIFI_HOTSPOT_CLIENTS_CHANGED");
            numIntent.addFlags(603979776);
            numIntent.putExtra("HotspotClientNum", clientNum);
            this.mContext.sendBroadcastAsUser(numIntent, UserHandle.ALL);
        }
    }

    private void initializeHotspotExtra() {
        this.mIntentStopHotspot = PendingIntent.getBroadcast(this.mContext, 2, new Intent(ACTION_STOP_HOTSPOT), 0);
        this.mDuration = System.getInt(this.mContext.getContentResolver(), "wifi_hotspot_auto_disable", 2);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_STOP_HOTSPOT);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(Tethering.TAG, "mIntentStopHotspot onReceive, action:" + action);
                if (action.equals(Tethering.ACTION_STOP_HOTSPOT)) {
                    Tethering.this.mWifiManager.setWifiApEnabled(null, false);
                    int wifiSavedState = 0;
                    try {
                        wifiSavedState = Global.getInt(Tethering.this.mContext.getContentResolver(), "wifi_saved_state");
                    } catch (SettingNotFoundException e) {
                        Log.e(Tethering.TAG, "SettingNotFoundException:" + e);
                    }
                    Log.d(Tethering.TAG, "Received stop hotspot intent, wifiSavedState:" + wifiSavedState);
                    if (wifiSavedState == 1) {
                        Tethering.this.mWifiManager.setWifiEnabled(true);
                        Global.putInt(Tethering.this.mContext.getContentResolver(), "wifi_saved_state", 0);
                    }
                }
            }
        }, intentFilter);
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0067 A:{SYNTHETIC, Splitter: B:25:0x0067} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0070 A:{SYNTHETIC, Splitter: B:30:0x0070} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean readDeviceInfoFromDnsmasq(WifiDevice device) {
        IOException ex;
        Throwable th;
        boolean result = false;
        FileInputStream fstream = null;
        try {
            FileInputStream fstream2 = new FileInputStream(dhcpLocation);
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fstream2)));
                while (true) {
                    String line = br.readLine();
                    if (line == null || line.length() == 0) {
                        break;
                    }
                    String[] fields = line.split(" ");
                    if (fields.length > 3) {
                        String addr = fields[1];
                        String name = fields[3];
                        if (addr.equals(device.deviceAddress)) {
                            device.deviceName = name;
                            result = true;
                            break;
                        }
                    }
                }
                if (fstream2 != null) {
                    try {
                        fstream2.close();
                    } catch (IOException e) {
                    }
                }
                fstream = fstream2;
            } catch (IOException e2) {
                ex = e2;
                fstream = fstream2;
                try {
                    Log.e(TAG, "readDeviceNameFromDnsmasq: " + ex);
                    if (fstream != null) {
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    if (fstream != null) {
                        try {
                            fstream.close();
                        } catch (IOException e3) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fstream = fstream2;
                if (fstream != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            ex = e4;
            Log.e(TAG, "readDeviceNameFromDnsmasq: " + ex);
            if (fstream != null) {
                try {
                    fstream.close();
                } catch (IOException e5) {
                }
            }
            return result;
        }
        return result;
    }

    public void interfaceMessageRecevied(String message) {
        if (DBG) {
            Log.d(TAG, "interfaceMessageRecevied: message=" + message);
        }
        try {
            WifiDevice device = new WifiDevice(message);
            if (device.deviceState == 1) {
                this.mL2ConnectedDeviceMap.put(device.deviceAddress, device);
                if (readDeviceInfoFromDnsmasq(device)) {
                    this.mConnectedDeviceMap.put(device.deviceAddress, device);
                    Log.d(TAG, "Some client connect: clientnum = " + this.mConnectedDeviceMap.size());
                    if (this.mDuration != 0 && this.mConnectedDeviceMap.size() >= 1) {
                        this.mAlarmManager.cancel(this.mIntentStopHotspot);
                    }
                    sendTetherConnectStateChangedBroadcast();
                    return;
                }
                if (DBG) {
                    Log.d(TAG, "Starting poll device info for " + device.deviceAddress);
                }
                new DnsmasqThread(this, device, 1000, 10).start();
            } else if (device.deviceState == 0) {
                this.mL2ConnectedDeviceMap.remove(device.deviceAddress);
                this.mConnectedDeviceMap.remove(device.deviceAddress);
                if (this.mDuration != 0 && this.mConnectedDeviceMap.size() == 0) {
                    Log.d(TAG, "Set alarm for no client, mDuration:" + this.mDuration);
                    this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + (((long) this.mDuration) * 300000), this.mIntentStopHotspot);
                }
                sendTetherConnectStateChangedBroadcast();
            }
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "WifiDevice IllegalArgument: " + ex);
        }
    }

    public int tether(String iface) {
        if (DBG) {
            Log.d(TAG, "Tethering " + iface);
        }
        synchronized (this.mPublicSync) {
            TetherState tetherState = (TetherState) this.mTetherStates.get(iface);
            if (tetherState == null) {
                Log.e(TAG, "Tried to Tether an unknown iface: " + iface + ", ignoring");
                return 1;
            } else if (tetherState.mLastState != 1) {
                Log.e(TAG, "Tried to Tether an unavailable iface: " + iface + ", ignoring");
                return 4;
            } else {
                tetherState.mStateMachine.sendMessage(TetherInterfaceStateMachine.CMD_TETHER_REQUESTED);
                return 0;
            }
        }
    }

    public int untether(String iface) {
        if (DBG) {
            Log.d(TAG, "Untethering " + iface);
        }
        synchronized (this.mPublicSync) {
            TetherState tetherState = (TetherState) this.mTetherStates.get(iface);
            if (tetherState == null) {
                Log.e(TAG, "Tried to Untether an unknown iface :" + iface + ", ignoring");
                return 1;
            } else if (tetherState.mLastState != 2) {
                Log.e(TAG, "Tried to untether an untethered iface :" + iface + ", ignoring");
                return 4;
            } else {
                tetherState.mStateMachine.sendMessage(TetherInterfaceStateMachine.CMD_TETHER_UNREQUESTED);
                return 0;
            }
        }
    }

    public void untetherAll() {
        stopTethering(0);
        stopTethering(1);
        stopTethering(2);
    }

    public int getLastTetherError(String iface) {
        synchronized (this.mPublicSync) {
            TetherState tetherState = (TetherState) this.mTetherStates.get(iface);
            if (tetherState == null) {
                Log.e(TAG, "Tried to getLastTetherError on an unknown iface :" + iface + ", ignoring");
                return 1;
            }
            int i = tetherState.mLastError;
            return i;
        }
    }

    private void sendTetherStateChangedBroadcast() {
        if (getConnectivityManager().isTetheringSupported()) {
            Log.d(TAG, "sendTetherStateChangedBroadcast");
            ArrayList<String> availableList = new ArrayList();
            ArrayList<String> activeList = new ArrayList();
            ArrayList<String> erroredList = new ArrayList();
            boolean wifiTethered = false;
            boolean usbTethered = false;
            boolean bluetoothTethered = false;
            synchronized (this.mPublicSync) {
                for (int i = 0; i < this.mTetherStates.size(); i++) {
                    TetherState tetherState = (TetherState) this.mTetherStates.valueAt(i);
                    String iface = (String) this.mTetherStates.keyAt(i);
                    if (tetherState.mLastError != 0) {
                        Log.d(TAG, "add err");
                        erroredList.add(iface);
                    } else if (tetherState.mLastState == 1) {
                        Log.d(TAG, "add avai");
                        availableList.add(iface);
                    } else if (tetherState.mLastState == 2) {
                        if (isUsb(iface)) {
                            Log.d(TAG, "usb isTethered");
                            usbTethered = true;
                        } else if (isWifi(iface)) {
                            Log.d(TAG, "wifi isTethered");
                            wifiTethered = true;
                        } else if (isBluetooth(iface)) {
                            Log.d(TAG, "bt isTethered");
                            bluetoothTethered = true;
                        }
                        activeList.add(iface);
                    }
                }
            }
            Intent broadcast = new Intent("android.net.conn.TETHER_STATE_CHANGED");
            broadcast.addFlags(603979776);
            broadcast.putStringArrayListExtra("availableArray", availableList);
            broadcast.putStringArrayListExtra("activeArray", activeList);
            broadcast.putStringArrayListExtra("erroredArray", erroredList);
            this.mContext.sendStickyBroadcastAsUser(broadcast, UserHandle.ALL);
            if (DBG) {
                String str = TAG;
                Object[] objArr = new Object[3];
                objArr[0] = TextUtils.join(",", availableList);
                objArr[1] = TextUtils.join(",", activeList);
                objArr[2] = TextUtils.join(",", erroredList);
                Log.d(str, String.format("sendTetherStateChangedBroadcast avail=[%s] active=[%s] error=[%s]", objArr));
            }
            if (usbTethered) {
                if (wifiTethered || bluetoothTethered) {
                    if (!wifiTethered && bluetoothTethered) {
                        this.mHotspotClientNum = 0;
                    }
                    oppoShowTetheredNotification(201852002);
                } else {
                    this.mHotspotClientNum = 0;
                    oppoShowTetheredNotification(201852004);
                }
            } else if (wifiTethered) {
                if (bluetoothTethered) {
                    oppoShowTetheredNotification(201852002);
                } else {
                    clearTetheredNotification();
                    oppoShowTetheredNotification(201852003);
                }
            } else if (bluetoothTethered) {
                this.mHotspotClientNum = 0;
                oppoShowTetheredNotification(201852112);
            } else {
                this.mHotspotClientNum = 0;
                this.mLastClientNum = -1;
                clearTetheredNotification();
            }
        }
    }

    private void showTetheredNotification(int icon) {
        Log.i(TAG, "showTetheredNotification icon:" + icon);
        synchronized (this.mNotificationSync) {
            NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManagerService.NOTIFICATON_TITLE_NAME);
            if (notificationManager == null) {
                return;
            }
            if (this.mLastNotificationId != 0) {
                if (this.mLastNotificationId == icon) {
                    return;
                } else {
                    notificationManager.cancelAsUser(null, this.mLastNotificationId, UserHandle.ALL);
                    this.mLastNotificationId = 0;
                }
            }
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.TetherSettings");
            intent.setFlags(1073741824);
            PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, 0, intent, 0, null, UserHandle.CURRENT);
            Resources r = Resources.getSystem();
            CharSequence title = r.getText(17040531);
            CharSequence message = r.getText(17040532);
            if (this.mTetheredNotificationBuilder == null) {
                this.mTetheredNotificationBuilder = new Builder(this.mContext);
                this.mTetheredNotificationBuilder.setWhen(0).setOngoing(true).setColor(this.mContext.getColor(17170523)).setVisibility(1).setCategory("status");
            }
            this.mTetheredNotificationBuilder.setSmallIcon(icon).setContentTitle(title).setContentText(message).setContentIntent(pi);
            this.mLastNotificationId = icon;
            notificationManager.notifyAsUser(null, this.mLastNotificationId, this.mTetheredNotificationBuilder.build(), UserHandle.ALL);
        }
    }

    private void clearTetheredNotification() {
        Log.i(TAG, "clearTetheredNotification");
        synchronized (this.mNotificationSync) {
            NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManagerService.NOTIFICATON_TITLE_NAME);
            if (!(notificationManager == null || this.mLastNotificationId == 0)) {
                notificationManager.cancel(0);
                this.mLastNotificationId = 0;
            }
        }
    }

    private void tetherMatchingInterfaces(boolean enable, int interfaceType) {
        if (VDBG) {
            Log.d(TAG, "tetherMatchingInterfaces(" + enable + ", " + interfaceType + ")");
        }
        try {
            String[] ifaces = this.mNMService.listInterfaces();
            String chosenIface = null;
            if (ifaces != null) {
                for (String iface : ifaces) {
                    if (ifaceNameToType(iface) == interfaceType) {
                        chosenIface = iface;
                        break;
                    }
                }
            }
            if (chosenIface == null) {
                Log.e(TAG, "could not find iface of type " + interfaceType);
                return;
            }
            if ((enable ? tether(chosenIface) : untether(chosenIface)) != 0) {
                Log.e(TAG, "unable start or stop tethering on iface " + chosenIface);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error listing Interfaces", e);
        }
    }

    public String[] getTetherableUsbRegexs() {
        return this.mTetherableUsbRegexs;
    }

    public String[] getTetherableWifiRegexs() {
        return this.mTetherableWifiRegexs;
    }

    public String[] getTetherableBluetoothRegexs() {
        return this.mTetherableBluetoothRegexs;
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.server.connectivity.Tethering.setUsbTethering(boolean):int, dom blocks: [B:11:0x0040, B:24:0x0069]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public int setUsbTethering(boolean r8) {
        /*
        r7 = this;
        r6 = 0;
        r3 = VDBG;
        if (r3 == 0) goto L_0x0026;
    L_0x0005:
        r3 = "Tethering";
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "setUsbTethering(";
        r4 = r4.append(r5);
        r4 = r4.append(r8);
        r5 = ")";
        r4 = r4.append(r5);
        r4 = r4.toString();
        android.util.Log.d(r3, r4);
    L_0x0026:
        r3 = r7.mContext;
        r4 = "usb";
        r2 = r3.getSystemService(r4);
        r2 = (android.hardware.usb.UsbManager) r2;
        r4 = r7.mPublicSync;
        monitor-enter(r4);
        if (r8 == 0) goto L_0x0063;
    L_0x0036:
        r3 = r7.mRndisEnabled;	 Catch:{ all -> 0x004d }
        if (r3 == 0) goto L_0x0050;	 Catch:{ all -> 0x004d }
    L_0x003a:
        r0 = android.os.Binder.clearCallingIdentity();	 Catch:{ all -> 0x004d }
        r3 = 1;
        r5 = 1;
        r7.tetherMatchingInterfaces(r3, r5);	 Catch:{ all -> 0x0048 }
        android.os.Binder.restoreCallingIdentity(r0);	 Catch:{ all -> 0x004d }
    L_0x0046:
        monitor-exit(r4);
        return r6;
    L_0x0048:
        r3 = move-exception;
        android.os.Binder.restoreCallingIdentity(r0);	 Catch:{ all -> 0x004d }
        throw r3;	 Catch:{ all -> 0x004d }
    L_0x004d:
        r3 = move-exception;
        monitor-exit(r4);
        throw r3;
    L_0x0050:
        r3 = 1;
        r7.mUsbTetherRequested = r3;	 Catch:{ all -> 0x004d }
        r3 = "Tethering";	 Catch:{ all -> 0x004d }
        r5 = "The MTK_TETHERING_RNDIS only";	 Catch:{ all -> 0x004d }
        android.util.Log.d(r3, r5);	 Catch:{ all -> 0x004d }
        r3 = "rndis";	 Catch:{ all -> 0x004d }
        r2.setCurrentFunction(r3);	 Catch:{ all -> 0x004d }
        goto L_0x0046;	 Catch:{ all -> 0x004d }
    L_0x0063:
        r0 = android.os.Binder.clearCallingIdentity();	 Catch:{ all -> 0x004d }
        r3 = 0;
        r5 = 1;
        r7.tetherMatchingInterfaces(r3, r5);	 Catch:{ all -> 0x007b }
        android.os.Binder.restoreCallingIdentity(r0);	 Catch:{ all -> 0x004d }
        r3 = r7.mRndisEnabled;	 Catch:{ all -> 0x004d }
        if (r3 == 0) goto L_0x0077;	 Catch:{ all -> 0x004d }
    L_0x0073:
        r3 = 0;	 Catch:{ all -> 0x004d }
        r2.setCurrentFunction(r3);	 Catch:{ all -> 0x004d }
    L_0x0077:
        r3 = 0;	 Catch:{ all -> 0x004d }
        r7.mUsbTetherRequested = r3;	 Catch:{ all -> 0x004d }
        goto L_0x0046;	 Catch:{ all -> 0x004d }
    L_0x007b:
        r3 = move-exception;	 Catch:{ all -> 0x004d }
        android.os.Binder.restoreCallingIdentity(r0);	 Catch:{ all -> 0x004d }
        throw r3;	 Catch:{ all -> 0x004d }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.Tethering.setUsbTethering(boolean):int");
    }

    public int[] getUpstreamIfaceTypes() {
        int[] values;
        synchronized (this.mPublicSync) {
            updateConfiguration();
            values = new int[this.mUpstreamIfaceTypes.size()];
            Iterator<Integer> iterator = this.mUpstreamIfaceTypes.iterator();
            for (int i = 0; i < this.mUpstreamIfaceTypes.size(); i++) {
                values[i] = ((Integer) iterator.next()).intValue();
            }
        }
        return values;
    }

    private void checkDunRequired() {
        int secureSetting = 2;
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            secureSetting = tm.getTetherApnRequired();
        }
        synchronized (this.mPublicSync) {
            if (VDBG) {
                Log.i(TAG, "checkDunRequired:" + secureSetting);
            }
            if (secureSetting != 2) {
                int requiredApn;
                if (secureSetting == 1) {
                    requiredApn = 4;
                } else {
                    requiredApn = 5;
                }
                if (requiredApn == 4) {
                    while (this.mUpstreamIfaceTypes.contains(MOBILE_TYPE)) {
                        this.mUpstreamIfaceTypes.remove(MOBILE_TYPE);
                    }
                    while (this.mUpstreamIfaceTypes.contains(HIPRI_TYPE)) {
                        this.mUpstreamIfaceTypes.remove(HIPRI_TYPE);
                    }
                    if (!this.mUpstreamIfaceTypes.contains(DUN_TYPE)) {
                        this.mUpstreamIfaceTypes.add(DUN_TYPE);
                    }
                } else {
                    while (this.mUpstreamIfaceTypes.contains(DUN_TYPE)) {
                        this.mUpstreamIfaceTypes.remove(DUN_TYPE);
                    }
                    if (!this.mUpstreamIfaceTypes.contains(MOBILE_TYPE)) {
                        this.mUpstreamIfaceTypes.add(MOBILE_TYPE);
                    }
                    if (!this.mUpstreamIfaceTypes.contains(HIPRI_TYPE)) {
                        this.mUpstreamIfaceTypes.add(HIPRI_TYPE);
                    }
                }
            }
            if (this.mUpstreamIfaceTypes.contains(DUN_TYPE)) {
                this.mPreferredUpstreamMobileApn = 4;
            } else {
                this.mPreferredUpstreamMobileApn = 5;
            }
            Log.d(TAG, "mPreferredUpstreamMobileApn = " + this.mPreferredUpstreamMobileApn);
        }
    }

    public String[] getTetheredIfaces() {
        ArrayList<String> list = new ArrayList();
        synchronized (this.mPublicSync) {
            for (int i = 0; i < this.mTetherStates.size(); i++) {
                if (((TetherState) this.mTetherStates.valueAt(i)).mLastState == 2) {
                    list.add((String) this.mTetherStates.keyAt(i));
                }
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public String[] getTetheredIfacePairs() {
        ArrayList<String> list = new ArrayList();
        synchronized (this.mPublicSync) {
            for (int i = 0; i < this.mTetherStates.size(); i++) {
                TetherState tetherState = (TetherState) this.mTetherStates.valueAt(i);
                if (tetherState.mLastState == 2) {
                    String upStreamIface = tetherState.mStateMachine.upstreamIfaceName();
                    list.add(upStreamIface);
                    list.add((String) this.mTetherStates.keyAt(i));
                    Log.i(TAG, "getTetheredIfacePairs:" + upStreamIface + ", " + ((String) this.mTetherStates.keyAt(i)));
                }
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public String[] getTetherableIfaces() {
        ArrayList<String> list = new ArrayList();
        synchronized (this.mPublicSync) {
            for (int i = 0; i < this.mTetherStates.size(); i++) {
                if (((TetherState) this.mTetherStates.valueAt(i)).mLastState == 1) {
                    list.add((String) this.mTetherStates.keyAt(i));
                }
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public String[] getTetheredDhcpRanges() {
        return this.mDhcpRange;
    }

    public String[] getErroredIfaces() {
        ArrayList<String> list = new ArrayList();
        synchronized (this.mPublicSync) {
            for (int i = 0; i < this.mTetherStates.size(); i++) {
                if (((TetherState) this.mTetherStates.valueAt(i)).mLastError != 0) {
                    list.add((String) this.mTetherStates.keyAt(i));
                }
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    private void maybeLogMessage(State state, int what) {
        if (DBG) {
            Log.d(TAG, state.getName() + " got " + ((String) sMagicDecoderRing.get(what, Integer.toString(what))));
        }
    }

    public boolean getIpv6FeatureEnable() {
        return this.mIpv6FeatureEnable;
    }

    public void setIpv6FeatureEnable(boolean enable) {
        Log.d(TAG, "setIpv6FeatureEnable:" + enable + " old:" + this.mIpv6FeatureEnable);
        int value = enable ? 1 : 0;
        if (this.mIpv6FeatureEnable != enable) {
            this.mIpv6FeatureEnable = enable;
            System.putInt(this.mContext.getContentResolver(), "tether_ipv6_feature", value);
        }
    }

    boolean pertainsToCurrentUpstream(NetworkState ns) {
        if (!(ns == null || ns.linkProperties == null || this.mCurrentUpstreamIface == null)) {
            for (String ifname : ns.linkProperties.getAllInterfaceNames()) {
                if (this.mCurrentUpstreamIface.equals(ifname)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump ConnectivityService.Tether from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        pw.println("Tethering:");
        pw.increaseIndent();
        pw.print("mUpstreamIfaceTypes:");
        synchronized (this.mPublicSync) {
            for (Integer netType : this.mUpstreamIfaceTypes) {
                pw.print(" " + ConnectivityManager.getNetworkTypeName(netType.intValue()));
            }
            pw.println();
            pw.println("Tether state:");
            pw.increaseIndent();
            for (int i = 0; i < this.mTetherStates.size(); i++) {
                TetherState tetherState = (TetherState) this.mTetherStates.valueAt(i);
                pw.print(((String) this.mTetherStates.keyAt(i)) + " - ");
                switch (tetherState.mLastState) {
                    case 0:
                        pw.print("UnavailableState");
                        break;
                    case 1:
                        pw.print("AvailableState");
                        break;
                    case 2:
                        pw.print("TetheredState");
                        break;
                    default:
                        pw.print("UnknownState");
                        break;
                }
                pw.println(" - lastError = " + tetherState.mLastError);
            }
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
    }

    public void notifyInterfaceStateChange(String iface, TetherInterfaceStateMachine who, int state, int error) {
        synchronized (this.mPublicSync) {
            TetherState tetherState = (TetherState) this.mTetherStates.get(iface);
            if (tetherState != null && tetherState.mStateMachine.equals(who)) {
                tetherState.mLastState = state;
                tetherState.mLastError = error;
            } else if (DBG) {
                Log.d(TAG, "got notification from stale iface " + iface);
            }
        }
        if (DBG) {
            Log.d(TAG, "iface " + iface + " notified that it was in state " + state + " with error " + error);
        }
        try {
            this.mPolicyManager.onTetheringChanged(iface, state == 2);
        } catch (RemoteException e) {
        }
        if (error == 5) {
            this.mTetherMasterSM.sendMessage(327686, who);
        }
        switch (state) {
            case 0:
            case 1:
                this.mTetherMasterSM.sendMessage(327682, who);
                break;
            case 2:
                this.mTetherMasterSM.sendMessage(327681, who);
                break;
        }
        sendTetherStateChangedBroadcast();
    }

    private void trackNewTetherableInterface(String iface, int interfaceType) {
        TetherState tetherState = new TetherState(new TetherInterfaceStateMachine(iface, this.mLooper, interfaceType, this.mNMService, this.mStatsService, this, this.mMdDirectTethering));
        this.mTetherStates.put(iface, tetherState);
        tetherState.mStateMachine.start();
    }

    public boolean isTetheringChangeDone() {
        Log.d(TAG, "TODO: mIsTetheringChangeDone need implement");
        return this.mIsTetheringChangeDone;
    }

    private String getDownstreamIface() {
        synchronized (this.mPublicSync) {
            for (int i = 0; i < this.mTetherStates.size(); i++) {
                String iface = (String) this.mTetherStates.keyAt(i);
                if (((TetherState) this.mTetherStates.valueAt(i)).mLastState == 2) {
                    Log.d(TAG, "getDownstreamIface:" + iface);
                    return iface;
                }
            }
            return null;
        }
    }

    private boolean enableUdpForwardingForUsb(boolean enabled, String ipAddr) {
        Toast mToast = Toast.makeText(this.mContext, null, 0);
        Log.d(TAG, "enableUdpForwardingForUsb:" + enabled + ":" + ipAddr);
        String[] tetherInterfaces = getTetheredIfacePairs();
        if (this.mCurrentUpstreamIface != null) {
            tetherInterfaces[0] = this.mCurrentUpstreamIface;
        }
        if (tetherInterfaces.length != 2) {
            Log.e(TAG, "[NS-IOT]Wrong tethering state:" + tetherInterfaces.length);
            mToast.setText("Please only enable one tethering, now:" + (tetherInterfaces.length / 2));
            mToast.show();
            return false;
        } else if (tetherInterfaces[0] == null) {
            Log.e(TAG, "[NS-IOT]Upstream is null");
            mToast.setText("[NS-IOT]Upstream is null" + (tetherInterfaces.length / 2));
            mToast.show();
            return false;
        } else {
            String extInterface = tetherInterfaces[0];
            String inInterface = tetherInterfaces[1];
            if (ipAddr == null || ipAddr.length() == 0 || "unknown".equals(ipAddr)) {
                try {
                    Log.e(TAG, "[NS-IOT]getUsbClient(" + inInterface);
                    this.mNMService.getUsbClient(inInterface);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "[NS-IOT]getUsbClient failed!");
                }
                ipAddr = SystemProperties.get("net.rndis.client");
                if (enabled && (ipAddr == null || ipAddr.length() == 0)) {
                    Log.d(TAG, "[NS-IOT]There is no HostPC address!");
                    mToast.setText("There is no HostPC address");
                    mToast.show();
                    return false;
                }
                Log.d(TAG, "[NS-IOT]Disable or There is HostPC prefix: " + ipAddr);
            }
            mToast.setText("enableUdpForwarding(" + enabled + "," + inInterface + "," + extInterface + "," + ipAddr);
            mToast.show();
            try {
                Log.d(TAG, "[NS-IOT]enableUdpForwarding(" + enabled + "," + inInterface + "," + extInterface + "," + ipAddr);
                this.mNMService.enableUdpForwarding(enabled, inInterface, extInterface, ipAddr);
                this.mNMService.setMtu(extInterface, 1500);
                return true;
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.e(TAG, "[NS-IOT]enableUdpForwarding failed!");
                mToast.setText("enableUdpForwarding failed!");
                mToast.show();
                return false;
            }
        }
    }

    private Resources getResourcesUsingMccMnc(Context context, int mcc, int mnc) {
        try {
            if (DBG) {
                Log.i(TAG, "getResourcesUsingMccMnc: mcc = " + mcc + ", mnc = " + mnc);
            }
            Configuration configuration = new Configuration();
            configuration.mcc = mcc;
            configuration.mnc = mnc;
            return context.createConfigurationContext(configuration).getResources();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "getResourcesUsingMccMnc fail, return null");
            return null;
        }
    }

    private void oppoShowTetheredNotification(int icon) {
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManagerService.NOTIFICATON_TITLE_NAME);
        if (notificationManager != null) {
            CharSequence title;
            if (this.mTetheredNotificationBuilder != null) {
                if (this.mLastNotificationId != icon || (201852003 == icon && this.mLastClientNum != this.mHotspotClientNum)) {
                    notificationManager.cancel(0);
                    this.mLastNotificationId = 0;
                } else {
                    Log.d(TAG, "the same notification, do not update, just reurn!");
                    return;
                }
            }
            this.mLastClientNum = this.mHotspotClientNum;
            int largeIcon = icon;
            switch (icon) {
                case 201852002:
                    largeIcon = 201852117;
                    break;
                case 201852003:
                    largeIcon = 201852114;
                    break;
                case 201852004:
                    largeIcon = 201852116;
                    break;
                case 201852112:
                    largeIcon = 201852115;
                    break;
            }
            Intent intent = new Intent();
            Resources r = Resources.getSystem();
            if (icon == 201852003) {
                intent.setAction("android.settings.OPPO_WIFI_AP_SETTINGS");
                if (this.mHotspotClientNum == 0) {
                    title = r.getText(201590070);
                } else {
                    title = this.mHotspotClientNum + r.getText(201590071).toString();
                }
            } else {
                intent.setAction("android.settings.OPPO_TETHER_SETTINGS");
                title = r.getText(201590072);
            }
            intent.setFlags(1073741824);
            PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, 0, intent, 0, null, UserHandle.CURRENT);
            CharSequence message = r.getText(17040532);
            if (this.mTetheredNotificationBuilder != null) {
                this.mTetheredNotificationBuilder = null;
            }
            this.mTetheredNotificationBuilder = new Builder(this.mContext).setContentTitle(title).setContentText(message).setTicker(title).setContentIntent(pi).setSmallIcon(201852197).setLargeIcon(BitmapFactory.decodeResource(this.mContext.getResources(), largeIcon));
            this.mLastNotificationId = icon;
            Notification mTeNotification = this.mTetheredNotificationBuilder.build();
            mTeNotification.flags = 2;
            mTeNotification.defaults &= -2;
            if (DBG) {
                Log.d(TAG, "notify teteredNotification ticker: " + title);
            }
            notificationManager.notify(0, mTeNotification);
        }
    }
}
