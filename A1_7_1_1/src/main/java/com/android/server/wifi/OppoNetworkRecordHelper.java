package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.net.DelayedDiskWrite;
import com.android.server.net.DelayedDiskWrite.Writer;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OppoNetworkRecordHelper {
    private static final long AUTO_CLEAR_UNTIL = 2592000000L;
    private static final int FIRST_AMOUNT_TO_REMOVE = 60;
    public static final int MAX_NETWORK_RECORDS = 100;
    private static final int SECOND_AMOUNT_TO_REMOVE = 20;
    private static final String TAG = "OppoNetworkRecordHelper";
    private static boolean VDBG = false;
    public static final String WIFI_AUTO_CLEAR_TIMESTAMP = "persist.wifi.auto_clear";
    private static long autoClearUntil;
    private static final String networkRecordFile = null;
    private HashMap<Integer, NetworkRecord> mNetworkRecords;
    private WifiConfigManager mWifiConfigManager;
    final DelayedDiskWrite mWriter;

    public class NetworkRecord {
        String SSID;
        boolean changed;
        int count;
        boolean isolated;
        int networkId;
        long timestamp;

        public NetworkRecord(NetworkRecord record) {
            this.networkId = record.networkId;
            this.SSID = record.SSID;
            this.timestamp = record.timestamp;
            this.count = record.count;
            this.changed = record.changed;
            this.isolated = record.isolated;
        }

        public NetworkRecord(int netid, String ssid, long time, int c) {
            this.networkId = netid;
            this.SSID = ssid;
            this.timestamp = time;
            this.count = c;
            this.changed = false;
            this.isolated = false;
        }

        public NetworkRecord(String record) {
            if (record != null) {
                String[] details = record.split("\t");
                if (details.length == 4) {
                    try {
                        this.networkId = Integer.parseInt(details[0]);
                        this.SSID = details[1];
                        try {
                            this.timestamp = Long.parseLong(details[2]);
                        } catch (NumberFormatException e) {
                            Log.e(OppoNetworkRecordHelper.TAG, "Failed to parse timestamp '" + details[2] + "'");
                            this.timestamp = 0;
                        }
                        try {
                            this.count = Integer.parseInt(details[3]);
                        } catch (NumberFormatException e2) {
                            Log.e(OppoNetworkRecordHelper.TAG, "Failed to parse count '" + details[3] + "'");
                            this.count = 1;
                        }
                        this.changed = false;
                        this.isolated = false;
                    } catch (NumberFormatException e3) {
                        Log.e(OppoNetworkRecordHelper.TAG, "Failed to parse network-id '" + details[0] + "'");
                        this.networkId = -1;
                        return;
                    }
                }
                Log.e(OppoNetworkRecordHelper.TAG, "Failed to create NetworkRecord:Invalid record:" + record);
                this.networkId = -1;
            } else {
                this.networkId = -1;
            }
        }

        public boolean isValid() {
            if (this.networkId == -1 || this.SSID == null || this.timestamp <= 0 || this.count <= 0) {
                return false;
            }
            return true;
        }

        public String toString() {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(this.networkId).append("\t");
            sbuf.append(this.SSID).append("\t");
            sbuf.append(this.timestamp).append("\t");
            sbuf.append(this.count);
            return sbuf.toString();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            NetworkRecord other = (NetworkRecord) obj;
            return other.networkId == this.networkId && other.SSID.equals(this.SSID) && other.timestamp == this.timestamp && other.count == this.count;
        }
    }

    class RecordCountComparator implements Comparator<NetworkRecord> {
        RecordCountComparator() {
        }

        public int compare(NetworkRecord o1, NetworkRecord o2) {
            if (o1.count > o2.count) {
                return -1;
            }
            if (o1.count < o2.count) {
                return 1;
            }
            return 0;
        }
    }

    class RecordNetIdComparator implements Comparator<NetworkRecord> {
        RecordNetIdComparator() {
        }

        public int compare(NetworkRecord o1, NetworkRecord o2) {
            if (o1.networkId > o2.networkId) {
                return 1;
            }
            if (o1.networkId < o2.networkId) {
                return -1;
            }
            return 0;
        }
    }

    class RecordTimeComparator implements Comparator<NetworkRecord> {
        RecordTimeComparator() {
        }

        public int compare(NetworkRecord o1, NetworkRecord o2) {
            if (o1.timestamp > o2.timestamp) {
                return -1;
            }
            if (o1.timestamp < o2.timestamp) {
                return 1;
            }
            return 0;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.OppoNetworkRecordHelper.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.OppoNetworkRecordHelper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.OppoNetworkRecordHelper.<clinit>():void");
    }

    public OppoNetworkRecordHelper(WifiConfigManager configStore) {
        this.mNetworkRecords = new HashMap();
        this.mWifiConfigManager = configStore;
        this.mWriter = new DelayedDiskWrite();
    }

    /* JADX WARNING: Missing block: B:16:0x0056, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean addOrUpdateNetworkRecord(NetworkRecord record, boolean save) {
        NetworkRecord t_record = new NetworkRecord(record);
        synchronized (this.mNetworkRecords) {
            if (t_record.isValid()) {
                NetworkRecord oldRecord = (NetworkRecord) this.mNetworkRecords.get(Integer.valueOf(t_record.networkId));
                if (oldRecord == null) {
                    if (VDBG) {
                        Log.d(TAG, "Adding new record:" + t_record);
                    }
                    t_record.changed = true;
                } else if (!oldRecord.equals(t_record)) {
                    if (VDBG) {
                        Log.d(TAG, "Updating record, old record:" + oldRecord + " new record:" + t_record);
                    }
                    t_record.changed = true;
                }
                this.mNetworkRecords.put(Integer.valueOf(t_record.networkId), t_record);
                if (t_record.changed && save) {
                    saveAllNetworkRecords(true);
                }
            } else {
                Log.e(TAG, "addOrUpdateNetworkRecord invalid record:" + record);
                return false;
            }
        }
    }

    public boolean addOrUpdateNetworkRecord(int networkId, String SSID) {
        int netId = networkId;
        String ssid = SSID;
        long time = System.currentTimeMillis();
        int count = 1;
        NetworkRecord record = (NetworkRecord) this.mNetworkRecords.get(Integer.valueOf(networkId));
        if (record != null) {
            count = record.count + 1;
        }
        return addOrUpdateNetworkRecord(new NetworkRecord(networkId, SSID, time, count), true);
    }

    public boolean addOrUpdateNetworkRecordWithoutSaving(int networkId, String SSID) {
        int netId = networkId;
        String ssid = SSID;
        long time = System.currentTimeMillis();
        int count = 1;
        NetworkRecord record = (NetworkRecord) this.mNetworkRecords.get(Integer.valueOf(networkId));
        if (record != null) {
            count = record.count + 1;
        }
        return addOrUpdateNetworkRecord(new NetworkRecord(networkId, SSID, time, count), false);
    }

    public boolean removeNetworkRecord(NetworkRecord record) {
        synchronized (this.mNetworkRecords) {
            if (record != null) {
                if (record.isValid()) {
                    if (this.mNetworkRecords.remove(Integer.valueOf(record.networkId)) == null) {
                        Log.e(TAG, "removeNetworkRecord failed, no record found:" + record);
                        return false;
                    }
                    Log.d(TAG, "Successfully removed network record_1:" + record);
                    saveAllNetworkRecords(true);
                    return true;
                }
            }
            Log.e(TAG, "removeNetworkRecord failed, invalid record:" + record);
            return false;
        }
    }

    public boolean removeNetworkRecord(int networkId) {
        return removeNetworkRecord((NetworkRecord) this.mNetworkRecords.get(Integer.valueOf(networkId)));
    }

    public boolean removeNetworkRecordWithoutSaving(NetworkRecord record) {
        synchronized (this.mNetworkRecords) {
            if (record != null) {
                if (record.isValid()) {
                    if (this.mNetworkRecords.remove(Integer.valueOf(record.networkId)) == null) {
                        Log.e(TAG, "removeNetworkRecordWithoutSaving failed, no record found:" + record);
                        return false;
                    }
                    if (VDBG) {
                        Log.d(TAG, "Successfully removed network record_2:" + record);
                    }
                    return true;
                }
            }
            Log.e(TAG, "removeNetworkRecordWithoutSaving failed, invalid record:" + record);
            return false;
        }
    }

    public boolean removeNetworkRecordWithoutSaving(int networkId) {
        return removeNetworkRecordWithoutSaving((NetworkRecord) this.mNetworkRecords.get(Integer.valueOf(networkId)));
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x00c6 A:{SYNTHETIC, Splitter: B:28:0x00c6} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00d7  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00c6 A:{SYNTHETIC, Splitter: B:28:0x00c6} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00d7  */
    /* JADX WARNING: Removed duplicated region for block: B:84:? A:{SYNTHETIC, RETURN, SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00ff  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00bf A:{SYNTHETIC, Splitter: B:23:0x00bf} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00c6 A:{SYNTHETIC, Splitter: B:28:0x00c6} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00d7  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00ff  */
    /* JADX WARNING: Removed duplicated region for block: B:84:? A:{SYNTHETIC, RETURN, SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0157 A:{SYNTHETIC, Splitter: B:52:0x0157} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00c6 A:{SYNTHETIC, Splitter: B:28:0x00c6} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00d7  */
    /* JADX WARNING: Removed duplicated region for block: B:84:? A:{SYNTHETIC, RETURN, SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00ff  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00c6 A:{SYNTHETIC, Splitter: B:28:0x00c6} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00d7  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00ff  */
    /* JADX WARNING: Removed duplicated region for block: B:84:? A:{SYNTHETIC, RETURN, SKIP} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void loadAllNetworkRecords() {
        int num;
        List<WifiConfiguration> configs;
        IOException e;
        Throwable th;
        BufferedReader reader = null;
        synchronized (this.mNetworkRecords) {
            try {
                this.mNetworkRecords.clear();
                try {
                    BufferedReader reader2 = new BufferedReader(new FileReader(networkRecordFile));
                    try {
                        for (String line = reader2.readLine(); line != null; line = reader2.readLine()) {
                            if (line != null && VDBG) {
                                Log.d(TAG, "loadAllNetworkRecords line: " + line);
                            }
                            NetworkRecord record = new NetworkRecord(line);
                            if (record.isValid()) {
                                this.mNetworkRecords.put(Integer.valueOf(record.networkId), record);
                                if (VDBG) {
                                    Log.d(TAG, "loadAllNetworkRecords put record: " + record);
                                }
                            } else {
                                Log.e(TAG, "loadAllNetworkRecords invalid record:" + record);
                            }
                        }
                        reader = reader2;
                    } catch (EOFException e2) {
                        reader = reader2;
                        if (reader != null) {
                            try {
                                reader.close();
                                reader = null;
                            } catch (Exception e3) {
                                Log.e(TAG, "loadAllNetworkRecords: Error closing file:" + e3);
                            }
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (Exception e32) {
                                Log.e(TAG, "loadAllNetworkRecord: Error closing file" + e32);
                            }
                        }
                        num = this.mNetworkRecords.size();
                        if (VDBG) {
                            Log.d(TAG, "After loadAllNetworkRecords number of records: " + num);
                        }
                        configs = this.mWifiConfigManager.getSavedNetworks();
                        if (num == 0 && configs.size() > 0) {
                            for (WifiConfiguration config : configs) {
                                if (addOrUpdateNetworkRecordWithoutSaving(config.networkId, config.SSID)) {
                                    num++;
                                }
                            }
                            saveAllNetworkRecords(true);
                            Log.w(TAG, "loadAllNetworkRecords added: " + num + " records from ConfiguredNetworks");
                            if (num > 80) {
                                SystemProperties.set(WIFI_AUTO_CLEAR_TIMESTAMP, Long.toString(System.currentTimeMillis() + AUTO_CLEAR_UNTIL));
                                autoClearUntil = SystemClock.elapsedRealtime() + AUTO_CLEAR_UNTIL;
                                if (VDBG) {
                                    Log.d(TAG, "loadAllNetworkRecords current time:" + System.currentTimeMillis() + "  persistAutoClearTime:" + SystemProperties.getLong(WIFI_AUTO_CLEAR_TIMESTAMP, 0) + "  elapsedRealtime:" + SystemClock.elapsedRealtime() + "  autoClearUntil:" + autoClearUntil);
                                }
                            }
                        }
                    } catch (FileNotFoundException e4) {
                        reader = reader2;
                        if (reader != null) {
                            reader.close();
                            reader = null;
                        }
                        Log.e(TAG, "loadAllNetworkRecords networkRecord.txt not found!");
                        if (reader != null) {
                        }
                        num = this.mNetworkRecords.size();
                        if (VDBG) {
                        }
                        configs = this.mWifiConfigManager.getSavedNetworks();
                        if (num == 0) {
                        }
                    } catch (IOException e5) {
                        e = e5;
                        reader = reader2;
                        Log.e(TAG, "loadAllNetworkRecords: Error reading network records:" + e);
                        if (reader != null) {
                        }
                        num = this.mNetworkRecords.size();
                        if (VDBG) {
                        }
                        configs = this.mWifiConfigManager.getSavedNetworks();
                        if (num == 0) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                } catch (EOFException e6) {
                    if (reader != null) {
                    }
                    if (reader != null) {
                    }
                    num = this.mNetworkRecords.size();
                    if (VDBG) {
                    }
                    configs = this.mWifiConfigManager.getSavedNetworks();
                    if (num == 0) {
                    }
                } catch (FileNotFoundException e7) {
                    if (reader != null) {
                    }
                    Log.e(TAG, "loadAllNetworkRecords networkRecord.txt not found!");
                    if (reader != null) {
                    }
                    num = this.mNetworkRecords.size();
                    if (VDBG) {
                    }
                    configs = this.mWifiConfigManager.getSavedNetworks();
                    if (num == 0) {
                    }
                } catch (IOException e8) {
                    e = e8;
                    Log.e(TAG, "loadAllNetworkRecords: Error reading network records:" + e);
                    if (reader != null) {
                    }
                    num = this.mNetworkRecords.size();
                    if (VDBG) {
                    }
                    configs = this.mWifiConfigManager.getSavedNetworks();
                    if (num == 0) {
                    }
                }
            } catch (Exception e322) {
                Log.e(TAG, "loadAllNetworkRecords: Error closing file:" + e322);
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    public void saveAllNetworkRecords(boolean force) {
        final List<NetworkRecord> networkRecords = getNetworkRecords();
        boolean needWrite = false;
        final int[] count = new int[1];
        count[0] = 0;
        for (NetworkRecord record : networkRecords) {
            if (VDBG) {
                Log.d(TAG, "saveAllNetworkRecords record:" + record.toString() + " changed=" + record.changed);
            }
            if (this.mWifiConfigManager.getConfiguredNetwork(record.networkId) == null) {
                Log.w(TAG, "saveAllNetworkRecords isolated record found:" + record);
                removeNetworkRecordWithoutSaving(record);
                if (VDBG) {
                    Log.w(TAG, "saveAllNetworkRecords removed isolated record:" + record);
                }
                record.isolated = true;
                needWrite = true;
            }
            if (record.changed) {
                record.changed = false;
                needWrite = true;
            }
        }
        if (VDBG) {
            Log.d(TAG, " saveAllNetworkRecords num of records:" + this.mNetworkRecords.size() + "  force=" + force + "  needWrite=" + needWrite);
        }
        if (force || needWrite) {
            this.mWriter.write(networkRecordFile, new Writer() {
                public void onWriteCalled(DataOutputStream out) throws IOException {
                    for (NetworkRecord record : networkRecords) {
                        if (!record.isValid()) {
                            Log.w(OppoNetworkRecordHelper.TAG, "onWriteCalled invalid record:" + record);
                        } else if (record.isolated) {
                            Log.w(OppoNetworkRecordHelper.TAG, "onWriteCalled isolated record:" + record + " ,skip.");
                        } else {
                            if (OppoNetworkRecordHelper.VDBG) {
                                Log.d(OppoNetworkRecordHelper.TAG, "onWriteCalled write record:" + record.toString());
                            }
                            out.writeBytes(record.toString() + "\n");
                            int[] iArr = count;
                            iArr[0] = iArr[0] + 1;
                        }
                    }
                    if (OppoNetworkRecordHelper.VDBG) {
                        Log.d(OppoNetworkRecordHelper.TAG, " saveAllNetworkRecords finished saving " + count[0] + " records!");
                    }
                }
            });
        } else {
            Log.w(TAG, "saveAllNetworkRecords no need to write");
        }
    }

    public List<NetworkRecord> getNetworkRecords() {
        List<NetworkRecord> networkRecords = new ArrayList();
        synchronized (this.mNetworkRecords) {
            for (NetworkRecord record : this.mNetworkRecords.values()) {
                networkRecords.add(new NetworkRecord(record));
            }
        }
        return networkRecords;
    }

    public NetworkRecord getNetworkRecord(int networkId) {
        NetworkRecord record = (NetworkRecord) this.mNetworkRecords.get(Integer.valueOf(networkId));
        if (record == null) {
            return null;
        }
        return new NetworkRecord(record);
    }

    public void clearObsoleteNetworks() {
        Log.d(TAG, "clearObsoleteNetworks");
        long persistAutoClearTime = SystemProperties.getLong(WIFI_AUTO_CLEAR_TIMESTAMP, 0);
        if (VDBG) {
            Log.d(TAG, "current time:" + System.currentTimeMillis() + "  persistAutoClearTime:" + persistAutoClearTime + "  elapsedRealtime:" + SystemClock.elapsedRealtime() + "  autoClearUntil:" + autoClearUntil);
        }
        if (System.currentTimeMillis() <= persistAutoClearTime || SystemClock.elapsedRealtime() <= autoClearUntil) {
            Log.w(TAG, "clearObsoleteNetworks is disabled until " + new Date(persistAutoClearTime));
            return;
        }
        int i;
        Log.d(TAG, "clearObsoleteNetworks is granted to proceed");
        ArrayList<NetworkRecord> networkRecords = (ArrayList) getNetworkRecords();
        Collections.sort(networkRecords, new RecordTimeComparator());
        if (VDBG) {
            Log.d(TAG, "clearObsoleteNetworks after sort by timestamp:");
        }
        if (VDBG) {
            dumpNetowrkRecords(networkRecords);
        }
        for (i = 0; i < FIRST_AMOUNT_TO_REMOVE; i++) {
            networkRecords.remove(0);
        }
        if (VDBG) {
            Log.d(TAG, "clearObsoleteNetworks after first remove:");
        }
        if (VDBG) {
            dumpNetowrkRecords(networkRecords);
        }
        Collections.sort(networkRecords, new RecordCountComparator());
        if (VDBG) {
            Log.d(TAG, "clearObsoleteNetworks after sort by count:");
        }
        if (VDBG) {
            dumpNetowrkRecords(networkRecords);
        }
        for (i = 0; i < 20; i++) {
            networkRecords.remove(0);
        }
        if (VDBG) {
            Log.d(TAG, "clearObsoleteNetworks after second remove:");
        }
        if (VDBG) {
            dumpNetowrkRecords(networkRecords);
        }
        boolean removed = false;
        for (NetworkRecord record : networkRecords) {
            if (record != null && this.mWifiConfigManager.removeNetworkWithoutBroadcast(record.networkId)) {
                removed = true;
            }
        }
        if (removed) {
            this.mWifiConfigManager.saveConfig();
            this.mWifiConfigManager.sendConfiguredNetworksChangedBroadcast();
            this.mWifiConfigManager.writeIpAndProxyConfigurations();
            this.mWifiConfigManager.writeKnownNetworkHistory();
            saveAllNetworkRecords(true);
        }
        if (VDBG) {
            Log.d(TAG, "clearObsoleteNetworks after remove obsolete records:");
        }
        networkRecords = (ArrayList) getNetworkRecords();
        Collections.sort(networkRecords, new RecordNetIdComparator());
        if (VDBG) {
            dumpNetowrkRecords(networkRecords);
        }
    }

    public void dumpNetowrkRecords(List<NetworkRecord> records) {
        Log.d(TAG, "dumpNetowrkRecords:");
        for (NetworkRecord record : records) {
            Log.d(TAG, "dump record:" + record);
        }
    }

    void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            VDBG = true;
        } else {
            VDBG = false;
        }
    }
}
