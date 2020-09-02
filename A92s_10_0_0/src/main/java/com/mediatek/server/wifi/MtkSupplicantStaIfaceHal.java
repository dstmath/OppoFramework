package com.mediatek.server.wifi;

import android.hardware.wifi.supplicant.V1_0.ISupplicant;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.os.HidlSupport;
import android.os.IHwInterface;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.wifi.SupplicantStaIfaceHal;
import com.android.server.wifi.WifiInjector;
import com.mediatek.powerhalservice.PowerHalWifiMonitor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicant;
import vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface;
import vendor.mediatek.hardware.wifi.supplicant.V2_1.ISupplicantStaIface;
import vendor.mediatek.hardware.wifi.supplicant.V2_1.ISupplicantStaIfaceCallback;

public class MtkSupplicantStaIfaceHal {
    private static final String TAG = "MtkSupplicantStaIfaceHal";
    private static ISupplicantStaIface mMtkSupplicantStaIface;
    private static MtkSupplicantStaIfaceHal sMtkSupplicantStaIfaceHal = null;

    public static MtkSupplicantStaIfaceHal getInstance() {
        if (sMtkSupplicantStaIfaceHal == null) {
            synchronized (MtkSupplicantStaIfaceHal.class) {
                if (sMtkSupplicantStaIfaceHal == null) {
                    sMtkSupplicantStaIfaceHal = new MtkSupplicantStaIfaceHal();
                }
            }
        }
        return sMtkSupplicantStaIfaceHal;
    }

    public static void setupMtkIface(String ifaceName) {
        ISupplicantIface mtkIfaceHwBinder = getMtkIfaceV2_0(ifaceName);
        if (mtkIfaceHwBinder == null) {
            Log.e(TAG, "setupMtkIface got null iface");
        } else {
            registeCallback(mtkIfaceHwBinder, new SupplicantStaIfaceHalCallbackV2_1());
        }
    }

    private static boolean registeCallback(ISupplicantIface iface, SupplicantStaIfaceHalCallbackV2_1 callback) {
        synchronized (getLockForSupplicantStaIfaceHal()) {
            if (checkSupplicantStaIfaceAndLogFailure(iface, "registerMtkCallback") == null) {
                return false;
            }
            try {
                ISupplicantStaIface supplicantStaIfaceV21 = getMtkStaIfaceMockableV2_1(iface);
                if (supplicantStaIfaceV21 != null) {
                    boolean checkStatusAndLogFailure = checkStatusAndLogFailure(supplicantStaIfaceV21.registerCallback_2_1(callback), "registerMtkCallback");
                    return checkStatusAndLogFailure;
                }
                Log.e(TAG, "registerMtkCallback mISupplicantStaIface is not IMtkSupplicantStaIface");
                return false;
            } catch (RemoteException e) {
                handleRemoteException(WifiInjector.getInstance().getSupplicantStaIfaceHal(), e, "registerMtkCallback");
                return false;
            }
        }
    }

    private static ISupplicantIface getMtkIfaceV2_0(String ifaceName) {
        synchronized (getLockForSupplicantStaIfaceHal()) {
            ArrayList<ISupplicant.IfaceInfo> supplicantIfaces = new ArrayList<>();
            try {
                getISupplicant().listInterfaces(new ISupplicant.listInterfacesCallback(supplicantIfaces) {
                    /* class com.mediatek.server.wifi.$$Lambda$MtkSupplicantStaIfaceHal$qThYSX7aov63YlTxEUiLtNggQAU */
                    private final /* synthetic */ ArrayList f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // android.hardware.wifi.supplicant.V1_0.ISupplicant.listInterfacesCallback
                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        MtkSupplicantStaIfaceHal.lambda$getMtkIfaceV2_0$0(this.f$0, supplicantStatus, arrayList);
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
                                /* class com.mediatek.server.wifi.$$Lambda$MtkSupplicantStaIfaceHal$snD9QeVkaSoCAJ1LnW6DGnmR3Hs */
                                private final /* synthetic */ HidlSupport.Mutable f$0;

                                {
                                    this.f$0 = r1;
                                }

                                @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicant.getInterfaceCallback
                                public final void onValues(SupplicantStatus supplicantStatus, ISupplicantIface iSupplicantIface) {
                                    MtkSupplicantStaIfaceHal.lambda$getMtkIfaceV2_0$1(this.f$0, supplicantStatus, iSupplicantIface);
                                }
                            });
                        } catch (RemoteException e) {
                            Log.e(TAG, "ISupplicant.getInterface exception: " + e);
                            supplicantServiceDiedHandler(ifaceName);
                            return null;
                        }
                    }
                }
                ISupplicantIface iSupplicantIface = (ISupplicantIface) supplicantIface.value;
                return iSupplicantIface;
            } catch (RemoteException e2) {
                Log.e(TAG, "ISupplicant.listInterfaces exception: " + e2);
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

    /* access modifiers changed from: private */
    public static Object getLockForSupplicantStaIfaceHal() {
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

    protected static vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicant getMtkSupplicantMockableV2_0() throws RemoteException {
        vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicant castFrom;
        synchronized (getLockForSupplicantStaIfaceHal()) {
            try {
                castFrom = vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicant.castFrom((IHwInterface) vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicant.getService());
            } catch (NoSuchElementException e) {
                Log.e(TAG, "Failed to get IMtkSupplicant2_0", e);
                return null;
            } catch (Throwable th) {
                throw th;
            }
        }
        return castFrom;
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

    protected static ISupplicantStaIface getMtkStaIfaceMockableV2_1(ISupplicantIface iface) {
        ISupplicantStaIface asInterface;
        synchronized (getLockForSupplicantStaIfaceHal()) {
            asInterface = ISupplicantStaIface.asInterface(iface.asBinder());
        }
        return asInterface;
    }

    private static ISupplicantStaIface checkSupplicantStaIfaceAndLogFailure(ISupplicantIface iface, String methodStr) {
        synchronized (getLockForSupplicantStaIfaceHal()) {
            ISupplicantStaIface mtkIface = getMtkStaIfaceMockableV2_1(iface);
            if (mtkIface == null) {
                Log.e(TAG, "Can't call " + methodStr + ", Mtkiface is null");
                return null;
            }
            Log.d(TAG, "Do Mtkiface." + methodStr);
            return mtkIface;
        }
    }

    private static boolean checkStatusAndLogFailure(SupplicantStatus status, String methodStr) {
        if (status.code != 0) {
            Log.e(TAG, "ISupplicantStaIface2_1." + methodStr + " failed: " + status);
            return false;
        }
        Log.d(TAG, "ISupplicantStaIface2_1." + methodStr + " succeeded");
        return true;
    }

    private static class SupplicantStaIfaceHalCallbackV2_1 extends ISupplicantStaIfaceCallback.Stub {
        private SupplicantStaIfaceHalCallbackV2_1() {
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIfaceCallback
        public void onEssDisassocImminentNotice(byte[] bssid, int pmfEnabled, int reAuthDelayInSec, String url) {
            synchronized (MtkSupplicantStaIfaceHal.getLockForSupplicantStaIfaceHal()) {
                Log.d(MtkSupplicantStaIfaceHal.TAG, "onEssDisassocImminentNotice --> " + bssid + " " + pmfEnabled + " " + reAuthDelayInSec + " " + url);
            }
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_1.ISupplicantStaIfaceCallback
        public void onDataStallNotice(int errCode) {
            Log.d(MtkSupplicantStaIfaceHal.TAG, "onDataStallNotice --> " + errCode);
            PowerHalWifiMonitor.getInstance().supplicantHalCallback(errCode);
            if (errCode != 6 && errCode != 7) {
                WifiInjector.getInstance().getClientModeImpl().notifyDataStallEvent(errCode + 512);
            }
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_1.ISupplicantStaIfaceCallback
        public void onAssocFreqChanged(int freq) {
            synchronized (MtkSupplicantStaIfaceHal.getLockForSupplicantStaIfaceHal()) {
                Log.d(MtkSupplicantStaIfaceHal.TAG, "onAssocFreqChanged --> " + freq);
                WifiInjector.getInstance().getWifiMonitor().broadcastAssocFreqChanged(WifiInjector.getInstance().getWifiNative().getClientInterfaceName(), freq);
            }
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
}
