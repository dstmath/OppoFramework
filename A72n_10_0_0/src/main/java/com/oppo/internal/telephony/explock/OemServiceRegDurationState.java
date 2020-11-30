package com.oppo.internal.telephony.explock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityTdscdma;
import android.telephony.CellIdentityWcdma;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.text.TextUtils;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.AbstractSubscriptionController;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.util.OemTelephonyUtils;

public class OemServiceRegDurationState extends Handler {
    private String LOG_TAG = "OemSRS";
    private Context mContext;
    private GsmCdmaPhone mPhone;
    private BroadcastReceiver mRegionLockReceiver = new BroadcastReceiver() {
        /* class com.oppo.internal.telephony.explock.OemServiceRegDurationState.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RegionLockConstant.ACTION_UNLOCK_NETWORK_SIM1) || intent.getAction().equals(RegionLockConstant.ACTION_UNLOCK_NETWORK_SIM2)) {
                OemServiceRegDurationState oemServiceRegDurationState = OemServiceRegDurationState.this;
                oemServiceRegDurationState.logd("ACTION_UNLOCK_NETWORK,phoneId==" + intent.getIntExtra("phoneId", 0) + ",getPhoneId()==" + OemServiceRegDurationState.this.mPhone.getPhoneId());
                if (intent.getIntExtra("phoneId", 0) == OemServiceRegDurationState.this.mPhone.getPhoneId()) {
                    OemServiceRegDurationState oemServiceRegDurationState2 = OemServiceRegDurationState.this;
                    oemServiceRegDurationState2.sendMessage(oemServiceRegDurationState2.obtainMessage(RegionLockConstant.EVENT_NETWORK_LOCK_STATUS));
                }
            }
        }
    };
    private PendingIntent mResetIntentSlot1 = null;
    private PendingIntent mResetIntentSlot2 = null;
    private boolean oppoNeedSetAlarm = true;
    private boolean oppoNeedSetRadio = true;
    private RegionLockPlmnListParser regionLockPlmnList;

    public OemServiceRegDurationState(Context context, GsmCdmaPhone phone) {
        this.mPhone = phone;
        this.mContext = context;
        oppoInitRegionLock();
    }

    private void oppoInitRegionLock() {
        if (OemConstant.EXP_VERSION && OemLockUtils.isRegionLock()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(RegionLockConstant.ACTION_UNLOCK_NETWORK_SIM1);
            filter.addAction(RegionLockConstant.ACTION_UNLOCK_NETWORK_SIM2);
            this.mContext.registerReceiver(this.mRegionLockReceiver, filter);
            this.regionLockPlmnList = RegionLockPlmnListParser.getInstance(this.mContext);
        }
    }

    public boolean isRegionLockedState(ServiceState mNewSS, CellIdentity id, boolean hasLocationChanged) {
        if (OemLockUtils.isRegionLock() && OemLockUtils.getRegionLockStatus() && !oppoIsTestCard()) {
            logd("Davis oppoNeedSetRadio==" + this.oppoNeedSetRadio + ",oppoNeedSetAlarm==" + this.oppoNeedSetAlarm);
            String plmn = null;
            if (getRegState(mNewSS) == 0 && !TextUtils.isEmpty(mNewSS.getOperatorNumeric())) {
                plmn = mNewSS.getOperatorNumeric();
            }
            if (TextUtils.isEmpty(plmn)) {
                if (mNewSS.getVoiceRegState() != 0 && !this.oppoNeedSetAlarm) {
                    this.oppoNeedSetAlarm = true;
                    cancelNetworkStatusAlarm(this.mPhone.getPhoneId());
                    logd("Davis cancel alarm");
                }
                if (mNewSS.getVoiceRegState() != 0 && !this.oppoNeedSetRadio) {
                    this.oppoNeedSetRadio = true;
                    logd("Davis reset radio flag");
                }
            } else if (this.regionLockPlmnList.oppoIsWhiteListNetwork(plmn)) {
                if (this.oppoNeedSetAlarm) {
                    startResetNetworkStatusAlarm(this.mPhone.getPhoneId());
                    this.oppoNeedSetAlarm = false;
                    logd("Davis start alarm");
                }
                SystemProperties.set(RegionLockConstant.NOTIFY_NETLOCK_FLAG, "0");
                if (OemLockUtils.isEnableUpRlock()) {
                    recordServiceCell(id, hasLocationChanged);
                }
            } else if (this.regionLockPlmnList.oppoIsBlackListNetwork(plmn)) {
                oppoSetPowerRadioOff(this.mPhone.getPhoneId());
                SystemProperties.set(RegionLockConstant.NOTIFY_NETLOCK_FLAG, "1");
                return true;
            }
        }
        return false;
    }

    private int getRegState(ServiceState ss) {
        int regState = ss.getVoiceRegState();
        int dataRegState = ss.getDataRegState();
        return (regState == 1 && dataRegState == 0) ? dataRegState : regState;
    }

    private void startResetNetworkStatusAlarm(int phoneId) {
        int delayInMs = SystemProperties.getInt(RegionLockConstant.PERSIST_LOCK_TIME, (int) RegionLockConstant.DEFAULT_TIMES);
        logd("lock timer, delayInMs = " + delayInMs + "phoneId==" + phoneId);
        AlarmManager alarm = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        if (alarm == null) {
            return;
        }
        if (phoneId == 1) {
            Intent intent = new Intent(RegionLockConstant.ACTION_UNLOCK_NETWORK_SIM2);
            intent.putExtra("phoneId", phoneId);
            this.mResetIntentSlot2 = PendingIntent.getBroadcast(this.mPhone.getContext(), 1, intent, 134217728);
            alarm.setExact(2, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mResetIntentSlot2);
            return;
        }
        Intent intent2 = new Intent(RegionLockConstant.ACTION_UNLOCK_NETWORK_SIM1);
        intent2.putExtra("phoneId", phoneId);
        this.mResetIntentSlot1 = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent2, 134217728);
        alarm.setExact(2, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mResetIntentSlot1);
    }

    private void cancelNetworkStatusAlarm(int phoneId) {
        AlarmManager alarm = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        if (phoneId == 1) {
            PendingIntent pendingIntent = this.mResetIntentSlot2;
            if (pendingIntent != null) {
                alarm.cancel(pendingIntent);
                this.mResetIntentSlot2 = null;
                return;
            }
            return;
        }
        PendingIntent pendingIntent2 = this.mResetIntentSlot1;
        if (pendingIntent2 != null) {
            alarm.cancel(pendingIntent2);
            this.mResetIntentSlot1 = null;
        }
    }

    private void oppoSetPowerRadioOff(int phoneId) {
        logd("Davis,oppoSetPowerRadioOff");
        if (this.oppoNeedSetRadio) {
            this.oppoNeedSetRadio = false;
            Intent intent = new Intent(RegionLockConstant.ACTION_NETWORK_LOCK);
            intent.putExtra(RegionLockConstant.NETLOCK_STATUS, "1");
            sendBroadCastChangedNetlockStatus(intent);
        }
        try {
            ((AbstractSubscriptionController) OemTelephonyUtils.typeCasting(AbstractSubscriptionController.class, SubscriptionController.getInstance())).deactivateSubId(this.mPhone.getSubId());
        } catch (Exception e) {
            logd("deactivateUiccCard : " + e);
        }
    }

    private void sendBroadCastChangedNetlockStatus(Intent intent) {
        this.mPhone.getContext().sendBroadcast(intent, "oppo.permission.OPPO_COMPONENT_SAFE");
    }

    private boolean oppoIsTestCard() {
        if (!((AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, this.mPhone)).is_test_card()) {
            return false;
        }
        logd("Davis,test simcard");
        return true;
    }

    private void recordServiceCell(CellIdentity id, boolean hasLocationChanged) {
        int cid;
        if (hasLocationChanged && (cid = getCid(id)) != -1) {
            OemCellInfoMonitor.getDefault(this.mPhone.getContext()).addRecord(String.valueOf(cid));
        }
    }

    private static int getCid(CellIdentity id) {
        if (id == null) {
            return -1;
        }
        int cid = -1;
        int type = id.getType();
        if (type == 1) {
            cid = ((CellIdentityGsm) id).getCid();
        } else if (type == 3) {
            cid = ((CellIdentityLte) id).getCi();
        } else if (type == 4) {
            cid = ((CellIdentityWcdma) id).getCid();
        } else if (type == 5) {
            cid = ((CellIdentityTdscdma) id).getCid();
        }
        if (cid == Integer.MAX_VALUE) {
            return -1;
        }
        return cid;
    }

    private void isMatchServiceRegisterDuration() {
        try {
            OemCellInfoMonitor.getDefault(this.mPhone.getContext()).isMatchRegisterDuration(true);
        } catch (Exception e) {
        }
    }

    public void handleMessage(Message msg) {
        logd("handleMessage:" + msg.what);
        if (msg.what == 5000) {
            logd("EVENT_OPPO_CHANGED_NETWORK_LOCK_STATUS");
            if (OemLockUtils.isEnableUpRlock()) {
                isMatchServiceRegisterDuration();
                return;
            }
            Intent intent = new Intent(RegionLockConstant.ACTION_NETWORK_LOCK);
            intent.putExtra(RegionLockConstant.NETLOCK_STATUS, "0");
            intent.putExtra(RegionLockConstant.UNLOCK_TYPE, "1");
            sendBroadCastChangedNetlockStatus(intent);
            OemLockUtils.setRegionLockedStatus("0");
        }
    }

    /* access modifiers changed from: package-private */
    public void logd(String s) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(this.LOG_TAG, s);
        }
    }

    /* access modifiers changed from: package-private */
    public void loge(String s) {
        Rlog.e(this.LOG_TAG, s);
    }
}
