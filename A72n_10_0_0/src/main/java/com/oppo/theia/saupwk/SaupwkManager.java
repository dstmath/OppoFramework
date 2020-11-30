package com.oppo.theia.saupwk;

import android.os.Handler;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.theia.NoFocusWindow;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaupwkManager {
    static final String TAG = "SAUPWK";
    private static SaupwkManager sInstance = null;
    Handler mHandler = new Handler();

    public static synchronized SaupwkManager getInstance() {
        SaupwkManager saupwkManager;
        synchronized (SaupwkManager.class) {
            if (sInstance == null) {
                sInstance = new SaupwkManager();
            }
            saupwkManager = sInstance;
        }
        return saupwkManager;
    }

    public void saupwkLogDumpTrigger() {
        if (SystemProperties.get("persist.sys.saupwk_en", "").equals(NoFocusWindow.HUNG_CONFIG_ENABLE)) {
            String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
            SystemProperties.set("sys.bootfinish.timestamp", date);
            Slog.w(TAG, "[SAUPWK]: marking sys.bootfinish.timestamp as " + date);
            this.mHandler.postDelayed(new Runnable() {
                /* class com.oppo.theia.saupwk.SaupwkManager.AnonymousClass1 */

                public void run() {
                    String reason = SystemProperties.get("persist.sys.rbsreason", "na");
                    SystemProperties.set("sys.sr.reboot_reason", reason);
                    SystemProperties.set("persist.sys.rbsreason", "");
                    String rk = SystemProperties.get("persist.sys.saupwknum.rk", "0");
                    String ru = SystemProperties.get("persist.sys.saupwknum.ru", "0");
                    String nk = SystemProperties.get("persist.sys.saupwknum.nk", "0");
                    String nu = SystemProperties.get("persist.sys.saupwknum.nu", "0");
                    Slog.d(SaupwkManager.TAG, "[SAUPWK]: rk:" + rk + " ru:" + ru + " nk:" + nk + " nu:" + nu + " rbsreason:" + reason);
                    int numpwk = Integer.parseInt(rk) + Integer.parseInt(ru) + Integer.parseInt(nk) + Integer.parseInt(nu);
                    SystemProperties.set("sys.saupwknum", String.valueOf(numpwk));
                    SystemProperties.set("persist.sys.saupwknum.rk", "0");
                    SystemProperties.set("persist.sys.saupwknum.ru", "0");
                    SystemProperties.set("persist.sys.saupwknum.nk", "0");
                    SystemProperties.set("persist.sys.saupwknum.nu", "0");
                    if (numpwk > 0) {
                        Slog.d(SaupwkManager.TAG, "[SAUPWK]: setting sys.saupwk.logdump to true ...\n");
                        SystemProperties.set("sys.saupwk.logdump", TemperatureProvider.SWITCH_ON);
                        return;
                    }
                    Slog.d(SaupwkManager.TAG, "[SAUPWK]: none power key detected ....\n");
                }
            }, 10000);
        }
    }

    public void saupwkEnterSR(String reason) {
        if (SystemProperties.get("persist.sys.saupwk_en", "").equals(NoFocusWindow.HUNG_CONFIG_ENABLE)) {
            String str = "sau";
            if (reason.equals(str) || reason.equals("silence")) {
                String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
                SystemProperties.set("persist.sys.sr_start", date);
                String from = SystemProperties.get("ro.build.version.ota", "na");
                SystemProperties.set("persist.sys.sau_from_ver", from);
                StringBuilder sb = new StringBuilder();
                sb.append("[SAUPWK]: sau START from ");
                sb.append(from);
                sb.append("@");
                sb.append(date);
                sb.append(", reason=");
                sb.append(reason.equals("silence") ? "slc" : str);
                Slog.w(TAG, sb.toString());
                if (reason.equals("silence")) {
                    str = "slc";
                }
                SystemProperties.set("persist.sys.rbsreason", str);
                String sr_reason = SystemProperties.get("persist.sys.rbsreason", "na");
                Slog.w(TAG, "[SAUPWK]: persist.sys.rbsreason:" + sr_reason);
            }
        }
    }

    public void saupwkMarkSlsauEnd() {
        if (SystemProperties.get("persist.sys.saupwk_en", "").equals(NoFocusWindow.HUNG_CONFIG_ENABLE)) {
            String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
            SystemProperties.set("persist.sys.sr_end", date);
            Slog.w(TAG, "[SAUPWK]: marking persist.sys.sr_end as " + date);
            String to = SystemProperties.get("ro.build.version.ota", "na");
            SystemProperties.set("persist.sys.sau_to_ver", to);
            Slog.w(TAG, "[SAUPWK]: sau END with " + to + "@ " + date);
            String str_old = SystemProperties.get("sys.slsau_finished", "");
            SystemProperties.set("sys.slsau_finished", TemperatureProvider.SWITCH_ON);
            String str_new = SystemProperties.get("sys.slsau_finished", "");
            Slog.d(TAG, "[SAUPWK]: setting property sys.slsau_finished:" + str_old + " to " + str_new);
        }
    }
}
