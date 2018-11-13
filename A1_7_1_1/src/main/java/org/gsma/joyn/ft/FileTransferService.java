package org.gsma.joyn.ft;

import android.content.ServiceConnection;
import org.gsma.joyn.JoynService;

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
public class FileTransferService extends JoynService {
    public static final String TAG = "TAPI-FileTransferService";
    private IFileTransferService api;
    private ServiceConnection apiConnection;

    /* renamed from: org.gsma.joyn.ft.FileTransferService$1 */
    class AnonymousClass1 implements ServiceConnection {
        final /* synthetic */ FileTransferService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: org.gsma.joyn.ft.FileTransferService.1.<init>(org.gsma.joyn.ft.FileTransferService):void, dex: 
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
        AnonymousClass1(org.gsma.joyn.ft.FileTransferService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: org.gsma.joyn.ft.FileTransferService.1.<init>(org.gsma.joyn.ft.FileTransferService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.1.<init>(org.gsma.joyn.ft.FileTransferService):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.1.onServiceConnected(android.content.ComponentName, android.os.IBinder):void, dex: 
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
        public void onServiceConnected(android.content.ComponentName r1, android.os.IBinder r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.1.onServiceConnected(android.content.ComponentName, android.os.IBinder):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.1.onServiceConnected(android.content.ComponentName, android.os.IBinder):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.1.onServiceDisconnected(android.content.ComponentName):void, dex: 
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
        public void onServiceDisconnected(android.content.ComponentName r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.1.onServiceDisconnected(android.content.ComponentName):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.1.onServiceDisconnected(android.content.ComponentName):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.gsma.joyn.ft.FileTransferService.-get0(org.gsma.joyn.ft.FileTransferService):org.gsma.joyn.JoynServiceListener, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    /* renamed from: -get0 */
    static /* synthetic */ org.gsma.joyn.JoynServiceListener m12-get0(org.gsma.joyn.ft.FileTransferService r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.gsma.joyn.ft.FileTransferService.-get0(org.gsma.joyn.ft.FileTransferService):org.gsma.joyn.JoynServiceListener, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.-get0(org.gsma.joyn.ft.FileTransferService):org.gsma.joyn.JoynServiceListener");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: org.gsma.joyn.ft.FileTransferService.<init>(android.content.Context, org.gsma.joyn.JoynServiceListener):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public FileTransferService(android.content.Context r1, org.gsma.joyn.JoynServiceListener r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: org.gsma.joyn.ft.FileTransferService.<init>(android.content.Context, org.gsma.joyn.JoynServiceListener):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.<init>(android.content.Context, org.gsma.joyn.JoynServiceListener):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.addNewFileTransferListener(org.gsma.joyn.ft.NewFileTransferListener):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public void addNewFileTransferListener(org.gsma.joyn.ft.NewFileTransferListener r1) throws org.gsma.joyn.JoynServiceException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.addNewFileTransferListener(org.gsma.joyn.ft.NewFileTransferListener):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.addNewFileTransferListener(org.gsma.joyn.ft.NewFileTransferListener):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.connect():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public void connect() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.connect():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.connect():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.gsma.joyn.ft.FileTransferService.disconnect():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public void disconnect() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.gsma.joyn.ft.FileTransferService.disconnect():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.disconnect():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.getConfiguration():org.gsma.joyn.ft.FileTransferServiceConfiguration, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransferServiceConfiguration getConfiguration() throws org.gsma.joyn.JoynServiceException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.getConfiguration():org.gsma.joyn.ft.FileTransferServiceConfiguration, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.getConfiguration():org.gsma.joyn.ft.FileTransferServiceConfiguration");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.getFileTransfer(java.lang.String):org.gsma.joyn.ft.FileTransfer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransfer getFileTransfer(java.lang.String r1) throws org.gsma.joyn.JoynServiceException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.getFileTransfer(java.lang.String):org.gsma.joyn.ft.FileTransfer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.getFileTransfer(java.lang.String):org.gsma.joyn.ft.FileTransfer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.getFileTransferFor(android.content.Intent):org.gsma.joyn.ft.FileTransfer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransfer getFileTransferFor(android.content.Intent r1) throws org.gsma.joyn.JoynServiceException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.getFileTransferFor(android.content.Intent):org.gsma.joyn.ft.FileTransfer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.getFileTransferFor(android.content.Intent):org.gsma.joyn.ft.FileTransfer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.getFileTransfers():java.util.Set<org.gsma.joyn.ft.FileTransfer>, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public java.util.Set<org.gsma.joyn.ft.FileTransfer> getFileTransfers() throws org.gsma.joyn.JoynServiceException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.getFileTransfers():java.util.Set<org.gsma.joyn.ft.FileTransfer>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.getFileTransfers():java.util.Set<org.gsma.joyn.ft.FileTransfer>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.prosecuteFile(java.lang.String, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransfer prosecuteFile(java.lang.String r1, java.lang.String r2, org.gsma.joyn.ft.FileTransferListener r3) throws org.gsma.joyn.JoynServiceException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.prosecuteFile(java.lang.String, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.prosecuteFile(java.lang.String, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.removeNewFileTransferListener(org.gsma.joyn.ft.NewFileTransferListener):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public void removeNewFileTransferListener(org.gsma.joyn.ft.NewFileTransferListener r1) throws org.gsma.joyn.JoynServiceException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.removeNewFileTransferListener(org.gsma.joyn.ft.NewFileTransferListener):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.removeNewFileTransferListener(org.gsma.joyn.ft.NewFileTransferListener):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.resumeFileTransfer(java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransfer resumeFileTransfer(java.lang.String r1, org.gsma.joyn.ft.FileTransferListener r2) throws org.gsma.joyn.JoynServiceException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.resumeFileTransfer(java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.resumeFileTransfer(java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.gsma.joyn.ft.FileTransferService.setApi(android.os.IInterface):void, dex:  in method: org.gsma.joyn.ft.FileTransferService.setApi(android.os.IInterface):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.gsma.joyn.ft.FileTransferService.setApi(android.os.IInterface):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 9 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 10 more
        */
    protected void setApi(android.os.IInterface r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: org.gsma.joyn.ft.FileTransferService.setApi(android.os.IInterface):void, dex:  in method: org.gsma.joyn.ft.FileTransferService.setApi(android.os.IInterface):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.setApi(android.os.IInterface):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.transferFile(java.lang.String, java.lang.String, java.lang.String, int, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransfer transferFile(java.lang.String r1, java.lang.String r2, java.lang.String r3, int r4, java.lang.String r5, org.gsma.joyn.ft.FileTransferListener r6) throws org.gsma.joyn.JoynServiceException, org.gsma.joyn.JoynContactFormatException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.transferFile(java.lang.String, java.lang.String, java.lang.String, int, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.transferFile(java.lang.String, java.lang.String, java.lang.String, int, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: org.gsma.joyn.ft.FileTransferService.transferFile(java.lang.String, java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransfer transferFile(java.lang.String r1, java.lang.String r2, java.lang.String r3, int r4, org.gsma.joyn.ft.FileTransferListener r5) throws org.gsma.joyn.JoynServiceException, org.gsma.joyn.JoynContactFormatException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: org.gsma.joyn.ft.FileTransferService.transferFile(java.lang.String, java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.transferFile(java.lang.String, java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.transferFile(java.lang.String, java.lang.String, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransfer transferFile(java.lang.String r1, java.lang.String r2, java.lang.String r3, org.gsma.joyn.ft.FileTransferListener r4) throws org.gsma.joyn.JoynServiceException, org.gsma.joyn.JoynContactFormatException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.transferFile(java.lang.String, java.lang.String, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.transferFile(java.lang.String, java.lang.String, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.transferFile(java.lang.String, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransfer transferFile(java.lang.String r1, java.lang.String r2, org.gsma.joyn.ft.FileTransferListener r3) throws org.gsma.joyn.JoynServiceException, org.gsma.joyn.JoynContactFormatException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.transferFile(java.lang.String, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.transferFile(java.lang.String, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: org.gsma.joyn.ft.FileTransferService.transferFileToGroup(java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransfer transferFileToGroup(java.lang.String r1, java.lang.String r2, int r3, org.gsma.joyn.ft.FileTransferListener r4) throws org.gsma.joyn.JoynServiceException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: org.gsma.joyn.ft.FileTransferService.transferFileToGroup(java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.transferFileToGroup(java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.transferFileToGroup(java.lang.String, java.lang.String, java.lang.String, int, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransfer transferFileToGroup(java.lang.String r1, java.lang.String r2, java.lang.String r3, int r4, java.lang.String r5, org.gsma.joyn.ft.FileTransferListener r6) throws org.gsma.joyn.JoynServiceException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.transferFileToGroup(java.lang.String, java.lang.String, java.lang.String, int, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.transferFileToGroup(java.lang.String, java.lang.String, java.lang.String, int, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: org.gsma.joyn.ft.FileTransferService.transferFileToGroup(java.lang.String, java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransfer transferFileToGroup(java.lang.String r1, java.lang.String r2, java.lang.String r3, int r4, org.gsma.joyn.ft.FileTransferListener r5) throws org.gsma.joyn.JoynServiceException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: org.gsma.joyn.ft.FileTransferService.transferFileToGroup(java.lang.String, java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.transferFileToGroup(java.lang.String, java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: org.gsma.joyn.ft.FileTransferService.transferFileToGroup(java.lang.String, java.util.Set, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransfer transferFileToGroup(java.lang.String r1, java.util.Set<java.lang.String> r2, java.lang.String r3, int r4, org.gsma.joyn.ft.FileTransferListener r5) throws org.gsma.joyn.JoynServiceException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: org.gsma.joyn.ft.FileTransferService.transferFileToGroup(java.lang.String, java.util.Set, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.transferFileToGroup(java.lang.String, java.util.Set, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.transferFileToGroup(java.lang.String, java.util.Set, java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransfer transferFileToGroup(java.lang.String r1, java.util.Set<java.lang.String> r2, java.lang.String r3, java.lang.String r4, int r5, org.gsma.joyn.ft.FileTransferListener r6) throws org.gsma.joyn.JoynServiceException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.transferFileToGroup(java.lang.String, java.util.Set, java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.transferFileToGroup(java.lang.String, java.util.Set, java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.transferFileToMultiple(java.util.List, java.lang.String, java.lang.String, int, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransfer transferFileToMultiple(java.util.List<java.lang.String> r1, java.lang.String r2, java.lang.String r3, int r4, java.lang.String r5, org.gsma.joyn.ft.FileTransferListener r6) throws org.gsma.joyn.JoynServiceException, org.gsma.joyn.JoynContactFormatException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.transferFileToMultiple(java.util.List, java.lang.String, java.lang.String, int, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.transferFileToMultiple(java.util.List, java.lang.String, java.lang.String, int, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: org.gsma.joyn.ft.FileTransferService.transferFileToMultiple(java.util.List, java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransfer transferFileToMultiple(java.util.List<java.lang.String> r1, java.lang.String r2, java.lang.String r3, int r4, org.gsma.joyn.ft.FileTransferListener r5) throws org.gsma.joyn.JoynServiceException, org.gsma.joyn.JoynContactFormatException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: org.gsma.joyn.ft.FileTransferService.transferFileToMultiple(java.util.List, java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.transferFileToMultiple(java.util.List, java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.transferFileToSecondaryDevice(java.lang.String, java.lang.String, int, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransfer transferFileToSecondaryDevice(java.lang.String r1, java.lang.String r2, int r3, java.lang.String r4, org.gsma.joyn.ft.FileTransferListener r5) throws org.gsma.joyn.JoynServiceException, org.gsma.joyn.JoynContactFormatException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.ft.FileTransferService.transferFileToSecondaryDevice(java.lang.String, java.lang.String, int, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.transferFileToSecondaryDevice(java.lang.String, java.lang.String, int, java.lang.String, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: org.gsma.joyn.ft.FileTransferService.transferFileToSecondaryDevice(java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    public org.gsma.joyn.ft.FileTransfer transferFileToSecondaryDevice(java.lang.String r1, java.lang.String r2, int r3, org.gsma.joyn.ft.FileTransferListener r4) throws org.gsma.joyn.JoynServiceException, org.gsma.joyn.JoynContactFormatException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: org.gsma.joyn.ft.FileTransferService.transferFileToSecondaryDevice(java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ft.FileTransferService.transferFileToSecondaryDevice(java.lang.String, java.lang.String, int, org.gsma.joyn.ft.FileTransferListener):org.gsma.joyn.ft.FileTransfer");
    }
}
