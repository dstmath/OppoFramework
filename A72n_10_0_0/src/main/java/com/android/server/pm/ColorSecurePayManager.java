package com.android.server.pm;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.wm.startingwindow.ColorStartingWindowContants;
import com.color.settings.ColorSettings;
import com.color.settings.ColorSettingsChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ColorSecurePayManager implements IColorSecurePayManager {
    private static final String CONFIG_ENABLE_SECURE_PAY = "securepay/enabledapp.xml";
    private static final String OPPO_SECURITYPAY_FEATURE = "oppo.securitypay.support";
    private static final int SYSTEM_USER_ID = 0;
    public static final String TAG = "ColorSecurePayManager";
    private static final String TAG_SECURE_APP = "SecureApp";
    private static ColorSecurePayManager sColorSecurePayManager = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private IColorPackageManagerServiceEx mColorPmsEx = null;
    private ColorSettingsChangeListener mConfigChangeListener = new ColorSettingsChangeListener(new Handler()) {
        /* class com.android.server.pm.ColorSecurePayManager.AnonymousClass1 */

        public void onSettingsChange(boolean selfChange, String path, int userId) {
            if (ColorSecurePayManager.sDebugfDetail) {
                Slog.i(ColorSecurePayManager.TAG, "onSettingsChange: path = " + path + ", selfChange = " + selfChange);
            }
            if (userId == ColorSecurePayManager.this.mCurrentUserId && !TextUtils.isEmpty(path) && path.contains(ColorSecurePayManager.CONFIG_ENABLE_SECURE_PAY)) {
                ColorSecurePayManager.this.loadConfig();
            }
        }
    };
    private Context mContext;
    private int mCurrentUserId = 0;
    boolean mDynamicDebug = false;
    private ArrayList<String> mEnableSecureAppList = new ArrayList<>();
    private boolean mHasSecurePayFeature = false;
    private PackageManagerService mPms = null;

    public static synchronized ColorSecurePayManager getInstance() {
        ColorSecurePayManager colorSecurePayManager;
        synchronized (ColorSecurePayManager.class) {
            if (sColorSecurePayManager == null) {
                sColorSecurePayManager = new ColorSecurePayManager();
            }
            colorSecurePayManager = sColorSecurePayManager;
        }
        return colorSecurePayManager;
    }

    private ColorSecurePayManager() {
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        if (pmsEx != null) {
            this.mColorPmsEx = pmsEx;
            this.mPms = pmsEx.getPackageManagerService();
        }
        registerLogModule();
    }

    public boolean isSecurePayApp(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        if (!isSupportSecurePay()) {
            Slog.d(TAG, "this is not support securepay");
            return false;
        }
        ArrayList<String> applist = this.mEnableSecureAppList;
        if (applist.isEmpty() || !applist.contains(pkgName)) {
            return false;
        }
        if (!sDebugfDetail) {
            return true;
        }
        Log.d(TAG, pkgName + " is secure app.");
        return true;
    }

    public void initSecurePay(Context context) {
        if (this.mPms.hasSystemFeature(OPPO_SECURITYPAY_FEATURE, 0)) {
            Slog.i(TAG, "start init SandboxApp initSecurePay");
            this.mContext = context;
            loadConfig();
        }
    }

    public void monitorSecurePay() {
        initFileObserver();
    }

    private void initFileObserver() {
        ColorSettings.registerChangeListener(this.mPms.mContext, (String) null, 0, this.mConfigChangeListener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadConfig() {
        int event;
        int attrCount;
        Slog.i(TAG, "loadConfig mCurrentUserId = " + this.mCurrentUserId);
        ArrayList<String> applist = this.mEnableSecureAppList;
        BufferedReader br = null;
        try {
            InputStream inputStream = ColorSettings.readConfigAsUser(this.mPms.mContext, CONFIG_ENABLE_SECURE_PAY, this.mCurrentUserId, 0);
            if (inputStream != null) {
                if (!applist.isEmpty()) {
                    applist.clear();
                }
                BufferedReader br2 = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(br2);
                do {
                    event = parser.next();
                    String tag = parser.getName();
                    if (event == 2 && "item".equals(tag) && (attrCount = parser.getAttributeCount()) > 0) {
                        for (int i = 0; i < attrCount; i++) {
                            String attrName = parser.getAttributeName(i);
                            String attrValue = parser.getAttributeValue(i);
                            if ("pkg".equals(attrName)) {
                                if (sDebugfDetail) {
                                    Slog.d(TAG, "attrValue = " + attrValue);
                                }
                                applist.add(attrValue);
                            }
                        }
                    }
                } while (event != 1);
                try {
                    br2.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (0 != 0) {
                try {
                    br.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        } catch (XmlPullParserException e3) {
            e3.printStackTrace();
            if (0 != 0) {
                br.close();
            }
        } catch (UnsupportedEncodingException e4) {
            e4.printStackTrace();
            if (0 != 0) {
                br.close();
            }
        } catch (IOException e5) {
            e5.printStackTrace();
            if (0 != 0) {
                br.close();
            }
        } catch (NumberFormatException e6) {
            e6.printStackTrace();
            if (0 != 0) {
                br.close();
            }
        } catch (Exception e7) {
            e7.printStackTrace();
            if (0 != 0) {
                br.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    br.close();
                } catch (Exception e8) {
                    e8.printStackTrace();
                }
            }
            throw th;
        }
    }

    public boolean isSystemAppCall() {
        boolean isSystemCaller = true;
        PackageManagerService packageManagerService = this.mPms;
        boolean z = true;
        if (packageManagerService == null || !packageManagerService.hasSystemFeature(OPPO_SECURITYPAY_FEATURE, 0)) {
            return true;
        }
        int callingUid = Binder.getCallingUid();
        String callerName = this.mPms.getNameForUid(callingUid);
        if (callingUid < 10000 || callerName == null) {
            return true;
        }
        if (callerName.contains(":")) {
            String[] shareName = callerName.split(":");
            if (!(shareName[0] == null || shareName[1] == null)) {
                if (this.mDynamicDebug) {
                    Slog.d(TAG, " getInstalledPackages shareName = " + shareName[0]);
                }
                if (OppoPackageManagerHelper.isShareUid(shareName[0])) {
                    return true;
                }
                String[] shareUidPkg = this.mPms.getPackagesForUid(Integer.parseInt(shareName[1]));
                if (!(shareUidPkg == null || shareUidPkg[0] == null)) {
                    if (this.mDynamicDebug) {
                        Slog.d(TAG, " getInstalledPackages shareUidPkg = " + shareUidPkg[0]);
                    }
                    isSystemCaller = ColorPackageManagerHelper.isOppoApkList(shareUidPkg[0]);
                }
            }
        } else {
            if (!ColorPackageManagerHelper.isOppoApkList(callerName) && !isSecurePayApp(callerName)) {
                z = false;
            }
            isSystemCaller = z;
        }
        if (!this.mDynamicDebug) {
            return isSystemCaller;
        }
        Slog.d(TAG, " getInstalledPackages callerName = " + callerName);
        return isSystemCaller;
    }

    public boolean isSupportSecurePay() {
        return ColorPackageManagerHelper.isSupportSecurePay();
    }

    public boolean isSpecialSecureApp(String pkgName) {
        return ColorPackageManagerHelper.isSpecialSecureApp(pkgName);
    }

    public void startSecurityPayService(String prevPkgName, String nextPkgName, String preClsName, String nextClsName) {
        String prevPkg = "";
        String nextPkg = "";
        boolean isWechatPay = false;
        boolean isExitWechatPay = false;
        if (this.mHasSecurePayFeature) {
            if (prevPkgName != null) {
                prevPkg = prevPkgName;
            }
            if (nextPkgName != null) {
                nextPkg = nextPkgName;
            }
            if (prevPkg.equals(nextPkg) || ColorStartingWindowContants.WECHAT_PACKAGE_NAME.equals(nextPkg)) {
                if (ColorStartingWindowContants.WECHAT_PACKAGE_NAME.equals(nextPkg)) {
                    if (nextClsName != null && OppoListManager.getInstance().getSecurePayActivityList().contains(nextClsName)) {
                        isWechatPay = true;
                    }
                    if (ColorStartingWindowContants.WECHAT_PACKAGE_NAME.equals(prevPkg) && preClsName != null && OppoListManager.getInstance().getSecurePayActivityList().contains(preClsName)) {
                        isExitWechatPay = true;
                    }
                }
            } else if (isSecurePayApp(nextPkg)) {
                Slog.d(TAG, "resume secure pay app : " + nextPkg);
                startSecurePayIntent(nextPkg, prevPkg);
                return;
            }
            if (isWechatPay) {
                Slog.d(TAG, "remuse wechat pay app : " + nextPkg);
                if (isSecurePayApp(nextPkg)) {
                    Slog.d(TAG, "remuse secure pay app : " + nextPkg);
                    startSecurePayIntent(nextPkg, prevPkg);
                    return;
                }
            }
            if (isExitWechatPay && isSecurePayApp(prevPkg)) {
                Slog.d(TAG, "exitWeChatPay");
                exitWeChatPay(prevPkg);
            }
        }
    }

    private void startSecurePayIntent(final String pkgName, final String prevPkgName) {
        this.mPms.mHandler.post(new Runnable() {
            /* class com.android.server.pm.ColorSecurePayManager.AnonymousClass2 */

            public void run() {
                Slog.d(ColorSecurePayManager.TAG, "startSecurePayIntent,pkgName:" + pkgName + " UserHandle.CURRENT=" + UserHandle.CURRENT + ", mCurrentUserId=" + ColorSecurePayManager.this.mCurrentUserId);
                Intent it = new Intent("oppo.intent.action.SECURE_PAY_SCAN_RISK");
                it.setPackage("com.coloros.securepay");
                it.putExtra("extra_key_app_pkg", pkgName);
                it.putExtra("extra_key_pre_pkg", prevPkgName);
                ColorSecurePayManager.this.mPms.mContext.startServiceAsUser(it, UserHandle.of(ColorSecurePayManager.this.mCurrentUserId));
            }
        });
    }

    private void exitWeChatPay(final String pkgName) {
        this.mPms.mHandler.post(new Runnable() {
            /* class com.android.server.pm.ColorSecurePayManager.AnonymousClass3 */

            public void run() {
                Intent intent = new Intent("oppo.intent.action.SECURE_PAY_SCAN_RISK");
                intent.setPackage("com.coloros.securepay");
                intent.putExtra("extra_key_app_pkg", pkgName);
                intent.putExtra("extra_key_exit", true);
                ColorSecurePayManager.this.mPms.mContext.startServiceAsUser(intent, UserHandle.of(ColorSecurePayManager.this.mCurrentUserId));
            }
        });
    }

    public void systemReady() {
        this.mHasSecurePayFeature = this.mPms.hasSystemFeature(OPPO_SECURITYPAY_FEATURE, 0);
        if (this.mHasSecurePayFeature) {
            Slog.i(TAG, "start init SandboxApp ");
            initSecurePay(this.mContext);
            monitorSecurePay();
            registerUserChangeReceiver();
        }
    }

    private void registerUserChangeReceiver() {
        this.mPms.mContext.getApplicationContext().registerReceiver(new UserChangeReceiver(), new IntentFilter("android.intent.action.USER_SWITCHED"));
    }

    /* access modifiers changed from: package-private */
    public class UserChangeReceiver extends BroadcastReceiver {
        UserChangeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                Slog.i(ColorSecurePayManager.TAG, "UserChangeReceiver mCurrentUserId = " + ColorSecurePayManager.this.mCurrentUserId);
                int lastUserId = ColorSecurePayManager.this.mCurrentUserId;
                try {
                    UserInfo userInfo = ActivityManager.getService().getCurrentUser();
                    if (userInfo != null) {
                        ColorSecurePayManager.this.mCurrentUserId = userInfo.id;
                    }
                    if (lastUserId != ColorSecurePayManager.this.mCurrentUserId) {
                        ColorSecurePayManager.this.loadConfig();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog####");
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorSecurePayManager.class.getName());
            Slog.i(TAG, "invoke end!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }
}
