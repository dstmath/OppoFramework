package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Binder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.ImsiEncryptionInfo;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.IPhoneSubInfo;
import com.android.internal.telephony.uicc.IsimRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;

public class PhoneSubInfoController extends IPhoneSubInfo.Stub {
    private static final boolean DBG = true;
    private static final String TAG = "PhoneSubInfoController";
    private static final boolean VDBG = false;
    private final AppOpsManager mAppOps;
    @UnsupportedAppUsage
    private final Context mContext;
    @UnsupportedAppUsage
    private final Phone[] mPhone;

    /* access modifiers changed from: private */
    public interface CallPhoneMethodHelper<T> {
        T callMethod(Phone phone);
    }

    /* access modifiers changed from: private */
    public interface PermissionCheckHelper {
        boolean checkPermission(Context context, int i, String str, String str2);
    }

    public PhoneSubInfoController(Context context, Phone[] phone) {
        this.mPhone = phone;
        if (ServiceManager.getService("iphonesubinfo") == null) {
            ServiceManager.addService("iphonesubinfo", this);
        }
        this.mContext = context;
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
    }

    public String getDeviceId(String callingPackage) {
        return getDeviceIdForPhone(SubscriptionManager.getPhoneId(getDefaultSubscription()), callingPackage);
    }

    public String getDeviceIdForPhone(int phoneId, String callingPackage) {
        return (String) callPhoneMethodForPhoneIdWithReadDeviceIdentifiersCheck(phoneId, callingPackage, "getDeviceId", $$Lambda$PhoneSubInfoController$LX6rN0XZFTVXkDiHGVCozgs8kHU.INSTANCE);
    }

    public String getNaiForSubscriber(int subId, String callingPackage) {
        return (String) callPhoneMethodForSubIdWithReadCheck(subId, callingPackage, "getNai", $$Lambda$PhoneSubInfoController$AAs5l6UPqOJI6iOy7O7wnhNgpN4.INSTANCE);
    }

    public String getImeiForSubscriber(int subId, String callingPackage) {
        return (String) callPhoneMethodForSubIdWithReadDeviceIdentifiersCheck(subId, callingPackage, "getImei", $$Lambda$PhoneSubInfoController$_djiy1W26lRIJyfoQefqkIQNgSU.INSTANCE);
    }

    public ImsiEncryptionInfo getCarrierInfoForImsiEncryption(int subId, int keyType, String callingPackage) {
        return (ImsiEncryptionInfo) callPhoneMethodForSubIdWithReadCheck(subId, callingPackage, "getCarrierInfoForImsiEncryption", new CallPhoneMethodHelper(keyType) {
            /* class com.android.internal.telephony.$$Lambda$PhoneSubInfoController$AjZFvwh3Ujx5W3fleFNksc6bLf0 */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // com.android.internal.telephony.PhoneSubInfoController.CallPhoneMethodHelper
            public final Object callMethod(Phone phone) {
                return phone.getCarrierInfoForImsiEncryption(this.f$0);
            }
        });
    }

    public void setCarrierInfoForImsiEncryption(int subId, String callingPackage, ImsiEncryptionInfo imsiEncryptionInfo) {
        callPhoneMethodForSubIdWithModifyCheck(subId, callingPackage, "setCarrierInfoForImsiEncryption", new CallPhoneMethodHelper(imsiEncryptionInfo) {
            /* class com.android.internal.telephony.$$Lambda$PhoneSubInfoController$ChCf_gnGN3K5prBkykg6tWs0aTk */
            private final /* synthetic */ ImsiEncryptionInfo f$0;

            {
                this.f$0 = r1;
            }

            @Override // com.android.internal.telephony.PhoneSubInfoController.CallPhoneMethodHelper
            public final Object callMethod(Phone phone) {
                return phone.setCarrierInfoForImsiEncryption(this.f$0);
            }
        });
    }

    public void resetCarrierKeysForImsiEncryption(int subId, String callingPackage) {
        callPhoneMethodForSubIdWithModifyCheck(subId, callingPackage, "setCarrierInfoForImsiEncryption", $$Lambda$PhoneSubInfoController$Pb4HmeqsjasrNaXBByGh_CFogk.INSTANCE);
    }

    public String getDeviceSvn(String callingPackage) {
        return getDeviceSvnUsingSubId(getDefaultSubscription(), callingPackage);
    }

    public String getDeviceSvnUsingSubId(int subId, String callingPackage) {
        return (String) callPhoneMethodForSubIdWithReadCheck(subId, callingPackage, "getDeviceSvn", $$Lambda$PhoneSubInfoController$VgStcgP2F9IDb29Rx_E2o89A7U.INSTANCE);
    }

    public String getSubscriberId(String callingPackage) {
        return getSubscriberIdForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getSubscriberIdForSubscriber(int subId, String callingPackage) {
        long identity = Binder.clearCallingIdentity();
        try {
            if (SubscriptionController.getInstance().isActiveSubId(subId, callingPackage)) {
                String subscriberId = (String) callPhoneMethodForSubIdWithReadSubscriberIdentifiersCheck(subId, callingPackage, "getSubscriberId", $$Lambda$PhoneSubInfoController$2WGP2Bp11k7_Xwi1N4YefElOUuM.INSTANCE);
                if (subscriberId != null || !PhoneFactory.inWhiteList(callingPackage)) {
                    return subscriberId;
                }
                return (String) oppoCallPhoneMethodWithoutPermissionCheck(subId, callingPackage, "getSubscriberId", $$Lambda$PhoneSubInfoController$G1DZ7WURkYLLj1kmBrzQ6Xrggg.INSTANCE);
            } else if (!TelephonyPermissions.checkCallingOrSelfReadSubscriberIdentifiers(this.mContext, subId, callingPackage, "getSubscriberId") && !PhoneFactory.inWhiteList(callingPackage)) {
                return null;
            } else {
                long identity2 = Binder.clearCallingIdentity();
                try {
                    return SubscriptionController.getInstance().getImsiPrivileged(subId);
                } finally {
                    Binder.restoreCallingIdentity(identity2);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public String getIccSerialNumber(String callingPackage) {
        return getIccSerialNumberForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getIccSerialNumberForSubscriber(int subId, String callingPackage) {
        String number = (String) callPhoneMethodForSubIdWithReadSubscriberIdentifiersCheck(subId, callingPackage, "getIccSerialNumber", $$Lambda$PhoneSubInfoController$CqLkDHfQeI52TPrvqxgkvVKaKLo.INSTANCE);
        if (number != null || !PhoneFactory.inWhiteList(callingPackage)) {
            return number;
        }
        log("getIccSerialNumberForSubscriber inWhiteList : " + callingPackage);
        return (String) oppoCallPhoneMethodWithoutPermissionCheck(subId, callingPackage, "getIccSerialNumber", $$Lambda$PhoneSubInfoController$GoG_1QtT5yeOwq7BMcy53gtmyM.INSTANCE);
    }

    public String getLine1Number(String callingPackage) {
        return getLine1NumberForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getLine1NumberForSubscriber(int subId, String callingPackage) {
        return (String) callPhoneMethodForSubIdWithReadPhoneNumberCheck(subId, callingPackage, "getLine1Number", $$Lambda$PhoneSubInfoController$hC9HM02Vg4zx4JInC7q7yEbsdcI.INSTANCE);
    }

    public String getLine1AlphaTag(String callingPackage) {
        return getLine1AlphaTagForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getLine1AlphaTagForSubscriber(int subId, String callingPackage) {
        return (String) callPhoneMethodForSubIdWithReadCheck(subId, callingPackage, "getLine1AlphaTag", $$Lambda$PhoneSubInfoController$SeHGhJFoeG6ijgNiB5U2_kUFDJA.INSTANCE);
    }

    public String getMsisdn(String callingPackage) {
        return getMsisdnForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getMsisdnForSubscriber(int subId, String callingPackage) {
        return (String) callPhoneMethodForSubIdWithReadCheck(subId, callingPackage, "getMsisdn", $$Lambda$PhoneSubInfoController$F_7AE9rR3Bccc0JeeVIpjD2KVAE.INSTANCE);
    }

    public String getVoiceMailNumber(String callingPackage) {
        return getVoiceMailNumberForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getVoiceMailNumberForSubscriber(int subId, String callingPackage) {
        return (String) callPhoneMethodForSubIdWithReadCheck(subId, callingPackage, "getVoiceMailNumber", new CallPhoneMethodHelper() {
            /* class com.android.internal.telephony.$$Lambda$PhoneSubInfoController$aw73rTBHu0X0WqvInc1xp7uTFrc */

            @Override // com.android.internal.telephony.PhoneSubInfoController.CallPhoneMethodHelper
            public final Object callMethod(Phone phone) {
                return PhoneSubInfoController.this.lambda$getVoiceMailNumberForSubscriber$14$PhoneSubInfoController(phone);
            }
        });
    }

    public /* synthetic */ String lambda$getVoiceMailNumberForSubscriber$14$PhoneSubInfoController(Phone phone) {
        return PhoneNumberUtils.extractNetworkPortion(phone.getVoiceMailNumber());
    }

    public String getVoiceMailAlphaTag(String callingPackage) {
        return getVoiceMailAlphaTagForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getVoiceMailAlphaTagForSubscriber(int subId, String callingPackage) {
        return (String) callPhoneMethodForSubIdWithReadCheck(subId, callingPackage, "getVoiceMailAlphaTag", $$Lambda$PhoneSubInfoController$xO2ljDGld3kMgltafF9QZGKsaI.INSTANCE);
    }

    @UnsupportedAppUsage
    private Phone getPhone(int subId) {
        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            phoneId = 0;
        }
        return this.mPhone[phoneId];
    }

    private void enforcePrivilegedPermissionOrCarrierPrivilege(int subId, String message) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE") != 0) {
            TelephonyPermissions.enforceCallingOrSelfCarrierPrivilege(subId, message);
        }
    }

    private void enforceModifyPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "Requires MODIFY_PHONE_STATE");
    }

    @UnsupportedAppUsage
    private int getDefaultSubscription() {
        return PhoneFactory.getDefaultSubscription();
    }

    public String getIsimImpi(int subId) {
        return (String) callPhoneMethodForSubIdWithPrivilegedCheck(subId, "getIsimImpi", $$Lambda$PhoneSubInfoController$kp09UCYxVSn7nVQQIbjm7ThlroE.INSTANCE);
    }

    static /* synthetic */ String lambda$getIsimImpi$16(Phone phone) {
        IsimRecords isim = phone.getIsimRecords();
        if (isim != null) {
            return isim.getIsimImpi();
        }
        return null;
    }

    public String getIsimDomain(int subId) {
        return (String) callPhoneMethodForSubIdWithPrivilegedCheck(subId, "getIsimDomain", $$Lambda$PhoneSubInfoController$kr9evJwg39y1RLkyPnWeeqLSLIk.INSTANCE);
    }

    static /* synthetic */ String lambda$getIsimDomain$17(Phone phone) {
        IsimRecords isim = phone.getIsimRecords();
        if (isim != null) {
            return isim.getIsimDomain();
        }
        return null;
    }

    public String[] getIsimImpu(int subId) {
        return (String[]) callPhoneMethodForSubIdWithPrivilegedCheck(subId, "getIsimImpu", $$Lambda$PhoneSubInfoController$noiasAbmtY_Ny_e8w06oXH9tVig.INSTANCE);
    }

    static /* synthetic */ String[] lambda$getIsimImpu$18(Phone phone) {
        IsimRecords isim = phone.getIsimRecords();
        if (isim != null) {
            return isim.getIsimImpu();
        }
        return null;
    }

    public String getIsimIst(int subId) throws RemoteException {
        return (String) callPhoneMethodForSubIdWithPrivilegedCheck(subId, "getIsimIst", $$Lambda$PhoneSubInfoController$jNvm1bV9rqY4StuIOFOGfGCWhH4.INSTANCE);
    }

    static /* synthetic */ String lambda$getIsimIst$19(Phone phone) {
        IsimRecords isim = phone.getIsimRecords();
        if (isim != null) {
            return isim.getIsimIst();
        }
        return null;
    }

    public String[] getIsimPcscf(int subId) throws RemoteException {
        return (String[]) callPhoneMethodForSubIdWithPrivilegedCheck(subId, "getIsimPcscf", $$Lambda$PhoneSubInfoController$fH8RePvX1PAoHrDbOJZfSRtvhUQ.INSTANCE);
    }

    static /* synthetic */ String[] lambda$getIsimPcscf$20(Phone phone) {
        IsimRecords isim = phone.getIsimRecords();
        if (isim != null) {
            return isim.getIsimPcscf();
        }
        return null;
    }

    public String getIccSimChallengeResponse(int subId, int appType, int authType, String data) throws RemoteException {
        return (String) callPhoneMethodWithPermissionCheck(subId, null, "getIccSimChallengeResponse", new CallPhoneMethodHelper(appType, authType, data) {
            /* class com.android.internal.telephony.$$Lambda$PhoneSubInfoController$9BYfrUh0D6hdp5RVrPYkJmLz08 */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ String f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.internal.telephony.PhoneSubInfoController.CallPhoneMethodHelper
            public final Object callMethod(Phone phone) {
                return PhoneSubInfoController.this.lambda$getIccSimChallengeResponse$21$PhoneSubInfoController(this.f$1, this.f$2, this.f$3, phone);
            }
        }, new PermissionCheckHelper() {
            /* class com.android.internal.telephony.$$Lambda$PhoneSubInfoController$6zUZoyC6xtE2eiRpIhVKXEjXw */

            @Override // com.android.internal.telephony.PhoneSubInfoController.PermissionCheckHelper
            public final boolean checkPermission(Context context, int i, String str, String str2) {
                return PhoneSubInfoController.this.lambda$getIccSimChallengeResponse$22$PhoneSubInfoController(context, i, str, str2);
            }
        });
    }

    public /* synthetic */ String lambda$getIccSimChallengeResponse$21$PhoneSubInfoController(int appType, int authType, String data, Phone phone) {
        UiccCard uiccCard = phone.getUiccCard();
        if (uiccCard == null) {
            loge("getIccSimChallengeResponse() UiccCard is null");
            return null;
        }
        UiccCardApplication uiccApp = uiccCard.getApplicationByType(appType);
        if (uiccApp == null) {
            loge("getIccSimChallengeResponse() no app with specified type -- " + appType);
            return null;
        }
        loge("getIccSimChallengeResponse() found app " + uiccApp.getAid() + " specified type -- " + appType);
        if (authType == 128 || authType == 129) {
            return uiccApp.getIccRecords().getIccSimChallengeResponse(authType, data);
        }
        loge("getIccSimChallengeResponse() unsupported authType: " + authType);
        return null;
    }

    public /* synthetic */ boolean lambda$getIccSimChallengeResponse$22$PhoneSubInfoController(Context aContext, int aSubId, String aCallingPackage, String aMessage) {
        enforcePrivilegedPermissionOrCarrierPrivilege(aSubId, aMessage);
        return true;
    }

    public String getGroupIdLevel1ForSubscriber(int subId, String callingPackage) {
        return (String) callPhoneMethodForSubIdWithReadCheck(subId, callingPackage, "getGroupIdLevel1", $$Lambda$PhoneSubInfoController$zcjb_jqlqKFg3Necw0n9qhhQT8.INSTANCE);
    }

    private <T> T callPhoneMethodWithPermissionCheck(int subId, String callingPackage, String message, CallPhoneMethodHelper<T> callMethodHelper, PermissionCheckHelper permissionCheckHelper) {
        if (!permissionCheckHelper.checkPermission(this.mContext, subId, callingPackage, message)) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            Phone phone = getPhone(subId);
            if (phone != null) {
                return callMethodHelper.callMethod(phone);
            }
            loge(message + " phone is null for Subscription:" + subId);
            Binder.restoreCallingIdentity(identity);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private <T> T callPhoneMethodForSubIdWithReadCheck(int subId, String callingPackage, String message, CallPhoneMethodHelper<T> callMethodHelper) {
        return callPhoneMethodWithPermissionCheck(subId, callingPackage, message, callMethodHelper, $$Lambda$PhoneSubInfoController$po8_4IMWtKcwFfhE86w8JrxFyiQ.INSTANCE);
    }

    private <T> T callPhoneMethodForSubIdWithReadDeviceIdentifiersCheck(int subId, String callingPackage, String message, CallPhoneMethodHelper<T> callMethodHelper) {
        return callPhoneMethodWithPermissionCheck(subId, callingPackage, message, callMethodHelper, $$Lambda$PhoneSubInfoController$7qVhar26kNZifDiwBbDMBF7rSAU.INSTANCE);
    }

    private <T> T callPhoneMethodForSubIdWithReadSubscriberIdentifiersCheck(int subId, String callingPackage, String message, CallPhoneMethodHelper<T> callMethodHelper) {
        return callPhoneMethodWithPermissionCheck(subId, callingPackage, message, callMethodHelper, $$Lambda$PhoneSubInfoController$Tindg8C4HeRTS8KeokNuT5s_py0.INSTANCE);
    }

    private <T> T callPhoneMethodForSubIdWithPrivilegedCheck(int subId, String message, CallPhoneMethodHelper<T> callMethodHelper) {
        return callPhoneMethodWithPermissionCheck(subId, null, message, callMethodHelper, new PermissionCheckHelper(message) {
            /* class com.android.internal.telephony.$$Lambda$PhoneSubInfoController$7ZsXb7urOcs2WbxM3YMLmSSy_pw */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.PhoneSubInfoController.PermissionCheckHelper
            public final boolean checkPermission(Context context, int i, String str, String str2) {
                return PhoneSubInfoController.this.lambda$callPhoneMethodForSubIdWithPrivilegedCheck$27$PhoneSubInfoController(this.f$1, context, i, str, str2);
            }
        });
    }

    public /* synthetic */ boolean lambda$callPhoneMethodForSubIdWithPrivilegedCheck$27$PhoneSubInfoController(String message, Context aContext, int aSubId, String aCallingPackage, String aMessage) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", message);
        return true;
    }

    private <T> T callPhoneMethodForSubIdWithModifyCheck(int subId, String callingPackage, String message, CallPhoneMethodHelper<T> callMethodHelper) {
        return callPhoneMethodWithPermissionCheck(subId, null, message, callMethodHelper, new PermissionCheckHelper() {
            /* class com.android.internal.telephony.$$Lambda$PhoneSubInfoController$AuNbWweDZ9jyUKruuo74jW32vo */

            @Override // com.android.internal.telephony.PhoneSubInfoController.PermissionCheckHelper
            public final boolean checkPermission(Context context, int i, String str, String str2) {
                return PhoneSubInfoController.this.lambda$callPhoneMethodForSubIdWithModifyCheck$28$PhoneSubInfoController(context, i, str, str2);
            }
        });
    }

    public /* synthetic */ boolean lambda$callPhoneMethodForSubIdWithModifyCheck$28$PhoneSubInfoController(Context aContext, int aSubId, String aCallingPackage, String aMessage) {
        enforceModifyPermission();
        return true;
    }

    private <T> T callPhoneMethodForSubIdWithReadPhoneNumberCheck(int subId, String callingPackage, String message, CallPhoneMethodHelper<T> callMethodHelper) {
        return callPhoneMethodWithPermissionCheck(subId, callingPackage, message, callMethodHelper, $$Lambda$PhoneSubInfoController$w3DDNtOVgadzMRbV4Mpy0PHCNbU.INSTANCE);
    }

    private <T> T callPhoneMethodForPhoneIdWithReadDeviceIdentifiersCheck(int phoneId, String callingPackage, String message, CallPhoneMethodHelper<T> callMethodHelper) {
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            phoneId = 0;
        }
        Phone phone = this.mPhone[phoneId];
        if (phone == null || !TelephonyPermissions.checkCallingOrSelfReadDeviceIdentifiers(this.mContext, phone.getSubId(), callingPackage, message)) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            return callMethodHelper.callMethod(phone);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private <T> T oppoCallPhoneMethodWithoutPermissionCheck(int subId, String callingPackage, String message, CallPhoneMethodHelper<T> callMethodHelper) {
        long identity = Binder.clearCallingIdentity();
        try {
            Phone phone = getPhone(subId);
            if (phone != null) {
                return callMethodHelper.callMethod(phone);
            }
            loge(message + "oppo phone is null for Subscription:" + subId);
            Binder.restoreCallingIdentity(identity);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void log(String s) {
        Rlog.d(TAG, s);
    }

    @UnsupportedAppUsage
    private void loge(String s) {
        Rlog.e(TAG, s);
    }
}
