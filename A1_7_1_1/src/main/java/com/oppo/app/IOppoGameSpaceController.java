package com.oppo.app;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

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
public interface IOppoGameSpaceController extends IInterface {

    public static abstract class Stub extends Binder implements IOppoGameSpaceController {
        private static final String DESCRIPTOR = "com.oppo.app.IOppoGameSpaceController";
        static final int TRANSACTION_gameExiting = 2;
        static final int TRANSACTION_gameStarting = 1;
        static final int TRANSACTION_videoStarting = 3;

        private static class Proxy implements IOppoGameSpaceController {
            private IBinder mRemote;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.oppo.app.IOppoGameSpaceController.Stub.Proxy.<init>(android.os.IBinder):void, dex: 
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
            Proxy(android.os.IBinder r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.oppo.app.IOppoGameSpaceController.Stub.Proxy.<init>(android.os.IBinder):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.oppo.app.IOppoGameSpaceController.Stub.Proxy.<init>(android.os.IBinder):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.app.IOppoGameSpaceController.Stub.Proxy.asBinder():android.os.IBinder, dex: 
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
            public android.os.IBinder asBinder() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.app.IOppoGameSpaceController.Stub.Proxy.asBinder():android.os.IBinder, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.oppo.app.IOppoGameSpaceController.Stub.Proxy.asBinder():android.os.IBinder");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.app.IOppoGameSpaceController.Stub.Proxy.gameExiting(java.lang.String):void, dex: 
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
            public void gameExiting(java.lang.String r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.app.IOppoGameSpaceController.Stub.Proxy.gameExiting(java.lang.String):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.oppo.app.IOppoGameSpaceController.Stub.Proxy.gameExiting(java.lang.String):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.app.IOppoGameSpaceController.Stub.Proxy.gameStarting(android.content.Intent, java.lang.String, boolean):void, dex: 
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
            public void gameStarting(android.content.Intent r1, java.lang.String r2, boolean r3) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.app.IOppoGameSpaceController.Stub.Proxy.gameStarting(android.content.Intent, java.lang.String, boolean):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.oppo.app.IOppoGameSpaceController.Stub.Proxy.gameStarting(android.content.Intent, java.lang.String, boolean):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.app.IOppoGameSpaceController.Stub.Proxy.videoStarting(android.content.Intent, java.lang.String):void, dex: 
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
            public void videoStarting(android.content.Intent r1, java.lang.String r2) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.app.IOppoGameSpaceController.Stub.Proxy.videoStarting(android.content.Intent, java.lang.String):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.oppo.app.IOppoGameSpaceController.Stub.Proxy.videoStarting(android.content.Intent, java.lang.String):void");
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.app.IOppoGameSpaceController.Stub.<init>():void, dex: 
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
        public Stub() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.app.IOppoGameSpaceController.Stub.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.app.IOppoGameSpaceController.Stub.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.app.IOppoGameSpaceController.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean, dex: 
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
        public boolean onTransact(int r1, android.os.Parcel r2, android.os.Parcel r3, int r4) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.app.IOppoGameSpaceController.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.app.IOppoGameSpaceController.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }

        public static IOppoGameSpaceController asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoGameSpaceController)) {
                return new Proxy(obj);
            }
            return (IOppoGameSpaceController) iin;
        }

        public IBinder asBinder() {
            return this;
        }
    }

    void gameExiting(String str) throws RemoteException;

    void gameStarting(Intent intent, String str, boolean z) throws RemoteException;

    void videoStarting(Intent intent, String str) throws RemoteException;
}
