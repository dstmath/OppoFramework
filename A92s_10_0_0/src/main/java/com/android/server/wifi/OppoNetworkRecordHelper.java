package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class OppoNetworkRecordHelper {
    private static final int FIRST_AMOUNT_TO_SAVE = 60;
    public static final int MAX_NETWORK_RECORDS = 100;
    protected static final String NETWORK_RECORD_FILE = (Environment.getDataDirectory() + "/misc/wifi/networkRecord.txt");
    private static final int RECORD_LENGTH = 4;
    private static final int SECOND_AMOUNT_TO_SAVE = 20;
    private static final String TAG = "OppoNetworkRecordHelper";
    private static boolean mDebug = false;
    private HashMap<String, Pair> mNetworkRecords = new HashMap<>();
    private WifiConfigManager mWifiConfigManager;

    OppoNetworkRecordHelper(WifiConfigManager wifiConfigManager) {
        this.mWifiConfigManager = wifiConfigManager;
    }

    /* access modifiers changed from: protected */
    public void clearObsoleteNetworks() {
        List<WifiConfiguration> networksToBeRemoved = this.mWifiConfigManager.getSavedNetworks(1010);
        if (networksToBeRemoved.size() > 80) {
            Log.d(TAG, "clearing Obsolete Networks");
            if (mDebug) {
                Log.d(TAG, " first sort the networks by lastConnected time.");
            }
            Collections.sort(networksToBeRemoved, new RecordLastConnectedTimeComparator());
            int i = 0;
            int index = networksToBeRemoved.size() - 1;
            while (i < 60 && index >= 0) {
                if (!networksToBeRemoved.get(index).getNetworkSelectionStatus().getHasEverConnected()) {
                    i--;
                } else {
                    networksToBeRemoved.remove(index);
                }
                i++;
                index--;
            }
            if (mDebug) {
                Log.d(TAG, " second sort the networks by numAssociation count.");
            }
            Collections.sort(networksToBeRemoved, new RecordConnectCountComparator());
            for (int i2 = 0; i2 < 20; i2++) {
                networksToBeRemoved.remove(networksToBeRemoved.size() - 1);
            }
            int needToRemoveNum = networksToBeRemoved.size();
            if (mDebug) {
                Log.d(TAG, " " + needToRemoveNum + " networks will be removed!");
            }
            for (WifiConfiguration config : networksToBeRemoved) {
                if (config != null && this.mWifiConfigManager.removeNetworkWithoutBroadcast(config.networkId)) {
                    needToRemoveNum--;
                }
            }
            if (needToRemoveNum == 0) {
                this.mWifiConfigManager.sendConfiguredNetworksChangedBroadcast();
                this.mWifiConfigManager.saveToStore(true);
                Log.d(TAG, " successfully clean redundant configurated networks!");
                return;
            }
            Log.e(TAG, " failed to remove all networks!! " + needToRemoveNum + " left.");
        }
    }

    class RecordLastConnectedTimeComparator implements Comparator<WifiConfiguration> {
        RecordLastConnectedTimeComparator() {
        }

        public int compare(WifiConfiguration c1, WifiConfiguration c2) {
            long t1 = c1.lastConnected;
            long t2 = c2.lastConnected;
            if (t1 != t2) {
                return t1 > t2 ? 1 : -1;
            }
            return 0;
        }
    }

    class RecordConnectCountComparator implements Comparator<WifiConfiguration> {
        RecordConnectCountComparator() {
        }

        public int compare(WifiConfiguration c1, WifiConfiguration c2) {
            int t1 = c1.numAssociation;
            int t2 = c2.numAssociation;
            if (t1 != t2) {
                return t1 > t2 ? 1 : -1;
            }
            return 0;
        }
    }

    public void dump(List<WifiConfiguration> networks) {
        Log.d(TAG, " dump need-to-remove networks:");
        Iterator<WifiConfiguration> it = networks.iterator();
        while (it.hasNext()) {
            Log.d(TAG, " " + it.next().SSID);
        }
    }

    public static void enableVerboseLogging(int Verbose) {
        if (Verbose > 0) {
            mDebug = true;
        } else {
            mDebug = false;
        }
    }

    public void loadAllNetworkRecords() {
        String str;
        String str2;
        BufferedReader reader = null;
        synchronized (this.mNetworkRecords) {
            this.mNetworkRecords.clear();
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(NETWORK_RECORD_FILE));
                for (String line = reader2.readLine(); line != null; line = reader2.readLine()) {
                    Log.d(TAG, "loadAllNetworkRecords line: " + line);
                    String[] details = line.split("\t");
                    if (details.length == 4) {
                        try {
                            this.mNetworkRecords.put(details[1], new Pair(Long.parseLong(details[2]), Integer.parseInt(details[3])));
                        } catch (Exception e) {
                            Log.e(TAG, " failed to parse: " + e);
                        }
                    } else {
                        Log.e(TAG, "loadAllNetworkRecords invalid record;");
                    }
                }
                try {
                    reader2.close();
                } catch (Exception e2) {
                    str = TAG;
                    str2 = "loadAllNetworkRecords: Error closing file:" + e2;
                    Log.e(str, str2);
                    Log.d(TAG, "After loadAllNetworkRecords number of records: " + this.mNetworkRecords.size());
                }
            } catch (Exception e3) {
                Log.e(TAG, "loadAllNetworkRecords ERROR: " + e3);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e4) {
                        str = TAG;
                        str2 = "loadAllNetworkRecords: Error closing file:" + e4;
                    }
                }
            } catch (Throwable th) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e5) {
                        Log.e(TAG, "loadAllNetworkRecords: Error closing file:" + e5);
                    }
                }
                throw th;
            }
        }
        Log.d(TAG, "After loadAllNetworkRecords number of records: " + this.mNetworkRecords.size());
    }

    public static boolean isNetworkRecordTxtPresent() {
        return new File(NETWORK_RECORD_FILE).exists();
    }

    public void deleteNetworkRecordTxt() {
        if (isNetworkRecordTxtPresent()) {
            if (!new File(NETWORK_RECORD_FILE).delete()) {
                Log.e(TAG, " failed to remove networkRecord.txt!");
            } else {
                Log.d(TAG, " networkRecord.txt removed!");
            }
            this.mNetworkRecords = null;
        }
    }

    public void fillFieldIfNecessary(WifiConfiguration configuration) {
        Pair netR = this.mNetworkRecords.get(configuration.SSID);
        if (netR != null) {
            if (configuration.lastConnected == 0) {
                configuration.lastConnected = netR.getLastConnectedTimeStamp();
            }
            int i = 2;
            if (configuration.numAssociation == 2) {
                int lNumAssociation = netR.getNumAssociation();
                if (lNumAssociation > 2) {
                    i = lNumAssociation;
                }
                configuration.numAssociation = i;
                return;
            }
            return;
        }
        Log.e(TAG, " no related record found in networkRecord.txt, use default value");
        if (configuration.lastConnected == 0) {
            configuration.lastConnected = System.currentTimeMillis() - 604800000;
        }
    }

    private class Pair {
        private long mPairlastConnected;
        private int mPairnumAssociation;

        public Pair(long l, int i) {
            this.mPairlastConnected = l;
            this.mPairnumAssociation = i;
        }

        public long getLastConnectedTimeStamp() {
            return this.mPairlastConnected;
        }

        public int getNumAssociation() {
            return this.mPairnumAssociation;
        }
    }
}
