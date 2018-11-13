package android.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.CountryDetector;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telecom.PhoneAccount;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.TtsSpan;
import android.text.style.TtsSpan.TelephoneBuilder;
import android.util.SparseIntArray;
import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.android.i18n.phonenumbers.Phonemetadata.PhoneMetadata;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber.CountryCodeSource;
import com.android.i18n.phonenumbers.ShortNumberUtil;
import com.android.internal.R;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.midi.MidiConstants;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyProperties;
import com.mediatek.internal.telephony.ITelephonyEx;
import com.mediatek.internal.telephony.ITelephonyEx.Stub;
import com.mediatek.internal.telephony.cdma.pluscode.IPlusCodeUtils;
import com.mediatek.internal.telephony.cdma.pluscode.PlusCodeProcessor;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class PhoneNumberUtils {
    private static final int CCC_LENGTH = 0;
    private static final String[] CDMA_SIM_RECORDS_PROPERTY_ECC_LIST = null;
    private static final String CLIR_OFF = "#31#";
    private static final String CLIR_ON = "*31#";
    private static final String[] CONVERT_TO_EMERGENCY_MAP = null;
    private static final boolean[] COUNTRY_CALLING_CALL = null;
    private static final boolean DBG = false;
    public static final int FORMAT_JAPAN = 2;
    public static final int FORMAT_NANP = 1;
    public static final int FORMAT_UNKNOWN = 0;
    private static final Pattern GLOBAL_PHONE_NUMBER_PATTERN = null;
    private static final SparseIntArray KEYPAD_MAP = null;
    private static final String KOREA_ISO_COUNTRY_CODE = "KR";
    static final String LOG_TAG = "PhoneNumberUtils";
    private static final int MAX_SIM_NUM = 4;
    static final int MIN_MATCH = 7;
    private static final int MIN_MATCH_CTA = 11;
    static final int MIN_MATCH_EXP = 7;
    static final int MIN_MATCH_OPPO = 11;
    private static final String[] NANP_COUNTRIES = null;
    private static final String NANP_IDP_STRING = "011";
    private static final int NANP_LENGTH = 10;
    private static final int NANP_STATE_DASH = 4;
    private static final int NANP_STATE_DIGIT = 1;
    private static final int NANP_STATE_ONE = 3;
    private static final int NANP_STATE_PLUS = 2;
    private static final String[] NETWORK_ECC_LIST = null;
    public static final char PAUSE = ',';
    private static final char PLUS_SIGN_CHAR = '+';
    private static final String PLUS_SIGN_STRING = "+";
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE = null;
    private static final String[] SIM_RECORDS_PROPERTY_ECC_LIST = null;
    public static final int TOA_International = 145;
    public static final int TOA_Unknown = 129;
    private static final boolean VDBG = false;
    public static final char WAIT = ';';
    public static final char WILD = 'N';
    private static ArrayList<EccSource> sAllEccSource;
    private static EccSource sCtaEcc;
    private static boolean sIsC2kSupport;
    private static boolean sIsCtaSet;
    private static boolean sIsCtaSupport;
    private static boolean sIsOP09Support;
    private static EccSource sNetworkEcc;
    private static EccSource sOmhEcc;
    private static IPlusCodeUtils sPlusCodeUtils;
    private static EccSource sPropertyEcc;
    private static EccSource sSimEcc;
    private static int sSpecificEccCat;
    private static EccSource sTestEcc;
    private static EccSource sXmlEcc;

    private static class CountryCallingCodeAndNewIndex {
        public final int countryCallingCode;
        public final int newIndex;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.telephony.PhoneNumberUtils.CountryCallingCodeAndNewIndex.<init>(int, int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public CountryCallingCodeAndNewIndex(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.telephony.PhoneNumberUtils.CountryCallingCodeAndNewIndex.<init>(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.CountryCallingCodeAndNewIndex.<init>(int, int):void");
        }
    }

    private static class EccSource {
        protected ArrayList<EccEntry> mCdmaEccList;
        protected ArrayList<EccEntry> mEccList;
        private int mPhoneType;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.telephony.PhoneNumberUtils.EccSource.<init>(int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public EccSource(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.telephony.PhoneNumberUtils.EccSource.<init>(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccSource.<init>(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccSource.isCt4GDualModeCard(int):boolean, dex: 
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
        private boolean isCt4GDualModeCard(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccSource.isCt4GDualModeCard(int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccSource.isCt4GDualModeCard(int):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccSource.isEccPlmnMatch(java.lang.String):boolean, dex: 
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
        public static boolean isEccPlmnMatch(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccSource.isEccPlmnMatch(java.lang.String):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccSource.isEccPlmnMatch(java.lang.String):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccSource.isSimReady(int):boolean, dex: 
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
        private boolean isSimReady(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccSource.isSimReady(int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccSource.isSimReady(int):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telephony.PhoneNumberUtils.EccSource.addToEccList(java.util.ArrayList):void, dex: 
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
        public synchronized void addToEccList(java.util.ArrayList<android.telephony.PhoneNumberUtils.EccEntry> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telephony.PhoneNumberUtils.EccSource.addToEccList(java.util.ArrayList):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccSource.addToEccList(java.util.ArrayList):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telephony.PhoneNumberUtils.EccSource.getServiceCategory(java.lang.String, int):int, dex: 
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
        public synchronized int getServiceCategory(java.lang.String r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telephony.PhoneNumberUtils.EccSource.getServiceCategory(java.lang.String, int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccSource.getServiceCategory(java.lang.String, int):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccSource.isMatch(java.lang.String, java.lang.String):boolean, dex: 
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
        public boolean isMatch(java.lang.String r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccSource.isMatch(java.lang.String, java.lang.String):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccSource.isMatch(java.lang.String, java.lang.String):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccSource.isMatch(java.lang.String, java.lang.String, java.lang.String):boolean, dex: 
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
        public boolean isMatch(java.lang.String r1, java.lang.String r2, java.lang.String r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccSource.isMatch(java.lang.String, java.lang.String, java.lang.String):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccSource.isMatch(java.lang.String, java.lang.String, java.lang.String):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.telephony.PhoneNumberUtils.EccSource.isPhoneTypeSupport(int):boolean, dex: 
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
        public boolean isPhoneTypeSupport(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.telephony.PhoneNumberUtils.EccSource.isPhoneTypeSupport(int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccSource.isPhoneTypeSupport(int):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccSource.isSimInsert(int):boolean, dex: 
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
        public boolean isSimInsert(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccSource.isSimInsert(int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccSource.isSimInsert(int):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccSource.isSimInsert(int, int):boolean, dex: 
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
        public boolean isSimInsert(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccSource.isSimInsert(int, int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccSource.isSimInsert(int, int):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telephony.PhoneNumberUtils.EccSource.isSpecialEmergencyNumber(int, java.lang.String):boolean, dex: 
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
        public synchronized boolean isSpecialEmergencyNumber(int r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telephony.PhoneNumberUtils.EccSource.isSpecialEmergencyNumber(int, java.lang.String):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccSource.isSpecialEmergencyNumber(int, java.lang.String):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccSource.isSpecialEmergencyNumber(java.lang.String):boolean, dex: 
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
        public synchronized boolean isSpecialEmergencyNumber(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccSource.isSpecialEmergencyNumber(java.lang.String):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccSource.isSpecialEmergencyNumber(java.lang.String):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.EccSource.parseEccList():void, dex: 
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
        public synchronized void parseEccList() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.EccSource.parseEccList():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccSource.parseEccList():void");
        }

        public boolean isEmergencyNumber(String number, int subId, int phoneType) {
            return false;
        }
    }

    private static class CtaEccSource extends EccSource {
        private static String[] sCtaList;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.CtaEccSource.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.CtaEccSource.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.CtaEccSource.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.CtaEccSource.<init>(int):void, dex: 
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
        public CtaEccSource(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.CtaEccSource.<init>(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.CtaEccSource.<init>(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.CtaEccSource.isNeedCheckCtaSet():boolean, dex: 
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
        private boolean isNeedCheckCtaSet() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.CtaEccSource.isNeedCheckCtaSet():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.CtaEccSource.isNeedCheckCtaSet():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.CtaEccSource.isSimLocked():boolean, dex: 
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
        private boolean isSimLocked() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.CtaEccSource.isSimLocked():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.CtaEccSource.isSimLocked():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.CtaEccSource.addToEccList(java.util.ArrayList):void, dex: 
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
        public synchronized void addToEccList(java.util.ArrayList<android.telephony.PhoneNumberUtils.EccEntry> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.CtaEccSource.addToEccList(java.util.ArrayList):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.CtaEccSource.addToEccList(java.util.ArrayList):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.CtaEccSource.isEmergencyNumber(java.lang.String, int, int):boolean, dex: 
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
        public synchronized boolean isEmergencyNumber(java.lang.String r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.CtaEccSource.isEmergencyNumber(java.lang.String, int, int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.CtaEccSource.isEmergencyNumber(java.lang.String, int, int):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telephony.PhoneNumberUtils.CtaEccSource.parseEccList():void, dex: 
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
        public synchronized void parseEccList() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telephony.PhoneNumberUtils.CtaEccSource.parseEccList():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.CtaEccSource.parseEccList():void");
        }

        public synchronized int getServiceCategory(String number, int subId) {
            return -1;
        }
    }

    public static class EccEntry {
        public static final String CATEGORY_ATTR = "Category";
        public static final String CDMA_ECC_LIST_PATH = "/system/vendor/etc/cdma_ecc_list.xml";
        public static final String CDMA_SS_ECC_LIST_PATH = "/system/vendor/etc/cdma_ecc_list_ss.xml";
        public static final String CONDITION_ATTR = "Condition";
        public static final String ECC_ALWAYS = "1";
        public static final String ECC_ATTR = "Ecc";
        public static final String ECC_ENTRY_TAG = "EccEntry";
        public static final String ECC_FOR_MMI = "2";
        public static final String ECC_LIST_PATH = "/system/vendor/etc/ecc_list.xml";
        public static final String ECC_LIST_PATH_CIP = "/custom/etc/ecc_list.xml";
        public static final String ECC_NO_SIM = "0";
        public static final String PLMN_ATTR = "Plmn";
        public static final String PROPERTY_COUNT = "ro.semc.ecclist.num";
        public static final String PROPERTY_NON_ECC = "ro.semc.ecclist.non_ecc.";
        public static final String PROPERTY_NUMBER = "ro.semc.ecclist.number.";
        public static final String PROPERTY_PLMN = "ro.semc.ecclist.plmn.";
        public static final String PROPERTY_PREFIX = "ro.semc.ecclist.";
        public static final String PROPERTY_TYPE = "ro.semc.ecclist.type.";
        public static final String[] PROPERTY_TYPE_KEY = null;
        public static final Short[] PROPERTY_TYPE_VALUE = null;
        private String mCategory;
        private String mCondition;
        private String mEcc;
        private String mName;
        private String mPlmn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.EccEntry.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.EccEntry.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccEntry.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.telephony.PhoneNumberUtils.EccEntry.<init>():void, dex:  in method: android.telephony.PhoneNumberUtils.EccEntry.<init>():void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.telephony.PhoneNumberUtils.EccEntry.<init>():void, dex: 
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
        public EccEntry() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.telephony.PhoneNumberUtils.EccEntry.<init>():void, dex:  in method: android.telephony.PhoneNumberUtils.EccEntry.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccEntry.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telephony.PhoneNumberUtils.EccEntry.<init>(java.lang.String, java.lang.String):void, dex: 
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
        public EccEntry(java.lang.String r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telephony.PhoneNumberUtils.EccEntry.<init>(java.lang.String, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccEntry.<init>(java.lang.String, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telephony.PhoneNumberUtils.EccEntry.getCategory():java.lang.String, dex: 
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
        public java.lang.String getCategory() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telephony.PhoneNumberUtils.EccEntry.getCategory():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccEntry.getCategory():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telephony.PhoneNumberUtils.EccEntry.getCondition():java.lang.String, dex: 
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
        public java.lang.String getCondition() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telephony.PhoneNumberUtils.EccEntry.getCondition():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccEntry.getCondition():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telephony.PhoneNumberUtils.EccEntry.getEcc():java.lang.String, dex: 
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
        public java.lang.String getEcc() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telephony.PhoneNumberUtils.EccEntry.getEcc():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccEntry.getEcc():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.telephony.PhoneNumberUtils.EccEntry.getName():java.lang.String, dex:  in method: android.telephony.PhoneNumberUtils.EccEntry.getName():java.lang.String, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.telephony.PhoneNumberUtils.EccEntry.getName():java.lang.String, dex: 
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
        public java.lang.String getName() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.telephony.PhoneNumberUtils.EccEntry.getName():java.lang.String, dex:  in method: android.telephony.PhoneNumberUtils.EccEntry.getName():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccEntry.getName():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.telephony.PhoneNumberUtils.EccEntry.getPlmn():java.lang.String, dex:  in method: android.telephony.PhoneNumberUtils.EccEntry.getPlmn():java.lang.String, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.telephony.PhoneNumberUtils.EccEntry.getPlmn():java.lang.String, dex: 
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
        public java.lang.String getPlmn() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.telephony.PhoneNumberUtils.EccEntry.getPlmn():java.lang.String, dex:  in method: android.telephony.PhoneNumberUtils.EccEntry.getPlmn():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccEntry.getPlmn():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telephony.PhoneNumberUtils.EccEntry.setCategory(java.lang.String):void, dex: 
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
        public void setCategory(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telephony.PhoneNumberUtils.EccEntry.setCategory(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccEntry.setCategory(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telephony.PhoneNumberUtils.EccEntry.setCondition(java.lang.String):void, dex: 
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
        public void setCondition(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telephony.PhoneNumberUtils.EccEntry.setCondition(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccEntry.setCondition(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telephony.PhoneNumberUtils.EccEntry.setEcc(java.lang.String):void, dex: 
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
        public void setEcc(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telephony.PhoneNumberUtils.EccEntry.setEcc(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccEntry.setEcc(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.telephony.PhoneNumberUtils.EccEntry.setName(java.lang.String):void, dex:  in method: android.telephony.PhoneNumberUtils.EccEntry.setName(java.lang.String):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.telephony.PhoneNumberUtils.EccEntry.setName(java.lang.String):void, dex: 
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
        public void setName(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.telephony.PhoneNumberUtils.EccEntry.setName(java.lang.String):void, dex:  in method: android.telephony.PhoneNumberUtils.EccEntry.setName(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccEntry.setName(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.telephony.PhoneNumberUtils.EccEntry.setPlmn(java.lang.String):void, dex:  in method: android.telephony.PhoneNumberUtils.EccEntry.setPlmn(java.lang.String):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.telephony.PhoneNumberUtils.EccEntry.setPlmn(java.lang.String):void, dex: 
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
        public void setPlmn(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.telephony.PhoneNumberUtils.EccEntry.setPlmn(java.lang.String):void, dex:  in method: android.telephony.PhoneNumberUtils.EccEntry.setPlmn(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccEntry.setPlmn(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccEntry.toString():java.lang.String, dex: 
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
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.EccEntry.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.EccEntry.toString():java.lang.String");
        }
    }

    private static class NetworkEccSource extends EccSource {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.NetworkEccSource.<init>(int):void, dex: 
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
        public NetworkEccSource(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.NetworkEccSource.<init>(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.NetworkEccSource.<init>(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.NetworkEccSource.getServiceCategory(java.lang.String, int):int, dex: 
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
        public synchronized int getServiceCategory(java.lang.String r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.NetworkEccSource.getServiceCategory(java.lang.String, int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.NetworkEccSource.getServiceCategory(java.lang.String, int):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.NetworkEccSource.isEmergencyNumber(java.lang.String, int, int):boolean, dex: 
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
        public synchronized boolean isEmergencyNumber(java.lang.String r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.NetworkEccSource.isEmergencyNumber(java.lang.String, int, int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.NetworkEccSource.isEmergencyNumber(java.lang.String, int, int):boolean");
        }
    }

    private static class OmhEccSource extends EccSource {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.OmhEccSource.<init>(int):void, dex: 
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
        public OmhEccSource(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.OmhEccSource.<init>(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.OmhEccSource.<init>(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.OmhEccSource.isEmergencyNumber(java.lang.String, int, int):boolean, dex: 
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
        public synchronized boolean isEmergencyNumber(java.lang.String r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.OmhEccSource.isEmergencyNumber(java.lang.String, int, int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.OmhEccSource.isEmergencyNumber(java.lang.String, int, int):boolean");
        }
    }

    private static class PropertyEccSource extends EccSource {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.PropertyEccSource.<init>(int):void, dex: 
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
        public PropertyEccSource(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.PropertyEccSource.<init>(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.PropertyEccSource.<init>(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.PropertyEccSource.isEmergencyNumber(java.lang.String, int, int):boolean, dex: 
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
        public synchronized boolean isEmergencyNumber(java.lang.String r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.PropertyEccSource.isEmergencyNumber(java.lang.String, int, int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.PropertyEccSource.isEmergencyNumber(java.lang.String, int, int):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.PropertyEccSource.parseEccList():void, dex: 
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
        public synchronized void parseEccList() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.PropertyEccSource.parseEccList():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.PropertyEccSource.parseEccList():void");
        }
    }

    private static class SimEccSource extends EccSource {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.SimEccSource.<init>(int):void, dex: 
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
        public SimEccSource(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.SimEccSource.<init>(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.SimEccSource.<init>(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.SimEccSource.getServiceCategory(java.lang.String, int):int, dex: 
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
        public synchronized int getServiceCategory(java.lang.String r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.SimEccSource.getServiceCategory(java.lang.String, int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.SimEccSource.getServiceCategory(java.lang.String, int):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.SimEccSource.isEmergencyNumber(java.lang.String, int, int):boolean, dex: 
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
        public synchronized boolean isEmergencyNumber(java.lang.String r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.SimEccSource.isEmergencyNumber(java.lang.String, int, int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.SimEccSource.isEmergencyNumber(java.lang.String, int, int):boolean");
        }
    }

    private static class TestEccSource extends EccSource {
        private static final String TEST_ECC_LIST = "persist.radio.mtk.testecc";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.TestEccSource.<init>(int):void, dex: 
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
        public TestEccSource(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.TestEccSource.<init>(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.TestEccSource.<init>(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.TestEccSource.isEmergencyNumber(java.lang.String, int, int):boolean, dex: 
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
        public synchronized boolean isEmergencyNumber(java.lang.String r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.TestEccSource.isEmergencyNumber(java.lang.String, int, int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.TestEccSource.isEmergencyNumber(java.lang.String, int, int):boolean");
        }
    }

    private static class XmlEccSource extends EccSource {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.XmlEccSource.<init>(int):void, dex: 
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
        public XmlEccSource(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberUtils.XmlEccSource.<init>(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.XmlEccSource.<init>(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.XmlEccSource.parseFromXml(java.lang.String, java.util.ArrayList):void, dex: 
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
        private synchronized void parseFromXml(java.lang.String r1, java.util.ArrayList<android.telephony.PhoneNumberUtils.EccEntry> r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.XmlEccSource.parseFromXml(java.lang.String, java.util.ArrayList):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.XmlEccSource.parseFromXml(java.lang.String, java.util.ArrayList):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telephony.PhoneNumberUtils.XmlEccSource.getServiceCategory(java.lang.String, int):int, dex: 
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
        public synchronized int getServiceCategory(java.lang.String r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telephony.PhoneNumberUtils.XmlEccSource.getServiceCategory(java.lang.String, int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.XmlEccSource.getServiceCategory(java.lang.String, int):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telephony.PhoneNumberUtils.XmlEccSource.isEmergencyNumber(java.lang.String, int, int):boolean, dex: 
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
        public synchronized boolean isEmergencyNumber(java.lang.String r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telephony.PhoneNumberUtils.XmlEccSource.isEmergencyNumber(java.lang.String, int, int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.XmlEccSource.isEmergencyNumber(java.lang.String, int, int):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.XmlEccSource.isMatch(java.lang.String, java.lang.String, java.lang.String):boolean, dex: 
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
        public boolean isMatch(java.lang.String r1, java.lang.String r2, java.lang.String r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.XmlEccSource.isMatch(java.lang.String, java.lang.String, java.lang.String):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.XmlEccSource.isMatch(java.lang.String, java.lang.String, java.lang.String):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telephony.PhoneNumberUtils.XmlEccSource.parseEccList():void, dex: 
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
        public synchronized void parseEccList() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telephony.PhoneNumberUtils.XmlEccSource.parseEccList():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.XmlEccSource.parseEccList():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.PhoneNumberUtils.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.<clinit>():void");
    }

    public static boolean isISODigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static final boolean is12Key(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#';
    }

    public static final boolean isDialable(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == PLUS_SIGN_CHAR || c == WILD;
    }

    public static final boolean isReallyDialable(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == PLUS_SIGN_CHAR;
    }

    public static final boolean isNonSeparator(char c) {
        if ((c >= '0' && c <= '9') || c == '*' || c == '#' || c == PLUS_SIGN_CHAR || c == WILD || c == ';' || c == ',') {
            return true;
        }
        return false;
    }

    public static final boolean isStartsPostDial(char c) {
        return c == ',' || c == ';';
    }

    private static boolean isPause(char c) {
        return c == 'p' || c == 'P';
    }

    private static boolean isToneWait(char c) {
        return c == 'w' || c == 'W';
    }

    private static boolean isSeparator(char ch) {
        boolean z = true;
        if (isDialable(ch)) {
            return false;
        }
        if (DateFormat.AM_PM <= ch && ch <= DateFormat.TIME_ZONE) {
            return false;
        }
        if (DateFormat.CAPITAL_AM_PM <= ch && ch <= 'Z') {
            z = false;
        }
        return z;
    }

    public static String getNumberFromIntent(Intent intent, Context context) {
        String number = null;
        Uri uri = intent.getData();
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        int subscription = 0;
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            subscription = intent.getIntExtra("subscription", SubscriptionManager.getDefaultVoicePhoneId());
        }
        if (scheme.equals(PhoneAccount.SCHEME_TEL) || scheme.equals(PhoneAccount.SCHEME_SIP)) {
            return uri.getSchemeSpecificPart();
        }
        if (scheme.equals(PhoneAccount.SCHEME_VOICEMAIL)) {
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                int[] subId = SubscriptionManager.getSubId(subscription);
                if (subId != null && subId[0] > 0) {
                    return TelephonyManager.getDefault().getCompleteVoiceMailNumber(subId[0]);
                }
            }
            return TelephonyManager.getDefault().getCompleteVoiceMailNumber();
        } else if (context == null) {
            return null;
        } else {
            String type = intent.resolveType(context);
            String phoneColumn = null;
            String authority = uri.getAuthority();
            if ("contacts".equals(authority)) {
                phoneColumn = "number";
            } else if ("com.android.contacts".equals(authority)) {
                phoneColumn = "data1";
            }
            Cursor cursor = null;
            try {
                ContentResolver contentResolver = context.getContentResolver();
                String[] strArr = new String[1];
                strArr[0] = phoneColumn;
                cursor = contentResolver.query(uri, strArr, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    number = cursor.getString(cursor.getColumnIndex(phoneColumn));
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (RuntimeException e) {
                Rlog.e(LOG_TAG, "Error getting phone number.", e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return number;
        }
    }

    public static String extractNetworkPortion(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                ret.append(digit);
            } else if (c == PLUS_SIGN_CHAR) {
                String prefix = ret.toString();
                if (prefix.length() == 0 || prefix.equals(CLIR_ON) || prefix.equals(CLIR_OFF)) {
                    ret.append(c);
                }
            } else if (isDialable(c)) {
                ret.append(c);
            } else if (isStartsPostDial(c)) {
                break;
            }
        }
        return ret.toString();
    }

    public static String extractNetworkPortionAlt(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        boolean haveSeenPlus = false;
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            if (c == PLUS_SIGN_CHAR) {
                if (haveSeenPlus) {
                    continue;
                } else {
                    haveSeenPlus = true;
                }
            }
            if (isDialable(c)) {
                ret.append(c);
            } else if (isStartsPostDial(c)) {
                break;
            }
        }
        vlog("[extractNetworkPortionAlt] phoneNumber: " + ret.toString());
        return ret.toString();
    }

    public static String stripSeparators(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                ret.append(digit);
            } else if (isNonSeparator(c)) {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    public static String convertAndStrip(String phoneNumber) {
        return stripSeparators(convertKeypadLettersToDigits(phoneNumber));
    }

    public static String convertPreDial(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            if (isPause(c)) {
                c = ',';
            } else if (isToneWait(c)) {
                c = ';';
            }
            ret.append(c);
        }
        return ret.toString();
    }

    private static int minPositive(int a, int b) {
        if (a >= 0 && b >= 0) {
            if (a >= b) {
                a = b;
            }
            return a;
        } else if (a >= 0) {
            return a;
        } else {
            if (b >= 0) {
                return b;
            }
            return -1;
        }
    }

    private static void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private static int indexOfLastNetworkChar(String a) {
        int origLength = a.length();
        int trimIndex = minPositive(a.indexOf(44), a.indexOf(59));
        if (trimIndex < 0) {
            return origLength - 1;
        }
        return trimIndex - 1;
    }

    public static String extractPostDialPortion(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        StringBuilder ret = new StringBuilder();
        int s = phoneNumber.length();
        for (int i = indexOfLastNetworkChar(phoneNumber) + 1; i < s; i++) {
            char c = phoneNumber.charAt(i);
            if (isNonSeparator(c)) {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    public static boolean compare(String a, String b) {
        return compare(a, b, false);
    }

    public static boolean compare(Context context, String a, String b) {
        return compare(a, b, context.getResources().getBoolean(R.bool.config_use_strict_phone_number_comparation));
    }

    public static boolean compare(String a, String b, boolean useStrictComparation) {
        return useStrictComparation ? compareStrictly(a, b) : compareLoosely(a, b);
    }

    public static boolean compareLoosely(String a, String b) {
        int numNonDialableCharsInA = 0;
        int numNonDialableCharsInB = 0;
        if (a == null || b == null) {
            return a == b;
        } else if (a.length() == 0 || b.length() == 0) {
            return false;
        } else {
            int minMatchLen;
            int ia = indexOfLastNetworkChar(a);
            int ib = indexOfLastNetworkChar(b);
            int matched = 0;
            while (ia >= 0 && ib >= 0) {
                boolean skipCmp = false;
                char ca = a.charAt(ia);
                if (!isDialable(ca)) {
                    ia--;
                    skipCmp = true;
                    numNonDialableCharsInA++;
                }
                char cb = b.charAt(ib);
                if (!isDialable(cb)) {
                    ib--;
                    skipCmp = true;
                    numNonDialableCharsInB++;
                }
                if (!skipCmp) {
                    if (cb != ca && ca != WILD && cb != WILD) {
                        break;
                    }
                    ia--;
                    ib--;
                    matched++;
                }
            }
            if (sIsCtaSupport || sIsOP09Support) {
                minMatchLen = 11;
            } else if (SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("CN")) {
                minMatchLen = 11;
            } else {
                minMatchLen = 7;
            }
            vlog("[compareLoosely] a: " + a + ", b: " + b + ", minMatchLen:" + minMatchLen);
            if (matched < minMatchLen) {
                int effectiveALen = a.length() - numNonDialableCharsInA;
                if (effectiveALen == b.length() - numNonDialableCharsInB && effectiveALen == matched) {
                    return true;
                }
                vlog("[compareLoosely] return: false");
                return false;
            } else if (matched >= minMatchLen && (ia < 0 || ib < 0)) {
                return true;
            } else {
                if (matchIntlPrefix(a, ia + 1) && matchIntlPrefix(b, ib + 1)) {
                    return true;
                }
                if (matchTrunkPrefix(a, ia + 1) && matchIntlPrefixAndCC(b, ib + 1)) {
                    return true;
                }
                if (matchTrunkPrefix(b, ib + 1) && matchIntlPrefixAndCC(a, ia + 1)) {
                    return true;
                }
                vlog("[compareLoosely] return: false");
                return false;
            }
        }
    }

    public static boolean compareStrictly(String a, String b) {
        return compareStrictly(a, b, true);
    }

    public static boolean compareStrictly(String a, String b, boolean acceptInvalidCCCPrefix) {
        if (a == null || b == null) {
            return a == b;
        } else if (a.length() == 0 && b.length() == 0) {
            return false;
        } else {
            char chA;
            char chB;
            int forwardIndexA = 0;
            int forwardIndexB = 0;
            CountryCallingCodeAndNewIndex cccA = tryGetCountryCallingCodeAndNewIndex(a, acceptInvalidCCCPrefix);
            CountryCallingCodeAndNewIndex cccB = tryGetCountryCallingCodeAndNewIndex(b, acceptInvalidCCCPrefix);
            boolean bothHasCountryCallingCode = false;
            boolean okToIgnorePrefix = true;
            boolean trunkPrefixIsOmittedA = false;
            boolean trunkPrefixIsOmittedB = false;
            if (cccA != null && cccB != null) {
                if (cccA.countryCallingCode != cccB.countryCallingCode) {
                    return false;
                }
                okToIgnorePrefix = false;
                bothHasCountryCallingCode = true;
                forwardIndexA = cccA.newIndex;
                forwardIndexB = cccB.newIndex;
            } else if (cccA == null && cccB == null) {
                okToIgnorePrefix = false;
            } else {
                int tmp;
                if (cccA != null) {
                    forwardIndexA = cccA.newIndex;
                } else {
                    tmp = tryGetTrunkPrefixOmittedIndex(b, 0);
                    if (tmp >= 0) {
                        forwardIndexA = tmp;
                        trunkPrefixIsOmittedA = true;
                    }
                }
                if (cccB != null) {
                    forwardIndexB = cccB.newIndex;
                } else {
                    tmp = tryGetTrunkPrefixOmittedIndex(b, 0);
                    if (tmp >= 0) {
                        forwardIndexB = tmp;
                        trunkPrefixIsOmittedB = true;
                    }
                }
            }
            int backwardIndexA = a.length() - 1;
            int backwardIndexB = b.length() - 1;
            while (backwardIndexA >= forwardIndexA && backwardIndexB >= forwardIndexB) {
                boolean skip_compare = false;
                chA = a.charAt(backwardIndexA);
                chB = b.charAt(backwardIndexB);
                if (isSeparator(chA)) {
                    backwardIndexA--;
                    skip_compare = true;
                }
                if (isSeparator(chB)) {
                    backwardIndexB--;
                    skip_compare = true;
                }
                if (!skip_compare) {
                    if (chA != chB) {
                        return false;
                    }
                    backwardIndexA--;
                    backwardIndexB--;
                }
            }
            if (!okToIgnorePrefix) {
                boolean maybeNamp = !bothHasCountryCallingCode;
                while (backwardIndexA >= forwardIndexA) {
                    chA = a.charAt(backwardIndexA);
                    if (isDialable(chA)) {
                        if (!maybeNamp || tryGetISODigit(chA) != 1) {
                            return false;
                        }
                        maybeNamp = false;
                    }
                    backwardIndexA--;
                }
                while (backwardIndexB >= forwardIndexB) {
                    chB = b.charAt(backwardIndexB);
                    if (isDialable(chB)) {
                        if (!maybeNamp || tryGetISODigit(chB) != 1) {
                            return false;
                        }
                        maybeNamp = false;
                    }
                    backwardIndexB--;
                }
            } else if ((!trunkPrefixIsOmittedA || forwardIndexA > backwardIndexA) && checkPrefixIsIgnorable(a, forwardIndexA, backwardIndexA)) {
                if ((trunkPrefixIsOmittedB && forwardIndexB <= backwardIndexB) || !checkPrefixIsIgnorable(b, forwardIndexA, backwardIndexB)) {
                    if (acceptInvalidCCCPrefix) {
                        return compare(a, b, false);
                    }
                    return false;
                }
            } else if (acceptInvalidCCCPrefix) {
                return compare(a, b, false);
            } else {
                return false;
            }
            return true;
        }
    }

    public static String toCallerIDMinMatch(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return phoneNumber;
        }
        int minMatchLen;
        String np = extractNetworkPortionAlt(phoneNumber);
        if (sIsCtaSupport || sIsOP09Support) {
            minMatchLen = 11;
        } else if (SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("CN")) {
            minMatchLen = 11;
        } else {
            minMatchLen = 7;
        }
        String strStrippedReversed = internalGetStrippedReversed(np, minMatchLen);
        vlog("[toCallerIDMinMatch] phoneNumber: " + phoneNumber + ", minMatchLen: " + minMatchLen + ", result:" + strStrippedReversed);
        return strStrippedReversed;
    }

    public static String getStrippedReversed(String phoneNumber) {
        String np = extractNetworkPortionAlt(phoneNumber);
        if (np == null) {
            return null;
        }
        return internalGetStrippedReversed(np, np.length());
    }

    private static String internalGetStrippedReversed(String np, int numDigits) {
        if (np == null) {
            return null;
        }
        StringBuilder ret = new StringBuilder(numDigits);
        int length = np.length();
        int i = length - 1;
        int s = length;
        while (i >= 0 && length - i <= numDigits) {
            ret.append(np.charAt(i));
            i--;
        }
        return ret.toString();
    }

    public static String stringFromStringAndTOA(String s, int TOA) {
        if (s == null) {
            return null;
        }
        if (TOA != 145 || s.length() <= 0 || s.charAt(0) == PLUS_SIGN_CHAR) {
            return s;
        }
        return PLUS_SIGN_STRING + s;
    }

    public static int toaFromString(String s) {
        if (s == null || s.length() <= 0 || s.charAt(0) != PLUS_SIGN_CHAR) {
            return 129;
        }
        return 145;
    }

    public static String calledPartyBCDToString(byte[] bytes, int offset, int length) {
        boolean prependPlus = false;
        StringBuilder ret = new StringBuilder((length * 2) + 1);
        if (length < 2) {
            return PhoneConstants.MVNO_TYPE_NONE;
        }
        if ((bytes[offset] & 240) == 144) {
            prependPlus = true;
        }
        internalCalledPartyBCDFragmentToString(ret, bytes, offset + 1, length - 1);
        if (!prependPlus) {
            return ret.toString();
        }
        if (ret.length() == 0) {
            return PhoneConstants.MVNO_TYPE_NONE;
        }
        return prependPlusToNumber(ret.toString());
    }

    private static void internalCalledPartyBCDFragmentToString(StringBuilder sb, byte[] bytes, int offset, int length) {
        int i = offset;
        while (i < length + offset) {
            char c = bcdToChar((byte) (bytes[i] & 15));
            if (c != 0) {
                sb.append(c);
                byte b = (byte) ((bytes[i] >> 4) & 15);
                if (b == MidiConstants.STATUS_CHANNEL_MASK && i + 1 == length + offset) {
                    break;
                }
                c = bcdToChar(b);
                if (c != 0) {
                    sb.append(c);
                    i++;
                } else {
                    return;
                }
            }
            return;
        }
    }

    public static String calledPartyBCDFragmentToString(byte[] bytes, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * 2);
        internalCalledPartyBCDFragmentToString(ret, bytes, offset, length);
        return ret.toString();
    }

    private static char bcdToChar(byte b) {
        if (b < (byte) 10) {
            return (char) (b + 48);
        }
        switch (b) {
            case (byte) 10:
                return '*';
            case (byte) 11:
                return '#';
            case (byte) 12:
                return ',';
            case (byte) 13:
                return WILD;
            case (byte) 14:
                return ';';
            default:
                return 0;
        }
    }

    private static int charToBCD(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c == '*') {
            return 10;
        }
        if (c == '#') {
            return 11;
        }
        if (c == ',') {
            return 12;
        }
        if (c == WILD) {
            return 13;
        }
        if (c == ';') {
            return 14;
        }
        throw new RuntimeException("invalid char for BCD " + c);
    }

    public static boolean isWellFormedSmsAddress(String address) {
        boolean z;
        String networkPortion = extractNetworkPortion(address);
        if (networkPortion.equals(PLUS_SIGN_STRING)) {
            z = true;
        } else {
            z = TextUtils.isEmpty(networkPortion);
        }
        if (z) {
            return false;
        }
        return isDialable(networkPortion);
    }

    public static boolean isGlobalPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }
        return GLOBAL_PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches();
    }

    private static boolean isDialable(String address) {
        int count = address.length();
        for (int i = 0; i < count; i++) {
            if (!isDialable(address.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNonSeparator(String address) {
        int count = address.length();
        for (int i = 0; i < count; i++) {
            if (!isNonSeparator(address.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static byte[] networkPortionToCalledPartyBCD(String s) {
        return numberToCalledPartyBCDHelper(extractNetworkPortion(s), false);
    }

    public static byte[] networkPortionToCalledPartyBCDWithLength(String s) {
        return numberToCalledPartyBCDHelper(extractNetworkPortion(s), true);
    }

    public static byte[] numberToCalledPartyBCD(String number) {
        number = stripSeparators(number);
        if (number == null) {
            return null;
        }
        dlog("numberToCalledPartyBCD(),after--number:" + number);
        return numberToCalledPartyBCDHelper(number, false);
    }

    private static byte[] numberToCalledPartyBCDHelper(String number, boolean includeLength) {
        int numberLenReal = number.length();
        int numberLenEffective = numberLenReal;
        boolean hasPlus = number.indexOf(43) != -1;
        if (hasPlus) {
            numberLenEffective = numberLenReal - 1;
        }
        if (numberLenEffective == 0) {
            return null;
        }
        int i;
        int resultLen = (numberLenEffective + 1) / 2;
        int extraBytes = 1;
        if (includeLength) {
            extraBytes = 2;
        }
        resultLen += extraBytes;
        byte[] result = new byte[resultLen];
        int digitCount = 0;
        for (int i2 = 0; i2 < numberLenReal; i2++) {
            char c = number.charAt(i2);
            if (c != PLUS_SIGN_CHAR) {
                i = (digitCount >> 1) + extraBytes;
                result[i] = (byte) (result[i] | ((byte) ((charToBCD(c) & 15) << ((digitCount & 1) == 1 ? 4 : 0))));
                digitCount++;
            }
        }
        if ((digitCount & 1) == 1) {
            i = (digitCount >> 1) + extraBytes;
            result[i] = (byte) (result[i] | 240);
        }
        int offset = 0;
        if (includeLength) {
            offset = 1;
            result[0] = (byte) (resultLen - 1);
        }
        result[offset] = (byte) (hasPlus ? 145 : 129);
        return result;
    }

    @Deprecated
    public static String formatNumber(String source) {
        Editable text = new SpannableStringBuilder(source);
        formatNumber(text, getFormatTypeForLocale(Locale.getDefault()));
        return text.toString();
    }

    @Deprecated
    public static String formatNumber(String source, int defaultFormattingType) {
        Editable text = new SpannableStringBuilder(source);
        formatNumber(text, defaultFormattingType);
        return text.toString();
    }

    @Deprecated
    public static int getFormatTypeForLocale(Locale locale) {
        return getFormatTypeFromCountryCode(locale.getCountry());
    }

    @Deprecated
    public static void formatNumber(Editable text, int defaultFormattingType) {
        int formatType = defaultFormattingType;
        if (text.length() > 2 && text.charAt(0) == PLUS_SIGN_CHAR) {
            formatType = text.charAt(1) == '1' ? 1 : (text.length() >= 3 && text.charAt(1) == '8' && text.charAt(2) == '1') ? 2 : 0;
        }
        switch (formatType) {
            case 0:
                removeDashes(text);
                return;
            case 1:
                formatNanpNumber(text);
                return;
            case 2:
                formatJapaneseNumber(text);
                return;
            default:
                return;
        }
    }

    /* JADX WARNING: Missing block: B:15:0x003a, code:
            r2 = r2 + 1;
            r6 = r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @Deprecated
    public static void formatNanpNumber(Editable text) {
        int length = text.length();
        if (length <= "+1-nnn-nnn-nnnn".length() && length > 5) {
            int numDashes;
            CharSequence saved = text.subSequence(0, length);
            removeDashes(text);
            length = text.length();
            int[] dashPositions = new int[3];
            int state = 1;
            int numDigits = 0;
            int i = 0;
            int numDashes2 = 0;
            while (i < length) {
                switch (text.charAt(i)) {
                    case '+':
                        if (i != 0) {
                            break;
                        }
                        state = 2;
                        numDashes = numDashes2;
                        continue;
                    case '-':
                        state = 4;
                        numDashes = numDashes2;
                        continue;
                    case '1':
                        if (numDigits == 0 || state == 2) {
                            state = 3;
                            numDashes = numDashes2;
                            continue;
                        }
                    case '0':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        if (state == 2) {
                            text.replace(0, length, saved);
                            return;
                        }
                        if (state == 3) {
                            numDashes = numDashes2 + 1;
                            dashPositions[numDashes2] = i;
                        } else if (state == 4 || !(numDigits == 3 || numDigits == 6)) {
                            numDashes = numDashes2;
                        } else {
                            numDashes = numDashes2 + 1;
                            dashPositions[numDashes2] = i;
                        }
                        state = 1;
                        numDigits++;
                        continue;
                    default:
                        break;
                }
                text.replace(0, length, saved);
                return;
            }
            if (numDigits == 7) {
                numDashes = numDashes2 - 1;
            } else {
                numDashes = numDashes2;
            }
            for (i = 0; i < numDashes; i++) {
                int pos = dashPositions[i];
                text.replace(pos + i, pos + i, NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
            }
            int len = text.length();
            while (len > 0 && text.charAt(len - 1) == '-') {
                text.delete(len - 1, len);
                len--;
            }
        }
    }

    @Deprecated
    public static void formatJapaneseNumber(Editable text) {
        JapanesePhoneNumberFormatter.format(text);
    }

    private static void removeDashes(Editable text) {
        int p = 0;
        while (p < text.length()) {
            if (text.charAt(p) == '-') {
                text.delete(p, p + 1);
            } else {
                p++;
            }
        }
    }

    public static String formatNumberToE164(String phoneNumber, String defaultCountryIso) {
        return formatNumberInternal(phoneNumber, defaultCountryIso, PhoneNumberFormat.E164);
    }

    public static String formatNumberToRFC3966(String phoneNumber, String defaultCountryIso) {
        return formatNumberInternal(phoneNumber, defaultCountryIso, PhoneNumberFormat.RFC3966);
    }

    private static String formatNumberInternal(String rawPhoneNumber, String defaultCountryIso, PhoneNumberFormat formatIdentifier) {
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        try {
            PhoneNumber phoneNumber = util.parse(rawPhoneNumber, defaultCountryIso);
            if (util.isValidNumber(phoneNumber)) {
                if (formatIdentifier == PhoneNumberFormat.RFC3966) {
                    String postDial = extractPostDialPortion(rawPhoneNumber);
                    if (postDial != null && postDial.length() > 0) {
                        phoneNumber = new PhoneNumber().mergeFrom(phoneNumber).setExtension(postDial.substring(1));
                    }
                }
                return util.format(phoneNumber, formatIdentifier);
            }
        } catch (NumberParseException e) {
        }
        return null;
    }

    public static String formatNumber(String phoneNumber, String defaultCountryIso) {
        if (phoneNumber.startsWith("#") || phoneNumber.startsWith(PhoneConstants.APN_TYPE_ALL)) {
            return phoneNumber;
        }
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        String result = null;
        try {
            PhoneNumber pn = util.parseAndKeepRawInput(phoneNumber, defaultCountryIso);
            if (KOREA_ISO_COUNTRY_CODE.equals(defaultCountryIso) && pn.getCountryCode() == util.getCountryCodeForRegion(KOREA_ISO_COUNTRY_CODE) && pn.getCountryCodeSource() == CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN) {
                result = util.format(pn, PhoneNumberFormat.NATIONAL);
            } else {
                result = util.formatInOriginalFormat(pn, defaultCountryIso);
            }
        } catch (NumberParseException e) {
        }
        return result;
    }

    public static String formatNumber(String phoneNumber, String phoneNumberE164, String defaultCountryIso) {
        int len = phoneNumber.length();
        for (int i = 0; i < len; i++) {
            if (!isDialable(phoneNumber.charAt(i))) {
                return phoneNumber;
            }
        }
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        if (phoneNumberE164 != null && phoneNumberE164.length() >= 2 && phoneNumberE164.charAt(0) == PLUS_SIGN_CHAR) {
            try {
                String regionCode = util.getRegionCodeForNumber(util.parse(phoneNumberE164, "ZZ"));
                if (!TextUtils.isEmpty(regionCode) && normalizeNumber(phoneNumber).indexOf(phoneNumberE164.substring(1)) <= 0) {
                    defaultCountryIso = regionCode;
                }
            } catch (NumberParseException e) {
            }
        }
        String result = formatNumber(phoneNumber, defaultCountryIso);
        if (result == null) {
            result = phoneNumber;
        }
        return result;
    }

    public static String normalizeNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return PhoneConstants.MVNO_TYPE_NONE;
        }
        StringBuilder sb = new StringBuilder();
        int len = phoneNumber.length();
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                sb.append(digit);
            } else if (sb.length() == 0 && c == PLUS_SIGN_CHAR) {
                sb.append(c);
            } else if ((c >= DateFormat.AM_PM && c <= DateFormat.TIME_ZONE) || (c >= DateFormat.CAPITAL_AM_PM && c <= 'Z')) {
                return normalizeNumber(convertKeypadLettersToDigits(phoneNumber));
            }
        }
        return sb.toString();
    }

    public static String replaceUnicodeDigits(String number) {
        StringBuilder normalizedDigits = new StringBuilder(number.length());
        for (char c : number.toCharArray()) {
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                normalizedDigits.append(digit);
            } else {
                normalizedDigits.append(c);
            }
        }
        return normalizedDigits.toString();
    }

    public static boolean isEmergencyNumber(String number) {
        return isEmergencyNumber(getDefaultVoiceSubId(), number);
    }

    public static boolean isEmergencyNumber(int subId, String number) {
        return isEmergencyNumberInternal(subId, number, true);
    }

    public static boolean isPotentialEmergencyNumber(String number) {
        return isPotentialEmergencyNumber(getDefaultVoiceSubId(), number);
    }

    public static boolean isPotentialEmergencyNumber(int subId, String number) {
        return isEmergencyNumberInternal(subId, number, false);
    }

    private static boolean isEmergencyNumberInternal(String number, boolean useExactMatch) {
        return isEmergencyNumberInternal(getDefaultVoiceSubId(), number, useExactMatch);
    }

    private static boolean isEmergencyNumberInternal(int subId, String number, boolean useExactMatch) {
        return isEmergencyNumberInternal(subId, number, null, useExactMatch);
    }

    public static boolean isEmergencyNumber(String number, String defaultCountryIso) {
        return isEmergencyNumber(getDefaultVoiceSubId(), number, defaultCountryIso);
    }

    public static boolean isEmergencyNumber(int subId, String number, String defaultCountryIso) {
        return isEmergencyNumberInternal(subId, number, defaultCountryIso, true);
    }

    public static boolean isPotentialEmergencyNumber(String number, String defaultCountryIso) {
        return isPotentialEmergencyNumber(getDefaultVoiceSubId(), number, defaultCountryIso);
    }

    public static boolean isPotentialEmergencyNumber(int subId, String number, String defaultCountryIso) {
        return isEmergencyNumberInternal(subId, number, defaultCountryIso, false);
    }

    private static boolean isEmergencyNumberInternal(String number, String defaultCountryIso, boolean useExactMatch) {
        return isEmergencyNumberInternal(getDefaultVoiceSubId(), number, defaultCountryIso, useExactMatch);
    }

    private static boolean isEmergencyNumberInternal(int subId, String number, String defaultCountryIso, boolean useExactMatch) {
        return isEmergencyNumberExt(subId, number, defaultCountryIso, useExactMatch);
    }

    public static boolean isLocalEmergencyNumber(Context context, String number) {
        return isLocalEmergencyNumber(context, getDefaultVoiceSubId(), number);
    }

    public static boolean isLocalEmergencyNumber(Context context, int subId, String number) {
        return isLocalEmergencyNumberInternal(subId, number, context, true);
    }

    public static boolean isPotentialLocalEmergencyNumber(Context context, String number) {
        return isPotentialLocalEmergencyNumber(context, getDefaultVoiceSubId(), number);
    }

    public static boolean isPotentialLocalEmergencyNumber(Context context, int subId, String number) {
        return isLocalEmergencyNumberInternal(subId, number, context, false);
    }

    private static boolean isLocalEmergencyNumberInternal(String number, Context context, boolean useExactMatch) {
        return isLocalEmergencyNumberInternal(getDefaultVoiceSubId(), number, context, useExactMatch);
    }

    private static boolean isLocalEmergencyNumberInternal(int subId, String number, Context context, boolean useExactMatch) {
        String countryIso;
        CountryDetector detector = (CountryDetector) context.getSystemService("country_detector");
        if (detector == null || detector.detectCountry() == null) {
            countryIso = context.getResources().getConfiguration().locale.getCountry();
            Rlog.w(LOG_TAG, "No CountryDetector; falling back to countryIso based on locale: " + countryIso);
        } else {
            countryIso = detector.detectCountry().getCountryIso();
        }
        return isEmergencyNumberInternal(subId, number, countryIso, useExactMatch);
    }

    public static boolean isVoiceMailNumber(String number) {
        return isVoiceMailNumber(SubscriptionManager.getDefaultSubscriptionId(), number);
    }

    public static boolean isVoiceMailNumber(int subId, String number) {
        return isVoiceMailNumber(null, subId, number);
    }

    public static boolean isVoiceMailNumber(Context context, int subId, String number) {
        TelephonyManager tm;
        boolean z = false;
        if (context == null) {
            try {
                tm = TelephonyManager.getDefault();
            } catch (SecurityException e) {
                return false;
            }
        }
        tm = TelephonyManager.from(context);
        String vmNumber = tm.getVoiceMailNumber(subId);
        number = extractNetworkPortionAlt(number);
        if (!TextUtils.isEmpty(number)) {
            z = compare(number, vmNumber);
        }
        return z;
    }

    public static String convertKeypadLettersToDigits(String input) {
        if (input == null) {
            return input;
        }
        int len = input.length();
        if (len == 0) {
            return input;
        }
        char[] out = input.toCharArray();
        for (int i = 0; i < len; i++) {
            char c = out[i];
            out[i] = (char) KEYPAD_MAP.get(c, c);
        }
        return new String(out);
    }

    public static String cdmaCheckAndProcessPlusCode(String dialStr) {
        String result = preProcessPlusCode(dialStr);
        if (result != null && !result.equals(dialStr)) {
            return result;
        }
        if (!TextUtils.isEmpty(dialStr) && isReallyDialable(dialStr.charAt(0)) && isNonSeparator(dialStr)) {
            String currIso = TelephonyManager.getDefault().getNetworkCountryIso();
            String defaultIso = TelephonyManager.getDefault().getSimCountryIso();
            if (!(TextUtils.isEmpty(currIso) || TextUtils.isEmpty(defaultIso))) {
                return cdmaCheckAndProcessPlusCodeByNumberFormat(dialStr, getFormatTypeFromCountryCode(currIso), getFormatTypeFromCountryCode(defaultIso));
            }
        }
        return dialStr;
    }

    public static String cdmaCheckAndProcessPlusCodeForSms(String dialStr) {
        String result = preProcessPlusCodeForSms(dialStr);
        if (result != null && !result.equals(dialStr)) {
            return result;
        }
        if (!TextUtils.isEmpty(dialStr) && isReallyDialable(dialStr.charAt(0)) && isNonSeparator(dialStr)) {
            String defaultIso = TelephonyManager.getDefault().getSimCountryIso();
            if (!TextUtils.isEmpty(defaultIso)) {
                int format = getFormatTypeFromCountryCode(defaultIso);
                return cdmaCheckAndProcessPlusCodeByNumberFormat(dialStr, format, format);
            }
        }
        return dialStr;
    }

    public static String cdmaCheckAndProcessPlusCodeByNumberFormat(String dialStr, int currFormat, int defaultFormat) {
        String retStr = dialStr;
        boolean useNanp = currFormat == defaultFormat && currFormat == 1;
        if (dialStr != null && dialStr.lastIndexOf(PLUS_SIGN_STRING) != -1) {
            String tempDialStr = dialStr;
            retStr = null;
            do {
                String networkDialStr;
                if (useNanp) {
                    networkDialStr = extractNetworkPortion(tempDialStr);
                } else {
                    networkDialStr = extractNetworkPortionAlt(tempDialStr);
                }
                networkDialStr = processPlusCode(networkDialStr, useNanp);
                if (!TextUtils.isEmpty(networkDialStr)) {
                    if (retStr == null) {
                        retStr = networkDialStr;
                    } else {
                        retStr = retStr.concat(networkDialStr);
                    }
                    String postDialStr = extractPostDialPortion(tempDialStr);
                    if (!TextUtils.isEmpty(postDialStr)) {
                        int dialableIndex = findDialableIndexFromPostDialStr(postDialStr);
                        if (dialableIndex >= 1) {
                            retStr = appendPwCharBackToOrigDialStr(dialableIndex, retStr, postDialStr);
                            tempDialStr = postDialStr.substring(dialableIndex);
                        } else {
                            if (dialableIndex < 0) {
                                postDialStr = PhoneConstants.MVNO_TYPE_NONE;
                            }
                            Rlog.e("wrong postDialStr=", postDialStr);
                        }
                    }
                    if (TextUtils.isEmpty(postDialStr)) {
                        break;
                    }
                } else {
                    Rlog.e("checkAndProcessPlusCode: null newDialStr", networkDialStr);
                    return dialStr;
                }
            } while (!TextUtils.isEmpty(tempDialStr));
        }
        return retStr;
    }

    public static CharSequence createTtsSpannable(CharSequence phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        Spannable spannable = Factory.getInstance().newSpannable(phoneNumber);
        addTtsSpan(spannable, 0, spannable.length());
        return spannable;
    }

    public static void addTtsSpan(Spannable s, int start, int endExclusive) {
        s.setSpan(createTtsSpan(s.subSequence(start, endExclusive).toString()), start, endExclusive, 33);
    }

    @Deprecated
    public static CharSequence ttsSpanAsPhoneNumber(CharSequence phoneNumber) {
        return createTtsSpannable(phoneNumber);
    }

    @Deprecated
    public static void ttsSpanAsPhoneNumber(Spannable s, int start, int end) {
        addTtsSpan(s, start, end);
    }

    public static TtsSpan createTtsSpan(String phoneNumberString) {
        if (phoneNumberString == null) {
            return null;
        }
        PhoneNumber phoneNumber = null;
        try {
            phoneNumber = PhoneNumberUtil.getInstance().parse(phoneNumberString, null);
        } catch (NumberParseException e) {
        }
        TelephoneBuilder builder = new TelephoneBuilder();
        if (phoneNumber == null) {
            builder.setNumberParts(splitAtNonNumerics(phoneNumberString));
        } else {
            if (phoneNumber.hasCountryCode()) {
                builder.setCountryCode(Integer.toString(phoneNumber.getCountryCode()));
            }
            builder.setNumberParts(Long.toString(phoneNumber.getNationalNumber()));
        }
        return builder.build();
    }

    private static String splitAtNonNumerics(CharSequence number) {
        StringBuilder sb = new StringBuilder(number.length());
        for (int i = 0; i < number.length(); i++) {
            Object valueOf;
            if (isISODigit(number.charAt(i))) {
                valueOf = Character.valueOf(number.charAt(i));
            } else {
                valueOf = " ";
            }
            sb.append(valueOf);
        }
        return sb.toString().replaceAll(" +", " ").trim();
    }

    private static String getCurrentIdp(boolean useNanp) {
        if (useNanp) {
            return NANP_IDP_STRING;
        }
        return SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_IDP_STRING, PLUS_SIGN_STRING);
    }

    private static boolean isTwoToNine(char c) {
        if (c < '2' || c > '9') {
            return false;
        }
        return true;
    }

    private static int getFormatTypeFromCountryCode(String country) {
        for (String compareToIgnoreCase : NANP_COUNTRIES) {
            if (compareToIgnoreCase.compareToIgnoreCase(country) == 0) {
                return 1;
            }
        }
        if ("jp".compareToIgnoreCase(country) == 0) {
            return 2;
        }
        return 0;
    }

    public static boolean isNanp(String dialStr) {
        if (dialStr == null) {
            Rlog.e("isNanp: null dialStr passed in", dialStr);
            return false;
        } else if (dialStr.length() != 10 || !isTwoToNine(dialStr.charAt(0)) || !isTwoToNine(dialStr.charAt(3))) {
            return false;
        } else {
            for (int i = 1; i < 10; i++) {
                if (!isISODigit(dialStr.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean isOneNanp(String dialStr) {
        if (dialStr != null) {
            String newDialStr = dialStr.substring(1);
            if (dialStr.charAt(0) == '1' && isNanp(newDialStr)) {
                return true;
            }
            return false;
        }
        Rlog.e("isOneNanp: null dialStr passed in", dialStr);
        return false;
    }

    public static boolean isUriNumber(String number) {
        if (number != null) {
            return !number.contains("@") ? number.contains("%40") : true;
        } else {
            return false;
        }
    }

    public static String getUsernameFromUriNumber(String number) {
        int delimiterIndex = number.indexOf(64);
        if (delimiterIndex < 0) {
            delimiterIndex = number.indexOf("%40");
        }
        if (delimiterIndex < 0) {
            Rlog.w(LOG_TAG, "getUsernameFromUriNumber: no delimiter found in SIP addr '" + number + "'");
            delimiterIndex = number.length();
        }
        return number.substring(0, delimiterIndex);
    }

    public static Uri convertSipUriToTelUri(Uri source) {
        if (!PhoneAccount.SCHEME_SIP.equals(source.getScheme())) {
            return source;
        }
        String[] numberParts = source.getSchemeSpecificPart().split("[@;:]");
        if (numberParts.length == 0) {
            return source;
        }
        return Uri.fromParts(PhoneAccount.SCHEME_TEL, numberParts[0], null);
    }

    private static String processPlusCode(String networkDialStr, boolean useNanp) {
        String retStr = networkDialStr;
        if (networkDialStr == null || networkDialStr.charAt(0) != PLUS_SIGN_CHAR || networkDialStr.length() <= 1) {
            return retStr;
        }
        String newStr = networkDialStr.substring(1);
        if (useNanp && isOneNanp(newStr)) {
            return newStr;
        }
        return networkDialStr.replaceFirst("[+]", getCurrentIdp(useNanp));
    }

    private static int findDialableIndexFromPostDialStr(String postDialStr) {
        for (int index = 0; index < postDialStr.length(); index++) {
            if (isReallyDialable(postDialStr.charAt(index))) {
                return index;
            }
        }
        return -1;
    }

    private static String appendPwCharBackToOrigDialStr(int dialableIndex, String origStr, String dialStr) {
        if (dialableIndex == 1) {
            return dialStr.charAt(0);
        }
        return origStr.concat(dialStr.substring(0, dialableIndex));
    }

    private static boolean matchIntlPrefix(String a, int len) {
        boolean z = true;
        int state = 0;
        for (int i = 0; i < len; i++) {
            char c = a.charAt(i);
            switch (state) {
                case 0:
                    if (c != PLUS_SIGN_CHAR) {
                        if (c != '0') {
                            if (!isNonSeparator(c)) {
                                break;
                            }
                            return false;
                        }
                        state = 2;
                        break;
                    }
                    state = 1;
                    break;
                case 2:
                    if (c != '0') {
                        if (c != '1') {
                            if (!isNonSeparator(c)) {
                                break;
                            }
                            return false;
                        }
                        state = 4;
                        break;
                    }
                    state = 3;
                    break;
                case 4:
                    if (c != '1') {
                        if (!isNonSeparator(c)) {
                            break;
                        }
                        return false;
                    }
                    state = 5;
                    break;
                default:
                    if (!isNonSeparator(c)) {
                        break;
                    }
                    return false;
            }
        }
        if (!(state == 1 || state == 3 || state == 5)) {
            z = false;
        }
        return z;
    }

    private static boolean matchIntlPrefixAndCC(String a, int len) {
        boolean z = true;
        int state = 0;
        for (int i = 0; i < len; i++) {
            char c = a.charAt(i);
            switch (state) {
                case 0:
                    if (c != PLUS_SIGN_CHAR) {
                        if (c != '0') {
                            if (!isNonSeparator(c)) {
                                break;
                            }
                            return false;
                        }
                        state = 2;
                        break;
                    }
                    state = 1;
                    break;
                case 1:
                case 3:
                case 5:
                    if (!isISODigit(c)) {
                        if (!isNonSeparator(c)) {
                            break;
                        }
                        return false;
                    }
                    state = 6;
                    break;
                case 2:
                    if (c != '0') {
                        if (c != '1') {
                            if (!isNonSeparator(c)) {
                                break;
                            }
                            return false;
                        }
                        state = 4;
                        break;
                    }
                    state = 3;
                    break;
                case 4:
                    if (c != '1') {
                        if (!isNonSeparator(c)) {
                            break;
                        }
                        return false;
                    }
                    state = 5;
                    break;
                case 6:
                case 7:
                    if (!isISODigit(c)) {
                        if (!isNonSeparator(c)) {
                            break;
                        }
                        return false;
                    }
                    state++;
                    break;
                default:
                    if (!isNonSeparator(c)) {
                        break;
                    }
                    return false;
            }
        }
        if (!(state == 6 || state == 7 || state == 8)) {
            z = false;
        }
        return z;
    }

    private static boolean matchTrunkPrefix(String a, int len) {
        boolean found = false;
        for (int i = 0; i < len; i++) {
            char c = a.charAt(i);
            if (c == '0' && !found) {
                found = true;
            } else if (isNonSeparator(c)) {
                return false;
            }
        }
        return found;
    }

    private static boolean isCountryCallingCode(int countryCallingCodeCandidate) {
        if (countryCallingCodeCandidate <= 0 || countryCallingCodeCandidate >= CCC_LENGTH) {
            return false;
        }
        return COUNTRY_CALLING_CALL[countryCallingCodeCandidate];
    }

    private static int tryGetISODigit(char ch) {
        if ('0' > ch || ch > '9') {
            return -1;
        }
        return ch - 48;
    }

    private static CountryCallingCodeAndNewIndex tryGetCountryCallingCodeAndNewIndex(String str, boolean acceptThailandCase) {
        int state = 0;
        int ccc = 0;
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            switch (state) {
                case 0:
                    if (ch != PLUS_SIGN_CHAR) {
                        if (ch != '0') {
                            if (ch != '1') {
                                if (!isDialable(ch)) {
                                    break;
                                }
                                return null;
                            } else if (acceptThailandCase) {
                                state = 8;
                                break;
                            } else {
                                return null;
                            }
                        }
                        state = 2;
                        break;
                    }
                    state = 1;
                    break;
                case 1:
                case 3:
                case 5:
                case 6:
                case 7:
                    int ret = tryGetISODigit(ch);
                    if (ret <= 0) {
                        if (!isDialable(ch)) {
                            break;
                        }
                        return null;
                    }
                    ccc = (ccc * 10) + ret;
                    if (ccc < 100 && !isCountryCallingCode(ccc)) {
                        if (state != 1 && state != 3 && state != 5) {
                            state++;
                            break;
                        }
                        state = 6;
                        break;
                    }
                    return new CountryCallingCodeAndNewIndex(ccc, i + 1);
                    break;
                case 2:
                    if (ch != '0') {
                        if (ch != '1') {
                            if (!isDialable(ch)) {
                                break;
                            }
                            return null;
                        }
                        state = 4;
                        break;
                    }
                    state = 3;
                    break;
                case 4:
                    if (ch != '1') {
                        if (!isDialable(ch)) {
                            break;
                        }
                        return null;
                    }
                    state = 5;
                    break;
                case 8:
                    if (ch != '6') {
                        if (!isDialable(ch)) {
                            break;
                        }
                        return null;
                    }
                    state = 9;
                    break;
                case 9:
                    if (ch == '6') {
                        return new CountryCallingCodeAndNewIndex(66, i + 1);
                    }
                    return null;
                default:
                    return null;
            }
        }
        return null;
    }

    private static int tryGetTrunkPrefixOmittedIndex(String str, int currentIndex) {
        int length = str.length();
        for (int i = currentIndex; i < length; i++) {
            char ch = str.charAt(i);
            if (tryGetISODigit(ch) >= 0) {
                return i + 1;
            }
            if (isDialable(ch)) {
                return -1;
            }
        }
        return -1;
    }

    private static boolean checkPrefixIsIgnorable(String str, int forwardIndex, int backwardIndex) {
        boolean trunk_prefix_was_read = false;
        while (backwardIndex >= forwardIndex) {
            if (tryGetISODigit(str.charAt(backwardIndex)) >= 0) {
                if (trunk_prefix_was_read) {
                    return false;
                }
                trunk_prefix_was_read = true;
            } else if (isDialable(str.charAt(backwardIndex))) {
                return false;
            }
            backwardIndex--;
        }
        return true;
    }

    private static int getDefaultVoiceSubId() {
        return Integer.MAX_VALUE;
    }

    public static boolean isConvertToEmergencyNumberEnabled() {
        return CONVERT_TO_EMERGENCY_MAP != null && CONVERT_TO_EMERGENCY_MAP.length > 0;
    }

    public static String convertToEmergencyNumber(String number) {
        if (TextUtils.isEmpty(number)) {
            return number;
        }
        String normalizedNumber = normalizeNumber(number);
        if (isEmergencyNumber(normalizedNumber)) {
            return number;
        }
        for (String convertMap : CONVERT_TO_EMERGENCY_MAP) {
            String[] entry = null;
            String[] filterNumbers = null;
            Object convertedNumber = null;
            if (!TextUtils.isEmpty(convertMap)) {
                entry = convertMap.split(":");
            }
            if (entry != null && entry.length == 2) {
                convertedNumber = entry[1];
                if (!TextUtils.isEmpty(entry[0])) {
                    filterNumbers = entry[0].split(",");
                }
            }
            if (!(TextUtils.isEmpty(convertedNumber) || filterNumbers == null || filterNumbers.length == 0)) {
                for (String filterNumber : filterNumbers) {
                    if (!TextUtils.isEmpty(filterNumber) && filterNumber.equals(normalizedNumber)) {
                        return convertedNumber;
                    }
                }
                continue;
            }
        }
        return number;
    }

    private static void initialize() {
        boolean z;
        sIsCtaSupport = "1".equals(SystemProperties.get("ro.mtk_cta_support"));
        sIsCtaSet = "1".equals(SystemProperties.get("ro.mtk_cta_set"));
        sIsC2kSupport = "1".equals(SystemProperties.get("ro.boot.opt_c2k_support"));
        if (!"OP09".equals(SystemProperties.get("persist.operator.optr"))) {
            z = false;
        } else if ("SEGDEFAULT".equals(SystemProperties.get("persist.operator.seg"))) {
            z = true;
        } else {
            z = "SEGC".equals(SystemProperties.get("persist.operator.seg"));
        }
        sIsOP09Support = z;
        log("Init: sIsCtaSupport: " + sIsCtaSupport + ", sIsCtaSet: " + sIsCtaSet + ", sIsC2kSupport: " + sIsC2kSupport + ", sIsOP09Support: " + sIsOP09Support);
        sPlusCodeUtils = PlusCodeProcessor.getPlusCodeUtils();
        initEccSource();
    }

    private static void initEccSource() {
        sAllEccSource = new ArrayList();
        sNetworkEcc = new NetworkEccSource(1);
        sPropertyEcc = new PropertyEccSource(1);
        if (sIsC2kSupport) {
            sXmlEcc = new XmlEccSource(3);
            sSimEcc = new SimEccSource(3);
            sTestEcc = new TestEccSource(3);
        } else {
            sXmlEcc = new XmlEccSource(1);
            sSimEcc = new SimEccSource(1);
            sTestEcc = new TestEccSource(1);
        }
        sAllEccSource.add(sNetworkEcc);
        sAllEccSource.add(sSimEcc);
        sAllEccSource.add(sXmlEcc);
        sAllEccSource.add(sPropertyEcc);
        sAllEccSource.add(sTestEcc);
        if (sIsCtaSet) {
            sCtaEcc = new CtaEccSource(1);
            sAllEccSource.add(sCtaEcc);
        }
        if (sIsC2kSupport) {
            sOmhEcc = new OmhEccSource(2);
            sAllEccSource.add(sOmhEcc);
        }
    }

    public static String extractCLIRPortion(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        Matcher m = Pattern.compile("^([*][#]|[*]{1,2}|[#]{1,2})([0-9]{2,3})([*])([+]?[0-9]+)(.*)(#)$").matcher(phoneNumber);
        if (m.matches()) {
            return m.group(4);
        }
        if (phoneNumber.startsWith(CLIR_ON) || phoneNumber.startsWith(CLIR_OFF)) {
            dlog(phoneNumber + " Start with *31# or #31#, return " + phoneNumber.substring(4));
            return phoneNumber.substring(4);
        }
        if (phoneNumber.indexOf(PLUS_SIGN_STRING) != -1 && phoneNumber.indexOf(PLUS_SIGN_STRING) == phoneNumber.lastIndexOf(PLUS_SIGN_STRING)) {
            m = Pattern.compile("(^[#*])(.*)([#*])(.*)(#)$").matcher(phoneNumber);
            String strDialNumber;
            if (!m.matches()) {
                m = Pattern.compile("(^[#*])(.*)([#*])(.*)").matcher(phoneNumber);
                if (m.matches()) {
                    strDialNumber = m.group(4);
                    if (strDialNumber != null && strDialNumber.length() > 1 && strDialNumber.charAt(0) == PLUS_SIGN_CHAR) {
                        dlog(phoneNumber + " matcher pattern2, return " + strDialNumber);
                        return strDialNumber;
                    }
                }
            } else if (PhoneConstants.MVNO_TYPE_NONE.equals(m.group(2))) {
                dlog(phoneNumber + " matcher pattern1, return empty string.");
                return PhoneConstants.MVNO_TYPE_NONE;
            } else {
                strDialNumber = m.group(4);
                if (strDialNumber != null && strDialNumber.length() > 1 && strDialNumber.charAt(0) == PLUS_SIGN_CHAR) {
                    dlog(phoneNumber + " matcher pattern1, return " + strDialNumber);
                    return strDialNumber;
                }
            }
        }
        return phoneNumber;
    }

    public static String prependPlusToNumber(String number) {
        StringBuilder ret;
        String retString = number.toString();
        Matcher m = Pattern.compile("^([*][#]|[*]{1,2}|[#]{1,2})([0-9]{2,3})([*])([0-9]+)(.*)(#)$").matcher(retString);
        if (m.matches()) {
            ret = new StringBuilder();
            ret.append(m.group(1));
            ret.append(m.group(2));
            ret.append(m.group(3));
            ret.append(PLUS_SIGN_STRING);
            ret.append(m.group(4));
            ret.append(m.group(5));
            ret.append(m.group(6));
        } else {
            m = Pattern.compile("(^[#*])(.*)([#*])(.*)(#)$").matcher(retString);
            if (!m.matches()) {
                m = Pattern.compile("(^[#*])(.*)([#*])(.*)").matcher(retString);
                if (m.matches()) {
                    ret = new StringBuilder();
                    ret.append(m.group(1));
                    ret.append(m.group(2));
                    ret.append(m.group(3));
                    ret.append(PLUS_SIGN_STRING);
                    ret.append(m.group(4));
                } else {
                    ret = new StringBuilder();
                    ret.append(PLUS_SIGN_CHAR);
                    ret.append(retString);
                }
            } else if (PhoneConstants.MVNO_TYPE_NONE.equals(m.group(2))) {
                ret = new StringBuilder();
                ret.append(m.group(1));
                ret.append(m.group(3));
                ret.append(m.group(4));
                ret.append(m.group(5));
                ret.append(PLUS_SIGN_STRING);
            } else {
                ret = new StringBuilder();
                ret.append(m.group(1));
                ret.append(m.group(2));
                ret.append(m.group(3));
                ret.append(PLUS_SIGN_STRING);
                ret.append(m.group(4));
                ret.append(m.group(5));
            }
        }
        return ret.toString();
    }

    public static String getInternationalPrefix(String countryIso) {
        if (countryIso == null) {
            return PhoneConstants.MVNO_TYPE_NONE;
        }
        PhoneMetadata metadata = PhoneNumberUtil.getInstance().getMetadataForRegion(countryIso);
        if (metadata == null) {
            return null;
        }
        String prefix = metadata.getInternationalPrefix();
        if (countryIso.equalsIgnoreCase("tw")) {
            prefix = "0(?:0[25679] | 16 | 17 | 19)";
        }
        return prefix;
    }

    private static String preProcessPlusCode(String dialStr) {
        if (!TextUtils.isEmpty(dialStr) && isReallyDialable(dialStr.charAt(0)) && isNonSeparator(dialStr)) {
            String currIso = TelephonyManager.getDefault().getNetworkCountryIso();
            String defaultIso = TelephonyManager.getDefault().getSimCountryIso();
            boolean needToFormat = true;
            if (!(TextUtils.isEmpty(currIso) || TextUtils.isEmpty(defaultIso))) {
                int currFormat = getFormatTypeFromCountryCode(currIso);
                needToFormat = (currFormat == getFormatTypeFromCountryCode(defaultIso) && currFormat == 1) ? false : true;
            }
            if (needToFormat) {
                dlog("preProcessPlusCode, before format number:" + dialStr);
                String retStr = dialStr;
                if (dialStr != null && dialStr.lastIndexOf(PLUS_SIGN_STRING) != -1) {
                    String tempDialStr = dialStr;
                    retStr = null;
                    do {
                        String networkDialStr = extractNetworkPortionAlt(tempDialStr);
                        if (networkDialStr != null && networkDialStr.charAt(0) == PLUS_SIGN_CHAR && networkDialStr.length() > 1) {
                            if (sPlusCodeUtils.canFormatPlusToIddNdd()) {
                                networkDialStr = sPlusCodeUtils.replacePlusCodeWithIddNdd(networkDialStr);
                            } else {
                                dlog("preProcessPlusCode, can't format plus code.");
                                return dialStr;
                            }
                        }
                        dlog("preProcessPlusCode, networkDialStr:" + networkDialStr);
                        if (!TextUtils.isEmpty(networkDialStr)) {
                            if (retStr == null) {
                                retStr = networkDialStr;
                            } else {
                                retStr = retStr.concat(networkDialStr);
                            }
                            String postDialStr = extractPostDialPortion(tempDialStr);
                            if (!TextUtils.isEmpty(postDialStr)) {
                                int dialableIndex = findDialableIndexFromPostDialStr(postDialStr);
                                if (dialableIndex >= 1) {
                                    retStr = appendPwCharBackToOrigDialStr(dialableIndex, retStr, postDialStr);
                                    tempDialStr = postDialStr.substring(dialableIndex);
                                } else {
                                    if (dialableIndex < 0) {
                                        postDialStr = PhoneConstants.MVNO_TYPE_NONE;
                                    }
                                    Rlog.e(LOG_TAG, "preProcessPlusCode, wrong postDialStr:" + postDialStr);
                                }
                            }
                            dlog("preProcessPlusCode, postDialStr:" + postDialStr + ", tempDialStr:" + tempDialStr);
                            if (TextUtils.isEmpty(postDialStr)) {
                                break;
                            }
                        } else {
                            Rlog.e(LOG_TAG, "preProcessPlusCode, null newDialStr:" + networkDialStr);
                            return dialStr;
                        }
                    } while (!TextUtils.isEmpty(tempDialStr));
                }
                dialStr = retStr;
                dlog("preProcessPlusCode, after format number:" + dialStr);
            } else {
                dlog("preProcessPlusCode, no need format, currIso:" + currIso + ", defaultIso:" + defaultIso);
            }
        }
        return dialStr;
    }

    private static String preProcessPlusCodeForSms(String dialStr) {
        dlog("preProcessPlusCodeForSms ENTER.");
        if (TextUtils.isEmpty(dialStr) || !dialStr.startsWith(PLUS_SIGN_STRING) || !isReallyDialable(dialStr.charAt(0)) || !isNonSeparator(dialStr) || getFormatTypeFromCountryCode(TelephonyManager.getDefault().getSimCountryIso()) == 1 || !sPlusCodeUtils.canFormatPlusCodeForSms()) {
            return dialStr;
        }
        String retAddr = sPlusCodeUtils.replacePlusCodeForSms(dialStr);
        if (TextUtils.isEmpty(retAddr)) {
            dlog("preProcessPlusCodeForSms, can't handle the plus code by PlusCodeUtils");
            return dialStr;
        }
        dlog("preProcessPlusCodeForSms, new dialStr = " + retAddr);
        return retAddr;
    }

    public static boolean isEmergencyNumberExt(int subId, String number, String defaultCountryIso, boolean useExactMatch) {
        if (number == null || isUriNumber(number)) {
            return false;
        }
        boolean ret;
        number = extractNetworkPortionAlt(number);
        dlog("[isEmergencyNumberExt] number: " + number + ", subId: " + subId + ", iso: " + defaultCountryIso + ", useExactMatch: " + useExactMatch);
        if (subId == Integer.MAX_VALUE || subId == -1) {
            int queryPhoneType = getQueryPhoneType(subId);
            if ((queryPhoneType & 1) != 0 && isEmergencyNumberExt(number, 1, subId)) {
                return true;
            }
            if ((queryPhoneType & 2) != 0 && isEmergencyNumberExt(number, 2, subId)) {
                return true;
            }
            if (SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("CN") && isOppoEmergencyNumber(number)) {
                dlog("[isEmergencyNumberExt] match OPPO default ECC in invalid subId case");
                return true;
            }
        }
        if (TelephonyManager.getDefault().getCurrentPhoneType(subId) == 2) {
            ret = isEmergencyNumberExt(number, 2, subId);
        } else {
            ret = isEmergencyNumberExt(number, 1, subId);
        }
        if (ret) {
            return true;
        }
        if (defaultCountryIso == null || isMmiCode(number)) {
            dlog("[isEmergencyNumber] no match ");
            return false;
        }
        ShortNumberUtil util = new ShortNumberUtil();
        if (useExactMatch) {
            ret = util.isEmergencyNumber(number, defaultCountryIso);
        } else {
            ret = util.connectsToEmergencyNumber(number, defaultCountryIso);
        }
        dlog("[isEmergencyNumberExt] AOSP check return: " + ret + ", iso: " + defaultCountryIso + ", useExactMatch: " + useExactMatch);
        return ret;
    }

    public static boolean isEmergencyNumberExt(String number, int phoneType) {
        dlog("[isEmergencyNumberExt], number:" + number + ", phoneType:" + phoneType);
        return isEmergencyNumberExt(number, phoneType, Integer.MAX_VALUE);
    }

    public static boolean isEmergencyNumberExt(String number, int phoneType, int subId) {
        vlog("[isEmergencyNumberExt], number:" + number + ", phoneType:" + phoneType);
        for (EccSource es : sAllEccSource) {
            if (es.isEmergencyNumber(number, subId, phoneType)) {
                return true;
            }
        }
        String region = SystemProperties.get("persist.sys.oppo.region", "CN");
        if (SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US") && isOppoEmergencyNumber(number) && !"AU".equals(region)) {
            return true;
        }
        dlog("[isEmergencyNumberExt] no match for phoneType: " + phoneType);
        return false;
    }

    public static boolean isSpecialEmergencyNumber(String dialString) {
        return isSpecialEmergencyNumber(Integer.MAX_VALUE, dialString);
    }

    public static boolean isSpecialEmergencyNumber(int subId, String dialString) {
        String version = SystemProperties.get("ro.oppo.version", "CN");
        if (version.equalsIgnoreCase("CN")) {
            if (sNetworkEcc.isEmergencyNumber(dialString, subId, 1) || sSimEcc.isEmergencyNumber(dialString, subId, 1)) {
                return false;
            }
            for (EccSource es : sAllEccSource) {
                if (es.isSpecialEmergencyNumber(subId, dialString)) {
                    return true;
                }
            }
        } else if (version.equalsIgnoreCase("US")) {
            for (EccSource es2 : sAllEccSource) {
                if (es2.isSpecialEmergencyNumber(subId, dialString)) {
                    return true;
                }
            }
            String[] emergencyNumList = new String[4];
            emergencyNumList[0] = "112";
            emergencyNumList[1] = "911";
            emergencyNumList[2] = "999";
            emergencyNumList[3] = "08";
            for (String equals : emergencyNumList) {
                if (equals.equals(dialString)) {
                    Rlog.d(LOG_TAG, "[isSpecialEmergencyNumber] match exp customized ecc list,these numbers always dial emergency call to modem ATDE ");
                    return false;
                }
            }
            return true;
        }
        log("[isSpecialEmergencyNumber] not special ecc");
        return false;
    }

    public static ArrayList<EccEntry> getEccList() {
        ArrayList<EccEntry> resList = new ArrayList();
        sXmlEcc.addToEccList(resList);
        sPropertyEcc.addToEccList(resList);
        if (sIsCtaSet) {
            sCtaEcc.addToEccList(resList);
        }
        dlog("[getEccList] ECC list: " + resList);
        return resList;
    }

    public static void setSpecificEccCategory(int eccCat) {
        log("[setSpecificEccCategory] set ECC category: " + eccCat);
        sSpecificEccCat = eccCat;
    }

    public static int getServiceCategoryFromEcc(String number) {
        return getServiceCategoryFromEccBySubId(number, Integer.MAX_VALUE);
    }

    public static int getServiceCategoryFromEccBySubId(String number, int subId) {
        if (sSpecificEccCat >= 0) {
            log("[getServiceCategoryFromEccBySubId] specific ECC category: " + sSpecificEccCat);
            int eccCat = sSpecificEccCat;
            sSpecificEccCat = -1;
            return eccCat;
        }
        for (EccSource es : sAllEccSource) {
            int category = es.getServiceCategory(number, subId);
            if (category >= 0) {
                return category;
            }
        }
        log("[getServiceCategoryFromEccBySubId] no matched, ECC: " + number + ", subId: " + subId);
        return 0;
    }

    private static int getQueryPhoneType(int subId) {
        int i;
        int simNum = TelephonyManager.getDefault().getPhoneCount();
        boolean needQueryGsm = false;
        boolean needQueryCdma = false;
        if (sIsC2kSupport) {
            for (i = 0; i < simNum; i++) {
                int[] allSubId = SubscriptionManager.getSubId(i);
                if (allSubId == null) {
                    dlog("[getQueryPhoneType] allSubId is null");
                } else {
                    vlog("[getQueryPhoneType] allSubId:" + allSubId[0]);
                    int phoneType = TelephonyManager.getDefault().getCurrentPhoneType(allSubId[0]);
                    if (phoneType == 1) {
                        needQueryGsm = true;
                    } else if (phoneType == 2) {
                        needQueryCdma = true;
                    }
                }
            }
            if (!(needQueryGsm || isSimInsert())) {
                needQueryGsm = true;
            }
        } else {
            needQueryGsm = true;
        }
        if (sIsC2kSupport && simNum > 1 && !needQueryCdma) {
            boolean isRoaming = false;
            ITelephonyEx telEx = Stub.asInterface(ServiceManager.getService("phoneEx"));
            if (telEx != null) {
                int[] iccTypes = new int[simNum];
                for (i = 0; i < simNum; i++) {
                    try {
                        iccTypes[i] = telEx.getIccAppFamily(i);
                    } catch (RemoteException ex) {
                        log("getIccAppFamily, RemoteException:" + ex);
                    } catch (NullPointerException ex2) {
                        log("getIccAppFamily, NullPointerException:" + ex2);
                    }
                }
                i = 0;
                while (i < simNum) {
                    if (iccTypes[i] >= 2 || isCt3gDualModeCard(i)) {
                        log("[getQueryPhoneType] Slot" + i + " is roaming");
                        isRoaming = true;
                        break;
                    }
                    i++;
                }
                if (!isRoaming) {
                    for (i = 0; i < simNum; i++) {
                        if (iccTypes[i] == 0) {
                            vlog("[getQueryPhoneType] Slot" + i + " no card");
                            needQueryCdma = true;
                            break;
                        }
                    }
                }
            } else {
                log("[getQueryPhoneType] fail to get ITelephonyEx service");
            }
        }
        int phoneTypeRet = 0;
        if (needQueryGsm) {
            phoneTypeRet = 1;
        }
        if (needQueryCdma) {
            phoneTypeRet |= 2;
        }
        vlog("[getQueryPhoneType] needQueryGsm:" + needQueryGsm + ", needQueryCdma:" + needQueryCdma + ", ret: " + phoneTypeRet);
        return phoneTypeRet;
    }

    private static boolean isCt3gDualModeCard(int slotId) {
        String[] ct3gProp = new String[4];
        ct3gProp[0] = "gsm.ril.ct3g";
        ct3gProp[1] = "gsm.ril.ct3g.2";
        ct3gProp[2] = "gsm.ril.ct3g.3";
        ct3gProp[3] = "gsm.ril.ct3g.4";
        if (slotId < 0 || slotId >= ct3gProp.length) {
            return false;
        }
        return "1".equals(SystemProperties.get(ct3gProp[slotId]));
    }

    private static boolean isMmiCode(String number) {
        if (Pattern.compile("^[*][0-9]+[#]$").matcher(number).matches()) {
            return true;
        }
        return false;
    }

    private static boolean isSimInsert() {
        int simNum = TelephonyManager.getDefault().getPhoneCount();
        for (int i = 0; i < simNum; i++) {
            if (TelephonyManager.getDefault().hasIccCard(i)) {
                return true;
            }
        }
        return false;
    }

    private static void dlog(String msg) {
        if (VDBG) {
            log(msg);
        }
    }

    private static void vlog(String msg) {
    }

    private static boolean isOppoEmergencyNumber(String number) {
        String[] emergencyNumList = new String[10];
        emergencyNumList[0] = "112";
        emergencyNumList[1] = "911";
        emergencyNumList[2] = "000";
        emergencyNumList[3] = "08";
        emergencyNumList[4] = "110";
        emergencyNumList[5] = "118";
        emergencyNumList[6] = "119";
        emergencyNumList[7] = "999";
        emergencyNumList[8] = "122";
        emergencyNumList[9] = "120";
        if (SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US")) {
            String region = SystemProperties.get("persist.sys.oppo.region", "CN");
            if (region.equalsIgnoreCase("TH")) {
                String[] emergencyNumListTH = new String[15];
                emergencyNumListTH[0] = "112";
                emergencyNumListTH[1] = "911";
                emergencyNumListTH[2] = "000";
                emergencyNumListTH[3] = "08";
                emergencyNumListTH[4] = "110";
                emergencyNumListTH[5] = "118";
                emergencyNumListTH[6] = "119";
                emergencyNumListTH[7] = "999";
                emergencyNumListTH[8] = "122";
                emergencyNumListTH[9] = "120";
                emergencyNumListTH[10] = "191";
                emergencyNumListTH[11] = "1195";
                emergencyNumListTH[12] = "1199";
                emergencyNumListTH[13] = "199";
                emergencyNumListTH[14] = "1669";
                emergencyNumList = emergencyNumListTH;
            } else if (region.equalsIgnoreCase("ID")) {
                String[] emergencyNumListID = new String[15];
                emergencyNumListID[0] = "112";
                emergencyNumListID[1] = "911";
                emergencyNumListID[2] = "000";
                emergencyNumListID[3] = "08";
                emergencyNumListID[4] = "110";
                emergencyNumListID[5] = "118";
                emergencyNumListID[6] = "119";
                emergencyNumListID[7] = "999";
                emergencyNumListID[8] = "122";
                emergencyNumListID[9] = "120";
                emergencyNumListID[10] = "113";
                emergencyNumListID[11] = "1131";
                emergencyNumListID[12] = "115";
                emergencyNumListID[13] = "129";
                emergencyNumListID[14] = "123";
                emergencyNumList = emergencyNumListID;
            } else if (region.equalsIgnoreCase("VN")) {
                String[] emergencyNumListVN = new String[13];
                emergencyNumListVN[0] = "112";
                emergencyNumListVN[1] = "911";
                emergencyNumListVN[2] = "000";
                emergencyNumListVN[3] = "08";
                emergencyNumListVN[4] = "110";
                emergencyNumListVN[5] = "118";
                emergencyNumListVN[6] = "119";
                emergencyNumListVN[7] = "999";
                emergencyNumListVN[8] = "122";
                emergencyNumListVN[9] = "120";
                emergencyNumListVN[10] = "113";
                emergencyNumListVN[11] = "114";
                emergencyNumListVN[12] = "115";
                emergencyNumList = emergencyNumListVN;
            } else if (region.equalsIgnoreCase("RU")) {
                String[] emergencyNumListRU = new String[2];
                emergencyNumListRU[0] = "112";
                emergencyNumListRU[1] = "911";
                emergencyNumList = emergencyNumListRU;
            } else if (region.equalsIgnoreCase("MX")) {
                String[] emergencyNumListMX = new String[11];
                emergencyNumListMX[0] = "112";
                emergencyNumListMX[1] = "911";
                emergencyNumListMX[2] = "000";
                emergencyNumListMX[3] = "08";
                emergencyNumListMX[4] = "118";
                emergencyNumListMX[5] = "119";
                emergencyNumListMX[6] = "999";
                emergencyNumListMX[7] = "060";
                emergencyNumListMX[8] = "065";
                emergencyNumListMX[9] = "066";
                emergencyNumListMX[10] = "068";
                emergencyNumList = emergencyNumListMX;
            } else if (region.equalsIgnoreCase("IN")) {
                String[] emergencyNumListIN = new String[6];
                emergencyNumListIN[0] = "112";
                emergencyNumListIN[1] = "911";
                emergencyNumListIN[2] = "100";
                emergencyNumListIN[3] = "101";
                emergencyNumListIN[4] = "102";
                emergencyNumListIN[5] = "108";
                emergencyNumList = emergencyNumListIN;
            } else if (region.equalsIgnoreCase("MY")) {
                String[] emergencyNumListMY = new String[8];
                emergencyNumListMY[0] = "112";
                emergencyNumListMY[1] = "911";
                emergencyNumListMY[2] = "08";
                emergencyNumListMY[3] = "118";
                emergencyNumListMY[4] = "119";
                emergencyNumListMY[5] = "999";
                emergencyNumListMY[6] = "994";
                emergencyNumListMY[7] = "991";
                emergencyNumList = emergencyNumListMY;
            } else if (region.equalsIgnoreCase("SG")) {
                String[] emergencyNumListSG = new String[8];
                emergencyNumListSG[0] = "112";
                emergencyNumListSG[1] = "911";
                emergencyNumListSG[2] = "000";
                emergencyNumListSG[3] = "08";
                emergencyNumListSG[4] = "999";
                emergencyNumListSG[5] = "118";
                emergencyNumListSG[6] = "119";
                emergencyNumListSG[7] = "1777";
                emergencyNumList = emergencyNumListSG;
            } else if (region.equalsIgnoreCase("TW")) {
                String[] emergencyNumListTW = new String[9];
                emergencyNumListTW[0] = "112";
                emergencyNumListTW[1] = "911";
                emergencyNumListTW[2] = "000";
                emergencyNumListTW[3] = "08";
                emergencyNumListTW[4] = "110";
                emergencyNumListTW[5] = "118";
                emergencyNumListTW[6] = "119";
                emergencyNumListTW[7] = "113";
                emergencyNumListTW[8] = "165";
                emergencyNumList = emergencyNumListTW;
            } else if (region.equalsIgnoreCase("DK")) {
                String[] emergencyNumListDK = new String[8];
                emergencyNumListDK[0] = "112";
                emergencyNumListDK[1] = "911";
                emergencyNumListDK[2] = "000";
                emergencyNumListDK[3] = "08";
                emergencyNumListDK[4] = "999";
                emergencyNumListDK[5] = "114";
                emergencyNumListDK[6] = "118";
                emergencyNumListDK[7] = "119";
                emergencyNumList = emergencyNumListDK;
            } else if (region.equalsIgnoreCase("PH")) {
                String[] emergencyNumListPH = new String[6];
                emergencyNumListPH[0] = "112";
                emergencyNumListPH[1] = "911";
                emergencyNumListPH[2] = "000";
                emergencyNumListPH[3] = "08";
                emergencyNumListPH[4] = "999";
                emergencyNumListPH[5] = "117";
                emergencyNumList = emergencyNumListPH;
            } else if (region.equalsIgnoreCase("MM")) {
                String[] emergencyNumListMM = new String[8];
                emergencyNumListMM[0] = "112";
                emergencyNumListMM[1] = "911";
                emergencyNumListMM[2] = "000";
                emergencyNumListMM[3] = "08";
                emergencyNumListMM[4] = "999";
                emergencyNumListMM[5] = "199";
                emergencyNumListMM[6] = "191";
                emergencyNumListMM[7] = "192";
                emergencyNumList = emergencyNumListMM;
            } else if (region.equalsIgnoreCase("PK")) {
                String[] emergencyNumlistPK = new String[8];
                emergencyNumlistPK[0] = "112";
                emergencyNumlistPK[1] = "911";
                emergencyNumlistPK[2] = "000";
                emergencyNumlistPK[3] = "08";
                emergencyNumlistPK[4] = "15";
                emergencyNumlistPK[5] = "16";
                emergencyNumlistPK[6] = "115";
                emergencyNumlistPK[7] = "1122";
                emergencyNumList = emergencyNumlistPK;
            } else if (region.equalsIgnoreCase("NG")) {
                String[] emergencyNumListNG = new String[5];
                emergencyNumListNG[0] = "112";
                emergencyNumListNG[1] = "911";
                emergencyNumListNG[2] = "000";
                emergencyNumListNG[3] = "08";
                emergencyNumListNG[4] = "119";
                emergencyNumList = emergencyNumListNG;
            } else if (region.equalsIgnoreCase("DE")) {
                String[] emergencyNumListDE = new String[5];
                emergencyNumListDE[0] = "112";
                emergencyNumListDE[1] = "911";
                emergencyNumListDE[2] = "000";
                emergencyNumListDE[3] = "08";
                emergencyNumListDE[4] = "110";
                emergencyNumList = emergencyNumListDE;
            } else if (region.equalsIgnoreCase("NL")) {
                String[] emergencyNumListNL = new String[2];
                emergencyNumListNL[0] = "112";
                emergencyNumListNL[1] = "911";
                emergencyNumList = emergencyNumListNL;
            } else if (region.equalsIgnoreCase("SE")) {
                String[] emergencyNumListSE = new String[4];
                emergencyNumListSE[0] = "112";
                emergencyNumListSE[1] = "911";
                emergencyNumListSE[2] = "000";
                emergencyNumListSE[3] = "08";
                emergencyNumList = emergencyNumListSE;
            } else if (region.equalsIgnoreCase("TR")) {
                String[] emergencyNumListTR = new String[6];
                emergencyNumListTR[0] = "112";
                emergencyNumListTR[1] = "911";
                emergencyNumListTR[2] = "000";
                emergencyNumListTR[3] = "08";
                emergencyNumListTR[4] = "110";
                emergencyNumListTR[5] = "155";
                emergencyNumList = emergencyNumListTR;
            } else if (region.equalsIgnoreCase("IR")) {
                String[] emergencyNumListIR = new String[7];
                emergencyNumListIR[0] = "112";
                emergencyNumListIR[1] = "911";
                emergencyNumListIR[2] = "000";
                emergencyNumListIR[3] = "08";
                emergencyNumListIR[4] = "115";
                emergencyNumListIR[5] = "125";
                emergencyNumListIR[6] = "110";
                emergencyNumList = emergencyNumListIR;
            } else if (region.equalsIgnoreCase("AE")) {
                String[] emergencyNumListAE = new String[11];
                emergencyNumListAE[0] = "112";
                emergencyNumListAE[1] = "911";
                emergencyNumListAE[2] = "000";
                emergencyNumListAE[3] = "08";
                emergencyNumListAE[4] = "998";
                emergencyNumListAE[5] = "999";
                emergencyNumListAE[6] = "997";
                emergencyNumListAE[7] = "996";
                emergencyNumListAE[8] = "993";
                emergencyNumListAE[9] = "991";
                emergencyNumListAE[10] = "992";
                emergencyNumList = emergencyNumListAE;
            } else if (region.equalsIgnoreCase("EG")) {
                String[] emergencyNumListEG = new String[4];
                emergencyNumListEG[0] = "112";
                emergencyNumListEG[1] = "911";
                emergencyNumListEG[2] = "000";
                emergencyNumListEG[3] = "08";
                emergencyNumList = emergencyNumListEG;
            } else if (region.equalsIgnoreCase("AU")) {
                String[] emergencyNumListAU = new String[8];
                emergencyNumListAU[0] = "112";
                emergencyNumListAU[1] = "911";
                emergencyNumListAU[2] = "000";
                emergencyNumListAU[3] = "08";
                emergencyNumListAU[4] = "110";
                emergencyNumListAU[5] = "999";
                emergencyNumListAU[6] = "118";
                emergencyNumListAU[7] = "119";
                emergencyNumList = emergencyNumListAU;
            } else if (region.equalsIgnoreCase("DZ")) {
                String[] emergencyNumListDZ = new String[8];
                emergencyNumListDZ[0] = "112";
                emergencyNumListDZ[1] = "911";
                emergencyNumListDZ[2] = "000";
                emergencyNumListDZ[3] = "08";
                emergencyNumListDZ[4] = "14";
                emergencyNumListDZ[5] = "17";
                emergencyNumListDZ[6] = "1055";
                emergencyNumListDZ[7] = "1548";
                emergencyNumList = emergencyNumListDZ;
            } else if (region.equalsIgnoreCase("ZA")) {
                String[] emergencyNumListZA = new String[10];
                emergencyNumListZA[0] = "112";
                emergencyNumListZA[1] = "911";
                emergencyNumListZA[2] = "000";
                emergencyNumListZA[3] = "08";
                emergencyNumListZA[4] = "10111";
                emergencyNumListZA[5] = "10177";
                emergencyNumListZA[6] = "1022";
                emergencyNumListZA[7] = "1011";
                emergencyNumListZA[8] = "0831999";
                emergencyNumListZA[9] = "0800111990";
                emergencyNumList = emergencyNumListZA;
            } else if (region.equalsIgnoreCase("MA")) {
                String[] emergencyNumListMA = new String[8];
                emergencyNumListMA[0] = "112";
                emergencyNumListMA[1] = "911";
                emergencyNumListMA[2] = "000";
                emergencyNumListMA[3] = "08";
                emergencyNumListMA[4] = "15";
                emergencyNumListMA[5] = "16";
                emergencyNumListMA[6] = "19";
                emergencyNumListMA[7] = "177";
                emergencyNumList = emergencyNumListMA;
            } else if (region.equalsIgnoreCase("AG")) {
                String[] emergencyNumListAG = new String[23];
                emergencyNumListAG[0] = "112";
                emergencyNumListAG[1] = "911";
                emergencyNumListAG[2] = "000";
                emergencyNumListAG[3] = "08";
                emergencyNumListAG[4] = "100";
                emergencyNumListAG[5] = "102";
                emergencyNumListAG[6] = "103";
                emergencyNumListAG[7] = "105";
                emergencyNumListAG[8] = "106";
                emergencyNumListAG[9] = "107";
                emergencyNumListAG[10] = "108";
                emergencyNumListAG[11] = "110";
                emergencyNumListAG[12] = "113";
                emergencyNumListAG[13] = "114";
                emergencyNumListAG[14] = "115";
                emergencyNumListAG[15] = "125";
                emergencyNumListAG[16] = "132";
                emergencyNumListAG[17] = "133";
                emergencyNumListAG[18] = "134";
                emergencyNumListAG[19] = "135";
                emergencyNumListAG[20] = "137";
                emergencyNumListAG[21] = "139";
                emergencyNumListAG[22] = "31416";
                emergencyNumList = emergencyNumListAG;
            } else if (region.equalsIgnoreCase("CO")) {
                String[] emergencyNumListCO = new String[5];
                emergencyNumListCO[0] = "112";
                emergencyNumListCO[1] = "911";
                emergencyNumListCO[2] = "000";
                emergencyNumListCO[3] = "08";
                emergencyNumListCO[4] = "123";
                emergencyNumList = emergencyNumListCO;
            } else if (region.equalsIgnoreCase("BH")) {
                String[] emergencyNumListBH = new String[9];
                emergencyNumListBH[0] = "112";
                emergencyNumListBH[1] = "911";
                emergencyNumListBH[2] = "199";
                emergencyNumListBH[3] = "997";
                emergencyNumListBH[4] = "998";
                emergencyNumListBH[5] = "999";
                emergencyNumListBH[6] = "990";
                emergencyNumListBH[7] = "992";
                emergencyNumListBH[8] = "994";
                emergencyNumList = emergencyNumListBH;
            } else if (region.equalsIgnoreCase("OM")) {
                String[] emergencyNumListOM = new String[5];
                emergencyNumListOM[0] = "112";
                emergencyNumListOM[1] = "911";
                emergencyNumListOM[2] = "153";
                emergencyNumListOM[3] = "154";
                emergencyNumListOM[4] = "9999";
                emergencyNumList = emergencyNumListOM;
            } else if (region.equalsIgnoreCase("QA")) {
                String[] emergencyNumListQA = new String[7];
                emergencyNumListQA[0] = "112";
                emergencyNumListQA[1] = "911";
                emergencyNumListQA[2] = "999";
                emergencyNumListQA[3] = "991";
                emergencyNumListQA[4] = "992";
                emergencyNumListQA[5] = "996";
                emergencyNumListQA[6] = "998";
                emergencyNumList = emergencyNumListQA;
            } else if (region.equalsIgnoreCase("KW")) {
                String[] emergencyNumListKW = new String[3];
                emergencyNumListKW[0] = "112";
                emergencyNumListKW[1] = "911";
                emergencyNumListKW[2] = "777";
                emergencyNumList = emergencyNumListKW;
            } else if (region.equalsIgnoreCase("KH")) {
                String[] emergencyNumListKH = new String[7];
                emergencyNumListKH[0] = "112";
                emergencyNumListKH[1] = "911";
                emergencyNumListKH[2] = "117";
                emergencyNumListKH[3] = "118";
                emergencyNumListKH[4] = "119";
                emergencyNumListKH[5] = "023723871";
                emergencyNumListKH[6] = "023724046";
                emergencyNumList = emergencyNumListKH;
            } else if (region.equalsIgnoreCase("LA")) {
                String[] emergencyNumListLA = new String[3];
                emergencyNumListLA[0] = "112";
                emergencyNumListLA[1] = "911";
                emergencyNumListLA[2] = "1191";
                emergencyNumList = emergencyNumListLA;
            } else if (region.equalsIgnoreCase("KE")) {
                String[] emergencyNumListKE = new String[3];
                emergencyNumListKE[0] = "112";
                emergencyNumListKE[1] = "911";
                emergencyNumListKE[2] = "999";
                emergencyNumList = emergencyNumListKE;
            } else if (region.equalsIgnoreCase("LK")) {
                String[] emergencyNumListLK = new String[5];
                emergencyNumListLK[0] = "112";
                emergencyNumListLK[1] = "911";
                emergencyNumListLK[2] = "110";
                emergencyNumListLK[3] = "118";
                emergencyNumListLK[4] = "119";
                emergencyNumList = emergencyNumListLK;
            } else if (region.equalsIgnoreCase("KZ")) {
                String[] emergencyNumListKZ = new String[6];
                emergencyNumListKZ[0] = "112";
                emergencyNumListKZ[1] = "911";
                emergencyNumListKZ[2] = "101";
                emergencyNumListKZ[3] = "102";
                emergencyNumListKZ[4] = "103";
                emergencyNumListKZ[5] = "104";
                emergencyNumList = emergencyNumListKZ;
            } else if (region.equalsIgnoreCase("NP")) {
                String[] emergencyNumListNP = new String[4];
                emergencyNumListNP[0] = "197";
                emergencyNumListNP[1] = "100";
                emergencyNumListNP[2] = "101";
                emergencyNumListNP[3] = "102";
                emergencyNumList = emergencyNumListNP;
            } else if (region.equalsIgnoreCase("NZ")) {
                String[] emergencyNumListNZ = new String[6];
                emergencyNumListNZ[0] = "112";
                emergencyNumListNZ[1] = "911";
                emergencyNumListNZ[2] = "111";
                emergencyNumListNZ[3] = "000";
                emergencyNumListNZ[4] = "119";
                emergencyNumListNZ[5] = "999";
                emergencyNumList = emergencyNumListNZ;
            } else if (region.equalsIgnoreCase("SA")) {
                String[] emergencyNumListSA = new String[9];
                emergencyNumListSA[0] = "112";
                emergencyNumListSA[1] = "911";
                emergencyNumListSA[2] = "993";
                emergencyNumListSA[3] = "994";
                emergencyNumListSA[4] = "996";
                emergencyNumListSA[5] = "966";
                emergencyNumListSA[6] = "999";
                emergencyNumListSA[7] = "998";
                emergencyNumListSA[8] = "997";
                emergencyNumList = emergencyNumListSA;
            } else if (region.equalsIgnoreCase("PL")) {
                String[] emergencyNumListPL = new String[5];
                emergencyNumListPL[0] = "112";
                emergencyNumListPL[1] = "999";
                emergencyNumListPL[2] = "998";
                emergencyNumListPL[3] = "997";
                emergencyNumListPL[4] = "911";
                emergencyNumList = emergencyNumListPL;
            } else if (region.equalsIgnoreCase("FR")) {
                String[] emergencyNumListFR = new String[9];
                emergencyNumListFR[0] = "112";
                emergencyNumListFR[1] = "15";
                emergencyNumListFR[2] = "17";
                emergencyNumListFR[3] = "18";
                emergencyNumListFR[4] = "115";
                emergencyNumListFR[5] = "119";
                emergencyNumListFR[6] = "116000";
                emergencyNumListFR[7] = "114";
                emergencyNumListFR[8] = "911";
                emergencyNumList = emergencyNumListFR;
            } else if (region.equalsIgnoreCase("IT")) {
                String[] emergencyNumListIT = new String[5];
                emergencyNumListIT[0] = "112";
                emergencyNumListIT[1] = "113";
                emergencyNumListIT[2] = "115";
                emergencyNumListIT[3] = "118";
                emergencyNumListIT[4] = "911";
                emergencyNumList = emergencyNumListIT;
            } else if (region.equalsIgnoreCase("ES")) {
                String[] emergencyNumListES = new String[6];
                emergencyNumListES[0] = "112";
                emergencyNumListES[1] = "091";
                emergencyNumListES[2] = "061";
                emergencyNumListES[3] = "080";
                emergencyNumListES[4] = "092";
                emergencyNumListES[5] = "911";
                emergencyNumList = emergencyNumListES;
            }
        }
        int eccLength = emergencyNumList.length;
        for (int i = 0; i < eccLength; i++) {
            String numberPlus = emergencyNumList[i] + PLUS_SIGN_STRING;
            if (emergencyNumList[i].equals(number) || numberPlus.equals(number)) {
                Rlog.d(LOG_TAG, "[isEmergencyNumber] isOppoEmergencyNumberreturn true");
                return true;
            }
        }
        return false;
    }

    public static boolean oppoCompare(String a, String b) {
        return oppoCompare(a, b, false);
    }

    public static boolean oppoCompare(String a, String b, boolean useStrictComparation) {
        return useStrictComparation ? compareStrictly(a, b) : oppoCompareLoosely(a, b);
    }

    public static boolean oppoCompareLoosely(String a, String b) {
        int numNonDialableCharsInA = 0;
        int numNonDialableCharsInB = 0;
        if (a == null || b == null) {
            return a == b;
        } else if (a.length() == 0 || b.length() == 0) {
            return false;
        } else {
            int minMatchLen;
            int ia = indexOfLastNetworkChar(a);
            int ib = indexOfLastNetworkChar(b);
            int matched = 0;
            while (ia >= 0 && ib >= 0) {
                boolean skipCmp = false;
                char ca = a.charAt(ia);
                if (!isDialable(ca)) {
                    ia--;
                    skipCmp = true;
                    numNonDialableCharsInA++;
                }
                char cb = b.charAt(ib);
                if (!isDialable(cb)) {
                    ib--;
                    skipCmp = true;
                    numNonDialableCharsInB++;
                }
                if (!skipCmp) {
                    if (cb != ca && ca != 'N' && cb != 'N') {
                        break;
                    }
                    ia--;
                    ib--;
                    matched++;
                }
            }
            if (sIsCtaSupport || sIsOP09Support) {
                minMatchLen = 11;
            } else if (SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("CN")) {
                minMatchLen = 11;
            } else {
                minMatchLen = 7;
            }
            vlog("[compareLoosely] a: " + a + ", b: " + b + ", minMatchLen:" + minMatchLen);
            if (matched < minMatchLen) {
                int effectiveALen = a.length() - numNonDialableCharsInA;
                if (effectiveALen == b.length() - numNonDialableCharsInB && effectiveALen == matched) {
                    return true;
                }
                vlog("[compareLoosely] return: false");
                return false;
            } else if (matched >= minMatchLen && (ia < 0 || ib < 0)) {
                return true;
            } else {
                if (matchIntlPrefix(a, ia + 1) && matchIntlPrefix(b, ib + 1)) {
                    return true;
                }
                if (matchTrunkPrefix(a, ia + 1) && matchIntlPrefixAndCC(b, ib + 1)) {
                    return true;
                }
                if (matchTrunkPrefix(b, ib + 1) && matchIntlPrefixAndCC(a, ia + 1)) {
                    return true;
                }
                boolean aPlusFirst = a.charAt(0) == '+';
                boolean bPlusFirst = b.charAt(0) == '+';
                if (ia >= 4 || ib >= 4 || (!(aPlusFirst || bPlusFirst) || (aPlusFirst && bPlusFirst))) {
                    vlog("[compareLoosely] return: false");
                    return false;
                }
                Rlog.d(LOG_TAG, "add debug unmatched number if firsit is +");
                return true;
            }
        }
    }
}
