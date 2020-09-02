package android.util;

import android.annotation.UnsupportedAppUsage;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.SntpClient;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.internal.R;

public class NtpTrustedTime extends OppoBaseNtpTrustedTime implements TrustedTime {
    private static final String BACKUP_SERVER = "persist.backup.ntpServer";
    private static final boolean LOGD = false;
    private static final String TAG = "NtpTrustedTime";
    private static String mBackupServer = "";
    private static int mNtpRetries = 0;
    private static int mNtpRetriesMax = 0;
    private static Context sContext;
    private static NtpTrustedTime sSingleton;
    private boolean mBackupmode = false;
    private ConnectivityManager mCM;
    private long mCachedNtpCertainty;
    private long mCachedNtpElapsedRealtime;
    private long mCachedNtpTime;
    private boolean mHasCache;
    private final String mServer;
    private final long mTimeout;

    private NtpTrustedTime(String server, long timeout) {
        super(server, timeout);
        this.mServer = server;
        this.mTimeout = timeout;
    }

    @UnsupportedAppUsage
    public static synchronized NtpTrustedTime getInstance(Context context) {
        NtpTrustedTime ntpTrustedTime;
        synchronized (NtpTrustedTime.class) {
            if (sSingleton == null) {
                Resources res = context.getResources();
                ContentResolver resolver = context.getContentResolver();
                String defaultServer = res.getString(R.string.config_ntpServer);
                String secureServer = Settings.Global.getString(resolver, Settings.Global.NTP_SERVER);
                sSingleton = new NtpTrustedTime(secureServer != null ? secureServer : defaultServer, Settings.Global.getLong(resolver, Settings.Global.NTP_TIMEOUT, (long) res.getInteger(R.integer.config_ntpTimeout)));
                sContext = context;
            }
            ntpTrustedTime = sSingleton;
        }
        return ntpTrustedTime;
    }

    @Override // android.util.TrustedTime
    @UnsupportedAppUsage
    public boolean forceRefresh() {
        synchronized (this) {
            if (this.mCM == null) {
                this.mCM = (ConnectivityManager) sContext.getSystemService(ConnectivityManager.class);
            }
        }
        ConnectivityManager connectivityManager = this.mCM;
        return forceRefresh(connectivityManager == null ? null : connectivityManager.getActiveNetwork());
    }

    public boolean forceRefresh(Network network) {
        if (TextUtils.isEmpty(this.mServer)) {
            return false;
        }
        synchronized (this) {
            if (this.mCM == null) {
                this.mCM = (ConnectivityManager) sContext.getSystemService(ConnectivityManager.class);
            }
        }
        ConnectivityManager connectivityManager = this.mCM;
        NetworkInfo ni = connectivityManager == null ? null : connectivityManager.getNetworkInfo(network);
        if (ni == null || !ni.isConnected() || !isAutomaticTimeRequested()) {
            return false;
        }
        if (foceRefreshForCnRegion()) {
            return true;
        }
        if (isSupportOppoNtpTrustedTime()) {
            return refreshTimeWithOppoNTP(network);
        }
        SntpClient client = new SntpClient();
        String targetServer = this.mServer;
        if (getBackupmode()) {
            setBackupmode(false);
            targetServer = mBackupServer;
        }
        if (!client.requestTime(targetServer, (int) this.mTimeout, network)) {
            return false;
        }
        this.mHasCache = true;
        this.mCachedNtpTime = client.getNtpTime();
        this.mCachedNtpElapsedRealtime = client.getNtpTimeReference();
        this.mCachedNtpCertainty = client.getRoundTripTime() / 2;
        return true;
    }

    @Override // android.util.TrustedTime
    @UnsupportedAppUsage
    public boolean hasCache() {
        return this.mHasCache;
    }

    @Override // android.util.TrustedTime
    public long getCacheAge() {
        if (this.mHasCache) {
            return SystemClock.elapsedRealtime() - this.mCachedNtpElapsedRealtime;
        }
        return Long.MAX_VALUE;
    }

    @Override // android.util.TrustedTime
    public long getCacheCertainty() {
        if (this.mHasCache) {
            return this.mCachedNtpCertainty;
        }
        return Long.MAX_VALUE;
    }

    @Override // android.util.TrustedTime
    @UnsupportedAppUsage
    public long currentTimeMillis() {
        if (this.mHasCache) {
            return this.mCachedNtpTime + getCacheAge();
        }
        throw new IllegalStateException("Missing authoritative time source");
    }

    @UnsupportedAppUsage
    public long getCachedNtpTime() {
        return this.mCachedNtpTime;
    }

    @UnsupportedAppUsage
    public long getCachedNtpTimeReference() {
        return this.mCachedNtpElapsedRealtime;
    }

    public void setBackupmode(boolean mode) {
        if (isBackupSupported()) {
            this.mBackupmode = mode;
        }
    }

    private boolean getBackupmode() {
        return this.mBackupmode;
    }

    private boolean isBackupSupported() {
        String str;
        return (mNtpRetriesMax <= 0 || (str = mBackupServer) == null || str.length() == 0) ? false : true;
    }

    private void countInBackupmode() {
        if (isBackupSupported()) {
            mNtpRetries++;
            if (mNtpRetries >= mNtpRetriesMax) {
                mNtpRetries = 0;
                setBackupmode(true);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.util.OppoBaseNtpTrustedTime
    public void updateCacheStatus(boolean hasCache, long cacheNtpTime, long cacheNtpElapRealTim, long cacheNtpCertainty) {
        this.mHasCache = hasCache;
        this.mCachedNtpTime = cacheNtpTime;
        this.mCachedNtpElapsedRealtime = cacheNtpElapRealTim;
        this.mCachedNtpCertainty = cacheNtpCertainty;
    }

    /* access modifiers changed from: protected */
    @Override // android.util.OppoBaseNtpTrustedTime
    public Context getContext() {
        return sContext;
    }

    /* access modifiers changed from: protected */
    @Override // android.util.OppoBaseNtpTrustedTime
    public String updateBackupStatus() {
        if (true != getBackupmode()) {
            return null;
        }
        setBackupmode(false);
        return mBackupServer;
    }

    /* access modifiers changed from: protected */
    @Override // android.util.OppoBaseNtpTrustedTime
    public void onCountInBackupmode() {
        countInBackupmode();
    }
}
