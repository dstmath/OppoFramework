package com.oppo.enterprise.mdmcoreservice.service.managerimpl;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IOppoCustomizeService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager;
import com.oppo.enterprise.mdmcoreservice.receiver.CallLimitAlarmReceiver;
import com.oppo.enterprise.mdmcoreservice.service.PermissionManager;
import java.util.Calendar;

public class DevicePhoneManagerImpl extends IDevicePhoneManager.Stub {
    private static String PERSIST_SYS_OEM_P_RRT = "persist.sys.oem_p_rrt";
    private static String PERSIST_SYS_OEM_P_SST = "persist.sys.oem_p_sst";
    private static String PERSIST_SYS_OEM_P_ST = "persist.sys.oem_p_st";
    private static String PERSIST_SYS_OEM_S_RRT = "persist.sys.oem_s_rrt";
    private static String PERSIST_SYS_OEM_S_SST = "persist.sys.oem_s_sst";
    private static String PERSIST_SYS_OEM_S_ST = "persist.sys.oem_s_st";
    private AlarmManager mAlarmManager;
    private Context mContext;
    private IOppoCustomizeService mCustService = IOppoCustomizeService.Stub.asInterface(ServiceManager.getService("oppocustomize"));
    private PendingIntent mPendingIntent;

    public DevicePhoneManagerImpl(Context context) {
        this.mContext = context;
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
    }

    private static boolean propSetEnable(String prop, boolean defval) {
        return propSetEnable(prop, defval ? "1" : "0");
    }

    private static boolean propSetEnable(String prop, String defval) {
        try {
            Log.d("DevicePhoneManagerImpl", "propSetEnable " + prop + ": " + defval);
            SystemProperties.set(prop, defval);
            return true;
        } catch (Exception ex) {
            Log.e("DevicePhoneManagerImpl", "setProp error :" + ex.getMessage());
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public boolean setNonEmergencyCallDisabled(ComponentName admin, boolean disable) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DevicePhoneManagerImpl", "setNonEmergencyCallDisabled , disable：" + disable);
        Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_phone_non_emergencyCall_disable", disable ? 1 : 0);
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public boolean isNonEmergencyCallDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DevicePhoneManagerImpl", "isNonEmergencyCallDisabled");
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "oppo_phone_non_emergencyCall_disable", 0) == 1) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public boolean setForwardCallSettingDisabled(ComponentName admin, boolean disable) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DevicePhoneManagerImpl", "setForwardCallSettingDisabled , disable：" + disable);
        Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_phone_forward_call_setting_disable", disable ? 1 : 0);
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public boolean setPhoneCallLimitation(ComponentName admin, boolean isOutgoing, int limitNumber, int dateType) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DevicePhoneManagerImpl", "setPhoneCallLimitation , isOutgoing：" + isOutgoing + ",limitNumber:" + limitNumber + ",dateType:" + dateType);
        if (limitNumber < 1 || !startCallPolicyAlarm(dateType, isOutgoing)) {
            return false;
        }
        if (isOutgoing) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_phone_set_outgoing_call_limit_policy", limitNumber);
        } else {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_phone_set_incoming_call_limit_policy", limitNumber);
        }
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public int getPhoneCallLimitation(ComponentName admin, boolean isOutgoing) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DevicePhoneManagerImpl", "getPhoneCallLimitation , isOutgoing：" + isOutgoing);
        if (isOutgoing) {
            return Settings.Secure.getInt(this.mContext.getContentResolver(), "oppo_phone_set_outgoing_call_limit_policy", 0);
        }
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "oppo_phone_set_incoming_call_limit_policy", 0);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public boolean removePhoneCallLimitation(ComponentName admin, boolean isOutgoing) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DevicePhoneManagerImpl", "removePhoneCallLimitation , isOutgoing：" + isOutgoing);
        if (isOutgoing) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_phone_set_outgoing_call_limit_policy", -1);
            return true;
        }
        Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_phone_set_incoming_call_limit_policy", -1);
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public void endCall(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm == null) {
            Log.w("DevicePhoneManagerImpl", "TelephonyManager is null, return!");
            return;
        }
        try {
            if (tm.getCallState() != 0) {
                boolean ret = tm.endCall();
                Log.d("DevicePhoneManagerImpl", "end the call is " + ret);
                return;
            }
            Log.d("DevicePhoneManagerImpl", "current call state is idle.");
        } catch (Exception e) {
            Log.e("DevicePhoneManagerImpl", "endCall fail!");
            e.printStackTrace();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public void answerRingingCall(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        TelecomManager tm = (TelecomManager) this.mContext.getSystemService("telecom");
        if (tm == null) {
            Log.w("DevicePhoneManagerImpl", "TelecomManager is null, return!");
            return;
        }
        try {
            if (tm.getCallState() == 1) {
                tm.acceptRingingCall();
                Log.d("DevicePhoneManagerImpl", "answer the call");
                return;
            }
            Log.d("DevicePhoneManagerImpl", "current call state is not ring.");
        } catch (Exception e) {
            Log.i("DevicePhoneManagerImpl", "answer the ringing Call error: " + e.toString());
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public boolean setVoiceIncomingDisabledforSlot1(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        return propSetEnable("persist.sys.oem_s1_vi", !disabled);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public boolean setVoiceOutgoingDisabledforSlot1(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        return propSetEnable("persist.sys.oem_s1_vo", !disabled);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public boolean setVoiceIncomingDisabledforSlot2(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        return propSetEnable("persist.sys.oem_s2_vi", !disabled);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public boolean setVoiceOutgoingDisabledforSlot2(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        return propSetEnable("persist.sys.oem_s2_vo", !disabled);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public boolean setRoamingCallDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        return propSetEnable("persist.sys.oem_r_vb", !disabled);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public boolean isRoamingCallDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return !propGetEnable("persist.sys.oem_r_vb", "-1");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public boolean setIncomingThirdCallDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        return propSetEnable("persist.sys.oem_t_vi", !disabled);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public void setSlot1SmsSendDisabled(ComponentName admin, boolean openswitch) {
        PermissionManager.getInstance().checkPermission();
        if (openswitch) {
            try {
                SystemProperties.set("persist.sys.oem_s1_ss", "0");
            } catch (Exception e) {
                Log.e("DevicePhoneManagerImpl", "setSlot1SmsSendDisabled fail!", e);
            }
        } else {
            SystemProperties.set("persist.sys.oem_s1_ss", "1");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public void setSlot1SmsReceiveDisabled(ComponentName admin, boolean openswitch) {
        PermissionManager.getInstance().checkPermission();
        if (openswitch) {
            try {
                SystemProperties.set("persist.sys.oem_s1_sr", "0");
            } catch (Exception e) {
                Log.e("DevicePhoneManagerImpl", "setSlot1SmsReceiveDisabled fail!", e);
            }
        } else {
            SystemProperties.set("persist.sys.oem_s1_sr", "1");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public void setSlot2SmsSendDisabled(ComponentName admin, boolean openswitch) {
        PermissionManager.getInstance().checkPermission();
        if (openswitch) {
            try {
                SystemProperties.set("persist.sys.oem_s2_ss", "0");
            } catch (Exception e) {
                Log.e("DevicePhoneManagerImpl", "setSlot2SmsSendDisabled fail!", e);
            }
        } else {
            SystemProperties.set("persist.sys.oem_s2_ss", "1");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public void setSlot2SmsReceiveDisabled(ComponentName admin, boolean openswitch) {
        PermissionManager.getInstance().checkPermission();
        if (openswitch) {
            try {
                SystemProperties.set("persist.sys.oem_s2_sr", "0");
            } catch (Exception e) {
                Log.e("DevicePhoneManagerImpl", "setSlot2SmsReceiveDisabled fail!", e);
            }
        } else {
            SystemProperties.set("persist.sys.oem_s2_sr", "1");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public void setSlot1SmsLimitation(ComponentName componentName, int limitNumber) throws RemoteException {
        PermissionManager.getInstance().checkPermission();
        try {
            SystemProperties.set("persist.sys.oem_p_slt", "0");
            SystemProperties.set("persist.sys.oem_p_sdt", "-1");
            SystemProperties.set(PERSIST_SYS_OEM_P_SST, Integer.toString(limitNumber));
            Log.d("DevicePhoneManagerImpl", "setSlot1SmsLimitation");
        } catch (Exception e) {
            Log.e("DevicePhoneManagerImpl", "setSlot1SmsLimitation fail!", e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public void setSlotOneSmsLimitation(ComponentName componentName, boolean isOutgoing, int dateType, int limitNumber) throws RemoteException {
        PermissionManager.getInstance().checkPermission();
        if (isOutgoing) {
            try {
                SystemProperties.set(PERSIST_SYS_OEM_P_ST, "0");
                SystemProperties.set("persist.sys.oem_p_slt", "0");
                SystemProperties.set(PERSIST_SYS_OEM_P_SST, Integer.toString(limitNumber));
                SystemProperties.set("persist.sys.oem_p_sdt", Integer.toString(dateType));
            } catch (Exception e) {
                Log.e("DevicePhoneManagerImpl", "setSlotOneSmsLimitation fail!", e);
                return;
            }
        } else {
            SystemProperties.set("persist.sys.oem_p_rt", "0");
            SystemProperties.set("persist.sys.oem_p_rlt", "0");
            SystemProperties.set(PERSIST_SYS_OEM_P_RRT, Integer.toString(limitNumber));
            SystemProperties.set("persist.sys.oem_p_rdt", Integer.toString(dateType));
        }
        Log.d("DevicePhoneManagerImpl", "setSlotOneSmsLimitation");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public void setSlot2SmsLimitation(ComponentName componentName, int limitNumber) throws RemoteException {
        PermissionManager.getInstance().checkPermission();
        try {
            SystemProperties.set("persist.sys.oem_s_slt", "0");
            SystemProperties.set("persist.sys.oem_s_sdt", "-1");
            SystemProperties.set(PERSIST_SYS_OEM_S_SST, Integer.toString(limitNumber));
            Log.d("DevicePhoneManagerImpl", "setSlot2SmsLimitation");
        } catch (Exception e) {
            Log.e("DevicePhoneManagerImpl", "setSlot2SmsLimitation fail!", e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public void setSlotTwoSmsLimitation(ComponentName componentName, boolean isOutgoing, int dateType, int limitNumber) throws RemoteException {
        PermissionManager.getInstance().checkPermission();
        if (isOutgoing) {
            try {
                SystemProperties.set(PERSIST_SYS_OEM_S_ST, "0");
                SystemProperties.set("persist.sys.oem_s_slt", "0");
                SystemProperties.set(PERSIST_SYS_OEM_S_SST, Integer.toString(limitNumber));
                SystemProperties.set("persist.sys.oem_s_sdt", Integer.toString(dateType));
            } catch (Exception e) {
                Log.e("DevicePhoneManagerImpl", "setSlotTwoSmsLimitation fail!", e);
                return;
            }
        } else {
            SystemProperties.set("persist.sys.oem_s_rt", "0");
            SystemProperties.set("persist.sys.oem_s_rlt", "0");
            SystemProperties.set(PERSIST_SYS_OEM_S_RRT, Integer.toString(limitNumber));
            SystemProperties.set("persist.sys.oem_s_rdt", Integer.toString(dateType));
        }
        Log.d("DevicePhoneManagerImpl", "setSlotTwoSmsLimitation");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public void removeSmsLimitation(ComponentName componentName) throws RemoteException {
        PermissionManager.getInstance().checkPermission();
        try {
            SystemProperties.set(PERSIST_SYS_OEM_S_SST, "-1");
            SystemProperties.set(PERSIST_SYS_OEM_P_SST, "-1");
            SystemProperties.set(PERSIST_SYS_OEM_S_ST, "0");
            SystemProperties.set(PERSIST_SYS_OEM_P_ST, "0");
            SystemProperties.set("persist.sys.oem_p_slt", "0");
            SystemProperties.set("persist.sys.oem_p_sdt", "-1");
            SystemProperties.set("persist.sys.oem_s_slt", "0");
            SystemProperties.set("persist.sys.oem_s_sdt", "-1");
            SystemProperties.set(PERSIST_SYS_OEM_S_RRT, "-1");
            SystemProperties.set(PERSIST_SYS_OEM_P_RRT, "-1");
            SystemProperties.set("persist.sys.oem_p_rt", "0");
            SystemProperties.set("persist.sys.oem_s_rt", "0");
            SystemProperties.set("persist.sys.oem_p_rlt", "0");
            SystemProperties.set("persist.sys.oem_p_rdt", "-1");
            SystemProperties.set("persist.sys.oem_s_rlt", "0");
            SystemProperties.set("persist.sys.oem_s_rdt", "-1");
            Log.d("DevicePhoneManagerImpl", "removeSmsLimitation");
        } catch (Exception e) {
            Log.e("DevicePhoneManagerImpl", "removeSmsLimitation fail!", e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public int getSlot1SmsLimitation(ComponentName admin, boolean isOutgoing) {
        PermissionManager.getInstance().checkPermission();
        if (isOutgoing) {
            return SystemProperties.getInt("persist.sys.oem_p_sst", -1);
        }
        return SystemProperties.getInt("persist.sys.oem_p_rrt", -1);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public int getSlot2SmsLimitation(ComponentName admin, boolean isOutgoing) {
        PermissionManager.getInstance().checkPermission();
        if (isOutgoing) {
            return SystemProperties.getInt("persist.sys.oem_s_sst", -1);
        }
        return SystemProperties.getInt("persist.sys.oem_s_rrt", -1);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public void removeSlot1SmsLimitation(ComponentName admin, boolean isOutgoing) {
        PermissionManager.getInstance().checkPermission();
        if (isOutgoing) {
            try {
                SystemProperties.set(PERSIST_SYS_OEM_P_SST, "-1");
                SystemProperties.set("persist.sys.oem_p_st", "0");
                SystemProperties.set("persist.sys.oem_p_slt", "0");
                SystemProperties.set("persist.sys.oem_p_sdt", "-1");
            } catch (Exception e) {
                Log.e("DevicePhoneManagerImpl", "removeSlot1SmsLimitation fail!", e);
                return;
            }
        } else {
            SystemProperties.set(PERSIST_SYS_OEM_P_RRT, "-1");
            SystemProperties.set("persist.sys.oem_p_rt", "0");
            SystemProperties.set("persist.sys.oem_p_rlt", "0");
            SystemProperties.set("persist.sys.oem_p_rdt", "-1");
        }
        Log.d("DevicePhoneManagerImpl", "removeSlot1SmsLimitation");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public void removeSlot2SmsLimitation(ComponentName admin, boolean isOutgoing) {
        PermissionManager.getInstance().checkPermission();
        if (isOutgoing) {
            try {
                SystemProperties.set(PERSIST_SYS_OEM_S_SST, "-1");
                SystemProperties.set("persist.sys.oem_s_st", "0");
                SystemProperties.set("persist.sys.oem_s_slt", "0");
                SystemProperties.set("persist.sys.oem_s_sdt", "-1");
            } catch (Exception e) {
                Log.e("DevicePhoneManagerImpl", "removeSlot2SmsLimitation fail!", e);
                return;
            }
        } else {
            SystemProperties.set(PERSIST_SYS_OEM_S_RRT, "-1");
            SystemProperties.set("persist.sys.oem_s_rt", "0");
            SystemProperties.set("persist.sys.oem_s_rlt", "0");
            SystemProperties.set("persist.sys.oem_s_rdt", "-1");
        }
        Log.d("DevicePhoneManagerImpl", "removeSlot2SmsLimitation");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public void setSlotTwoDisabled(ComponentName componentName, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        boolean isHasSlotTwo = TelephonyManager.getDefault().hasIccCard(1);
        Log.d("DevicePhoneManagerImpl", "isHasSlotTwo ：" + isHasSlotTwo);
        if (isHasSlotTwo) {
            if (disabled) {
                propSetEnable("persist.sys.oem_disable_slot_two", "0");
            } else {
                propSetEnable("persist.sys.oem_disable_slot_two", "-1");
            }
            if (disabled) {
                try {
                    this.mCustService.deactivateSubId(getSubId(1));
                } catch (Exception e) {
                    Log.d("DevicePhoneManagerImpl", "deactivateSubId:err!");
                }
            } else {
                try {
                    this.mCustService.activateSubId(getSubId(1));
                } catch (Exception e2) {
                    Log.d("DevicePhoneManagerImpl", "activateSubId:err!");
                }
            }
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager
    public boolean isSlotTwoDisabled(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        return !propGetEnable("persist.sys.oem_disable_slot_two", "-1");
    }

    public static int getSubId(int slotId) {
        int vRetSubId;
        int[] subId = SubscriptionManager.getSubId(slotId);
        if (subId == null || subId.length <= 0) {
            vRetSubId = -1000;
        } else {
            vRetSubId = subId[0];
        }
        Log.d("DevicePhoneManagerImpl", "SubId=" + vRetSubId);
        return vRetSubId;
    }

    private static boolean propGetEnable(String prop, String defval) {
        boolean ret = false;
        try {
            Log.d("DevicePhoneManagerImpl", "propGetEnable " + prop + ": " + defval);
            String val = SystemProperties.get(prop, defval);
            if (val == null) {
                return false;
            }
            if (!val.equals("true")) {
                if (!val.equals("false")) {
                    if (Integer.parseInt(val) != 0) {
                        ret = true;
                    }
                    return ret;
                }
            }
            return Boolean.parseBoolean(val);
        } catch (Exception ex) {
            Log.e("DevicePhoneManagerImpl", "getProp error :" + ex.getMessage());
            return false;
        }
    }

    private boolean startCallPolicyAlarm(int dateType, boolean isOutgoing) {
        if (!(this.mAlarmManager == null || this.mPendingIntent == null)) {
            this.mAlarmManager.cancel(this.mPendingIntent);
        }
        int fieldType = -1;
        if (dateType == 0) {
            fieldType = 6;
        } else if (dateType == 1) {
            fieldType = 3;
        } else if (dateType == 2) {
            fieldType = 2;
        }
        if (fieldType == -1) {
            Log.d("DevicePhoneManagerImpl", "startCallPolicyAlarm, return because dateType is invalid");
            return false;
        }
        Log.d("DevicePhoneManagerImpl", "startCallPolicyAlarm, dateType: " + dateType + ",fieldType:" + fieldType);
        Intent intent = new Intent(this.mContext, CallLimitAlarmReceiver.class);
        intent.setAction("android.intent.action.call.limit.time.out");
        intent.putExtra("isoutgoingcall", isOutgoing);
        this.mPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(fieldType, 1);
        if (this.mAlarmManager != null) {
            this.mAlarmManager.set(2, calendar.getTimeInMillis(), this.mPendingIntent);
        }
        return true;
    }
}
