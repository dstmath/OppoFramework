package com.android.internal.telephony.imsphone;

import android.app.ActivityManagerNative;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LinkProperties;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.ims.ImsCallForwardInfo;
import com.android.ims.ImsCallForwardInfoEx;
import com.android.ims.ImsEcbmStateListener;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.ims.ImsReasonInfo;
import com.android.ims.ImsSsInfo;
import com.android.ims.ImsUtInterface;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Call.SrvccState;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CallForwardInfoEx;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CallTracker;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccPhoneBookInterfaceManager;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.Phone.FeatureType;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneInternalInterface.DataActivityState;
import com.android.internal.telephony.PhoneInternalInterface.SuppService;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.SuppSrvRequest;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.oem.rus.RusUpdateMtkIms;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class ImsPhone extends ImsPhoneBase {
    static final int CANCEL_ECM_TIMER = 1;
    private static final boolean DBG = true;
    private static final int DEFAULT_ECM_EXIT_TIMER_VALUE = 300000;
    private static final int EVENT_CONFIG_RUS_MTK_IMS = 2;
    private static final int EVENT_CONFIG_USER_AGENT = 1;
    private static final int EVENT_DEFAULT_PHONE_DATA_STATE_CHANGED = 51;
    private static final int EVENT_DELAY_TO_UNREGISTER = 4;
    private static final int EVENT_GET_CALL_BARRING_DONE = 46;
    private static final int EVENT_GET_CALL_WAITING_DONE = 48;
    private static final int EVENT_GET_CLIR_DONE = 50;
    private static final int EVENT_RADIO_STATE_CHANGED = 3;
    private static final int EVENT_SERVICE_STATE_CHANGED = 52;
    private static final int EVENT_SET_CALL_BARRING_DONE = 45;
    private static final int EVENT_SET_CALL_WAITING_DONE = 47;
    private static final int EVENT_SET_CLIR_DONE = 49;
    private static final int EVENT_VOICE_CALL_ENDED = 53;
    private static final String IMS_CALL_BARRING_PASSWORD = "persist.radio.ss.imscbpwd";
    private static final String KEY_LAST_OP = "rus_mtk_ims_last_op";
    private static final String LOG_TAG = "ImsPhone";
    static final int RESTART_ECM_TIMER = 0;
    public static final String USSD_DURING_IMS_INCALL = "ussd_during_ims_incall";
    public static final String UT_BUNDLE_KEY_CLIR = "queryClir";
    private static final boolean VDBG = false;
    private static HandlerThread mConfigThread;
    private boolean isResetSuccess;
    ImsPhoneCallTracker mCT;
    private Handler mConfigHandler;
    private Uri[] mCurrentSubscriberUris;
    private final BroadcastReceiver mDefaultDataSubscriptionChangedReceiver;
    Phone mDefaultPhone;
    private String mDialString;
    private Registrant mEcmExitRespRegistrant;
    private Runnable mExitEcmRunnable;
    ImsExternalCallTracker mExternalCallTracker;
    private ImsEcbmStateListener mImsEcbmStateListener;
    private boolean mImsRegistered;
    private boolean mIsPhoneInEcmState;
    private String mLastDialString;
    private ArrayList<ImsPhoneMmiCode> mPendingMMIs;
    private BroadcastReceiver mResultReceiver;
    private boolean mRoaming;
    private ServiceState mSS;
    private final RegistrantList mSilentRedialRegistrants;
    private RegistrantList mSsnRegistrants;
    public boolean mUssiCSFB;
    private WakeLock mWakeLock;

    /* renamed from: com.android.internal.telephony.imsphone.ImsPhone$4 */
    class AnonymousClass4 extends ImsEcbmStateListener {
        final /* synthetic */ ImsPhone this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.imsphone.ImsPhone.4.<init>(com.android.internal.telephony.imsphone.ImsPhone):void, dex: 
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
        AnonymousClass4(com.android.internal.telephony.imsphone.ImsPhone r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.imsphone.ImsPhone.4.<init>(com.android.internal.telephony.imsphone.ImsPhone):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsPhone.4.<init>(com.android.internal.telephony.imsphone.ImsPhone):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.imsphone.ImsPhone.4.onECBMEntered():void, dex: 
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
        public void onECBMEntered() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.imsphone.ImsPhone.4.onECBMEntered():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsPhone.4.onECBMEntered():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.imsphone.ImsPhone.4.onECBMExited():void, dex: 
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
        public void onECBMExited() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.imsphone.ImsPhone.4.onECBMExited():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsPhone.4.onECBMExited():void");
        }
    }

    /* renamed from: com.android.internal.telephony.imsphone.ImsPhone$5 */
    class AnonymousClass5 extends BroadcastReceiver {
        final /* synthetic */ ImsPhone this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.imsphone.ImsPhone.5.<init>(com.android.internal.telephony.imsphone.ImsPhone):void, dex: 
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
        AnonymousClass5(com.android.internal.telephony.imsphone.ImsPhone r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.imsphone.ImsPhone.5.<init>(com.android.internal.telephony.imsphone.ImsPhone):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsPhone.5.<init>(com.android.internal.telephony.imsphone.ImsPhone):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.imsphone.ImsPhone.5.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.imsphone.ImsPhone.5.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsPhone.5.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    private static class Cf {
        final boolean mIsCfu;
        final Message mOnComplete;
        final int mServiceClass;
        final String mSetCfNumber;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.imsphone.ImsPhone.Cf.<init>(java.lang.String, boolean, android.os.Message, int):void, dex: 
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
        Cf(java.lang.String r1, boolean r2, android.os.Message r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.imsphone.ImsPhone.Cf.<init>(java.lang.String, boolean, android.os.Message, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsPhone.Cf.<init>(java.lang.String, boolean, android.os.Message, int):void");
        }
    }

    private static class CfEx {
        final boolean mIsCfu;
        final Message mOnComplete;
        final String mSetCfNumber;
        final long[] mSetTimeSlot;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.imsphone.ImsPhone.CfEx.<init>(java.lang.String, long[], boolean, android.os.Message):void, dex: 
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
        CfEx(java.lang.String r1, long[] r2, boolean r3, android.os.Message r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.imsphone.ImsPhone.CfEx.<init>(java.lang.String, long[], boolean, android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsPhone.CfEx.<init>(java.lang.String, long[], boolean, android.os.Message):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.imsphone.ImsPhone.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.imsphone.ImsPhone.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsPhone.<clinit>():void");
    }

    public /* bridge */ /* synthetic */ void activateCellBroadcastSms(int activate, Message response) {
        super.activateCellBroadcastSms(activate, response);
    }

    public /* bridge */ /* synthetic */ boolean disableDataConnectivity() {
        return super.disableDataConnectivity();
    }

    public /* bridge */ /* synthetic */ void disableLocationUpdates() {
        super.disableLocationUpdates();
    }

    public /* bridge */ /* synthetic */ boolean enableDataConnectivity() {
        return super.enableDataConnectivity();
    }

    public /* bridge */ /* synthetic */ void enableLocationUpdates() {
        super.enableLocationUpdates();
    }

    public /* bridge */ /* synthetic */ List getAllCellInfo() {
        return super.getAllCellInfo();
    }

    public /* bridge */ /* synthetic */ void getAvailableNetworks(Message response) {
        super.getAvailableNetworks(response);
    }

    public /* bridge */ /* synthetic */ boolean getCallForwardingIndicator() {
        return super.getCallForwardingIndicator();
    }

    public /* bridge */ /* synthetic */ void getCellBroadcastSmsConfig(Message response) {
        super.getCellBroadcastSmsConfig(response);
    }

    public /* bridge */ /* synthetic */ CellLocation getCellLocation() {
        return super.getCellLocation();
    }

    public /* bridge */ /* synthetic */ List getCurrentDataConnectionList() {
        return super.getCurrentDataConnectionList();
    }

    public /* bridge */ /* synthetic */ DataActivityState getDataActivityState() {
        return super.getDataActivityState();
    }

    public /* bridge */ /* synthetic */ void getDataCallList(Message response) {
        super.getDataCallList(response);
    }

    public /* bridge */ /* synthetic */ DataState getDataConnectionState() {
        return super.getDataConnectionState();
    }

    public /* bridge */ /* synthetic */ DataState getDataConnectionState(String apnType) {
        return super.getDataConnectionState(apnType);
    }

    public /* bridge */ /* synthetic */ boolean getDataEnabled() {
        return super.getDataEnabled();
    }

    public /* bridge */ /* synthetic */ boolean getDataRoamingEnabled() {
        return super.getDataRoamingEnabled();
    }

    public /* bridge */ /* synthetic */ String getDeviceId() {
        return super.getDeviceId();
    }

    public /* bridge */ /* synthetic */ String getDeviceSvn() {
        return super.getDeviceSvn();
    }

    public /* bridge */ /* synthetic */ String getEsn() {
        return super.getEsn();
    }

    public /* bridge */ /* synthetic */ String getGroupIdLevel1() {
        return super.getGroupIdLevel1();
    }

    public /* bridge */ /* synthetic */ String getGroupIdLevel2() {
        return super.getGroupIdLevel2();
    }

    public /* bridge */ /* synthetic */ IccCard getIccCard() {
        return super.getIccCard();
    }

    public /* bridge */ /* synthetic */ IccFileHandler getIccFileHandler() {
        return super.getIccFileHandler();
    }

    public /* bridge */ /* synthetic */ IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager() {
        return super.getIccPhoneBookInterfaceManager();
    }

    public /* bridge */ /* synthetic */ boolean getIccRecordsLoaded() {
        return super.getIccRecordsLoaded();
    }

    public /* bridge */ /* synthetic */ String getIccSerialNumber() {
        return super.getIccSerialNumber();
    }

    public /* bridge */ /* synthetic */ String getImei() {
        return super.getImei();
    }

    public /* bridge */ /* synthetic */ String getLine1AlphaTag() {
        return super.getLine1AlphaTag();
    }

    public /* bridge */ /* synthetic */ String getLine1Number() {
        return super.getLine1Number();
    }

    public /* bridge */ /* synthetic */ LinkProperties getLinkProperties(String apnType) {
        return super.getLinkProperties(apnType);
    }

    public /* bridge */ /* synthetic */ String getMeid() {
        return super.getMeid();
    }

    public /* bridge */ /* synthetic */ boolean getMessageWaitingIndicator() {
        return super.getMessageWaitingIndicator();
    }

    public /* bridge */ /* synthetic */ void getNeighboringCids(Message response) {
        super.getNeighboringCids(response);
    }

    public /* bridge */ /* synthetic */ int getPhoneType() {
        return super.getPhoneType();
    }

    public /* bridge */ /* synthetic */ SignalStrength getSignalStrength() {
        return super.getSignalStrength();
    }

    public /* bridge */ /* synthetic */ String getSubscriberId() {
        return super.getSubscriberId();
    }

    public /* bridge */ /* synthetic */ String getVoiceMailAlphaTag() {
        return super.getVoiceMailAlphaTag();
    }

    public /* bridge */ /* synthetic */ String getVoiceMailNumber() {
        return super.getVoiceMailNumber();
    }

    public /* bridge */ /* synthetic */ boolean handlePinMmi(String dialString) {
        return super.handlePinMmi(dialString);
    }

    public /* bridge */ /* synthetic */ boolean isDataConnectivityPossible() {
        return super.isDataConnectivityPossible();
    }

    public /* bridge */ /* synthetic */ void migrateFrom(Phone from) {
        super.migrateFrom(from);
    }

    public /* bridge */ /* synthetic */ boolean needsOtaServiceProvisioning() {
        return super.needsOtaServiceProvisioning();
    }

    public /* bridge */ /* synthetic */ void notifyCallForwardingIndicator() {
        super.notifyCallForwardingIndicator();
    }

    public /* bridge */ /* synthetic */ void notifyDisconnect(Connection cn) {
        super.notifyDisconnect(cn);
    }

    public /* bridge */ /* synthetic */ void notifyPhoneStateChanged() {
        super.notifyPhoneStateChanged();
    }

    public /* bridge */ /* synthetic */ void notifyPreciseCallStateChanged() {
        super.notifyPreciseCallStateChanged();
    }

    public /* bridge */ /* synthetic */ void onTtyModeReceived(int mode) {
        super.onTtyModeReceived(mode);
    }

    public /* bridge */ /* synthetic */ void registerForOnHoldTone(Handler h, int what, Object obj) {
        super.registerForOnHoldTone(h, what, obj);
    }

    public /* bridge */ /* synthetic */ void registerForRingbackTone(Handler h, int what, Object obj) {
        super.registerForRingbackTone(h, what, obj);
    }

    public /* bridge */ /* synthetic */ void registerForTtyModeReceived(Handler h, int what, Object obj) {
        super.registerForTtyModeReceived(h, what, obj);
    }

    public /* bridge */ /* synthetic */ void saveClirSetting(int commandInterfaceCLIRMode) {
        super.saveClirSetting(commandInterfaceCLIRMode);
    }

    public /* bridge */ /* synthetic */ void selectNetworkManually(OperatorInfo network, boolean persistSelection, Message response) {
        super.selectNetworkManually(network, persistSelection, response);
    }

    public /* bridge */ /* synthetic */ void setCellBroadcastSmsConfig(int[] configValuesArray, Message response) {
        super.setCellBroadcastSmsConfig(configValuesArray, response);
    }

    public /* bridge */ /* synthetic */ void setDataEnabled(boolean enable) {
        super.setDataEnabled(enable);
    }

    public /* bridge */ /* synthetic */ void setDataRoamingEnabled(boolean enable) {
        super.setDataRoamingEnabled(enable);
    }

    public /* bridge */ /* synthetic */ boolean setLine1Number(String alphaTag, String number, Message onComplete) {
        return super.setLine1Number(alphaTag, number, onComplete);
    }

    public /* bridge */ /* synthetic */ void setNetworkSelectionModeAutomatic(Message response) {
        super.setNetworkSelectionModeAutomatic(response);
    }

    public /* bridge */ /* synthetic */ void setRadioPower(boolean power) {
        super.setRadioPower(power);
    }

    public /* bridge */ /* synthetic */ void setVoiceMailNumber(String alphaTag, String voiceMailNumber, Message onComplete) {
        super.setVoiceMailNumber(alphaTag, voiceMailNumber, onComplete);
    }

    public /* bridge */ /* synthetic */ void startRingbackTone() {
        super.startRingbackTone();
    }

    public /* bridge */ /* synthetic */ void stopRingbackTone() {
        super.stopRingbackTone();
    }

    public /* bridge */ /* synthetic */ void unregisterForOnHoldTone(Handler h) {
        super.unregisterForOnHoldTone(h);
    }

    public /* bridge */ /* synthetic */ void unregisterForRingbackTone(Handler h) {
        super.unregisterForRingbackTone(h);
    }

    public /* bridge */ /* synthetic */ void unregisterForTtyModeReceived(Handler h) {
        super.unregisterForTtyModeReceived(h);
    }

    public /* bridge */ /* synthetic */ void updateServiceLocation() {
        super.updateServiceLocation();
    }

    protected void setCurrentSubscriberUris(Uri[] currentSubscriberUris) {
        this.mCurrentSubscriberUris = currentSubscriberUris;
    }

    public Uri[] getCurrentSubscriberUris() {
        return this.mCurrentSubscriberUris;
    }

    public ImsPhone(Context context, PhoneNotifier notifier, Phone defaultPhone) {
        this(context, notifier, defaultPhone, false);
    }

    public ImsPhone(Context context, PhoneNotifier notifier, Phone defaultPhone, boolean unitTestMode) {
        super(LOG_TAG, context, notifier, unitTestMode);
        this.mUssiCSFB = false;
        this.mPendingMMIs = new ArrayList();
        this.mSS = new ServiceState();
        this.mSilentRedialRegistrants = new RegistrantList();
        this.mImsRegistered = false;
        this.mRoaming = false;
        this.mSsnRegistrants = new RegistrantList();
        this.mExitEcmRunnable = new Runnable() {
            public void run() {
                ImsPhone.this.exitEmergencyCallbackMode();
            }
        };
        this.isResetSuccess = false;
        this.mConfigHandler = new Handler(mConfigThread.getLooper()) {
            public void handleMessage(Message msg) {
                boolean z = true;
                Rlog.d(ImsPhone.LOG_TAG, "mConfigHandler receive " + msg.what);
                switch (msg.what) {
                    case 1:
                        ImsPhone.this.setUserAgentToMd();
                        return;
                    case 2:
                        ImsPhone imsPhone = ImsPhone.this;
                        if (msg.arg1 != 1) {
                            z = false;
                        }
                        imsPhone.onUpdateRusConfig(z);
                        return;
                    case 3:
                        ImsPhone.this.resetRadioAndIms(true);
                        return;
                    case 4:
                        ImsPhone.this.handleDelayMessage();
                        return;
                    default:
                        return;
                }
            }
        };
        this.mDefaultDataSubscriptionChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Rlog.d(ImsPhone.LOG_TAG, "receive dds broadcast");
                ImsPhone.this.mConfigHandler.sendEmptyMessage(1);
                ImsPhone.this.setRusConfig(true);
            }
        };
        this.mImsEcbmStateListener = new AnonymousClass4(this);
        this.mResultReceiver = new AnonymousClass5(this);
        this.mDefaultPhone = defaultPhone;
        this.mSS.setStateOff();
        this.mExternalCallTracker = TelephonyComponentFactory.getInstance().makeImsExternalCallTracker(this);
        this.mCT = TelephonyComponentFactory.getInstance().makeImsPhoneCallTracker(this);
        this.mCT.registerPhoneStateListener(this.mExternalCallTracker);
        this.mExternalCallTracker.setCallPuller(this.mCT);
        this.mPhoneId = this.mDefaultPhone.getPhoneId();
        this.mIsPhoneInEcmState = Boolean.parseBoolean(TelephonyManager.getTelephonyProperty(this.mPhoneId, "ril.cdma.inecmmode", "false"));
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, LOG_TAG);
        this.mWakeLock.setReferenceCounted(false);
        if (this.mDefaultPhone.getServiceStateTracker() != null) {
            this.mDefaultPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(this, 51, null);
        }
        updateDataServiceState();
        this.mDefaultPhone.registerForServiceStateChanged(this, 52, null);
        this.mContext.registerReceiver(this.mDefaultDataSubscriptionChangedReceiver, new IntentFilter("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED"));
    }

    public void dispose() {
        Rlog.d(LOG_TAG, "dispose");
        this.mPendingMMIs.clear();
        this.mExternalCallTracker.tearDown();
        this.mCT.unregisterPhoneStateListener(this.mExternalCallTracker);
        this.mCT.unregisterForVoiceCallEnded(this);
        this.mCT.dispose();
        if (this.mDefaultPhone != null && this.mDefaultPhone.getServiceStateTracker() != null) {
            this.mDefaultPhone.getServiceStateTracker().unregisterForDataRegStateOrRatChanged(this);
            this.mDefaultPhone.unregisterForServiceStateChanged(this);
        }
    }

    public ServiceState getServiceState() {
        return this.mSS;
    }

    void setServiceState(int state) {
        this.mSS.setVoiceRegState(state);
        updateDataServiceState();
    }

    public CallTracker getCallTracker() {
        return this.mCT;
    }

    public ImsExternalCallTracker getExternalCallTracker() {
        return this.mExternalCallTracker;
    }

    public List<? extends ImsPhoneMmiCode> getPendingMmiCodes() {
        Rlog.d(LOG_TAG, "getPendingMmiCodes");
        dumpPendingMmi();
        return this.mPendingMMIs;
    }

    public void acceptCall(int videoState) throws CallStateException {
        this.mCT.acceptCall(videoState);
    }

    public void rejectCall() throws CallStateException {
        this.mCT.rejectCall();
    }

    public void switchHoldingAndActive() throws CallStateException {
        this.mCT.switchWaitingOrHoldingAndActive();
    }

    public boolean canConference() {
        return this.mCT.canConference();
    }

    public boolean canDial() {
        return this.mCT.canDial();
    }

    public void conference() {
        this.mCT.conference();
    }

    public void clearDisconnected() {
        this.mCT.clearDisconnected();
    }

    public boolean canTransfer() {
        return this.mCT.canTransfer();
    }

    public void explicitCallTransfer() {
        this.mCT.explicitCallTransfer();
    }

    public void explicitCallTransfer(String number, int type) {
        this.mCT.unattendedCallTransfer(number, type);
    }

    public /* bridge */ /* synthetic */ Call getForegroundCall() {
        return getForegroundCall();
    }

    public ImsPhoneCall getForegroundCall() {
        return this.mCT.mForegroundCall;
    }

    public /* bridge */ /* synthetic */ Call getBackgroundCall() {
        return getBackgroundCall();
    }

    public ImsPhoneCall getBackgroundCall() {
        return this.mCT.mBackgroundCall;
    }

    public /* bridge */ /* synthetic */ Call getRingingCall() {
        return getRingingCall();
    }

    public ImsPhoneCall getRingingCall() {
        return this.mCT.mRingingCall;
    }

    private boolean handleCallDeflectionIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        if (getRingingCall().getState() != State.IDLE) {
            Rlog.d(LOG_TAG, "MmiCode 0: rejectCall");
            try {
                this.mCT.rejectCall();
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "reject failed", e);
                notifySuppServiceFailed(SuppService.REJECT);
            }
        } else if (getBackgroundCall().getState() != State.IDLE) {
            Rlog.d(LOG_TAG, "MmiCode 0: hangupWaitingOrBackground");
            try {
                this.mCT.hangup(getBackgroundCall());
            } catch (CallStateException e2) {
                Rlog.d(LOG_TAG, "hangup failed", e2);
            }
        }
        return true;
    }

    private boolean handleCallWaitingIncallSupplementaryService(String dialString) {
        int len = dialString.length();
        if (len > 2) {
            return false;
        }
        ImsPhoneCall call = getForegroundCall();
        if (len > 1) {
            try {
                Rlog.d(LOG_TAG, "not support 1X SEND");
                notifySuppServiceFailed(SuppService.HANGUP);
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "hangup failed", e);
                notifySuppServiceFailed(SuppService.HANGUP);
            }
        } else if (call.getState() != State.IDLE) {
            Rlog.d(LOG_TAG, "MmiCode 1: hangup foreground");
            this.mCT.hangup(call);
        } else {
            Rlog.d(LOG_TAG, "MmiCode 1: switchWaitingOrHoldingAndActive");
            this.mCT.switchWaitingOrHoldingAndActive();
        }
        return true;
    }

    private boolean handleCallHoldIncallSupplementaryService(String dialString) {
        int len = dialString.length();
        if (len > 2) {
            return false;
        }
        if (len > 1) {
            Rlog.d(LOG_TAG, "separate not supported");
            notifySuppServiceFailed(SuppService.SEPARATE);
        } else {
            try {
                if (getRingingCall().getState() != State.IDLE) {
                    Rlog.d(LOG_TAG, "MmiCode 2: accept ringing call");
                    this.mCT.acceptCall(2);
                } else {
                    Rlog.d(LOG_TAG, "MmiCode 2: switchWaitingOrHoldingAndActive");
                    this.mCT.switchWaitingOrHoldingAndActive();
                }
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "switch failed", e);
                notifySuppServiceFailed(SuppService.SWITCH);
            }
        }
        return true;
    }

    private boolean handleMultipartyIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        Rlog.d(LOG_TAG, "MmiCode 3: merge calls");
        conference();
        return true;
    }

    private boolean handleEctIncallSupplementaryService(String dialString) {
        if (dialString.length() != 1) {
            return false;
        }
        Rlog.d(LOG_TAG, "MmiCode 4: not support explicit call transfer");
        notifySuppServiceFailed(SuppService.TRANSFER);
        return true;
    }

    private boolean handleCcbsIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        Rlog.i(LOG_TAG, "MmiCode 5: CCBS not supported!");
        notifySuppServiceFailed(SuppService.UNKNOWN);
        return true;
    }

    public void notifySuppSvcNotification(SuppServiceNotification suppSvc) {
        Rlog.d(LOG_TAG, "notifySuppSvcNotification: suppSvc = " + suppSvc);
        this.mSsnRegistrants.notifyRegistrants(new AsyncResult(null, suppSvc, null));
    }

    public boolean handleInCallMmiCommands(String dialString) {
        if (!isInCall() || TextUtils.isEmpty(dialString)) {
            return false;
        }
        boolean result = false;
        switch (dialString.charAt(0)) {
            case EVENT_GET_CALL_WAITING_DONE /*48*/:
                result = handleCallDeflectionIncallSupplementaryService(dialString);
                break;
            case '1':
                result = handleCallWaitingIncallSupplementaryService(dialString);
                break;
            case '2':
                result = handleCallHoldIncallSupplementaryService(dialString);
                break;
            case '3':
                result = handleMultipartyIncallSupplementaryService(dialString);
                break;
            case '4':
                result = handleEctIncallSupplementaryService(dialString);
                break;
            case '5':
                result = handleCcbsIncallSupplementaryService(dialString);
                break;
        }
        return result;
    }

    private boolean isUssdDuringInCall(ImsPhoneMmiCode mmi) {
        if (mmi == null || !mmi.isUssdNumber()) {
            return false;
        }
        return isInCall();
    }

    public boolean isInCall() {
        State foregroundCallState = getForegroundCall().getState();
        State backgroundCallState = getBackgroundCall().getState();
        State ringingCallState = getRingingCall().getState();
        if (foregroundCallState.isAlive() || backgroundCallState.isAlive()) {
            return true;
        }
        return ringingCallState.isAlive();
    }

    public void notifyNewRingingConnection(Connection c) {
        this.mDefaultPhone.notifyNewRingingConnectionP(c);
    }

    void notifyUnknownConnection(Connection c) {
        this.mDefaultPhone.notifyUnknownConnectionP(c);
    }

    public void notifyForVideoCapabilityChanged(boolean isVideoCapable) {
        this.mIsVideoCapable = isVideoCapable;
        this.mDefaultPhone.notifyForVideoCapabilityChanged(isVideoCapable);
    }

    public Connection dial(String dialString, int videoState) throws CallStateException {
        return dialInternal(dialString, videoState, null);
    }

    public Connection dial(String dialString, UUSInfo uusInfo, int videoState, Bundle intentExtras) throws CallStateException {
        return dialInternal(dialString, videoState, intentExtras);
    }

    private Connection dialInternal(String dialString, int videoState, Bundle intentExtras) throws CallStateException {
        String newDialString = dialString;
        if (!PhoneNumberUtils.isUriNumber(dialString)) {
            newDialString = PhoneNumberUtils.stripSeparators(dialString);
        }
        if (handleInCallMmiCommands(newDialString)) {
            return null;
        }
        if (this.mDefaultPhone.getPhoneType() == 2) {
            return this.mCT.dial(dialString, videoState, intentExtras);
        }
        String networkPortion = dialString;
        if (!PhoneNumberUtils.isUriNumber(dialString)) {
            networkPortion = PhoneNumberUtils.extractNetworkPortionAlt(newDialString);
        }
        ImsPhoneMmiCode mmi = ImsPhoneMmiCode.newFromDialString(networkPortion, this);
        Rlog.d(LOG_TAG, "dialing w/ mmi '" + mmi + "'...");
        if (isUssdDuringInCall(mmi)) {
            Rlog.d(LOG_TAG, "USSD during in-call, ignore this operation!");
            throw new CallStateException(USSD_DURING_IMS_INCALL);
        }
        this.mDialString = dialString;
        if (mmi == null) {
            return this.mCT.dial(dialString, videoState, intentExtras);
        }
        if (mmi.isTemporaryModeCLIR()) {
            return this.mCT.dial(mmi.getDialingNumber(), mmi.getCLIRMode(), videoState, intentExtras);
        }
        if (!mmi.isSupportedOverImsPhone()) {
            this.mDefaultPhone.setCsFallbackStatus(1);
            throw new CallStateException(Phone.CS_FALLBACK);
        } else if (this.mUssiCSFB) {
            Rlog.d(LOG_TAG, "USSI CSFB");
            this.mUssiCSFB = false;
            throw new CallStateException(Phone.CS_FALLBACK);
        } else {
            this.mPendingMMIs.add(mmi);
            Rlog.d(LOG_TAG, "dialInternal: " + dialString + ", mmi=" + mmi);
            dumpPendingMmi();
            this.mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
            mmi.processCode();
            return null;
        }
    }

    public void sendDtmf(char c) {
        if (!PhoneNumberUtils.is12Key(c)) {
            Rlog.e(LOG_TAG, "sendDtmf called with invalid character '" + c + "'");
        } else if (this.mCT.getState() == PhoneConstants.State.OFFHOOK) {
            this.mCT.sendDtmf(c, null);
        }
    }

    public void startDtmf(char c) {
        Object obj = 1;
        if (!PhoneNumberUtils.is12Key(c) && (c < 'A' || c > 'D')) {
            obj = null;
        }
        if (obj == null) {
            Rlog.e(LOG_TAG, "startDtmf called with invalid character '" + c + "'");
        } else {
            this.mCT.startDtmf(c);
        }
    }

    public void stopDtmf() {
        this.mCT.stopDtmf();
    }

    public void notifyIncomingRing() {
        Rlog.d(LOG_TAG, "notifyIncomingRing");
        sendMessage(obtainMessage(14, new AsyncResult(null, null, null)));
    }

    public void setMute(boolean muted) {
        this.mCT.setMute(muted);
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) {
        this.mCT.setUiTTYMode(uiTtyMode, onComplete);
    }

    public boolean getMute() {
        return this.mCT.getMute();
    }

    public PhoneConstants.State getState() {
        return this.mCT.getState();
    }

    public void handleMmiCodeCsfb(int reason, ImsPhoneMmiCode mmi) {
        Rlog.d(LOG_TAG, "handleMmiCodeCsfb: reason = " + reason + ", mDialString = " + this.mDialString + ", mmi=" + mmi);
        removeMmi(mmi);
        if (reason == 830) {
            this.mDefaultPhone.setCsFallbackStatus(2);
        } else if (reason == 831) {
            this.mDefaultPhone.setCsFallbackStatus(1);
        }
        SuppSrvRequest ss = SuppSrvRequest.obtain(15, null);
        ss.mParcel.writeString(this.mDialString);
        this.mDefaultPhone.sendMessage(this.mDefaultPhone.obtainMessage(2001, ss));
    }

    private boolean isValidCommandInterfaceCFReason(int commandInterfaceCFReason) {
        switch (commandInterfaceCFReason) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return true;
            default:
                return false;
        }
    }

    private boolean isValidCommandInterfaceCFAction(int commandInterfaceCFAction) {
        switch (commandInterfaceCFAction) {
            case 0:
            case 1:
            case 3:
            case 4:
                return true;
            default:
                return false;
        }
    }

    private boolean isCfEnable(int action) {
        return action == 1 || action == 3;
    }

    private int getConditionFromCFReason(int reason) {
        switch (reason) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            default:
                return -1;
        }
    }

    private int getCFReasonFromCondition(int condition) {
        switch (condition) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            default:
                return 3;
        }
    }

    private int getActionFromCFAction(int action) {
        switch (action) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 3:
                return 3;
            case 4:
                return 4;
            default:
                return -1;
        }
    }

    public void getOutgoingCallerIdDisplay(Message onComplete) {
        Rlog.d(LOG_TAG, "getCLIR");
        try {
            this.mCT.getUtInterface().queryCLIR(obtainMessage(50, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void setOutgoingCallerIdDisplay(int clirMode, Message onComplete) {
        Rlog.d(LOG_TAG, "setCLIR action= " + clirMode);
        try {
            this.mCT.getUtInterface().updateCLIR(clirMode, obtainMessage(49, clirMode, 0, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
        Rlog.d(LOG_TAG, "getCallForwardingOption reason=" + commandInterfaceCFReason);
        if (isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
            Rlog.d(LOG_TAG, "requesting call forwarding query.");
            if (commandInterfaceCFReason == 0) {
                ((GsmCdmaPhone) this.mDefaultPhone).setSystemProperty("persist.radio.ut.cfu.mode", "disabled_ut_cfu_mode");
            }
            try {
                this.mCT.getUtInterface().queryCallForward(getConditionFromCFReason(commandInterfaceCFReason), null, obtainMessage(13, onComplete));
            } catch (ImsException e) {
                sendErrorResponse(onComplete, e);
            }
        } else if (onComplete != null) {
            sendErrorResponse(onComplete);
        }
    }

    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message onComplete) {
        setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, 1, timerSeconds, onComplete);
    }

    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int serviceClass, int timerSeconds, Message onComplete) {
        Rlog.d(LOG_TAG, "setCallForwardingOption action=" + commandInterfaceCFAction + ", reason=" + commandInterfaceCFReason + " serviceClass=" + serviceClass);
        String operator = SystemProperties.get("ro.oppo.operator", "US");
        String region = SystemProperties.get("persist.sys.oppo.region", "US");
        Rlog.d(LOG_TAG, "operator and region : " + operator + " " + region);
        if (operator.equals("SINGTEL") && region.equals("SG") && dialingNumber != null) {
            if (!dialingNumber.startsWith("+")) {
                dialingNumber = "+65" + dialingNumber;
                Rlog.d(LOG_TAG, "Singtel version add +65!");
            }
        }
        if (operator.equals("VODAFONE") && region.equals("AU") && dialingNumber != null) {
            if (dialingNumber.startsWith("0")) {
                dialingNumber = "+61" + dialingNumber.substring(1, dialingNumber.length());
                Rlog.d(LOG_TAG, "AU VDF version force to use +61 international format!");
            }
        }
        if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
            if ((dialingNumber == null || dialingNumber.isEmpty()) && this.mDefaultPhone != null && this.mDefaultPhone.getPhoneType() == 1 && (this.mDefaultPhone instanceof GsmCdmaPhone) && ((GsmCdmaPhone) this.mDefaultPhone).isSupportSaveCFNumber() && isCfEnable(commandInterfaceCFAction)) {
                String getNumber = ((GsmCdmaPhone) this.mDefaultPhone).getCFPreviousDialNumber(commandInterfaceCFReason);
                if (!(getNumber == null || getNumber.isEmpty())) {
                    dialingNumber = getNumber;
                }
            }
            try {
                this.mCT.getUtInterface().updateCallForward(getActionFromCFAction(commandInterfaceCFAction), getConditionFromCFReason(commandInterfaceCFReason), dialingNumber, serviceClass, timerSeconds, obtainMessage(12, isCfEnable(commandInterfaceCFAction) ? 1 : 0, 0, new Cf(dialingNumber, commandInterfaceCFReason == 0, onComplete, serviceClass)));
            } catch (ImsException e) {
                sendErrorResponse(onComplete, e);
            }
        } else if (onComplete != null) {
            sendErrorResponse(onComplete);
        }
    }

    public void getCallWaiting(Message onComplete) {
        Rlog.d(LOG_TAG, "getCallWaiting");
        try {
            this.mCT.getUtInterface().queryCallWaiting(obtainMessage(EVENT_GET_CALL_WAITING_DONE, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void setCallWaiting(boolean enable, Message onComplete) {
        setCallWaiting(enable, 1, onComplete);
    }

    public void setCallWaiting(boolean enable, int serviceClass, Message onComplete) {
        Rlog.d(LOG_TAG, "setCallWaiting enable=" + enable);
        try {
            this.mCT.getUtInterface().updateCallWaiting(enable, serviceClass, obtainMessage(47, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    private int getCBTypeFromFacility(String facility) {
        if (CommandsInterface.CB_FACILITY_BAOC.equals(facility)) {
            return 2;
        }
        if (CommandsInterface.CB_FACILITY_BAOIC.equals(facility)) {
            return 3;
        }
        if (CommandsInterface.CB_FACILITY_BAOICxH.equals(facility)) {
            return 4;
        }
        if (CommandsInterface.CB_FACILITY_BAIC.equals(facility)) {
            return 1;
        }
        if (CommandsInterface.CB_FACILITY_BAICr.equals(facility)) {
            return 5;
        }
        if (CommandsInterface.CB_FACILITY_BA_ALL.equals(facility)) {
            return 7;
        }
        if (CommandsInterface.CB_FACILITY_BA_MO.equals(facility)) {
            return 8;
        }
        if (CommandsInterface.CB_FACILITY_BA_MT.equals(facility)) {
            return 9;
        }
        return 0;
    }

    public void getCallBarring(String facility, Message onComplete) {
        Rlog.d(LOG_TAG, "getCallBarring facility=" + facility);
        try {
            this.mCT.getUtInterface().queryCallBarring(getCBTypeFromFacility(facility), obtainMessage(46, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void setCallBarring(String facility, boolean lockState, String password, Message onComplete) {
        int action;
        Rlog.d(LOG_TAG, "setCallBarring facility=" + facility + ", lockState=" + lockState);
        Message resp = obtainMessage(45, onComplete);
        if (lockState) {
            action = 1;
        } else {
            action = 0;
        }
        try {
            ImsUtInterface ut = this.mCT.getUtInterface();
            SystemProperties.set(IMS_CALL_BARRING_PASSWORD, password);
            ut.updateCallBarring(getCBTypeFromFacility(facility), action, resp, null);
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void getCallForwardInTimeSlot(int commandInterfaceCFReason, Message onComplete) {
        Rlog.d(LOG_TAG, "getCallForwardInTimeSlot reason = " + commandInterfaceCFReason);
        if (commandInterfaceCFReason == 0) {
            Rlog.d(LOG_TAG, "requesting call forwarding in a time slot query.");
            ((GsmCdmaPhone) this.mDefaultPhone).setSystemProperty("persist.radio.ut.cfu.mode", "disabled_ut_cfu_mode");
            try {
                this.mCT.getUtInterface().queryCallForwardInTimeSlot(getConditionFromCFReason(commandInterfaceCFReason), obtainMessage(109, onComplete));
            } catch (ImsException e) {
                sendErrorResponse(onComplete, e);
            }
        } else if (onComplete != null) {
            sendErrorResponse(onComplete);
        }
    }

    public void setCallForwardInTimeSlot(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, long[] timeSlot, Message onComplete) {
        Rlog.d(LOG_TAG, "setCallForwardInTimeSlot action = " + commandInterfaceCFAction + ", reason = " + commandInterfaceCFReason);
        if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && commandInterfaceCFReason == 0) {
            try {
                this.mCT.getUtInterface().updateCallForwardInTimeSlot(getActionFromCFAction(commandInterfaceCFAction), getConditionFromCFReason(commandInterfaceCFReason), dialingNumber, timerSeconds, timeSlot, obtainMessage(110, commandInterfaceCFAction, 0, new CfEx(dialingNumber, timeSlot, true, onComplete)));
            } catch (ImsException e) {
                sendErrorResponse(onComplete, e);
            }
        } else if (onComplete != null) {
            sendErrorResponse(onComplete);
        }
    }

    private CallForwardInfoEx[] handleCfInTimeSlotQueryResult(ImsCallForwardInfoEx[] infos) {
        CallForwardInfoEx[] cfInfos = null;
        if (is93MDSupport()) {
            if (!(infos == null || infos.length == 0)) {
                cfInfos = new CallForwardInfoEx[infos.length];
            }
        } else if (infos != null) {
            cfInfos = new CallForwardInfoEx[infos.length];
        }
        IccRecords r = this.mDefaultPhone.getIccRecords();
        if (infos != null && infos.length != 0) {
            int i = 0;
            int s = infos.length;
            while (i < s) {
                boolean z;
                if (!(infos[i].mCondition != 0 || (infos[i].mServiceClass & 1) == 0 || r == null)) {
                    if (infos[i].mStatus == 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    setVoiceCallForwardingFlag(r, 1, z, infos[i].mNumber);
                    ((GsmCdmaPhone) this.mDefaultPhone).setSystemProperty("persist.radio.ut.cfu.mode", infos[i].mStatus == 1 ? "enabled_ut_cfu_mode_on" : "enabled_ut_cfu_mode_off");
                    saveTimeSlot(infos[i].mTimeSlot);
                }
                if (infos[i].mCondition == 0 && (infos[i].mServiceClass & 512) != 0) {
                    if (infos[i].mStatus == 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    setVideoCallForwardingFlag(z);
                }
                cfInfos[i] = getCallForwardInfoEx(infos[i]);
                i++;
            }
        } else if (r != null) {
            setVoiceCallForwardingFlag(r, 1, false, null);
            ((GsmCdmaPhone) this.mDefaultPhone).setSystemProperty("persist.radio.ut.cfu.mode", "enabled_ut_cfu_mode_off");
        }
        return cfInfos;
    }

    private CallForwardInfoEx getCallForwardInfoEx(ImsCallForwardInfoEx info) {
        CallForwardInfoEx cfInfo = new CallForwardInfoEx();
        cfInfo.status = info.mStatus;
        cfInfo.reason = getCFReasonFromCondition(info.mCondition);
        cfInfo.serviceClass = info.mServiceClass;
        cfInfo.toa = info.mToA;
        cfInfo.number = info.mNumber;
        cfInfo.timeSeconds = info.mTimeSeconds;
        cfInfo.timeSlot = info.mTimeSlot;
        return cfInfo;
    }

    public void sendUssdResponse(String ussdMessge) {
        Rlog.d(LOG_TAG, "sendUssdResponse");
        ImsPhoneMmiCode mmi = ImsPhoneMmiCode.newFromUssdUserInput(ussdMessge, this);
        this.mPendingMMIs.add(mmi);
        Rlog.d(LOG_TAG, "sendUssdResponse: " + ussdMessge + ", mmi=" + mmi);
        dumpPendingMmi();
        this.mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
        mmi.sendUssd(ussdMessge);
    }

    public void sendUSSD(String ussdString, Message response) {
        this.mCT.sendUSSD(ussdString, response);
    }

    public void cancelUSSD() {
        this.mCT.cancelUSSD();
    }

    public void cancelUSSD(Message response) {
        this.mCT.cancelUSSD(response);
    }

    private void sendErrorResponse(Message onComplete) {
        Rlog.d(LOG_TAG, "sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, null, new CommandException(Error.GENERIC_FAILURE));
            onComplete.sendToTarget();
        }
    }

    void sendErrorResponse(Message onComplete, Throwable e) {
        Rlog.d(LOG_TAG, "sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, null, getCommandException(e));
            onComplete.sendToTarget();
        }
    }

    private CommandException getCommandException(int code, String errorString) {
        Rlog.d(LOG_TAG, "getCommandException code= " + code + ", errorString= " + errorString);
        Error error = Error.GENERIC_FAILURE;
        switch (code) {
            case 801:
                error = Error.REQUEST_NOT_SUPPORTED;
                break;
            case 802:
                error = Error.RADIO_NOT_AVAILABLE;
                break;
            case 821:
                error = Error.PASSWORD_INCORRECT;
                break;
            case 830:
                error = Error.UT_XCAP_403_FORBIDDEN;
                break;
            case 831:
                error = Error.UT_UNKNOWN_HOST;
                break;
            case 833:
                error = Error.UT_XCAP_409_CONFLICT;
                break;
            case 834:
                error = Error.OEM_ERROR_7;
                break;
        }
        return new CommandException(error, errorString);
    }

    private CommandException getCommandException(Throwable e) {
        if (e instanceof ImsException) {
            return getCommandException(((ImsException) e).getCode(), e.getMessage());
        }
        Rlog.d(LOG_TAG, "getCommandException generic failure");
        return new CommandException(Error.GENERIC_FAILURE);
    }

    private void onNetworkInitiatedUssd(ImsPhoneMmiCode mmi) {
        Rlog.d(LOG_TAG, "onNetworkInitiatedUssd");
        this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
    }

    void onIncomingUSSD(int ussdMode, String ussdMessage) {
        Rlog.d(LOG_TAG, "onIncomingUSSD ussdMode=" + ussdMode);
        boolean isUssdRequest = ussdMode == 1;
        boolean isUssdError = ussdMode != 0 ? ussdMode != 1 : false;
        ImsPhoneMmiCode found = null;
        int s = this.mPendingMMIs.size();
        for (int i = 0; i < s; i++) {
            if (((ImsPhoneMmiCode) this.mPendingMMIs.get(i)).isPendingUSSD()) {
                found = (ImsPhoneMmiCode) this.mPendingMMIs.get(i);
                break;
            }
        }
        if (found != null) {
            if (isUssdError) {
                this.mUssiCSFB = true;
                found.onUssdFinishedError();
                return;
            }
            found.onUssdFinished(ussdMessage, isUssdRequest);
        } else if (!isUssdError && ussdMessage != null) {
            onNetworkInitiatedUssd(ImsPhoneMmiCode.newNetworkInitiatedUssd(ussdMessage, isUssdRequest, this));
        }
    }

    public void onMMIDone(ImsPhoneMmiCode mmi) {
        Rlog.d(LOG_TAG, "onMMIDone: " + mmi + ", mUssiCSFB=" + this.mUssiCSFB);
        dumpPendingMmi();
        if (this.mUssiCSFB) {
            this.mDefaultPhone.sendMessage(this.mDefaultPhone.obtainMessage(2003, mmi.getUssdDialString()));
            this.mPendingMMIs.remove(mmi);
            return;
        }
        if (this.mPendingMMIs.remove(mmi) || mmi.isUssdRequest()) {
            this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
        }
    }

    public void removeMmi(ImsPhoneMmiCode mmi) {
        Rlog.d(LOG_TAG, "removeMmi: " + mmi);
        dumpPendingMmi();
        this.mPendingMMIs.remove(mmi);
    }

    public void dumpPendingMmi() {
        int size = this.mPendingMMIs.size();
        if (size == 0) {
            Rlog.d(LOG_TAG, "dumpPendingMmi: none");
            return;
        }
        for (int i = 0; i < size; i++) {
            Rlog.d(LOG_TAG, "dumpPendingMmi: " + this.mPendingMMIs.get(i));
        }
    }

    public ArrayList<Connection> getHandoverConnection() {
        ArrayList<Connection> connList = new ArrayList();
        connList.addAll(getForegroundCall().mConnections);
        connList.addAll(getBackgroundCall().mConnections);
        connList.addAll(getRingingCall().mConnections);
        if (connList.size() > 0) {
            return connList;
        }
        return null;
    }

    public void notifySrvccState(SrvccState state) {
        this.mCT.notifySrvccState(state);
    }

    void initiateSilentRedial() {
        AsyncResult ar = new AsyncResult(null, this.mLastDialString, null);
        if (ar != null) {
            this.mSilentRedialRegistrants.notifyRegistrants(ar);
        }
    }

    public void registerForSilentRedial(Handler h, int what, Object obj) {
        this.mSilentRedialRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSilentRedial(Handler h) {
        this.mSilentRedialRegistrants.remove(h);
    }

    public void registerForSuppServiceNotification(Handler h, int what, Object obj) {
        this.mSsnRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSuppServiceNotification(Handler h) {
        this.mSsnRegistrants.remove(h);
    }

    public int getSubId() {
        return this.mDefaultPhone.getSubId();
    }

    public int getPhoneId() {
        return this.mDefaultPhone.getPhoneId();
    }

    private CallForwardInfo getCallForwardInfo(ImsCallForwardInfo info) {
        CallForwardInfo cfInfo = new CallForwardInfo();
        cfInfo.status = info.mStatus;
        cfInfo.reason = getCFReasonFromCondition(info.mCondition);
        cfInfo.serviceClass = info.mServiceClass;
        cfInfo.toa = info.mToA;
        cfInfo.number = info.mNumber;
        cfInfo.timeSeconds = info.mTimeSeconds;
        return cfInfo;
    }

    private CallForwardInfo[] handleCfQueryResult(ImsCallForwardInfo[] infos) {
        CallForwardInfo[] cfInfos = null;
        if (is93MDSupport()) {
            if (!(infos == null || infos.length == 0)) {
                cfInfos = new CallForwardInfo[infos.length];
            }
        } else if (infos != null) {
            cfInfos = new CallForwardInfo[infos.length];
        }
        IccRecords r = this.mDefaultPhone.getIccRecords();
        if (infos != null && infos.length != 0) {
            int i = 0;
            int s = infos.length;
            while (i < s) {
                boolean z;
                if (!(infos[i].mCondition != 0 || (infos[i].mServiceClass & 1) == 0 || r == null)) {
                    if (infos[i].mStatus == 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    setVoiceCallForwardingFlag(r, 1, z, infos[i].mNumber);
                    ((GsmCdmaPhone) this.mDefaultPhone).setSystemProperty("persist.radio.ut.cfu.mode", infos[i].mStatus == 1 ? "enabled_ut_cfu_mode_on" : "enabled_ut_cfu_mode_off");
                }
                cfInfos[i] = getCallForwardInfo(infos[i]);
                if (infos[i].mCondition == 0 && (infos[i].mServiceClass & 512) != 0) {
                    if (infos[i].mStatus == 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    setVideoCallForwardingFlag(z);
                }
                i++;
            }
        } else if (r != null) {
            setVoiceCallForwardingFlag(r, 1, false, null);
        }
        return cfInfos;
    }

    private int[] handleCbQueryResult(ImsSsInfo[] infos) {
        int[] cbInfos = new int[1];
        cbInfos[0] = infos[0].mStatus;
        return cbInfos;
    }

    private int[] handleCwQueryResult(ImsSsInfo[] infos) {
        int[] cwInfos = new int[2];
        cwInfos[0] = 0;
        if (infos[0].mStatus == 1) {
            cwInfos[0] = 1;
            cwInfos[1] = 1;
        }
        return cwInfos;
    }

    private void sendResponse(Message onComplete, Object result, Throwable e) {
        if (onComplete != null) {
            CommandException ex = null;
            if (e != null) {
                ex = getCommandException(e);
            }
            AsyncResult.forMessage(onComplete, result, ex);
            onComplete.sendToTarget();
        }
    }

    private void updateDataServiceState() {
        if (this.mSS != null && this.mDefaultPhone.getServiceStateTracker() != null && this.mDefaultPhone.getServiceStateTracker().mSS != null) {
            ServiceState ss = this.mDefaultPhone.getServiceStateTracker().mSS;
            this.mSS.setDataRegState(ss.getDataRegState());
            this.mSS.setRilDataRadioTechnology(ss.getRilDataRadioTechnology());
            Rlog.d(LOG_TAG, "updateDataServiceState: defSs = " + ss + " imsSs = " + this.mSS);
        }
    }

    /* JADX WARNING: Missing block: B:3:0x0032, code:
            return;
     */
    /* JADX WARNING: Missing block: B:72:0x01e7, code:
            r23 = null;
     */
    /* JADX WARNING: Missing block: B:73:0x01ed, code:
            if (r5.exception != null) goto L_0x020b;
     */
    /* JADX WARNING: Missing block: B:75:0x01fb, code:
            if (r28.what != 46) goto L_0x0222;
     */
    /* JADX WARNING: Missing block: B:76:0x01fd, code:
            r23 = handleCbQueryResult((com.android.ims.ImsSsInfo[]) r5.result);
     */
    /* JADX WARNING: Missing block: B:77:0x020b, code:
            sendResponse((android.os.Message) r5.userObj, r23, r5.exception);
     */
    /* JADX WARNING: Missing block: B:79:0x022e, code:
            if (r28.what != EVENT_GET_CALL_WAITING_DONE) goto L_0x020b;
     */
    /* JADX WARNING: Missing block: B:80:0x0230, code:
            r23 = handleCwQueryResult((com.android.ims.ImsSsInfo[]) r5.result);
     */
    /* JADX WARNING: Missing block: B:93:0x02a3, code:
            if (is93MDSupport() != false) goto L_0x0303;
     */
    /* JADX WARNING: Missing block: B:95:0x02b1, code:
            if (((com.android.internal.telephony.GsmCdmaPhone) r27.mDefaultPhone).isOpTransferXcap404() == false) goto L_0x0303;
     */
    /* JADX WARNING: Missing block: B:97:0x02b7, code:
            if (r5.exception == null) goto L_0x0303;
     */
    /* JADX WARNING: Missing block: B:99:0x02c3, code:
            if ((r5.exception instanceof com.android.ims.ImsException) == false) goto L_0x0303;
     */
    /* JADX WARNING: Missing block: B:101:0x02d1, code:
            if (r28.what != 45) goto L_0x0303;
     */
    /* JADX WARNING: Missing block: B:102:0x02d3, code:
            r14 = (com.android.ims.ImsException) r5.exception;
     */
    /* JADX WARNING: Missing block: B:103:0x02d7, code:
            if (r14 == null) goto L_0x0303;
     */
    /* JADX WARNING: Missing block: B:105:0x02e3, code:
            if (r14.getCode() != 832) goto L_0x0303;
     */
    /* JADX WARNING: Missing block: B:106:0x02e5, code:
            r19 = (android.os.Message) r5.userObj;
     */
    /* JADX WARNING: Missing block: B:107:0x02eb, code:
            if (r19 == null) goto L_0x0303;
     */
    /* JADX WARNING: Missing block: B:108:0x02ed, code:
            android.os.AsyncResult.forMessage(r19, null, new com.android.internal.telephony.CommandException(com.android.internal.telephony.CommandException.Error.UT_XCAP_404_NOT_FOUND));
            r19.sendToTarget();
     */
    /* JADX WARNING: Missing block: B:109:0x0302, code:
            return;
     */
    /* JADX WARNING: Missing block: B:110:0x0303, code:
            sendResponse((android.os.Message) r5.userObj, null, r5.exception);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(Message msg) {
        AsyncResult ar = msg.obj;
        Rlog.d(LOG_TAG, "handleMessage what=" + msg.what);
        ImsException imsException;
        Message resp;
        switch (msg.what) {
            case 12:
                IccRecords r = this.mDefaultPhone.getIccRecords();
                Cf cf = ar.userObj;
                int cfAction = msg.arg1;
                int cfReason = msg.arg2;
                int cfEnable = isCfEnable(cfAction) ? 1 : 0;
                if (cf.mIsCfu && ar.exception == null && r != null) {
                    if (!((GsmCdmaPhone) this.mDefaultPhone).queryCFUAgainAfterSet() || cfReason != 0) {
                        setVoiceCallForwardingFlag(r, 1, cfEnable == 1, cf.mSetCfNumber);
                        int serviceClass = cf.mServiceClass;
                        if (cfReason == 0 && (serviceClass & 512) != 0) {
                            boolean z;
                            if (cfEnable == 1) {
                                z = true;
                            } else {
                                z = false;
                            }
                            setVideoCallForwardingFlag(z);
                        }
                    } else if (ar.result == null) {
                        Rlog.i(LOG_TAG, "arResult is null.");
                    } else {
                        Rlog.d(LOG_TAG, "[EVENT_SET_CALL_FORWARD_DONE check cfinfo.");
                        CallForwardInfo[] cfInfos = handleCfQueryResult((ImsCallForwardInfo[]) ar.result);
                    }
                }
                if (this.mDefaultPhone != null && this.mDefaultPhone.getPhoneType() == 1 && (this.mDefaultPhone instanceof GsmCdmaPhone) && ((GsmCdmaPhone) this.mDefaultPhone).isSupportSaveCFNumber() && ar.exception == null) {
                    if (cfEnable == 1) {
                        if (!((GsmCdmaPhone) this.mDefaultPhone).applyCFSharePreference(cfReason, cf.mSetCfNumber)) {
                            Rlog.d(LOG_TAG, "applySharePreference false.");
                        }
                    }
                    if (cfAction == 4) {
                        ((GsmCdmaPhone) this.mDefaultPhone).clearCFSharePreference(cfReason);
                    }
                }
                sendResponse(cf.mOnComplete, null, ar.exception);
                break;
            case 13:
                Object cfInfos2 = null;
                if (ar.exception == null) {
                    cfInfos2 = handleCfQueryResult((ImsCallForwardInfo[]) ar.result);
                }
                sendResponse((Message) ar.userObj, cfInfos2, ar.exception);
                break;
            case 45:
                break;
            case 46:
                if (!is93MDSupport() && ((GsmCdmaPhone) this.mDefaultPhone).isOpTransferXcap404() && ar.exception != null && (ar.exception instanceof ImsException)) {
                    imsException = ar.exception;
                    if (imsException != null && imsException.getCode() == 832) {
                        resp = ar.userObj;
                        if (resp != null) {
                            AsyncResult.forMessage(resp, null, new CommandException(Error.UT_XCAP_404_NOT_FOUND));
                            resp.sendToTarget();
                            return;
                        }
                    }
                }
                break;
            case 47:
                break;
            case EVENT_GET_CALL_WAITING_DONE /*48*/:
                break;
            case 49:
                if (ar.exception == null && this.mDefaultPhone.getPhoneType() == 1 && (this.mDefaultPhone instanceof GsmCdmaPhone)) {
                    ((GsmCdmaPhone) this.mDefaultPhone).saveClirSetting(msg.arg1);
                    break;
                }
            case 50:
                Bundle ssInfo = ar.result;
                Object clirInfo = null;
                if (ssInfo != null) {
                    clirInfo = ssInfo.getIntArray("queryClir");
                }
                sendResponse((Message) ar.userObj, clirInfo, ar.exception);
                break;
            case 51:
                Rlog.d(LOG_TAG, "EVENT_DEFAULT_PHONE_DATA_STATE_CHANGED");
                updateDataServiceState();
                break;
            case 52:
                ServiceState newServiceState = msg.obj.result;
                if (this.mRoaming != newServiceState.getRoaming()) {
                    Rlog.d(LOG_TAG, "Roaming state changed");
                    updateRoamingState(newServiceState.getRoaming());
                    break;
                }
                break;
            case 53:
                Rlog.d(LOG_TAG, "Voice call ended. Handle pending updateRoamingState.");
                this.mCT.unregisterForVoiceCallEnded(this);
                boolean newRoaming = getCurrentRoaming();
                if (this.mRoaming != newRoaming) {
                    updateRoamingState(newRoaming);
                    break;
                }
                break;
            case 109:
                Object cfInfosEx = null;
                if (ar.exception == null) {
                    cfInfosEx = handleCfInTimeSlotQueryResult((ImsCallForwardInfoEx[]) ar.result);
                }
                if (ar.exception != null && (ar.exception instanceof ImsException)) {
                    imsException = (ImsException) ar.exception;
                    if (imsException != null && imsException.getCode() == 830) {
                        this.mDefaultPhone.setCsFallbackStatus(2);
                        resp = (Message) ar.userObj;
                        if (resp != null) {
                            AsyncResult.forMessage(resp, cfInfosEx, new CommandException(Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED));
                            resp.sendToTarget();
                            return;
                        }
                    }
                }
                sendResponse((Message) ar.userObj, cfInfosEx, ar.exception);
                break;
            case 110:
                IccRecords records = this.mDefaultPhone.getIccRecords();
                CfEx cfEx = ar.userObj;
                if (cfEx.mIsCfu && ar.exception == null && records != null) {
                    setVoiceCallForwardingFlag(records, 1, (isCfEnable(msg.arg1) ? 1 : 0) == 1, cfEx.mSetCfNumber);
                    saveTimeSlot(cfEx.mSetTimeSlot);
                }
                if (ar.exception != null && (ar.exception instanceof ImsException)) {
                    imsException = (ImsException) ar.exception;
                    if (imsException != null && imsException.getCode() == 830) {
                        this.mDefaultPhone.setCsFallbackStatus(2);
                        resp = cfEx.mOnComplete;
                        if (resp != null) {
                            AsyncResult.forMessage(resp, null, new CommandException(Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED));
                            resp.sendToTarget();
                            return;
                        }
                    }
                }
                sendResponse(cfEx.mOnComplete, null, ar.exception);
                break;
            default:
                super.handleMessage(msg);
                break;
        }
    }

    public ImsEcbmStateListener getImsEcbmStateListener() {
        return this.mImsEcbmStateListener;
    }

    public boolean isInEmergencyCall() {
        return this.mCT.isInEmergencyCall();
    }

    public boolean isInEcm() {
        return this.mIsPhoneInEcmState;
    }

    private void sendEmergencyCallbackModeChange() {
        Intent intent = new Intent("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        intent.putExtra("phoneinECMState", this.mIsPhoneInEcmState);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, getPhoneId());
        ActivityManagerNative.broadcastStickyIntent(intent, null, -1);
        Rlog.d(LOG_TAG, "sendEmergencyCallbackModeChange");
    }

    public void exitEmergencyCallbackMode() {
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
        Rlog.d(LOG_TAG, "exitEmergencyCallbackMode()");
        try {
            this.mCT.getEcbmInterface().exitEmergencyCallbackMode();
        } catch (ImsException e) {
            e.printStackTrace();
        }
    }

    private void handleEnterEmergencyCallbackMode() {
        Rlog.d(LOG_TAG, "handleEnterEmergencyCallbackMode,mIsPhoneInEcmState= " + this.mIsPhoneInEcmState);
        if (!this.mIsPhoneInEcmState) {
            this.mIsPhoneInEcmState = true;
            sendEmergencyCallbackModeChange();
            TelephonyManager.setTelephonyProperty(this.mPhoneId, "ril.cdma.inecmmode", "true");
            postDelayed(this.mExitEcmRunnable, SystemProperties.getLong("ro.cdma.ecmexittimer", 300000));
            this.mWakeLock.acquire();
        }
    }

    private void handleExitEmergencyCallbackMode() {
        Rlog.d(LOG_TAG, "handleExitEmergencyCallbackMode: mIsPhoneInEcmState = " + this.mIsPhoneInEcmState);
        removeCallbacks(this.mExitEcmRunnable);
        if (this.mEcmExitRespRegistrant != null) {
            this.mEcmExitRespRegistrant.notifyResult(Boolean.TRUE);
        }
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
        if (this.mIsPhoneInEcmState) {
            this.mIsPhoneInEcmState = false;
            TelephonyManager.setTelephonyProperty(this.mPhoneId, "ril.cdma.inecmmode", "false");
        }
        sendEmergencyCallbackModeChange();
    }

    void handleTimerInEmergencyCallbackMode(int action) {
        switch (action) {
            case 0:
                postDelayed(this.mExitEcmRunnable, SystemProperties.getLong("ro.cdma.ecmexittimer", 300000));
                ((GsmCdmaPhone) this.mDefaultPhone).notifyEcbmTimerReset(Boolean.FALSE);
                return;
            case 1:
                removeCallbacks(this.mExitEcmRunnable);
                ((GsmCdmaPhone) this.mDefaultPhone).notifyEcbmTimerReset(Boolean.TRUE);
                return;
            default:
                Rlog.e(LOG_TAG, "handleTimerInEmergencyCallbackMode, unsupported action " + action);
                return;
        }
    }

    public void setOnEcbModeExitResponse(Handler h, int what, Object obj) {
        this.mEcmExitRespRegistrant = new Registrant(h, what, obj);
    }

    public void unsetOnEcbModeExitResponse(Handler h) {
        this.mEcmExitRespRegistrant.clear();
    }

    public void onFeatureCapabilityChanged() {
        this.mDefaultPhone.getServiceStateTracker().onImsCapabilityChanged();
    }

    public boolean isVolteEnabled() {
        return this.mCT.isVolteEnabled();
    }

    public boolean isWifiCallingEnabled() {
        return this.mCT.isVowifiEnabled();
    }

    public boolean isVideoEnabled() {
        return this.mCT.isVideoCallEnabled();
    }

    public Phone getDefaultPhone() {
        return this.mDefaultPhone;
    }

    public boolean isImsRegistered() {
        return this.mImsRegistered;
    }

    public void setImsRegistered(boolean value) {
        this.mImsRegistered = value;
        if (this.mImsRegistered) {
            String notificationTag = "wifi_calling";
            ((NotificationManager) this.mContext.getSystemService("notification")).cancel("wifi_calling", 1);
        }
    }

    public void callEndCleanupHandOverCallIfAny() {
        this.mCT.callEndCleanupHandOverCallIfAny();
    }

    public void processDisconnectReason(ImsReasonInfo imsReasonInfo) {
        if (imsReasonInfo.mCode == 1000 && imsReasonInfo.mExtraMessage != null) {
            CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
            if (configManager == null) {
                Rlog.e(LOG_TAG, "processDisconnectReason: CarrierConfigManager is not ready");
                return;
            }
            PersistableBundle pb = configManager.getConfigForSubId(getSubId());
            if (pb == null) {
                Rlog.e(LOG_TAG, "processDisconnectReason: no config for subId " + getSubId());
                return;
            }
            String[] wfcOperatorErrorCodes = pb.getStringArray("wfc_operator_error_codes_string_array");
            if (wfcOperatorErrorCodes != null) {
                String[] wfcOperatorErrorAlertMessages = this.mContext.getResources().getStringArray(17236067);
                String[] wfcOperatorErrorNotificationMessages = this.mContext.getResources().getStringArray(17236068);
                for (int i = 0; i < wfcOperatorErrorCodes.length; i++) {
                    String[] codes = wfcOperatorErrorCodes[i].split("\\|");
                    if (codes.length != 2) {
                        Rlog.e(LOG_TAG, "Invalid carrier config: " + wfcOperatorErrorCodes[i]);
                    } else if (imsReasonInfo.mExtraMessage.startsWith(codes[0])) {
                        int codeStringLength = codes[0].length();
                        if (!Character.isLetterOrDigit(codes[0].charAt(codeStringLength - 1)) || imsReasonInfo.mExtraMessage.length() <= codeStringLength || !Character.isLetterOrDigit(imsReasonInfo.mExtraMessage.charAt(codeStringLength))) {
                            CharSequence title = this.mContext.getText(17039598);
                            int idx = Integer.parseInt(codes[1]);
                            if (idx < 0 || idx >= wfcOperatorErrorAlertMessages.length || idx >= wfcOperatorErrorNotificationMessages.length) {
                                Rlog.e(LOG_TAG, "Invalid index: " + wfcOperatorErrorCodes[i]);
                            } else {
                                CharSequence messageAlert = imsReasonInfo.mExtraMessage;
                                CharSequence messageNotification = imsReasonInfo.mExtraMessage;
                                if (!wfcOperatorErrorAlertMessages[idx].isEmpty()) {
                                    messageAlert = wfcOperatorErrorAlertMessages[idx];
                                }
                                if (!wfcOperatorErrorNotificationMessages[idx].isEmpty()) {
                                    messageNotification = wfcOperatorErrorNotificationMessages[idx];
                                }
                                Intent intent = new Intent("com.android.ims.REGISTRATION_ERROR");
                                intent.putExtra(Phone.EXTRA_KEY_ALERT_TITLE, title);
                                intent.putExtra(Phone.EXTRA_KEY_ALERT_MESSAGE, messageAlert);
                                intent.putExtra(Phone.EXTRA_KEY_NOTIFICATION_MESSAGE, messageNotification);
                                this.mContext.sendOrderedBroadcast(intent, null, this.mResultReceiver, null, -1, null, null);
                            }
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
    }

    public boolean isUtEnabled() {
        return this.mCT.isUtEnabled();
    }

    public void sendEmergencyCallStateChange(boolean callActive) {
        this.mDefaultPhone.sendEmergencyCallStateChange(callActive);
    }

    public void setBroadcastEmergencyCallStateChanges(boolean broadcast) {
        this.mDefaultPhone.setBroadcastEmergencyCallStateChanges(broadcast);
    }

    public WakeLock getWakeLock() {
        return this.mWakeLock;
    }

    public long getVtDataUsage() {
        return this.mCT.getVtDataUsage();
    }

    private void updateRoamingState(boolean newRoaming) {
        if (this.mCT.getState() == PhoneConstants.State.IDLE) {
            Rlog.d(LOG_TAG, "updateRoamingState now: " + newRoaming);
            this.mRoaming = newRoaming;
            ImsManager.setWfcMode(this.mContext, ImsManager.getWfcMode(this.mContext, newRoaming), newRoaming);
            return;
        }
        Rlog.d(LOG_TAG, "updateRoamingState postponed: " + newRoaming);
        this.mCT.registerForVoiceCallEnded(this, 53, null);
    }

    private boolean getCurrentRoaming() {
        return ((TelephonyManager) this.mContext.getSystemService("phone")).isNetworkRoaming();
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("ImsPhone extends:");
        super.dump(fd, pw, args);
        pw.flush();
        pw.println("ImsPhone:");
        pw.println("  mDefaultPhone = " + this.mDefaultPhone);
        pw.println("  mPendingMMIs = " + this.mPendingMMIs);
        pw.println("  mPostDialHandler = " + this.mPostDialHandler);
        pw.println("  mSS = " + this.mSS);
        pw.println("  mWakeLock = " + this.mWakeLock);
        pw.println("  mIsPhoneInEcmState = " + this.mIsPhoneInEcmState);
        pw.println("  mEcmExitRespRegistrant = " + this.mEcmExitRespRegistrant);
        pw.println("  mSilentRedialRegistrants = " + this.mSilentRedialRegistrants);
        pw.println("  mImsRegistered = " + this.mImsRegistered);
        pw.println("  mRoaming = " + this.mRoaming);
        pw.println("  mSsnRegistrants = " + this.mSsnRegistrants);
        pw.flush();
    }

    public Connection dial(List<String> numbers, int videoState) throws CallStateException {
        return this.mCT.dial(numbers, videoState);
    }

    public void hangupAll() throws CallStateException {
        Rlog.d(LOG_TAG, "hangupAll");
        this.mCT.hangupAll();
    }

    public boolean isFeatureSupported(FeatureType feature) {
        if (feature != FeatureType.VOLTE_ENHANCED_CONFERENCE && feature != FeatureType.VIDEO_RESTRICTION && feature != FeatureType.VOLTE_ECT) {
            return feature == FeatureType.VOLTE_CONF_REMOVE_MEMBER;
        } else {
            String[] strArr = new String[5];
            strArr[0] = "46000";
            strArr[1] = "46002";
            strArr[2] = "46004";
            strArr[3] = "46007";
            strArr[4] = "46008";
            List<String> voLteEnhancedConfMccMncList = Arrays.asList(strArr);
            strArr = new String[22];
            strArr[0] = "20205";
            strArr[1] = "20404";
            strArr[2] = "21401";
            strArr[3] = "21406";
            strArr[4] = "21670";
            strArr[5] = "22210";
            strArr[6] = "22601";
            strArr[7] = "23099";
            strArr[8] = "23003";
            strArr[9] = "23415";
            strArr[10] = "23591";
            strArr[11] = "24099";
            strArr[12] = "26204";
            strArr[13] = "26202";
            strArr[14] = "26209";
            strArr[15] = "26801";
            strArr[16] = "27201";
            strArr[17] = "27402";
            strArr[18] = "27403";
            strArr[19] = "27801";
            strArr[20] = "28602";
            strArr[21] = "90128";
            List<String> voLteECTSupportList = Arrays.asList(strArr);
            IccRecords iccRecords = this.mDefaultPhone.getIccRecords();
            if (iccRecords == null) {
                Rlog.d(LOG_TAG, "isFeatureSupported(" + feature + ") no iccRecords");
                return false;
            }
            String mccMnc = iccRecords.getOperatorNumeric();
            if (feature == FeatureType.VOLTE_ECT) {
                boolean retECT = voLteECTSupportList.contains(mccMnc);
                Rlog.d(LOG_TAG, "isFeatureSupported(" + feature + "): retECT = " + retECT + " current mccMnc = " + mccMnc);
                return retECT;
            }
            boolean ret = voLteEnhancedConfMccMncList.contains(mccMnc);
            Rlog.d(LOG_TAG, "isFeatureSupported(" + feature + "): ret = " + ret + " current mccMnc = " + mccMnc);
            return ret;
        }
    }

    public boolean isSupportLteEcc() {
        return this.mCT.isSupportLteEcc();
    }

    private boolean is93MDSupport() {
        if (SystemProperties.get("ro.md_auto_setup_ims").equals("1")) {
            return true;
        }
        return false;
    }

    public void setUserAgentToMd() {
        int defaultDataSubId = SubscriptionController.getInstance().getDefaultDataSubId();
        if (this.mDefaultPhone == null || defaultDataSubId != this.mDefaultPhone.getSubId()) {
            Rlog.d(LOG_TAG, "mDefaultPhone is null or not dds, return");
            return;
        }
        String value;
        String name = "user_agent";
        String model = SystemProperties.get("ro.product.model", " ");
        String project = SystemProperties.get("ro.commonsoft.product", " ");
        String version = SystemProperties.get("ro.build.soft.version", " ");
        String androidVersion = SystemProperties.get("ro.build.version.release", " ");
        String firmware = SystemProperties.get("ro.build.ota.versionname", " ");
        String date = SystemProperties.get("ro.build.date.Ymd", " ");
        switch (((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).getConfigForSubId(this.mDefaultPhone.getSubId()).getInt("oppo_user_agent_type", 1)) {
            case 1:
                value = "OPPO_" + model + "_" + version;
                break;
            case 2:
                value = "OPPO_" + model;
                break;
            case 3:
                value = "Telstra OPPO " + model + " Android_" + androidVersion + " " + firmware;
                break;
            case 4:
                value = "Optus OPPO " + model + " Android_" + androidVersion + " " + firmware;
                break;
            case 5:
                value = "OPPO_" + model + "_" + version + "_" + date;
                break;
            default:
                value = "OPPO_" + model + "_" + version;
                break;
        }
        Rlog.d(LOG_TAG, "setUserAgentToMd value : " + value + " for subId : " + this.mDefaultPhone.getSubId());
        Phone phone = this.mDefaultPhone;
        String[] strArr = new String[2];
        strArr[0] = "AT+ECFGSET=\"" + name + "\",\"" + value + "\"";
        strArr[1] = UsimPBMemInfo.STRING_NOT_SET;
        phone.invokeOemRilRequestStrings(strArr, null);
    }

    public void onUpdateRusConfig(boolean needReset) {
        int defaultDataSubId = SubscriptionController.getInstance().getDefaultDataSubId();
        if (this.mDefaultPhone == null || defaultDataSubId != this.mDefaultPhone.getSubId()) {
            Rlog.d(LOG_TAG, "onUpdateRusConfig mDefaultPhone is null or not dds, return");
            return;
        }
        Rlog.d(LOG_TAG, "onUpdateRusConfig");
        if (RusUpdateMtkIms.setConfig() && needReset) {
            resetRadioAndIms(false);
        }
    }

    public void setRusConfig(boolean needReset) {
        this.mConfigHandler.sendMessage(this.mConfigHandler.obtainMessage(2, needReset ? 1 : 0, -1));
    }

    private void resetRadioAndIms(boolean power) {
        Rlog.d(LOG_TAG, "resetRadioAndIms for phoneId " + this.mPhoneId + ", power " + power);
        if (this.mDefaultPhone != null && this.mDefaultPhone.mCi != null) {
            if (power && !this.mDefaultPhone.mCi.getRadioState().isOn()) {
                boolean isAirplanMode = SystemProperties.getBoolean("persist.radio.airplane.mode.on", false);
                if (!this.mDefaultPhone.isRadioOn() && !isAirplanMode) {
                    Rlog.d(LOG_TAG, "resetRadioAndIms turn radio on ");
                    this.mDefaultPhone.mCi.unregisterForRadioStateChanged(this.mConfigHandler);
                    this.mDefaultPhone.setRadioPower(power);
                    ImsManager.updateImsServiceConfig(this.mContext, this.mPhoneId, true);
                    this.isResetSuccess = true;
                }
            } else if (!power) {
                Phone phone = this.mDefaultPhone;
                String[] strArr = new String[2];
                strArr[0] = "AT+EIMSCFG=0,0,0,0,0,0";
                strArr[1] = UsimPBMemInfo.STRING_NOT_SET;
                phone.invokeOemRilRequestStrings(strArr, null);
                Rlog.d(LOG_TAG, "resetRadioAndIms turn radio off");
                this.mDefaultPhone.mCi.registerForRadioStateChanged(this.mConfigHandler, 3, null);
                this.mDefaultPhone.setRadioPower(power);
                this.isResetSuccess = false;
                this.mConfigHandler.sendEmptyMessageDelayed(4, 30000);
            }
        }
    }

    private void handleDelayMessage() {
        if (!(this.mDefaultPhone == null || this.mDefaultPhone.mCi == null)) {
            this.mDefaultPhone.mCi.unregisterForRadioStateChanged(this.mConfigHandler);
        }
        if (!this.isResetSuccess) {
            PreferenceManager.getDefaultSharedPreferences(this.mContext).edit().putString(KEY_LAST_OP + this.mPhoneId, "op_default").apply();
        }
    }
}
