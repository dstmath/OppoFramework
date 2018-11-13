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
import android.text.TextUtils;
import com.android.internal.R;
import com.android.internal.telephony.PhoneConstants;
import java.net.InetAddress;

public class NtpTrustedTime implements TrustedTime {
    private static final boolean LOGD = true;
    private static final String TAG = "NtpTrustedTime";
    private static Context sContext;
    private static NtpTrustedTime sSingleton;
    private ConnectivityManager mCM;
    private long mCachedNtpCertainty;
    private long mCachedNtpElapsedRealtime;
    private long mCachedNtpTime;
    private boolean mHasCache;
    private String mServer;
    private final long mTimeout;
    private String[] oppoNTPserver = new String[]{PhoneConstants.MVNO_TYPE_NONE, "cn.pool.ntp.org"};

    private NtpTrustedTime(String server, long timeout) {
        Log.d(TAG, "creating NtpTrustedTime using " + server);
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
                String secureServer = Global.getString(resolver, "ntp_server");
                sSingleton = new NtpTrustedTime(secureServer != null ? secureServer : defaultServer, Global.getLong(resolver, "ntp_timeout", defaultTimeout));
                sContext = context;
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
        NetworkInfo ni = null;
        if (TextUtils.isEmpty(this.mServer)) {
            return false;
        }
        if (isAutomaticTimeRequested()) {
            synchronized (this) {
                if (this.mCM == null) {
                    this.mCM = (ConnectivityManager) sContext.getSystemService("connectivity");
                }
            }
            if (this.mCM != null) {
                ni = this.mCM.getActiveNetworkInfo();
            }
            if (ni == null || !ni.isConnected()) {
                Log.d(TAG, "forceRefresh: no connectivity");
                return false;
            }
            Log.d(TAG, "forceRefresh() from cache miss");
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
            for (int i = 0; i < this.oppoNTPserver.length; i++) {
                if ("1".equals(SystemProperties.get("sys.ntp.exception", "0"))) {
                    SystemClock.sleep(this.mTimeout);
                } else if (client.requestTime(this.oppoNTPserver[i], (int) this.mTimeout)) {
                    Log.d(TAG, "mServer = " + this.oppoNTPserver[i]);
                    this.mHasCache = true;
                    this.mCachedNtpTime = client.getNtpTime();
                    this.mCachedNtpElapsedRealtime = client.getNtpTimeReference();
                    this.mCachedNtpCertainty = client.getRoundTripTime() / 2;
                    return true;
                }
            }
            return false;
        }
        Log.d(TAG, "Settings.Global.AUTO_TIME = 0");
        return false;
    }

    public void setServer(String server) {
        Log.d(TAG, "setServer:[" + server + "]");
        if (server != null) {
            this.mServer = server;
        }
    }

    public String getServer() {
        Log.d(TAG, "getServer:[" + this.mServer + "]");
        return this.mServer;
    }

    public boolean hasCache() {
        return this.mHasCache;
    }

    public long getCacheAge() {
        if (this.mHasCache) {
            return SystemClock.elapsedRealtime() - this.mCachedNtpElapsedRealtime;
        }
        return Long.MAX_VALUE;
    }

    public long getCacheCertainty() {
        if (this.mHasCache) {
            return this.mCachedNtpCertainty;
        }
        return Long.MAX_VALUE;
    }

    public long currentTimeMillis() {
        if (this.mHasCache) {
            Log.d(TAG, "currentTimeMillis() cache hit");
            return this.mCachedNtpTime + getCacheAge();
        }
        throw new IllegalStateException("Missing authoritative time source");
    }

    public long getCachedNtpTime() {
        Log.d(TAG, "getCachedNtpTime() cache hit");
        return this.mCachedNtpTime;
    }

    public long getCachedNtpTimeReference() {
        return this.mCachedNtpElapsedRealtime;
    }
}
