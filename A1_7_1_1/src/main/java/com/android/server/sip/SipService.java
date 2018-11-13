package com.android.server.sip;

import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.sip.ISipService.Stub;
import android.net.sip.ISipSession;
import android.net.sip.ISipSessionListener;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipSessionAdapter;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import javax.sip.SipException;

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
public final class SipService extends Stub {
    static final boolean DBG = true;
    private static final int DEFAULT_KEEPALIVE_INTERVAL = 10;
    private static final int DEFAULT_MAX_KEEPALIVE_INTERVAL = 120;
    private static final int EXPIRY_TIME = 3600;
    private static final int MIN_EXPIRY_TIME = 60;
    private static final int SHORT_EXPIRY_TIME = 10;
    static final String TAG = "SipService";
    private final AppOpsManager mAppOps;
    private ConnectivityReceiver mConnectivityReceiver;
    private Context mContext;
    private MyExecutor mExecutor;
    private int mKeepAliveInterval;
    private int mLastGoodKeepAliveInterval;
    private String mLocalIp;
    private SipWakeLock mMyWakeLock;
    private int mNetworkType;
    private Map<String, ISipSession> mPendingSessions;
    private Map<String, SipSessionGroupExt> mSipGroups;
    private SipKeepAliveProcessCallback mSipKeepAliveProcessCallback;
    private boolean mSipOnWifiOnly;
    private SipWakeupTimer mTimer;
    private WifiLock mWifiLock;

    private class ConnectivityReceiver extends BroadcastReceiver {
        final /* synthetic */ SipService this$0;

        /* renamed from: com.android.server.sip.SipService$ConnectivityReceiver$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ ConnectivityReceiver this$1;
            final /* synthetic */ NetworkInfo val$info;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipService.ConnectivityReceiver.1.<init>(com.android.server.sip.SipService$ConnectivityReceiver, android.net.NetworkInfo):void, dex: 
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
            AnonymousClass1(com.android.server.sip.SipService.ConnectivityReceiver r1, android.net.NetworkInfo r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipService.ConnectivityReceiver.1.<init>(com.android.server.sip.SipService$ConnectivityReceiver, android.net.NetworkInfo):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.ConnectivityReceiver.1.<init>(com.android.server.sip.SipService$ConnectivityReceiver, android.net.NetworkInfo):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.ConnectivityReceiver.1.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.ConnectivityReceiver.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.ConnectivityReceiver.1.run():void");
            }
        }

        /* synthetic */ ConnectivityReceiver(SipService this$0, ConnectivityReceiver connectivityReceiver) {
            this(this$0);
        }

        private ConnectivityReceiver(SipService this$0) {
            this.this$0 = this$0;
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
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        public void onReceive(android.content.Context r5, android.content.Intent r6) {
            /*
            r4 = this;
            r0 = r6.getExtras();
            if (r0 == 0) goto L_0x001d;
        L_0x0006:
            r2 = "networkInfo";
            r1 = r0.get(r2);
            r1 = (android.net.NetworkInfo) r1;
            r2 = r4.this$0;
            r2 = r2.mExecutor;
            r3 = new com.android.server.sip.SipService$ConnectivityReceiver$1;
            r3.<init>(r4, r1);
            r2.execute(r3);
        L_0x001d:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.ConnectivityReceiver.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    private class MyExecutor extends Handler implements Executor {
        final /* synthetic */ SipService this$0;

        MyExecutor(SipService this$0) {
            this.this$0 = this$0;
            super(SipService.createLooper());
        }

        public void execute(Runnable task) {
            this.this$0.mMyWakeLock.acquire((Object) task);
            Message.obtain(this, 0, task).sendToTarget();
        }

        public void handleMessage(Message msg) {
            if (msg.obj instanceof Runnable) {
                executeInternal((Runnable) msg.obj);
            } else {
                this.this$0.log("handleMessage: not Runnable ignore msg=" + msg);
            }
        }

        private void executeInternal(Runnable task) {
            try {
                task.run();
            } catch (Throwable t) {
                this.this$0.loge("run task: " + task, t);
            } finally {
                this.this$0.mMyWakeLock.release(task);
            }
        }
    }

    private class SipAutoReg extends SipSessionAdapter implements Runnable, KeepAliveProcessCallback {
        private static final int MIN_KEEPALIVE_SUCCESS_COUNT = 10;
        private static final boolean SAR_DBG = true;
        private String SAR_TAG;
        private int mBackoff;
        private int mErrorCode;
        private String mErrorMessage;
        private long mExpiryTime;
        private SipSessionImpl mKeepAliveSession;
        private int mKeepAliveSuccessCount;
        private SipSessionListenerProxy mProxy;
        private boolean mRegistered;
        private boolean mRunning;
        private SipSessionImpl mSession;
        final /* synthetic */ SipService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipService.SipAutoReg.<init>(com.android.server.sip.SipService):void, dex: 
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
        private SipAutoReg(com.android.server.sip.SipService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipService.SipAutoReg.<init>(com.android.server.sip.SipService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.<init>(com.android.server.sip.SipService):void");
        }

        /* synthetic */ SipAutoReg(SipService this$0, SipAutoReg sipAutoReg) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipService.SipAutoReg.backoffDuration():int, dex: 
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
        private int backoffDuration() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipService.SipAutoReg.backoffDuration():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.backoffDuration():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.log(java.lang.String):void, dex: 
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
        private void log(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.log(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.log(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.loge(java.lang.String):void, dex: 
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
        private void loge(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.loge(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.loge(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.loge(java.lang.String, java.lang.Throwable):void, dex: 
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
        private void loge(java.lang.String r1, java.lang.Throwable r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.loge(java.lang.String, java.lang.Throwable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.loge(java.lang.String, java.lang.Throwable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.notCurrentSession(android.net.sip.ISipSession):boolean, dex: 
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
        private boolean notCurrentSession(android.net.sip.ISipSession r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.notCurrentSession(android.net.sip.ISipSession):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.notCurrentSession(android.net.sip.ISipSession):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipAutoReg.restart(int):void, dex: 
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
        private void restart(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipAutoReg.restart(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.restart(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.android.server.sip.SipService.SipAutoReg.restartLater():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private void restartLater() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.android.server.sip.SipService.SipAutoReg.restartLater():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.restartLater():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipAutoReg.startKeepAliveProcess(int):void, dex: 
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
        private void startKeepAliveProcess(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipAutoReg.startKeepAliveProcess(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.startKeepAliveProcess(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.stopKeepAliveProcess():void, dex: 
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
        private void stopKeepAliveProcess() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.stopKeepAliveProcess():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.stopKeepAliveProcess():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipService.SipAutoReg.isRegistered():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public boolean isRegistered() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipService.SipAutoReg.isRegistered():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.isRegistered():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipAutoReg.onError(int, java.lang.String):void, dex: 
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
        public void onError(int r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipAutoReg.onError(int, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.onError(int, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.onKeepAliveIntervalChanged():void, dex: 
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
        public void onKeepAliveIntervalChanged() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.onKeepAliveIntervalChanged():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.onKeepAliveIntervalChanged():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipAutoReg.onRegistering(android.net.sip.ISipSession):void, dex: 
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
        public void onRegistering(android.net.sip.ISipSession r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipAutoReg.onRegistering(android.net.sip.ISipSession):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.onRegistering(android.net.sip.ISipSession):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipAutoReg.onRegistrationDone(android.net.sip.ISipSession, int):void, dex: 
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
        public void onRegistrationDone(android.net.sip.ISipSession r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipAutoReg.onRegistrationDone(android.net.sip.ISipSession, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.onRegistrationDone(android.net.sip.ISipSession, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipAutoReg.onRegistrationFailed(android.net.sip.ISipSession, int, java.lang.String):void, dex: 
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
        public void onRegistrationFailed(android.net.sip.ISipSession r1, int r2, java.lang.String r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipAutoReg.onRegistrationFailed(android.net.sip.ISipSession, int, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.onRegistrationFailed(android.net.sip.ISipSession, int, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipAutoReg.onRegistrationTimeout(android.net.sip.ISipSession):void, dex: 
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
        public void onRegistrationTimeout(android.net.sip.ISipSession r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipAutoReg.onRegistrationTimeout(android.net.sip.ISipSession):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.onRegistrationTimeout(android.net.sip.ISipSession):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.onResponse(boolean):void, dex: 
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
        public void onResponse(boolean r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.onResponse(boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.onResponse(boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.run():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.run():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.setListener(android.net.sip.ISipSessionListener):void, dex: 
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
        public void setListener(android.net.sip.ISipSessionListener r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipAutoReg.setListener(android.net.sip.ISipSessionListener):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.setListener(android.net.sip.ISipSessionListener):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipService.SipAutoReg.start(com.android.server.sip.SipSessionGroup):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void start(com.android.server.sip.SipSessionGroup r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipService.SipAutoReg.start(com.android.server.sip.SipSessionGroup):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.start(com.android.server.sip.SipSessionGroup):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipService.SipAutoReg.stop():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void stop() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipService.SipAutoReg.stop():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipAutoReg.stop():void");
        }
    }

    private class SipKeepAliveProcessCallback implements Runnable, KeepAliveProcessCallback {
        private static final int MIN_INTERVAL = 5;
        private static final int NAT_MEASUREMENT_RETRY_INTERVAL = 120;
        private static final int PASS_THRESHOLD = 10;
        private static final boolean SKAI_DBG = true;
        private static final String SKAI_TAG = "SipKeepAliveProcessCallback";
        private SipSessionGroupExt mGroup;
        private int mInterval;
        private SipProfile mLocalProfile;
        private int mMaxInterval;
        private int mMinInterval;
        private int mPassCount;
        private SipSessionImpl mSession;
        final /* synthetic */ SipService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.<init>(com.android.server.sip.SipService, android.net.sip.SipProfile, int, int):void, dex: 
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
        public SipKeepAliveProcessCallback(com.android.server.sip.SipService r1, android.net.sip.SipProfile r2, int r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.<init>(com.android.server.sip.SipService, android.net.sip.SipProfile, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipKeepAliveProcessCallback.<init>(com.android.server.sip.SipService, android.net.sip.SipProfile, int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.checkTermination():boolean, dex: 
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
        private boolean checkTermination() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.checkTermination():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipKeepAliveProcessCallback.checkTermination():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.log(java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void log(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.log(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipKeepAliveProcessCallback.log(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.loge(java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void loge(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.loge(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipKeepAliveProcessCallback.loge(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.loge(java.lang.String, java.lang.Throwable):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void loge(java.lang.String r1, java.lang.Throwable r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.loge(java.lang.String, java.lang.Throwable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipKeepAliveProcessCallback.loge(java.lang.String, java.lang.Throwable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.restart():void, dex: 
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
        private void restart() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.restart():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipKeepAliveProcessCallback.restart():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.restartLater():void, dex: 
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
        private void restartLater() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.restartLater():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipKeepAliveProcessCallback.restartLater():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.onError(int, java.lang.String):void, dex: 
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
        public void onError(int r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.onError(int, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipKeepAliveProcessCallback.onError(int, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.onResponse(boolean):void, dex: 
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
        public void onResponse(boolean r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.onResponse(boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipKeepAliveProcessCallback.onResponse(boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.run():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipKeepAliveProcessCallback.run():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.start():void, dex: 
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
        public void start() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.start():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipKeepAliveProcessCallback.start():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.stop():void, dex: 
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
        public void stop() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipKeepAliveProcessCallback.stop():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipKeepAliveProcessCallback.stop():void");
        }
    }

    private class SipSessionGroupExt extends SipSessionAdapter {
        private static final boolean SSGE_DBG = true;
        private static final String SSGE_TAG = "SipSessionGroupExt";
        private SipAutoReg mAutoRegistration;
        private PendingIntent mIncomingCallPendingIntent;
        private boolean mOpenedToReceiveCalls;
        private SipSessionGroup mSipGroup;
        final /* synthetic */ SipService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipService.SipSessionGroupExt.<init>(com.android.server.sip.SipService, android.net.sip.SipProfile, android.app.PendingIntent, android.net.sip.ISipSessionListener):void, dex: 
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
        public SipSessionGroupExt(com.android.server.sip.SipService r1, android.net.sip.SipProfile r2, android.app.PendingIntent r3, android.net.sip.ISipSessionListener r4) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipService.SipSessionGroupExt.<init>(com.android.server.sip.SipService, android.net.sip.SipProfile, android.app.PendingIntent, android.net.sip.ISipSessionListener):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.<init>(com.android.server.sip.SipService, android.net.sip.SipProfile, android.app.PendingIntent, android.net.sip.ISipSessionListener):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipSessionGroupExt.duplicate(android.net.sip.SipProfile):android.net.sip.SipProfile, dex: 
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
        private android.net.sip.SipProfile duplicate(android.net.sip.SipProfile r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipSessionGroupExt.duplicate(android.net.sip.SipProfile):android.net.sip.SipProfile, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.duplicate(android.net.sip.SipProfile):android.net.sip.SipProfile");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.getUri():java.lang.String, dex: 
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
        private java.lang.String getUri() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.getUri():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.getUri():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipService.SipSessionGroupExt.log(java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void log(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipService.SipSessionGroupExt.log(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.log(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipService.SipSessionGroupExt.loge(java.lang.String, java.lang.Throwable):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void loge(java.lang.String r1, java.lang.Throwable r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipService.SipSessionGroupExt.loge(java.lang.String, java.lang.Throwable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.loge(java.lang.String, java.lang.Throwable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.android.server.sip.SipService.SipSessionGroupExt.close():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void close() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.android.server.sip.SipService.SipSessionGroupExt.close():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.close():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.containsSession(java.lang.String):boolean, dex: 
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
        public boolean containsSession(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.containsSession(java.lang.String):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.containsSession(java.lang.String):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.createSession(android.net.sip.ISipSessionListener):android.net.sip.ISipSession, dex: 
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
        public android.net.sip.ISipSession createSession(android.net.sip.ISipSessionListener r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.createSession(android.net.sip.ISipSessionListener):android.net.sip.ISipSession, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.createSession(android.net.sip.ISipSessionListener):android.net.sip.ISipSession");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.getLocalProfile():android.net.sip.SipProfile, dex: 
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
        public android.net.sip.SipProfile getLocalProfile() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.getLocalProfile():android.net.sip.SipProfile, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.getLocalProfile():android.net.sip.SipProfile");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipService.SipSessionGroupExt.isOpenedToReceiveCalls():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public boolean isOpenedToReceiveCalls() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipService.SipSessionGroupExt.isOpenedToReceiveCalls():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.isOpenedToReceiveCalls():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.isRegistered():boolean, dex: 
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
        public boolean isRegistered() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.isRegistered():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.isRegistered():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipSessionGroupExt.onConnectivityChanged(boolean):void, dex: 
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
        public void onConnectivityChanged(boolean r1) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipSessionGroupExt.onConnectivityChanged(boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.onConnectivityChanged(boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipSessionGroupExt.onError(android.net.sip.ISipSession, int, java.lang.String):void, dex: 
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
        public void onError(android.net.sip.ISipSession r1, int r2, java.lang.String r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipService.SipSessionGroupExt.onError(android.net.sip.ISipSession, int, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.onError(android.net.sip.ISipSession, int, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.onKeepAliveIntervalChanged():void, dex: 
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
        public void onKeepAliveIntervalChanged() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.onKeepAliveIntervalChanged():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.onKeepAliveIntervalChanged():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.onRinging(android.net.sip.ISipSession, android.net.sip.SipProfile, java.lang.String):void, dex: 
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
        public void onRinging(android.net.sip.ISipSession r1, android.net.sip.SipProfile r2, java.lang.String r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.onRinging(android.net.sip.ISipSession, android.net.sip.SipProfile, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.onRinging(android.net.sip.ISipSession, android.net.sip.SipProfile, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.android.server.sip.SipService.SipSessionGroupExt.openToReceiveCalls():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void openToReceiveCalls() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.android.server.sip.SipService.SipSessionGroupExt.openToReceiveCalls():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.openToReceiveCalls():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipService.SipSessionGroupExt.setIncomingCallPendingIntent(android.app.PendingIntent):void, dex: 
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
        public void setIncomingCallPendingIntent(android.app.PendingIntent r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipService.SipSessionGroupExt.setIncomingCallPendingIntent(android.app.PendingIntent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.setIncomingCallPendingIntent(android.app.PendingIntent):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.setListener(android.net.sip.ISipSessionListener):void, dex: 
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
        public void setListener(android.net.sip.ISipSessionListener r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.setListener(android.net.sip.ISipSessionListener):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.setListener(android.net.sip.ISipSessionListener):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.setWakeupTimer(com.android.server.sip.SipWakeupTimer):void, dex: 
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
        void setWakeupTimer(com.android.server.sip.SipWakeupTimer r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipService.SipSessionGroupExt.setWakeupTimer(com.android.server.sip.SipWakeupTimer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipService.SipSessionGroupExt.setWakeupTimer(com.android.server.sip.SipWakeupTimer):void");
        }
    }

    public static void start(Context context) {
        if (SipManager.isApiSupported(context) && ServiceManager.getService("sip") == null) {
            ServiceManager.addService("sip", new SipService(context));
            context.sendBroadcast(new Intent(SipManager.ACTION_SIP_SERVICE_UP));
            slog("start:");
        }
    }

    private SipService(Context context) {
        this.mNetworkType = -1;
        this.mExecutor = new MyExecutor(this);
        this.mSipGroups = new HashMap();
        this.mPendingSessions = new HashMap();
        this.mLastGoodKeepAliveInterval = 10;
        log("SipService: started!");
        this.mContext = context;
        this.mConnectivityReceiver = new ConnectivityReceiver(this, null);
        this.mWifiLock = ((WifiManager) context.getSystemService("wifi")).createWifiLock(1, TAG);
        this.mWifiLock.setReferenceCounted(false);
        this.mSipOnWifiOnly = SipManager.isSipWifiOnly(context);
        this.mMyWakeLock = new SipWakeLock((PowerManager) context.getSystemService("power"));
        this.mTimer = new SipWakeupTimer(context, this.mExecutor);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
    }

    public synchronized SipProfile[] getListOfProfiles(String opPackageName) {
        if (canUseSip(opPackageName, "getListOfProfiles")) {
            boolean isCallerRadio = isCallerRadio();
            ArrayList<SipProfile> profiles = new ArrayList();
            for (SipSessionGroupExt group : this.mSipGroups.values()) {
                if (isCallerRadio || isCallerCreator(group)) {
                    profiles.add(group.getLocalProfile());
                }
            }
            return (SipProfile[]) profiles.toArray(new SipProfile[profiles.size()]);
        }
        return new SipProfile[0];
    }

    public synchronized void open(SipProfile localProfile, String opPackageName) {
        if (canUseSip(opPackageName, "open")) {
            localProfile.setCallingUid(Binder.getCallingUid());
            try {
                createGroup(localProfile);
            } catch (SipException e) {
                loge("openToMakeCalls()", e);
            }
        } else {
            return;
        }
    }

    public synchronized void open3(SipProfile localProfile, PendingIntent incomingCallPendingIntent, ISipSessionListener listener, String opPackageName) {
        if (canUseSip(opPackageName, "open3")) {
            localProfile.setCallingUid(Binder.getCallingUid());
            if (incomingCallPendingIntent == null) {
                log("open3: incomingCallPendingIntent cannot be null; the profile is not opened");
                return;
            }
            log("open3: " + obfuscateSipUri(localProfile.getUriString()) + ": " + incomingCallPendingIntent + ": " + listener);
            try {
                SipSessionGroupExt group = createGroup(localProfile, incomingCallPendingIntent, listener);
                if (localProfile.getAutoRegistration()) {
                    group.openToReceiveCalls();
                    updateWakeLocks();
                }
            } catch (SipException e) {
                loge("open3:", e);
            }
        } else {
            return;
        }
        return;
    }

    private boolean isCallerCreator(SipSessionGroupExt group) {
        return group.getLocalProfile().getCallingUid() == Binder.getCallingUid() ? DBG : false;
    }

    private boolean isCallerCreatorOrRadio(SipSessionGroupExt group) {
        return !isCallerRadio() ? isCallerCreator(group) : DBG;
    }

    private boolean isCallerRadio() {
        return Binder.getCallingUid() == 1001 ? DBG : false;
    }

    public synchronized void close(String localProfileUri, String opPackageName) {
        if (canUseSip(opPackageName, "close")) {
            SipSessionGroupExt group = (SipSessionGroupExt) this.mSipGroups.get(localProfileUri);
            if (group != null) {
                if (isCallerCreatorOrRadio(group)) {
                    group = (SipSessionGroupExt) this.mSipGroups.remove(localProfileUri);
                    notifyProfileRemoved(group.getLocalProfile());
                    group.close();
                    updateWakeLocks();
                    return;
                }
                log("only creator or radio can close this profile");
            }
        }
    }

    public synchronized boolean isOpened(String localProfileUri, String opPackageName) {
        if (!canUseSip(opPackageName, "isOpened")) {
            return false;
        }
        SipSessionGroupExt group = (SipSessionGroupExt) this.mSipGroups.get(localProfileUri);
        if (group == null) {
            return false;
        }
        if (isCallerCreatorOrRadio(group)) {
            return DBG;
        }
        log("only creator or radio can query on the profile");
        return false;
    }

    public synchronized boolean isRegistered(String localProfileUri, String opPackageName) {
        if (!canUseSip(opPackageName, "isRegistered")) {
            return false;
        }
        SipSessionGroupExt group = (SipSessionGroupExt) this.mSipGroups.get(localProfileUri);
        if (group == null) {
            return false;
        }
        if (isCallerCreatorOrRadio(group)) {
            return group.isRegistered();
        }
        log("only creator or radio can query on the profile");
        return false;
    }

    /* JADX WARNING: Missing block: B:16:0x0022, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setRegistrationListener(String localProfileUri, ISipSessionListener listener, String opPackageName) {
        if (canUseSip(opPackageName, "setRegistrationListener")) {
            SipSessionGroupExt group = (SipSessionGroupExt) this.mSipGroups.get(localProfileUri);
            if (group != null) {
                if (isCallerCreator(group)) {
                    group.setListener(listener);
                } else {
                    log("only creator can set listener on the profile");
                }
            }
        }
    }

    public synchronized ISipSession createSession(SipProfile localProfile, ISipSessionListener listener, String opPackageName) {
        log("createSession: profile" + localProfile);
        if (!canUseSip(opPackageName, "createSession")) {
            return null;
        }
        localProfile.setCallingUid(Binder.getCallingUid());
        if (this.mNetworkType == -1) {
            log("createSession: mNetworkType==-1 ret=null");
            return null;
        }
        try {
            if (((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo().getState() != State.SUSPENDED) {
                return createGroup(localProfile).createSession(listener);
            }
            log("createSession due to mobile suspend");
            return null;
        } catch (SipException e) {
            loge("createSession;", e);
            return null;
        }
    }

    public synchronized ISipSession getPendingSession(String callId, String opPackageName) {
        if (!canUseSip(opPackageName, "getPendingSession")) {
            return null;
        }
        if (callId == null) {
            return null;
        }
        return (ISipSession) this.mPendingSessions.get(callId);
    }

    private String determineLocalIp() {
        try {
            DatagramSocket s = new DatagramSocket();
            s.connect(InetAddress.getByName("192.168.1.1"), 80);
            return s.getLocalAddress().getHostAddress();
        } catch (IOException e) {
            loge("determineLocalIp()", e);
            return null;
        }
    }

    private SipSessionGroupExt createGroup(SipProfile localProfile) throws SipException {
        String key = localProfile.getUriString();
        SipSessionGroupExt group = (SipSessionGroupExt) this.mSipGroups.get(key);
        if (group == null) {
            group = new SipSessionGroupExt(this, localProfile, null, null);
            this.mSipGroups.put(key, group);
            notifyProfileAdded(localProfile);
            return group;
        } else if (isCallerCreator(group)) {
            return group;
        } else {
            throw new SipException("only creator can access the profile");
        }
    }

    private SipSessionGroupExt createGroup(SipProfile localProfile, PendingIntent incomingCallPendingIntent, ISipSessionListener listener) throws SipException {
        String key = localProfile.getUriString();
        SipSessionGroupExt group = (SipSessionGroupExt) this.mSipGroups.get(key);
        if (group == null) {
            group = new SipSessionGroupExt(this, localProfile, incomingCallPendingIntent, listener);
            this.mSipGroups.put(key, group);
            notifyProfileAdded(localProfile);
            return group;
        } else if (isCallerCreator(group)) {
            group.setIncomingCallPendingIntent(incomingCallPendingIntent);
            group.setListener(listener);
            return group;
        } else {
            throw new SipException("only creator can access the profile");
        }
    }

    private void notifyProfileAdded(SipProfile localProfile) {
        log("notify: profile added: " + localProfile);
        Intent intent = new Intent(SipManager.ACTION_SIP_ADD_PHONE);
        intent.putExtra(SipManager.EXTRA_LOCAL_URI, localProfile.getUriString());
        this.mContext.sendBroadcast(intent);
        if (this.mSipGroups.size() == 1) {
            registerReceivers();
        }
    }

    private void notifyProfileRemoved(SipProfile localProfile) {
        log("notify: profile removed: " + localProfile);
        Intent intent = new Intent(SipManager.ACTION_SIP_REMOVE_PHONE);
        intent.putExtra(SipManager.EXTRA_LOCAL_URI, localProfile.getUriString());
        this.mContext.sendBroadcast(intent);
        if (this.mSipGroups.size() == 0) {
            unregisterReceivers();
        }
    }

    private void stopPortMappingMeasurement() {
        if (this.mSipKeepAliveProcessCallback != null) {
            this.mSipKeepAliveProcessCallback.stop();
            this.mSipKeepAliveProcessCallback = null;
        }
    }

    private void startPortMappingLifetimeMeasurement(SipProfile localProfile) {
        startPortMappingLifetimeMeasurement(localProfile, DEFAULT_MAX_KEEPALIVE_INTERVAL);
    }

    private void startPortMappingLifetimeMeasurement(SipProfile localProfile, int maxInterval) {
        if (this.mSipKeepAliveProcessCallback == null && this.mKeepAliveInterval == -1 && isBehindNAT(this.mLocalIp)) {
            log("startPortMappingLifetimeMeasurement: profile=" + localProfile.getUriString());
            int minInterval = this.mLastGoodKeepAliveInterval;
            if (minInterval >= maxInterval) {
                this.mLastGoodKeepAliveInterval = 10;
                minInterval = 10;
                log("  reset min interval to " + 10);
            }
            this.mSipKeepAliveProcessCallback = new SipKeepAliveProcessCallback(this, localProfile, minInterval, maxInterval);
            this.mSipKeepAliveProcessCallback.start();
        }
    }

    private void restartPortMappingLifetimeMeasurement(SipProfile localProfile, int maxInterval) {
        stopPortMappingMeasurement();
        this.mKeepAliveInterval = -1;
        startPortMappingLifetimeMeasurement(localProfile, maxInterval);
    }

    private synchronized void addPendingSession(ISipSession session) {
        try {
            cleanUpPendingSessions();
            this.mPendingSessions.put(session.getCallId(), session);
            log("#pending sess=" + this.mPendingSessions.size());
        } catch (RemoteException e) {
            loge("addPendingSession()", e);
        }
        return;
    }

    private void cleanUpPendingSessions() throws RemoteException {
        for (Entry<String, ISipSession> entry : (Entry[]) this.mPendingSessions.entrySet().toArray(new Entry[this.mPendingSessions.size()])) {
            if (((ISipSession) entry.getValue()).getState() != 3) {
                this.mPendingSessions.remove(entry.getKey());
            }
        }
    }

    private synchronized boolean callingSelf(SipSessionGroupExt ringingGroup, SipSessionImpl ringingSession) {
        String callId = ringingSession.getCallId();
        for (SipSessionGroupExt group : this.mSipGroups.values()) {
            if (group != ringingGroup && group.containsSession(callId)) {
                log("call self: " + ringingSession.getLocalProfile().getUriString() + " -> " + group.getLocalProfile().getUriString());
                return DBG;
            }
        }
        return false;
    }

    private synchronized void onKeepAliveIntervalChanged() {
        for (SipSessionGroupExt group : this.mSipGroups.values()) {
            group.onKeepAliveIntervalChanged();
        }
    }

    private int getKeepAliveInterval() {
        if (this.mKeepAliveInterval < 0) {
            return this.mLastGoodKeepAliveInterval;
        }
        return this.mKeepAliveInterval;
    }

    private boolean isBehindNAT(String address) {
        try {
            byte[] d = InetAddress.getByName(address).getAddress();
            return (d[0] == (byte) 10 || (((d[0] & 255) == 172 && (d[1] & 240) == 16) || ((d[0] & 255) == 192 && (d[1] & 255) == 168))) ? DBG : false;
        } catch (UnknownHostException e) {
            loge("isBehindAT()" + address, e);
        }
    }

    private boolean canUseSip(String packageName, String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.USE_SIP", message);
        if (this.mAppOps.noteOp(53, Binder.getCallingUid(), packageName) == 0) {
            return DBG;
        }
        return false;
    }

    private void registerReceivers() {
        this.mContext.registerReceiver(this.mConnectivityReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        log("registerReceivers:");
    }

    private void unregisterReceivers() {
        this.mContext.unregisterReceiver(this.mConnectivityReceiver);
        log("unregisterReceivers:");
        this.mWifiLock.release();
        this.mNetworkType = -1;
    }

    private void updateWakeLocks() {
        for (SipSessionGroupExt group : this.mSipGroups.values()) {
            if (group.isOpenedToReceiveCalls()) {
                if (this.mNetworkType == 1 || this.mNetworkType == -1) {
                    this.mWifiLock.acquire();
                } else {
                    this.mWifiLock.release();
                }
                return;
            }
        }
        this.mWifiLock.release();
        this.mMyWakeLock.reset();
    }

    /* JADX WARNING: Missing block: B:7:0x0011, code:
            if (r10.getType() == r9.mNetworkType) goto L_0x0022;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void onConnectivityChanged(NetworkInfo info) {
        if (info != null) {
            if (!info.isConnected()) {
            }
        }
        info = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        int networkType = (info == null || !info.isConnected()) ? -1 : info.getType();
        if (this.mSipOnWifiOnly && networkType != 1) {
            networkType = -1;
        }
        if (this.mNetworkType != networkType) {
            log("onConnectivityChanged: " + this.mNetworkType + " -> " + networkType);
            if (this.mNetworkType != -1) {
                this.mLocalIp = null;
                stopPortMappingMeasurement();
                for (SipSessionGroupExt group : this.mSipGroups.values()) {
                    group.onConnectivityChanged(false);
                }
            }
            try {
                this.mNetworkType = networkType;
                if (this.mNetworkType != -1) {
                    this.mLocalIp = determineLocalIp();
                    this.mKeepAliveInterval = -1;
                    this.mLastGoodKeepAliveInterval = 10;
                    for (SipSessionGroupExt group2 : this.mSipGroups.values()) {
                        group2.onConnectivityChanged(DBG);
                    }
                }
                updateWakeLocks();
            } catch (SipException e) {
                loge("onConnectivityChanged()", e);
            }
        } else {
            return;
        }
        return;
    }

    private static Looper createLooper() {
        HandlerThread thread = new HandlerThread("SipService.Executor");
        thread.start();
        return thread.getLooper();
    }

    private void log(String s) {
        Rlog.d(TAG, s);
    }

    private static void slog(String s) {
        Rlog.d(TAG, s);
    }

    private void loge(String s, Throwable e) {
        Rlog.e(TAG, s, e);
    }

    public static String obfuscateSipUri(String sipUri) {
        StringBuilder sb = new StringBuilder();
        int start = 0;
        sipUri = sipUri.trim();
        if (sipUri.startsWith("sip:")) {
            start = 4;
            sb.append("sip:");
        }
        char prevC = 0;
        int len = sipUri.length();
        int i = start;
        while (i < len) {
            char c = sipUri.charAt(i);
            char nextC = i + 1 < len ? sipUri.charAt(i + 1) : 0;
            char charToAppend = '*';
            if (i - start < 1 || i + 1 == len || isAllowedCharacter(c) || prevC == '@' || nextC == '@') {
                charToAppend = c;
            }
            sb.append(charToAppend);
            prevC = c;
            i++;
        }
        return sb.toString();
    }

    private static boolean isAllowedCharacter(char c) {
        return (c == '@' || c == '.') ? DBG : false;
    }
}
