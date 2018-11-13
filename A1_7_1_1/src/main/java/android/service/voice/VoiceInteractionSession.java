package android.service.voice;

import android.R;
import android.app.Dialog;
import android.app.Instrumentation;
import android.app.VoiceInteractor.PickOptionRequest.Option;
import android.app.VoiceInteractor.Prompt;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Region;
import android.inputmethodservice.SoftInputWindow;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.view.KeyEvent;
import android.view.KeyEvent.Callback;
import android.view.KeyEvent.DispatcherState;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnComputeInternalInsetsListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.android.internal.app.IVoiceInteractionManagerService;
import com.android.internal.app.IVoiceInteractionSessionShowCallback;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.app.IVoiceInteractor.Stub;
import com.android.internal.app.IVoiceInteractorCallback;
import com.android.internal.app.IVoiceInteractorRequest;
import com.android.internal.os.HandlerCaller;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;

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
public class VoiceInteractionSession implements Callback, ComponentCallbacks2 {
    static final boolean DEBUG = false;
    public static final String KEY_CONTENT = "content";
    public static final String KEY_DATA = "data";
    public static final String KEY_RECEIVER_EXTRAS = "receiverExtras";
    public static final String KEY_STRUCTURE = "structure";
    static final int MSG_CANCEL = 7;
    static final int MSG_CLOSE_SYSTEM_DIALOGS = 102;
    static final int MSG_DESTROY = 103;
    static final int MSG_HANDLE_ASSIST = 104;
    static final int MSG_HANDLE_SCREENSHOT = 105;
    static final int MSG_HIDE = 107;
    static final int MSG_ON_LOCKSCREEN_SHOWN = 108;
    static final int MSG_SHOW = 106;
    static final int MSG_START_ABORT_VOICE = 4;
    static final int MSG_START_COMMAND = 5;
    static final int MSG_START_COMPLETE_VOICE = 3;
    static final int MSG_START_CONFIRMATION = 1;
    static final int MSG_START_PICK_OPTION = 2;
    static final int MSG_SUPPORTS_COMMANDS = 6;
    static final int MSG_TASK_FINISHED = 101;
    static final int MSG_TASK_STARTED = 100;
    public static final int SHOW_SOURCE_ACTIVITY = 16;
    public static final int SHOW_SOURCE_APPLICATION = 8;
    public static final int SHOW_SOURCE_ASSIST_GESTURE = 4;
    public static final int SHOW_WITH_ASSIST = 1;
    public static final int SHOW_WITH_SCREENSHOT = 2;
    static final String TAG = "VoiceInteractionSession";
    final ArrayMap<IBinder, Request> mActiveRequests;
    final MyCallbacks mCallbacks;
    FrameLayout mContentFrame;
    final Context mContext;
    final DispatcherState mDispatcherState;
    final HandlerCaller mHandlerCaller;
    boolean mInShowWindow;
    LayoutInflater mInflater;
    boolean mInitialized;
    final OnComputeInternalInsetsListener mInsetsComputer;
    final IVoiceInteractor mInteractor;
    View mRootView;
    final IVoiceInteractionSession mSession;
    IVoiceInteractionManagerService mSystemService;
    int mTheme;
    TypedArray mThemeAttrs;
    final Insets mTmpInsets;
    IBinder mToken;
    final WeakReference<VoiceInteractionSession> mWeakRef;
    SoftInputWindow mWindow;
    boolean mWindowAdded;
    boolean mWindowVisible;
    boolean mWindowWasVisible;

    /* renamed from: android.service.voice.VoiceInteractionSession$1 */
    class AnonymousClass1 extends Stub {
        final /* synthetic */ VoiceInteractionSession this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.1.<init>(android.service.voice.VoiceInteractionSession):void, dex: 
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
        AnonymousClass1(android.service.voice.VoiceInteractionSession r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.1.<init>(android.service.voice.VoiceInteractionSession):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.1.<init>(android.service.voice.VoiceInteractionSession):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.service.voice.VoiceInteractionSession.1.startAbortVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex:  in method: android.service.voice.VoiceInteractionSession.1.startAbortVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.service.voice.VoiceInteractionSession.1.startAbortVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
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
        public com.android.internal.app.IVoiceInteractorRequest startAbortVoice(java.lang.String r1, com.android.internal.app.IVoiceInteractorCallback r2, android.app.VoiceInteractor.Prompt r3, android.os.Bundle r4) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.service.voice.VoiceInteractionSession.1.startAbortVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex:  in method: android.service.voice.VoiceInteractionSession.1.startAbortVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.1.startAbortVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.service.voice.VoiceInteractionSession.1.startCommand(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, java.lang.String, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex:  in method: android.service.voice.VoiceInteractionSession.1.startCommand(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, java.lang.String, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.service.voice.VoiceInteractionSession.1.startCommand(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, java.lang.String, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
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
        public com.android.internal.app.IVoiceInteractorRequest startCommand(java.lang.String r1, com.android.internal.app.IVoiceInteractorCallback r2, java.lang.String r3, android.os.Bundle r4) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.service.voice.VoiceInteractionSession.1.startCommand(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, java.lang.String, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex:  in method: android.service.voice.VoiceInteractionSession.1.startCommand(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, java.lang.String, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.1.startCommand(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, java.lang.String, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.service.voice.VoiceInteractionSession.1.startCompleteVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex:  in method: android.service.voice.VoiceInteractionSession.1.startCompleteVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.service.voice.VoiceInteractionSession.1.startCompleteVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
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
        public com.android.internal.app.IVoiceInteractorRequest startCompleteVoice(java.lang.String r1, com.android.internal.app.IVoiceInteractorCallback r2, android.app.VoiceInteractor.Prompt r3, android.os.Bundle r4) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.service.voice.VoiceInteractionSession.1.startCompleteVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex:  in method: android.service.voice.VoiceInteractionSession.1.startCompleteVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.1.startCompleteVoice(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.service.voice.VoiceInteractionSession.1.startConfirmation(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex:  in method: android.service.voice.VoiceInteractionSession.1.startConfirmation(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.service.voice.VoiceInteractionSession.1.startConfirmation(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
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
        public com.android.internal.app.IVoiceInteractorRequest startConfirmation(java.lang.String r1, com.android.internal.app.IVoiceInteractorCallback r2, android.app.VoiceInteractor.Prompt r3, android.os.Bundle r4) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.service.voice.VoiceInteractionSession.1.startConfirmation(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex:  in method: android.service.voice.VoiceInteractionSession.1.startConfirmation(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.1.startConfirmation(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.service.voice.VoiceInteractionSession.1.startPickOption(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex:  in method: android.service.voice.VoiceInteractionSession.1.startPickOption(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.service.voice.VoiceInteractionSession.1.startPickOption(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
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
        public com.android.internal.app.IVoiceInteractorRequest startPickOption(java.lang.String r1, com.android.internal.app.IVoiceInteractorCallback r2, android.app.VoiceInteractor.Prompt r3, android.app.VoiceInteractor.PickOptionRequest.Option[] r4, android.os.Bundle r5) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.service.voice.VoiceInteractionSession.1.startPickOption(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex:  in method: android.service.voice.VoiceInteractionSession.1.startPickOption(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.1.startPickOption(java.lang.String, com.android.internal.app.IVoiceInteractorCallback, android.app.VoiceInteractor$Prompt, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):com.android.internal.app.IVoiceInteractorRequest");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.1.supportsCommands(java.lang.String, java.lang.String[]):boolean[], dex: 
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
        public boolean[] supportsCommands(java.lang.String r1, java.lang.String[] r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.1.supportsCommands(java.lang.String, java.lang.String[]):boolean[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.1.supportsCommands(java.lang.String, java.lang.String[]):boolean[]");
        }
    }

    /* renamed from: android.service.voice.VoiceInteractionSession$2 */
    class AnonymousClass2 extends IVoiceInteractionSession.Stub {
        final /* synthetic */ VoiceInteractionSession this$0;

        /* renamed from: android.service.voice.VoiceInteractionSession$2$1 */
        class AnonymousClass1 extends Thread {
            final /* synthetic */ AnonymousClass2 this$1;
            final /* synthetic */ AssistContent val$content;
            final /* synthetic */ int val$count;
            final /* synthetic */ Bundle val$data;
            final /* synthetic */ int val$index;
            final /* synthetic */ AssistStructure val$structure;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.2.1.<init>(android.service.voice.VoiceInteractionSession$2, java.lang.String, android.app.assist.AssistStructure, android.os.Bundle, android.app.assist.AssistContent, int, int):void, dex: 
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
            AnonymousClass1(android.service.voice.VoiceInteractionSession.AnonymousClass2 r1, java.lang.String r2, android.app.assist.AssistStructure r3, android.os.Bundle r4, android.app.assist.AssistContent r5, int r6, int r7) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.2.1.<init>(android.service.voice.VoiceInteractionSession$2, java.lang.String, android.app.assist.AssistStructure, android.os.Bundle, android.app.assist.AssistContent, int, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.2.1.<init>(android.service.voice.VoiceInteractionSession$2, java.lang.String, android.app.assist.AssistStructure, android.os.Bundle, android.app.assist.AssistContent, int, int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.1.run():void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.2.1.run():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.2.<init>(android.service.voice.VoiceInteractionSession):void, dex: 
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
        AnonymousClass2(android.service.voice.VoiceInteractionSession r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.2.<init>(android.service.voice.VoiceInteractionSession):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.2.<init>(android.service.voice.VoiceInteractionSession):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.closeSystemDialogs():void, dex: 
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
        public void closeSystemDialogs() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.closeSystemDialogs():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.2.closeSystemDialogs():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.destroy():void, dex: 
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
        public void destroy() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.destroy():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.2.destroy():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.2.handleAssist(android.os.Bundle, android.app.assist.AssistStructure, android.app.assist.AssistContent, int, int):void, dex: 
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
        public void handleAssist(android.os.Bundle r1, android.app.assist.AssistStructure r2, android.app.assist.AssistContent r3, int r4, int r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.2.handleAssist(android.os.Bundle, android.app.assist.AssistStructure, android.app.assist.AssistContent, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.2.handleAssist(android.os.Bundle, android.app.assist.AssistStructure, android.app.assist.AssistContent, int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.handleScreenshot(android.graphics.Bitmap):void, dex: 
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
        public void handleScreenshot(android.graphics.Bitmap r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.handleScreenshot(android.graphics.Bitmap):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.2.handleScreenshot(android.graphics.Bitmap):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.hide():void, dex: 
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
        public void hide() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.hide():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.2.hide():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.onLockscreenShown():void, dex: 
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
        public void onLockscreenShown() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.onLockscreenShown():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.2.onLockscreenShown():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.show(android.os.Bundle, int, com.android.internal.app.IVoiceInteractionSessionShowCallback):void, dex: 
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
        public void show(android.os.Bundle r1, int r2, com.android.internal.app.IVoiceInteractionSessionShowCallback r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.show(android.os.Bundle, int, com.android.internal.app.IVoiceInteractionSessionShowCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.2.show(android.os.Bundle, int, com.android.internal.app.IVoiceInteractionSessionShowCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.taskFinished(android.content.Intent, int):void, dex: 
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
        public void taskFinished(android.content.Intent r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.taskFinished(android.content.Intent, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.2.taskFinished(android.content.Intent, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.taskStarted(android.content.Intent, int):void, dex: 
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
        public void taskStarted(android.content.Intent r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.2.taskStarted(android.content.Intent, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.2.taskStarted(android.content.Intent, int):void");
        }
    }

    /* renamed from: android.service.voice.VoiceInteractionSession$3 */
    class AnonymousClass3 implements OnComputeInternalInsetsListener {
        final /* synthetic */ VoiceInteractionSession this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.3.<init>(android.service.voice.VoiceInteractionSession):void, dex: 
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
        AnonymousClass3(android.service.voice.VoiceInteractionSession r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.3.<init>(android.service.voice.VoiceInteractionSession):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.3.<init>(android.service.voice.VoiceInteractionSession):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.service.voice.VoiceInteractionSession.3.onComputeInternalInsets(android.view.ViewTreeObserver$InternalInsetsInfo):void, dex:  in method: android.service.voice.VoiceInteractionSession.3.onComputeInternalInsets(android.view.ViewTreeObserver$InternalInsetsInfo):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.service.voice.VoiceInteractionSession.3.onComputeInternalInsets(android.view.ViewTreeObserver$InternalInsetsInfo):void, dex: 
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
        public void onComputeInternalInsets(android.view.ViewTreeObserver.InternalInsetsInfo r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.service.voice.VoiceInteractionSession.3.onComputeInternalInsets(android.view.ViewTreeObserver$InternalInsetsInfo):void, dex:  in method: android.service.voice.VoiceInteractionSession.3.onComputeInternalInsets(android.view.ViewTreeObserver$InternalInsetsInfo):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.3.onComputeInternalInsets(android.view.ViewTreeObserver$InternalInsetsInfo):void");
        }
    }

    /* renamed from: android.service.voice.VoiceInteractionSession$4 */
    class AnonymousClass4 implements OnPreDrawListener {
        final /* synthetic */ VoiceInteractionSession this$0;
        final /* synthetic */ IVoiceInteractionSessionShowCallback val$showCallback;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.4.<init>(android.service.voice.VoiceInteractionSession, com.android.internal.app.IVoiceInteractionSessionShowCallback):void, dex: 
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
        AnonymousClass4(android.service.voice.VoiceInteractionSession r1, com.android.internal.app.IVoiceInteractionSessionShowCallback r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.4.<init>(android.service.voice.VoiceInteractionSession, com.android.internal.app.IVoiceInteractionSessionShowCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.4.<init>(android.service.voice.VoiceInteractionSession, com.android.internal.app.IVoiceInteractionSessionShowCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.4.onPreDraw():boolean, dex: 
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
        public boolean onPreDraw() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.4.onPreDraw():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.4.onPreDraw():boolean");
        }
    }

    public static class Request {
        final IVoiceInteractorCallback mCallback;
        final String mCallingPackage;
        final int mCallingUid;
        final Bundle mExtras;
        final IVoiceInteractorRequest mInterface;
        final WeakReference<VoiceInteractionSession> mSession;

        /* renamed from: android.service.voice.VoiceInteractionSession$Request$1 */
        class AnonymousClass1 extends IVoiceInteractorRequest.Stub {
            final /* synthetic */ Request this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.Request.1.<init>(android.service.voice.VoiceInteractionSession$Request):void, dex: 
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
            AnonymousClass1(android.service.voice.VoiceInteractionSession.Request r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.Request.1.<init>(android.service.voice.VoiceInteractionSession$Request):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.Request.1.<init>(android.service.voice.VoiceInteractionSession$Request):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.Request.1.cancel():void, dex: 
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
            public void cancel() throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.Request.1.cancel():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.Request.1.cancel():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.service.voice.VoiceInteractionSession.Request.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.os.Bundle):void, dex:  in method: android.service.voice.VoiceInteractionSession.Request.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.os.Bundle):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.service.voice.VoiceInteractionSession.Request.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.os.Bundle):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        Request(java.lang.String r1, int r2, com.android.internal.app.IVoiceInteractorCallback r3, android.service.voice.VoiceInteractionSession r4, android.os.Bundle r5) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.service.voice.VoiceInteractionSession.Request.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.os.Bundle):void, dex:  in method: android.service.voice.VoiceInteractionSession.Request.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.Request.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.Request.cancel():void, dex: 
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
        public void cancel() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.Request.cancel():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.Request.cancel():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: 0 in method: android.service.voice.VoiceInteractionSession.Request.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void, dex:  in method: android.service.voice.VoiceInteractionSession.Request.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: 0 in method: android.service.voice.VoiceInteractionSession.Request.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: com.android.dex.DexException: bogus registerCount: 0
            	at com.android.dx.io.instructions.InstructionCodec$32.decode(InstructionCodec.java:693)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        void dump(java.lang.String r1, java.io.FileDescriptor r2, java.io.PrintWriter r3, java.lang.String[] r4) {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: 0 in method: android.service.voice.VoiceInteractionSession.Request.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void, dex:  in method: android.service.voice.VoiceInteractionSession.Request.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.Request.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: d in method: android.service.voice.VoiceInteractionSession.Request.finishRequest():void, dex:  in method: android.service.voice.VoiceInteractionSession.Request.finishRequest():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: d in method: android.service.voice.VoiceInteractionSession.Request.finishRequest():void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: com.android.dex.DexException: bogus registerCount: d
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        void finishRequest() {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: d in method: android.service.voice.VoiceInteractionSession.Request.finishRequest():void, dex:  in method: android.service.voice.VoiceInteractionSession.Request.finishRequest():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.Request.finishRequest():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.Request.getCallingPackage():java.lang.String, dex: 
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
        public java.lang.String getCallingPackage() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.Request.getCallingPackage():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.Request.getCallingPackage():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.service.voice.VoiceInteractionSession.Request.getCallingUid():int, dex: 
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
        public int getCallingUid() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.service.voice.VoiceInteractionSession.Request.getCallingUid():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.Request.getCallingUid():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.Request.getExtras():android.os.Bundle, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.Request.getExtras():android.os.Bundle, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.Request.getExtras():android.os.Bundle");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.Request.isActive():boolean, dex: 
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
        public boolean isActive() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.Request.isActive():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.Request.isActive():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.Request.toString():java.lang.String, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.Request.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.Request.toString():java.lang.String");
        }
    }

    public static final class AbortVoiceRequest extends Request {
        final Prompt mPrompt;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.AbortVoiceRequest.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.app.VoiceInteractor$Prompt, android.os.Bundle):void, dex: 
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
        AbortVoiceRequest(java.lang.String r1, int r2, com.android.internal.app.IVoiceInteractorCallback r3, android.service.voice.VoiceInteractionSession r4, android.app.VoiceInteractor.Prompt r5, android.os.Bundle r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.AbortVoiceRequest.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.app.VoiceInteractor$Prompt, android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.AbortVoiceRequest.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.app.VoiceInteractor$Prompt, android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.AbortVoiceRequest.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void, dex: 
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
        void dump(java.lang.String r1, java.io.FileDescriptor r2, java.io.PrintWriter r3, java.lang.String[] r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.AbortVoiceRequest.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.AbortVoiceRequest.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.AbortVoiceRequest.getMessage():java.lang.CharSequence, dex: 
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
        public java.lang.CharSequence getMessage() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.AbortVoiceRequest.getMessage():java.lang.CharSequence, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.AbortVoiceRequest.getMessage():java.lang.CharSequence");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.AbortVoiceRequest.getVoicePrompt():android.app.VoiceInteractor$Prompt, dex: 
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
        public android.app.VoiceInteractor.Prompt getVoicePrompt() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.AbortVoiceRequest.getVoicePrompt():android.app.VoiceInteractor$Prompt, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.AbortVoiceRequest.getVoicePrompt():android.app.VoiceInteractor$Prompt");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.AbortVoiceRequest.sendAbortResult(android.os.Bundle):void, dex: 
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
        public void sendAbortResult(android.os.Bundle r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.AbortVoiceRequest.sendAbortResult(android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.AbortVoiceRequest.sendAbortResult(android.os.Bundle):void");
        }
    }

    public static final class CommandRequest extends Request {
        final String mCommand;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.CommandRequest.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, java.lang.String, android.os.Bundle):void, dex: 
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
        CommandRequest(java.lang.String r1, int r2, com.android.internal.app.IVoiceInteractorCallback r3, android.service.voice.VoiceInteractionSession r4, java.lang.String r5, android.os.Bundle r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.CommandRequest.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, java.lang.String, android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.CommandRequest.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, java.lang.String, android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.CommandRequest.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void, dex: 
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
        void dump(java.lang.String r1, java.io.FileDescriptor r2, java.io.PrintWriter r3, java.lang.String[] r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.CommandRequest.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.CommandRequest.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.CommandRequest.getCommand():java.lang.String, dex: 
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
        public java.lang.String getCommand() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.CommandRequest.getCommand():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.CommandRequest.getCommand():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.CommandRequest.sendCommandResult(boolean, android.os.Bundle):void, dex: 
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
        void sendCommandResult(boolean r1, android.os.Bundle r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.CommandRequest.sendCommandResult(boolean, android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.CommandRequest.sendCommandResult(boolean, android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.CommandRequest.sendIntermediateResult(android.os.Bundle):void, dex: 
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
        public void sendIntermediateResult(android.os.Bundle r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.CommandRequest.sendIntermediateResult(android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.CommandRequest.sendIntermediateResult(android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.CommandRequest.sendResult(android.os.Bundle):void, dex: 
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
        public void sendResult(android.os.Bundle r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.CommandRequest.sendResult(android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.CommandRequest.sendResult(android.os.Bundle):void");
        }
    }

    public static final class CompleteVoiceRequest extends Request {
        final Prompt mPrompt;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.CompleteVoiceRequest.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.app.VoiceInteractor$Prompt, android.os.Bundle):void, dex: 
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
        CompleteVoiceRequest(java.lang.String r1, int r2, com.android.internal.app.IVoiceInteractorCallback r3, android.service.voice.VoiceInteractionSession r4, android.app.VoiceInteractor.Prompt r5, android.os.Bundle r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.CompleteVoiceRequest.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.app.VoiceInteractor$Prompt, android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.CompleteVoiceRequest.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.app.VoiceInteractor$Prompt, android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.CompleteVoiceRequest.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void, dex: 
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
        void dump(java.lang.String r1, java.io.FileDescriptor r2, java.io.PrintWriter r3, java.lang.String[] r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.CompleteVoiceRequest.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.CompleteVoiceRequest.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.CompleteVoiceRequest.getMessage():java.lang.CharSequence, dex: 
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
        public java.lang.CharSequence getMessage() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.CompleteVoiceRequest.getMessage():java.lang.CharSequence, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.CompleteVoiceRequest.getMessage():java.lang.CharSequence");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.CompleteVoiceRequest.getVoicePrompt():android.app.VoiceInteractor$Prompt, dex: 
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
        public android.app.VoiceInteractor.Prompt getVoicePrompt() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.CompleteVoiceRequest.getVoicePrompt():android.app.VoiceInteractor$Prompt, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.CompleteVoiceRequest.getVoicePrompt():android.app.VoiceInteractor$Prompt");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.CompleteVoiceRequest.sendCompleteResult(android.os.Bundle):void, dex: 
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
        public void sendCompleteResult(android.os.Bundle r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.CompleteVoiceRequest.sendCompleteResult(android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.CompleteVoiceRequest.sendCompleteResult(android.os.Bundle):void");
        }
    }

    public static final class ConfirmationRequest extends Request {
        final Prompt mPrompt;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.ConfirmationRequest.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.app.VoiceInteractor$Prompt, android.os.Bundle):void, dex: 
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
        ConfirmationRequest(java.lang.String r1, int r2, com.android.internal.app.IVoiceInteractorCallback r3, android.service.voice.VoiceInteractionSession r4, android.app.VoiceInteractor.Prompt r5, android.os.Bundle r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.ConfirmationRequest.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.app.VoiceInteractor$Prompt, android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.ConfirmationRequest.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.app.VoiceInteractor$Prompt, android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.ConfirmationRequest.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void, dex: 
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
        void dump(java.lang.String r1, java.io.FileDescriptor r2, java.io.PrintWriter r3, java.lang.String[] r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.ConfirmationRequest.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.ConfirmationRequest.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.ConfirmationRequest.getPrompt():java.lang.CharSequence, dex: 
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
        public java.lang.CharSequence getPrompt() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.ConfirmationRequest.getPrompt():java.lang.CharSequence, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.ConfirmationRequest.getPrompt():java.lang.CharSequence");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.ConfirmationRequest.getVoicePrompt():android.app.VoiceInteractor$Prompt, dex: 
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
        public android.app.VoiceInteractor.Prompt getVoicePrompt() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.ConfirmationRequest.getVoicePrompt():android.app.VoiceInteractor$Prompt, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.ConfirmationRequest.getVoicePrompt():android.app.VoiceInteractor$Prompt");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.ConfirmationRequest.sendConfirmationResult(boolean, android.os.Bundle):void, dex: 
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
        public void sendConfirmationResult(boolean r1, android.os.Bundle r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.ConfirmationRequest.sendConfirmationResult(boolean, android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.ConfirmationRequest.sendConfirmationResult(boolean, android.os.Bundle):void");
        }
    }

    public static final class Insets {
        public static final int TOUCHABLE_INSETS_CONTENT = 1;
        public static final int TOUCHABLE_INSETS_FRAME = 0;
        public static final int TOUCHABLE_INSETS_REGION = 3;
        public final Rect contentInsets;
        public int touchableInsets;
        public final Region touchableRegion;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.Insets.<init>():void, dex: 
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
        public Insets() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.Insets.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.Insets.<init>():void");
        }
    }

    class MyCallbacks implements HandlerCaller.Callback, SoftInputWindow.Callback {
        final /* synthetic */ VoiceInteractionSession this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.MyCallbacks.<init>(android.service.voice.VoiceInteractionSession):void, dex: 
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
        MyCallbacks(android.service.voice.VoiceInteractionSession r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.MyCallbacks.<init>(android.service.voice.VoiceInteractionSession):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.MyCallbacks.<init>(android.service.voice.VoiceInteractionSession):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.service.voice.VoiceInteractionSession.MyCallbacks.executeMessage(android.os.Message):void, dex: 
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
        public void executeMessage(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.service.voice.VoiceInteractionSession.MyCallbacks.executeMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.MyCallbacks.executeMessage(android.os.Message):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.MyCallbacks.onBackPressed():void, dex: 
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
        public void onBackPressed() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.MyCallbacks.onBackPressed():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.MyCallbacks.onBackPressed():void");
        }
    }

    public static final class PickOptionRequest extends Request {
        final Option[] mOptions;
        final Prompt mPrompt;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.app.VoiceInteractor$Prompt, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void, dex: 
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
        PickOptionRequest(java.lang.String r1, int r2, com.android.internal.app.IVoiceInteractorCallback r3, android.service.voice.VoiceInteractionSession r4, android.app.VoiceInteractor.Prompt r5, android.app.VoiceInteractor.PickOptionRequest.Option[] r6, android.os.Bundle r7) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.app.VoiceInteractor$Prompt, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.PickOptionRequest.<init>(java.lang.String, int, com.android.internal.app.IVoiceInteractorCallback, android.service.voice.VoiceInteractionSession, android.app.VoiceInteractor$Prompt, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void, dex: 
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
        void dump(java.lang.String r1, java.io.FileDescriptor r2, java.io.PrintWriter r3, java.lang.String[] r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.PickOptionRequest.dump(java.lang.String, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.getOptions():android.app.VoiceInteractor$PickOptionRequest$Option[], dex: 
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
        public android.app.VoiceInteractor.PickOptionRequest.Option[] getOptions() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.getOptions():android.app.VoiceInteractor$PickOptionRequest$Option[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.PickOptionRequest.getOptions():android.app.VoiceInteractor$PickOptionRequest$Option[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.getPrompt():java.lang.CharSequence, dex: 
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
        public java.lang.CharSequence getPrompt() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.getPrompt():java.lang.CharSequence, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.PickOptionRequest.getPrompt():java.lang.CharSequence");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.getVoicePrompt():android.app.VoiceInteractor$Prompt, dex:  in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.getVoicePrompt():android.app.VoiceInteractor$Prompt, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.getVoicePrompt():android.app.VoiceInteractor$Prompt, dex: 
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
        public android.app.VoiceInteractor.Prompt getVoicePrompt() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.getVoicePrompt():android.app.VoiceInteractor$Prompt, dex:  in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.getVoicePrompt():android.app.VoiceInteractor$Prompt, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.PickOptionRequest.getVoicePrompt():android.app.VoiceInteractor$Prompt");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.sendIntermediatePickOptionResult(android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void, dex: 
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
        public void sendIntermediatePickOptionResult(android.app.VoiceInteractor.PickOptionRequest.Option[] r1, android.os.Bundle r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.sendIntermediatePickOptionResult(android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.PickOptionRequest.sendIntermediatePickOptionResult(android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.sendPickOptionResult(boolean, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void, dex: 
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
        void sendPickOptionResult(boolean r1, android.app.VoiceInteractor.PickOptionRequest.Option[] r2, android.os.Bundle r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.sendPickOptionResult(boolean, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.PickOptionRequest.sendPickOptionResult(boolean, android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.sendPickOptionResult(android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void, dex: 
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
        public void sendPickOptionResult(android.app.VoiceInteractor.PickOptionRequest.Option[] r1, android.os.Bundle r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.service.voice.VoiceInteractionSession.PickOptionRequest.sendPickOptionResult(android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.PickOptionRequest.sendPickOptionResult(android.app.VoiceInteractor$PickOptionRequest$Option[], android.os.Bundle):void");
        }
    }

    public VoiceInteractionSession(Context context) {
        this(context, new Handler());
    }

    public VoiceInteractionSession(Context context, Handler handler) {
        this.mDispatcherState = new DispatcherState();
        this.mTheme = 0;
        this.mActiveRequests = new ArrayMap();
        this.mTmpInsets = new Insets();
        this.mWeakRef = new WeakReference(this);
        this.mInteractor = new AnonymousClass1(this);
        this.mSession = new AnonymousClass2(this);
        this.mCallbacks = new MyCallbacks(this);
        this.mInsetsComputer = new AnonymousClass3(this);
        this.mContext = context;
        this.mHandlerCaller = new HandlerCaller(context, handler.getLooper(), this.mCallbacks, true);
    }

    public Context getContext() {
        return this.mContext;
    }

    void addRequest(Request req) {
        synchronized (this) {
            this.mActiveRequests.put(req.mInterface.asBinder(), req);
        }
    }

    boolean isRequestActive(IBinder reqInterface) {
        boolean containsKey;
        synchronized (this) {
            containsKey = this.mActiveRequests.containsKey(reqInterface);
        }
        return containsKey;
    }

    Request removeRequest(IBinder reqInterface) {
        Request request;
        synchronized (this) {
            request = (Request) this.mActiveRequests.remove(reqInterface);
        }
        return request;
    }

    void doCreate(IVoiceInteractionManagerService service, IBinder token) {
        this.mSystemService = service;
        this.mToken = token;
        onCreate();
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
    void doShow(android.os.Bundle r6, int r7, com.android.internal.app.IVoiceInteractionSessionShowCallback r8) {
        /*
        r5 = this;
        r4 = 0;
        r3 = 1;
        r1 = r5.mInShowWindow;
        if (r1 == 0) goto L_0x0010;
    L_0x0006:
        r1 = "VoiceInteractionSession";
        r2 = "Re-entrance in to showWindow";
        android.util.Log.w(r1, r2);
        return;
    L_0x0010:
        r1 = 1;
        r5.mInShowWindow = r1;	 Catch:{ all -> 0x0050 }
        r1 = r5.mWindowVisible;	 Catch:{ all -> 0x0050 }
        if (r1 != 0) goto L_0x0027;	 Catch:{ all -> 0x0050 }
    L_0x0017:
        r1 = r5.mWindowAdded;	 Catch:{ all -> 0x0050 }
        if (r1 != 0) goto L_0x0027;	 Catch:{ all -> 0x0050 }
    L_0x001b:
        r1 = 1;	 Catch:{ all -> 0x0050 }
        r5.mWindowAdded = r1;	 Catch:{ all -> 0x0050 }
        r0 = r5.onCreateContentView();	 Catch:{ all -> 0x0050 }
        if (r0 == 0) goto L_0x0027;	 Catch:{ all -> 0x0050 }
    L_0x0024:
        r5.setContentView(r0);	 Catch:{ all -> 0x0050 }
    L_0x0027:
        r5.onShow(r6, r7);	 Catch:{ all -> 0x0050 }
        r1 = r5.mWindowVisible;	 Catch:{ all -> 0x0050 }
        if (r1 != 0) goto L_0x0036;	 Catch:{ all -> 0x0050 }
    L_0x002e:
        r1 = 1;	 Catch:{ all -> 0x0050 }
        r5.mWindowVisible = r1;	 Catch:{ all -> 0x0050 }
        r1 = r5.mWindow;	 Catch:{ all -> 0x0050 }
        r1.show();	 Catch:{ all -> 0x0050 }
    L_0x0036:
        if (r8 == 0) goto L_0x004b;	 Catch:{ all -> 0x0050 }
    L_0x0038:
        r1 = r5.mRootView;	 Catch:{ all -> 0x0050 }
        r1.invalidate();	 Catch:{ all -> 0x0050 }
        r1 = r5.mRootView;	 Catch:{ all -> 0x0050 }
        r1 = r1.getViewTreeObserver();	 Catch:{ all -> 0x0050 }
        r2 = new android.service.voice.VoiceInteractionSession$4;	 Catch:{ all -> 0x0050 }
        r2.<init>(r5, r8);	 Catch:{ all -> 0x0050 }
        r1.addOnPreDrawListener(r2);	 Catch:{ all -> 0x0050 }
    L_0x004b:
        r5.mWindowWasVisible = r3;
        r5.mInShowWindow = r4;
        return;
    L_0x0050:
        r1 = move-exception;
        r5.mWindowWasVisible = r3;
        r5.mInShowWindow = r4;
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionSession.doShow(android.os.Bundle, int, com.android.internal.app.IVoiceInteractionSessionShowCallback):void");
    }

    void doHide() {
        if (this.mWindowVisible) {
            this.mWindow.hide();
            this.mWindowVisible = false;
            onHide();
        }
    }

    void doDestroy() {
        onDestroy();
        if (this.mInitialized) {
            this.mRootView.getViewTreeObserver().removeOnComputeInternalInsetsListener(this.mInsetsComputer);
            if (this.mWindowAdded) {
                this.mWindow.dismiss();
                this.mWindowAdded = false;
            }
            this.mInitialized = false;
        }
    }

    void initViews() {
        this.mInitialized = true;
        this.mThemeAttrs = this.mContext.obtainStyledAttributes(R.styleable.VoiceInteractionSession);
        this.mRootView = this.mInflater.inflate((int) com.android.internal.R.layout.voice_interaction_session, null);
        this.mRootView.setSystemUiVisibility(1792);
        this.mWindow.setContentView(this.mRootView);
        this.mRootView.getViewTreeObserver().addOnComputeInternalInsetsListener(this.mInsetsComputer);
        this.mContentFrame = (FrameLayout) this.mRootView.findViewById(16908290);
    }

    public void setDisabledShowContext(int flags) {
        try {
            this.mSystemService.setDisabledShowContext(flags);
        } catch (RemoteException e) {
        }
    }

    public int getDisabledShowContext() {
        try {
            return this.mSystemService.getDisabledShowContext();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public int getUserDisabledShowContext() {
        try {
            return this.mSystemService.getUserDisabledShowContext();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public void show(Bundle args, int flags) {
        if (this.mToken == null) {
            throw new IllegalStateException("Can't call before onCreate()");
        }
        try {
            this.mSystemService.showSessionFromSession(this.mToken, args, flags);
        } catch (RemoteException e) {
        }
    }

    public void hide() {
        if (this.mToken == null) {
            throw new IllegalStateException("Can't call before onCreate()");
        }
        try {
            this.mSystemService.hideSessionFromSession(this.mToken);
        } catch (RemoteException e) {
        }
    }

    public void setTheme(int theme) {
        if (this.mWindow != null) {
            throw new IllegalStateException("Must be called before onCreate()");
        }
        this.mTheme = theme;
    }

    public void startVoiceActivity(Intent intent) {
        if (this.mToken == null) {
            throw new IllegalStateException("Can't call before onCreate()");
        }
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(this.mContext);
            Instrumentation.checkStartActivityResult(this.mSystemService.startVoiceActivity(this.mToken, intent, intent.resolveType(this.mContext.getContentResolver())), intent);
        } catch (RemoteException e) {
        }
    }

    public void setKeepAwake(boolean keepAwake) {
        if (this.mToken == null) {
            throw new IllegalStateException("Can't call before onCreate()");
        }
        try {
            this.mSystemService.setKeepAwake(this.mToken, keepAwake);
        } catch (RemoteException e) {
        }
    }

    public void closeSystemDialogs() {
        if (this.mToken == null) {
            throw new IllegalStateException("Can't call before onCreate()");
        }
        try {
            this.mSystemService.closeSystemDialogs(this.mToken);
        } catch (RemoteException e) {
        }
    }

    public LayoutInflater getLayoutInflater() {
        return this.mInflater;
    }

    public Dialog getWindow() {
        return this.mWindow;
    }

    public void finish() {
        if (this.mToken == null) {
            throw new IllegalStateException("Can't call before onCreate()");
        }
        try {
            this.mSystemService.finish(this.mToken);
        } catch (RemoteException e) {
        }
    }

    public void onCreate() {
        doOnCreate();
    }

    private void doOnCreate() {
        int i;
        if (this.mTheme != 0) {
            i = this.mTheme;
        } else {
            i = com.android.internal.R.style.Theme_DeviceDefault_VoiceInteractionSession;
        }
        this.mTheme = i;
        this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        this.mWindow = new SoftInputWindow(this.mContext, TAG, this.mTheme, this.mCallbacks, this, this.mDispatcherState, 2031, 80, true);
        this.mWindow.getWindow().addFlags(com.android.internal.R.attr.transcriptMode);
        initViews();
        this.mWindow.getWindow().setLayout(-1, -1);
        this.mWindow.setToken(this.mToken);
    }

    public void onShow(Bundle args, int showFlags) {
    }

    public void onHide() {
    }

    public void onDestroy() {
    }

    public View onCreateContentView() {
        return null;
    }

    public void setContentView(View view) {
        this.mContentFrame.removeAllViews();
        this.mContentFrame.addView(view, new LayoutParams(-1, -1));
        this.mContentFrame.requestApplyInsets();
    }

    void doOnHandleAssist(Bundle data, AssistStructure structure, Throwable failure, AssistContent content) {
        if (failure != null) {
            onAssistStructureFailure(failure);
        }
        onHandleAssist(data, structure, content);
    }

    void doOnHandleAssistSecondary(Bundle data, AssistStructure structure, Throwable failure, AssistContent content, int index, int count) {
        if (failure != null) {
            onAssistStructureFailure(failure);
        }
        onHandleAssistSecondary(data, structure, content, index, count);
    }

    public void onAssistStructureFailure(Throwable failure) {
    }

    public void onHandleAssist(Bundle data, AssistStructure structure, AssistContent content) {
    }

    public void onHandleAssistSecondary(Bundle data, AssistStructure structure, AssistContent content, int index, int count) {
    }

    public void onHandleScreenshot(Bitmap screenshot) {
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        return false;
    }

    public void onBackPressed() {
        hide();
    }

    public void onCloseSystemDialogs() {
        hide();
    }

    public void onLockscreenShown() {
        hide();
    }

    public void onConfigurationChanged(Configuration newConfig) {
    }

    public void onLowMemory() {
    }

    public void onTrimMemory(int level) {
    }

    public void onComputeInsets(Insets outInsets) {
        outInsets.contentInsets.left = 0;
        outInsets.contentInsets.bottom = 0;
        outInsets.contentInsets.right = 0;
        View decor = getWindow().getWindow().getDecorView();
        outInsets.contentInsets.top = decor.getHeight();
        outInsets.touchableInsets = 0;
        outInsets.touchableRegion.setEmpty();
    }

    public void onTaskStarted(Intent intent, int taskId) {
    }

    public void onTaskFinished(Intent intent, int taskId) {
        hide();
    }

    public boolean[] onGetSupportedCommands(String[] commands) {
        return new boolean[commands.length];
    }

    public void onRequestConfirmation(ConfirmationRequest request) {
    }

    public void onRequestPickOption(PickOptionRequest request) {
    }

    public void onRequestCompleteVoice(CompleteVoiceRequest request) {
    }

    public void onRequestAbortVoice(AbortVoiceRequest request) {
    }

    public void onRequestCommand(CommandRequest request) {
    }

    public void onCancelRequest(Request request) {
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.print(prefix);
        writer.print("mToken=");
        writer.println(this.mToken);
        writer.print(prefix);
        writer.print("mTheme=#");
        writer.println(Integer.toHexString(this.mTheme));
        writer.print(prefix);
        writer.print("mInitialized=");
        writer.println(this.mInitialized);
        writer.print(prefix);
        writer.print("mWindowAdded=");
        writer.print(this.mWindowAdded);
        writer.print(" mWindowVisible=");
        writer.println(this.mWindowVisible);
        writer.print(prefix);
        writer.print("mWindowWasVisible=");
        writer.print(this.mWindowWasVisible);
        writer.print(" mInShowWindow=");
        writer.println(this.mInShowWindow);
        if (this.mActiveRequests.size() > 0) {
            writer.print(prefix);
            writer.println("Active requests:");
            String innerPrefix = prefix + "    ";
            for (int i = 0; i < this.mActiveRequests.size(); i++) {
                Request req = (Request) this.mActiveRequests.valueAt(i);
                writer.print(prefix);
                writer.print("  #");
                writer.print(i);
                writer.print(": ");
                writer.println(req);
                req.dump(innerPrefix, fd, writer, args);
            }
        }
    }
}
