package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.telephony.CarrierConfigManager;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.util.SparseArray;
import android.util.SparseIntArray;
import java.util.Arrays;

public class RatRatcheter {
    private static final String LOG_TAG = "RilRatcheter";
    private BroadcastReceiver mConfigChangedReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.RatRatcheter.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction())) {
                RatRatcheter.this.resetRatFamilyMap();
            }
        }
    };
    private boolean mDataRatchetEnabled = true;
    private final Phone mPhone;
    private final SparseArray<SparseIntArray> mRatFamilyMap = new SparseArray<>();
    private boolean mVoiceRatchetEnabled = true;

    public static boolean updateBandwidths(int[] bandwidths, ServiceState serviceState) {
        if (bandwidths == null || Arrays.stream(bandwidths).sum() <= Arrays.stream(serviceState.getCellBandwidths()).sum()) {
            return false;
        }
        serviceState.setCellBandwidths(bandwidths);
        return true;
    }

    public RatRatcheter(Phone phone) {
        this.mPhone = phone;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        phone.getContext().registerReceiverAsUser(this.mConfigChangedReceiver, UserHandle.ALL, intentFilter, null, null);
        resetRatFamilyMap();
    }

    private int ratchetRat(int oldNetworkType, int newNetworkType) {
        int oldRat = ServiceState.networkTypeToRilRadioTechnology(oldNetworkType);
        int newRat = ServiceState.networkTypeToRilRadioTechnology(newNetworkType);
        synchronized (this.mRatFamilyMap) {
            SparseIntArray oldFamily = this.mRatFamilyMap.get(oldRat);
            if (oldFamily == null) {
                return newNetworkType;
            }
            SparseIntArray newFamily = this.mRatFamilyMap.get(newRat);
            if (newFamily != oldFamily) {
                return newNetworkType;
            }
            int rilRadioTechnologyToNetworkType = ServiceState.rilRadioTechnologyToNetworkType(newFamily.get(oldRat, -1) > newFamily.get(newRat, -1) ? oldRat : newRat);
            return rilRadioTechnologyToNetworkType;
        }
    }

    public void ratchet(ServiceState oldSS, ServiceState newSS, boolean locationChange) {
        if (!locationChange && isSameRatFamily(oldSS, newSS)) {
            updateBandwidths(oldSS.getCellBandwidths(), newSS);
        }
        boolean newUsingCA = false;
        if (locationChange) {
            this.mVoiceRatchetEnabled = false;
            this.mDataRatchetEnabled = false;
            return;
        }
        if (oldSS.isUsingCarrierAggregation() || newSS.isUsingCarrierAggregation() || newSS.getCellBandwidths().length > 1) {
            newUsingCA = true;
        }
        NetworkRegistrationInfo oldCsNri = oldSS.getNetworkRegistrationInfo(1, 1);
        NetworkRegistrationInfo newCsNri = newSS.getNetworkRegistrationInfo(1, 1);
        if (this.mVoiceRatchetEnabled) {
            newCsNri.setAccessNetworkTechnology(ratchetRat(oldCsNri.getAccessNetworkTechnology(), newCsNri.getAccessNetworkTechnology()));
            newSS.addNetworkRegistrationInfo(newCsNri);
        } else if (oldCsNri.getAccessNetworkTechnology() != oldCsNri.getAccessNetworkTechnology()) {
            this.mVoiceRatchetEnabled = true;
        }
        NetworkRegistrationInfo oldPsNri = oldSS.getNetworkRegistrationInfo(2, 1);
        NetworkRegistrationInfo newPsNri = newSS.getNetworkRegistrationInfo(2, 1);
        if (this.mDataRatchetEnabled) {
            newPsNri.setAccessNetworkTechnology(ratchetRat(oldPsNri.getAccessNetworkTechnology(), newPsNri.getAccessNetworkTechnology()));
            newSS.addNetworkRegistrationInfo(newPsNri);
        } else if (oldPsNri.getAccessNetworkTechnology() != newPsNri.getAccessNetworkTechnology()) {
            this.mDataRatchetEnabled = true;
        }
        newSS.setIsUsingCarrierAggregation(newUsingCA);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003f, code lost:
        return r2;
     */
    private boolean isSameRatFamily(ServiceState ss1, ServiceState ss2) {
        synchronized (this.mRatFamilyMap) {
            boolean z = true;
            int dataRat1 = ServiceState.networkTypeToRilRadioTechnology(ss1.getNetworkRegistrationInfo(2, 1).getAccessNetworkTechnology());
            int dataRat2 = ServiceState.networkTypeToRilRadioTechnology(ss2.getNetworkRegistrationInfo(2, 1).getAccessNetworkTechnology());
            if (dataRat1 == dataRat2) {
                return true;
            }
            if (this.mRatFamilyMap.get(dataRat1) == null) {
                return false;
            }
            if (this.mRatFamilyMap.get(dataRat1) != this.mRatFamilyMap.get(dataRat2)) {
                z = false;
            }
        }
    }

    /* access modifiers changed from: private */
    public void resetRatFamilyMap() {
        synchronized (this.mRatFamilyMap) {
            this.mRatFamilyMap.clear();
            CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
            if (configManager != null) {
                PersistableBundle b = configManager.getConfig();
                if (b != null) {
                    String[] ratFamilies = b.getStringArray("ratchet_rat_families");
                    if (ratFamilies != null) {
                        for (String ratFamily : ratFamilies) {
                            String[] rats = ratFamily.split(",");
                            if (rats.length >= 2) {
                                SparseIntArray currentFamily = new SparseIntArray(rats.length);
                                int length = rats.length;
                                int pos = 0;
                                int pos2 = 0;
                                while (true) {
                                    if (pos2 >= length) {
                                        break;
                                    }
                                    String ratString = rats[pos2];
                                    try {
                                        int ratInt = Integer.parseInt(ratString.trim());
                                        if (this.mRatFamilyMap.get(ratInt) != null) {
                                            Rlog.e(LOG_TAG, "RAT listed twice: " + ratString);
                                            break;
                                        }
                                        currentFamily.put(ratInt, pos);
                                        this.mRatFamilyMap.put(ratInt, currentFamily);
                                        pos2++;
                                        pos++;
                                    } catch (NumberFormatException e) {
                                        Rlog.e(LOG_TAG, "NumberFormatException on " + ratString);
                                    } catch (Exception e2) {
                                        Rlog.d(LOG_TAG, e2.toString());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
