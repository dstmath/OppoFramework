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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IOppoAutoInstallService.Stub;
import android.os.Message;
import android.os.OppoAutoInstallManager;
import android.os.StatFs;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Toast;
import com.android.server.LocationManagerService;
import com.android.server.SystemService;
import com.android.server.am.OppoAppStartupManager;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OppoAutoInstallService extends Stub {
    private static final String ACTION_REDTEAMOBILE_ROAMING_MAIN = "com.redteamobile.roaming.MAIN";
    protected static final String KEY_SETTINGS_CHANGEOVER = "changeover_status";
    private static final int MSG_GR_CHECK_INTERNET = 9;
    private static final int MSG_GR_DOWN_INSTALL = 4;
    private static final int MSG_GR_EXIT = 8;
    private static final int MSG_GR_INIT = 3;
    private static final int MSG_GR_INSTALL_TALKBACK = 10;
    private static final int MSG_GR_REINSTALL = 5;
    private static final int MSG_GR_SHOW_EXCEPTION = 6;
    private static final int MSG_GR_SUCC = 7;
    private static final String TAG = "OppoAutoInstallService";
    protected static final String VALUE_CHANGEOVER = "1";
    protected static final String VALUE_NOT_CHANGEOVER = "0";
    private Boolean DEBUG_GR = Boolean.valueOf(OppoAutoInstallManager.DEBUG_GR);
    private String grAbandon = "";
    private String grCancel = "";
    private String grDoDown = "";
    private String grDoDownDown = "";
    private String grDownTipContent = "";
    private String grDownTipContentDown = "";
    private String grExceptionContent = "";
    private String grExceptionContentDown = "";
    private String grFileName = null;
    private String grNetworkContent = "";
    private String grNeverRemind = "";
    private String grNoOppoRoamTip = "";
    private String grNotAccessTip = "";
    private String grOk = "";
    private String grOppoRoam = "";
    private String grOppoRoamTip = "";
    private String grReinstallPTipContent = "";
    private String grSpaceContent = "";
    private String grSucc = "";
    private String grSuccDown = "";
    private String grTalkbackExceptionContent = "";
    private String grTalkbackTipContent = "";
    private String grTipContent = "";
    private String grTipContentDown = "";
    private String grTipInstalling = "";
    private String grTipTitle = "";
    private Boolean hasGrInit = Boolean.valueOf(false);
    private Context mContext = null;
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
            if (msg.what == 3) {
                if (OppoAutoInstallService.this.DEBUG_GR.booleanValue()) {
                    Log.d(OppoAutoInstallService.TAG, "Geloin: we get gr init msg.");
                }
                OppoAutoInstallService.this.initGr();
            } else if (msg.what == 4) {
                if (OppoAutoInstallService.this.DEBUG_GR.booleanValue()) {
                    Log.d(OppoAutoInstallService.TAG, "Geloin: we get gr down msg.");
                }
                if (OppoAutoInstallManager.canCreateDialog.booleanValue()) {
                    OppoAutoInstallManager.canCreateDialog = Boolean.valueOf(false);
                    pkgName = msg.getData().getString("pkgName");
                    am = (ActivityManager) OppoAutoInstallService.this.mContext.getSystemService(OppoAppStartupManager.TYPE_ACTIVITY);
                    for (String pName : OppoAutoInstallManager.grList) {
                        am.forceStopPackage(pName);
                    }
                    am.forceStopPackage(pkgName);
                    if (OppoAutoInstallService.this.DEBUG_GR.booleanValue()) {
                        Log.d(OppoAutoInstallService.TAG, "Geloin: we killed " + pkgName);
                    }
                    Boolean isNetworkOk = OppoAutoInstallService.this.isNetworkOk();
                    Boolean isSpaceOk = OppoAutoInstallService.this.isSpaceOk();
                    pm = OppoAutoInstallService.this.mContext.getPackageManager();
                    try {
                        String tipContent;
                        appInfo = pm.getApplicationInfo(pkgName, 0);
                        appName = appInfo.loadLabel(pm).toString();
                        baseCodePath = appInfo.getBaseCodePath();
                        if (!isNetworkOk.booleanValue()) {
                            tipContent = OppoAutoInstallService.this.grNetworkContent;
                        } else if (!isSpaceOk.booleanValue()) {
                            tipContent = OppoAutoInstallService.this.grSpaceContent;
                        } else if (OppoAutoInstallService.this.mIsGRIn.booleanValue()) {
                            tipContent = String.format(OppoAutoInstallService.this.grTipContent, new Object[]{appName});
                        } else {
                            tipContent = String.format(OppoAutoInstallService.this.grTipContentDown, new Object[]{appName});
                        }
                        if (OppoAutoInstallService.this.isChangeOver()) {
                            if (OppoAutoInstallService.this.mIsGRIn.booleanValue()) {
                                OppoAutoInstallService.this.notInstalls.put(pkgName, baseCodePath);
                                OppoAutoInstallManager.isNoDialogInstalling = Boolean.valueOf(true);
                                Log.d(OppoAutoInstallService.TAG, "installOnDialog: pkgName = " + pkgName);
                                OppoGrThreadFactory.executor.execute(OppoGrThreadFactory.newOppoGrThread(OppoAutoInstallService.this.mContext, OppoAutoInstallService.this.notInstalls));
                                OppoAutoInstallService.this.notInstalls = new HashMap();
                            }
                            return;
                        }
                        builder = new Builder(OppoAutoInstallService.this.mContext);
                        builder.setTitle(OppoAutoInstallService.this.grTipTitle);
                        builder.setMessage(tipContent);
                        if (isNetworkOk.booleanValue() && (isSpaceOk.booleanValue() ^ 1) == 0) {
                            String doDown = OppoAutoInstallService.this.grDoDown;
                            if (!OppoAutoInstallService.this.mIsGRIn.booleanValue()) {
                                doDown = OppoAutoInstallService.this.grDoDownDown;
                            }
                            builder.setPositiveButton(doDown, new OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    OppoAutoInstallService.this.notInstalls.put(pkgName, baseCodePath);
                                    String eMessage = OppoAutoInstallService.this.grExceptionContent;
                                    if (!OppoAutoInstallService.this.mIsGRIn.booleanValue()) {
                                        eMessage = OppoAutoInstallService.this.grExceptionContentDown;
                                    }
                                    String downTipContent = OppoAutoInstallService.this.grDownTipContent;
                                    if (!OppoAutoInstallService.this.mIsGRIn.booleanValue()) {
                                        downTipContent = OppoAutoInstallService.this.grDownTipContentDown;
                                    }
                                    OppoGrThreadFactory.executor.execute(OppoGrThreadFactory.newOppoGrThread(OppoAutoInstallService.this.grFileName, OppoAutoInstallService.this.mContext, baseCodePath, OppoAutoInstallService.this.grTipTitle, downTipContent, OppoAutoInstallService.this.notInstalls, OppoAutoInstallService.this.grAbandon, OppoAutoInstallService.this.grOk, eMessage, appName, pkgName));
                                    OppoAutoInstallService.this.notInstalls = new HashMap();
                                }
                            });
                            builder.setNegativeButton(OppoAutoInstallService.this.grAbandon, new OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    OppoAutoInstallService.this.notInstalls.put(pkgName, baseCodePath);
                                    OppoAutoInstallManager.canCreateDialog = Boolean.valueOf(true);
                                    dialog.cancel();
                                }
                            });
                        } else {
                            builder.setPositiveButton(OppoAutoInstallService.this.grOk, new OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    OppoAutoInstallService.this.notInstalls.put(pkgName, baseCodePath);
                                    OppoAutoInstallManager.canCreateDialog = Boolean.valueOf(true);
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
                    }
                }
            } else if (msg.what == 5) {
                if (OppoAutoInstallService.this.DEBUG_GR.booleanValue()) {
                    Log.d(OppoAutoInstallService.TAG, "Geloin: we get gr reinstall msg.");
                }
                if (OppoAutoInstallManager.canReinstall.booleanValue()) {
                    OppoAutoInstallManager.canReinstall = Boolean.valueOf(false);
                    pkgName = msg.getData().getString("pkgName");
                    am = (ActivityManager) OppoAutoInstallService.this.mContext.getSystemService(OppoAppStartupManager.TYPE_ACTIVITY);
                    for (String pName2 : OppoAutoInstallManager.grList) {
                        am.forceStopPackage(pName2);
                    }
                    am.forceStopPackage(pkgName);
                    if (OppoAutoInstallService.this.DEBUG_GR.booleanValue()) {
                        Log.d(OppoAutoInstallService.TAG, "Geloin: we killed " + pkgName);
                    }
                    pm = OppoAutoInstallService.this.mContext.getPackageManager();
                    try {
                        appInfo = pm.getApplicationInfo(pkgName, 0);
                        appName = appInfo.loadLabel(pm).toString();
                        baseCodePath = appInfo.getBaseCodePath();
                        String grReinstallPTipContentTmp = String.format(OppoAutoInstallService.this.grReinstallPTipContent, new Object[]{appName});
                        eMessage = OppoAutoInstallService.this.grExceptionContent;
                        if (!OppoAutoInstallService.this.mIsGRIn.booleanValue()) {
                            eMessage = OppoAutoInstallService.this.grExceptionContentDown;
                        }
                        OppoGrThreadFactory.executor.execute(OppoGrThreadFactory.newOppoGrThread(OppoAutoInstallService.this.mContext, baseCodePath, OppoAutoInstallService.this.grTipTitle, grReinstallPTipContentTmp, OppoAutoInstallService.this.grAbandon, OppoAutoInstallService.this.grOk, eMessage, appName, pkgName));
                    } catch (Exception e2) {
                    }
                }
            } else if (msg.what == 6) {
                if (OppoAutoInstallService.this.DEBUG_GR.booleanValue()) {
                    Log.d(OppoAutoInstallService.TAG, "Geloin: we get gr show exception msg.");
                }
                eMessage = OppoAutoInstallService.this.grExceptionContent;
                if (!OppoAutoInstallService.this.mIsGRIn.booleanValue()) {
                    eMessage = OppoAutoInstallService.this.grExceptionContentDown;
                }
                String exceptionType = msg.getData().getString("exceptionType");
                if (exceptionType != null) {
                    if (exceptionType.equals("NetworkError")) {
                        eMessage = OppoAutoInstallService.this.grNetworkContent;
                    }
                    if (exceptionType.equals("TalkbackError")) {
                        eMessage = OppoAutoInstallService.this.grTalkbackExceptionContent;
                    }
                }
                builder = new Builder(OppoAutoInstallService.this.mContext);
                builder.setTitle(OppoAutoInstallService.this.grTipTitle);
                builder.setMessage(eMessage);
                builder.setNegativeButton(OppoAutoInstallService.this.grOk, new OnClickListener() {
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
                if (OppoAutoInstallService.this.DEBUG_GR.booleanValue()) {
                    Log.d(OppoAutoInstallService.TAG, "Geloin: we get gr success msg.");
                }
                String succMsg = OppoAutoInstallService.this.grSucc;
                bundle = msg.getData();
                baseCodePath = bundle.getString("baseCodePath");
                appName = bundle.getString("appName");
                pkgName = bundle.getString("pkgName");
                if (appName != null) {
                    succMsg = String.format(OppoAutoInstallService.this.grSuccDown, new Object[]{appName});
                } else if (baseCodePath != null) {
                    succMsg = String.format(OppoAutoInstallService.this.grSucc, new Object[]{baseCodePath});
                }
                builder = new Builder(OppoAutoInstallService.this.mContext);
                builder.setTitle(OppoAutoInstallService.this.grTipTitle);
                builder.setMessage(succMsg);
                builder.setNegativeButton(OppoAutoInstallService.this.grOk, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        if (pkgName == null) {
                            return;
                        }
                        if (pkgName.equals("com.google.android.marvin.talkback")) {
                            Log.d(OppoAutoInstallService.TAG, "send broadcast for talkback install successfully");
                            OppoAutoInstallService.this.mContext.sendBroadcast(new Intent("com.oppo.intent.action.TALKBACK_INSTALL_SUCCESS"));
                            return;
                        }
                        Intent resolveIntent = new Intent("android.intent.action.MAIN", null);
                        resolveIntent.addCategory("android.intent.category.LAUNCHER");
                        resolveIntent.setPackage(pkgName);
                        List<ResolveInfo> apps = OppoAutoInstallService.this.mContext.getPackageManager().queryIntentActivities(resolveIntent, 0);
                        if (apps != null && apps.size() > 0) {
                            ResolveInfo ri = (ResolveInfo) apps.iterator().next();
                            if (ri != null) {
                                String className = ri.activityInfo.name;
                                Intent intent = new Intent("android.intent.action.MAIN");
                                intent.addCategory("android.intent.category.LAUNCHER");
                                intent.setFlags(268435456);
                                intent.setComponent(new ComponentName(pkgName, className));
                                OppoAutoInstallService.this.mContext.startActivity(intent);
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
                if (OppoAutoInstallService.this.DEBUG_GR.booleanValue()) {
                    Log.d(OppoAutoInstallService.TAG, "Geloin: we get gr exit msg, and we will kill " + pkgName);
                }
                am = (ActivityManager) OppoAutoInstallService.this.mContext.getSystemService(OppoAppStartupManager.TYPE_ACTIVITY);
                for (String pName22 : OppoAutoInstallManager.grList) {
                    am.forceStopPackage(pName22);
                }
                am.forceStopPackage(pkgName);
                if (OppoAutoInstallService.this.DEBUG_GR.booleanValue()) {
                    Log.d(OppoAutoInstallService.TAG, "Geloin: we killed " + pkgName);
                }
            } else if (msg.what == 9) {
                if (OppoAutoInstallService.this.DEBUG_GR.booleanValue()) {
                    Log.d(OppoAutoInstallService.TAG, "Geloin: we will check wether can access google service");
                }
                bundle = msg.getData();
                String userInChina = bundle.getString("isInChina");
                String oppoRoamSupportStr = bundle.getString("canSupportOppoRoam");
                String netMsg;
                if (userInChina == null && oppoRoamSupportStr == null) {
                    OppoGrThreadFactory.executor.execute(OppoGrThreadFactory.newOppoGrThread(OppoAutoInstallService.this.mContext));
                } else if (userInChina != null) {
                    netMsg = OppoAutoInstallService.this.grNotAccessTip;
                    builder = new Builder(OppoAutoInstallService.this.mContext);
                    builder.setTitle(OppoAutoInstallService.this.grTipTitle);
                    builder.setMessage(netMsg);
                    builder.setPositiveButton(OppoAutoInstallService.this.grOk, new OnClickListener() {
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
                        netMsg = OppoAutoInstallService.this.grOppoRoamTip;
                    } else {
                        netMsg = OppoAutoInstallService.this.grNoOppoRoamTip;
                    }
                    builder = new Builder(OppoAutoInstallService.this.mContext);
                    builder.setTitle(OppoAutoInstallService.this.grTipTitle);
                    builder.setMessage(netMsg);
                    builder.setNegativeButton(OppoAutoInstallService.this.grOk, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    if (valueOf.booleanValue()) {
                        builder.setPositiveButton(OppoAutoInstallService.this.grOppoRoam, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                Intent intent = new Intent("com.redteamobile.roaming.MAIN");
                                intent.setFlags(268435456);
                                OppoAutoInstallService.this.mContext.startActivity(intent);
                            }
                        });
                    }
                    dialog = builder.create();
                    dialog.getWindow().setType(2003);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    dialog.show();
                }
            } else if (msg.what == 10) {
                bundle = msg.getData();
                pkgName = bundle.getString("pkgName");
                if (OppoAutoInstallService.this.DEBUG_GR.booleanValue()) {
                    Log.d(OppoAutoInstallService.TAG, "Geloin: MSG_GR_INSTALL_TALKBACK" + pkgName);
                }
                appName = bundle.getString("appName");
                baseCodePath = bundle.getString("baseCodePath");
                eMessage = OppoAutoInstallService.this.grTalkbackExceptionContent;
                String str = baseCodePath;
                OppoGrThreadFactory.executor.execute(OppoGrThreadFactory.newOppoGrThread(OppoAutoInstallService.this.mContext, str, OppoAutoInstallService.this.grTipTitle, String.format(OppoAutoInstallService.this.grTalkbackTipContent, new Object[]{appName}), OppoAutoInstallService.this.grAbandon, OppoAutoInstallService.this.grOk, eMessage, appName, pkgName, 5));
            }
        }
    };
    private Boolean mIsGRIn = Boolean.valueOf(false);
    private Map<String, String> notInstalls = new HashMap();

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
            for (String fp : OppoAutoInstallManager.mGrApkPathList) {
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
        String bitDesc = "";
        String densityDesc = "";
        String sdkVersion = "";
        if (Build.SUPPORTED_64_BIT_ABIS.length > 0) {
            bitDesc = "_arm64";
        }
        switch (Integer.valueOf(SystemProperties.getInt("ro.sf.lcd_density", 160)).intValue()) {
            case 240:
                densityDesc = "_hdpi";
                break;
            case Vr2dDisplay.DEFAULT_VIRTUAL_DISPLAY_DPI /*320*/:
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
        this.grTipTitle = rs.getString(17039990);
        this.grTipContent = rs.getString(17039987);
        this.grTipContentDown = rs.getString(17039988);
        this.grReinstallPTipContent = rs.getString(17039982);
        this.grTalkbackTipContent = rs.getString(17039986);
        this.grTalkbackExceptionContent = rs.getString(17039974);
        this.grOk = rs.getString(17039979);
        this.grCancel = rs.getString(17039967);
        this.grDownTipContent = rs.getString(17039970);
        this.grDownTipContentDown = rs.getString(17039971);
        this.grAbandon = rs.getString(17039966);
        this.grNeverRemind = rs.getString(17039976);
        this.grDoDown = rs.getString(17039968);
        this.grDoDownDown = rs.getString(17039969);
        this.grNetworkContent = rs.getString(17039975);
        this.grSpaceContent = rs.getString(17039983);
        this.grSuccDown = rs.getString(17039985);
        this.grExceptionContent = rs.getString(17039972);
        this.grExceptionContentDown = rs.getString(17039973);
        this.grSucc = rs.getString(17039984);
        this.grNotAccessTip = rs.getString(17039978);
        this.grOppoRoamTip = rs.getString(17039981);
        this.grNoOppoRoamTip = rs.getString(17039977);
        this.grOppoRoam = rs.getString(17039980);
        this.grTipInstalling = rs.getString(17039989);
        this.grFileName = "gr" + sdkVersion + bitDesc + densityDesc + ".zip";
        this.hasGrInit = Boolean.valueOf(true);
    }

    private void doTalkbackInstall(String baseCodePath, String appName, String pkgName) {
        Message msg = this.mHandler.obtainMessage(Integer.valueOf(10).intValue());
        Bundle bundle = new Bundle();
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
    }

    public void doGr(String baseCodePath, String appName, String pkgName, String action) {
        Message msg;
        Bundle bundle;
        checkIfGrIn();
        if (!this.hasGrInit.booleanValue()) {
            initGr();
        }
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
            if ("DO_GR_INSTALL_TALKBACK".equals(action)) {
                Log.d(TAG, "doTalkbackInstall");
                doTalkbackInstall(baseCodePath, appName, pkgName);
            } else if (!OppoAutoInstallManager.canCreateDialog.booleanValue() || (OppoAutoInstallManager.canReinstall.booleanValue() ^ 1) != 0) {
            } else {
                if (OppoAutoInstallManager.isNoDialogInstalling.booleanValue()) {
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(OppoAutoInstallService.this.mContext, OppoAutoInstallService.this.grTipInstalling, 0).show();
                        }
                    });
                    Log.d(TAG, "checkIfNoDialogInstallingGr isNoDialogInstalling = " + OppoAutoInstallManager.isNoDialogInstalling);
                    return;
                }
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

    private boolean isChangeOver() {
        String value = Secure.getString(this.mContext.getContentResolver(), KEY_SETTINGS_CHANGEOVER);
        return value != null ? value.equals("1") : false;
    }

    public OppoAutoInstallService(Context context) {
        this.mContext = context;
        if (OppoAutoInstallManager.willUseGrLeader.booleanValue() && (this.hasGrInit.booleanValue() ^ 1) != 0) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(3));
        }
        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
                    Uri data = intent.getData();
                    if (data != null) {
                        String pkgName = data.getSchemeSpecificPart();
                        if (OppoAutoInstallManager.isNeedLeader(pkgName).booleanValue() && OppoAutoInstallManager.canShowDialog(pkgName).booleanValue() && (OppoAutoInstallManager.grExists().booleanValue() ^ 1) != 0) {
                            if (OppoAutoInstallManager.DEBUG_GR) {
                                Log.d(OppoAutoInstallService.TAG, "Geloin: Will leader when installed " + pkgName);
                            }
                            OppoAutoInstallService.this.doGr(null, null, pkgName, "DO_GR_DOWN_INSTALL");
                        }
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiver(receiver, intentFilter);
        BroadcastReceiver mLocaleChangeReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                try {
                    if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
                        OppoAutoInstallService.this.hasGrInit = Boolean.valueOf(false);
                    }
                } catch (Exception e) {
                    Log.d(OppoAutoInstallService.TAG, "Geloin: Exception in mLocaleChangeReceiver.onReceive" + e);
                }
            }
        };
        IntentFilter localeFilter = new IntentFilter();
        localeFilter.addAction("android.intent.action.LOCALE_CHANGED");
        localeFilter.setPriority(1000);
        this.mContext.registerReceiver(mLocaleChangeReceiver, localeFilter);
    }
}
