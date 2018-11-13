package android.service.media;

import android.app.Service;
import android.content.Intent;
import android.media.browse.MediaBrowser;
import android.media.browse.MediaBrowser.MediaItem;
import android.media.browse.MediaBrowserUtils;
import android.media.session.MediaSession.Token;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.service.media.IMediaBrowserService.Stub;
import android.util.ArrayMap;
import android.util.Pair;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
public abstract class MediaBrowserService extends Service {
    private static final boolean DBG = false;
    public static final String KEY_MEDIA_ITEM = "media_item";
    private static final int RESULT_FLAG_OPTION_NOT_HANDLED = 1;
    public static final String SERVICE_INTERFACE = "android.media.browse.MediaBrowserService";
    private static final String TAG = "MediaBrowserService";
    private ServiceBinder mBinder;
    private final ArrayMap<IBinder, ConnectionRecord> mConnections;
    private ConnectionRecord mCurConnection;
    private final Handler mHandler;
    Token mSession;

    /* renamed from: android.service.media.MediaBrowserService$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ MediaBrowserService this$0;
        final /* synthetic */ Token val$token;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.1.<init>(android.service.media.MediaBrowserService, android.media.session.MediaSession$Token):void, dex: 
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
        AnonymousClass1(android.service.media.MediaBrowserService r1, android.media.session.MediaSession.Token r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.1.<init>(android.service.media.MediaBrowserService, android.media.session.MediaSession$Token):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.1.<init>(android.service.media.MediaBrowserService, android.media.session.MediaSession$Token):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.1.run():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.1.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.1.run():void");
        }
    }

    /* renamed from: android.service.media.MediaBrowserService$2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ MediaBrowserService this$0;
        final /* synthetic */ Bundle val$options;
        final /* synthetic */ String val$parentId;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.2.<init>(android.service.media.MediaBrowserService, java.lang.String, android.os.Bundle):void, dex: 
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
        AnonymousClass2(android.service.media.MediaBrowserService r1, java.lang.String r2, android.os.Bundle r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.2.<init>(android.service.media.MediaBrowserService, java.lang.String, android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.2.<init>(android.service.media.MediaBrowserService, java.lang.String, android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.2.run():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.2.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.2.run():void");
        }
    }

    public class Result<T> {
        private Object mDebug;
        private boolean mDetachCalled;
        private int mFlags;
        private boolean mSendResultCalled;
        final /* synthetic */ MediaBrowserService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.Result.<init>(android.service.media.MediaBrowserService, java.lang.Object):void, dex: 
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
        Result(android.service.media.MediaBrowserService r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.Result.<init>(android.service.media.MediaBrowserService, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.Result.<init>(android.service.media.MediaBrowserService, java.lang.Object):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.service.media.MediaBrowserService.Result.detach():void, dex:  in method: android.service.media.MediaBrowserService.Result.detach():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.service.media.MediaBrowserService.Result.detach():void, dex: 
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
        public void detach() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.service.media.MediaBrowserService.Result.detach():void, dex:  in method: android.service.media.MediaBrowserService.Result.detach():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.Result.detach():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.service.media.MediaBrowserService.Result.isDone():boolean, dex: 
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
        boolean isDone() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.service.media.MediaBrowserService.Result.isDone():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.Result.isDone():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.service.media.MediaBrowserService.Result.onResultSent(java.lang.Object, int):void, dex: 
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
        void onResultSent(T r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.service.media.MediaBrowserService.Result.onResultSent(java.lang.Object, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.Result.onResultSent(java.lang.Object, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.service.media.MediaBrowserService.Result.sendResult(java.lang.Object):void, dex: 
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
        public void sendResult(T r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.service.media.MediaBrowserService.Result.sendResult(java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.Result.sendResult(java.lang.Object):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.service.media.MediaBrowserService.Result.setFlags(int):void, dex: 
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
        void setFlags(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.service.media.MediaBrowserService.Result.setFlags(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.Result.setFlags(int):void");
        }
    }

    /* renamed from: android.service.media.MediaBrowserService$3 */
    class AnonymousClass3 extends Result<List<MediaItem>> {
        final /* synthetic */ MediaBrowserService this$0;
        final /* synthetic */ ConnectionRecord val$connection;
        final /* synthetic */ Bundle val$options;
        final /* synthetic */ String val$parentId;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.3.<init>(android.service.media.MediaBrowserService, android.service.media.MediaBrowserService, java.lang.Object, android.service.media.MediaBrowserService$ConnectionRecord, java.lang.String, android.os.Bundle):void, dex: 
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
        AnonymousClass3(android.service.media.MediaBrowserService r1, android.service.media.MediaBrowserService r2, java.lang.Object r3, android.service.media.MediaBrowserService.ConnectionRecord r4, java.lang.String r5, android.os.Bundle r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.3.<init>(android.service.media.MediaBrowserService, android.service.media.MediaBrowserService, java.lang.Object, android.service.media.MediaBrowserService$ConnectionRecord, java.lang.String, android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.3.<init>(android.service.media.MediaBrowserService, android.service.media.MediaBrowserService, java.lang.Object, android.service.media.MediaBrowserService$ConnectionRecord, java.lang.String, android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.media.MediaBrowserService.3.onResultSent(java.lang.Object, int):void, dex: 
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
        /* bridge */ /* synthetic */ void onResultSent(java.lang.Object r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.media.MediaBrowserService.3.onResultSent(java.lang.Object, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.3.onResultSent(java.lang.Object, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.3.onResultSent(java.util.List, int):void, dex: 
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
        void onResultSent(java.util.List<android.media.browse.MediaBrowser.MediaItem> r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.3.onResultSent(java.util.List, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.3.onResultSent(java.util.List, int):void");
        }
    }

    /* renamed from: android.service.media.MediaBrowserService$4 */
    class AnonymousClass4 extends Result<MediaItem> {
        final /* synthetic */ MediaBrowserService this$0;
        final /* synthetic */ ResultReceiver val$receiver;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.4.<init>(android.service.media.MediaBrowserService, android.service.media.MediaBrowserService, java.lang.Object, android.os.ResultReceiver):void, dex: 
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
        AnonymousClass4(android.service.media.MediaBrowserService r1, android.service.media.MediaBrowserService r2, java.lang.Object r3, android.os.ResultReceiver r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.4.<init>(android.service.media.MediaBrowserService, android.service.media.MediaBrowserService, java.lang.Object, android.os.ResultReceiver):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.4.<init>(android.service.media.MediaBrowserService, android.service.media.MediaBrowserService, java.lang.Object, android.os.ResultReceiver):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.media.MediaBrowserService.4.onResultSent(android.media.browse.MediaBrowser$MediaItem, int):void, dex: 
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
        void onResultSent(android.media.browse.MediaBrowser.MediaItem r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.media.MediaBrowserService.4.onResultSent(android.media.browse.MediaBrowser$MediaItem, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.4.onResultSent(android.media.browse.MediaBrowser$MediaItem, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.media.MediaBrowserService.4.onResultSent(java.lang.Object, int):void, dex: 
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
        /* bridge */ /* synthetic */ void onResultSent(java.lang.Object r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.media.MediaBrowserService.4.onResultSent(java.lang.Object, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.4.onResultSent(java.lang.Object, int):void");
        }
    }

    public static final class BrowserRoot {
        public static final String EXTRA_OFFLINE = "android.service.media.extra.OFFLINE";
        public static final String EXTRA_RECENT = "android.service.media.extra.RECENT";
        public static final String EXTRA_SUGGESTED = "android.service.media.extra.SUGGESTED";
        private final Bundle mExtras;
        private final String mRootId;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.BrowserRoot.<init>(java.lang.String, android.os.Bundle):void, dex: 
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
        public BrowserRoot(java.lang.String r1, android.os.Bundle r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.BrowserRoot.<init>(java.lang.String, android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.BrowserRoot.<init>(java.lang.String, android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.BrowserRoot.getExtras():android.os.Bundle, dex: 
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
        public android.os.Bundle getExtras() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.BrowserRoot.getExtras():android.os.Bundle, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.BrowserRoot.getExtras():android.os.Bundle");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.BrowserRoot.getRootId():java.lang.String, dex: 
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
        public java.lang.String getRootId() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.BrowserRoot.getRootId():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.BrowserRoot.getRootId():java.lang.String");
        }
    }

    private class ConnectionRecord {
        IMediaBrowserServiceCallbacks callbacks;
        String pkg;
        BrowserRoot root;
        Bundle rootHints;
        HashMap<String, List<Pair<IBinder, Bundle>>> subscriptions;
        final /* synthetic */ MediaBrowserService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.service.media.MediaBrowserService.ConnectionRecord.<init>(android.service.media.MediaBrowserService):void, dex:  in method: android.service.media.MediaBrowserService.ConnectionRecord.<init>(android.service.media.MediaBrowserService):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.service.media.MediaBrowserService.ConnectionRecord.<init>(android.service.media.MediaBrowserService):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private ConnectionRecord(android.service.media.MediaBrowserService r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.service.media.MediaBrowserService.ConnectionRecord.<init>(android.service.media.MediaBrowserService):void, dex:  in method: android.service.media.MediaBrowserService.ConnectionRecord.<init>(android.service.media.MediaBrowserService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ConnectionRecord.<init>(android.service.media.MediaBrowserService):void");
        }

        /* synthetic */ ConnectionRecord(MediaBrowserService this$0, ConnectionRecord connectionRecord) {
            this(this$0);
        }
    }

    private class ServiceBinder extends Stub {
        final /* synthetic */ MediaBrowserService this$0;

        /* renamed from: android.service.media.MediaBrowserService$ServiceBinder$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ ServiceBinder this$1;
            final /* synthetic */ IMediaBrowserServiceCallbacks val$callbacks;
            final /* synthetic */ String val$pkg;
            final /* synthetic */ Bundle val$rootHints;
            final /* synthetic */ int val$uid;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.ServiceBinder.1.<init>(android.service.media.MediaBrowserService$ServiceBinder, android.service.media.IMediaBrowserServiceCallbacks, java.lang.String, android.os.Bundle, int):void, dex: 
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
            AnonymousClass1(android.service.media.MediaBrowserService.ServiceBinder r1, android.service.media.IMediaBrowserServiceCallbacks r2, java.lang.String r3, android.os.Bundle r4, int r5) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.ServiceBinder.1.<init>(android.service.media.MediaBrowserService$ServiceBinder, android.service.media.IMediaBrowserServiceCallbacks, java.lang.String, android.os.Bundle, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.1.<init>(android.service.media.MediaBrowserService$ServiceBinder, android.service.media.IMediaBrowserServiceCallbacks, java.lang.String, android.os.Bundle, int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.service.media.MediaBrowserService.ServiceBinder.1.run():void, dex:  in method: android.service.media.MediaBrowserService.ServiceBinder.1.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.service.media.MediaBrowserService.ServiceBinder.1.run():void, dex: 
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
            public void run() {
                /*
                // Can't load method instructions: Load method exception: null in method: android.service.media.MediaBrowserService.ServiceBinder.1.run():void, dex:  in method: android.service.media.MediaBrowserService.ServiceBinder.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.1.run():void");
            }
        }

        /* renamed from: android.service.media.MediaBrowserService$ServiceBinder$2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ ServiceBinder this$1;
            final /* synthetic */ IMediaBrowserServiceCallbacks val$callbacks;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.ServiceBinder.2.<init>(android.service.media.MediaBrowserService$ServiceBinder, android.service.media.IMediaBrowserServiceCallbacks):void, dex: 
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
            AnonymousClass2(android.service.media.MediaBrowserService.ServiceBinder r1, android.service.media.IMediaBrowserServiceCallbacks r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.ServiceBinder.2.<init>(android.service.media.MediaBrowserService$ServiceBinder, android.service.media.IMediaBrowserServiceCallbacks):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.2.<init>(android.service.media.MediaBrowserService$ServiceBinder, android.service.media.IMediaBrowserServiceCallbacks):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.ServiceBinder.2.run():void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.ServiceBinder.2.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.2.run():void");
            }
        }

        /* renamed from: android.service.media.MediaBrowserService$ServiceBinder$3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ ServiceBinder this$1;
            final /* synthetic */ IMediaBrowserServiceCallbacks val$callbacks;
            final /* synthetic */ String val$id;
            final /* synthetic */ Bundle val$options;
            final /* synthetic */ IBinder val$token;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.ServiceBinder.3.<init>(android.service.media.MediaBrowserService$ServiceBinder, android.service.media.IMediaBrowserServiceCallbacks, java.lang.String, android.os.IBinder, android.os.Bundle):void, dex: 
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
            AnonymousClass3(android.service.media.MediaBrowserService.ServiceBinder r1, android.service.media.IMediaBrowserServiceCallbacks r2, java.lang.String r3, android.os.IBinder r4, android.os.Bundle r5) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.ServiceBinder.3.<init>(android.service.media.MediaBrowserService$ServiceBinder, android.service.media.IMediaBrowserServiceCallbacks, java.lang.String, android.os.IBinder, android.os.Bundle):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.3.<init>(android.service.media.MediaBrowserService$ServiceBinder, android.service.media.IMediaBrowserServiceCallbacks, java.lang.String, android.os.IBinder, android.os.Bundle):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.ServiceBinder.3.run():void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.ServiceBinder.3.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.3.run():void");
            }
        }

        /* renamed from: android.service.media.MediaBrowserService$ServiceBinder$4 */
        class AnonymousClass4 implements Runnable {
            final /* synthetic */ ServiceBinder this$1;
            final /* synthetic */ IMediaBrowserServiceCallbacks val$callbacks;
            final /* synthetic */ String val$id;
            final /* synthetic */ IBinder val$token;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.ServiceBinder.4.<init>(android.service.media.MediaBrowserService$ServiceBinder, android.service.media.IMediaBrowserServiceCallbacks, java.lang.String, android.os.IBinder):void, dex: 
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
            AnonymousClass4(android.service.media.MediaBrowserService.ServiceBinder r1, android.service.media.IMediaBrowserServiceCallbacks r2, java.lang.String r3, android.os.IBinder r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.ServiceBinder.4.<init>(android.service.media.MediaBrowserService$ServiceBinder, android.service.media.IMediaBrowserServiceCallbacks, java.lang.String, android.os.IBinder):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.4.<init>(android.service.media.MediaBrowserService$ServiceBinder, android.service.media.IMediaBrowserServiceCallbacks, java.lang.String, android.os.IBinder):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.ServiceBinder.4.run():void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.ServiceBinder.4.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.4.run():void");
            }
        }

        /* renamed from: android.service.media.MediaBrowserService$ServiceBinder$5 */
        class AnonymousClass5 implements Runnable {
            final /* synthetic */ ServiceBinder this$1;
            final /* synthetic */ IMediaBrowserServiceCallbacks val$callbacks;
            final /* synthetic */ String val$mediaId;
            final /* synthetic */ ResultReceiver val$receiver;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.ServiceBinder.5.<init>(android.service.media.MediaBrowserService$ServiceBinder, android.service.media.IMediaBrowserServiceCallbacks, java.lang.String, android.os.ResultReceiver):void, dex: 
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
            AnonymousClass5(android.service.media.MediaBrowserService.ServiceBinder r1, android.service.media.IMediaBrowserServiceCallbacks r2, java.lang.String r3, android.os.ResultReceiver r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.ServiceBinder.5.<init>(android.service.media.MediaBrowserService$ServiceBinder, android.service.media.IMediaBrowserServiceCallbacks, java.lang.String, android.os.ResultReceiver):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.5.<init>(android.service.media.MediaBrowserService$ServiceBinder, android.service.media.IMediaBrowserServiceCallbacks, java.lang.String, android.os.ResultReceiver):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.ServiceBinder.5.run():void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.ServiceBinder.5.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.5.run():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.ServiceBinder.<init>(android.service.media.MediaBrowserService):void, dex: 
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
        private ServiceBinder(android.service.media.MediaBrowserService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.media.MediaBrowserService.ServiceBinder.<init>(android.service.media.MediaBrowserService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.<init>(android.service.media.MediaBrowserService):void");
        }

        /* synthetic */ ServiceBinder(MediaBrowserService this$0, ServiceBinder serviceBinder) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.ServiceBinder.addSubscription(java.lang.String, android.os.IBinder, android.os.Bundle, android.service.media.IMediaBrowserServiceCallbacks):void, dex: 
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
        public void addSubscription(java.lang.String r1, android.os.IBinder r2, android.os.Bundle r3, android.service.media.IMediaBrowserServiceCallbacks r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.ServiceBinder.addSubscription(java.lang.String, android.os.IBinder, android.os.Bundle, android.service.media.IMediaBrowserServiceCallbacks):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.addSubscription(java.lang.String, android.os.IBinder, android.os.Bundle, android.service.media.IMediaBrowserServiceCallbacks):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.service.media.MediaBrowserService.ServiceBinder.addSubscriptionDeprecated(java.lang.String, android.service.media.IMediaBrowserServiceCallbacks):void, dex: 
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
        public void addSubscriptionDeprecated(java.lang.String r1, android.service.media.IMediaBrowserServiceCallbacks r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.service.media.MediaBrowserService.ServiceBinder.addSubscriptionDeprecated(java.lang.String, android.service.media.IMediaBrowserServiceCallbacks):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.addSubscriptionDeprecated(java.lang.String, android.service.media.IMediaBrowserServiceCallbacks):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.ServiceBinder.connect(java.lang.String, android.os.Bundle, android.service.media.IMediaBrowserServiceCallbacks):void, dex: 
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
        public void connect(java.lang.String r1, android.os.Bundle r2, android.service.media.IMediaBrowserServiceCallbacks r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.ServiceBinder.connect(java.lang.String, android.os.Bundle, android.service.media.IMediaBrowserServiceCallbacks):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.connect(java.lang.String, android.os.Bundle, android.service.media.IMediaBrowserServiceCallbacks):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.ServiceBinder.disconnect(android.service.media.IMediaBrowserServiceCallbacks):void, dex: 
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
        public void disconnect(android.service.media.IMediaBrowserServiceCallbacks r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.ServiceBinder.disconnect(android.service.media.IMediaBrowserServiceCallbacks):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.disconnect(android.service.media.IMediaBrowserServiceCallbacks):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.service.media.MediaBrowserService.ServiceBinder.getMediaItem(java.lang.String, android.os.ResultReceiver, android.service.media.IMediaBrowserServiceCallbacks):void, dex: 
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
        public void getMediaItem(java.lang.String r1, android.os.ResultReceiver r2, android.service.media.IMediaBrowserServiceCallbacks r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.service.media.MediaBrowserService.ServiceBinder.getMediaItem(java.lang.String, android.os.ResultReceiver, android.service.media.IMediaBrowserServiceCallbacks):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.getMediaItem(java.lang.String, android.os.ResultReceiver, android.service.media.IMediaBrowserServiceCallbacks):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.ServiceBinder.removeSubscription(java.lang.String, android.os.IBinder, android.service.media.IMediaBrowserServiceCallbacks):void, dex: 
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
        public void removeSubscription(java.lang.String r1, android.os.IBinder r2, android.service.media.IMediaBrowserServiceCallbacks r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.media.MediaBrowserService.ServiceBinder.removeSubscription(java.lang.String, android.os.IBinder, android.service.media.IMediaBrowserServiceCallbacks):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.removeSubscription(java.lang.String, android.os.IBinder, android.service.media.IMediaBrowserServiceCallbacks):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.service.media.MediaBrowserService.ServiceBinder.removeSubscriptionDeprecated(java.lang.String, android.service.media.IMediaBrowserServiceCallbacks):void, dex: 
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
        public void removeSubscriptionDeprecated(java.lang.String r1, android.service.media.IMediaBrowserServiceCallbacks r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.service.media.MediaBrowserService.ServiceBinder.removeSubscriptionDeprecated(java.lang.String, android.service.media.IMediaBrowserServiceCallbacks):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.ServiceBinder.removeSubscriptionDeprecated(java.lang.String, android.service.media.IMediaBrowserServiceCallbacks):void");
        }
    }

    public abstract BrowserRoot onGetRoot(String str, int i, Bundle bundle);

    public abstract void onLoadChildren(String str, Result<List<MediaItem>> result);

    public MediaBrowserService() {
        this.mConnections = new ArrayMap();
        this.mHandler = new Handler();
    }

    public void onCreate() {
        super.onCreate();
        this.mBinder = new ServiceBinder(this, null);
    }

    public IBinder onBind(Intent intent) {
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            return this.mBinder;
        }
        return null;
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
    }

    public void onLoadChildren(String parentId, Result<List<MediaItem>> result, Bundle options) {
        result.setFlags(1);
        onLoadChildren(parentId, result);
    }

    public void onLoadItem(String itemId, Result<MediaItem> result) {
        result.sendResult(null);
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
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public void setSessionToken(android.media.session.MediaSession.Token r3) {
        /*
        r2 = this;
        if (r3 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "Session token may not be null.";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        r0 = r2.mSession;
        if (r0 == 0) goto L_0x0018;
    L_0x000f:
        r0 = new java.lang.IllegalStateException;
        r1 = "The session token has already been set.";
        r0.<init>(r1);
        throw r0;
    L_0x0018:
        r2.mSession = r3;
        r0 = r2.mHandler;
        r1 = new android.service.media.MediaBrowserService$1;
        r1.<init>(r2, r3);
        r0.post(r1);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.setSessionToken(android.media.session.MediaSession$Token):void");
    }

    public Token getSessionToken() {
        return this.mSession;
    }

    public final Bundle getBrowserRootHints() {
        if (this.mCurConnection == null) {
            throw new IllegalStateException("This should be called inside of onLoadChildren or onLoadItem methods");
        } else if (this.mCurConnection.rootHints == null) {
            return null;
        } else {
            return new Bundle(this.mCurConnection.rootHints);
        }
    }

    public void notifyChildrenChanged(String parentId) {
        notifyChildrenChangedInternal(parentId, null);
    }

    public void notifyChildrenChanged(String parentId, Bundle options) {
        if (options == null) {
            throw new IllegalArgumentException("options cannot be null in notifyChildrenChanged");
        }
        notifyChildrenChangedInternal(parentId, options);
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
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private void notifyChildrenChangedInternal(java.lang.String r3, android.os.Bundle r4) {
        /*
        r2 = this;
        if (r3 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "parentId cannot be null in notifyChildrenChanged";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        r0 = r2.mHandler;
        r1 = new android.service.media.MediaBrowserService$2;
        r1.<init>(r2, r3, r4);
        r0.post(r1);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.notifyChildrenChangedInternal(java.lang.String, android.os.Bundle):void");
    }

    private boolean isValidPackage(String pkg, int uid) {
        if (pkg == null) {
            return false;
        }
        for (String equals : getPackageManager().getPackagesForUid(uid)) {
            if (equals.equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    private void addSubscription(String id, ConnectionRecord connection, IBinder token, Bundle options) {
        List<Pair<IBinder, Bundle>> callbackList = (List) connection.subscriptions.get(id);
        if (callbackList == null) {
            callbackList = new ArrayList();
        }
        for (Pair<IBinder, Bundle> callback : callbackList) {
            if (token == callback.first && MediaBrowserUtils.areSameOptions(options, (Bundle) callback.second)) {
                return;
            }
        }
        callbackList.add(new Pair(token, options));
        connection.subscriptions.put(id, callbackList);
        performLoadChildren(id, connection, options);
    }

    private boolean removeSubscription(String id, ConnectionRecord connection, IBinder token) {
        boolean z = false;
        if (token == null) {
            if (connection.subscriptions.remove(id) != null) {
                z = true;
            }
            return z;
        }
        boolean removed = false;
        List<Pair<IBinder, Bundle>> callbackList = (List) connection.subscriptions.get(id);
        if (callbackList != null) {
            for (Pair<IBinder, Bundle> callback : callbackList) {
                if (token == callback.first) {
                    removed = true;
                    callbackList.remove(callback);
                }
            }
            if (callbackList.size() == 0) {
                connection.subscriptions.remove(id);
            }
        }
        return removed;
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
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private void performLoadChildren(java.lang.String r9, android.service.media.MediaBrowserService.ConnectionRecord r10, android.os.Bundle r11) {
        /*
        r8 = this;
        r7 = 0;
        r0 = new android.service.media.MediaBrowserService$3;
        r1 = r8;
        r2 = r8;
        r3 = r9;
        r4 = r10;
        r5 = r9;
        r6 = r11;
        r0.<init>(r1, r2, r3, r4, r5, r6);
        r8.mCurConnection = r10;
        if (r11 != 0) goto L_0x0042;
    L_0x0010:
        r8.onLoadChildren(r9, r0);
    L_0x0013:
        r8.mCurConnection = r7;
        r1 = r0.isDone();
        if (r1 != 0) goto L_0x0046;
    L_0x001b:
        r1 = new java.lang.IllegalStateException;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "onLoadChildren must call detach() or sendResult() before returning for package=";
        r2 = r2.append(r3);
        r3 = r10.pkg;
        r2 = r2.append(r3);
        r3 = " id=";
        r2 = r2.append(r3);
        r2 = r2.append(r9);
        r2 = r2.toString();
        r1.<init>(r2);
        throw r1;
    L_0x0042:
        r8.onLoadChildren(r9, r0, r11);
        goto L_0x0013;
    L_0x0046:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.performLoadChildren(java.lang.String, android.service.media.MediaBrowserService$ConnectionRecord, android.os.Bundle):void");
    }

    private List<MediaItem> applyOptions(List<MediaItem> list, Bundle options) {
        if (list == null) {
            return null;
        }
        int page = options.getInt(MediaBrowser.EXTRA_PAGE, -1);
        int pageSize = options.getInt(MediaBrowser.EXTRA_PAGE_SIZE, -1);
        if (page == -1 && pageSize == -1) {
            return list;
        }
        int fromIndex = pageSize * page;
        int toIndex = fromIndex + pageSize;
        if (page < 0 || pageSize < 1 || fromIndex >= list.size()) {
            return Collections.EMPTY_LIST;
        }
        if (toIndex > list.size()) {
            toIndex = list.size();
        }
        return list.subList(fromIndex, toIndex);
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
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private void performLoadItem(java.lang.String r5, android.service.media.MediaBrowserService.ConnectionRecord r6, android.os.ResultReceiver r7) {
        /*
        r4 = this;
        r0 = new android.service.media.MediaBrowserService$4;
        r0.<init>(r4, r4, r5, r7);
        r4.mCurConnection = r6;
        r4.onLoadItem(r5, r0);
        r1 = 0;
        r4.mCurConnection = r1;
        r1 = r0.isDone();
        if (r1 != 0) goto L_0x002d;
    L_0x0013:
        r1 = new java.lang.IllegalStateException;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "onLoadItem must call detach() or sendResult() before returning for id=";
        r2 = r2.append(r3);
        r2 = r2.append(r5);
        r2 = r2.toString();
        r1.<init>(r2);
        throw r1;
    L_0x002d:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.media.MediaBrowserService.performLoadItem(java.lang.String, android.service.media.MediaBrowserService$ConnectionRecord, android.os.ResultReceiver):void");
    }
}
