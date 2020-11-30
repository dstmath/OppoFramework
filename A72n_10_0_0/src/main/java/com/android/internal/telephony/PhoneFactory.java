package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.net.LocalServerSocket;
import android.os.Looper;
import android.os.ServiceManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.AnomalyReporter;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.util.LocalLog;
import com.android.internal.os.BackgroundThread;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.dataconnection.TelephonyNetworkFactory;
import com.android.internal.telephony.euicc.EuiccCardController;
import com.android.internal.telephony.euicc.EuiccController;
import com.android.internal.telephony.ims.ImsResolver;
import com.android.internal.telephony.imsphone.ImsPhoneFactory;
import com.android.internal.telephony.sip.SipPhone;
import com.android.internal.telephony.sip.SipPhoneFactory;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.NotificationChannelController;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhoneFactory {
    static final boolean DBG = false;
    static final String LOG_TAG = "PhoneFactory";
    static final int SOCKET_OPEN_MAX_RETRY = 3;
    static final int SOCKET_OPEN_RETRY_MILLIS = 2000;
    private static CellularNetworkValidator sCellularNetworkValidator;
    @UnsupportedAppUsage
    private static CommandsInterface sCommandsInterface = null;
    private static CommandsInterface[] sCommandsInterfaces = null;
    @UnsupportedAppUsage
    private static Context sContext;
    private static List<String> sCptList = null;
    private static EuiccCardController sEuiccCardController;
    private static EuiccController sEuiccController;
    private static ImsResolver sImsResolver;
    private static IntentBroadcaster sIntentBroadcaster;
    private static final HashMap<String, LocalLog> sLocalLogs = new HashMap<>();
    static final Object sLockProxyPhones = new Object();
    @UnsupportedAppUsage
    private static boolean sMadeDefaults = false;
    private static NotificationChannelController sNotificationChannelController;
    private static Phone sPhone = null;
    private static PhoneConfigurationManager sPhoneConfigurationManager;
    @UnsupportedAppUsage
    private static PhoneNotifier sPhoneNotifier;
    private static PhoneSwitcher sPhoneSwitcher;
    private static Phone[] sPhones = null;
    private static ProxyController sProxyController;
    private static SubscriptionInfoUpdater sSubInfoRecordUpdater = null;
    private static SubscriptionMonitor sSubscriptionMonitor;
    private static TelephonyNetworkFactory[] sTelephonyNetworkFactories;
    private static UiccController sUiccController;

    public static void makeDefaultPhones(Context context) {
        makeDefaultPhone(context);
    }

    @UnsupportedAppUsage
    public static void makeDefaultPhone(Context context) {
        int phoneType;
        synchronized (sLockProxyPhones) {
            if (!sMadeDefaults) {
                sContext = context;
                TelephonyDevController.create();
                int retryCount = 0;
                while (true) {
                    boolean hasException = false;
                    int i = 1;
                    int retryCount2 = retryCount + 1;
                    try {
                        new LocalServerSocket("com.android.internal.telephony");
                    } catch (IOException e) {
                        hasException = true;
                    }
                    if (!hasException) {
                        TelephonyComponentFactory telephonyComponentFactory = TelephonyComponentFactory.getInstance().inject(TelephonyComponentFactory.class.getName());
                        sPhoneNotifier = telephonyComponentFactory.makeDefaultPhoneNotifier();
                        int cdmaSubscription = CdmaSubscriptionSourceManager.getDefault(context);
                        Rlog.i(LOG_TAG, "Cdma Subscription set to " + cdmaSubscription);
                        int numPhones = ((TelephonyManager) context.getSystemService("phone")).getPhoneCount();
                        int[] networkModes = new int[numPhones];
                        sPhones = new Phone[numPhones];
                        sCommandsInterfaces = new RIL[numPhones];
                        sTelephonyNetworkFactories = new TelephonyNetworkFactory[numPhones];
                        for (int i2 = 0; i2 < numPhones; i2++) {
                            networkModes[i2] = RILConstants.PREFERRED_NETWORK_MODE;
                            Rlog.i(LOG_TAG, "Network Mode set to " + Integer.toString(networkModes[i2]));
                            sCommandsInterfaces[i2] = telephonyComponentFactory.makeRil(context, networkModes[i2], cdmaSubscription, Integer.valueOf(i2));
                        }
                        telephonyComponentFactory.initRadioManager(context, numPhones, sCommandsInterfaces);
                        sUiccController = UiccController.make(context, sCommandsInterfaces);
                        Rlog.i(LOG_TAG, "Creating SubscriptionController");
                        SubscriptionController.init(context, sCommandsInterfaces);
                        telephonyComponentFactory.initEmbmsAdaptor(context, sCommandsInterfaces);
                        MultiSimSettingController.init(context, SubscriptionController.getInstance());
                        if (context.getPackageManager().hasSystemFeature("android.hardware.telephony.euicc")) {
                            sEuiccController = EuiccController.init(context);
                            sEuiccCardController = EuiccCardController.init(context);
                        }
                        int i3 = 0;
                        while (i3 < numPhones) {
                            Phone phone = null;
                            int phoneType2 = TelephonyManager.getPhoneType(networkModes[i3]);
                            if (phoneType2 == i) {
                                phoneType = phoneType2;
                                phone = telephonyComponentFactory.makePhone(context, sCommandsInterfaces[i3], sPhoneNotifier, i3, 1, TelephonyComponentFactory.getInstance());
                            } else {
                                phoneType = phoneType2;
                                if (phoneType == 2) {
                                    phone = telephonyComponentFactory.makePhone(context, sCommandsInterfaces[i3], sPhoneNotifier, i3, 6, TelephonyComponentFactory.getInstance());
                                }
                            }
                            Rlog.i(LOG_TAG, "Creating Phone with type = " + phoneType + " sub = " + i3);
                            sPhones[i3] = phone;
                            i3++;
                            i = 1;
                        }
                        if (numPhones > 0) {
                            sPhone = sPhones[0];
                            sCommandsInterface = sCommandsInterfaces[0];
                        }
                        ComponentName componentName = SmsApplication.getDefaultSmsApplication(context, true);
                        String packageName = "NONE";
                        if (componentName != null) {
                            packageName = componentName.getPackageName();
                        }
                        Rlog.i(LOG_TAG, "defaultSmsApplication: " + packageName);
                        SmsApplication.initSmsPackageMonitor(context);
                        sMadeDefaults = true;
                        telephonyComponentFactory.makeNetworkStatusUpdater(sPhones, numPhones);
                        Rlog.i(LOG_TAG, "Creating SubInfoRecordUpdater ");
                        sSubInfoRecordUpdater = telephonyComponentFactory.makeSubscriptionInfoUpdater(BackgroundThread.get().getLooper(), context, sPhones, sCommandsInterfaces);
                        SubscriptionController.getInstance().updatePhonesAvailability(sPhones);
                        telephonyComponentFactory.makeDataSubSelector(sContext, numPhones);
                        telephonyComponentFactory.makeSuppServManager(sContext, sPhones);
                        telephonyComponentFactory.initGwsdService(context);
                        telephonyComponentFactory.makeSmartDataSwitchAssistant(sContext, sPhones);
                        telephonyComponentFactory.makeDcHelper(sContext, sPhones);
                        if (context.getPackageManager().hasSystemFeature("android.hardware.telephony.ims")) {
                            boolean isDynamicBinding = sContext.getResources().getBoolean(17891429);
                            String defaultImsPackage = sContext.getResources().getString(17039742);
                            Rlog.i(LOG_TAG, "ImsResolver: defaultImsPackage: " + defaultImsPackage);
                            sImsResolver = new ImsResolver(sContext, defaultImsPackage, numPhones, isDynamicBinding);
                            sImsResolver.initPopulateCacheAndStartBind();
                            for (int i4 = 0; i4 < numPhones; i4++) {
                                sPhones[i4].startMonitoringImsService();
                            }
                        } else {
                            Rlog.i(LOG_TAG, "IMS is not supported on this device, skipping ImsResolver.");
                        }
                        ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
                        SubscriptionController sc = SubscriptionController.getInstance();
                        sSubscriptionMonitor = new SubscriptionMonitor(tr, sContext, sc, numPhones);
                        sPhoneConfigurationManager = PhoneConfigurationManager.init(sContext);
                        sCellularNetworkValidator = CellularNetworkValidator.make(sContext);
                        sPhoneSwitcher = PhoneSwitcher.make(sPhoneConfigurationManager.getNumberOfModemsWithSimultaneousDataConnections(), numPhones, sContext, sc, Looper.myLooper(), tr, sCommandsInterfaces, sPhones);
                        sProxyController = ProxyController.getInstance(context, sPhones, sUiccController, sCommandsInterfaces, sPhoneSwitcher);
                        sIntentBroadcaster = IntentBroadcaster.getInstance(context);
                        sNotificationChannelController = new NotificationChannelController(context);
                        sTelephonyNetworkFactories = new TelephonyNetworkFactory[numPhones];
                        int i5 = 0;
                        while (i5 < numPhones) {
                            sTelephonyNetworkFactories[i5] = telephonyComponentFactory.makeTelephonyNetworkFactories(sSubscriptionMonitor, Looper.myLooper(), sPhones[i5]);
                            i5++;
                            componentName = componentName;
                        }
                        telephonyComponentFactory.makeWorldPhoneManager();
                        telephonyComponentFactory.initCarrierExpress();
                    } else if (retryCount2 <= 3) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e2) {
                        }
                        retryCount = retryCount2;
                    } else {
                        throw new RuntimeException("PhoneFactory probably already running");
                    }
                }
            }
        }
    }

    @UnsupportedAppUsage
    public static Phone getDefaultPhone() {
        Phone phone;
        synchronized (sLockProxyPhones) {
            if (sMadeDefaults) {
                phone = sPhone;
            } else {
                throw new IllegalStateException("Default phones haven't been made yet!");
            }
        }
        return phone;
    }

    @UnsupportedAppUsage
    public static Phone getPhone(int phoneId) {
        Phone phone;
        synchronized (sLockProxyPhones) {
            if (!sMadeDefaults) {
                throw new IllegalStateException("Default phones haven't been made yet!");
            } else if (phoneId == Integer.MAX_VALUE) {
                phone = sPhone;
            } else {
                phone = (phoneId < 0 || phoneId >= sPhones.length) ? null : sPhones[phoneId];
            }
        }
        return phone;
    }

    @UnsupportedAppUsage
    public static Phone[] getPhones() {
        Phone[] phoneArr;
        synchronized (sLockProxyPhones) {
            if (sMadeDefaults) {
                phoneArr = sPhones;
            } else {
                throw new IllegalStateException("Default phones haven't been made yet!");
            }
        }
        return phoneArr;
    }

    public static SubscriptionInfoUpdater getSubscriptionInfoUpdater() {
        return sSubInfoRecordUpdater;
    }

    public static ImsResolver getImsResolver() {
        return sImsResolver;
    }

    public static TelephonyNetworkFactory getNetworkFactory(int phoneId) {
        TelephonyNetworkFactory factory;
        synchronized (sLockProxyPhones) {
            if (sMadeDefaults) {
                if (phoneId == Integer.MAX_VALUE) {
                    phoneId = sPhone.getSubId();
                }
                factory = (sTelephonyNetworkFactories == null || phoneId < 0 || phoneId >= sTelephonyNetworkFactories.length) ? null : sTelephonyNetworkFactories[phoneId];
            } else {
                throw new IllegalStateException("Default phones haven't been made yet!");
            }
        }
        return factory;
    }

    public static SipPhone makeSipPhone(String sipUri) {
        return SipPhoneFactory.makePhone(sipUri, sContext, sPhoneNotifier);
    }

    @UnsupportedAppUsage
    public static int calculatePreferredNetworkType(Context context, int phoneSubId) {
        ContentResolver contentResolver = context.getContentResolver();
        int networkType = Settings.Global.getInt(contentResolver, "preferred_network_mode" + phoneSubId, -1);
        Rlog.d(LOG_TAG, "calculatePreferredNetworkType: phoneSubId = " + phoneSubId + " networkType = " + networkType);
        if (networkType != -1) {
            return networkType;
        }
        int networkType2 = RILConstants.PREFERRED_NETWORK_MODE;
        try {
            return TelephonyManager.getIntAtIndex(context.getContentResolver(), "preferred_network_mode", SubscriptionController.getInstance().getPhoneId(phoneSubId));
        } catch (Settings.SettingNotFoundException e) {
            Rlog.e(LOG_TAG, "Settings Exception Reading Value At Index for Settings.Global.PREFERRED_NETWORK_MODE");
            return networkType2;
        }
    }

    @UnsupportedAppUsage
    public static int getDefaultSubscription() {
        return SubscriptionController.getInstance().getDefaultSubId();
    }

    public static boolean isSMSPromptEnabled() {
        int value = 0;
        try {
            value = Settings.Global.getInt(sContext.getContentResolver(), "multi_sim_sms_prompt");
        } catch (Settings.SettingNotFoundException e) {
            Rlog.e(LOG_TAG, "Settings Exception Reading Dual Sim SMS Prompt Values");
        }
        boolean prompt = value != 0;
        Rlog.d(LOG_TAG, "SMS Prompt option:" + prompt);
        return prompt;
    }

    public static Phone makeImsPhone(PhoneNotifier phoneNotifier, Phone defaultPhone) {
        return ImsPhoneFactory.makePhone(sContext, phoneNotifier, defaultPhone);
    }

    public static void requestEmbeddedSubscriptionInfoListRefresh(int cardId, Runnable callback) {
        sSubInfoRecordUpdater.requestEmbeddedSubscriptionInfoListRefresh(cardId, callback);
    }

    public static SmsController getSmsController() {
        SmsController smsController;
        synchronized (sLockProxyPhones) {
            if (sMadeDefaults) {
                smsController = sProxyController.getSmsController();
            } else {
                throw new IllegalStateException("Default phones haven't been made yet!");
            }
        }
        return smsController;
    }

    public static void addLocalLog(String key, int size) {
        synchronized (sLocalLogs) {
            if (!sLocalLogs.containsKey(key)) {
                sLocalLogs.put(key, new LocalLog(size));
            } else {
                throw new IllegalArgumentException("key " + key + " already present");
            }
        }
    }

    public static void localLog(String key, String log) {
        synchronized (sLocalLogs) {
            if (sLocalLogs.containsKey(key)) {
                sLocalLogs.get(key).log(log);
            } else {
                throw new IllegalArgumentException("key " + key + " not found");
            }
        }
    }

    public static void dump(FileDescriptor fd, PrintWriter printwriter, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(printwriter, "  ");
        pw.println("PhoneFactory:");
        pw.println(" sMadeDefaults=" + sMadeDefaults);
        sPhoneSwitcher.dump(fd, pw, args);
        pw.println();
        Phone[] phones = getPhones();
        for (int i = 0; i < phones.length; i++) {
            pw.increaseIndent();
            try {
                phones[i].dump(fd, pw, args);
                pw.flush();
                pw.println("++++++++++++++++++++++++++++++++");
                sTelephonyNetworkFactories[i].dump(fd, pw, args);
                pw.flush();
                pw.decreaseIndent();
                pw.println("++++++++++++++++++++++++++++++++");
            } catch (Exception e) {
                pw.println("Telephony DebugService: Could not get Phone[" + i + "] e=" + e);
            }
        }
        pw.println("SubscriptionMonitor:");
        pw.increaseIndent();
        try {
            sSubscriptionMonitor.dump(fd, pw, args);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("UiccController:");
        pw.increaseIndent();
        try {
            sUiccController.dump(fd, pw, args);
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        pw.flush();
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        if (sEuiccController != null) {
            pw.println("EuiccController:");
            pw.increaseIndent();
            try {
                sEuiccController.dump(fd, pw, args);
                sEuiccCardController.dump(fd, pw, args);
            } catch (Exception e4) {
                e4.printStackTrace();
            }
            pw.flush();
            pw.decreaseIndent();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        pw.println("SubscriptionController:");
        pw.increaseIndent();
        try {
            SubscriptionController.getInstance().dump(fd, pw, args);
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        pw.flush();
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("SubInfoRecordUpdater:");
        pw.increaseIndent();
        try {
            sSubInfoRecordUpdater.dump(fd, pw, args);
        } catch (Exception e6) {
            e6.printStackTrace();
        }
        pw.flush();
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("LocalLogs:");
        pw.increaseIndent();
        synchronized (sLocalLogs) {
            for (String key : sLocalLogs.keySet()) {
                pw.println(key);
                pw.increaseIndent();
                sLocalLogs.get(key).dump(fd, pw, args);
                pw.decreaseIndent();
            }
            pw.flush();
        }
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("SharedPreferences:");
        pw.increaseIndent();
        try {
            if (sContext != null) {
                Map spValues = PreferenceManager.getDefaultSharedPreferences(sContext).getAll();
                for (Object key2 : spValues.keySet()) {
                    pw.println(key2 + " : " + spValues.get(key2));
                }
            }
        } catch (Exception e7) {
            e7.printStackTrace();
        }
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("DebugEvents:");
        pw.increaseIndent();
        try {
            AnomalyReporter.dump(fd, pw, args);
        } catch (Exception e8) {
            e8.printStackTrace();
        }
        pw.flush();
        pw.decreaseIndent();
    }

    public static List<String> updateWhiteList(int tag) {
        sCptList = ActivityThread.getCptListByType(tag);
        return sCptList;
    }

    public static boolean inWhiteList(String packageName) {
        List<String> list = sCptList;
        if (list == null || !list.contains(packageName)) {
            return false;
        }
        Rlog.d(LOG_TAG, "inWhiteList return true, packageName : " + packageName);
        return true;
    }
}
