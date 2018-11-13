package com.android.server.pm;

import android.content.Context;
import android.os.FileObserver;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ColorSecurePayManager {
    private static boolean DEBUG_SECUREPAY_APP = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String SECUREPAY_DEFAULT_PKG_CONFIG = "/data/oppo/coloros/securepay/enabledapp.xml";
    private static final String SECUREPAY_DEFAULT_PKG_PATH = "/data/oppo/coloros/securepay";
    private static final String TAG = "ColorSecurePayManager";
    private static final String TAG_SECURE_APP = "SecureApp";
    private static ColorSecurePayManager mSecurePay;
    private FileObserverPolicy mConfigFileObserver = null;
    private Context mContext;
    private ArrayList<String> mEnableSecureAppList = new ArrayList();
    private PackageManagerService mPackageManagerService;

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.focusPath.equals(ColorSecurePayManager.SECUREPAY_DEFAULT_PKG_CONFIG)) {
                if (ColorSecurePayManager.DEBUG_SECUREPAY_APP) {
                    Slog.i(ColorSecurePayManager.TAG, "onEvent: focusPath = /data/oppo/coloros/securepay/enabledapp.xml");
                }
                ColorSecurePayManager.this.readConfigFile(true);
            }
        }
    }

    public static synchronized ColorSecurePayManager getInstance() {
        ColorSecurePayManager colorSecurePayManager;
        synchronized (ColorSecurePayManager.class) {
            if (mSecurePay == null) {
                mSecurePay = new ColorSecurePayManager();
            }
            colorSecurePayManager = mSecurePay;
        }
        return colorSecurePayManager;
    }

    public boolean isSecurePayApp(String pkgName) {
        if (pkgName == null || this.mEnableSecureAppList.isEmpty() || !this.mEnableSecureAppList.contains(pkgName)) {
            return false;
        }
        if (DEBUG_SECUREPAY_APP) {
            Log.d(TAG, pkgName + " is secure app.");
        }
        return true;
    }

    private ColorSecurePayManager() {
    }

    public void initSecurePay(Context context, PackageManagerService pms) {
        this.mContext = context;
        this.mPackageManagerService = pms;
        initDir();
        readConfigFile(false);
    }

    public void monitorSecurePay() {
        initFileObserver();
    }

    private void initDir() {
        File defaultAppFilePath = new File(SECUREPAY_DEFAULT_PKG_PATH);
        File defaultAppConfigPath = new File(SECUREPAY_DEFAULT_PKG_CONFIG);
        try {
            if (!defaultAppFilePath.exists()) {
                defaultAppFilePath.mkdirs();
            }
            if (!defaultAppConfigPath.exists()) {
                defaultAppConfigPath.createNewFile();
            }
        } catch (IOException e) {
            Slog.e(TAG, "init defaultAppConfigPath Dir failed!!!");
        }
    }

    private void initFileObserver() {
        this.mConfigFileObserver = new FileObserverPolicy(SECUREPAY_DEFAULT_PKG_CONFIG);
        this.mConfigFileObserver.startWatching();
    }

    /* JADX WARNING: Removed duplicated region for block: B:77:0x010d A:{SYNTHETIC, Splitter: B:77:0x010d} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0101 A:{SYNTHETIC, Splitter: B:71:0x0101} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00f2 A:{SYNTHETIC, Splitter: B:63:0x00f2} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00e3 A:{SYNTHETIC, Splitter: B:55:0x00e3} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00d4 A:{SYNTHETIC, Splitter: B:47:0x00d4} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00c5 A:{SYNTHETIC, Splitter: B:39:0x00c5} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readConfigFile(boolean restore) {
        Exception e;
        XmlPullParserException e2;
        UnsupportedEncodingException e3;
        IOException e4;
        NumberFormatException e5;
        Throwable th;
        File file = new File(SECUREPAY_DEFAULT_PKG_CONFIG);
        if (file.exists()) {
            if (!this.mEnableSecureAppList.isEmpty()) {
                this.mEnableSecureAppList.clear();
            }
            BufferedReader br = null;
            try {
                BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
                try {
                    XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                    parser.setInput(br2);
                    int event;
                    do {
                        event = parser.next();
                        String tag = parser.getName();
                        if (event == 2 && "item".equals(tag)) {
                            int attrCount = parser.getAttributeCount();
                            if (attrCount > 0) {
                                for (int i = 0; i < attrCount; i++) {
                                    String attrName = parser.getAttributeName(i);
                                    String attrValue = parser.getAttributeValue(i);
                                    if ("pkg".equals(attrName)) {
                                        if (DEBUG_SECUREPAY_APP) {
                                            Slog.d(TAG, "attrValue = " + attrValue);
                                        }
                                        this.mEnableSecureAppList.add(attrValue);
                                    }
                                }
                            }
                        }
                    } while (event != 1);
                    if (br2 != null) {
                        try {
                            br2.close();
                        } catch (Exception e6) {
                            e6.printStackTrace();
                        }
                    }
                    br = br2;
                } catch (XmlPullParserException e7) {
                    e2 = e7;
                    br = br2;
                    e2.printStackTrace();
                    if (br != null) {
                        try {
                            br.close();
                        } catch (Exception e62) {
                            e62.printStackTrace();
                        }
                    }
                } catch (UnsupportedEncodingException e8) {
                    e3 = e8;
                    br = br2;
                    e3.printStackTrace();
                    if (br != null) {
                        try {
                            br.close();
                        } catch (Exception e622) {
                            e622.printStackTrace();
                        }
                    }
                } catch (IOException e9) {
                    e4 = e9;
                    br = br2;
                    e4.printStackTrace();
                    if (br != null) {
                        try {
                            br.close();
                        } catch (Exception e6222) {
                            e6222.printStackTrace();
                        }
                    }
                } catch (NumberFormatException e10) {
                    e5 = e10;
                    br = br2;
                    e5.printStackTrace();
                    if (br != null) {
                        try {
                            br.close();
                        } catch (Exception e62222) {
                            e62222.printStackTrace();
                        }
                    }
                } catch (Exception e11) {
                    e62222 = e11;
                    br = br2;
                    try {
                        e62222.printStackTrace();
                        if (br != null) {
                            try {
                                br.close();
                            } catch (Exception e622222) {
                                e622222.printStackTrace();
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (br != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    br = br2;
                    if (br != null) {
                        try {
                            br.close();
                        } catch (Exception e6222222) {
                            e6222222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (XmlPullParserException e12) {
                e2 = e12;
                e2.printStackTrace();
                if (br != null) {
                }
            } catch (UnsupportedEncodingException e13) {
                e3 = e13;
                e3.printStackTrace();
                if (br != null) {
                }
            } catch (IOException e14) {
                e4 = e14;
                e4.printStackTrace();
                if (br != null) {
                }
            } catch (NumberFormatException e15) {
                e5 = e15;
                e5.printStackTrace();
                if (br != null) {
                }
            } catch (Exception e16) {
                e6222222 = e16;
                e6222222.printStackTrace();
                if (br != null) {
                }
            }
        }
    }
}
