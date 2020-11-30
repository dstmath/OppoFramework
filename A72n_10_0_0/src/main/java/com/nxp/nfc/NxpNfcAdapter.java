package com.nxp.nfc;

import android.nfc.INfcAdapter;
import android.nfc.NfcAdapter;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import com.nxp.nfc.INxpNfcAdapter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NxpNfcAdapter {
    private static int ALL_SE_ID_TYPE = 7;
    private static final String TAG = "NXPNFC";
    static boolean sIsInitialized = false;
    static HashMap<NfcAdapter, NxpNfcAdapter> sNfcAdapters = new HashMap<>();
    private static INxpNfcAdapter sNxpService;
    private static INfcAdapter sService;

    private NxpNfcAdapter() {
    }

    public static synchronized NxpNfcAdapter getNxpNfcAdapter(NfcAdapter adapter) {
        NxpNfcAdapter nxpAdapter;
        synchronized (NxpNfcAdapter.class) {
            if (!sIsInitialized) {
                if (adapter != null) {
                    sService = getServiceInterface();
                    if (sService != null) {
                        sNxpService = getNxpNfcAdapterInterface();
                        if (sNxpService != null) {
                            sIsInitialized = true;
                        } else {
                            Log.e(TAG, "could not retrieve NXP NFC service");
                            throw new UnsupportedOperationException();
                        }
                    } else {
                        Log.e(TAG, "could not retrieve NFC service");
                        throw new UnsupportedOperationException();
                    }
                } else {
                    Log.v(TAG, "could not find NFC support");
                    throw new UnsupportedOperationException();
                }
            }
            nxpAdapter = sNfcAdapters.get(adapter);
            if (nxpAdapter == null) {
                nxpAdapter = new NxpNfcAdapter();
                sNfcAdapters.put(adapter, nxpAdapter);
            }
        }
        return nxpAdapter;
    }

    private static INfcAdapter getServiceInterface() {
        IBinder b = ServiceManager.getService("nfc");
        if (b == null) {
            return null;
        }
        return INfcAdapter.Stub.asInterface(b);
    }

    private static void attemptDeadServiceRecovery(Exception e) {
        Log.e(TAG, "Service dead - attempting to recover", e);
        INfcAdapter service = getServiceInterface();
        if (service == null) {
            Log.e(TAG, "could not retrieve NFC service during service recovery");
            return;
        }
        sService = service;
        sNxpService = getNxpNfcAdapterInterface();
    }

    public void MifareDesfireRouteSet(String routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws IOException {
        int seID;
        try {
            if (routeLoc.equals(NfcConstants.UICC_ID)) {
                seID = 2;
            } else if (routeLoc.equals(NfcConstants.UICC2_ID)) {
                seID = 4;
            } else if (routeLoc.equals(NfcConstants.SMART_MX_ID)) {
                seID = 1;
            } else if (routeLoc.equals(NfcConstants.HOST_ID)) {
                seID = 0;
            } else {
                Log.e(TAG, "confMifareDesfireProtoRoute: wrong default route ID");
                throw new IOException("confMifareProtoRoute failed: Wrong default route ID");
            }
            Log.i(TAG, "calling Services");
            sNxpService.MifareDesfireRouteSet(seID, fullPower, lowPower, noPower);
        } catch (RemoteException e) {
            Log.e(TAG, "confMifareDesfireProtoRoute failed", e);
            attemptDeadServiceRecovery(e);
            throw new IOException("confMifareDesfireProtoRoute failed");
        }
    }

    public void MifareCLTRouteSet(String routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws IOException {
        int seID;
        try {
            if (routeLoc.equals(NfcConstants.UICC_ID)) {
                seID = 2;
            } else if (routeLoc.equals(NfcConstants.UICC2_ID)) {
                seID = 4;
            } else if (routeLoc.equals(NfcConstants.SMART_MX_ID)) {
                seID = 1;
            } else if (routeLoc.equals(NfcConstants.HOST_ID)) {
                seID = 0;
            } else {
                Log.e(TAG, "confMifareCLT: wrong default route ID");
                throw new IOException("confMifareCLT failed: Wrong default route ID");
            }
            sNxpService.MifareCLTRouteSet(seID, fullPower, lowPower, noPower);
        } catch (RemoteException e) {
            Log.e(TAG, "confMifareCLT failed", e);
            attemptDeadServiceRecovery(e);
            throw new IOException("confMifareCLT failed");
        }
    }

    public Map<String, Integer> getServicesAidCacheSize(int UserID, String category) throws IOException {
        try {
            List<NfcAidServiceInfo> serviceInfoList = sNxpService.getServicesAidInfo(UserID, category);
            Map<String, Integer> nonPaymentServiceAidCacheSize = new HashMap<>();
            if (serviceInfoList != null) {
                for (NfcAidServiceInfo serviceInfo : serviceInfoList) {
                    nonPaymentServiceAidCacheSize.put(serviceInfo.getComponentName(), serviceInfo.getAidSize());
                }
            }
            return nonPaymentServiceAidCacheSize;
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    public List<NfcAidServiceInfo> getServicesAidInfo(int UserID, String category) throws IOException {
        try {
            return sNxpService.getServicesAidInfo(UserID, category);
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    public static INxpNfcAdapter getNxpNfcAdapterInterface() {
        INfcAdapter iNfcAdapter = sService;
        if (iNfcAdapter != null) {
            try {
                IBinder b = iNfcAdapter.getNfcAdapterVendorInterface("nxp");
                if (b == null) {
                    return null;
                }
                return INxpNfcAdapter.Stub.asInterface(b);
            } catch (RemoteException e) {
                return null;
            }
        } else {
            throw new UnsupportedOperationException("You need a reference from NfcAdapter to use the  NXP NFC APIs");
        }
    }

    public void changeDiscoveryTech(IBinder binder, int pollTech, int listenTech) throws IOException {
        try {
            sNxpService.changeDiscoveryTech(binder, pollTech, listenTech);
        } catch (RemoteException e) {
            Log.e(TAG, "changeDiscoveryTech failed", e);
        }
    }

    public int nfcSelfTest(int type) throws IOException {
        INxpNfcAdapter iNxpNfcAdapter = sNxpService;
        if (iNxpNfcAdapter != null) {
            try {
                return iNxpNfcAdapter.nfcSelfTest(type);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in nfcSelfTest(): ", e);
                e.printStackTrace();
                attemptDeadServiceRecovery(e);
                throw new IOException("RemoteException in nfcSelfTest()");
            }
        } else {
            throw new UnsupportedOperationException("You need a context on NxpNfcAdapter to use the  NXP NFC extras APIs");
        }
    }

    public INxpNfcAdapterExtras getNxpNfcAdapterExtrasInterface() {
        INxpNfcAdapter iNxpNfcAdapter = sNxpService;
        if (iNxpNfcAdapter != null) {
            try {
                return iNxpNfcAdapter.getNxpNfcAdapterExtrasInterface();
            } catch (RemoteException e) {
                Log.e(TAG, "getNxpNfcAdapterExtrasInterface failed", e);
                attemptDeadServiceRecovery(e);
                return null;
            } catch (Exception e2) {
                e2.printStackTrace();
                return null;
            }
        } else {
            throw new UnsupportedOperationException("You need a context on NxpNfcAdapter to use the  NXP NFC extras APIs");
        }
    }

    public int mPOSSetReaderMode(String pkg, boolean on) throws IOException {
        try {
            return sNxpService.mPOSSetReaderMode(pkg, on);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in mPOSSetReaderMode (int state): ", e);
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            throw new IOException("RemoteException in mPOSSetReaderMode (int state)");
        }
    }

    public boolean mPOSGetReaderMode(String pkg) throws IOException {
        try {
            return sNxpService.mPOSGetReaderMode(pkg);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in mPOSGetReaderMode (): ", e);
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            throw new IOException("RemoteException in mPOSSetReaderMode ()");
        }
    }

    public void stopPoll(String pkg, int mode) throws IOException {
        try {
            sNxpService.stopPoll(pkg, mode);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in stopPoll(int mode): ", e);
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            throw new IOException("RemoteException in stopPoll(int mode)");
        }
    }

    public void startPoll(String pkg) throws IOException {
        try {
            sNxpService.startPoll(pkg);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in startPoll(): ", e);
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            throw new IOException("RemoteException in startPoll()");
        }
    }

    public String[] getActiveSecureElementList(String pkg) throws IOException {
        try {
            Log.d(TAG, "getActiveSecureElementList-Enter");
            int[] activeSEList = sNxpService.getActiveSecureElementList(pkg);
            if (activeSEList == null || activeSEList.length == 0) {
                return new String[0];
            }
            String[] arr = new String[activeSEList.length];
            for (int i = 0; i < activeSEList.length; i++) {
                Log.e(TAG, "getActiveSecureElementList activeSE[i]" + activeSEList[i]);
                if (activeSEList[i] == 1) {
                    arr[i] = NfcConstants.SMART_MX_ID;
                } else if (activeSEList[i] == 2) {
                    arr[i] = NfcConstants.UICC_ID;
                } else if (activeSEList[i] == 4) {
                    arr[i] = NfcConstants.UICC2_ID;
                } else {
                    throw new IOException("No Secure Element Activeted");
                }
            }
            return arr;
        } catch (RemoteException e) {
            Log.e(TAG, "getActiveSecureElementList: failed", e);
            attemptDeadServiceRecovery(e);
            throw new IOException("Failure in deselecting the selected Secure Element");
        }
    }

    public void DefaultRouteSet(String routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws IOException {
        int seID;
        try {
            if (routeLoc.equals(NfcConstants.UICC_ID)) {
                seID = 2;
            } else if (routeLoc.equals(NfcConstants.UICC2_ID)) {
                seID = 4;
            } else if (routeLoc.equals(NfcConstants.SMART_MX_ID)) {
                seID = 1;
            } else if (routeLoc.equals(NfcConstants.HOST_ID)) {
                seID = 0;
            } else {
                Log.e(TAG, "DefaultRouteSet: wrong default route ID");
                throw new IOException("DefaultRouteSet failed: Wrong default route ID");
            }
            sNxpService.DefaultRouteSet(seID, fullPower, lowPower, noPower);
        } catch (RemoteException e) {
            Log.e(TAG, "confsetDefaultRoute failed", e);
            attemptDeadServiceRecovery(e);
            throw new IOException("confsetDefaultRoute failed");
        }
    }

    public int getMaxAidRoutingTableSize() throws IOException {
        try {
            return sNxpService.getMaxAidRoutingTableSize();
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 0;
        }
    }

    public int getCommittedAidRoutingTableSize() throws IOException {
        try {
            return sNxpService.getCommittedAidRoutingTableSize();
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 0;
        }
    }

    public int updateServiceState(Map<String, Boolean> serviceState) throws IOException {
        try {
            return sNxpService.updateServiceState(UserHandle.myUserId(), serviceState);
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 255;
        }
    }

    public byte[] getFwVersion() throws IOException {
        try {
            return sNxpService.getFWVersion();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getFwVersion(): ", e);
            attemptDeadServiceRecovery(e);
            throw new IOException("RemoteException in getFwVersion()");
        }
    }

    public byte[] readerPassThruMode(byte status, byte modulationTyp) throws IOException {
        try {
            return sNxpService.readerPassThruMode(status, modulationTyp);
        } catch (RemoteException e) {
            Log.e(TAG, "Remote exception in readerPassThruMode(): ", e);
            throw new IOException("Remote exception in readerPassThruMode()");
        }
    }

    public byte[] transceiveAppData(byte[] data) throws IOException {
        try {
            return sNxpService.transceiveAppData(data);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in transceiveAppData(): ", e);
            throw new IOException("RemoteException in transceiveAppData()");
        }
    }

    public int setConfig(String configs, String pkg) throws IOException {
        try {
            return sNxpService.setConfig(configs, pkg);
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 255;
        }
    }

    public int changeRfParams(byte[] data, boolean lastCMD) throws IOException {
        try {
            return sNxpService.changeRfParams(data, lastCMD);
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 255;
        }
    }

    public int changeRfParamsByConfig(byte[] data) throws IOException {
        try {
            return sNxpService.changeRfParamsByConfig(data);
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 255;
        }
    }

    public int selectUicc(int uiccSlot) throws IOException {
        try {
            return sNxpService.selectUicc(uiccSlot);
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 255;
        }
    }

    public int getSelectedUicc() throws IOException {
        try {
            return sNxpService.getSelectedUicc();
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 255;
        }
    }

    public int setFieldDetectMode(boolean mode) {
        try {
            return sNxpService.setFieldDetectMode(mode);
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 3;
        }
    }

    public boolean isFieldDetectEnabled() {
        try {
            return sNxpService.isFieldDetectEnabled();
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public int activateSeInterface() throws IOException {
        try {
            return sNxpService.activateSeInterface();
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 255;
        }
    }

    public int deactivateSeInterface() throws IOException {
        try {
            return sNxpService.deactivateSeInterface();
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 255;
        }
    }

    public int doWriteT4tData(byte[] fileId, byte[] data, int length) {
        try {
            return sNxpService.doWriteT4tData(fileId, data, length);
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return -1;
        }
    }

    public byte[] doReadT4tData(byte[] fileId) {
        try {
            return sNxpService.doReadT4tData(fileId);
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return null;
        }
    }
}
