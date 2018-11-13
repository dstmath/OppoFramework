package android.icu.impl;

import android.icu.text.Replaceable;
import android.icu.text.ReplaceableString;
import android.icu.text.Transliterator.Position;

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
public class UtilityExtensions {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.UtilityExtensions.<init>():void, dex: 
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
    public UtilityExtensions() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.UtilityExtensions.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UtilityExtensions.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.UtilityExtensions.appendToRule(java.lang.StringBuffer, android.icu.text.UnicodeMatcher, boolean, java.lang.StringBuffer):void, dex: 
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
    public static void appendToRule(java.lang.StringBuffer r1, android.icu.text.UnicodeMatcher r2, boolean r3, java.lang.StringBuffer r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.UtilityExtensions.appendToRule(java.lang.StringBuffer, android.icu.text.UnicodeMatcher, boolean, java.lang.StringBuffer):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UtilityExtensions.appendToRule(java.lang.StringBuffer, android.icu.text.UnicodeMatcher, boolean, java.lang.StringBuffer):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.icu.impl.UtilityExtensions.appendToRule(java.lang.StringBuffer, java.lang.String, boolean, boolean, java.lang.StringBuffer):void, dex:  in method: android.icu.impl.UtilityExtensions.appendToRule(java.lang.StringBuffer, java.lang.String, boolean, boolean, java.lang.StringBuffer):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.icu.impl.UtilityExtensions.appendToRule(java.lang.StringBuffer, java.lang.String, boolean, boolean, java.lang.StringBuffer):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:752)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public static void appendToRule(java.lang.StringBuffer r1, java.lang.String r2, boolean r3, boolean r4, java.lang.StringBuffer r5) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.icu.impl.UtilityExtensions.appendToRule(java.lang.StringBuffer, java.lang.String, boolean, boolean, java.lang.StringBuffer):void, dex:  in method: android.icu.impl.UtilityExtensions.appendToRule(java.lang.StringBuffer, java.lang.String, boolean, boolean, java.lang.StringBuffer):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UtilityExtensions.appendToRule(java.lang.StringBuffer, java.lang.String, boolean, boolean, java.lang.StringBuffer):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UtilityExtensions.formatInput(android.icu.text.ReplaceableString, android.icu.text.Transliterator$Position):java.lang.String, dex: 
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
    public static java.lang.String formatInput(android.icu.text.ReplaceableString r1, android.icu.text.Transliterator.Position r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UtilityExtensions.formatInput(android.icu.text.ReplaceableString, android.icu.text.Transliterator$Position):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UtilityExtensions.formatInput(android.icu.text.ReplaceableString, android.icu.text.Transliterator$Position):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.UtilityExtensions.formatInput(java.lang.StringBuffer, android.icu.text.ReplaceableString, android.icu.text.Transliterator$Position):java.lang.StringBuffer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public static java.lang.StringBuffer formatInput(java.lang.StringBuffer r1, android.icu.text.ReplaceableString r2, android.icu.text.Transliterator.Position r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.UtilityExtensions.formatInput(java.lang.StringBuffer, android.icu.text.ReplaceableString, android.icu.text.Transliterator$Position):java.lang.StringBuffer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UtilityExtensions.formatInput(java.lang.StringBuffer, android.icu.text.ReplaceableString, android.icu.text.Transliterator$Position):java.lang.StringBuffer");
    }

    public static String formatInput(Replaceable input, Position pos) {
        return formatInput((ReplaceableString) input, pos);
    }

    public static StringBuffer formatInput(StringBuffer appendTo, Replaceable input, Position pos) {
        return formatInput(appendTo, (ReplaceableString) input, pos);
    }
}
