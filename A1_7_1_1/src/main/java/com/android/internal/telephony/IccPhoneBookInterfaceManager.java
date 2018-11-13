package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.mediatek.internal.telephony.uicc.AlphaTag;
import com.mediatek.internal.telephony.uicc.CsimPhbStorageInfo;
import com.mediatek.internal.telephony.uicc.UsimGroup;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class IccPhoneBookInterfaceManager {
    /* renamed from: -com-android-internal-telephony-CommandException$ErrorSwitchesValues */
    private static final /* synthetic */ int[] f20xa0f04c1a = null;
    protected static final boolean ALLOW_SIM_OP_IN_UI_THREAD = false;
    protected static final boolean DBG = false;
    protected static final int EVENT_GET_SIZE_DONE = 1;
    protected static final int EVENT_LOAD_DONE = 2;
    protected static final int EVENT_UPDATE_DONE = 3;
    static final String LOG_TAG = "IccPhoneBookIM";
    protected static final int OPPO_EVENT_ADN_FIELD_DONE = 102;
    public static final HandlerThread mHandlerThread = null;
    protected AdnRecordCache mAdnCache;
    protected final IccPbHandler mBaseHandler;
    private UiccCardApplication mCurrentApp;
    protected int mErrorCause;
    private boolean mIs3gCard;
    protected boolean mIsCsim;
    protected final Object mLock;
    protected Phone mPhone;
    protected int[] mRecordSize;
    protected List<AdnRecord> mRecords;
    protected int mSlotId;
    protected boolean mSuccess;
    protected boolean phonebookReady;
    protected int simNameLeng;
    private int simTotal;
    private int simUsed;
    protected int simrecord_efid;

    protected class IccPbHandler extends Handler {
        final /* synthetic */ IccPhoneBookInterfaceManager this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.IccPhoneBookInterfaceManager.IccPbHandler.<init>(com.android.internal.telephony.IccPhoneBookInterfaceManager, android.os.Looper):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public IccPbHandler(com.android.internal.telephony.IccPhoneBookInterfaceManager r1, android.os.Looper r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.IccPhoneBookInterfaceManager.IccPbHandler.<init>(com.android.internal.telephony.IccPhoneBookInterfaceManager, android.os.Looper):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IccPhoneBookInterfaceManager.IccPbHandler.<init>(com.android.internal.telephony.IccPhoneBookInterfaceManager, android.os.Looper):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.IccPhoneBookInterfaceManager.IccPbHandler.notifyPending(android.os.AsyncResult):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private void notifyPending(android.os.AsyncResult r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.IccPhoneBookInterfaceManager.IccPbHandler.notifyPending(android.os.AsyncResult):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IccPhoneBookInterfaceManager.IccPbHandler.notifyPending(android.os.AsyncResult):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.IccPhoneBookInterfaceManager.IccPbHandler.handleMessage(android.os.Message):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void handleMessage(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.IccPhoneBookInterfaceManager.IccPbHandler.handleMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IccPhoneBookInterfaceManager.IccPbHandler.handleMessage(android.os.Message):void");
        }
    }

    /* renamed from: -getcom-android-internal-telephony-CommandException$ErrorSwitchesValues */
    private static /* synthetic */ int[] m48x9944abe() {
        if (f20xa0f04c1a != null) {
            return f20xa0f04c1a;
        }
        int[] iArr = new int[Error.values().length];
        try {
            iArr[Error.ABORTED.ordinal()] = 18;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Error.ADDITIONAL_NUMBER_SAVE_FAILURE.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Error.ADDITIONAL_NUMBER_STRING_TOO_LONG.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Error.ADN_LIST_NOT_EXIST.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Error.CALL_BARRED.ordinal()] = 19;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Error.CC_CALL_HOLD_FAILED_CAUSED_BY_TERMINATED.ordinal()] = 20;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Error.DEVICE_IN_USE.ordinal()] = 21;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Error.DIAL_MODIFIED_TO_DIAL.ordinal()] = 22;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Error.DIAL_MODIFIED_TO_SS.ordinal()] = 23;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Error.DIAL_MODIFIED_TO_USSD.ordinal()] = 24;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Error.DIAL_STRING_TOO_LONG.ordinal()] = 4;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Error.EMAIL_NAME_TOOLONG.ordinal()] = 5;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Error.EMAIL_SIZE_LIMIT.ordinal()] = 6;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Error.EMPTY_RECORD.ordinal()] = 25;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Error.ENCODING_ERR.ordinal()] = 26;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Error.FDN_CHECK_FAILURE.ordinal()] = 27;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Error.GENERIC_FAILURE.ordinal()] = 7;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Error.ILLEGAL_SIM_OR_ME.ordinal()] = 28;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Error.IMS_PROVISION_NO_DEFAULT_ERROR.ordinal()] = 29;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Error.INTERNAL_ERR.ordinal()] = 30;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Error.INVALID_ARGUMENTS.ordinal()] = 31;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Error.INVALID_CALL_ID.ordinal()] = 32;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Error.INVALID_MODEM_STATE.ordinal()] = 33;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Error.INVALID_PARAMETER.ordinal()] = 34;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Error.INVALID_RESPONSE.ordinal()] = 35;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Error.INVALID_SIM_STATE.ordinal()] = 36;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Error.INVALID_SMSC_ADDRESS.ordinal()] = 37;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Error.INVALID_SMS_FORMAT.ordinal()] = 38;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Error.INVALID_STATE.ordinal()] = 39;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Error.LCE_NOT_SUPPORTED.ordinal()] = 40;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Error.MISSING_RESOURCE.ordinal()] = 41;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Error.MODEM_ERR.ordinal()] = 42;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Error.MODE_NOT_SUPPORTED.ordinal()] = 43;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Error.NETWORK_ERR.ordinal()] = 44;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Error.NETWORK_NOT_READY.ordinal()] = 45;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Error.NETWORK_REJECT.ordinal()] = 46;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Error.NOT_PROVISIONED.ordinal()] = 47;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Error.NOT_READY.ordinal()] = 8;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Error.NO_MEMORY.ordinal()] = 48;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Error.NO_NETWORK_FOUND.ordinal()] = 49;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Error.NO_RESOURCES.ordinal()] = 50;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Error.NO_SMS_TO_ACK.ordinal()] = 51;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Error.NO_SUBSCRIPTION.ordinal()] = 52;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Error.NO_SUCH_ELEMENT.ordinal()] = 53;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Error.NO_SUCH_ENTRY.ordinal()] = 54;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Error.OEM_ERROR_1.ordinal()] = 9;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Error.OEM_ERROR_10.ordinal()] = 55;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Error.OEM_ERROR_11.ordinal()] = 56;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Error.OEM_ERROR_12.ordinal()] = 57;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Error.OEM_ERROR_13.ordinal()] = 58;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Error.OEM_ERROR_14.ordinal()] = 59;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Error.OEM_ERROR_15.ordinal()] = 60;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Error.OEM_ERROR_16.ordinal()] = 61;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Error.OEM_ERROR_17.ordinal()] = 62;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Error.OEM_ERROR_18.ordinal()] = 63;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Error.OEM_ERROR_19.ordinal()] = 64;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Error.OEM_ERROR_2.ordinal()] = 10;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Error.OEM_ERROR_20.ordinal()] = 65;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Error.OEM_ERROR_21.ordinal()] = 66;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Error.OEM_ERROR_22.ordinal()] = 67;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Error.OEM_ERROR_23.ordinal()] = 68;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Error.OEM_ERROR_24.ordinal()] = 69;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Error.OEM_ERROR_25.ordinal()] = 70;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Error.OEM_ERROR_3.ordinal()] = 11;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Error.OEM_ERROR_4.ordinal()] = 71;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Error.OEM_ERROR_5.ordinal()] = 72;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Error.OEM_ERROR_6.ordinal()] = 73;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Error.OEM_ERROR_7.ordinal()] = 74;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Error.OEM_ERROR_8.ordinal()] = 75;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Error.OEM_ERROR_9.ordinal()] = 76;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[Error.OPERATION_NOT_ALLOWED.ordinal()] = 77;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[Error.OP_NOT_ALLOWED_BEFORE_REG_NW.ordinal()] = 78;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Error.OP_NOT_ALLOWED_DURING_VOICE_CALL.ordinal()] = 79;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Error.PASSWORD_INCORRECT.ordinal()] = 12;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Error.RADIO_NOT_AVAILABLE.ordinal()] = 80;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Error.REQUEST_NOT_SUPPORTED.ordinal()] = 81;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[Error.REQUEST_RATE_LIMITED.ordinal()] = 82;
        } catch (NoSuchFieldError e77) {
        }
        try {
            iArr[Error.SIM_ABSENT.ordinal()] = 83;
        } catch (NoSuchFieldError e78) {
        }
        try {
            iArr[Error.SIM_ALREADY_POWERED_OFF.ordinal()] = 84;
        } catch (NoSuchFieldError e79) {
        }
        try {
            iArr[Error.SIM_ALREADY_POWERED_ON.ordinal()] = 85;
        } catch (NoSuchFieldError e80) {
        }
        try {
            iArr[Error.SIM_BUSY.ordinal()] = 86;
        } catch (NoSuchFieldError e81) {
        }
        try {
            iArr[Error.SIM_DATA_NOT_AVAILABLE.ordinal()] = 87;
        } catch (NoSuchFieldError e82) {
        }
        try {
            iArr[Error.SIM_ERR.ordinal()] = 88;
        } catch (NoSuchFieldError e83) {
        }
        try {
            iArr[Error.SIM_FULL.ordinal()] = 89;
        } catch (NoSuchFieldError e84) {
        }
        try {
            iArr[Error.SIM_MEM_FULL.ordinal()] = 13;
        } catch (NoSuchFieldError e85) {
        }
        try {
            iArr[Error.SIM_PIN2.ordinal()] = 90;
        } catch (NoSuchFieldError e86) {
        }
        try {
            iArr[Error.SIM_PUK2.ordinal()] = 14;
        } catch (NoSuchFieldError e87) {
        }
        try {
            iArr[Error.SIM_SAP_CONNECT_FAILURE.ordinal()] = 91;
        } catch (NoSuchFieldError e88) {
        }
        try {
            iArr[Error.SIM_SAP_CONNECT_OK_CALL_ONGOING.ordinal()] = 92;
        } catch (NoSuchFieldError e89) {
        }
        try {
            iArr[Error.SIM_SAP_MSG_SIZE_TOO_LARGE.ordinal()] = 93;
        } catch (NoSuchFieldError e90) {
        }
        try {
            iArr[Error.SIM_SAP_MSG_SIZE_TOO_SMALL.ordinal()] = 94;
        } catch (NoSuchFieldError e91) {
        }
        try {
            iArr[Error.SMS_FAIL_RETRY.ordinal()] = 95;
        } catch (NoSuchFieldError e92) {
        }
        try {
            iArr[Error.SNE_NAME_TOOLONG.ordinal()] = 15;
        } catch (NoSuchFieldError e93) {
        }
        try {
            iArr[Error.SNE_SIZE_LIMIT.ordinal()] = 16;
        } catch (NoSuchFieldError e94) {
        }
        try {
            iArr[Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED.ordinal()] = 96;
        } catch (NoSuchFieldError e95) {
        }
        try {
            iArr[Error.SS_MODIFIED_TO_DIAL.ordinal()] = 97;
        } catch (NoSuchFieldError e96) {
        }
        try {
            iArr[Error.SS_MODIFIED_TO_SS.ordinal()] = 98;
        } catch (NoSuchFieldError e97) {
        }
        try {
            iArr[Error.SS_MODIFIED_TO_USSD.ordinal()] = 99;
        } catch (NoSuchFieldError e98) {
        }
        try {
            iArr[Error.SUBSCRIPTION_NOT_AVAILABLE.ordinal()] = 100;
        } catch (NoSuchFieldError e99) {
        }
        try {
            iArr[Error.SUBSCRIPTION_NOT_SUPPORTED.ordinal()] = 101;
        } catch (NoSuchFieldError e100) {
        }
        try {
            iArr[Error.SYSTEM_ERR.ordinal()] = 102;
        } catch (NoSuchFieldError e101) {
        }
        try {
            iArr[Error.TEXT_STRING_TOO_LONG.ordinal()] = 17;
        } catch (NoSuchFieldError e102) {
        }
        try {
            iArr[Error.USSD_MODIFIED_TO_DIAL.ordinal()] = Phone.OEM_PRODUCT_16391;
        } catch (NoSuchFieldError e103) {
        }
        try {
            iArr[Error.USSD_MODIFIED_TO_SS.ordinal()] = 104;
        } catch (NoSuchFieldError e104) {
        }
        try {
            iArr[Error.USSD_MODIFIED_TO_USSD.ordinal()] = 105;
        } catch (NoSuchFieldError e105) {
        }
        try {
            iArr[Error.UT_UNKNOWN_HOST.ordinal()] = 106;
        } catch (NoSuchFieldError e106) {
        }
        try {
            iArr[Error.UT_XCAP_403_FORBIDDEN.ordinal()] = Phone.OEM_PRODUCT_17373;
        } catch (NoSuchFieldError e107) {
        }
        try {
            iArr[Error.UT_XCAP_404_NOT_FOUND.ordinal()] = Phone.OEM_PRODUCT_17375;
        } catch (NoSuchFieldError e108) {
        }
        try {
            iArr[Error.UT_XCAP_409_CONFLICT.ordinal()] = 109;
        } catch (NoSuchFieldError e109) {
        }
        f20xa0f04c1a = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IccPhoneBookInterfaceManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IccPhoneBookInterfaceManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IccPhoneBookInterfaceManager.<clinit>():void");
    }

    public IccPhoneBookInterfaceManager(Phone phone) {
        this.mCurrentApp = null;
        this.mLock = new Object();
        this.mIs3gCard = false;
        this.mSlotId = -1;
        this.mIsCsim = false;
        this.mPhone = phone;
        this.phonebookReady = false;
        IccRecords r = phone.getIccRecords();
        if (r != null) {
            this.mAdnCache = r.getAdnCache();
        }
        this.mBaseHandler = new IccPbHandler(this, mHandlerThread.getLooper());
    }

    public void dispose() {
        logd("IccPhoneBookInterfaceManager: reset...");
        this.phonebookReady = false;
    }

    public void updateIccRecords(IccRecords iccRecords) {
        int i = -1;
        if (iccRecords != null) {
            this.mAdnCache = iccRecords.getAdnCache();
            if (this.mAdnCache != null) {
                i = this.mAdnCache.getSlotId();
            }
            this.mSlotId = i;
            if (!this.phonebookReady) {
                onPhbReady();
            }
            logi("[updateIccRecords] Set mAdnCache value");
            return;
        }
        this.mAdnCache = null;
        this.phonebookReady = false;
        logi("[updateIccRecords] Set mAdnCache value to null");
        this.mSlotId = -1;
    }

    protected void logd(String msg) {
        Rlog.d(LOG_TAG, "[IccPbInterfaceManager] " + msg + "(slot " + this.mSlotId + ")");
    }

    protected void loge(String msg) {
        Rlog.e(LOG_TAG, "[IccPbInterfaceManager] " + msg + "(slot " + this.mSlotId + ")");
    }

    protected void logi(String msg) {
        Rlog.i(LOG_TAG, "[IccPbInterfaceManager] " + msg + "(slot " + this.mSlotId + ")");
    }

    public boolean updateAdnRecordsInEfBySearch(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) {
        if (updateAdnRecordsInEfBySearchWithError(efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber, pin2) == 1) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:48:0x0117, code:
            if (r9.mErrorCause != 1) goto L_0x013c;
     */
    /* JADX WARNING: Missing block: B:49:0x0119, code:
            logi("updateAdnRecordsInEfBySearchWithError success index is " + r6);
     */
    /* JADX WARNING: Missing block: B:51:0x0131, code:
            return r6;
     */
    /* JADX WARNING: Missing block: B:60:0x013f, code:
            return r9.mErrorCause;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int updateAdnRecordsInEfBySearchWithError(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) {
        int index = -1;
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            loge("updateAdnRecordsInEfBySearchWithError mAdnCache is null");
            return 0;
        } else if (this.phonebookReady) {
            if (DBG) {
                logd("updateAdnRecordsInEfBySearch: efid=0x" + Integer.toHexString(efid).toUpperCase() + " (" + Rlog.pii(LOG_TAG, oldTag) + "," + Rlog.pii(LOG_TAG, oldPhoneNumber) + ")" + "==>" + " (" + Rlog.pii(LOG_TAG, newTag) + "," + Rlog.pii(LOG_TAG, newPhoneNumber) + ")" + " pin2=" + Rlog.pii(LOG_TAG, pin2));
            }
            efid = updateEfForIccType(efid);
            synchronized (this.mLock) {
                if (this.mAdnCache.hasCmdInProgress(efid)) {
                    logd("IccPhoneBookInterfaceManager: updateAdnRecordsInEfBySearchWithError: hasCmdInProgress");
                    return -10;
                }
                checkThread();
                this.mSuccess = false;
                AtomicBoolean status = new AtomicBoolean(false);
                Message response = this.mBaseHandler.obtainMessage(3, status);
                AdnRecord oldAdn = new AdnRecord(oldTag, oldPhoneNumber);
                if (newPhoneNumber == null) {
                    newPhoneNumber = UsimPBMemInfo.STRING_NOT_SET;
                }
                AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber);
                if (this.mAdnCache != null) {
                    index = this.mAdnCache.updateAdnBySearch(efid, oldAdn, newAdn, pin2, response);
                    waitForResult(status);
                } else {
                    loge("Failure while trying to update by search due to uninitialised adncache");
                }
            }
        } else {
            logd("IccPhoneBookInterfaceManager: updateAdnRecordsInEfBySearchWithError: phonebook not ready.");
            return 0;
        }
    }

    /* JADX WARNING: Missing block: B:39:0x0128, code:
            if (r15.mErrorCause != 1) goto L_0x0146;
     */
    /* JADX WARNING: Missing block: B:40:0x012a, code:
            logi("updateUsimPBRecordsInEfBySearchWithError success index is " + r10);
     */
    /* JADX WARNING: Missing block: B:42:0x0142, code:
            return r10;
     */
    /* JADX WARNING: Missing block: B:49:0x0149, code:
            return r15.mErrorCause;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int updateUsimPBRecordsInEfBySearchWithError(int efid, String oldTag, String oldPhoneNumber, String oldAnr, String oldGrpIds, String[] oldEmails, String newTag, String newPhoneNumber, String newAnr, String newGrpIds, String[] newEmails) {
        AtomicBoolean status = new AtomicBoolean(false);
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            loge("updateUsimPBRecordsInEfBySearchWithError mAdnCache is null");
            return 0;
        } else {
            if (DBG) {
                logd("updateUsimPBRecordsInEfBySearchWithError: efid=" + efid + " (" + oldTag + "," + oldPhoneNumber + "oldAnr" + oldAnr + " oldGrpIds " + oldGrpIds + ")" + "==>" + "(" + newTag + "," + newPhoneNumber + ")" + " newAnr= " + newAnr + " newGrpIds = " + newGrpIds + " newEmails = " + newEmails);
            }
            synchronized (this.mLock) {
                if (this.mAdnCache.hasCmdInProgress(efid)) {
                    return -10;
                }
                checkThread();
                this.mSuccess = false;
                Message response = this.mBaseHandler.obtainMessage(3, status);
                AdnRecord oldAdn = new AdnRecord(oldTag, oldPhoneNumber);
                if (newPhoneNumber == null) {
                    newPhoneNumber = UsimPBMemInfo.STRING_NOT_SET;
                }
                int index = this.mAdnCache.updateAdnBySearch(efid, oldAdn, new AdnRecord(0, 0, newTag, newPhoneNumber, newAnr, newEmails, newGrpIds), null, response);
                waitForResult(status);
            }
        }
    }

    public synchronized int updateUsimPBRecordsBySearchWithError(int efid, AdnRecord oldAdn, AdnRecord newAdn) {
        AtomicBoolean status = new AtomicBoolean(false);
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            loge("updateUsimPBRecordsBySearchWithError mAdnCache is null");
            return 0;
        } else {
            int index;
            if (DBG) {
                logd("updateUsimPBRecordsBySearchWithError: efid=" + efid + " (" + oldAdn + ")" + "==>" + "(" + newAdn + ")");
            }
            synchronized (this.mLock) {
                checkThread();
                this.mSuccess = false;
                Message response = this.mBaseHandler.obtainMessage(3, status);
                if (newAdn.getNumber() == null) {
                    newAdn.setNumber(UsimPBMemInfo.STRING_NOT_SET);
                }
                index = this.mAdnCache.updateAdnBySearch(efid, oldAdn, newAdn, null, response);
                waitForResult(status);
            }
            if (this.mErrorCause == 1) {
                logi("updateUsimPBRecordsBySearchWithError success index is " + index);
                return index;
            }
            return this.mErrorCause;
        }
    }

    public boolean updateAdnRecordsInEfByIndex(int efid, String newTag, String newPhoneNumber, int index, String pin2) {
        if (updateAdnRecordsInEfByIndexWithError(efid, newTag, newPhoneNumber, index, pin2) == 1) {
            return true;
        }
        return false;
    }

    public synchronized int updateAdnRecordsInEfByIndexWithError(int efid, String newTag, String newPhoneNumber, int index, String pin2) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            loge("updateAdnRecordsInEfByIndex mAdnCache is null");
            return 0;
        } else if (this.phonebookReady) {
            if (DBG) {
                logd("updateAdnRecordsInEfByIndex: efid=0x" + Integer.toHexString(efid).toUpperCase() + " Index=" + index + " ==> " + "(" + Rlog.pii(LOG_TAG, newTag) + "," + Rlog.pii(LOG_TAG, newPhoneNumber) + ")" + " pin2=" + Rlog.pii(LOG_TAG, pin2));
            }
            synchronized (this.mLock) {
                if (this.mAdnCache.hasCmdInProgress(efid)) {
                    return -10;
                }
                checkThread();
                this.mSuccess = false;
                AtomicBoolean status = new AtomicBoolean(false);
                Message response = this.mBaseHandler.obtainMessage(3, status);
                if (newPhoneNumber == null) {
                    newPhoneNumber = UsimPBMemInfo.STRING_NOT_SET;
                }
                AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber);
                if (this.mAdnCache != null) {
                    this.mAdnCache.updateAdnByIndex(efid, newAdn, index, pin2, response);
                    waitForResult(status);
                } else {
                    loge("Failure while trying to update by index due to uninitialised adncache");
                }
                return this.mErrorCause;
            }
        } else {
            logd("IccPhoneBookInterfaceManager: updateAdnRecordsInEfByIndex: phonebook not ready.");
            return 0;
        }
    }

    public synchronized int updateUsimPBRecordsInEfByIndexWithError(int efid, String newTag, String newPhoneNumber, String newAnr, String newGrpIds, String[] newEmails, int index) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            loge("updateUsimPBRecordsInEfByIndexWithError mAdnCache is null");
            return 0;
        } else {
            if (DBG) {
                logd("updateUsimPBRecordsInEfByIndexWithError: efid=" + efid + " Index=" + index + " ==> " + "(" + newTag + "," + newPhoneNumber + ")" + " newAnr= " + newAnr + " newGrpIds = " + newGrpIds + " newEmails = " + newEmails);
            }
            synchronized (this.mLock) {
                if (this.mAdnCache.hasCmdInProgress(efid)) {
                    return -10;
                }
                checkThread();
                this.mSuccess = false;
                AtomicBoolean status = new AtomicBoolean(false);
                Message response = this.mBaseHandler.obtainMessage(3, status);
                if (newPhoneNumber == null) {
                    newPhoneNumber = UsimPBMemInfo.STRING_NOT_SET;
                }
                this.mAdnCache.updateAdnByIndex(efid, new AdnRecord(efid, index, newTag, newPhoneNumber, newAnr, newEmails, newGrpIds), index, null, response);
                waitForResult(status);
                return this.mErrorCause;
            }
        }
    }

    public synchronized int updateUsimPBRecordsByIndexWithError(int efid, AdnRecord record, int index) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (this.mAdnCache == null) {
            loge("updateUsimPBRecordsByIndexWithError mAdnCache is null");
            return 0;
        } else {
            if (DBG) {
                logd("updateUsimPBRecordsByIndexWithError: efid=" + efid + " Index=" + index + " ==> " + record);
            }
            synchronized (this.mLock) {
                checkThread();
                this.mSuccess = false;
                AtomicBoolean status = new AtomicBoolean(false);
                this.mAdnCache.updateAdnByIndex(efid, record, index, null, this.mBaseHandler.obtainMessage(3, status));
                waitForResult(status);
            }
            return this.mErrorCause;
        }
    }

    private String getAdnEFPath(int efid) {
        if (efid == 28474) {
            return "3F007F10";
        }
        return null;
    }

    public int[] getAdnRecordsSize(int efid) {
        if (DBG) {
            logd("getAdnRecordsSize: efid=" + efid);
        }
        synchronized (this.mLock) {
            checkThread();
            this.mRecordSize = new int[3];
            AtomicBoolean status = new AtomicBoolean(false);
            Message response = this.mBaseHandler.obtainMessage(1, status);
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh != null) {
                if (getAdnEFPath(efid) != null) {
                    fh.getEFLinearRecordSize(efid, getAdnEFPath(efid), response);
                } else {
                    fh.getEFLinearRecordSize(efid, response);
                }
                waitForResult(status);
            }
        }
        return this.mRecordSize;
    }

    public synchronized List<AdnRecord> getAdnRecordsInEf(int efid) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
        }
        efid = updateEfForIccType(efid);
        if (DBG) {
            logd("getAdnRecordsInEF: efid=0x" + Integer.toHexString(efid).toUpperCase());
        }
        if (this.mAdnCache == null) {
            loge("getAdnRecordsInEF mAdnCache is null");
            return null;
        }
        synchronized (this.mLock) {
            if (this.mAdnCache.hasCmdInProgress(efid)) {
                return null;
            }
            checkThread();
            AtomicBoolean status = new AtomicBoolean(false);
            Message response = this.mBaseHandler.obtainMessage(2, status);
            if (this.mAdnCache != null) {
                this.mAdnCache.requestLoadAllAdnLike(efid, this.mAdnCache.extensionEfForEf(efid), response);
                waitForResult(status);
            } else {
                loge("Failure while trying to load from SIM due to uninitialised adncache");
            }
            return this.mRecords;
        }
    }

    protected void checkThread() {
        if (this.mBaseHandler.getLooper().equals(Looper.myLooper())) {
            loge("query() called on the main UI thread!");
            throw new IllegalStateException("You cannot call query on this provder from the main UI thread.");
        }
    }

    protected void waitForResult(AtomicBoolean status) {
        while (!status.get()) {
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                logd("interrupted while trying to update by search");
            }
        }
    }

    private int updateEfForIccType(int efid) {
        if (efid == 28474 && this.mPhone.getCurrentUiccAppType() == AppType.APPTYPE_USIM) {
            return IccConstants.EF_PBR;
        }
        return efid;
    }

    private int getErrorCauseFromException(CommandException e) {
        if (e == null) {
            return 1;
        }
        int ret;
        switch (m48x9944abe()[e.getCommandError().ordinal()]) {
            case 1:
                ret = -14;
                break;
            case 2:
                ret = -6;
                break;
            case 3:
                ret = -11;
                break;
            case 4:
            case 9:
                ret = -1;
                break;
            case 5:
                ret = -13;
                break;
            case 6:
                ret = -12;
                break;
            case 7:
                ret = -10;
                break;
            case 8:
                ret = -4;
                break;
            case 10:
            case 17:
                ret = -2;
                break;
            case 11:
            case 13:
                ret = -3;
                break;
            case 12:
            case 14:
                ret = -5;
                break;
            case 15:
                ret = -17;
                break;
            case 16:
                ret = -16;
                break;
            default:
                ret = 0;
                break;
        }
        return ret;
    }

    public void onPhbReady() {
        if (this.mAdnCache != null) {
            this.mAdnCache.requestLoadAllAdnLike(28474, this.mAdnCache.extensionEfForEf(28474), null);
            this.phonebookReady = isPhbReady();
        }
    }

    public boolean isPhbReady() {
        String strPhbReady = "false";
        String strAllSimState = UsimPBMemInfo.STRING_NOT_SET;
        String strCurSimState = UsimPBMemInfo.STRING_NOT_SET;
        boolean isSimLocked = false;
        int subId = this.mPhone.getSubId();
        int phoneId = this.mPhone.getPhoneId();
        int slotId = SubscriptionManager.getSlotId(subId);
        if (SubscriptionManager.isValidSlotId(slotId)) {
            strAllSimState = SystemProperties.get("gsm.sim.state");
            if (strAllSimState != null && strAllSimState.length() > 0) {
                String[] values = strAllSimState.split(",");
                if (phoneId >= 0 && phoneId < values.length && values[phoneId] != null) {
                    strCurSimState = values[phoneId];
                }
            }
            if (strCurSimState.equals("NETWORK_LOCKED")) {
                isSimLocked = true;
            } else {
                isSimLocked = strCurSimState.equals("PIN_REQUIRED");
            }
            if (this.mPhone.getPhoneType() == 1 || CsimPhbStorageInfo.isUsingGsmPhbReady(this.mPhone.getIccFileHandler())) {
                if (slotId == 0) {
                    strPhbReady = SystemProperties.get("gsm.sim.ril.phbready", "false");
                } else {
                    strPhbReady = SystemProperties.get("gsm.sim.ril.phbready." + (slotId + 1), "false");
                }
            } else if (slotId == 0) {
                strPhbReady = SystemProperties.get("cdma.sim.ril.phbready", "false");
            } else {
                strPhbReady = SystemProperties.get("cdma.sim.ril.phbready." + (slotId + 1), "false");
            }
        }
        logi("[isPhbReady] subId:" + subId + ", slotId: " + slotId + ", isPhbReady: " + strPhbReady + ",strSimState: " + strAllSimState + ", phoneType: " + this.mPhone.getPhoneType());
        if (!strPhbReady.equals("true") || isSimLocked) {
            return false;
        }
        return true;
    }

    public List<UsimGroup> getUsimGroups() {
        return this.mAdnCache == null ? null : this.mAdnCache.getUsimGroups();
    }

    public String getUsimGroupById(int nGasId) {
        return this.mAdnCache == null ? null : this.mAdnCache.getUsimGroupById(nGasId);
    }

    public boolean removeUsimGroupById(int nGasId) {
        return this.mAdnCache == null ? false : this.mAdnCache.removeUsimGroupById(nGasId);
    }

    public int insertUsimGroup(String grpName) {
        return this.mAdnCache == null ? -1 : this.mAdnCache.insertUsimGroup(grpName);
    }

    public int updateUsimGroup(int nGasId, String grpName) {
        return this.mAdnCache == null ? -1 : this.mAdnCache.updateUsimGroup(nGasId, grpName);
    }

    public boolean addContactToGroup(int adnIndex, int grpIndex) {
        return this.mAdnCache == null ? false : this.mAdnCache.addContactToGroup(adnIndex, grpIndex);
    }

    public boolean removeContactFromGroup(int adnIndex, int grpIndex) {
        return this.mAdnCache == null ? false : this.mAdnCache.removeContactFromGroup(adnIndex, grpIndex);
    }

    public boolean updateContactToGroups(int adnIndex, int[] grpIdList) {
        return this.mAdnCache == null ? false : this.mAdnCache.updateContactToGroups(adnIndex, grpIdList);
    }

    public boolean moveContactFromGroupsToGroups(int adnIndex, int[] fromGrpIdList, int[] toGrpIdList) {
        if (this.mAdnCache == null) {
            return false;
        }
        return this.mAdnCache.moveContactFromGroupsToGroups(adnIndex, fromGrpIdList, toGrpIdList);
    }

    public int hasExistGroup(String grpName) {
        return this.mAdnCache == null ? -1 : this.mAdnCache.hasExistGroup(grpName);
    }

    public int getUsimGrpMaxNameLen() {
        return this.mAdnCache == null ? -1 : this.mAdnCache.getUsimGrpMaxNameLen();
    }

    public int getUsimGrpMaxCount() {
        return this.mAdnCache == null ? -1 : this.mAdnCache.getUsimGrpMaxCount();
    }

    public List<AlphaTag> getUsimAasList() {
        return this.mAdnCache == null ? null : this.mAdnCache.getUsimAasList();
    }

    public String getUsimAasById(int index) {
        return this.mAdnCache == null ? null : this.mAdnCache.getUsimAasById(index);
    }

    public boolean removeUsimAasById(int index, int pbrIndex) {
        return this.mAdnCache == null ? false : this.mAdnCache.removeUsimAasById(index, pbrIndex);
    }

    public int insertUsimAas(String aasName) {
        return this.mAdnCache == null ? -1 : this.mAdnCache.insertUsimAas(aasName);
    }

    public boolean updateUsimAas(int index, int pbrIndex, String aasName) {
        return this.mAdnCache == null ? false : this.mAdnCache.updateUsimAas(index, pbrIndex, aasName);
    }

    public boolean updateAdnAas(int adnIndex, int aasIndex) {
        return this.mAdnCache == null ? false : this.mAdnCache.updateAdnAas(adnIndex, aasIndex);
    }

    public int getAnrCount() {
        return this.mAdnCache == null ? 0 : this.mAdnCache.getAnrCount();
    }

    public int getEmailCount() {
        return this.mAdnCache == null ? 0 : this.mAdnCache.getEmailCount();
    }

    public int getUsimAasMaxCount() {
        return this.mAdnCache == null ? -1 : this.mAdnCache.getUsimAasMaxCount();
    }

    public int getUsimAasMaxNameLen() {
        return this.mAdnCache == null ? -1 : this.mAdnCache.getUsimAasMaxNameLen();
    }

    public boolean hasSne() {
        return this.mAdnCache == null ? false : this.mAdnCache.hasSne();
    }

    public int getSneRecordLen() {
        return this.mAdnCache == null ? -1 : this.mAdnCache.getSneRecordLen();
    }

    public boolean isAdnAccessible() {
        return this.mAdnCache == null ? false : this.mAdnCache.isAdnAccessible();
    }

    public synchronized UsimPBMemInfo[] getPhonebookMemStorageExt() {
        UsimPBMemInfo[] usimPBMemInfoArr = null;
        synchronized (this) {
            if (this.mAdnCache != null) {
                usimPBMemInfoArr = this.mAdnCache.getPhonebookMemStorageExt();
            }
        }
        return usimPBMemInfoArr;
    }

    public int getUpbDone() {
        return this.mAdnCache == null ? -1 : this.mAdnCache.getUpbDone();
    }

    public int[] getAdnRecordsCapacity() {
        return this.mAdnCache == null ? null : this.mAdnCache.getAdnRecordsCapacity();
    }

    public int oppoGetAdnEmailLen() {
        return 30;
    }

    public int oppoGetSimPhonebookAllSpace() {
        if (this.phonebookReady) {
            logd("IccPhoneBookInterfaceManager: oppoGetSimPhonebookAllSpace: simrecord_efid:" + this.simrecord_efid);
            if (!(this.simrecord_efid == 28474 || this.simrecord_efid == IccConstants.EF_PBR)) {
                if (this.mPhone.getCurrentUiccAppType() == AppType.APPTYPE_USIM) {
                    this.simrecord_efid = IccConstants.EF_PBR;
                } else {
                    this.simrecord_efid = 28474;
                }
                getAdnRecordsInEf(this.simrecord_efid);
            }
            if (this.mRecords != null) {
                this.simTotal = this.mRecords.size();
            } else {
                this.simTotal = 0;
            }
            logd("oppoGetSimPhonebookAllSpace:" + this.simTotal);
            return this.simTotal;
        }
        logd("oppoGetSimPhonebookAllSpace: phonebook not ready");
        return -1;
    }

    public int oppoGetSimPhonebookUsedSpace() {
        logd("oppoGetSimPhonebookUsedSpace");
        if (this.phonebookReady) {
            logd("IccPhoneBookInterfaceManager: oppoGetSimPhonebookUsedSpace: simrecord_efid:" + this.simrecord_efid);
            if (!(this.simrecord_efid == 28474 || this.simrecord_efid == IccConstants.EF_PBR)) {
                if (this.mPhone.getCurrentUiccAppType() == AppType.APPTYPE_USIM) {
                    this.simrecord_efid = IccConstants.EF_PBR;
                } else {
                    this.simrecord_efid = 28474;
                }
                getAdnRecordsInEf(this.simrecord_efid);
            }
            this.simUsed = 0;
            if (this.mRecords != null) {
                int N = this.mRecords.size();
                for (int i = 0; i < N; i++) {
                    if (!((AdnRecord) this.mRecords.get(i)).isEmpty()) {
                        this.simUsed++;
                    }
                }
            }
            logd("oppoGetSimPhonebookUsedSpace:" + this.simUsed);
            return this.simUsed;
        }
        logd("oppoGetSimPhonebookUsedSpace: phonebook not ready");
        return -1;
    }

    public AdnRecord oppoGetAndRecordByIndex(int index) {
        logd("guixiang0304 oppoGetAndRecordByIndex index = " + index);
        if (this.mRecords == null || index < 0 || index >= this.mRecords.size()) {
            return null;
        }
        return (AdnRecord) this.mRecords.get(index);
    }

    public int oppoGetSimPhonebookNameLength() {
        int i = 14;
        if (!this.phonebookReady) {
            return -1;
        }
        if (this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.ct.optr")) {
            this.simNameLeng = 14;
            return this.simNameLeng;
        }
        if (this.simNameLeng <= 0) {
            checkThread();
            getAdnRecordsSize(28474);
            if (this.mRecordSize != null && this.mRecordSize.length == 3) {
                this.simNameLeng = this.mRecordSize[0] - 14;
            }
        }
        if (OemConstant.EXP_VERSION) {
            this.simNameLeng = this.simNameLeng <= 0 ? 10 : this.simNameLeng;
        } else {
            if (this.simNameLeng > 0) {
                i = this.simNameLeng;
            }
            this.simNameLeng = i;
        }
        logd("oppoGetSimPhonebookNameLength  v1 simNameLeng:" + this.simNameLeng);
        return this.simNameLeng;
    }

    public int oppoAddAdnRecordsInEfBySearchEx(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber1, String newPhoneNumber2, String pin2, String email) {
        int index = -1;
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (!this.phonebookReady || this.mAdnCache == null) {
            return -1;
        } else {
            synchronized (this.mLock) {
                checkThread();
                this.mSuccess = false;
                AtomicBoolean status = new AtomicBoolean(false);
                Message response = this.mBaseHandler.obtainMessage(3, status);
                AdnRecord oldAdn = new AdnRecord(oldTag, oldPhoneNumber);
                AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber1);
                if (this.mAdnCache != null) {
                    if (newPhoneNumber2 != null) {
                        newAdn.setNumber2(newPhoneNumber2);
                    }
                    if (email != null) {
                        String[] strArr = new String[1];
                        strArr[0] = email.toString();
                        newAdn.setEmails(strArr);
                    }
                    index = this.mAdnCache.oppoUpdateAdnBySearch(efid, oldAdn, newAdn, pin2, response);
                    waitForResult(status);
                }
            }
            if (!this.mSuccess) {
                index = -1;
            }
            return index;
        }
    }

    public boolean oppoUpdateAdnRecordsInEfByIndexEx(int efid, String newTag, String newPhoneNumber1, String newPhoneNumber2, int index, String pin2, String email) {
        if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
            throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
        } else if (!this.phonebookReady || this.mAdnCache == null) {
            return false;
        } else {
            synchronized (this.mLock) {
                checkThread();
                this.mSuccess = false;
                AtomicBoolean status = new AtomicBoolean(false);
                Message response = this.mBaseHandler.obtainMessage(3, status);
                AdnRecord newAdn = new AdnRecord(efid, index, newTag, newPhoneNumber1);
                if (newPhoneNumber2 != null) {
                    newAdn.setNumber2(newPhoneNumber2);
                }
                if (email != null) {
                    String[] strArr = new String[1];
                    strArr[0] = email.toString();
                    newAdn.setEmails(strArr);
                }
                this.mAdnCache.oppoUpdateAdnByIndex(efid, this.mAdnCache.extensionEfForEf(efid), newAdn, index, pin2, response);
                waitForResult(status);
            }
            return this.mSuccess;
        }
    }

    public boolean isPhoneBookReady() {
        return this.phonebookReady;
    }

    public void broadcastIccPhoneBookReadyIntent(String value, String reason) {
        Intent intent = new Intent("android.intent.action.PBM_STATE_READY");
        intent.putExtra("pbstate", value);
        logd("Broadcasting intent ACTION_PBM_STATE_READY" + value + " reason " + reason);
        ActivityManagerNative.broadcastStickyIntent(intent, null, -1);
    }

    public boolean isPhoneBookPbrExist() {
        return this.mAdnCache.oppoCheckPbrIsExsit();
    }

    public String colorGetIccCardType() {
        String vRet = UsimPBMemInfo.STRING_NOT_SET;
        if (this.mIsCsim) {
            vRet = "CSIM";
        } else if (this.mIs3gCard) {
            vRet = "USIM";
        } else {
            vRet = "SIM";
        }
        logd("colorGetIccCardType---->" + vRet);
        return vRet;
    }
}
