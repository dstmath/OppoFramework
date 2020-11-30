package com.mediatek.internal.telephony;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.MultiSimSettingController;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.util.ArrayUtils;
import com.mediatek.internal.telephony.datasub.DataSubSelector;
import com.mediatek.internal.telephony.datasub.DataSubSelectorUtil;
import com.mediatek.internal.telephony.datasub.IDataSubSelectorOPExt;
import java.util.List;

public class MtkMultiSimSettingController extends MultiSimSettingController {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "MtkMultiSimSettingController";
    private static final int NO_SIM_STRING_LENGTH = 3;

    public MtkMultiSimSettingController(Context context, SubscriptionController sc) {
        super(context, sc);
    }

    /* access modifiers changed from: protected */
    public void onUserDataEnabled(int subId, boolean enable) {
        log("onUserDataEnabled");
        setUserDataEnabledForGroup(subId, enable);
    }

    /* access modifiers changed from: protected */
    public void updateDefaults(boolean init) {
        log("updateDefaults");
        if (this.mSubInfoInitialized) {
            List<SubscriptionInfo> activeSubInfos = this.mSubController.getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            IDataSubSelectorOPExt opExt = DataSubSelector.getDataSubSelectorOpExt();
            boolean followAospData = opExt == null || opExt.enableAospDefaultDataUpdate();
            log("updateDefaults, followAospData:" + followAospData);
            if (ArrayUtils.isEmpty(activeSubInfos)) {
                this.mPrimarySubList.clear();
                log("[updateDefaultValues] No active sub. Setting default to INVALID sub.");
                if (followAospData) {
                    this.mSubController.setDefaultDataSubId(-1);
                }
                this.mSubController.setDefaultVoiceSubId(-1);
                this.mSubController.setDefaultSmsSubId(-1);
            } else if (!isAnySimNotReady(activeSubInfos.size())) {
                int change = updatePrimarySubListAndGetChangeType(activeSubInfos, init);
                log("[updateDefaultValues] change: " + change);
                if (change != 0) {
                    if (this.mPrimarySubList.size() != 1 || change == 2) {
                        log("[updateDefaultValues] records: " + this.mPrimarySubList);
                        log("[updateDefaultValues] Update default data subscription");
                        boolean dataSelected = false;
                        if (followAospData) {
                            dataSelected = updateDefaultValue(this.mPrimarySubList, this.mSubController.getDefaultDataSubId(), new MultiSimSettingController.UpdateDefaultAction() {
                                /* class com.mediatek.internal.telephony.$$Lambda$MtkMultiSimSettingController$AI6NzmRf7535747tm8xElzUEJc */

                                public final void update(int i) {
                                    MtkMultiSimSettingController.this.lambda$updateDefaults$0$MtkMultiSimSettingController(i);
                                }
                            });
                        }
                        log("[updateDefaultValues] Update default voice subscription");
                        boolean voiceSelected = updateDefaultValue(this.mPrimarySubList, this.mSubController.getDefaultVoiceSubId(), new MultiSimSettingController.UpdateDefaultAction() {
                            /* class com.mediatek.internal.telephony.$$Lambda$MtkMultiSimSettingController$aydnFdVZUnBZRMShX89XhueYFSA */

                            public final void update(int i) {
                                MtkMultiSimSettingController.this.lambda$updateDefaults$1$MtkMultiSimSettingController(i);
                            }
                        });
                        log("[updateDefaultValues] Update default sms subscription");
                        sendSubChangeNotificationIfNeeded(change, dataSelected, voiceSelected, updateDefaultValue(this.mPrimarySubList, this.mSubController.getDefaultSmsSubId(), new MultiSimSettingController.UpdateDefaultAction() {
                            /* class com.mediatek.internal.telephony.$$Lambda$MtkMultiSimSettingController$1kbBkQ2TgfwuOezLd0Y6zb3PWSg */

                            public final void update(int i) {
                                MtkMultiSimSettingController.this.lambda$updateDefaults$2$MtkMultiSimSettingController(i);
                            }
                        }));
                        return;
                    }
                    int subId = ((Integer) this.mPrimarySubList.get(0)).intValue();
                    log("[updateDefaultValues] to only primary sub " + subId);
                    if (followAospData) {
                        this.mSubController.setDefaultDataSubId(subId);
                    }
                    this.mSubController.setDefaultVoiceSubId(subId);
                    this.mSubController.setDefaultSmsSubId(subId);
                }
            }
        }
    }

    public /* synthetic */ void lambda$updateDefaults$0$MtkMultiSimSettingController(int newValue) {
        this.mSubController.setDefaultDataSubId(newValue);
    }

    public /* synthetic */ void lambda$updateDefaults$1$MtkMultiSimSettingController(int newValue) {
        this.mSubController.setDefaultVoiceSubId(newValue);
    }

    public /* synthetic */ void lambda$updateDefaults$2$MtkMultiSimSettingController(int newValue) {
        this.mSubController.setDefaultSmsSubId(newValue);
    }

    /* access modifiers changed from: protected */
    public int getSimSelectDialogType(int change, boolean dataSelected, boolean voiceSelected, boolean smsSelected) {
        int dialogType = MtkMultiSimSettingController.super.getSimSelectDialogType(change, dataSelected, voiceSelected, smsSelected);
        if (dialogType != 0 || this.mPrimarySubList.size() <= 1 || change != 6 || dataSelected) {
            return dialogType;
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public void disableDataForNonDefaultNonOpportunisticSubscriptions() {
    }

    private boolean isAnySimNotReady(int subCount) {
        int simCount = 0;
        int maxCount = TelephonyManager.getDefault().getSimCount();
        for (int i = 0; i < maxCount; i++) {
            String iccid = SystemProperties.get(DataSubSelectorUtil.PROPERTY_ICCID[i], "");
            if (iccid.equals("")) {
                log("Not ready sim exist:" + i + ", max:" + maxCount);
                return true;
            }
            if (iccid.length() > 3) {
                simCount++;
            }
        }
        log("simCount:" + simCount);
        if (subCount != simCount) {
            return true;
        }
        return false;
    }

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
