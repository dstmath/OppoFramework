package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.app.BroadcastOptions;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IDeviceIdleController;
import android.os.Message;
import android.os.OppoUsageManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.provider.Settings.Global;
import android.provider.Telephony.Sms.Inbox;
import android.provider.Telephony.Sms.Intents;
import android.service.carrier.ICarrierMessagingCallback.Stub;
import android.telephony.CarrierMessagingServiceManager;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.EventLog;
import com.android.internal.telephony.SmsHeader.ConcatRef;
import com.android.internal.telephony.SmsHeader.PortAddrs;
import com.android.internal.telephony.regionlock.RegionLockConstant;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.util.HexDump;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.mediatek.common.MPlugin;
import com.mediatek.common.sms.IConcatenatedSmsFwkExt;
import com.mediatek.common.sms.TimerRecord;
import com.mediatek.internal.telephony.ppl.IPplSmsFilter;
import com.mediatek.internal.telephony.ppl.PplSmsFilterExtension;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.ByteArrayOutputStream;
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
public abstract class InboundSmsHandler extends StateMachine {
    private static String ACTION_OPEN_SMS_APP = null;
    public static final int ADDRESS_COLUMN = 6;
    private static final String AUTHORITY = "com.coloros.provider.BlackListProvider";
    private static final Uri AUTHORITY_URI = null;
    private static final String COLOR_DEFAULT_MMS_REGIONS = "color_default_mms_regions";
    public static final int COUNT_COLUMN = 5;
    public static final int DATE_COLUMN = 3;
    protected static final boolean DBG = true;
    public static final int DESTINATION_PORT_COLUMN = 2;
    private static final boolean ENG = false;
    private static final int EVENT_BROADCAST_COMPLETE = 3;
    public static final int EVENT_BROADCAST_SMS = 2;
    public static final int EVENT_INJECT_SMS = 8;
    public static final int EVENT_NEW_SMS = 1;
    private static final int EVENT_RELEASE_WAKELOCK = 5;
    private static final int EVENT_RETURN_TO_IDLE = 4;
    public static final int EVENT_START_ACCEPTING_SMS = 6;
    private static final int EVENT_UPDATE_PHONE_OBJECT = 7;
    public static final int ID_COLUMN = 7;
    private static final String[] INITIAL_REGIONS = null;
    public static final int MESSAGE_BODY_COLUMN = 8;
    private static final int NOTIFICATION_ID_NEW_MESSAGE = 1;
    private static final String NOTIFICATION_TAG = "InboundSmsHandler";
    private static final String OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE = "com.android.mms";
    private static final int OEM_SMS_MMS_SHOWDIALOG = 1;
    public static final int PDU_COLUMN = 0;
    private static final String[] PDU_PROJECTION = null;
    private static final String[] PDU_SEQUENCE_PORT_PROJECTION = null;
    public static final int REFERENCE_NUMBER_COLUMN = 4;
    public static final String SELECT_BY_ID = "_id=?";
    public static final String SELECT_BY_REFERENCE = "address=? AND reference_number=? AND count=? AND deleted=0 AND sub_id=?";
    public static final int SEQUENCE_COLUMN = 1;
    public static final int SUB_ID_COLUMN = 9;
    private static final String TABLE_BL_LIST = "bl_list";
    public static String TAG = null;
    private static final Uri URI_BLACKLIST_BLOCK_SMS_AND_CALL = null;
    private static Uri URI_BLACKLIST_LIST = null;
    private static final String URI_PATH_SMS_AND_CALL_BLOCK = "sms_and_call_block";
    private static final boolean VDBG = false;
    private static final int WAKELOCK_TIMEOUT = 3000;
    private static Handler mUiHandler;
    protected static final Uri sRawUri = null;
    protected static final Uri sRawUriPermanentDelete = null;
    private final int DELETE_PERMANENTLY;
    private final int MARK_DELETED;
    protected CellBroadcastHandler mCellBroadcastHandler;
    private IConcatenatedSmsFwkExt mConcatenatedSmsFwkExt;
    protected final Context mContext;
    private final DefaultState mDefaultState;
    private final DeliveringState mDeliveringState;
    IDeviceIdleController mDeviceIdleController;
    private final IdleState mIdleState;
    private OppoUsageManager mOppoUsageManager;
    protected Phone mPhone;
    private BroadcastReceiver mPhonePrivacyLockReceiver;
    private IPplSmsFilter mPplSmsFilter;
    protected Object mRawLock;
    private final ContentResolver mResolver;
    private final boolean mSmsReceiveDisabled;
    private final StartupState mStartupState;
    public SmsStorageMonitor mStorageMonitor;
    private UserManager mUserManager;
    private final WaitingState mWaitingState;
    private final WakeLock mWakeLock;
    private final WapPushOverSms mWapPush;

    /* renamed from: com.android.internal.telephony.InboundSmsHandler$1 */
    class AnonymousClass1 extends BroadcastReceiver {
        final /* synthetic */ InboundSmsHandler this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.InboundSmsHandler.1.<init>(com.android.internal.telephony.InboundSmsHandler):void, dex: 
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
        AnonymousClass1(com.android.internal.telephony.InboundSmsHandler r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.InboundSmsHandler.1.<init>(com.android.internal.telephony.InboundSmsHandler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.1.<init>(com.android.internal.telephony.InboundSmsHandler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.InboundSmsHandler.1.onReceive(android.content.Context, android.content.Intent):void, dex:  in method: com.android.internal.telephony.InboundSmsHandler.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.InboundSmsHandler.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterRange(InstructionCodec.java:985)
            	at com.android.dx.io.instructions.InstructionCodec.access$1100(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$28.decode(InstructionCodec.java:611)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.InboundSmsHandler.1.onReceive(android.content.Context, android.content.Intent):void, dex:  in method: com.android.internal.telephony.InboundSmsHandler.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.1.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    private final class CarrierSmsFilter extends CarrierMessagingServiceManager {
        private final int mDestPort;
        private final byte[][] mPdus;
        private final SmsBroadcastReceiver mSmsBroadcastReceiver;
        private volatile CarrierSmsFilterCallback mSmsFilterCallback;
        private final String mSmsFormat;
        private final int mUploadFlag;
        final /* synthetic */ InboundSmsHandler this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get0(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):int, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        /* renamed from: -get0 */
        static /* synthetic */ int m52-get0(com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get0(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get0(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get1(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):byte[][], dex: 
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
        /* renamed from: -get1 */
        static /* synthetic */ byte[][] m53-get1(com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get1(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):byte[][], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get1(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):byte[][]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get2(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):com.android.internal.telephony.InboundSmsHandler$SmsBroadcastReceiver, dex: 
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
        /* renamed from: -get2 */
        static /* synthetic */ com.android.internal.telephony.InboundSmsHandler.SmsBroadcastReceiver m54-get2(com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get2(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):com.android.internal.telephony.InboundSmsHandler$SmsBroadcastReceiver, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get2(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):com.android.internal.telephony.InboundSmsHandler$SmsBroadcastReceiver");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get3(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):java.lang.String, dex:  in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get3(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):java.lang.String, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get3(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):java.lang.String, dex: 
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
        /* renamed from: -get3 */
        static /* synthetic */ java.lang.String m55-get3(com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get3(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):java.lang.String, dex:  in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get3(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get3(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get4(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):int, dex:  in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get4(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):int, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get4(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):int, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        /* renamed from: -get4 */
        static /* synthetic */ int m56-get4(com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get4(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):int, dex:  in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get4(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.-get4(com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.<init>(com.android.internal.telephony.InboundSmsHandler, byte[][], int, java.lang.String, com.android.internal.telephony.InboundSmsHandler$SmsBroadcastReceiver, int):void, dex: 
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
        CarrierSmsFilter(com.android.internal.telephony.InboundSmsHandler r1, byte[][] r2, int r3, java.lang.String r4, com.android.internal.telephony.InboundSmsHandler.SmsBroadcastReceiver r5, int r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.<init>(com.android.internal.telephony.InboundSmsHandler, byte[][], int, java.lang.String, com.android.internal.telephony.InboundSmsHandler$SmsBroadcastReceiver, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.<init>(com.android.internal.telephony.InboundSmsHandler, byte[][], int, java.lang.String, com.android.internal.telephony.InboundSmsHandler$SmsBroadcastReceiver, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.filterSms(java.lang.String, com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilterCallback):void, dex: 
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
        void filterSms(java.lang.String r1, com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.filterSms(java.lang.String, com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilterCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.filterSms(java.lang.String, com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilterCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.onServiceReady(android.service.carrier.ICarrierMessagingService):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.onServiceReady(android.service.carrier.ICarrierMessagingService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter.onServiceReady(android.service.carrier.ICarrierMessagingService):void");
        }

        CarrierSmsFilter(InboundSmsHandler this$0, byte[][] pdus, int destPort, String smsFormat, SmsBroadcastReceiver smsBroadcastReceiver) {
            this(this$0, pdus, destPort, smsFormat, smsBroadcastReceiver, 0);
        }
    }

    private final class CarrierSmsFilterCallback extends Stub {
        private final CarrierSmsFilter mSmsFilter;
        private final boolean mUserUnlocked;
        final /* synthetic */ InboundSmsHandler this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.<init>(com.android.internal.telephony.InboundSmsHandler, com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter, boolean):void, dex:  in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.<init>(com.android.internal.telephony.InboundSmsHandler, com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter, boolean):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.<init>(com.android.internal.telephony.InboundSmsHandler, com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter, boolean):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        CarrierSmsFilterCallback(com.android.internal.telephony.InboundSmsHandler r1, com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilter r2, boolean r3) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.<init>(com.android.internal.telephony.InboundSmsHandler, com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter, boolean):void, dex:  in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.<init>(com.android.internal.telephony.InboundSmsHandler, com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.<init>(com.android.internal.telephony.InboundSmsHandler, com.android.internal.telephony.InboundSmsHandler$CarrierSmsFilter, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.onDownloadMmsComplete(int):void, dex: 
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
        public void onDownloadMmsComplete(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.onDownloadMmsComplete(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.onDownloadMmsComplete(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.onFilterComplete(int):void, dex: 
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
        public void onFilterComplete(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.onFilterComplete(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.onFilterComplete(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.onSendMmsComplete(int, byte[]):void, dex: 
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
        public void onSendMmsComplete(int r1, byte[] r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.onSendMmsComplete(int, byte[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.onSendMmsComplete(int, byte[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.onSendMultipartSmsComplete(int, int[]):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.onSendMultipartSmsComplete(int, int[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.onSendMultipartSmsComplete(int, int[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.onSendSmsComplete(int, int):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.onSendSmsComplete(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.CarrierSmsFilterCallback.onSendSmsComplete(int, int):void");
        }
    }

    private class DefaultState extends State {
        final /* synthetic */ InboundSmsHandler this$0;

        /* synthetic */ DefaultState(InboundSmsHandler this$0, DefaultState defaultState) {
            this(this$0);
        }

        private DefaultState(InboundSmsHandler this$0) {
            this.this$0 = this$0;
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 7:
                    try {
                        this.this$0.onUpdatePhoneObject((Phone) msg.obj);
                        break;
                    } catch (Exception e) {
                        this.this$0.loge("EVENT_UPDATE_PHONE_OBJECT--exception");
                        break;
                    }
                default:
                    if (msg != null) {
                        this.this$0.loge("processMessage: unhandled message type " + msg.what);
                        break;
                    }
                    break;
            }
            return true;
        }
    }

    private class DeliveringState extends State {
        final /* synthetic */ InboundSmsHandler this$0;

        /* synthetic */ DeliveringState(InboundSmsHandler this$0, DeliveringState deliveringState) {
            this(this$0);
        }

        private DeliveringState(InboundSmsHandler this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.log("entering Delivering state");
        }

        public void exit() {
            this.this$0.log("leaving Delivering state");
        }

        public boolean processMessage(Message msg) {
            this.this$0.log("DeliveringState.processMessage:" + msg.what);
            switch (msg.what) {
                case 1:
                    try {
                        this.this$0.handleNewSms((AsyncResult) msg.obj);
                        this.this$0.sendMessage(4);
                    } catch (Exception e) {
                        this.this$0.log("EVENT_NEW_SMS--exception");
                    }
                    return true;
                case 2:
                    boolean processResult;
                    try {
                        processResult = this.this$0.processMessagePart(msg.obj);
                    } catch (Exception e2) {
                        processResult = false;
                        e2.printStackTrace();
                    }
                    if (processResult) {
                        this.this$0.transitionTo(this.this$0.mWaitingState);
                    } else {
                        this.this$0.log("No broadcast sent on processing EVENT_BROADCAST_SMS in Delivering state. Return to Idle state");
                        this.this$0.sendMessage(4);
                    }
                    return true;
                case 4:
                    this.this$0.transitionTo(this.this$0.mIdleState);
                    return true;
                case 5:
                    this.this$0.mWakeLock.release();
                    if (!this.this$0.mWakeLock.isHeld()) {
                        this.this$0.loge("mWakeLock released while delivering/broadcasting!");
                    }
                    return true;
                case 8:
                    try {
                        this.this$0.handleInjectSms((AsyncResult) msg.obj);
                        this.this$0.sendMessage(4);
                    } catch (Exception e3) {
                        this.this$0.log("EVENT_INJECT_SMS--exception");
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    private class IdleState extends State {
        final /* synthetic */ InboundSmsHandler this$0;

        /* synthetic */ IdleState(InboundSmsHandler this$0, IdleState idleState) {
            this(this$0);
        }

        private IdleState(InboundSmsHandler this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.log("entering Idle state");
            this.this$0.sendMessageDelayed(5, 3000);
        }

        public void exit() {
            this.this$0.mWakeLock.acquire();
            this.this$0.log("acquired wakelock, leaving Idle state");
        }

        public boolean processMessage(Message msg) {
            this.this$0.log("IdleState.processMessage:" + msg.what);
            this.this$0.log("Idle state processing message type " + msg.what);
            switch (msg.what) {
                case 1:
                case 2:
                case 8:
                case 3001:
                    this.this$0.deferMessage(msg);
                    this.this$0.transitionTo(this.this$0.mDeliveringState);
                    return true;
                case 4:
                    return true;
                case 5:
                    this.this$0.mWakeLock.release();
                    if (this.this$0.mWakeLock.isHeld()) {
                        this.this$0.log("mWakeLock is still held after release");
                    } else {
                        this.this$0.log("mWakeLock released");
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    private static class NewMessageNotificationActionReceiver extends BroadcastReceiver {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.InboundSmsHandler.NewMessageNotificationActionReceiver.<init>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private NewMessageNotificationActionReceiver() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.InboundSmsHandler.NewMessageNotificationActionReceiver.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.NewMessageNotificationActionReceiver.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.InboundSmsHandler.NewMessageNotificationActionReceiver.<init>(com.android.internal.telephony.InboundSmsHandler$NewMessageNotificationActionReceiver):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        /* synthetic */ NewMessageNotificationActionReceiver(com.android.internal.telephony.InboundSmsHandler.NewMessageNotificationActionReceiver r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.InboundSmsHandler.NewMessageNotificationActionReceiver.<init>(com.android.internal.telephony.InboundSmsHandler$NewMessageNotificationActionReceiver):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.NewMessageNotificationActionReceiver.<init>(com.android.internal.telephony.InboundSmsHandler$NewMessageNotificationActionReceiver):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.InboundSmsHandler.NewMessageNotificationActionReceiver.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.InboundSmsHandler.NewMessageNotificationActionReceiver.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.NewMessageNotificationActionReceiver.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    private final class SmsBroadcastReceiver extends BroadcastReceiver {
        private long mBroadcastTimeNano;
        private final String mDeleteWhere;
        private final String[] mDeleteWhereArgs;
        final /* synthetic */ InboundSmsHandler this$0;

        SmsBroadcastReceiver(InboundSmsHandler this$0, InboundSmsTracker tracker) {
            this.this$0 = this$0;
            this.mDeleteWhere = tracker.getDeleteWhere();
            this.mDeleteWhereArgs = tracker.getDeleteWhereArgs();
            this.mBroadcastTimeNano = System.nanoTime();
        }

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Rlog.d("sms", "intent == null");
                return;
            }
            String action = intent.getAction();
            try {
                Rlog.d("sms", "onReceive--SmsBroadcastReceiver, intent.getAction=" + action);
                if (!(this.this$0.mOppoUsageManager == null || action == null || (!action.equals("android.provider.Telephony.SMS_DELIVER") && !action.equals("android.provider.Telephony.WAP_PUSH_DELIVER")))) {
                    Rlog.d(InboundSmsHandler.NOTIFICATION_TAG, "accumulate the count of the received sms");
                    this.this$0.mOppoUsageManager.accumulateHistoryCountOfReceivedMsg(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Bundle options;
            int rc;
            if (action.equals("android.provider.Telephony.SMS_DELIVER")) {
                intent.setAction("android.provider.Telephony.SMS_RECEIVED");
                intent.setComponent(null);
                Intent intent2 = intent;
                this.this$0.dispatchIntent(intent2, "android.permission.RECEIVE_SMS", 16, this.this$0.handleSmsWhitelisting(null), this, UserHandle.ALL);
            } else if (action.equals("android.provider.Telephony.WAP_PUSH_DELIVER")) {
                intent.setAction("android.provider.Telephony.WAP_PUSH_RECEIVED");
                intent.setComponent(null);
                options = null;
                try {
                    long duration = this.this$0.mDeviceIdleController.addPowerSaveTempWhitelistAppForMms(this.this$0.mContext.getPackageName(), 0, "mms-broadcast");
                    BroadcastOptions bopts = BroadcastOptions.makeBasic();
                    bopts.setTemporaryAppWhitelistDuration(duration);
                    options = bopts.toBundle();
                } catch (RemoteException e2) {
                }
                String mimeType = intent.getType();
                this.this$0.dispatchIntent(intent, WapPushOverSms.getPermissionForType(mimeType), WapPushOverSms.getAppOpsPermissionForIntent(mimeType), options, this, UserHandle.SYSTEM);
            } else if (action.equals(Intents.PRIVACY_LOCK_SMS_RECEIVED_ACTION)) {
                options = this.this$0.handleSmsWhitelisting(null);
                rc = getResultCode();
                if (rc == 101) {
                    this.this$0.log("[PPL] Reject by phone privacy lock and delete from raw table. Result code:" + rc);
                    this.this$0.deleteFromRawTable(this.mDeleteWhere, this.mDeleteWhereArgs, 2);
                    this.this$0.sendMessage(3);
                } else {
                    if (InboundSmsHandler.ENG) {
                        this.this$0.log("[PPL] Permit to dispatch, send sms default application first. Result code:" + rc);
                    }
                    intent.setAction("android.provider.Telephony.SMS_DELIVER");
                    ComponentName componentName = SmsApplication.getDefaultSmsApplication(this.this$0.mContext, true);
                    if (componentName != null) {
                        intent.setComponent(componentName);
                        this.this$0.log("Delivering SMS to: " + componentName.getPackageName() + " " + componentName.getClassName());
                    }
                    this.this$0.dispatchIntent(intent, "android.permission.RECEIVE_SMS", 16, options, this, UserHandle.OWNER);
                }
            } else {
                if (!("android.intent.action.DATA_SMS_RECEIVED".equals(action) || "android.provider.Telephony.SMS_RECEIVED".equals(action) || "android.intent.action.DATA_SMS_RECEIVED".equals(action) || "android.provider.Telephony.WAP_PUSH_RECEIVED".equals(action))) {
                    this.this$0.loge("unexpected BroadcastReceiver action: " + action);
                }
                rc = getResultCode();
                if (rc == -1 || rc == 1) {
                    this.this$0.log("successful broadcast, deleting from raw table.");
                } else {
                    this.this$0.loge("a broadcast receiver set the result code to " + rc + ", deleting from raw table anyway!");
                }
                this.this$0.deleteFromRawTable(this.mDeleteWhere, this.mDeleteWhereArgs, 2);
                this.this$0.sendMessage(3);
                int durationMillis = (int) ((System.nanoTime() - this.mBroadcastTimeNano) / 1000000);
                if (durationMillis >= RegionLockConstant.EVENT_NETWORK_LOCK_STATUS) {
                    this.this$0.loge("Slow ordered broadcast completion time: " + durationMillis + " ms");
                } else {
                    this.this$0.log("ordered broadcast completed in: " + durationMillis + " ms");
                }
            }
        }
    }

    private class StartupState extends State {
        final /* synthetic */ InboundSmsHandler this$0;

        /* synthetic */ StartupState(InboundSmsHandler this$0, StartupState startupState) {
            this(this$0);
        }

        private StartupState(InboundSmsHandler this$0) {
            this.this$0 = this$0;
        }

        public boolean processMessage(Message msg) {
            this.this$0.log("StartupState.processMessage:" + msg.what);
            switch (msg.what) {
                case 1:
                case 2:
                case 8:
                case 3001:
                    this.this$0.deferMessage(msg);
                    return true;
                case 6:
                    this.this$0.transitionTo(this.this$0.mIdleState);
                    return true;
                default:
                    return false;
            }
        }
    }

    private static class UIHandler extends Handler {
        private Context context;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.InboundSmsHandler.UIHandler.<init>(android.content.Context):void, dex: 
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
        public UIHandler(android.content.Context r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.InboundSmsHandler.UIHandler.<init>(android.content.Context):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.UIHandler.<init>(android.content.Context):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.InboundSmsHandler.UIHandler.handleMessage(android.os.Message):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void handleMessage(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.InboundSmsHandler.UIHandler.handleMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.UIHandler.handleMessage(android.os.Message):void");
        }
    }

    private class WaitingState extends State {
        final /* synthetic */ InboundSmsHandler this$0;

        /* synthetic */ WaitingState(InboundSmsHandler this$0, WaitingState waitingState) {
            this(this$0);
        }

        private WaitingState(InboundSmsHandler this$0) {
            this.this$0 = this$0;
        }

        public boolean processMessage(Message msg) {
            this.this$0.log("WaitingState.processMessage:" + msg.what);
            switch (msg.what) {
                case 2:
                case 3001:
                    this.this$0.deferMessage(msg);
                    return true;
                case 3:
                    this.this$0.sendMessage(4);
                    this.this$0.transitionTo(this.this$0.mDeliveringState);
                    return true;
                case 4:
                    return true;
                default:
                    return false;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.InboundSmsHandler.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.InboundSmsHandler.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.<clinit>():void");
    }

    protected abstract void acknowledgeLastIncomingSms(boolean z, int i, Message message);

    protected abstract int dispatchMessageRadioSpecific(SmsMessageBase smsMessageBase);

    protected abstract boolean is3gpp2();

    protected InboundSmsHandler(String name, Context context, SmsStorageMonitor storageMonitor, Phone phone, CellBroadcastHandler cellBroadcastHandler) {
        boolean z;
        Context context2;
        super(name);
        this.mDefaultState = new DefaultState(this, null);
        this.mStartupState = new StartupState(this, null);
        this.mIdleState = new IdleState(this, null);
        this.mDeliveringState = new DeliveringState(this, null);
        this.mWaitingState = new WaitingState(this, null);
        this.DELETE_PERMANENTLY = 1;
        this.MARK_DELETED = 2;
        this.mRawLock = new Object();
        this.mConcatenatedSmsFwkExt = null;
        this.mPplSmsFilter = null;
        this.mPhonePrivacyLockReceiver = new AnonymousClass1(this);
        this.mOppoUsageManager = null;
        this.mContext = context;
        this.mStorageMonitor = storageMonitor;
        this.mPhone = phone;
        this.mCellBroadcastHandler = cellBroadcastHandler;
        this.mResolver = context.getContentResolver();
        this.mWapPush = new WapPushOverSms(context);
        try {
            this.mOppoUsageManager = OppoUsageManager.getOppoUsageManager();
        } catch (Exception e) {
        }
        if (TelephonyManager.from(this.mContext).getSmsReceiveCapableForPhone(this.mPhone.getPhoneId(), this.mContext.getResources().getBoolean(17956957))) {
            z = false;
        } else {
            z = true;
        }
        this.mSmsReceiveDisabled = z;
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, name);
        this.mWakeLock.acquire();
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mDeviceIdleController = TelephonyComponentFactory.getInstance().getIDeviceIdleController();
        addState(this.mDefaultState);
        addState(this.mStartupState, this.mDefaultState);
        addState(this.mIdleState, this.mDefaultState);
        addState(this.mDeliveringState, this.mDefaultState);
        addState(this.mWaitingState, this.mDeliveringState);
        setInitialState(this.mStartupState);
        log("created InboundSmsHandler");
        if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
            try {
                this.mConcatenatedSmsFwkExt = (IConcatenatedSmsFwkExt) MPlugin.createInstance(IConcatenatedSmsFwkExt.class.getName(), this.mContext);
                if (this.mConcatenatedSmsFwkExt != null) {
                    this.mConcatenatedSmsFwkExt.setPhoneId(this.mPhone.getPhoneId());
                    log("initial IConcatenatedSmsFwkExt done, actual class name is " + this.mConcatenatedSmsFwkExt.getClass().getName());
                } else {
                    log("FAIL! intial mConcatenatedSmsFwkExt");
                }
            } catch (RuntimeException e2) {
                loge("FAIL! No IConcatenatedSmsFwkExt");
            }
        }
        this.mPplSmsFilter = new PplSmsFilterExtension(this.mContext);
        if (SmsConstants.isPrivacyLockSupport()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intents.PRIVACY_LOCK_SMS_RECEIVED_ACTION);
            this.mContext.registerReceiver(this.mPhonePrivacyLockReceiver, filter);
        }
        Context context3 = this.mContext;
        if (this.mPhone == null) {
            context2 = null;
        } else {
            context2 = this.mPhone.getContext();
        }
        initUIHandler(context3, context2);
    }

    public void dispose() {
        quit();
    }

    public void updatePhoneObject(Phone phone) {
        sendMessage(7, phone);
    }

    protected void onQuitting() {
        this.mWapPush.dispose();
        while (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
        if (SmsConstants.isPrivacyLockSupport()) {
            this.mContext.unregisterReceiver(this.mPhonePrivacyLockReceiver);
        }
    }

    public Phone getPhone() {
        return this.mPhone;
    }

    private void handleNewSms(AsyncResult ar) {
        boolean handled = true;
        if (ar.exception != null) {
            loge("Exception processing incoming SMS: " + ar.exception);
            return;
        }
        int result;
        try {
            result = dispatchMessage(ar.result.mWrappedSmsMessage);
        } catch (RuntimeException ex) {
            loge("Exception dispatching message", ex);
            result = 2;
        }
        if (result != -1) {
            if (result != 1) {
                handled = false;
            }
            notifyAndAcknowledgeLastIncomingSms(handled, result, null);
        }
    }

    private void handleInjectSms(AsyncResult ar) {
        int result;
        PendingIntent pendingIntent = null;
        try {
            pendingIntent = (PendingIntent) ar.userObj;
            SmsMessage sms = ar.result;
            if (sms == null) {
                result = 2;
            } else {
                result = dispatchMessage(sms.mWrappedSmsMessage);
            }
        } catch (RuntimeException ex) {
            loge("Exception dispatching message", ex);
            result = 2;
        }
        if (pendingIntent != null) {
            try {
                pendingIntent.send(result);
            } catch (CanceledException e) {
            }
        }
    }

    private int dispatchMessage(SmsMessageBase smsb) {
        if (smsb == null) {
            loge("dispatchSmsMessage: message is null");
            return 2;
        } else if (this.mSmsReceiveDisabled) {
            log("Received short message on device which doesn't support receiving SMS. Ignored.");
            return 1;
        } else {
            boolean onlyCore = false;
            try {
                onlyCore = IPackageManager.Stub.asInterface(ServiceManager.getService("package")).isOnlyCoreApps();
            } catch (RemoteException e) {
            }
            if (!onlyCore) {
                return dispatchMessageRadioSpecific(smsb);
            }
            log("Received a short message in encrypted state. Rejecting.");
            return 2;
        }
    }

    protected void onUpdatePhoneObject(Phone phone) {
        this.mPhone = phone;
        this.mStorageMonitor = this.mPhone.mSmsStorageMonitor;
        log("onUpdatePhoneObject: phone=" + this.mPhone.getClass().getSimpleName());
    }

    private void notifyAndAcknowledgeLastIncomingSms(boolean success, int result, Message response) {
        if (!success) {
            Intent intent = new Intent("android.provider.Telephony.SMS_REJECTED");
            intent.putExtra("result", result);
            this.mContext.sendBroadcast(intent, "android.permission.RECEIVE_SMS");
        }
        acknowledgeLastIncomingSms(success, result, response);
    }

    protected int dispatchNormalMessage(SmsMessageBase sms) {
        InboundSmsTracker tracker;
        SmsHeader smsHeader = sms.getUserDataHeader();
        if (smsHeader == null || smsHeader.concatRef == null) {
            int destPort = -1;
            if (!(smsHeader == null || smsHeader.portAddrs == null)) {
                destPort = smsHeader.portAddrs.destPort;
                log("destination port: " + destPort);
            }
            tracker = TelephonyComponentFactory.getInstance().makeInboundSmsTracker(this.mPhone.getSubId(), sms.getPdu(), sms.getTimestampMillis(), destPort, is3gpp2(), false, sms.getDisplayOriginatingAddress(), sms.getMessageBody());
        } else {
            ConcatRef concatRef = smsHeader.concatRef;
            PortAddrs portAddrs = smsHeader.portAddrs;
            tracker = TelephonyComponentFactory.getInstance().makeInboundSmsTracker(this.mPhone.getSubId(), sms.getPdu(), sms.getTimestampMillis(), portAddrs != null ? portAddrs.destPort : -1, is3gpp2(), sms.getDisplayOriginatingAddress(), concatRef.refNumber, concatRef.seqNumber, concatRef.msgCount, false, sms.getMessageBody());
        }
        return addTrackerToRawTableAndSendMessage(tracker, tracker.getDestPort() == -1);
    }

    protected int addTrackerToRawTableAndSendMessage(InboundSmsTracker tracker, boolean deDup) {
        switch (addTrackerToRawTable(tracker, deDup)) {
            case 1:
                sendMessage(2, tracker);
                return 1;
            case 5:
                return 1;
            default:
                return 2;
        }
    }

    private boolean processMessagePart(InboundSmsTracker tracker) {
        Cursor cursor;
        int messageCount = tracker.getMessageCount();
        int destPort = tracker.getDestPort();
        try {
            loge("pp__isDupCheckRequired: " + tracker.isDupCheckRequired());
            if (tracker.isDupCheckRequired()) {
                loge("pp__DeleteWhere: " + tracker.getDeleteWhere() + "DeleteWhereargs: " + tracker.getDeleteWhereArgs());
                boolean processMsg = true;
                cursor = this.mResolver.query(sRawUri, null, tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), null);
                if (!(cursor == null || cursor.moveToNext())) {
                    processMsg = false;
                }
                loge("pp__proced: " + processMsg);
                if (cursor != null) {
                    cursor.close();
                }
                if (!processMsg) {
                    return false;
                }
                loge("pp__END of duplicate checking");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Object[] objArr;
        Object[] objArr2;
        if (messageCount <= 0) {
            objArr = new Object[3];
            objArr[0] = "72298611";
            objArr[1] = Integer.valueOf(-1);
            objArr2 = new Object[1];
            objArr2[0] = Integer.valueOf(messageCount);
            objArr[2] = String.format("processMessagePart: invalid messageCount = %d", objArr2);
            EventLog.writeEvent(1397638484, objArr);
            return false;
        }
        byte[][] pdus;
        if (messageCount == 1) {
            pdus = new byte[1][];
            pdus[0] = tracker.getPdu();
        } else {
            synchronized (this.mRawLock) {
                cursor = null;
                try {
                    String address = tracker.getAddress();
                    String refNumber = Integer.toString(tracker.getReferenceNumber());
                    String count = Integer.toString(tracker.getMessageCount());
                    String subId = Integer.toString(this.mPhone.getSubId());
                    String[] whereArgs = new String[4];
                    whereArgs[0] = address;
                    whereArgs[1] = refNumber;
                    whereArgs[2] = count;
                    whereArgs[3] = subId;
                    cursor = this.mResolver.query(sRawUri, PDU_SEQUENCE_PORT_PROJECTION, SELECT_BY_REFERENCE, whereArgs, null);
                    if (cursor.getCount() < messageCount) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return false;
                    }
                    pdus = new byte[messageCount][];
                    while (cursor.moveToNext()) {
                        int index = cursor.getInt(1) - tracker.getIndexOffset();
                        if (index >= pdus.length || index < 0) {
                            objArr = new Object[3];
                            objArr[0] = "72298611";
                            objArr[1] = Integer.valueOf(-1);
                            objArr2 = new Object[2];
                            objArr2[0] = Integer.valueOf(tracker.getIndexOffset() + index);
                            objArr2[1] = Integer.valueOf(messageCount);
                            objArr[2] = String.format("processMessagePart: invalid seqNumber = %d, messageCount = %d", objArr2);
                            EventLog.writeEvent(1397638484, objArr);
                        } else {
                            pdus[index] = HexDump.hexStringToByteArray(cursor.getString(0));
                            if (index == 0 && !cursor.isNull(2)) {
                                int port = InboundSmsTracker.getRealDestPort(cursor.getInt(2));
                                if (port != -1) {
                                    destPort = port;
                                }
                            }
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable e) {
                    loge("Can't access multipart SMS database", e);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return false;
                } catch (Exception e2) {
                    Rlog.d("sms", "processMessagePart--exception, error!!");
                    if (cursor != null) {
                        cursor.close();
                    }
                    return false;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
        List<byte[]> pduList = Arrays.asList(pdus);
        if (pduList.size() == 0 || pduList.contains(null)) {
            loge("processMessagePart: returning false due to " + (pduList.size() == 0 ? "pduList.size() == 0" : "pduList.contains(null)"));
            return false;
        }
        BroadcastReceiver resultReceiver = new SmsBroadcastReceiver(this, tracker);
        if (!this.mUserManager.isUserUnlocked()) {
            return processMessagePartWithUserLocked(tracker, pdus, destPort, resultReceiver);
        }
        if (destPort == SmsHeader.PORT_WAP_PUSH) {
            int result;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            for (byte[] pdu : pdus) {
                byte[] pdu2;
                if (!tracker.is3gpp2()) {
                    SmsMessage msg = SmsMessage.createFromPdu(pdu2, SmsMessage.FORMAT_3GPP);
                    if (msg != null) {
                        pdu2 = msg.getUserData();
                    } else {
                        loge("processMessagePart: SmsMessage.createFromPdu returned null");
                        return false;
                    }
                }
                output.write(pdu2, 0, pdu2.length);
            }
            if (SmsConstants.isWapPushSupport()) {
                log("dispatch wap push pdu with addr & sc addr");
                Bundle bundle = new Bundle();
                if (tracker.is3gpp2WapPdu()) {
                    bundle.putString("address", tracker.getAddress());
                    bundle.putString("service_center", UsimPBMemInfo.STRING_NOT_SET);
                } else {
                    SmsMessage sms = SmsMessage.createFromPdu(pdus[0], tracker.getFormat());
                    if (sms != null) {
                        bundle.putString("address", sms.getOriginatingAddress());
                        String sca = sms.getServiceCenterAddress();
                        if (sca == null) {
                            sca = UsimPBMemInfo.STRING_NOT_SET;
                        }
                        bundle.putString("service_center", sca);
                    }
                }
                result = this.mWapPush.dispatchWapPdu(output.toByteArray(), resultReceiver, this, bundle);
            } else {
                log("dispatch wap push pdu");
                result = this.mWapPush.dispatchWapPdu(output.toByteArray(), resultReceiver, this);
            }
            log("dispatchWapPdu() returned " + result);
            if (result == -1) {
                return true;
            }
            deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 2);
            return false;
        } else if (BlockChecker.isBlocked(this.mContext, tracker.getAddress())) {
            deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 1);
            return false;
        } else {
            try {
                if (!(!isColorOsVersion3X() || this.mContext == null || tracker == null)) {
                    String senderNumber = tracker.getAddress();
                    String tmpAddress = senderNumber;
                    Rlog.d("sms", "color os 3.0 -- sms sender number=" + senderNumber);
                    if (!(senderNumber == null || senderNumber.length() != 13 || senderNumber.charAt(0) == '+')) {
                        if (senderNumber.startsWith("861")) {
                            senderNumber = "+" + senderNumber;
                        }
                    }
                    boolean isNumberBlocked = !TextUtils.isEmpty(senderNumber) ? isInBlackLists(this.mContext, senderNumber) : false;
                    Rlog.d("sms", "sms isNumberBlocked=" + isNumberBlocked + " senderNumber=" + senderNumber);
                    if (isNumberBlocked) {
                        deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 1);
                        return false;
                    }
                    boolean isPolicyMessageReceEnable = OemConstant.isSmsReceiveEnable(this.mPhone);
                    Rlog.d("sms", "isPolicyMessageReceEnable=" + isPolicyMessageReceEnable);
                    if (!isPolicyMessageReceEnable) {
                        deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 1);
                        return false;
                    }
                }
            } catch (Exception e3) {
                Rlog.e("sms", "need check the reason, sms framework");
                e3.printStackTrace();
            }
            if (!filterSms(pdus, destPort, tracker, resultReceiver, true)) {
                oemSetDefaultSms(this.mContext, this.mPhone == null ? null : this.mPhone.getContext());
                dispatchSmsDeliveryIntent(pdus, tracker.getFormat(), destPort, resultReceiver, 0);
            }
            return true;
        }
    }

    private boolean processMessagePartWithUserLocked(InboundSmsTracker tracker, byte[][] pdus, int destPort, SmsBroadcastReceiver resultReceiver) {
        log("Credential-encrypted storage not available. Port: " + destPort);
        if (destPort == SmsHeader.PORT_WAP_PUSH && this.mWapPush.isWapPushForMms(pdus[0], this)) {
            showNewMessageNotification();
            return false;
        } else if (destPort != -1) {
            return false;
        } else {
            if (filterSms(pdus, destPort, tracker, resultReceiver, false)) {
                return true;
            }
            showNewMessageNotification();
            return false;
        }
    }

    private void showNewMessageNotification() {
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            log("Show new message notification.");
            ((NotificationManager) this.mContext.getSystemService("notification")).notify(NOTIFICATION_TAG, 1, new Builder(this.mContext).setSmallIcon(17301646).setAutoCancel(true).setVisibility(1).setDefaults(-1).setContentTitle(this.mContext.getString(17040886)).setContentText(this.mContext.getString(17040887)).setContentIntent(PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_OPEN_SMS_APP), 1073741824)).build());
        }
    }

    static void cancelNewMessageNotification(Context context) {
        ((NotificationManager) context.getSystemService("notification")).cancel(NOTIFICATION_TAG, 1);
    }

    private boolean filterSms(byte[][] pdus, int destPort, InboundSmsTracker tracker, SmsBroadcastReceiver resultReceiver, boolean userUnlocked) {
        List carrierPackages = null;
        UiccCard card = UiccController.getInstance().getUiccCard(this.mPhone.getPhoneId());
        if (card != null) {
            carrierPackages = card.getCarrierPackageNamesForIntent(this.mContext.getPackageManager(), new Intent("android.service.carrier.CarrierMessagingService"));
        } else {
            loge("UiccCard not initialized.");
        }
        CarrierSmsFilter smsFilter;
        if (carrierPackages == null || carrierPackages.size() != 1) {
            List<String> systemPackages = getSystemAppForIntent(new Intent("android.service.carrier.CarrierMessagingService"));
            if (systemPackages == null || systemPackages.size() != 1) {
                logv("Unable to find carrier package: " + carrierPackages + ", nor systemPackages: " + systemPackages);
                if (!VisualVoicemailSmsFilter.filter(this.mContext, pdus, tracker.getFormat(), destPort, this.mPhone.getSubId())) {
                    return false;
                }
                log("Visual voicemail SMS dropped");
                dropSms(resultReceiver);
                return true;
            }
            log("Found system package.");
            smsFilter = new CarrierSmsFilter(this, pdus, destPort, tracker.getFormat(), resultReceiver, 0);
            smsFilter.filterSms((String) systemPackages.get(0), new CarrierSmsFilterCallback(this, smsFilter, userUnlocked));
            return true;
        }
        log("Found carrier package.");
        smsFilter = new CarrierSmsFilter(this, pdus, destPort, tracker.getFormat(), resultReceiver, 0);
        smsFilter.filterSms((String) carrierPackages.get(0), new CarrierSmsFilterCallback(this, smsFilter, userUnlocked));
        return true;
    }

    private List<String> getSystemAppForIntent(Intent intent) {
        List<String> packages = new ArrayList();
        PackageManager packageManager = this.mContext.getPackageManager();
        String carrierFilterSmsPerm = "android.permission.CARRIER_FILTER_SMS";
        for (ResolveInfo info : packageManager.queryIntentServices(intent, 0)) {
            if (info.serviceInfo == null) {
                loge("Can't get service information from " + info);
            } else {
                String packageName = info.serviceInfo.packageName;
                if (packageManager.checkPermission(carrierFilterSmsPerm, packageName) == 0) {
                    packages.add(packageName);
                    log("getSystemAppForIntent: added package " + packageName);
                }
            }
        }
        return packages;
    }

    public void dispatchIntent(Intent intent, String permission, int appOp, Bundle opts, BroadcastReceiver resultReceiver, UserHandle user) {
        if (intent == null || intent.getAction() == null || !intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            intent.addFlags(134217728);
        } else {
            intent.setFlags(0);
        }
        intent.putExtra("rTime", System.currentTimeMillis());
        String action = intent.getAction();
        if ("android.provider.Telephony.SMS_DELIVER".equals(action) || "android.provider.Telephony.SMS_RECEIVED".equals(action) || "android.provider.Telephony.WAP_PUSH_DELIVER".equals(action) || "android.provider.Telephony.WAP_PUSH_RECEIVED".equals(action)) {
            intent.addFlags(268435456);
        }
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        if (user.equals(UserHandle.ALL)) {
            int[] users = null;
            try {
                users = ActivityManagerNative.getDefault().getRunningUserIds();
            } catch (RemoteException e) {
            }
            if (users == null) {
                users = new int[1];
                users[0] = user.getIdentifier();
            }
            for (int i = users.length - 1; i >= 0; i--) {
                UserHandle targetUser = new UserHandle(users[i]);
                if (users[i] != 0) {
                    if (!this.mUserManager.hasUserRestriction("no_sms", targetUser)) {
                        UserInfo info = this.mUserManager.getUserInfo(users[i]);
                        if (info != null) {
                            if (info.isManagedProfile()) {
                            }
                        }
                    }
                }
                this.mContext.sendOrderedBroadcastAsUser(intent, targetUser, permission, appOp, opts, users[i] == 0 ? resultReceiver : null, getHandler(), -1, null, null);
            }
            return;
        }
        this.mContext.sendOrderedBroadcastAsUser(intent, user, permission, appOp, opts, resultReceiver, getHandler(), -1, null, null);
    }

    private void deleteFromRawTable(String deleteWhere, String[] deleteWhereArgs, int deleteType) {
        Uri uri = deleteType == 1 ? sRawUriPermanentDelete : sRawUri;
        if (deleteWhere == null && deleteWhereArgs == null) {
            loge("No rows need be deleted from raw table!");
            return;
        }
        synchronized (this.mRawLock) {
            int rows = this.mResolver.delete(uri, deleteWhere, deleteWhereArgs);
            if (rows == 0) {
                loge("No rows were deleted from raw table!");
            } else {
                log("Deleted " + rows + " rows from raw table.");
            }
        }
    }

    private Bundle handleSmsWhitelisting(ComponentName target) {
        String pkgName;
        String reason;
        if (target != null) {
            pkgName = target.getPackageName();
            reason = "sms-app";
        } else {
            pkgName = this.mContext.getPackageName();
            reason = "sms-broadcast";
        }
        try {
            long duration = this.mDeviceIdleController.addPowerSaveTempWhitelistAppForSms(pkgName, 0, reason);
            BroadcastOptions bopts = BroadcastOptions.makeBasic();
            bopts.setTemporaryAppWhitelistDuration(duration);
            return bopts.toBundle();
        } catch (RemoteException e) {
            return null;
        }
    }

    private void dispatchSmsDeliveryIntent(byte[][] pdus, String format, int destPort, BroadcastReceiver resultReceiver, int longSmsUploadFlag) {
        Intent intent = new Intent();
        intent.putExtra(IPplSmsFilter.KEY_PDUS, pdus);
        intent.putExtra("format", format);
        if (destPort == -1) {
            intent.setAction("android.provider.Telephony.SMS_DELIVER");
            ComponentName componentName = SmsApplication.getDefaultSmsApplication(this.mContext, true);
            if (componentName != null) {
                intent.setComponent(componentName);
                log("Delivering SMS to: " + componentName.getPackageName() + " " + componentName.getClassName());
            } else {
                intent.setComponent(null);
            }
            if (SmsManager.getDefault().getAutoPersisting()) {
                Uri uri = writeInboxMessage(intent);
                if (uri != null) {
                    intent.putExtra("uri", uri.toString());
                }
            }
            if (SmsConstants.isPrivacyLockSupport()) {
                intent.setAction(Intents.PRIVACY_LOCK_SMS_RECEIVED_ACTION);
                intent.setComponent(null);
            }
        } else {
            intent.setAction("android.intent.action.DATA_SMS_RECEIVED");
            intent.setData(Uri.parse("sms://localhost:" + destPort));
            intent.setComponent(null);
        }
        dispatchIntent(intent, "android.permission.RECEIVE_SMS", 16, handleSmsWhitelisting(intent.getComponent()), resultReceiver, UserHandle.SYSTEM);
    }

    private boolean duplicateExists(InboundSmsTracker tracker) throws SQLException {
        String where;
        String address = tracker.getAddress();
        String refNumber = Integer.toString(tracker.getReferenceNumber());
        String count = Integer.toString(tracker.getMessageCount());
        String seqNumber = Integer.toString(tracker.getSequenceNumber());
        String date = Long.toString(tracker.getTimestamp());
        String messageBody = tracker.getMessageBody();
        if (tracker.getMessageCount() == 1) {
            where = "address=? AND reference_number=? AND count=? AND sequence=? AND date=? AND message_body=?";
        } else {
            where = "address=? AND reference_number=? AND count=? AND sequence=? AND ((date=? AND message_body=?) OR deleted=0)";
        }
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = this.mResolver;
            Uri uri = sRawUri;
            String[] strArr = PDU_PROJECTION;
            String[] strArr2 = new String[6];
            strArr2[0] = address;
            strArr2[1] = refNumber;
            strArr2[2] = count;
            strArr2[3] = seqNumber;
            strArr2[4] = date;
            strArr2[5] = messageBody;
            cursor = contentResolver.query(uri, strArr, where, strArr2, null);
            if (cursor == null || !cursor.moveToNext()) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            loge("Discarding duplicate message segment, refNumber=" + refNumber + " seqNumber=" + seqNumber + " count=" + count);
            String oldPduString = cursor.getString(0);
            byte[] pdu = tracker.getPdu();
            byte[] oldPdu = HexDump.hexStringToByteArray(oldPduString);
            if (!Arrays.equals(oldPdu, tracker.getPdu())) {
                loge("Warning: dup message segment PDU of length " + pdu.length + " is different from existing PDU of length " + oldPdu.length);
            }
            return true;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int addTrackerToRawTable(InboundSmsTracker tracker, boolean deDup) {
        String address = tracker.getAddress();
        String refNumber = Integer.toString(tracker.getReferenceNumber());
        String count = Integer.toString(tracker.getMessageCount());
        String subId = Integer.toString(this.mPhone.getSubId());
        synchronized (this.mRawLock) {
            if (deDup) {
                Cursor cursor = null;
                try {
                    String seqNumber = Integer.toString(tracker.getSequenceNumber());
                    String date = Long.toString(tracker.getTimestamp());
                    String messageBody = tracker.getMessageBody();
                    String[] strArr = new String[7];
                    strArr[0] = address;
                    strArr[1] = refNumber;
                    strArr[2] = count;
                    strArr[3] = seqNumber;
                    strArr[4] = date;
                    strArr[5] = messageBody;
                    strArr[6] = subId;
                    cursor = this.mResolver.query(sRawUri, PDU_PROJECTION, "address=? AND reference_number=? AND count=? AND sequence=? AND date=? AND message_body=? AND sub_id=?", strArr, null);
                    if (cursor.moveToNext()) {
                        loge("Discarding duplicate message segment, refNumber=" + refNumber + " seqNumber=" + seqNumber + " count=" + count);
                        String oldPduString = cursor.getString(0);
                        byte[] pdu = tracker.getPdu();
                        byte[] oldPdu = HexDump.hexStringToByteArray(oldPduString);
                        if (!Arrays.equals(oldPdu, tracker.getPdu())) {
                            loge("Warning: dup message segment PDU of length " + pdu.length + " is different from existing PDU of length " + oldPdu.length);
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                        return 5;
                    } else if (cursor != null) {
                        cursor.close();
                    }
                } catch (SQLException e) {
                    loge("Can't access SMS database", e);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return 2;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } else {
                logd("Skipped message de-duping logic");
            }
            Uri newUri = this.mResolver.insert(sRawUri, tracker.getContentValues());
            log("URI of new row -> " + newUri);
            try {
                long rowId = ContentUris.parseId(newUri);
                if (tracker.getMessageCount() == 1) {
                    String str = SELECT_BY_ID;
                    String[] strArr2 = new String[1];
                    strArr2[0] = Long.toString(rowId);
                    tracker.setDeleteWhere(str, strArr2);
                } else {
                    String[] deleteWhereArgs = new String[4];
                    deleteWhereArgs[0] = address;
                    deleteWhereArgs[1] = refNumber;
                    deleteWhereArgs[2] = count;
                    deleteWhereArgs[3] = subId;
                    tracker.setDeleteWhere(SELECT_BY_REFERENCE, deleteWhereArgs);
                }
                return 1;
            } catch (Exception e2) {
                loge("error parsing URI for new row: " + newUri, e2);
                return 2;
            }
        }
    }

    static boolean isCurrentFormat3gpp2() {
        return 2 == TelephonyManager.getDefault().getCurrentPhoneType();
    }

    private void dropSms(SmsBroadcastReceiver receiver) {
        deleteFromRawTable(receiver.mDeleteWhere, receiver.mDeleteWhereArgs, 2);
        sendMessage(3);
    }

    private boolean isSkipNotifyFlagSet(int callbackResult) {
        return (callbackResult & 2) > 0;
    }

    protected void log(String s) {
        Rlog.d(getName(), s);
    }

    protected void loge(String s) {
        Rlog.e(getName(), s);
    }

    protected void loge(String s, Throwable e) {
        Rlog.e(getName(), s, e);
    }

    private Uri writeInboxMessage(Intent intent) {
        try {
            SmsMessage[] messages = Intents.getMessagesFromIntent(intent);
            if (messages == null || messages.length < 1) {
                loge("Failed to parse SMS pdu");
                return null;
            }
            int i = 0;
            int length = messages.length;
            while (i < length) {
                try {
                    messages[i].getDisplayMessageBody();
                    i++;
                } catch (NullPointerException e) {
                    loge("NPE inside SmsMessage");
                    return null;
                }
            }
            ContentValues values = parseSmsMessage(messages);
            long identity = Binder.clearCallingIdentity();
            Uri insert;
            try {
                insert = this.mContext.getContentResolver().insert(Inbox.CONTENT_URI, values);
                return insert;
            } catch (Exception e2) {
                insert = "Failed to persist inbox message";
                loge(insert, e2);
                return null;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } catch (Exception e3) {
            Rlog.d(TAG, "writeInboxMessage--exception");
        }
    }

    private static ContentValues parseSmsMessage(SmsMessage[] msgs) {
        int i = 0;
        SmsMessage sms = msgs[0];
        ContentValues values = new ContentValues();
        values.put("address", sms.getDisplayOriginatingAddress());
        values.put("body", buildMessageBodyFromPdus(msgs));
        values.put("date_sent", Long.valueOf(sms.getTimestampMillis()));
        values.put("date", Long.valueOf(System.currentTimeMillis()));
        values.put("protocol", Integer.valueOf(sms.getProtocolIdentifier()));
        values.put("seen", Integer.valueOf(0));
        values.put("read", Integer.valueOf(0));
        String subject = sms.getPseudoSubject();
        if (!TextUtils.isEmpty(subject)) {
            values.put("subject", subject);
        }
        String str = "reply_path_present";
        if (sms.isReplyPathPresent()) {
            i = 1;
        }
        values.put(str, Integer.valueOf(i));
        values.put("service_center", sms.getServiceCenterAddress());
        return values;
    }

    private static String buildMessageBodyFromPdus(SmsMessage[] msgs) {
        int i = 0;
        if (msgs.length == 1) {
            return replaceFormFeeds(msgs[0].getDisplayMessageBody());
        }
        StringBuilder body = new StringBuilder();
        int length = msgs.length;
        while (i < length) {
            body.append(msgs[i].getDisplayMessageBody());
            i++;
        }
        return replaceFormFeeds(body.toString());
    }

    private static String replaceFormFeeds(String s) {
        return s == null ? UsimPBMemInfo.STRING_NOT_SET : s.replace(12, 10);
    }

    public WakeLock getWakeLock() {
        return this.mWakeLock;
    }

    public int getWakeLockTimeout() {
        return WAKELOCK_TIMEOUT;
    }

    /* JADX WARNING: Removed duplicated region for block: B:57:0x018f A:{Catch:{ Exception -> 0x01c2 }} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x018f A:{Catch:{ Exception -> 0x01c2 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean dispatchConcateSmsParts(TimerRecord record) {
        boolean handled = false;
        if (record == null) {
            if (ENG) {
                log("ConcatenatedSmsFwkExt: null TimerRecord in msg");
            }
            return false;
        }
        if (ENG) {
            log("ConcatenatedSmsFwkExt: timer is expired, dispatch existed segments. refNumber = " + record.refNumber);
        }
        InboundSmsTracker smsTracker = record.mTracker;
        SmsBroadcastReceiver receiver = new SmsBroadcastReceiver(this, smsTracker);
        synchronized (this.mRawLock) {
            try {
                byte[][] pdus = this.mConcatenatedSmsFwkExt.queryExistedSegments(record);
                List<byte[]> pduList = Arrays.asList(pdus);
                if (pduList.size() == 0 || pduList.contains(null)) {
                    loge("dispatchConcateSmsParts: returning false due to " + (pduList.size() == 0 ? "pduList.size() == 0" : "pduList.contains(null)"));
                    return false;
                } else if (!this.mUserManager.isUserUnlocked()) {
                    log("dispatchConcateSmsParts: device is still locked so delete segment(s), ref = " + record.refNumber);
                    this.mConcatenatedSmsFwkExt.deleteExistedSegments(record);
                    boolean processMessagePartWithUserLocked = processMessagePartWithUserLocked(smsTracker, pdus, -1, receiver);
                    return processMessagePartWithUserLocked;
                } else if (BlockChecker.isBlocked(this.mContext, smsTracker.getAddress())) {
                    log("dispatchConcateSmsParts: block phone number, number = " + smsTracker.getAddress());
                    this.mConcatenatedSmsFwkExt.deleteExistedSegments(record);
                    deleteFromRawTable(smsTracker.getDeleteWhere(), smsTracker.getDeleteWhereArgs(), 1);
                    return false;
                } else {
                    if (pdus != null) {
                        if (pdus.length > 0) {
                            int flag = this.mConcatenatedSmsFwkExt.getUploadFlag(record);
                            if (flag == 2 || flag == 1) {
                                this.mConcatenatedSmsFwkExt.setUploadFlag(record);
                                List carrierPackages = null;
                                UiccCard card = UiccController.getInstance().getUiccCard(this.mPhone.getPhoneId());
                                if (card != null) {
                                    carrierPackages = card.getCarrierPackageNamesForIntent(this.mContext.getPackageManager(), new Intent("android.service.carrier.CarrierMessagingService"));
                                } else {
                                    loge("UiccCard not initialized.");
                                }
                                List<String> systemPackages = getSystemAppForIntent(new Intent("android.service.carrier.CarrierMessagingService"));
                                CarrierSmsFilter smsFilter;
                                if (carrierPackages == null || carrierPackages.size() != 1) {
                                    if (systemPackages != null) {
                                        if (systemPackages.size() == 1) {
                                            log("Found system package.");
                                            smsFilter = new CarrierSmsFilter(this, pdus, -1, smsTracker.getFormat(), receiver, flag);
                                            smsFilter.filterSms((String) systemPackages.get(0), new CarrierSmsFilterCallback(this, smsFilter, true));
                                        }
                                    }
                                    logv("Unable to find carrier package: " + carrierPackages + ", nor systemPackages: " + systemPackages);
                                    dispatchSmsDeliveryIntent(pdus, smsTracker.getFormat(), -1, receiver, flag);
                                } else {
                                    log("Found carrier package.");
                                    smsFilter = new CarrierSmsFilter(this, pdus, -1, smsTracker.getFormat(), receiver, flag);
                                    smsFilter.filterSms((String) carrierPackages.get(0), new CarrierSmsFilterCallback(this, smsFilter, true));
                                }
                                handled = true;
                                if (ENG) {
                                    log("ConcatenatedSmsFwkExt: delete segment(s), tracker = " + ((InboundSmsTracker) record.mTracker));
                                }
                                this.mConcatenatedSmsFwkExt.deleteExistedSegments(record);
                            } else {
                                if (ENG) {
                                    log("ConcatenatedSmsFwkExt: invalid upload flag");
                                }
                                if (ENG) {
                                }
                                this.mConcatenatedSmsFwkExt.deleteExistedSegments(record);
                            }
                        }
                    }
                    if (ENG) {
                        log("ConcatenatedSmsFwkExt: no pdus to be dispatched");
                    }
                    if (ENG) {
                    }
                    this.mConcatenatedSmsFwkExt.deleteExistedSegments(record);
                }
            } catch (Exception eInner) {
                eInner.printStackTrace();
            }
        }
        return handled;
    }

    protected int phonePrivacyLockCheck(Intent intent) {
        if (!SmsConstants.isPrivacyLockSupport() || !SmsConstants.isPrivacyLockSupport()) {
            return 0;
        }
        Bundle pplData = new Bundle();
        Object[] messages = (Object[]) intent.getExtra(IPplSmsFilter.KEY_PDUS);
        byte[][] pdus = new byte[messages.length][];
        for (int i = 0; i < messages.length; i++) {
            pdus[i] = (byte[]) messages[i];
        }
        pplData.putSerializable(IPplSmsFilter.KEY_PDUS, pdus);
        pplData.putString("format", (String) intent.getExtra("format"));
        pplData.putInt(IPplSmsFilter.KEY_SUB_ID, this.mPhone.getSubId());
        pplData.putInt(IPplSmsFilter.KEY_SMS_TYPE, 0);
        boolean pplResult = this.mPplSmsFilter.pplFilter(pplData);
        if (ENG) {
            log("[Moms] Phone privacy check end, Need to filter(result) = " + pplResult);
        }
        if (pplResult) {
            return -1;
        }
        return 0;
    }

    static void registerNewMessageNotificationActionHandler(Context context) {
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction(ACTION_OPEN_SMS_APP);
        context.registerReceiver(new NewMessageNotificationActionReceiver(), userFilter);
    }

    public static boolean isInBlackLists(Context context, String number) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.withAppendedPath(URI_BLACKLIST_BLOCK_SMS_AND_CALL, number), null, "block_type=1 OR block_type=3", null, null);
            boolean z = cursor.getCount() > 0;
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
            return false;
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

    public static boolean isColorOsVersion3X() {
        try {
            String romversion = SystemProperties.get("ro.build.version.opporom");
            if (romversion == null || romversion.length() == 0) {
                return false;
            }
            return romversion.startsWith("V3.");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private static void oemShowDialogSmsMMs(Context context, Context phoneContext) {
        Message msg = Message.obtain();
        msg.what = 1;
        if (mUiHandler == null) {
            Rlog.e("sms", "warning:oemShowDialogSmsMMs mUiHandler==null");
        } else {
            mUiHandler.sendMessage(msg);
        }
    }

    private static void oemShowDialogSmsMMs(Context context) {
        oemShowDialogSmsMMs(context, context);
    }

    private void initUIHandler(Context context, Context phoneContext) {
        if (!(context == null || phoneContext == null || context == phoneContext)) {
            Rlog.v("sms", "Context != phone.getContext()");
        }
        if (context != null) {
            mUiHandler = new UIHandler(context);
            Rlog.v("sms", "initUIHandler1");
        } else if (phoneContext != null) {
            mUiHandler = new UIHandler(phoneContext);
            Rlog.v("sms", "initUIHandler2");
        }
    }

    public static void oemSetDefaultSms(Context context, Context phoneContext) {
        if (context == null) {
            try {
                Rlog.e("sms", "oemSetDefaultSms error, context == null!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (isDefaultMmsRegion(context)) {
            Rlog.d("sms", "isDefaultMmsRegion-true");
        } else {
            ComponentName lastSmsCompName = SmsApplication.getDefaultSmsApplication(context, true);
            if (lastSmsCompName == null) {
                Rlog.e("sms", "lastSmsCompName == null");
                SmsApplication.setDefaultApplication(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE, context);
                oemShowDialogSmsMMs(context, phoneContext);
                return;
            }
            String lastSmsPackage = lastSmsCompName.getPackageName();
            Rlog.e("sms", "lastSmsPackage=" + lastSmsPackage);
            if (!(lastSmsPackage == null || lastSmsPackage.equals(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE)) || TextUtils.isEmpty(lastSmsPackage)) {
                SmsApplication.setDefaultApplication(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE, context);
                oemShowDialogSmsMMs(context, phoneContext);
            }
        }
    }

    public static void oemSetDefaultWappush(Context context) {
        if (context == null) {
            try {
                Rlog.e("sms", "oemSetDefaultWappush error, context == null!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (isDefaultMmsRegion(context)) {
            Rlog.d("sms", "isDefaultMmsRegion-true");
        } else {
            ComponentName lastWappushsCompName = SmsApplication.getDefaultMmsApplication(context, true);
            if (lastWappushsCompName == null) {
                Rlog.e("sms", "lastWappushsCompName == null");
                SmsApplication.setDefaultApplication(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE, context);
                oemShowDialogSmsMMs(context);
                return;
            }
            String lastWappushPackage = lastWappushsCompName.getPackageName();
            Rlog.e("sms", "lastWappushPackage=" + lastWappushPackage);
            if (!(lastWappushPackage == null || lastWappushPackage.equals(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE)) || TextUtils.isEmpty(lastWappushPackage)) {
                SmsApplication.setDefaultApplication(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE, context);
                oemShowDialogSmsMMs(context);
            }
        }
    }

    public static boolean isDefaultMmsRegion(Context context) {
        if (context == null) {
            return false;
        }
        try {
            if (!context.getPackageManager().hasSystemFeature("oppo.version.exp")) {
                return false;
            }
            String currentRegion = SystemProperties.get("persist.sys.oppo.region", "OC");
            String defaultRegions = Global.getString(context.getContentResolver(), COLOR_DEFAULT_MMS_REGIONS);
            Rlog.d("sms", "currentRegion = " + currentRegion + ", defaultRegions = " + defaultRegions);
            if (TextUtils.isEmpty(defaultRegions) || "null".equals(defaultRegions)) {
                for (String region : INITIAL_REGIONS) {
                    if (!TextUtils.isEmpty(region) && region.equals(currentRegion)) {
                        return true;
                    }
                }
                return false;
            }
            String[] list = defaultRegions.split(";");
            if (list != null && list.length > 0) {
                for (String region2 : list) {
                    if (!TextUtils.isEmpty(region2) && region2.equals(currentRegion)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
