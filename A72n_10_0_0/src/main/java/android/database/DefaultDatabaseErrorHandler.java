package android.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseConfiguration;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.util.Pair;
import java.io.File;
import java.util.List;

public final class DefaultDatabaseErrorHandler implements DatabaseErrorHandler {
    private static final String TAG = "DefaultDatabaseErrorHandler";

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x003f, code lost:
        if (r0 != null) goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0041, code lost:
        r2 = r0.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0049, code lost:
        if (r2.hasNext() != false) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004b, code lost:
        deleteDatabaseFile(r2.next().second);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005a, code lost:
        deleteDatabaseFile(r6.getPath());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0061, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0038, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0038 A[ExcHandler: all (r1v8 'th' java.lang.Throwable A[CUSTOM_DECLARE]), PHI: r0 
      PHI: (r0v6 'attachedDbs' java.util.List<android.util.Pair<java.lang.String, java.lang.String>>) = (r0v4 'attachedDbs' java.util.List<android.util.Pair<java.lang.String, java.lang.String>>), (r0v5 'attachedDbs' java.util.List<android.util.Pair<java.lang.String, java.lang.String>>), (r0v5 'attachedDbs' java.util.List<android.util.Pair<java.lang.String, java.lang.String>>), (r0v4 'attachedDbs' java.util.List<android.util.Pair<java.lang.String, java.lang.String>>) binds: [B:5:0x0032, B:10:0x003b, B:11:?, B:6:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:5:0x0032] */
    @Override // android.database.DatabaseErrorHandler
    public void onCorruption(SQLiteDatabase dbObj) {
        Log.e(TAG, "Corruption reported by sqlite on database: " + dbObj.getPath());
        SQLiteDatabase.wipeDetected(dbObj.getPath(), "corruption");
        if (!dbObj.isOpen()) {
            deleteDatabaseFile(dbObj.getPath());
            return;
        }
        List<Pair<String, String>> attachedDbs = null;
        try {
            attachedDbs = dbObj.getAttachedDbs();
            dbObj.close();
        } catch (SQLiteException e) {
        } catch (Throwable th) {
        }
        if (attachedDbs != null) {
            for (Pair<String, String> p : attachedDbs) {
                deleteDatabaseFile(p.second);
            }
            return;
        }
        deleteDatabaseFile(dbObj.getPath());
    }

    private void deleteDatabaseFile(String fileName) {
        if (!fileName.equalsIgnoreCase(SQLiteDatabaseConfiguration.MEMORY_DB_PATH) && fileName.trim().length() != 0) {
            Log.e(TAG, "deleting the database file: " + fileName);
            try {
                SQLiteDatabase.deleteDatabase(new File(fileName), false);
            } catch (Exception e) {
                Log.w(TAG, "delete failed: " + e.getMessage());
            }
        }
    }
}
