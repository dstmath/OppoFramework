package com.android.server.oppo;

import java.io.PrintWriter;
import java.lang.reflect.Field;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
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
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
public class DumpObject {
    private static final boolean DEBUG = false;
    private static final String TAG = null;
    private static final Object mInvalidObject = null;
    private final Object mLock;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.oppo.DumpObject.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.oppo.DumpObject.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.oppo.DumpObject.<clinit>():void");
    }

    public DumpObject() {
        this.mLock = new Object();
    }

    public void dumpValue(PrintWriter pw, Object obj) {
        dumpValue(pw, obj, IElsaManager.EMPTY_PACKAGE);
    }

    public void dumpValue(PrintWriter pw, Object obj, String variable) {
        synchronized (this.mLock) {
            try {
                Object obj_find = findObject(obj, variable);
                if (obj_find == mInvalidObject) {
                    pw.println("dump object fail,can not find object!");
                    return;
                } else if (obj_find == null) {
                    pw.println("value is: null");
                    return;
                } else if (isBaseType(obj_find.getClass())) {
                    dumpBaseTypeObject(pw, obj_find);
                } else {
                    dumpComplexTypeObject(pw, obj_find);
                }
            } catch (Exception e) {
                pw.println("dump object variable fail!");
            }
        }
    }

    private void log(String msg) {
    }

    private boolean isBaseType(Class<?> cls) {
        try {
            boolean bBaseType;
            if (cls.equals(String.class) || cls.equals(Integer.class) || cls.equals(Byte.class) || cls.equals(Long.class) || cls.equals(Double.class) || cls.equals(Float.class) || cls.equals(Character.class) || cls.equals(Short.class) || cls.equals(Boolean.class)) {
                bBaseType = true;
            } else {
                bBaseType = cls.isPrimitive();
            }
            log("isBaseType, cls:" + cls + " bBaseType:" + bBaseType);
            return bBaseType;
        } catch (Exception e) {
            log("isBaseType Exception, return false");
            return false;
        }
    }

    private void dumpBaseTypeObject(PrintWriter pw, Object obj) {
        pw.println(obj.getClass() + " " + "value is:" + obj);
    }

    private void dumpComplexTypeObject(PrintWriter pw, Object obj) {
        for (Class<?> cls = obj.getClass(); cls != null; cls = cls.getSuperclass()) {
            for (Field field : cls.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    pw.println(cls + " " + field.getType() + " " + field.getName() + "=" + field.get(obj));
                    field.setAccessible(false);
                } catch (Exception e) {
                    pw.println(cls + IElsaManager.EMPTY_PACKAGE + "cannot access field.getName():" + field.getName());
                }
            }
        }
    }

    private Object findObject(Object obj, String variable) {
        if (variable == null || variable.isEmpty()) {
            return obj;
        }
        log("findObject, variable:" + variable);
        String[] variables = variable.split("\\.");
        int i = 0;
        while (i < variables.length) {
            log("findObject, i:" + i + " variables[i]:" + variables[i]);
            boolean findField = false;
            try {
                Class<?> cls = obj.getClass();
                while (cls != null) {
                    Field[] fields = cls.getDeclaredFields();
                    int length = fields.length;
                    int i2 = 0;
                    while (i2 < length) {
                        Field field = fields[i2];
                        try {
                            field.setAccessible(true);
                            log("findObject, i:" + i + " found field.getName():" + field.getName());
                            if (field.getName().equals(variables[i])) {
                                obj = field.get(obj);
                                findField = true;
                                break;
                            }
                            field.setAccessible(false);
                            i2++;
                        } catch (Exception e) {
                        }
                    }
                    if (!findField) {
                        cls = cls.getSuperclass();
                    }
                }
                log("findObject, i:" + i + " findField:" + findField);
                if (!findField) {
                    return mInvalidObject;
                }
                i++;
            } catch (Exception e2) {
                return mInvalidObject;
            }
        }
        return obj;
    }
}
