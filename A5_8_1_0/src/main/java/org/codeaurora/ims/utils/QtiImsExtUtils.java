package org.codeaurora.ims.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.android.internal.telephony.uicc.SpnOverride;
import java.io.File;
import org.codeaurora.ims.QtiCallConstants;
import org.codeaurora.ims.QtiCarrierConfigs;
import org.codeaurora.ims.QtiImsException;

public class QtiImsExtUtils {
    public static final String ACTION_VOPS_SSAC_STATUS = "org.codeaurora.VOIP_VOPS_SSAC_STATUS";
    public static final String CARRIER_ONE_DEFAULT_MCC_MNC = "405854";
    public static final String EXTRA_SSAC = "Ssac";
    public static final String EXTRA_VOPS = "Vops";
    private static String LOG_TAG = "QtiImsExtUtils";
    public static final String PROPERTY_RADIO_ATEL_CARRIER = "persist.radio.atel.carrier";
    public static final int QTI_IMS_ASSURED_TRANSFER = 2;
    public static final int QTI_IMS_BLIND_TRANSFER = 1;
    public static final String QTI_IMS_CALL_DEFLECT_NUMBER = "ims_call_deflect_number";
    public static final int QTI_IMS_CONSULTATIVE_TRANSFER = 4;
    public static final int QTI_IMS_HO_DISABLE_ALL = 2;
    public static final int QTI_IMS_HO_ENABLED_WLAN_TO_WWAN_ONLY = 3;
    public static final int QTI_IMS_HO_ENABLED_WWAN_TO_WLAN_ONLY = 4;
    public static final int QTI_IMS_HO_ENABLE_ALL = 1;
    public static final int QTI_IMS_HO_INVALID = 0;
    public static final String QTI_IMS_INCOMING_CONF_EXTRA_KEY = "incomingConference";
    public static final String QTI_IMS_PHONE_ID_EXTRA_KEY = "phoneId";
    public static final int QTI_IMS_REQUEST_ERROR = 1;
    public static final int QTI_IMS_REQUEST_SUCCESS = 0;
    public static final int QTI_IMS_SMS_APP_INVALID = -1;
    public static final int QTI_IMS_SMS_APP_NOT_RCS = 2;
    public static final int QTI_IMS_SMS_APP_RCS = 1;
    public static final int QTI_IMS_SMS_APP_SELECTION_NOT_ALLOWED = 0;
    public static final String QTI_IMS_STATIC_IMAGE_SETTING = "ims_vt_call_static_image";
    public static final String QTI_IMS_TRANSFER_EXTRA_KEY = "transferType";
    public static final int QTI_IMS_VOLTE_PREF_OFF = 0;
    public static final int QTI_IMS_VOLTE_PREF_ON = 1;
    public static final int QTI_IMS_VOLTE_PREF_UNKNOWN = 2;
    public static final int QTI_IMS_VVM_APP_INVALID = -1;
    public static final int QTI_IMS_VVM_APP_NOT_RCS = 0;
    public static final int QTI_IMS_VVM_APP_RCS = 1;

    public static class VideoQualityFeatureValuesConstants {
        public static final int HIGH = 2;
        public static final int LOW = 0;
        public static final int MEDIUM = 1;
    }

    private QtiImsExtUtils() {
    }

    public static String getCallDeflectNumber(ContentResolver contentResolver) {
        String deflectcall = Global.getString(contentResolver, QTI_IMS_CALL_DEFLECT_NUMBER);
        if (deflectcall == null || !deflectcall.isEmpty()) {
            return deflectcall;
        }
        return null;
    }

    public static void setCallDeflectNumber(ContentResolver contentResolver, String value) {
        String deflectNum = value;
        if (value == null || value.isEmpty()) {
            deflectNum = SpnOverride.MVNO_TYPE_NONE;
        }
        Global.putString(contentResolver, QTI_IMS_CALL_DEFLECT_NUMBER, deflectNum);
    }

    public static String getStaticImageUriStr(ContentResolver contentResolver) {
        return Global.getString(contentResolver, QTI_IMS_STATIC_IMAGE_SETTING);
    }

    private static boolean isValidUriStr(String uri) {
        return (uri == null || (uri.isEmpty() ^ 1) == 0) ? false : new File(uri).exists();
    }

    private static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        Log.d(LOG_TAG, "calculateInSampleSize: reqWidth = " + reqWidth + " reqHeight = " + reqHeight + " raw width = " + width + " raw height = " + height);
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.d(LOG_TAG, "calculateInSampleSize: inSampleSize = " + inSampleSize);
        return inSampleSize;
    }

    public static Bitmap decodeImage(String uri, int reqWidth, int reqHeight) {
        if (uri == null) {
            return null;
        }
        Options options = new Options();
        options.inPreferredConfig = Config.ARGB_8888;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return scaleImage(BitmapFactory.decodeFile(uri, options), reqWidth, reqHeight);
    }

    private static Bitmap scaleImage(Bitmap bitmap, int reqWidth, int reqHeight) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float scaleWidth = ((float) reqWidth) / ((float) w);
        float scaleHeight = ((float) reqHeight) / ((float) h);
        Log.d(LOG_TAG, "scaleImage bitmap w = " + w + " bitmap h = " + h);
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, false);
    }

    public static Bitmap decodeImage(Resources res, int resId, int reqWidth, int reqHeight) {
        Options options = new Options();
        options.inPreferredConfig = Config.ARGB_8888;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return scaleImage(BitmapFactory.decodeResource(res, resId, options), reqWidth, reqHeight);
    }

    public static Bitmap getStaticImage(Context context, int reqWidth, int reqHeight) throws QtiImsException {
        String uriStr = getStaticImageUriStr(context.getContentResolver());
        Log.d(LOG_TAG, "getStaticImage: uriStr = " + uriStr + " reqWidth = " + reqWidth + " reqHeight = " + reqHeight);
        if (isValidUriStr(uriStr)) {
            Bitmap imageBitmap = decodeImage(uriStr, reqWidth, reqHeight);
            if (imageBitmap != null) {
                return imageBitmap;
            }
            throw new QtiImsException("image decoding error");
        }
        throw new QtiImsException("invalid file path");
    }

    public static boolean shallTransmitStaticImage(int phoneId, Context context) {
        return isCarrierConfigEnabled(phoneId, context, QtiCarrierConfigs.TRANSMIT_STATIC_IMAGE);
    }

    public static boolean shallShowStaticImageUi(int phoneId, Context context) {
        return isCarrierConfigEnabled(phoneId, context, QtiCarrierConfigs.SHOW_STATIC_IMAGE_UI);
    }

    public static boolean shallShowVideoQuality(int phoneId, Context context) {
        return isCarrierConfigEnabled(phoneId, context, QtiCarrierConfigs.SHOW_VIDEO_QUALITY_UI);
    }

    public static boolean isCallTransferEnabled(Context context) {
        return SystemProperties.getBoolean("persist.radio.ims_call_transfer", false);
    }

    public static boolean useExt(int phoneId, Context context) {
        return isCarrierConfigEnabled(phoneId, context, QtiCarrierConfigs.USE_VIDEO_UI_EXTENSIONS);
    }

    public static boolean useCustomVideoUi(int phoneId, Context context) {
        return isCarrierConfigEnabled(phoneId, context, QtiCarrierConfigs.USE_CUSTOM_VIDEO_UI);
    }

    public static boolean isCsRetryConfigEnabled(int phoneId, Context context) {
        return isCarrierConfigEnabled(phoneId, context, QtiCarrierConfigs.CONFIG_CS_RETRY);
    }

    public static boolean isVoWiFiCallQualityEnabled(int phoneId, Context context) {
        return isCarrierConfigEnabled(phoneId, context, QtiCarrierConfigs.VOWIFI_CALL_QUALITY);
    }

    public static boolean isCarrierOneSupported() {
        return CARRIER_ONE_DEFAULT_MCC_MNC.equals(SystemProperties.get(PROPERTY_RADIO_ATEL_CARRIER));
    }

    public static boolean isCarrierConfigEnabled(int phoneId, Context context, String carrierConfig) {
        return QtiCarrierConfigHelper.getInstance().getBoolean(context, phoneId, carrierConfig);
    }

    public static boolean allowVideoCallsInLowBattery(int phoneId, Context context) {
        return isCarrierConfigEnabled(phoneId, context, QtiCarrierConfigs.ALLOW_VIDEO_CALL_IN_LOW_BATTERY);
    }

    public static boolean shallHidePreviewInVtConference(int phoneId, Context context) {
        return isCarrierConfigEnabled(phoneId, context, QtiCarrierConfigs.HIDE_PREVIEW_IN_VT_CONFERENCE);
    }

    public static boolean canHoldVideoCall(int phoneId, Context context) {
        return isCarrierConfigEnabled(phoneId, context, QtiCarrierConfigs.ALLOW_HOLD_IN_VIDEO_CALL);
    }

    public static boolean shallRemoveModifyCallCapability(Context context) {
        return shallRemoveModifyCallCapability(-1, context);
    }

    public static boolean shallRemoveModifyCallCapability(int phoneId, Context context) {
        return isCarrierConfigEnabled(phoneId, context, QtiCarrierConfigs.REMOVE_MODIFY_CALL_CAPABILITY);
    }

    private static PersistableBundle getConfigForPhoneId(Context context, int phoneId) {
        if (context == null) {
            Log.e(LOG_TAG, "getConfigForPhoneId context is null");
            return null;
        }
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        if (configManager == null) {
            Log.e(LOG_TAG, "getConfigForPhoneId configManager is null");
            return null;
        } else if (phoneId == -1) {
            Log.e(LOG_TAG, "getConfigForPhoneId phoneId is invalid");
            return null;
        } else {
            int subId = getSubscriptionIdFromPhoneId(context, phoneId);
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                return configManager.getConfigForSubId(subId);
            }
            Log.e(LOG_TAG, "getConfigForPhoneId subId is invalid");
            return null;
        }
    }

    private static int getSubscriptionIdFromPhoneId(Context context, int phoneId) {
        SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        if (subscriptionManager == null) {
            return -1;
        }
        SubscriptionInfo subInfo = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(phoneId);
        if (subInfo == null) {
            return -1;
        }
        return subInfo.getSubscriptionId();
    }

    public static boolean isRttOn(Context context) {
        return getRttMode(context) != 0;
    }

    public static int getRttMode(Context context) {
        return Global.getInt(context.getContentResolver(), QtiCallConstants.QTI_IMS_RTT_MODE, 0);
    }

    public static void setRttMode(boolean value, Context context) {
        Global.putInt(context.getContentResolver(), QtiCallConstants.QTI_IMS_RTT_MODE, value ? 1 : 0);
    }

    public static boolean isRttSupported(int phoneId, Context context) {
        return isCarrierConfigEnabled(phoneId, context, QtiCarrierConfigs.KEY_CARRIER_RTT_SUPPORTED);
    }

    public static boolean isRttSupportedOnVtCalls(int phoneId, Context context) {
        return isCarrierConfigEnabled(phoneId, context, QtiCarrierConfigs.KEY_CARRIER_RTT_SUPPORTED_ON_VTCALLS);
    }

    public static boolean isRttUpgradeSupported(int phoneId, Context context) {
        return isCarrierConfigEnabled(phoneId, context, QtiCarrierConfigs.KEY_CARRIER_RTT_UPGRADE_SUPPORTED);
    }

    public static int getRttOperatingMode(Context context) {
        return SystemProperties.getInt(QtiCallConstants.PROPERTY_RTT_OPERATING_MODE, 0);
    }

    public static boolean isRttDowngradeSupported(int phoneId, Context context) {
        return isCarrierConfigEnabled(phoneId, context, QtiCarrierConfigs.KEY_CARRIER_RTT_DOWNGRADE_SUPPORTED);
    }

    public static boolean isCallDeflectionSupported(int phoneId, Context context) {
        return isCarrierConfigEnabled(phoneId, context, QtiCarrierConfigs.KEY_CARRIER_IMS_CALL_DEFLECT_SUPPORTED);
    }

    public static boolean isCancelModifyCallSupported(int phoneId, Context context) {
        return isCarrierConfigEnabled(phoneId, context, QtiCarrierConfigs.KEY_CARRIER_CANCEL_MODIFY_CALL_SUPPORTED);
    }
}
