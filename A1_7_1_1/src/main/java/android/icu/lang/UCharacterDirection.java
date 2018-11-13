package android.icu.lang;

import android.icu.lang.UCharacterEnums.ECharacterDirection;

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
public final class UCharacterDirection implements ECharacterDirection {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.lang.UCharacterDirection.<init>():void, dex: 
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
    private UCharacterDirection() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.lang.UCharacterDirection.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.lang.UCharacterDirection.<init>():void");
    }

    public static String toString(int dir) {
        switch (dir) {
            case 0:
                return "Left-to-Right";
            case 1:
                return "Right-to-Left";
            case 2:
                return "European Number";
            case 3:
                return "European Number Separator";
            case 4:
                return "European Number Terminator";
            case 5:
                return "Arabic Number";
            case 6:
                return "Common Number Separator";
            case 7:
                return "Paragraph Separator";
            case 8:
                return "Segment Separator";
            case 9:
                return "Whitespace";
            case 10:
                return "Other Neutrals";
            case 11:
                return "Left-to-Right Embedding";
            case 12:
                return "Left-to-Right Override";
            case 13:
                return "Right-to-Left Arabic";
            case 14:
                return "Right-to-Left Embedding";
            case 15:
                return "Right-to-Left Override";
            case 16:
                return "Pop Directional Format";
            case 17:
                return "Non-Spacing Mark";
            case 18:
                return "Boundary Neutral";
            case 19:
                return "First Strong Isolate";
            case 20:
                return "Left-to-Right Isolate";
            case 21:
                return "Right-to-Left Isolate";
            case 22:
                return "Pop Directional Isolate";
            default:
                return "Unassigned";
        }
    }
}
