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
import java.util.List;

public class OppoNetworkRecordHelper {
    private static final int FIRST_AMOUNT_TO_SAVE = 60;
    public static final int MAX_NETWORK_RECORDS = 100;
    protected static final String NETWORK_RECORD_FILE = (Environment.getDataDirectory() + "/misc/wifi/networkRecord.txt");
    private static final int RECORD_LENGTH = 4;
    private static final int SECOND_AMOUNT_TO_SAVE = 20;
    private static final String TAG = "OppoNetworkRecordHelper";
    private boolean mDebug = false;
    private HashMap<String, Pair> mNetworkRecords = new HashMap();
    private WifiConfigManager mWifiConfigManager;

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

    class RecordConnectCountComparator implements Comparator<WifiConfiguration> {
        RecordConnectCountComparator() {
        }

        public int compare(WifiConfiguration c1, WifiConfiguration c2) {
            int t1 = c1.numAssociation;
            int t2 = c2.numAssociation;
            if (t1 != t2) {
                return t1 > t2 ? 1 : -1;
            } else {
                return 0;
            }
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
            } else {
                return 0;
            }
        }
    }

    OppoNetworkRecordHelper(WifiConfigManager wifiConfigManager) {
        this.mWifiConfigManager = wifiConfigManager;
    }

    protected void clearObsoleteNetworks() {
        List<WifiConfiguration> networksToBeRemoved = this.mWifiConfigManager.getSavedNetworks();
        if (networksToBeRemoved.size() > 80) {
            Log.d(TAG, "clearing Obsolete Networks");
            if (this.mDebug) {
                Log.d(TAG, " first sort the networks by lastConnected time.");
            }
            Collections.sort(networksToBeRemoved, new RecordLastConnectedTimeComparator());
            int i = 0;
            int index = networksToBeRemoved.size() - 1;
            while (i < 60 && index >= 0) {
                if (((WifiConfiguration) networksToBeRemoved.get(index)).getNetworkSelectionStatus().getHasEverConnected()) {
                    networksToBeRemoved.remove(index);
                } else {
                    i--;
                }
                i++;
                index--;
            }
            if (this.mDebug) {
                Log.d(TAG, " second sort the networks by numAssociation count.");
            }
            Collections.sort(networksToBeRemoved, new RecordConnectCountComparator());
            for (i = 0; i < 20; i++) {
                networksToBeRemoved.remove(networksToBeRemoved.size() - 1);
            }
            int needToRemoveNum = networksToBeRemoved.size();
            if (this.mDebug) {
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
            } else {
                Log.e(TAG, " failed to remove all networks!! " + needToRemoveNum + " left.");
            }
        }
    }

    public void dump(List<WifiConfiguration> networks) {
        Log.d(TAG, " dump need-to-remove networks:");
        for (WifiConfiguration config : networks) {
            Log.d(TAG, " " + config.SSID);
        }
    }

    public void enableVerboseLogging(int Verbose) {
        if (Verbose > 0) {
            this.mDebug = true;
        } else {
            this.mDebug = false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x00cc A:{ExcHandler: all (th java.lang.Throwable), Splitter: B:6:0x0015} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:19:0x0062, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:20:0x0063, code:
            android.util.Log.e(TAG, " failed to parse: " + r1);
     */
    /* JADX WARNING: Missing block: B:34:0x00cc, code:
            r8 = th;
     */
    /* JADX WARNING: Missing block: B:35:0x00cd, code:
            r6 = r7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void loadAllNetworkRecords() {
        Exception e;
        Throwable th;
        BufferedReader bufferedReader = null;
        synchronized (this.mNetworkRecords) {
            try {
                this.mNetworkRecords.clear();
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(NETWORK_RECORD_FILE));
                    try {
                        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                            if (line != null) {
                                Log.d(TAG, "loadAllNetworkRecords line: " + line);
                            }
                            String[] details = line.split("\t");
                            if (details.length == 4) {
                                this.mNetworkRecords.put(details[1], new Pair(Long.parseLong(details[2]), Integer.parseInt(details[3])));
                            } else {
                                Log.e(TAG, "loadAllNetworkRecords invalid record;");
                            }
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (Exception e2) {
                                Log.e(TAG, "loadAllNetworkRecords: Error closing file:" + e2);
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                    } catch (Exception e3) {
                        e2 = e3;
                        bufferedReader = reader;
                    } catch (Throwable th3) {
                    }
                } catch (Exception e4) {
                    e2 = e4;
                    try {
                        Log.e(TAG, "loadAllNetworkRecords ERROR: " + e2);
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                        Log.d(TAG, "After loadAllNetworkRecords number of records: " + this.mNetworkRecords.size());
                    } catch (Throwable th4) {
                        th = th4;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (Exception e22) {
                                Log.e(TAG, "loadAllNetworkRecords: Error closing file:" + e22);
                            }
                        }
                        throw th;
                    }
                }
            } catch (Exception e222) {
                Log.e(TAG, "loadAllNetworkRecords: Error closing file:" + e222);
            } catch (Throwable th5) {
                th = th5;
            }
        }
    }

    public static boolean isNetworkRecordTxtPresent() {
        return new File(NETWORK_RECORD_FILE).exists();
    }

    public void deleteNetworkRecordTxt() {
        if (isNetworkRecordTxtPresent()) {
            if (new File(NETWORK_RECORD_FILE).delete()) {
                Log.d(TAG, " networkRecord.txt removed!");
            } else {
                Log.e(TAG, " failed to remove networkRecord.txt!");
            }
            this.mNetworkRecords = null;
        }
    }

    public void fillFieldIfNecessary(WifiConfiguration configuration) {
        Pair netR = (Pair) this.mNetworkRecords.get(configuration.SSID);
        if (netR != null) {
            if (configuration.lastConnected == 0) {
                configuration.lastConnected = netR.getLastConnectedTimeStamp();
            }
            if (configuration.numAssociation == 2) {
                int lNumAssociation = netR.getNumAssociation();
                if (lNumAssociation <= 2) {
                    lNumAssociation = 2;
                }
                configuration.numAssociation = lNumAssociation;
                return;
            }
            return;
        }
        Log.e(TAG, " no related record found in networkRecord.txt, use default value");
        if (configuration.lastConnected == 0) {
            configuration.lastConnected = System.currentTimeMillis() - 604800000;
        }
    }
}
