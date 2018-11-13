package com.android.internal.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View.OnKeyListener;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

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
public class PlatLogoActivity extends Activity {
    public static final boolean FINISH = false;
    public static final boolean REVEAL_THE_NAME = false;
    PathInterpolator mInterpolator;
    int mKeyCount;
    FrameLayout mLayout;
    int mTapCount;

    /* renamed from: com.android.internal.app.PlatLogoActivity$1 */
    class AnonymousClass1 implements OnKeyListener {
        final /* synthetic */ PlatLogoActivity this$0;
        final /* synthetic */ ImageView val$im;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.PlatLogoActivity.1.<init>(com.android.internal.app.PlatLogoActivity, android.widget.ImageView):void, dex: 
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
        AnonymousClass1(com.android.internal.app.PlatLogoActivity r1, android.widget.ImageView r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.app.PlatLogoActivity.1.<init>(com.android.internal.app.PlatLogoActivity, android.widget.ImageView):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.PlatLogoActivity.1.<init>(com.android.internal.app.PlatLogoActivity, android.widget.ImageView):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.PlatLogoActivity.1.onKey(android.view.View, int, android.view.KeyEvent):boolean, dex: 
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
        public boolean onKey(android.view.View r1, int r2, android.view.KeyEvent r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.app.PlatLogoActivity.1.onKey(android.view.View, int, android.view.KeyEvent):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.PlatLogoActivity.1.onKey(android.view.View, int, android.view.KeyEvent):boolean");
        }
    }

    public PlatLogoActivity() {
        this.mInterpolator = new PathInterpolator(0.0f, 0.0f, 0.5f, 1.0f);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mLayout = new FrameLayout(this);
        setContentView(this.mLayout);
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
    public void onAttachedToWindow() {
        /*
        r11 = this;
        r10 = 1;
        r7 = 1056964608; // 0x3f000000 float:0.5 double:5.222099017E-315;
        r9 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r5 = r11.getResources();
        r0 = r5.getDisplayMetrics();
        r1 = r0.density;
        r5 = r0.widthPixels;
        r6 = r0.heightPixels;
        r5 = java.lang.Math.min(r5, r6);
        r5 = (float) r5;
        r6 = 1142292480; // 0x44160000 float:600.0 double:5.64367472E-315;
        r6 = r6 * r1;
        r5 = java.lang.Math.min(r5, r6);
        r6 = 1120403456; // 0x42c80000 float:100.0 double:5.53552857E-315;
        r6 = r6 * r1;
        r5 = r5 - r6;
        r4 = (int) r5;
        r2 = new android.widget.ImageView;
        r2.<init>(r11);
        r5 = 1109393408; // 0x42200000 float:40.0 double:5.481131706E-315;
        r5 = r5 * r1;
        r3 = (int) r5;
        r2.setPadding(r3, r3, r3, r3);
        r5 = 1101004800; // 0x41a00000 float:20.0 double:5.439686476E-315;
        r2.setTranslationZ(r5);
        r2.setScaleX(r7);
        r2.setScaleY(r7);
        r5 = 0;
        r2.setAlpha(r5);
        r5 = new android.graphics.drawable.RippleDrawable;
        r6 = -1;
        r6 = android.content.res.ColorStateList.valueOf(r6);
        r7 = 17302876; // 0x108055c float:2.49831E-38 double:8.5487566E-317;
        r7 = r11.getDrawable(r7);
        r8 = 0;
        r5.<init>(r6, r7, r8);
        r2.setBackground(r5);
        r2.setClickable(r10);
        r2.setFocusable(r10);
        r2.requestFocus();
        r5 = new com.android.internal.app.PlatLogoActivity$1;
        r5.<init>(r11, r2);
        r2.setOnKeyListener(r5);
        r5 = r11.mLayout;
        r6 = new android.widget.FrameLayout$LayoutParams;
        r7 = 17;
        r6.<init>(r4, r4, r7);
        r5.addView(r2, r6);
        r5 = r2.animate();
        r5 = r5.scaleX(r9);
        r5 = r5.scaleY(r9);
        r5 = r5.alpha(r9);
        r6 = r11.mInterpolator;
        r5 = r5.setInterpolator(r6);
        r6 = 500; // 0x1f4 float:7.0E-43 double:2.47E-321;
        r5 = r5.setDuration(r6);
        r6 = 800; // 0x320 float:1.121E-42 double:3.953E-321;
        r5 = r5.setStartDelay(r6);
        r5.start();
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.PlatLogoActivity.onAttachedToWindow():void");
    }
}
