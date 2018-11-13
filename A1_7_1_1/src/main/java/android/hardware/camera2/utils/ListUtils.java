package android.hardware.camera2.utils;

import java.util.List;

public class ListUtils {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.utils.ListUtils.listToString(java.util.List):java.lang.String, dex: 
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
    public static <T> java.lang.String listToString(java.util.List<T> r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.utils.ListUtils.listToString(java.util.List):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.utils.ListUtils.listToString(java.util.List):java.lang.String");
    }

    public static <T> boolean listContains(List<T> list, T needle) {
        if (list == null) {
            return false;
        }
        return list.contains(needle);
    }

    public static <T> boolean listElementsEqualTo(List<T> list, T single) {
        boolean z = false;
        if (list == null) {
            return false;
        }
        if (list.size() == 1) {
            z = list.contains(single);
        }
        return z;
    }

    public static <T> T listSelectFirstFrom(List<T> list, T[] choices) {
        if (list == null) {
            return null;
        }
        for (T choice : choices) {
            if (list.contains(choice)) {
                return choice;
            }
        }
        return null;
    }

    private ListUtils() {
        throw new AssertionError();
    }
}
