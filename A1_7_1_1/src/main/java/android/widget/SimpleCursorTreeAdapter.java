package android.widget;

import android.database.Cursor;
import android.view.View;

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
public abstract class SimpleCursorTreeAdapter extends ResourceCursorTreeAdapter {
    private int[] mChildFrom;
    private String[] mChildFromNames;
    private int[] mChildTo;
    private int[] mGroupFrom;
    private String[] mGroupFromNames;
    private int[] mGroupTo;
    private ViewBinder mViewBinder;

    public interface ViewBinder {
        boolean setViewValue(View view, Cursor cursor, int i);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.SimpleCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, int, java.lang.String[], int[], int, int, java.lang.String[], int[]):void, dex: 
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
    public SimpleCursorTreeAdapter(android.content.Context r1, android.database.Cursor r2, int r3, int r4, java.lang.String[] r5, int[] r6, int r7, int r8, java.lang.String[] r9, int[] r10) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.SimpleCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, int, java.lang.String[], int[], int, int, java.lang.String[], int[]):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, int, java.lang.String[], int[], int, int, java.lang.String[], int[]):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.SimpleCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, int, java.lang.String[], int[], int, java.lang.String[], int[]):void, dex: 
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
    public SimpleCursorTreeAdapter(android.content.Context r1, android.database.Cursor r2, int r3, int r4, java.lang.String[] r5, int[] r6, int r7, java.lang.String[] r8, int[] r9) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.SimpleCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, int, java.lang.String[], int[], int, java.lang.String[], int[]):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, int, java.lang.String[], int[], int, java.lang.String[], int[]):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.SimpleCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, java.lang.String[], int[], int, java.lang.String[], int[]):void, dex: 
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
    public SimpleCursorTreeAdapter(android.content.Context r1, android.database.Cursor r2, int r3, java.lang.String[] r4, int[] r5, int r6, java.lang.String[] r7, int[] r8) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.SimpleCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, java.lang.String[], int[], int, java.lang.String[], int[]):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleCursorTreeAdapter.<init>(android.content.Context, android.database.Cursor, int, java.lang.String[], int[], int, java.lang.String[], int[]):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleCursorTreeAdapter.bindView(android.view.View, android.content.Context, android.database.Cursor, int[], int[]):void, dex: 
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
    private void bindView(android.view.View r1, android.content.Context r2, android.database.Cursor r3, int[] r4, int[] r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleCursorTreeAdapter.bindView(android.view.View, android.content.Context, android.database.Cursor, int[], int[]):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleCursorTreeAdapter.bindView(android.view.View, android.content.Context, android.database.Cursor, int[], int[]):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.SimpleCursorTreeAdapter.init(java.lang.String[], int[], java.lang.String[], int[]):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private void init(java.lang.String[] r1, int[] r2, java.lang.String[] r3, int[] r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.SimpleCursorTreeAdapter.init(java.lang.String[], int[], java.lang.String[], int[]):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleCursorTreeAdapter.init(java.lang.String[], int[], java.lang.String[], int[]):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.SimpleCursorTreeAdapter.initFromColumns(android.database.Cursor, java.lang.String[], int[]):void, dex: 
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
    private void initFromColumns(android.database.Cursor r1, java.lang.String[] r2, int[] r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.SimpleCursorTreeAdapter.initFromColumns(android.database.Cursor, java.lang.String[], int[]):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleCursorTreeAdapter.initFromColumns(android.database.Cursor, java.lang.String[], int[]):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleCursorTreeAdapter.bindChildView(android.view.View, android.content.Context, android.database.Cursor, boolean):void, dex: 
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
    protected void bindChildView(android.view.View r1, android.content.Context r2, android.database.Cursor r3, boolean r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleCursorTreeAdapter.bindChildView(android.view.View, android.content.Context, android.database.Cursor, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleCursorTreeAdapter.bindChildView(android.view.View, android.content.Context, android.database.Cursor, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleCursorTreeAdapter.bindGroupView(android.view.View, android.content.Context, android.database.Cursor, boolean):void, dex: 
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
    protected void bindGroupView(android.view.View r1, android.content.Context r2, android.database.Cursor r3, boolean r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleCursorTreeAdapter.bindGroupView(android.view.View, android.content.Context, android.database.Cursor, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleCursorTreeAdapter.bindGroupView(android.view.View, android.content.Context, android.database.Cursor, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleCursorTreeAdapter.getViewBinder():android.widget.SimpleCursorTreeAdapter$ViewBinder, dex: 
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
    public android.widget.SimpleCursorTreeAdapter.ViewBinder getViewBinder() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.SimpleCursorTreeAdapter.getViewBinder():android.widget.SimpleCursorTreeAdapter$ViewBinder, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleCursorTreeAdapter.getViewBinder():android.widget.SimpleCursorTreeAdapter$ViewBinder");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.SimpleCursorTreeAdapter.setViewBinder(android.widget.SimpleCursorTreeAdapter$ViewBinder):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public void setViewBinder(android.widget.SimpleCursorTreeAdapter.ViewBinder r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.SimpleCursorTreeAdapter.setViewBinder(android.widget.SimpleCursorTreeAdapter$ViewBinder):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleCursorTreeAdapter.setViewBinder(android.widget.SimpleCursorTreeAdapter$ViewBinder):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.SimpleCursorTreeAdapter.setViewImage(android.widget.ImageView, java.lang.String):void, dex: 
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
    protected void setViewImage(android.widget.ImageView r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.SimpleCursorTreeAdapter.setViewImage(android.widget.ImageView, java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleCursorTreeAdapter.setViewImage(android.widget.ImageView, java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.SimpleCursorTreeAdapter.setViewText(android.widget.TextView, java.lang.String):void, dex: 
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
    public void setViewText(android.widget.TextView r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.SimpleCursorTreeAdapter.setViewText(android.widget.TextView, java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SimpleCursorTreeAdapter.setViewText(android.widget.TextView, java.lang.String):void");
    }
}
