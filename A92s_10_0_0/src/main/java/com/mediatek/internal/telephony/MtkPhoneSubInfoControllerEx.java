package com.mediatek.internal.telephony;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Binder;
import android.os.Message;
import android.os.ServiceManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyPermissions;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IsimRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UsimServiceTable;
import com.mediatek.internal.telephony.IMtkPhoneSubInfoEx;
import com.mediatek.internal.telephony.uicc.MtkIsimUiccRecords;

public class MtkPhoneSubInfoControllerEx extends IMtkPhoneSubInfoEx.Stub {
    private static final boolean DBG = true;
    private static final String TAG = "MtkPhoneSubInfoCtlEx";
    private static final boolean VDBG = false;
    private final AppOpsManager mAppOps;
    private final Context mContext;
    private final Phone[] mPhone;

    public MtkPhoneSubInfoControllerEx(Context context, Phone[] phone) {
        this.mPhone = phone;
        if (ServiceManager.getService("iphonesubinfoEx") == null) {
            ServiceManager.addService("iphonesubinfoEx", this);
        }
        this.mContext = context;
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
    }

    private void log(String s) {
        Rlog.d(TAG, s);
    }

    private void loge(String s) {
        Rlog.e(TAG, s);
    }

    private boolean checkReadPhoneState(String callingPackage, String message) {
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", message);
            return true;
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", message);
            if (this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) == 0) {
                return true;
            }
            return false;
        }
    }

    private int getDefaultSubscription() {
        return PhoneFactory.getDefaultSubscription();
    }

    private Phone getPhone(int subId) {
        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            phoneId = 0;
        }
        return this.mPhone[phoneId];
    }

    private IccRecords getIccRecords(int subId) {
        Phone phone = getPhone(subId);
        if (phone != null) {
            UiccCard uiccCard = phone.getUiccCard();
            UiccCardApplication uiccApp = uiccCard != null ? uiccCard.getApplication(1) : null;
            if (uiccApp != null) {
                return uiccApp.getIccRecords();
            }
            return null;
        }
        loge("getIccRecords phone is null for Subscription:" + subId);
        return null;
    }

    public boolean getUsimService(int service, String callingPackage) {
        return getUsimServiceForSubscriber(getDefaultSubscription(), service, callingPackage);
    }

    public boolean getUsimServiceForSubscriber(int subId, int service, String callingPackage) {
        Phone phone = getPhone(subId);
        if (phone == null) {
            loge("getUsimService phone is null for Subscription:" + subId);
            return false;
        } else if (!checkReadPhoneState(callingPackage, "getUsimService")) {
            return false;
        } else {
            UsimServiceTable ust = phone.getUsimServiceTable();
            if (ust != null) {
                return ust.isAvailable(service);
            }
            log("getUsimService fail due to UST is null.");
            return false;
        }
    }

    public byte[] getUsimPsismsc() {
        return getUsimPsismscForSubscriber(getDefaultSubscription());
    }

    public byte[] getUsimPsismscForSubscriber(int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        IccRecords iccRecords = getIccRecords(subId);
        if (iccRecords != null) {
            return iccRecords.getEfPsismsc();
        }
        loge("getUsimPsismsc iccRecords is null for Subscription:" + subId);
        return null;
    }

    public byte[] getUsimSmsp() {
        return getUsimSmspForSubscriber(getDefaultSubscription());
    }

    public byte[] getUsimSmspForSubscriber(int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        IccRecords iccRecords = getIccRecords(subId);
        if (iccRecords != null) {
            return iccRecords.getEfSmsp();
        }
        loge("getUsimSmsp iccRecords is null for Subscription:" + subId);
        return null;
    }

    public int getMncLength() {
        return getMncLengthForSubscriber(getDefaultSubscription());
    }

    public int getMncLengthForSubscriber(int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        IccRecords iccRecords = getIccRecords(subId);
        if (iccRecords != null) {
            return iccRecords.getMncLength();
        }
        loge("getMncLength iccRecords is null for Subscription:" + subId);
        return 0;
    }

    public String getIsimImpiForSubscriber(int subId) {
        Phone phone = getPhone(subId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        if (phone != null) {
            IsimRecords isim = phone.getIsimRecords();
            if (isim != null) {
                return isim.getIsimImpi();
            }
            return null;
        }
        loge("getIsimImpi phone is null for Subscription:" + subId);
        return null;
    }

    public String getIsimDomainForSubscriber(int subId) {
        Phone phone = getPhone(subId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        if (phone != null) {
            IsimRecords isim = phone.getIsimRecords();
            if (isim != null) {
                return isim.getIsimDomain();
            }
            return null;
        }
        loge("getIsimDomain phone is null for Subscription:" + subId);
        return null;
    }

    public String[] getIsimImpuForSubscriber(int subId) {
        Phone phone = getPhone(subId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        if (phone != null) {
            IsimRecords isim = phone.getIsimRecords();
            if (isim != null) {
                return isim.getIsimImpu();
            }
            return null;
        }
        loge("getIsimImpu phone is null for Subscription:" + subId);
        return null;
    }

    public String getIsimIstForSubscriber(int subId) {
        Phone phone = getPhone(subId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        if (phone != null) {
            IsimRecords isim = phone.getIsimRecords();
            if (isim != null) {
                return isim.getIsimIst();
            }
            return null;
        }
        loge("getIsimIst phone is null for Subscription:" + subId);
        return null;
    }

    public String[] getIsimPcscfForSubscriber(int subId) {
        Phone phone = getPhone(subId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        if (phone != null) {
            IsimRecords isim = phone.getIsimRecords();
            if (isim != null) {
                return isim.getIsimPcscf();
            }
            return null;
        }
        loge("getIsimPcscf phone is null for Subscription:" + subId);
        return null;
    }

    public byte[] getIsimPsismsc() {
        return getIsimPsismscForSubscriber(getDefaultSubscription());
    }

    public byte[] getIsimPsismscForSubscriber(int subId) {
        Phone phone = getPhone(subId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        if (phone != null) {
            IsimRecords isim = phone.getIsimRecords();
            if (isim != null) {
                return ((MtkIsimUiccRecords) isim).getEfPsismsc();
            }
            return null;
        }
        loge("getIsimPsismsc phone is null for Subscription:" + subId);
        return null;
    }

    public String getIsimGbabp() {
        return getIsimGbabpForSubscriber(getDefaultSubscription());
    }

    public String getIsimGbabpForSubscriber(int subId) {
        Phone phone = getPhone(subId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        if (phone != null) {
            IsimRecords isim = phone.getIsimRecords();
            if (isim != null) {
                return ((MtkIsimUiccRecords) isim).getIsimGbabp();
            }
            return null;
        }
        loge("getIsimGbabp phone is null for Subscription:" + subId);
        return null;
    }

    public void setIsimGbabp(String gbabp, Message onComplete) {
        setIsimGbabpForSubscriber(getDefaultSubscription(), gbabp, onComplete);
    }

    public void setIsimGbabpForSubscriber(int subId, String gbabp, Message onComplete) {
        Phone phone = getPhone(subId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        if (phone != null) {
            IsimRecords isim = phone.getIsimRecords();
            if (isim != null) {
                ((MtkIsimUiccRecords) isim).setIsimGbabp(gbabp, onComplete);
                return;
            }
            loge("setIsimGbabp isim is null for Subscription:" + subId);
            return;
        }
        loge("setIsimGbabp phone is null for Subscription:" + subId);
    }

    public String getUsimGbabp() {
        return getUsimGbabpForSubscriber(getDefaultSubscription());
    }

    public String getUsimGbabpForSubscriber(int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        IccRecords iccRecords = getIccRecords(subId);
        if (iccRecords != null) {
            return iccRecords.getEfGbabp();
        }
        loge("getUsimGbabp iccRecords is null for Subscription:" + subId);
        return null;
    }

    public void setUsimGbabp(String gbabp, Message onComplete) {
        setUsimGbabpForSubscriber(getDefaultSubscription(), gbabp, onComplete);
    }

    public void setUsimGbabpForSubscriber(int subId, String gbabp, Message onComplete) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        IccRecords iccRecords = getIccRecords(subId);
        if (iccRecords != null) {
            iccRecords.setEfGbabp(gbabp, onComplete);
            return;
        }
        loge("setUsimGbabp iccRecords is null for Subscription:" + subId);
    }

    public String getLine1PhoneNumberForSubscriber(int subId, String callingPackage) {
        Phone phone = getPhone(subId);
        if (phone == null) {
            loge("getLine1PhoneNumber phone is null for Subscription:" + subId);
            return null;
        } else if (TelephonyPermissions.checkCallingOrSelfReadPhoneNumber(this.mContext, subId, callingPackage, "getLine1PhoneNumber")) {
            return ((MtkGsmCdmaPhone) phone).getLine1PhoneNumber();
        } else {
            loge("getLine1PhoneNumber permission check fail:" + subId);
            return null;
        }
    }
}
