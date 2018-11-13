package org.codeaurora.ims.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class QtiCarrierConfigHelper {
    private static int PHONE_COUNT = TelephonyManager.getDefault().getPhoneCount();
    private static final String TAG = QtiCarrierConfigHelper.class.getSimpleName();
    private CarrierConfigManager mCarrierConfigManager;
    private Map<Integer, PersistableBundle> mConfigsMap;
    private Context mContext;
    private AtomicBoolean mInitialized;
    private final OnSubscriptionsChangedListener mOnSubscriptionsChangeListener;
    private final BroadcastReceiver mReceiver;
    private SubscriptionManager mSubscriptionManager;

    private static class SingletonHolder {
        public static final QtiCarrierConfigHelper sInstance = new QtiCarrierConfigHelper();

        private SingletonHolder() {
        }
    }

    /* synthetic */ QtiCarrierConfigHelper(QtiCarrierConfigHelper -this0) {
        this();
    }

    private QtiCarrierConfigHelper() {
        this.mConfigsMap = new ConcurrentHashMap();
        this.mInitialized = new AtomicBoolean(false);
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    int subId = intent.getIntExtra("subscription", -1);
                    if (QtiCarrierConfigHelper.this.mSubscriptionManager != null) {
                        SubscriptionInfo subInfo = QtiCarrierConfigHelper.this.mSubscriptionManager.getActiveSubscriptionInfo(subId);
                        if (subInfo != null) {
                            Log.d(QtiCarrierConfigHelper.TAG, "Reload carrier configs on sub Id: " + subId);
                            QtiCarrierConfigHelper.this.loadConfigsForSubInfo(subInfo);
                            return;
                        }
                        int phoneId = intent.getIntExtra("phone", -1);
                        if (QtiCarrierConfigHelper.this.mCarrierConfigManager != null && QtiCarrierConfigHelper.this.mCarrierConfigManager.getConfigForSubId(subId) == null) {
                            QtiCarrierConfigHelper.this.mConfigsMap.remove(Integer.valueOf(phoneId));
                            Log.d(QtiCarrierConfigHelper.TAG, "Clear carrier configs on phone Id: " + phoneId);
                        }
                    }
                }
            }
        };
        this.mOnSubscriptionsChangeListener = new OnSubscriptionsChangedListener() {
            public void onSubscriptionsChanged() {
                if (QtiCarrierConfigHelper.this.mSubscriptionManager != null) {
                    List<SubscriptionInfo> subInfos = QtiCarrierConfigHelper.this.mSubscriptionManager.getActiveSubscriptionInfoList();
                    if (subInfos != null) {
                        for (SubscriptionInfo subInfo : subInfos) {
                            Log.d(QtiCarrierConfigHelper.TAG, "Reload carrier configs on sub Id due sub changed: " + subInfo.getSubscriptionId());
                            QtiCarrierConfigHelper.this.loadConfigsForSubInfo(subInfo);
                        }
                    }
                }
            }
        };
    }

    public static QtiCarrierConfigHelper getInstance() {
        return SingletonHolder.sInstance;
    }

    public void setup(Context context) {
        if (context != null) {
            this.mContext = context.getApplicationContext();
            if (this.mContext != null) {
                this.mInitialized.set(true);
                this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
                this.mCarrierConfigManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
                List<SubscriptionInfo> subInfos = this.mSubscriptionManager.getActiveSubscriptionInfoList();
                if (subInfos != null) {
                    for (SubscriptionInfo subInfo : subInfos) {
                        loadConfigsForSubInfo(subInfo);
                    }
                }
                this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"));
                this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
            }
        }
    }

    public void teardown() {
        this.mConfigsMap.clear();
        this.mInitialized.set(false);
        if (this.mContext != null) {
            this.mContext.unregisterReceiver(this.mReceiver);
            if (this.mSubscriptionManager != null) {
                this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
            }
        }
    }

    private void loadConfigsForSubInfo(SubscriptionInfo subInfo) {
        if (subInfo != null && this.mCarrierConfigManager != null) {
            PersistableBundle pb = this.mCarrierConfigManager.getConfigForSubId(subInfo.getSubscriptionId());
            if (pb != null) {
                Log.d(TAG, "Load carrier configs on sub Id: " + subInfo.getSubscriptionId() + " slot Id: " + subInfo.getSimSlotIndex());
                this.mConfigsMap.put(Integer.valueOf(subInfo.getSimSlotIndex()), pb);
                return;
            }
            Log.d(TAG, "No configs on sub Id: " + subInfo.getSubscriptionId());
            this.mConfigsMap.put(Integer.valueOf(subInfo.getSimSlotIndex()), null);
        }
    }

    private void sanityCheckConfigsLoaded(Context context, int phoneId) {
        if (context != null && this.mInitialized.compareAndSet(false, true)) {
            setup(context);
        }
    }

    public boolean isValidPhoneId(int phoneId) {
        return phoneId >= 0 && phoneId < PHONE_COUNT;
    }

    public boolean getBoolean(Context context, int phoneId, String key) {
        if (isValidPhoneId(phoneId)) {
            sanityCheckConfigsLoaded(context, phoneId);
            PersistableBundle pb = (PersistableBundle) this.mConfigsMap.get(Integer.valueOf(phoneId));
            if (pb != null) {
                return pb.getBoolean(key, false);
            }
            Log.d(TAG, "WARNING, no carrier configs on phone Id: " + phoneId);
            return false;
        }
        Log.d(TAG, "Invalid phone ID: " + phoneId);
        return false;
    }

    public boolean getBoolean(int phoneId, String key) {
        if (isValidPhoneId(phoneId)) {
            PersistableBundle pb = (PersistableBundle) this.mConfigsMap.get(Integer.valueOf(phoneId));
            if (pb != null) {
                return pb.getBoolean(key, false);
            }
            if (this.mInitialized.get()) {
                Log.d(TAG, "WARNING, no carrier configs on phone Id: " + phoneId);
                return false;
            }
            Log.d(TAG, "WARNING, Don't set up yet.");
            return false;
        }
        Log.d(TAG, "Invalid phone ID: " + phoneId);
        return false;
    }
}
