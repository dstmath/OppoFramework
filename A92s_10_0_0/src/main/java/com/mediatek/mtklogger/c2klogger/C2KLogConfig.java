package com.mediatek.mtklogger.c2klogger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class C2KLogConfig {
    private static final String TAG = "saber/C2KLogConfig";
    private File mConfigFile = null;
    private Context mContext = null;
    private SharedPreferences mSharedPreferences;

    public C2KLogConfig(Context context) {
        this.mContext = context;
    }

    public void checkConfig() {
        this.mSharedPreferences = this.mContext.getSharedPreferences(C2KLogUtils.CONFIG_FILE_NAME, 0);
        Log.d(TAG, "-->checkConfig()");
        if (checkConfigFile(C2KLogUtils.LOG_PATH_TYPE_INTERNAL_SD_KEY) || checkConfigFile(C2KLogUtils.LOG_PATH_TYPE_EXTERNAL_SD_KEY)) {
            initConfig();
        } else {
            initDefaultConfig();
        }
    }

    private boolean checkConfigFile(String sdType) {
        String storagePath = this.mSharedPreferences.getString(sdType, "");
        this.mConfigFile = new File(storagePath + "/" + C2KLogUtils.C2KLOG_CUSTOMIZE_CONFIG_FOLDER + "/" + C2KLogUtils.C2KLOG_CUSTOMIZE_CONFIG_FILE);
        File file = this.mConfigFile;
        if (file == null || !file.exists()) {
            Log.i(TAG, "The configfile in flow path is not exist : " + this.mConfigFile);
            return false;
        }
        Log.d(TAG, "The configfile in flow path is exist : " + this.mConfigFile);
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x0119  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0122 A[SYNTHETIC, Splitter:B:40:0x0122] */
    /* JADX WARNING: Removed duplicated region for block: B:45:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    private void initConfig() {
        Throwable th;
        Log.d(TAG, "-->initConfig()");
        SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        Properties customizeProp = new Properties();
        FileInputStream customizeInputStream = null;
        try {
            customizeInputStream = new FileInputStream(this.mConfigFile);
            customizeProp.load(customizeInputStream);
            String perLogsize = customizeProp.getProperty(C2KLogUtils.CONIFG_PERLOGSIZE);
            String filterFile = customizeProp.getProperty(C2KLogUtils.CONIFG_FILTERFILE);
            String totalLogsize = customizeProp.getProperty(C2KLogUtils.CONIFG_TOTALLOGSIZE);
            int perLogsizeInt = 6;
            int totalLogsizeInt = C2KLogUtils.DEFAULT_CONIFG_TOTALLOGSIZE;
            if (perLogsize != null) {
                try {
                    perLogsizeInt = Integer.parseInt(perLogsize);
                } catch (NumberFormatException e) {
                    StringBuilder sb = new StringBuilder();
                    try {
                        sb.append("perLogsize ");
                        sb.append(perLogsize);
                        sb.append(" in config file is invalid");
                        Log.w(TAG, sb.toString());
                    } catch (IOException e2) {
                        e = e2;
                    }
                } catch (IOException e3) {
                    e = e3;
                    try {
                        Log.e(TAG, "read customize config file error!" + e.toString());
                        initDefaultConfig();
                        if (customizeInputStream == null) {
                            customizeInputStream.close();
                            return;
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (customizeInputStream != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (customizeInputStream != null) {
                        try {
                            customizeInputStream.close();
                        } catch (IOException e4) {
                            Log.e(TAG, "Fail to close opened customization file.");
                        }
                    }
                    throw th;
                }
            }
            if (totalLogsize != null) {
                try {
                    totalLogsizeInt = Integer.parseInt(totalLogsize);
                } catch (NumberFormatException e5) {
                    Log.w(TAG, "totalLogsize " + totalLogsize + " in config file is invalid");
                }
            }
            Log.d(TAG, "initConfig perLogsizeInt = " + perLogsizeInt + "; totalLogsizeInt = " + totalLogsizeInt + "; filterFile = " + filterFile);
            editor.putString(C2KLogUtils.CONIFG_PATH, this.mConfigFile.getParentFile().getCanonicalPath()).putString(C2KLogUtils.CONIFG_DEVICEPATH, getDevicePath()).putInt(C2KLogUtils.CONIFG_PERLOGSIZE, perLogsizeInt).putInt(C2KLogUtils.CONIFG_TOTALLOGSIZE, totalLogsizeInt).putString(C2KLogUtils.CONIFG_FILTERFILE, filterFile);
            editor.apply();
            try {
                customizeInputStream.close();
            } catch (IOException e6) {
                Log.e(TAG, "Fail to close opened customization file.");
            }
        } catch (IOException e7) {
            e = e7;
            Log.e(TAG, "read customize config file error!" + e.toString());
            initDefaultConfig();
            if (customizeInputStream == null) {
            }
        } catch (Throwable th4) {
            th = th4;
            if (customizeInputStream != null) {
            }
            throw th;
        }
    }

    private void initDefaultConfig() {
        Log.w(TAG, "-->initDefaultConfig()");
        SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        editor.putString(C2KLogUtils.CONIFG_PATH, C2KLogUtils.DEFAULT_CONIFG_PATH).putString(C2KLogUtils.CONIFG_DEVICEPATH, getDevicePath()).putInt(C2KLogUtils.CONIFG_PERLOGSIZE, 6).putString(C2KLogUtils.CONIFG_FILTERFILE, C2KLogUtils.DEFAULT_CONIFG_FILTERFILE);
        editor.apply();
    }

    private String getDevicePath() {
        Log.d(TAG, "-->getDevicePath()");
        String pathFromPro = SystemProperties.get("viatel.device.ets", "sdio.1.ttySDIO");
        Log.d(TAG, "getDevicePath() pathFromPro = " + pathFromPro);
        String[] paths = pathFromPro.split("\\.");
        if (paths.length != 3) {
            return C2KLogUtils.DEFAULT_CONIFG_DEVICEPATH;
        }
        return "/dev/" + paths[2] + paths[1];
    }
}
