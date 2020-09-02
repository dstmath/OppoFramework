package com.android.server.mdmcrsh;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.OppoManager;
import android.os.UEventObserver;
import android.util.Log;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;

class ModemcrashLogObserver extends UEventObserver {
    private static final String NAME_MMKEYLOG = "oppo_critical_log";
    private static final String TAG = "OppomodemService";
    Context mContext;
    Handler mHandler;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final UEventInfo mUEventInfo;

    public ModemcrashLogObserver(Context context, Looper looper) {
        this.mContext = context;
        initHandler(looper);
        this.mUEventInfo = makeObservedUEvent();
    }

    private void initHandler(Looper looper) {
        this.mHandler = new Handler(looper) {
            /* class com.android.server.mdmcrsh.ModemcrashLogObserver.AnonymousClass1 */

            public void handleMessage(Message msg) {
            }
        };
    }

    private String getIssueCause(int id) {
        return "";
    }

    private String getIssueDesc(int id) {
        return this.mContext.getString(201653530);
    }

    /* access modifiers changed from: package-private */
    public void init() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.mdmcrsh.ModemcrashLogObserver.AnonymousClass2 */

            public void run() {
                ModemcrashLogObserver.this.doInit();
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void doInit() {
        if (this.mUEventInfo == null) {
            Slog.d("mUEventInfo is null, should not be here!", "init()");
            return;
        }
        synchronized (this.mLock) {
            Slog.d("ModemcrashLogObserver", "init()");
            char[] buffer = new char[1024];
            FileReader file = null;
            FileReader fileName = null;
            try {
                file = new FileReader(this.mUEventInfo.getSwitchStatePath());
                int curState = Integer.valueOf(new String(buffer, 0, file.read(buffer, 0, 1024)).trim()).intValue();
                fileName = new FileReader(this.mUEventInfo.getSwitchNamePath());
                fileName.read(buffer, 0, 1024);
                Slog.e("ModemcrashLogObserver", "curState:" + curState);
                if (curState < 0) {
                }
                try {
                    file.close();
                } catch (IOException e) {
                }
                try {
                    fileName.close();
                } catch (IOException e2) {
                }
            } catch (FileNotFoundException e3) {
                Slog.w("ModemcrashLogObserver", this.mUEventInfo.getSwitchStatePath() + " not found while attempting to determine initial switch state");
                if (file != null) {
                    try {
                        file.close();
                    } catch (IOException e4) {
                    }
                }
                if (fileName != null) {
                    fileName.close();
                }
            } catch (Exception e5) {
                Slog.e("ModemcrashLogObserver", "", e5);
                if (file != null) {
                    try {
                        file.close();
                    } catch (IOException e6) {
                    }
                }
                if (fileName != null) {
                    fileName.close();
                }
            } catch (Throwable th) {
                if (file != null) {
                    try {
                        file.close();
                    } catch (IOException e7) {
                    }
                }
                if (fileName != null) {
                    try {
                        fileName.close();
                    } catch (IOException e8) {
                    }
                }
                throw th;
            }
        }
        startObserving("DEVPATH=" + this.mUEventInfo.getDevPath());
    }

    private UEventInfo makeObservedUEvent() {
        UEventInfo uei = new UEventInfo(NAME_MMKEYLOG);
        if (uei.checkSwitchExists()) {
            return uei;
        }
        Slog.w("ModemcrashLogObserver", "This kernel does not have mm key log support");
        return null;
    }

    public void onUEvent(final UEventObserver.UEvent event) {
        Log.d(TAG, "MM Key LogEvent UEVENT: " + event.toString());
        this.mHandler.post(new Runnable() {
            /* class com.android.server.mdmcrsh.ModemcrashLogObserver.AnonymousClass3 */

            public void run() {
                try {
                    String name = event.get("SWITCH_NAME");
                    int state = Integer.parseInt(event.get("SWITCH_STATE"));
                    synchronized (ModemcrashLogObserver.this.mLock) {
                        Log.d(ModemcrashLogObserver.TAG, "onUEvent: start write log");
                        ModemcrashLogObserver.this.writeMMKeyLog(name, state);
                        if (state < 800 || state > 999) {
                            if (state == 1001) {
                                Log.d(ModemcrashLogObserver.TAG, "onUEvent: 1001");
                            } else if (state == 509) {
                                ModemcrashLogObserver.this.writeModemCrashLog(name, state);
                            } else {
                                ModemcrashLogObserver.this.writeMMKeyLog(name, state);
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    Slog.e("ModemcrashLogObserver", "Could not parse switch state from event " + event);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void writeMMKeyLog(String name, int state) {
        Log.d(TAG, "writeMMKeyLog: name = " + name + "\n type index = " + state);
    }

    /* access modifiers changed from: private */
    public void writeModemCrashLog(String name, int state) {
        Log.d(TAG, "writeModemCrashLog: name = " + name + "\n type index = " + state);
        StringBuilder sb = new StringBuilder();
        sb.append("Desc: ");
        sb.append(getIssueDesc(state));
        Log.d(TAG, sb.toString());
        int ret = OppoManager.writeLogToPartition(state, name, "NETWORK", "modem_crash", this.mContext.getString(201653514));
        if (ret == -1) {
            Slog.v("writeModemCrashLog", "failed to OppoManager.writeLogToPartition");
            return;
        }
        Slog.v("writeModemCrashLog", "has write :" + ret + " bytes to critical log partition!");
    }

    private final class UEventInfo {
        private static final String VERSION_PATH = "/proc/version";
        private final String mDevName;
        private final String mKernelVersion = getKernelVersion();

        private String getKernelVersion() {
            String kernelVersion = "4.9.117+";
            BufferedReader bReader = null;
            if (!new File(VERSION_PATH).exists()) {
                Slog.e(ModemcrashLogObserver.TAG, "Could not Open /proc/version, set kernelVersion as default: " + kernelVersion);
                return kernelVersion;
            }
            try {
                BufferedReader bReader2 = new BufferedReader(new FileReader(VERSION_PATH));
                String strLine = bReader2.readLine();
                if (strLine != null) {
                    boolean isFound = false;
                    String[] arr = strLine.split("\\s+");
                    int length = arr.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        }
                        String ss = arr[i];
                        if (Pattern.compile("\\d+(\\.\\d+)*.*").matcher(ss).matches()) {
                            kernelVersion = ss;
                            isFound = true;
                            break;
                        }
                        i++;
                    }
                    if (!isFound) {
                        Slog.e(ModemcrashLogObserver.TAG, "Kernel Version not found, set kernelVersion as default: " + kernelVersion);
                    }
                } else {
                    Slog.e(ModemcrashLogObserver.TAG, "/proc/version is empty, set kernelVersion as default: " + kernelVersion);
                }
                try {
                    bReader2.close();
                } catch (Exception e) {
                    Slog.e(ModemcrashLogObserver.TAG, "Fail to close BufferedReader");
                }
            } catch (Exception e2) {
                Slog.e(ModemcrashLogObserver.TAG, "saveModemDumpFile exp:" + e2);
                bReader.close();
            } catch (Throwable th) {
                try {
                    bReader.close();
                } catch (Exception e3) {
                    Slog.e(ModemcrashLogObserver.TAG, "Fail to close BufferedReader");
                }
                throw th;
            }
            return kernelVersion;
        }

        public UEventInfo(String devName) {
            this.mDevName = devName;
        }

        public String getDevName() {
            return this.mDevName;
        }

        public String getDevPath() {
            if (this.mKernelVersion.startsWith("4.4")) {
                return String.format(Locale.US, "/devices/virtual/switch/%s", this.mDevName);
            }
            return String.format(Locale.US, "/devices/virtual/critical_log/%s", this.mDevName);
        }

        public String getSwitchStatePath() {
            if (this.mKernelVersion.startsWith("4.4")) {
                return String.format(Locale.US, "/sys/class/switch/%s/state", this.mDevName);
            }
            return String.format(Locale.US, "/sys/devices/virtual/critical_log/%s/state", this.mDevName);
        }

        public String getSwitchNamePath() {
            if (this.mKernelVersion.startsWith("4.4")) {
                return String.format(Locale.US, "/sys/class/switch/%s/name", this.mDevName);
            }
            return String.format(Locale.US, "/sys/devices/virtual/critical_log/%s/name", this.mDevName);
        }

        public boolean checkSwitchExists() {
            return new File(getSwitchStatePath()).exists();
        }
    }
}
