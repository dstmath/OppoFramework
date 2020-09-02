package com.oppo.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IntentBroadcaster;
import com.android.internal.telephony.OppoSimlockManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.utils.OppoPolicyController;
import java.util.Arrays;

public class OppoSimlockManagerImpl extends OppoSimlockManager {
    protected static final String ACTION_SIM_LOCK_STATE_CHANGE = "oppo.intent.action.SIM_LOCK_STATE_CHANGE";
    protected static final int DATA_INVALID = 4;
    protected static final int EVENT_GET_SIMLOCK_OPERATOR_DONE = 22;
    protected static final int EVENT_SET_SIMLOCK_REGIONMARK_DONE = 24;
    protected static final int EVENT_SIMLOCK_REFRESH = 20;
    protected static final int EVENT_SML_DEVICE_LOCK_INFO_CHANGED = 23;
    protected static final int EVENT_SML_SLOT_LOCK_INFO_CHANGED = 25;
    protected static final int EVENT_UPDATE_SIMLOCK_OPERATOR = 21;
    protected static final int FUNCTION_SIMLOCK = 1;
    protected static final int IMEI_ERROR = 1;
    protected static final String INTENT_KEY_DEVICELOCK_STATE = "devicelock_state";
    protected static final String INTENT_KEY_FAIL_TYPE = "fail_type";
    protected static final String INTENT_KEY_LOCKSTATE = "lockstate";
    protected static final String INTENT_KEY_LOCK_TYPE = "lock_type";
    protected static final String INTENT_KEY_OPERATOR_TYPE = "operator_type";
    protected static final String INTENT_KEY_RESULT = "result";
    protected static final String INTENT_KEY_SLOT = "slot";
    protected static final int LOCKSTATE_INVALID = -1;
    protected static final int LOCKSTATE_LIMITED_UNLOCKED = 2;
    protected static final int LOCKSTATE_LOCKED = 1;
    protected static final int LOCKSTATE_PERMANENT_UNLOCKED = 3;
    protected static final int LOCKSTATE_UNLOCKED = 0;
    private static final String LOG_TAG = "OppoSimlockManager";
    protected static final int NO_ERROR = 0;
    protected static final String OPPO_SIMLOCK_CARD1_IS_OPERATOR_CARD = "persist.oppo.network.card1valid";
    protected static final String OPPO_SIMLOCK_CARD1_STATE = "persist.oppo.network.card1state";
    protected static final String OPPO_SIMLOCK_CARD2_IS_OPERATOR_CARD = "persist.oppo.network.card2valid";
    protected static final String OPPO_SIMLOCK_CARD2_STATE = "persist.oppo.network.card2state";
    protected static final String OPPO_SIMLOCK_FEATURE = "persist.oppo.network.simlock_on";
    protected static final String OPPO_SIMLOCK_OPERATOR = "persist.oppo.network.operator";
    protected static final String OPPO_SIMLOCK_OPERATOR_PRODUCTION = "persist.sys.oppo.simlockoperator";
    protected static final String OPPO_SIMLOCK_VN_REGIONMARK = "persist.oppo.network.vn.regionmark";
    protected static final int PASSWORD_ERROR = 2;
    protected static final int RESULT_FAIL = 0;
    protected static final int RESULT_SUCCESS = 1;
    protected static final int SIGNATURE_ERROR = 3;
    protected static final int SML_SLOT_LOCK_POLICY_ALL_SLOTS_INDIVIDUAL = 3;
    protected static final int SML_SLOT_LOCK_POLICY_LEGACY = 255;
    protected static final int SML_SLOT_LOCK_POLICY_LK_SLOT1 = 4;
    protected static final int SML_SLOT_LOCK_POLICY_LK_SLOT2 = 5;
    protected static final int SML_SLOT_LOCK_POLICY_LK_SLOTA = 6;
    protected static final int SML_SLOT_LOCK_POLICY_LK_SLOTA_RESTRICT_INVALID_CS = 7;
    protected static final int SML_SLOT_LOCK_POLICY_LK_SLOTA_RESTRICT_INVALID_ECC_FOR_VALID_NO_SERVICE = 9;
    protected static final int SML_SLOT_LOCK_POLICY_LK_SLOTA_RESTRICT_INVALID_VOICE = 10;
    protected static final int SML_SLOT_LOCK_POLICY_LK_SLOTA_RESTRICT_REVERSE = 8;
    protected static final int SML_SLOT_LOCK_POLICY_NONE = 0;
    protected static final int SML_SLOT_LOCK_POLICY_ONLY_SLOT1 = 1;
    protected static final int SML_SLOT_LOCK_POLICY_ONLY_SLOT2 = 2;
    protected static final int SML_SLOT_LOCK_POLICY_SERVICE_CAPABILITY_CS_ONLY = 1;
    protected static final int SML_SLOT_LOCK_POLICY_SERVICE_CAPABILITY_ECC_ONLY = 3;
    protected static final int SML_SLOT_LOCK_POLICY_SERVICE_CAPABILITY_FULL = 0;
    protected static final int SML_SLOT_LOCK_POLICY_SERVICE_CAPABILITY_NO_SERVICE = 4;
    protected static final int SML_SLOT_LOCK_POLICY_SERVICE_CAPABILITY_PS_ONLY = 2;
    protected static final int SML_SLOT_LOCK_POLICY_SERVICE_CAPABILITY_UNKNOWN = -1;
    protected static final int SML_SLOT_LOCK_POLICY_UNKNOWN = -1;
    protected static final int SML_SLOT_LOCK_POLICY_VALID_CARD_ABSENT = 2;
    protected static final int SML_SLOT_LOCK_POLICY_VALID_CARD_NO = 1;
    protected static final int SML_SLOT_LOCK_POLICY_VALID_CARD_UNKNOWN = -1;
    protected static final int SML_SLOT_LOCK_POLICY_VALID_CARD_YES = 0;
    protected static final int TYPE_AFTER_SALE_LOCK = 2;
    protected static final int TYPE_AFTER_SALE_UNLOCK = 3;
    protected static final int TYPE_PRODUCTION_LINE_LOCK = 0;
    protected static final int TYPE_PRODUCTION_LINE_UNLOCK = 1;
    protected static final int TYPE_SIM_LOCK = 1;
    protected static final int UNKNOWN = 5;
    protected static final int UNSUPPORT = 6;
    private static Context mContext = null;
    protected int[][] SmlInfo = {new int[]{-1, -1, -1, -1}, new int[]{-1, -1, -1, -1}};
    private CommandsInterface[] mCi = null;
    private Phone[] mPhone = null;

    public OppoSimlockManagerImpl(Phone[] phone, CommandsInterface[] ci, Context context) {
        super(phone, ci, context);
        init(phone, ci, context);
    }

    public void init(Phone[] phone, CommandsInterface[] ci, Context context) {
        this.mPhone = phone;
        this.mCi = ci;
        mContext = context;
        SystemProperties.set(OPPO_SIMLOCK_CARD1_STATE, Integer.toString(-1));
        SystemProperties.set(OPPO_SIMLOCK_CARD2_STATE, Integer.toString(-1));
        if (phone != null && phone.length > 0 && ci != null && ci.length > 0) {
            logd("OppoSimlockManagerImpl init");
            String[] atString = {"AT+ESLBLOB=13", "+ESLBLOB"};
            phone[0].invokeOemRilRequestStrings(atString, obtainMessage(22));
            for (int i = 0; i < ci.length; i++) {
                ((AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, phone[i])).registerForSimlockState(this, 23, 0);
            }
        }
    }

    /* JADX INFO: Multiple debug info for r17v1 java.lang.String: [D('simlockOperator' java.lang.String), D('VnRegionMark' java.lang.String)] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0167  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0171  */
    public void handleMessage(Message msg) {
        String[] atresult2;
        String simlockOperator;
        String[] atresult3;
        getCiIndex(msg);
        switch (msg.what) {
            case 21:
                logd("handleMessage EVENT_UPDATE_SIMLOCK_OPERATOR");
                this.mPhone[0].invokeOemRilRequestStrings(new String[]{"AT+ESLBLOB=13", "+ESLBLOB"}, obtainMessage(22));
                return;
            case 22:
                logd("handleMessage EVENT_GET_SIMLOCK_OPERATOR_DONE");
                AsyncResult ar2 = (AsyncResult) msg.obj;
                SystemProperties.set(OPPO_SIMLOCK_OPERATOR, "-1");
                SystemProperties.set(OPPO_SIMLOCK_OPERATOR_PRODUCTION, "-1");
                if (ar2 != null) {
                    if (ar2.result == null || !(ar2.result instanceof String[])) {
                        atresult2 = null;
                    } else {
                        atresult2 = (String[]) ar2.result;
                    }
                    if (atresult2 != null) {
                        try {
                            if (atresult2[0].length() > 10) {
                                String[] splitResult = atresult2[0].substring(10).split(",");
                                simlockOperator = splitResult[0];
                                try {
                                    String VnRegionMark = splitResult[1];
                                    try {
                                        if ("1".equals(VnRegionMark)) {
                                            SystemProperties.set(OPPO_SIMLOCK_VN_REGIONMARK, VnRegionMark);
                                        } else if ("VN".equals(SystemProperties.get("ro.oppo.regionmark", "EX"))) {
                                            this.mPhone[0].invokeOemRilRequestStrings(new String[]{"AT+ESLBLOB=14", "+ESLBLOB"}, obtainMessage(24));
                                            logd("Region mark is VN,need to set the Region mark flag");
                                        }
                                        logd("at result not null, simlockOperator is " + simlockOperator + ",VnRegionMark is " + VnRegionMark);
                                    } catch (Exception e) {
                                        e = e;
                                        Rlog.d(LOG_TAG, "Exception is :" + e.toString());
                                        if ("-1".equals(simlockOperator)) {
                                        }
                                    }
                                } catch (Exception e2) {
                                    e = e2;
                                    Rlog.d(LOG_TAG, "Exception is :" + e.toString());
                                    if ("-1".equals(simlockOperator)) {
                                    }
                                }
                                if ("-1".equals(simlockOperator)) {
                                    SystemProperties.set(OPPO_SIMLOCK_OPERATOR, "-1");
                                    SystemProperties.set(OPPO_SIMLOCK_OPERATOR_PRODUCTION, "-1");
                                    OppoPolicyController.setSimlockOperator("-1");
                                    return;
                                }
                                SystemProperties.set(OPPO_SIMLOCK_OPERATOR, simlockOperator);
                                SystemProperties.set(OPPO_SIMLOCK_OPERATOR_PRODUCTION, simlockOperator);
                                SystemProperties.set(OPPO_SIMLOCK_FEATURE, "TRUE");
                                OppoPolicyController.setSimlockOperator(simlockOperator);
                                return;
                            }
                        } catch (Exception e3) {
                            e = e3;
                            simlockOperator = "-1";
                            Rlog.d(LOG_TAG, "Exception is :" + e.toString());
                            if ("-1".equals(simlockOperator)) {
                            }
                        }
                    }
                    simlockOperator = "-1";
                    if ("-1".equals(simlockOperator)) {
                    }
                } else {
                    return;
                }
                break;
            case 23:
                logd("handleMessage EVENT_SML_DEVICE_LOCK_INFO_CHANGED");
                onSimlockDeviceLockInfoChanged((AsyncResult) msg.obj);
                return;
            case 24:
                logd("handleMessage EVENT_SET_SIMLOCK_REGIONMARK_DONE");
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar != null) {
                    if (ar.result == null || !(ar.result instanceof String[])) {
                        atresult3 = null;
                    } else {
                        atresult3 = (String[]) ar.result;
                    }
                    if (atresult3 != null) {
                        try {
                            if (atresult3[0].length() > 10) {
                                String VnRegionMark2 = atresult3[0].substring(10);
                                SystemProperties.set(OPPO_SIMLOCK_VN_REGIONMARK, VnRegionMark2);
                                logd("set regionmark done, VnRegionMark flag is " + VnRegionMark2);
                                return;
                            }
                            return;
                        } catch (Exception e4) {
                            Rlog.d(LOG_TAG, "Exception is :" + e4.toString());
                            return;
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            default:
                return;
        }
    }

    public void handleOppoLoaded(int slotId) {
        if (slotId == 0 && "FALSE".equals(SystemProperties.get(OPPO_SIMLOCK_CARD1_IS_OPERATOR_CARD, Integer.toString(-1))) && Integer.toString(1).equals(SystemProperties.get(OPPO_SIMLOCK_CARD1_STATE, Integer.toString(-1)))) {
            broadcastSimlockIntentIfNeeded(0, 2);
        } else if (slotId == 1 && "FALSE".equals(SystemProperties.get(OPPO_SIMLOCK_CARD2_IS_OPERATOR_CARD, Integer.toString(-1))) && Integer.toString(1).equals(SystemProperties.get(OPPO_SIMLOCK_CARD2_STATE, Integer.toString(-1)))) {
            broadcastSimlockIntentIfNeeded(1, 2);
        }
    }

    public void handleOppoAbsentOrError(int slotId) {
        if (slotId == 0) {
            SystemProperties.set(OPPO_SIMLOCK_CARD1_STATE, Integer.toString(-1));
            SystemProperties.set(OPPO_SIMLOCK_CARD1_IS_OPERATOR_CARD, Integer.toString(-1));
        } else if (slotId == 1) {
            SystemProperties.set(OPPO_SIMLOCK_CARD2_STATE, Integer.toString(-1));
            SystemProperties.set(OPPO_SIMLOCK_CARD2_IS_OPERATOR_CARD, Integer.toString(-1));
        }
    }

    public void handleOppoSimlocked(int slotId) {
        if (slotId == 0) {
            SystemProperties.set(OPPO_SIMLOCK_CARD1_STATE, Integer.toString(1));
        } else if (slotId == 1) {
            SystemProperties.set(OPPO_SIMLOCK_CARD2_STATE, Integer.toString(1));
        }
        broadcastSimlockIntentIfNeeded(slotId, 1);
    }

    private void onSimlockDeviceLockInfoChanged(AsyncResult ar) {
        int[] resp = (int[]) ar.result;
        if (resp != null) {
            logd("onSimlockDeviceLockInfoChanged Oppo simlock the callback simlock data is " + Arrays.toString(resp));
            handleSimlockData(resp);
            return;
        }
        logd("onSimlockDeviceLockInfoChanged the resp is null");
    }

    private void handleSimlockData(int[] simlockData) {
        if (simlockData == null || simlockData.length != 8) {
            Rlog.d(LOG_TAG, "the data is invalid");
            return;
        }
        int operatorType = simlockData[0];
        int lockType = simlockData[1];
        int result = simlockData[2];
        int failType = simlockData[3];
        int i = simlockData[4];
        int i2 = simlockData[5];
        int deviceLockState = simlockData[6];
        int operatorInfo = simlockData[7];
        if (operatorType == 255) {
            SystemProperties.set(OPPO_SIMLOCK_OPERATOR, "-1");
            SystemProperties.set(OPPO_SIMLOCK_OPERATOR_PRODUCTION, "-1");
        } else {
            SystemProperties.set(OPPO_SIMLOCK_OPERATOR, Integer.toString(operatorType));
            SystemProperties.set(OPPO_SIMLOCK_OPERATOR_PRODUCTION, Integer.toString(operatorType));
        }
        OppoPolicyController.setSimlockOperator(Integer.toString(operatorType));
        if (operatorInfo != -1) {
            if ((operatorInfo & 1) != 0) {
                OppoPolicyController.setCallOutRestricted(true);
            } else {
                OppoPolicyController.setCallOutRestricted(false);
            }
            if ((operatorInfo & 2) != 0) {
                OppoPolicyController.setCallInRestricted(true);
            } else {
                OppoPolicyController.setCallInRestricted(false);
            }
            if ((operatorInfo & 4) != 0) {
                OppoPolicyController.setSmsReceiveRestricted(true);
            } else {
                OppoPolicyController.setSmsReceiveRestricted(false);
            }
            if ((operatorInfo & 8) != 0) {
                OppoPolicyController.setSmsSendRestricted(true);
            } else {
                OppoPolicyController.setSmsSendRestricted(false);
            }
            if ((operatorInfo & 64) != 0) {
                OppoPolicyController.setPsRestricted(true);
            } else {
                OppoPolicyController.setPsRestricted(false);
            }
        }
        broadcastSimlockIntentIfNeeded(1, lockType, result, failType, operatorType, -1, -1, deviceLockState);
    }

    public void onSmlSlotLoclInfoChaned(AsyncResult ar, Integer index) {
        if (ar.exception != null || ar.result == null) {
            Rlog.e(LOG_TAG, "onSmlSlotLoclInfoChaned exception");
            return;
        }
        boolean needUpdate = false;
        int[] info = (int[]) ar.result;
        if (info.length != 4) {
            Rlog.e(LOG_TAG, "onSmlSlotLoclInfoChaned exception");
            return;
        }
        int slotId = index.intValue();
        logd("onSmlSlotLoclInfoChaned, infomation:,lock policy:" + info[0] + ",lock state:" + info[1] + ",service capability:" + info[2] + ",sim valid:" + info[3] + ",slotId:" + slotId);
        for (int i = 0; i < 4; i++) {
            int[][] iArr = this.SmlInfo;
            if (iArr[slotId][i] != info[i]) {
                iArr[slotId][i] = info[i];
                needUpdate = true;
            }
        }
        if (!needUpdate) {
            return;
        }
        if (info[0] != 9 && info[0] != 7) {
            return;
        }
        if (info[2] == 0) {
            if (info[3] == 0) {
                if (slotId == 0) {
                    SystemProperties.set(OPPO_SIMLOCK_CARD1_STATE, Integer.toString(0));
                    broadcastSimlockIntentIfNeeded(0, 0);
                } else if (slotId == 1) {
                    SystemProperties.set(OPPO_SIMLOCK_CARD2_STATE, Integer.toString(0));
                    broadcastSimlockIntentIfNeeded(1, 0);
                }
            } else if (info[3] != 1) {
            } else {
                if (slotId == 0) {
                    SystemProperties.set(OPPO_SIMLOCK_CARD1_STATE, Integer.toString(2));
                    broadcastSimlockIntentIfNeeded(0, 2);
                } else if (slotId == 1) {
                    SystemProperties.set(OPPO_SIMLOCK_CARD2_STATE, Integer.toString(2));
                    broadcastSimlockIntentIfNeeded(1, 2);
                }
            }
        } else if (info[2] == 4) {
            if (info[3] != 1) {
                return;
            }
            if (slotId == 0) {
                SystemProperties.set(OPPO_SIMLOCK_CARD1_STATE, Integer.toString(1));
                broadcastSimlockIntentIfNeeded(0, 1);
            } else if (slotId == 1) {
                SystemProperties.set(OPPO_SIMLOCK_CARD2_STATE, Integer.toString(1));
                broadcastSimlockIntentIfNeeded(1, 1);
            }
        } else if (info[2] != 1) {
        } else {
            if (info[3] == 0) {
                if (slotId == 0) {
                    SystemProperties.set(OPPO_SIMLOCK_CARD1_STATE, Integer.toString(0));
                    broadcastSimlockIntentIfNeeded(0, 0);
                } else if (slotId == 1) {
                    SystemProperties.set(OPPO_SIMLOCK_CARD2_STATE, Integer.toString(0));
                    broadcastSimlockIntentIfNeeded(1, 0);
                }
            } else if (info[3] != 1) {
            } else {
                if (slotId == 0) {
                    SystemProperties.set(OPPO_SIMLOCK_CARD1_STATE, Integer.toString(2));
                    broadcastSimlockIntentIfNeeded(0, 2);
                } else if (slotId == 1) {
                    SystemProperties.set(OPPO_SIMLOCK_CARD2_STATE, Integer.toString(2));
                    broadcastSimlockIntentIfNeeded(1, 2);
                }
            }
        }
    }

    private void broadcastSimlockIntentIfNeeded(int function, int lockType, int result, int failType, int operatorType, int slotId, int lockState, int devicelockstate) {
        if (function == 1 && mContext != null) {
            logd("broadcastSimlockIntentIfNeeded");
            Intent i = new Intent(ACTION_SIM_LOCK_STATE_CHANGE);
            i.putExtra(INTENT_KEY_LOCK_TYPE, lockType);
            i.putExtra(INTENT_KEY_RESULT, result);
            i.putExtra(INTENT_KEY_FAIL_TYPE, failType);
            i.putExtra(INTENT_KEY_OPERATOR_TYPE, operatorType);
            i.putExtra(INTENT_KEY_SLOT, slotId);
            i.putExtra(INTENT_KEY_LOCKSTATE, lockState);
            i.putExtra(INTENT_KEY_DEVICELOCK_STATE, devicelockstate);
            i.addFlags(16777216);
            IntentBroadcaster.getInstance(mContext).broadcastStickyIntent(i, 0);
            logd("Broadcasting intent ACTION_SIM_LOCK_STATE_CHANGE, lockType = " + lockType + " result = " + result + " failType = " + failType + " operatorType = " + operatorType + " slotId = " + slotId + " lockState = " + lockState + " devicelockstate = " + devicelockstate);
        }
    }

    private void broadcastSimlockIntentIfNeeded(int lockType, int result, int failType) {
        if (lockType >= 0 && result >= 0) {
            broadcastSimlockIntentIfNeeded(1, lockType, result, failType, -1, 0, -1, -1);
        }
    }

    private void broadcastSimlockIntentIfNeeded(int operatorType) {
        if (operatorType > 0) {
            broadcastSimlockIntentIfNeeded(1, -1, -1, -1, operatorType, 0, -1, -1);
            SystemProperties.set(OPPO_SIMLOCK_OPERATOR, Integer.toString(operatorType));
            SystemProperties.set(OPPO_SIMLOCK_OPERATOR_PRODUCTION, Integer.toString(operatorType));
        }
    }

    private void broadcastSimlockIntentIfNeeded(int slotId, int lockState) {
        broadcastSimlockIntentIfNeeded(1, -1, -1, -1, -1, slotId, lockState, -1);
        if (slotId == 0) {
            SystemProperties.set(OPPO_SIMLOCK_CARD1_STATE, Integer.toString(lockState));
        } else if (slotId == 1) {
            SystemProperties.set(OPPO_SIMLOCK_CARD2_STATE, Integer.toString(lockState));
        }
    }

    private Integer getCiIndex(Message msg) {
        Integer index = new Integer(0);
        if (msg == null) {
            return index;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return (Integer) msg.obj;
        }
        if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return index;
        }
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
            return index;
        }
        return (Integer) ar.userObj;
    }

    private void logd(String message) {
        Rlog.d(LOG_TAG, message);
    }
}
