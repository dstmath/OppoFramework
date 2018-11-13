package android.widget;

import android.view.LayoutInflater;

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
public abstract class ResourceCursorTreeAdapter extends CursorTreeAdapter {
    private int mChildLayout;
    private int mCollapsedGroupLayout;
    private int mExpandedGroupLayout;
    private LayoutInflater mInflater;
    private int mLastChildLayout;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.ResourceCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, int):void, dex: 
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
    public ResourceCursorTreeAdapter(android.content.Context r1, android.database.Cursor r2, int r3, int r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.ResourceCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.ResourceCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.ResourceCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, int, int):void, dex: 
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
    public ResourceCursorTreeAdapter(android.content.Context r1, android.database.Cursor r2, int r3, int r4, int r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.ResourceCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.ResourceCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.widget.ResourceCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, int, int, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public ResourceCursorTreeAdapter(android.content.Context r1, android.database.Cursor r2, int r3, int r4, int r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.widget.ResourceCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, int, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.ResourceCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, int, int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.ResourceCursorTreeAdapter.newChildView(android.content.Context, android.database.Cursor, boolean, android.view.ViewGroup):android.view.View, dex:  in method: android.widget.ResourceCursorTreeAdapter.newChildView(android.content.Context, android.database.Cursor, boolean, android.view.ViewGroup):android.view.View, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.ResourceCursorTreeAdapter.newChildView(android.content.Context, android.database.Cursor, boolean, android.view.ViewGroup):android.view.View, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$23.decode(InstructionCodec.java:514)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public android.view.View newChildView(android.content.Context r1, android.database.Cursor r2, boolean r3, android.view.ViewGroup r4) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.widget.ResourceCursorTreeAdapter.newChildView(android.content.Context, android.database.Cursor, boolean, android.view.ViewGroup):android.view.View, dex:  in method: android.widget.ResourceCursorTreeAdapter.newChildView(android.content.Context, android.database.Cursor, boolean, android.view.ViewGroup):android.view.View, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.ResourceCursorTreeAdapter.newChildView(android.content.Context, android.database.Cursor, boolean, android.view.ViewGroup):android.view.View");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.ResourceCursorTreeAdapter.newGroupView(android.content.Context, android.database.Cursor, boolean, android.view.ViewGroup):android.view.View, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public android.view.View newGroupView(android.content.Context r1, android.database.Cursor r2, boolean r3, android.view.ViewGroup r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.ResourceCursorTreeAdapter.newGroupView(android.content.Context, android.database.Cursor, boolean, android.view.ViewGroup):android.view.View, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.ResourceCursorTreeAdapter.newGroupView(android.content.Context, android.database.Cursor, boolean, android.view.ViewGroup):android.view.View");
    }
}
