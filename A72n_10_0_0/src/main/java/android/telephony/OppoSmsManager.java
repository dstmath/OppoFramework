package android.telephony;

import android.app.ActivityThread;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.ISms;
import java.util.ArrayList;
import java.util.List;

public abstract class OppoSmsManager {
    private static final String APK_LABEL_NAME = "apk_label_name";
    private static final String APK_PACKAGE_NAME = "apk_package_name";
    private static final String DEFAULT_PACKAGE = "com.test.send.message;com.bankmandiri.mandirionline;bri.delivery.brimobile";
    private static final String LOG_TAG = "OppoSmsManager";
    private static final String MESSENGER = "messenger";
    private static final int MSG_CLICK_SEND_ITEM = 1;
    private static final String ROMUPDATE_SEND_MESSAGE_PKG = "romupdate_send_message_pkg";
    private static final String SIM_NAME_ONE = "same_name_one";
    private static final String SIM_NAME_TWO = "same_name_two";
    private static final String SUBSCRIPTION_ID_ONE = "subscription_id_one";
    private static final String SUBSCRIPTION_ID_TWO = "subscription_id_two";
    private static final String TAG = "OppoSmsManager";

    public abstract ArrayList<String> divideMessage(String str);

    public abstract int getSubscriptionId();

    /* access modifiers changed from: protected */
    public abstract int getSubscriptionIdOem();

    /* access modifiers changed from: protected */
    public abstract void notifySmsErrorNoDefaultSetPublic(Context context, PendingIntent pendingIntent);

    /* access modifiers changed from: protected */
    public abstract void notifySmsErrorNoDefaultSetPublic(Context context, List<PendingIntent> list);

    /* access modifiers changed from: protected */
    public abstract void notifySmsGenericErrorPublic(PendingIntent pendingIntent);

    /* access modifiers changed from: protected */
    public abstract void notifySmsGenericErrorPublic(List<PendingIntent> list);

    /* access modifiers changed from: package-private */
    public abstract void resolveSubscriptionForOperationPublic(SmsManager.SubscriptionResolverResult subscriptionResolverResult);

    public abstract void sendMultipartTextMessagePublic(String str, String str2, ArrayList<String> arrayList, ArrayList<PendingIntent> arrayList2, ArrayList<PendingIntent> arrayList3, int i, boolean z, int i2);

    public ArrayList<String> divideMessageOem(String text, int encodingType) {
        if (encodingType == 1 || encodingType == 3) {
            return divideMessageOemInner(text, getSubscriptionId(), encodingType);
        }
        return divideMessage(text);
    }

    private ArrayList<String> divideMessageOemInner(String text, int subId, int encodingType) {
        if (text != null) {
            try {
                ArrayList<String> ret = OppoSmsMessage.oemFragmentText(text, subId, encodingType);
                if (ret != null) {
                    Rlog.d("OppoSmsManager", "divideMessageOem, mSub=" + getSubscriptionIdOem() + " subId=" + subId + " ret.size()=" + ret.size() + " encodingType=" + encodingType);
                }
                return ret;
            } catch (Exception e) {
                e.printStackTrace();
                return SmsMessage.fragmentText(text, subId);
            }
        } else {
            throw new IllegalArgumentException("text is null");
        }
    }

    public void sendMultipartTextMessageOem(String destinationAddress, String scAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, int priority, boolean expectMore, int validityPeriod, int encodingType) {
        Rlog.d("OppoSmsManager", "encodingType=" + encodingType);
        if (encodingType == 1 || encodingType == 3) {
            sendMultipartTextMessageInternalOem(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, true, priority, expectMore, validityPeriod, encodingType);
        } else {
            sendMultipartTextMessagePublic(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, priority, expectMore, validityPeriod);
        }
    }

    private void sendMultipartTextMessageInternalOem(final String destinationAddress, final String scAddress, final List<String> parts, final List<PendingIntent> sentIntents, final List<PendingIntent> deliveryIntents, final boolean persistMessage, int priority, final boolean expectMore, int validityPeriod, final int encodingType) {
        final int priority2;
        final int validityPeriod2;
        PendingIntent deliveryIntent;
        RemoteException e;
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (parts == null || parts.size() < 1) {
            throw new IllegalArgumentException("Invalid message body");
        } else {
            if (priority < 0 || priority > 3) {
                priority2 = -1;
            } else {
                priority2 = priority;
            }
            if (validityPeriod < 5 || validityPeriod > 635040) {
                validityPeriod2 = -1;
            } else {
                validityPeriod2 = validityPeriod;
            }
            if (parts.size() > 1) {
                final Context context = ActivityThread.currentApplication().getApplicationContext();
                if (persistMessage) {
                    resolveSubscriptionForOperationPublic(new SmsManager.SubscriptionResolverResult() {
                        /* class android.telephony.OppoSmsManager.AnonymousClass1 */

                        @Override // android.telephony.SmsManager.SubscriptionResolverResult
                        public void onSuccess(int subId) {
                            try {
                                ISms iSms = OppoSmsManager.getISmsServiceOrThrow();
                                if (iSms != null) {
                                    iSms.sendMultipartTextForSubscriberWithOptionsOem(subId, ActivityThread.currentPackageName(), destinationAddress, scAddress, parts, sentIntents, deliveryIntents, persistMessage, priority2, expectMore, validityPeriod2, encodingType);
                                }
                            } catch (RemoteException e) {
                                Log.e("OppoSmsManager", "sendMultipartTextMessageInternal: Couldn't send SMS - " + e.getMessage());
                                OppoSmsManager.this.notifySmsGenericErrorPublic(sentIntents);
                            }
                        }

                        @Override // android.telephony.SmsManager.SubscriptionResolverResult
                        public void onFailure() {
                            OppoSmsManager.this.notifySmsErrorNoDefaultSetPublic(context, sentIntents);
                        }
                    });
                } else {
                    try {
                        ISms iSms = getISmsServiceOrThrow();
                        if (iSms != null) {
                            try {
                                iSms.sendMultipartTextForSubscriberWithOptionsOem(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, parts, sentIntents, deliveryIntents, persistMessage, priority2, expectMore, validityPeriod2, encodingType);
                            } catch (RemoteException e2) {
                                e = e2;
                            }
                        }
                    } catch (RemoteException e3) {
                        e = e3;
                        Log.e("OppoSmsManager", "sendMultipartTextMessageInternal (no persist): Couldn't send SMS - " + e.getMessage());
                        notifySmsGenericErrorPublic(sentIntents);
                        return;
                    }
                }
                return;
            }
            PendingIntent sentIntent = null;
            if (sentIntents != null && sentIntents.size() > 0) {
                sentIntent = sentIntents.get(0);
            }
            if (deliveryIntents == null || deliveryIntents.size() <= 0) {
                deliveryIntent = null;
            } else {
                deliveryIntent = deliveryIntents.get(0);
            }
            sendTextMessageInternalOem(destinationAddress, scAddress, parts.get(0), sentIntent, deliveryIntent, persistMessage, priority2, expectMore, validityPeriod2, encodingType);
        }
    }

    private void sendTextMessageInternalOem(final String destinationAddress, final String scAddress, final String text, final PendingIntent sentIntent, final PendingIntent deliveryIntent, final boolean persistMessage, int priority, final boolean expectMore, int validityPeriod, final int encodingType) {
        final int priority2;
        final int validityPeriod2;
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (!TextUtils.isEmpty(text)) {
            if (priority < 0 || priority > 3) {
                priority2 = -1;
            } else {
                priority2 = priority;
            }
            if (validityPeriod < 5 || validityPeriod > 635040) {
                validityPeriod2 = -1;
            } else {
                validityPeriod2 = validityPeriod;
            }
            final Context context = ActivityThread.currentApplication().getApplicationContext();
            if (persistMessage) {
                resolveSubscriptionForOperationPublic(new SmsManager.SubscriptionResolverResult() {
                    /* class android.telephony.OppoSmsManager.AnonymousClass2 */

                    @Override // android.telephony.SmsManager.SubscriptionResolverResult
                    public void onSuccess(int subId) {
                        try {
                            ISms iSms = OppoSmsManager.getISmsServiceOrThrow();
                            if (iSms != null) {
                                iSms.sendTextForSubscriberWithOptionsOem(subId, ActivityThread.currentPackageName(), destinationAddress, scAddress, text, sentIntent, deliveryIntent, persistMessage, priority2, expectMore, validityPeriod2, encodingType);
                            }
                        } catch (RemoteException e) {
                            Log.e("OppoSmsManager", "sendTextMessageInternal: Couldn't send SMS, exception - " + e.getMessage());
                            OppoSmsManager.this.notifySmsGenericErrorPublic(sentIntent);
                        }
                    }

                    @Override // android.telephony.SmsManager.SubscriptionResolverResult
                    public void onFailure() {
                        OppoSmsManager.this.notifySmsErrorNoDefaultSetPublic(context, sentIntent);
                    }
                });
                return;
            }
            try {
                ISms iSms = getISmsServiceOrThrow();
                if (iSms != null) {
                    iSms.sendTextForSubscriberWithOptionsOem(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, text, sentIntent, deliveryIntent, persistMessage, priority2, expectMore, validityPeriod2, encodingType);
                }
            } catch (RemoteException e) {
                Log.e("OppoSmsManager", "sendTextMessageInternal(no persist): Couldn't send SMS, exception - " + e.getMessage());
                notifySmsGenericErrorPublic(sentIntent);
            }
        } else {
            throw new IllegalArgumentException("Invalid message body");
        }
    }

    /* access modifiers changed from: private */
    public static ISms getISmsServiceOrThrow() {
        ISms iSms = getISmsService();
        if (iSms != null) {
            return iSms;
        }
        throw new UnsupportedOperationException("Sms is not supported");
    }

    private static ISms getISmsService() {
        return ISms.Stub.asInterface(ServiceManager.getService("isms"));
    }

    /* access modifiers changed from: protected */
    public boolean romSendDataMessage(String destinationAddress, String scAddress, short destinationPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean romSendTextMessageInternal(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage, String packageName) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean romSendMultipartTextMessageInternal(String destinationAddress, String scAddress, List<String> list, List<PendingIntent> list2, List<PendingIntent> list3, boolean persistMessage, String packageName) {
        return false;
    }

    private boolean isNeedDisplayPickSimCardDialog(Context context, List<SubscriptionInfo> operatorSubInfoList) {
        boolean isNeedDisplayDialog = false;
        try {
            Log.d("OppoSmsManager", "isNeedDisplayPickSimCardDialog: mSubId = " + getSubscriptionIdOem() + ", pkgName = " + context.getPackageName());
            if (isWhiteListPackageName(context)) {
                List<SubscriptionInfo> subInfoList = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
                if (subInfoList != null) {
                    int subInfoLength = subInfoList.size();
                    int canSendMessageCardCount = 0;
                    int softSimSlotId = ColorOSTelephonyManager.getDefault(context).colorGetSoftSimCardSlotId();
                    for (int i = 0; i < subInfoLength; i++) {
                        SubscriptionInfo sir = subInfoList.get(i);
                        if (softSimSlotId != sir.getSimSlotIndex() && SubscriptionManager.getSubState(sir.getSubscriptionId()) == 1) {
                            operatorSubInfoList.add(sir);
                            canSendMessageCardCount++;
                        }
                    }
                    if (canSendMessageCardCount == 2) {
                        isNeedDisplayDialog = true;
                    }
                }
            } else {
                isNeedDisplayDialog = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("OppoSmsManager", "isNeedDisplayPickSimCardDialog: isNeedDisplayDialog = " + isNeedDisplayDialog);
        return isNeedDisplayDialog;
    }

    private boolean isWhiteListPackageName(Context context) {
        boolean isWhiteListPackageName = false;
        String allPkg = Settings.Global.getString(context.getContentResolver(), ROMUPDATE_SEND_MESSAGE_PKG);
        Log.d("OppoSmsManager", "isWhiteListPackageName: allPkg = " + allPkg);
        if (TextUtils.isEmpty(allPkg)) {
            allPkg = DEFAULT_PACKAGE;
        }
        String[] splitAllPkg = allPkg.split(";");
        if (splitAllPkg != null && splitAllPkg.length > 0) {
            int i = 0;
            while (true) {
                if (i >= splitAllPkg.length) {
                    break;
                } else if (splitAllPkg[i].trim().equals(context.getPackageName())) {
                    isWhiteListPackageName = true;
                    break;
                } else {
                    i++;
                }
            }
        }
        Log.d("OppoSmsManager", "isWhiteListPackageName: isWhiteListPackageName = " + isWhiteListPackageName);
        return isWhiteListPackageName;
    }

    private String getAppLabel(Context context) {
        String pkg = "";
        try {
            pkg = context.getPackageName();
            Log.d("OppoSmsManager", "isWhiteListPackageName: pkg = " + pkg);
            String rt = context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(pkg, 0)).toString();
            if (!TextUtils.isEmpty(rt)) {
                return rt;
            }
            return pkg;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startPickSimCardActivity(Context context, Handler handler, List<SubscriptionInfo> operatorSubInfoList) {
        Intent intent = new Intent();
        intent.setClassName("com.coloros.simsettings", "com.coloros.simsettings.SendSmsPickSimCardAlertDialog");
        intent.addFlags(268435456);
        intent.putExtra(APK_LABEL_NAME, getAppLabel(context));
        intent.putExtra(APK_PACKAGE_NAME, context.getPackageName());
        intent.putExtra(SIM_NAME_ONE, operatorSubInfoList.get(0).getDisplayName());
        intent.putExtra(SIM_NAME_TWO, operatorSubInfoList.get(1).getDisplayName());
        intent.putExtra(SUBSCRIPTION_ID_ONE, operatorSubInfoList.get(0).getSubscriptionId());
        intent.putExtra(SUBSCRIPTION_ID_TWO, operatorSubInfoList.get(1).getSubscriptionId());
        intent.putExtra("messenger", new Messenger(handler));
        context.startActivity(intent);
    }

    public static SmsManager getSmsManagerForSubscriber(int subId) {
        return SmsManager.getSmsManagerForSubscriptionId(subId);
    }

    public static SmsManager oppogetSmsManagerForSubscriber(int subId) {
        return SmsManager.getSmsManagerForSubscriptionId(subId);
    }
}
