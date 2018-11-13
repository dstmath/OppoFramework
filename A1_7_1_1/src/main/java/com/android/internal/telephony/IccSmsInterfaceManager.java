package com.android.internal.telephony;

import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserManager;
import android.telephony.Rlog;
import android.telephony.SimSmsInsertStatus;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SmsParameters;
import android.util.Log;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.cdma.CdmaSmsBroadcastConfigInfo;
import com.android.internal.telephony.cdma.SmsMessage.SubmitPdu;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.gsm.SmsMessage.DeliverPdu;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.util.HexDump;
import com.mediatek.internal.telephony.IccSmsStorageStatus;
import com.mediatek.internal.telephony.SmsCbConfigInfo;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
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
public class IccSmsInterfaceManager {
    private static final int CB_ACTIVATION_OFF = 0;
    private static final int CB_ACTIVATION_ON = 1;
    private static final int CB_ACTIVATION_UNKNOWN = -1;
    static final boolean DBG = true;
    private static final int EVENT_GET_BROADCAST_ACTIVATION_DONE = 108;
    private static final int EVENT_GET_BROADCAST_CONFIG_CHANNEL_DONE = 110;
    private static final int EVENT_GET_BROADCAST_CONFIG_DONE = 107;
    private static final int EVENT_GET_BROADCAST_CONFIG_LANGUAGE_DONE = 112;
    private static final int EVENT_GET_SMS_PARAMS = 104;
    private static final int EVENT_GET_SMS_SIM_MEM_STATUS_DONE = 102;
    private static final int EVENT_INSERT_TEXT_MESSAGE_TO_ICC_DONE = 103;
    private static final int EVENT_LOAD_DONE = 1;
    private static final int EVENT_LOAD_ONE_RECORD_DONE = 106;
    private static final int EVENT_REMOVE_BROADCAST_MSG_DONE = 109;
    protected static final int EVENT_SET_BROADCAST_ACTIVATION_DONE = 3;
    protected static final int EVENT_SET_BROADCAST_CONFIG_DONE = 4;
    private static final int EVENT_SET_BROADCAST_CONFIG_LANGUAGE_DONE = 111;
    private static final int EVENT_SET_ETWS_CONFIG_DONE = 101;
    private static final int EVENT_SET_SMS_PARAMS = 105;
    private static final int EVENT_SIM_SMS_DELETE_DONE = 100;
    private static final int EVENT_UPDATE_DONE = 2;
    private static final String INDEXT_SPLITOR = ",";
    static final String LOG_TAG = "IccSmsInterfaceManager";
    private static final int SMS_CB_CODE_SCHEME_MAX = 255;
    private static final int SMS_CB_CODE_SCHEME_MIN = 0;
    private static int sConcatenatedRef;
    protected final AppOpsManager mAppOps;
    private CdmaBroadcastRangeManager mCdmaBroadcastRangeManager;
    private CellBroadcastRangeManager mCellBroadcastRangeManager;
    protected final Context mContext;
    private int mCurrentCellBroadcastActivation;
    protected SMSDispatcher mDispatcher;
    protected Handler mHandler;
    private boolean mInsertMessageSuccess;
    protected final Object mLoadLock;
    protected final Object mLock;
    protected Phone mPhone;
    private final Object mSimInsertLock;
    private IccSmsStorageStatus mSimMemStatus;
    private List<SmsRawData> mSms;
    private SmsBroadcastConfigInfo[] mSmsCBConfig;
    private String mSmsCbChannelConfig;
    private String mSmsCbLanguageConfig;
    private SmsParameters mSmsParams;
    private boolean mSmsParamsSuccess;
    private SmsRawData mSmsRawData;
    private BroadcastReceiver mSmsWipeReceiver;
    protected boolean mSuccess;
    private final UserManager mUserManager;
    private SimSmsInsertStatus smsInsertRet;
    private SimSmsInsertStatus smsInsertRet2;

    /* renamed from: com.android.internal.telephony.IccSmsInterfaceManager$2 */
    class AnonymousClass2 extends BroadcastReceiver {
        final /* synthetic */ IccSmsInterfaceManager this$0;

        /* renamed from: com.android.internal.telephony.IccSmsInterfaceManager$2$1 */
        class AnonymousClass1 extends Thread {
            final /* synthetic */ AnonymousClass2 this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.IccSmsInterfaceManager.2.1.<init>(com.android.internal.telephony.IccSmsInterfaceManager$2):void, dex: 
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
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            AnonymousClass1(com.android.internal.telephony.IccSmsInterfaceManager.AnonymousClass2 r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.IccSmsInterfaceManager.2.1.<init>(com.android.internal.telephony.IccSmsInterfaceManager$2):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IccSmsInterfaceManager.2.1.<init>(com.android.internal.telephony.IccSmsInterfaceManager$2):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.IccSmsInterfaceManager.2.1.run():void, dex: 
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
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.IccSmsInterfaceManager.2.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IccSmsInterfaceManager.2.1.run():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.IccSmsInterfaceManager.2.<init>(com.android.internal.telephony.IccSmsInterfaceManager):void, dex: 
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
        AnonymousClass2(com.android.internal.telephony.IccSmsInterfaceManager r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.IccSmsInterfaceManager.2.<init>(com.android.internal.telephony.IccSmsInterfaceManager):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IccSmsInterfaceManager.2.<init>(com.android.internal.telephony.IccSmsInterfaceManager):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.IccSmsInterfaceManager.2.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.IccSmsInterfaceManager.2.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IccSmsInterfaceManager.2.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    class CdmaBroadcastRangeManager extends IntRangeManager {
        private ArrayList<CdmaSmsBroadcastConfigInfo> mConfigList;
        final /* synthetic */ IccSmsInterfaceManager this$0;

        CdmaBroadcastRangeManager(IccSmsInterfaceManager this$0) {
            this.this$0 = this$0;
            this.mConfigList = new ArrayList();
        }

        protected void startUpdate() {
            this.mConfigList.clear();
        }

        protected void addRange(int startId, int endId, boolean selected) {
            this.mConfigList.add(new CdmaSmsBroadcastConfigInfo(startId, endId, 1, selected));
        }

        protected boolean finishUpdate() {
            if (this.mConfigList.isEmpty()) {
                return true;
            }
            return this.this$0.setCdmaBroadcastConfig((CdmaSmsBroadcastConfigInfo[]) this.mConfigList.toArray(new CdmaSmsBroadcastConfigInfo[this.mConfigList.size()]));
        }
    }

    class CellBroadcastRangeManager extends IntRangeManager {
        private ArrayList<SmsBroadcastConfigInfo> mConfigList;
        final /* synthetic */ IccSmsInterfaceManager this$0;

        CellBroadcastRangeManager(IccSmsInterfaceManager this$0) {
            this.this$0 = this$0;
            this.mConfigList = new ArrayList();
        }

        protected void startUpdate() {
            this.mConfigList.clear();
        }

        protected void addRange(int startId, int endId, boolean selected) {
            this.mConfigList.add(new SmsBroadcastConfigInfo(startId, endId, 0, 255, selected));
        }

        protected boolean finishUpdate() {
            if (this.mConfigList.isEmpty()) {
                return this.this$0.setCellBroadcastConfig(new SmsBroadcastConfigInfo[0]);
            }
            return this.this$0.setCellBroadcastConfig((SmsBroadcastConfigInfo[]) this.mConfigList.toArray(new SmsBroadcastConfigInfo[this.mConfigList.size()]));
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.IccSmsInterfaceManager.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.IccSmsInterfaceManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IccSmsInterfaceManager.<clinit>():void");
    }

    protected IccSmsInterfaceManager(Phone phone) {
        this.mLock = new Object();
        this.mLoadLock = new Object();
        this.mCurrentCellBroadcastActivation = -1;
        this.mCellBroadcastRangeManager = new CellBroadcastRangeManager(this);
        this.mCdmaBroadcastRangeManager = new CdmaBroadcastRangeManager(this);
        this.mSimInsertLock = new Object();
        this.smsInsertRet = new SimSmsInsertStatus(0, UsimPBMemInfo.STRING_NOT_SET);
        this.smsInsertRet2 = new SimSmsInsertStatus(0, UsimPBMemInfo.STRING_NOT_SET);
        this.mSmsParams = null;
        this.mSmsParamsSuccess = false;
        this.mSmsRawData = null;
        this.mSmsCBConfig = null;
        this.mSmsCbChannelConfig = UsimPBMemInfo.STRING_NOT_SET;
        this.mSmsCbLanguageConfig = UsimPBMemInfo.STRING_NOT_SET;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                AsyncResult ar;
                Object obj;
                int index;
                SimSmsInsertStatus -get7;
                ArrayList<SmsBroadcastConfigInfo> mList;
                switch (msg.what) {
                    case 1:
                        ar = (AsyncResult) msg.obj;
                        obj = IccSmsInterfaceManager.this.mLoadLock;
                        synchronized (obj) {
                            if (ar.exception == null) {
                                IccSmsInterfaceManager.this.mSms = IccSmsInterfaceManager.this.buildValidRawData((ArrayList) ar.result);
                                IccSmsInterfaceManager.this.markMessagesAsRead((ArrayList) ar.result);
                            } else {
                                if (Rlog.isLoggable("SMS", 3)) {
                                    IccSmsInterfaceManager.this.log("Cannot load Sms records");
                                }
                                IccSmsInterfaceManager.this.mSms = null;
                            }
                            IccSmsInterfaceManager.this.mLoadLock.notifyAll();
                            break;
                        }
                    case 2:
                        ar = msg.obj;
                        synchronized (IccSmsInterfaceManager.this.mLock) {
                            IccSmsInterfaceManager.this.mSuccess = ar.exception == null;
                            if (IccSmsInterfaceManager.this.mSuccess) {
                                try {
                                    index = ((int[]) ar.result)[0];
                                    -get7 = IccSmsInterfaceManager.this.smsInsertRet2;
                                    -get7.indexInIcc += index + IccSmsInterfaceManager.INDEXT_SPLITOR;
                                    IccSmsInterfaceManager.this.log("[insertRaw save one pdu in index " + index);
                                } catch (ClassCastException e) {
                                    e.printStackTrace();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                IccSmsInterfaceManager.this.log("[insertRaw fail to insert raw into ICC");
                                -get7 = IccSmsInterfaceManager.this.smsInsertRet2;
                                -get7.indexInIcc += "-1,";
                            }
                            IccSmsInterfaceManager.this.mLock.notifyAll();
                        }
                        if (ar.exception != null) {
                            CommandException e2 = ar.exception;
                            IccSmsInterfaceManager.this.log("Cannot update SMS " + e2.getCommandError());
                            if (e2.getCommandError() == Error.SIM_FULL) {
                                IccSmsInterfaceManager.this.mDispatcher.handleIccFull();
                                return;
                            }
                            return;
                        }
                        return;
                    case 3:
                    case 4:
                    case 101:
                    case 111:
                        ar = (AsyncResult) msg.obj;
                        obj = IccSmsInterfaceManager.this.mLock;
                        synchronized (obj) {
                            IccSmsInterfaceManager.this.mSuccess = ar.exception == null;
                            IccSmsInterfaceManager.this.mLock.notifyAll();
                            break;
                        }
                    case 102:
                        ar = (AsyncResult) msg.obj;
                        obj = IccSmsInterfaceManager.this.mLock;
                        synchronized (obj) {
                            if (ar.exception == null) {
                                IccSmsInterfaceManager.this.mSuccess = true;
                                if (IccSmsInterfaceManager.this.mSimMemStatus == null) {
                                    IccSmsInterfaceManager.this.mSimMemStatus = new IccSmsStorageStatus();
                                }
                                IccSmsStorageStatus tmpStatus = ar.result;
                                IccSmsInterfaceManager.this.mSimMemStatus.mUsed = tmpStatus.mUsed;
                                IccSmsInterfaceManager.this.mSimMemStatus.mTotal = tmpStatus.mTotal;
                            } else {
                                IccSmsInterfaceManager.this.log("Cannot Get Sms SIM Memory Status from SIM");
                            }
                            IccSmsInterfaceManager.this.mLock.notifyAll();
                            break;
                        }
                    case 103:
                        ar = (AsyncResult) msg.obj;
                        obj = IccSmsInterfaceManager.this.mSimInsertLock;
                        synchronized (obj) {
                            IccSmsInterfaceManager.this.mInsertMessageSuccess = ar.exception == null;
                            if (IccSmsInterfaceManager.this.mInsertMessageSuccess) {
                                try {
                                    index = ((int[]) ar.result)[0];
                                    -get7 = IccSmsInterfaceManager.this.smsInsertRet;
                                    -get7.indexInIcc += index + IccSmsInterfaceManager.INDEXT_SPLITOR;
                                    IccSmsInterfaceManager.this.log("insertText save one pdu in index " + index);
                                } catch (ClassCastException e3) {
                                    e3.printStackTrace();
                                } catch (Exception ex2) {
                                    ex2.printStackTrace();
                                }
                            } else {
                                IccSmsInterfaceManager.this.log("insertText fail to insert sms into ICC");
                                -get7 = IccSmsInterfaceManager.this.smsInsertRet;
                                -get7.indexInIcc += "-1,";
                            }
                            IccSmsInterfaceManager.this.mSimInsertLock.notifyAll();
                            break;
                        }
                    case 104:
                        ar = (AsyncResult) msg.obj;
                        obj = IccSmsInterfaceManager.this.mLock;
                        synchronized (obj) {
                            if (ar.exception == null) {
                                try {
                                    IccSmsInterfaceManager.this.mSmsParams = (SmsParameters) ar.result;
                                } catch (ClassCastException e32) {
                                    IccSmsInterfaceManager.this.log("[EFsmsp fail to get sms params ClassCastException");
                                    e32.printStackTrace();
                                } catch (Exception ex22) {
                                    IccSmsInterfaceManager.this.log("[EFsmsp fail to get sms params Exception");
                                    ex22.printStackTrace();
                                }
                            } else {
                                IccSmsInterfaceManager.this.log("[EFsmsp fail to get sms params");
                                IccSmsInterfaceManager.this.mSmsParams = null;
                            }
                            IccSmsInterfaceManager.this.mLock.notifyAll();
                            break;
                        }
                    case 105:
                        ar = (AsyncResult) msg.obj;
                        obj = IccSmsInterfaceManager.this.mLock;
                        synchronized (obj) {
                            if (ar.exception == null) {
                                IccSmsInterfaceManager.this.mSmsParamsSuccess = true;
                            } else {
                                IccSmsInterfaceManager.this.log("[EFsmsp fail to set sms params");
                                IccSmsInterfaceManager.this.mSmsParamsSuccess = false;
                            }
                            IccSmsInterfaceManager.this.mLock.notifyAll();
                            break;
                        }
                    case 106:
                        ar = (AsyncResult) msg.obj;
                        obj = IccSmsInterfaceManager.this.mLock;
                        synchronized (obj) {
                            if (ar.exception == null) {
                                try {
                                    byte[] rawData = ar.result;
                                    if (rawData[0] == (byte) 0) {
                                        IccSmsInterfaceManager.this.log("sms raw data status is FREE");
                                        IccSmsInterfaceManager.this.mSmsRawData = null;
                                    } else {
                                        IccSmsInterfaceManager.this.mSmsRawData = new SmsRawData(rawData);
                                    }
                                } catch (ClassCastException e322) {
                                    IccSmsInterfaceManager.this.log("fail to get sms raw data ClassCastException");
                                    e322.printStackTrace();
                                    IccSmsInterfaceManager.this.mSmsRawData = null;
                                }
                            } else {
                                IccSmsInterfaceManager.this.log("fail to get sms raw data rild");
                                IccSmsInterfaceManager.this.mSmsRawData = null;
                            }
                            IccSmsInterfaceManager.this.mLock.notifyAll();
                            break;
                        }
                    case 107:
                        ar = (AsyncResult) msg.obj;
                        obj = IccSmsInterfaceManager.this.mLock;
                        synchronized (obj) {
                            if (ar.exception == null) {
                                mList = ar.result;
                                if (mList.size() != 0) {
                                    IccSmsInterfaceManager.this.mSmsCBConfig = new SmsBroadcastConfigInfo[mList.size()];
                                    mList.toArray(IccSmsInterfaceManager.this.mSmsCBConfig);
                                    if (IccSmsInterfaceManager.this.mSmsCBConfig != null) {
                                        IccSmsInterfaceManager.this.log("config size=" + IccSmsInterfaceManager.this.mSmsCBConfig.length);
                                        for (index = 0; index < IccSmsInterfaceManager.this.mSmsCBConfig.length; index++) {
                                            IccSmsInterfaceManager.this.log("mSmsCBConfig[" + index + "] = " + "Channel id: " + IccSmsInterfaceManager.this.mSmsCBConfig[index].getFromServiceId() + "-" + IccSmsInterfaceManager.this.mSmsCBConfig[index].getToServiceId() + ", " + "Language: " + IccSmsInterfaceManager.this.mSmsCBConfig[index].getFromCodeScheme() + "-" + IccSmsInterfaceManager.this.mSmsCBConfig[index].getToCodeScheme() + ", " + "Selected: " + IccSmsInterfaceManager.this.mSmsCBConfig[index].isSelected());
                                        }
                                    }
                                }
                            } else {
                                IccSmsInterfaceManager.this.log("Cannot Get CB configs");
                            }
                            IccSmsInterfaceManager.this.mLock.notifyAll();
                            break;
                        }
                    case 108:
                        ar = (AsyncResult) msg.obj;
                        obj = IccSmsInterfaceManager.this.mLock;
                        synchronized (obj) {
                            if (ar.exception == null) {
                                IccSmsInterfaceManager.this.mSuccess = ((int[]) ar.result)[0] == 1;
                            }
                            IccSmsInterfaceManager.this.log("queryCbActivation: " + IccSmsInterfaceManager.this.mSuccess);
                            IccSmsInterfaceManager.this.mLock.notifyAll();
                            break;
                        }
                    case 109:
                        ar = (AsyncResult) msg.obj;
                        obj = IccSmsInterfaceManager.this.mLock;
                        synchronized (obj) {
                            IccSmsInterfaceManager.this.mSuccess = ar.exception == null;
                            IccSmsInterfaceManager.this.mLock.notifyAll();
                            break;
                        }
                    case 110:
                        ar = (AsyncResult) msg.obj;
                        obj = IccSmsInterfaceManager.this.mLock;
                        synchronized (obj) {
                            if (ar.exception == null) {
                                mList = (ArrayList) ar.result;
                                for (int i = 0; i < mList.size(); i++) {
                                    SmsBroadcastConfigInfo cbConfig = (SmsBroadcastConfigInfo) mList.get(i);
                                    if (cbConfig.getFromServiceId() == cbConfig.getToServiceId()) {
                                        IccSmsInterfaceManager.this.mSmsCbChannelConfig = IccSmsInterfaceManager.this.mSmsCbChannelConfig + cbConfig.getFromServiceId();
                                    } else {
                                        IccSmsInterfaceManager.this.mSmsCbChannelConfig = IccSmsInterfaceManager.this.mSmsCbChannelConfig + cbConfig.getFromServiceId() + "-" + cbConfig.getToServiceId();
                                    }
                                    if (i + 1 != mList.size()) {
                                        IccSmsInterfaceManager.this.mSmsCbChannelConfig = IccSmsInterfaceManager.this.mSmsCbChannelConfig + IccSmsInterfaceManager.INDEXT_SPLITOR;
                                    }
                                }
                                IccSmsInterfaceManager.this.log("Channel configuration " + IccSmsInterfaceManager.this.mSmsCbChannelConfig);
                            } else {
                                IccSmsInterfaceManager.this.log("Cannot Get CB configs");
                            }
                            IccSmsInterfaceManager.this.mLock.notifyAll();
                            break;
                        }
                    case 112:
                        ar = (AsyncResult) msg.obj;
                        obj = IccSmsInterfaceManager.this.mLock;
                        synchronized (obj) {
                            if (ar.exception == null) {
                                IccSmsInterfaceManager.this.mSmsCbLanguageConfig = (String) ar.result;
                                IccSmsInterfaceManager.this.mSmsCbLanguageConfig = IccSmsInterfaceManager.this.mSmsCbLanguageConfig != null ? IccSmsInterfaceManager.this.mSmsCbLanguageConfig : UsimPBMemInfo.STRING_NOT_SET;
                                IccSmsInterfaceManager.this.log("Language configuration " + IccSmsInterfaceManager.this.mSmsCbLanguageConfig);
                            } else {
                                IccSmsInterfaceManager.this.log("Cannot Get CB configs");
                            }
                            IccSmsInterfaceManager.this.mLock.notifyAll();
                            break;
                        }
                    default:
                        return;
                }
                return;
            }
        };
        this.mSmsWipeReceiver = new AnonymousClass2(this);
        this.mPhone = phone;
        this.mContext = phone.getContext();
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mDispatcher = new ImsSMSDispatcher(phone, phone.mSmsStorageMonitor, phone.mSmsUsageMonitor);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.mediatek.dm.LAWMO_WIPE");
        this.mContext.registerReceiver(this.mSmsWipeReceiver, filter);
    }

    protected void markMessagesAsRead(ArrayList<byte[]> messages) {
        if (messages != null) {
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh == null) {
                if (Rlog.isLoggable("SMS", 3)) {
                    log("markMessagesAsRead - aborting, no icc card present.");
                }
                return;
            }
            int count = messages.size();
            for (int i = 0; i < count; i++) {
                byte[] ba = (byte[]) messages.get(i);
                if (ba[0] == (byte) 3) {
                    int n = ba.length;
                    byte[] nba = new byte[(n - 1)];
                    System.arraycopy(ba, 1, nba, 0, n - 1);
                    fh.updateEFLinearFixed(IccConstants.EF_SMS, i + 1, makeSmsRecordData(1, nba), null, null);
                    if (Rlog.isLoggable("SMS", 3)) {
                        log("SMS " + (i + 1) + " marked as read");
                    }
                }
            }
        }
    }

    protected void updatePhoneObject(Phone phone) {
        this.mPhone = phone;
        this.mDispatcher.updatePhoneObject(phone);
    }

    protected void enforceReceiveAndSend(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.RECEIVE_SMS", message);
        this.mContext.enforceCallingOrSelfPermission("android.permission.SEND_SMS", message);
    }

    public boolean updateMessageOnIccEf(String callingPackage, int index, int status, byte[] pdu) {
        log("updateMessageOnIccEf: index=" + index + " status=" + status + " ==> " + "(" + Arrays.toString(pdu) + ")");
        enforceReceiveAndSend("Updating message on Icc");
        log("updateMessageOnIccEf: callingPackage = " + callingPackage + ", Binder.getCallingUid() = " + Binder.getCallingUid());
        if (Binder.getCallingUid() == 1001) {
            callingPackage = "com.android.phone";
        }
        if (this.mAppOps.noteOp(22, Binder.getCallingUid(), callingPackage) != 0) {
            log("updateMessageOnIccEf: noteOp NOT ALLOWED");
            return false;
        }
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHandler.obtainMessage(2);
            if (status != 0) {
                IccFileHandler fh = this.mPhone.getIccFileHandler();
                if (fh == null) {
                    response.recycle();
                    boolean z = this.mSuccess;
                    return z;
                }
                fh.updateEFLinearFixed(IccConstants.EF_SMS, index, makeSmsRecordData(status, pdu), null, response);
            } else if (1 == this.mPhone.getPhoneType()) {
                this.mPhone.mCi.deleteSmsOnSim(index, response);
            } else {
                this.mPhone.mCi.deleteSmsOnRuim(index, response);
            }
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to update by index");
            }
        }
        return this.mSuccess;
    }

    public boolean copyMessageToIccEf(String callingPackage, int status, byte[] pdu, byte[] smsc) {
        log("copyMessageToIccEf: status=" + status + " ==> " + "pdu=(" + Arrays.toString(pdu) + "), smsc=(" + Arrays.toString(smsc) + ")");
        enforceReceiveAndSend("Copying message to Icc");
        if (this.mAppOps.noteOp(22, Binder.getCallingUid(), callingPackage) != 0) {
            return false;
        }
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHandler.obtainMessage(2);
            if (1 == this.mPhone.getPhoneType()) {
                this.mPhone.mCi.writeSmsToSim(status, IccUtils.bytesToHexString(smsc), IccUtils.bytesToHexString(pdu), response);
            } else {
                if (status == 1 || status == 3) {
                    SmsMessage msg = SmsMessage.createFromPdu(pdu, SmsMessage.FORMAT_3GPP2);
                    if (msg == null) {
                        return false;
                    }
                    SubmitPdu mpdu = com.android.internal.telephony.cdma.SmsMessage.createEfPdu(msg.getDisplayOriginatingAddress(), msg.getMessageBody(), msg.getTimestampMillis());
                    if (mpdu != null) {
                        pdu = mpdu.encodedMessage;
                    } else {
                        return false;
                    }
                }
                this.mPhone.mCi.writeSmsToRuim(status, IccUtils.bytesToHexString(pdu), response);
            }
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to update by index");
            }
        }
        return this.mSuccess;
    }

    public List<SmsRawData> getAllMessagesFromIccEf(String callingPackage) {
        log("getAllMessagesFromEF " + callingPackage);
        this.mContext.enforceCallingOrSelfPermission("android.permission.RECEIVE_SMS", "Reading messages from Icc");
        if (this.mAppOps.noteOp(21, Binder.getCallingUid(), callingPackage) != 0) {
            return new ArrayList();
        }
        synchronized (this.mLoadLock) {
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh == null) {
                Rlog.e(LOG_TAG, "Cannot load Sms records. No icc card?");
                this.mSms = null;
                List<SmsRawData> list = this.mSms;
                return list;
            }
            fh.loadEFLinearFixedAll(IccConstants.EF_SMS, this.mHandler.obtainMessage(1));
            try {
                this.mLoadLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to load from the Icc");
            }
        }
        return this.mSms;
    }

    public void sendDataWithSelfPermissions(String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        this.mPhone.getContext().enforceCallingOrSelfPermission("android.permission.SEND_SMS", "Sending SMS message");
        sendDataInternal(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
    }

    public void sendData(String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        sendDataInternal(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
    }

    private void sendDataInternal(String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (Rlog.isLoggable("SMS", 2)) {
            log("sendData: destAddr=" + destAddr + " scAddr=" + scAddr + " destPort=" + destPort + " data='" + HexDump.toHexString(data) + "' sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent);
        }
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            this.mDispatcher.sendData(filterDestAddress(destAddr), scAddr, destPort, data, sentIntent, deliveryIntent);
        }
    }

    public void sendText(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        sendTextInternal(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp);
    }

    public void sendTextWithSelfPermissions(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) {
        this.mPhone.getContext().enforceCallingOrSelfPermission("android.permission.SEND_SMS", "Sending SMS message");
        sendTextInternal(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessage);
    }

    private void sendTextInternal(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) {
        if (Rlog.isLoggable("SMS", 2)) {
            log("sendText: destAddr=" + destAddr + " scAddr=" + scAddr + " text='" + text + "' sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent);
        }
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            if (!persistMessageForNonDefaultSmsApp) {
                enforceCarrierOrPhonePrivilege();
            }
            this.mDispatcher.sendText(filterDestAddress(destAddr), scAddr, text, sentIntent, deliveryIntent, null, callingPackage, persistMessageForNonDefaultSmsApp);
        }
    }

    public void injectSmsPdu(byte[] pdu, String format, PendingIntent receivedIntent) {
        enforceCarrierPrivilege();
        if (Rlog.isLoggable("SMS", 2)) {
            log("pdu: " + pdu + "\n format=" + format + "\n receivedIntent=" + receivedIntent);
        }
        this.mDispatcher.injectSmsPdu(pdu, format, receivedIntent);
    }

    public void sendMultipartText(String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) {
        int i;
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (!persistMessageForNonDefaultSmsApp) {
            enforceCarrierPrivilege();
        }
        if (Rlog.isLoggable("SMS", 2)) {
            i = 0;
            for (String part : parts) {
                int i2 = i + 1;
                log("sendMultipartText: destAddr=" + destAddr + ", srAddr=" + scAddr + ", part[" + i + "]=" + part);
                i = i2;
            }
        }
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            destAddr = filterDestAddress(destAddr);
            if (parts.size() <= 1 || parts.size() >= 10 || SmsMessage.hasEmsSupport()) {
                this.mDispatcher.sendMultipartText(destAddr, scAddr, (ArrayList) parts, (ArrayList) sentIntents, (ArrayList) deliveryIntents, null, callingPackage, persistMessageForNonDefaultSmsApp);
                return;
            }
            i = 0;
            while (i < parts.size()) {
                String singlePart = (String) parts.get(i);
                if (SmsMessage.shouldAppendPageNumberAsPrefix()) {
                    singlePart = String.valueOf(i + 1) + '/' + parts.size() + ' ' + singlePart;
                } else {
                    singlePart = singlePart.concat(' ' + String.valueOf(i + 1) + '/' + parts.size());
                }
                PendingIntent pendingIntent = null;
                if (sentIntents != null && sentIntents.size() > i) {
                    pendingIntent = (PendingIntent) sentIntents.get(i);
                }
                PendingIntent pendingIntent2 = null;
                if (deliveryIntents != null && deliveryIntents.size() > i) {
                    pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
                }
                this.mDispatcher.sendText(destAddr, scAddr, singlePart, pendingIntent, pendingIntent2, null, callingPackage, persistMessageForNonDefaultSmsApp);
                i++;
            }
        }
    }

    public int getPremiumSmsPermission(String packageName) {
        return this.mDispatcher.getPremiumSmsPermission(packageName);
    }

    public void setPremiumSmsPermission(String packageName, int permission) {
        this.mDispatcher.setPremiumSmsPermission(packageName, permission);
    }

    protected ArrayList<SmsRawData> buildValidRawData(ArrayList<byte[]> messages) {
        int count = messages.size();
        ArrayList<SmsRawData> ret = new ArrayList(count);
        int validSmsCount = 0;
        for (int i = 0; i < count; i++) {
            if (((byte[]) messages.get(i))[0] == (byte) 0) {
                ret.add(null);
            } else {
                validSmsCount++;
                ret.add(new SmsRawData((byte[]) messages.get(i)));
            }
        }
        log("validSmsCount = " + validSmsCount);
        return ret;
    }

    protected byte[] makeSmsRecordData(int status, byte[] pdu) {
        byte[] data;
        if (1 == this.mPhone.getPhoneType()) {
            data = new byte[176];
        } else {
            data = new byte[255];
        }
        data[0] = (byte) (status & 7);
        log("ISIM-makeSmsRecordData: pdu size = " + pdu.length);
        if (pdu.length == 176) {
            log("ISIM-makeSmsRecordData: sim pdu");
            try {
                System.arraycopy(pdu, 1, data, 1, pdu.length - 1);
            } catch (ArrayIndexOutOfBoundsException e) {
                log("ISIM-makeSmsRecordData: out of bounds, sim pdu");
            }
        } else {
            log("ISIM-makeSmsRecordData: normal pdu");
            try {
                System.arraycopy(pdu, 0, data, 1, pdu.length);
            } catch (ArrayIndexOutOfBoundsException e2) {
                log("ISIM-makeSmsRecordData: out of bounds, normal pdu");
            }
        }
        for (int j = pdu.length + 1; j < data.length; j++) {
            data[j] = (byte) -1;
        }
        return data;
    }

    public boolean enableCellBroadcast(int messageIdentifier, int ranType) {
        return enableCellBroadcastRange(messageIdentifier, messageIdentifier, ranType);
    }

    public boolean disableCellBroadcast(int messageIdentifier, int ranType) {
        return disableCellBroadcastRange(messageIdentifier, messageIdentifier, ranType);
    }

    public boolean enableCellBroadcastRange(int startMessageId, int endMessageId, int ranType) {
        if (ranType == 0) {
            return enableGsmBroadcastRange(startMessageId, endMessageId);
        }
        if (ranType == 1) {
            return enableCdmaBroadcastRange(startMessageId, endMessageId);
        }
        throw new IllegalArgumentException("Not a supportted RAN Type");
    }

    public boolean disableCellBroadcastRange(int startMessageId, int endMessageId, int ranType) {
        if (ranType == 0) {
            return disableGsmBroadcastRange(startMessageId, endMessageId);
        }
        if (ranType == 1) {
            return disableCdmaBroadcastRange(startMessageId, endMessageId);
        }
        throw new IllegalArgumentException("Not a supportted RAN Type");
    }

    public synchronized boolean enableGsmBroadcastRange(int startMessageId, int endMessageId) {
        boolean z = false;
        synchronized (this) {
            Context context = this.mPhone.getContext();
            context.enforceCallingPermission("android.permission.RECEIVE_SMS", "Enabling cell broadcast SMS");
            String client = context.getPackageManager().getNameForUid(Binder.getCallingUid());
            if (this.mCellBroadcastRangeManager.enableRange(startMessageId, endMessageId, client)) {
                log("Added GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
                if (!this.mCellBroadcastRangeManager.isEmpty()) {
                    z = true;
                }
                setCellBroadcastActivation(z);
                return true;
            }
            log("Failed to add GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            return false;
        }
    }

    public synchronized boolean disableGsmBroadcastRange(int startMessageId, int endMessageId) {
        boolean z = false;
        synchronized (this) {
            Context context = this.mPhone.getContext();
            context.enforceCallingPermission("android.permission.RECEIVE_SMS", "Disabling cell broadcast SMS");
            String client = context.getPackageManager().getNameForUid(Binder.getCallingUid());
            if (this.mCellBroadcastRangeManager.disableRange(startMessageId, endMessageId, client)) {
                log("Removed GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
                if (!this.mCellBroadcastRangeManager.isEmpty()) {
                    z = true;
                }
                setCellBroadcastActivation(z);
                return true;
            }
            log("Failed to remove GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            return false;
        }
    }

    public synchronized boolean enableCdmaBroadcastRange(int startMessageId, int endMessageId) {
        boolean z = false;
        synchronized (this) {
            Context context = this.mPhone.getContext();
            context.enforceCallingPermission("android.permission.RECEIVE_SMS", "Enabling cdma broadcast SMS");
            String client = context.getPackageManager().getNameForUid(Binder.getCallingUid());
            if (this.mCdmaBroadcastRangeManager.enableRange(startMessageId, endMessageId, client)) {
                log("Added cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
                if (!this.mCdmaBroadcastRangeManager.isEmpty()) {
                    z = true;
                }
                setCdmaBroadcastActivation(z);
                return true;
            }
            log("Failed to add cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            return false;
        }
    }

    public synchronized boolean disableCdmaBroadcastRange(int startMessageId, int endMessageId) {
        boolean z = false;
        synchronized (this) {
            Context context = this.mPhone.getContext();
            context.enforceCallingPermission("android.permission.RECEIVE_SMS", "Disabling cell broadcast SMS");
            String client = context.getPackageManager().getNameForUid(Binder.getCallingUid());
            if (this.mCdmaBroadcastRangeManager.disableRange(startMessageId, endMessageId, client)) {
                log("Removed cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
                if (!this.mCdmaBroadcastRangeManager.isEmpty()) {
                    z = true;
                }
                setCdmaBroadcastActivation(z);
                return true;
            }
            log("Failed to remove cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            return false;
        }
    }

    private boolean setCellBroadcastConfig(SmsBroadcastConfigInfo[] configs) {
        log("Calling setGsmBroadcastConfig with " + configs.length + " configurations");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(4);
            this.mSuccess = false;
            this.mPhone.mCi.setGsmBroadcastConfig(configs, response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to set cell broadcast config");
            }
        }
        return this.mSuccess;
    }

    private boolean setCellBroadcastActivation(boolean activate) {
        log("Calling setCellBroadcastActivation(" + activate + ')');
        int newActivationState = activate ? 1 : 0;
        synchronized (this.mLock) {
            if (this.mCurrentCellBroadcastActivation != newActivationState) {
                Message response = this.mHandler.obtainMessage(3);
                this.mSuccess = false;
                this.mPhone.mCi.setGsmBroadcastActivation(activate, response);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    log("interrupted while trying to set cell broadcast activation");
                }
            } else {
                this.mSuccess = true;
            }
        }
        if (this.mSuccess && this.mCurrentCellBroadcastActivation != newActivationState) {
            this.mCurrentCellBroadcastActivation = newActivationState;
            log("mCurrentCellBroadcastActivation change to " + this.mCurrentCellBroadcastActivation);
        }
        if (!activate && this.mSuccess) {
            this.mCellBroadcastRangeManager.clearAllRanges();
        }
        return this.mSuccess;
    }

    private boolean setCdmaBroadcastConfig(CdmaSmsBroadcastConfigInfo[] configs) {
        log("Calling setCdmaBroadcastConfig with " + configs.length + " configurations");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(4);
            this.mSuccess = false;
            this.mPhone.mCi.setCdmaBroadcastConfig(configs, response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to set cdma broadcast config");
            }
        }
        return this.mSuccess;
    }

    private boolean setCdmaBroadcastActivation(boolean activate) {
        log("Calling setCdmaBroadcastActivation(" + activate + ")");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(3);
            this.mSuccess = false;
            this.mPhone.mCi.setCdmaBroadcastActivation(activate, response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to set cdma broadcast activation");
            }
        }
        return this.mSuccess;
    }

    protected void log(String msg) {
        Rlog.d(LOG_TAG, "[IccSmsInterfaceManager] " + msg);
    }

    public boolean isImsSmsSupported() {
        return this.mDispatcher.isIms();
    }

    public String getImsSmsFormat() {
        return this.mDispatcher.getImsSmsFormat();
    }

    public void sendStoredText(String callingPkg, Uri messageUri, String scAddress, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (Rlog.isLoggable("SMS", 2)) {
            log("sendStoredText: scAddr=" + scAddress + " messageUri=" + messageUri + " sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent);
        }
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPkg) == 0) {
            ContentResolver resolver = this.mPhone.getContext().getContentResolver();
            if (isFailedOrDraft(resolver, messageUri)) {
                String[] textAndAddress = loadTextAndAddress(resolver, messageUri);
                if (textAndAddress == null) {
                    Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredText: can not load text");
                    returnUnspecifiedFailure(sentIntent);
                    return;
                }
                textAndAddress[1] = filterDestAddress(textAndAddress[1]);
                this.mDispatcher.sendText(textAndAddress[1], scAddress, textAndAddress[0], sentIntent, deliveryIntent, messageUri, callingPkg, true);
                return;
            }
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredText: not FAILED or DRAFT message");
            returnUnspecifiedFailure(sentIntent);
        }
    }

    public void sendStoredMultipartText(String callingPkg, Uri messageUri, String scAddress, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPkg) == 0) {
            ContentResolver resolver = this.mPhone.getContext().getContentResolver();
            if (isFailedOrDraft(resolver, messageUri)) {
                String[] textAndAddress = loadTextAndAddress(resolver, messageUri);
                if (textAndAddress == null) {
                    Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredMultipartText: can not load text");
                    returnUnspecifiedFailure((List) sentIntents);
                    return;
                }
                ArrayList<String> parts = SmsManager.getDefault().divideMessage(textAndAddress[0]);
                if (parts == null || parts.size() < 1) {
                    Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredMultipartText: can not divide text");
                    returnUnspecifiedFailure((List) sentIntents);
                    return;
                }
                textAndAddress[1] = filterDestAddress(textAndAddress[1]);
                if (parts.size() <= 1 || parts.size() >= 10 || SmsMessage.hasEmsSupport()) {
                    this.mDispatcher.sendMultipartText(textAndAddress[1], scAddress, parts, (ArrayList) sentIntents, (ArrayList) deliveryIntents, messageUri, callingPkg, true);
                    return;
                }
                int i = 0;
                while (i < parts.size()) {
                    String singlePart = (String) parts.get(i);
                    if (SmsMessage.shouldAppendPageNumberAsPrefix()) {
                        singlePart = String.valueOf(i + 1) + '/' + parts.size() + ' ' + singlePart;
                    } else {
                        singlePart = singlePart.concat(' ' + String.valueOf(i + 1) + '/' + parts.size());
                    }
                    PendingIntent pendingIntent = null;
                    if (sentIntents != null && sentIntents.size() > i) {
                        pendingIntent = (PendingIntent) sentIntents.get(i);
                    }
                    PendingIntent pendingIntent2 = null;
                    if (deliveryIntents != null && deliveryIntents.size() > i) {
                        pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
                    }
                    this.mDispatcher.sendText(textAndAddress[1], scAddress, singlePart, pendingIntent, pendingIntent2, messageUri, callingPkg, true);
                    i++;
                }
                return;
            }
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredMultipartText: not FAILED or DRAFT message");
            returnUnspecifiedFailure((List) sentIntents);
        }
    }

    private boolean isFailedOrDraft(ContentResolver resolver, Uri messageUri) {
        long identity = Binder.clearCallingIdentity();
        Cursor cursor = null;
        try {
            String[] strArr = new String[1];
            strArr[0] = "type";
            cursor = resolver.query(messageUri, strArr, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                Binder.restoreCallingIdentity(identity);
                return false;
            }
            int type = cursor.getInt(0);
            boolean z = type != 3 ? type == 5 : true;
            if (cursor != null) {
                cursor.close();
            }
            Binder.restoreCallingIdentity(identity);
            return z;
        } catch (SQLiteException e) {
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]isFailedOrDraft: query message type failed", e);
            if (cursor != null) {
                cursor.close();
            }
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    private String[] loadTextAndAddress(ContentResolver resolver, Uri messageUri) {
        long identity = Binder.clearCallingIdentity();
        Cursor cursor = null;
        String[] strArr;
        try {
            String[] strArr2 = new String[2];
            strArr2[0] = "body";
            strArr2[1] = "address";
            cursor = resolver.query(messageUri, strArr2, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                Binder.restoreCallingIdentity(identity);
                return null;
            }
            strArr = new String[2];
            strArr[0] = cursor.getString(0);
            strArr[1] = cursor.getString(1);
            return strArr;
        } catch (SQLiteException e) {
            strArr = LOG_TAG;
            Log.e(strArr, "[IccSmsInterfaceManager]loadText: query message text failed", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void returnUnspecifiedFailure(PendingIntent pi) {
        if (pi != null) {
            try {
                pi.send(1);
            } catch (CanceledException e) {
            }
        }
    }

    private void returnUnspecifiedFailure(List<PendingIntent> pis) {
        if (pis != null) {
            for (PendingIntent pi : pis) {
                returnUnspecifiedFailure(pi);
            }
        }
    }

    private void enforceCarrierPrivilege() {
        UiccController controller = UiccController.getInstance();
        if (controller == null || controller.getUiccCard(this.mPhone.getPhoneId()) == null) {
            throw new SecurityException("No Carrier Privilege: No UICC");
        } else if (controller.getUiccCard(this.mPhone.getPhoneId()).getCarrierPrivilegeStatusForCurrentTransaction(this.mContext.getPackageManager()) != 1) {
            throw new SecurityException("No Carrier Privilege.");
        }
    }

    private void enforceCarrierOrPhonePrivilege() {
        if (Binder.getCallingUid() != 1001) {
            enforceCarrierPrivilege();
        }
    }

    private String filterDestAddress(String destAddr) {
        String result = SmsNumberUtils.filterDestAddr(this.mPhone, destAddr);
        return result != null ? result : destAddr;
    }

    public void sendDataWithOriginalPort(String callingPackage, String destAddr, String scAddr, int destPort, int originalPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        Rlog.d(LOG_TAG, "Enter IccSmsInterfaceManager.sendDataWithOriginalPort");
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (Rlog.isLoggable("SMS", 2)) {
            log("sendData: destAddr=" + destAddr + " scAddr=" + scAddr + " destPort=" + destPort + " originalPort=" + originalPort + " data='" + HexDump.toHexString(data) + "' sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent);
        }
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            this.mDispatcher.sendData(destAddr, scAddr, destPort, originalPort, data, sentIntent, deliveryIntent);
        }
    }

    public void sendMultipartData(String callingPackage, String destAddr, String scAddr, int destPort, List<SmsRawData> data, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (Rlog.isLoggable("SMS", 2)) {
            for (SmsRawData rData : data) {
                log("sendMultipartData: destAddr=" + destAddr + " scAddr=" + scAddr + " destPort=" + destPort + " data='" + HexDump.toHexString(rData.getBytes()));
            }
        }
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            this.mDispatcher.sendMultipartData(destAddr, scAddr, destPort, (ArrayList) data, (ArrayList) sentIntents, (ArrayList) deliveryIntents);
        }
    }

    public void setSmsMemoryStatus(boolean status) {
        log("setSmsMemoryStatus: set storage status -> " + status);
        this.mDispatcher.setSmsMemoryStatus(status);
    }

    public boolean isSmsReady() {
        boolean isReady = this.mDispatcher.isSmsReady();
        log("isSmsReady: " + isReady);
        return isReady;
    }

    public void sendTextWithEncodingType(String callingPackage, String destAddr, String scAddr, String text, int encodingType, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            this.mDispatcher.sendTextWithEncodingType(destAddr, scAddr, text, encodingType, sentIntent, deliveryIntent, null, callingPackage, persistMessage);
        }
    }

    public void sendMultipartTextWithEncodingType(String callingPackage, String destAddr, String scAddr, List<String> parts, int encodingType, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            destAddr = filterDestAddress(destAddr);
            if (parts.size() <= 1 || parts.size() >= 10 || SmsMessage.hasEmsSupport()) {
                this.mDispatcher.sendMultipartTextWithEncodingType(destAddr, scAddr, (ArrayList) parts, encodingType, (ArrayList) sentIntents, (ArrayList) deliveryIntents, null, callingPackage, persistMessageForNonDefaultSmsApp);
                return;
            }
            int i = 0;
            while (i < parts.size()) {
                String singlePart = (String) parts.get(i);
                if (SmsMessage.shouldAppendPageNumberAsPrefix()) {
                    singlePart = String.valueOf(i + 1) + '/' + parts.size() + ' ' + singlePart;
                } else {
                    singlePart = singlePart.concat(' ' + String.valueOf(i + 1) + '/' + parts.size());
                }
                PendingIntent pendingIntent = null;
                if (sentIntents != null && sentIntents.size() > i) {
                    pendingIntent = (PendingIntent) sentIntents.get(i);
                }
                PendingIntent pendingIntent2 = null;
                if (deliveryIntents != null && deliveryIntents.size() > i) {
                    pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
                }
                this.mDispatcher.sendTextWithEncodingType(destAddr, scAddr, singlePart, encodingType, pendingIntent, pendingIntent2, null, callingPackage, persistMessageForNonDefaultSmsApp);
                i++;
            }
        }
    }

    public void sendTextWithExtraParams(String callingPackage, String destAddr, String scAddr, String text, Bundle extraParams, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            this.mDispatcher.sendTextWithExtraParams(destAddr, scAddr, text, extraParams, sentIntent, deliveryIntent, null, callingPackage, persistMessage);
        }
    }

    public void sendMultipartTextWithExtraParams(String callingPackage, String destAddr, String scAddr, List<String> parts, Bundle extraParams, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            destAddr = filterDestAddress(destAddr);
            if (parts.size() <= 1 || parts.size() >= 10 || SmsMessage.hasEmsSupport()) {
                this.mDispatcher.sendMultipartTextWithExtraParams(destAddr, scAddr, (ArrayList) parts, extraParams, (ArrayList) sentIntents, (ArrayList) deliveryIntents, null, callingPackage, persistMessageForNonDefaultSmsApp);
                return;
            }
            int i = 0;
            while (i < parts.size()) {
                String singlePart = (String) parts.get(i);
                if (SmsMessage.shouldAppendPageNumberAsPrefix()) {
                    singlePart = String.valueOf(i + 1) + '/' + parts.size() + ' ' + singlePart;
                } else {
                    singlePart = singlePart.concat(' ' + String.valueOf(i + 1) + '/' + parts.size());
                }
                PendingIntent pendingIntent = null;
                if (sentIntents != null && sentIntents.size() > i) {
                    pendingIntent = (PendingIntent) sentIntents.get(i);
                }
                PendingIntent pendingIntent2 = null;
                if (deliveryIntents != null && deliveryIntents.size() > i) {
                    pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
                }
                this.mDispatcher.sendTextWithExtraParams(destAddr, scAddr, singlePart, extraParams, pendingIntent, pendingIntent2, null, callingPackage, persistMessageForNonDefaultSmsApp);
                i++;
            }
        }
    }

    public String getFormat() {
        return this.mDispatcher.getFormat();
    }

    public SmsRawData getMessageFromIccEf(String callingPackage, int index) {
        log("getMessageFromIccEf");
        this.mPhone.getContext().enforceCallingPermission("android.permission.RECEIVE_SMS", "Reading messages from SIM");
        if (this.mAppOps.noteOp(21, Binder.getCallingUid(), callingPackage) != 0) {
            return null;
        }
        this.mSmsRawData = null;
        synchronized (this.mLock) {
            if (this.mPhone.getIccFileHandler() != null) {
                this.mPhone.getIccFileHandler().loadEFLinearFixed(IccConstants.EF_SMS, index, this.mHandler.obtainMessage(106));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    log("interrupted while trying to load from the SIM");
                }
            }
        }
        return this.mSmsRawData;
    }

    public List<SmsRawData> getAllMessagesFromIccEfByMode(String callingPackage, int mode) {
        log("getAllMessagesFromIccEfByMode, mode=" + mode);
        if (mode < 1 || mode > 2) {
            log("getAllMessagesFromIccEfByMode wrong mode=" + mode);
            return this.mSms;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.RECEIVE_SMS", "Reading messages from Icc");
        if (this.mAppOps.noteOp(21, Binder.getCallingUid(), callingPackage) != 0) {
            return new ArrayList();
        }
        synchronized (this.mLoadLock) {
            if (this.mPhone.getIccFileHandler() == null) {
                Rlog.e(LOG_TAG, "Cannot load Sms records. No icc card?");
                if (this.mSms != null) {
                    this.mSms.clear();
                    List<SmsRawData> list = this.mSms;
                    return list;
                }
            }
            this.mPhone.getIccFileHandler().loadEFLinearFixedAll((int) IccConstants.EF_SMS, mode, this.mHandler.obtainMessage(1));
            try {
                this.mLoadLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to load from the SIM");
            }
        }
        return this.mSms;
    }

    public SmsParameters getSmsParameters(String callingPackage) {
        log("getSmsParameters");
        enforceReceiveAndSend("Get SMS parametner on SIM");
        if (this.mAppOps.noteOp(21, Binder.getCallingUid(), callingPackage) != 0) {
            return null;
        }
        synchronized (this.mLock) {
            this.mPhone.mCi.getSmsParameters(this.mHandler.obtainMessage(104));
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get sms params");
            }
        }
        return this.mSmsParams;
    }

    public boolean setSmsParameters(String callingPackage, SmsParameters params) {
        log("setSmsParameters");
        enforceReceiveAndSend("Set SMS parametner on SIM");
        if (this.mAppOps.noteOp(22, Binder.getCallingUid(), callingPackage) != 0) {
            return false;
        }
        this.mSmsParamsSuccess = false;
        synchronized (this.mLock) {
            this.mPhone.mCi.setSmsParameters(params, this.mHandler.obtainMessage(105));
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get sms params");
            }
        }
        return this.mSmsParamsSuccess;
    }

    public int copyTextMessageToIccCard(String callingPkg, String scAddress, String address, List<String> text, int status, long timestamp) {
        log("copyTextMessageToIccCard, sc address: " + scAddress + " address: " + address + " message count: " + text.size() + " status: " + status + " timestamp: " + timestamp);
        enforceReceiveAndSend("Copying message to USIM/SIM");
        if (this.mAppOps.noteOp(22, Binder.getCallingUid(), callingPkg) != 0) {
            return 1;
        }
        IccSmsStorageStatus memStatus = getSmsSimMemoryStatus(callingPkg);
        if (memStatus == null) {
            log("Fail to get SIM memory status");
            return 1;
        } else if (memStatus.getUnused() >= text.size()) {
            return this.mDispatcher.copyTextMessageToIccCard(scAddress, address, text, status, timestamp);
        } else {
            log("SIM memory is not enough");
            return 7;
        }
    }

    public SimSmsInsertStatus insertTextMessageToIccCard(String callingPackage, String scAddress, String address, List<String> text, int status, long timestamp) {
        log("insertTextMessageToIccCard");
        enforceReceiveAndSend("insertText insert message into SIM");
        if (this.mAppOps.noteOp(22, Binder.getCallingUid(), callingPackage) != 0) {
            this.smsInsertRet.insertStatus = 1;
            return this.smsInsertRet;
        }
        int msgCount = text.size();
        log("insertText scAddr=" + scAddress + ", addr=" + address + ", msgCount=" + msgCount + ", status=" + status + ", timestamp=" + timestamp);
        this.smsInsertRet.indexInIcc = UsimPBMemInfo.STRING_NOT_SET;
        IccSmsStorageStatus memStatus = getSmsSimMemoryStatus(callingPackage);
        if (memStatus != null) {
            int unused = memStatus.getUnused();
            if (unused < msgCount) {
                log("insertText SIM mem is not enough [" + unused + "/" + msgCount + "]");
                this.smsInsertRet.insertStatus = 7;
                return this.smsInsertRet;
            }
            if (!checkPhoneNumberInternal(scAddress)) {
                log("insertText invalid sc address");
                scAddress = null;
            }
            if (checkPhoneNumberInternal(address)) {
                boolean isDeliverPdu;
                if (status == 1 || status == 3) {
                    log("insertText to encode delivery pdu");
                    isDeliverPdu = true;
                } else if (status == 5 || status == 7) {
                    log("insertText to encode submit pdu");
                    isDeliverPdu = false;
                } else {
                    log("insertText invalid status " + status);
                    this.smsInsertRet.insertStatus = 1;
                    return this.smsInsertRet;
                }
                log("insertText params check pass");
                if (2 == this.mPhone.getPhoneType()) {
                    return writeTextMessageToRuim(address, text, status, timestamp);
                }
                int i;
                int encoding = 0;
                TextEncodingDetails[] details = new TextEncodingDetails[msgCount];
                for (i = 0; i < msgCount; i++) {
                    details[i] = com.android.internal.telephony.gsm.SmsMessage.calculateLength((CharSequence) text.get(i), false);
                    if (encoding != details[i].codeUnitSize && (encoding == 0 || encoding == 1)) {
                        encoding = details[i].codeUnitSize;
                    }
                }
                log("insertText create & insert pdu start...");
                i = 0;
                while (i < msgCount) {
                    if (this.mInsertMessageSuccess || i <= 0) {
                        int singleShiftId = -1;
                        int lockingShiftId = -1;
                        int language = details[i].shiftLangId;
                        int encoding_detail = encoding;
                        if (encoding == 1) {
                            if (details[i].languageTable > 0 && details[i].languageShiftTable > 0) {
                                singleShiftId = details[i].languageTable;
                                lockingShiftId = details[i].languageShiftTable;
                                encoding_detail = 13;
                            } else if (details[i].languageShiftTable > 0) {
                                lockingShiftId = details[i].languageShiftTable;
                                encoding_detail = 12;
                            } else if (details[i].languageTable > 0) {
                                singleShiftId = details[i].languageTable;
                                encoding_detail = 11;
                            }
                        }
                        byte[] smsHeader = null;
                        if (msgCount > 1) {
                            log("insertText create pdu header for concat-message");
                            smsHeader = SmsHeader.getSubmitPduHeaderWithLang(-1, getNextConcatRef() & 255, i + 1, msgCount, singleShiftId, lockingShiftId);
                        }
                        if (isDeliverPdu) {
                            DeliverPdu pdu = com.android.internal.telephony.gsm.SmsMessage.getDeliverPduWithLang(scAddress, address, (String) text.get(i), smsHeader, timestamp, encoding_detail, language);
                            if (pdu != null) {
                                this.mPhone.mCi.writeSmsToSim(status, IccUtils.bytesToHexString(pdu.encodedScAddress), IccUtils.bytesToHexString(pdu.encodedMessage), this.mHandler.obtainMessage(103));
                            } else {
                                log("insertText fail to create deliver pdu");
                                this.smsInsertRet.insertStatus = 1;
                                return this.smsInsertRet;
                            }
                        }
                        com.android.internal.telephony.gsm.SmsMessage.SubmitPdu pdu2 = com.android.internal.telephony.gsm.SmsMessage.getSubmitPduWithLang(scAddress, address, (String) text.get(i), false, smsHeader, encoding_detail, language);
                        if (pdu2 != null) {
                            this.mPhone.mCi.writeSmsToSim(status, IccUtils.bytesToHexString(pdu2.encodedScAddress), IccUtils.bytesToHexString(pdu2.encodedMessage), this.mHandler.obtainMessage(103));
                        } else {
                            log("insertText fail to create submit pdu");
                            this.smsInsertRet.insertStatus = 1;
                            return this.smsInsertRet;
                        }
                        synchronized (this.mSimInsertLock) {
                            try {
                                log("insertText wait until the pdu be wrote into the SIM");
                                this.mSimInsertLock.wait();
                            } catch (InterruptedException e) {
                                log("insertText fail to insert pdu");
                                this.smsInsertRet.insertStatus = 1;
                                return this.smsInsertRet;
                            }
                        }
                        i++;
                    } else {
                        log("insertText last message insert fail");
                        this.smsInsertRet.insertStatus = 1;
                        return this.smsInsertRet;
                    }
                }
                log("insertText create & insert pdu end");
                if (this.mInsertMessageSuccess) {
                    log("insertText all messages inserted");
                    this.smsInsertRet.insertStatus = 1;
                    return this.smsInsertRet;
                }
                log("insertText pdu insert fail");
                this.smsInsertRet.insertStatus = 1;
                return this.smsInsertRet;
            }
            log("insertText invalid address");
            this.smsInsertRet.insertStatus = 8;
            return this.smsInsertRet;
        }
        log("insertText fail to get SIM mem status");
        this.smsInsertRet.insertStatus = 1;
        return this.smsInsertRet;
    }

    public SimSmsInsertStatus insertRawMessageToIccCard(String callingPackage, int status, byte[] pdu, byte[] smsc) {
        log("insertRawMessageToIccCard");
        enforceReceiveAndSend("insertRaw insert message into SIM");
        if (this.mAppOps.noteOp(22, Binder.getCallingUid(), callingPackage) != 0) {
            this.smsInsertRet2.insertStatus = 1;
            return this.smsInsertRet2;
        }
        synchronized (this.mLock) {
            this.mSuccess = false;
            this.smsInsertRet2.insertStatus = 1;
            this.smsInsertRet2.indexInIcc = UsimPBMemInfo.STRING_NOT_SET;
            Message response = this.mHandler.obtainMessage(2);
            if (2 != this.mPhone.getPhoneType()) {
                this.mPhone.mCi.writeSmsToSim(status, IccUtils.bytesToHexString(smsc), IccUtils.bytesToHexString(pdu), response);
            } else {
                this.mPhone.mCi.writeSmsToRuim(status, IccUtils.bytesToHexString(pdu), response);
            }
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("insertRaw interrupted while trying to update by index");
            }
        }
        if (this.mSuccess) {
            log("insertRaw message inserted");
            this.smsInsertRet2.insertStatus = 0;
            return this.smsInsertRet2;
        }
        log("insertRaw pdu insert fail");
        this.smsInsertRet2.insertStatus = 1;
        return this.smsInsertRet2;
    }

    public IccSmsStorageStatus getSmsSimMemoryStatus(String callingPackage) {
        log("getSmsSimMemoryStatus");
        enforceReceiveAndSend("Get SMS SIM Card Memory Status from RUIM");
        if (this.mAppOps.noteOp(21, Binder.getCallingUid(), callingPackage) != 0) {
            return null;
        }
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHandler.obtainMessage(102);
            if (this.mPhone.getPhoneType() == 2) {
                this.mPhone.mCi.getSmsRuimMemoryStatus(response);
            } else {
                this.mPhone.mCi.getSmsSimMemoryStatus(response);
            }
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get SMS SIM Card Memory Status from SIM");
            }
        }
        if (this.mSuccess) {
            return this.mSimMemStatus;
        }
        return null;
    }

    public boolean setEtwsConfig(int mode) {
        log("Calling setEtwsConfig(" + mode + ')');
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(101);
            this.mSuccess = false;
            this.mPhone.mCi.setEtws(mode, response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to set ETWS config");
            }
        }
        return this.mSuccess;
    }

    public boolean activateCellBroadcastSms(boolean activate) {
        log("activateCellBroadcastSms activate : " + activate);
        return setCellBroadcastActivation(activate);
    }

    private static int getNextConcatRef() {
        int i = sConcatenatedRef;
        sConcatenatedRef = i + 1;
        return i;
    }

    private static boolean checkPhoneNumberCharacter(char c) {
        if ((c >= '0' && c <= '9') || c == '*' || c == '+' || c == '#' || c == 'N' || c == ' ' || c == '-') {
            return true;
        }
        return false;
    }

    private static boolean checkPhoneNumberInternal(String number) {
        if (number == null) {
            return true;
        }
        int n = number.length();
        for (int i = 0; i < n; i++) {
            if (!checkPhoneNumberCharacter(number.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private SmsCbConfigInfo Convert2SmsCbConfigInfo(SmsBroadcastConfigInfo info) {
        return new SmsCbConfigInfo(info.getFromServiceId(), info.getToServiceId(), info.getFromCodeScheme(), info.getToCodeScheme(), info.isSelected());
    }

    private SmsBroadcastConfigInfo Convert2SmsBroadcastConfigInfo(SmsCbConfigInfo info) {
        return new SmsBroadcastConfigInfo(info.mFromServiceId, info.mToServiceId, info.mFromCodeScheme, info.mToCodeScheme, info.mSelected);
    }

    public SmsCbConfigInfo[] getCellBroadcastSmsConfig() {
        log("getCellBroadcastSmsConfig");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(107);
            this.mSmsCBConfig = null;
            this.mPhone.mCi.getGsmBroadcastConfigEx(response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get CB config");
            }
        }
        if (this.mSmsCBConfig != null) {
            log("config length = " + this.mSmsCBConfig.length);
            if (this.mSmsCBConfig.length != 0) {
                SmsCbConfigInfo[] result = new SmsCbConfigInfo[this.mSmsCBConfig.length];
                for (int i = 0; i < this.mSmsCBConfig.length; i++) {
                    result[i] = Convert2SmsCbConfigInfo(this.mSmsCBConfig[i]);
                }
                return result;
            }
        }
        return null;
    }

    public boolean setCellBroadcastSmsConfig(SmsCbConfigInfo[] channels, SmsCbConfigInfo[] languages) {
        log("setCellBroadcastSmsConfig");
        if (channels == null && languages == null) {
            return true;
        }
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(4);
            this.mSuccess = false;
            ArrayList<SmsBroadcastConfigInfo> chid_list = new ArrayList();
            if (channels != null) {
                for (SmsCbConfigInfo Convert2SmsBroadcastConfigInfo : channels) {
                    chid_list.add(Convert2SmsBroadcastConfigInfo(Convert2SmsBroadcastConfigInfo));
                }
            }
            ArrayList<SmsBroadcastConfigInfo> lang_list = new ArrayList();
            if (languages != null) {
                for (SmsCbConfigInfo Convert2SmsBroadcastConfigInfo2 : languages) {
                    lang_list.add(Convert2SmsBroadcastConfigInfo(Convert2SmsBroadcastConfigInfo2));
                }
            }
            chid_list.addAll(lang_list);
            this.mPhone.mCi.setGsmBroadcastConfigEx((SmsBroadcastConfigInfo[]) chid_list.toArray(new SmsBroadcastConfigInfo[1]), response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to set CB config");
            }
        }
        return this.mSuccess;
    }

    public boolean queryCellBroadcastSmsActivation() {
        log("queryCellBroadcastSmsActivation");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(108);
            this.mSuccess = false;
            this.mPhone.mCi.getGsmBroadcastActivation(response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get CB activation");
            }
        }
        return this.mSuccess;
    }

    public boolean removeCellBroadcastMsg(int channelId, int serialId) {
        log("removeCellBroadcastMsg(" + channelId + " , " + serialId + ")");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(109);
            this.mSuccess = false;
            this.mPhone.mCi.removeCellBroadcastMsg(channelId, serialId, response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to remove CB msg");
            }
        }
        return this.mSuccess;
    }

    protected SimSmsInsertStatus writeTextMessageToRuim(String address, List<String> text, int status, long timestamp) {
        SimSmsInsertStatus insertRet = new SimSmsInsertStatus(0, UsimPBMemInfo.STRING_NOT_SET);
        this.mSuccess = true;
        int i = 0;
        while (i < text.size()) {
            if (this.mSuccess) {
                SubmitPdu pdu = com.android.internal.telephony.cdma.SmsMessage.createEfPdu(address, (String) text.get(i), timestamp);
                if (pdu != null) {
                    this.mPhone.mCi.writeSmsToRuim(status, IccUtils.bytesToHexString(pdu.encodedMessage), this.mHandler.obtainMessage(103));
                    synchronized (this.mLock) {
                        try {
                            this.mLock.wait();
                        } catch (InterruptedException e) {
                            log("InterruptedException " + e);
                            insertRet.insertStatus = 1;
                            return insertRet;
                        }
                    }
                    i++;
                } else {
                    log("writeTextMessageToRuim: pdu == null");
                    insertRet.insertStatus = 1;
                    return insertRet;
                }
            }
            log("[copyText Exception happened when copy message");
            insertRet.insertStatus = 1;
            return insertRet;
        }
        log("writeTextMessageToRuim: done");
        insertRet.insertStatus = 0;
        return insertRet;
    }

    public String getCellBroadcastRanges() {
        log("getCellBroadcastChannels");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(110);
            this.mSmsCbChannelConfig = UsimPBMemInfo.STRING_NOT_SET;
            this.mPhone.mCi.getGsmBroadcastConfig(response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get CB config");
            }
        }
        return this.mSmsCbChannelConfig;
    }

    public boolean setCellBroadcastLangs(String lang) {
        log("setCellBroadcastLangs");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(111);
            this.mSuccess = false;
            this.mPhone.mCi.setGsmBroadcastLangs(lang, response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get CB config");
            }
        }
        return this.mSuccess;
    }

    public String getCellBroadcastLangs() {
        log("getCellBroadcastLangs");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(112);
            this.mSmsCbLanguageConfig = UsimPBMemInfo.STRING_NOT_SET;
            this.mPhone.mCi.getGsmBroadcastLangs(response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get CB config");
            }
        }
        return this.mSmsCbLanguageConfig;
    }
}
