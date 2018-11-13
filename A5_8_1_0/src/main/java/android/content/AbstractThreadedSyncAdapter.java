package android.content;

import android.accounts.Account;
import android.content.ISyncAdapter.Stub;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.Trace;
import android.util.Log;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractThreadedSyncAdapter {
    private static final boolean ENABLE_LOG;
    @Deprecated
    public static final int LOG_SYNC_DETAILS = 2743;
    private static final String TAG = "SyncAdapter";
    private boolean mAllowParallelSyncs;
    private final boolean mAutoInitialize;
    private final Context mContext;
    private final ISyncAdapterImpl mISyncAdapterImpl;
    private final AtomicInteger mNumSyncStarts;
    private final Object mSyncThreadLock;
    private final HashMap<Account, SyncThread> mSyncThreads;

    private class ISyncAdapterImpl extends Stub {
        /* synthetic */ ISyncAdapterImpl(AbstractThreadedSyncAdapter this$0, ISyncAdapterImpl -this1) {
            this();
        }

        private ISyncAdapterImpl() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:36:0x00a9 A:{Splitter: B:5:0x003f, ExcHandler: java.lang.RuntimeException (r10_0 'th' java.lang.Throwable)} */
        /* JADX WARNING: Missing block: B:25:0x0090, code:
            if (android.content.AbstractThreadedSyncAdapter.-get0() == false) goto L_0x009b;
     */
        /* JADX WARNING: Missing block: B:26:0x0092, code:
            android.util.Log.d(android.content.AbstractThreadedSyncAdapter.TAG, "startSync() finishing");
     */
        /* JADX WARNING: Missing block: B:27:0x009b, code:
            return;
     */
        /* JADX WARNING: Missing block: B:36:0x00a9, code:
            r10 = move-exception;
     */
        /* JADX WARNING: Missing block: B:39:0x00ae, code:
            if (android.content.AbstractThreadedSyncAdapter.-get0() != false) goto L_0x00b0;
     */
        /* JADX WARNING: Missing block: B:40:0x00b0, code:
            android.util.Log.d(android.content.AbstractThreadedSyncAdapter.TAG, "startSync() caught exception", r10);
     */
        /* JADX WARNING: Missing block: B:41:0x00b9, code:
            throw r10;
     */
        /* JADX WARNING: Missing block: B:44:0x00bf, code:
            if (android.content.AbstractThreadedSyncAdapter.-get0() != false) goto L_0x00c1;
     */
        /* JADX WARNING: Missing block: B:45:0x00c1, code:
            android.util.Log.d(android.content.AbstractThreadedSyncAdapter.TAG, "startSync() finishing");
     */
        /* JADX WARNING: Missing block: B:52:0x0104, code:
            if (r9 == false) goto L_0x010b;
     */
        /* JADX WARNING: Missing block: B:53:0x0106, code:
            r4.onFinished(android.content.SyncResult.ALREADY_IN_PROGRESS);
     */
        /* JADX WARNING: Missing block: B:55:0x010f, code:
            if (android.content.AbstractThreadedSyncAdapter.-get0() == false) goto L_0x011a;
     */
        /* JADX WARNING: Missing block: B:56:0x0111, code:
            android.util.Log.d(android.content.AbstractThreadedSyncAdapter.TAG, "startSync() finishing");
     */
        /* JADX WARNING: Missing block: B:57:0x011a, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void startSync(ISyncContext syncContext, String authority, Account account, Bundle extras) {
            if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                if (extras != null) {
                    extras.size();
                }
                Log.d(AbstractThreadedSyncAdapter.TAG, "startSync() start " + authority + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + account + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + extras);
            }
            try {
                SyncContext syncContextClient = new SyncContext(syncContext);
                Account threadsKey = AbstractThreadedSyncAdapter.this.toSyncKey(account);
                synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                    boolean alreadyInProgress;
                    if (AbstractThreadedSyncAdapter.this.mSyncThreads.containsKey(threadsKey)) {
                        if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                            Log.d(AbstractThreadedSyncAdapter.TAG, "  alreadyInProgress");
                        }
                        alreadyInProgress = true;
                    } else {
                        if (AbstractThreadedSyncAdapter.this.mAutoInitialize && extras != null) {
                            if (extras.getBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, false)) {
                                try {
                                    if (ContentResolver.getIsSyncable(account, authority) < 0) {
                                        ContentResolver.setIsSyncable(account, authority, 1);
                                    }
                                    syncContextClient.onFinished(new SyncResult());
                                } catch (Throwable th) {
                                    syncContextClient.onFinished(new SyncResult());
                                }
                            }
                        }
                        SyncThread syncThread = new SyncThread(AbstractThreadedSyncAdapter.this, "SyncAdapterThread-" + AbstractThreadedSyncAdapter.this.mNumSyncStarts.incrementAndGet(), syncContextClient, authority, account, extras, null);
                        AbstractThreadedSyncAdapter.this.mSyncThreads.put(threadsKey, syncThread);
                        syncThread.start();
                        alreadyInProgress = false;
                    }
                }
            } catch (Throwable th2) {
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:27:0x0087 A:{Splitter: B:1:0x0001, ExcHandler: java.lang.RuntimeException (r3_0 'th' java.lang.Throwable)} */
        /* JADX WARNING: Missing block: B:27:0x0087, code:
            r3 = move-exception;
     */
        /* JADX WARNING: Missing block: B:30:0x008c, code:
            if (android.content.AbstractThreadedSyncAdapter.-get0() != false) goto L_0x008e;
     */
        /* JADX WARNING: Missing block: B:31:0x008e, code:
            android.util.Log.d(android.content.AbstractThreadedSyncAdapter.TAG, "cancelSync() caught exception", r3);
     */
        /* JADX WARNING: Missing block: B:32:0x0097, code:
            throw r3;
     */
        /* JADX WARNING: Missing block: B:35:0x009d, code:
            if (android.content.AbstractThreadedSyncAdapter.-get0() != false) goto L_0x009f;
     */
        /* JADX WARNING: Missing block: B:36:0x009f, code:
            android.util.Log.d(android.content.AbstractThreadedSyncAdapter.TAG, "cancelSync() finishing");
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void cancelSync(ISyncContext syncContext) {
            Thread info = null;
            try {
                synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                    for (Thread current : AbstractThreadedSyncAdapter.this.mSyncThreads.values()) {
                        if (current.mSyncContext.getSyncContextBinder() == syncContext.asBinder()) {
                            info = current;
                            break;
                        }
                    }
                }
                if (info != null) {
                    if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                        Log.d(AbstractThreadedSyncAdapter.TAG, "cancelSync() " + info.mAuthority + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + info.mAccount);
                    }
                    if (AbstractThreadedSyncAdapter.this.mAllowParallelSyncs) {
                        AbstractThreadedSyncAdapter.this.onSyncCanceled(info);
                    } else {
                        AbstractThreadedSyncAdapter.this.onSyncCanceled();
                    }
                } else if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                    Log.w(AbstractThreadedSyncAdapter.TAG, "cancelSync() unknown context");
                }
                if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                    Log.d(AbstractThreadedSyncAdapter.TAG, "cancelSync() finishing");
                }
            } catch (Throwable th) {
            }
        }
    }

    private class SyncThread extends Thread {
        private final Account mAccount;
        private final String mAuthority;
        private final Bundle mExtras;
        private final SyncContext mSyncContext;
        private final Account mThreadsKey;

        /* synthetic */ SyncThread(AbstractThreadedSyncAdapter this$0, String name, SyncContext syncContext, String authority, Account account, Bundle extras, SyncThread -this6) {
            this(name, syncContext, authority, account, extras);
        }

        private SyncThread(String name, SyncContext syncContext, String authority, Account account, Bundle extras) {
            super(name);
            this.mSyncContext = syncContext;
            this.mAuthority = authority;
            this.mAccount = account;
            this.mExtras = extras;
            this.mThreadsKey = AbstractThreadedSyncAdapter.this.toSyncKey(account);
        }

        /* JADX WARNING: Removed duplicated region for block: B:76:0x0136 A:{Splitter: B:4:0x0021, ExcHandler: java.lang.RuntimeException (r7_0 'th' java.lang.Throwable), PHI: r4 } */
        /* JADX WARNING: Missing block: B:76:0x0136, code:
            r7 = move-exception;
     */
        /* JADX WARNING: Missing block: B:79:0x013b, code:
            if (android.content.AbstractThreadedSyncAdapter.-get0() != false) goto L_0x013d;
     */
        /* JADX WARNING: Missing block: B:80:0x013d, code:
            android.util.Log.d(android.content.AbstractThreadedSyncAdapter.TAG, "caught exception", r7);
     */
        /* JADX WARNING: Missing block: B:81:0x0146, code:
            throw r7;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            Process.setThreadPriority(10);
            if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                Log.d(AbstractThreadedSyncAdapter.TAG, "Thread started");
            }
            Trace.traceBegin(128, this.mAuthority);
            SyncResult syncResult = new SyncResult();
            ContentProviderClient contentProviderClient = null;
            try {
                if (isCanceled()) {
                    if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                        Log.d(AbstractThreadedSyncAdapter.TAG, "Already canceled");
                    }
                    Trace.traceEnd(128);
                    if (!isCanceled()) {
                        this.mSyncContext.onFinished(syncResult);
                    }
                    synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                        AbstractThreadedSyncAdapter.this.mSyncThreads.remove(this.mThreadsKey);
                    }
                    if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                        Log.d(AbstractThreadedSyncAdapter.TAG, "Thread finished");
                    }
                    return;
                }
                if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                    Log.d(AbstractThreadedSyncAdapter.TAG, "Calling onPerformSync...");
                }
                contentProviderClient = AbstractThreadedSyncAdapter.this.mContext.getContentResolver().acquireContentProviderClient(this.mAuthority);
                if (contentProviderClient != null) {
                    AbstractThreadedSyncAdapter.this.onPerformSync(this.mAccount, this.mExtras, this.mAuthority, contentProviderClient, syncResult);
                } else {
                    syncResult.databaseError = true;
                }
                if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                    Log.d(AbstractThreadedSyncAdapter.TAG, "onPerformSync done");
                }
                Trace.traceEnd(128);
                if (contentProviderClient != null) {
                    contentProviderClient.release();
                }
                if (!isCanceled()) {
                    this.mSyncContext.onFinished(syncResult);
                }
                synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                    AbstractThreadedSyncAdapter.this.mSyncThreads.remove(this.mThreadsKey);
                }
                if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                    Log.d(AbstractThreadedSyncAdapter.TAG, "Thread finished");
                }
            } catch (SecurityException e) {
                if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                    Log.d(AbstractThreadedSyncAdapter.TAG, "SecurityException", e);
                }
                AbstractThreadedSyncAdapter.this.onSecurityException(this.mAccount, this.mExtras, this.mAuthority, syncResult);
                syncResult.databaseError = true;
                Trace.traceEnd(128);
                if (contentProviderClient != null) {
                    contentProviderClient.release();
                }
                if (!isCanceled()) {
                    this.mSyncContext.onFinished(syncResult);
                }
                synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                    AbstractThreadedSyncAdapter.this.mSyncThreads.remove(this.mThreadsKey);
                    if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                        Log.d(AbstractThreadedSyncAdapter.TAG, "Thread finished");
                    }
                }
            } catch (Throwable th) {
            } catch (Throwable th2) {
                Trace.traceEnd(128);
                if (contentProviderClient != null) {
                    contentProviderClient.release();
                }
                if (!isCanceled()) {
                    this.mSyncContext.onFinished(syncResult);
                }
                synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                    AbstractThreadedSyncAdapter.this.mSyncThreads.remove(this.mThreadsKey);
                    if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                        Log.d(AbstractThreadedSyncAdapter.TAG, "Thread finished");
                    }
                }
            }
        }

        private boolean isCanceled() {
            return Thread.currentThread().isInterrupted();
        }
    }

    public abstract void onPerformSync(Account account, Bundle bundle, String str, ContentProviderClient contentProviderClient, SyncResult syncResult);

    static {
        boolean isLoggable;
        if (Build.IS_DEBUGGABLE) {
            isLoggable = Log.isLoggable(TAG, 3);
        } else {
            isLoggable = false;
        }
        ENABLE_LOG = isLoggable;
    }

    public AbstractThreadedSyncAdapter(Context context, boolean autoInitialize) {
        this(context, autoInitialize, false);
    }

    public AbstractThreadedSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        this.mSyncThreads = new HashMap();
        this.mSyncThreadLock = new Object();
        this.mContext = context;
        this.mISyncAdapterImpl = new ISyncAdapterImpl(this, null);
        this.mNumSyncStarts = new AtomicInteger(0);
        this.mAutoInitialize = autoInitialize;
        this.mAllowParallelSyncs = allowParallelSyncs;
    }

    public Context getContext() {
        return this.mContext;
    }

    private Account toSyncKey(Account account) {
        if (this.mAllowParallelSyncs) {
            return account;
        }
        return null;
    }

    public final IBinder getSyncAdapterBinder() {
        return this.mISyncAdapterImpl.asBinder();
    }

    public void onSecurityException(Account account, Bundle extras, String authority, SyncResult syncResult) {
    }

    public void onSyncCanceled() {
        SyncThread syncThread;
        synchronized (this.mSyncThreadLock) {
            syncThread = (SyncThread) this.mSyncThreads.get(null);
        }
        if (syncThread != null) {
            syncThread.interrupt();
        }
    }

    public void onSyncCanceled(Thread thread) {
        thread.interrupt();
    }
}
