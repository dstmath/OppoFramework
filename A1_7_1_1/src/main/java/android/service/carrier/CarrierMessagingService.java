package android.service.carrier;

import android.app.Service;
import android.os.RemoteException;
import android.service.carrier.ICarrierMessagingService.Stub;

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
public abstract class CarrierMessagingService extends Service {
    public static final int DOWNLOAD_STATUS_ERROR = 2;
    public static final int DOWNLOAD_STATUS_OK = 0;
    public static final int DOWNLOAD_STATUS_RETRY_ON_CARRIER_NETWORK = 1;
    public static final int RECEIVE_OPTIONS_DEFAULT = 0;
    public static final int RECEIVE_OPTIONS_DROP = 1;
    public static final int RECEIVE_OPTIONS_SKIP_NOTIFY_WHEN_CREDENTIAL_PROTECTED_STORAGE_UNAVAILABLE = 2;
    public static final int SEND_FLAG_REQUEST_DELIVERY_STATUS = 1;
    public static final int SEND_STATUS_ERROR = 2;
    public static final int SEND_STATUS_OK = 0;
    public static final int SEND_STATUS_RETRY_ON_CARRIER_NETWORK = 1;
    public static final String SERVICE_INTERFACE = "android.service.carrier.CarrierMessagingService";
    private final ICarrierMessagingWrapper mWrapper;

    public interface ResultCallback<T> {
        void onReceiveResult(T t) throws RemoteException;
    }

    /* renamed from: android.service.carrier.CarrierMessagingService$1 */
    class AnonymousClass1 implements ResultCallback<Boolean> {
        final /* synthetic */ CarrierMessagingService this$0;
        final /* synthetic */ ResultCallback val$callback;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.1.<init>(android.service.carrier.CarrierMessagingService, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
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
        AnonymousClass1(android.service.carrier.CarrierMessagingService r1, android.service.carrier.CarrierMessagingService.ResultCallback r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.1.<init>(android.service.carrier.CarrierMessagingService, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.1.<init>(android.service.carrier.CarrierMessagingService, android.service.carrier.CarrierMessagingService$ResultCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.1.onReceiveResult(java.lang.Boolean):void, dex: 
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
        public void onReceiveResult(java.lang.Boolean r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.1.onReceiveResult(java.lang.Boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.1.onReceiveResult(java.lang.Boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.1.onReceiveResult(java.lang.Object):void, dex: 
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
        public /* bridge */ /* synthetic */ void onReceiveResult(java.lang.Object r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.1.onReceiveResult(java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.1.onReceiveResult(java.lang.Object):void");
        }
    }

    private class ICarrierMessagingWrapper extends Stub {
        final /* synthetic */ CarrierMessagingService this$0;

        /* renamed from: android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper$1 */
        class AnonymousClass1 implements ResultCallback<Integer> {
            final /* synthetic */ ICarrierMessagingWrapper this$1;
            final /* synthetic */ ICarrierMessagingCallback val$callback;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.1.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void, dex: 
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
            AnonymousClass1(android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper r1, android.service.carrier.ICarrierMessagingCallback r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.1.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.1.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.1.onReceiveResult(java.lang.Integer):void, dex: 
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
            public void onReceiveResult(java.lang.Integer r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.1.onReceiveResult(java.lang.Integer):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.1.onReceiveResult(java.lang.Integer):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.1.onReceiveResult(java.lang.Object):void, dex: 
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
            public /* bridge */ /* synthetic */ void onReceiveResult(java.lang.Object r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.1.onReceiveResult(java.lang.Object):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.1.onReceiveResult(java.lang.Object):void");
            }
        }

        /* renamed from: android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper$2 */
        class AnonymousClass2 implements ResultCallback<SendSmsResult> {
            final /* synthetic */ ICarrierMessagingWrapper this$1;
            final /* synthetic */ ICarrierMessagingCallback val$callback;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.2.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void, dex: 
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
            AnonymousClass2(android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper r1, android.service.carrier.ICarrierMessagingCallback r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.2.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.2.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.2.onReceiveResult(android.service.carrier.CarrierMessagingService$SendSmsResult):void, dex: 
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
            public void onReceiveResult(android.service.carrier.CarrierMessagingService.SendSmsResult r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.2.onReceiveResult(android.service.carrier.CarrierMessagingService$SendSmsResult):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.2.onReceiveResult(android.service.carrier.CarrierMessagingService$SendSmsResult):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.2.onReceiveResult(java.lang.Object):void, dex: 
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
            public /* bridge */ /* synthetic */ void onReceiveResult(java.lang.Object r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.2.onReceiveResult(java.lang.Object):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.2.onReceiveResult(java.lang.Object):void");
            }
        }

        /* renamed from: android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper$3 */
        class AnonymousClass3 implements ResultCallback<SendSmsResult> {
            final /* synthetic */ ICarrierMessagingWrapper this$1;
            final /* synthetic */ ICarrierMessagingCallback val$callback;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.3.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void, dex: 
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
            AnonymousClass3(android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper r1, android.service.carrier.ICarrierMessagingCallback r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.3.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.3.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.3.onReceiveResult(android.service.carrier.CarrierMessagingService$SendSmsResult):void, dex: 
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
            public void onReceiveResult(android.service.carrier.CarrierMessagingService.SendSmsResult r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.3.onReceiveResult(android.service.carrier.CarrierMessagingService$SendSmsResult):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.3.onReceiveResult(android.service.carrier.CarrierMessagingService$SendSmsResult):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.3.onReceiveResult(java.lang.Object):void, dex: 
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
            public /* bridge */ /* synthetic */ void onReceiveResult(java.lang.Object r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.3.onReceiveResult(java.lang.Object):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.3.onReceiveResult(java.lang.Object):void");
            }
        }

        /* renamed from: android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper$4 */
        class AnonymousClass4 implements ResultCallback<SendMultipartSmsResult> {
            final /* synthetic */ ICarrierMessagingWrapper this$1;
            final /* synthetic */ ICarrierMessagingCallback val$callback;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.4.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void, dex: 
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
            AnonymousClass4(android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper r1, android.service.carrier.ICarrierMessagingCallback r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.4.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.4.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.4.onReceiveResult(android.service.carrier.CarrierMessagingService$SendMultipartSmsResult):void, dex: 
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
            public void onReceiveResult(android.service.carrier.CarrierMessagingService.SendMultipartSmsResult r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.4.onReceiveResult(android.service.carrier.CarrierMessagingService$SendMultipartSmsResult):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.4.onReceiveResult(android.service.carrier.CarrierMessagingService$SendMultipartSmsResult):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.4.onReceiveResult(java.lang.Object):void, dex: 
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
            public /* bridge */ /* synthetic */ void onReceiveResult(java.lang.Object r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.4.onReceiveResult(java.lang.Object):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.4.onReceiveResult(java.lang.Object):void");
            }
        }

        /* renamed from: android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper$5 */
        class AnonymousClass5 implements ResultCallback<SendMmsResult> {
            final /* synthetic */ ICarrierMessagingWrapper this$1;
            final /* synthetic */ ICarrierMessagingCallback val$callback;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.5.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void, dex: 
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
            AnonymousClass5(android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper r1, android.service.carrier.ICarrierMessagingCallback r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.5.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.5.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.5.onReceiveResult(android.service.carrier.CarrierMessagingService$SendMmsResult):void, dex: 
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
            public void onReceiveResult(android.service.carrier.CarrierMessagingService.SendMmsResult r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.5.onReceiveResult(android.service.carrier.CarrierMessagingService$SendMmsResult):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.5.onReceiveResult(android.service.carrier.CarrierMessagingService$SendMmsResult):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.5.onReceiveResult(java.lang.Object):void, dex: 
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
            public /* bridge */ /* synthetic */ void onReceiveResult(java.lang.Object r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.5.onReceiveResult(java.lang.Object):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.5.onReceiveResult(java.lang.Object):void");
            }
        }

        /* renamed from: android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper$6 */
        class AnonymousClass6 implements ResultCallback<Integer> {
            final /* synthetic */ ICarrierMessagingWrapper this$1;
            final /* synthetic */ ICarrierMessagingCallback val$callback;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.6.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void, dex: 
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
            AnonymousClass6(android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper r1, android.service.carrier.ICarrierMessagingCallback r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.6.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.6.<init>(android.service.carrier.CarrierMessagingService$ICarrierMessagingWrapper, android.service.carrier.ICarrierMessagingCallback):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.6.onReceiveResult(java.lang.Integer):void, dex: 
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
            public void onReceiveResult(java.lang.Integer r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.6.onReceiveResult(java.lang.Integer):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.6.onReceiveResult(java.lang.Integer):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.6.onReceiveResult(java.lang.Object):void, dex: 
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
            public /* bridge */ /* synthetic */ void onReceiveResult(java.lang.Object r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.6.onReceiveResult(java.lang.Object):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.6.onReceiveResult(java.lang.Object):void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.<init>(android.service.carrier.CarrierMessagingService):void, dex: 
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
        private ICarrierMessagingWrapper(android.service.carrier.CarrierMessagingService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.<init>(android.service.carrier.CarrierMessagingService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.<init>(android.service.carrier.CarrierMessagingService):void");
        }

        /* synthetic */ ICarrierMessagingWrapper(CarrierMessagingService this$0, ICarrierMessagingWrapper iCarrierMessagingWrapper) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.downloadMms(android.net.Uri, int, android.net.Uri, android.service.carrier.ICarrierMessagingCallback):void, dex: 
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
        public void downloadMms(android.net.Uri r1, int r2, android.net.Uri r3, android.service.carrier.ICarrierMessagingCallback r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.downloadMms(android.net.Uri, int, android.net.Uri, android.service.carrier.ICarrierMessagingCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.downloadMms(android.net.Uri, int, android.net.Uri, android.service.carrier.ICarrierMessagingCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.filterSms(android.service.carrier.MessagePdu, java.lang.String, int, int, android.service.carrier.ICarrierMessagingCallback):void, dex: 
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
        public void filterSms(android.service.carrier.MessagePdu r1, java.lang.String r2, int r3, int r4, android.service.carrier.ICarrierMessagingCallback r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.filterSms(android.service.carrier.MessagePdu, java.lang.String, int, int, android.service.carrier.ICarrierMessagingCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.filterSms(android.service.carrier.MessagePdu, java.lang.String, int, int, android.service.carrier.ICarrierMessagingCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.sendDataSms(byte[], int, java.lang.String, int, int, android.service.carrier.ICarrierMessagingCallback):void, dex: 
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
        public void sendDataSms(byte[] r1, int r2, java.lang.String r3, int r4, int r5, android.service.carrier.ICarrierMessagingCallback r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.sendDataSms(byte[], int, java.lang.String, int, int, android.service.carrier.ICarrierMessagingCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.sendDataSms(byte[], int, java.lang.String, int, int, android.service.carrier.ICarrierMessagingCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.sendMms(android.net.Uri, int, android.net.Uri, android.service.carrier.ICarrierMessagingCallback):void, dex: 
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
        public void sendMms(android.net.Uri r1, int r2, android.net.Uri r3, android.service.carrier.ICarrierMessagingCallback r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.sendMms(android.net.Uri, int, android.net.Uri, android.service.carrier.ICarrierMessagingCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.sendMms(android.net.Uri, int, android.net.Uri, android.service.carrier.ICarrierMessagingCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.sendMultipartTextSms(java.util.List, int, java.lang.String, int, android.service.carrier.ICarrierMessagingCallback):void, dex: 
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
        public void sendMultipartTextSms(java.util.List<java.lang.String> r1, int r2, java.lang.String r3, int r4, android.service.carrier.ICarrierMessagingCallback r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.sendMultipartTextSms(java.util.List, int, java.lang.String, int, android.service.carrier.ICarrierMessagingCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.sendMultipartTextSms(java.util.List, int, java.lang.String, int, android.service.carrier.ICarrierMessagingCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.sendTextSms(java.lang.String, int, java.lang.String, int, android.service.carrier.ICarrierMessagingCallback):void, dex: 
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
        public void sendTextSms(java.lang.String r1, int r2, java.lang.String r3, int r4, android.service.carrier.ICarrierMessagingCallback r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.sendTextSms(java.lang.String, int, java.lang.String, int, android.service.carrier.ICarrierMessagingCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.sendTextSms(java.lang.String, int, java.lang.String, int, android.service.carrier.ICarrierMessagingCallback):void");
        }
    }

    public static final class SendMmsResult {
        private byte[] mSendConfPdu;
        private int mSendStatus;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.service.carrier.CarrierMessagingService.SendMmsResult.<init>(int, byte[]):void, dex: 
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
        public SendMmsResult(int r1, byte[] r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.service.carrier.CarrierMessagingService.SendMmsResult.<init>(int, byte[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.SendMmsResult.<init>(int, byte[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.SendMmsResult.getSendConfPdu():byte[], dex: 
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
        public byte[] getSendConfPdu() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.SendMmsResult.getSendConfPdu():byte[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.SendMmsResult.getSendConfPdu():byte[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.service.carrier.CarrierMessagingService.SendMmsResult.getSendStatus():int, dex: 
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
        public int getSendStatus() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.service.carrier.CarrierMessagingService.SendMmsResult.getSendStatus():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.SendMmsResult.getSendStatus():int");
        }
    }

    public static final class SendMultipartSmsResult {
        private final int[] mMessageRefs;
        private final int mSendStatus;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.service.carrier.CarrierMessagingService.SendMultipartSmsResult.<init>(int, int[]):void, dex: 
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
        public SendMultipartSmsResult(int r1, int[] r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.service.carrier.CarrierMessagingService.SendMultipartSmsResult.<init>(int, int[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.SendMultipartSmsResult.<init>(int, int[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.SendMultipartSmsResult.getMessageRefs():int[], dex: 
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
        public int[] getMessageRefs() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.carrier.CarrierMessagingService.SendMultipartSmsResult.getMessageRefs():int[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.SendMultipartSmsResult.getMessageRefs():int[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.service.carrier.CarrierMessagingService.SendMultipartSmsResult.getSendStatus():int, dex: 
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
        public int getSendStatus() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.service.carrier.CarrierMessagingService.SendMultipartSmsResult.getSendStatus():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.SendMultipartSmsResult.getSendStatus():int");
        }
    }

    public static final class SendSmsResult {
        private final int mMessageRef;
        private final int mSendStatus;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.service.carrier.CarrierMessagingService.SendSmsResult.<init>(int, int):void, dex: 
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
        public SendSmsResult(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.service.carrier.CarrierMessagingService.SendSmsResult.<init>(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.SendSmsResult.<init>(int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.service.carrier.CarrierMessagingService.SendSmsResult.getMessageRef():int, dex: 
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
        public int getMessageRef() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.service.carrier.CarrierMessagingService.SendSmsResult.getMessageRef():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.SendSmsResult.getMessageRef():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.service.carrier.CarrierMessagingService.SendSmsResult.getSendStatus():int, dex: 
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
        public int getSendStatus() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.service.carrier.CarrierMessagingService.SendSmsResult.getSendStatus():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.SendSmsResult.getSendStatus():int");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.<init>():void, dex: 
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
    public CarrierMessagingService() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.carrier.CarrierMessagingService.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.onBind(android.content.Intent):android.os.IBinder, dex: 
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
    public android.os.IBinder onBind(android.content.Intent r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.onBind(android.content.Intent):android.os.IBinder, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.onBind(android.content.Intent):android.os.IBinder");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.service.carrier.CarrierMessagingService.onDownloadMms(android.net.Uri, int, android.net.Uri, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
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
    public void onDownloadMms(android.net.Uri r1, int r2, android.net.Uri r3, android.service.carrier.CarrierMessagingService.ResultCallback<java.lang.Integer> r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.service.carrier.CarrierMessagingService.onDownloadMms(android.net.Uri, int, android.net.Uri, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.onDownloadMms(android.net.Uri, int, android.net.Uri, android.service.carrier.CarrierMessagingService$ResultCallback):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.service.carrier.CarrierMessagingService.onFilterSms(android.service.carrier.MessagePdu, java.lang.String, int, int, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
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
    @java.lang.Deprecated
    public void onFilterSms(android.service.carrier.MessagePdu r1, java.lang.String r2, int r3, int r4, android.service.carrier.CarrierMessagingService.ResultCallback<java.lang.Boolean> r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.service.carrier.CarrierMessagingService.onFilterSms(android.service.carrier.MessagePdu, java.lang.String, int, int, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.onFilterSms(android.service.carrier.MessagePdu, java.lang.String, int, int, android.service.carrier.CarrierMessagingService$ResultCallback):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.service.carrier.CarrierMessagingService.onReceiveTextSms(android.service.carrier.MessagePdu, java.lang.String, int, int, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void onReceiveTextSms(android.service.carrier.MessagePdu r1, java.lang.String r2, int r3, int r4, android.service.carrier.CarrierMessagingService.ResultCallback<java.lang.Integer> r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.service.carrier.CarrierMessagingService.onReceiveTextSms(android.service.carrier.MessagePdu, java.lang.String, int, int, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.onReceiveTextSms(android.service.carrier.MessagePdu, java.lang.String, int, int, android.service.carrier.CarrierMessagingService$ResultCallback):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.service.carrier.CarrierMessagingService.onSendDataSms(byte[], int, java.lang.String, int, int, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void onSendDataSms(byte[] r1, int r2, java.lang.String r3, int r4, int r5, android.service.carrier.CarrierMessagingService.ResultCallback<android.service.carrier.CarrierMessagingService.SendSmsResult> r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.service.carrier.CarrierMessagingService.onSendDataSms(byte[], int, java.lang.String, int, int, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.onSendDataSms(byte[], int, java.lang.String, int, int, android.service.carrier.CarrierMessagingService$ResultCallback):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.service.carrier.CarrierMessagingService.onSendDataSms(byte[], int, java.lang.String, int, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
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
    @java.lang.Deprecated
    public void onSendDataSms(byte[] r1, int r2, java.lang.String r3, int r4, android.service.carrier.CarrierMessagingService.ResultCallback<android.service.carrier.CarrierMessagingService.SendSmsResult> r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.service.carrier.CarrierMessagingService.onSendDataSms(byte[], int, java.lang.String, int, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.onSendDataSms(byte[], int, java.lang.String, int, android.service.carrier.CarrierMessagingService$ResultCallback):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.service.carrier.CarrierMessagingService.onSendMms(android.net.Uri, int, android.net.Uri, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
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
    public void onSendMms(android.net.Uri r1, int r2, android.net.Uri r3, android.service.carrier.CarrierMessagingService.ResultCallback<android.service.carrier.CarrierMessagingService.SendMmsResult> r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.service.carrier.CarrierMessagingService.onSendMms(android.net.Uri, int, android.net.Uri, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.onSendMms(android.net.Uri, int, android.net.Uri, android.service.carrier.CarrierMessagingService$ResultCallback):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.onSendMultipartTextSms(java.util.List, int, java.lang.String, int, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
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
    public void onSendMultipartTextSms(java.util.List<java.lang.String> r1, int r2, java.lang.String r3, int r4, android.service.carrier.CarrierMessagingService.ResultCallback<android.service.carrier.CarrierMessagingService.SendMultipartSmsResult> r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.onSendMultipartTextSms(java.util.List, int, java.lang.String, int, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.onSendMultipartTextSms(java.util.List, int, java.lang.String, int, android.service.carrier.CarrierMessagingService$ResultCallback):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.service.carrier.CarrierMessagingService.onSendMultipartTextSms(java.util.List, int, java.lang.String, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
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
    @java.lang.Deprecated
    public void onSendMultipartTextSms(java.util.List<java.lang.String> r1, int r2, java.lang.String r3, android.service.carrier.CarrierMessagingService.ResultCallback<android.service.carrier.CarrierMessagingService.SendMultipartSmsResult> r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.service.carrier.CarrierMessagingService.onSendMultipartTextSms(java.util.List, int, java.lang.String, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.onSendMultipartTextSms(java.util.List, int, java.lang.String, android.service.carrier.CarrierMessagingService$ResultCallback):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.onSendTextSms(java.lang.String, int, java.lang.String, int, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
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
    public void onSendTextSms(java.lang.String r1, int r2, java.lang.String r3, int r4, android.service.carrier.CarrierMessagingService.ResultCallback<android.service.carrier.CarrierMessagingService.SendSmsResult> r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.carrier.CarrierMessagingService.onSendTextSms(java.lang.String, int, java.lang.String, int, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.onSendTextSms(java.lang.String, int, java.lang.String, int, android.service.carrier.CarrierMessagingService$ResultCallback):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.service.carrier.CarrierMessagingService.onSendTextSms(java.lang.String, int, java.lang.String, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
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
    @java.lang.Deprecated
    public void onSendTextSms(java.lang.String r1, int r2, java.lang.String r3, android.service.carrier.CarrierMessagingService.ResultCallback<android.service.carrier.CarrierMessagingService.SendSmsResult> r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.service.carrier.CarrierMessagingService.onSendTextSms(java.lang.String, int, java.lang.String, android.service.carrier.CarrierMessagingService$ResultCallback):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.carrier.CarrierMessagingService.onSendTextSms(java.lang.String, int, java.lang.String, android.service.carrier.CarrierMessagingService$ResultCallback):void");
    }
}
