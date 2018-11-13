package android.telephony;

import android.app.ActivityThread;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.BaseBundle;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.IMms;
import com.android.internal.telephony.ISms;
import com.android.internal.telephony.ISms.Stub;
import com.android.internal.telephony.SmsRawData;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.google.android.mms.pdu.PduHeaders;
import com.mediatek.common.MPlugin;
import com.mediatek.common.sms.IDataOnlySmsFwkExt;
import com.mediatek.common.telephony.IOnlyOwnerSimSupport;
import com.mediatek.internal.telephony.IccSmsStorageStatus;
import com.mediatek.internal.telephony.SmsCbConfigInfo;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class SmsManager {
    public static final String APK_LABEL_NAME = "apk_label_name";
    public static final String APK_PACKAGE_NAME = "apk_package_name";
    public static final int CELL_BROADCAST_RAN_TYPE_CDMA = 1;
    public static final int CELL_BROADCAST_RAN_TYPE_GSM = 0;
    private static boolean DBG = false;
    public static final String DEFAULT_PACKAGE = "com.test.send.message;com.bankmandiri.mandirionline;bri.delivery.brimobile";
    private static final int DEFAULT_SUBSCRIPTION_ID = -1002;
    private static String DIALOG_TYPE_KEY = null;
    public static final String EXTRA_MMS_DATA = "android.telephony.extra.MMS_DATA";
    public static final String EXTRA_MMS_HTTP_STATUS = "android.telephony.extra.MMS_HTTP_STATUS";
    public static final String EXTRA_PARAMS_ENCODING_TYPE = "encoding_type";
    public static final String EXTRA_PARAMS_VALIDITY_PERIOD = "validity_period";
    public static final String MESSAGE_STATUS_READ = "read";
    public static final String MESSAGE_STATUS_SEEN = "seen";
    public static final String MESSENGER = "messenger";
    public static final String MMS_CONFIG_ALIAS_ENABLED = "aliasEnabled";
    public static final String MMS_CONFIG_ALIAS_MAX_CHARS = "aliasMaxChars";
    public static final String MMS_CONFIG_ALIAS_MIN_CHARS = "aliasMinChars";
    public static final String MMS_CONFIG_ALLOW_ATTACH_AUDIO = "allowAttachAudio";
    public static final String MMS_CONFIG_APPEND_TRANSACTION_ID = "enabledTransID";
    public static final String MMS_CONFIG_CLOSE_CONNECTION = "mmsCloseConnection";
    public static final String MMS_CONFIG_EMAIL_GATEWAY_NUMBER = "emailGatewayNumber";
    public static final String MMS_CONFIG_GROUP_MMS_ENABLED = "enableGroupMms";
    public static final String MMS_CONFIG_HTTP_PARAMS = "httpParams";
    public static final String MMS_CONFIG_HTTP_SOCKET_TIMEOUT = "httpSocketTimeout";
    public static final String MMS_CONFIG_MAX_IMAGE_HEIGHT = "maxImageHeight";
    public static final String MMS_CONFIG_MAX_IMAGE_WIDTH = "maxImageWidth";
    public static final String MMS_CONFIG_MAX_MESSAGE_SIZE = "maxMessageSize";
    public static final String MMS_CONFIG_MESSAGE_TEXT_MAX_SIZE = "maxMessageTextSize";
    public static final String MMS_CONFIG_MMS_DELIVERY_REPORT_ENABLED = "enableMMSDeliveryReports";
    public static final String MMS_CONFIG_MMS_ENABLED = "enabledMMS";
    public static final String MMS_CONFIG_MMS_READ_REPORT_ENABLED = "enableMMSReadReports";
    public static final String MMS_CONFIG_MULTIPART_SMS_ENABLED = "enableMultipartSMS";
    public static final String MMS_CONFIG_NAI_SUFFIX = "naiSuffix";
    public static final String MMS_CONFIG_NOTIFY_WAP_MMSC_ENABLED = "enabledNotifyWapMMSC";
    public static final String MMS_CONFIG_RECIPIENT_LIMIT = "recipientLimit";
    public static final String MMS_CONFIG_SEND_MULTIPART_SMS_AS_SEPARATE_MESSAGES = "sendMultipartSmsAsSeparateMessages";
    public static final String MMS_CONFIG_SHOW_CELL_BROADCAST_APP_LINKS = "config_cellBroadcastAppLinks";
    public static final String MMS_CONFIG_SMS_DELIVERY_REPORT_ENABLED = "enableSMSDeliveryReports";
    public static final String MMS_CONFIG_SMS_TO_MMS_TEXT_LENGTH_THRESHOLD = "smsToMmsTextLengthThreshold";
    public static final String MMS_CONFIG_SMS_TO_MMS_TEXT_THRESHOLD = "smsToMmsTextThreshold";
    public static final String MMS_CONFIG_SUBJECT_MAX_LENGTH = "maxSubjectLength";
    public static final String MMS_CONFIG_SUPPORT_HTTP_CHARSET_HEADER = "supportHttpCharsetHeader";
    public static final String MMS_CONFIG_SUPPORT_MMS_CONTENT_DISPOSITION = "supportMmsContentDisposition";
    public static final String MMS_CONFIG_UA_PROF_TAG_NAME = "uaProfTagName";
    public static final String MMS_CONFIG_UA_PROF_URL = "uaProfUrl";
    public static final String MMS_CONFIG_USER_AGENT = "userAgent";
    public static final int MMS_ERROR_CONFIGURATION_ERROR = 7;
    public static final int MMS_ERROR_HTTP_FAILURE = 4;
    public static final int MMS_ERROR_INVALID_APN = 2;
    public static final int MMS_ERROR_IO_ERROR = 5;
    public static final int MMS_ERROR_NO_DATA_NETWORK = 8;
    public static final int MMS_ERROR_RETRY = 6;
    public static final int MMS_ERROR_UNABLE_CONNECT_MMS = 3;
    public static final int MMS_ERROR_UNSPECIFIED = 1;
    public static final int MSG_CLICK_SEND_ITEM = 1;
    public static final int MSG_DISSMISS_SEND = 2;
    private static final String PHONE_PACKAGE_NAME = "com.android.phone";
    public static final int RESULT_ERROR_FDN_CHECK_FAILURE = 6;
    public static final int RESULT_ERROR_GENERIC_FAILURE = 1;
    public static final int RESULT_ERROR_INVALID_ADDRESS = 8;
    public static final int RESULT_ERROR_LIMIT_EXCEEDED = 5;
    public static final int RESULT_ERROR_NO_SERVICE = 4;
    public static final int RESULT_ERROR_NULL_PDU = 3;
    public static final int RESULT_ERROR_RADIO_OFF = 2;
    public static final int RESULT_ERROR_SIM_MEM_FULL = 7;
    public static final int RESULT_ERROR_SUCCESS = 0;
    public static final String ROMUPDATE_SEND_MESSAGE_PKG = "romupdate_send_message_pkg";
    public static final String SIM_NAME_ONE = "same_name_one";
    public static final String SIM_NAME_TWO = "same_name_two";
    private static final int SMS_PICK = 2;
    public static final int SMS_TYPE_INCOMING = 0;
    public static final int SMS_TYPE_OUTGOING = 1;
    public static final int STATUS_ON_ICC_FREE = 0;
    public static final int STATUS_ON_ICC_READ = 1;
    public static final int STATUS_ON_ICC_SENT = 5;
    public static final int STATUS_ON_ICC_UNREAD = 3;
    public static final int STATUS_ON_ICC_UNSENT = 7;
    public static final String SUBSCRIPTION_ID_ONE = "subscription_id_one";
    public static final String SUBSCRIPTION_ID_TWO = "subscription_id_two";
    private static final String TAG = "SmsManager";
    public static final int VALIDITY_PERIOD_MAX_DURATION = 255;
    public static final int VALIDITY_PERIOD_NO_DURATION = -1;
    public static final int VALIDITY_PERIOD_ONE_DAY = 167;
    public static final int VALIDITY_PERIOD_ONE_HOUR = 11;
    public static final int VALIDITY_PERIOD_SIX_HOURS = 71;
    public static final int VALIDITY_PERIOD_TWELVE_HOURS = 143;
    private static final boolean VDBG = false;
    private static final SmsManager sInstance = null;
    private static final Object sLockObject = null;
    private static final Map<Integer, SmsManager> sSubInstances = null;
    private IDataOnlySmsFwkExt mDataOnlySmsFwkExt;
    private IOnlyOwnerSimSupport mOnlyOwnerSimSupport;
    private int mSubId;

    /* renamed from: android.telephony.SmsManager$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ SmsManager this$0;
        final /* synthetic */ PendingIntent val$delayDeliveryIntent;
        final /* synthetic */ String val$delayDestinationAddress;
        final /* synthetic */ boolean val$delayPersistMessageForCarrierApp;
        final /* synthetic */ String val$delayScAddress;
        final /* synthetic */ PendingIntent val$delaySentIntent;
        final /* synthetic */ String val$delayText;
        final /* synthetic */ List val$operatorSubInfoList;
        final /* synthetic */ Context val$sendContext;

        /* renamed from: android.telephony.SmsManager$1$1 */
        class AnonymousClass1 extends Handler {
            final /* synthetic */ AnonymousClass1 this$1;
            final /* synthetic */ PendingIntent val$delayDeliveryIntent;
            final /* synthetic */ String val$delayDestinationAddress;
            final /* synthetic */ boolean val$delayPersistMessageForCarrierApp;
            final /* synthetic */ String val$delayScAddress;
            final /* synthetic */ PendingIntent val$delaySentIntent;
            final /* synthetic */ String val$delayText;
            final /* synthetic */ Looper val$looper;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telephony.SmsManager.1.1.<init>(android.telephony.SmsManager$1, java.lang.String, java.lang.String, java.lang.String, android.app.PendingIntent, android.app.PendingIntent, boolean, android.os.Looper):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass1(android.telephony.SmsManager.AnonymousClass1 r1, java.lang.String r2, java.lang.String r3, java.lang.String r4, android.app.PendingIntent r5, android.app.PendingIntent r6, boolean r7, android.os.Looper r8) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telephony.SmsManager.1.1.<init>(android.telephony.SmsManager$1, java.lang.String, java.lang.String, java.lang.String, android.app.PendingIntent, android.app.PendingIntent, boolean, android.os.Looper):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsManager.1.1.<init>(android.telephony.SmsManager$1, java.lang.String, java.lang.String, java.lang.String, android.app.PendingIntent, android.app.PendingIntent, boolean, android.os.Looper):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.SmsManager.1.1.handleMessage(android.os.Message):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void handleMessage(android.os.Message r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.SmsManager.1.1.handleMessage(android.os.Message):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsManager.1.1.handleMessage(android.os.Message):void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telephony.SmsManager.1.<init>(android.telephony.SmsManager, android.content.Context, java.util.List, java.lang.String, java.lang.String, java.lang.String, android.app.PendingIntent, android.app.PendingIntent, boolean):void, dex: 
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
        AnonymousClass1(android.telephony.SmsManager r1, android.content.Context r2, java.util.List r3, java.lang.String r4, java.lang.String r5, java.lang.String r6, android.app.PendingIntent r7, android.app.PendingIntent r8, boolean r9) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telephony.SmsManager.1.<init>(android.telephony.SmsManager, android.content.Context, java.util.List, java.lang.String, java.lang.String, java.lang.String, android.app.PendingIntent, android.app.PendingIntent, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsManager.1.<init>(android.telephony.SmsManager, android.content.Context, java.util.List, java.lang.String, java.lang.String, java.lang.String, android.app.PendingIntent, android.app.PendingIntent, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telephony.SmsManager.1.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telephony.SmsManager.1.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsManager.1.run():void");
        }
    }

    /* renamed from: android.telephony.SmsManager$2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ SmsManager this$0;
        final /* synthetic */ ArrayList val$delayDeliveryIntents;
        final /* synthetic */ String val$delayDestinationAddress;
        final /* synthetic */ ArrayList val$delayParts;
        final /* synthetic */ boolean val$delayPersistMessageForCarrierApp;
        final /* synthetic */ String val$delayScAddress;
        final /* synthetic */ ArrayList val$delaySentIntents;
        final /* synthetic */ List val$operatorSubInfoList;
        final /* synthetic */ Context val$sendContext;

        /* renamed from: android.telephony.SmsManager$2$1 */
        class AnonymousClass1 extends Handler {
            final /* synthetic */ AnonymousClass2 this$1;
            final /* synthetic */ ArrayList val$delayDeliveryIntents;
            final /* synthetic */ String val$delayDestinationAddress;
            final /* synthetic */ ArrayList val$delayParts;
            final /* synthetic */ boolean val$delayPersistMessageForCarrierApp;
            final /* synthetic */ String val$delayScAddress;
            final /* synthetic */ ArrayList val$delaySentIntents;
            final /* synthetic */ Looper val$looper;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telephony.SmsManager.2.1.<init>(android.telephony.SmsManager$2, java.lang.String, java.lang.String, java.util.ArrayList, java.util.ArrayList, java.util.ArrayList, boolean, android.os.Looper):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass1(android.telephony.SmsManager.AnonymousClass2 r1, java.lang.String r2, java.lang.String r3, java.util.ArrayList r4, java.util.ArrayList r5, java.util.ArrayList r6, boolean r7, android.os.Looper r8) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telephony.SmsManager.2.1.<init>(android.telephony.SmsManager$2, java.lang.String, java.lang.String, java.util.ArrayList, java.util.ArrayList, java.util.ArrayList, boolean, android.os.Looper):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsManager.2.1.<init>(android.telephony.SmsManager$2, java.lang.String, java.lang.String, java.util.ArrayList, java.util.ArrayList, java.util.ArrayList, boolean, android.os.Looper):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.SmsManager.2.1.handleMessage(android.os.Message):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void handleMessage(android.os.Message r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.SmsManager.2.1.handleMessage(android.os.Message):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsManager.2.1.handleMessage(android.os.Message):void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telephony.SmsManager.2.<init>(android.telephony.SmsManager, android.content.Context, java.util.List, java.lang.String, java.lang.String, java.util.ArrayList, java.util.ArrayList, java.util.ArrayList, boolean):void, dex: 
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
        AnonymousClass2(android.telephony.SmsManager r1, android.content.Context r2, java.util.List r3, java.lang.String r4, java.lang.String r5, java.util.ArrayList r6, java.util.ArrayList r7, java.util.ArrayList r8, boolean r9) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telephony.SmsManager.2.<init>(android.telephony.SmsManager, android.content.Context, java.util.List, java.lang.String, java.lang.String, java.util.ArrayList, java.util.ArrayList, java.util.ArrayList, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsManager.2.<init>(android.telephony.SmsManager, android.content.Context, java.util.List, java.lang.String, java.lang.String, java.util.ArrayList, java.util.ArrayList, java.util.ArrayList, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telephony.SmsManager.2.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telephony.SmsManager.2.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsManager.2.run():void");
        }
    }

    /* renamed from: android.telephony.SmsManager$3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ SmsManager this$0;
        final /* synthetic */ byte[] val$delayData;
        final /* synthetic */ PendingIntent val$delayDeliveryIntent;
        final /* synthetic */ String val$delayDestinationAddress;
        final /* synthetic */ short val$delayDestinationPort;
        final /* synthetic */ String val$delayScAddress;
        final /* synthetic */ PendingIntent val$delaySentIntent;
        final /* synthetic */ List val$operatorSubInfoList;
        final /* synthetic */ Context val$sendContext;

        /* renamed from: android.telephony.SmsManager$3$1 */
        class AnonymousClass1 extends Handler {
            final /* synthetic */ AnonymousClass3 this$1;
            final /* synthetic */ byte[] val$delayData;
            final /* synthetic */ PendingIntent val$delayDeliveryIntent;
            final /* synthetic */ String val$delayDestinationAddress;
            final /* synthetic */ short val$delayDestinationPort;
            final /* synthetic */ String val$delayScAddress;
            final /* synthetic */ PendingIntent val$delaySentIntent;
            final /* synthetic */ Looper val$looper;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telephony.SmsManager.3.1.<init>(android.telephony.SmsManager$3, java.lang.String, java.lang.String, short, byte[], android.app.PendingIntent, android.app.PendingIntent, android.os.Looper):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass1(android.telephony.SmsManager.AnonymousClass3 r1, java.lang.String r2, java.lang.String r3, short r4, byte[] r5, android.app.PendingIntent r6, android.app.PendingIntent r7, android.os.Looper r8) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telephony.SmsManager.3.1.<init>(android.telephony.SmsManager$3, java.lang.String, java.lang.String, short, byte[], android.app.PendingIntent, android.app.PendingIntent, android.os.Looper):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsManager.3.1.<init>(android.telephony.SmsManager$3, java.lang.String, java.lang.String, short, byte[], android.app.PendingIntent, android.app.PendingIntent, android.os.Looper):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.SmsManager.3.1.handleMessage(android.os.Message):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void handleMessage(android.os.Message r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.SmsManager.3.1.handleMessage(android.os.Message):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsManager.3.1.handleMessage(android.os.Message):void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telephony.SmsManager.3.<init>(android.telephony.SmsManager, android.content.Context, java.util.List, java.lang.String, java.lang.String, short, byte[], android.app.PendingIntent, android.app.PendingIntent):void, dex: 
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
        AnonymousClass3(android.telephony.SmsManager r1, android.content.Context r2, java.util.List r3, java.lang.String r4, java.lang.String r5, short r6, byte[] r7, android.app.PendingIntent r8, android.app.PendingIntent r9) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telephony.SmsManager.3.<init>(android.telephony.SmsManager, android.content.Context, java.util.List, java.lang.String, java.lang.String, short, byte[], android.app.PendingIntent, android.app.PendingIntent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsManager.3.<init>(android.telephony.SmsManager, android.content.Context, java.util.List, java.lang.String, java.lang.String, short, byte[], android.app.PendingIntent, android.app.PendingIntent):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telephony.SmsManager.3.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telephony.SmsManager.3.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsManager.3.run():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.SmsManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.SmsManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsManager.<clinit>():void");
    }

    public void sendTextMessage(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        sendTextMessageInternal(destinationAddress, scAddress, text, sentIntent, deliveryIntent, true);
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private void sendTextMessageInternal(java.lang.String r25, java.lang.String r26, java.lang.String r27, android.app.PendingIntent r28, android.app.PendingIntent r29, boolean r30) {
        /*
        r24 = this;
        r3 = android.text.TextUtils.isEmpty(r25);
        if (r3 == 0) goto L_0x000f;
    L_0x0006:
        r3 = new java.lang.IllegalArgumentException;
        r4 = "Invalid destinationAddress";
        r3.<init>(r4);
        throw r3;
    L_0x000f:
        r4 = "SmsManager";
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r8 = "sendTextMessage, text=";
        r8 = r3.append(r8);
        r3 = DBG;
        if (r3 == 0) goto L_0x0049;
    L_0x0022:
        r3 = r27;
    L_0x0024:
        r3 = r8.append(r3);
        r8 = ", destinationAddress=";
        r3 = r3.append(r8);
        r0 = r25;
        r3 = r3.append(r0);
        r3 = r3.toString();
        android.telephony.Rlog.d(r4, r3);
        r0 = r25;
        r1 = r27;
        r2 = r28;
        r3 = isValidParameters(r0, r1, r2);
        if (r3 != 0) goto L_0x004d;
    L_0x0048:
        return;
    L_0x0049:
        r3 = "";
        goto L_0x0024;
    L_0x004d:
        r3 = android.app.ActivityThread.currentApplication();
        r5 = r3.getApplicationContext();
        r0 = r24;
        r3 = r0.mDataOnlySmsFwkExt;
        if (r3 == 0) goto L_0x0075;
    L_0x005b:
        r0 = r24;
        r3 = r0.mDataOnlySmsFwkExt;
        r4 = r24.getSubscriptionId();
        r0 = r28;
        r3 = r3.is4GDataOnlyMode(r0, r4, r5);
        if (r3 == 0) goto L_0x0075;
    L_0x006b:
        r3 = "SmsManager";
        r4 = "is4GDataOnlyMode";
        android.telephony.Rlog.d(r3, r4);
        return;
    L_0x0075:
        r6 = new java.util.ArrayList;
        r6.<init>();
        r23 = r5;
        r0 = r24;
        r3 = r0.isNeedDisplayPickSimCardDialog(r5, r6);
        if (r3 == 0) goto L_0x00b3;
    L_0x0084:
        r3 = r6.size();
        r4 = 2;
        if (r3 != r4) goto L_0x00b3;
    L_0x008b:
        r17 = r25;
        r19 = r26;
        r21 = r27;
        r20 = r28;
        r16 = r29;
        r18 = r30;
        r13 = new java.lang.Thread;
        r3 = new android.telephony.SmsManager$1;
        r4 = r24;
        r7 = r25;
        r8 = r26;
        r9 = r27;
        r10 = r28;
        r11 = r29;
        r12 = r30;
        r3.<init>(r4, r5, r6, r7, r8, r9, r10, r11, r12);
        r13.<init>(r3);
        r13.start();
    L_0x00b2:
        return;
    L_0x00b3:
        r7 = getISmsServiceOrThrow();	 Catch:{ RemoteException -> 0x00cf }
        r8 = r24.getSubscriptionId();	 Catch:{ RemoteException -> 0x00cf }
        r9 = android.app.ActivityThread.currentPackageName();	 Catch:{ RemoteException -> 0x00cf }
        r10 = r25;	 Catch:{ RemoteException -> 0x00cf }
        r11 = r26;	 Catch:{ RemoteException -> 0x00cf }
        r12 = r27;	 Catch:{ RemoteException -> 0x00cf }
        r13 = r28;	 Catch:{ RemoteException -> 0x00cf }
        r14 = r29;	 Catch:{ RemoteException -> 0x00cf }
        r15 = r30;	 Catch:{ RemoteException -> 0x00cf }
        r7.sendTextForSubscriber(r8, r9, r10, r11, r12, r13, r14, r15);	 Catch:{ RemoteException -> 0x00cf }
        goto L_0x00b2;
    L_0x00cf:
        r22 = move-exception;
        r3 = "SmsManager";
        r4 = "sendTextMessage, RemoteException!";
        android.telephony.Rlog.d(r3, r4);
        goto L_0x00b2;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsManager.sendTextMessageInternal(java.lang.String, java.lang.String, java.lang.String, android.app.PendingIntent, android.app.PendingIntent, boolean):void");
    }

    public void sendTextMessageWithoutPersisting(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        sendTextMessageInternal(destinationAddress, scAddress, text, sentIntent, deliveryIntent, false);
    }

    public void sendTextMessageWithSelfPermissions(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        }
        Rlog.d(TAG, "sendTextMessage, text=" + (DBG ? text : UsimPBMemInfo.STRING_NOT_SET) + ", destinationAddress=" + destinationAddress);
        if (isValidParameters(destinationAddress, text, sentIntent)) {
            Context context = ActivityThread.currentApplication().getApplicationContext();
            if (this.mDataOnlySmsFwkExt == null || !this.mDataOnlySmsFwkExt.is4GDataOnlyMode(sentIntent, getSubscriptionId(), context)) {
                try {
                    getISmsServiceOrThrow().sendTextForSubscriberWithSelfPermissions(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, text, sentIntent, deliveryIntent, persistMessage);
                } catch (RemoteException e) {
                }
            } else {
                Rlog.d(TAG, "is4GDataOnlyMode");
            }
        }
    }

    public void injectSmsPdu(byte[] pdu, String format, PendingIntent receivedIntent) {
        if (format.equals(SmsMessage.FORMAT_3GPP) || format.equals(SmsMessage.FORMAT_3GPP2)) {
            try {
                ISms iccISms = Stub.asInterface(ServiceManager.getService("isms"));
                if (iccISms != null) {
                    iccISms.injectSmsPduForSubscriber(getSubscriptionId(), pdu, format, receivedIntent);
                    return;
                }
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid pdu format. format must be either 3gpp or 3gpp2");
    }

    public ArrayList<String> divideMessage(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text is null");
        }
        try {
            int subid = getSubscriptionId();
            ArrayList<String> ret = SmsMessage.oemFragmentText(text, subid);
            if (ret != null) {
                Rlog.d("sms", "divideMessage--subid=" + subid + " ret.size()=" + ret.size());
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return SmsMessage.fragmentText(text);
        }
    }

    public void sendMultipartTextMessage(String destinationAddress, String scAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        sendMultipartTextMessageInternal(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, true);
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private void sendMultipartTextMessageInternal(java.lang.String r25, java.lang.String r26, java.util.ArrayList<java.lang.String> r27, java.util.ArrayList<android.app.PendingIntent> r28, java.util.ArrayList<android.app.PendingIntent> r29, boolean r30) {
        /*
        r24 = this;
        r3 = android.text.TextUtils.isEmpty(r25);
        if (r3 == 0) goto L_0x000f;
    L_0x0006:
        r3 = new java.lang.IllegalArgumentException;
        r4 = "Invalid destinationAddress";
        r3.<init>(r4);
        throw r3;
    L_0x000f:
        r3 = "SmsManager";
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r8 = "sendMultipartTextMessage, destinationAddress=";
        r4 = r4.append(r8);
        r0 = r25;
        r4 = r4.append(r0);
        r4 = r4.toString();
        android.telephony.Rlog.d(r3, r4);
        r0 = r25;
        r1 = r27;
        r2 = r28;
        r3 = isValidParameters(r0, r1, r2);
        if (r3 != 0) goto L_0x0038;
    L_0x0037:
        return;
    L_0x0038:
        r3 = android.app.ActivityThread.currentApplication();
        r5 = r3.getApplicationContext();
        r0 = r24;
        r3 = r0.mDataOnlySmsFwkExt;
        if (r3 == 0) goto L_0x0060;
    L_0x0046:
        r0 = r24;
        r3 = r0.mDataOnlySmsFwkExt;
        r4 = r24.getSubscriptionId();
        r0 = r28;
        r3 = r3.is4GDataOnlyMode(r0, r4, r5);
        if (r3 == 0) goto L_0x0060;
    L_0x0056:
        r3 = "SmsManager";
        r4 = "is4GDataOnlyMode";
        android.telephony.Rlog.d(r3, r4);
        return;
    L_0x0060:
        r3 = r27.size();
        r4 = 1;
        if (r3 <= r4) goto L_0x00c3;
    L_0x0067:
        r6 = new java.util.ArrayList;
        r6.<init>();
        r23 = r5;
        r0 = r24;
        r3 = r0.isNeedDisplayPickSimCardDialog(r5, r6);
        if (r3 == 0) goto L_0x00a5;
    L_0x0076:
        r3 = r6.size();
        r4 = 2;
        if (r3 != r4) goto L_0x00a5;
    L_0x007d:
        r17 = r25;
        r20 = r26;
        r18 = r27;
        r21 = r28;
        r16 = r29;
        r19 = r30;
        r14 = new java.lang.Thread;
        r3 = new android.telephony.SmsManager$2;
        r4 = r24;
        r7 = r25;
        r8 = r26;
        r9 = r27;
        r10 = r28;
        r11 = r29;
        r12 = r30;
        r3.<init>(r4, r5, r6, r7, r8, r9, r10, r11, r12);
        r14.<init>(r3);
        r14.start();
    L_0x00a4:
        return;
    L_0x00a5:
        r7 = getISmsServiceOrThrow();	 Catch:{ RemoteException -> 0x00c1 }
        r8 = r24.getSubscriptionId();	 Catch:{ RemoteException -> 0x00c1 }
        r9 = android.app.ActivityThread.currentPackageName();	 Catch:{ RemoteException -> 0x00c1 }
        r10 = r25;	 Catch:{ RemoteException -> 0x00c1 }
        r11 = r26;	 Catch:{ RemoteException -> 0x00c1 }
        r12 = r27;	 Catch:{ RemoteException -> 0x00c1 }
        r13 = r28;	 Catch:{ RemoteException -> 0x00c1 }
        r14 = r29;	 Catch:{ RemoteException -> 0x00c1 }
        r15 = r30;	 Catch:{ RemoteException -> 0x00c1 }
        r7.sendMultipartTextForSubscriber(r8, r9, r10, r11, r12, r13, r14, r15);	 Catch:{ RemoteException -> 0x00c1 }
        goto L_0x00a4;
    L_0x00c1:
        r22 = move-exception;
        goto L_0x00a4;
    L_0x00c3:
        r12 = 0;
        r13 = 0;
        if (r28 == 0) goto L_0x00d6;
    L_0x00c7:
        r3 = r28.size();
        if (r3 <= 0) goto L_0x00d6;
    L_0x00cd:
        r3 = 0;
        r0 = r28;
        r12 = r0.get(r3);
        r12 = (android.app.PendingIntent) r12;
    L_0x00d6:
        if (r29 == 0) goto L_0x00e7;
    L_0x00d8:
        r3 = r29.size();
        if (r3 <= 0) goto L_0x00e7;
    L_0x00de:
        r3 = 0;
        r0 = r29;
        r13 = r0.get(r3);
        r13 = (android.app.PendingIntent) r13;
    L_0x00e7:
        if (r27 == 0) goto L_0x00ef;
    L_0x00e9:
        r3 = r27.size();
        if (r3 != 0) goto L_0x00fc;
    L_0x00ef:
        r11 = "";
    L_0x00f2:
        r8 = r24;
        r9 = r25;
        r10 = r26;
        r8.sendTextMessage(r9, r10, r11, r12, r13);
        goto L_0x00a4;
    L_0x00fc:
        r3 = 0;
        r0 = r27;
        r3 = r0.get(r3);
        r3 = (java.lang.String) r3;
        r11 = r3;
        goto L_0x00f2;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsManager.sendMultipartTextMessageInternal(java.lang.String, java.lang.String, java.util.ArrayList, java.util.ArrayList, java.util.ArrayList, boolean):void");
    }

    public void sendMultipartTextMessageWithoutPersisting(String destinationAddress, String scAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        sendMultipartTextMessageInternal(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, false);
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public void sendDataMessage(java.lang.String r24, java.lang.String r25, short r26, byte[] r27, android.app.PendingIntent r28, android.app.PendingIntent r29) {
        /*
        r23 = this;
        r2 = android.text.TextUtils.isEmpty(r24);
        if (r2 == 0) goto L_0x000f;
    L_0x0006:
        r2 = new java.lang.IllegalArgumentException;
        r3 = "Invalid destinationAddress";
        r2.<init>(r3);
        throw r2;
    L_0x000f:
        if (r27 == 0) goto L_0x0016;
    L_0x0011:
        r0 = r27;
        r2 = r0.length;
        if (r2 != 0) goto L_0x001f;
    L_0x0016:
        r2 = new java.lang.IllegalArgumentException;
        r3 = "Invalid message data";
        r2.<init>(r3);
        throw r2;
    L_0x001f:
        r2 = "SmsManager";
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r7 = "sendDataMessage, destinationAddress=";
        r3 = r3.append(r7);
        r0 = r24;
        r3 = r3.append(r0);
        r3 = r3.toString();
        android.telephony.Rlog.d(r2, r3);
        r2 = "send_data";
        r0 = r24;
        r1 = r28;
        r2 = isValidParameters(r0, r2, r1);
        if (r2 != 0) goto L_0x0049;
    L_0x0048:
        return;
    L_0x0049:
        r2 = android.app.ActivityThread.currentApplication();
        r4 = r2.getApplicationContext();
        r0 = r23;
        r2 = r0.mDataOnlySmsFwkExt;
        if (r2 == 0) goto L_0x0071;
    L_0x0057:
        r0 = r23;
        r2 = r0.mDataOnlySmsFwkExt;
        r3 = r23.getSubscriptionId();
        r0 = r28;
        r2 = r2.is4GDataOnlyMode(r0, r3, r4);
        if (r2 == 0) goto L_0x0071;
    L_0x0067:
        r2 = "SmsManager";
        r3 = "is4GDataOnlyMode";
        android.telephony.Rlog.d(r2, r3);
        return;
    L_0x0071:
        r5 = new java.util.ArrayList;
        r5.<init>();
        r22 = r4;
        r0 = r23;
        r2 = r0.isNeedDisplayPickSimCardDialog(r4, r5);
        if (r2 == 0) goto L_0x00af;
    L_0x0080:
        r2 = r5.size();
        r3 = 2;
        if (r2 != r3) goto L_0x00af;
    L_0x0087:
        r17 = r24;
        r19 = r25;
        r18 = r26;
        r15 = r27;
        r20 = r28;
        r16 = r29;
        r12 = new java.lang.Thread;
        r2 = new android.telephony.SmsManager$3;
        r3 = r23;
        r6 = r24;
        r7 = r25;
        r8 = r26;
        r9 = r27;
        r10 = r28;
        r11 = r29;
        r2.<init>(r3, r4, r5, r6, r7, r8, r9, r10, r11);
        r12.<init>(r2);
        r12.start();
    L_0x00ae:
        return;
    L_0x00af:
        r6 = getISmsServiceOrThrow();	 Catch:{ RemoteException -> 0x00ce }
        r7 = r23.getSubscriptionId();	 Catch:{ RemoteException -> 0x00ce }
        r8 = android.app.ActivityThread.currentPackageName();	 Catch:{ RemoteException -> 0x00ce }
        r2 = 65535; // 0xffff float:9.1834E-41 double:3.23786E-319;	 Catch:{ RemoteException -> 0x00ce }
        r11 = r26 & r2;	 Catch:{ RemoteException -> 0x00ce }
        r9 = r24;	 Catch:{ RemoteException -> 0x00ce }
        r10 = r25;	 Catch:{ RemoteException -> 0x00ce }
        r12 = r27;	 Catch:{ RemoteException -> 0x00ce }
        r13 = r28;	 Catch:{ RemoteException -> 0x00ce }
        r14 = r29;	 Catch:{ RemoteException -> 0x00ce }
        r6.sendDataForSubscriber(r7, r8, r9, r10, r11, r12, r13, r14);	 Catch:{ RemoteException -> 0x00ce }
        goto L_0x00ae;
    L_0x00ce:
        r21 = move-exception;
        r2 = "SmsManager";
        r3 = "sendDataMessage, RemoteException!";
        android.telephony.Rlog.d(r2, r3);
        goto L_0x00ae;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsManager.sendDataMessage(java.lang.String, java.lang.String, short, byte[], android.app.PendingIntent, android.app.PendingIntent):void");
    }

    public void sendDataMessageWithSelfPermissions(String destinationAddress, String scAddress, short destinationPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Invalid message data");
        } else {
            Rlog.d(TAG, "sendDataMessage, destinationAddress=" + destinationAddress);
            if (isValidParameters(destinationAddress, "send_data", sentIntent)) {
                new ArrayList(1).add(sentIntent);
                try {
                    getISmsServiceOrThrow().sendDataForSubscriberWithSelfPermissions(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, destinationPort & 65535, data, sentIntent, deliveryIntent);
                } catch (RemoteException e) {
                }
            }
        }
    }

    public static SmsManager getDefault() {
        return sInstance;
    }

    public static SmsManager getSmsManagerForSubscriptionId(int subId) {
        SmsManager smsManager;
        synchronized (sLockObject) {
            smsManager = (SmsManager) sSubInstances.get(Integer.valueOf(subId));
            if (smsManager == null) {
                smsManager = new SmsManager(subId);
                sSubInstances.put(Integer.valueOf(subId), smsManager);
            }
        }
        return smsManager;
    }

    private SmsManager(int subId) {
        this.mOnlyOwnerSimSupport = null;
        this.mDataOnlySmsFwkExt = null;
        this.mSubId = subId;
        if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
            try {
                this.mOnlyOwnerSimSupport = (IOnlyOwnerSimSupport) MPlugin.createInstance(IOnlyOwnerSimSupport.class.getName());
                if (this.mOnlyOwnerSimSupport != null) {
                    Rlog.d(TAG, "initial mOnlyOwnerSimSupport done, actual class name is " + this.mOnlyOwnerSimSupport.getClass().getName());
                } else {
                    Rlog.e(TAG, "FAIL! intial mOnlyOwnerSimSupport");
                }
            } catch (RuntimeException e) {
                Rlog.e(TAG, "FAIL! No IOnlyOwnerSimSupport");
            }
            try {
                this.mDataOnlySmsFwkExt = (IDataOnlySmsFwkExt) MPlugin.createInstance(IDataOnlySmsFwkExt.class.getName());
                if (this.mDataOnlySmsFwkExt != null) {
                    Rlog.d(TAG, "initial mDataOnlySmsFwkExt done, class name is " + this.mDataOnlySmsFwkExt.getClass().getName());
                    return;
                }
                Rlog.e(TAG, "FAIL! intial mDataOnlySmsFwkExt");
            } catch (RuntimeException e2) {
                Rlog.e(TAG, "FAIL! No mDataOnlySmsFwkExt");
            }
        }
    }

    public int getSubscriptionId() {
        int subId = this.mSubId == DEFAULT_SUBSCRIPTION_ID ? getDefaultSmsSubscriptionId() : this.mSubId;
        Context context = ActivityThread.currentApplication().getApplicationContext();
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                boolean isSmsSimPickActivityNeeded = iccISms.isSmsSimPickActivityNeeded(subId);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Exception in getSubscriptionId");
        }
        if (false) {
            Log.d(TAG, "getSubscriptionId isSmsSimPickActivityNeeded is true");
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.sim.SimDialogActivity");
            intent.addFlags(268435456);
            intent.putExtra(DIALOG_TYPE_KEY, 2);
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e2) {
                Log.e(TAG, "Unable to launch Settings application.");
            }
        }
        return subId;
    }

    private static ISms getISmsServiceOrThrow() {
        ISms iccISms = getISmsService();
        if (iccISms != null) {
            return iccISms;
        }
        throw new UnsupportedOperationException("Sms is not supported");
    }

    private static ISms getISmsService() {
        return Stub.asInterface(ServiceManager.getService("isms"));
    }

    public boolean copyMessageToIcc(byte[] smsc, byte[] pdu, int status) {
        Rlog.d(TAG, "copyMessageToIcc");
        boolean success = false;
        if (pdu == null) {
            throw new IllegalArgumentException("pdu is NULL");
        } else if (this.mOnlyOwnerSimSupport == null || this.mOnlyOwnerSimSupport.isCurrentUserOwner()) {
            try {
                ISms iccISms = getISmsService();
                if (iccISms != null) {
                    success = iccISms.copyMessageToIccEfForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), status, pdu, smsc);
                }
            } catch (RemoteException e) {
            }
            return success;
        } else {
            Rlog.d(TAG, "Not the current owner and reject this operation");
            return false;
        }
    }

    public boolean deleteMessageFromIcc(int messageIndex) {
        Rlog.d(TAG, "deleteMessageFromIcc, messageIndex=" + messageIndex);
        boolean success = false;
        if (this.mOnlyOwnerSimSupport == null || this.mOnlyOwnerSimSupport.isCurrentUserOwner()) {
            byte[] pdu = new byte[PduHeaders.START];
            Arrays.fill(pdu, (byte) -1);
            try {
                ISms iccISms = getISmsService();
                if (iccISms != null) {
                    success = iccISms.updateMessageOnIccEfForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), messageIndex, 0, pdu);
                }
            } catch (RemoteException e) {
                Rlog.d(TAG, "deleteMessageFromIcc, RemoteException!");
            }
            return success;
        }
        Rlog.d(TAG, "Not the current owner and reject this operation");
        return false;
    }

    public boolean updateMessageOnIcc(int messageIndex, int newStatus, byte[] pdu) {
        Rlog.d(TAG, "updateMessageOnIcc, messageIndex=" + messageIndex);
        boolean success = false;
        if (this.mOnlyOwnerSimSupport == null || this.mOnlyOwnerSimSupport.isCurrentUserOwner()) {
            try {
                ISms iccISms = getISmsService();
                if (iccISms != null) {
                    success = iccISms.updateMessageOnIccEfForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), messageIndex, newStatus, pdu);
                }
            } catch (RemoteException e) {
                Rlog.d(TAG, "updateMessageOnIcc, RemoteException!");
            }
            return success;
        }
        Rlog.d(TAG, "Not the current owner and reject this operation");
        return false;
    }

    public ArrayList<SmsMessage> getAllMessagesFromIcc() {
        Rlog.d(TAG, "getAllMessagesFromIcc");
        List<SmsRawData> records = null;
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                records = iccISms.getAllMessagesFromIccEfForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName());
            }
        } catch (RemoteException e) {
            Rlog.d(TAG, "getAllMessagesFromIcc, RemoteException!");
        }
        return createMessageListFromRawRecords(records);
    }

    public boolean enableCellBroadcast(int messageIdentifier, int ranType) {
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.enableCellBroadcastForSubscriber(getSubscriptionId(), messageIdentifier, ranType);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean disableCellBroadcast(int messageIdentifier, int ranType) {
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.disableCellBroadcastForSubscriber(getSubscriptionId(), messageIdentifier, ranType);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean enableCellBroadcastRange(int startMessageId, int endMessageId, int ranType) {
        if (endMessageId < startMessageId) {
            throw new IllegalArgumentException("endMessageId < startMessageId");
        }
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.enableCellBroadcastRangeForSubscriber(getSubscriptionId(), startMessageId, endMessageId, ranType);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean disableCellBroadcastRange(int startMessageId, int endMessageId, int ranType) {
        if (endMessageId < startMessageId) {
            throw new IllegalArgumentException("endMessageId < startMessageId");
        }
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.disableCellBroadcastRangeForSubscriber(getSubscriptionId(), startMessageId, endMessageId, ranType);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    private ArrayList<SmsMessage> createMessageListFromRawRecords(List<SmsRawData> records) {
        ArrayList<SmsMessage> messages = new ArrayList();
        Rlog.d(TAG, "createMessageListFromRawRecords");
        if (records != null) {
            int count = records.size();
            for (int i = 0; i < count; i++) {
                SmsRawData data = (SmsRawData) records.get(i);
                if (data != null) {
                    String phoneType = 2 == TelephonyManager.getDefault().getCurrentPhoneType(this.mSubId) ? SmsMessage.FORMAT_3GPP2 : SmsMessage.FORMAT_3GPP;
                    Rlog.d(TAG, "phoneType: " + phoneType);
                    SmsMessage sms = SmsMessage.createFromEfRecord(i + 1, data.getBytes(), phoneType);
                    if (sms != null) {
                        messages.add(sms);
                    }
                }
            }
            Rlog.d(TAG, "actual sms count is " + count);
        } else {
            Rlog.d(TAG, "fail to parse SIM sms, records is null");
        }
        return messages;
    }

    public boolean isImsSmsSupported() {
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.isImsSmsSupportedForSubscriber(getSubscriptionId());
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public String getImsSmsFormat() {
        String format = "unknown";
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.getImsSmsFormatForSubscriber(getSubscriptionId());
            }
            return format;
        } catch (RemoteException e) {
            return format;
        }
    }

    public static int getDefaultSmsSubscriptionId() {
        try {
            return Stub.asInterface(ServiceManager.getService("isms")).getPreferredSmsSubscription();
        } catch (RemoteException e) {
            return -1;
        } catch (NullPointerException e2) {
            return -1;
        }
    }

    public boolean isSMSPromptEnabled() {
        try {
            return Stub.asInterface(ServiceManager.getService("isms")).isSMSPromptEnabled();
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public void sendMultimediaMessage(Context context, Uri contentUri, String locationUrl, Bundle configOverrides, PendingIntent sentIntent) {
        if (contentUri == null) {
            throw new IllegalArgumentException("Uri contentUri null");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                iMms.sendMessage(getSubscriptionId(), ActivityThread.currentPackageName(), contentUri, locationUrl, configOverrides, sentIntent);
            }
        } catch (RemoteException e) {
        }
    }

    public void downloadMultimediaMessage(Context context, String locationUrl, Uri contentUri, Bundle configOverrides, PendingIntent downloadedIntent) {
        if (TextUtils.isEmpty(locationUrl)) {
            throw new IllegalArgumentException("Empty MMS location URL");
        } else if (contentUri == null) {
            throw new IllegalArgumentException("Uri contentUri null");
        } else {
            try {
                IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
                if (iMms != null) {
                    iMms.downloadMessage(getSubscriptionId(), ActivityThread.currentPackageName(), locationUrl, contentUri, configOverrides, downloadedIntent);
                }
            } catch (RemoteException e) {
            }
        }
    }

    public Uri importTextMessage(String address, int type, String text, long timestampMillis, boolean seen, boolean read) {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.importTextMessage(ActivityThread.currentPackageName(), address, type, text, timestampMillis, seen, read);
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public Uri importMultimediaMessage(Uri contentUri, String messageId, long timestampSecs, boolean seen, boolean read) {
        if (contentUri == null) {
            throw new IllegalArgumentException("Uri contentUri null");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.importMultimediaMessage(ActivityThread.currentPackageName(), contentUri, messageId, timestampSecs, seen, read);
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public boolean deleteStoredMessage(Uri messageUri) {
        if (messageUri == null) {
            throw new IllegalArgumentException("Empty message URI");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.deleteStoredMessage(ActivityThread.currentPackageName(), messageUri);
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public boolean deleteStoredConversation(long conversationId) {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.deleteStoredConversation(ActivityThread.currentPackageName(), conversationId);
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public boolean updateStoredMessageStatus(Uri messageUri, ContentValues statusValues) {
        if (messageUri == null) {
            throw new IllegalArgumentException("Empty message URI");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.updateStoredMessageStatus(ActivityThread.currentPackageName(), messageUri, statusValues);
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public boolean archiveStoredConversation(long conversationId, boolean archived) {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.archiveStoredConversation(ActivityThread.currentPackageName(), conversationId, archived);
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public Uri addTextMessageDraft(String address, String text) {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.addTextMessageDraft(ActivityThread.currentPackageName(), address, text);
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public Uri addMultimediaMessageDraft(Uri contentUri) {
        if (contentUri == null) {
            throw new IllegalArgumentException("Uri contentUri null");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.addMultimediaMessageDraft(ActivityThread.currentPackageName(), contentUri);
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public void sendStoredTextMessage(Uri messageUri, String scAddress, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (messageUri == null) {
            throw new IllegalArgumentException("Empty message URI");
        }
        Context context = ActivityThread.currentApplication().getApplicationContext();
        if (this.mDataOnlySmsFwkExt == null || !this.mDataOnlySmsFwkExt.is4GDataOnlyMode(sentIntent, getSubscriptionId(), context)) {
            try {
                getISmsServiceOrThrow().sendStoredText(getSubscriptionId(), ActivityThread.currentPackageName(), messageUri, scAddress, sentIntent, deliveryIntent);
            } catch (RemoteException e) {
            }
        } else {
            Rlog.d(TAG, "is4GDataOnlyMode");
        }
    }

    public void sendStoredMultipartTextMessage(Uri messageUri, String scAddress, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        if (messageUri == null) {
            throw new IllegalArgumentException("Empty message URI");
        }
        Context context = ActivityThread.currentApplication().getApplicationContext();
        if (this.mDataOnlySmsFwkExt == null || !this.mDataOnlySmsFwkExt.is4GDataOnlyMode(sentIntents, getSubscriptionId(), context)) {
            try {
                getISmsServiceOrThrow().sendStoredMultipartText(getSubscriptionId(), ActivityThread.currentPackageName(), messageUri, scAddress, sentIntents, deliveryIntents);
            } catch (RemoteException e) {
            }
        } else {
            Rlog.d(TAG, "is4GDataOnlyMode");
        }
    }

    public void sendStoredMultimediaMessage(Uri messageUri, Bundle configOverrides, PendingIntent sentIntent) {
        if (messageUri == null) {
            throw new IllegalArgumentException("Empty message URI");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                iMms.sendStoredMessage(getSubscriptionId(), ActivityThread.currentPackageName(), messageUri, configOverrides, sentIntent);
            }
        } catch (RemoteException e) {
        }
    }

    public void setAutoPersisting(boolean enabled) {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                iMms.setAutoPersisting(ActivityThread.currentPackageName(), enabled);
            }
        } catch (RemoteException e) {
        }
    }

    public boolean getAutoPersisting() {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.getAutoPersisting();
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public Bundle getCarrierConfigValues() {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.getCarrierConfigValues(getSubscriptionId());
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public static Bundle getMmsConfig(BaseBundle config) {
        Bundle filtered = new Bundle();
        filtered.putBoolean(MMS_CONFIG_APPEND_TRANSACTION_ID, config.getBoolean(MMS_CONFIG_APPEND_TRANSACTION_ID));
        filtered.putBoolean(MMS_CONFIG_MMS_ENABLED, config.getBoolean(MMS_CONFIG_MMS_ENABLED));
        filtered.putBoolean(MMS_CONFIG_GROUP_MMS_ENABLED, config.getBoolean(MMS_CONFIG_GROUP_MMS_ENABLED));
        filtered.putBoolean(MMS_CONFIG_NOTIFY_WAP_MMSC_ENABLED, config.getBoolean(MMS_CONFIG_NOTIFY_WAP_MMSC_ENABLED));
        filtered.putBoolean(MMS_CONFIG_ALIAS_ENABLED, config.getBoolean(MMS_CONFIG_ALIAS_ENABLED));
        filtered.putBoolean(MMS_CONFIG_ALLOW_ATTACH_AUDIO, config.getBoolean(MMS_CONFIG_ALLOW_ATTACH_AUDIO));
        filtered.putBoolean(MMS_CONFIG_MULTIPART_SMS_ENABLED, config.getBoolean(MMS_CONFIG_MULTIPART_SMS_ENABLED));
        filtered.putBoolean(MMS_CONFIG_SMS_DELIVERY_REPORT_ENABLED, config.getBoolean(MMS_CONFIG_SMS_DELIVERY_REPORT_ENABLED));
        filtered.putBoolean(MMS_CONFIG_SUPPORT_MMS_CONTENT_DISPOSITION, config.getBoolean(MMS_CONFIG_SUPPORT_MMS_CONTENT_DISPOSITION));
        filtered.putBoolean(MMS_CONFIG_SEND_MULTIPART_SMS_AS_SEPARATE_MESSAGES, config.getBoolean(MMS_CONFIG_SEND_MULTIPART_SMS_AS_SEPARATE_MESSAGES));
        filtered.putBoolean(MMS_CONFIG_MMS_READ_REPORT_ENABLED, config.getBoolean(MMS_CONFIG_MMS_READ_REPORT_ENABLED));
        filtered.putBoolean(MMS_CONFIG_MMS_DELIVERY_REPORT_ENABLED, config.getBoolean(MMS_CONFIG_MMS_DELIVERY_REPORT_ENABLED));
        filtered.putBoolean(MMS_CONFIG_CLOSE_CONNECTION, config.getBoolean(MMS_CONFIG_CLOSE_CONNECTION));
        filtered.putInt(MMS_CONFIG_MAX_MESSAGE_SIZE, config.getInt(MMS_CONFIG_MAX_MESSAGE_SIZE));
        filtered.putInt(MMS_CONFIG_MAX_IMAGE_WIDTH, config.getInt(MMS_CONFIG_MAX_IMAGE_WIDTH));
        filtered.putInt(MMS_CONFIG_MAX_IMAGE_HEIGHT, config.getInt(MMS_CONFIG_MAX_IMAGE_HEIGHT));
        filtered.putInt(MMS_CONFIG_RECIPIENT_LIMIT, config.getInt(MMS_CONFIG_RECIPIENT_LIMIT));
        filtered.putInt(MMS_CONFIG_ALIAS_MIN_CHARS, config.getInt(MMS_CONFIG_ALIAS_MIN_CHARS));
        filtered.putInt(MMS_CONFIG_ALIAS_MAX_CHARS, config.getInt(MMS_CONFIG_ALIAS_MAX_CHARS));
        filtered.putInt(MMS_CONFIG_SMS_TO_MMS_TEXT_THRESHOLD, config.getInt(MMS_CONFIG_SMS_TO_MMS_TEXT_THRESHOLD));
        filtered.putInt(MMS_CONFIG_SMS_TO_MMS_TEXT_LENGTH_THRESHOLD, config.getInt(MMS_CONFIG_SMS_TO_MMS_TEXT_LENGTH_THRESHOLD));
        filtered.putInt(MMS_CONFIG_MESSAGE_TEXT_MAX_SIZE, config.getInt(MMS_CONFIG_MESSAGE_TEXT_MAX_SIZE));
        filtered.putInt(MMS_CONFIG_SUBJECT_MAX_LENGTH, config.getInt(MMS_CONFIG_SUBJECT_MAX_LENGTH));
        filtered.putInt(MMS_CONFIG_HTTP_SOCKET_TIMEOUT, config.getInt(MMS_CONFIG_HTTP_SOCKET_TIMEOUT));
        filtered.putString(MMS_CONFIG_UA_PROF_TAG_NAME, config.getString(MMS_CONFIG_UA_PROF_TAG_NAME));
        filtered.putString(MMS_CONFIG_USER_AGENT, config.getString(MMS_CONFIG_USER_AGENT));
        filtered.putString(MMS_CONFIG_UA_PROF_URL, config.getString(MMS_CONFIG_UA_PROF_URL));
        filtered.putString(MMS_CONFIG_HTTP_PARAMS, config.getString(MMS_CONFIG_HTTP_PARAMS));
        filtered.putString(MMS_CONFIG_EMAIL_GATEWAY_NUMBER, config.getString(MMS_CONFIG_EMAIL_GATEWAY_NUMBER));
        filtered.putString(MMS_CONFIG_NAI_SUFFIX, config.getString(MMS_CONFIG_NAI_SUFFIX));
        filtered.putBoolean(MMS_CONFIG_SHOW_CELL_BROADCAST_APP_LINKS, config.getBoolean(MMS_CONFIG_SHOW_CELL_BROADCAST_APP_LINKS));
        filtered.putBoolean(MMS_CONFIG_SUPPORT_HTTP_CHARSET_HEADER, config.getBoolean(MMS_CONFIG_SUPPORT_HTTP_CHARSET_HEADER));
        return filtered;
    }

    private static boolean isValidParameters(String destinationAddress, String text, PendingIntent sentIntent) {
        ArrayList sentIntents = new ArrayList();
        ArrayList parts = new ArrayList();
        sentIntents.add(sentIntent);
        parts.add(text);
        return isValidParameters(destinationAddress, parts, sentIntents);
    }

    private static boolean isValidParameters(String destinationAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents) {
        if (parts == null || parts.size() == 0) {
            return true;
        }
        if (!isValidSmsDestinationAddress(destinationAddress)) {
            for (int i = 0; i < sentIntents.size(); i++) {
                PendingIntent sentIntent = (PendingIntent) sentIntents.get(i);
                if (sentIntent != null) {
                    try {
                        sentIntent.send(1);
                    } catch (CanceledException e) {
                    }
                }
            }
            Rlog.d(TAG, "Invalid destinationAddress: " + destinationAddress);
            return false;
        } else if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (parts != null && parts.size() >= 1) {
            return true;
        } else {
            throw new IllegalArgumentException("Invalid message body");
        }
    }

    private static boolean isValidSmsDestinationAddress(String da) {
        boolean z = true;
        String encodeAddress = PhoneNumberUtils.extractNetworkPortion(da);
        if (encodeAddress == null) {
            return true;
        }
        if (encodeAddress.isEmpty()) {
            z = false;
        }
        return z;
    }

    public ArrayList<SmsMessage> getAllMessagesFromIccEfByMode(int mode) {
        Rlog.d(TAG, "getAllMessagesFromIcc, mode=" + mode);
        List records = null;
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                records = iccISms.getAllMessagesFromIccEfByModeForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), mode);
            }
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException!");
        }
        int sz = 0;
        if (records != null) {
            sz = records.size();
        }
        for (int i = 0; i < sz; i++) {
            SmsRawData record = (SmsRawData) records.get(i);
            if (record != null) {
                byte[] data = record.getBytes();
                int index = i + 1;
                if ((data[0] & 255) == 3) {
                    Rlog.d(TAG, "index[" + index + "] is STATUS_ON_ICC_READ");
                    if (updateMessageOnIcc(index, 1, data)) {
                        Rlog.d(TAG, "update index[" + index + "] to STATUS_ON_ICC_READ");
                    } else {
                        Rlog.d(TAG, "fail to update message status");
                    }
                }
            }
        }
        return createMessageListFromRawRecordsByMode(getSubscriptionId(), records, mode);
    }

    private static ArrayList<SmsMessage> createMessageListFromRawRecordsByMode(int subId, List<SmsRawData> records, int mode) {
        Rlog.d(TAG, "createMessageListFromRawRecordsByMode");
        ArrayList<SmsMessage> msg = null;
        if (records != null) {
            int count = records.size();
            msg = new ArrayList();
            for (int i = 0; i < count; i++) {
                SmsRawData data = (SmsRawData) records.get(i);
                if (data != null) {
                    SmsMessage singleSms = createFromEfRecordByMode(subId, i + 1, data.getBytes(), mode);
                    if (singleSms != null) {
                        msg.add(singleSms);
                    }
                }
            }
            Rlog.d(TAG, "actual sms count is " + msg.size());
        } else {
            Rlog.d(TAG, "fail to parse SIM sms, records is null");
        }
        return msg;
    }

    private static SmsMessage createFromEfRecordByMode(int subId, int index, byte[] data, int mode) {
        SmsMessage sms;
        if (mode == 2) {
            sms = SmsMessage.createFromEfRecord(index, data, SmsMessage.FORMAT_3GPP2);
        } else {
            sms = SmsMessage.createFromEfRecord(index, data, SmsMessage.FORMAT_3GPP);
        }
        if (sms != null) {
            sms.setSubId(subId);
        }
        return sms;
    }

    public int copyTextMessageToIccCard(String scAddress, String address, List<String> text, int status, long timestamp) {
        Rlog.d(TAG, "copyTextMessageToIccCard");
        int result = 1;
        if (this.mOnlyOwnerSimSupport == null || this.mOnlyOwnerSimSupport.isCurrentUserOwner()) {
            try {
                ISms iccISms = getISmsServiceOrThrow();
                if (iccISms != null) {
                    result = iccISms.copyTextMessageToIccCardForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), scAddress, address, text, status, timestamp);
                }
            } catch (RemoteException e) {
                Rlog.d(TAG, "RemoteException!");
            }
            return result;
        }
        Rlog.d(TAG, "Not the current owner and reject this operation");
        return 1;
    }

    public void sendDataMessage(String destinationAddress, String scAddress, short destinationPort, short originalPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        Rlog.d(TAG, "sendDataMessage, destinationAddress=" + destinationAddress);
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (!isValidParameters(destinationAddress, "send_data", sentIntent)) {
        } else {
            if (data == null || data.length == 0) {
                throw new IllegalArgumentException("Invalid message data");
            }
            Context context = ActivityThread.currentApplication().getApplicationContext();
            if (this.mDataOnlySmsFwkExt != null) {
                if (this.mDataOnlySmsFwkExt.is4GDataOnlyMode(sentIntent, getSubscriptionId(), context)) {
                    Rlog.d(TAG, "is4GDataOnlyMode");
                    return;
                }
            }
            try {
                ISms iccISms = getISmsServiceOrThrow();
                if (iccISms != null) {
                    iccISms.sendDataWithOriginalPortForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, destinationPort & 65535, originalPort & 65535, data, sentIntent, deliveryIntent);
                }
            } catch (RemoteException e) {
                Rlog.d(TAG, "RemoteException!");
            }
        }
    }

    public void sendTextMessageWithEncodingType(String destAddr, String scAddr, String text, int encodingType, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        Rlog.d(TAG, "sendTextMessageWithEncodingType, text=" + (DBG ? text : UsimPBMemInfo.STRING_NOT_SET) + ", encoding=" + encodingType);
        if (TextUtils.isEmpty(destAddr)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (isValidParameters(destAddr, text, sentIntent)) {
            Context context = ActivityThread.currentApplication().getApplicationContext();
            if (this.mDataOnlySmsFwkExt != null) {
                if (this.mDataOnlySmsFwkExt.is4GDataOnlyMode(sentIntent, getSubscriptionId(), context)) {
                    Rlog.d(TAG, "is4GDataOnlyMode");
                    return;
                }
            }
            try {
                ISms iccISms = getISmsServiceOrThrow();
                if (iccISms != null) {
                    iccISms.sendTextWithEncodingTypeForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destAddr, scAddr, text, encodingType, sentIntent, deliveryIntent, true);
                }
            } catch (RemoteException e) {
                Rlog.d(TAG, "RemoteException");
            }
        } else {
            Rlog.d(TAG, "the parameters are invalid");
        }
    }

    public void sendMultipartTextMessageWithEncodingType(String destAddr, String scAddr, ArrayList<String> parts, int encodingType, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        Rlog.d(TAG, "sendMultipartTextMessageWithEncodingType, encoding=" + encodingType);
        if (TextUtils.isEmpty(destAddr)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (isValidParameters(destAddr, (ArrayList) parts, (ArrayList) sentIntents)) {
            Context context = ActivityThread.currentApplication().getApplicationContext();
            if (this.mDataOnlySmsFwkExt != null) {
                if (this.mDataOnlySmsFwkExt.is4GDataOnlyMode(sentIntents, getSubscriptionId(), context)) {
                    Rlog.d(TAG, "is4GDataOnlyMode");
                    return;
                }
            }
            if (parts.size() > 1) {
                try {
                    ISms iccISms = getISmsServiceOrThrow();
                    if (iccISms != null) {
                        iccISms.sendMultipartTextWithEncodingTypeForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destAddr, scAddr, parts, encodingType, sentIntents, deliveryIntents, true);
                    }
                } catch (RemoteException e) {
                    Rlog.d(TAG, "RemoteException");
                }
            } else {
                PendingIntent pendingIntent = null;
                PendingIntent pendingIntent2 = null;
                if (sentIntents != null && sentIntents.size() > 0) {
                    pendingIntent = (PendingIntent) sentIntents.get(0);
                }
                Rlog.d(TAG, "get sentIntent: " + pendingIntent);
                if (deliveryIntents != null && deliveryIntents.size() > 0) {
                    pendingIntent2 = (PendingIntent) deliveryIntents.get(0);
                }
                Rlog.d(TAG, "send single message");
                if (parts != null) {
                    Rlog.d(TAG, "parts.size = " + parts.size());
                }
                String text = (parts == null || parts.size() == 0) ? UsimPBMemInfo.STRING_NOT_SET : (String) parts.get(0);
                Rlog.d(TAG, "pass encoding type " + encodingType);
                sendTextMessageWithEncodingType(destAddr, scAddr, text, encodingType, pendingIntent, pendingIntent2);
            }
        } else {
            Rlog.d(TAG, "invalid parameters for multipart message");
        }
    }

    public ArrayList<String> divideMessage(String text, int encodingType) {
        Rlog.d(TAG, "divideMessage, encoding = " + encodingType);
        ArrayList<String> ret = SmsMessage.fragmentText(text, encodingType);
        Rlog.d(TAG, "divideMessage: size = " + ret.size());
        return ret;
    }

    public SimSmsInsertStatus insertTextMessageToIccCard(String scAddress, String address, List<String> text, int status, long timestamp) {
        Rlog.d(TAG, "insertTextMessageToIccCard");
        SimSmsInsertStatus ret = null;
        if (this.mOnlyOwnerSimSupport == null || this.mOnlyOwnerSimSupport.isCurrentUserOwner()) {
            try {
                ISms iccISms = getISmsServiceOrThrow();
                if (iccISms != null) {
                    ret = iccISms.insertTextMessageToIccCardForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), scAddress, address, text, status, timestamp);
                }
            } catch (RemoteException e) {
                Rlog.d(TAG, "RemoteException");
            }
            Rlog.d(TAG, ret != null ? "insert Text " + ret.indexInIcc : "insert Text null");
            return ret;
        }
        Rlog.d(TAG, "Not the current owner and reject this operation");
        return null;
    }

    public SimSmsInsertStatus insertRawMessageToIccCard(int status, byte[] pdu, byte[] smsc) {
        Rlog.d(TAG, "insertRawMessageToIccCard");
        SimSmsInsertStatus ret = null;
        if (this.mOnlyOwnerSimSupport == null || this.mOnlyOwnerSimSupport.isCurrentUserOwner()) {
            try {
                ISms iccISms = getISmsServiceOrThrow();
                if (iccISms != null) {
                    ret = iccISms.insertRawMessageToIccCardForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), status, pdu, smsc);
                }
            } catch (RemoteException e) {
                Rlog.d(TAG, "RemoteException");
            }
            Rlog.d(TAG, ret != null ? "insert Raw " + ret.indexInIcc : "insert Raw null");
            return ret;
        }
        Rlog.d(TAG, UsimPBMemInfo.STRING_NOT_SET);
        return null;
    }

    public void sendTextMessageWithExtraParams(String destAddr, String scAddr, String text, Bundle extraParams, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        Rlog.d(TAG, "sendTextMessageWithExtraParams, text=" + (DBG ? text : UsimPBMemInfo.STRING_NOT_SET));
        if (TextUtils.isEmpty(destAddr)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (!isValidParameters(destAddr, text, sentIntent)) {
        } else {
            if (extraParams == null) {
                Rlog.d(TAG, "bundle is null");
                return;
            }
            Context context = ActivityThread.currentApplication().getApplicationContext();
            if (this.mDataOnlySmsFwkExt != null) {
                if (this.mDataOnlySmsFwkExt.is4GDataOnlyMode(sentIntent, getSubscriptionId(), context)) {
                    Rlog.d(TAG, "is4GDataOnlyMode");
                    return;
                }
            }
            try {
                ISms iccISms = getISmsServiceOrThrow();
                if (iccISms != null) {
                    iccISms.sendTextWithExtraParamsForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destAddr, scAddr, text, extraParams, sentIntent, deliveryIntent, true);
                }
            } catch (RemoteException e) {
                Rlog.d(TAG, "RemoteException");
            }
        }
    }

    public void sendMultipartTextMessageWithExtraParams(String destAddr, String scAddr, ArrayList<String> parts, Bundle extraParams, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        Rlog.d(TAG, "sendMultipartTextMessageWithExtraParams");
        if (TextUtils.isEmpty(destAddr)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (!isValidParameters(destAddr, (ArrayList) parts, (ArrayList) sentIntents)) {
        } else {
            if (extraParams == null) {
                Rlog.d(TAG, "bundle is null");
                return;
            }
            Context context = ActivityThread.currentApplication().getApplicationContext();
            if (this.mDataOnlySmsFwkExt != null) {
                if (this.mDataOnlySmsFwkExt.is4GDataOnlyMode(sentIntents, getSubscriptionId(), context)) {
                    Rlog.d(TAG, "is4GDataOnlyMode");
                    return;
                }
            }
            if (parts.size() > 1) {
                try {
                    ISms iccISms = getISmsServiceOrThrow();
                    if (iccISms != null) {
                        iccISms.sendMultipartTextWithExtraParamsForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destAddr, scAddr, parts, extraParams, sentIntents, deliveryIntents, true);
                    }
                } catch (RemoteException e) {
                    Rlog.d(TAG, "RemoteException");
                }
            } else {
                PendingIntent pendingIntent = null;
                PendingIntent pendingIntent2 = null;
                if (sentIntents != null && sentIntents.size() > 0) {
                    pendingIntent = (PendingIntent) sentIntents.get(0);
                }
                if (deliveryIntents != null && deliveryIntents.size() > 0) {
                    pendingIntent2 = (PendingIntent) deliveryIntents.get(0);
                }
                String text = (parts == null || parts.size() == 0) ? UsimPBMemInfo.STRING_NOT_SET : (String) parts.get(0);
                sendTextMessageWithExtraParams(destAddr, scAddr, text, extraParams, pendingIntent, pendingIntent2);
            }
        }
    }

    public SmsParameters getSmsParameters() {
        Rlog.d(TAG, "getSmsParameters");
        if (this.mOnlyOwnerSimSupport == null || this.mOnlyOwnerSimSupport.isCurrentUserOwner()) {
            try {
                ISms iccISms = getISmsServiceOrThrow();
                if (iccISms != null) {
                    return iccISms.getSmsParametersForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName());
                }
                return null;
            } catch (RemoteException e) {
                Rlog.d(TAG, "RemoteException");
                Rlog.d(TAG, "fail to get SmsParameters");
                return null;
            }
        }
        Rlog.d(TAG, "Not the current owner and reject this operation");
        return null;
    }

    public boolean setSmsParameters(SmsParameters params) {
        Rlog.d(TAG, "setSmsParameters");
        if (this.mOnlyOwnerSimSupport == null || this.mOnlyOwnerSimSupport.isCurrentUserOwner()) {
            try {
                ISms iccISms = getISmsServiceOrThrow();
                if (iccISms != null) {
                    return iccISms.setSmsParametersForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), params);
                }
                return false;
            } catch (RemoteException e) {
                Rlog.d(TAG, "RemoteException");
                return false;
            }
        }
        Rlog.d(TAG, "Not the current owner and reject this operation");
        return false;
    }

    public int copySmsToIcc(byte[] smsc, byte[] pdu, int status) {
        Rlog.d(TAG, "copySmsToIcc");
        SimSmsInsertStatus smsStatus = insertRawMessageToIccCard(status, pdu, smsc);
        if (smsStatus == null) {
            return -1;
        }
        int[] index = smsStatus.getIndex();
        if (index == null || index.length <= 0) {
            return -1;
        }
        return index[0];
    }

    public boolean updateSmsOnSimReadStatus(int index, boolean read) {
        int newStatus = 1;
        Rlog.d(TAG, "updateSmsOnSimReadStatus");
        SmsRawData record = null;
        try {
            ISms iccISms = getISmsServiceOrThrow();
            if (iccISms != null) {
                record = iccISms.getMessageFromIccEfForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), index);
            }
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
        }
        if (record != null) {
            byte[] rawData = record.getBytes();
            int status = rawData[0] & 255;
            Rlog.d(TAG, "sms status is " + status);
            if (status != 3 && status != 1) {
                Rlog.d(TAG, "non-delivery sms " + status);
                return false;
            } else if ((status != 3 || read) && !(status == 1 && read)) {
                Rlog.d(TAG, "update sms status as " + read);
                if (!read) {
                    newStatus = 3;
                }
                return updateMessageOnIcc(index, newStatus, rawData);
            } else {
                Rlog.d(TAG, "no need to update status");
                return true;
            }
        }
        Rlog.d(TAG, "record is null");
        return false;
    }

    public boolean setEtwsConfig(int mode) {
        Rlog.d(TAG, "setEtwsConfig, mode=" + mode);
        try {
            ISms iccISms = getISmsServiceOrThrow();
            if (iccISms != null) {
                return iccISms.setEtwsConfigForSubscriber(getSubscriptionId(), mode);
            }
            return false;
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
            return false;
        }
    }

    public void setSmsMemoryStatus(boolean status) {
        Rlog.d(TAG, "setSmsMemoryStatus");
        try {
            ISms iccISms = getISmsServiceOrThrow();
            if (iccISms != null) {
                iccISms.setSmsMemoryStatusForSubscriber(getSubscriptionId(), status);
            }
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
        }
    }

    public IccSmsStorageStatus getSmsSimMemoryStatus() {
        Rlog.d(TAG, "getSmsSimMemoryStatus");
        try {
            ISms iccISms = getISmsServiceOrThrow();
            if (iccISms != null) {
                return iccISms.getSmsSimMemoryStatusForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName());
            }
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
        }
        return null;
    }

    private SmsBroadcastConfigInfo Convert2SmsBroadcastConfigInfo(SmsCbConfigInfo info) {
        return new SmsBroadcastConfigInfo(info.mFromServiceId, info.mToServiceId, info.mFromCodeScheme, info.mToCodeScheme, info.mSelected);
    }

    private SmsCbConfigInfo Convert2SmsCbConfigInfo(SmsBroadcastConfigInfo info) {
        return new SmsCbConfigInfo(info.getFromServiceId(), info.getToServiceId(), info.getFromCodeScheme(), info.getToCodeScheme(), info.isSelected());
    }

    public SmsBroadcastConfigInfo[] getCellBroadcastSmsConfig() {
        Rlog.d(TAG, "getCellBroadcastSmsConfig");
        Rlog.d(TAG, "subId=" + getSubscriptionId());
        SmsCbConfigInfo[] configs = null;
        try {
            ISms iccISms = getISmsServiceOrThrow();
            if (iccISms != null) {
                configs = iccISms.getCellBroadcastSmsConfigForSubscriber(getSubscriptionId());
            } else {
                Rlog.d(TAG, "fail to get sms service");
            }
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
        }
        if (configs != null) {
            Rlog.d(TAG, "config length = " + configs.length);
            if (configs.length != 0) {
                SmsBroadcastConfigInfo[] result = new SmsBroadcastConfigInfo[configs.length];
                for (int i = 0; i < configs.length; i++) {
                    result[i] = Convert2SmsBroadcastConfigInfo(configs[i]);
                }
                return result;
            }
        }
        return null;
    }

    public boolean setCellBroadcastSmsConfig(SmsBroadcastConfigInfo[] channels, SmsBroadcastConfigInfo[] languages) {
        Rlog.d(TAG, "setCellBroadcastSmsConfig");
        Rlog.d(TAG, "subId=" + getSubscriptionId());
        if (channels != null) {
            Rlog.d(TAG, "channel size=" + channels.length);
        } else {
            Rlog.d(TAG, "channel size=0");
        }
        if (languages != null) {
            Rlog.d(TAG, "language size=" + languages.length);
        } else {
            Rlog.d(TAG, "language size=0");
        }
        if (getISmsServiceOrThrow() != null) {
            int enableResult = 1;
            boolean disableResult = true;
            if (!(channels == null || channels.length == 0)) {
                SmsCbConfigInfo[] channelInfos = new SmsCbConfigInfo[channels.length];
                for (int i = 0; i < channels.length; i++) {
                    channelInfos[i] = Convert2SmsCbConfigInfo(channels[i]);
                    if (channelInfos[i].mSelected) {
                        enableResult &= enableCellBroadcastRange(channelInfos[i].mFromServiceId, channelInfos[i].mToServiceId, 0);
                        if (VDBG) {
                            Rlog.d(TAG, "setCellBroadcastSmsConfig enableResult[" + i + "] =" + enableResult);
                        }
                    } else {
                        disableResult &= disableCellBroadcastRange(channelInfos[i].mFromServiceId, channelInfos[i].mToServiceId, 0);
                        if (VDBG) {
                            Rlog.d(TAG, "setCellBroadcastSmsConfig disableResult[" + i + "] =" + disableResult);
                        }
                    }
                }
            }
            boolean result = enableResult != 0 ? disableResult : false;
            Rlog.d(TAG, "setCellBroadcastSmsConfig result =" + result);
            return result;
        }
        Rlog.d(TAG, "fail to get sms service");
        return false;
    }

    public boolean queryCellBroadcastSmsActivation() {
        Rlog.d(TAG, "queryCellBroadcastSmsActivation");
        Rlog.d(TAG, "subId=" + getSubscriptionId());
        try {
            ISms iccISms = getISmsServiceOrThrow();
            if (iccISms != null) {
                return iccISms.queryCellBroadcastSmsActivationForSubscriber(getSubscriptionId());
            }
            Rlog.d(TAG, "fail to get sms service");
            return false;
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException!");
            return false;
        }
    }

    public boolean activateCellBroadcastSms(boolean activate) {
        Rlog.d(TAG, "activateCellBroadcastSms activate : " + activate + ", sub = " + getSubscriptionId());
        try {
            ISms iccISms = getISmsServiceOrThrow();
            if (iccISms != null) {
                return iccISms.activateCellBroadcastSmsForSubscriber(getSubscriptionId(), activate);
            }
            Rlog.d(TAG, "fail to get sms service");
            return false;
        } catch (RemoteException e) {
            Rlog.d(TAG, "fail to activate CB");
            return false;
        }
    }

    public boolean removeCellBroadcastMsg(int channelId, int serialId) {
        Rlog.d(TAG, "RemoveCellBroadcastMsg, subId=" + getSubscriptionId());
        try {
            ISms iccISms = getISmsServiceOrThrow();
            if (iccISms != null) {
                return iccISms.removeCellBroadcastMsgForSubscriber(getSubscriptionId(), channelId, serialId);
            }
            Rlog.d(TAG, "fail to get sms service");
            return false;
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoveCellBroadcastMsg, RemoteException!");
            return false;
        }
    }

    public String getCellBroadcastRanges() {
        Rlog.d(TAG, "getCellBroadcastRanges, subId=" + getSubscriptionId());
        String configs = UsimPBMemInfo.STRING_NOT_SET;
        try {
            ISms iccISms = getISmsServiceOrThrow();
            if (iccISms != null) {
                return iccISms.getCellBroadcastRangesForSubscriber(getSubscriptionId());
            }
            Rlog.d(TAG, "fail to get sms service");
            return configs;
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
            return configs;
        }
    }

    public boolean setCellBroadcastLang(String lang) {
        Rlog.d(TAG, "setCellBroadcastLang, subId=" + getSubscriptionId());
        try {
            ISms iccISms = getISmsServiceOrThrow();
            if (iccISms != null) {
                return iccISms.setCellBroadcastLangsForSubscriber(getSubscriptionId(), lang);
            }
            Rlog.d(TAG, "fail to get sms service");
            return false;
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
            return false;
        }
    }

    public String getCellBroadcastLang() {
        Rlog.d(TAG, "getCellBroadcastLang, subId=" + getSubscriptionId());
        String langs = UsimPBMemInfo.STRING_NOT_SET;
        try {
            ISms iccISms = getISmsServiceOrThrow();
            if (iccISms != null) {
                return iccISms.getCellBroadcastLangsForSubscriber(getSubscriptionId());
            }
            Rlog.d(TAG, "fail to get sms service");
            return langs;
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
            return langs;
        }
    }

    public int colorCopyTextMessageToIccCard(String scAddress, String address, List<String> text, int status, long timestamp) {
        return copyTextMessageToIccCard(scAddress, address, text, status, timestamp);
    }

    public void colorSendMultipartTextMessageWithExtraParams(String destAddr, String scAddr, ArrayList<String> parts, Bundle extraParams, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        sendMultipartTextMessageWithExtraParams(destAddr, scAddr, parts, extraParams, sentIntents, deliveryIntents);
    }

    public static SmsManager getSmsManagerForSubscriber(int subId) {
        return getSmsManagerForSubscriptionId(subId);
    }

    public static SmsManager oppogetSmsManagerForSubscriber(int subId) {
        return getSmsManagerForSubscriptionId(subId);
    }

    private boolean isNeedDisplayPickSimCardDialog(Context context, List<SubscriptionInfo> operatorSubInfoList) {
        boolean isNeedDisplayDialog = false;
        try {
            Log.d(TAG, "isNeedDisplayPickSimCardDialog: mSubId = " + this.mSubId + ", pkgName = " + context.getPackageName());
            if (isWhiteListPackageName(context)) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                List<SubscriptionInfo> subInfoList = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
                if (subInfoList != null) {
                    int subInfoLength = subInfoList.size();
                    int canSendMessageCardCount = 0;
                    int softSimSlotId = ColorOSTelephonyManager.getDefault(context).colorGetSoftSimCardSlotId();
                    for (int i = 0; i < subInfoLength; i++) {
                        SubscriptionInfo sir = (SubscriptionInfo) subInfoList.get(i);
                        if (softSimSlotId != sir.getSimSlotIndex() && sir.mStatus == 1) {
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
        Log.d(TAG, "isNeedDisplayPickSimCardDialog: isNeedDisplayDialog = " + isNeedDisplayDialog);
        return isNeedDisplayDialog;
    }

    private boolean isWhiteListPackageName(Context context) {
        boolean isWhiteListPackageName = false;
        String allPkg = Global.getString(context.getContentResolver(), ROMUPDATE_SEND_MESSAGE_PKG);
        Log.d(TAG, "isWhiteListPackageName: allPkg = " + allPkg);
        if (TextUtils.isEmpty(allPkg)) {
            allPkg = DEFAULT_PACKAGE;
        }
        String[] splitAllPkg = allPkg.split(";");
        if (splitAllPkg != null && splitAllPkg.length > 0) {
            for (String trim : splitAllPkg) {
                if (trim.trim().equals(context.getPackageName())) {
                    isWhiteListPackageName = true;
                    break;
                }
            }
        }
        Log.d(TAG, "isWhiteListPackageName: isWhiteListPackageName = " + isWhiteListPackageName);
        return isWhiteListPackageName;
    }

    private String getAppLabel(Context context) {
        String pkg = UsimPBMemInfo.STRING_NOT_SET;
        try {
            pkg = context.getPackageName();
            Log.d(TAG, "isWhiteListPackageName: pkg = " + pkg);
            String rt = context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(pkg, 0)).toString();
            if (TextUtils.isEmpty(rt)) {
                return pkg;
            }
            return rt;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPickSimCardActivity(Context context, Handler handler, List<SubscriptionInfo> operatorSubInfoList) {
        Intent intent = new Intent();
        intent.setClassName("com.coloros.simsettings", "com.coloros.simsettings.SendSmsPickSimCardAlertDialog");
        intent.addFlags(268435456);
        intent.putExtra(APK_LABEL_NAME, getAppLabel(context));
        intent.putExtra(APK_PACKAGE_NAME, context.getPackageName());
        intent.putExtra(SIM_NAME_ONE, ((SubscriptionInfo) operatorSubInfoList.get(0)).getDisplayName());
        intent.putExtra(SIM_NAME_TWO, ((SubscriptionInfo) operatorSubInfoList.get(1)).getDisplayName());
        intent.putExtra(SUBSCRIPTION_ID_ONE, ((SubscriptionInfo) operatorSubInfoList.get(0)).getSubscriptionId());
        intent.putExtra(SUBSCRIPTION_ID_TWO, ((SubscriptionInfo) operatorSubInfoList.get(1)).getSubscriptionId());
        intent.putExtra(MESSENGER, new Messenger(handler));
        context.startActivity(intent);
    }
}
