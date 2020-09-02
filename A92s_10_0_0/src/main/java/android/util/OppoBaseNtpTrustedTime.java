package android.util;

import android.content.Context;
import android.net.Network;
import android.net.OppoHttpClient;
import android.net.SntpClient;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import java.net.InetAddress;

public abstract class OppoBaseNtpTrustedTime {
    private static final boolean IS_SUPPORT_OPPO_NTP_TRUSTED_TIME = false;
    private static final boolean LOGD = false;
    private static final String TAG = "OppoBaseNtpTrustedTime";
    private final long mLocalTimeout;
    protected String[] mOppoNTPserverArray = {"", "cn.pool.ntp.org", ""};

    /* access modifiers changed from: protected */
    public abstract Context getContext();

    /* access modifiers changed from: protected */
    public abstract void onCountInBackupmode();

    /* access modifiers changed from: protected */
    public abstract String updateBackupStatus();

    /* access modifiers changed from: protected */
    public abstract void updateCacheStatus(boolean z, long j, long j2, long j3);

    public OppoBaseNtpTrustedTime(String server, long timeout) {
        this.mOppoNTPserverArray[0] = server;
        this.mLocalTimeout = timeout;
    }

    /* access modifiers changed from: protected */
    public boolean isAutomaticTimeRequested() {
        return Settings.Global.getInt(getContext().getContentResolver(), "auto_time", 0) != 0;
    }

    /* access modifiers changed from: protected */
    public boolean foceRefreshForCnRegion() {
        String region = SystemProperties.get("persist.sys.oppo.region", "CN");
        if ("CN".equals(region) || "OC".equals(region)) {
            OppoHttpClient oppoHttpClient = new OppoHttpClient();
            if (oppoHttpClient.requestTime(getContext(), 0, (int) this.mLocalTimeout)) {
                Log.d(TAG, "Use oppo http server algin time success!");
                updateCacheStatus(true, oppoHttpClient.getHttpTime(), oppoHttpClient.getHttpTimeReference(), oppoHttpClient.getRoundTripTime() / 2);
                return true;
            }
            InetAddress.clearDnsCache();
            if (oppoHttpClient.requestTime(getContext(), 1, (int) this.mLocalTimeout)) {
                Log.d(TAG, "Use oppo http server1 algin time success!");
                updateCacheStatus(true, oppoHttpClient.getHttpTime(), oppoHttpClient.getHttpTimeReference(), oppoHttpClient.getRoundTripTime() / 2);
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean refreshTimeWithOppoNTP(Network network) {
        int size;
        SntpClient client = new SntpClient();
        int size2 = this.mOppoNTPserverArray.length;
        String backupServer = updateBackupStatus();
        if (backupServer != null) {
            this.mOppoNTPserverArray[2] = backupServer;
            size = size2;
        } else {
            size = this.mOppoNTPserverArray.length - 1;
        }
        for (int i = 0; i < size; i++) {
            if (WifiEnterpriseConfig.ENGINE_ENABLE.equals(SystemProperties.get("sys.ntp.exception", WifiEnterpriseConfig.ENGINE_DISABLE))) {
                SystemClock.sleep(this.mLocalTimeout);
            } else if (client.requestTime(this.mOppoNTPserverArray[i], (int) this.mLocalTimeout, network)) {
                updateCacheStatus(true, client.getNtpTime(), client.getNtpTimeReference(), client.getRoundTripTime() / 2);
                return true;
            } else if (i == 2) {
                onCountInBackupmode();
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isSupportOppoNtpTrustedTime() {
        return false;
    }
}
