package com.android.internal.telephony;

import android.app.ActivityManager;
import android.content.AsyncQueryHandler;
import android.content.AsyncQueryHandler.WorkerArgs;
import android.content.AsyncQueryHandler.WorkerHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.SQLException;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.ContactsContract.PhoneLookup;
import android.telecom.PhoneAccount;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

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
public class CallerInfoAsyncQuery {
    private static final boolean DBG = true;
    private static final boolean ENABLE_UNKNOWN_NUMBER_GEO_DESCRIPTION = true;
    private static final int EVENT_ADD_LISTENER = 2;
    private static final int EVENT_EMERGENCY_NUMBER = 4;
    private static final int EVENT_END_OF_QUEUE = 3;
    private static final int EVENT_GET_GEO_DESCRIPTION = 100;
    private static final int EVENT_NEW_QUERY = 1;
    private static final int EVENT_VOICEMAIL_NUMBER = 5;
    private static final String LOG_TAG = "CallerInfoAsyncQuery";
    private CallerInfoAsyncQueryHandler mHandler;

    private class CallerInfoAsyncQueryHandler extends AsyncQueryHandler {
        private CallerInfo mCallerInfo;
        private Context mContext;
        private List<Runnable> mPendingListenerCallbacks;
        private Uri mQueryUri;
        final /* synthetic */ CallerInfoAsyncQuery this$0;

        /* renamed from: com.android.internal.telephony.CallerInfoAsyncQuery$CallerInfoAsyncQueryHandler$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ CallerInfoAsyncQueryHandler this$1;
            final /* synthetic */ CookieWrapper val$cw;
            final /* synthetic */ int val$token;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.CallerInfoAsyncQuery.CallerInfoAsyncQueryHandler.1.<init>(com.android.internal.telephony.CallerInfoAsyncQuery$CallerInfoAsyncQueryHandler, com.android.internal.telephony.CallerInfoAsyncQuery$CookieWrapper, int):void, dex: 
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
            AnonymousClass1(com.android.internal.telephony.CallerInfoAsyncQuery.CallerInfoAsyncQueryHandler r1, com.android.internal.telephony.CallerInfoAsyncQuery.CookieWrapper r2, int r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.CallerInfoAsyncQuery.CallerInfoAsyncQueryHandler.1.<init>(com.android.internal.telephony.CallerInfoAsyncQuery$CallerInfoAsyncQueryHandler, com.android.internal.telephony.CallerInfoAsyncQuery$CookieWrapper, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.CallerInfoAsyncQuery.CallerInfoAsyncQueryHandler.1.<init>(com.android.internal.telephony.CallerInfoAsyncQuery$CallerInfoAsyncQueryHandler, com.android.internal.telephony.CallerInfoAsyncQuery$CookieWrapper, int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.CallerInfoAsyncQuery.CallerInfoAsyncQueryHandler.1.run():void, dex: 
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
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.CallerInfoAsyncQuery.CallerInfoAsyncQueryHandler.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.CallerInfoAsyncQuery.CallerInfoAsyncQueryHandler.1.run():void");
            }
        }

        protected class CallerInfoWorkerHandler extends WorkerHandler {
            final /* synthetic */ CallerInfoAsyncQueryHandler this$1;

            public CallerInfoWorkerHandler(CallerInfoAsyncQueryHandler this$1, Looper looper) {
                this.this$1 = this$1;
                super(this$1, looper);
            }

            public void handleMessage(Message msg) {
                WorkerArgs args = msg.obj;
                CookieWrapper cw = args.cookie;
                if (cw == null) {
                    Rlog.i(CallerInfoAsyncQuery.LOG_TAG, "Unexpected command (CookieWrapper is null): " + msg.what + " ignored by CallerInfoWorkerHandler, passing onto parent.");
                    super.handleMessage(msg);
                    return;
                }
                Rlog.d(CallerInfoAsyncQuery.LOG_TAG, "Processing event: " + cw.event + " token (arg1): " + msg.arg1 + " command: " + msg.what + " query URI: " + CallerInfoAsyncQuery.sanitizeUriToString(args.uri));
                switch (cw.event) {
                    case 1:
                        super.handleMessage(msg);
                        return;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        Message reply = args.handler.obtainMessage(msg.what);
                        reply.obj = args;
                        reply.arg1 = msg.arg1;
                        reply.sendToTarget();
                        return;
                    case 100:
                        handleGeoDescription(msg);
                        return;
                    default:
                        return;
                }
            }

            private void handleGeoDescription(Message msg) {
                WorkerArgs args = msg.obj;
                CookieWrapper cw = args.cookie;
                if (!(TextUtils.isEmpty(cw.number) || cw.cookie == null || this.this$1.mContext == null)) {
                    long startTimeMillis = SystemClock.elapsedRealtime();
                    cw.geoDescription = CallerInfo.getGeoDescription(this.this$1.mContext, cw.number);
                    long duration = SystemClock.elapsedRealtime() - startTimeMillis;
                    if (duration > 500) {
                        Rlog.d(CallerInfoAsyncQuery.LOG_TAG, "[handleGeoDescription]Spends long time to retrieve Geo description: " + duration);
                    }
                }
                Message reply = args.handler.obtainMessage(msg.what);
                reply.obj = args;
                reply.arg1 = msg.arg1;
                reply.sendToTarget();
            }
        }

        /* synthetic */ CallerInfoAsyncQueryHandler(CallerInfoAsyncQuery this$0, Context context, CallerInfoAsyncQueryHandler callerInfoAsyncQueryHandler) {
            this(this$0, context);
        }

        private CallerInfoAsyncQueryHandler(CallerInfoAsyncQuery this$0, Context context) {
            this.this$0 = this$0;
            super(CallerInfoAsyncQuery.getCurrentProfileContentResolver(context));
            this.mPendingListenerCallbacks = new ArrayList();
            this.mContext = context;
        }

        protected Handler createHandler(Looper looper) {
            return new CallerInfoWorkerHandler(this, looper);
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
        protected void onQueryComplete(int r18, java.lang.Object r19, android.database.Cursor r20) {
            /*
            r17 = this;
            r2 = "CallerInfoAsyncQuery";
            r3 = new java.lang.StringBuilder;
            r3.<init>();
            r5 = "##### onQueryComplete() #####   query complete for token: ";
            r3 = r3.append(r5);
            r0 = r18;
            r3 = r3.append(r0);
            r3 = r3.toString();
            android.telephony.Rlog.d(r2, r3);
            r13 = r19;
            r13 = (com.android.internal.telephony.CallerInfoAsyncQuery.CookieWrapper) r13;
            if (r13 != 0) goto L_0x0031;
        L_0x0022:
            r2 = "CallerInfoAsyncQuery";
            r3 = "Cookie is null, ignoring onQueryComplete() request.";
            android.telephony.Rlog.i(r2, r3);
            if (r20 == 0) goto L_0x0030;
        L_0x002d:
            r20.close();
        L_0x0030:
            return;
        L_0x0031:
            r2 = r13.event;
            r3 = 3;
            if (r2 != r3) goto L_0x0062;
        L_0x0036:
            r0 = r17;
            r2 = r0.mPendingListenerCallbacks;
            r16 = r2.iterator();
        L_0x003e:
            r2 = r16.hasNext();
            if (r2 == 0) goto L_0x004e;
        L_0x0044:
            r15 = r16.next();
            r15 = (java.lang.Runnable) r15;
            r15.run();
            goto L_0x003e;
        L_0x004e:
            r0 = r17;
            r2 = r0.mPendingListenerCallbacks;
            r2.clear();
            r0 = r17;
            r2 = r0.this$0;
            r2.release();
            if (r20 == 0) goto L_0x0061;
        L_0x005e:
            r20.close();
        L_0x0061:
            return;
        L_0x0062:
            r2 = r13.event;
            r3 = 100;
            if (r2 != r3) goto L_0x008b;
        L_0x0068:
            r0 = r17;
            r2 = r0.mCallerInfo;
            if (r2 == 0) goto L_0x0076;
        L_0x006e:
            r0 = r17;
            r2 = r0.mCallerInfo;
            r3 = r13.geoDescription;
            r2.geoDescription = r3;
        L_0x0076:
            r4 = new com.android.internal.telephony.CallerInfoAsyncQuery$CookieWrapper;
            r2 = 0;
            r4.<init>(r2);
            r2 = 3;
            r4.event = r2;
            r5 = 0;
            r6 = 0;
            r7 = 0;
            r8 = 0;
            r9 = 0;
            r2 = r17;
            r3 = r18;
            r2.startQuery(r3, r4, r5, r6, r7, r8, r9);
        L_0x008b:
            r0 = r17;
            r2 = r0.mCallerInfo;
            if (r2 != 0) goto L_0x00ed;
        L_0x0091:
            r0 = r17;
            r2 = r0.mContext;
            if (r2 == 0) goto L_0x009d;
        L_0x0097:
            r0 = r17;
            r2 = r0.mQueryUri;
            if (r2 != 0) goto L_0x00a6;
        L_0x009d:
            r2 = new com.android.internal.telephony.CallerInfoAsyncQuery$QueryPoolException;
            r3 = "Bad context or query uri, or CallerInfoAsyncQuery already released.";
            r2.<init>(r3);
            throw r2;
        L_0x00a6:
            r2 = r13.event;
            r3 = 4;
            if (r2 != r3) goto L_0x0107;
        L_0x00ab:
            r2 = new com.android.internal.telephony.CallerInfo;
            r2.<init>();
            r0 = r17;
            r3 = r0.mContext;
            r2 = r2.markAsEmergency(r3);
            r0 = r17;
            r0.mCallerInfo = r2;
        L_0x00bc:
            r2 = "CallerInfoAsyncQuery";
            r3 = new java.lang.StringBuilder;
            r3.<init>();
            r5 = "constructing CallerInfo object for token: ";
            r3 = r3.append(r5);
            r0 = r18;
            r3 = r3.append(r0);
            r3 = r3.toString();
            android.telephony.Rlog.d(r2, r3);
            r4 = new com.android.internal.telephony.CallerInfoAsyncQuery$CookieWrapper;
            r2 = 0;
            r4.<init>(r2);
            r2 = 3;
            r4.event = r2;
            r5 = 0;
            r6 = 0;
            r7 = 0;
            r8 = 0;
            r9 = 0;
            r2 = r17;
            r3 = r18;
            r2.startQuery(r3, r4, r5, r6, r7, r8, r9);
        L_0x00ed:
            r2 = r13.listener;
            if (r2 == 0) goto L_0x01dd;
        L_0x00f1:
            r0 = r17;
            r2 = r0.mPendingListenerCallbacks;
            r3 = new com.android.internal.telephony.CallerInfoAsyncQuery$CallerInfoAsyncQueryHandler$1;
            r0 = r17;
            r1 = r18;
            r3.<init>(r0, r13, r1);
            r2.add(r3);
        L_0x0101:
            if (r20 == 0) goto L_0x0106;
        L_0x0103:
            r20.close();
        L_0x0106:
            return;
        L_0x0107:
            r2 = r13.event;
            r3 = 5;
            if (r2 != r3) goto L_0x011c;
        L_0x010c:
            r2 = new com.android.internal.telephony.CallerInfo;
            r2.<init>();
            r3 = r13.subId;
            r2 = r2.markAsVoiceMail(r3);
            r0 = r17;
            r0.mCallerInfo = r2;
            goto L_0x00bc;
        L_0x011c:
            r0 = r17;
            r2 = r0.mContext;
            r0 = r17;
            r3 = r0.mQueryUri;
            r5 = r13.subId;
            r0 = r20;
            r2 = com.android.internal.telephony.CallerInfo.getCallerInfo(r2, r3, r0, r5);
            r0 = r17;
            r0.mCallerInfo = r2;
            r2 = "CallerInfoAsyncQuery";
            r3 = new java.lang.StringBuilder;
            r3.<init>();
            r5 = "==> Got mCallerInfo: ";
            r3 = r3.append(r5);
            r0 = r17;
            r5 = r0.mCallerInfo;
            r3 = r3.append(r5);
            r3 = r3.toString();
            android.telephony.Rlog.d(r2, r3);
            r0 = r17;
            r2 = r0.mContext;
            r3 = r13.number;
            r0 = r17;
            r5 = r0.mCallerInfo;
            r14 = com.android.internal.telephony.CallerInfo.doSecondaryLookupIfNecessary(r2, r3, r5);
            r0 = r17;
            r2 = r0.mCallerInfo;
            if (r14 == r2) goto L_0x0184;
        L_0x0162:
            r0 = r17;
            r0.mCallerInfo = r14;
            r2 = "CallerInfoAsyncQuery";
            r3 = new java.lang.StringBuilder;
            r3.<init>();
            r5 = "#####async contact look up with numeric username";
            r3 = r3.append(r5);
            r0 = r17;
            r5 = r0.mCallerInfo;
            r3 = r3.append(r5);
            r3 = r3.toString();
            android.telephony.Rlog.d(r2, r3);
        L_0x0184:
            r2 = r13.number;
            r2 = android.text.TextUtils.isEmpty(r2);
            if (r2 != 0) goto L_0x01a6;
        L_0x018c:
            r0 = r17;
            r2 = r0.mCallerInfo;
            r3 = r13.number;
            r0 = r17;
            r5 = r0.mCallerInfo;
            r5 = r5.normalizedNumber;
            r0 = r17;
            r6 = r0.mContext;
            r6 = com.android.internal.telephony.CallerInfo.getCurrentCountryIso(r6);
            r3 = android.telephony.PhoneNumberUtils.formatNumber(r3, r5, r6);
            r2.phoneNumber = r3;
        L_0x01a6:
            r0 = r17;
            r2 = r0.mCallerInfo;
            r2 = r2.name;
            r2 = android.text.TextUtils.isEmpty(r2);
            if (r2 != 0) goto L_0x01c2;
        L_0x01b2:
            r2 = "ro.mtk_phone_number_geo";
            r2 = android.os.SystemProperties.get(r2);
            r3 = "1";
            r2 = r2.equals(r3);
            if (r2 == 0) goto L_0x00bc;
        L_0x01c2:
            r2 = "CallerInfoAsyncQuery";
            r3 = "start querying geo description";
            android.telephony.Rlog.d(r2, r3);
            r2 = 100;
            r13.event = r2;
            r8 = 0;
            r9 = 0;
            r10 = 0;
            r11 = 0;
            r12 = 0;
            r5 = r17;
            r6 = r18;
            r7 = r13;
            r5.startQuery(r6, r7, r8, r9, r10, r11, r12);
            return;
        L_0x01dd:
            r2 = "CallerInfoAsyncQuery";
            r3 = "There is no listener to notify for this query.";
            android.telephony.Rlog.w(r2, r3);
            goto L_0x0101;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.CallerInfoAsyncQuery.CallerInfoAsyncQueryHandler.onQueryComplete(int, java.lang.Object, android.database.Cursor):void");
        }
    }

    private static final class CookieWrapper {
        public Object cookie;
        public int event;
        public String geoDescription;
        public OnQueryCompleteListener listener;
        public String number;
        public int subId;

        /* synthetic */ CookieWrapper(CookieWrapper cookieWrapper) {
            this();
        }

        private CookieWrapper() {
        }
    }

    public interface OnQueryCompleteListener {
        void onQueryComplete(int i, Object obj, CallerInfo callerInfo);
    }

    public static class QueryPoolException extends SQLException {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.CallerInfoAsyncQuery.QueryPoolException.<init>(java.lang.String):void, dex: 
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
        public QueryPoolException(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.CallerInfoAsyncQuery.QueryPoolException.<init>(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.CallerInfoAsyncQuery.QueryPoolException.<init>(java.lang.String):void");
        }
    }

    static ContentResolver getCurrentProfileContentResolver(Context context) {
        Rlog.d(LOG_TAG, "Trying to get current content resolver...");
        int currentUser = ActivityManager.getCurrentUser();
        int myUser = UserManager.get(context).getUserHandle();
        Rlog.d(LOG_TAG, "myUser=" + myUser + "currentUser=" + currentUser);
        if (myUser != currentUser) {
            try {
                return context.createPackageContextAsUser(context.getPackageName(), 0, new UserHandle(currentUser)).getContentResolver();
            } catch (NameNotFoundException e) {
                Rlog.e(LOG_TAG, "Can't find self package", e);
            }
        }
        return context.getContentResolver();
    }

    private CallerInfoAsyncQuery() {
    }

    public static CallerInfoAsyncQuery startQuery(int token, Context context, Uri contactRef, OnQueryCompleteListener listener, Object cookie) {
        CallerInfoAsyncQuery c = new CallerInfoAsyncQuery();
        c.allocate(context, contactRef);
        Rlog.d(LOG_TAG, "starting query for URI: " + contactRef + " handler: " + c.toString());
        CookieWrapper cw = new CookieWrapper();
        cw.listener = listener;
        cw.cookie = cookie;
        cw.event = 1;
        c.mHandler.startQuery(token, cw, contactRef, null, null, null, null);
        return c;
    }

    public static CallerInfoAsyncQuery startQuery(int token, Context context, String number, OnQueryCompleteListener listener, Object cookie) {
        return startQuery(token, context, number, listener, cookie, SubscriptionManager.getDefaultSubscriptionId());
    }

    public static CallerInfoAsyncQuery startQuery(int token, Context context, String number, OnQueryCompleteListener listener, Object cookie, int subId) {
        Rlog.d(LOG_TAG, "##### CallerInfoAsyncQuery startQuery()... #####");
        Rlog.d(LOG_TAG, "- number: xxxxxxx");
        Rlog.d(LOG_TAG, "- cookie: " + cookie);
        Uri contactRef = PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI.buildUpon().appendPath(number).appendQueryParameter(PhoneAccount.SCHEME_SIP, String.valueOf(PhoneNumberUtils.isUriNumber(number))).build();
        Rlog.d(LOG_TAG, "==> contactRef: " + sanitizeUriToString(contactRef));
        CallerInfoAsyncQuery c = new CallerInfoAsyncQuery();
        c.allocate(context, contactRef);
        CookieWrapper cw = new CookieWrapper();
        cw.listener = listener;
        cw.cookie = cookie;
        cw.number = number;
        cw.subId = subId;
        if (PhoneNumberUtils.isEmergencyNumberExt(number, TelephonyManager.getDefault().getCurrentPhoneType(cw.subId))) {
            cw.event = 4;
        } else if (PhoneNumberUtils.isVoiceMailNumber(subId, number)) {
            cw.event = 5;
        } else {
            cw.event = 1;
        }
        c.mHandler.startQuery(token, cw, contactRef, null, null, null, "(CASE WHEN number='" + number + "' THEN 0 ELSE 1 END) ");
        return c;
    }

    public void addQueryListener(int token, OnQueryCompleteListener listener, Object cookie) {
        Rlog.d(LOG_TAG, "adding listener to query: " + sanitizeUriToString(this.mHandler.mQueryUri) + " handler: " + this.mHandler.toString());
        CookieWrapper cw = new CookieWrapper();
        cw.listener = listener;
        cw.cookie = cookie;
        cw.event = 2;
        this.mHandler.startQuery(token, cw, null, null, null, null, null);
    }

    private void allocate(Context context, Uri contactRef) {
        if (context == null || contactRef == null) {
            throw new QueryPoolException("Bad context or query uri.");
        }
        this.mHandler = new CallerInfoAsyncQueryHandler(this, context, null);
        this.mHandler.mQueryUri = contactRef;
    }

    private void release() {
        this.mHandler.mContext = null;
        this.mHandler.mQueryUri = null;
        this.mHandler.mCallerInfo = null;
        this.mHandler = null;
    }

    private static String sanitizeUriToString(Uri uri) {
        if (uri == null) {
            return PhoneConstants.MVNO_TYPE_NONE;
        }
        String uriString = uri.toString();
        int indexOfLastSlash = uriString.lastIndexOf(47);
        if (indexOfLastSlash > 0) {
            return uriString.substring(0, indexOfLastSlash) + "/xxxxxxx";
        }
        return uriString;
    }
}
