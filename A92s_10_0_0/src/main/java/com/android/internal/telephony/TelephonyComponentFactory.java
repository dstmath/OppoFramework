package com.android.internal.telephony;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.ServiceManager;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStatVfs;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.cat.CatService;
import com.android.internal.telephony.cat.CommandParamsFactory;
import com.android.internal.telephony.cat.IconLoader;
import com.android.internal.telephony.cat.RilMessageDecoder;
import com.android.internal.telephony.cdma.CdmaInboundSmsHandler;
import com.android.internal.telephony.cdma.CdmaSMSDispatcher;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.cdma.EriManager;
import com.android.internal.telephony.dataconnection.DataConnection;
import com.android.internal.telephony.dataconnection.DataEnabledSettings;
import com.android.internal.telephony.dataconnection.DataServiceManager;
import com.android.internal.telephony.dataconnection.DcController;
import com.android.internal.telephony.dataconnection.DcRequest;
import com.android.internal.telephony.dataconnection.DcTesterFailBringUpAll;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.dataconnection.TelephonyNetworkFactory;
import com.android.internal.telephony.dataconnection.TransportManager;
import com.android.internal.telephony.emergency.EmergencyNumberTracker;
import com.android.internal.telephony.gsm.GsmCellBroadcastHandler;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmSMSDispatcher;
import com.android.internal.telephony.imsphone.ImsExternalCallTracker;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccProfile;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class TelephonyComponentFactory {
    public static final String LOG_TAG = "TelephonyComponentFactory";
    /* access modifiers changed from: private */
    public static final String TAG = TelephonyComponentFactory.class.getSimpleName();
    static final boolean USE_NEW_NITZ_STATE_MACHINE = true;
    private static TelephonyComponentFactory sInstance;
    private InjectedComponents mInjectedComponents;

    /* access modifiers changed from: private */
    public static class InjectedComponents {
        private static final String ATTRIBUTE_JAR = "jar";
        private static final String ATTRIBUTE_PACKAGE = "package";
        private static final String PRODUCT = "/product/";
        private static final String SYSTEM = "/system/";
        private static final String TAG_COMPONENT = "component";
        private static final String TAG_COMPONENTS = "components";
        private static final String TAG_INJECTION = "injection";
        /* access modifiers changed from: private */
        public final Set<String> mComponentNames;
        /* access modifiers changed from: private */
        public TelephonyComponentFactory mInjectedInstance;
        private String mJarPath;
        private String mPackageName;

        private InjectedComponents() {
            this.mComponentNames = new HashSet();
        }

        /* access modifiers changed from: private */
        public String getValidatedPaths() {
            String access$000 = TelephonyComponentFactory.TAG;
            Rlog.e(access$000, "getValidatedPaths: " + this.mPackageName + " ," + this.mJarPath);
            if (TextUtils.isEmpty(this.mPackageName) || TextUtils.isEmpty(this.mJarPath)) {
                return null;
            }
            return (String) Arrays.stream(this.mJarPath.split(File.pathSeparator)).filter($$Lambda$TelephonyComponentFactory$InjectedComponents$09rMKC8001jAR0zFrzzlPx26Xjs.INSTANCE).filter($$Lambda$TelephonyComponentFactory$InjectedComponents$UYUq9z2WZwxqOLXquU0tTNN9wAs.INSTANCE).distinct().collect(Collectors.joining(File.pathSeparator));
        }

        static /* synthetic */ boolean lambda$getValidatedPaths$0(String s) {
            return s.startsWith(SYSTEM) || s.startsWith(PRODUCT);
        }

        static /* synthetic */ boolean lambda$getValidatedPaths$1(String s) {
            try {
                StructStatVfs vfs = Os.statvfs(s);
                String access$000 = TelephonyComponentFactory.TAG;
                Rlog.e(access$000, "StructStatVfs: " + vfs.f_flag + " ," + OsConstants.ST_RDONLY);
                if ((vfs.f_flag & ((long) OsConstants.ST_RDONLY)) != 0) {
                    return true;
                }
                return false;
            } catch (ErrnoException e) {
                String access$0002 = TelephonyComponentFactory.TAG;
                Rlog.e(access$0002, "Injection jar is not protected , path: " + s + e.getMessage());
                return false;
            }
        }

        /* access modifiers changed from: private */
        public void makeInjectedInstance() {
            String validatedPaths = getValidatedPaths();
            String access$000 = TelephonyComponentFactory.TAG;
            Rlog.d(access$000, "validated paths: " + validatedPaths);
            if (!TextUtils.isEmpty(validatedPaths)) {
                try {
                    this.mInjectedInstance = (TelephonyComponentFactory) new PathClassLoader(validatedPaths, ClassLoader.getSystemClassLoader()).loadClass(this.mPackageName).newInstance();
                } catch (ClassNotFoundException e) {
                    String access$0002 = TelephonyComponentFactory.TAG;
                    Rlog.e(access$0002, "failed: " + e.getMessage());
                } catch (IllegalAccessException | InstantiationException e2) {
                    String access$0003 = TelephonyComponentFactory.TAG;
                    Rlog.e(access$0003, "injection failed: " + e2.getMessage());
                }
            }
        }

        /* access modifiers changed from: private */
        public boolean isComponentInjected(String componentName) {
            if (this.mInjectedInstance == null) {
                return false;
            }
            return this.mComponentNames.contains(componentName);
        }

        /* access modifiers changed from: private */
        public void parseXml(XmlPullParser parser) {
            parseXmlByTag(parser, false, new Consumer() {
                /* class com.android.internal.telephony.$$Lambda$TelephonyComponentFactory$InjectedComponents$nLdppNQT1Bv7QyIU3LwAwVD2K60 */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    TelephonyComponentFactory.InjectedComponents.this.lambda$parseXml$2$TelephonyComponentFactory$InjectedComponents((XmlPullParser) obj);
                }
            }, TAG_INJECTION);
        }

        public /* synthetic */ void lambda$parseXml$2$TelephonyComponentFactory$InjectedComponents(XmlPullParser p) {
            setAttributes(p);
            parseInjection(p);
        }

        private void parseInjection(XmlPullParser parser) {
            parseXmlByTag(parser, false, new Consumer() {
                /* class com.android.internal.telephony.$$Lambda$TelephonyComponentFactory$InjectedComponents$eUdIxJOKoyVP5UmFJtWXBUO93Qk */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    TelephonyComponentFactory.InjectedComponents.this.lambda$parseInjection$3$TelephonyComponentFactory$InjectedComponents((XmlPullParser) obj);
                }
            }, TAG_COMPONENTS);
        }

        /* access modifiers changed from: private */
        /* renamed from: parseComponents */
        public void lambda$parseInjection$3$TelephonyComponentFactory$InjectedComponents(XmlPullParser parser) {
            parseXmlByTag(parser, true, new Consumer() {
                /* class com.android.internal.telephony.$$Lambda$TelephonyComponentFactory$InjectedComponents$DKjB_mCxFOHomOyKLPFU99Dywc */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    TelephonyComponentFactory.InjectedComponents.this.lambda$parseComponents$4$TelephonyComponentFactory$InjectedComponents((XmlPullParser) obj);
                }
            }, TAG_COMPONENT);
        }

        /* access modifiers changed from: private */
        /* renamed from: parseComponent */
        public void lambda$parseComponents$4$TelephonyComponentFactory$InjectedComponents(XmlPullParser parser) {
            try {
                int outerDepth = parser.getDepth();
                while (true) {
                    int type = parser.next();
                    if (type == 1) {
                        return;
                    }
                    if (type != 3 || parser.getDepth() > outerDepth) {
                        String access$000 = TelephonyComponentFactory.TAG;
                        Rlog.i(access$000, "parseComponent: type " + type);
                        if (type == 4) {
                            this.mComponentNames.add(parser.getText());
                            String access$0002 = TelephonyComponentFactory.TAG;
                            Rlog.i(access$0002, "parseComponent: text " + parser.getText());
                        }
                    } else {
                        return;
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                Rlog.e(TelephonyComponentFactory.TAG, "Failed to parse the component.", e);
            }
        }

        private void parseXmlByTag(XmlPullParser parser, boolean allowDuplicate, Consumer<XmlPullParser> consumer, String tag) {
            try {
                int outerDepth = parser.getDepth();
                while (true) {
                    int type = parser.next();
                    if (type == 1) {
                        return;
                    }
                    if (type == 3 && parser.getDepth() <= outerDepth) {
                        return;
                    }
                    if (type == 2 && tag.equals(parser.getName())) {
                        consumer.accept(parser);
                        if (!allowDuplicate) {
                            return;
                        }
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                String access$000 = TelephonyComponentFactory.TAG;
                Rlog.e(access$000, "Failed to parse or find tag: " + tag, e);
            }
        }

        private void setAttributes(XmlPullParser parser) {
            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String name = parser.getAttributeName(i);
                String value = parser.getAttributeValue(i);
                if (ATTRIBUTE_PACKAGE.equals(name)) {
                    this.mPackageName = value;
                } else if (ATTRIBUTE_JAR.equals(name)) {
                    this.mJarPath = value;
                }
            }
        }
    }

    public static TelephonyComponentFactory getInstance() {
        if (sInstance == null) {
            sInstance = new TelephonyComponentFactory();
        }
        return sInstance;
    }

    public void injectTheComponentFactory(XmlResourceParser parser) {
        if (this.mInjectedComponents != null) {
            Rlog.d(TAG, "Already injected.");
        } else if (parser != null) {
            this.mInjectedComponents = new InjectedComponents();
            this.mInjectedComponents.parseXml(parser);
            this.mInjectedComponents.makeInjectedInstance();
            boolean injectSuccessful = !TextUtils.isEmpty(this.mInjectedComponents.getValidatedPaths());
            String str = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Total components injected: ");
            sb.append(injectSuccessful ? this.mInjectedComponents.mComponentNames.size() : 0);
            Rlog.i(str, sb.toString());
        }
    }

    public TelephonyComponentFactory inject(String componentName) {
        InjectedComponents injectedComponents = this.mInjectedComponents;
        if (injectedComponents == null || !injectedComponents.isComponentInjected(componentName)) {
            return sInstance;
        }
        return this.mInjectedComponents.mInjectedInstance;
    }

    public GsmCdmaCallTracker makeGsmCdmaCallTracker(GsmCdmaPhone phone) {
        return new GsmCdmaCallTracker(phone);
    }

    public CallManager makeCallManager() {
        return new CallManager();
    }

    public SmsStorageMonitor makeSmsStorageMonitor(Phone phone) {
        return new SmsStorageMonitor(phone);
    }

    public SmsUsageMonitor makeSmsUsageMonitor(Context context) {
        return new SmsUsageMonitor(context);
    }

    public ServiceStateTracker makeServiceStateTracker(GsmCdmaPhone phone, CommandsInterface ci) {
        return new ServiceStateTracker(phone, ci);
    }

    public EmergencyNumberTracker makeEmergencyNumberTracker(Phone phone, CommandsInterface ci) {
        return new EmergencyNumberTracker(phone, ci);
    }

    public NitzStateMachine makeNitzStateMachine(GsmCdmaPhone phone) {
        return new NewNitzStateMachine(phone);
    }

    public SimActivationTracker makeSimActivationTracker(Phone phone) {
        return new SimActivationTracker(phone);
    }

    public DcTracker makeDcTracker(Phone phone, int transportType) {
        return new DcTracker(phone, transportType);
    }

    public CarrierSignalAgent makeCarrierSignalAgent(Phone phone) {
        return new CarrierSignalAgent(phone);
    }

    public CarrierActionAgent makeCarrierActionAgent(Phone phone) {
        return new CarrierActionAgent(phone);
    }

    public CarrierResolver makeCarrierResolver(Phone phone) {
        return new CarrierResolver(phone);
    }

    public IccPhoneBookInterfaceManager makeIccPhoneBookInterfaceManager(Phone phone) {
        return new IccPhoneBookInterfaceManager(phone);
    }

    public IccSmsInterfaceManager makeIccSmsInterfaceManager(Phone phone) {
        return new IccSmsInterfaceManager(phone);
    }

    public SubscriptionController makeSubscriptionController(Phone phone) {
        return new SubscriptionController(phone);
    }

    public SubscriptionController makeSubscriptionController(Context c, CommandsInterface[] ci) {
        return new SubscriptionController(c);
    }

    public SubscriptionInfoUpdater makeSubscriptionInfoUpdater(Looper looper, Context context, Phone[] phone, CommandsInterface[] ci) {
        return new SubscriptionInfoUpdater(looper, context, phone, ci);
    }

    public MultiSimSettingController makeMultiSimSettingController(Context context, SubscriptionController sc) {
        return new MultiSimSettingController(context, sc);
    }

    public UiccController makeUiccController(Context c, CommandsInterface[] ci) {
        return new UiccController(c, ci);
    }

    public UiccProfile makeUiccProfile(Context context, CommandsInterface ci, IccCardStatus ics, int phoneId, UiccCard uiccCard, Object lock) {
        return new UiccProfile(context, ci, ics, phoneId, uiccCard, lock);
    }

    public EriManager makeEriManager(Phone phone, int eriFileSource) {
        return new EriManager(phone, eriFileSource);
    }

    public WspTypeDecoder makeWspTypeDecoder(byte[] pdu) {
        return new WspTypeDecoder(pdu);
    }

    public InboundSmsTracker makeInboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, boolean is3gpp2WapPdu, String address, String displayAddr, String messageBody, boolean isClass0) {
        return new InboundSmsTracker(pdu, timestamp, destPort, is3gpp2, is3gpp2WapPdu, address, displayAddr, messageBody, isClass0);
    }

    public InboundSmsTracker makeInboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, String address, String displayAddr, int referenceNumber, int sequenceNumber, int messageCount, boolean is3gpp2WapPdu, String messageBody, boolean isClass0) {
        return new InboundSmsTracker(pdu, timestamp, destPort, is3gpp2, address, displayAddr, referenceNumber, sequenceNumber, messageCount, is3gpp2WapPdu, messageBody, isClass0);
    }

    public InboundSmsTracker makeInboundSmsTracker(Cursor cursor, boolean isCurrentFormat3gpp2) {
        return new InboundSmsTracker(cursor, isCurrentFormat3gpp2);
    }

    public SmsHeader makeSmsHeader() {
        return new SmsHeader();
    }

    public ImsSmsDispatcher makeImsSmsDispatcher(Phone phone, SmsDispatchersController smsDispatchersController) {
        return new ImsSmsDispatcher(phone, smsDispatchersController);
    }

    public CdmaSMSDispatcher makeCdmaSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController) {
        return new CdmaSMSDispatcher(phone, smsDispatchersController);
    }

    public GsmSMSDispatcher makeGsmSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController, GsmInboundSmsHandler gsmInboundSmsHandler) {
        return new GsmSMSDispatcher(phone, smsDispatchersController, gsmInboundSmsHandler);
    }

    public void makeSmsBroadcastUndelivered(Context context, GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler) {
        SmsBroadcastUndelivered.initialize(context, gsmInboundSmsHandler, cdmaInboundSmsHandler);
    }

    public WapPushOverSms makeWapPushOverSms(Context context) {
        return new WapPushOverSms(context);
    }

    public GsmInboundSmsHandler makeGsmInboundSmsHandler(Context context, SmsStorageMonitor storageMonitor, Phone phone) {
        return GsmInboundSmsHandler.makeInboundSmsHandler(context, storageMonitor, phone);
    }

    public GsmCellBroadcastHandler makeGsmCellBroadcastHandler(Context context, Phone phone) {
        return GsmCellBroadcastHandler.makeGsmCellBroadcastHandler(context, phone);
    }

    public SmsDispatchersController makeSmsDispatchersController(Phone phone, SmsStorageMonitor storageMonitor, SmsUsageMonitor usageMonitor) {
        return new SmsDispatchersController(phone, phone.mSmsStorageMonitor, phone.mSmsUsageMonitor);
    }

    public ImsPhoneCallTracker makeImsPhoneCallTracker(ImsPhone imsPhone) {
        return new ImsPhoneCallTracker(imsPhone);
    }

    public ImsExternalCallTracker makeImsExternalCallTracker(ImsPhone imsPhone) {
        return new ImsExternalCallTracker(imsPhone);
    }

    public AppSmsManager makeAppSmsManager(Context context) {
        return new AppSmsManager(context);
    }

    public DeviceStateMonitor makeDeviceStateMonitor(Phone phone) {
        return new DeviceStateMonitor(phone);
    }

    public TransportManager makeTransportManager(Phone phone) {
        return new TransportManager(phone);
    }

    public CdmaSubscriptionSourceManager getCdmaSubscriptionSourceManagerInstance(Context context, CommandsInterface ci, Handler h, int what, Object obj) {
        return CdmaSubscriptionSourceManager.getInstance(context, ci, h, what, obj);
    }

    public CdmaSubscriptionSourceManager makeCdmaSubscriptionSourceManager(Context context, CommandsInterface ci, Handler h, int what, Object obj) {
        return new CdmaSubscriptionSourceManager(context, ci);
    }

    public void initEmbmsAdaptor(Context context, CommandsInterface[] sCommandsInterfaces) {
    }

    public IDeviceIdleController getIDeviceIdleController() {
        return IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
    }

    public void makeSuppServManager(Context context, Phone[] phones) {
    }

    public LocaleTracker makeLocaleTracker(Phone phone, NitzStateMachine nitzStateMachine, Looper looper) {
        return new LocaleTracker(phone, nitzStateMachine, looper);
    }

    public DataEnabledSettings makeDataEnabledSettings(Phone phone) {
        return new DataEnabledSettings(phone);
    }

    public void initRadioManager(Context context, int numPhones, CommandsInterface[] sCommandsInterfaces) {
    }

    public CdmaInboundSmsHandler makeCdmaInboundSmsHandler(Context context, SmsStorageMonitor storageMonitor, Phone phone, CdmaSMSDispatcher smsDispatcher) {
        return new CdmaInboundSmsHandler(context, storageMonitor, phone, smsDispatcher);
    }

    public void makeDataSubSelector(Context context, int numPhones) {
    }

    public TelephonyNetworkFactory makeTelephonyNetworkFactories(SubscriptionMonitor subscriptionMonitor, Looper looper, Phone phone) {
        return new TelephonyNetworkFactory(subscriptionMonitor, looper, phone);
    }

    public PhoneSwitcher makePhoneSwitcher(int maxActivePhones, int numPhones, Context context, SubscriptionController subscriptionController, Looper looper, ITelephonyRegistry tr, CommandsInterface[] cis, Phone[] phones) {
        return new PhoneSwitcher(maxActivePhones, numPhones, context, subscriptionController, looper, tr, cis, phones);
    }

    public void makeSmartDataSwitchAssistant(Context context, Phone[] phones) {
    }

    public DcRequest makeDcRequest(NetworkRequest nr, Context context) {
        return new DcRequest(nr, context);
    }

    public void makeWorldPhoneManager() {
    }

    public ProxyController makeProxyController(Context context, Phone[] phone, UiccController uiccController, CommandsInterface[] ci, PhoneSwitcher ps) {
        return new ProxyController(context, phone, uiccController, ci, ps);
    }

    public GsmCdmaPhone makePhone(Context context, CommandsInterface ci, PhoneNotifier notifier, int phoneId, int precisePhoneType, TelephonyComponentFactory telephonyComponentFactory) {
        return new GsmCdmaPhone(context, ci, notifier, phoneId, precisePhoneType, telephonyComponentFactory);
    }

    public RIL makeRil(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        return new RIL(context, preferredNetworkType, cdmaSubscription, instanceId);
    }

    public DefaultPhoneNotifier makeDefaultPhoneNotifier() {
        Rlog.d(LOG_TAG, "makeDefaultPhoneNotifier aosp");
        return new DefaultPhoneNotifier();
    }

    public void initGwsdService(Context context) {
    }

    public CatService makeCatService(CommandsInterface ci, UiccCardApplication ca, IccRecords ir, Context context, IccFileHandler fh, UiccProfile uiccProfile, int slotId) {
        return new CatService(ci, ca, ir, context, fh, uiccProfile, slotId);
    }

    public RilMessageDecoder makeRilMessageDecoder(Handler caller, IccFileHandler fh, int slotId) {
        return new RilMessageDecoder(caller, fh);
    }

    public CommandParamsFactory makeCommandParamsFactory(RilMessageDecoder caller, IccFileHandler fh) {
        return new CommandParamsFactory(caller, fh);
    }

    public IconLoader makeIconLoader(Looper looper, IccFileHandler fh) {
        return new IconLoader(looper, fh);
    }

    public DcController makeDcController(String name, Phone phone, DcTracker dct, DataServiceManager dataServiceManager, Handler handler) {
        return new DcController(name, phone, dct, dataServiceManager, handler);
    }

    public RetryManager makeRetryManager(Phone phone, String apnType) {
        return new RetryManager(phone, apnType);
    }

    public DataConnection makeDataConnection(Phone phone, String name, int id, DcTracker dct, DataServiceManager dataServiceManager, DcTesterFailBringUpAll failBringUpAll, DcController dcc) {
        return new DataConnection(phone, name, id, dct, dataServiceManager, failBringUpAll, dcc);
    }

    public void makeDcHelper(Context context, Phone[] phones) {
    }

    public ImsPhone makeImsPhone(Context context, PhoneNotifier phoneNotifier, Phone defaultPhone) {
        return new ImsPhone(context, phoneNotifier, defaultPhone);
    }

    public void initCarrierExpress() {
    }

    public void makeNetworkStatusUpdater(Phone[] phones, int numPhones) {
    }
}
