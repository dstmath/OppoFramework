package android.widget;

import android.view.LayoutInflater;
import java.util.List;
import java.util.Map;

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
public class SimpleExpandableListAdapter extends BaseExpandableListAdapter {
    private List<? extends List<? extends Map<String, ?>>> mChildData;
    private String[] mChildFrom;
    private int mChildLayout;
    private int[] mChildTo;
    private int mCollapsedGroupLayout;
    private int mExpandedGroupLayout;
    private List<? extends Map<String, ?>> mGroupData;
    private String[] mGroupFrom;
    private int[] mGroupTo;
    private LayoutInflater mInflater;
    private int mLastChildLayout;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.SimpleExpandableListAdapter.<init>(android.content.Context, java.util.List, int, int, java.lang.String[], int[], java.util.List, int, int, java.lang.String[], int[]):void, dex:  in method: android.widget.SimpleExpandableListAdapter.<init>(android.content.Context, java.util.List, int, int, java.lang.String[], int[], java.util.List, int, int, java.lang.String[], int[]):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.SimpleExpandableListAdapter.<init>(android.content.Context, java.util.List, int, int, java.lang.String[], int[], java.util.List, int, int, java.lang.String[], int[]):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public SimpleExpandableListAdapter(android.content.Context r1, java.util.List<? extends java.util.Map<java.lang.String, ?>> r2, int r3, int r4, java.lang.String[] r5, int[] r6, java.util.List<? extends java.util.List<? extends java.util.Map<java.lang.String, ?>>> r7, int r8, int r9, java.lang.String[] r10, int[] r11) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.widget.SimpleExpandableListAdapter.<init>(android.content.Context, java.util.List, int, int, java.lang.String[], int[], java.util.List, int, int, java.lang.String[], int[]):void, dex:  in method: android.widget.SimpleExpandableListAdapter.<init>(android.content.Context, java.util.List, int, int, java.lang.String[], int[], java.util.List, int, int, java.lang.String[], int[]):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleExpandableListAdapter.<init>(android.content.Context, java.util.List, int, int, java.lang.String[], int[], java.util.List, int, int, java.lang.String[], int[]):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.SimpleExpandableListAdapter.<init>(android.content.Context, java.util.List, int, int, java.lang.String[], int[], java.util.List, int, java.lang.String[], int[]):void, dex: 
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
    public SimpleExpandableListAdapter(android.content.Context r1, java.util.List<? extends java.util.Map<java.lang.String, ?>> r2, int r3, int r4, java.lang.String[] r5, int[] r6, java.util.List<? extends java.util.List<? extends java.util.Map<java.lang.String, ?>>> r7, int r8, java.lang.String[] r9, int[] r10) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.SimpleExpandableListAdapter.<init>(android.content.Context, java.util.List, int, int, java.lang.String[], int[], java.util.List, int, java.lang.String[], int[]):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleExpandableListAdapter.<init>(android.content.Context, java.util.List, int, int, java.lang.String[], int[], java.util.List, int, java.lang.String[], int[]):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.SimpleExpandableListAdapter.<init>(android.content.Context, java.util.List, int, java.lang.String[], int[], java.util.List, int, java.lang.String[], int[]):void, dex: 
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
    public SimpleExpandableListAdapter(android.content.Context r1, java.util.List<? extends java.util.Map<java.lang.String, ?>> r2, int r3, java.lang.String[] r4, int[] r5, java.util.List<? extends java.util.List<? extends java.util.Map<java.lang.String, ?>>> r6, int r7, java.lang.String[] r8, int[] r9) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.SimpleExpandableListAdapter.<init>(android.content.Context, java.util.List, int, java.lang.String[], int[], java.util.List, int, java.lang.String[], int[]):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleExpandableListAdapter.<init>(android.content.Context, java.util.List, int, java.lang.String[], int[], java.util.List, int, java.lang.String[], int[]):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.SimpleExpandableListAdapter.bindView(android.view.View, java.util.Map, java.lang.String[], int[]):void, dex:  in method: android.widget.SimpleExpandableListAdapter.bindView(android.view.View, java.util.Map, java.lang.String[], int[]):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.SimpleExpandableListAdapter.bindView(android.view.View, java.util.Map, java.lang.String[], int[]):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:752)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void bindView(android.view.View r1, java.util.Map<java.lang.String, ?> r2, java.lang.String[] r3, int[] r4) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.widget.SimpleExpandableListAdapter.bindView(android.view.View, java.util.Map, java.lang.String[], int[]):void, dex:  in method: android.widget.SimpleExpandableListAdapter.bindView(android.view.View, java.util.Map, java.lang.String[], int[]):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleExpandableListAdapter.bindView(android.view.View, java.util.Map, java.lang.String[], int[]):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleExpandableListAdapter.getChild(int, int):java.lang.Object, dex: 
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
    public java.lang.Object getChild(int r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleExpandableListAdapter.getChild(int, int):java.lang.Object, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleExpandableListAdapter.getChild(int, int):java.lang.Object");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.SimpleExpandableListAdapter.getChildView(int, int, boolean, android.view.View, android.view.ViewGroup):android.view.View, dex: 
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
    public android.view.View getChildView(int r1, int r2, boolean r3, android.view.View r4, android.view.ViewGroup r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.SimpleExpandableListAdapter.getChildView(int, int, boolean, android.view.View, android.view.ViewGroup):android.view.View, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleExpandableListAdapter.getChildView(int, int, boolean, android.view.View, android.view.ViewGroup):android.view.View");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleExpandableListAdapter.getChildrenCount(int):int, dex: 
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
    public int getChildrenCount(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleExpandableListAdapter.getChildrenCount(int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleExpandableListAdapter.getChildrenCount(int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleExpandableListAdapter.getGroup(int):java.lang.Object, dex: 
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
    public java.lang.Object getGroup(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleExpandableListAdapter.getGroup(int):java.lang.Object, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleExpandableListAdapter.getGroup(int):java.lang.Object");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleExpandableListAdapter.getGroupCount():int, dex: 
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
    public int getGroupCount() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleExpandableListAdapter.getGroupCount():int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleExpandableListAdapter.getGroupCount():int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.SimpleExpandableListAdapter.getGroupView(int, boolean, android.view.View, android.view.ViewGroup):android.view.View, dex: 
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
    public android.view.View getGroupView(int r1, boolean r2, android.view.View r3, android.view.ViewGroup r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.SimpleExpandableListAdapter.getGroupView(int, boolean, android.view.View, android.view.ViewGroup):android.view.View, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleExpandableListAdapter.getGroupView(int, boolean, android.view.View, android.view.ViewGroup):android.view.View");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleExpandableListAdapter.newChildView(boolean, android.view.ViewGroup):android.view.View, dex: 
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
    public android.view.View newChildView(boolean r1, android.view.ViewGroup r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleExpandableListAdapter.newChildView(boolean, android.view.ViewGroup):android.view.View, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleExpandableListAdapter.newChildView(boolean, android.view.ViewGroup):android.view.View");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.SimpleExpandableListAdapter.newGroupView(boolean, android.view.ViewGroup):android.view.View, dex:  in method: android.widget.SimpleExpandableListAdapter.newGroupView(boolean, android.view.ViewGroup):android.view.View, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.SimpleExpandableListAdapter.newGroupView(boolean, android.view.ViewGroup):android.view.View, dex: 
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
    public android.view.View newGroupView(boolean r1, android.view.ViewGroup r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.widget.SimpleExpandableListAdapter.newGroupView(boolean, android.view.ViewGroup):android.view.View, dex:  in method: android.widget.SimpleExpandableListAdapter.newGroupView(boolean, android.view.ViewGroup):android.view.View, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleExpandableListAdapter.newGroupView(boolean, android.view.ViewGroup):android.view.View");
    }

    public long getChildId(int groupPosition, int childPosition) {
        return (long) childPosition;
    }

    public long getGroupId(int groupPosition) {
        return (long) groupPosition;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean hasStableIds() {
        return true;
    }
}
