package com.qualcomm.qti.internal.telephony.primarycard;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.qualcomm.qti.internal.telephony.QtiUiccCardProvisioner;
import java.util.HashSet;
import java.util.Set;
import org.codeaurora.internal.IExtTelephony;
import org.codeaurora.internal.IExtTelephony.Stub;

public class SubsidyLockSettingsObserver extends ContentObserver {
    public static final int AP_LOCKED = 102;
    public static final int PERMANENTLY_UNLOCKED = 100;
    public static final int SUBSIDY_LOCKED = 101;
    private static final String SUBSIDY_LOCK_SETTINGS = "subsidy_status";
    private static final String SUBSIDY_LOCK_SYSTEM_PROPERY = "ro.radio.subsidylock";
    private static final String SUBSIDY_STATE_KEY = "device_Subsidy_state";
    public static final int SUBSIDY_UNLOCKED = 103;
    private static final String TAG = "SubsidyLockSettingsObserver";
    private static int mCurrentState = -1;
    private QtiCardInfoManager mCardInfoManager;
    private Context mContext;
    private QtiPrimaryCardPriorityHandler mPriorityHandler;
    private SubsidySettingsHandler mSettingsHandler;

    private class SIMDeactivationRecords {
        private static final String KEY_DEACTIVATION_RECORD = "key_deactivation_record";
        private static final String PREFS_NAME = "deactivation_record";
        private SharedPreferences mSharedPrefs;
        private Set<String> mSimRecords = null;

        public SIMDeactivationRecords() {
            this.mSharedPrefs = SubsidyLockSettingsObserver.this.mContext.getSharedPreferences(PREFS_NAME, 0);
            parse();
        }

        private void parse() {
            Set<String> simRecords = this.mSharedPrefs.getStringSet(KEY_DEACTIVATION_RECORD, new HashSet());
            this.mSimRecords = new HashSet();
            this.mSimRecords.addAll(simRecords);
        }

        public void addRecord(String subId) {
            if (!isDeactivated(subId)) {
                this.mSimRecords.add(subId);
                this.mSharedPrefs.edit().putStringSet(KEY_DEACTIVATION_RECORD, this.mSimRecords).commit();
            }
        }

        public void removeRecord(String subId) {
            if (this.mSimRecords.contains(subId)) {
                this.mSimRecords.remove(subId);
            }
            this.mSharedPrefs.edit().putStringSet(KEY_DEACTIVATION_RECORD, this.mSimRecords).commit();
        }

        public boolean isDeactivated(String subId) {
            return this.mSimRecords.contains(subId);
        }
    }

    private class SubsidySettingsHandler extends Handler {
        public static final int MSG_ALL_CARDS_READY = 4;
        public static final int MSG_EXIT = 3;
        public static final int MSG_LOCKED = 0;
        public static final int MSG_RESTRICTED = 1;
        public static final int MSG_SET_PRIMARY_CARD = 5;
        public static final int MSG_UNLOCKED = 2;
        static final int PROVISIONED = 1;
        static final int SUCCESS = 0;
        private int mNumSimSlots;

        public SubsidySettingsHandler(Looper looper) {
            super(looper);
            this.mNumSimSlots = 0;
            this.mNumSimSlots = TelephonyManager.getDefault().getPhoneCount();
        }

        public void handleMessage(Message msg) {
            Rlog.d(SubsidyLockSettingsObserver.TAG, " handleMessage, event  " + msg.what + " current state " + SubsidyLockSettingsObserver.mCurrentState);
            int i;
            SubscriptionInfo sir;
            switch (msg.what) {
                case 0:
                    IExtTelephony mExtTelephony = Stub.asInterface(ServiceManager.getService("extphone"));
                    i = 0;
                    while (i < this.mNumSimSlots) {
                        try {
                            sir = SubscriptionManager.from(SubsidyLockSettingsObserver.this.mContext).getActiveSubscriptionInfoForSimSlotIndex(i);
                            if (sir == null || sir.getMcc() == 0) {
                                Rlog.e(SubsidyLockSettingsObserver.TAG, "Invalid sub info for slot id: " + i + ", not proceeding further.");
                            } else if (mExtTelephony.isPrimaryCarrierSlotId(i)) {
                                QtiUiccCardProvisioner.make(SubsidyLockSettingsObserver.this.mContext).activateUiccCard(i);
                            } else if (mExtTelephony.getCurrentUiccCardProvisioningStatus(i) == 1 && QtiUiccCardProvisioner.make(SubsidyLockSettingsObserver.this.mContext).deactivateUiccCard(i) == 0) {
                                new SIMDeactivationRecords().addRecord(String.valueOf(sir.getSubscriptionId()));
                            }
                            i++;
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e2) {
                            e2.printStackTrace();
                        }
                    }
                    SubsidyLockSettingsObserver.this.updateDeviceState(SubsidyLockSettingsObserver.AP_LOCKED);
                    obtainMessage(3).sendToTarget();
                    return;
                case 1:
                    IExtTelephony mExtTelephony1 = Stub.asInterface(ServiceManager.getService("extphone"));
                    i = 0;
                    while (i < this.mNumSimSlots) {
                        try {
                            sir = SubscriptionManager.from(SubsidyLockSettingsObserver.this.mContext).getActiveSubscriptionInfoForSimSlotIndex(i);
                            if (sir == null || sir.getMcc() == 0) {
                                Rlog.e(SubsidyLockSettingsObserver.TAG, "Invalid subscription info for slot id: " + i + ", not proceeding further.");
                            } else if (!mExtTelephony1.isPrimaryCarrierSlotId(i)) {
                                SIMDeactivationRecords records = new SIMDeactivationRecords();
                                if (records.isDeactivated(String.valueOf(sir.getSubscriptionId())) && mExtTelephony1.activateUiccCard(i) == 0) {
                                    records.removeRecord(String.valueOf(sir.getSubscriptionId()));
                                }
                            } else if (SubsidyLockSettingsObserver.isSubsidyUnlocked(SubsidyLockSettingsObserver.this.mContext) && mExtTelephony1.isPrimaryCarrierSlotId(i)) {
                                QtiUiccCardProvisioner.make(SubsidyLockSettingsObserver.this.mContext).activateUiccCard(i);
                            }
                            i++;
                        } catch (RemoteException e3) {
                            e3.printStackTrace();
                        } catch (NullPointerException e22) {
                            e22.printStackTrace();
                        }
                    }
                    if (msg.obj != null) {
                        SubsidyLockSettingsObserver.this.updateDeviceState(((Integer) msg.obj).intValue());
                    }
                    obtainMessage(3).sendToTarget();
                    return;
                case 2:
                    obtainMessage(1, Integer.valueOf(100)).sendToTarget();
                    return;
                case 3:
                    if (getLooper() != Looper.getMainLooper()) {
                        getLooper().quitSafely();
                        return;
                    }
                    return;
                case 4:
                    SubsidyLockSettingsObserver.this.onChange(false);
                    return;
                case 5:
                    QtiPrimaryCardController.getInstance().trySetPrimarySub();
                    return;
                default:
                    return;
            }
        }
    }

    public SubsidyLockSettingsObserver(Context context) {
        super(null);
        this.mContext = context;
        mCurrentState = PreferenceManager.getDefaultSharedPreferences(this.mContext).getInt(SUBSIDY_STATE_KEY, mCurrentState);
        Rlog.d(TAG, " in constructor, context =  " + this.mContext + " device state " + mCurrentState);
    }

    public void observe(QtiCardInfoManager cardInfoManager, QtiPrimaryCardPriorityHandler cardPriorityHandler) {
        ContentResolver resolver = this.mContext.getContentResolver();
        this.mCardInfoManager = cardInfoManager;
        this.mPriorityHandler = cardPriorityHandler;
        resolver.registerContentObserver(Secure.getUriFor(SUBSIDY_LOCK_SETTINGS), false, this);
        registerAllCardsAvailableCallback();
    }

    private void registerAllCardsAvailableCallback() {
        if (this.mSettingsHandler == null) {
            this.mSettingsHandler = new SubsidySettingsHandler(Looper.getMainLooper());
            this.mCardInfoManager.registerAllCardsInfoAvailable(this.mSettingsHandler, 4, null);
        }
    }

    public void onChange(boolean selfChange) {
        if (this.mPriorityHandler != null) {
            this.mPriorityHandler.reloadPriorityConfig();
            this.mPriorityHandler.loadCurrentPriorityConfigs(true);
        }
        HandlerThread thread = new HandlerThread("Subsidy Settings handler thread");
        thread.start();
        Handler handler = new SubsidySettingsHandler(thread.getLooper());
        if (isSubsidyLocked(this.mContext)) {
            handler.obtainMessage(0).sendToTarget();
        } else if (isPermanentlyUnlocked(this.mContext)) {
            handler.obtainMessage(2).sendToTarget();
        } else if (isSubsidyUnlocked(this.mContext)) {
            handler.obtainMessage(1, Integer.valueOf(SUBSIDY_UNLOCKED)).sendToTarget();
        }
    }

    public static boolean isSubsidyLocked(Context context) {
        int subsidyLock = Secure.getInt(context.getContentResolver(), SUBSIDY_LOCK_SETTINGS, -1);
        if (subsidyLock == SUBSIDY_LOCKED || subsidyLock == AP_LOCKED) {
            return true;
        }
        return false;
    }

    public static boolean isSubsidyUnlocked(Context context) {
        return Secure.getInt(context.getContentResolver(), SUBSIDY_LOCK_SETTINGS, -1) == SUBSIDY_UNLOCKED;
    }

    public static boolean isPermanentlyUnlocked(Context context) {
        return Secure.getInt(context.getContentResolver(), SUBSIDY_LOCK_SETTINGS, -1) == 100;
    }

    private void updateDeviceState(int newState) {
        if (mCurrentState != newState) {
            Rlog.d(TAG, " updateDeviceState, new state  " + newState);
            if ((newState == 100 || mCurrentState == 100) && this.mPriorityHandler != null) {
                this.mPriorityHandler.reloadPriorityConfig();
                this.mPriorityHandler.loadCurrentPriorityConfigs(true);
            }
            if (mCurrentState == 100 && (newState == AP_LOCKED || newState == SUBSIDY_UNLOCKED)) {
                this.mSettingsHandler.obtainMessage(5).sendToTarget();
            } else if (newState == 100) {
                QtiPrimaryCardController.getInstance().saveUserSelectionMode();
            }
            saveDeviceState(newState);
        }
    }

    private void saveDeviceState(int newState) {
        mCurrentState = newState;
        PreferenceManager.getDefaultSharedPreferences(this.mContext).edit().putInt(SUBSIDY_STATE_KEY, newState).commit();
    }

    public static boolean isSubsidyLockFeatureEnabled() {
        if (SystemProperties.getInt(SUBSIDY_LOCK_SYSTEM_PROPERY, 0) == 1) {
            return true;
        }
        return false;
    }
}
