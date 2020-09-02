package mediatek.telecom;

import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.mediatek.internal.telecom.IMtkTelecomService;
import com.mediatek.telephony.MtkTelephonyManagerEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MtkTelecomManager {
    public static final String ACTION_CALL_RECORD = "mediatek.telecom.action.CALL_RECORD";
    public static final String ACTION_INCALL_SCREEN_STATE_CHANGED = "mediatek.telecom.action.INCALL_SCREEN_STATE_CHANGED";
    public static final String ACTION_NEW_OUTGOING_CALL_UNDEMOTE = "mediatek.intent.action.NEW_OUTGOING_CALL";
    public static final int CALL_RECORDING_EVENT_SHOW_TOAST = 0;
    public static final int CALL_RECORDING_STATE_ACTIVE = 1;
    public static final int CALL_RECORDING_STATE_IDLE = 0;
    public static final String DISCONNECT_REASON_VOLTE_SS_DATA_OFF = "disconnect.reason.volte.ss.data.off";
    public static final int ECT_TYPE_ASSURED = 2;
    public static final int ECT_TYPE_AUTO = 0;
    public static final int ECT_TYPE_BLIND = 1;
    public static final String EVENT_RTT_SUPPORT_CHANGED = "mediatek.telecom.event.RTT_SUPPORT_CHANGED";
    public static final String EXTRA_CALLING_VIA_PHONE_ACCOUNT_HANDLE = "mediatek.telecom.extra.CALLING_VIA_PHONE_ACCOUNT_HANDLE";
    public static final String EXTRA_CALL_RECORDING_STATE = "mediatek.telecom.CALL_RECORDING_STATE";
    public static final String EXTRA_INCALL_SCREEN_SHOW = "mediatek.telecom.extra.INCALL_SCREEN_SHOW";
    public static final String EXTRA_INCOMING_GWSD = "mediatek.telecom.extra.EXTRA_INCOMING_GWSD";
    public static final String EXTRA_INCOMING_VOLTE_CONFERENCE = "mediatek.telecom.extra.EXTRA_INCOMING_VOLTE_CONFERENCE";
    public static final String EXTRA_IS_RTT_EMERGENCY_CALLBACK = "mediatek.telecom.extra.IS_RTT_EMERGENCY_CALLBACK";
    public static final String EXTRA_RTT_STATUS_LOCAL = "mediatek.telecom.extra.RTT_STATUS_LOCAL";
    public static final String EXTRA_RTT_STATUS_REMOTE = "mediatek.telecom.extra.RTT_STATUS_REMOTE";
    public static final String EXTRA_RTT_SUPPORT_LOCAL = "mediatek.telecom.extra.RTT_SUPPORT_LOCAL";
    public static final String EXTRA_RTT_SUPPORT_REMOTE = "mediatek.telecom.extra.RTT_SUPPORT_REMOTE";
    public static final String EXTRA_START_VOLTE_CONFERENCE = "mediatek.telecom.extra.EXTRA_START_VOLTE_CONFERENCE";
    public static final String EXTRA_VIRTUAL_LINE_NUMBER = "mediatek.telecom.extra.VIRTUAL_LINE_NUMBER";
    public static final String EXTRA_VOLTE_CONFERENCE_NUMBERS = "mediatek.telecom.extra.VOLTE_CONFERENCE_NUMBERS";
    public static final String EXTRA_VOLTE_MARKED_AS_EMERGENCY = "mediatek.telecom.extra.VOLTE_MARKED_AS_EMERGENCY";
    public static final String EXTRA_VOLTE_PAU = "mediatek.telecom.extra.VOLTE_PAU";
    public static final String EXTRA_WAS_VIDEO_CALL = "mediatek.telecom.extra.WAS_VIDEO_CALL";
    public static final String MTK_CONNECTION_EVENT_CALL_RECORDING_STATE_CHANGED = "mediatek.telecom.CONNECTION_EVENT_CALL_RECORDING_STATE_CHANGED";
    public static final String MTK_TELECOM_SERVICE_NAME = "mtk_telecom";
    private static final String TAG = MtkTelecomManager.class.getSimpleName();
    private static final MtkTelecomManager sInstance = new MtkTelecomManager();

    private MtkTelecomManager() {
    }

    private IMtkTelecomService getTelecomService() {
        return IMtkTelecomService.Stub.asInterface(ServiceManager.getService(MTK_TELECOM_SERVICE_NAME));
    }

    private boolean isServiceConnected() {
        boolean isConnected = getTelecomService() != null;
        if (!isConnected) {
            Log.w(TAG, "Mtk Telecom Service not found.");
        }
        return isConnected;
    }

    public boolean isInVideoCall(Context context) {
        try {
            if (isServiceConnected()) {
                return getTelecomService().isInVideoCall(context.getOpPackageName());
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException calling isInVideoCall().", e);
            return false;
        }
    }

    public List<PhoneAccount> getAllPhoneAccountsIncludingVirtual() {
        try {
            if (isServiceConnected()) {
                return getTelecomService().getAllPhoneAccountsIncludingVirtual();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException calling getAllPhoneAccountsIncludeVirtual()", e);
        }
        return Collections.EMPTY_LIST;
    }

    public List<PhoneAccountHandle> getAllPhoneAccountHandlesIncludingVirtual() {
        try {
            if (isServiceConnected()) {
                return getTelecomService().getAllPhoneAccountHandlesIncludingVirtual();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException calling getAllPhoneAccountHandlesIncludingVirtual()", e);
        }
        return Collections.EMPTY_LIST;
    }

    public static MtkTelecomManager getInstance() {
        return sInstance;
    }

    public boolean isInVolteCall(Context context) {
        try {
            if (isServiceConnected()) {
                return getTelecomService().isInVolteCall(context.getOpPackageName());
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException calling isInVolteCall().", e);
            return false;
        }
    }

    public boolean isInCall(Context context) {
        try {
            if (isServiceConnected()) {
                return getTelecomService().isInCall(context.getOpPackageName());
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException calling isInCall().", e);
            return false;
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
    public static Intent createConferenceInvitationIntent(Context context) {
        PersistableBundle bundle;
        Intent intent = new Intent("android.intent.action.CALL");
        intent.putExtra(EXTRA_START_VOLTE_CONFERENCE, true);
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        List<PhoneAccount> phoneAccounts = ((TelecomManager) context.getSystemService("telecom")).getAllPhoneAccounts();
        List<PhoneAccount> volteConferenceAccounts = new ArrayList<>(telephonyManager.getPhoneCount());
        for (PhoneAccount phoneAccount : phoneAccounts) {
            int subId = telephonyManager.getSubIdForPhoneAccount(phoneAccount);
            boolean votleEnabled = MtkTelephonyManagerEx.getDefault().isVolteEnabled(subId);
            boolean volteConferenceEnabled = false;
            if (votleEnabled && (bundle = configManager.getConfigForSubId(subId)) != null) {
                volteConferenceEnabled = bundle.getBoolean("mtk_volte_conference_enhanced_enable_bool");
            }
            if (volteConferenceEnabled && votleEnabled) {
                volteConferenceAccounts.add(phoneAccount);
            }
        }
        if (volteConferenceAccounts.size() == 1) {
            intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", volteConferenceAccounts.get(0).getAccountHandle());
        }
        return intent;
    }
}
