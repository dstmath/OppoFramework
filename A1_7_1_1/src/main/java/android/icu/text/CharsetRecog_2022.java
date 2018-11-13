package android.icu.text;

import android.icu.lang.UCharacterEnums.ECharacterCategory;

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
abstract class CharsetRecog_2022 extends CharsetRecognizer {

    static class CharsetRecog_2022CN extends CharsetRecog_2022 {
        private byte[][] escapeSequences;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.text.CharsetRecog_2022.CharsetRecog_2022CN.<init>():void, dex: 
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
        CharsetRecog_2022CN() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.text.CharsetRecog_2022.CharsetRecog_2022CN.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_2022.CharsetRecog_2022CN.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.text.CharsetRecog_2022.CharsetRecog_2022CN.match(android.icu.text.CharsetDetector):android.icu.text.CharsetMatch, dex: 
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
        android.icu.text.CharsetMatch match(android.icu.text.CharsetDetector r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.text.CharsetRecog_2022.CharsetRecog_2022CN.match(android.icu.text.CharsetDetector):android.icu.text.CharsetMatch, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_2022.CharsetRecog_2022CN.match(android.icu.text.CharsetDetector):android.icu.text.CharsetMatch");
        }

        String getName() {
            return "ISO-2022-CN";
        }
    }

    static class CharsetRecog_2022JP extends CharsetRecog_2022 {
        private byte[][] escapeSequences;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.text.CharsetRecog_2022.CharsetRecog_2022JP.<init>():void, dex: 
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
        CharsetRecog_2022JP() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.text.CharsetRecog_2022.CharsetRecog_2022JP.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_2022.CharsetRecog_2022JP.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.text.CharsetRecog_2022.CharsetRecog_2022JP.match(android.icu.text.CharsetDetector):android.icu.text.CharsetMatch, dex: 
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
        android.icu.text.CharsetMatch match(android.icu.text.CharsetDetector r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.text.CharsetRecog_2022.CharsetRecog_2022JP.match(android.icu.text.CharsetDetector):android.icu.text.CharsetMatch, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_2022.CharsetRecog_2022JP.match(android.icu.text.CharsetDetector):android.icu.text.CharsetMatch");
        }

        String getName() {
            return "ISO-2022-JP";
        }
    }

    static class CharsetRecog_2022KR extends CharsetRecog_2022 {
        private byte[][] escapeSequences;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.text.CharsetRecog_2022.CharsetRecog_2022KR.<init>():void, dex: 
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
        CharsetRecog_2022KR() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.text.CharsetRecog_2022.CharsetRecog_2022KR.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_2022.CharsetRecog_2022KR.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.text.CharsetRecog_2022.CharsetRecog_2022KR.match(android.icu.text.CharsetDetector):android.icu.text.CharsetMatch, dex: 
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
        android.icu.text.CharsetMatch match(android.icu.text.CharsetDetector r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.text.CharsetRecog_2022.CharsetRecog_2022KR.match(android.icu.text.CharsetDetector):android.icu.text.CharsetMatch, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_2022.CharsetRecog_2022KR.match(android.icu.text.CharsetDetector):android.icu.text.CharsetMatch");
        }

        String getName() {
            return "ISO-2022-KR";
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.CharsetRecog_2022.<init>():void, dex: 
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
    CharsetRecog_2022() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.CharsetRecog_2022.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_2022.<init>():void");
    }

    int match(byte[] text, int textLen, byte[][] escapeSequences) {
        int hits = 0;
        int misses = 0;
        int shifts = 0;
        int i = 0;
        while (i < textLen) {
            if (text[i] == ECharacterCategory.OTHER_SYMBOL) {
                for (byte[] seq : escapeSequences) {
                    if (textLen - i >= seq.length) {
                        int j = 1;
                        while (j < seq.length) {
                            if (seq[j] == text[i + j]) {
                                j++;
                            }
                        }
                        hits++;
                        i += seq.length - 1;
                        break;
                    }
                }
                misses++;
            }
            if (text[i] == (byte) 14 || text[i] == (byte) 15) {
                shifts++;
                i++;
            } else {
                i++;
            }
        }
        if (hits == 0) {
            return 0;
        }
        int quality = ((hits * 100) - (misses * 100)) / (hits + misses);
        if (hits + shifts < 5) {
            quality -= (5 - (hits + shifts)) * 10;
        }
        if (quality < 0) {
            quality = 0;
        }
        return quality;
    }
}
