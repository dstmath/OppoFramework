package org.gsma.joyn.gsh;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import java.util.List;
import org.gsma.joyn.IJoynServiceRegistrationListener;
import org.gsma.joyn.chat.Geoloc;

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
public interface IGeolocSharingService extends IInterface {

    public static abstract class Stub extends Binder implements IGeolocSharingService {
        private static final String DESCRIPTOR = "org.gsma.joyn.gsh.IGeolocSharingService";
        static final int TRANSACTION_addNewGeolocSharingListener = 7;
        static final int TRANSACTION_addServiceRegistrationListener = 2;
        static final int TRANSACTION_getGeolocSharing = 5;
        static final int TRANSACTION_getGeolocSharings = 4;
        static final int TRANSACTION_getServiceVersion = 9;
        static final int TRANSACTION_isServiceRegistered = 1;
        static final int TRANSACTION_removeNewGeolocSharingListener = 8;
        static final int TRANSACTION_removeServiceRegistrationListener = 3;
        static final int TRANSACTION_shareGeoloc = 6;

        private static class Proxy implements IGeolocSharingService {
            private IBinder mRemote;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.<init>(android.os.IBinder):void, dex: 
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
            Proxy(android.os.IBinder r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.<init>(android.os.IBinder):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.<init>(android.os.IBinder):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.addNewGeolocSharingListener(org.gsma.joyn.gsh.INewGeolocSharingListener):void, dex: 
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
            public void addNewGeolocSharingListener(org.gsma.joyn.gsh.INewGeolocSharingListener r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.addNewGeolocSharingListener(org.gsma.joyn.gsh.INewGeolocSharingListener):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.addNewGeolocSharingListener(org.gsma.joyn.gsh.INewGeolocSharingListener):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.addServiceRegistrationListener(org.gsma.joyn.IJoynServiceRegistrationListener):void, dex: 
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
            public void addServiceRegistrationListener(org.gsma.joyn.IJoynServiceRegistrationListener r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.addServiceRegistrationListener(org.gsma.joyn.IJoynServiceRegistrationListener):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.addServiceRegistrationListener(org.gsma.joyn.IJoynServiceRegistrationListener):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.asBinder():android.os.IBinder, dex: 
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
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public android.os.IBinder asBinder() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.asBinder():android.os.IBinder, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.asBinder():android.os.IBinder");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.getGeolocSharing(java.lang.String):org.gsma.joyn.gsh.IGeolocSharing, dex: 
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
            public org.gsma.joyn.gsh.IGeolocSharing getGeolocSharing(java.lang.String r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.getGeolocSharing(java.lang.String):org.gsma.joyn.gsh.IGeolocSharing, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.getGeolocSharing(java.lang.String):org.gsma.joyn.gsh.IGeolocSharing");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.getGeolocSharings():java.util.List<android.os.IBinder>, dex: 
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
            public java.util.List<android.os.IBinder> getGeolocSharings() throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.getGeolocSharings():java.util.List<android.os.IBinder>, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.getGeolocSharings():java.util.List<android.os.IBinder>");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.getServiceVersion():int, dex: 
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
            public int getServiceVersion() throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.getServiceVersion():int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.getServiceVersion():int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.isServiceRegistered():boolean, dex: 
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
            public boolean isServiceRegistered() throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.isServiceRegistered():boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.isServiceRegistered():boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.removeNewGeolocSharingListener(org.gsma.joyn.gsh.INewGeolocSharingListener):void, dex: 
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
            public void removeNewGeolocSharingListener(org.gsma.joyn.gsh.INewGeolocSharingListener r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.removeNewGeolocSharingListener(org.gsma.joyn.gsh.INewGeolocSharingListener):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.removeNewGeolocSharingListener(org.gsma.joyn.gsh.INewGeolocSharingListener):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.removeServiceRegistrationListener(org.gsma.joyn.IJoynServiceRegistrationListener):void, dex: 
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
            public void removeServiceRegistrationListener(org.gsma.joyn.IJoynServiceRegistrationListener r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.removeServiceRegistrationListener(org.gsma.joyn.IJoynServiceRegistrationListener):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.removeServiceRegistrationListener(org.gsma.joyn.IJoynServiceRegistrationListener):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.shareGeoloc(java.lang.String, org.gsma.joyn.chat.Geoloc, org.gsma.joyn.gsh.IGeolocSharingListener):org.gsma.joyn.gsh.IGeolocSharing, dex: 
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
            public org.gsma.joyn.gsh.IGeolocSharing shareGeoloc(java.lang.String r1, org.gsma.joyn.chat.Geoloc r2, org.gsma.joyn.gsh.IGeolocSharingListener r3) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.shareGeoloc(java.lang.String, org.gsma.joyn.chat.Geoloc, org.gsma.joyn.gsh.IGeolocSharingListener):org.gsma.joyn.gsh.IGeolocSharing, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.gsh.IGeolocSharingService.Stub.Proxy.shareGeoloc(java.lang.String, org.gsma.joyn.chat.Geoloc, org.gsma.joyn.gsh.IGeolocSharingListener):org.gsma.joyn.gsh.IGeolocSharing");
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.<init>():void, dex: 
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
        public Stub() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.gsh.IGeolocSharingService.Stub.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean, dex: 
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
        public boolean onTransact(int r1, android.os.Parcel r2, android.os.Parcel r3, int r4) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.gsh.IGeolocSharingService.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.gsh.IGeolocSharingService.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }

        public static IGeolocSharingService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGeolocSharingService)) {
                return new Proxy(obj);
            }
            return (IGeolocSharingService) iin;
        }

        public IBinder asBinder() {
            return this;
        }
    }

    void addNewGeolocSharingListener(INewGeolocSharingListener iNewGeolocSharingListener) throws RemoteException;

    void addServiceRegistrationListener(IJoynServiceRegistrationListener iJoynServiceRegistrationListener) throws RemoteException;

    IGeolocSharing getGeolocSharing(String str) throws RemoteException;

    List<IBinder> getGeolocSharings() throws RemoteException;

    int getServiceVersion() throws RemoteException;

    boolean isServiceRegistered() throws RemoteException;

    void removeNewGeolocSharingListener(INewGeolocSharingListener iNewGeolocSharingListener) throws RemoteException;

    void removeServiceRegistrationListener(IJoynServiceRegistrationListener iJoynServiceRegistrationListener) throws RemoteException;

    IGeolocSharing shareGeoloc(String str, Geoloc geoloc, IGeolocSharingListener iGeolocSharingListener) throws RemoteException;
}
