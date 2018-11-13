package com.android.server.location;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.telephony.ColorOSTelephonyManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.android.server.display.DisplayTransformManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class OppoNetworkUtil {
    public static final String ACTION_REDTEAMOBILE_ROAMING_MAIN = "com.redteamobile.roaming.MAIN";
    public static final int CARD_TYPE_CM = 2;
    public static final int CARD_TYPE_CT = 1;
    public static final int CARD_TYPE_CU = 3;
    public static final int CARD_TYPE_OTHER = 4;
    public static final int CARD_TYPE_UNKNOWN = -1;
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final String TAG = "OppoNetworkUtil";
    private static final boolean mIsDebug = true;

    public static boolean isMobileTypeActive(Context context) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (info == null || info.getType() != 0) {
            return false;
        }
        return true;
    }

    public static boolean isRoaming(Context context, int slotId) {
        ColorOSTelephonyManager colorOSTelephonyManager = ColorOSTelephonyManager.getDefault(context);
        if (colorOSTelephonyManager != null) {
            boolean isRoaming = colorOSTelephonyManager.isNetworkRoamingGemini(slotId);
            Log.d(TAG, "isRoaming = " + isRoaming);
            if (isRoaming) {
                return true;
            }
            return false;
        }
        Log.e(TAG, "colorPhone is null!");
        return false;
    }

    public static boolean isChineseSim(Context context, int slotId) {
        String imsi = ColorOSTelephonyManager.getDefault(context).getSubscriberIdGemini(slotId);
        Log.d(TAG, "isChineseSim imsi = " + imsi);
        if (imsi != null && imsi.startsWith("460")) {
            return true;
        }
        if (imsi != null && imsi.startsWith("20404") && 1 == getSimType(context)) {
            return true;
        }
        return false;
    }

    public static boolean isNotChineseOperator(Context context, int slotId) {
        String mMCC = ColorOSTelephonyManager.getDefault(context).getNetworkOperatorGemini(slotId);
        Log.d(TAG, "isChineseSim mMCC = " + mMCC);
        if (mMCC == null || (mMCC.startsWith("460") ^ 1) == 0) {
            return false;
        }
        return true;
    }

    public static int getDataSlotId(Context context) {
        int slotId = ColorOSTelephonyManager.getDefault(context).colorGetDataSubscription();
        Log.d(TAG, "getDataSlotId slotId = " + slotId);
        return slotId;
    }

    public static boolean isRedTeaSoftSimSupport(Context context) {
        if (context == null) {
            Log.e(TAG, "isSoftSimCard false, context is null");
            return false;
        }
        if (context.getPackageManager().resolveActivity(new Intent(ACTION_REDTEAMOBILE_ROAMING_MAIN), 65536) != null) {
            return true;
        }
        return false;
    }

    public static boolean isSoftSimCard(Context context, int slotIdx) {
        if (context == null) {
            Log.e(TAG, "isSoftSimCard false, context is null");
            return false;
        } else if (slotIdx != ColorOSTelephonyManager.getDefault(context).colorGetSoftSimCardSlotId()) {
            return false;
        } else {
            Log.d(TAG, "isSoftSimCard true, slot " + slotIdx);
            return true;
        }
    }

    public static int getSimType(Context context) {
        SubscriptionInfo sir = SubscriptionManager.from(context).getActiveSubscriptionInfo(SubscriptionManager.getSubId(getDataSlotId(context))[0]);
        if (sir == null) {
            return -1;
        }
        Log.d(TAG, " sir.getIconTint() = " + sir.getIconTint());
        return sir.getIconTint();
    }

    public static boolean checkGoogleNetwork() {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL("http://www.google.com");
            Log.d(TAG, "try to connect :  " + url.getHost());
            long begin = SystemClock.elapsedRealtime();
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("HEAD");
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setUseCaches(false);
            urlConnection.connect();
            int rspCode = urlConnection.getResponseCode();
            Log.d(TAG, "ResponseCode : " + rspCode + " , cost:" + (SystemClock.elapsedRealtime() - begin) + " ms");
            if ((rspCode < 200 || rspCode >= DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR) && rspCode != VoldResponseCode.OpFailedStorageBusy) {
                Log.e(TAG, "Fail, try to connect next server. ");
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                return false;
            }
            Log.d(TAG, "success !!!! ");
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            return true;
        } catch (SocketTimeoutException e) {
            Log.d(TAG, " SocketTimeoutException " + e.toString());
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        } catch (IOException e2) {
            Log.d(TAG, "IOException " + e2.toString());
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        } catch (Exception e3) {
            Log.d(TAG, "Exception " + e3.toString());
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        } catch (Throwable th) {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public static boolean isForbidAccessGMS_inAbroadMobileNetwork(Context context) {
        boolean res = false;
        if (isMobileTypeActive(context)) {
            int slotID = getDataSlotId(context);
            if (isNotChineseOperator(context, slotID) && isChineseSim(context, slotID) && (isSoftSimCard(context, slotID) ^ 1) != 0) {
                res = true;
            }
        }
        Log.d(TAG, "isForbidAccessGMS_inAbroadMobileNetwork res = " + res);
        return res;
    }
}
