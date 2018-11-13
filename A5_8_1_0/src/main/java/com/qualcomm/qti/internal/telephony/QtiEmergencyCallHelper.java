package com.qualcomm.qti.internal.telephony;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager.MultiSimVariants;
import android.util.Log;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.PhoneFactory;

public class QtiEmergencyCallHelper {
    private static final String ALLOW_ECALL_ENHANCEMENT_PROPERTY = "persist.radio.enhance_ecall";
    private static final int INVALID = -1;
    private static final String LOG_TAG = "QtiEmergencyCallHelper";
    private static final int PRIMARY_STACK_MODEMID = 0;
    private static final int PROVISIONED = 1;
    private static String emerNum = null;
    private static boolean isDeviceInDualStandBy = true;
    private static boolean isEmergencyCallHelperInUse = false;
    private static QtiEmergencyCallHelper sInstance = null;

    public static int getPhoneIdForECall() {
        int phId;
        Phone phone;
        QtiSubscriptionController scontrol = QtiSubscriptionController.getInstance();
        int voicePhoneId = scontrol.getPhoneId(scontrol.getDefaultVoiceSubId());
        int phoneId = -1;
        TelephonyManager tm = TelephonyManager.getDefault();
        int phoneCount = tm.getPhoneCount();
        if (isDeviceInDualStandBy) {
            phId = 0;
            while (phId < phoneCount) {
                if (isEmergencyNumInternal(PhoneFactory.getPhone(phId).getSubId(), emerNum)) {
                    phId++;
                } else {
                    Log.d(LOG_TAG, "For sub specific number, return pref voice phone id:" + voicePhoneId);
                    return voicePhoneId;
                }
            }
            if (tm.getMultiSimConfiguration() != MultiSimVariants.DSDA) {
                for (Phone phone2 : PhoneFactory.getPhones()) {
                    if (phone2.getState() == State.OFFHOOK) {
                        Log.d(LOG_TAG, "Call already active on phoneId: " + phone2.getPhoneId());
                        return phone2.getPhoneId();
                    }
                }
            }
        }
        for (phId = 0; phId < phoneCount; phId++) {
            if (PhoneFactory.getPhone(phId).getServiceState().getState() == 0) {
                phoneId = phId;
                if (phId == voicePhoneId) {
                    break;
                }
            }
        }
        Log.d(LOG_TAG, "Voice phoneId in service = " + phoneId);
        if (phoneId == -1) {
            for (phId = 0; phId < phoneCount; phId++) {
                phone2 = PhoneFactory.getPhone(phId);
                int state = phone2.getServiceState().getState();
                if (phone2.getServiceState().isEmergencyOnly()) {
                    phoneId = phId;
                    if (phId == voicePhoneId) {
                        break;
                    }
                }
            }
        }
        Log.d(LOG_TAG, "Voice phoneId in Limited service = " + phoneId);
        if (phoneId == -1) {
            phId = 0;
            while (phId < phoneCount) {
                QtiUiccCardProvisioner uiccProvisioner = QtiUiccCardProvisioner.getInstance();
                if (tm.getSimState(phId) == 5 && uiccProvisioner.getCurrentUiccCardProvisioningStatus(phId) == 1) {
                    phoneId = phId;
                    if (phId == voicePhoneId) {
                        break;
                    }
                }
                phId++;
            }
            if (phoneId == -1) {
                phoneId = getPrimaryStackPhoneId();
            }
        }
        Log.d(LOG_TAG, "Voice phoneId in service = " + phoneId + " preferred phoneId =" + voicePhoneId);
        return phoneId;
    }

    public static int getPrimaryStackPhoneId() {
        int primayStackPhoneId = -1;
        for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
            Phone phone = PhoneFactory.getPhone(i);
            if (phone != null) {
                Log.d(LOG_TAG, "Logical Modem id: " + phone.getModemUuId() + " phoneId: " + i);
                String modemUuId = phone.getModemUuId();
                if (modemUuId != null && modemUuId.length() > 0 && !modemUuId.isEmpty() && Integer.parseInt(modemUuId) == 0) {
                    primayStackPhoneId = i;
                    Log.d(LOG_TAG, "Primay Stack phone id: " + primayStackPhoneId + " selected");
                    break;
                }
            }
        }
        if (primayStackPhoneId != -1) {
            return primayStackPhoneId;
        }
        Log.d(LOG_TAG, "Returning default phone id");
        return 0;
    }

    public static boolean isDeviceInSingleStandby() {
        if (SystemProperties.getBoolean(ALLOW_ECALL_ENHANCEMENT_PROPERTY, false)) {
            TelephonyManager tm = TelephonyManager.getDefault();
            int phoneCnt = tm.getPhoneCount();
            if (phoneCnt == 1) {
                return true;
            }
            int phoneId = 0;
            while (phoneId < phoneCnt) {
                QtiUiccCardProvisioner uiccProvisioner = QtiUiccCardProvisioner.getInstance();
                if (tm.getSimState(phoneId) == 5 && uiccProvisioner.getCurrentUiccCardProvisioningStatus(phoneId) == 1) {
                    phoneId++;
                } else {
                    Log.d(LOG_TAG, "modem is in single standby mode");
                    isDeviceInDualStandBy = false;
                    return true;
                }
            }
            Log.d(LOG_TAG, "modem is in dual standby mode");
            return false;
        }
        Log.d(LOG_TAG, "persist.radio.enhance_ecall not enabled");
        return false;
    }

    public static boolean isEmergencyNumber(String number) {
        boolean isEmergencyNum = false;
        QtiSubscriptionController scontrol = QtiSubscriptionController.getInstance();
        Phone[] phones = PhoneFactory.getPhones();
        emerNum = number;
        if (!isDeviceInSingleStandby()) {
            return PhoneNumberUtils.isEmergencyNumber(scontrol.getDefaultVoiceSubId(), number);
        }
        for (Phone phone : phones) {
            isEmergencyNum |= PhoneNumberUtils.isEmergencyNumber(phone.getSubId(), number);
        }
        return isEmergencyNum;
    }

    public static boolean isLocalEmergencyNumber(Context context, String number) {
        boolean isLocalEmergencyNum = false;
        QtiSubscriptionController scontrol = QtiSubscriptionController.getInstance();
        Phone[] phones = PhoneFactory.getPhones();
        emerNum = number;
        if (!isDeviceInSingleStandby()) {
            return PhoneNumberUtils.isLocalEmergencyNumber(context, scontrol.getDefaultVoiceSubId(), number);
        }
        for (Phone phone : phones) {
            isLocalEmergencyNum |= PhoneNumberUtils.isLocalEmergencyNumber(context, phone.getSubId(), number);
        }
        return isLocalEmergencyNum;
    }

    public static boolean isPotentialEmergencyNumber(String number) {
        boolean isPotentialEmergencyNum = false;
        QtiSubscriptionController scontrol = QtiSubscriptionController.getInstance();
        Phone[] phones = PhoneFactory.getPhones();
        if (!isDeviceInSingleStandby()) {
            return PhoneNumberUtils.isPotentialEmergencyNumber(scontrol.getDefaultVoiceSubId(), number);
        }
        for (Phone phone : phones) {
            isPotentialEmergencyNum |= PhoneNumberUtils.isPotentialEmergencyNumber(phone.getSubId(), number);
        }
        return isPotentialEmergencyNum;
    }

    public static boolean isPotentialLocalEmergencyNumber(Context context, String number) {
        boolean isPotentialLocalEmergencyNum = false;
        QtiSubscriptionController scontrol = QtiSubscriptionController.getInstance();
        Phone[] phones = PhoneFactory.getPhones();
        if (!isDeviceInSingleStandby()) {
            return PhoneNumberUtils.isPotentialLocalEmergencyNumber(context, scontrol.getDefaultVoiceSubId(), number);
        }
        for (Phone phone : phones) {
            isPotentialLocalEmergencyNum |= PhoneNumberUtils.isPotentialLocalEmergencyNumber(context, phone.getSubId(), number);
        }
        return isPotentialLocalEmergencyNum;
    }

    public static boolean isEmergencyNumInternal(int subId, String number) {
        return PhoneNumberUtils.isEmergencyNumber(subId, number);
    }
}
