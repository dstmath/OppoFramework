package com.android.internal.telephony.gsm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.CellLocation;
import android.telephony.SmsCbLocation;
import android.telephony.SmsCbMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import com.android.internal.telephony.CellBroadcastHandler;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.ServiceStateTracker;
import com.mediatek.internal.telephony.CellBroadcastFwkExt;
import com.mediatek.internal.telephony.EtwsNotification;
import java.util.HashMap;
import java.util.Iterator;

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
public class GsmCellBroadcastHandler extends CellBroadcastHandler {
    private static final String INTENT_ETWS_ALARM = "com.android.internal.telephony.etws";
    private static final boolean VDBG = false;
    private static boolean[] mIsCellAreaInSpecificRegion;
    private CellBroadcastFwkExt mCellBroadcastFwkExt;
    private PendingIntent mEtwsAlarmIntent;
    private final BroadcastReceiver mEtwsPrimaryBroadcastReceiver;
    private final BroadcastReceiver mPlmnChangedBroadcastReceiver;
    private final HashMap<SmsCbConcatInfo, byte[][]> mSmsCbPageMap;

    /* renamed from: com.android.internal.telephony.gsm.GsmCellBroadcastHandler$1 */
    class AnonymousClass1 extends BroadcastReceiver {
        final /* synthetic */ GsmCellBroadcastHandler this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.1.<init>(com.android.internal.telephony.gsm.GsmCellBroadcastHandler):void, dex: 
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
        AnonymousClass1(com.android.internal.telephony.gsm.GsmCellBroadcastHandler r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.1.<init>(com.android.internal.telephony.gsm.GsmCellBroadcastHandler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.1.<init>(com.android.internal.telephony.gsm.GsmCellBroadcastHandler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.1.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    /* renamed from: com.android.internal.telephony.gsm.GsmCellBroadcastHandler$2 */
    class AnonymousClass2 extends BroadcastReceiver {
        final /* synthetic */ GsmCellBroadcastHandler this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.2.<init>(com.android.internal.telephony.gsm.GsmCellBroadcastHandler):void, dex: 
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
        AnonymousClass2(com.android.internal.telephony.gsm.GsmCellBroadcastHandler r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.2.<init>(com.android.internal.telephony.gsm.GsmCellBroadcastHandler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.2.<init>(com.android.internal.telephony.gsm.GsmCellBroadcastHandler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.2.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.2.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.2.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    private static final class SmsCbConcatInfo {
        private final SmsCbHeader mHeader;
        private final SmsCbLocation mLocation;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.SmsCbConcatInfo.<init>(com.android.internal.telephony.gsm.SmsCbHeader, android.telephony.SmsCbLocation):void, dex: 
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
        SmsCbConcatInfo(com.android.internal.telephony.gsm.SmsCbHeader r1, android.telephony.SmsCbLocation r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.SmsCbConcatInfo.<init>(com.android.internal.telephony.gsm.SmsCbHeader, android.telephony.SmsCbLocation):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.SmsCbConcatInfo.<init>(com.android.internal.telephony.gsm.SmsCbHeader, android.telephony.SmsCbLocation):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.SmsCbConcatInfo.equals(java.lang.Object):boolean, dex: 
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
        public boolean equals(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.SmsCbConcatInfo.equals(java.lang.Object):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.SmsCbConcatInfo.equals(java.lang.Object):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.SmsCbConcatInfo.hashCode():int, dex: 
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
        public int hashCode() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.SmsCbConcatInfo.hashCode():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.SmsCbConcatInfo.hashCode():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.SmsCbConcatInfo.matchesLocation(java.lang.String, int, int):boolean, dex: 
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
        public boolean matchesLocation(java.lang.String r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.SmsCbConcatInfo.matchesLocation(java.lang.String, int, int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.SmsCbConcatInfo.matchesLocation(java.lang.String, int, int):boolean");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.GsmCellBroadcastHandler.<clinit>():void");
    }

    protected GsmCellBroadcastHandler(Context context, Phone phone) {
        super("GsmCellBroadcastHandler", context, phone);
        this.mSmsCbPageMap = new HashMap(4);
        this.mEtwsAlarmIntent = null;
        this.mCellBroadcastFwkExt = null;
        this.mEtwsPrimaryBroadcastReceiver = new AnonymousClass1(this);
        this.mPlmnChangedBroadcastReceiver = new AnonymousClass2(this);
        phone.mCi.setOnNewGsmBroadcastSms(getHandler(), 1, null);
        phone.mCi.setOnEtwsNotification(getHandler(), ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT, null);
        this.mCellBroadcastFwkExt = new CellBroadcastFwkExt(phone);
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_ETWS_ALARM);
        context.registerReceiver(this.mEtwsPrimaryBroadcastReceiver, filter);
        IntentFilter plmnFliter = new IntentFilter();
        plmnFliter.addAction("mediatek.intent.action.LOCATED_PLMN_CHANGED");
        context.registerReceiver(this.mPlmnChangedBroadcastReceiver, plmnFliter);
        for (int i = 0; i < SmsCbConstants.specificRegionList.length; i++) {
            mIsCellAreaInSpecificRegion[i] = false;
        }
    }

    protected void onQuitting() {
        this.mPhone.mCi.unSetOnNewGsmBroadcastSms(getHandler());
        this.mPhone.mCi.unSetOnEtwsNotification(getHandler());
        super.onQuitting();
    }

    public static GsmCellBroadcastHandler makeGsmCellBroadcastHandler(Context context, Phone phone) {
        GsmCellBroadcastHandler handler = new GsmCellBroadcastHandler(context, phone);
        handler.start();
        return handler;
    }

    protected boolean handleSmsMessage(Message message) {
        if (message.obj instanceof AsyncResult) {
            SmsCbMessage cbMessage = handleGsmBroadcastSms((AsyncResult) message.obj);
            if (cbMessage != null) {
                handleBroadcastSms(cbMessage);
                return true;
            }
        }
        return super.handleSmsMessage(message);
    }

    private SmsCbMessage handleGsmBroadcastSms(AsyncResult ar) {
        try {
            SmsCbLocation location;
            byte[][] pdus;
            byte[] receivedPdu = (byte[]) ar.result;
            SmsCbHeader header = new SmsCbHeader(receivedPdu, false);
            String plmn = TelephonyManager.from(this.mContext).getNetworkOperatorForPhone(this.mPhone.getPhoneId());
            int lac = -1;
            int cid = -1;
            CellLocation cl = this.mPhone.getCellLocation();
            if (cl instanceof GsmCellLocation) {
                GsmCellLocation cellLocation = (GsmCellLocation) cl;
                lac = cellLocation.getLac();
                cid = cellLocation.getCid();
            }
            switch (header.getGeographicalScope()) {
                case 0:
                case 3:
                    location = new SmsCbLocation(plmn, lac, cid);
                    break;
                case 2:
                    location = new SmsCbLocation(plmn, lac, -1);
                    break;
                default:
                    location = new SmsCbLocation(plmn);
                    break;
            }
            int pageCount = header.getNumberOfPages();
            if (pageCount > 1) {
                SmsCbConcatInfo concatInfo = new SmsCbConcatInfo(header, location);
                pdus = (byte[][]) this.mSmsCbPageMap.get(concatInfo);
                if (pdus == null) {
                    pdus = new byte[pageCount][];
                    this.mSmsCbPageMap.put(concatInfo, pdus);
                }
                pdus[header.getPageIndex() - 1] = receivedPdu;
                for (byte[] pdu : pdus) {
                    if (pdu == null) {
                        return null;
                    }
                }
                this.mSmsCbPageMap.remove(concatInfo);
            } else {
                pdus = new byte[1][];
                pdus[0] = receivedPdu;
            }
            Iterator<SmsCbConcatInfo> iter = this.mSmsCbPageMap.keySet().iterator();
            while (iter.hasNext()) {
                if (!((SmsCbConcatInfo) iter.next()).matchesLocation(plmn, lac, cid)) {
                    iter.remove();
                }
            }
            if (header.getServiceCategory() == 4352 || header.getServiceCategory() == 4353 || header.getServiceCategory() == 4354 || header.getServiceCategory() == 4355 || header.getServiceCategory() == 4356) {
                stopEtwsAlarm();
                startEtwsAlarm();
            }
            return GsmSmsCbMessage.createSmsCbMessage(header, location, pdus);
        } catch (RuntimeException e) {
            -wrap2("Error in decoding SMS CB pdu", e);
            return null;
        }
    }

    protected boolean handleEtwsPrimaryNotification(Message message) {
        if (message.obj instanceof AsyncResult) {
            EtwsNotification noti = message.obj.result;
            -wrap0(noti.toString());
            if (this.mCellBroadcastFwkExt.containDuplicatedEtwsNotification(noti)) {
                -wrap0("find duplicated ETWS notifiction");
                return false;
            }
            this.mCellBroadcastFwkExt.openEtwsChannel(noti);
            SmsCbMessage etwsPrimary = handleEtwsPdu(noti.getEtwsPdu(), noti.plmnId);
            if (etwsPrimary != null) {
                -wrap0("ETWS Primary dispatch to App, open necessary channels and start timer");
                handleBroadcastSms(etwsPrimary, true);
                stopEtwsAlarm();
                startEtwsAlarm();
                return true;
            }
        }
        return super.handleEtwsPrimaryNotification(message);
    }

    private SmsCbMessage handleEtwsPdu(byte[] pdu, String plmn) {
        if (pdu == null || pdu.length != 56) {
            -wrap0("invalid ETWS PDU");
            return null;
        }
        SmsCbLocation location;
        SmsCbHeader header = new SmsCbHeader(pdu, true);
        GsmCellLocation cellLocation = (GsmCellLocation) this.mPhone.getCellLocation();
        int lac = cellLocation.getLac();
        int cid = cellLocation.getCid();
        switch (header.getGeographicalScope()) {
            case 0:
            case 3:
                location = new SmsCbLocation(plmn, lac, cid);
                break;
            case 2:
                location = new SmsCbLocation(plmn, lac, -1);
                break;
            default:
                location = new SmsCbLocation(plmn);
                break;
        }
        byte[][] pdus = new byte[1][];
        pdus[0] = pdu;
        return GsmSmsCbMessage.createSmsCbMessage(header, location, pdus);
    }

    protected void startEtwsAlarm() {
        AlarmManager am = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        -wrap0("startEtwsAlarm");
        Intent intent = new Intent(INTENT_ETWS_ALARM);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        this.mEtwsAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
        am.set(2, SystemClock.elapsedRealtime() + 1800000, this.mEtwsAlarmIntent);
    }

    protected void stopEtwsAlarm() {
        AlarmManager am = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        -wrap0("stopEtwsAlarm");
        if (this.mEtwsAlarmIntent != null) {
            am.cancel(this.mEtwsAlarmIntent);
            this.mEtwsAlarmIntent = null;
        }
    }

    public static int isCellAreaInSpecificRegion() {
        for (int i = 0; i < SmsCbConstants.specificRegionList.length; i++) {
            if (mIsCellAreaInSpecificRegion[i]) {
                return SmsCbConstants.specificRegionList[i];
            }
        }
        return 0;
    }
}
