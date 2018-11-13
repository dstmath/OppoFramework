package com.android.server.pm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.FileObserver;
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

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ColorSecurePayManager {
    private static boolean DEBUG_SECUREPAY_APP = false;
    private static final String SANDBOX_RESTORE_ACTION = "oppo.intent.action.SANBOX_RESTORE";
    private static final String SECUREPAY_DEFAULT_PKG_CONFIG = "/data/oppo/securepay/enabledapp.xml";
    private static final String SECUREPAY_DEFAULT_PKG_PATH = "/data/oppo/securepay";
    private static final String TAG = "ColorSecurePayManager";
    private static final String TAG_SECURE_APP = "SecureApp";
    private static ColorSecurePayManager mSecurePay;
    private FileObserverPolicy mConfigFileObserver;
    private Context mContext;
    private ArrayList<String> mEnableSecureAppList;
    private PackageManagerService mPackageManagerService;
    private final BroadcastReceiver mReceiver;

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.focusPath.equals(ColorSecurePayManager.SECUREPAY_DEFAULT_PKG_CONFIG)) {
                if (ColorSecurePayManager.DEBUG_SECUREPAY_APP) {
                    Slog.i(ColorSecurePayManager.TAG, "onEvent: focusPath = /data/oppo/securepay/enabledapp.xml");
                }
                ColorSecurePayManager.this.readConfigFile(true);
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.ColorSecurePayManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.ColorSecurePayManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.ColorSecurePayManager.<clinit>():void");
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
        this.mConfigFileObserver = null;
        this.mEnableSecureAppList = new ArrayList();
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ColorSecurePayManager.SANDBOX_RESTORE_ACTION.equals(action)) {
                    Slog.d(ColorSecurePayManager.TAG, "receive action : " + action);
                    if (ColorSecurePayManager.this.mPackageManagerService != null) {
                        ColorSecurePayManager.this.mPackageManagerService.restoreAllSandboxApp();
                    }
                }
            }
        };
    }

    public void initSecurePay(Context context, PackageManagerService pms) {
        this.mContext = context;
        this.mPackageManagerService = pms;
        initDir();
        readConfigFile(false);
    }

    public void monitorSecurePay() {
        initBroadcast();
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

    private void initBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SANDBOX_RESTORE_ACTION);
        this.mContext.registerReceiver(this.mReceiver, filter, "oppo.permission.OPPO_COMPONENT_SAFE", null);
    }

    private void initFileObserver() {
        this.mConfigFileObserver = new FileObserverPolicy(SECUREPAY_DEFAULT_PKG_CONFIG);
        this.mConfigFileObserver.startWatching();
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x0117 A:{SYNTHETIC, Splitter: B:42:0x0117} */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x016f A:{SYNTHETIC, Splitter: B:86:0x016f} */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0160 A:{SYNTHETIC, Splitter: B:78:0x0160} */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0151 A:{SYNTHETIC, Splitter: B:70:0x0151} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0142 A:{SYNTHETIC, Splitter: B:62:0x0142} */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x0180 A:{SYNTHETIC, Splitter: B:94:0x0180} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readConfigFile(boolean restore) {
        Exception e;
        XmlPullParserException e2;
        Throwable th;
        UnsupportedEncodingException e3;
        IOException e4;
        NumberFormatException e5;
        File file = new File(SECUREPAY_DEFAULT_PKG_CONFIG);
        if (file.exists()) {
            ArrayList<String> tmpSecureAppList = new ArrayList(this.mEnableSecureAppList);
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
                                        if (restore) {
                                            if (tmpSecureAppList.contains(attrValue)) {
                                                if (DEBUG_SECUREPAY_APP) {
                                                    Slog.d(TAG, "remove " + attrValue);
                                                }
                                                tmpSecureAppList.remove(attrValue);
                                            } else {
                                                if (DEBUG_SECUREPAY_APP) {
                                                    Slog.d(TAG, "add " + attrValue);
                                                }
                                                tmpSecureAppList.add(attrValue);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } while (event != 1);
                    if (restore) {
                        if (!tmpSecureAppList.isEmpty()) {
                            restoreSecureApp(tmpSecureAppList);
                        }
                    }
                    if (br2 != null) {
                        try {
                            br2.close();
                        } catch (Exception e6) {
                            e6.printStackTrace();
                        }
                    }
                } catch (XmlPullParserException e7) {
                    e2 = e7;
                    br = br2;
                    try {
                        e2.printStackTrace();
                        if (br != null) {
                            try {
                                br.close();
                            } catch (Exception e62) {
                                e62.printStackTrace();
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (br != null) {
                        }
                        throw th;
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
                    e62222.printStackTrace();
                    if (br != null) {
                        try {
                            br.close();
                        } catch (Exception e622222) {
                            e622222.printStackTrace();
                        }
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

    private void restoreSecureApp(ArrayList<String> restoreAppList) {
        for (String restoreApp : restoreAppList) {
            if (!(this.mPackageManagerService == null || ColorPackageManagerHelper.isSpecialSecureApp(restoreApp))) {
                if (DEBUG_SECUREPAY_APP) {
                    Slog.d(TAG, "restore app is :" + restoreApp);
                }
                this.mPackageManagerService.restoreSingleSandboxApp(restoreApp);
            }
        }
    }
}
