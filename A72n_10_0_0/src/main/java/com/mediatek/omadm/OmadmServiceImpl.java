package com.mediatek.omadm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IHwBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import com.mediatek.common.omadm.IOmadmManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import vendor.mediatek.hardware.omadm.V1_0.IOmadm;
import vendor.mediatek.hardware.omadm.V1_0.IOmadmIndication;

public class OmadmServiceImpl extends IOmadmManager.Stub {
    public static boolean DEBUG = true;
    private static final int HIDL_APN_GET_ID = 1;
    private static final int HIDL_APN_GET_IPVX = 0;
    private static final int HIDL_APN_GET_NAME = 0;
    private static final int HIDL_APN_IS_ENABLE = 2;
    private static final int HIDL_APN_RESTORE_INFO = 1;
    private static final int HIDL_APN_SET_ENABLE = 1;
    private static final int HIDL_APN_SET_IPVX = 0;
    private static final int HIDL_APN_SET_NAME = 0;
    private static final int HIDL_DCMO_LVC_GET = 3;
    private static final int HIDL_DCMO_LVC_SET = 3;
    private static final int HIDL_DCMO_VLT_GET = 2;
    private static final int HIDL_DCMO_VLT_SET = 2;
    private static final int HIDL_DCMO_VWF_GET = 4;
    private static final int HIDL_DCMO_VWF_SET = 4;
    private static final int HIDL_DMV = 23;
    private static final int HIDL_GETGET_EXT = 26;
    private static final int HIDL_GET_DEVICE_ID = 0;
    private static final int HIDL_GET_ICCID = 25;
    private static final int HIDL_GET_LANGUAGE = 24;
    private static final int HIDL_IMS_SMS_FORMAT_GET = 1;
    private static final int HIDL_IMS_SMS_FORMAT_SET = 1;
    private static final int HIDL_IMS_SMS_OVER_IP_ENABLE = 0;
    private static final int HIDL_IMS_SMS_OVER_IP_IS_ENABLED = 0;
    private static final int HIDL_MANUFACTURER = 21;
    private static final int HIDL_MODEL = 22;
    private static final int HIDL_NETWORK_IMS_DOMAIN = 4;
    private static final int HIDL_NETWORK_IMS_SMS_FORMAT_GET = 5;
    private static final int HIDL_NW_APN_GET_ID = 1;
    private static final int HIDL_NW_APN_GET_IP = 3;
    private static final int HIDL_NW_APN_GET_NAME = 2;
    private static final int HIDL_OMADM_ISFACTORY_SET = 5;
    private static final int HIDL_SYSTEM_DATE_GET = 11;
    private static final int HIDL_SYSTEM_DEVICETYPE_GET = 20;
    private static final int HIDL_SYSTEM_FWV_GET = 7;
    private static final int HIDL_SYSTEM_HOSTDEVICE_DATESTAMP_GET = 18;
    private static final int HIDL_SYSTEM_HOSTDEVICE_DEVICEID_GET = 19;
    private static final int HIDL_SYSTEM_HOSTDEVICE_FWV_GET = 16;
    private static final int HIDL_SYSTEM_HOSTDEVICE_HWV_GET = 17;
    private static final int HIDL_SYSTEM_HOSTDEVICE_MANU_GET = 13;
    private static final int HIDL_SYSTEM_HOSTDEVICE_MODEL_GET = 14;
    private static final int HIDL_SYSTEM_HOSTDEVICE_SWV_GET = 15;
    private static final int HIDL_SYSTEM_HWV_GET = 9;
    private static final int HIDL_SYSTEM_OEM_GET = 6;
    private static final int HIDL_SYSTEM_SUPPORT_LRGOBJ_GET = 10;
    private static final int HIDL_SYSTEM_SWV_GET = 8;
    private static final int HIDL_SYSTEM_TIMEUTC_GET = 12;
    private static final int MSG_GET_SMSIP = 1;
    private static final int MSG_SET_SMSIP = 0;
    private static final String TAG = "OmadmServiceImpl";
    private static AtomicInteger trCounter = new AtomicInteger(0);
    private final Object mAdNetLock = new Object();
    private final Context mContext;
    public BroadcastReceiver mEvHandlerReceiver = new BroadcastReceiver() {
        /* class com.mediatek.omadm.OmadmServiceImpl.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            Log.w(OmadmServiceImpl.TAG, "BroadcastReceiver::onReceive()");
            String action = intent.getAction();
            synchronized (this) {
                if (OmadmServiceImpl.this.mOmadmProxy != null) {
                    if (action.equals("android.provider.Telephony.WAP_PUSH_RECEIVED") && OmadmServiceImpl.this.isOmadmWapPush(intent)) {
                        if (OmadmServiceImpl.DEBUG) {
                            Log.d(OmadmServiceImpl.TAG, "WAP PUSH received");
                        }
                        OmadmServiceImpl.this.checkOmadmWapPush(intent);
                        abortBroadcast();
                    } else if (action.equals("android.intent.action.DATA_SMS_RECEIVED")) {
                        if (OmadmServiceImpl.DEBUG) {
                            Log.d(OmadmServiceImpl.TAG, "DATA SMS received");
                        }
                        OmadmServiceImpl.this.checkDataSms(intent);
                    } else if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {
                        if (OmadmServiceImpl.DEBUG) {
                            Log.d(OmadmServiceImpl.TAG, "DATA SMS received");
                        }
                        OmadmServiceImpl.this.checkDataSms(intent);
                    } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                        Log.d(OmadmServiceImpl.TAG, "ACTION_BOOT_COMPLETED received");
                    } else if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                        String state = intent.getStringExtra("ss");
                        Log.d(OmadmServiceImpl.TAG, "SIM_STATE_CHANGED received, state = " + state);
                        if (state.equals("LOADED") && OmadmServiceImpl.this.mFactoyMode == 1) {
                            Log.d(OmadmServiceImpl.TAG, "SIM_STATE_CHANGED Loaded & is factory mode check APN NVsettings");
                            try {
                                OmadmServiceImpl.this.mOmadmProxy.writeInt(OmadmServiceImpl.HIDL_SYSTEM_OEM_GET);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            OmadmServiceImpl.this.mFactoyMode = 0;
                        }
                    }
                }
            }
        }
    };
    private int mFactoyMode = 0;
    private FotaApnSettings mFotaApn = null;
    private FotaNetworkManager mFotaManager = null;
    private final Handler mHandler = new Handler();
    private ImsConfigManager mImsConfigManager = null;
    private final Object mLock = new Object();
    private int mNetId = 0;
    private NetworkDetector mNwDetector = null;
    private OmadmIndication mOmadmIndication = new OmadmIndication();
    private volatile IOmadm mOmadmProxy = null;
    private final AtomicLong mOmadmProxyCookie = new AtomicLong(0);
    private final OmadmProxyDeathRecipient mOmadmProxyDeathRecipient = new OmadmProxyDeathRecipient();

    /* access modifiers changed from: package-private */
    public final class OmadmProxyDeathRecipient implements IHwBinder.DeathRecipient {
        OmadmProxyDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            Log.d("@M_OmadmServiceImpl", "OMADM HIDL serviceDied");
        }
    }

    public class OmadmIndication extends IOmadmIndication.Stub {
        public OmadmIndication() {
        }

        private byte[] arrayListTobyte(ArrayList<Byte> data, int length) {
            byte[] byteList = new byte[length];
            for (int i = 0; i < length; i++) {
                byteList[i] = data.get(i).byteValue();
            }
            Log.d("@M_OmadmServiceImpl", "OMADM HIDL : arrayListTobyte, byteList = " + byteList);
            return byteList;
        }

        @Override // vendor.mediatek.hardware.omadm.V1_0.IOmadmIndication
        public String getStringTypeInfo(int iidex) {
            Log.d("@M_OmadmServiceImpl", "getStringTypeInfo: idex" + iidex);
            if (iidex == 0) {
                return OmadmServiceImpl.this.get_DeviceId();
            }
            if (iidex == 4) {
                return OmadmServiceImpl.this.get_ImsDomain();
            }
            switch (iidex) {
                case OmadmServiceImpl.HIDL_SYSTEM_OEM_GET /* 6 */:
                    try {
                        return OmadmServiceImpl.this.get_DeviceOEM();
                    } catch (Exception e) {
                        Log.e(OmadmServiceImpl.TAG, "Unexpected Exception ", e);
                        return PalConstDefs.EMPTY_STRING;
                    }
                case 7:
                    return OmadmServiceImpl.this.get_FWV();
                case 8:
                    return OmadmServiceImpl.this.get_SWV();
                case OmadmServiceImpl.HIDL_SYSTEM_HWV_GET /* 9 */:
                    return OmadmServiceImpl.this.get_HWV();
                case 10:
                    return OmadmServiceImpl.this.support_lrgobj_get();
                case 11:
                    return OmadmServiceImpl.this.get_Date();
                case 12:
                    return OmadmServiceImpl.this.get_Time();
                case 13:
                    try {
                        return OmadmServiceImpl.this.get_HostDeviceManu();
                    } catch (Exception e2) {
                        Log.e(OmadmServiceImpl.TAG, "Unexpected Exception ", e2);
                        return PalConstDefs.EMPTY_STRING;
                    }
                case OmadmServiceImpl.HIDL_SYSTEM_HOSTDEVICE_MODEL_GET /* 14 */:
                    try {
                        return OmadmServiceImpl.this.get_HostDeviceModel();
                    } catch (Exception e3) {
                        Log.e(OmadmServiceImpl.TAG, "Unexpected Exception ", e3);
                        return PalConstDefs.EMPTY_STRING;
                    }
                case OmadmServiceImpl.HIDL_SYSTEM_HOSTDEVICE_SWV_GET /* 15 */:
                    return OmadmServiceImpl.this.get_HostSWV();
                case OmadmServiceImpl.HIDL_SYSTEM_HOSTDEVICE_FWV_GET /* 16 */:
                    return OmadmServiceImpl.this.get_HostFWV();
                case OmadmServiceImpl.HIDL_SYSTEM_HOSTDEVICE_HWV_GET /* 17 */:
                    return OmadmServiceImpl.this.get_HostHWV();
                case OmadmServiceImpl.HIDL_SYSTEM_HOSTDEVICE_DATESTAMP_GET /* 18 */:
                    return OmadmServiceImpl.this.get_HostDateStamp();
                case OmadmServiceImpl.HIDL_SYSTEM_HOSTDEVICE_DEVICEID_GET /* 19 */:
                    try {
                        return OmadmServiceImpl.this.get_HostID();
                    } catch (Exception e4) {
                        Log.e(OmadmServiceImpl.TAG, "Unexpected Exception ", e4);
                        return PalConstDefs.EMPTY_STRING;
                    }
                case OmadmServiceImpl.HIDL_SYSTEM_DEVICETYPE_GET /* 20 */:
                    try {
                        return OmadmServiceImpl.this.get_Type();
                    } catch (Exception e5) {
                        Log.e(OmadmServiceImpl.TAG, "Unexpected Exception ", e5);
                        return PalConstDefs.EMPTY_STRING;
                    }
                case OmadmServiceImpl.HIDL_MANUFACTURER /* 21 */:
                    try {
                        return OmadmServiceImpl.this.get_Manufacturer();
                    } catch (RemoteException e6) {
                        Log.e(OmadmServiceImpl.TAG, "Unexpected Exception ", e6);
                        return PalConstDefs.EMPTY_STRING;
                    }
                case OmadmServiceImpl.HIDL_MODEL /* 22 */:
                    return OmadmServiceImpl.this.get_Model();
                case OmadmServiceImpl.HIDL_DMV /* 23 */:
                    return OmadmServiceImpl.this.get_Dmversion();
                case OmadmServiceImpl.HIDL_GET_LANGUAGE /* 24 */:
                    return OmadmServiceImpl.this.get_Language();
                case OmadmServiceImpl.HIDL_GET_ICCID /* 25 */:
                    return OmadmServiceImpl.this.get_Iccid();
                case OmadmServiceImpl.HIDL_GETGET_EXT /* 26 */:
                    return OmadmServiceImpl.this.get_Ext();
                default:
                    Log.e(OmadmServiceImpl.TAG, "Fault getStringTypeInfo iidex");
                    return PalConstDefs.EMPTY_STRING;
            }
        }

        @Override // vendor.mediatek.hardware.omadm.V1_0.IOmadmIndication
        public int getIntTypeInfo(int iidex) {
            Log.d("@M_OmadmServiceImpl", "getIntTypeInfo: idex" + iidex);
            if (iidex == 0) {
                return OmadmServiceImpl.this.is_ImsSmsOverIpEnabled();
            }
            if (iidex == 1) {
                return OmadmServiceImpl.this.get_ImsSmsFormat();
            }
            if (iidex == 2) {
                return OmadmServiceImpl.this.get_ImsVlt();
            }
            if (iidex == 3) {
                return OmadmServiceImpl.this.get_ImsLvcState();
            }
            if (iidex == 4) {
                return OmadmServiceImpl.this.get_ImsVwfState();
            }
            try {
                Log.e(OmadmServiceImpl.TAG, "Fault getIntTypeInfo iidex");
                return 11;
            } catch (Exception e) {
                Log.e(OmadmServiceImpl.TAG, "Unexpected Exception ", e);
                return 11;
            }
        }

        @Override // vendor.mediatek.hardware.omadm.V1_0.IOmadmIndication
        public int setIntTypeInfo(int iidex, int i_info) {
            Log.d("@M_OmadmServiceImpl", "setIntTypeInfo: idex" + iidex + "info: " + i_info);
            if (iidex == 0) {
                return OmadmServiceImpl.this.enable_ImsSmsOverIp(i_info);
            }
            if (iidex == 1) {
                return OmadmServiceImpl.this.set_ImsSmsFormat(i_info);
            }
            if (iidex == 2) {
                return OmadmServiceImpl.this.set_ImsVlt(i_info);
            }
            if (iidex == 3) {
                return OmadmServiceImpl.this.set_ImsLvcState(i_info);
            }
            if (iidex == 4) {
                return OmadmServiceImpl.this.set_ImsVwfState(i_info);
            }
            if (iidex == 5) {
                return OmadmServiceImpl.this.set_IsFactory(i_info);
            }
            try {
                Log.e(OmadmServiceImpl.TAG, "Fault setIntTypeInfo iidex");
            } catch (Exception e) {
                Log.e(OmadmServiceImpl.TAG, "Unexpected Exception ", e);
            }
            return 1;
        }

        @Override // vendor.mediatek.hardware.omadm.V1_0.IOmadmIndication
        public String getApnStringTypeInfo(int iidex, int apncls) {
            Log.d("@M_OmadmServiceImpl", "getApnStringTypeInfo: idex" + iidex + "class: " + apncls);
            if (iidex == 0) {
                return OmadmServiceImpl.this.get_ApnName(apncls);
            }
            Log.e(OmadmServiceImpl.TAG, "Fault getApnStringTypeInfo iidex");
            return PalConstDefs.EMPTY_STRING;
        }

        @Override // vendor.mediatek.hardware.omadm.V1_0.IOmadmIndication
        public int setApnStringTypeInfo(int iidex, int apncls, String s_info) {
            Log.d("@M_OmadmServiceImpl", "setApnStringTypeInfo: idex" + iidex + "class: " + apncls + "info: " + s_info);
            if (iidex == 0) {
                try {
                    return OmadmServiceImpl.this.set_ApnName(apncls, s_info);
                } catch (Exception e) {
                    Log.e(OmadmServiceImpl.TAG, "Unexpected Exception ", e);
                }
            } else if (iidex != 1) {
                Log.e(OmadmServiceImpl.TAG, "Fault setApnStringTypeInfo iidex");
            } else {
                OmadmServiceImpl.this.checkApnRestore(apncls, s_info);
            }
            return 1;
        }

        @Override // vendor.mediatek.hardware.omadm.V1_0.IOmadmIndication
        public int getApnIntTypeInfo(int iidex, int apncls) {
            Log.d("@M_OmadmServiceImpl", "getApnIntTypeInfo: idex" + iidex + "class: " + apncls);
            if (iidex == 0) {
                return OmadmServiceImpl.this.get_ApnIpvX(apncls);
            }
            if (iidex == 1) {
                return OmadmServiceImpl.this.get_ApnId(apncls);
            }
            if (iidex == 2) {
                return OmadmServiceImpl.this.is_ApnEnabled(apncls);
            }
            Log.e(OmadmServiceImpl.TAG, "Fault getApnIntTypeInfo iidex");
            return 1;
        }

        @Override // vendor.mediatek.hardware.omadm.V1_0.IOmadmIndication
        public int setApnIntTypeInfo(int iidex, int apncls, int i_info) {
            Log.d("@M_OmadmServiceImpl", "setApnIntTypeInfo: idex" + iidex + "class: " + apncls + "info: " + i_info);
            if (iidex == 0) {
                return OmadmServiceImpl.this.set_ApnIpvX(apncls, i_info);
            }
            if (iidex == 1) {
                return OmadmServiceImpl.this.enable_Apn(apncls, i_info);
            }
            try {
                Log.e(OmadmServiceImpl.TAG, "Fault setApnIntTypeInfo iidex");
            } catch (Exception e) {
                Log.e(OmadmServiceImpl.TAG, "Unexpected Exception ", e);
            }
            return 1;
        }

        @Override // vendor.mediatek.hardware.omadm.V1_0.IOmadmIndication
        public int requestAdminNetwork(boolean enable) {
            Log.d("@M_OmadmServiceImpl", "requestAdminNetwork : enable = " + enable);
            try {
                return OmadmServiceImpl.this.request_AdminNetwork(enable);
            } catch (RemoteException e) {
                Log.e(OmadmServiceImpl.TAG, "Unexpected Exception ", e);
                return 0;
            }
        }
    }

    public IOmadm getOmadmProxy() {
        if (this.mOmadmProxy != null) {
            return this.mOmadmProxy;
        }
        try {
            Log.d("@M_OmadmServiceImpl", "IOmadm.getService");
            this.mOmadmProxy = IOmadm.getService("omadm");
            if (this.mOmadmProxy != null) {
                this.mOmadmProxy.linkToDeath(this.mOmadmProxyDeathRecipient, this.mOmadmProxyCookie.incrementAndGet());
                Log.d("@M_OmadmServiceImpl", "setResponseFunctions");
                this.mOmadmProxy.setResponseFunctions(this.mOmadmIndication);
                setEventhndlIntentListeners();
                regNwkEventReceiver();
            } else {
                Log.d("@M_OmadmServiceImpl", "getOmadmProxy: mImsaProxy == null");
            }
        } catch (RemoteException | RuntimeException e) {
            this.mOmadmProxy = null;
            Log.d("@M_OmadmServiceImpl", "mOmadmProxy getService/setResponseFunctions: " + e);
        }
        return this.mOmadmProxy;
    }

    public OmadmServiceImpl(Context context) {
        Log.d(TAG, "OmadmServiceImpl() ... constructor");
        this.mContext = context;
        this.mImsConfigManager = new ImsConfigManager(context);
        getOmadmProxy();
    }

    private void regNwkEventReceiver() {
        unregNwkEventReceiver();
        this.mNwDetector = new NetworkDetector(this.mContext, this);
        NetworkDetector networkDetector = this.mNwDetector;
        if (networkDetector != null) {
            networkDetector.register(this.mContext);
        } else {
            Log.e(TAG, "regNwkEventReceiver() cannot allocate NetworkDetector object");
        }
    }

    private void unregNwkEventReceiver() {
        NetworkDetector networkDetector = this.mNwDetector;
        if (networkDetector != null) {
            networkDetector.unregister(this.mContext);
            this.mNwDetector = null;
        }
    }

    private void setEventhndlIntentListeners() {
        Log.w(TAG, "setEventhndlIntentListeners");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.WAP_PUSH_RECEIVED");
        try {
            intentFilter.addDataType("application/vnd.syncml.notification");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Log.w(TAG, "Malformed SUPL init mime type");
        }
        this.mContext.registerReceiver(this.mEvHandlerReceiver, intentFilter, null, this.mHandler);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.DATA_SMS_RECEIVED");
        intentFilter2.addDataScheme("sms");
        intentFilter2.addDataAuthority("localhost", "0");
        this.mContext.registerReceiver(this.mEvHandlerReceiver, intentFilter2, null, this.mHandler);
        IntentFilter intentFilter3 = new IntentFilter();
        intentFilter3.addAction("android.provider.Telephony.SMS_RECEIVED");
        this.mContext.registerReceiver(this.mEvHandlerReceiver, intentFilter3, null, this.mHandler);
        IntentFilter intentFilter4 = new IntentFilter();
        intentFilter4.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mEvHandlerReceiver, intentFilter4, null, this.mHandler);
        IntentFilter intentFilter5 = new IntentFilter();
        intentFilter5.addAction("android.intent.action.SIM_STATE_CHANGED");
        this.mContext.registerReceiver(this.mEvHandlerReceiver, intentFilter5, null, this.mHandler);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isOmadmWapPush(Intent intent) {
        if (DEBUG) {
            Slog.d(TAG, "isOmadmPush()");
        }
        return intent.getType().equals("application/vnd.syncml.notification");
    }

    public void checkDataSms(Intent intent) {
        if (DEBUG) {
            Log.d(TAG, "checkDataSms()");
        }
        try {
            SmsMessage[] sms = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            for (SmsMessage msg : sms) {
                if (DEBUG) {
                    Log.d(TAG, "message body " + msg.getMessageBody());
                }
                this.mOmadmProxy.SmsCancelSysUpdate(msg.getMessageBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Byte> byteToArrayList(int length, byte[] value) {
        ArrayList<Byte> myarraylist = new ArrayList<>();
        Log.d("@M_OmadmServiceImpl", " byteToArrayList, value.length = " + value.length + ", value = " + value + ", length = " + length);
        for (int i = 0; i < length; i++) {
            myarraylist.add(Byte.valueOf(value[i]));
        }
        return myarraylist;
    }

    public void checkOmadmWapPush(Intent intent) {
        if (DEBUG) {
            Log.d(TAG, "checkOmadmWapPush()");
        }
        try {
            byte[] data = (byte[]) intent.getExtra("data");
            int cnt = data.length;
            for (int i = 0; i < cnt; i++) {
                Log.d(TAG, "checkOmadmWapPush: get content[" + i + "]=" + (data[i] & 255));
            }
            Log.d(TAG, "checkOmadmWapPush(): get WAP data len = " + cnt);
            this.mOmadmProxy.writeEvent(((Integer) intent.getExtra("transactionId")).intValue(), cnt, byteToArrayList(cnt, data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String get_DeviceId() {
        String str = ((TelephonyManager) this.mContext.getSystemService("phone")).getDeviceId();
        if (str != null) {
            return str;
        }
        Log.w(TAG, "TelephonyManageris not ready");
        return PalConstDefs.EMPTY_STRING;
    }

    public String get_Manufacturer() throws RemoteException {
        return Build.MANUFACTURER.substring(0, 1).toUpperCase() + Build.MANUFACTURER.substring(1);
    }

    public String get_Model() {
        String str = System.getProperty("ro.vendor.product.model", "k71v1_64_bsp");
        Log.d(TAG, "Model prop =  " + System.getProperty("ro.vendor.product.model", PalConstDefs.EMPTY_STRING) + "get str = " + str);
        return str;
    }

    public String get_Dmversion() {
        return PalConstDefs.VERSION;
    }

    public String get_Language() {
        return Locale.getDefault().getDisplayLanguage();
    }

    public String get_Iccid() {
        TelephonyManager mgr = (TelephonyManager) this.mContext.getSystemService("phone");
        if (mgr == null) {
            Log.w(TAG, "TelephonyManageris not ready");
            return PalConstDefs.NULL_STRING;
        } else if (mgr.getSimState() == 5) {
            return mgr.getSimSerialNumber();
        } else {
            Log.w(TAG, "Sim stateis not ready");
            return PalConstDefs.NULL_STRING;
        }
    }

    public String get_Ext() {
        return "./DevInfo/Ext/ConfigurationVer";
    }

    public String get_DeviceOEM() {
        String man = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model == null && man == null) {
            Log.w(TAG, "manufactureris not identified");
            throw new IllegalStateException("manufactureris not identified");
        } else if (man == null) {
            Log.w(TAG, "manufactureris not identified");
            throw new IllegalStateException("manufactureris not identified");
        } else if (model == null || !model.startsWith(man)) {
            return man;
        } else {
            return model;
        }
    }

    public String get_FWV() {
        String ver = PalConstDefs.EMPTY_STRING;
        try {
            ver = readFile(PalConstDefs.FIRMWARE_VER_PATH);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        if (ver == PalConstDefs.EMPTY_STRING) {
            return PalConstDefs.EMPTY_STRING;
        }
        return ver;
    }

    public String get_SWV() {
        String ver = Build.VERSION.RELEASE + " API:" + Build.VERSION.SDK_INT + " build: " + Build.DISPLAY;
        if (ver == PalConstDefs.EMPTY_STRING) {
            return PalConstDefs.NOT_AVAILABLE;
        }
        return ver;
    }

    public String get_HWV() {
        String ver = Build.HARDWARE;
        if (ver == PalConstDefs.EMPTY_STRING) {
            return PalConstDefs.NOT_AVAILABLE;
        }
        return ver;
    }

    public String support_lrgobj_get() {
        return "false";
    }

    public String get_Date() {
        return new SimpleDateFormat("MM:dd:yyyy").format(new Date(Build.TIME));
    }

    public String get_Time() {
        return new SimpleDateFormat("hh:mm").format(new Date(Build.TIME));
    }

    public String get_HostDeviceManu() {
        if (0 == 1) {
            return get_DeviceOEM();
        }
        if (0 == 2) {
            UsbManager mngr = (UsbManager) this.mContext.getSystemService("usb");
            if (mngr.getDeviceList().size() != 0) {
                return ((UsbDevice) mngr.getDeviceList().values().toArray()[0]).getManufacturerName();
            }
            Log.w(TAG, "no devices");
            throw new IllegalStateException(PalConstDefs.OPERATION_NOT_SUPPORTED);
        }
        Log.w(TAG, "Host device operation Operation not supported");
        return PalConstDefs.NOT_AVAILABLE;
    }

    public String get_HostDeviceModel() {
        if (0 == 1) {
            return Build.MODEL;
        }
        if (0 == 2) {
            UsbManager mngr = (UsbManager) this.mContext.getSystemService("usb");
            if (mngr.getDeviceList().size() != 0) {
                return ((UsbDevice) mngr.getDeviceList().values().toArray()[0]).getProductName();
            }
            Log.w(TAG, "no devices");
            throw new IllegalStateException(PalConstDefs.OPERATION_NOT_SUPPORTED);
        }
        Log.w(TAG, "Host device operation Operation not supported");
        return PalConstDefs.NOT_AVAILABLE;
    }

    public String get_HostHWV() {
        if (0 == 1) {
            return get_HWV();
        }
        Log.w(TAG, "Host device operation Operation not supported");
        return PalConstDefs.NOT_AVAILABLE;
    }

    public String get_HostSWV() {
        if (0 == 1) {
            return get_SWV();
        }
        Log.w(TAG, "Host device operation Operation not supported");
        return PalConstDefs.NOT_AVAILABLE;
    }

    public String get_HostFWV() {
        if (0 == 1) {
            return get_FWV();
        }
        Log.w(TAG, "Host device operation Operation not supported");
        return PalConstDefs.NOT_AVAILABLE;
    }

    public String get_HostDateStamp() {
        if (0 == 1) {
            return new SimpleDateFormat("dd:MM:yyyy hh:mm:ss").format(new Date(Build.TIME));
        }
        Log.w(TAG, "Host device operation Operation not supported");
        return PalConstDefs.NOT_AVAILABLE;
    }

    public String get_HostID() {
        if (0 == 1) {
            return get_DeviceId() + " " + Build.SERIAL;
        }
        Log.w(TAG, "Host device operation Operation not supported");
        return PalConstDefs.NOT_AVAILABLE;
    }

    public String get_Type() {
        TelephonyManager mngr = (TelephonyManager) this.mContext.getSystemService("phone");
        if (mngr == null) {
            Log.w(TAG, "TelephonyManageris not ready");
            throw new IllegalStateException("TelephonyManageris not ready");
        } else if (mngr.getPhoneType() == 0) {
            return PalConstDefs.FEATURE_PHONE;
        } else {
            return PalConstDefs.SMART_DEVICE;
        }
    }

    private String readFile(String fileName) throws IOException {
        File file = new File(PalConstDefs.MO_WORK_PATH, fileName);
        if (file.exists()) {
            FileInputStream confVerFos = new FileInputStream(file);
            int dataLength = (int) file.length();
            if (dataLength <= 0) {
                return PalConstDefs.EMPTY_STRING;
            }
            byte[] buffer = new byte[dataLength];
            confVerFos.read(buffer, 0, dataLength);
            return new String(buffer);
        }
        file.createNewFile();
        Log.w(TAG, "read attempt on file [" + fileName + "] failed... create new one");
        return PalConstDefs.EMPTY_STRING;
    }

    private void writeToFile(String fileName, String value) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(PalConstDefs.MO_WORK_PATH + fileName, false);
        fileOutputStream.write(value.getBytes());
        fileOutputStream.close();
    }

    public String get_ApnName(int apnCls) {
        try {
            String apnName = Apns.getName(this.mContext, apnCls);
            if (DEBUG) {
                Slog.d(TAG, "get_ApnName[" + apnCls + "] = " + apnName);
            }
            return apnName;
        } catch (Exception e) {
            Slog.w(TAG, "get_ApnName cannot get valid info", e);
            return PalConstDefs.EMPTY_STRING;
        }
    }

    public int set_ApnName(int apnCls, String apnName) {
        if (DEBUG) {
            Slog.d(TAG, "set_ApnName[" + apnCls + "] = " + apnName);
        }
        int ret = Apns.setName(this.mContext, apnCls, apnName);
        storeApnConfig(apnCls);
        return ret;
    }

    public int get_ApnIpvX(int apnCls) {
        try {
            int mask = Apns.getIpVersions(this.mContext, apnCls);
            if (DEBUG) {
                Slog.d(TAG, "get_ApnIpvX[" + apnCls + "] = " + mask);
            }
            return mask;
        } catch (Exception e) {
            Slog.w(TAG, "get_ApnIpvX cannot get valid info", e);
            return -1;
        }
    }

    public int get_ApnId(int apnCls) {
        int id = Apns.getId(this.mContext, apnCls);
        if (DEBUG) {
            Slog.d(TAG, "get_ApnId[" + apnCls + "] = " + id);
        }
        return id;
    }

    public int set_ApnIpvX(int apnCls, int protocol) {
        int rc = Apns.setIpVersions(this.mContext, apnCls, protocol);
        if (DEBUG) {
            Slog.d(TAG, "set_ApnIpvX[" + apnCls + "] = " + protocol);
        }
        storeApnConfig(apnCls);
        return rc;
    }

    public int is_ApnEnabled(int apnCls) {
        try {
            int enabled = Apns.isEnabled(this.mContext, apnCls);
            if (DEBUG) {
                Slog.d(TAG, "is_ApnEnabled [ " + apnCls + "] = " + enabled);
            }
            return enabled;
        } catch (Exception e) {
            Slog.w(TAG, "is_ApnEnabled cannot get valid info", e);
            return -1;
        }
    }

    public int enable_Apn(int apnCls, int enable) {
        int ret = Apns.enable(this.mContext, apnCls, enable);
        if (DEBUG) {
            Slog.d(TAG, "enable_Apn [ " + apnCls + "] = " + enable);
        }
        storeApnConfig(apnCls);
        return ret;
    }

    public void checkApnRestore(int apnCls, String data) {
        if (data != PalConstDefs.EMPTY_STRING) {
            String[] field = data.split("/");
            Log.d(TAG, "checkApnRestore ... get and resotre config[" + apnCls + "] = " + field[0] + ", " + field[1] + " ," + field[2]);
            set_ApnName(apnCls, field[0]);
            set_ApnIpvX(apnCls, field[1].equals("3") ? 3 : field[1].equals("2") ? 2 : 1);
            enable_Apn(apnCls, !field[2].equals("0"));
        }
    }

    public void storeApnConfig(int idex) {
        String Config = (PalConstDefs.APN_CFG_IDEX + idex) + "/" + get_ApnName(idex) + "/" + get_ApnIpvX(idex) + "/" + is_ApnEnabled(idex) + "|";
        Log.d(TAG, "storeApnConfig: " + Config);
        try {
            byte[] data = Config.getBytes();
            this.mOmadmProxy.writeBytes(byteToArrayList(data.length, data));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public int is_ImsSmsOverIpEnabled() {
        return 1;
    }

    public int enable_ImsSmsOverIp(int enable) {
        this.mImsConfigManager.setProvisionedIntValue(HIDL_SYSTEM_HOSTDEVICE_MODEL_GET, enable);
        return 0;
    }

    public String get_ImsDomain() {
        return PalConstDefs.IMS_DOMAIN;
    }

    public int get_ImsSmsFormat() {
        return this.mImsConfigManager.getProvisionedIntValue(13);
    }

    public int set_ImsSmsFormat(int format) {
        this.mImsConfigManager.setProvisionedIntValue(13, format);
        return 0;
    }

    public void notifyOmadmNetworkManager(int netFeature, boolean enabled) {
        Slog.d(TAG, "notifyOmadmNetworkManager [ " + netFeature + "] = " + enabled);
        synchronized (this.mLock) {
            if (this.mOmadmProxy != null) {
                try {
                    this.mOmadmProxy.omadmNetManagerReply(netFeature, enabled ? 1 : 0);
                } catch (DeadObjectException e) {
                    Log.w(TAG, "Binder died. Remove listener");
                    this.mOmadmProxy = null;
                } catch (RemoteException e2) {
                    Log.e(TAG, "Unexpected Exception ", e2);
                }
            }
        }
    }

    public void omadmControllerDispachAdminNetStatus(int status, int netId) {
        synchronized (this.mLock) {
            if (this.mOmadmProxy != null) {
                try {
                    this.mOmadmProxy.omadmControllerDispachAdminNetStatus(status, netId);
                } catch (DeadObjectException e) {
                    Log.w(TAG, "Binder died. Remove listener");
                    this.mOmadmProxy = null;
                } catch (RemoteException e2) {
                    Log.e(TAG, "Unexpected Exception ", e2);
                }
            }
        }
    }

    public int request_AdminNetwork(boolean enable) throws RemoteException {
        synchronized (this.mAdNetLock) {
            final int subID = SubscriptionManager.getDefaultDataSubscriptionId();
            if (-1 == subID) {
                Log.e(TAG, "Phone is not ready. Sub ID = " + subID);
                return 11;
            }
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0) {
                Log.w(TAG, "Unable to acquire Admin Network if Airplane mode ON");
                omadmControllerDispachAdminNetStatus(2, 0);
            }
            if (this.mFotaManager == null) {
                this.mFotaManager = new FotaNetworkManager(this, this.mContext, subID);
            }
            if (enable) {
                if (this.mOmadmProxy == null) {
                    Log.e(TAG, "No admin network listeners registred");
                    return 10;
                }
                trCounter.incrementAndGet();
                new Thread() {
                    /* class com.mediatek.omadm.OmadmServiceImpl.AnonymousClass2 */

                    public void run() {
                        try {
                            OmadmServiceImpl.this.mNetId = OmadmServiceImpl.this.mFotaManager.acquireNetwork(OmadmServiceImpl.TAG);
                            String apnName = OmadmServiceImpl.this.mFotaManager.getApnName();
                            if (OmadmServiceImpl.DEBUG) {
                                Log.v(OmadmServiceImpl.TAG, "Network ID = " + OmadmServiceImpl.this.mNetId + " APN name = " + apnName);
                            }
                            OmadmServiceImpl.this.mFotaApn = FotaApnSettings.load(OmadmServiceImpl.this.mContext, apnName, subID, OmadmServiceImpl.TAG, true);
                            if (OmadmServiceImpl.DEBUG) {
                                Log.v(OmadmServiceImpl.TAG, "Using " + OmadmServiceImpl.this.mFotaApn.toString());
                            }
                            OmadmServiceImpl.this.omadmControllerDispachAdminNetStatus(1, OmadmServiceImpl.this.mNetId);
                        } catch (FotaException e) {
                            Log.w(OmadmServiceImpl.TAG, "Unable to acquire Admin Network. Timed Out");
                            OmadmServiceImpl.this.omadmControllerDispachAdminNetStatus(2, 0);
                        } catch (Exception e2) {
                            Log.e(OmadmServiceImpl.TAG, "Caught exception when acquiring Admin Network" + e2);
                            OmadmServiceImpl.this.omadmControllerDispachAdminNetStatus(2, 0);
                        } catch (Throwable th) {
                            OmadmServiceImpl.trCounter.decrementAndGet();
                            throw th;
                        }
                        OmadmServiceImpl.trCounter.decrementAndGet();
                    }
                }.start();
            } else if (this.mFotaManager.releaseNetwork(TAG) && trCounter.compareAndSet(0, 0)) {
                this.mFotaManager = null;
            }
            return 0;
        }
    }

    public int get_ImsVlt() {
        if (DEBUG) {
            Slog.d(TAG, "getImsVlt()");
        }
        int value = this.mImsConfigManager.getProvisionedIntValue(10);
        if (DEBUG) {
            Slog.d(TAG, "getImsVlt() = " + value);
        }
        return value;
    }

    public int set_ImsVlt(int value) {
        if (DEBUG) {
            Slog.d(TAG, "setImsVlt(" + value + ")");
        }
        this.mImsConfigManager.setProvisionedIntValue(10, value);
        return 0;
    }

    public int get_ImsLvcState() {
        if (DEBUG) {
            Slog.d(TAG, "getImsLvcState()");
        }
        int value = this.mImsConfigManager.getProvisionedIntValue(11);
        if (DEBUG) {
            Slog.d(TAG, "getImsLvcState() = " + value);
        }
        return value;
    }

    public int set_ImsLvcState(int val) {
        if (DEBUG) {
            Slog.d(TAG, "setImsLvcState(" + val + ")");
        }
        this.mImsConfigManager.setProvisionedIntValue(11, val);
        return 0;
    }

    public int get_ImsVwfState() {
        if (DEBUG) {
            Slog.d(TAG, "getImsVwfState()");
        }
        int value = this.mImsConfigManager.getProvisionedIntValue(27);
        if (DEBUG) {
            Slog.d(TAG, "getImsVwfState() = " + value);
        }
        if (value == 0) {
            return 1;
        }
        return 0;
    }

    public int set_ImsVwfState(int val) {
        int val2;
        if (DEBUG) {
            Slog.d(TAG, "setImsVwfState(" + val + ")");
        }
        if (val != 0) {
            val2 = 0;
        } else {
            val2 = 1;
        }
        this.mImsConfigManager.setProvisionedIntValue(27, val2);
        return 0;
    }

    public int set_IsFactory(int val) {
        if (DEBUG) {
            Slog.d(TAG, "set_IsFactory(" + val + ")");
        }
        this.mFactoyMode = val;
        return 0;
    }

    public ParcelFileDescriptor inputStream(String path) throws RemoteException {
        if (DEBUG) {
            Slog.d(TAG, "inputStream(" + path + ")");
        }
        if (FileUtils.checkPathAllow(path)) {
            try {
                return FileUtils.pipeTo(new FileInputStream(path));
            } catch (FileNotFoundException e) {
                PalConstDefs.throwEcxeption(11);
                return null;
            } catch (IOException e2) {
                PalConstDefs.throwEcxeption(13);
                return null;
            }
        } else {
            Slog.w(TAG, "inputStream(" + path + "): is not allowed");
            throw new SecurityException(PalConstDefs.EMPTY_STRING);
        }
    }
}
