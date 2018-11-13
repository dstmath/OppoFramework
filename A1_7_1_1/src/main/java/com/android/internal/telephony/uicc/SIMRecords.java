package com.android.internal.telephony.uicc;

import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.oppo.Telephony.SimInfo;
import android.provider.oppo.Telephony.WapPush;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.gsm.SimTlv;
import com.android.internal.telephony.regionlock.RegionLockConstant;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccConstants.IccService;
import com.android.internal.telephony.uicc.IccConstants.IccServiceStatus;
import com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded;
import com.android.internal.telephony.uicc.UsimServiceTable.UsimService;
import com.mediatek.common.MPlugin;
import com.mediatek.common.telephony.ITelephonyExt;
import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
import com.mediatek.internal.telephony.ppl.PplSmsFilterExtension;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import com.mediatek.internal.telephony.worldphone.IWorldPhone;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

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
public class SIMRecords extends IccRecords {
    /* renamed from: -com-android-internal-telephony-uicc-SIMRecords$GetSpnFsmStateSwitchesValues */
    private static final /* synthetic */ int[] f11x102e4fe = null;
    static final int CFF_LINE1_MASK = 15;
    static final int CFF_LINE1_RESET = 240;
    static final int CFF_UNCONDITIONAL_ACTIVE = 10;
    static final int CFF_UNCONDITIONAL_DEACTIVE = 5;
    private static final int CFIS_ADN_CAPABILITY_ID_OFFSET = 14;
    private static final int CFIS_ADN_EXTENSION_ID_OFFSET = 15;
    private static final int CFIS_BCD_NUMBER_LENGTH_OFFSET = 2;
    private static final int CFIS_TON_NPI_OFFSET = 3;
    private static final String CHANGE_TO_REGION = "change_to_region";
    private static final int CPHS_SST_MBN_ENABLED = 48;
    private static final int CPHS_SST_MBN_MASK = 48;
    private static final boolean CRASH_RIL = false;
    private static final int EVENT_APP_LOCKED = 35;
    private static final int EVENT_CARRIER_CONFIG_CHANGED = 37;
    private static final int EVENT_CFU_IND = 211;
    private static final int EVENT_DELAYED_SEND_PHB_CHANGE = 200;
    private static final int EVENT_DUAL_IMSI_READY = 44;
    private static final int EVENT_EF_CSP_PLMN_MODE_BIT_CHANGED = 203;
    protected static final int EVENT_GET_AD_DONE = 9;
    private static final int EVENT_GET_ALL_OPL_DONE = 104;
    private static final int EVENT_GET_ALL_SMS_DONE = 18;
    private static final int EVENT_GET_CFF_DONE = 24;
    private static final int EVENT_GET_CFIS_DONE = 32;
    private static final int EVENT_GET_CPHSONS_DONE = 105;
    private static final int EVENT_GET_CPHS_MAILBOX_DONE = 11;
    private static final int EVENT_GET_CSP_CPHS_DONE = 33;
    private static final int EVENT_GET_EF_ICCID_DONE = 300;
    private static final int EVENT_GET_ELP_DONE = 43;
    private static final int EVENT_GET_GBABP_DONE = 209;
    private static final int EVENT_GET_GBANL_DONE = 210;
    private static final int EVENT_GET_GID1_DONE = 34;
    private static final int EVENT_GET_GID2_DONE = 36;
    private static final int EVENT_GET_ICCID_DONE = 4;
    private static final int EVENT_GET_IMSI_DONE = 3;
    private static final int EVENT_GET_INFO_CPHS_DONE = 26;
    private static final int EVENT_GET_LI_DONE = 42;
    private static final int EVENT_GET_MBDN_DONE = 6;
    private static final int EVENT_GET_MBI_DONE = 5;
    protected static final int EVENT_GET_MSISDN_DONE = 10;
    private static final int EVENT_GET_MWIS_DONE = 7;
    private static final int EVENT_GET_NEW_MSISDN_DONE = 206;
    private static final int EVENT_GET_PNN_DONE = 15;
    private static final int EVENT_GET_PSISMSC_DONE = 207;
    private static final int EVENT_GET_RAT_DONE = 204;
    private static final int EVENT_GET_SHORT_CPHSONS_DONE = 106;
    private static final int EVENT_GET_SIM_ECC_DONE = 102;
    private static final int EVENT_GET_SMSP_DONE = 208;
    private static final int EVENT_GET_SMS_DONE = 22;
    private static final int EVENT_GET_SPDI_DONE = 13;
    private static final int EVENT_GET_SPN_DONE = 12;
    protected static final int EVENT_GET_SST_DONE = 17;
    private static final int EVENT_GET_USIM_ECC_DONE = 103;
    private static final int EVENT_GET_VOICE_MAIL_INDICATOR_CPHS_DONE = 8;
    private static final int EVENT_IMSI_REFRESH_QUERY = 212;
    private static final int EVENT_IMSI_REFRESH_QUERY_DONE = 213;
    private static final int EVENT_MARK_SMS_READ_DONE = 19;
    private static final int EVENT_MELOCK_CHANGED = 400;
    private static final int EVENT_QUERY_ICCID_DONE = 107;
    private static final int EVENT_QUERY_ICCID_DONE_FOR_HOT_SWAP = 205;
    private static final int EVENT_QUERY_MENU_TITLE_DONE = 53;
    private static final int EVENT_RADIO_AVAILABLE = 41;
    private static final int EVENT_RADIO_STATE_CHANGED = 201;
    private static final int EVENT_SET_CPHS_MAILBOX_DONE = 25;
    private static final int EVENT_SET_MBDN_DONE = 20;
    private static final int EVENT_SIM_REFRESH = 31;
    private static final int EVENT_SMS_ON_SIM = 21;
    private static final int EVENT_UPDATE_DONE = 14;
    private static final String[] INDIA_AIRTEL_PLMN = null;
    private static final String[] INDIA_TATA_DOCOMO_PLMN = null;
    private static final String KEY_SIM_ID = "SIM_ID";
    private static final String[] LANGUAGE_CODE_FOR_LP = null;
    protected static final String LOG_TAG = "SIMRecords";
    private static final String[] MCCMNC_CODES_HAVING_3DIGITS_MNC = null;
    static final String[] SIMRECORD_PROPERTY_RIL_PHB_READY = null;
    static final String[] SIMRECORD_PROPERTY_RIL_PUK1 = null;
    static final int TAG_FULL_NETWORK_NAME = 67;
    static final int TAG_SHORT_NETWORK_NAME = 69;
    static final int TAG_SPDI = 163;
    static final int TAG_SPDI_PLMN_LIST = 128;
    private static final int[] simServiceNumber = null;
    private static final int[] usimServiceNumber = null;
    private String[] SIM_RECORDS_PROPERTY_ECC_LIST;
    String cphsOnsl;
    String cphsOnss;
    private int efLanguageToLoad;
    private boolean hasQueryIccId;
    private int iccIdQueryState;
    private boolean isDispose;
    private boolean isValidMBI;
    private int mCallForwardingStatus;
    private byte[] mCphsInfo;
    boolean mCspPlmnEnabled;
    byte[] mEfCPHS_MWI;
    byte[] mEfCff;
    byte[] mEfCfis;
    private byte[] mEfELP;
    String mEfEcc;
    private ArrayList<byte[]> mEfGbanlList;
    byte[] mEfLi;
    byte[] mEfMWIS;
    byte[] mEfPl;
    private byte[] mEfPsismsc;
    private byte[] mEfRat;
    private boolean mEfRatLoaded;
    private byte[] mEfSST;
    private byte[] mEfSmsp;
    private String mGbabp;
    private String[] mGbanl;
    private boolean mIsPhbEfResetDone;
    private String mMenuTitleFromEf;
    private ArrayList<OplRecord> mOperatorList;
    private boolean mPhbReady;
    private boolean mPhbWaitSub;
    private Phone mPhone;
    String mPnnHomeName;
    private ArrayList<OperatorName> mPnnNetworkNames;
    private RadioTechnologyChangedReceiver mRTC;
    private boolean mReadingOpl;
    private final BroadcastReceiver mReceiver;
    private boolean mSIMInfoReady;
    private String mSimImsi;
    private BroadcastReceiver mSimReceiver;
    int mSlotId;
    private String mSpNameInEfSpn;
    ArrayList<String> mSpdiNetworks;
    int mSpnDisplayCondition;
    SpnOverride mSpnOverride;
    private GetSpnFsmState mSpnState;
    private BroadcastReceiver mSubReceiver;
    private ITelephonyExt mTelephonyExt;
    private UiccCard mUiccCard;
    private UiccController mUiccController;
    UsimServiceTable mUsimServiceTable;
    VoiceMailConstants mVmConfig;

    private class EfPlLoaded implements IccRecordLoaded {
        /* synthetic */ EfPlLoaded(SIMRecords this$0, EfPlLoaded efPlLoaded) {
            this();
        }

        private EfPlLoaded() {
        }

        public String getEfName() {
            return "EF_PL";
        }

        public void onRecordLoaded(AsyncResult ar) {
            SIMRecords.this.mEfPl = (byte[]) ar.result;
            SIMRecords.this.log("EF_PL=" + IccUtils.bytesToHexString(SIMRecords.this.mEfPl));
        }
    }

    private class EfUsimLiLoaded implements IccRecordLoaded {
        /* synthetic */ EfUsimLiLoaded(SIMRecords this$0, EfUsimLiLoaded efUsimLiLoaded) {
            this();
        }

        private EfUsimLiLoaded() {
        }

        public String getEfName() {
            return "EF_LI";
        }

        public void onRecordLoaded(AsyncResult ar) {
            SIMRecords.this.mEfLi = (byte[]) ar.result;
            SIMRecords.this.log("EF_LI=" + IccUtils.bytesToHexString(SIMRecords.this.mEfLi));
        }
    }

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private enum GetSpnFsmState {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.SIMRecords.GetSpnFsmState.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.SIMRecords.GetSpnFsmState.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.SIMRecords.GetSpnFsmState.<clinit>():void");
        }
    }

    public static class OperatorName {
        public String sFullName;
        public String sShortName;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.SIMRecords.OperatorName.<init>():void, dex: 
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
        public OperatorName() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.SIMRecords.OperatorName.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.SIMRecords.OperatorName.<init>():void");
        }
    }

    public static class OplRecord {
        public int nMaxLAC;
        public int nMinLAC;
        public int nPnnIndex;
        public String sPlmn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.SIMRecords.OplRecord.<init>():void, dex: 
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
        public OplRecord() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.SIMRecords.OplRecord.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.SIMRecords.OplRecord.<init>():void");
        }
    }

    private class RadioTechnologyChangedReceiver extends BroadcastReceiver {
        final /* synthetic */ SIMRecords this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.uicc.SIMRecords.RadioTechnologyChangedReceiver.<init>(com.android.internal.telephony.uicc.SIMRecords):void, dex: 
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
        private RadioTechnologyChangedReceiver(com.android.internal.telephony.uicc.SIMRecords r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.uicc.SIMRecords.RadioTechnologyChangedReceiver.<init>(com.android.internal.telephony.uicc.SIMRecords):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.SIMRecords.RadioTechnologyChangedReceiver.<init>(com.android.internal.telephony.uicc.SIMRecords):void");
        }

        /* synthetic */ RadioTechnologyChangedReceiver(SIMRecords this$0, RadioTechnologyChangedReceiver radioTechnologyChangedReceiver) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.uicc.SIMRecords.RadioTechnologyChangedReceiver.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.uicc.SIMRecords.RadioTechnologyChangedReceiver.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.SIMRecords.RadioTechnologyChangedReceiver.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    private class RebootClickListener implements OnClickListener {
        final /* synthetic */ SIMRecords this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.uicc.SIMRecords.RebootClickListener.<init>(com.android.internal.telephony.uicc.SIMRecords):void, dex: 
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
        private RebootClickListener(com.android.internal.telephony.uicc.SIMRecords r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.uicc.SIMRecords.RebootClickListener.<init>(com.android.internal.telephony.uicc.SIMRecords):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.SIMRecords.RebootClickListener.<init>(com.android.internal.telephony.uicc.SIMRecords):void");
        }

        /* synthetic */ RebootClickListener(SIMRecords this$0, RebootClickListener rebootClickListener) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.uicc.SIMRecords.RebootClickListener.onClick(android.content.DialogInterface, int):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.uicc.SIMRecords.RebootClickListener.onClick(android.content.DialogInterface, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.SIMRecords.RebootClickListener.onClick(android.content.DialogInterface, int):void");
        }
    }

    private class SIMBroadCastReceiver extends BroadcastReceiver {
        final /* synthetic */ SIMRecords this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.uicc.SIMRecords.SIMBroadCastReceiver.<init>(com.android.internal.telephony.uicc.SIMRecords):void, dex: 
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
        private SIMBroadCastReceiver(com.android.internal.telephony.uicc.SIMRecords r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.uicc.SIMRecords.SIMBroadCastReceiver.<init>(com.android.internal.telephony.uicc.SIMRecords):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.SIMRecords.SIMBroadCastReceiver.<init>(com.android.internal.telephony.uicc.SIMRecords):void");
        }

        /* synthetic */ SIMBroadCastReceiver(SIMRecords this$0, SIMBroadCastReceiver sIMBroadCastReceiver) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.uicc.SIMRecords.SIMBroadCastReceiver.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.uicc.SIMRecords.SIMBroadCastReceiver.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.SIMRecords.SIMBroadCastReceiver.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    private class SubBroadCastReceiver extends BroadcastReceiver {
        final /* synthetic */ SIMRecords this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.uicc.SIMRecords.SubBroadCastReceiver.<init>(com.android.internal.telephony.uicc.SIMRecords):void, dex: 
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
        private SubBroadCastReceiver(com.android.internal.telephony.uicc.SIMRecords r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.uicc.SIMRecords.SubBroadCastReceiver.<init>(com.android.internal.telephony.uicc.SIMRecords):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.SIMRecords.SubBroadCastReceiver.<init>(com.android.internal.telephony.uicc.SIMRecords):void");
        }

        /* synthetic */ SubBroadCastReceiver(SIMRecords this$0, SubBroadCastReceiver subBroadCastReceiver) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.uicc.SIMRecords.SubBroadCastReceiver.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.uicc.SIMRecords.SubBroadCastReceiver.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.SIMRecords.SubBroadCastReceiver.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    /* renamed from: -getcom-android-internal-telephony-uicc-SIMRecords$GetSpnFsmStateSwitchesValues */
    private static /* synthetic */ int[] m19xf89fffa2() {
        if (f11x102e4fe != null) {
            return f11x102e4fe;
        }
        int[] iArr = new int[GetSpnFsmState.values().length];
        try {
            iArr[GetSpnFsmState.IDLE.ordinal()] = 5;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[GetSpnFsmState.INIT.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[GetSpnFsmState.READ_SPN_3GPP.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[GetSpnFsmState.READ_SPN_CPHS.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[GetSpnFsmState.READ_SPN_SHORT_CPHS.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        f11x102e4fe = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.SIMRecords.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.SIMRecords.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.SIMRecords.<clinit>():void");
    }

    public String toString() {
        return "SimRecords: " + super.toString() + " mVmConfig" + this.mVmConfig + " mSpnOverride=" + "mSpnOverride" + " callForwardingEnabled=" + this.mCallForwardingStatus + " spnState=" + this.mSpnState + " mCphsInfo=" + this.mCphsInfo + " mCspPlmnEnabled=" + this.mCspPlmnEnabled + " efMWIS=" + this.mEfMWIS + " efCPHS_MWI=" + this.mEfCPHS_MWI + " mEfCff=" + this.mEfCff + " mEfCfis=" + this.mEfCfis + " getOperatorNumeric=" + getOperatorNumeric();
    }

    public SIMRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mCphsInfo = null;
        this.mCspPlmnEnabled = true;
        this.mEfMWIS = null;
        this.mEfCPHS_MWI = null;
        this.mEfCff = null;
        this.mEfCfis = null;
        this.mEfLi = null;
        this.mEfPl = null;
        this.mSpdiNetworks = null;
        this.mPnnHomeName = null;
        this.isValidMBI = false;
        this.mEfRatLoaded = false;
        this.mEfRat = null;
        this.iccIdQueryState = -1;
        this.efLanguageToLoad = 0;
        this.mIsPhbEfResetDone = false;
        this.mSimImsi = null;
        this.mEfSST = null;
        this.mEfELP = null;
        this.mEfPsismsc = null;
        this.mEfSmsp = null;
        String[] strArr = new String[4];
        strArr[0] = "ril.ecclist";
        strArr[1] = "ril.ecclist1";
        strArr[2] = "ril.ecclist2";
        strArr[3] = "ril.ecclist3";
        this.SIM_RECORDS_PROPERTY_ECC_LIST = strArr;
        this.mPhbReady = false;
        this.mPhbWaitSub = false;
        this.mSIMInfoReady = false;
        this.mPnnNetworkNames = null;
        this.mOperatorList = null;
        this.mSpNameInEfSpn = null;
        this.mMenuTitleFromEf = null;
        this.isDispose = false;
        this.mEfEcc = UsimPBMemInfo.STRING_NOT_SET;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    SIMRecords.this.sendMessage(SIMRecords.this.obtainMessage(37));
                }
            }
        };
        this.mReadingOpl = false;
        this.mSlotId = app.getSlotId();
        this.mUiccController = UiccController.getInstance();
        this.mUiccCard = this.mUiccController.getUiccCard(this.mSlotId);
        log("mUiccCard Instance = " + this.mUiccCard);
        this.mPhone = PhoneFactory.getPhone(app.getPhoneId());
        this.mAdnCache = new AdnRecordCache(this.mFh, ci, app);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.ACTION_PHONE_RESTART");
        intent.putExtra(PplSmsFilterExtension.INSTRUCTION_KEY_SIM_ID, this.mSlotId);
        this.mContext.sendBroadcast(intent);
        this.mVmConfig = new VoiceMailConstants();
        this.mSpnOverride = SpnOverride.getInstance();
        this.mRecordsRequested = false;
        this.mRecordsToLoad = 0;
        this.cphsOnsl = null;
        this.cphsOnss = null;
        this.hasQueryIccId = false;
        this.mCi.setOnSmsOnSim(this, 21, null);
        this.mCi.registerForIccRefresh(this, 31, null);
        this.mCi.registerForPhbReady(this, 410, null);
        this.mCi.registerForCallForwardingInfo(this, 211, null);
        this.mCi.registerForRadioStateChanged(this, 201, null);
        this.mCi.registerForAvailable(this, 41, null);
        this.mCi.registerForEfCspPlmnModeBitChanged(this, EVENT_EF_CSP_PLMN_MODE_BIT_CHANGED, null);
        this.mCi.registerForMelockChanged(this, EVENT_MELOCK_CHANGED, null);
        this.mCi.registerForImsiRefreshDone(this, 212, null);
        resetRecords();
        this.mParentApp.registerForReady(this, 1, null);
        this.mParentApp.registerForLocked(this, 35, null);
        this.mSimReceiver = new SIMBroadCastReceiver(this, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.mediatek.dm.LAWMO_WIPE");
        filter.addAction("action_pin_dismiss");
        filter.addAction("action_melock_dismiss");
        filter.addAction(IWorldPhone.ACTION_SHUTDOWN_IPO);
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        this.mContext.registerReceiver(this.mSimReceiver, filter);
        this.mSubReceiver = new SubBroadCastReceiver(this, null);
        IntentFilter subFilter = new IntentFilter();
        subFilter.addAction("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        this.mContext.registerReceiver(this.mSubReceiver, subFilter);
        this.mRTC = new RadioTechnologyChangedReceiver(this, null);
        IntentFilter rtcFilter = new IntentFilter();
        rtcFilter.addAction("android.intent.action.RADIO_TECHNOLOGY");
        this.mContext.registerReceiver(this.mRTC, rtcFilter);
        log("SIMRecords updateIccRecords");
        if (this.mPhone.getIccPhoneBookInterfaceManager() != null) {
            this.mPhone.getIccPhoneBookInterfaceManager().updateIccRecords(this);
        }
        if (isPhbReady()) {
            log("Phonebook is ready.");
            this.mPhbReady = true;
            broadcastPhbStateChangedIntent(this.mPhbReady);
        }
        try {
            this.mTelephonyExt = (ITelephonyExt) MPlugin.createInstance(ITelephonyExt.class.getName(), this.mContext);
        } catch (Exception e) {
            loge("Fail to create plug-in");
            e.printStackTrace();
        }
        log("SIMRecords X ctor this=" + this);
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        c.registerReceiver(this.mReceiver, intentfilter);
    }

    public void dispose() {
        log("Disposing SIMRecords this=" + this);
        this.isDispose = true;
        this.mCi.unregisterForIccRefresh(this);
        this.mCi.unSetOnSmsOnSim(this);
        this.mCi.unregisterForCallForwardingInfo(this);
        this.mCi.unregisterForPhbReady(this);
        this.mCi.unregisterForRadioStateChanged(this);
        this.mCi.unregisterForEfCspPlmnModeBitChanged(this);
        this.mCi.unregisterForMelockChanged(this);
        this.mParentApp.unregisterForReady(this);
        this.mParentApp.unregisterForLocked(this);
        this.mContext.unregisterReceiver(this.mSimReceiver);
        this.mContext.unregisterReceiver(this.mSubReceiver);
        this.mContext.unregisterReceiver(this.mReceiver);
        this.mContext.unregisterReceiver(this.mRTC);
        this.mPhbWaitSub = false;
        resetRecords();
        this.mAdnCache.reset();
        setPhbReady(false);
        this.mIccId = null;
        this.mImsi = null;
        this.mPhone.getIccPhoneBookInterfaceManager().dispose();
        super.dispose();
    }

    protected void finalize() {
        log("finalized");
    }

    protected void resetRecords() {
        this.mImsi = null;
        this.mMsisdn = null;
        this.mVoiceMailNum = null;
        this.mMncLength = -1;
        log("setting0 mMncLength" + this.mMncLength);
        this.mIccId = null;
        this.mFullIccId = null;
        this.mSpnDisplayCondition = -1;
        this.mEfMWIS = null;
        this.mEfCPHS_MWI = null;
        this.mSpdiNetworks = null;
        this.mPnnHomeName = null;
        this.mGid1 = null;
        this.mGid2 = null;
        this.mAdnCache.reset();
        log("SIMRecords: onRadioOffOrNotAvailable set 'gsm.sim.operator.numeric' to operator=null");
        log("update icc_operator_numeric=" + null);
        this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), UsimPBMemInfo.STRING_NOT_SET);
        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), UsimPBMemInfo.STRING_NOT_SET);
        this.mTelephonyManager.setSimCountryIsoForPhone(this.mParentApp.getPhoneId(), UsimPBMemInfo.STRING_NOT_SET);
        setSystemProperty("gsm.sim.operator.default-name", null);
        this.mRecordsRequested = false;
    }

    public String getIMSI() {
        return this.mImsi;
    }

    public String getMsisdnNumber() {
        return this.mMsisdn;
    }

    public String getGid1() {
        return this.mGid1;
    }

    public String getGid2() {
        return this.mGid2;
    }

    public UsimServiceTable getUsimServiceTable() {
        return this.mUsimServiceTable;
    }

    private int getExtFromEf(int ef) {
        switch (ef) {
            case IccConstants.EF_MSISDN /*28480*/:
                if (this.mParentApp.getType() == AppType.APPTYPE_USIM) {
                    return IccConstants.EF_EXT5;
                }
                return IccConstants.EF_EXT1;
            default:
                return IccConstants.EF_EXT1;
        }
    }

    public void setMsisdnNumber(String alphaTag, String number, Message onComplete) {
        this.mNewMsisdn = number;
        this.mNewMsisdnTag = alphaTag;
        log("Set MSISDN: " + this.mNewMsisdnTag + " " + Rlog.pii(LOG_TAG, this.mNewMsisdn));
        new AdnRecordLoader(this.mFh).updateEF(new AdnRecord(this.mNewMsisdnTag, this.mNewMsisdn), IccConstants.EF_MSISDN, getExtFromEf(IccConstants.EF_MSISDN), 1, null, obtainMessage(30, onComplete));
    }

    public String getMsisdnAlphaTag() {
        return this.mMsisdnTag;
    }

    public String getVoiceMailNumber() {
        log("getVoiceMailNumber " + this.mVoiceMailNum);
        return this.mVoiceMailNum;
    }

    public void setVoiceMailNumber(String alphaTag, String voiceNumber, Message onComplete) {
        log("setVoiceMailNumber, mIsVoiceMailFixed " + this.mIsVoiceMailFixed + ", mMailboxIndex " + this.mMailboxIndex + ", mMailboxIndex " + this.mMailboxIndex);
        if (this.mIsVoiceMailFixed) {
            AsyncResult.forMessage(onComplete).exception = new IccVmFixedException("Voicemail number is fixed by operator");
            onComplete.sendToTarget();
            return;
        }
        this.mNewVoiceMailNum = voiceNumber;
        this.mNewVoiceMailTag = alphaTag;
        AdnRecord adn = new AdnRecord(this.mNewVoiceMailTag, this.mNewVoiceMailNum);
        if (this.mMailboxIndex != 0 && this.mMailboxIndex != 255) {
            new AdnRecordLoader(this.mFh).updateEF(adn, IccConstants.EF_MBDN, IccConstants.EF_EXT6, this.mMailboxIndex, null, obtainMessage(20, onComplete));
        } else if (isCphsMailboxEnabled()) {
            log("setVoiceMailNumber,load EF_MAILBOX_CPHS");
            new AdnRecordLoader(this.mFh).updateEF(adn, IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, null, obtainMessage(25, onComplete));
        } else {
            log("setVoiceMailNumber,Update SIM voice mailbox error");
            AsyncResult.forMessage(onComplete).exception = new IccVmNotSupportedException("Update SIM voice mailbox error");
            onComplete.sendToTarget();
        }
    }

    public String getVoiceMailAlphaTag() {
        return this.mVoiceMailTag;
    }

    public void setVoiceMessageWaiting(int line, int countWaiting) {
        int i = 0;
        if (line == 1) {
            try {
                if (this.mEfMWIS != null) {
                    byte[] bArr = this.mEfMWIS;
                    int i2 = this.mEfMWIS[0] & 254;
                    if (countWaiting != 0) {
                        i = 1;
                    }
                    bArr[0] = (byte) (i | i2);
                    if (countWaiting < 0) {
                        this.mEfMWIS[1] = (byte) 0;
                    } else {
                        this.mEfMWIS[1] = (byte) countWaiting;
                    }
                    this.mFh.updateEFLinearFixed(IccConstants.EF_MWIS, 1, this.mEfMWIS, null, obtainMessage(14, IccConstants.EF_MWIS, 0));
                }
                if (this.mParentApp.getType() == AppType.APPTYPE_USIM) {
                    log("[setVoiceMessageWaiting] It is USIM card, skip write CPHS file");
                } else if (this.mEfCPHS_MWI != null) {
                    this.mEfCPHS_MWI[0] = (byte) ((countWaiting == 0 ? 5 : 10) | (this.mEfCPHS_MWI[0] & 240));
                    this.mFh.updateEFTransparent(IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS, this.mEfCPHS_MWI, obtainMessage(14, Integer.valueOf(IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS)));
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                logw("Error saving voice mail state to SIM. Probably malformed SIM record", ex);
            }
        }
    }

    public boolean checkEfCfis() {
        boolean isValid = this.mEfCfis != null && this.mEfCfis.length == 16;
        log("mEfCfis is null? = " + (this.mEfCfis == null));
        return isValid;
    }

    private boolean validEfCfis(byte[] data) {
        return data != null && data[0] >= (byte) 1 && data[0] <= (byte) 4;
    }

    public int getVoiceMessageCount() {
        int countVoiceMessages = 0;
        if (this.mEfMWIS != null) {
            countVoiceMessages = this.mEfMWIS[1] & 255;
            if (((this.mEfMWIS[0] & 1) != 0) && countVoiceMessages == 0) {
                countVoiceMessages = -1;
            }
            log(" VoiceMessageCount from SIM MWIS = " + countVoiceMessages);
        } else if (this.mEfCPHS_MWI != null) {
            int indicator = this.mEfCPHS_MWI[0] & 15;
            if (indicator == 10) {
                countVoiceMessages = -1;
            } else if (indicator == 5) {
                countVoiceMessages = 0;
            }
            log(" VoiceMessageCount from SIM CPHS = " + countVoiceMessages);
        }
        return countVoiceMessages;
    }

    public int getVoiceCallForwardingFlag() {
        return this.mCallForwardingStatus;
    }

    public void setVoiceCallForwardingFlag(int line, boolean enable, String dialNumber) {
        int i = 0;
        Rlog.d(LOG_TAG, "setVoiceCallForwardingFlag: " + enable);
        if (line == 1) {
            if (enable) {
                i = 1;
            }
            this.mCallForwardingStatus = i;
            Rlog.d(LOG_TAG, " mRecordsEventsRegistrants: size=" + this.mRecordsEventsRegistrants.size());
            this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(1));
            try {
                if (checkEfCfis()) {
                    byte[] bArr;
                    if (enable) {
                        bArr = this.mEfCfis;
                        bArr[1] = (byte) (bArr[1] | 1);
                    } else {
                        bArr = this.mEfCfis;
                        bArr[1] = (byte) (bArr[1] & 254);
                    }
                    log("setVoiceCallForwardingFlag: enable=" + enable + " mEfCfis=" + IccUtils.bytesToHexString(this.mEfCfis));
                    if (enable && !TextUtils.isEmpty(dialNumber)) {
                        logv("EF_CFIS: updating cf number, " + Rlog.pii(LOG_TAG, dialNumber));
                        byte[] bcdNumber = PhoneNumberUtils.numberToCalledPartyBCD(convertNumberIfContainsPrefix(dialNumber));
                        System.arraycopy(bcdNumber, 0, this.mEfCfis, 3, bcdNumber.length);
                        this.mEfCfis[2] = (byte) bcdNumber.length;
                        this.mEfCfis[14] = (byte) -1;
                        this.mEfCfis[15] = (byte) -1;
                    }
                    if (this.mFh != null) {
                        this.mFh.updateEFLinearFixed(IccConstants.EF_CFIS, 1, this.mEfCfis, null, obtainMessage(14, Integer.valueOf(IccConstants.EF_CFIS)));
                    } else {
                        log("setVoiceCallForwardingFlag: mFh is null, skip update EF_CFIS");
                    }
                } else {
                    log("setVoiceCallForwardingFlag: ignoring enable=" + enable + " invalid mEfCfis=" + IccUtils.bytesToHexString(this.mEfCfis));
                }
                if (this.mEfCff != null) {
                    if (enable) {
                        this.mEfCff[0] = (byte) ((this.mEfCff[0] & 240) | 10);
                    } else {
                        this.mEfCff[0] = (byte) ((this.mEfCff[0] & 240) | 5);
                    }
                    if (this.mFh != null) {
                        this.mFh.updateEFTransparent(IccConstants.EF_CFF_CPHS, this.mEfCff, obtainMessage(14, Integer.valueOf(IccConstants.EF_CFF_CPHS)));
                    } else {
                        log("setVoiceCallForwardingFlag: mFh is null, skip update EF_CFF_CPHS");
                    }
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                logw("Error saving call forwarding flag to SIM. Probably malformed SIM record", ex);
            }
        }
    }

    public void onRefresh(boolean fileChanged, int[] fileList) {
        if (fileChanged) {
            fetchSimRecords();
        }
    }

    public String getOperatorNumeric() {
        if (this.mImsi == null) {
            if (getRecordsLoaded()) {
                log("getOperatorNumeric: IMSI == null when record loaded.");
            }
            return null;
        } else if (this.mMncLength != -1 && this.mMncLength != 0) {
            return this.mImsi.substring(0, this.mMncLength + 3);
        } else {
            log("getSIMOperatorNumeric: bad mncLength");
            return null;
        }
    }

    public String getSIMCPHSOns() {
        if (this.cphsOnsl != null) {
            return this.cphsOnsl;
        }
        return this.cphsOnss;
    }

    public void handleMessage(Message msg) {
        boolean isRecordLoadResponse = false;
        if (this.mDestroyed.get()) {
            loge("Received message " + msg + "[" + msg.what + "] " + " while being destroyed. Ignoring.");
            return;
        }
        String mccmncCode;
        try {
            AsyncResult ar;
            String[] strArr;
            int i;
            int length;
            byte[] data;
            AdnRecord adn;
            int i2;
            String eccNum;
            switch (msg.what) {
                case 1:
                    onReady();
                    fetchEccList();
                    break;
                case 3:
                    isRecordLoadResponse = true;
                    ar = msg.obj;
                    if (ar.exception == null) {
                        this.mImsi = (String) ar.result;
                        if (this.mImsi != null && (this.mImsi.length() < 6 || this.mImsi.length() > 15)) {
                            loge("invalid IMSI " + this.mImsi);
                            this.mImsi = null;
                        }
                        log("IMSI: mMncLength=" + this.mMncLength);
                        log("IMSI: " + this.mImsi.substring(0, 6) + Rlog.pii(LOG_TAG, this.mImsi.substring(6)));
                        this.mIsTestCard = OemConstant.isTestCard(this.mImsi);
                        log("leon mIsTestCard: " + this.mIsTestCard);
                        if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                            mccmncCode = this.mImsi.substring(0, 6);
                            strArr = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                            i = 0;
                            length = strArr.length;
                            while (i < length) {
                                if (strArr[i].equals(mccmncCode)) {
                                    this.mMncLength = 3;
                                    log("IMSI: setting1 mMncLength=" + this.mMncLength);
                                } else {
                                    i++;
                                }
                            }
                        }
                        if (this.mMncLength == 0 || this.mMncLength == -1) {
                            try {
                                this.mMncLength = MccTable.smallestDigitsMccForMnc(Integer.parseInt(this.mImsi.substring(0, 3)));
                                log("setting2 mMncLength=" + this.mMncLength);
                            } catch (NumberFormatException e) {
                                this.mMncLength = 0;
                                loge("Corrupt IMSI! setting3 mMncLength=" + this.mMncLength);
                            }
                        }
                        if (!(this.mMncLength == 0 || this.mMncLength == -1)) {
                            log("update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
                            updateConfiguration(this.mImsi.substring(0, this.mMncLength + 3));
                        }
                        this.mImsiReadyRegistrants.notifyRegistrants();
                        String operatorFromIMSI = getOperatorNumeric();
                        if (!TextUtils.isEmpty(operatorFromIMSI)) {
                            log("IMSI: set 'gsm.sim.operator.numeric' to operator='" + operatorFromIMSI + "'");
                            this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), operatorFromIMSI);
                            break;
                        }
                    }
                    loge("Exception querying IMSI, Exception:" + ar.exception);
                    break;
                    break;
                case 4:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        this.mIccId = IccUtils.bcdToString(data, 0, data.length);
                        this.mFullIccId = IccUtils.bchToString(data, 0, data.length);
                        log("iccid: " + SubscriptionInfo.givePrintableIccid(this.mFullIccId));
                        break;
                    }
                    break;
                case 5:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = ar.result;
                    boolean isValidMbdn = false;
                    if (ar.exception == null) {
                        log("EF_MBI: " + IccUtils.bytesToHexString(data));
                        this.mMailboxIndex = data[0] & 255;
                        if (!(this.mMailboxIndex == 0 || this.mMailboxIndex == 255)) {
                            log("Got valid mailbox number for MBDN");
                            isValidMbdn = true;
                            this.isValidMBI = true;
                        }
                    }
                    this.mRecordsToLoad++;
                    if (!isValidMbdn) {
                        if (!isCphsMailboxEnabled()) {
                            log("EVENT_GET_MBI_DONE, do nothing");
                            this.mRecordsToLoad--;
                            break;
                        }
                        log("EVENT_GET_MBI_DONE, to load EF_MAILBOX_CPHS");
                        new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                        break;
                    }
                    log("EVENT_GET_MBI_DONE, to load EF_MBDN");
                    new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MBDN, IccConstants.EF_EXT6, this.mMailboxIndex, obtainMessage(6));
                    break;
                case 6:
                case 11:
                    this.mVoiceMailNum = null;
                    this.mVoiceMailTag = null;
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        adn = ar.result;
                        log("VM: " + adn + (msg.what == 11 ? " EF[MAILBOX]" : " EF[MBDN]"));
                        if (!adn.isEmpty() || msg.what != 6) {
                            this.mVoiceMailNum = adn.getNumber();
                            this.mVoiceMailTag = adn.getAlphaTag();
                            break;
                        }
                        this.mRecordsToLoad++;
                        new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                        break;
                    }
                    loge("Invalid or missing EF" + (msg.what == 11 ? "[MAILBOX]" : "[MBDN]"));
                    if (msg.what == 6) {
                        this.mRecordsToLoad++;
                        new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                        break;
                    }
                    break;
                case 7:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    log("EF_MWIS : " + IccUtils.bytesToHexString(data));
                    if (ar.exception == null) {
                        if ((data[0] & 255) != 255) {
                            this.mEfMWIS = data;
                            break;
                        } else {
                            log("SIMRecords: Uninitialized record MWIS");
                            break;
                        }
                    }
                    loge("EVENT_GET_MWIS_DONE exception = " + ar.exception);
                    break;
                case 8:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    log("EF_CPHS_MWI: " + IccUtils.bytesToHexString(data));
                    if (ar.exception == null) {
                        this.mEfCPHS_MWI = data;
                        break;
                    } else {
                        loge("EVENT_GET_VOICE_MAIL_INDICATOR_CPHS_DONE exception = " + ar.exception);
                        break;
                    }
                case 9:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        log("EF_AD: " + IccUtils.bytesToHexString(data));
                        if (data.length >= 3) {
                            if ((data[0] & 1) == 1 && (data[2] & 1) == 1) {
                                log("SIMRecords: Cipher is enable");
                            }
                            if (data.length != 3) {
                                this.mMncLength = data[3] & 15;
                                log("setting4 mMncLength=" + this.mMncLength);
                                if (this.mMncLength == 15) {
                                    this.mMncLength = 0;
                                    log("setting5 mMncLength=" + this.mMncLength);
                                } else if (!(this.mMncLength == 2 || this.mMncLength == 3)) {
                                    this.mMncLength = -1;
                                    log("setting5 mMncLength=" + this.mMncLength);
                                }
                                this.mIsTestCard = !this.mIsTestCard ? OemConstant.isTestCard(this.mPhone.getContext(), data[0]) : true;
                                log("leon mIsTestCard 2: " + this.mIsTestCard);
                                if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                                    mccmncCode = this.mImsi.substring(0, 6);
                                    log("mccmncCode=" + mccmncCode);
                                    strArr = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                                    i = 0;
                                    length = strArr.length;
                                    while (i < length) {
                                        if (strArr[i].equals(mccmncCode)) {
                                            this.mMncLength = 3;
                                            log("setting6 mMncLength=" + this.mMncLength);
                                        } else {
                                            i++;
                                        }
                                    }
                                }
                                if (this.mMncLength == 0 || this.mMncLength == -1) {
                                    if (this.mImsi != null) {
                                        try {
                                            this.mMncLength = MccTable.smallestDigitsMccForMnc(Integer.parseInt(this.mImsi.substring(0, 3)));
                                            log("setting7 mMncLength=" + this.mMncLength);
                                        } catch (NumberFormatException e2) {
                                            this.mMncLength = 0;
                                            loge("Corrupt IMSI! setting8 mMncLength=" + this.mMncLength);
                                        }
                                    } else {
                                        this.mMncLength = 0;
                                        log("MNC length not present in EF_AD setting9 mMncLength=" + this.mMncLength);
                                    }
                                }
                                if (!(this.mImsi == null || this.mMncLength == 0)) {
                                    log("update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
                                    updateConfiguration(this.mImsi.substring(0, this.mMncLength + 3));
                                    break;
                                }
                            }
                            log("MNC length not present in EF_AD");
                            if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                                mccmncCode = this.mImsi.substring(0, 6);
                                log("mccmncCode=" + mccmncCode);
                                strArr = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                                i = 0;
                                length = strArr.length;
                                while (i < length) {
                                    if (strArr[i].equals(mccmncCode)) {
                                        this.mMncLength = 3;
                                        log("setting6 mMncLength=" + this.mMncLength);
                                    } else {
                                        i++;
                                    }
                                }
                            }
                            if (this.mMncLength == 0 || this.mMncLength == -1) {
                                if (this.mImsi != null) {
                                    try {
                                        this.mMncLength = MccTable.smallestDigitsMccForMnc(Integer.parseInt(this.mImsi.substring(0, 3)));
                                        log("setting7 mMncLength=" + this.mMncLength);
                                    } catch (NumberFormatException e3) {
                                        this.mMncLength = 0;
                                        loge("Corrupt IMSI! setting8 mMncLength=" + this.mMncLength);
                                    }
                                } else {
                                    this.mMncLength = 0;
                                    log("MNC length not present in EF_AD setting9 mMncLength=" + this.mMncLength);
                                }
                            }
                            if (!(this.mImsi == null || this.mMncLength == 0)) {
                                log("update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
                                updateConfiguration(this.mImsi.substring(0, this.mMncLength + 3));
                                break;
                            }
                        }
                        log("Corrupt AD data on SIM");
                        if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                            mccmncCode = this.mImsi.substring(0, 6);
                            log("mccmncCode=" + mccmncCode);
                            strArr = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                            i = 0;
                            length = strArr.length;
                            while (i < length) {
                                if (strArr[i].equals(mccmncCode)) {
                                    this.mMncLength = 3;
                                    log("setting6 mMncLength=" + this.mMncLength);
                                } else {
                                    i++;
                                }
                            }
                        }
                        if (this.mMncLength == 0 || this.mMncLength == -1) {
                            if (this.mImsi != null) {
                                try {
                                    this.mMncLength = MccTable.smallestDigitsMccForMnc(Integer.parseInt(this.mImsi.substring(0, 3)));
                                    log("setting7 mMncLength=" + this.mMncLength);
                                } catch (NumberFormatException e4) {
                                    this.mMncLength = 0;
                                    loge("Corrupt IMSI! setting8 mMncLength=" + this.mMncLength);
                                }
                            } else {
                                this.mMncLength = 0;
                                log("MNC length not present in EF_AD setting9 mMncLength=" + this.mMncLength);
                            }
                        }
                        if (!(this.mImsi == null || this.mMncLength == 0)) {
                            log("update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
                            updateConfiguration(this.mImsi.substring(0, this.mMncLength + 3));
                            break;
                        }
                    }
                    if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                        mccmncCode = this.mImsi.substring(0, 6);
                        log("mccmncCode=" + mccmncCode);
                        strArr = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                        i = 0;
                        length = strArr.length;
                        while (i < length) {
                            if (strArr[i].equals(mccmncCode)) {
                                this.mMncLength = 3;
                                log("setting6 mMncLength=" + this.mMncLength);
                            } else {
                                i++;
                            }
                        }
                    }
                    if (this.mMncLength == 0 || this.mMncLength == -1) {
                        if (this.mImsi != null) {
                            try {
                                this.mMncLength = MccTable.smallestDigitsMccForMnc(Integer.parseInt(this.mImsi.substring(0, 3)));
                                log("setting7 mMncLength=" + this.mMncLength);
                            } catch (NumberFormatException e5) {
                                this.mMncLength = 0;
                                loge("Corrupt IMSI! setting8 mMncLength=" + this.mMncLength);
                            }
                        } else {
                            this.mMncLength = 0;
                            log("MNC length not present in EF_AD setting9 mMncLength=" + this.mMncLength);
                        }
                    }
                    if (!(this.mImsi == null || this.mMncLength == 0)) {
                        log("update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
                        updateConfiguration(this.mImsi.substring(0, this.mMncLength + 3));
                        break;
                    }
                case 10:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        adn = (AdnRecord) ar.result;
                        this.mMsisdn = adn.getNumber();
                        this.mMsisdnTag = adn.getAlphaTag();
                        this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(100));
                        log("MSISDN: " + Rlog.pii(LOG_TAG, this.mMsisdn));
                        break;
                    }
                    loge("Invalid or missing EF[MSISDN]");
                    break;
                case 12:
                    log("EF_SPN loaded and try to extract: ");
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    if (ar != null && ar.exception == null) {
                        log("getSpnFsm, Got data from EF_SPN");
                        data = (byte[]) ar.result;
                        this.mSpnDisplayCondition = data[0] & 255;
                        if (this.mSpnDisplayCondition == 255) {
                            this.mSpnDisplayCondition = -1;
                        }
                        setServiceProviderName(IccUtils.adnStringFieldToString(data, 1, data.length - 1));
                        this.mSpNameInEfSpn = getServiceProviderName();
                        if (this.mSpNameInEfSpn != null && this.mSpNameInEfSpn.equals(UsimPBMemInfo.STRING_NOT_SET)) {
                            log("set spNameInEfSpn to null because parsing result is empty");
                            this.mSpNameInEfSpn = null;
                        }
                        log("Load EF_SPN: " + getServiceProviderName() + " spnDisplayCondition: " + this.mSpnDisplayCondition);
                        if (!OemConstant.EXP_VERSION) {
                            if (this.mMncLength != 0 && this.mMncLength != -1 && SubscriptionManager.isUsimWithCsim(this.mSlotId) && OemConstant.isCtCard(this.mPhone)) {
                                String spn = getServiceProviderName();
                                String operName = SpnOverride.getInstance().getSpnByEfSpn("20404", spn);
                                log("SPN loaded, spn=" + spn + "   operName = " + operName);
                                if (spn != null) {
                                    if (!spn.equals(UsimPBMemInfo.STRING_NOT_SET) && spn.equals(operName)) {
                                        updateConfiguration("46011");
                                        log("SPN loaded, update 46011 to set language");
                                    }
                                }
                                updateConfiguration(this.mImsi.substring(0, this.mMncLength + 3));
                                log("SPN loaded, update mccmnc =" + this.mImsi.substring(0, this.mMncLength + 3));
                            }
                            setSpnFromConfig(getOperatorNumeric());
                            break;
                        }
                        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), getServiceProviderName());
                        break;
                    }
                    loge(": read spn fail!");
                    this.mSpnDisplayCondition = -1;
                    break;
                    break;
                case 13:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        parseEfSpdi(data);
                        break;
                    }
                    break;
                case 14:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        logw("update failed. ", ar.exception);
                        break;
                    }
                    break;
                case 15:
                    isRecordLoadResponse = false;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        parseEFpnn((ArrayList) ar.result);
                        if (!this.mReadingOpl) {
                            this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(3));
                            break;
                        }
                    }
                    break;
                case 17:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        this.mUsimServiceTable = new UsimServiceTable(data);
                        log("SST: " + this.mUsimServiceTable);
                        this.mEfSST = data;
                        if (this.mParentApp != null && this.mParentApp.getState() == AppState.APPSTATE_READY) {
                            this.mParentApp.queryFdn();
                            break;
                        }
                    }
                    break;
                case 18:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        handleSmses((ArrayList) ar.result);
                        break;
                    }
                    break;
                case 19:
                    Rlog.i("ENF", "marked read: sms " + msg.arg1);
                    break;
                case 20:
                    isRecordLoadResponse = false;
                    ar = (AsyncResult) msg.obj;
                    log("EVENT_SET_MBDN_DONE ex:" + ar.exception);
                    if (ar.exception == null) {
                        this.mVoiceMailNum = this.mNewVoiceMailNum;
                        this.mVoiceMailTag = this.mNewVoiceMailTag;
                    }
                    if (!isCphsMailboxEnabled()) {
                        if (ar.userObj != null) {
                            Resources resource = Resources.getSystem();
                            if (ar.exception == null || !resource.getBoolean(17957016)) {
                                AsyncResult.forMessage((Message) ar.userObj).exception = ar.exception;
                            } else {
                                AsyncResult.forMessage((Message) ar.userObj).exception = new IccVmNotSupportedException("Update SIM voice mailbox error");
                            }
                            ((Message) ar.userObj).sendToTarget();
                            break;
                        }
                    }
                    adn = new AdnRecord(this.mVoiceMailTag, this.mVoiceMailNum);
                    Message onCphsCompleted = ar.userObj;
                    if (ar.exception == null && ar.userObj != null) {
                        AsyncResult.forMessage((Message) ar.userObj).exception = null;
                        ((Message) ar.userObj).sendToTarget();
                        log("Callback with MBDN successful.");
                        onCphsCompleted = null;
                    }
                    new AdnRecordLoader(this.mFh).updateEF(adn, IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, null, obtainMessage(25, onCphsCompleted));
                    break;
                    break;
                case 21:
                    isRecordLoadResponse = false;
                    ar = (AsyncResult) msg.obj;
                    int[] index = (int[]) ar.result;
                    if (ar.exception != null || index.length != 1) {
                        loge("Error on SMS_ON_SIM with exp " + ar.exception + " length " + index.length);
                        break;
                    }
                    log("READ EF_SMS RECORD index=" + index[0]);
                    this.mFh.loadEFLinearFixed(IccConstants.EF_SMS, index[0], obtainMessage(22));
                    break;
                    break;
                case 22:
                    isRecordLoadResponse = false;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        loge("Error on GET_SMS with exp " + ar.exception);
                        break;
                    } else {
                        handleSms((byte[]) ar.result);
                        break;
                    }
                case 24:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        log("EF_CFF_CPHS: " + IccUtils.bytesToHexString(data));
                        this.mEfCff = data;
                        break;
                    }
                    this.mEfCff = null;
                    break;
                case 25:
                    isRecordLoadResponse = false;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        this.mVoiceMailNum = this.mNewVoiceMailNum;
                        this.mVoiceMailTag = this.mNewVoiceMailTag;
                    } else {
                        loge("Set CPHS MailBox with exception: " + ar.exception);
                    }
                    if (ar.userObj != null) {
                        log("Callback with CPHS MB successful.");
                        AsyncResult.forMessage((Message) ar.userObj).exception = ar.exception;
                        ((Message) ar.userObj).sendToTarget();
                        break;
                    }
                    break;
                case 26:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        this.mCphsInfo = (byte[]) ar.result;
                        log("iCPHS: " + IccUtils.bytesToHexString(this.mCphsInfo));
                        if (!this.isValidMBI && isCphsMailboxEnabled()) {
                            this.mRecordsToLoad++;
                            new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                            break;
                        }
                    }
                    break;
                case 30:
                    isRecordLoadResponse = false;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        this.mMsisdn = this.mNewMsisdn;
                        this.mMsisdnTag = this.mNewMsisdnTag;
                        this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(100));
                        log("Success to update EF[MSISDN]");
                    }
                    if (ar.userObj != null) {
                        AsyncResult.forMessage((Message) ar.userObj).exception = ar.exception;
                        ((Message) ar.userObj).sendToTarget();
                        break;
                    }
                    break;
                case 31:
                    isRecordLoadResponse = false;
                    ar = (AsyncResult) msg.obj;
                    log("Sim REFRESH with exception: " + ar.exception);
                    if (ar.exception == null) {
                        handleSimRefresh((IccRefreshResponse) ar.result);
                        break;
                    }
                    break;
                case 32:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        log("EF_CFIS: " + IccUtils.bytesToHexString(data));
                        this.mEfCfis = data;
                        break;
                    }
                    this.mEfCfis = null;
                    break;
                case 33:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        data = (byte[]) ar.result;
                        log("EF_CSP: " + IccUtils.bytesToHexString(data));
                        handleEfCspData(data);
                        break;
                    }
                    loge("Exception in fetching EF_CSP data " + ar.exception);
                    break;
                case 34:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        this.mGid1 = IccUtils.bytesToHexString(data);
                        log("GID1: " + this.mGid1);
                        break;
                    }
                    loge("Exception in get GID1 " + ar.exception);
                    this.mGid1 = null;
                    break;
                case 35:
                    onLocked();
                    fetchEccList();
                    break;
                case 36:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        this.mGid2 = IccUtils.bytesToHexString(data);
                        log("GID2: " + this.mGid2);
                        break;
                    }
                    loge("Exception in get GID2 " + ar.exception);
                    this.mGid2 = null;
                    break;
                case 37:
                    handleCarrierNameOverride();
                    break;
                case 41:
                    if (this.mTelephonyExt.isSetLanguageBySIM()) {
                        fetchLanguageIndicator();
                    }
                    this.mMsisdn = UsimPBMemInfo.STRING_NOT_SET;
                    this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(100));
                    break;
                case 42:
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        log("EF_LI: " + IccUtils.bytesToHexString(data));
                        this.mEfLi = data;
                    }
                    onLanguageFileLoaded();
                    break;
                case 43:
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        log("EF_ELP: " + IccUtils.bytesToHexString(data));
                        this.mEfELP = data;
                    }
                    onLanguageFileLoaded();
                    break;
                case 53:
                    log("[sume receive response message");
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    if (ar != null && ar.exception == null) {
                        data = (byte[]) ar.result;
                        if (data != null && data.length >= 2) {
                            int len = data[1] & 255;
                            log("[sume tag = " + (data[0] & 255) + ", len = " + len);
                            this.mMenuTitleFromEf = IccUtils.adnStringFieldToString(data, 2, len);
                            log("[sume menu title is " + this.mMenuTitleFromEf);
                            break;
                        }
                    }
                    if (ar.exception != null) {
                        loge("[sume exception in AsyncResult: " + ar.exception.getClass().getName());
                    } else {
                        log("[sume null AsyncResult");
                    }
                    this.mMenuTitleFromEf = null;
                    break;
                    break;
                case 102:
                    log("handleMessage (EVENT_GET_SIM_ECC_DONE)");
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        this.mEfEcc = UsimPBMemInfo.STRING_NOT_SET;
                        data = (byte[]) ar.result;
                        for (i2 = 0; i2 + 2 < data.length; i2 += 3) {
                            eccNum = IccUtils.bcdToString(data, i2, 3);
                            if (eccNum != null) {
                                if (!(eccNum.equals(UsimPBMemInfo.STRING_NOT_SET) || this.mEfEcc.equals(UsimPBMemInfo.STRING_NOT_SET))) {
                                    this.mEfEcc += ";";
                                }
                            }
                            this.mEfEcc += eccNum + ",0";
                        }
                        this.mEfEcc += ";112,0;911,0";
                        log("SIM mEfEcc is " + this.mEfEcc);
                        SystemProperties.set(this.SIM_RECORDS_PROPERTY_ECC_LIST[this.mSlotId], this.mEfEcc);
                        break;
                    }
                    loge("Get SIM ecc with exception: " + ar.exception);
                    break;
                case 103:
                    log("handleMessage (EVENT_GET_USIM_ECC_DONE)");
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        ArrayList eccRecords = ar.result;
                        int count = eccRecords.size();
                        this.mEfEcc = UsimPBMemInfo.STRING_NOT_SET;
                        for (i2 = 0; i2 < count; i2++) {
                            data = (byte[]) eccRecords.get(i2);
                            log("USIM EF_ECC record " + i2 + ": " + IccUtils.bytesToHexString(data));
                            eccNum = IccUtils.bcdToString(data, 0, 3);
                            if (eccNum != null) {
                                if (!eccNum.equals(UsimPBMemInfo.STRING_NOT_SET)) {
                                    if (!this.mEfEcc.equals(UsimPBMemInfo.STRING_NOT_SET)) {
                                        this.mEfEcc += ";";
                                    }
                                    this.mEfEcc += eccNum;
                                    this.mEfEcc += "," + String.valueOf(data[data.length - 1] & 255);
                                }
                            }
                        }
                        this.mEfEcc += ";112,0;911,0";
                        log("USIM mEfEcc is " + this.mEfEcc);
                        SystemProperties.set(this.SIM_RECORDS_PROPERTY_ECC_LIST[this.mSlotId], this.mEfEcc);
                        break;
                    }
                    loge("Get USIM ecc with exception: " + ar.exception);
                    break;
                case 104:
                    isRecordLoadResponse = false;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        parseEFopl((ArrayList) ar.result);
                        this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(3));
                        break;
                    }
                    break;
                case 105:
                    log("handleMessage (EVENT_GET_CPHSONS_DONE)");
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    if (ar != null && ar.exception == null) {
                        data = (byte[]) ar.result;
                        this.cphsOnsl = IccUtils.adnStringFieldToString(data, 0, data.length);
                        log("Load EF_SPN_CPHS: " + this.cphsOnsl);
                        break;
                    }
                case 106:
                    log("handleMessage (EVENT_GET_SHORT_CPHSONS_DONE)");
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    if (ar != null && ar.exception == null) {
                        data = (byte[]) ar.result;
                        this.cphsOnss = IccUtils.adnStringFieldToString(data, 0, data.length);
                        log("Load EF_SPN_SHORT_CPHS: " + this.cphsOnss);
                        break;
                    }
                case 200:
                    this.mPhbReady = isPhbReady();
                    log("[EVENT_DELAYED_SEND_PHB_CHANGE] isReady : " + this.mPhbReady);
                    broadcastPhbStateChangedIntent(this.mPhbReady);
                    break;
                case EVENT_EF_CSP_PLMN_MODE_BIT_CHANGED /*203*/:
                    ar = (AsyncResult) msg.obj;
                    if (ar != null && ar.exception == null) {
                        processEfCspPlmnModeBitUrc(((int[]) ar.result)[0]);
                        break;
                    }
                case EVENT_GET_RAT_DONE /*204*/:
                    log("handleMessage (EVENT_GET_RAT_DONE)");
                    ar = (AsyncResult) msg.obj;
                    this.mEfRatLoaded = true;
                    if (ar != null && ar.exception == null) {
                        this.mEfRat = (byte[]) ar.result;
                        log("load EF_RAT complete: " + this.mEfRat[0]);
                        boradcastEfRatContentNotify(512);
                        break;
                    }
                    log("load EF_RAT fail");
                    this.mEfRat = null;
                    if (this.mParentApp.getType() != AppType.APPTYPE_USIM) {
                        boradcastEfRatContentNotify(512);
                        break;
                    } else {
                        boradcastEfRatContentNotify(256);
                        break;
                    }
                    break;
                case EVENT_GET_PSISMSC_DONE /*207*/:
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        log("EF_PSISMSC: " + IccUtils.bytesToHexString(data));
                        if (data != null) {
                            this.mEfPsismsc = data;
                            break;
                        }
                    }
                    break;
                case 208:
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        log("EF_SMSP: " + IccUtils.bytesToHexString(data));
                        if (data != null) {
                            this.mEfSmsp = data;
                            break;
                        }
                    }
                    break;
                case EVENT_GET_GBABP_DONE /*209*/:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        loge("Error on GET_GBABP with exp " + ar.exception);
                        break;
                    }
                    this.mGbabp = IccUtils.bytesToHexString((byte[]) ar.result);
                    log("EF_GBABP=" + this.mGbabp);
                    break;
                case EVENT_GET_GBANL_DONE /*210*/:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        loge("Error on GET_GBANL with exp " + ar.exception);
                        break;
                    }
                    this.mEfGbanlList = (ArrayList) ar.result;
                    log("GET_GBANL record count: " + this.mEfGbanlList.size());
                    break;
                case 211:
                    ar = (AsyncResult) msg.obj;
                    if (!(ar == null || ar.exception != null || ar.result == null)) {
                        log("handle EVENT_CFU_IND, setVoiceCallForwardingFlag:" + ar.result[0]);
                        break;
                    }
                case 212:
                    log("handleMessage (EVENT_IMSI_REFRESH_QUERY) mImsi= " + this.mImsi);
                    this.mCi.getIMSIForApp(this.mParentApp.getAid(), obtainMessage(213));
                    break;
                case 213:
                    log("handleMessage (EVENT_IMSI_REFRESH_QUERY_DONE)");
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        this.mImsi = (String) ar.result;
                        if (this.mImsi != null && (this.mImsi.length() < 6 || this.mImsi.length() > 15)) {
                            loge("invalid IMSI " + this.mImsi);
                            this.mImsi = null;
                        }
                        log("IMSI: mMncLength=" + this.mMncLength);
                        if (this.mImsi != null) {
                            log("IMSI: " + this.mImsi.substring(0, 6) + "xxxxxxx");
                        }
                        if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                            String mccmncRefresh = this.mImsi.substring(0, 6);
                            strArr = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                            i = 0;
                            length = strArr.length;
                            while (i < length) {
                                if (strArr[i].equals(mccmncRefresh)) {
                                    this.mMncLength = 3;
                                    log("IMSI: setting1 mMncLength=" + this.mMncLength);
                                } else {
                                    i++;
                                }
                            }
                        }
                        if (this.mMncLength == 0 || this.mMncLength == -1) {
                            try {
                                this.mMncLength = MccTable.smallestDigitsMccForMnc(Integer.parseInt(this.mImsi.substring(0, 3)));
                                log("setting2 mMncLength=" + this.mMncLength);
                            } catch (NumberFormatException e6) {
                                this.mMncLength = 0;
                                loge("Corrupt IMSI! setting3 mMncLength=" + this.mMncLength);
                            }
                        }
                        if (!(this.mMncLength == 0 || this.mMncLength == -1)) {
                            log("update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
                            updateConfiguration(this.mImsi.substring(0, this.mMncLength + 3));
                        }
                        if (!this.mImsi.equals(this.mSimImsi)) {
                            this.mSimImsi = this.mImsi;
                            this.mImsiReadyRegistrants.notifyRegistrants();
                            log("SimRecords: mImsiReadyRegistrants.notifyRegistrants");
                        }
                        if (this.mRecordsToLoad == 0 && this.mRecordsRequested) {
                            onAllRecordsLoaded();
                            break;
                        }
                    }
                    loge("Exception querying IMSI, Exception:" + ar.exception);
                    break;
                case EVENT_MELOCK_CHANGED /*400*/:
                    log("handleMessage (EVENT_MELOCK_CHANGED)");
                    ar = (AsyncResult) msg.obj;
                    if (!(ar == null || ar.exception != null || ar.result == null)) {
                        int[] simMelockEvent = (int[]) ar.result;
                        log("sim melock event = " + simMelockEvent[0]);
                        RebootClickListener rebootClickListener = new RebootClickListener(this, null);
                        if (simMelockEvent[0] == 0) {
                            AlertDialog alertDialog = new Builder(this.mContext).setTitle("Unlock Phone").setMessage("Please restart the phone now since unlock setting has changed.").setPositiveButton("OK", rebootClickListener).create();
                            alertDialog.setCancelable(false);
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.getWindow().setType(2003);
                            alertDialog.show();
                            break;
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
            if (isRecordLoadResponse) {
                onRecordLoaded();
            }
        } catch (Throwable exc) {
            logw("Exception parsing SIM record", exc);
            if (isRecordLoadResponse) {
                onRecordLoaded();
            }
        } catch (Throwable th) {
            if (isRecordLoadResponse) {
                onRecordLoaded();
            }
        }
    }

    private void handleFileUpdate(int efid) {
        switch (efid) {
            case IccConstants.EF_PBR /*20272*/:
            case 28474:
            case IccConstants.EF_SDN /*28489*/:
                break;
            case IccConstants.EF_CFF_CPHS /*28435*/:
                this.mRecordsToLoad++;
                log("SIM Refresh called for EF_CFF_CPHS");
                this.mFh.loadEFTransparent(IccConstants.EF_CFF_CPHS, obtainMessage(24));
                return;
            case IccConstants.EF_CSP_CPHS /*28437*/:
                this.mRecordsToLoad++;
                log("[CSP] SIM Refresh for EF_CSP_CPHS");
                this.mFh.loadEFTransparent(IccConstants.EF_CSP_CPHS, obtainMessage(33));
                return;
            case IccConstants.EF_MAILBOX_CPHS /*28439*/:
                this.mRecordsToLoad++;
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                return;
            case IccConstants.EF_FDN /*28475*/:
                log("SIM Refresh called for EF_FDN");
                this.mParentApp.queryFdn();
                break;
            case IccConstants.EF_MSISDN /*28480*/:
                this.mRecordsToLoad++;
                log("SIM Refresh called for EF_MSISDN");
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MSISDN, getExtFromEf(IccConstants.EF_MSISDN), 1, obtainMessage(10));
                return;
            case IccConstants.EF_MBDN /*28615*/:
                this.mRecordsToLoad++;
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MBDN, IccConstants.EF_EXT6, this.mMailboxIndex, obtainMessage(6));
                return;
            case IccConstants.EF_CFIS /*28619*/:
                this.mRecordsToLoad++;
                log("SIM Refresh called for EF_CFIS");
                this.mFh.loadEFLinearFixed(IccConstants.EF_CFIS, 1, obtainMessage(32));
                return;
            default:
                log("handleFileUpdate default");
                if (this.mAdnCache.isUsimPhbEfAndNeedReset(efid) && !this.mIsPhbEfResetDone) {
                    this.mIsPhbEfResetDone = true;
                    this.mAdnCache.reset();
                    setPhbReady(false);
                }
                fetchSimRecords();
                return;
        }
        if (!this.mIsPhbEfResetDone) {
            this.mIsPhbEfResetDone = true;
            this.mAdnCache.reset();
            log("handleFileUpdate ADN like");
            setPhbReady(false);
        }
    }

    private void handleSimRefresh(IccRefreshResponse refreshResponse) {
        if (refreshResponse == null) {
            log("handleSimRefresh received without input");
        } else if (refreshResponse.aid == null || TextUtils.isEmpty(refreshResponse.aid) || refreshResponse.aid.equals(this.mParentApp.getAid())) {
            switch (refreshResponse.refreshResult) {
                case 0:
                    log("handleSimRefresh with SIM_REFRESH_FILE_UPDATED");
                    handleFileUpdate(refreshResponse.efId);
                    this.mIsPhbEfResetDone = false;
                    break;
                case 1:
                    log("handleSimRefresh with SIM_REFRESH_INIT");
                    setPhbReady(false);
                    onIccRefreshInit();
                    break;
                case 2:
                    log("handleSimRefresh with SIM_REFRESH_RESET");
                    log("mSimVar : " + TelephonyManager.getDefault().getMultiSimConfiguration());
                    if (SystemProperties.get("ro.sim_refresh_reset_by_modem").equals("1")) {
                        log("Sim reset by modem!");
                    } else {
                        log("sim_refresh_reset_by_modem false");
                        this.mCi.resetRadio(null);
                    }
                    setPhbReady(false);
                    onIccRefreshInit();
                    break;
                case 4:
                    log("handleSimRefresh with REFRESH_INIT_FULL_FILE_UPDATED");
                    setPhbReady(false);
                    onIccRefreshInit();
                    break;
                case 5:
                    log("handleSimRefresh with REFRESH_INIT_FILE_UPDATED, EFID = " + refreshResponse.efId);
                    handleFileUpdate(refreshResponse.efId);
                    this.mIsPhbEfResetDone = false;
                    if (this.mParentApp.getState() == AppState.APPSTATE_READY) {
                        sendMessage(obtainMessage(1));
                        break;
                    }
                    break;
                case 6:
                    log("handleSimRefresh with REFRESH_SESSION_RESET");
                    onIccRefreshInit();
                    break;
                default:
                    log("handleSimRefresh with unknown operation");
                    break;
            }
            if (refreshResponse.refreshResult == 1 || refreshResponse.refreshResult == 2 || refreshResponse.refreshResult == 4 || refreshResponse.refreshResult == 5 || refreshResponse.refreshResult == 3) {
                log("notify stk app to remove the idle text");
                Intent intent = new Intent(WapPush.ACTION_REMOVE_IDLE_TEXT);
                intent.putExtra(KEY_SIM_ID, this.mSlotId);
                this.mContext.sendBroadcast(intent);
            }
        } else {
            log("handleSimRefresh, refreshResponse.aid = " + refreshResponse.aid + ", mParentApp.getAid() = " + this.mParentApp.getAid());
        }
    }

    private int dispatchGsmMessage(SmsMessage message) {
        this.mNewSmsRegistrants.notifyResult(message);
        return 0;
    }

    private void handleSms(byte[] ba) {
        if (ba[0] != (byte) 0) {
            Rlog.d("ENF", "status : " + ba[0]);
        }
        if (ba[0] == (byte) 3) {
            int n = ba.length;
            byte[] pdu = new byte[(n - 1)];
            System.arraycopy(ba, 1, pdu, 0, n - 1);
            dispatchGsmMessage(SmsMessage.createFromPdu(pdu, SmsMessage.FORMAT_3GPP));
        }
    }

    private void handleSmses(ArrayList<byte[]> messages) {
        int count = messages.size();
        for (int i = 0; i < count; i++) {
            byte[] ba = (byte[]) messages.get(i);
            if (ba[0] != (byte) 0) {
                Rlog.i("ENF", "status " + i + ": " + ba[0]);
            }
            if (ba[0] == (byte) 3) {
                int n = ba.length;
                byte[] pdu = new byte[(n - 1)];
                System.arraycopy(ba, 1, pdu, 0, n - 1);
                dispatchGsmMessage(SmsMessage.createFromPdu(pdu, SmsMessage.FORMAT_3GPP));
                ba[0] = (byte) 1;
            }
        }
    }

    private String findBestLanguage(byte[] languages) {
        String[] locales = this.mContext.getAssets().getLocales();
        if (languages == null || locales == null) {
            return null;
        }
        for (int i = 0; i + 1 < languages.length; i += 2) {
            try {
                String lang = new String(languages, i, 2, "ISO-8859-1");
                log("languages from sim = " + lang);
                int j = 0;
                while (j < locales.length) {
                    if (locales[j] != null && locales[j].length() >= 2 && locales[j].substring(0, 2).equalsIgnoreCase(lang)) {
                        return lang;
                    }
                    j++;
                }
                continue;
            } catch (UnsupportedEncodingException e) {
                log("Failed to parse USIM language records" + e);
            }
        }
        return null;
    }

    protected void onRecordLoaded() {
        this.mRecordsToLoad--;
        log("onRecordLoaded " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
        if (this.mRecordsToLoad == 0 && this.mRecordsRequested) {
            onAllRecordsLoaded();
        } else if (this.mRecordsToLoad < 0) {
            loge("recordsToLoad <0, programmer error suspected");
            this.mRecordsToLoad = 0;
        }
    }

    public static boolean isNeedToChangeRegion(Context context) {
        if (Global.getInt(context.getContentResolver(), CHANGE_TO_REGION, 0) == 0) {
            return true;
        }
        return false;
    }

    public static void ChangeRegion(Context context, boolean isOn) {
        Global.putInt(context.getContentResolver(), CHANGE_TO_REGION, isOn ? 0 : 1);
    }

    public static boolean isNetLockRegionMachine() {
        if (SystemProperties.get(RegionLockConstant.NETLOCK_VERSION, "NULL").equals("NULL")) {
            return false;
        }
        return true;
    }

    public boolean isIndiaAirtelPlmn() {
        String mccmncCode = getOperatorNumeric();
        if (mccmncCode != null) {
            for (String mccmnc : INDIA_AIRTEL_PLMN) {
                if (mccmnc.equals(mccmncCode)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isTataDocomoPlmn() {
        String mccmncCode = getOperatorNumeric();
        if (mccmncCode != null) {
            for (String mccmnc : INDIA_TATA_DOCOMO_PLMN) {
                if (mccmnc.equals(mccmncCode)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setVoiceCallForwardingFlagFromSimRecords() {
        boolean z = false;
        int i = 1;
        if (checkEfCfis()) {
            this.mCallForwardingStatus = this.mEfCfis[1] & 1;
            Phone phone = this.mPhone;
            if (this.mCallForwardingStatus == 1) {
                z = true;
            }
            phone.setVoiceCallForwardingFlag(1, z, null);
            log("EF_CFIS: callForwardingEnabled=" + this.mCallForwardingStatus);
        } else if (this.mEfCff != null) {
            if ((this.mEfCff[0] & 15) != 10) {
                i = 0;
            }
            this.mCallForwardingStatus = i;
            log("EF_CFF: callForwardingEnabled=" + this.mCallForwardingStatus);
        } else {
            this.mCallForwardingStatus = -1;
            log("EF_CFIS and EF_CFF not valid. callForwardingEnabled=" + this.mCallForwardingStatus);
        }
    }

    protected void onAllRecordsLoaded() {
        log("record load complete");
        if (Resources.getSystem().getBoolean(17957023)) {
            setSimLanguage(this.mEfLi, this.mEfPl);
        } else {
            log("Not using EF LI/EF PL");
        }
        setVoiceCallForwardingFlagFromSimRecords();
        if (this.mParentApp.getState() == AppState.APPSTATE_PIN || this.mParentApp.getState() == AppState.APPSTATE_PUK || this.mParentApp.getState() == AppState.APPSTATE_SUBSCRIPTION_PERSO) {
            this.mRecordsRequested = false;
            return;
        }
        String operator = getOperatorNumeric();
        if (TextUtils.isEmpty(operator)) {
            log("onAllRecordsLoaded empty 'gsm.sim.operator.numeric' skipping");
        } else {
            log("onAllRecordsLoaded set 'gsm.sim.operator.numeric' to operator='" + operator + "'");
            log("update icc_operator_numeric=" + operator);
            this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), operator);
            SubscriptionController subController = SubscriptionController.getInstance();
            subController.setMccMnc(operator, subController.getDefaultSubId());
        }
        if (TextUtils.isEmpty(this.mImsi)) {
            log("onAllRecordsLoaded empty imsi skipping setting mcc");
        } else {
            String countryCode;
            log("onAllRecordsLoaded set mcc imsi" + UsimPBMemInfo.STRING_NOT_SET);
            try {
                countryCode = MccTable.countryCodeForMcc(Integer.parseInt(this.mImsi.substring(0, 3)));
            } catch (NumberFormatException e) {
                countryCode = null;
                loge("SIMRecords: Corrupt IMSI!");
            }
            this.mTelephonyManager.setSimCountryIsoForPhone(this.mParentApp.getPhoneId(), countryCode);
        }
        if (this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.version.exp")) {
            String region = SystemProperties.get("persist.sys.oppo.region", "CN");
            String operatorVersion = SystemProperties.get("ro.oppo.operator", UsimPBMemInfo.STRING_NOT_SET);
            boolean isNetLockRegion = isNetLockRegionMachine();
            String country = this.mTelephonyManager.getSimCountryIsoForPhone(this.mParentApp.getPhoneId());
            CharSequence upperCountry = null;
            if (isNeedToChangeRegion(this.mContext) && !isNetLockRegion && TextUtils.isEmpty(operatorVersion)) {
                if (!TextUtils.isEmpty(country)) {
                    upperCountry = country.toUpperCase();
                }
                if (!TextUtils.isEmpty(upperCountry) && upperCountry.equals("CN")) {
                    upperCountry = "OC";
                }
                log("upperCountry = " + upperCountry);
                if (!(TextUtils.isEmpty(upperCountry) || upperCountry.equals(region))) {
                    log("Need to change region");
                    boolean result = this.mPhone.getContext().getPackageManager().loadRegionFeature(upperCountry);
                    log("result " + result);
                    if (result) {
                        SystemProperties.set("persist.sys.oppo.region", upperCountry);
                        this.mContext.sendBroadcast(new Intent("android.settings.OPPO_REGION_CHANGED"));
                    }
                }
                ChangeRegion(this.mContext, false);
            }
        }
        setVoiceMailByCountry(operator);
        setSpnFromConfig(getOperatorNumeric());
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        log("imsi = " + this.mImsi + " operator = " + operator);
        if (operator != null) {
            if (operator.equals("46002") || operator.equals("46007")) {
                operator = "46000";
            }
            setSystemProperty("gsm.sim.operator.default-name", SpnOverride.getInstance().lookupOperatorName(SubscriptionManager.getSubIdUsingPhoneId(this.mParentApp.getPhoneId()), operator, true, this.mContext));
        }
        fetchPnnAndOpl();
        fetchRatBalancing();
        fetchSmsp();
        fetchGbaRecords();
        if (this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.version.exp")) {
            this.mContext.sendBroadcast(new Intent("android.intent.action.INSERT_TEST_SIM"));
        }
    }

    private void handleCarrierNameOverride() {
        CarrierConfigManager configLoader = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configLoader != null && configLoader.getConfig().getBoolean("carrier_name_override_bool")) {
            String carrierName = configLoader.getConfig().getString("carrier_name_string");
            setServiceProviderName(carrierName);
            this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), carrierName);
        } else if (getRecordsLoaded()) {
            setSpnFromConfig(getOperatorNumeric());
        }
    }

    private void setSpnFromConfig(String carrier) {
        boolean isCnList = isInCnList(this.mSpn);
        if (TextUtils.isEmpty(this.mSpn) || ((isCnList || (this.mSpn != null && this.mSpn.startsWith(RadioCapabilitySwitchUtil.CN_MCC))) && carrier != null && carrier.startsWith(RadioCapabilitySwitchUtil.CN_MCC))) {
            if (isCnList && "20404".equals(carrier)) {
                carrier = "46011";
            }
            String operator = SubscriptionController.getOemOperator(this.mContext, carrier);
            if (!TextUtils.isEmpty(operator)) {
                setServiceProviderName(operator);
            }
        }
        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), getServiceProviderName());
    }

    private void setVoiceMailByCountry(String spn) {
        if (this.mVmConfig.containsCarrier(spn)) {
            log("setVoiceMailByCountry");
            this.mIsVoiceMailFixed = true;
            this.mVoiceMailNum = this.mVmConfig.getVoiceMailNumber(spn);
            this.mVoiceMailTag = this.mVmConfig.getVoiceMailTag(spn);
        }
    }

    public void onReady() {
        fetchSimRecords();
    }

    private void onLocked() {
        log("only fetch EF_LI and EF_PL in lock state");
        loadEfLiAndEfPl();
    }

    private void loadEfLiAndEfPl() {
        if (this.mParentApp.getType() == AppType.APPTYPE_USIM) {
            this.mRecordsRequested = true;
            this.mFh.loadEFTransparent(IccConstants.EF_LI, obtainMessage(100, new EfUsimLiLoaded(this, null)));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(12037, obtainMessage(100, new EfPlLoaded(this, null)));
            this.mRecordsToLoad++;
        }
    }

    private void loadCallForwardingRecords() {
        this.mRecordsRequested = true;
        this.mFh.loadEFLinearFixed(IccConstants.EF_CFIS, 1, obtainMessage(32));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_CFF_CPHS, obtainMessage(24));
        this.mRecordsToLoad++;
    }

    protected void fetchSimRecords() {
        this.mRecordsRequested = true;
        log("fetchSimRecords " + this.mRecordsToLoad);
        this.mCi.getIMSIForApp(this.mParentApp.getAid(), obtainMessage(3));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(4));
        this.mRecordsToLoad++;
        new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MSISDN, getExtFromEf(IccConstants.EF_MSISDN), 1, obtainMessage(10));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixed(IccConstants.EF_MBI, 1, obtainMessage(5));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_AD, obtainMessage(9));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixed(IccConstants.EF_MWIS, 1, obtainMessage(7));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS, obtainMessage(8));
        this.mRecordsToLoad++;
        loadCallForwardingRecords();
        getSpnFsm(true, null);
        this.mFh.loadEFTransparent(IccConstants.EF_SPDI, obtainMessage(13));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_SST, obtainMessage(17));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_INFO_CPHS, obtainMessage(26));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_CSP_CPHS, obtainMessage(33));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_GID1, obtainMessage(34));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_GID2, obtainMessage(36));
        this.mRecordsToLoad++;
        loadEfLiAndEfPl();
        if (this.mTelephonyExt == null) {
            loge("fetchSimRecords(): mTelephonyExt is null!!!");
        } else if (this.mTelephonyExt.isSetLanguageBySIM()) {
            this.mFh.loadEFTransparent(IccConstants.EF_SUME, obtainMessage(53));
            this.mRecordsToLoad++;
        }
        log("fetchSimRecords " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
    }

    public int getDisplayRule(String plmn) {
        boolean bSpnActive = false;
        String spn = getServiceProviderName();
        if (!(this.mEfSST == null || this.mParentApp == null)) {
            if (this.mParentApp.getType() == AppType.APPTYPE_USIM) {
                if (this.mEfSST.length >= 3 && (this.mEfSST[2] & 4) == 4) {
                    bSpnActive = true;
                    log("getDisplayRule USIM mEfSST is " + IccUtils.bytesToHexString(this.mEfSST) + " set bSpnActive to true");
                }
            } else if (this.mEfSST.length >= 5 && (this.mEfSST[4] & 2) == 2) {
                bSpnActive = true;
                log("getDisplayRule SIM mEfSST is " + IccUtils.bytesToHexString(this.mEfSST) + " set bSpnActive to true");
            }
        }
        log("getDisplayRule mParentApp is " + (this.mParentApp != null ? this.mParentApp : "null"));
        if (this.mParentApp != null && this.mParentApp.getUiccCard() != null && this.mParentApp.getUiccCard().getOperatorBrandOverride() != null) {
            log("getDisplayRule, getOperatorBrandOverride is not null");
            return 2;
        } else if (!bSpnActive || TextUtils.isEmpty(spn) || spn.equals(UsimPBMemInfo.STRING_NOT_SET) || this.mSpnDisplayCondition == -1) {
            log("getDisplayRule, no EF_SPN");
            return 2;
        } else if (isOnMatchingPlmn(plmn)) {
            if ((this.mSpnDisplayCondition & 1) == 1) {
                return 3;
            }
            return 1;
        } else if ((this.mSpnDisplayCondition & 2) == 0) {
            return 3;
        } else {
            return 2;
        }
    }

    private boolean isOnMatchingPlmn(String plmn) {
        if (plmn == null) {
            return false;
        }
        if (isHPlmn(plmn)) {
            return true;
        }
        if (this.mSpdiNetworks != null) {
            for (String spdiNet : this.mSpdiNetworks) {
                if (plmn.equals(spdiNet)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void getSpnFsm(boolean start, AsyncResult ar) {
        if (start) {
            if (this.mSpnState == GetSpnFsmState.READ_SPN_3GPP || this.mSpnState == GetSpnFsmState.READ_SPN_CPHS || this.mSpnState == GetSpnFsmState.READ_SPN_SHORT_CPHS || this.mSpnState == GetSpnFsmState.INIT) {
                this.mSpnState = GetSpnFsmState.INIT;
                return;
            }
            this.mSpnState = GetSpnFsmState.INIT;
        }
        byte[] data;
        String spn;
        switch (m19xf89fffa2()[this.mSpnState.ordinal()]) {
            case 1:
                this.mFh.loadEFTransparent(IccConstants.EF_SPN, obtainMessage(12));
                this.mRecordsToLoad++;
                this.mSpnState = GetSpnFsmState.READ_SPN_3GPP;
                break;
            case 2:
                if (ar == null || ar.exception != null) {
                    this.mSpnState = GetSpnFsmState.READ_SPN_CPHS;
                } else {
                    data = ar.result;
                    this.mSpnDisplayCondition = data[0] & 255;
                    setServiceProviderName(IccUtils.adnStringFieldToString(data, 1, data.length - 1));
                    spn = getServiceProviderName();
                    if (spn == null || spn.length() == 0) {
                        this.mSpnState = GetSpnFsmState.READ_SPN_CPHS;
                    } else {
                        log("Load EF_SPN: " + spn + " spnDisplayCondition: " + this.mSpnDisplayCondition);
                        if (OemConstant.EXP_VERSION) {
                            this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), getServiceProviderName());
                        } else {
                            setSpnFromConfig(getOperatorNumeric());
                        }
                        this.mSpnState = GetSpnFsmState.IDLE;
                    }
                }
                if (this.mSpnState == GetSpnFsmState.READ_SPN_CPHS) {
                    this.mFh.loadEFTransparent(IccConstants.EF_SPN_CPHS, obtainMessage(12));
                    this.mRecordsToLoad++;
                    this.mSpnDisplayCondition = -1;
                    break;
                }
                break;
            case 3:
                if (ar == null || ar.exception != null) {
                    this.mSpnState = GetSpnFsmState.READ_SPN_SHORT_CPHS;
                } else {
                    data = (byte[]) ar.result;
                    setServiceProviderName(IccUtils.adnStringFieldToString(data, 0, data.length));
                    spn = getServiceProviderName();
                    if (spn == null || spn.length() == 0) {
                        this.mSpnState = GetSpnFsmState.READ_SPN_SHORT_CPHS;
                    } else {
                        this.mSpnDisplayCondition = 2;
                        log("Load EF_SPN_CPHS: " + spn);
                        if (OemConstant.EXP_VERSION) {
                            this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), getServiceProviderName());
                        } else {
                            setSpnFromConfig(getOperatorNumeric());
                        }
                        this.mSpnState = GetSpnFsmState.IDLE;
                    }
                }
                if (this.mSpnState == GetSpnFsmState.READ_SPN_SHORT_CPHS) {
                    this.mFh.loadEFTransparent(IccConstants.EF_SPN_SHORT_CPHS, obtainMessage(12));
                    this.mRecordsToLoad++;
                    break;
                }
                break;
            case 4:
                if (ar == null || ar.exception != null) {
                    setServiceProviderName(null);
                    log("No SPN loaded in either CHPS or 3GPP");
                } else {
                    data = (byte[]) ar.result;
                    setServiceProviderName(IccUtils.adnStringFieldToString(data, 0, data.length));
                    spn = getServiceProviderName();
                    if (spn == null || spn.length() == 0) {
                        log("No SPN loaded in either CHPS or 3GPP");
                    } else {
                        this.mSpnDisplayCondition = 2;
                        log("Load EF_SPN_SHORT_CPHS: " + spn);
                        if (OemConstant.EXP_VERSION) {
                            this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), getServiceProviderName());
                        } else {
                            setSpnFromConfig(getOperatorNumeric());
                        }
                    }
                }
                this.mSpnState = GetSpnFsmState.IDLE;
                break;
            default:
                this.mSpnState = GetSpnFsmState.IDLE;
                break;
        }
    }

    private void parseEfSpdi(byte[] data) {
        SimTlv tlv = new SimTlv(data, 0, data.length);
        byte[] plmnEntries = null;
        while (tlv.isValidObject()) {
            if (tlv.getTag() == 163) {
                tlv = new SimTlv(tlv.getData(), 0, tlv.getData().length);
            }
            if (tlv.getTag() == 128) {
                plmnEntries = tlv.getData();
                break;
            }
            tlv.nextObject();
        }
        if (plmnEntries != null) {
            this.mSpdiNetworks = new ArrayList(plmnEntries.length / 3);
            for (int i = 0; i + 2 < plmnEntries.length; i += 3) {
                String plmnCode = IccUtils.parsePlmnToString(plmnEntries, i, 3);
                if (plmnCode.length() >= 5) {
                    log("EF_SPDI network: " + plmnCode);
                    this.mSpdiNetworks.add(plmnCode);
                }
            }
        }
    }

    private boolean isCphsMailboxEnabled() {
        boolean z = true;
        if (this.mCphsInfo == null) {
            return false;
        }
        if ((this.mCphsInfo[1] & 48) != 48) {
            z = false;
        }
        return z;
    }

    protected void log(String s) {
        Rlog.d(LOG_TAG, "[SIMRecords] " + s + " (slot " + this.mSlotId + ")");
    }

    protected void loge(String s) {
        Rlog.e(LOG_TAG, "[SIMRecords] " + s + " (slot " + this.mSlotId + ")");
    }

    protected void logw(String s, Throwable tr) {
        Rlog.w(LOG_TAG, "[SIMRecords] " + s + " (slot " + this.mSlotId + ")", tr);
    }

    protected void logv(String s) {
        Rlog.v(LOG_TAG, "[SIMRecords] " + s + " (slot " + this.mSlotId + ")");
    }

    public boolean isCspPlmnEnabled() {
        return this.mCspPlmnEnabled;
    }

    private void handleEfCspData(byte[] data) {
        int usedCspGroups = data.length / 2;
        this.mCspPlmnEnabled = true;
        for (int i = 0; i < usedCspGroups; i++) {
            if (data[i * 2] == (byte) -64) {
                log("[CSP] found ValueAddedServicesGroup, value " + data[(i * 2) + 1]);
                if ((data[(i * 2) + 1] & 128) == 128) {
                    this.mCspPlmnEnabled = true;
                } else {
                    this.mCspPlmnEnabled = false;
                    log("[CSP] Set Automatic Network Selection");
                    this.mNetworkSelectionModeAutomaticRegistrants.notifyRegistrants();
                }
                return;
            }
        }
        log("[CSP] Value Added Service Group (0xC0), not found!");
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("SIMRecords: " + this);
        pw.println(" extends:");
        super.dump(fd, pw, args);
        pw.println(" mVmConfig=" + this.mVmConfig);
        pw.println(" mSpnOverride=" + this.mSpnOverride);
        pw.println(" mCallForwardingStatus=" + this.mCallForwardingStatus);
        pw.println(" mSpnState=" + this.mSpnState);
        pw.println(" mCphsInfo=" + this.mCphsInfo);
        pw.println(" mCspPlmnEnabled=" + this.mCspPlmnEnabled);
        pw.println(" mEfMWIS[]=" + Arrays.toString(this.mEfMWIS));
        pw.println(" mEfCPHS_MWI[]=" + Arrays.toString(this.mEfCPHS_MWI));
        pw.println(" mEfCff[]=" + Arrays.toString(this.mEfCff));
        pw.println(" mEfCfis[]=" + Arrays.toString(this.mEfCfis));
        pw.println(" mSpnDisplayCondition=" + this.mSpnDisplayCondition);
        pw.println(" mSpdiNetworks[]=" + this.mSpdiNetworks);
        pw.println(" mPnnHomeName=" + this.mPnnHomeName);
        pw.println(" mUsimServiceTable=" + this.mUsimServiceTable);
        pw.println(" mGid1=" + this.mGid1);
        pw.println(" mGid2=" + this.mGid2);
        pw.flush();
    }

    public String getSpNameInEfSpn() {
        log("getSpNameInEfSpn(): " + this.mSpNameInEfSpn);
        return this.mSpNameInEfSpn;
    }

    public String isOperatorMvnoForImsi() {
        String imsiPattern = SpnOverride.getInstance().isOperatorMvnoForImsi(getOperatorNumeric(), getIMSI());
        String mccmnc = getOperatorNumeric();
        log("isOperatorMvnoForImsi(), imsiPattern: " + imsiPattern + ", mccmnc: " + mccmnc);
        if (imsiPattern == null || mccmnc == null) {
            return null;
        }
        String result = imsiPattern.substring(mccmnc.length(), imsiPattern.length());
        log("isOperatorMvnoForImsi(): " + result);
        return result;
    }

    public String getFirstFullNameInEfPnn() {
        if (this.mPnnNetworkNames == null || this.mPnnNetworkNames.size() == 0) {
            log("getFirstFullNameInEfPnn(): empty");
            return null;
        }
        OperatorName opName = (OperatorName) this.mPnnNetworkNames.get(0);
        log("getFirstFullNameInEfPnn(): first fullname: " + opName.sFullName);
        if (opName.sFullName != null) {
            return new String(opName.sFullName);
        }
        return null;
    }

    public String isOperatorMvnoForEfPnn() {
        String MCCMNC = getOperatorNumeric();
        String PNN = getFirstFullNameInEfPnn();
        log("isOperatorMvnoForEfPnn(): mccmnc = " + MCCMNC + ", pnn = " + PNN);
        if (SpnOverride.getInstance().getSpnByEfPnn(MCCMNC, PNN) != null) {
            return PNN;
        }
        return null;
    }

    public String getMvnoMatchType() {
        String IMSI = getIMSI();
        String SPN = getSpNameInEfSpn();
        String PNN = getFirstFullNameInEfPnn();
        String GID1 = getGid1();
        String MCCMNC = getOperatorNumeric();
        log("getMvnoMatchType(): imsi = " + IMSI + ", mccmnc = " + MCCMNC + ", spn = " + SPN);
        if (SpnOverride.getInstance().getSpnByEfSpn(MCCMNC, SPN) != null) {
            return "spn";
        }
        if (SpnOverride.getInstance().getSpnByImsi(MCCMNC, IMSI) != null) {
            return "imsi";
        }
        if (SpnOverride.getInstance().getSpnByEfPnn(MCCMNC, PNN) != null) {
            return "pnn";
        }
        if (SpnOverride.getInstance().getSpnByEfGid1(MCCMNC, GID1) != null) {
            return "gid";
        }
        return UsimPBMemInfo.STRING_NOT_SET;
    }

    private void wipeAllSIMContacts() {
        log("wipeAllSIMContacts");
        this.mAdnCache.reset();
        log("wipeAllSIMContacts after reset");
    }

    private void processShutdownIPO() {
        this.hasQueryIccId = false;
        this.iccIdQueryState = -1;
        this.mIccId = null;
        this.mImsi = null;
        this.mSpNameInEfSpn = null;
    }

    private void fetchEccList() {
        int eccFromModemUrc = SystemProperties.getInt("ril.ef.ecc.support", 0);
        log("fetchEccList(), eccFromModemUrc:" + eccFromModemUrc);
        if (eccFromModemUrc == 0) {
            this.mEfEcc = UsimPBMemInfo.STRING_NOT_SET;
            if (this.mParentApp.getType() == AppType.APPTYPE_USIM) {
                this.mFh.loadEFLinearFixedAll(IccConstants.EF_ECC, obtainMessage(103));
            } else {
                this.mFh.loadEFTransparent(IccConstants.EF_ECC, obtainMessage(102));
            }
        }
    }

    private void updateConfiguration(String numeric) {
        if (TextUtils.isEmpty(numeric) || this.mOldMccMnc.equals(numeric)) {
            log("Do not update configuration if mcc mnc no change.");
            return;
        }
        this.mOldMccMnc = numeric;
        MccTable.updateMccMncConfiguration(this.mContext, this.mOldMccMnc, false);
    }

    private void parseEFpnn(ArrayList messages) {
        int count = messages.size();
        log("parseEFpnn(): pnn has " + count + " records");
        this.mPnnNetworkNames = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            byte[] data = (byte[]) messages.get(i);
            log("parseEFpnn(): pnn record " + i + " content is " + IccUtils.bytesToHexString(data));
            SimTlv tlv = new SimTlv(data, 0, data.length);
            OperatorName opName = new OperatorName();
            while (tlv.isValidObject()) {
                if (tlv.getTag() == TAG_FULL_NETWORK_NAME) {
                    opName.sFullName = IccUtils.networkNameToString(tlv.getData(), 0, tlv.getData().length);
                    log("parseEFpnn(): pnn sFullName is " + opName.sFullName);
                } else if (tlv.getTag() == 69) {
                    opName.sShortName = IccUtils.networkNameToString(tlv.getData(), 0, tlv.getData().length);
                    log("parseEFpnn(): pnn sShortName is " + opName.sShortName);
                }
                tlv.nextObject();
            }
            this.mPnnNetworkNames.add(opName);
        }
    }

    private void fetchPnnAndOpl() {
        boolean z = true;
        log("fetchPnnAndOpl()");
        boolean bPnnActive = false;
        this.mReadingOpl = false;
        if (this.mEfSST != null) {
            if (this.mParentApp.getType() == AppType.APPTYPE_USIM) {
                if (this.mEfSST.length >= 6) {
                    if ((this.mEfSST[5] & 16) == 16) {
                        bPnnActive = true;
                    } else {
                        bPnnActive = false;
                    }
                    if (bPnnActive) {
                        if ((this.mEfSST[5] & 32) != 32) {
                            z = false;
                        }
                        this.mReadingOpl = z;
                    }
                }
            } else if (this.mEfSST.length >= 13) {
                if ((this.mEfSST[12] & 48) == 48) {
                    bPnnActive = true;
                } else {
                    bPnnActive = false;
                }
                if (bPnnActive) {
                    if ((this.mEfSST[12] & 192) != 192) {
                        z = false;
                    }
                    this.mReadingOpl = z;
                }
            }
        }
        log("bPnnActive = " + bPnnActive + ", bOplActive = " + this.mReadingOpl);
        if (bPnnActive) {
            this.mFh.loadEFLinearFixedAll(IccConstants.EF_PNN, obtainMessage(15));
            if (this.mReadingOpl) {
                this.mFh.loadEFLinearFixedAll(IccConstants.EF_OPL, obtainMessage(104));
            }
        }
    }

    private void fetchSpn() {
        log("fetchSpn()");
        if (getSIMServiceStatus(IccService.SPN) == IccServiceStatus.ACTIVATED) {
            setServiceProviderName(null);
            this.mFh.loadEFTransparent(IccConstants.EF_SPN, obtainMessage(12));
            this.mRecordsToLoad++;
            return;
        }
        log("[SIMRecords] SPN service is not activated  ");
    }

    public IccServiceStatus getSIMServiceStatus(IccService enService) {
        int nServiceNum = enService.getIndex();
        IccServiceStatus simServiceStatus = IccServiceStatus.UNKNOWN;
        log("getSIMServiceStatus enService is " + enService + " Service Index is " + nServiceNum);
        if (nServiceNum >= 0 && nServiceNum < IccService.UNSUPPORTED_SERVICE.getIndex() && this.mEfSST != null) {
            int nbyte;
            int nbit;
            if (this.mParentApp.getType() == AppType.APPTYPE_USIM) {
                int nUSTIndex = usimServiceNumber[nServiceNum];
                if (nUSTIndex <= 0) {
                    simServiceStatus = IccServiceStatus.NOT_EXIST_IN_USIM;
                } else {
                    nbyte = nUSTIndex / 8;
                    nbit = nUSTIndex % 8;
                    if (nbit == 0) {
                        nbit = 7;
                        nbyte--;
                    } else {
                        nbit--;
                    }
                    log("getSIMServiceStatus USIM nbyte: " + nbyte + " nbit: " + nbit);
                    simServiceStatus = (this.mEfSST.length <= nbyte || (this.mEfSST[nbyte] & (1 << nbit)) <= 0) ? IccServiceStatus.INACTIVATED : IccServiceStatus.ACTIVATED;
                }
            } else {
                int nSSTIndex = simServiceNumber[nServiceNum];
                if (nSSTIndex <= 0) {
                    simServiceStatus = IccServiceStatus.NOT_EXIST_IN_SIM;
                } else {
                    nbyte = nSSTIndex / 4;
                    nbit = nSSTIndex % 4;
                    if (nbit == 0) {
                        nbit = 3;
                        nbyte--;
                    } else {
                        nbit--;
                    }
                    int nMask = 2 << (nbit * 2);
                    log("getSIMServiceStatus SIM nbyte: " + nbyte + " nbit: " + nbit + " nMask: " + nMask);
                    simServiceStatus = (this.mEfSST.length <= nbyte || (this.mEfSST[nbyte] & nMask) != nMask) ? IccServiceStatus.INACTIVATED : IccServiceStatus.ACTIVATED;
                }
            }
        }
        log("getSIMServiceStatus simServiceStatus: " + simServiceStatus);
        return simServiceStatus;
    }

    private void fetchSmsp() {
        log("fetchSmsp()");
        if (this.mUsimServiceTable != null && this.mParentApp.getType() != AppType.APPTYPE_SIM && this.mUsimServiceTable.isAvailable(UsimService.SM_SERVICE_PARAMS)) {
            log("SMSP support.");
            this.mFh.loadEFLinearFixed(IccConstants.EF_SMSP, 1, obtainMessage(208));
            if (this.mUsimServiceTable.isAvailable(UsimService.SM_OVER_IP)) {
                log("PSISMSP support.");
                this.mFh.loadEFLinearFixed(28645, 1, obtainMessage(EVENT_GET_PSISMSC_DONE));
            }
        }
    }

    private void fetchGbaRecords() {
        log("fetchGbaRecords");
        if (this.mUsimServiceTable != null && this.mParentApp.getType() != AppType.APPTYPE_SIM && this.mUsimServiceTable.isAvailable(UsimService.GBA)) {
            log("GBA support.");
            this.mFh.loadEFTransparent(IccConstants.EF_ISIM_GBABP, obtainMessage(EVENT_GET_GBABP_DONE));
            this.mFh.loadEFLinearFixedAll(IccConstants.EF_ISIM_GBANL, obtainMessage(EVENT_GET_GBANL_DONE));
        }
    }

    private void fetchMbiRecords() {
        log("fetchMbiRecords");
        if (this.mUsimServiceTable != null && this.mParentApp.getType() != AppType.APPTYPE_SIM && this.mUsimServiceTable.isAvailable(UsimService.MBDN)) {
            log("MBI/MBDN support.");
            this.mFh.loadEFLinearFixed(IccConstants.EF_MBI, 1, obtainMessage(5));
            this.mRecordsToLoad++;
        }
    }

    private void fetchMwisRecords() {
        log("fetchMwisRecords");
        if (this.mUsimServiceTable != null && this.mParentApp.getType() != AppType.APPTYPE_SIM && this.mUsimServiceTable.isAvailable(UsimService.MWI_STATUS)) {
            log("MWIS support.");
            this.mFh.loadEFLinearFixed(IccConstants.EF_MWIS, 1, obtainMessage(7));
            this.mRecordsToLoad++;
        }
    }

    private void parseEFopl(ArrayList messages) {
        int count = messages.size();
        log("parseEFopl(): opl has " + count + " records");
        this.mOperatorList = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            byte[] data = (byte[]) messages.get(i);
            OplRecord oplRec = new OplRecord();
            oplRec.sPlmn = IccUtils.parsePlmnToStringForEfOpl(data, 0, 3);
            byte[] minLac = new byte[2];
            minLac[0] = data[3];
            minLac[1] = data[4];
            oplRec.nMinLAC = Integer.parseInt(IccUtils.bytesToHexString(minLac), 16);
            byte[] maxLAC = new byte[2];
            maxLAC[0] = data[5];
            maxLAC[1] = data[6];
            oplRec.nMaxLAC = Integer.parseInt(IccUtils.bytesToHexString(maxLAC), 16);
            byte[] pnnRecordIndex = new byte[1];
            pnnRecordIndex[0] = data[7];
            oplRec.nPnnIndex = Integer.parseInt(IccUtils.bytesToHexString(pnnRecordIndex), 16);
            log("parseEFopl(): record=" + i + " content=" + IccUtils.bytesToHexString(data) + " sPlmn=" + oplRec.sPlmn + " nMinLAC=" + oplRec.nMinLAC + " nMaxLAC=" + oplRec.nMaxLAC + " nPnnIndex=" + oplRec.nPnnIndex);
            this.mOperatorList.add(oplRec);
        }
    }

    private void boradcastEfRatContentNotify(int item) {
        Intent intent = new Intent("android.intent.action.ACTION_EF_RAT_CONTENT_NOTIFY");
        intent.putExtra("ef_rat_status", item);
        intent.putExtra(SimInfo.SLOT, this.mSlotId);
        log("broadCast intent ACTION_EF_RAT_CONTENT_NOTIFY: item: " + item + ", simId: " + this.mSlotId);
        ActivityManagerNative.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
    }

    private void processEfCspPlmnModeBitUrc(int bit) {
        log("processEfCspPlmnModeBitUrc: bit = " + bit);
        if (bit == 0) {
            this.mCspPlmnEnabled = false;
        } else {
            this.mCspPlmnEnabled = true;
        }
        Intent intent = new Intent("android.intent.action.ACTION_EF_CSP_CONTENT_NOTIFY");
        intent.putExtra("plmn_mode_bit", bit);
        intent.putExtra(SimInfo.SLOT, this.mSlotId);
        log("broadCast intent ACTION_EF_CSP_CONTENT_NOTIFY, EXTRA_PLMN_MODE_BIT: " + bit);
        ActivityManagerNative.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
    }

    private void fetchLanguageIndicator() {
        log("fetchLanguageIndicator ");
        String l = SystemProperties.get("persist.sys.language");
        String c = SystemProperties.get("persist.sys.country");
        String oldSimLang = SystemProperties.get("persist.sys.simlanguage");
        if (l != null && l.length() != 0) {
            return;
        }
        if (c != null && c.length() != 0) {
            return;
        }
        if (oldSimLang == null || oldSimLang.length() == 0) {
            if (this.mEfLi == null) {
                this.mFh.loadEFTransparent(IccConstants.EF_LI, obtainMessage(42));
                this.efLanguageToLoad++;
            }
            this.mFh.loadEFTransparent(12037, obtainMessage(43));
            this.efLanguageToLoad++;
        }
    }

    private void onLanguageFileLoaded() {
        this.efLanguageToLoad--;
        log("onLanguageFileLoaded efLanguageToLoad is " + this.efLanguageToLoad);
        if (this.efLanguageToLoad == 0) {
            log("onLanguageFileLoaded all language file loaded");
            if (this.mEfLi == null && this.mEfELP == null) {
                log("onLanguageFileLoaded all language file are not exist!");
            } else {
                setLanguageFromSIM();
            }
        }
    }

    private void setLanguageFromSIM() {
        boolean bMatched;
        log("setLanguageFromSIM ");
        if (this.mParentApp.getType() == AppType.APPTYPE_USIM) {
            bMatched = getMatchedLocaleByLI(this.mEfLi);
        } else {
            bMatched = getMatchedLocaleByLP(this.mEfLi);
        }
        if (!(bMatched || this.mEfELP == null)) {
            bMatched = getMatchedLocaleByLI(this.mEfELP);
        }
        log("setLanguageFromSIM End");
    }

    private boolean getMatchedLocaleByLI(byte[] data) {
        boolean ret = false;
        if (data == null) {
            return false;
        }
        int lenOfLI = data.length;
        int i = 0;
        while (i + 2 <= lenOfLI) {
            String lang = IccUtils.parseLanguageIndicator(data, i, 2);
            log("USIM language in language indicator: i is " + i + " language is " + lang);
            if (lang != null && !lang.equals(UsimPBMemInfo.STRING_NOT_SET)) {
                ret = matchLangToLocale(lang.toLowerCase());
                if (ret) {
                    break;
                }
                i += 2;
            } else {
                log("USIM language in language indicator: i is " + i + " language is empty");
                break;
            }
        }
        return ret;
    }

    private boolean getMatchedLocaleByLP(byte[] data) {
        boolean ret = false;
        if (data == null) {
            return false;
        }
        int lenOfLP = data.length;
        String lang = null;
        int i = 0;
        while (i < lenOfLP) {
            int index = this.mEfLi[0] & 255;
            if (index >= 0 && index <= 15) {
                lang = LANGUAGE_CODE_FOR_LP[index];
            } else if (32 <= index && index <= 47) {
                lang = LANGUAGE_CODE_FOR_LP[index - 16];
            }
            log("SIM language in language preference: i is " + i + " language is " + lang);
            if (lang != null && !lang.equals(UsimPBMemInfo.STRING_NOT_SET)) {
                ret = matchLangToLocale(lang);
                if (ret) {
                    break;
                }
                i++;
            } else {
                log("SIM language in language preference: i is " + i + " language is empty");
                break;
            }
        }
        return ret;
    }

    private boolean matchLangToLocale(String lang) {
        String[] locals = this.mContext.getAssets().getLocales();
        int localsSize = locals.length;
        for (int i = 0; i < localsSize; i++) {
            String s = locals[i];
            if (s.length() == 5) {
                String language = s.substring(0, 2);
                log("Supported languages: the i" + i + " th is " + language);
                if (lang.equals(language)) {
                    log("Matched! lang: " + lang + ", country is " + s.substring(3, 5));
                    return true;
                }
            }
        }
        return false;
    }

    public String getMenuTitleFromEf() {
        return this.mMenuTitleFromEf;
    }

    private void fetchCPHSOns() {
        log("fetchCPHSOns()");
        this.cphsOnsl = null;
        this.cphsOnss = null;
        this.mFh.loadEFTransparent(IccConstants.EF_SPN_CPHS, obtainMessage(105));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_SPN_SHORT_CPHS, obtainMessage(106));
        this.mRecordsToLoad++;
    }

    private void fetchRatBalancing() {
        if (this.mTelephonyExt != null && !this.mTelephonyExt.isSetLanguageBySIM()) {
            log("support MTK_RAT_BALANCING");
            if (this.mParentApp.getType() == AppType.APPTYPE_USIM) {
                log("start loading EF_RAT");
                this.mFh.loadEFTransparent(IccConstants.EF_RAT, obtainMessage(EVENT_GET_RAT_DONE));
            } else if (this.mParentApp.getType() == AppType.APPTYPE_SIM) {
                log("loading EF_RAT fail, because of SIM");
                this.mEfRatLoaded = false;
                this.mEfRat = null;
                boradcastEfRatContentNotify(512);
            } else {
                log("loading EF_RAT fail, because of +EUSIM");
            }
        }
    }

    public int getEfRatBalancing() {
        log("getEfRatBalancing: iccCardType = " + this.mParentApp.getType() + ", mEfRatLoaded = " + this.mEfRatLoaded + ", mEfRat is null = " + (this.mEfRat == null));
        if (this.mParentApp.getType() == AppType.APPTYPE_USIM && this.mEfRatLoaded && this.mEfRat == null) {
            return 256;
        }
        return 512;
    }

    public boolean isHPlmn(String plmn) {
        ServiceStateTracker sst = this.mPhone.getServiceStateTracker();
        if (sst != null) {
            return sst.isHPlmn(plmn);
        }
        log("can't get sst");
        return false;
    }

    private boolean isMatchingPlmnForEfOpl(String simPlmn, String bcchPlmn) {
        if (simPlmn == null || simPlmn.equals(UsimPBMemInfo.STRING_NOT_SET) || bcchPlmn == null || bcchPlmn.equals(UsimPBMemInfo.STRING_NOT_SET)) {
            return false;
        }
        log("isMatchingPlmnForEfOpl(): simPlmn = " + simPlmn + ", bcchPlmn = " + bcchPlmn);
        int simPlmnLen = simPlmn.length();
        int bcchPlmnLen = bcchPlmn.length();
        if (simPlmnLen < 5 || bcchPlmnLen < 5) {
            return false;
        }
        int i = 0;
        while (i < 5) {
            if (simPlmn.charAt(i) != 'd' && simPlmn.charAt(i) != bcchPlmn.charAt(i)) {
                return false;
            }
            i++;
        }
        if (simPlmnLen == 6 && bcchPlmnLen == 6) {
            return simPlmn.charAt(5) == 'd' || simPlmn.charAt(5) == bcchPlmn.charAt(5);
        } else {
            if (bcchPlmnLen != 6 || bcchPlmn.charAt(5) == '0' || bcchPlmn.charAt(5) == 'd') {
                return simPlmnLen != 6 || simPlmn.charAt(5) == '0' || simPlmn.charAt(5) == 'd';
            } else {
                return false;
            }
        }
    }

    private boolean isPlmnEqualsSimNumeric(String plmn) {
        String mccmnc = getOperatorNumeric();
        if (plmn == null) {
            return false;
        }
        if (mccmnc == null || mccmnc.equals(UsimPBMemInfo.STRING_NOT_SET)) {
            log("isPlmnEqualsSimNumeric: getOperatorNumeric error: " + mccmnc);
            return false;
        } else if (plmn.equals(mccmnc)) {
            return true;
        } else {
            return plmn.length() == 5 && mccmnc.length() == 6 && plmn.equals(mccmnc.substring(0, 5));
        }
    }

    public String getEonsIfExist(String plmn, int nLac, boolean bLongNameRequired) {
        log("EONS getEonsIfExist: plmn is " + plmn + " nLac is " + nLac + " bLongNameRequired: " + bLongNameRequired);
        if (plmn == null || this.mPnnNetworkNames == null || this.mPnnNetworkNames.size() == 0) {
            return null;
        }
        int nPnnIndex = -1;
        boolean isHPLMN = isPlmnEqualsSimNumeric(plmn);
        if (this.mOperatorList != null) {
            int i = 0;
            while (i < this.mOperatorList.size()) {
                OplRecord oplRec = (OplRecord) this.mOperatorList.get(i);
                if (!isMatchingPlmnForEfOpl(oplRec.sPlmn, plmn) || (!(oplRec.nMinLAC == 0 && oplRec.nMaxLAC == 65534) && (oplRec.nMinLAC > nLac || oplRec.nMaxLAC < nLac))) {
                    i++;
                } else {
                    log("getEonsIfExist: find it in EF_OPL");
                    if (oplRec.nPnnIndex == 0) {
                        log("getEonsIfExist: oplRec.nPnnIndex is 0, from other sources");
                        return null;
                    }
                    nPnnIndex = oplRec.nPnnIndex;
                }
            }
        } else if (isHPLMN) {
            log("getEonsIfExist: Plmn is HPLMN, return PNN's first record");
            nPnnIndex = 1;
        } else {
            log("getEonsIfExist: Plmn is not HPLMN and no mOperatorList, return null");
            return null;
        }
        if (nPnnIndex == -1 && isHPLMN && this.mOperatorList.size() == 1) {
            log("getEonsIfExist: not find it in EF_OPL, but Plmn is HPLMN, return PNN's first record");
            nPnnIndex = 1;
        } else if (nPnnIndex > 1 && nPnnIndex > this.mPnnNetworkNames.size() && isHPLMN) {
            log("getEonsIfExist: find it in EF_OPL, but index in EF_OPL > EF_PNN list length & Plmn is HPLMN, return PNN's first record");
            nPnnIndex = 1;
        } else if (nPnnIndex > 1 && nPnnIndex > this.mPnnNetworkNames.size() && !isHPLMN) {
            log("getEonsIfExist: find it in EF_OPL, but index in EF_OPL > EF_PNN list length & Plmn is not HPLMN, return PNN's first record");
            nPnnIndex = -1;
        }
        String sEons = null;
        if (nPnnIndex >= 1) {
            OperatorName opName = (OperatorName) this.mPnnNetworkNames.get(nPnnIndex - 1);
            if (bLongNameRequired) {
                if (opName.sFullName != null) {
                    sEons = new String(opName.sFullName);
                } else if (opName.sShortName != null) {
                    sEons = new String(opName.sShortName);
                }
            } else if (!bLongNameRequired) {
                if (opName.sShortName != null) {
                    sEons = new String(opName.sShortName);
                } else if (opName.sFullName != null) {
                    sEons = new String(opName.sFullName);
                }
            }
            String spn = getServiceProviderName();
            String simCardMccMnc = getOperatorNumeric();
            log("getEonsIfExist spn = " + spn + ", simCardMccMnc " + simCardMccMnc);
            if (!TextUtils.isEmpty(spn) && "50503".equals(simCardMccMnc) && "50503".equals(plmn)) {
                sEons = spn;
                log("sEons = " + spn);
            }
        }
        log("getEonsIfExist: sEons is " + sEons);
        return sEons;
    }

    public String getEfGbabp() {
        log("GBABP = " + this.mGbabp);
        return this.mGbabp;
    }

    public void setEfGbabp(String gbabp, Message onComplete) {
        byte[] data = IccUtils.hexStringToBytes(gbabp);
        log("setEfGbabp data = " + data);
        this.mFh.updateEFTransparent(IccConstants.EF_GBABP, data, onComplete);
    }

    public byte[] getEfPsismsc() {
        log("PSISMSC = " + this.mEfPsismsc);
        return this.mEfPsismsc;
    }

    public byte[] getEfSmsp() {
        log("mEfSmsp = " + this.mEfPsismsc);
        return this.mEfSmsp;
    }

    public int getMncLength() {
        log("mncLength = " + this.mMncLength);
        return this.mMncLength;
    }

    public void broadcastPhbStateChangedIntent(boolean isReady) {
        if (this.mPhone.getPhoneType() == 1 || (this.isDispose && !isReady)) {
            log("broadcastPhbStateChangedIntent, mPhbReady " + this.mPhbReady);
            if (isReady) {
                int phoneId = this.mParentApp.getPhoneId();
                this.mSubId = SubscriptionManager.getSubIdUsingPhoneId(phoneId);
                String strAllSimState = SystemProperties.get("gsm.sim.state");
                String strCurSimState = UsimPBMemInfo.STRING_NOT_SET;
                if (strAllSimState != null && strAllSimState.length() > 0) {
                    String[] values = strAllSimState.split(",");
                    if (phoneId >= 0 && phoneId < values.length && values[phoneId] != null) {
                        strCurSimState = values[phoneId];
                    }
                }
                if (this.mSubId <= 0 || strCurSimState.equals("NOT_READY")) {
                    log("broadcastPhbStateChangedIntent, mSubId " + this.mSubId + ", sim state " + strAllSimState);
                    this.mPhbWaitSub = true;
                    return;
                }
            } else if (this.mSubId <= 0) {
                log("broadcastPhbStateChangedIntent, isReady == false and mSubId <= 0");
                return;
            }
            Intent intent = new Intent("android.intent.action.PHB_STATE_CHANGED");
            intent.putExtra("ready", isReady);
            intent.putExtra("subscription", this.mSubId);
            log("Broadcasting intent ACTION_PHB_STATE_CHANGED " + isReady + " sub id " + this.mSubId + " phoneId " + this.mParentApp.getPhoneId());
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            if (!isReady) {
                this.mSubId = -1;
            }
            return;
        }
        log("broadcastPhbStateChangedIntent, Not active Phone.");
    }

    public boolean isPhbReady() {
        log("isPhbReady(): cached mPhbReady = " + (this.mPhbReady ? "true" : "false"));
        String strPhbReady = "false";
        String strAllSimState = UsimPBMemInfo.STRING_NOT_SET;
        String strCurSimState = UsimPBMemInfo.STRING_NOT_SET;
        int phoneId = this.mParentApp.getPhoneId();
        strPhbReady = SystemProperties.get(SIMRECORD_PROPERTY_RIL_PHB_READY[this.mParentApp.getSlotId()], "false");
        strAllSimState = SystemProperties.get("gsm.sim.state");
        if (strAllSimState != null && strAllSimState.length() > 0) {
            String[] values = strAllSimState.split(",");
            if (phoneId >= 0 && phoneId < values.length && values[phoneId] != null) {
                strCurSimState = values[phoneId];
            }
        }
        boolean isSimLocked;
        if (strCurSimState.equals("NETWORK_LOCKED")) {
            isSimLocked = true;
        } else {
            isSimLocked = strCurSimState.equals("PIN_REQUIRED");
        }
        log("isPhbReady(): mPhbReady = " + (this.mPhbReady ? "true" : "false") + ", strCurSimState = " + strCurSimState);
        if (!strPhbReady.equals("true") || isSimLocked) {
            return false;
        }
        return true;
    }

    public void setPhbReady(boolean isReady) {
        log("setPhbReady(): isReady = " + (isReady ? "true" : "false"));
        if (this.mPhbReady != isReady) {
            String strPhbReady = isReady ? "true" : "false";
            this.mPhbReady = isReady;
            SystemProperties.set(SIMRECORD_PROPERTY_RIL_PHB_READY[this.mParentApp.getSlotId()], strPhbReady);
            broadcastPhbStateChangedIntent(this.mPhbReady);
        }
    }

    public boolean isRadioAvailable() {
        if (this.mCi != null) {
            return this.mCi.getRadioState().isAvailable();
        }
        return false;
    }

    protected int getChildPhoneId() {
        int phoneId = this.mParentApp.getPhoneId();
        log("[getChildPhoneId] phoneId = " + phoneId);
        return phoneId;
    }

    protected void updatePHBStatus(int status, boolean isSimLocked) {
        log("[updatePHBStatus] status : " + status + " | isSimLocked : " + isSimLocked + " | mPhbReady : " + this.mPhbReady);
        if (status == 1) {
            if (isSimLocked) {
                log("phb ready but sim is not ready.");
            } else if (!this.mPhbReady) {
                this.mPhbReady = true;
                broadcastPhbStateChangedIntent(this.mPhbReady);
            }
        } else if (status == 0 && this.mPhbReady) {
            this.mAdnCache.reset();
            this.mPhbReady = false;
            broadcastPhbStateChangedIntent(this.mPhbReady);
        }
    }

    private String convertNumberIfContainsPrefix(String dialNumber) {
        String r = dialNumber;
        if (dialNumber == null) {
            return r;
        }
        if (!dialNumber.startsWith("tel:") && !dialNumber.startsWith("sip:") && !dialNumber.startsWith("sips:")) {
            return r;
        }
        r = dialNumber.substring(dialNumber.indexOf(":") + 1);
        Rlog.d(LOG_TAG, "convertNumberIfContainsPrefix: dialNumber = " + dialNumber);
        return r;
    }
}
