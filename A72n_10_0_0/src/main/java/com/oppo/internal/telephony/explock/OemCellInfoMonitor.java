package com.oppo.internal.telephony.explock;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class OemCellInfoMonitor {
    private static final String KEY_CELL_RECORD = "key_cell_record";
    private static final String PREFS_NAME = "region_lock_service_cell_record";
    private static Context mContext = null;
    private static OemCellInfoMonitor sInstance = null;
    private Set<String> mCellRecords = null;
    private SharedPreferences mSharedPrefs;

    public static synchronized OemCellInfoMonitor getDefault(Context context) {
        OemCellInfoMonitor oemCellInfoMonitor;
        synchronized (OemCellInfoMonitor.class) {
            if (sInstance == null) {
                mContext = context;
                sInstance = new OemCellInfoMonitor(context);
            }
            oemCellInfoMonitor = sInstance;
        }
        return oemCellInfoMonitor;
    }

    public OemCellInfoMonitor(Context context) {
        this.mSharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        parse();
    }

    private void parse() {
        Set<String> cellRecords = this.mSharedPrefs.getStringSet(KEY_CELL_RECORD, new HashSet());
        this.mCellRecords = new HashSet();
        this.mCellRecords.addAll(cellRecords);
    }

    public void addRecord(String cell) {
        if (!isInclude(cell) && getNetworkCellCount() < 5) {
            this.mCellRecords.add(cell);
            this.mSharedPrefs.edit().putStringSet(KEY_CELL_RECORD, this.mCellRecords).commit();
            updateNetworkInfo(getNetworkCellCount());
        }
    }

    public void removeRecord(String cell) {
        if (this.mCellRecords.contains(cell)) {
            this.mCellRecords.remove(cell);
        }
        this.mSharedPrefs.edit().putStringSet(KEY_CELL_RECORD, this.mCellRecords).commit();
        updateNetworkInfo(getNetworkCellCount());
    }

    public boolean isInclude(String cell) {
        return this.mCellRecords.contains(cell);
    }

    public void updateNetworkInfo(int count) {
        OemRegionLockMonitorManager orlm = OemRegionLockMonitorManager.getInstance();
        if (orlm != null) {
            orlm.updateNetworkInfo(count);
        }
    }

    public int getNetworkCellCount() {
        HashSet<String> keySet = (HashSet) this.mSharedPrefs.getStringSet(KEY_CELL_RECORD, null);
        if (keySet == null || keySet.size() <= 0) {
            return -1;
        }
        return keySet.size();
    }

    public void isMatchRegisterDuration(boolean hasMatchRegisterDuration) {
        OemRegionLockMonitorManager orlm = OemRegionLockMonitorManager.getInstance();
        if (orlm != null) {
            orlm.updateMatchRegisterDurationState(hasMatchRegisterDuration);
        }
    }
}
