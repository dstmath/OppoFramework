package android.os;

public class OppoBatteryManager {
    public static final String ACTION_ADDITIONAL_BATTERY_CHANGED = "android.intent.action.ADDITIONAL_BATTERY_CHANGED";
    public static final String EXTRA_BATTERY_CURRENT = "batterycurrent";
    public static final String EXTRA_BATTERY_NOTIFY_CODE = "notifycode";
    public static final String EXTRA_BATTERY_REALTIME_CAPATICAL = "realtime_capatical";
    public static final String EXTRA_BATTERY_SOC_JUMP = "soc_jump";
    public static final String EXTRA_CHARGER_TECHNOLOGY = "chargertechnology";
    public static final String EXTRA_CHARGER_VOLTAGE = "chargervoltage";
    public static final String EXTRA_CHARGE_FAST_CHARGER = "chargefastcharger";
    public static final String EXTRA_CHARGE_PLUGGED = "chargeplugged";
    public static final int FAST_CHARGER_TECHNOLOGY = 1;
    public static final int NORMAL_CHARGER_TECHNOLOGY = 0;
}
