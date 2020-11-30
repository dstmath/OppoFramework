package com.oppo.enterprise.mdmcoreservice.service.managerimpl;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import java.util.ArrayList;

public class KeyguardPolicyManager {
    private static final KeyguardPolicyManager ourInstance = new KeyguardPolicyManager();

    public static KeyguardPolicyManager getInstance() {
        return ourInstance;
    }

    private KeyguardPolicyManager() {
    }

    public void setKeyguardPolicy(ComponentName cn, Context context, int[] policies) throws Exception {
        if (policies != null) {
            DeviceRestrictionManagerImpl deviceRestrictionManager = new DeviceRestrictionManagerImpl(context);
            setKeyguardDB(context, hasFlags(policies, 1), "disable_keyguard_camera");
            setKeyguardDB(context, hasFlags(policies, 2), "disable_keyguard_notification");
            deviceRestrictionManager.setUnlockByFingerprintDisabled(cn, hasFlags(policies, 4));
            deviceRestrictionManager.setUnlockByFaceDisabled(cn, hasFlags(policies, 8));
            return;
        }
        throw new Exception("cannot use null");
    }

    public int[] getKeyguardPolicy(ComponentName cn, Context context) {
        ArrayList<Integer> list = new ArrayList<>();
        DeviceRestrictionManagerImpl deviceRestrictionManager = new DeviceRestrictionManagerImpl(context);
        if (1 == getKeyguardDB(context, "disable_keyguard_camera")) {
            list.add(1);
        }
        if (1 == getKeyguardDB(context, "disable_keyguard_notification")) {
            list.add(2);
        }
        if (deviceRestrictionManager.isUnlockByFingerprintDisabled(cn)) {
            list.add(4);
        }
        if (deviceRestrictionManager.isUnlockByFaceDisabled(cn)) {
            list.add(8);
        }
        int[] ints = new int[list.size()];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = list.get(i).intValue();
        }
        return ints;
    }

    public boolean setKeyguardDB(Context context, boolean disable, String... keys) {
        try {
            for (String key : keys) {
                Settings.Secure.putInt(context.getContentResolver(), key, disable ? 1 : 0);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int getKeyguardDB(Context context, String key) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), key);
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean hasFlags(int[] policies, int flag) {
        boolean contain = false;
        for (int policy : policies) {
            if (policy == flag) {
                contain = true;
            }
        }
        return contain;
    }
}
