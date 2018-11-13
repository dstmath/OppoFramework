package android.renderscript;

import android.renderscript.Script.FieldID;
import android.renderscript.Script.KernelID;
import android.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
public final class ScriptGroup extends BaseObj {
    private static final String TAG = "ScriptGroup";
    private List<Closure> mClosures;
    IO[] mInputs;
    private List<Input> mInputs2;
    private String mName;
    IO[] mOutputs;
    private Future[] mOutputs2;

    public static final class Binding {
        private final FieldID mField;
        private final Object mValue;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.Binding.<init>(android.renderscript.Script$FieldID, java.lang.Object):void, dex: 
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
        public Binding(android.renderscript.Script.FieldID r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.Binding.<init>(android.renderscript.Script$FieldID, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Binding.<init>(android.renderscript.Script$FieldID, java.lang.Object):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Binding.getField():android.renderscript.Script$FieldID, dex: 
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
        android.renderscript.Script.FieldID getField() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Binding.getField():android.renderscript.Script$FieldID, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Binding.getField():android.renderscript.Script$FieldID");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Binding.getValue():java.lang.Object, dex: 
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
        java.lang.Object getValue() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Binding.getValue():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Binding.getValue():java.lang.Object");
        }
    }

    public static final class Builder2 {
        private static final String TAG = "ScriptGroup.Builder2";
        List<Closure> mClosures;
        List<Input> mInputs;
        RenderScript mRS;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.Builder2.<init>(android.renderscript.RenderScript):void, dex: 
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
        public Builder2(android.renderscript.RenderScript r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.Builder2.<init>(android.renderscript.RenderScript):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder2.<init>(android.renderscript.RenderScript):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder2.addInvokeInternal(android.renderscript.Script$InvokeID, java.lang.Object[], java.util.Map):android.renderscript.ScriptGroup$Closure, dex: 
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
        private android.renderscript.ScriptGroup.Closure addInvokeInternal(android.renderscript.Script.InvokeID r1, java.lang.Object[] r2, java.util.Map<android.renderscript.Script.FieldID, java.lang.Object> r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder2.addInvokeInternal(android.renderscript.Script$InvokeID, java.lang.Object[], java.util.Map):android.renderscript.ScriptGroup$Closure, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder2.addInvokeInternal(android.renderscript.Script$InvokeID, java.lang.Object[], java.util.Map):android.renderscript.ScriptGroup$Closure");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder2.addKernelInternal(android.renderscript.Script$KernelID, android.renderscript.Type, java.lang.Object[], java.util.Map):android.renderscript.ScriptGroup$Closure, dex: 
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
        private android.renderscript.ScriptGroup.Closure addKernelInternal(android.renderscript.Script.KernelID r1, android.renderscript.Type r2, java.lang.Object[] r3, java.util.Map<android.renderscript.Script.FieldID, java.lang.Object> r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder2.addKernelInternal(android.renderscript.Script$KernelID, android.renderscript.Type, java.lang.Object[], java.util.Map):android.renderscript.ScriptGroup$Closure, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder2.addKernelInternal(android.renderscript.Script$KernelID, android.renderscript.Type, java.lang.Object[], java.util.Map):android.renderscript.ScriptGroup$Closure");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptGroup.Builder2.seperateArgsAndBindings(java.lang.Object[], java.util.ArrayList, java.util.Map):boolean, dex: 
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
        private boolean seperateArgsAndBindings(java.lang.Object[] r1, java.util.ArrayList<java.lang.Object> r2, java.util.Map<android.renderscript.Script.FieldID, java.lang.Object> r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptGroup.Builder2.seperateArgsAndBindings(java.lang.Object[], java.util.ArrayList, java.util.Map):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder2.seperateArgsAndBindings(java.lang.Object[], java.util.ArrayList, java.util.Map):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder2.addInput():android.renderscript.ScriptGroup$Input, dex: 
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
        public android.renderscript.ScriptGroup.Input addInput() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder2.addInput():android.renderscript.ScriptGroup$Input, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder2.addInput():android.renderscript.ScriptGroup$Input");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptGroup.Builder2.addInvoke(android.renderscript.Script$InvokeID, java.lang.Object[]):android.renderscript.ScriptGroup$Closure, dex: 
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
        public android.renderscript.ScriptGroup.Closure addInvoke(android.renderscript.Script.InvokeID r1, java.lang.Object... r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptGroup.Builder2.addInvoke(android.renderscript.Script$InvokeID, java.lang.Object[]):android.renderscript.ScriptGroup$Closure, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder2.addInvoke(android.renderscript.Script$InvokeID, java.lang.Object[]):android.renderscript.ScriptGroup$Closure");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptGroup.Builder2.addKernel(android.renderscript.Script$KernelID, android.renderscript.Type, java.lang.Object[]):android.renderscript.ScriptGroup$Closure, dex: 
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
        public android.renderscript.ScriptGroup.Closure addKernel(android.renderscript.Script.KernelID r1, android.renderscript.Type r2, java.lang.Object... r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptGroup.Builder2.addKernel(android.renderscript.Script$KernelID, android.renderscript.Type, java.lang.Object[]):android.renderscript.ScriptGroup$Closure, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder2.addKernel(android.renderscript.Script$KernelID, android.renderscript.Type, java.lang.Object[]):android.renderscript.ScriptGroup$Closure");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptGroup.Builder2.create(java.lang.String, android.renderscript.ScriptGroup$Future[]):android.renderscript.ScriptGroup, dex: 
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
        public android.renderscript.ScriptGroup create(java.lang.String r1, android.renderscript.ScriptGroup.Future... r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptGroup.Builder2.create(java.lang.String, android.renderscript.ScriptGroup$Future[]):android.renderscript.ScriptGroup, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder2.create(java.lang.String, android.renderscript.ScriptGroup$Future[]):android.renderscript.ScriptGroup");
        }
    }

    public static final class Builder {
        private int mKernelCount;
        private ArrayList<ConnectLine> mLines;
        private ArrayList<Node> mNodes;
        private RenderScript mRS;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.Builder.<init>(android.renderscript.RenderScript):void, dex: 
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
        public Builder(android.renderscript.RenderScript r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.Builder.<init>(android.renderscript.RenderScript):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder.<init>(android.renderscript.RenderScript):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder.findNode(android.renderscript.Script$KernelID):android.renderscript.ScriptGroup$Node, dex: 
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
        private android.renderscript.ScriptGroup.Node findNode(android.renderscript.Script.KernelID r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder.findNode(android.renderscript.Script$KernelID):android.renderscript.ScriptGroup$Node, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder.findNode(android.renderscript.Script$KernelID):android.renderscript.ScriptGroup$Node");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder.findNode(android.renderscript.Script):android.renderscript.ScriptGroup$Node, dex: 
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
        private android.renderscript.ScriptGroup.Node findNode(android.renderscript.Script r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder.findNode(android.renderscript.Script):android.renderscript.ScriptGroup$Node, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder.findNode(android.renderscript.Script):android.renderscript.ScriptGroup$Node");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptGroup.Builder.mergeDAGs(int, int):void, dex:  in method: android.renderscript.ScriptGroup.Builder.mergeDAGs(int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptGroup.Builder.mergeDAGs(int, int):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:752)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private void mergeDAGs(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptGroup.Builder.mergeDAGs(int, int):void, dex:  in method: android.renderscript.ScriptGroup.Builder.mergeDAGs(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder.mergeDAGs(int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder.validateCycle(android.renderscript.ScriptGroup$Node, android.renderscript.ScriptGroup$Node):void, dex: 
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
        private void validateCycle(android.renderscript.ScriptGroup.Node r1, android.renderscript.ScriptGroup.Node r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder.validateCycle(android.renderscript.ScriptGroup$Node, android.renderscript.ScriptGroup$Node):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder.validateCycle(android.renderscript.ScriptGroup$Node, android.renderscript.ScriptGroup$Node):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder.validateDAG():void, dex: 
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
        private void validateDAG() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder.validateDAG():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder.validateDAG():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.renderscript.ScriptGroup.Builder.validateDAGRecurse(android.renderscript.ScriptGroup$Node, int):void, dex: 
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
        private void validateDAGRecurse(android.renderscript.ScriptGroup.Node r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.renderscript.ScriptGroup.Builder.validateDAGRecurse(android.renderscript.ScriptGroup$Node, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder.validateDAGRecurse(android.renderscript.ScriptGroup$Node, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder.addConnection(android.renderscript.Type, android.renderscript.Script$KernelID, android.renderscript.Script$FieldID):android.renderscript.ScriptGroup$Builder, dex: 
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
        public android.renderscript.ScriptGroup.Builder addConnection(android.renderscript.Type r1, android.renderscript.Script.KernelID r2, android.renderscript.Script.FieldID r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder.addConnection(android.renderscript.Type, android.renderscript.Script$KernelID, android.renderscript.Script$FieldID):android.renderscript.ScriptGroup$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder.addConnection(android.renderscript.Type, android.renderscript.Script$KernelID, android.renderscript.Script$FieldID):android.renderscript.ScriptGroup$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder.addConnection(android.renderscript.Type, android.renderscript.Script$KernelID, android.renderscript.Script$KernelID):android.renderscript.ScriptGroup$Builder, dex: 
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
        public android.renderscript.ScriptGroup.Builder addConnection(android.renderscript.Type r1, android.renderscript.Script.KernelID r2, android.renderscript.Script.KernelID r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder.addConnection(android.renderscript.Type, android.renderscript.Script$KernelID, android.renderscript.Script$KernelID):android.renderscript.ScriptGroup$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder.addConnection(android.renderscript.Type, android.renderscript.Script$KernelID, android.renderscript.Script$KernelID):android.renderscript.ScriptGroup$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder.addKernel(android.renderscript.Script$KernelID):android.renderscript.ScriptGroup$Builder, dex: 
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
        public android.renderscript.ScriptGroup.Builder addKernel(android.renderscript.Script.KernelID r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder.addKernel(android.renderscript.Script$KernelID):android.renderscript.ScriptGroup$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder.addKernel(android.renderscript.Script$KernelID):android.renderscript.ScriptGroup$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder.create():android.renderscript.ScriptGroup, dex: 
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
        public android.renderscript.ScriptGroup create() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Builder.create():android.renderscript.ScriptGroup, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Builder.create():android.renderscript.ScriptGroup");
        }
    }

    public static final class Closure extends BaseObj {
        private static final String TAG = "Closure";
        private Object[] mArgs;
        private Map<FieldID, Object> mBindings;
        private FieldPacker mFP;
        private Map<FieldID, Future> mGlobalFuture;
        private Future mReturnFuture;
        private Allocation mReturnValue;

        private static final class ValueAndSize {
            public int size;
            public long value;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptGroup.Closure.ValueAndSize.<init>(android.renderscript.RenderScript, java.lang.Object):void, dex: 
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
            public ValueAndSize(android.renderscript.RenderScript r1, java.lang.Object r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptGroup.Closure.ValueAndSize.<init>(android.renderscript.RenderScript, java.lang.Object):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Closure.ValueAndSize.<init>(android.renderscript.RenderScript, java.lang.Object):void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.renderscript.ScriptGroup.Closure.<init>(long, android.renderscript.RenderScript):void, dex: 
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
        Closure(long r1, android.renderscript.RenderScript r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.renderscript.ScriptGroup.Closure.<init>(long, android.renderscript.RenderScript):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Closure.<init>(long, android.renderscript.RenderScript):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: e in method: android.renderscript.ScriptGroup.Closure.<init>(android.renderscript.RenderScript, android.renderscript.Script$InvokeID, java.lang.Object[], java.util.Map):void, dex:  in method: android.renderscript.ScriptGroup.Closure.<init>(android.renderscript.RenderScript, android.renderscript.Script$InvokeID, java.lang.Object[], java.util.Map):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: e in method: android.renderscript.ScriptGroup.Closure.<init>(android.renderscript.RenderScript, android.renderscript.Script$InvokeID, java.lang.Object[], java.util.Map):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: com.android.dex.DexException: bogus registerCount: e
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        Closure(android.renderscript.RenderScript r1, android.renderscript.Script.InvokeID r2, java.lang.Object[] r3, java.util.Map<android.renderscript.Script.FieldID, java.lang.Object> r4) {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: e in method: android.renderscript.ScriptGroup.Closure.<init>(android.renderscript.RenderScript, android.renderscript.Script$InvokeID, java.lang.Object[], java.util.Map):void, dex:  in method: android.renderscript.ScriptGroup.Closure.<init>(android.renderscript.RenderScript, android.renderscript.Script$InvokeID, java.lang.Object[], java.util.Map):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Closure.<init>(android.renderscript.RenderScript, android.renderscript.Script$InvokeID, java.lang.Object[], java.util.Map):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: e in method: android.renderscript.ScriptGroup.Closure.<init>(android.renderscript.RenderScript, android.renderscript.Script$KernelID, android.renderscript.Type, java.lang.Object[], java.util.Map):void, dex:  in method: android.renderscript.ScriptGroup.Closure.<init>(android.renderscript.RenderScript, android.renderscript.Script$KernelID, android.renderscript.Type, java.lang.Object[], java.util.Map):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: e in method: android.renderscript.ScriptGroup.Closure.<init>(android.renderscript.RenderScript, android.renderscript.Script$KernelID, android.renderscript.Type, java.lang.Object[], java.util.Map):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: com.android.dex.DexException: bogus registerCount: e
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        Closure(android.renderscript.RenderScript r1, android.renderscript.Script.KernelID r2, android.renderscript.Type r3, java.lang.Object[] r4, java.util.Map<android.renderscript.Script.FieldID, java.lang.Object> r5) {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: e in method: android.renderscript.ScriptGroup.Closure.<init>(android.renderscript.RenderScript, android.renderscript.Script$KernelID, android.renderscript.Type, java.lang.Object[], java.util.Map):void, dex:  in method: android.renderscript.ScriptGroup.Closure.<init>(android.renderscript.RenderScript, android.renderscript.Script$KernelID, android.renderscript.Type, java.lang.Object[], java.util.Map):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Closure.<init>(android.renderscript.RenderScript, android.renderscript.Script$KernelID, android.renderscript.Type, java.lang.Object[], java.util.Map):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptGroup.Closure.retrieveValueAndDependenceInfo(android.renderscript.RenderScript, int, android.renderscript.Script$FieldID, java.lang.Object, long[], int[], long[], long[]):void, dex: 
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
        private void retrieveValueAndDependenceInfo(android.renderscript.RenderScript r1, int r2, android.renderscript.Script.FieldID r3, java.lang.Object r4, long[] r5, int[] r6, long[] r7, long[] r8) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptGroup.Closure.retrieveValueAndDependenceInfo(android.renderscript.RenderScript, int, android.renderscript.Script$FieldID, java.lang.Object, long[], int[], long[], long[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Closure.retrieveValueAndDependenceInfo(android.renderscript.RenderScript, int, android.renderscript.Script$FieldID, java.lang.Object, long[], int[], long[], long[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Closure.destroy():void, dex: 
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
        public void destroy() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Closure.destroy():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Closure.destroy():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.Closure.finalize():void, dex: 
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
        protected void finalize() throws java.lang.Throwable {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.Closure.finalize():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Closure.finalize():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Closure.getGlobal(android.renderscript.Script$FieldID):android.renderscript.ScriptGroup$Future, dex: 
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
        public android.renderscript.ScriptGroup.Future getGlobal(android.renderscript.Script.FieldID r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Closure.getGlobal(android.renderscript.Script$FieldID):android.renderscript.ScriptGroup$Future, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Closure.getGlobal(android.renderscript.Script$FieldID):android.renderscript.ScriptGroup$Future");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Closure.getReturn():android.renderscript.ScriptGroup$Future, dex: 
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
        public android.renderscript.ScriptGroup.Future getReturn() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Closure.getReturn():android.renderscript.ScriptGroup$Future, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Closure.getReturn():android.renderscript.ScriptGroup$Future");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptGroup.Closure.setArg(int, java.lang.Object):void, dex: 
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
        void setArg(int r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptGroup.Closure.setArg(int, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Closure.setArg(int, java.lang.Object):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptGroup.Closure.setGlobal(android.renderscript.Script$FieldID, java.lang.Object):void, dex: 
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
        void setGlobal(android.renderscript.Script.FieldID r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptGroup.Closure.setGlobal(android.renderscript.Script$FieldID, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Closure.setGlobal(android.renderscript.Script$FieldID, java.lang.Object):void");
        }
    }

    static class ConnectLine {
        Type mAllocationType;
        KernelID mFrom;
        FieldID mToF;
        KernelID mToK;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.ConnectLine.<init>(android.renderscript.Type, android.renderscript.Script$KernelID, android.renderscript.Script$FieldID):void, dex: 
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
        ConnectLine(android.renderscript.Type r1, android.renderscript.Script.KernelID r2, android.renderscript.Script.FieldID r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.ConnectLine.<init>(android.renderscript.Type, android.renderscript.Script$KernelID, android.renderscript.Script$FieldID):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.ConnectLine.<init>(android.renderscript.Type, android.renderscript.Script$KernelID, android.renderscript.Script$FieldID):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.ConnectLine.<init>(android.renderscript.Type, android.renderscript.Script$KernelID, android.renderscript.Script$KernelID):void, dex: 
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
        ConnectLine(android.renderscript.Type r1, android.renderscript.Script.KernelID r2, android.renderscript.Script.KernelID r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.ConnectLine.<init>(android.renderscript.Type, android.renderscript.Script$KernelID, android.renderscript.Script$KernelID):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.ConnectLine.<init>(android.renderscript.Type, android.renderscript.Script$KernelID, android.renderscript.Script$KernelID):void");
        }
    }

    public static final class Future {
        Closure mClosure;
        FieldID mFieldID;
        Object mValue;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.Future.<init>(android.renderscript.ScriptGroup$Closure, android.renderscript.Script$FieldID, java.lang.Object):void, dex: 
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
        Future(android.renderscript.ScriptGroup.Closure r1, android.renderscript.Script.FieldID r2, java.lang.Object r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.Future.<init>(android.renderscript.ScriptGroup$Closure, android.renderscript.Script$FieldID, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Future.<init>(android.renderscript.ScriptGroup$Closure, android.renderscript.Script$FieldID, java.lang.Object):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Future.getClosure():android.renderscript.ScriptGroup$Closure, dex: 
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
        android.renderscript.ScriptGroup.Closure getClosure() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Future.getClosure():android.renderscript.ScriptGroup$Closure, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Future.getClosure():android.renderscript.ScriptGroup$Closure");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Future.getFieldID():android.renderscript.Script$FieldID, dex: 
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
        android.renderscript.Script.FieldID getFieldID() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Future.getFieldID():android.renderscript.Script$FieldID, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Future.getFieldID():android.renderscript.Script$FieldID");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Future.getValue():java.lang.Object, dex: 
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
        java.lang.Object getValue() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Future.getValue():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Future.getValue():java.lang.Object");
        }
    }

    static class IO {
        Allocation mAllocation;
        KernelID mKID;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.IO.<init>(android.renderscript.Script$KernelID):void, dex: 
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
        IO(android.renderscript.Script.KernelID r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.IO.<init>(android.renderscript.Script$KernelID):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.IO.<init>(android.renderscript.Script$KernelID):void");
        }
    }

    public static final class Input {
        List<Pair<Closure, Integer>> mArgIndex;
        List<Pair<Closure, FieldID>> mFieldID;
        Object mValue;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.Input.<init>():void, dex: 
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
        Input() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.Input.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Input.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Input.addReference(android.renderscript.ScriptGroup$Closure, int):void, dex: 
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
        void addReference(android.renderscript.ScriptGroup.Closure r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Input.addReference(android.renderscript.ScriptGroup$Closure, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Input.addReference(android.renderscript.ScriptGroup$Closure, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Input.addReference(android.renderscript.ScriptGroup$Closure, android.renderscript.Script$FieldID):void, dex: 
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
        void addReference(android.renderscript.ScriptGroup.Closure r1, android.renderscript.Script.FieldID r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Input.addReference(android.renderscript.ScriptGroup$Closure, android.renderscript.Script$FieldID):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Input.addReference(android.renderscript.ScriptGroup$Closure, android.renderscript.Script$FieldID):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Input.get():java.lang.Object, dex: 
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
        java.lang.Object get() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.Input.get():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Input.get():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.Input.set(java.lang.Object):void, dex: 
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
        void set(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.Input.set(java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Input.set(java.lang.Object):void");
        }
    }

    static class Node {
        int dagNumber;
        ArrayList<ConnectLine> mInputs;
        ArrayList<KernelID> mKernels;
        Node mNext;
        ArrayList<ConnectLine> mOutputs;
        Script mScript;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.Node.<init>(android.renderscript.Script):void, dex: 
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
        Node(android.renderscript.Script r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.renderscript.ScriptGroup.Node.<init>(android.renderscript.Script):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.Node.<init>(android.renderscript.Script):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: e in method: android.renderscript.ScriptGroup.<init>(long, android.renderscript.RenderScript):void, dex:  in method: android.renderscript.ScriptGroup.<init>(long, android.renderscript.RenderScript):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: e in method: android.renderscript.ScriptGroup.<init>(long, android.renderscript.RenderScript):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus registerCount: e
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    ScriptGroup(long r1, android.renderscript.RenderScript r3) {
        /*
        // Can't load method instructions: Load method exception: bogus registerCount: e in method: android.renderscript.ScriptGroup.<init>(long, android.renderscript.RenderScript):void, dex:  in method: android.renderscript.ScriptGroup.<init>(long, android.renderscript.RenderScript):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.<init>(long, android.renderscript.RenderScript):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptGroup.<init>(android.renderscript.RenderScript, java.lang.String, java.util.List, java.util.List, android.renderscript.ScriptGroup$Future[]):void, dex:  in method: android.renderscript.ScriptGroup.<init>(android.renderscript.RenderScript, java.lang.String, java.util.List, java.util.List, android.renderscript.ScriptGroup$Future[]):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptGroup.<init>(android.renderscript.RenderScript, java.lang.String, java.util.List, java.util.List, android.renderscript.ScriptGroup$Future[]):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:61)
        	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:756)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    ScriptGroup(android.renderscript.RenderScript r1, java.lang.String r2, java.util.List<android.renderscript.ScriptGroup.Closure> r3, java.util.List<android.renderscript.ScriptGroup.Input> r4, android.renderscript.ScriptGroup.Future[] r5) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptGroup.<init>(android.renderscript.RenderScript, java.lang.String, java.util.List, java.util.List, android.renderscript.ScriptGroup$Future[]):void, dex:  in method: android.renderscript.ScriptGroup.<init>(android.renderscript.RenderScript, java.lang.String, java.util.List, java.util.List, android.renderscript.ScriptGroup$Future[]):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.<init>(android.renderscript.RenderScript, java.lang.String, java.util.List, java.util.List, android.renderscript.ScriptGroup$Future[]):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.destroy():void, dex: 
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
    public void destroy() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.destroy():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.destroy():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.execute():void, dex: 
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
    public void execute() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.execute():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.execute():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.execute(java.lang.Object[]):java.lang.Object[], dex: 
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
    public java.lang.Object[] execute(java.lang.Object... r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.execute(java.lang.Object[]):java.lang.Object[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.execute(java.lang.Object[]):java.lang.Object[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.setInput(android.renderscript.Script$KernelID, android.renderscript.Allocation):void, dex: 
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
    public void setInput(android.renderscript.Script.KernelID r1, android.renderscript.Allocation r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.setInput(android.renderscript.Script$KernelID, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.setInput(android.renderscript.Script$KernelID, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.setOutput(android.renderscript.Script$KernelID, android.renderscript.Allocation):void, dex: 
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
    public void setOutput(android.renderscript.Script.KernelID r1, android.renderscript.Allocation r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptGroup.setOutput(android.renderscript.Script$KernelID, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptGroup.setOutput(android.renderscript.Script$KernelID, android.renderscript.Allocation):void");
    }
}
