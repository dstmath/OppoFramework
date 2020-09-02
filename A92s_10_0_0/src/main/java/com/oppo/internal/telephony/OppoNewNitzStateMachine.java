package com.oppo.internal.telephony;

import android.net.ConnectivityManager;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.util.TimestampedValue;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.IOppoNewNitzStateMachine;
import com.oppo.internal.telephony.utils.ConnectivityManagerHelper;
import java.text.SimpleDateFormat;
import java.util.HashMap;

public class OppoNewNitzStateMachine implements IOppoNewNitzStateMachine {
    public static final long NITZ_NTP_INTERVAL_OEM = 86400000;
    public static final long NITZ_NTP_INTERVAL_OEM_SECOND = 1000;
    public static final String PROPERTY_USENTP_INTERVAL = "persist.sys.usentpinterval";
    public static final String PROPERTY_USENTP_TYPE = "persist.sys.usentptype";
    public static final long SYSTEM_NTP_INTERVAL_OEM = 5000;
    public static final String TAG_ENABLE = "use_ntp_enable";
    public static final String TAG_ENABLE_VALUE = "1";
    public static final String TAG_INTERVAL = "use_ntp_interval";
    public static final int TYPE_USENTP_CLOSE = 0;
    public static final int TYPE_USENTP_OPEN = 1;
    public static final int USE_NTP_TIME_INTERVAL_LESS = 2;
    public static final int USE_NTP_TIME_INTERVAL_MORE = 1;
    public static final int USE_NTP_TIME_NONE = 0;
    public static boolean mIsUseNtpTime = false;
    public static long mUseNtpInterval = 0;
    public String LOG_TAG = "OppoNewNitzStateMachine";
    public final String TZUPDATE_UPLOAD_INFO = "tzupdate_upload_info";
    public ConnectivityManager mConnectivityManager = null;
    public GsmCdmaPhone mPhone;
    protected String[][] mTimeZoneIdOfCapitalCity = {new String[]{"au", "Australia/Sydney"}, new String[]{"br", "America/Sao_Paulo"}, new String[]{"ca", "America/Toronto"}, new String[]{"cl", "America/Santiago"}, new String[]{"es", "Europe/Madrid"}, new String[]{"fm", "Pacific/Ponape"}, new String[]{"gl", "America/Godthab"}, new String[]{"kz", "Asia/Almaty"}, new String[]{"mn", "Asia/Ulaanbaatar"}, new String[]{"mx", "America/Mexico_City"}, new String[]{"pf", "Pacific/Tahiti"}, new String[]{"pt", "Europe/Lisbon"}, new String[]{"us", "America/New_York"}, new String[]{"ec", "America/Guayaquil"}, new String[]{"cn", "Asia/Shanghai"}};

    public OppoNewNitzStateMachine(GsmCdmaPhone phone) {
        this.mPhone = phone;
    }

    public static int OppogetUseNtpType() {
        return SystemProperties.getInt(PROPERTY_USENTP_TYPE, 1);
    }

    public static long OppogetUseNtpInterval() {
        return SystemProperties.getLong(PROPERTY_USENTP_INTERVAL, (long) NITZ_NTP_INTERVAL_OEM);
    }

    public void OppoisUseNtptime(long currenttimeinmillis, long millisSinceNitzReceived, long ReferenceTimeMillis, TimestampedValue<Long> timestampedValue) {
        try {
            if (mIsUseNtpTime) {
                long time = currenttimeinmillis + millisSinceNitzReceived;
                if (this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.cmcc.test")) {
                    return;
                }
                if (this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.cu.test")) {
                    return;
                }
                if (!this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.ct.test")) {
                    if (this.mConnectivityManager == null) {
                        this.mConnectivityManager = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
                    }
                    if (this.mConnectivityManager != null && ConnectivityManagerHelper.hasCache(this.mConnectivityManager)) {
                        long currentNtpTime = ConnectivityManagerHelper.getCurrentTimeMillis(this.mConnectivityManager);
                        long currentSystemTime = System.currentTimeMillis();
                        String str = this.LOG_TAG;
                        Rlog.d(str, "time = " + time + ", mConnectivityManager.getCurrentTimeMillis() = " + currentNtpTime + ", currentSystemTime = " + currentSystemTime);
                        String str2 = this.LOG_TAG;
                        StringBuilder sb = new StringBuilder();
                        sb.append("mUseNtpInterval = ");
                        sb.append(mUseNtpInterval);
                        Rlog.d(str2, sb.toString());
                        if (mUseNtpInterval != 0) {
                            if (Math.abs(time - currentNtpTime) >= mUseNtpInterval) {
                                return;
                            }
                        }
                        if (Math.abs(currentSystemTime - currentNtpTime) > SYSTEM_NTP_INTERVAL_OEM) {
                            try {
                                new TimestampedValue(ReferenceTimeMillis, Long.valueOf(currentNtpTime));
                            } catch (Exception e) {
                                e = e;
                            }
                        }
                    }
                }
            }
        } catch (Exception e2) {
            e = e2;
            e.printStackTrace();
        }
    }

    public void OppoSetUseNtpTime(String name, String value) {
        if (value != null && name != null) {
            if (TAG_ENABLE.equals(name)) {
                if (value.equals("1")) {
                    mIsUseNtpTime = true;
                    SystemProperties.set(PROPERTY_USENTP_TYPE, "1");
                    return;
                }
                mIsUseNtpTime = false;
                SystemProperties.set(PROPERTY_USENTP_TYPE, "0");
            } else if (TAG_INTERVAL.equals(name)) {
                mUseNtpInterval = 1000 * Long.parseLong(value);
                SystemProperties.set(PROPERTY_USENTP_INTERVAL, "" + mUseNtpInterval);
            }
        }
    }

    public String OppoGetTimeZonesWithCapitalCity(String iso) {
        int i = 0;
        while (true) {
            String[][] strArr = this.mTimeZoneIdOfCapitalCity;
            if (i >= strArr.length) {
                return null;
            }
            if (iso.equals(strArr[i][0])) {
                String zoneId = this.mTimeZoneIdOfCapitalCity[i][1];
                String str = this.LOG_TAG;
                Rlog.d(str, "uses TimeZone of Capital City:" + this.mTimeZoneIdOfCapitalCity[i][1]);
                return zoneId;
            }
            i++;
        }
    }

    public void OppoRecordNitzTimeZone(int settingType, String zoneId) {
        String dateStr = null;
        try {
            dateStr = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(Long.valueOf(System.currentTimeMillis()));
        } catch (Exception e) {
        }
        if (dateStr == null) {
            dateStr = "";
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "NITZ_TIMEZONE[" + this.mPhone.getPhoneId() + "]:" + settingType + ":" + zoneId + ":" + System.currentTimeMillis() + ":" + dateStr);
        HashMap<String, String> data = new HashMap<>();
        data.put("settingType", String.valueOf(settingType));
        data.put("zoneId", String.valueOf(zoneId));
        data.put("systemCurrentTime", String.valueOf(System.currentTimeMillis()));
        data.put("systemCurrentTime_date", dateStr);
        sendCommonDcsUploader("tzupdate_upload_info", data);
    }

    private boolean sendCommonDcsUploader(String eventId, HashMap<String, String> dataMap) {
        try {
            Class<?> cx = Class.forName("android.app.ActivityThread");
            Object ret = cx.getMethod("sendCommonDcsUploader", String.class, String.class, HashMap.class).invoke(cx, "PSW_Android", eventId, dataMap);
            if (ret != null) {
                return ((Boolean) ret).booleanValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
