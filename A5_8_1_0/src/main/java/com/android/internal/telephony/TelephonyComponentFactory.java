package com.android.internal.telephony;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.IDeviceIdleController;
import android.os.IDeviceIdleController.Stub;
import android.os.Looper;
import android.os.ServiceManager;
import android.telephony.Rlog;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.cdma.EriManager;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.imsphone.ImsExternalCallTracker;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;
import com.android.internal.telephony.uicc.IccCardProxy;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Constructor;

public class TelephonyComponentFactory {
    protected static String LOG_TAG = "TelephonyComponentFactory";
    private static TelephonyComponentFactory sInstance;

    public static TelephonyComponentFactory getInstance() {
        if (sInstance == null) {
            String fullClsName = "com.qualcomm.qti.internal.telephony.QtiTelephonyComponentFactory";
            PathClassLoader classLoader = new PathClassLoader("/system/framework/qti-telephony-common.jar", ClassLoader.getSystemClassLoader());
            Rlog.d(LOG_TAG, "classLoader = " + classLoader);
            if (fullClsName == null || fullClsName.length() == 0) {
                Rlog.d(LOG_TAG, "no customized TelephonyPlugin available, fallback to default");
                fullClsName = "com.android.internal.telephony.TelephonyComponentFactory";
            }
            try {
                Class<?> cls = Class.forName(fullClsName, false, classLoader);
                Rlog.d(LOG_TAG, "cls = " + cls);
                Constructor custMethod = cls.getConstructor(new Class[0]);
                Rlog.d(LOG_TAG, "constructor method = " + custMethod);
                sInstance = (TelephonyComponentFactory) custMethod.newInstance(new Object[0]);
            } catch (NoClassDefFoundError e) {
                e.printStackTrace();
                Rlog.e(LOG_TAG, "error loading TelephonyComponentFactory");
                sInstance = new TelephonyComponentFactory();
            } catch (Exception e2) {
                e2.printStackTrace();
                Rlog.e(LOG_TAG, "Error loading TelephonyComponentFactory");
                sInstance = new TelephonyComponentFactory();
            }
        }
        return sInstance;
    }

    public GsmCdmaCallTracker makeGsmCdmaCallTracker(GsmCdmaPhone phone) {
        Rlog.d(LOG_TAG, "makeGsmCdmaCallTracker");
        return new GsmCdmaCallTracker(phone);
    }

    public SmsStorageMonitor makeSmsStorageMonitor(Phone phone) {
        Rlog.d(LOG_TAG, "makeSmsStorageMonitor");
        return new SmsStorageMonitor(phone);
    }

    public SmsUsageMonitor makeSmsUsageMonitor(Context context) {
        Rlog.d(LOG_TAG, "makeSmsUsageMonitor");
        return new SmsUsageMonitor(context);
    }

    public ServiceStateTracker makeServiceStateTracker(GsmCdmaPhone phone, CommandsInterface ci) {
        Rlog.d(LOG_TAG, "makeServiceStateTracker");
        return new ServiceStateTracker(phone, ci);
    }

    public SimActivationTracker makeSimActivationTracker(Phone phone) {
        return new SimActivationTracker(phone);
    }

    public DcTracker makeDcTracker(Phone phone) {
        Rlog.d(LOG_TAG, "makeDcTracker");
        return new DcTracker(phone);
    }

    public CarrierSignalAgent makeCarrierSignalAgent(Phone phone) {
        return new CarrierSignalAgent(phone);
    }

    public CarrierActionAgent makeCarrierActionAgent(Phone phone) {
        return new CarrierActionAgent(phone);
    }

    public IccPhoneBookInterfaceManager makeIccPhoneBookInterfaceManager(Phone phone) {
        Rlog.d(LOG_TAG, "makeIccPhoneBookInterfaceManager");
        return new IccPhoneBookInterfaceManager(phone);
    }

    public IccSmsInterfaceManager makeIccSmsInterfaceManager(Phone phone) {
        Rlog.d(LOG_TAG, "makeIccSmsInterfaceManager");
        return new IccSmsInterfaceManager(phone);
    }

    public IccCardProxy makeIccCardProxy(Context context, CommandsInterface ci, int phoneId) {
        Rlog.d(LOG_TAG, "makeIccCardProxy");
        return new IccCardProxy(context, ci, phoneId);
    }

    public EriManager makeEriManager(Phone phone, Context context, int eriFileSource) {
        Rlog.d(LOG_TAG, "makeEriManager");
        return new EriManager(phone, context, eriFileSource);
    }

    public WspTypeDecoder makeWspTypeDecoder(byte[] pdu) {
        Rlog.d(LOG_TAG, "makeWspTypeDecoder");
        return new WspTypeDecoder(pdu);
    }

    public InboundSmsTracker makeInboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, boolean is3gpp2WapPdu, String address, String displayAddr, String messageBody) {
        Rlog.d(LOG_TAG, "makeInboundSmsTracker");
        return new InboundSmsTracker(pdu, timestamp, destPort, is3gpp2, is3gpp2WapPdu, address, displayAddr, messageBody);
    }

    public InboundSmsTracker makeInboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, String address, String displayAddr, int referenceNumber, int sequenceNumber, int messageCount, boolean is3gpp2WapPdu, String messageBody) {
        Rlog.d(LOG_TAG, "makeInboundSmsTracker");
        return new InboundSmsTracker(pdu, timestamp, destPort, is3gpp2, address, displayAddr, referenceNumber, sequenceNumber, messageCount, is3gpp2WapPdu, messageBody);
    }

    public InboundSmsTracker makeInboundSmsTracker(Cursor cursor, boolean isCurrentFormat3gpp2) {
        return new InboundSmsTracker(cursor, isCurrentFormat3gpp2);
    }

    public InboundSmsTracker makeInboundSmsTracker(Cursor cursor, boolean isCurrentFormat3gpp2, boolean isUndeliverMsg) {
        return new InboundSmsTracker(cursor, isCurrentFormat3gpp2, isUndeliverMsg);
    }

    public ImsPhoneCallTracker makeImsPhoneCallTracker(ImsPhone imsPhone) {
        Rlog.d(LOG_TAG, "makeImsPhoneCallTracker");
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

    public CdmaSubscriptionSourceManager getCdmaSubscriptionSourceManagerInstance(Context context, CommandsInterface ci, Handler h, int what, Object obj) {
        Rlog.d(LOG_TAG, "getCdmaSubscriptionSourceManagerInstance");
        return CdmaSubscriptionSourceManager.getInstance(context, ci, h, what, obj);
    }

    public IDeviceIdleController getIDeviceIdleController() {
        Rlog.d(LOG_TAG, "getIDeviceIdleController");
        return Stub.asInterface(ServiceManager.getService("deviceidle"));
    }

    public Phone makePhone(Context context, CommandsInterface ci, PhoneNotifier notifier, int phoneId, int precisePhoneType, TelephonyComponentFactory telephonyComponentFactory) {
        Rlog.d(LOG_TAG, "makePhone");
        if (precisePhoneType == 1) {
            return new GsmCdmaPhone(context, ci, notifier, phoneId, 1, telephonyComponentFactory);
        }
        if (precisePhoneType == 2) {
            return new GsmCdmaPhone(context, ci, notifier, phoneId, 6, telephonyComponentFactory);
        }
        return null;
    }

    public SubscriptionController initSubscriptionController(Context c, CommandsInterface[] ci) {
        Rlog.d(LOG_TAG, "initSubscriptionController");
        return SubscriptionController.init(c, ci);
    }

    public SubscriptionInfoUpdater makeSubscriptionInfoUpdater(Looper looper, Context context, Phone[] phones, CommandsInterface[] ci) {
        Rlog.d(LOG_TAG, "makeSubscriptionInfoUpdater");
        return new SubscriptionInfoUpdater(looper, context, phones, ci);
    }

    public void makeExtTelephonyClasses(Context context, Phone[] phones, CommandsInterface[] commandsInterfaces) {
        Rlog.d(LOG_TAG, "makeExtTelephonyClasses");
    }

    public PhoneSwitcher makePhoneSwitcher(int maxActivePhones, int numPhones, Context context, SubscriptionController subscriptionController, Looper looper, ITelephonyRegistry tr, CommandsInterface[] cis, Phone[] phones) {
        Rlog.d(LOG_TAG, "makePhoneSwitcher");
        return new PhoneSwitcher(maxActivePhones, numPhones, context, subscriptionController, looper, tr, cis, phones);
    }

    public RIL makeRIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        Rlog.d(LOG_TAG, "makeRIL");
        return new RIL(context, preferredNetworkType, cdmaSubscription, instanceId);
    }
}
