package android.view.inputmethod;

import android.os.Bundle;
import java.lang.reflect.Modifier;
import java.util.Map;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class InputConnectionInspector {
    private static final Map<Class, Integer> sMissingMethodsMap = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.inputmethod.InputConnectionInspector.<clinit>():void, dex: 
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
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.inputmethod.InputConnectionInspector.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.inputmethod.InputConnectionInspector.<clinit>():void");
    }

    public static int getMissingMethodFlags(InputConnection ic) {
        if (ic == null || (ic instanceof BaseInputConnection)) {
            return 0;
        }
        if (ic instanceof InputConnectionWrapper) {
            return ((InputConnectionWrapper) ic).getMissingMethodFlags();
        }
        return getMissingMethodFlagsInternal(ic.getClass());
    }

    public static int getMissingMethodFlagsInternal(Class clazz) {
        Integer cachedFlags = (Integer) sMissingMethodsMap.get(clazz);
        if (cachedFlags != null) {
            return cachedFlags.intValue();
        }
        int flags = 0;
        if (!hasGetSelectedText(clazz)) {
            flags = 1;
        }
        if (!hasSetComposingRegion(clazz)) {
            flags |= 2;
        }
        if (!hasCommitCorrection(clazz)) {
            flags |= 4;
        }
        if (!hasRequestCursorUpdate(clazz)) {
            flags |= 8;
        }
        if (!hasDeleteSurroundingTextInCodePoints(clazz)) {
            flags |= 16;
        }
        if (!hasGetHandler(clazz)) {
            flags |= 32;
        }
        if (!hasCloseConnection(clazz)) {
            flags |= 64;
        }
        if (!hasCommitContent(clazz)) {
            flags |= 128;
        }
        sMissingMethodsMap.put(clazz, Integer.valueOf(flags));
        return flags;
    }

    private static boolean hasGetSelectedText(Class clazz) {
        boolean z = false;
        try {
            Class[] clsArr = new Class[1];
            clsArr[0] = Integer.TYPE;
            if (!Modifier.isAbstract(clazz.getMethod("getSelectedText", clsArr).getModifiers())) {
                z = true;
            }
            return z;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasSetComposingRegion(Class clazz) {
        boolean z = false;
        try {
            Class[] clsArr = new Class[2];
            clsArr[0] = Integer.TYPE;
            clsArr[1] = Integer.TYPE;
            if (!Modifier.isAbstract(clazz.getMethod("setComposingRegion", clsArr).getModifiers())) {
                z = true;
            }
            return z;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasCommitCorrection(Class clazz) {
        boolean z = false;
        try {
            Class[] clsArr = new Class[1];
            clsArr[0] = CorrectionInfo.class;
            if (!Modifier.isAbstract(clazz.getMethod("commitCorrection", clsArr).getModifiers())) {
                z = true;
            }
            return z;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasRequestCursorUpdate(Class clazz) {
        boolean z = false;
        try {
            Class[] clsArr = new Class[1];
            clsArr[0] = Integer.TYPE;
            if (!Modifier.isAbstract(clazz.getMethod("requestCursorUpdates", clsArr).getModifiers())) {
                z = true;
            }
            return z;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasDeleteSurroundingTextInCodePoints(Class clazz) {
        boolean z = false;
        try {
            Class[] clsArr = new Class[2];
            clsArr[0] = Integer.TYPE;
            clsArr[1] = Integer.TYPE;
            if (!Modifier.isAbstract(clazz.getMethod("deleteSurroundingTextInCodePoints", clsArr).getModifiers())) {
                z = true;
            }
            return z;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasGetHandler(Class clazz) {
        boolean z = false;
        try {
            if (!Modifier.isAbstract(clazz.getMethod("getHandler", new Class[0]).getModifiers())) {
                z = true;
            }
            return z;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasCloseConnection(Class clazz) {
        boolean z = false;
        try {
            if (!Modifier.isAbstract(clazz.getMethod("closeConnection", new Class[0]).getModifiers())) {
                z = true;
            }
            return z;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasCommitContent(Class clazz) {
        boolean z = false;
        try {
            Class[] clsArr = new Class[3];
            clsArr[0] = InputContentInfo.class;
            clsArr[1] = Integer.TYPE;
            clsArr[2] = Bundle.class;
            if (!Modifier.isAbstract(clazz.getMethod("commitContent", clsArr).getModifiers())) {
                z = true;
            }
            return z;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static String getMissingMethodFlagsAsString(int flags) {
        StringBuilder sb = new StringBuilder();
        boolean isEmpty = true;
        if ((flags & 1) != 0) {
            sb.append("getSelectedText(int)");
            isEmpty = false;
        }
        if ((flags & 2) != 0) {
            if (!isEmpty) {
                sb.append(",");
            }
            sb.append("setComposingRegion(int, int)");
            isEmpty = false;
        }
        if ((flags & 4) != 0) {
            if (!isEmpty) {
                sb.append(",");
            }
            sb.append("commitCorrection(CorrectionInfo)");
            isEmpty = false;
        }
        if ((flags & 8) != 0) {
            if (!isEmpty) {
                sb.append(",");
            }
            sb.append("requestCursorUpdate(int)");
            isEmpty = false;
        }
        if ((flags & 16) != 0) {
            if (!isEmpty) {
                sb.append(",");
            }
            sb.append("deleteSurroundingTextInCodePoints(int, int)");
            isEmpty = false;
        }
        if ((flags & 32) != 0) {
            if (!isEmpty) {
                sb.append(",");
            }
            sb.append("getHandler()");
        }
        if ((flags & 64) != 0) {
            if (!isEmpty) {
                sb.append(",");
            }
            sb.append("closeConnection()");
        }
        if ((flags & 128) != 0) {
            if (!isEmpty) {
                sb.append(",");
            }
            sb.append("commitContent(InputContentInfo, Bundle)");
        }
        return sb.toString();
    }
}
