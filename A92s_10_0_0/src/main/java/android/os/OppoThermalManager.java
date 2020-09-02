package android.os;

import android.os.health.HealthKeys;
import android.util.DisplayMetrics;
import com.android.internal.logging.nano.MetricsProto;
import java.util.ArrayList;

public class OppoThermalManager {
    public static final String EXTRA_BATTERY_PHONETEMP = "phoneTemp";
    public static int mConfigVersion = 2018030210;
    public static int mCpuLoadRecInterv = 50;
    public static int mCpuLoadRecThreshold = 200;
    public static int mHeat1Align = 100;
    public static int mHeat2Align = 100;
    public static int mHeat3Align = 100;
    public static int mHeatAlign = 100;
    public static int mHeatCaptureCpuFreqInterval = 120;
    public static int mHeatHoldTimeThreshold = 30;
    public static int mHeatHoldUploadTime = HealthKeys.BASE_PACKAGE;
    public static int mHeatIncRatioThreshold = 1;
    public static int mHeatRecInterv = 2;
    public static int mHeatThreshold = DisplayMetrics.DENSITY_440;
    public static int mHeatTopProCounts = 5;
    public static boolean mHeatTopProFeatureOn = true;
    public static int mHeatTopProInterval = 120;
    public static int mLessHeatThreshold = MetricsProto.MetricsEvent.ACTION_NOTIFICATION_GROUP_GESTURE_EXPANDER;
    public static int mMonitorAppLimitTime = 2400000;
    public static ArrayList<String> mMonitorAppList = new ArrayList<>();
    public static int mMoreHeatThreshold = 490;
    public static int mPreHeatDexOatThreshold = 400;
    public static int mPreHeatThreshold = 400;
    public static boolean mRecordThermalHistory = false;
    public static boolean mThermalBatteryTemp = true;
    public static boolean mThermalCaptureLog = false;
    public static int mThermalCaptureLogThreshold = 400;
    public static boolean mThermalFeatureOn = false;
    public static String mThermalHeatPath = "/sys/class/thermal/thermal_zone5/temp";
    public static String mThermalHeatPath1 = "/sys/class/thermal/thermal_zone11/temp";
    public static String mThermalHeatPath2 = "";
    public static String mThermalHeatPath3 = "";
    public static boolean mThermalUploadDcs = true;
    public static boolean mThermalUploadErrLog = false;
    public static boolean mThermalUploadLog = false;
    public static int mTopCpuRecInterv = 20;
    public static int mTopCpuRecThreshold = 50;
}
