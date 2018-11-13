package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentProvider;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.Rlog;
import java.util.HashMap;

public class OemSimProtect {
    private static final String BUNDLE_PACKAGE = "packageName";
    private static final String BUNDLE_RESULT = "result";
    private static final String METHOD_CHECK_PROTECTED_STATE = "checkProtectedState";
    private static final String SIM_PROTECT_CONTACT = "contact";
    private static final String SIM_PROTECT_SMS = "sms";
    private static final Uri SIM_PROTECT_URI = Uri.parse("content://com.coloros.provider.PermissionProvider");
    private static final Uri SIM_PROTECT_URI_CONTACT = Uri.withAppendedPath(SIM_PROTECT_URI_OBSERVER, SIM_PROTECT_CONTACT);
    private static final Uri SIM_PROTECT_URI_OBSERVER = Uri.parse("content://com.coloros.provider.PermissionProvider/privacy_protect");
    private static final Uri SIM_PROTECT_URI_SMS = Uri.withAppendedPath(SIM_PROTECT_URI_OBSERVER, "sms");
    private static OemSimProtect instance = new OemSimProtect();
    private boolean isReg = false;
    private SimObserver observer_contact = new SimObserver();
    private HashMap<String, Boolean> sim_map_contact = new HashMap();
    private HashMap<String, Boolean> sim_map_sms = new HashMap();

    private class SimObserver extends ContentObserver {
        public SimObserver() {
            super(null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange);
            Rlog.d("OemSimProtect", "checkPrivacyProtect ,uri:" + uri + ",selfChange:" + selfChange);
            if (OemSimProtect.SIM_PROTECT_URI_CONTACT.equals(uri)) {
                Rlog.d("OemSimProtect", "checkPrivacyProtect ,contact.");
                OemSimProtect.this.sim_map_contact.clear();
            } else if (OemSimProtect.SIM_PROTECT_URI_SMS.equals(uri)) {
                Rlog.d("OemSimProtect", "checkPrivacyProtect ,sms.");
                OemSimProtect.this.sim_map_sms.clear();
            }
        }
    }

    private boolean checkPrivacyProtect(Context context, String packageName, String type) {
        if (!this.isReg) {
            registerSimProtectObserver(context);
        }
        boolean result = false;
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_PACKAGE, packageName);
        ContentResolver resolver = context.getContentResolver();
        IContentProvider provider = resolver.acquireUnstableProvider(SIM_PROTECT_URI);
        try {
            result = provider.call(context.getPackageName(), METHOD_CHECK_PROTECTED_STATE, type, bundle).getBoolean(BUNDLE_RESULT, false);
        } catch (Exception e) {
            Rlog.e("OemSimProtect", "checkPrivacyProtect error.");
        } finally {
            resolver.releaseUnstableProvider(provider);
        }
        Rlog.d("OemSimProtect", "checkPrivacyProtect :" + type + "/" + packageName + "/" + result);
        return result;
    }

    public static OemSimProtect getInstance() {
        return instance;
    }

    private OemSimProtect() {
    }

    public boolean isSimProtectContact(Context context, String packageName) {
        if (this.sim_map_contact.containsKey(packageName)) {
            return ((Boolean) this.sim_map_contact.get(packageName)).booleanValue();
        }
        boolean val = checkPrivacyProtect(context, packageName, SIM_PROTECT_CONTACT);
        this.sim_map_contact.put(packageName, Boolean.valueOf(val));
        return val;
    }

    public boolean isSimProtectSms(Context context, String packageName) {
        if (this.sim_map_sms.containsKey(packageName)) {
            return ((Boolean) this.sim_map_sms.get(packageName)).booleanValue();
        }
        boolean val = checkPrivacyProtect(context, packageName, "sms");
        this.sim_map_sms.put(packageName, Boolean.valueOf(val));
        return val;
    }

    public void registerSimProtectObserver(Context context) {
        try {
            context.getContentResolver().registerContentObserver(SIM_PROTECT_URI_OBSERVER, true, this.observer_contact);
            this.isReg = true;
        } catch (Exception e) {
            Rlog.e("OemSimProtect", "registerSimProtectObserver error.");
        }
    }

    public void unRegisterSimProtectObserver(Context context) {
        try {
            context.getContentResolver().unregisterContentObserver(this.observer_contact);
            this.isReg = false;
        } catch (Exception e) {
            Rlog.e("OemSimProtect", "unRegisterSimProtectObserver error.");
        }
    }
}
