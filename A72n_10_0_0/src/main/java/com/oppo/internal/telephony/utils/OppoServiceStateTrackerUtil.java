package com.oppo.internal.telephony.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.text.TextUtils;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.uicc.AbstractSIMRecords;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.android.internal.telephony.util.ReflectionHelper;
import java.lang.reflect.Method;

public class OppoServiceStateTrackerUtil {
    protected static final boolean DBG = OemConstant.SWITCH_LOG;
    private static final String LOG_TAG = "OppoSSTUtil";
    private static String[] mMvnoPlmnArray = {"46605", "46697", "50503", "50502"};
    private static String operator = SystemProperties.get("ro.oppo.operator", "ex");
    private static String[] sMvnoPlmnArray = {"46605", "46697", "50503", "50502", "50501", "42403", "50506", "52501", "502153", "50211"};

    public static void oppoNvCheckAndRestore(Context context) {
        String nvstate = SystemProperties.get("ril.nvrestored", "success");
        log("oppoNvCheckAndRestore:" + nvstate);
        if (nvstate.equals("fail")) {
            log("nv restore state reached.");
            NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
            notificationManager.createNotificationChannel(new NotificationChannel("com.oppo.engineermode", "OppoNvRecovery:The process of NV recovery is unsuccessful!", 3));
            Notification.Builder builder = new Notification.Builder(context);
            builder.setSmallIcon(17301651);
            builder.setAutoCancel(false);
            builder.setContentTitle("OppoNvRecovery");
            builder.setContentText("The process of NV recovery is unsuccessful!");
            builder.setShowWhen(true);
            builder.setWhen(System.currentTimeMillis());
            builder.setChannelId("com.oppo.engineermode");
            Notification status = builder.build();
            status.flags = 34;
            status.priority = 2;
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.qualcomm.QualCommActivity"));
            intent.addFlags(268435456);
            status.contentIntent = PendingIntent.getActivityAsUser(context, 0, intent, 0, null, UserHandle.CURRENT);
            notificationManager.notify(8888, status);
        }
    }

    public static void oppoLteLockCheckDone(Context context) {
        log("lte earfcn is lock.");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        notificationManager.createNotificationChannel(new NotificationChannel("com.oppo.engineermode", "Lock LTE EARFCN:The earfcn of lte is lock, please unlock it befor test!", 3));
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(17301651);
        builder.setAutoCancel(false);
        builder.setContentTitle("LTE EARFCN IS LOCK");
        builder.setContentText("The earfcn of lte is lock, please clean it before test!");
        builder.setShowWhen(true);
        builder.setWhen(System.currentTimeMillis());
        builder.setChannelId("com.oppo.engineermode");
        Notification status = builder.build();
        status.flags = 34;
        status.priority = 2;
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.network.OppoLockLteArfcn"));
        intent.addFlags(268435456);
        status.contentIntent = PendingIntent.getActivityAsUser(context, 0, intent, 0, null, UserHandle.CURRENT);
        notificationManager.notify(8887, status);
    }

    public static String oppoGetExPlmn(ServiceState ss, String plmn, String simNumeric) {
        if (ss == null) {
            return plmn;
        }
        if (DBG) {
            log("oppoGetExPlmn plmn = " + plmn);
        }
        if (!(!SystemProperties.get("ro.oppo.version", "CN").equals("US") || simNumeric == null || ss.getOperatorNumeric() == null)) {
            if (DBG) {
                log("oppoGetExPlmn ss.getOperatorNumeric() = " + ss.getOperatorNumeric() + "  simNumeric = " + simNumeric);
            }
            if (ss.getOperatorNumeric().startsWith("52047") && simNumeric.equals("52005")) {
                plmn = "dtac-T";
            }
        }
        return plmn;
    }

    public static boolean isVodafoneNationalRoaming(String operatorMccMnc, String simCardMccMnc) {
        if (TextUtils.isEmpty(operatorMccMnc) || !operatorMccMnc.startsWith("505") || TextUtils.isEmpty(simCardMccMnc)) {
            return false;
        }
        if (simCardMccMnc.equals("50503") || simCardMccMnc.equals("50506")) {
            return true;
        }
        return false;
    }

    public static boolean isNZOperatorCheck(String simCardMccMnc) {
        if (TextUtils.isEmpty(simCardMccMnc) || !simCardMccMnc.equals("53024")) {
            return false;
        }
        return true;
    }

    public static boolean isVodafoneHomePlmn(String operatorMccMnc, String simCardMccMnc) {
        if (TextUtils.isEmpty(operatorMccMnc) || TextUtils.isEmpty(simCardMccMnc) || (!simCardMccMnc.equals("50503") && !simCardMccMnc.equals("50506"))) {
            return false;
        }
        if (operatorMccMnc.equals("50503") || operatorMccMnc.equals("50506")) {
            return true;
        }
        return false;
    }

    public static boolean isHomePlmn(String operatorMccMnc, String simCardMccMnc) {
        if (TextUtils.isEmpty(operatorMccMnc) || TextUtils.isEmpty(simCardMccMnc) || !simCardMccMnc.equals(operatorMccMnc)) {
            return false;
        }
        return true;
    }

    public static boolean isVodafoneOperatorCheck(String simCardMccMnc) {
        if (TextUtils.isEmpty(simCardMccMnc)) {
            return false;
        }
        if (simCardMccMnc.equals("50503") || simCardMccMnc.equals("50506")) {
            return true;
        }
        return false;
    }

    public static boolean isThailandAisVodafoneOperatorCheck(String simCardMccMnc) {
        if (TextUtils.isEmpty(simCardMccMnc)) {
            return false;
        }
        if (simCardMccMnc.equals("52003") || simCardMccMnc.equals("52001")) {
            return true;
        }
        return false;
    }

    public static String getNameForIwlanOnly(IccRecords iccRecords, ServiceState ss, String spn) {
        String iwlanName = null;
        SIMRecords tSimRecords = (SIMRecords) iccRecords;
        if (ss.getVoiceRegState() != 0 && 18 == ss.getRilDataRadioTechnology()) {
            if (!TextUtils.isEmpty(spn)) {
                iwlanName = spn;
                if (DBG) {
                    log("updateSpnDisplay:iwlan name get from spn=" + iwlanName);
                }
            } else {
                AbstractSIMRecords tmpSimRecords = (AbstractSIMRecords) OemTelephonyUtils.typeCasting(AbstractSIMRecords.class, tSimRecords);
                if (!(tmpSimRecords == null || tmpSimRecords.getEFpnnNetworkNames(0) == null)) {
                    iwlanName = tmpSimRecords.getEFpnnNetworkNames(0).sFullName;
                    if (DBG) {
                        log("updateSpnDisplay:iwlan name get from pnn=" + iwlanName);
                    }
                }
            }
        }
        if (DBG) {
            log("getNameForIwlanOnly: iwlanName =" + iwlanName);
        }
        return iwlanName;
    }

    public static boolean isSpecialGTCardCheck(String iccId, String simCardMccMnc) {
        if (DBG) {
            log("iccId : " + iccId);
        }
        int len = iccId != null ? iccId.length() : 0;
        if (TextUtils.isEmpty(simCardMccMnc) || len <= 6 || !simCardMccMnc.equals("52505") || !iccId.substring(0, 7).equals("8988605")) {
            return false;
        }
        return true;
    }

    public static boolean isVodafoneVersion() {
        boolean isVoda = false;
        if ("VODAFONE".equals(operator) || "VODAFONE_PREPAID".equals(operator) || "VODAFONE_POSTPAID".equals(operator)) {
            isVoda = true;
        }
        if (DBG) {
            log("isVodafoneVersion :" + isVoda);
        }
        return isVoda;
    }

    public static void oppoNvCheckAndRestore(Context context, Intent intent) {
        if (intent.getAction().equals("state.nvRestore.onBootup")) {
            log("nv restore state reached.");
            ((NotificationManager) context.getSystemService("notification")).notify(8888, new Notification.Builder(context).setTicker("Oppo_NV_Backup:The NV partition is invalid!").setSmallIcon(17301624).setContentTitle("Oppo_NV_Backup").setContentText("The NV partition is invalid!").setWhen(System.currentTimeMillis()).build());
        }
    }

    public static String oppoGetPlmnOverride(Context context, String operatorNumic, ServiceState ss) {
        try {
            Object oppoGetPlmnOverride = ReflectionHelper.callMethod((Object) null, "android.telephony.OppoTelephonyFunction", "oppoGetPlmnOverride", new Class[]{Context.class, String.class, ServiceState.class}, new Object[]{context, operatorNumic, ss});
            if (oppoGetPlmnOverride != null) {
                return (String) oppoGetPlmnOverride;
            }
            return "";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public static void oppoSetDataNetmgrdMTU(ServiceState ss) {
        if (ss.getOperatorNumeric() != null) {
            if (ss.getOperatorNumeric().equals("46000") || ss.getOperatorNumeric().equals("46002") || ss.getOperatorNumeric().equals("46007")) {
                SystemProperties.set("persist.data_netmgrd_mtu", "1400");
            }
        } else if (SystemProperties.get("persist.data_netmgrd_mtu").equals("1400")) {
            SystemProperties.set("persist.data_netmgrd_mtu", "1500");
        }
    }

    public static String oppoExpDisplayFormatting(ServiceState ss, String plmn, String simNumeric) {
        if (!(!SystemProperties.get("ro.oppo.version", "CN").equals("US") || simNumeric == null || ss.getOperatorNumeric() == null)) {
            if (ss.getOperatorNumeric().startsWith("405") && !TextUtils.isEmpty(plmn) && (plmn.equals("JIO") || plmn.equals("Jio 4G"))) {
                plmn = "JIO 4G";
            } else if (simNumeric.equals("23211") && ss.getOperatorNumeric().equals("23201")) {
                plmn = "bob";
            } else if (simNumeric.equals("23212") && ss.getOperatorNumeric().equals("23201")) {
                plmn = "yesss!";
            } else if (simNumeric.equals("50218") && ss.getOperatorNumeric().equals("50212")) {
                plmn = "U Mobile";
            } else if (ss.getOperatorNumeric().equals("50212")) {
                plmn = "MY MAXIS";
            } else if (ss.getOperatorNumeric().startsWith("51503") && simNumeric.equals("51505") && ss.getRoaming()) {
                plmn = "Sun";
            } else if (ss.getOperatorNumeric().startsWith("51503") && simNumeric.equals("51505")) {
                plmn = "SUN";
            } else if (ss.getOperatorNumeric().startsWith("52047") && simNumeric.equals("52005")) {
                plmn = "dtac-T";
            } else if (ss.getOperatorNumeric().startsWith("502153") && simNumeric.equals("502153")) {
                plmn = "unifi";
            } else if (ss.getOperatorNumeric().equals("52501") && !TextUtils.isEmpty(plmn) && plmn.equals("singtel")) {
                plmn = "Singtel";
            }
        }
        return plmn;
    }

    public static boolean[] oppoExpDisplayRules(ServiceState ss, String plmn, String spn, String simNumeric) {
        boolean[] oppoShow = new boolean[2];
        if (ss.getOperatorNumeric() != null) {
            if (ss.getRoaming() && ss.getOperatorNumeric().startsWith("460")) {
                oppoShow[0] = true;
                oppoShow[1] = false;
            }
            if (!TextUtils.isEmpty(simNumeric) && ss.getOperatorNumeric().startsWith("520")) {
                if (!simNumeric.startsWith("520")) {
                    if (!TextUtils.isEmpty(plmn)) {
                        oppoShow[1] = false;
                        oppoShow[0] = true;
                    } else {
                        oppoShow[1] = true;
                        oppoShow[0] = false;
                    }
                } else if (!TextUtils.isEmpty(spn)) {
                    oppoShow[1] = true;
                    oppoShow[0] = false;
                } else {
                    oppoShow[1] = false;
                    oppoShow[0] = true;
                }
            }
            if (!TextUtils.isEmpty(simNumeric)) {
                if (ss.getOperatorNumeric().startsWith("52015") && simNumeric.equals("52003")) {
                    oppoShow[1] = false;
                    oppoShow[0] = true;
                }
                log("display AIS-T for 52015");
            }
            if (ss.getOperatorNumeric().startsWith("46689")) {
                if (!TextUtils.isEmpty(plmn)) {
                    oppoShow[1] = false;
                    oppoShow[0] = true;
                } else {
                    oppoShow[1] = true;
                    oppoShow[0] = false;
                }
            }
            if (!TextUtils.isEmpty(simNumeric) && ss.getOperatorNumeric().startsWith("46697") && simNumeric.equals("46605")) {
                if (!TextUtils.isEmpty(spn)) {
                    oppoShow[1] = true;
                    oppoShow[0] = false;
                } else {
                    oppoShow[1] = false;
                    oppoShow[0] = true;
                }
            }
            if (ss.getOperatorNumeric().startsWith("51010")) {
                if (!TextUtils.isEmpty(plmn)) {
                    oppoShow[1] = false;
                    oppoShow[0] = true;
                } else {
                    oppoShow[1] = true;
                    oppoShow[0] = false;
                }
            }
            if (!TextUtils.isEmpty(simNumeric) && ((ss.getOperatorNumeric().equals("40518") && simNumeric.equals("405812")) || ((ss.getOperatorNumeric().equals("40488") && simNumeric.equals("40414")) || ((ss.getOperatorNumeric().equals("40552") && simNumeric.equals("405752")) || ((ss.getOperatorNumeric().equals("40552") && simNumeric.equals("405876")) || ((ss.getOperatorNumeric().equals("40449") && simNumeric.equals("405819")) || ((ss.getOperatorNumeric().equals("40498") && simNumeric.equals("405927")) || ((ss.getOperatorNumeric().startsWith("530") && simNumeric.startsWith("53024")) || ((ss.getOperatorNumeric().equals("20820") && simNumeric.equals("20825")) || ((ss.getOperatorNumeric().equals("20408") && simNumeric.equals("20409")) || (ss.getOperatorNumeric().equals("21407") && simNumeric.equals("21422")))))))))))) {
                if (!TextUtils.isEmpty(spn)) {
                    oppoShow[1] = true;
                    oppoShow[0] = false;
                } else {
                    oppoShow[1] = false;
                    oppoShow[0] = true;
                }
            }
        }
        return oppoShow;
    }

    public static boolean isNationalRoaming(String operatorMccMnc, String simCardMccMnc) {
        if (TextUtils.isEmpty(operatorMccMnc) || !operatorMccMnc.startsWith("505") || TextUtils.isEmpty(simCardMccMnc) || !simCardMccMnc.equals("50503")) {
            return false;
        }
        return true;
    }

    public static boolean oppoNoFixZoneAfterNitz(String operatorNumeric) {
        if (TextUtils.isEmpty(operatorNumeric) || !operatorNumeric.equals("47007")) {
            return true;
        }
        log("nitz is error,not fix zone after nitz");
        return false;
    }

    public static boolean oppoIsNZSimCheck(String simCardMccMnc) {
        if (TextUtils.isEmpty(simCardMccMnc) || !simCardMccMnc.equals("53024")) {
            return false;
        }
        return true;
    }

    public static boolean oppoIsNZOperatorCheck(String operator2) {
        if (TextUtils.isEmpty(operator2) || !operator2.startsWith("530")) {
            return false;
        }
        return true;
    }

    public static boolean isAUOperatorCheck(String simCardMccMnc) {
        if (TextUtils.isEmpty(simCardMccMnc) || !simCardMccMnc.equals("50502")) {
            return false;
        }
        return true;
    }

    public static void log(String s) {
        Rlog.d(LOG_TAG, "[GsmSST] " + s);
    }

    public static boolean isMvnoPlmn(String plmn) {
        int i = 0;
        while (true) {
            String[] strArr = mMvnoPlmnArray;
            if (i >= strArr.length) {
                log("is not MvnoPlmn");
                return false;
            } else if (strArr[i].equals(plmn)) {
                log("is MvnoPlmn");
                return true;
            } else {
                i++;
            }
        }
    }

    public static boolean isGT4GSimCardCheck(String simCardMccMnc) {
        if (TextUtils.isEmpty(simCardMccMnc) || !simCardMccMnc.equals("46605")) {
            return false;
        }
        return true;
    }

    public static void copyFrom(SignalStrength to, SignalStrength from) {
        try {
            Method method = Class.forName("android.telephony.SignalStrength").getMethod("copyFrom", SignalStrength.class);
            method.setAccessible(true);
            method.invoke(to, from);
        } catch (Exception e) {
            log("ClassNotFoundException" + e.getMessage());
        }
    }
}
