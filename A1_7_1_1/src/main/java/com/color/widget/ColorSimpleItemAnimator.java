package com.color.widget;

import com.color.widget.ColorRecyclerView.ItemAnimator;
import com.color.widget.ColorRecyclerView.ViewHolder;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public abstract class ColorSimpleItemAnimator extends ItemAnimator {
    private static final boolean DEBUG = false;
    private static final String TAG = "SimpleItemAnimator";
    boolean mSupportsChangeAnimations;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorSimpleItemAnimator.<init>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public ColorSimpleItemAnimator() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorSimpleItemAnimator.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.<init>():void");
    }

    public abstract boolean animateAdd(ViewHolder viewHolder);

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorSimpleItemAnimator.animateAppearance(com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public boolean animateAppearance(com.color.widget.ColorRecyclerView.ViewHolder r1, com.color.widget.ColorRecyclerView.ItemAnimator.ItemHolderInfo r2, com.color.widget.ColorRecyclerView.ItemAnimator.ItemHolderInfo r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorSimpleItemAnimator.animateAppearance(com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.animateAppearance(com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo):boolean");
    }

    public abstract boolean animateChange(ViewHolder viewHolder, ViewHolder viewHolder2, int i, int i2, int i3, int i4);

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorSimpleItemAnimator.animateChange(com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo):boolean, dex:  in method: com.color.widget.ColorSimpleItemAnimator.animateChange(com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorSimpleItemAnimator.animateChange(com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo):boolean, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public boolean animateChange(com.color.widget.ColorRecyclerView.ViewHolder r1, com.color.widget.ColorRecyclerView.ViewHolder r2, com.color.widget.ColorRecyclerView.ItemAnimator.ItemHolderInfo r3, com.color.widget.ColorRecyclerView.ItemAnimator.ItemHolderInfo r4) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorSimpleItemAnimator.animateChange(com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo):boolean, dex:  in method: com.color.widget.ColorSimpleItemAnimator.animateChange(com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.animateChange(com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorSimpleItemAnimator.animateDisappearance(com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public boolean animateDisappearance(com.color.widget.ColorRecyclerView.ViewHolder r1, com.color.widget.ColorRecyclerView.ItemAnimator.ItemHolderInfo r2, com.color.widget.ColorRecyclerView.ItemAnimator.ItemHolderInfo r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorSimpleItemAnimator.animateDisappearance(com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.animateDisappearance(com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo):boolean");
    }

    public abstract boolean animateMove(ViewHolder viewHolder, int i, int i2, int i3, int i4);

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorSimpleItemAnimator.animatePersistence(com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public boolean animatePersistence(com.color.widget.ColorRecyclerView.ViewHolder r1, com.color.widget.ColorRecyclerView.ItemAnimator.ItemHolderInfo r2, com.color.widget.ColorRecyclerView.ItemAnimator.ItemHolderInfo r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorSimpleItemAnimator.animatePersistence(com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.animatePersistence(com.color.widget.ColorRecyclerView$ViewHolder, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo, com.color.widget.ColorRecyclerView$ItemAnimator$ItemHolderInfo):boolean");
    }

    public abstract boolean animateRemove(ViewHolder viewHolder);

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorSimpleItemAnimator.canReuseUpdatedViewHolder(com.color.widget.ColorRecyclerView$ViewHolder):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public boolean canReuseUpdatedViewHolder(com.color.widget.ColorRecyclerView.ViewHolder r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorSimpleItemAnimator.canReuseUpdatedViewHolder(com.color.widget.ColorRecyclerView$ViewHolder):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.canReuseUpdatedViewHolder(com.color.widget.ColorRecyclerView$ViewHolder):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleItemAnimator.dispatchAddFinished(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public final void dispatchAddFinished(com.color.widget.ColorRecyclerView.ViewHolder r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleItemAnimator.dispatchAddFinished(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.dispatchAddFinished(com.color.widget.ColorRecyclerView$ViewHolder):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleItemAnimator.dispatchAddStarting(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public final void dispatchAddStarting(com.color.widget.ColorRecyclerView.ViewHolder r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleItemAnimator.dispatchAddStarting(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.dispatchAddStarting(com.color.widget.ColorRecyclerView$ViewHolder):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleItemAnimator.dispatchChangeFinished(com.color.widget.ColorRecyclerView$ViewHolder, boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public final void dispatchChangeFinished(com.color.widget.ColorRecyclerView.ViewHolder r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleItemAnimator.dispatchChangeFinished(com.color.widget.ColorRecyclerView$ViewHolder, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.dispatchChangeFinished(com.color.widget.ColorRecyclerView$ViewHolder, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleItemAnimator.dispatchChangeStarting(com.color.widget.ColorRecyclerView$ViewHolder, boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public final void dispatchChangeStarting(com.color.widget.ColorRecyclerView.ViewHolder r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleItemAnimator.dispatchChangeStarting(com.color.widget.ColorRecyclerView$ViewHolder, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.dispatchChangeStarting(com.color.widget.ColorRecyclerView$ViewHolder, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleItemAnimator.dispatchMoveFinished(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public final void dispatchMoveFinished(com.color.widget.ColorRecyclerView.ViewHolder r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleItemAnimator.dispatchMoveFinished(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.dispatchMoveFinished(com.color.widget.ColorRecyclerView$ViewHolder):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleItemAnimator.dispatchMoveStarting(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public final void dispatchMoveStarting(com.color.widget.ColorRecyclerView.ViewHolder r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleItemAnimator.dispatchMoveStarting(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.dispatchMoveStarting(com.color.widget.ColorRecyclerView$ViewHolder):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleItemAnimator.dispatchRemoveFinished(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public final void dispatchRemoveFinished(com.color.widget.ColorRecyclerView.ViewHolder r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleItemAnimator.dispatchRemoveFinished(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.dispatchRemoveFinished(com.color.widget.ColorRecyclerView$ViewHolder):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleItemAnimator.dispatchRemoveStarting(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public final void dispatchRemoveStarting(com.color.widget.ColorRecyclerView.ViewHolder r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleItemAnimator.dispatchRemoveStarting(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.dispatchRemoveStarting(com.color.widget.ColorRecyclerView$ViewHolder):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorSimpleItemAnimator.getSupportsChangeAnimations():boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public boolean getSupportsChangeAnimations() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorSimpleItemAnimator.getSupportsChangeAnimations():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.getSupportsChangeAnimations():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleItemAnimator.onAddFinished(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
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
    public void onAddFinished(com.color.widget.ColorRecyclerView.ViewHolder r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleItemAnimator.onAddFinished(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.onAddFinished(com.color.widget.ColorRecyclerView$ViewHolder):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleItemAnimator.onAddStarting(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
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
    public void onAddStarting(com.color.widget.ColorRecyclerView.ViewHolder r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleItemAnimator.onAddStarting(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.onAddStarting(com.color.widget.ColorRecyclerView$ViewHolder):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleItemAnimator.onChangeFinished(com.color.widget.ColorRecyclerView$ViewHolder, boolean):void, dex: 
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
    public void onChangeFinished(com.color.widget.ColorRecyclerView.ViewHolder r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleItemAnimator.onChangeFinished(com.color.widget.ColorRecyclerView$ViewHolder, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.onChangeFinished(com.color.widget.ColorRecyclerView$ViewHolder, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleItemAnimator.onChangeStarting(com.color.widget.ColorRecyclerView$ViewHolder, boolean):void, dex: 
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
    public void onChangeStarting(com.color.widget.ColorRecyclerView.ViewHolder r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleItemAnimator.onChangeStarting(com.color.widget.ColorRecyclerView$ViewHolder, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.onChangeStarting(com.color.widget.ColorRecyclerView$ViewHolder, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleItemAnimator.onMoveFinished(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
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
    public void onMoveFinished(com.color.widget.ColorRecyclerView.ViewHolder r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleItemAnimator.onMoveFinished(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.onMoveFinished(com.color.widget.ColorRecyclerView$ViewHolder):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleItemAnimator.onMoveStarting(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
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
    public void onMoveStarting(com.color.widget.ColorRecyclerView.ViewHolder r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleItemAnimator.onMoveStarting(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.onMoveStarting(com.color.widget.ColorRecyclerView$ViewHolder):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleItemAnimator.onRemoveFinished(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
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
    public void onRemoveFinished(com.color.widget.ColorRecyclerView.ViewHolder r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleItemAnimator.onRemoveFinished(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.onRemoveFinished(com.color.widget.ColorRecyclerView$ViewHolder):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleItemAnimator.onRemoveStarting(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
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
    public void onRemoveStarting(com.color.widget.ColorRecyclerView.ViewHolder r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleItemAnimator.onRemoveStarting(com.color.widget.ColorRecyclerView$ViewHolder):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.onRemoveStarting(com.color.widget.ColorRecyclerView$ViewHolder):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorSimpleItemAnimator.setSupportsChangeAnimations(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void setSupportsChangeAnimations(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorSimpleItemAnimator.setSupportsChangeAnimations(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleItemAnimator.setSupportsChangeAnimations(boolean):void");
    }
}
