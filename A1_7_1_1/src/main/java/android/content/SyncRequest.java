package android.content;

import android.accounts.Account;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

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
public class SyncRequest implements Parcelable {
    public static final Creator<SyncRequest> CREATOR = null;
    private static final String TAG = "SyncRequest";
    private final Account mAccountToSync;
    private final String mAuthority;
    private final boolean mDisallowMetered;
    private final Bundle mExtras;
    private final boolean mIsAuthority;
    private final boolean mIsExpedited;
    private final boolean mIsPeriodic;
    private final long mSyncFlexTimeSecs;
    private final long mSyncRunTimeSecs;

    public static class Builder {
        private static final int SYNC_TARGET_ADAPTER = 2;
        private static final int SYNC_TARGET_UNKNOWN = 0;
        private static final int SYNC_TYPE_ONCE = 2;
        private static final int SYNC_TYPE_PERIODIC = 1;
        private static final int SYNC_TYPE_UNKNOWN = 0;
        private Account mAccount;
        private String mAuthority;
        private Bundle mCustomExtras;
        private boolean mDisallowMetered;
        private boolean mExpedited;
        private boolean mIgnoreBackoff;
        private boolean mIgnoreSettings;
        private boolean mIsManual;
        private boolean mNoRetry;
        private boolean mRequiresCharging;
        private Bundle mSyncConfigExtras;
        private long mSyncFlexTimeSecs;
        private long mSyncRunTimeSecs;
        private int mSyncTarget = 0;
        private int mSyncType = 0;

        public Builder syncOnce() {
            if (this.mSyncType != 0) {
                throw new IllegalArgumentException("Sync type has already been defined.");
            }
            this.mSyncType = 2;
            setupInterval(0, 0);
            return this;
        }

        public Builder syncPeriodic(long pollFrequency, long beforeSeconds) {
            if (this.mSyncType != 0) {
                throw new IllegalArgumentException("Sync type has already been defined.");
            }
            this.mSyncType = 1;
            setupInterval(pollFrequency, beforeSeconds);
            return this;
        }

        private void setupInterval(long at, long before) {
            if (before > at) {
                throw new IllegalArgumentException("Specified run time for the sync must be after the specified flex time.");
            }
            this.mSyncRunTimeSecs = at;
            this.mSyncFlexTimeSecs = before;
        }

        public Builder setDisallowMetered(boolean disallow) {
            if (this.mIgnoreSettings && disallow) {
                throw new IllegalArgumentException("setDisallowMetered(true) after having specified that settings are ignored.");
            }
            this.mDisallowMetered = disallow;
            return this;
        }

        public Builder setRequiresCharging(boolean requiresCharging) {
            this.mRequiresCharging = true;
            return this;
        }

        public Builder setSyncAdapter(Account account, String authority) {
            if (this.mSyncTarget != 0) {
                throw new IllegalArgumentException("Sync target has already been defined.");
            } else if (authority == null || authority.length() != 0) {
                this.mSyncTarget = 2;
                this.mAccount = account;
                this.mAuthority = authority;
                return this;
            } else {
                throw new IllegalArgumentException("Authority must be non-empty");
            }
        }

        public Builder setExtras(Bundle bundle) {
            this.mCustomExtras = bundle;
            return this;
        }

        public Builder setNoRetry(boolean noRetry) {
            this.mNoRetry = noRetry;
            return this;
        }

        public Builder setIgnoreSettings(boolean ignoreSettings) {
            if (this.mDisallowMetered && ignoreSettings) {
                throw new IllegalArgumentException("setIgnoreSettings(true) after having specified sync settings with this builder.");
            }
            this.mIgnoreSettings = ignoreSettings;
            return this;
        }

        public Builder setIgnoreBackoff(boolean ignoreBackoff) {
            this.mIgnoreBackoff = ignoreBackoff;
            return this;
        }

        public Builder setManual(boolean isManual) {
            this.mIsManual = isManual;
            return this;
        }

        public Builder setExpedited(boolean expedited) {
            this.mExpedited = expedited;
            return this;
        }

        public SyncRequest build() {
            ContentResolver.validateSyncExtrasBundle(this.mCustomExtras);
            if (this.mCustomExtras == null) {
                this.mCustomExtras = new Bundle();
            }
            this.mSyncConfigExtras = new Bundle();
            if (this.mIgnoreBackoff) {
                this.mSyncConfigExtras.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_BACKOFF, true);
            }
            if (this.mDisallowMetered) {
                this.mSyncConfigExtras.putBoolean("allow_metered", true);
            }
            if (this.mRequiresCharging) {
                this.mSyncConfigExtras.putBoolean(ContentResolver.SYNC_EXTRAS_REQUIRE_CHARGING, true);
            }
            if (this.mIgnoreSettings) {
                this.mSyncConfigExtras.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS, true);
            }
            if (this.mNoRetry) {
                this.mSyncConfigExtras.putBoolean(ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY, true);
            }
            if (this.mExpedited) {
                this.mSyncConfigExtras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            }
            if (this.mIsManual) {
                this.mSyncConfigExtras.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_BACKOFF, true);
                this.mSyncConfigExtras.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS, true);
            }
            if (this.mSyncType == 1 && (ContentResolver.invalidPeriodicExtras(this.mCustomExtras) || ContentResolver.invalidPeriodicExtras(this.mSyncConfigExtras))) {
                throw new IllegalArgumentException("Illegal extras were set");
            } else if (this.mSyncTarget != 0) {
                return new SyncRequest(this);
            } else {
                throw new IllegalArgumentException("Must specify an adapter with setSyncAdapter(Account, String");
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.content.SyncRequest.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.content.SyncRequest.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.SyncRequest.<clinit>():void");
    }

    /* synthetic */ SyncRequest(Parcel in, SyncRequest syncRequest) {
        this(in);
    }

    public boolean isPeriodic() {
        return this.mIsPeriodic;
    }

    public boolean isExpedited() {
        return this.mIsExpedited;
    }

    public Account getAccount() {
        return this.mAccountToSync;
    }

    public String getProvider() {
        return this.mAuthority;
    }

    public Bundle getBundle() {
        return this.mExtras;
    }

    public long getSyncFlexTime() {
        return this.mSyncFlexTimeSecs;
    }

    public long getSyncRunTime() {
        return this.mSyncRunTimeSecs;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        int i;
        int i2 = 1;
        parcel.writeBundle(this.mExtras);
        parcel.writeLong(this.mSyncFlexTimeSecs);
        parcel.writeLong(this.mSyncRunTimeSecs);
        if (this.mIsPeriodic) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.mDisallowMetered) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.mIsAuthority) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (!this.mIsExpedited) {
            i2 = 0;
        }
        parcel.writeInt(i2);
        parcel.writeParcelable(this.mAccountToSync, flags);
        parcel.writeString(this.mAuthority);
    }

    private SyncRequest(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.mExtras = Bundle.setDefusable(in.readBundle(), true);
        this.mSyncFlexTimeSecs = in.readLong();
        this.mSyncRunTimeSecs = in.readLong();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mIsPeriodic = z;
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mDisallowMetered = z;
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mIsAuthority = z;
        if (in.readInt() == 0) {
            z2 = false;
        }
        this.mIsExpedited = z2;
        this.mAccountToSync = (Account) in.readParcelable(null);
        this.mAuthority = in.readString();
    }

    protected SyncRequest(Builder b) {
        boolean z;
        boolean z2 = true;
        this.mSyncFlexTimeSecs = b.mSyncFlexTimeSecs;
        this.mSyncRunTimeSecs = b.mSyncRunTimeSecs;
        this.mAccountToSync = b.mAccount;
        this.mAuthority = b.mAuthority;
        if (b.mSyncType == 1) {
            z = true;
        } else {
            z = false;
        }
        this.mIsPeriodic = z;
        if (b.mSyncTarget != 2) {
            z2 = false;
        }
        this.mIsAuthority = z2;
        this.mIsExpedited = b.mExpedited;
        this.mExtras = new Bundle(b.mCustomExtras);
        this.mExtras.putAll(b.mSyncConfigExtras);
        this.mDisallowMetered = b.mDisallowMetered;
    }
}
