package com.android.internal.telephony;

import android.app.BroadcastOptions;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.IWapPushManager.Stub;
import com.android.internal.telephony.uicc.IccUtils;
import com.google.android.mms.pdu.DeliveryInd;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduParser;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.ReadOrigInd;
import java.util.HashMap;

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
public class WapPushOverSms implements ServiceConnection {
    private static final boolean DBG = true;
    private static final String LOCATION_SELECTION = "m_type=? AND ct_l =?";
    private static final String TAG = "WAP PUSH";
    private static final String THREAD_ID_SELECTION = "m_id=? AND m_type=?";
    private Bundle bundle;
    private final BroadcastReceiver mBroadcastReceiver;
    private final Context mContext;
    private IDeviceIdleController mDeviceIdleController;
    private volatile IWapPushManager mWapPushManager;
    private String mWapPushManagerPackage;

    /* renamed from: com.android.internal.telephony.WapPushOverSms$1 */
    class AnonymousClass1 extends BroadcastReceiver {
        final /* synthetic */ WapPushOverSms this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.WapPushOverSms.1.<init>(com.android.internal.telephony.WapPushOverSms):void, dex: 
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
        AnonymousClass1(com.android.internal.telephony.WapPushOverSms r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.WapPushOverSms.1.<init>(com.android.internal.telephony.WapPushOverSms):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.WapPushOverSms.1.<init>(com.android.internal.telephony.WapPushOverSms):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.WapPushOverSms.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.WapPushOverSms.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.WapPushOverSms.1.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    private class BindServiceThread extends Thread {
        private final Context context;
        final /* synthetic */ WapPushOverSms this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.WapPushOverSms.BindServiceThread.<init>(com.android.internal.telephony.WapPushOverSms, android.content.Context):void, dex: 
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
        private BindServiceThread(com.android.internal.telephony.WapPushOverSms r1, android.content.Context r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.WapPushOverSms.BindServiceThread.<init>(com.android.internal.telephony.WapPushOverSms, android.content.Context):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.WapPushOverSms.BindServiceThread.<init>(com.android.internal.telephony.WapPushOverSms, android.content.Context):void");
        }

        /* synthetic */ BindServiceThread(WapPushOverSms this$0, Context context, BindServiceThread bindServiceThread) {
            this(this$0, context);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.WapPushOverSms.BindServiceThread.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.WapPushOverSms.BindServiceThread.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.WapPushOverSms.BindServiceThread.run():void");
        }
    }

    private final class DecodedResult {
        String contentType;
        HashMap<String, String> contentTypeParameters;
        byte[] header;
        HashMap<String, String> headerList;
        byte[] intentData;
        String mimeType;
        GenericPdu parsedPdu;
        int pduType;
        int phoneId;
        int statusCode;
        int subId;
        final /* synthetic */ WapPushOverSms this$0;
        int transactionId;
        String wapAppId;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.WapPushOverSms.DecodedResult.<init>(com.android.internal.telephony.WapPushOverSms):void, dex: 
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
        private DecodedResult(com.android.internal.telephony.WapPushOverSms r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.WapPushOverSms.DecodedResult.<init>(com.android.internal.telephony.WapPushOverSms):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.WapPushOverSms.DecodedResult.<init>(com.android.internal.telephony.WapPushOverSms):void");
        }

        /* synthetic */ DecodedResult(WapPushOverSms this$0, DecodedResult decodedResult) {
            this(this$0);
        }
    }

    private void bindWapPushManagerService(Context context) {
        Intent intent = new Intent(IWapPushManager.class.getName());
        ComponentName comp = intent.resolveSystemService(context.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp == null || !context.bindService(intent, this, 1)) {
            Rlog.e(TAG, "bindService() for wappush manager failed");
            return;
        }
        synchronized (this) {
            this.mWapPushManagerPackage = comp.getPackageName();
        }
        Rlog.v(TAG, "bindService() for wappush manager succeeded");
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        this.mWapPushManager = Stub.asInterface(service);
        Rlog.v(TAG, "wappush manager connected to " + hashCode());
    }

    public void onServiceDisconnected(ComponentName name) {
        this.mWapPushManager = null;
        Rlog.v(TAG, "wappush manager disconnected.");
    }

    public WapPushOverSms(Context context) {
        this.mBroadcastReceiver = new AnonymousClass1(this);
        this.mContext = context;
        this.mDeviceIdleController = TelephonyComponentFactory.getInstance().getIDeviceIdleController();
        if (((UserManager) this.mContext.getSystemService("user")).isUserUnlocked()) {
            bindWapPushManagerService(this.mContext);
            return;
        }
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_UNLOCKED");
        context.registerReceiver(this.mBroadcastReceiver, userFilter);
    }

    public void dispose() {
        if (this.mWapPushManager != null) {
            Rlog.v(TAG, "dispose: unbind wappush manager");
            this.mContext.unbindService(this);
            return;
        }
        Rlog.e(TAG, "dispose: not bound to a wappush manager");
    }

    /* JADX WARNING: Removed duplicated region for block: B:55:0x0223 A:{ExcHandler: java.lang.ArrayIndexOutOfBoundsException (e java.lang.ArrayIndexOutOfBoundsException), Splitter: B:58:0x0249} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0223 A:{ExcHandler: java.lang.ArrayIndexOutOfBoundsException (e java.lang.ArrayIndexOutOfBoundsException), Splitter: B:58:0x0249} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:55:0x0223, code:
            r4 = e;
     */
    /* JADX WARNING: Missing block: B:100:0x0419, code:
            r10 = move-exception;
     */
    /* JADX WARNING: Missing block: B:101:0x041a, code:
            r10.printStackTrace();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private DecodedResult decodeWapPdu(byte[] pdu, InboundSmsHandler handler) {
        Exception e;
        DecodedResult decodedResult = new DecodedResult(this, null);
        Rlog.d(TAG, "Rx: " + IccUtils.bytesToHexString(pdu));
        try {
            int transactionId = pdu[0] & 255;
            int index = 1 + 1;
            int index2;
            try {
                int pduType = pdu[1] & 255;
                int phoneId = handler.getPhone().getPhoneId();
                if (pduType == 6 || pduType == 7) {
                    index2 = index;
                } else {
                    index2 = this.mContext.getResources().getInteger(17694866);
                    if (index2 != -1) {
                        index = index2 + 1;
                        transactionId = pdu[index2] & 255;
                        index2 = index + 1;
                        pduType = pdu[index] & 255;
                        Rlog.d(TAG, "index = " + index2 + " PDU Type = " + pduType + " transactionID = " + transactionId);
                        if (!(pduType == 6 || pduType == 7)) {
                            Rlog.w(TAG, "Received non-PUSH WAP PDU. Type = " + pduType);
                            decodedResult.statusCode = 1;
                            return decodedResult;
                        }
                    }
                    Rlog.w(TAG, "Received non-PUSH WAP PDU. Type = " + pduType);
                    decodedResult.statusCode = 1;
                    return decodedResult;
                }
                WspTypeDecoder pduDecoder = TelephonyComponentFactory.getInstance().makeWspTypeDecoder(pdu);
                if (pduDecoder.decodeUintvarInteger(index2)) {
                    int headerLength = (int) pduDecoder.getValue32();
                    index2 += pduDecoder.getDecodedDataLength();
                    int headerStartIndex = index2;
                    if (pduDecoder.decodeContentType(index2)) {
                        byte[] intentData;
                        int subId;
                        String mimeType = pduDecoder.getValueString();
                        long binaryContentType = pduDecoder.getValue32();
                        index2 += pduDecoder.getDecodedDataLength();
                        byte[] header = new byte[headerLength];
                        System.arraycopy(pdu, headerStartIndex, header, 0, header.length);
                        pduDecoder.decodeHeaders(index2, (headerLength - index2) + headerStartIndex);
                        if (mimeType == null || !mimeType.equals(WspTypeDecoder.CONTENT_TYPE_B_PUSH_CO)) {
                            int dataIndex = headerStartIndex + headerLength;
                            intentData = new byte[(pdu.length - dataIndex)];
                            System.arraycopy(pdu, dataIndex, intentData, 0, intentData.length);
                        } else {
                            intentData = pdu;
                        }
                        int[] subIds = SubscriptionManager.getSubId(phoneId);
                        if (subIds == null || subIds.length <= 0) {
                            try {
                                subId = SmsManager.getDefaultSmsSubscriptionId();
                            } catch (Exception e2) {
                                Rlog.e(TAG, "Unable to parse PDU: " + e2.toString());
                            } catch (ArrayIndexOutOfBoundsException e3) {
                            }
                        } else {
                            subId = subIds[0];
                        }
                        GenericPdu parsedPdu = null;
                        parsedPdu = new PduParser(intentData, shouldParseContentDisposition(subId)).parse();
                        if (parsedPdu != null) {
                            if (parsedPdu.getMessageType() == 130) {
                                NotificationInd nInd = (NotificationInd) parsedPdu;
                                if (nInd.getFrom() == null || !BlockChecker.isBlocked(this.mContext, nInd.getFrom().getString())) {
                                    boolean is3xVersion = InboundSmsHandler.isColorOsVersion3X();
                                    if (!is3xVersion || nInd == null || nInd.getFrom() == null) {
                                        Rlog.e(TAG, "error to get wap number, is3xVersion=" + is3xVersion);
                                    } else {
                                        String senderNumber = nInd.getFrom().getString();
                                        Rlog.d("sms", "color os 3.0 -- mms send number=" + senderNumber);
                                        String tmpAddress = senderNumber;
                                        if (senderNumber != null && senderNumber.length() == 13 && senderNumber.charAt(0) != '+' && senderNumber.startsWith("861")) {
                                            senderNumber = "+" + senderNumber;
                                        }
                                        boolean isNumberBlocked = !TextUtils.isEmpty(senderNumber) ? InboundSmsHandler.isInBlackLists(this.mContext, senderNumber) : false;
                                        Rlog.d("sms", "mms isNumberBlocked=" + isNumberBlocked + " senderNumber=" + senderNumber);
                                        if (isNumberBlocked) {
                                            decodedResult.statusCode = 1;
                                            return decodedResult;
                                        }
                                    }
                                }
                                decodedResult.statusCode = 1;
                                return decodedResult;
                            }
                        }
                        if (pduDecoder.seekXWapApplicationId(index2, (index2 + headerLength) - 1)) {
                            String contentType;
                            pduDecoder.decodeXWapApplicationId((int) pduDecoder.getValue32());
                            String wapAppId = pduDecoder.getValueString();
                            if (wapAppId == null) {
                                wapAppId = Integer.toString((int) pduDecoder.getValue32());
                            }
                            decodedResult.wapAppId = wapAppId;
                            if (mimeType == null) {
                                contentType = Long.toString(binaryContentType);
                            } else {
                                contentType = mimeType;
                            }
                            decodedResult.contentType = contentType;
                            Rlog.v(TAG, "appid found: " + wapAppId + ":" + contentType);
                        }
                        decodedResult.subId = subId;
                        decodedResult.phoneId = phoneId;
                        decodedResult.parsedPdu = parsedPdu;
                        decodedResult.mimeType = mimeType;
                        decodedResult.transactionId = transactionId;
                        decodedResult.pduType = pduType;
                        decodedResult.header = header;
                        decodedResult.intentData = intentData;
                        decodedResult.contentTypeParameters = pduDecoder.getContentParameters();
                        decodedResult.statusCode = -1;
                        decodedResult.headerList = pduDecoder.getHeaders();
                        return decodedResult;
                    }
                    Rlog.w(TAG, "Received PDU. Header Content-Type error.");
                    decodedResult.statusCode = 2;
                    return decodedResult;
                }
                Rlog.w(TAG, "Received PDU. Header Length error.");
                decodedResult.statusCode = 2;
                return decodedResult;
            } catch (ArrayIndexOutOfBoundsException e4) {
                ArrayIndexOutOfBoundsException aie = e4;
                index2 = index;
                Rlog.e(TAG, "ignoring dispatchWapPdu() array index exception: " + aie);
                decodedResult.statusCode = 2;
                return decodedResult;
            } catch (Exception e5) {
                e2 = e5;
                index2 = index;
                e2.printStackTrace();
                decodedResult.statusCode = 2;
                return decodedResult;
            }
        } catch (ArrayIndexOutOfBoundsException e32) {
        } catch (Exception e6) {
            e2 = e6;
            e2.printStackTrace();
            decodedResult.statusCode = 2;
            return decodedResult;
        }
    }

    public int dispatchWapPdu(byte[] pdu, BroadcastReceiver receiver, InboundSmsHandler handler) {
        try {
            DecodedResult result = decodeWapPdu(pdu, handler);
            if (result.statusCode != -1) {
                return result.statusCode;
            }
            Intent intent;
            if (SmsManager.getDefault().getAutoPersisting()) {
                writeInboxMessage(result.subId, result.parsedPdu);
            }
            if (result.wapAppId != null) {
                boolean processFurther = true;
                try {
                    IWapPushManager wapPushMan = this.mWapPushManager;
                    if (wapPushMan == null) {
                        Rlog.w(TAG, "wap push manager not found!");
                    } else {
                        synchronized (this) {
                            this.mDeviceIdleController.addPowerSaveTempWhitelistAppForMms(this.mWapPushManagerPackage, 0, "mms-mgr");
                        }
                        intent = new Intent();
                        intent.putExtra("transactionId", result.transactionId);
                        intent.putExtra("pduType", result.pduType);
                        intent.putExtra("header", result.header);
                        intent.putExtra("data", result.intentData);
                        intent.putExtra("contentTypeParameters", result.contentTypeParameters);
                        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, result.phoneId);
                        intent.putExtra("wspHeaders", result.headerList);
                        if (this.bundle != null) {
                            Rlog.d(TAG, "put addr info into intent 1");
                            intent.putExtra("address", this.bundle.getString("address"));
                            intent.putExtra("service_center", this.bundle.getString("service_center"));
                        }
                        int procRet = wapPushMan.processMessage(result.wapAppId, result.contentType, intent);
                        Rlog.v(TAG, "procRet:" + procRet);
                        if ((procRet & 1) > 0 && (WapPushManagerParams.FURTHER_PROCESSING & procRet) == 0) {
                            processFurther = false;
                        }
                    }
                    if (!processFurther) {
                        return 1;
                    }
                } catch (RemoteException e) {
                    Rlog.w(TAG, "remote func failed...");
                }
            }
            Rlog.v(TAG, "fall back to existing handler");
            if (result.mimeType == null) {
                Rlog.w(TAG, "Header Content-Type error.");
                return 2;
            }
            intent = new Intent("android.provider.Telephony.WAP_PUSH_DELIVER");
            intent.setType(result.mimeType);
            intent.putExtra("transactionId", result.transactionId);
            intent.putExtra("pduType", result.pduType);
            intent.putExtra("header", result.header);
            intent.putExtra("data", result.intentData);
            intent.putExtra("contentTypeParameters", result.contentTypeParameters);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, result.phoneId);
            intent.putExtra("wspHeaders", result.headerList);
            if (this.bundle != null) {
                Rlog.d(TAG, "put addr info into intent 2");
                intent.putExtra("address", this.bundle.getString("address"));
                intent.putExtra("service_center", this.bundle.getString("service_center"));
            }
            try {
                InboundSmsHandler.oemSetDefaultWappush(this.mContext);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
            ComponentName componentName = SmsApplication.getDefaultMmsApplication(this.mContext, true);
            Bundle options = null;
            if (componentName != null) {
                intent.setComponent(componentName);
                Rlog.v(TAG, "Delivering MMS to: " + componentName.getPackageName() + " " + componentName.getClassName());
                try {
                    long duration = this.mDeviceIdleController.addPowerSaveTempWhitelistAppForMms(componentName.getPackageName(), 0, "mms-app");
                    BroadcastOptions bopts = BroadcastOptions.makeBasic();
                    bopts.setTemporaryAppWhitelistDuration(duration);
                    options = bopts.toBundle();
                } catch (RemoteException e2) {
                }
            }
            handler.dispatchIntent(intent, getPermissionForType(result.mimeType), getAppOpsPermissionForIntent(result.mimeType), options, receiver, UserHandle.SYSTEM);
            return -1;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 2;
        }
    }

    public boolean isWapPushForMms(byte[] pdu, InboundSmsHandler handler) {
        DecodedResult result = decodeWapPdu(pdu, handler);
        if (result.statusCode == -1) {
            return "application/vnd.wap.mms-message".equals(result.mimeType);
        }
        return false;
    }

    private static boolean shouldParseContentDisposition(int subId) {
        return SmsManager.getSmsManagerForSubscriptionId(subId).getCarrierConfigValues().getBoolean(SmsManager.MMS_CONFIG_SUPPORT_MMS_CONTENT_DISPOSITION, true);
    }

    private void writeInboxMessage(int subId, GenericPdu pdu) {
        if (pdu == null) {
            try {
                Rlog.e(TAG, "Invalid PUSH PDU");
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        }
        PduPersister persister = PduPersister.getPduPersister(this.mContext);
        int type = pdu.getMessageType();
        switch (type) {
            case 130:
                NotificationInd nInd = (NotificationInd) pdu;
                Bundle configs = SmsManager.getSmsManagerForSubscriptionId(subId).getCarrierConfigValues();
                if (configs != null && configs.getBoolean(SmsManager.MMS_CONFIG_APPEND_TRANSACTION_ID, false)) {
                    byte[] contentLocation = nInd.getContentLocation();
                    if ((byte) 61 == contentLocation[contentLocation.length - 1]) {
                        byte[] transactionId = nInd.getTransactionId();
                        byte[] contentLocationWithId = new byte[(contentLocation.length + transactionId.length)];
                        System.arraycopy(contentLocation, 0, contentLocationWithId, 0, contentLocation.length);
                        System.arraycopy(transactionId, 0, contentLocationWithId, contentLocation.length, transactionId.length);
                        nInd.setContentLocation(contentLocationWithId);
                    }
                }
                if (isDuplicateNotification(this.mContext, nInd)) {
                    Rlog.d(TAG, "Skip storing duplicate MMS WAP push notification ind: " + new String(nInd.getContentLocation()));
                    return;
                }
                if (persister.persist(pdu, Inbox.CONTENT_URI, true, true, null) == null) {
                    Rlog.e(TAG, "Failed to save MMS WAP push notification ind");
                    return;
                }
                return;
            case 134:
            case 136:
                long threadId = getDeliveryOrReadReportThreadId(this.mContext, pdu);
                if (threadId == -1) {
                    Rlog.e(TAG, "Failed to find delivery or read report's thread id");
                    return;
                }
                Uri uri = persister.persist(pdu, Inbox.CONTENT_URI, true, true, null);
                if (uri == null) {
                    Rlog.e(TAG, "Failed to persist delivery or read report");
                    return;
                }
                ContentValues values = new ContentValues(1);
                values.put("thread_id", Long.valueOf(threadId));
                if (SqliteWrapper.update(this.mContext, this.mContext.getContentResolver(), uri, values, null, null) != 1) {
                    Rlog.e(TAG, "Failed to update delivery or read report thread id");
                    return;
                }
                return;
            default:
                try {
                    Log.e(TAG, "Received unrecognized WAP Push PDU.");
                    return;
                } catch (Throwable e) {
                    Log.e(TAG, "Failed to save MMS WAP push data: type=" + type, e);
                    return;
                } catch (Throwable e2) {
                    Log.e(TAG, "Unexpected RuntimeException in persisting MMS WAP push data", e2);
                    return;
                }
        }
    }

    private static long getDeliveryOrReadReportThreadId(Context context, GenericPdu pdu) {
        String messageId;
        if (pdu instanceof DeliveryInd) {
            messageId = new String(((DeliveryInd) pdu).getMessageId());
        } else if (pdu instanceof ReadOrigInd) {
            messageId = new String(((ReadOrigInd) pdu).getMessageId());
        } else {
            Rlog.e(TAG, "WAP Push data is neither delivery or read report type: " + pdu.getClass().getCanonicalName());
            return -1;
        }
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(context, context.getContentResolver(), Mms.CONTENT_URI, new String[]{"thread_id"}, THREAD_ID_SELECTION, new String[]{DatabaseUtils.sqlEscapeString(messageId), Integer.toString(128)}, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return -1;
            }
            long j = cursor.getLong(0);
            if (cursor != null) {
                cursor.close();
            }
            return j;
        } catch (SQLiteException e) {
            Rlog.e(TAG, "Failed to query delivery or read report thread id", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static boolean isDuplicateNotification(Context context, NotificationInd nInd) {
        if (nInd.getContentLocation() != null) {
            String[] selectionArgs = new String[]{new String(nInd.getContentLocation())};
            Cursor cursor = null;
            try {
                cursor = SqliteWrapper.query(context, context.getContentResolver(), Mms.CONTENT_URI, new String[]{"_id"}, LOCATION_SELECTION, new String[]{Integer.toString(130), new String(rawLocation)}, null);
                if (cursor != null && cursor.getCount() > 0) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return true;
                } else if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                Rlog.e(TAG, "failed to query existing notification ind", e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return false;
    }

    public static String getPermissionForType(String mimeType) {
        if ("application/vnd.wap.mms-message".equals(mimeType)) {
            return "android.permission.RECEIVE_MMS";
        }
        return "android.permission.RECEIVE_WAP_PUSH";
    }

    public static int getAppOpsPermissionForIntent(String mimeType) {
        if ("application/vnd.wap.mms-message".equals(mimeType)) {
            return 18;
        }
        return 19;
    }

    public int dispatchWapPdu(byte[] pdu, BroadcastReceiver receiver, InboundSmsHandler handler, Bundle extra) {
        if (extra != null) {
            Rlog.i(TAG, "dispathchWapPdu!" + extra.getString("address") + " " + extra.getString("service_center"));
        }
        this.bundle = extra;
        return dispatchWapPdu(pdu, receiver, handler);
    }
}
