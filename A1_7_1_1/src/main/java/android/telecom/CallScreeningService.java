package android.telecom;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.telecom.Call.Details;
import com.android.internal.telecom.ICallScreeningAdapter;
import com.android.internal.telecom.ICallScreeningService.Stub;

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
public abstract class CallScreeningService extends Service {
    private static final int MSG_SCREEN_CALL = 1;
    public static final String SERVICE_INTERFACE = "android.telecom.CallScreeningService";
    private ICallScreeningAdapter mCallScreeningAdapter;
    private final Handler mHandler;

    /* renamed from: android.telecom.CallScreeningService$1 */
    class AnonymousClass1 extends Handler {
        final /* synthetic */ CallScreeningService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telecom.CallScreeningService.1.<init>(android.telecom.CallScreeningService, android.os.Looper):void, dex: 
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
        AnonymousClass1(android.telecom.CallScreeningService r1, android.os.Looper r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telecom.CallScreeningService.1.<init>(android.telecom.CallScreeningService, android.os.Looper):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.1.<init>(android.telecom.CallScreeningService, android.os.Looper):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.telecom.CallScreeningService.1.handleMessage(android.os.Message):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.telecom.CallScreeningService.1.handleMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.1.handleMessage(android.os.Message):void");
        }
    }

    public static class CallResponse {
        private final boolean mShouldDisallowCall;
        private final boolean mShouldRejectCall;
        private final boolean mShouldSkipCallLog;
        private final boolean mShouldSkipNotification;

        public static class Builder {
            private boolean mShouldDisallowCall;
            private boolean mShouldRejectCall;
            private boolean mShouldSkipCallLog;
            private boolean mShouldSkipNotification;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telecom.CallScreeningService.CallResponse.Builder.<init>():void, dex: 
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
            public Builder() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telecom.CallScreeningService.CallResponse.Builder.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.CallResponse.Builder.<init>():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.telecom.CallScreeningService.CallResponse.Builder.build():android.telecom.CallScreeningService$CallResponse, dex: 
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
            public android.telecom.CallScreeningService.CallResponse build() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.telecom.CallScreeningService.CallResponse.Builder.build():android.telecom.CallScreeningService$CallResponse, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.CallResponse.Builder.build():android.telecom.CallScreeningService$CallResponse");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.telecom.CallScreeningService.CallResponse.Builder.setDisallowCall(boolean):android.telecom.CallScreeningService$CallResponse$Builder, dex: 
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
            public android.telecom.CallScreeningService.CallResponse.Builder setDisallowCall(boolean r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.telecom.CallScreeningService.CallResponse.Builder.setDisallowCall(boolean):android.telecom.CallScreeningService$CallResponse$Builder, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.CallResponse.Builder.setDisallowCall(boolean):android.telecom.CallScreeningService$CallResponse$Builder");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.telecom.CallScreeningService.CallResponse.Builder.setRejectCall(boolean):android.telecom.CallScreeningService$CallResponse$Builder, dex:  in method: android.telecom.CallScreeningService.CallResponse.Builder.setRejectCall(boolean):android.telecom.CallScreeningService$CallResponse$Builder, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.telecom.CallScreeningService.CallResponse.Builder.setRejectCall(boolean):android.telecom.CallScreeningService$CallResponse$Builder, dex: 
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
                	... 7 more
                Caused by: java.io.EOFException
                	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
                	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
                	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
                	... 8 more
                */
            public android.telecom.CallScreeningService.CallResponse.Builder setRejectCall(boolean r1) {
                /*
                // Can't load method instructions: Load method exception: null in method: android.telecom.CallScreeningService.CallResponse.Builder.setRejectCall(boolean):android.telecom.CallScreeningService$CallResponse$Builder, dex:  in method: android.telecom.CallScreeningService.CallResponse.Builder.setRejectCall(boolean):android.telecom.CallScreeningService$CallResponse$Builder, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.CallResponse.Builder.setRejectCall(boolean):android.telecom.CallScreeningService$CallResponse$Builder");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.telecom.CallScreeningService.CallResponse.Builder.setSkipCallLog(boolean):android.telecom.CallScreeningService$CallResponse$Builder, dex: 
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
            public android.telecom.CallScreeningService.CallResponse.Builder setSkipCallLog(boolean r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.telecom.CallScreeningService.CallResponse.Builder.setSkipCallLog(boolean):android.telecom.CallScreeningService$CallResponse$Builder, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.CallResponse.Builder.setSkipCallLog(boolean):android.telecom.CallScreeningService$CallResponse$Builder");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.telecom.CallScreeningService.CallResponse.Builder.setSkipNotification(boolean):android.telecom.CallScreeningService$CallResponse$Builder, dex: 
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
            public android.telecom.CallScreeningService.CallResponse.Builder setSkipNotification(boolean r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.telecom.CallScreeningService.CallResponse.Builder.setSkipNotification(boolean):android.telecom.CallScreeningService$CallResponse$Builder, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.CallResponse.Builder.setSkipNotification(boolean):android.telecom.CallScreeningService$CallResponse$Builder");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.telecom.CallScreeningService.CallResponse.<init>(boolean, boolean, boolean, boolean):void, dex: 
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
        private CallResponse(boolean r1, boolean r2, boolean r3, boolean r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.telecom.CallScreeningService.CallResponse.<init>(boolean, boolean, boolean, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.CallResponse.<init>(boolean, boolean, boolean, boolean):void");
        }

        /* synthetic */ CallResponse(boolean shouldDisallowCall, boolean shouldRejectCall, boolean shouldSkipCallLog, boolean shouldSkipNotification, CallResponse callResponse) {
            this(shouldDisallowCall, shouldRejectCall, shouldSkipCallLog, shouldSkipNotification);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.telecom.CallScreeningService.CallResponse.getDisallowCall():boolean, dex: 
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
        public boolean getDisallowCall() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.telecom.CallScreeningService.CallResponse.getDisallowCall():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.CallResponse.getDisallowCall():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.telecom.CallScreeningService.CallResponse.getRejectCall():boolean, dex:  in method: android.telecom.CallScreeningService.CallResponse.getRejectCall():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.telecom.CallScreeningService.CallResponse.getRejectCall():boolean, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public boolean getRejectCall() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.telecom.CallScreeningService.CallResponse.getRejectCall():boolean, dex:  in method: android.telecom.CallScreeningService.CallResponse.getRejectCall():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.CallResponse.getRejectCall():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.telecom.CallScreeningService.CallResponse.getSkipCallLog():boolean, dex: 
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
        public boolean getSkipCallLog() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.telecom.CallScreeningService.CallResponse.getSkipCallLog():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.CallResponse.getSkipCallLog():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.telecom.CallScreeningService.CallResponse.getSkipNotification():boolean, dex: 
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
        public boolean getSkipNotification() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.telecom.CallScreeningService.CallResponse.getSkipNotification():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.CallResponse.getSkipNotification():boolean");
        }
    }

    private final class CallScreeningBinder extends Stub {
        final /* synthetic */ CallScreeningService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telecom.CallScreeningService.CallScreeningBinder.<init>(android.telecom.CallScreeningService):void, dex: 
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
        private CallScreeningBinder(android.telecom.CallScreeningService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telecom.CallScreeningService.CallScreeningBinder.<init>(android.telecom.CallScreeningService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.CallScreeningBinder.<init>(android.telecom.CallScreeningService):void");
        }

        /* synthetic */ CallScreeningBinder(CallScreeningService this$0, CallScreeningBinder callScreeningBinder) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telecom.CallScreeningService.CallScreeningBinder.screenCall(com.android.internal.telecom.ICallScreeningAdapter, android.telecom.ParcelableCall):void, dex: 
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
        public void screenCall(com.android.internal.telecom.ICallScreeningAdapter r1, android.telecom.ParcelableCall r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telecom.CallScreeningService.CallScreeningBinder.screenCall(com.android.internal.telecom.ICallScreeningAdapter, android.telecom.ParcelableCall):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.CallScreeningBinder.screenCall(com.android.internal.telecom.ICallScreeningAdapter, android.telecom.ParcelableCall):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.telecom.CallScreeningService.-get0(android.telecom.CallScreeningService):android.os.Handler, dex:  in method: android.telecom.CallScreeningService.-get0(android.telecom.CallScreeningService):android.os.Handler, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.telecom.CallScreeningService.-get0(android.telecom.CallScreeningService):android.os.Handler, dex: 
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
    /* renamed from: -get0 */
    static /* synthetic */ android.os.Handler m14-get0(android.telecom.CallScreeningService r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.telecom.CallScreeningService.-get0(android.telecom.CallScreeningService):android.os.Handler, dex:  in method: android.telecom.CallScreeningService.-get0(android.telecom.CallScreeningService):android.os.Handler, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.-get0(android.telecom.CallScreeningService):android.os.Handler");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telecom.CallScreeningService.-set0(android.telecom.CallScreeningService, com.android.internal.telecom.ICallScreeningAdapter):com.android.internal.telecom.ICallScreeningAdapter, dex: 
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
    /* renamed from: -set0 */
    static /* synthetic */ com.android.internal.telecom.ICallScreeningAdapter m15-set0(android.telecom.CallScreeningService r1, com.android.internal.telecom.ICallScreeningAdapter r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telecom.CallScreeningService.-set0(android.telecom.CallScreeningService, com.android.internal.telecom.ICallScreeningAdapter):com.android.internal.telecom.ICallScreeningAdapter, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.-set0(android.telecom.CallScreeningService, com.android.internal.telecom.ICallScreeningAdapter):com.android.internal.telecom.ICallScreeningAdapter");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.telecom.CallScreeningService.<init>():void, dex:  in method: android.telecom.CallScreeningService.<init>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.telecom.CallScreeningService.<init>():void, dex: 
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
    public CallScreeningService() {
        /*
        // Can't load method instructions: Load method exception: null in method: android.telecom.CallScreeningService.<init>():void, dex:  in method: android.telecom.CallScreeningService.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.<init>():void");
    }

    public abstract void onScreenCall(Details details);

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telecom.CallScreeningService.respondToCall(android.telecom.Call$Details, android.telecom.CallScreeningService$CallResponse):void, dex: 
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
    public final void respondToCall(android.telecom.Call.Details r1, android.telecom.CallScreeningService.CallResponse r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telecom.CallScreeningService.respondToCall(android.telecom.Call$Details, android.telecom.CallScreeningService$CallResponse):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.CallScreeningService.respondToCall(android.telecom.Call$Details, android.telecom.CallScreeningService$CallResponse):void");
    }

    public IBinder onBind(Intent intent) {
        Log.v((Object) this, "onBind", new Object[0]);
        return new CallScreeningBinder(this, null);
    }

    public boolean onUnbind(Intent intent) {
        Log.v((Object) this, "onUnbind", new Object[0]);
        return false;
    }
}
