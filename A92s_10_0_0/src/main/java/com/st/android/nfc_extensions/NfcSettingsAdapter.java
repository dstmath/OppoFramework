package com.st.android.nfc_extensions;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.st.android.nfc_extensions.INfcAdapterStExtensions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NfcSettingsAdapter {
    public static final String DEFAULT_AB_TECH_ROUTE = "default_ab_tech_route";
    public static final String DEFAULT_AID_ROUTE = "default_aid_route";
    public static final String DEFAULT_FELICA_ROUTE = "default_felica_route";
    public static final String DEFAULT_ISO_DEP_ROUTE = "default_iso_dep_route";
    public static final String DEFAULT_MIFARE_ROUTE = "default_mifare_route";
    public static final String DEFAULT_ROUTE = "Default";
    public static final String DEFAULT_SC_ROUTE = "default_sc_route";
    public static final String ESE_ROUTE = "eSE";
    public static final int FLAG_OFF = 0;
    public static final int FLAG_ON = 1;
    public static final String HCE_ROUTE = "HCE";
    public static final int MODE_HCE = 2;
    public static final int MODE_P2P = 4;
    public static final int MODE_READER = 1;
    public static final String SERVICE_SETTINGS_NAME = "nfc_settings";
    public static final String SE_ESE1 = "eSE";
    public static final String SE_SIM1 = "SIM1";
    public static final String SE_SIM2 = "SIM2";
    public static final String SE_STATE_ACTIVATED = "Active";
    public static final String SE_STATE_AVAILABLE = "Available";
    public static final String SE_STATE_NOT_AVAILABLE = "N/A";
    private static final String TAG = "NfcSettingsAdapter";
    public static final String UICC_ROUTE = "UICC";
    static HashMap<Context, NfcSettingsAdapter> sNfcSettingsAdapters = new HashMap<>();
    static INfcSettingsAdapter sService;
    final Context mContext;

    public NfcSettingsAdapter(Context context) {
        this.mContext = context;
        sService = getServiceInterface();
    }

    public static NfcSettingsAdapter getDefaultAdapter(Context context) {
        if (NfcAdapter.getDefaultAdapter(context) == null) {
            Log.d(TAG, "getDefaultAdapter = null");
            return null;
        }
        NfcSettingsAdapter adapter = sNfcSettingsAdapters.get(context);
        if (adapter == null) {
            adapter = new NfcSettingsAdapter(context);
            sNfcSettingsAdapters.put(context, adapter);
        }
        if (sService == null) {
            sService = getServiceInterface();
            Log.d(TAG, "sService = " + sService);
        }
        Log.d(TAG, "adapter = " + adapter);
        return adapter;
    }

    private static INfcSettingsAdapter getServiceInterface() {
        IBinder b = ServiceManager.getService(NfcAdapterStExtensions.SERVICE_NAME);
        if (b != null) {
            try {
                return INfcAdapterStExtensions.Stub.asInterface(b).getNfcSettingsAdapterInterface();
            } catch (RemoteException e) {
                throw new RuntimeException("Cannot retrieve NfcSettingsAdapter from service :nfc.st_ext");
            }
        } else {
            throw new RuntimeException("Cannot retrieve service :nfc.st_ext");
        }
    }

    public int getModeFlag(int mode) {
        try {
            if (sService == null) {
                Log.e(TAG, "getModeFlag() - sService = null");
                return -1;
            }
            int flag = sService.getModeFlag(mode);
            Log.d(TAG, "getModeFlag()" + settingModeToString(mode, flag));
            return flag;
        } catch (RemoteException e) {
            Log.e(TAG, "getModeFlag() - e = " + e.toString());
            return -1;
        }
    }

    public void setModeFlag(int mode, int flag) {
        try {
            if (sService == null) {
                Log.e(TAG, "setModeFlag() - sService = null");
                return;
            }
            Log.d(TAG, "setModeFlag()" + settingModeToString(mode, flag));
            sService.setModeFlag(mode, flag);
        } catch (RemoteException e) {
            Log.e(TAG, "setModeFlag() - e = " + e.toString());
        }
    }

    public boolean isRoutingTableOverflow() {
        try {
            if (sService == null) {
                Log.e(TAG, "isRoutingTableOverflow() - sService = null");
                return false;
            }
            Log.d(TAG, "sService.isRoutingTableOverflow()");
            return sService.isRoutingTableOverflow();
        } catch (RemoteException e) {
            Log.e(TAG, "isRoutingTableOverflow() - e = " + e.toString());
            return false;
        }
    }

    public boolean isShowOverflowMenu() {
        try {
            if (sService == null) {
                Log.e(TAG, "isShowOverflowMenu() - sService = null");
                return false;
            }
            Log.d(TAG, "sService.isShowOverflowMenu()");
            return sService.isShowOverflowMenu();
        } catch (RemoteException e) {
            Log.e(TAG, "isShowOverflowMenu() - e = " + e.toString());
            return false;
        }
    }

    public List<ServiceEntry> getServiceEntryList(int userHandle) {
        try {
            if (sService == null) {
                Log.e(TAG, "getServiceEntryList() - sService = null");
                return null;
            }
            Log.d(TAG, "sService.getServiceEntryList()");
            return sService.getServiceEntryList(userHandle);
        } catch (RemoteException e) {
            Log.e(TAG, "getServiceEntryList() - e = " + e.toString());
            return null;
        }
    }

    public boolean testServiceEntryList(List<ServiceEntry> proposal) {
        try {
            if (sService == null) {
                Log.e(TAG, "testServiceEntryList() - sService = null");
                return false;
            }
            Log.d(TAG, "sService.testServiceEntryList()");
            return sService.testServiceEntryList(proposal);
        } catch (RemoteException e) {
            Log.e(TAG, "testServiceEntryList() - e = " + e.toString());
            return false;
        }
    }

    public void commitServiceEntryList(List<ServiceEntry> proposal) {
        try {
            if (sService == null) {
                Log.e(TAG, "commitServiceEntryList() - sService = null");
                return;
            }
            Log.d(TAG, "sService.commitServiceEntryList()");
            sService.commitServiceEntryList(proposal);
        } catch (RemoteException e) {
            Log.e(TAG, "commitServiceEntryList() - e = " + e.toString());
        }
    }

    public boolean isUiccConnected() {
        boolean result = false;
        try {
            result = sService.isUiccConnected();
        } catch (RemoteException e) {
            Log.e(TAG, "isUiccConnected() - e = " + e.toString());
        }
        Log.d(TAG, "isUiccConnected() - " + result);
        return result;
    }

    public boolean iseSEConnected() {
        boolean result = false;
        try {
            result = sService.iseSEConnected();
        } catch (RemoteException e) {
            Log.e(TAG, "iseSEConnected() - e = " + e.toString());
        }
        Log.d(TAG, "iseSEConnected() - " + result);
        return result;
    }

    public boolean isSEConnected(int HostID) {
        boolean result = false;
        try {
            result = sService.isSEConnected(HostID);
        } catch (RemoteException e) {
            Log.e(TAG, "isSEConnected() - e = " + e.toString());
        }
        Log.d(TAG, "isSEConnected(" + HostID + ") - " + result);
        return result;
    }

    public boolean EnableSE(String se_id, boolean enable) {
        Log.i(TAG, "EnableSE(" + se_id + ", " + enable + ")");
        try {
            return sService.EnableSE(se_id, enable);
        } catch (RemoteException e) {
            Log.e(TAG, "EnableSE() - e = " + e.toString());
            return false;
        }
    }

    public List<String> getSecureElementsStatus() {
        Log.i(TAG, "getSecureElementsStatus()");
        try {
            return sService.getSecureElementsStatus();
        } catch (RemoteException e) {
            Log.e(TAG, "getSecureElementsStatus() e = " + e.toString());
            return null;
        }
    }

    public void registerNfcSettingsCallback(INfcSettingsCallback cb) {
        Log.i(TAG, "registerNfcSettingsCallback()");
        try {
            sService.registerNfcSettingsCallback(cb);
        } catch (RemoteException e) {
            Log.e(TAG, "registerNfcSettingsCallback() e = " + e.toString());
        }
    }

    public void unregisterNfcSettingsCallback(INfcSettingsCallback cb) {
        Log.i(TAG, "unregisterNfcSettingsCallback()");
        try {
            sService.unregisterNfcSettingsCallback(cb);
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterNfcSettingsCallback() e = " + e.toString());
        }
    }

    public List<ServiceEntry> getNonAidBasedServiceEntryList(int userHandle) {
        try {
            if (sService == null) {
                Log.e(TAG, "getNonAidBasedServiceEntryList() - sService = null");
                return null;
            }
            Log.d(TAG, "sService.getNonAidBasedServiceEntryList()");
            return sService.getNonAidBasedServiceEntryList(userHandle);
        } catch (RemoteException e) {
            Log.e(TAG, "getNonAidBasedServiceEntryList() - e = " + e.toString());
            return null;
        }
    }

    public void commitNonAidBasedServiceEntryList(List<ServiceEntry> proposal) {
        try {
            if (sService == null) {
                Log.e(TAG, "commitNonAidBasedServiceEntryList() - sService = null");
                return;
            }
            Log.d(TAG, "sService.commitNonAidBasedServiceEntryList()");
            sService.commitNonAidBasedServiceEntryList(proposal);
        } catch (RemoteException e) {
            Log.e(TAG, "commitNonAidBasedServiceEntryList() - e = " + e.toString());
        }
    }

    /* access modifiers changed from: package-private */
    public String settingModeToString(int mode, int flag) {
        String valueString;
        if (mode == 1) {
            valueString = "MODE_READER ";
        } else if (mode == 2) {
            valueString = "MODE_HCE ";
        } else if (mode != 4) {
            valueString = "Unknown mode ";
        } else {
            valueString = "MODE_P2P ";
        }
        if (flag == 0) {
            return valueString + "FLAG_OFF";
        } else if (flag == 1) {
            return valueString + "FLAG_ON";
        } else {
            return valueString + "unknown flag value";
        }
    }

    public void DefaultRouteSet(String routeLoc) throws IOException {
        Log.i(TAG, "DefaultRouteSet() - route: " + routeLoc);
        HashMap<String, String> entry = new HashMap<>();
        entry.put(DEFAULT_AID_ROUTE, routeLoc);
        setUserDefaultRoutes(entry);
    }

    public void setUserDefaultRoutes(Map<String, String> routeList) throws IOException {
        List<DefaultRouteEntry> defaultRouteList = new ArrayList<>();
        for (Map.Entry<String, String> entry : routeList.entrySet()) {
            String routeKey = entry.getKey();
            String routeValue = entry.getValue();
            Log.d(TAG, "setUserDefaultRoutes() - " + routeKey + ": " + routeValue);
            if (!DEFAULT_AID_ROUTE.contentEquals(routeKey) && !DEFAULT_MIFARE_ROUTE.contentEquals(routeKey) && !DEFAULT_ISO_DEP_ROUTE.contentEquals(routeKey) && !DEFAULT_FELICA_ROUTE.contentEquals(routeKey) && !DEFAULT_AB_TECH_ROUTE.contentEquals(routeKey) && !DEFAULT_SC_ROUTE.contentEquals(routeKey)) {
                Log.e(TAG, "setUserDefaultRoutes() - " + routeKey + " does not exists");
                throw new IOException(routeKey + " does not exists");
            } else if (UICC_ROUTE.contentEquals(routeValue) || "eSE".contentEquals(routeValue) || HCE_ROUTE.contentEquals(routeValue) || DEFAULT_ROUTE.contentEquals(routeValue)) {
                defaultRouteList.add(new DefaultRouteEntry(routeKey, routeValue));
            } else {
                Log.e(TAG, "setUserDefaultRoutes() - " + routeValue + " does not exists");
                throw new IOException(routeValue + " does not exists");
            }
        }
        try {
            sService.setDefaultUserRoutes(defaultRouteList);
        } catch (RemoteException e) {
            Log.e(TAG, "setDefaultUserRoutes failed", e);
            throw new IOException("setDefaultUserRoutes failed");
        }
    }

    public Map<String, String> getUserDefaultRoutes() throws IOException {
        Map<String, String> userRoutes = new HashMap<>();
        new ArrayList();
        try {
            for (DefaultRouteEntry entry : sService.getDefaultUserRoutes()) {
                Log.d(TAG, "getUserDefaultRoutes() - " + entry.getRouteName() + ": " + entry.getRouteLoc());
                userRoutes.put(entry.getRouteName(), entry.getRouteLoc());
            }
            return userRoutes;
        } catch (RemoteException e) {
            Log.e(TAG, "getUserDefaultRoutes failed", e);
            throw new IOException("getUserDefaultRoutes failed");
        }
    }

    public Map<String, String> getEffectiveDefaultRoutes() throws IOException {
        Map<String, String> userRoutes = new HashMap<>();
        new ArrayList();
        try {
            for (DefaultRouteEntry entry : sService.getEffectiveRoutes()) {
                Log.d(TAG, "getEffectiveDefaultRoutes() - " + entry.getRouteName() + ": " + entry.getRouteLoc());
                userRoutes.put(entry.getRouteName(), entry.getRouteLoc());
            }
            return userRoutes;
        } catch (RemoteException e) {
            Log.e(TAG, "getEffectiveDefaultRoutes failed", e);
            throw new IOException("getEffectiveDefaultRoutes failed");
        }
    }

    public int getAvailableSpaceForAid() {
        try {
            if (sService == null) {
                Log.e(TAG, "getAvailableSpaceForAid() - sService = null");
                return -1;
            }
            Log.d(TAG, "sService.getAvailableSpaceForAid()");
            return sService.getAvailableSpaceForAid();
        } catch (RemoteException e) {
            Log.e(TAG, "getAvailableSpaceForAid() - e = " + e.toString());
            return -1;
        }
    }
}
