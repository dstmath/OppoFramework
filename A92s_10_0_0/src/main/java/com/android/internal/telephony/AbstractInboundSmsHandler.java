package com.android.internal.telephony;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.util.ReflectionHelper;
import com.android.internal.util.StateMachine;
import java.util.List;

public abstract class AbstractInboundSmsHandler extends StateMachine {
    private static final String LOG_TAG = "AbstractInboundSmsHandler";
    private static final String OEM_DEFAULT_SYSTEM_MMS_DELIVER_CLASS = "com.android.mms.transaction.PushReceiver";
    private static final String OEM_DEFAULT_SYSTEM_SMS_DELIVER_CLASS = "com.android.mms.transaction.PrivilegedSmsReceiver";
    private static final String OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE = "com.android.mms";
    private static final String OPPO_BLOCK_SMS_DELIVER_ACTION = "oppo.intent.action.OPPO_BLOCK_SMS_DELIVER_ACTION";
    private static final String TAG = "AbstractInboundSmsHandler";
    private static final String WAP_PUSH_DELIVER_ACTION_OEM_BLOCK_MMS = "oppo.intent.action.WAP_PUSH_DELIVER_ACTION_OEM_BLOCK_MMS";
    private Phone mPhone;
    private IOppoInboundSmsHandler mReference;

    /* access modifiers changed from: protected */
    public void logd(String s) {
        if (this.mPhone != null) {
            OppoRlog.Rlog.d("AbstractInboundSmsHandler[" + this.mPhone.getPhoneId() + "]", s);
            return;
        }
        OppoRlog.Rlog.d("AbstractInboundSmsHandler", s);
    }

    /* access modifiers changed from: protected */
    public void logv(String s) {
        if (this.mPhone != null) {
            OppoRlog.Rlog.v("AbstractInboundSmsHandler[" + this.mPhone.getPhoneId() + "]", s);
            return;
        }
        OppoRlog.Rlog.v("AbstractInboundSmsHandler", s);
    }

    /* access modifiers changed from: protected */
    public void logi(String s) {
        if (this.mPhone != null) {
            OppoRlog.Rlog.i("AbstractInboundSmsHandler[" + this.mPhone.getPhoneId() + "]", s);
            return;
        }
        OppoRlog.Rlog.i("AbstractInboundSmsHandler", s);
    }

    /* access modifiers changed from: protected */
    public void logw(String s) {
        OppoRlog.Rlog.w("AbstractInboundSmsHandler", s);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    public AbstractInboundSmsHandler(String name, Context context, Phone phone) {
        super(r0);
        String str;
        if (phone != null) {
            str = name + "[" + phone.getPhoneId() + "]";
        } else {
            str = name;
        }
        Context context2 = null;
        this.mReference = null;
        this.mPhone = phone;
        this.mReference = (IOppoInboundSmsHandler) OppoTelephonyFactory.getInstance().getFeature(IOppoInboundSmsHandler.DEFAULT, this);
        OppoRlog.Rlog.d("AbstractInboundSmsHandler", "mReference=" + this.mReference);
        oemInitUIHandler(context, phone != null ? phone.getContext() : context2);
    }

    public void oemInitUIHandler(Context context, Context phoneContext) {
        IOppoInboundSmsHandler iOppoInboundSmsHandler = this.mReference;
        if (iOppoInboundSmsHandler != null) {
            iOppoInboundSmsHandler.oemInitUIHandler(context, phoneContext);
        } else {
            OppoRlog.Rlog.e("AbstractInboundSmsHandler", "initUIHandler-error");
        }
    }

    public void oemSetDefaultSms(Context context) {
        IOppoInboundSmsHandler iOppoInboundSmsHandler = this.mReference;
        if (iOppoInboundSmsHandler != null) {
            iOppoInboundSmsHandler.oemSetDefaultSms(context);
        } else {
            OppoRlog.Rlog.e("AbstractInboundSmsHandler", "oemSetDefaultSms-error");
        }
    }

    public void oemSetDefaultWappush(Context context) {
        IOppoInboundSmsHandler iOppoInboundSmsHandler = this.mReference;
        if (iOppoInboundSmsHandler != null) {
            iOppoInboundSmsHandler.oemSetDefaultWappush(context);
        } else {
            OppoRlog.Rlog.e("AbstractInboundSmsHandler", "oemSetDefaultWappush-error");
        }
    }

    public void oemSetWapPushScAddress(WapPushOverSms wapPush, byte[][] pdus, InboundSmsTracker tracker) {
        IOppoInboundSmsHandler iOppoInboundSmsHandler = this.mReference;
        if (iOppoInboundSmsHandler != null) {
            iOppoInboundSmsHandler.oemSetWapPushScAddress(wapPush, pdus, tracker);
        } else {
            OppoRlog.Rlog.e("AbstractInboundSmsHandler", "oemSetWapPushScAddress-error");
        }
    }

    public void oemRemoveAbortFlag(Intent intent, UserHandle user) {
        IOppoInboundSmsHandler iOppoInboundSmsHandler = this.mReference;
        if (iOppoInboundSmsHandler != null) {
            iOppoInboundSmsHandler.oemRemoveAbortFlag(intent, user);
        } else {
            OppoRlog.Rlog.e("AbstractInboundSmsHandler", "oemRemoveAbortFlag-error");
        }
    }

    public void oemMtSmsCount(Intent intent) {
        IOppoInboundSmsHandler iOppoInboundSmsHandler = this.mReference;
        if (iOppoInboundSmsHandler != null) {
            iOppoInboundSmsHandler.oemMtSmsCount(intent);
        } else {
            OppoRlog.Rlog.e("AbstractInboundSmsHandler", "oemCountMtSms-error");
        }
    }

    public boolean oemIsProgressing() {
        IOppoInboundSmsHandler iOppoInboundSmsHandler = this.mReference;
        if (iOppoInboundSmsHandler != null) {
            return iOppoInboundSmsHandler.oemIsProgressing();
        }
        return false;
    }

    public Uri oemCheckSubIdWhenMtSms(UserManager userManager, Phone phone) {
        IOppoInboundSmsHandler iOppoInboundSmsHandler = this.mReference;
        if (iOppoInboundSmsHandler == null) {
            return OemConstant.sRawUriStaticFinal;
        }
        Uri ret = iOppoInboundSmsHandler.oemCheckSubIdWhenMtSms(userManager, phone);
        if (ret != null) {
            return ret;
        }
        OppoRlog.Rlog.e("AbstractInboundSmsHandler", "oemCheckSubIdWhenMtSms, error");
        return OemConstant.sRawUriStaticFinal;
    }

    public boolean oemIsCurrentFormat3gpp2(Context context) {
        IOppoInboundSmsHandler iOppoInboundSmsHandler = this.mReference;
        if (iOppoInboundSmsHandler != null) {
            return iOppoInboundSmsHandler.oemIsCurrentFormat3gpp2(context);
        }
        return InboundSmsHandler.isCurrentFormat3gpp2();
    }

    /* access modifiers changed from: protected */
    public boolean oemDealWithCtImsSms(byte[][] pdus, String format, Context context) {
        IOppoInboundSmsHandler iOppoInboundSmsHandler = this.mReference;
        if (iOppoInboundSmsHandler != null) {
            return iOppoInboundSmsHandler.oemDealWithCtImsSms(pdus, format, context);
        }
        return false;
    }

    public static boolean romFilter(String number, Context context, List<String> smsFilterPackages) {
        if (smsFilterPackages == null) {
            try {
                OppoRlog.Rlog.d("AbstractInboundSmsHandler", "smsFilterPackages == null");
                return false;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (!romIsFindPhoneNumber(context, number)) {
            if (smsFilterPackages.contains("com.coloros.findmyphone")) {
                smsFilterPackages.remove("com.coloros.findmyphone");
            }
            return false;
        } else if (context == null || context.getPackageManager().hasSystemFeature("oppo.version.exp") || smsFilterPackages.contains("com.coloros.findmyphone")) {
            OppoRlog.Rlog.d("AbstractInboundSmsHandler", "filter-size:" + smsFilterPackages.size());
            return false;
        } else {
            smsFilterPackages.add("com.coloros.findmyphone");
            OppoRlog.Rlog.d("AbstractInboundSmsHandler", "add filter");
            return true;
        }
    }

    public boolean romProcessMessagePartWithUserLocked(boolean block, WapPushOverSms wapPush, byte[][] pdus, int destPort, InboundSmsHandler handler) {
        try {
            Object romIsWapPushForMmsWithBlock = ReflectionHelper.callMethod(wapPush, "com.android.internal.telephony.WapPushOverSms", "romIsWapPushForMmsWithBlock", new Class[]{byte[].class, InboundSmsHandler.class}, new Object[]{pdus[0], handler});
            if (!block) {
                if (block || destPort != 2948 || romIsWapPushForMmsWithBlock == null || !((Boolean) romIsWapPushForMmsWithBlock).booleanValue()) {
                    return false;
                }
            }
            OppoRlog.Rlog.d("AbstractInboundSmsHandler", "user locked with Credential-encrypted storage not available. block message, dont show notification");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected static boolean romIsFindPhoneNumber(Context context, String address) {
        String numberString = Settings.Secure.getString(context.getContentResolver(), "findmyphone_sms_service_number");
        if (TextUtils.isEmpty(numberString) || TextUtils.isEmpty(address)) {
            OppoRlog.Rlog.d("AbstractInboundSmsHandler", "number empty or address empty");
            return false;
        }
        String[] numbers = numberString.split(",");
        if (numbers == null || numbers.length <= 0) {
            OppoRlog.Rlog.d("AbstractInboundSmsHandler", "numbers null");
            return false;
        }
        for (String number : numbers) {
            if (address.endsWith(number)) {
                OppoRlog.Rlog.d("AbstractInboundSmsHandler", "find phone");
                return true;
            }
        }
        return false;
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
    public ComponentName romDealWithMtSms(boolean block, Intent intent, ComponentName componentName) {
        if (!block) {
            return null;
        }
        if (componentName != null) {
            try {
                OppoRlog.Rlog.d("AbstractInboundSmsHandler", "romDealWithMtSms , default app: " + componentName.getPackageName());
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        OppoRlog.Rlog.d("AbstractInboundSmsHandler", "oem, Delivering sms");
        intent.putExtra("isBlacklist", true);
        intent.setAction(OPPO_BLOCK_SMS_DELIVER_ACTION);
        return new ComponentName(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE, OEM_DEFAULT_SYSTEM_SMS_DELIVER_CLASS);
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
    public static ComponentName romDealWithMtMms(boolean block, Intent intent, ComponentName componentName) {
        if (!block) {
            return null;
        }
        if (componentName != null) {
            try {
                OppoRlog.Rlog.d("AbstractInboundSmsHandler", "romDealWithMtMms , default app: " + componentName.getPackageName());
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        OppoRlog.Rlog.d("AbstractInboundSmsHandler", "oem Delivering mms");
        intent.setAction(WAP_PUSH_DELIVER_ACTION_OEM_BLOCK_MMS);
        intent.putExtra("isBlacklist", true);
        return new ComponentName(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE, OEM_DEFAULT_SYSTEM_MMS_DELIVER_CLASS);
    }
}
