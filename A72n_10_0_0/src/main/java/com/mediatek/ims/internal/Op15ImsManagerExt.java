package com.mediatek.ims.internal;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.ImsConfig;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.mediatek.ims.internal.ext.ImsManagerExt;
import java.util.Arrays;
import java.util.List;

public class Op15ImsManagerExt extends ImsManagerExt {
    private static final boolean ENG = true;
    private static final List<String> OP15_MCCMNC_LIST = Arrays.asList("23410");
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final String TAG = "Entitlement-Op15ImsManagerExt-";
    private static final boolean TELDBG = true;
    private Context mContext;

    public Op15ImsManagerExt(Context context) {
        this.mContext = context;
    }

    @Override // com.mediatek.ims.internal.ext.ImsManagerExt, com.mediatek.ims.internal.ext.IImsManagerExt
    public boolean isFeatureEnabledByPlatform(Context context, int feature, int phoneId) {
        Log.d(TAG, "feature:" + feature + ", phoneId:" + phoneId);
        if (feature == 0 || feature == 2) {
            return isFeatureProvisionedOnDevice(feature, phoneId);
        }
        return super.isFeatureEnabledByPlatform(context, feature, phoneId);
    }

    public boolean isFeatureProvisionedOnDevice(int feature, int phoneId) {
        ImsManager imsManager = ImsManager.getInstance(this.mContext, phoneId);
        boolean result = false;
        if (!isEntitlementEnabled()) {
            Log.d(TAG, "Entitlement sys property not enabled return true");
            return true;
        } else if (!isMccMncReady(phoneId) || isOp15Card(phoneId)) {
            if (imsManager != null) {
                try {
                    ImsConfig imsConfig = imsManager.getConfigInterface();
                    if (imsConfig != null) {
                        if (feature == 0) {
                            int value = imsConfig.getProvisionedValue(10);
                            Log.d(TAG, "VoLTE provisioned value = " + value);
                            if (value == 1) {
                                result = true;
                            }
                        } else {
                            int value2 = imsConfig.getProvisionedValue(28);
                            Log.d(TAG, "VoWifi provisioned value = " + value2);
                            if (value2 == 1) {
                                result = true;
                            }
                        }
                    }
                } catch (ImsException e) {
                    Log.e(TAG, "Volte not updated, ImsConfig null");
                    e.printStackTrace();
                } catch (RuntimeException e2) {
                    Log.e(TAG, "ImsConfig not ready");
                    e2.printStackTrace();
                }
            } else {
                Log.e(TAG, "Volte not updated, ImsManager null");
            }
            Log.d(TAG, "isFeatureProvisionedOnDevice returns " + result);
            return result;
        } else {
            Log.d(TAG, "This operator is no need to check provision.");
            return true;
        }
    }

    private static int getSubIdUsingPhoneId(int phoneId) {
        int[] values = SubscriptionManager.getSubId(phoneId);
        if (values == null || values.length <= 0) {
            return SubscriptionManager.getDefaultSubscriptionId();
        }
        Log.d(TAG, "getSubIdUsingPhoneId:" + values[0] + ", phoneId:" + phoneId);
        return values[0];
    }

    private static boolean isOp15Card(int phoneId) {
        return OP15_MCCMNC_LIST.contains(getSimOperatorNumericForPhone(phoneId));
    }

    private static boolean isMccMncReady(int phoneId) {
        if (!TextUtils.isEmpty(getSimOperatorNumericForPhone(phoneId))) {
            return true;
        }
        Log.d(TAG, "MccMnc is empty.");
        return false;
    }

    private static String getSimOperatorNumericForPhone(int phoneId) {
        String mccMncPropertyName;
        int subId = getSubIdUsingPhoneId(phoneId);
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            Log.d(TAG, "Is Invalid Subscription id.");
            return "";
        }
        if (TelephonyManager.getDefault().getCurrentPhoneType(subId) == 2) {
            if (phoneId == 0) {
                mccMncPropertyName = "vendor.cdma.ril.uicc.mccmnc";
            } else {
                mccMncPropertyName = "vendor.cdma.ril.uicc.mccmnc." + phoneId;
            }
        } else if (phoneId == 0) {
            mccMncPropertyName = "vendor.gsm.ril.uicc.mccmnc";
        } else {
            mccMncPropertyName = "vendor.gsm.ril.uicc.mccmnc." + phoneId;
        }
        String mccMnc = SystemProperties.get(mccMncPropertyName, "");
        Log.d(TAG, "getMccMnc, mccMnc value:" + mccMnc);
        return mccMnc;
    }

    private static boolean isEntitlementEnabled() {
        boolean isEntitlementEnabled = true;
        if (1 != SystemProperties.getInt("persist.vendor.entitlement_enabled", 1)) {
            isEntitlementEnabled = false;
        }
        Log.d(TAG, "in Op15fwkplugin, isEntitlementEnabled:" + isEntitlementEnabled);
        return isEntitlementEnabled;
    }
}
