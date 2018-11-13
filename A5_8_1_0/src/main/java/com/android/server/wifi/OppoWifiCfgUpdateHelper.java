package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.oppo.RomUpdateHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.CopyOption;
import java.nio.file.Files;

public class OppoWifiCfgUpdateHelper extends RomUpdateHelper {
    private static final String ACTION_RECOVERY_WIFI = "oppo.intent.action.Other.RECOVERY_WIFI";
    private static final String DATA_FILE_DIR = "/data/misc/wifi/WCNSS_qcom_cfg_new.ini";
    public static final String FILTER_NAME = "qcom_wifi_cfg";
    private static final String MTK_CFG_FILE = "/data/misc/wifi/wifi_fw.cfg";
    private static final String MTK_CFG_FILE_BACK = "/data/misc/wifi/wifi_fw_back.cfg";
    private static final String RO_BOARD_PLATFORM = "ro.board.platform";
    private static final String SYS_FILE_DIR = "/vendor/etc/wifi/WCNSS_qcom_cfg.ini";
    private static final String TAG = "WifiCfgUpdateHelper";
    private static final String VERSION_PATTERN = "#OppoVersion=";
    private static final String WCNSS_CFG_FILE = "/persist/WCNSS_qcom_cfg.ini";
    private static final String WCNSS_CFG_FILE_BACK = "/persist/WCNSS_qcom_cfg_back.ini";
    private static final String WCNSS_CFG_FILE_NEW = "/data/misc/wifi/WCNSS_qcom_cfg_new.ini";
    private String mNewCfgFile = null;
    private String mOldBackupCfgFile = null;
    private String mOldCfgFile = null;

    private class MyTask extends AsyncTask<Void, Void, Void> {
        /* synthetic */ MyTask(OppoWifiCfgUpdateHelper this$0, MyTask -this1) {
            this();
        }

        private MyTask() {
        }

        protected Void doInBackground(Void... params) {
            try {
                if (!new File("/data/misc/wifi/WCNSS_qcom_cfg_new.ini").exists()) {
                    Log.d(OppoWifiCfgUpdateHelper.TAG, " no new cfg file exsits");
                } else if (OppoWifiCfgUpdateHelper.this.hasNewerVersion()) {
                    OppoWifiCfgUpdateHelper.this.replaceOldcfg();
                }
            } catch (SecurityException se) {
                se.printStackTrace();
            }
            return null;
        }
    }

    public OppoWifiCfgUpdateHelper(Context context) {
        super(context, FILTER_NAME, SYS_FILE_DIR, "/data/misc/wifi/WCNSS_qcom_cfg_new.ini");
        setUpdateInfo(null, null);
        String platform = SystemProperties.get(RO_BOARD_PLATFORM);
        if (platform != null ? platform.startsWith("mt") : false) {
            this.mOldCfgFile = MTK_CFG_FILE;
            this.mOldBackupCfgFile = MTK_CFG_FILE_BACK;
        } else {
            this.mOldCfgFile = WCNSS_CFG_FILE;
            this.mOldBackupCfgFile = WCNSS_CFG_FILE_BACK;
        }
        this.mNewCfgFile = "/data/misc/wifi/WCNSS_qcom_cfg_new.ini";
    }

    public void getUpdateFromProvider() {
        super.getUpdateFromProvider();
        new MyTask(this, null).execute(new Void[0]);
    }

    private boolean hasNewerVersion() {
        File oldCfgFile = new File(this.mOldCfgFile);
        File newCfgFile = new File(this.mNewCfgFile);
        int oldVersion = getVersion(oldCfgFile);
        int newVersion = getVersion(newCfgFile);
        Log.d(TAG, "old version: " + oldVersion + "new version :" + newVersion);
        return newVersion > oldVersion;
    }

    private void replaceOldcfg() {
        File oldCfgFile = new File(this.mOldCfgFile);
        File backupCfgFile = new File(this.mOldBackupCfgFile);
        if (oldCfgFile.exists()) {
            if (oldCfgFile.renameTo(backupCfgFile) && copyNewCfg()) {
                File backCfgFile = new File(this.mOldBackupCfgFile);
                File newCfgFile = new File(this.mNewCfgFile);
                backCfgFile.delete();
                newCfgFile.delete();
            }
        } else if (copyNewCfg()) {
            new File(this.mNewCfgFile).delete();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0057 A:{SYNTHETIC, Splitter: B:25:0x0057} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0048 A:{SYNTHETIC, Splitter: B:16:0x0048} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0063 A:{SYNTHETIC, Splitter: B:32:0x0063} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean copyNewCfg() {
        FileNotFoundException notFound;
        IOException ioException;
        Throwable th;
        Log.d(TAG, "copyNewCfg");
        File newCfgFile = new File(this.mNewCfgFile);
        File oldCfgFile = new File(this.mOldCfgFile);
        FileInputStream src = null;
        try {
            FileInputStream src2 = new FileInputStream(newCfgFile);
            try {
                Files.copy(src2, oldCfgFile.toPath(), new CopyOption[0]);
                FileUtils.setPermissions(this.mOldCfgFile, 432, OppoManuConnectManager.UID_DEFAULT, 1010);
                sendRecoverBroadcast();
                if (src2 != null) {
                    try {
                        src2.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            } catch (FileNotFoundException e2) {
                notFound = e2;
                src = src2;
                notFound.printStackTrace();
                if (src != null) {
                    try {
                        src.close();
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }
                return false;
            } catch (IOException e4) {
                ioException = e4;
                src = src2;
                try {
                    ioException.printStackTrace();
                    if (src != null) {
                        try {
                            src.close();
                        } catch (Exception e32) {
                            e32.printStackTrace();
                        }
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (src != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                src = src2;
                if (src != null) {
                    try {
                        src.close();
                    } catch (Exception e322) {
                        e322.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e5) {
            notFound = e5;
            notFound.printStackTrace();
            if (src != null) {
            }
            return false;
        } catch (IOException e6) {
            ioException = e6;
            ioException.printStackTrace();
            if (src != null) {
            }
            return false;
        }
    }

    private void sendRecoverBroadcast() {
        Log.d(TAG, "sendRecoverBroadcast");
        this.mContext.sendBroadcastAsUser(new Intent(ACTION_RECOVERY_WIFI), UserHandle.ALL);
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x009c A:{SYNTHETIC, Splitter: B:47:0x009c} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00a1 A:{SYNTHETIC, Splitter: B:50:0x00a1} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0089 A:{SYNTHETIC, Splitter: B:36:0x0089} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x008e A:{SYNTHETIC, Splitter: B:39:0x008e} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0076 A:{SYNTHETIC, Splitter: B:25:0x0076} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x007b A:{SYNTHETIC, Splitter: B:28:0x007b} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00ac A:{SYNTHETIC, Splitter: B:56:0x00ac} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00b1 A:{SYNTHETIC, Splitter: B:59:0x00b1} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x009c A:{SYNTHETIC, Splitter: B:47:0x009c} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00a1 A:{SYNTHETIC, Splitter: B:50:0x00a1} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0089 A:{SYNTHETIC, Splitter: B:36:0x0089} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x008e A:{SYNTHETIC, Splitter: B:39:0x008e} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0076 A:{SYNTHETIC, Splitter: B:25:0x0076} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x007b A:{SYNTHETIC, Splitter: B:28:0x007b} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00ac A:{SYNTHETIC, Splitter: B:56:0x00ac} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00b1 A:{SYNTHETIC, Splitter: B:59:0x00b1} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getVersion(File file) {
        FileNotFoundException notFound;
        IOException ioexception;
        NumberFormatException foramtException;
        Throwable th;
        int version = 0;
        FileInputStream inputStream = null;
        BufferedReader reader = null;
        try {
            FileInputStream inputStream2 = new FileInputStream(file);
            try {
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(inputStream2));
                try {
                    String versionString = reader2.readLine();
                    if (versionString != null && versionString.startsWith(VERSION_PATTERN)) {
                        String trimedVersion = versionString.substring(VERSION_PATTERN.length()).trim();
                        Log.d(TAG, "getVersion trimedVersion " + trimedVersion + " trimedVersion length " + trimedVersion.length());
                        version = Integer.parseInt(trimedVersion);
                    }
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (Exception e) {
                        }
                    }
                    if (inputStream2 != null) {
                        try {
                            inputStream2.close();
                        } catch (Exception e2) {
                        }
                    }
                    inputStream = inputStream2;
                } catch (FileNotFoundException e3) {
                    notFound = e3;
                    reader = reader2;
                    inputStream = inputStream2;
                    notFound.printStackTrace();
                    if (reader != null) {
                    }
                    if (inputStream != null) {
                    }
                    return version;
                } catch (IOException e4) {
                    ioexception = e4;
                    reader = reader2;
                    inputStream = inputStream2;
                    ioexception.printStackTrace();
                    if (reader != null) {
                    }
                    if (inputStream != null) {
                    }
                    return version;
                } catch (NumberFormatException e5) {
                    foramtException = e5;
                    reader = reader2;
                    inputStream = inputStream2;
                    try {
                        foramtException.printStackTrace();
                        if (reader != null) {
                        }
                        if (inputStream != null) {
                        }
                        return version;
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (Exception e6) {
                            }
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Exception e7) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    inputStream = inputStream2;
                    if (reader != null) {
                    }
                    if (inputStream != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e8) {
                notFound = e8;
                inputStream = inputStream2;
                notFound.printStackTrace();
                if (reader != null) {
                }
                if (inputStream != null) {
                }
                return version;
            } catch (IOException e9) {
                ioexception = e9;
                inputStream = inputStream2;
                ioexception.printStackTrace();
                if (reader != null) {
                }
                if (inputStream != null) {
                }
                return version;
            } catch (NumberFormatException e10) {
                foramtException = e10;
                inputStream = inputStream2;
                foramtException.printStackTrace();
                if (reader != null) {
                }
                if (inputStream != null) {
                }
                return version;
            } catch (Throwable th4) {
                th = th4;
                inputStream = inputStream2;
                if (reader != null) {
                }
                if (inputStream != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e11) {
            notFound = e11;
            notFound.printStackTrace();
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e12) {
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e13) {
                }
            }
            return version;
        } catch (IOException e14) {
            ioexception = e14;
            ioexception.printStackTrace();
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e15) {
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e16) {
                }
            }
            return version;
        } catch (NumberFormatException e17) {
            foramtException = e17;
            foramtException.printStackTrace();
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e18) {
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e19) {
                }
            }
            return version;
        }
        return version;
    }
}
