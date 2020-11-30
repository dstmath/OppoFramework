package android.telephony;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telecom.Log;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import com.android.internal.telecom.ITelecomService;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.SmsApplication;
import java.util.List;

public class ColorOSTelephonyManager implements IColorOSTelephony {
    private static final String MTK_DUAL_CARD_FEATURE = "mtk.gemini.support";
    private static final String QCOM_DUAL_CARD_FEATURE = "oppo.qualcomm.gemini.support";
    private static final String QCOM_PLATFORM_FEATURE = "oppo.hw.manufacturer.qualcomm";
    private static final String TAG = "ColorOSTelephonyManager";
    private static boolean isMtkGeminiSupport = false;
    private static boolean isQcomGeminiSupport = false;
    private static String vDescriptor = IColorOSTelephony.COLOR_SINGLE_CARD_DESCRIPTOR;
    private CarrierConfigManager mCarrierConfigManager;
    private Context mContext;
    private boolean mIsDualLteSupported = false;
    private boolean mIsExpVersion = false;
    private TelephonyManager mTelephonyManager;

    public static ColorOSTelephonyManager getDefault(Context context) {
        return new ColorOSTelephonyManager(context);
    }

    public ColorOSTelephonyManager(Context context) {
        this.mContext = context;
        this.mTelephonyManager = TelephonyManager.from(context);
        initRemoteService();
    }

    private void initRemoteService() {
        isQcomGeminiSupport = true;
        isMtkGeminiSupport = false;
        vDescriptor = IColorOSTelephony.COLOR_SINGLE_CARD_DESCRIPTOR;
        this.mCarrierConfigManager = (CarrierConfigManager) this.mContext.getSystemService(Context.CARRIER_CONFIG_SERVICE);
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager != null) {
            this.mIsExpVersion = packageManager.hasSystemFeature("oppo.version.exp");
            this.mIsDualLteSupported = packageManager.hasSystemFeature("oppo.all.client_lte_lte");
        }
    }

    @Deprecated
    public int colorGetQcomActiveSubscriptionsCount() {
        Context context = this.mContext;
        if (context != null) {
            return SubscriptionManager.from(context).getActiveSubscriptionInfoCount();
        }
        return 0;
    }

    public String getSubscriberIdGemini(int slotID) {
        if (isQcomGeminiSupport) {
            int[] subId = SubscriptionManager.getSubId(slotID);
            if (subId == null || subId.length <= 0 || !SubscriptionManager.isValidSubscriptionId(subId[0])) {
                return null;
            }
            return this.mTelephonyManager.getSubscriberId(subId[0]);
        } else if (isMtkGeminiSupport) {
            return null;
        } else {
            return this.mTelephonyManager.getSubscriberId();
        }
    }

    @Deprecated
    public int getCallStateGemini(int slotID) {
        int[] subId;
        if (isQcomGeminiSupport) {
            int[] subId2 = SubscriptionManager.getSubId(slotID);
            if (subId2 == null || subId2.length <= 0) {
                return 0;
            }
            return TelephonyManager.getDefault().getCallState(subId2[0]);
        } else if (!isMtkGeminiSupport && slotID == 0 && (subId = SubscriptionManager.getSubId(slotID)) != null && subId.length > 0) {
            return TelephonyManager.getDefault().getCallState(subId[0]);
        } else {
            return 0;
        }
    }

    public String getVoiceMailNumberGemini(int slotID) {
        if (isQcomGeminiSupport) {
            int[] subId = SubscriptionManager.getSubId(slotID);
            if (subId == null || subId.length <= 0) {
                return null;
            }
            return this.mTelephonyManager.getVoiceMailNumber(subId[0]);
        } else if (isMtkGeminiSupport) {
            return null;
        } else {
            return this.mTelephonyManager.getVoiceMailNumber();
        }
    }

    public String getLine1NumberGemini(int slotID) {
        if (isQcomGeminiSupport) {
            int[] subId = SubscriptionManager.getSubId(slotID);
            if (subId == null || subId.length <= 0) {
                return null;
            }
            return this.mTelephonyManager.getLine1Number(subId[0]);
        } else if (isMtkGeminiSupport) {
            return null;
        } else {
            return this.mTelephonyManager.getLine1Number();
        }
    }

    @Deprecated
    public int getSimStateGemini(int slotID) {
        if (isQcomGeminiSupport) {
            return TelephonyManager.getDefault().getSimState(slotID);
        }
        if (!isMtkGeminiSupport && slotID == 0) {
            return TelephonyManager.getDefault().getSimState();
        }
        return -1;
    }

    public boolean hasIccCardGemini(int slotID) {
        if (isQcomGeminiSupport) {
            return TelephonyManager.getDefault().hasIccCard(slotID);
        }
        if (!isMtkGeminiSupport && slotID == 0) {
            return TelephonyManager.getDefault().hasIccCard();
        }
        return false;
    }

    public int getNetworkTypeGemini(int slotID) {
        if (isQcomGeminiSupport) {
            int[] subId = SubscriptionManager.getSubId(slotID);
            if (subId == null || subId.length <= 0) {
                return 0;
            }
            return this.mTelephonyManager.getNetworkType(subId[0]);
        } else if (isMtkGeminiSupport) {
            return 0;
        } else {
            return this.mTelephonyManager.getNetworkType();
        }
    }

    public boolean isNetworkRoamingGemini(int slotID) {
        if (isQcomGeminiSupport) {
            int[] subId = SubscriptionManager.getSubId(slotID);
            if (subId == null || subId.length <= 0) {
                return false;
            }
            return TelephonyManager.getDefault().isNetworkRoaming(subId[0]);
        } else if (!isMtkGeminiSupport && slotID == 0) {
            return TelephonyManager.getDefault().isNetworkRoaming();
        } else {
            return false;
        }
    }

    public String getDeviceIdGemini(int slotID) {
        if (isQcomGeminiSupport) {
            return this.mTelephonyManager.getDeviceId(slotID);
        }
        if (isMtkGeminiSupport) {
            return null;
        }
        this.mTelephonyManager.getDeviceId();
        return null;
    }

    public void listenGemini(Context context, PhoneStateListener listener, int events, int slotID) {
        TelephonyManager telephonyManager = null;
        int[] subIds = SubscriptionManager.getSubId(slotID);
        if (subIds != null && subIds.length > 0) {
            telephonyManager = new TelephonyManager(context, subIds[0]);
        }
        if (telephonyManager != null) {
            telephonyManager.listen(listener, events);
        } else {
            log("listenGemini ERROR!");
        }
    }

    @Deprecated
    public boolean isSimInsert(int slotID) {
        if (isQcomGeminiSupport) {
            return hasIccCardGemini(slotID);
        }
        if (!isMtkGeminiSupport && slotID == 0) {
            return hasIccCardGemini(slotID);
        }
        return false;
    }

    @Deprecated
    public String colorGetIccCardTypeGemini(int slotID) {
        if (isQcomGeminiSupport) {
            try {
                return getIccCardTypeGemini(slotID);
            } catch (Exception e) {
                return "";
            }
        } else {
            boolean z = isMtkGeminiSupport;
            return "";
        }
    }

    public String getNetworkOperatorGemini(int slotID) {
        if (isQcomGeminiSupport) {
            int[] subId = SubscriptionManager.getSubId(slotID);
            if (subId == null || subId.length <= 0) {
                return "";
            }
            return TelephonyManager.getDefault().getNetworkOperator(subId[0]);
        } else if (!isMtkGeminiSupport && slotID == 0) {
            return TelephonyManager.getDefault().getNetworkOperator();
        } else {
            return "";
        }
    }

    public String getSimOperatorGemini(int slotID) {
        if (isQcomGeminiSupport) {
            int[] subId = SubscriptionManager.getSubId(slotID);
            if (subId == null || subId.length <= 0) {
                return "";
            }
            return TelephonyManager.getDefault().getSimOperator(subId[0]);
        } else if (!isMtkGeminiSupport && slotID == 0) {
            return TelephonyManager.getDefault().getSimOperator();
        } else {
            return "";
        }
    }

    public int getVoiceNetworkTypeGemini(int slotID) {
        if (isQcomGeminiSupport) {
            int[] subId = SubscriptionManager.getSubId(slotID);
            if (subId == null || subId.length <= 0) {
                return 0;
            }
            return this.mTelephonyManager.getVoiceNetworkType(subId[0]);
        } else if (isMtkGeminiSupport) {
            return 0;
        } else {
            return this.mTelephonyManager.getVoiceNetworkType();
        }
    }

    public int getCurrentPhoneTypeGemini(int slotID) {
        if (isQcomGeminiSupport) {
            int[] subId = SubscriptionManager.getSubId(slotID);
            if (subId == null || subId.length <= 0) {
                return 0;
            }
            return TelephonyManager.getDefault().getCurrentPhoneType(subId[0]);
        } else if (!isMtkGeminiSupport && slotID == 0) {
            return TelephonyManager.getDefault().getCurrentPhoneType();
        } else {
            return 0;
        }
    }

    public String getSimSerialNumberGemini(int slotID) {
        if (isQcomGeminiSupport) {
            int[] subId = SubscriptionManager.getSubId(slotID);
            if (subId == null || subId.length <= 0) {
                return "";
            }
            return this.mTelephonyManager.getSimSerialNumber(subId[0]);
        } else if (isMtkGeminiSupport) {
            return "";
        } else {
            return this.mTelephonyManager.getSimSerialNumber();
        }
    }

    private ITelephony getITelephony() {
        return ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
    }

    private ITelecomService getTelecomService() {
        return ITelecomService.Stub.asInterface(ServiceManager.getService(Context.TELECOM_SERVICE));
    }

    @Deprecated
    public boolean endCallGemini(int slotID) {
        try {
            return getTelecomService().endCall(this.mContext.getPackageName());
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    @Deprecated
    public void answerRingingCallGemini(int subscription) {
        try {
            if (isQcomGeminiSupport) {
                getTelecomService().acceptRingingCall(this.mContext.getPackageName());
            } else if (!isMtkGeminiSupport) {
                getTelecomService().acceptRingingCall(this.mContext.getPackageName());
            }
        } catch (RemoteException | NullPointerException e) {
        }
    }

    @Deprecated
    public boolean isRingingGemini(int slotID, String callingPackage) {
        try {
            return this.mTelephonyManager.isRinging();
        } catch (NullPointerException e) {
            return false;
        } catch (Exception e2) {
            return false;
        }
    }

    @Deprecated
    public boolean isIdleGemini(int slotID, String callingPackage) {
        try {
            return this.mTelephonyManager.isIdle();
        } catch (NullPointerException e) {
            return true;
        } catch (Exception e2) {
            return true;
        }
    }

    @Deprecated
    public boolean isOffhookGemini(int slotID, String callingPackage) {
        try {
            return this.mTelephonyManager.isOffhook();
        } catch (NullPointerException e) {
            return false;
        } catch (Exception e2) {
            return false;
        }
    }

    @Deprecated
    public void silenceRingerGemini(int slotID, String callingPackage) {
        try {
            getTelecomService().silenceRinger(callingPackage);
        } catch (RemoteException e) {
            Log.w(TAG, "Error calling ITelecomService#silenceRinger", e);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public boolean showInCallScreenGemini(boolean showDialpad, String callingPackage) {
        try {
            getTelecomService().showInCallScreen(showDialpad, callingPackage);
            return true;
        } catch (RemoteException e) {
            Log.w(TAG, "Error calling ITelecomService#showInCallScreen", e);
            return false;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean supplyPuk(String puk, String pin, int slotID) {
        try {
            if (isQcomGeminiSupport) {
                int[] subId = SubscriptionManager.getSubId(slotID);
                if (subId == null || subId.length <= 0) {
                    return false;
                }
                return getITelephony().supplyPukForSubscriber(subId[0], puk, pin);
            } else if (isMtkGeminiSupport) {
                return false;
            } else {
                return getITelephony().supplyPuk(puk, pin);
            }
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public boolean supplyPin(String pin, int slotID) {
        try {
            if (isQcomGeminiSupport) {
                int[] subId = SubscriptionManager.getSubId(slotID);
                if (subId == null || subId.length <= 0) {
                    return false;
                }
                return getITelephony().supplyPinForSubscriber(subId[0], pin);
            } else if (isMtkGeminiSupport) {
                return false;
            } else {
                return getITelephony().supplyPin(pin);
            }
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public int[] supplyPukReportResult(String puk, String pin, int slotID) {
        try {
            if (isQcomGeminiSupport) {
                int[] subId = SubscriptionManager.getSubId(slotID);
                if (subId != null) {
                    return getITelephony().supplyPukReportResultForSubscriber(subId[0], puk, pin);
                }
                return null;
            } else if (isMtkGeminiSupport) {
                return null;
            } else {
                return getITelephony().supplyPukReportResult(puk, pin);
            }
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public int[] supplyPinReportResult(String pin, int slotID) {
        try {
            if (isQcomGeminiSupport) {
                int[] subId = SubscriptionManager.getSubId(slotID);
                if (subId != null) {
                    return getITelephony().supplyPinReportResultForSubscriber(subId[0], pin);
                }
                return null;
            } else if (isMtkGeminiSupport) {
                return null;
            } else {
                return getITelephony().supplyPinReportResult(pin);
            }
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public boolean colorIsSubActive(int subscription) {
        if (getSimStateGemini(subscription) == 5) {
            return true;
        }
        return false;
    }

    @Deprecated
    public int colorGetActiveSubscriptionsCount(Context mContext2) {
        return SubscriptionManager.from(mContext2).getActiveSubscriptionInfoCount();
    }

    @Deprecated
    public int colorGetDefaultSubscription() {
        return SubscriptionManager.getDefaultSubscriptionId();
    }

    public int colorGetDataSubscription() {
        return SubscriptionManager.getSlotIndex(SubscriptionManager.getDefaultDataSubscriptionId());
    }

    public void colorSetDataSubscription(Context mContext2, int slotId) {
        int[] subId = SubscriptionManager.getSubId(slotId);
        if (subId != null && subId.length > 0) {
            SubscriptionManager.from(mContext2).setDefaultDataSubId(subId[0]);
        }
    }

    private static IBinder getRemoteServiceBinder() {
        IBinder mRemote = ServiceManager.getService("phone");
        if (mRemote != null) {
            return mRemote;
        }
        log("***********************************");
        log("ColorOSTelephonyManager is NULL !!!1");
        log("***********************************");
        return null;
    }

    public int colorGetSimIndicatorState(int subscription) {
        int _result;
        if (!isMtkGeminiSupport) {
            return 0;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subscription);
            mRemote.transact(10005, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (Exception e) {
            log("colorGetSimIndicatorState ERROR !!! " + e);
            _result = -1;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public int colorSetPreferredNetworkType(int slotID, int type) {
        boolean vSetPreNetWorkType = false;
        try {
            int[] subId = SubscriptionManager.getSubId(slotID);
            if (subId != null && subId.length > 0) {
                vSetPreNetWorkType = getITelephony().setPreferredNetworkType(subId[0], type);
            }
        } catch (RemoteException ex) {
            Log.w(TAG, "setPreferredNetworkType RemoteException", ex);
        } catch (NullPointerException ex2) {
            Log.w(TAG, "setPreferredNetworkType NPE", ex2);
        }
        if (vSetPreNetWorkType) {
            return 0;
        }
        return -1;
    }

    public boolean colorSetLine1Number(int subscription, String phoneNumber) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        boolean _result = false;
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return false;
        }
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subscription);
            _data.writeString(phoneNumber);
            mRemote.transact(10009, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = true;
            }
        } catch (Exception e) {
            log("colorSetLine1Number ERROR !!! " + e);
            _result = false;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public boolean colorGetIccLockEnabled(int subscription) {
        boolean _result;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subscription);
            boolean z = false;
            mRemote.transact(10010, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                z = true;
            }
            _result = z;
        } catch (Exception e) {
            log("colorGetIccLockEnabled ERROR !!! " + e);
            _result = false;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public String colorGetScAddressGemini(int subscription, int slotId) {
        String _result;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return null;
        }
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subscription);
            _data.writeInt(slotId);
            mRemote.transact(10015, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readString();
        } catch (Exception e) {
            log("colorGetScAddressGemini ERROR !!! " + e);
            _result = null;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public void colorSetScAddressGemini(int subscription, String scAddr, int simId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return;
        }
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subscription);
            _data.writeString(scAddr);
            _data.writeInt(simId);
            mRemote.transact(10016, _data, _reply, 0);
            _reply.readException();
        } catch (Exception e) {
            log("colorSetScAddressGemini ERROR !!! " + e);
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    public String getIccCardTypeGemini(int subscription) {
        String _result;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return null;
        }
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subscription);
            mRemote.transact(10038, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readString();
        } catch (Exception e) {
            log("colorSetPrioritySubscription ERROR !!! " + e);
            _result = null;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    @Deprecated
    public String colorGetQcomImeiGemini(int subscription) {
        return null;
    }

    @Deprecated
    public String[] colorGetQcomLTECDMAImei(int subscription) {
        String[] _result;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return null;
        }
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subscription);
            mRemote.transact(10042, _data, _reply, 0);
            _reply.readException();
            _result = _reply.createStringArray();
        } catch (Exception e) {
            log("colorGetQcomLTECDMAImei ERROR !!! " + e);
            _result = null;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public boolean colorIsWhiteSIMCard(int subscription) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        boolean _result = false;
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return false;
        }
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subscription);
            mRemote.transact(10047, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = true;
            }
        } catch (Exception e) {
            log("colorIsWhiteSIMCard ERROR !!! " + e);
            _result = false;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    @Deprecated
    public String colorGetMeid(int subscription) {
        return null;
    }

    public boolean isUriFileExist(String vUri) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        boolean _result = false;
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return false;
        }
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeString(vUri);
            mRemote.transact(10049, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = true;
            }
        } catch (Exception e) {
            log("isUriFileExist ERROR !!! " + e);
            _result = false;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public CellLocation getCellLocation(int slotId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        Bundle _result = null;
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return null;
        }
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(slotId);
            mRemote.transact(10051, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readBundle();
        } catch (Exception e) {
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        CellLocation cl = null;
        if (_result != null) {
            if (_result.isEmpty()) {
                return null;
            }
            String vCardType = getIccCardTypeGemini(slotId);
            int vPhoneType = _result.getInt("type", 0);
            log("getCellLocation-->" + vCardType + ", vPhoneType-->" + vPhoneType + ",  slotId-->" + slotId);
            cl = newCellLocationFromBundle(_result, vCardType);
            if (cl != null && cl.isEmpty()) {
                log("getCellLocationTT44 guix");
                CellLocation cl2 = new GsmCellLocation(_result);
                if (!cl2.isEmpty()) {
                    return cl2;
                }
                log("getCellLocationTT33");
                return null;
            }
        }
        return cl;
    }

    private static CellLocation newCellLocationFromBundle(Bundle bundle, String cardType) {
        int phoneType = bundle.getInt("type", 0);
        if ("CSIM".equals(cardType) || "RUIM".equals(cardType) || phoneType == 2) {
            return new CdmaCellLocation(bundle);
        }
        return new GsmCellLocation(bundle);
    }

    @Deprecated
    public static List<SubInfoRecord> colorgetSubInfoUsingSlotId(Context context, int slotId) {
        return null;
    }

    @Deprecated
    public static List<SubInfoRecord> colorgetActiveSubInfoList(Context context) {
        return null;
    }

    @Deprecated
    public static int colorgetActiveSubInfoCount(Context context) {
        return SubscriptionManager.from(context).getActiveSubscriptionInfoCount();
    }

    @Deprecated
    public static int colorgetPhoneId(Context context, int subId) {
        return SubscriptionManager.getPhoneId(subId);
    }

    public static int colorgetSubId(Context context, int slotId) {
        int[] subId = SubscriptionManager.getSubId(slotId);
        if (subId == null || subId.length <= 0) {
            return -1000;
        }
        return subId[0];
    }

    public static int colorgetSlotId(Context context, int subId) {
        return SubscriptionManager.getSlotIndex(subId);
    }

    @Deprecated
    public static int colorgetOnDemandDataSubId(Context context) {
        return -1;
    }

    @Deprecated
    public static int colorgetSubState(Context context, int subId) {
        return -1;
    }

    public static boolean colorisValidPhoneId(Context context, int phoneId) {
        return SubscriptionManager.isValidPhoneId(phoneId);
    }

    public static boolean colorisValidSlotId(Context context, int slotId) {
        return SubscriptionManager.isValidSlotIndex(slotId);
    }

    public static boolean colorisValidSubId(Context context, int subId) {
        return SubscriptionManager.isValidSubscriptionId(subId);
    }

    public static int colorgetDefaultDataPhoneId(Context context) {
        return SubscriptionManager.from(context).getDefaultDataPhoneId();
    }

    public static int colorgetDefaultDataSubId(Context context) {
        return SubscriptionManager.getDefaultDataSubscriptionId();
    }

    public static int colorgetDefaultSmsPhoneId(Context context) {
        return SubscriptionManager.from(context).getDefaultSmsPhoneId();
    }

    public static int colorgetDefaultSmsSubId(Context context) {
        return SubscriptionManager.getDefaultSmsSubscriptionId();
    }

    @Deprecated
    public static int colorgetDefaultSubId(Context context) {
        return SubscriptionManager.getDefaultSubscriptionId();
    }

    public String getNetworkCountryIso(int subId) {
        return TelephonyManager.getDefault().getNetworkCountryIso(subId);
    }

    public boolean handlePinMmiForSubscriber(int subId, String dialString) {
        try {
            return getITelephony().handlePinMmiForSubscriber(subId, dialString);
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public String getIccOperatorNumeric(int subId) {
        String _result;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return null;
        }
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subId);
            mRemote.transact(IColorOSTelephony.COLOR_GET_OPERATOR_NUMERIC, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readString();
        } catch (Exception e) {
            log("getIccOperatorNumeric ERROR !!! " + e);
            _result = null;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public static void setDefaultApplication(String packageName, Context context) {
        SmsApplication.setDefaultApplication(packageName, context);
    }

    @Deprecated
    public String colorGetImei(int slot) {
        return null;
    }

    private static void log(String msg) {
        Log.d(TAG, msg, new Object[0]);
    }

    public boolean colorIsQcomSubActive(int slotId) {
        IBinder remoteServiceBinder = getRemoteServiceBinder();
        boolean _result = false;
        if (remoteServiceBinder == null) {
            log("colorIsQcomSubActive remoteServiceBinder is null, return!");
            return false;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(slotId);
            remoteServiceBinder.transact(IColorOSTelephony.COLOR_IS_SUB_ACTIVE, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = true;
            }
        } catch (Exception e) {
            log("colorIsQcomSubActive ERROR !!! " + e);
            _result = false;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    @Deprecated
    public boolean isRingingGemini(int slotID) {
        Context context = this.mContext;
        if (context != null) {
            return isRingingGemini(slotID, context.getOpPackageName());
        }
        return false;
    }

    @Deprecated
    public void silenceRingerGemini(int slotID) {
        Context context = this.mContext;
        if (context != null) {
            silenceRingerGemini(slotID, context.getOpPackageName());
        }
    }

    public void listenGemini(PhoneStateListener listener, int events, int slotID) {
        Context context = this.mContext;
        if (context != null) {
            listenGemini(context, listener, events, slotID);
        }
    }

    @Deprecated
    public boolean isIdleGemini(int slotId) {
        Context context = this.mContext;
        if (context != null) {
            return isIdleGemini(slotId, context.getOpPackageName());
        }
        return true;
    }

    public boolean isColorHasSoftSimCard() {
        IBinder mRemote = getRemoteServiceBinder();
        boolean _result = false;
        if (mRemote == null) {
            return false;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            mRemote.transact(IColorOSTelephony.COLOR_HAS_SOFT_SIM, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = true;
            }
        } catch (Exception e) {
            log("colorIsImsRegistered ERROR !!! " + e);
            _result = false;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public int colorGetSoftSimCardSlotId() {
        int _result = -1;
        IBinder remoteServiceBinder = getRemoteServiceBinder();
        if (remoteServiceBinder == null) {
            log("getSubState remoteServiceBinder is null, return!");
            return -1;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            remoteServiceBinder.transact(IColorOSTelephony.COLOR_GET_SOFT_SIM_SLOT, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (Exception e) {
            log("getSubState ERROR !!! " + e);
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public void colorSetDataRoamingEnabled(int slotId, boolean enable) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return;
        }
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(slotId);
            _data.writeInt(enable ? 1 : 0);
            mRemote.transact(10053, _data, _reply, 0);
            _reply.readException();
        } catch (Exception e) {
            log("colorSetScAddressGemini ERROR !!! " + e);
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    public void oppoGetEsnChangeFlag(Message result, int slotId) {
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote != null) {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();
            try {
                _data.writeInterfaceToken(vDescriptor);
                _data.writeParcelable(result, 0);
                _data.writeInt(slotId);
                mRemote.transact(10054, _data, _reply, 0);
                _reply.readException();
            } catch (Exception e) {
                log("oppoGetEsnChangeFlag ERROR !!! " + e);
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
            _reply.recycle();
            _data.recycle();
        }
    }

    public boolean colorIsImsRegistered(Context context, int slotId) {
        IBinder mRemote = getRemoteServiceBinder();
        boolean _result = false;
        if (mRemote == null) {
            return false;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(slotId);
            mRemote.transact(10055, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = true;
            }
        } catch (Exception e) {
            log("colorIsImsRegistered ERROR !!! " + e);
            _result = false;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public String colorGetPlmnOverride(String operatorNumic, ServiceState ss) {
        return OppoTelephonyFunction.oppoGetPlmnOverride(this.mContext, operatorNumic, ss);
    }

    public String colorGetOemSpn(int slotId) {
        String _result;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return null;
        }
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(slotId);
            mRemote.transact(10056, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readString();
        } catch (Exception e) {
            log("colorGetOemSpn ERROR !!! " + e);
            _result = null;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public boolean colorMvnoMatches(int phoneId, int family, String mvnoType, String mvnoMatchData) {
        IBinder mRemote = getRemoteServiceBinder();
        boolean _result = false;
        if (mRemote == null) {
            return false;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(phoneId);
            _data.writeInt(family);
            _data.writeString(mvnoType);
            _data.writeString(mvnoMatchData);
            mRemote.transact(10057, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = true;
            }
        } catch (Exception e) {
            log("colorMvnoMatches ERROR !!! " + e);
            _result = false;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public boolean isOppoSingleSimCard() {
        return OppoTelephonyFunction.oppoGetSingleSimCard();
    }

    public boolean colorIsSimLockedEnabled() {
        return OppoTelephonyFunction.colorIsSimLockedEnabled();
    }

    public boolean colorGetSimLockedStatus(int phoneId) {
        return OppoTelephonyFunction.colorGetSimLockedStatus(isQcomGeminiSupport, getSimOperatorGemini(phoneId), getSubscriberIdGemini(phoneId), getSimStateGemini(phoneId));
    }

    public boolean colorIsVolteEnabledByPlatform(Context context, int phoneId) {
        IBinder mRemote = getRemoteServiceBinder();
        boolean _result = false;
        if (mRemote == null) {
            return false;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(phoneId);
            mRemote.transact(10058, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = true;
            }
        } catch (Exception e) {
            log("colorIsImsRegistered ERROR !!! " + e);
            _result = false;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public boolean colorIsVtEnabledByPlatform(Context context, int phoneId) {
        IBinder mRemote = getRemoteServiceBinder();
        boolean _result = false;
        if (mRemote == null) {
            return false;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(phoneId);
            mRemote.transact(10059, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = true;
            }
        } catch (Exception e) {
            log("colorIsImsRegistered ERROR !!! " + e);
            _result = false;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public boolean colorIsWfcEnabledByPlatform(Context context, int phoneId) {
        IBinder mRemote = getRemoteServiceBinder();
        boolean _result = false;
        if (mRemote == null) {
            return false;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(phoneId);
            mRemote.transact(10060, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = true;
            }
        } catch (Exception e) {
            log("colorIsImsRegistered ERROR !!! " + e);
            _result = false;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public void setDualLteEnabled(boolean enable) {
        IBinder remoteServiceBinder = getRemoteServiceBinder();
        if (remoteServiceBinder == null) {
            log("setDualLteEnabled remoteServiceBinder is null, return!");
            return;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(enable ? 1 : 0);
            remoteServiceBinder.transact(10062, _data, _reply, 0);
            _reply.readException();
        } catch (Exception e) {
            log("setDualLteEnabled ERROR !!! " + e);
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    public boolean isDualLteEnabled() {
        IBinder remoteServiceBinder = getRemoteServiceBinder();
        boolean _result = false;
        if (remoteServiceBinder == null) {
            log("isDualLteEnabled remoteServiceBinder is null, return!");
            return false;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            remoteServiceBinder.transact(10063, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = true;
            }
        } catch (Exception e) {
            log("isDualLteEnabled ERROR !!! " + e);
            _result = false;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public boolean isDualLteSupportedByPlatform() {
        if (this.mIsExpVersion) {
            boolean z = true;
            if (CarrierConfigManager.getDefaultConfig().getBoolean(OppoCarrierConfigManager.OPPO_DUAL_LTE_AVAILABLE)) {
                if (!getBooleanCarrierConfig(OppoCarrierConfigManager.OPPO_DUAL_LTE_AVAILABLE, 0) || !getBooleanCarrierConfig(OppoCarrierConfigManager.OPPO_DUAL_LTE_AVAILABLE, 1)) {
                    z = false;
                }
                this.mIsDualLteSupported = z;
            } else {
                if (!getBooleanCarrierConfig(OppoCarrierConfigManager.OPPO_DUAL_LTE_AVAILABLE, 0) && !getBooleanCarrierConfig(OppoCarrierConfigManager.OPPO_DUAL_LTE_AVAILABLE, 1)) {
                    z = false;
                }
                this.mIsDualLteSupported = z;
            }
        }
        log("isDualLteSupportedByPlatform mIsDualLteSupported = " + this.mIsDualLteSupported);
        return this.mIsDualLteSupported;
    }

    public boolean getBooleanCarrierConfig(String key, int phoneId) {
        if (TextUtils.isEmpty(key)) {
            log("getBooleanCarrierConfig return false for key is null!");
            return false;
        }
        int[] subIds = SubscriptionManager.getSubId(phoneId);
        if (subIds == null || subIds.length == 0) {
            return false;
        }
        log("getBooleanCarrierConfig: phoneId=" + phoneId + " subId=" + subIds[0] + " key = " + key);
        PersistableBundle b = null;
        CarrierConfigManager carrierConfigManager = this.mCarrierConfigManager;
        if (carrierConfigManager != null) {
            b = carrierConfigManager.getConfigForSubId(subIds[0]);
        }
        if (b != null) {
            return b.getBoolean(key);
        }
        return CarrierConfigManager.getDefaultConfig().getBoolean(key);
    }

    public static boolean getBooleanCarrierConfig(Context context, String key, int phoneId) {
        if (context == null || TextUtils.isEmpty(key)) {
            log("getBooleanCarrierConfig return false for context is null or key is null!");
            return false;
        }
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService(Context.CARRIER_CONFIG_SERVICE);
        int[] subIds = SubscriptionManager.getSubId(phoneId);
        if (subIds == null || subIds.length == 0) {
            return false;
        }
        log("getBooleanCarrierConfig: phoneId=" + phoneId + " subId=" + subIds[0] + " key = " + key);
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(subIds[0]);
        }
        if (b != null) {
            return b.getBoolean(key);
        }
        return CarrierConfigManager.getDefaultConfig().getBoolean(key);
    }

    public static void activateSubId(int subId) {
        IBinder remoteServiceBinder = getRemoteServiceBinder();
        if (remoteServiceBinder == null) {
            log("activateSubId remoteServiceBinder is null, return!");
            return;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subId);
            remoteServiceBinder.transact(IColorOSTelephony.COLOR_ACTIVATE_SUB, _data, _reply, 0);
            _reply.readException();
        } catch (Exception e) {
            log("activateSubId ERROR !!! " + e);
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    public static void deactivateSubId(int subId) {
        IBinder remoteServiceBinder = getRemoteServiceBinder();
        if (remoteServiceBinder == null) {
            log("deactivateSubId remoteServiceBinder is null, return!");
            return;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subId);
            remoteServiceBinder.transact(IColorOSTelephony.COLOR_DEACTIVATE_SUB, _data, _reply, 0);
            _reply.readException();
        } catch (Exception e) {
            log("deactivateSubId ERROR !!! " + e);
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    public static int getSubState(int subId) {
        int _result = 1;
        IBinder remoteServiceBinder = getRemoteServiceBinder();
        if (remoteServiceBinder == null) {
            log("getSubState remoteServiceBinder is null, return!");
            return 1;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subId);
            remoteServiceBinder.transact(IColorOSTelephony.COLOR_GET_SUB_STATE, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (Exception e) {
            log("getSubState ERROR !!! " + e);
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public void oppoSimlockReq(String data) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return;
        }
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeString(data);
            mRemote.transact(10064, _data, _reply, 0);
            _reply.readException();
        } catch (Exception e) {
            log("oppo simlock request ERROR !!! " + e);
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    public String oppoCommonReq(String data) {
        String _result;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return null;
        }
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeString(data);
            mRemote.transact(10065, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readString();
        } catch (Exception e) {
            log("oppo common request ERROR !!! " + e);
            _result = null;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }
}
