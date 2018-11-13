package com.android.server;

import android.os.FileUtils;
import android.os.SystemClock;
import android.os.SystemProperties;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
    private static final String criticalEventPath = "/data/system/dropbox/critical_event.log";
    private static AgingCriticalEvent instance = null;
    private static boolean isSecureVersion = false;
    private static final String powerLongPressTempProperty = "persist.sys.event.powerpress";
    private static final String systemserverCrashTempProperty = "persist.sys.system.crashinfo";

    private AgingCriticalEvent() {
        boolean z = true;
        if (SystemProperties.getInt("ro.secure", 1) != 1) {
            z = false;
        }
        isSecureVersion = z;
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
            res = new StringBuffer().append(new String(buffer)).toString().trim();
            fin.close();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return res;
        }
    }

    public void writeEvent(String strEvent, String... args) {
        try {
            if (!isSecureVersion) {
                if (EVENT_SYSTEM_BOOTUP == strEvent && SystemProperties.getInt(AGING_BOOT_REASON_CHECK, 0) == 0) {
                    String bootReason = readBootReason();
                    if (bootReason.equals("kernel")) {
                        writeEventInner(EVENT_KERNEL_CRASH, new String[0]);
                    } else if (bootReason.equals("modem")) {
                        writeEventInner(EVENT_MODEM_CRASH, new String[0]);
                    }
                    SystemProperties.set(AGING_BOOT_REASON_CHECK, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                }
                writeEventInner(strEvent, args);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x01f3 A:{SYNTHETIC, Splitter: B:46:0x01f3} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x01f8 A:{Catch:{ Exception -> 0x0207 }} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x01fd A:{Catch:{ Exception -> 0x0207 }} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x020f A:{SYNTHETIC, Splitter: B:56:0x020f} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0214 A:{Catch:{ Exception -> 0x0223 }} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0219 A:{Catch:{ Exception -> 0x0223 }} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x01f3 A:{SYNTHETIC, Splitter: B:46:0x01f3} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x01f8 A:{Catch:{ Exception -> 0x0207 }} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x01fd A:{Catch:{ Exception -> 0x0207 }} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x020f A:{SYNTHETIC, Splitter: B:56:0x020f} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0214 A:{Catch:{ Exception -> 0x0223 }} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0219 A:{Catch:{ Exception -> 0x0223 }} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x01f3 A:{SYNTHETIC, Splitter: B:46:0x01f3} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x01f8 A:{Catch:{ Exception -> 0x0207 }} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x01fd A:{Catch:{ Exception -> 0x0207 }} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x020f A:{SYNTHETIC, Splitter: B:56:0x020f} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0214 A:{Catch:{ Exception -> 0x0223 }} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0219 A:{Catch:{ Exception -> 0x0223 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeEventInner(String strEvent, String... args) {
        Exception e;
        Writer writer;
        Throwable th;
        String strOutput = (("" + "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())) + "]") + "[" + Long.toString(SystemClock.elapsedRealtime() / 1000) + "s]") + strEvent;
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
            OutputStreamWriter writer2 = null;
            try {
                Writer outputStreamWriter;
                BufferedWriter out2;
                FileOutputStream filestream2 = new FileOutputStream(criticalEventPath, true);
                try {
                    outputStreamWriter = new OutputStreamWriter(filestream2);
                    try {
                        out2 = new BufferedWriter(outputStreamWriter);
                    } catch (Exception e2) {
                        e = e2;
                        writer = outputStreamWriter;
                        filestream = filestream2;
                        try {
                            e.printStackTrace();
                            if (out != null) {
                            }
                            if (writer2 != null) {
                            }
                            if (filestream != null) {
                            }
                            debug_log("writeEvent strOutput:" + strOutput);
                        } catch (Throwable th2) {
                            th = th2;
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (Exception e3) {
                                    e3.printStackTrace();
                                    throw th;
                                }
                            }
                            if (writer2 != null) {
                                writer2.close();
                            }
                            if (filestream != null) {
                                filestream.flush();
                                FileUtils.sync(filestream);
                                filestream.close();
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        writer = outputStreamWriter;
                        filestream = filestream2;
                        if (out != null) {
                        }
                        if (writer2 != null) {
                        }
                        if (filestream != null) {
                        }
                        throw th;
                    }
                } catch (Exception e4) {
                    e3 = e4;
                    filestream = filestream2;
                    e3.printStackTrace();
                    if (out != null) {
                    }
                    if (writer2 != null) {
                    }
                    if (filestream != null) {
                    }
                    debug_log("writeEvent strOutput:" + strOutput);
                } catch (Throwable th4) {
                    th = th4;
                    filestream = filestream2;
                    if (out != null) {
                    }
                    if (writer2 != null) {
                    }
                    if (filestream != null) {
                    }
                    throw th;
                }
                try {
                    if (EVENT_SYSTEM_BOOTUP == strEvent) {
                        String strPowerLongpressOutput = SystemProperties.get(powerLongPressTempProperty, "");
                        if (!strPowerLongpressOutput.equals("")) {
                            SystemProperties.set(powerLongPressTempProperty, "");
                            out2.write(strPowerLongpressOutput + "\n");
                        }
                        String systemserverCrashOutput = SystemProperties.get(systemserverCrashTempProperty, "");
                        if (!systemserverCrashOutput.equals("")) {
                            SystemProperties.set(systemserverCrashTempProperty, "");
                            out2.write(systemserverCrashOutput + "\n");
                        }
                        strOutput = "#system bootup====>\n" + strOutput;
                    }
                    out2.write(strOutput + "\n");
                    out2.flush();
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (Exception e32) {
                            e32.printStackTrace();
                        }
                    }
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                    if (filestream2 != null) {
                        filestream2.flush();
                        FileUtils.sync(filestream2);
                        filestream2.close();
                    }
                    writer = outputStreamWriter;
                    out = out2;
                } catch (Exception e5) {
                    e32 = e5;
                    writer = outputStreamWriter;
                    filestream = filestream2;
                    out = out2;
                    e32.printStackTrace();
                    if (out != null) {
                    }
                    if (writer2 != null) {
                    }
                    if (filestream != null) {
                    }
                    debug_log("writeEvent strOutput:" + strOutput);
                } catch (Throwable th5) {
                    th = th5;
                    writer = outputStreamWriter;
                    filestream = filestream2;
                    out = out2;
                    if (out != null) {
                    }
                    if (writer2 != null) {
                    }
                    if (filestream != null) {
                    }
                    throw th;
                }
            } catch (Exception e6) {
                e32 = e6;
                e32.printStackTrace();
                if (out != null) {
                    try {
                        out.close();
                    } catch (Exception e322) {
                        e322.printStackTrace();
                    }
                }
                if (writer2 != null) {
                    writer2.close();
                }
                if (filestream != null) {
                    filestream.flush();
                    FileUtils.sync(filestream);
                    filestream.close();
                }
                debug_log("writeEvent strOutput:" + strOutput);
            }
            debug_log("writeEvent strOutput:" + strOutput);
        }
    }
}
