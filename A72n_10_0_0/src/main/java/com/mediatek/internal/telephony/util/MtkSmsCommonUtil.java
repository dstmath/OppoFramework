package com.mediatek.internal.telephony.util;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.SMSDispatcher;
import com.mediatek.internal.telephony.ppl.IPplSmsFilter;
import com.mediatek.internal.telephony.ppl.PplSmsFilterExtension;

public final class MtkSmsCommonUtil {
    private static final boolean ENG = "eng".equals(Build.TYPE);
    public static final String IS_EMERGENCY_CB_PRIMARY = "isPrimary";
    private static final boolean IS_PRIVACY_PROTECTION_LOCK_SUPPORT = SystemProperties.get("ro.vendor.mtk_privacy_protection_lock").equals("1");
    private static final boolean IS_WAPPUSH_SUPPORT = SystemProperties.get("ro.vendor.mtk_wappush_support").equals("1");
    public static final String SELECT_BY_REFERENCE = "address=? AND reference_number=? AND count=? AND deleted=0 AND sub_id=?";
    public static final String SQL_3GPP2_SMS = " AND (destination_port & 262144=262144)";
    public static final String SQL_3GPP_SMS = " AND (destination_port & 131072=131072)";
    private static final String TAG = "MtkSmsCommonUtil";
    public static PplSmsFilterExtension sPplSmsFilter = null;

    private MtkSmsCommonUtil() {
    }

    public static boolean isPrivacyLockSupport() {
        return IS_PRIVACY_PROTECTION_LOCK_SUPPORT;
    }

    public static boolean isWapPushSupport() {
        return IS_WAPPUSH_SUPPORT;
    }

    public static void filterOutByPpl(Context context, SMSDispatcher.SmsTracker tracker) {
        if (isPrivacyLockSupport()) {
            if (sPplSmsFilter == null) {
                sPplSmsFilter = new PplSmsFilterExtension(context);
            }
            if (ENG) {
                Rlog.d(TAG, "[PPL] Phone privacy check start");
            }
            Bundle pplData = new Bundle();
            PplSmsFilterExtension pplSmsFilterExtension = sPplSmsFilter;
            pplData.putString(IPplSmsFilter.KEY_MSG_CONTENT, tracker.mFullMessageText);
            PplSmsFilterExtension pplSmsFilterExtension2 = sPplSmsFilter;
            pplData.putString(IPplSmsFilter.KEY_DST_ADDR, tracker.mDestAddress);
            PplSmsFilterExtension pplSmsFilterExtension3 = sPplSmsFilter;
            pplData.putString(IPplSmsFilter.KEY_FORMAT, tracker.mFormat);
            PplSmsFilterExtension pplSmsFilterExtension4 = sPplSmsFilter;
            pplData.putInt(IPplSmsFilter.KEY_SUB_ID, tracker.mSubId);
            PplSmsFilterExtension pplSmsFilterExtension5 = sPplSmsFilter;
            pplData.putInt(IPplSmsFilter.KEY_SMS_TYPE, 1);
            boolean pplResult = sPplSmsFilter.pplFilter(pplData);
            if (pplResult) {
                tracker.mPersistMessage = false;
            }
            if (ENG) {
                Rlog.d(TAG, "[PPL] Phone privacy check end, Need to filter(result) = " + pplResult);
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: byte[][] */
    /* JADX WARN: Multi-variable type inference failed */
    public static int phonePrivacyLockCheck(byte[][] pdus, String format, Context context, int subId) {
        if (!isPrivacyLockSupport() || 0 != 0) {
            return 0;
        }
        if (sPplSmsFilter == null) {
            sPplSmsFilter = new PplSmsFilterExtension(context);
        }
        Bundle pplData = new Bundle();
        PplSmsFilterExtension pplSmsFilterExtension = sPplSmsFilter;
        pplData.putSerializable(IPplSmsFilter.KEY_PDUS, pdus);
        PplSmsFilterExtension pplSmsFilterExtension2 = sPplSmsFilter;
        pplData.putString(IPplSmsFilter.KEY_FORMAT, format);
        PplSmsFilterExtension pplSmsFilterExtension3 = sPplSmsFilter;
        pplData.putInt(IPplSmsFilter.KEY_SUB_ID, subId);
        PplSmsFilterExtension pplSmsFilterExtension4 = sPplSmsFilter;
        pplData.putInt(IPplSmsFilter.KEY_SMS_TYPE, 0);
        boolean pplResult = sPplSmsFilter.pplFilter(pplData);
        if (ENG) {
            Rlog.d(TAG, "[Ppl] Phone privacy check end, Need to filter(result) = " + pplResult);
        }
        if (pplResult) {
            return -1;
        }
        return 0;
    }
}
