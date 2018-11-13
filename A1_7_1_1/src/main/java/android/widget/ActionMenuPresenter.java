package android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.ActionProvider;
import android.view.ActionProvider.SubUiVisibilityListener;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ActionMenuView.ActionMenuChildView;
import com.android.internal.R;
import com.android.internal.view.ActionBarPolicy;
import com.android.internal.view.menu.ActionMenuItemView;
import com.android.internal.view.menu.ActionMenuItemView.PopupCallback;
import com.android.internal.view.menu.BaseMenuPresenter;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuPopupHelper;
import com.android.internal.view.menu.MenuPresenter.Callback;
import com.android.internal.view.menu.MenuView;
import com.android.internal.view.menu.MenuView.ItemView;
import com.android.internal.view.menu.ShowableListMenu;
import com.android.internal.view.menu.SubMenuBuilder;
import java.util.ArrayList;
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
public class ActionMenuPresenter extends BaseMenuPresenter implements SubUiVisibilityListener {
    private static final boolean ACTIONBAR_ANIMATIONS_ENABLED = false;
    private static final int ITEM_ANIMATION_DURATION = 150;
    private final SparseBooleanArray mActionButtonGroups;
    private ActionButtonSubmenu mActionButtonPopup;
    private int mActionItemWidthLimit;
    private OnAttachStateChangeListener mAttachStateChangeListener;
    private boolean mExpandedActionViewsExclusive;
    private OnPreDrawListener mItemAnimationPreDrawListener;
    private int mMaxItems;
    private boolean mMaxItemsSet;
    private int mMinCellSize;
    int mOpenSubMenuId;
    private OverflowMenuButton mOverflowButton;
    private OverflowPopup mOverflowPopup;
    private Drawable mPendingOverflowIcon;
    private boolean mPendingOverflowIconSet;
    private ActionMenuPopupCallback mPopupCallback;
    final PopupPresenterCallback mPopupPresenterCallback;
    private SparseArray<MenuItemLayoutInfo> mPostLayoutItems;
    private OpenOverflowRunnable mPostedOpenRunnable;
    private SparseArray<MenuItemLayoutInfo> mPreLayoutItems;
    private boolean mReserveOverflow;
    private boolean mReserveOverflowSet;
    private List<ItemAnimationInfo> mRunningItemAnimations;
    private boolean mStrictWidthLimit;
    private int mWidthLimit;
    private boolean mWidthLimitSet;

    /* renamed from: android.widget.ActionMenuPresenter$3 */
    class AnonymousClass3 extends AnimatorListenerAdapter {
        final /* synthetic */ ActionMenuPresenter this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.ActionMenuPresenter.3.<init>(android.widget.ActionMenuPresenter):void, dex: 
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
        AnonymousClass3(android.widget.ActionMenuPresenter r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.ActionMenuPresenter.3.<init>(android.widget.ActionMenuPresenter):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ActionMenuPresenter.3.<init>(android.widget.ActionMenuPresenter):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.ActionMenuPresenter.3.onAnimationEnd(android.animation.Animator):void, dex: 
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
        public void onAnimationEnd(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.ActionMenuPresenter.3.onAnimationEnd(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ActionMenuPresenter.3.onAnimationEnd(android.animation.Animator):void");
        }
    }

    /* renamed from: android.widget.ActionMenuPresenter$4 */
    class AnonymousClass4 extends AnimatorListenerAdapter {
        final /* synthetic */ ActionMenuPresenter this$0;
        final /* synthetic */ MenuItemLayoutInfo val$menuItemLayoutInfoPre;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.ActionMenuPresenter.4.<init>(android.widget.ActionMenuPresenter, android.widget.ActionMenuPresenter$MenuItemLayoutInfo):void, dex: 
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
        AnonymousClass4(android.widget.ActionMenuPresenter r1, android.widget.ActionMenuPresenter.MenuItemLayoutInfo r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.ActionMenuPresenter.4.<init>(android.widget.ActionMenuPresenter, android.widget.ActionMenuPresenter$MenuItemLayoutInfo):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ActionMenuPresenter.4.<init>(android.widget.ActionMenuPresenter, android.widget.ActionMenuPresenter$MenuItemLayoutInfo):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.ActionMenuPresenter.4.onAnimationEnd(android.animation.Animator):void, dex: 
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
        public void onAnimationEnd(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.ActionMenuPresenter.4.onAnimationEnd(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ActionMenuPresenter.4.onAnimationEnd(android.animation.Animator):void");
        }
    }

    /* renamed from: android.widget.ActionMenuPresenter$5 */
    class AnonymousClass5 extends AnimatorListenerAdapter {
        final /* synthetic */ ActionMenuPresenter this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.ActionMenuPresenter.5.<init>(android.widget.ActionMenuPresenter):void, dex: 
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
        AnonymousClass5(android.widget.ActionMenuPresenter r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.ActionMenuPresenter.5.<init>(android.widget.ActionMenuPresenter):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ActionMenuPresenter.5.<init>(android.widget.ActionMenuPresenter):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.ActionMenuPresenter.5.onAnimationEnd(android.animation.Animator):void, dex: 
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
        public void onAnimationEnd(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.ActionMenuPresenter.5.onAnimationEnd(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ActionMenuPresenter.5.onAnimationEnd(android.animation.Animator):void");
        }
    }

    private class ActionButtonSubmenu extends MenuPopupHelper {
        final /* synthetic */ ActionMenuPresenter this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.ActionMenuPresenter.ActionButtonSubmenu.<init>(android.widget.ActionMenuPresenter, android.content.Context, com.android.internal.view.menu.SubMenuBuilder, android.view.View):void, dex: 
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
        public ActionButtonSubmenu(android.widget.ActionMenuPresenter r1, android.content.Context r2, com.android.internal.view.menu.SubMenuBuilder r3, android.view.View r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.ActionMenuPresenter.ActionButtonSubmenu.<init>(android.widget.ActionMenuPresenter, android.content.Context, com.android.internal.view.menu.SubMenuBuilder, android.view.View):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ActionMenuPresenter.ActionButtonSubmenu.<init>(android.widget.ActionMenuPresenter, android.content.Context, com.android.internal.view.menu.SubMenuBuilder, android.view.View):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.ActionMenuPresenter.ActionButtonSubmenu.onDismiss():void, dex: 
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
        protected void onDismiss() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.ActionMenuPresenter.ActionButtonSubmenu.onDismiss():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ActionMenuPresenter.ActionButtonSubmenu.onDismiss():void");
        }
    }

    private class ActionMenuPopupCallback extends PopupCallback {
        final /* synthetic */ ActionMenuPresenter this$0;

        /* synthetic */ ActionMenuPopupCallback(ActionMenuPresenter this$0, ActionMenuPopupCallback actionMenuPopupCallback) {
            this(this$0);
        }

        private ActionMenuPopupCallback(ActionMenuPresenter this$0) {
            this.this$0 = this$0;
        }

        public ShowableListMenu getPopup() {
            return this.this$0.mActionButtonPopup != null ? this.this$0.mActionButtonPopup.getPopup() : null;
        }
    }

    private static class ItemAnimationInfo {
        static final int FADE_IN = 1;
        static final int FADE_OUT = 2;
        static final int MOVE = 0;
        int animType;
        Animator animator;
        int id;
        MenuItemLayoutInfo menuItemLayoutInfo;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.widget.ActionMenuPresenter.ItemAnimationInfo.<init>(int, android.widget.ActionMenuPresenter$MenuItemLayoutInfo, android.animation.Animator, int):void, dex: 
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
        ItemAnimationInfo(int r1, android.widget.ActionMenuPresenter.MenuItemLayoutInfo r2, android.animation.Animator r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.widget.ActionMenuPresenter.ItemAnimationInfo.<init>(int, android.widget.ActionMenuPresenter$MenuItemLayoutInfo, android.animation.Animator, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ActionMenuPresenter.ItemAnimationInfo.<init>(int, android.widget.ActionMenuPresenter$MenuItemLayoutInfo, android.animation.Animator, int):void");
        }
    }

    private static class MenuItemLayoutInfo {
        int left;
        int top;
        View view;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.ActionMenuPresenter.MenuItemLayoutInfo.<init>(android.view.View, boolean):void, dex: 
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
        MenuItemLayoutInfo(android.view.View r1, boolean r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.ActionMenuPresenter.MenuItemLayoutInfo.<init>(android.view.View, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ActionMenuPresenter.MenuItemLayoutInfo.<init>(android.view.View, boolean):void");
        }
    }

    private class OpenOverflowRunnable implements Runnable {
        private OverflowPopup mPopup;
        final /* synthetic */ ActionMenuPresenter this$0;

        public OpenOverflowRunnable(ActionMenuPresenter this$0, OverflowPopup popup) {
            this.this$0 = this$0;
            this.mPopup = popup;
        }

        public void run() {
            if (this.this$0.mMenu != null) {
                this.this$0.mMenu.changeMenuMode();
            }
            View menuView = (View) this.this$0.mMenuView;
            if (!(menuView == null || menuView.getWindowToken() == null || !this.mPopup.tryShow())) {
                this.this$0.mOverflowPopup = this.mPopup;
            }
            this.this$0.mPostedOpenRunnable = null;
        }
    }

    private class OverflowMenuButton extends ImageButton implements ActionMenuChildView {
        final /* synthetic */ ActionMenuPresenter this$0;

        public OverflowMenuButton(ActionMenuPresenter this$0, Context context) {
            this.this$0 = this$0;
            super(context, null, R.attr.actionOverflowButtonStyle);
            setClickable(true);
            setFocusable(true);
            setVisibility(0);
            setEnabled(true);
            setOnTouchListener(new ForwardingListener(this, this) {
                final /* synthetic */ OverflowMenuButton this$1;

                public ShowableListMenu getPopup() {
                    if (this.this$1.this$0.mOverflowPopup == null) {
                        return null;
                    }
                    return this.this$1.this$0.mOverflowPopup.getPopup();
                }

                public boolean onForwardingStarted() {
                    this.this$1.this$0.showOverflowMenu();
                    return true;
                }

                public boolean onForwardingStopped() {
                    if (this.this$1.this$0.mPostedOpenRunnable != null) {
                        return false;
                    }
                    this.this$1.this$0.hideOverflowMenu();
                    return true;
                }
            });
        }

        public boolean performClick() {
            if (super.performClick()) {
                return true;
            }
            playSoundEffect(0);
            this.this$0.showOverflowMenu();
            return true;
        }

        public boolean needsDividerBefore() {
            return false;
        }

        public boolean needsDividerAfter() {
            return false;
        }

        public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfoInternal(info);
            info.setCanOpenPopup(true);
        }

        protected boolean setFrame(int l, int t, int r, int b) {
            boolean changed = super.setFrame(l, t, r, b);
            Drawable d = getDrawable();
            Drawable bg = getBackground();
            if (!(d == null || bg == null)) {
                int width = getWidth();
                int height = getHeight();
                int halfEdge = Math.max(width, height) / 2;
                int centerX = (width + (getPaddingLeft() - getPaddingRight())) / 2;
                int centerY = (height + (getPaddingTop() - getPaddingBottom())) / 2;
                bg.setHotspotBounds(centerX - halfEdge, centerY - halfEdge, centerX + halfEdge, centerY + halfEdge);
            }
            return changed;
        }
    }

    private class OverflowPopup extends MenuPopupHelper {
        final /* synthetic */ ActionMenuPresenter this$0;

        public OverflowPopup(ActionMenuPresenter this$0, Context context, MenuBuilder menu, View anchorView, boolean overflowOnly) {
            this.this$0 = this$0;
            super(context, menu, anchorView, overflowOnly, R.attr.actionOverflowMenuStyle);
            setGravity(Gravity.END);
            setPresenterCallback(this$0.mPopupPresenterCallback);
        }

        protected void onDismiss() {
            if (this.this$0.mMenu != null) {
                this.this$0.mMenu.close();
            }
            this.this$0.mOverflowPopup = null;
            super.onDismiss();
        }
    }

    private class PopupPresenterCallback implements Callback {
        final /* synthetic */ ActionMenuPresenter this$0;

        /* synthetic */ PopupPresenterCallback(ActionMenuPresenter this$0, PopupPresenterCallback popupPresenterCallback) {
            this(this$0);
        }

        private PopupPresenterCallback(ActionMenuPresenter this$0) {
            this.this$0 = this$0;
        }

        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            if (subMenu == null) {
                return false;
            }
            this.this$0.mOpenSubMenuId = ((SubMenuBuilder) subMenu).getItem().getItemId();
            Callback cb = this.this$0.getCallback();
            return cb != null ? cb.onOpenSubMenu(subMenu) : false;
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
            if (menu instanceof SubMenuBuilder) {
                menu.getRootMenu().close(false);
            }
            Callback cb = this.this$0.getCallback();
            if (cb != null) {
                cb.onCloseMenu(menu, allMenusAreClosing);
            }
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private static class SavedState implements Parcelable {
        public static final Creator<SavedState> CREATOR = null;
        public int openSubMenuId;

        /* renamed from: android.widget.ActionMenuPresenter$SavedState$1 */
        static class AnonymousClass1 implements Creator<SavedState> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public /* bridge */ /* synthetic */ Object[] newArray(int size) {
                return newArray(size);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.ActionMenuPresenter.SavedState.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.ActionMenuPresenter.SavedState.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ActionMenuPresenter.SavedState.<clinit>():void");
        }

        SavedState() {
        }

        SavedState(Parcel in) {
            this.openSubMenuId = in.readInt();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.openSubMenuId);
        }
    }

    public ActionMenuPresenter(Context context) {
        super(context, R.layout.action_menu_layout, R.layout.action_menu_item_layout);
        this.mActionButtonGroups = new SparseBooleanArray();
        this.mPopupPresenterCallback = new PopupPresenterCallback(this, null);
        this.mPreLayoutItems = new SparseArray();
        this.mPostLayoutItems = new SparseArray();
        this.mRunningItemAnimations = new ArrayList();
        this.mItemAnimationPreDrawListener = new OnPreDrawListener() {
            public boolean onPreDraw() {
                ActionMenuPresenter.this.computeMenuItemAnimationInfo(false);
                ((View) ActionMenuPresenter.this.mMenuView).getViewTreeObserver().removeOnPreDrawListener(this);
                ActionMenuPresenter.this.runItemAnimations();
                return true;
            }
        };
        this.mAttachStateChangeListener = new OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
            }

            public void onViewDetachedFromWindow(View v) {
                ((View) ActionMenuPresenter.this.mMenuView).getViewTreeObserver().removeOnPreDrawListener(ActionMenuPresenter.this.mItemAnimationPreDrawListener);
                ActionMenuPresenter.this.mPreLayoutItems.clear();
                ActionMenuPresenter.this.mPostLayoutItems.clear();
            }
        };
    }

    public void initForMenu(Context context, MenuBuilder menu) {
        super.initForMenu(context, menu);
        Resources res = context.getResources();
        ActionBarPolicy abp = ActionBarPolicy.get(context);
        if (!this.mReserveOverflowSet) {
            this.mReserveOverflow = abp.showsOverflowMenuButton();
        }
        if (!this.mWidthLimitSet) {
            this.mWidthLimit = abp.getEmbeddedMenuWidthLimit();
        }
        if (!this.mMaxItemsSet) {
            this.mMaxItems = abp.getMaxActionButtons();
        }
        int width = this.mWidthLimit;
        if (this.mReserveOverflow) {
            if (this.mOverflowButton == null) {
                this.mOverflowButton = new OverflowMenuButton(this, this.mSystemContext);
                if (this.mPendingOverflowIconSet) {
                    this.mOverflowButton.setImageDrawable(this.mPendingOverflowIcon);
                    this.mPendingOverflowIcon = null;
                    this.mPendingOverflowIconSet = false;
                }
                int spec = MeasureSpec.makeMeasureSpec(0, 0);
                this.mOverflowButton.measure(spec, spec);
            }
            width -= this.mOverflowButton.getMeasuredWidth();
        } else {
            this.mOverflowButton = null;
        }
        this.mActionItemWidthLimit = width;
        this.mMinCellSize = (int) (res.getDisplayMetrics().density * 56.0f);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (!this.mMaxItemsSet) {
            this.mMaxItems = ActionBarPolicy.get(this.mContext).getMaxActionButtons();
        }
        if (this.mMenu != null) {
            this.mMenu.onItemsChanged(true);
        }
    }

    public void setWidthLimit(int width, boolean strict) {
        this.mWidthLimit = width;
        this.mStrictWidthLimit = strict;
        this.mWidthLimitSet = true;
    }

    public void setReserveOverflow(boolean reserveOverflow) {
        this.mReserveOverflow = reserveOverflow;
        this.mReserveOverflowSet = true;
    }

    public void setItemLimit(int itemCount) {
        this.mMaxItems = itemCount;
        this.mMaxItemsSet = true;
    }

    public void setExpandedActionViewsExclusive(boolean isExclusive) {
        this.mExpandedActionViewsExclusive = isExclusive;
    }

    public void setOverflowIcon(Drawable icon) {
        if (this.mOverflowButton != null) {
            this.mOverflowButton.setImageDrawable(icon);
            return;
        }
        this.mPendingOverflowIconSet = true;
        this.mPendingOverflowIcon = icon;
    }

    public Drawable getOverflowIcon() {
        if (this.mOverflowButton != null) {
            return this.mOverflowButton.getDrawable();
        }
        if (this.mPendingOverflowIconSet) {
            return this.mPendingOverflowIcon;
        }
        return null;
    }

    public MenuView getMenuView(ViewGroup root) {
        MenuView oldMenuView = this.mMenuView;
        MenuView result = super.getMenuView(root);
        if (oldMenuView != result) {
            ((ActionMenuView) result).setPresenter(this);
            if (oldMenuView != null) {
                ((View) oldMenuView).removeOnAttachStateChangeListener(this.mAttachStateChangeListener);
            }
            ((View) result).addOnAttachStateChangeListener(this.mAttachStateChangeListener);
        }
        return result;
    }

    public View getItemView(MenuItemImpl item, View convertView, ViewGroup parent) {
        View actionView = item.getActionView();
        if (actionView == null || item.hasCollapsibleActionView()) {
            actionView = super.getItemView(item, convertView, parent);
        }
        actionView.setVisibility(item.isActionViewExpanded() ? 8 : 0);
        ActionMenuView menuParent = (ActionMenuView) parent;
        LayoutParams lp = actionView.getLayoutParams();
        if (!menuParent.checkLayoutParams(lp)) {
            actionView.setLayoutParams(menuParent.generateLayoutParams(lp));
        }
        return actionView;
    }

    public void bindItemView(MenuItemImpl item, ItemView itemView) {
        itemView.initialize(item, 0);
        ActionMenuItemView actionItemView = (ActionMenuItemView) itemView;
        actionItemView.setItemInvoker(this.mMenuView);
        if (this.mPopupCallback == null) {
            this.mPopupCallback = new ActionMenuPopupCallback(this, null);
        }
        actionItemView.setPopupCallback(this.mPopupCallback);
    }

    public boolean shouldIncludeItem(int childIndex, MenuItemImpl item) {
        return item.isActionButton();
    }

    private void computeMenuItemAnimationInfo(boolean preLayout) {
        ViewGroup menuView = this.mMenuView;
        int count = menuView.getChildCount();
        SparseArray items = preLayout ? this.mPreLayoutItems : this.mPostLayoutItems;
        for (int i = 0; i < count; i++) {
            View child = menuView.getChildAt(i);
            int id = child.getId();
            if (!(id <= 0 || child.getWidth() == 0 || child.getHeight() == 0)) {
                items.put(id, new MenuItemLayoutInfo(child, preLayout));
            }
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
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private void runItemAnimations() {
        /*
        r20 = this;
        r3 = 0;
    L_0x0001:
        r0 = r20;
        r15 = r0.mPreLayoutItems;
        r15 = r15.size();
        if (r3 >= r15) goto L_0x01ab;
    L_0x000b:
        r0 = r20;
        r15 = r0.mPreLayoutItems;
        r4 = r15.keyAt(r3);
        r0 = r20;
        r15 = r0.mPreLayoutItems;
        r9 = r15.get(r4);
        r9 = (android.widget.ActionMenuPresenter.MenuItemLayoutInfo) r9;
        r0 = r20;
        r15 = r0.mPostLayoutItems;
        r12 = r15.indexOfKey(r4);
        if (r12 < 0) goto L_0x012a;
    L_0x0027:
        r0 = r20;
        r15 = r0.mPostLayoutItems;
        r8 = r15.valueAt(r12);
        r8 = (android.widget.ActionMenuPresenter.MenuItemLayoutInfo) r8;
        r13 = 0;
        r14 = 0;
        r15 = r9.left;
        r0 = r8.left;
        r16 = r0;
        r0 = r16;
        if (r15 == r0) goto L_0x0064;
    L_0x003d:
        r15 = android.view.View.TRANSLATION_X;
        r16 = 2;
        r0 = r16;
        r0 = new float[r0];
        r16 = r0;
        r0 = r9.left;
        r17 = r0;
        r0 = r8.left;
        r18 = r0;
        r17 = r17 - r18;
        r0 = r17;
        r0 = (float) r0;
        r17 = r0;
        r18 = 0;
        r16[r18] = r17;
        r17 = 0;
        r18 = 1;
        r16[r18] = r17;
        r13 = android.animation.PropertyValuesHolder.ofFloat(r15, r16);
    L_0x0064:
        r15 = r9.top;
        r0 = r8.top;
        r16 = r0;
        r0 = r16;
        if (r15 == r0) goto L_0x0095;
    L_0x006e:
        r15 = android.view.View.TRANSLATION_Y;
        r16 = 2;
        r0 = r16;
        r0 = new float[r0];
        r16 = r0;
        r0 = r9.top;
        r17 = r0;
        r0 = r8.top;
        r18 = r0;
        r17 = r17 - r18;
        r0 = r17;
        r0 = (float) r0;
        r17 = r0;
        r18 = 0;
        r16[r18] = r17;
        r17 = 0;
        r18 = 1;
        r16[r18] = r17;
        r14 = android.animation.PropertyValuesHolder.ofFloat(r15, r16);
    L_0x0095:
        if (r13 != 0) goto L_0x0099;
    L_0x0097:
        if (r14 == 0) goto L_0x00f9;
    L_0x0099:
        r6 = 0;
    L_0x009a:
        r0 = r20;
        r15 = r0.mRunningItemAnimations;
        r15 = r15.size();
        if (r6 >= r15) goto L_0x00be;
    L_0x00a4:
        r0 = r20;
        r15 = r0.mRunningItemAnimations;
        r11 = r15.get(r6);
        r11 = (android.widget.ActionMenuPresenter.ItemAnimationInfo) r11;
        r15 = r11.id;
        if (r15 != r4) goto L_0x00bb;
    L_0x00b2:
        r15 = r11.animType;
        if (r15 != 0) goto L_0x00bb;
    L_0x00b6:
        r15 = r11.animator;
        r15.cancel();
    L_0x00bb:
        r6 = r6 + 1;
        goto L_0x009a;
    L_0x00be:
        if (r13 == 0) goto L_0x0117;
    L_0x00c0:
        if (r14 == 0) goto L_0x0104;
    L_0x00c2:
        r15 = r8.view;
        r16 = 2;
        r0 = r16;
        r0 = new android.animation.PropertyValuesHolder[r0];
        r16 = r0;
        r17 = 0;
        r16[r17] = r13;
        r17 = 1;
        r16[r17] = r14;
        r2 = android.animation.ObjectAnimator.ofPropertyValuesHolder(r15, r16);
    L_0x00d8:
        r16 = 150; // 0x96 float:2.1E-43 double:7.4E-322;
        r0 = r16;
        r2.setDuration(r0);
        r2.start();
        r5 = new android.widget.ActionMenuPresenter$ItemAnimationInfo;
        r15 = 0;
        r5.<init>(r4, r8, r2, r15);
        r0 = r20;
        r15 = r0.mRunningItemAnimations;
        r15.add(r5);
        r15 = new android.widget.ActionMenuPresenter$3;
        r0 = r20;
        r15.<init>(r0);
        r2.addListener(r15);
    L_0x00f9:
        r0 = r20;
        r15 = r0.mPostLayoutItems;
        r15.remove(r4);
    L_0x0100:
        r3 = r3 + 1;
        goto L_0x0001;
    L_0x0104:
        r15 = r8.view;
        r16 = 1;
        r0 = r16;
        r0 = new android.animation.PropertyValuesHolder[r0];
        r16 = r0;
        r17 = 0;
        r16[r17] = r13;
        r2 = android.animation.ObjectAnimator.ofPropertyValuesHolder(r15, r16);
        goto L_0x00d8;
    L_0x0117:
        r15 = r8.view;
        r16 = 1;
        r0 = r16;
        r0 = new android.animation.PropertyValuesHolder[r0];
        r16 = r0;
        r17 = 0;
        r16[r17] = r14;
        r2 = android.animation.ObjectAnimator.ofPropertyValuesHolder(r15, r16);
        goto L_0x00d8;
    L_0x012a:
        r10 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r6 = 0;
    L_0x012d:
        r0 = r20;
        r15 = r0.mRunningItemAnimations;
        r15 = r15.size();
        if (r6 >= r15) goto L_0x015d;
    L_0x0137:
        r0 = r20;
        r15 = r0.mRunningItemAnimations;
        r11 = r15.get(r6);
        r11 = (android.widget.ActionMenuPresenter.ItemAnimationInfo) r11;
        r15 = r11.id;
        if (r15 != r4) goto L_0x015a;
    L_0x0145:
        r15 = r11.animType;
        r16 = 1;
        r0 = r16;
        if (r15 != r0) goto L_0x015a;
    L_0x014d:
        r15 = r11.menuItemLayoutInfo;
        r15 = r15.view;
        r10 = r15.getAlpha();
        r15 = r11.animator;
        r15.cancel();
    L_0x015a:
        r6 = r6 + 1;
        goto L_0x012d;
    L_0x015d:
        r15 = r9.view;
        r16 = android.view.View.ALPHA;
        r17 = 2;
        r0 = r17;
        r0 = new float[r0];
        r17 = r0;
        r18 = 0;
        r17[r18] = r10;
        r18 = 0;
        r19 = 1;
        r17[r19] = r18;
        r2 = android.animation.ObjectAnimator.ofFloat(r15, r16, r17);
        r0 = r20;
        r15 = r0.mMenuView;
        r15 = (android.view.ViewGroup) r15;
        r15 = r15.getOverlay();
        r0 = r9.view;
        r16 = r0;
        r15.add(r16);
        r16 = 150; // 0x96 float:2.1E-43 double:7.4E-322;
        r0 = r16;
        r2.setDuration(r0);
        r2.start();
        r5 = new android.widget.ActionMenuPresenter$ItemAnimationInfo;
        r15 = 2;
        r5.<init>(r4, r9, r2, r15);
        r0 = r20;
        r15 = r0.mRunningItemAnimations;
        r15.add(r5);
        r15 = new android.widget.ActionMenuPresenter$4;
        r0 = r20;
        r15.<init>(r0, r9);
        r2.addListener(r15);
        goto L_0x0100;
    L_0x01ab:
        r3 = 0;
    L_0x01ac:
        r0 = r20;
        r15 = r0.mPostLayoutItems;
        r15 = r15.size();
        if (r3 >= r15) goto L_0x0243;
    L_0x01b6:
        r0 = r20;
        r15 = r0.mPostLayoutItems;
        r4 = r15.keyAt(r3);
        r0 = r20;
        r15 = r0.mPostLayoutItems;
        r12 = r15.indexOfKey(r4);
        if (r12 < 0) goto L_0x023f;
    L_0x01c8:
        r0 = r20;
        r15 = r0.mPostLayoutItems;
        r7 = r15.valueAt(r12);
        r7 = (android.widget.ActionMenuPresenter.MenuItemLayoutInfo) r7;
        r10 = 0;
        r6 = 0;
    L_0x01d4:
        r0 = r20;
        r15 = r0.mRunningItemAnimations;
        r15 = r15.size();
        if (r6 >= r15) goto L_0x0204;
    L_0x01de:
        r0 = r20;
        r15 = r0.mRunningItemAnimations;
        r11 = r15.get(r6);
        r11 = (android.widget.ActionMenuPresenter.ItemAnimationInfo) r11;
        r15 = r11.id;
        if (r15 != r4) goto L_0x0201;
    L_0x01ec:
        r15 = r11.animType;
        r16 = 2;
        r0 = r16;
        if (r15 != r0) goto L_0x0201;
    L_0x01f4:
        r15 = r11.menuItemLayoutInfo;
        r15 = r15.view;
        r10 = r15.getAlpha();
        r15 = r11.animator;
        r15.cancel();
    L_0x0201:
        r6 = r6 + 1;
        goto L_0x01d4;
    L_0x0204:
        r15 = r7.view;
        r16 = android.view.View.ALPHA;
        r17 = 2;
        r0 = r17;
        r0 = new float[r0];
        r17 = r0;
        r18 = 0;
        r17[r18] = r10;
        r18 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r19 = 1;
        r17[r19] = r18;
        r2 = android.animation.ObjectAnimator.ofFloat(r15, r16, r17);
        r2.start();
        r16 = 150; // 0x96 float:2.1E-43 double:7.4E-322;
        r0 = r16;
        r2.setDuration(r0);
        r5 = new android.widget.ActionMenuPresenter$ItemAnimationInfo;
        r15 = 1;
        r5.<init>(r4, r7, r2, r15);
        r0 = r20;
        r15 = r0.mRunningItemAnimations;
        r15.add(r5);
        r15 = new android.widget.ActionMenuPresenter$5;
        r0 = r20;
        r15.<init>(r0);
        r2.addListener(r15);
    L_0x023f:
        r3 = r3 + 1;
        goto L_0x01ac;
    L_0x0243:
        r0 = r20;
        r15 = r0.mPreLayoutItems;
        r15.clear();
        r0 = r20;
        r15 = r0.mPostLayoutItems;
        r15.clear();
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.ActionMenuPresenter.runItemAnimations():void");
    }

    private void setupItemAnimations() {
        computeMenuItemAnimationInfo(true);
        ((View) this.mMenuView).getViewTreeObserver().addOnPreDrawListener(this.mItemAnimationPreDrawListener);
    }

    public void updateMenuView(boolean cleared) {
        int count;
        if (((ViewGroup) ((View) this.mMenuView).getParent()) != null) {
        }
        super.updateMenuView(cleared);
        ((View) this.mMenuView).requestLayout();
        if (this.mMenu != null) {
            ArrayList<MenuItemImpl> actionItems = this.mMenu.getActionItems();
            count = actionItems.size();
            for (int i = 0; i < count; i++) {
                ActionProvider provider = ((MenuItemImpl) actionItems.get(i)).getActionProvider();
                if (provider != null) {
                    provider.setSubUiVisibilityListener(this);
                }
            }
        }
        ArrayList nonActionItems = this.mMenu != null ? this.mMenu.getNonActionItems() : null;
        boolean hasOverflow = false;
        if (this.mReserveOverflow && nonActionItems != null) {
            count = nonActionItems.size();
            hasOverflow = count == 1 ? !((MenuItemImpl) nonActionItems.get(0)).isActionViewExpanded() : count > 0;
        }
        if (hasOverflow) {
            if (this.mOverflowButton == null) {
                this.mOverflowButton = new OverflowMenuButton(this, this.mSystemContext);
            }
            ViewGroup parent = (ViewGroup) this.mOverflowButton.getParent();
            if (parent != this.mMenuView) {
                if (parent != null) {
                    parent.removeView(this.mOverflowButton);
                }
                ActionMenuView menuView = this.mMenuView;
                menuView.addView(this.mOverflowButton, menuView.generateOverflowButtonLayoutParams());
            }
        } else if (this.mOverflowButton != null && this.mOverflowButton.getParent() == this.mMenuView) {
            ((ViewGroup) this.mMenuView).removeView(this.mOverflowButton);
        }
        ((ActionMenuView) this.mMenuView).setOverflowReserved(this.mReserveOverflow);
    }

    public boolean filterLeftoverView(ViewGroup parent, int childIndex) {
        if (parent.getChildAt(childIndex) == this.mOverflowButton) {
            return false;
        }
        return super.filterLeftoverView(parent, childIndex);
    }

    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        if (!subMenu.hasVisibleItems()) {
            return false;
        }
        SubMenuBuilder topSubMenu = subMenu;
        while (topSubMenu.getParentMenu() != this.mMenu) {
            topSubMenu = (SubMenuBuilder) topSubMenu.getParentMenu();
        }
        View anchor = findViewForItem(topSubMenu.getItem());
        if (anchor == null) {
            return false;
        }
        this.mOpenSubMenuId = subMenu.getItem().getItemId();
        boolean preserveIconSpacing = false;
        int count = subMenu.size();
        for (int i = 0; i < count; i++) {
            MenuItem childItem = subMenu.getItem(i);
            if (childItem.isVisible() && childItem.getIcon() != null) {
                preserveIconSpacing = true;
                break;
            }
        }
        this.mActionButtonPopup = new ActionButtonSubmenu(this, this.mContext, subMenu, anchor);
        this.mActionButtonPopup.setForceShowIcon(preserveIconSpacing);
        this.mActionButtonPopup.show();
        super.onSubMenuSelected(subMenu);
        return true;
    }

    private View findViewForItem(MenuItem item) {
        ViewGroup parent = this.mMenuView;
        if (parent == null) {
            return null;
        }
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = parent.getChildAt(i);
            if ((child instanceof ItemView) && ((ItemView) child).getItemData() == item) {
                return child;
            }
        }
        return null;
    }

    public boolean showOverflowMenu() {
        if (!this.mReserveOverflow || isOverflowMenuShowing() || this.mMenu == null || this.mMenuView == null || this.mPostedOpenRunnable != null || this.mMenu.getNonActionItems().isEmpty()) {
            return false;
        }
        this.mPostedOpenRunnable = new OpenOverflowRunnable(this, new OverflowPopup(this, this.mContext, this.mMenu, this.mOverflowButton, true));
        ((View) this.mMenuView).post(this.mPostedOpenRunnable);
        super.onSubMenuSelected(null);
        return true;
    }

    public boolean hideOverflowMenu() {
        if (this.mPostedOpenRunnable == null || this.mMenuView == null) {
            MenuPopupHelper popup = this.mOverflowPopup;
            if (popup == null) {
                return false;
            }
            popup.dismiss();
            return true;
        }
        ((View) this.mMenuView).removeCallbacks(this.mPostedOpenRunnable);
        this.mPostedOpenRunnable = null;
        return true;
    }

    public boolean dismissPopupMenus() {
        return hideOverflowMenu() | hideSubMenus();
    }

    public boolean hideSubMenus() {
        if (this.mActionButtonPopup == null) {
            return false;
        }
        this.mActionButtonPopup.dismiss();
        return true;
    }

    public boolean isOverflowMenuShowing() {
        return this.mOverflowPopup != null ? this.mOverflowPopup.isShowing() : false;
    }

    public boolean isOverflowMenuShowPending() {
        return this.mPostedOpenRunnable == null ? isOverflowMenuShowing() : true;
    }

    public boolean isOverflowReserved() {
        return this.mReserveOverflow;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Changwei.Li@Plf.SDK, 2015-06-11 : Modify for SplitMenu", property = OppoRomType.ROM)
    public boolean flagActionItems() {
        ArrayList<MenuItemImpl> visibleItems;
        int itemsSize;
        int i;
        MenuItemImpl item;
        if (this.mMenu != null) {
            visibleItems = this.mMenu.getVisibleItems();
            itemsSize = visibleItems.size();
        } else {
            visibleItems = null;
            itemsSize = 0;
        }
        int maxActions = this.mMaxItems;
        int widthLimit = this.mActionItemWidthLimit;
        int querySpec = MeasureSpec.makeMeasureSpec(0, 0);
        ViewGroup parent = (ViewGroup) this.mMenuView;
        int requiredItems = 0;
        int requestedItems = 0;
        int firstActionWidth = 0;
        boolean hasOverflow = false;
        for (i = 0; i < itemsSize; i++) {
            item = (MenuItemImpl) visibleItems.get(i);
            if (item.requiresActionButton()) {
                requiredItems++;
            } else if (item.requestsActionButton()) {
                requestedItems++;
            } else {
                hasOverflow = true;
            }
            if (this.mExpandedActionViewsExclusive && item.isActionViewExpanded()) {
                maxActions = 0;
            }
        }
        if (this.mReserveOverflow && (hasOverflow || requiredItems + requestedItems > maxActions)) {
            maxActions--;
        }
        maxActions -= requiredItems;
        SparseBooleanArray seenGroups = this.mActionButtonGroups;
        seenGroups.clear();
        int cellSize = 0;
        int cellsRemaining = 0;
        if (this.mStrictWidthLimit) {
            cellsRemaining = widthLimit / this.mMinCellSize;
            cellSize = this.mMinCellSize + ((widthLimit % this.mMinCellSize) / cellsRemaining);
        }
        for (i = 0; i < itemsSize; i++) {
            item = (MenuItemImpl) visibleItems.get(i);
            View v;
            int measuredWidth;
            int groupId;
            if (item.requiresActionButton()) {
                v = getItemView(item, null, parent);
                if (this.mStrictWidthLimit) {
                    cellsRemaining -= ActionMenuView.measureChildForCells(v, cellSize, cellsRemaining, querySpec, 0);
                } else {
                    v.measure(querySpec, querySpec);
                }
                measuredWidth = v.getMeasuredWidth();
                widthLimit -= measuredWidth;
                if (firstActionWidth == 0) {
                    firstActionWidth = measuredWidth;
                }
                groupId = item.getGroupId();
                if (groupId != 0) {
                    seenGroups.put(groupId, true);
                }
                item.setIsActionButton(true);
            } else if (item.requestsActionButton()) {
                boolean isAction;
                groupId = item.getGroupId();
                boolean inGroup = seenGroups.get(groupId);
                if ((maxActions > 0 || inGroup) && widthLimit > 0) {
                    boolean z = !this.mStrictWidthLimit || cellsRemaining > 0;
                    isAction = z;
                } else {
                    isAction = false;
                }
                if (isAction) {
                    int isAction2;
                    v = getItemView(item, null, parent);
                    if (this.mStrictWidthLimit) {
                        int cells = ActionMenuView.measureChildForCells(v, cellSize, cellsRemaining, querySpec, 0);
                        cellsRemaining -= cells;
                        if (cells == 0) {
                            isAction2 = 0;
                        }
                    } else {
                        v.measure(querySpec, querySpec);
                    }
                    measuredWidth = v.getMeasuredWidth();
                    widthLimit -= measuredWidth;
                    if (firstActionWidth == 0) {
                        firstActionWidth = measuredWidth;
                    }
                    if (this.mStrictWidthLimit) {
                        isAction = isAction2 & (widthLimit >= 0 ? 1 : 0);
                    } else {
                        isAction = isAction2 & (widthLimit + firstActionWidth > 0 ? 1 : 0);
                    }
                }
                if (isAction && groupId != 0) {
                    seenGroups.put(groupId, true);
                } else if (inGroup) {
                    seenGroups.put(groupId, false);
                    for (int j = 0; j < i; j++) {
                        MenuItemImpl areYouMyGroupie = (MenuItemImpl) visibleItems.get(j);
                        if (areYouMyGroupie.getGroupId() == groupId) {
                            if (areYouMyGroupie.isActionButton()) {
                                maxActions++;
                            }
                            areYouMyGroupie.setIsActionButton(false);
                        }
                    }
                }
                if (isAction) {
                    maxActions--;
                }
                item.setIsActionButton(isAction);
            } else {
                item.setIsActionButton(false);
            }
        }
        if (this.mIsOppoStyle) {
            int bottomCount = 0;
            for (i = 0; i < itemsSize; i++) {
                item = (MenuItemImpl) visibleItems.get(i);
                if (!item.requiresActionButton()) {
                    bottomCount++;
                    if (bottomCount <= 5) {
                        item.setIsActionButton(true);
                    } else {
                        item.setIsActionButton(false);
                    }
                }
            }
        }
        return true;
    }

    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        dismissPopupMenus();
        super.onCloseMenu(menu, allMenusAreClosing);
    }

    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState();
        state.openSubMenuId = this.mOpenSubMenuId;
        return state;
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState saved = (SavedState) state;
        if (saved.openSubMenuId > 0) {
            MenuItem item = this.mMenu.findItem(saved.openSubMenuId);
            if (item != null) {
                onSubMenuSelected((SubMenuBuilder) item.getSubMenu());
            }
        }
    }

    public void onSubUiVisibilityChanged(boolean isVisible) {
        if (isVisible) {
            super.onSubMenuSelected(null);
        } else if (this.mMenu != null) {
            this.mMenu.close(false);
        }
    }

    public void setMenuView(ActionMenuView menuView) {
        if (menuView != this.mMenuView) {
            if (this.mMenuView != null) {
                ((View) this.mMenuView).removeOnAttachStateChangeListener(this.mAttachStateChangeListener);
            }
            this.mMenuView = menuView;
            menuView.addOnAttachStateChangeListener(this.mAttachStateChangeListener);
        }
        menuView.initialize(this.mMenu);
    }
}
