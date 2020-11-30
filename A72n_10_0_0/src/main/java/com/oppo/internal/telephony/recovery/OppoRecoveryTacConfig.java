package com.oppo.internal.telephony.recovery;

import android.content.Context;
import android.database.ContentObserver;
import android.provider.Settings;
import android.telephony.Rlog;
import java.util.ArrayList;

public class OppoRecoveryTacConfig {
    private static final String DEFAULT_TAC_LIST = "9366,9338,9339,9340,5295";
    public static final String SYS_TAC_URI = "oppo_recovery_tac_list";
    private static final String TAG = "OppoRecoveryTacConfig";
    public static ArrayList<Integer> mTacCfgList = new ArrayList<>();

    public static void updateSettingConfig(Context context) {
        try {
            ArrayList<Integer> cfg = parseConfig(Settings.Global.getString(context.getContentResolver(), SYS_TAC_URI));
            if (cfg != null) {
                mTacCfgList = cfg;
                Rlog.d(TAG, "update tac config from rus: " + cfg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.e(TAG, "update rus config failed!" + e.getMessage());
        }
    }

    public static ArrayList<Integer> parseConfig(String str) {
        String[] split;
        ArrayList<Integer> list = new ArrayList<>();
        if (str == null) {
            return null;
        }
        try {
            if (str.length() == 0) {
                return list;
            }
            for (String str2 : str.split(",")) {
                list.add(Integer.valueOf(Integer.parseInt(str2)));
            }
            if (list.size() != 0) {
                return list;
            }
            Rlog.d(TAG, "parse tac list failed, no tac in list");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ArrayList<Integer> getDefaultTacList() {
        return parseConfig(DEFAULT_TAC_LIST);
    }

    public static ArrayList<Integer> getSystemConfig(Context context) {
        try {
            ArrayList<Integer> list = parseConfig(Settings.Global.getString(context.getContentResolver(), SYS_TAC_URI));
            if (list != null) {
                return list;
            }
            return getDefaultTacList();
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.e(TAG, "getSystemConfig failed " + e.getMessage());
            return getDefaultTacList();
        }
    }

    public static void initTacConfig(Context context, ContentObserver observer) {
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor(SYS_TAC_URI), true, observer);
        mTacCfgList = getSystemConfig(context);
        Rlog.d(TAG, "initTacConfig:" + mTacCfgList);
    }

    public static boolean isSpecial5gTac(int tac) {
        for (int i = 0; i < mTacCfgList.size(); i++) {
            if (mTacCfgList.get(i).intValue() == tac) {
                return true;
            }
        }
        return false;
    }
}
