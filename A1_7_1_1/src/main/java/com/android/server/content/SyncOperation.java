package com.android.server.content;

import android.accounts.Account;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.am.OppoProcessManager;
import com.android.server.content.SyncStorageEngine.EndPoint;
import com.android.server.policy.PhoneWindowManager;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SyncOperation {
    public static final int NO_JOB_ID = -1;
    public static final int REASON_ACCOUNTS_UPDATED = -2;
    public static final int REASON_BACKGROUND_DATA_SETTINGS_CHANGED = -1;
    public static final int REASON_IS_SYNCABLE = -5;
    public static final int REASON_MASTER_SYNC_AUTO = -7;
    private static String[] REASON_NAMES = null;
    public static final int REASON_PERIODIC = -4;
    public static final int REASON_SERVICE_CHANGED = -3;
    public static final int REASON_SYNC_AUTO = -6;
    public static final int REASON_USER_START = -8;
    public static final String TAG = "SyncManager";
    public final boolean allowParallelSyncs;
    public long expectedRuntime;
    public final Bundle extras;
    public final long flexMillis;
    public final boolean isPeriodic;
    public int jobId;
    public final String key;
    public final String owningPackage;
    public final int owningUid;
    public final long periodMillis;
    public final int reason;
    int retries;
    public final int sourcePeriodicId;
    public final int syncSource;
    public final EndPoint target;
    public String wakeLockName;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.content.SyncOperation.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.content.SyncOperation.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.content.SyncOperation.<clinit>():void");
    }

    public SyncOperation(Account account, int userId, int owningUid, String owningPackage, int reason, int source, String provider, Bundle extras, boolean allowParallelSyncs) {
        this(new EndPoint(account, provider, userId), owningUid, owningPackage, reason, source, extras, allowParallelSyncs);
    }

    private SyncOperation(EndPoint info, int owningUid, String owningPackage, int reason, int source, Bundle extras, boolean allowParallelSyncs) {
        this(info, owningUid, owningPackage, reason, source, extras, allowParallelSyncs, false, -1, 0, 0);
    }

    public SyncOperation(SyncOperation op, long periodMillis, long flexMillis) {
        this(op.target, op.owningUid, op.owningPackage, op.reason, op.syncSource, new Bundle(op.extras), op.allowParallelSyncs, op.isPeriodic, op.sourcePeriodicId, periodMillis, flexMillis);
    }

    public SyncOperation(EndPoint info, int owningUid, String owningPackage, int reason, int source, Bundle extras, boolean allowParallelSyncs, boolean isPeriodic, int sourcePeriodicId, long periodMillis, long flexMillis) {
        this.target = info;
        this.owningUid = owningUid;
        this.owningPackage = owningPackage;
        this.reason = reason;
        this.syncSource = source;
        this.extras = new Bundle(extras);
        this.allowParallelSyncs = allowParallelSyncs;
        this.isPeriodic = isPeriodic;
        this.sourcePeriodicId = sourcePeriodicId;
        this.periodMillis = periodMillis;
        this.flexMillis = flexMillis;
        this.jobId = -1;
        this.key = toKey();
    }

    public SyncOperation createOneTimeSyncOperation() {
        if (this.isPeriodic) {
            return new SyncOperation(this.target, this.owningUid, this.owningPackage, this.reason, this.syncSource, new Bundle(this.extras), this.allowParallelSyncs, false, this.jobId, this.periodMillis, this.flexMillis);
        }
        return null;
    }

    public SyncOperation(SyncOperation other) {
        this.target = other.target;
        this.owningUid = other.owningUid;
        this.owningPackage = other.owningPackage;
        this.reason = other.reason;
        this.syncSource = other.syncSource;
        this.allowParallelSyncs = other.allowParallelSyncs;
        this.extras = new Bundle(other.extras);
        this.wakeLockName = other.wakeLockName();
        this.isPeriodic = other.isPeriodic;
        this.sourcePeriodicId = other.sourcePeriodicId;
        this.periodMillis = other.periodMillis;
        this.flexMillis = other.flexMillis;
        this.key = other.key;
    }

    PersistableBundle toJobInfoExtras() {
        PersistableBundle jobInfoExtras = new PersistableBundle();
        PersistableBundle syncExtrasBundle = new PersistableBundle();
        for (String key : this.extras.keySet()) {
            Account value = this.extras.get(key);
            if (value instanceof Account) {
                Account account = value;
                PersistableBundle accountBundle = new PersistableBundle();
                accountBundle.putString("accountName", account.name);
                accountBundle.putString("accountType", account.type);
                jobInfoExtras.putPersistableBundle("ACCOUNT:" + key, accountBundle);
            } else if (value instanceof Long) {
                syncExtrasBundle.putLong(key, ((Long) value).longValue());
            } else if (value instanceof Integer) {
                syncExtrasBundle.putInt(key, ((Integer) value).intValue());
            } else if (value instanceof Boolean) {
                syncExtrasBundle.putBoolean(key, ((Boolean) value).booleanValue());
            } else if (value instanceof Float) {
                syncExtrasBundle.putDouble(key, (double) ((Float) value).floatValue());
            } else if (value instanceof Double) {
                syncExtrasBundle.putDouble(key, ((Double) value).doubleValue());
            } else if (value instanceof String) {
                syncExtrasBundle.putString(key, (String) value);
            } else if (value == null) {
                syncExtrasBundle.putString(key, null);
            } else {
                Slog.e(TAG, "Unknown extra type.");
            }
        }
        jobInfoExtras.putPersistableBundle("syncExtras", syncExtrasBundle);
        jobInfoExtras.putBoolean("SyncManagerJob", true);
        jobInfoExtras.putString(OppoProcessManager.RESUME_REASON_PROVIDER_STR, this.target.provider);
        jobInfoExtras.putString("accountName", this.target.account.name);
        jobInfoExtras.putString("accountType", this.target.account.type);
        jobInfoExtras.putInt("userId", this.target.userId);
        jobInfoExtras.putInt("owningUid", this.owningUid);
        jobInfoExtras.putString("owningPackage", this.owningPackage);
        jobInfoExtras.putInt(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, this.reason);
        jobInfoExtras.putInt("source", this.syncSource);
        jobInfoExtras.putBoolean("allowParallelSyncs", this.allowParallelSyncs);
        jobInfoExtras.putInt("jobId", this.jobId);
        jobInfoExtras.putBoolean("isPeriodic", this.isPeriodic);
        jobInfoExtras.putInt("sourcePeriodicId", this.sourcePeriodicId);
        jobInfoExtras.putLong("periodMillis", this.periodMillis);
        jobInfoExtras.putLong("flexMillis", this.flexMillis);
        jobInfoExtras.putLong("expectedRuntime", this.expectedRuntime);
        jobInfoExtras.putInt("retries", this.retries);
        return jobInfoExtras;
    }

    static SyncOperation maybeCreateFromJobExtras(PersistableBundle jobExtras) {
        if (!jobExtras.getBoolean("SyncManagerJob", false)) {
            return null;
        }
        String accountName = jobExtras.getString("accountName");
        String accountType = jobExtras.getString("accountType");
        String provider = jobExtras.getString(OppoProcessManager.RESUME_REASON_PROVIDER_STR);
        int userId = jobExtras.getInt("userId", Integer.MAX_VALUE);
        int owningUid = jobExtras.getInt("owningUid");
        String owningPackage = jobExtras.getString("owningPackage");
        int reason = jobExtras.getInt(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, Integer.MAX_VALUE);
        int source = jobExtras.getInt("source", Integer.MAX_VALUE);
        boolean allowParallelSyncs = jobExtras.getBoolean("allowParallelSyncs", false);
        boolean isPeriodic = jobExtras.getBoolean("isPeriodic", false);
        int initiatedBy = jobExtras.getInt("sourcePeriodicId", -1);
        long periodMillis = jobExtras.getLong("periodMillis");
        long flexMillis = jobExtras.getLong("flexMillis");
        Bundle extras = new Bundle();
        PersistableBundle syncExtras = jobExtras.getPersistableBundle("syncExtras");
        if (syncExtras != null) {
            extras.putAll(syncExtras);
        }
        for (String key : jobExtras.keySet()) {
            if (key != null && key.startsWith("ACCOUNT:")) {
                String newKey = key.substring(8);
                PersistableBundle accountsBundle = jobExtras.getPersistableBundle(key);
                extras.putParcelable(newKey, new Account(accountsBundle.getString("accountName"), accountsBundle.getString("accountType")));
            }
        }
        SyncOperation op = new SyncOperation(new EndPoint(new Account(accountName, accountType), provider, userId), owningUid, owningPackage, reason, source, extras, allowParallelSyncs, isPeriodic, initiatedBy, periodMillis, flexMillis);
        op.jobId = jobExtras.getInt("jobId");
        op.expectedRuntime = jobExtras.getLong("expectedRuntime");
        op.retries = jobExtras.getInt("retries");
        return op;
    }

    boolean isConflict(SyncOperation toRun) {
        EndPoint other = toRun.target;
        if (!this.target.account.type.equals(other.account.type) || !this.target.provider.equals(other.provider) || this.target.userId != other.userId) {
            return false;
        }
        if (this.allowParallelSyncs) {
            return this.target.account.name.equals(other.account.name);
        }
        return true;
    }

    boolean isReasonPeriodic() {
        return this.reason == -4;
    }

    boolean matchesPeriodicOperation(SyncOperation other) {
        if (this.target.matchesSpec(other.target) && SyncManager.syncExtrasEquals(this.extras, other.extras, true) && this.periodMillis == other.periodMillis) {
            return this.flexMillis == other.flexMillis;
        } else {
            return false;
        }
    }

    boolean isDerivedFromFailedPeriodicSync() {
        return this.sourcePeriodicId != -1;
    }

    int findPriority() {
        if (isInitialization()) {
            return 20;
        }
        if (isExpedited()) {
            return 10;
        }
        return 0;
    }

    private String toKey() {
        StringBuilder sb = new StringBuilder();
        sb.append("provider: ").append(this.target.provider);
        sb.append(" account {name=").append(this.target.account.name).append(", user=").append(this.target.userId).append(", type=").append(this.target.account.type).append("}");
        sb.append(" isPeriodic: ").append(this.isPeriodic);
        sb.append(" period: ").append(this.periodMillis);
        sb.append(" flex: ").append(this.flexMillis);
        sb.append(" extras: ");
        extrasToStringBuilder(this.extras, sb);
        return sb.toString();
    }

    public String toString() {
        return dump(null, true);
    }

    String dump(PackageManager pm, boolean useOneLine) {
        StringBuilder sb = new StringBuilder();
        sb.append("JobId: ").append(this.jobId).append(", ").append(this.target.account.name).append(" u").append(this.target.userId).append(" (").append(this.target.account.type).append(")").append(", ").append(this.target.provider).append(", ");
        sb.append(SyncStorageEngine.SOURCES[this.syncSource]);
        if (this.extras.getBoolean("expedited", false)) {
            sb.append(", EXPEDITED");
        }
        sb.append(", reason: ");
        sb.append(reasonToString(pm, this.reason));
        if (this.isPeriodic) {
            sb.append(", period: ").append(this.periodMillis).append(", flexMillis: ").append(this.flexMillis);
        }
        if (!useOneLine) {
            sb.append("\n    ");
            sb.append("owningUid=");
            UserHandle.formatUid(sb, this.owningUid);
            sb.append(" owningPackage=");
            sb.append(this.owningPackage);
        }
        if (!(useOneLine || this.extras.keySet().isEmpty())) {
            sb.append("\n    ");
            extrasToStringBuilder(this.extras, sb);
        }
        return sb.toString();
    }

    static String reasonToString(PackageManager pm, int reason) {
        if (reason < 0) {
            int index = (-reason) - 1;
            if (index >= REASON_NAMES.length) {
                return String.valueOf(reason);
            }
            return REASON_NAMES[index];
        } else if (pm == null) {
            return String.valueOf(reason);
        } else {
            String[] packages = pm.getPackagesForUid(reason);
            if (packages != null && packages.length == 1) {
                return packages[0];
            }
            String name = pm.getNameForUid(reason);
            if (name != null) {
                return name;
            }
            return String.valueOf(reason);
        }
    }

    boolean isInitialization() {
        return this.extras.getBoolean("initialize", false);
    }

    boolean isExpedited() {
        return this.extras.getBoolean("expedited", false);
    }

    boolean ignoreBackoff() {
        return this.extras.getBoolean("ignore_backoff", false);
    }

    boolean isNotAllowedOnMetered() {
        return this.extras.getBoolean("allow_metered", false);
    }

    boolean isManual() {
        return this.extras.getBoolean("force", false);
    }

    boolean isIgnoreSettings() {
        return this.extras.getBoolean("ignore_settings", false);
    }

    private static void extrasToStringBuilder(Bundle bundle, StringBuilder sb) {
        sb.append("[");
        for (String key : bundle.keySet()) {
            sb.append(key).append("=").append(bundle.get(key)).append(" ");
        }
        sb.append("]");
    }

    String wakeLockName() {
        if (this.wakeLockName != null) {
            return this.wakeLockName;
        }
        String str = this.target.provider + "/" + this.target.account.type + "/" + this.target.account.name;
        this.wakeLockName = str;
        return str;
    }

    public Object[] toEventLog(int event) {
        Object[] logArray = new Object[4];
        logArray[1] = Integer.valueOf(event);
        logArray[2] = Integer.valueOf(this.syncSource);
        logArray[0] = this.target.provider;
        logArray[3] = Integer.valueOf(this.target.account.name.hashCode());
        return logArray;
    }
}
