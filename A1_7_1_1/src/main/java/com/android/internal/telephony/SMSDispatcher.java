package com.android.internal.telephony;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.OppoUsageManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Telephony.Sms.Sent;
import android.service.carrier.ICarrierMessagingCallback.Stub;
import android.telephony.CarrierMessagingServiceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.SmsHeader.ConcatRef;
import com.android.internal.telephony.SmsMessageBase.SubmitPduBase;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.mediatek.internal.telephony.ppl.IPplSmsFilter;
import com.mediatek.internal.telephony.ppl.PplSmsFilterExtension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public abstract class SMSDispatcher extends Handler {
    static final boolean DBG = false;
    private static final boolean ENG = false;
    protected static final int EVENT_ACTIVATE_CB_COMPLETE = 101;
    private static final int EVENT_CONFIRM_SEND_TO_POSSIBLE_PREMIUM_SHORT_CODE = 8;
    private static final int EVENT_CONFIRM_SEND_TO_PREMIUM_SHORT_CODE = 9;
    protected static final int EVENT_COPY_TEXT_MESSAGE_DONE = 106;
    protected static final int EVENT_DELAY_SEND_MESSAGE_QUEUE = 107;
    protected static final int EVENT_GET_CB_CONFIG_COMPLETE = 102;
    protected static final int EVENT_HANDLE_STATUS_REPORT = 10;
    protected static final int EVENT_ICC_CHANGED = 15;
    protected static final int EVENT_IMS_STATE_CHANGED = 12;
    protected static final int EVENT_IMS_STATE_DONE = 13;
    protected static final int EVENT_NEW_ICC_SMS = 14;
    protected static final int EVENT_QUERY_CB_ACTIVATION_COMPLETE = 104;
    protected static final int EVENT_RADIO_ON = 11;
    static final int EVENT_SEND_CONFIRMED_SMS = 5;
    private static final int EVENT_SEND_LIMIT_REACHED_CONFIRMATION = 4;
    private static final int EVENT_SEND_RETRY = 3;
    protected static final int EVENT_SEND_SMS_COMPLETE = 2;
    protected static final int EVENT_SET_CB_CONFIG_COMPLETE = 103;
    protected static final int EVENT_SMS_READY = 105;
    static final int EVENT_STOP_SENDING = 7;
    private static final float MAX_LABEL_SIZE_PX = 500.0f;
    private static final int MAX_SEND_RETRIES = 3;
    private static final int MO_MSG_QUEUE_LIMIT = 5;
    protected static String MSG_REF_NUM = null;
    protected static String PDU_SIZE = null;
    private static final int PREMIUM_RULE_USE_BOTH = 3;
    private static final int PREMIUM_RULE_USE_NETWORK = 2;
    private static final int PREMIUM_RULE_USE_SIM = 1;
    private static final int RESULT_ERROR_RUIM_PLUG_OUT = 107;
    private static final String SEND_NEXT_MSG_EXTRA = "SendNextMsg";
    private static final int SEND_RETRY_DELAY = 2000;
    private static final int SINGLE_PART_SMS = 1;
    static final String TAG = "SMSDispatcher";
    protected static final int WAKE_LOCK_TIMEOUT = 500;
    protected static boolean isDmLock;
    private static SmsUsageMonitor mUsageMonitorStatic;
    private static int sConcatenatedRef;
    protected final ArrayList<SmsTracker> deliveryPendingList;
    protected final CommandsInterface mCi;
    protected final Context mContext;
    private BroadcastReceiver mDMLockReceiver;
    private ImsSMSDispatcher mImsSMSDispatcher;
    protected Object mLock;
    private OppoUsageManager mOppoUsageManager;
    private int mPendingTrackerCount;
    protected Phone mPhone;
    private final AtomicInteger mPremiumSmsRule;
    protected final ContentResolver mResolver;
    protected ArrayList<SmsTracker> mSTrackersQueue;
    private final SettingsObserver mSettingsObserver;
    protected boolean mSmsCapable;
    protected boolean mSmsReady;
    protected boolean mSmsSendDisabled;
    protected boolean mStorageAvailable;
    protected boolean mSuccess;
    protected final TelephonyManager mTelephonyManager;
    private SmsUsageMonitor mUsageMonitor;
    protected WakeLock mWakeLock;
    protected int messageCountNeedCopy;

    /* renamed from: com.android.internal.telephony.SMSDispatcher$1 */
    class AnonymousClass1 extends BroadcastReceiver {
        final /* synthetic */ SMSDispatcher this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SMSDispatcher.1.<init>(com.android.internal.telephony.SMSDispatcher):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass1(com.android.internal.telephony.SMSDispatcher r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SMSDispatcher.1.<init>(com.android.internal.telephony.SMSDispatcher):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.1.<init>(com.android.internal.telephony.SMSDispatcher):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.1.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    private final class ConfirmDialogListener implements OnClickListener, OnCancelListener, OnCheckedChangeListener {
        private Button mNegativeButton;
        private Button mPositiveButton;
        private boolean mRememberChoice;
        private final TextView mRememberUndoInstruction;
        private final SmsTracker mTracker;
        final /* synthetic */ SMSDispatcher this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsTracker, android.widget.TextView):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        ConfirmDialogListener(com.android.internal.telephony.SMSDispatcher r1, com.android.internal.telephony.SMSDispatcher.SmsTracker r2, android.widget.TextView r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsTracker, android.widget.TextView):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsTracker, android.widget.TextView):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.onCancel(android.content.DialogInterface):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onCancel(android.content.DialogInterface r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.onCancel(android.content.DialogInterface):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.onCancel(android.content.DialogInterface):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.onCheckedChanged(android.widget.CompoundButton, boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onCheckedChanged(android.widget.CompoundButton r1, boolean r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.onCheckedChanged(android.widget.CompoundButton, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.onCheckedChanged(android.widget.CompoundButton, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.onClick(android.content.DialogInterface, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onClick(android.content.DialogInterface r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.onClick(android.content.DialogInterface, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.onClick(android.content.DialogInterface, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.setNegativeButton(android.widget.Button):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        void setNegativeButton(android.widget.Button r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.setNegativeButton(android.widget.Button):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.setNegativeButton(android.widget.Button):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.setPositiveButton(android.widget.Button):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        void setPositiveButton(android.widget.Button r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.setPositiveButton(android.widget.Button):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.ConfirmDialogListener.setPositiveButton(android.widget.Button):void");
        }
    }

    protected abstract class SmsSender extends CarrierMessagingServiceManager {
        protected volatile SmsSenderCallback mSenderCallback;
        protected final SmsTracker mTracker;
        final /* synthetic */ SMSDispatcher this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SMSDispatcher.SmsSender.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsTracker):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        protected SmsSender(com.android.internal.telephony.SMSDispatcher r1, com.android.internal.telephony.SMSDispatcher.SmsTracker r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SMSDispatcher.SmsSender.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsTracker):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.SmsSender.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsTracker):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SMSDispatcher.SmsSender.sendSmsByCarrierApp(java.lang.String, com.android.internal.telephony.SMSDispatcher$SmsSenderCallback):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void sendSmsByCarrierApp(java.lang.String r1, com.android.internal.telephony.SMSDispatcher.SmsSenderCallback r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SMSDispatcher.SmsSender.sendSmsByCarrierApp(java.lang.String, com.android.internal.telephony.SMSDispatcher$SmsSenderCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.SmsSender.sendSmsByCarrierApp(java.lang.String, com.android.internal.telephony.SMSDispatcher$SmsSenderCallback):void");
        }
    }

    protected final class DataSmsSender extends SmsSender {
        final /* synthetic */ SMSDispatcher this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SMSDispatcher.DataSmsSender.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsTracker):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public DataSmsSender(com.android.internal.telephony.SMSDispatcher r1, com.android.internal.telephony.SMSDispatcher.SmsTracker r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SMSDispatcher.DataSmsSender.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsTracker):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.DataSmsSender.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsTracker):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SMSDispatcher.DataSmsSender.onServiceReady(android.service.carrier.ICarrierMessagingService):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        protected void onServiceReady(android.service.carrier.ICarrierMessagingService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SMSDispatcher.DataSmsSender.onServiceReady(android.service.carrier.ICarrierMessagingService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.DataSmsSender.onServiceReady(android.service.carrier.ICarrierMessagingService):void");
        }
    }

    protected final class MultipartSmsSender extends CarrierMessagingServiceManager {
        private final List<String> mParts;
        private volatile MultipartSmsSenderCallback mSenderCallback;
        public final SmsTracker[] mTrackers;
        final /* synthetic */ SMSDispatcher this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSender.<init>(com.android.internal.telephony.SMSDispatcher, java.util.ArrayList, com.android.internal.telephony.SMSDispatcher$SmsTracker[]):void, dex:  in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSender.<init>(com.android.internal.telephony.SMSDispatcher, java.util.ArrayList, com.android.internal.telephony.SMSDispatcher$SmsTracker[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSender.<init>(com.android.internal.telephony.SMSDispatcher, java.util.ArrayList, com.android.internal.telephony.SMSDispatcher$SmsTracker[]):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public MultipartSmsSender(com.android.internal.telephony.SMSDispatcher r1, java.util.ArrayList<java.lang.String> r2, com.android.internal.telephony.SMSDispatcher.SmsTracker[] r3) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSender.<init>(com.android.internal.telephony.SMSDispatcher, java.util.ArrayList, com.android.internal.telephony.SMSDispatcher$SmsTracker[]):void, dex:  in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSender.<init>(com.android.internal.telephony.SMSDispatcher, java.util.ArrayList, com.android.internal.telephony.SMSDispatcher$SmsTracker[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.MultipartSmsSender.<init>(com.android.internal.telephony.SMSDispatcher, java.util.ArrayList, com.android.internal.telephony.SMSDispatcher$SmsTracker[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus element_width: 03e5 in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSender.onServiceReady(android.service.carrier.ICarrierMessagingService):void, dex:  in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSender.onServiceReady(android.service.carrier.ICarrierMessagingService):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus element_width: 03e5 in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSender.onServiceReady(android.service.carrier.ICarrierMessagingService):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: com.android.dex.DexException: bogus element_width: 03e5
            	at com.android.dx.io.instructions.InstructionCodec$36.decode(InstructionCodec.java:871)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        protected void onServiceReady(android.service.carrier.ICarrierMessagingService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus element_width: 03e5 in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSender.onServiceReady(android.service.carrier.ICarrierMessagingService):void, dex:  in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSender.onServiceReady(android.service.carrier.ICarrierMessagingService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.MultipartSmsSender.onServiceReady(android.service.carrier.ICarrierMessagingService):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSender.sendSmsByCarrierApp(java.lang.String, com.android.internal.telephony.SMSDispatcher$MultipartSmsSenderCallback):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void sendSmsByCarrierApp(java.lang.String r1, com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSender.sendSmsByCarrierApp(java.lang.String, com.android.internal.telephony.SMSDispatcher$MultipartSmsSenderCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.MultipartSmsSender.sendSmsByCarrierApp(java.lang.String, com.android.internal.telephony.SMSDispatcher$MultipartSmsSenderCallback):void");
        }
    }

    protected final class MultipartSmsSenderCallback extends Stub {
        private final MultipartSmsSender mSmsSender;
        final /* synthetic */ SMSDispatcher this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$MultipartSmsSender):void, dex:  in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$MultipartSmsSender):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$MultipartSmsSender):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public MultipartSmsSenderCallback(com.android.internal.telephony.SMSDispatcher r1, com.android.internal.telephony.SMSDispatcher.MultipartSmsSender r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$MultipartSmsSender):void, dex:  in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$MultipartSmsSender):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$MultipartSmsSender):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.onDownloadMmsComplete(int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onDownloadMmsComplete(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.onDownloadMmsComplete(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.onDownloadMmsComplete(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.onFilterComplete(int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onFilterComplete(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.onFilterComplete(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.onFilterComplete(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.onSendMmsComplete(int, byte[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onSendMmsComplete(int r1, byte[] r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.onSendMmsComplete(int, byte[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.onSendMmsComplete(int, byte[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.onSendMultipartSmsComplete(int, int[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onSendMultipartSmsComplete(int r1, int[] r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.onSendMultipartSmsComplete(int, int[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.onSendMultipartSmsComplete(int, int[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.onSendSmsComplete(int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onSendSmsComplete(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.onSendSmsComplete(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.MultipartSmsSenderCallback.onSendSmsComplete(int, int):void");
        }
    }

    private static class SettingsObserver extends ContentObserver {
        private final Context mContext;
        private final AtomicInteger mPremiumSmsRule;

        SettingsObserver(Handler handler, AtomicInteger premiumSmsRule, Context context) {
            super(handler);
            this.mPremiumSmsRule = premiumSmsRule;
            this.mContext = context;
            onChange(false);
        }

        public void onChange(boolean selfChange) {
            this.mPremiumSmsRule.set(Global.getInt(this.mContext.getContentResolver(), "sms_short_code_rule", 1));
        }
    }

    protected final class SmsSenderCallback extends Stub {
        private final SmsSender mSmsSender;
        final /* synthetic */ SMSDispatcher this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsSender):void, dex:  in method: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsSender):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsSender):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public SmsSenderCallback(com.android.internal.telephony.SMSDispatcher r1, com.android.internal.telephony.SMSDispatcher.SmsSender r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsSender):void, dex:  in method: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsSender):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsSender):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.onDownloadMmsComplete(int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onDownloadMmsComplete(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.onDownloadMmsComplete(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.onDownloadMmsComplete(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.onFilterComplete(int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onFilterComplete(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.onFilterComplete(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.onFilterComplete(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.onSendMmsComplete(int, byte[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onSendMmsComplete(int r1, byte[] r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.onSendMmsComplete(int, byte[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.onSendMmsComplete(int, byte[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.onSendMultipartSmsComplete(int, int[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onSendMultipartSmsComplete(int r1, int[] r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.onSendMultipartSmsComplete(int, int[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.onSendMultipartSmsComplete(int, int[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.onSendSmsComplete(int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onSendSmsComplete(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.onSendSmsComplete(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.SmsSenderCallback.onSendSmsComplete(int, int):void");
        }
    }

    public static class SmsTracker {
        private AtomicBoolean mAnyPartFailed;
        public final PackageInfo mAppInfo;
        private final HashMap<String, Object> mData;
        public final PendingIntent mDeliveryIntent;
        public final String mDestAddress;
        public boolean mExpectMore;
        String mFormat;
        private String mFullMessageText;
        public int mImsRetry;
        private boolean mIsText;
        public int mMessageRef;
        public Uri mMessageUri;
        public boolean mPersistMessage;
        private IPplSmsFilter mPplSmsFilter;
        public int mRetryCount;
        public final PendingIntent mSentIntent;
        public final SmsHeader mSmsHeader;
        private int mSubId;
        private long mTimestamp;
        private AtomicInteger mUnsentPartCount;

        class AsyncPersistOrUpdateTask extends AsyncTask<Void, Void, Void> {
            private final Context mContext;
            private int mErrorCode;
            private int mMessageType;
            final /* synthetic */ SmsTracker this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.SMSDispatcher.SmsTracker.AsyncPersistOrUpdateTask.<init>(com.android.internal.telephony.SMSDispatcher$SmsTracker, android.content.Context, int, int):void, dex:  in method: com.android.internal.telephony.SMSDispatcher.SmsTracker.AsyncPersistOrUpdateTask.<init>(com.android.internal.telephony.SMSDispatcher$SmsTracker, android.content.Context, int, int):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.SMSDispatcher.SmsTracker.AsyncPersistOrUpdateTask.<init>(com.android.internal.telephony.SMSDispatcher$SmsTracker, android.content.Context, int, int):void, dex: 
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
                	... 11 more
                Caused by: java.io.EOFException
                	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
                	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
                	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
                	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
                	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
                	... 12 more
                */
            public AsyncPersistOrUpdateTask(com.android.internal.telephony.SMSDispatcher.SmsTracker r1, android.content.Context r2, int r3, int r4) {
                /*
                // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.SMSDispatcher.SmsTracker.AsyncPersistOrUpdateTask.<init>(com.android.internal.telephony.SMSDispatcher$SmsTracker, android.content.Context, int, int):void, dex:  in method: com.android.internal.telephony.SMSDispatcher.SmsTracker.AsyncPersistOrUpdateTask.<init>(com.android.internal.telephony.SMSDispatcher$SmsTracker, android.content.Context, int, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.SmsTracker.AsyncPersistOrUpdateTask.<init>(com.android.internal.telephony.SMSDispatcher$SmsTracker, android.content.Context, int, int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.SmsTracker.AsyncPersistOrUpdateTask.doInBackground(java.lang.Object[]):java.lang.Object, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            protected /* bridge */ /* synthetic */ java.lang.Object doInBackground(java.lang.Object[] r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.SmsTracker.AsyncPersistOrUpdateTask.doInBackground(java.lang.Object[]):java.lang.Object, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.SmsTracker.AsyncPersistOrUpdateTask.doInBackground(java.lang.Object[]):java.lang.Object");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.SMSDispatcher.SmsTracker.AsyncPersistOrUpdateTask.doInBackground(java.lang.Void[]):java.lang.Void, dex:  in method: com.android.internal.telephony.SMSDispatcher.SmsTracker.AsyncPersistOrUpdateTask.doInBackground(java.lang.Void[]):java.lang.Void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.SMSDispatcher.SmsTracker.AsyncPersistOrUpdateTask.doInBackground(java.lang.Void[]):java.lang.Void, dex: 
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
                	... 11 more
                Caused by: java.io.EOFException
                	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
                	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
                	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
                	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
                	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
                	... 12 more
                */
            protected java.lang.Void doInBackground(java.lang.Void... r1) {
                /*
                // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.SMSDispatcher.SmsTracker.AsyncPersistOrUpdateTask.doInBackground(java.lang.Void[]):java.lang.Void, dex:  in method: com.android.internal.telephony.SMSDispatcher.SmsTracker.AsyncPersistOrUpdateTask.doInBackground(java.lang.Void[]):java.lang.Void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.SmsTracker.AsyncPersistOrUpdateTask.doInBackground(java.lang.Void[]):java.lang.Void");
            }
        }

        /* synthetic */ SmsTracker(HashMap data, PendingIntent sentIntent, PendingIntent deliveryIntent, PackageInfo appInfo, String destAddr, String format, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, SmsHeader smsHeader, boolean isExpectMore, String fullMessageText, int subId, boolean isText, boolean persistMessage, SmsTracker smsTracker) {
            this(data, sentIntent, deliveryIntent, appInfo, destAddr, format, unsentPartCount, anyPartFailed, messageUri, smsHeader, isExpectMore, fullMessageText, subId, isText, persistMessage);
        }

        private SmsTracker(HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, PackageInfo appInfo, String destAddr, String format, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, SmsHeader smsHeader, boolean isExpectMore, String fullMessageText, int subId, boolean isText, boolean persistMessage) {
            this.mTimestamp = System.currentTimeMillis();
            this.mPplSmsFilter = null;
            this.mData = data;
            this.mSentIntent = sentIntent;
            this.mDeliveryIntent = deliveryIntent;
            this.mRetryCount = 0;
            this.mAppInfo = appInfo;
            this.mDestAddress = destAddr;
            this.mFormat = format;
            this.mExpectMore = isExpectMore;
            this.mImsRetry = 0;
            this.mMessageRef = 0;
            this.mUnsentPartCount = unsentPartCount;
            this.mAnyPartFailed = anyPartFailed;
            this.mMessageUri = messageUri;
            this.mSmsHeader = smsHeader;
            this.mFullMessageText = fullMessageText;
            this.mSubId = subId;
            this.mIsText = isText;
            this.mPersistMessage = persistMessage;
        }

        boolean isMultipart() {
            return this.mData.containsKey("parts");
        }

        public HashMap<String, Object> getData() {
            return this.mData;
        }

        public void updateSentMessageStatus(Context context, int status) {
            if (this.mMessageUri != null) {
                ContentValues values = new ContentValues(1);
                values.put("status", Integer.valueOf(status));
                SqliteWrapper.update(context, context.getContentResolver(), this.mMessageUri, values, null, null);
            }
        }

        private void updateMessageState(Context context, int messageType, int errorCode) {
            if (this.mMessageUri != null) {
                ContentValues values = new ContentValues(2);
                values.put("type", Integer.valueOf(messageType));
                values.put("error_code", Integer.valueOf(errorCode));
                long identity = Binder.clearCallingIdentity();
                try {
                    if (SqliteWrapper.update(context, context.getContentResolver(), this.mMessageUri, values, null, null) != 1) {
                        Rlog.e(SMSDispatcher.TAG, "Failed to move message to " + messageType);
                    }
                    Binder.restoreCallingIdentity(identity);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        private Uri persistSentMessageIfRequired(Context context, int messageType, int errorCode) {
            if (this.mIsText && this.mPersistMessage) {
                if (SmsApplication.shouldWriteMessageForPackage(this.mAppInfo != null ? this.mAppInfo.packageName : null, context) && !isFilterOutByPpl(context, this.mDestAddress, this.mFullMessageText)) {
                    Rlog.d(SMSDispatcher.TAG, "Persist SMS into " + (messageType == 5 ? "FAILED" : "SENT"));
                    if (this.mAppInfo == null || !SMSDispatcher.noNeedWriteSmsToDbWhenSendSms(context, this.mAppInfo.packageName)) {
                        ContentValues values = new ContentValues();
                        values.put("sub_id", Integer.valueOf(this.mSubId));
                        values.put("address", this.mDestAddress);
                        values.put("body", this.mFullMessageText);
                        values.put("date", Long.valueOf(System.currentTimeMillis()));
                        values.put("seen", Integer.valueOf(1));
                        values.put("read", Integer.valueOf(1));
                        String creator = this.mAppInfo != null ? this.mAppInfo.packageName : null;
                        if (!TextUtils.isEmpty(creator)) {
                            values.put("creator", creator);
                        }
                        if (this.mDeliveryIntent != null) {
                            values.put("status", Integer.valueOf(32));
                        }
                        if (errorCode != 0) {
                            values.put("error_code", Integer.valueOf(errorCode));
                        }
                        long identity = Binder.clearCallingIdentity();
                        ContentResolver resolver = context.getContentResolver();
                        try {
                            Uri uri = resolver.insert(Sent.CONTENT_URI, values);
                            if (uri != null && messageType == 5) {
                                ContentValues updateValues = new ContentValues(1);
                                updateValues.put("type", Integer.valueOf(5));
                                resolver.update(uri, updateValues, null, null);
                            }
                            Binder.restoreCallingIdentity(identity);
                            return uri;
                        } catch (Exception e) {
                            Rlog.e(SMSDispatcher.TAG, "writeOutboxMessage: Failed to persist outbox message", e);
                            Binder.restoreCallingIdentity(identity);
                            return null;
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(identity);
                            throw th;
                        }
                    }
                    Rlog.d("sms", "needWriteSmsToDbWhenSendSms--no");
                    return null;
                }
            }
            return null;
        }

        private void persistOrUpdateMessage(Context context, int messageType, int errorCode) {
            new AsyncPersistOrUpdateTask(this, context, messageType, errorCode).execute(new Void[0]);
        }

        public void onFailed(Context context, int error, int errorCode) {
            if (this.mAnyPartFailed != null) {
                this.mAnyPartFailed.set(true);
            }
            boolean isSinglePartOrLastPart = true;
            if (this.mUnsentPartCount != null) {
                isSinglePartOrLastPart = this.mUnsentPartCount.decrementAndGet() == 0;
            }
            if (isSinglePartOrLastPart) {
                persistOrUpdateMessage(context, 5, errorCode);
            }
            if (this.mSentIntent != null) {
                try {
                    Intent fillIn = new Intent();
                    if (this.mMessageUri != null) {
                        fillIn.putExtra("uri", this.mMessageUri.toString());
                    }
                    if (errorCode != 0) {
                        fillIn.putExtra("errorCode", errorCode);
                    }
                    if (this.mUnsentPartCount != null && isSinglePartOrLastPart) {
                        fillIn.putExtra(SMSDispatcher.SEND_NEXT_MSG_EXTRA, true);
                    }
                    int szPdu = 0;
                    int smscLength = 0;
                    int pduLength = 0;
                    if (this.mData != null) {
                        if (this.mData.get("smsc") != null) {
                            smscLength = ((byte[]) this.mData.get("smsc")).length;
                        }
                        if (this.mData.get("pdu") != null) {
                            pduLength = ((byte[]) this.mData.get("pdu")).length;
                        }
                        szPdu = smscLength + pduLength;
                    }
                    fillIn.putExtra(SMSDispatcher.PDU_SIZE, szPdu);
                    this.mSentIntent.send(context, error, fillIn);
                } catch (CanceledException e) {
                    Rlog.e(SMSDispatcher.TAG, "Failed to send result");
                }
            }
        }

        public void onSent(Context context) {
            boolean isSinglePartOrLastPart = true;
            if (this.mUnsentPartCount != null) {
                isSinglePartOrLastPart = this.mUnsentPartCount.decrementAndGet() == 0;
            }
            if (isSinglePartOrLastPart) {
                int messageType = 2;
                if (this.mAnyPartFailed != null && this.mAnyPartFailed.get()) {
                    messageType = 5;
                }
                persistOrUpdateMessage(context, messageType, 0);
            }
            if (this.mSentIntent != null) {
                try {
                    Intent fillIn = new Intent();
                    if (this.mMessageUri != null) {
                        fillIn.putExtra("uri", this.mMessageUri.toString());
                    }
                    if (this.mUnsentPartCount != null && isSinglePartOrLastPart) {
                        fillIn.putExtra(SMSDispatcher.SEND_NEXT_MSG_EXTRA, true);
                    }
                    int szPdu = 0;
                    int smscLength = 0;
                    int pduLength = 0;
                    if (this.mData != null) {
                        if (this.mData.get("smsc") != null) {
                            smscLength = ((byte[]) this.mData.get("smsc")).length;
                        }
                        if (this.mData.get("pdu") != null) {
                            pduLength = ((byte[]) this.mData.get("pdu")).length;
                        }
                        szPdu = smscLength + pduLength;
                    }
                    fillIn.putExtra(SMSDispatcher.PDU_SIZE, szPdu);
                    fillIn.putExtra(SMSDispatcher.MSG_REF_NUM, this.mMessageRef);
                    Rlog.d(SMSDispatcher.TAG, "message reference number : " + this.mMessageRef);
                    this.mSentIntent.send(context, -1, fillIn);
                } catch (CanceledException e) {
                    Rlog.e(SMSDispatcher.TAG, "Failed to send result");
                }
            }
        }

        protected boolean isFilterOutByPpl(Context context, String destAddr, String text) {
            if (this.mPplSmsFilter == null) {
                this.mPplSmsFilter = new PplSmsFilterExtension(context);
            }
            if (!SmsConstants.isPrivacyLockSupport()) {
                return false;
            }
            if (SMSDispatcher.ENG) {
                Rlog.d(SMSDispatcher.TAG, "[Moms] Phone privacy check start");
            }
            Bundle pplData = new Bundle();
            pplData.putString(IPplSmsFilter.KEY_MSG_CONTENT, text);
            pplData.putString(IPplSmsFilter.KEY_DST_ADDR, destAddr);
            pplData.putString("format", this.mFormat);
            pplData.putInt(IPplSmsFilter.KEY_SUB_ID, this.mSubId);
            pplData.putInt(IPplSmsFilter.KEY_SMS_TYPE, 1);
            boolean pplResult = this.mPplSmsFilter.pplFilter(pplData);
            if (SMSDispatcher.ENG) {
                Rlog.d(SMSDispatcher.TAG, "[Moms] Phone privacy check end, Need to filter(result) = " + pplResult);
            }
            return pplResult;
        }
    }

    protected final class TextSmsSender extends SmsSender {
        final /* synthetic */ SMSDispatcher this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SMSDispatcher.TextSmsSender.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsTracker):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public TextSmsSender(com.android.internal.telephony.SMSDispatcher r1, com.android.internal.telephony.SMSDispatcher.SmsTracker r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SMSDispatcher.TextSmsSender.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsTracker):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.TextSmsSender.<init>(com.android.internal.telephony.SMSDispatcher, com.android.internal.telephony.SMSDispatcher$SmsTracker):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SMSDispatcher.TextSmsSender.onServiceReady(android.service.carrier.ICarrierMessagingService):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        protected void onServiceReady(android.service.carrier.ICarrierMessagingService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SMSDispatcher.TextSmsSender.onServiceReady(android.service.carrier.ICarrierMessagingService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.TextSmsSender.onServiceReady(android.service.carrier.ICarrierMessagingService):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SMSDispatcher.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.<clinit>():void");
    }

    protected abstract TextEncodingDetails calculateLength(CharSequence charSequence, boolean z);

    protected abstract String getFormat();

    protected abstract SmsTracker getNewSubmitPduTracker(String str, String str2, String str3, SmsHeader smsHeader, int i, PendingIntent pendingIntent, PendingIntent pendingIntent2, boolean z, AtomicInteger atomicInteger, AtomicBoolean atomicBoolean, Uri uri, String str4);

    protected abstract void injectSmsPdu(byte[] bArr, String str, PendingIntent pendingIntent);

    protected abstract void sendData(String str, String str2, int i, byte[] bArr, PendingIntent pendingIntent, PendingIntent pendingIntent2);

    protected abstract void sendSms(SmsTracker smsTracker);

    protected abstract void sendSmsByPstn(SmsTracker smsTracker);

    protected abstract void sendSubmitPdu(SmsTracker smsTracker);

    protected abstract void sendText(String str, String str2, String str3, PendingIntent pendingIntent, PendingIntent pendingIntent2, Uri uri, String str4, boolean z);

    protected static int getNextConcatenatedRef() {
        sConcatenatedRef++;
        return sConcatenatedRef;
    }

    protected SMSDispatcher(Phone phone, SmsUsageMonitor usageMonitor, ImsSMSDispatcher imsSMSDispatcher) {
        boolean z;
        this.mPremiumSmsRule = new AtomicInteger(1);
        this.mSTrackersQueue = new ArrayList(5);
        this.mSmsCapable = true;
        this.mOppoUsageManager = null;
        this.mStorageAvailable = true;
        this.mSmsReady = false;
        this.messageCountNeedCopy = 0;
        this.mLock = new Object();
        this.mSuccess = true;
        this.deliveryPendingList = new ArrayList();
        this.mDMLockReceiver = new AnonymousClass1(this);
        this.mPhone = phone;
        this.mImsSMSDispatcher = imsSMSDispatcher;
        this.mOppoUsageManager = OppoUsageManager.getOppoUsageManager();
        this.mContext = phone.getContext();
        this.mResolver = this.mContext.getContentResolver();
        this.mCi = phone.mCi;
        this.mUsageMonitor = usageMonitor;
        if (mUsageMonitorStatic == null) {
            mUsageMonitorStatic = this.mUsageMonitor;
        }
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mSettingsObserver = new SettingsObserver(this, this.mPremiumSmsRule, this.mContext);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("sms_short_code_rule"), false, this.mSettingsObserver);
        this.mSmsCapable = this.mContext.getResources().getBoolean(17956957);
        if (this.mTelephonyManager.getSmsSendCapableForPhone(this.mPhone.getPhoneId(), this.mSmsCapable)) {
            z = false;
        } else {
            z = true;
        }
        this.mSmsSendDisabled = z;
        Rlog.d(TAG, "SMSDispatcher: ctor mSmsCapable=" + this.mSmsCapable + " format=" + getFormat() + " mSmsSendDisabled=" + this.mSmsSendDisabled);
        createWakelock();
        this.mCi.registerForSmsReady(this, 105, null);
        IntentFilter dmFilter = new IntentFilter();
        dmFilter.addAction("com.mediatek.dm.LAWMO_LOCK");
        dmFilter.addAction("com.mediatek.dm.LAWMO_UNLOCK");
        this.mContext.registerReceiver(this.mDMLockReceiver, dmFilter);
    }

    protected void updatePhoneObject(Phone phone) {
        this.mPhone = phone;
        this.mUsageMonitor = phone.mSmsUsageMonitor;
        if (mUsageMonitorStatic == null) {
            mUsageMonitorStatic = this.mUsageMonitor;
        }
        Rlog.d(TAG, "Active phone changed to " + this.mPhone.getPhoneName());
    }

    public void dispose() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mSettingsObserver);
    }

    protected void handleStatusReport(Object o) {
        Rlog.d(TAG, "handleStatusReport() called with no subclass.");
    }

    protected void finalize() throws Throwable {
        super.finalize();
        Rlog.d(TAG, "SMSDispatcher finalized");
    }

    public void handleMessage(Message msg) {
        boolean z = true;
        try {
            AsyncResult ar;
            switch (msg.what) {
                case 2:
                    handleSendComplete((AsyncResult) msg.obj);
                    return;
                case 3:
                    Rlog.d(TAG, "SMS retry..");
                    sendRetrySms((SmsTracker) msg.obj);
                    return;
                case 4:
                    handleReachSentLimit((SmsTracker) msg.obj);
                    return;
                case 5:
                    SmsTracker tracker = msg.obj;
                    if (tracker.isMultipart()) {
                        sendMultipartSms(tracker);
                    } else {
                        if (this.mPendingTrackerCount > 1) {
                            tracker.mExpectMore = true;
                        } else {
                            tracker.mExpectMore = false;
                        }
                        sendSms(tracker);
                    }
                    this.mPendingTrackerCount--;
                    return;
                case 7:
                    ((SmsTracker) msg.obj).onFailed(this.mContext, 5, 0);
                    this.mPendingTrackerCount--;
                    return;
                case 8:
                    handleConfirmShortCode(false, (SmsTracker) msg.obj);
                    return;
                case 9:
                    handleConfirmShortCode(true, (SmsTracker) msg.obj);
                    return;
                case 10:
                    handleStatusReport(msg.obj);
                    return;
                case 101:
                case 102:
                case 103:
                    ar = msg.obj;
                    AsyncResult.forMessage((Message) ar.userObj, ar.result, ar.exception);
                    ((Message) ar.userObj).sendToTarget();
                    return;
                case 104:
                    handleQueryCbActivation((AsyncResult) msg.obj);
                    return;
                case 105:
                    Rlog.d(TAG, "SMS is ready, Phone: " + this.mPhone.getPhoneId());
                    this.mSmsReady = true;
                    notifySmsReady(this.mSmsReady);
                    return;
                case 106:
                    ar = (AsyncResult) msg.obj;
                    synchronized (this.mLock) {
                        if (ar.exception != null) {
                            z = false;
                        }
                        this.mSuccess = z;
                        if (this.mSuccess) {
                            Rlog.d(TAG, "[copyText success to copy one");
                            this.messageCountNeedCopy--;
                        } else {
                            Rlog.d(TAG, "[copyText fail to copy one");
                            this.messageCountNeedCopy = 0;
                        }
                        this.mLock.notifyAll();
                    }
                    return;
                case Phone.OEM_PRODUCT_17373 /*107*/:
                    Rlog.d(TAG, "EVENT_DELAY_SEND_MESSAGE_QUEUE: " + msg.obj);
                    handleSendNextTracker((SmsTracker) msg.obj);
                    return;
                default:
                    Rlog.e(TAG, "handleMessage() ignoring message of unexpected type " + msg.what);
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        e.printStackTrace();
    }

    private static int getSendSmsFlag(PendingIntent deliveryIntent) {
        if (deliveryIntent == null) {
            return 0;
        }
        return 1;
    }

    private void processSendSmsResponse(SmsTracker tracker, int result, int messageRef) {
        if (tracker == null) {
            Rlog.e(TAG, "processSendSmsResponse: null tracker");
            return;
        }
        SmsResponse smsResponse = new SmsResponse(messageRef, null, -1);
        switch (result) {
            case 0:
                Rlog.d(TAG, "Sending SMS by IP succeeded.");
                sendMessage(obtainMessage(2, new AsyncResult(tracker, smsResponse, null)));
                break;
            case 1:
                Rlog.d(TAG, "Sending SMS by IP failed. Retry on carrier network.");
                sendSubmitPdu(tracker);
                break;
            case 2:
                Rlog.d(TAG, "Sending SMS by IP failed.");
                sendMessage(obtainMessage(2, new AsyncResult(tracker, smsResponse, new CommandException(Error.GENERIC_FAILURE))));
                break;
            default:
                Rlog.d(TAG, "Unknown result " + result + " Retry on carrier network.");
                sendSubmitPdu(tracker);
                break;
        }
    }

    protected void handleSendComplete(AsyncResult ar) {
        SmsTracker tracker = ar.userObj;
        PendingIntent sentIntent = tracker.mSentIntent;
        handleSendNextTracker(tracker);
        if (ar.result != null) {
            tracker.mMessageRef = ((SmsResponse) ar.result).mMessageRef;
        } else {
            Rlog.d(TAG, "SmsResponse was null");
        }
        if (ar.exception == null) {
            countSendSms(tracker);
            if (tracker.mDeliveryIntent != null) {
                this.deliveryPendingList.add(tracker);
            }
            tracker.onSent(this.mContext);
        } else {
            int errorCode;
            int ss = this.mPhone.getServiceState().getState();
            if (2 == this.mPhone.getPhoneType() && ar.result != null) {
                errorCode = ((SmsResponse) ar.result).mErrorCode;
                if (errorCode == Phone.OEM_PRODUCT_17373) {
                    Rlog.d(TAG, "RUIM card is plug out");
                    tracker.onFailed(this.mContext, 1, errorCode);
                    return;
                }
            }
            if (tracker.mImsRetry > 0 && ss != 0) {
                tracker.mRetryCount = 3;
                Rlog.d(TAG, "handleSendComplete: Skipping retry:  isIms()=" + isIms() + " mRetryCount=" + tracker.mRetryCount + " mImsRetry=" + tracker.mImsRetry + " mMessageRef=" + tracker.mMessageRef + " SS= " + this.mPhone.getServiceState().getState());
            }
            if (isIms() || ss == 0) {
                errorCode = 0;
                if (ar.result != null) {
                    errorCode = ((SmsResponse) ar.result).mErrorCode;
                }
                int error = 1;
                if (((CommandException) ar.exception).getCommandError() == Error.FDN_CHECK_FAILURE) {
                    error = 6;
                }
                tracker.onFailed(this.mContext, error, errorCode);
            } else {
                tracker.onFailed(this.mContext, getNotInServiceError(ss), 0);
            }
        }
    }

    protected static void handleNotInService(int ss, PendingIntent sentIntent) {
        if (sentIntent == null) {
            Rlog.d(TAG, "Send sms fail without sentIntent due to no service");
        } else if (ss == 3) {
            try {
                sentIntent.send(2);
            } catch (CanceledException e) {
                Rlog.d(TAG, "CanceledException happenedwhen send sms fail with sentIntent due to no service");
            }
        } else {
            sentIntent.send(4);
        }
    }

    protected static int getNotInServiceError(int ss) {
        if (ss == 3) {
            return 2;
        }
        return 4;
    }

    protected void sendMultipartText(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage) {
        int i;
        String fullMessageText = getMultipartMessageText(parts);
        int refNumber = getNextConcatenatedRef() & 255;
        int msgCount = parts.size();
        int encoding = 0;
        TextEncodingDetails[] encodingForParts = new TextEncodingDetails[msgCount];
        for (i = 0; i < msgCount; i++) {
            TextEncodingDetails details = calculateLength((CharSequence) parts.get(i), false);
            if (encoding != details.codeUnitSize && (encoding == 0 || encoding == 1)) {
                encoding = details.codeUnitSize;
            }
            encodingForParts[i] = details;
        }
        Object trackers = new SmsTracker[msgCount];
        AtomicInteger unsentPartCount = new AtomicInteger(msgCount);
        AtomicBoolean anyPartFailed = new AtomicBoolean(false);
        i = 0;
        while (i < msgCount) {
            ConcatRef concatRef = new ConcatRef();
            concatRef.refNumber = refNumber;
            concatRef.seqNumber = i + 1;
            concatRef.msgCount = msgCount;
            concatRef.isEightBits = true;
            SmsHeader smsHeader = new SmsHeader();
            smsHeader.concatRef = concatRef;
            if (encoding == 1) {
                smsHeader.languageTable = encodingForParts[i].languageTable;
                smsHeader.languageShiftTable = encodingForParts[i].languageShiftTable;
            }
            PendingIntent pendingIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                pendingIntent = (PendingIntent) sentIntents.get(i);
            }
            PendingIntent pendingIntent2 = null;
            if (deliveryIntents != null && deliveryIntents.size() > i) {
                pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
            }
            trackers[i] = getNewSubmitPduTracker(destAddr, scAddr, (String) parts.get(i), smsHeader, encoding, pendingIntent, pendingIntent2, i == msgCount + -1, unsentPartCount, anyPartFailed, messageUri, fullMessageText);
            trackers[i].mPersistMessage = persistMessage;
            i++;
        }
        if (parts == null || trackers == null || trackers.length == 0 || trackers[0] == null) {
            Rlog.e(TAG, "Cannot send multipart text. parts=" + parts + " trackers=" + trackers);
            return;
        }
        String carrierPackage = getCarrierAppPackageName();
        if (carrierPackage != null) {
            Rlog.d(TAG, "Found carrier package.");
            MultipartSmsSender multipartSmsSender = new MultipartSmsSender(this, parts, trackers);
            multipartSmsSender.sendSmsByCarrierApp(carrierPackage, new MultipartSmsSenderCallback(this, multipartSmsSender));
        } else {
            Rlog.v(TAG, "No carrier package.");
            for (SmsTracker tracker : trackers) {
                if (tracker != null) {
                    sendSubmitPdu(tracker);
                } else {
                    Rlog.e(TAG, "Null tracker.");
                }
            }
        }
    }

    protected SmsTracker getNewSubmitPduTracker(String destinationAddress, String scAddress, String message, SmsHeader smsHeader, int encoding, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean lastPart, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, String fullMessageText, int validityPeriod) {
        return null;
    }

    protected void sendRawPdu(SmsTracker tracker) {
        byte[] pdu = (byte[]) tracker.getData().get("pdu");
        if (this.mSmsSendDisabled) {
            Rlog.e(TAG, "Device does not support sending sms.");
            tracker.onFailed(this.mContext, 4, 0);
        } else if (pdu == null) {
            Rlog.e(TAG, "Empty PDU");
            tracker.onFailed(this.mContext, 3, 0);
        } else {
            PackageManager pm = this.mContext.getPackageManager();
            String[] packageNames = pm.getPackagesForUid(Binder.getCallingUid());
            if (packageNames == null || packageNames.length == 0) {
                Rlog.e(TAG, "Can't get calling app package name: refusing to send SMS");
                tracker.onFailed(this.mContext, 1, 0);
                return;
            }
            String packageName = getPackageNameViaProcessId(packageNames);
            if (packageName != null) {
                packageNames[0] = packageName;
            }
            Rlog.d(TAG, "sendRawPdu and get the package name via process id: " + packageNames[0]);
            try {
                PackageInfo appInfo = pm.getPackageInfo(packageNames[0], 64);
                if (checkDestination(tracker)) {
                    ServiceStateTracker.mSMSSendCount++;
                    if (this.mUsageMonitor.check(appInfo.packageName, 1)) {
                        sendSms(tracker);
                    } else {
                        sendMessage(obtainMessage(4, tracker));
                        return;
                    }
                }
                if (PhoneNumberUtils.isLocalEmergencyNumber(this.mContext, tracker.mDestAddress)) {
                    new AsyncEmergencyContactNotifier(this.mContext).execute(new Void[0]);
                }
            } catch (NameNotFoundException e) {
                Rlog.e(TAG, "Can't get calling app package info: refusing to send SMS");
                tracker.onFailed(this.mContext, 1, 0);
            }
        }
    }

    boolean checkDestination(SmsTracker tracker) {
        return true;
    }

    private boolean denyIfQueueLimitReached(SmsTracker tracker) {
        if (this.mPendingTrackerCount >= 5) {
            Rlog.e(TAG, "Denied because queue limit reached");
            tracker.onFailed(this.mContext, 5, 0);
            return true;
        }
        this.mPendingTrackerCount++;
        return false;
    }

    private CharSequence getAppLabel(String appPackage) {
        PackageManager pm = this.mContext.getPackageManager();
        try {
            return convertSafeLabel(pm.getApplicationInfo(appPackage, 0).loadLabel(pm).toString(), appPackage);
        } catch (NameNotFoundException e) {
            Rlog.e(TAG, "PackageManager Name Not Found for package " + appPackage);
            return appPackage;
        }
    }

    private CharSequence convertSafeLabel(String labelStr, String appPackage) {
        int labelLength = labelStr.length();
        int offset = 0;
        while (offset < labelLength) {
            int codePoint = labelStr.codePointAt(offset);
            int type = Character.getType(codePoint);
            if (type == 13 || type == 15 || type == 14) {
                labelStr = labelStr.substring(0, offset);
                break;
            }
            if (type == 12) {
                labelStr = labelStr.substring(0, offset) + " " + labelStr.substring(Character.charCount(codePoint) + offset);
            }
            offset += Character.charCount(codePoint);
        }
        labelStr = labelStr.trim();
        if (labelStr.isEmpty()) {
            return appPackage;
        }
        TextPaint paint = new TextPaint();
        paint.setTextSize(42.0f);
        return TextUtils.ellipsize(labelStr, paint, MAX_LABEL_SIZE_PX, TruncateAt.END);
    }

    protected void handleReachSentLimit(SmsTracker tracker) {
        if (!denyIfQueueLimitReached(tracker)) {
            CharSequence appLabel = getAppLabel(tracker.mAppInfo.packageName);
            Resources r = Resources.getSystem();
            Object[] objArr = new Object[1];
            objArr[0] = appLabel;
            Spanned messageText = Html.fromHtml(r.getString(17040388, objArr));
            ConfirmDialogListener listener = new ConfirmDialogListener(this, tracker, null);
            AlertDialog d = new Builder(this.mContext).setTitle(17040387).setIcon(17301642).setMessage(messageText).setPositiveButton(r.getString(17040389), listener).setNegativeButton(r.getString(17040390), listener).setOnCancelListener(listener).create();
            d.getWindow().setType(2003);
            d.show();
        }
    }

    protected void handleConfirmShortCode(boolean isPremium, SmsTracker tracker) {
        if (!denyIfQueueLimitReached(tracker)) {
            int detailsId;
            if (isPremium) {
                detailsId = 17040393;
            } else {
                detailsId = 17040392;
            }
            CharSequence appLabel = getAppLabel(tracker.mAppInfo.packageName);
            Resources r = Resources.getSystem();
            Object[] objArr = new Object[2];
            objArr[0] = appLabel;
            objArr[1] = tracker.mDestAddress;
            Spanned messageText = Html.fromHtml(r.getString(17040391, objArr));
            View layout = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(17367270, null);
            ConfirmDialogListener listener = new ConfirmDialogListener(this, tracker, (TextView) layout.findViewById(16909327));
            ((TextView) layout.findViewById(16909321)).setText(messageText);
            ((TextView) ((ViewGroup) layout.findViewById(16909322)).findViewById(16909324)).setText(detailsId);
            ((CheckBox) layout.findViewById(16909325)).setOnCheckedChangeListener(listener);
            AlertDialog d = new Builder(this.mContext).setView(layout).setPositiveButton(r.getString(17040394), listener).setNegativeButton(r.getString(17040395), listener).setOnCancelListener(listener).create();
            d.getWindow().setType(2003);
            d.show();
            listener.setPositiveButton(d.getButton(-1));
            listener.setNegativeButton(d.getButton(-2));
        }
    }

    public int getPremiumSmsPermission(String packageName) {
        if (mUsageMonitorStatic == null) {
            mUsageMonitorStatic = this.mUsageMonitor;
        }
        return mUsageMonitorStatic.getPremiumSmsPermission(packageName);
    }

    public void setPremiumSmsPermission(String packageName, int permission) {
        if (mUsageMonitorStatic == null) {
            mUsageMonitorStatic = this.mUsageMonitor;
        }
        mUsageMonitorStatic.setPremiumSmsPermission(packageName, permission);
    }

    protected static String getAppNameByIntent(PendingIntent intent) {
        Resources r = Resources.getSystem();
        if (intent != null) {
            return intent.getTargetPackage();
        }
        return "Resource unusable";
    }

    public void sendRetrySms(SmsTracker tracker) {
        if (this.mImsSMSDispatcher != null) {
            this.mImsSMSDispatcher.sendRetrySms(tracker);
        } else {
            Rlog.e(TAG, this.mImsSMSDispatcher + " is null. Retry failed");
        }
    }

    private void sendMultipartSms(SmsTracker tracker) {
        HashMap<String, Object> map = tracker.getData();
        String destinationAddress = (String) map.get("destination");
        String scAddress = (String) map.get("scaddress");
        ArrayList<String> parts = (ArrayList) map.get("parts");
        ArrayList<PendingIntent> sentIntents = (ArrayList) map.get("sentIntents");
        ArrayList<PendingIntent> deliveryIntents = (ArrayList) map.get("deliveryIntents");
        int ss = this.mPhone.getServiceState().getState();
        if (isIms() || ss == 0 || this.mTelephonyManager.isWifiCallingAvailable()) {
            sendMultipartText(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, null, null, tracker.mPersistMessage);
            return;
        }
        int i = 0;
        int count = parts.size();
        while (i < count) {
            PendingIntent pendingIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                pendingIntent = (PendingIntent) sentIntents.get(i);
            }
            handleNotInService(ss, pendingIntent);
            i++;
        }
    }

    protected SmsTracker getSmsTracker(HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, SmsHeader smsHeader, boolean isExpectMore, String fullMessageText, boolean isText, boolean persistMessage) {
        PackageManager pm = this.mContext.getPackageManager();
        String[] packageNames = pm.getPackagesForUid(Binder.getCallingUid());
        PackageInfo appInfo = null;
        if (packageNames != null && packageNames.length > 0) {
            try {
                String packageName = getPackageNameViaProcessId(packageNames);
                if (packageName != null) {
                    packageNames[0] = packageName;
                }
                Rlog.d(TAG, "SmsTrackerFactory and get the package name via process id: " + packageNames[0]);
                appInfo = pm.getPackageInfo(packageNames[0], 64);
            } catch (NameNotFoundException e) {
            }
        }
        return new SmsTracker(data, sentIntent, deliveryIntent, appInfo, PhoneNumberUtils.extractNetworkPortion((String) data.get("destAddr")), format, unsentPartCount, anyPartFailed, messageUri, smsHeader, isExpectMore, fullMessageText, getSubId(), isText, persistMessage, null);
    }

    protected SmsTracker getSmsTracker(HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, Uri messageUri, boolean isExpectMore, String fullMessageText, boolean isText, boolean persistMessage) {
        return getSmsTracker(data, sentIntent, deliveryIntent, format, null, null, messageUri, null, isExpectMore, fullMessageText, isText, persistMessage);
    }

    protected HashMap<String, Object> getSmsTrackerMap(String destAddr, String scAddr, String text, SubmitPduBase pdu) {
        HashMap<String, Object> map = new HashMap();
        map.put("destAddr", destAddr);
        map.put("scAddr", scAddr);
        map.put("text", text);
        map.put("smsc", pdu.encodedScAddress);
        map.put("pdu", pdu.encodedMessage);
        return map;
    }

    protected HashMap<String, Object> getSmsTrackerMap(String destAddr, String scAddr, int destPort, byte[] data, SubmitPduBase pdu) {
        HashMap<String, Object> map = new HashMap();
        map.put("destAddr", destAddr);
        map.put("scAddr", scAddr);
        map.put("destPort", Integer.valueOf(destPort));
        map.put("data", data);
        map.put("smsc", pdu.encodedScAddress);
        map.put("pdu", pdu.encodedMessage);
        return map;
    }

    public boolean isIms() {
        if (this.mImsSMSDispatcher != null) {
            return this.mImsSMSDispatcher.isIms();
        }
        Rlog.e(TAG, this.mImsSMSDispatcher + " is null");
        return false;
    }

    public String getImsSmsFormat() {
        if (this.mImsSMSDispatcher != null) {
            return this.mImsSMSDispatcher.getImsSmsFormat();
        }
        Rlog.e(TAG, this.mImsSMSDispatcher + " is null");
        return null;
    }

    protected String getMultipartMessageText(ArrayList<String> parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part != null) {
                sb.append(part);
            }
        }
        return sb.toString();
    }

    protected String getCarrierAppPackageName() {
        String str = null;
        UiccCard card = UiccController.getInstance().getUiccCard(this.mPhone.getPhoneId());
        if (card == null) {
            return null;
        }
        List<String> carrierPackages = card.getCarrierPackageNamesForIntent(this.mContext.getPackageManager(), new Intent("android.service.carrier.CarrierMessagingService"));
        if (carrierPackages != null && carrierPackages.size() == 1) {
            str = (String) carrierPackages.get(0);
        }
        return str;
    }

    protected int getSubId() {
        return SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mPhone.getPhoneId());
    }

    private void checkCallerIsPhoneOrCarrierApp() {
        int uid = Binder.getCallingUid();
        if (UserHandle.getAppId(uid) != 1001 && uid != 0) {
            try {
                if (!UserHandle.isSameApp(this.mContext.getPackageManager().getApplicationInfo(getCarrierAppPackageName(), 0).uid, Binder.getCallingUid())) {
                    throw new SecurityException("Caller is not phone or carrier app!");
                }
            } catch (NameNotFoundException e) {
                throw new SecurityException("Caller is not phone or carrier app!");
            }
        }
    }

    private void createWakelock() {
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, TAG);
        this.mWakeLock.setReferenceCounted(true);
    }

    protected void handleIccFull() {
    }

    protected void handleQueryCbActivation(AsyncResult ar) {
        Rlog.e(TAG, "didn't support cellBoradcast in the CDMA phone");
    }

    protected void sendData(String destAddr, String scAddr, int destPort, int originalPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
    }

    protected void sendMultipartData(String destAddr, String scAddr, int destPort, ArrayList<SmsRawData> arrayList, ArrayList<PendingIntent> arrayList2, ArrayList<PendingIntent> arrayList3) {
    }

    public int copyTextMessageToIccCard(String scAddress, String address, List<String> list, int status, long timestamp) {
        return 0;
    }

    private void notifySmsReady(boolean isReady) {
        Intent intent = new Intent("android.provider.Telephony.SMS_STATE_CHANGED");
        intent.putExtra("ready", isReady);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        this.mWakeLock.acquire(500);
        this.mContext.sendBroadcast(intent);
    }

    protected void setSmsMemoryStatus(boolean status) {
        if (status != this.mStorageAvailable) {
            this.mStorageAvailable = status;
            this.mCi.reportSmsMemoryStatus(status, null);
        }
    }

    protected boolean isSmsReady() {
        return this.mSmsReady;
    }

    protected void sendTextWithEncodingType(String destAddr, String scAddr, String text, int encodingType, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage) {
    }

    protected void sendMultipartTextWithEncodingType(String destAddr, String scAddr, ArrayList<String> arrayList, int encodingType, ArrayList<PendingIntent> arrayList2, ArrayList<PendingIntent> arrayList3, Uri messageUri, String callingPkg, boolean persistMessage) {
    }

    public void sendTextWithExtraParams(String destAddr, String scAddr, String text, Bundle extraParams, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage) {
    }

    public void sendMultipartTextWithExtraParams(String destAddr, String scAddr, ArrayList<String> arrayList, Bundle extraParams, ArrayList<PendingIntent> arrayList2, ArrayList<PendingIntent> arrayList3, Uri messageUri, String callingPkg, boolean persistMessage) {
    }

    private String getPackageNameViaProcessId(String[] packageNames) {
        String packageName = null;
        if (packageNames.length == 1) {
            return packageNames[0];
        }
        if (packageNames.length <= 1) {
            return null;
        }
        int callingPid = Binder.getCallingPid();
        List<RunningAppProcessInfo> processList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (processList == null) {
            return null;
        }
        for (RunningAppProcessInfo processInfo : processList) {
            if (callingPid == processInfo.pid) {
                for (String pkgInProcess : processInfo.pkgList) {
                    for (String pkg : packageNames) {
                        if (pkg.equals(pkgInProcess)) {
                            packageName = pkg;
                            break;
                        }
                    }
                    if (packageName != null) {
                        return packageName;
                    }
                }
                return packageName;
            }
        }
        return null;
    }

    protected void handleSendNextTracker(SmsTracker currentTracker) {
        int szPdu = 0;
        if (currentTracker != null) {
            HashMap map = currentTracker.mData;
            int smscLength = 0;
            int pduLength = 0;
            if (map != null) {
                if (map.get("smsc") != null) {
                    smscLength = ((byte[]) map.get("smsc")).length;
                }
                if (map.get("pdu") != null) {
                    pduLength = ((byte[]) map.get("pdu")).length;
                }
                szPdu = smscLength + pduLength;
            }
        } else {
            Rlog.d(TAG, "Current tracker is null");
        }
        SmsTracker smsTracker = null;
        synchronized (this.mSTrackersQueue) {
            SmsTracker tempTracker;
            Rlog.d(TAG, "Remove Tracker");
            if (this.mSTrackersQueue.isEmpty()) {
                tempTracker = null;
            } else {
                tempTracker = (SmsTracker) this.mSTrackersQueue.remove(0);
            }
            if (tempTracker != null && tempTracker.equals(currentTracker) && ENG) {
                Rlog.d(TAG, "[pdu size: " + szPdu);
            }
            if (!this.mSTrackersQueue.isEmpty()) {
                smsTracker = (SmsTracker) this.mSTrackersQueue.get(0);
            }
        }
        if (smsTracker == null) {
            Rlog.d(TAG, "mSTrackersQueue is empty");
        } else if (isFormatMatch(smsTracker, this.mPhone)) {
            sendSms(smsTracker);
        } else {
            smsTracker.onFailed(this.mContext, 1, 0);
            if (ENG) {
                Rlog.d(TAG, "The next tracker can't be sent because format doest not mach!");
            }
            sendMessageDelayed(obtainMessage(Phone.OEM_PRODUCT_17373, smsTracker), 10);
        }
    }

    boolean isFormatMatch(SmsTracker tracker, Phone phone) {
        if (ENG) {
            Rlog.d(TAG, "isFormatMatch, isIms " + isIms() + ", ims sms format " + getImsSmsFormat());
        }
        if (isIms() && tracker.mFormat.equals(getImsSmsFormat())) {
            return true;
        }
        if (tracker.mFormat.equals(SmsMessage.FORMAT_3GPP2) && phone.getPhoneType() == 2) {
            return true;
        }
        if (tracker.mFormat.equals(SmsMessage.FORMAT_3GPP) && phone.getPhoneType() == 1) {
            return true;
        }
        return false;
    }

    protected static boolean noNeedWriteSmsToDbWhenSendSms(Context context, String packageName) {
        Cursor cursor = null;
        int count = -1;
        try {
            if (SystemProperties.get("persist.sys.sms_cmcc", "0").equals("1") || context == null || TextUtils.isEmpty(packageName)) {
                return false;
            }
            if (InboundSmsHandler.isDefaultMmsRegion(context)) {
                Rlog.d("sms", "except region");
                return false;
            }
            boolean z;
            Uri uri = Uri.parse("content://com.color.provider.SafeProvider/pp_permission");
            String[] strArr = new String[1];
            strArr[0] = packageName;
            cursor = context.getContentResolver().query(uri, null, "pkg_name=? AND send_sms=1", strArr, null);
            count = cursor.getCount();
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (count >= 1) {
                z = true;
            } else {
                z = false;
            }
            return z;
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ex22) {
                    ex22.printStackTrace();
                }
            }
        }
    }

    protected void countSendSms(SmsTracker smsTracker) {
        try {
            String packageName = smsTracker.mAppInfo.applicationInfo.packageName;
            if (this.mOppoUsageManager == null || !"com.android.mms".equals(packageName)) {
                boolean shouldCount = true;
                String[] countSmsFilterPackages = this.mContext.getResources().getStringArray(17236061);
                Rlog.d(TAG, "packageName=" + packageName);
                if (countSmsFilterPackages != null && packageName != null) {
                    for (String smsPackage : countSmsFilterPackages) {
                        if (smsPackage != null && packageName.startsWith(smsPackage)) {
                            shouldCount = false;
                            break;
                        }
                    }
                }
                Rlog.d(TAG, "shouldCount=" + shouldCount);
                if (shouldCount) {
                    Rlog.d(TAG, "accumulate the count of the send sms");
                    if (this.mOppoUsageManager != null) {
                        this.mOppoUsageManager.accumulateHistoryCountOfSendedMsg(1);
                    }
                }
                return;
            }
            Rlog.d(TAG, "com.android.sms--shouldCount, accumulate the count of the send sms");
            this.mOppoUsageManager.accumulateHistoryCountOfSendedMsg(1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
