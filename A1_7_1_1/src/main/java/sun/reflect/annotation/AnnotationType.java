package sun.reflect.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class AnnotationType {
    private boolean inherited;
    private final Map<String, Object> memberDefaults;
    private final Map<String, Class<?>> memberTypes;
    private final Map<String, Method> members;
    private RetentionPolicy retention;

    /* renamed from: sun.reflect.annotation.AnnotationType$1 */
    class AnonymousClass1 implements PrivilegedAction<Method[]> {
        final /* synthetic */ AnnotationType this$0;
        final /* synthetic */ Class val$annotationClass;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: sun.reflect.annotation.AnnotationType.1.<init>(sun.reflect.annotation.AnnotationType, java.lang.Class):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass1(sun.reflect.annotation.AnnotationType r1, java.lang.Class r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: sun.reflect.annotation.AnnotationType.1.<init>(sun.reflect.annotation.AnnotationType, java.lang.Class):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.reflect.annotation.AnnotationType.1.<init>(sun.reflect.annotation.AnnotationType, java.lang.Class):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.reflect.annotation.AnnotationType.1.run():java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public /* bridge */ /* synthetic */ java.lang.Object run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.reflect.annotation.AnnotationType.1.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.reflect.annotation.AnnotationType.1.run():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: sun.reflect.annotation.AnnotationType.1.run():java.lang.reflect.Method[], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.reflect.Method[] run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: sun.reflect.annotation.AnnotationType.1.run():java.lang.reflect.Method[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.reflect.annotation.AnnotationType.1.run():java.lang.reflect.Method[]");
        }
    }

    public static synchronized AnnotationType getInstance(Class<? extends Annotation> annotationClass) {
        AnnotationType result;
        synchronized (AnnotationType.class) {
            result = annotationClass.getAnnotationType();
            if (result == null) {
                result = new AnnotationType(annotationClass);
            }
        }
        return result;
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
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private AnnotationType(java.lang.Class<? extends java.lang.annotation.Annotation> r11) {
        /*
        r10 = this;
        r6 = 0;
        r10.<init>();
        r7 = new java.util.HashMap;
        r7.<init>();
        r10.memberTypes = r7;
        r7 = new java.util.HashMap;
        r7.<init>();
        r10.memberDefaults = r7;
        r7 = new java.util.HashMap;
        r7.<init>();
        r10.members = r7;
        r7 = java.lang.annotation.RetentionPolicy.RUNTIME;
        r10.retention = r7;
        r10.inherited = r6;
        r7 = r11.isAnnotation();
        if (r7 != 0) goto L_0x002e;
    L_0x0025:
        r6 = new java.lang.IllegalArgumentException;
        r7 = "Not an annotation type";
        r6.<init>(r7);
        throw r6;
    L_0x002e:
        r7 = new sun.reflect.annotation.AnnotationType$1;
        r7.<init>(r10, r11);
        r2 = java.security.AccessController.doPrivileged(r7);
        r2 = (java.lang.reflect.Method[]) r2;
        r7 = r2.length;
    L_0x003a:
        if (r6 >= r7) goto L_0x0088;
    L_0x003c:
        r1 = r2[r6];
        r8 = r1.getParameterTypes();
        r8 = r8.length;
        if (r8 == 0) goto L_0x005f;
    L_0x0045:
        r6 = new java.lang.IllegalArgumentException;
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r7 = r7.append(r1);
        r8 = " has params";
        r7 = r7.append(r8);
        r7 = r7.toString();
        r6.<init>(r7);
        throw r6;
    L_0x005f:
        r3 = r1.getName();
        r5 = r1.getReturnType();
        r8 = r10.memberTypes;
        r9 = invocationHandlerReturnType(r5);
        r8.put(r3, r9);
        r8 = r10.members;
        r8.put(r3, r1);
        r0 = r1.getDefaultValue();
        if (r0 == 0) goto L_0x0080;
    L_0x007b:
        r8 = r10.memberDefaults;
        r8.put(r3, r0);
    L_0x0080:
        r8 = r10.members;
        r8.put(r3, r1);
        r6 = r6 + 1;
        goto L_0x003a;
    L_0x0088:
        r11.setAnnotationType(r10);
        r6 = java.lang.annotation.Retention.class;
        if (r11 == r6) goto L_0x00a9;
    L_0x008f:
        r6 = java.lang.annotation.Inherited.class;
        if (r11 == r6) goto L_0x00a9;
    L_0x0093:
        r6 = java.lang.annotation.Retention.class;
        r4 = r11.getAnnotation(r6);
        r4 = (java.lang.annotation.Retention) r4;
        if (r4 != 0) goto L_0x00aa;
    L_0x009d:
        r6 = java.lang.annotation.RetentionPolicy.CLASS;
    L_0x009f:
        r10.retention = r6;
        r6 = java.lang.annotation.Inherited.class;
        r6 = r11.isAnnotationPresent(r6);
        r10.inherited = r6;
    L_0x00a9:
        return;
    L_0x00aa:
        r6 = r4.value();
        goto L_0x009f;
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.reflect.annotation.AnnotationType.<init>(java.lang.Class):void");
    }

    public static Class<?> invocationHandlerReturnType(Class<?> type) {
        if (type == Byte.TYPE) {
            return Byte.class;
        }
        if (type == Character.TYPE) {
            return Character.class;
        }
        if (type == Double.TYPE) {
            return Double.class;
        }
        if (type == Float.TYPE) {
            return Float.class;
        }
        if (type == Integer.TYPE) {
            return Integer.class;
        }
        if (type == Long.TYPE) {
            return Long.class;
        }
        if (type == Short.TYPE) {
            return Short.class;
        }
        if (type == Boolean.TYPE) {
            return Boolean.class;
        }
        return type;
    }

    public Map<String, Class<?>> memberTypes() {
        return this.memberTypes;
    }

    public Map<String, Method> members() {
        return this.members;
    }

    public Map<String, Object> memberDefaults() {
        return this.memberDefaults;
    }

    public RetentionPolicy retention() {
        return this.retention;
    }

    public boolean isInherited() {
        return this.inherited;
    }

    public String toString() {
        StringBuffer s = new StringBuffer("Annotation Type:\n");
        s.append("   Member types: " + this.memberTypes + "\n");
        s.append("   Member defaults: " + this.memberDefaults + "\n");
        s.append("   Retention policy: " + this.retention + "\n");
        s.append("   Inherited: " + this.inherited);
        return s.toString();
    }
}
