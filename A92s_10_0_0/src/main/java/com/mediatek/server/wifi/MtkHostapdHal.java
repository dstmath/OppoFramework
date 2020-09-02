package com.mediatek.server.wifi;

import android.content.Context;
import android.hardware.wifi.hostapd.V1_0.HostapdStatus;
import android.hardware.wifi.hostapd.V1_0.IHostapd;
import android.net.wifi.WifiConfiguration;
import android.os.IHwInterface;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.HostapdHal;
import com.android.server.wifi.OppoSoftapP2pBandControl;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.util.NativeUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import javax.annotation.concurrent.ThreadSafe;
import vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd;
import vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapdCallback;

@ThreadSafe
public class MtkHostapdHal {
    private static final String TAG = "MtkHostapdHal";
    private static final String WIFI_HOTSPOT_FIX_CHANNEL_FLAG_VALUE = "oppo.wifi.hotspot.fix.channel.flag";
    private static String sIfaceName;

    /* JADX WARNING: Code restructure failed: missing block: B:65:0x019d, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x01a1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x01a2, code lost:
        r1 = new java.lang.StringBuilder();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:?, code lost:
        r1.append("IHostapd.getService exception: ");
        r1.append(r0);
        android.util.Log.e(com.mediatek.server.wifi.MtkHostapdHal.TAG, r1.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x01bb, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x01eb, code lost:
        r0 = th;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x01a1 A[ExcHandler: NoSuchElementException (r0v36 'e' java.util.NoSuchElementException A[CUSTOM_DECLARE]), Splitter:B:54:0x0179] */
    public static boolean addAccessPoint(String ifaceName, WifiConfiguration config) {
        sIfaceName = ifaceName;
        Context context = getContext();
        boolean enableAcs = context.getResources().getBoolean(17891592);
        boolean enableIeee80211AC = context.getResources().getBoolean(17891593);
        HostapdHal hostapdHal = getHostapdHal();
        OppoSoftapP2pBandControl mOppoSoftapP2pBandControl = WifiInjector.getInstance().getOppoSoftapP2pBandControl();
        synchronized (getLock(hostapdHal)) {
            try {
                IHostapd.IfaceParams ifaceParams = new IHostapd.IfaceParams();
                ifaceParams.ifaceName = ifaceName;
                ifaceParams.hwModeParams.enable80211N = true;
                ifaceParams.hwModeParams.enable80211AC = enableIeee80211AC;
                try {
                    ifaceParams.channelParams.band = getBand(hostapdHal, config);
                    if (mOppoSoftapP2pBandControl.isSoftapInOnly2GRegion()) {
                        try {
                            if (mOppoSoftapP2pBandControl.needSoftapUse2GOnlyCountry()) {
                                ifaceParams.channelParams.band = 0;
                                Log.d(TAG, "need set 2.4g ");
                            }
                        } catch (Throwable th) {
                            e = th;
                            throw e;
                        }
                    }
                    if (enableAcs) {
                        ifaceParams.channelParams.enableAcs = true;
                        ifaceParams.channelParams.acsShouldExcludeDfs = true;
                    } else {
                        if (ifaceParams.channelParams.band == 2) {
                            Log.d(TAG, "ACS is not supported on this device, using 2.4 GHz band.");
                            ifaceParams.channelParams.band = 0;
                        }
                        ifaceParams.channelParams.enableAcs = false;
                        ifaceParams.channelParams.channel = config.apChannel;
                    }
                    if (ifaceParams.channelParams.band == 1 && !mOppoSoftapP2pBandControl.canUseSoftap5GBand()) {
                        ifaceParams.channelParams.band = 0;
                    }
                    if (WifiInjector.getInstance().getOppoWifiSharingManager().isWifiSharingTethering() && ifaceParams.channelParams.band == 0 && WifiInjector.getInstance().getClientModeImpl().syncRequestConnectionInfo().is24GHz() && config.apChannel >= 1 && config.apChannel <= 13) {
                        ifaceParams.channelParams.enableAcs = false;
                        ifaceParams.channelParams.channel = config.apChannel;
                        Log.d(TAG, "disable acs supported when g-band sharing ");
                    }
                    int fixChannelFlag = Settings.System.getInt(context.getContentResolver(), WIFI_HOTSPOT_FIX_CHANNEL_FLAG_VALUE, 0);
                    if (config.apChannel > 0 && fixChannelFlag == 1) {
                        ifaceParams.channelParams.enableAcs = false;
                        ifaceParams.channelParams.channel = config.apChannel;
                        ifaceParams.channelParams.channel &= Constants.SHORT_MASK;
                        ifaceParams.channelParams.channel += config.apSecondaryChannel << 16;
                        Log.d(TAG, "disable acs supported when fix channel for WFA test ");
                    }
                    IHostapd.NetworkParams nwParams = new IHostapd.NetworkParams();
                    nwParams.ssid.addAll(NativeUtil.stringToByteArrayList(config.SSID));
                    nwParams.isHidden = config.hiddenSSID;
                    nwParams.encryptionType = getEncryptionType(hostapdHal, config);
                    nwParams.pskPassphrase = config.preSharedKey != null ? config.preSharedKey : "";
                    if (!WifiInjector.getInstance().getOppoWifiSharingManager().isWifiSharingTethering()) {
                        nwParams.maxNumSta = Settings.System.getInt(context.getContentResolver(), "oppo_wifi_ap_max_devices_connect", 10);
                    } else {
                        nwParams.maxNumSta = 10;
                    }
                    nwParams.macAddrAcl = "0";
                    nwParams.acceptMacFileContent = "";
                    if (!checkHostapdAndLogFailure(hostapdHal, "addAccessPoint")) {
                        return false;
                    }
                    try {
                        vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd iHostapd = vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.castFrom((IHwInterface) vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.getService());
                        if (iHostapd != null) {
                            boolean checkStatusAndLogFailure = checkStatusAndLogFailure(hostapdHal, iHostapd.addAccessPoint(ifaceParams, nwParams), "addAccessPoint");
                            return checkStatusAndLogFailure;
                        }
                        Log.e(TAG, "addAccessPoint: Failed to get IHostapd");
                        return false;
                    } catch (RemoteException e) {
                        e = e;
                        handleRemoteException(hostapdHal, e, "addAccessPoint");
                        return false;
                    } catch (NoSuchElementException e2) {
                    }
                } catch (IllegalArgumentException e3) {
                    Log.e(TAG, "Unrecognized apBand " + config.apBand);
                    return false;
                }
            } catch (Throwable th2) {
                e = th2;
                throw e;
            }
        }
    }

    public static boolean registerCallback(IHostapdCallback callback) {
        HostapdHal hostapdHal = getHostapdHal();
        synchronized (getLock(hostapdHal)) {
            if (!checkHostapdAndLogFailure(hostapdHal, "registerCallback")) {
                return false;
            }
            try {
                vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd iHostapd = vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.castFrom((IHwInterface) vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.getService());
                if (iHostapd != null) {
                    boolean checkStatusAndLogFailure = checkStatusAndLogFailure(hostapdHal, iHostapd.registerCallback(callback), "registerCallback");
                    return checkStatusAndLogFailure;
                }
                Log.e(TAG, "registerCallback: Failed to get IHostapd");
                return false;
            } catch (RemoteException e) {
                handleRemoteException(hostapdHal, e, "registerCallback");
                return false;
            } catch (NoSuchElementException e2) {
                Log.e(TAG, "IHostapd.getService exception: " + e2);
                return false;
            }
        }
    }

    public static boolean blockClient(String deviceAddress) {
        if (TextUtils.isEmpty(deviceAddress)) {
            return false;
        }
        HostapdHal hostapdHal = getHostapdHal();
        synchronized (getLock(hostapdHal)) {
            if (!checkHostapdAndLogFailure(hostapdHal, "blockClient")) {
                return false;
            }
            try {
                vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd iHostapd = vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.castFrom((IHwInterface) vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.getService());
                if (iHostapd != null) {
                    boolean checkStatusAndLogFailure = checkStatusAndLogFailure(hostapdHal, iHostapd.blockClient(deviceAddress), "blockClient");
                    return checkStatusAndLogFailure;
                }
                Log.e(TAG, "blockClient: Failed to get IHostapd");
                return false;
            } catch (RemoteException e) {
                handleRemoteException(hostapdHal, e, "blockClient");
                return false;
            } catch (NoSuchElementException e2) {
                Log.e(TAG, "IHostapd.getService exception: " + e2);
                return false;
            }
        }
    }

    public static boolean unblockClient(String deviceAddress) {
        if (TextUtils.isEmpty(deviceAddress)) {
            return false;
        }
        HostapdHal hostapdHal = getHostapdHal();
        synchronized (getLock(hostapdHal)) {
            if (!checkHostapdAndLogFailure(hostapdHal, "unblockClient")) {
                return false;
            }
            try {
                vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd iHostapd = vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.castFrom((IHwInterface) vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.getService());
                if (iHostapd != null) {
                    boolean checkStatusAndLogFailure = checkStatusAndLogFailure(hostapdHal, iHostapd.unblockClient(deviceAddress), "unblockClient");
                    return checkStatusAndLogFailure;
                }
                Log.e(TAG, "unblockClient: Failed to get IHostapd");
                return false;
            } catch (RemoteException e) {
                handleRemoteException(hostapdHal, e, "unblockClient");
                return false;
            } catch (NoSuchElementException e2) {
                Log.e(TAG, "IHostapd.getService exception: " + e2);
                return false;
            }
        }
    }

    public static boolean updateAllowedList(String acceptMacFileContent) {
        if (TextUtils.isEmpty(acceptMacFileContent)) {
            return false;
        }
        HostapdHal hostapdHal = getHostapdHal();
        synchronized (getLock(hostapdHal)) {
            if (!checkHostapdAndLogFailure(hostapdHal, "updateAllowedList")) {
                return false;
            }
            try {
                vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd iHostapd = vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.castFrom((IHwInterface) vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.getService());
                if (iHostapd != null) {
                    boolean checkStatusAndLogFailure = checkStatusAndLogFailure(hostapdHal, iHostapd.updateAllowedList(acceptMacFileContent), "updateAllowedList");
                    return checkStatusAndLogFailure;
                }
                Log.e(TAG, "updateAllowedList: Failed to get IHostapd");
                return false;
            } catch (RemoteException e) {
                handleRemoteException(hostapdHal, e, "updateAllowedList");
                return false;
            } catch (NoSuchElementException e2) {
                Log.e(TAG, "IHostapd.getService exception: " + e2);
                return false;
            }
        }
    }

    public static boolean setAllDevicesAllowed(boolean enable) {
        HostapdHal hostapdHal = getHostapdHal();
        synchronized (getLock(hostapdHal)) {
            if (!checkHostapdAndLogFailure(hostapdHal, "setAllDevicesAllowed")) {
                return false;
            }
            try {
                vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd iHostapd = vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.castFrom((IHwInterface) vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.getService());
                if (iHostapd != null) {
                    boolean checkStatusAndLogFailure = checkStatusAndLogFailure(hostapdHal, iHostapd.setAllDevicesAllowed(enable), "setAllDevicesAllowed");
                    return checkStatusAndLogFailure;
                }
                Log.e(TAG, "setAllDevicesAllowed: Failed to get IHostapd");
                return false;
            } catch (RemoteException e) {
                handleRemoteException(hostapdHal, e, "setAllDevicesAllowed");
                return false;
            } catch (NoSuchElementException e2) {
                Log.e(TAG, "IHostapd.getService exception: " + e2);
                return false;
            }
        }
    }

    public static String getIfaceName() {
        return sIfaceName;
    }

    private static Context getContext() {
        WifiInjector wifiInjector = WifiInjector.getInstance();
        try {
            Field contextField = wifiInjector.getClass().getDeclaredField("mContext");
            contextField.setAccessible(true);
            return (Context) contextField.get(wifiInjector);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static HostapdHal getHostapdHal() {
        WifiInjector wifiInjector = WifiInjector.getInstance();
        try {
            Field hostapdHalField = wifiInjector.getClass().getDeclaredField("mHostapdHal");
            hostapdHalField.setAccessible(true);
            return (HostapdHal) hostapdHalField.get(wifiInjector);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Object getLock(HostapdHal hostapdHal) {
        try {
            Field lockField = hostapdHal.getClass().getDeclaredField("mLock");
            lockField.setAccessible(true);
            return lockField.get(hostapdHal);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return new Object();
        }
    }

    private static int getEncryptionType(HostapdHal hostapdHal, WifiConfiguration localConfig) {
        try {
            Method method = hostapdHal.getClass().getDeclaredMethod("getEncryptionType", WifiConfiguration.class);
            method.setAccessible(true);
            return ((Integer) method.invoke(hostapdHal, localConfig)).intValue();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static int getBand(HostapdHal hostapdHal, WifiConfiguration localConfig) {
        try {
            Method method = hostapdHal.getClass().getDeclaredMethod("getBand", WifiConfiguration.class);
            method.setAccessible(true);
            return ((Integer) method.invoke(hostapdHal, localConfig)).intValue();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static boolean checkHostapdAndLogFailure(HostapdHal hostapdHal, String methodStr) {
        try {
            Method method = hostapdHal.getClass().getDeclaredMethod("checkHostapdAndLogFailure", String.class);
            method.setAccessible(true);
            return ((Boolean) method.invoke(hostapdHal, methodStr)).booleanValue();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean checkStatusAndLogFailure(HostapdHal hostapdHal, HostapdStatus status, String methodStr) {
        try {
            Method method = hostapdHal.getClass().getDeclaredMethod("checkStatusAndLogFailure", HostapdStatus.class, String.class);
            method.setAccessible(true);
            return ((Boolean) method.invoke(hostapdHal, status, methodStr)).booleanValue();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void handleRemoteException(HostapdHal hostapdHal, RemoteException re, String methodStr) {
        try {
            Method method = hostapdHal.getClass().getDeclaredMethod("handleRemoteException", RemoteException.class, String.class);
            method.setAccessible(true);
            method.invoke(hostapdHal, re, methodStr);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }
}
