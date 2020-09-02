package com.android.server.connectivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkUtils;
import android.net.shared.PrivateDnsConfig;
import android.provider.Settings;
import android.util.Slog;
import com.android.server.connectivity.DnsManager;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OppoDnsManagerHelper {
    public static final String PRIVATE_DNS_VALIDATING_STATUS = "private_dns_validating_status";
    public static final int PRIVATE_DNS_VALIDATING_STATUS_FAILURE = 0;
    public static final int PRIVATE_DNS_VALIDATING_STATUS_IN_PROGRESS = 1;
    public static final int PRIVATE_DNS_VALIDATING_STATUS_SUCCESS = 2;
    private static final String TAG = "OppoDnsManager";
    private ConnectivityManager mCm;
    private final Context mContext;
    private String mCurrentHost = "";

    public OppoDnsManagerHelper(Context ctx) {
        this.mContext = ctx;
    }

    private void checkAndSetConnectivityInstance() {
        if (this.mCm == null) {
            this.mCm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
    }

    public void updatePrivateDnsValidation(DnsManager.PrivateDnsValidationUpdate update, boolean hasValidatedServer) {
        if (update.hostname.equals(this.mCurrentHost)) {
            checkAndSetConnectivityInstance();
            ConnectivityManager connectivityManager = this.mCm;
            if (connectivityManager != null) {
                Network activeNetwork = connectivityManager.getActiveNetwork();
                if (activeNetwork == null || activeNetwork.netId != update.netId) {
                    Slog.w(TAG, "update.netId(" + update.netId + ") is no active network ignore update private dns settings status");
                } else if (hasValidatedServer) {
                    Slog.w(TAG, "update private dns status = SUCCESS");
                    setIntSetting(PRIVATE_DNS_VALIDATING_STATUS, 2);
                } else {
                    Slog.w(TAG, "update private dns status = FAILURE");
                    setIntSetting(PRIVATE_DNS_VALIDATING_STATUS, 0);
                }
            }
        }
    }

    public void updatePrivateDnsValidatingStatus(PrivateDnsConfig privateDnsCfg, LinkProperties lp, boolean hasValidatedServer) {
        String tlsHostname = privateDnsCfg.hostname;
        if ((NetworkUtils.makeStrings((Collection) Arrays.stream(privateDnsCfg.ips).filter(new Predicate(lp) {
            /* class com.android.server.connectivity.$$Lambda$OppoDnsManagerHelper$m8bRMov7wkNRtAaKwaphUrG1DAE */
            private final /* synthetic */ LinkProperties f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return this.f$0.isReachable((InetAddress) obj);
            }
        }).collect(Collectors.toList())).length == 0 && tlsHostname.length() != 0) || !hasValidatedServer) {
            Slog.w(TAG, "update private dns status = IN_PROGRESS");
            setIntSetting(PRIVATE_DNS_VALIDATING_STATUS, 1);
            this.mCurrentHost = tlsHostname;
        } else if (hasValidatedServer) {
            Slog.w(TAG, "maybe network have been validated,just set status PRIVATE_DNS_VALIDATING_STATUS_SUCCESS");
            setIntSetting(PRIVATE_DNS_VALIDATING_STATUS, 2);
        }
    }

    public void updatePrivateDnsFailureStatus() {
        setIntSetting(PRIVATE_DNS_VALIDATING_STATUS, 0);
    }

    private boolean setIntSetting(String which, int dflt) {
        return Settings.Global.putInt(this.mContext.getContentResolver(), which, dflt);
    }
}
