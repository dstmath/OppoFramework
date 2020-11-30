package com.android.server.operator;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.om.IOverlayManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.operator.IOppoOperatorManager;
import android.operator.OppoOperatorManager;
import android.operator.OppoOperatorManagerInternal;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.XmlUtils;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.stat.BackLightStat;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.pm.Installer;
import com.android.server.pm.UserManagerService;
import com.android.server.pm.permission.DefaultPermissionGrantPolicy;
import com.android.server.pm.permission.PermissionManagerServiceInternal;
import com.android.server.slice.SliceClientPermissions;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import vendor.oppo.hardware.nfc.V1_0.OppoNfcChipVersion;

public class OppoOperatorManagerService extends SystemService {
    private static final String ACTION_COTA_MOUNT_COMPLETED = "oppo.intent.action.COTA_MOUNT_COMPLETED";
    private static final String ATTR_NAME = "name";
    private static final Set<String> CALENDAR_PERMISSIONS = new ArraySet();
    private static final Set<String> CAMERA_PERMISSIONS = new ArraySet();
    private static final Set<String> CONTACTS_PERMISSIONS = new ArraySet();
    private static final String COTA_TAG = "COTA";
    private static final String CUSTOMIZE_BROADCAST_ACTION = "oppo.intent.action.OPPO_START_CUSTOMIZE";
    private static final String DATA_APP_DIR = "/data/app";
    private static final HashMap<String, String> DATA_PACKAGES_OF_INTEREST = new HashMap<>();
    private static final String GMAIL_OVERLAY_PACKAGE = "com.oppo.gmail.overlay";
    private static final String GRANT_PERMISSION_POST_INSTALL = "postinstall";
    private static final String GRANT_PERMISSION_PRELOAD = "preload";
    private static final String KEY_OPE_EXTRA = "operator";
    private static final String KEY_REG_EXTRA = "region";
    private static final String LEVEL_DANGEROUS = "dangerous";
    private static final String LEVEL_SENSITIVE = "sensitive";
    private static final Set<String> LOCATION_PERMISSIONS = new ArraySet();
    private static final Set<String> MICROPHONE_PERMISSIONS = new ArraySet();
    private static final Map<String, Set<String>> NAME_TO_SET = new HashMap();
    private static final String OPERATOR_ALTICE = "SFR;MEO;LAREUNION";
    private static final String OPERATOR_TMOBILE = "TMOBILE;CS;TELE2";
    private static final String OPERATOR_VODAFONE = "VODAFONE";
    private static final Set<String> PACKAGES_TO_KILL = new ArraySet();
    private static final Set<String> PACKAGES_TO_NONSTOP = new ArraySet();
    private static final String PACKAGE_STATE_FILE = "/data/system/operator_package_state.xml";
    private static final Set<String> PHONE_PERMISSIONS = new ArraySet();
    private static final String PROP_PERSIST_DT_CUSTOMOFF = "persist.sys.dt_customoff";
    private static final String PROP_RO_DT_CUSTOMOFF = "ro.oppo.dt_customoff";
    private static final Set<String> SENSORS_PERMISSIONS = new ArraySet();
    private static final String SIM_COUNTRY_PROP = "persist.sys.oppo_optb";
    private static final String SIM_OPERATOR_PROP = "persist.sys.oppo_opta";
    private static final Set<String> SINGLE_PERMISSION_SET = new ArraySet();
    private static final Set<String> SMS_PERMISSIONS = new ArraySet();
    private static final Set<String> STORAGE_PERMISSIONS = new ArraySet();
    private static final HashMap<String, String> SUB_OPERATORS = new HashMap<>();
    private static final Set<String> SYSTEM_PACKAGES_OF_INTEREST = new ArraySet();
    private static final String TAG = "OppoOperatorManagerService";
    private static final String TAG_PACKAGE = "package";
    private static final String TAG_PACKAGE_STATE = "package-state";
    private static final String TAG_PACKAGE_UNINSTALLED = "package-uninstalled";
    private static HashMap<String, ArrayList<Element>> mAccounts = new HashMap<>();
    private static HashMap<String, ArrayList<Element>> mBookMarks = new HashMap<>();
    private static HashMap<String, ArrayList<Element>> mEmails = new HashMap<>();
    private static HashMap<String, ArrayList<Element>> mHomePages = new HashMap<>();
    private static HashMap<String, ArrayList<Element>> mPackages = new HashMap<>();
    private static HashMap<String, ArrayList<Element>> mPermissionGroups = new HashMap<>();
    private static HashMap<String, ArrayList<Element>> mPermssions = new HashMap<>();
    private static HashMap<String, ArrayList<Element>> mSignatures = new HashMap<>();
    private static volatile boolean sConfigXmlParsed = false;
    private static boolean sDebugOn = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static boolean sHasCotaConfigMap = false;
    private static final Object sLock = new Object();
    private static boolean sNeedCheckSpn = false;
    private static boolean sNeedEnableSimTriggeredApps = false;
    private static String sOperator = SystemProperties.get("ro.oppo.operator", "");
    private static final String sProject = SystemProperties.get("ro.separate.soft", "");
    private static String sRegion = SystemProperties.get("ro.oppo.regionmark", "");
    private static String sSimCountry;
    private static String sSimOperator;
    private static Set<String> sSpnCandidates = new ArraySet();
    private static ArrayList<String> sUninstalledPackagesByUser = new ArrayList<>();
    DefaultPermissionGrantPolicy mDefaultPermissionPolicy;
    private boolean mEnableDynamicFeature = false;
    private HashMap<String, HashMap<String, Set<String>>> mFeaturesMap = new HashMap<>();
    private H mHandler;
    private ServiceThread mHandlerThread = new ServiceThread(TAG, 0, true);
    private PackageManagerInternal mPackageManagerInternal;
    private PermissionManagerServiceInternal mPermissionManagerServiceInternal;

    static {
        PACKAGES_TO_KILL.add("com.android.email.partnerprovider");
        PACKAGES_TO_KILL.add("com.android.providers.partnerbookmarks");
        PACKAGES_TO_KILL.add("com.oppo.partnerbrowsercustomizations");
        PACKAGES_TO_KILL.add("com.google.android.gm");
        PACKAGES_TO_KILL.add("com.android.chrome");
        PHONE_PERMISSIONS.add("android.permission.READ_PHONE_STATE");
        PHONE_PERMISSIONS.add("android.permission.CALL_PHONE");
        PHONE_PERMISSIONS.add("android.permission.READ_CALL_LOG");
        PHONE_PERMISSIONS.add("android.permission.WRITE_CALL_LOG");
        PHONE_PERMISSIONS.add("com.android.voicemail.permission.ADD_VOICEMAIL");
        PHONE_PERMISSIONS.add("android.permission.USE_SIP");
        PHONE_PERMISSIONS.add("android.permission.PROCESS_OUTGOING_CALLS");
        CONTACTS_PERMISSIONS.add("android.permission.READ_CONTACTS");
        CONTACTS_PERMISSIONS.add("android.permission.WRITE_CONTACTS");
        CONTACTS_PERMISSIONS.add("android.permission.GET_ACCOUNTS");
        LOCATION_PERMISSIONS.add("android.permission.ACCESS_FINE_LOCATION");
        LOCATION_PERMISSIONS.add("android.permission.ACCESS_COARSE_LOCATION");
        CALENDAR_PERMISSIONS.add("android.permission.READ_CALENDAR");
        CALENDAR_PERMISSIONS.add("android.permission.WRITE_CALENDAR");
        SMS_PERMISSIONS.add("android.permission.SEND_SMS");
        SMS_PERMISSIONS.add("android.permission.RECEIVE_SMS");
        SMS_PERMISSIONS.add("android.permission.READ_SMS");
        SMS_PERMISSIONS.add("android.permission.RECEIVE_WAP_PUSH");
        SMS_PERMISSIONS.add("android.permission.RECEIVE_MMS");
        SMS_PERMISSIONS.add("android.permission.READ_CELL_BROADCASTS");
        MICROPHONE_PERMISSIONS.add("android.permission.RECORD_AUDIO");
        CAMERA_PERMISSIONS.add("android.permission.CAMERA");
        SENSORS_PERMISSIONS.add("android.permission.BODY_SENSORS");
        STORAGE_PERMISSIONS.add("android.permission.READ_EXTERNAL_STORAGE");
        STORAGE_PERMISSIONS.add("android.permission.WRITE_EXTERNAL_STORAGE");
        NAME_TO_SET.put("android.permission-group.PHONE", PHONE_PERMISSIONS);
        NAME_TO_SET.put("android.permission-group.CONTACTS", CONTACTS_PERMISSIONS);
        NAME_TO_SET.put("android.permission-group.LOCATION", LOCATION_PERMISSIONS);
        NAME_TO_SET.put("android.permission-group.CALENDAR", CALENDAR_PERMISSIONS);
        NAME_TO_SET.put("android.permission-group.SMS", SMS_PERMISSIONS);
        NAME_TO_SET.put("android.permission-group.MICROPHONE", MICROPHONE_PERMISSIONS);
        NAME_TO_SET.put("android.permission-group.CAMERA", CAMERA_PERMISSIONS);
        NAME_TO_SET.put("android.permission-group.SENSORS", SENSORS_PERMISSIONS);
        NAME_TO_SET.put("android.permission-group.STORAGE", STORAGE_PERMISSIONS);
        SYSTEM_PACKAGES_OF_INTEREST.add("de.telekom.tsc");
        SYSTEM_PACKAGES_OF_INTEREST.add("com.sfr.android.sfrjeux");
        SYSTEM_PACKAGES_OF_INTEREST.add("com.altice.android.myapps");
        SUB_OPERATORS.put("VODAFONE_EEAEUEX", OPERATOR_VODAFONE);
        SUB_OPERATORS.put("VODAFONE_NONEEA", OPERATOR_VODAFONE);
        SUB_OPERATORS.put("EUEX", OPERATOR_TMOBILE);
        SUB_OPERATORS.put("ALTICEEUEX", OPERATOR_ALTICE);
        DATA_PACKAGES_OF_INTEREST.put("VODAFONE_EEAEUEX", "");
        DATA_PACKAGES_OF_INTEREST.put("VODAFONE_NONEEA", "");
        DATA_PACKAGES_OF_INTEREST.put("EUEX", "");
        DATA_PACKAGES_OF_INTEREST.put("ALTICEEUEX", "");
    }

    public OppoOperatorManagerService(Context context) {
        super(context);
        this.mHandlerThread.start();
        init();
    }

    private void init() {
        if (isEuex()) {
            String roCustomoff = SystemProperties.get(PROP_RO_DT_CUSTOMOFF);
            if (TextUtils.isEmpty(SystemProperties.get(PROP_PERSIST_DT_CUSTOMOFF)) && !TextUtils.isEmpty(roCustomoff)) {
                Slog.i(TAG, "setprop PROP_PERSIST_DT_CUSTOMOFF " + roCustomoff);
                SystemProperties.set(PROP_PERSIST_DT_CUSTOMOFF, roCustomoff);
            }
        }
        parseConfigMap();
        readOppoOperatorFeature(Environment.buildPath(Environment.getRootDirectory(), new String[]{"etc", "oppoOperatorFeatures"}));
    }

    /* JADX INFO: Multiple debug info for r4v21 java.lang.String: [D('operatorFiles' java.io.File[]), D('name' java.lang.String)] */
    private void readOppoOperatorFeature(File operatorDir) {
        String operator;
        OppoOperatorManagerService oppoOperatorManagerService = this;
        if (sHasCotaConfigMap) {
            Slog.w(TAG, "skip readOppoOperatorFeature for cota");
        } else if (!operatorDir.exists() || !operatorDir.isDirectory()) {
            Slog.w(TAG, "No directory " + operatorDir + ", skipping");
        } else if (!operatorDir.canRead()) {
            Slog.w(TAG, "Directory " + operatorDir + " cannot be read");
        } else {
            String subOperators = SUB_OPERATORS.get(sOperator + sRegion);
            Slog.i(TAG, "readOppoOperatorFeature subOperators " + subOperators + " sOperator " + sOperator + " sRegion " + sRegion);
            if ("VODAFONE_EEA".equals(sOperator) || "VODAFONE_NONEEA".equals(sOperator)) {
                operator = sOperator + "." + OPERATOR_VODAFONE;
            } else if (isEuex()) {
                if (OPERATOR_TMOBILE.contains(";")) {
                    String[] subOperatorsArray = OPERATOR_TMOBILE.split(";");
                    int totalNum = subOperatorsArray.length;
                    String operator2 = "";
                    for (String subOperator : subOperatorsArray) {
                        operator2 = 0 == totalNum - 1 ? operator2 + "OPPO." + subOperator : operator2 + "OPPO." + subOperator + ";";
                    }
                    operator = operator2;
                } else {
                    operator = "OPPO.TMOBILE;CS;TELE2";
                }
            } else if (!TextUtils.isEmpty(subOperators)) {
                String tag = sOperator;
                if (TextUtils.isEmpty(tag)) {
                    tag = "OPPO";
                }
                if (subOperators.contains(";")) {
                    String[] subOperatorsArray2 = subOperators.split(";");
                    int totalNum2 = subOperatorsArray2.length;
                    int i = 0;
                    operator = "";
                    for (String subOperator2 : subOperatorsArray2) {
                        operator = i == totalNum2 - 1 ? operator + tag + "." + subOperator2 : operator + tag + "." + subOperator2 + ";";
                        i++;
                    }
                    Slog.i(TAG, "readOppoOperatorFeature sub operator feature file string " + operator + " subOperators " + subOperators);
                } else {
                    operator = tag + "." + subOperators;
                }
            } else {
                operator = "null.null";
            }
            if ("null.null".equals(operator) || TextUtils.isEmpty(subOperators)) {
                Slog.i(TAG, "readOppoOperatorFeature no feature files related to this operator  " + sOperator);
                return;
            }
            Slog.i(TAG, "readOppoOperatorFeature feature file string " + operator + " subOperators " + subOperators);
            File[] operatorFiles = operatorDir.listFiles();
            int startIndex = "/system/etc/oppoOperatorFeatures/com.oppo.operator.".length();
            int endIndex = startIndex + 2;
            int count = 0;
            if (operatorFiles != null && operatorFiles.length > 0) {
                String[] nameArray = new String[1];
                String[] operatorArray = new String[1];
                if (operator.contains(";") && subOperators.contains(";")) {
                    nameArray = operator.split(";");
                    operatorArray = subOperators.split(";");
                } else if (operator.contains(";") || subOperators.contains(";")) {
                    Slog.e(TAG, " whoops readOppoOperatorFeature should not happen,  name " + operator + " subOperators " + subOperators);
                    return;
                } else {
                    nameArray[0] = operator;
                    Slog.i(TAG, "readOppoOperatorFeature nameArray[0] " + nameArray[0]);
                    operatorArray[0] = subOperators;
                    Slog.i(TAG, "readOppoOperatorFeature operatorArray[0] " + operatorArray[0]);
                }
                if (nameArray != null && operatorArray != null) {
                    if (nameArray.length == operatorArray.length) {
                        int length = operatorFiles.length;
                        int count2 = 0;
                        int count3 = 0;
                        while (count3 < length) {
                            File file = operatorFiles[count3];
                            int i2 = 0;
                            int count4 = count2;
                            int count5 = 0;
                            for (int length2 = nameArray.length; count5 < length2; length2 = length2) {
                                String name = nameArray[count5];
                                if (i2 < operatorArray.length) {
                                    String subOperator3 = operatorArray[i2];
                                    i2++;
                                    if (file.getPath().contains(name)) {
                                        String region = file.getPath().substring(startIndex, endIndex);
                                        if (!TextUtils.isEmpty(region)) {
                                            if (testAllUpperCase(region)) {
                                                synchronized (oppoOperatorManagerService.mFeaturesMap) {
                                                    oppoOperatorManagerService.readOppoFeatureLocked(file, region, subOperator3);
                                                    count4++;
                                                }
                                            }
                                        }
                                        Slog.i(TAG, "readOppoOperatorFeature skip operator file " + file);
                                    }
                                    count5++;
                                    oppoOperatorManagerService = this;
                                    operatorFiles = operatorFiles;
                                } else {
                                    Slog.e(TAG, " whoops out of range i " + i2 + " operatorArray.length " + operatorArray.length);
                                    return;
                                }
                            }
                            count3++;
                            oppoOperatorManagerService = this;
                            count2 = count4;
                        }
                        count = count2;
                    }
                }
                Slog.e(TAG, "invalid nameArray " + nameArray + " or operatorArray " + operatorArray);
                return;
            }
            Slog.i(TAG, "readOppoOperatorFeature total valid feature files number  " + count + " sOperator " + sOperator + " sRegion " + sRegion);
        }
    }

    public static boolean testAllUpperCase(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 'a' && c <= 'z') {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0043 A[Catch:{ XmlPullParserException -> 0x00b4, IOException -> 0x00ae, all -> 0x00ac }, LOOP:1: B:11:0x0043->B:42:0x0043, LOOP_START] */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00a4 A[Catch:{ XmlPullParserException -> 0x00b4, IOException -> 0x00ae, all -> 0x00ac }] */
    private void readOppoFeatureLocked(File file, String region, String operator) {
        int type;
        Slog.i(TAG, "readOppoFeatureLocked " + file + " region " + region);
        try {
            FileReader permReader = new FileReader(file);
            HashSet<String> featureSets = new HashSet<>();
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(permReader);
                while (true) {
                    type = parser.next();
                    if (type == 2 || type == 1) {
                        if (type != 2) {
                            while (true) {
                                XmlUtils.nextElement(parser);
                                if (parser.getEventType() == 1) {
                                    break;
                                } else if ("feature".equals(parser.getName())) {
                                    String fname = parser.getAttributeValue(null, "name");
                                    if (fname == null) {
                                        Slog.w(TAG, "<feature> without name in " + file + " at " + parser.getPositionDescription());
                                    } else {
                                        Slog.i(TAG, "Got feature " + fname);
                                        featureSets.add(fname);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                } else {
                                    XmlUtils.skipCurrentTag(parser);
                                }
                            }
                            IoUtils.closeQuietly(permReader);
                            if (!this.mFeaturesMap.containsKey(operator)) {
                                this.mFeaturesMap.put(operator, new HashMap<>());
                            }
                            this.mFeaturesMap.get(operator).put(region, featureSets);
                            return;
                        }
                        throw new XmlPullParserException("No start tag found");
                    }
                }
                if (type != 2) {
                }
            } catch (XmlPullParserException e) {
                Slog.w(TAG, "Got exception parsing permissions.", e);
            } catch (IOException e2) {
                Slog.w(TAG, "Got exception parsing permissions.", e2);
            } catch (Throwable th) {
                IoUtils.closeQuietly(permReader);
                throw th;
            }
        } catch (FileNotFoundException e3) {
            Slog.w(TAG, "Couldn't find or open permissions file " + file);
        }
    }

    private static boolean isEuex() {
        if (!TextUtils.isEmpty(sOperator) || !"EUEX".equals(sRegion)) {
            return false;
        }
        return true;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: com.android.server.operator.OppoOperatorManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v2, types: [com.android.server.operator.OppoOperatorManagerService$BinderService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        Slog.i(TAG, "onStart");
        this.mHandler = new H(this.mHandlerThread.getLooper());
        publishBinderService(KEY_OPE_EXTRA, new BinderService());
        publishLocalService(OppoOperatorManagerInternal.class, new LocalService());
        IntentFilter filter = new IntentFilter();
        filter.addAction(CUSTOMIZE_BROADCAST_ACTION);
        filter.setPriority(1000);
        registerCotaMountFinishReceiver();
        getContext().registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.operator.OppoOperatorManagerService.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                String broadcastOperator = intent.getStringExtra(OppoOperatorManagerService.KEY_OPE_EXTRA);
                String broadcastCountry = intent.getStringExtra(OppoOperatorManagerService.KEY_REG_EXTRA);
                if (!OppoOperatorManagerService.this.mEnableDynamicFeature) {
                    Slog.w(OppoOperatorManagerService.TAG, "mEnableDynamicFeature set true through broadcast");
                    OppoOperatorManagerService.this.mEnableDynamicFeature = true;
                }
                if (TextUtils.isEmpty(OppoOperatorManagerService.sSimOperator)) {
                    String unused = OppoOperatorManagerService.sSimOperator = broadcastOperator;
                }
                if (TextUtils.isEmpty(OppoOperatorManagerService.sSimCountry)) {
                    String unused2 = OppoOperatorManagerService.sSimCountry = broadcastCountry;
                }
                Slog.i(OppoOperatorManagerService.TAG, "onReceive " + intent + " broadcastOperator " + broadcastOperator + " broadcastCountry " + broadcastCountry + " sSimOperator  " + OppoOperatorManagerService.sSimOperator + " sSimCountry " + OppoOperatorManagerService.sSimCountry + " mEnableDynamicFeature " + OppoOperatorManagerService.this.mEnableDynamicFeature);
                OppoOperatorManagerService.this.enableGmailOverlay(broadcastOperator, broadcastCountry);
                OppoOperatorManagerService.this.killBackgroundGmsProcesses();
                OppoOperatorManagerService.this.enableSimTriggeredApps(OppoOperatorManagerService.sSimOperator, OppoOperatorManagerService.sSimCountry);
            }
        }, UserHandle.ALL, filter, null, this.mHandler);
        getPackageManagerInternal().getPackageList(new PackageManagerInternal.PackageListObserver() {
            /* class com.android.server.operator.OppoOperatorManagerService.AnonymousClass2 */

            public void onPackageAdded(String packageName, int uid) {
                String key = OppoOperatorManagerService.sOperator + OppoOperatorManagerService.sRegion;
                if (!TextUtils.isEmpty((CharSequence) OppoOperatorManagerService.DATA_PACKAGES_OF_INTEREST.get(key)) && ((String) OppoOperatorManagerService.DATA_PACKAGES_OF_INTEREST.get(key)).contains(packageName) && OppoOperatorManagerService.sUninstalledPackagesByUser.contains(packageName)) {
                    OppoOperatorManagerService.sUninstalledPackagesByUser.remove(packageName);
                    Slog.i(OppoOperatorManagerService.TAG, "onPackageAdded for interested package " + packageName);
                    OppoOperatorManagerService.this.persistenceAsync();
                }
            }

            public void onPackageRemoved(String packageName, int uid) {
                String key = OppoOperatorManagerService.sOperator + OppoOperatorManagerService.sRegion;
                if (!TextUtils.isEmpty((CharSequence) OppoOperatorManagerService.DATA_PACKAGES_OF_INTEREST.get(key)) && ((String) OppoOperatorManagerService.DATA_PACKAGES_OF_INTEREST.get(key)).contains(packageName) && UserHandle.getUserId(uid) == 0) {
                    OppoOperatorManagerService.sUninstalledPackagesByUser.add(packageName);
                    Slog.i(OppoOperatorManagerService.TAG, "onPackageRemoved for interested package " + packageName);
                    OppoOperatorManagerService.this.persistenceAsync();
                }
            }
        });
    }

    private void registerCotaMountFinishReceiver() {
        getContext().registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.operator.OppoOperatorManagerService.AnonymousClass3 */

            public void onReceive(Context context, Intent intent) {
                Slog.i(OppoOperatorManagerService.TAG, "onReceive intent" + intent);
                OppoOperatorManagerService.this.doCotaMountFinish();
            }
        }, UserHandle.ALL, new IntentFilter(ACTION_COTA_MOUNT_COMPLETED), null, this.mHandler);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doCotaMountFinish() {
        sConfigXmlParsed = false;
        parseConfigMap();
        killBackgroundGmsProcesses();
        grantCustomizedRuntimePermissions();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableSimTriggeredApps(String operator, String country) {
        ArrayList<Element> pkgList;
        if (sNeedEnableSimTriggeredApps && !TextUtils.isEmpty(operator) && !TextUtils.isEmpty(country) && (pkgList = mPackages.get(operator)) != null && !pkgList.isEmpty()) {
            Package pkg = null;
            for (int i = 0; i < pkgList.size(); i++) {
                if (pkgList.get(i) instanceof Package) {
                    pkg = (Package) pkgList.get(i);
                }
                if (pkg != null && TemperatureProvider.SWITCH_OFF.equals(pkg.getReboot()) && TemperatureProvider.SWITCH_OFF.equals(pkg.getRemovable()) && country.equals(pkg.getCountry()) && getContext() != null && getContext().getPackageManager().isPackageAvailable(pkg.getPkgName())) {
                    Slog.i(TAG, "enableSimTriggeredApps  pkgName " + pkg.getPkgName() + " simOperator " + operator + " simCountry " + country);
                    StringBuilder sb = new StringBuilder();
                    sb.append("setApplicationEnabledSetting enable ");
                    sb.append(pkg.getPkgName());
                    Slog.i(TAG, sb.toString());
                    getContext().getPackageManager().setApplicationEnabledSetting(pkg.getPkgName(), 1, 0);
                }
            }
        }
    }

    private void updateSimTriggeredApps() {
        if (sNeedEnableSimTriggeredApps && OppoOperatorManager.SERVICE_ENABLED) {
            String subOperators = SUB_OPERATORS.get(sOperator + sRegion);
            if (!TextUtils.isEmpty(subOperators)) {
                String[] operatorArray = new String[1];
                if (subOperators.contains(";")) {
                    operatorArray = subOperators.split(";");
                } else {
                    operatorArray[0] = subOperators;
                }
                String simOperator = SystemProperties.get(SIM_OPERATOR_PROP, "null");
                String simCountry = SystemProperties.get(SIM_COUNTRY_PROP, "null");
                for (String operator : operatorArray) {
                    ArrayList<Element> pkgList = mPackages.get(operator);
                    if (pkgList != null && !pkgList.isEmpty()) {
                        Package pkg = null;
                        for (int i = 0; i < pkgList.size(); i++) {
                            if (pkgList.get(i) instanceof Package) {
                                pkg = (Package) pkgList.get(i);
                            }
                            if (pkg != null && TemperatureProvider.SWITCH_OFF.equals(pkg.getReboot()) && TemperatureProvider.SWITCH_OFF.equals(pkg.getRemovable()) && getContext() != null && getContext().getPackageManager().isPackageAvailable(pkg.getPkgName())) {
                                Slog.i(TAG, "updateSimTriggeredApps  pkgName " + pkg.getPkgName() + " subOperator " + operator + " simOperator " + simOperator + " simCountry " + simCountry);
                                if (!simOperator.equals(operator) || !simCountry.equals(pkg.getCountry())) {
                                    Slog.i(TAG, "setApplicationEnabledSetting disable " + pkg.getPkgName());
                                    getContext().getPackageManager().setApplicationEnabledSetting(pkg.getPkgName(), 2, 0);
                                } else {
                                    Slog.i(TAG, "setApplicationEnabledSetting enable " + pkg.getPkgName());
                                    getContext().getPackageManager().setApplicationEnabledSetting(pkg.getPkgName(), 1, 0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void persistenceAsync() {
        H h = this.mHandler;
        if (h == null) {
            Slog.e(TAG, "mHandler is null, exit persistenceAsync");
            return;
        }
        if (h.hasMessages(2021)) {
            this.mHandler.removeMessages(2021);
        }
        this.mHandler.sendEmptyMessage(2021);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableGmailOverlay(String simOperator, String simCountry) {
        String localSimOperator = simOperator;
        String localSimCountry = simCountry;
        String operator = "";
        if (TextUtils.isEmpty(localSimOperator)) {
            localSimOperator = SystemProperties.get(SIM_OPERATOR_PROP, "null");
        }
        if (TextUtils.isEmpty(localSimCountry)) {
            localSimCountry = SystemProperties.get(SIM_COUNTRY_PROP, "null");
        }
        if ("VODAFONE_EEA".equals(sOperator) || "VODAFONE_NONEEA".equals(sOperator)) {
            operator = OPERATOR_VODAFONE;
        }
        String numeric = ((TelephonyManager) getContext().getSystemService("phone")).getSimOperatorNumeric();
        if (TextUtils.isEmpty(operator) || !operator.equals(localSimOperator)) {
            Slog.i(TAG, "skip enableGmailOverlay operator not match , operator " + operator + " simOperator " + localSimOperator + " simCountry " + localSimCountry + " numeric " + numeric);
            return;
        }
        IOverlayManager overlayManager = IOverlayManager.Stub.asInterface(ServiceManager.getService("overlay"));
        try {
            Slog.i(TAG, "enableGmailOverlay for operator " + operator + " simOperator " + localSimOperator + " simCountry " + localSimCountry + " numeric " + numeric);
            overlayManager.setEnabled(GMAIL_OVERLAY_PACKAGE, true, -2);
        } catch (Exception e) {
            Slog.e(TAG, "enableGmailOverlay exception ", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void killBackgroundGmsProcesses() {
        int importance;
        ActivityManager mAm = (ActivityManager) getContext().getSystemService(ActivityManager.class);
        for (String pkg : PACKAGES_TO_KILL) {
            if (mAm != null && (importance = mAm.getPackageImportance(pkg)) < 1000 && importance >= 400) {
                Slog.i(TAG, "kill " + pkg + " importance " + importance);
                mAm.killBackgroundProcesses(pkg);
            }
        }
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        super.onBootPhase(phase);
    }

    private final class LocalService extends OppoOperatorManagerInternal {
        private LocalService() {
        }

        public void testInternal() {
            Slog.i(OppoOperatorManagerService.TAG, "OppoOperatorManagerInternal testInternal");
        }

        public HashSet<String> getGrantedRuntimePermissionsPostInstall(String pkg) {
            new HashSet();
            return OppoOperatorManagerService.this.getGrantedRuntimePermissions(pkg, OppoOperatorManagerService.GRANT_PERMISSION_POST_INSTALL);
        }

        public HashSet<String> getGrantedRuntimePermissionsPreload(String pkg, boolean systemFixed) {
            new HashSet();
            return OppoOperatorManagerService.this.getGrantedRuntimePermissions(pkg, OppoOperatorManagerService.GRANT_PERMISSION_PRELOAD);
        }

        public boolean hasFeatureDynamiclyEnabeld(String name) {
            return OppoOperatorManagerService.this.hasFeatureDynamiclyEnabeld(name);
        }
    }

    public boolean hasFeatureDynamiclyEnabeld(String name) {
        HashMap<String, HashMap<String, Set<String>>> hashMap;
        if (!this.mEnableDynamicFeature || TextUtils.isEmpty(sSimCountry) || (hashMap = this.mFeaturesMap) == null || hashMap.get(sSimOperator) == null || this.mFeaturesMap.get(sSimOperator).get(sSimCountry) == null || !this.mFeaturesMap.get(sSimOperator).get(sSimCountry).contains(name)) {
            return false;
        }
        if (!sDebugOn) {
            return true;
        }
        Slog.d(TAG, "hasFeatureDynamiclyEnabeld " + name + " calling pid " + Binder.getCallingPid() + " sSimCountry " + sSimCountry + " sSimOperator " + sSimOperator);
        return true;
    }

    public HashSet<String> getGrantedRuntimePermissions(String pkg, String tag) {
        Permission perm;
        PermissionGroup pg;
        HashSet<String> results = new HashSet<>();
        String simOperator = SystemProperties.get(SIM_OPERATOR_PROP, "null");
        String simCountry = SystemProperties.get(SIM_COUNTRY_PROP, "null");
        ArrayList<Element> list = mPermissionGroups.get(simOperator);
        if (list != null && !list.isEmpty()) {
            Iterator<Element> it = list.iterator();
            while (it.hasNext()) {
                Element element = it.next();
                if ((element instanceof PermissionGroup) && (pg = (PermissionGroup) element) != null) {
                    if ((simCountry.equals(pg.getCountry()) || "COMMON".equals(pg.getCountry())) && tag.equals(pg.getGrant()) && pg.getPackages() != null && pg.getPackages().contains(pkg)) {
                        Slog.i(TAG, "getGrantedRuntimePermissions permission group  " + pg.getGrpName() + " pkg " + pkg);
                        results.addAll(pg.getPermissions());
                    }
                }
            }
            list.clear();
        }
        ArrayList<Element> list2 = mPermssions.get(simOperator);
        if (list2 != null && !list2.isEmpty()) {
            Iterator<Element> it2 = list2.iterator();
            while (it2.hasNext()) {
                Element element2 = it2.next();
                if ((element2 instanceof Permission) && (perm = (Permission) element2) != null) {
                    if ((simCountry.equals(perm.getCountry()) || "COMMON".equals(perm.getCountry())) && tag.equals(perm.getGrant()) && perm.getPackages() != null && perm.getPackages().contains(pkg)) {
                        Slog.i(TAG, "getGrantedRuntimePermissions permission " + perm.getPermName() + " pkg " + pkg);
                        results.add(perm.getPermission());
                    }
                }
            }
            list2.clear();
        }
        return results;
    }

    private class BinderService extends IOppoOperatorManager.Stub {
        private BinderService() {
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
            if (DumpUtils.checkDumpAndUsageStatsPermission(OppoOperatorManagerService.this.getContext(), OppoOperatorManagerService.TAG, fout)) {
                String simCountry = SystemProperties.get(OppoOperatorManagerService.SIM_COUNTRY_PROP, "null");
                String simOperator = SystemProperties.get(OppoOperatorManagerService.SIM_OPERATOR_PROP, "null");
                if (OppoOperatorManagerService.sHasCotaConfigMap) {
                    simCountry = OppoOperatorManagerService.COTA_TAG;
                    simOperator = OppoOperatorManagerService.COTA_TAG;
                }
                if (0 < args.length) {
                    String cmd = args[0];
                    int opti = 0 + 1;
                    if ("-d".equals(cmd)) {
                        boolean unused = OppoOperatorManagerService.sDebugOn = !OppoOperatorManagerService.sDebugOn;
                        fout.println("change sDebugOn to " + OppoOperatorManagerService.sDebugOn);
                    } else if ("-b".equals(cmd)) {
                        OppoOperatorManagerService.this.dumpElements(fout, simOperator, simCountry, OppoOperatorManagerService.mBookMarks, "bookmarks");
                    } else if ("-p".equals(cmd)) {
                        OppoOperatorManagerService.this.dumpElements(fout, simOperator, simCountry, OppoOperatorManagerService.mHomePages, "homepages");
                    } else if ("-e".equals(cmd)) {
                        OppoOperatorManagerService.this.dumpElements(fout, simOperator, simCountry, OppoOperatorManagerService.mEmails, "emails");
                        OppoOperatorManagerService.this.dumpElements(fout, simOperator, simCountry, OppoOperatorManagerService.mAccounts, "accounts");
                        OppoOperatorManagerService.this.dumpElements(fout, simOperator, simCountry, OppoOperatorManagerService.mSignatures, "signatures");
                    } else if ("-f".equals(cmd)) {
                        OppoOperatorManagerService oppoOperatorManagerService = OppoOperatorManagerService.this;
                        oppoOperatorManagerService.dumpFeatures(fout, simOperator, simCountry, oppoOperatorManagerService.mFeaturesMap, "features");
                    } else if ("-perm".equals(cmd)) {
                        OppoOperatorManagerService.this.dumpElements(fout, simOperator, simCountry, OppoOperatorManagerService.mPermissionGroups, "permission groups");
                        OppoOperatorManagerService.this.dumpElements(fout, simOperator, simCountry, OppoOperatorManagerService.mPermssions, "permissions");
                    } else if ("-pkg".equals(cmd)) {
                        OppoOperatorManagerService.this.dumpElements(fout, simOperator, simCountry, OppoOperatorManagerService.mPackages, "packages");
                    } else if ("-h".equals(cmd) || "-help".equals(cmd)) {
                        fout.println("OppoOperatorManager dump options:");
                        fout.println("  [-d] [-b] [-p] [-e] [-f] [-perm] ...");
                        fout.println("  -d: swich on/off log.");
                        fout.println("  -b: dump bookmarks for current sim country");
                        fout.println("  -p: dump homepage for current sim country");
                        fout.println("  -e: dump email configurations for current sim country");
                        fout.println("  -f: dump features for current operator");
                        fout.println("  -perm: dump permissions for current sim country");
                        fout.println("  -pkg: dump packages for current sim country");
                        fout.println("  dump all above by default");
                    } else {
                        fout.println("invalid params return");
                    }
                } else {
                    OppoOperatorManagerService.this.dumpElements(fout, simOperator, simCountry, OppoOperatorManagerService.mBookMarks, "bookmarks");
                    OppoOperatorManagerService.this.dumpElements(fout, simOperator, simCountry, OppoOperatorManagerService.mHomePages, "homepages");
                    OppoOperatorManagerService.this.dumpElements(fout, simOperator, simCountry, OppoOperatorManagerService.mEmails, "emails");
                    OppoOperatorManagerService.this.dumpElements(fout, simOperator, simCountry, OppoOperatorManagerService.mAccounts, "accounts");
                    OppoOperatorManagerService.this.dumpElements(fout, simOperator, simCountry, OppoOperatorManagerService.mSignatures, "signatures");
                    OppoOperatorManagerService.this.dumpElements(fout, simOperator, simCountry, OppoOperatorManagerService.mPermissionGroups, "permission groups");
                    OppoOperatorManagerService.this.dumpElements(fout, simOperator, simCountry, OppoOperatorManagerService.mPermssions, "permissions");
                    OppoOperatorManagerService oppoOperatorManagerService2 = OppoOperatorManagerService.this;
                    oppoOperatorManagerService2.dumpFeatures(fout, simOperator, simCountry, oppoOperatorManagerService2.mFeaturesMap, "features");
                    OppoOperatorManagerService.this.dumpElements(fout, simOperator, simCountry, OppoOperatorManagerService.mPackages, "packages");
                    fout.println("");
                    fout.println("sOperator: " + OppoOperatorManagerService.sOperator);
                    fout.println("sRegion: " + OppoOperatorManagerService.sRegion);
                    fout.println("sSimOperator: " + OppoOperatorManagerService.sSimOperator);
                    fout.println("sSimCountry: " + OppoOperatorManagerService.sSimCountry);
                    fout.println("ro.oppo.operator: " + SystemProperties.get("ro.oppo.operator"));
                    fout.println("ro.oppo.regionmark: " + SystemProperties.get("ro.oppo.regionmark"));
                    fout.println("persist.sys.oppo_opta: " + SystemProperties.get(OppoOperatorManagerService.SIM_OPERATOR_PROP));
                    fout.println("persist.sys.oppo_optb: " + SystemProperties.get(OppoOperatorManagerService.SIM_COUNTRY_PROP));
                    fout.println("persist.sys.oppo_optcopied: " + SystemProperties.get("persist.sys.oppo_optcopied"));
                    fout.println("sDebugOn: " + OppoOperatorManagerService.sDebugOn);
                    fout.println("mEnableDynamicFeature: " + OppoOperatorManagerService.this.mEnableDynamicFeature);
                    fout.println("sConfigXmlParsed: " + OppoOperatorManagerService.sConfigXmlParsed);
                    if (OppoOperatorManagerService.sUninstalledPackagesByUser.isEmpty()) {
                        fout.println("sUninstalledPackagesByUser: 0");
                    } else {
                        fout.println("sUninstalledPackagesByUser: " + OppoOperatorManagerService.sUninstalledPackagesByUser.size());
                        Iterator it = OppoOperatorManagerService.sUninstalledPackagesByUser.iterator();
                        while (it.hasNext()) {
                            fout.println((String) it.next());
                        }
                    }
                    fout.println("sHasCotaConfigMap: " + OppoOperatorManagerService.sHasCotaConfigMap);
                    fout.println("sNeedEnableSimTriggeredApps: " + OppoOperatorManagerService.sNeedEnableSimTriggeredApps);
                    fout.println("sNeedCheckSpn: " + OppoOperatorManagerService.sNeedCheckSpn);
                    fout.println("sSpnCandidates: " + OppoOperatorManagerService.sSpnCandidates);
                    fout.println("persist.sys.oppo_last_spn: " + SystemProperties.get("persist.sys.oppo_last_spn", ""));
                }
            }
        }

        public void testAidl() {
            Slog.i(OppoOperatorManagerService.TAG, "IOppoOperatorManager testAidl");
        }

        public Map getConfigMap(Bundle bundle) throws RemoteException {
            ArrayList list;
            Signature global_signature;
            Account account;
            Email email;
            HomePage hp;
            ArrayList list2;
            ArrayList list3;
            String config = null;
            Map result = new HashMap();
            String simOperator = SystemProperties.get(OppoOperatorManagerService.SIM_OPERATOR_PROP, "null");
            String simCountry = SystemProperties.get(OppoOperatorManagerService.SIM_COUNTRY_PROP, "null");
            if (OppoOperatorManagerService.sHasCotaConfigMap) {
                simOperator = OppoOperatorManagerService.COTA_TAG;
                simCountry = OppoOperatorManagerService.COTA_TAG;
            }
            if (bundle != null) {
                config = bundle.getString("config");
            }
            Slog.i(OppoOperatorManagerService.TAG, "getConfigMap bundle sOperator " + OppoOperatorManagerService.sOperator + " sRegion " + OppoOperatorManagerService.sRegion + " simOperator " + simOperator + " simCountry " + simCountry + " config " + config + " sNeedCheckSpn " + OppoOperatorManagerService.sNeedCheckSpn);
            String targetSpn = "";
            if (OppoOperatorManagerService.sNeedCheckSpn) {
                targetSpn = OppoOperatorManagerService.this.getCurrentSpnLowerCase();
                Slog.i(OppoOperatorManagerService.TAG, "getConfigMap targetSpn " + targetSpn + " sSpnCandidates " + OppoOperatorManagerService.sSpnCandidates);
                String lastSpn = SystemProperties.get("persist.sys.oppo_last_spn", "");
                if (TextUtils.isEmpty(targetSpn) || !OppoOperatorManagerService.sSpnCandidates.contains(targetSpn)) {
                    targetSpn = lastSpn;
                } else if (!targetSpn.equals(lastSpn)) {
                    SystemProperties.set("persist.sys.oppo_last_spn", targetSpn);
                }
            }
            if ("bookmarks".equals(config)) {
                ArrayList list4 = (ArrayList) OppoOperatorManagerService.mBookMarks.get(simOperator);
                if (list4 == null || list4.isEmpty()) {
                    list2 = list4;
                } else {
                    String md5 = "";
                    if (OppoOperatorManagerService.sDebugOn) {
                        Slog.i(OppoOperatorManagerService.TAG, "getConfigMap bookmarks list size " + list4.size());
                    }
                    int index = 0;
                    int i = 0;
                    while (i < list4.size()) {
                        if (OppoOperatorManagerService.sDebugOn) {
                            Slog.e(OppoOperatorManagerService.TAG, "getConfigMap list i " + i + " list i " + list4.get(i));
                        }
                        if (list4.get(i) instanceof BookMark) {
                            BookMark bm = (BookMark) list4.get(i);
                            if (OppoOperatorManagerService.sDebugOn) {
                                Slog.i(OppoOperatorManagerService.TAG, "getConfigMap bookmarks bm country " + bm.getCountry() + " label " + bm.getLabel());
                            }
                            if (bm == null || !simCountry.equals(bm.getCountry())) {
                                list3 = list4;
                            } else if (TextUtils.isEmpty(bm.getSpn()) || bm.getSpn().equals(targetSpn)) {
                                HashMap<String, String> value = new HashMap<>();
                                if (OppoOperatorManagerService.sDebugOn) {
                                    StringBuilder sb = new StringBuilder();
                                    list3 = list4;
                                    sb.append("getConfigMap bookmarks label ");
                                    sb.append(bm.getLabel());
                                    sb.append(" url ");
                                    sb.append(bm.getUrl());
                                    sb.append(" folder ");
                                    sb.append(bm.getFolder());
                                    Slog.i(OppoOperatorManagerService.TAG, sb.toString());
                                } else {
                                    list3 = list4;
                                }
                                value.put("label", bm.getLabel());
                                value.put("url", bm.getUrl());
                                value.put("folder", bm.getFolder());
                                result.put("bookmark_" + index, value);
                                index++;
                                md5 = ((md5 + bm.getLabel()) + bm.getUrl()) + bm.getFolder();
                            } else {
                                list3 = list4;
                            }
                        } else {
                            list3 = list4;
                        }
                        i++;
                        list4 = list3;
                    }
                    list2 = list4;
                    if (!TextUtils.isEmpty(md5)) {
                        long startTime = System.currentTimeMillis();
                        result.put("digest", OppoOperatorManagerService.generateDigest(md5));
                        Slog.i(OppoOperatorManagerService.TAG, "getConfigMap bookmarks calculate digest took " + (System.currentTimeMillis() - startTime) + "ms");
                    }
                }
            } else if ("homepage".equals(config)) {
                ArrayList list5 = (ArrayList) OppoOperatorManagerService.mHomePages.get(simOperator);
                if (list5 != null && !list5.isEmpty()) {
                    int i2 = 0;
                    while (true) {
                        if (i2 >= list5.size()) {
                            break;
                        } else if (!(list5.get(i2) instanceof HomePage) || (hp = (HomePage) list5.get(i2)) == null || !simCountry.equals(hp.getCountry()) || (!TextUtils.isEmpty(hp.getSpn()) && !hp.getSpn().equals(targetSpn))) {
                            i2++;
                        }
                    }
                    if (OppoOperatorManagerService.sDebugOn) {
                        Slog.i(OppoOperatorManagerService.TAG, "getConfigMap homepage label " + hp.getLabel() + " url " + hp.getUrl());
                    }
                    result.put("label", hp.getLabel());
                    result.put("url", hp.getUrl());
                }
            } else if ("emails".equals(config)) {
                ArrayList list6 = (ArrayList) OppoOperatorManagerService.mEmails.get(simOperator);
                if (list6 != null && !list6.isEmpty()) {
                    int index2 = 0;
                    for (int i3 = 0; i3 < list6.size(); i3++) {
                        if ((list6.get(i3) instanceof Email) && (email = (Email) list6.get(i3)) != null && simCountry.equals(email.getCountry())) {
                            HashMap<String, String> value2 = new HashMap<>();
                            value2.put("id", email.getId());
                            value2.put("label", email.getLabel());
                            value2.put("domain", email.getDomain());
                            value2.put("signature", email.getSignature());
                            value2.put("incomingUriTemplate", email.getIncomingUriTemplate());
                            value2.put("incomingUsernameTemplate", email.getIncomingUsernameTemplate());
                            value2.put("outgoingUriTemplate", email.getOutgoingUriTemplate());
                            value2.put("outgoingUsernameTemplate", email.getOutgoingUsernameTemplate());
                            result.put("email_" + index2, value2);
                            index2++;
                        }
                    }
                }
            } else if ("accounts".equals(config)) {
                ArrayList list7 = (ArrayList) OppoOperatorManagerService.mAccounts.get(simOperator);
                if (list7 != null && !list7.isEmpty()) {
                    int index3 = 0;
                    for (int i4 = 0; i4 < list7.size(); i4++) {
                        if ((list7.get(i4) instanceof Account) && (account = (Account) list7.get(i4)) != null && simCountry.equals(account.getCountry())) {
                            HashMap<String, String> value3 = new HashMap<>();
                            value3.put("label", account.getLabel());
                            value3.put("logo", account.getLogo());
                            result.put("account_" + index3, value3);
                            index3++;
                        }
                    }
                }
            } else if ("global_signature".equals(config) && (list = (ArrayList) OppoOperatorManagerService.mSignatures.get(simOperator)) != null && !list.isEmpty()) {
                int i5 = 0;
                while (true) {
                    if (i5 >= list.size()) {
                        break;
                    } else if (!(list.get(i5) instanceof Signature) || (global_signature = (Signature) list.get(i5)) == null || !simCountry.equals(global_signature.getCountry())) {
                        i5++;
                    } else {
                        if (OppoOperatorManagerService.sDebugOn) {
                            Slog.i(OppoOperatorManagerService.TAG, "getConfigMap signature " + global_signature.getGlobal_signature());
                        }
                        result.put("global_signature", global_signature.getGlobal_signature());
                    }
                }
            }
            if (OppoOperatorManagerService.sDebugOn && !result.isEmpty()) {
                Slog.i(OppoOperatorManagerService.TAG, "getConfigMap config " + config + " map.size" + result.size());
                for (Object str : result.keySet()) {
                    if ((str instanceof String) && (result.get((String) str) instanceof HashMap)) {
                        Slog.i(OppoOperatorManagerService.TAG, "getConfigMap config " + config + " KEY " + str + " VALUE LABEL " + ((HashMap) result.get((String) str)).get("label"));
                    } else if ((str instanceof String) && (result.get((String) str) instanceof String)) {
                        Slog.i(OppoOperatorManagerService.TAG, "getConfigMap config " + config + " KEY " + str + " VALUE " + ((String) result.get((String) str)));
                    }
                }
            }
            return result;
        }

        public void grantCustomizedRuntimePermissions() throws RemoteException {
            OppoOperatorManagerService.this.grantCustomizedRuntimePermissions();
        }

        public boolean isDynamicFeatureEnabled() throws RemoteException {
            return OppoOperatorManagerService.this.mEnableDynamicFeature;
        }

        public void notifySmartCustomizationStart() throws RemoteException {
            Context context = OppoOperatorManagerService.this.getContext();
            if (context == null || context.checkCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE") != 0) {
                Slog.w(OppoOperatorManagerService.TAG, "notifySmartCustomizationStart failed " + context);
                return;
            }
            OppoOperatorManagerService.this.mEnableDynamicFeature = true;
            String unused = OppoOperatorManagerService.sSimOperator = SystemProperties.get(OppoOperatorManagerService.SIM_OPERATOR_PROP);
            String unused2 = OppoOperatorManagerService.sSimCountry = SystemProperties.get(OppoOperatorManagerService.SIM_COUNTRY_PROP);
            Slog.w(OppoOperatorManagerService.TAG, "notifySmartCustomizationStart successful mEnableDynamicFeature " + OppoOperatorManagerService.this.mEnableDynamicFeature + " sSimOperator " + OppoOperatorManagerService.sSimOperator + " sSimCountry " + OppoOperatorManagerService.sSimCountry);
        }

        public boolean hasFeatureDynamiclyEnabeld(String name) throws RemoteException {
            return OppoOperatorManagerService.this.hasFeatureDynamiclyEnabeld(name);
        }

        public boolean isInSimTriggeredSystemBlackList(String pkgName) throws RemoteException {
            OppoOperatorManagerService oppoOperatorManagerService = OppoOperatorManagerService.this;
            return OppoOperatorManagerService.isInSimTriggeredSystemBlackList(pkgName);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getCurrentSpnLowerCase() {
        String spn;
        TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService("phone");
        String spn2 = null;
        if (telephonyManager != null) {
            spn2 = telephonyManager.getSimOperatorNameForPhone(0);
            Slog.i(TAG, "getSimOperatorNameForPhone " + spn2);
        }
        if (!TextUtils.isEmpty(spn2)) {
            spn = spn2.toLowerCase();
        } else {
            spn = "";
        }
        Slog.i(TAG, "getCurrentSpnLowerCase " + spn);
        return spn;
    }

    public void grantCustomizedRuntimePermissions() {
        String simCountry;
        String simOperator;
        int i;
        String str;
        int userId;
        String str2;
        int i2;
        int userId2;
        PackageParser.Package pkg;
        String str3;
        Iterator<String> it;
        String str4;
        ArrayList<Element> list;
        Iterator<String> it2;
        Element element;
        OppoOperatorManagerService oppoOperatorManagerService = this;
        if (getPermissionManagerServiceInternal() != null) {
            oppoOperatorManagerService.mDefaultPermissionPolicy = getPermissionManagerServiceInternal().getDefaultPermissionGrantPolicy();
        }
        Slog.i(TAG, "grantCustomizedPermissions " + oppoOperatorManagerService.mDefaultPermissionPolicy);
        if (oppoOperatorManagerService.mDefaultPermissionPolicy != null) {
            try {
                Class<?> policy = Class.forName("com.android.server.pm.permission.DefaultPermissionGrantPolicy");
                Method grantRuntimePermissions = policy.getDeclaredMethod("grantRuntimePermissions", PackageInfo.class, Set.class, Boolean.TYPE, Integer.TYPE);
                grantRuntimePermissions.setAccessible(true);
                String simOperator2 = SystemProperties.get(SIM_OPERATOR_PROP, "null");
                String simCountry2 = SystemProperties.get(SIM_COUNTRY_PROP, "null");
                if (sHasCotaConfigMap) {
                    simCountry = COTA_TAG;
                    simOperator = COTA_TAG;
                } else {
                    simCountry = simCountry2;
                    simOperator = simOperator2;
                }
                new HashSet();
                HashMap<String, HashSet<String>> permissionMapSystemFixed = new HashMap<>();
                HashMap<String, HashSet<String>> permissionMapNoneSystemFixed = new HashMap<>();
                ArrayList<Element> list2 = mPermissionGroups.get(simOperator);
                String str5 = LEVEL_DANGEROUS;
                if (list2 == null || list2.isEmpty()) {
                    Slog.e(TAG, "mPermissionGroups is null for " + simOperator + " size " + mPermissionGroups.size());
                } else {
                    Iterator<Element> it3 = list2.iterator();
                    while (it3.hasNext()) {
                        Element element2 = it3.next();
                        if (element2 instanceof PermissionGroup) {
                            PermissionGroup pg = (PermissionGroup) element2;
                            if (pg != null) {
                                list = list2;
                                if (simCountry.equals(pg.getCountry()) || "COMMON".equals(pg.getCountry())) {
                                    if (GRANT_PERMISSION_PRELOAD.equals(pg.getGrant())) {
                                        if (str5.equals(pg.getLevel())) {
                                            if (pg.isSystemFixed()) {
                                                Iterator<String> it4 = pg.getPackages().iterator();
                                                while (it4.hasNext()) {
                                                    String pkg2 = it4.next();
                                                    if (!permissionMapSystemFixed.containsKey(pkg2)) {
                                                        element = element2;
                                                        permissionMapSystemFixed.put(pkg2, new HashSet<>());
                                                    } else {
                                                        element = element2;
                                                    }
                                                    permissionMapSystemFixed.get(pkg2).addAll(pg.getPermissions());
                                                    it4 = it4;
                                                    element2 = element;
                                                }
                                            } else {
                                                Iterator<String> it5 = pg.getPackages().iterator();
                                                while (it5.hasNext()) {
                                                    String pkg3 = it5.next();
                                                    if (!permissionMapNoneSystemFixed.containsKey(pkg3)) {
                                                        it2 = it5;
                                                        permissionMapNoneSystemFixed.put(pkg3, new HashSet<>());
                                                    } else {
                                                        it2 = it5;
                                                    }
                                                    permissionMapNoneSystemFixed.get(pkg3).addAll(pg.getPermissions());
                                                    it5 = it2;
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                list = list2;
                            }
                        } else {
                            list = list2;
                        }
                        policy = policy;
                        list2 = list;
                    }
                }
                ArrayList<Element> list3 = mPermssions.get(simOperator);
                if (list3 == null || list3.isEmpty()) {
                    Slog.e(TAG, "mPermssions is null for " + simOperator + " size " + mPermssions.size());
                } else {
                    Iterator<Element> it6 = list3.iterator();
                    while (it6.hasNext()) {
                        Element element3 = it6.next();
                        if (element3 instanceof Permission) {
                            Permission perm = (Permission) element3;
                            if (perm == null) {
                                str3 = str5;
                            } else if (!simCountry.equals(perm.getCountry()) && !"COMMON".equals(perm.getCountry())) {
                                str3 = str5;
                            } else if (!GRANT_PERMISSION_PRELOAD.equals(perm.getGrant())) {
                                str3 = str5;
                            } else if (!str5.equals(perm.getLevel())) {
                                str3 = str5;
                            } else if (perm.isSystemFixed()) {
                                for (Iterator<String> it7 = perm.getPackages().iterator(); it7.hasNext(); it7 = it7) {
                                    String pkg4 = it7.next();
                                    if (!permissionMapSystemFixed.containsKey(pkg4)) {
                                        str4 = str5;
                                        permissionMapSystemFixed.put(pkg4, new HashSet<>());
                                    } else {
                                        str4 = str5;
                                    }
                                    permissionMapSystemFixed.get(pkg4).add(perm.getPermission());
                                    str5 = str4;
                                }
                                str3 = str5;
                            } else {
                                str3 = str5;
                                Iterator<String> it8 = perm.getPackages().iterator();
                                while (it8.hasNext()) {
                                    String pkg5 = it8.next();
                                    if (!permissionMapNoneSystemFixed.containsKey(pkg5)) {
                                        it = it8;
                                        permissionMapNoneSystemFixed.put(pkg5, new HashSet<>());
                                    } else {
                                        it = it8;
                                    }
                                    permissionMapNoneSystemFixed.get(pkg5).add(perm.getPermission());
                                    it8 = it;
                                }
                            }
                        } else {
                            str3 = str5;
                        }
                        list3 = list3;
                        str5 = str3;
                    }
                }
                if (sDebugOn) {
                    if (!permissionMapNoneSystemFixed.isEmpty()) {
                        for (String pkg6 : permissionMapNoneSystemFixed.keySet()) {
                            Slog.d(TAG, "grantCustomizedPermissions " + pkg6 + "  none system fixed permissions " + permissionMapNoneSystemFixed.get(pkg6));
                        }
                    }
                    if (!permissionMapSystemFixed.isEmpty()) {
                        for (String pkg7 : permissionMapSystemFixed.keySet()) {
                            Slog.d(TAG, "grantCustomizedPermissions  " + pkg7 + " system fixed permissions " + permissionMapSystemFixed.get(pkg7));
                        }
                    }
                }
                int[] userIds = UserManagerService.getInstance().getUserIds();
                if (userIds == null || userIds.length == 0) {
                    i = 0;
                    userIds = new int[]{0};
                } else {
                    i = 0;
                }
                PackageInfo pkgInfo = null;
                int length = userIds.length;
                int i3 = i;
                while (i3 < length) {
                    int userId3 = userIds[i3];
                    Iterator<String> it9 = permissionMapNoneSystemFixed.keySet().iterator();
                    while (true) {
                        str = "skip pkg ";
                        if (!it9.hasNext()) {
                            break;
                        }
                        String pkgName = it9.next();
                        PackageParser.Package pkg8 = getPackageManagerInternal().getPackage(pkgName);
                        if (pkg8 == null || pkg8.applicationInfo == null || !doesPackageSupportRuntimePermissions(pkg8) || pkg8.requestedPermissions.isEmpty() || permissionMapNoneSystemFixed.get(pkgName) == null || permissionMapNoneSystemFixed.get(pkgName).isEmpty()) {
                            Slog.i(TAG, str + pkgName + " permissionMapNoneSystemFixed ");
                            i3 = i3;
                            userIds = userIds;
                            pkgInfo = pkgInfo;
                            userId3 = userId3;
                            length = length;
                        } else {
                            PackageInfo pkgInfo2 = getPackageManagerInternal().getPackageInfo(pkg8.packageName, 12288, 1000, userId3);
                            if (pkgInfo2 != null) {
                                userId2 = userId3;
                                pkg = pkg8;
                                i2 = length;
                                grantRuntimePermissions(grantRuntimePermissions, oppoOperatorManagerService.mDefaultPermissionPolicy, pkgInfo2, (HashSet) permissionMapNoneSystemFixed.get(pkgName), false, userId2);
                            } else {
                                userId2 = userId3;
                                pkg = pkg8;
                                i2 = length;
                            }
                            pkgInfo = pkgInfo2;
                            i3 = i3;
                            userIds = userIds;
                            userId3 = userId2;
                            length = i2;
                        }
                    }
                    int userId4 = userId3;
                    Iterator<String> it10 = permissionMapSystemFixed.keySet().iterator();
                    while (it10.hasNext()) {
                        String pkgName2 = it10.next();
                        PackageParser.Package pkg9 = getPackageManagerInternal().getPackage(pkgName2);
                        if (pkg9 == null || pkg9.applicationInfo == null || !doesPackageSupportRuntimePermissions(pkg9) || pkg9.requestedPermissions.isEmpty() || permissionMapSystemFixed.get(pkgName2) == null || permissionMapSystemFixed.get(pkgName2).isEmpty()) {
                            Slog.i(TAG, str + pkgName2 + " permissionMapSystemFixed ");
                            oppoOperatorManagerService = this;
                            str = str;
                            it10 = it10;
                            userId4 = userId4;
                        } else {
                            PackageInfo pkgInfo3 = getPackageManagerInternal().getPackageInfo(pkg9.packageName, 12288, 1000, userId4);
                            if (pkgInfo3 != null) {
                                str2 = str;
                                userId = userId4;
                                grantRuntimePermissions(grantRuntimePermissions, oppoOperatorManagerService.mDefaultPermissionPolicy, pkgInfo3, permissionMapSystemFixed.get(pkgName2), true, userId);
                            } else {
                                str2 = str;
                                userId = userId4;
                            }
                            oppoOperatorManagerService = this;
                            str = str2;
                            it10 = it10;
                            pkgInfo = pkgInfo3;
                            userId4 = userId;
                        }
                    }
                    i3++;
                    oppoOperatorManagerService = this;
                    userIds = userIds;
                    length = length;
                }
            } catch (Exception e) {
                Slog.e(TAG, "grantCustomizedRuntimePermissions invoke failure ", e);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpFeatures(PrintWriter fout, String simOperator, String simCountry, HashMap<String, HashMap<String, Set<String>>> featureMap, String tag) {
        if (fout != null) {
            if (tag != null) {
                fout.println("****************** " + tag.toUpperCase() + " ******************");
            }
            if (featureMap == null || featureMap.isEmpty()) {
                fout.println("None map for " + tag);
                return;
            }
            HashMap<String, Set<String>> map = featureMap.get(simOperator);
            if (!(map == null || map.isEmpty())) {
                for (String region : map.keySet()) {
                    if (region.equals(sSimCountry)) {
                        fout.println("CURRENT DYNAMIC !! region: " + region + " features: " + map.get(region));
                    } else if (region.equals(simCountry)) {
                        fout.println("CURRENT STATIC !! region: " + region + " features: " + map.get(region));
                    } else {
                        fout.println("region: " + region + " features: " + map.get(region));
                    }
                    fout.println("");
                }
            }
        }
    }

    private void grantRuntimePermissions(Method grantRuntimePermissions, DefaultPermissionGrantPolicy mDefaultPermissionPolicy2, PackageInfo pkgInfo, HashSet<String> permissions, boolean systemFixed, int userId) {
        try {
            grantRuntimePermissions.invoke(mDefaultPermissionPolicy2, pkgInfo, permissions, Boolean.valueOf(systemFixed), Integer.valueOf(userId));
            Slog.e(TAG, "grantRuntimePermissions " + pkgInfo + " permissions " + permissions + " systemFixed " + systemFixed + " userId " + userId);
        } catch (Exception e) {
            Slog.e(TAG, "grantRuntimePermissions ", e);
        }
    }

    private static boolean doesPackageSupportRuntimePermissions(PackageParser.Package pkg) {
        return pkg.applicationInfo.targetSdkVersion > 22;
    }

    private PermissionManagerServiceInternal getPermissionManagerServiceInternal() {
        if (this.mPermissionManagerServiceInternal == null) {
            this.mPermissionManagerServiceInternal = (PermissionManagerServiceInternal) LocalServices.getService(PermissionManagerServiceInternal.class);
        }
        return this.mPermissionManagerServiceInternal;
    }

    private PackageManagerInternal getPackageManagerInternal() {
        if (this.mPackageManagerInternal == null) {
            this.mPackageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        }
        return this.mPackageManagerInternal;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpElements(PrintWriter fout, String simOperator, String simCountry, HashMap<String, ArrayList<Element>> map, String tag) {
        if (fout != null) {
            if (tag != null) {
                fout.println("****************** " + tag.toUpperCase() + " ******************");
            }
            if (map == null || map.isEmpty()) {
                fout.println("None map for " + tag);
                return;
            }
            ArrayList<Element> list = map.get(simOperator);
            if (list == null || list.isEmpty()) {
                fout.println("None list for " + tag + " for simOperator " + simOperator + " simCountry " + simCountry);
                return;
            }
            int index = 1;
            Iterator<Element> it = list.iterator();
            while (it.hasNext()) {
                Element element = it.next();
                if (simCountry.equals(element.getCountry()) || "COMMON".equals(element.getCountry())) {
                    fout.println(index + "." + element.getName() + " for simOperator " + simOperator + " simCountry " + simCountry + " : " + element);
                    index++;
                }
            }
        }
    }

    public static String generateDigest(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        try {
            String result = "";
            for (byte b : MessageDigest.getInstance("MD5").digest(string.getBytes())) {
                String temp = Integer.toHexString(b & OppoNfcChipVersion.NONE);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result = result + temp;
            }
            return result.toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            Slog.e(TAG, "generateDigest NoSuchAlgorithmException ", e);
            return "";
        }
    }

    public void systemReady() {
        Slog.i(TAG, "systemReady");
        enableGmailOverlay(null, null);
        grantCustomizedRuntimePermissions();
        bringUpAppsFromStopState();
        updateSimTriggeredApps();
    }

    private void bringUpAppsFromStopState() {
        for (String pkg : PACKAGES_TO_NONSTOP) {
            try {
                Slog.i(TAG, "bringUpAppsFromStopState " + pkg);
                AppGlobals.getPackageManager().setPackageStoppedState(pkg, false, getContext().getUserId());
            } catch (Exception e) {
                Slog.e(TAG, "bringUpAppsFromStopState " + e);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class H extends Handler {
        public static final int MSG_PERSIST = 2021;

        public H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 2021) {
                OppoOperatorManagerService.this.persistence();
            }
        }
    }

    private static void loadUninstalledPackageList() {
        StringBuilder sb;
        File operationFile = new File(PACKAGE_STATE_FILE);
        if (!operationFile.exists()) {
            Slog.i(TAG, "package_state.xml not exist.");
            return;
        }
        try {
            FileInputStream in = new AtomicFile(operationFile).openRead();
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(in, null);
                parsePackageState(parser);
                IoUtils.closeQuietly(in);
                if (1 == 0) {
                    sb = new StringBuilder();
                    sb.append("parse ");
                    sb.append(operationFile.getPath());
                    sb.append(" failed, delete it.");
                    Slog.e(TAG, sb.toString());
                    operationFile.delete();
                }
            } catch (IOException | XmlPullParserException e) {
                Slog.e(TAG, "Failed parsing file: " + operationFile, e);
                IoUtils.closeQuietly(in);
                if (0 == 0) {
                    sb = new StringBuilder();
                }
            } catch (Throwable th) {
                IoUtils.closeQuietly(in);
                if (1 == 0) {
                    Slog.e(TAG, "parse " + operationFile.getPath() + " failed, delete it.");
                    operationFile.delete();
                }
                throw th;
            }
        } catch (FileNotFoundException e2) {
            Slog.i(TAG, "No package operation state.");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0048  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004f  */
    private static void parsePackageState(XmlPullParser parser) throws IOException, XmlPullParserException {
        boolean z;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String name = parser.getName();
                int hashCode = name.hashCode();
                if (hashCode != 511131722) {
                    if (hashCode == 1435800058 && name.equals(TAG_PACKAGE_UNINSTALLED)) {
                        z = true;
                        if (!z) {
                            Slog.d(TAG, "parse package-state tag.");
                        } else if (z) {
                            parsePackage(parser);
                        }
                    }
                } else if (name.equals(TAG_PACKAGE_STATE)) {
                    z = false;
                    if (!z) {
                    }
                }
                z = true;
                if (!z) {
                }
            }
        }
    }

    private static void parsePackage(XmlPullParser parser) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String name = parser.getName();
                char c = 65535;
                if (name.hashCode() == -807062458 && name.equals("package")) {
                    c = 0;
                }
                if (c == 0) {
                    String name2 = parser.getAttributeValue(null, "name");
                    if (sDebugOn) {
                        Slog.d(TAG, "parsePackage::name[" + name2 + "].");
                    }
                    sUninstalledPackagesByUser.add(name2);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void persistence() {
        AtomicFile destination = new AtomicFile(new File(PACKAGE_STATE_FILE));
        FileOutputStream out = null;
        try {
            out = destination.startWrite();
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(out, StandardCharsets.UTF_8.name());
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startDocument(null, true);
            serializer.startTag(null, TAG_PACKAGE_STATE);
            serializer.startTag(null, TAG_PACKAGE_UNINSTALLED);
            Iterator<String> it = sUninstalledPackagesByUser.iterator();
            while (it.hasNext()) {
                serializer.startTag(null, "package");
                serializer.attribute(null, "name", it.next());
                serializer.endTag(null, "package");
            }
            serializer.endTag(null, TAG_PACKAGE_UNINSTALLED);
            serializer.endTag(null, TAG_PACKAGE_STATE);
            serializer.endDocument();
            destination.finishWrite(out);
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            throw th;
        }
        IoUtils.closeQuietly(out);
    }

    public static File getOppoCotaDirectory() {
        try {
            Method method = Environment.class.getMethod("getResourceDirectory", new Class[0]);
            method.setAccessible(true);
            Object custom = method.invoke(null, new Object[0]);
            if (custom != null) {
                return (File) custom;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:237:0x0770 A[LOOP:0: B:51:0x014e->B:237:0x0770, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:268:0x0808 A[SYNTHETIC, Splitter:B:268:0x0808] */
    /* JADX WARNING: Removed duplicated region for block: B:274:0x082a  */
    /* JADX WARNING: Removed duplicated region for block: B:285:0x0767 A[SYNTHETIC] */
    private static void parseConfigMap() {
        XmlPullParserException e;
        FileNotFoundException e2;
        IOException e3;
        FileInputStream is;
        ArrayList<Element> permissionGroupList;
        int count;
        if (!sConfigXmlParsed) {
            synchronized (sLock) {
                if (!sConfigXmlParsed) {
                    File configMapFile = null;
                    File cotaConfigMapFile = null;
                    String operator = "";
                    File cotaDir = getOppoCotaDirectory();
                    if (cotaDir != null) {
                        Slog.i(TAG, " cotaDir " + cotaDir.getPath());
                        cotaConfigMapFile = new File(cotaDir, "etc/operator_config_map.xml");
                        Slog.i(TAG, " cotaConfigMapFile " + cotaConfigMapFile.getPath());
                        if (cotaConfigMapFile.exists()) {
                            sHasCotaConfigMap = true;
                            configMapFile = cotaConfigMapFile;
                        }
                    }
                    if (sHasCotaConfigMap) {
                        operator = COTA_TAG;
                    } else {
                        if (!"VODAFONE_EEA".equals(sOperator)) {
                            if (!"VODAFONE_NONEEA".equals(sOperator)) {
                                if (TextUtils.isEmpty(sOperator) && "EUEX".equals(sRegion)) {
                                    operator = OPERATOR_TMOBILE;
                                } else if ("ALTICE".equals(sOperator)) {
                                    operator = OPERATOR_ALTICE;
                                }
                            }
                        }
                        operator = OPERATOR_VODAFONE;
                    }
                    int subOperatorNum = 1;
                    if (!TextUtils.isEmpty(operator) && operator.contains(";")) {
                        subOperatorNum = operator.split(";").length;
                    }
                    if (configMapFile == null) {
                        configMapFile = new File("/system/oppo/operator_config_map.xml");
                    }
                    Slog.i(TAG, "parseConfigMap for " + operator + " subOperatorNum " + subOperatorNum + " configMapFile " + configMapFile.getPath());
                    if (!TextUtils.isEmpty(operator)) {
                        if (configMapFile.exists()) {
                            FileInputStream is2 = null;
                            try {
                                is2 = new FileInputStream(configMapFile);
                                try {
                                    ArrayList<Element> bookMarkList = new ArrayList<>();
                                    ArrayList<Element> homePageList = new ArrayList<>();
                                    ArrayList<Element> emailList = new ArrayList<>();
                                    ArrayList<Element> accountList = new ArrayList<>();
                                    ArrayList<Element> signatureList = new ArrayList<>();
                                    ArrayList<Element> permissionGroupList2 = new ArrayList<>();
                                    ArrayList<Element> permissionList = new ArrayList<>();
                                    ArrayList<Element> pkgList = new ArrayList<>();
                                    XmlPullParser parser = Xml.newPullParser();
                                    try {
                                        parser.setInput(is2, "utf-8");
                                        int type = parser.getEventType();
                                        int count2 = 0;
                                        String parsedOperator = "";
                                        while (true) {
                                            if (type == 1) {
                                                is = is2;
                                                break;
                                            }
                                            if (type == 2) {
                                                is = is2;
                                                permissionGroupList = permissionGroupList2;
                                                if ("Operator".equals(parser.getName())) {
                                                    parsedOperator = parser.getAttributeValue(null, "name");
                                                    count = count2;
                                                    type = parser.next();
                                                    if (count < subOperatorNum) {
                                                    }
                                                } else if ("Bookmarks".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                    Slog.i(TAG, "start parse Bookmarks for " + parsedOperator);
                                                } else if ("Bookmark".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                    bookMarkList.add(createBookmarkFromXml(parser, parsedOperator));
                                                } else if ("Homepages".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                    Slog.i(TAG, "start parse Homepages for " + parsedOperator);
                                                } else if ("Homepage".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                    HomePage homePage = createHomepageFromXml(parser, parsedOperator);
                                                    homePageList.add(homePage);
                                                    if (sDebugOn) {
                                                        Slog.i(TAG, "print parsed homePage " + homePage + " operator " + parsedOperator);
                                                    }
                                                } else if ("Emails".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                    Slog.i(TAG, "start parse Emails for " + parsedOperator);
                                                } else if ("Email".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                    Email email = createEmailFromXml(parser, parsedOperator);
                                                    emailList.add(email);
                                                    if (sDebugOn) {
                                                        Slog.i(TAG, "print parsed Email " + email + " operator " + parsedOperator);
                                                    }
                                                } else if ("Accounts".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                    Slog.i(TAG, "start parse Accounts for " + parsedOperator);
                                                } else if ("Account".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                    Account account = createAccountFromXml(parser, parsedOperator);
                                                    accountList.add(account);
                                                    if (sDebugOn) {
                                                        Slog.i(TAG, "print parsed Account " + account + " operator " + parsedOperator);
                                                    }
                                                } else if ("Signatures".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                    Slog.i(TAG, "start parse Signatures for " + parsedOperator);
                                                } else if ("Signature".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                    Signature signature = createSignatureFromXml(parser, parsedOperator);
                                                    signatureList.add(signature);
                                                    if (sDebugOn) {
                                                        Slog.i(TAG, "print parsed Signature " + signature + " operator " + parsedOperator);
                                                    }
                                                } else if ("Permissions".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                    Slog.i(TAG, "start parse Permissions for " + parsedOperator);
                                                } else if ("Permission".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                    Permission permission = createPermissionFromXml(parser, parsedOperator);
                                                    permissionList.add(permission);
                                                    if (sDebugOn) {
                                                        Slog.i(TAG, "print parsed Permission " + permission + " operator " + parsedOperator);
                                                    }
                                                } else if ("PermissionsGroups".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                    Slog.i(TAG, "start parse PermissionsGroups for " + parsedOperator);
                                                } else if ("PermissionGroup".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                    PermissionGroup permissionGroup = createPermissionGroupFromXml(parser, parsedOperator);
                                                    permissionGroupList.add(permissionGroup);
                                                    if (sDebugOn) {
                                                        StringBuilder sb = new StringBuilder();
                                                        permissionGroupList = permissionGroupList;
                                                        sb.append("print parsed PermissionGroup ");
                                                        sb.append(permissionGroup);
                                                        sb.append(" operator ");
                                                        sb.append(parsedOperator);
                                                        Slog.i(TAG, sb.toString());
                                                    } else {
                                                        permissionGroupList = permissionGroupList;
                                                    }
                                                } else if ("Packages".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                    Slog.i(TAG, "start parse PermissionsGroups for " + parsedOperator);
                                                } else if ("Package".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                    Package pkg = createPackageFromXml(parser, parsedOperator);
                                                    if (pkg != null && TemperatureProvider.SWITCH_OFF.equals(pkg.getRemovable()) && TemperatureProvider.SWITCH_OFF.equals(pkg.getReboot()) && !sNeedEnableSimTriggeredApps) {
                                                        Slog.i(TAG, "set needEnableSimTriggeredApps true for  " + pkg);
                                                        sNeedEnableSimTriggeredApps = true;
                                                    }
                                                    pkgList.add(pkg);
                                                    if (sDebugOn) {
                                                        StringBuilder sb2 = new StringBuilder();
                                                        pkgList = pkgList;
                                                        sb2.append("print parsed Package ");
                                                        sb2.append(pkg);
                                                        sb2.append(" operator ");
                                                        sb2.append(parsedOperator);
                                                        Slog.i(TAG, sb2.toString());
                                                    } else {
                                                        pkgList = pkgList;
                                                    }
                                                }
                                            } else if (type != 3) {
                                                is = is2;
                                                permissionGroupList = permissionGroupList2;
                                            } else {
                                                try {
                                                    if ("Operator".equals(parser.getName())) {
                                                        if (isOperatorMatch(parsedOperator, operator)) {
                                                            count = count2 + 1;
                                                            if (count >= subOperatorNum) {
                                                                is = is2;
                                                                try {
                                                                    StringBuilder sb3 = new StringBuilder();
                                                                    permissionGroupList = permissionGroupList2;
                                                                    sb3.append("end parse Operator ");
                                                                    sb3.append(parsedOperator);
                                                                    sb3.append(" and stop parsing . count ");
                                                                    sb3.append(count);
                                                                    sb3.append(" subOperatorNum ");
                                                                    sb3.append(subOperatorNum);
                                                                    Slog.i(TAG, sb3.toString());
                                                                    parsedOperator = "";
                                                                    type = parser.next();
                                                                    if (count < subOperatorNum) {
                                                                        Slog.i(TAG, "stop parsing");
                                                                        break;
                                                                    }
                                                                    count2 = count;
                                                                    cotaDir = cotaDir;
                                                                    is2 = is;
                                                                    permissionGroupList2 = permissionGroupList;
                                                                } catch (XmlPullParserException e4) {
                                                                    e = e4;
                                                                    is2 = is;
                                                                    Slog.e(TAG, "parseConfigMap XmlPullParserException ", e);
                                                                    if (is2 != null) {
                                                                    }
                                                                    Slog.i(TAG, "parseConfigMap finish");
                                                                    sConfigXmlParsed = true;
                                                                    if (!sSpnCandidates.isEmpty()) {
                                                                    }
                                                                } catch (FileNotFoundException e5) {
                                                                    e2 = e5;
                                                                    is2 = is;
                                                                    Slog.e(TAG, "parseConfigMap FileNotFoundException ", e2);
                                                                    if (is2 != null) {
                                                                    }
                                                                    Slog.i(TAG, "parseConfigMap finish");
                                                                    sConfigXmlParsed = true;
                                                                    if (!sSpnCandidates.isEmpty()) {
                                                                    }
                                                                } catch (IOException e6) {
                                                                    e3 = e6;
                                                                    is2 = is;
                                                                    Slog.e(TAG, "parseConfigMap IOException ", e3);
                                                                    if (is2 != null) {
                                                                    }
                                                                    Slog.i(TAG, "parseConfigMap finish");
                                                                    sConfigXmlParsed = true;
                                                                    if (!sSpnCandidates.isEmpty()) {
                                                                    }
                                                                }
                                                            } else {
                                                                is = is2;
                                                                permissionGroupList = permissionGroupList2;
                                                                count2 = count;
                                                            }
                                                        } else {
                                                            is = is2;
                                                            permissionGroupList = permissionGroupList2;
                                                        }
                                                        Slog.i(TAG, "end parse Operator " + parsedOperator);
                                                        parsedOperator = "";
                                                        count = count2;
                                                        type = parser.next();
                                                        if (count < subOperatorNum) {
                                                        }
                                                    } else {
                                                        is = is2;
                                                        permissionGroupList = permissionGroupList2;
                                                        if ("Bookmarks".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                            Slog.i(TAG, "end parse Bookmarks for " + parsedOperator);
                                                            if (sDebugOn) {
                                                                for (Iterator<Element> it = bookMarkList.iterator(); it.hasNext(); it = it) {
                                                                    Slog.i(TAG, "print parsed bookmark " + it.next());
                                                                }
                                                            }
                                                            mBookMarks.put(parsedOperator, (ArrayList) bookMarkList.clone());
                                                            bookMarkList.clear();
                                                        } else if ("Homepages".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                            Slog.i(TAG, "end parse Homepages for " + parsedOperator);
                                                            mHomePages.put(parsedOperator, (ArrayList) homePageList.clone());
                                                            homePageList.clear();
                                                        } else if ("Emails".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                            Slog.i(TAG, "end parse Emails for " + parsedOperator);
                                                            mEmails.put(parsedOperator, (ArrayList) emailList.clone());
                                                            emailList.clear();
                                                        } else if ("Accounts".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                            Slog.i(TAG, "end parse mAccounts for " + parsedOperator);
                                                            mAccounts.put(parsedOperator, (ArrayList) accountList.clone());
                                                            accountList.clear();
                                                        } else if ("Signatures".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                            Slog.i(TAG, "end parse Signatures for " + parsedOperator);
                                                            mSignatures.put(parsedOperator, (ArrayList) signatureList.clone());
                                                            signatureList.clear();
                                                        } else if ("Permissions".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                            Slog.i(TAG, "end parse Permissions for " + parsedOperator);
                                                            mPermssions.put(parsedOperator, (ArrayList) permissionList.clone());
                                                            permissionList.clear();
                                                        } else if ("PermissionsGroups".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                            Slog.i(TAG, "end parse PermissionsGroups for " + parsedOperator);
                                                            mPermissionGroups.put(parsedOperator, (ArrayList) permissionGroupList.clone());
                                                            permissionGroupList.clear();
                                                        } else if ("Packages".equals(parser.getName()) && isOperatorMatch(parsedOperator, operator)) {
                                                            Slog.i(TAG, "end parse Packages for " + parsedOperator);
                                                            mPackages.put(parsedOperator, (ArrayList) pkgList.clone());
                                                            pkgList.clear();
                                                        }
                                                    }
                                                } catch (XmlPullParserException e7) {
                                                    e = e7;
                                                    Slog.e(TAG, "parseConfigMap XmlPullParserException ", e);
                                                    if (is2 != null) {
                                                    }
                                                    Slog.i(TAG, "parseConfigMap finish");
                                                    sConfigXmlParsed = true;
                                                    if (!sSpnCandidates.isEmpty()) {
                                                    }
                                                } catch (FileNotFoundException e8) {
                                                    e2 = e8;
                                                    Slog.e(TAG, "parseConfigMap FileNotFoundException ", e2);
                                                    if (is2 != null) {
                                                    }
                                                    Slog.i(TAG, "parseConfigMap finish");
                                                    sConfigXmlParsed = true;
                                                    if (!sSpnCandidates.isEmpty()) {
                                                    }
                                                } catch (IOException e9) {
                                                    e3 = e9;
                                                    Slog.e(TAG, "parseConfigMap IOException ", e3);
                                                    if (is2 != null) {
                                                    }
                                                    Slog.i(TAG, "parseConfigMap finish");
                                                    sConfigXmlParsed = true;
                                                    if (!sSpnCandidates.isEmpty()) {
                                                    }
                                                }
                                            }
                                            count = count2;
                                            type = parser.next();
                                            if (count < subOperatorNum) {
                                            }
                                        }
                                        is2 = is;
                                    } catch (XmlPullParserException e10) {
                                        e = e10;
                                        Slog.e(TAG, "parseConfigMap XmlPullParserException ", e);
                                        if (is2 != null) {
                                        }
                                        Slog.i(TAG, "parseConfigMap finish");
                                        sConfigXmlParsed = true;
                                        if (!sSpnCandidates.isEmpty()) {
                                        }
                                    } catch (FileNotFoundException e11) {
                                        e2 = e11;
                                        Slog.e(TAG, "parseConfigMap FileNotFoundException ", e2);
                                        if (is2 != null) {
                                        }
                                        Slog.i(TAG, "parseConfigMap finish");
                                        sConfigXmlParsed = true;
                                        if (!sSpnCandidates.isEmpty()) {
                                        }
                                    } catch (IOException e12) {
                                        e3 = e12;
                                        Slog.e(TAG, "parseConfigMap IOException ", e3);
                                        if (is2 != null) {
                                        }
                                        Slog.i(TAG, "parseConfigMap finish");
                                        sConfigXmlParsed = true;
                                        if (!sSpnCandidates.isEmpty()) {
                                        }
                                    }
                                } catch (XmlPullParserException e13) {
                                    e = e13;
                                    Slog.e(TAG, "parseConfigMap XmlPullParserException ", e);
                                    if (is2 != null) {
                                    }
                                    Slog.i(TAG, "parseConfigMap finish");
                                    sConfigXmlParsed = true;
                                    if (!sSpnCandidates.isEmpty()) {
                                    }
                                } catch (FileNotFoundException e14) {
                                    e2 = e14;
                                    Slog.e(TAG, "parseConfigMap FileNotFoundException ", e2);
                                    if (is2 != null) {
                                    }
                                    Slog.i(TAG, "parseConfigMap finish");
                                    sConfigXmlParsed = true;
                                    if (!sSpnCandidates.isEmpty()) {
                                    }
                                } catch (IOException e15) {
                                    e3 = e15;
                                    Slog.e(TAG, "parseConfigMap IOException ", e3);
                                    if (is2 != null) {
                                    }
                                    Slog.i(TAG, "parseConfigMap finish");
                                    sConfigXmlParsed = true;
                                    if (!sSpnCandidates.isEmpty()) {
                                    }
                                }
                            } catch (XmlPullParserException e16) {
                                e = e16;
                                Slog.e(TAG, "parseConfigMap XmlPullParserException ", e);
                                if (is2 != null) {
                                }
                                Slog.i(TAG, "parseConfigMap finish");
                                sConfigXmlParsed = true;
                                if (!sSpnCandidates.isEmpty()) {
                                }
                            } catch (FileNotFoundException e17) {
                                e2 = e17;
                                Slog.e(TAG, "parseConfigMap FileNotFoundException ", e2);
                                if (is2 != null) {
                                }
                                Slog.i(TAG, "parseConfigMap finish");
                                sConfigXmlParsed = true;
                                if (!sSpnCandidates.isEmpty()) {
                                }
                            } catch (IOException e18) {
                                e3 = e18;
                                Slog.e(TAG, "parseConfigMap IOException ", e3);
                                if (is2 != null) {
                                }
                                Slog.i(TAG, "parseConfigMap finish");
                                sConfigXmlParsed = true;
                                if (!sSpnCandidates.isEmpty()) {
                                }
                            }
                            if (is2 != null) {
                                try {
                                    is2.close();
                                } catch (IOException e19) {
                                    Slog.e(TAG, "IOException  is.close", e19);
                                }
                            }
                            Slog.i(TAG, "parseConfigMap finish");
                            sConfigXmlParsed = true;
                            if (!sSpnCandidates.isEmpty()) {
                                sNeedCheckSpn = true;
                            }
                        }
                    }
                    Slog.w(TAG, "Whoops configMapFile does not exist or invalid operator " + operator);
                    sConfigXmlParsed = true;
                }
            }
        }
    }

    private static boolean isOperatorMatch(String parsedOperator, String subOperators) {
        if (TextUtils.isEmpty(parsedOperator) || TextUtils.isEmpty(subOperators)) {
            return false;
        }
        if (!subOperators.contains(";")) {
            if (parsedOperator.equals(subOperators)) {
                return true;
            }
            return false;
        } else if (!subOperators.contains(parsedOperator)) {
            return false;
        } else {
            for (String operator : subOperators.split(";")) {
                if (parsedOperator.equals(operator)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static Package createPackageFromXml(XmlPullParser parser, String operator) {
        Package pkg = new Package();
        if (parser != null) {
            pkg.setCountry(parser.getAttributeValue(null, "country"));
            pkg.setPkgName(parser.getAttributeValue(null, "name"));
            pkg.setRemovable(parser.getAttributeValue(null, "removable"));
            pkg.setReboot(parser.getAttributeValue(null, BackLightStat.VALUE_UPLOAD_REBOOT));
            pkg.setPath(parser.getAttributeValue(null, "path"));
        }
        if (sDebugOn) {
            Slog.i(TAG, "createPackageFromXml " + pkg);
        }
        return pkg;
    }

    private static Permission createPermissionFromXml(XmlPullParser parser, String operator) {
        boolean systemFixed;
        Set<String> packages = new HashSet<>();
        Permission perm = new Permission();
        if (parser != null) {
            perm.setCountry(parser.getAttributeValue(null, "country"));
            perm.setLevel(parser.getAttributeValue(null, "level"));
            perm.setPermName(parser.getAttributeValue(null, "name"));
            String pkgs = parser.getAttributeValue(null, "packages");
            if (!TextUtils.isEmpty(pkgs)) {
                if (pkgs.contains(";")) {
                    for (String pkg : pkgs.split(";")) {
                        packages.add(pkg);
                    }
                } else {
                    packages.add(pkgs);
                }
            }
            perm.setPackages(packages);
            perm.setGrant(parser.getAttributeValue(null, "grant"));
            String systemFixedStr = parser.getAttributeValue(null, "systemFixed");
            if (TextUtils.isEmpty(systemFixedStr) || !systemFixedStr.equals(TemperatureProvider.SWITCH_ON)) {
                systemFixed = false;
            } else {
                systemFixed = true;
            }
            perm.setSystemFixed(systemFixed);
        }
        if (sDebugOn) {
            Slog.i(TAG, "createPermissionFromXml " + perm);
        }
        return perm;
    }

    private static PermissionGroup createPermissionGroupFromXml(XmlPullParser parser, String operator) {
        boolean systemFixed;
        Set<String> packages = new HashSet<>();
        PermissionGroup permGrp = new PermissionGroup();
        if (parser != null) {
            permGrp.setCountry(parser.getAttributeValue(null, "country"));
            permGrp.setLevel(parser.getAttributeValue(null, "level"));
            String name = parser.getAttributeValue(null, "name");
            permGrp.setGrpName(name);
            Set<String> perms = NAME_TO_SET.get(name);
            if (perms != null && !perms.isEmpty()) {
                permGrp.setPermissions(perms);
            }
            String pkgs = parser.getAttributeValue(null, "packages");
            if (!TextUtils.isEmpty(pkgs)) {
                if (pkgs.contains(";")) {
                    for (String pkg : pkgs.split(";")) {
                        packages.add(pkg);
                    }
                } else {
                    packages.add(pkgs);
                }
            }
            permGrp.setPackages(packages);
            permGrp.setGrant(parser.getAttributeValue(null, "grant"));
            String systemFixedStr = parser.getAttributeValue(null, "systemFixed");
            if (TextUtils.isEmpty(systemFixedStr) || !systemFixedStr.equals(TemperatureProvider.SWITCH_ON)) {
                systemFixed = false;
            } else {
                systemFixed = true;
            }
            permGrp.setSystemFixed(systemFixed);
        }
        if (sDebugOn) {
            Slog.i(TAG, "createPermissionGroupFromXml " + permGrp);
        }
        return permGrp;
    }

    private static Email createEmailFromXml(XmlPullParser parser, String operator) {
        Email email = new Email();
        if (parser != null) {
            email.setCountry(parser.getAttributeValue(null, "country"));
            email.setId(parser.getAttributeValue(null, "id"));
            email.setLabel(parser.getAttributeValue(null, "label"));
            email.setDomain(parser.getAttributeValue(null, "domain"));
            email.setSignature(parser.getAttributeValue(null, "signature"));
            email.setIncomingUriTemplate(parser.getAttributeValue(null, "incomingUriTemplate"));
            email.setIncomingUsernameTemplate(parser.getAttributeValue(null, "incomingUsernameTemplate"));
            email.setOutgoingUriTemplate(parser.getAttributeValue(null, "outgoingUriTemplate"));
            email.setOutgoingUsernameTemplate(parser.getAttributeValue(null, "outgoingUsernameTemplate"));
            String spn = parser.getAttributeValue(null, "spn");
            if (!TextUtils.isEmpty(spn)) {
                email.setSpn(spn);
                sSpnCandidates.add(spn);
            }
        }
        if (sDebugOn) {
            Slog.i(TAG, "createEmailFromXml " + email);
        }
        return email;
    }

    private static Account createAccountFromXml(XmlPullParser parser, String operator) {
        Account account = new Account();
        if (parser != null) {
            account.setCountry(parser.getAttributeValue(null, "country"));
            account.setLabel(parser.getAttributeValue(null, "label"));
            account.setLogo(parser.getAttributeValue(null, "logo"));
            String spn = parser.getAttributeValue(null, "spn");
            if (!TextUtils.isEmpty(spn)) {
                account.setSpn(spn);
                sSpnCandidates.add(spn);
            }
        }
        if (sDebugOn) {
            Slog.i(TAG, "createAccountFromXml " + account);
        }
        return account;
    }

    private static HomePage createHomepageFromXml(XmlPullParser parser, String operator) {
        HomePage homePage = new HomePage();
        if (parser != null) {
            homePage.setLabel(parser.getAttributeValue(null, "label"));
            homePage.setUrl(parser.getAttributeValue(null, "url"));
            homePage.setCountry(parser.getAttributeValue(null, "country"));
            String spn = parser.getAttributeValue(null, "spn");
            if (!TextUtils.isEmpty(spn)) {
                homePage.setSpn(spn);
                sSpnCandidates.add(spn);
            }
        }
        if (sDebugOn) {
            Slog.i(TAG, "createHomepageFromXml " + homePage);
        }
        return homePage;
    }

    private static BookMark createBookmarkFromXml(XmlPullParser parser, String operator) {
        BookMark bookMark = new BookMark();
        if (parser != null) {
            bookMark.setLabel(parser.getAttributeValue(null, "label"));
            bookMark.setUrl(parser.getAttributeValue(null, "url"));
            bookMark.setCountry(parser.getAttributeValue(null, "country"));
            String folder = parser.getAttributeValue(null, "folder");
            if (folder == null) {
                folder = operator;
            }
            bookMark.setFolder(folder);
            String spn = parser.getAttributeValue(null, "spn");
            if (!TextUtils.isEmpty(spn)) {
                bookMark.setSpn(spn);
                sSpnCandidates.add(spn);
            }
        }
        if (sDebugOn) {
            Slog.i(TAG, "createBookmarkFromXml " + bookMark);
        }
        return bookMark;
    }

    private static Signature createSignatureFromXml(XmlPullParser parser, String operator) {
        Signature signature = new Signature();
        if (parser != null) {
            signature.setGlobal_signature(parser.getAttributeValue(null, "global_signature"));
            signature.setCountry(parser.getAttributeValue(null, "country"));
            String spn = parser.getAttributeValue(null, "spn");
            if (!TextUtils.isEmpty(spn)) {
                signature.setSpn(spn);
                sSpnCandidates.add(spn);
            }
        }
        if (sDebugOn) {
            Slog.i(TAG, "createSigantureFromXml " + signature);
        }
        return signature;
    }

    public static void copySimTriggeredApps(boolean isUpgrade, Installer installer, Object installLock) {
        String simOperator;
        String simCountry;
        File srcDir;
        File destDir;
        if (OppoOperatorManager.SERVICE_ENABLED) {
            parseConfigMap();
            loadUninstalledPackageList();
            StringBuilder sb = new StringBuilder();
            sb.append("copySimTriggeredApps start isUpgrade:");
            sb.append(isUpgrade);
            sb.append(" installer:");
            sb.append(installer);
            sb.append(" installLock:");
            sb.append(installLock);
            sb.append(" uninstall list:");
            sb.append(!sUninstalledPackagesByUser.isEmpty());
            Slog.i(TAG, sb.toString());
            String simOperator2 = SystemProperties.get(SIM_OPERATOR_PROP, "null");
            String simCountry2 = SystemProperties.get(SIM_COUNTRY_PROP, "null");
            ArrayList<Element> pkgList = mPackages.get(simOperator2);
            if (pkgList == null || pkgList.isEmpty()) {
                Slog.i(TAG, "copySimTriggeredApps no sim triggered apps");
            } else if (!SystemProperties.getBoolean("persist.sys.oppo_optcopied", false) || isUpgrade) {
                Slog.i(TAG, "copySimTriggeredApps operator match , start copy ");
                long startTime = System.currentTimeMillis();
                SystemProperties.set("persist.sys.oppo_optcopied", TemperatureProvider.SWITCH_OFF);
                enableOppoDeriveAbi();
                Package pkg = null;
                File srcFile = null;
                int totalCopyNum = 0;
                int i = 0;
                while (i < pkgList.size()) {
                    if (pkgList.get(i) instanceof Package) {
                        pkg = (Package) pkgList.get(i);
                    }
                    if (pkg == null || !TemperatureProvider.SWITCH_ON.equals(pkg.getReboot())) {
                        simOperator = simOperator2;
                        simCountry = simCountry2;
                    } else if (!TemperatureProvider.SWITCH_ON.equals(pkg.getRemovable())) {
                        simOperator = simOperator2;
                        simCountry = simCountry2;
                    } else if (simCountry2.equals(pkg.getCountry())) {
                        File srcDir2 = new File(pkg.getPath());
                        if (!srcDir2.exists()) {
                            srcDir = srcDir2;
                            simOperator = simOperator2;
                            simCountry = simCountry2;
                        } else if (ArrayUtils.isEmpty(srcDir2.listFiles())) {
                            srcDir = srcDir2;
                            simOperator = simOperator2;
                            simCountry = simCountry2;
                        } else {
                            File[] listFiles = srcDir2.listFiles();
                            int length = listFiles.length;
                            int i2 = 0;
                            while (true) {
                                if (i2 >= length) {
                                    simOperator = simOperator2;
                                    break;
                                }
                                File file = listFiles[i2];
                                simOperator = simOperator2;
                                if (file.getName().startsWith(pkg.getPkgName()) && file.getName().endsWith(".apk")) {
                                    srcFile = file;
                                    break;
                                }
                                i2++;
                                listFiles = listFiles;
                                simOperator2 = simOperator;
                            }
                            if (srcFile == null) {
                                Slog.w(TAG, "src file for " + pkg.getPkgName() + " does not exist, skip copy");
                                simCountry = simCountry2;
                            } else {
                                File destDir2 = new File(DATA_APP_DIR, srcFile.getName().substring(0, srcFile.getName().length() - 4));
                                File oriFile = isPackageExists(pkg.getPkgName());
                                Slog.i(TAG, "isPackageExists " + pkg.getPkgName() + " oriFile " + oriFile + " destDir " + destDir2.getPath() + " srcFile " + srcFile.getPath());
                                if (oriFile == null && !isUninstalledByUser(pkg.getPkgName())) {
                                    copyPackageToData(srcFile, destDir2, pkg.getPkgName(), null);
                                    totalCopyNum++;
                                    simCountry = simCountry2;
                                } else if (oriFile == null || !oriFile.exists() || !oriFile.getName().endsWith(".apk") || !needUpdateApp(srcFile, oriFile)) {
                                    simCountry = simCountry2;
                                } else {
                                    String path = oriFile.getPath();
                                    String apkName = oriFile.getName();
                                    Slog.i(TAG, "oriFile.getPath " + path + " oriFile.getName " + apkName);
                                    if (TextUtils.isEmpty(path)) {
                                        simCountry = simCountry2;
                                    } else if (!path.contains(SliceClientPermissions.SliceAuthority.DELIMITER)) {
                                        simCountry = simCountry2;
                                    } else {
                                        if (path.split(SliceClientPermissions.SliceAuthority.DELIMITER).length > 3) {
                                            destDir = new File(oriFile.getParent());
                                            synchronized (installLock) {
                                                try {
                                                    StringBuilder sb2 = new StringBuilder();
                                                    simCountry = simCountry2;
                                                    sb2.append("removeCodePath directory ");
                                                    sb2.append(destDir.getPath());
                                                    Slog.i(TAG, sb2.toString());
                                                    removeCodePath(destDir, installer);
                                                } catch (Throwable th) {
                                                    th = th;
                                                    throw th;
                                                }
                                            }
                                        } else {
                                            simCountry = simCountry2;
                                            synchronized (installLock) {
                                                Slog.i(TAG, "removeCodePath file " + path);
                                                removeCodePath(oriFile, installer);
                                            }
                                            destDir = destDir2;
                                        }
                                        copyPackageToData(srcFile, destDir, pkg.getPkgName(), apkName);
                                        totalCopyNum++;
                                    }
                                }
                            }
                        }
                    } else {
                        simOperator = simOperator2;
                        simCountry = simCountry2;
                    }
                    i++;
                    simCountry2 = simCountry;
                    simOperator2 = simOperator;
                }
                SystemProperties.set("persist.sys.oppo_optcopied", TemperatureProvider.SWITCH_ON);
                Slog.i(TAG, "copySimTriggeredApps took " + (System.currentTimeMillis() - startTime) + "ms for copying " + totalCopyNum + " apps ");
            } else {
                Slog.i(TAG, "copySimTriggeredApps skip copy, already done ");
            }
        }
    }

    private static void enableOppoDeriveAbi() {
        try {
            Slog.i(TAG, "enableOppoDeriveAbi ");
            Method method = Class.forName("com.android.server.pm.PackageManagerCommonSoft").getDeclaredMethod("enableOppoDeriveAbi", new Class[0]);
            method.setAccessible(true);
            method.invoke(null, new Object[0]);
        } catch (Exception e) {
            Slog.e(TAG, "enableOppoDeriveAbi invoke failure ", e);
        }
    }

    private static boolean isUninstalledByUser(String pkgName) {
        boolean result = sUninstalledPackagesByUser.contains(pkgName);
        Slog.i(TAG, "isUninstalledByUser pkgName " + pkgName + StringUtils.SPACE + result);
        return sUninstalledPackagesByUser.contains(pkgName);
    }

    private static void copyPackageToData(File srcFile, File destDir, String pkgName, String apkName) {
        File destFile;
        if (!srcFile.exists()) {
            Slog.w(TAG, "file " + srcFile.getPath() + " does not exist, skip copy");
            return;
        }
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        FileUtils.setPermissions(destDir.getPath(), 505, -1, -1);
        if (TextUtils.isEmpty(apkName)) {
            destFile = new File(destDir, srcFile.getName());
        } else {
            destFile = new File(destDir, apkName);
        }
        Slog.i(TAG, "apk:" + pkgName + " has NOT been installed, copy it to " + destFile.getPath() + "......");
        FileUtils.copyFile(srcFile, destFile);
        FileUtils.setPermissions(destFile.getPath(), TemperatureProvider.HIGH_TEMPERATURE_THRESHOLD, -1, -1);
    }

    private static void removeCodePath(File codePath, Installer installer) {
        if (codePath.isDirectory()) {
            try {
                installer.rmPackageDir(codePath.getAbsolutePath());
            } catch (Installer.InstallerException e) {
                Slog.w(TAG, "Failed to remove code path", e);
            }
        } else {
            codePath.delete();
        }
    }

    private static boolean needUpdateApp(File apkFile, File oriFile) {
        PackageParser.ApkLite srcPkg = null;
        PackageParser.ApkLite oriPkg = null;
        try {
            srcPkg = PackageParser.parseApkLite(apkFile, 0);
            oriPkg = PackageParser.parseApkLite(oriFile, 0);
        } catch (Exception e) {
            Slog.w(TAG, "failed to parse apkFile " + apkFile.getPath() + " or oriFile  " + oriFile.getPath(), e);
        }
        if (srcPkg != null) {
            Slog.i(TAG, "needUpdateApp src name " + srcPkg.packageName + " version " + srcPkg.getLongVersionCode());
        }
        if (oriPkg != null) {
            Slog.i(TAG, "needUpdateApp ori name " + oriPkg.packageName + " version " + oriPkg.getLongVersionCode() + " ori path " + oriFile.getPath());
        }
        if (srcPkg == null || oriPkg == null || srcPkg.getLongVersionCode() <= oriPkg.getLongVersionCode()) {
            Slog.i(TAG, "needUpdateApp false ");
            return false;
        }
        Slog.i(TAG, "needUpdateApp true srcPkg.getLongVersionCode " + srcPkg.getLongVersionCode() + " oriPkg.getLongVersionCode() " + oriPkg.getLongVersionCode());
        return true;
    }

    public static boolean isInSimTriggeredSystemBlackList(String pkgName) {
        if (!SYSTEM_PACKAGES_OF_INTEREST.contains(pkgName)) {
            return false;
        }
        if (!OppoOperatorManager.SERVICE_ENABLED) {
            return true;
        }
        parseConfigMap();
        String simOperator = SystemProperties.get(SIM_OPERATOR_PROP, "null");
        String simCountry = SystemProperties.get(SIM_COUNTRY_PROP, "null");
        ArrayList<Element> pkgList = mPackages.get(simOperator);
        if (pkgList == null || pkgList.isEmpty()) {
            return true;
        }
        Package pkg = null;
        for (int i = 0; i < pkgList.size(); i++) {
            if (pkgList.get(i) instanceof Package) {
                pkg = (Package) pkgList.get(i);
            }
            if (pkg != null && pkgName != null && pkgName.equals(pkg.getPkgName()) && simCountry.equals(pkg.getCountry()) && TemperatureProvider.SWITCH_ON.equals(pkg.getReboot()) && TemperatureProvider.SWITCH_OFF.equals(pkg.getRemovable())) {
                Slog.i(TAG, "isInSimTriggeredSystemBlackList false " + pkgName + " simCountry " + simCountry + " simOperator " + simOperator);
                return false;
            }
        }
        return true;
    }

    public static File isPackageExists(String pkgName) {
        File dataAppDir = new File(DATA_APP_DIR);
        if (!dataAppDir.exists()) {
            return null;
        }
        File[] files = dataAppDir.listFiles();
        if (ArrayUtils.isEmpty(files)) {
            Slog.d(TAG, "No files in app dir ");
            return null;
        }
        for (File apkFile : files) {
            if (apkFile.getName().startsWith(pkgName)) {
                if (apkFile.isDirectory()) {
                    File[] listFiles = apkFile.listFiles();
                    for (File realApkFile : listFiles) {
                        if (realApkFile.getName().endsWith(".apk")) {
                            Slog.i(TAG, "isPackageExists realApkFile " + realApkFile.getPath());
                            return realApkFile;
                        }
                    }
                    continue;
                } else if (apkFile.getName().endsWith(".apk")) {
                    Slog.i(TAG, "isPackageExists apkFile " + apkFile.getPath());
                    return apkFile;
                } else {
                    Slog.e(TAG, "isPackageExists whoops, it should not happen " + apkFile.getPath());
                    return null;
                }
            }
        }
        return null;
    }
}
