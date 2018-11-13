package android.widget;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Collections;

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
class ExpandableListConnector extends BaseAdapter implements Filterable {
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "XiaoKang.Feng@Plf.SDK : 2015-06-24 : Add for color style expandable list", property = OppoRomType.ROM)
    private Runnable mColorExpandListRunnable;
    private final DataSetObserver mDataSetObserver;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "XiaoKang.Feng@Plf.SDK : 2016-01-07 : Add for color style expandable list", property = OppoRomType.ROM)
    private long mDuration;
    private ArrayList<GroupMetadata> mExpGroupMetadataList;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "XiaoKang.Feng@Plf.SDK : 2015-06-24 : Add for color style expandable list", property = OppoRomType.ROM)
    private boolean mExpandAnimationEnabled;
    private ExpandableListAdapter mExpandableListAdapter;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "XiaoKang.Feng@Plf.SDK : 2015-06-24 : Add for color style expandable list", property = OppoRomType.ROM)
    private Handler mHandler;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "XiaoKang.Feng@Plf.SDK : 2015-06-24 : Add for color style expandable list", property = OppoRomType.ROM)
    private float mHeightOffset;
    private int mMaxExpGroupCount;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "XiaoKang.Feng@Plf.SDK : 2015-06-24 : Add for color style expandable list", property = OppoRomType.ROM)
    PositionMetadata mPositionMetadata;
    private int mTotalExpChildrenCount;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "XiaoKang.Feng@Plf.SDK : 2015-06-24 : Add for color style expandable list", property = OppoRomType.ROM)
    int tmpCollapseGroupPos;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "XiaoKang.Feng@Plf.SDK : 2015-06-24 : Add for color style expandable list", property = OppoRomType.ROM)
    int tmpExpandGroupPos;

    /* renamed from: android.widget.ExpandableListConnector$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ ExpandableListConnector this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.ExpandableListConnector.1.<init>(android.widget.ExpandableListConnector):void, dex: 
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
        AnonymousClass1(android.widget.ExpandableListConnector r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.ExpandableListConnector.1.<init>(android.widget.ExpandableListConnector):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.1.<init>(android.widget.ExpandableListConnector):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.ExpandableListConnector.1.run():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.ExpandableListConnector.1.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.1.run():void");
        }
    }

    /* renamed from: android.widget.ExpandableListConnector$2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ ExpandableListConnector this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.ExpandableListConnector.2.<init>(android.widget.ExpandableListConnector):void, dex: 
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
        AnonymousClass2(android.widget.ExpandableListConnector r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.ExpandableListConnector.2.<init>(android.widget.ExpandableListConnector):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.2.<init>(android.widget.ExpandableListConnector):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.ExpandableListConnector.2.run():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.ExpandableListConnector.2.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.2.run():void");
        }
    }

    @OppoHook(level = OppoHookType.NEW_CLASS, note = "XiaoKang.Feng@Plf.SDK, 2015-06-24 : Add for color style expandable list", property = OppoRomType.ROM)
    class ColorExpandListAnimation extends ValueAnimator {
        final int COLLAPSE_ANIMATION;
        final int EXPAND_ANIMATION;
        private View mAnimatedView;
        private boolean mChageOffset;
        private int mType;
        final /* synthetic */ ExpandableListConnector this$0;

        /* renamed from: android.widget.ExpandableListConnector$ColorExpandListAnimation$1 */
        class AnonymousClass1 implements AnimatorUpdateListener {
            final /* synthetic */ ColorExpandListAnimation this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.ExpandableListConnector.ColorExpandListAnimation.1.<init>(android.widget.ExpandableListConnector$ColorExpandListAnimation):void, dex: 
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
            AnonymousClass1(android.widget.ExpandableListConnector.ColorExpandListAnimation r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.ExpandableListConnector.ColorExpandListAnimation.1.<init>(android.widget.ExpandableListConnector$ColorExpandListAnimation):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.ColorExpandListAnimation.1.<init>(android.widget.ExpandableListConnector$ColorExpandListAnimation):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.ExpandableListConnector.ColorExpandListAnimation.1.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
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
            public void onAnimationUpdate(android.animation.ValueAnimator r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.ExpandableListConnector.ColorExpandListAnimation.1.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.ColorExpandListAnimation.1.onAnimationUpdate(android.animation.ValueAnimator):void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.ExpandableListConnector.ColorExpandListAnimation.-get0(android.widget.ExpandableListConnector$ColorExpandListAnimation):android.view.View, dex:  in method: android.widget.ExpandableListConnector.ColorExpandListAnimation.-get0(android.widget.ExpandableListConnector$ColorExpandListAnimation):android.view.View, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.ExpandableListConnector.ColorExpandListAnimation.-get0(android.widget.ExpandableListConnector$ColorExpandListAnimation):android.view.View, dex: 
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
        /* renamed from: -get0 */
        static /* synthetic */ android.view.View m329-get0(android.widget.ExpandableListConnector.ColorExpandListAnimation r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.widget.ExpandableListConnector.ColorExpandListAnimation.-get0(android.widget.ExpandableListConnector$ColorExpandListAnimation):android.view.View, dex:  in method: android.widget.ExpandableListConnector.ColorExpandListAnimation.-get0(android.widget.ExpandableListConnector$ColorExpandListAnimation):android.view.View, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.ColorExpandListAnimation.-get0(android.widget.ExpandableListConnector$ColorExpandListAnimation):android.view.View");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.ExpandableListConnector.ColorExpandListAnimation.-get1(android.widget.ExpandableListConnector$ColorExpandListAnimation):boolean, dex:  in method: android.widget.ExpandableListConnector.ColorExpandListAnimation.-get1(android.widget.ExpandableListConnector$ColorExpandListAnimation):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.ExpandableListConnector.ColorExpandListAnimation.-get1(android.widget.ExpandableListConnector$ColorExpandListAnimation):boolean, dex: 
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
        /* renamed from: -get1 */
        static /* synthetic */ boolean m330-get1(android.widget.ExpandableListConnector.ColorExpandListAnimation r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.widget.ExpandableListConnector.ColorExpandListAnimation.-get1(android.widget.ExpandableListConnector$ColorExpandListAnimation):boolean, dex:  in method: android.widget.ExpandableListConnector.ColorExpandListAnimation.-get1(android.widget.ExpandableListConnector$ColorExpandListAnimation):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.ColorExpandListAnimation.-get1(android.widget.ExpandableListConnector$ColorExpandListAnimation):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.ExpandableListConnector.ColorExpandListAnimation.<init>(android.widget.ExpandableListConnector, android.view.View, int, boolean):void, dex: 
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
        public ColorExpandListAnimation(android.widget.ExpandableListConnector r1, android.view.View r2, int r3, boolean r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.ExpandableListConnector.ColorExpandListAnimation.<init>(android.widget.ExpandableListConnector, android.view.View, int, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.ColorExpandListAnimation.<init>(android.widget.ExpandableListConnector, android.view.View, int, boolean):void");
        }

        public ColorExpandListAnimation(ExpandableListConnector this$0, View view, int type) {
            this(this$0, view, type, false);
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
    static class GroupMetadata implements Parcelable, Comparable<GroupMetadata> {
        public static final Creator<GroupMetadata> CREATOR = null;
        static final int REFRESH = -1;
        int flPos;
        long gId;
        int gPos;
        int lastChildFlPos;

        /* renamed from: android.widget.ExpandableListConnector$GroupMetadata$1 */
        static class AnonymousClass1 implements Creator<GroupMetadata> {
            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.ExpandableListConnector.GroupMetadata.1.<init>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass1() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.ExpandableListConnector.GroupMetadata.1.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.GroupMetadata.1.<init>():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.ExpandableListConnector.GroupMetadata.1.createFromParcel(android.os.Parcel):android.widget.ExpandableListConnector$GroupMetadata, dex: 
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
            public android.widget.ExpandableListConnector.GroupMetadata createFromParcel(android.os.Parcel r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.ExpandableListConnector.GroupMetadata.1.createFromParcel(android.os.Parcel):android.widget.ExpandableListConnector$GroupMetadata, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.GroupMetadata.1.createFromParcel(android.os.Parcel):android.widget.ExpandableListConnector$GroupMetadata");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.ExpandableListConnector.GroupMetadata.1.createFromParcel(android.os.Parcel):java.lang.Object, dex: 
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
            public /* bridge */ /* synthetic */ java.lang.Object createFromParcel(android.os.Parcel r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.ExpandableListConnector.GroupMetadata.1.createFromParcel(android.os.Parcel):java.lang.Object, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.GroupMetadata.1.createFromParcel(android.os.Parcel):java.lang.Object");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.ExpandableListConnector.GroupMetadata.1.newArray(int):java.lang.Object[], dex: 
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
            public /* bridge */ /* synthetic */ java.lang.Object[] newArray(int r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.ExpandableListConnector.GroupMetadata.1.newArray(int):java.lang.Object[], dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.GroupMetadata.1.newArray(int):java.lang.Object[]");
            }

            public GroupMetadata[] newArray(int size) {
                return new GroupMetadata[size];
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.ExpandableListConnector.GroupMetadata.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.ExpandableListConnector.GroupMetadata.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.GroupMetadata.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.ExpandableListConnector.GroupMetadata.<init>():void, dex: 
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
        private GroupMetadata() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.ExpandableListConnector.GroupMetadata.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.GroupMetadata.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.widget.ExpandableListConnector.GroupMetadata.obtain(int, int, int, long):android.widget.ExpandableListConnector$GroupMetadata, dex: 
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
        static android.widget.ExpandableListConnector.GroupMetadata obtain(int r1, int r2, int r3, long r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.widget.ExpandableListConnector.GroupMetadata.obtain(int, int, int, long):android.widget.ExpandableListConnector$GroupMetadata, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.GroupMetadata.obtain(int, int, int, long):android.widget.ExpandableListConnector$GroupMetadata");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.ExpandableListConnector.GroupMetadata.compareTo(android.widget.ExpandableListConnector$GroupMetadata):int, dex: 
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
        public int compareTo(android.widget.ExpandableListConnector.GroupMetadata r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.ExpandableListConnector.GroupMetadata.compareTo(android.widget.ExpandableListConnector$GroupMetadata):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.GroupMetadata.compareTo(android.widget.ExpandableListConnector$GroupMetadata):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.ExpandableListConnector.GroupMetadata.compareTo(java.lang.Object):int, dex: 
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
        public /* bridge */ /* synthetic */ int compareTo(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.ExpandableListConnector.GroupMetadata.compareTo(java.lang.Object):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.GroupMetadata.compareTo(java.lang.Object):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.ExpandableListConnector.GroupMetadata.writeToParcel(android.os.Parcel, int):void, dex:  in method: android.widget.ExpandableListConnector.GroupMetadata.writeToParcel(android.os.Parcel, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.ExpandableListConnector.GroupMetadata.writeToParcel(android.os.Parcel, int):void, dex: 
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
        public void writeToParcel(android.os.Parcel r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.widget.ExpandableListConnector.GroupMetadata.writeToParcel(android.os.Parcel, int):void, dex:  in method: android.widget.ExpandableListConnector.GroupMetadata.writeToParcel(android.os.Parcel, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.GroupMetadata.writeToParcel(android.os.Parcel, int):void");
        }

        public int describeContents() {
            return 0;
        }
    }

    protected class MyDataSetObserver extends DataSetObserver {
        final /* synthetic */ ExpandableListConnector this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.ExpandableListConnector.MyDataSetObserver.<init>(android.widget.ExpandableListConnector):void, dex: 
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
        protected MyDataSetObserver(android.widget.ExpandableListConnector r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.ExpandableListConnector.MyDataSetObserver.<init>(android.widget.ExpandableListConnector):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.MyDataSetObserver.<init>(android.widget.ExpandableListConnector):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.ExpandableListConnector.MyDataSetObserver.onChanged():void, dex: 
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
        public void onChanged() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.ExpandableListConnector.MyDataSetObserver.onChanged():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.MyDataSetObserver.onChanged():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.ExpandableListConnector.MyDataSetObserver.onInvalidated():void, dex: 
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
        public void onInvalidated() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.ExpandableListConnector.MyDataSetObserver.onInvalidated():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.MyDataSetObserver.onInvalidated():void");
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
    public static class PositionMetadata {
        private static final int MAX_POOL_SIZE = 5;
        private static ArrayList<PositionMetadata> sPool;
        public int groupInsertIndex;
        public GroupMetadata groupMetadata;
        public ExpandableListPosition position;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.ExpandableListConnector.PositionMetadata.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.ExpandableListConnector.PositionMetadata.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.PositionMetadata.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.ExpandableListConnector.PositionMetadata.<init>():void, dex: 
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
        private PositionMetadata() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.ExpandableListConnector.PositionMetadata.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.PositionMetadata.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.ExpandableListConnector.PositionMetadata.getRecycledOrCreate():android.widget.ExpandableListConnector$PositionMetadata, dex: 
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
        private static android.widget.ExpandableListConnector.PositionMetadata getRecycledOrCreate() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.ExpandableListConnector.PositionMetadata.getRecycledOrCreate():android.widget.ExpandableListConnector$PositionMetadata, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.PositionMetadata.getRecycledOrCreate():android.widget.ExpandableListConnector$PositionMetadata");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.ExpandableListConnector.PositionMetadata.obtain(int, int, int, int, android.widget.ExpandableListConnector$GroupMetadata, int):android.widget.ExpandableListConnector$PositionMetadata, dex: 
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
        static android.widget.ExpandableListConnector.PositionMetadata obtain(int r1, int r2, int r3, int r4, android.widget.ExpandableListConnector.GroupMetadata r5, int r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.ExpandableListConnector.PositionMetadata.obtain(int, int, int, int, android.widget.ExpandableListConnector$GroupMetadata, int):android.widget.ExpandableListConnector$PositionMetadata, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.PositionMetadata.obtain(int, int, int, int, android.widget.ExpandableListConnector$GroupMetadata, int):android.widget.ExpandableListConnector$PositionMetadata");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.ExpandableListConnector.PositionMetadata.resetState():void, dex: 
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
        private void resetState() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.ExpandableListConnector.PositionMetadata.resetState():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.PositionMetadata.resetState():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.ExpandableListConnector.PositionMetadata.isExpanded():boolean, dex: 
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
        public boolean isExpanded() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.ExpandableListConnector.PositionMetadata.isExpanded():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.PositionMetadata.isExpanded():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.ExpandableListConnector.PositionMetadata.recycle():void, dex: 
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
        public void recycle() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.ExpandableListConnector.PositionMetadata.recycle():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListConnector.PositionMetadata.recycle():void");
        }
    }

    public ExpandableListConnector(ExpandableListAdapter expandableListAdapter) {
        this.mMaxExpGroupCount = Integer.MAX_VALUE;
        this.mDataSetObserver = new MyDataSetObserver(this);
        this.tmpExpandGroupPos = -1;
        this.tmpCollapseGroupPos = -1;
        this.mExpandAnimationEnabled = false;
        this.mHandler = null;
        this.mDuration = 300;
        this.mExpGroupMetadataList = new ArrayList();
        setExpandableListAdapter(expandableListAdapter);
    }

    public void setExpandableListAdapter(ExpandableListAdapter expandableListAdapter) {
        if (this.mExpandableListAdapter != null) {
            this.mExpandableListAdapter.unregisterDataSetObserver(this.mDataSetObserver);
        }
        this.mExpandableListAdapter = expandableListAdapter;
        expandableListAdapter.registerDataSetObserver(this.mDataSetObserver);
    }

    PositionMetadata getUnflattenedPos(int flPos) {
        ArrayList<GroupMetadata> egml = this.mExpGroupMetadataList;
        int numExpGroups = egml.size();
        int leftExpGroupIndex = 0;
        int rightExpGroupIndex = numExpGroups - 1;
        int midExpGroupIndex = 0;
        if (numExpGroups == 0) {
            return PositionMetadata.obtain(flPos, 2, flPos, -1, null, 0);
        }
        int insertPosition;
        int groupPos;
        while (leftExpGroupIndex <= rightExpGroupIndex) {
            midExpGroupIndex = ((rightExpGroupIndex - leftExpGroupIndex) / 2) + leftExpGroupIndex;
            GroupMetadata midExpGm = (GroupMetadata) egml.get(midExpGroupIndex);
            if (flPos > midExpGm.lastChildFlPos) {
                leftExpGroupIndex = midExpGroupIndex + 1;
            } else if (flPos < midExpGm.flPos) {
                rightExpGroupIndex = midExpGroupIndex - 1;
            } else if (flPos == midExpGm.flPos) {
                return PositionMetadata.obtain(flPos, 2, midExpGm.gPos, -1, midExpGm, midExpGroupIndex);
            } else if (flPos <= midExpGm.lastChildFlPos) {
                return PositionMetadata.obtain(flPos, 1, midExpGm.gPos, flPos - (midExpGm.flPos + 1), midExpGm, midExpGroupIndex);
            }
        }
        if (leftExpGroupIndex > midExpGroupIndex) {
            GroupMetadata leftExpGm = (GroupMetadata) egml.get(leftExpGroupIndex - 1);
            insertPosition = leftExpGroupIndex;
            groupPos = (flPos - leftExpGm.lastChildFlPos) + leftExpGm.gPos;
        } else if (rightExpGroupIndex < midExpGroupIndex) {
            rightExpGroupIndex++;
            GroupMetadata rightExpGm = (GroupMetadata) egml.get(rightExpGroupIndex);
            insertPosition = rightExpGroupIndex;
            groupPos = rightExpGm.gPos - (rightExpGm.flPos - flPos);
        } else {
            throw new RuntimeException("Unknown state");
        }
        return PositionMetadata.obtain(flPos, 2, groupPos, -1, null, insertPosition);
    }

    PositionMetadata getFlattenedPos(ExpandableListPosition pos) {
        ArrayList<GroupMetadata> egml = this.mExpGroupMetadataList;
        int numExpGroups = egml.size();
        int leftExpGroupIndex = 0;
        int rightExpGroupIndex = numExpGroups - 1;
        int midExpGroupIndex = 0;
        if (numExpGroups == 0) {
            return PositionMetadata.obtain(pos.groupPos, pos.type, pos.groupPos, pos.childPos, null, 0);
        }
        while (leftExpGroupIndex <= rightExpGroupIndex) {
            midExpGroupIndex = ((rightExpGroupIndex - leftExpGroupIndex) / 2) + leftExpGroupIndex;
            GroupMetadata midExpGm = (GroupMetadata) egml.get(midExpGroupIndex);
            if (pos.groupPos > midExpGm.gPos) {
                leftExpGroupIndex = midExpGroupIndex + 1;
            } else if (pos.groupPos < midExpGm.gPos) {
                rightExpGroupIndex = midExpGroupIndex - 1;
            } else if (pos.groupPos == midExpGm.gPos) {
                if (pos.type == 2) {
                    return PositionMetadata.obtain(midExpGm.flPos, pos.type, pos.groupPos, pos.childPos, midExpGm, midExpGroupIndex);
                }
                if (pos.type == 1) {
                    return PositionMetadata.obtain((midExpGm.flPos + pos.childPos) + 1, pos.type, pos.groupPos, pos.childPos, midExpGm, midExpGroupIndex);
                }
                return null;
            }
        }
        if (pos.type != 2) {
            return null;
        }
        if (leftExpGroupIndex > midExpGroupIndex) {
            GroupMetadata leftExpGm = (GroupMetadata) egml.get(leftExpGroupIndex - 1);
            return PositionMetadata.obtain(leftExpGm.lastChildFlPos + (pos.groupPos - leftExpGm.gPos), pos.type, pos.groupPos, pos.childPos, null, leftExpGroupIndex);
        } else if (rightExpGroupIndex >= midExpGroupIndex) {
            return null;
        } else {
            rightExpGroupIndex++;
            GroupMetadata rightExpGm = (GroupMetadata) egml.get(rightExpGroupIndex);
            return PositionMetadata.obtain(rightExpGm.flPos - (rightExpGm.gPos - pos.groupPos), pos.type, pos.groupPos, pos.childPos, null, rightExpGroupIndex);
        }
    }

    public boolean areAllItemsEnabled() {
        return this.mExpandableListAdapter.areAllItemsEnabled();
    }

    public boolean isEnabled(int flatListPos) {
        boolean retValue;
        PositionMetadata metadata = getUnflattenedPos(flatListPos);
        ExpandableListPosition pos = metadata.position;
        if (pos.type == 1) {
            retValue = this.mExpandableListAdapter.isChildSelectable(pos.groupPos, pos.childPos);
        } else {
            retValue = true;
        }
        metadata.recycle();
        return retValue;
    }

    public int getCount() {
        return this.mExpandableListAdapter.getGroupCount() + this.mTotalExpChildrenCount;
    }

    public Object getItem(int flatListPos) {
        Object retValue;
        PositionMetadata posMetadata = getUnflattenedPos(flatListPos);
        if (posMetadata.position.type == 2) {
            retValue = this.mExpandableListAdapter.getGroup(posMetadata.position.groupPos);
        } else if (posMetadata.position.type == 1) {
            retValue = this.mExpandableListAdapter.getChild(posMetadata.position.groupPos, posMetadata.position.childPos);
        } else {
            throw new RuntimeException("Flat list position is of unknown type");
        }
        posMetadata.recycle();
        return retValue;
    }

    public long getItemId(int flatListPos) {
        long retValue;
        PositionMetadata posMetadata = getUnflattenedPos(flatListPos);
        long groupId = this.mExpandableListAdapter.getGroupId(posMetadata.position.groupPos);
        if (posMetadata.position.type == 2) {
            retValue = this.mExpandableListAdapter.getCombinedGroupId(groupId);
        } else if (posMetadata.position.type == 1) {
            retValue = this.mExpandableListAdapter.getCombinedChildId(groupId, this.mExpandableListAdapter.getChildId(posMetadata.position.groupPos, posMetadata.position.childPos));
        } else {
            throw new RuntimeException("Flat list position is of unknown type");
        }
        posMetadata.recycle();
        return retValue;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "XiaoKang.Feng@Plf.SDK : 2015-06-24 : Modify for color style expandable list", property = OppoRomType.ROM)
    public View getView(int flatListPos, View convertView, ViewGroup parent) {
        View retValue;
        PositionMetadata posMetadata = getUnflattenedPos(flatListPos);
        if (posMetadata.position.type == 2) {
            retValue = this.mExpandableListAdapter.getGroupView(posMetadata.position.groupPos, posMetadata.isExpanded(), convertView, parent);
        } else if (posMetadata.position.type == 1) {
            retValue = this.mExpandableListAdapter.getChildView(posMetadata.position.groupPos, posMetadata.position.childPos, posMetadata.groupMetadata.lastChildFlPos == flatListPos, convertView, parent);
            if (retValue != null && this.mExpandAnimationEnabled) {
                boolean isfirst = false;
                if (posMetadata.position.childPos == 0) {
                    isfirst = true;
                }
                ColorExpandListAnimation animation;
                if (this.tmpExpandGroupPos == posMetadata.position.groupPos) {
                    animation = new ColorExpandListAnimation(this, retValue, 1, isfirst);
                    animation.setDuration(this.mDuration);
                    animation.start();
                } else if (this.tmpCollapseGroupPos == posMetadata.position.groupPos) {
                    animation = new ColorExpandListAnimation(this, retValue, 0, isfirst);
                    animation.setDuration(this.mDuration);
                    animation.start();
                }
            }
        } else {
            throw new RuntimeException("Flat list position is of unknown type");
        }
        posMetadata.recycle();
        return retValue;
    }

    public int getItemViewType(int flatListPos) {
        int retValue;
        PositionMetadata metadata = getUnflattenedPos(flatListPos);
        ExpandableListPosition pos = metadata.position;
        if (this.mExpandableListAdapter instanceof HeterogeneousExpandableList) {
            HeterogeneousExpandableList adapter = this.mExpandableListAdapter;
            if (pos.type == 2) {
                retValue = adapter.getGroupType(pos.groupPos);
            } else {
                retValue = adapter.getGroupTypeCount() + adapter.getChildType(pos.groupPos, pos.childPos);
            }
        } else if (pos.type == 2) {
            retValue = 0;
        } else {
            retValue = 1;
        }
        metadata.recycle();
        return retValue;
    }

    public int getViewTypeCount() {
        if (!(this.mExpandableListAdapter instanceof HeterogeneousExpandableList)) {
            return 2;
        }
        HeterogeneousExpandableList adapter = this.mExpandableListAdapter;
        return adapter.getGroupTypeCount() + adapter.getChildTypeCount();
    }

    public boolean hasStableIds() {
        return this.mExpandableListAdapter.hasStableIds();
    }

    private void refreshExpGroupMetadataList(boolean forceChildrenCountRefresh, boolean syncGroupPositions) {
        int i;
        GroupMetadata curGm;
        ArrayList<GroupMetadata> egml = this.mExpGroupMetadataList;
        int egmlSize = egml.size();
        int curFlPos = 0;
        this.mTotalExpChildrenCount = 0;
        if (syncGroupPositions) {
            boolean positionsChanged = false;
            for (i = egmlSize - 1; i >= 0; i--) {
                curGm = (GroupMetadata) egml.get(i);
                int newGPos = findGroupPosition(curGm.gId, curGm.gPos);
                if (newGPos != curGm.gPos) {
                    if (newGPos == -1) {
                        egml.remove(i);
                        egmlSize--;
                    }
                    curGm.gPos = newGPos;
                    if (!positionsChanged) {
                        positionsChanged = true;
                    }
                }
            }
            if (positionsChanged) {
                Collections.sort(egml);
            }
        }
        int lastGPos = 0;
        for (i = 0; i < egmlSize; i++) {
            int gChildrenCount;
            curGm = (GroupMetadata) egml.get(i);
            if (curGm.lastChildFlPos == -1 || forceChildrenCountRefresh) {
                gChildrenCount = this.mExpandableListAdapter.getChildrenCount(curGm.gPos);
            } else {
                gChildrenCount = curGm.lastChildFlPos - curGm.flPos;
            }
            this.mTotalExpChildrenCount += gChildrenCount;
            curFlPos += curGm.gPos - lastGPos;
            lastGPos = curGm.gPos;
            curGm.flPos = curFlPos;
            curFlPos += gChildrenCount;
            curGm.lastChildFlPos = curFlPos;
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "XiaoKang.Feng@Plf.SDK : 2015-06-24 : Modify for color style expandable list", property = OppoRomType.ROM)
    boolean collapseGroup(int groupPos) {
        ExpandableListPosition elGroupPos = ExpandableListPosition.obtain(2, groupPos, -1, -1);
        PositionMetadata pm = getFlattenedPos(elGroupPos);
        elGroupPos.recycle();
        if (pm == null) {
            return false;
        }
        boolean retValue = collapseGroup(pm);
        if (!this.mExpandAnimationEnabled) {
            pm.recycle();
        }
        return retValue;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "XiaoKang.Feng@Plf.SDK : 2015-06-24 : Modify for color style expandable list", property = OppoRomType.ROM)
    boolean collapseGroup(PositionMetadata posMetadata) {
        if (posMetadata.groupMetadata == null) {
            return false;
        }
        if (this.mExpandAnimationEnabled) {
            this.mPositionMetadata = posMetadata;
            this.tmpExpandGroupPos = -1;
            this.tmpCollapseGroupPos = posMetadata.groupMetadata.gPos;
            this.mDuration = getCollapseDuration(this.mExpandableListAdapter.getChildrenCount(this.tmpCollapseGroupPos));
            notifyDataSetChanged();
            this.mExpandableListAdapter.onGroupCollapsed(posMetadata.groupMetadata.gPos);
            if (!(this.mColorExpandListRunnable == null || this.mHandler == null)) {
                this.mHandler.removeCallbacks(this.mColorExpandListRunnable);
            }
            this.mColorExpandListRunnable = new AnonymousClass1(this);
            if (this.mHandler != null) {
                this.mHandler.postDelayed(this.mColorExpandListRunnable, this.mDuration);
            }
            return true;
        }
        this.mExpGroupMetadataList.remove(posMetadata.groupMetadata);
        refreshExpGroupMetadataList(false, false);
        notifyDataSetChanged();
        this.mExpandableListAdapter.onGroupCollapsed(posMetadata.groupMetadata.gPos);
        return true;
    }

    boolean expandGroup(int groupPos) {
        ExpandableListPosition elGroupPos = ExpandableListPosition.obtain(2, groupPos, -1, -1);
        PositionMetadata pm = getFlattenedPos(elGroupPos);
        elGroupPos.recycle();
        boolean retValue = expandGroup(pm);
        pm.recycle();
        return retValue;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "XiaoKang.Feng@Plf.SDK : 2015-06-24 : Modify for color style expandable list", property = OppoRomType.ROM)
    boolean expandGroup(PositionMetadata posMetadata) {
        if (posMetadata.position.groupPos < 0) {
            throw new RuntimeException("Need group");
        } else if (this.mMaxExpGroupCount == 0 || posMetadata.groupMetadata != null) {
            return false;
        } else {
            if (this.mExpandAnimationEnabled) {
                this.tmpExpandGroupPos = posMetadata.position.groupPos;
                this.tmpCollapseGroupPos = -1;
                if (!(this.mColorExpandListRunnable == null || this.mHandler == null)) {
                    this.mHandler.removeCallbacks(this.mColorExpandListRunnable);
                }
                this.mDuration = getExpandDuration(this.mExpandableListAdapter.getChildrenCount(this.tmpExpandGroupPos));
                this.mColorExpandListRunnable = new AnonymousClass2(this);
                if (this.mHandler != null) {
                    this.mHandler.postDelayed(this.mColorExpandListRunnable, this.mDuration);
                }
            }
            if (this.mExpGroupMetadataList.size() >= this.mMaxExpGroupCount) {
                GroupMetadata collapsedGm = (GroupMetadata) this.mExpGroupMetadataList.get(0);
                int collapsedIndex = this.mExpGroupMetadataList.indexOf(collapsedGm);
                collapseGroup(collapsedGm.gPos);
                if (posMetadata.groupInsertIndex > collapsedIndex) {
                    posMetadata.groupInsertIndex--;
                }
            }
            GroupMetadata expandedGm = GroupMetadata.obtain(-1, -1, posMetadata.position.groupPos, this.mExpandableListAdapter.getGroupId(posMetadata.position.groupPos));
            this.mExpGroupMetadataList.add(posMetadata.groupInsertIndex, expandedGm);
            refreshExpGroupMetadataList(false, false);
            notifyDataSetChanged();
            this.mExpandableListAdapter.onGroupExpanded(expandedGm.gPos);
            return true;
        }
    }

    public boolean isGroupExpanded(int groupPosition) {
        for (int i = this.mExpGroupMetadataList.size() - 1; i >= 0; i--) {
            if (((GroupMetadata) this.mExpGroupMetadataList.get(i)).gPos == groupPosition) {
                return true;
            }
        }
        return false;
    }

    public void setMaxExpGroupCount(int maxExpGroupCount) {
        this.mMaxExpGroupCount = maxExpGroupCount;
    }

    ExpandableListAdapter getAdapter() {
        return this.mExpandableListAdapter;
    }

    public Filter getFilter() {
        ExpandableListAdapter adapter = getAdapter();
        if (adapter instanceof Filterable) {
            return ((Filterable) adapter).getFilter();
        }
        return null;
    }

    ArrayList<GroupMetadata> getExpandedGroupMetadataList() {
        return this.mExpGroupMetadataList;
    }

    void setExpandedGroupMetadataList(ArrayList<GroupMetadata> expandedGroupMetadataList) {
        if (expandedGroupMetadataList != null && this.mExpandableListAdapter != null) {
            int numGroups = this.mExpandableListAdapter.getGroupCount();
            int i = expandedGroupMetadataList.size() - 1;
            while (i >= 0) {
                if (((GroupMetadata) expandedGroupMetadataList.get(i)).gPos < numGroups) {
                    i--;
                } else {
                    return;
                }
            }
            this.mExpGroupMetadataList = expandedGroupMetadataList;
            refreshExpGroupMetadataList(true, false);
        }
    }

    public boolean isEmpty() {
        ExpandableListAdapter adapter = getAdapter();
        return adapter != null ? adapter.isEmpty() : true;
    }

    int findGroupPosition(long groupIdToMatch, int seedGroupPosition) {
        int count = this.mExpandableListAdapter.getGroupCount();
        if (count == 0) {
            return -1;
        }
        if (groupIdToMatch == Long.MIN_VALUE) {
            return -1;
        }
        seedGroupPosition = Math.min(count - 1, Math.max(0, seedGroupPosition));
        long endTime = SystemClock.uptimeMillis() + 100;
        int first = seedGroupPosition;
        int last = seedGroupPosition;
        boolean next = false;
        ExpandableListAdapter adapter = getAdapter();
        if (adapter == null) {
            return -1;
        }
        while (SystemClock.uptimeMillis() <= endTime) {
            if (adapter.getGroupId(seedGroupPosition) != groupIdToMatch) {
                boolean hitLast = last == count + -1;
                boolean hitFirst = first == 0;
                if (hitLast && hitFirst) {
                    break;
                } else if (hitFirst || (next && !hitLast)) {
                    last++;
                    seedGroupPosition = last;
                    next = false;
                } else if (hitLast || !(next || hitFirst)) {
                    first--;
                    seedGroupPosition = first;
                    next = true;
                }
            } else {
                return seedGroupPosition;
            }
        }
        return -1;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK, 2016-01-07 : Add for color style expandable list", property = OppoRomType.ROM)
    private long getExpandDuration(int count) {
        return getAnimationDuration(count, 200, 300, 600);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK, 2016-01-07 : Add for color style expandable list", property = OppoRomType.ROM)
    private long getCollapseDuration(int count) {
        return getAnimationDuration(count, 200, 300, 400);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK, 2016-01-07 : Add for color style expandable list", property = OppoRomType.ROM)
    private long getAnimationDuration(int count, int min, int middle, int max) {
        if (count <= 5) {
            return (long) ((((middle - min) * count) / 5) + min);
        }
        if (count >= 12) {
            return (long) max;
        }
        return (long) ((((max - middle) * (count - 5)) / 7) + middle);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK, 2015-06-24 : Add for color style expandable list", property = OppoRomType.ROM)
    public boolean isExpandAnimationEnabled() {
        return this.mExpandAnimationEnabled;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK, 2015-06-24 : Add for color style expandable list", property = OppoRomType.ROM)
    public void setExpandAnimationEnabled(boolean enabled) {
        if (this.mExpandAnimationEnabled != enabled) {
            this.mExpandAnimationEnabled = enabled;
            if (this.mHandler == null && this.mExpandAnimationEnabled) {
                Looper looper = Looper.myLooper();
                if (looper != null) {
                    this.mHandler = new Handler(looper);
                    return;
                }
                looper = Looper.getMainLooper();
                if (looper != null) {
                    this.mHandler = new Handler(looper);
                } else {
                    this.mHandler = null;
                }
            }
        }
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK, 2015-06-24 : Add for color style expandable list", property = OppoRomType.ROM)
    boolean colorCollapseGroup(PositionMetadata posMetadata) {
        if (posMetadata.groupMetadata == null || (posMetadata.groupMetadata.gPos != this.tmpCollapseGroupPos && this.tmpCollapseGroupPos >= 0)) {
            if (this.tmpCollapseGroupPos < 0) {
                return false;
            }
            ExpandableListPosition elGroupPos = ExpandableListPosition.obtain(2, this.tmpCollapseGroupPos, -1, -1);
            PositionMetadata pm = getFlattenedPos(elGroupPos);
            elGroupPos.recycle();
            if (pm == null) {
                return false;
            }
            posMetadata = pm;
        }
        this.mExpGroupMetadataList.remove(posMetadata.groupMetadata);
        refreshExpGroupMetadataList(false, false);
        notifyDataSetChanged();
        posMetadata.recycle();
        return true;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK, 2015-06-24 : Add for color style expandable list", property = OppoRomType.ROM)
    int calChildrenBottomPosition(int position, int height) {
        if ((this.tmpExpandGroupPos == -1 && this.tmpCollapseGroupPos == -1) || height <= 0) {
            return height;
        }
        PositionMetadata metadata = getUnflattenedPos(position);
        int grouppos = metadata.position.groupPos;
        int childpos = metadata.position.childPos;
        int childCount = this.mExpandableListAdapter.getChildrenCount(grouppos);
        if (childCount >= 100) {
            return height;
        }
        if ((grouppos != this.tmpExpandGroupPos && grouppos != this.tmpCollapseGroupPos) || this.mHeightOffset >= ((float) (childpos + 1)) * (1.0f / ((float) childCount))) {
            return height;
        }
        if (this.mHeightOffset <= ((float) childpos) * (1.0f / ((float) childCount))) {
            return 0;
        }
        return (int) (((float) height) * ((this.mHeightOffset * ((float) childCount)) - ((float) childpos)));
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK, 2015-06-24 : Add for color style expandable list", property = OppoRomType.ROM)
    boolean isExpandCollapse(int position) {
        if (!this.mExpandAnimationEnabled) {
            return false;
        }
        PositionMetadata metadata = getUnflattenedPos(position);
        if (metadata != null) {
            int grouppos = metadata.position.groupPos;
            if (grouppos == this.tmpExpandGroupPos || this.tmpCollapseGroupPos == grouppos) {
                return true;
            }
        }
        return false;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK, 2015-06-24 : Add for color style expandable list", property = OppoRomType.ROM)
    boolean isExpandingGroup(int grouppos) {
        return grouppos == this.tmpExpandGroupPos;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK, 2015-06-24 : Add for color style expandable list", property = OppoRomType.ROM)
    boolean isCollapsingGroup(int grouppos) {
        return grouppos == this.tmpCollapseGroupPos;
    }
}
