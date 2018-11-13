package com.color.app;

import android.content.Context;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import com.color.app.ColorAlertDialogCustom.ListItemAttr;

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
public class ColorAlertControllerCustom extends ColorAlertController {
    private static final String TAG = "ColorAlertControllerCustom";
    private boolean mIsDynaChange;
    private ListItemAttr[] mItemsAttrs;

    public static class AlertParams extends com.color.app.ColorAlertController.AlertParams {
        public ListItemAttr[] mItemsAttrs;

        /* renamed from: com.color.app.ColorAlertControllerCustom$AlertParams$1 */
        class AnonymousClass1 implements OnItemClickListener {
            final /* synthetic */ AlertParams this$1;
            final /* synthetic */ ColorAlertControllerCustom val$customDialog;
            final /* synthetic */ ColorAlertController val$dialog;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.app.ColorAlertControllerCustom.AlertParams.1.<init>(com.color.app.ColorAlertControllerCustom$AlertParams, com.color.app.ColorAlertController, com.color.app.ColorAlertControllerCustom):void, dex: 
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
            AnonymousClass1(com.color.app.ColorAlertControllerCustom.AlertParams r1, com.color.app.ColorAlertController r2, com.color.app.ColorAlertControllerCustom r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.app.ColorAlertControllerCustom.AlertParams.1.<init>(com.color.app.ColorAlertControllerCustom$AlertParams, com.color.app.ColorAlertController, com.color.app.ColorAlertControllerCustom):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.app.ColorAlertControllerCustom.AlertParams.1.<init>(com.color.app.ColorAlertControllerCustom$AlertParams, com.color.app.ColorAlertController, com.color.app.ColorAlertControllerCustom):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.app.ColorAlertControllerCustom.AlertParams.1.onItemClick(android.widget.AdapterView, android.view.View, int, long):void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.app.ColorAlertControllerCustom.AlertParams.1.onItemClick(android.widget.AdapterView, android.view.View, int, long):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.app.ColorAlertControllerCustom.AlertParams.1.onItemClick(android.widget.AdapterView, android.view.View, int, long):void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.app.ColorAlertControllerCustom.AlertParams.<init>(android.content.Context):void, dex: 
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
        public AlertParams(android.content.Context r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.app.ColorAlertControllerCustom.AlertParams.<init>(android.content.Context):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.app.ColorAlertControllerCustom.AlertParams.<init>(android.content.Context):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.color.app.ColorAlertControllerCustom.AlertParams.createListView(com.color.app.ColorAlertController):void, dex: 
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
        protected void createListView(com.color.app.ColorAlertController r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.color.app.ColorAlertControllerCustom.AlertParams.createListView(com.color.app.ColorAlertController):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.app.ColorAlertControllerCustom.AlertParams.createListView(com.color.app.ColorAlertController):void");
        }
    }

    private static class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
        private Context context;
        private ColorAlertControllerCustom dialog;
        private ListItemAttr[] mItemsAttrs;
        private boolean textBold;
        private int textColor;
        private int textId;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.app.ColorAlertControllerCustom.CheckedItemAdapter.<init>(android.content.Context, int, int, java.lang.CharSequence[]):void, dex: 
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
        public CheckedItemAdapter(android.content.Context r1, int r2, int r3, java.lang.CharSequence[] r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.app.ColorAlertControllerCustom.CheckedItemAdapter.<init>(android.content.Context, int, int, java.lang.CharSequence[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.app.ColorAlertControllerCustom.CheckedItemAdapter.<init>(android.content.Context, int, int, java.lang.CharSequence[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.app.ColorAlertControllerCustom.CheckedItemAdapter.<init>(com.color.app.ColorAlertControllerCustom, android.content.Context, int, int, java.lang.CharSequence[], com.color.app.ColorAlertDialogCustom$ListItemAttr[]):void, dex: 
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
        public CheckedItemAdapter(com.color.app.ColorAlertControllerCustom r1, android.content.Context r2, int r3, int r4, java.lang.CharSequence[] r5, com.color.app.ColorAlertDialogCustom.ListItemAttr[] r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.app.ColorAlertControllerCustom.CheckedItemAdapter.<init>(com.color.app.ColorAlertControllerCustom, android.content.Context, int, int, java.lang.CharSequence[], com.color.app.ColorAlertDialogCustom$ListItemAttr[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.app.ColorAlertControllerCustom.CheckedItemAdapter.<init>(com.color.app.ColorAlertControllerCustom, android.content.Context, int, int, java.lang.CharSequence[], com.color.app.ColorAlertDialogCustom$ListItemAttr[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.app.ColorAlertControllerCustom.CheckedItemAdapter.getView(int, android.view.View, android.view.ViewGroup):android.view.View, dex: 
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
        public android.view.View getView(int r1, android.view.View r2, android.view.ViewGroup r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.app.ColorAlertControllerCustom.CheckedItemAdapter.getView(int, android.view.View, android.view.ViewGroup):android.view.View, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.app.ColorAlertControllerCustom.CheckedItemAdapter.getView(int, android.view.View, android.view.ViewGroup):android.view.View");
        }

        public boolean hasStableIds() {
            return true;
        }

        public long getItemId(int position) {
            return (long) position;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.color.app.ColorAlertControllerCustom.-get0(com.color.app.ColorAlertControllerCustom):boolean, dex: 
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
    /* renamed from: -get0 */
    static /* synthetic */ boolean m4-get0(com.color.app.ColorAlertControllerCustom r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.color.app.ColorAlertControllerCustom.-get0(com.color.app.ColorAlertControllerCustom):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.app.ColorAlertControllerCustom.-get0(com.color.app.ColorAlertControllerCustom):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.app.ColorAlertControllerCustom.-get1(com.color.app.ColorAlertControllerCustom):com.color.app.ColorAlertDialogCustom$ListItemAttr[], dex: 
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
    /* renamed from: -get1 */
    static /* synthetic */ com.color.app.ColorAlertDialogCustom.ListItemAttr[] m5-get1(com.color.app.ColorAlertControllerCustom r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.app.ColorAlertControllerCustom.-get1(com.color.app.ColorAlertControllerCustom):com.color.app.ColorAlertDialogCustom$ListItemAttr[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.app.ColorAlertControllerCustom.-get1(com.color.app.ColorAlertControllerCustom):com.color.app.ColorAlertDialogCustom$ListItemAttr[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.color.app.ColorAlertControllerCustom.<init>(android.content.Context, android.content.DialogInterface, android.view.Window):void, dex: 
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
    public ColorAlertControllerCustom(android.content.Context r1, android.content.DialogInterface r2, android.view.Window r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.color.app.ColorAlertControllerCustom.<init>(android.content.Context, android.content.DialogInterface, android.view.Window):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.app.ColorAlertControllerCustom.<init>(android.content.Context, android.content.DialogInterface, android.view.Window):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.color.app.ColorAlertControllerCustom.selectContentView():int, dex: 
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
    public int selectContentView() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.color.app.ColorAlertControllerCustom.selectContentView():int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.app.ColorAlertControllerCustom.selectContentView():int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.app.ColorAlertControllerCustom.setButtonIsBold(int, int, int):void, dex: 
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
    public void setButtonIsBold(int r1, int r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.app.ColorAlertControllerCustom.setButtonIsBold(int, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.app.ColorAlertControllerCustom.setButtonIsBold(int, int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.app.ColorAlertControllerCustom.setListItemState(com.color.app.ColorAlertDialogCustom$ListItemAttr[], boolean):void, dex: 
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
    public void setListItemState(com.color.app.ColorAlertDialogCustom.ListItemAttr[] r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.app.ColorAlertControllerCustom.setListItemState(com.color.app.ColorAlertDialogCustom$ListItemAttr[], boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.app.ColorAlertControllerCustom.setListItemState(com.color.app.ColorAlertDialogCustom$ListItemAttr[], boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.app.ColorAlertControllerCustom.setupContent(android.view.ViewGroup):void, dex: 
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
    protected void setupContent(android.view.ViewGroup r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.app.ColorAlertControllerCustom.setupContent(android.view.ViewGroup):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.app.ColorAlertControllerCustom.setupContent(android.view.ViewGroup):void");
    }
}
