package android.os;

public class OppoBatteryManager {
    public static final String ACTION_ADDITIONAL_BATTERY_CHANGED = "android.intent.action.ADDITIONAL_BATTERY_CHANGED";
    public static final String EXTRA_BATTERY_CHG_BALANCE_TYPE = "battery_charge_balance_type";
    public static final String EXTRA_BATTERY_CURRENT = "batterycurrent";
    public static final String EXTRA_BATTERY_MIN_VOLTAGE_TYPE = "battery_min_voltage_type";
    public static final String EXTRA_BATTERY_NOTIFY_CODE = "notifycode";
    public static final String EXTRA_BATTERY_NOW_VOLTAGE_TYPE = "battery_now_voltage_type";
    public static final String EXTRA_BATTERY_QUIET_THERM_TYPE = "battery_quiet_therm_type";
    public static final String EXTRA_BATTERY_REALTIME_CAPATICAL = "realtime_capatical";
    public static final String EXTRA_BATTERY_SOC_JUMP = "soc_jump";
    public static final String EXTRA_CHARGER_TECHNOLOGY = "chargertechnology";
    public static final String EXTRA_CHARGER_VOLTAGE = "chargervoltage";
    public static final String EXTRA_CHARGE_FAST_CHARGER = "chargefastcharger";
    public static final String EXTRA_CHARGE_PLUGGED = "chargeplugged";
    public static final String EXTRA_FAST_CHG_TYPE = "fast_chg_type";
    public static final String EXTRA_OTG_ONLINE = "otgonline";
    public static final String EXTRA_WIRELESS_DEVIATED_CHG_TYPE = "wireless_deviated_chg_type";
    public static final String EXTRA_WIRELESS_REVERSE_CHG_TYPE = "wireless_reverse_chg_type";
    public static final int FAST_CHARGER_TECHNOLOGY = 1;
    public static final int NORMAL_CHARGER_TECHNOLOGY = 0;
}
