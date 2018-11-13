package android.inputmethodservice;

import android.R;
import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.params.TonemapCurve;
import android.inputmethodservice.Keyboard.Key;
import android.media.AudioManager;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.color.util.ColorContextUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class KeyboardView extends View implements OnClickListener {
    public static final int COMMON_KEY = 0;
    private static final int DEBOUNCE_TIME = 70;
    private static final boolean DEBUG = false;
    private static final int DELAY_AFTER_PREVIEW = 70;
    private static final int DELAY_BEFORE_PREVIEW = 0;
    private static final int DELAY_DISSMISS_POPUPWINDOW = 120;
    public static final int DELETE_KEY = 1;
    private static final int[] KEY_DELETE = null;
    private static final String LOG_TAG = "KeyboardView";
    private static final int LONGPRESS_TIMEOUT = 0;
    private static final int[] LONG_PRESSABLE_STATE_SET = null;
    private static int MAX_NEARBY_KEYS = 0;
    private static final int MSG_LONGPRESS = 4;
    private static final int MSG_REMOVE_POPUPWINDOW = 5;
    private static final int MSG_REMOVE_PREVIEW = 2;
    private static final int MSG_REPEAT = 3;
    private static final int MSG_SHOW_PREVIEW = 1;
    private static final int MULTITAP_INTERVAL = 800;
    private static final int NOT_A_KEY = -1;
    public static final int OK_KEY = 2;
    private static final int REPEAT_INTERVAL = 50;
    private static final int REPEAT_START_DELAY = 400;
    public static final int SECURITYKEYBOARD = 1;
    public static final int UNLOCKKEYBOARD = 2;
    private boolean mAbortKey;
    private AccessibilityManager mAccessibilityManager;
    private AudioManager mAudioManager;
    private float mBackgroundDimAmount;
    private int mBgColor;
    private int mBgHeight;
    private int mBgWidth;
    private Bitmap mBuffer;
    private Canvas mCanvas;
    private Rect mClipRegion;
    private Drawable mColorEndKeyBg;
    private int mColorGoTextColor;
    private Drawable mColorLockDeleteKey;
    private Drawable mColorLockShiftKey;
    private Drawable mColorLockSpaceKey;
    private PopupWindow mColorPopup;
    private Drawable mColorPopupBg;
    private int mColorPopupHeight;
    private int mColorPopupWidth;
    private int mColorSecurityBg;
    private int mColorSecurityBgHeight;
    private int mColorSecurityBgWidth;
    private Drawable mColorSecurityKeyBg;
    private int mColorSecurityOffset1;
    private int mColorSecurityOffset2;
    private Drawable mColorSpecialKeyBg;
    private ColorStateList mColorTextColor;
    private final int[] mCoordinates;
    private int mCurrentKey;
    private int mCurrentKeyIndex;
    private long mCurrentKeyTime;
    private Rect mDirtyRect;
    private boolean mDisambiguateSwipe;
    private int[] mDistances;
    private int mDownKey;
    private long mDownTime;
    private boolean mDrawPending;
    private int mEndLabelSize;
    private GestureDetector mGestureDetector;
    Handler mHandler;
    private boolean mHasText;
    private boolean mHeadsetRequiredToHearPasswordsAnnounced;
    private boolean mInMultiTap;
    private Key mInvalidatedKey;
    private boolean mIsDownFlag;
    private boolean mIsEnable;
    private Drawable mKeyBackground;
    private int mKeyBoardType;
    private int[] mKeyIndices;
    private int mKeyNormalColor;
    private TextView mKeyPreview;
    private int mKeyTextColor;
    private int mKeyTextSize;
    private Keyboard mKeyboard;
    private OnKeyboardActionListener mKeyboardActionListener;
    private boolean mKeyboardChanged;
    private OnKeyboardCharListener mKeyboardCharListener;
    private Key[] mKeys;
    private int mLabelTextSize;
    private int mLastCodeX;
    private int mLastCodeY;
    private int mLastKey;
    private long mLastKeyTime;
    private long mLastMoveTime;
    private int mLastSentIndex;
    private long mLastTapTime;
    private int mLastX;
    private int mLastY;
    private int mLowerLetterSize;
    private KeyboardView mMiniKeyboard;
    private Map<Key, View> mMiniKeyboardCache;
    private View mMiniKeyboardContainer;
    private int mMiniKeyboardOffsetX;
    private int mMiniKeyboardOffsetY;
    private boolean mMiniKeyboardOnScreen;
    private int mOldPointerCount;
    private float mOldPointerX;
    private float mOldPointerY;
    private Rect mPadding;
    private Paint mPaint;
    private PopupWindow mPopupKeyboard;
    private int mPopupLayout;
    private View mPopupParent;
    private int mPopupPreviewX;
    private int mPopupPreviewY;
    private int mPopupX;
    private int mPopupY;
    private boolean mPossiblePoly;
    private ColorStateList mPressedColor;
    private boolean mPreviewCentered;
    private int mPreviewHeight;
    private StringBuilder mPreviewLabel;
    private int mPreviewOffset;
    private PopupWindow mPreviewPopup;
    private TextView mPreviewText;
    private int mPreviewTextSizeLarge;
    private int mPreviousKey;
    private boolean mProximityCorrectOn;
    private int mProximityThreshold;
    private int mRepeatKeyIndex;
    private int mShadowColor;
    private float mShadowRadius;
    private boolean mShowPreview;
    private boolean mShowTouchPoints;
    private int mSpaceLabelSize;
    private ColorStateList mSpecialColor;
    private int mStartX;
    private int mStartY;
    private int mSwipeThreshold;
    private SwipeTracker mSwipeTracker;
    private int mTapCount;
    private Drawable mTransParentBg;
    private Typeface mTypeface;
    private CharSequence mUpdateText;
    private int mVerticalCorrection;

    public interface OnKeyboardCharListener {
        void onCharacter(String str, int i);
    }

    public interface OnKeyboardActionListener {
        void onKey(int i, int[] iArr);

        void onPress(int i);

        void onRelease(int i);

        void onText(CharSequence charSequence);

        void swipeDown();

        void swipeLeft();

        void swipeRight();

        void swipeUp();
    }

    /* renamed from: android.inputmethodservice.KeyboardView$3 */
    class AnonymousClass3 implements OnKeyboardActionListener {
        final /* synthetic */ KeyboardView this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.inputmethodservice.KeyboardView.3.<init>(android.inputmethodservice.KeyboardView):void, dex: 
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
        AnonymousClass3(android.inputmethodservice.KeyboardView r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.inputmethodservice.KeyboardView.3.<init>(android.inputmethodservice.KeyboardView):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.inputmethodservice.KeyboardView.3.<init>(android.inputmethodservice.KeyboardView):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.inputmethodservice.KeyboardView.3.onKey(int, int[]):void, dex: 
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
        public void onKey(int r1, int[] r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.inputmethodservice.KeyboardView.3.onKey(int, int[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.inputmethodservice.KeyboardView.3.onKey(int, int[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.inputmethodservice.KeyboardView.3.onPress(int):void, dex: 
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
        public void onPress(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.inputmethodservice.KeyboardView.3.onPress(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.inputmethodservice.KeyboardView.3.onPress(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.inputmethodservice.KeyboardView.3.onRelease(int):void, dex: 
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
        public void onRelease(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.inputmethodservice.KeyboardView.3.onRelease(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.inputmethodservice.KeyboardView.3.onRelease(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.inputmethodservice.KeyboardView.3.onText(java.lang.CharSequence):void, dex: 
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
        public void onText(java.lang.CharSequence r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.inputmethodservice.KeyboardView.3.onText(java.lang.CharSequence):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.inputmethodservice.KeyboardView.3.onText(java.lang.CharSequence):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.inputmethodservice.KeyboardView.3.swipeDown():void, dex: 
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
        public void swipeDown() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.inputmethodservice.KeyboardView.3.swipeDown():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.inputmethodservice.KeyboardView.3.swipeDown():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.inputmethodservice.KeyboardView.3.swipeLeft():void, dex: 
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
        public void swipeLeft() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.inputmethodservice.KeyboardView.3.swipeLeft():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.inputmethodservice.KeyboardView.3.swipeLeft():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.inputmethodservice.KeyboardView.3.swipeRight():void, dex: 
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
        public void swipeRight() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.inputmethodservice.KeyboardView.3.swipeRight():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.inputmethodservice.KeyboardView.3.swipeRight():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.inputmethodservice.KeyboardView.3.swipeUp():void, dex: 
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
        public void swipeUp() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.inputmethodservice.KeyboardView.3.swipeUp():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.inputmethodservice.KeyboardView.3.swipeUp():void");
        }
    }

    private static class SwipeTracker {
        static final int LONGEST_PAST_TIME = 200;
        static final int NUM_PAST = 4;
        final long[] mPastTime;
        final float[] mPastX;
        final float[] mPastY;
        float mXVelocity;
        float mYVelocity;

        /* synthetic */ SwipeTracker(SwipeTracker swipeTracker) {
            this();
        }

        private SwipeTracker() {
            this.mPastX = new float[4];
            this.mPastY = new float[4];
            this.mPastTime = new long[4];
        }

        public void clear() {
            this.mPastTime[0] = 0;
        }

        public void addMovement(MotionEvent ev) {
            long time = ev.getEventTime();
            int N = ev.getHistorySize();
            for (int i = 0; i < N; i++) {
                addPoint(ev.getHistoricalX(i), ev.getHistoricalY(i), ev.getHistoricalEventTime(i));
            }
            addPoint(ev.getX(), ev.getY(), time);
        }

        private void addPoint(float x, float y, long time) {
            int drop = -1;
            long[] pastTime = this.mPastTime;
            int i = 0;
            while (i < 4 && pastTime[i] != 0) {
                if (pastTime[i] < time - 200) {
                    drop = i;
                }
                i++;
            }
            if (i == 4 && drop < 0) {
                drop = 0;
            }
            if (drop == i) {
                drop--;
            }
            float[] pastX = this.mPastX;
            float[] pastY = this.mPastY;
            if (drop >= 0) {
                int start = drop + 1;
                int count = (4 - drop) - 1;
                System.arraycopy(pastX, start, pastX, 0, count);
                System.arraycopy(pastY, start, pastY, 0, count);
                System.arraycopy(pastTime, start, pastTime, 0, count);
                i -= drop + 1;
            }
            pastX[i] = x;
            pastY[i] = y;
            pastTime[i] = time;
            i++;
            if (i < 4) {
                pastTime[i] = 0;
            }
        }

        public void computeCurrentVelocity(int units) {
            computeCurrentVelocity(units, Float.MAX_VALUE);
        }

        public void computeCurrentVelocity(int units, float maxVelocity) {
            float max;
            float[] pastX = this.mPastX;
            float[] pastY = this.mPastY;
            long[] pastTime = this.mPastTime;
            float oldestX = pastX[0];
            float oldestY = pastY[0];
            long oldestTime = pastTime[0];
            float accumX = TonemapCurve.LEVEL_BLACK;
            float accumY = TonemapCurve.LEVEL_BLACK;
            int N = 0;
            while (N < 4 && pastTime[N] != 0) {
                N++;
            }
            for (int i = 1; i < N; i++) {
                int dur = (int) (pastTime[i] - oldestTime);
                if (dur != 0) {
                    float vel = ((pastX[i] - oldestX) / ((float) dur)) * ((float) units);
                    if (accumX == TonemapCurve.LEVEL_BLACK) {
                        accumX = vel;
                    } else {
                        accumX = (accumX + vel) * 0.5f;
                    }
                    vel = ((pastY[i] - oldestY) / ((float) dur)) * ((float) units);
                    if (accumY == TonemapCurve.LEVEL_BLACK) {
                        accumY = vel;
                    } else {
                        accumY = (accumY + vel) * 0.5f;
                    }
                }
            }
            if (accumX < TonemapCurve.LEVEL_BLACK) {
                max = Math.max(accumX, -maxVelocity);
            } else {
                max = Math.min(accumX, maxVelocity);
            }
            this.mXVelocity = max;
            if (accumY < TonemapCurve.LEVEL_BLACK) {
                max = Math.max(accumY, -maxVelocity);
            } else {
                max = Math.min(accumY, maxVelocity);
            }
            this.mYVelocity = max;
        }

        public float getXVelocity() {
            return this.mXVelocity;
        }

        public float getYVelocity() {
            return this.mYVelocity;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.inputmethodservice.KeyboardView.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.inputmethodservice.KeyboardView.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.inputmethodservice.KeyboardView.<clinit>():void");
    }

    public KeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 18219140);
    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Add for 3.0", property = OppoRomType.ROM)
    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mCurrentKeyIndex = -1;
        this.mCoordinates = new int[2];
        this.mPreviewCentered = false;
        this.mShowPreview = true;
        this.mShowTouchPoints = true;
        this.mCurrentKey = -1;
        this.mDownKey = -1;
        this.mKeyIndices = new int[12];
        this.mRepeatKeyIndex = -1;
        this.mClipRegion = new Rect(0, 0, 0, 0);
        this.mSwipeTracker = new SwipeTracker();
        this.mOldPointerCount = 1;
        this.mDistances = new int[MAX_NEARBY_KEYS];
        this.mPreviewLabel = new StringBuilder(1);
        this.mDirtyRect = new Rect();
        this.mTransParentBg = null;
        this.mPressedColor = null;
        this.mBgWidth = 0;
        this.mBgHeight = 0;
        this.mSpecialColor = null;
        this.mKeyBoardType = 0;
        this.mColorSecurityKeyBg = null;
        this.mColorSpecialKeyBg = null;
        this.mColorEndKeyBg = null;
        this.mTypeface = null;
        this.mColorPopupBg = null;
        this.mHasText = false;
        this.mUpdateText = null;
        this.mLowerLetterSize = 0;
        this.mSpaceLabelSize = 0;
        this.mEndLabelSize = 0;
        this.mColorLockDeleteKey = null;
        this.mColorLockShiftKey = null;
        this.mColorLockSpaceKey = null;
        this.mIsEnable = true;
        this.mPreviousKey = -1;
        this.mIsDownFlag = false;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KeyboardView, defStyleAttr, defStyleRes);
        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int previewLayout = 0;
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case 0:
                    this.mShadowColor = a.getColor(attr, 0);
                    break;
                case 1:
                    this.mShadowRadius = a.getFloat(attr, TonemapCurve.LEVEL_BLACK);
                    break;
                case 2:
                    this.mKeyBackground = a.getDrawable(attr);
                    break;
                case 3:
                    this.mKeyTextSize = a.getDimensionPixelSize(attr, 18);
                    break;
                case 4:
                    this.mLabelTextSize = a.getDimensionPixelSize(attr, 14);
                    break;
                case 5:
                    this.mKeyTextColor = a.getColor(attr, -16777216);
                    break;
                case 6:
                    previewLayout = a.getResourceId(attr, 0);
                    break;
                case 7:
                    this.mPreviewOffset = a.getDimensionPixelOffset(attr, 0);
                    break;
                case 8:
                    this.mPreviewHeight = a.getDimensionPixelSize(attr, 80);
                    break;
                case 9:
                    this.mVerticalCorrection = a.getDimensionPixelOffset(attr, 0);
                    break;
                case 10:
                    this.mPopupLayout = a.getResourceId(attr, 0);
                    break;
                default:
                    break;
            }
        }
        this.mBackgroundDimAmount = this.mContext.obtainStyledAttributes(com.android.internal.R.styleable.Theme).getFloat(2, 0.5f);
        this.mPreviewPopup = new PopupWindow(context);
        if (previewLayout != 0) {
            this.mPreviewText = (TextView) inflate.inflate(previewLayout, null);
            this.mPreviewTextSizeLarge = (int) this.mPreviewText.getTextSize();
            this.mPreviewPopup.setContentView(this.mPreviewText);
            this.mPreviewPopup.setBackgroundDrawable(null);
        } else {
            this.mShowPreview = false;
        }
        this.mPreviewPopup.setTouchable(false);
        this.mPopupKeyboard = new PopupWindow(context);
        this.mPopupKeyboard.setBackgroundDrawable(null);
        this.mPopupParent = this;
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setTextSize(TonemapCurve.LEVEL_BLACK);
        this.mPaint.setTextAlign(Align.CENTER);
        this.mPaint.setAlpha(255);
        this.mPadding = new Rect(0, 0, 0, 0);
        this.mMiniKeyboardCache = new HashMap();
        if (this.mKeyBackground != null) {
            this.mKeyBackground.getPadding(this.mPadding);
        }
        this.mSwipeThreshold = (int) (getResources().getDisplayMetrics().density * 500.0f);
        this.mDisambiguateSwipe = getResources().getBoolean(17956941);
        this.mAccessibilityManager = AccessibilityManager.getInstance(context);
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        resetMultiTap();
        if (ColorContextUtil.isOppoStyle(getContext())) {
            this.mTransParentBg = getResources().getDrawable(201852153);
            this.mBgColor = getResources().getColor(201720869);
            this.mPressedColor = getResources().getColorStateList(201720870);
            this.mSpecialColor = getResources().getColorStateList(201720871);
            this.mKeyNormalColor = getResources().getColor(201720872);
            if (this.mTransParentBg != null) {
                this.mBgWidth = this.mTransParentBg.getIntrinsicWidth();
                this.mBgHeight = this.mTransParentBg.getIntrinsicHeight();
            }
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for the security poopupWindow", property = OppoRomType.ROM)
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initGestureDetector();
        if (this.mHandler == null) {
            this.mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 1:
                            KeyboardView.this.showKey(msg.arg1);
                            return;
                        case 2:
                            KeyboardView.this.mPreviewText.setVisibility(4);
                            return;
                        case 3:
                            if (KeyboardView.this.repeatKey()) {
                                sendMessageDelayed(Message.obtain((Handler) this, 3), 50);
                                return;
                            }
                            return;
                        case 4:
                            KeyboardView.this.openPopupIfRequired((MotionEvent) msg.obj);
                            return;
                        case 5:
                            if (KeyboardView.this.isSecurityKeyboard()) {
                                KeyboardView.this.dismissPopupWindow();
                                return;
                            }
                            return;
                        default:
                            return;
                    }
                }
            };
        }
    }

    private void initGestureDetector() {
        if (this.mGestureDetector == null) {
            this.mGestureDetector = new GestureDetector(getContext(), new SimpleOnGestureListener() {
                public boolean onFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
                    if (KeyboardView.this.mPossiblePoly) {
                        return false;
                    }
                    float absX = Math.abs(velocityX);
                    float absY = Math.abs(velocityY);
                    float deltaX = me2.getX() - me1.getX();
                    float deltaY = me2.getY() - me1.getY();
                    int travelX = KeyboardView.this.getWidth() / 2;
                    int travelY = KeyboardView.this.getHeight() / 2;
                    KeyboardView.this.mSwipeTracker.computeCurrentVelocity(1000);
                    float endingVelocityX = KeyboardView.this.mSwipeTracker.getXVelocity();
                    float endingVelocityY = KeyboardView.this.mSwipeTracker.getYVelocity();
                    boolean sendDownKey = false;
                    if (velocityX <= ((float) KeyboardView.this.mSwipeThreshold) || absY >= absX || deltaX <= ((float) travelX)) {
                        if (velocityX >= ((float) (-KeyboardView.this.mSwipeThreshold)) || absY >= absX || deltaX >= ((float) (-travelX))) {
                            if (velocityY >= ((float) (-KeyboardView.this.mSwipeThreshold)) || absX >= absY || deltaY >= ((float) (-travelY))) {
                                if (velocityY > ((float) KeyboardView.this.mSwipeThreshold) && absX < absY / 2.0f && deltaY > ((float) travelY)) {
                                    if (!KeyboardView.this.mDisambiguateSwipe || endingVelocityY >= velocityY / 4.0f) {
                                        KeyboardView.this.swipeDown();
                                        return true;
                                    }
                                    sendDownKey = true;
                                }
                            } else if (!KeyboardView.this.mDisambiguateSwipe || endingVelocityY <= velocityY / 4.0f) {
                                KeyboardView.this.swipeUp();
                                return true;
                            } else {
                                sendDownKey = true;
                            }
                        } else if (!KeyboardView.this.mDisambiguateSwipe || endingVelocityX <= velocityX / 4.0f) {
                            KeyboardView.this.swipeLeft();
                            return true;
                        } else {
                            sendDownKey = true;
                        }
                    } else if (!KeyboardView.this.mDisambiguateSwipe || endingVelocityX >= velocityX / 4.0f) {
                        KeyboardView.this.swipeRight();
                        return true;
                    } else {
                        sendDownKey = true;
                    }
                    if (sendDownKey) {
                        KeyboardView.this.detectAndSendKey(KeyboardView.this.mDownKey, KeyboardView.this.mStartX, KeyboardView.this.mStartY, me1.getEventTime());
                    }
                    return false;
                }
            });
            this.mGestureDetector.setIsLongpressEnabled(false);
        }
    }

    public void setOnKeyboardActionListener(OnKeyboardActionListener listener) {
        this.mKeyboardActionListener = listener;
    }

    protected OnKeyboardActionListener getOnKeyboardActionListener() {
        return this.mKeyboardActionListener;
    }

    public void setKeyboard(Keyboard keyboard) {
        if (this.mKeyboard != null) {
            showPreview(-1);
        }
        removeMessages();
        this.mKeyboard = keyboard;
        List<Key> keys = this.mKeyboard.getKeys();
        this.mKeys = (Key[]) keys.toArray(new Key[keys.size()]);
        requestLayout();
        this.mKeyboardChanged = true;
        invalidateAllKeys();
        computeProximityThreshold(keyboard);
        this.mMiniKeyboardCache.clear();
        this.mAbortKey = true;
    }

    public Keyboard getKeyboard() {
        return this.mKeyboard;
    }

    public boolean setShifted(boolean shifted) {
        if (this.mKeyboard == null || !this.mKeyboard.setShifted(shifted)) {
            return false;
        }
        invalidateAllKeys();
        return true;
    }

    public boolean isShifted() {
        if (this.mKeyboard != null) {
            return this.mKeyboard.isShifted();
        }
        return false;
    }

    public void setPreviewEnabled(boolean previewEnabled) {
        this.mShowPreview = previewEnabled;
    }

    public boolean isPreviewEnabled() {
        return this.mShowPreview;
    }

    public void setVerticalCorrection(int verticalOffset) {
    }

    public void setPopupParent(View v) {
        this.mPopupParent = v;
    }

    public void setPopupOffset(int x, int y) {
        this.mMiniKeyboardOffsetX = x;
        this.mMiniKeyboardOffsetY = y;
        if (this.mPreviewPopup.isShowing()) {
            this.mPreviewPopup.dismiss();
        }
    }

    public void setProximityCorrectionEnabled(boolean enabled) {
        this.mProximityCorrectOn = enabled;
    }

    public boolean isProximityCorrectionEnabled() {
        return this.mProximityCorrectOn;
    }

    public void onClick(View v) {
        dismissPopupKeyboard();
    }

    private CharSequence adjustCase(CharSequence label) {
        if (!this.mKeyboard.isShifted() || label == null || label.length() >= 3 || !Character.isLowerCase(label.charAt(0))) {
            return label;
        }
        return label.toString().toUpperCase();
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for security window", property = OppoRomType.ROM)
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mKeyboard == null) {
            setMeasuredDimension(this.mPaddingLeft + this.mPaddingRight, this.mPaddingTop + this.mPaddingBottom);
            return;
        }
        int width = (this.mKeyboard.getMinWidth() + this.mPaddingLeft) + this.mPaddingRight;
        if (isSecurityKeyboard() && width < this.mColorSecurityBgWidth) {
            width = this.mColorSecurityBgWidth;
        }
        if (MeasureSpec.getSize(widthMeasureSpec) < width + 10) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        int height = (this.mKeyboard.getHeight() + this.mPaddingTop) + this.mPaddingBottom;
        if (isSecurityKeyboard() && height < this.mColorSecurityBgHeight) {
            height = this.mColorSecurityBgHeight;
        }
        setMeasuredDimension(width, height);
    }

    private void computeProximityThreshold(Keyboard keyboard) {
        if (keyboard != null) {
            Key[] keys = this.mKeys;
            if (keys != null) {
                int dimensionSum = 0;
                for (Key key : keys) {
                    dimensionSum += Math.min(key.width, key.height) + key.gap;
                }
                if (dimensionSum >= 0 && length != 0) {
                    this.mProximityThreshold = (int) ((((float) dimensionSum) * 1.4f) / ((float) length));
                    this.mProximityThreshold *= this.mProximityThreshold;
                }
            }
        }
    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.mKeyboard != null) {
            this.mKeyboard.resize(w, h);
        }
        this.mBuffer = null;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for 3.0", property = OppoRomType.ROM)
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isSecurityKeyboard()) {
            canvas.save();
            canvas.clipRect(0, 0, this.mColorSecurityBgWidth, this.mColorSecurityBgHeight);
            canvas.drawColor(this.mColorSecurityBg);
            canvas.restore();
        }
        if (isNumberUnLockKeyboard()) {
            canvas.save();
            canvas.clipRect(0, 0, this.mBgWidth, this.mBgHeight);
            canvas.drawColor(this.mBgColor);
            canvas.restore();
        }
        if (this.mDrawPending || this.mBuffer == null || this.mKeyboardChanged) {
            onBufferDraw();
        }
        canvas.drawBitmap(this.mBuffer, (float) TonemapCurve.LEVEL_BLACK, (float) TonemapCurve.LEVEL_BLACK, null);
        if (isNumberUnLockKeyboard() && this.mTransParentBg != null) {
            this.mTransParentBg.setBounds(0, 0, this.mBgWidth, this.mBgHeight);
            this.mTransParentBg.draw(canvas);
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for the new GUI of the 3.0", property = OppoRomType.ROM)
    private void onBufferDraw() {
        if (this.mBuffer == null || this.mKeyboardChanged) {
            if (this.mBuffer == null || (this.mKeyboardChanged && !(this.mBuffer.getWidth() == getWidth() && this.mBuffer.getHeight() == getHeight()))) {
                this.mBuffer = Bitmap.createBitmap(Math.max(1, getWidth()), Math.max(1, getHeight()), Config.ARGB_8888);
                this.mCanvas = new Canvas(this.mBuffer);
            }
            invalidateAllKeys();
            this.mKeyboardChanged = false;
        }
        Canvas canvas = this.mCanvas;
        canvas.clipRect(this.mDirtyRect, Op.REPLACE);
        if (this.mKeyboard != null) {
            Paint paint = this.mPaint;
            Drawable keyBackground = this.mKeyBackground;
            Rect clipRegion = this.mClipRegion;
            Rect padding = this.mPadding;
            int kbdPaddingLeft = this.mPaddingLeft;
            int kbdPaddingTop = this.mPaddingTop;
            Key[] keys = this.mKeys;
            Key invalidKey = this.mInvalidatedKey;
            paint.setColor(this.mKeyTextColor);
            boolean drawSingleKey = false;
            if (invalidKey != null && canvas.getClipBounds(clipRegion) && (invalidKey.x + kbdPaddingLeft) - 1 <= clipRegion.left && (invalidKey.y + kbdPaddingTop) - 1 <= clipRegion.top && ((invalidKey.x + invalidKey.width) + kbdPaddingLeft) + 1 >= clipRegion.right && ((invalidKey.y + invalidKey.height) + kbdPaddingTop) + 1 >= clipRegion.bottom) {
                drawSingleKey = true;
            }
            canvas.drawColor(0, Mode.CLEAR);
            int keyCount = keys.length;
            int i = 0;
            while (i < keyCount) {
                Key key = keys[i];
                if (!drawSingleKey || invalidKey == key) {
                    int[] drawableState = key.getCurrentDrawableState();
                    if (isNumberUnLockKeyboard()) {
                        int left = key.x + kbdPaddingLeft;
                        int top = key.y + kbdPaddingTop;
                        int right = (key.x + kbdPaddingLeft) + key.width;
                        int bottom = (key.y + kbdPaddingTop) + key.height;
                        canvas.save();
                        canvas.clipRect(left, top, right, bottom);
                        if (!(i == 9 || i == 11 || this.mPressedColor == null)) {
                            canvas.drawColor(this.mPressedColor.getColorForState(drawableState, 0));
                        }
                        if (i == 11 && this.mSpecialColor != null) {
                            canvas.drawColor(this.mSpecialColor.getColorForState(drawableState, 0));
                        }
                        if (i == 9) {
                            canvas.drawColor(this.mKeyNormalColor);
                        }
                        canvas.restore();
                    }
                    if (isSecurityKeyboard()) {
                        int color;
                        if (this.mColorSpecialKeyBg != null && (key.codes[0] == -1 || key.codes[0] == -5 || key.codes[0] == -2)) {
                            keyBackground = this.mColorSpecialKeyBg;
                        } else if (this.mColorSecurityKeyBg != null && key.codes[0] != 10) {
                            keyBackground = this.mColorSecurityKeyBg;
                        } else if (key.codes[0] == 10 && this.mColorEndKeyBg != null) {
                            keyBackground = this.mColorEndKeyBg;
                        }
                        if (this.mUpdateText != null && i == keyCount - 1) {
                            if (key.icon != null) {
                                key.icon = null;
                            }
                            key.label = this.mUpdateText;
                        }
                        if (i == keyCount - 1) {
                            color = this.mColorGoTextColor;
                        } else {
                            color = this.mColorTextColor.getColorForState(drawableState, 0);
                        }
                        paint.setColor(color);
                    }
                    if (keyBackground != null) {
                        keyBackground.setState(drawableState);
                    }
                    String label = key.label == null ? null : adjustCase(key.label).toString();
                    if (keyBackground != null) {
                        Rect bounds = keyBackground.getBounds();
                        if (!(key.width == bounds.right && key.height == bounds.bottom)) {
                            keyBackground.setBounds(0, 0, key.width, key.height);
                        }
                    }
                    canvas.translate((float) (key.x + kbdPaddingLeft), (float) (key.y + kbdPaddingTop));
                    if (keyBackground != null) {
                        keyBackground.draw(canvas);
                    }
                    if (label != null) {
                        float textY;
                        if (isSecurityKeyboard()) {
                            if (Character.isLowerCase(label.charAt(0)) && key.codes[0] != 32) {
                                paint.setTextSize((float) this.mLowerLetterSize);
                            } else if (key.codes[0] == 32) {
                                paint.setTextSize((float) this.mSpaceLabelSize);
                            } else if (key.codes[0] == -2 || key.codes[0] == 10 || key.codes[0] == -1) {
                                paint.setTextSize((float) this.mEndLabelSize);
                            } else {
                                paint.setTextSize((float) this.mKeyTextSize);
                            }
                            if (this.mTypeface != null) {
                                paint.setTypeface(this.mTypeface);
                            }
                        } else if (label.length() <= 1 || key.codes.length >= 2) {
                            paint.setTextSize((float) this.mKeyTextSize);
                            paint.setTypeface(Typeface.DEFAULT);
                        } else {
                            paint.setTextSize((float) this.mLabelTextSize);
                            paint.setTypeface(Typeface.DEFAULT_BOLD);
                        }
                        if (!isSecurityKeyboard()) {
                            paint.setShadowLayer(this.mShadowRadius, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, this.mShadowColor);
                        }
                        FontMetricsInt fmi = paint.getFontMetricsInt();
                        if (isSecurityKeyboard()) {
                            textY = (float) ((((key.height - (fmi.bottom - fmi.top)) / 2) - fmi.top) + ((padding.bottom - padding.top) / 2));
                            if (!(key.codes[0] == -2 || key.codes[0] == 10)) {
                                textY = Character.isLowerCase(label.charAt(0)) ? textY - ((float) this.mColorSecurityOffset2) : textY - ((float) this.mColorSecurityOffset1);
                            }
                        } else {
                            textY = (((float) (((key.height - padding.top) - padding.bottom) / 2)) + ((paint.getTextSize() - paint.descent()) / 2.0f)) + ((float) padding.top);
                        }
                        if (isUnLockKeyboard() && i < 10) {
                            Log.d(LOG_TAG, "ondraw label : " + label + ", key.x = " + key.x + ", " + kbdPaddingLeft);
                        }
                        canvas.drawText(label, (float) ((((key.width - padding.left) - padding.right) / 2) + padding.left), textY, paint);
                        if (!isSecurityKeyboard()) {
                            paint.setShadowLayer(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, 0);
                        }
                    } else if (key.icon != null) {
                        if (isUnLockKeyboard() && key.codes[0] == -5 && this.mColorLockDeleteKey != null) {
                            key.icon = this.mColorLockDeleteKey;
                        }
                        if (isUnLockKeyboard() && key.codes[0] == -1 && this.mColorLockShiftKey != null && !this.mKeyboard.isShifted()) {
                            key.icon = this.mColorLockShiftKey;
                        }
                        if (isUnLockKeyboard() && key.codes[0] == 32 && this.mColorLockSpaceKey != null) {
                            key.icon = this.mColorLockSpaceKey;
                        }
                        int drawableX = ((((key.width - padding.left) - padding.right) - key.icon.getIntrinsicWidth()) / 2) + padding.left;
                        int drawableY = ((((key.height - padding.top) - padding.bottom) - key.icon.getIntrinsicHeight()) / 2) + padding.top;
                        if (isSecurityKeyboard()) {
                            drawableY = (key.height - key.icon.getIntrinsicHeight()) / 2;
                        }
                        canvas.translate((float) drawableX, (float) drawableY);
                        key.icon.setBounds(0, 0, key.icon.getIntrinsicWidth(), key.icon.getIntrinsicHeight());
                        key.icon.draw(canvas);
                        canvas.translate((float) (-drawableX), (float) (-drawableY));
                    }
                    canvas.translate((float) ((-key.x) - kbdPaddingLeft), (float) ((-key.y) - kbdPaddingTop));
                }
                i++;
            }
            this.mInvalidatedKey = null;
            if (this.mMiniKeyboardOnScreen) {
                paint.setColor(((int) (this.mBackgroundDimAmount * 255.0f)) << 24);
                canvas.drawRect(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, (float) getWidth(), (float) getHeight(), paint);
            }
            this.mDrawPending = false;
            this.mDirtyRect.setEmpty();
        }
    }

    /* JADX WARNING: Missing block: B:22:0x0091, code:
            if (r7 < r22.mProximityThreshold) goto L_0x0093;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for the BUG:949796 ", property = OppoRomType.ROM)
    private int getKeyIndices(int x, int y, int[] allKeys) {
        Key[] keys = this.mKeys;
        int primaryIndex = -1;
        int closestKey = -1;
        int closestKeyDist = this.mProximityThreshold + 1;
        Arrays.fill(this.mDistances, Integer.MAX_VALUE);
        int[] nearestKeyIndices = this.mKeyboard.getNearestKeys(x, y);
        int keyCount = nearestKeyIndices.length;
        for (int i = 0; i < keyCount; i++) {
            boolean isProximity;
            Key key = keys[nearestKeyIndices[i]];
            int dist = 0;
            boolean isInside = key.isInside(x, y);
            if (isInside) {
                primaryIndex = nearestKeyIndices[i];
            }
            if (isSecurityKeyboard()) {
                if (this.mProximityCorrectOn) {
                    dist = key.squaredDistanceFrom(x, y);
                    if (dist < this.mProximityThreshold) {
                        isProximity = true;
                    }
                }
                isProximity = isInside;
            } else {
                if (this.mProximityCorrectOn) {
                    dist = key.squaredDistanceFrom(x, y);
                }
                if (!isInside) {
                    isProximity = false;
                }
                isProximity = key.codes[0] > 32;
            }
            if (isProximity) {
                int nCodes = key.codes.length;
                if (dist < closestKeyDist) {
                    closestKeyDist = dist;
                    closestKey = nearestKeyIndices[i];
                }
                if (allKeys != null) {
                    int j = 0;
                    while (j < this.mDistances.length) {
                        if (this.mDistances[j] > dist) {
                            System.arraycopy(this.mDistances, j, this.mDistances, j + nCodes, (this.mDistances.length - j) - nCodes);
                            System.arraycopy(allKeys, j, allKeys, j + nCodes, (allKeys.length - j) - nCodes);
                            for (int c = 0; c < nCodes; c++) {
                                allKeys[j + c] = key.codes[c];
                                this.mDistances[j + c] = dist;
                            }
                        } else {
                            j++;
                        }
                    }
                }
            }
        }
        if (primaryIndex == -1) {
            return closestKey;
        }
        return primaryIndex;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for delete the c code", property = OppoRomType.ROM)
    private void detectAndSendKey(int index, int x, int y, long eventTime) {
        if (index != -1 && index < this.mKeys.length) {
            Key key = this.mKeys[index];
            if (key.text != null) {
                this.mKeyboardActionListener.onText(key.text);
                this.mKeyboardActionListener.onRelease(-1);
            } else {
                int code = key.codes[0];
                int[] codes = new int[MAX_NEARBY_KEYS];
                Arrays.fill(codes, -1);
                getKeyIndices(x, y, codes);
                if (this.mInMultiTap) {
                    if (this.mTapCount != -1) {
                        this.mKeyboardActionListener.onKey(-5, KEY_DELETE);
                        if (isSecurityKeyboard()) {
                            sendCharToTarget(code, key);
                        }
                    } else {
                        this.mTapCount = 0;
                    }
                    code = key.codes[this.mTapCount];
                }
                if (isNumberUnLockKeyboard()) {
                    if (index != 9) {
                        this.mKeyboardActionListener.onKey(code, codes);
                        this.mKeyboardActionListener.onRelease(code);
                    }
                } else if (isSecurityKeyboard()) {
                    sendCharToTarget(code, key);
                    this.mKeyboardActionListener.onKey(code, codes);
                    this.mKeyboardActionListener.onRelease(code);
                } else {
                    this.mKeyboardActionListener.onKey(code, codes);
                    this.mKeyboardActionListener.onRelease(code);
                }
            }
            this.mLastSentIndex = index;
            this.mLastTapTime = eventTime;
        }
    }

    private CharSequence getPreviewText(Key key) {
        int i = 0;
        if (!this.mInMultiTap) {
            return adjustCase(key.label);
        }
        this.mPreviewLabel.setLength(0);
        StringBuilder stringBuilder = this.mPreviewLabel;
        int[] iArr = key.codes;
        if (this.mTapCount >= 0) {
            i = this.mTapCount;
        }
        stringBuilder.append((char) iArr[i]);
        return adjustCase(this.mPreviewLabel);
    }

    private void showPreview(int keyIndex) {
        int oldKeyIndex = this.mCurrentKeyIndex;
        PopupWindow previewPopup = this.mPreviewPopup;
        this.mCurrentKeyIndex = keyIndex;
        Key[] keys = this.mKeys;
        if (oldKeyIndex != this.mCurrentKeyIndex) {
            int keyCode;
            if (oldKeyIndex != -1 && keys.length > oldKeyIndex) {
                boolean z;
                Key oldKey = keys[oldKeyIndex];
                if (this.mCurrentKeyIndex == -1) {
                    z = true;
                } else {
                    z = false;
                }
                oldKey.onReleased(z);
                invalidateKey(oldKeyIndex);
                keyCode = oldKey.codes[0];
                sendAccessibilityEventForUnicodeCharacter(256, keyCode);
                sendAccessibilityEventForUnicodeCharacter(65536, keyCode);
            }
            if (this.mCurrentKeyIndex != -1 && keys.length > this.mCurrentKeyIndex) {
                Key newKey = keys[this.mCurrentKeyIndex];
                newKey.onPressed();
                invalidateKey(this.mCurrentKeyIndex);
                keyCode = newKey.codes[0];
                sendAccessibilityEventForUnicodeCharacter(128, keyCode);
                sendAccessibilityEventForUnicodeCharacter(32768, keyCode);
            }
        }
        if (oldKeyIndex != this.mCurrentKeyIndex && this.mShowPreview) {
            this.mHandler.removeMessages(1);
            if (previewPopup.isShowing() && keyIndex == -1) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), 70);
            }
            if (keyIndex == -1) {
                return;
            }
            if (previewPopup.isShowing() && this.mPreviewText.getVisibility() == 0) {
                showKey(keyIndex);
            } else {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, keyIndex, 0), 0);
            }
        }
    }

    private void showKey(int keyIndex) {
        PopupWindow previewPopup = this.mPreviewPopup;
        Key[] keys = this.mKeys;
        if (keyIndex >= 0 && keyIndex < this.mKeys.length) {
            Key key = keys[keyIndex];
            if (key.icon != null) {
                this.mPreviewText.setCompoundDrawables(null, null, null, key.iconPreview != null ? key.iconPreview : key.icon);
                this.mPreviewText.setText(null);
            } else {
                this.mPreviewText.setCompoundDrawables(null, null, null, null);
                this.mPreviewText.setText(getPreviewText(key));
                if (key.label.length() <= 1 || key.codes.length >= 2) {
                    this.mPreviewText.setTextSize(0, (float) this.mPreviewTextSizeLarge);
                    this.mPreviewText.setTypeface(Typeface.DEFAULT);
                } else {
                    this.mPreviewText.setTextSize(0, (float) this.mKeyTextSize);
                    this.mPreviewText.setTypeface(Typeface.DEFAULT_BOLD);
                }
            }
            this.mPreviewText.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
            int popupWidth = Math.max(this.mPreviewText.getMeasuredWidth(), (key.width + this.mPreviewText.getPaddingLeft()) + this.mPreviewText.getPaddingRight());
            int popupHeight = this.mPreviewHeight;
            LayoutParams lp = this.mPreviewText.getLayoutParams();
            if (lp != null) {
                lp.width = popupWidth;
                lp.height = popupHeight;
            }
            if (this.mPreviewCentered) {
                this.mPopupPreviewX = 160 - (this.mPreviewText.getMeasuredWidth() / 2);
                this.mPopupPreviewY = -this.mPreviewText.getMeasuredHeight();
            } else {
                this.mPopupPreviewX = (key.x - this.mPreviewText.getPaddingLeft()) + this.mPaddingLeft;
                this.mPopupPreviewY = (key.y - popupHeight) + this.mPreviewOffset;
            }
            this.mHandler.removeMessages(2);
            getLocationInWindow(this.mCoordinates);
            int[] iArr = this.mCoordinates;
            iArr[0] = iArr[0] + this.mMiniKeyboardOffsetX;
            iArr = this.mCoordinates;
            iArr[1] = iArr[1] + this.mMiniKeyboardOffsetY;
            this.mPreviewText.getBackground().setState(key.popupResId != 0 ? LONG_PRESSABLE_STATE_SET : EMPTY_STATE_SET);
            this.mPopupPreviewX += this.mCoordinates[0];
            this.mPopupPreviewY += this.mCoordinates[1];
            getLocationOnScreen(this.mCoordinates);
            if (this.mPopupPreviewY + this.mCoordinates[1] < 0) {
                if (key.x + key.width <= getWidth() / 2) {
                    this.mPopupPreviewX += (int) (((double) key.width) * 2.5d);
                } else {
                    this.mPopupPreviewX -= (int) (((double) key.width) * 2.5d);
                }
                this.mPopupPreviewY += popupHeight;
            }
            if (previewPopup.isShowing()) {
                previewPopup.update(this.mPopupPreviewX, this.mPopupPreviewY, popupWidth, popupHeight);
            } else {
                previewPopup.setWidth(popupWidth);
                previewPopup.setHeight(popupHeight);
                previewPopup.showAtLocation(this.mPopupParent, 0, this.mPopupPreviewX, this.mPopupPreviewY);
            }
            this.mPreviewText.setVisibility(0);
        }
    }

    private void sendAccessibilityEventForUnicodeCharacter(int eventType, int code) {
        boolean speakPassword = false;
        if (this.mAccessibilityManager.isEnabled()) {
            String text;
            AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
            onInitializeAccessibilityEvent(event);
            if (Secure.getIntForUser(this.mContext.getContentResolver(), Secure.ACCESSIBILITY_SPEAK_PASSWORD, 0, -3) != 0) {
                speakPassword = true;
            }
            if (speakPassword || this.mAudioManager.isBluetoothA2dpOn() || this.mAudioManager.isWiredHeadsetOn()) {
                switch (code) {
                    case -6:
                        text = this.mContext.getString(17040576);
                        break;
                    case -5:
                        text = this.mContext.getString(17040578);
                        break;
                    case -4:
                        text = this.mContext.getString(17040579);
                        break;
                    case -3:
                        text = this.mContext.getString(17040577);
                        break;
                    case -2:
                        text = this.mContext.getString(17040580);
                        break;
                    case -1:
                        text = this.mContext.getString(17040581);
                        break;
                    case 10:
                        text = this.mContext.getString(17040582);
                        break;
                    default:
                        text = String.valueOf((char) code);
                        break;
                }
            } else if (this.mHeadsetRequiredToHearPasswordsAnnounced) {
                text = this.mContext.getString(17040590);
            } else {
                if (eventType == 256) {
                    this.mHeadsetRequiredToHearPasswordsAnnounced = true;
                }
                text = this.mContext.getString(17040589);
            }
            event.getText().add(text);
            this.mAccessibilityManager.sendAccessibilityEvent(event);
        }
    }

    public void invalidateAllKeys() {
        this.mDirtyRect.union(0, 0, getWidth(), getHeight());
        this.mDrawPending = true;
        invalidate();
    }

    public void invalidateKey(int keyIndex) {
        if (this.mKeys != null && keyIndex >= 0 && keyIndex < this.mKeys.length) {
            Key key = this.mKeys[keyIndex];
            this.mInvalidatedKey = key;
            this.mDirtyRect.union(key.x + this.mPaddingLeft, key.y + this.mPaddingTop, (key.x + key.width) + this.mPaddingLeft, (key.y + key.height) + this.mPaddingTop);
            onBufferDraw();
            invalidate(key.x + this.mPaddingLeft, key.y + this.mPaddingTop, (key.x + key.width) + this.mPaddingLeft, (key.y + key.height) + this.mPaddingTop);
        }
    }

    private boolean openPopupIfRequired(MotionEvent me) {
        if (this.mPopupLayout == 0 || this.mCurrentKey < 0 || this.mCurrentKey >= this.mKeys.length) {
            return false;
        }
        boolean result = onLongPress(this.mKeys[this.mCurrentKey]);
        if (result) {
            this.mAbortKey = true;
            showPreview(-1);
        }
        return result;
    }

    protected boolean onLongPress(Key popupKey) {
        int popupKeyboardId = popupKey.popupResId;
        if (popupKeyboardId == 0) {
            return false;
        }
        int i;
        this.mMiniKeyboardContainer = (View) this.mMiniKeyboardCache.get(popupKey);
        if (this.mMiniKeyboardContainer == null) {
            Keyboard keyboard;
            this.mMiniKeyboardContainer = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(this.mPopupLayout, null);
            this.mMiniKeyboard = (KeyboardView) this.mMiniKeyboardContainer.findViewById(R.id.keyboardView);
            View closeButton = this.mMiniKeyboardContainer.findViewById(R.id.closeButton);
            if (closeButton != null) {
                closeButton.setOnClickListener(this);
            }
            this.mMiniKeyboard.setOnKeyboardActionListener(new AnonymousClass3(this));
            if (popupKey.popupCharacters != null) {
                keyboard = new Keyboard(getContext(), popupKeyboardId, popupKey.popupCharacters, -1, getPaddingRight() + getPaddingLeft());
            } else {
                keyboard = new Keyboard(getContext(), popupKeyboardId);
            }
            this.mMiniKeyboard.setKeyboard(keyboard);
            this.mMiniKeyboard.setPopupParent(this);
            this.mMiniKeyboardContainer.measure(MeasureSpec.makeMeasureSpec(getWidth(), Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(getHeight(), Integer.MIN_VALUE));
            this.mMiniKeyboardCache.put(popupKey, this.mMiniKeyboardContainer);
        } else {
            this.mMiniKeyboard = (KeyboardView) this.mMiniKeyboardContainer.findViewById(R.id.keyboardView);
        }
        getLocationInWindow(this.mCoordinates);
        this.mPopupX = popupKey.x + this.mPaddingLeft;
        this.mPopupY = popupKey.y + this.mPaddingTop;
        this.mPopupX = (this.mPopupX + popupKey.width) - this.mMiniKeyboardContainer.getMeasuredWidth();
        this.mPopupY -= this.mMiniKeyboardContainer.getMeasuredHeight();
        int x = (this.mPopupX + this.mMiniKeyboardContainer.getPaddingRight()) + this.mCoordinates[0];
        int y = (this.mPopupY + this.mMiniKeyboardContainer.getPaddingBottom()) + this.mCoordinates[1];
        KeyboardView keyboardView = this.mMiniKeyboard;
        if (x < 0) {
            i = 0;
        } else {
            i = x;
        }
        keyboardView.setPopupOffset(i, y);
        this.mMiniKeyboard.setShifted(isShifted());
        this.mPopupKeyboard.setContentView(this.mMiniKeyboardContainer);
        this.mPopupKeyboard.setWidth(this.mMiniKeyboardContainer.getMeasuredWidth());
        this.mPopupKeyboard.setHeight(this.mMiniKeyboardContainer.getMeasuredHeight());
        this.mPopupKeyboard.showAtLocation(this, 0, x, y);
        this.mMiniKeyboardOnScreen = true;
        invalidateAllKeys();
        return true;
    }

    public boolean onHoverEvent(MotionEvent event) {
        if (!this.mAccessibilityManager.isTouchExplorationEnabled() || event.getPointerCount() != 1) {
            return true;
        }
        switch (event.getAction()) {
            case 7:
                event.setAction(2);
                break;
            case 9:
                event.setAction(0);
                break;
            case 10:
                event.setAction(1);
                break;
        }
        return onTouchEvent(event);
    }

    public boolean onTouchEvent(MotionEvent me) {
        boolean result;
        int pointerCount = me.getPointerCount();
        int action = me.getAction();
        long now = me.getEventTime();
        if (pointerCount != this.mOldPointerCount) {
            if (pointerCount == 1) {
                MotionEvent down = MotionEvent.obtain(now, now, 0, me.getX(), me.getY(), me.getMetaState());
                result = onModifiedTouchEvent(down, false);
                down.recycle();
                if (action == 1) {
                    result = onModifiedTouchEvent(me, true);
                }
            } else {
                MotionEvent up = MotionEvent.obtain(now, now, 1, this.mOldPointerX, this.mOldPointerY, me.getMetaState());
                result = onModifiedTouchEvent(up, true);
                up.recycle();
            }
        } else if (pointerCount == 1) {
            result = onModifiedTouchEvent(me, false);
            this.mOldPointerX = me.getX();
            this.mOldPointerY = me.getY();
        } else {
            result = true;
        }
        this.mOldPointerCount = pointerCount;
        return result;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for delete the c code", property = OppoRomType.ROM)
    private boolean onModifiedTouchEvent(MotionEvent me, boolean possiblePoly) {
        int touchX = ((int) me.getX()) - this.mPaddingLeft;
        int touchY = ((int) me.getY()) - this.mPaddingTop;
        if (touchY >= (-this.mVerticalCorrection)) {
            touchY += this.mVerticalCorrection;
        }
        int action = me.getAction();
        long eventTime = me.getEventTime();
        int keyIndex = getKeyIndices(touchX, touchY, null);
        if (isNumberUnLockKeyboard() && !isKeyboardViewEnabled()) {
            return false;
        }
        if (!isUnLockKeyboard() || isKeyboardViewEnabled() || keyIndex == this.mKeys.length - 1) {
            this.mPossiblePoly = possiblePoly;
            if (action == 0) {
                this.mSwipeTracker.clear();
            }
            this.mSwipeTracker.addMovement(me);
            if (this.mAbortKey && action != 0 && action != 3) {
                return true;
            }
            if (this.mGestureDetector.onTouchEvent(me)) {
                showPreview(-1);
                if (isSecurityKeyboard()) {
                    dismissPopupWindow();
                }
                this.mHandler.removeMessages(3);
                this.mHandler.removeMessages(4);
                return true;
            } else if (this.mMiniKeyboardOnScreen && action != 3) {
                return true;
            } else {
                switch (action) {
                    case 0:
                        this.mAbortKey = false;
                        this.mStartX = touchX;
                        this.mStartY = touchY;
                        this.mLastCodeX = touchX;
                        this.mLastCodeY = touchY;
                        this.mLastKeyTime = 0;
                        this.mCurrentKeyTime = 0;
                        this.mLastKey = -1;
                        this.mCurrentKey = keyIndex;
                        this.mDownKey = keyIndex;
                        this.mDownTime = me.getEventTime();
                        this.mLastMoveTime = this.mDownTime;
                        checkMultiTap(eventTime, keyIndex);
                        if (!isNumberUnLockKeyboard()) {
                            this.mKeyboardActionListener.onPress(keyIndex != -1 ? this.mKeys[keyIndex].codes[0] : 0);
                        } else if (keyIndex != 9) {
                            this.mKeyboardActionListener.onPress(keyIndex != -1 ? this.mKeys[keyIndex].codes[0] : 0);
                        }
                        this.mHandler.removeMessages(5);
                        if (isSecurityKeyboard() && this.mCurrentKey != -1) {
                            setPopWindowShow();
                            if (this.mPreviousKey == -1) {
                                showPopUpWindow(this.mCurrentKey);
                            }
                            this.mPreviousKey = this.mCurrentKey;
                        }
                        if (isUnLockKeyboard() && this.mCurrentKey == this.mKeys.length - 1) {
                            this.mIsDownFlag = true;
                        }
                        if (this.mCurrentKey >= 0 && this.mKeys[this.mCurrentKey].repeatable) {
                            this.mRepeatKeyIndex = this.mCurrentKey;
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), 400);
                            repeatKey();
                            if (this.mAbortKey) {
                                this.mRepeatKeyIndex = -1;
                                break;
                            }
                        }
                        if (this.mCurrentKey != -1) {
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4, me), (long) LONGPRESS_TIMEOUT);
                        }
                        showPreview(keyIndex);
                        break;
                    case 1:
                        removeMessages();
                        if (keyIndex == this.mCurrentKey) {
                            this.mCurrentKeyTime += eventTime - this.mLastMoveTime;
                        } else {
                            resetMultiTap();
                            this.mLastKey = this.mCurrentKey;
                            this.mLastKeyTime = (this.mCurrentKeyTime + eventTime) - this.mLastMoveTime;
                            this.mCurrentKey = keyIndex;
                            this.mCurrentKeyTime = 0;
                        }
                        if (this.mCurrentKeyTime < this.mLastKeyTime && this.mCurrentKeyTime < 70 && this.mLastKey != -1) {
                            this.mCurrentKey = this.mLastKey;
                            touchX = this.mLastCodeX;
                            touchY = this.mLastCodeY;
                        }
                        showPreview(-1);
                        Arrays.fill(this.mKeyIndices, -1);
                        if (!(this.mRepeatKeyIndex != -1 || this.mMiniKeyboardOnScreen || this.mAbortKey)) {
                            detectAndSendKey(this.mCurrentKey, touchX, touchY, eventTime);
                        }
                        invalidateKey(keyIndex);
                        this.mRepeatKeyIndex = -1;
                        if (isUnLockKeyboard() && this.mCurrentKey == this.mKeys.length - 1) {
                            this.mIsDownFlag = false;
                        }
                        if (isSecurityKeyboard()) {
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5), 120);
                            break;
                        }
                        break;
                    case 2:
                        boolean continueLongPress = false;
                        if (keyIndex != -1) {
                            if (this.mCurrentKey == -1) {
                                this.mCurrentKey = keyIndex;
                                this.mCurrentKeyTime = eventTime - this.mDownTime;
                            } else if (keyIndex == this.mCurrentKey) {
                                this.mCurrentKeyTime += eventTime - this.mLastMoveTime;
                                continueLongPress = true;
                            } else if (this.mRepeatKeyIndex == -1) {
                                resetMultiTap();
                                this.mLastKey = this.mCurrentKey;
                                this.mLastCodeX = this.mLastX;
                                this.mLastCodeY = this.mLastY;
                                this.mLastKeyTime = (this.mCurrentKeyTime + eventTime) - this.mLastMoveTime;
                                this.mCurrentKey = keyIndex;
                                this.mCurrentKeyTime = 0;
                            }
                        }
                        if (!continueLongPress) {
                            this.mHandler.removeMessages(4);
                            if (keyIndex != -1) {
                                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4, me), (long) LONGPRESS_TIMEOUT);
                            }
                        }
                        this.mHandler.removeMessages(5);
                        if (isSecurityKeyboard() && this.mCurrentKey != -1) {
                            setPopWindowShow();
                            this.mPreviousKey = this.mCurrentKey;
                        }
                        showPreview(this.mCurrentKey);
                        this.mLastMoveTime = eventTime;
                        break;
                    case 3:
                        removeMessages();
                        dismissPopupKeyboard();
                        this.mAbortKey = true;
                        showPreview(-1);
                        invalidateKey(this.mCurrentKey);
                        if (isSecurityKeyboard()) {
                            dismissPopupWindow();
                            break;
                        }
                        break;
                }
                this.mLastX = touchX;
                this.mLastY = touchY;
                return true;
            }
        }
        if (this.mIsDownFlag && this.mPreviousKey != -1 && this.mPreviousKey == this.mKeys.length - 1) {
            if (this.mKeys[this.mPreviousKey].pressed) {
                this.mKeys[this.mPreviousKey].onReleased(this.mCurrentKeyIndex == -1);
                this.mCurrentKeyIndex = -1;
                this.mIsDownFlag = false;
            }
            invalidateKey(this.mPreviousKey);
        }
        return false;
    }

    private boolean repeatKey() {
        Key key = this.mKeys[this.mRepeatKeyIndex];
        detectAndSendKey(this.mCurrentKey, key.x, key.y, this.mLastTapTime);
        return true;
    }

    protected void swipeRight() {
        this.mKeyboardActionListener.swipeRight();
    }

    protected void swipeLeft() {
        this.mKeyboardActionListener.swipeLeft();
    }

    protected void swipeUp() {
        this.mKeyboardActionListener.swipeUp();
    }

    protected void swipeDown() {
        this.mKeyboardActionListener.swipeDown();
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for the security popupwindow", property = OppoRomType.ROM)
    public void closing() {
        if (this.mPreviewPopup.isShowing()) {
            this.mPreviewPopup.dismiss();
        }
        if (isSecurityKeyboard()) {
            dismissPopupWindow();
        }
        this.mPreviousKey = -1;
        removeMessages();
        dismissPopupKeyboard();
        this.mBuffer = null;
        this.mCanvas = null;
        this.mMiniKeyboardCache.clear();
    }

    private void removeMessages() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(3);
            this.mHandler.removeMessages(4);
            this.mHandler.removeMessages(1);
        }
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        closing();
    }

    private void dismissPopupKeyboard() {
        if (this.mPopupKeyboard.isShowing()) {
            this.mPopupKeyboard.dismiss();
            this.mMiniKeyboardOnScreen = false;
            invalidateAllKeys();
        }
    }

    public boolean handleBack() {
        if (!this.mPopupKeyboard.isShowing()) {
            return false;
        }
        dismissPopupKeyboard();
        return true;
    }

    private void resetMultiTap() {
        this.mLastSentIndex = -1;
        this.mTapCount = 0;
        this.mLastTapTime = -1;
        this.mInMultiTap = false;
    }

    private void checkMultiTap(long eventTime, int keyIndex) {
        if (keyIndex != -1) {
            Key key = this.mKeys[keyIndex];
            if (key.codes.length > 1) {
                this.mInMultiTap = true;
                if (eventTime >= this.mLastTapTime + 800 || keyIndex != this.mLastSentIndex) {
                    this.mTapCount = -1;
                    return;
                } else {
                    this.mTapCount = (this.mTapCount + 1) % key.codes.length;
                    return;
                }
            }
            if (eventTime > this.mLastTapTime + 800 || keyIndex != this.mLastSentIndex) {
                resetMultiTap();
            }
        }
    }

    private void setPopWindowShow() {
        if (this.mCurrentKey == this.mPreviousKey && (this.mColorPopup == null || !this.mColorPopup.isShowing())) {
            showPopUpWindow(this.mCurrentKey);
        }
        if (this.mCurrentKey != this.mPreviousKey) {
            showPopUpWindow(this.mCurrentKey);
        }
    }

    private void showPopUpWindow(int keyIndex) {
        int codes = this.mKeys[keyIndex].codes[0];
        CharSequence label = this.mKeys[keyIndex].label;
        int[] coordinates = new int[2];
        getLocationInWindow(coordinates);
        if (label != null && codes != -1 && codes != -5 && codes != -2 && codes != 10 && codes != 32) {
            if (this.mKeyPreview != null) {
                this.mKeyPreview.setVisibility(0);
                this.mKeyPreview.setText(getPreviewText(this.mKeys[keyIndex]));
            }
            int popupWindowLocalx = (this.mKeys[keyIndex].x + (this.mKeys[keyIndex].width / 2)) - (this.mColorPopupWidth / 2);
            int popupWindowLocaly = (this.mKeys[keyIndex].y - this.mColorPopupHeight) + coordinates[1];
            if (this.mColorPopup != null && this.mColorPopup.isShowing()) {
                this.mColorPopup.update(popupWindowLocalx, popupWindowLocaly, this.mColorPopupWidth, this.mColorPopupHeight);
            } else if (this.mColorPopup != null) {
                this.mColorPopup.showAtLocation(this, 0, popupWindowLocalx, popupWindowLocaly);
            }
        } else if (codes == -1 || codes == -5 || codes == -2 || codes == 10 || codes == 32) {
            dismissPopupWindow();
        }
    }

    public void dismissPopupWindow() {
        if (this.mColorPopup != null && this.mColorPopup.isShowing()) {
            this.mColorPopup.dismiss();
        }
    }

    private void sendCharToTarget(int code, Key key) {
        String label = null;
        if (this.mKeyboardCharListener != null && code != -1 && code != -2) {
            if (code == 10) {
                this.mKeyboardCharListener.onCharacter(null, 2);
            } else if (this.mKeyBoardType == 1 && code == 32) {
                this.mKeyboardCharListener.onCharacter(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER, 0);
            } else if (this.mKeyBoardType == 1 && code != 10 && code != -5) {
                if (key.label != null) {
                    label = adjustCase(key.label).toString();
                }
                if (label != null) {
                    this.mKeyboardCharListener.onCharacter(label, 0);
                }
            } else if (this.mKeyBoardType == 1 && code == -5) {
                this.mKeyboardCharListener.onCharacter(null, 1);
            }
        }
    }

    private boolean isNumberUnLockKeyboard() {
        if (!ColorContextUtil.isOppoStyle(getContext()) || this.mKeyBoardType == 1) {
            return false;
        }
        return this.mKeyBoardType != 2;
    }

    private boolean isSecurityKeyboard() {
        if (this.mKeyBoardType == 1) {
            return true;
        }
        if (this.mKeyBoardType == 2) {
            return ColorContextUtil.isOppoStyle(getContext());
        }
        return false;
    }

    private boolean isUnLockKeyboard() {
        return this.mKeyBoardType == 2 ? ColorContextUtil.isOppoStyle(getContext()) : false;
    }

    public void setKeyboardViewEnabled(boolean enabled) {
        this.mIsEnable = enabled;
    }

    public boolean isKeyboardViewEnabled() {
        return this.mIsEnable;
    }

    public void setOnKeyboardCharListener(OnKeyboardCharListener listener) {
        this.mKeyboardCharListener = listener;
    }

    public void updateOkKey(CharSequence text) {
        CharSequence temp = getResources().getText(17040492);
        if (text == null || this.mKeyBoardType != 1) {
            if (text != null && isUnLockKeyboard()) {
                this.mUpdateText = text;
            }
        } else if (temp.toString().equals(text.toString())) {
            this.mUpdateText = null;
        } else {
            this.mUpdateText = text;
        }
        if (this.mKeys != null && this.mKeys.length > 0) {
            invalidateKey(this.mKeys.length - 1);
        }
    }

    public void setKeyboardType(int keyboardType) {
        WindowManager wm;
        int height;
        this.mKeyBoardType = keyboardType;
        if (isSecurityKeyboard()) {
            LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.mPaddingTop = 0;
            this.mColorPopupWidth = getResources().getDimensionPixelOffset(201655540);
            this.mColorPopupHeight = getResources().getDimensionPixelOffset(201655541);
            this.mLowerLetterSize = getResources().getDimensionPixelOffset(201655544);
            this.mSpaceLabelSize = getResources().getDimensionPixelOffset(201655545);
            this.mEndLabelSize = getResources().getDimensionPixelOffset(201655546);
            try {
                this.mTypeface = Typeface.createFromFile("/system/fonts/Roboto-Regular.ttf");
            } catch (RuntimeException e) {
                this.mTypeface = Typeface.createFromFile(Typeface.LIGHT_PATH);
            } catch (Exception e2) {
                this.mTypeface = Typeface.DEFAULT;
            }
            this.mKeyPreview = (TextView) inflate.inflate(201917584, null);
            this.mKeyPreview.setTypeface(this.mTypeface);
            this.mColorPopup = new PopupWindow(getContext());
            this.mColorPopup.setWidth(this.mColorPopupWidth);
            this.mColorPopup.setHeight(this.mColorPopupHeight);
            this.mColorPopup.setContentView(this.mKeyPreview);
            this.mColorPopup.setBackgroundDrawable(null);
            this.mColorPopup.setTouchable(false);
        }
        if (this.mKeyBoardType == 1) {
            wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            this.mColorSecurityBgWidth = wm.getDefaultDisplay().getWidth();
            height = wm.getDefaultDisplay().getHeight();
            this.mColorSecurityBgHeight = (int) (((float) height) * getResources().getFraction(201655539, 1, 1));
            this.mColorSecurityOffset1 = (int) (((float) height) * getResources().getFraction(201655547, 1, 1));
            this.mColorSecurityOffset2 = (int) (((float) height) * getResources().getFraction(201655548, 1, 1));
            this.mColorTextColor = getResources().getColorStateList(201720897);
            this.mColorGoTextColor = getContext().getColor(201720901);
            this.mColorSecurityBg = getResources().getColor(201720898);
            this.mColorSecurityKeyBg = getResources().getDrawable(201852180);
            this.mColorSpecialKeyBg = getResources().getDrawable(201852181);
            this.mColorEndKeyBg = getResources().getDrawable(201852188);
        }
        if (isUnLockKeyboard()) {
            wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            if (getContext().getResources().getConfiguration().orientation == 2) {
                this.mColorSecurityBgWidth = wm.getDefaultDisplay().getHeight();
                height = wm.getDefaultDisplay().getWidth();
            } else {
                this.mColorSecurityBgWidth = wm.getDefaultDisplay().getWidth();
                height = wm.getDefaultDisplay().getHeight();
            }
            if (this.mColorSecurityBgWidth > height) {
                int temp = this.mColorSecurityBgWidth;
                this.mColorSecurityBgWidth = height;
                height = temp;
            }
            this.mColorSecurityBgHeight = (int) (((float) height) * getResources().getFraction(201655549, 1, 1));
            this.mColorSecurityOffset1 = (int) (((float) height) * getResources().getFraction(201655550, 1, 1));
            this.mColorSecurityOffset2 = (int) (((float) height) * getResources().getFraction(201655551, 1, 1));
            this.mColorPopupBg = getResources().getDrawable(201852182);
            this.mColorTextColor = getResources().getColorStateList(201720903);
            this.mColorGoTextColor = getContext().getColor(201720903);
            this.mColorSecurityBg = 0;
            this.mColorSecurityKeyBg = getResources().getDrawable(201852192);
            this.mColorSpecialKeyBg = getResources().getDrawable(201852192);
            this.mColorEndKeyBg = getResources().getDrawable(201852196);
            this.mColorLockDeleteKey = getResources().getDrawable(201852195);
            this.mColorLockSpaceKey = getResources().getDrawable(201852199);
            this.mColorLockShiftKey = getResources().getDrawable(201852193);
            if (this.mKeyPreview != null) {
                this.mKeyPreview.setBackgroundDrawable(this.mColorPopupBg);
                this.mKeyPreview.setTextColor(this.mColorTextColor);
            }
        }
    }
}
