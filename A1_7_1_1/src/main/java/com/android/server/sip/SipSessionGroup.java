package com.android.server.sip;

import android.net.sip.ISipSession.Stub;
import android.net.sip.SipProfile;
import android.net.sip.SipSessionAdapter;
import gov.nist.javax.sip.clientauthutils.AccountManager;
import gov.nist.javax.sip.clientauthutils.UserCredentials;
import gov.nist.javax.sip.header.extensions.ReferredByHeader;
import java.util.EventObject;
import java.util.Map;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipListener;
import javax.sip.SipStack;
import javax.sip.Transaction;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;

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
class SipSessionGroup implements SipListener {
    private static final String ANONYMOUS = "anonymous";
    private static final int CANCEL_CALL_TIMER = 3;
    private static final boolean DBG = true;
    private static final boolean DBG_PING = false;
    private static final EventObject DEREGISTER = null;
    private static final EventObject END_CALL = null;
    private static final int END_CALL_TIMER = 3;
    private static final int EXPIRY_TIME = 3600;
    private static final int INCALL_KEEPALIVE_INTERVAL = 10;
    private static final int KEEPALIVE_TIMEOUT = 5;
    private static final String TAG = "SipSession";
    private static final String THREAD_POOL_SIZE = "1";
    private static final long WAKE_LOCK_HOLDING_TIME = 500;
    private SipSessionImpl mCallReceiverSession;
    private String mExternalIp;
    private int mExternalPort;
    private String mLocalIp;
    private final SipProfile mLocalProfile;
    private final String mPassword;
    private Map<String, SipSessionImpl> mSessionMap;
    private SipHelper mSipHelper;
    private SipStack mSipStack;
    private SipWakeLock mWakeLock;
    private SipWakeupTimer mWakeupTimer;

    interface KeepAliveProcessCallback {
        void onError(int i, String str);

        void onResponse(boolean z);
    }

    static class KeepAliveProcessCallbackProxy implements KeepAliveProcessCallback {
        private static final String KAPCP_TAG = "KeepAliveProcessCallbackProxy";
        private KeepAliveProcessCallback mCallback;

        /* renamed from: com.android.server.sip.SipSessionGroup$KeepAliveProcessCallbackProxy$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ KeepAliveProcessCallbackProxy this$1;
            final /* synthetic */ boolean val$portChanged;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.1.<init>(com.android.server.sip.SipSessionGroup$KeepAliveProcessCallbackProxy, boolean):void, dex: 
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
            AnonymousClass1(com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy r1, boolean r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.1.<init>(com.android.server.sip.SipSessionGroup$KeepAliveProcessCallbackProxy, boolean):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.1.<init>(com.android.server.sip.SipSessionGroup$KeepAliveProcessCallbackProxy, boolean):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.1.run():void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.1.run():void");
            }
        }

        /* renamed from: com.android.server.sip.SipSessionGroup$KeepAliveProcessCallbackProxy$2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ KeepAliveProcessCallbackProxy this$1;
            final /* synthetic */ String val$description;
            final /* synthetic */ int val$errorCode;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.2.<init>(com.android.server.sip.SipSessionGroup$KeepAliveProcessCallbackProxy, int, java.lang.String):void, dex: 
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
            AnonymousClass2(com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy r1, int r2, java.lang.String r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.2.<init>(com.android.server.sip.SipSessionGroup$KeepAliveProcessCallbackProxy, int, java.lang.String):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.2.<init>(com.android.server.sip.SipSessionGroup$KeepAliveProcessCallbackProxy, int, java.lang.String):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.2.run():void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.2.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.2.run():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.-get0(com.android.server.sip.SipSessionGroup$KeepAliveProcessCallbackProxy):com.android.server.sip.SipSessionGroup$KeepAliveProcessCallback, dex: 
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
        /* renamed from: -get0 */
        static /* synthetic */ com.android.server.sip.SipSessionGroup.KeepAliveProcessCallback m35-get0(com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.-get0(com.android.server.sip.SipSessionGroup$KeepAliveProcessCallbackProxy):com.android.server.sip.SipSessionGroup$KeepAliveProcessCallback, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.-get0(com.android.server.sip.SipSessionGroup$KeepAliveProcessCallbackProxy):com.android.server.sip.SipSessionGroup$KeepAliveProcessCallback");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.-wrap0(com.android.server.sip.SipSessionGroup$KeepAliveProcessCallbackProxy, java.lang.String, java.lang.Throwable):void, dex: 
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
        /* renamed from: -wrap0 */
        static /* synthetic */ void m36-wrap0(com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy r1, java.lang.String r2, java.lang.Throwable r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.-wrap0(com.android.server.sip.SipSessionGroup$KeepAliveProcessCallbackProxy, java.lang.String, java.lang.Throwable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.-wrap0(com.android.server.sip.SipSessionGroup$KeepAliveProcessCallbackProxy, java.lang.String, java.lang.Throwable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.<init>(com.android.server.sip.SipSessionGroup$KeepAliveProcessCallback):void, dex: 
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
        KeepAliveProcessCallbackProxy(com.android.server.sip.SipSessionGroup.KeepAliveProcessCallback r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.<init>(com.android.server.sip.SipSessionGroup$KeepAliveProcessCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.<init>(com.android.server.sip.SipSessionGroup$KeepAliveProcessCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.loge(java.lang.String, java.lang.Throwable):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.loge(java.lang.String, java.lang.Throwable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.loge(java.lang.String, java.lang.Throwable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.proxy(java.lang.Runnable):void, dex: 
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
        private void proxy(java.lang.Runnable r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.proxy(java.lang.Runnable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.proxy(java.lang.Runnable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.onError(int, java.lang.String):void, dex: 
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
        public void onError(int r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.onError(int, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.onError(int, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.onResponse(boolean):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.onResponse(boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.KeepAliveProcessCallbackProxy.onResponse(boolean):void");
        }
    }

    private class MakeCallCommand extends EventObject {
        private String mSessionDescription;
        private int mTimeout;
        final /* synthetic */ SipSessionGroup this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.server.sip.SipSessionGroup.MakeCallCommand.<init>(com.android.server.sip.SipSessionGroup, android.net.sip.SipProfile, java.lang.String, int):void, dex:  in method: com.android.server.sip.SipSessionGroup.MakeCallCommand.<init>(com.android.server.sip.SipSessionGroup, android.net.sip.SipProfile, java.lang.String, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.server.sip.SipSessionGroup.MakeCallCommand.<init>(com.android.server.sip.SipSessionGroup, android.net.sip.SipProfile, java.lang.String, int):void, dex: 
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
        public MakeCallCommand(com.android.server.sip.SipSessionGroup r1, android.net.sip.SipProfile r2, java.lang.String r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.server.sip.SipSessionGroup.MakeCallCommand.<init>(com.android.server.sip.SipSessionGroup, android.net.sip.SipProfile, java.lang.String, int):void, dex:  in method: com.android.server.sip.SipSessionGroup.MakeCallCommand.<init>(com.android.server.sip.SipSessionGroup, android.net.sip.SipProfile, java.lang.String, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.MakeCallCommand.<init>(com.android.server.sip.SipSessionGroup, android.net.sip.SipProfile, java.lang.String, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.MakeCallCommand.getPeerProfile():android.net.sip.SipProfile, dex: 
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
        public android.net.sip.SipProfile getPeerProfile() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.MakeCallCommand.getPeerProfile():android.net.sip.SipProfile, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.MakeCallCommand.getPeerProfile():android.net.sip.SipProfile");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.MakeCallCommand.getSessionDescription():java.lang.String, dex: 
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
        public java.lang.String getSessionDescription() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.MakeCallCommand.getSessionDescription():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.MakeCallCommand.getSessionDescription():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.server.sip.SipSessionGroup.MakeCallCommand.getTimeout():int, dex:  in method: com.android.server.sip.SipSessionGroup.MakeCallCommand.getTimeout():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.server.sip.SipSessionGroup.MakeCallCommand.getTimeout():int, dex: 
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
        public int getTimeout() {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.server.sip.SipSessionGroup.MakeCallCommand.getTimeout():int, dex:  in method: com.android.server.sip.SipSessionGroup.MakeCallCommand.getTimeout():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.MakeCallCommand.getTimeout():int");
        }
    }

    private class RegisterCommand extends EventObject {
        private int mDuration;
        final /* synthetic */ SipSessionGroup this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.RegisterCommand.<init>(com.android.server.sip.SipSessionGroup, int):void, dex: 
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
        public RegisterCommand(com.android.server.sip.SipSessionGroup r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.RegisterCommand.<init>(com.android.server.sip.SipSessionGroup, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.RegisterCommand.<init>(com.android.server.sip.SipSessionGroup, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipSessionGroup.RegisterCommand.getDuration():int, dex: 
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
        public int getDuration() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipSessionGroup.RegisterCommand.getDuration():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.RegisterCommand.getDuration():int");
        }
    }

    class SipSessionImpl extends Stub {
        private static final boolean SSI_DBG = true;
        private static final String SSI_TAG = "SipSessionImpl";
        int mAuthenticationRetryCount;
        ClientTransaction mClientTransaction;
        Dialog mDialog;
        boolean mInCall;
        RequestEvent mInviteReceived;
        SipProfile mPeerProfile;
        String mPeerSessionDescription;
        SipSessionListenerProxy mProxy;
        SipSessionImpl mReferSession;
        ReferredByHeader mReferredBy;
        String mReplaces;
        ServerTransaction mServerTransaction;
        SessionTimer mSessionTimer;
        private SipKeepAlive mSipKeepAlive;
        private SipSessionImpl mSipSessionImpl;
        int mState;
        final /* synthetic */ SipSessionGroup this$0;

        /* renamed from: com.android.server.sip.SipSessionGroup$SipSessionImpl$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ SipSessionImpl this$1;
            final /* synthetic */ EventObject val$command;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.1.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl, java.util.EventObject):void, dex: 
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
            AnonymousClass1(com.android.server.sip.SipSessionGroup.SipSessionImpl r1, java.util.EventObject r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.1.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl, java.util.EventObject):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.1.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl, java.util.EventObject):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.1.run():void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.1.run():void");
            }
        }

        /* renamed from: com.android.server.sip.SipSessionGroup$SipSessionImpl$2 */
        class AnonymousClass2 implements AccountManager {
            final /* synthetic */ SipSessionImpl this$1;

            /* renamed from: com.android.server.sip.SipSessionGroup$SipSessionImpl$2$1 */
            class AnonymousClass1 implements UserCredentials {
                final /* synthetic */ AnonymousClass2 this$2;

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.2.1.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl$2):void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 8 more
                    */
                AnonymousClass1(com.android.server.sip.SipSessionGroup.SipSessionImpl.AnonymousClass2 r1) {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.2.1.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl$2):void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.2.1.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl$2):void");
                }

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.2.1.getPassword():java.lang.String, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 8 more
                    */
                public java.lang.String getPassword() {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.2.1.getPassword():java.lang.String, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.2.1.getPassword():java.lang.String");
                }

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.2.1.getSipDomain():java.lang.String, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 8 more
                    */
                public java.lang.String getSipDomain() {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.2.1.getSipDomain():java.lang.String, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.2.1.getSipDomain():java.lang.String");
                }

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.2.1.getUserName():java.lang.String, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 8 more
                    */
                public java.lang.String getUserName() {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.2.1.getUserName():java.lang.String, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.2.1.getUserName():java.lang.String");
                }
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.2.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
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
            AnonymousClass2(com.android.server.sip.SipSessionGroup.SipSessionImpl r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.2.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.2.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl):void");
            }

            public UserCredentials getCredentials(ClientTransaction challengedTransaction, String realm) {
                return new AnonymousClass1(this);
            }
        }

        class SessionTimer {
            private boolean mRunning;
            final /* synthetic */ SipSessionImpl this$1;

            /* renamed from: com.android.server.sip.SipSessionGroup$SipSessionImpl$SessionTimer$1 */
            class AnonymousClass1 implements Runnable {
                final /* synthetic */ SessionTimer this$2;
                final /* synthetic */ int val$timeout;

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.1.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl$SessionTimer, int):void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 8 more
                    */
                AnonymousClass1(com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer r1, int r2) {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.1.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl$SessionTimer, int):void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.1.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl$SessionTimer, int):void");
                }

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.1.run():void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 8 more
                    */
                public void run() {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.1.run():void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.1.run():void");
                }
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.-get0(com.android.server.sip.SipSessionGroup$SipSessionImpl$SessionTimer):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            /* renamed from: -get0 */
            static /* synthetic */ boolean m43-get0(com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.-get0(com.android.server.sip.SipSessionGroup$SipSessionImpl$SessionTimer):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.-get0(com.android.server.sip.SipSessionGroup$SipSessionImpl$SessionTimer):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.-wrap0(com.android.server.sip.SipSessionGroup$SipSessionImpl$SessionTimer, int):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            /* renamed from: -wrap0 */
            static /* synthetic */ void m44-wrap0(com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer r1, int r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.-wrap0(com.android.server.sip.SipSessionGroup$SipSessionImpl$SessionTimer, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.-wrap0(com.android.server.sip.SipSessionGroup$SipSessionImpl$SessionTimer, int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.-wrap1(com.android.server.sip.SipSessionGroup$SipSessionImpl$SessionTimer):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            /* renamed from: -wrap1 */
            static /* synthetic */ void m45-wrap1(com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.-wrap1(com.android.server.sip.SipSessionGroup$SipSessionImpl$SessionTimer):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.-wrap1(com.android.server.sip.SipSessionGroup$SipSessionImpl$SessionTimer):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
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
            SessionTimer(com.android.server.sip.SipSessionGroup.SipSessionImpl r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.sleep(int):void, dex: 
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
            private synchronized void sleep(int r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.sleep(int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.sleep(int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.timeout():void, dex: 
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
            private void timeout() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.timeout():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.timeout():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.cancel():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            synchronized void cancel() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.cancel():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.cancel():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.start(int):void, dex: 
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
            void start(int r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.start(int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SessionTimer.start(int):void");
            }
        }

        class SipKeepAlive extends SipSessionAdapter implements Runnable {
            private static final boolean SKA_DBG = true;
            private static final String SKA_TAG = "SipKeepAlive";
            private KeepAliveProcessCallback mCallback;
            private int mInterval;
            private boolean mPortChanged;
            private int mRPort;
            private boolean mRunning;
            final /* synthetic */ SipSessionImpl this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex:  in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
                	... 7 more
                Caused by: java.io.EOFException
                	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
                	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
                	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
                	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
                	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
                	... 8 more
                */
            SipKeepAlive(com.android.server.sip.SipSessionGroup.SipSessionImpl r1) {
                /*
                // Can't load method instructions: Load method exception: null in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex:  in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.<init>(com.android.server.sip.SipSessionGroup$SipSessionImpl):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.log(java.lang.String):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            private void log(java.lang.String r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.log(java.lang.String):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.log(java.lang.String):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.parseOptionsResult(java.util.EventObject):boolean, dex: 
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
            private boolean parseOptionsResult(java.util.EventObject r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.parseOptionsResult(java.util.EventObject):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.parseOptionsResult(java.util.EventObject):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.sendKeepAlive():void, dex: 
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
            private void sendKeepAlive() throws javax.sip.SipException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.sendKeepAlive():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.sendKeepAlive():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.onError(android.net.sip.ISipSession, int, java.lang.String):void, dex: 
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
            public void onError(android.net.sip.ISipSession r1, int r2, java.lang.String r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.onError(android.net.sip.ISipSession, int, java.lang.String):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.onError(android.net.sip.ISipSession, int, java.lang.String):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.process(java.util.EventObject):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            boolean process(java.util.EventObject r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.process(java.util.EventObject):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.process(java.util.EventObject):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.run():void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.run():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.start(int, com.android.server.sip.SipSessionGroup$KeepAliveProcessCallback):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            void start(int r1, com.android.server.sip.SipSessionGroup.KeepAliveProcessCallback r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.start(int, com.android.server.sip.SipSessionGroup$KeepAliveProcessCallback):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.start(int, com.android.server.sip.SipSessionGroup$KeepAliveProcessCallback):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.stop():void, dex: 
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
            void stop() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.stop():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.SipKeepAlive.stop():void");
            }

            private int getRPortFromResponse(Response response) {
                ViaHeader viaHeader = (ViaHeader) response.getHeader("Via");
                return viaHeader == null ? -1 : viaHeader.getRPort();
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap0(com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
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
        /* renamed from: -wrap0 */
        static /* synthetic */ void m37-wrap0(com.android.server.sip.SipSessionGroup.SipSessionImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap0(com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap0(com.android.server.sip.SipSessionGroup$SipSessionImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap1(com.android.server.sip.SipSessionGroup$SipSessionImpl, int, java.lang.String):void, dex: 
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
        /* renamed from: -wrap1 */
        static /* synthetic */ void m38-wrap1(com.android.server.sip.SipSessionGroup.SipSessionImpl r1, int r2, java.lang.String r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap1(com.android.server.sip.SipSessionGroup$SipSessionImpl, int, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap1(com.android.server.sip.SipSessionGroup$SipSessionImpl, int, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap2(com.android.server.sip.SipSessionGroup$SipSessionImpl, java.lang.Throwable):void, dex: 
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
        /* renamed from: -wrap2 */
        static /* synthetic */ void m39-wrap2(com.android.server.sip.SipSessionGroup.SipSessionImpl r1, java.lang.Throwable r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap2(com.android.server.sip.SipSessionGroup$SipSessionImpl, java.lang.Throwable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap2(com.android.server.sip.SipSessionGroup$SipSessionImpl, java.lang.Throwable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap3(com.android.server.sip.SipSessionGroup$SipSessionImpl, java.util.EventObject):void, dex: 
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
        /* renamed from: -wrap3 */
        static /* synthetic */ void m40-wrap3(com.android.server.sip.SipSessionGroup.SipSessionImpl r1, java.util.EventObject r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap3(com.android.server.sip.SipSessionGroup$SipSessionImpl, java.util.EventObject):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap3(com.android.server.sip.SipSessionGroup$SipSessionImpl, java.util.EventObject):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap4(com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
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
        /* renamed from: -wrap4 */
        static /* synthetic */ void m41-wrap4(com.android.server.sip.SipSessionGroup.SipSessionImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap4(com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap4(com.android.server.sip.SipSessionGroup$SipSessionImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap5(com.android.server.sip.SipSessionGroup$SipSessionImpl, int):void, dex: 
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
        /* renamed from: -wrap5 */
        static /* synthetic */ void m42-wrap5(com.android.server.sip.SipSessionGroup.SipSessionImpl r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap5(com.android.server.sip.SipSessionGroup$SipSessionImpl, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.-wrap5(com.android.server.sip.SipSessionGroup$SipSessionImpl, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.<init>(com.android.server.sip.SipSessionGroup, android.net.sip.ISipSessionListener):void, dex: 
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
        public SipSessionImpl(com.android.server.sip.SipSessionGroup r1, android.net.sip.ISipSessionListener r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.<init>(com.android.server.sip.SipSessionGroup, android.net.sip.ISipSessionListener):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.<init>(com.android.server.sip.SipSessionGroup, android.net.sip.ISipSessionListener):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.cancelSessionTimer():void, dex: 
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
        private void cancelSessionTimer() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.cancelSessionTimer():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.cancelSessionTimer():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.crossDomainAuthenticationRequired(javax.sip.message.Response):boolean, dex: 
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
        private boolean crossDomainAuthenticationRequired(javax.sip.message.Response r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.crossDomainAuthenticationRequired(javax.sip.message.Response):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.crossDomainAuthenticationRequired(javax.sip.message.Response):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.doCommandAsync(java.util.EventObject):void, dex: 
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
        private void doCommandAsync(java.util.EventObject r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.doCommandAsync(java.util.EventObject):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.doCommandAsync(java.util.EventObject):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.enableKeepAlive():void, dex: 
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
        private void enableKeepAlive() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.enableKeepAlive():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.enableKeepAlive():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.endCallNormally():void, dex: 
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
        private void endCallNormally() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.endCallNormally():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.endCallNormally():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.endCallOnBusy():void, dex: 
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
        private void endCallOnBusy() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.endCallOnBusy():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.endCallOnBusy():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.endCallOnError(int, java.lang.String):void, dex: 
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
        private void endCallOnError(int r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.endCallOnError(int, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.endCallOnError(int, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.endingCall(java.util.EventObject):boolean, dex: 
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
        private boolean endingCall(java.util.EventObject r1) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.endingCall(java.util.EventObject):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.endingCall(java.util.EventObject):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.establishCall(boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private void establishCall(boolean r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.establishCall(boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.establishCall(boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getErrorCode(java.lang.Throwable):int, dex: 
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
        private int getErrorCode(java.lang.Throwable r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getErrorCode(java.lang.Throwable):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.getErrorCode(java.lang.Throwable):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getExpiryTime(javax.sip.message.Response):int, dex: 
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
        private int getExpiryTime(javax.sip.message.Response r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getExpiryTime(javax.sip.message.Response):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.getExpiryTime(javax.sip.message.Response):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getNonceFromResponse(javax.sip.message.Response):java.lang.String, dex: 
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
        private java.lang.String getNonceFromResponse(javax.sip.message.Response r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getNonceFromResponse(javax.sip.message.Response):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.getNonceFromResponse(javax.sip.message.Response):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getRealmFromResponse(javax.sip.message.Response):java.lang.String, dex: 
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
        private java.lang.String getRealmFromResponse(javax.sip.message.Response r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getRealmFromResponse(javax.sip.message.Response):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.getRealmFromResponse(javax.sip.message.Response):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getResponseString(int):java.lang.String, dex: 
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
        private java.lang.String getResponseString(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getResponseString(int):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.getResponseString(int):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getTransaction():javax.sip.Transaction, dex: 
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
        private javax.sip.Transaction getTransaction() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getTransaction():javax.sip.Transaction, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.getTransaction():javax.sip.Transaction");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.handleAuthentication(javax.sip.ResponseEvent):boolean, dex: 
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
        private boolean handleAuthentication(javax.sip.ResponseEvent r1) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.handleAuthentication(javax.sip.ResponseEvent):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.handleAuthentication(javax.sip.ResponseEvent):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.inCall(java.util.EventObject):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private boolean inCall(java.util.EventObject r1) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.inCall(java.util.EventObject):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.inCall(java.util.EventObject):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.incomingCall(java.util.EventObject):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private boolean incomingCall(java.util.EventObject r1) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.incomingCall(java.util.EventObject):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.incomingCall(java.util.EventObject):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.incomingCallToInCall(java.util.EventObject):boolean, dex: 
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
        private boolean incomingCallToInCall(java.util.EventObject r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.incomingCallToInCall(java.util.EventObject):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.incomingCallToInCall(java.util.EventObject):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.isCurrentTransaction(javax.sip.TransactionTerminatedEvent):boolean, dex: 
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
        private boolean isCurrentTransaction(javax.sip.TransactionTerminatedEvent r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.isCurrentTransaction(javax.sip.TransactionTerminatedEvent):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.isCurrentTransaction(javax.sip.TransactionTerminatedEvent):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.log(java.lang.String):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.log(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.log(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.onError(int, java.lang.String):void, dex: 
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
        private void onError(int r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.onError(int, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.onError(int, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.onError(java.lang.Throwable):void, dex: 
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
        private void onError(java.lang.Throwable r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.onError(java.lang.Throwable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.onError(java.lang.Throwable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.onError(javax.sip.message.Response):void, dex: 
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
        private void onError(javax.sip.message.Response r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.onError(javax.sip.message.Response):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.onError(javax.sip.message.Response):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.onRegistrationDone(int):void, dex: 
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
        private void onRegistrationDone(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.onRegistrationDone(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.onRegistrationDone(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.onRegistrationFailed(int, java.lang.String):void, dex: 
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
        private void onRegistrationFailed(int r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.onRegistrationFailed(int, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.onRegistrationFailed(int, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.onRegistrationFailed(javax.sip.message.Response):void, dex: 
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
        private void onRegistrationFailed(javax.sip.message.Response r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.onRegistrationFailed(javax.sip.message.Response):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.onRegistrationFailed(javax.sip.message.Response):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.outgoingCall(java.util.EventObject):boolean, dex: 
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
        private boolean outgoingCall(java.util.EventObject r1) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.outgoingCall(java.util.EventObject):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.outgoingCall(java.util.EventObject):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.outgoingCallToReady(java.util.EventObject):boolean, dex: 
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
        private boolean outgoingCallToReady(java.util.EventObject r1) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.outgoingCallToReady(java.util.EventObject):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.outgoingCallToReady(java.util.EventObject):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.processCommand(java.util.EventObject):void, dex: 
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
        private void processCommand(java.util.EventObject r1) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.processCommand(java.util.EventObject):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.processCommand(java.util.EventObject):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.processDialogTerminated(javax.sip.DialogTerminatedEvent):void, dex: 
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
        private void processDialogTerminated(javax.sip.DialogTerminatedEvent r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.processDialogTerminated(javax.sip.DialogTerminatedEvent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.processDialogTerminated(javax.sip.DialogTerminatedEvent):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.processExceptions(java.util.EventObject):boolean, dex: 
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
        private boolean processExceptions(java.util.EventObject r1) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.processExceptions(java.util.EventObject):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.processExceptions(java.util.EventObject):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.processReferRequest(javax.sip.RequestEvent):boolean, dex: 
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
        private boolean processReferRequest(javax.sip.RequestEvent r1) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.processReferRequest(javax.sip.RequestEvent):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.processReferRequest(javax.sip.RequestEvent):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.processTimeout(javax.sip.TimeoutEvent):void, dex: 
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
        private void processTimeout(javax.sip.TimeoutEvent r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.processTimeout(javax.sip.TimeoutEvent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.processTimeout(javax.sip.TimeoutEvent):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.processTransactionTerminated(javax.sip.TransactionTerminatedEvent):void, dex: 
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
        private void processTransactionTerminated(javax.sip.TransactionTerminatedEvent r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.processTransactionTerminated(javax.sip.TransactionTerminatedEvent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.processTransactionTerminated(javax.sip.TransactionTerminatedEvent):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.readyForCall(java.util.EventObject):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private boolean readyForCall(java.util.EventObject r1) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.readyForCall(java.util.EventObject):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.readyForCall(java.util.EventObject):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.registeringToReady(java.util.EventObject):boolean, dex: 
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
        private boolean registeringToReady(java.util.EventObject r1) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.registeringToReady(java.util.EventObject):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.registeringToReady(java.util.EventObject):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.reset():void, dex: 
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
        private void reset() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.reset():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.reset():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.startSessionTimer(int):void, dex: 
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
        private void startSessionTimer(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.startSessionTimer(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.startSessionTimer(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.answerCall(java.lang.String, int):void, dex: 
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
        public void answerCall(java.lang.String r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.answerCall(java.lang.String, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.answerCall(java.lang.String, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.changeCall(java.lang.String, int):void, dex: 
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
        public void changeCall(java.lang.String r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.changeCall(java.lang.String, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.changeCall(java.lang.String, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.duplicate():com.android.server.sip.SipSessionGroup$SipSessionImpl, dex: 
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
        com.android.server.sip.SipSessionGroup.SipSessionImpl duplicate() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.duplicate():com.android.server.sip.SipSessionGroup$SipSessionImpl, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.duplicate():com.android.server.sip.SipSessionGroup$SipSessionImpl");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.endCall():void, dex: 
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
        public void endCall() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.endCall():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.endCall():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getLocalIp():java.lang.String, dex: 
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
        public java.lang.String getLocalIp() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getLocalIp():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.getLocalIp():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getLocalProfile():android.net.sip.SipProfile, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getLocalProfile():android.net.sip.SipProfile, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.getLocalProfile():android.net.sip.SipProfile");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getPeerProfile():android.net.sip.SipProfile, dex:  in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getPeerProfile():android.net.sip.SipProfile, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getPeerProfile():android.net.sip.SipProfile, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public android.net.sip.SipProfile getPeerProfile() {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getPeerProfile():android.net.sip.SipProfile, dex:  in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getPeerProfile():android.net.sip.SipProfile, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.getPeerProfile():android.net.sip.SipProfile");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getState():int, dex: 
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
        public int getState() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.getState():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.getState():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.isInCall():boolean, dex: 
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
        public boolean isInCall() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.isInCall():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.isInCall():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.makeCall(android.net.sip.SipProfile, java.lang.String, int):void, dex: 
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
        public void makeCall(android.net.sip.SipProfile r1, java.lang.String r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.makeCall(android.net.sip.SipProfile, java.lang.String, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.makeCall(android.net.sip.SipProfile, java.lang.String, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.process(java.util.EventObject):boolean, dex: 
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
        public boolean process(java.util.EventObject r1) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.process(java.util.EventObject):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.process(java.util.EventObject):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.register(int):void, dex: 
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
        public void register(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.register(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.register(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.setListener(android.net.sip.ISipSessionListener):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.setListener(android.net.sip.ISipSessionListener):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.setListener(android.net.sip.ISipSessionListener):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.startKeepAliveProcess(int, android.net.sip.SipProfile, com.android.server.sip.SipSessionGroup$KeepAliveProcessCallback):void, dex: 
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
        public void startKeepAliveProcess(int r1, android.net.sip.SipProfile r2, com.android.server.sip.SipSessionGroup.KeepAliveProcessCallback r3) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.startKeepAliveProcess(int, android.net.sip.SipProfile, com.android.server.sip.SipSessionGroup$KeepAliveProcessCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.startKeepAliveProcess(int, android.net.sip.SipProfile, com.android.server.sip.SipSessionGroup$KeepAliveProcessCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.startKeepAliveProcess(int, com.android.server.sip.SipSessionGroup$KeepAliveProcessCallback):void, dex: 
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
        public void startKeepAliveProcess(int r1, com.android.server.sip.SipSessionGroup.KeepAliveProcessCallback r2) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.startKeepAliveProcess(int, com.android.server.sip.SipSessionGroup$KeepAliveProcessCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.startKeepAliveProcess(int, com.android.server.sip.SipSessionGroup$KeepAliveProcessCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.stopKeepAliveProcess():void, dex: 
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
        public void stopKeepAliveProcess() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.stopKeepAliveProcess():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.stopKeepAliveProcess():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.toString():java.lang.String, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.toString():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.unregister():void, dex: 
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
        public void unregister() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionImpl.unregister():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionImpl.unregister():void");
        }

        public String getCallId() {
            return SipHelper.getCallId(getTransaction());
        }

        protected String generateTag() {
            return String.valueOf((long) (Math.random() * 4.294967296E9d));
        }

        private String toString(Transaction transaction) {
            if (transaction == null) {
                return "null";
            }
            Request request = transaction.getRequest();
            Dialog dialog = transaction.getDialog();
            CSeqHeader cseq = (CSeqHeader) request.getHeader("CSeq");
            String str = "req=%s,%s,s=%s,ds=%s,";
            Object[] objArr = new Object[4];
            objArr[0] = request.getMethod();
            objArr[1] = Long.valueOf(cseq.getSeqNumber());
            objArr[2] = transaction.getState();
            objArr[3] = dialog == null ? "-" : dialog.getState();
            return String.format(str, objArr);
        }

        private AccountManager getAccountManager() {
            return new AnonymousClass2(this);
        }

        private String createErrorMessage(Response response) {
            Object[] objArr = new Object[2];
            objArr[0] = response.getReasonPhrase();
            objArr[1] = Integer.valueOf(response.getStatusCode());
            return String.format("%s (%d)", objArr);
        }

        private int getErrorCode(int responseStatusCode) {
            switch (responseStatusCode) {
                case 403:
                case 404:
                case 406:
                case 410:
                case 480:
                case 488:
                    return -7;
                case 408:
                    return -5;
                case 414:
                case 484:
                case 485:
                    return -6;
                default:
                    if (responseStatusCode < 500) {
                        return -4;
                    }
                    return -2;
            }
        }
    }

    private class SipSessionCallReceiverImpl extends SipSessionImpl {
        private static final boolean SSCRI_DBG = true;
        private static final String SSCRI_TAG = "SipSessionCallReceiverImpl";
        final /* synthetic */ SipSessionGroup this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.SipSessionCallReceiverImpl.<init>(com.android.server.sip.SipSessionGroup, android.net.sip.ISipSessionListener):void, dex: 
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
        public SipSessionCallReceiverImpl(com.android.server.sip.SipSessionGroup r1, android.net.sip.ISipSessionListener r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.SipSessionCallReceiverImpl.<init>(com.android.server.sip.SipSessionGroup, android.net.sip.ISipSessionListener):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionCallReceiverImpl.<init>(com.android.server.sip.SipSessionGroup, android.net.sip.ISipSessionListener):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionCallReceiverImpl.log(java.lang.String):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.SipSessionCallReceiverImpl.log(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionCallReceiverImpl.log(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionCallReceiverImpl.processInviteWithReplaces(javax.sip.RequestEvent, gov.nist.javax.sip.header.extensions.ReplacesHeader):int, dex: 
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
        private int processInviteWithReplaces(javax.sip.RequestEvent r1, gov.nist.javax.sip.header.extensions.ReplacesHeader r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.SipSessionCallReceiverImpl.processInviteWithReplaces(javax.sip.RequestEvent, gov.nist.javax.sip.header.extensions.ReplacesHeader):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionCallReceiverImpl.processInviteWithReplaces(javax.sip.RequestEvent, gov.nist.javax.sip.header.extensions.ReplacesHeader):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionCallReceiverImpl.processNewInviteRequest(javax.sip.RequestEvent):void, dex: 
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
        private void processNewInviteRequest(javax.sip.RequestEvent r1) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionCallReceiverImpl.processNewInviteRequest(javax.sip.RequestEvent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionCallReceiverImpl.processNewInviteRequest(javax.sip.RequestEvent):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionCallReceiverImpl.process(java.util.EventObject):boolean, dex: 
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
        public boolean process(java.util.EventObject r1) throws javax.sip.SipException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.SipSessionCallReceiverImpl.process(java.util.EventObject):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.SipSessionCallReceiverImpl.process(java.util.EventObject):boolean");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.-get2(com.android.server.sip.SipSessionGroup):java.lang.String, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    /* renamed from: -get2 */
    static /* synthetic */ java.lang.String m23-get2(com.android.server.sip.SipSessionGroup r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.-get2(com.android.server.sip.SipSessionGroup):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.-get2(com.android.server.sip.SipSessionGroup):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipSessionGroup.-get3(com.android.server.sip.SipSessionGroup):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    /* renamed from: -get3 */
    static /* synthetic */ int m24-get3(com.android.server.sip.SipSessionGroup r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipSessionGroup.-get3(com.android.server.sip.SipSessionGroup):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.-get3(com.android.server.sip.SipSessionGroup):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.-get4(com.android.server.sip.SipSessionGroup):java.lang.String, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    /* renamed from: -get4 */
    static /* synthetic */ java.lang.String m25-get4(com.android.server.sip.SipSessionGroup r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.-get4(com.android.server.sip.SipSessionGroup):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.-get4(com.android.server.sip.SipSessionGroup):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.server.sip.SipSessionGroup.-get5(com.android.server.sip.SipSessionGroup):android.net.sip.SipProfile, dex:  in method: com.android.server.sip.SipSessionGroup.-get5(com.android.server.sip.SipSessionGroup):android.net.sip.SipProfile, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.server.sip.SipSessionGroup.-get5(com.android.server.sip.SipSessionGroup):android.net.sip.SipProfile, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get5 */
    static /* synthetic */ android.net.sip.SipProfile m26-get5(com.android.server.sip.SipSessionGroup r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.server.sip.SipSessionGroup.-get5(com.android.server.sip.SipSessionGroup):android.net.sip.SipProfile, dex:  in method: com.android.server.sip.SipSessionGroup.-get5(com.android.server.sip.SipSessionGroup):android.net.sip.SipProfile, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.-get5(com.android.server.sip.SipSessionGroup):android.net.sip.SipProfile");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.server.sip.SipSessionGroup.-get6(com.android.server.sip.SipSessionGroup):java.lang.String, dex:  in method: com.android.server.sip.SipSessionGroup.-get6(com.android.server.sip.SipSessionGroup):java.lang.String, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.server.sip.SipSessionGroup.-get6(com.android.server.sip.SipSessionGroup):java.lang.String, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get6 */
    static /* synthetic */ java.lang.String m27-get6(com.android.server.sip.SipSessionGroup r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.server.sip.SipSessionGroup.-get6(com.android.server.sip.SipSessionGroup):java.lang.String, dex:  in method: com.android.server.sip.SipSessionGroup.-get6(com.android.server.sip.SipSessionGroup):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.-get6(com.android.server.sip.SipSessionGroup):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.-get7(com.android.server.sip.SipSessionGroup):java.util.Map, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    /* renamed from: -get7 */
    static /* synthetic */ java.util.Map m28-get7(com.android.server.sip.SipSessionGroup r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.-get7(com.android.server.sip.SipSessionGroup):java.util.Map, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.-get7(com.android.server.sip.SipSessionGroup):java.util.Map");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.-get8(com.android.server.sip.SipSessionGroup):com.android.server.sip.SipHelper, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    /* renamed from: -get8 */
    static /* synthetic */ com.android.server.sip.SipHelper m29-get8(com.android.server.sip.SipSessionGroup r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.-get8(com.android.server.sip.SipSessionGroup):com.android.server.sip.SipHelper, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.-get8(com.android.server.sip.SipSessionGroup):com.android.server.sip.SipHelper");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.server.sip.SipSessionGroup.-get9(com.android.server.sip.SipSessionGroup):com.android.server.sip.SipWakeupTimer, dex:  in method: com.android.server.sip.SipSessionGroup.-get9(com.android.server.sip.SipSessionGroup):com.android.server.sip.SipWakeupTimer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.server.sip.SipSessionGroup.-get9(com.android.server.sip.SipSessionGroup):com.android.server.sip.SipWakeupTimer, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$23.decode(InstructionCodec.java:514)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get9 */
    static /* synthetic */ com.android.server.sip.SipWakeupTimer m30-get9(com.android.server.sip.SipSessionGroup r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.server.sip.SipSessionGroup.-get9(com.android.server.sip.SipSessionGroup):com.android.server.sip.SipWakeupTimer, dex:  in method: com.android.server.sip.SipSessionGroup.-get9(com.android.server.sip.SipSessionGroup):com.android.server.sip.SipWakeupTimer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.-get9(com.android.server.sip.SipSessionGroup):com.android.server.sip.SipWakeupTimer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.-wrap10(com.android.server.sip.SipSessionGroup, javax.sip.ResponseEvent):void, dex: 
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
    /* renamed from: -wrap10 */
    static /* synthetic */ void m31-wrap10(com.android.server.sip.SipSessionGroup r1, javax.sip.ResponseEvent r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.-wrap10(com.android.server.sip.SipSessionGroup, javax.sip.ResponseEvent):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.-wrap10(com.android.server.sip.SipSessionGroup, javax.sip.ResponseEvent):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.-wrap11(com.android.server.sip.SipSessionGroup, java.lang.String, java.lang.Throwable):void, dex: 
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
    /* renamed from: -wrap11 */
    static /* synthetic */ void m32-wrap11(com.android.server.sip.SipSessionGroup r1, java.lang.String r2, java.lang.Throwable r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.-wrap11(com.android.server.sip.SipSessionGroup, java.lang.String, java.lang.Throwable):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.-wrap11(com.android.server.sip.SipSessionGroup, java.lang.String, java.lang.Throwable):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.-wrap12(com.android.server.sip.SipSessionGroup, com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
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
    /* renamed from: -wrap12 */
    static /* synthetic */ void m33-wrap12(com.android.server.sip.SipSessionGroup r1, com.android.server.sip.SipSessionGroup.SipSessionImpl r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.-wrap12(com.android.server.sip.SipSessionGroup, com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.-wrap12(com.android.server.sip.SipSessionGroup, com.android.server.sip.SipSessionGroup$SipSessionImpl):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.-wrap9(com.android.server.sip.SipSessionGroup, com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
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
    /* renamed from: -wrap9 */
    static /* synthetic */ void m34-wrap9(com.android.server.sip.SipSessionGroup r1, com.android.server.sip.SipSessionGroup.SipSessionImpl r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.-wrap9(com.android.server.sip.SipSessionGroup, com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.-wrap9(com.android.server.sip.SipSessionGroup, com.android.server.sip.SipSessionGroup$SipSessionImpl):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.<init>(android.net.sip.SipProfile, java.lang.String, com.android.server.sip.SipWakeupTimer, com.android.server.sip.SipWakeLock):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public SipSessionGroup(android.net.sip.SipProfile r1, java.lang.String r2, com.android.server.sip.SipWakeupTimer r3, com.android.server.sip.SipWakeLock r4) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.<init>(android.net.sip.SipProfile, java.lang.String, com.android.server.sip.SipWakeupTimer, com.android.server.sip.SipWakeLock):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.<init>(android.net.sip.SipProfile, java.lang.String, com.android.server.sip.SipWakeupTimer, com.android.server.sip.SipWakeLock):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.addSipSession(com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
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
    private synchronized void addSipSession(com.android.server.sip.SipSessionGroup.SipSessionImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.addSipSession(com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.addSipSession(com.android.server.sip.SipSessionGroup$SipSessionImpl):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.createNewSession(javax.sip.RequestEvent, android.net.sip.ISipSessionListener, javax.sip.ServerTransaction, int):com.android.server.sip.SipSessionGroup$SipSessionImpl, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private com.android.server.sip.SipSessionGroup.SipSessionImpl createNewSession(javax.sip.RequestEvent r1, android.net.sip.ISipSessionListener r2, javax.sip.ServerTransaction r3, int r4) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.createNewSession(javax.sip.RequestEvent, android.net.sip.ISipSessionListener, javax.sip.ServerTransaction, int):com.android.server.sip.SipSessionGroup$SipSessionImpl, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.createNewSession(javax.sip.RequestEvent, android.net.sip.ISipSessionListener, javax.sip.ServerTransaction, int):com.android.server.sip.SipSessionGroup$SipSessionImpl");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.createPeerProfile(javax.sip.header.HeaderAddress):android.net.sip.SipProfile, dex: 
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
    private static android.net.sip.SipProfile createPeerProfile(javax.sip.header.HeaderAddress r1) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.createPeerProfile(javax.sip.header.HeaderAddress):android.net.sip.SipProfile, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.createPeerProfile(javax.sip.header.HeaderAddress):android.net.sip.SipProfile");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.expectResponse(java.lang.String, java.util.EventObject):boolean, dex: 
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
    private static boolean expectResponse(java.lang.String r1, java.util.EventObject r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.expectResponse(java.lang.String, java.util.EventObject):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.expectResponse(java.lang.String, java.util.EventObject):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.extractContent(javax.sip.message.Message):java.lang.String, dex: 
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
    private java.lang.String extractContent(javax.sip.message.Message r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.extractContent(javax.sip.message.Message):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.extractContent(javax.sip.message.Message):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.extractExternalAddress(javax.sip.ResponseEvent):void, dex: 
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
    private void extractExternalAddress(javax.sip.ResponseEvent r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.extractExternalAddress(javax.sip.ResponseEvent):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.extractExternalAddress(javax.sip.ResponseEvent):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.getRootCause(java.lang.Throwable):java.lang.Throwable, dex: 
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
    private java.lang.Throwable getRootCause(java.lang.Throwable r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.getRootCause(java.lang.Throwable):java.lang.Throwable, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.getRootCause(java.lang.Throwable):java.lang.Throwable");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.getSipSession(java.util.EventObject):com.android.server.sip.SipSessionGroup$SipSessionImpl, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private synchronized com.android.server.sip.SipSessionGroup.SipSessionImpl getSipSession(java.util.EventObject r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.getSipSession(java.util.EventObject):com.android.server.sip.SipSessionGroup$SipSessionImpl, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.getSipSession(java.util.EventObject):com.android.server.sip.SipSessionGroup$SipSessionImpl");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.getStackName():java.lang.String, dex: 
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
    private java.lang.String getStackName() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.getStackName():java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.getStackName():java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipSessionGroup.isLoggable(com.android.server.sip.SipSessionGroup$SipSessionImpl):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private static boolean isLoggable(com.android.server.sip.SipSessionGroup.SipSessionImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.server.sip.SipSessionGroup.isLoggable(com.android.server.sip.SipSessionGroup$SipSessionImpl):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.isLoggable(com.android.server.sip.SipSessionGroup$SipSessionImpl):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.isLoggable(com.android.server.sip.SipSessionGroup$SipSessionImpl, java.util.EventObject):boolean, dex: 
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
    private static boolean isLoggable(com.android.server.sip.SipSessionGroup.SipSessionImpl r1, java.util.EventObject r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.isLoggable(com.android.server.sip.SipSessionGroup$SipSessionImpl, java.util.EventObject):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.isLoggable(com.android.server.sip.SipSessionGroup$SipSessionImpl, java.util.EventObject):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.isRequestEvent(java.lang.String, java.util.EventObject):boolean, dex: 
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
    private static boolean isRequestEvent(java.lang.String r1, java.util.EventObject r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.isRequestEvent(java.lang.String, java.util.EventObject):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.isRequestEvent(java.lang.String, java.util.EventObject):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.log(java.lang.String):void, dex: 
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
    private void log(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.log(java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.log(java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.logEvt(java.util.EventObject):java.lang.String, dex: 
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
    private static java.lang.String logEvt(java.util.EventObject r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.logEvt(java.util.EventObject):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.logEvt(java.util.EventObject):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.loge(java.lang.String, java.lang.Throwable):void, dex: 
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
    private void loge(java.lang.String r1, java.lang.Throwable r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.loge(java.lang.String, java.lang.Throwable):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.loge(java.lang.String, java.lang.Throwable):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.process(java.util.EventObject):void, dex: 
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
    private synchronized void process(java.util.EventObject r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.process(java.util.EventObject):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.process(java.util.EventObject):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.removeSipSession(com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private synchronized void removeSipSession(com.android.server.sip.SipSessionGroup.SipSessionImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.removeSipSession(com.android.server.sip.SipSessionGroup$SipSessionImpl):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.removeSipSession(com.android.server.sip.SipSessionGroup$SipSessionImpl):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.close():void, dex: 
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
    public synchronized void close() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.close():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.close():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.closeToNotReceiveCalls():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public synchronized void closeToNotReceiveCalls() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipSessionGroup.closeToNotReceiveCalls():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.closeToNotReceiveCalls():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.containsSession(java.lang.String):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    synchronized boolean containsSession(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.containsSession(java.lang.String):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.containsSession(java.lang.String):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.createSession(android.net.sip.ISipSessionListener):android.net.sip.ISipSession, dex: 
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
    public android.net.sip.ISipSession createSession(android.net.sip.ISipSessionListener r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.createSession(android.net.sip.ISipSessionListener):android.net.sip.ISipSession, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.createSession(android.net.sip.ISipSessionListener):android.net.sip.ISipSession");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.server.sip.SipSessionGroup.getLocalProfile():android.net.sip.SipProfile, dex:  in method: com.android.server.sip.SipSessionGroup.getLocalProfile():android.net.sip.SipProfile, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.server.sip.SipSessionGroup.getLocalProfile():android.net.sip.SipProfile, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public android.net.sip.SipProfile getLocalProfile() {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.server.sip.SipSessionGroup.getLocalProfile():android.net.sip.SipProfile, dex:  in method: com.android.server.sip.SipSessionGroup.getLocalProfile():android.net.sip.SipProfile, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.getLocalProfile():android.net.sip.SipProfile");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.getLocalProfileUri():java.lang.String, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public java.lang.String getLocalProfileUri() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.getLocalProfileUri():java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.getLocalProfileUri():java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.isClosed():boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public synchronized boolean isClosed() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.isClosed():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.isClosed():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.onConnectivityChanged():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    synchronized void onConnectivityChanged() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.onConnectivityChanged():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.onConnectivityChanged():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.openToReceiveCalls(android.net.sip.ISipSessionListener):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public synchronized void openToReceiveCalls(android.net.sip.ISipSessionListener r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.openToReceiveCalls(android.net.sip.ISipSessionListener):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.openToReceiveCalls(android.net.sip.ISipSessionListener):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.processDialogTerminated(javax.sip.DialogTerminatedEvent):void, dex: 
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
    public void processDialogTerminated(javax.sip.DialogTerminatedEvent r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.processDialogTerminated(javax.sip.DialogTerminatedEvent):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.processDialogTerminated(javax.sip.DialogTerminatedEvent):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.processIOException(javax.sip.IOExceptionEvent):void, dex: 
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
    public void processIOException(javax.sip.IOExceptionEvent r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.processIOException(javax.sip.IOExceptionEvent):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.processIOException(javax.sip.IOExceptionEvent):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.processRequest(javax.sip.RequestEvent):void, dex: 
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
    public void processRequest(javax.sip.RequestEvent r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.processRequest(javax.sip.RequestEvent):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.processRequest(javax.sip.RequestEvent):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.processResponse(javax.sip.ResponseEvent):void, dex: 
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
    public void processResponse(javax.sip.ResponseEvent r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.processResponse(javax.sip.ResponseEvent):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.processResponse(javax.sip.ResponseEvent):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.processTimeout(javax.sip.TimeoutEvent):void, dex: 
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
    public void processTimeout(javax.sip.TimeoutEvent r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.processTimeout(javax.sip.TimeoutEvent):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.processTimeout(javax.sip.TimeoutEvent):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.processTransactionTerminated(javax.sip.TransactionTerminatedEvent):void, dex: 
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
    public void processTransactionTerminated(javax.sip.TransactionTerminatedEvent r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipSessionGroup.processTransactionTerminated(javax.sip.TransactionTerminatedEvent):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.processTransactionTerminated(javax.sip.TransactionTerminatedEvent):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.reset():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    synchronized void reset() throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipSessionGroup.reset():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.reset():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.resetExternalAddress():void, dex: 
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
    synchronized void resetExternalAddress() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipSessionGroup.resetExternalAddress():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.resetExternalAddress():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.server.sip.SipSessionGroup.setWakeupTimer(com.android.server.sip.SipWakeupTimer):void, dex:  in method: com.android.server.sip.SipSessionGroup.setWakeupTimer(com.android.server.sip.SipWakeupTimer):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.server.sip.SipSessionGroup.setWakeupTimer(com.android.server.sip.SipWakeupTimer):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$23.decode(InstructionCodec.java:514)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    void setWakeupTimer(com.android.server.sip.SipWakeupTimer r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.server.sip.SipSessionGroup.setWakeupTimer(com.android.server.sip.SipWakeupTimer):void, dex:  in method: com.android.server.sip.SipSessionGroup.setWakeupTimer(com.android.server.sip.SipWakeupTimer):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipSessionGroup.setWakeupTimer(com.android.server.sip.SipWakeupTimer):void");
    }

    private static String getCseqMethod(Message message) {
        return ((CSeqHeader) message.getHeader("CSeq")).getMethod();
    }

    private static boolean isLoggable(EventObject evt) {
        return isLoggable(null, evt);
    }
}
