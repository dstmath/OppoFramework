package com.android.internal.telephony.gsm;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.mediatek.internal.telephony.uicc.AlphaTag;
import com.mediatek.internal.telephony.uicc.CsimPhbStorageInfo;
import com.mediatek.internal.telephony.uicc.EFResponseData;
import com.mediatek.internal.telephony.uicc.PhbEntry;
import com.mediatek.internal.telephony.uicc.UsimGroup;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
public class UsimPhoneBookManager extends Handler implements IccConstants {
    private static final boolean DBG = false;
    private static final int EVENT_AAS_LOAD_DONE = 5;
    private static final int EVENT_AAS_LOAD_DONE_OPTMZ = 28;
    private static final int EVENT_AAS_UPDATE_DONE = 10;
    private static final int EVENT_ANR_RECORD_LOAD_DONE = 16;
    private static final int EVENT_ANR_RECORD_LOAD_OPTMZ_DONE = 23;
    private static final int EVENT_ANR_UPDATE_DONE = 9;
    private static final int EVENT_EMAIL_LOAD_DONE = 4;
    private static final int EVENT_EMAIL_RECORD_LOAD_DONE = 15;
    private static final int EVENT_EMAIL_RECORD_LOAD_OPTMZ_DONE = 22;
    private static final int EVENT_EMAIL_UPDATE_DONE = 8;
    private static final int EVENT_EXT1_LOAD_DONE = 1001;
    private static final int EVENT_GAS_LOAD_DONE = 6;
    private static final int EVENT_GAS_UPDATE_DONE = 13;
    private static final int EVENT_GET_RECORDS_SIZE_DONE = 1000;
    private static final int EVENT_GRP_RECORD_LOAD_DONE = 17;
    private static final int EVENT_GRP_UPDATE_DONE = 12;
    private static final int EVENT_IAP_LOAD_DONE = 3;
    private static final int EVENT_IAP_RECORD_LOAD_DONE = 14;
    private static final int EVENT_IAP_UPDATE_DONE = 7;
    private static final int EVENT_PBR_LOAD_DONE = 1;
    private static final int EVENT_QUERY_ANR_AVAILABLE_OPTMZ_DONE = 26;
    private static final int EVENT_QUERY_EMAIL_AVAILABLE_OPTMZ_DONE = 25;
    private static final int EVENT_QUERY_PHB_ADN_INFO = 21;
    private static final int EVENT_QUERY_SNE_AVAILABLE_OPTMZ_DONE = 27;
    private static final int EVENT_SELECT_EF_FILE_DONE = 20;
    private static final int EVENT_SNE_RECORD_LOAD_DONE = 18;
    private static final int EVENT_SNE_RECORD_LOAD_OPTMZ_DONE = 24;
    private static final int EVENT_SNE_UPDATE_DONE = 11;
    private static final int EVENT_UPB_CAPABILITY_QUERY_DONE = 19;
    private static final int EVENT_USIM_ADN_LOAD_DONE = 2;
    private static final byte INVALID_BYTE = (byte) -1;
    private static final int INVALID_SFI = -1;
    private static final String LOG_TAG = "UsimPhoneBookManager";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.log.tag.tel_dbg";
    private static final int UPB_EF_AAS = 3;
    private static final int UPB_EF_ANR = 0;
    private static final int UPB_EF_EMAIL = 1;
    private static final int UPB_EF_GAS = 4;
    private static final int UPB_EF_GRP = 5;
    private static final int UPB_EF_SNE = 2;
    private static final int USIM_EFAAS_TAG = 199;
    private static final int USIM_EFADN_TAG = 192;
    private static final int USIM_EFANR_TAG = 196;
    private static final int USIM_EFCCP1_TAG = 203;
    private static final int USIM_EFEMAIL_TAG = 202;
    private static final int USIM_EFEXT1_TAG = 194;
    private static final int USIM_EFGRP_TAG = 198;
    private static final int USIM_EFGSD_TAG = 200;
    private static final int USIM_EFIAP_TAG = 193;
    private static final int USIM_EFPBC_TAG = 197;
    private static final int USIM_EFSNE_TAG = 195;
    private static final int USIM_EFUID_TAG = 201;
    public static final int USIM_ERROR_CAPACITY_FULL = -30;
    public static final int USIM_ERROR_GROUP_COUNT = -20;
    public static final int USIM_ERROR_NAME_LEN = -10;
    public static final int USIM_ERROR_OTHERS = -50;
    public static final int USIM_ERROR_STRING_TOOLONG = -40;
    public static final int USIM_MAX_ANR_COUNT = 3;
    private static final int USIM_TYPE1_TAG = 168;
    private static final int USIM_TYPE2_CONDITIONAL_LENGTH = 2;
    private static final int USIM_TYPE2_TAG = 169;
    private static final int USIM_TYPE3_TAG = 170;
    protected EFResponseData efData;
    private ArrayList<String> mAasForAnr;
    private AdnRecordCache mAdnCache;
    private int mAdnFileSize;
    private int[] mAdnRecordSize;
    private ArrayList<int[]> mAnrInfo;
    private int mAnrRecordSize;
    private CommandsInterface mCi;
    private UiccCardApplication mCurrentApp;
    private ArrayList<byte[]> mEmailFileRecord;
    private int mEmailFileSize;
    private int[] mEmailInfo;
    private int[] mEmailRecTable;
    private int mEmailRecordSize;
    private SparseArray<ArrayList<String>> mEmailsForAdnRec;
    private ArrayList<ArrayList<byte[]>> mExt1FileList;
    private IccFileHandler mFh;
    private ArrayList<UsimGroup> mGasForGrp;
    private final Object mGasLock;
    private ArrayList<ArrayList<byte[]>> mIapFileList;
    private ArrayList<byte[]> mIapFileRecord;
    private Boolean mIsPbrPresent;
    private final Object mLock;
    private AtomicBoolean mNeedNotify;
    private ArrayList<Integer> mOPPOEFRecNum;
    Handler mOppoHandler;
    private ArrayList<PbrRecord> mPbrRecords;
    private ArrayList<AdnRecord> mPhoneBookRecords;
    private AtomicInteger mReadingAnrNum;
    private AtomicInteger mReadingEmailNum;
    private AtomicInteger mReadingGrpNum;
    private AtomicInteger mReadingIapNum;
    private AtomicInteger mReadingSneNum;
    private SparseArray<int[]> mRecordSize;
    private boolean mRefreshAdnInfo;
    private boolean mRefreshAnrInfo;
    private boolean mRefreshCache;
    private boolean mRefreshEmailInfo;
    private int mResult;
    private SparseIntArray mSfiEfidTable;
    private int mSliceCount;
    private int mSlotId;
    private int[] mSneInfo;
    private final Object mUPBCapabilityLock;
    private int[] mUpbCap;
    private int mUpbDone;
    private Message pendingResponse;

    /* renamed from: com.android.internal.telephony.gsm.UsimPhoneBookManager$1 */
    class AnonymousClass1 extends Handler {
        final /* synthetic */ UsimPhoneBookManager this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.1.<init>(com.android.internal.telephony.gsm.UsimPhoneBookManager):void, dex: 
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
        AnonymousClass1(com.android.internal.telephony.gsm.UsimPhoneBookManager r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.1.<init>(com.android.internal.telephony.gsm.UsimPhoneBookManager):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.UsimPhoneBookManager.1.<init>(com.android.internal.telephony.gsm.UsimPhoneBookManager):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.1.handleMessage(android.os.Message):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.1.handleMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.UsimPhoneBookManager.1.handleMessage(android.os.Message):void");
        }
    }

    private class File {
        public int mAnrIndex;
        private final int mEfid;
        private final int mIndex;
        private final int mParentTag;
        public int mPbrRecord;
        private final int mSfi;
        public int mTag;
        final /* synthetic */ UsimPhoneBookManager this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.<init>(com.android.internal.telephony.gsm.UsimPhoneBookManager, int, int, int, int):void, dex:  in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.<init>(com.android.internal.telephony.gsm.UsimPhoneBookManager, int, int, int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.<init>(com.android.internal.telephony.gsm.UsimPhoneBookManager, int, int, int, int):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        File(com.android.internal.telephony.gsm.UsimPhoneBookManager r1, int r2, int r3, int r4, int r5) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.<init>(com.android.internal.telephony.gsm.UsimPhoneBookManager, int, int, int, int):void, dex:  in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.<init>(com.android.internal.telephony.gsm.UsimPhoneBookManager, int, int, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.<init>(com.android.internal.telephony.gsm.UsimPhoneBookManager, int, int, int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getEfid():int, dex: 
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
        public int getEfid() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getEfid():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getEfid():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getIndex():int, dex:  in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getIndex():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getIndex():int, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public int getIndex() {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getIndex():int, dex:  in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getIndex():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getIndex():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getParentTag():int, dex:  in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getParentTag():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getParentTag():int, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public int getParentTag() {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getParentTag():int, dex:  in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getParentTag():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getParentTag():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getSfi():int, dex: 
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
        public int getSfi() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getSfi():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.getSfi():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.toString():java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.UsimPhoneBookManager.File.toString():java.lang.String");
        }
    }

    private class PbrRecord {
        private int mAnrIndex;
        private SparseArray<File> mFileIds;
        private int mMasterFileRecordNum;
        final /* synthetic */ UsimPhoneBookManager this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.-get0(com.android.internal.telephony.gsm.UsimPhoneBookManager$PbrRecord):int, dex: 
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
        /* renamed from: -get0 */
        static /* synthetic */ int m179-get0(com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.-get0(com.android.internal.telephony.gsm.UsimPhoneBookManager$PbrRecord):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.-get0(com.android.internal.telephony.gsm.UsimPhoneBookManager$PbrRecord):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.-get1(com.android.internal.telephony.gsm.UsimPhoneBookManager$PbrRecord):android.util.SparseArray, dex: 
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
        /* renamed from: -get1 */
        static /* synthetic */ android.util.SparseArray m180-get1(com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.-get1(com.android.internal.telephony.gsm.UsimPhoneBookManager$PbrRecord):android.util.SparseArray, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.-get1(com.android.internal.telephony.gsm.UsimPhoneBookManager$PbrRecord):android.util.SparseArray");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.-set0(com.android.internal.telephony.gsm.UsimPhoneBookManager$PbrRecord, int):int, dex:  in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.-set0(com.android.internal.telephony.gsm.UsimPhoneBookManager$PbrRecord, int):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.-set0(com.android.internal.telephony.gsm.UsimPhoneBookManager$PbrRecord, int):int, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        /* renamed from: -set0 */
        static /* synthetic */ int m181-set0(com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.-set0(com.android.internal.telephony.gsm.UsimPhoneBookManager$PbrRecord, int):int, dex:  in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.-set0(com.android.internal.telephony.gsm.UsimPhoneBookManager$PbrRecord, int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.-set0(com.android.internal.telephony.gsm.UsimPhoneBookManager$PbrRecord, int):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.<init>(com.android.internal.telephony.gsm.UsimPhoneBookManager, byte[]):void, dex: 
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
        PbrRecord(com.android.internal.telephony.gsm.UsimPhoneBookManager r1, byte[] r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.<init>(com.android.internal.telephony.gsm.UsimPhoneBookManager, byte[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.<init>(com.android.internal.telephony.gsm.UsimPhoneBookManager, byte[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.parseEfAndSFI(com.android.internal.telephony.gsm.SimTlv, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        void parseEfAndSFI(com.android.internal.telephony.gsm.SimTlv r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.parseEfAndSFI(com.android.internal.telephony.gsm.SimTlv, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.parseEfAndSFI(com.android.internal.telephony.gsm.SimTlv, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.parseTag(com.android.internal.telephony.gsm.SimTlv):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        void parseTag(com.android.internal.telephony.gsm.SimTlv r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.parseTag(com.android.internal.telephony.gsm.SimTlv):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord.parseTag(com.android.internal.telephony.gsm.SimTlv):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.gsm.UsimPhoneBookManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.UsimPhoneBookManager.<clinit>():void");
    }

    public UsimPhoneBookManager(IccFileHandler fh, AdnRecordCache cache) {
        this.mSlotId = -1;
        this.mLock = new Object();
        this.mGasLock = new Object();
        this.mUPBCapabilityLock = new Object();
        this.mEmailRecordSize = -1;
        this.mEmailFileSize = 100;
        this.mAdnFileSize = 250;
        this.mAnrRecordSize = -1;
        this.mSliceCount = 0;
        this.mUpbDone = -1;
        this.mIapFileList = null;
        this.mRefreshCache = false;
        this.mRefreshEmailInfo = false;
        this.mRefreshAnrInfo = false;
        this.mRefreshAdnInfo = false;
        this.mEmailRecTable = new int[400];
        this.mUpbCap = new int[8];
        this.mResult = -1;
        this.mReadingAnrNum = new AtomicInteger(0);
        this.mReadingEmailNum = new AtomicInteger(0);
        this.mReadingGrpNum = new AtomicInteger(0);
        this.mReadingSneNum = new AtomicInteger(0);
        this.mReadingIapNum = new AtomicInteger(0);
        this.mNeedNotify = new AtomicBoolean(false);
        this.efData = null;
        this.mOppoHandler = new AnonymousClass1(this);
        this.mOPPOEFRecNum = new ArrayList();
        this.mFh = fh;
        this.mPhoneBookRecords = new ArrayList();
        this.mGasForGrp = new ArrayList();
        this.mIapFileList = new ArrayList();
        this.mPbrRecords = null;
        this.mIsPbrPresent = Boolean.valueOf(true);
        this.mAdnCache = cache;
        this.mCi = null;
        this.mCurrentApp = null;
        this.mEmailsForAdnRec = new SparseArray();
        this.mSfiEfidTable = new SparseIntArray();
        logi("constructor finished. ");
    }

    public UsimPhoneBookManager(IccFileHandler fh, AdnRecordCache cache, CommandsInterface ci, UiccCardApplication app) {
        int i = -1;
        this.mSlotId = -1;
        this.mLock = new Object();
        this.mGasLock = new Object();
        this.mUPBCapabilityLock = new Object();
        this.mEmailRecordSize = -1;
        this.mEmailFileSize = 100;
        this.mAdnFileSize = 250;
        this.mAnrRecordSize = -1;
        this.mSliceCount = 0;
        this.mUpbDone = -1;
        this.mIapFileList = null;
        this.mRefreshCache = false;
        this.mRefreshEmailInfo = false;
        this.mRefreshAnrInfo = false;
        this.mRefreshAdnInfo = false;
        this.mEmailRecTable = new int[400];
        this.mUpbCap = new int[8];
        this.mResult = -1;
        this.mReadingAnrNum = new AtomicInteger(0);
        this.mReadingEmailNum = new AtomicInteger(0);
        this.mReadingGrpNum = new AtomicInteger(0);
        this.mReadingSneNum = new AtomicInteger(0);
        this.mReadingIapNum = new AtomicInteger(0);
        this.mNeedNotify = new AtomicBoolean(false);
        this.efData = null;
        this.mOppoHandler = new AnonymousClass1(this);
        this.mOPPOEFRecNum = new ArrayList();
        this.mFh = fh;
        this.mPhoneBookRecords = new ArrayList();
        this.mGasForGrp = new ArrayList();
        this.mIapFileList = new ArrayList();
        this.mPbrRecords = null;
        this.mIsPbrPresent = Boolean.valueOf(true);
        this.mAdnCache = cache;
        this.mCi = ci;
        this.mCurrentApp = app;
        if (app != null) {
            i = app.getSlotId();
        }
        this.mSlotId = i;
        this.mEmailsForAdnRec = new SparseArray();
        this.mSfiEfidTable = new SparseIntArray();
        logi("constructor finished. ");
    }

    public void reset() {
        this.mPhoneBookRecords.clear();
        this.mIapFileRecord = null;
        this.mEmailFileRecord = null;
        this.mPbrRecords = null;
        this.mIsPbrPresent = Boolean.valueOf(true);
        this.mRefreshCache = false;
        this.mEmailsForAdnRec.clear();
        this.mSfiEfidTable.clear();
        this.mGasForGrp.clear();
        this.mIapFileList = null;
        this.mAasForAnr = null;
        this.mExt1FileList = null;
        this.mSliceCount = 0;
        this.mEmailRecTable = new int[400];
        this.mEmailInfo = null;
        this.mSneInfo = null;
        this.mAnrInfo = null;
        for (int i = 0; i < 8; i++) {
            this.mUpbCap[i] = -1;
        }
        this.mEmailRecordSize = -1;
        this.mAnrRecordSize = -1;
        this.mUpbDone = -1;
        this.mAdnRecordSize = null;
        this.mRefreshEmailInfo = false;
        this.mRefreshAnrInfo = false;
        this.mRefreshAdnInfo = false;
        this.mOPPOEFRecNum.clear();
        if (this.mLock != null) {
            synchronized (this.mLock) {
                Rlog.d(LOG_TAG, "leon.UsimPhoneBookManager release. ");
                this.mLock.notifyAll();
            }
        }
        logi("reset finished. ");
    }

    public ArrayList<AdnRecord> loadEfFilesFromUsim() {
        synchronized (this.mLock) {
            long prevTime = System.currentTimeMillis();
            ArrayList<AdnRecord> arrayList;
            if (!this.mPhoneBookRecords.isEmpty()) {
                if (this.mRefreshCache) {
                    this.mRefreshCache = false;
                    refreshCache();
                }
                arrayList = this.mPhoneBookRecords;
                return arrayList;
            } else if (this.mIsPbrPresent.booleanValue()) {
                if (this.mPbrRecords == null || this.mPbrRecords.size() == 0) {
                    readPbrFileAndWait(true);
                }
                Rlog.d(LOG_TAG, "loadEfFilesFromUsim, mIsPbrPresent: " + this.mIsPbrPresent);
                if (this.mPbrRecords == null || this.mPbrRecords.size() == 0) {
                    readAdnFileAndWait(0);
                    arrayList = this.mAdnCache.getRecordsIfLoaded(28474);
                    return arrayList;
                }
                int[] size;
                int i;
                if (this.mEmailRecordSize < 0) {
                    readEmailRecordSize();
                }
                if (this.mAnrRecordSize < 0) {
                    readAnrRecordSize();
                }
                int adnEf = ((File) PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(0)).get(192)).getEfid();
                if (adnEf > 0) {
                    size = readEFLinearRecordSize(adnEf);
                    if (size != null && size.length == 3) {
                        this.mAdnFileSize = size[2];
                    }
                }
                if (PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(0)).get(195) != null) {
                    size = readEFLinearRecordSize(((File) PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(0)).get(195)).getEfid());
                }
                int numRecs = this.mPbrRecords.size();
                if (CsimPhbStorageInfo.hasModemPhbEnhanceCapability(this.mFh)) {
                    readAasFileAndWaitOptmz();
                    readAdnFileAndWait(0);
                } else {
                    for (i = 0; i < numRecs; i++) {
                        readAASFileAndWait(i);
                        readAdnFileAndWaitForUICC(i);
                    }
                }
                if (this.mPhoneBookRecords.isEmpty()) {
                    arrayList = this.mPhoneBookRecords;
                    return arrayList;
                }
                if (CsimPhbStorageInfo.hasModemPhbEnhanceCapability(this.mFh)) {
                    readSneFileAndWaitOptmz();
                    readAnrFileAndWaitOptmz();
                    readEmailFileAndWaitOptmz();
                } else {
                    for (i = 0; i < numRecs; i++) {
                        if (isSupportSne()) {
                            readSneFileAndWait(i);
                        }
                        readAnrFileAndWait(i);
                        readEmailFileAndWait(i);
                    }
                }
                readGrpIdsAndWait();
                this.mUpbDone = 1;
                log("loadEfFilesFromUsim Time: " + (System.currentTimeMillis() - prevTime));
                return this.mPhoneBookRecords;
            } else {
                return null;
            }
        }
    }

    private void refreshCache() {
        if (this.mPbrRecords != null) {
            this.mPhoneBookRecords.clear();
            int numRecs = this.mPbrRecords.size();
            for (int i = 0; i < numRecs; i++) {
                readAdnFileAndWait(i);
            }
        }
    }

    public void invalidateCache() {
        this.mRefreshCache = true;
    }

    private void readPbrFileAndWait(boolean is7FFF) {
        this.mFh.loadEFLinearFixedAll((int) IccConstants.EF_PBR, obtainMessage(1), is7FFF);
        try {
            this.mLock.wait();
        } catch (InterruptedException e) {
            Rlog.e(LOG_TAG, "Interrupted Exception in readPbrFileAndWait");
        }
    }

    private void readEmailFileAndWait(int recId) {
        logi("readEmailFileAndWait " + recId);
        if (this.mPbrRecords != null) {
            SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(recId));
            if (files != null) {
                File emailFile = (File) files.get(USIM_EFEMAIL_TAG);
                if (emailFile != null) {
                    int emailEfid = emailFile.getEfid();
                    if (emailFile.getParentTag() == 168) {
                        readType1Ef(emailFile, 0);
                    } else if (emailFile.getParentTag() == 169) {
                        readType2Ef(emailFile);
                    }
                }
            }
        }
    }

    private void readIapFileAndWait(int pbrIndex, int efid, boolean forceRefresh) {
        logi("readIapFileAndWait pbrIndex :" + pbrIndex + ",efid:" + efid + ",forceRefresh:" + forceRefresh);
        if (efid > 0) {
            int[] size;
            if (this.mIapFileList == null) {
                logi("readIapFileAndWait IapFileList is null !!!! recreate it !");
                this.mIapFileList = new ArrayList();
            }
            if (this.mRecordSize == null || this.mRecordSize.get(efid) == null) {
                size = readEFLinearRecordSize(efid);
            } else {
                size = (int[]) this.mRecordSize.get(efid);
            }
            if (size == null || size.length != 3) {
                Rlog.e(LOG_TAG, "readIapFileAndWait: read record size error.");
                this.mIapFileList.add(pbrIndex, new ArrayList());
                return;
            }
            int i;
            if (this.mIapFileList.size() <= pbrIndex) {
                log("Create IAP first!");
                ArrayList<byte[]> iapList = new ArrayList();
                for (i = 0; i < this.mAdnFileSize; i++) {
                    Object value = new byte[size[0]];
                    int lens = value.length;
                    for (int le = 0; le < lens; le++) {
                        value[le] = (byte) -1;
                    }
                    iapList.add(value);
                }
                this.mIapFileList.add(pbrIndex, iapList);
            } else {
                log("This IAP has been loaded!");
                if (!forceRefresh) {
                    return;
                }
            }
            int numAdnRecs = this.mPhoneBookRecords.size();
            int nOffset = pbrIndex * this.mAdnFileSize;
            int nMax = nOffset + this.mAdnFileSize;
            if (numAdnRecs < nMax) {
                nMax = numAdnRecs;
            }
            log("readIapFileAndWait nOffset " + nOffset + ", nMax " + nMax);
            int totalReadingNum = 0;
            i = nOffset;
            while (i < nMax) {
                try {
                    AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(i);
                    if (rec.getAlphaTag().length() > 0 || rec.getNumber().length() > 0) {
                        this.mReadingIapNum.addAndGet(1);
                        int[] data = new int[2];
                        data[0] = pbrIndex;
                        data[1] = i - nOffset;
                        this.mFh.readEFLinearFixed(efid, (i + 1) - nOffset, size[0], obtainMessage(14, data));
                        totalReadingNum++;
                    }
                    i++;
                } catch (IndexOutOfBoundsException e) {
                    Rlog.e(LOG_TAG, "readIapFileAndWait: mPhoneBookRecords IndexOutOfBoundsException numAdnRecs is " + numAdnRecs + "index is " + i);
                }
            }
            if (this.mReadingIapNum.get() == 0) {
                this.mNeedNotify.set(false);
                return;
            }
            this.mNeedNotify.set(true);
            logi("readIapFileAndWait before mLock.wait " + this.mNeedNotify.get() + " total:" + totalReadingNum);
            synchronized (this.mLock) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e2) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readIapFileAndWait");
                }
            }
            logi("readIapFileAndWait after mLock.wait");
            return;
        }
        return;
    }

    private void readAASFileAndWait(int recId) {
        logi("readAASFileAndWait " + recId);
        if (this.mPbrRecords != null) {
            SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(recId));
            if (files != null) {
                File aasFile = (File) files.get(USIM_EFAAS_TAG);
                if (aasFile != null) {
                    int aasEfid = aasFile.getEfid();
                    log("readAASFileAndWait-get AAS EFID " + aasEfid);
                    if (this.mAasForAnr != null) {
                        logi("AAS has been loaded for Pbr number " + recId);
                    }
                    if (this.mFh != null) {
                        Message msg = obtainMessage(5);
                        msg.arg1 = recId;
                        this.mFh.loadEFLinearFixedAll(aasEfid, msg);
                        try {
                            this.mLock.wait();
                        } catch (InterruptedException e) {
                            Rlog.e(LOG_TAG, "Interrupted Exception in readAASFileAndWait");
                        }
                        return;
                    }
                    Rlog.e(LOG_TAG, "readAASFileAndWait-IccFileHandler is null");
                }
            }
        }
    }

    private void readSneFileAndWait(int recId) {
        logi("readSneFileAndWait " + recId);
        if (this.mPbrRecords != null) {
            SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(recId));
            if (files != null) {
                File sneFile = (File) files.get(195);
                if (sneFile != null) {
                    log("readSneFileAndWait: EFSNE id is " + sneFile.getEfid());
                    if (sneFile.getParentTag() == 169) {
                        readType2Ef(sneFile);
                    } else if (sneFile.getParentTag() == 168) {
                        readType1Ef(sneFile, 0);
                    }
                }
            }
        }
    }

    private void readAnrFileAndWait(int recId) {
        logi("readAnrFileAndWait: recId is " + recId);
        if (this.mPbrRecords != null) {
            SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(recId));
            if (files == null) {
                log("readAnrFileAndWait: No anr tag in pbr record " + recId);
                return;
            }
            int index = 0;
            while (index < PbrRecord.m179-get0((PbrRecord) this.mPbrRecords.get(recId))) {
                File anrFile = (File) files.get((index * 256) + 196);
                if (anrFile == null) {
                    index++;
                } else {
                    if (anrFile.getParentTag() == 169) {
                        anrFile.mAnrIndex = index;
                        readType2Ef(anrFile);
                    } else if (anrFile.getParentTag() == 168) {
                        anrFile.mAnrIndex = index;
                        readType1Ef(anrFile, index);
                    }
                }
            }
        }
    }

    private void readGrpIdsAndWait() {
        logi("readGrpIdsAndWait begin");
        if (this.mPbrRecords != null) {
            SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(0));
            if (files != null && ((File) files.get(USIM_EFGRP_TAG)) != null) {
                int totalReadingNum = 0;
                int numAdnRecs = this.mPhoneBookRecords.size();
                int i = 0;
                while (i < numAdnRecs) {
                    try {
                        AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(i);
                        if (rec.getAlphaTag().length() > 0 || rec.getNumber().length() > 0) {
                            this.mReadingGrpNum.incrementAndGet();
                            int adnIndex = rec.getRecId();
                            int[] data = new int[2];
                            data[0] = i;
                            data[1] = adnIndex;
                            this.mCi.readUPBGrpEntry(adnIndex, obtainMessage(17, data));
                            totalReadingNum++;
                        }
                        i++;
                    } catch (IndexOutOfBoundsException e) {
                        Rlog.e(LOG_TAG, "readGrpIdsAndWait: mPhoneBookRecords IndexOutOfBoundsException numAdnRecs is " + numAdnRecs + "index is " + i);
                    }
                }
                if (this.mReadingGrpNum.get() == 0) {
                    this.mNeedNotify.set(false);
                    return;
                }
                this.mNeedNotify.set(true);
                logi("readGrpIdsAndWait before mLock.wait " + this.mNeedNotify.get() + " total:" + totalReadingNum);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e2) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readGrpIdsAndWait");
                }
                logi("readGrpIdsAndWait after mLock.wait");
            }
        }
    }

    private void readAdnFileAndWait(int recId) {
        logi("readAdnFileAndWait: recId is " + recId + UsimPBMemInfo.STRING_NOT_SET);
        int previousSize = this.mPhoneBookRecords.size();
        this.mAdnCache.requestLoadAllAdnLike(28474, this.mAdnCache.extensionEfForEf(28474), obtainMessage(2));
        try {
            this.mLock.wait();
        } catch (InterruptedException e) {
            Rlog.e(LOG_TAG, "Interrupted Exception in readAdnFileAndWait");
        }
        if (this.mPbrRecords != null && this.mPbrRecords.size() > recId) {
            PbrRecord.m181-set0((PbrRecord) this.mPbrRecords.get(recId), this.mPhoneBookRecords.size() - previousSize);
        }
    }

    private void createPbrFile(ArrayList<byte[]> records) {
        if (records == null) {
            this.mPbrRecords = null;
            this.mIsPbrPresent = Boolean.valueOf(false);
            return;
        }
        this.mPbrRecords = new ArrayList();
        this.mSliceCount = 0;
        for (int i = 0; i < records.size(); i++) {
            if (((byte[]) records.get(i))[0] != (byte) -1) {
                this.mPbrRecords.add(new PbrRecord(this, (byte[]) records.get(i)));
            }
        }
        for (PbrRecord record : this.mPbrRecords) {
            File file = (File) PbrRecord.m180-get1(record).get(192);
            if (file != null) {
                int sfi = file.getSfi();
                if (sfi != -1) {
                    this.mSfiEfidTable.put(sfi, ((File) PbrRecord.m180-get1(record).get(192)).getEfid());
                }
            }
        }
    }

    private void readAasFileAndWaitOptmz() {
        logi("readAasFileAndWaitOptmz begin");
        if (this.mAasForAnr == null || this.mAasForAnr.size() == 0) {
            int aasRecNum = 0;
            if (this.mUpbCap[3] >= 0) {
                aasRecNum = this.mUpbCap[3];
            } else if (this.mPbrRecords != null) {
                SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(0));
                if (files != null) {
                    File aasFile = (File) files.get(USIM_EFAAS_TAG);
                    if (aasFile != null) {
                        int[] size = readEFLinearRecordSize(aasFile.getEfid());
                        if (size != null && size.length == 3) {
                            aasRecNum = size[2];
                        }
                    } else {
                        return;
                    }
                }
                return;
            } else {
                return;
            }
            this.mCi.readUPBAasList(1, aasRecNum, obtainMessage(28));
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                Rlog.e(LOG_TAG, "Interrupted Exception in readAasFileAndWaitOptmz");
            }
        }
    }

    private void readEmailFileAndWaitOptmz() {
        if (this.mPbrRecords != null) {
            SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(0));
            if (files != null && ((File) files.get(USIM_EFEMAIL_TAG)) != null) {
                int totalReadingNum = 0;
                int numAdnRecs = this.mPhoneBookRecords.size();
                int i = 0;
                while (i < numAdnRecs) {
                    try {
                        AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(i);
                        if (rec.getAlphaTag().length() > 0 || rec.getNumber().length() > 0) {
                            int[] data = new int[2];
                            data[0] = 0;
                            data[1] = i;
                            this.mReadingEmailNum.incrementAndGet();
                            this.mCi.readUPBEmailEntry(i + 1, 1, obtainMessage(22, data));
                            totalReadingNum++;
                        }
                        i++;
                    } catch (IndexOutOfBoundsException e) {
                        Rlog.e(LOG_TAG, "readEmailFileAndWaitOptmz: mPhoneBookRecords IndexOutOfBoundsnumAdnRecs is " + numAdnRecs + "index is " + i);
                    }
                }
                if (this.mReadingEmailNum.get() == 0) {
                    this.mNeedNotify.set(false);
                    return;
                }
                this.mNeedNotify.set(true);
                logi("readEmailFileAndWaitOptmz before mLock.wait " + this.mNeedNotify.get() + " total:" + totalReadingNum);
                synchronized (this.mLock) {
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e2) {
                        Rlog.e(LOG_TAG, "Interrupted Exception in readEmailFileAndWaitOptmz");
                    }
                }
                logi("readEmailFileAndWaitOptmz after mLock.wait " + this.mNeedNotify.get());
                return;
            }
            return;
        }
        return;
    }

    private void readAnrFileAndWaitOptmz() {
        if (this.mPbrRecords != null) {
            SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(0));
            if (files != null && ((File) files.get(196)) != null) {
                int totalReadingNum = 0;
                int numAdnRecs = this.mPhoneBookRecords.size();
                int i = 0;
                while (i < numAdnRecs) {
                    try {
                        AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(i);
                        if (rec.getAlphaTag().length() > 0 || rec.getNumber().length() > 0) {
                            int[] data = new int[3];
                            data[0] = 0;
                            data[1] = i;
                            data[2] = 0;
                            this.mReadingAnrNum.addAndGet(1);
                            this.mCi.readUPBAnrEntry(i + 1, 1, obtainMessage(23, data));
                            totalReadingNum++;
                        }
                        i++;
                    } catch (IndexOutOfBoundsException e) {
                        Rlog.e(LOG_TAG, "readAnrFileAndWaitOptmz: mPhoneBookRecords IndexOutOfBoundsnumAdnRecs is " + numAdnRecs + "index is " + i);
                    }
                }
                if (this.mReadingAnrNum.get() == 0) {
                    this.mNeedNotify.set(false);
                    return;
                }
                this.mNeedNotify.set(true);
                logi("readAnrFileAndWaitOptmz before mLock.wait " + this.mNeedNotify.get() + " total:" + totalReadingNum);
                synchronized (this.mLock) {
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e2) {
                        Rlog.e(LOG_TAG, "Interrupted Exception in readAnrFileAndWaitOptmz");
                    }
                }
                logi("readAnrFileAndWaitOptmz after mLock.wait " + this.mNeedNotify.get());
                return;
            }
            return;
        }
        return;
    }

    private void readSneFileAndWaitOptmz() {
        if (this.mPbrRecords != null) {
            SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(0));
            if (files != null && ((File) files.get(195)) != null) {
                int totalReadingNum = 0;
                int numAdnRecs = this.mPhoneBookRecords.size();
                int i = 0;
                while (i < numAdnRecs) {
                    try {
                        AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(i);
                        if (rec.getAlphaTag().length() > 0 || rec.getNumber().length() > 0) {
                            int[] data = new int[2];
                            data[0] = 0;
                            data[1] = i;
                            this.mReadingSneNum.incrementAndGet();
                            this.mCi.readUPBSneEntry(i + 1, 1, obtainMessage(24, data));
                            totalReadingNum++;
                        }
                        i++;
                    } catch (IndexOutOfBoundsException e) {
                        Rlog.e(LOG_TAG, "readSneFileAndWaitOptmz: mPhoneBookRecords IndexOutOfBoundsnumAdnRecs is " + numAdnRecs + "index is " + i);
                    }
                }
                if (this.mReadingSneNum.get() == 0) {
                    this.mNeedNotify.set(false);
                    return;
                }
                this.mNeedNotify.set(true);
                logi("readSneFileAndWaitOptmz before mLock.wait " + this.mNeedNotify.get() + " total:" + totalReadingNum);
                synchronized (this.mLock) {
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e2) {
                        Rlog.e(LOG_TAG, "Interrupted Exception in readSneFileAndWaitOptmz");
                    }
                }
                logi("readSneFileAndWaitOptmz after mLock.wait " + this.mNeedNotify.get());
                return;
            }
            return;
        }
        return;
    }

    private void updatePhoneAdnRecordWithEmailByIndexOptmz(int emailIndex, int adnIndex, String email) {
        log("updatePhoneAdnRecordWithEmailByIndex emailIndex = " + emailIndex + ",adnIndex = " + adnIndex + ", email = " + email);
        if (!(email == null || email == null)) {
            try {
                if (!email.equals(UsimPBMemInfo.STRING_NOT_SET)) {
                    AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(adnIndex);
                    String[] strArr = new String[1];
                    strArr[0] = email;
                    rec.setEmails(strArr);
                }
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "[JE]updatePhoneAdnRecordWithEmailByIndex " + e.getMessage());
            }
        }
    }

    private void updatePhoneAdnRecordWithAnrByIndexOptmz(int recId, int adnIndex, int anrIndex, PhbEntry anrData) {
        log("updatePhoneAdnRecordWithAnrByIndexOptmz the " + adnIndex + "th anr record is " + anrData);
        if (!(anrData == null || anrData.number == null || anrData.number.equals(UsimPBMemInfo.STRING_NOT_SET))) {
            String anr;
            if (anrData.ton == 145) {
                anr = PhoneNumberUtils.prependPlusToNumber(anrData.number);
            } else {
                anr = anrData.number;
            }
            anr = anr.replace('?', 'N').replace('p', ',').replace('w', ';');
            int anrAas = anrData.index;
            if (!(anr == null || anr.equals(UsimPBMemInfo.STRING_NOT_SET))) {
                String str = null;
                if (!(anrAas <= 0 || anrAas == 255 || this.mAasForAnr == null || this.mAasForAnr == null || anrAas > this.mAasForAnr.size())) {
                    str = (String) this.mAasForAnr.get(anrAas - 1);
                }
                log(" updatePhoneAdnRecordWithAnrByIndex " + adnIndex + " th anr is " + anr + " the anrIndex is " + anrIndex);
                try {
                    AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(adnIndex);
                    rec.setAnr(anr, anrIndex);
                    if (str != null && str.length() > 0) {
                        rec.setAasIndex(anrAas);
                    }
                    this.mPhoneBookRecords.set(adnIndex, rec);
                } catch (IndexOutOfBoundsException e) {
                    Rlog.e(LOG_TAG, "updatePhoneAdnRecordWithAnrByIndex: mPhoneBookRecords IndexOutOfBoundsException size: " + this.mPhoneBookRecords.size() + "index: " + adnIndex);
                }
            }
        }
    }

    private String[] buildAnrRecordOptmz(String number, int aas) {
        int ton = 129;
        if (number.indexOf(43) != -1) {
            if (number.indexOf(43) != number.lastIndexOf(43)) {
                Rlog.w(LOG_TAG, "There are multiple '+' in the number: " + number);
            }
            ton = 145;
            number = number.replace("+", UsimPBMemInfo.STRING_NOT_SET);
        }
        String[] res = new String[3];
        res[0] = number.replace('N', '?').replace(',', 'p').replace(';', 'w');
        res[1] = Integer.toString(ton);
        res[2] = Integer.toString(aas);
        return res;
    }

    private void updatePhoneAdnRecordWithSneByIndexOptmz(int adnIndex, String sne) {
        if (sne != null) {
            log("updatePhoneAdnRecordWithSneByIndex index " + adnIndex + " recData file is " + sne);
            if (!(sne == null || sne.equals(UsimPBMemInfo.STRING_NOT_SET))) {
                try {
                    ((AdnRecord) this.mPhoneBookRecords.get(adnIndex)).setSne(sne);
                } catch (IndexOutOfBoundsException e) {
                    Rlog.e(LOG_TAG, "updatePhoneAdnRecordWithSneByIndex: mPhoneBookRecords IndexOutOfBoundsException size() is " + this.mPhoneBookRecords.size() + "index is " + adnIndex);
                }
            }
        }
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        Object obj;
        int i;
        CommandException e;
        int[] userData;
        switch (msg.what) {
            case 1:
                logi("handleMessage: EVENT_PBR_LOAD_DONE");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    createPbrFile((ArrayList) ar.result);
                } else {
                    this.mIsPbrPresent = Boolean.valueOf(false);
                    Rlog.d(LOG_TAG, "UsimPhoneBookManager, get PBR with exception:" + ar.exception);
                }
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            case 2:
                logi("Loading USIM ADN records done");
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null || this.mPhoneBookRecords == null) {
                    Rlog.w(LOG_TAG, "Loading USIM ADN records fail.");
                } else if (!CsimPhbStorageInfo.hasModemPhbEnhanceCapability(this.mFh) && this.mPhoneBookRecords.size() > 0 && ar.result != null) {
                    ArrayList<AdnRecord> adnList = changeAdnRecordNumber(this.mPhoneBookRecords.size(), (ArrayList) ar.result);
                    this.mOPPOEFRecNum.add(Integer.valueOf(adnList.size()));
                    this.mPhoneBookRecords.addAll(adnList);
                    CsimPhbStorageInfo.checkPhbStorage(adnList);
                } else if (ar.result != null) {
                    this.mOPPOEFRecNum.add(Integer.valueOf(((ArrayList) ar.result).size()));
                    this.mPhoneBookRecords.addAll((ArrayList) ar.result);
                    if (!CsimPhbStorageInfo.hasModemPhbEnhanceCapability(this.mFh)) {
                        CsimPhbStorageInfo.checkPhbStorage((ArrayList) ar.result);
                    }
                    log("Loading USIM ADN records " + this.mPhoneBookRecords.size());
                } else {
                    log("Loading USIM ADN records ar.result:" + ar.result);
                }
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            case 3:
                logi("Loading USIM IAP records done");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mIapFileRecord = (ArrayList) ar.result;
                }
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            case 4:
                logi("Loading USIM Email records done");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mEmailFileRecord = (ArrayList) ar.result;
                }
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            case 5:
                ar = (AsyncResult) msg.obj;
                logi("EVENT_AAS_LOAD_DONE done pbr " + msg.arg1);
                if (ar.exception == null) {
                    ArrayList<byte[]> aasFileRecords = ar.result;
                    if (aasFileRecords != null) {
                        int size = aasFileRecords.size();
                        ArrayList<String> list = new ArrayList();
                        for (i = 0; i < size; i++) {
                            byte[] aas = (byte[]) aasFileRecords.get(i);
                            if (aas == null) {
                                list.add(null);
                            } else {
                                String aasAlphaTag = IccUtils.adnStringFieldToString(aas, 0, aas.length);
                                log("AAS[" + i + "]=" + aasAlphaTag + ",byte=" + IccUtils.bytesToHexString(aas));
                                list.add(aasAlphaTag);
                            }
                        }
                        this.mAasForAnr = list;
                    }
                }
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            case 6:
                logi("Load UPB GAS done");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    String[] gasList = (String[]) ar.result;
                    if (gasList != null && gasList.length > 0) {
                        this.mGasForGrp = new ArrayList();
                        for (i = 0; i < gasList.length; i++) {
                            String gas = decodeGas(gasList[i]);
                            this.mGasForGrp.add(new UsimGroup(i + 1, gas));
                            log("Load UPB GAS done i is " + i + ", gas is " + gas);
                        }
                        break;
                    }
                }
                obj = this.mGasLock;
                synchronized (obj) {
                    this.mGasLock.notify();
                    break;
                }
            case 7:
                logi("Updating USIM IAP records done");
                if (((AsyncResult) msg.obj).exception == null) {
                    log("Updating USIM IAP records successfully!");
                    return;
                }
                return;
            case 8:
                logi("Updating USIM Email records done");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    log("Updating USIM Email records successfully!");
                    this.mRefreshEmailInfo = true;
                } else {
                    Rlog.e(LOG_TAG, "EVENT_EMAIL_UPDATE_DONE exception", ar.exception);
                }
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            case 9:
                logi("Updating USIM ANR records done");
                ar = (AsyncResult) msg.obj;
                IccIoResult res = ar.result;
                if (ar.exception != null) {
                    Rlog.e(LOG_TAG, "EVENT_ANR_UPDATE_DONE exception", ar.exception);
                } else if (res == null) {
                    this.mRefreshAnrInfo = true;
                } else if (res.getException() == null) {
                    log("Updating USIM ANR records successfully!");
                    this.mRefreshAnrInfo = true;
                }
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            case 10:
                logi("EVENT_AAS_UPDATE_DONE done.");
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            case 11:
                logi("update UPB SNE done");
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    Rlog.e(LOG_TAG, "EVENT_SNE_UPDATE_DONE exception", ar.exception);
                    e = (CommandException) ar.exception;
                    if (e.getCommandError() == Error.TEXT_STRING_TOO_LONG || e.getCommandError() == Error.OEM_ERROR_2) {
                        this.mResult = -40;
                    } else {
                        if (e.getCommandError() == Error.SIM_MEM_FULL || e.getCommandError() == Error.OEM_ERROR_3) {
                            this.mResult = -30;
                        } else {
                            this.mResult = -50;
                        }
                    }
                } else {
                    this.mResult = 0;
                }
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            case 12:
                logi("update UPB GRP done");
                if (((AsyncResult) msg.obj).exception == null) {
                    this.mResult = 0;
                } else {
                    this.mResult = -1;
                }
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            case 13:
                logi("update UPB GAS done");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mResult = 0;
                } else {
                    e = ar.exception;
                    if (e.getCommandError() == Error.TEXT_STRING_TOO_LONG || e.getCommandError() == Error.OEM_ERROR_2) {
                        this.mResult = -10;
                    } else {
                        if (e.getCommandError() == Error.SIM_MEM_FULL || e.getCommandError() == Error.OEM_ERROR_3) {
                            this.mResult = -20;
                        } else {
                            this.mResult = -1;
                        }
                    }
                }
                logi("update UPB GAS done mResult is " + this.mResult);
                obj = this.mGasLock;
                synchronized (obj) {
                    this.mGasLock.notify();
                    break;
                }
            case 14:
                ar = (AsyncResult) msg.obj;
                userData = (int[]) ar.userObj;
                IccIoResult re = ar.result;
                if (!(re == null || this.mIapFileList == null || re.getException() != null)) {
                    log("Loading USIM Iap record done result is " + IccUtils.bytesToHexString(re.payload));
                    try {
                        ArrayList<byte[]> iapList = (ArrayList) this.mIapFileList.get(userData[0]);
                        if (iapList.size() > 0) {
                            iapList.set(userData[1], re.payload);
                        } else {
                            Rlog.w(LOG_TAG, "Warning: IAP size is 0");
                        }
                    } catch (IndexOutOfBoundsException e2) {
                        Rlog.e(LOG_TAG, "Index out of bounds.");
                    }
                }
                this.mReadingIapNum.decrementAndGet();
                log("haman, mReadingIapNum when load done after minus: " + this.mReadingIapNum.get() + ",mNeedNotify " + this.mNeedNotify.get() + ", Iap pbr:" + userData[0] + ", adn i:" + userData[1]);
                if (this.mReadingIapNum.get() == 0) {
                    if (this.mNeedNotify.get()) {
                        this.mNeedNotify.set(false);
                        synchronized (this.mLock) {
                            this.mLock.notify();
                        }
                    }
                    logi("EVENT_IAP_RECORD_LOAD_DONE end mLock.notify");
                    return;
                }
                return;
            case 15:
                ar = (AsyncResult) msg.obj;
                userData = (int[]) ar.userObj;
                IccIoResult em = ar.result;
                log("Loading USIM email record done email index:" + userData[0] + ", adn i:" + userData[1]);
                if (em != null && em.getException() == null) {
                    log("Loading USIM Email record done result is " + IccUtils.bytesToHexString(em.payload));
                    updatePhoneAdnRecordWithEmailByIndex(userData[0], userData[1], em.payload);
                }
                this.mReadingEmailNum.decrementAndGet();
                log("haman, mReadingEmailNum when load done after minus: " + this.mReadingEmailNum.get() + ", mNeedNotify:" + this.mNeedNotify.get());
                if (this.mReadingEmailNum.get() == 0) {
                    if (this.mNeedNotify.get()) {
                        this.mNeedNotify.set(false);
                        synchronized (this.mLock) {
                            this.mLock.notify();
                        }
                    }
                    logi("EVENT_EMAIL_RECORD_LOAD_DONE end mLock.notify");
                    return;
                }
                return;
            case 16:
                ar = (AsyncResult) msg.obj;
                userData = (int[]) ar.userObj;
                IccIoResult result = ar.result;
                if (result != null && result.getException() == null) {
                    updatePhoneAdnRecordWithAnrByIndex(userData[0], userData[1], userData[2], result.payload);
                }
                this.mReadingAnrNum.decrementAndGet();
                log("haman, mReadingAnrNum when load done after minus: " + this.mReadingAnrNum.get() + ", mNeedNotify:" + this.mNeedNotify.get());
                if (this.mReadingAnrNum.get() == 0) {
                    if (this.mNeedNotify.get()) {
                        this.mNeedNotify.set(false);
                        synchronized (this.mLock) {
                            this.mLock.notify();
                        }
                    }
                    logi("EVENT_ANR_RECORD_LOAD_DONE end mLock.notify");
                    return;
                }
                return;
            case 17:
                ar = (AsyncResult) msg.obj;
                userData = (int[]) ar.userObj;
                if (ar.result != null) {
                    int[] grpIds = (int[]) ar.result;
                    if (grpIds.length > 0) {
                        updatePhoneAdnRecordWithGrpByIndex(userData[0], userData[1], grpIds);
                    }
                }
                this.mReadingGrpNum.decrementAndGet();
                log("haman, mReadingGrpNum when load done after minus: " + this.mReadingGrpNum.get() + ",mNeedNotify:" + this.mNeedNotify.get());
                if (this.mReadingGrpNum.get() == 0) {
                    if (this.mNeedNotify.get()) {
                        this.mNeedNotify.set(false);
                        synchronized (this.mLock) {
                            this.mLock.notify();
                        }
                    }
                    logi("EVENT_GRP_RECORD_LOAD_DONE end mLock.notify");
                    return;
                }
                return;
            case 18:
                logi("Loading USIM SNE record done");
                ar = (AsyncResult) msg.obj;
                userData = (int[]) ar.userObj;
                IccIoResult r = ar.result;
                if (r != null && r.getException() == null) {
                    log("Loading USIM SNE record done result is " + IccUtils.bytesToHexString(r.payload));
                    updatePhoneAdnRecordWithSneByIndex(userData[0], userData[1], r.payload);
                }
                this.mReadingSneNum.decrementAndGet();
                log("haman, mReadingSneNum when load done after minus: " + this.mReadingSneNum.get() + ",mNeedNotify:" + this.mNeedNotify.get());
                if (this.mReadingSneNum.get() == 0) {
                    if (this.mNeedNotify.get()) {
                        this.mNeedNotify.set(false);
                        synchronized (this.mLock) {
                            this.mLock.notify();
                        }
                    }
                    logi("EVENT_SNE_RECORD_LOAD_DONE end mLock.notify");
                    return;
                }
                return;
            case 19:
                logi("Query UPB capability done");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mUpbCap = (int[]) ar.result;
                }
                obj = this.mUPBCapabilityLock;
                synchronized (obj) {
                    this.mUPBCapabilityLock.notify();
                    break;
                }
            case 20:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.efData = (EFResponseData) ar.result;
                } else {
                    Rlog.w(LOG_TAG, "Select EF file fail" + ar.exception);
                }
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            case 21:
                logi("EVENT_QUERY_PHB_ADN_INFO");
                ar = msg.obj;
                if (ar.exception == null) {
                    int[] info = (int[]) ar.result;
                    if (info != null) {
                        this.mAdnRecordSize = new int[4];
                        this.mAdnRecordSize[0] = info[0];
                        this.mAdnRecordSize[1] = info[1];
                        this.mAdnRecordSize[2] = info[2];
                        this.mAdnRecordSize[3] = info[3];
                        log("recordSize[0]=" + this.mAdnRecordSize[0] + ",recordSize[1]=" + this.mAdnRecordSize[1] + ",recordSize[2]=" + this.mAdnRecordSize[2] + ",recordSize[3]=" + this.mAdnRecordSize[3]);
                    } else {
                        this.mAdnRecordSize = new int[4];
                        this.mAdnRecordSize[0] = 0;
                        this.mAdnRecordSize[1] = 0;
                        this.mAdnRecordSize[2] = 0;
                        this.mAdnRecordSize[3] = 0;
                    }
                }
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            case 22:
                ar = (AsyncResult) msg.obj;
                userData = (int[]) ar.userObj;
                String emailResult = ar.result;
                if (emailResult != null && ar.exception == null) {
                    log("Loading USIM Email record done result is " + emailResult);
                    updatePhoneAdnRecordWithEmailByIndexOptmz(userData[0], userData[1], emailResult);
                }
                this.mReadingEmailNum.decrementAndGet();
                log("haman, mReadingEmailNum when load done after minus: " + this.mReadingEmailNum.get() + ", mNeedNotify:" + this.mNeedNotify.get() + ", email index:" + userData[0] + ", adn i:" + userData[1]);
                if (this.mReadingEmailNum.get() == 0) {
                    if (this.mNeedNotify.get()) {
                        this.mNeedNotify.set(false);
                        synchronized (this.mLock) {
                            this.mLock.notify();
                        }
                    }
                    logi("EVENT_EMAIL_RECORD_LOAD_OPTMZ_DONE end mLock.notify");
                    return;
                }
                return;
            case 23:
                ar = (AsyncResult) msg.obj;
                userData = (int[]) ar.userObj;
                PhbEntry[] anrResult = ar.result;
                if (anrResult != null && ar.exception == null) {
                    log("Loading USIM Anr record done result is " + anrResult[0]);
                    updatePhoneAdnRecordWithAnrByIndexOptmz(userData[0], userData[1], userData[2], anrResult[0]);
                }
                this.mReadingAnrNum.decrementAndGet();
                log("haman, mReadingAnrNum when load done after minus: " + this.mReadingAnrNum.get() + ", mNeedNotify:" + this.mNeedNotify.get() + ", anr index:" + userData[2] + ", adn i:" + userData[1]);
                if (this.mReadingAnrNum.get() == 0) {
                    if (this.mNeedNotify.get()) {
                        this.mNeedNotify.set(false);
                        synchronized (this.mLock) {
                            this.mLock.notify();
                        }
                    }
                    logi("EVENT_ANR_RECORD_LOAD_OPTMZ_DONE end mLock.notify");
                    return;
                }
                return;
            case 24:
                ar = (AsyncResult) msg.obj;
                userData = (int[]) ar.userObj;
                String sneResult = ar.result;
                if (sneResult != null && ar.exception == null) {
                    sneResult = decodeGas(sneResult);
                    log("Loading USIM Sne record done result is " + sneResult);
                    updatePhoneAdnRecordWithSneByIndexOptmz(userData[1], sneResult);
                }
                this.mReadingSneNum.decrementAndGet();
                log("haman, mReadingSneNum when load done after minus: " + this.mReadingSneNum.get() + ", mNeedNotify:" + this.mNeedNotify.get() + ", sne index:" + userData[0] + ", adn i:" + userData[1]);
                if (this.mReadingSneNum.get() == 0) {
                    if (this.mNeedNotify.get()) {
                        this.mNeedNotify.set(false);
                        synchronized (this.mLock) {
                            this.mLock.notify();
                        }
                    }
                    logi("EVENT_SNE_RECORD_LOAD_OPTMZ_DONE end mLock.notify");
                    return;
                }
                return;
            case 25:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mEmailInfo = (int[]) ar.result;
                    if (this.mEmailInfo == null) {
                        log("mEmailInfo Null!");
                    } else {
                        logi("mEmailInfo = " + this.mEmailInfo[0] + " " + this.mEmailInfo[1] + " " + this.mEmailInfo[2]);
                    }
                }
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            case 26:
                ar = (AsyncResult) msg.obj;
                Object tmpAnrInfo = (int[]) ar.result;
                if (ar.exception == null) {
                    if (tmpAnrInfo == null) {
                        log("tmpAnrInfo Null!");
                    } else {
                        logi("tmpAnrInfo = " + tmpAnrInfo[0] + " " + tmpAnrInfo[1] + " " + tmpAnrInfo[2]);
                        if (this.mAnrInfo == null) {
                            this.mAnrInfo = new ArrayList();
                        } else if (this.mAnrInfo.size() > 0) {
                            this.mAnrInfo.clear();
                        }
                        this.mAnrInfo.add(tmpAnrInfo);
                    }
                }
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            case 27:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mSneInfo = (int[]) ar.result;
                    if (this.mSneInfo == null) {
                        log("mSneInfo Null!");
                    } else {
                        logi("mSneInfo = " + this.mSneInfo[0] + " " + this.mSneInfo[1] + " " + this.mSneInfo[2]);
                    }
                }
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            case 28:
                logi("Load UPB AAS done");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    String[] aasList = ar.result;
                    if (aasList != null && aasList.length > 0) {
                        this.mAasForAnr = new ArrayList();
                        for (i = 0; i < aasList.length; i++) {
                            String aas2 = decodeGas(aasList[i]);
                            this.mAasForAnr.add(aas2);
                            log("Load UPB AAS done i is " + i + ", aas is " + aas2);
                        }
                        break;
                    }
                }
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            case 1000:
                ar = (AsyncResult) msg.obj;
                int efid = msg.arg1;
                if (ar.exception == null) {
                    Object recordSize = (int[]) ar.result;
                    if (recordSize.length == 3) {
                        if (this.mRecordSize == null) {
                            this.mRecordSize = new SparseArray();
                        }
                        this.mRecordSize.put(efid, recordSize);
                    } else {
                        Rlog.e(LOG_TAG, "get wrong record size format" + ar.exception);
                    }
                } else {
                    Rlog.e(LOG_TAG, "get EF record size failed" + ar.exception);
                }
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            case 1001:
                ar = (AsyncResult) msg.obj;
                logi("EVENT_EXT1_LOAD_DONE done pbr " + msg.arg1);
                if (ar.exception == null) {
                    ArrayList<byte[]> record = ar.result;
                    if (record != null) {
                        log("EVENT_EXT1_LOAD_DONE done size " + record.size());
                        if (this.mExt1FileList == null) {
                            this.mExt1FileList = new ArrayList();
                        }
                        this.mExt1FileList.add(record);
                    }
                }
                obj = this.mLock;
                synchronized (obj) {
                    this.mLock.notify();
                    break;
                }
            default:
                Rlog.e(LOG_TAG, "UnRecognized Message : " + msg.what);
                return;
        }
    }

    private void queryUpbCapablityAndWait() {
        logi("queryUpbCapablityAndWait begin");
        synchronized (this.mUPBCapabilityLock) {
            for (int i = 0; i < 8; i++) {
                this.mUpbCap[i] = -1;
            }
            if (checkIsPhbReady()) {
                this.mCi.queryUPBCapability(obtainMessage(19));
                try {
                    this.mUPBCapabilityLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in queryUpbCapablityAndWait");
                }
            }
        }
        logi("queryUpbCapablityAndWait done:N_Anr is " + this.mUpbCap[0] + ", N_Email is " + this.mUpbCap[1] + ",N_Sne is " + this.mUpbCap[2] + ",N_Aas is " + this.mUpbCap[3] + ", L_Aas is " + this.mUpbCap[4] + ",N_Gas is " + this.mUpbCap[5] + ",L_Gas is " + this.mUpbCap[6] + ", N_Grp is " + this.mUpbCap[7]);
        return;
    }

    private void readGasListAndWait() {
        logi("readGasListAndWait begin");
        synchronized (this.mGasLock) {
            if (this.mUpbCap[5] <= 0) {
                log("readGasListAndWait no need to read. return");
                return;
            }
            this.mCi.readUPBGasList(1, this.mUpbCap[5], obtainMessage(6));
            try {
                this.mGasLock.wait();
            } catch (InterruptedException e) {
                Rlog.e(LOG_TAG, "Interrupted Exception in readGasListAndWait");
            }
        }
    }

    private void updatePhoneAdnRecordWithAnrByIndex(int recId, int adnIndex, int anrIndex, byte[] anrRecData) {
        log("updatePhoneAdnRecordWithAnrByIndex the " + adnIndex + "th anr record is " + IccUtils.bytesToHexString(anrRecData));
        int anrRecLength = anrRecData[1];
        int anrAas = anrRecData[0];
        if (anrRecLength > 0 && anrRecLength <= 11) {
            String anr = PhoneNumberUtils.calledPartyBCDToString(anrRecData, 2, anrRecData[1]);
            if (!(anr == null || anr.equals(UsimPBMemInfo.STRING_NOT_SET))) {
                String str = null;
                if (!(anrAas <= 0 || anrAas == 255 || this.mAasForAnr == null)) {
                    ArrayList<String> aasList = this.mAasForAnr;
                    if (aasList != null && anrAas <= aasList.size()) {
                        str = (String) aasList.get(anrAas - 1);
                    }
                }
                logi(" updatePhoneAdnRecordWithAnrByIndex " + adnIndex + " th anr is " + anr + " the anrIndex is " + anrIndex);
                try {
                    AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(adnIndex);
                    rec.setAnr(anr, anrIndex);
                    if (str != null && str.length() > 0) {
                        rec.setAasIndex(anrAas);
                    }
                    this.mPhoneBookRecords.set(adnIndex, rec);
                } catch (IndexOutOfBoundsException e) {
                    Rlog.e(LOG_TAG, "updatePhoneAdnRecordWithAnrByIndex: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is " + this.mPhoneBookRecords.size() + "index is " + adnIndex);
                }
            }
        }
    }

    public ArrayList<UsimGroup> getUsimGroups() {
        logi("getUsimGroups ");
        synchronized (this.mGasLock) {
            if (this.mGasForGrp.isEmpty()) {
                queryUpbCapablityAndWait();
                readGasListAndWait();
                return this.mGasForGrp;
            }
            ArrayList<UsimGroup> arrayList = this.mGasForGrp;
            return arrayList;
        }
    }

    public String getUsimGroupById(int nGasId) {
        String grpName = null;
        logi("getUsimGroupById nGasId is " + nGasId);
        if (this.mGasForGrp != null && nGasId <= this.mGasForGrp.size()) {
            UsimGroup uGas = (UsimGroup) this.mGasForGrp.get(nGasId - 1);
            if (uGas != null) {
                grpName = uGas.getAlphaTag();
                log("getUsimGroupById index is " + uGas.getRecordIndex() + ", name is " + grpName);
            }
        }
        logi("getUsimGroupById grpName is " + grpName);
        return grpName;
    }

    public synchronized boolean removeUsimGroupById(int nGasId) {
        boolean ret;
        ret = false;
        logi("removeUsimGroupById nGasId is " + nGasId);
        synchronized (this.mGasLock) {
            if (this.mGasForGrp == null || nGasId > this.mGasForGrp.size()) {
                Rlog.e(LOG_TAG, "removeUsimGroupById fail ");
            } else {
                UsimGroup uGas = (UsimGroup) this.mGasForGrp.get(nGasId - 1);
                if (uGas != null) {
                    log(" removeUsimGroupById index is " + uGas.getRecordIndex());
                }
                if (uGas == null || uGas.getAlphaTag() == null) {
                    Rlog.w(LOG_TAG, "removeUsimGroupById fail: this gas doesn't exist ");
                } else {
                    this.mCi.deleteUPBEntry(4, 0, nGasId, obtainMessage(13));
                    try {
                        this.mGasLock.wait();
                    } catch (InterruptedException e) {
                        Rlog.e(LOG_TAG, "Interrupted Exception in removeUsimGroupById");
                    }
                    if (this.mResult == 0) {
                        ret = true;
                        uGas.setAlphaTag(null);
                        this.mGasForGrp.set(nGasId - 1, uGas);
                    }
                }
            }
        }
        logi("removeUsimGroupById result is " + ret);
        return ret;
    }

    private String decodeGas(String srcGas) {
        String str;
        StringBuilder append = new StringBuilder().append("[decodeGas] gas string is ");
        if (srcGas == null) {
            str = "null";
        } else {
            str = srcGas;
        }
        log(append.append(str).toString());
        if (srcGas == null || srcGas.length() % 2 != 0) {
            return null;
        }
        String str2 = null;
        try {
            byte[] ba = IccUtils.hexStringToBytes(srcGas);
            if (ba == null) {
                Rlog.w(LOG_TAG, "gas string is null");
                return null;
            }
            str2 = new String(ba, 0, srcGas.length() / 2, "utf-16be");
            return str2;
        } catch (UnsupportedEncodingException ex) {
            Rlog.e(LOG_TAG, "[decodeGas] implausible UnsupportedEncodingException", ex);
        } catch (RuntimeException ex2) {
            Rlog.e(LOG_TAG, "[decodeGas] RuntimeException", ex2);
        }
    }

    private String encodeToUcs2(String input) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            String hexInt = Integer.toHexString(input.charAt(i));
            for (int j = 0; j < 4 - hexInt.length(); j++) {
                output.append("0");
            }
            output.append(hexInt);
        }
        return output.toString();
    }

    public synchronized int insertUsimGroup(String grpName) {
        int index = -1;
        logi("insertUsimGroup grpName is " + grpName);
        synchronized (this.mGasLock) {
            if (this.mGasForGrp == null || this.mGasForGrp.size() == 0) {
                Rlog.w(LOG_TAG, "insertUsimGroup fail ");
            } else {
                UsimGroup gasEntry = null;
                int i = 0;
                while (i < this.mGasForGrp.size()) {
                    gasEntry = (UsimGroup) this.mGasForGrp.get(i);
                    if (gasEntry != null && gasEntry.getAlphaTag() == null) {
                        index = gasEntry.getRecordIndex();
                        log("insertUsimGroup index is " + index);
                        break;
                    }
                    i++;
                }
                if (index < 0) {
                    Rlog.w(LOG_TAG, "insertUsimGroup fail: gas file is full.");
                    return -20;
                }
                this.mCi.editUPBEntry(4, 0, index, encodeToUcs2(grpName), null, obtainMessage(13));
                try {
                    this.mGasLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in insertUsimGroup");
                }
                if (this.mResult < 0) {
                    Rlog.e(LOG_TAG, "result is negative. insertUsimGroup");
                    int i2 = this.mResult;
                    return i2;
                }
                gasEntry.setAlphaTag(grpName);
                this.mGasForGrp.set(i, gasEntry);
            }
            return index;
        }
    }

    public synchronized int updateUsimGroup(int nGasId, String grpName) {
        int ret;
        logi("updateUsimGroup nGasId is " + nGasId);
        synchronized (this.mGasLock) {
            this.mResult = -1;
            if (this.mGasForGrp == null || nGasId > this.mGasForGrp.size()) {
                Rlog.w(LOG_TAG, "updateUsimGroup fail ");
            } else if (grpName != null) {
                int i = nGasId;
                this.mCi.editUPBEntry(4, 0, i, encodeToUcs2(grpName), null, obtainMessage(13));
                try {
                    this.mGasLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in updateUsimGroup");
                }
            }
            if (this.mResult == 0) {
                ret = nGasId;
                UsimGroup uGasEntry = (UsimGroup) this.mGasForGrp.get(nGasId - 1);
                if (uGasEntry != null) {
                    log("updateUsimGroup index is " + uGasEntry.getRecordIndex());
                    uGasEntry.setAlphaTag(grpName);
                } else {
                    Rlog.w(LOG_TAG, "updateUsimGroup the entry doesn't exist ");
                }
            } else {
                ret = this.mResult;
            }
        }
        return ret;
    }

    public boolean addContactToGroup(int adnIndex, int grpIndex) {
        boolean ret = false;
        logi("addContactToGroup adnIndex is " + adnIndex + " to grp " + grpIndex);
        if (this.mPhoneBookRecords == null || adnIndex <= 0 || adnIndex > this.mPhoneBookRecords.size()) {
            Rlog.e(LOG_TAG, "addContactToGroup no records or invalid index.");
            return false;
        }
        synchronized (this.mLock) {
            try {
                AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(adnIndex - 1);
                if (rec != null) {
                    int i;
                    log(" addContactToGroup the adn index is " + rec.getRecId() + " old grpList is " + rec.getGrpIds());
                    String grpList = rec.getGrpIds();
                    boolean bExist = false;
                    int nOrder = -1;
                    int grpCount = this.mUpbCap[7];
                    int grpMaxCount = this.mUpbCap[7] > this.mUpbCap[5] ? this.mUpbCap[5] : this.mUpbCap[7];
                    int[] grpIdArray = new int[grpCount];
                    for (i = 0; i < grpCount; i++) {
                        grpIdArray[i] = 0;
                    }
                    if (grpList != null) {
                        String[] grpIds = rec.getGrpIds().split(",");
                        i = 0;
                        while (i < grpMaxCount) {
                            grpIdArray[i] = Integer.parseInt(grpIds[i]);
                            if (grpIndex == grpIdArray[i]) {
                                bExist = true;
                                log(" addContactToGroup the adn is already in the group. i is " + i);
                                break;
                            }
                            if (nOrder < 0) {
                                if (grpIdArray[i] == 0 || grpIdArray[i] == 255) {
                                    nOrder = i;
                                    log(" addContactToGroup found an unsed position in the group list. i is " + i);
                                }
                            }
                            i++;
                        }
                    } else {
                        nOrder = 0;
                    }
                    if (!bExist && nOrder >= 0) {
                        grpIdArray[nOrder] = grpIndex;
                        this.mCi.writeUPBGrpEntry(adnIndex, grpIdArray, obtainMessage(12));
                        try {
                            this.mLock.wait();
                        } catch (InterruptedException e) {
                            Rlog.e(LOG_TAG, "Interrupted Exception in addContactToGroup");
                        }
                        if (this.mResult == 0) {
                            ret = true;
                            updatePhoneAdnRecordWithGrpByIndex(adnIndex - 1, adnIndex, grpIdArray);
                            logi(" addContactToGroup the adn index is " + rec.getRecId());
                            this.mResult = -1;
                        }
                    }
                }
            } catch (IndexOutOfBoundsException e2) {
                Rlog.e(LOG_TAG, "addContactToGroup: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is " + this.mPhoneBookRecords.size() + "index is " + (adnIndex - 1));
                return false;
            }
        }
        return ret;
    }

    public synchronized boolean removeContactFromGroup(int adnIndex, int grpIndex) {
        boolean ret = false;
        logi("removeContactFromGroup adnIndex is " + adnIndex + " to grp " + grpIndex);
        if (this.mPhoneBookRecords != null && adnIndex > 0) {
            if (adnIndex <= this.mPhoneBookRecords.size()) {
                synchronized (this.mLock) {
                    try {
                        AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(adnIndex - 1);
                        if (rec != null) {
                            String grpList = rec.getGrpIds();
                            if (grpList == null) {
                                Rlog.e(LOG_TAG, " the adn is not in any group. ");
                                return false;
                            }
                            String[] grpIds = grpList.split(",");
                            boolean bExist = false;
                            int nOrder = -1;
                            int[] grpIdArray = new int[grpIds.length];
                            for (int i = 0; i < grpIds.length; i++) {
                                grpIdArray[i] = Integer.parseInt(grpIds[i]);
                                if (grpIndex == grpIdArray[i]) {
                                    bExist = true;
                                    nOrder = i;
                                    log(" removeContactFromGroup the adn is in the group. i is " + i);
                                }
                            }
                            if (!bExist || nOrder < 0) {
                                Rlog.e(LOG_TAG, " removeContactFromGroup the adn is not in the group. ");
                            } else {
                                grpIdArray[nOrder] = 0;
                                this.mCi.writeUPBGrpEntry(adnIndex, grpIdArray, obtainMessage(12));
                                try {
                                    this.mLock.wait();
                                } catch (InterruptedException e) {
                                    Rlog.e(LOG_TAG, "Interrupted Exception in removeContactFromGroup");
                                }
                                if (this.mResult == 0) {
                                    ret = true;
                                    updatePhoneAdnRecordWithGrpByIndex(adnIndex - 1, adnIndex, grpIdArray);
                                    this.mResult = -1;
                                }
                            }
                        }
                        return ret;
                    } catch (IndexOutOfBoundsException e2) {
                        Rlog.e(LOG_TAG, "removeContactFromGroup: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is " + this.mPhoneBookRecords.size() + "index is " + (adnIndex - 1));
                        return false;
                    }
                }
            }
        }
        Rlog.e(LOG_TAG, "removeContactFromGroup no records or invalid index.");
        return false;
    }

    /* JADX WARNING: Missing block: B:34:0x0104, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean updateContactToGroups(int adnIndex, int[] grpIdList) {
        boolean ret = false;
        if (this.mPhoneBookRecords == null || adnIndex <= 0 || adnIndex > this.mPhoneBookRecords.size() || grpIdList == null) {
            Rlog.e(LOG_TAG, "updateContactToGroups no records or invalid index.");
            return false;
        }
        logi("updateContactToGroups grpIdList is " + adnIndex + " to grp list count " + grpIdList.length);
        synchronized (this.mLock) {
            AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(adnIndex - 1);
            if (rec != null) {
                log(" updateContactToGroups the adn index is " + rec.getRecId() + " old grpList is " + rec.getGrpIds());
                int grpCount = this.mUpbCap[7];
                if (grpIdList.length > grpCount) {
                    Rlog.e(LOG_TAG, "updateContactToGroups length of grpIdList > grpCount.");
                    return false;
                }
                int[] grpIdArray = new int[grpCount];
                for (int i = 0; i < grpCount; i++) {
                    int i2;
                    if (i < grpIdList.length) {
                        i2 = grpIdList[i];
                    } else {
                        boolean i22 = false;
                    }
                    grpIdArray[i] = i22;
                    log("updateContactToGroups i:" + i + ",grpIdArray[" + i + "]:" + grpIdArray[i]);
                }
                this.mCi.writeUPBGrpEntry(adnIndex, grpIdArray, obtainMessage(12));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in updateContactToGroups");
                }
                if (this.mResult == 0) {
                    ret = true;
                    updatePhoneAdnRecordWithGrpByIndex(adnIndex - 1, adnIndex, grpIdArray);
                    logi(" updateContactToGroups the adn index is " + rec.getRecId());
                    this.mResult = -1;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:77:0x01a7, code:
            return r14;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean moveContactFromGroupsToGroups(int adnIndex, int[] fromGrpIdList, int[] toGrpIdList) {
        boolean ret = false;
        if (this.mPhoneBookRecords == null || adnIndex <= 0 || adnIndex > this.mPhoneBookRecords.size()) {
            Rlog.e(LOG_TAG, "moveContactFromGroupsToGroups no records or invalid index.");
            return false;
        }
        synchronized (this.mLock) {
            AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(adnIndex - 1);
            if (rec != null) {
                Object obj;
                int i;
                int j;
                int grpCount = this.mUpbCap[7];
                int grpMaxCount = this.mUpbCap[7] > this.mUpbCap[5] ? this.mUpbCap[5] : this.mUpbCap[7];
                String grpIds = rec.getGrpIds();
                StringBuilder append = new StringBuilder().append(" moveContactFromGroupsToGroups the adn index is ").append(rec.getRecId()).append(" original grpIds is ").append(grpIds).append(", fromGrpIdList: ");
                if (fromGrpIdList == null) {
                    obj = "null";
                } else {
                    obj = fromGrpIdList;
                }
                append = append.append(obj).append(", toGrpIdList: ");
                if (toGrpIdList == null) {
                    obj = "null";
                } else {
                    obj = toGrpIdList;
                }
                logi(append.append(obj).toString());
                int[] grpIdIntArray = new int[grpCount];
                for (i = 0; i < grpCount; i++) {
                    grpIdIntArray[i] = 0;
                }
                if (grpIds != null) {
                    String[] grpIdStrArray = grpIds.split(",");
                    for (i = 0; i < grpMaxCount; i++) {
                        grpIdIntArray[i] = Integer.parseInt(grpIdStrArray[i]);
                    }
                }
                if (fromGrpIdList != null) {
                    for (int i2 : fromGrpIdList) {
                        for (j = 0; j < grpMaxCount; j++) {
                            if (grpIdIntArray[j] == i2) {
                                grpIdIntArray[j] = 0;
                            }
                        }
                    }
                }
                if (toGrpIdList != null) {
                    for (i = 0; i < toGrpIdList.length; i++) {
                        boolean bEmpty = false;
                        boolean bExist = false;
                        for (int k = 0; k < grpMaxCount; k++) {
                            if (grpIdIntArray[k] == toGrpIdList[i]) {
                                bExist = true;
                                break;
                            }
                        }
                        if (bExist) {
                            Rlog.w(LOG_TAG, "moveContactFromGroupsToGroups the adn isalready in the group.");
                        } else {
                            j = 0;
                            while (j < grpMaxCount) {
                                if (grpIdIntArray[j] == 0 || grpIdIntArray[j] == 255) {
                                    bEmpty = true;
                                    grpIdIntArray[j] = toGrpIdList[i];
                                    break;
                                }
                                j++;
                            }
                            if (!bEmpty) {
                                Rlog.e(LOG_TAG, "moveContactFromGroupsToGroups no empty to add.");
                                return false;
                            }
                        }
                    }
                }
                this.mCi.writeUPBGrpEntry(adnIndex, grpIdIntArray, obtainMessage(12));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in moveContactFromGroupsToGroups");
                }
                if (this.mResult == 0) {
                    ret = true;
                    updatePhoneAdnRecordWithGrpByIndex(adnIndex - 1, adnIndex, grpIdIntArray);
                    logi("moveContactFromGroupsToGroups the adn index is " + rec.getRecId());
                    this.mResult = -1;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:55:0x00fd, code:
            return r8;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean removeContactGroup(int adnIndex) {
        boolean ret = false;
        logi("removeContactsGroup adnIndex is " + adnIndex);
        if (this.mPhoneBookRecords == null || this.mPhoneBookRecords.isEmpty()) {
            return false;
        }
        synchronized (this.mLock) {
            try {
                AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(adnIndex - 1);
                if (rec == null) {
                    return false;
                }
                log("removeContactsGroup rec is " + rec);
                String grpList = rec.getGrpIds();
                if (grpList == null) {
                    return false;
                }
                int i;
                String[] grpIds = grpList.split(",");
                boolean hasGroup = false;
                for (String parseInt : grpIds) {
                    int value = Integer.parseInt(parseInt);
                    if (value > 0 && value < 255) {
                        hasGroup = true;
                        break;
                    }
                }
                if (hasGroup) {
                    this.mCi.writeUPBGrpEntry(adnIndex, new int[0], obtainMessage(12));
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e) {
                        Rlog.e(LOG_TAG, "Interrupted Exception in removeContactGroup");
                    }
                    if (this.mResult == 0) {
                        ret = true;
                        int[] grpIdArray = new int[grpIds.length];
                        for (i = 0; i < grpIds.length; i++) {
                            grpIdArray[i] = 0;
                        }
                        updatePhoneAdnRecordWithGrpByIndex(adnIndex - 1, adnIndex, grpIdArray);
                        logi(" removeContactGroup the adn index is " + rec.getRecId());
                        this.mResult = -1;
                    }
                }
            } catch (IndexOutOfBoundsException e2) {
                Rlog.e(LOG_TAG, "removeContactGroup: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is " + this.mPhoneBookRecords.size() + "index is " + (adnIndex - 1));
                return false;
            }
        }
    }

    public int hasExistGroup(String grpName) {
        int grpId = -1;
        logi("hasExistGroup grpName is " + grpName);
        if (grpName == null) {
            return -1;
        }
        if (this.mGasForGrp != null && this.mGasForGrp.size() > 0) {
            for (int i = 0; i < this.mGasForGrp.size(); i++) {
                UsimGroup uGas = (UsimGroup) this.mGasForGrp.get(i);
                if (uGas != null && grpName.equals(uGas.getAlphaTag())) {
                    log("getUsimGroupById index is " + uGas.getRecordIndex() + ", name is " + grpName);
                    grpId = uGas.getRecordIndex();
                    break;
                }
            }
        }
        logi("hasExistGroup grpId is " + grpId);
        return grpId;
    }

    public int getUsimGrpMaxNameLen() {
        int ret;
        logi("getUsimGrpMaxNameLen begin");
        synchronized (this.mUPBCapabilityLock) {
            if (checkIsPhbReady()) {
                if (this.mUpbCap[6] < 0) {
                    queryUpbCapablityAndWait();
                }
                ret = this.mUpbCap[6];
            } else {
                ret = -1;
            }
            logi("getUsimGrpMaxNameLen done: L_Gas is " + ret);
        }
        return ret;
    }

    public int getUsimGrpMaxCount() {
        int ret;
        logi("getUsimGrpMaxCount begin");
        synchronized (this.mUPBCapabilityLock) {
            if (checkIsPhbReady()) {
                if (this.mUpbCap[5] < 0) {
                    queryUpbCapablityAndWait();
                }
                ret = this.mUpbCap[5];
            } else {
                ret = -1;
            }
            logi("getUsimGrpMaxCount done: N_Gas is " + ret);
        }
        return ret;
    }

    private void log(String msg) {
        if (DBG) {
            Rlog.d(LOG_TAG, msg + "(slot " + this.mSlotId + ")");
        }
    }

    private void logi(String msg) {
        Rlog.i(LOG_TAG, msg + "(slot " + this.mSlotId + ")");
    }

    /* JADX WARNING: Missing block: B:65:0x017a, code:
            if (r20 >= r17.mAnrInfo.size()) goto L_0x0129;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isAnrCapacityFree(String anr, int adnIndex, int anrIndex, AdnRecord oldAdn) {
        String oldAnr = null;
        if (oldAdn != null) {
            oldAnr = oldAdn.getAdditionalNumber(anrIndex);
        }
        if (anr != null) {
            if (!anr.equals(UsimPBMemInfo.STRING_NOT_SET) && anrIndex >= 0 && getUsimEfType(196) != 168 && (oldAnr == null || oldAnr.equals(UsimPBMemInfo.STRING_NOT_SET))) {
                if (CsimPhbStorageInfo.hasModemPhbEnhanceCapability(this.mFh)) {
                    synchronized (this.mLock) {
                        if (!(this.mRefreshAnrInfo || this.mAnrInfo == null)) {
                        }
                        this.mCi.queryUPBAvailable(0, anrIndex + 1, obtainMessage(26));
                        try {
                            this.mLock.wait();
                        } catch (InterruptedException e) {
                            Rlog.e(LOG_TAG, "Interrupted Exception in isAnrCapacityFree");
                        }
                        this.mRefreshAnrInfo = false;
                    }
                    if (this.mAnrInfo == null || this.mAnrInfo.get(anrIndex) == null || ((int[]) this.mAnrInfo.get(anrIndex))[1] <= 0) {
                        return false;
                    }
                    return true;
                }
                int pbrRecNum = (adnIndex - 1) / this.mAdnFileSize;
                int anrRecNum = (adnIndex - 1) % this.mAdnFileSize;
                try {
                    log("isAnrCapacityFree anr: " + anr);
                    if (this.mRecordSize == null || this.mRecordSize.size() == 0) {
                        log("isAnrCapacityFree: mAnrFileSize is empty");
                        return false;
                    }
                    File anrFile = (File) PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(pbrRecNum)).get((anrIndex * 256) + 196);
                    if (anrFile == null) {
                        return false;
                    }
                    int size = ((int[]) this.mRecordSize.get(anrFile.getEfid()))[2];
                    log("isAnrCapacityFree size: " + size);
                    if (size >= anrRecNum + 1) {
                        return true;
                    }
                    log("isAnrCapacityFree: anrRecNum out of size: " + anrRecNum);
                    return false;
                } catch (IndexOutOfBoundsException e2) {
                    Rlog.e(LOG_TAG, "isAnrCapacityFree Index out of bounds.");
                    return false;
                } catch (NullPointerException e3) {
                    Rlog.e(LOG_TAG, "isAnrCapacityFree exception:" + e3.toString());
                    return false;
                }
            }
        }
        return true;
    }

    public void updateAnrByAdnIndex(String anr, int adnIndex, int anrIndex) {
        int pbrRecNum = (adnIndex - 1) / this.mAdnFileSize;
        int anrRecNum = (adnIndex - 1) % this.mAdnFileSize;
        if (this.mPbrRecords != null) {
            SparseArray<File> fileIds = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(pbrRecNum));
            if (fileIds == null) {
                log("updateAnrByAdnIndex: No anr tag in pbr record 0");
                return;
            } else if (this.mPhoneBookRecords == null || this.mPhoneBookRecords.isEmpty()) {
                Rlog.w(LOG_TAG, "updateAnrByAdnIndex: mPhoneBookRecords is empty");
                return;
            } else {
                File anrFile = (File) fileIds.get((anrIndex * 256) + 196);
                if (anrFile == null) {
                    log("updateAnrByAdnIndex no efFile anrIndex: " + anrIndex);
                    return;
                }
                log("updateAnrByAdnIndex effile " + anrFile);
                int aas;
                if (CsimPhbStorageInfo.hasModemPhbEnhanceCapability(this.mFh)) {
                    try {
                        aas = ((AdnRecord) this.mPhoneBookRecords.get(adnIndex - 1)).getAasIndex();
                        Message msg = obtainMessage(9);
                        synchronized (this.mLock) {
                            if (anr != null) {
                                if (anr.length() != 0) {
                                    String[] param = buildAnrRecordOptmz(anr, aas);
                                    this.mCi.editUPBEntry(0, anrIndex + 1, adnIndex, param[0], param[1], param[2], msg);
                                    this.mLock.wait();
                                }
                            }
                            this.mCi.deleteUPBEntry(0, anrIndex + 1, adnIndex, msg);
                            try {
                                this.mLock.wait();
                            } catch (InterruptedException e) {
                                Rlog.e(LOG_TAG, "Interrupted Exception in updateAnrByAdnIndexOptmz");
                            }
                        }
                    } catch (IndexOutOfBoundsException e2) {
                        Rlog.e(LOG_TAG, "updateAnrByAdnIndexOptmz: mPhoneBookRecords IndexOutOfBoundsException size() is " + this.mPhoneBookRecords.size() + "index is " + (adnIndex - 1));
                        return;
                    }
                }
                int efid = anrFile.getEfid();
                log("updateAnrByAdnIndex recId: " + pbrRecNum + " EF_ANR id is " + Integer.toHexString(efid).toUpperCase());
                if (anrFile.getParentTag() == 169) {
                    updateType2Anr(anr, adnIndex, anrFile);
                    return;
                }
                try {
                    aas = ((AdnRecord) this.mPhoneBookRecords.get(adnIndex - 1)).getAasIndex();
                    byte[] data = buildAnrRecord(anr, this.mAnrRecordSize, aas);
                    if (data != null) {
                        this.mFh.updateEFLinearFixed(efid, anrRecNum + 1, data, null, obtainMessage(9));
                    }
                } catch (IndexOutOfBoundsException e3) {
                    Rlog.e(LOG_TAG, "updateAnrByAdnIndex: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is " + this.mPhoneBookRecords.size() + "index is " + (adnIndex - 1));
                    return;
                }
                return;
            }
        }
        return;
    }

    private int getEmailRecNum(String[] emails, int pbrRecNum, int nIapRecNum, byte[] iapRec, int tagNum) {
        boolean hasEmail = false;
        int emailType2Index = ((File) PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(pbrRecNum)).get(USIM_EFEMAIL_TAG)).getIndex();
        if (emails == null) {
            if (iapRec[emailType2Index] != (byte) -1 && iapRec[emailType2Index] > (byte) 0) {
                this.mEmailRecTable[iapRec[emailType2Index] - 1] = 0;
            }
            return -1;
        }
        int i = 0;
        while (i < emails.length) {
            if (emails[i] != null && !emails[i].equals(UsimPBMemInfo.STRING_NOT_SET)) {
                hasEmail = true;
                break;
            }
            i++;
        }
        if (hasEmail) {
            int recNum = iapRec[tagNum];
            log("getEmailRecNum recNum:" + recNum);
            if (recNum > this.mEmailFileSize || recNum >= 255 || recNum <= 0) {
                int nOffset = pbrRecNum * this.mEmailFileSize;
                for (i = nOffset; i < this.mEmailFileSize + nOffset; i++) {
                    log("updateEmailsByAdnIndex: mEmailRecTable[" + i + "] is " + this.mEmailRecTable[i]);
                    if (this.mEmailRecTable[i] == 0) {
                        recNum = (i + 1) - nOffset;
                        this.mEmailRecTable[i] = nIapRecNum;
                        break;
                    }
                }
            }
            if (recNum > this.mEmailFileSize) {
                recNum = 255;
            }
            if (recNum == -1) {
                return -2;
            }
            return recNum;
        }
        if (iapRec[emailType2Index] != (byte) -1 && iapRec[emailType2Index] > (byte) 0) {
            this.mEmailRecTable[iapRec[emailType2Index] - 1] = 0;
        }
        return -1;
    }

    public boolean checkEmailCapacityFree(int adnIndex, String[] emails, AdnRecord oldAdn) {
        boolean hasEmail = false;
        if (emails == null || getUsimEfType(USIM_EFEMAIL_TAG) == 168 || (oldAdn != null && oldAdn.getEmails() != null)) {
            return true;
        }
        int i = 0;
        while (i < emails.length) {
            if (emails[i] != null && !emails[i].equals(UsimPBMemInfo.STRING_NOT_SET)) {
                hasEmail = true;
                break;
            }
            i++;
        }
        if (!hasEmail) {
            return true;
        }
        if (CsimPhbStorageInfo.hasModemPhbEnhanceCapability(this.mFh)) {
            synchronized (this.mLock) {
                if (this.mRefreshEmailInfo || this.mEmailInfo == null) {
                    this.mCi.queryUPBAvailable(1, 1, obtainMessage(25));
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e) {
                        Rlog.e(LOG_TAG, "Interrupted Exception in checkEmailCapacityFreeOptmz");
                    }
                }
                this.mRefreshEmailInfo = false;
            }
            return this.mEmailInfo != null && this.mEmailInfo[1] > 0;
        } else {
            int nOffset = ((adnIndex - 1) / this.mAdnFileSize) * this.mEmailFileSize;
            for (i = nOffset; i < this.mEmailFileSize + nOffset; i++) {
                if (this.mEmailRecTable[i] == 0) {
                    return true;
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:8:0x001e, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkSneCapacityFree(int adnIndex, String sne, AdnRecord oldAdn) {
        String oldSne = null;
        if (oldAdn != null) {
            oldSne = oldAdn.getSne();
        }
        if (sne == null || sne.equals(UsimPBMemInfo.STRING_NOT_SET) || getUsimEfType(195) == 168 || ((oldSne != null && !oldSne.equals(UsimPBMemInfo.STRING_NOT_SET)) || !CsimPhbStorageInfo.hasModemPhbEnhanceCapability(this.mFh))) {
            return true;
        }
        synchronized (this.mLock) {
            if (this.mSneInfo == null) {
                this.mCi.queryUPBAvailable(2, 1, obtainMessage(27));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in checkSneCapacityFree");
                }
            }
        }
        return this.mSneInfo != null && this.mSneInfo[1] > 0;
    }

    public boolean checkEmailLength(String[] emails) {
        if (emails == null || emails[0] == null || this.mPbrRecords == null) {
            return true;
        }
        SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(0));
        if (files == null) {
            return true;
        }
        File emailFile = (File) files.get(USIM_EFEMAIL_TAG);
        if (emailFile == null) {
            return true;
        }
        int maxDataLength = (this.mEmailRecordSize == -1 || !(emailFile.getParentTag() == 169)) ? this.mEmailRecordSize : this.mEmailRecordSize - 2;
        byte[] eMailData = GsmAlphabet.stringToGsm8BitPacked(emails[0]);
        logi("checkEmailLength eMailData.length=" + eMailData.length + ", maxDataLength=" + maxDataLength);
        return maxDataLength == -1 || eMailData.length <= maxDataLength;
    }

    public int updateEmailsByAdnIndex(String[] emails, int adnIndex) {
        int pbrRecNum = (adnIndex - 1) / this.mAdnFileSize;
        int adnRecNum = (adnIndex - 1) % this.mAdnFileSize;
        if (this.mPbrRecords == null) {
            return 0;
        }
        SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(pbrRecNum));
        if (files == null || files.size() == 0) {
            return 0;
        }
        if (this.mPhoneBookRecords == null || this.mPhoneBookRecords.isEmpty()) {
            return 0;
        }
        File efFile = (File) files.get(USIM_EFEMAIL_TAG);
        if (efFile == null) {
            log("updateEmailsByAdnIndex: No email tag in pbr record 0");
            return 0;
        }
        int efid = efFile.getEfid();
        boolean emailType2 = efFile.getParentTag() == 169;
        int emailType2Index = efFile.getIndex();
        logi("updateEmailsByAdnIndex: pbrrecNum is " + pbrRecNum + " EF_EMAIL id is " + Integer.toHexString(efid).toUpperCase());
        if (CsimPhbStorageInfo.hasModemPhbEnhanceCapability(this.mFh)) {
            Message msg = obtainMessage(8);
            synchronized (this.mLock) {
                if (emails != null) {
                    if (emails.length != 0) {
                        if (!(emails[0] == null || emails[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
                            this.mCi.editUPBEntry(1, 1, adnIndex, encodeToUcs2(emails[0]), null, msg);
                            this.mLock.wait();
                        }
                    }
                }
                this.mCi.deleteUPBEntry(1, 1, adnIndex, msg);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in updateEmailsByAdnIndex");
                }
            }
            return 0;
        } else if (emailType2 && this.mIapFileList != null) {
            return updateType2Email(emails, adnIndex, efFile);
        } else {
            log("updateEmailsByAdnIndex file: " + efFile);
            String email = (emails == null || emails.length <= 0) ? null : emails[0];
            if (this.mEmailRecordSize <= 0) {
                return -50;
            }
            byte[] data = buildEmailRecord(email, adnIndex, this.mEmailRecordSize, emailType2);
            log("updateEmailsByAdnIndex build type1 email record:" + IccUtils.bytesToHexString(data));
            if (data == null) {
                return -40;
            }
            this.mFh.updateEFLinearFixed(efid, adnRecNum + 1, data, null, obtainMessage(8));
            return 0;
        }
    }

    private int updateType2Email(String[] emails, int adnIndex, File emailFile) {
        int pbrRecNum = (adnIndex - 1) / this.mAdnFileSize;
        int adnRecNum = (adnIndex - 1) % this.mAdnFileSize;
        int emailType2Index = emailFile.getIndex();
        int efid = emailFile.getEfid();
        try {
            ArrayList<byte[]> iapFile = (ArrayList) this.mIapFileList.get(pbrRecNum);
            if (iapFile.size() > 0) {
                byte[] iapRec = (byte[]) iapFile.get(adnRecNum);
                int recNum = getEmailRecNum(emails, pbrRecNum, adnRecNum + 1, iapRec, emailType2Index);
                log("updateEmailsByAdnIndex: Email recNum is " + recNum);
                if (-2 == recNum) {
                    return -30;
                }
                log("updateEmailsByAdnIndex: found Email recNum is " + recNum);
                iapRec[emailType2Index] = (byte) recNum;
                SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(pbrRecNum));
                if (files.get(193) != null) {
                    this.mFh.updateEFLinearFixed(((File) files.get(193)).getEfid(), adnRecNum + 1, iapRec, null, obtainMessage(7));
                    if (!(recNum == 255 || recNum == -1)) {
                        String eMailAd = null;
                        if (emails != null) {
                            try {
                                eMailAd = emails[0];
                            } catch (IndexOutOfBoundsException e) {
                                Rlog.e(LOG_TAG, "Error: updateEmailsByAdnIndex no email address, continuing");
                            }
                            if (this.mEmailRecordSize <= 0) {
                                return -50;
                            }
                            byte[] eMailRecData = buildEmailRecord(eMailAd, adnIndex, this.mEmailRecordSize, true);
                            if (eMailRecData == null) {
                                return -40;
                            }
                            this.mFh.updateEFLinearFixed(((File) files.get(USIM_EFEMAIL_TAG)).getEfid(), recNum, eMailRecData, null, obtainMessage(8));
                        }
                    }
                    return 0;
                }
                Rlog.e(LOG_TAG, "updateEmailsByAdnIndex Error: No IAP file!");
                return -50;
            }
            Rlog.w(LOG_TAG, "Warning: IAP size is 0");
            return -50;
        } catch (IndexOutOfBoundsException e2) {
            Rlog.e(LOG_TAG, "Index out of bounds.");
            return -50;
        }
    }

    private byte[] buildAnrRecord(String anr, int recordSize, int aas) {
        log("buildAnrRecord anr:" + anr + ",recordSize:" + recordSize + ",aas:" + aas);
        if (recordSize <= 0) {
            readAnrRecordSize();
        }
        log("buildAnrRecord recordSize:" + recordSize);
        byte[] anrString = new byte[recordSize];
        for (int i = 0; i < recordSize; i++) {
            anrString[i] = (byte) -1;
        }
        String updatedAnr = PhoneNumberUtils.convertPreDial(anr);
        if (TextUtils.isEmpty(updatedAnr)) {
            Rlog.w(LOG_TAG, "[buildAnrRecord] Empty dialing number");
            return anrString;
        } else if (updatedAnr.length() > 20) {
            Rlog.w(LOG_TAG, "[buildAnrRecord] Max length of dialing number is 20");
            return null;
        } else {
            byte[] bcdNumber = PhoneNumberUtils.numberToCalledPartyBCD(updatedAnr);
            if (bcdNumber != null) {
                anrString[0] = (byte) aas;
                System.arraycopy(bcdNumber, 0, anrString, 2, bcdNumber.length);
                anrString[1] = (byte) bcdNumber.length;
            }
            return anrString;
        }
    }

    private byte[] buildEmailRecord(String strEmail, int adnIndex, int recordSize, boolean emailType2) {
        byte[] eMailRecData = new byte[recordSize];
        for (int i = 0; i < recordSize; i++) {
            eMailRecData[i] = (byte) -1;
        }
        if (!(strEmail == null || strEmail.equals(UsimPBMemInfo.STRING_NOT_SET))) {
            int maxDataLength;
            byte[] eMailData = GsmAlphabet.stringToGsm8BitPacked(strEmail);
            if (this.mEmailRecordSize == -1 || !emailType2) {
                maxDataLength = eMailRecData.length;
            } else {
                maxDataLength = eMailRecData.length - 2;
            }
            log("buildEmailRecord eMailData.length=" + eMailData.length + ", maxDataLength=" + maxDataLength);
            if (eMailData.length > maxDataLength) {
                return null;
            }
            System.arraycopy(eMailData, 0, eMailRecData, 0, eMailData.length);
            log("buildEmailRecord eMailData=" + IccUtils.bytesToHexString(eMailData) + ", eMailRecData=" + IccUtils.bytesToHexString(eMailRecData));
            if (emailType2 && this.mPbrRecords != null) {
                int adnRecId = (adnIndex % this.mAdnFileSize) & 255;
                File adnFile = (File) PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get((adnIndex - 1) / this.mAdnFileSize)).get(192);
                eMailRecData[recordSize - 2] = (byte) adnFile.getSfi();
                eMailRecData[recordSize - 1] = (byte) adnRecId;
                log("buildEmailRecord x+1=" + adnFile.getSfi() + ", x+2=" + adnRecId);
            }
        }
        return eMailRecData;
    }

    public void updateUsimPhonebookRecordsList(int index, AdnRecord newAdn) {
        logi("updateUsimPhonebookRecordsList update the " + index + "th record.");
        if (index < this.mPhoneBookRecords.size()) {
            AdnRecord oldAdn = (AdnRecord) this.mPhoneBookRecords.get(index);
            if (!(oldAdn == null || oldAdn.getGrpIds() == null)) {
                newAdn.setGrpIds(oldAdn.getGrpIds());
            }
            this.mPhoneBookRecords.set(index, newAdn);
            this.mRefreshAdnInfo = true;
        }
    }

    private void updatePhoneAdnRecordWithGrpByIndex(int recIndex, int adnIndex, int[] grpIds) {
        log("updatePhoneAdnRecordWithGrpByIndex the " + recIndex + "th grp ");
        if (recIndex <= this.mPhoneBookRecords.size()) {
            int grpSize = grpIds.length;
            if (grpSize > 0) {
                try {
                    AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(recIndex);
                    log("updatePhoneAdnRecordWithGrpByIndex the adnIndex is " + adnIndex + "; the original index is " + rec.getRecId());
                    StringBuilder grpIdsSb = new StringBuilder();
                    for (int i = 0; i < grpSize - 1; i++) {
                        grpIdsSb.append(grpIds[i]);
                        grpIdsSb.append(",");
                    }
                    grpIdsSb.append(grpIds[grpSize - 1]);
                    rec.setGrpIds(grpIdsSb.toString());
                    log("updatePhoneAdnRecordWithGrpByIndex grpIds is " + grpIdsSb.toString());
                    this.mPhoneBookRecords.set(recIndex, rec);
                    log("updatePhoneAdnRecordWithGrpByIndex the rec:" + rec);
                } catch (IndexOutOfBoundsException e) {
                    Rlog.e(LOG_TAG, "updatePhoneAdnRecordWithGrpByIndex: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is " + this.mPhoneBookRecords.size() + "index is " + recIndex);
                }
            }
        }
    }

    private void readType1Ef(File file, int anrIndex) {
        log("readType1Ef:" + file);
        if (file.getParentTag() == 168) {
            int[] size;
            int pbrIndex = file.mPbrRecord;
            int numAdnRecs = this.mPhoneBookRecords.size();
            int nOffset = pbrIndex * this.mAdnFileSize;
            int nMax = nOffset + this.mAdnFileSize;
            if (numAdnRecs < nMax) {
                nMax = numAdnRecs;
            }
            if (this.mRecordSize == null || this.mRecordSize.get(file.getEfid()) == null) {
                size = readEFLinearRecordSize(file.getEfid());
            } else {
                size = (int[]) this.mRecordSize.get(file.getEfid());
            }
            if (size == null || size.length != 3) {
                Rlog.e(LOG_TAG, "readType1Ef: read record size error.");
                return;
            }
            int i;
            int recordSize = size[0];
            int tag = file.mTag % 256;
            int fileIndex = file.mTag / 256;
            log("readType1Ef: RecordSize = " + recordSize);
            if (tag == USIM_EFEMAIL_TAG) {
                i = nOffset;
                while (i < this.mEmailFileSize + nOffset) {
                    try {
                        this.mEmailRecTable[i] = 0;
                        i++;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Rlog.e(LOG_TAG, "init RecTable error " + e.getMessage());
                    }
                }
            }
            if (recordSize == 0) {
                Rlog.w(LOG_TAG, "readType1Ef: recordSize is 0. ");
                return;
            }
            int totalReadingNum = 0;
            i = nOffset;
            while (i < nMax) {
                try {
                    AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(i);
                    if (rec.getAlphaTag().length() > 0 || rec.getNumber().length() > 0) {
                        int[] data = new int[3];
                        data[0] = file.mPbrRecord;
                        data[1] = i;
                        data[2] = anrIndex;
                        int loadWhat = 0;
                        switch (tag) {
                            case 195:
                                loadWhat = 18;
                                this.mReadingSneNum.incrementAndGet();
                                break;
                            case 196:
                                loadWhat = 16;
                                this.mReadingAnrNum.addAndGet(1);
                                break;
                            case USIM_EFEMAIL_TAG /*202*/:
                                data[0] = ((i + 1) - nOffset) + (this.mEmailFileSize * nOffset);
                                loadWhat = 15;
                                this.mReadingEmailNum.incrementAndGet();
                                break;
                            default:
                                Rlog.e(LOG_TAG, "not support tag " + file.mTag);
                                break;
                        }
                        this.mFh.readEFLinearFixed(file.getEfid(), (i + 1) - nOffset, recordSize, obtainMessage(loadWhat, data));
                        totalReadingNum++;
                    }
                    i++;
                } catch (IndexOutOfBoundsException e2) {
                    Rlog.e(LOG_TAG, "readType1Ef: mPhoneBookRecords IndexOutOfBoundsException numAdnRecs is " + numAdnRecs + "index is " + i);
                }
            }
            switch (tag) {
                case 195:
                    if (this.mReadingSneNum.get() != 0) {
                        this.mNeedNotify.set(true);
                        break;
                    } else {
                        this.mNeedNotify.set(false);
                        return;
                    }
                case 196:
                    if (this.mReadingAnrNum.get() != 0) {
                        this.mNeedNotify.set(true);
                        break;
                    } else {
                        this.mNeedNotify.set(false);
                        return;
                    }
                case USIM_EFEMAIL_TAG /*202*/:
                    if (this.mReadingEmailNum.get() != 0) {
                        this.mNeedNotify.set(true);
                        break;
                    } else {
                        this.mNeedNotify.set(false);
                        return;
                    }
                default:
                    Rlog.e(LOG_TAG, "not support tag " + Integer.toHexString(file.mTag).toUpperCase());
                    break;
            }
            logi("readType1Ef before mLock.wait " + this.mNeedNotify.get() + " total:" + totalReadingNum);
            synchronized (this.mLock) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e3) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readType1Ef");
                }
            }
            logi("readType1Ef after mLock.wait " + this.mNeedNotify.get());
            return;
        }
        return;
    }

    private void readType2Ef(File file) {
        log("readType2Ef:" + file);
        if (file.getParentTag() == 169) {
            int recId = file.mPbrRecord;
            SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(file.mPbrRecord));
            if (files == null) {
                Rlog.e(LOG_TAG, "Error: no fileIds");
                return;
            }
            File iapFile = (File) files.get(193);
            if (iapFile == null) {
                Rlog.e(LOG_TAG, "Can't locate EF_IAP in EF_PBR.");
                return;
            }
            readIapFileAndWait(recId, iapFile.getEfid(), false);
            if (this.mIapFileList == null || this.mIapFileList.size() <= recId || ((ArrayList) this.mIapFileList.get(recId)).size() == 0) {
                Rlog.e(LOG_TAG, "Error: IAP file is empty");
                return;
            }
            int i;
            int[] size;
            int numAdnRecs = this.mPhoneBookRecords.size();
            int nOffset = recId * this.mAdnFileSize;
            int nMax = nOffset + this.mAdnFileSize;
            if (numAdnRecs < nMax) {
                nMax = numAdnRecs;
            }
            switch (file.mTag) {
                case 195:
                case 196:
                    break;
                case USIM_EFEMAIL_TAG /*202*/:
                    i = nOffset;
                    while (i < this.mEmailFileSize + nOffset) {
                        try {
                            this.mEmailRecTable[i] = 0;
                            i++;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            Rlog.e(LOG_TAG, "init RecTable error " + e.getMessage());
                            break;
                        }
                    }
                    break;
                default:
                    Rlog.e(LOG_TAG, "no implement type2 EF " + file.mTag);
                    return;
            }
            int efid = file.getEfid();
            if (this.mRecordSize == null || this.mRecordSize.get(efid) == null) {
                size = readEFLinearRecordSize(efid);
            } else {
                size = (int[]) this.mRecordSize.get(efid);
            }
            if (size == null || size.length != 3) {
                Rlog.e(LOG_TAG, "readType2: read record size error.");
                return;
            }
            log("readType2: RecordSize = " + size[0]);
            ArrayList<byte[]> iapList = (ArrayList) this.mIapFileList.get(recId);
            if (iapList.size() == 0) {
                Rlog.e(LOG_TAG, "Warning: IAP size is 0");
                return;
            }
            int Type2Index = file.getIndex();
            int totalReadingNum = 0;
            i = nOffset;
            while (i < nMax) {
                try {
                    AdnRecord arec = (AdnRecord) this.mPhoneBookRecords.get(i);
                    if (arec.getAlphaTag().length() > 0 || arec.getNumber().length() > 0) {
                        int index = ((byte[]) iapList.get(i - nOffset))[Type2Index] & 255;
                        if (index > 0 && index < 255) {
                            log("Type2 iap[" + (i - nOffset) + "]=" + index);
                            int[] data = new int[3];
                            data[0] = recId;
                            data[1] = i;
                            int loadWhat = 0;
                            switch (file.mTag) {
                                case 195:
                                    loadWhat = 18;
                                    this.mReadingSneNum.incrementAndGet();
                                    break;
                                case 196:
                                    loadWhat = 16;
                                    data[2] = file.mAnrIndex;
                                    this.mReadingAnrNum.addAndGet(1);
                                    break;
                                case USIM_EFEMAIL_TAG /*202*/:
                                    data[0] = ((i + 1) - nOffset) + (this.mEmailFileSize * nOffset);
                                    loadWhat = 15;
                                    this.mReadingEmailNum.incrementAndGet();
                                    break;
                                default:
                                    Rlog.e(LOG_TAG, "not support tag " + file.mTag);
                                    break;
                            }
                            this.mFh.readEFLinearFixed(efid, index, size[0], obtainMessage(loadWhat, data));
                            totalReadingNum++;
                        }
                    }
                    i++;
                } catch (IndexOutOfBoundsException e2) {
                    Rlog.e(LOG_TAG, "readType2Ef: mPhoneBookRecords IndexOutOfBoundsException numAdnRecs is " + numAdnRecs + "index is " + i);
                }
            }
            switch (file.mTag) {
                case 195:
                    if (this.mReadingSneNum.get() != 0) {
                        this.mNeedNotify.set(true);
                        break;
                    } else {
                        this.mNeedNotify.set(false);
                        return;
                    }
                case 196:
                    if (this.mReadingAnrNum.get() != 0) {
                        this.mNeedNotify.set(true);
                        break;
                    } else {
                        this.mNeedNotify.set(false);
                        return;
                    }
                case USIM_EFEMAIL_TAG /*202*/:
                    if (this.mReadingEmailNum.get() != 0) {
                        this.mNeedNotify.set(true);
                        break;
                    } else {
                        this.mNeedNotify.set(false);
                        return;
                    }
                default:
                    Rlog.e(LOG_TAG, "not support tag " + file.mTag);
                    break;
            }
            logi("readType2Ef before mLock.wait " + this.mNeedNotify.get() + " total:" + totalReadingNum);
            synchronized (this.mLock) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e3) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readType2Ef");
                }
            }
            logi("readType2Ef after mLock.wait " + this.mNeedNotify.get());
            return;
        }
        return;
    }

    private void updatePhoneAdnRecordWithEmailByIndex(int emailIndex, int adnIndex, byte[] emailRecData) {
        log("updatePhoneAdnRecordWithEmailByIndex emailIndex = " + emailIndex + ",adnIndex = " + adnIndex);
        if (emailRecData != null && this.mPbrRecords != null) {
            boolean emailType2 = ((File) PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(0)).get(USIM_EFEMAIL_TAG)).getParentTag() == 169;
            log("updatePhoneAdnRecordWithEmailByIndex: Type2: " + emailType2 + " emailData: " + IccUtils.bytesToHexString(emailRecData));
            int length = emailRecData.length;
            if (emailType2 && emailRecData.length >= 2) {
                length = emailRecData.length - 2;
            }
            log("updatePhoneAdnRecordWithEmailByIndex length = " + length);
            byte[] validEMailData = new byte[length];
            for (int i = 0; i < length; i++) {
                validEMailData[i] = (byte) -1;
            }
            System.arraycopy(emailRecData, 0, validEMailData, 0, length);
            log("validEMailData=" + IccUtils.bytesToHexString(validEMailData) + ", validEmailLen=" + length);
            try {
                String email = IccUtils.adnStringFieldToString(validEMailData, 0, length);
                log("updatePhoneAdnRecordWithEmailByIndex index " + adnIndex + " emailRecData record is " + email);
                if (!(email == null || email.equals(UsimPBMemInfo.STRING_NOT_SET))) {
                    AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(adnIndex);
                    String[] strArr = new String[1];
                    strArr[0] = email;
                    rec.setEmails(strArr);
                }
                this.mEmailRecTable[emailIndex - 1] = adnIndex + 1;
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "[JE]updatePhoneAdnRecordWithEmailByIndex " + e.getMessage());
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:73:0x021a A:{SYNTHETIC, Splitter: B:73:0x021a} */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x0277  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateType2Anr(String anr, int adnIndex, File file) {
        logi("updateType2Ef anr:" + anr + ",adnIndex:" + adnIndex + ",file:" + file);
        int iapRecNum = (adnIndex - 1) % this.mAdnFileSize;
        log("updateType2Ef pbrRecNum:" + ((adnIndex - 1) / this.mAdnFileSize) + ",iapRecNum:" + iapRecNum);
        if (this.mIapFileList != null && file != null && this.mPbrRecords != null) {
            SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(file.mPbrRecord));
            if (files != null) {
                try {
                    ArrayList<byte[]> list = (ArrayList) this.mIapFileList.get(file.mPbrRecord);
                    if (list != null) {
                        if (list.size() == 0) {
                            Rlog.e(LOG_TAG, "Warning: IAP size is 0");
                            return;
                        }
                        byte[] iap = (byte[]) list.get(iapRecNum);
                        if (iap != null) {
                            int index = iap[file.getIndex()] & 255;
                            log("updateType2Ef orignal index :" + index);
                            if (anr == null || anr.length() == 0) {
                                if (index > 0) {
                                    iap[file.getIndex()] = (byte) -1;
                                    if (files.get(193) != null) {
                                        this.mFh.updateEFLinearFixed(((File) files.get(193)).getEfid(), iapRecNum + 1, iap, null, obtainMessage(7));
                                    } else {
                                        Rlog.e(LOG_TAG, "updateType2Anr Error: No IAP file!");
                                        return;
                                    }
                                }
                                return;
                            }
                            int recNum = 0;
                            int size = ((int[]) this.mRecordSize.get(file.getEfid()))[2];
                            log("updateType2Anr size :" + size);
                            if (index <= 0 || index > size) {
                                int i;
                                byte[] value;
                                int tem;
                                int[] indexArray = new int[(size + 1)];
                                for (i = 1; i <= size; i++) {
                                    indexArray[i] = 0;
                                }
                                for (i = 0; i < list.size(); i++) {
                                    value = (byte[]) list.get(i);
                                    if (value != null) {
                                        tem = value[file.getIndex()] & 255;
                                        if (tem > 0 && tem < 255 && tem <= size) {
                                            indexArray[tem] = 1;
                                        }
                                    }
                                }
                                boolean sharedAnr = false;
                                File file2 = null;
                                for (i = 0; i < this.mPbrRecords.size(); i++) {
                                    if (i != file.mPbrRecord) {
                                        file2 = (File) PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(i)).get((adnIndex * 256) + 196);
                                        if (file2 != null) {
                                            if (file2.getEfid() == file.getEfid()) {
                                                sharedAnr = true;
                                            }
                                            if (sharedAnr) {
                                                try {
                                                    ArrayList<byte[]> relatedList = (ArrayList) this.mIapFileList.get(file2.mPbrRecord);
                                                    if (relatedList != null && relatedList.size() > 0) {
                                                        for (i = 0; i < relatedList.size(); i++) {
                                                            value = (byte[]) relatedList.get(i);
                                                            if (value != null) {
                                                                tem = value[file2.getIndex()] & 255;
                                                                if (tem > 0 && tem < 255 && tem <= size) {
                                                                    indexArray[tem] = 1;
                                                                }
                                                            }
                                                        }
                                                    }
                                                } catch (IndexOutOfBoundsException e) {
                                                    Rlog.e(LOG_TAG, "Index out of bounds.");
                                                    return;
                                                }
                                            }
                                            for (i = 1; i <= size; i++) {
                                                if (indexArray[i] == 0) {
                                                    recNum = i;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                if (sharedAnr) {
                                }
                                while (i <= size) {
                                }
                            } else {
                                recNum = index;
                            }
                            log("updateType2Anr final index :" + recNum);
                            if (recNum != 0) {
                                AdnRecord rec = null;
                                try {
                                    rec = (AdnRecord) this.mPhoneBookRecords.get(adnIndex - 1);
                                } catch (IndexOutOfBoundsException e2) {
                                    Rlog.e(LOG_TAG, "updateType2Anr: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is " + this.mPhoneBookRecords.size() + "index is " + (adnIndex - 1));
                                }
                                if (rec != null) {
                                    int aas = rec.getAasIndex();
                                    byte[] data = buildAnrRecord(anr, this.mAnrRecordSize, aas);
                                    int fileId = file.getEfid();
                                    if (data != null) {
                                        this.mFh.updateEFLinearFixed(fileId, recNum, data, null, obtainMessage(9));
                                        if (recNum != index) {
                                            iap[file.getIndex()] = (byte) recNum;
                                            if (files.get(193) != null) {
                                                this.mFh.updateEFLinearFixed(((File) files.get(193)).getEfid(), iapRecNum + 1, iap, null, obtainMessage(7));
                                            } else {
                                                Rlog.e(LOG_TAG, "updateType2Anr Error: No IAP file!");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (IndexOutOfBoundsException e3) {
                    Rlog.e(LOG_TAG, "Index out of bounds.");
                }
            }
        }
    }

    private void readAnrRecordSize() {
        log("readAnrRecordSize");
        if (this.mPbrRecords != null) {
            SparseArray<File> fileIds = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(0));
            if (fileIds != null) {
                File anrFile = (File) fileIds.get(196);
                if (fileIds.size() == 0 || anrFile == null) {
                    log("readAnrRecordSize: No anr tag in pbr file ");
                    return;
                }
                int[] size = readEFLinearRecordSize(anrFile.getEfid());
                if (size == null || size.length != 3) {
                    Rlog.e(LOG_TAG, "readAnrRecordSize: read record size error.");
                } else {
                    this.mAnrRecordSize = size[0];
                }
            }
        }
    }

    private void readEmailRecordSize() {
        log("readEmailRecordSize");
        if (this.mPbrRecords != null) {
            SparseArray<File> fileIds = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(0));
            if (fileIds != null) {
                File emailFile = (File) fileIds.get(USIM_EFEMAIL_TAG);
                if (fileIds.size() == 0 || emailFile == null) {
                    log("readEmailRecordSize: No email tag in pbr file ");
                    return;
                }
                int[] size = readEFLinearRecordSize(emailFile.getEfid());
                if (size == null || size.length != 3) {
                    log("readEmailRecordSize: read record size error.");
                    return;
                }
                this.mEmailFileSize = size[2];
                this.mEmailRecordSize = size[0];
            }
        }
    }

    private boolean loadAasFiles() {
        synchronized (this.mLock) {
            if (this.mAasForAnr == null || this.mAasForAnr.size() == 0) {
                if (this.mIsPbrPresent.booleanValue()) {
                    loadPBRFiles();
                    if (this.mPbrRecords == null) {
                        return false;
                    }
                    int numRecs = this.mPbrRecords.size();
                    if (this.mAasForAnr == null) {
                        this.mAasForAnr = new ArrayList();
                    }
                    this.mAasForAnr.clear();
                    if (CsimPhbStorageInfo.hasModemPhbEnhanceCapability(this.mFh)) {
                        readAasFileAndWaitOptmz();
                    } else {
                        for (int i = 0; i < numRecs; i++) {
                            readAASFileAndWait(i);
                        }
                    }
                } else {
                    Rlog.e(LOG_TAG, "No PBR files");
                    return false;
                }
            }
            return true;
        }
    }

    public ArrayList<AlphaTag> getUsimAasList() {
        logi("getUsimAasList start");
        ArrayList<AlphaTag> results = new ArrayList();
        if (!loadAasFiles()) {
            return results;
        }
        ArrayList<String> allAas = this.mAasForAnr;
        if (allAas == null) {
            return results;
        }
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < allAas.size(); j++) {
                String value = (String) allAas.get(j);
                logi("aasIndex:" + (j + 1) + ",pbrIndex:" + i + ",value:" + value);
                results.add(new AlphaTag(j + 1, value, i));
            }
        }
        return results;
    }

    public String getUsimAasById(int index, int pbrIndex) {
        logi("getUsimAasById by id " + index + ",pbrIndex " + pbrIndex);
        if (!loadAasFiles()) {
            return null;
        }
        ArrayList<String> map = this.mAasForAnr;
        if (map != null) {
            return (String) map.get(index - 1);
        }
        return null;
    }

    public boolean removeUsimAasById(int index, int pbrIndex) {
        logi("removeUsimAasById by id " + index + ",pbrIndex " + pbrIndex);
        if (!loadAasFiles()) {
            return false;
        }
        int aasIndex = index;
        SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(pbrIndex));
        if (files == null || files.get(USIM_EFAAS_TAG) == null) {
            Rlog.e(LOG_TAG, "removeUsimAasById-PBR have no AAS EF file");
            return false;
        }
        log("removeUsimAasById result,efid:" + ((File) files.get(USIM_EFAAS_TAG)).getEfid());
        if (this.mFh != null) {
            Message msg = obtainMessage(10);
            int len = getUsimAasMaxNameLen();
            byte[] aasString = new byte[len];
            for (int i = 0; i < len; i++) {
                aasString[i] = (byte) -1;
            }
            synchronized (this.mLock) {
                this.mCi.deleteUPBEntry(3, 1, index, msg);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in removesimAasById");
                }
            }
            AsyncResult ar = msg.obj;
            if (ar == null || ar.exception == null) {
                ArrayList<String> list = this.mAasForAnr;
                if (list != null) {
                    log("remove aas done " + ((String) list.get(index - 1)));
                    list.set(index - 1, null);
                }
                return true;
            }
            Rlog.e(LOG_TAG, "removeUsimAasById exception " + ar.exception);
            return false;
        }
        Rlog.e(LOG_TAG, "removeUsimAasById-IccFileHandler is null");
        return false;
    }

    /* JADX WARNING: Missing block: B:45:0x00e7, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int insertUsimAas(String aasName) {
        logi("insertUsimAas " + aasName);
        if (aasName == null || aasName.length() == 0) {
            return 0;
        }
        if (!loadAasFiles()) {
            return -1;
        }
        if (aasName.length() > getUsimAasMaxNameLen()) {
            return 0;
        }
        synchronized (this.mLock) {
            int aasIndex = 0;
            boolean found = false;
            ArrayList<String> allAas = this.mAasForAnr;
            for (int j = 0; j < allAas.size(); j++) {
                String value = (String) allAas.get(j);
                if (value == null || value.length() == 0) {
                    found = true;
                    aasIndex = j + 1;
                    break;
                }
            }
            log("insertUsimAas aasIndex:" + aasIndex + ",found:" + found);
            if (found) {
                String temp = encodeToUcs2(aasName);
                Message msg = obtainMessage(10);
                this.mCi.editUPBEntry(3, 0, aasIndex, temp, null, msg);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in insertUsimAas");
                }
                AsyncResult ar = msg.obj;
                log("insertUsimAas UPB_EF_AAS: ar " + ar);
                if (ar == null || ar.exception == null) {
                    ArrayList<String> list = this.mAasForAnr;
                    if (list != null) {
                        list.set(aasIndex - 1, aasName);
                        logi("insertUsimAas update mAasForAnr done");
                    }
                } else {
                    Rlog.e(LOG_TAG, "insertUsimAas exception " + ar.exception);
                    return -1;
                }
            }
            return -2;
        }
    }

    public boolean updateUsimAas(int index, int pbrIndex, String aasName) {
        logi("updateUsimAas index " + index + ",pbrIndex " + pbrIndex + ",aasName " + aasName);
        if (!loadAasFiles()) {
            return false;
        }
        ArrayList<String> map = this.mAasForAnr;
        if (index <= 0 || index > map.size()) {
            Rlog.e(LOG_TAG, "updateUsimAas not found aas index " + index);
            return false;
        }
        log("updateUsimAas old aas " + ((String) map.get(index - 1)));
        if (aasName == null || aasName.length() == 0) {
            return removeUsimAasById(index, pbrIndex);
        }
        int limit = getUsimAasMaxNameLen();
        int len = aasName.length();
        log("updateUsimAas aas limit " + limit);
        if (len > limit) {
            return false;
        }
        log("updateUsimAas offset " + 0);
        int aasIndex = index + 0;
        String temp = encodeToUcs2(aasName);
        Message msg = obtainMessage(10);
        synchronized (this.mLock) {
            this.mCi.editUPBEntry(3, 0, aasIndex, temp, null, msg);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                Rlog.e(LOG_TAG, "Interrupted Exception in updateUsimAas");
            }
        }
        AsyncResult ar = msg.obj;
        if (ar == null || ar.exception == null) {
            ArrayList<String> list = this.mAasForAnr;
            if (list != null) {
                list.set(index - 1, aasName);
                logi("updateUsimAas update mAasForAnr done");
            }
            return true;
        }
        Rlog.e(LOG_TAG, "updateUsimAas exception " + ar.exception);
        return false;
    }

    public boolean updateAdnAas(int adnIndex, int aasIndex) {
        int pbrRecNum = (adnIndex - 1) / this.mAdnFileSize;
        int index = (adnIndex - 1) % this.mAdnFileSize;
        try {
            AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(adnIndex - 1);
            rec.setAasIndex(aasIndex);
            for (int i = 0; i < 3; i++) {
                updateAnrByAdnIndex(rec.getAdditionalNumber(i), adnIndex, i);
            }
            return true;
        } catch (IndexOutOfBoundsException e) {
            Rlog.e(LOG_TAG, "updateADNAAS: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is " + this.mPhoneBookRecords.size() + "index is " + (adnIndex - 1));
            return false;
        }
    }

    public int getUsimAasMaxNameLen() {
        logi("getUsimAasMaxNameLen begin");
        synchronized (this.mUPBCapabilityLock) {
            if (this.mUpbCap[4] < 0 && checkIsPhbReady()) {
                this.mCi.queryUPBCapability(obtainMessage(19));
                try {
                    this.mUPBCapabilityLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in getUsimAasMaxNameLen");
                }
            }
        }
        logi("getUsimAasMaxNameLen done: L_AAS is " + this.mUpbCap[4]);
        return this.mUpbCap[4];
    }

    public int getUsimAasMaxCount() {
        logi("getUsimAasMaxCount begin");
        synchronized (this.mUPBCapabilityLock) {
            if (this.mUpbCap[3] < 0 && checkIsPhbReady()) {
                this.mCi.queryUPBCapability(obtainMessage(19));
                try {
                    this.mUPBCapabilityLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in getUsimAasMaxCount");
                }
            }
        }
        logi("getUsimAasMaxCount done: N_AAS is " + this.mUpbCap[3]);
        return this.mUpbCap[3];
    }

    public void loadPBRFiles() {
        if (this.mIsPbrPresent.booleanValue()) {
            synchronized (this.mLock) {
                if (this.mPbrRecords == null) {
                    readPbrFileAndWait(true);
                }
            }
        }
    }

    public int getAnrCount() {
        int i = 0;
        logi("getAnrCount begin");
        synchronized (this.mUPBCapabilityLock) {
            if (this.mUpbCap[0] < 0 && checkIsPhbReady()) {
                this.mCi.queryUPBCapability(obtainMessage(19));
                try {
                    this.mUPBCapabilityLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in getAnrCount");
                }
            }
        }
        if (this.mAnrRecordSize <= 0) {
            return this.mAnrRecordSize;
        }
        logi("getAnrCount done: N_ANR is " + this.mUpbCap[0]);
        if (this.mUpbCap[0] > 0) {
            i = 1;
        }
        return i;
    }

    public int getEmailCount() {
        int i = 1;
        logi("getEmailCount begin");
        synchronized (this.mUPBCapabilityLock) {
            if (this.mUpbCap[1] < 0 && checkIsPhbReady()) {
                this.mCi.queryUPBCapability(obtainMessage(19));
                try {
                    this.mUPBCapabilityLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in getEmailCount");
                }
            }
        }
        if (this.mEmailRecordSize <= 0) {
            return this.mEmailRecordSize;
        }
        logi("getEmailCount done: N_EMAIL is " + this.mUpbCap[1]);
        if (this.mUpbCap[1] <= 0) {
            i = 0;
        }
        return i;
    }

    public boolean hasSne() {
        log("hasSne begin");
        synchronized (this.mUPBCapabilityLock) {
            if (this.mUpbCap[2] < 0 && checkIsPhbReady()) {
                this.mCi.queryUPBCapability(obtainMessage(19));
                try {
                    this.mUPBCapabilityLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in hasSne");
                }
            }
        }
        log("hasSne done: N_Sne is " + this.mUpbCap[2]);
        if (this.mUpbCap[2] > 0) {
            return true;
        }
        return false;
    }

    public int getSneRecordLen() {
        int resultSize = 0;
        if (!hasSne()) {
            return 0;
        }
        if (this.mPbrRecords == null || this.mPbrRecords.get(0) == null) {
            return -1;
        }
        SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(0));
        if (files == null) {
            return -1;
        }
        File sneFile = (File) files.get(195);
        if (sneFile == null) {
            return 0;
        }
        int[] size;
        int efid = sneFile.getEfid();
        boolean sneType2 = sneFile.getParentTag() == 169;
        logi("getSneRecordLen: EFSNE id is " + efid);
        if (this.mRecordSize == null || this.mRecordSize.get(efid) == null) {
            size = readEFLinearRecordSize(efid);
        } else {
            size = (int[]) this.mRecordSize.get(efid);
        }
        if (size != null) {
            if (sneType2) {
                resultSize = size[0] - 2;
            } else {
                resultSize = size[0];
            }
        }
        return resultSize;
    }

    public int getUpbDone() {
        return this.mUpbDone;
    }

    private void updatePhoneAdnRecordWithSneByIndex(int recNum, int adnIndex, byte[] recData) {
        if (recData != null) {
            String sne = IccUtils.adnStringFieldToString(recData, 0, recData.length);
            log("updatePhoneAdnRecordWithSneByIndex index " + adnIndex + " recData file is " + sne);
            if (!(sne == null || sne.equals(UsimPBMemInfo.STRING_NOT_SET))) {
                try {
                    ((AdnRecord) this.mPhoneBookRecords.get(adnIndex)).setSne(sne);
                } catch (IndexOutOfBoundsException e) {
                    Rlog.e(LOG_TAG, "updatePhoneAdnRecordWithSneByIndex: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is " + this.mPhoneBookRecords.size() + "index is " + adnIndex);
                }
            }
        }
    }

    public int updateSneByAdnIndex(String sne, int adnIndex) {
        logi("updateSneByAdnIndex sne is " + sne + ",adnIndex " + adnIndex);
        int pbrRecNum = (adnIndex - 1) / this.mAdnFileSize;
        int nIapRecNum = (adnIndex - 1) % this.mAdnFileSize;
        if (this.mPbrRecords == null) {
            return -1;
        }
        Message msg = obtainMessage(11);
        SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(pbrRecNum));
        if (files == null || files.get(195) == null) {
            log("updateSneByAdnIndex: No SNE tag in pbr file 0");
            return -1;
        } else if (this.mPhoneBookRecords == null || this.mPhoneBookRecords.isEmpty()) {
            return -1;
        } else {
            log("updateSneByAdnIndex: EF_SNE id is " + Integer.toHexString(((File) files.get(195)).getEfid()).toUpperCase());
            log("updateSneByAdnIndex: efIndex is " + 1);
            synchronized (this.mLock) {
                if (sne != null) {
                    if (sne.length() != 0) {
                        this.mCi.editUPBEntry(2, 1, adnIndex, encodeToUcs2(sne), null, msg);
                        this.mLock.wait();
                    }
                }
                this.mCi.deleteUPBEntry(2, 1, adnIndex, msg);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in updateSneByAdnIndex");
                }
            }
            return this.mResult;
        }
    }

    public int[] getAdnRecordsCapacity() {
        int[] capacity = new int[6];
        if (this.mRefreshAdnInfo || this.mRefreshEmailInfo || this.mRefreshAnrInfo || this.mAdnRecordSize == null || this.mAdnRecordSize.length != 4) {
            getAdnStorageInfo();
            this.mRefreshAdnInfo = false;
        }
        if (this.mAdnRecordSize == null || this.mAdnRecordSize.length != 4) {
            return null;
        }
        capacity[0] = this.mAdnRecordSize[1];
        capacity[1] = this.mAdnRecordSize[0];
        if (this.mRefreshEmailInfo || this.mEmailInfo == null || this.mEmailInfo.length != 3) {
            this.mCi.queryUPBAvailable(1, 1, obtainMessage(25));
            synchronized (this.mLock) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in getAdnRecordsCapacity");
                }
            }
            this.mRefreshEmailInfo = false;
        }
        if (this.mEmailInfo == null || this.mEmailInfo.length != 3) {
            return null;
        }
        capacity[2] = this.mEmailInfo[0];
        capacity[3] = this.mEmailInfo[0] - this.mEmailInfo[1];
        if (this.mRefreshAnrInfo || this.mAnrInfo == null || this.mAnrInfo.get(0) == null || ((int[]) this.mAnrInfo.get(0)).length != 3) {
            this.mCi.queryUPBAvailable(0, 1, obtainMessage(26));
            synchronized (this.mLock) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e2) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in getAdnRecordsCapacity");
                }
            }
            this.mRefreshAnrInfo = false;
        }
        if (this.mAnrInfo == null || this.mAnrInfo.get(0) == null || ((int[]) this.mAnrInfo.get(0)).length != 3) {
            return null;
        }
        capacity[4] = ((int[]) this.mAnrInfo.get(0))[0];
        capacity[5] = ((int[]) this.mAnrInfo.get(0))[0] - ((int[]) this.mAnrInfo.get(0))[1];
        logi("getAdnRecordsCapacity: max adn=" + capacity[0] + ", used adn=" + capacity[1] + ", max email=" + capacity[2] + ", used email=" + capacity[3] + ", max anr=" + capacity[4] + ", used anr=" + capacity[5]);
        return capacity;
    }

    private int[] getAdnStorageInfo() {
        log("getAdnStorageInfo ");
        if (this.mCi != null) {
            this.mCi.queryPhbStorageInfo(0, obtainMessage(21));
            synchronized (this.mLock) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in getAdnStorageInfo");
                }
            }
            return this.mAdnRecordSize;
        }
        Rlog.w(LOG_TAG, "GetAdnStorageInfo: filehandle is null.");
        return null;
    }

    public UsimPBMemInfo[] getPhonebookMemStorageExt() {
        boolean is3G = this.mCurrentApp.getType() == AppType.APPTYPE_USIM;
        log("getPhonebookMemStorageExt isUsim " + is3G);
        if (!is3G) {
            return getPhonebookMemStorageExt2G();
        }
        if (this.mPbrRecords == null) {
            loadPBRFiles();
        }
        if (this.mPbrRecords == null) {
            return null;
        }
        int i;
        log("getPhonebookMemStorageExt slice " + this.mPbrRecords.size());
        UsimPBMemInfo[] response = new UsimPBMemInfo[this.mPbrRecords.size()];
        for (i = 0; i < this.mPbrRecords.size(); i++) {
            response[i] = new UsimPBMemInfo();
        }
        if (this.mPhoneBookRecords.isEmpty()) {
            Rlog.w(LOG_TAG, "mPhoneBookRecords has not been loaded.");
            return response;
        }
        int pbrIndex = 0;
        while (pbrIndex < this.mPbrRecords.size()) {
            int[] size;
            int used;
            AdnRecord rec;
            SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(pbrIndex));
            int numAdnRecs = this.mPhoneBookRecords.size();
            int nOffset = pbrIndex * this.mAdnFileSize;
            int nMax = nOffset + this.mAdnFileSize;
            if (numAdnRecs < nMax) {
                nMax = numAdnRecs;
            }
            File adnFile = (File) files.get(192);
            if (adnFile != null) {
                size = readEFLinearRecordSize(adnFile.getEfid());
                if (size != null) {
                    response[pbrIndex].setAdnLength(size[0]);
                    response[pbrIndex].setAdnTotal(size[2]);
                }
                response[pbrIndex].setAdnType(adnFile.getParentTag());
                response[pbrIndex].setSliceIndex(pbrIndex + 1);
                used = 0;
                rec = null;
                for (int j = nOffset; j < nMax; j++) {
                    try {
                        rec = (AdnRecord) this.mPhoneBookRecords.get(j);
                    } catch (IndexOutOfBoundsException e) {
                        Rlog.e(LOG_TAG, "getPhonebookMemStorageExt: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is " + this.mPhoneBookRecords.size() + "index is " + j);
                    }
                    if (rec != null && ((rec.getAlphaTag() != null && rec.getAlphaTag().length() > 0) || (rec.getNumber() != null && rec.getNumber().length() > 0))) {
                        log("Adn: " + rec.toString());
                        used++;
                        rec = null;
                    }
                }
                log("adn used " + used);
                response[pbrIndex].setAdnUsed(used);
            }
            File anrFile = (File) files.get(196);
            if (anrFile != null) {
                size = readEFLinearRecordSize(anrFile.getEfid());
                if (size != null) {
                    response[pbrIndex].setAnrLength(size[0]);
                    response[pbrIndex].setAnrTotal(size[2]);
                }
                response[pbrIndex].setAnrType(anrFile.getParentTag());
                used = 0;
                rec = null;
                for (i = nOffset; i < this.mPhoneBookRecords.size() + nOffset; i++) {
                    try {
                        rec = (AdnRecord) this.mPhoneBookRecords.get(i);
                    } catch (IndexOutOfBoundsException e2) {
                        Rlog.e(LOG_TAG, "getPhonebookMemStorageExt: mPhoneBookRecords IndexOutOfBoundsException mPhoneBookRecords.size() is " + this.mPhoneBookRecords.size() + "index is " + i);
                    }
                    if (rec == null) {
                        log("null anr rec ");
                    } else {
                        String anrStr = rec.getAdditionalNumber();
                        if (anrStr != null && anrStr.length() > 0) {
                            log("anrStr: " + anrStr);
                            used++;
                        }
                    }
                }
                log("anr used: " + used);
                response[pbrIndex].setAnrUsed(used);
            }
            File emailFile = (File) files.get(USIM_EFEMAIL_TAG);
            if (emailFile != null) {
                size = readEFLinearRecordSize(emailFile.getEfid());
                if (size != null) {
                    response[pbrIndex].setEmailLength(size[0]);
                    response[pbrIndex].setEmailTotal(size[2]);
                }
                response[pbrIndex].setEmailType(emailFile.getParentTag());
                log("numAdnRecs:" + numAdnRecs);
                used = 0;
                i = nOffset;
                while (i < this.mEmailFileSize + nOffset) {
                    try {
                        if (this.mEmailRecTable[i] > 0) {
                            used++;
                        }
                        i++;
                    } catch (ArrayIndexOutOfBoundsException e3) {
                        Rlog.e(LOG_TAG, "get mEmailRecTable error " + e3.getMessage());
                    }
                }
                log("emailList used:" + used);
                response[pbrIndex].setEmailUsed(used);
            }
            File ext1File = (File) files.get(194);
            if (ext1File != null) {
                size = readEFLinearRecordSize(ext1File.getEfid());
                if (size != null) {
                    response[pbrIndex].setExt1Length(size[0]);
                    response[pbrIndex].setExt1Total(size[2]);
                }
                response[pbrIndex].setExt1Type(ext1File.getParentTag());
                synchronized (this.mLock) {
                    readExt1FileAndWait(pbrIndex);
                }
                used = 0;
                if (this.mExt1FileList != null && pbrIndex < this.mExt1FileList.size()) {
                    ArrayList<byte[]> ext1 = (ArrayList) this.mExt1FileList.get(pbrIndex);
                    if (ext1 != null) {
                        int len = ext1.size();
                        for (i = 0; i < len; i++) {
                            byte[] arr = (byte[]) ext1.get(i);
                            log("ext1[" + i + "]=" + IccUtils.bytesToHexString(arr));
                            if (arr != null && arr.length > 0 && (arr[0] == (byte) 1 || arr[0] == (byte) 2)) {
                                used++;
                            }
                        }
                    }
                }
                response[pbrIndex].setExt1Used(used);
            }
            File gasFile = (File) files.get(200);
            if (gasFile != null) {
                size = readEFLinearRecordSize(gasFile.getEfid());
                if (size != null) {
                    response[pbrIndex].setGasLength(size[0]);
                    response[pbrIndex].setGasTotal(size[2]);
                }
                response[pbrIndex].setGasType(gasFile.getParentTag());
            }
            File aasFile = (File) files.get(USIM_EFAAS_TAG);
            if (aasFile != null) {
                size = readEFLinearRecordSize(aasFile.getEfid());
                if (size != null) {
                    response[pbrIndex].setAasLength(size[0]);
                    response[pbrIndex].setAasTotal(size[2]);
                }
                response[pbrIndex].setAasType(aasFile.getParentTag());
            }
            File sneFile = (File) files.get(195);
            if (sneFile != null) {
                size = readEFLinearRecordSize(sneFile.getEfid());
                if (size != null) {
                    response[pbrIndex].setSneLength(size[0]);
                    response[pbrIndex].setSneTotal(size[0]);
                }
                response[pbrIndex].setSneType(sneFile.getParentTag());
            }
            File ccpFile = (File) files.get(USIM_EFCCP1_TAG);
            if (ccpFile != null) {
                size = readEFLinearRecordSize(ccpFile.getEfid());
                if (size != null) {
                    response[pbrIndex].setCcpLength(size[0]);
                    response[pbrIndex].setCcpTotal(size[0]);
                }
                response[pbrIndex].setCcpType(ccpFile.getParentTag());
            }
            pbrIndex++;
        }
        for (i = 0; i < this.mPbrRecords.size(); i++) {
            log("getPhonebookMemStorageExt[" + i + "]:" + response[i]);
        }
        return response;
    }

    public UsimPBMemInfo[] getPhonebookMemStorageExt2G() {
        UsimPBMemInfo[] response = new UsimPBMemInfo[1];
        response[0] = new UsimPBMemInfo();
        int[] size = readEFLinearRecordSize(28474);
        if (size != null) {
            response[0].setAdnLength(size[0]);
            if (isAdnAccessible()) {
                response[0].setAdnTotal(size[2]);
            } else {
                response[0].setAdnTotal(0);
            }
        }
        response[0].setAdnType(168);
        response[0].setSliceIndex(1);
        size = readEFLinearRecordSize(IccConstants.EF_EXT1);
        if (size != null) {
            response[0].setExt1Length(size[0]);
            response[0].setExt1Total(size[2]);
        }
        response[0].setExt1Type(170);
        synchronized (this.mLock) {
            if (this.mFh != null) {
                Message msg = obtainMessage(1001);
                msg.arg1 = 0;
                this.mFh.loadEFLinearFixedAll(IccConstants.EF_EXT1, msg);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readExt1FileAndWait");
                }
            } else {
                Rlog.e(LOG_TAG, "readExt1FileAndWait-IccFileHandler is null");
                return response;
            }
        }
        int used = 0;
        if (this.mExt1FileList != null && this.mExt1FileList.size() > 0) {
            ArrayList<byte[]> ext1 = (ArrayList) this.mExt1FileList.get(0);
            if (ext1 != null) {
                int len = ext1.size();
                for (int i = 0; i < len; i++) {
                    byte[] arr = (byte[]) ext1.get(i);
                    log("ext1[" + i + "]=" + IccUtils.bytesToHexString(arr));
                    if (arr != null && arr.length > 0 && (arr[0] == (byte) 1 || arr[0] == (byte) 2)) {
                        used++;
                    }
                }
            }
        }
        response[0].setExt1Used(used);
        log("getPhonebookMemStorageExt2G:" + response[0]);
        return response;
    }

    public int[] readEFLinearRecordSize(int fileId) {
        int[] recordSize;
        log("readEFLinearRecordSize fileid " + Integer.toHexString(fileId).toUpperCase());
        Message msg = obtainMessage(1000);
        msg.arg1 = fileId;
        synchronized (this.mLock) {
            if (this.mFh != null) {
                this.mFh.getEFLinearRecordSize(fileId, msg);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readEFLinearRecordSize");
                }
            } else {
                Rlog.e(LOG_TAG, "readEFLinearRecordSize-IccFileHandler is null");
            }
            recordSize = this.mRecordSize != null ? (int[]) this.mRecordSize.get(fileId) : null;
            if (recordSize != null) {
                logi("readEFLinearRecordSize fileid:" + Integer.toHexString(fileId).toUpperCase() + ",len:" + recordSize[0] + ",total:" + recordSize[1] + ",count:" + recordSize[2]);
            }
        }
        return recordSize;
    }

    private void readExt1FileAndWait(int recId) {
        logi("readExt1FileAndWait " + recId);
        if (this.mPbrRecords != null && this.mPbrRecords.get(recId) != null) {
            SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(recId));
            if (files == null || files.get(194) == null) {
                Rlog.e(LOG_TAG, "readExt1FileAndWait-PBR have no Ext1 record");
                return;
            }
            int efid = ((File) files.get(194)).getEfid();
            log("readExt1FileAndWait-get EXT1 EFID " + efid);
            if (this.mExt1FileList != null && recId < this.mExt1FileList.size()) {
                log("EXT1 has been loaded for Pbr number " + recId);
            } else if (this.mFh != null) {
                Message msg = obtainMessage(1001);
                msg.arg1 = recId;
                this.mFh.loadEFLinearFixedAll(efid, msg);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readExt1FileAndWait");
                }
            } else {
                Rlog.e(LOG_TAG, "readExt1FileAndWait-IccFileHandler is null");
            }
        }
    }

    private boolean checkIsPhbReady() {
        boolean z = false;
        String strPhbReady = "false";
        String strAllSimState = UsimPBMemInfo.STRING_NOT_SET;
        String strCurSimState = UsimPBMemInfo.STRING_NOT_SET;
        int slotId = this.mCurrentApp.getSlotId();
        if (SubscriptionManager.isValidSlotId(slotId)) {
            int[] subId = SubscriptionManager.getSubId(slotId);
            int phoneId = SubscriptionManager.getPhoneId(subId[0]);
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
            if (1 == slotId) {
                strPhbReady = SystemProperties.get("gsm.sim.ril.phbready.2", "false");
            } else if (2 == slotId) {
                strPhbReady = SystemProperties.get("gsm.sim.ril.phbready.3", "false");
            } else if (3 == slotId) {
                strPhbReady = SystemProperties.get("gsm.sim.ril.phbready.4", "false");
            } else {
                strPhbReady = SystemProperties.get("gsm.sim.ril.phbready", "false");
            }
            logi("[isPhbReady] subId[0]:" + subId[0] + ", slotId: " + slotId + ", isPhbReady: " + strPhbReady + ",strSimState: " + strAllSimState);
            if (strPhbReady.equals("true") && !isSimLocked) {
                z = true;
            }
            return z;
        }
        log("[isPhbReady] InvalidSlotId slotId: " + slotId);
        return false;
    }

    public boolean isAdnAccessible() {
        if (this.mFh != null && this.mCurrentApp.getType() == AppType.APPTYPE_SIM) {
            synchronized (this.mLock) {
                this.mFh.selectEFFile(28474, obtainMessage(20));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in isAdnAccessible");
                }
            }
            return this.efData == null || (this.efData.getFileStatus() & 5) > 0;
        }
        return true;
    }

    public boolean isUsimPhbEfAndNeedReset(int fileId) {
        logi("isUsimPhbEfAndNeedReset, fileId: " + Integer.toHexString(fileId).toUpperCase());
        if (this.mPbrRecords == null) {
            Rlog.e(LOG_TAG, "isUsimPhbEfAndNeedReset, No PBR files");
            return false;
        }
        int numRecs = this.mPbrRecords.size();
        for (int i = 0; i < numRecs; i++) {
            SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(i));
            int j = 192;
            while (j <= USIM_EFCCP1_TAG) {
                if (j == 197 || j == 201 || j == USIM_EFCCP1_TAG) {
                    logi("isUsimPhbEfAndNeedReset, not reset EF: " + j);
                } else if (files.get(j) != null && fileId == ((File) files.get(j)).getEfid()) {
                    logi("isUsimPhbEfAndNeedReset, return true with EF: " + j);
                    return true;
                }
                j++;
            }
        }
        log("isUsimPhbEfAndNeedReset, return false.");
        return false;
    }

    private void readAdnFileAndWaitForUICC(int recId) {
        logi("readAdnFileAndWaitForUICC " + recId);
        if (this.mPbrRecords != null) {
            SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(recId));
            if (files != null && files.size() != 0) {
                if (files.get(192) == null) {
                    Rlog.e(LOG_TAG, "readAdnFileAndWaitForUICC: No ADN tag in pbr record " + recId);
                    return;
                }
                int efid = ((File) files.get(192)).getEfid();
                log("readAdnFileAndWaitForUICC: EFADN id is " + efid);
                log("UiccPhoneBookManager readAdnFileAndWaitForUICC: recId is " + recId + UsimPBMemInfo.STRING_NOT_SET);
                this.mAdnCache.requestLoadAllAdnLike(efid, this.mAdnCache.extensionEfForEf(28474), obtainMessage(2));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readAdnFileAndWait");
                }
                int previousSize = this.mPhoneBookRecords.size();
                if (this.mPbrRecords != null && this.mPbrRecords.size() > recId) {
                    PbrRecord.m181-set0((PbrRecord) this.mPbrRecords.get(recId), this.mPhoneBookRecords.size() - previousSize);
                }
            }
        }
    }

    public ArrayList<AdnRecord> getAdnListFromUsim() {
        return this.mPhoneBookRecords;
    }

    private ArrayList<AdnRecord> changeAdnRecordNumber(int baseNumber, ArrayList<AdnRecord> adnList) {
        int size = adnList.size();
        for (int i = 0; i < size; i++) {
            AdnRecord adnRecord = (AdnRecord) adnList.get(i);
            if (adnRecord != null) {
                adnRecord.setRecordIndex(adnRecord.getRecId() + baseNumber);
            }
        }
        return adnList;
    }

    private int getUsimEfType(int efTag) {
        if (this.mPbrRecords == null) {
            return 0;
        }
        SparseArray<File> files = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(0));
        if (files == null) {
            return 0;
        }
        File efFile = (File) files.get(efTag);
        if (efFile == null) {
            return 0;
        }
        Rlog.d(LOG_TAG, "[getUsimEfType] efTag: " + efTag + ", type: " + efFile.getParentTag());
        return efFile.getParentTag();
    }

    public boolean isPbrExsit() {
        log("isPbrExsit: mIsPbrPresent = " + this.mIsPbrPresent);
        return this.mIsPbrPresent.booleanValue();
    }

    private boolean isSupportSne() {
        log("isSupportSne: " + false);
        return false;
    }

    public int oppoUpdateAdn(int efid, AdnRecord oldAdn, AdnRecord newAdn, int index, String pin2, Message response) {
        log("oppoUpdateAdn: efid = 0x" + Integer.toHexString(efid) + " index = " + index);
        if (efid != 28474 || newAdn == null) {
            log("oppoUpdateAdn: efid must is EF_ADN and newAdn can't  null");
            return -1;
        }
        int count = 1;
        if (-1 == index) {
            Iterator<AdnRecord> it = this.mPhoneBookRecords.iterator();
            while (it.hasNext()) {
                if (oldAdn != null) {
                    if (oldAdn.isEqual((AdnRecord) it.next())) {
                        index = count;
                        break;
                    }
                }
                count++;
            }
            if (-1 == index) {
                return -1;
            }
        }
        int recNum = 0;
        int recordIndex = -1;
        count = 0;
        for (Integer intValue : this.mOPPOEFRecNum) {
            int k = intValue.intValue();
            if (index <= count + k) {
                recordIndex = index - count;
                break;
            }
            recNum++;
            count += k;
        }
        if (-1 == recordIndex || this.mPbrRecords == null) {
            log("oppoUpdateAdn: recordIndex or mPbrRecords error");
            return -1;
        }
        SparseArray<File> FileIds = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(recNum));
        if (FileIds == null || FileIds.size() < 1) {
            log("oppoUpdateAdn: FileIds error");
            return -1;
        } else if (this.pendingResponse != null) {
            log("oppoUpdateAdn: pendingResponse not null");
            return -1;
        } else {
            this.pendingResponse = response;
            File file = (File) FileIds.get(192);
            this.mAdnCache.updateAdnByIndex(file.getEfid(), newAdn, recordIndex, pin2, this.mOppoHandler.obtainMessage(90, index, recordIndex, newAdn));
            return index;
        }
    }

    public int oppoGetAdnEfIdForUsim() {
        if (this.mPbrRecords == null) {
            return 28474;
        }
        SparseArray<File> FileIds = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(0));
        if (FileIds != null) {
            return ((File) FileIds.get(192)).getEfid();
        }
        return 28474;
    }

    public int oppoGetExt1EfIdForUsim() {
        if (this.mPbrRecords == null) {
            return 0;
        }
        SparseArray<File> FileIds = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(0));
        if (FileIds != null) {
            return ((File) FileIds.get(194)).getEfid();
        }
        return 0;
    }

    public int getUsimEmailLength() {
        return this.mEmailRecordSize;
    }

    private int oppoGePbcEFidForUsim(int index) {
        int recNum = 0;
        int recordIndex = -1;
        int count = 0;
        for (Integer intValue : this.mOPPOEFRecNum) {
            int k = intValue.intValue();
            if (index <= count + k) {
                recordIndex = index - count;
                break;
            }
            recNum++;
            count += k;
        }
        if (-1 == recordIndex) {
            log("oppoGePbcEFidForUsim: not found recordIndex!!!");
            return -1;
        }
        SparseArray<File> FileIds = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(recNum));
        if (FileIds == null) {
            log("ClearEFPbc:mFileIds is null for record:" + recNum);
            return -1;
        }
        File file = (File) FileIds.get(197);
        if (file != null) {
            return file.getEfid();
        }
        log("ClearEFPbc:File is null for record:" + recNum);
        return -1;
    }

    private void ClearEFPbc() {
        int numRecs = this.mPbrRecords.size();
        for (int i = 0; i < numRecs; i++) {
            SparseArray<File> FileIds = PbrRecord.m180-get1((PbrRecord) this.mPbrRecords.get(i));
            if (FileIds == null) {
                log("ClearEFPbc:mFileIds is null for number:" + i);
                return;
            }
            File file = (File) FileIds.get(197);
            if (file == null) {
                log("ClearEFPbc:File is null for number:" + i);
                return;
            }
            int efid = file.getEfid();
            this.mFh.getEFLinearRecordSize(efid, this.mOppoHandler.obtainMessage(94, i, efid));
        }
    }

    private void clearAllEFPbcControlInformation(int efid, int recordSize, int recordCount) {
        for (int numRecords = 1; numRecords <= recordCount; numRecords++) {
            this.mFh.oppoReadEFLinearFixedRecord(efid, numRecords, recordSize, this.mOppoHandler.obtainMessage(92, efid, numRecords));
        }
    }

    private void resetEFpbcControlInfor(int efid, int numRecords) {
        byte[] data = new byte[2];
        data[0] = (byte) 0;
        data[1] = (byte) 0;
        this.mFh.updateEFLinearFixed(efid, numRecords, data, null, null);
        this.mFh.loadEFTransparent(20259, this.mOppoHandler.obtainMessage(93));
    }
}
