package android.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.OppoHttpClient;
import android.net.SntpClient;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.SubscriptionPlan;
import android.text.TextUtils;
import com.android.internal.R;
import java.net.InetAddress;

public class NtpTrustedTime implements TrustedTime {
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
    private String[] oppoNTPserver = new String[]{"", "cn.pool.ntp.org", ""};

    private NtpTrustedTime(String server, long timeout) {
        this.mServer = server;
        this.mTimeout = timeout;
        this.oppoNTPserver[0] = this.mServer;
    }

    public static synchronized NtpTrustedTime getInstance(Context context) {
        NtpTrustedTime ntpTrustedTime;
        synchronized (NtpTrustedTime.class) {
            if (sSingleton == null) {
                Resources res = context.getResources();
                ContentResolver resolver = context.getContentResolver();
                String defaultServer = res.getString(R.string.config_ntpServer);
                long defaultTimeout = (long) res.getInteger(R.integer.config_ntpTimeout);
                String secureServer = Global.getString(resolver, Global.NTP_SERVER);
                sSingleton = new NtpTrustedTime(secureServer != null ? secureServer : defaultServer, Global.getLong(resolver, Global.NTP_TIMEOUT, defaultTimeout));
                sContext = context;
                if (sSingleton != null) {
                    String backupServer = SystemProperties.get("persist.backup.ntpServer");
                    ntpTrustedTime = sSingleton;
                    mNtpRetriesMax = res.getInteger(R.integer.config_ntpRetry);
                    ntpTrustedTime = sSingleton;
                    if (mNtpRetriesMax <= 0 || backupServer == null || backupServer.length() == 0) {
                        ntpTrustedTime = sSingleton;
                        mNtpRetriesMax = 0;
                        ntpTrustedTime = sSingleton;
                        mBackupServer = "";
                    } else {
                        ntpTrustedTime = sSingleton;
                        mBackupServer = backupServer.trim().replace("\"", "");
                    }
                }
            }
            ntpTrustedTime = sSingleton;
        }
        return ntpTrustedTime;
    }

    private boolean isAutomaticTimeRequested() {
        if (Global.getInt(sContext.getContentResolver(), "auto_time", 0) != 0) {
            return true;
        }
        return false;
    }

    public boolean forceRefresh() {
        if (TextUtils.isEmpty(this.mServer) || !isAutomaticTimeRequested()) {
            return false;
        }
        synchronized (this) {
            if (this.mCM == null) {
                this.mCM = (ConnectivityManager) sContext.getSystemService("connectivity");
            }
        }
        NetworkInfo ni = this.mCM == null ? null : this.mCM.getActiveNetworkInfo();
        if (ni == null || (ni.isConnected() ^ 1) != 0) {
            return false;
        }
        String region = SystemProperties.get("persist.sys.oppo.region", "CN");
        if ("CN".equals(region) || "OC".equals(region)) {
            OppoHttpClient oppoHttpClient = new OppoHttpClient();
            if (oppoHttpClient.requestTime(sContext, 0, (int) this.mTimeout)) {
                Log.d(TAG, "Use oppo http server algin time success!");
                this.mHasCache = true;
                this.mCachedNtpTime = oppoHttpClient.getHttpTime();
                this.mCachedNtpElapsedRealtime = oppoHttpClient.getHttpTimeReference();
                this.mCachedNtpCertainty = oppoHttpClient.getRoundTripTime() / 2;
                return true;
            }
            InetAddress.clearDnsCache();
            if (oppoHttpClient.requestTime(sContext, 1, (int) this.mTimeout)) {
                Log.d(TAG, "Use oppo http server1 algin time success!");
                this.mHasCache = true;
                this.mCachedNtpTime = oppoHttpClient.getHttpTime();
                this.mCachedNtpElapsedRealtime = oppoHttpClient.getHttpTimeReference();
                this.mCachedNtpCertainty = oppoHttpClient.getRoundTripTime() / 2;
                return true;
            }
        }
        SntpClient client = new SntpClient();
        int size = this.oppoNTPserver.length;
        if (getBackupmode()) {
            setBackupmode(false);
            this.oppoNTPserver[2] = mBackupServer;
        } else {
            size = this.oppoNTPserver.length - 1;
        }
        for (int i = 0; i < size; i++) {
            if ("1".equals(SystemProperties.get("sys.ntp.exception", "0"))) {
                SystemClock.sleep(this.mTimeout);
            } else if (client.requestTime(this.oppoNTPserver[i], (int) this.mTimeout)) {
                Log.d(TAG, "mServer = " + this.oppoNTPserver[i]);
                this.mHasCache = true;
                this.mCachedNtpTime = client.getNtpTime();
                this.mCachedNtpElapsedRealtime = client.getNtpTimeReference();
                this.mCachedNtpCertainty = client.getRoundTripTime() / 2;
                return true;
            } else if (i == 2) {
                countInBackupmode();
            }
        }
        return false;
    }

    public boolean hasCache() {
        return this.mHasCache;
    }

    public long getCacheAge() {
        if (this.mHasCache) {
            return SystemClock.elapsedRealtime() - this.mCachedNtpElapsedRealtime;
        }
        return SubscriptionPlan.BYTES_UNLIMITED;
    }

    public long getCacheCertainty() {
        if (this.mHasCache) {
            return this.mCachedNtpCertainty;
        }
        return SubscriptionPlan.BYTES_UNLIMITED;
    }

    public long currentTimeMillis() {
        if (this.mHasCache) {
            return this.mCachedNtpTime + getCacheAge();
        }
        throw new IllegalStateException("Missing authoritative time source");
    }

    public long getCachedNtpTime() {
        return this.mCachedNtpTime;
    }

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
        if (mNtpRetriesMax <= 0 || mBackupServer == null || mBackupServer.length() == 0) {
            return false;
        }
        return true;
    }

    private void countInBackupmode() {
        if (isBackupSupported()) {
            mNtpRetries++;
            if (mNtpRetries == mNtpRetriesMax) {
                mNtpRetries = 0;
                setBackupmode(true);
            }
        }
    }
}
