package com.oppo.enterprise.mdmcoreservice.certificate;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Slog;
import java.util.HashMap;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class OppoCertificateVerifyOkHttpHelper {
    private static final boolean DBG;
    private static final boolean PANIC_TYPE = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final boolean RELEASE_TYPE = SystemProperties.getBoolean("ro.build.release_type", false);
    public static OppoCertificateVerifyOkHttpHelper mHelper = null;
    private Context mContext;
    private HashMap<String, String> mQuery = new HashMap<>();

    static {
        boolean z = false;
        if (!RELEASE_TYPE || PANIC_TYPE) {
            z = true;
        }
        DBG = z;
    }

    public OppoCertificateVerifyOkHttpHelper(Context context) {
        this.mContext = context;
    }

    public static OppoCertificateVerifyOkHttpHelper getInstance(Context context) {
        if (mHelper == null) {
            mHelper = new OppoCertificateVerifyOkHttpHelper(context);
        }
        return mHelper;
    }

    public void initParam(String imei, String licenseCode, String openid) {
        this.mQuery.put("imei", imei == null ? "" : imei);
        this.mQuery.put("openId", openid == null ? "" : openid);
        this.mQuery.put("licenseCode", licenseCode == null ? "" : licenseCode);
    }

    private String getUrlFromRes(boolean enable) {
        int resId;
        if (enable) {
            resId = this.mContext.getResources().getIdentifier("grom_test_url", "string", this.mContext.getPackageName());
        } else {
            resId = this.mContext.getResources().getIdentifier("grom_product_url", "string", this.mContext.getPackageName());
        }
        if (resId == 0) {
            return null;
        }
        return (String) this.mContext.getResources().getText(resId);
    }

    private String getUrl(String server_url) {
        return server_url + "/cer/verify";
    }

    private String getServerTimeUrl(String server_url) {
        return server_url + "/cer/serverTime";
    }

    private String getRequestUrl(HashMap<String, String> param, String mainUrl) {
        if (param.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder(mainUrl);
        sb.append("?");
        boolean first = true;
        for (String key : param.keySet()) {
            if (!first) {
                sb.append("&");
            }
            sb.append(key);
            sb.append("=");
            sb.append(param.get(key));
            if (first) {
                first = false;
            }
        }
        return sb.toString();
    }

    public Call getSponse(boolean enable, int server_type) {
        String requestUrlWithoutParam;
        String mServer_url = getUrlFromRes(enable);
        if (mServer_url == null) {
            return null;
        }
        if (server_type == 0) {
            requestUrlWithoutParam = getServerTimeUrl(mServer_url);
        } else {
            requestUrlWithoutParam = getUrl(mServer_url);
        }
        if (requestUrlWithoutParam == null) {
            logD("requestUrlWithoutParam is null");
            return null;
        }
        logD(requestUrlWithoutParam);
        return new OkHttpClient().newCall(new Request.Builder().get().url(getRequestUrl(this.mQuery, requestUrlWithoutParam)).build());
    }

    private void logD(String msg) {
        if (DBG) {
            Slog.d("OppoCertificateVerifyOkHttpHelper", msg);
        }
    }
}
