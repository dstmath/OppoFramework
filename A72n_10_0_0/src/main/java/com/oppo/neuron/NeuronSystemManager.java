package com.oppo.neuron;

import android.content.ContentValues;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.util.Slog;
import com.oppo.neuron.INeuronSystemService;
import java.util.List;

public final class NeuronSystemManager {
    public static final int DEFAULT_PROP = 1;
    public static final boolean LOG_ON = SystemProperties.getBoolean("persist.vendor.sys.ns_logon", false);
    public static final int NS_APP_PRELOAD = 16;
    public static final int NS_EVENT_PUBLISH = 8;
    public static final int NS_ON = 1;
    public static final int NS_UPLOAD_DB = 4;
    public static final int NS_WRITE_DB = 2;
    private static final String TAG = "NeuronSystem";
    private static NeuronSystemManager sNeuronSystemManager = null;
    public static int sNsProp = SystemProperties.getInt("persist.vendor.sys.neuron_system", 1);
    private INeuronSystemService mService;

    private NeuronSystemManager() {
        this.mService = null;
        this.mService = INeuronSystemService.Stub.asInterface(ServiceManager.getService("neuronsystem"));
        if (this.mService == null) {
            Slog.d(TAG, "can not get service neuronsystem");
        }
    }

    public static NeuronSystemManager getInstance() {
        if (sNeuronSystemManager == null) {
            synchronized (NeuronSystemManager.class) {
                if (sNeuronSystemManager == null) {
                    sNeuronSystemManager = new NeuronSystemManager();
                }
            }
        }
        return sNeuronSystemManager;
    }

    public static boolean isEnable() {
        return (sNsProp & 1) != 0;
    }

    public void publishEvent(int type, ContentValues contentValues) {
        INeuronSystemService iNeuronSystemService = this.mService;
        if (iNeuronSystemService != null) {
            try {
                iNeuronSystemService.publishEvent(type, contentValues);
            } catch (Exception e) {
                Slog.d(TAG, "NeuronSystemManager publishEvent err: " + e);
            }
        }
    }

    public void enableRecommendedApps(boolean enable, List<String> pkgs) {
        INeuronSystemService iNeuronSystemService = this.mService;
        if (iNeuronSystemService == null) {
            Slog.d(TAG, "NeuronSystemManager enableRecommendedApps can not get service neuronsystem");
            return;
        }
        try {
            iNeuronSystemService.enableRecommendedApps(enable, pkgs);
        } catch (RemoteException e) {
            Slog.e(TAG, "Exception happend while enableRecommendedApps", e);
        }
    }

    public List<String> getRecommendedApps(int topK) {
        INeuronSystemService iNeuronSystemService = this.mService;
        if (iNeuronSystemService == null) {
            Slog.d(TAG, "NeuronSystemManager getRecommendedApps can not get service neuronsystem");
            return null;
        }
        try {
            return iNeuronSystemService.getRecommendedApps(topK);
        } catch (RemoteException e) {
            Slog.e(TAG, "Exception happend while getRecommendedApps", e);
            return null;
        }
    }

    public static void notifyCellToNeuronSystem(int subId, List<CellInfo> cells, boolean is5GNsa) {
        if (isEnable()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("date", Long.valueOf(System.currentTimeMillis()));
            for (CellInfo cell : cells) {
                if (cell.isRegistered() && (cell instanceof CellInfoLte)) {
                    CellIdentityLte identity = ((CellInfoLte) cell).getCellIdentity();
                    String key = String.format("subid-%d-mcc-%s-mnc-%s-ci-%d-pci-%d-tac-%d", Integer.valueOf(subId), identity.getMccString(), identity.getMncString(), Integer.valueOf(identity.getCi()), Integer.valueOf(identity.getPci()), Integer.valueOf(identity.getTac()));
                    if (is5GNsa) {
                        contentValues.put("type", "5GNSA");
                    } else {
                        contentValues.put("type", "4G");
                    }
                    contentValues.put("data", key);
                    getInstance().publishEvent(107, contentValues);
                    return;
                } else if (cell.isRegistered() && (cell instanceof CellInfoNr)) {
                    CellIdentityNr identity2 = (CellIdentityNr) ((CellInfoNr) cell).getCellIdentity();
                    String key2 = String.format("subid-%d-mcc-%s-mnc-%s-ci-%d-pci-%d-tac-%d", Integer.valueOf(subId), identity2.getMccString(), identity2.getMncString(), Long.valueOf(identity2.getNci()), Integer.valueOf(identity2.getPci()), Integer.valueOf(identity2.getTac()));
                    contentValues.put("type", "5GSA");
                    contentValues.put("data", key2);
                    getInstance().publishEvent(107, contentValues);
                    return;
                }
            }
        }
    }
}
