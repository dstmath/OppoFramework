package com.mediatek.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.wifi.supplicant.V1_0.ISupplicant;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.HidlSupport;
import android.os.IHwInterface;
import android.os.RemoteException;
import android.security.KeyStore;
import android.util.Log;
import android.util.MutableBoolean;
import com.android.server.wifi.SupplicantStaIfaceHal;
import com.android.server.wifi.SupplicantStaNetworkHal;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hotspot2.anqp.NAIRealmData;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicant;
import vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface;
import vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork;
import vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface;
import vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork;

public class MtkWapi {
    public static final int EID_WAPI = 68;
    private static final String TAG = "MtkWapi";
    private static final int WAPI_AUTH_KEY_MGMT_PSK = 41030656;
    private static final int WAPI_AUTH_KEY_MGMT_WAI = 24253440;
    private static final int WAPI_VERSION = 1;
    public static boolean mIsCheckedSupport = false;
    public static boolean mIsSystemSupportWapi = false;
    private static ISupplicantStaIface mMtkiface;
    public static String[] mWapiCertSelCache;
    private static MtkWapi sMtkWapi = null;

    public static String parseWapiElement(ScanResult.InformationElement ie) {
        ByteBuffer buf = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
        Log.d("InformationElementUtil.WAPI", "parseWapiElement start");
        try {
            if (buf.getShort() != 1) {
                Log.e("InformationElementUtil.WAPI", "incorrect WAPI version");
                return null;
            }
            int count = buf.getShort();
            if (count != 1) {
                Log.e("InformationElementUtil.WAPI", "WAPI IE invalid AKM count: " + count);
            }
            String security = "[WAPI";
            int keyMgmt = buf.getInt();
            if (keyMgmt == WAPI_AUTH_KEY_MGMT_WAI) {
                security = security + "-CERT";
            } else if (keyMgmt == WAPI_AUTH_KEY_MGMT_PSK) {
                security = security + "-PSK";
            }
            return security + "]";
        } catch (BufferUnderflowException e) {
            Log.e("IE_Capabilities", "Couldn't parse WAPI element, buffer underflow");
            return null;
        }
    }

    public static boolean updateWapiCertSelList(WifiConfiguration config) {
        try {
            String[] aliases = KeyStore.getInstance().list("WAPI_CACERT_", 1010);
            Arrays.sort(aliases);
            StringBuilder sortedAliases = new StringBuilder();
            for (String alias : aliases) {
                sortedAliases.append(alias);
                sortedAliases.append(NAIRealmData.NAI_REALM_STRING_SEPARATOR);
            }
            if (!isWapiCertSelListChanged(aliases) || setWapiCertAliasList(sortedAliases.toString())) {
                return true;
            }
            Log.e(TAG, "failed to set alias list: " + sortedAliases.toString());
            return false;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public static boolean isWapiCertSelListChanged(String[] newWapiCertSel) {
        String[] strArr = mWapiCertSelCache;
        if (strArr != null && Arrays.equals(strArr, newWapiCertSel)) {
            return false;
        }
        mWapiCertSelCache = newWapiCertSel;
        return true;
    }

    public static boolean hasWapiConfigChanged(WifiConfiguration config, WifiConfiguration config1) {
        if (!mIsSystemSupportWapi || !isConfigForWapiNetwork(config) || !isConfigForWapiNetwork(config1)) {
            return false;
        }
        if (config.allowedKeyManagement.get(13) && config1.allowedKeyManagement.get(13)) {
            return false;
        }
        if (config.wapiCertSel == null) {
            if (config1.wapiCertSel == null) {
                return false;
            }
            return true;
        } else if (!config.wapiCertSel.equals(config1.wapiCertSel)) {
            return true;
        } else {
            return false;
        }
    }

    public static MtkWapi getInstance() {
        if (sMtkWapi == null) {
            synchronized (TAG) {
                sMtkWapi = new MtkWapi();
            }
        }
        return sMtkWapi;
    }

    public static boolean isConfigForWapiNetwork(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        if (isWapiPskConfiguration(config) || isWapiCertConfiguration(config)) {
            return true;
        }
        return false;
    }

    public static boolean isWapiPskConfiguration(WifiConfiguration config) {
        if (config != null && config.allowedKeyManagement.get(13)) {
            return true;
        }
        return false;
    }

    public static boolean isWapiCertConfiguration(WifiConfiguration config) {
        if (config != null && config.allowedKeyManagement.get(14)) {
            return true;
        }
        return false;
    }

    public static String generateCapabilitiesString(ScanResult.InformationElement[] ies, BitSet beaconCap, String capabilities) {
        String isWAPI = null;
        if (ies == null || beaconCap == null) {
            return capabilities;
        }
        if (!mIsCheckedSupport && checkSupportWapi()) {
            init();
        }
        if (!mIsSystemSupportWapi) {
            return capabilities;
        }
        for (ScanResult.InformationElement ie : ies) {
            if (ie.id == 68) {
                isWAPI = parseWapiElement(ie);
            }
        }
        if (isWAPI != null) {
            capabilities = capabilities + isWAPI;
            if (capabilities.contains("[WEP]")) {
                return capabilities.replace("[WEP]", "");
            }
        }
        return capabilities;
    }

    public static boolean isScanResultForWapiNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("WAPI");
    }

    public static void setupMtkIface(String ifaceName) {
        ISupplicantIface mtkIfaceHwBinder = getMtkIfaceV2_0(ifaceName);
        if (mtkIfaceHwBinder == null) {
            Log.e(TAG, "setupMtkIface got null iface");
            return;
        }
        Log.e(TAG, "mtkIfaceHwBinder get successfully");
        mMtkiface = getMtkStaIfaceMockableV2_0(mtkIfaceHwBinder);
        if (mMtkiface == null) {
            Log.e(TAG, "Mtk sta iface null");
        }
    }

    protected static ISupplicantStaIface getMtkStaIfaceMockableV2_0(ISupplicantIface iface) {
        ISupplicantStaIface asInterface;
        synchronized (getLockForSupplicantStaIfaceHal()) {
            asInterface = ISupplicantStaIface.asInterface(iface.asBinder());
        }
        return asInterface;
    }

    private static ISupplicantIface getMtkIfaceV2_0(String ifaceName) {
        synchronized (getLockForSupplicantStaIfaceHal()) {
            ArrayList<ISupplicant.IfaceInfo> supplicantIfaces = new ArrayList<>();
            try {
                ISupplicant supplicant = getISupplicant();
                if (supplicant == null) {
                    return null;
                }
                supplicant.listInterfaces(new ISupplicant.listInterfacesCallback(supplicantIfaces) {
                    /* class com.mediatek.server.wifi.$$Lambda$MtkWapi$S0hFtMozywQDJr6gEnr6b_jwsno */
                    private final /* synthetic */ ArrayList f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicant.listInterfacesCallback
                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        MtkWapi.lambda$getMtkIfaceV2_0$0(this.f$0, supplicantStatus, arrayList);
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
                            vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicant mtkSupplicantIface = getMtkSupplicantMockableV2_0();
                            if (mtkSupplicantIface == null) {
                                return null;
                            }
                            mtkSupplicantIface.getInterface(ifaceInfo, new ISupplicant.getInterfaceCallback(supplicantIface) {
                                /* class com.mediatek.server.wifi.$$Lambda$MtkWapi$p5q4jBPCMcdUQryIocxNaBG52QI */
                                private final /* synthetic */ HidlSupport.Mutable f$0;

                                {
                                    this.f$0 = r1;
                                }

                                @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicant.getInterfaceCallback
                                public final void onValues(SupplicantStatus supplicantStatus, ISupplicantIface iSupplicantIface) {
                                    MtkWapi.lambda$getMtkIfaceV2_0$1(this.f$0, supplicantStatus, iSupplicantIface);
                                }
                            });
                        } catch (RemoteException e) {
                            Log.e(TAG, "ISupplicant.getInterface exception: " + e);
                            supplicantServiceDiedHandler(ifaceName);
                            return null;
                        } catch (IllegalArgumentException e2) {
                            Log.e(TAG, "ISupplicant.getInterface IllegalArgumentException: " + e2);
                            supplicantServiceDiedHandler(ifaceName);
                            return null;
                        } catch (Exception e3) {
                            Log.e(TAG, "ISupplicant.getInterface Exception: " + e3);
                            supplicantServiceDiedHandler(ifaceName);
                            return null;
                        }
                    }
                }
                return (ISupplicantIface) supplicantIface.value;
            } catch (RemoteException e4) {
                Log.e(TAG, "ISupplicant.listInterfaces exception: " + e4);
                supplicantServiceDiedHandler(ifaceName);
                return null;
            } catch (IllegalArgumentException e5) {
                Log.e(TAG, "ISupplicant.listInterfaces IllegalArgumentException: " + e5);
                supplicantServiceDiedHandler(ifaceName);
                return null;
            } catch (Exception e6) {
                Log.e(TAG, "ISupplicant.listInterfaces Exception: " + e6);
                supplicantServiceDiedHandler(ifaceName);
                return null;
            }
        }
    }

    static /* synthetic */ void lambda$getMtkIfaceV2_0$0(ArrayList supplicantIfaces, SupplicantStatus status, ArrayList ifaces) {
        if (status.code != 0) {
            Log.e(TAG, "Getting Supplicant Interfaces failed: " + status.code);
            return;
        }
        supplicantIfaces.addAll(ifaces);
    }

    static /* synthetic */ void lambda$getMtkIfaceV2_0$1(HidlSupport.Mutable supplicantIface, SupplicantStatus status, ISupplicantIface iface) {
        if (status.code != 0) {
            Log.e(TAG, "Failed to get ISupplicantIface " + status.code);
            return;
        }
        supplicantIface.value = iface;
    }

    protected static vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicant getMtkSupplicantMockableV2_0() throws RemoteException {
        vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicant castFrom;
        synchronized (getLockForSupplicantStaIfaceHal()) {
            try {
                castFrom = vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicant.castFrom((IHwInterface) vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicant.getService());
            } catch (NoSuchElementException e) {
                Log.e(TAG, "Failed to get IMtkSupplicant", e);
                return null;
            } catch (Throwable th) {
                throw th;
            }
        }
        return castFrom;
    }

    private static boolean checkSupportWapi() {
        SupplicantStaIfaceHal supplicant = WifiInjector.getInstance().getSupplicantStaIfaceHal();
        synchronized (getLock(supplicant)) {
            if (!checkMtkIfaceAndLogFailure("getMtkFeatureMask")) {
                return false;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                mMtkiface.getFeatureMask(new ISupplicantStaIface.getFeatureMaskCallback(statusOk, supplicant) {
                    /* class com.mediatek.server.wifi.$$Lambda$MtkWapi$8yLCyqgbhS7OlFDMLfhPVQcQjvQ */
                    private final /* synthetic */ MutableBoolean f$0;
                    private final /* synthetic */ SupplicantStaIfaceHal f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface.getFeatureMaskCallback
                    public final void onValues(SupplicantStatus supplicantStatus, int i) {
                        MtkWapi.lambda$checkSupportWapi$2(this.f$0, this.f$1, supplicantStatus, i);
                    }
                });
                return statusOk.value;
            } catch (RemoteException e) {
                handleRemoteException(supplicant, e, "getMtkFeatureMask");
                return false;
            }
        }
    }

    static /* synthetic */ void lambda$checkSupportWapi$2(MutableBoolean statusOk, SupplicantStaIfaceHal supplicant, SupplicantStatus status, int maskValue) {
        boolean z = false;
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            if ((maskValue & 1) == 1) {
                z = true;
            }
            mIsSystemSupportWapi = z;
            mIsCheckedSupport = true;
        }
        checkStatusAndLogFailure(supplicant, status, "getMtkFeatureMask");
    }

    public static boolean setWapiCertAliasList(String aliases) {
        SupplicantStaIfaceHal supplicant = WifiInjector.getInstance().getSupplicantStaIfaceHal();
        synchronized (getLock(supplicant)) {
            if (!checkMtkIfaceAndLogFailure("setWapiCertAliasList")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(supplicant, mMtkiface.setWapiCertAliasList(aliases), "setWapiCertAliasList");
            } catch (RemoteException e) {
                handleRemoteException(supplicant, e, "setWapiCertAliasList");
                return false;
            }
        }
    }

    public static boolean setWapiCertAlias(SupplicantStaNetworkHal network, int supplicantNetworkId, String alias) {
        Log.d(TAG, "supplicantNetworkId= " + supplicantNetworkId);
        if (network == null) {
            return false;
        }
        synchronized (getLock(network)) {
            try {
                ISupplicantStaNetwork mtkIface = getMtkStaNetwork(supplicantNetworkId);
                if (mtkIface == null) {
                    Log.e(TAG, "setWapiCertAlias NullPoint Exception");
                    return false;
                }
                return checkStatusAndLogFailure(network, mtkIface.setWapiCertAlias(alias == null ? "NULL" : alias), "setWapiCertAlias");
            } catch (RemoteException e) {
                handleRemoteException(network, e, "setWapiCertAlias");
                return false;
            } catch (IllegalArgumentException e2) {
                Log.e(TAG, "setWapiCertAlias IllegalArgumentException Exception", e2);
                return false;
            } catch (Exception e3) {
                Log.e(TAG, "setWapiCertAlias Exception", e3);
                return false;
            }
        }
    }

    private static ISupplicantStaNetwork getMtkStaNetwork(int id) {
        HidlSupport.Mutable<ISupplicantNetwork> gotNetwork = new HidlSupport.Mutable<>();
        try {
            mMtkiface.getNetwork(id, new ISupplicantIface.getNetworkCallback(gotNetwork) {
                /* class com.mediatek.server.wifi.$$Lambda$MtkWapi$5S7RBdlxyzqEN86P8UDTAYmcc3A */
                private final /* synthetic */ HidlSupport.Mutable f$0;

                {
                    this.f$0 = r1;
                }

                @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface.getNetworkCallback
                public final void onValues(SupplicantStatus supplicantStatus, ISupplicantNetwork iSupplicantNetwork) {
                    MtkWapi.lambda$getMtkStaNetwork$3(this.f$0, supplicantStatus, iSupplicantNetwork);
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "MtkStaIface.getMtkStaNetwork failed with exception", e);
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "MtkStaIface.getMtkStaNetwork failed with IllegalArgumentException", e2);
        } catch (Exception e3) {
            Log.e(TAG, "MtkStaIface.getMtkStaNetwork failed with Exception", e3);
        }
        if (gotNetwork.value != null) {
            return ISupplicantStaNetwork.asInterface(((ISupplicantNetwork) gotNetwork.value).asBinder());
        }
        return null;
    }

    static /* synthetic */ void lambda$getMtkStaNetwork$3(HidlSupport.Mutable gotNetwork, SupplicantStatus status, ISupplicantNetwork network) {
        if (checkStatusAndLogFailure(status, "getMtkStaNetwork")) {
            gotNetwork.value = network;
        }
    }

    private static Object getLockForSupplicantStaIfaceHal() {
        return getLock(WifiInjector.getInstance().getSupplicantStaIfaceHal());
    }

    private static Object getLock(SupplicantStaIfaceHal supplicant) {
        try {
            Field lockField = supplicant.getClass().getDeclaredField("mLock");
            lockField.setAccessible(true);
            return lockField.get(supplicant);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return new Object();
        }
    }

    private static android.hardware.wifi.supplicant.V1_0.ISupplicant getISupplicant() {
        SupplicantStaIfaceHal supplicant = WifiInjector.getInstance().getSupplicantStaIfaceHal();
        try {
            Field field = supplicant.getClass().getDeclaredField("mISupplicant");
            field.setAccessible(true);
            return (android.hardware.wifi.supplicant.V1_0.ISupplicant) field.get(supplicant);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean checkStatusAndLogFailure(SupplicantStatus status, String methodStr) {
        if (status.code != 0) {
            Log.e(TAG, "ISupplicantStaIface." + methodStr + " failed: " + status);
            return false;
        }
        Log.d(TAG, "ISupplicantStaIface." + methodStr + " succeeded");
        return true;
    }

    private static String getInterfaceName() {
        WifiNative wifiNative = WifiInjector.getInstance().getWifiNative();
        try {
            Method method = wifiNative.getClass().getDeclaredMethod("getClientInterfaceName", new Class[0]);
            method.setAccessible(true);
            return (String) method.invoke(wifiNative, new Object[0]);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static void supplicantServiceDiedHandler(String ifaceName) {
        SupplicantStaIfaceHal supplicant = WifiInjector.getInstance().getSupplicantStaIfaceHal();
        try {
            Method method = supplicant.getClass().getDeclaredMethod("supplicantServiceDiedHandler", String.class);
            method.setAccessible(true);
            method.invoke(supplicant, ifaceName);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkStatusAndLogFailure(SupplicantStaIfaceHal supplicant, SupplicantStatus status, String methodStr) {
        try {
            Method method = supplicant.getClass().getDeclaredMethod("checkStatusAndLogFailure", SupplicantStatus.class, String.class);
            method.setAccessible(true);
            return ((Boolean) method.invoke(supplicant, status, methodStr)).booleanValue();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void handleRemoteException(SupplicantStaIfaceHal supplicant, RemoteException re, String methodStr) {
        try {
            Method method = supplicant.getClass().getDeclaredMethod("handleRemoteException", RemoteException.class, String.class);
            method.setAccessible(true);
            method.invoke(supplicant, re, methodStr);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private static Object getLock(SupplicantStaNetworkHal network) {
        try {
            Field field = network.getClass().getDeclaredField("mLock");
            field.setAccessible(true);
            return field.get(network);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return new Object();
        }
    }

    private static boolean checkMtkIfaceAndLogFailure(String methodStr) {
        if (mMtkiface == null) {
            Log.e(TAG, "Can't call " + methodStr + ", Mtkiface is null");
            return false;
        }
        Log.d(TAG, "Do Mtkiface." + methodStr);
        return true;
    }

    private static boolean checkStatusAndLogFailure(SupplicantStaNetworkHal network, SupplicantStatus status, String methodStr) {
        try {
            Method method = network.getClass().getDeclaredMethod("checkStatusAndLogFailure", SupplicantStatus.class, String.class);
            method.setAccessible(true);
            return ((Boolean) method.invoke(network, status, methodStr)).booleanValue();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void handleRemoteException(SupplicantStaNetworkHal network, RemoteException re, String methodStr) {
        try {
            Method method = network.getClass().getDeclaredMethod("handleRemoteException", RemoteException.class, String.class);
            method.setAccessible(true);
            method.invoke(network, re, methodStr);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private static Context getContext() {
        WifiInjector wi = WifiInjector.getInstance();
        try {
            Field field = wi.getClass().getDeclaredField("mContext");
            field.setAccessible(true);
            return (Context) field.get(wi);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void init() {
        Context context = getContext();
        if (context != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            context.registerReceiver(new BroadcastReceiver() {
                /* class com.mediatek.server.wifi.MtkWapi.AnonymousClass1 */

                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                        int state = intent.getIntExtra("wifi_state", 4);
                        Log.d(MtkWapi.TAG, "onReceive WIFI_STATE_CHANGED_ACTION state --> " + state);
                        if (state == 1) {
                            MtkWapi.mWapiCertSelCache = null;
                        }
                    }
                }
            }, new IntentFilter(filter));
        }
    }
}
