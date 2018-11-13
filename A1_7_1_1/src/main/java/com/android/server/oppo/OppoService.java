package com.android.server.oppo;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IOppoService.Stub;
import android.os.Message;
import android.os.OppoManager;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import android.widget.Toast;
import com.android.server.LocationManagerService;
import com.android.server.SystemService;
import com.android.server.am.OppoProcessManager;
import com.oppo.RomUpdateHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class OppoService extends Stub {
    private static final String ACTION_REDTEAMOBILE_ROAMING_MAIN = "com.redteamobile.roaming.MAIN";
    private static final String DATA_FILE_DIR = "data/system/criticallog_config.xml";
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_FLASH_LIGHT = true;
    public static final int DELAY_TIME = 36000000;
    public static final String FILTER_NAME = "criticallog_config";
    private static final int GET_IMEI_NO_DELAY = 20000;
    protected static final String KEY_SETTINGS_CHANGEOVER = "changeover_status";
    private static final int MSG_GET_IMEI_NO = 2;
    private static final int MSG_GR_CHECK_INTERNET = 9;
    private static final int MSG_GR_DOWN_INSTALL = 4;
    private static final int MSG_GR_EXIT = 8;
    private static final int MSG_GR_INIT = 3;
    private static final int MSG_GR_REINSTALL = 5;
    private static final int MSG_GR_SHOW_EXCEPTION = 6;
    private static final int MSG_GR_SUCC = 7;
    private static final int MSG_WRITE_MM_KEY_LOG = 30;
    private static final String NAME_MMKEYLOG = "oppo_critical_log";
    private static final String SYS_FILE_DIR = "system/etc/criticallog_config.xml";
    private static final String TAG = "OppoService";
    protected static final String VALUE_CHANGEOVER = "1";
    protected static final String VALUE_NOT_CHANGEOVER = "0";
    private static final int WRITE_MM_KEY_LOG_DELAY = 10000;
    private static String curName;
    private static int curState;
    private Boolean DEBUG_GR = Boolean.valueOf(OppoManager.DEBUG_GR);
    private String grAbandon = IElsaManager.EMPTY_PACKAGE;
    private String grCancel = IElsaManager.EMPTY_PACKAGE;
    private String grDoDown = IElsaManager.EMPTY_PACKAGE;
    private String grDoDownDown = IElsaManager.EMPTY_PACKAGE;
    private String grDownTipContent = IElsaManager.EMPTY_PACKAGE;
    private String grDownTipContentDown = IElsaManager.EMPTY_PACKAGE;
    private String grExceptionContent = IElsaManager.EMPTY_PACKAGE;
    private String grExceptionContentDown = IElsaManager.EMPTY_PACKAGE;
    private String grFileName = null;
    private String grNetworkContent = IElsaManager.EMPTY_PACKAGE;
    private String grNeverRemind = IElsaManager.EMPTY_PACKAGE;
    private String grNoOppoRoamTip = IElsaManager.EMPTY_PACKAGE;
    private String grNotAccessTip = IElsaManager.EMPTY_PACKAGE;
    private String grOk = IElsaManager.EMPTY_PACKAGE;
    private String grOppoRoam = IElsaManager.EMPTY_PACKAGE;
    private String grOppoRoamTip = IElsaManager.EMPTY_PACKAGE;
    private String grReinstallPTipContent = IElsaManager.EMPTY_PACKAGE;
    private String grSpaceContent = IElsaManager.EMPTY_PACKAGE;
    private String grSucc = IElsaManager.EMPTY_PACKAGE;
    private String grSuccDown = IElsaManager.EMPTY_PACKAGE;
    private String grTipContent = IElsaManager.EMPTY_PACKAGE;
    private String grTipContentDown = IElsaManager.EMPTY_PACKAGE;
    private String grTipInstalling = IElsaManager.EMPTY_PACKAGE;
    private String grTipTitle = IElsaManager.EMPTY_PACKAGE;
    private Boolean hasGrInit = Boolean.valueOf(false);
    private Context mContext;
    private FlashLightControler mFlashLightControler = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            final String pkgName;
            ActivityManager am;
            PackageManager pm;
            ApplicationInfo appInfo;
            final String appName;
            final String baseCodePath;
            Builder builder;
            AlertDialog dialog;
            String eMessage;
            Bundle bundle;
            if (msg.what == 2) {
                if (OppoService.this.mRetry != 0) {
                    if (OppoService.this.isFactoryMode()) {
                        SystemProperties.set("sys.usb.config", "diag_mdm,adb");
                        SystemClock.sleep(100);
                        SystemProperties.set("sys.dial.enable", Boolean.toString(true));
                        OppoService.this.mRetry = 0;
                    } else {
                        OppoService oppoService = OppoService.this;
                        oppoService.mRetry = oppoService.mRetry - 1;
                        sendMessageDelayed(obtainMessage(2), 10000);
                    }
                } else {
                    return;
                }
            } else if (msg.what == 3) {
                if (OppoService.this.DEBUG_GR.booleanValue()) {
                    Log.d(OppoService.TAG, "Geloin: we get gr init msg.");
                }
                OppoService.this.initGr();
            } else if (msg.what == 4) {
                if (OppoService.this.DEBUG_GR.booleanValue()) {
                    Log.d(OppoService.TAG, "Geloin: we get gr down msg.");
                }
                if (OppoManager.canCreateDialog.booleanValue()) {
                    OppoManager.canCreateDialog = Boolean.valueOf(false);
                    pkgName = msg.getData().getString("pkgName");
                    am = (ActivityManager) OppoService.this.mContext.getSystemService("activity");
                    for (String pName : OppoManager.grList) {
                        am.forceStopPackage(pName);
                    }
                    am.forceStopPackage(pkgName);
                    if (OppoService.this.DEBUG_GR.booleanValue()) {
                        Log.d(OppoService.TAG, "Geloin: we killed " + pkgName);
                    }
                    Boolean isNetworkOk = OppoService.this.isNetworkOk();
                    Boolean isSpaceOk = OppoService.this.isSpaceOk();
                    pm = OppoService.this.mContext.getPackageManager();
                    try {
                        String tipContent;
                        appInfo = pm.getApplicationInfo(pkgName, 0);
                        appName = appInfo.loadLabel(pm).toString();
                        baseCodePath = appInfo.getBaseCodePath();
                        if (!isNetworkOk.booleanValue()) {
                            tipContent = OppoService.this.grNetworkContent;
                        } else if (!isSpaceOk.booleanValue()) {
                            tipContent = OppoService.this.grSpaceContent;
                        } else if (OppoService.this.mIsGRIn.booleanValue()) {
                            tipContent = String.format(OppoService.this.grTipContent, new Object[]{appName});
                        } else {
                            tipContent = String.format(OppoService.this.grTipContentDown, new Object[]{appName});
                        }
                        if (OppoService.this.isChangeOver()) {
                            if (OppoService.this.mIsGRIn.booleanValue()) {
                                OppoService.this.notInstalls.put(pkgName, baseCodePath);
                                OppoManager.isNoDialogInstalling = Boolean.valueOf(true);
                                Log.d(OppoService.TAG, "installOnDialog: pkgName = " + pkgName);
                                OppoGrThreadFactory.executor.execute(OppoGrThreadFactory.newOppoGrThread(OppoService.this.mContext, OppoService.this.notInstalls));
                                OppoService.this.notInstalls = new HashMap();
                            }
                            return;
                        }
                        builder = new Builder(OppoService.this.mContext);
                        builder.setTitle(OppoService.this.grTipTitle);
                        builder.setMessage(tipContent);
                        if (isNetworkOk.booleanValue() && isSpaceOk.booleanValue()) {
                            String doDown = OppoService.this.grDoDown;
                            if (!OppoService.this.mIsGRIn.booleanValue()) {
                                doDown = OppoService.this.grDoDownDown;
                            }
                            builder.setPositiveButton(doDown, new OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    OppoService.this.notInstalls.put(pkgName, baseCodePath);
                                    String eMessage = OppoService.this.grExceptionContent;
                                    if (!OppoService.this.mIsGRIn.booleanValue()) {
                                        eMessage = OppoService.this.grExceptionContentDown;
                                    }
                                    String downTipContent = OppoService.this.grDownTipContent;
                                    if (!OppoService.this.mIsGRIn.booleanValue()) {
                                        downTipContent = OppoService.this.grDownTipContentDown;
                                    }
                                    OppoGrThreadFactory.executor.execute(OppoGrThreadFactory.newOppoGrThread(OppoService.this.grFileName, OppoService.this.mContext, baseCodePath, OppoService.this.grTipTitle, downTipContent, OppoService.this.notInstalls, OppoService.this.grAbandon, OppoService.this.grOk, eMessage, appName, pkgName));
                                    OppoService.this.notInstalls = new HashMap();
                                }
                            });
                            builder.setNegativeButton(OppoService.this.grAbandon, new OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    OppoService.this.notInstalls.put(pkgName, baseCodePath);
                                    OppoManager.canCreateDialog = Boolean.valueOf(true);
                                    dialog.cancel();
                                }
                            });
                        } else {
                            builder.setPositiveButton(OppoService.this.grOk, new OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    OppoService.this.notInstalls.put(pkgName, baseCodePath);
                                    OppoManager.canCreateDialog = Boolean.valueOf(true);
                                    dialog.cancel();
                                }
                            });
                        }
                        dialog = builder.create();
                        dialog.getWindow().setType(2003);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setCancelable(false);
                        dialog.show();
                    } catch (Exception e) {
                        return;
                    }
                }
            } else if (msg.what == 5) {
                if (OppoService.this.DEBUG_GR.booleanValue()) {
                    Log.d(OppoService.TAG, "Geloin: we get gr reinstall msg.");
                }
                if (OppoManager.canReinstall.booleanValue()) {
                    OppoManager.canReinstall = Boolean.valueOf(false);
                    pkgName = msg.getData().getString("pkgName");
                    am = (ActivityManager) OppoService.this.mContext.getSystemService("activity");
                    for (String pName2 : OppoManager.grList) {
                        am.forceStopPackage(pName2);
                    }
                    am.forceStopPackage(pkgName);
                    if (OppoService.this.DEBUG_GR.booleanValue()) {
                        Log.d(OppoService.TAG, "Geloin: we killed " + pkgName);
                    }
                    pm = OppoService.this.mContext.getPackageManager();
                    try {
                        appInfo = pm.getApplicationInfo(pkgName, 0);
                        appName = appInfo.loadLabel(pm).toString();
                        baseCodePath = appInfo.getBaseCodePath();
                        String grReinstallPTipContentTmp = String.format(OppoService.this.grReinstallPTipContent, new Object[]{appName});
                        eMessage = OppoService.this.grExceptionContent;
                        if (!OppoService.this.mIsGRIn.booleanValue()) {
                            eMessage = OppoService.this.grExceptionContentDown;
                        }
                        OppoGrThreadFactory.executor.execute(OppoGrThreadFactory.newOppoGrThread(OppoService.this.mContext, baseCodePath, OppoService.this.grTipTitle, grReinstallPTipContentTmp, OppoService.this.grAbandon, OppoService.this.grOk, eMessage, appName, pkgName));
                    } catch (Exception e2) {
                        return;
                    }
                }
            } else if (msg.what == 6) {
                if (OppoService.this.DEBUG_GR.booleanValue()) {
                    Log.d(OppoService.TAG, "Geloin: we get gr show exception msg.");
                }
                eMessage = OppoService.this.grExceptionContent;
                if (!OppoService.this.mIsGRIn.booleanValue()) {
                    eMessage = OppoService.this.grExceptionContentDown;
                }
                String exceptionType = msg.getData().getString("exceptionType");
                if (exceptionType != null) {
                    if (exceptionType.equals("NetworkError")) {
                        eMessage = OppoService.this.grNetworkContent;
                    }
                }
                builder = new Builder(OppoService.this.mContext);
                builder.setTitle(OppoService.this.grTipTitle);
                builder.setMessage(eMessage);
                builder.setNegativeButton(OppoService.this.grOk, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog = builder.create();
                dialog.getWindow().setType(2003);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.show();
            } else if (msg.what == 7) {
                if (OppoService.this.DEBUG_GR.booleanValue()) {
                    Log.d(OppoService.TAG, "Geloin: we get gr success msg.");
                }
                String succMsg = OppoService.this.grSucc;
                bundle = msg.getData();
                baseCodePath = bundle.getString("baseCodePath");
                appName = bundle.getString("appName");
                pkgName = bundle.getString("pkgName");
                if (appName != null) {
                    succMsg = String.format(OppoService.this.grSuccDown, new Object[]{appName});
                } else if (baseCodePath != null) {
                    succMsg = String.format(OppoService.this.grSucc, new Object[]{baseCodePath});
                }
                builder = new Builder(OppoService.this.mContext);
                builder.setTitle(OppoService.this.grTipTitle);
                builder.setMessage(succMsg);
                builder.setNegativeButton(OppoService.this.grOk, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        if (pkgName != null) {
                            Intent resolveIntent = new Intent("android.intent.action.MAIN", null);
                            resolveIntent.addCategory("android.intent.category.LAUNCHER");
                            resolveIntent.setPackage(pkgName);
                            List<ResolveInfo> apps = OppoService.this.mContext.getPackageManager().queryIntentActivities(resolveIntent, 0);
                            if (apps != null && apps.size() > 0) {
                                ResolveInfo ri = (ResolveInfo) apps.iterator().next();
                                if (ri != null) {
                                    String className = ri.activityInfo.name;
                                    Intent intent = new Intent("android.intent.action.MAIN");
                                    intent.addCategory("android.intent.category.LAUNCHER");
                                    intent.setFlags(268435456);
                                    intent.setComponent(new ComponentName(pkgName, className));
                                    OppoService.this.mContext.startActivity(intent);
                                }
                            }
                        }
                    }
                });
                dialog = builder.create();
                dialog.getWindow().setType(2003);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.show();
            } else if (msg.what == 8) {
                pkgName = msg.getData().getString("pkgName");
                if (OppoService.this.DEBUG_GR.booleanValue()) {
                    Log.d(OppoService.TAG, "Geloin: we get gr exit msg, and we will kill " + pkgName);
                }
                am = (ActivityManager) OppoService.this.mContext.getSystemService("activity");
                for (String pName22 : OppoManager.grList) {
                    am.forceStopPackage(pName22);
                }
                am.forceStopPackage(pkgName);
                if (OppoService.this.DEBUG_GR.booleanValue()) {
                    Log.d(OppoService.TAG, "Geloin: we killed " + pkgName);
                }
            } else if (msg.what == 9) {
                if (OppoService.this.DEBUG_GR.booleanValue()) {
                    Log.d(OppoService.TAG, "Geloin: we will check wether can access google service");
                }
                bundle = msg.getData();
                String userInChina = bundle.getString("isInChina");
                String oppoRoamSupportStr = bundle.getString("canSupportOppoRoam");
                String netMsg;
                if (userInChina == null && oppoRoamSupportStr == null) {
                    OppoGrThreadFactory.executor.execute(OppoGrThreadFactory.newOppoGrThread(OppoService.this.mContext));
                } else if (userInChina != null) {
                    netMsg = OppoService.this.grNotAccessTip;
                    builder = new Builder(OppoService.this.mContext);
                    builder.setTitle(OppoService.this.grTipTitle);
                    builder.setMessage(netMsg);
                    builder.setPositiveButton(OppoService.this.grOk, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog = builder.create();
                    dialog.getWindow().setType(2003);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    dialog.show();
                } else {
                    Boolean valueOf = Boolean.valueOf(false);
                    try {
                        valueOf = Boolean.valueOf(oppoRoamSupportStr);
                    } catch (Exception e3) {
                        valueOf = Boolean.valueOf(false);
                    }
                    if (valueOf.booleanValue()) {
                        netMsg = OppoService.this.grOppoRoamTip;
                    } else {
                        netMsg = OppoService.this.grNoOppoRoamTip;
                    }
                    builder = new Builder(OppoService.this.mContext);
                    builder.setTitle(OppoService.this.grTipTitle);
                    builder.setMessage(netMsg);
                    builder.setNegativeButton(OppoService.this.grOk, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    if (valueOf.booleanValue()) {
                        builder.setPositiveButton(OppoService.this.grOppoRoam, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                Intent intent = new Intent("com.redteamobile.roaming.MAIN");
                                intent.setFlags(268435456);
                                OppoService.this.mContext.startActivity(intent);
                            }
                        });
                    }
                    dialog = builder.create();
                    dialog.getWindow().setType(2003);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    dialog.show();
                }
            }
            if (msg.what == 30) {
                OppoService.this.mMMKernelKeyLogObserver.writeMMKeyLog(OppoService.curName, OppoService.curState);
            }
        }
    };
    private Boolean mIsGRIn = Boolean.valueOf(false);
    OppoLogService mLogService;
    private MMKernelKeyLogObserver mMMKernelKeyLogObserver;
    private NetWakeManager mNetWakeManager;
    private int mRetry = 7;
    CriticalLogConfigUpdateHelper mXmlHelper;
    private Map<String, String> notInstalls = new HashMap();

    class CriticalLogConfigUpdateHelper extends RomUpdateHelper {
        public CriticalLogConfigUpdateHelper(Context context, String filterName, String systemFile, String dataFile) {
            super(context, filterName, systemFile, dataFile);
        }

        public void getUpdateFromProvider() {
            super.getUpdateFromProvider();
            OppoManager.updateConfig();
            Log.v(OppoService.TAG, "update criticallog config");
        }
    }

    private class FlashLightControler {
        private static final String FLASH_LIGHT_DRIVER_NODE = "/proc/qcom_flash";
        private static final String FLASH_LIGHT_MODE_CLOSE = "0";
        private static final String FLASH_LIGHT_MODE_OPEN = "1";

        public boolean openFlashLightImpl() {
            return writeValueToFlashLightNode("1");
        }

        public boolean closeFlashLightImpl() {
            return writeValueToFlashLightNode(FLASH_LIGHT_MODE_CLOSE);
        }

        public String getFlashLightStateImpl() {
            return getCurrentFlashLightState();
        }

        private boolean writeValueToFlashLightNode(String value) {
            Slog.d(OppoService.TAG, "writeValueToFlashLightNode, new value:" + value);
            if (value == null || value.length() <= 0) {
                Slog.w(OppoService.TAG, "writeValueToFlashLightNode:value unavailable!");
                return false;
            }
            try {
                FileWriter nodeFileWriter = new FileWriter(new File(FLASH_LIGHT_DRIVER_NODE));
                if (nodeFileWriter == null) {
                    Slog.w(OppoService.TAG, "write flashLight node:FileWriter create failed!");
                    return false;
                }
                try {
                    nodeFileWriter.write(value);
                    try {
                        nodeFileWriter.close();
                        Slog.d(OppoService.TAG, "write flashLight node succeed!");
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        Slog.e(OppoService.TAG, "close flashLight node failed!");
                        return false;
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                    Slog.e(OppoService.TAG, "write flashLight node failed!");
                    return false;
                }
            } catch (IOException e22) {
                e22.printStackTrace();
                Slog.e(OppoService.TAG, "write flashLight node:FileWriter create failed!");
                return false;
            }
        }

        private String getCurrentFlashLightState() {
            File nodeFile = new File(FLASH_LIGHT_DRIVER_NODE);
            char[] valueArray = new char[10];
            String result = IElsaManager.EMPTY_PACKAGE;
            try {
                FileReader nodeFileReader = new FileReader(nodeFile);
                if (nodeFileReader == null) {
                    Slog.w(OppoService.TAG, "getCurrentFlashLightState:FileReader create failed!");
                    return result;
                }
                try {
                    if (nodeFileReader.read(valueArray) > 0) {
                        result = new String(valueArray).trim();
                    }
                    try {
                        nodeFileReader.close();
                        Slog.d(OppoService.TAG, "getCurrentFlashLightState succeed!");
                        return result;
                    } catch (IOException e) {
                        e.printStackTrace();
                        Slog.e(OppoService.TAG, "getCurrentFlashLightState:FileReader close failed!");
                        return result;
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                    Slog.e(OppoService.TAG, "getCurrentFlashLightState:read failed!");
                    return result;
                }
            } catch (IOException e22) {
                e22.printStackTrace();
                Slog.e(OppoService.TAG, "getCurrentFlashLightState:FileReader create failed!");
                return result;
            }
        }
    }

    class MMKernelKeyLogObserver extends UEventObserver {
        private static final String MULTIMEDIA_TAG = "MULTIMEDIA";
        private static final int TYPE_ADSP_CLK_OPEN_TIMEOUT = 311;
        private static final int TYPE_ADSP_LOAD_FAIL = 301;
        private static final int TYPE_BL_EXCEPTION = 310;
        private static final int TYPE_ESD_EXCEPTION = 306;
        private static final int TYPE_FENCE_TIMEOUT = 309;
        private static final int TYPE_GPU_EXCEPTION = 307;
        private static final int TYPE_HP_PA_EXCEPTION = 312;
        private static final int TYPE_IOMMU_ERROR = 308;
        private static final int TYPE_KGSL_EXCEPTION = 304;
        private static final int TYPE_NO_DATA_TO_SHOW = 303;
        private static final int TYPE_SMART_PA_EXCEPTION = 302;
        private static final int TYPE_SOUND_CARD_REGISTER_FAIL = 300;
        private static final int TYPE_VSYNC_EXCEPTION = 305;
        private final Object mLock = new Object();
        private final UEventInfo mUEventInfo = makeObservedUEvent();

        private final class UEventInfo {
            private final String mDevName;

            public UEventInfo(String devName) {
                this.mDevName = devName;
            }

            public String getDevName() {
                return this.mDevName;
            }

            public String getDevPath() {
                return String.format(Locale.US, "/devices/virtual/switch/%s", new Object[]{this.mDevName});
            }

            public String getSwitchStatePath() {
                return String.format(Locale.US, "/sys/class/switch/%s/state", new Object[]{this.mDevName});
            }

            public String getSwitchNamePath() {
                return String.format(Locale.US, "/sys/class/switch/%s/name", new Object[]{this.mDevName});
            }

            public boolean checkSwitchExists() {
                return new File(getSwitchStatePath()).exists();
            }
        }

        private String getIssueCause(int id) {
            switch (id) {
                case 300:
                    return "sound_card_register_fail";
                case 301:
                    return "adps_load_fail";
                case 302:
                    return "smart_pa_exception";
                case TYPE_NO_DATA_TO_SHOW /*303*/:
                    return "no_data_to_show";
                case TYPE_KGSL_EXCEPTION /*304*/:
                    return "kgsl_exception";
                case TYPE_VSYNC_EXCEPTION /*305*/:
                    return "vsync_exception";
                case TYPE_ESD_EXCEPTION /*306*/:
                    return "esd_exception";
                case TYPE_GPU_EXCEPTION /*307*/:
                    return "gpu_exception";
                case TYPE_IOMMU_ERROR /*308*/:
                    return "iommu_error";
                case TYPE_FENCE_TIMEOUT /*309*/:
                    return "fence_timeout";
                case TYPE_BL_EXCEPTION /*310*/:
                    return "bl_exception";
                case TYPE_ADSP_CLK_OPEN_TIMEOUT /*311*/:
                    return "adsp clk open time out";
                case TYPE_HP_PA_EXCEPTION /*312*/:
                    return "headphones pa excetion";
                default:
                    return IElsaManager.EMPTY_PACKAGE;
            }
        }

        private String getIssueDesc(int id) {
            int resId;
            switch (id) {
                case 300:
                case 301:
                case 302:
                case TYPE_ADSP_CLK_OPEN_TIMEOUT /*311*/:
                case TYPE_HP_PA_EXCEPTION /*312*/:
                    resId = 17040940;
                    break;
                case TYPE_NO_DATA_TO_SHOW /*303*/:
                case TYPE_KGSL_EXCEPTION /*304*/:
                case TYPE_VSYNC_EXCEPTION /*305*/:
                case TYPE_ESD_EXCEPTION /*306*/:
                case TYPE_GPU_EXCEPTION /*307*/:
                case TYPE_IOMMU_ERROR /*308*/:
                case TYPE_FENCE_TIMEOUT /*309*/:
                case TYPE_BL_EXCEPTION /*310*/:
                    resId = 17040942;
                    break;
                default:
                    resId = 17040943;
                    break;
            }
            return OppoService.this.mContext.getString(resId);
        }

        void init() {
            if (this.mUEventInfo == null) {
                Slog.d("mUEventInfo is null, should not be here!", "init()");
                return;
            }
            synchronized (this.mLock) {
                Slog.d("MMKernelKeyLogObserver", "init()");
                char[] buffer = new char[1024];
                try {
                    FileReader file = new FileReader(this.mUEventInfo.getSwitchStatePath());
                    int len = file.read(buffer, 0, 1024);
                    file.close();
                    OppoService.curState = Integer.valueOf(new String(buffer, 0, len).trim()).intValue();
                    FileReader fileName = new FileReader(this.mUEventInfo.getSwitchNamePath());
                    len = fileName.read(buffer, 0, 1024);
                    fileName.close();
                    OppoService.curName = new String(buffer, 0, len).trim();
                    Slog.e("MMKernelKeyLogObserver", "curName:" + OppoService.curName + "curState:" + OppoService.curState);
                    if (OppoService.curState < 0) {
                        OppoService.curState = 0 - OppoService.curState;
                    }
                    if (OppoService.curState >= 1 && OppoService.curName != null) {
                        OppoService.this.mHandler.sendMessageDelayed(OppoService.this.mHandler.obtainMessage(30), 10000);
                    }
                } catch (FileNotFoundException e) {
                    Slog.w("MMKernelKeyLogObserver", this.mUEventInfo.getSwitchStatePath() + " not found while attempting to determine initial switch state");
                } catch (Exception e2) {
                    Slog.e("MMKernelKeyLogObserver", IElsaManager.EMPTY_PACKAGE, e2);
                }
            }
            startObserving("DEVPATH=" + this.mUEventInfo.getDevPath());
            return;
        }

        private UEventInfo makeObservedUEvent() {
            UEventInfo uei = new UEventInfo(OppoService.NAME_MMKEYLOG);
            if (uei.checkSwitchExists()) {
                return uei;
            }
            Slog.w("MMKernelKeyLogObserver", "This kernel does not have mm key log support");
            return null;
        }

        public void onUEvent(UEvent event) {
            Log.d(OppoService.TAG, "MM Key LogEvent UEVENT: " + event.toString());
            try {
                String name = event.get("SWITCH_NAME");
                int state = Integer.parseInt(event.get("SWITCH_STATE"));
                synchronized (this.mLock) {
                    Log.d(OppoService.TAG, "onUEvent: start write log");
                    writeMMKeyLog(name, state);
                }
            } catch (NumberFormatException e) {
                Slog.e("MMKernelKeyLogObserver", "Could not parse switch state from event " + event);
            }
        }

        private void writeMMKeyLog(String name, int state) {
            Log.d(OppoService.TAG, "writeMMKeyLog: name = " + name + "\n type index = " + state);
            if (state < 300 || state > 399) {
                Log.e("MMKernelKeyLogObserver", "ingore switch state: " + state);
                return;
            }
            Log.d(OppoService.TAG, "Desc: " + getIssueDesc(state));
            int ret = OppoManager.writeLogToPartition(state, name, MULTIMEDIA_TAG, getIssueCause(state), getIssueDesc(state));
            if (ret == -1) {
                Slog.v("MMKernelKeyLogObserver", "failed to OppoManager.writeLogToPartition");
            } else {
                Slog.v("MMKernelKeyLogObserver", "has write :" + ret + " bytes to critical log partition!");
            }
        }
    }

    private native void native_finalizeRawPartition();

    private native boolean native_initRawPartition();

    private native String native_readCriticalData(int i, int i2);

    private native String native_readRawPartition(int i, int i2);

    private native int native_writeCriticalData(int i, String str);

    private native int native_writeRawPartition(String str);

    private Boolean isNetworkOk() {
        return Boolean.valueOf(true);
    }

    private Boolean isSpaceOk() {
        StatFs fs = new StatFs("/data/system");
        return Boolean.valueOf(fs.getAvailableBlocksLong() * fs.getBlockSizeLong() > 262144000);
    }

    private void checkIfGrIn() {
        this.mIsGRIn = Boolean.valueOf(true);
        if (isSpaceOk().booleanValue()) {
            for (String fp : OppoManager.mGrApkPathList) {
                if (!new File(fp).exists()) {
                    this.mIsGRIn = Boolean.valueOf(false);
                    return;
                }
            }
            return;
        }
        this.mIsGRIn = Boolean.valueOf(false);
    }

    private void initGr() {
        String bitDesc = IElsaManager.EMPTY_PACKAGE;
        String densityDesc = IElsaManager.EMPTY_PACKAGE;
        String sdkVersion = IElsaManager.EMPTY_PACKAGE;
        if (Build.SUPPORTED_64_BIT_ABIS.length > 0) {
            bitDesc = "_arm64";
        }
        switch (Integer.valueOf(SystemProperties.getInt("ro.sf.lcd_density", 160)).intValue()) {
            case 240:
                densityDesc = "_hdpi";
                break;
            case OppoProcessManager.MSG_SUSPEND_PROCESS_DELAY /*320*/:
                densityDesc = "_xhdpi";
                break;
            case SystemService.PHASE_LOCK_SETTINGS_READY /*480*/:
                densityDesc = "_xxhdpi";
                break;
            default:
                densityDesc = "_alldpi";
                break;
        }
        sdkVersion = LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + SystemProperties.get("ro.build.version.sdk", "21");
        Resources rs = this.mContext.getResources();
        this.grTipTitle = rs.getString(17040944);
        this.grTipContent = rs.getString(17040945);
        this.grTipContentDown = rs.getString(17040946);
        this.grReinstallPTipContent = rs.getString(17040949);
        this.grOk = rs.getString(17040951);
        this.grCancel = rs.getString(17040952);
        this.grDownTipContent = rs.getString(17040947);
        this.grDownTipContentDown = rs.getString(17040948);
        this.grAbandon = rs.getString(17040950);
        this.grNeverRemind = rs.getString(17040958);
        this.grDoDown = rs.getString(17040960);
        this.grDoDownDown = rs.getString(17040961);
        this.grNetworkContent = rs.getString(17040953);
        this.grSpaceContent = rs.getString(17040954);
        this.grSuccDown = rs.getString(17040959);
        this.grExceptionContent = rs.getString(17040955);
        this.grExceptionContentDown = rs.getString(17040956);
        this.grSucc = rs.getString(17040957);
        this.grNotAccessTip = rs.getString(17040962);
        this.grOppoRoamTip = rs.getString(17040963);
        this.grNoOppoRoamTip = rs.getString(17040964);
        this.grOppoRoam = rs.getString(17040965);
        this.grTipInstalling = rs.getString(17040966);
        this.grFileName = "gr" + sdkVersion + bitDesc + densityDesc + ".zip";
        this.hasGrInit = Boolean.valueOf(true);
    }

    public void doGr(String baseCodePath, String appName, String pkgName, String action) {
        Message msg;
        Bundle bundle;
        checkIfGrIn();
        Integer what = null;
        if (action != null) {
            if ("DO_GR_SHOW_EXCEPTION".equals(action)) {
                msg = this.mHandler.obtainMessage(Integer.valueOf(6).intValue());
                if (pkgName != null) {
                    bundle = new Bundle();
                    bundle.putString("exceptionType", pkgName);
                    msg.setData(bundle);
                }
                this.mHandler.sendMessage(msg);
                return;
            } else if ("DO_GR_SUCC".equals(action)) {
                msg = this.mHandler.obtainMessage(Integer.valueOf(7).intValue());
                bundle = new Bundle();
                if (appName != null) {
                    bundle.putString("appName", appName);
                }
                if (baseCodePath != null) {
                    bundle.putString("baseCodePath", baseCodePath);
                }
                if (pkgName != null) {
                    bundle.putString("pkgName", pkgName);
                }
                msg.setData(bundle);
                this.mHandler.sendMessage(msg);
                return;
            } else if ("DO_GR_CHECK_INTERNET".equals(action)) {
                msg = this.mHandler.obtainMessage(Integer.valueOf(9).intValue());
                bundle = new Bundle();
                if (baseCodePath != null) {
                    bundle.putString("isInChina", baseCodePath);
                }
                if (appName != null) {
                    bundle.putString("canSupportOppoRoam", appName);
                }
                msg.setData(bundle);
                this.mHandler.sendMessage(msg);
                return;
            }
        }
        if (action != null) {
            if (!this.hasGrInit.booleanValue()) {
                initGr();
            }
            if (OppoManager.isNoDialogInstalling.booleanValue()) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(OppoService.this.mContext, OppoService.this.grTipInstalling, 0).show();
                    }
                });
                Log.d(TAG, "checkIfNoDialogInstallingGr isNoDialogInstalling = " + OppoManager.isNoDialogInstalling);
            } else if (OppoManager.canCreateDialog.booleanValue() && OppoManager.canReinstall.booleanValue()) {
                if ("DO_GR_DOWN_INSTALL".equals(action)) {
                    what = Integer.valueOf(4);
                } else if ("DO_GR_REINSTALL".equals(action)) {
                    what = Integer.valueOf(5);
                } else if ("DO_GR_EXIT".equals(action) && pkgName != null) {
                    what = Integer.valueOf(8);
                }
                if (what != null) {
                    msg = this.mHandler.obtainMessage(what.intValue());
                    bundle = new Bundle();
                    bundle.putString("baseCodePath", baseCodePath);
                    bundle.putString("appName", appName);
                    bundle.putString("pkgName", pkgName);
                    msg.setData(bundle);
                    this.mHandler.sendMessage(msg);
                }
            }
        }
    }

    private boolean isFactoryMode() {
        boolean result = false;
        TelephonyManager manager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (manager == null) {
            Log.e(TAG, "TelephonyManager service is not ready!");
            return false;
        }
        String imei = manager.getDeviceId();
        if (imei == null || (imei != null && VALUE_NOT_CHANGEOVER.equals(imei))) {
            result = true;
        }
        return result;
    }

    public OppoService(Context context) {
        this.mContext = context;
        if (OppoManager.willUseGrLeader.booleanValue() && !this.hasGrInit.booleanValue()) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(3));
        }
        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
                    Uri data = intent.getData();
                    if (data != null) {
                        String pkgName = data.getSchemeSpecificPart();
                        if (OppoManager.isNeedLeader(pkgName).booleanValue() && OppoManager.canShowDialog(pkgName).booleanValue() && !OppoManager.grExists().booleanValue()) {
                            if (OppoManager.DEBUG_GR) {
                                Log.d(OppoService.TAG, "Geloin: Will leader when installed " + pkgName);
                            }
                            OppoService.this.doGr(null, null, pkgName, "DO_GR_DOWN_INSTALL");
                        }
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiver(receiver, intentFilter);
        this.mMMKernelKeyLogObserver = new MMKernelKeyLogObserver();
        this.mMMKernelKeyLogObserver.init();
        this.mNetWakeManager = new NetWakeManager(context);
        this.mNetWakeManager.CoverObservse_init();
        RegisterXmlUpdate(this.mContext);
        SyncCacheToEmmcTimmer();
        this.mLogService = new OppoLogService(this.mContext);
        if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
            startSensorLog(true);
            try {
                if (this.mContext.getPackageManager().getPackageInfo("com.oppo.stethoscope", 0) != null) {
                    Slog.v(TAG, "has stethoscope");
                    startLogSizeMonitor();
                }
            } catch (Exception e) {
                Slog.v(TAG, "get stethoscope error: " + e.toString());
            }
        }
        checkRebootStatus();
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    public String readRawPartition(int offset, int size) {
        return native_readRawPartition(offset, size);
    }

    public int writeRawPartition(String content) {
        return native_writeRawPartition(content);
    }

    public String readCriticalData(int id, int size) {
        return native_readCriticalData(id, size);
    }

    public int writeCriticalData(int id, String content) {
        return native_writeCriticalData(id, content);
    }

    void RegisterXmlUpdate(Context c) {
        this.mXmlHelper = new CriticalLogConfigUpdateHelper(c, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        this.mXmlHelper.init();
        this.mXmlHelper.initUpdateBroadcastReceiver();
    }

    void SyncCacheToEmmcTimmer() {
        Log.v(TAG, "syncCacheToEmmc , start timmer sync ");
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                Log.v(OppoService.TAG, "syncCacheToEmmc , timmer sync ");
                OppoManager.syncCacheToEmmc();
                OppoService.this.mHandler.postDelayed(this, 36000000);
            }
        }, 36000000);
    }

    public void systemReady() {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                Log.d(OppoService.TAG, "systemReady initLogCoreService");
                if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
                    OppoService.this.mLogService.initLogCoreService();
                }
            }
        }, 5000);
    }

    public boolean iScoreLogServiceRunning() {
        if (this.mLogService == null) {
            return false;
        }
        boolean result = this.mLogService.isLogCoreServiceRunning();
        Log.v(TAG, "LogCoreService Running : " + result);
        return result;
    }

    public void StartLogCoreService() {
        Log.v(TAG, "StartLogCoreService : " + this.mLogService);
        if (this.mLogService == null) {
            this.mLogService = new OppoLogService(this.mContext);
        }
        this.mLogService.initLogCoreService();
    }

    public String getOppoLogInfoString(int index) {
        if (Binder.getCallingUid() != 1000) {
            return null;
        }
        return this.mLogService.getOppoLogInfoString(index);
    }

    public void unbindCoreLogService() {
        this.mLogService.unbindService();
    }

    public void startSensorLog(boolean isOutPutFile) {
        this.mLogService.startSensorLog(isOutPutFile);
    }

    public void stopSensorLog() {
        this.mLogService.stopSensorLog();
    }

    public void startLogSizeMonitor() {
        this.mLogService.startLogSizeMonitor();
    }

    public void stopLogSizeMonitor() {
        this.mLogService.stopLogSizeMonitor();
    }

    void checkRebootStatus() {
        String bootReason = readBootReason("/sys/power/app_boot");
        String ftmMode = readBootReason("/sys/systeminfo/ftmmode");
        String silence = SystemProperties.get("persist.sys.oppo.silence", IElsaManager.EMPTY_PACKAGE);
        String fatal = SystemProperties.get("persist.sys.oppo.fatal", IElsaManager.EMPTY_PACKAGE);
        Slog.v(TAG, "bootReason = " + bootReason + "ftmMode = " + ftmMode + " silence = " + silence + "fatal = " + fatal);
        if (!(bootReason.equals("kernel") || fatal.equals("1") || silence.trim().equals("1") || ftmMode.equals("12") || SystemProperties.getLong("ro.runtime.firstboot", 0) != 0)) {
            SystemProperties.set("sys.oppo.reboot", "1");
        }
        SystemProperties.set("persist.sys.oppo.silence", VALUE_NOT_CHANGEOVER);
    }

    private static String readBootReason(String path) {
        String res = IElsaManager.EMPTY_PACKAGE;
        try {
            FileInputStream fin = new FileInputStream(path);
            int length = fin.available();
            if (length != 0) {
                byte[] buffer = new byte[length];
                fin.read(buffer);
                res = new StringBuffer().append(new String(buffer)).toString().trim();
            }
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    private boolean isChangeOver() {
        String value = Secure.getString(this.mContext.getContentResolver(), KEY_SETTINGS_CHANGEOVER);
        return value != null ? value.equals("1") : false;
    }

    public boolean openFlashLight() {
        return getFlashLightControler().openFlashLightImpl();
    }

    public boolean closeFlashLight() {
        return getFlashLightControler().closeFlashLightImpl();
    }

    public String getFlashLightState() {
        return getFlashLightControler().getFlashLightStateImpl();
    }

    private FlashLightControler getFlashLightControler() {
        if (this.mFlashLightControler == null) {
            this.mFlashLightControler = new FlashLightControler();
        }
        return this.mFlashLightControler;
    }
}
