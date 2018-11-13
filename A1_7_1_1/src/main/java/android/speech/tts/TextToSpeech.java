package android.speech.tts;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.speech.tts.ITextToSpeechCallback.Stub;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.PhoneConstants;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
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
public class TextToSpeech {
    public static final String ACTION_TTS_QUEUE_PROCESSING_COMPLETED = "android.speech.tts.TTS_QUEUE_PROCESSING_COMPLETED";
    public static final int ERROR = -1;
    public static final int ERROR_INVALID_REQUEST = -8;
    public static final int ERROR_NETWORK = -6;
    public static final int ERROR_NETWORK_TIMEOUT = -7;
    public static final int ERROR_NOT_INSTALLED_YET = -9;
    public static final int ERROR_OUTPUT = -5;
    public static final int ERROR_SERVICE = -4;
    public static final int ERROR_SYNTHESIS = -3;
    public static final int LANG_AVAILABLE = 0;
    public static final int LANG_COUNTRY_AVAILABLE = 1;
    public static final int LANG_COUNTRY_VAR_AVAILABLE = 2;
    public static final int LANG_MISSING_DATA = -1;
    public static final int LANG_NOT_SUPPORTED = -2;
    public static final int QUEUE_ADD = 1;
    static final int QUEUE_DESTROY = 2;
    public static final int QUEUE_FLUSH = 0;
    public static final int STOPPED = -2;
    public static final int SUCCESS = 0;
    private static final String TAG = "TextToSpeech";
    private Connection mConnectingServiceConnection;
    private final Context mContext;
    private volatile String mCurrentEngine;
    private final Map<String, Uri> mEarcons;
    private final TtsEngines mEnginesHelper;
    private OnInitListener mInitListener;
    private final Bundle mParams;
    private String mRequestedEngine;
    private Connection mServiceConnection;
    private final Object mStartLock;
    private final boolean mUseFallback;
    private volatile UtteranceProgressListener mUtteranceProgressListener;
    private final Map<CharSequence, Uri> mUtterances;

    private interface Action<R> {
        R run(ITextToSpeechService iTextToSpeechService) throws RemoteException;
    }

    /* renamed from: android.speech.tts.TextToSpeech$11 */
    class AnonymousClass11 implements Action<Set<Locale>> {
        final /* synthetic */ TextToSpeech this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.11.<init>(android.speech.tts.TextToSpeech):void, dex: 
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
        AnonymousClass11(android.speech.tts.TextToSpeech r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.11.<init>(android.speech.tts.TextToSpeech):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.11.<init>(android.speech.tts.TextToSpeech):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.11.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.11.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.11.run(android.speech.tts.ITextToSpeechService):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.11.run(android.speech.tts.ITextToSpeechService):java.util.Set<java.util.Locale>, dex: 
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
        public java.util.Set<java.util.Locale> run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.11.run(android.speech.tts.ITextToSpeechService):java.util.Set<java.util.Locale>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.11.run(android.speech.tts.ITextToSpeechService):java.util.Set<java.util.Locale>");
        }
    }

    /* renamed from: android.speech.tts.TextToSpeech$12 */
    class AnonymousClass12 implements Action<Set<Voice>> {
        final /* synthetic */ TextToSpeech this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.12.<init>(android.speech.tts.TextToSpeech):void, dex: 
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
        AnonymousClass12(android.speech.tts.TextToSpeech r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.12.<init>(android.speech.tts.TextToSpeech):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.12.<init>(android.speech.tts.TextToSpeech):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.12.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.12.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.12.run(android.speech.tts.ITextToSpeechService):java.lang.Object");
        }

        public Set<Voice> run(ITextToSpeechService service) throws RemoteException {
            List<Voice> voices = service.getVoices();
            return voices != null ? new HashSet(voices) : new HashSet();
        }
    }

    /* renamed from: android.speech.tts.TextToSpeech$13 */
    class AnonymousClass13 implements Action<Integer> {
        final /* synthetic */ TextToSpeech this$0;
        final /* synthetic */ Voice val$voice;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.13.<init>(android.speech.tts.TextToSpeech, android.speech.tts.Voice):void, dex: 
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
        AnonymousClass13(android.speech.tts.TextToSpeech r1, android.speech.tts.Voice r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.13.<init>(android.speech.tts.TextToSpeech, android.speech.tts.Voice):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.13.<init>(android.speech.tts.TextToSpeech, android.speech.tts.Voice):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.13.run(android.speech.tts.ITextToSpeechService):java.lang.Integer, dex: 
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
        public java.lang.Integer run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.13.run(android.speech.tts.ITextToSpeechService):java.lang.Integer, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.13.run(android.speech.tts.ITextToSpeechService):java.lang.Integer");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.13.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.13.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.13.run(android.speech.tts.ITextToSpeechService):java.lang.Object");
        }
    }

    /* renamed from: android.speech.tts.TextToSpeech$14 */
    class AnonymousClass14 implements Action<Voice> {
        final /* synthetic */ TextToSpeech this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.14.<init>(android.speech.tts.TextToSpeech):void, dex: 
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
        AnonymousClass14(android.speech.tts.TextToSpeech r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.14.<init>(android.speech.tts.TextToSpeech):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.14.<init>(android.speech.tts.TextToSpeech):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.14.run(android.speech.tts.ITextToSpeechService):android.speech.tts.Voice, dex: 
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
        public android.speech.tts.Voice run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.14.run(android.speech.tts.ITextToSpeechService):android.speech.tts.Voice, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.14.run(android.speech.tts.ITextToSpeechService):android.speech.tts.Voice");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.14.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.14.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.14.run(android.speech.tts.ITextToSpeechService):java.lang.Object");
        }
    }

    /* renamed from: android.speech.tts.TextToSpeech$15 */
    class AnonymousClass15 implements Action<Voice> {
        final /* synthetic */ TextToSpeech this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.15.<init>(android.speech.tts.TextToSpeech):void, dex: 
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
        AnonymousClass15(android.speech.tts.TextToSpeech r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.15.<init>(android.speech.tts.TextToSpeech):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.15.<init>(android.speech.tts.TextToSpeech):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.15.run(android.speech.tts.ITextToSpeechService):android.speech.tts.Voice, dex: 
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
        public android.speech.tts.Voice run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.15.run(android.speech.tts.ITextToSpeechService):android.speech.tts.Voice, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.15.run(android.speech.tts.ITextToSpeechService):android.speech.tts.Voice");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.15.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.15.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.15.run(android.speech.tts.ITextToSpeechService):java.lang.Object");
        }
    }

    /* renamed from: android.speech.tts.TextToSpeech$17 */
    class AnonymousClass17 implements Action<Integer> {
        final /* synthetic */ TextToSpeech this$0;
        final /* synthetic */ File val$file;
        final /* synthetic */ Bundle val$params;
        final /* synthetic */ CharSequence val$text;
        final /* synthetic */ String val$utteranceId;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.17.<init>(android.speech.tts.TextToSpeech, java.io.File, java.lang.CharSequence, android.os.Bundle, java.lang.String):void, dex: 
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
        AnonymousClass17(android.speech.tts.TextToSpeech r1, java.io.File r2, java.lang.CharSequence r3, android.os.Bundle r4, java.lang.String r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.17.<init>(android.speech.tts.TextToSpeech, java.io.File, java.lang.CharSequence, android.os.Bundle, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.17.<init>(android.speech.tts.TextToSpeech, java.io.File, java.lang.CharSequence, android.os.Bundle, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.17.run(android.speech.tts.ITextToSpeechService):java.lang.Integer, dex: 
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
        public java.lang.Integer run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.17.run(android.speech.tts.ITextToSpeechService):java.lang.Integer, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.17.run(android.speech.tts.ITextToSpeechService):java.lang.Integer");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.17.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.17.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.17.run(android.speech.tts.ITextToSpeechService):java.lang.Object");
        }
    }

    /* renamed from: android.speech.tts.TextToSpeech$1 */
    class AnonymousClass1 implements Action<Void> {
        final /* synthetic */ TextToSpeech this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.1.<init>(android.speech.tts.TextToSpeech):void, dex: 
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
        AnonymousClass1(android.speech.tts.TextToSpeech r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.1.<init>(android.speech.tts.TextToSpeech):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.1.<init>(android.speech.tts.TextToSpeech):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.1.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.1.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.1.run(android.speech.tts.ITextToSpeechService):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.1.run(android.speech.tts.ITextToSpeechService):java.lang.Void, dex: 
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
        public java.lang.Void run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.1.run(android.speech.tts.ITextToSpeechService):java.lang.Void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.1.run(android.speech.tts.ITextToSpeechService):java.lang.Void");
        }
    }

    /* renamed from: android.speech.tts.TextToSpeech$2 */
    class AnonymousClass2 implements Action<Integer> {
        final /* synthetic */ TextToSpeech this$0;
        final /* synthetic */ Bundle val$params;
        final /* synthetic */ int val$queueMode;
        final /* synthetic */ CharSequence val$text;
        final /* synthetic */ String val$utteranceId;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.2.<init>(android.speech.tts.TextToSpeech, java.lang.CharSequence, int, android.os.Bundle, java.lang.String):void, dex: 
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
        AnonymousClass2(android.speech.tts.TextToSpeech r1, java.lang.CharSequence r2, int r3, android.os.Bundle r4, java.lang.String r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.2.<init>(android.speech.tts.TextToSpeech, java.lang.CharSequence, int, android.os.Bundle, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.2.<init>(android.speech.tts.TextToSpeech, java.lang.CharSequence, int, android.os.Bundle, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.2.run(android.speech.tts.ITextToSpeechService):java.lang.Integer, dex: 
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
        public java.lang.Integer run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.2.run(android.speech.tts.ITextToSpeechService):java.lang.Integer, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.2.run(android.speech.tts.ITextToSpeechService):java.lang.Integer");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.2.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.2.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.2.run(android.speech.tts.ITextToSpeechService):java.lang.Object");
        }
    }

    /* renamed from: android.speech.tts.TextToSpeech$3 */
    class AnonymousClass3 implements Action<Integer> {
        final /* synthetic */ TextToSpeech this$0;
        final /* synthetic */ String val$earcon;
        final /* synthetic */ Bundle val$params;
        final /* synthetic */ int val$queueMode;
        final /* synthetic */ String val$utteranceId;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.3.<init>(android.speech.tts.TextToSpeech, java.lang.String, int, android.os.Bundle, java.lang.String):void, dex: 
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
        AnonymousClass3(android.speech.tts.TextToSpeech r1, java.lang.String r2, int r3, android.os.Bundle r4, java.lang.String r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.3.<init>(android.speech.tts.TextToSpeech, java.lang.String, int, android.os.Bundle, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.3.<init>(android.speech.tts.TextToSpeech, java.lang.String, int, android.os.Bundle, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.3.run(android.speech.tts.ITextToSpeechService):java.lang.Integer, dex: 
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
        public java.lang.Integer run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.3.run(android.speech.tts.ITextToSpeechService):java.lang.Integer, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.3.run(android.speech.tts.ITextToSpeechService):java.lang.Integer");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.3.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.3.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.3.run(android.speech.tts.ITextToSpeechService):java.lang.Object");
        }
    }

    /* renamed from: android.speech.tts.TextToSpeech$4 */
    class AnonymousClass4 implements Action<Integer> {
        final /* synthetic */ TextToSpeech this$0;
        final /* synthetic */ long val$durationInMs;
        final /* synthetic */ int val$queueMode;
        final /* synthetic */ String val$utteranceId;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.4.<init>(android.speech.tts.TextToSpeech, long, int, java.lang.String):void, dex: 
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
        AnonymousClass4(android.speech.tts.TextToSpeech r1, long r2, int r4, java.lang.String r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.4.<init>(android.speech.tts.TextToSpeech, long, int, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.4.<init>(android.speech.tts.TextToSpeech, long, int, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.4.run(android.speech.tts.ITextToSpeechService):java.lang.Integer, dex: 
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
        public java.lang.Integer run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.4.run(android.speech.tts.ITextToSpeechService):java.lang.Integer, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.4.run(android.speech.tts.ITextToSpeechService):java.lang.Integer");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.4.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.4.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.4.run(android.speech.tts.ITextToSpeechService):java.lang.Object");
        }
    }

    /* renamed from: android.speech.tts.TextToSpeech$5 */
    class AnonymousClass5 implements Action<Set<String>> {
        final /* synthetic */ TextToSpeech this$0;
        final /* synthetic */ Locale val$locale;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.5.<init>(android.speech.tts.TextToSpeech, java.util.Locale):void, dex: 
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
        AnonymousClass5(android.speech.tts.TextToSpeech r1, java.util.Locale r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.5.<init>(android.speech.tts.TextToSpeech, java.util.Locale):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.5.<init>(android.speech.tts.TextToSpeech, java.util.Locale):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.5.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.5.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.5.run(android.speech.tts.ITextToSpeechService):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.5.run(android.speech.tts.ITextToSpeechService):java.util.Set<java.lang.String>, dex: 
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
        public java.util.Set<java.lang.String> run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.5.run(android.speech.tts.ITextToSpeechService):java.util.Set<java.lang.String>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.5.run(android.speech.tts.ITextToSpeechService):java.util.Set<java.lang.String>");
        }
    }

    /* renamed from: android.speech.tts.TextToSpeech$6 */
    class AnonymousClass6 implements Action<Boolean> {
        final /* synthetic */ TextToSpeech this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.6.<init>(android.speech.tts.TextToSpeech):void, dex: 
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
        AnonymousClass6(android.speech.tts.TextToSpeech r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.6.<init>(android.speech.tts.TextToSpeech):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.6.<init>(android.speech.tts.TextToSpeech):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.6.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.6.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.6.run(android.speech.tts.ITextToSpeechService):java.lang.Object");
        }

        public Boolean run(ITextToSpeechService service) throws RemoteException {
            return Boolean.valueOf(service.isSpeaking());
        }
    }

    /* renamed from: android.speech.tts.TextToSpeech$7 */
    class AnonymousClass7 implements Action<Integer> {
        final /* synthetic */ TextToSpeech this$0;

        AnonymousClass7(TextToSpeech this$0) {
            this.this$0 = this$0;
        }

        public /* bridge */ /* synthetic */ Object run(ITextToSpeechService service) throws RemoteException {
            return run(service);
        }

        public Integer run(ITextToSpeechService service) throws RemoteException {
            return Integer.valueOf(service.stop(this.this$0.getCallerIdentity()));
        }
    }

    /* renamed from: android.speech.tts.TextToSpeech$8 */
    class AnonymousClass8 implements Action<Locale> {
        final /* synthetic */ TextToSpeech this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.8.<init>(android.speech.tts.TextToSpeech):void, dex: 
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
        AnonymousClass8(android.speech.tts.TextToSpeech r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.8.<init>(android.speech.tts.TextToSpeech):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.8.<init>(android.speech.tts.TextToSpeech):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.8.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.8.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.8.run(android.speech.tts.ITextToSpeechService):java.lang.Object");
        }

        public Locale run(ITextToSpeechService service) throws RemoteException {
            String[] defaultLanguage = service.getClientDefaultLanguage();
            return new Locale(defaultLanguage[0], defaultLanguage[1], defaultLanguage[2]);
        }
    }

    /* renamed from: android.speech.tts.TextToSpeech$9 */
    class AnonymousClass9 implements Action<Integer> {
        final /* synthetic */ TextToSpeech this$0;
        final /* synthetic */ Locale val$loc;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.9.<init>(android.speech.tts.TextToSpeech, java.util.Locale):void, dex: 
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
        AnonymousClass9(android.speech.tts.TextToSpeech r1, java.util.Locale r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.9.<init>(android.speech.tts.TextToSpeech, java.util.Locale):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.9.<init>(android.speech.tts.TextToSpeech, java.util.Locale):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.9.run(android.speech.tts.ITextToSpeechService):java.lang.Integer, dex: 
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
        public java.lang.Integer run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.speech.tts.TextToSpeech.9.run(android.speech.tts.ITextToSpeechService):java.lang.Integer, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.9.run(android.speech.tts.ITextToSpeechService):java.lang.Integer");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.9.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run(android.speech.tts.ITextToSpeechService r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.speech.tts.TextToSpeech.9.run(android.speech.tts.ITextToSpeechService):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.9.run(android.speech.tts.ITextToSpeechService):java.lang.Object");
        }
    }

    private class Connection implements ServiceConnection {
        private final Stub mCallback;
        private boolean mEstablished;
        private SetupConnectionAsyncTask mOnSetupConnectionAsyncTask;
        private ITextToSpeechService mService;
        final /* synthetic */ TextToSpeech this$0;

        /* renamed from: android.speech.tts.TextToSpeech$Connection$1 */
        class AnonymousClass1 extends Stub {
            final /* synthetic */ Connection this$1;

            AnonymousClass1(Connection this$1) {
                this.this$1 = this$1;
            }

            public void onStop(String utteranceId, boolean isStarted) throws RemoteException {
                UtteranceProgressListener listener = this.this$1.this$0.mUtteranceProgressListener;
                if (listener != null) {
                    listener.onStop(utteranceId, isStarted);
                }
            }

            public void onSuccess(String utteranceId) {
                UtteranceProgressListener listener = this.this$1.this$0.mUtteranceProgressListener;
                if (listener != null) {
                    listener.onDone(utteranceId);
                }
            }

            public void onError(String utteranceId, int errorCode) {
                UtteranceProgressListener listener = this.this$1.this$0.mUtteranceProgressListener;
                if (listener != null) {
                    listener.onError(utteranceId);
                }
            }

            public void onStart(String utteranceId) {
                UtteranceProgressListener listener = this.this$1.this$0.mUtteranceProgressListener;
                if (listener != null) {
                    listener.onStart(utteranceId);
                }
            }

            public void onBeginSynthesis(String utteranceId, int sampleRateInHz, int audioFormat, int channelCount) {
                UtteranceProgressListener listener = this.this$1.this$0.mUtteranceProgressListener;
                if (listener != null) {
                    listener.onBeginSynthesis(utteranceId, sampleRateInHz, audioFormat, channelCount);
                }
            }

            public void onAudioAvailable(String utteranceId, byte[] audio) {
                UtteranceProgressListener listener = this.this$1.this$0.mUtteranceProgressListener;
                if (listener != null) {
                    listener.onAudioAvailable(utteranceId, audio);
                }
            }
        }

        private class SetupConnectionAsyncTask extends AsyncTask<Void, Void, Integer> {
            private final ComponentName mName;
            final /* synthetic */ Connection this$1;

            public SetupConnectionAsyncTask(Connection this$1, ComponentName name) {
                this.this$1 = this$1;
                this.mName = name;
            }

            protected /* bridge */ /* synthetic */ Object doInBackground(Object[] params) {
                return doInBackground((Void[]) params);
            }

            protected Integer doInBackground(Void... params) {
                synchronized (this.this$1.this$0.mStartLock) {
                    if (isCancelled()) {
                        return null;
                    }
                    try {
                        this.this$1.mService.setCallback(this.this$1.getCallerIdentity(), this.this$1.mCallback);
                        if (this.this$1.this$0.mParams.getString(Engine.KEY_PARAM_LANGUAGE) == null) {
                            String[] defaultLanguage = this.this$1.mService.getClientDefaultLanguage();
                            this.this$1.this$0.mParams.putString(Engine.KEY_PARAM_LANGUAGE, defaultLanguage[0]);
                            this.this$1.this$0.mParams.putString(Engine.KEY_PARAM_COUNTRY, defaultLanguage[1]);
                            this.this$1.this$0.mParams.putString(Engine.KEY_PARAM_VARIANT, defaultLanguage[2]);
                            this.this$1.this$0.mParams.putString(Engine.KEY_PARAM_VOICE_NAME, this.this$1.mService.getDefaultVoiceNameFor(defaultLanguage[0], defaultLanguage[1], defaultLanguage[2]));
                        }
                        Log.i(TextToSpeech.TAG, "Set up connection to " + this.mName);
                        Integer valueOf = Integer.valueOf(0);
                        return valueOf;
                    } catch (RemoteException e) {
                        Log.e(TextToSpeech.TAG, "Error connecting to service, setCallback() failed");
                        return Integer.valueOf(-1);
                    }
                }
            }

            protected /* bridge */ /* synthetic */ void onPostExecute(Object result) {
                onPostExecute((Integer) result);
            }

            protected void onPostExecute(Integer result) {
                synchronized (this.this$1.this$0.mStartLock) {
                    if (this.this$1.mOnSetupConnectionAsyncTask == this) {
                        this.this$1.mOnSetupConnectionAsyncTask = null;
                    }
                    this.this$1.mEstablished = true;
                    this.this$1.this$0.dispatchOnInit(result.intValue());
                }
            }
        }

        /* synthetic */ Connection(TextToSpeech this$0, Connection connection) {
            this(this$0);
        }

        private Connection(TextToSpeech this$0) {
            this.this$0 = this$0;
            this.mCallback = new AnonymousClass1(this);
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (this.this$0.mStartLock) {
                this.this$0.mConnectingServiceConnection = null;
                Log.i(TextToSpeech.TAG, "Connected to " + name);
                if (this.mOnSetupConnectionAsyncTask != null) {
                    this.mOnSetupConnectionAsyncTask.cancel(false);
                }
                this.mService = ITextToSpeechService.Stub.asInterface(service);
                this.this$0.mServiceConnection = this;
                this.mEstablished = false;
                this.mOnSetupConnectionAsyncTask = new SetupConnectionAsyncTask(this, name);
                this.mOnSetupConnectionAsyncTask.execute(new Void[0]);
            }
        }

        public IBinder getCallerIdentity() {
            return this.mCallback;
        }

        private boolean clearServiceConnection() {
            boolean result;
            synchronized (this.this$0.mStartLock) {
                result = false;
                if (this.mOnSetupConnectionAsyncTask != null) {
                    result = this.mOnSetupConnectionAsyncTask.cancel(false);
                    this.mOnSetupConnectionAsyncTask = null;
                }
                this.mService = null;
                if (this.this$0.mServiceConnection == this) {
                    this.this$0.mServiceConnection = null;
                }
            }
            return result;
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.i(TextToSpeech.TAG, "Asked to disconnect from " + name);
            if (clearServiceConnection()) {
                this.this$0.dispatchOnInit(-1);
            }
        }

        public void disconnect() {
            this.this$0.mContext.unbindService(this);
            clearServiceConnection();
        }

        public boolean isEstablished() {
            return this.mService != null ? this.mEstablished : false;
        }

        public <R> R runAction(Action<R> action, R errorResult, String method, boolean reconnect, boolean onlyEstablishedConnection) {
            synchronized (this.this$0.mStartLock) {
                try {
                    if (this.mService == null) {
                        Log.w(TextToSpeech.TAG, method + " failed: not connected to TTS engine");
                        return errorResult;
                    }
                    if (onlyEstablishedConnection) {
                        if (!isEstablished()) {
                            Log.w(TextToSpeech.TAG, method + " failed: TTS engine connection not fully set up");
                            return errorResult;
                        }
                    }
                    R run = action.run(this.mService);
                    return run;
                } catch (RemoteException ex) {
                    Log.e(TextToSpeech.TAG, method + " failed", ex);
                    if (reconnect) {
                        disconnect();
                        this.this$0.initTts();
                    }
                    return errorResult;
                }
            }
        }
    }

    public class Engine {
        public static final String ACTION_CHECK_TTS_DATA = "android.speech.tts.engine.CHECK_TTS_DATA";
        public static final String ACTION_GET_SAMPLE_TEXT = "android.speech.tts.engine.GET_SAMPLE_TEXT";
        public static final String ACTION_INSTALL_TTS_DATA = "android.speech.tts.engine.INSTALL_TTS_DATA";
        public static final String ACTION_TTS_DATA_INSTALLED = "android.speech.tts.engine.TTS_DATA_INSTALLED";
        @Deprecated
        public static final int CHECK_VOICE_DATA_BAD_DATA = -1;
        public static final int CHECK_VOICE_DATA_FAIL = 0;
        @Deprecated
        public static final int CHECK_VOICE_DATA_MISSING_DATA = -2;
        @Deprecated
        public static final int CHECK_VOICE_DATA_MISSING_VOLUME = -3;
        public static final int CHECK_VOICE_DATA_PASS = 1;
        @Deprecated
        public static final String DEFAULT_ENGINE = "com.svox.pico";
        public static final float DEFAULT_PAN = 0.0f;
        public static final int DEFAULT_PITCH = 100;
        public static final int DEFAULT_RATE = 100;
        public static final int DEFAULT_STREAM = 3;
        public static final float DEFAULT_VOLUME = 1.0f;
        public static final String EXTRA_AVAILABLE_VOICES = "availableVoices";
        @Deprecated
        public static final String EXTRA_CHECK_VOICE_DATA_FOR = "checkVoiceDataFor";
        public static final String EXTRA_SAMPLE_TEXT = "sampleText";
        @Deprecated
        public static final String EXTRA_TTS_DATA_INSTALLED = "dataInstalled";
        public static final String EXTRA_UNAVAILABLE_VOICES = "unavailableVoices";
        @Deprecated
        public static final String EXTRA_VOICE_DATA_FILES = "dataFiles";
        @Deprecated
        public static final String EXTRA_VOICE_DATA_FILES_INFO = "dataFilesInfo";
        @Deprecated
        public static final String EXTRA_VOICE_DATA_ROOT_DIRECTORY = "dataRoot";
        public static final String INTENT_ACTION_TTS_SERVICE = "android.intent.action.TTS_SERVICE";
        @Deprecated
        public static final String KEY_FEATURE_EMBEDDED_SYNTHESIS = "embeddedTts";
        public static final String KEY_FEATURE_NETWORK_RETRIES_COUNT = "networkRetriesCount";
        @Deprecated
        public static final String KEY_FEATURE_NETWORK_SYNTHESIS = "networkTts";
        public static final String KEY_FEATURE_NETWORK_TIMEOUT_MS = "networkTimeoutMs";
        public static final String KEY_FEATURE_NOT_INSTALLED = "notInstalled";
        public static final String KEY_PARAM_AUDIO_ATTRIBUTES = "audioAttributes";
        public static final String KEY_PARAM_COUNTRY = "country";
        public static final String KEY_PARAM_ENGINE = "engine";
        public static final String KEY_PARAM_LANGUAGE = "language";
        public static final String KEY_PARAM_PAN = "pan";
        public static final String KEY_PARAM_PITCH = "pitch";
        public static final String KEY_PARAM_RATE = "rate";
        public static final String KEY_PARAM_SESSION_ID = "sessionId";
        public static final String KEY_PARAM_STREAM = "streamType";
        public static final String KEY_PARAM_UTTERANCE_ID = "utteranceId";
        public static final String KEY_PARAM_VARIANT = "variant";
        public static final String KEY_PARAM_VOICE_NAME = "voiceName";
        public static final String KEY_PARAM_VOLUME = "volume";
        public static final String SERVICE_META_DATA = "android.speech.tts";
        public static final int USE_DEFAULTS = 0;
        final /* synthetic */ TextToSpeech this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.Engine.<init>(android.speech.tts.TextToSpeech):void, dex: 
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
        public Engine(android.speech.tts.TextToSpeech r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.speech.tts.TextToSpeech.Engine.<init>(android.speech.tts.TextToSpeech):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.Engine.<init>(android.speech.tts.TextToSpeech):void");
        }
    }

    public static class EngineInfo {
        public int icon;
        public String label;
        public String name;
        public int priority;
        public boolean system;

        public EngineInfo() {
        }

        public String toString() {
            return "EngineInfo{name=" + this.name + "}";
        }
    }

    public interface OnInitListener {
        void onInit(int i);
    }

    @Deprecated
    public interface OnUtteranceCompletedListener {
        void onUtteranceCompleted(String str);
    }

    public TextToSpeech(Context context, OnInitListener listener) {
        this(context, listener, null);
    }

    public TextToSpeech(Context context, OnInitListener listener, String engine) {
        this(context, listener, engine, null, true);
    }

    public TextToSpeech(Context context, OnInitListener listener, String engine, String packageName, boolean useFallback) {
        this.mStartLock = new Object();
        this.mParams = new Bundle();
        this.mCurrentEngine = null;
        this.mContext = context;
        this.mInitListener = listener;
        this.mRequestedEngine = engine;
        this.mUseFallback = useFallback;
        this.mEarcons = new HashMap();
        this.mUtterances = new HashMap();
        this.mUtteranceProgressListener = null;
        this.mEnginesHelper = new TtsEngines(this.mContext);
        initTts();
    }

    private <R> R runActionNoReconnect(Action<R> action, R errorResult, String method, boolean onlyEstablishedConnection) {
        return runAction(action, errorResult, method, false, onlyEstablishedConnection);
    }

    private <R> R runAction(Action<R> action, R errorResult, String method) {
        return runAction(action, errorResult, method, true, true);
    }

    private <R> R runAction(Action<R> action, R errorResult, String method, boolean reconnect, boolean onlyEstablishedConnection) {
        synchronized (this.mStartLock) {
            if (this.mServiceConnection == null) {
                Log.w(TAG, method + " failed: not bound to TTS engine");
                return errorResult;
            }
            R runAction = this.mServiceConnection.runAction(action, errorResult, method, reconnect, onlyEstablishedConnection);
            return runAction;
        }
    }

    private int initTts() {
        if (this.mRequestedEngine != null) {
            if (this.mEnginesHelper.isEngineInstalled(this.mRequestedEngine)) {
                if (connectToEngine(this.mRequestedEngine)) {
                    this.mCurrentEngine = this.mRequestedEngine;
                    return 0;
                } else if (!this.mUseFallback) {
                    this.mCurrentEngine = null;
                    dispatchOnInit(-1);
                    return -1;
                }
            } else if (!this.mUseFallback) {
                Log.i(TAG, "Requested engine not installed: " + this.mRequestedEngine);
                this.mCurrentEngine = null;
                dispatchOnInit(-1);
                return -1;
            }
        }
        String defaultEngine = getDefaultEngine();
        if (defaultEngine == null || defaultEngine.equals(this.mRequestedEngine) || !connectToEngine(defaultEngine)) {
            String highestRanked = this.mEnginesHelper.getHighestRankedEngineName();
            if (highestRanked == null || highestRanked.equals(this.mRequestedEngine) || highestRanked.equals(defaultEngine) || !connectToEngine(highestRanked)) {
                this.mCurrentEngine = null;
                dispatchOnInit(-1);
                return -1;
            }
            this.mCurrentEngine = highestRanked;
            return 0;
        }
        this.mCurrentEngine = defaultEngine;
        return 0;
    }

    private boolean connectToEngine(String engine) {
        Connection connection = new Connection(this, null);
        Intent intent = new Intent(Engine.INTENT_ACTION_TTS_SERVICE);
        intent.setPackage(engine);
        if (this.mContext.bindService(intent, connection, 1)) {
            Log.i(TAG, "Sucessfully bound to " + engine);
            this.mConnectingServiceConnection = connection;
            return true;
        }
        Log.e(TAG, "Failed to bind to " + engine);
        return false;
    }

    private void dispatchOnInit(int result) {
        synchronized (this.mStartLock) {
            if (this.mInitListener != null) {
                this.mInitListener.onInit(result);
                this.mInitListener = null;
            }
        }
    }

    private IBinder getCallerIdentity() {
        return this.mServiceConnection.getCallerIdentity();
    }

    public void shutdown() {
        synchronized (this.mStartLock) {
            if (this.mConnectingServiceConnection != null) {
                this.mContext.unbindService(this.mConnectingServiceConnection);
                this.mConnectingServiceConnection = null;
                return;
            }
            runActionNoReconnect(new AnonymousClass1(this), null, "shutdown", false);
        }
    }

    public int addSpeech(String text, String packagename, int resourceId) {
        synchronized (this.mStartLock) {
            this.mUtterances.put(text, makeResourceUri(packagename, resourceId));
        }
        return 0;
    }

    public int addSpeech(CharSequence text, String packagename, int resourceId) {
        synchronized (this.mStartLock) {
            this.mUtterances.put(text, makeResourceUri(packagename, resourceId));
        }
        return 0;
    }

    public int addSpeech(String text, String filename) {
        synchronized (this.mStartLock) {
            this.mUtterances.put(text, Uri.parse(filename));
        }
        return 0;
    }

    public int addSpeech(CharSequence text, File file) {
        synchronized (this.mStartLock) {
            this.mUtterances.put(text, Uri.fromFile(file));
        }
        return 0;
    }

    public int addEarcon(String earcon, String packagename, int resourceId) {
        synchronized (this.mStartLock) {
            this.mEarcons.put(earcon, makeResourceUri(packagename, resourceId));
        }
        return 0;
    }

    @Deprecated
    public int addEarcon(String earcon, String filename) {
        synchronized (this.mStartLock) {
            this.mEarcons.put(earcon, Uri.parse(filename));
        }
        return 0;
    }

    public int addEarcon(String earcon, File file) {
        synchronized (this.mStartLock) {
            this.mEarcons.put(earcon, Uri.fromFile(file));
        }
        return 0;
    }

    private Uri makeResourceUri(String packageName, int resourceId) {
        return new Builder().scheme("android.resource").encodedAuthority(packageName).appendEncodedPath(String.valueOf(resourceId)).build();
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
    public int speak(java.lang.CharSequence r7, int r8, android.os.Bundle r9, java.lang.String r10) {
        /*
        r6 = this;
        r0 = new android.speech.tts.TextToSpeech$2;
        r1 = r6;
        r2 = r7;
        r3 = r8;
        r4 = r9;
        r5 = r10;
        r0.<init>(r1, r2, r3, r4, r5);
        r1 = -1;
        r1 = java.lang.Integer.valueOf(r1);
        r2 = "speak";
        r0 = r6.runAction(r0, r1, r2);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.speak(java.lang.CharSequence, int, android.os.Bundle, java.lang.String):int");
    }

    @Deprecated
    public int speak(String text, int queueMode, HashMap<String, String> params) {
        String str = null;
        Bundle convertParamsHashMaptoBundle = convertParamsHashMaptoBundle(params);
        if (params != null) {
            str = (String) params.get(Engine.KEY_PARAM_UTTERANCE_ID);
        }
        return speak(text, queueMode, convertParamsHashMaptoBundle, str);
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
    public int playEarcon(java.lang.String r7, int r8, android.os.Bundle r9, java.lang.String r10) {
        /*
        r6 = this;
        r0 = new android.speech.tts.TextToSpeech$3;
        r1 = r6;
        r2 = r7;
        r3 = r8;
        r4 = r9;
        r5 = r10;
        r0.<init>(r1, r2, r3, r4, r5);
        r1 = -1;
        r1 = java.lang.Integer.valueOf(r1);
        r2 = "playEarcon";
        r0 = r6.runAction(r0, r1, r2);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.playEarcon(java.lang.String, int, android.os.Bundle, java.lang.String):int");
    }

    @Deprecated
    public int playEarcon(String earcon, int queueMode, HashMap<String, String> params) {
        String str = null;
        Bundle convertParamsHashMaptoBundle = convertParamsHashMaptoBundle(params);
        if (params != null) {
            str = (String) params.get(Engine.KEY_PARAM_UTTERANCE_ID);
        }
        return playEarcon(earcon, queueMode, convertParamsHashMaptoBundle, str);
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
    public int playSilentUtterance(long r8, int r10, java.lang.String r11) {
        /*
        r7 = this;
        r0 = new android.speech.tts.TextToSpeech$4;
        r1 = r7;
        r2 = r8;
        r4 = r10;
        r5 = r11;
        r0.<init>(r1, r2, r4, r5);
        r1 = -1;
        r1 = java.lang.Integer.valueOf(r1);
        r2 = "playSilentUtterance";
        r0 = r7.runAction(r0, r1, r2);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.playSilentUtterance(long, int, java.lang.String):int");
    }

    @Deprecated
    public int playSilence(long durationInMs, int queueMode, HashMap<String, String> params) {
        String str = null;
        if (params != null) {
            str = (String) params.get(Engine.KEY_PARAM_UTTERANCE_ID);
        }
        return playSilentUtterance(durationInMs, queueMode, str);
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
    @java.lang.Deprecated
    public java.util.Set<java.lang.String> getFeatures(java.util.Locale r4) {
        /*
        r3 = this;
        r0 = new android.speech.tts.TextToSpeech$5;
        r0.<init>(r3, r4);
        r1 = "getFeatures";
        r2 = 0;
        r0 = r3.runAction(r0, r2, r1);
        r0 = (java.util.Set) r0;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.getFeatures(java.util.Locale):java.util.Set<java.lang.String>");
    }

    public boolean isSpeaking() {
        return ((Boolean) runAction(new AnonymousClass6(this), Boolean.valueOf(false), "isSpeaking")).booleanValue();
    }

    public int stop() {
        return ((Integer) runAction(new AnonymousClass7(this), Integer.valueOf(-1), "stop")).intValue();
    }

    public int setSpeechRate(float speechRate) {
        if (speechRate > 0.0f) {
            int intRate = (int) (100.0f * speechRate);
            if (intRate > 0) {
                synchronized (this.mStartLock) {
                    this.mParams.putInt(Engine.KEY_PARAM_RATE, intRate);
                }
                return 0;
            }
        }
        return -1;
    }

    public int setPitch(float pitch) {
        if (pitch > 0.0f) {
            int intPitch = (int) (100.0f * pitch);
            if (intPitch > 0) {
                synchronized (this.mStartLock) {
                    this.mParams.putInt(Engine.KEY_PARAM_PITCH, intPitch);
                }
                return 0;
            }
        }
        return -1;
    }

    public int setAudioAttributes(AudioAttributes audioAttributes) {
        if (audioAttributes == null) {
            return -1;
        }
        synchronized (this.mStartLock) {
            this.mParams.putParcelable(Engine.KEY_PARAM_AUDIO_ATTRIBUTES, audioAttributes);
        }
        return 0;
    }

    public String getCurrentEngine() {
        return this.mCurrentEngine;
    }

    @Deprecated
    public Locale getDefaultLanguage() {
        return (Locale) runAction(new AnonymousClass8(this), null, "getDefaultLanguage");
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
    public int setLanguage(java.util.Locale r4) {
        /*
        r3 = this;
        r0 = new android.speech.tts.TextToSpeech$9;
        r0.<init>(r3, r4);
        r1 = -2;
        r1 = java.lang.Integer.valueOf(r1);
        r2 = "setLanguage";
        r0 = r3.runAction(r0, r1, r2);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.setLanguage(java.util.Locale):int");
    }

    @Deprecated
    public Locale getLanguage() {
        return (Locale) runAction(new Action<Locale>() {
            public Locale run(ITextToSpeechService service) {
                return new Locale(TextToSpeech.this.mParams.getString(Engine.KEY_PARAM_LANGUAGE, PhoneConstants.MVNO_TYPE_NONE), TextToSpeech.this.mParams.getString(Engine.KEY_PARAM_COUNTRY, PhoneConstants.MVNO_TYPE_NONE), TextToSpeech.this.mParams.getString(Engine.KEY_PARAM_VARIANT, PhoneConstants.MVNO_TYPE_NONE));
            }
        }, null, "getLanguage");
    }

    public Set<Locale> getAvailableLanguages() {
        return (Set) runAction(new AnonymousClass11(this), null, "getAvailableLanguages");
    }

    public Set<Voice> getVoices() {
        return (Set) runAction(new AnonymousClass12(this), null, "getVoices");
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
    public int setVoice(android.speech.tts.Voice r4) {
        /*
        r3 = this;
        r0 = new android.speech.tts.TextToSpeech$13;
        r0.<init>(r3, r4);
        r1 = -2;
        r1 = java.lang.Integer.valueOf(r1);
        r2 = "setVoice";
        r0 = r3.runAction(r0, r1, r2);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.setVoice(android.speech.tts.Voice):int");
    }

    public Voice getVoice() {
        return (Voice) runAction(new AnonymousClass14(this), null, "getVoice");
    }

    private Voice getVoice(ITextToSpeechService service, String voiceName) throws RemoteException {
        List<Voice> voices = service.getVoices();
        if (voices == null) {
            Log.w(TAG, "getVoices returned null");
            return null;
        }
        for (Voice voice : voices) {
            if (voice.getName().equals(voiceName)) {
                return voice;
            }
        }
        Log.w(TAG, "Could not find voice " + voiceName + " in voice list");
        return null;
    }

    public Voice getDefaultVoice() {
        return (Voice) runAction(new AnonymousClass15(this), null, "getDefaultVoice");
    }

    public int isLanguageAvailable(final Locale loc) {
        return ((Integer) runAction(new Action<Integer>(this) {
            final /* synthetic */ TextToSpeech this$0;

            public /* bridge */ /* synthetic */ Object run(ITextToSpeechService service) throws RemoteException {
                return run(service);
            }

            public Integer run(ITextToSpeechService service) throws RemoteException {
                try {
                    try {
                        return Integer.valueOf(service.isLanguageAvailable(loc.getISO3Language(), loc.getISO3Country(), loc.getVariant()));
                    } catch (MissingResourceException e) {
                        Log.w(TextToSpeech.TAG, "Couldn't retrieve ISO 3166 country code for locale: " + loc, e);
                        return Integer.valueOf(-2);
                    }
                } catch (MissingResourceException e2) {
                    Log.w(TextToSpeech.TAG, "Couldn't retrieve ISO 639-2/T language code for locale: " + loc, e2);
                    return Integer.valueOf(-2);
                }
            }
        }, Integer.valueOf(-2), "isLanguageAvailable")).intValue();
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
    public int synthesizeToFile(java.lang.CharSequence r7, android.os.Bundle r8, java.io.File r9, java.lang.String r10) {
        /*
        r6 = this;
        r0 = new android.speech.tts.TextToSpeech$17;
        r1 = r6;
        r2 = r9;
        r3 = r7;
        r4 = r8;
        r5 = r10;
        r0.<init>(r1, r2, r3, r4, r5);
        r1 = -1;
        r1 = java.lang.Integer.valueOf(r1);
        r2 = "synthesizeToFile";
        r0 = r6.runAction(r0, r1, r2);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TextToSpeech.synthesizeToFile(java.lang.CharSequence, android.os.Bundle, java.io.File, java.lang.String):int");
    }

    @Deprecated
    public int synthesizeToFile(String text, HashMap<String, String> params, String filename) {
        return synthesizeToFile(text, convertParamsHashMaptoBundle(params), new File(filename), (String) params.get(Engine.KEY_PARAM_UTTERANCE_ID));
    }

    private Bundle convertParamsHashMaptoBundle(HashMap<String, String> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        Bundle bundle = new Bundle();
        copyIntParam(bundle, params, Engine.KEY_PARAM_STREAM);
        copyIntParam(bundle, params, Engine.KEY_PARAM_SESSION_ID);
        copyStringParam(bundle, params, Engine.KEY_PARAM_UTTERANCE_ID);
        copyFloatParam(bundle, params, Engine.KEY_PARAM_VOLUME);
        copyFloatParam(bundle, params, Engine.KEY_PARAM_PAN);
        copyStringParam(bundle, params, Engine.KEY_FEATURE_NETWORK_SYNTHESIS);
        copyStringParam(bundle, params, Engine.KEY_FEATURE_EMBEDDED_SYNTHESIS);
        copyIntParam(bundle, params, Engine.KEY_FEATURE_NETWORK_TIMEOUT_MS);
        copyIntParam(bundle, params, Engine.KEY_FEATURE_NETWORK_RETRIES_COUNT);
        if (!TextUtils.isEmpty(this.mCurrentEngine)) {
            for (Entry<String, String> entry : params.entrySet()) {
                String key = (String) entry.getKey();
                if (key != null && key.startsWith(this.mCurrentEngine)) {
                    bundle.putString(key, (String) entry.getValue());
                }
            }
        }
        return bundle;
    }

    private Bundle getParams(Bundle params) {
        if (params == null || params.isEmpty()) {
            return this.mParams;
        }
        Bundle bundle = new Bundle(this.mParams);
        bundle.putAll(params);
        verifyIntegerBundleParam(bundle, Engine.KEY_PARAM_STREAM);
        verifyIntegerBundleParam(bundle, Engine.KEY_PARAM_SESSION_ID);
        verifyStringBundleParam(bundle, Engine.KEY_PARAM_UTTERANCE_ID);
        verifyFloatBundleParam(bundle, Engine.KEY_PARAM_VOLUME);
        verifyFloatBundleParam(bundle, Engine.KEY_PARAM_PAN);
        verifyBooleanBundleParam(bundle, Engine.KEY_FEATURE_NETWORK_SYNTHESIS);
        verifyBooleanBundleParam(bundle, Engine.KEY_FEATURE_EMBEDDED_SYNTHESIS);
        verifyIntegerBundleParam(bundle, Engine.KEY_FEATURE_NETWORK_TIMEOUT_MS);
        verifyIntegerBundleParam(bundle, Engine.KEY_FEATURE_NETWORK_RETRIES_COUNT);
        return bundle;
    }

    private static boolean verifyIntegerBundleParam(Bundle bundle, String key) {
        if (bundle.containsKey(key)) {
            boolean z;
            if (bundle.get(key) instanceof Integer) {
                z = true;
            } else {
                z = bundle.get(key) instanceof Long;
            }
            if (!z) {
                bundle.remove(key);
                Log.w(TAG, "Synthesis request paramter " + key + " containst value " + " with invalid type. Should be an Integer or a Long");
                return false;
            }
        }
        return true;
    }

    private static boolean verifyStringBundleParam(Bundle bundle, String key) {
        if (!bundle.containsKey(key) || (bundle.get(key) instanceof String)) {
            return true;
        }
        bundle.remove(key);
        Log.w(TAG, "Synthesis request paramter " + key + " containst value " + " with invalid type. Should be a String");
        return false;
    }

    private static boolean verifyBooleanBundleParam(Bundle bundle, String key) {
        if (bundle.containsKey(key)) {
            boolean z;
            if (bundle.get(key) instanceof Boolean) {
                z = true;
            } else {
                z = bundle.get(key) instanceof String;
            }
            if (!z) {
                bundle.remove(key);
                Log.w(TAG, "Synthesis request paramter " + key + " containst value " + " with invalid type. Should be a Boolean or String");
                return false;
            }
        }
        return true;
    }

    private static boolean verifyFloatBundleParam(Bundle bundle, String key) {
        if (bundle.containsKey(key)) {
            boolean z;
            if (bundle.get(key) instanceof Float) {
                z = true;
            } else {
                z = bundle.get(key) instanceof Double;
            }
            if (!z) {
                bundle.remove(key);
                Log.w(TAG, "Synthesis request paramter " + key + " containst value " + " with invalid type. Should be a Float or a Double");
                return false;
            }
        }
        return true;
    }

    private void copyStringParam(Bundle bundle, HashMap<String, String> params, String key) {
        String value = (String) params.get(key);
        if (value != null) {
            bundle.putString(key, value);
        }
    }

    private void copyIntParam(Bundle bundle, HashMap<String, String> params, String key) {
        String valueString = (String) params.get(key);
        if (!TextUtils.isEmpty(valueString)) {
            try {
                bundle.putInt(key, Integer.parseInt(valueString));
            } catch (NumberFormatException e) {
            }
        }
    }

    private void copyFloatParam(Bundle bundle, HashMap<String, String> params, String key) {
        String valueString = (String) params.get(key);
        if (!TextUtils.isEmpty(valueString)) {
            try {
                bundle.putFloat(key, Float.parseFloat(valueString));
            } catch (NumberFormatException e) {
            }
        }
    }

    @Deprecated
    public int setOnUtteranceCompletedListener(OnUtteranceCompletedListener listener) {
        this.mUtteranceProgressListener = UtteranceProgressListener.from(listener);
        return 0;
    }

    public int setOnUtteranceProgressListener(UtteranceProgressListener listener) {
        this.mUtteranceProgressListener = listener;
        return 0;
    }

    @Deprecated
    public int setEngineByPackageName(String enginePackageName) {
        this.mRequestedEngine = enginePackageName;
        return initTts();
    }

    public String getDefaultEngine() {
        return this.mEnginesHelper.getDefaultEngine();
    }

    @Deprecated
    public boolean areDefaultsEnforced() {
        return false;
    }

    public List<EngineInfo> getEngines() {
        return this.mEnginesHelper.getEngines();
    }

    public static int getMaxSpeechInputLength() {
        return 4000;
    }
}
