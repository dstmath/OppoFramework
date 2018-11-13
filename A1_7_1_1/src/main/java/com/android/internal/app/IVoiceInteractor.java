package com.android.internal.app;

import android.app.VoiceInteractor.PickOptionRequest.Option;
import android.app.VoiceInteractor.Prompt;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
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
public interface IVoiceInteractor extends IInterface {

    public static abstract class Stub extends Binder implements IVoiceInteractor {
        private static final String DESCRIPTOR = "com.android.internal.app.IVoiceInteractor";
        static final int TRANSACTION_startAbortVoice = 4;
        static final int TRANSACTION_startCommand = 5;
        static final int TRANSACTION_startCompleteVoice = 3;
        static final int TRANSACTION_startConfirmation = 1;
        static final int TRANSACTION_startPickOption = 2;
        static final int TRANSACTION_supportsCommands = 6;

        private static class Proxy implements IVoiceInteractor {
            private IBinder mRemote;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.IVoiceInteractor.Stub.Proxy.<init>(android.os.IBinder):void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.IVoiceInteractor.Stub.Proxy.<init>(android.os.IBinder):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.IVoiceInteractor.Stub.Proxy.<init>(android.os.IBinder):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.IVoiceInteractor.Stub.Proxy.asBinder():android.os.IBinder, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.IVoiceInteractor.Stub.Proxy.asBinder():android.os.IBinder, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.IVoiceInteractor.Stub.Proxy.asBinder():android.os.IBinder");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.IVoiceInteractor.Stub.Proxy.startAbortVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
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
            public com.android.internal.app.IVoiceInteractorRequest startAbortVoice(java.lang.String r1, com.android.internal.app.IVoiceInteractorCallback r2, android.app.VoiceInteractor.Prompt r3, android.os.Bundle r4) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.IVoiceInteractor.Stub.Proxy.startAbortVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.IVoiceInteractor.Stub.Proxy.startAbortVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.IVoiceInteractor.Stub.Proxy.startCommand(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, java.lang.String, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
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
            public com.android.internal.app.IVoiceInteractorRequest startCommand(java.lang.String r1, com.android.internal.app.IVoiceInteractorCallback r2, java.lang.String r3, android.os.Bundle r4) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.IVoiceInteractor.Stub.Proxy.startCommand(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, java.lang.String, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.IVoiceInteractor.Stub.Proxy.startCommand(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, java.lang.String, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.IVoiceInteractor.Stub.Proxy.startCompleteVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
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
            public com.android.internal.app.IVoiceInteractorRequest startCompleteVoice(java.lang.String r1, com.android.internal.app.IVoiceInteractorCallback r2, android.app.VoiceInteractor.Prompt r3, android.os.Bundle r4) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.IVoiceInteractor.Stub.Proxy.startCompleteVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.IVoiceInteractor.Stub.Proxy.startCompleteVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.IVoiceInteractor.Stub.Proxy.startConfirmation(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
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
            public com.android.internal.app.IVoiceInteractorRequest startConfirmation(java.lang.String r1, com.android.internal.app.IVoiceInteractorCallback r2, android.app.VoiceInteractor.Prompt r3, android.os.Bundle r4) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.IVoiceInteractor.Stub.Proxy.startConfirmation(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.IVoiceInteractor.Stub.Proxy.startConfirmation(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.IVoiceInteractor.Stub.Proxy.startPickOption(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
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
            public com.android.internal.app.IVoiceInteractorRequest startPickOption(java.lang.String r1, com.android.internal.app.IVoiceInteractorCallback r2, android.app.VoiceInteractor.Prompt r3, android.app.VoiceInteractor.PickOptionRequest.Option[] r4, android.os.Bundle r5) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.IVoiceInteractor.Stub.Proxy.startPickOption(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.IVoiceInteractor.Stub.Proxy.startPickOption(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.IVoiceInteractor.Stub.Proxy.supportsCommands(java.lang.String, java.lang.String[]):boolean[], dex: 
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
            public boolean[] supportsCommands(java.lang.String r1, java.lang.String[] r2) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.IVoiceInteractor.Stub.Proxy.supportsCommands(java.lang.String, java.lang.String[]):boolean[], dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.IVoiceInteractor.Stub.Proxy.supportsCommands(java.lang.String, java.lang.String[]):boolean[]");
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoiceInteractor asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoiceInteractor)) {
                return new Proxy(obj);
            }
            return (IVoiceInteractor) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String _arg0;
            IVoiceInteractorCallback _arg1;
            Prompt _arg2;
            Bundle _arg3;
            IVoiceInteractorRequest _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = com.android.internal.app.IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg2 = (Prompt) Prompt.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    _result = startConfirmation(_arg0, _arg1, _arg2, _arg3);
                    reply.writeNoException();
                    reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                    return true;
                case 2:
                    Bundle _arg4;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = com.android.internal.app.IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg2 = (Prompt) Prompt.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    Option[] _arg32 = (Option[]) data.createTypedArray(Option.CREATOR);
                    if (data.readInt() != 0) {
                        _arg4 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg4 = null;
                    }
                    _result = startPickOption(_arg0, _arg1, _arg2, _arg32, _arg4);
                    reply.writeNoException();
                    reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = com.android.internal.app.IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg2 = (Prompt) Prompt.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    _result = startCompleteVoice(_arg0, _arg1, _arg2, _arg3);
                    reply.writeNoException();
                    reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = com.android.internal.app.IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg2 = (Prompt) Prompt.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    _result = startAbortVoice(_arg0, _arg1, _arg2, _arg3);
                    reply.writeNoException();
                    reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = com.android.internal.app.IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                    String _arg22 = data.readString();
                    if (data.readInt() != 0) {
                        _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    _result = startCommand(_arg0, _arg1, _arg22, _arg3);
                    reply.writeNoException();
                    reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    boolean[] _result2 = supportsCommands(data.readString(), data.createStringArray());
                    reply.writeNoException();
                    reply.writeBooleanArray(_result2);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    IVoiceInteractorRequest startAbortVoice(String str, IVoiceInteractorCallback iVoiceInteractorCallback, Prompt prompt, Bundle bundle) throws RemoteException;

    IVoiceInteractorRequest startCommand(String str, IVoiceInteractorCallback iVoiceInteractorCallback, String str2, Bundle bundle) throws RemoteException;

    IVoiceInteractorRequest startCompleteVoice(String str, IVoiceInteractorCallback iVoiceInteractorCallback, Prompt prompt, Bundle bundle) throws RemoteException;

    IVoiceInteractorRequest startConfirmation(String str, IVoiceInteractorCallback iVoiceInteractorCallback, Prompt prompt, Bundle bundle) throws RemoteException;

    IVoiceInteractorRequest startPickOption(String str, IVoiceInteractorCallback iVoiceInteractorCallback, Prompt prompt, Option[] optionArr, Bundle bundle) throws RemoteException;

    boolean[] supportsCommands(String str, String[] strArr) throws RemoteException;
}
