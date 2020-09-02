package com.mediatek.omadm;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.NetworkUtils;
import android.net.Uri;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import java.net.URI;
import java.net.URISyntaxException;

public class FotaApnSettings {
    private static final String[] APNPROJ = {"type", "mmsc", "mmsproxy", "mmsport", "name", "apn", "bearer_bitmask", "protocol", "roaming_protocol", "authtype", "mvno_type", "mvno_match_data", "proxy", "port", "server", "user", "password"};
    private static final int FIELD_PORT = 13;
    private static final int FIELD_PROXY = 12;
    private static final int FIELD_SERVER = 14;
    private static final int FIELD_TYPE = 0;
    private static final String mAdmApnType = "fota";
    private final String mProxyAddr;
    private final int mProxyPort;
    private final String mSrvUrl;
    private final String mText;

    private static String getText(Cursor cursor) {
        StringBuilder str = new StringBuilder();
        str.append("APN [");
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            String name = cursor.getColumnName(i);
            String val = cursor.getString(i);
            if (!TextUtils.isEmpty(val)) {
                if (i > 0) {
                    str.append(' ');
                }
                str.append(name);
                str.append('=');
                str.append(val);
            }
        }
        str.append("]");
        return str.toString();
    }

    private static boolean checkApnType(String types) {
        if (TextUtils.isEmpty(types)) {
            return false;
        }
        for (String type : types.split(",")) {
            if (type.trim().equals(mAdmApnType)) {
                return true;
            }
        }
        return false;
    }

    private static String valueNullCheck(String value) {
        if (value != null) {
            return value.trim();
        }
        return null;
    }

    public static FotaApnSettings load(Context context, String apnName, int subId, String logTag, boolean chCurr) throws FotaException {
        Log.i(logTag, "Load APN name = " + apnName);
        StringBuilder selBuilder = new StringBuilder();
        String[] selArgs = null;
        if (chCurr) {
            selBuilder.append("current");
            selBuilder.append(" IS NOT NULL");
        }
        String apnName2 = valueNullCheck(apnName);
        if (!TextUtils.isEmpty(apnName2)) {
            if (selBuilder.length() > 0) {
                selBuilder.append(" AND ");
            }
            selBuilder.append("apn");
            selBuilder.append("=?");
            selArgs = new String[]{apnName2};
        }
        Cursor cur = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = Telephony.Carriers.CONTENT_URI;
            cur = SqliteWrapper.query(context, contentResolver, Uri.withAppendedPath(uri, "/subId/" + subId), APNPROJ, selBuilder.toString(), selArgs, (String) null);
            if (cur != null) {
                int proxy_port = 80;
                while (cur.moveToNext()) {
                    if (checkApnType(cur.getString(0))) {
                        String ser_url = valueNullCheck(cur.getString(FIELD_SERVER));
                        if (!TextUtils.isEmpty(ser_url)) {
                            ser_url = NetworkUtils.trimV4AddrZeros(ser_url);
                            try {
                                new URI(ser_url);
                            } catch (URISyntaxException e) {
                                throw new FotaException("Invalid Server url " + ser_url);
                            }
                        }
                        String proxy_addr = valueNullCheck(cur.getString(12));
                        if (!TextUtils.isEmpty(proxy_addr)) {
                            proxy_addr = NetworkUtils.trimV4AddrZeros(proxy_addr);
                            String portString = valueNullCheck(cur.getString(13));
                            if (!TextUtils.isEmpty(portString)) {
                                try {
                                    proxy_port = Integer.parseInt(portString);
                                } catch (NumberFormatException e2) {
                                    Log.e(logTag, "Invalid port " + portString + ", use 80");
                                }
                            }
                        }
                        FotaApnSettings fotaApnSettings = new FotaApnSettings(ser_url, proxy_addr, proxy_port, getText(cur));
                        cur.close();
                        return fotaApnSettings;
                    }
                }
            }
            throw new FotaException("Can not find valid APN");
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
    }

    public FotaApnSettings(String srvUrl, String proxyAddr, int proxyPort, String debugText) {
        this.mSrvUrl = srvUrl;
        this.mProxyAddr = proxyAddr;
        this.mProxyPort = proxyPort;
        this.mText = debugText;
    }

    public String toString() {
        return this.mText;
    }
}
