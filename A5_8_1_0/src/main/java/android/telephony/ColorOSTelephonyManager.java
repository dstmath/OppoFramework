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
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telecom.ITelecomService;
import com.android.internal.telephony.ISub;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephony.Stub;
import java.util.List;
import org.codeaurora.internal.IExtTelephony;

public class ColorOSTelephonyManager implements IColorOSTelephony {
    private static final String MTK_DUAL_CARD_FEATURE = "mtk.gemini.support";
    private static final String QCOM_DUAL_CARD_FEATURE = "oppo.qualcomm.gemini.support";
    private static final String QCOM_PLATFORM_FEATURE = "oppo.hw.manufacturer.qualcomm";
    private static final String TAG = "ColorOSTelephonyManager";
    private static boolean isMtkGeminiSupport = false;
    private static boolean isQcomGeminiSupport = false;
    private static String vDescriptor = null;
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
        this.mCarrierConfigManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager != null) {
            this.mIsExpVersion = packageManager.hasSystemFeature("oppo.version.exp");
            this.mIsDualLteSupported = packageManager.hasSystemFeature("oppo.all.client_lte_lte");
        }
    }

    public int colorGetQcomActiveSubscriptionsCount() {
        if (this.mContext != null) {
            return SubscriptionManager.from(this.mContext).getActiveSubscriptionInfoCount();
        }
        return 0;
    }

    public String getSubscriberIdGemini(int slotID) {
        if (isQcomGeminiSupport) {
            int[] subId = SubscriptionManager.getSubId(slotID);
            if (subId == null || subId.length <= 0) {
                return null;
            }
            return this.mTelephonyManager.getSubscriberId(subId[0]);
        } else if (isMtkGeminiSupport) {
            return null;
        } else {
            return this.mTelephonyManager.getSubscriberId();
        }
    }

    public int getCallStateGemini(int slotID) {
        int[] subId;
        if (isQcomGeminiSupport) {
            subId = SubscriptionManager.getSubId(slotID);
            if (subId == null || subId.length <= 0) {
                return 0;
            }
            return TelephonyManager.getDefault().getCallState(subId[0]);
        } else if (isMtkGeminiSupport || slotID != 0) {
            return 0;
        } else {
            subId = SubscriptionManager.getSubId(slotID);
            if (subId == null || subId.length <= 0) {
                return 0;
            }
            return TelephonyManager.getDefault().getCallState(subId[0]);
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

    public int getSimStateGemini(int slotID) {
        if (isQcomGeminiSupport) {
            return TelephonyManager.getDefault().getSimState(slotID);
        }
        if (isMtkGeminiSupport || slotID != 0) {
            return -1;
        }
        return TelephonyManager.getDefault().getSimState();
    }

    public boolean hasIccCardGemini(int slotID) {
        if (isQcomGeminiSupport) {
            return TelephonyManager.getDefault().hasIccCard(slotID);
        }
        if (isMtkGeminiSupport || slotID != 0) {
            return false;
        }
        return TelephonyManager.getDefault().hasIccCard();
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
        } else if (isMtkGeminiSupport || slotID != 0) {
            return false;
        } else {
            return TelephonyManager.getDefault().isNetworkRoaming();
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
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        int[] subId;
        if (isQcomGeminiSupport) {
            subId = SubscriptionManager.getSubId(slotID);
            if (subId != null && subId.length > 0) {
                listener.mSubId = Integer.valueOf(subId[0]);
            }
            if (telephonyManager != null) {
                log("listenGemini-Register SubID," + listener.mSubId);
                telephonyManager.listen(listener, events);
                return;
            }
            log("listenGeminiA ERROR");
        } else if (!isMtkGeminiSupport && slotID == 0) {
            subId = SubscriptionManager.getSubId(slotID);
            if (subId != null && subId.length > 0) {
                listener.mSubId = Integer.valueOf(subId[0]);
            }
            if (telephonyManager != null) {
                log("listenGemini-Register");
                telephonyManager.listen(listener, events);
                return;
            }
            log("listenGeminiA ERROR");
        }
    }

    public boolean isSimInsert(int slotID) {
        if (isQcomGeminiSupport) {
            return hasIccCardGemini(slotID);
        }
        if (isMtkGeminiSupport || slotID != 0) {
            return false;
        }
        return hasIccCardGemini(slotID);
    }

    public String getIccCardTypeGemini(int slotID) {
        String vRet = "";
        if (isQcomGeminiSupport) {
            try {
                return colorGetIccCardTypeGemini(slotID);
            } catch (Exception e) {
                return vRet;
            }
        }
        boolean z = isMtkGeminiSupport;
        return vRet;
    }

    public String getNetworkOperatorGemini(int slotID) {
        String vRet = "";
        if (isQcomGeminiSupport) {
            int[] subId = SubscriptionManager.getSubId(slotID);
            if (subId == null || subId.length <= 0) {
                return vRet;
            }
            return TelephonyManager.getDefault().getNetworkOperator(subId[0]);
        } else if (isMtkGeminiSupport || slotID != 0) {
            return vRet;
        } else {
            return TelephonyManager.getDefault().getNetworkOperator();
        }
    }

    public String getSimOperatorGemini(int slotID) {
        String vRet = "";
        if (isQcomGeminiSupport) {
            int[] subId = SubscriptionManager.getSubId(slotID);
            if (subId == null || subId.length <= 0) {
                return vRet;
            }
            return TelephonyManager.getDefault().getSimOperator(subId[0]);
        } else if (isMtkGeminiSupport || slotID != 0) {
            return vRet;
        } else {
            return TelephonyManager.getDefault().getSimOperator();
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
        } else if (isMtkGeminiSupport || slotID != 0) {
            return 0;
        } else {
            return TelephonyManager.getDefault().getCurrentPhoneType();
        }
    }

    public String getSimSerialNumberGemini(int slotID) {
        String vRet = "";
        if (isQcomGeminiSupport) {
            int[] subId = SubscriptionManager.getSubId(slotID);
            if (subId == null || subId.length <= 0) {
                return vRet;
            }
            return this.mTelephonyManager.getSimSerialNumber(subId[0]);
        } else if (isMtkGeminiSupport) {
            return vRet;
        } else {
            return this.mTelephonyManager.getSimSerialNumber();
        }
    }

    private ITelephony getITelephony() {
        return Stub.asInterface(ServiceManager.getService("phone"));
    }

    private IExtTelephony getIExtTelephony() {
        return IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone"));
    }

    private ITelecomService getTelecomService() {
        return ITelecomService.Stub.asInterface(ServiceManager.getService("telecom"));
    }

    public boolean endCallGemini(int slotID) {
        boolean vRet = false;
        try {
            if (isQcomGeminiSupport) {
                int[] subId = SubscriptionManager.getSubId(slotID);
                if (subId != null && subId.length > 0) {
                    vRet = getITelephony().endCallForSubscriber(subId[0]);
                }
                return vRet;
            } else if (isMtkGeminiSupport) {
                return false;
            } else {
                return getITelephony().endCall();
            }
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public void answerRingingCallGemini(int subscription) {
        try {
            if (isQcomGeminiSupport) {
                getTelecomService().acceptRingingCall(this.mContext.getPackageName());
            } else if (!isMtkGeminiSupport) {
                getTelecomService().acceptRingingCall(this.mContext.getPackageName());
            }
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
    }

    public boolean isRingingGemini(int slotID, String callingPackage) {
        boolean vRet = false;
        try {
            int[] subId;
            if (isQcomGeminiSupport) {
                subId = SubscriptionManager.getSubId(slotID);
                if (subId != null && subId.length > 0) {
                    vRet = getITelephony().isRingingForSubscriber(subId[0], callingPackage);
                }
                return vRet;
            } else if (isMtkGeminiSupport) {
                return false;
            } else {
                if (slotID == 0) {
                    subId = SubscriptionManager.getSubId(slotID);
                    if (subId != null && subId.length > 0) {
                        vRet = getITelephony().isRingingForSubscriber(subId[0], callingPackage);
                    }
                }
                return vRet;
            }
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public boolean isIdleGemini(int slotID, String callingPackage) {
        boolean vRet = true;
        try {
            int[] subId;
            if (isQcomGeminiSupport) {
                subId = SubscriptionManager.getSubId(slotID);
                if (subId != null && subId.length > 0) {
                    vRet = getITelephony().isIdleForSubscriber(subId[0], callingPackage);
                }
                return vRet;
            } else if (isMtkGeminiSupport) {
                return false;
            } else {
                if (slotID == 0) {
                    subId = SubscriptionManager.getSubId(slotID);
                    if (subId != null && subId.length > 0) {
                        vRet = getITelephony().isIdleForSubscriber(subId[0], callingPackage);
                    }
                }
                return vRet;
            }
        } catch (RemoteException e) {
            return true;
        } catch (NullPointerException e2) {
            return true;
        }
    }

    public boolean isOffhookGemini(int slotID, String callingPackage) {
        boolean vRet = false;
        try {
            int[] subId;
            if (isQcomGeminiSupport) {
                subId = SubscriptionManager.getSubId(slotID);
                if (subId != null && subId.length > 0) {
                    vRet = getITelephony().isOffhookForSubscriber(subId[0], callingPackage);
                }
                return vRet;
            } else if (isMtkGeminiSupport) {
                return false;
            } else {
                if (slotID == 0) {
                    subId = SubscriptionManager.getSubId(slotID);
                    if (subId != null && subId.length > 0) {
                        vRet = getITelephony().isOffhookForSubscriber(subId[0], callingPackage);
                    }
                }
                return vRet;
            }
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public void silenceRingerGemini(int slotID, String callingPackage) {
        try {
            getTelecomService().silenceRinger(callingPackage);
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelecomService#silenceRinger", e);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public boolean showInCallScreenGemini(boolean showDialpad, String callingPackage) {
        try {
            getTelecomService().showInCallScreen(showDialpad, callingPackage);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelecomService#showInCallScreen", e);
            return false;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean supplyPuk(String puk, String pin, int slotID) {
        boolean vRet = false;
        try {
            if (isQcomGeminiSupport) {
                int[] subId = SubscriptionManager.getSubId(slotID);
                if (subId != null && subId.length > 0) {
                    vRet = getITelephony().supplyPukForSubscriber(subId[0], puk, pin);
                }
                return vRet;
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
        boolean vRet = false;
        try {
            if (isQcomGeminiSupport) {
                int[] subId = SubscriptionManager.getSubId(slotID);
                if (subId != null && subId.length > 0) {
                    vRet = getITelephony().supplyPinForSubscriber(subId[0], pin);
                }
                return vRet;
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

    public int colorGetActiveSubscriptionsCount(Context mContext) {
        return SubscriptionManager.from(mContext).getActiveSubscriptionInfoCount();
    }

    public int colorGetDefaultSubscription() {
        return SubscriptionManager.getDefaultSubscriptionId();
    }

    public int colorGetDataSubscription() {
        return SubscriptionManager.getSlotIndex(SubscriptionManager.getDefaultDataSubscriptionId());
    }

    public void colorSetDataSubscription(Context mContext, int slotId) {
        int[] subId = SubscriptionManager.getSubId(slotId);
        if (subId != null && subId.length > 0) {
            SubscriptionManager.from(mContext).setDefaultDataSubId(subId[0]);
        }
    }

    private IBinder getRemoteServiceBinder() {
        IBinder mRemote = ServiceManager.getService("phone");
        if (mRemote != null) {
            return mRemote;
        }
        log("***********************************");
        log("ColorOSTelephonyManager is NULL !!!" + 1);
        log("***********************************");
        return null;
    }

    public int colorGetSimIndicatorState(int subscription) {
        if (!isMtkGeminiSupport) {
            return 0;
        }
        int _result;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subscription);
            mRemote.transact(10005, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            log("colorGetSimIndicatorState ERROR !!! " + e);
            _result = -1;
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
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
            Rlog.e(TAG, "setPreferredNetworkType RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "setPreferredNetworkType NPE", ex2);
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
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return false;
        }
        boolean _result;
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subscription);
            _data.writeString(phoneNumber);
            mRemote.transact(IColorOSTelephony.COLOR_SET_LIN1_NUMBER, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt() != 0;
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            log("colorSetLine1Number ERROR !!! " + e);
            _result = false;
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return _result;
    }

    public boolean colorGetIccLockEnabled(int subscription) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subscription);
            mRemote.transact(10010, _data, _reply, 0);
            _reply.readException();
            boolean _result = _reply.readInt() != 0;
            _reply.recycle();
            _data.recycle();
            return _result;
        } catch (Exception e) {
            log("colorGetIccLockEnabled ERROR !!! " + e);
            _reply.recycle();
            _data.recycle();
            return false;
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
    }

    public String colorGetScAddressGemini(int subscription, int slotId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return null;
        }
        String _result;
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subscription);
            _data.writeInt(slotId);
            mRemote.transact(10015, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readString();
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            log("colorGetScAddressGemini ERROR !!! " + e);
            _result = null;
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
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
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public String colorGetIccCardTypeGemini(int subscription) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return null;
        }
        String _result;
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subscription);
            mRemote.transact(IColorOSTelephony.COLOR_GET_ICC_CARD_TYPE, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readString();
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            log("colorSetPrioritySubscription ERROR !!! " + e);
            _result = null;
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return _result;
    }

    public String colorGetQcomImeiGemini(int subscription) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return null;
        }
        String _result;
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subscription);
            mRemote.transact(IColorOSTelephony.COLOR_GET_QCOM_GEMINI, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readString();
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            log("colorGetQcomImeiGemini ERROR !!! " + e);
            _result = null;
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return _result;
    }

    public String[] colorGetQcomLTECDMAImei(int subscription) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return null;
        }
        String[] _result;
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subscription);
            mRemote.transact(IColorOSTelephony.COLOR_GET_QCOM_LTECDMA_IMEI, _data, _reply, 0);
            _reply.readException();
            _result = _reply.createStringArray();
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            log("colorGetQcomLTECDMAImei ERROR !!! " + e);
            _result = null;
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return _result;
    }

    public boolean colorIsWhiteSIMCard(int subscription) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return false;
        }
        boolean _result;
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subscription);
            mRemote.transact(IColorOSTelephony.COLOR_GET_IS_WHITE_SIM_CARD, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt() != 0;
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            log("colorIsWhiteSIMCard ERROR !!! " + e);
            _result = false;
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return _result;
    }

    public String colorGetMeid(int subscription) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return null;
        }
        String _result;
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(subscription);
            mRemote.transact(IColorOSTelephony.COLOR_GET_MEID, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readString();
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            log("colorGetMeid ERROR !!! " + e);
            _result = null;
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return _result;
    }

    public boolean isUriFileExist(String vUri) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return false;
        }
        boolean _result;
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeString(vUri);
            mRemote.transact(IColorOSTelephony.COLOR_IS_URI_EXIST, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt() != 0;
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            log("isUriFileExist ERROR !!! " + e);
            _result = false;
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return _result;
    }

    public CellLocation getCellLocation(int slotId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        Bundle bundle = null;
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return null;
        }
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(slotId);
            mRemote.transact(IColorOSTelephony.COLOR_GET_CELLLOCATION, _data, _reply, 0);
            _reply.readException();
            bundle = _reply.readBundle();
        } catch (Exception e) {
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        CellLocation cl = null;
        if (bundle != null) {
            if (bundle.isEmpty()) {
                return null;
            }
            String vCardType = getIccCardTypeGemini(slotId);
            log("getCellLocation-->" + vCardType + ", vPhoneType-->" + bundle.getInt("type", 0) + ",  slotId-->" + slotId);
            cl = CellLocation.newFromBundle(bundle, vCardType);
            if (cl != null && cl.isEmpty()) {
                log("getCellLocationTT44 guix");
                cl = new GsmCellLocation(bundle);
                if (cl != null && (cl.isEmpty() ^ 1) != 0) {
                    return cl;
                }
                log("getCellLocationTT33");
                return null;
            }
        }
        return cl;
    }

    public boolean isCTCCard(int subscription) {
        return SubscriptionManager.isCTCCard(subscription);
    }

    public static List<SubInfoRecord> colorgetSubInfoUsingSlotId(Context context, int slotId) {
        return SubscriptionManager.from(context).getSubInfoUsingSlotId(slotId);
    }

    public static List<SubInfoRecord> colorgetActiveSubInfoList(Context context) {
        return SubscriptionManager.from(context).getActiveSubInfoList();
    }

    public static int colorgetActiveSubInfoCount(Context context) {
        return SubscriptionManager.from(context).getActiveSubscriptionInfoCount();
    }

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

    public static SubInfoRecord colorgetSubInfoForSubscriber(Context context, int subId) {
        return SubscriptionManager.from(context).getSubInfoForSubscriber(subId);
    }

    public static int colorgetSlotId(Context context, int subId) {
        return SubscriptionManager.getSlotIndex(subId);
    }

    public static int colorgetOnDemandDataSubId(Context context) {
        return -1;
    }

    public static int colorgetSubState(Context context, int subId) {
        SubscriptionManager.from(context);
        return SubscriptionManager.getSubState(subId);
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

    public static void colorsetDefaultDataSubId(Context context, int subId) {
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

    public static boolean colorisSMSPromptEnabled(Context context) {
        SubscriptionManager.from(context);
        return SubscriptionManager.colorisSMSPromptEnabled();
    }

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
        return SubscriptionManager.getOperatorNumericForData(subId);
    }

    public static void setDefaultApplication(String packageName, Context context) {
        SubscriptionManager.from(context).setDefaultApplication(packageName);
    }

    public int colorgetPreferredDataSubscription() {
        return -1;
    }

    public String colorGetImei(int slot) {
        return colorGetQcomImeiGemini(slot);
    }

    private static void log(String msg) {
        Rlog.d(TAG, msg);
    }

    public boolean colorIsQcomSubActive(int slotId) {
        try {
            if (getIExtTelephony().getCurrentUiccCardProvisioningStatus(slotId) == 1) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean isRingingGemini(int slotID) {
        if (this.mContext != null) {
            return isRingingGemini(slotID, this.mContext.getOpPackageName());
        }
        return false;
    }

    public void silenceRingerGemini(int slotID) {
        if (this.mContext != null) {
            silenceRingerGemini(slotID, this.mContext.getOpPackageName());
        }
    }

    public void listenGemini(PhoneStateListener listener, int events, int slotID) {
        if (this.mContext != null) {
            listenGemini(this.mContext, listener, events, slotID);
        }
    }

    public boolean isIdleGemini(int slotId) {
        if (this.mContext != null) {
            return isIdleGemini(slotId, this.mContext.getOpPackageName());
        }
        return true;
    }

    public boolean isColorHasSoftSimCard() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.isHasSoftSimCard();
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public int colorGetSoftSimCardSlotId() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getSoftSimCardSlotId();
            }
        } catch (RemoteException e) {
        }
        return -1;
    }

    public void colorSetDataRoamingEnabled(int slotId, boolean enable) {
        int i = 0;
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
            if (enable) {
                i = 1;
            }
            _data.writeInt(i);
            mRemote.transact(IColorOSTelephony.COLOR_SET_DATA_ROAMING_ENABLED, _data, _reply, 0);
            _reply.readException();
        } catch (Exception e) {
            log("colorSetScAddressGemini ERROR !!! " + e);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public void colorSetSarRfState(int state, int subId) {
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote != null) {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();
            try {
                _data.writeInterfaceToken(vDescriptor);
                _data.writeInt(state);
                _data.writeInt(subId);
                mRemote.transact(IColorOSTelephony.COLOR_SET_SAR_RF_STATE, _data, _reply, 0);
                _reply.readException();
            } catch (Exception e) {
                log("colorSetSarRfState ERROR !!! " + e);
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
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
                mRemote.transact(IColorOSTelephony.COLOR_GET_ESN_CHANGE_FLAG, _data, _reply, 0);
                _reply.readException();
            } catch (Exception e) {
                log("oppoGetEsnChangeFlag ERROR !!! " + e);
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
    }

    public boolean colorIsImsRegistered(Context context, int slotId) {
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            return false;
        }
        boolean _result;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(slotId);
            mRemote.transact(IColorOSTelephony.COLOR_IS_IMS_REGISTERED, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt() != 0;
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            log("colorIsImsRegistered ERROR !!! " + e);
            _result = false;
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return _result;
    }

    public String colorGetPlmnOverride(String operatorNumic, ServiceState ss) {
        return OppoTelephonyFunction.oppoGetPlmnOverride(this.mContext, operatorNumic, ss);
    }

    public String colorGetOemSpn(int slotId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return null;
        }
        String _result;
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(slotId);
            mRemote.transact(IColorOSTelephony.COLOR_GET_OEM_SPN, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readString();
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            log("colorGetOemSpn ERROR !!! " + e);
            _result = null;
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return _result;
    }

    public boolean colorMvnoMatches(int phoneId, int family, String mvnoType, String mvnoMatchData) {
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            return false;
        }
        boolean _result;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(phoneId);
            _data.writeInt(family);
            _data.writeString(mvnoType);
            _data.writeString(mvnoMatchData);
            mRemote.transact(IColorOSTelephony.COLOR_MVNO_MATCHES, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt() != 0;
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            log("colorMvnoMatches ERROR !!! " + e);
            _result = false;
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
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
        if (mRemote == null) {
            return false;
        }
        boolean _result;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(phoneId);
            mRemote.transact(IColorOSTelephony.COLOR_IS_VOLTE_ENABLED, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt() != 0;
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            log("colorIsImsRegistered ERROR !!! " + e);
            _result = false;
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return _result;
    }

    public boolean colorIsVtEnabledByPlatform(Context context, int phoneId) {
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            return false;
        }
        boolean _result;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(phoneId);
            mRemote.transact(IColorOSTelephony.COLOR_IS_VT_ENABLED, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt() != 0;
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            log("colorIsImsRegistered ERROR !!! " + e);
            _result = false;
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return _result;
    }

    public boolean colorIsWfcEnabledByPlatform(Context context, int phoneId) {
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            return false;
        }
        boolean _result;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(phoneId);
            mRemote.transact(IColorOSTelephony.COLOR_IS_WFC_ENABLED, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt() != 0;
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            log("colorIsImsRegistered ERROR !!! " + e);
            _result = false;
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return _result;
    }

    public String colorGetIccId(int slotId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder mRemote = getRemoteServiceBinder();
        if (mRemote == null) {
            _reply.recycle();
            _data.recycle();
            return null;
        }
        String _result;
        try {
            _data.writeInterfaceToken(vDescriptor);
            _data.writeInt(slotId);
            mRemote.transact(IColorOSTelephony.COLOR_GET_ICCID, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readString();
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            log("colorGetIccId ERROR !!! " + e);
            _result = null;
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return _result;
    }

    public void setDualLteEnabled(boolean enable) {
        int i = 0;
        IBinder remoteServiceBinder = getRemoteServiceBinder();
        if (remoteServiceBinder == null) {
            log("setDualLteEnabled remoteServiceBinder is null, return!");
            return;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            if (enable) {
                i = 1;
            }
            _data.writeInt(i);
            remoteServiceBinder.transact(IColorOSTelephony.COLOR_SET_DUAL_LTE_ENABLED, _data, _reply, 0);
            _reply.readException();
        } catch (Exception e) {
            log("setDualLteEnabled ERROR !!! " + e);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public boolean isDualLteEnabled() {
        IBinder remoteServiceBinder = getRemoteServiceBinder();
        if (remoteServiceBinder == null) {
            log("isDualLteEnabled remoteServiceBinder is null, return!");
            return false;
        }
        boolean _result;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(vDescriptor);
            remoteServiceBinder.transact(IColorOSTelephony.COLOR_IS_DUAL_LTE_ENABLED, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt() != 0;
            _reply.recycle();
            _data.recycle();
        } catch (Exception e) {
            log("isDualLteEnabled ERROR !!! " + e);
            _result = false;
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return _result;
    }

    public boolean isDualLteSupportedByPlatform() {
        boolean z = false;
        if (this.mIsExpVersion) {
            String key = OppoCarrierConfigManager.OPPO_DUAL_LTE_AVAILABLE;
            if (CarrierConfigManager.getDefaultConfig().getBoolean(key)) {
                if (getBooleanCarrierConfig(key, 0)) {
                    z = getBooleanCarrierConfig(key, 1);
                }
                this.mIsDualLteSupported = z;
            } else {
                if (getBooleanCarrierConfig(key, 0)) {
                    z = true;
                } else {
                    z = getBooleanCarrierConfig(key, 1);
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
        if (this.mCarrierConfigManager != null) {
            b = this.mCarrierConfigManager.getConfigForSubId(subIds[0]);
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
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
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
}
