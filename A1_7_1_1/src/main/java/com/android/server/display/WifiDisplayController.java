package com.android.server.display;

import android.app.AlertDialog;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.StatusBarManager;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.display.WifiDisplay;
import android.hardware.display.WifiDisplaySessionInfo;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.RemoteDisplay;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.PersistentGroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.WifiP2pLinkInfoListener;
import android.net.wifi.p2p.WifiP2pWfdInfo;
import android.net.wifi.p2p.link.WifiP2pLinkInfo;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.util.DumpUtils.Dump;
import com.android.internal.view.IInputMethodManager;
import com.android.server.LocationManagerService;
import com.android.server.notification.NotificationManagerService;
import com.android.server.oppo.IElsaManager;
import com.mediatek.hdmi.IMtkHdmiManager;
import com.mediatek.hdmi.IMtkHdmiManager.Stub;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.util.Objects;

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
final class WifiDisplayController implements Dump {
    private static final int CONNECTION_TIMEOUT_SECONDS = 60;
    private static final int CONNECT_MAX_RETRIES = 3;
    private static final int CONNECT_MIN_RETRIES = 0;
    private static final int CONNECT_RETRY_DELAY_MILLIS = 500;
    private static boolean DEBUG = false;
    private static final int DEFAULT_CONTROL_PORT = 7236;
    private static final int DISCOVER_PEERS_INTERVAL_MILLIS = 10000;
    public static final String DRM_CONTENT_MEDIAPLAYER = "com.mediatek.mediaplayer.DRM_PLAY";
    private static final int MAX_THROUGHPUT = 50;
    private static final int RECONNECT_RETRY_DELAY_MILLIS = 1000;
    private static final int RESCAN_RETRY_DELAY_MILLIS = 2000;
    private static final int RTSP_SINK_TIMEOUT_SECONDS = 10;
    private static final int RTSP_TIMEOUT_SECONDS = 75;
    private static final int RTSP_TIMEOUT_SECONDS_CERT_MODE = 120;
    private static final String TAG = "WifiDisplayController";
    private static final int WFDCONTROLLER_AVERATE_SCORE_COUNT = 4;
    private static final int WFDCONTROLLER_HDMI_UPDATE = 2;
    private static final int WFDCONTROLLER_INVALID_VALUE = -1;
    private static final int WFDCONTROLLER_LATENCY_INFO_DELAY_MILLIS = 2000;
    private static final int WFDCONTROLLER_LATENCY_INFO_FIRST_MILLIS = 100;
    private static final int WFDCONTROLLER_LATENCY_INFO_PERIOD_MILLIS = 3000;
    private static final int WFDCONTROLLER_LINK_INFO_PERIOD_MILLIS = 2000;
    private static final String WFDCONTROLLER_PRE_SHUTDOWN = "android.intent.action.ACTION_PRE_SHUTDOWN";
    private static final int WFDCONTROLLER_SCORE_THRESHOLD1 = 100;
    private static final int WFDCONTROLLER_SCORE_THRESHOLD2 = 80;
    private static final int WFDCONTROLLER_SCORE_THRESHOLD3 = 30;
    private static final int WFDCONTROLLER_SCORE_THRESHOLD4 = 10;
    private static final int WFDCONTROLLER_WFD_STAT_DISCONNECT = 0;
    private static final String WFDCONTROLLER_WFD_STAT_FILE = "/proc/wmt_tm/wfd_stat";
    private static final int WFDCONTROLLER_WFD_STAT_STANDBY = 1;
    private static final int WFDCONTROLLER_WFD_STAT_STREAMING = 2;
    private static final int WFDCONTROLLER_WFD_UPDATE = 0;
    private static final int WFDCONTROLLER_WIFI_APP_SCAN_PERIOD_MILLIS = 100;
    private static final int WFD_BLOCK_MAC_TIME = 15000;
    private static final int WFD_BUILD_CONNECT_DIALOG = 9;
    private static final int WFD_CHANGE_RESOLUTION_DIALOG = 5;
    public static final String WFD_CHANNEL_CONFLICT_OCCURS = "com.mediatek.wifi.p2p.OP.channel";
    public static final String WFD_CLEARMOTION_DIMMED = "com.mediatek.clearmotion.DIMMED_UPDATE";
    private static final int WFD_CONFIRM_CONNECT_DIALOG = 8;
    public static final String WFD_CONNECTION = "com.mediatek.wfd.connection";
    private static final int WFD_HDMI_EXCLUDED_DIALOG_HDMI_UPDATE = 3;
    private static final int WFD_HDMI_EXCLUDED_DIALOG_WFD_UPDATE = 2;
    public static final String WFD_PORTRAIT = "com.mediatek.wfd.portrait";
    private static final int WFD_RECONNECT_DIALOG = 4;
    public static final String WFD_SINK_CHANNEL_CONFLICT_OCCURS = "com.mediatek.wifi.p2p.freq.conflict";
    private static final int WFD_SINK_DISCOVER_RETRY_COUNT = 5;
    private static final int WFD_SINK_DISCOVER_RETRY_DELAY_MILLIS = 100;
    public static final String WFD_SINK_GC_REQUEST_CONNECT = "com.mediatek.wifi.p2p.GO.GCrequest.connect";
    private static final int WFD_SINK_IP_RETRY_COUNT = 50;
    private static final int WFD_SINK_IP_RETRY_DELAY_MILLIS = 1000;
    private static final int WFD_SINK_IP_RETRY_FIRST_DELAY = 300;
    private static final int WFD_SOUND_PATH_DIALOG = 6;
    private static final int WFD_WAIT_CONNECT_DIALOG = 7;
    private static final int WFD_WIFIP2P_EXCLUDED_DIALOG = 1;
    private static final Pattern wfdLinkInfoPattern = null;
    private int WFDCONTROLLER_DISPLAY_NOTIFICATION_TIME;
    private int WFDCONTROLLER_DISPLAY_POWER_SAVING_DELAY;
    private int WFDCONTROLLER_DISPLAY_POWER_SAVING_OPTION;
    private int WFDCONTROLLER_DISPLAY_RESOLUTION;
    private int WFDCONTROLLER_DISPLAY_SECURE_OPTION;
    private int WFDCONTROLLER_DISPLAY_TOAST_TIME;
    private boolean WFDCONTROLLER_QE_ON;
    private boolean WFDCONTROLLER_SQC_INFO_ON;
    private WifiDisplay mAdvertisedDisplay;
    private int mAdvertisedDisplayFlags;
    private int mAdvertisedDisplayHeight;
    private Surface mAdvertisedDisplaySurface;
    private int mAdvertisedDisplayWidth;
    private OnAudioFocusChangeListener mAudioFocusListener;
    private AudioManager mAudioManager;
    private boolean mAutoChannelSelection;
    private boolean mAutoEnableWifi;
    private final ArrayList<WifiP2pDevice> mAvailableWifiDisplayPeers;
    private int mBackupShowTouchVal;
    private String mBlockMac;
    private AlertDialog mBuildConnectDialog;
    private WifiP2pDevice mCancelingDevice;
    private AlertDialog mChangeResolutionDialog;
    private ChannelConflictState mChannelConflictState;
    private AlertDialog mConfirmConnectDialog;
    private WifiP2pDevice mConnectedDevice;
    private WifiP2pGroup mConnectedDeviceGroupInfo;
    private WifiP2pDevice mConnectingDevice;
    private int mConnectionRetriesLeft;
    private final Runnable mConnectionTimeout;
    private final Context mContext;
    private boolean mDRMContent_Mediaplayer;
    private final Runnable mDelayProfiling;
    private WifiP2pDevice mDesiredDevice;
    private WifiP2pDevice mDisconnectingDevice;
    private final Runnable mDiscoverPeers;
    private boolean mDiscoverPeersInProgress;
    private boolean mDisplayApToast;
    private final Runnable mDisplayNotification;
    private final Runnable mDisplayToast;
    private final Runnable mEnableWifiDelay;
    private String mFast_DesiredMac;
    private boolean mFast_NeedFastRtsp;
    private final Runnable mGetSinkIpAddr;
    private AlertDialog mHDMIExcludeDialog_HDMIUpdate;
    private AlertDialog mHDMIExcludeDialog_WfdUpdate;
    private final Handler mHandler;
    private IMtkHdmiManager mHdmiManager;
    private IInputMethodManager mInputMethodManager;
    private boolean mIsConnected_OtherP2p;
    private boolean mIsConnecting_P2p_Rtsp;
    private boolean mIsNeedRotate;
    private boolean mIsWFDConnected;
    private boolean mLastTimeConnected;
    private final Runnable mLatencyInfo;
    View mLatencyPanelView;
    private int mLatencyProfiling;
    private int mLevel;
    private final Listener mListener;
    private int mNetworkId;
    private NetworkInfo mNetworkInfo;
    private boolean mNotiTimerStarted;
    private final NotificationManager mNotificationManager;
    private final ContentObserver mObserver;
    private int mP2pOperFreq;
    private int mPlayerID_Mediaplayer;
    private int mPrevResolution;
    private int mRSSI;
    private boolean mRTSPConnecting;
    private final Runnable mReConnect;
    private WifiP2pDevice mReConnectDevice;
    private AlertDialog mReConnecteDialog;
    private boolean mReConnecting;
    private int mReConnection_Timeout_Remain_Seconds;
    private boolean mReScanning;
    private boolean mReconnectForResolutionChange;
    private RemoteDisplay mRemoteDisplay;
    private boolean mRemoteDisplayConnected;
    private String mRemoteDisplayInterface;
    private int mResolution;
    private final Runnable mRtspSinkTimeout;
    private final Runnable mRtspTimeout;
    private boolean mScanRequested;
    private final Runnable mScanWifiAp;
    private int[] mScore;
    private int mScoreIndex;
    private int mScoreLevel;
    private String mSinkDeviceName;
    private final Runnable mSinkDiscover;
    private int mSinkDiscoverRetryCount;
    private boolean mSinkEnabled;
    private String mSinkIpAddress;
    private int mSinkIpRetryCount;
    private String mSinkMacAddress;
    private WifiP2pGroup mSinkP2pGroup;
    private int mSinkPort;
    private SinkState mSinkState;
    private Surface mSinkSurface;
    private AlertDialog mSoundPathDialog;
    StatusBarManager mStatusBarManager;
    private boolean mStopWifiScan;
    TextView mTextView;
    private WifiP2pDevice mThisDevice;
    private boolean mToastTimerStarted;
    private boolean mUserDecided;
    private AlertDialog mWaitConnectDialog;
    private WakeLock mWakeLock;
    private WakeLock mWakeLockSink;
    private boolean mWfdEnabled;
    private boolean mWfdEnabling;
    WifiP2pWfdInfo mWfdInfo;
    private boolean mWifiApConnected;
    private int mWifiApFreq;
    private String mWifiApSsid;
    private AlertDialog mWifiDirectExcludeDialog;
    private boolean mWifiDisplayCertMode;
    private boolean mWifiDisplayOnSetting;
    private int mWifiDisplayWpsConfig;
    private final Runnable mWifiLinkInfo;
    private WifiLock mWifiLock;
    private WifiManager mWifiManager;
    private int mWifiNetworkId;
    private final Channel mWifiP2pChannel;
    private int mWifiP2pChannelId;
    private boolean mWifiP2pEnabled;
    private final WifiP2pManager mWifiP2pManager;
    private final BroadcastReceiver mWifiP2pReceiver;
    private boolean mWifiPowerSaving;
    private int mWifiRate;
    private int mWifiScore;

    public interface Listener {
        void onDisplayChanged(WifiDisplay wifiDisplay);

        void onDisplayConnected(WifiDisplay wifiDisplay, Surface surface, int i, int i2, int i3);

        void onDisplayConnecting(WifiDisplay wifiDisplay);

        void onDisplayConnectionFailed();

        void onDisplayDisconnected();

        void onDisplayDisconnecting();

        void onDisplaySessionInfo(WifiDisplaySessionInfo wifiDisplaySessionInfo);

        void onFeatureStateChanged(int i);

        void onScanFinished();

        void onScanResults(WifiDisplay[] wifiDisplayArr);

        void onScanStarted();
    }

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    enum ChannelConflictEvt {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.display.WifiDisplayController.ChannelConflictEvt.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.display.WifiDisplayController.ChannelConflictEvt.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.WifiDisplayController.ChannelConflictEvt.<clinit>():void");
        }
    }

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    enum ChannelConflictState {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.display.WifiDisplayController.ChannelConflictState.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.display.WifiDisplayController.ChannelConflictState.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.WifiDisplayController.ChannelConflictState.<clinit>():void");
        }
    }

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    enum SinkState {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.display.WifiDisplayController.SinkState.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.display.WifiDisplayController.SinkState.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.WifiDisplayController.SinkState.<clinit>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.display.WifiDisplayController.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.display.WifiDisplayController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.WifiDisplayController.<clinit>():void");
    }

    public WifiDisplayController(Context context, Handler handler, Listener listener) {
        this.mAvailableWifiDisplayPeers = new ArrayList();
        this.mWifiDisplayWpsConfig = 4;
        this.WFDCONTROLLER_SQC_INFO_ON = false;
        this.WFDCONTROLLER_QE_ON = true;
        this.mAutoChannelSelection = false;
        this.mLatencyProfiling = 2;
        this.mReconnectForResolutionChange = false;
        this.mWifiP2pChannelId = -1;
        this.mWifiApConnected = false;
        this.mWifiApFreq = 0;
        this.mWifiNetworkId = -1;
        this.mWifiApSsid = null;
        this.mLatencyPanelView = null;
        this.mTextView = null;
        this.mScore = new int[4];
        this.mScoreIndex = 0;
        this.mScoreLevel = 0;
        this.mLevel = 0;
        this.mWifiScore = 0;
        this.mWifiRate = 0;
        this.mRSSI = 0;
        this.mStopWifiScan = false;
        this.mWifiPowerSaving = true;
        this.mP2pOperFreq = 0;
        this.mNetworkId = -1;
        this.mSinkEnabled = false;
        this.mDiscoverPeers = new Runnable() {
            public void run() {
                Slog.d(WifiDisplayController.TAG, "mDiscoverPeers, run()");
                WifiDisplayController.this.tryDiscoverPeers();
            }
        };
        this.mConnectionTimeout = new Runnable() {
            public void run() {
                if (WifiDisplayController.this.mConnectingDevice != null && WifiDisplayController.this.mConnectingDevice == WifiDisplayController.this.mDesiredDevice) {
                    Slog.i(WifiDisplayController.TAG, "Timed out waiting for Wifi display connection after 60 seconds: " + WifiDisplayController.this.mConnectingDevice.deviceName);
                    WifiDisplayController.this.handleConnectionFailure(true);
                }
            }
        };
        this.mRtspTimeout = new Runnable() {
            public void run() {
                if (WifiDisplayController.this.mConnectedDevice != null && WifiDisplayController.this.mRemoteDisplay != null && !WifiDisplayController.this.mRemoteDisplayConnected) {
                    Slog.i(WifiDisplayController.TAG, "Timed out waiting for Wifi display RTSP connection after 75 seconds: " + WifiDisplayController.this.mConnectedDevice.deviceName);
                    WifiDisplayController.this.handleConnectionFailure(true);
                }
            }
        };
        this.mWifiP2pReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("android.net.wifi.p2p.STATE_CHANGED")) {
                    boolean enabled = intent.getIntExtra("wifi_p2p_state", 1) == 2;
                    Slog.d(WifiDisplayController.TAG, "Received WIFI_P2P_STATE_CHANGED_ACTION: enabled=" + enabled);
                    WifiDisplayController.this.handleStateChanged(enabled);
                } else if (action.equals("android.net.wifi.p2p.PEERS_CHANGED")) {
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "Received WIFI_P2P_PEERS_CHANGED_ACTION.");
                    }
                    WifiDisplayController.this.handlePeersChanged();
                } else if (action.equals("android.net.wifi.p2p.CONNECTION_STATE_CHANGE")) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    int reason = intent.getIntExtra("reason=", -1);
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "Received WIFI_P2P_CONNECTION_CHANGED_ACTION: networkInfo=" + networkInfo + ", reason = " + reason);
                    } else {
                        Slog.d(WifiDisplayController.TAG, "Received WIFI_P2P_CONNECTION_CHANGED_ACTION: isConnected? " + networkInfo.isConnected() + ", reason = " + reason);
                    }
                    WifiDisplayController.this.updateWifiP2pChannelId(networkInfo.isConnected(), intent);
                    if (SystemProperties.get("ro.mtk_wfd_sink_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && WifiDisplayController.this.mSinkEnabled) {
                        if (reason != -2) {
                            WifiDisplayController.this.handleSinkP2PConnection(networkInfo);
                        }
                        return;
                    }
                    WifiDisplayController.this.handleConnectionChanged(networkInfo, reason);
                    WifiDisplayController.this.mLastTimeConnected = networkInfo.isConnected();
                } else if (action.equals("android.net.wifi.p2p.THIS_DEVICE_CHANGED")) {
                    WifiDisplayController.this.mThisDevice = (WifiP2pDevice) intent.getParcelableExtra("wifiP2pDevice");
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "Received WIFI_P2P_THIS_DEVICE_CHANGED_ACTION: mThisDevice= " + WifiDisplayController.this.mThisDevice);
                    }
                } else if (action.equals(WifiDisplayController.DRM_CONTENT_MEDIAPLAYER)) {
                    WifiDisplayController.this.mDRMContent_Mediaplayer = intent.getBooleanExtra("isPlaying", false);
                    int playerID = intent.getIntExtra("playerId", 0);
                    Slog.i(WifiDisplayController.TAG, "Received DRM_CONTENT_MEDIAPLAYER: isPlaying = " + WifiDisplayController.this.mDRMContent_Mediaplayer + ", player = " + playerID + ", isConnected = " + WifiDisplayController.this.mIsWFDConnected + ", isConnecting = " + WifiDisplayController.this.mRTSPConnecting);
                    if (WifiDisplayController.this.mIsWFDConnected || WifiDisplayController.this.mRTSPConnecting) {
                        if (WifiDisplayController.this.mDRMContent_Mediaplayer) {
                            WifiDisplayController.this.mPlayerID_Mediaplayer = playerID;
                        } else if (WifiDisplayController.this.mPlayerID_Mediaplayer != playerID) {
                            Slog.w(WifiDisplayController.TAG, "player ID doesn't match last time: " + WifiDisplayController.this.mPlayerID_Mediaplayer);
                        }
                    }
                } else if (action.equals("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE")) {
                    int discoveryState = intent.getIntExtra("discoveryState", 1);
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "Received WIFI_P2P_DISCOVERY_CHANGED_ACTION: discoveryState=" + discoveryState);
                    }
                    if (discoveryState == 1) {
                    }
                } else if (action.equals(WifiDisplayController.WFDCONTROLLER_PRE_SHUTDOWN)) {
                    Slog.i(WifiDisplayController.TAG, "Received android.intent.action.ACTION_PRE_SHUTDOWN, do disconnect anyway");
                    if (WifiDisplayController.this.mWifiP2pManager != null) {
                        WifiDisplayController.this.mWifiP2pManager.removeGroup(WifiDisplayController.this.mWifiP2pChannel, null);
                    }
                    if (WifiDisplayController.this.mRemoteDisplay != null) {
                        WifiDisplayController.this.mRemoteDisplay.dispose();
                    }
                } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    boolean updated = false;
                    boolean connected = ((NetworkInfo) intent.getParcelableExtra("networkInfo")).isConnected();
                    if (connected != WifiDisplayController.this.mWifiApConnected) {
                        updated = true;
                    }
                    WifiDisplayController.this.mWifiApConnected = connected;
                    if (WifiDisplayController.this.mWifiApConnected) {
                        WifiInfo conInfo = WifiDisplayController.this.mWifiManager.getConnectionInfo();
                        if (conInfo != null) {
                            if (!(conInfo.getSSID().equals(WifiDisplayController.this.mWifiApSsid) && conInfo.getFrequency() == WifiDisplayController.this.mWifiApFreq && conInfo.getNetworkId() == WifiDisplayController.this.mWifiNetworkId)) {
                                updated = true;
                            }
                            WifiDisplayController.this.mWifiApSsid = conInfo.getSSID();
                            WifiDisplayController.this.mWifiApFreq = conInfo.getFrequency();
                            WifiDisplayController.this.mWifiNetworkId = conInfo.getNetworkId();
                        }
                    } else {
                        WifiDisplayController.this.mWifiApSsid = null;
                        WifiDisplayController.this.mWifiApFreq = 0;
                        WifiDisplayController.this.mWifiNetworkId = -1;
                    }
                    if (updated) {
                        ChannelConflictEvt channelConflictEvt;
                        if (SystemProperties.get("ro.mtk_wfd_sink_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && WifiDisplayController.this.mSinkEnabled) {
                            WifiDisplayController.this.setSinkMiracastMode();
                        }
                        WifiDisplayController wifiDisplayController = WifiDisplayController.this;
                        if (WifiDisplayController.this.mWifiApConnected) {
                            channelConflictEvt = ChannelConflictEvt.EVT_AP_CONNECTED;
                        } else {
                            channelConflictEvt = ChannelConflictEvt.EVT_AP_DISCONNECTED;
                        }
                        wifiDisplayController.handleChannelConflictProcedure(channelConflictEvt);
                    }
                } else if (action.equals(WifiDisplayController.WFD_SINK_GC_REQUEST_CONNECT)) {
                    WifiDisplayController.this.mSinkDeviceName = intent.getStringExtra("deviceName");
                    Slog.i(WifiDisplayController.TAG, "Received WFD_SINK_GC_REQUEST_CONNECT, mSinkDeviceName:" + WifiDisplayController.this.mSinkDeviceName);
                    if (!WifiDisplayController.this.isSinkState(SinkState.SINK_STATE_WAITING_P2P_CONNECTION)) {
                        Slog.d(WifiDisplayController.TAG, "State is wrong. Decline directly ! setGCInviteResult(false)");
                        WifiDisplayController.this.mWifiP2pManager.setGCInviteResult(WifiDisplayController.this.mWifiP2pChannel, false, 0, null);
                    } else if (WifiDisplayController.this.mSinkDeviceName != null) {
                        WifiDisplayController.this.showDialog(8);
                    }
                } else if (action.equals(WifiDisplayController.WFD_CHANNEL_CONFLICT_OCCURS)) {
                    WifiDisplayController.this.mP2pOperFreq = intent.getIntExtra("p2pOperFreq", -1);
                    Slog.i(WifiDisplayController.TAG, "Received WFD_CHANNEL_CONFLICT_OCCURS, p2pOperFreq:" + WifiDisplayController.this.mP2pOperFreq);
                    if (WifiDisplayController.this.mP2pOperFreq != -1) {
                        WifiDisplayController.this.startChannelConflictProcedure();
                    }
                } else if (action.equals(WifiDisplayController.WFD_SINK_CHANNEL_CONFLICT_OCCURS)) {
                    Slog.i(WifiDisplayController.TAG, "Received WFD_SINK_CHANNEL_CONFLICT_OCCURS, mSinkEnabled:" + WifiDisplayController.this.mSinkEnabled + ", apConnected:" + WifiDisplayController.this.mWifiApConnected);
                    if (SystemProperties.get("ro.mtk_wfd_sink_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && WifiDisplayController.this.mSinkEnabled && WifiDisplayController.this.mWifiApConnected) {
                        WifiDisplayController.this.notifyApDisconnected();
                    }
                }
            }
        };
        this.mWifiLinkInfo = new Runnable() {
            public void run() {
                if (WifiDisplayController.this.mConnectedDevice == null) {
                    Slog.e(WifiDisplayController.TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ": ConnectedDevice is null");
                } else if (WifiDisplayController.this.mRemoteDisplay == null) {
                    Slog.e(WifiDisplayController.TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ": RemoteDisplay is null");
                } else {
                    WifiDisplayController.this.mWifiP2pManager.requestWifiP2pLinkInfo(WifiDisplayController.this.mWifiP2pChannel, WifiDisplayController.this.mConnectedDevice.deviceAddress, new WifiP2pLinkInfoListener() {
                        public void onLinkInfoAvailable(WifiP2pLinkInfo status) {
                            if (status == null || status.linkInfo == null) {
                                Slog.e(WifiDisplayController.TAG, "onLinkInfoAvailable() parameter is null!");
                                return;
                            }
                            Matcher match = WifiDisplayController.wfdLinkInfoPattern.matcher(status.linkInfo);
                            if (match.find()) {
                                WifiDisplayController.this.mWifiScore = WifiDisplayController.this.parseDec(match.group(2));
                                WifiDisplayController.this.mRSSI = WifiDisplayController.this.parseFloat(match.group(4));
                                WifiDisplayController.this.mWifiRate = WifiDisplayController.this.parseFloat(match.group(6));
                                WifiDisplayController.this.updateSignalLevel(WifiDisplayController.this.checkInterference(match));
                                return;
                            }
                            Slog.e(WifiDisplayController.TAG, "wfdLinkInfoPattern Malformed Pattern, not match String ");
                        }
                    });
                    WifiDisplayController.this.mHandler.postDelayed(WifiDisplayController.this.mWifiLinkInfo, 2000);
                }
            }
        };
        this.mObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange, Uri uri) {
                boolean z = true;
                if (!selfChange) {
                    boolean z2;
                    WifiDisplayController.this.WFDCONTROLLER_DISPLAY_TOAST_TIME = Global.getInt(WifiDisplayController.this.mContext.getContentResolver(), "wifi_display_display_toast_time", 20);
                    WifiDisplayController.this.WFDCONTROLLER_DISPLAY_NOTIFICATION_TIME = Global.getInt(WifiDisplayController.this.mContext.getContentResolver(), "wifi_display_notification_time", 120);
                    WifiDisplayController.this.WFDCONTROLLER_SQC_INFO_ON = Global.getInt(WifiDisplayController.this.mContext.getContentResolver(), "wifi_display_sqc_info_on", 0) != 0;
                    WifiDisplayController wifiDisplayController = WifiDisplayController.this;
                    if (Global.getInt(WifiDisplayController.this.mContext.getContentResolver(), "wifi_display_qe_on", 0) != 0) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    wifiDisplayController.WFDCONTROLLER_QE_ON = z2;
                    WifiDisplayController wifiDisplayController2 = WifiDisplayController.this;
                    if (Global.getInt(WifiDisplayController.this.mContext.getContentResolver(), "wifi_display_auto_channel_selection", 0) == 0) {
                        z = false;
                    }
                    wifiDisplayController2.mAutoChannelSelection = z;
                    Slog.d(WifiDisplayController.TAG, "onChange(), t_time:" + WifiDisplayController.this.WFDCONTROLLER_DISPLAY_TOAST_TIME + ",n_time:" + WifiDisplayController.this.WFDCONTROLLER_DISPLAY_NOTIFICATION_TIME + ",sqc:" + WifiDisplayController.this.WFDCONTROLLER_SQC_INFO_ON + ",qe:" + WifiDisplayController.this.WFDCONTROLLER_QE_ON + ",autoChannel:" + WifiDisplayController.this.mAutoChannelSelection);
                    if (SystemProperties.get("ro.mtk_wfd_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
                        WifiDisplayController.this.handleResolutionChange();
                        WifiDisplayController.this.handleLatencyProfilingChange();
                        WifiDisplayController.this.handleSecureOptionChange();
                        WifiDisplayController.this.handlePortraitResolutionSupportChange();
                    }
                }
            }
        };
        this.mLatencyInfo = new Runnable() {
            public void run() {
                if (WifiDisplayController.this.mConnectedDevice == null) {
                    Slog.e(WifiDisplayController.TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ": ConnectedDevice is null");
                } else if (WifiDisplayController.this.mRemoteDisplay == null) {
                    Slog.e(WifiDisplayController.TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ": RemoteDisplay is null");
                } else if (WifiDisplayController.this.mLatencyProfiling == 0 || WifiDisplayController.this.mLatencyProfiling == 1 || WifiDisplayController.this.mLatencyProfiling == 3) {
                    int wifiApNum = WifiDisplayController.this.getWifiApNum();
                    String WifiInfo = WifiDisplayController.this.mWifiP2pChannelId + "," + wifiApNum + "," + WifiDisplayController.this.mWifiScore + "," + WifiDisplayController.this.mWifiRate + "," + WifiDisplayController.this.mRSSI;
                    Slog.d(WifiDisplayController.TAG, "WifiInfo:" + WifiInfo);
                    int avgLatency = WifiDisplayController.this.mRemoteDisplay.getWfdParam(5);
                    int sinkFps = WifiDisplayController.this.mRemoteDisplay.getWfdParam(6);
                    String WFDLatency = avgLatency + ",0,0";
                    Slog.d(WifiDisplayController.TAG, "WFDLatency:" + WFDLatency);
                    if (WifiDisplayController.this.mLatencyProfiling == 0 || WifiDisplayController.this.mLatencyProfiling == 1) {
                        Global.putString(WifiDisplayController.this.mContext.getContentResolver(), "wifi_display_wifi_info", WifiInfo);
                        Global.putString(WifiDisplayController.this.mContext.getContentResolver(), "wifi_display_wfd_latency", WFDLatency);
                    } else if (WifiDisplayController.this.mLatencyProfiling == 3) {
                        WifiDisplayController.this.mTextView.setText("AP:" + wifiApNum + "\n" + "S:" + WifiDisplayController.this.mWifiScore + "\n" + "R:" + WifiDisplayController.this.mWifiRate + "\n" + "RS:" + WifiDisplayController.this.mRSSI + "\n" + "AL:" + avgLatency + "\n" + "SF:" + sinkFps + "\n");
                    }
                    WifiDisplayController.this.mHandler.postDelayed(WifiDisplayController.this.mLatencyInfo, 3000);
                } else {
                    Slog.e(WifiDisplayController.TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ": mLatencyProfiling:" + WifiDisplayController.this.mLatencyProfiling);
                }
            }
        };
        this.mScanWifiAp = new Runnable() {
            public void run() {
                if (WifiDisplayController.this.mConnectedDevice == null) {
                    Slog.e(WifiDisplayController.TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ": ConnectedDevice is null");
                } else if (WifiDisplayController.this.mRemoteDisplay == null) {
                    Slog.e(WifiDisplayController.TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ": RemoteDisplay is null");
                } else if (WifiDisplayController.this.mLatencyProfiling == 0 || WifiDisplayController.this.mLatencyProfiling == 1 || WifiDisplayController.this.mLatencyProfiling == 3) {
                    Slog.d(WifiDisplayController.TAG, "call mWifiManager.startScan()");
                    WifiDisplayController.this.mWifiManager.startScan();
                } else {
                    Slog.e(WifiDisplayController.TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ": mLatencyProfiling:" + WifiDisplayController.this.mLatencyProfiling);
                }
            }
        };
        this.mDelayProfiling = new Runnable() {
            public void run() {
                if (WifiDisplayController.this.mLatencyProfiling == 3 && WifiDisplayController.this.mIsWFDConnected) {
                    WifiDisplayController.this.startProfilingInfo();
                }
            }
        };
        this.mDisplayToast = new Runnable() {
            public void run() {
                Slog.d(WifiDisplayController.TAG, "mDisplayToast run()" + WifiDisplayController.this.mLevel);
                Resources mResource = Resources.getSystem();
                if (WifiDisplayController.this.mLevel != 0) {
                    Toast.makeText(WifiDisplayController.this.mContext, mResource.getString(134545537), 0).show();
                }
                WifiDisplayController.this.mToastTimerStarted = false;
            }
        };
        this.mDisplayNotification = new Runnable() {
            public void run() {
                Slog.d(WifiDisplayController.TAG, "mDisplayNotification run()" + WifiDisplayController.this.mLevel);
                if (WifiDisplayController.this.mLevel != 0) {
                    WifiDisplayController.this.showNotification(134545536, 134545538);
                }
                WifiDisplayController.this.mNotiTimerStarted = false;
            }
        };
        this.mReConnect = new Runnable() {
            public void run() {
                Slog.d(WifiDisplayController.TAG, "mReConnect, run()");
                if (WifiDisplayController.this.mReConnectDevice == null) {
                    Slog.w(WifiDisplayController.TAG, "no reconnect device");
                    return;
                }
                for (WifiP2pDevice device : WifiDisplayController.this.mAvailableWifiDisplayPeers) {
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "\t" + WifiDisplayController.describeWifiP2pDevice(device));
                    }
                    if (device.deviceAddress.equals(WifiDisplayController.this.mReConnectDevice.deviceAddress)) {
                        Slog.i(WifiDisplayController.TAG, "connect() in mReConnect. Set mReConnecting as true");
                        WifiDisplayController.this.mReScanning = false;
                        WifiDisplayController.this.mReConnecting = true;
                        WifiDisplayController.this.connect(device);
                        return;
                    }
                }
                WifiDisplayController.this.mReConnection_Timeout_Remain_Seconds = WifiDisplayController.this.mReConnection_Timeout_Remain_Seconds - 1;
                if (WifiDisplayController.this.mReConnection_Timeout_Remain_Seconds > 0) {
                    Slog.i(WifiDisplayController.TAG, "post mReconnect, s:" + WifiDisplayController.this.mReConnection_Timeout_Remain_Seconds);
                    WifiDisplayController.this.mHandler.postDelayed(WifiDisplayController.this.mReConnect, 1000);
                    return;
                }
                Slog.e(WifiDisplayController.TAG, "reconnect timeout!");
                Toast.makeText(WifiDisplayController.this.mContext, 134545542, 0).show();
                WifiDisplayController.this.resetReconnectVariable();
            }
        };
        this.mSinkDiscover = new Runnable() {
            public void run() {
                Slog.d(WifiDisplayController.TAG, "mSinkDiscover run(), count:" + WifiDisplayController.this.mSinkDiscoverRetryCount);
                if (WifiDisplayController.this.isSinkState(SinkState.SINK_STATE_WAITING_P2P_CONNECTION)) {
                    WifiDisplayController.this.startWaitConnection();
                } else {
                    Slog.d(WifiDisplayController.TAG, "mSinkState:(" + WifiDisplayController.this.mSinkState + ") is wrong !");
                }
            }
        };
        this.mGetSinkIpAddr = new Runnable() {
            public void run() {
                Slog.d(WifiDisplayController.TAG, "mGetSinkIpAddr run(), count:" + WifiDisplayController.this.mSinkIpRetryCount);
                if (WifiDisplayController.this.isSinkState(SinkState.SINK_STATE_WIFI_P2P_CONNECTED)) {
                    WifiDisplayController.this.mSinkIpAddress = WifiDisplayController.this.mWifiP2pManager.getPeerIpAddress(WifiDisplayController.this.mSinkMacAddress);
                    WifiDisplayController wifiDisplayController;
                    if (WifiDisplayController.this.mSinkIpAddress != null) {
                        wifiDisplayController = WifiDisplayController.this;
                        wifiDisplayController.mSinkIpAddress = wifiDisplayController.mSinkIpAddress + ":" + WifiDisplayController.this.mSinkPort;
                        Slog.i(WifiDisplayController.TAG, "sink Ip address = " + WifiDisplayController.this.mSinkIpAddress);
                        WifiDisplayController.this.connectRtsp();
                    } else if (WifiDisplayController.this.mSinkIpRetryCount > 0) {
                        wifiDisplayController = WifiDisplayController.this;
                        wifiDisplayController.mSinkIpRetryCount = wifiDisplayController.mSinkIpRetryCount - 1;
                        WifiDisplayController.this.mHandler.postDelayed(WifiDisplayController.this.mGetSinkIpAddr, 1000);
                    } else {
                        Slog.d(WifiDisplayController.TAG, "mGetSinkIpAddr FAIL !!!!!!");
                    }
                    return;
                }
                Slog.d(WifiDisplayController.TAG, "mSinkState:(" + WifiDisplayController.this.mSinkState + ") is wrong !");
            }
        };
        this.mRtspSinkTimeout = new Runnable() {
            public void run() {
                Slog.d(WifiDisplayController.TAG, "mRtspSinkTimeout, run()");
                WifiDisplayController.this.disconnectWfdSink();
            }
        };
        this.mAudioFocusListener = new OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                Slog.e(WifiDisplayController.TAG, "onAudioFocusChange(), focus:" + focusChange);
                switch (focusChange) {
                }
            }
        };
        this.mEnableWifiDelay = new Runnable() {
            public void run() {
                Slog.d(WifiDisplayController.TAG, "Enable wifi automatically.");
                WifiDisplayController.this.mAutoEnableWifi = true;
                int wifiApState = WifiDisplayController.this.mWifiManager.getWifiApState();
                if (wifiApState == 12 || wifiApState == 13) {
                    ((ConnectivityManager) WifiDisplayController.this.mContext.getSystemService("connectivity")).stopTethering(0);
                }
                WifiDisplayController.this.mWifiManager.setWifiEnabled(true);
            }
        };
        this.mContext = context;
        this.mHandler = handler;
        this.mListener = listener;
        this.mWifiP2pManager = (WifiP2pManager) context.getSystemService("wifip2p");
        this.mWifiP2pChannel = this.mWifiP2pManager.initialize(context, handler.getLooper(), null);
        getWifiLock();
        this.mWfdInfo = new WifiP2pWfdInfo();
        this.mHdmiManager = Stub.asInterface(ServiceManager.getService("mtkhdmi"));
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.p2p.STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.p2p.PEERS_CHANGED");
        intentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.p2p.THIS_DEVICE_CHANGED");
        intentFilter.addAction(DRM_CONTENT_MEDIAPLAYER);
        intentFilter.addAction("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE");
        intentFilter.addAction(WFDCONTROLLER_PRE_SHUTDOWN);
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction(WFD_SINK_GC_REQUEST_CONNECT);
        intentFilter.addAction(WFD_CHANNEL_CONFLICT_OCCURS);
        intentFilter.addAction(WFD_SINK_CHANNEL_CONFLICT_OCCURS);
        context.registerReceiver(this.mWifiP2pReceiver, intentFilter, null, this.mHandler);
        ContentObserver settingsObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange, Uri uri) {
                if (!selfChange) {
                    WifiDisplayController.this.updateSettings();
                }
            }
        };
        ContentResolver resolver = this.mContext.getContentResolver();
        resolver.registerContentObserver(Global.getUriFor("wifi_display_on"), false, settingsObserver);
        resolver.registerContentObserver(Global.getUriFor("wifi_display_certification_on"), false, settingsObserver);
        resolver.registerContentObserver(Global.getUriFor("wifi_display_wps_config"), false, settingsObserver);
        updateSettings();
        resolver.registerContentObserver(System.getUriFor("hdmi_enable_status"), false, new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange, Uri uri) {
                WifiDisplayController.this.updateSettingsHDMI();
            }
        });
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRealMetrics(dm);
        Slog.i(TAG, "RealMetrics, Width = " + dm.widthPixels + ", Height = " + dm.heightPixels);
        if (dm.widthPixels < dm.heightPixels) {
            this.mIsNeedRotate = true;
        }
        registerEMObserver(dm.widthPixels, dm.heightPixels);
        this.mNotificationManager = (NotificationManager) context.getSystemService(NotificationManagerService.NOTIFICATON_TITLE_NAME);
        actionAtDisconnected(null);
        updateWfdStatFile(0);
        PowerManager pm = (PowerManager) context.getSystemService("power");
        this.mWakeLock = pm.newWakeLock(26, "UIBC Source");
        this.mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
        this.mWakeLockSink = pm.newWakeLock(26, "WFD Sink");
    }

    private void updateSettings() {
        boolean z;
        ContentResolver resolver = this.mContext.getContentResolver();
        if (Global.getInt(resolver, "wifi_display_on", 0) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mWifiDisplayOnSetting = z;
        if (Global.getInt(resolver, "wifi_display_certification_on", 0) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mWifiDisplayCertMode = z;
        this.mWifiDisplayWpsConfig = 4;
        if (this.mWifiDisplayCertMode) {
            this.mWifiDisplayWpsConfig = Global.getInt(resolver, "wifi_display_wps_config", 4);
        }
        loadDebugLevel();
        boolean HDMIOn = false;
        if (SystemProperties.get("ro.mtk_hdmi_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            HDMIOn = System.getInt(resolver, "hdmi_enable_status", 1) != 0;
        }
        if (this.mWifiDisplayOnSetting && HDMIOn) {
            dialogWfdHdmiConflict(0);
        } else {
            enableWifiDisplay();
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println("mWifiDisplayOnSetting=" + this.mWifiDisplayOnSetting);
        pw.println("mWifiP2pEnabled=" + this.mWifiP2pEnabled);
        pw.println("mWfdEnabled=" + this.mWfdEnabled);
        pw.println("mWfdEnabling=" + this.mWfdEnabling);
        pw.println("mNetworkInfo=" + this.mNetworkInfo);
        pw.println("mScanRequested=" + this.mScanRequested);
        pw.println("mDiscoverPeersInProgress=" + this.mDiscoverPeersInProgress);
        pw.println("mDesiredDevice=" + describeWifiP2pDevice(this.mDesiredDevice));
        pw.println("mConnectingDisplay=" + describeWifiP2pDevice(this.mConnectingDevice));
        pw.println("mDisconnectingDisplay=" + describeWifiP2pDevice(this.mDisconnectingDevice));
        pw.println("mCancelingDisplay=" + describeWifiP2pDevice(this.mCancelingDevice));
        pw.println("mConnectedDevice=" + describeWifiP2pDevice(this.mConnectedDevice));
        pw.println("mConnectionRetriesLeft=" + this.mConnectionRetriesLeft);
        pw.println("mRemoteDisplay=" + this.mRemoteDisplay);
        pw.println("mRemoteDisplayInterface=" + this.mRemoteDisplayInterface);
        pw.println("mRemoteDisplayConnected=" + this.mRemoteDisplayConnected);
        pw.println("mAdvertisedDisplay=" + this.mAdvertisedDisplay);
        pw.println("mAdvertisedDisplaySurface=" + this.mAdvertisedDisplaySurface);
        pw.println("mAdvertisedDisplayWidth=" + this.mAdvertisedDisplayWidth);
        pw.println("mAdvertisedDisplayHeight=" + this.mAdvertisedDisplayHeight);
        pw.println("mAdvertisedDisplayFlags=" + this.mAdvertisedDisplayFlags);
        pw.println("mBackupShowTouchVal=" + this.mBackupShowTouchVal);
        pw.println("mFast_NeedFastRtsp=" + this.mFast_NeedFastRtsp);
        pw.println("mFast_DesiredMac=" + this.mFast_DesiredMac);
        pw.println("mIsNeedRotate=" + this.mIsNeedRotate);
        pw.println("mIsConnected_OtherP2p=" + this.mIsConnected_OtherP2p);
        pw.println("mIsConnecting_P2p_Rtsp=" + this.mIsConnecting_P2p_Rtsp);
        pw.println("mIsWFDConnected=" + this.mIsWFDConnected);
        pw.println("mDRMContent_Mediaplayer=" + this.mDRMContent_Mediaplayer);
        pw.println("mPlayerID_Mediaplayer=" + this.mPlayerID_Mediaplayer);
        pw.println("mAvailableWifiDisplayPeers: size=" + this.mAvailableWifiDisplayPeers.size());
        for (WifiP2pDevice device : this.mAvailableWifiDisplayPeers) {
            pw.println("  " + describeWifiP2pDevice(device));
        }
    }

    public void requestStartScan() {
        Slog.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ",mSinkEnabled:" + this.mSinkEnabled);
        if (!((SystemProperties.get("ro.mtk_wfd_sink_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && this.mSinkEnabled) || this.mScanRequested)) {
            this.mScanRequested = true;
            updateScanState();
        }
    }

    public void requestStopScan() {
        if (this.mScanRequested) {
            this.mScanRequested = false;
            updateScanState();
        }
    }

    public void requestConnect(String address) {
        Slog.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ", address = " + address);
        resetReconnectVariable();
        if (DEBUG) {
            Slog.d(TAG, "mAvailableWifiDisplayPeers dump:");
        }
        for (WifiP2pDevice device : this.mAvailableWifiDisplayPeers) {
            if (DEBUG) {
                Slog.d(TAG, "\t" + describeWifiP2pDevice(device));
            }
            if (device.deviceAddress.equals(address)) {
                if (this.mIsConnected_OtherP2p) {
                    Slog.i(TAG, "OtherP2P is connected! Show dialog!");
                    advertiseDisplay(createWifiDisplay(device), null, 0, 0, 0);
                    showDialog(1);
                    return;
                }
                connect(device);
            }
        }
    }

    public void requestPause() {
        if (this.mRemoteDisplay != null) {
            this.mRemoteDisplay.pause();
        }
    }

    public void requestResume() {
        if (this.mRemoteDisplay != null) {
            this.mRemoteDisplay.resume();
        }
    }

    public void requestDisconnect() {
        Slog.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
        disconnect();
        resetReconnectVariable();
    }

    private void updateWfdEnableState() {
        Slog.i(TAG, "updateWfdEnableState(), mWifiDisplayOnSetting:" + this.mWifiDisplayOnSetting + ", mWifiP2pEnabled:" + this.mWifiP2pEnabled);
        if (this.mWifiDisplayOnSetting && this.mWifiP2pEnabled) {
            this.mSinkEnabled = false;
            if (!this.mWfdEnabled && !this.mWfdEnabling) {
                this.mWfdEnabling = true;
                updateWfdInfo(true);
                if (SystemProperties.get("ro.mtk_wfd_hdcp_tx_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) || SystemProperties.get("ro.mtk_dx_hdcp_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
                    updateWifiPowerSavingMode(false);
                    return;
                }
                return;
            }
            return;
        }
        updateWfdInfo(false);
        if (SystemProperties.get("ro.mtk_wfd_hdcp_tx_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) || SystemProperties.get("ro.mtk_dx_hdcp_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            updateWifiPowerSavingMode(true);
        }
        this.mWfdEnabling = false;
        this.mWfdEnabled = false;
        reportFeatureState();
        updateScanState();
        disconnect();
        dismissDialog();
        this.mBlockMac = null;
    }

    private void resetWfdInfo() {
        this.mWfdInfo.setWfdEnabled(false);
        this.mWfdInfo.setDeviceType(0);
        this.mWfdInfo.setSessionAvailable(false);
        this.mWfdInfo.setUibcSupported(false);
        this.mWfdInfo.setContentProtected(false);
    }

    private void updateWfdInfo(boolean enable) {
        boolean z = true;
        Slog.i(TAG, "updateWfdInfo(), enable:" + enable + ",mWfdEnabling:" + this.mWfdEnabling);
        resetWfdInfo();
        if (enable) {
            this.mWfdInfo.setWfdEnabled(true);
            if (SystemProperties.get("ro.mtk_wfd_sink_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && this.mSinkEnabled) {
                this.mWfdInfo.setDeviceType(1);
            } else {
                this.mWfdInfo.setDeviceType(0);
            }
            Slog.i(TAG, "Set session available as true");
            this.mWfdInfo.setSessionAvailable(true);
            this.mWfdInfo.setControlPort(DEFAULT_CONTROL_PORT);
            this.mWfdInfo.setMaxThroughput(50);
            if (!SystemProperties.get("ro.mtk_wfd_sink_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) || !this.mSinkEnabled) {
                this.mWfdInfo.setUibcSupported(true);
            } else if (SystemProperties.get("ro.mtk_wfd_sink_uibc_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
                this.mWfdInfo.setUibcSupported(true);
            } else {
                this.mWfdInfo.setUibcSupported(false);
            }
            if (SystemProperties.get("ro.mtk_wfd_hdcp_tx_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) || SystemProperties.get("ro.mtk_dx_hdcp_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) || SystemProperties.get("ro.mtk_wfd_hdcp_rx_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
                this.mWfdInfo.setContentProtected(true);
            }
            String str = TAG;
            StringBuilder append = new StringBuilder().append("HDCP Tx support? ");
            if (!SystemProperties.get("ro.mtk_wfd_hdcp_tx_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
                z = SystemProperties.get("ro.mtk_dx_hdcp_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
            }
            Slog.i(str, append.append(z).append(", our wfd info: ").append(this.mWfdInfo).toString());
            Slog.i(TAG, "HDCP Rx support? " + SystemProperties.get("ro.mtk_wfd_hdcp_rx_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) + ", our wfd info: " + this.mWfdInfo);
            if (this.mWfdEnabling) {
                this.mWifiP2pManager.setWFDInfo(this.mWifiP2pChannel, this.mWfdInfo, new ActionListener() {
                    public void onSuccess() {
                        Slog.d(WifiDisplayController.TAG, "Successfully set WFD info.");
                        if (WifiDisplayController.this.mWfdEnabling) {
                            WifiDisplayController.this.mWfdEnabling = false;
                            WifiDisplayController.this.mWfdEnabled = true;
                            WifiDisplayController.this.reportFeatureState();
                            if (SystemProperties.get("ro.mtk_wfd_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && WifiDisplayController.this.mAutoEnableWifi) {
                                WifiDisplayController.this.mAutoEnableWifi = false;
                                Slog.d(WifiDisplayController.TAG, "scan after enable wifi automatically.");
                            }
                            WifiDisplayController.this.updateScanState();
                        }
                    }

                    public void onFailure(int reason) {
                        Slog.d(WifiDisplayController.TAG, "Failed to set WFD info with reason " + reason + ".");
                        WifiDisplayController.this.mWfdEnabling = false;
                    }
                });
                return;
            } else {
                this.mWifiP2pManager.setWFDInfo(this.mWifiP2pChannel, this.mWfdInfo, null);
                return;
            }
        }
        this.mWfdInfo.setWfdEnabled(false);
        this.mWifiP2pManager.setWFDInfo(this.mWifiP2pChannel, this.mWfdInfo, new ActionListener() {
            public void onSuccess() {
                if (WifiDisplayController.DEBUG) {
                    Slog.d(WifiDisplayController.TAG, "Successfully set WFD info.");
                }
            }

            public void onFailure(int reason) {
                if (WifiDisplayController.DEBUG) {
                    Slog.d(WifiDisplayController.TAG, "Failed to set WFD info with reason " + reason + ".");
                }
            }
        });
    }

    private void reportFeatureState() {
        final int featureState = computeFeatureState();
        Slog.d(TAG, "reportFeatureState(), featureState = " + featureState);
        this.mHandler.post(new Runnable() {
            public void run() {
                Slog.d(WifiDisplayController.TAG, "callback onFeatureStateChanged(): featureState = " + featureState);
                WifiDisplayController.this.mListener.onFeatureStateChanged(featureState);
            }
        });
    }

    private int computeFeatureState() {
        if (this.mWifiP2pEnabled) {
            int i;
            if (this.mWifiDisplayOnSetting) {
                i = 3;
            } else {
                i = 2;
            }
            return i;
        }
        if (this.mWifiDisplayOnSetting) {
            Slog.d(TAG, "Wifi p2p is disabled, update WIFI_DISPLAY_ON as false.");
            Global.putInt(this.mContext.getContentResolver(), "wifi_display_on", 0);
            this.mWifiDisplayOnSetting = false;
        }
        return 1;
    }

    private void updateScanState() {
        Slog.i(TAG, "updateScanState(), mSinkEnabled:" + this.mSinkEnabled + "mDiscoverPeersInProgress:" + this.mDiscoverPeersInProgress);
        if (!SystemProperties.get("ro.mtk_wfd_sink_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) || !this.mSinkEnabled) {
            if ((this.mScanRequested && this.mWfdEnabled && this.mDesiredDevice == null) || this.mReScanning) {
                if (this.mDiscoverPeersInProgress) {
                    this.mHandler.removeCallbacks(this.mDiscoverPeers);
                    this.mHandler.postDelayed(this.mDiscoverPeers, 10000);
                } else {
                    Slog.i(TAG, "Starting Wifi display scan.");
                    this.mDiscoverPeersInProgress = true;
                    handleScanStarted();
                    tryDiscoverPeers();
                }
            } else if (this.mDiscoverPeersInProgress) {
                this.mHandler.removeCallbacks(this.mDiscoverPeers);
                if (this.mDesiredDevice == null || this.mDesiredDevice == this.mConnectedDevice) {
                    Slog.i(TAG, "Stopping Wifi display scan.");
                    this.mDiscoverPeersInProgress = false;
                    stopPeerDiscovery();
                    handleScanFinished();
                }
            }
        }
    }

    private void discoverPeers() {
        if (!this.mDiscoverPeersInProgress) {
            Slog.i(TAG, "Starting Wifi display scan.");
            this.mDiscoverPeersInProgress = true;
            handleScanStarted();
            tryDiscoverPeers();
        }
    }

    private void tryDiscoverPeers() {
        Slog.d(TAG, "tryDiscoverPeers()");
        this.mWifiP2pManager.discoverPeers(this.mWifiP2pChannel, new ActionListener() {
            public void onSuccess() {
                if (WifiDisplayController.DEBUG) {
                    Slog.d(WifiDisplayController.TAG, "Discover peers succeeded.  Requesting peers now.");
                }
                if (WifiDisplayController.this.mDiscoverPeersInProgress) {
                    WifiDisplayController.this.requestPeers();
                }
            }

            public void onFailure(int reason) {
                if (WifiDisplayController.DEBUG) {
                    Slog.d(WifiDisplayController.TAG, "Discover peers failed with reason " + reason + ".");
                }
            }
        });
        if (this.mHandler.hasCallbacks(this.mDiscoverPeers)) {
            this.mHandler.removeCallbacks(this.mDiscoverPeers);
        }
        if (this.mReScanning) {
            Slog.d(TAG, "mReScanning is true. post mDiscoverPeers every 2s");
            this.mHandler.postDelayed(this.mDiscoverPeers, 2000);
            return;
        }
        this.mHandler.postDelayed(this.mDiscoverPeers, 10000);
    }

    private void stopPeerDiscovery() {
        this.mWifiP2pManager.stopPeerDiscovery(this.mWifiP2pChannel, new ActionListener() {
            public void onSuccess() {
                if (WifiDisplayController.DEBUG) {
                    Slog.d(WifiDisplayController.TAG, "Stop peer discovery succeeded.");
                }
            }

            public void onFailure(int reason) {
                if (WifiDisplayController.DEBUG) {
                    Slog.d(WifiDisplayController.TAG, "Stop peer discovery failed with reason " + reason + ".");
                }
            }
        });
    }

    private void requestPeers() {
        this.mWifiP2pManager.requestPeers(this.mWifiP2pChannel, new PeerListListener() {
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                if (!SystemProperties.get("ro.mtk_wfd_sink_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) || !WifiDisplayController.this.mSinkEnabled) {
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "Received list of peers. mDiscoverPeersInProgress=" + WifiDisplayController.this.mDiscoverPeersInProgress);
                    }
                    WifiDisplayController.this.mAvailableWifiDisplayPeers.clear();
                    for (WifiP2pDevice device : peers.getDeviceList()) {
                        if (WifiDisplayController.DEBUG) {
                            Slog.d(WifiDisplayController.TAG, "  " + WifiDisplayController.describeWifiP2pDevice(device));
                        }
                        if (WifiDisplayController.this.mConnectedDevice != null && WifiDisplayController.this.mConnectedDevice.deviceAddress.equals(device.deviceAddress)) {
                            WifiDisplayController.this.mAvailableWifiDisplayPeers.add(device);
                        } else if (WifiDisplayController.this.mConnectingDevice != null && WifiDisplayController.this.mConnectingDevice.deviceAddress.equals(device.deviceAddress)) {
                            WifiDisplayController.this.mAvailableWifiDisplayPeers.add(device);
                        } else if (WifiDisplayController.this.mBlockMac != null && device.deviceAddress.equals(WifiDisplayController.this.mBlockMac)) {
                            Slog.i(WifiDisplayController.TAG, "Block scan result on block mac:" + WifiDisplayController.this.mBlockMac);
                        } else if (WifiDisplayController.isWifiDisplay(device)) {
                            WifiDisplayController.this.mAvailableWifiDisplayPeers.add(device);
                        }
                    }
                    WifiDisplayController.this.handleScanResults();
                }
            }
        });
    }

    private void handleScanStarted() {
        this.mHandler.post(new Runnable() {
            public void run() {
                Slog.d(WifiDisplayController.TAG, "callback onScanStarted()");
                WifiDisplayController.this.mListener.onScanStarted();
            }
        });
    }

    private void handleScanResults() {
        final int count = this.mAvailableWifiDisplayPeers.size();
        final WifiDisplay[] displays = (WifiDisplay[]) WifiDisplay.CREATOR.newArray(count);
        for (int i = 0; i < count; i++) {
            WifiP2pDevice device = (WifiP2pDevice) this.mAvailableWifiDisplayPeers.get(i);
            displays[i] = createWifiDisplay(device);
            updateDesiredDevice(device);
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                Slog.d(WifiDisplayController.TAG, "callback onScanResults(), count = " + count);
                if (WifiDisplayController.DEBUG) {
                    for (int i = 0; i < count; i++) {
                        Slog.d(WifiDisplayController.TAG, "\t" + displays[i].getDeviceName() + ": " + displays[i].getDeviceAddress());
                    }
                }
                WifiDisplayController.this.mListener.onScanResults(displays);
            }
        });
    }

    private void handleScanFinished() {
        this.mHandler.post(new Runnable() {
            public void run() {
                WifiDisplayController.this.mListener.onScanFinished();
            }
        });
    }

    private void updateDesiredDevice(WifiP2pDevice device) {
        String address = device.deviceAddress;
        if (this.mDesiredDevice != null && this.mDesiredDevice.deviceAddress.equals(address)) {
            if (DEBUG) {
                Slog.d(TAG, "updateDesiredDevice: new information " + describeWifiP2pDevice(device));
            }
            this.mDesiredDevice.update(device);
            if (this.mAdvertisedDisplay != null && this.mAdvertisedDisplay.getDeviceAddress().equals(address)) {
                readvertiseDisplay(createWifiDisplay(this.mDesiredDevice));
            }
        }
    }

    private void connect(WifiP2pDevice device) {
        Slog.i(TAG, "connect: device name = " + device.deviceName);
        if (this.mDesiredDevice != null && !this.mDesiredDevice.deviceAddress.equals(device.deviceAddress)) {
            if (DEBUG) {
                Slog.d(TAG, "connect: nothing to do, already connecting to " + describeWifiP2pDevice(this.mDesiredDevice));
            }
        } else if (this.mDesiredDevice != null && this.mDesiredDevice.deviceAddress.equals(device.deviceAddress)) {
            if (DEBUG) {
                Slog.d(TAG, "connect: connecting to the same dongle already " + describeWifiP2pDevice(this.mDesiredDevice));
            }
        } else if (this.mConnectedDevice != null && !this.mConnectedDevice.deviceAddress.equals(device.deviceAddress) && this.mDesiredDevice == null) {
            if (DEBUG) {
                Slog.d(TAG, "connect: nothing to do, already connected to " + describeWifiP2pDevice(device) + " and not part way through " + "connecting to a different device.");
            }
        } else if (this.mWfdEnabled) {
            this.mDesiredDevice = device;
            this.mConnectionRetriesLeft = 0;
            updateConnection();
        } else {
            Slog.i(TAG, "Ignoring request to connect to Wifi display because the  feature is currently disabled: " + device.deviceName);
        }
    }

    private void disconnect() {
        Slog.i(TAG, "disconnect, mRemoteDisplayInterface = " + this.mRemoteDisplayInterface);
        if (SystemProperties.get("ro.mtk_wfd_sink_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && this.mSinkEnabled) {
            disconnectWfdSink();
            return;
        }
        this.mDesiredDevice = null;
        updateWfdStatFile(0);
        if (this.mConnectedDevice != null) {
            this.mReConnectDevice = this.mConnectedDevice;
        }
        if (!(this.mConnectingDevice == null && this.mConnectedDevice == null)) {
            removeSpecificPersistentGroup();
        }
        updateConnection();
    }

    private void retryConnection() {
        this.mDesiredDevice = new WifiP2pDevice(this.mDesiredDevice);
        updateConnection();
    }

    private void updateConnection() {
        updateScanState();
        if (!(this.mRemoteDisplay == null || this.mConnectedDevice == this.mDesiredDevice) || this.mIsConnecting_P2p_Rtsp) {
            String localInterface = this.mRemoteDisplayInterface != null ? this.mRemoteDisplayInterface : "localhost";
            String localDeviceName = this.mConnectedDevice != null ? this.mConnectedDevice.deviceName : this.mConnectingDevice != null ? this.mConnectingDevice.deviceName : "N/A";
            Slog.i(TAG, "Stopped listening for RTSP connection on " + localInterface + " from Wifi display : " + localDeviceName);
            this.mIsConnected_OtherP2p = false;
            this.mIsConnecting_P2p_Rtsp = false;
            Slog.i(TAG, "\tbefore dispose() ---> ");
            this.mListener.onDisplayDisconnecting();
            this.mRemoteDisplay.dispose();
            Slog.i(TAG, "\t<--- after dispose()");
            this.mRemoteDisplay = null;
            this.mRemoteDisplayInterface = null;
            this.mRemoteDisplayConnected = false;
            this.mHandler.removeCallbacks(this.mRtspTimeout);
            this.mWifiP2pManager.setMiracastMode(0);
            unadvertiseDisplay();
        }
        if (this.mDisconnectingDevice == null) {
            final WifiP2pDevice oldDevice;
            if (this.mConnectedDevice != null && this.mConnectedDevice != this.mDesiredDevice) {
                Slog.i(TAG, "Disconnecting from Wifi display: " + this.mConnectedDevice.deviceName);
                this.mDisconnectingDevice = this.mConnectedDevice;
                this.mConnectedDevice = null;
                this.mConnectedDeviceGroupInfo = null;
                unadvertiseDisplay();
                oldDevice = this.mDisconnectingDevice;
                this.mWifiP2pManager.removeGroup(this.mWifiP2pChannel, new ActionListener() {
                    public void onSuccess() {
                        Slog.i(WifiDisplayController.TAG, "Disconnected from Wifi display: " + oldDevice.deviceName);
                        WifiDisplayController.this.mHandler.postDelayed(new Runnable() {
                            public void run() {
                                WifiDisplayController.this.discoverPeers();
                                Slog.i(WifiDisplayController.TAG, "requestScan when Disconnected");
                            }
                        }, 500);
                        next();
                    }

                    public void onFailure(int reason) {
                        Slog.i(WifiDisplayController.TAG, "Failed to disconnect from Wifi display: " + oldDevice.deviceName + ", reason=" + reason);
                        next();
                    }

                    private void next() {
                        if (WifiDisplayController.this.mDisconnectingDevice == oldDevice) {
                            WifiDisplayController.this.mDisconnectingDevice = null;
                            if (WifiDisplayController.this.mRemoteDisplay != null) {
                                WifiDisplayController.this.mIsConnecting_P2p_Rtsp = true;
                            }
                            WifiDisplayController.this.updateConnection();
                        }
                    }
                });
            } else if (this.mCancelingDevice == null) {
                if (this.mConnectingDevice != null && this.mConnectingDevice != this.mDesiredDevice) {
                    Slog.i(TAG, "Canceling connection to Wifi display: " + this.mConnectingDevice.deviceName);
                    this.mCancelingDevice = this.mConnectingDevice;
                    this.mConnectingDevice = null;
                    unadvertiseDisplay();
                    this.mHandler.removeCallbacks(this.mConnectionTimeout);
                    oldDevice = this.mCancelingDevice;
                    this.mWifiP2pManager.cancelConnect(this.mWifiP2pChannel, new ActionListener() {
                        public void onSuccess() {
                            Slog.i(WifiDisplayController.TAG, "Canceled connection to Wifi display: " + oldDevice.deviceName);
                            next();
                        }

                        public void onFailure(int reason) {
                            Slog.i(WifiDisplayController.TAG, "Failed to cancel connection to Wifi display: " + oldDevice.deviceName + ", reason=" + reason + ". Do removeGroup()");
                            WifiDisplayController.this.mWifiP2pManager.removeGroup(WifiDisplayController.this.mWifiP2pChannel, null);
                            next();
                        }

                        private void next() {
                            if (WifiDisplayController.this.mCancelingDevice == oldDevice) {
                                WifiDisplayController.this.mCancelingDevice = null;
                                if (WifiDisplayController.this.mRemoteDisplay != null) {
                                    WifiDisplayController.this.mIsConnecting_P2p_Rtsp = true;
                                }
                                WifiDisplayController.this.updateConnection();
                            }
                        }
                    });
                } else if (this.mDesiredDevice == null) {
                    if (this.mWifiDisplayCertMode) {
                        this.mListener.onDisplaySessionInfo(getSessionInfo(this.mConnectedDeviceGroupInfo, 0));
                    }
                    unadvertiseDisplay();
                } else if (this.mConnectedDevice == null && this.mConnectingDevice == null) {
                    Slog.i(TAG, "Connecting to Wifi display: " + this.mDesiredDevice.deviceName);
                    this.mConnectingDevice = this.mDesiredDevice;
                    WifiP2pConfig config = new WifiP2pConfig();
                    WpsInfo wps = new WpsInfo();
                    if (this.mWifiDisplayWpsConfig != 4) {
                        wps.setup = this.mWifiDisplayWpsConfig;
                    } else if (this.mConnectingDevice.wpsPbcSupported()) {
                        wps.setup = 0;
                    } else if (this.mConnectingDevice.wpsDisplaySupported()) {
                        wps.setup = 2;
                    } else if (this.mConnectingDevice.wpsKeypadSupported()) {
                        wps.setup = 1;
                    } else {
                        wps.setup = 0;
                    }
                    config.wps = wps;
                    config.deviceAddress = this.mConnectingDevice.deviceAddress;
                    Slog.i(TAG, "I want to be GO.");
                    config.groupOwnerIntent = Integer.valueOf(SystemProperties.get("wfd.source.go_intent", String.valueOf(14))).intValue();
                    Slog.i(TAG, "Source go_intent:" + config.groupOwnerIntent);
                    advertiseDisplay(createWifiDisplay(this.mConnectingDevice), null, 0, 0, 0);
                    updateWfdInfo(true);
                    setAutoChannelSelection();
                    enterCCState(ChannelConflictState.STATE_IDLE);
                    this.mWifiP2pManager.setMiracastMode(1);
                    stopWifiScan(true);
                    final WifiP2pDevice newDevice = this.mDesiredDevice;
                    this.mWifiP2pManager.connect(this.mWifiP2pChannel, config, new ActionListener() {
                        public void onSuccess() {
                            Slog.i(WifiDisplayController.TAG, "Initiated connection to Wifi display: " + newDevice.deviceName);
                            WifiDisplayController.this.mHandler.postDelayed(WifiDisplayController.this.mConnectionTimeout, 60000);
                        }

                        public void onFailure(int reason) {
                            if (WifiDisplayController.this.mConnectingDevice == newDevice) {
                                Slog.i(WifiDisplayController.TAG, "Failed to initiate connection to Wifi display: " + newDevice.deviceName + ", reason=" + reason);
                                WifiDisplayController.this.mConnectingDevice = null;
                                WifiDisplayController.this.handleConnectionFailure(false);
                            }
                        }
                    });
                    this.mRTSPConnecting = true;
                    oldDevice = this.mConnectingDevice;
                    String iface = "127.0.0.1:" + getPortNumber(this.mConnectingDevice);
                    this.mRemoteDisplayInterface = iface;
                    Slog.i(TAG, "Listening for RTSP connection on " + iface + " from Wifi display: " + this.mConnectingDevice.deviceName + " , Speed-Up rtsp setup, DRM Content isPlaying = " + this.mDRMContent_Mediaplayer);
                    this.mRemoteDisplay = RemoteDisplay.listen(iface, new android.media.RemoteDisplay.Listener() {
                        public void onDisplayConnected(Surface surface, int width, int height, int flags, int session) {
                            if (WifiDisplayController.this.mConnectingDevice != null) {
                                WifiDisplayController.this.mConnectedDevice = WifiDisplayController.this.mConnectingDevice;
                            }
                            if ((WifiDisplayController.this.mConnectedDevice != oldDevice || WifiDisplayController.this.mRemoteDisplayConnected) && WifiDisplayController.DEBUG) {
                                Slog.e(WifiDisplayController.TAG, "!!RTSP connected condition GOT Trobule:\nmConnectedDevice: " + WifiDisplayController.this.mConnectedDevice + "\noldDevice: " + oldDevice + "\nmRemoteDisplayConnected: " + WifiDisplayController.this.mRemoteDisplayConnected);
                            }
                            if (!(WifiDisplayController.this.mConnectedDevice == null || oldDevice == null || !WifiDisplayController.this.mConnectedDevice.deviceAddress.equals(oldDevice.deviceAddress) || WifiDisplayController.this.mRemoteDisplayConnected)) {
                                Slog.i(WifiDisplayController.TAG, "Opened RTSP connection with Wifi display: " + WifiDisplayController.this.mConnectedDevice.deviceName);
                                WifiDisplayController.this.mRemoteDisplayConnected = true;
                                WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspTimeout);
                                if (WifiDisplayController.this.mWifiDisplayCertMode) {
                                    WifiDisplayController.this.mListener.onDisplaySessionInfo(WifiDisplayController.this.getSessionInfo(WifiDisplayController.this.mConnectedDeviceGroupInfo, session));
                                }
                                WifiDisplayController.this.updateWfdStatFile(2);
                                WifiDisplayController.this.advertiseDisplay(WifiDisplayController.createWifiDisplay(WifiDisplayController.this.mConnectedDevice), surface, width, height, flags);
                            }
                            WifiDisplayController.this.mRTSPConnecting = false;
                        }

                        public void onDisplayDisconnected() {
                            if (WifiDisplayController.this.mConnectedDevice == oldDevice) {
                                Slog.i(WifiDisplayController.TAG, "Closed RTSP connection with Wifi display: " + WifiDisplayController.this.mConnectedDevice.deviceName);
                                WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspTimeout);
                                WifiDisplayController.this.disconnect();
                            }
                            WifiDisplayController.this.mRTSPConnecting = false;
                        }

                        public void onDisplayError(int error) {
                            if (WifiDisplayController.this.mConnectedDevice == oldDevice) {
                                Slog.i(WifiDisplayController.TAG, "Lost RTSP connection with Wifi display due to error " + error + ": " + WifiDisplayController.this.mConnectedDevice.deviceName);
                                WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspTimeout);
                                WifiDisplayController.this.handleConnectionFailure(false);
                            }
                            WifiDisplayController.this.mRTSPConnecting = false;
                        }

                        public void onDisplayKeyEvent(int uniCode, int flags) {
                            Slog.d(WifiDisplayController.TAG, "onDisplayKeyEvent:uniCode=" + uniCode);
                            if (WifiDisplayController.this.mInputMethodManager != null) {
                                try {
                                    if (WifiDisplayController.this.mWakeLock != null) {
                                        WifiDisplayController.this.mWakeLock.acquire();
                                    }
                                    WifiDisplayController.this.mInputMethodManager.sendCharacterToCurClient(uniCode);
                                    if (WifiDisplayController.this.mWakeLock != null) {
                                        WifiDisplayController.this.mWakeLock.release();
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        public void onDisplayGenericMsgEvent(int event) {
                            Slog.d(WifiDisplayController.TAG, "onDisplayGenericMsgEvent: " + event);
                        }
                    }, this.mHandler, this.mContext.getOpPackageName());
                    this.mHandler.postDelayed(this.mRtspTimeout, (long) ((this.mWifiDisplayCertMode ? 120 : 75) * 1000));
                } else if (this.mConnectedDevice != null && this.mRemoteDisplay == null && getInterfaceAddress(this.mConnectedDeviceGroupInfo) == null) {
                    Slog.i(TAG, "Failed to get local interface address for communicating with Wifi display: " + this.mConnectedDevice.deviceName);
                    handleConnectionFailure(false);
                }
            }
        }
    }

    private WifiDisplaySessionInfo getSessionInfo(WifiP2pGroup info, int session) {
        if (info == null) {
            return null;
        }
        String hostAddress;
        Inet4Address addr = getInterfaceAddress(info);
        boolean z = !info.getOwner().deviceAddress.equals(this.mThisDevice.deviceAddress);
        String str = info.getOwner().deviceAddress + " " + info.getNetworkName();
        String passphrase = info.getPassphrase();
        if (addr != null) {
            hostAddress = addr.getHostAddress();
        } else {
            hostAddress = IElsaManager.EMPTY_PACKAGE;
        }
        WifiDisplaySessionInfo sessionInfo = new WifiDisplaySessionInfo(z, session, str, passphrase, hostAddress);
        if (DEBUG) {
            Slog.d(TAG, sessionInfo.toString());
        }
        return sessionInfo;
    }

    private void handleStateChanged(boolean enabled) {
        this.mWifiP2pEnabled = enabled;
        updateWfdEnableState();
        if (!enabled) {
            dismissDialog();
        }
    }

    private void handlePeersChanged() {
        requestPeers();
    }

    private void handleConnectionChanged(NetworkInfo networkInfo, int reason) {
        Slog.i(TAG, "handleConnectionChanged(), mWfdEnabled:" + this.mWfdEnabled);
        this.mNetworkInfo = networkInfo;
        if (!this.mWfdEnabled || !networkInfo.isConnected()) {
            this.mConnectedDeviceGroupInfo = null;
            if (!(this.mConnectingDevice == null && this.mConnectedDevice == null)) {
                disconnect();
            }
            if (this.mWfdEnabled) {
                requestPeers();
                if (this.mLastTimeConnected && this.mReconnectForResolutionChange) {
                    Slog.i(TAG, "requestStartScan() for resolution change.");
                    this.mReScanning = true;
                    updateScanState();
                    this.mReConnection_Timeout_Remain_Seconds = 60;
                    this.mHandler.postDelayed(this.mReConnect, 1000);
                }
            }
            this.mReconnectForResolutionChange = false;
            if (7 == reason && this.mReConnectDevice != null) {
                Slog.i(TAG, "reconnect procedure start, ReConnectDevice = " + this.mReConnectDevice);
                dialogReconnect();
            }
            handleChannelConflictProcedure(ChannelConflictEvt.EVT_WFD_P2P_DISCONNECTED);
        } else if (this.mDesiredDevice != null || this.mWifiDisplayCertMode) {
            this.mWifiP2pManager.requestGroupInfo(this.mWifiP2pChannel, new GroupInfoListener() {
                public void onGroupInfoAvailable(WifiP2pGroup info) {
                    if (info == null) {
                        Slog.i(WifiDisplayController.TAG, "Error: group is null !!!");
                        return;
                    }
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "Received group info: " + WifiDisplayController.describeWifiP2pGroup(info));
                    }
                    if (WifiDisplayController.this.mConnectingDevice != null && !info.contains(WifiDisplayController.this.mConnectingDevice)) {
                        Slog.i(WifiDisplayController.TAG, "Aborting connection to Wifi display because the current P2P group does not contain the device we expected to find: " + WifiDisplayController.this.mConnectingDevice.deviceName + ", group info was: " + WifiDisplayController.describeWifiP2pGroup(info));
                        WifiDisplayController.this.handleConnectionFailure(false);
                    } else if (WifiDisplayController.this.mDesiredDevice == null || info.contains(WifiDisplayController.this.mDesiredDevice)) {
                        if (WifiDisplayController.this.mWifiDisplayCertMode) {
                            boolean owner = info.getOwner().deviceAddress.equals(WifiDisplayController.this.mThisDevice.deviceAddress);
                            if (owner && info.getClientList().isEmpty()) {
                                WifiDisplayController.this.mConnectingDevice = WifiDisplayController.this.mDesiredDevice = null;
                                WifiDisplayController.this.mConnectedDeviceGroupInfo = info;
                                WifiDisplayController.this.updateConnection();
                            } else if (WifiDisplayController.this.mConnectingDevice == null && WifiDisplayController.this.mDesiredDevice == null) {
                                WifiDisplayController.this.mConnectingDevice = WifiDisplayController.this.mDesiredDevice = owner ? (WifiP2pDevice) info.getClientList().iterator().next() : info.getOwner();
                            }
                        }
                        if (WifiDisplayController.this.mConnectingDevice != null && WifiDisplayController.this.mConnectingDevice == WifiDisplayController.this.mDesiredDevice) {
                            Slog.i(WifiDisplayController.TAG, "Connected to Wifi display: " + WifiDisplayController.this.mConnectingDevice.deviceName);
                            WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mConnectionTimeout);
                            WifiDisplayController.this.mConnectedDeviceGroupInfo = info;
                            WifiDisplayController.this.mConnectedDevice = WifiDisplayController.this.mConnectingDevice;
                            WifiDisplayController.this.mConnectingDevice = null;
                            WifiDisplayController.this.updateWfdStatFile(1);
                            WifiDisplayController.this.updateConnection();
                            WifiDisplayController.this.handleChannelConflictProcedure(ChannelConflictEvt.EVT_WFD_P2P_CONNECTED);
                        }
                    } else {
                        Slog.i(WifiDisplayController.TAG, "Aborting connection to Wifi display because the current P2P group does not contain the device we desired to find: " + WifiDisplayController.this.mDesiredDevice.deviceName + ", group info was: " + WifiDisplayController.describeWifiP2pGroup(info));
                        WifiDisplayController.this.disconnect();
                    }
                }
            });
        }
        if (this.mDesiredDevice == null) {
            this.mIsConnected_OtherP2p = networkInfo.isConnected();
            if (this.mIsConnected_OtherP2p) {
                Slog.w(TAG, "Wifi P2p connection is connected but it does not wifidisplay trigger");
                resetReconnectVariable();
            }
        }
    }

    private void handleConnectionFailure(boolean timeoutOccurred) {
        int i = 0;
        Slog.i(TAG, "Wifi display connection failed!");
        Slog.i(TAG, "requestScan after connection failed..");
        discoverPeers();
        if (this.mDesiredDevice == null) {
            return;
        }
        if (this.mConnectionRetriesLeft > 0) {
            final WifiP2pDevice oldDevice = this.mDesiredDevice;
            Handler handler = this.mHandler;
            Runnable anonymousClass34 = new Runnable() {
                public void run() {
                    if (WifiDisplayController.this.mDesiredDevice == oldDevice && WifiDisplayController.this.mConnectionRetriesLeft > 0) {
                        WifiDisplayController wifiDisplayController = WifiDisplayController.this;
                        wifiDisplayController.mConnectionRetriesLeft = wifiDisplayController.mConnectionRetriesLeft - 1;
                        Slog.i(WifiDisplayController.TAG, "Retrying Wifi display connection.  Retries left: " + WifiDisplayController.this.mConnectionRetriesLeft);
                        WifiDisplayController.this.retryConnection();
                    }
                }
            };
            if (!timeoutOccurred) {
                i = 500;
            }
            handler.postDelayed(anonymousClass34, (long) i);
            return;
        }
        disconnect();
    }

    private void advertiseDisplay(WifiDisplay display, Surface surface, int width, int height, int flags) {
        if (DEBUG) {
            Slog.d(TAG, "advertiseDisplay(): ----->\n\tdisplay: " + display + "\n\tsurface: " + surface + "\n\twidth: " + width + "\n\theight: " + height + "\n\tflags: " + flags);
        }
        if (!Objects.equal(this.mAdvertisedDisplay, display) || this.mAdvertisedDisplaySurface != surface || this.mAdvertisedDisplayWidth != width || this.mAdvertisedDisplayHeight != height || this.mAdvertisedDisplayFlags != flags) {
            final WifiDisplay oldDisplay = this.mAdvertisedDisplay;
            final Surface oldSurface = this.mAdvertisedDisplaySurface;
            this.mAdvertisedDisplay = display;
            this.mAdvertisedDisplaySurface = surface;
            this.mAdvertisedDisplayWidth = width;
            this.mAdvertisedDisplayHeight = height;
            this.mAdvertisedDisplayFlags = flags;
            final Surface surface2 = surface;
            final WifiDisplay wifiDisplay = display;
            final int i = flags;
            final int i2 = width;
            final int i3 = height;
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "oldSurface = " + oldSurface + ", surface = " + surface2 + ", oldDisplay = " + oldDisplay + ", display = " + wifiDisplay);
                    }
                    if (oldSurface != null && surface2 != oldSurface) {
                        Slog.d(WifiDisplayController.TAG, "callback onDisplayDisconnected()");
                        WifiDisplayController.this.mListener.onDisplayDisconnected();
                        WifiDisplayController.this.actionAtDisconnected(oldDisplay);
                    } else if (!(oldDisplay == null || oldDisplay.hasSameAddress(wifiDisplay))) {
                        Slog.d(WifiDisplayController.TAG, "callback onDisplayConnectionFailed()");
                        WifiDisplayController.this.mListener.onDisplayConnectionFailed();
                        WifiDisplayController.this.actionAtConnectionFailed();
                    }
                    if (wifiDisplay != null) {
                        if (!wifiDisplay.hasSameAddress(oldDisplay)) {
                            Slog.d(WifiDisplayController.TAG, "callback onDisplayConnecting(): display = " + wifiDisplay);
                            WifiDisplayController.this.mListener.onDisplayConnecting(wifiDisplay);
                            WifiDisplayController.this.actionAtConnecting();
                        } else if (!wifiDisplay.equals(oldDisplay)) {
                            WifiDisplayController.this.mListener.onDisplayChanged(wifiDisplay);
                        }
                        if (surface2 != null && surface2 != oldSurface) {
                            boolean z;
                            WifiDisplayController.this.updateIfHdcp(i);
                            Slog.d(WifiDisplayController.TAG, "callback onDisplayConnected(): display = " + wifiDisplay + ", surface = " + surface2 + ", width = " + i2 + ", height = " + i3 + ", flags = " + i);
                            WifiDisplayController.this.mListener.onDisplayConnected(wifiDisplay, surface2, i2, i3, i);
                            WifiDisplayController wifiDisplayController = WifiDisplayController.this;
                            WifiDisplay wifiDisplay = wifiDisplay;
                            int i = i;
                            if (i2 < i3) {
                                z = true;
                            } else {
                                z = false;
                            }
                            wifiDisplayController.actionAtConnected(wifiDisplay, i, z);
                        }
                    }
                }
            });
        } else if (DEBUG) {
            Slog.d(TAG, "advertiseDisplay() : no need update!");
        }
    }

    private void unadvertiseDisplay() {
        advertiseDisplay(null, null, 0, 0, 0);
    }

    private void readvertiseDisplay(WifiDisplay display) {
        advertiseDisplay(display, this.mAdvertisedDisplaySurface, this.mAdvertisedDisplayWidth, this.mAdvertisedDisplayHeight, this.mAdvertisedDisplayFlags);
    }

    private static Inet4Address getInterfaceAddress(WifiP2pGroup info) {
        try {
            Enumeration<InetAddress> addrs = NetworkInterface.getByName(info.getInterface()).getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress addr = (InetAddress) addrs.nextElement();
                if (addr instanceof Inet4Address) {
                    return (Inet4Address) addr;
                }
            }
            Slog.w(TAG, "Could not obtain address of network interface " + info.getInterface() + " because it had no IPv4 addresses.");
            return null;
        } catch (SocketException ex) {
            Slog.w(TAG, "Could not obtain address of network interface " + info.getInterface(), ex);
            return null;
        }
    }

    private static int getPortNumber(WifiP2pDevice device) {
        if (device.deviceName.startsWith("DIRECT-") && device.deviceName.endsWith("Broadcom")) {
            return 8554;
        }
        return DEFAULT_CONTROL_PORT;
    }

    private static boolean isWifiDisplay(WifiP2pDevice device) {
        if (device.wfdInfo != null && device.wfdInfo.isWfdEnabled() && device.wfdInfo.isSessionAvailable()) {
            return isPrimarySinkDeviceType(device.wfdInfo.getDeviceType());
        }
        return false;
    }

    private static boolean isPrimarySinkDeviceType(int deviceType) {
        if (deviceType == 1 || deviceType == 3) {
            return true;
        }
        return false;
    }

    private static String describeWifiP2pDevice(WifiP2pDevice device) {
        return device != null ? device.toString().replace(10, ',') : "null";
    }

    private static String describeWifiP2pGroup(WifiP2pGroup group) {
        return group != null ? group.toString().replace(10, ',') : "null";
    }

    private static WifiDisplay createWifiDisplay(WifiP2pDevice device) {
        return new WifiDisplay(device.deviceAddress, device.deviceName, null, true, device.wfdInfo.isSessionAvailable(), false);
    }

    private void sendKeyEvent(int keyCode, int isDown) {
        long now = SystemClock.uptimeMillis();
        if (isDown == 1) {
            injectKeyEvent(new KeyEvent(now, now, 0, translateAsciiToKeyCode(keyCode), 0, 0, -1, 0, 0, 257));
            return;
        }
        injectKeyEvent(new KeyEvent(now, now, 1, translateAsciiToKeyCode(keyCode), 0, 0, -1, 0, 0, 257));
    }

    private void sendTap(float x, float y) {
        long now = SystemClock.uptimeMillis();
        injectPointerEvent(MotionEvent.obtain(now, now, 0, x, y, 0));
        injectPointerEvent(MotionEvent.obtain(now, now, 1, x, y, 0));
    }

    private void injectKeyEvent(KeyEvent event) {
        Slog.d(TAG, "InjectKeyEvent: " + event);
        InputManager.getInstance().injectInputEvent(event, 2);
    }

    private void injectPointerEvent(MotionEvent event) {
        event.setSource(4098);
        Slog.d("Input", "InjectPointerEvent: " + event);
        InputManager.getInstance().injectInputEvent(event, 2);
    }

    private int translateSpecialCode(int ascii) {
        switch (ascii) {
            case 8:
                return 67;
            case 12:
                return 66;
            case 13:
                return 66;
            case 16:
                return 59;
            case 20:
                return HdmiCecKeycode.CEC_KEYCODE_F3_GREEN;
            case 27:
                return 111;
            case 32:
                return 62;
            case 33:
                return 93;
            case 34:
                return 92;
            case 37:
                return 19;
            case 38:
                return 20;
            case 39:
                return 22;
            case 40:
                return 21;
            case 186:
                return 74;
            case 187:
                return 70;
            case 188:
                return 55;
            case 189:
                return 69;
            case 190:
                return 56;
            case 191:
                return 76;
            case 192:
                return 68;
            case NetdResponseCode.InterfaceTxThrottleResult /*219*/:
                return 71;
            case NetdResponseCode.QuotaCounterResult /*220*/:
                return 73;
            case NetdResponseCode.TetheringStatsResult /*221*/:
                return 72;
            case NetdResponseCode.DnsProxyQueryResult /*222*/:
                return 75;
            default:
                return 0;
        }
    }

    private int translateAsciiToKeyCode(int ascii) {
        if (ascii >= 48 && ascii <= 57) {
            return ascii - 41;
        }
        if (ascii >= 65 && ascii <= 90) {
            return ascii - 36;
        }
        int newKeyCode = translateSpecialCode(ascii);
        if (newKeyCode > 0) {
            Slog.d(TAG, "special code: " + ascii + ":" + newKeyCode);
            return newKeyCode;
        }
        Slog.d(TAG, "translateAsciiToKeyCode: ascii is not supported" + ascii);
        return 0;
    }

    private void getWifiLock() {
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
        if (this.mWifiLock == null && this.mWifiManager != null) {
            this.mWifiLock = this.mWifiManager.createWifiLock(1, "WFD_WifiLock");
        }
    }

    private void updateIfHdcp(int flags) {
        boolean secure = false;
        if ((flags & 1) != 0) {
            secure = true;
        }
        if (secure) {
            SystemProperties.set("media.wfd.hdcp", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        } else {
            SystemProperties.set("media.wfd.hdcp", "0");
        }
    }

    private void stopWifiScan(boolean ifStop) {
        if (this.mStopWifiScan != ifStop) {
            Slog.i(TAG, "stopWifiScan()," + ifStop);
            this.mWifiManager.stopReconnectAndScan(ifStop, 0, true);
            this.mStopWifiScan = ifStop;
        }
    }

    private void actionAtConnected(WifiDisplay display, int flags, boolean portrait) {
        boolean secure;
        boolean show = false;
        Slog.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
        this.mIsWFDConnected = true;
        Intent intent = new Intent(WFD_CONNECTION);
        intent.addFlags(67108864);
        intent.putExtra("connected", 1);
        if (display != null) {
            intent.putExtra("device_address", display.getDeviceAddress());
            intent.putExtra("device_name", display.getDeviceName());
            intent.putExtra("device_alias", display.getDeviceAlias());
        } else {
            Slog.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ", null display");
            intent.putExtra("device_address", "00:00:00:00:00:00");
            intent.putExtra("device_name", "wifidisplay dongle");
            intent.putExtra("device_alias", "wifidisplay dongle");
        }
        if ((flags & 1) != 0) {
            secure = true;
        } else {
            secure = false;
        }
        if (secure) {
            intent.putExtra("secure", 1);
        } else {
            intent.putExtra("secure", 0);
        }
        Slog.i(TAG, "secure:" + secure);
        int usingUIBC = this.mRemoteDisplay.getWfdParam(8);
        if ((usingUIBC & 1) == 0 && (usingUIBC & 2) == 0) {
            intent.putExtra("uibc_touch_mouse", 0);
        } else {
            intent.putExtra("uibc_touch_mouse", 1);
        }
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        if (this.mReConnecting) {
            resetReconnectVariable();
        }
        getWifiLock();
        if (this.mWifiManager == null || this.mWifiLock == null) {
            Slog.e(TAG, "actionAtConnected(): mWifiManager: " + this.mWifiManager + ", mWifiLock: " + this.mWifiLock);
        } else if (this.mWifiLock.isHeld()) {
            Slog.e(TAG, "WFD connected, and WifiLock is Held!");
        } else {
            if (DEBUG) {
                Slog.i(TAG, "acquire wifilock");
            }
            this.mWifiLock.acquire();
        }
        if (this.WFDCONTROLLER_QE_ON) {
            this.mHandler.postDelayed(this.mWifiLinkInfo, 2000);
            resetSignalParam();
        }
        if (SystemProperties.get("ro.mtk_wfd_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            if (SystemProperties.getInt("af.policy.r_submix_prio_adjust", 0) == 0) {
                show = true;
            }
            if (show) {
                checkA2dpStatus();
            }
            updateChosenCapability(usingUIBC, portrait);
            this.mInputMethodManager = IInputMethodManager.Stub.asInterface(ServiceManager.getService("input_method"));
            if (this.mLatencyProfiling == 3) {
                this.mHandler.postDelayed(this.mDelayProfiling, 2000);
            }
        }
        notifyClearMotion(true);
        if (this.mWifiApConnected) {
            checkIfWifiApIs11G();
        }
    }

    private void actionAtDisconnected(WifiDisplay display) {
        Slog.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
        if (this.mIsWFDConnected && display.getDeviceName().contains("Push2TV")) {
            this.mBlockMac = display.getDeviceAddress();
            Slog.i(TAG, "Add block mac:" + this.mBlockMac);
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    Slog.i(WifiDisplayController.TAG, "Remove block mac:" + WifiDisplayController.this.mBlockMac);
                    WifiDisplayController.this.mBlockMac = null;
                }
            }, 15000);
        }
        this.mIsWFDConnected = false;
        Intent intent = new Intent(WFD_CONNECTION);
        intent.addFlags(67108864);
        intent.putExtra("connected", 0);
        if (display != null) {
            intent.putExtra("device_address", display.getDeviceAddress());
            intent.putExtra("device_name", display.getDeviceName());
            intent.putExtra("device_alias", display.getDeviceAlias());
        } else {
            Slog.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ", null display");
            intent.putExtra("device_address", "00:00:00:00:00:00");
            intent.putExtra("device_name", "wifidisplay dongle");
            intent.putExtra("device_alias", "wifidisplay dongle");
        }
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        if (this.mReConnecting) {
            Toast.makeText(this.mContext, 134545542, 0).show();
            resetReconnectVariable();
        }
        getWifiLock();
        if (this.mWifiManager == null || this.mWifiLock == null) {
            Slog.e(TAG, "actionAtDisconnected(): mWifiManager: " + this.mWifiManager + ", mWifiLock: " + this.mWifiLock);
        } else if (this.mWifiLock.isHeld()) {
            if (DEBUG) {
                Slog.i(TAG, "release wifilock");
            }
            this.mWifiLock.release();
        } else {
            Slog.e(TAG, "WFD disconnected, and WifiLock isn't Held!");
        }
        if (this.WFDCONTROLLER_QE_ON) {
            this.mHandler.removeCallbacks(this.mWifiLinkInfo);
        }
        clearNotify();
        if (SystemProperties.get("ro.mtk_wfd_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            updateChosenCapability(0, false);
            stopProfilingInfo();
        }
        notifyClearMotion(false);
        stopWifiScan(false);
    }

    private void actionAtConnecting() {
        Slog.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    private void actionAtConnectionFailed() {
        Slog.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
        if (this.mReConnecting) {
            Toast.makeText(this.mContext, 134545542, 0).show();
            resetReconnectVariable();
        }
        stopWifiScan(false);
    }

    private int loadWfdWpsSetup() {
        String wfdWpsSetup = SystemProperties.get("wlan.wfd.wps.setup", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        if (DEBUG) {
            Slog.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ", wfdWpsSetup = " + wfdWpsSetup);
        }
        switch (Integer.valueOf(wfdWpsSetup).intValue()) {
            case 0:
                return 2;
            case 1:
                return 0;
            default:
                return 0;
        }
    }

    private void loadDebugLevel() {
        String debugLevel = SystemProperties.get("wlan.wfd.controller.debug", "0");
        if (DEBUG) {
            Slog.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ", debugLevel = " + debugLevel);
        }
        switch (Integer.valueOf(debugLevel).intValue()) {
            case 0:
                DEBUG = false;
                return;
            case 1:
                DEBUG = true;
                return;
            default:
                DEBUG = false;
                return;
        }
    }

    private void enableWifiDisplay() {
        this.mHandler.removeCallbacks(this.mEnableWifiDelay);
        if (SystemProperties.get("ro.mtk_wfd_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && this.mWifiDisplayOnSetting && !this.mWifiP2pEnabled) {
            long delay = Global.getLong(this.mContext.getContentResolver(), "wifi_reenable_delay", 500);
            Slog.d(TAG, "Enable wifi with delay:" + delay);
            this.mHandler.postDelayed(this.mEnableWifiDelay, delay);
            Toast.makeText(this.mContext, 134545544, 0).show();
            return;
        }
        this.mAutoEnableWifi = false;
        updateWfdEnableState();
    }

    private void updateSettingsHDMI() {
        ContentResolver resolver = this.mContext.getContentResolver();
        boolean HDMIOn = false;
        if (SystemProperties.get("ro.mtk_hdmi_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            HDMIOn = System.getInt(resolver, "hdmi_enable_status", 0) != 0;
        }
        if (!HDMIOn || !this.mWifiDisplayOnSetting) {
            return;
        }
        if (3 == computeFeatureState()) {
            dialogWfdHdmiConflict(2);
            return;
        }
        if (DEBUG) {
            Slog.d(TAG, "HDMI on and WFD feature state isn't on --> turn off WifiDisplay directly");
        }
        Global.putInt(this.mContext.getContentResolver(), "wifi_display_on", 0);
    }

    private void updateWfdStatFile(int wfd_stat) {
    }

    private void dialogWfdHdmiConflict(int which) {
        if (DEBUG) {
            Slog.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ", which = " + which);
        }
        if (which == 0) {
            showDialog(2);
        } else if (2 == which) {
            showDialog(3);
        }
    }

    private boolean checkInterference(Matcher match) {
        int rssi = Float.valueOf(match.group(4)).intValue();
        int rate = Float.valueOf(match.group(6)).intValue();
        int totalCnt = Integer.valueOf(match.group(7)).intValue();
        int thresholdCnt = Integer.valueOf(match.group(8)).intValue();
        int failCnt = Integer.valueOf(match.group(9)).intValue();
        int timeoutCnt = Integer.valueOf(match.group(10)).intValue();
        int apt = Integer.valueOf(match.group(11)).intValue();
        int aat = Integer.valueOf(match.group(12)).intValue();
        if (rssi < -50 || rate < 58 || totalCnt < 10 || thresholdCnt > 3 || failCnt > 2 || timeoutCnt > 2 || apt > 2 || aat > 1) {
            return true;
        }
        return false;
    }

    private int parseDec(String decString) {
        int num = 0;
        try {
            return Integer.parseInt(decString);
        } catch (NumberFormatException e) {
            Slog.e(TAG, "Failed to parse dec string " + decString);
            return num;
        }
    }

    private int parseFloat(String floatString) {
        try {
            return (int) Float.parseFloat(floatString);
        } catch (NumberFormatException e) {
            Slog.e(TAG, "Failed to parse float string " + floatString);
            return 0;
        }
    }

    private void updateSignalLevel(boolean interference) {
        int avarageScore = getAverageScore();
        updateScoreLevel(avarageScore);
        String message = "W:" + avarageScore + ",I:" + interference + ",L:" + this.mLevel;
        if (this.mScoreLevel >= 6) {
            this.mLevel += 2;
            this.mScoreLevel = 0;
        } else if (this.mScoreLevel >= 4) {
            this.mLevel++;
            this.mScoreLevel = 0;
        } else if (this.mScoreLevel <= -6) {
            this.mLevel -= 2;
            this.mScoreLevel = 0;
        } else if (this.mScoreLevel <= -4) {
            this.mLevel--;
            this.mScoreLevel = 0;
        }
        if (this.mLevel > 0) {
            this.mLevel = 0;
        }
        if (this.mLevel < -5) {
            this.mLevel = -5;
        }
        message = message + ">" + this.mLevel;
        handleLevelChange();
        if (this.WFDCONTROLLER_SQC_INFO_ON) {
            Toast.makeText(this.mContext, message, 0).show();
        }
        Slog.d(TAG, message);
    }

    private int getAverageScore() {
        this.mScore[this.mScoreIndex % 4] = this.mWifiScore;
        this.mScoreIndex++;
        int count = 0;
        int sum = 0;
        for (int i = 0; i < 4; i++) {
            if (this.mScore[i] != -1) {
                sum += this.mScore[i];
                count++;
            }
        }
        return sum / count;
    }

    private void updateScoreLevel(int score) {
        if (score >= 100) {
            if (this.mScoreLevel < 0) {
                this.mScoreLevel = 0;
            }
            this.mScoreLevel += 6;
        } else if (score >= 80) {
            if (this.mScoreLevel < 0) {
                this.mScoreLevel = 0;
            }
            this.mScoreLevel += 2;
        } else if (score >= 30) {
            if (this.mScoreLevel > 0) {
                this.mScoreLevel = 0;
            }
            this.mScoreLevel -= 2;
        } else if (score >= 10) {
            if (this.mScoreLevel > 0) {
                this.mScoreLevel = 0;
            }
            this.mScoreLevel -= 3;
        } else {
            if (this.mScoreLevel > 0) {
                this.mScoreLevel = 0;
            }
            this.mScoreLevel -= 6;
        }
    }

    private void resetSignalParam() {
        this.mLevel = 0;
        this.mScoreLevel = 0;
        this.mScoreIndex = 0;
        for (int i = 0; i < 4; i++) {
            this.mScore[i] = -1;
        }
        this.mNotiTimerStarted = false;
        this.mToastTimerStarted = false;
    }

    private void registerEMObserver(int widthPixels, int heightPixels) {
        int i;
        int i2 = 1;
        this.WFDCONTROLLER_DISPLAY_TOAST_TIME = this.mContext.getResources().getInteger(134938628);
        this.WFDCONTROLLER_DISPLAY_NOTIFICATION_TIME = this.mContext.getResources().getInteger(134938629);
        this.WFDCONTROLLER_DISPLAY_RESOLUTION = this.mContext.getResources().getInteger(134938630);
        this.WFDCONTROLLER_DISPLAY_POWER_SAVING_OPTION = this.mContext.getResources().getInteger(134938631);
        this.WFDCONTROLLER_DISPLAY_POWER_SAVING_DELAY = this.mContext.getResources().getInteger(134938632);
        this.WFDCONTROLLER_DISPLAY_SECURE_OPTION = this.mContext.getResources().getInteger(134938633);
        Slog.d(TAG, "registerEMObserver(), tt:" + this.WFDCONTROLLER_DISPLAY_TOAST_TIME + ",nt:" + this.WFDCONTROLLER_DISPLAY_NOTIFICATION_TIME + ",res:" + this.WFDCONTROLLER_DISPLAY_RESOLUTION + ",ps:" + this.WFDCONTROLLER_DISPLAY_POWER_SAVING_OPTION + ",psd:" + this.WFDCONTROLLER_DISPLAY_POWER_SAVING_DELAY + ",so:" + this.WFDCONTROLLER_DISPLAY_SECURE_OPTION);
        Global.putInt(this.mContext.getContentResolver(), "wifi_display_display_toast_time", this.WFDCONTROLLER_DISPLAY_TOAST_TIME);
        Global.putInt(this.mContext.getContentResolver(), "wifi_display_notification_time", this.WFDCONTROLLER_DISPLAY_NOTIFICATION_TIME);
        Global.putInt(this.mContext.getContentResolver(), "wifi_display_sqc_info_on", this.WFDCONTROLLER_SQC_INFO_ON ? 1 : 0);
        ContentResolver contentResolver = this.mContext.getContentResolver();
        String str = "wifi_display_qe_on";
        if (this.WFDCONTROLLER_QE_ON) {
            i = 1;
        } else {
            i = 0;
        }
        Global.putInt(contentResolver, str, i);
        if (SystemProperties.get("ro.mtk_wfd_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            int r = Global.getInt(this.mContext.getContentResolver(), "wifi_display_max_resolution", -1);
            if (r == -1) {
                if (this.WFDCONTROLLER_DISPLAY_RESOLUTION >= 0 && this.WFDCONTROLLER_DISPLAY_RESOLUTION <= 3) {
                    i = this.WFDCONTROLLER_DISPLAY_RESOLUTION;
                    this.mResolution = i;
                    this.mPrevResolution = i;
                } else if (widthPixels < 1080 || heightPixels < 1920) {
                    this.mResolution = 0;
                    this.mPrevResolution = 0;
                } else {
                    this.mResolution = 2;
                    this.mPrevResolution = 2;
                }
            } else if (r < 0 || r > 3) {
                this.mResolution = 0;
                this.mPrevResolution = 0;
            } else {
                this.mResolution = r;
                this.mPrevResolution = r;
            }
            int resolutionIndex = getResolutionIndex(this.mResolution);
            Slog.i(TAG, "mResolution:" + this.mResolution + ", resolutionIndex: " + resolutionIndex);
            SystemProperties.set("media.wfd.video-format", String.valueOf(resolutionIndex));
        }
        ContentResolver contentResolver2 = this.mContext.getContentResolver();
        String str2 = "wifi_display_auto_channel_selection";
        if (!this.mAutoChannelSelection) {
            i2 = 0;
        }
        Global.putInt(contentResolver2, str2, i2);
        Global.putInt(this.mContext.getContentResolver(), "wifi_display_max_resolution", this.mResolution);
        Global.putInt(this.mContext.getContentResolver(), "wifi_display_power_saving_option", this.WFDCONTROLLER_DISPLAY_POWER_SAVING_OPTION);
        Global.putInt(this.mContext.getContentResolver(), "wifi_display_power_saving_delay", this.WFDCONTROLLER_DISPLAY_POWER_SAVING_DELAY);
        Global.putInt(this.mContext.getContentResolver(), "wifi_display_latency_profiling", this.mLatencyProfiling);
        Global.putString(this.mContext.getContentResolver(), "wifi_display_chosen_capability", IElsaManager.EMPTY_PACKAGE);
        initPortraitResolutionSupport();
        resetLatencyInfo();
        initSecureOption();
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_display_display_toast_time"), false, this.mObserver);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_display_notification_time"), false, this.mObserver);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_display_sqc_info_on"), false, this.mObserver);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_display_qe_on"), false, this.mObserver);
        if (SystemProperties.get("ro.mtk_wfd_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_display_auto_channel_selection"), false, this.mObserver);
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_display_max_resolution"), false, this.mObserver);
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_display_latency_profiling"), false, this.mObserver);
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_display_security_option"), false, this.mObserver);
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_display_portrait_resolution"), false, this.mObserver);
        }
    }

    private void initPortraitResolutionSupport() {
        Global.putInt(this.mContext.getContentResolver(), "wifi_display_portrait_resolution", 0);
        SystemProperties.set("media.wfd.portrait", String.valueOf(0));
    }

    private void handlePortraitResolutionSupportChange() {
        int value = Global.getInt(this.mContext.getContentResolver(), "wifi_display_portrait_resolution", 0);
        Slog.i(TAG, "handlePortraitResolutionSupportChange:" + value);
        SystemProperties.set("media.wfd.portrait", String.valueOf(value));
    }

    private void sendPortraitIntent() {
        Slog.d(TAG, "sendPortraitIntent()");
        Intent intent = new Intent(WFD_PORTRAIT);
        intent.addFlags(67108864);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void initSecureOption() {
        Global.putInt(this.mContext.getContentResolver(), "wifi_display_security_option", this.WFDCONTROLLER_DISPLAY_SECURE_OPTION);
        SystemProperties.set("wlan.wfd.security.image", String.valueOf(this.WFDCONTROLLER_DISPLAY_SECURE_OPTION));
    }

    private void handleSecureOptionChange() {
        int secureOption = Global.getInt(this.mContext.getContentResolver(), "wifi_display_security_option", 1);
        if (secureOption != this.WFDCONTROLLER_DISPLAY_SECURE_OPTION) {
            Slog.i(TAG, "handleSecureOptionChange:" + secureOption + "->" + this.WFDCONTROLLER_DISPLAY_SECURE_OPTION);
            this.WFDCONTROLLER_DISPLAY_SECURE_OPTION = secureOption;
            SystemProperties.set("ro.sf.security.image", String.valueOf(this.WFDCONTROLLER_DISPLAY_SECURE_OPTION));
        }
    }

    private int getResolutionIndex(int settingValue) {
        switch (settingValue) {
            case 0:
            case 3:
                return 5;
            case 1:
            case 2:
                return 7;
            default:
                return 5;
        }
    }

    private void handleResolutionChange() {
        int r = Global.getInt(this.mContext.getContentResolver(), "wifi_display_max_resolution", 0);
        if (r != this.mResolution) {
            this.mPrevResolution = this.mResolution;
            this.mResolution = r;
            Slog.d(TAG, "handleResolutionChange(), resolution:" + this.mPrevResolution + "->" + this.mResolution);
            int idxModified = getResolutionIndex(this.mResolution);
            int idxOriginal = getResolutionIndex(this.mPrevResolution);
            if (idxModified != idxOriginal) {
                boolean doNotRemind = Global.getInt(this.mContext.getContentResolver(), "wifi_display_change_resolution_remind", 0) != 0;
                Slog.d(TAG, "index:" + idxOriginal + "->" + idxModified + ", doNotRemind:" + doNotRemind);
                SystemProperties.set("media.wfd.video-format", String.valueOf(idxModified));
                if (!(this.mConnectedDevice == null && this.mConnectingDevice == null)) {
                    if (doNotRemind) {
                        Slog.d(TAG, "-- reconnect for resolution change --");
                        disconnect();
                        this.mReconnectForResolutionChange = true;
                    } else {
                        showDialog(5);
                    }
                }
            }
        }
    }

    private void revertResolutionChange() {
        Slog.d(TAG, "revertResolutionChange(), resolution:" + this.mResolution + "->" + this.mPrevResolution);
        int idxModified = getResolutionIndex(this.mResolution);
        int idxOriginal = getResolutionIndex(this.mPrevResolution);
        Slog.d(TAG, "index:" + idxModified + "->" + idxOriginal);
        SystemProperties.set("media.wfd.video-format", String.valueOf(idxOriginal));
        this.mResolution = this.mPrevResolution;
        Global.putInt(this.mContext.getContentResolver(), "wifi_display_max_resolution", this.mResolution);
    }

    private void handleLatencyProfilingChange() {
        int value = Global.getInt(this.mContext.getContentResolver(), "wifi_display_latency_profiling", 2);
        if (value != this.mLatencyProfiling) {
            Slog.d(TAG, "handleLatencyProfilingChange(), connected:" + this.mIsWFDConnected + ",value:" + this.mLatencyProfiling + "->" + value);
            this.mLatencyProfiling = value;
            if (this.mLatencyProfiling != 3) {
                this.mHandler.removeCallbacks(this.mDelayProfiling);
            }
            if ((this.mLatencyProfiling == 0 || this.mLatencyProfiling == 1 || this.mLatencyProfiling == 3) && this.mIsWFDConnected) {
                startProfilingInfo();
            } else {
                stopProfilingInfo();
            }
        }
    }

    private void showLatencyPanel() {
        Slog.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
        this.mLatencyPanelView = LayoutInflater.from(this.mContext).inflate(134676517, null);
        this.mTextView = (TextView) this.mLatencyPanelView.findViewById(135331988);
        this.mTextView.setTextColor(-1);
        this.mTextView.setText("AP:\nS:\nR:\nAL:\n");
        LayoutParams layoutParams = new LayoutParams();
        layoutParams.type = 2037;
        layoutParams.flags = 8;
        layoutParams.width = -2;
        layoutParams.height = -2;
        layoutParams.gravity = 51;
        layoutParams.alpha = 0.7f;
        ((WindowManager) this.mContext.getSystemService("window")).addView(this.mLatencyPanelView, layoutParams);
    }

    private void hideLatencyPanel() {
        Slog.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
        if (this.mLatencyPanelView != null) {
            ((WindowManager) this.mContext.getSystemService("window")).removeView(this.mLatencyPanelView);
            this.mLatencyPanelView = null;
        }
        this.mTextView = null;
    }

    private void checkA2dpStatus() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            Slog.d(TAG, "checkA2dpStatus(), BT is not enabled");
            return;
        }
        int value = Global.getInt(this.mContext.getContentResolver(), "wifi_display_sound_path_do_not_remind", -1);
        Slog.d(TAG, "checkA2dpStatus(), value:" + value);
        if (value != 1) {
            adapter.getProfileProxy(this.mContext, new ServiceListener() {
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    boolean empty = ((BluetoothA2dp) proxy).getConnectedDevices().isEmpty();
                    Slog.d(WifiDisplayController.TAG, "BluetoothProfile listener is connected, empty:" + empty);
                    if (!empty) {
                        WifiDisplayController.this.showDialog(6);
                    }
                }

                public void onServiceDisconnected(int profile) {
                }
            }, 2);
        }
    }

    private void setAutoChannelSelection() {
        Slog.d(TAG, "setAutoChannelSelection(), auto:" + this.mAutoChannelSelection);
        if (this.mAutoChannelSelection) {
            this.mWifiP2pManager.setP2pAutoChannel(this.mWifiP2pChannel, true, null);
        } else {
            this.mWifiP2pManager.setP2pAutoChannel(this.mWifiP2pChannel, false, null);
        }
    }

    private void updateChosenCapability(int usingUIBC, boolean portrait) {
        String capability = IElsaManager.EMPTY_PACKAGE;
        if (this.mIsWFDConnected) {
            if (this.mRemoteDisplay.getWfdParam(3) == 1) {
                capability = capability + "LPCM(2 ch),";
            } else {
                capability = capability + "AAC(2 ch),";
            }
            if (this.mRemoteDisplay.getWfdParam(4) == 1) {
                capability = capability + "H.264(CBP level 3.1),";
            } else {
                capability = capability + "H.264(CHP level 4.1),";
            }
            int resolutionIndex = getResolutionIndex(this.mResolution);
            if (resolutionIndex == 5) {
                if (portrait) {
                    capability = capability + "720x1280 30p,";
                } else {
                    capability = capability + "1280x720 30p,";
                }
            } else if (resolutionIndex != 7) {
                capability = capability + "640x480 60p,";
            } else if (portrait) {
                capability = capability + "1080x1920 30p,";
            } else {
                capability = capability + "1920x1080 30p,";
            }
            if (this.mRemoteDisplay.getWfdParam(7) == 1) {
                capability = capability + "with HDCP,";
            } else {
                capability = capability + "without HDCP,";
            }
            if (usingUIBC != 0) {
                capability = capability + "with UIBC";
            } else {
                capability = capability + "without UIBC";
            }
        }
        Slog.d(TAG, "updateChosenCapability(), connected:" + this.mIsWFDConnected + ", capability:" + capability + ", portrait:" + portrait);
        Global.putString(this.mContext.getContentResolver(), "wifi_display_chosen_capability", capability);
    }

    private void startProfilingInfo() {
        if (this.mLatencyProfiling == 3) {
            showLatencyPanel();
        } else {
            hideLatencyPanel();
        }
        this.mHandler.removeCallbacks(this.mLatencyInfo);
        this.mHandler.removeCallbacks(this.mScanWifiAp);
        this.mHandler.postDelayed(this.mLatencyInfo, 100);
        this.mHandler.postDelayed(this.mScanWifiAp, 100);
    }

    private void stopProfilingInfo() {
        hideLatencyPanel();
        this.mHandler.removeCallbacks(this.mLatencyInfo);
        this.mHandler.removeCallbacks(this.mScanWifiAp);
        this.mHandler.removeCallbacks(this.mDelayProfiling);
        resetLatencyInfo();
    }

    private void resetLatencyInfo() {
        Global.putString(this.mContext.getContentResolver(), "wifi_display_wifi_info", "0,0,0,0");
        Global.putString(this.mContext.getContentResolver(), "wifi_display_wfd_latency", "0,0,0");
    }

    private int getWifiApNum() {
        int count = 0;
        List<ScanResult> results = this.mWifiManager.getScanResults();
        ArrayList<String> SSIDList = new ArrayList();
        if (results != null) {
            for (ScanResult result : results) {
                if (!(result.SSID == null || result.SSID.length() == 0 || result.capabilities.contains("[IBSS]") || getFreqId(result.frequency) != this.mWifiP2pChannelId)) {
                    boolean duplicate = false;
                    for (String ssid : SSIDList) {
                        if (ssid.equals(result.SSID)) {
                            duplicate = true;
                            break;
                        }
                    }
                    if (!duplicate) {
                        if (DEBUG) {
                            Slog.d(TAG, "AP SSID: " + result.SSID);
                        }
                        SSIDList.add(result.SSID);
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private void updateWifiP2pChannelId(boolean connected, Intent intent) {
        if (this.mWfdEnabled && connected && (this.mDesiredDevice != null || this.mSinkEnabled)) {
            int freq = ((WifiP2pGroup) intent.getParcelableExtra("p2pGroupInfo")).getFrequency();
            this.mWifiP2pChannelId = getFreqId(freq);
            Slog.d(TAG, "updateWifiP2pChannelId(), freq:" + freq + ", id:" + this.mWifiP2pChannelId);
            return;
        }
        this.mWifiP2pChannelId = -1;
        Slog.d(TAG, "updateWifiP2pChannelId(), id:" + this.mWifiP2pChannelId);
    }

    private int getFreqId(int frequency) {
        switch (frequency) {
            case 2412:
                return 1;
            case 2417:
                return 2;
            case 2422:
                return 3;
            case 2427:
                return 4;
            case 2432:
                return 5;
            case 2437:
                return 6;
            case 2442:
                return 7;
            case 2447:
                return 8;
            case 2452:
                return 9;
            case 2457:
                return 10;
            case 2462:
                return 11;
            case 2467:
                return 12;
            case 2472:
                return 13;
            case 2484:
                return 14;
            case 5180:
                return 36;
            case 5190:
                return 38;
            case 5200:
                return 40;
            case 5210:
                return 42;
            case 5220:
                return 44;
            case 5230:
                return 46;
            case 5240:
                return 48;
            case 5260:
                return 52;
            case 5280:
                return 56;
            case 5300:
                return 60;
            case 5320:
                return 64;
            case 5500:
                return 100;
            case 5520:
                return 104;
            case 5540:
                return 108;
            case 5560:
                return 112;
            case 5580:
                return HdmiCecKeycode.CEC_KEYCODE_F4_YELLOW;
            case 5600:
                return 120;
            case 5620:
                return 124;
            case 5640:
                return 128;
            case 5660:
                return 132;
            case 5680:
                return 136;
            case 5700:
                return 140;
            case 5745:
                return 149;
            case 5765:
                return 153;
            case 5785:
                return 157;
            case 5805:
                return 161;
            case 5825:
                return 165;
            default:
                return 0;
        }
    }

    private void handleLevelChange() {
        if (this.mLevel < 0) {
            if (!this.mToastTimerStarted) {
                this.mHandler.postDelayed(this.mDisplayToast, (long) (this.WFDCONTROLLER_DISPLAY_TOAST_TIME * 1000));
                this.mToastTimerStarted = true;
            }
            if (!this.mNotiTimerStarted) {
                this.mHandler.postDelayed(this.mDisplayNotification, (long) (this.WFDCONTROLLER_DISPLAY_NOTIFICATION_TIME * 1000));
                this.mNotiTimerStarted = true;
                return;
            }
            return;
        }
        clearNotify();
    }

    private void clearNotify() {
        if (this.mToastTimerStarted) {
            this.mHandler.removeCallbacks(this.mDisplayToast);
            this.mToastTimerStarted = false;
        }
        if (this.mNotiTimerStarted) {
            this.mHandler.removeCallbacks(this.mDisplayNotification);
            this.mNotiTimerStarted = false;
        }
        this.mNotificationManager.cancelAsUser(null, 134545536, UserHandle.ALL);
    }

    private void showNotification(int titleId, int contentId) {
        Slog.d(TAG, "showNotification(), titleId:" + titleId);
        this.mNotificationManager.cancelAsUser(null, titleId, UserHandle.ALL);
        Resources mResource = Resources.getSystem();
        this.mNotificationManager.notifyAsUser(null, titleId, new BigTextStyle(new Builder(this.mContext).setContentTitle(mResource.getString(titleId)).setContentText(mResource.getString(contentId)).setSmallIcon(134348907).setAutoCancel(true)).bigText(mResource.getString(contentId)).build(), UserHandle.ALL);
    }

    private void dialogReconnect() {
        showDialog(4);
    }

    private void resetReconnectVariable() {
        Slog.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
        this.mReScanning = false;
        this.mReConnectDevice = null;
        this.mReConnection_Timeout_Remain_Seconds = 0;
        this.mReConnecting = false;
        this.mHandler.removeCallbacks(this.mReConnect);
    }

    private void chooseNo_WifiDirectExcludeDialog() {
        if (SystemProperties.get("ro.mtk_wfd_sink_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && this.mSinkEnabled) {
            Slog.d(TAG, "[sink] callback onDisplayConnectionFailed()");
            this.mListener.onDisplayConnectionFailed();
            return;
        }
        unadvertiseDisplay();
    }

    private void chooseNo_HDMIExcludeDialog_WfdUpdate() {
        Global.putInt(this.mContext.getContentResolver(), "wifi_display_on", 0);
        updateWfdEnableState();
    }

    private void turnOffHdmi() {
        if (this.mHdmiManager != null) {
            try {
                this.mHdmiManager.enableHdmi(false);
            } catch (RemoteException e) {
                Slog.d(TAG, "hdmi manager RemoteException: " + e.getMessage());
            }
        }
    }

    private void prepareDialog(int dialogID) {
        Resources mResource = Resources.getSystem();
        View checkboxLayout;
        final CheckBox checkbox;
        View progressLayout;
        if (1 == dialogID) {
            this.mWifiDirectExcludeDialog = new AlertDialog.Builder(this.mContext).setTitle(mResource.getString(134545526)).setPositiveButton(mResource.getString(17040419), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Slog.d(WifiDisplayController.TAG, "[Exclude Dialog] disconnect previous Wi-Fi P2p connection");
                    WifiDisplayController.this.mIsConnected_OtherP2p = false;
                    WifiDisplayController.this.mWifiP2pManager.removeGroup(WifiDisplayController.this.mWifiP2pChannel, new ActionListener() {
                        public void onSuccess() {
                            Slog.i(WifiDisplayController.TAG, "Disconnected from previous Wi-Fi P2p device, succeess");
                        }

                        public void onFailure(int reason) {
                            Slog.i(WifiDisplayController.TAG, "Disconnected from previous Wi-Fi P2p device, failure = " + reason);
                        }
                    });
                    WifiDisplayController.this.chooseNo_WifiDirectExcludeDialog();
                    WifiDisplayController.this.mUserDecided = true;
                }
            }).setNegativeButton(mResource.getString(17040373), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Slog.d(WifiDisplayController.TAG, "[Exclude Dialog] keep previous Wi-Fi P2p connection");
                    WifiDisplayController.this.chooseNo_WifiDirectExcludeDialog();
                    WifiDisplayController.this.mUserDecided = true;
                }
            }).setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    Slog.d(WifiDisplayController.TAG, "[Exclude Dialog] onCancel(): keep previous Wi-Fi P2p connection");
                    WifiDisplayController.this.chooseNo_WifiDirectExcludeDialog();
                    WifiDisplayController.this.mUserDecided = true;
                }
            }).setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface arg0) {
                    Slog.d(WifiDisplayController.TAG, "[Exclude Dialog] onDismiss()");
                    if (!WifiDisplayController.this.mUserDecided) {
                        WifiDisplayController.this.chooseNo_WifiDirectExcludeDialog();
                    }
                }
            }).create();
            popupDialog(this.mWifiDirectExcludeDialog);
        } else if (2 == dialogID) {
            this.mHDMIExcludeDialog_WfdUpdate = new AlertDialog.Builder(this.mContext).setTitle(reviseHDMIString(mResource.getString(134545523))).setPositiveButton(mResource.getString(17040419), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "WifiDisplay on, user turn off HDMI");
                    }
                    WifiDisplayController.this.turnOffHdmi();
                    WifiDisplayController.this.enableWifiDisplay();
                    WifiDisplayController.this.mUserDecided = true;
                }
            }).setNegativeButton(mResource.getString(17040373), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "WifiDisplay on, user DON'T turn off HDMI -> turn off WifiDisplay");
                    }
                    WifiDisplayController.this.chooseNo_HDMIExcludeDialog_WfdUpdate();
                    WifiDisplayController.this.mUserDecided = true;
                }
            }).setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "onCancel(): WifiDisplay on, user DON'T turn off HDMI -> turn off WifiDisplay");
                    }
                    WifiDisplayController.this.chooseNo_HDMIExcludeDialog_WfdUpdate();
                    WifiDisplayController.this.mUserDecided = true;
                }
            }).setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface arg0) {
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "onDismiss()");
                    }
                    if (!WifiDisplayController.this.mUserDecided) {
                        WifiDisplayController.this.chooseNo_HDMIExcludeDialog_WfdUpdate();
                    }
                }
            }).create();
            popupDialog(this.mHDMIExcludeDialog_WfdUpdate);
        } else if (3 == dialogID) {
            this.mHDMIExcludeDialog_HDMIUpdate = new AlertDialog.Builder(this.mContext).setTitle(reviseHDMIString(mResource.getString(134545524))).setPositiveButton(mResource.getString(17040419), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "HDMI on, turn off WifiDisplay");
                    }
                    Global.putInt(WifiDisplayController.this.mContext.getContentResolver(), "wifi_display_on", 0);
                    WifiDisplayController.this.mUserDecided = true;
                }
            }).setNegativeButton(mResource.getString(17040373), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "HDMI on, user DON'T turn off WifiDisplay -> turn off HDMI");
                    }
                    WifiDisplayController.this.turnOffHdmi();
                    WifiDisplayController.this.mUserDecided = true;
                }
            }).setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "onCancel(): HDMI on, user DON'T turn off WifiDisplay -> turn off HDMI");
                    }
                    WifiDisplayController.this.turnOffHdmi();
                    WifiDisplayController.this.mUserDecided = true;
                }
            }).setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface arg0) {
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "onDismiss()");
                    }
                    if (!WifiDisplayController.this.mUserDecided) {
                        WifiDisplayController.this.turnOffHdmi();
                    }
                }
            }).create();
            popupDialog(this.mHDMIExcludeDialog_HDMIUpdate);
        } else if (4 == dialogID) {
            this.mReConnecteDialog = new AlertDialog.Builder(this.mContext).setTitle(134545540).setMessage(134545541).setPositiveButton(mResource.getString(17040419), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "user want to reconnect");
                    }
                    WifiDisplayController.this.mReScanning = true;
                    WifiDisplayController.this.updateScanState();
                    WifiDisplayController.this.mReConnection_Timeout_Remain_Seconds = 60;
                    WifiDisplayController.this.mHandler.postDelayed(WifiDisplayController.this.mReConnect, 1000);
                }
            }).setNegativeButton(mResource.getString(17040373), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "user want nothing");
                    }
                }
            }).setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    if (WifiDisplayController.DEBUG) {
                        Slog.d(WifiDisplayController.TAG, "user want nothing");
                    }
                }
            }).create();
            popupDialog(this.mReConnecteDialog);
        } else if (5 == dialogID) {
            checkboxLayout = LayoutInflater.from(this.mContext).inflate(134676491, null);
            checkbox = (CheckBox) checkboxLayout.findViewById(135331903);
            checkbox.setText(134545553);
            this.mChangeResolutionDialog = new AlertDialog.Builder(this.mContext).setView(checkboxLayout).setTitle(134545552).setPositiveButton(mResource.getString(17040419), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    boolean checked = checkbox.isChecked();
                    Slog.d(WifiDisplayController.TAG, "[Change resolution]: ok. checked:" + checked);
                    if (checked) {
                        Global.putInt(WifiDisplayController.this.mContext.getContentResolver(), "wifi_display_change_resolution_remind", 1);
                    } else {
                        Global.putInt(WifiDisplayController.this.mContext.getContentResolver(), "wifi_display_change_resolution_remind", 0);
                    }
                    if (WifiDisplayController.this.mConnectedDevice != null || WifiDisplayController.this.mConnectingDevice != null) {
                        Slog.d(WifiDisplayController.TAG, "-- reconnect for resolution change --");
                        WifiDisplayController.this.disconnect();
                        WifiDisplayController.this.mReconnectForResolutionChange = true;
                    }
                }
            }).setNegativeButton(mResource.getString(17039360), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Slog.d(WifiDisplayController.TAG, "[Change resolution]: cancel");
                    WifiDisplayController.this.revertResolutionChange();
                }
            }).setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    Slog.d(WifiDisplayController.TAG, "[Change resolution]: doesn't choose");
                    WifiDisplayController.this.revertResolutionChange();
                }
            }).create();
            popupDialog(this.mChangeResolutionDialog);
        } else if (6 == dialogID) {
            checkboxLayout = LayoutInflater.from(this.mContext).inflate(134676491, null);
            checkbox = (CheckBox) checkboxLayout.findViewById(135331903);
            checkbox.setText(134545553);
            if (Global.getInt(this.mContext.getContentResolver(), "wifi_display_sound_path_do_not_remind", -1) == -1) {
                checkbox.setChecked(true);
            }
            this.mSoundPathDialog = new AlertDialog.Builder(this.mContext).setView(checkboxLayout).setTitle(134545545).setPositiveButton(mResource.getString(17040419), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    boolean checked = checkbox.isChecked();
                    Slog.d(WifiDisplayController.TAG, "[Sound path reminder]: ok. checked:" + checked);
                    if (checked) {
                        Global.putInt(WifiDisplayController.this.mContext.getContentResolver(), "wifi_display_sound_path_do_not_remind", 1);
                    } else {
                        Global.putInt(WifiDisplayController.this.mContext.getContentResolver(), "wifi_display_sound_path_do_not_remind", 0);
                    }
                }
            }).setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    Slog.d(WifiDisplayController.TAG, "[Sound path reminder]: cancel");
                }
            }).create();
            popupDialog(this.mSoundPathDialog);
        } else if (7 == dialogID) {
            progressLayout = LayoutInflater.from(this.mContext).inflate(134676510, null);
            ((ProgressBar) progressLayout.findViewById(135331952)).setIndeterminate(true);
            ((TextView) progressLayout.findViewById(135331953)).setText(134545556);
            this.mWaitConnectDialog = new AlertDialog.Builder(this.mContext).setView(progressLayout).setNegativeButton(mResource.getString(17039360), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Slog.d(WifiDisplayController.TAG, "[Wait connection]: cancel");
                    WifiDisplayController.this.disconnectWfdSink();
                }
            }).setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    Slog.d(WifiDisplayController.TAG, "[Wait connection]: no choice");
                    WifiDisplayController.this.disconnectWfdSink();
                }
            }).create();
            popupDialog(this.mWaitConnectDialog);
        } else if (8 == dialogID) {
            dismissDialogDetail(this.mWaitConnectDialog);
            this.mConfirmConnectDialog = new AlertDialog.Builder(this.mContext).setTitle(this.mSinkDeviceName + " " + mResource.getString(134545557)).setPositiveButton(mResource.getString(17040372), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Slog.d(WifiDisplayController.TAG, "[Confirm Dialog]: accept");
                    int value = Integer.valueOf(SystemProperties.get("wfd.sink.go_intent", String.valueOf(14))).intValue();
                    Slog.i(WifiDisplayController.TAG, "Sink go_intent:" + value + ", setGCInviteResult(true)");
                    WifiDisplayController.this.mWifiP2pManager.setGCInviteResult(WifiDisplayController.this.mWifiP2pChannel, true, value, null);
                    WifiDisplayController.this.showDialog(9);
                    WifiDisplayController.this.mUserDecided = true;
                }
            }).setNegativeButton(mResource.getString(17040373), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Slog.d(WifiDisplayController.TAG, "[Confirm Dialog]:decline, setGCInviteResult(false)");
                    WifiDisplayController.this.mWifiP2pManager.setGCInviteResult(WifiDisplayController.this.mWifiP2pChannel, false, 0, null);
                    WifiDisplayController.this.disconnectWfdSink();
                    WifiDisplayController.this.mUserDecided = true;
                }
            }).setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    Slog.d(WifiDisplayController.TAG, "[Confirm Dialog]: cancel, setGCInviteResult(false)");
                    WifiDisplayController.this.mWifiP2pManager.setGCInviteResult(WifiDisplayController.this.mWifiP2pChannel, false, 0, null);
                    WifiDisplayController.this.mUserDecided = true;
                    WifiDisplayController.this.disconnectWfdSink();
                }
            }).setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface arg0) {
                    if (!WifiDisplayController.this.mUserDecided) {
                        Slog.d(WifiDisplayController.TAG, "[Confirm Dialog]: dismiss, setGCInviteResult(false)");
                        WifiDisplayController.this.mWifiP2pManager.setGCInviteResult(WifiDisplayController.this.mWifiP2pChannel, false, 0, null);
                        WifiDisplayController.this.disconnectWfdSink();
                    }
                }
            }).create();
            popupDialog(this.mConfirmConnectDialog);
        } else if (9 == dialogID) {
            progressLayout = LayoutInflater.from(this.mContext).inflate(134676510, null);
            ((ProgressBar) progressLayout.findViewById(135331952)).setIndeterminate(true);
            ((TextView) progressLayout.findViewById(135331953)).setText(134545558);
            this.mBuildConnectDialog = new AlertDialog.Builder(this.mContext).setView(progressLayout).setNegativeButton(mResource.getString(17039360), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Slog.d(WifiDisplayController.TAG, "[Build connection]: cancel");
                    WifiDisplayController.this.disconnectWfdSink();
                }
            }).setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    Slog.d(WifiDisplayController.TAG, "[Build connection]: no choice");
                    WifiDisplayController.this.disconnectWfdSink();
                }
            }).create();
            popupDialog(this.mBuildConnectDialog);
        }
    }

    private void popupDialog(AlertDialog dialog) {
        dialog.getWindow().setType(2003);
        LayoutParams attributes = dialog.getWindow().getAttributes();
        attributes.privateFlags |= 16;
        dialog.show();
    }

    private void showDialog(int dialogID) {
        this.mUserDecided = false;
        prepareDialog(dialogID);
    }

    private void dismissDialog() {
        dismissDialogDetail(this.mWifiDirectExcludeDialog);
        dismissDialogDetail(this.mHDMIExcludeDialog_WfdUpdate);
        dismissDialogDetail(this.mHDMIExcludeDialog_HDMIUpdate);
        dismissDialogDetail(this.mReConnecteDialog);
    }

    private void dismissDialogDetail(AlertDialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void notifyClearMotion(boolean connected) {
        if (SystemProperties.get("ro.mtk_clearmotion_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            SystemProperties.set("sys.display.clearMotion.dimmed", connected ? LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON : "0");
            Intent intent = new Intent(WFD_CLEARMOTION_DIMMED);
            intent.addFlags(67108864);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private void updateWifiPowerSavingMode(boolean enable) {
        if (this.mWifiPowerSaving != enable) {
            this.mWifiPowerSaving = enable;
            Slog.d(TAG, "setPowerSavingMode():" + this.mWifiPowerSaving);
            this.mWifiManager.setPowerSavingMode(enable);
        }
    }

    private void checkIfWifiApIs11G() {
        Slog.d(TAG, "checkIfWifiApIs11G()");
        String wifiStatus = this.mWifiManager.getWifiStatus();
        if (wifiStatus == null) {
            Slog.d(TAG, "getWifiStatus() return null.");
            return;
        }
        if (DEBUG) {
            Slog.d(TAG, "getWifiStatus() return: " + wifiStatus);
        }
        for (String token : wifiStatus.split("\n")) {
            if (token.startsWith("group_cipher=")) {
                String cipher = nameValueAssign(token.split("="));
                if (cipher == null) {
                    Slog.e(TAG, "cipher is null.");
                } else {
                    Slog.d(TAG, "cipher is " + cipher);
                    if (cipher.contains("TKIP") || cipher.contains("WEP")) {
                        Toast.makeText(this.mContext, 134545554, 0).show();
                    }
                }
            }
        }
    }

    private String nameValueAssign(String[] nameValue) {
        if (nameValue == null || 2 != nameValue.length) {
            return null;
        }
        return nameValue[1];
    }

    private String reviseHDMIString(String input) {
        try {
            if (this.mHdmiManager != null && (this.mHdmiManager.getDisplayType() == 2 || this.mHdmiManager.getDisplayType() == 1)) {
                return input.replaceAll("HDMI", "MHL");
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HdmiManager.getDisplayType() RemoteException");
        }
        return input;
    }

    public boolean getIfSinkEnabled() {
        Slog.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ",enable = " + this.mSinkEnabled);
        return this.mSinkEnabled;
    }

    public void requestEnableSink(boolean enable) {
        Slog.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ",enable = " + enable + ",Connected = " + this.mIsWFDConnected + ", option = " + SystemProperties.get("ro.mtk_wfd_sink_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) + ", WfdEnabled = " + this.mWfdEnabled);
        if (SystemProperties.get("ro.mtk_wfd_sink_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && this.mSinkEnabled != enable && !this.mIsWFDConnected) {
            if (enable && this.mIsConnected_OtherP2p) {
                Slog.i(TAG, "OtherP2P is connected! Only set variable. Ignore !");
                this.mSinkEnabled = enable;
                enterSinkState(SinkState.SINK_STATE_IDLE);
                return;
            }
            stopWifiScan(enable);
            if (enable) {
                requestStopScan();
            }
            this.mSinkEnabled = enable;
            updateWfdInfo(true);
            if (this.mSinkEnabled) {
                enterSinkState(SinkState.SINK_STATE_IDLE);
            } else {
                requestStartScan();
            }
        }
    }

    public void requestWaitConnection(Surface surface) {
        Slog.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ", mSinkState:" + this.mSinkState);
        if (!isSinkState(SinkState.SINK_STATE_IDLE)) {
            Slog.i(TAG, "State is wrong! Ignore the request !");
        } else if (this.mIsConnected_OtherP2p) {
            Slog.i(TAG, "OtherP2P is connected! Show dialog!");
            this.mHandler.post(new Runnable() {
                public void run() {
                    WifiDisplayController.this.notifyDisplayConnecting();
                }
            });
            showDialog(1);
        } else {
            this.mSinkSurface = surface;
            this.mIsWFDConnected = false;
            this.mSinkDiscoverRetryCount = 5;
            startWaitConnection();
            setSinkMiracastMode();
            enterSinkState(SinkState.SINK_STATE_WAITING_P2P_CONNECTION);
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (WifiDisplayController.this.isSinkState(SinkState.SINK_STATE_WAITING_P2P_CONNECTION)) {
                        WifiDisplayController.this.notifyDisplayConnecting();
                    }
                }
            });
        }
    }

    public void requestSuspendDisplay(boolean suspend, Surface surface) {
        Slog.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ",suspend = " + suspend);
        this.mSinkSurface = surface;
        if (isSinkState(SinkState.SINK_STATE_RTSP_CONNECTED)) {
            boolean z;
            if (this.mRemoteDisplay != null) {
                this.mRemoteDisplay.suspendDisplay(suspend, surface);
            }
            if (suspend) {
                z = false;
            } else {
                z = true;
            }
            blockNotificationList(z);
            return;
        }
        Slog.i(TAG, "State is wrong !!!, SinkState:" + this.mSinkState);
    }

    public void sendUibcInputEvent(String input) {
        if (SystemProperties.get("ro.mtk_wfd_sink_uibc_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && this.mSinkEnabled && this.mRemoteDisplay != null) {
            this.mRemoteDisplay.sendUibcEvent(input);
        }
    }

    private synchronized void disconnectWfdSink() {
        Slog.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ", SinkState = " + this.mSinkState);
        if (isSinkState(SinkState.SINK_STATE_WAITING_P2P_CONNECTION) || isSinkState(SinkState.SINK_STATE_WIFI_P2P_CONNECTED)) {
            this.mHandler.removeCallbacks(this.mGetSinkIpAddr);
            this.mHandler.removeCallbacks(this.mSinkDiscover);
            stopPeerDiscovery();
            Slog.i(TAG, "Disconnected from WFD sink (P2P).");
            deletePersistentGroup();
            enterSinkState(SinkState.SINK_STATE_IDLE);
            updateIfSinkConnected(false);
            this.mWifiP2pManager.setMiracastMode(0);
            this.mHandler.post(new Runnable() {
                public void run() {
                    Slog.d(WifiDisplayController.TAG, "[Sink] callback onDisplayDisconnected()");
                    WifiDisplayController.this.mListener.onDisplayDisconnected();
                }
            });
        } else if (isSinkState(SinkState.SINK_STATE_WAITING_RTSP) || isSinkState(SinkState.SINK_STATE_RTSP_CONNECTED)) {
            if (this.mRemoteDisplay != null) {
                Slog.i(TAG, "before dispose()");
                this.mRemoteDisplay.dispose();
                Slog.i(TAG, "after dispose()");
            }
            this.mHandler.removeCallbacks(this.mRtspSinkTimeout);
            enterSinkState(SinkState.SINK_STATE_WIFI_P2P_CONNECTED);
            this.mHandler.post(new Runnable() {
                public void run() {
                    WifiDisplayController.this.disconnectWfdSink();
                }
            });
        }
        this.mRemoteDisplay = null;
        this.mSinkDeviceName = null;
        this.mSinkMacAddress = null;
        this.mSinkPort = 0;
        this.mSinkIpAddress = null;
        this.mSinkSurface = null;
        this.mHandler.post(new Runnable() {
            public void run() {
                WifiDisplayController.this.dismissDialogDetail(WifiDisplayController.this.mWaitConnectDialog);
                WifiDisplayController.this.dismissDialogDetail(WifiDisplayController.this.mConfirmConnectDialog);
                WifiDisplayController.this.dismissDialogDetail(WifiDisplayController.this.mBuildConnectDialog);
                if (WifiDisplayController.this.mWifiDirectExcludeDialog != null && WifiDisplayController.this.mWifiDirectExcludeDialog.isShowing()) {
                    WifiDisplayController.this.chooseNo_WifiDirectExcludeDialog();
                }
                WifiDisplayController.this.dismissDialogDetail(WifiDisplayController.this.mWifiDirectExcludeDialog);
            }
        });
    }

    private void removeSpecificPersistentGroup() {
        final WifiP2pDevice targetDevice = this.mConnectingDevice != null ? this.mConnectingDevice : this.mConnectedDevice;
        if (targetDevice != null && targetDevice.deviceName.contains("BRAVIA")) {
            Slog.d(TAG, "removeSpecificPersistentGroup");
            this.mWifiP2pManager.requestPersistentGroupInfo(this.mWifiP2pChannel, new PersistentGroupInfoListener() {
                public void onPersistentGroupInfoAvailable(WifiP2pGroupList groups) {
                    Slog.d(WifiDisplayController.TAG, "onPersistentGroupInfoAvailable()");
                    for (WifiP2pGroup g : groups.getGroupList()) {
                        if (targetDevice.deviceAddress.equalsIgnoreCase(g.getOwner().deviceAddress)) {
                            Slog.d(WifiDisplayController.TAG, "deletePersistentGroup(), net id:" + g.getNetworkId());
                            WifiDisplayController.this.mWifiP2pManager.deletePersistentGroup(WifiDisplayController.this.mWifiP2pChannel, g.getNetworkId(), null);
                        }
                    }
                }
            });
        }
    }

    private void deletePersistentGroup() {
        Slog.d(TAG, "deletePersistentGroup");
        if (this.mSinkP2pGroup != null) {
            Slog.d(TAG, "mSinkP2pGroup network id: " + this.mSinkP2pGroup.getNetworkId());
            if (this.mSinkP2pGroup.getNetworkId() >= 0) {
                this.mWifiP2pManager.deletePersistentGroup(this.mWifiP2pChannel, this.mSinkP2pGroup.getNetworkId(), null);
            }
            this.mSinkP2pGroup = null;
        }
    }

    private void handleSinkP2PConnection(NetworkInfo networkInfo) {
        Slog.i(TAG, "handleSinkP2PConnection(), sinkState:" + this.mSinkState);
        if (this.mWifiP2pManager == null || !networkInfo.isConnected()) {
            if (isSinkState(SinkState.SINK_STATE_WAITING_P2P_CONNECTION) || isSinkState(SinkState.SINK_STATE_WIFI_P2P_CONNECTED)) {
                disconnectWfdSink();
            }
        } else if (isSinkState(SinkState.SINK_STATE_WAITING_P2P_CONNECTION)) {
            this.mWifiP2pManager.requestGroupInfo(this.mWifiP2pChannel, new GroupInfoListener() {
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    Slog.i(WifiDisplayController.TAG, "onGroupInfoAvailable(), mSinkState:" + WifiDisplayController.this.mSinkState);
                    if (!WifiDisplayController.this.isSinkState(SinkState.SINK_STATE_WAITING_P2P_CONNECTION)) {
                        return;
                    }
                    if (group == null) {
                        Slog.i(WifiDisplayController.TAG, "Error: group is null !!!");
                        return;
                    }
                    WifiDisplayController.this.mSinkP2pGroup = group;
                    boolean found = false;
                    if (group.getOwner().deviceAddress.equals(WifiDisplayController.this.mThisDevice.deviceAddress)) {
                        Slog.i(WifiDisplayController.TAG, "group owner is my self !");
                        for (WifiP2pDevice c : group.getClientList()) {
                            Slog.i(WifiDisplayController.TAG, "Client device:" + c);
                            if (WifiDisplayController.this.isWifiDisplaySource(c) && WifiDisplayController.this.mSinkDeviceName.equals(c.deviceName)) {
                                WifiDisplayController.this.mSinkMacAddress = c.deviceAddress;
                                WifiDisplayController.this.mSinkPort = c.wfdInfo.getControlPort();
                                Slog.i(WifiDisplayController.TAG, "Found ! Sink name:" + WifiDisplayController.this.mSinkDeviceName + ",mac address:" + WifiDisplayController.this.mSinkMacAddress + ",port:" + WifiDisplayController.this.mSinkPort);
                                found = true;
                                break;
                            }
                        }
                    } else {
                        Slog.i(WifiDisplayController.TAG, "group owner is not my self ! So I am GC.");
                        WifiDisplayController.this.mSinkMacAddress = group.getOwner().deviceAddress;
                        WifiDisplayController.this.mSinkPort = group.getOwner().wfdInfo.getControlPort();
                        Slog.i(WifiDisplayController.TAG, "Sink name:" + WifiDisplayController.this.mSinkDeviceName + ",mac address:" + WifiDisplayController.this.mSinkMacAddress + ",port:" + WifiDisplayController.this.mSinkPort);
                        found = true;
                    }
                    if (found) {
                        WifiDisplayController.this.mSinkIpRetryCount = 50;
                        WifiDisplayController.this.enterSinkState(SinkState.SINK_STATE_WIFI_P2P_CONNECTED);
                        WifiDisplayController.this.mHandler.postDelayed(WifiDisplayController.this.mGetSinkIpAddr, 300);
                    }
                }
            });
        }
    }

    private boolean isWifiDisplaySource(WifiP2pDevice device) {
        boolean result;
        if (device.wfdInfo != null && device.wfdInfo.isWfdEnabled() && device.wfdInfo.isSessionAvailable()) {
            result = isSourceDeviceType(device.wfdInfo.getDeviceType());
        } else {
            result = false;
        }
        if (!result) {
            Slog.e(TAG, "This is not WFD source device !!!!!!");
        }
        return result;
    }

    private void notifyDisplayConnecting() {
        WifiDisplay display = new WifiDisplay("Temp address", "WiFi Display Device", null, true, true, false);
        Slog.d(TAG, "[sink] callback onDisplayConnecting()");
        this.mListener.onDisplayConnecting(display);
    }

    private boolean isSourceDeviceType(int deviceType) {
        return deviceType == 0 || deviceType == 3;
    }

    private void startWaitConnection() {
        Slog.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ", mSinkState:" + this.mSinkState + ", retryCount:" + this.mSinkDiscoverRetryCount);
        this.mWifiP2pManager.discoverPeers(this.mWifiP2pChannel, new ActionListener() {
            public void onSuccess() {
                if (WifiDisplayController.this.isSinkState(SinkState.SINK_STATE_WAITING_P2P_CONNECTION)) {
                    Slog.d(WifiDisplayController.TAG, "[sink] succeed for discoverPeers()");
                    WifiDisplayController.this.showDialog(7);
                }
            }

            public void onFailure(int reason) {
                if (WifiDisplayController.this.isSinkState(SinkState.SINK_STATE_WAITING_P2P_CONNECTION)) {
                    Slog.e(WifiDisplayController.TAG, "[sink] failed for discoverPeers(), reason:" + reason + ", retryCount:" + WifiDisplayController.this.mSinkDiscoverRetryCount);
                    if (reason != 2 || WifiDisplayController.this.mSinkDiscoverRetryCount <= 0) {
                        WifiDisplayController.this.enterSinkState(SinkState.SINK_STATE_IDLE);
                        Slog.d(WifiDisplayController.TAG, "[sink] callback onDisplayConnectionFailed()");
                        WifiDisplayController.this.mListener.onDisplayConnectionFailed();
                    } else {
                        WifiDisplayController wifiDisplayController = WifiDisplayController.this;
                        wifiDisplayController.mSinkDiscoverRetryCount = wifiDisplayController.mSinkDiscoverRetryCount - 1;
                        WifiDisplayController.this.mHandler.postDelayed(WifiDisplayController.this.mSinkDiscover, 100);
                    }
                }
            }
        });
    }

    private void connectRtsp() {
        Slog.d(TAG, "connectRtsp(), mSinkState:" + this.mSinkState);
        this.mRemoteDisplay = RemoteDisplay.connect(this.mSinkIpAddress, this.mSinkSurface, new android.media.RemoteDisplay.Listener() {
            public void onDisplayConnected(Surface surface, int width, int height, int flags, int session) {
                Slog.i(WifiDisplayController.TAG, "Opened RTSP connection! w:" + width + ",h:" + height);
                WifiDisplayController.this.dismissDialogDetail(WifiDisplayController.this.mBuildConnectDialog);
                WifiDisplayController.this.enterSinkState(SinkState.SINK_STATE_RTSP_CONNECTED);
                WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspSinkTimeout);
                WifiDisplay display = new WifiDisplay(WifiDisplayController.this.mSinkMacAddress, WifiDisplayController.this.mSinkDeviceName, null, true, true, false);
                if (width < height) {
                    WifiDisplayController.this.sendPortraitIntent();
                }
                Slog.d(WifiDisplayController.TAG, "[sink] callback onDisplayConnected(), addr:" + WifiDisplayController.this.mSinkMacAddress + ", name:" + WifiDisplayController.this.mSinkDeviceName);
                WifiDisplayController.this.updateIfSinkConnected(true);
                WifiDisplayController.this.mListener.onDisplayConnected(display, null, 0, 0, 0);
            }

            public void onDisplayDisconnected() {
                Slog.i(WifiDisplayController.TAG, "Closed RTSP connection! mSinkState:" + WifiDisplayController.this.mSinkState);
                if (WifiDisplayController.this.isSinkState(SinkState.SINK_STATE_WAITING_RTSP) || WifiDisplayController.this.isSinkState(SinkState.SINK_STATE_RTSP_CONNECTED)) {
                    WifiDisplayController.this.dismissDialogDetail(WifiDisplayController.this.mBuildConnectDialog);
                    WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspSinkTimeout);
                    WifiDisplayController.this.disconnectWfdSink();
                }
            }

            public void onDisplayError(int error) {
                Slog.i(WifiDisplayController.TAG, "Lost RTSP connection! mSinkState:" + WifiDisplayController.this.mSinkState);
                if (WifiDisplayController.this.isSinkState(SinkState.SINK_STATE_WAITING_RTSP) || WifiDisplayController.this.isSinkState(SinkState.SINK_STATE_RTSP_CONNECTED)) {
                    WifiDisplayController.this.dismissDialogDetail(WifiDisplayController.this.mBuildConnectDialog);
                    WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspSinkTimeout);
                    WifiDisplayController.this.disconnectWfdSink();
                }
            }

            public void onDisplayKeyEvent(int keyCode, int flags) {
                Slog.d(WifiDisplayController.TAG, "onDisplayKeyEvent:");
            }

            public void onDisplayGenericMsgEvent(int event) {
            }
        }, this.mHandler);
        enterSinkState(SinkState.SINK_STATE_WAITING_RTSP);
        this.mHandler.postDelayed(this.mRtspSinkTimeout, (long) ((this.mWifiDisplayCertMode ? 120 : 10) * 1000));
    }

    private void blockNotificationList(boolean block) {
        Slog.i(TAG, "blockNotificationList(), block:" + block);
        if (block) {
            this.mStatusBarManager.disable(DumpState.DUMP_INSTALLS);
        } else {
            this.mStatusBarManager.disable(0);
        }
    }

    private void enterSinkState(SinkState state) {
        Slog.i(TAG, "enterSinkState()," + this.mSinkState + "->" + state);
        this.mSinkState = state;
    }

    private boolean isSinkState(SinkState state) {
        return this.mSinkState == state;
    }

    private void updateIfSinkConnected(boolean connected) {
        boolean z = false;
        if (this.mIsWFDConnected != connected) {
            boolean z2;
            this.mIsWFDConnected = connected;
            blockNotificationList(connected);
            String str = TAG;
            StringBuilder append = new StringBuilder().append("Set session available as ");
            if (connected) {
                z2 = false;
            } else {
                z2 = true;
            }
            Slog.i(str, append.append(z2).toString());
            WifiP2pWfdInfo wifiP2pWfdInfo = this.mWfdInfo;
            if (!connected) {
                z = true;
            }
            wifiP2pWfdInfo.setSessionAvailable(z);
            this.mWifiP2pManager.setWFDInfo(this.mWifiP2pChannel, this.mWfdInfo, null);
            if (this.mWakeLockSink != null) {
                if (connected) {
                    this.mWakeLockSink.acquire();
                } else {
                    this.mWakeLockSink.release();
                }
            }
            getAudioFocus(connected);
        }
    }

    private void getAudioFocus(boolean grab) {
        if (!grab) {
            this.mAudioManager.abandonAudioFocus(this.mAudioFocusListener);
        } else if (this.mAudioManager.requestAudioFocus(this.mAudioFocusListener, 3, 1) == 0) {
            Slog.e(TAG, "requestAudioFocus() FAIL !!!");
        }
    }

    private void setSinkMiracastMode() {
        Slog.i(TAG, "setSinkMiracastMode(), freq:" + this.mWifiApFreq);
        if (this.mWifiApConnected) {
            this.mWifiP2pManager.setMiracastMode(2, this.mWifiApFreq);
        } else {
            this.mWifiP2pManager.setMiracastMode(2);
        }
    }

    private void notifyApDisconnected() {
        Slog.e(TAG, "notifyApDisconnected()");
        Resources r = Resources.getSystem();
        Context context = this.mContext;
        Object[] objArr = new Object[1];
        objArr[0] = this.mWifiApSsid;
        Toast.makeText(context, r.getString(134545666, objArr), 0).show();
        showNotification(134545667, 134545668);
    }

    private void startChannelConflictProcedure() {
        Slog.i(TAG, "startChannelConflictProcedure(), mChannelConflictState:" + this.mChannelConflictState + ",mWifiApConnected:" + this.mWifiApConnected);
        if (!isCCState(ChannelConflictState.STATE_IDLE)) {
            Slog.i(TAG, "State is wrong !!");
        } else if (this.mWifiApConnected) {
            if (wifiApHasSameFreq()) {
                this.mNetworkId = this.mWifiNetworkId;
                Slog.i(TAG, "Same Network Id:" + this.mNetworkId);
                this.mDisplayApToast = false;
                this.mWifiManager.disconnect();
                enterCCState(ChannelConflictState.STATE_AP_DISCONNECTING);
            } else {
                this.mNetworkId = getSameFreqNetworkId();
                if (this.mNetworkId == -1) {
                    this.mWifiP2pManager.setFreqConflictExResult(this.mWifiP2pChannel, false, null);
                } else {
                    this.mDisplayApToast = true;
                    this.mWifiManager.disconnect();
                    enterCCState(ChannelConflictState.STATE_AP_DISCONNECTING);
                }
            }
        } else {
            Slog.i(TAG, "No WiFi AP Connected. Wrong !!");
        }
    }

    private void handleChannelConflictProcedure(ChannelConflictEvt event) {
        if (!isCCState(null) && !isCCState(ChannelConflictState.STATE_IDLE)) {
            Slog.i(TAG, "handleChannelConflictProcedure(), evt:" + event + ", ccState:" + this.mChannelConflictState);
            if (isCCState(ChannelConflictState.STATE_AP_DISCONNECTING)) {
                if (event == ChannelConflictEvt.EVT_AP_DISCONNECTED) {
                    this.mWifiP2pManager.setFreqConflictExResult(this.mWifiP2pChannel, true, null);
                    enterCCState(ChannelConflictState.STATE_WFD_CONNECTING);
                } else {
                    this.mWifiP2pManager.setFreqConflictExResult(this.mWifiP2pChannel, false, null);
                    enterCCState(ChannelConflictState.STATE_IDLE);
                }
            } else if (isCCState(ChannelConflictState.STATE_WFD_CONNECTING)) {
                if (event == ChannelConflictEvt.EVT_WFD_P2P_CONNECTED) {
                    Slog.i(TAG, "connect AP, mNetworkId:" + this.mNetworkId);
                    this.mWifiManager.connect(this.mNetworkId, null);
                    enterCCState(ChannelConflictState.STATE_AP_CONNECTING);
                } else {
                    enterCCState(ChannelConflictState.STATE_IDLE);
                }
            } else if (isCCState(ChannelConflictState.STATE_AP_CONNECTING)) {
                if (event == ChannelConflictEvt.EVT_AP_CONNECTED) {
                    if (this.mDisplayApToast) {
                        Resources r = Resources.getSystem();
                        Context context = this.mContext;
                        Object[] objArr = new Object[1];
                        objArr[0] = this.mWifiApSsid;
                        Toast.makeText(context, r.getString(134545664, objArr), 0).show();
                    }
                    enterCCState(ChannelConflictState.STATE_IDLE);
                } else {
                    enterCCState(ChannelConflictState.STATE_IDLE);
                }
            }
        }
    }

    private boolean wifiApHasSameFreq() {
        Slog.i(TAG, "wifiApHasSameFreq()");
        if (this.mWifiApSsid == null || this.mWifiApSsid.length() < 2) {
            Slog.e(TAG, "mWifiApSsid is invalid !!");
            return false;
        }
        String apSsid = this.mWifiApSsid.substring(1, this.mWifiApSsid.length() - 1);
        List<ScanResult> results = this.mWifiManager.getScanResults();
        boolean found = false;
        if (results != null) {
            for (ScanResult result : results) {
                Slog.i(TAG, "SSID:" + result.SSID + ",Freq:" + result.frequency + ",Level:" + result.level + ",BSSID:" + result.BSSID);
                if (result.SSID != null && result.SSID.length() != 0 && !result.capabilities.contains("[IBSS]") && result.SSID.equals(apSsid) && result.frequency == this.mP2pOperFreq) {
                    found = true;
                    break;
                }
            }
        }
        Slog.i(TAG, "AP SSID:" + apSsid + ", sameFreq:" + found);
        return found;
    }

    private int getSameFreqNetworkId() {
        Slog.i(TAG, "getSameFreqNetworkId()");
        List<WifiConfiguration> everConnecteds = this.mWifiManager.getConfiguredNetworks();
        List<ScanResult> results = this.mWifiManager.getScanResults();
        if (results == null || everConnecteds == null) {
            Slog.i(TAG, "results:" + results + ",everConnecteds:" + everConnecteds);
            return -1;
        }
        int maxRssi = -128;
        int selectedNetworkId = -1;
        for (WifiConfiguration everConnected : everConnecteds) {
            String trim = everConnected.SSID.substring(1, everConnected.SSID.length() - 1);
            Slog.i(TAG, "SSID:" + trim + ",NetId:" + everConnected.networkId);
            for (ScanResult result : results) {
                if (result.SSID != null && result.SSID.length() != 0 && !result.capabilities.contains("[IBSS]") && trim.equals(result.SSID) && result.frequency == this.mP2pOperFreq && result.level > maxRssi) {
                    selectedNetworkId = everConnected.networkId;
                    maxRssi = result.level;
                    break;
                }
            }
        }
        Slog.i(TAG, "Selected Network Id:" + selectedNetworkId);
        return selectedNetworkId;
    }

    private void enterCCState(ChannelConflictState state) {
        Slog.i(TAG, "enterCCState()," + this.mChannelConflictState + "->" + state);
        this.mChannelConflictState = state;
    }

    private boolean isCCState(ChannelConflictState state) {
        return this.mChannelConflictState == state;
    }
}
