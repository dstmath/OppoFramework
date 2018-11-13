package com.mediatek.appworkingset;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Binder;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Iterator;

class AWSDBHelper extends SQLiteOpenHelper {
    private static final String CREATE_TABLE_PKG_PRIORITY_LIST = "CREATE TABLE aws_pkg_priority(prim_key_id INTEGER PRIMARY KEY,pkg_name TEXT,priority INTEGER)";
    private static final String CREATE_TABLE_PKG_PROC_LIST = "CREATE TABLE aws_pkg_process(prim_key_id INTEGER PRIMARY KEY,pkg_id INTEGER,proc_name TEXT,uid INTEGER,launch_mem INTEGER)";
    static final int DB_EMPTY = 1;
    private static final String DB_NAME = "awshelper.db";
    private static final int DB_VERSION = 1;
    static final boolean DEBUG_DB = false;
    static final int ERROR_MAX_EXCEEDED = -2;
    static final int ERROR_MAX_PKG_PRIORITY_LIST = 200;
    static final int ERROR_MAX_PKG_PROC_LIST = 2000;
    static final int MAX_EXCEEDED = -1;
    static final int MAX_PKG_PRIORITY_LIST = 100;
    static final int MAX_PKG_PROC_LIST = 1000;
    static final String TAG = "AWSDBHelper";
    final Context mContext;
    private boolean mIsReady = false;
    final AWSManager mManager;
    private int mNumPackagePriorityList;
    SparseArray<String> mPackageIDMap;
    ArrayList<PkgPriority> mPackagePriorityList;
    ArrayMap<String, PkgPriority> mPackagePriorityMap;

    public interface PackagePriorityList {
        public static final String KEY_ID = "prim_key_id";
        public static final String KEY_PKG_NAME = "pkg_name";
        public static final String KEY_PRIORITY = "priority";
        public static final String TABLE_NAME = "aws_pkg_priority";
    }

    public interface PackageProcessList {
        public static final String KEY_ID = "prim_key_id";
        public static final String KEY_LAUNCH_MEM = "launch_mem";
        public static final String KEY_PKG_ID = "pkg_id";
        public static final String KEY_PROC_NAME = "proc_name";
        public static final String KEY_UID = "uid";
        public static final String TABLE_NAME = "aws_pkg_process";
    }

    private class PkgPriority {
        private Integer ID;
        private String pkgName;
        private int pkgPriority;

        public PkgPriority(Integer num, String str, int i) {
            this.ID = num;
            this.pkgName = str;
            this.pkgPriority = i;
        }

        public Integer getPkgID() {
            return this.ID;
        }

        public String getPkgName() {
            return this.pkgName;
        }

        public int getPriority() {
            return this.pkgPriority;
        }

        public void setPriority(int i) {
            this.pkgPriority = i;
        }
    }

    private class PkgProcess {
        private Integer PkgID;
        private String dummyPkgName;
        private long launchMem;
        private String procName;
        private int uid;

        public PkgProcess(String str, int i, long j, String str2) {
            this.PkgID = null;
            this.procName = str;
            this.uid = i;
            this.launchMem = j;
            this.dummyPkgName = str2;
        }

        public PkgProcess(Integer num, String str, int i, long j) {
            this.PkgID = num;
            this.procName = str;
            this.uid = i;
            this.launchMem = j;
        }

        public Integer getPkgID() {
            return this.PkgID;
        }

        public String getProcName() {
            return this.procName;
        }

        public int getUID() {
            return this.uid;
        }

        public long getlaunchMem() {
            return this.launchMem;
        }

        public String getdummyPkgName() {
            return this.dummyPkgName;
        }

        public void setPkgID(Integer num) {
            this.PkgID = num;
        }
    }

    public AWSDBHelper(Context context, AWSManager aWSManager) {
        super(context, DB_NAME, null, 1);
        if (aWSManager == null || context == null) {
            this.mManager = null;
            this.mContext = null;
            Log.e(TAG, "AWSDBHelper construct fail:" + aWSManager + "," + context);
            return;
        }
        this.mManager = aWSManager;
        this.mContext = context;
        this.mIsReady = true;
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL(CREATE_TABLE_PKG_PROC_LIST);
        sQLiteDatabase.execSQL(CREATE_TABLE_PKG_PRIORITY_LIST);
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS aws_pkg_process");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS aws_pkg_priority");
        onCreate(sQLiteDatabase);
    }

    private boolean IsSystemAndReady(String str) {
        if (!this.mIsReady) {
            Log.v(TAG, "not ready");
            return false;
        }
        int callingUid = Binder.getCallingUid();
        if (callingUid == 0 || callingUid == 1000) {
            return true;
        }
        throw new SecurityException(str + " called from non-system process");
    }

    protected void readDB() {
        if (IsSystemAndReady("readDB")) {
            ArrayList arrayList = new ArrayList();
            PackageManager packageManager = this.mContext.getPackageManager();
            if (packageManager != null) {
                int packagePriorityList = getPackagePriorityList();
                if (packagePriorityList == -2) {
                    return;
                }
                if (packagePriorityList == 1) {
                    Log.v(TAG, "DB size empty");
                    return;
                } else if (checkSizeOfPackageProcessList() != -2) {
                    Iterator it;
                    String pkgName;
                    for (ApplicationInfo applicationInfo : packageManager.getInstalledApplications(128)) {
                        arrayList.add(applicationInfo.packageName);
                    }
                    ArrayList arrayList2 = new ArrayList();
                    Iterator it2 = this.mPackagePriorityList.iterator();
                    while (it2.hasNext()) {
                        PkgPriority pkgPriority = (PkgPriority) it2.next();
                        it = arrayList.iterator();
                        while (it.hasNext()) {
                            if (pkgPriority.getPkgName().equals((String) it.next())) {
                                arrayList2.add(pkgPriority);
                                it2.remove();
                            }
                        }
                    }
                    if (!arrayList2.isEmpty()) {
                        SQLiteDatabase readableDatabase = getReadableDatabase();
                        String[] strArr = new String[]{PackageProcessList.KEY_PROC_NAME, PackageProcessList.KEY_UID, PackageProcessList.KEY_LAUNCH_MEM};
                        ArrayMap arrayMap = new ArrayMap();
                        readableDatabase.beginTransaction();
                        try {
                            Iterator it3 = arrayList2.iterator();
                            while (it3.hasNext()) {
                                PkgPriority pkgPriority2 = (PkgPriority) it3.next();
                                arrayMap.put(pkgPriority2.getPkgName(), readableDatabase.query(PackageProcessList.TABLE_NAME, strArr, "pkg_id=" + pkgPriority2.getPkgID().intValue(), null, null, null, null, null));
                            }
                            readableDatabase.setTransactionSuccessful();
                            readableDatabase.endTransaction();
                        } catch (Exception e) {
                            Log.e(TAG, "query PkgProc, " + e);
                            readableDatabase.endTransaction();
                            readableDatabase.endTransaction();
                        } catch (Throwable th) {
                            readableDatabase.endTransaction();
                            throw th;
                        }
                        for (int size = arrayList2.size() - 1; size >= 0; size--) {
                            pkgName = ((PkgPriority) arrayList2.get(size)).getPkgName();
                            Cursor cursor = (Cursor) arrayMap.get(pkgName);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                for (int i = 0; i < cursor.getCount(); i++) {
                                    ProcessRecordStore processRecordStore = new ProcessRecordStore(cursor.getString(cursor.getColumnIndex(PackageProcessList.KEY_PROC_NAME)), cursor.getInt(cursor.getColumnIndex(PackageProcessList.KEY_UID)), pkgName, cursor.getLong(cursor.getColumnIndex(PackageProcessList.KEY_LAUNCH_MEM)));
                                    this.mManager.updateProcessNames(processRecordStore);
                                    this.mManager.updateLaunchProcList(pkgName, processRecordStore);
                                    cursor.moveToNext();
                                }
                            }
                            if (!(cursor == null || cursor.isClosed())) {
                                cursor.close();
                            }
                        }
                    } else {
                        Log.v(TAG, "Nothing to read from db");
                    }
                    if (!this.mPackagePriorityList.isEmpty()) {
                        Log.v(TAG, "Deleting Unmapped...");
                        SQLiteDatabase writableDatabase = getWritableDatabase();
                        String str = "pkg_id=?";
                        pkgName = "prim_key_id=?";
                        writableDatabase.beginTransaction();
                        try {
                            it = this.mPackagePriorityList.iterator();
                            while (it.hasNext()) {
                                String[] strArr2 = new String[]{String.valueOf(((PkgPriority) it.next()).getPkgID().intValue())};
                                writableDatabase.delete(PackageProcessList.TABLE_NAME, str, strArr2);
                                writableDatabase.delete(PackagePriorityList.TABLE_NAME, pkgName, strArr2);
                            }
                            writableDatabase.setTransactionSuccessful();
                            writableDatabase.endTransaction();
                        } catch (Exception e2) {
                            Log.e(TAG, "Delete Unmapped, " + e2);
                            writableDatabase.endTransaction();
                            writableDatabase.endTransaction();
                        } catch (Throwable th2) {
                            writableDatabase.endTransaction();
                            throw th2;
                        }
                    }
                    Log.v(TAG, "Nothing for db to delete");
                    close();
                    Log.v(TAG, "readDB done");
                    return;
                } else {
                    return;
                }
            }
            Log.v(TAG, "PM not ready, skip reading db");
            return;
        }
        Log.v(TAG, "not ready, skip reading db");
    }

    /* JADX WARNING: Removed duplicated region for block: B:130:0x03e2 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0118  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x0322 A:{SYNTHETIC, Splitter: B:115:0x0322} */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x036e A:{Catch:{ Exception -> 0x03ba, all -> 0x03dd }} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0118  */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x03e2 A:{RETURN} */
    /* JADX WARNING: Missing block: B:19:0x0092, code:
            r4 = r29.mPackagePriorityList.iterator();
     */
    /* JADX WARNING: Missing block: B:21:0x009e, code:
            if (r4.hasNext() != false) goto L_0x01bf;
     */
    /* JADX WARNING: Missing block: B:22:0x00a0, code:
            r3 = getWritableDatabase();
            r3.beginTransaction();
     */
    /* JADX WARNING: Missing block: B:24:?, code:
            r4 = r24.iterator();
     */
    /* JADX WARNING: Missing block: B:26:0x00af, code:
            if (r4.hasNext() != false) goto L_0x01d1;
     */
    /* JADX WARNING: Missing block: B:27:0x00b1, code:
            r4 = r23.iterator();
     */
    /* JADX WARNING: Missing block: B:29:0x00b9, code:
            if (r4.hasNext() != false) goto L_0x0231;
     */
    /* JADX WARNING: Missing block: B:30:0x00bb, code:
            r4 = r29.mPackagePriorityList.iterator();
     */
    /* JADX WARNING: Missing block: B:32:0x00c7, code:
            if (r4.hasNext() != false) goto L_0x0270;
     */
    /* JADX WARNING: Missing block: B:33:0x00c9, code:
            r3.setTransactionSuccessful();
     */
    /* JADX WARNING: Missing block: B:34:0x00cc, code:
            r3.endTransaction();
     */
    /* JADX WARNING: Missing block: B:85:0x01bf, code:
            r2 = (com.mediatek.appworkingset.AWSDBHelper.PkgPriority) r4.next();
            r2.setPriority(r3);
            r25.add(r2);
            r3 = r3 + 1;
     */
    /* JADX WARNING: Missing block: B:87:?, code:
            r2 = (com.mediatek.appworkingset.AWSDBHelper.PkgPriority) r4.next();
            r5 = new android.content.ContentValues();
            r6 = r2.getPkgName();
            r5.put(com.mediatek.appworkingset.AWSDBHelper.PackagePriorityList.KEY_PKG_NAME, r2.getPkgName());
            r5.put(com.mediatek.appworkingset.AWSDBHelper.PackagePriorityList.KEY_PRIORITY, java.lang.Integer.valueOf(r2.getPriority()));
            r2 = (int) r3.insertWithOnConflict(com.mediatek.appworkingset.AWSDBHelper.PackagePriorityList.TABLE_NAME, null, r5, 4);
     */
    /* JADX WARNING: Missing block: B:88:0x0203, code:
            if (r2 == -1) goto L_0x00ab;
     */
    /* JADX WARNING: Missing block: B:89:0x0205, code:
            r29.mPackageIDMap.put(r2, r6);
     */
    /* JADX WARNING: Missing block: B:91:0x020e, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:93:?, code:
            android.util.Log.e(TAG, "Update to PackagePriorityList, " + r2);
            r3.endTransaction();
     */
    /* JADX WARNING: Missing block: B:94:0x022c, code:
            r3.endTransaction();
     */
    /* JADX WARNING: Missing block: B:96:?, code:
            r2 = (com.mediatek.appworkingset.AWSDBHelper.PkgPriority) r4.next();
            r5 = new android.content.ContentValues();
            r5.put(com.mediatek.appworkingset.AWSDBHelper.PackagePriorityList.KEY_PRIORITY, java.lang.Integer.valueOf(r2.getPriority()));
            r3.update(com.mediatek.appworkingset.AWSDBHelper.PackagePriorityList.TABLE_NAME, r5, "prim_key_id=" + r2.getPkgID(), null);
     */
    /* JADX WARNING: Missing block: B:98:0x026b, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:99:0x026c, code:
            r3.endTransaction();
     */
    /* JADX WARNING: Missing block: B:100:0x026f, code:
            throw r2;
     */
    /* JADX WARNING: Missing block: B:102:?, code:
            r2 = (com.mediatek.appworkingset.AWSDBHelper.PkgPriority) r4.next();
            r5 = new android.content.ContentValues();
            r5.put(com.mediatek.appworkingset.AWSDBHelper.PackagePriorityList.KEY_PRIORITY, java.lang.Integer.valueOf(r2.getPriority()));
            r3.update(com.mediatek.appworkingset.AWSDBHelper.PackagePriorityList.TABLE_NAME, r5, "prim_key_id=" + r2.getPkgID(), null);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void updateDB() {
        ArrayList arrayList;
        ArrayList arrayList2;
        ArrayList arrayList3;
        ArrayList arrayList4;
        int i;
        int i2;
        int uid;
        if (!IsSystemAndReady("updateDB")) {
            Log.v(TAG, "not ready, skip update db");
            return;
        } else if (getPackagePriorityList() != -2) {
            arrayList = new ArrayList();
            arrayList2 = new ArrayList();
            arrayList3 = new ArrayList();
            ArrayList arrayList5 = new ArrayList();
            arrayList4 = new ArrayList();
            i = 0;
            synchronized (this.mManager.mPackagesProcessMap) {
                try {
                    PkgPriorityNode pkgPriorityNode = this.mManager.mPDirty;
                    PkgPriorityNode pkgPriorityNode2 = this.mManager.mPHead.next;
                    if (pkgPriorityNode2 != null) {
                        PkgPriorityNode pkgPriorityNode3 = pkgPriorityNode2;
                        int i3 = 1;
                        while (true) {
                            PkgPriority pkgPriority;
                            int i4;
                            String pkgName = pkgPriorityNode3.getPkgName();
                            PkgPriority pkgPriority2 = (PkgPriority) this.mPackagePriorityMap.get(pkgName);
                            if (pkgPriority2 != null) {
                                pkgPriority2.setPriority(i3);
                                arrayList3.add(pkgPriority2);
                                arrayList4.add(pkgPriority2);
                                this.mPackagePriorityList.remove(pkgPriority2);
                                pkgPriority = pkgPriority2;
                                i4 = 0;
                                i2 = i;
                            } else {
                                i2 = i + 1;
                                pkgPriority2 = new PkgPriority(null, pkgName, i3);
                                arrayList5.add(pkgPriority2);
                                arrayList4.add(pkgPriority2);
                                pkgPriority = pkgPriority2;
                                i4 = 1;
                            }
                            Iterator it = pkgPriorityNode3.procList.iterator();
                            while (it.hasNext()) {
                                ProcessRecordStore processRecordStore = (ProcessRecordStore) it.next();
                                String procName = processRecordStore.getProcName();
                                uid = processRecordStore.getUid();
                                long launchMem = processRecordStore.getLaunchMem(pkgName);
                                if (i4 != 1) {
                                    ArrayList arrayList6 = arrayList;
                                    arrayList6.add(new PkgProcess(Integer.valueOf(pkgPriority.getPkgID().intValue()), procName, uid, launchMem));
                                } else {
                                    arrayList2.add(new PkgProcess(procName, uid, launchMem, pkgName));
                                }
                            }
                            pkgPriorityNode2 = pkgPriorityNode3.next;
                            i = i3 + 1;
                            if (!(pkgPriorityNode2.next == null || pkgPriorityNode2.prev == pkgPriorityNode)) {
                                pkgPriorityNode3 = pkgPriorityNode2;
                                i3 = i;
                                i = i2;
                            }
                        }
                    } else {
                        Log.v(TAG, "No dirty, nothing to update");
                        return;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        } else {
            return;
        }
        SQLiteDatabase writableDatabase;
        Iterator it2;
        PkgProcess pkgProcess;
        int checkSizeOfPackageProcessList;
        writableDatabase = getWritableDatabase();
        writableDatabase.beginTransaction();
        try {
            ContentValues contentValues;
            it2 = arrayList2.iterator();
            while (it2.hasNext()) {
                pkgProcess = (PkgProcess) it2.next();
                contentValues = new ContentValues();
                contentValues.put(PackageProcessList.KEY_PKG_ID, pkgProcess.getPkgID());
                contentValues.put(PackageProcessList.KEY_PROC_NAME, pkgProcess.getProcName());
                contentValues.put(PackageProcessList.KEY_UID, Integer.valueOf(pkgProcess.getUID()));
                contentValues.put(PackageProcessList.KEY_LAUNCH_MEM, Long.valueOf(pkgProcess.getlaunchMem()));
                if (writableDatabase.insertWithOnConflict(PackageProcessList.TABLE_NAME, null, contentValues, 4) == -1) {
                }
            }
            it2 = arrayList.iterator();
            while (it2.hasNext()) {
                pkgProcess = (PkgProcess) it2.next();
                contentValues = new ContentValues();
                contentValues.put(PackageProcessList.KEY_PKG_ID, pkgProcess.getPkgID());
                contentValues.put(PackageProcessList.KEY_PROC_NAME, pkgProcess.getProcName());
                contentValues.put(PackageProcessList.KEY_UID, Integer.valueOf(pkgProcess.getUID()));
                contentValues.put(PackageProcessList.KEY_LAUNCH_MEM, Long.valueOf(pkgProcess.getlaunchMem()));
                if (writableDatabase.insertWithOnConflict(PackageProcessList.TABLE_NAME, null, contentValues, 4) == -1) {
                }
            }
            writableDatabase.setTransactionSuccessful();
            writableDatabase.endTransaction();
        } catch (Exception e) {
            Log.e(TAG, "Insert All new ones PackageProcessList, " + e);
            writableDatabase.endTransaction();
            writableDatabase.endTransaction();
        } catch (Throwable th2) {
            writableDatabase.endTransaction();
            throw th2;
        }
        checkSizeOfPackageProcessList = checkSizeOfPackageProcessList();
        if (checkSizeOfPackageProcessList != -2) {
            i = checkSizeOfPackagePriorityList();
            if (i != -2) {
                if (checkSizeOfPackageProcessList == -1 || i == -1) {
                    uid = (this.mNumPackagePriorityList + i2) - 100;
                    if (uid > 0) {
                        String str = "pkg_id=?";
                        String str2 = "prim_key_id=?";
                        writableDatabase.beginTransaction();
                        try {
                            int size = arrayList4.size();
                            checkSizeOfPackageProcessList = size - 1;
                            while (true) {
                                i = checkSizeOfPackageProcessList;
                                if (size - i > uid) {
                                    writableDatabase.setTransactionSuccessful();
                                    writableDatabase.endTransaction();
                                    break;
                                }
                                String[] strArr = new String[]{String.valueOf(((PkgPriority) arrayList4.get(i)).getPkgID())};
                                writableDatabase.delete(PackageProcessList.TABLE_NAME, str, strArr);
                                writableDatabase.delete(PackagePriorityList.TABLE_NAME, str2, strArr);
                                checkSizeOfPackageProcessList = i - 1;
                            }
                        } catch (Exception e2) {
                            Log.e(TAG, "Delete exceed for ProcPriorityList, numToDelete= " + uid + ", newlyAdd= " + i2 + ", mNumPackagePriorityList= " + this.mNumPackagePriorityList + ", MAX_PKG_PRIORITY_LIST= " + 100 + e2);
                            writableDatabase.endTransaction();
                            writableDatabase.endTransaction();
                        } catch (Throwable th22) {
                            writableDatabase.endTransaction();
                            throw th22;
                        }
                    }
                }
                close();
                Log.v(TAG, "DB updated done");
                return;
            }
            return;
        }
        return;
        checkSizeOfPackageProcessList = checkSizeOfPackageProcessList();
        if (checkSizeOfPackageProcessList != -2) {
        }
        close();
        Log.v(TAG, "DB updated done");
        return;
        it2 = arrayList2.iterator();
        while (it2.hasNext()) {
            pkgProcess = (PkgProcess) it2.next();
            pkgProcess.setPkgID(Integer.valueOf(this.mPackageIDMap.keyAt(this.mPackageIDMap.indexOfValue(pkgProcess.getdummyPkgName()))));
        }
        SQLiteDatabase writableDatabase2 = getWritableDatabase();
        writableDatabase2.beginTransaction();
        try {
            Iterator it3 = arrayList3.iterator();
            while (it3.hasNext()) {
                String str3 = (String) this.mPackageIDMap.get(((PkgPriority) it3.next()).getPkgID().intValue());
                String[] strArr2 = new String[]{String.valueOf(((PkgPriority) it3.next()).getPkgID().intValue())};
                writableDatabase2.delete(PackageProcessList.TABLE_NAME, "pkg_id=?", strArr2);
            }
            writableDatabase2.setTransactionSuccessful();
            writableDatabase2.endTransaction();
        } catch (Exception e22) {
            Log.e(TAG, "Delete exceed for PackageProcessList, " + e22);
            writableDatabase2.endTransaction();
            writableDatabase2.endTransaction();
        } catch (Throwable th222) {
            writableDatabase2.endTransaction();
            throw th222;
        }
        writableDatabase = getWritableDatabase();
        writableDatabase.beginTransaction();
        it2 = arrayList2.iterator();
        while (it2.hasNext()) {
        }
        it2 = arrayList.iterator();
        while (it2.hasNext()) {
        }
        writableDatabase.setTransactionSuccessful();
        writableDatabase.endTransaction();
        checkSizeOfPackageProcessList = checkSizeOfPackageProcessList();
        if (checkSizeOfPackageProcessList != -2) {
        }
    }

    private int checkSizeOfPackageProcessList() {
        int i;
        int i2 = 1;
        long queryNumEntries = DatabaseUtils.queryNumEntries(getReadableDatabase(), PackageProcessList.TABLE_NAME);
        if (queryNumEntries <= 2000) {
            i = 1;
        } else {
            i = 0;
        }
        if (i == 0) {
            Log.v(TAG, "DB exceed maximum, delete all:2000");
            deleteAllTable();
            return -2;
        }
        if (queryNumEntries > 1000) {
            i2 = 0;
        }
        if (i2 != 0) {
            return 0;
        }
        Log.v(TAG, "DB exceed maximum, delete one:1000");
        return -1;
    }

    private int checkSizeOfPackagePriorityList() {
        int i;
        int i2 = 1;
        long queryNumEntries = DatabaseUtils.queryNumEntries(getReadableDatabase(), PackagePriorityList.TABLE_NAME);
        this.mNumPackagePriorityList = (int) queryNumEntries;
        if (queryNumEntries <= 200) {
            i = 1;
        } else {
            i = 0;
        }
        if (i == 0) {
            Log.v(TAG, "DB exceed maximum, delete all:" + queryNumEntries + ">" + 200);
            deleteAllTable();
            return -2;
        }
        if (queryNumEntries > 100) {
            i2 = 0;
        }
        if (i2 != 0) {
            return 0;
        }
        Log.v(TAG, "DB exceed maximum, delete one:" + queryNumEntries + ">" + 100);
        return -1;
    }

    private int getPackagePriorityList() {
        if (this.mPackagePriorityMap != null) {
            this.mPackagePriorityMap.clear();
        } else {
            this.mPackagePriorityMap = new ArrayMap();
        }
        if (this.mPackagePriorityList != null) {
            this.mPackagePriorityList.clear();
        } else {
            this.mPackagePriorityList = new ArrayList();
        }
        if (this.mPackageIDMap != null) {
            this.mPackageIDMap.clear();
        } else {
            this.mPackageIDMap = new SparseArray();
        }
        Cursor query = getReadableDatabase().query(PackagePriorityList.TABLE_NAME, new String[]{"prim_key_id", PackagePriorityList.KEY_PKG_NAME, PackagePriorityList.KEY_PRIORITY}, null, null, null, null, PackagePriorityList.KEY_PRIORITY);
        int count = query.getCount();
        this.mNumPackagePriorityList = count;
        if (count <= 200) {
            query.moveToFirst();
            for (int i = 0; i < count; i++) {
                int i2 = query.getInt(query.getColumnIndex("prim_key_id"));
                String string = query.getString(query.getColumnIndex(PackagePriorityList.KEY_PKG_NAME));
                int i3 = query.getInt(query.getColumnIndex(PackagePriorityList.KEY_PRIORITY));
                Log.v(TAG, "Reading :ID,pkgName,priority=" + i2 + "," + string + "," + i3);
                PkgPriority pkgPriority = new PkgPriority(Integer.valueOf(i2), string, i3);
                this.mPackagePriorityMap.put(string, pkgPriority);
                this.mPackageIDMap.put(i2, string);
                this.mPackagePriorityList.add(pkgPriority);
                query.moveToNext();
            }
            query.close();
            if (this.mNumPackagePriorityList <= 100) {
                return this.mNumPackagePriorityList != 0 ? 0 : 1;
            } else {
                return -1;
            }
        }
        Log.v(TAG, "DB exceed maximum, delete all:200");
        deleteAllTable();
        query.close();
        return -2;
    }

    private void deleteAllTable() {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.beginTransaction();
        try {
            writableDatabase.delete(PackageProcessList.TABLE_NAME, null, null);
            writableDatabase.delete(PackagePriorityList.TABLE_NAME, null, null);
            writableDatabase.setTransactionSuccessful();
            writableDatabase.endTransaction();
        } catch (Exception e) {
            Log.e(TAG, "deleteAllTable, " + e);
            writableDatabase.endTransaction();
            writableDatabase.endTransaction();
        } catch (Throwable th) {
            writableDatabase.endTransaction();
            throw th;
        }
    }
}
