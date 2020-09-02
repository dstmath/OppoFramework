package com.android.server.location;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.telephony.ColorOSTelephonyManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class OppoNetworkUtil {
    public static final String ACTION_REDTEAMOBILE_ROAMING_MAIN = "com.redteamobile.roaming.MAIN";
    public static final int CARD_TYPE_CM = 2;
    public static final int CARD_TYPE_CT = 1;
    public static final int CARD_TYPE_CU = 3;
    public static final int CARD_TYPE_OTHER = 4;
    public static final int CARD_TYPE_UNKNOWN = -1;
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final String TAG = "OppoNetworkUtil";
    private static boolean mIsDebug = true;

    public static boolean isMobileTypeActive(Context context) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return info != null && info.getType() == 0;
    }

    public static boolean isRoaming(Context context, int slotId) {
        ColorOSTelephonyManager colorOSTelephonyManager = ColorOSTelephonyManager.getDefault(context);
        if (colorOSTelephonyManager != null) {
            boolean isRoaming = colorOSTelephonyManager.isNetworkRoamingGemini(slotId);
            if (mIsDebug) {
                Log.d(TAG, "isRoaming = " + isRoaming);
            }
            if (isRoaming) {
                return true;
            }
            return false;
        }
        if (mIsDebug) {
            Log.e(TAG, "colorPhone is null!");
        }
        return false;
    }

    public static boolean isChineseSim(Context context, int slotId) {
        String imsi = ((TelephonyManager) context.getSystemService("phone")).getSubscriberId(slotId);
        if (mIsDebug) {
            Log.d(TAG, "isChineseSim imsi = " + imsi + ", slotId = " + slotId);
        }
        if (imsi != null && imsi.startsWith("460")) {
            return true;
        }
        if (imsi == null || !imsi.startsWith("20404") || 1 != getSimType(context)) {
            return false;
        }
        return true;
    }

    public static boolean isNotChineseOperator(Context context, int slotId) {
        String mMCC = ColorOSTelephonyManager.getDefault(context).getNetworkOperatorGemini(slotId);
        if (mIsDebug) {
            Log.d(TAG, "isChineseSim mMCC = " + mMCC);
        }
        if (mMCC == null || mMCC.startsWith("460")) {
            return false;
        }
        return true;
    }

    public static int getDataSlotId(Context context) {
        int slotId = ColorOSTelephonyManager.getDefault(context).colorGetDataSubscription();
        if (mIsDebug) {
            Log.d(TAG, "getDataSlotId slotId = " + slotId);
        }
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
            if (!mIsDebug) {
                return true;
            }
            Log.d(TAG, "isSoftSimCard true, slot " + slotIdx);
            return true;
        }
    }

    public static int getSimType(Context context) {
        SubscriptionInfo sir;
        int[] subId = SubscriptionManager.getSubId(getDataSlotId(context));
        if (subId == null || subId.length <= 0 || (sir = SubscriptionManager.from(context).getActiveSubscriptionInfo(subId[0])) == null) {
            return -1;
        }
        Log.d(TAG, " sir.getIconTint() = " + sir.getIconTint());
        return sir.getIconTint();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00c8, code lost:
        if (r1 != null) goto L_0x0065;
     */
    public static boolean checkGoogleNetwork() {
        HttpsURLConnection urlConnection = null;
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
            HttpsURLConnection.setDefaultHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
            urlConnection = (HttpsURLConnection) new URL("https://www.google.com").openConnection();
            urlConnection.setRequestMethod("HEAD");
            urlConnection.setConnectTimeout(SOCKET_TIMEOUT_MS);
            urlConnection.setReadTimeout(SOCKET_TIMEOUT_MS);
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setUseCaches(false);
            urlConnection.connect();
            int rspCode = urlConnection.getResponseCode();
            SystemClock.elapsedRealtime();
            if ((rspCode < 200 || rspCode >= 400) && rspCode != 405) {
                if (mIsDebug) {
                    Log.d(TAG, "failed !!!! ");
                }
                urlConnection.disconnect();
                return false;
            }
            if (mIsDebug) {
                Log.d(TAG, "success !!!! ");
            }
            urlConnection.disconnect();
            return true;
        } catch (SocketTimeoutException e) {
            if (mIsDebug) {
                Log.d(TAG, " SocketTimeoutException " + e.toString());
            }
        } catch (IOException e2) {
            if (mIsDebug) {
                Log.d(TAG, "IOException " + e2.toString());
            }
            if (urlConnection != null) {
            }
        } catch (Exception e3) {
            if (mIsDebug) {
                Log.d(TAG, "Exception " + e3.toString());
            }
            if (urlConnection != null) {
            }
        } catch (Throwable th) {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            throw th;
        }
    }

    public static boolean isForbidAccessGMS_inAbroadMobileNetwork(Context context) {
        boolean res = false;
        if (true == isMobileTypeActive(context)) {
            int slotID = getDataSlotId(context);
            if (isNotChineseOperator(context, slotID) && isChineseSim(context, slotID) && !isSoftSimCard(context, slotID)) {
                res = true;
            }
        }
        if (mIsDebug) {
            Log.d(TAG, "isForbidAccessGMS_inAbroadMobileNetwork res = " + res);
        }
        return res;
    }

    public static void setDebug(boolean isDebug) {
        mIsDebug = isDebug;
    }
}
