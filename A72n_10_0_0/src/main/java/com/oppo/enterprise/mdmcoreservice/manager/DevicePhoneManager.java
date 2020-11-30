package com.oppo.enterprise.mdmcoreservice.manager;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IDevicePhoneManager;

public class DevicePhoneManager extends DeviceBaseManager {
    public static final int CALLS_LIMIT_TYPE_DAY = 0;
    public static final int CALLS_LIMIT_TYPE_MONTH = 2;
    public static final int CALLS_LIMIT_TYPE_WEEK = 1;
    private static final String TAG = "DevicePhoneManager";
    private static final Object mLock = new Object();
    private static volatile DevicePhoneManager sInstance;

    public static final DevicePhoneManager getInstance(Context context) {
        DevicePhoneManager devicePhoneManager;
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new DevicePhoneManager(context);
            }
            devicePhoneManager = sInstance;
        }
        return devicePhoneManager;
    }

    private DevicePhoneManager(Context context) {
        super(context);
    }

    private IDevicePhoneManager getIDevicePhoneManager() {
        return IDevicePhoneManager.Stub.asInterface(getOppoMdmManager("DevicePhoneManager"));
    }

    public boolean setNonEmergencyCallDisabled(ComponentName admin, boolean disable) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager setNonEmergencyCallDisabled admin=" + admin + " setVale=" + disable);
                return manager.setNonEmergencyCallDisabled(admin, disable);
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setNonEmergencyCallDisabled error! e=" + e);
            return false;
        }
    }

    public boolean isNonEmergencyCallDisabled(ComponentName admin) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager isNonEmergencyCallDisabled admin=" + admin);
                return manager.isNonEmergencyCallDisabled(admin);
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "isNonEmergencyCallDisabled error! e=" + e);
            return false;
        }
    }

    public boolean setForwardCallSettingDisabled(ComponentName admin, boolean disable) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager setForwardCallSettingDisabled admin=" + admin + ", setVale=" + disable);
                return manager.setForwardCallSettingDisabled(admin, disable);
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setForwardCallSettingDisabled error! e=" + e);
            return false;
        }
    }

    public boolean setPhoneCallLimitation(ComponentName admin, boolean isOutgoing, int limitNumber, int dateType) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager setPhoneCallLimitation admin=" + admin + ", isOutgoing=" + isOutgoing + ",limitNumber :" + limitNumber);
                return manager.setPhoneCallLimitation(admin, isOutgoing, limitNumber, dateType);
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setForwardCallSettingDisabled error! e=" + e);
            return false;
        }
    }

    public int getPhoneCallLimitation(ComponentName admin, boolean isOutgoing) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager getPhoneCallLimitation admin=" + admin + ", isOutgoing=" + isOutgoing);
                return manager.getPhoneCallLimitation(admin, isOutgoing);
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
            return 0;
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setForwardCallSettingDisabled error! e=" + e);
            return 0;
        }
    }

    public boolean removePhoneCallLimitation(ComponentName admin, boolean isOutgoing) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager removePhoneCallLimitation admin=" + admin + ", isOutgoing=" + isOutgoing);
                return manager.removePhoneCallLimitation(admin, isOutgoing);
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setForwardCallSettingDisabled error! e=" + e);
            return false;
        }
    }

    public void endCall(ComponentName admin) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager:" + manager);
                manager.endCall(admin);
                return;
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "endCall error!");
            e.printStackTrace();
        }
    }

    public void answerRingingCall(ComponentName admin) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager:" + manager);
                manager.answerRingingCall(admin);
                return;
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "answerRingingCall error: " + e.toString());
        }
    }

    public boolean setVoiceIncomingDisabledforSlot1(ComponentName admin, boolean disabled) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager:" + manager);
                return manager.setVoiceIncomingDisabledforSlot1(admin, disabled);
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setVoiceIncomingDisabledforSlot1 error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setVoiceOutgoingDisabledforSlot1(ComponentName admin, boolean disabled) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager:" + manager);
                return manager.setVoiceOutgoingDisabledforSlot1(admin, disabled);
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setVoiceIncomingDisabledforSlot1 error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setVoiceIncomingDisabledforSlot2(ComponentName admin, boolean disabled) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager:" + manager);
                return manager.setVoiceIncomingDisabledforSlot2(admin, disabled);
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setVoiceIncomingDisabledforSlot2 error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setVoiceOutgoingDisabledforSlot2(ComponentName admin, boolean disabled) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager:" + manager);
                return manager.setVoiceOutgoingDisabledforSlot2(admin, disabled);
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setVoiceOutgoingDisabledforSlot2 error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean setRoamingCallDisabled(ComponentName admin, boolean disabled) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager:" + manager);
                return manager.setRoamingCallDisabled(admin, disabled);
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setRoamingCallDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isRoamingCallDisabled(ComponentName admin) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager:" + manager);
                return manager.isRoamingCallDisabled(admin);
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "isRoamingCallDisabled error : RemoteException");
            return false;
        }
    }

    public boolean setIncomingThirdCallDisabled(ComponentName admin, boolean disabled) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager:" + manager);
                return manager.setIncomingThirdCallDisabled(admin, disabled);
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setIncomingThirdCallDisabled error!");
            e.printStackTrace();
            return false;
        }
    }

    public void setSlot1SmsReceiveDisabled(ComponentName admin, boolean openswitch) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager:" + manager);
                manager.setSlot1SmsReceiveDisabled(admin, openswitch);
                return;
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setSlot1SmsReceiveDisabled error!");
        }
    }

    public void setSlot1SmsSendDisabled(ComponentName admin, boolean openswitch) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager:" + manager);
                manager.setSlot1SmsSendDisabled(admin, openswitch);
                return;
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setSlot1SmsSendDisabled error!");
        }
    }

    public void setSlot2SmsSendDisabled(ComponentName admin, boolean openswitch) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager:" + manager);
                manager.setSlot2SmsSendDisabled(admin, openswitch);
                return;
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setSlot2SmsSendDisabled error!");
        }
    }

    public void setSlot2SmsReceiveDisabled(ComponentName admin, boolean openswitch) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager:" + manager);
                manager.setSlot2SmsReceiveDisabled(admin, openswitch);
                return;
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setSlot2SmsReceiveDisabled error!");
        }
    }

    public void setSlot1SmsLimitation(ComponentName componentName, int limitNumber) {
        if (limitNumber < 0) {
            try {
                Log.d("DevicePhoneManager", "limitNumber is illegal");
            } catch (RemoteException e) {
                Log.i("DevicePhoneManager", "setSlot1SmsLimitation error!");
            }
        } else {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setSlot1SmsLimitation(componentName, limitNumber);
                return;
            }
            Log.d("DevicePhoneManager", "mdm service IDeviceRestrictionManager manager is null");
        }
    }

    public void setSlot1SmsLimitation(ComponentName componentName, boolean isOutgoing, int dateType, int limitNumber) {
        if (limitNumber < 0 || dateType < 0 || dateType > 2) {
            Log.d("DevicePhoneManager", "limitNumber or dateType is illegal");
            return;
        }
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setSlotOneSmsLimitation(componentName, isOutgoing, dateType, limitNumber);
            } else {
                Log.d("DevicePhoneManager", "mdm service IDeviceRestrictionManager manager is null");
            }
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setSlot1SmsLimitation error!");
        }
    }

    public void setSlot2SmsLimitation(ComponentName componentName, int limitNumber) {
        if (limitNumber < 0) {
            try {
                Log.d("DevicePhoneManager", "limitNumber is illegal");
            } catch (RemoteException e) {
                Log.i("DevicePhoneManager", "setSlot2SmsLimitation error!");
            }
        } else {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setSlot2SmsLimitation(componentName, limitNumber);
                return;
            }
            Log.d("DevicePhoneManager", "mdm service IDeviceRestrictionManager manager is null");
        }
    }

    public void setSlot2SmsLimitation(ComponentName componentName, boolean isOutgoing, int dateType, int limitNumber) {
        if (limitNumber < 0 || dateType < 0 || dateType > 2) {
            Log.d("DevicePhoneManager", "limitNumber or dateType is illegal");
            return;
        }
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.setSlotTwoSmsLimitation(componentName, isOutgoing, dateType, limitNumber);
            } else {
                Log.d("DevicePhoneManager", "mdm service IDeviceRestrictionManager manager is null");
            }
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setSlot2SmsLimitation error!");
        }
    }

    public void removeSmsLimitation(ComponentName componentName) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDeviceRestrictionManager manager:" + manager);
                manager.removeSmsLimitation(componentName);
                return;
            }
            Log.d("DevicePhoneManager", "mdm service IDeviceRestrictionManager manager is null");
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "removeSmsLimitation error!");
            e.printStackTrace();
        }
    }

    public int getSlot1SmsLimitation(ComponentName admin, boolean isOutgoing) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager:" + manager);
                return manager.getSlot1SmsLimitation(admin, isOutgoing);
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
            return -1;
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "getSlot1SmsLimitation e=" + e);
            return -1;
        }
    }

    public int getSlot2SmsLimitation(ComponentName admin, boolean isOutgoing) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager:" + manager);
                return manager.getSlot2SmsLimitation(admin, isOutgoing);
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
            return -1;
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "getSlot2SmsLimitation e=" + e);
            return -1;
        }
    }

    public void removeSlot1SmsLimitation(ComponentName admin, boolean isOutgoing) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager removeSlot1SmsLimitation admin=" + admin + ", isOutgoing=" + isOutgoing);
                manager.removeSlot1SmsLimitation(admin, isOutgoing);
                return;
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "removeSlot1SmsLimitation error! e=" + e);
        }
    }

    public void removeSlot2SmsLimitation(ComponentName admin, boolean isOutgoing) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager removeSlot2SmsLimitation admin=" + admin + ", isOutgoing=" + isOutgoing);
                manager.removeSlot2SmsLimitation(admin, isOutgoing);
                return;
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "removeSlot2SmsLimitation error! e=" + e);
        }
    }

    public void setSlotTwoDisabled(ComponentName componentName, boolean disabled) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager:" + manager);
                manager.setSlotTwoDisabled(componentName, disabled);
                return;
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "setSlot2Disabled error!");
            e.printStackTrace();
        }
    }

    public boolean isSlotTwoDisabled(ComponentName componentName) {
        try {
            IDevicePhoneManager manager = getIDevicePhoneManager();
            if (manager != null) {
                Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager:isSlotTwoDisabled");
                return manager.isSlotTwoDisabled(componentName);
            }
            Log.d("DevicePhoneManager", "mdm service IDevicePhoneManager manager is null");
            return false;
        } catch (RemoteException e) {
            Log.i("DevicePhoneManager", "isSlot2Disabled error!");
            e.printStackTrace();
            return false;
        }
    }
}
