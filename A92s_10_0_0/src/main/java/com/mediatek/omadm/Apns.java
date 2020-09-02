package com.mediatek.omadm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.SystemProperties;
import android.provider.Telephony;
import android.util.Slog;

public class Apns {
    private static final int APN_CLASS_1 = 1;
    private static final int APN_CLASS_2 = 2;
    private static final int APN_CLASS_3 = 3;
    private static final int APN_CLASS_4 = 4;
    private static final int APN_CLASS_5 = 5;
    private static final String[] CLASSES_TYPES = {CLASS_TYPE_1, CLASS_TYPE_2, CLASS_TYPE_3, CLASS_TYPE_4, CLASS_TYPE_5};
    private static final String CLASS_TYPE_1 = "'%ims%'";
    private static final String CLASS_TYPE_2 = "'%fota%'";
    private static final String CLASS_TYPE_3 = "'%default%'";
    private static final String CLASS_TYPE_4 = "'%cbs,mms%'";
    private static final String CLASS_TYPE_5 = "'%800%'";
    public static boolean DEBUG = true;
    private static final int IPV4V6_MASK = 3;
    private static final int IPV4_MASK = 1;
    private static final int IPV6_MASK = 2;
    private static final String[] PROJECT_APN = {"apn"};
    private static final String[] PROJECT_CENABLED = {"carrier_enabled"};
    private static final String[] PROJECT_CIDS = {"_id"};
    private static final String[] PROJECT_PROT = {"protocol"};
    private static final String TAG = Apns.class.getSimpleName();

    private static String createApnClsSelect(int apnCls) {
        if (apnCls < 1 || apnCls > APN_CLASS_5) {
            return null;
        }
        String classTypes = CLASSES_TYPES[apnCls - 1];
        StringBuilder sel = new StringBuilder("type");
        sel.append(" like ");
        sel.append(classTypes);
        if (apnCls < APN_CLASS_5) {
            sel.append(" and ");
            sel.append("numeric");
            sel.append("='");
            sel.append(SystemProperties.get("gsm.sim.operator.numeric", PalConstDefs.EMPTY_STRING));
            sel.append("'");
        }
        return sel.toString();
    }

    public static int enable(Context ctx, int apnCls, int enable) {
        int err = 0;
        String sel = createApnClsSelect(apnCls);
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "isEnabled.selection[" + apnCls + "] = " + sel);
        }
        if (sel == null) {
            PalConstDefs.throwEcxeption(7);
        }
        ContentValues cv = new ContentValues(1);
        cv.put("carrier_enabled", Integer.valueOf(enable));
        if (ctx.getContentResolver().update(Telephony.Carriers.CONTENT_URI, cv, sel, null) < 1) {
            err = 7;
        }
        PalConstDefs.throwEcxeption(err);
        return err;
    }

    public static int isEnabled(Context ctx, int apnCls) {
        int err = 0;
        int enabled = -1;
        String sel = createApnClsSelect(apnCls);
        if (sel == null) {
            err = 7;
            Slog.d(TAG, "isEnabled.selection is NULL!!");
        } else {
            String str = TAG;
            Slog.d(str, "isEnabled.selection[" + apnCls + "] = " + sel);
        }
        Cursor c = ctx.getContentResolver().query(Telephony.Carriers.CONTENT_URI, PROJECT_CENABLED, sel, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                enabled = c.getInt(0);
            } else {
                err = 7;
                Slog.d(TAG, "isEnabledValue got invalid content");
            }
            c.close();
        }
        PalConstDefs.throwEcxeption(err);
        return enabled;
    }

    public static int getId(Context ctx, int apnCls) {
        if (apnCls < 1 || apnCls > APN_CLASS_5) {
            PalConstDefs.throwEcxeption(7);
        }
        if (APN_CLASS_5 == apnCls) {
            return apnCls + 1;
        }
        return apnCls;
    }

    public static int getIpVersions(Context ctx, int apnCls) {
        int ipVMask = 0;
        String ipVStr = null;
        int err = 0;
        String sel = createApnClsSelect(apnCls);
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "getIpVersions.selection[" + apnCls + "] = " + sel);
        }
        if (sel == null) {
            PalConstDefs.throwEcxeption(7);
        }
        Cursor c = ctx.getContentResolver().query(Telephony.Carriers.CONTENT_URI, PROJECT_PROT, sel, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                ipVStr = c.getString(0);
            } else {
                err = 7;
            }
            c.close();
        }
        if (ipVStr != null) {
            if (DEBUG) {
                String str2 = TAG;
                Slog.d(str2, "IPVersions[" + apnCls + "].string = " + ipVStr);
            }
            if (ipVStr.equalsIgnoreCase("IP")) {
                ipVMask = 0 | 1;
            } else {
                if (ipVStr.contains("V4")) {
                    ipVMask = 0 | 1;
                }
                if (ipVStr.contains("V6")) {
                    ipVMask |= 2;
                }
            }
        } else if (DEBUG) {
            String str3 = TAG;
            Slog.d(str3, "IPVersions[" + apnCls + "] = null");
            err = 10;
        }
        PalConstDefs.throwEcxeption(err);
        return ipVMask;
    }

    public static String getName(Context ctx, int apnCls) {
        int err = 0;
        String name = null;
        String sel = createApnClsSelect(apnCls);
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "getName.selection [" + apnCls + "] = " + sel);
        }
        if (sel == null) {
            PalConstDefs.throwEcxeption(7);
        }
        Cursor c = ctx.getContentResolver().query(Telephony.Carriers.CONTENT_URI, PROJECT_APN, sel, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                name = c.getString(0);
            } else {
                err = 10;
            }
            c.close();
        }
        PalConstDefs.throwEcxeption(err);
        if (DEBUG) {
            String str2 = TAG;
            Slog.d(str2, "getName[" + apnCls + "] = " + name);
        }
        return name;
    }

    public static int setIpVersions(Context ctx, int apnCls, int ipvFlags) {
        int err = 0;
        String sel = createApnClsSelect(apnCls);
        String ipvStr = null;
        if (sel == null) {
            PalConstDefs.throwEcxeption(7);
        }
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "getIpVersions.selection[" + apnCls + "] = " + sel);
        }
        if (ipvFlags == 1) {
            ipvStr = "IP";
        } else if (ipvFlags == 2) {
            ipvStr = "IPV6";
        } else if (ipvFlags == 3) {
            ipvStr = "IPV4V6";
        }
        if (ipvStr != null) {
            ContentValues cv = new ContentValues(1);
            cv.put("protocol", ipvStr);
            if (ctx.getContentResolver().update(Telephony.Carriers.CONTENT_URI, cv, sel, null) < 1) {
                err = 10;
            }
        }
        PalConstDefs.throwEcxeption(err);
        return err;
    }

    public static int setName(Context ctx, int apnCls, String apnName) {
        int err = 0;
        ContentValues cv = new ContentValues(1);
        cv.put("apn", apnName);
        String sel = createApnClsSelect(apnCls);
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "setName.selection[" + apnCls + "] = " + sel);
        }
        if (sel == null) {
            PalConstDefs.throwEcxeption(7);
        }
        int rows = ctx.getContentResolver().update(Telephony.Carriers.CONTENT_URI, cv, sel, null);
        if (DEBUG) {
            String str2 = TAG;
            Slog.d(str2, "setName[" + apnCls + "] = " + ((String) null) + ", rows = " + rows);
        }
        if (DEBUG) {
            String apnName2 = getName(ctx, apnCls);
            String str3 = TAG;
            Slog.d(str3, "check.setName[" + apnCls + "] = " + apnName2);
        }
        if (rows < 1) {
            err = 10;
        }
        PalConstDefs.throwEcxeption(err);
        return err;
    }
}
