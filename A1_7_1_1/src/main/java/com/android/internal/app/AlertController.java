package com.android.internal.app;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.android.internal.R;
import com.color.util.ColorContextUtil;
import com.oppo.util.OppoDialogUtil;
import java.lang.ref.WeakReference;

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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class AlertController {
    public static final int MICRO = 1;
    private ListAdapter mAdapter;
    private int mAlertDialogLayout;
    private final OnClickListener mButtonHandler;
    private Button mButtonNegative;
    private Message mButtonNegativeMessage;
    private CharSequence mButtonNegativeText;
    private Button mButtonNeutral;
    private Message mButtonNeutralMessage;
    private CharSequence mButtonNeutralText;
    private int mButtonPanelLayoutHint;
    private int mButtonPanelSideLayout;
    private Button mButtonPositive;
    private Message mButtonPositiveMessage;
    private CharSequence mButtonPositiveText;
    private int mCheckedItem;
    private final Context mContext;
    private View mCustomTitleView;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Shuai.Zhang@Plf.SDK, 2016-04-28 : Add for a new style AlertDialog", property = OppoRomType.ROM)
    private int mDeleteDialogOption;
    private final DialogInterface mDialogInterface;
    private boolean mForceInverseBackground;
    private Handler mHandler;
    private Drawable mIcon;
    private int mIconId;
    private ImageView mIconView;
    private int mListItemLayout;
    private int mListLayout;
    protected ListView mListView;
    protected CharSequence mMessage;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Suying.You@Plf.SDK : Add for the message is scroll", property = OppoRomType.ROM)
    public boolean mMessageScroll;
    protected TextView mMessageView;
    private int mMultiChoiceItemLayout;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Suying.You@Plf.SDK : Add for the button of the delete dialog is scroll", property = OppoRomType.ROM)
    public boolean mScroll;
    protected ScrollView mScrollView;
    private boolean mShowTitle;
    private int mSingleChoiceItemLayout;
    private CharSequence mTitle;
    private TextView mTitleView;
    private View mView;
    private int mViewLayoutResId;
    private int mViewSpacingBottom;
    private int mViewSpacingLeft;
    private int mViewSpacingRight;
    private boolean mViewSpacingSpecified;
    private int mViewSpacingTop;
    protected final Window mWindow;

    public static class AlertParams {
        public ListAdapter mAdapter;
        public boolean mCancelable;
        public int mCheckedItem;
        public boolean[] mCheckedItems;
        public final Context mContext;
        public Cursor mCursor;
        public View mCustomTitleView;
        public boolean mForceInverseBackground;
        @OppoHook(level = OppoHookType.NEW_FIELD, note = "Yujun.Feng@Plf.SDK, 2017-02-20 : Add for 3.1 dialog", property = OppoRomType.ROM)
        public boolean mHasMessage;
        public Drawable mIcon;
        public int mIconAttrId;
        public int mIconId;
        public final LayoutInflater mInflater;
        public String mIsCheckedColumn;
        public boolean mIsMultiChoice;
        @OppoHook(level = OppoHookType.NEW_FIELD, note = "Suying.You@Plf.SDK, 2016-08-20 : Add for 3.1 dialog", property = OppoRomType.ROM)
        public boolean mIsScroll;
        public boolean mIsSingleChoice;
        @OppoHook(level = OppoHookType.NEW_FIELD, note = "Suying.You@Plf.SDK, 2016-05-31 : Add for 3.1 dialog", property = OppoRomType.ROM)
        public boolean mIsTitle;
        public CharSequence[] mItems;
        public String mLabelColumn;
        public CharSequence mMessage;
        @OppoHook(level = OppoHookType.NEW_FIELD, note = "Suying.You@Plf.SDK, 2017-01-23 : Add for 3.1 dialog", property = OppoRomType.ROM)
        public boolean mMessageIsScroll;
        public DialogInterface.OnClickListener mNegativeButtonListener;
        public CharSequence mNegativeButtonText;
        public DialogInterface.OnClickListener mNeutralButtonListener;
        public CharSequence mNeutralButtonText;
        public OnCancelListener mOnCancelListener;
        public OnMultiChoiceClickListener mOnCheckboxClickListener;
        public DialogInterface.OnClickListener mOnClickListener;
        public OnDismissListener mOnDismissListener;
        public OnItemSelectedListener mOnItemSelectedListener;
        public OnKeyListener mOnKeyListener;
        public OnPrepareListViewListener mOnPrepareListViewListener;
        public DialogInterface.OnClickListener mPositiveButtonListener;
        public CharSequence mPositiveButtonText;
        public boolean mRecycleOnMeasure;
        @OppoHook(level = OppoHookType.NEW_FIELD, note = "Suying.You@Plf.SDK, 2016-08-20 : Add for 3.1 dialog", property = OppoRomType.ROM)
        public CharSequence[] mSummaryItems;
        public CharSequence mTitle;
        public View mView;
        public int mViewLayoutResId;
        public int mViewSpacingBottom;
        public int mViewSpacingLeft;
        public int mViewSpacingRight;
        public boolean mViewSpacingSpecified;
        public int mViewSpacingTop;
        @OppoHook(level = OppoHookType.NEW_FIELD, note = "Suying.You@Plf.SDK, 2016-05-31 : Add for 3.1 dialog", property = OppoRomType.ROM)
        public int[] textColor;

        /* renamed from: com.android.internal.app.AlertController$AlertParams$1 */
        class AnonymousClass1 extends ArrayAdapter<CharSequence> {
            final /* synthetic */ AlertParams this$1;
            final /* synthetic */ RecycleListView val$listView;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.AlertController.AlertParams.1.<init>(com.android.internal.app.AlertController$AlertParams, android.content.Context, int, int, java.lang.CharSequence[], com.android.internal.app.AlertController$RecycleListView):void, dex: 
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
            AnonymousClass1(com.android.internal.app.AlertController.AlertParams r1, android.content.Context r2, int r3, int r4, java.lang.CharSequence[] r5, com.android.internal.app.AlertController.RecycleListView r6) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.AlertController.AlertParams.1.<init>(com.android.internal.app.AlertController$AlertParams, android.content.Context, int, int, java.lang.CharSequence[], com.android.internal.app.AlertController$RecycleListView):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.AlertController.AlertParams.1.<init>(com.android.internal.app.AlertController$AlertParams, android.content.Context, int, int, java.lang.CharSequence[], com.android.internal.app.AlertController$RecycleListView):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.AlertController.AlertParams.1.getView(int, android.view.View, android.view.ViewGroup):android.view.View, dex: 
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
            public android.view.View getView(int r1, android.view.View r2, android.view.ViewGroup r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.AlertController.AlertParams.1.getView(int, android.view.View, android.view.ViewGroup):android.view.View, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.AlertController.AlertParams.1.getView(int, android.view.View, android.view.ViewGroup):android.view.View");
            }
        }

        /* renamed from: com.android.internal.app.AlertController$AlertParams$2 */
        class AnonymousClass2 extends CursorAdapter {
            private final int mIsCheckedIndex;
            private final int mLabelIndex;
            final /* synthetic */ AlertParams this$1;
            final /* synthetic */ AlertController val$dialog;
            final /* synthetic */ RecycleListView val$listView;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.AlertController.AlertParams.2.<init>(com.android.internal.app.AlertController$AlertParams, android.content.Context, android.database.Cursor, boolean, com.android.internal.app.AlertController$RecycleListView, com.android.internal.app.AlertController):void, dex: 
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
            AnonymousClass2(com.android.internal.app.AlertController.AlertParams r1, android.content.Context r2, android.database.Cursor r3, boolean r4, com.android.internal.app.AlertController.RecycleListView r5, com.android.internal.app.AlertController r6) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.AlertController.AlertParams.2.<init>(com.android.internal.app.AlertController$AlertParams, android.content.Context, android.database.Cursor, boolean, com.android.internal.app.AlertController$RecycleListView, com.android.internal.app.AlertController):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.AlertController.AlertParams.2.<init>(com.android.internal.app.AlertController$AlertParams, android.content.Context, android.database.Cursor, boolean, com.android.internal.app.AlertController$RecycleListView, com.android.internal.app.AlertController):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.AlertController.AlertParams.2.bindView(android.view.View, android.content.Context, android.database.Cursor):void, dex: 
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
            public void bindView(android.view.View r1, android.content.Context r2, android.database.Cursor r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.AlertController.AlertParams.2.bindView(android.view.View, android.content.Context, android.database.Cursor):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.AlertController.AlertParams.2.bindView(android.view.View, android.content.Context, android.database.Cursor):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.AlertController.AlertParams.2.newView(android.content.Context, android.database.Cursor, android.view.ViewGroup):android.view.View, dex: 
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
            public android.view.View newView(android.content.Context r1, android.database.Cursor r2, android.view.ViewGroup r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.AlertController.AlertParams.2.newView(android.content.Context, android.database.Cursor, android.view.ViewGroup):android.view.View, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.AlertController.AlertParams.2.newView(android.content.Context, android.database.Cursor, android.view.ViewGroup):android.view.View");
            }
        }

        /* renamed from: com.android.internal.app.AlertController$AlertParams$3 */
        class AnonymousClass3 implements OnItemClickListener {
            final /* synthetic */ AlertParams this$1;
            final /* synthetic */ AlertController val$dialog;

            AnonymousClass3(AlertParams this$1, AlertController val$dialog) {
                this.this$1 = this$1;
                this.val$dialog = val$dialog;
            }

            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                this.this$1.mOnClickListener.onClick(this.val$dialog.mDialogInterface, position);
                if (!this.this$1.mIsSingleChoice) {
                    this.val$dialog.mDialogInterface.dismiss();
                }
            }
        }

        /* renamed from: com.android.internal.app.AlertController$AlertParams$4 */
        class AnonymousClass4 implements OnItemClickListener {
            final /* synthetic */ AlertParams this$1;
            final /* synthetic */ AlertController val$dialog;
            final /* synthetic */ RecycleListView val$listView;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.AlertController.AlertParams.4.<init>(com.android.internal.app.AlertController$AlertParams, com.android.internal.app.AlertController$RecycleListView, com.android.internal.app.AlertController):void, dex: 
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
            AnonymousClass4(com.android.internal.app.AlertController.AlertParams r1, com.android.internal.app.AlertController.RecycleListView r2, com.android.internal.app.AlertController r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.AlertController.AlertParams.4.<init>(com.android.internal.app.AlertController$AlertParams, com.android.internal.app.AlertController$RecycleListView, com.android.internal.app.AlertController):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.AlertController.AlertParams.4.<init>(com.android.internal.app.AlertController$AlertParams, com.android.internal.app.AlertController$RecycleListView, com.android.internal.app.AlertController):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.AlertController.AlertParams.4.onItemClick(android.widget.AdapterView, android.view.View, int, long):void, dex: 
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
            public void onItemClick(android.widget.AdapterView<?> r1, android.view.View r2, int r3, long r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.app.AlertController.AlertParams.4.onItemClick(android.widget.AdapterView, android.view.View, int, long):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.AlertController.AlertParams.4.onItemClick(android.widget.AdapterView, android.view.View, int, long):void");
            }
        }

        public interface OnPrepareListViewListener {
            void onPrepareListView(ListView listView);
        }

        public AlertParams(Context context) {
            this.mIconId = 0;
            this.mIconAttrId = 0;
            this.mViewSpacingSpecified = false;
            this.mCheckedItem = -1;
            this.mRecycleOnMeasure = true;
            this.mIsTitle = false;
            this.mHasMessage = false;
            this.mContext = context;
            this.mCancelable = true;
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        }

        public void apply(AlertController dialog) {
            if (this.mCustomTitleView != null) {
                dialog.setCustomTitle(this.mCustomTitleView);
            } else {
                if (this.mTitle != null) {
                    dialog.setTitle(this.mTitle);
                }
                if (this.mIcon != null) {
                    dialog.setIcon(this.mIcon);
                }
                if (this.mIconId != 0) {
                    dialog.setIcon(this.mIconId);
                }
                if (this.mIconAttrId != 0) {
                    dialog.setIcon(dialog.getIconAttributeResId(this.mIconAttrId));
                }
            }
            if (this.mMessage != null) {
                dialog.setMessage(this.mMessage);
            }
            if (this.mPositiveButtonText != null) {
                dialog.setButton(-1, this.mPositiveButtonText, this.mPositiveButtonListener, null);
            }
            if (this.mNegativeButtonText != null) {
                dialog.setButton(-2, this.mNegativeButtonText, this.mNegativeButtonListener, null);
            }
            if (this.mNeutralButtonText != null) {
                dialog.setButton(-3, this.mNeutralButtonText, this.mNeutralButtonListener, null);
            }
            if (this.mForceInverseBackground) {
                dialog.setInverseBackgroundForced(true);
            }
            if (!(this.mItems == null && this.mCursor == null && this.mAdapter == null)) {
                createListView(dialog);
            }
            if (this.mView != null) {
                if (this.mViewSpacingSpecified) {
                    dialog.setView(this.mView, this.mViewSpacingLeft, this.mViewSpacingTop, this.mViewSpacingRight, this.mViewSpacingBottom);
                    return;
                }
                dialog.setView(this.mView);
            } else if (this.mViewLayoutResId != 0) {
                dialog.setView(this.mViewLayoutResId);
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
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        @android.annotation.OppoHook(level = android.annotation.OppoHook.OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK, 2016-05-31 : Modify for 3.1 dialog", property = android.annotation.OppoHook.OppoRomType.ROM)
        private void createListView(com.android.internal.app.AlertController r19) {
            /*
            r18 = this;
            r0 = r18;
            r2 = r0.mContext;
            r3 = com.color.util.ColorContextUtil.isOppoStyle(r2);
            r0 = r18;
            r2 = r0.mContext;
            r6 = r19.mListLayout;
            r0 = r19;
            r2 = com.android.internal.app.ColorInjector.AlertController.initListLayout(r0, r3, r2, r6);
            r0 = r19;
            r0.mListLayout = r2;
            r0 = r18;
            r2 = r0.mMessage;
            if (r2 == 0) goto L_0x00bd;
        L_0x0021:
            r2 = 1;
        L_0x0022:
            r0 = r18;
            r0.mHasMessage = r2;
            r0 = r18;
            r2 = r0.mInflater;
            r6 = r19.mListLayout;
            r7 = 0;
            r4 = r2.inflate(r6, r7);
            r4 = (com.android.internal.app.AlertController.RecycleListView) r4;
            r0 = r18;
            r5 = r0.mTitle;
            r0 = r18;
            r6 = r0.mIsScroll;
            r0 = r18;
            r7 = r0.mMessageIsScroll;
            r2 = r19;
            r2 = com.android.internal.app.ColorInjector.AlertController.initRecycleListView(r2, r3, r4, r5, r6, r7);
            r0 = r18;
            r0.mIsTitle = r2;
            r0 = r18;
            r2 = r0.mIsMultiChoice;
            if (r2 == 0) goto L_0x00d4;
        L_0x0051:
            r0 = r18;
            r2 = r0.mCursor;
            if (r2 != 0) goto L_0x00c0;
        L_0x0057:
            r5 = new com.android.internal.app.AlertController$AlertParams$1;
            r0 = r18;
            r7 = r0.mContext;
            r8 = r19.mMultiChoiceItemLayout;
            r0 = r18;
            r10 = r0.mItems;
            r9 = 16908308; // 0x1020014 float:2.3877285E-38 double:8.353814E-317;
            r6 = r18;
            r11 = r4;
            r5.<init>(r6, r7, r8, r9, r10, r11);
        L_0x006e:
            r0 = r18;
            r2 = r0.mOnPrepareListViewListener;
            if (r2 == 0) goto L_0x007b;
        L_0x0074:
            r0 = r18;
            r2 = r0.mOnPrepareListViewListener;
            r2.onPrepareListView(r4);
        L_0x007b:
            r0 = r19;
            r0.mAdapter = r5;
            r0 = r18;
            r2 = r0.mCheckedItem;
            r0 = r19;
            r0.mCheckedItem = r2;
            r0 = r18;
            r2 = r0.mOnClickListener;
            if (r2 == 0) goto L_0x0164;
        L_0x008f:
            r2 = new com.android.internal.app.AlertController$AlertParams$3;
            r0 = r18;
            r1 = r19;
            r2.<init>(r0, r1);
            r4.setOnItemClickListener(r2);
        L_0x009b:
            r0 = r18;
            r2 = r0.mOnItemSelectedListener;
            if (r2 == 0) goto L_0x00a8;
        L_0x00a1:
            r0 = r18;
            r2 = r0.mOnItemSelectedListener;
            r4.setOnItemSelectedListener(r2);
        L_0x00a8:
            r0 = r18;
            r2 = r0.mIsSingleChoice;
            if (r2 == 0) goto L_0x0178;
        L_0x00ae:
            r2 = 1;
            r4.setChoiceMode(r2);
        L_0x00b2:
            r0 = r18;
            r2 = r0.mRecycleOnMeasure;
            r4.mRecycleOnMeasure = r2;
            r0 = r19;
            r0.mListView = r4;
            return;
        L_0x00bd:
            r2 = 0;
            goto L_0x0022;
        L_0x00c0:
            r5 = new com.android.internal.app.AlertController$AlertParams$2;
            r0 = r18;
            r7 = r0.mContext;
            r0 = r18;
            r8 = r0.mCursor;
            r9 = 0;
            r6 = r18;
            r10 = r4;
            r11 = r19;
            r5.<init>(r6, r7, r8, r9, r10, r11);
            goto L_0x006e;
        L_0x00d4:
            r0 = r18;
            r2 = r0.mIsSingleChoice;
            if (r2 == 0) goto L_0x0118;
        L_0x00da:
            r14 = r19.mSingleChoiceItemLayout;
        L_0x00de:
            r17 = com.android.internal.app.ColorInjector.AlertController.isDialogThree(r19);
            r2 = com.android.internal.app.ColorInjector.AlertController.isCustomAdapter(r3);
            if (r2 == 0) goto L_0x011d;
        L_0x00e8:
            if (r17 == 0) goto L_0x011d;
        L_0x00ea:
            r0 = r18;
            r7 = r0.mLabelColumn;
            r0 = r18;
            r8 = r0.mCursor;
            r0 = r18;
            r9 = r0.mContext;
            r0 = r18;
            r10 = r0.mItems;
            r0 = r18;
            r11 = r0.mSummaryItems;
            r0 = r18;
            r12 = r0.mIsTitle;
            r0 = r18;
            r13 = r0.textColor;
            r0 = r18;
            r15 = r0.mAdapter;
            r0 = r18;
            r0 = r0.mHasMessage;
            r16 = r0;
            r6 = r19;
            r5 = com.android.internal.app.ColorInjector.AlertController.initCheckedItemAdapter(r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16);
            goto L_0x006e;
        L_0x0118:
            r14 = r19.mListItemLayout;
            goto L_0x00de;
        L_0x011d:
            r0 = r18;
            r2 = r0.mCursor;
            if (r2 == 0) goto L_0x0146;
        L_0x0123:
            r5 = new android.widget.SimpleCursorAdapter;
            r0 = r18;
            r6 = r0.mContext;
            r0 = r18;
            r8 = r0.mCursor;
            r2 = 1;
            r9 = new java.lang.String[r2];
            r0 = r18;
            r2 = r0.mLabelColumn;
            r7 = 0;
            r9[r7] = r2;
            r2 = 1;
            r10 = new int[r2];
            r2 = 16908308; // 0x1020014 float:2.3877285E-38 double:8.353814E-317;
            r7 = 0;
            r10[r7] = r2;
            r7 = r14;
            r5.<init>(r6, r7, r8, r9, r10);
            goto L_0x006e;
        L_0x0146:
            r0 = r18;
            r2 = r0.mAdapter;
            if (r2 == 0) goto L_0x0152;
        L_0x014c:
            r0 = r18;
            r5 = r0.mAdapter;
            goto L_0x006e;
        L_0x0152:
            r5 = new com.android.internal.app.AlertController$CheckedItemAdapter;
            r0 = r18;
            r2 = r0.mContext;
            r0 = r18;
            r6 = r0.mItems;
            r7 = 16908308; // 0x1020014 float:2.3877285E-38 double:8.353814E-317;
            r5.<init>(r2, r14, r7, r6);
            goto L_0x006e;
        L_0x0164:
            r0 = r18;
            r2 = r0.mOnCheckboxClickListener;
            if (r2 == 0) goto L_0x009b;
        L_0x016a:
            r2 = new com.android.internal.app.AlertController$AlertParams$4;
            r0 = r18;
            r1 = r19;
            r2.<init>(r0, r4, r1);
            r4.setOnItemClickListener(r2);
            goto L_0x009b;
        L_0x0178:
            r0 = r18;
            r2 = r0.mIsMultiChoice;
            if (r2 == 0) goto L_0x00b2;
        L_0x017e:
            r2 = 2;
            r4.setChoiceMode(r2);
            goto L_0x00b2;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.AlertController.AlertParams.createListView(com.android.internal.app.AlertController):void");
        }
    }

    private static final class ButtonHandler extends Handler {
        private static final int MSG_DISMISS_DIALOG = 1;
        private WeakReference<DialogInterface> mDialog;

        public ButtonHandler(DialogInterface dialog) {
            this.mDialog = new WeakReference(dialog);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -3:
                case -2:
                case -1:
                    ((DialogInterface.OnClickListener) msg.obj).onClick((DialogInterface) this.mDialog.get(), msg.what);
                    return;
                case 1:
                    ((DialogInterface) msg.obj).dismiss();
                    return;
                default:
                    return;
            }
        }
    }

    private static class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
        public CheckedItemAdapter(Context context, int resource, int textViewResourceId, CharSequence[] objects) {
            super(context, resource, textViewResourceId, (Object[]) objects);
        }

        public boolean hasStableIds() {
            return true;
        }

        public long getItemId(int position) {
            return (long) position;
        }
    }

    public static class RecycleListView extends ListView {
        private final int mPaddingBottomNoButtons;
        private final int mPaddingTopNoTitle;
        boolean mRecycleOnMeasure;

        public RecycleListView(Context context) {
            this(context, null);
        }

        public RecycleListView(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.mRecycleOnMeasure = true;
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RecycleListView);
            this.mPaddingBottomNoButtons = ta.getDimensionPixelOffset(0, -1);
            this.mPaddingTopNoTitle = ta.getDimensionPixelOffset(1, -1);
        }

        public void setHasDecor(boolean hasTitle, boolean hasButtons) {
            if (!hasButtons || !hasTitle) {
                setPadding(getPaddingLeft(), hasTitle ? getPaddingTop() : this.mPaddingTopNoTitle, getPaddingRight(), hasButtons ? getPaddingBottom() : this.mPaddingBottomNoButtons);
            }
        }

        protected boolean recycleOnMeasure() {
            return this.mRecycleOnMeasure;
        }
    }

    private static boolean shouldCenterSingleButton(Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.alertDialogCenterButtons, outValue, true);
        if (outValue.data != 0) {
            return true;
        }
        return false;
    }

    public static final AlertController create(Context context, DialogInterface di, Window window) {
        TypedArray a = context.obtainStyledAttributes(null, R.styleable.AlertDialog, R.attr.alertDialogStyle, 0);
        int controllerType = a.getInt(20, 0);
        a.recycle();
        switch (controllerType) {
            case 1:
                return new MicroAlertController(context, di, window);
            default:
                return new AlertController(context, di, window);
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHua.Lin@Plf.SDK : protected -> public", property = OppoRomType.ROM)
    public AlertController(Context context, DialogInterface di, Window window) {
        this.mViewSpacingSpecified = false;
        this.mIconId = 0;
        this.mCheckedItem = -1;
        this.mButtonPanelLayoutHint = 0;
        this.mButtonHandler = new OnClickListener() {
            public void onClick(View v) {
                Message m;
                if (v == AlertController.this.mButtonPositive && AlertController.this.mButtonPositiveMessage != null) {
                    m = Message.obtain(AlertController.this.mButtonPositiveMessage);
                } else if (v == AlertController.this.mButtonNegative && AlertController.this.mButtonNegativeMessage != null) {
                    m = Message.obtain(AlertController.this.mButtonNegativeMessage);
                } else if (v != AlertController.this.mButtonNeutral || AlertController.this.mButtonNeutralMessage == null) {
                    m = null;
                } else {
                    m = Message.obtain(AlertController.this.mButtonNeutralMessage);
                }
                if (m != null) {
                    m.sendToTarget();
                }
                AlertController.this.mHandler.obtainMessage(1, AlertController.this.mDialogInterface).sendToTarget();
            }
        };
        this.mDeleteDialogOption = 0;
        this.mScroll = false;
        this.mMessageScroll = true;
        this.mContext = context;
        this.mDialogInterface = di;
        this.mWindow = window;
        this.mHandler = new ButtonHandler(di);
        TypedArray a = context.obtainStyledAttributes(null, R.styleable.AlertDialog, R.attr.alertDialogStyle, 0);
        this.mAlertDialogLayout = a.getResourceId(10, R.layout.alert_dialog);
        this.mButtonPanelSideLayout = a.getResourceId(11, 0);
        this.mListLayout = a.getResourceId(12, R.layout.select_dialog);
        this.mMultiChoiceItemLayout = a.getResourceId(13, R.layout.select_dialog_multichoice);
        this.mSingleChoiceItemLayout = a.getResourceId(14, R.layout.select_dialog_singlechoice);
        this.mListItemLayout = a.getResourceId(15, R.layout.select_dialog_item);
        this.mShowTitle = a.getBoolean(18, true);
        a.recycle();
        window.requestFeature(1);
    }

    static boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }
        if (!(v instanceof ViewGroup)) {
            return false;
        }
        ViewGroup vg = (ViewGroup) v;
        int i = vg.getChildCount();
        while (i > 0) {
            i--;
            if (canTextInput(vg.getChildAt(i))) {
                return true;
            }
        }
        return false;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHua.Lin@Plf.SDK : Modify for Change the display position of dialog; Shuai.Zhang@Plf.SDK, 2015-05-19 : Modify for ColorOS 3.0 Dialog", property = OppoRomType.ROM)
    public void installContent() {
        this.mWindow.setContentView(selectContentView());
        AlertController.updateWindow(this, this.mContext, this.mWindow);
        setupView();
        AlertController.setDialogListener(this, this.mContext, this.mWindow, this.mDeleteDialogOption);
    }

    private int selectContentView() {
        if (this.mButtonPanelSideLayout == 0) {
            return this.mAlertDialogLayout;
        }
        if (this.mButtonPanelLayoutHint == 1) {
            return this.mButtonPanelSideLayout;
        }
        return this.mAlertDialogLayout;
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
        if (this.mTitleView != null) {
            this.mTitleView.setText(title);
        }
    }

    public void setCustomTitle(View customTitleView) {
        this.mCustomTitleView = customTitleView;
    }

    public void setMessage(CharSequence message) {
        this.mMessage = message;
        if (this.mMessageView != null) {
            this.mMessageView.setText(message);
        }
    }

    public void setView(int layoutResId) {
        this.mView = null;
        this.mViewLayoutResId = layoutResId;
        this.mViewSpacingSpecified = false;
    }

    public void setView(View view) {
        this.mView = view;
        this.mViewLayoutResId = 0;
        this.mViewSpacingSpecified = false;
    }

    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
        this.mView = view;
        this.mViewLayoutResId = 0;
        this.mViewSpacingSpecified = true;
        this.mViewSpacingLeft = viewSpacingLeft;
        this.mViewSpacingTop = viewSpacingTop;
        this.mViewSpacingRight = viewSpacingRight;
        this.mViewSpacingBottom = viewSpacingBottom;
    }

    public void setButtonPanelLayoutHint(int layoutHint) {
        this.mButtonPanelLayoutHint = layoutHint;
    }

    public void setButton(int whichButton, CharSequence text, DialogInterface.OnClickListener listener, Message msg) {
        if (msg == null && listener != null) {
            msg = this.mHandler.obtainMessage(whichButton, listener);
        }
        switch (whichButton) {
            case -3:
                this.mButtonNeutralText = text;
                this.mButtonNeutralMessage = msg;
                return;
            case -2:
                this.mButtonNegativeText = text;
                this.mButtonNegativeMessage = msg;
                return;
            case -1:
                this.mButtonPositiveText = text;
                this.mButtonPositiveMessage = msg;
                return;
            default:
                throw new IllegalArgumentException("Button does not exist");
        }
    }

    public void setIcon(int resId) {
        this.mIcon = null;
        this.mIconId = resId;
        if (this.mIconView == null) {
            return;
        }
        if (resId != 0) {
            this.mIconView.setVisibility(0);
            this.mIconView.setImageResource(this.mIconId);
            return;
        }
        this.mIconView.setVisibility(8);
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
        this.mIconId = 0;
        if (this.mIconView == null) {
            return;
        }
        if (icon != null) {
            this.mIconView.setVisibility(0);
            this.mIconView.setImageDrawable(icon);
            return;
        }
        this.mIconView.setVisibility(8);
    }

    public int getIconAttributeResId(int attrId) {
        TypedValue out = new TypedValue();
        this.mContext.getTheme().resolveAttribute(attrId, out, true);
        return out.resourceId;
    }

    public void setInverseBackgroundForced(boolean forceInverseBackground) {
        this.mForceInverseBackground = forceInverseBackground;
    }

    public ListView getListView() {
        return this.mListView;
    }

    public Button getButton(int whichButton) {
        switch (whichButton) {
            case -3:
                return this.mButtonNeutral;
            case -2:
                return this.mButtonNegative;
            case -1:
                return this.mButtonPositive;
            default:
                return null;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return this.mScrollView != null ? this.mScrollView.executeKeyEvent(event) : false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return this.mScrollView != null ? this.mScrollView.executeKeyEvent(event) : false;
    }

    private ViewGroup resolvePanel(View customPanel, View defaultPanel) {
        if (customPanel == null) {
            if (defaultPanel instanceof ViewStub) {
                defaultPanel = ((ViewStub) defaultPanel).inflate();
            }
            return (ViewGroup) defaultPanel;
        }
        if (defaultPanel != null) {
            ViewParent parent = defaultPanel.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(defaultPanel);
            }
        }
        if (customPanel instanceof ViewStub) {
            customPanel = ((ViewStub) customPanel).inflate();
        }
        return (ViewGroup) customPanel;
    }

    private void setupView() {
        View spacer;
        View parentPanel = this.mWindow.findViewById(R.id.parentPanel);
        View defaultTopPanel = parentPanel.findViewById(R.id.topPanel);
        View defaultContentPanel = parentPanel.findViewById(R.id.contentPanel);
        View defaultButtonPanel = parentPanel.findViewById(R.id.buttonPanel);
        ViewGroup customPanel = (ViewGroup) parentPanel.findViewById(R.id.customPanel);
        setupCustomContent(customPanel);
        View customTopPanel = customPanel.findViewById(R.id.topPanel);
        View customContentPanel = customPanel.findViewById(R.id.contentPanel);
        View customButtonPanel = customPanel.findViewById(R.id.buttonPanel);
        ViewGroup topPanel = resolvePanel(customTopPanel, defaultTopPanel);
        ViewGroup contentPanel = resolvePanel(customContentPanel, defaultContentPanel);
        ViewGroup buttonPanel = resolvePanel(customButtonPanel, defaultButtonPanel);
        setupContent(contentPanel);
        setupButtons(buttonPanel);
        setupTitle(topPanel);
        boolean hasCustomPanel = customPanel != null ? customPanel.getVisibility() != 8 : false;
        boolean hasTopPanel = topPanel != null ? topPanel.getVisibility() != 8 : false;
        boolean hasButtonPanel = buttonPanel != null ? buttonPanel.getVisibility() != 8 : false;
        if (!hasButtonPanel) {
            if (contentPanel != null) {
                spacer = contentPanel.findViewById(R.id.textSpacerNoButtons);
                if (spacer != null) {
                    spacer.setVisibility(0);
                }
            }
            this.mWindow.setCloseOnTouchOutsideIfNotSet(true);
        }
        if (hasTopPanel) {
            if (this.mScrollView != null) {
                this.mScrollView.setClipToPadding(true);
            }
            View divider = null;
            if (this.mMessage == null && this.mListView == null && !hasCustomPanel) {
                divider = topPanel.findViewById(R.id.titleDividerTop);
            } else {
                if (!hasCustomPanel) {
                    divider = topPanel.findViewById(R.id.titleDividerNoCustom);
                }
                if (divider == null) {
                    divider = topPanel.findViewById(R.id.titleDivider);
                }
            }
            if (divider != null) {
                divider.setVisibility(0);
            }
        } else if (contentPanel != null) {
            spacer = contentPanel.findViewById(R.id.textSpacerNoTitle);
            if (spacer != null) {
                spacer.setVisibility(0);
            }
        }
        if (this.mListView instanceof RecycleListView) {
            ((RecycleListView) this.mListView).setHasDecor(hasTopPanel, hasButtonPanel);
        }
        if (!hasCustomPanel) {
            View content = this.mListView != null ? this.mListView : this.mScrollView;
            if (content != null) {
                content.setScrollIndicators((hasTopPanel ? 1 : 0) | (hasButtonPanel ? 2 : 0), 3);
            }
        }
        TypedArray a = this.mContext.obtainStyledAttributes(null, R.styleable.AlertDialog, R.attr.alertDialogStyle, 0);
        setBackground(a, topPanel, contentPanel, customPanel, buttonPanel, hasTopPanel, hasCustomPanel, hasButtonPanel);
        a.recycle();
    }

    private void setupCustomContent(ViewGroup customPanel) {
        View customView;
        boolean hasCustomView = false;
        if (this.mView != null) {
            customView = this.mView;
        } else if (this.mViewLayoutResId != 0) {
            customView = LayoutInflater.from(this.mContext).inflate(this.mViewLayoutResId, customPanel, false);
        } else {
            customView = null;
        }
        if (customView != null) {
            hasCustomView = true;
        }
        if (!(hasCustomView && canTextInput(customView))) {
            this.mWindow.setFlags(131072, 131072);
        }
        if (hasCustomView) {
            FrameLayout custom = (FrameLayout) this.mWindow.findViewById(R.id.custom);
            custom.addView(customView, new LayoutParams(-1, -1));
            if (this.mViewSpacingSpecified) {
                custom.setPadding(this.mViewSpacingLeft, this.mViewSpacingTop, this.mViewSpacingRight, this.mViewSpacingBottom);
            }
            if (this.mListView != null) {
                ((LinearLayout.LayoutParams) customPanel.getLayoutParams()).weight = 0.0f;
                return;
            }
            return;
        }
        customPanel.setVisibility(8);
    }

    protected void setupTitle(ViewGroup topPanel) {
        boolean hasTextTitle = false;
        if (this.mCustomTitleView == null || !this.mShowTitle) {
            this.mIconView = (ImageView) this.mWindow.findViewById(R.id.icon);
            if (!TextUtils.isEmpty(this.mTitle)) {
                hasTextTitle = true;
            }
            if (hasTextTitle && this.mShowTitle) {
                this.mTitleView = (TextView) this.mWindow.findViewById(R.id.alertTitle);
                this.mTitleView.setText(this.mTitle);
                if (this.mIconId != 0) {
                    this.mIconView.setImageResource(this.mIconId);
                    return;
                } else if (this.mIcon != null) {
                    this.mIconView.setImageDrawable(this.mIcon);
                    return;
                } else {
                    this.mTitleView.setPadding(this.mIconView.getPaddingLeft(), this.mIconView.getPaddingTop(), this.mIconView.getPaddingRight(), this.mIconView.getPaddingBottom());
                    this.mIconView.setVisibility(8);
                    return;
                }
            }
            this.mWindow.findViewById(R.id.title_template).setVisibility(8);
            this.mIconView.setVisibility(8);
            topPanel.setVisibility(8);
            return;
        }
        topPanel.addView(this.mCustomTitleView, 0, new LayoutParams(-1, -2));
        this.mWindow.findViewById(R.id.title_template).setVisibility(8);
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for when the dialog is DELETE_ALERT_DIALOG_THREE, and has the message", property = OppoRomType.ROM)
    protected void setupContent(ViewGroup contentPanel) {
        this.mScrollView = (ScrollView) contentPanel.findViewById(R.id.scrollView);
        this.mScrollView.setFocusable(false);
        this.mMessageView = (TextView) contentPanel.findViewById(R.id.message);
        if (this.mMessageView != null) {
            if (ColorContextUtil.isOppoStyle(this.mContext) && this.mDeleteDialogOption == 3) {
                AlertController.addListView(this.mListView, this.mMessage, this.mMessageView, this.mScrollView, (ImageView) contentPanel.findViewById(201458936), (LinearLayout) contentPanel.findViewById(201458934), contentPanel, this.mScroll, this.mMessageScroll);
            } else if (this.mMessage != null) {
                this.mMessageView.setText(this.mMessage);
            } else {
                this.mMessageView.setVisibility(8);
                this.mScrollView.removeView(this.mMessageView);
                if (this.mListView != null) {
                    ViewGroup scrollParent = (ViewGroup) this.mScrollView.getParent();
                    int childIndex = scrollParent.indexOfChild(this.mScrollView);
                    scrollParent.removeViewAt(childIndex);
                    scrollParent.addView(this.mListView, childIndex, new LayoutParams(-1, -1));
                } else {
                    contentPanel.setVisibility(8);
                }
            }
        }
    }

    private static void manageScrollIndicators(View v, View upIndicator, View downIndicator) {
        int i = 0;
        if (upIndicator != null) {
            int i2;
            if (v.canScrollVertically(-1)) {
                i2 = 0;
            } else {
                i2 = 4;
            }
            upIndicator.setVisibility(i2);
        }
        if (downIndicator != null) {
            if (!v.canScrollVertically(1)) {
                i = 4;
            }
            downIndicator.setVisibility(i);
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Shuai.Zhang@Plf.SDK, 2015-05-19 : Modify for ColorOS 3.0 Dialog", property = OppoRomType.ROM)
    protected void setupButtons(ViewGroup buttonPanel) {
        int whichButtons = 0;
        this.mButtonPositive = (Button) buttonPanel.findViewById(R.id.button1);
        this.mButtonPositive.setOnClickListener(this.mButtonHandler);
        if (TextUtils.isEmpty(this.mButtonPositiveText)) {
            this.mButtonPositive.setVisibility(8);
        } else {
            this.mButtonPositive.setText(this.mButtonPositiveText);
            this.mButtonPositive.setVisibility(0);
            whichButtons = 1;
        }
        this.mButtonNegative = (Button) buttonPanel.findViewById(R.id.button2);
        this.mButtonNegative.setOnClickListener(this.mButtonHandler);
        if (TextUtils.isEmpty(this.mButtonNegativeText)) {
            this.mButtonNegative.setVisibility(8);
        } else {
            this.mButtonNegative.setText(this.mButtonNegativeText);
            this.mButtonNegative.setVisibility(0);
            whichButtons |= 2;
        }
        this.mButtonNeutral = (Button) buttonPanel.findViewById(R.id.button3);
        this.mButtonNeutral.setOnClickListener(this.mButtonHandler);
        if (TextUtils.isEmpty(this.mButtonNeutralText)) {
            this.mButtonNeutral.setVisibility(8);
        } else {
            this.mButtonNeutral.setText(this.mButtonNeutralText);
            this.mButtonNeutral.setVisibility(0);
            whichButtons |= 4;
        }
        ColorStateList textFousedColor = null;
        boolean shouldCenterSingleButton = shouldCenterSingleButton(this.mContext);
        boolean isOppoStyle = ColorContextUtil.isOppoStyle(this.mContext);
        if (AlertController.isCustomButtons(isOppoStyle)) {
            textFousedColor = AlertController.setupButtons(this.mContext, shouldCenterSingleButton, this.mDeleteDialogOption, whichButtons, this.mButtonPositive, this.mButtonNegative, this.mButtonNeutral);
        } else if (shouldCenterSingleButton) {
            if (whichButtons == 1) {
                centerButton(this.mButtonPositive);
            } else if (whichButtons == 2) {
                centerButton(this.mButtonNegative);
            } else if (whichButtons == 4) {
                centerButton(this.mButtonNeutral);
            }
        }
        if (isOppoStyle) {
            AlertController.changeButtonArrangeStyles(this.mContext, buttonPanel, this.mDeleteDialogOption, whichButtons, this.mButtonPositive, this.mButtonNegative, this.mButtonNeutral, this.mButtonPositiveText, this.mButtonNegativeText, this.mButtonNeutralText);
        }
        if (!(whichButtons != 0)) {
            buttonPanel.setVisibility(8);
        }
        AlertController.updateButtonsBackground(isOppoStyle, textFousedColor, this.mDeleteDialogOption, whichButtons, this.mButtonPositive, this.mButtonNegative, this.mButtonNeutral);
    }

    private void centerButton(Button button) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.gravity = 1;
        params.weight = 0.5f;
        button.setLayoutParams(params);
        View leftSpacer = this.mWindow.findViewById(R.id.leftSpacer);
        if (leftSpacer != null) {
            leftSpacer.setVisibility(0);
        }
        View rightSpacer = this.mWindow.findViewById(R.id.rightSpacer);
        if (rightSpacer != null) {
            rightSpacer.setVisibility(0);
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Shuai.Zhang@Plf.SDK, 2016-04-28 : Modify for a new style AlertDialog; Changwei.Li@Plf.SDK, 2016-05-25 : Modify for delete dailog two", property = OppoRomType.ROM)
    private void setBackground(TypedArray a, View topPanel, View contentPanel, View customPanel, View buttonPanel, boolean hasTitle, boolean hasCustomView, boolean hasButtons) {
        View view;
        int i;
        int fullDark = 0;
        int topDark = 0;
        int centerDark = 0;
        int bottomDark = 0;
        int fullBright = 0;
        int topBright = 0;
        int centerBright = 0;
        int bottomBright = 0;
        int bottomMedium = 0;
        if (a.getBoolean(19, true)) {
            fullDark = R.drawable.popup_full_dark;
            topDark = R.drawable.popup_top_dark;
            centerDark = R.drawable.popup_center_dark;
            bottomDark = R.drawable.popup_bottom_dark;
            fullBright = R.drawable.popup_full_bright;
            topBright = R.drawable.popup_top_bright;
            centerBright = R.drawable.popup_center_bright;
            bottomBright = R.drawable.popup_bottom_bright;
            bottomMedium = R.drawable.popup_bottom_medium;
        }
        topBright = a.getResourceId(5, topBright);
        topDark = a.getResourceId(1, topDark);
        centerBright = a.getResourceId(6, centerBright);
        centerDark = a.getResourceId(2, centerDark);
        View[] views = new View[4];
        boolean[] light = new boolean[4];
        View lastView = null;
        boolean lastLight = false;
        int pos = 0;
        if (hasTitle) {
            views[0] = topPanel;
            light[0] = false;
            pos = 1;
        }
        if (contentPanel.getVisibility() == 8) {
            view = null;
        } else {
            view = contentPanel;
        }
        views[pos] = view;
        light[pos] = this.mListView != null;
        pos++;
        if (hasCustomView) {
            views[pos] = customPanel;
            light[pos] = this.mForceInverseBackground;
            pos++;
        }
        if (hasButtons) {
            views[pos] = buttonPanel;
            light[pos] = true;
        }
        boolean setView = false;
        for (pos = 0; pos < views.length; pos++) {
            View v = views[pos];
            if (v != null) {
                if (lastView != null) {
                    if (setView) {
                        lastView.setBackgroundResource(lastLight ? centerBright : centerDark);
                    } else {
                        if (lastLight) {
                            i = topBright;
                        } else {
                            i = topDark;
                        }
                        lastView.setBackgroundResource(i);
                    }
                    setView = true;
                }
                lastView = v;
                lastLight = light[pos];
            }
        }
        if (lastView != null) {
            if (setView) {
                i = lastLight ? hasButtons ? AlertController.updateBottomMediumDrawable(this.mDeleteDialogOption, a.getResourceId(8, bottomMedium)) : a.getResourceId(7, bottomBright) : a.getResourceId(3, bottomDark);
                lastView.setBackgroundResource(i);
            } else {
                lastView.setBackgroundResource(lastLight ? a.getResourceId(4, fullBright) : a.getResourceId(0, fullDark));
            }
        }
        ListView listView = this.mListView;
        if (!(listView == null || this.mAdapter == null)) {
            listView.setAdapter(this.mAdapter);
            int checkedItem = this.mCheckedItem;
            if (checkedItem > -1) {
                listView.setItemChecked(checkedItem, true);
                listView.setSelection(checkedItem);
            }
        }
        AlertController.updatePanelsBackground(this.mDeleteDialogOption, buttonPanel, topPanel, contentPanel, customPanel, this.mButtonPositive, this.mButtonNeutral);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Shuai.Zhang@Plf.SDK, 2016-04-28 : Add for a new style AlertDialog", property = OppoRomType.ROM)
    public void setDeleteDialogOption(int delete) {
        this.mDeleteDialogOption = delete;
        OppoDialogUtil.setDeleteDialogOption(delete);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Shuai.Zhang@Plf.SDK, 2016-04-28 : Add for a new style AlertDialog", property = OppoRomType.ROM)
    public AlertController(Context context, DialogInterface di, Window window, int deleteDialogOption) {
        this(context, di, window);
        setDeleteDialogOption(deleteDialogOption);
        this.mAlertDialogLayout = AlertController.initLayout(deleteDialogOption, this.mAlertDialogLayout);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Changwei.Li@Plf.SDK, 2016-05-25 : Add for delete dialog type three", property = OppoRomType.ROM)
    public int getDeleteDialogOption() {
        return this.mDeleteDialogOption;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "JianHua.Lin@Plf.SDK : Add for Change the display position of dialog", property = OppoRomType.ROM)
    public void initializeDialog() {
        this.mWindow.setContentView(this.mAlertDialogLayout);
        setupView();
        new OppoDialogUtil(this.mContext).setDialogButtonFlag(this.mWindow, 8, this.mDeleteDialogOption);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Suying.You@Plf.SDK, 2016-08-18 : Add for it sets whether the button is bold", property = OppoRomType.ROM)
    public void setButtonIsBold(int positive, int negative, int neutral) {
        AlertController.setButtonBold(positive, negative, neutral, this.mButtonPositive, this.mButtonNegative, this.mButtonNeutral, this.mContext);
    }
}
