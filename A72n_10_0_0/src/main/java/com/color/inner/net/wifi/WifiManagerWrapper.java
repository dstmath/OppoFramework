package com.color.inner.net.wifi;

import android.net.wifi.OppoMirrorWifiManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import oppo.net.wifi.HotspotClient;

public class WifiManagerWrapper {
    public static final String EXTRA_WIFI_AP_STATE = "wifi_state";
    private static final String TAG = "WifiManagerWrapper";
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;

    public interface ActionListenerWrapper {
        void onFailure(int i);

        void onSuccess();
    }

    private WifiManagerWrapper() {
    }

    public static int getWifiApState(WifiManager wifiManager) {
        try {
            return wifiManager.getWifiApState();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static WifiConfiguration getWifiApConfiguration(WifiManager wifiManager) {
        try {
            return wifiManager.getWifiApConfiguration();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static boolean setWifiApConfiguration(WifiManager wifiManager, WifiConfiguration wifiConfig) {
        try {
            return wifiManager.setWifiApConfiguration(wifiConfig);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static void connect(WifiManager wifiManager, WifiConfiguration config, final ActionListenerWrapper listener) {
        WifiManager.ActionListener actionListener = null;
        if (listener != null) {
            try {
                actionListener = new WifiManager.ActionListener() {
                    /* class com.color.inner.net.wifi.WifiManagerWrapper.AnonymousClass1 */

                    public void onSuccess() {
                        ActionListenerWrapper.this.onSuccess();
                    }

                    public void onFailure(int reason) {
                        ActionListenerWrapper.this.onFailure(reason);
                    }
                };
            } catch (Throwable e) {
                Log.e(TAG, e.toString());
                return;
            }
        }
        wifiManager.connect(config, actionListener);
    }

    public static void connect(WifiManager wifiManager, int networkId, final ActionListenerWrapper listener) {
        WifiManager.ActionListener actionListener = null;
        if (listener != null) {
            try {
                actionListener = new WifiManager.ActionListener() {
                    /* class com.color.inner.net.wifi.WifiManagerWrapper.AnonymousClass2 */

                    public void onSuccess() {
                        ActionListenerWrapper.this.onSuccess();
                    }

                    public void onFailure(int reason) {
                        ActionListenerWrapper.this.onFailure(reason);
                    }
                };
            } catch (Throwable e) {
                Log.e(TAG, e.toString());
                return;
            }
        }
        wifiManager.connect(networkId, actionListener);
    }

    public static void forget(WifiManager wifiManager, int netId, final ActionListenerWrapper listener) {
        WifiManager.ActionListener actionListener = null;
        if (listener != null) {
            try {
                actionListener = new WifiManager.ActionListener() {
                    /* class com.color.inner.net.wifi.WifiManagerWrapper.AnonymousClass3 */

                    public void onSuccess() {
                        ActionListenerWrapper.this.onSuccess();
                    }

                    public void onFailure(int reason) {
                        ActionListenerWrapper.this.onFailure(reason);
                    }
                };
            } catch (Throwable e) {
                Log.e(TAG, e.toString());
                return;
            }
        }
        wifiManager.forget(netId, actionListener);
    }

    public static boolean isDualBandSupported(WifiManager wifiManager) {
        try {
            return wifiManager.isDualBandSupported();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static boolean isSlaSupported(WifiManager wifiManager) {
        try {
            return ((Boolean) OppoMirrorWifiManager.isSlaSupported.call(wifiManager, new Object[0])).booleanValue();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static WifiConfiguration getWifiSharingConfiguration(WifiManager wifiManager) {
        try {
            return (WifiConfiguration) OppoMirrorWifiManager.getWifiSharingConfiguration.call(wifiManager, new Object[0]);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static void setWifiSharingConfiguration(WifiManager wifiManager, WifiConfiguration wifiConfiguration) {
        try {
            OppoMirrorWifiManager.setWifiSharingConfiguration.call(wifiManager, new Object[]{wifiConfiguration});
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static String[] getAllSlaAppsAndStates(WifiManager wifiManager) {
        try {
            return (String[]) OppoMirrorWifiManager.getAllSlaAppsAndStates.call(wifiManager, new Object[0]);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static List<HotspotClient> getHotspotClients(WifiManager wifiManager) {
        try {
            return (List) OppoMirrorWifiManager.getHotspotClients.call(wifiManager, new Object[0]);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static List<HotspotClient> getBlockedHotspotClients(WifiManager wifiManager) {
        try {
            return (List) OppoMirrorWifiManager.getBlockedHotspotClients.call(wifiManager, new Object[0]);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static boolean blockClient(WifiManager wifiManager, HotspotClient client) {
        try {
            return ((Boolean) OppoMirrorWifiManager.blockClient.call(wifiManager, new Object[]{client})).booleanValue();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static boolean unblockClient(WifiManager wifiManager, HotspotClient client) {
        try {
            return ((Boolean) OppoMirrorWifiManager.unblockClient.call(wifiManager, new Object[]{client})).booleanValue();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static String[] getAllSlaAcceleratedApps(WifiManager wifiManager) {
        try {
            return (String[]) OppoMirrorWifiManager.getAllSlaAcceleratedApps.call(wifiManager, new Object[0]);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static int enableDualSta(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("enableDualSta", new Class[0]);
            if (method != null) {
                return ((Integer) method.invoke(wifiManager, new Object[0])).intValue();
            }
            Log.d(TAG, "not found method enableDualSta");
            return -1;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static int enableDualStaByForce(WifiManager wifiManager, boolean force) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("enableDualStaByForce", Boolean.TYPE);
            if (method != null) {
                return ((Integer) method.invoke(wifiManager, Boolean.valueOf(force))).intValue();
            }
            Log.d(TAG, "not found method enableDualStaByForce");
            return -1;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static void disableDualSta(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("disableDualSta", new Class[0]);
            if (method != null) {
                method.invoke(wifiManager, new Object[0]);
            } else {
                Log.d(TAG, "not found method disableDualSta");
            }
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static WifiInfo getOppoSta2ConnectionInfo(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("getOppoSta2ConnectionInfo", new Class[0]);
            if (method != null) {
                return (WifiInfo) method.invoke(wifiManager, new Object[0]);
            }
            Log.d(TAG, "not found method getOppoSta2ConnectionInfo");
            return null;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static String[] getAllDualStaApps(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("getAllDualStaApps", new Class[0]);
            if (method != null) {
                return (String[]) method.invoke(wifiManager, new Object[0]);
            }
            Log.d(TAG, "not found method getAllDualStaApps");
            return null;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static boolean isDualStaSupported(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("isDualStaSupported", new Class[0]);
            if (method != null) {
                return ((Boolean) method.invoke(wifiManager, new Object[0])).booleanValue();
            }
            Log.d(TAG, "not found method isDualStaSupported");
            return false;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static boolean setSlaAppState(WifiManager wifiManager, String pkgName, boolean enabled) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("setSlaAppState", String.class, Boolean.TYPE);
            if (method != null) {
                return ((Boolean) method.invoke(wifiManager, pkgName, Boolean.valueOf(enabled))).booleanValue();
            }
            Log.d(TAG, "not found method setSlaAppState");
            return false;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static boolean getSlaAppState(WifiManager wifiManager, String pkgName) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("getSlaAppState", String.class);
            if (method != null) {
                return ((Boolean) method.invoke(wifiManager, pkgName)).booleanValue();
            }
            Log.d(TAG, "not found method getSlaAppState");
            return false;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static boolean isMptcpSupported(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("isMptcpSupported", new Class[0]);
            if (method != null) {
                return ((Boolean) method.invoke(wifiManager, new Object[0])).booleanValue();
            }
            Log.d(TAG, "not found method isMptcpSupported");
            return false;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static boolean isWIFI6Supported(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("isWIFI6Supported", new Class[0]);
            if (method != null) {
                return ((Boolean) method.invoke(wifiManager, new Object[0])).booleanValue();
            }
            Log.d(TAG, "not found method isWIFI6Supported");
            return false;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static boolean isCertificateExpired(WifiManager wifiManager, String fqdn) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("checkPasspointXMLCAExpired", String.class);
            if (method != null) {
                return ((Boolean) method.invoke(wifiManager, fqdn)).booleanValue();
            }
            Log.d(TAG, "Not found method when check CA expired.");
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "Exception when check CA expired state " + e);
            return true;
        }
    }

    public static boolean isCertificatePreInstalled(WifiManager wifiManager, String fqdn) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("checkInternalPasspointPresetProvider", String.class);
            if (method != null) {
                return ((Boolean) method.invoke(wifiManager, fqdn)).booleanValue();
            }
            Log.d(TAG, "Not found method when check CA preinstalled.");
            return false;
        } catch (Throwable e) {
            Log.e(TAG, "Exception when check CA perinstalled " + e);
            return false;
        }
    }

    public static boolean isCertificateExist(WifiManager wifiManager, String fqdn) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("checkPasspointCAExist", String.class);
            if (method != null) {
                return ((Boolean) method.invoke(wifiManager, fqdn)).booleanValue();
            }
            Log.d(TAG, "Not found method when check CA exists state.");
            return false;
        } catch (Throwable e) {
            Log.e(TAG, "Exception when check CA exists state " + e);
            return false;
        }
    }

    public static boolean isPasspointFeatureSupport(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("checkFWKSupportPasspoint", new Class[0]);
            if (method != null) {
                return ((Boolean) method.invoke(wifiManager, new Object[0])).booleanValue();
            }
            Log.d(TAG, "Not found method when check passpoint feature");
            return false;
        } catch (Throwable e) {
            Log.e(TAG, "Exception when check passpoint feature " + e);
            return false;
        }
    }

    public static boolean setPasspointCertifiedState(WifiManager wifiManager, String signature) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("setPasspointCertifiedState", String.class);
            if (method != null) {
                return ((Boolean) method.invoke(wifiManager, signature)).booleanValue();
            }
            Log.d(TAG, "Not found method when set certified state.");
            return false;
        } catch (Throwable e) {
            Log.e(TAG, "Exception when check CA extists " + e);
            return false;
        }
    }

    public static List<ScanResult> passpointANQPScanResults(WifiManager wifiManager, List<ScanResult> scanResults) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("passpointANQPScanResults", List.class);
            if (method != null) {
                return (List) method.invoke(wifiManager, scanResults);
            }
            Log.d(TAG, "Not found method when get anqp scan results.");
            return Collections.emptyList();
        } catch (Throwable th) {
            return Collections.emptyList();
        }
    }
}
