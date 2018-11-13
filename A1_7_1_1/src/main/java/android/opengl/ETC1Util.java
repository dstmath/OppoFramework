package android.opengl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
public class ETC1Util {

    public static class ETC1Texture {
        private ByteBuffer mData;
        private int mHeight;
        private int mWidth;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.opengl.ETC1Util.ETC1Texture.<init>(int, int, java.nio.ByteBuffer):void, dex: 
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
        public ETC1Texture(int r1, int r2, java.nio.ByteBuffer r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.opengl.ETC1Util.ETC1Texture.<init>(int, int, java.nio.ByteBuffer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.opengl.ETC1Util.ETC1Texture.<init>(int, int, java.nio.ByteBuffer):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.opengl.ETC1Util.ETC1Texture.getData():java.nio.ByteBuffer, dex: 
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
        public java.nio.ByteBuffer getData() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.opengl.ETC1Util.ETC1Texture.getData():java.nio.ByteBuffer, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.opengl.ETC1Util.ETC1Texture.getData():java.nio.ByteBuffer");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.opengl.ETC1Util.ETC1Texture.getHeight():int, dex: 
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
        public int getHeight() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.opengl.ETC1Util.ETC1Texture.getHeight():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.opengl.ETC1Util.ETC1Texture.getHeight():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.opengl.ETC1Util.ETC1Texture.getWidth():int, dex: 
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
        public int getWidth() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.opengl.ETC1Util.ETC1Texture.getWidth():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.opengl.ETC1Util.ETC1Texture.getWidth():int");
        }
    }

    public static void loadTexture(int target, int level, int border, int fallbackFormat, int fallbackType, InputStream input) throws IOException {
        loadTexture(target, level, border, fallbackFormat, fallbackType, createTexture(input));
    }

    public static void loadTexture(int target, int level, int border, int fallbackFormat, int fallbackType, ETC1Texture texture) {
        if (fallbackFormat != 6407) {
            throw new IllegalArgumentException("fallbackFormat must be GL_RGB");
        } else if (fallbackType == 33635 || fallbackType == 5121) {
            int width = texture.getWidth();
            int height = texture.getHeight();
            Buffer data = texture.getData();
            if (isETC1Supported()) {
                GLES10.glCompressedTexImage2D(target, level, 36196, width, height, border, data.remaining(), data);
                return;
            }
            int pixelSize = fallbackType != 5121 ? 2 : 3;
            int stride = pixelSize * width;
            Buffer decodedData = ByteBuffer.allocateDirect(stride * height).order(ByteOrder.nativeOrder());
            ETC1.decodeImage(data, decodedData, width, height, pixelSize, stride);
            GLES10.glTexImage2D(target, level, fallbackFormat, width, height, border, fallbackFormat, fallbackType, decodedData);
        } else {
            throw new IllegalArgumentException("Unsupported fallbackType");
        }
    }

    public static boolean isETC1Supported() {
        int[] results = new int[20];
        GLES10.glGetIntegerv(34466, results, 0);
        int numFormats = results[0];
        if (numFormats > results.length) {
            results = new int[numFormats];
        }
        GLES10.glGetIntegerv(34467, results, 0);
        for (int i = 0; i < numFormats; i++) {
            if (results[i] == 36196) {
                return true;
            }
        }
        return false;
    }

    public static ETC1Texture createTexture(InputStream input) throws IOException {
        byte[] ioBuffer = new byte[4096];
        if (input.read(ioBuffer, 0, 16) != 16) {
            throw new IOException("Unable to read PKM file header.");
        }
        ByteBuffer headerBuffer = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder());
        headerBuffer.put(ioBuffer, 0, 16).position(0);
        if (ETC1.isValid(headerBuffer)) {
            int width = ETC1.getWidth(headerBuffer);
            int height = ETC1.getHeight(headerBuffer);
            int encodedSize = ETC1.getEncodedDataSize(width, height);
            ByteBuffer dataBuffer = ByteBuffer.allocateDirect(encodedSize).order(ByteOrder.nativeOrder());
            int i = 0;
            while (i < encodedSize) {
                int chunkSize = Math.min(ioBuffer.length, encodedSize - i);
                if (input.read(ioBuffer, 0, chunkSize) != chunkSize) {
                    throw new IOException("Unable to read PKM file data.");
                }
                dataBuffer.put(ioBuffer, 0, chunkSize);
                i += chunkSize;
            }
            dataBuffer.position(0);
            return new ETC1Texture(width, height, dataBuffer);
        }
        throw new IOException("Not a PKM file.");
    }

    public static ETC1Texture compressTexture(Buffer input, int width, int height, int pixelSize, int stride) {
        ByteBuffer compressedImage = ByteBuffer.allocateDirect(ETC1.getEncodedDataSize(width, height)).order(ByteOrder.nativeOrder());
        ETC1.encodeImage(input, width, height, pixelSize, stride, compressedImage);
        return new ETC1Texture(width, height, compressedImage);
    }

    public static void writeTexture(ETC1Texture texture, OutputStream output) throws IOException {
        ByteBuffer dataBuffer = texture.getData();
        int originalPosition = dataBuffer.position();
        try {
            int width = texture.getWidth();
            int height = texture.getHeight();
            ByteBuffer header = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder());
            ETC1.formatHeader(header, width, height);
            byte[] ioBuffer = new byte[4096];
            header.get(ioBuffer, 0, 16);
            output.write(ioBuffer, 0, 16);
            int encodedSize = ETC1.getEncodedDataSize(width, height);
            int i = 0;
            while (i < encodedSize) {
                int chunkSize = Math.min(ioBuffer.length, encodedSize - i);
                dataBuffer.get(ioBuffer, 0, chunkSize);
                output.write(ioBuffer, 0, chunkSize);
                i += chunkSize;
            }
        } finally {
            dataBuffer.position(originalPosition);
        }
    }
}
