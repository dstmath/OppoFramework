package android.net.wifi.nan;

import java.util.Iterator;

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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    */
public class TlvBufferUtils {

    public static class TlvConstructor {
        private byte[] mArray;
        private int mArrayLength;
        private int mLengthSize;
        private int mPosition;
        private int mTypeSize;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.<init>(int, int):void, dex:  in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.<init>(int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.<init>(int, int):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public TlvConstructor(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.<init>(int, int):void, dex:  in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.<init>(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.<init>(int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.addHeader(int, int):void, dex:  in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.addHeader(int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.addHeader(int, int):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:756)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private void addHeader(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.addHeader(int, int):void, dex:  in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.addHeader(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.addHeader(int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.checkLength(int):void, dex: 
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
        private void checkLength(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.checkLength(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.checkLength(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.allocate(int):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
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
        public android.net.wifi.nan.TlvBufferUtils.TlvConstructor allocate(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.allocate(int):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.allocate(int):android.net.wifi.nan.TlvBufferUtils$TlvConstructor");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.getActualLength():int, dex:  in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.getActualLength():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.getActualLength():int, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public int getActualLength() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.getActualLength():int, dex:  in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.getActualLength():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.getActualLength():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.getArray():byte[], dex: 
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
        public byte[] getArray() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.getArray():byte[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.getArray():byte[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putByte(int, byte):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
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
        public android.net.wifi.nan.TlvBufferUtils.TlvConstructor putByte(int r1, byte r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putByte(int, byte):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putByte(int, byte):android.net.wifi.nan.TlvBufferUtils$TlvConstructor");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putByteArray(int, byte[]):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
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
        public android.net.wifi.nan.TlvBufferUtils.TlvConstructor putByteArray(int r1, byte[] r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putByteArray(int, byte[]):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putByteArray(int, byte[]):android.net.wifi.nan.TlvBufferUtils$TlvConstructor");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putByteArray(int, byte[], int, int):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex:  in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putByteArray(int, byte[], int, int):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putByteArray(int, byte[], int, int):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public android.net.wifi.nan.TlvBufferUtils.TlvConstructor putByteArray(int r1, byte[] r2, int r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putByteArray(int, byte[], int, int):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex:  in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putByteArray(int, byte[], int, int):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putByteArray(int, byte[], int, int):android.net.wifi.nan.TlvBufferUtils$TlvConstructor");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putInt(int, int):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex:  in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putInt(int, int):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putInt(int, int):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public android.net.wifi.nan.TlvBufferUtils.TlvConstructor putInt(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putInt(int, int):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex:  in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putInt(int, int):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putInt(int, int):android.net.wifi.nan.TlvBufferUtils$TlvConstructor");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putShort(int, short):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex:  in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putShort(int, short):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putShort(int, short):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public android.net.wifi.nan.TlvBufferUtils.TlvConstructor putShort(int r1, short r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putShort(int, short):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex:  in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putShort(int, short):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putShort(int, short):android.net.wifi.nan.TlvBufferUtils$TlvConstructor");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putString(int, java.lang.String):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
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
        public android.net.wifi.nan.TlvBufferUtils.TlvConstructor putString(int r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putString(int, java.lang.String):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.putString(int, java.lang.String):android.net.wifi.nan.TlvBufferUtils$TlvConstructor");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.wrap(byte[]):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
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
        public android.net.wifi.nan.TlvBufferUtils.TlvConstructor wrap(byte[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.wrap(byte[]):android.net.wifi.nan.TlvBufferUtils$TlvConstructor, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvConstructor.wrap(byte[]):android.net.wifi.nan.TlvBufferUtils$TlvConstructor");
        }

        public TlvConstructor putZeroLengthElement(int type) {
            checkLength(0);
            addHeader(type, 0);
            return this;
        }
    }

    public static class TlvElement {
        public int mLength;
        public int mOffset;
        public byte[] mRefArray;
        public int mType;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.net.wifi.nan.TlvBufferUtils.TlvElement.<init>(int, int, byte[], int):void, dex: 
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
        private TlvElement(int r1, int r2, byte[] r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.net.wifi.nan.TlvBufferUtils.TlvElement.<init>(int, int, byte[], int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvElement.<init>(int, int, byte[], int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.wifi.nan.TlvBufferUtils.TlvElement.<init>(int, int, byte[], int, android.net.wifi.nan.TlvBufferUtils$TlvElement):void, dex: 
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
        /* synthetic */ TlvElement(int r1, int r2, byte[] r3, int r4, android.net.wifi.nan.TlvBufferUtils.TlvElement r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.wifi.nan.TlvBufferUtils.TlvElement.<init>(int, int, byte[], int, android.net.wifi.nan.TlvBufferUtils$TlvElement):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvElement.<init>(int, int, byte[], int, android.net.wifi.nan.TlvBufferUtils$TlvElement):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.net.wifi.nan.TlvBufferUtils.TlvElement.getByte():byte, dex: 
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
        public byte getByte() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.net.wifi.nan.TlvBufferUtils.TlvElement.getByte():byte, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvElement.getByte():byte");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.net.wifi.nan.TlvBufferUtils.TlvElement.getInt():int, dex: 
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
        public int getInt() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.net.wifi.nan.TlvBufferUtils.TlvElement.getInt():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvElement.getInt():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.net.wifi.nan.TlvBufferUtils.TlvElement.getShort():short, dex: 
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
        public short getShort() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.net.wifi.nan.TlvBufferUtils.TlvElement.getShort():short, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvElement.getShort():short");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.nan.TlvBufferUtils.TlvElement.getString():java.lang.String, dex: 
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
        public java.lang.String getString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.nan.TlvBufferUtils.TlvElement.getString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvElement.getString():java.lang.String");
        }
    }

    public static class TlvIterable implements Iterable<TlvElement> {
        private byte[] mArray;
        private int mArrayLength;
        private int mLengthSize;
        private int mTypeSize;

        /* renamed from: android.net.wifi.nan.TlvBufferUtils$TlvIterable$1 */
        class AnonymousClass1 implements Iterator<TlvElement> {
            private int mOffset;
            final /* synthetic */ TlvIterable this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.1.<init>(android.net.wifi.nan.TlvBufferUtils$TlvIterable):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass1(android.net.wifi.nan.TlvBufferUtils.TlvIterable r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.1.<init>(android.net.wifi.nan.TlvBufferUtils$TlvIterable):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvIterable.1.<init>(android.net.wifi.nan.TlvBufferUtils$TlvIterable):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.1.hasNext():boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public boolean hasNext() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.1.hasNext():boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvIterable.1.hasNext():boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.1.next():android.net.wifi.nan.TlvBufferUtils$TlvElement, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public android.net.wifi.nan.TlvBufferUtils.TlvElement next() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.1.next():android.net.wifi.nan.TlvBufferUtils$TlvElement, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvIterable.1.next():android.net.wifi.nan.TlvBufferUtils$TlvElement");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.1.next():java.lang.Object, dex: 
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
            public /* bridge */ /* synthetic */ java.lang.Object next() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.1.next():java.lang.Object, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvIterable.1.next():java.lang.Object");
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.-get0(android.net.wifi.nan.TlvBufferUtils$TlvIterable):byte[], dex: 
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
        /* renamed from: -get0 */
        static /* synthetic */ byte[] m530-get0(android.net.wifi.nan.TlvBufferUtils.TlvIterable r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.-get0(android.net.wifi.nan.TlvBufferUtils$TlvIterable):byte[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvIterable.-get0(android.net.wifi.nan.TlvBufferUtils$TlvIterable):byte[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.-get1(android.net.wifi.nan.TlvBufferUtils$TlvIterable):int, dex: 
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
        /* renamed from: -get1 */
        static /* synthetic */ int m531-get1(android.net.wifi.nan.TlvBufferUtils.TlvIterable r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.-get1(android.net.wifi.nan.TlvBufferUtils$TlvIterable):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvIterable.-get1(android.net.wifi.nan.TlvBufferUtils$TlvIterable):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.-get2(android.net.wifi.nan.TlvBufferUtils$TlvIterable):int, dex: 
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
        /* renamed from: -get2 */
        static /* synthetic */ int m532-get2(android.net.wifi.nan.TlvBufferUtils.TlvIterable r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.-get2(android.net.wifi.nan.TlvBufferUtils$TlvIterable):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvIterable.-get2(android.net.wifi.nan.TlvBufferUtils$TlvIterable):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.-get3(android.net.wifi.nan.TlvBufferUtils$TlvIterable):int, dex:  in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.-get3(android.net.wifi.nan.TlvBufferUtils$TlvIterable):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.-get3(android.net.wifi.nan.TlvBufferUtils$TlvIterable):int, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        /* renamed from: -get3 */
        static /* synthetic */ int m533-get3(android.net.wifi.nan.TlvBufferUtils.TlvIterable r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.-get3(android.net.wifi.nan.TlvBufferUtils$TlvIterable):int, dex:  in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.-get3(android.net.wifi.nan.TlvBufferUtils$TlvIterable):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvIterable.-get3(android.net.wifi.nan.TlvBufferUtils$TlvIterable):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.<init>(int, int, byte[], int):void, dex: 
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
        public TlvIterable(int r1, int r2, byte[] r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.<init>(int, int, byte[], int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvIterable.<init>(int, int, byte[], int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.toString():java.lang.String, dex: 
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
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.wifi.nan.TlvBufferUtils.TlvIterable.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.TlvIterable.toString():java.lang.String");
        }

        public Iterator<TlvElement> iterator() {
            return new AnonymousClass1(this);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.wifi.nan.TlvBufferUtils.<init>():void, dex: 
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
    private TlvBufferUtils() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.wifi.nan.TlvBufferUtils.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.nan.TlvBufferUtils.<init>():void");
    }
}
