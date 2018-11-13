package com.android.internal.util;

import android.util.Log;
import android.util.SparseArray;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
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
public class MessageUtils {
    private static final boolean DBG = false;
    public static final String[] DEFAULT_PREFIXES = null;
    private static final String TAG = null;

    public static class DuplicateConstantError extends Error {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.util.MessageUtils.DuplicateConstantError.<init>():void, dex: 
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
        private DuplicateConstantError() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.util.MessageUtils.DuplicateConstantError.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.MessageUtils.DuplicateConstantError.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.util.MessageUtils.DuplicateConstantError.<init>(java.lang.String, java.lang.String, int):void, dex: 
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
        public DuplicateConstantError(java.lang.String r1, java.lang.String r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.util.MessageUtils.DuplicateConstantError.<init>(java.lang.String, java.lang.String, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.MessageUtils.DuplicateConstantError.<init>(java.lang.String, java.lang.String, int):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.util.MessageUtils.<clinit>():void, dex: 
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
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.util.MessageUtils.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.MessageUtils.<clinit>():void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x00a9 A:{Catch:{ SecurityException -> 0x00a7, SecurityException -> 0x00a7 }, ExcHandler: java.lang.IllegalArgumentException (e java.lang.IllegalArgumentException), Splitter: B:33:0x008f} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00a7 A:{Catch:{ SecurityException -> 0x00a7, SecurityException -> 0x00a7 }, ExcHandler: java.lang.SecurityException (e java.lang.SecurityException), Splitter: B:30:0x0088} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static SparseArray<String> findMessageNames(Class[] classes, String[] prefixes) {
        SparseArray<String> messageNames = new SparseArray();
        int i = 0;
        int length = classes.length;
        while (true) {
            int i2 = i;
            if (i2 >= length) {
                return messageNames;
            }
            Class c = classes[i2];
            String className = c.getName();
            try {
                Field[] fields = c.getDeclaredFields();
                i = 0;
                int length2 = fields.length;
                while (true) {
                    int i3 = i;
                    if (i3 >= length2) {
                        continue;
                        break;
                    }
                    Field field = fields[i3];
                    int modifiers = field.getModifiers();
                    if (((Modifier.isStatic(modifiers) ? 0 : 1) | (Modifier.isFinal(modifiers) ? 0 : 1)) == 0) {
                        String name = field.getName();
                        for (String prefix : prefixes) {
                            if (name.startsWith(prefix)) {
                                try {
                                    field.setAccessible(true);
                                    try {
                                        int value = field.getInt(null);
                                        String previousName = (String) messageNames.get(value);
                                        if (previousName == null || previousName.equals(name)) {
                                            messageNames.put(value, name);
                                        } else {
                                            throw new DuplicateConstantError(name, previousName, value);
                                        }
                                    } catch (IllegalArgumentException e) {
                                    }
                                } catch (SecurityException e2) {
                                }
                            }
                        }
                        continue;
                    }
                    i = i3 + 1;
                }
            } catch (SecurityException e3) {
                Log.e(TAG, "Can't list fields of class " + className);
            }
            i = i2 + 1;
        }
    }

    public static SparseArray<String> findMessageNames(Class[] classNames) {
        return findMessageNames(classNames, DEFAULT_PREFIXES);
    }
}
