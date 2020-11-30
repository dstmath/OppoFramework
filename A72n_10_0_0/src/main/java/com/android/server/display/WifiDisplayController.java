package com.android.server.display;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.display.WifiDisplay;
import android.hardware.display.WifiDisplaySessionInfo;
import android.media.RemoteDisplay;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pWfdInfo;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Slog;
import android.view.Surface;
import com.android.internal.util.DumpUtils;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.mediatek.server.display.MtkWifiDisplayController;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Objects;

public final class WifiDisplayController implements DumpUtils.Dump {
    private static final int CONNECTION_TIMEOUT_SECONDS = 30;
    private static final int CONNECT_MAX_RETRIES = 3;
    private static final int CONNECT_RETRY_DELAY_MILLIS = 500;
    private static final boolean DEBUG = true;
    private static final int DEFAULT_CONTROL_PORT = 7236;
    private static final int DISCOVER_PEERS_INTERVAL_MILLIS = 10000;
    private static final int MAX_THROUGHPUT = 50;
    private static final int RTSP_TIMEOUT_SECONDS = 30;
    private static final int RTSP_TIMEOUT_SECONDS_CERT_MODE = 120;
    private static final String TAG = "WifiDisplayController";
    private long connectedTime;
    private WifiDisplay mAdvertisedDisplay;
    private int mAdvertisedDisplayFlags;
    private int mAdvertisedDisplayHeight;
    private Surface mAdvertisedDisplaySurface;
    private int mAdvertisedDisplayWidth;
    public final ArrayList<WifiP2pDevice> mAvailableWifiDisplayPeers = new ArrayList<>();
    private WifiP2pDevice mCancelingDevice;
    public WifiP2pDevice mConnectedDevice;
    private WifiP2pGroup mConnectedDeviceGroupInfo;
    public WifiP2pDevice mConnectingDevice;
    private int mConnectionRetriesLeft;
    private final Runnable mConnectionTimeout = new Runnable() {
        /* class com.android.server.display.WifiDisplayController.AnonymousClass17 */

        public void run() {
            if (WifiDisplayController.this.mConnectingDevice != null && WifiDisplayController.this.mConnectingDevice == WifiDisplayController.this.mDesiredDevice) {
                Slog.i(WifiDisplayController.TAG, "Timed out waiting for Wifi display connection after 30 seconds: " + WifiDisplayController.this.mConnectingDevice.deviceName);
                WifiDisplayController.this.handleConnectionFailure(true);
                WifiDisplayController.this.mWifiDisplayUsageHelper.wfdConnectedFailed("P2P_Connection_Timeout", WifiDisplayController.this.mConnectingDevice, WifiDisplayController.this.mConnectedDeviceGroupInfo, WifiDisplayController.this.mContext);
            }
        }
    };
    private final Context mContext;
    private WifiP2pDevice mDesiredDevice;
    private WifiP2pDevice mDisconnectingDevice;
    private final Runnable mDiscoverPeers = new Runnable() {
        /* class com.android.server.display.WifiDisplayController.AnonymousClass16 */

        public void run() {
            WifiDisplayController.this.tryDiscoverPeers();
        }
    };
    private boolean mDiscoverPeersInProgress;
    private final Handler mHandler;
    private final Listener mListener;
    private MtkWifiDisplayController mMtkController;
    private NetworkInfo mNetworkInfo;
    private RemoteDisplay mRemoteDisplay;
    private boolean mRemoteDisplayConnected;
    private String mRemoteDisplayInterface;
    private final Runnable mRtspTimeout = new Runnable() {
        /* class com.android.server.display.WifiDisplayController.AnonymousClass18 */

        public void run() {
            if (WifiDisplayController.this.mConnectedDevice != null && WifiDisplayController.this.mRemoteDisplay != null && !WifiDisplayController.this.mRemoteDisplayConnected) {
                Slog.i(WifiDisplayController.TAG, "Timed out waiting for Wifi display RTSP connection after 30 seconds: " + WifiDisplayController.this.mConnectedDevice.deviceName);
                WifiDisplayController.this.handleConnectionFailure(true);
                WifiDisplayController.this.mWifiDisplayUsageHelper.wfdConnectedFailed("RTSP_TimeOut", WifiDisplayController.this.mConnectedDevice, WifiDisplayController.this.mConnectedDeviceGroupInfo, WifiDisplayController.this.mContext);
            }
        }
    };
    private boolean mScanRequested;
    private WifiP2pDevice mThisDevice;
    private boolean mWfdEnabled;
    private boolean mWfdEnabling;
    private boolean mWifiDisplayCertMode;
    private boolean mWifiDisplayOnSetting;
    OppoWifiDisplayUsageHelper mWifiDisplayUsageHelper = new OppoWifiDisplayUsageHelper();
    private int mWifiDisplayWpsConfig = 4;
    private final WifiP2pManager.Channel mWifiP2pChannel;
    private boolean mWifiP2pEnabled;
    private final WifiP2pManager mWifiP2pManager;
    private final BroadcastReceiver mWifiP2pReceiver = new BroadcastReceiver() {
        /* class com.android.server.display.WifiDisplayController.AnonymousClass21 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.net.wifi.p2p.STATE_CHANGED")) {
                boolean enabled = true;
                if (intent.getIntExtra("wifi_p2p_state", 1) != 2) {
                    enabled = false;
                }
                Slog.d(WifiDisplayController.TAG, "Received WIFI_P2P_STATE_CHANGED_ACTION: enabled=" + enabled);
                WifiDisplayController.this.handleStateChanged(enabled);
            } else if (action.equals("android.net.wifi.p2p.PEERS_CHANGED")) {
                Slog.d(WifiDisplayController.TAG, "Received WIFI_P2P_PEERS_CHANGED_ACTION.");
                WifiDisplayController.this.handlePeersChanged();
            } else if (action.equals("android.net.wifi.p2p.CONNECTION_STATE_CHANGE")) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                Slog.d(WifiDisplayController.TAG, "Received WIFI_P2P_CONNECTION_CHANGED_ACTION: networkInfo=" + networkInfo);
                WifiDisplayController.this.handleConnectionChanged(networkInfo);
            } else if (action.equals("android.net.wifi.p2p.THIS_DEVICE_CHANGED")) {
                WifiDisplayController.this.mThisDevice = (WifiP2pDevice) intent.getParcelableExtra("wifiP2pDevice");
                Slog.d(WifiDisplayController.TAG, "Received WIFI_P2P_THIS_DEVICE_CHANGED_ACTION: mThisDevice= " + WifiDisplayController.this.mThisDevice);
            }
        }
    };

    public interface Listener {
        void onDisplayChanged(WifiDisplay wifiDisplay);

        void onDisplayConnected(WifiDisplay wifiDisplay, Surface surface, int i, int i2, int i3);

        void onDisplayConnecting(WifiDisplay wifiDisplay);

        void onDisplayConnectionFailed();

        void onDisplayDisconnected();

        void onDisplaySessionInfo(WifiDisplaySessionInfo wifiDisplaySessionInfo);

        void onFeatureStateChanged(int i);

        void onScanFinished();

        void onScanResults(WifiDisplay[] wifiDisplayArr);

        void onScanStarted();
    }

    static /* synthetic */ int access$3520(WifiDisplayController x0, int x1) {
        int i = x0.mConnectionRetriesLeft - x1;
        x0.mConnectionRetriesLeft = i;
        return i;
    }

    public WifiDisplayController(Context context, Handler handler, Listener listener) {
        this.mContext = context;
        this.mHandler = handler;
        this.mListener = listener;
        this.mWifiP2pManager = (WifiP2pManager) context.getSystemService("wifip2p");
        this.mWifiP2pChannel = this.mWifiP2pManager.initialize(context, handler.getLooper(), null);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.p2p.STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.p2p.PEERS_CHANGED");
        intentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.p2p.THIS_DEVICE_CHANGED");
        context.registerReceiver(this.mWifiP2pReceiver, intentFilter, null, this.mHandler);
        ContentObserver settingsObserver = new ContentObserver(this.mHandler) {
            /* class com.android.server.display.WifiDisplayController.AnonymousClass1 */

            public void onChange(boolean selfChange, Uri uri) {
                WifiDisplayController.this.updateSettings();
            }
        };
        ContentResolver resolver = this.mContext.getContentResolver();
        resolver.registerContentObserver(Settings.Global.getUriFor("wifi_display_on"), false, settingsObserver);
        resolver.registerContentObserver(Settings.Global.getUriFor("wifi_display_certification_on"), false, settingsObserver);
        resolver.registerContentObserver(Settings.Global.getUriFor("wifi_display_wps_config"), false, settingsObserver);
        updateSettings();
        this.mMtkController = new MtkWifiDisplayController(this.mContext, this.mHandler, this);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSettings() {
        ContentResolver resolver = this.mContext.getContentResolver();
        boolean z = false;
        this.mWifiDisplayOnSetting = Settings.Global.getInt(resolver, "wifi_display_on", 0) != 0;
        if (Settings.Global.getInt(resolver, "wifi_display_certification_on", 0) != 0) {
            z = true;
        }
        this.mWifiDisplayCertMode = z;
        this.mWifiDisplayWpsConfig = 4;
        if (this.mWifiDisplayCertMode) {
            this.mWifiDisplayWpsConfig = Settings.Global.getInt(resolver, "wifi_display_wps_config", 4);
        }
        updateWfdEnableState();
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
        pw.println("mAvailableWifiDisplayPeers: size=" + this.mAvailableWifiDisplayPeers.size());
        Iterator<WifiP2pDevice> it = this.mAvailableWifiDisplayPeers.iterator();
        while (it.hasNext()) {
            pw.println("  " + describeWifiP2pDevice(it.next()));
        }
    }

    public void requestStartScan() {
        if (!this.mScanRequested) {
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
        Iterator<WifiP2pDevice> it = this.mAvailableWifiDisplayPeers.iterator();
        while (it.hasNext()) {
            WifiP2pDevice device = it.next();
            if (device.deviceAddress.equals(address)) {
                connect(device);
            }
        }
    }

    public void requestPause() {
        RemoteDisplay remoteDisplay = this.mRemoteDisplay;
        if (remoteDisplay != null) {
            remoteDisplay.pause();
        }
    }

    public void requestResume() {
        RemoteDisplay remoteDisplay = this.mRemoteDisplay;
        if (remoteDisplay != null) {
            remoteDisplay.resume();
        }
    }

    public void requestDisconnect() {
        disconnect();
    }

    private void updateWfdEnableState() {
        if (!this.mWifiDisplayOnSetting || !this.mWifiP2pEnabled) {
            if (this.mWfdEnabled || this.mWfdEnabling) {
                WifiP2pWfdInfo wfdInfo = new WifiP2pWfdInfo();
                wfdInfo.setWfdEnabled(false);
                this.mWifiP2pManager.setWFDInfo(this.mWifiP2pChannel, wfdInfo, new WifiP2pManager.ActionListener() {
                    /* class com.android.server.display.WifiDisplayController.AnonymousClass3 */

                    public void onSuccess() {
                        Slog.d(WifiDisplayController.TAG, "Successfully set WFD info.");
                    }

                    public void onFailure(int reason) {
                        Slog.d(WifiDisplayController.TAG, "Failed to set WFD info with reason " + reason + ".");
                    }
                });
            }
            this.mWfdEnabling = false;
            this.mWfdEnabled = false;
            reportFeatureState();
            updateScanState();
            disconnect();
        } else if (!this.mWfdEnabled && !this.mWfdEnabling) {
            this.mWfdEnabling = true;
            WifiP2pWfdInfo wfdInfo2 = new WifiP2pWfdInfo();
            wfdInfo2.setWfdEnabled(true);
            wfdInfo2.setDeviceType(0);
            wfdInfo2.setSessionAvailable(true);
            wfdInfo2.setControlPort(DEFAULT_CONTROL_PORT);
            wfdInfo2.setMaxThroughput(50);
            this.mWifiP2pManager.setWFDInfo(this.mWifiP2pChannel, wfdInfo2, new WifiP2pManager.ActionListener() {
                /* class com.android.server.display.WifiDisplayController.AnonymousClass2 */

                public void onSuccess() {
                    Slog.d(WifiDisplayController.TAG, "Successfully set WFD info.");
                    if (WifiDisplayController.this.mWfdEnabling) {
                        WifiDisplayController.this.mWfdEnabling = false;
                        WifiDisplayController.this.mWfdEnabled = true;
                        WifiDisplayController.this.reportFeatureState();
                        WifiDisplayController.this.updateScanState();
                    }
                }

                public void onFailure(int reason) {
                    Slog.d(WifiDisplayController.TAG, "Failed to set WFD info with reason " + reason + ".");
                    WifiDisplayController.this.mWfdEnabling = false;
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportFeatureState() {
        final int featureState = computeFeatureState();
        this.mHandler.post(new Runnable() {
            /* class com.android.server.display.WifiDisplayController.AnonymousClass4 */

            public void run() {
                WifiDisplayController.this.mListener.onFeatureStateChanged(featureState);
            }
        });
    }

    private int computeFeatureState() {
        if (!this.mWifiP2pEnabled) {
            if (!this.mWifiDisplayOnSetting) {
                return 1;
            }
            Slog.d(TAG, "Wifi p2p is disabled, update WIFI_DISPLAY_ON as false.");
            Settings.Global.putInt(this.mContext.getContentResolver(), "wifi_display_on", 0);
            this.mWifiDisplayOnSetting = false;
            return 1;
        } else if (this.mWifiDisplayOnSetting) {
            return 3;
        } else {
            return 2;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateScanState() {
        if (!this.mScanRequested || !this.mWfdEnabled || this.mDesiredDevice != null) {
            if (this.mDiscoverPeersInProgress) {
                this.mHandler.removeCallbacks(this.mDiscoverPeers);
                WifiP2pDevice wifiP2pDevice = this.mDesiredDevice;
                if (wifiP2pDevice == null || wifiP2pDevice == this.mConnectedDevice) {
                    Slog.i(TAG, "Stopping Wifi display scan.");
                    this.mDiscoverPeersInProgress = false;
                    stopPeerDiscovery();
                    handleScanFinished();
                }
            }
        } else if (!this.mDiscoverPeersInProgress) {
            Slog.i(TAG, "Starting Wifi display scan.");
            this.mDiscoverPeersInProgress = true;
            handleScanStarted();
            tryDiscoverPeers();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void discoverPeers() {
        if (!this.mDiscoverPeersInProgress) {
            Slog.i(TAG, "Starting Wifi display scan.");
            this.mDiscoverPeersInProgress = true;
            handleScanStarted();
            tryDiscoverPeers();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tryDiscoverPeers() {
        this.mWifiP2pManager.discoverPeers(this.mWifiP2pChannel, new WifiP2pManager.ActionListener() {
            /* class com.android.server.display.WifiDisplayController.AnonymousClass5 */

            public void onSuccess() {
                Slog.d(WifiDisplayController.TAG, "Discover peers succeeded.  Requesting peers now.");
                if (WifiDisplayController.this.mDiscoverPeersInProgress) {
                    WifiDisplayController.this.requestPeers();
                }
            }

            public void onFailure(int reason) {
                Slog.d(WifiDisplayController.TAG, "Discover peers failed with reason " + reason + ".");
            }
        });
        this.mHandler.postDelayed(this.mDiscoverPeers, 10000);
    }

    private void stopPeerDiscovery() {
        this.mWifiP2pManager.stopPeerDiscovery(this.mWifiP2pChannel, new WifiP2pManager.ActionListener() {
            /* class com.android.server.display.WifiDisplayController.AnonymousClass6 */

            public void onSuccess() {
                Slog.d(WifiDisplayController.TAG, "Stop peer discovery succeeded.");
            }

            public void onFailure(int reason) {
                Slog.d(WifiDisplayController.TAG, "Stop peer discovery failed with reason " + reason + ".");
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestPeers() {
        this.mWifiP2pManager.requestPeers(this.mWifiP2pChannel, new WifiP2pManager.PeerListListener() {
            /* class com.android.server.display.WifiDisplayController.AnonymousClass7 */

            public void onPeersAvailable(WifiP2pDeviceList peers) {
                Slog.d(WifiDisplayController.TAG, "Received list of peers.");
                WifiDisplayController.this.mAvailableWifiDisplayPeers.clear();
                for (WifiP2pDevice device : peers.getDeviceList()) {
                    Slog.d(WifiDisplayController.TAG, "  " + WifiDisplayController.describeWifiP2pDevice(device));
                    if (WifiDisplayController.isWifiDisplay(device)) {
                        WifiDisplayController.this.mAvailableWifiDisplayPeers.add(device);
                    }
                }
                if (WifiDisplayController.this.mDiscoverPeersInProgress) {
                    WifiDisplayController.this.handleScanResults();
                }
            }
        });
    }

    private void handleScanStarted() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.display.WifiDisplayController.AnonymousClass8 */

            public void run() {
                WifiDisplayController.this.mListener.onScanStarted();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScanResults() {
        int count = this.mAvailableWifiDisplayPeers.size();
        final WifiDisplay[] displays = (WifiDisplay[]) WifiDisplay.CREATOR.newArray(count);
        for (int i = 0; i < count; i++) {
            WifiP2pDevice device = this.mAvailableWifiDisplayPeers.get(i);
            displays[i] = createWifiDisplay(device);
            updateDesiredDevice(device);
        }
        this.mHandler.post(new Runnable() {
            /* class com.android.server.display.WifiDisplayController.AnonymousClass9 */

            public void run() {
                WifiDisplayController.this.mListener.onScanResults(displays);
            }
        });
    }

    private void handleScanFinished() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.display.WifiDisplayController.AnonymousClass10 */

            public void run() {
                WifiDisplayController.this.mListener.onScanFinished();
            }
        });
    }

    private void updateDesiredDevice(WifiP2pDevice device) {
        String address = device.deviceAddress;
        WifiP2pDevice wifiP2pDevice = this.mDesiredDevice;
        if (wifiP2pDevice != null && wifiP2pDevice.deviceAddress.equals(address)) {
            Slog.d(TAG, "updateDesiredDevice: new information " + describeWifiP2pDevice(device));
            this.mDesiredDevice.update(device);
            WifiDisplay wifiDisplay = this.mAdvertisedDisplay;
            if (wifiDisplay != null && wifiDisplay.getDeviceAddress().equals(address)) {
                readvertiseDisplay(createWifiDisplay(this.mDesiredDevice));
            }
        }
    }

    private void connect(WifiP2pDevice device) {
        WifiP2pDevice wifiP2pDevice = this.mDesiredDevice;
        if (wifiP2pDevice == null || wifiP2pDevice.deviceAddress.equals(device.deviceAddress)) {
            WifiP2pDevice wifiP2pDevice2 = this.mConnectedDevice;
            if (wifiP2pDevice2 != null && !wifiP2pDevice2.deviceAddress.equals(device.deviceAddress) && this.mDesiredDevice == null) {
                Slog.d(TAG, "connect: nothing to do, already connected to " + describeWifiP2pDevice(device) + " and not part way through connecting to a different device.");
            } else if (!this.mWfdEnabled) {
                Slog.i(TAG, "Ignoring request to connect to Wifi display because the  feature is currently disabled: " + device.deviceName);
            } else {
                this.mDesiredDevice = device;
                this.mConnectionRetriesLeft = 3;
                updateConnection();
            }
        } else {
            Slog.d(TAG, "connect: nothing to do, already connecting to " + describeWifiP2pDevice(device));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disconnect() {
        this.mDesiredDevice = null;
        updateConnection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void retryConnection() {
        this.mDesiredDevice = new WifiP2pDevice(this.mDesiredDevice);
        updateConnection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateConnection() {
        updateScanState();
        if (this.mRemoteDisplay != null && this.mConnectedDevice != this.mDesiredDevice) {
            Slog.i(TAG, "Stopped listening for RTSP connection on " + this.mRemoteDisplayInterface + " from Wifi display: " + this.mConnectedDevice.deviceName);
            if (this.mRemoteDisplayConnected || this.mConnectionRetriesLeft != 3 || !this.mHandler.hasCallbacks(this.mRtspTimeout)) {
                this.mRemoteDisplay.dispose();
                this.mRemoteDisplay = null;
                this.mRemoteDisplayInterface = null;
                this.mRemoteDisplayConnected = false;
                this.mHandler.removeCallbacks(this.mRtspTimeout);
                this.mWifiP2pManager.setMiracastMode(0);
                unadvertiseDisplay();
                return;
            }
            Slog.i(TAG, "Rtsp is establishing, do not disconnect untill rtsp has been established, return");
        } else if (this.mDisconnectingDevice == null) {
            WifiP2pDevice wifiP2pDevice = this.mConnectedDevice;
            if (wifiP2pDevice != null && wifiP2pDevice != this.mDesiredDevice) {
                Slog.i(TAG, "Disconnecting from Wifi display: " + this.mConnectedDevice.deviceName);
                this.mDisconnectingDevice = this.mConnectedDevice;
                this.mConnectedDevice = null;
                this.mConnectedDeviceGroupInfo = null;
                unadvertiseDisplay();
                if (this.mDisconnectingDevice != null) {
                    final WifiP2pDevice oldDevice = this.mDisconnectingDevice;
                    this.mWifiP2pManager.removeGroup(this.mWifiP2pChannel, new WifiP2pManager.ActionListener() {
                        /* class com.android.server.display.WifiDisplayController.AnonymousClass11 */

                        public void onSuccess() {
                            Slog.i(WifiDisplayController.TAG, "Disconnected from Wifi display: " + oldDevice.deviceName);
                            WifiDisplayController.this.mHandler.postDelayed(new Runnable() {
                                /* class com.android.server.display.WifiDisplayController.AnonymousClass11.AnonymousClass1 */

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
                                WifiDisplayController.this.updateConnection();
                            }
                        }
                    });
                }
            } else if (this.mCancelingDevice == null) {
                WifiP2pDevice wifiP2pDevice2 = this.mConnectingDevice;
                if (wifiP2pDevice2 != null && !wifiP2pDevice2.equals(this.mDesiredDevice)) {
                    Slog.i(TAG, "Canceling connection to Wifi display: " + this.mConnectingDevice.deviceName);
                    this.mCancelingDevice = this.mConnectingDevice;
                    this.mConnectingDevice = null;
                    unadvertiseDisplay();
                    this.mHandler.removeCallbacks(this.mConnectionTimeout);
                    final WifiP2pDevice oldDevice2 = this.mCancelingDevice;
                    this.mWifiP2pManager.cancelConnect(this.mWifiP2pChannel, new WifiP2pManager.ActionListener() {
                        /* class com.android.server.display.WifiDisplayController.AnonymousClass12 */

                        public void onSuccess() {
                            Slog.i(WifiDisplayController.TAG, "Canceled connection to Wifi display: " + oldDevice2.deviceName);
                            next();
                        }

                        public void onFailure(int reason) {
                            Slog.i(WifiDisplayController.TAG, "Failed to cancel connection to Wifi display: " + oldDevice2.deviceName + ", reason=" + reason);
                            if (reason == 2) {
                                WifiDisplayController.this.mWifiP2pManager.removeGroup(WifiDisplayController.this.mWifiP2pChannel, new WifiP2pManager.ActionListener() {
                                    /* class com.android.server.display.WifiDisplayController.AnonymousClass12.AnonymousClass1 */

                                    public void onSuccess() {
                                        Slog.i(WifiDisplayController.TAG, "removeGroup successfully on cancelConnect failure");
                                    }

                                    public void onFailure(int reason) {
                                        Slog.i(WifiDisplayController.TAG, "Failed to removeGroup on cancelConnect failure, reason=" + reason);
                                    }
                                });
                            }
                            next();
                        }

                        private void next() {
                            if (WifiDisplayController.this.mCancelingDevice == oldDevice2) {
                                WifiDisplayController.this.mCancelingDevice = null;
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
                    int i = this.mWifiDisplayWpsConfig;
                    if (i != 4) {
                        wps.setup = i;
                    } else if (this.mConnectingDevice.wpsPbcSupported()) {
                        wps.setup = 0;
                    } else if (this.mConnectingDevice.wpsDisplaySupported()) {
                        wps.setup = 2;
                    } else {
                        wps.setup = 1;
                    }
                    config.wps = wps;
                    config.deviceAddress = this.mConnectingDevice.deviceAddress;
                    Slog.i(TAG, "I want to be GO.");
                    config.groupOwnerIntent = Integer.valueOf(SystemProperties.get("wfd.source.go_intent", String.valueOf(14))).intValue();
                    WifiP2pConfig config2 = this.mMtkController.overWriteConfig(config);
                    advertiseDisplay(createWifiDisplay(this.mConnectingDevice), null, 0, 0, 0);
                    final WifiP2pDevice newDevice = this.mDesiredDevice;
                    this.mWifiP2pManager.connect(this.mWifiP2pChannel, config2, new WifiP2pManager.ActionListener() {
                        /* class com.android.server.display.WifiDisplayController.AnonymousClass13 */

                        public void onSuccess() {
                            Slog.i(WifiDisplayController.TAG, "Initiated connection to Wifi display: " + newDevice.deviceName);
                            WifiDisplayController.this.mHandler.postDelayed(WifiDisplayController.this.mConnectionTimeout, 30000);
                        }

                        public void onFailure(int reason) {
                            if (WifiDisplayController.this.mConnectingDevice == newDevice) {
                                Slog.i(WifiDisplayController.TAG, "Failed to initiate connection to Wifi display: " + newDevice.deviceName + ", reason=" + reason);
                                WifiDisplayController wifiDisplayController = WifiDisplayController.this;
                                wifiDisplayController.mConnectingDevice = null;
                                wifiDisplayController.handleConnectionFailure(false);
                                WifiDisplayController.this.mWifiDisplayUsageHelper.wfdConnectedFailed("P2P_Fail_Connect", WifiDisplayController.this.mConnectingDevice, WifiDisplayController.this.mConnectedDeviceGroupInfo, WifiDisplayController.this.mContext);
                            }
                        }
                    });
                } else if (this.mConnectedDevice != null && this.mRemoteDisplay == null) {
                    Inet4Address addr = getInterfaceAddress(this.mConnectedDeviceGroupInfo);
                    if (addr == null) {
                        Slog.i(TAG, "Failed to get local interface address for communicating with Wifi display: " + this.mConnectedDevice.deviceName);
                        handleConnectionFailure(false);
                        this.mWifiDisplayUsageHelper.wfdConnectedFailed("P2P_Addr_NULL", this.mConnectedDevice, this.mConnectedDeviceGroupInfo, this.mContext);
                        return;
                    }
                    this.mWifiP2pManager.setMiracastMode(1);
                    final WifiP2pDevice oldDevice3 = this.mConnectedDevice;
                    String iface = addr.getHostAddress() + ":" + getPortNumber(this.mConnectedDevice);
                    this.mRemoteDisplayInterface = iface;
                    Slog.i(TAG, "Listening for RTSP connection on " + iface + " from Wifi display: " + this.mConnectedDevice.deviceName);
                    this.mRemoteDisplay = RemoteDisplay.listen(iface, new RemoteDisplay.Listener() {
                        /* class com.android.server.display.WifiDisplayController.AnonymousClass14 */

                        public void onDisplayConnected(Surface surface, int width, int height, int flags, int session) {
                            if (WifiDisplayController.this.mConnectedDevice == oldDevice3 && !WifiDisplayController.this.mRemoteDisplayConnected) {
                                Slog.i(WifiDisplayController.TAG, "Opened RTSP connection with Wifi display: " + WifiDisplayController.this.mConnectedDevice.deviceName);
                                WifiDisplayController.this.mRemoteDisplayConnected = true;
                                WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspTimeout);
                                if (WifiDisplayController.this.mWifiDisplayCertMode) {
                                    Listener listener = WifiDisplayController.this.mListener;
                                    WifiDisplayController wifiDisplayController = WifiDisplayController.this;
                                    listener.onDisplaySessionInfo(wifiDisplayController.getSessionInfo(wifiDisplayController.mConnectedDeviceGroupInfo, session));
                                }
                                WifiDisplayController.this.advertiseDisplay(WifiDisplayController.createWifiDisplay(WifiDisplayController.this.mConnectedDevice), surface, width, height, flags);
                            }
                            WifiDisplayController.this.mWifiDisplayUsageHelper.wfdConnecteSuceess(WifiDisplayController.this.mConnectedDeviceGroupInfo, WifiDisplayController.this.mConnectedDevice, WifiDisplayController.this.mContext);
                            WifiDisplayController.this.connectedTime = System.currentTimeMillis();
                        }

                        public void onDisplayDisconnected() {
                            if (WifiDisplayController.this.mConnectedDevice == oldDevice3) {
                                Slog.i(WifiDisplayController.TAG, "Closed RTSP connection with Wifi display: " + WifiDisplayController.this.mConnectedDevice.deviceName);
                                WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspTimeout);
                                WifiDisplayController.this.mWifiDisplayUsageHelper.reportWfdConnectionTime(Long.valueOf(System.currentTimeMillis() - WifiDisplayController.this.connectedTime), WifiDisplayController.this.mConnectedDevice, WifiDisplayController.this.mContext);
                                WifiDisplayController.this.disconnect();
                            }
                        }

                        public void onDisplayError(int error) {
                            if (WifiDisplayController.this.mConnectedDevice == oldDevice3) {
                                Slog.i(WifiDisplayController.TAG, "Lost RTSP connection with Wifi display due to error " + error + ": " + WifiDisplayController.this.mConnectedDevice.deviceName);
                                WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspTimeout);
                                WifiDisplayController.this.handleConnectionFailure(false);
                                WifiDisplayController.this.mWifiDisplayUsageHelper.wfdConnectedFailed("Lost_RTSP_Connection", WifiDisplayController.this.mConnectedDevice, WifiDisplayController.this.mConnectedDeviceGroupInfo, WifiDisplayController.this.mContext);
                            }
                        }
                    }, this.mHandler, this.mContext.getOpPackageName());
                    this.mHandler.postDelayed(this.mRtspTimeout, (long) ((this.mWifiDisplayCertMode ? RTSP_TIMEOUT_SECONDS_CERT_MODE : 30) * 1000));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private WifiDisplaySessionInfo getSessionInfo(WifiP2pGroup info, int session) {
        if (info == null) {
            return null;
        }
        Inet4Address addr = getInterfaceAddress(info);
        WifiDisplaySessionInfo sessionInfo = new WifiDisplaySessionInfo(!info.getOwner().deviceAddress.equals(this.mThisDevice.deviceAddress), session, info.getOwner().deviceAddress + StringUtils.SPACE + info.getNetworkName(), info.getPassphrase(), addr != null ? addr.getHostAddress() : "");
        Slog.d(TAG, sessionInfo.toString());
        return sessionInfo;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStateChanged(boolean enabled) {
        this.mWifiP2pEnabled = enabled;
        updateWfdEnableState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePeersChanged() {
        requestPeers();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleConnectionChanged(NetworkInfo networkInfo) {
        this.mNetworkInfo = networkInfo;
        this.mWifiDisplayUsageHelper.p2pConnectState(networkInfo, this.mConnectingDevice, Boolean.valueOf(this.mWfdEnabled), this.mConnectedDeviceGroupInfo, this.mContext);
        if (!this.mWfdEnabled || !networkInfo.isConnected()) {
            this.mConnectedDeviceGroupInfo = null;
            if (!(this.mConnectingDevice == null && this.mConnectedDevice == null)) {
                disconnect();
            }
            if (this.mWfdEnabled) {
                requestPeers();
                this.mMtkController.checkReConnect();
            }
        } else if (this.mDesiredDevice != null || this.mWifiDisplayCertMode) {
            this.mWifiP2pManager.requestGroupInfo(this.mWifiP2pChannel, new WifiP2pManager.GroupInfoListener() {
                /* class com.android.server.display.WifiDisplayController.AnonymousClass15 */

                public void onGroupInfoAvailable(WifiP2pGroup info) {
                    Slog.d(WifiDisplayController.TAG, "Received group info: " + WifiDisplayController.describeWifiP2pGroup(info));
                    if (WifiDisplayController.this.mConnectingDevice != null && !info.contains(WifiDisplayController.this.mConnectingDevice)) {
                        Slog.i(WifiDisplayController.TAG, "Aborting connection to Wifi display because the current P2P group does not contain the device we expected to find: " + WifiDisplayController.this.mConnectingDevice.deviceName + ", group info was: " + WifiDisplayController.describeWifiP2pGroup(info));
                        WifiDisplayController.this.mWifiDisplayUsageHelper.wfdConnectedFailed("P2P_Group_Fail", WifiDisplayController.this.mConnectingDevice, WifiDisplayController.this.mConnectedDeviceGroupInfo, WifiDisplayController.this.mContext);
                        WifiDisplayController.this.handleConnectionFailure(false);
                    } else if (WifiDisplayController.this.mDesiredDevice == null || info.contains(WifiDisplayController.this.mDesiredDevice)) {
                        if (WifiDisplayController.this.mWifiDisplayCertMode) {
                            boolean owner = info.getOwner().deviceAddress.equals(WifiDisplayController.this.mThisDevice.deviceAddress);
                            if (owner && info.getClientList().isEmpty()) {
                                WifiDisplayController wifiDisplayController = WifiDisplayController.this;
                                wifiDisplayController.mConnectingDevice = wifiDisplayController.mDesiredDevice = null;
                                WifiDisplayController.this.mConnectedDeviceGroupInfo = info;
                                WifiDisplayController.this.updateConnection();
                            } else if (WifiDisplayController.this.mConnectingDevice == null && WifiDisplayController.this.mDesiredDevice == null) {
                                WifiDisplayController wifiDisplayController2 = WifiDisplayController.this;
                                wifiDisplayController2.mConnectingDevice = wifiDisplayController2.mDesiredDevice = owner ? info.getClientList().iterator().next() : info.getOwner();
                            }
                        }
                        if (WifiDisplayController.this.mConnectingDevice != null && WifiDisplayController.this.mConnectingDevice == WifiDisplayController.this.mDesiredDevice) {
                            Slog.i(WifiDisplayController.TAG, "Connected to Wifi display: " + WifiDisplayController.this.mConnectingDevice.deviceName);
                            WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mConnectionTimeout);
                            WifiDisplayController.this.mConnectedDeviceGroupInfo = info;
                            WifiDisplayController wifiDisplayController3 = WifiDisplayController.this;
                            wifiDisplayController3.mConnectedDevice = wifiDisplayController3.mConnectingDevice;
                            WifiDisplayController wifiDisplayController4 = WifiDisplayController.this;
                            wifiDisplayController4.mConnectingDevice = null;
                            wifiDisplayController4.updateConnection();
                        }
                    } else {
                        WifiDisplayController.this.disconnect();
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleConnectionFailure(boolean timeoutOccurred) {
        Slog.i(TAG, "Wifi display connection failed!");
        Slog.i(TAG, "requestScan after connection failed..");
        discoverPeers();
        if (this.mDesiredDevice == null) {
            return;
        }
        if (this.mConnectionRetriesLeft > 0) {
            final WifiP2pDevice oldDevice = this.mDesiredDevice;
            this.mHandler.postDelayed(new Runnable() {
                /* class com.android.server.display.WifiDisplayController.AnonymousClass19 */

                public void run() {
                    if (WifiDisplayController.this.mDesiredDevice == oldDevice && WifiDisplayController.this.mConnectionRetriesLeft > 0) {
                        WifiDisplayController.access$3520(WifiDisplayController.this, 1);
                        Slog.i(WifiDisplayController.TAG, "Retrying Wifi display connection.  Retries left: " + WifiDisplayController.this.mConnectionRetriesLeft);
                        WifiDisplayController.this.retryConnection();
                    } else if (WifiDisplayController.this.mDesiredDevice == null && WifiDisplayController.this.mConnectionRetriesLeft > 0) {
                        WifiDisplayController.this.mConnectionRetriesLeft = 0;
                        WifiDisplayController.this.disconnect();
                    }
                }
            }, timeoutOccurred ? 0 : 500);
            return;
        }
        disconnect();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void advertiseDisplay(final WifiDisplay display, final Surface surface, final int width, final int height, final int flags) {
        if (!Objects.equals(this.mAdvertisedDisplay, display) || this.mAdvertisedDisplaySurface != surface || this.mAdvertisedDisplayWidth != width || this.mAdvertisedDisplayHeight != height || this.mAdvertisedDisplayFlags != flags) {
            final WifiDisplay oldDisplay = this.mAdvertisedDisplay;
            final Surface oldSurface = this.mAdvertisedDisplaySurface;
            this.mAdvertisedDisplay = display;
            this.mAdvertisedDisplaySurface = surface;
            this.mAdvertisedDisplayWidth = width;
            this.mAdvertisedDisplayHeight = height;
            this.mAdvertisedDisplayFlags = flags;
            this.mHandler.post(new Runnable() {
                /* class com.android.server.display.WifiDisplayController.AnonymousClass20 */

                public void run() {
                    Surface surface = oldSurface;
                    if (surface == null || surface == surface) {
                        WifiDisplay wifiDisplay = oldDisplay;
                        if (wifiDisplay != null && !wifiDisplay.hasSameAddress(display)) {
                            WifiDisplayController.this.mListener.onDisplayConnectionFailed();
                            WifiDisplayController.this.mMtkController.setWFD(false);
                        }
                    } else {
                        WifiDisplayController.this.mListener.onDisplayDisconnected();
                        WifiDisplayController.this.mMtkController.setWFD(false);
                    }
                    WifiDisplay wifiDisplay2 = display;
                    if (wifiDisplay2 != null) {
                        if (!wifiDisplay2.hasSameAddress(oldDisplay)) {
                            WifiDisplayController.this.mListener.onDisplayConnecting(display);
                        } else if (!display.equals(oldDisplay)) {
                            WifiDisplayController.this.mListener.onDisplayChanged(display);
                        }
                        Surface surface2 = surface;
                        if (surface2 != null && surface2 != oldSurface) {
                            WifiDisplayController.this.mListener.onDisplayConnected(display, surface, width, height, flags);
                            WifiDisplayController.this.mMtkController.setWFD(true);
                        }
                    }
                }
            });
        }
    }

    private void unadvertiseDisplay() {
        advertiseDisplay(null, null, 0, 0, 0);
    }

    private void readvertiseDisplay(WifiDisplay display) {
        advertiseDisplay(display, this.mAdvertisedDisplaySurface, this.mAdvertisedDisplayWidth, this.mAdvertisedDisplayHeight, this.mAdvertisedDisplayFlags);
    }

    private static Inet4Address getInterfaceAddress(WifiP2pGroup info) {
        if (info == null) {
            return null;
        }
        try {
            Enumeration<InetAddress> addrs = NetworkInterface.getByName(info.getInterface()).getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress addr = addrs.nextElement();
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
        if (!device.deviceName.startsWith("DIRECT-") || !device.deviceName.endsWith("Broadcom")) {
            return DEFAULT_CONTROL_PORT;
        }
        return 8554;
    }

    /* access modifiers changed from: private */
    public static boolean isWifiDisplay(WifiP2pDevice device) {
        return device.wfdInfo != null && device.wfdInfo.isWfdEnabled() && isPrimarySinkDeviceType(device.wfdInfo.getDeviceType());
    }

    private static boolean isPrimarySinkDeviceType(int deviceType) {
        return deviceType == 1 || deviceType == 3;
    }

    /* access modifiers changed from: private */
    public static String describeWifiP2pDevice(WifiP2pDevice device) {
        return device != null ? device.toString().replace('\n', ',') : "null";
    }

    /* access modifiers changed from: private */
    public static String describeWifiP2pGroup(WifiP2pGroup group) {
        return group != null ? group.toString().replace('\n', ',') : "null";
    }

    /* access modifiers changed from: private */
    public static WifiDisplay createWifiDisplay(WifiP2pDevice device) {
        return new WifiDisplay(device.deviceAddress, device.deviceName, (String) null, true, device.wfdInfo.isSessionAvailable(), false);
    }
}
