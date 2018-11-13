package android.speech.tts;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.speech.tts.ITextToSpeechService.Stub;
import android.speech.tts.TextToSpeech.Engine;
import android.util.Log;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;

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
public abstract class TextToSpeechService extends Service {
    private static final boolean DBG = false;
    private static final String SYNTH_THREAD_NAME = "SynthThread";
    private static final String TAG = "TextToSpeechService";
    private AudioPlaybackHandler mAudioPlaybackHandler;
    private final Stub mBinder;
    private CallbackMap mCallbacks;
    private TtsEngines mEngineHelper;
    private String mPackageName;
    private SynthHandler mSynthHandler;
    private final Object mVoicesInfoLock;

    static class AudioOutputParams {
        public final AudioAttributes mAudioAttributes;
        public final float mPan;
        public final int mSessionId;
        public final float mVolume;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.speech.tts.TextToSpeechService.AudioOutputParams.<init>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AudioOutputParams() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.speech.tts.TextToSpeechService.AudioOutputParams.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.AudioOutputParams.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.speech.tts.TextToSpeechService.AudioOutputParams.<init>(int, float, float, android.media.AudioAttributes):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AudioOutputParams(int r1, float r2, float r3, android.media.AudioAttributes r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.speech.tts.TextToSpeechService.AudioOutputParams.<init>(int, float, float, android.media.AudioAttributes):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.AudioOutputParams.<init>(int, float, float, android.media.AudioAttributes):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.AudioOutputParams.createFromV1ParamsBundle(android.os.Bundle, boolean):android.speech.tts.TextToSpeechService$AudioOutputParams, dex: 
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
        static android.speech.tts.TextToSpeechService.AudioOutputParams createFromV1ParamsBundle(android.os.Bundle r1, boolean r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.AudioOutputParams.createFromV1ParamsBundle(android.os.Bundle, boolean):android.speech.tts.TextToSpeechService$AudioOutputParams, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.AudioOutputParams.createFromV1ParamsBundle(android.os.Bundle, boolean):android.speech.tts.TextToSpeechService$AudioOutputParams");
        }
    }

    private abstract class SpeechItem {
        private final Object mCallerIdentity;
        private final int mCallerPid;
        private final int mCallerUid;
        private boolean mStarted;
        private boolean mStopped;
        final /* synthetic */ TextToSpeechService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeechService.SpeechItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int):void, dex: 
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
        public SpeechItem(android.speech.tts.TextToSpeechService r1, java.lang.Object r2, int r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeechService.SpeechItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SpeechItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SpeechItem.getCallerIdentity():java.lang.Object, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.Object getCallerIdentity() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SpeechItem.getCallerIdentity():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SpeechItem.getCallerIdentity():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.speech.tts.TextToSpeechService.SpeechItem.getCallerPid():int, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public int getCallerPid() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.speech.tts.TextToSpeechService.SpeechItem.getCallerPid():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SpeechItem.getCallerPid():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.speech.tts.TextToSpeechService.SpeechItem.getCallerUid():int, dex:  in method: android.speech.tts.TextToSpeechService.SpeechItem.getCallerUid():int, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.speech.tts.TextToSpeechService.SpeechItem.getCallerUid():int, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public int getCallerUid() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.speech.tts.TextToSpeechService.SpeechItem.getCallerUid():int, dex:  in method: android.speech.tts.TextToSpeechService.SpeechItem.getCallerUid():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SpeechItem.getCallerUid():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.speech.tts.TextToSpeechService.SpeechItem.isStarted():boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        protected synchronized boolean isStarted() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.speech.tts.TextToSpeechService.SpeechItem.isStarted():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SpeechItem.isStarted():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.speech.tts.TextToSpeechService.SpeechItem.isStopped():boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        protected synchronized boolean isStopped() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.speech.tts.TextToSpeechService.SpeechItem.isStopped():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SpeechItem.isStopped():boolean");
        }

        public abstract boolean isValid();

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.speech.tts.TextToSpeechService.SpeechItem.play():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void play() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.speech.tts.TextToSpeechService.SpeechItem.play():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SpeechItem.play():void");
        }

        protected abstract void playImpl();

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.speech.tts.TextToSpeechService.SpeechItem.stop():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void stop() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.speech.tts.TextToSpeechService.SpeechItem.stop():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SpeechItem.stop():void");
        }

        protected abstract void stopImpl();
    }

    interface UtteranceProgressDispatcher {
        void dispatchOnAudioAvailable(byte[] bArr);

        void dispatchOnBeginSynthesis(int i, int i2, int i3);

        void dispatchOnError(int i);

        void dispatchOnStart();

        void dispatchOnStop();

        void dispatchOnSuccess();
    }

    private abstract class UtteranceSpeechItem extends SpeechItem implements UtteranceProgressDispatcher {
        final /* synthetic */ TextToSpeechService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int):void, dex: 
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
        public UtteranceSpeechItem(android.speech.tts.TextToSpeechService r1, java.lang.Object r2, int r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnAudioAvailable(byte[]):void, dex: 
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
        public void dispatchOnAudioAvailable(byte[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnAudioAvailable(byte[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnAudioAvailable(byte[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnBeginSynthesis(int, int, int):void, dex:  in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnBeginSynthesis(int, int, int):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnBeginSynthesis(int, int, int):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:72)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public void dispatchOnBeginSynthesis(int r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnBeginSynthesis(int, int, int):void, dex:  in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnBeginSynthesis(int, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnBeginSynthesis(int, int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnError(int):void, dex: 
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
        public void dispatchOnError(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnError(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnError(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnStart():void, dex: 
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
        public void dispatchOnStart() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnStart():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnStart():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnStop():void, dex: 
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
        public void dispatchOnStop() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnStop():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnStop():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnSuccess():void, dex: 
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
        public void dispatchOnSuccess() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnSuccess():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.dispatchOnSuccess():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.getFloatParam(android.os.Bundle, java.lang.String, float):float, dex: 
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
        float getFloatParam(android.os.Bundle r1, java.lang.String r2, float r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.getFloatParam(android.os.Bundle, java.lang.String, float):float, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.getFloatParam(android.os.Bundle, java.lang.String, float):float");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.getIntParam(android.os.Bundle, java.lang.String, int):int, dex: 
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
        int getIntParam(android.os.Bundle r1, java.lang.String r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.getIntParam(android.os.Bundle, java.lang.String, int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.getIntParam(android.os.Bundle, java.lang.String, int):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.getStringParam(android.os.Bundle, java.lang.String, java.lang.String):java.lang.String, dex: 
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
        java.lang.String getStringParam(android.os.Bundle r1, java.lang.String r2, java.lang.String r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.getStringParam(android.os.Bundle, java.lang.String, java.lang.String):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.UtteranceSpeechItem.getStringParam(android.os.Bundle, java.lang.String, java.lang.String):java.lang.String");
        }

        public abstract String getUtteranceId();
    }

    private abstract class SpeechItemV1 extends UtteranceSpeechItem {
        protected final Bundle mParams;
        protected final String mUtteranceId;
        final /* synthetic */ TextToSpeechService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.speech.tts.TextToSpeechService.SpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String):void, dex:  in method: android.speech.tts.TextToSpeechService.SpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.speech.tts.TextToSpeechService.SpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        SpeechItemV1(android.speech.tts.TextToSpeechService r1, java.lang.Object r2, int r3, int r4, android.os.Bundle r5, java.lang.String r6) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.speech.tts.TextToSpeechService.SpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String):void, dex:  in method: android.speech.tts.TextToSpeechService.SpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SpeechItemV1.getAudioParams():android.speech.tts.TextToSpeechService$AudioOutputParams, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        android.speech.tts.TextToSpeechService.AudioOutputParams getAudioParams() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SpeechItemV1.getAudioParams():android.speech.tts.TextToSpeechService$AudioOutputParams, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SpeechItemV1.getAudioParams():android.speech.tts.TextToSpeechService$AudioOutputParams");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SpeechItemV1.getPitch():int, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        int getPitch() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SpeechItemV1.getPitch():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SpeechItemV1.getPitch():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SpeechItemV1.getSpeechRate():int, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        int getSpeechRate() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SpeechItemV1.getSpeechRate():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SpeechItemV1.getSpeechRate():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.speech.tts.TextToSpeechService.SpeechItemV1.getUtteranceId():java.lang.String, dex:  in method: android.speech.tts.TextToSpeechService.SpeechItemV1.getUtteranceId():java.lang.String, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.speech.tts.TextToSpeechService.SpeechItemV1.getUtteranceId():java.lang.String, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public java.lang.String getUtteranceId() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.speech.tts.TextToSpeechService.SpeechItemV1.getUtteranceId():java.lang.String, dex:  in method: android.speech.tts.TextToSpeechService.SpeechItemV1.getUtteranceId():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SpeechItemV1.getUtteranceId():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SpeechItemV1.hasLanguage():boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        boolean hasLanguage() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SpeechItemV1.hasLanguage():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SpeechItemV1.hasLanguage():boolean");
        }
    }

    private class AudioSpeechItemV1 extends SpeechItemV1 {
        private final AudioPlaybackQueueItem mItem;
        final /* synthetic */ TextToSpeechService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String, android.net.Uri):void, dex:  in method: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String, android.net.Uri):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String, android.net.Uri):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$23.decode(InstructionCodec.java:514)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public AudioSpeechItemV1(android.speech.tts.TextToSpeechService r1, java.lang.Object r2, int r3, int r4, android.os.Bundle r5, java.lang.String r6, android.net.Uri r7) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String, android.net.Uri):void, dex:  in method: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String, android.net.Uri):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String, android.net.Uri):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.getAudioParams():android.speech.tts.TextToSpeechService$AudioOutputParams, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        android.speech.tts.TextToSpeechService.AudioOutputParams getAudioParams() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.getAudioParams():android.speech.tts.TextToSpeechService$AudioOutputParams, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.getAudioParams():android.speech.tts.TextToSpeechService$AudioOutputParams");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.getUtteranceId():java.lang.String, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.String getUtteranceId() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.getUtteranceId():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.getUtteranceId():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.playImpl():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        protected void playImpl() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.playImpl():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.playImpl():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.stopImpl():void, dex: 
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
        protected void stopImpl() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.stopImpl():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.AudioSpeechItemV1.stopImpl():void");
        }

        public boolean isValid() {
            return true;
        }
    }

    private class CallbackMap extends RemoteCallbackList<ITextToSpeechCallback> {
        private final HashMap<IBinder, ITextToSpeechCallback> mCallerToCallback;
        final /* synthetic */ TextToSpeechService this$0;

        /* synthetic */ CallbackMap(TextToSpeechService this$0, CallbackMap callbackMap) {
            this(this$0);
        }

        private CallbackMap(TextToSpeechService this$0) {
            this.this$0 = this$0;
            this.mCallerToCallback = new HashMap();
        }

        public void setCallback(IBinder caller, ITextToSpeechCallback cb) {
            synchronized (this.mCallerToCallback) {
                ITextToSpeechCallback old;
                if (cb != null) {
                    register(cb, caller);
                    old = (ITextToSpeechCallback) this.mCallerToCallback.put(caller, cb);
                } else {
                    old = (ITextToSpeechCallback) this.mCallerToCallback.remove(caller);
                }
                if (!(old == null || old == cb)) {
                    unregister(old);
                }
            }
        }

        public void dispatchOnStop(Object callerIdentity, String utteranceId, boolean started) {
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb != null) {
                try {
                    cb.onStop(utteranceId, started);
                } catch (RemoteException e) {
                    Log.e(TextToSpeechService.TAG, "Callback onStop failed: " + e);
                }
            }
        }

        public void dispatchOnSuccess(Object callerIdentity, String utteranceId) {
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb != null) {
                try {
                    cb.onSuccess(utteranceId);
                } catch (RemoteException e) {
                    Log.e(TextToSpeechService.TAG, "Callback onDone failed: " + e);
                }
            }
        }

        public void dispatchOnStart(Object callerIdentity, String utteranceId) {
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb != null) {
                try {
                    cb.onStart(utteranceId);
                } catch (RemoteException e) {
                    Log.e(TextToSpeechService.TAG, "Callback onStart failed: " + e);
                }
            }
        }

        public void dispatchOnError(Object callerIdentity, String utteranceId, int errorCode) {
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb != null) {
                try {
                    cb.onError(utteranceId, errorCode);
                } catch (RemoteException e) {
                    Log.e(TextToSpeechService.TAG, "Callback onError failed: " + e);
                }
            }
        }

        public void dispatchOnBeginSynthesis(Object callerIdentity, String utteranceId, int sampleRateInHz, int audioFormat, int channelCount) {
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb != null) {
                try {
                    cb.onBeginSynthesis(utteranceId, sampleRateInHz, audioFormat, channelCount);
                } catch (RemoteException e) {
                    Log.e(TextToSpeechService.TAG, "Callback dispatchOnBeginSynthesis(String, int, int, int) failed: " + e);
                }
            }
        }

        public void dispatchOnAudioAvailable(Object callerIdentity, String utteranceId, byte[] buffer) {
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb != null) {
                try {
                    cb.onAudioAvailable(utteranceId, buffer);
                } catch (RemoteException e) {
                    Log.e(TextToSpeechService.TAG, "Callback dispatchOnAudioAvailable(String, byte[]) failed: " + e);
                }
            }
        }

        public /* bridge */ /* synthetic */ void onCallbackDied(IInterface callback, Object cookie) {
            onCallbackDied((ITextToSpeechCallback) callback, cookie);
        }

        public void onCallbackDied(ITextToSpeechCallback callback, Object cookie) {
            IBinder caller = (IBinder) cookie;
            synchronized (this.mCallerToCallback) {
                this.mCallerToCallback.remove(caller);
            }
        }

        public void kill() {
            synchronized (this.mCallerToCallback) {
                this.mCallerToCallback.clear();
                super.kill();
            }
        }

        private ITextToSpeechCallback getCallbackFor(Object caller) {
            ITextToSpeechCallback cb;
            IBinder asBinder = (IBinder) caller;
            synchronized (this.mCallerToCallback) {
                cb = (ITextToSpeechCallback) this.mCallerToCallback.get(asBinder);
            }
            return cb;
        }
    }

    private class LoadLanguageItem extends SpeechItem {
        private final String mCountry;
        private final String mLanguage;
        private final String mVariant;
        final /* synthetic */ TextToSpeechService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.speech.tts.TextToSpeechService.LoadLanguageItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, java.lang.String, java.lang.String, java.lang.String):void, dex:  in method: android.speech.tts.TextToSpeechService.LoadLanguageItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, java.lang.String, java.lang.String, java.lang.String):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.speech.tts.TextToSpeechService.LoadLanguageItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, java.lang.String, java.lang.String, java.lang.String):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public LoadLanguageItem(android.speech.tts.TextToSpeechService r1, java.lang.Object r2, int r3, int r4, java.lang.String r5, java.lang.String r6, java.lang.String r7) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.speech.tts.TextToSpeechService.LoadLanguageItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, java.lang.String, java.lang.String, java.lang.String):void, dex:  in method: android.speech.tts.TextToSpeechService.LoadLanguageItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, java.lang.String, java.lang.String, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.LoadLanguageItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, java.lang.String, java.lang.String, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.LoadLanguageItem.playImpl():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        protected void playImpl() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.LoadLanguageItem.playImpl():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.LoadLanguageItem.playImpl():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.speech.tts.TextToSpeechService.LoadLanguageItem.stopImpl():void, dex: 
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
        protected void stopImpl() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.speech.tts.TextToSpeechService.LoadLanguageItem.stopImpl():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.LoadLanguageItem.stopImpl():void");
        }

        public boolean isValid() {
            return true;
        }
    }

    private class LoadVoiceItem extends SpeechItem {
        private final String mVoiceName;
        final /* synthetic */ TextToSpeechService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeechService.LoadVoiceItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, java.lang.String):void, dex: 
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
        public LoadVoiceItem(android.speech.tts.TextToSpeechService r1, java.lang.Object r2, int r3, int r4, java.lang.String r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeechService.LoadVoiceItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.LoadVoiceItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.LoadVoiceItem.playImpl():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        protected void playImpl() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.LoadVoiceItem.playImpl():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.LoadVoiceItem.playImpl():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.speech.tts.TextToSpeechService.LoadVoiceItem.stopImpl():void, dex: 
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
        protected void stopImpl() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.speech.tts.TextToSpeechService.LoadVoiceItem.stopImpl():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.LoadVoiceItem.stopImpl():void");
        }

        public boolean isValid() {
            return true;
        }
    }

    private class SilenceSpeechItem extends UtteranceSpeechItem {
        private final long mDuration;
        private final String mUtteranceId;
        final /* synthetic */ TextToSpeechService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeechService.SilenceSpeechItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, java.lang.String, long):void, dex: 
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
        public SilenceSpeechItem(android.speech.tts.TextToSpeechService r1, java.lang.Object r2, int r3, int r4, java.lang.String r5, long r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeechService.SilenceSpeechItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, java.lang.String, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SilenceSpeechItem.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, java.lang.String, long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SilenceSpeechItem.getUtteranceId():java.lang.String, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.String getUtteranceId() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SilenceSpeechItem.getUtteranceId():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SilenceSpeechItem.getUtteranceId():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SilenceSpeechItem.playImpl():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        protected void playImpl() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SilenceSpeechItem.playImpl():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SilenceSpeechItem.playImpl():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.speech.tts.TextToSpeechService.SilenceSpeechItem.stopImpl():void, dex: 
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
        protected void stopImpl() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.speech.tts.TextToSpeechService.SilenceSpeechItem.stopImpl():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SilenceSpeechItem.stopImpl():void");
        }

        public boolean isValid() {
            return true;
        }
    }

    private class SynthHandler extends Handler {
        private SpeechItem mCurrentSpeechItem;
        private int mFlushAll;
        private List<Object> mFlushedObjects;
        final /* synthetic */ TextToSpeechService this$0;

        /* renamed from: android.speech.tts.TextToSpeechService$SynthHandler$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ SynthHandler this$1;
            final /* synthetic */ SpeechItem val$speechItem;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeechService.SynthHandler.1.<init>(android.speech.tts.TextToSpeechService$SynthHandler, android.speech.tts.TextToSpeechService$SpeechItem):void, dex: 
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
            AnonymousClass1(android.speech.tts.TextToSpeechService.SynthHandler r1, android.speech.tts.TextToSpeechService.SpeechItem r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeechService.SynthHandler.1.<init>(android.speech.tts.TextToSpeechService$SynthHandler, android.speech.tts.TextToSpeechService$SpeechItem):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthHandler.1.<init>(android.speech.tts.TextToSpeechService$SynthHandler, android.speech.tts.TextToSpeechService$SpeechItem):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SynthHandler.1.run():void, dex: 
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
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SynthHandler.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthHandler.1.run():void");
            }
        }

        /* renamed from: android.speech.tts.TextToSpeechService$SynthHandler$3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ SynthHandler this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeechService.SynthHandler.3.<init>(android.speech.tts.TextToSpeechService$SynthHandler):void, dex: 
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
            AnonymousClass3(android.speech.tts.TextToSpeechService.SynthHandler r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeechService.SynthHandler.3.<init>(android.speech.tts.TextToSpeechService$SynthHandler):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthHandler.3.<init>(android.speech.tts.TextToSpeechService$SynthHandler):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SynthHandler.3.run():void, dex: 
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
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SynthHandler.3.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthHandler.3.run():void");
            }
        }

        public SynthHandler(TextToSpeechService this$0, Looper looper) {
            this.this$0 = this$0;
            super(looper);
            this.mCurrentSpeechItem = null;
            this.mFlushedObjects = new ArrayList();
            this.mFlushAll = 0;
        }

        private void startFlushingSpeechItems(Object callerIdentity) {
            synchronized (this.mFlushedObjects) {
                if (callerIdentity == null) {
                    this.mFlushAll++;
                } else {
                    this.mFlushedObjects.add(callerIdentity);
                }
            }
        }

        private void endFlushingSpeechItems(Object callerIdentity) {
            synchronized (this.mFlushedObjects) {
                if (callerIdentity == null) {
                    this.mFlushAll--;
                } else {
                    this.mFlushedObjects.remove(callerIdentity);
                }
            }
        }

        private boolean isFlushed(SpeechItem speechItem) {
            boolean contains;
            synchronized (this.mFlushedObjects) {
                contains = this.mFlushAll <= 0 ? this.mFlushedObjects.contains(speechItem.getCallerIdentity()) : true;
            }
            return contains;
        }

        private synchronized SpeechItem getCurrentSpeechItem() {
            return this.mCurrentSpeechItem;
        }

        private synchronized SpeechItem setCurrentSpeechItem(SpeechItem speechItem) {
            SpeechItem old;
            old = this.mCurrentSpeechItem;
            this.mCurrentSpeechItem = speechItem;
            return old;
        }

        /* JADX WARNING: Missing block: B:11:0x0016, code:
            return null;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private synchronized SpeechItem maybeRemoveCurrentSpeechItem(Object callerIdentity) {
            if (this.mCurrentSpeechItem != null && this.mCurrentSpeechItem.getCallerIdentity() == callerIdentity) {
                SpeechItem current = this.mCurrentSpeechItem;
                this.mCurrentSpeechItem = null;
                return current;
            }
        }

        public boolean isSpeaking() {
            return getCurrentSpeechItem() != null;
        }

        public void quit() {
            getLooper().quit();
            SpeechItem current = setCurrentSpeechItem(null);
            if (current != null) {
                current.stop();
            }
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
        public int enqueueSpeechItem(int r7, android.speech.tts.TextToSpeechService.SpeechItem r8) {
            /*
            r6 = this;
            r4 = 0;
            r5 = -1;
            r2 = 0;
            r3 = r8 instanceof android.speech.tts.TextToSpeechService.UtteranceProgressDispatcher;
            if (r3 == 0) goto L_0x000a;
        L_0x0007:
            r2 = r8;
            r2 = (android.speech.tts.TextToSpeechService.UtteranceProgressDispatcher) r2;
        L_0x000a:
            r3 = r8.isValid();
            if (r3 != 0) goto L_0x0017;
        L_0x0010:
            if (r2 == 0) goto L_0x0016;
        L_0x0012:
            r3 = -8;
            r2.dispatchOnError(r3);
        L_0x0016:
            return r5;
        L_0x0017:
            if (r7 != 0) goto L_0x0036;
        L_0x0019:
            r3 = r8.getCallerIdentity();
            r6.stopForApp(r3);
        L_0x0020:
            r1 = new android.speech.tts.TextToSpeechService$SynthHandler$1;
            r1.<init>(r6, r8);
            r0 = android.os.Message.obtain(r6, r1);
            r3 = r8.getCallerIdentity();
            r0.obj = r3;
            r3 = r6.sendMessage(r0);
            if (r3 == 0) goto L_0x003d;
        L_0x0035:
            return r4;
        L_0x0036:
            r3 = 2;
            if (r7 != r3) goto L_0x0020;
        L_0x0039:
            r6.stopAll();
            goto L_0x0020;
        L_0x003d:
            r3 = "TextToSpeechService";
            r4 = "SynthThread has quit";
            android.util.Log.w(r3, r4);
            if (r2 == 0) goto L_0x004c;
        L_0x0048:
            r3 = -4;
            r2.dispatchOnError(r3);
        L_0x004c:
            return r5;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthHandler.enqueueSpeechItem(int, android.speech.tts.TextToSpeechService$SpeechItem):int");
        }

        public int stopForApp(final Object callerIdentity) {
            if (callerIdentity == null) {
                return -1;
            }
            startFlushingSpeechItems(callerIdentity);
            SpeechItem current = maybeRemoveCurrentSpeechItem(callerIdentity);
            if (current != null) {
                current.stop();
            }
            this.this$0.mAudioPlaybackHandler.stopForApp(callerIdentity);
            sendMessage(Message.obtain(this, new Runnable(this) {
                final /* synthetic */ SynthHandler this$1;

                public void run() {
                    this.this$1.endFlushingSpeechItems(callerIdentity);
                }
            }));
            return 0;
        }

        public int stopAll() {
            startFlushingSpeechItems(null);
            SpeechItem current = setCurrentSpeechItem(null);
            if (current != null) {
                current.stop();
            }
            this.this$0.mAudioPlaybackHandler.stop();
            sendMessage(Message.obtain(this, new AnonymousClass3(this)));
            return 0;
        }
    }

    private class SynthThread extends HandlerThread implements IdleHandler {
        private boolean mFirstIdle;
        final /* synthetic */ TextToSpeechService this$0;

        public SynthThread(TextToSpeechService this$0) {
            this.this$0 = this$0;
            super(TextToSpeechService.SYNTH_THREAD_NAME, 0);
            this.mFirstIdle = true;
        }

        protected void onLooperPrepared() {
            getLooper().getQueue().addIdleHandler(this);
        }

        public boolean queueIdle() {
            if (this.mFirstIdle) {
                this.mFirstIdle = false;
            } else {
                broadcastTtsQueueProcessingCompleted();
            }
            return true;
        }

        private void broadcastTtsQueueProcessingCompleted() {
            this.this$0.sendBroadcast(new Intent(TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED));
        }
    }

    class SynthesisSpeechItemV1 extends SpeechItemV1 {
        private final int mCallerUid;
        private final String[] mDefaultLocale;
        private final EventLoggerV1 mEventLogger;
        private AbstractSynthesisCallback mSynthesisCallback;
        private final SynthesisRequest mSynthesisRequest;
        private final CharSequence mText;
        final /* synthetic */ TextToSpeechService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String, java.lang.CharSequence):void, dex: 
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
        public SynthesisSpeechItemV1(android.speech.tts.TextToSpeechService r1, java.lang.Object r2, int r3, int r4, android.os.Bundle r5, java.lang.String r6, java.lang.CharSequence r7) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String, java.lang.CharSequence):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String, java.lang.CharSequence):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getCountry():java.lang.String, dex:  in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getCountry():java.lang.String, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getCountry():java.lang.String, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:756)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        private java.lang.String getCountry() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getCountry():java.lang.String, dex:  in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getCountry():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getCountry():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getVariant():java.lang.String, dex:  in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getVariant():java.lang.String, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getVariant():java.lang.String, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:756)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        private java.lang.String getVariant() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getVariant():java.lang.String, dex:  in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getVariant():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getVariant():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.setRequestParams(android.speech.tts.SynthesisRequest):void, dex: 
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
        private void setRequestParams(android.speech.tts.SynthesisRequest r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.setRequestParams(android.speech.tts.SynthesisRequest):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.setRequestParams(android.speech.tts.SynthesisRequest):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.createSynthesisCallback():android.speech.tts.AbstractSynthesisCallback, dex: 
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
        protected android.speech.tts.AbstractSynthesisCallback createSynthesisCallback() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.createSynthesisCallback():android.speech.tts.AbstractSynthesisCallback, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.createSynthesisCallback():android.speech.tts.AbstractSynthesisCallback");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getLanguage():java.lang.String, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.String getLanguage() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getLanguage():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getLanguage():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getText():java.lang.CharSequence, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.CharSequence getText() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getText():java.lang.CharSequence, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getText():java.lang.CharSequence");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getVoiceName():java.lang.String, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.String getVoiceName() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getVoiceName():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.getVoiceName():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: d in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.isValid():boolean, dex:  in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.isValid():boolean, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: d in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.isValid():boolean, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: com.android.dex.DexException: bogus registerCount: d
            	at com.android.dx.io.instructions.InstructionCodec$32.decode(InstructionCodec.java:693)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public boolean isValid() {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: d in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.isValid():boolean, dex:  in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.isValid():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.isValid():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.playImpl():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        protected void playImpl() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.playImpl():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.playImpl():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.stopImpl():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        protected void stopImpl() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.stopImpl():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthesisSpeechItemV1.stopImpl():void");
        }
    }

    private class SynthesisToFileOutputStreamSpeechItemV1 extends SynthesisSpeechItemV1 {
        private final FileOutputStream mFileOutputStream;
        final /* synthetic */ TextToSpeechService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeechService.SynthesisToFileOutputStreamSpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String, java.lang.CharSequence, java.io.FileOutputStream):void, dex: 
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
        public SynthesisToFileOutputStreamSpeechItemV1(android.speech.tts.TextToSpeechService r1, java.lang.Object r2, int r3, int r4, android.os.Bundle r5, java.lang.String r6, java.lang.CharSequence r7, java.io.FileOutputStream r8) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeechService.SynthesisToFileOutputStreamSpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String, java.lang.CharSequence, java.io.FileOutputStream):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthesisToFileOutputStreamSpeechItemV1.<init>(android.speech.tts.TextToSpeechService, java.lang.Object, int, int, android.os.Bundle, java.lang.String, java.lang.CharSequence, java.io.FileOutputStream):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SynthesisToFileOutputStreamSpeechItemV1.createSynthesisCallback():android.speech.tts.AbstractSynthesisCallback, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        protected android.speech.tts.AbstractSynthesisCallback createSynthesisCallback() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeechService.SynthesisToFileOutputStreamSpeechItemV1.createSynthesisCallback():android.speech.tts.AbstractSynthesisCallback, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthesisToFileOutputStreamSpeechItemV1.createSynthesisCallback():android.speech.tts.AbstractSynthesisCallback");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.SynthesisToFileOutputStreamSpeechItemV1.playImpl():void, dex: 
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
        protected void playImpl() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeechService.SynthesisToFileOutputStreamSpeechItemV1.playImpl():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeechService.SynthesisToFileOutputStreamSpeechItemV1.playImpl():void");
        }
    }

    protected abstract String[] onGetLanguage();

    protected abstract int onIsLanguageAvailable(String str, String str2, String str3);

    protected abstract int onLoadLanguage(String str, String str2, String str3);

    protected abstract void onStop();

    protected abstract void onSynthesizeText(SynthesisRequest synthesisRequest, SynthesisCallback synthesisCallback);

    public TextToSpeechService() {
        this.mVoicesInfoLock = new Object();
        this.mBinder = new Stub() {
            public int speak(IBinder caller, CharSequence text, int queueMode, Bundle params, String utteranceId) {
                if (!checkNonNull(caller, text, params)) {
                    return -1;
                }
                return TextToSpeechService.this.mSynthHandler.enqueueSpeechItem(queueMode, new SynthesisSpeechItemV1(TextToSpeechService.this, caller, Binder.getCallingUid(), Binder.getCallingPid(), params, utteranceId, text));
            }

            public int synthesizeToFileDescriptor(IBinder caller, CharSequence text, ParcelFileDescriptor fileDescriptor, Bundle params, String utteranceId) {
                if (!checkNonNull(caller, text, fileDescriptor, params)) {
                    return -1;
                }
                IBinder iBinder = caller;
                Bundle bundle = params;
                String str = utteranceId;
                CharSequence charSequence = text;
                return TextToSpeechService.this.mSynthHandler.enqueueSpeechItem(1, new SynthesisToFileOutputStreamSpeechItemV1(TextToSpeechService.this, iBinder, Binder.getCallingUid(), Binder.getCallingPid(), bundle, str, charSequence, new AutoCloseOutputStream(ParcelFileDescriptor.adoptFd(fileDescriptor.detachFd()))));
            }

            public int playAudio(IBinder caller, Uri audioUri, int queueMode, Bundle params, String utteranceId) {
                if (!checkNonNull(caller, audioUri, params)) {
                    return -1;
                }
                return TextToSpeechService.this.mSynthHandler.enqueueSpeechItem(queueMode, new AudioSpeechItemV1(TextToSpeechService.this, caller, Binder.getCallingUid(), Binder.getCallingPid(), params, utteranceId, audioUri));
            }

            public int playSilence(IBinder caller, long duration, int queueMode, String utteranceId) {
                if (!checkNonNull(caller)) {
                    return -1;
                }
                return TextToSpeechService.this.mSynthHandler.enqueueSpeechItem(queueMode, new SilenceSpeechItem(TextToSpeechService.this, caller, Binder.getCallingUid(), Binder.getCallingPid(), utteranceId, duration));
            }

            public boolean isSpeaking() {
                return !TextToSpeechService.this.mSynthHandler.isSpeaking() ? TextToSpeechService.this.mAudioPlaybackHandler.isSpeaking() : true;
            }

            public int stop(IBinder caller) {
                if (checkNonNull(caller)) {
                    return TextToSpeechService.this.mSynthHandler.stopForApp(caller);
                }
                return -1;
            }

            public String[] getLanguage() {
                return TextToSpeechService.this.onGetLanguage();
            }

            public String[] getClientDefaultLanguage() {
                return TextToSpeechService.this.getSettingsLocale();
            }

            public int isLanguageAvailable(String lang, String country, String variant) {
                if (checkNonNull(lang)) {
                    return TextToSpeechService.this.onIsLanguageAvailable(lang, country, variant);
                }
                return -1;
            }

            public String[] getFeaturesForLanguage(String lang, String country, String variant) {
                Set<String> features = TextToSpeechService.this.onGetFeaturesForLanguage(lang, country, variant);
                if (features == null) {
                    return new String[0];
                }
                String[] featuresArray = new String[features.size()];
                features.toArray(featuresArray);
                return featuresArray;
            }

            public int loadLanguage(IBinder caller, String lang, String country, String variant) {
                if (!checkNonNull(lang)) {
                    return -1;
                }
                int retVal = TextToSpeechService.this.onIsLanguageAvailable(lang, country, variant);
                if (retVal == 0 || retVal == 1 || retVal == 2) {
                    if (TextToSpeechService.this.mSynthHandler.enqueueSpeechItem(1, new LoadLanguageItem(TextToSpeechService.this, caller, Binder.getCallingUid(), Binder.getCallingPid(), lang, country, variant)) != 0) {
                        return -1;
                    }
                }
                return retVal;
            }

            public List<Voice> getVoices() {
                return TextToSpeechService.this.onGetVoices();
            }

            public int loadVoice(IBinder caller, String voiceName) {
                if (!checkNonNull(voiceName)) {
                    return -1;
                }
                int retVal = TextToSpeechService.this.onIsValidVoiceName(voiceName);
                if (retVal == 0) {
                    if (TextToSpeechService.this.mSynthHandler.enqueueSpeechItem(1, new LoadVoiceItem(TextToSpeechService.this, caller, Binder.getCallingUid(), Binder.getCallingPid(), voiceName)) != 0) {
                        return -1;
                    }
                }
                return retVal;
            }

            public String getDefaultVoiceNameFor(String lang, String country, String variant) {
                if (!checkNonNull(lang)) {
                    return null;
                }
                int retVal = TextToSpeechService.this.onIsLanguageAvailable(lang, country, variant);
                if (retVal == 0 || retVal == 1 || retVal == 2) {
                    return TextToSpeechService.this.onGetDefaultVoiceNameFor(lang, country, variant);
                }
                return null;
            }

            public void setCallback(IBinder caller, ITextToSpeechCallback cb) {
                if (checkNonNull(caller)) {
                    TextToSpeechService.this.mCallbacks.setCallback(caller, cb);
                }
            }

            private String intern(String in) {
                return in.intern();
            }

            private boolean checkNonNull(Object... args) {
                for (Object o : args) {
                    if (o == null) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public void onCreate() {
        super.onCreate();
        SynthThread synthThread = new SynthThread(this);
        synthThread.start();
        this.mSynthHandler = new SynthHandler(this, synthThread.getLooper());
        this.mAudioPlaybackHandler = new AudioPlaybackHandler();
        this.mAudioPlaybackHandler.start();
        this.mEngineHelper = new TtsEngines(this);
        this.mCallbacks = new CallbackMap(this, null);
        this.mPackageName = getApplicationInfo().packageName;
        String[] defaultLocale = getSettingsLocale();
        onLoadLanguage(defaultLocale[0], defaultLocale[1], defaultLocale[2]);
    }

    public void onDestroy() {
        this.mSynthHandler.quit();
        this.mAudioPlaybackHandler.quit();
        this.mCallbacks.kill();
        super.onDestroy();
    }

    protected Set<String> onGetFeaturesForLanguage(String lang, String country, String variant) {
        return new HashSet();
    }

    private int getExpectedLanguageAvailableStatus(Locale locale) {
        if (!locale.getVariant().isEmpty()) {
            return 2;
        }
        if (locale.getCountry().isEmpty()) {
            return 0;
        }
        return 1;
    }

    public List<Voice> onGetVoices() {
        ArrayList<Voice> voices = new ArrayList();
        for (Locale locale : Locale.getAvailableLocales()) {
            try {
                if (onIsLanguageAvailable(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant()) == getExpectedLanguageAvailableStatus(locale)) {
                    voices.add(new Voice(onGetDefaultVoiceNameFor(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant()), locale, 300, 300, false, onGetFeaturesForLanguage(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant())));
                }
            } catch (MissingResourceException e) {
            }
        }
        return voices;
    }

    public String onGetDefaultVoiceNameFor(String lang, String country, String variant) {
        Locale iso3Locale;
        switch (onIsLanguageAvailable(lang, country, variant)) {
            case 0:
                iso3Locale = new Locale(lang);
                break;
            case 1:
                iso3Locale = new Locale(lang, country);
                break;
            case 2:
                iso3Locale = new Locale(lang, country, variant);
                break;
            default:
                return null;
        }
        String voiceName = TtsEngines.normalizeTTSLocale(iso3Locale).toLanguageTag();
        if (onIsValidVoiceName(voiceName) == 0) {
            return voiceName;
        }
        return null;
    }

    public int onLoadVoice(String voiceName) {
        Locale locale = Locale.forLanguageTag(voiceName);
        if (locale == null) {
            return -1;
        }
        try {
            if (onIsLanguageAvailable(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant()) != getExpectedLanguageAvailableStatus(locale)) {
                return -1;
            }
            onLoadLanguage(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant());
            return 0;
        } catch (MissingResourceException e) {
            return -1;
        }
    }

    public int onIsValidVoiceName(String voiceName) {
        Locale locale = Locale.forLanguageTag(voiceName);
        if (locale == null) {
            return -1;
        }
        try {
            if (onIsLanguageAvailable(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant()) != getExpectedLanguageAvailableStatus(locale)) {
                return -1;
            }
            return 0;
        } catch (MissingResourceException e) {
            return -1;
        }
    }

    private int getDefaultSpeechRate() {
        return getSecureSettingInt("tts_default_rate", 100);
    }

    private String[] getSettingsLocale() {
        return TtsEngines.toOldLocaleStringFormat(this.mEngineHelper.getLocalePrefForEngine(this.mPackageName));
    }

    private int getSecureSettingInt(String name, int defaultValue) {
        return Secure.getInt(getContentResolver(), name, defaultValue);
    }

    public IBinder onBind(Intent intent) {
        if (Engine.INTENT_ACTION_TTS_SERVICE.equals(intent.getAction())) {
            return this.mBinder;
        }
        return null;
    }
}
