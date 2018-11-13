package android.inputmethodservice;

import android.R;
import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager;
import android.app.ColorStatusBarManager;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.camera2.params.TonemapCurve;
import android.inputmethodservice.AbstractInputMethodService.AbstractInputMethodImpl;
import android.inputmethodservice.AbstractInputMethodService.AbstractInputMethodSessionImpl;
import android.inputmethodservice.KeyboardView.OnKeyboardCharListener;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.MovementMethod;
import android.util.Log;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.InternalInsetsInfo;
import android.view.ViewTreeObserver.OnComputeInternalInsetsListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.view.WindowManagerGlobal;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputContentInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.color.util.ColorNavigationBarUtil;
import com.color.util.ColorSecureKeyboardUtils;
import com.color.widget.ColorKeyBoardView;
import com.color.widget.ColorKeyBoardView.OnClickButtonListener;
import com.oppo.widget.OppoPasswordEntryKeyboardHelper;
import com.oppo.widget.OppoPasswordEntryKeyboardView;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
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
public class InputMethodService extends AbstractInputMethodService {
    public static final int BACK_DISPOSITION_DEFAULT = 0;
    public static final int BACK_DISPOSITION_WILL_DISMISS = 2;
    public static final int BACK_DISPOSITION_WILL_NOT_DISMISS = 1;
    static boolean DEBUG = false;
    static boolean DEBUG_SHOW_SOFTINPUT = false;
    private static final String HIDE_NAVIGATIONBAR_ENABLE = "hide_navigationbar_enable";
    public static final int IME_ACTIVE = 1;
    public static final int IME_VISIBLE = 2;
    static final int MOVEMENT_DOWN = -1;
    static final int MOVEMENT_UP = -2;
    private static final String SETTINGS_SECURITY_WINDOW = "security_window";
    static final String TAG = "InputMethodService";
    private static int mNavigationBarState;
    final OnClickListener mActionClickListener;
    int mBackDisposition;
    private OnClickButtonListener mButtonListener;
    FrameLayout mCandidatesFrame;
    boolean mCandidatesViewStarted;
    int mCandidatesVisibility;
    private ColorKeyBoardView mColorKeyboardView;
    private ColorStatusBarManager mColorStatusBarManager;
    CompletionInfo[] mCurCompletions;
    ViewGroup mExtractAccessories;
    View mExtractAction;
    ExtractEditText mExtractEditText;
    FrameLayout mExtractFrame;
    View mExtractView;
    boolean mExtractViewHidden;
    ExtractedText mExtractedText;
    int mExtractedToken;
    boolean mFullscreenApplied;
    ViewGroup mFullscreenArea;
    boolean mHardwareAccelerated;
    InputMethodManager mImm;
    boolean mInShowWindow;
    LayoutInflater mInflater;
    boolean mInitialized;
    InputBinding mInputBinding;
    InputConnection mInputConnection;
    EditorInfo mInputEditorInfo;
    FrameLayout mInputFrame;
    boolean mInputStarted;
    View mInputView;
    boolean mInputViewStarted;
    final OnComputeInternalInsetsListener mInsetsComputer;
    boolean mIsFullscreen;
    boolean mIsInputViewShown;
    private int mKeyBoardOrientation;
    private OppoPasswordEntryKeyboardView mKeyBoardView;
    private OnKeyboardCharListener mKeyboardChar;
    private OppoPasswordEntryKeyboardHelper mKeyboardHelper;
    boolean mLastShowInputRequested;
    View mRootView;
    private View mSecurityView;
    private SoftInputWindow mSecurityWindow;
    private FrameLayout mSecuritykeyboardArea;
    private SettingsObserver mSettingsObserver;
    boolean mShouldClearInsetOfPreviousIme;
    int mShowInputFlags;
    boolean mShowInputRequested;
    InputConnection mStartedInputConnection;
    int mStatusIcon;
    int mTheme;
    TypedArray mThemeAttrs;
    final Insets mTmpInsets;
    final int[] mTmpLocation;
    IBinder mToken;
    SoftInputWindow mWindow;
    boolean mWindowAdded;
    boolean mWindowCreated;
    boolean mWindowVisible;
    boolean mWindowWasVisible;

    /* renamed from: android.inputmethodservice.InputMethodService$3 */
    class AnonymousClass3 implements OnClickButtonListener {
        final /* synthetic */ InputMethodService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.inputmethodservice.InputMethodService.3.<init>(android.inputmethodservice.InputMethodService):void, dex: 
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
        AnonymousClass3(android.inputmethodservice.InputMethodService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.inputmethodservice.InputMethodService.3.<init>(android.inputmethodservice.InputMethodService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.inputmethodservice.InputMethodService.3.<init>(android.inputmethodservice.InputMethodService):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.inputmethodservice.InputMethodService.3.onClickButton():void, dex: 
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
        public void onClickButton() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.inputmethodservice.InputMethodService.3.onClickButton():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.inputmethodservice.InputMethodService.3.onClickButton():void");
        }
    }

    /* renamed from: android.inputmethodservice.InputMethodService$4 */
    class AnonymousClass4 implements OnKeyboardCharListener {
        final /* synthetic */ InputMethodService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.inputmethodservice.InputMethodService.4.<init>(android.inputmethodservice.InputMethodService):void, dex: 
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
        AnonymousClass4(android.inputmethodservice.InputMethodService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.inputmethodservice.InputMethodService.4.<init>(android.inputmethodservice.InputMethodService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.inputmethodservice.InputMethodService.4.<init>(android.inputmethodservice.InputMethodService):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.inputmethodservice.InputMethodService.4.onCharacter(java.lang.String, int):void, dex: 
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
        public void onCharacter(java.lang.String r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.inputmethodservice.InputMethodService.4.onCharacter(java.lang.String, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.inputmethodservice.InputMethodService.4.onCharacter(java.lang.String, int):void");
        }
    }

    public class InputMethodImpl extends AbstractInputMethodImpl {
        final /* synthetic */ InputMethodService this$0;

        public InputMethodImpl(InputMethodService this$0) {
            this.this$0 = this$0;
            super();
        }

        public void attachToken(IBinder token) {
            if (this.this$0.mToken == null) {
                this.this$0.mToken = token;
                this.this$0.mWindow.setToken(token);
                this.this$0.mSecurityWindow.setToken(token);
            }
        }

        public void bindInput(InputBinding binding) {
            this.this$0.mSecurityView = null;
            this.this$0.mSecuritykeyboardArea = null;
            this.this$0.mColorKeyboardView = null;
            this.this$0.mKeyBoardView = null;
            this.this$0.mKeyboardHelper = null;
            this.this$0.initSecurityView();
            this.this$0.updateSecurityView(this.this$0.mSecurityView);
            this.this$0.mInputBinding = binding;
            this.this$0.mInputConnection = binding.getConnection();
            if (InputMethodService.DEBUG) {
                Log.v(InputMethodService.TAG, "bindInput(): binding=" + binding + " ic=" + this.this$0.mInputConnection);
            }
            InputConnection ic = this.this$0.getCurrentInputConnection();
            if (ic != null) {
                ic.reportFullscreenMode(this.this$0.mIsFullscreen);
            }
            this.this$0.initialize();
            this.this$0.onBindInput();
        }

        public void unbindInput() {
            if (InputMethodService.DEBUG) {
                Log.v(InputMethodService.TAG, "unbindInput(): binding=" + this.this$0.mInputBinding + " ic=" + this.this$0.mInputConnection);
            }
            this.this$0.onUnbindInput();
            this.this$0.mInputBinding = null;
            this.this$0.mInputConnection = null;
        }

        public void startInput(InputConnection ic, EditorInfo attribute) {
            if (InputMethodService.DEBUG) {
                Log.v(InputMethodService.TAG, "startInput(): editor=" + attribute);
            }
            this.this$0.doStartInput(ic, attribute, false);
        }

        public void restartInput(InputConnection ic, EditorInfo attribute) {
            if (InputMethodService.DEBUG) {
                Log.v(InputMethodService.TAG, "restartInput(): editor=" + attribute);
            }
            this.this$0.doStartInput(ic, attribute, true);
        }

        public void hideSoftInput(int flags, ResultReceiver resultReceiver) {
            int i = 0;
            if (InputMethodService.DEBUG_SHOW_SOFTINPUT) {
                Log.v(InputMethodService.TAG, "hideSoftInput()");
            }
            boolean wasVis = this.this$0.isInputViewShown();
            this.this$0.mShowInputFlags = 0;
            this.this$0.mShowInputRequested = false;
            this.this$0.doHideWindow();
            this.this$0.clearInsetOfPreviousIme();
            if (resultReceiver != null) {
                if (wasVis != this.this$0.isInputViewShown()) {
                    i = 3;
                } else if (!wasVis) {
                    i = 1;
                }
                resultReceiver.send(i, null);
            }
        }

        public void showSoftInput(int flags, ResultReceiver resultReceiver) {
            int i;
            int i2 = 2;
            if (InputMethodService.DEBUG_SHOW_SOFTINPUT) {
                Log.v(InputMethodService.TAG, "showSoftInput()");
            }
            boolean wasVis = this.this$0.isInputViewShown();
            if (this.this$0.dispatchOnShowInputRequested(flags, false)) {
                try {
                    this.this$0.showWindow(true);
                } catch (BadTokenException e) {
                    if (InputMethodService.DEBUG_SHOW_SOFTINPUT) {
                        Log.v(InputMethodService.TAG, "BadTokenException: IME is done.");
                    }
                }
            } else if (InputMethodService.DEBUG_SHOW_SOFTINPUT) {
                Log.v(InputMethodService.TAG, "showSoftInput(), not show keyboard for onShowInputRequested is false!");
            }
            this.this$0.clearInsetOfPreviousIme();
            boolean showing = this.this$0.isInputViewShown();
            InputMethodManager inputMethodManager = this.this$0.mImm;
            IBinder iBinder = this.this$0.mToken;
            if (showing) {
                i = 2;
            } else {
                i = 0;
            }
            inputMethodManager.setImeWindowStatus(iBinder, i | 1, this.this$0.mBackDisposition);
            if (resultReceiver != null) {
                if (wasVis == this.this$0.isInputViewShown()) {
                    i2 = wasVis ? 0 : 1;
                }
                resultReceiver.send(i2, null);
            }
        }

        public void changeInputMethodSubtype(InputMethodSubtype subtype) {
            this.this$0.onCurrentInputMethodSubtypeChanged(subtype);
        }
    }

    public class InputMethodSessionImpl extends AbstractInputMethodSessionImpl {
        final /* synthetic */ InputMethodService this$0;

        public InputMethodSessionImpl(InputMethodService this$0) {
            this.this$0 = this$0;
            super();
        }

        public void finishInput() {
            if (isEnabled()) {
                if (InputMethodService.DEBUG) {
                    Log.v(InputMethodService.TAG, "finishInput() in " + this);
                }
                this.this$0.doFinishInput();
            }
        }

        public void displayCompletions(CompletionInfo[] completions) {
            if (isEnabled()) {
                this.this$0.mCurCompletions = completions;
                this.this$0.onDisplayCompletions(completions);
            }
        }

        public void updateExtractedText(int token, ExtractedText text) {
            if (isEnabled()) {
                this.this$0.onUpdateExtractedText(token, text);
            }
        }

        public void updateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
            if (isEnabled()) {
                this.this$0.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
            }
        }

        public void viewClicked(boolean focusChanged) {
            if (isEnabled()) {
                this.this$0.onViewClicked(focusChanged);
            }
        }

        public void updateCursor(Rect newCursor) {
            if (isEnabled()) {
                this.this$0.onUpdateCursor(newCursor);
            }
        }

        public void appPrivateCommand(String action, Bundle data) {
            if (isEnabled()) {
                this.this$0.onAppPrivateCommand(action, data);
            }
        }

        public void toggleSoftInput(int showFlags, int hideFlags) {
            this.this$0.onToggleSoftInput(showFlags, hideFlags);
        }

        public void updateCursorAnchorInfo(CursorAnchorInfo info) {
            if (isEnabled()) {
                this.this$0.onUpdateCursorAnchorInfo(info);
            }
        }
    }

    public static final class Insets {
        public static final int TOUCHABLE_INSETS_CONTENT = 1;
        public static final int TOUCHABLE_INSETS_FRAME = 0;
        public static final int TOUCHABLE_INSETS_REGION = 3;
        public static final int TOUCHABLE_INSETS_VISIBLE = 2;
        public int contentTopInsets;
        public int touchableInsets;
        public final Region touchableRegion;
        public int visibleTopInsets;

        public Insets() {
            this.touchableRegion = new Region();
        }
    }

    private static final class SettingsObserver extends ContentObserver {
        private final InputMethodService mService;
        private int mShowImeWithHardKeyboard;

        private SettingsObserver(InputMethodService service) {
            super(new Handler(service.getMainLooper()));
            this.mShowImeWithHardKeyboard = 0;
            this.mService = service;
        }

        public static SettingsObserver createAndRegister(InputMethodService service) {
            SettingsObserver observer = new SettingsObserver(service);
            service.getContentResolver().registerContentObserver(Secure.getUriFor(Secure.SHOW_IME_WITH_HARD_KEYBOARD), false, observer);
            service.getContentResolver().registerContentObserver(Secure.getUriFor(InputMethodService.HIDE_NAVIGATIONBAR_ENABLE), false, observer);
            return observer;
        }

        void unregister() {
            this.mService.getContentResolver().unregisterContentObserver(this);
        }

        private boolean shouldShowImeWithHardKeyboard() {
            if (this.mShowImeWithHardKeyboard == 0) {
                this.mShowImeWithHardKeyboard = Secure.getInt(this.mService.getContentResolver(), Secure.SHOW_IME_WITH_HARD_KEYBOARD, 0) != 0 ? 2 : 1;
            }
            switch (this.mShowImeWithHardKeyboard) {
                case 1:
                    return false;
                case 2:
                    return true;
                default:
                    Log.e(InputMethodService.TAG, "Unexpected mShowImeWithHardKeyboard=" + this.mShowImeWithHardKeyboard);
                    return false;
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            boolean z = true;
            if (Secure.getUriFor(Secure.SHOW_IME_WITH_HARD_KEYBOARD).equals(uri)) {
                int i;
                if (Secure.getInt(this.mService.getContentResolver(), Secure.SHOW_IME_WITH_HARD_KEYBOARD, 0) != 0) {
                    i = 2;
                } else {
                    i = 1;
                }
                this.mShowImeWithHardKeyboard = i;
                this.mService.resetStateForNewConfiguration();
            }
            if (Secure.getUriFor(InputMethodService.HIDE_NAVIGATIONBAR_ENABLE).equals(uri)) {
                InputMethodService.mNavigationBarState = Secure.getInt(this.mService.getContentResolver(), InputMethodService.HIDE_NAVIGATIONBAR_ENABLE, 0);
                ColorNavigationBarUtil instance = ColorNavigationBarUtil.getInstance();
                if (InputMethodService.mNavigationBarState != 2) {
                    z = false;
                }
                instance.setImePackageInGestureMode(z);
            }
        }

        public String toString() {
            return "SettingsObserver{mShowImeWithHardKeyboard=" + this.mShowImeWithHardKeyboard + "}";
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.inputmethodservice.InputMethodService.<clinit>():void, dex: 
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
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.inputmethodservice.InputMethodService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.inputmethodservice.InputMethodService.<clinit>():void");
    }

    public InputMethodService() {
        this.mTheme = 0;
        this.mHardwareAccelerated = false;
        this.mSecurityWindow = null;
        this.mSecurityView = null;
        this.mSecuritykeyboardArea = null;
        this.mKeyBoardView = null;
        this.mColorKeyboardView = null;
        this.mKeyboardHelper = null;
        this.mTmpInsets = new Insets();
        this.mTmpLocation = new int[2];
        this.mInsetsComputer = new OnComputeInternalInsetsListener() {
            public void onComputeInternalInsets(InternalInsetsInfo info) {
                if (InputMethodService.this.isExtractViewShown()) {
                    View decor = InputMethodService.this.getWindow().getWindow().getDecorView();
                    Rect rect = info.contentInsets;
                    int height = decor.getHeight();
                    info.visibleInsets.top = height;
                    rect.top = height;
                    info.touchableRegion.setEmpty();
                    info.setTouchableInsets(0);
                    return;
                }
                InputMethodService.this.onComputeInsets(InputMethodService.this.mTmpInsets);
                info.contentInsets.top = InputMethodService.this.mTmpInsets.contentTopInsets;
                info.visibleInsets.top = InputMethodService.this.mTmpInsets.visibleTopInsets;
                info.touchableRegion.set(InputMethodService.this.mTmpInsets.touchableRegion);
                info.setTouchableInsets(InputMethodService.this.mTmpInsets.touchableInsets);
            }
        };
        this.mActionClickListener = new OnClickListener() {
            public void onClick(View v) {
                EditorInfo ei = InputMethodService.this.getCurrentInputEditorInfo();
                InputConnection ic = InputMethodService.this.getCurrentInputConnection();
                if (ei != null && ic != null) {
                    if (ei.actionId != 0) {
                        ic.performEditorAction(ei.actionId);
                    } else if ((ei.imeOptions & 255) != 1) {
                        ic.performEditorAction(ei.imeOptions & 255);
                    }
                }
            }
        };
        this.mButtonListener = new AnonymousClass3(this);
        this.mKeyboardChar = new AnonymousClass4(this);
        this.mColorStatusBarManager = null;
    }

    public void setTheme(int theme) {
        if (this.mWindow != null) {
            throw new IllegalStateException("Must be called before onCreate()");
        }
        this.mTheme = theme;
    }

    public boolean enableHardwareAcceleration() {
        if (this.mWindow != null) {
            throw new IllegalStateException("Must be called before onCreate()");
        } else if (!ActivityManager.isHighEndGfx()) {
            return false;
        } else {
            this.mHardwareAccelerated = true;
            return true;
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for the security keyboard", property = OppoRomType.ROM)
    public void onCreate() {
        boolean z = true;
        this.mTheme = Resources.selectSystemTheme(this.mTheme, getApplicationInfo().targetSdkVersion, R.style.Theme_InputMethod, R.style.Theme_Holo_InputMethod, R.style.Theme_DeviceDefault_InputMethod, R.style.Theme_DeviceDefault_InputMethod);
        super.setTheme(this.mTheme);
        super.onCreate();
        this.mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        this.mSettingsObserver = SettingsObserver.createAndRegister(this);
        this.mShouldClearInsetOfPreviousIme = this.mImm.getInputMethodWindowVisibleHeight() > 0;
        this.mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mWindow = new SoftInputWindow(this, "InputMethod", this.mTheme, null, null, this.mDispatcherState, 2011, 80, false);
        if (this.mHardwareAccelerated) {
            this.mWindow.getWindow().addFlags(16777216);
        }
        initViews();
        this.mWindow.getWindow().setLayout(-1, -2);
        this.mSecurityWindow = new SoftInputWindow(this, "SecurityInputMethod", this.mTheme, null, null, this.mDispatcherState, 2011, 80, false);
        this.mSecurityWindow.getWindow().addFlags(8192);
        if (this.mHardwareAccelerated) {
            this.mSecurityWindow.getWindow().addFlags(16777216);
        }
        initSecurityView();
        updateSecurityView(this.mSecurityView);
        this.mSecurityWindow.getWindow().setLayout(-1, -2);
        ColorSecureKeyboardUtils.getInstance().initData(this);
        this.mColorStatusBarManager = new ColorStatusBarManager();
        mNavigationBarState = Secure.getInt(getContentResolver(), HIDE_NAVIGATIONBAR_ENABLE, 0);
        ColorNavigationBarUtil instance = ColorNavigationBarUtil.getInstance();
        if (mNavigationBarState != 2) {
            z = false;
        }
        instance.setImePackageInGestureMode(z);
    }

    public void onInitializeInterface() {
    }

    void initialize() {
        if (!this.mInitialized) {
            this.mInitialized = true;
            onInitializeInterface();
        }
    }

    void initViews() {
        this.mInitialized = false;
        this.mWindowCreated = false;
        this.mShowInputRequested = false;
        this.mShowInputFlags = 0;
        this.mThemeAttrs = obtainStyledAttributes(R.styleable.InputMethodService);
        this.mRootView = this.mInflater.inflate(17367149, null);
        this.mRootView.setSystemUiVisibility(768);
        this.mWindow.setContentView(this.mRootView);
        this.mRootView.getViewTreeObserver().removeOnComputeInternalInsetsListener(this.mInsetsComputer);
        this.mRootView.getViewTreeObserver().addOnComputeInternalInsetsListener(this.mInsetsComputer);
        if (Global.getInt(getContentResolver(), Global.FANCY_IME_ANIMATIONS, 0) != 0) {
            this.mWindow.getWindow().setWindowAnimations(16974575);
        }
        this.mFullscreenArea = (ViewGroup) this.mRootView.findViewById(16909191);
        this.mExtractViewHidden = false;
        this.mExtractFrame = (FrameLayout) this.mRootView.findViewById(R.id.extractArea);
        this.mExtractView = null;
        this.mExtractEditText = null;
        this.mExtractAccessories = null;
        this.mExtractAction = null;
        this.mFullscreenApplied = false;
        this.mCandidatesFrame = (FrameLayout) this.mRootView.findViewById(R.id.candidatesArea);
        this.mInputFrame = (FrameLayout) this.mRootView.findViewById(R.id.inputArea);
        this.mInputView = null;
        this.mIsInputViewShown = false;
        this.mExtractFrame.setVisibility(8);
        this.mCandidatesVisibility = getCandidatesHiddenVisibility();
        this.mCandidatesFrame.setVisibility(this.mCandidatesVisibility);
        this.mInputFrame.setVisibility(8);
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for the security keyboard", property = OppoRomType.ROM)
    public void onDestroy() {
        super.onDestroy();
        this.mRootView.getViewTreeObserver().removeOnComputeInternalInsetsListener(this.mInsetsComputer);
        doFinishInput();
        if (this.mWindowAdded) {
            this.mWindow.getWindow().setWindowAnimations(0);
            this.mWindow.dismiss();
            this.mSecurityWindow.getWindow().setWindowAnimations(0);
            this.mSecurityWindow.dismiss();
        }
        if (this.mSettingsObserver != null) {
            this.mSettingsObserver.unregister();
            this.mSettingsObserver = null;
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetStateForNewConfiguration();
    }

    private void resetStateForNewConfiguration() {
        int i = 0;
        boolean visible = this.mWindowVisible;
        int showFlags = this.mShowInputFlags;
        boolean showingInput = this.mShowInputRequested;
        CompletionInfo[] completions = this.mCurCompletions;
        if (this.mExtractEditText != null) {
            this.mExtractEditText.onWindowFocusChanged(false);
            this.mExtractEditText.setVisibility(8);
        }
        initViews();
        this.mInputViewStarted = false;
        this.mCandidatesViewStarted = false;
        this.mSecurityView = null;
        this.mSecuritykeyboardArea = null;
        this.mColorKeyboardView = null;
        this.mKeyBoardView = null;
        this.mKeyboardHelper = null;
        initSecurityView();
        updateSecurityView(this.mSecurityView);
        if (this.mInputStarted) {
            doStartInput(getCurrentInputConnection(), getCurrentInputEditorInfo(), true);
        }
        if (visible) {
            if (showingInput) {
                if (dispatchOnShowInputRequested(showFlags, true)) {
                    showWindow(true);
                    if (completions != null) {
                        this.mCurCompletions = completions;
                        onDisplayCompletions(completions);
                    }
                } else {
                    doHideWindow();
                }
            } else if (this.mCandidatesVisibility == 0) {
                showWindow(false);
            } else {
                doHideWindow();
            }
            boolean showing = onEvaluateInputViewShown();
            InputMethodManager inputMethodManager = this.mImm;
            IBinder iBinder = this.mToken;
            if (showing) {
                i = 2;
            }
            inputMethodManager.setImeWindowStatus(iBinder, i | 1, this.mBackDisposition);
        }
    }

    public AbstractInputMethodImpl onCreateInputMethodInterface() {
        return new InputMethodImpl(this);
    }

    public AbstractInputMethodSessionImpl onCreateInputMethodSessionInterface() {
        return new InputMethodSessionImpl(this);
    }

    public LayoutInflater getLayoutInflater() {
        return this.mInflater;
    }

    public Dialog getWindow() {
        return this.mWindow;
    }

    public void setBackDisposition(int disposition) {
        this.mBackDisposition = disposition;
    }

    public int getBackDisposition() {
        return this.mBackDisposition;
    }

    public int getMaxWidth() {
        return ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    }

    public InputBinding getCurrentInputBinding() {
        return this.mInputBinding;
    }

    public InputConnection getCurrentInputConnection() {
        InputConnection ic = this.mStartedInputConnection;
        if (ic != null) {
            return ic;
        }
        return this.mInputConnection;
    }

    public boolean getCurrentInputStarted() {
        return this.mInputStarted;
    }

    public EditorInfo getCurrentInputEditorInfo() {
        return this.mInputEditorInfo;
    }

    public void updateFullscreenMode() {
        boolean isFullscreen = this.mShowInputRequested ? onEvaluateFullscreenMode() : false;
        boolean changed = this.mLastShowInputRequested != this.mShowInputRequested;
        if (!(this.mIsFullscreen == isFullscreen && this.mFullscreenApplied)) {
            changed = true;
            this.mIsFullscreen = isFullscreen;
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.reportFullscreenMode(isFullscreen);
            }
            this.mFullscreenApplied = true;
            initialize();
            LayoutParams lp = (LayoutParams) this.mFullscreenArea.getLayoutParams();
            if (isFullscreen) {
                this.mFullscreenArea.setBackgroundDrawable(this.mThemeAttrs.getDrawable(0));
                lp.height = 0;
                lp.weight = 1.0f;
            } else {
                this.mFullscreenArea.setBackgroundDrawable(null);
                lp.height = -2;
                lp.weight = TonemapCurve.LEVEL_BLACK;
            }
            ((ViewGroup) this.mFullscreenArea.getParent()).updateViewLayout(this.mFullscreenArea, lp);
            if (isFullscreen) {
                if (this.mExtractView == null) {
                    View v = onCreateExtractTextView();
                    if (v != null) {
                        setExtractView(v);
                    }
                }
                startExtractingText(false);
            }
            updateExtractFrameVisibility();
        }
        if (changed) {
            boolean z;
            Window window = this.mWindow.getWindow();
            if (this.mShowInputRequested) {
                z = false;
            } else {
                z = true;
            }
            onConfigureWindow(window, isFullscreen, z);
            this.mLastShowInputRequested = this.mShowInputRequested;
        }
    }

    public void onConfigureWindow(Window win, boolean isFullscreen, boolean isCandidatesOnly) {
        int currentHeight = this.mWindow.getWindow().getAttributes().height;
        int newHeight = isFullscreen ? -1 : -2;
        if (this.mIsInputViewShown && currentHeight != newHeight) {
            Log.w(TAG, "Window size has been changed. This may cause jankiness of resizing window: " + currentHeight + " -> " + newHeight);
        }
        this.mWindow.getWindow().setLayout(-1, newHeight);
    }

    public boolean isFullscreenMode() {
        return this.mIsFullscreen;
    }

    public boolean onEvaluateFullscreenMode() {
        if (getResources().getConfiguration().orientation != 2) {
            return false;
        }
        if ((this.mInputEditorInfo == null || (this.mInputEditorInfo.imeOptions & 33554432) == 0) && getDockSide() == -1) {
            return true;
        }
        return false;
    }

    private int getDockSide() {
        try {
            return WindowManagerGlobal.getWindowManagerService().getDockedStackSide();
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to get dock side: " + e);
            return -1;
        }
    }

    public void setExtractViewShown(boolean shown) {
        if (this.mExtractViewHidden == shown) {
            this.mExtractViewHidden = !shown;
            updateExtractFrameVisibility();
        }
    }

    public boolean isExtractViewShown() {
        return this.mIsFullscreen && !this.mExtractViewHidden;
    }

    void updateExtractFrameVisibility() {
        int vis;
        boolean z;
        int i = 1;
        if (isFullscreenMode()) {
            vis = this.mExtractViewHidden ? 4 : 0;
            this.mExtractFrame.setVisibility(vis);
        } else {
            vis = 0;
            this.mExtractFrame.setVisibility(8);
        }
        if (this.mCandidatesVisibility == 0) {
            z = true;
        } else {
            z = false;
        }
        updateCandidatesVisibility(z);
        if (this.mWindowWasVisible && this.mFullscreenArea.getVisibility() != vis) {
            TypedArray typedArray = this.mThemeAttrs;
            if (vis != 0) {
                i = 2;
            }
            int animRes = typedArray.getResourceId(i, 0);
            if (animRes != 0) {
                this.mFullscreenArea.startAnimation(AnimationUtils.loadAnimation(this, animRes));
            }
        }
        this.mFullscreenArea.setVisibility(vis);
    }

    public void onComputeInsets(Insets outInsets) {
        int[] loc = this.mTmpLocation;
        if (this.mInputFrame.getVisibility() == 0) {
            this.mInputFrame.getLocationInWindow(loc);
        } else {
            loc[1] = getWindow().getWindow().getDecorView().getHeight();
        }
        if (isFullscreenMode()) {
            outInsets.contentTopInsets = getWindow().getWindow().getDecorView().getHeight();
        } else {
            outInsets.contentTopInsets = loc[1];
        }
        if (this.mCandidatesFrame.getVisibility() == 0) {
            this.mCandidatesFrame.getLocationInWindow(loc);
        }
        outInsets.visibleTopInsets = loc[1];
        outInsets.touchableInsets = 2;
        outInsets.touchableRegion.setEmpty();
    }

    public void updateInputViewShown() {
        boolean isShown = this.mShowInputRequested ? onEvaluateInputViewShown() : false;
        if (this.mIsInputViewShown != isShown && this.mWindowVisible) {
            this.mIsInputViewShown = isShown;
            this.mInputFrame.setVisibility(isShown ? 0 : 8);
            if (this.mInputView == null) {
                initialize();
                View v = onCreateInputView();
                if (v != null) {
                    setInputView(v);
                }
            }
        }
    }

    public boolean isShowInputRequested() {
        return this.mShowInputRequested;
    }

    public boolean isInputViewShown() {
        return this.mIsInputViewShown ? this.mWindowVisible : false;
    }

    public boolean onEvaluateInputViewShown() {
        boolean z = true;
        if (this.mSettingsObserver == null) {
            Log.w(TAG, "onEvaluateInputViewShown: mSettingsObserver must not be null here.");
            return false;
        } else if (this.mSettingsObserver.shouldShowImeWithHardKeyboard()) {
            return true;
        } else {
            Configuration config = getResources().getConfiguration();
            config.keyboard = 1;
            if (!(config.keyboard == 1 || config.hardKeyboardHidden == 2)) {
                z = false;
            }
            return z;
        }
    }

    public void setCandidatesViewShown(boolean shown) {
        updateCandidatesVisibility(shown);
        if (!this.mShowInputRequested && this.mWindowVisible != shown) {
            if (shown) {
                showWindow(false);
            } else {
                doHideWindow();
            }
        }
    }

    void updateCandidatesVisibility(boolean shown) {
        int vis = shown ? 0 : getCandidatesHiddenVisibility();
        if (this.mCandidatesVisibility != vis) {
            this.mCandidatesFrame.setVisibility(vis);
            this.mCandidatesVisibility = vis;
        }
    }

    public int getCandidatesHiddenVisibility() {
        return isExtractViewShown() ? 8 : 4;
    }

    public void showStatusIcon(int iconResId) {
        this.mStatusIcon = iconResId;
        this.mImm.showStatusIcon(this.mToken, getPackageName(), iconResId);
    }

    public void hideStatusIcon() {
        this.mStatusIcon = 0;
        this.mImm.hideStatusIcon(this.mToken);
    }

    public void switchInputMethod(String id) {
        this.mImm.setInputMethod(this.mToken, id);
    }

    public void setExtractView(View view) {
        this.mExtractFrame.removeAllViews();
        this.mExtractFrame.addView(view, new FrameLayout.LayoutParams(-1, -1));
        this.mExtractView = view;
        if (view != null) {
            this.mExtractEditText = (ExtractEditText) view.findViewById(R.id.inputExtractEditText);
            this.mExtractEditText.setIME(this);
            this.mExtractAction = view.findViewById(16909193);
            if (this.mExtractAction != null) {
                this.mExtractAccessories = (ViewGroup) view.findViewById(16909192);
            }
            startExtractingText(false);
            return;
        }
        this.mExtractEditText = null;
        this.mExtractAccessories = null;
        this.mExtractAction = null;
    }

    public void setCandidatesView(View view) {
        this.mCandidatesFrame.removeAllViews();
        this.mCandidatesFrame.addView(view, new FrameLayout.LayoutParams(-1, -2));
    }

    public void setInputView(View view) {
        this.mInputFrame.removeAllViews();
        this.mInputFrame.addView(view, new FrameLayout.LayoutParams(-1, -2));
        this.mInputView = view;
    }

    public View onCreateExtractTextView() {
        return this.mInflater.inflate(17367150, null);
    }

    public View onCreateCandidatesView() {
        return null;
    }

    public View onCreateInputView() {
        return null;
    }

    public void onStartInputView(EditorInfo info, boolean restarting) {
    }

    public void onFinishInputView(boolean finishingInput) {
        if (!finishingInput) {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }

    public void onStartCandidatesView(EditorInfo info, boolean restarting) {
    }

    public void onFinishCandidatesView(boolean finishingInput) {
        if (!finishingInput) {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }

    public boolean onShowInputRequested(int flags, boolean configChange) {
        if (!onEvaluateInputViewShown()) {
            return false;
        }
        if ((flags & 1) == 0) {
            if (configChange || !onEvaluateFullscreenMode()) {
                return this.mSettingsObserver.shouldShowImeWithHardKeyboard() || getResources().getConfiguration().keyboard == 1;
            } else {
                return false;
            }
        }
    }

    private boolean dispatchOnShowInputRequested(int flags, boolean configChange) {
        boolean result = onShowInputRequested(flags, configChange);
        if (result) {
            this.mShowInputFlags = flags;
        } else {
            this.mShowInputFlags = 0;
        }
        return result;
    }

    public void showWindow(boolean showInput) {
        if (DEBUG) {
            Log.v(TAG, "Showing window: showInput=" + showInput + " mShowInputRequested=" + this.mShowInputRequested + " mWindowAdded=" + this.mWindowAdded + " mWindowCreated=" + this.mWindowCreated + " mWindowVisible=" + this.mWindowVisible + " mInputStarted=" + this.mInputStarted + " mShowInputFlags=" + this.mShowInputFlags);
        }
        if (this.mInShowWindow) {
            Log.w(TAG, "Re-entrance in to showWindow");
            return;
        }
        boolean enable = Secure.getInt(getContentResolver(), SETTINGS_SECURITY_WINDOW, 1) == 1;
        if (this.mSecurityWindow == null || !enable || !needToShowSecurityWindow()) {
            try {
                this.mWindowWasVisible = this.mWindowVisible;
                this.mInShowWindow = true;
                showWindowInner(showInput);
                this.mWindowWasVisible = true;
                this.mInShowWindow = false;
            } catch (BadTokenException e) {
                if (DEBUG) {
                    Log.v(TAG, "BadTokenException: IME is done.");
                }
                afterWindowHidden();
                this.mWindowVisible = false;
                this.mWindowAdded = false;
                throw e;
            } catch (Throwable th) {
                this.mWindowWasVisible = true;
                this.mInShowWindow = false;
            }
        } else if (!isSecurityWindowVisible()) {
            if (this.mWindowWasVisible) {
                hideWindow();
            }
            beforeWindowShown();
            if (this.mKeyBoardOrientation != getResources().getConfiguration().orientation) {
                this.mSecurityView = null;
                this.mSecuritykeyboardArea = null;
                this.mColorKeyboardView = null;
                this.mKeyBoardView = null;
                this.mKeyboardHelper = null;
                initSecurityView();
                updateSecurityView(this.mSecurityView);
            }
            OppoPasswordEntryKeyboardHelper helper = this.mColorKeyboardView.getKeyboardHelper();
            if (helper != null) {
                helper.setKeyboardMode(0);
            }
            try {
                this.mSecurityWindow.show();
            } catch (BadTokenException e2) {
            }
            this.mShouldClearInsetOfPreviousIme = false;
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for the security keyboard", property = OppoRomType.ROM)
    void showWindowInner(boolean showInput) {
        int i;
        int i2;
        int i3 = 2;
        hideSecurityWindow();
        this.mRootView.getViewTreeObserver().addOnComputeInternalInsetsListener(this.mInsetsComputer);
        boolean doShowInput = false;
        if (this.mWindowVisible) {
            i = 1;
        } else {
            i = 0;
        }
        if (isInputViewShown()) {
            i2 = 2;
        } else {
            i2 = 0;
        }
        int previousImeWindowStatus = i | i2;
        beforeWindowShown();
        this.mWindowVisible = true;
        if (!this.mShowInputRequested && this.mInputStarted && showInput) {
            doShowInput = true;
            this.mShowInputRequested = true;
        }
        if (DEBUG) {
            Log.v(TAG, "showWindow: updating UI");
        }
        initialize();
        updateFullscreenMode();
        updateInputViewShown();
        if (!(this.mWindowAdded && this.mWindowCreated)) {
            this.mWindowAdded = true;
            this.mWindowCreated = true;
            initialize();
            if (DEBUG) {
                Log.v(TAG, "CALL: onCreateCandidatesView");
            }
            View v = onCreateCandidatesView();
            if (DEBUG) {
                Log.v(TAG, "showWindow: candidates=" + v);
            }
            if (v != null) {
                setCandidatesView(v);
            }
        }
        if (doShowInput) {
            startExtractingText(false);
        }
        if (!isInputViewShown()) {
            i3 = 0;
        }
        int nextImeWindowStatus = i3 | 1;
        if (previousImeWindowStatus != nextImeWindowStatus) {
            this.mImm.setImeWindowStatus(this.mToken, nextImeWindowStatus, this.mBackDisposition);
        }
        if ((previousImeWindowStatus & 1) == 0) {
            if (DEBUG) {
                Log.v(TAG, "showWindow: showing!");
            }
            onWindowShown();
            this.mWindow.show();
            this.mShouldClearInsetOfPreviousIme = false;
        }
        if (this.mShowInputRequested) {
            if (!this.mInputViewStarted) {
                if (DEBUG) {
                    Log.v(TAG, "CALL: onStartInputView");
                }
                this.mInputViewStarted = true;
                onStartInputView(this.mInputEditorInfo, false);
            }
        } else if (!this.mCandidatesViewStarted) {
            if (DEBUG) {
                Log.v(TAG, "CALL: onStartCandidatesView");
            }
            this.mCandidatesViewStarted = true;
            onStartCandidatesView(this.mInputEditorInfo, false);
        }
    }

    private void finishViews() {
        if (this.mInputViewStarted) {
            if (DEBUG) {
                Log.v(TAG, "CALL: onFinishInputView");
            }
            onFinishInputView(false);
        } else if (this.mCandidatesViewStarted) {
            if (DEBUG) {
                Log.v(TAG, "CALL: onFinishCandidatesView");
            }
            onFinishCandidatesView(false);
        }
        this.mInputViewStarted = false;
        this.mCandidatesViewStarted = false;
    }

    private void doHideWindow() {
        this.mImm.setImeWindowStatus(this.mToken, 0, this.mBackDisposition);
        if (this.mSecurityWindow != null && isSecurityWindowVisible()) {
            this.mSecurityWindow.hide();
            afterWindowHidden();
        }
        hideWindow();
    }

    public void hideWindow() {
        finishViews();
        if (this.mWindowVisible) {
            this.mWindow.hide();
            this.mRootView.getViewTreeObserver().removeOnComputeInternalInsetsListener(this.mInsetsComputer);
            afterWindowHidden();
            this.mWindowVisible = false;
            onWindowHidden();
            this.mWindowWasVisible = false;
        }
        updateFullscreenMode();
    }

    public void onWindowShown() {
    }

    public void onWindowHidden() {
    }

    private void clearInsetOfPreviousIme() {
        if (DEBUG) {
            Log.v(TAG, "clearInsetOfPreviousIme()  mShouldClearInsetOfPreviousIme=" + this.mShouldClearInsetOfPreviousIme);
        }
        if (this.mShouldClearInsetOfPreviousIme) {
            this.mImm.clearLastInputMethodWindowForTransition(this.mToken);
            this.mShouldClearInsetOfPreviousIme = false;
        }
    }

    public void onBindInput() {
    }

    public void onUnbindInput() {
    }

    public void onStartInput(EditorInfo attribute, boolean restarting) {
    }

    void doFinishInput() {
        if (this.mInputViewStarted) {
            if (DEBUG) {
                Log.v(TAG, "CALL: onFinishInputView");
            }
            onFinishInputView(true);
        } else if (this.mCandidatesViewStarted) {
            if (DEBUG) {
                Log.v(TAG, "CALL: onFinishCandidatesView");
            }
            onFinishCandidatesView(true);
        }
        this.mInputViewStarted = false;
        this.mCandidatesViewStarted = false;
        if (this.mInputStarted) {
            if (DEBUG) {
                Log.v(TAG, "CALL: onFinishInput");
            }
            onFinishInput();
        }
        this.mInputStarted = false;
        this.mStartedInputConnection = null;
        this.mCurCompletions = null;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for the security keyboard", property = OppoRomType.ROM)
    void doStartInput(InputConnection ic, EditorInfo attribute, boolean restarting) {
        startUpdateSecurityPassword(attribute);
        if (!restarting) {
            doFinishInput();
        }
        this.mInputStarted = true;
        this.mStartedInputConnection = ic;
        this.mInputEditorInfo = attribute;
        if (ic != null) {
            ic.reportFullscreenMode(this.mIsFullscreen);
        }
        initialize();
        if (DEBUG) {
            Log.v(TAG, "CALL: onStartInput");
        }
        onStartInput(attribute, restarting);
        if (!this.mWindowVisible) {
            return;
        }
        if (this.mShowInputRequested) {
            if (DEBUG) {
                Log.v(TAG, "CALL: onStartInputView");
            }
            this.mInputViewStarted = true;
            onStartInputView(this.mInputEditorInfo, restarting);
            startExtractingText(true);
        } else if (this.mCandidatesVisibility == 0) {
            if (DEBUG) {
                Log.v(TAG, "CALL: onStartCandidatesView");
            }
            this.mCandidatesViewStarted = true;
            onStartCandidatesView(this.mInputEditorInfo, restarting);
        }
    }

    public void onFinishInput() {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.finishComposingText();
        }
    }

    public void onDisplayCompletions(CompletionInfo[] completions) {
    }

    public void onUpdateExtractedText(int token, ExtractedText text) {
        if (!(this.mExtractedToken != token || text == null || this.mExtractEditText == null)) {
            this.mExtractedText = text;
            this.mExtractEditText.setExtractedText(text);
        }
    }

    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        ExtractEditText eet = this.mExtractEditText;
        if (eet != null && isFullscreenMode() && this.mExtractedText != null) {
            int off = this.mExtractedText.startOffset;
            eet.startInternalChanges();
            newSelStart -= off;
            newSelEnd -= off;
            int len = eet.getText().length();
            if (newSelStart < 0) {
                newSelStart = 0;
            } else if (newSelStart > len) {
                newSelStart = len;
            }
            if (newSelEnd < 0) {
                newSelEnd = 0;
            } else if (newSelEnd > len) {
                newSelEnd = len;
            }
            eet.setSelection(newSelStart, newSelEnd);
            eet.finishInternalChanges();
        }
    }

    public void onViewClicked(boolean focusChanged) {
    }

    @Deprecated
    public void onUpdateCursor(Rect newCursor) {
    }

    public void onUpdateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) {
    }

    public void requestHideSelf(int flags) {
        this.mImm.hideSoftInputFromInputMethod(this.mToken, flags);
    }

    private void requestShowSelf(int flags) {
        this.mImm.showSoftInputFromInputMethod(this.mToken, flags);
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for the security keyboard", property = OppoRomType.ROM)
    private boolean handleBack(boolean doIt) {
        if (this.mShowInputRequested) {
            if (doIt) {
                requestHideSelf(0);
            }
            return true;
        } else if (this.mWindowVisible) {
            if (this.mCandidatesVisibility == 0) {
                if (doIt) {
                    setCandidatesViewShown(false);
                }
            } else if (doIt) {
                doHideWindow();
            }
            return true;
        } else if (this.mSecurityWindow == null || !isSecurityWindowVisible()) {
            return false;
        } else {
            if (doIt) {
                hideSecurityWindow();
            }
            return true;
        }
    }

    private ExtractEditText getExtractEditTextIfVisible() {
        if (isExtractViewShown() && isInputViewShown()) {
            return this.mExtractEditText;
        }
        return null;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() != 4) {
            return doMovementKey(keyCode, event, -1);
        }
        ExtractEditText eet = getExtractEditTextIfVisible();
        if (eet != null && eet.handleBackInTextActionModeIfNeeded(event)) {
            return true;
        }
        if (!handleBack(false)) {
            return false;
        }
        event.startTracking();
        return true;
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        return doMovementKey(keyCode, event, count);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == 4) {
            ExtractEditText eet = getExtractEditTextIfVisible();
            if (eet != null && eet.handleBackInTextActionModeIfNeeded(event)) {
                return true;
            }
            if (event.isTracking() && !event.isCanceled()) {
                return handleBack(true);
            }
        }
        return doMovementKey(keyCode, event, -2);
    }

    public boolean onTrackballEvent(MotionEvent event) {
        if (DEBUG) {
            Log.v(TAG, "onTrackballEvent: " + event);
        }
        return false;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        if (DEBUG) {
            Log.v(TAG, "onGenericMotionEvent(): event " + event);
        }
        return false;
    }

    public void onAppPrivateCommand(String action, Bundle data) {
    }

    private void onToggleSoftInput(int showFlags, int hideFlags) {
        if (DEBUG) {
            Log.v(TAG, "toggleSoftInput()");
        }
        if (isInputViewShown()) {
            requestHideSelf(hideFlags);
        } else {
            requestShowSelf(showFlags);
        }
    }

    void reportExtractedMovement(int keyCode, int count) {
        int dx = 0;
        int dy = 0;
        switch (keyCode) {
            case 19:
                dy = -count;
                break;
            case 20:
                dy = count;
                break;
            case 21:
                dx = -count;
                break;
            case 22:
                dx = count;
                break;
        }
        onExtractedCursorMovement(dx, dy);
    }

    boolean doMovementKey(int keyCode, KeyEvent event, int count) {
        ExtractEditText eet = getExtractEditTextIfVisible();
        if (eet != null) {
            MovementMethod movement = eet.getMovementMethod();
            Layout layout = eet.getLayout();
            if (!(movement == null || layout == null)) {
                if (count == -1) {
                    if (movement.onKeyDown(eet, eet.getText(), keyCode, event)) {
                        reportExtractedMovement(keyCode, 1);
                        return true;
                    }
                } else if (count == -2) {
                    if (movement.onKeyUp(eet, eet.getText(), keyCode, event)) {
                        return true;
                    }
                } else if (movement.onKeyOther(eet, eet.getText(), event)) {
                    reportExtractedMovement(keyCode, count);
                } else {
                    KeyEvent down = KeyEvent.changeAction(event, 0);
                    if (movement.onKeyDown(eet, eet.getText(), keyCode, down)) {
                        KeyEvent up = KeyEvent.changeAction(event, 1);
                        movement.onKeyUp(eet, eet.getText(), keyCode, up);
                        while (true) {
                            count--;
                            if (count <= 0) {
                                break;
                            }
                            movement.onKeyDown(eet, eet.getText(), keyCode, down);
                            movement.onKeyUp(eet, eet.getText(), keyCode, up);
                        }
                        reportExtractedMovement(keyCode, count);
                    }
                }
            }
            switch (keyCode) {
                case 19:
                case 20:
                case 21:
                case 22:
                    return true;
            }
        }
        return false;
    }

    public void sendDownUpKeyEvents(int keyEventCode) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            long eventTime = SystemClock.uptimeMillis();
            ic.sendKeyEvent(new KeyEvent(eventTime, eventTime, 0, keyEventCode, 0, 0, -1, 0, 6));
            ic.sendKeyEvent(new KeyEvent(eventTime, SystemClock.uptimeMillis(), 1, keyEventCode, 0, 0, -1, 0, 6));
        }
    }

    public boolean sendDefaultEditorAction(boolean fromEnterKey) {
        EditorInfo ei = getCurrentInputEditorInfo();
        if (ei == null || ((fromEnterKey && (ei.imeOptions & 1073741824) != 0) || (ei.imeOptions & 255) == 1)) {
            return false;
        }
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.performEditorAction(ei.imeOptions & 255);
        }
        return true;
    }

    public void sendKeyChar(char charCode) {
        switch (charCode) {
            case 10:
                if (!sendDefaultEditorAction(true)) {
                    sendDownUpKeyEvents(66);
                    return;
                }
                return;
            default:
                if (charCode < '0' || charCode > '9') {
                    InputConnection ic = getCurrentInputConnection();
                    if (ic != null) {
                        ic.commitText(String.valueOf(charCode), 1);
                        return;
                    }
                    return;
                }
                sendDownUpKeyEvents((charCode - 48) + 7);
                return;
        }
    }

    public void onExtractedSelectionChanged(int start, int end) {
        InputConnection conn = getCurrentInputConnection();
        if (conn != null) {
            conn.setSelection(start, end);
        }
    }

    public void onExtractedDeleteText(int start, int end) {
        InputConnection conn = getCurrentInputConnection();
        if (conn != null) {
            conn.finishComposingText();
            conn.setSelection(start, start);
            conn.deleteSurroundingText(0, end - start);
        }
    }

    public void onExtractedReplaceText(int start, int end, CharSequence text) {
        InputConnection conn = getCurrentInputConnection();
        if (conn != null) {
            conn.setComposingRegion(start, end);
            conn.commitText(text, 1);
        }
    }

    public void onExtractedSetSpan(Object span, int start, int end, int flags) {
        InputConnection conn = getCurrentInputConnection();
        if (conn != null && conn.setSelection(start, end)) {
            CharSequence text = conn.getSelectedText(1);
            if (text instanceof Spannable) {
                ((Spannable) text).setSpan(span, 0, text.length(), flags);
                conn.setComposingRegion(start, end);
                conn.commitText(text, 1);
            }
        }
    }

    public void onExtractedTextClicked() {
        if (this.mExtractEditText != null && this.mExtractEditText.hasVerticalScrollBar()) {
            setCandidatesViewShown(false);
        }
    }

    /* JADX WARNING: Missing block: B:3:0x0007, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onExtractedCursorMovement(int dx, int dy) {
        if (!(this.mExtractEditText == null || dy == 0 || !this.mExtractEditText.hasVerticalScrollBar())) {
            setCandidatesViewShown(false);
        }
    }

    public boolean onExtractTextContextMenuItem(int id) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.performContextMenuAction(id);
        }
        return true;
    }

    public CharSequence getTextForImeAction(int imeOptions) {
        switch (imeOptions & 255) {
            case 1:
                return null;
            case 2:
                return getText(17040488);
            case 3:
                return getText(17040489);
            case 4:
                return getText(17040490);
            case 5:
                return getText(17040491);
            case 6:
                return getText(17040492);
            case 7:
                return getText(17040493);
            default:
                return getText(17040494);
        }
    }

    private int getIconForImeAction(int imeOptions) {
        switch (imeOptions & 255) {
            case 2:
                return 17302367;
            case 3:
                return 17302371;
            case 4:
                return 17302372;
            case 5:
                return 17302368;
            case 6:
                return 17302366;
            case 7:
                return 17302369;
            default:
                return 17302370;
        }
    }

    public void onUpdateExtractingVisibility(EditorInfo ei) {
        if (ei.inputType == 0 || (ei.imeOptions & 268435456) != 0) {
            setExtractViewShown(false);
        } else {
            setExtractViewShown(true);
        }
    }

    public void onUpdateExtractingViews(EditorInfo ei) {
        boolean z = true;
        if (isExtractViewShown() && this.mExtractAccessories != null) {
            boolean hasAction;
            if (ei.actionLabel != null) {
                hasAction = true;
            } else if ((ei.imeOptions & 255) == 1 || (ei.imeOptions & 536870912) != 0) {
                hasAction = false;
            } else {
                if (ei.inputType == 0) {
                    z = false;
                }
                hasAction = z;
            }
            if (hasAction) {
                this.mExtractAccessories.setVisibility(0);
                if (this.mExtractAction != null) {
                    if (this.mExtractAction instanceof ImageButton) {
                        ((ImageButton) this.mExtractAction).setImageResource(getIconForImeAction(ei.imeOptions));
                        if (ei.actionLabel != null) {
                            this.mExtractAction.setContentDescription(ei.actionLabel);
                        } else {
                            this.mExtractAction.setContentDescription(getTextForImeAction(ei.imeOptions));
                        }
                    } else if (ei.actionLabel != null) {
                        ((TextView) this.mExtractAction).setText(ei.actionLabel);
                    } else {
                        ((TextView) this.mExtractAction).setText(getTextForImeAction(ei.imeOptions));
                    }
                    this.mExtractAction.setOnClickListener(this.mActionClickListener);
                }
            } else {
                this.mExtractAccessories.setVisibility(8);
                if (this.mExtractAction != null) {
                    this.mExtractAction.setOnClickListener(null);
                }
            }
        }
    }

    public void onExtractingInputChanged(EditorInfo ei) {
        if (ei.inputType == 0) {
            requestHideSelf(2);
        }
    }

    void startExtractingText(boolean inputChanged) {
        ExtractedText extractedText = null;
        ExtractEditText eet = this.mExtractEditText;
        if (eet != null && getCurrentInputStarted() && isFullscreenMode()) {
            this.mExtractedToken++;
            ExtractedTextRequest req = new ExtractedTextRequest();
            req.token = this.mExtractedToken;
            req.flags = 1;
            req.hintMaxLines = 10;
            req.hintMaxChars = 10000;
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                extractedText = ic.getExtractedText(req, 1);
            }
            this.mExtractedText = extractedText;
            if (this.mExtractedText == null || ic == null) {
                Log.e(TAG, "Unexpected null in startExtractingText : mExtractedText = " + this.mExtractedText + ", input connection = " + ic);
            }
            EditorInfo ei = getCurrentInputEditorInfo();
            try {
                eet.startInternalChanges();
                onUpdateExtractingVisibility(ei);
                onUpdateExtractingViews(ei);
                int inputType = ei.inputType;
                if ((inputType & 15) == 1 && (262144 & inputType) != 0) {
                    inputType |= 131072;
                }
                eet.setInputType(inputType);
                eet.setHint(ei.hintText);
                if (this.mExtractedText != null) {
                    eet.setEnabled(true);
                    eet.setExtractedText(this.mExtractedText);
                } else {
                    eet.setEnabled(false);
                    eet.setText("");
                }
                eet.finishInternalChanges();
                if (inputChanged) {
                    onExtractingInputChanged(ei);
                }
            } catch (Throwable th) {
                eet.finishInternalChanges();
            }
        }
    }

    protected void onCurrentInputMethodSubtypeChanged(InputMethodSubtype newSubtype) {
        if (DEBUG) {
            int nameResId = newSubtype.getNameResId();
            Log.v(TAG, "--- " + ("changeInputMethodSubtype:" + (nameResId == 0 ? "<none>" : getString(nameResId)) + "," + newSubtype.getMode() + "," + newSubtype.getLocale() + "," + newSubtype.getExtraValue()));
        }
    }

    public int getInputMethodWindowRecommendedHeight() {
        return this.mImm.getInputMethodWindowVisibleHeight();
    }

    public final void exposeContent(InputContentInfo inputContentInfo, InputConnection inputConnection) {
        if (inputConnection != null && getCurrentInputConnection() == inputConnection) {
            this.mImm.exposeContent(this.mToken, inputContentInfo, getCurrentInputEditorInfo());
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        if (args.length == 1) {
            if ("enable".equals(args[0])) {
                DEBUG = true;
                return;
            } else if ("disable".equals(args[0])) {
                DEBUG = false;
                return;
            }
        }
        Printer p = new PrintWriterPrinter(fout);
        if (!dynamicallyConfigImsLogTag(p, args)) {
            p.println("Input method service state for " + this + ":");
            p.println("  mWindowCreated=" + this.mWindowCreated + " mWindowAdded=" + this.mWindowAdded);
            p.println("  mWindowVisible=" + this.mWindowVisible + " mWindowWasVisible=" + this.mWindowWasVisible + " mInShowWindow=" + this.mInShowWindow);
            p.println("  Configuration=" + getResources().getConfiguration());
            p.println("  mToken=" + this.mToken);
            p.println("  mInputBinding=" + this.mInputBinding);
            p.println("  mInputConnection=" + this.mInputConnection);
            p.println("  mStartedInputConnection=" + this.mStartedInputConnection);
            p.println("  mInputStarted=" + this.mInputStarted + " mInputViewStarted=" + this.mInputViewStarted + " mCandidatesViewStarted=" + this.mCandidatesViewStarted);
            if (this.mInputEditorInfo != null) {
                p.println("  mInputEditorInfo:");
                this.mInputEditorInfo.dump(p, "    ");
            } else {
                p.println("  mInputEditorInfo: null");
            }
            p.println("  mShowInputRequested=" + this.mShowInputRequested + " mLastShowInputRequested=" + this.mLastShowInputRequested + " mShowInputFlags=0x" + Integer.toHexString(this.mShowInputFlags));
            p.println("  mCandidatesVisibility=" + this.mCandidatesVisibility + " mFullscreenApplied=" + this.mFullscreenApplied + " mIsFullscreen=" + this.mIsFullscreen + " mExtractViewHidden=" + this.mExtractViewHidden);
            if (this.mExtractedText != null) {
                p.println("  mExtractedText:");
                p.println("    text=" + this.mExtractedText.text.length() + " chars" + " startOffset=" + this.mExtractedText.startOffset);
                p.println("    selectionStart=" + this.mExtractedText.selectionStart + " selectionEnd=" + this.mExtractedText.selectionEnd + " flags=0x" + Integer.toHexString(this.mExtractedText.flags));
            } else {
                p.println("  mExtractedText: null");
            }
            p.println("  mExtractedToken=" + this.mExtractedToken);
            p.println("  mIsInputViewShown=" + this.mIsInputViewShown + " mStatusIcon=" + this.mStatusIcon);
            p.println("Last computed insets:");
            p.println("  contentTopInsets=" + this.mTmpInsets.contentTopInsets + " visibleTopInsets=" + this.mTmpInsets.visibleTopInsets + " touchableInsets=" + this.mTmpInsets.touchableInsets + " touchableRegion=" + this.mTmpInsets.touchableRegion);
            p.println(" mShouldClearInsetOfPreviousIme=" + this.mShouldClearInsetOfPreviousIme);
            p.println(" mSettingsObserver=" + this.mSettingsObserver);
        }
    }

    private void initSecurityView() {
        this.mSecurityView = this.mInflater.inflate(17367149, null);
        this.mSecurityView.setSystemUiVisibility(768);
        this.mSecurityWindow.setContentView(this.mSecurityView);
    }

    private void updateSecurityView(View securityView) {
        this.mSecuritykeyboardArea = (FrameLayout) securityView.findViewById(R.id.inputArea);
        this.mSecuritykeyboardArea.setVisibility(0);
        this.mColorKeyboardView = new ColorKeyBoardView(this);
        this.mColorKeyboardView.setOnClickButtonListener(this.mButtonListener);
        this.mKeyBoardView = this.mColorKeyboardView.getColorKeyBoardView();
        this.mKeyboardHelper = new OppoPasswordEntryKeyboardHelper(securityView.getContext(), this.mKeyBoardView, null, 1, true);
        this.mColorKeyboardView.setKeyboardHelper(this.mKeyboardHelper);
        this.mKeyboardHelper.setKeyboardMode(0);
        this.mKeyBoardView.setOnKeyboardCharListener(this.mKeyboardChar);
        this.mSecuritykeyboardArea.addView(this.mColorKeyboardView);
        this.mKeyBoardOrientation = getResources().getConfiguration().orientation;
    }

    private void hideSecurityWindow() {
        if (this.mSecurityWindow != null && isSecurityWindowVisible()) {
            this.mSecurityWindow.hide();
            afterWindowHidden();
        }
    }

    private boolean isSecurityWindowVisible() {
        View decor = this.mSecurityWindow.getWindow().getDecorView();
        if (this.mSecurityWindow.isShowing() && decor != null && decor.getVisibility() == 0) {
            return true;
        }
        return false;
    }

    private void startUpdateSecurityPassword(EditorInfo ei) {
        boolean hasAction = ei.actionLabel == null ? ((ei.imeOptions & 255) == 1 || (ei.imeOptions & 536870912) != 0) ? false : ei.inputType != 0 : true;
        if (this.mKeyBoardView != null && hasAction) {
            this.mKeyBoardView.updateOkKey(getTextForImeAction(ei.imeOptions));
        }
    }

    private boolean needToShowSecurityWindow() {
        boolean z = false;
        if (this.mInputEditorInfo == null) {
            return false;
        }
        boolean isPasswordType;
        int inputType = this.mInputEditorInfo.inputType;
        if (TextView.isPasswordInputType(inputType)) {
            isPasswordType = true;
        } else {
            isPasswordType = TextView.isVisiblePasswordInputType(inputType);
        }
        if (isPasswordType && !inBlackList()) {
            z = true;
        }
        return z;
    }

    private boolean inBlackList() {
        if (this.mInputEditorInfo == null || this.mInputEditorInfo.packageName == null || getPackageName() == null) {
            return false;
        }
        return ColorSecureKeyboardUtils.getInstance().inBlackList(this.mInputEditorInfo.packageName, getPackageName());
    }

    protected boolean dynamicallyConfigImsLogTag(Printer printer, String[] args) {
        if (args.length != 3) {
            return false;
        }
        if (!"log".equals(args[0])) {
            return false;
        }
        String logCategoryTag = args[1];
        boolean on = WifiEnterpriseConfig.ENGINE_ENABLE.equals(args[2]);
        if ("all".equals(logCategoryTag) || "ims".equals(logCategoryTag)) {
            DEBUG = on;
        }
        return true;
    }

    private void beforeWindowShown() {
        if (DEBUG) {
            Log.v(TAG, "beforeWindowShown() mWindowVisible=" + this.mWindowVisible + " mNavigationBarState=" + mNavigationBarState);
        }
        if (mNavigationBarState == 1 && this.mColorStatusBarManager != null && !isInputWindowVisible() && !isSecurityWindowVisible()) {
            this.mColorStatusBarManager.showNavigationBarFromIme();
        }
    }

    private void afterWindowHidden() {
        if (DEBUG) {
            Log.v(TAG, "afterWindowHidden() mWindowVisible=" + this.mWindowVisible + " mNavigationBarState=" + mNavigationBarState);
        }
        if (mNavigationBarState == 1 && this.mColorStatusBarManager != null) {
            this.mColorStatusBarManager.hideNavigationBarFromIme();
        }
    }

    private boolean isInputWindowVisible() {
        View decor = this.mWindow.getWindow().getDecorView();
        if (this.mWindow.isShowing() && decor != null && decor.getVisibility() == 0) {
            return true;
        }
        return false;
    }
}
