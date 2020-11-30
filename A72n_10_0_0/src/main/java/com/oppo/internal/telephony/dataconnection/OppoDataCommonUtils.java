package com.oppo.internal.telephony.dataconnection;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.dataconnection.AbstractDcTracker;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.util.OemTelephonyUtils;

public class OppoDataCommonUtils {
    private static final String ACTION_COMMAND_FORCE_DISABLE_ENDC = "android.intent.force_disable_endc";
    private static final String LOG_TAG = "OppoDataCommonUtils";
    private static final String[] sAuTelstraOperator = {"50501", "50571", "50572", "50511"};
    private static final String[] sEuVodafoneOperator = {"22210", "20404"};
    private static final String[] sKDDIOperator = {"44050", "44051", "44052", "44053", "44054", "44070", "44071", "44072", "44073", "44074", "44075", "44076"};
    private static final String[] sLotteOperator = {"44011"};

    public static boolean oemCheckSetMtu(ApnSetting apn, LinkProperties lp, Phone phone) {
        try {
            DcTracker mDcTracker = phone.getDcTracker(1);
            if (apn != null) {
                if (lp != null) {
                    if (lp.getMtu() != 0 && lp.getMtu() != 1500) {
                        return false;
                    }
                    if (apn.getMtu() != 0 && apn.getMtu() != 1500) {
                        return false;
                    }
                    int mtu = 0;
                    if (SystemProperties.getInt("persist.sys.oppo.mtu", 0) == 0) {
                        String defaultMccMnc = TelephonyManager.getDefault().getSimOperatorNumeric(phone.getSubId());
                        if (TextUtils.isEmpty(defaultMccMnc)) {
                            defaultMccMnc = ((AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, mDcTracker)).getOperatorNumeric();
                        }
                        if (!TextUtils.isEmpty(defaultMccMnc)) {
                            int mcc = Integer.parseInt(defaultMccMnc.substring(0, 3));
                            int mnc = Integer.parseInt(defaultMccMnc.substring(3));
                            if (mcc == 460) {
                                mtu = 1410;
                                if (mnc == 3 || mnc == 11) {
                                    mtu = 1460;
                                }
                            } else if (mcc == 440) {
                                int i = 0;
                                while (true) {
                                    if (i >= sKDDIOperator.length) {
                                        break;
                                    } else if (sKDDIOperator[i].equals(defaultMccMnc)) {
                                        mtu = 1440;
                                        break;
                                    } else {
                                        i++;
                                    }
                                }
                                int i2 = 0;
                                while (true) {
                                    if (i2 >= sLotteOperator.length) {
                                        break;
                                    } else if (sLotteOperator[i2].equals(defaultMccMnc)) {
                                        mtu = 1420;
                                        break;
                                    } else {
                                        i2++;
                                    }
                                }
                            } else if (mcc == 505) {
                                int i3 = 0;
                                while (true) {
                                    if (i3 >= sAuTelstraOperator.length) {
                                        break;
                                    } else if (sAuTelstraOperator[i3].equals(defaultMccMnc)) {
                                        mtu = 1358;
                                        break;
                                    } else {
                                        i3++;
                                    }
                                }
                            } else if (mcc == 222 || mcc == 204) {
                                int i4 = 0;
                                while (true) {
                                    if (i4 >= sEuVodafoneOperator.length) {
                                        break;
                                    } else if (sEuVodafoneOperator[i4].equals(defaultMccMnc)) {
                                        mtu = 1340;
                                        break;
                                    } else {
                                        i4++;
                                    }
                                }
                            }
                        }
                        if (mtu != 0) {
                            lp.setMtu(mtu);
                            OppoRlog.Rlog.d(LOG_TAG, "MTU set by config resource to: " + mtu);
                            return true;
                        }
                    }
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            OppoRlog.Rlog.d(LOG_TAG, "MTU set error");
        }
    }

    public static void oemCloseNr(Phone phone) {
        try {
            boolean isNRConnected = true;
            boolean isWifiConnected = ((ConnectivityManager) phone.getContext().getSystemService("connectivity")).getNetworkInfo(1).isConnected();
            boolean hasCurrentActiveCall = ((TelephonyManager) phone.getContext().getSystemService(TelephonyManager.class)).getCallState() != 0;
            if (phone.getServiceState().getNrState() != 3) {
                isNRConnected = false;
            }
            OppoRlog.Rlog.d(LOG_TAG, "wifi=" + isWifiConnected + ",call=" + hasCurrentActiveCall + ",nr=" + isNRConnected);
            if (isNRConnected && !isWifiConnected) {
                if (!hasCurrentActiveCall) {
                    Intent intent = new Intent(ACTION_COMMAND_FORCE_DISABLE_ENDC);
                    intent.putExtra("PhoneId", phone.getPhoneId());
                    phone.getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
                    OppoRlog.Rlog.d(LOG_TAG, "sendBroadcastAsUser");
                    return;
                }
            }
            OppoRlog.Rlog.d(LOG_TAG, "oemCloseNr,return");
        } catch (Exception e) {
            OppoRlog.Rlog.e(LOG_TAG, "Send broadcast failed: " + e);
        }
    }
}
