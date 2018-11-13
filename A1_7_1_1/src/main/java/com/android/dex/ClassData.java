package com.android.dex;

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
public final class ClassData {
    private final Method[] directMethods;
    private final Field[] instanceFields;
    private final Field[] staticFields;
    private final Method[] virtualMethods;

    public static class Field {
        private final int accessFlags;
        private final int fieldIndex;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.android.dex.ClassData.Field.<init>(int, int):void, dex: 
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
        public Field(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.android.dex.ClassData.Field.<init>(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.dex.ClassData.Field.<init>(int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.dex.ClassData.Field.getAccessFlags():int, dex: 
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
        public int getAccessFlags() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.dex.ClassData.Field.getAccessFlags():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.dex.ClassData.Field.getAccessFlags():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.dex.ClassData.Field.getFieldIndex():int, dex: 
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
        public int getFieldIndex() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.dex.ClassData.Field.getFieldIndex():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.dex.ClassData.Field.getFieldIndex():int");
        }
    }

    public static class Method {
        private final int accessFlags;
        private final int codeOffset;
        private final int methodIndex;

        public Method(int methodIndex, int accessFlags, int codeOffset) {
            this.methodIndex = methodIndex;
            this.accessFlags = accessFlags;
            this.codeOffset = codeOffset;
        }

        public int getMethodIndex() {
            return this.methodIndex;
        }

        public int getAccessFlags() {
            return this.accessFlags;
        }

        public int getCodeOffset() {
            return this.codeOffset;
        }
    }

    public ClassData(Field[] staticFields, Field[] instanceFields, Method[] directMethods, Method[] virtualMethods) {
        this.staticFields = staticFields;
        this.instanceFields = instanceFields;
        this.directMethods = directMethods;
        this.virtualMethods = virtualMethods;
    }

    public Field[] getStaticFields() {
        return this.staticFields;
    }

    public Field[] getInstanceFields() {
        return this.instanceFields;
    }

    public Method[] getDirectMethods() {
        return this.directMethods;
    }

    public Method[] getVirtualMethods() {
        return this.virtualMethods;
    }

    public Field[] allFields() {
        Field[] result = new Field[(this.staticFields.length + this.instanceFields.length)];
        System.arraycopy(this.staticFields, 0, result, 0, this.staticFields.length);
        System.arraycopy(this.instanceFields, 0, result, this.staticFields.length, this.instanceFields.length);
        return result;
    }

    public Method[] allMethods() {
        Method[] result = new Method[(this.directMethods.length + this.virtualMethods.length)];
        System.arraycopy(this.directMethods, 0, result, 0, this.directMethods.length);
        System.arraycopy(this.virtualMethods, 0, result, this.directMethods.length, this.virtualMethods.length);
        return result;
    }
}
