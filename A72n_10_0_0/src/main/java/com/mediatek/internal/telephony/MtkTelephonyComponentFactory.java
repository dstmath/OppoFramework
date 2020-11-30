package com.mediatek.internal.telephony;

import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.DefaultPhoneNotifier;
import com.android.internal.telephony.DeviceStateMonitor;
import com.android.internal.telephony.GsmCdmaCallTracker;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.IccPhoneBookInterfaceManager;
import com.android.internal.telephony.IccSmsInterfaceManager;
import com.android.internal.telephony.ImsSmsDispatcher;
import com.android.internal.telephony.InboundSmsTracker;
import com.android.internal.telephony.MultiSimSettingController;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.PhoneSwitcher;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.RetryManager;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SmsDispatchersController;
import com.android.internal.telephony.SmsStorageMonitor;
import com.android.internal.telephony.SmsUsageMonitor;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.SubscriptionInfoUpdater;
import com.android.internal.telephony.SubscriptionMonitor;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.WapPushOverSms;
import com.android.internal.telephony.WspTypeDecoder;
import com.android.internal.telephony.cat.CatService;
import com.android.internal.telephony.cat.CommandParamsFactory;
import com.android.internal.telephony.cat.IconLoader;
import com.android.internal.telephony.cat.RilMessageDecoder;
import com.android.internal.telephony.cdma.CdmaInboundSmsHandler;
import com.android.internal.telephony.cdma.CdmaSMSDispatcher;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.dataconnection.DataConnection;
import com.android.internal.telephony.dataconnection.DataServiceManager;
import com.android.internal.telephony.dataconnection.DcController;
import com.android.internal.telephony.dataconnection.DcRequest;
import com.android.internal.telephony.dataconnection.DcTesterFailBringUpAll;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.dataconnection.TelephonyNetworkFactory;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmSMSDispatcher;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccProfile;
import com.mediatek.internal.telephony.carrierexpress.CarrierExpressFwkHandler;
import com.mediatek.internal.telephony.cat.MtkCatLog;
import com.mediatek.internal.telephony.cat.MtkCatService;
import com.mediatek.internal.telephony.cat.MtkCommandParamsFactory;
import com.mediatek.internal.telephony.cat.MtkIconLoader;
import com.mediatek.internal.telephony.cat.MtkRilMessageDecoder;
import com.mediatek.internal.telephony.cdma.MtkCdmaInboundSmsHandler;
import com.mediatek.internal.telephony.cdma.MtkCdmaSMSDispatcher;
import com.mediatek.internal.telephony.cdma.MtkCdmaSubscriptionSourceManager;
import com.mediatek.internal.telephony.dataconnection.MtkDataConnection;
import com.mediatek.internal.telephony.dataconnection.MtkDcController;
import com.mediatek.internal.telephony.dataconnection.MtkDcHelper;
import com.mediatek.internal.telephony.dataconnection.MtkDcRequest;
import com.mediatek.internal.telephony.dataconnection.MtkDcTracker;
import com.mediatek.internal.telephony.dataconnection.MtkTelephonyNetworkFactory;
import com.mediatek.internal.telephony.datasub.DataSubSelector;
import com.mediatek.internal.telephony.datasub.SmartDataSwitchAssistant;
import com.mediatek.internal.telephony.gsm.MtkGsmCellBroadcastHandler;
import com.mediatek.internal.telephony.gsm.MtkGsmInboundSmsHandler;
import com.mediatek.internal.telephony.gsm.MtkGsmSMSDispatcher;
import com.mediatek.internal.telephony.imsphone.MtkImsPhone;
import com.mediatek.internal.telephony.imsphone.MtkImsPhoneCallTracker;
import com.mediatek.internal.telephony.phb.MtkIccPhoneBookInterfaceManager;
import com.mediatek.internal.telephony.uicc.MtkUiccController;
import com.mediatek.internal.telephony.uicc.MtkUiccProfile;
import com.mediatek.internal.telephony.worldphone.WorldPhoneUtil;
import dalvik.system.PathClassLoader;

public class MtkTelephonyComponentFactory extends TelephonyComponentFactory {
    private static MtkTelephonyComponentFactory sInstance;

    public static MtkTelephonyComponentFactory getInstance() {
        if (sInstance == null) {
            sInstance = new MtkTelephonyComponentFactory();
        }
        return sInstance;
    }

    public GsmCdmaPhone makePhone(Context context, CommandsInterface ci, PhoneNotifier notifier, int phoneId, int precisePhoneType, TelephonyComponentFactory telephonyComponentFactory) {
        return new MtkGsmCdmaPhone(context, ci, notifier, phoneId, precisePhoneType, telephonyComponentFactory);
    }

    public RIL makeRil(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        return new MtkRIL(context, preferredNetworkType, cdmaSubscription, instanceId);
    }

    public ServiceStateTracker makeServiceStateTracker(GsmCdmaPhone phone, CommandsInterface ci) {
        return new MtkServiceStateTracker(phone, ci);
    }

    public SubscriptionController makeSubscriptionController(Phone phone) {
        return MtkSubscriptionController.mtkInit(phone);
    }

    public SubscriptionController makeSubscriptionController(Context c, CommandsInterface[] ci) {
        return MtkSubscriptionController.mtkInit(c, ci);
    }

    public GsmCdmaCallTracker makeGsmCdmaCallTracker(GsmCdmaPhone phone) {
        return new MtkGsmCdmaCallTracker(phone);
    }

    public SubscriptionInfoUpdater makeSubscriptionInfoUpdater(Looper looper, Context context, Phone[] phone, CommandsInterface[] ci) {
        return new MtkSubscriptionInfoUpdater(looper, context, phone, ci);
    }

    public MultiSimSettingController makeMultiSimSettingController(Context context, SubscriptionController sc) {
        Rlog.d("TelephonyComponentFactory", "makeMultiSimSettingController mtk");
        return new MtkMultiSimSettingController(context, sc);
    }

    public CdmaSubscriptionSourceManager makeCdmaSubscriptionSourceManager(Context context, CommandsInterface ci, Handler h, int what, Object obj) {
        return new MtkCdmaSubscriptionSourceManager(context, ci);
    }

    public DefaultPhoneNotifier makeDefaultPhoneNotifier() {
        Rlog.d("TelephonyComponentFactory", "makeDefaultPhoneNotifier mtk");
        return new MtkPhoneNotifier();
    }

    public UiccController makeUiccController(Context c, CommandsInterface[] ci) {
        Rlog.d("TelephonyComponentFactory", "makeUiccController mtk");
        return new MtkUiccController(c, ci);
    }

    public UiccProfile makeUiccProfile(Context context, CommandsInterface ci, IccCardStatus ics, int phoneId, UiccCard uiccCard, Object lock) {
        return new MtkUiccProfile(context, ci, ics, phoneId, uiccCard, lock);
    }

    public void initRadioManager(Context context, int numPhones, CommandsInterface[] sCommandsInterfaces) {
        RadioManager.init(context, numPhones, sCommandsInterfaces);
    }

    public CatService makeCatService(CommandsInterface ci, UiccCardApplication ca, IccRecords ir, Context context, IccFileHandler fh, UiccProfile uiccProfile, int slotId) {
        int phoneType;
        UiccCardApplication ca2;
        int[] subId = SubscriptionManager.getSubId(slotId);
        if (subId != null) {
            int phoneType2 = TelephonyManager.getDefault().getCurrentPhoneType(subId[0]);
            MtkCatLog.d("MtkCatService", "makeCatService phoneType : " + phoneType2 + " slotId: " + slotId + " subId[0]:" + subId[0]);
            phoneType = phoneType2;
        } else {
            phoneType = 1;
        }
        if (uiccProfile == null) {
            ca2 = ca;
        } else if (phoneType == 2) {
            ca2 = uiccProfile.getApplication(2);
        } else {
            ca2 = uiccProfile.getApplicationIndex(0);
        }
        MtkCatLog.v("MtkCatService", "makeCatService  ca = " + ca2);
        if (ci != null && ca2 != null && ir != null && context != null && fh != null && uiccProfile != null) {
            return new MtkCatService(ci, ca2, ir, context, fh, uiccProfile, slotId);
        }
        MtkCatLog.e("MtkCatService", "makeCatService exception, will not create MtkCatservice!!!!");
        return null;
    }

    public RilMessageDecoder makeRilMessageDecoder(Handler caller, IccFileHandler fh, int slotId) {
        return new MtkRilMessageDecoder(caller, fh, slotId);
    }

    public CommandParamsFactory makeCommandParamsFactory(RilMessageDecoder caller, IccFileHandler fh) {
        return new MtkCommandParamsFactory(caller, fh);
    }

    public IconLoader makeIconLoader(Looper looper, IccFileHandler fh) {
        return new MtkIconLoader(looper, fh);
    }

    public DcTracker makeDcTracker(Phone phone, int transportType) {
        return new MtkDcTracker(phone, transportType);
    }

    public TelephonyNetworkFactory makeTelephonyNetworkFactories(SubscriptionMonitor subscriptionMonitor, Looper looper, Phone phone) {
        return new MtkTelephonyNetworkFactory(subscriptionMonitor, looper, phone);
    }

    public void makeDcHelper(Context context, Phone[] phones) {
        MtkDcHelper.makeMtkDcHelper(context, phones);
    }

    public void makeDataSubSelector(Context context, int numPhones) {
        DataSubSelector.makeDataSubSelector(context, numPhones);
    }

    public void makeSmartDataSwitchAssistant(Context context, Phone[] phones) {
        SmartDataSwitchAssistant.makeSmartDataSwitchAssistant(context, phones);
    }

    public void makeSuppServManager(Context context, Phone[] phones) {
        MtkSuppServManager.makeSuppServManager(context, phones).init();
    }

    public PhoneSwitcher makePhoneSwitcher(int maxActivePhones, int numPhones, Context context, SubscriptionController subscriptionController, Looper looper, ITelephonyRegistry tr, CommandsInterface[] cis, Phone[] phones) {
        return new MtkPhoneSwitcher(maxActivePhones, numPhones, context, subscriptionController, looper, tr, cis, phones);
    }

    public SmsStorageMonitor makeSmsStorageMonitor(Phone phone) {
        return new MtkSmsStorageMonitor(phone);
    }

    public SmsUsageMonitor makeSmsUsageMonitor(Context context) {
        return new MtkSmsUsageMonitor(context);
    }

    public IccSmsInterfaceManager makeIccSmsInterfaceManager(Phone phone) {
        return new MtkIccSmsInterfaceManager(phone);
    }

    public ImsSmsDispatcher makeImsSmsDispatcher(Phone phone, SmsDispatchersController smsDispatchersController) {
        return new MtkImsSmsDispatcher(phone, smsDispatchersController);
    }

    public GsmSMSDispatcher makeGsmSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController, GsmInboundSmsHandler gsmInboundSmsHandler) {
        return new MtkGsmSMSDispatcher(phone, smsDispatchersController, gsmInboundSmsHandler);
    }

    public InboundSmsTracker makeInboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, boolean is3gpp2WapPdu, String address, String displayAddr, String messageBody, boolean isClass0) {
        return new MtkInboundSmsTracker(pdu, timestamp, destPort, is3gpp2, is3gpp2WapPdu, address, displayAddr, messageBody, isClass0);
    }

    public InboundSmsTracker makeInboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, String address, String displayAddr, int referenceNumber, int sequenceNumber, int messageCount, boolean is3gpp2WapPdu, String messageBody, boolean isClass0) {
        return new MtkInboundSmsTracker(pdu, timestamp, destPort, is3gpp2, address, displayAddr, referenceNumber, sequenceNumber, messageCount, is3gpp2WapPdu, messageBody, isClass0);
    }

    public InboundSmsTracker makeInboundSmsTracker(Cursor cursor, boolean isCurrentFormat3gpp2) {
        return new MtkInboundSmsTracker(cursor, isCurrentFormat3gpp2);
    }

    public void makeSmsBroadcastUndelivered(Context context, GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler) {
        MtkSmsBroadcastUndelivered.initialize(context, gsmInboundSmsHandler, cdmaInboundSmsHandler);
    }

    public WspTypeDecoder makeWspTypeDecoder(byte[] pdu) {
        return new MtkWspTypeDecoder(pdu);
    }

    public WapPushOverSms makeWapPushOverSms(Context context) {
        return new MtkWapPushOverSms(context);
    }

    public GsmInboundSmsHandler makeGsmInboundSmsHandler(Context context, SmsStorageMonitor storageMonitor, Phone phone) {
        return MtkGsmInboundSmsHandler.makeInboundSmsHandler(context, storageMonitor, phone);
    }

    public MtkSmsHeader makeSmsHeader() {
        return new MtkSmsHeader();
    }

    public MtkGsmCellBroadcastHandler makeGsmCellBroadcastHandler(Context context, Phone phone) {
        return MtkGsmCellBroadcastHandler.makeGsmCellBroadcastHandler(context, phone);
    }

    public SmsDispatchersController makeSmsDispatchersController(Phone phone, SmsStorageMonitor storageMonitor, SmsUsageMonitor usageMonitor) {
        return new MtkSmsDispatchersController(phone, phone.mSmsStorageMonitor, phone.mSmsUsageMonitor);
    }

    public ProxyController makeProxyController(Context context, Phone[] phone, UiccController uiccController, CommandsInterface[] ci, PhoneSwitcher ps) {
        return new MtkProxyController(context, phone, uiccController, ci, ps);
    }

    public CdmaInboundSmsHandler makeCdmaInboundSmsHandler(Context context, SmsStorageMonitor storageMonitor, Phone phone, CdmaSMSDispatcher smsDispatcher) {
        return new MtkCdmaInboundSmsHandler(context, storageMonitor, phone, smsDispatcher);
    }

    public CdmaSMSDispatcher makeCdmaSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController) {
        return new MtkCdmaSMSDispatcher(phone, smsDispatchersController);
    }

    public void initEmbmsAdaptor(Context context, CommandsInterface[] sCommandsInterfaces) {
        MtkEmbmsAdaptor.getDefault(context, sCommandsInterfaces);
    }

    public void makeWorldPhoneManager() {
        WorldPhoneUtil.makeWorldPhoneManager();
    }

    public IccPhoneBookInterfaceManager makeIccPhoneBookInterfaceManager(Phone phone) {
        Rlog.d("TelephonyComponentFactory", "makeIccPhoneBookInterfaceManager mtk");
        return new MtkIccPhoneBookInterfaceManager(phone);
    }

    public ImsPhoneCallTracker makeImsPhoneCallTracker(ImsPhone imsPhone) {
        return new MtkImsPhoneCallTracker(imsPhone);
    }

    public ImsPhone makeImsPhone(Context context, PhoneNotifier phoneNotifier, Phone defaultPhone) {
        try {
            return new MtkImsPhone(context, phoneNotifier, defaultPhone);
        } catch (Exception e) {
            Rlog.e("TelephonyComponentFactoryEx", "makeImsPhoneExt", e);
            return null;
        }
    }

    public CallManager makeCallManager() {
        return new MtkCallManager();
    }

    public RetryManager makeRetryManager(Phone phone, String apnType) {
        return new MtkRetryManager(phone, apnType);
    }

    public DataConnection makeDataConnection(Phone phone, String name, int id, DcTracker dct, DataServiceManager dataServiceManager, DcTesterFailBringUpAll failBringUpAll, DcController dcc) {
        DataConnection dc = new MtkDataConnection(phone, name, id, dct, dataServiceManager, failBringUpAll, dcc);
        DataConnection.TCP_BUFFER_SIZES_LTE = "2097152,4194304,8388608,262144,524288,1048576";
        return dc;
    }

    public DcController makeDcController(String name, Phone phone, DcTracker dct, DataServiceManager dataServiceManager, Handler handler) {
        return new MtkDcController(name + "-Mtk", phone, dct, dataServiceManager, handler);
    }

    public DcRequest makeDcRequest(NetworkRequest nr, Context context) {
        return new MtkDcRequest(nr, context);
    }

    public ComponentName makeConnectionServiceName() {
        Rlog.d("TelephonyComponentFactory", "makeConnectionServiceName mtk");
        return new ComponentName("com.android.phone", "com.mediatek.services.telephony.MtkTelephonyConnectionService");
    }

    public void makeNetworkStatusUpdater(Phone[] phones, int numPhones) {
        Rlog.d("TelephonyComponentFactory", "Creating NetworkStatusUpdater");
        MtkNetworkStatusUpdater.init(phones, numPhones);
    }

    public DeviceStateMonitor makeDeviceStateMonitor(Phone phone) {
        return new MtkDeviceStateMonitor(phone);
    }

    public void initCarrierExpress() {
        Rlog.d("TelephonyComponentFactory", "Creating CarrierExpress");
        CarrierExpressFwkHandler.init();
    }

    public void initGwsdService(Context context) {
        if (SystemProperties.get("ro.vendor.mtk_telephony_add_on_policy", "0").equals("0")) {
            try {
                Class<?> clazz = Class.forName("com.mediatek.gwsd.service.GwsdService", false, new PathClassLoader("/system/framework/mediatek-gwsdv2.jar", ClassLoader.getSystemClassLoader()));
                Rlog.d("TelephonyComponentFactory", "class = " + clazz);
                clazz.getMethod("getInstance", Context.class).invoke(clazz, context);
            } catch (Exception e) {
                Rlog.e("TelephonyComponentFactory", Log.getStackTraceString(e));
            }
        }
    }
}
