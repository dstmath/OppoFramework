package com.android.server;

import android.os.FileUtils;
import android.os.SystemClock;
import android.os.SystemProperties;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.theia.NoFocusWindow;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AgingCriticalEvent {
    private static final String AGING_BOOT_REASON_CHECK = "ro.runtime.agingbootcheck";
    private static final String BOOT_REASON_FILE = "/sys/power/app_boot";
    private static final boolean DEBUG_LOG = false;
    public static final String EVENT_KERNEL_CRASH = "kernel crash";
    public static final String EVENT_LOW_BATTERY_POWER_OFF = "low battery poweroff";
    public static final String EVENT_MODEM_CRASH = "modem crash";
    public static final String EVENT_POWERKEY_LONGPRESSED_RELEASE = "powerkey longpress released";
    public static final String EVENT_POWERKEY_LONG_PRESSED = "powerkey longpress poweroff";
    public static final String EVENT_SYSTEMSERVER_JAVA_CRASH = "systemserver java crash";
    public static final String EVENT_SYSTEMSERVER_NATIVE_CRASH = "systemserver native crash";
    public static final String EVENT_SYSTEMSERVER_WATCHDOG = "systemserver watchdog";
    public static final String EVENT_SYSTEM_BOOTCOMPLETE = "system bootcomplete";
    public static final String EVENT_SYSTEM_BOOTUP = "system bootup";
    public static final String EVENT_USER_POWER_OFF = "user poweroff";
    private static final String TAG = "AgingCriticalEventTag";
    private static final String criticalEventPath = "/data/system/critical_event.log";
    private static AgingCriticalEvent instance = null;
    private static final String powerLongPressTempProperty = "persist.sys.event.powerpress";
    private static final String systemserverCrashTempProperty = "persist.sys.system.crashinfo";

    private AgingCriticalEvent() {
    }

    public static AgingCriticalEvent getInstance() {
        if (instance == null) {
            instance = new AgingCriticalEvent();
        }
        return instance;
    }

    private void debug_log(String strLog) {
    }

    private String readBootReason() {
        String res = "";
        try {
            FileInputStream fin = new FileInputStream(BOOT_REASON_FILE);
            byte[] buffer = new byte[fin.available()];
            fin.read(buffer);
            StringBuffer sb = new StringBuffer();
            sb.append(new String(buffer));
            res = sb.toString().trim();
            fin.close();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return res;
        }
    }

    public void writeEvent(String strEvent, String... args) {
        if (EVENT_SYSTEM_BOOTUP == strEvent) {
            try {
                if (SystemProperties.getInt(AGING_BOOT_REASON_CHECK, 0) == 0) {
                    String bootReason = readBootReason();
                    if (bootReason.equals("kernel")) {
                        writeEventInner(EVENT_KERNEL_CRASH, new String[0]);
                    } else if (bootReason.equals("modem")) {
                        writeEventInner(EVENT_MODEM_CRASH, new String[0]);
                    }
                    SystemProperties.set(AGING_BOOT_REASON_CHECK, NoFocusWindow.HUNG_CONFIG_ENABLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        writeEventInner(strEvent, args);
    }

    private void writeEventInner(String strEvent, String... args) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dayNow = new Date(System.currentTimeMillis());
        long now = SystemClock.elapsedRealtime();
        String strOutput = (("[" + dateFormat.format(dayNow) + "]") + "[" + Long.toString(now / 1000) + "s]") + strEvent;
        for (String arg : args) {
            strOutput = (strOutput + ";") + arg;
        }
        if (strEvent == EVENT_POWERKEY_LONG_PRESSED) {
            SystemProperties.set(powerLongPressTempProperty, strOutput);
        } else if (strEvent == EVENT_POWERKEY_LONGPRESSED_RELEASE) {
            SystemProperties.set(powerLongPressTempProperty, "");
        } else {
            BufferedWriter out = null;
            FileOutputStream filestream = null;
            OutputStreamWriter writer = null;
            try {
                filestream = new FileOutputStream(criticalEventPath, true);
                writer = new OutputStreamWriter(filestream);
                out = new BufferedWriter(writer);
                if (EVENT_SYSTEM_BOOTUP == strEvent) {
                    String strPowerLongpressOutput = SystemProperties.get(powerLongPressTempProperty, "");
                    if (!strPowerLongpressOutput.equals("")) {
                        SystemProperties.set(powerLongPressTempProperty, "");
                        out.write(strPowerLongpressOutput + StringUtils.LF);
                    }
                    String systemserverCrashOutput = SystemProperties.get(systemserverCrashTempProperty, "");
                    if (!systemserverCrashOutput.equals("")) {
                        SystemProperties.set(systemserverCrashTempProperty, "");
                        out.write(systemserverCrashOutput + StringUtils.LF);
                    }
                    strOutput = "#system bootup====>\n" + strOutput;
                }
                out.write(strOutput + StringUtils.LF);
                out.flush();
                try {
                    out.close();
                    writer.close();
                    filestream.flush();
                    FileUtils.sync(filestream);
                    filestream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                if (out != null) {
                    out.close();
                }
                if (writer != null) {
                    writer.close();
                }
                if (filestream != null) {
                    filestream.flush();
                    FileUtils.sync(filestream);
                    filestream.close();
                }
            } catch (Throwable th) {
                if (out != null) {
                    try {
                        out.close();
                    } catch (Exception e3) {
                        e3.printStackTrace();
                        throw th;
                    }
                }
                if (writer != null) {
                    writer.close();
                }
                if (filestream != null) {
                    filestream.flush();
                    FileUtils.sync(filestream);
                    filestream.close();
                }
                throw th;
            }
            debug_log("writeEvent strOutput:" + strOutput);
        }
    }
}
