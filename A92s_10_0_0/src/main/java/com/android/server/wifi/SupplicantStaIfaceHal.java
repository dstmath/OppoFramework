package com.android.server.wifi;

import android.content.Context;
import android.hardware.wifi.V1_3.IWifiChip;
import android.hardware.wifi.supplicant.V1_0.ISupplicant;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.hardware.wifi.supplicant.V1_0.WpsConfigMethods;
import android.hardware.wifi.supplicant.V1_1.ISupplicant;
import android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback;
import android.hardware.wifi.supplicant.V1_2.ISupplicantStaIface;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.net.IpConfiguration;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiSsid;
import android.os.Build;
import android.os.Handler;
import android.os.HidlSupport;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.MutableBoolean;
import android.util.MutableInt;
import android.util.Pair;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.SupplicantStaIfaceHal;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hotspot2.AnqpEvent;
import com.android.server.wifi.hotspot2.IconEvent;
import com.android.server.wifi.hotspot2.WnmData;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.ANQPParser;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.util.GbkUtil;
import com.android.server.wifi.util.NativeUtil;
import com.mediatek.server.wifi.MtkSupplicantStaIfaceHal;
import com.mediatek.server.wifi.MtkWapi;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.concurrent.ThreadSafe;
import vendor.oppo.hardware.wifi.supplicant.V1_0.IOppoSupplicantStaIface;
import vendor.oppo.hardware.wifi.supplicant.V1_0.IOppoSupplicantStaIfaceCallback;
import vendor.oppo.hardware.wifi.supplicant.V1_0.IOppoSupplicantStaNetwork;

@ThreadSafe
public class SupplicantStaIfaceHal {
    @VisibleForTesting
    public static final String HAL_INSTANCE_NAME = "default";
    @VisibleForTesting
    public static final String INIT_SERVICE_NAME = "wpa_supplicant";
    @VisibleForTesting
    public static final String INIT_START_PROPERTY = "ctl.start";
    @VisibleForTesting
    public static final String INIT_STOP_PROPERTY = "ctl.stop";
    private static final String TAG = "SupplicantStaIfaceHal";
    @VisibleForTesting
    public static final long WAIT_FOR_DEATH_TIMEOUT_MS = 50;
    private static final Pattern WPS_DEVICE_TYPE_PATTERN = Pattern.compile("^(\\d{1,2})-([0-9a-fA-F]{8})-(\\d{1,2})$");
    /* access modifiers changed from: private */
    public final Context mContext;
    private HashMap<String, WifiConfiguration> mCurrentNetworkLocalConfigs = new HashMap<>();
    private HashMap<String, SupplicantStaNetworkHal> mCurrentNetworkRemoteHandles = new HashMap<>();
    private WifiNative.SupplicantDeathEventHandler mDeathEventHandler;
    /* access modifiers changed from: private */
    public long mDeathRecipientCookie = 0;
    /* access modifiers changed from: private */
    public WifiNative.DppEventCallback mDppCallback = null;
    /* access modifiers changed from: private */
    public final Handler mEventHandler;
    private HashMap<String, IOppoSupplicantStaIface> mIOppoSupplicantStaIfaces = new HashMap<>();
    /* access modifiers changed from: private */
    public IServiceManager mIServiceManager = null;
    private ISupplicant mISupplicant;
    private HashMap<String, ISupplicantStaIfaceCallback> mISupplicantStaIfaceCallbacks = new HashMap<>();
    private HashMap<String, ISupplicantStaIface> mISupplicantStaIfaces = new HashMap<>();
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final PropertyService mPropertyService;
    private ServiceManagerDeathRecipient mServiceManagerDeathRecipient;
    private final IServiceNotification mServiceNotificationCallback = new IServiceNotification.Stub() {
        /* class com.android.server.wifi.SupplicantStaIfaceHal.AnonymousClass1 */

        public void onRegistration(String fqName, String name, boolean preexisting) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                if (SupplicantStaIfaceHal.this.mVerboseLoggingEnabled) {
                    Log.i(SupplicantStaIfaceHal.TAG, "IServiceNotification.onRegistration for: " + fqName + ", " + name + " preexisting=" + preexisting);
                }
                if (!SupplicantStaIfaceHal.this.initSupplicantService()) {
                    Log.e(SupplicantStaIfaceHal.TAG, "initalizing ISupplicant failed.");
                    SupplicantStaIfaceHal.this.supplicantServiceDiedHandler(SupplicantStaIfaceHal.this.mDeathRecipientCookie);
                } else {
                    Log.i(SupplicantStaIfaceHal.TAG, "Completed initialization of ISupplicant.");
                }
            }
        }
    };
    private SupplicantDeathRecipient mSupplicantDeathRecipient;
    /* access modifiers changed from: private */
    public boolean mVerboseLoggingEnabled = false;
    /* access modifiers changed from: private */
    public final WifiMonitor mWifiMonitor;

    /* access modifiers changed from: private */
    public class ServiceManagerDeathRecipient implements IHwBinder.DeathRecipient {
        private ServiceManagerDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            SupplicantStaIfaceHal.this.mEventHandler.post(new Runnable(cookie) {
                /* class com.android.server.wifi.$$Lambda$SupplicantStaIfaceHal$ServiceManagerDeathRecipient$d46woRLrtA_5VzKq0teHof90 */
                private final /* synthetic */ long f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    SupplicantStaIfaceHal.ServiceManagerDeathRecipient.this.lambda$serviceDied$0$SupplicantStaIfaceHal$ServiceManagerDeathRecipient(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$serviceDied$0$SupplicantStaIfaceHal$ServiceManagerDeathRecipient(long cookie) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                Log.w(SupplicantStaIfaceHal.TAG, "IServiceManager died: cookie=" + cookie);
                SupplicantStaIfaceHal.this.supplicantServiceDiedHandler(SupplicantStaIfaceHal.this.mDeathRecipientCookie);
                IServiceManager unused = SupplicantStaIfaceHal.this.mIServiceManager = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public class SupplicantDeathRecipient implements IHwBinder.DeathRecipient {
        private SupplicantDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            SupplicantStaIfaceHal.this.mEventHandler.post(new Runnable(cookie) {
                /* class com.android.server.wifi.$$Lambda$SupplicantStaIfaceHal$SupplicantDeathRecipient$2qEgiCB3aUF88PzSSLK9dis2Y3s */
                private final /* synthetic */ long f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    SupplicantStaIfaceHal.SupplicantDeathRecipient.this.lambda$serviceDied$0$SupplicantStaIfaceHal$SupplicantDeathRecipient(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$serviceDied$0$SupplicantStaIfaceHal$SupplicantDeathRecipient(long cookie) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                Log.w(SupplicantStaIfaceHal.TAG, "ISupplicant died: cookie=" + cookie);
                SupplicantStaIfaceHal.this.supplicantServiceDiedHandler(cookie);
            }
        }
    }

    public SupplicantStaIfaceHal(Context context, WifiMonitor monitor, PropertyService propertyService, Looper looper) {
        this.mContext = context;
        this.mWifiMonitor = monitor;
        this.mPropertyService = propertyService;
        this.mEventHandler = new Handler(looper);
        this.mServiceManagerDeathRecipient = new ServiceManagerDeathRecipient();
        this.mSupplicantDeathRecipient = new SupplicantDeathRecipient();
    }

    /* access modifiers changed from: package-private */
    public void enableVerboseLogging(boolean enable) {
        synchronized (this.mLock) {
            this.mVerboseLoggingEnabled = enable;
        }
    }

    private boolean linkToServiceManagerDeath() {
        synchronized (this.mLock) {
            if (this.mIServiceManager == null) {
                return false;
            }
            try {
                if (this.mIServiceManager.linkToDeath(this.mServiceManagerDeathRecipient, 0)) {
                    return true;
                }
                Log.wtf(TAG, "Error on linkToDeath on IServiceManager");
                supplicantServiceDiedHandler(this.mDeathRecipientCookie);
                this.mIServiceManager = null;
                return false;
            } catch (RemoteException e) {
                Log.e(TAG, "IServiceManager.linkToDeath exception", e);
                return false;
            }
        }
    }

    public boolean initialize() {
        synchronized (this.mLock) {
            if (this.mVerboseLoggingEnabled) {
                Log.i(TAG, "Registering ISupplicant service ready callback.");
            }
            this.mISupplicant = null;
            this.mISupplicantStaIfaces.clear();
            this.mIOppoSupplicantStaIfaces.clear();
            if (this.mIServiceManager != null) {
                return true;
            }
            try {
                this.mIServiceManager = getServiceManagerMockable();
                if (this.mIServiceManager == null) {
                    Log.e(TAG, "Failed to get HIDL Service Manager");
                    return false;
                } else if (!linkToServiceManagerDeath()) {
                    return false;
                } else {
                    if (!this.mIServiceManager.registerForNotifications(ISupplicant.kInterfaceName, "", this.mServiceNotificationCallback)) {
                        Log.e(TAG, "Failed to register for notifications to android.hardware.wifi.supplicant@1.0::ISupplicant");
                        this.mIServiceManager = null;
                        return false;
                    }
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Exception while trying to register a listener for ISupplicant service: " + e);
                supplicantServiceDiedHandler(this.mDeathRecipientCookie);
            }
        }
        return true;
    }

    private boolean linkToSupplicantDeath(IHwBinder.DeathRecipient deathRecipient, long cookie) {
        synchronized (this.mLock) {
            if (this.mISupplicant == null) {
                return false;
            }
            try {
                if (this.mISupplicant.linkToDeath(deathRecipient, cookie)) {
                    return true;
                }
                Log.wtf(TAG, "Error on linkToDeath on ISupplicant");
                supplicantServiceDiedHandler(this.mDeathRecipientCookie);
                return false;
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicant.linkToDeath exception", e);
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean initSupplicantService() {
        synchronized (this.mLock) {
            try {
                this.mISupplicant = getSupplicantMockable();
                if (this.mISupplicant == null) {
                    Log.e(TAG, "Got null ISupplicant service. Stopping supplicant HIDL startup");
                    return false;
                }
                SupplicantDeathRecipient supplicantDeathRecipient = this.mSupplicantDeathRecipient;
                long j = this.mDeathRecipientCookie + 1;
                this.mDeathRecipientCookie = j;
                if (!linkToSupplicantDeath(supplicantDeathRecipient, j)) {
                    return false;
                }
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicant.getService exception: " + e);
                return false;
            } catch (NoSuchElementException e2) {
                Log.e(TAG, "ISupplicant.getService exception: " + e2);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public int getCurrentNetworkId(String ifaceName) {
        synchronized (this.mLock) {
            WifiConfiguration currentConfig = getCurrentNetworkLocalConfig(ifaceName);
            if (currentConfig == null) {
                return -1;
            }
            int i = currentConfig.networkId;
            return i;
        }
    }

    public boolean isSameNetwork(String ifaceName, WifiConfiguration config) {
        return WifiConfigurationUtil.isSameNetwork(config, this.mCurrentNetworkLocalConfigs.get(ifaceName));
    }

    public boolean setupIface(String ifaceName) {
        ISupplicantIface ifaceHwBinder;
        if (checkSupplicantStaIfaceAndLogFailure(ifaceName, "setupIface") != null) {
            return false;
        }
        if (isV1_1()) {
            ifaceHwBinder = addIfaceV1_1(ifaceName);
        } else {
            ifaceHwBinder = getIfaceV1_0(ifaceName);
        }
        if (ifaceHwBinder == null) {
            Log.e(TAG, "setupIface got null iface");
            return false;
        }
        MtkWapi.setupMtkIface(ifaceName);
        MtkSupplicantStaIfaceHal.setupMtkIface(ifaceName);
        SupplicantStaIfaceHalCallback callback = new SupplicantStaIfaceHalCallback(ifaceName);
        if (isV1_2()) {
            android.hardware.wifi.supplicant.V1_2.ISupplicantStaIface iface = getStaIfaceMockableV1_2(ifaceHwBinder);
            SupplicantStaIfaceHalCallbackV1_1 callbackV11 = new SupplicantStaIfaceHalCallbackV1_1(ifaceName, callback);
            if (!registerCallbackV1_2(iface, new SupplicantStaIfaceHalCallbackV1_2(ifaceName, callbackV11))) {
                return false;
            }
            this.mISupplicantStaIfaces.put(ifaceName, iface);
            this.mISupplicantStaIfaceCallbacks.put(ifaceName, callbackV11);
        } else if (isV1_1()) {
            android.hardware.wifi.supplicant.V1_1.ISupplicantStaIface iface2 = getStaIfaceMockableV1_1(ifaceHwBinder);
            SupplicantStaIfaceHalCallbackV1_1 callbackV1_1 = new SupplicantStaIfaceHalCallbackV1_1(ifaceName, callback);
            if (!registerCallbackV1_1(iface2, callbackV1_1)) {
                return false;
            }
            this.mISupplicantStaIfaces.put(ifaceName, iface2);
            this.mISupplicantStaIfaceCallbacks.put(ifaceName, callbackV1_1);
        } else {
            ISupplicantStaIface iface3 = getStaIfaceMockable(ifaceHwBinder);
            if (!registerCallback(iface3, callback)) {
                return false;
            }
            this.mISupplicantStaIfaces.put(ifaceName, iface3);
            this.mISupplicantStaIfaceCallbacks.put(ifaceName, callback);
        }
        this.mIOppoSupplicantStaIfaces.put(ifaceName, getOppoStaIfaceMockable(ifaceHwBinder));
        return true;
    }

    private ISupplicantIface getIfaceV1_0(String ifaceName) {
        synchronized (this.mLock) {
            if (this.mISupplicant == null) {
                return null;
            }
            ArrayList<ISupplicant.IfaceInfo> supplicantIfaces = new ArrayList<>();
            try {
                this.mISupplicant.listInterfaces(new ISupplicant.listInterfacesCallback(supplicantIfaces) {
                    /* class com.android.server.wifi.$$Lambda$SupplicantStaIfaceHal$baCrbI3eoP3PfmPzF28q6ekLM */
                    private final /* synthetic */ ArrayList f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicant.listInterfacesCallback
                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        SupplicantStaIfaceHal.lambda$getIfaceV1_0$0(this.f$0, supplicantStatus, arrayList);
                    }
                });
                if (supplicantIfaces.size() == 0) {
                    Log.e(TAG, "Got zero HIDL supplicant ifaces. Stopping supplicant HIDL startup.");
                    return null;
                }
                HidlSupport.Mutable<ISupplicantIface> supplicantIface = new HidlSupport.Mutable<>();
                Iterator<ISupplicant.IfaceInfo> it = supplicantIfaces.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ISupplicant.IfaceInfo ifaceInfo = it.next();
                    if (ifaceInfo.type == 0 && ifaceName.equals(ifaceInfo.name)) {
                        try {
                            this.mISupplicant.getInterface(ifaceInfo, new ISupplicant.getInterfaceCallback(supplicantIface) {
                                /* class com.android.server.wifi.$$Lambda$SupplicantStaIfaceHal$QKKt5Vr7ONbMGW5Dn_SiW886n4 */
                                private final /* synthetic */ HidlSupport.Mutable f$0;

                                {
                                    this.f$0 = r1;
                                }

                                @Override // android.hardware.wifi.supplicant.V1_0.ISupplicant.getInterfaceCallback
                                public final void onValues(SupplicantStatus supplicantStatus, ISupplicantIface iSupplicantIface) {
                                    SupplicantStaIfaceHal.lambda$getIfaceV1_0$1(this.f$0, supplicantStatus, iSupplicantIface);
                                }
                            });
                            break;
                        } catch (RemoteException e) {
                            Log.e(TAG, "ISupplicant.getInterface exception: " + e);
                            handleRemoteException(e, "getInterface");
                            return null;
                        }
                    }
                }
                ISupplicantIface iSupplicantIface = (ISupplicantIface) supplicantIface.value;
                return iSupplicantIface;
            } catch (RemoteException e2) {
                Log.e(TAG, "ISupplicant.listInterfaces exception: " + e2);
                handleRemoteException(e2, "listInterfaces");
                return null;
            }
        }
    }

    static /* synthetic */ void lambda$getIfaceV1_0$0(ArrayList supplicantIfaces, SupplicantStatus status, ArrayList ifaces) {
        if (status.code != 0) {
            Log.e(TAG, "Getting Supplicant Interfaces failed: " + status.code);
            return;
        }
        supplicantIfaces.addAll(ifaces);
    }

    static /* synthetic */ void lambda$getIfaceV1_0$1(HidlSupport.Mutable supplicantIface, SupplicantStatus status, ISupplicantIface iface) {
        if (status.code != 0) {
            Log.e(TAG, "Failed to get ISupplicantIface " + status.code);
            return;
        }
        supplicantIface.value = iface;
    }

    private ISupplicantIface addIfaceV1_1(String ifaceName) {
        ISupplicantIface iSupplicantIface;
        synchronized (this.mLock) {
            ISupplicant.IfaceInfo ifaceInfo = new ISupplicant.IfaceInfo();
            ifaceInfo.name = ifaceName;
            ifaceInfo.type = 0;
            HidlSupport.Mutable<ISupplicantIface> supplicantIface = new HidlSupport.Mutable<>();
            try {
                getSupplicantMockableV1_1().addInterface(ifaceInfo, new ISupplicant.addInterfaceCallback(supplicantIface) {
                    /* class com.android.server.wifi.$$Lambda$SupplicantStaIfaceHal$kPu1HFl0FicFP9N2B4hh_sincE8 */
                    private final /* synthetic */ HidlSupport.Mutable f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // android.hardware.wifi.supplicant.V1_1.ISupplicant.addInterfaceCallback
                    public final void onValues(SupplicantStatus supplicantStatus, ISupplicantIface iSupplicantIface) {
                        SupplicantStaIfaceHal.lambda$addIfaceV1_1$2(this.f$0, supplicantStatus, iSupplicantIface);
                    }
                });
                iSupplicantIface = (ISupplicantIface) supplicantIface.value;
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicant.addInterface exception: " + e);
                handleRemoteException(e, "addInterface");
                return null;
            } catch (NoSuchElementException e2) {
                Log.e(TAG, "ISupplicant.addInterface exception: " + e2);
                handleNoSuchElementException(e2, "addInterface");
                return null;
            }
        }
        return iSupplicantIface;
    }

    static /* synthetic */ void lambda$addIfaceV1_1$2(HidlSupport.Mutable supplicantIface, SupplicantStatus status, ISupplicantIface iface) {
        if (status.code == 0 || status.code == 5) {
            supplicantIface.value = iface;
            return;
        }
        Log.e(TAG, "Failed to create ISupplicantIface " + status.code);
    }

    public boolean teardownIface(String ifaceName) {
        synchronized (this.mLock) {
            if (checkSupplicantStaIfaceAndLogFailure(ifaceName, "teardownIface") == null) {
                return false;
            }
            if (isV1_1() && !removeIfaceV1_1(ifaceName)) {
                Log.e(TAG, "Failed to remove iface = " + ifaceName);
                return false;
            } else if (this.mISupplicantStaIfaces.remove(ifaceName) == null) {
                Log.e(TAG, "Trying to teardown unknown inteface");
                return false;
            } else {
                this.mISupplicantStaIfaceCallbacks.remove(ifaceName);
                this.mIOppoSupplicantStaIfaces.remove(ifaceName);
                return true;
            }
        }
    }

    private boolean removeIfaceV1_1(String ifaceName) {
        synchronized (this.mLock) {
            try {
                ISupplicant.IfaceInfo ifaceInfo = new ISupplicant.IfaceInfo();
                ifaceInfo.name = ifaceName;
                ifaceInfo.type = 0;
                SupplicantStatus status = getSupplicantMockableV1_1().removeInterface(ifaceInfo);
                if (status.code == 0) {
                    return true;
                }
                Log.e(TAG, "Failed to remove iface " + status.code);
                return false;
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicant.removeInterface exception: " + e);
                handleRemoteException(e, "removeInterface");
                return false;
            } catch (NoSuchElementException e2) {
                Log.e(TAG, "ISupplicant.removeInterface exception: " + e2);
                handleNoSuchElementException(e2, "removeInterface");
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public boolean registerDeathHandler(WifiNative.SupplicantDeathEventHandler handler) {
        if (this.mDeathEventHandler != null) {
            Log.e(TAG, "Death handler already present");
        }
        this.mDeathEventHandler = handler;
        return true;
    }

    public boolean deregisterDeathHandler() {
        if (this.mDeathEventHandler == null) {
            Log.e(TAG, "No Death handler present");
        }
        this.mDeathEventHandler = null;
        return true;
    }

    private void clearState() {
        synchronized (this.mLock) {
            this.mISupplicant = null;
            this.mISupplicantStaIfaces.clear();
            this.mIOppoSupplicantStaIfaces.clear();
            this.mCurrentNetworkLocalConfigs.clear();
            this.mCurrentNetworkRemoteHandles.clear();
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003b, code lost:
        return;
     */
    public void supplicantServiceDiedHandler(long cookie) {
        synchronized (this.mLock) {
            if (this.mDeathRecipientCookie != cookie) {
                Log.i(TAG, "Ignoring stale death recipient notification");
                return;
            }
            for (String ifaceName : this.mISupplicantStaIfaces.keySet()) {
                this.mWifiMonitor.broadcastSupplicantDisconnectionEvent(ifaceName);
            }
            clearState();
            if (this.mDeathEventHandler != null) {
                this.mDeathEventHandler.onDeath();
            }
        }
    }

    public boolean isInitializationStarted() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mIServiceManager != null;
        }
        return z;
    }

    public boolean isInitializationComplete() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mISupplicant != null;
        }
        return z;
    }

    private boolean startDaemon_V1_1() {
        synchronized (this.mLock) {
            try {
                getSupplicantMockableV1_1();
            } catch (RemoteException e) {
                Log.e(TAG, "Exception while trying to start supplicant: " + e);
                supplicantServiceDiedHandler(this.mDeathRecipientCookie);
                return false;
            } catch (NoSuchElementException e2) {
                Log.d(TAG, "Successfully triggered start of supplicant using HIDL");
            }
        }
        return true;
    }

    public boolean startDaemon() {
        synchronized (this.mLock) {
            if (isV1_1()) {
                Log.i(TAG, "Starting supplicant using HIDL");
                boolean startDaemon_V1_1 = startDaemon_V1_1();
                return startDaemon_V1_1;
            }
            Log.i(TAG, "Starting supplicant using init");
            this.mPropertyService.set(INIT_START_PROPERTY, INIT_SERVICE_NAME);
            return true;
        }
    }

    private void terminate_V1_1() {
        synchronized (this.mLock) {
            if (checkSupplicantAndLogFailure("terminate")) {
                try {
                    getSupplicantMockableV1_1().terminate();
                } catch (RemoteException e) {
                    handleRemoteException(e, "terminate");
                } catch (NoSuchElementException e2) {
                    handleNoSuchElementException(e2, "terminate");
                }
            }
        }
    }

    public void terminate() {
        synchronized (this.mLock) {
            long waitForDeathCookie = new Random().nextLong();
            CountDownLatch waitForDeathLatch = new CountDownLatch(1);
            linkToSupplicantDeath(new IHwBinder.DeathRecipient(waitForDeathCookie, waitForDeathLatch) {
                /* class com.android.server.wifi.$$Lambda$SupplicantStaIfaceHal$GeKWB0M_UXwMasjbhTkdwwfpXy4 */
                private final /* synthetic */ long f$1;
                private final /* synthetic */ CountDownLatch f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r4;
                }

                public final void serviceDied(long j) {
                    SupplicantStaIfaceHal.this.lambda$terminate$3$SupplicantStaIfaceHal(this.f$1, this.f$2, j);
                }
            }, waitForDeathCookie);
            if (isV1_1()) {
                Log.i(TAG, "Terminating supplicant using HIDL");
                terminate_V1_1();
            } else {
                Log.i(TAG, "Terminating supplicant using init");
                this.mPropertyService.set(INIT_STOP_PROPERTY, INIT_SERVICE_NAME);
            }
            try {
                if (!waitForDeathLatch.await(50, TimeUnit.MILLISECONDS)) {
                    Log.w(TAG, "Timed out waiting for confirmation of supplicant death");
                }
            } catch (InterruptedException e) {
                Log.w(TAG, "Failed to wait for supplicant death");
            }
        }
    }

    public /* synthetic */ void lambda$terminate$3$SupplicantStaIfaceHal(long waitForDeathCookie, CountDownLatch waitForDeathLatch, long cookie) {
        Log.d(TAG, "ISupplicant died: cookie=" + cookie);
        if (cookie == waitForDeathCookie) {
            for (String ifaceName : this.mISupplicantStaIfaces.keySet()) {
                this.mWifiMonitor.broadcastSupplicantDisconnectionEvent(ifaceName);
            }
            clearState();
            WifiNative.SupplicantDeathEventHandler supplicantDeathEventHandler = this.mDeathEventHandler;
            if (supplicantDeathEventHandler != null) {
                supplicantDeathEventHandler.onDeath();
            }
            waitForDeathLatch.countDown();
        }
    }

    /* access modifiers changed from: protected */
    public IServiceManager getServiceManagerMockable() throws RemoteException {
        IServiceManager service;
        synchronized (this.mLock) {
            service = IServiceManager.getService();
        }
        return service;
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.supplicant.V1_0.ISupplicant getSupplicantMockable() throws RemoteException, NoSuchElementException {
        android.hardware.wifi.supplicant.V1_0.ISupplicant service;
        synchronized (this.mLock) {
            service = android.hardware.wifi.supplicant.V1_0.ISupplicant.getService();
        }
        return service;
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.supplicant.V1_1.ISupplicant getSupplicantMockableV1_1() throws RemoteException, NoSuchElementException {
        android.hardware.wifi.supplicant.V1_1.ISupplicant castFrom;
        synchronized (this.mLock) {
            castFrom = android.hardware.wifi.supplicant.V1_1.ISupplicant.castFrom((IHwInterface) android.hardware.wifi.supplicant.V1_0.ISupplicant.getService());
        }
        return castFrom;
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.supplicant.V1_2.ISupplicant getSupplicantMockableV1_2() throws RemoteException, NoSuchElementException {
        android.hardware.wifi.supplicant.V1_2.ISupplicant castFrom;
        synchronized (this.mLock) {
            castFrom = android.hardware.wifi.supplicant.V1_2.ISupplicant.castFrom((IHwInterface) android.hardware.wifi.supplicant.V1_0.ISupplicant.getService());
        }
        return castFrom;
    }

    /* access modifiers changed from: protected */
    public ISupplicantStaIface getStaIfaceMockable(ISupplicantIface iface) {
        ISupplicantStaIface asInterface;
        synchronized (this.mLock) {
            asInterface = ISupplicantStaIface.asInterface(iface.asBinder());
        }
        return asInterface;
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.supplicant.V1_1.ISupplicantStaIface getStaIfaceMockableV1_1(ISupplicantIface iface) {
        android.hardware.wifi.supplicant.V1_1.ISupplicantStaIface asInterface;
        synchronized (this.mLock) {
            asInterface = android.hardware.wifi.supplicant.V1_1.ISupplicantStaIface.asInterface(iface.asBinder());
        }
        return asInterface;
    }

    /* access modifiers changed from: protected */
    public IOppoSupplicantStaIface getOppoStaIfaceMockable(ISupplicantIface iface) {
        IOppoSupplicantStaIface asInterface;
        synchronized (this.mLock) {
            asInterface = IOppoSupplicantStaIface.asInterface(iface.asBinder());
        }
        return asInterface;
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.supplicant.V1_2.ISupplicantStaIface getStaIfaceMockableV1_2(ISupplicantIface iface) {
        android.hardware.wifi.supplicant.V1_2.ISupplicantStaIface asInterface;
        synchronized (this.mLock) {
            asInterface = android.hardware.wifi.supplicant.V1_2.ISupplicantStaIface.asInterface(iface.asBinder());
        }
        return asInterface;
    }

    private boolean isV1_1() {
        return checkHalVersionByInterfaceName(android.hardware.wifi.supplicant.V1_1.ISupplicant.kInterfaceName);
    }

    private boolean isV1_2() {
        return checkHalVersionByInterfaceName(android.hardware.wifi.supplicant.V1_2.ISupplicant.kInterfaceName);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0020, code lost:
        return r0;
     */
    private boolean checkHalVersionByInterfaceName(String interfaceName) {
        boolean z = false;
        if (interfaceName == null) {
            return false;
        }
        synchronized (this.mLock) {
            if (this.mIServiceManager == null) {
                Log.e(TAG, "checkHalVersionByInterfaceName: called but mServiceManager is null");
                return false;
            }
            try {
                if (this.mIServiceManager.getTransport(interfaceName, "default") != 0) {
                    z = true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Exception while operating on IServiceManager: " + e);
                handleRemoteException(e, "getTransport");
                return false;
            }
        }
    }

    private ISupplicantStaIface getStaIface(String ifaceName) {
        return this.mISupplicantStaIfaces.get(ifaceName);
    }

    private IOppoSupplicantStaIface getOppoStaIface(String ifaceName) {
        return this.mIOppoSupplicantStaIfaces.get(ifaceName);
    }

    private SupplicantStaNetworkHal getCurrentNetworkRemoteHandle(String ifaceName) {
        return this.mCurrentNetworkRemoteHandles.get(ifaceName);
    }

    /* access modifiers changed from: private */
    public WifiConfiguration getCurrentNetworkLocalConfig(String ifaceName) {
        return this.mCurrentNetworkLocalConfigs.get(ifaceName);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0063, code lost:
        return null;
     */
    private Pair<SupplicantStaNetworkHal, WifiConfiguration> addNetworkAndSaveConfig(String ifaceName, WifiConfiguration config) {
        synchronized (this.mLock) {
            logi("addSupplicantStaNetwork via HIDL");
            if (config == null) {
                loge("Cannot add NULL network!");
                return null;
            }
            SupplicantStaNetworkHal network = addNetwork(ifaceName);
            if (network == null) {
                loge("Failed to add a network!");
                return null;
            }
            boolean saveSuccess = false;
            try {
                saveSuccess = network.saveWifiConfiguration(config);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Exception while saving config params: " + config, e);
            }
            if (!saveSuccess) {
                loge("Failed to save variables for: " + config.configKey());
                if (!removeAllNetworks(ifaceName)) {
                    loge("Failed to remove all networks on failure.");
                }
            } else {
                Pair<SupplicantStaNetworkHal, WifiConfiguration> pair = new Pair<>(network, new WifiConfiguration(config));
                return pair;
            }
        }
    }

    public boolean connectToNetwork(String ifaceName, WifiConfiguration config) {
        synchronized (this.mLock) {
            logd("connectToNetwork " + config.configKey());
            WifiConfiguration currentConfig = getCurrentNetworkLocalConfig(ifaceName);
            if (!WifiConfigurationUtil.isSameNetwork(config, currentConfig)) {
                this.mCurrentNetworkRemoteHandles.remove(ifaceName);
                this.mCurrentNetworkLocalConfigs.remove(ifaceName);
                if (!removeAllNetworks(ifaceName)) {
                    loge("Failed to remove existing networks");
                    return false;
                }
                Pair<SupplicantStaNetworkHal, WifiConfiguration> pair = addNetworkAndSaveConfig(ifaceName, config);
                if (pair == null) {
                    loge("Failed to add/save network configuration: " + config.configKey());
                    this.mWifiMonitor.broadcastSaveConfigFailed(ifaceName, config.networkId);
                    return false;
                }
                this.mCurrentNetworkRemoteHandles.put(ifaceName, (SupplicantStaNetworkHal) pair.first);
                this.mCurrentNetworkLocalConfigs.put(ifaceName, (WifiConfiguration) pair.second);
            } else if (Objects.equals(config.getNetworkSelectionStatus().getNetworkSelectionBSSID(), currentConfig.getNetworkSelectionStatus().getNetworkSelectionBSSID())) {
                logd("Network is already saved, will not trigger remove and add operation.");
            } else {
                logd("Network is already saved, but need to update BSSID.");
                if (!setCurrentNetworkBssid(ifaceName, config.getNetworkSelectionStatus().getNetworkSelectionBSSID())) {
                    loge("Failed to set current network BSSID.");
                    return false;
                }
                this.mCurrentNetworkLocalConfigs.put(ifaceName, new WifiConfiguration(config));
            }
            if (OppoManuConnectManager.getInstance() != null && OppoManuConnectManager.getInstance().shouldClearBssid()) {
                if (this.mVerboseLoggingEnabled) {
                    Log.d(TAG, "reset bssid to any");
                }
                if (!(this.mCurrentNetworkRemoteHandles == null || this.mCurrentNetworkRemoteHandles.get(ifaceName) == null)) {
                    this.mCurrentNetworkRemoteHandles.get(ifaceName).setBssid("any");
                }
            }
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "connectToNetwork");
            if (networkHandle != null) {
                if (networkHandle.select()) {
                    return true;
                }
            }
            loge("Failed to select network configuration: " + config.configKey());
            return false;
        }
    }

    public boolean roamToNetwork(String ifaceName, WifiConfiguration config) {
        synchronized (this.mLock) {
            if (getCurrentNetworkId(ifaceName) != config.networkId) {
                Log.w(TAG, "Cannot roam to a different network, initiate new connection. Current network ID: " + getCurrentNetworkId(ifaceName));
                boolean connectToNetwork = connectToNetwork(ifaceName, config);
                return connectToNetwork;
            }
            String bssid = config.getNetworkSelectionStatus().getNetworkSelectionBSSID();
            logd("roamToNetwork" + config.configKey() + " (bssid " + bssid + ")");
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "roamToNetwork");
            if (networkHandle != null) {
                if (networkHandle.setBssid(bssid)) {
                    if (reassociate(ifaceName)) {
                        return true;
                    }
                    loge("Failed to trigger reassociate");
                    return false;
                }
            }
            loge("Failed to set new bssid on network: " + config.configKey());
            return false;
        }
    }

    public boolean loadNetworks(String ifaceName, Map<String, WifiConfiguration> configs, SparseArray<Map<String, String>> networkExtras) {
        synchronized (this.mLock) {
            try {
                List<Integer> networkIds = listNetworks(ifaceName);
                boolean z = false;
                if (networkIds == null) {
                    Log.e(TAG, "Failed to list networks");
                    return false;
                }
                for (Integer networkId : networkIds) {
                    SupplicantStaNetworkHal network = getNetwork(ifaceName, networkId.intValue());
                    if (network == null) {
                        Log.e(TAG, "Failed to get network with ID: " + networkId);
                        return z;
                    }
                    WifiConfiguration config = new WifiConfiguration();
                    Map<String, String> networkExtra = new HashMap<>();
                    boolean loadSuccess = false;
                    try {
                        loadSuccess = network.loadWifiConfiguration(config, networkExtra);
                    } catch (IllegalArgumentException e) {
                        Log.wtf(TAG, "Exception while loading config params: " + config, e);
                    }
                    if (!loadSuccess) {
                        Log.e(TAG, "Failed to load wifi configuration for network with ID: " + networkId + ". Skipping...");
                    } else {
                        config.setIpAssignment(IpConfiguration.IpAssignment.DHCP);
                        config.setProxySettings(IpConfiguration.ProxySettings.NONE);
                        networkExtras.put(networkId.intValue(), networkExtra);
                        WifiConfiguration duplicateConfig = configs.put(networkExtra.get("configKey"), config);
                        if (duplicateConfig != null) {
                            Log.i(TAG, "Replacing duplicate network: " + duplicateConfig.networkId);
                            removeNetwork(ifaceName, duplicateConfig.networkId);
                            networkExtras.remove(duplicateConfig.networkId);
                        }
                        z = false;
                    }
                }
                return true;
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    public void removeNetworkIfCurrent(String ifaceName, int networkId) {
        synchronized (this.mLock) {
            if (getCurrentNetworkId(ifaceName) == networkId) {
                removeAllNetworks(ifaceName);
            }
        }
    }

    public boolean removeAllNetworks(String ifaceName) {
        synchronized (this.mLock) {
            ArrayList<Integer> networks = listNetworks(ifaceName);
            if (networks == null) {
                Log.e(TAG, "removeAllNetworks failed, got null networks");
                return false;
            }
            Iterator<Integer> it = networks.iterator();
            while (it.hasNext()) {
                int id = it.next().intValue();
                if (!removeNetwork(ifaceName, id)) {
                    Log.e(TAG, "removeAllNetworks failed to remove network: " + id);
                    return false;
                }
            }
            this.mCurrentNetworkRemoteHandles.remove(ifaceName);
            this.mCurrentNetworkLocalConfigs.remove(ifaceName);
            return true;
        }
    }

    public boolean setCurrentNetworkBssid(String ifaceName, String bssidStr) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "setCurrentNetworkBssid");
            if (networkHandle == null) {
                return false;
            }
            boolean bssid = networkHandle.setBssid(bssidStr);
            return bssid;
        }
    }

    public String getCurrentNetworkWpsNfcConfigurationToken(String ifaceName) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "getCurrentNetworkWpsNfcConfigurationToken");
            if (networkHandle == null) {
                return null;
            }
            String wpsNfcConfigurationToken = networkHandle.getWpsNfcConfigurationToken();
            return wpsNfcConfigurationToken;
        }
    }

    public String getCurrentNetworkEapAnonymousIdentity(String ifaceName) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "getCurrentNetworkEapAnonymousIdentity");
            if (networkHandle == null) {
                return null;
            }
            String fetchEapAnonymousIdentity = networkHandle.fetchEapAnonymousIdentity();
            return fetchEapAnonymousIdentity;
        }
    }

    public boolean sendCurrentNetworkEapIdentityResponse(String ifaceName, String identity, String encryptedIdentity) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "sendCurrentNetworkEapIdentityResponse");
            if (networkHandle == null) {
                return false;
            }
            boolean sendNetworkEapIdentityResponse = networkHandle.sendNetworkEapIdentityResponse(identity, encryptedIdentity);
            return sendNetworkEapIdentityResponse;
        }
    }

    public boolean sendCurrentNetworkEapSimGsmAuthResponse(String ifaceName, String paramsStr) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "sendCurrentNetworkEapSimGsmAuthResponse");
            if (networkHandle == null) {
                return false;
            }
            boolean sendNetworkEapSimGsmAuthResponse = networkHandle.sendNetworkEapSimGsmAuthResponse(paramsStr);
            return sendNetworkEapSimGsmAuthResponse;
        }
    }

    public boolean sendCurrentNetworkEapSimGsmAuthFailure(String ifaceName) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "sendCurrentNetworkEapSimGsmAuthFailure");
            if (networkHandle == null) {
                return false;
            }
            boolean sendNetworkEapSimGsmAuthFailure = networkHandle.sendNetworkEapSimGsmAuthFailure();
            return sendNetworkEapSimGsmAuthFailure;
        }
    }

    public boolean sendCurrentNetworkEapSimUmtsAuthResponse(String ifaceName, String paramsStr) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "sendCurrentNetworkEapSimUmtsAuthResponse");
            if (networkHandle == null) {
                return false;
            }
            boolean sendNetworkEapSimUmtsAuthResponse = networkHandle.sendNetworkEapSimUmtsAuthResponse(paramsStr);
            return sendNetworkEapSimUmtsAuthResponse;
        }
    }

    public boolean sendCurrentNetworkEapSimUmtsAutsResponse(String ifaceName, String paramsStr) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "sendCurrentNetworkEapSimUmtsAutsResponse");
            if (networkHandle == null) {
                return false;
            }
            boolean sendNetworkEapSimUmtsAutsResponse = networkHandle.sendNetworkEapSimUmtsAutsResponse(paramsStr);
            return sendNetworkEapSimUmtsAutsResponse;
        }
    }

    public boolean sendCurrentNetworkEapSimUmtsAuthFailure(String ifaceName) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "sendCurrentNetworkEapSimUmtsAuthFailure");
            if (networkHandle == null) {
                return false;
            }
            boolean sendNetworkEapSimUmtsAuthFailure = networkHandle.sendNetworkEapSimUmtsAuthFailure();
            return sendNetworkEapSimUmtsAuthFailure;
        }
    }

    private SupplicantStaNetworkHal addNetwork(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "addNetwork");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<ISupplicantNetwork> newNetwork = new HidlSupport.Mutable<>();
            try {
                iface.addNetwork(new ISupplicantIface.addNetworkCallback(newNetwork) {
                    /* class com.android.server.wifi.$$Lambda$SupplicantStaIfaceHal$P0XrIC01M2gx34WN4z0GsE6UrQ */
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantIface.addNetworkCallback
                    public final void onValues(SupplicantStatus supplicantStatus, ISupplicantNetwork iSupplicantNetwork) {
                        SupplicantStaIfaceHal.this.lambda$addNetwork$4$SupplicantStaIfaceHal(this.f$1, supplicantStatus, iSupplicantNetwork);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "addNetwork");
            }
            if (newNetwork.value == null) {
                return null;
            }
            SupplicantStaNetworkHal staNetworkMockable = getStaNetworkMockable(ifaceName, ISupplicantStaNetwork.asInterface(((ISupplicantNetwork) newNetwork.value).asBinder()), IOppoSupplicantStaNetwork.asInterface(((ISupplicantNetwork) newNetwork.value).asBinder()));
            return staNetworkMockable;
        }
    }

    public /* synthetic */ void lambda$addNetwork$4$SupplicantStaIfaceHal(HidlSupport.Mutable newNetwork, SupplicantStatus status, ISupplicantNetwork network) {
        if (checkStatusAndLogFailure(status, "addNetwork")) {
            newNetwork.value = network;
        }
    }

    private boolean removeNetwork(String ifaceName, int id) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "removeNetwork");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.removeNetwork(id), "removeNetwork");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "removeNetwork");
                return false;
            }
        }
    }

    /* access modifiers changed from: protected */
    public SupplicantStaNetworkHal getStaNetworkMockable(String ifaceName, ISupplicantStaNetwork iSupplicantStaNetwork, IOppoSupplicantStaNetwork iOppoSupplicantStaNetworkHal) {
        SupplicantStaNetworkHal network;
        synchronized (this.mLock) {
            network = new SupplicantStaNetworkHal(iSupplicantStaNetwork, iOppoSupplicantStaNetworkHal, ifaceName, this.mContext, this.mWifiMonitor);
            network.enableVerboseLogging(this.mVerboseLoggingEnabled);
        }
        return network;
    }

    /* access modifiers changed from: protected */
    public SupplicantStaNetworkHal getStaNetworkMockable(String ifaceName, ISupplicantStaNetwork iSupplicantStaNetwork) {
        SupplicantStaNetworkHal network;
        synchronized (this.mLock) {
            network = new SupplicantStaNetworkHal(iSupplicantStaNetwork, ifaceName, this.mContext, this.mWifiMonitor);
            network.enableVerboseLogging(this.mVerboseLoggingEnabled);
        }
        return network;
    }

    private SupplicantStaNetworkHal getNetwork(String ifaceName, int id) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "getNetwork");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<ISupplicantNetwork> gotNetwork = new HidlSupport.Mutable<>();
            try {
                iface.getNetwork(id, new ISupplicantIface.getNetworkCallback(gotNetwork) {
                    /* class com.android.server.wifi.$$Lambda$SupplicantStaIfaceHal$q2DutNJgLVtt96dVkcCOZSQlIqs */
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantIface.getNetworkCallback
                    public final void onValues(SupplicantStatus supplicantStatus, ISupplicantNetwork iSupplicantNetwork) {
                        SupplicantStaIfaceHal.this.lambda$getNetwork$5$SupplicantStaIfaceHal(this.f$1, supplicantStatus, iSupplicantNetwork);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "getNetwork");
            }
            if (gotNetwork.value == null) {
                return null;
            }
            SupplicantStaNetworkHal staNetworkMockable = getStaNetworkMockable(ifaceName, ISupplicantStaNetwork.asInterface(((ISupplicantNetwork) gotNetwork.value).asBinder()));
            return staNetworkMockable;
        }
    }

    public /* synthetic */ void lambda$getNetwork$5$SupplicantStaIfaceHal(HidlSupport.Mutable gotNetwork, SupplicantStatus status, ISupplicantNetwork network) {
        if (checkStatusAndLogFailure(status, "getNetwork")) {
            gotNetwork.value = network;
        }
    }

    private boolean registerCallback(ISupplicantStaIface iface, ISupplicantStaIfaceCallback callback) {
        synchronized (this.mLock) {
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.registerCallback(callback), "registerCallback");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "registerCallback");
                return false;
            }
        }
    }

    private boolean registerCallbackV1_1(android.hardware.wifi.supplicant.V1_1.ISupplicantStaIface iface, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback callback) {
        synchronized (this.mLock) {
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.registerCallback_1_1(callback), "registerCallback_1_1");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "registerCallback_1_1");
                return false;
            }
        }
    }

    private boolean registerCallbackV1_2(android.hardware.wifi.supplicant.V1_2.ISupplicantStaIface iface, android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback callback) {
        synchronized (this.mLock) {
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.registerCallback_1_2(callback), "registerCallback_1_2");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "registerCallback_1_2");
                return false;
            }
        }
    }

    private ArrayList<Integer> listNetworks(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "listNetworks");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<ArrayList<Integer>> networkIdList = new HidlSupport.Mutable<>();
            try {
                iface.listNetworks(new ISupplicantIface.listNetworksCallback(networkIdList) {
                    /* class com.android.server.wifi.$$Lambda$SupplicantStaIfaceHal$3qKIt9CWj2EaLh3TOmfHE9kpCNk */
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantIface.listNetworksCallback
                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        SupplicantStaIfaceHal.this.lambda$listNetworks$6$SupplicantStaIfaceHal(this.f$1, supplicantStatus, arrayList);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "listNetworks");
            }
            ArrayList<Integer> arrayList = (ArrayList) networkIdList.value;
            return arrayList;
        }
    }

    public /* synthetic */ void lambda$listNetworks$6$SupplicantStaIfaceHal(HidlSupport.Mutable networkIdList, SupplicantStatus status, ArrayList networkIds) {
        if (checkStatusAndLogFailure(status, "listNetworks")) {
            networkIdList.value = networkIds;
        }
    }

    public boolean setWpsDeviceName(String ifaceName, String name) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setWpsDeviceName");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setWpsDeviceName(name), "setWpsDeviceName");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWpsDeviceName");
                return false;
            }
        }
    }

    public boolean setWpsDeviceType(String ifaceName, String typeStr) {
        synchronized (this.mLock) {
            try {
                Matcher match = WPS_DEVICE_TYPE_PATTERN.matcher(typeStr);
                if (match.find()) {
                    if (match.groupCount() == 3) {
                        short categ = Short.parseShort(match.group(1));
                        byte[] oui = NativeUtil.hexStringToByteArray(match.group(2));
                        short subCateg = Short.parseShort(match.group(3));
                        byte[] bytes = new byte[8];
                        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
                        byteBuffer.putShort(categ);
                        byteBuffer.put(oui);
                        byteBuffer.putShort(subCateg);
                        boolean wpsDeviceType = setWpsDeviceType(ifaceName, bytes);
                        return wpsDeviceType;
                    }
                }
                Log.e(TAG, "Malformed WPS device type " + typeStr);
                return false;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + typeStr, e);
                return false;
            }
        }
    }

    private boolean setWpsDeviceType(String ifaceName, byte[] type) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setWpsDeviceType");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setWpsDeviceType(type), "setWpsDeviceType");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWpsDeviceType");
                return false;
            }
        }
    }

    public boolean setWpsManufacturer(String ifaceName, String manufacturer) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setWpsManufacturer");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setWpsManufacturer(manufacturer), "setWpsManufacturer");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWpsManufacturer");
                return false;
            }
        }
    }

    public boolean setWpsModelName(String ifaceName, String modelName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setWpsModelName");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setWpsModelName(modelName), "setWpsModelName");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWpsModelName");
                return false;
            }
        }
    }

    public boolean setWpsModelNumber(String ifaceName, String modelNumber) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setWpsModelNumber");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setWpsModelNumber(modelNumber), "setWpsModelNumber");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWpsModelNumber");
                return false;
            }
        }
    }

    public boolean setWpsSerialNumber(String ifaceName, String serialNumber) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setWpsSerialNumber");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setWpsSerialNumber(serialNumber), "setWpsSerialNumber");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWpsSerialNumber");
                return false;
            }
        }
    }

    public boolean setWpsConfigMethods(String ifaceName, String configMethodsStr) {
        String[] configMethodsStrArr;
        boolean wpsConfigMethods;
        synchronized (this.mLock) {
            short configMethodsMask = 0;
            for (String str : configMethodsStr.split("\\s+")) {
                configMethodsMask = (short) (stringToWpsConfigMethod(str) | configMethodsMask);
            }
            wpsConfigMethods = setWpsConfigMethods(ifaceName, configMethodsMask);
        }
        return wpsConfigMethods;
    }

    private boolean setWpsConfigMethods(String ifaceName, short configMethods) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setWpsConfigMethods");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setWpsConfigMethods(configMethods), "setWpsConfigMethods");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWpsConfigMethods");
                return false;
            }
        }
    }

    public boolean reassociate(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "reassociate");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.reassociate(), "reassociate");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "reassociate");
                return false;
            }
        }
    }

    public boolean manuReassociate(String ifaceName) {
        synchronized (this.mLock) {
            IOppoSupplicantStaIface iface = checkOppoSupplicantStaIfaceAndLogFailure(ifaceName, "reassociate");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.manuReassociate(), "reassociate");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "reassociate");
                return false;
            }
        }
    }

    public String doStringCommand(String cmd, String ifaceName) {
        synchronized (this.mLock) {
            IOppoSupplicantStaIface iface = checkOppoSupplicantStaIfaceAndLogFailure(ifaceName, "doStringCommand");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> result = new HidlSupport.Mutable<>();
            try {
                iface.doStringCommand(cmd, new IOppoSupplicantStaIface.doStringCommandCallback(result) {
                    /* class com.android.server.wifi.$$Lambda$SupplicantStaIfaceHal$UVQgHDV9hLS1tLatvRllcUrEplk */
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // vendor.oppo.hardware.wifi.supplicant.V1_0.IOppoSupplicantStaIface.doStringCommandCallback
                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaIfaceHal.this.lambda$doStringCommand$7$SupplicantStaIfaceHal(this.f$1, supplicantStatus, str);
                    }
                });
                String str = (String) result.value;
                return str;
            } catch (RemoteException e) {
                handleRemoteException(e, "doStringCommand");
                return null;
            }
        }
    }

    public /* synthetic */ void lambda$doStringCommand$7$SupplicantStaIfaceHal(HidlSupport.Mutable result, SupplicantStatus status, String str) {
        if (checkStatusAndLogFailure(status, "doStringCommand")) {
            result.value = str;
        }
    }

    public boolean reconnect(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "reconnect");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.reconnect(), "reconnect");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "reconnect");
                return false;
            }
        }
    }

    public boolean disconnect(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "disconnect");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.disconnect(), "disconnect");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "disconnect");
                return false;
            }
        }
    }

    public boolean setPowerSave(String ifaceName, boolean enable) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setPowerSave");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setPowerSave(enable), "setPowerSave");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setPowerSave");
                return false;
            }
        }
    }

    public boolean initiateTdlsDiscover(String ifaceName, String macAddress) {
        boolean initiateTdlsDiscover;
        synchronized (this.mLock) {
            try {
                initiateTdlsDiscover = initiateTdlsDiscover(ifaceName, NativeUtil.macAddressToByteArray(macAddress));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + macAddress, e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return initiateTdlsDiscover;
    }

    private boolean initiateTdlsDiscover(String ifaceName, byte[] macAddress) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "initiateTdlsDiscover");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.initiateTdlsDiscover(macAddress), "initiateTdlsDiscover");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "initiateTdlsDiscover");
                return false;
            }
        }
    }

    public boolean initiateTdlsSetup(String ifaceName, String macAddress) {
        boolean initiateTdlsSetup;
        synchronized (this.mLock) {
            try {
                initiateTdlsSetup = initiateTdlsSetup(ifaceName, NativeUtil.macAddressToByteArray(macAddress));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + macAddress, e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return initiateTdlsSetup;
    }

    private boolean initiateTdlsSetup(String ifaceName, byte[] macAddress) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "initiateTdlsSetup");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.initiateTdlsSetup(macAddress), "initiateTdlsSetup");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "initiateTdlsSetup");
                return false;
            }
        }
    }

    public boolean initiateTdlsTeardown(String ifaceName, String macAddress) {
        boolean initiateTdlsTeardown;
        synchronized (this.mLock) {
            try {
                initiateTdlsTeardown = initiateTdlsTeardown(ifaceName, NativeUtil.macAddressToByteArray(macAddress));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + macAddress, e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return initiateTdlsTeardown;
    }

    private boolean initiateTdlsTeardown(String ifaceName, byte[] macAddress) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "initiateTdlsTeardown");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.initiateTdlsTeardown(macAddress), "initiateTdlsTeardown");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "initiateTdlsTeardown");
                return false;
            }
        }
    }

    public boolean initiateAnqpQuery(String ifaceName, String bssid, ArrayList<Short> infoElements, ArrayList<Integer> hs20SubTypes) {
        boolean initiateAnqpQuery;
        synchronized (this.mLock) {
            try {
                initiateAnqpQuery = initiateAnqpQuery(ifaceName, NativeUtil.macAddressToByteArray(bssid), infoElements, hs20SubTypes);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + bssid, e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return initiateAnqpQuery;
    }

    private boolean initiateAnqpQuery(String ifaceName, byte[] macAddress, ArrayList<Short> infoElements, ArrayList<Integer> subTypes) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "initiateAnqpQuery");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.initiateAnqpQuery(macAddress, infoElements, subTypes), "initiateAnqpQuery");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "initiateAnqpQuery");
                return false;
            }
        }
    }

    public boolean initiateHs20IconQuery(String ifaceName, String bssid, String fileName) {
        boolean initiateHs20IconQuery;
        synchronized (this.mLock) {
            try {
                initiateHs20IconQuery = initiateHs20IconQuery(ifaceName, NativeUtil.macAddressToByteArray(bssid), fileName);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + bssid, e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return initiateHs20IconQuery;
    }

    private boolean initiateHs20IconQuery(String ifaceName, byte[] macAddress, String fileName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "initiateHs20IconQuery");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.initiateHs20IconQuery(macAddress, fileName), "initiateHs20IconQuery");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "initiateHs20IconQuery");
                return false;
            }
        }
    }

    public String getMacAddress(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "getMacAddress");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> gotMac = new HidlSupport.Mutable<>();
            try {
                iface.getMacAddress(new ISupplicantStaIface.getMacAddressCallback(gotMac) {
                    /* class com.android.server.wifi.$$Lambda$SupplicantStaIfaceHal$RN5yy1Bc5d6E1Z6k9lqZIMdLATc */
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIface.getMacAddressCallback
                    public final void onValues(SupplicantStatus supplicantStatus, byte[] bArr) {
                        SupplicantStaIfaceHal.this.lambda$getMacAddress$8$SupplicantStaIfaceHal(this.f$1, supplicantStatus, bArr);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "getMacAddress");
            }
            String str = (String) gotMac.value;
            return str;
        }
    }

    public /* synthetic */ void lambda$getMacAddress$8$SupplicantStaIfaceHal(HidlSupport.Mutable gotMac, SupplicantStatus status, byte[] macAddr) {
        if (checkStatusAndLogFailure(status, "getMacAddress")) {
            gotMac.value = NativeUtil.macAddressFromByteArray(macAddr);
        }
    }

    public boolean startRxFilter(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "startRxFilter");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.startRxFilter(), "startRxFilter");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "startRxFilter");
                return false;
            }
        }
    }

    public boolean stopRxFilter(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "stopRxFilter");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.stopRxFilter(), "stopRxFilter");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "stopRxFilter");
                return false;
            }
        }
    }

    public boolean addRxFilter(String ifaceName, int type) {
        byte halType;
        synchronized (this.mLock) {
            if (type == 0) {
                halType = 0;
            } else if (type != 1) {
                try {
                    Log.e(TAG, "Invalid Rx Filter type: " + type);
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                halType = 1;
            }
            boolean addRxFilter = addRxFilter(ifaceName, halType);
            return addRxFilter;
        }
    }

    private boolean addRxFilter(String ifaceName, byte type) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "addRxFilter");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.addRxFilter(type), "addRxFilter");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "addRxFilter");
                return false;
            }
        }
    }

    public boolean removeRxFilter(String ifaceName, int type) {
        byte halType;
        synchronized (this.mLock) {
            if (type == 0) {
                halType = 0;
            } else if (type != 1) {
                try {
                    Log.e(TAG, "Invalid Rx Filter type: " + type);
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                halType = 1;
            }
            boolean removeRxFilter = removeRxFilter(ifaceName, halType);
            return removeRxFilter;
        }
    }

    private boolean removeRxFilter(String ifaceName, byte type) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "removeRxFilter");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.removeRxFilter(type), "removeRxFilter");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "removeRxFilter");
                return false;
            }
        }
    }

    public boolean setBtCoexistenceMode(String ifaceName, int mode) {
        byte halMode;
        synchronized (this.mLock) {
            if (mode == 0) {
                halMode = 0;
            } else if (mode == 1) {
                halMode = 1;
            } else if (mode != 2) {
                try {
                    Log.e(TAG, "Invalid Bt Coex mode: " + mode);
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                halMode = 2;
            }
            boolean btCoexistenceMode = setBtCoexistenceMode(ifaceName, halMode);
            return btCoexistenceMode;
        }
    }

    private boolean setBtCoexistenceMode(String ifaceName, byte mode) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setBtCoexistenceMode");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setBtCoexistenceMode(mode), "setBtCoexistenceMode");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setBtCoexistenceMode");
                return false;
            }
        }
    }

    public boolean setBtCoexistenceScanModeEnabled(String ifaceName, boolean enable) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setBtCoexistenceScanModeEnabled");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setBtCoexistenceScanModeEnabled(enable), "setBtCoexistenceScanModeEnabled");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setBtCoexistenceScanModeEnabled");
                return false;
            }
        }
    }

    public boolean setSuspendModeEnabled(String ifaceName, boolean enable) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setSuspendModeEnabled");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setSuspendModeEnabled(enable), "setSuspendModeEnabled");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setSuspendModeEnabled");
                return false;
            }
        }
    }

    public boolean setCountryCode(String ifaceName, String codeStr) {
        synchronized (this.mLock) {
            if (TextUtils.isEmpty(codeStr)) {
                return false;
            }
            byte[] countryCodeBytes = NativeUtil.stringToByteArray(codeStr);
            if (countryCodeBytes.length != 2) {
                return false;
            }
            boolean countryCode = setCountryCode(ifaceName, countryCodeBytes);
            return countryCode;
        }
    }

    private boolean setCountryCode(String ifaceName, byte[] code) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setCountryCode");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setCountryCode(code), "setCountryCode");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setCountryCode");
                return false;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0036, code lost:
        return false;
     */
    public boolean startWpsRegistrar(String ifaceName, String bssidStr, String pin) {
        synchronized (this.mLock) {
            if (!TextUtils.isEmpty(bssidStr) && !TextUtils.isEmpty(pin)) {
                try {
                    boolean startWpsRegistrar = startWpsRegistrar(ifaceName, NativeUtil.macAddressToByteArray(bssidStr), pin);
                    return startWpsRegistrar;
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Illegal argument " + bssidStr, e);
                    return false;
                }
            }
        }
    }

    private boolean startWpsRegistrar(String ifaceName, byte[] bssid, String pin) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "startWpsRegistrar");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.startWpsRegistrar(bssid, pin), "startWpsRegistrar");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "startWpsRegistrar");
                return false;
            }
        }
    }

    public boolean startWpsPbc(String ifaceName, String bssidStr) {
        boolean startWpsPbc;
        synchronized (this.mLock) {
            try {
                startWpsPbc = startWpsPbc(ifaceName, NativeUtil.macAddressToByteArray(bssidStr));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + bssidStr, e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return startWpsPbc;
    }

    private boolean startWpsPbc(String ifaceName, byte[] bssid) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "startWpsPbc");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.startWpsPbc(bssid), "startWpsPbc");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "startWpsPbc");
                return false;
            }
        }
    }

    public boolean startWpsPinKeypad(String ifaceName, String pin) {
        if (TextUtils.isEmpty(pin)) {
            return false;
        }
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "startWpsPinKeypad");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.startWpsPinKeypad(pin), "startWpsPinKeypad");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "startWpsPinKeypad");
                return false;
            }
        }
    }

    public String startWpsPinDisplay(String ifaceName, String bssidStr) {
        String startWpsPinDisplay;
        synchronized (this.mLock) {
            try {
                startWpsPinDisplay = startWpsPinDisplay(ifaceName, NativeUtil.macAddressToByteArray(bssidStr));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + bssidStr, e);
                return null;
            } catch (Throwable th) {
                throw th;
            }
        }
        return startWpsPinDisplay;
    }

    private String startWpsPinDisplay(String ifaceName, byte[] bssid) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "startWpsPinDisplay");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> gotPin = new HidlSupport.Mutable<>();
            try {
                iface.startWpsPinDisplay(bssid, new ISupplicantStaIface.startWpsPinDisplayCallback(gotPin) {
                    /* class com.android.server.wifi.$$Lambda$SupplicantStaIfaceHal$obLcU23CRIRzqBy01VuBJvlyUg */
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIface.startWpsPinDisplayCallback
                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaIfaceHal.this.lambda$startWpsPinDisplay$9$SupplicantStaIfaceHal(this.f$1, supplicantStatus, str);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "startWpsPinDisplay");
            }
            String str = (String) gotPin.value;
            return str;
        }
    }

    public /* synthetic */ void lambda$startWpsPinDisplay$9$SupplicantStaIfaceHal(HidlSupport.Mutable gotPin, SupplicantStatus status, String pin) {
        if (checkStatusAndLogFailure(status, "startWpsPinDisplay")) {
            gotPin.value = pin;
        }
    }

    public boolean cancelWps(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "cancelWps");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.cancelWps(), "cancelWps");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "cancelWps");
                return false;
            }
        }
    }

    public boolean setExternalSim(String ifaceName, boolean useExternalSim) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setExternalSim");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setExternalSim(useExternalSim), "setExternalSim");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setExternalSim");
                return false;
            }
        }
    }

    public boolean enableAutoReconnect(String ifaceName, boolean enable) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "enableAutoReconnect");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.enableAutoReconnect(enable), "enableAutoReconnect");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "enableAutoReconnect");
                return false;
            }
        }
    }

    public boolean setLogLevel(boolean turnOnVerbose) {
        synchronized (this.mLock) {
            int logLevel = 3;
            if (!"user".equals(Build.TYPE)) {
                if (turnOnVerbose) {
                    logLevel = 0;
                }
                boolean debugParams = setDebugParams(logLevel, true, true);
                return debugParams;
            }
            if (turnOnVerbose) {
                logLevel = 2;
            }
            boolean debugParams2 = setDebugParams(logLevel, false, false);
            return debugParams2;
        }
    }

    private boolean setDebugParams(int level, boolean showTimestamp, boolean showKeys) {
        synchronized (this.mLock) {
            if (!checkSupplicantAndLogFailure("setDebugParams")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicant.setDebugParams(level, showTimestamp, showKeys), "setDebugParams");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setDebugParams");
                return false;
            }
        }
    }

    public boolean setConcurrencyPriority(boolean isStaHigherPriority) {
        synchronized (this.mLock) {
            if (isStaHigherPriority) {
                boolean concurrencyPriority = setConcurrencyPriority(0);
                return concurrencyPriority;
            }
            boolean concurrencyPriority2 = setConcurrencyPriority(1);
            return concurrencyPriority2;
        }
    }

    private boolean setConcurrencyPriority(int type) {
        synchronized (this.mLock) {
            if (!checkSupplicantAndLogFailure("setConcurrencyPriority")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicant.setConcurrencyPriority(type), "setConcurrencyPriority");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setConcurrencyPriority");
                return false;
            }
        }
    }

    private boolean checkSupplicantAndLogFailure(String methodStr) {
        synchronized (this.mLock) {
            if (this.mISupplicant == null) {
                Log.e(TAG, "Can't call " + methodStr + ", ISupplicant is null");
                return false;
            }
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "Do ISupplicant." + methodStr);
            }
            return true;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0042, code lost:
        return r1;
     */
    private ISupplicantStaIface checkSupplicantStaIfaceAndLogFailure(String ifaceName, String methodStr) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                Log.e(TAG, "Can't call " + methodStr + ", ISupplicantStaIface is null");
                return null;
            } else if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "Do ISupplicantStaIface." + methodStr);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0042, code lost:
        return r1;
     */
    private IOppoSupplicantStaIface checkOppoSupplicantStaIfaceAndLogFailure(String ifaceName, String methodStr) {
        synchronized (this.mLock) {
            IOppoSupplicantStaIface iface = getOppoStaIface(ifaceName);
            if (iface == null) {
                Log.e(TAG, "Can't call " + methodStr + ", IOppoSupplicantStaIface is null");
                return null;
            } else if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "Do IOppoSupplicantStaIface." + methodStr);
            }
        }
    }

    private SupplicantStaNetworkHal checkSupplicantStaNetworkAndLogFailure(String ifaceName, String methodStr) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHal = getCurrentNetworkRemoteHandle(ifaceName);
            if (networkHal != null) {
                return networkHal;
            }
            Log.e(TAG, "Can't call " + methodStr + ", SupplicantStaNetwork is null");
            return null;
        }
    }

    private boolean checkStatusAndLogFailure(SupplicantStatus status, String methodStr) {
        synchronized (this.mLock) {
            if (status.code != 0) {
                Log.e(TAG, "ISupplicantStaIface." + methodStr + " failed: " + status);
                return false;
            }
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "ISupplicantStaIface." + methodStr + " succeeded");
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void logCallback(String methodStr) {
        synchronized (this.mLock) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "ISupplicantStaIfaceCallback." + methodStr + " received");
            }
        }
    }

    private void handleNoSuchElementException(NoSuchElementException e, String methodStr) {
        synchronized (this.mLock) {
            clearState();
            Log.e(TAG, "ISupplicantStaIface." + methodStr + " failed with exception", e);
        }
    }

    private void handleRemoteException(RemoteException e, String methodStr) {
        synchronized (this.mLock) {
            clearState();
            Log.e(TAG, "ISupplicantStaIface." + methodStr + " failed with exception", e);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static short stringToWpsConfigMethod(String configMethod) {
        char c;
        switch (configMethod.hashCode()) {
            case -1781962557:
                if (configMethod.equals("virtual_push_button")) {
                    c = 9;
                    break;
                }
                c = 65535;
                break;
            case -1419358249:
                if (configMethod.equals("ethernet")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1134657068:
                if (configMethod.equals("keypad")) {
                    c = 8;
                    break;
                }
                c = 65535;
                break;
            case -614489202:
                if (configMethod.equals("virtual_display")) {
                    c = 12;
                    break;
                }
                c = 65535;
                break;
            case -522593958:
                if (configMethod.equals("physical_display")) {
                    c = 13;
                    break;
                }
                c = 65535;
                break;
            case -423872603:
                if (configMethod.equals("nfc_interface")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -416734217:
                if (configMethod.equals("push_button")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 3388229:
                if (configMethod.equals("p2ps")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 3599197:
                if (configMethod.equals("usba")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 102727412:
                if (configMethod.equals("label")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 179612103:
                if (configMethod.equals("ext_nfc_token")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1146869903:
                if (configMethod.equals("physical_push_button")) {
                    c = 10;
                    break;
                }
                c = 65535;
                break;
            case 1671764162:
                if (configMethod.equals("display")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 2010140181:
                if (configMethod.equals("int_nfc_token")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 4;
            case 3:
                return 8;
            case 4:
                return 32;
            case 5:
                return 16;
            case 6:
                return 64;
            case 7:
                return WpsConfigMethods.PUSHBUTTON;
            case 8:
                return WpsConfigMethods.KEYPAD;
            case 9:
                return WpsConfigMethods.VIRT_PUSHBUTTON;
            case 10:
                return WpsConfigMethods.PHY_PUSHBUTTON;
            case 11:
                return WpsConfigMethods.P2PS;
            case 12:
                return WpsConfigMethods.VIRT_DISPLAY;
            case 13:
                return WpsConfigMethods.PHY_DISPLAY;
            default:
                throw new IllegalArgumentException("Invalid WPS config method: " + configMethod);
        }
    }

    /* access modifiers changed from: private */
    public static SupplicantState supplicantHidlStateToFrameworkState(int state) {
        switch (state) {
            case 0:
                return SupplicantState.DISCONNECTED;
            case 1:
                return SupplicantState.INTERFACE_DISABLED;
            case 2:
                return SupplicantState.INACTIVE;
            case 3:
                return SupplicantState.SCANNING;
            case 4:
                return SupplicantState.AUTHENTICATING;
            case 5:
                return SupplicantState.ASSOCIATING;
            case 6:
                return SupplicantState.ASSOCIATED;
            case 7:
                return SupplicantState.FOUR_WAY_HANDSHAKE;
            case 8:
                return SupplicantState.GROUP_HANDSHAKE;
            case 9:
                return SupplicantState.COMPLETED;
            default:
                throw new IllegalArgumentException("Invalid state: " + state);
        }
    }

    private class SupplicantStaIfaceHalCallback extends ISupplicantStaIfaceCallback.Stub {
        private String mIfaceName;
        private boolean mStateIsFourway = false;

        SupplicantStaIfaceHalCallback(String ifaceName) {
            this.mIfaceName = ifaceName;
        }

        private ANQPElement parseAnqpElement(Constants.ANQPElementType infoID, ArrayList<Byte> payload) {
            ANQPElement aNQPElement;
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                try {
                    if (Constants.getANQPElementID(infoID) != null) {
                        aNQPElement = ANQPParser.parseElement(infoID, ByteBuffer.wrap(NativeUtil.byteArrayFromArrayList(payload)));
                    } else {
                        aNQPElement = ANQPParser.parseHS20Element(infoID, ByteBuffer.wrap(NativeUtil.byteArrayFromArrayList(payload)));
                    }
                } catch (IOException | BufferUnderflowException e) {
                    Log.e(SupplicantStaIfaceHal.TAG, "Failed parsing ANQP element payload: " + infoID, e);
                    return null;
                } catch (Throwable th) {
                    throw th;
                }
            }
            return aNQPElement;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x001a, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x001c, code lost:
            return;
         */
        private void addAnqpElementToMap(Map<Constants.ANQPElementType, ANQPElement> elementsMap, Constants.ANQPElementType infoID, ArrayList<Byte> payload) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                if (payload != null) {
                    if (!payload.isEmpty()) {
                        ANQPElement element = parseAnqpElement(infoID, payload);
                        if (element != null) {
                            elementsMap.put(infoID, element);
                        }
                    }
                }
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onNetworkAdded(int id) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onNetworkAdded");
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onNetworkRemoved(int id) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onNetworkRemoved");
                this.mStateIsFourway = false;
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onStateChanged(int newState, byte[] bssid, int id, ArrayList<Byte> ssid) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onStateChanged");
                SupplicantState newSupplicantState = SupplicantStaIfaceHal.supplicantHidlStateToFrameworkState(newState);
                WifiSsid wifiSsid = WifiSsid.createFromByteArray(NativeUtil.byteArrayFromArrayList(ssid));
                GbkUtil.checkAndSetGbk(wifiSsid);
                String bssidStr = NativeUtil.macAddressFromByteArray(bssid);
                this.mStateIsFourway = newState == 7;
                if (newSupplicantState == SupplicantState.COMPLETED) {
                    SupplicantStaIfaceHal.this.mWifiMonitor.broadcastNetworkConnectionEvent(this.mIfaceName, SupplicantStaIfaceHal.this.getCurrentNetworkId(this.mIfaceName), bssidStr);
                }
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastSupplicantStateChangeEvent(this.mIfaceName, SupplicantStaIfaceHal.this.getCurrentNetworkId(this.mIfaceName), wifiSsid, bssidStr, newSupplicantState);
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onAnqpQueryDone(byte[] bssid, ISupplicantStaIfaceCallback.AnqpData data, ISupplicantStaIfaceCallback.Hs20AnqpData hs20Data) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onAnqpQueryDone");
                Map<Constants.ANQPElementType, ANQPElement> elementsMap = new HashMap<>();
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.ANQPVenueName, data.venueName);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.ANQPRoamingConsortium, data.roamingConsortium);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.ANQPIPAddrAvailability, data.ipAddrTypeAvailability);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.ANQPNAIRealm, data.naiRealm);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.ANQP3GPPNetwork, data.anqp3gppCellularNetwork);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.ANQPDomName, data.domainName);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.HSFriendlyName, hs20Data.operatorFriendlyName);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.HSWANMetrics, hs20Data.wanMetrics);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.HSConnCapability, hs20Data.connectionCapability);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.HSOSUProviders, hs20Data.osuProvidersList);
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastAnqpDoneEvent(this.mIfaceName, new AnqpEvent(NativeUtil.macAddressToLong(bssid).longValue(), elementsMap));
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onHs20IconQueryDone(byte[] bssid, String fileName, ArrayList<Byte> data) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onHs20IconQueryDone");
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastIconDoneEvent(this.mIfaceName, new IconEvent(NativeUtil.macAddressToLong(bssid).longValue(), fileName, data.size(), NativeUtil.byteArrayFromArrayList(data)));
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onHs20SubscriptionRemediation(byte[] bssid, byte osuMethod, String url) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onHs20SubscriptionRemediation");
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastWnmEvent(this.mIfaceName, new WnmData(NativeUtil.macAddressToLong(bssid).longValue(), url, osuMethod));
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onHs20DeauthImminentNotice(byte[] bssid, int reasonCode, int reAuthDelayInSec, String url) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onHs20DeauthImminentNotice");
                WifiMonitor access$1200 = SupplicantStaIfaceHal.this.mWifiMonitor;
                String str = this.mIfaceName;
                long longValue = NativeUtil.macAddressToLong(bssid).longValue();
                boolean z = true;
                if (reasonCode != 1) {
                    z = false;
                }
                access$1200.broadcastWnmEvent(str, new WnmData(longValue, url, z, reAuthDelayInSec));
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onDisconnected(byte[] bssid, boolean locallyGenerated, int reasonCode) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onDisconnected");
                if (SupplicantStaIfaceHal.this.mVerboseLoggingEnabled) {
                    Log.e(SupplicantStaIfaceHal.TAG, "onDisconnected 4way=" + this.mStateIsFourway + " locallyGenerated=" + locallyGenerated + " reasonCode=" + reasonCode);
                }
                if (this.mStateIsFourway && (!locallyGenerated || reasonCode != 17)) {
                    SupplicantStaIfaceHal.this.mWifiMonitor.broadcastAuthenticationFailureEvent(this.mIfaceName, 2, -1);
                }
                int i = 1;
                SupplicantStaIfaceHal.this.updateDismessageToStatistics(locallyGenerated ? 1 : 0, reasonCode);
                WifiMonitor access$1200 = SupplicantStaIfaceHal.this.mWifiMonitor;
                String str = this.mIfaceName;
                if (!locallyGenerated) {
                    i = 0;
                }
                access$1200.broadcastNetworkDisconnectionEvent(str, i, reasonCode, NativeUtil.macAddressFromByteArray(bssid));
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onAssociationRejected(byte[] bssid, int statusCode, boolean timedOut) {
            WifiConfiguration curConfiguration;
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onAssociationRejected");
                if (statusCode != 1 || (curConfiguration = SupplicantStaIfaceHal.this.getCurrentNetworkLocalConfig(this.mIfaceName)) == null || !curConfiguration.allowedKeyManagement.get(8)) {
                    SupplicantStaIfaceHal.this.mWifiMonitor.broadcastAssociationRejectionEvent(this.mIfaceName, statusCode, timedOut, NativeUtil.macAddressFromByteArray(bssid));
                    return;
                }
                SupplicantStaIfaceHal.this.logCallback("SAE incorrect password");
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastAuthenticationFailureEvent(this.mIfaceName, 2, -1);
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onAuthenticationTimeout(byte[] bssid) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onAuthenticationTimeout");
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastAuthenticationFailureEvent(this.mIfaceName, 1, -1);
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onBssidChanged(byte reason, byte[] bssid) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onBssidChanged");
                if (reason == 0) {
                    SupplicantStaIfaceHal.this.mWifiMonitor.broadcastTargetBssidEvent(this.mIfaceName, NativeUtil.macAddressFromByteArray(bssid));
                } else if (reason == 1) {
                    SupplicantStaIfaceHal.this.mWifiMonitor.broadcastAssociatedBssidEvent(this.mIfaceName, NativeUtil.macAddressFromByteArray(bssid));
                }
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onEapFailure() {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onEapFailure");
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastAuthenticationFailureEvent(this.mIfaceName, 3, -1);
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onWpsEventSuccess() {
            SupplicantStaIfaceHal.this.logCallback("onWpsEventSuccess");
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastWpsSuccessEvent(this.mIfaceName);
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onWpsEventFail(byte[] bssid, short configError, short errorInd) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onWpsEventFail");
                if (configError == 16 && errorInd == 0) {
                    SupplicantStaIfaceHal.this.mWifiMonitor.broadcastWpsTimeoutEvent(this.mIfaceName);
                } else {
                    SupplicantStaIfaceHal.this.mWifiMonitor.broadcastWpsFailEvent(this.mIfaceName, configError, errorInd);
                }
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onWpsEventPbcOverlap() {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onWpsEventPbcOverlap");
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastWpsOverlapEvent(this.mIfaceName);
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onExtRadioWorkStart(int id) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onExtRadioWorkStart");
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onExtRadioWorkTimeout(int id) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onExtRadioWorkTimeout");
            }
        }
    }

    private class SupplicantStaIfaceHalCallbackV1_1 extends ISupplicantStaIfaceCallback.Stub {
        private SupplicantStaIfaceHalCallback mCallbackV1_0;
        private String mIfaceName;

        SupplicantStaIfaceHalCallbackV1_1(String ifaceName, SupplicantStaIfaceHalCallback callback) {
            this.mIfaceName = ifaceName;
            this.mCallbackV1_0 = callback;
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onNetworkAdded(int id) {
            this.mCallbackV1_0.onNetworkAdded(id);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onNetworkRemoved(int id) {
            this.mCallbackV1_0.onNetworkRemoved(id);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onStateChanged(int newState, byte[] bssid, int id, ArrayList<Byte> ssid) {
            this.mCallbackV1_0.onStateChanged(newState, bssid, id, ssid);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onAnqpQueryDone(byte[] bssid, ISupplicantStaIfaceCallback.AnqpData data, ISupplicantStaIfaceCallback.Hs20AnqpData hs20Data) {
            this.mCallbackV1_0.onAnqpQueryDone(bssid, data, hs20Data);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onHs20IconQueryDone(byte[] bssid, String fileName, ArrayList<Byte> data) {
            this.mCallbackV1_0.onHs20IconQueryDone(bssid, fileName, data);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onHs20SubscriptionRemediation(byte[] bssid, byte osuMethod, String url) {
            this.mCallbackV1_0.onHs20SubscriptionRemediation(bssid, osuMethod, url);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onHs20DeauthImminentNotice(byte[] bssid, int reasonCode, int reAuthDelayInSec, String url) {
            this.mCallbackV1_0.onHs20DeauthImminentNotice(bssid, reasonCode, reAuthDelayInSec, url);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onDisconnected(byte[] bssid, boolean locallyGenerated, int reasonCode) {
            this.mCallbackV1_0.onDisconnected(bssid, locallyGenerated, reasonCode);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onAssociationRejected(byte[] bssid, int statusCode, boolean timedOut) {
            this.mCallbackV1_0.onAssociationRejected(bssid, statusCode, timedOut);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onAuthenticationTimeout(byte[] bssid) {
            this.mCallbackV1_0.onAuthenticationTimeout(bssid);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onBssidChanged(byte reason, byte[] bssid) {
            this.mCallbackV1_0.onBssidChanged(reason, bssid);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onEapFailure() {
            this.mCallbackV1_0.onEapFailure();
        }

        @Override // android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback
        public void onEapFailure_1_1(int code) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onEapFailure_1_1");
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastAuthenticationFailureEvent(this.mIfaceName, 3, code);
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onWpsEventSuccess() {
            this.mCallbackV1_0.onWpsEventSuccess();
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onWpsEventFail(byte[] bssid, short configError, short errorInd) {
            this.mCallbackV1_0.onWpsEventFail(bssid, configError, errorInd);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onWpsEventPbcOverlap() {
            this.mCallbackV1_0.onWpsEventPbcOverlap();
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onExtRadioWorkStart(int id) {
            this.mCallbackV1_0.onExtRadioWorkStart(id);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onExtRadioWorkTimeout(int id) {
            this.mCallbackV1_0.onExtRadioWorkTimeout(id);
        }
    }

    private class SupplicantStaIfaceHalCallbackV1_2 extends IOppoSupplicantStaIfaceCallback.Stub {
        private SupplicantStaIfaceHalCallbackV1_1 mCallbackV1_1;
        private String mIfaceName;

        SupplicantStaIfaceHalCallbackV1_2(String ifaceName, SupplicantStaIfaceHalCallbackV1_1 callback) {
            this.mCallbackV1_1 = callback;
            this.mIfaceName = ifaceName;
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onNetworkAdded(int id) {
            this.mCallbackV1_1.onNetworkAdded(id);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onNetworkRemoved(int id) {
            this.mCallbackV1_1.onNetworkRemoved(id);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onStateChanged(int newState, byte[] bssid, int id, ArrayList<Byte> ssid) {
            this.mCallbackV1_1.onStateChanged(newState, bssid, id, ssid);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onAnqpQueryDone(byte[] bssid, ISupplicantStaIfaceCallback.AnqpData data, ISupplicantStaIfaceCallback.Hs20AnqpData hs20Data) {
            this.mCallbackV1_1.onAnqpQueryDone(bssid, data, hs20Data);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onHs20IconQueryDone(byte[] bssid, String fileName, ArrayList<Byte> data) {
            this.mCallbackV1_1.onHs20IconQueryDone(bssid, fileName, data);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onHs20SubscriptionRemediation(byte[] bssid, byte osuMethod, String url) {
            this.mCallbackV1_1.onHs20SubscriptionRemediation(bssid, osuMethod, url);
        }

        @Override // vendor.oppo.hardware.wifi.supplicant.V1_0.IOppoSupplicantStaIfaceCallback
        public void onSelectNetwork(byte[] bssid) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onSelectNetwork");
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastSelectNetworkEvent(this.mIfaceName, SupplicantStaIfaceHal.this.getCurrentNetworkId(this.mIfaceName));
            }
        }

        @Override // vendor.oppo.hardware.wifi.supplicant.V1_0.IOppoSupplicantStaIfaceCallback
        public void onSsidTempDisabled(String reason) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onSsidTempDisabled");
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastSsidTempDisabled(this.mIfaceName, SupplicantStaIfaceHal.this.getCurrentNetworkId(this.mIfaceName), reason);
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onHs20DeauthImminentNotice(byte[] bssid, int reasonCode, int reAuthDelayInSec, String url) {
            this.mCallbackV1_1.onHs20DeauthImminentNotice(bssid, reasonCode, reAuthDelayInSec, url);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onDisconnected(byte[] bssid, boolean locallyGenerated, int reasonCode) {
            this.mCallbackV1_1.onDisconnected(bssid, locallyGenerated, reasonCode);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onAssociationRejected(byte[] bssid, int statusCode, boolean timedOut) {
            this.mCallbackV1_1.onAssociationRejected(bssid, statusCode, timedOut);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onAuthenticationTimeout(byte[] bssid) {
            this.mCallbackV1_1.onAuthenticationTimeout(bssid);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onBssidChanged(byte reason, byte[] bssid) {
            this.mCallbackV1_1.onBssidChanged(reason, bssid);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onEapFailure() {
            this.mCallbackV1_1.onEapFailure();
        }

        @Override // android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback
        public void onEapFailure_1_1(int code) {
            this.mCallbackV1_1.onEapFailure_1_1(code);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onWpsEventSuccess() {
            this.mCallbackV1_1.onWpsEventSuccess();
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onWpsEventFail(byte[] bssid, short configError, short errorInd) {
            this.mCallbackV1_1.onWpsEventFail(bssid, configError, errorInd);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onWpsEventPbcOverlap() {
            this.mCallbackV1_1.onWpsEventPbcOverlap();
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onExtRadioWorkStart(int id) {
            this.mCallbackV1_1.onExtRadioWorkStart(id);
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback
        public void onExtRadioWorkTimeout(int id) {
            this.mCallbackV1_1.onExtRadioWorkTimeout(id);
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback
        public void onDppSuccessConfigReceived(ArrayList<Byte> ssid, String password, byte[] psk, int securityAkm) {
            if (SupplicantStaIfaceHal.this.mDppCallback == null) {
                SupplicantStaIfaceHal.loge("onDppSuccessConfigReceived callback is null");
                return;
            }
            WifiConfiguration newWifiConfiguration = new WifiConfiguration();
            WifiSsid wifiSsid = WifiSsid.createFromByteArray(NativeUtil.byteArrayFromArrayList(ssid));
            GbkUtil.checkAndSetGbk(wifiSsid);
            newWifiConfiguration.SSID = "\"" + wifiSsid.toString() + "\"";
            if (password != null) {
                newWifiConfiguration.preSharedKey = "\"" + password + "\"";
            } else if (psk != null) {
                newWifiConfiguration.preSharedKey = psk.toString();
            }
            if (securityAkm == 2 || securityAkm == 1) {
                newWifiConfiguration.allowedKeyManagement.set(8);
                newWifiConfiguration.requirePMF = true;
            } else if (securityAkm == 0) {
                newWifiConfiguration.allowedKeyManagement.set(1);
            } else {
                onDppFailure(7);
                return;
            }
            newWifiConfiguration.creatorName = SupplicantStaIfaceHal.this.mContext.getPackageManager().getNameForUid(1010);
            newWifiConfiguration.allowedAuthAlgorithms.set(0);
            newWifiConfiguration.allowedPairwiseCiphers.set(2);
            newWifiConfiguration.allowedProtocols.set(1);
            newWifiConfiguration.status = 2;
            SupplicantStaIfaceHal.this.mDppCallback.onSuccessConfigReceived(newWifiConfiguration);
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback
        public void onDppSuccessConfigSent() {
            if (SupplicantStaIfaceHal.this.mDppCallback != null) {
                SupplicantStaIfaceHal.this.mDppCallback.onSuccessConfigSent();
            } else {
                SupplicantStaIfaceHal.loge("onSuccessConfigSent callback is null");
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback
        public void onDppProgress(int code) {
            if (SupplicantStaIfaceHal.this.mDppCallback != null) {
                SupplicantStaIfaceHal.this.mDppCallback.onProgress(code);
            } else {
                SupplicantStaIfaceHal.loge("onDppProgress callback is null");
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIfaceCallback
        public void onDppFailure(int code) {
            if (SupplicantStaIfaceHal.this.mDppCallback != null) {
                SupplicantStaIfaceHal.this.mDppCallback.onFailure(code);
            } else {
                SupplicantStaIfaceHal.loge("onDppFailure callback is null");
            }
        }
    }

    private static void logd(String s) {
        Log.d(TAG, s);
    }

    private static void logi(String s) {
        Log.i(TAG, s);
    }

    /* access modifiers changed from: private */
    public static void loge(String s) {
        Log.e(TAG, s);
    }

    public int getAdvancedKeyMgmtCapabilities(String ifaceName) {
        int advancedCapabilities = 0;
        int keyMgmtCapabilities = getKeyMgmtCapabilities(ifaceName);
        if ((keyMgmtCapabilities & 1024) != 0) {
            advancedCapabilities = 0 | 134217728;
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "getAdvancedKeyMgmtCapabilities: SAE supported");
            }
        }
        if ((131072 & keyMgmtCapabilities) != 0) {
            advancedCapabilities |= IWifiChip.ChipCapabilityMask.DUAL_BAND_DUAL_CHANNEL;
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "getAdvancedKeyMgmtCapabilities: SUITE_B supported");
            }
        }
        if ((4194304 & keyMgmtCapabilities) != 0) {
            advancedCapabilities |= 536870912;
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "getAdvancedKeyMgmtCapabilities: OWE supported");
            }
        }
        if ((8388608 & keyMgmtCapabilities) != 0) {
            advancedCapabilities |= Integer.MIN_VALUE;
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "getAdvancedKeyMgmtCapabilities: DPP supported");
            }
        }
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "getAdvancedKeyMgmtCapabilities: Capability flags = " + keyMgmtCapabilities);
        }
        return advancedCapabilities;
    }

    private int getKeyMgmtCapabilities(String ifaceName) {
        MutableBoolean status = new MutableBoolean(false);
        MutableInt keyMgmtMask = new MutableInt(0);
        if (isV1_2()) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "getKeyMgmtCapabilities");
            if (iface == null) {
                return 0;
            }
            android.hardware.wifi.supplicant.V1_2.ISupplicantStaIface staIfaceV12 = getStaIfaceMockableV1_2(iface);
            if (staIfaceV12 == null) {
                Log.e(TAG, "getKeyMgmtCapabilities: ISupplicantStaIface is null, cannot get advanced capabilities");
                return 0;
            }
            try {
                staIfaceV12.getKeyMgmtCapabilities(new ISupplicantStaIface.getKeyMgmtCapabilitiesCallback(status, keyMgmtMask) {
                    /* class com.android.server.wifi.$$Lambda$SupplicantStaIfaceHal$hjOhCW02yzYNlDwHJaely2ISv0 */
                    private final /* synthetic */ MutableBoolean f$1;
                    private final /* synthetic */ MutableInt f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIface.getKeyMgmtCapabilitiesCallback
                    public final void onValues(SupplicantStatus supplicantStatus, int i) {
                        SupplicantStaIfaceHal.this.lambda$getKeyMgmtCapabilities$10$SupplicantStaIfaceHal(this.f$1, this.f$2, supplicantStatus, i);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "getKeyMgmtCapabilities");
            }
        } else {
            Log.e(TAG, "Method getKeyMgmtCapabilities is not supported in existing HAL");
        }
        return keyMgmtMask.value;
    }

    public /* synthetic */ void lambda$getKeyMgmtCapabilities$10$SupplicantStaIfaceHal(MutableBoolean status, MutableInt keyMgmtMask, SupplicantStatus statusInternal, int keyMgmtMaskInternal) {
        status.value = statusInternal.code == 0;
        if (status.value) {
            keyMgmtMask.value = keyMgmtMaskInternal;
        }
        checkStatusAndLogFailure(statusInternal, "getKeyMgmtCapabilities");
    }

    public int addDppPeerUri(String ifaceName, String uri) {
        MutableBoolean status = new MutableBoolean(false);
        MutableInt bootstrapId = new MutableInt(-1);
        if (!isV1_2()) {
            Log.e(TAG, "Method addDppPeerUri is not supported in existing HAL");
            return -1;
        }
        android.hardware.wifi.supplicant.V1_0.ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "addDppPeerUri");
        if (iface == null) {
            return -1;
        }
        android.hardware.wifi.supplicant.V1_2.ISupplicantStaIface staIfaceV12 = getStaIfaceMockableV1_2(iface);
        if (staIfaceV12 == null) {
            Log.e(TAG, "addDppPeerUri: ISupplicantStaIface is null");
            return -1;
        }
        try {
            staIfaceV12.addDppPeerUri(uri, new ISupplicantStaIface.addDppPeerUriCallback(status, bootstrapId) {
                /* class com.android.server.wifi.$$Lambda$SupplicantStaIfaceHal$Mgp0Eoph8SizT8uo8FHJiCwug2k */
                private final /* synthetic */ MutableBoolean f$1;
                private final /* synthetic */ MutableInt f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // android.hardware.wifi.supplicant.V1_2.ISupplicantStaIface.addDppPeerUriCallback
                public final void onValues(SupplicantStatus supplicantStatus, int i) {
                    SupplicantStaIfaceHal.this.lambda$addDppPeerUri$11$SupplicantStaIfaceHal(this.f$1, this.f$2, supplicantStatus, i);
                }
            });
            return bootstrapId.value;
        } catch (RemoteException e) {
            handleRemoteException(e, "addDppPeerUri");
            return -1;
        }
    }

    public /* synthetic */ void lambda$addDppPeerUri$11$SupplicantStaIfaceHal(MutableBoolean status, MutableInt bootstrapId, SupplicantStatus statusInternal, int bootstrapIdInternal) {
        status.value = statusInternal.code == 0;
        if (status.value) {
            bootstrapId.value = bootstrapIdInternal;
        }
        checkStatusAndLogFailure(statusInternal, "addDppPeerUri");
    }

    public boolean removeDppUri(String ifaceName, int bootstrapId) {
        if (!isV1_2()) {
            Log.e(TAG, "Method removeDppUri is not supported in existing HAL");
            return false;
        }
        android.hardware.wifi.supplicant.V1_0.ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "removeDppUri");
        if (iface == null) {
            return false;
        }
        android.hardware.wifi.supplicant.V1_2.ISupplicantStaIface staIfaceV12 = getStaIfaceMockableV1_2(iface);
        if (staIfaceV12 == null) {
            Log.e(TAG, "removeDppUri: ISupplicantStaIface is null");
            return false;
        }
        try {
            return checkStatusAndLogFailure(staIfaceV12.removeDppUri(bootstrapId), "removeDppUri");
        } catch (RemoteException e) {
            handleRemoteException(e, "removeDppUri");
            return false;
        }
    }

    public boolean stopDppInitiator(String ifaceName) {
        android.hardware.wifi.supplicant.V1_0.ISupplicantStaIface iface;
        if (!isV1_2() || (iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "stopDppInitiator")) == null) {
            return false;
        }
        android.hardware.wifi.supplicant.V1_2.ISupplicantStaIface staIfaceV12 = getStaIfaceMockableV1_2(iface);
        if (staIfaceV12 == null) {
            Log.e(TAG, "stopDppInitiator: ISupplicantStaIface is null");
            return false;
        }
        try {
            return checkStatusAndLogFailure(staIfaceV12.stopDppInitiator(), "stopDppInitiator");
        } catch (RemoteException e) {
            handleRemoteException(e, "stopDppInitiator");
            return false;
        }
    }

    public boolean startDppConfiguratorInitiator(String ifaceName, int peerBootstrapId, int ownBootstrapId, String ssid, String password, String psk, int netRole, int securityAkm) {
        if (!isV1_2()) {
            Log.e(TAG, "Method startDppConfiguratorInitiator is not supported in existing HAL");
            return false;
        }
        android.hardware.wifi.supplicant.V1_0.ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "startDppConfiguratorInitiator");
        if (iface == null) {
            return false;
        }
        android.hardware.wifi.supplicant.V1_2.ISupplicantStaIface staIfaceV12 = getStaIfaceMockableV1_2(iface);
        if (staIfaceV12 == null) {
            Log.e(TAG, "startDppConfiguratorInitiator: ISupplicantStaIface is null");
            return false;
        }
        try {
            return checkStatusAndLogFailure(staIfaceV12.startDppConfiguratorInitiator(peerBootstrapId, ownBootstrapId, ssid, password != null ? password : "", psk != null ? psk : "", netRole, securityAkm), "startDppConfiguratorInitiator");
        } catch (RemoteException e) {
            handleRemoteException(e, "startDppConfiguratorInitiator");
            return false;
        }
    }

    public boolean startDppEnrolleeInitiator(String ifaceName, int peerBootstrapId, int ownBootstrapId) {
        if (!isV1_2()) {
            Log.e(TAG, "Method startDppEnrolleeInitiator is not supported in existing HAL");
            return false;
        }
        android.hardware.wifi.supplicant.V1_0.ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "startDppEnrolleeInitiator");
        if (iface == null) {
            return false;
        }
        android.hardware.wifi.supplicant.V1_2.ISupplicantStaIface staIfaceV12 = getStaIfaceMockableV1_2(iface);
        if (staIfaceV12 == null) {
            Log.e(TAG, "startDppEnrolleeInitiator: ISupplicantStaIface is null");
            return false;
        }
        try {
            return checkStatusAndLogFailure(staIfaceV12.startDppEnrolleeInitiator(peerBootstrapId, ownBootstrapId), "startDppEnrolleeInitiator");
        } catch (RemoteException e) {
            handleRemoteException(e, "startDppEnrolleeInitiator");
            return false;
        }
    }

    public void registerDppCallback(WifiNative.DppEventCallback dppCallback) {
        this.mDppCallback = dppCallback;
    }

    public boolean setOshareMod(String ifaceName, int cmd) {
        byte targetMode;
        synchronized (this.mLock) {
            IOppoSupplicantStaIface iface = checkOppoSupplicantStaIfaceAndLogFailure(ifaceName, "setOshareMod");
            if (iface == null) {
                Log.e(TAG, "Can't call setOshareMod, IOppoSupplicantStaIface is null");
                return false;
            }
            if (cmd == 1) {
                targetMode = 1;
            } else {
                targetMode = 0;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setOshareMod(targetMode), "setOshareMod");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setOshareMod");
                return false;
            }
        }
    }

    public void updateCurrentConfigPairByWps(String ifaceName, int frameworkNetId) {
        WifiInjector wifiInjector;
        WifiConfigManager wifiConfigManager;
        WifiConfiguration wifiConfiguration;
        if (frameworkNetId != -1 && (wifiInjector = WifiInjector.getInstance()) != null && (wifiConfigManager = wifiInjector.getWifiConfigManager()) != null && (wifiConfiguration = wifiConfigManager.getWifiConfigurationForAll(frameworkNetId)) != null) {
            ArrayList<Integer> networks = listNetworks(ifaceName);
            int supplicantNetId = -1;
            if (networks != null && networks.size() == 1) {
                supplicantNetId = networks.get(0).intValue();
            }
            if (supplicantNetId != -1) {
                if (this.mVerboseLoggingEnabled) {
                    logd("updateCurrentConfigByWps,frameworkNetId = " + frameworkNetId + " supplicantNetId" + supplicantNetId);
                }
                this.mCurrentNetworkRemoteHandles.remove(ifaceName);
                this.mCurrentNetworkLocalConfigs.remove(ifaceName);
                this.mCurrentNetworkRemoteHandles.put(ifaceName, getNetwork(ifaceName, supplicantNetId));
                this.mCurrentNetworkLocalConfigs.put(ifaceName, new WifiConfiguration(wifiConfiguration));
            }
        }
    }

    public void updateDismessageToStatistics(int locallyGenerated, int reasonCode) {
        WifiInjector wifiInjector = WifiInjector.getInstance();
        if (wifiInjector != null && wifiInjector.getWifiDisconStat() != null) {
            wifiInjector.getWifiDisconStat().updateDisconnectionReason(locallyGenerated, reasonCode);
        }
    }
}
